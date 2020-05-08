 package com.od.jtimeseries.net.udp;
 
 import com.od.jtimeseries.identifiable.Identifiable;
 import com.od.jtimeseries.net.udp.message.SeriesDescriptionMessage;
 import com.od.jtimeseries.net.udp.message.TimeSeriesValueMessage;
 import com.od.jtimeseries.net.udp.message.UdpMessage;
 import com.od.jtimeseries.net.udp.message.UdpMessageFactory;
 import com.od.jtimeseries.net.udp.message.javaio.JavaIOMessageFactory;
 import com.od.jtimeseries.timeseries.IdentifiableTimeSeries;
 import com.od.jtimeseries.timeseries.TimeSeriesEvent;
 import com.od.jtimeseries.timeseries.TimeSeriesListenerAdapter;
 import com.od.jtimeseries.util.NamedExecutors;
 import com.od.jtimeseries.util.logging.LogMethods;
 import com.od.jtimeseries.util.logging.LogUtils;
 
 import java.util.Set;
 import java.util.WeakHashMap;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 /**
  * Listen to IdentifiableTimeSeries for append events and send a UDP datagram for each appended value
  * Currently works only for single item appends
  *
  * The maximum publish rate can be set, to limit the possible network overhead
  * Default max is 25 datagrams / second
  * = 25 * 8192 bytes (assuming max datagram size is 8192 bytes)  == 204800 bytes, so a bit less than 256KB/s worst case
  */
 public class UdpPublisher extends TimeSeriesListenerAdapter {
 
     private static final LogMethods logMethods = LogUtils.getLogMethods(UdpPublisher.class);
     private static final int DEFAULT_MAX_MESSAGES_PER_SECOND = 25;
 
     private ScheduledExecutorService rateControllingExecutor = NamedExecutors.newSingleThreadScheduledExecutor("UdpPublisherQueue" + this);
     private UdpClient udpClient;
     private int maxDatagramsPerSecond;
     private int maxQueueSize;
     private AppendPublishingListener appendPublishingListener = new AppendPublishingListener();
     private LinkedBlockingQueue<UdpMessage> messageQueue;
     private AtomicBoolean started = new AtomicBoolean();
     private long delayTimeMicroseconds;
     private UdpMessageFactory udpMessageFactory = new JavaIOMessageFactory();
     private WeakHashMap<Identifiable, Object> seriesWithDescriptionsPublished = new WeakHashMap<Identifiable, Object>();
     private static final int DEFAULT_MAX_QUEUE_SIZE = 8192;
 
     public UdpPublisher(UdpClient udpClient) {
         this(udpClient, DEFAULT_MAX_MESSAGES_PER_SECOND, DEFAULT_MAX_QUEUE_SIZE);
     }
 
     public UdpPublisher(UdpClient udpClient, int maxDatagramsPerSecond) {
         this(udpClient, maxDatagramsPerSecond, DEFAULT_MAX_QUEUE_SIZE);
     }
 
     public UdpPublisher(UdpClient udpClient, int maxDatagramsPerSecond, int maxQueueSize) {
         this.udpClient = udpClient;
         this.maxDatagramsPerSecond = maxDatagramsPerSecond;
         this.maxQueueSize = maxQueueSize;
         messageQueue = new LinkedBlockingQueue<UdpMessage>(maxQueueSize);
         delayTimeMicroseconds = 1000000 / maxDatagramsPerSecond;
     }
 
     /**
      * Set the message factory which determines which varient of udp message to send
      * The default is javaio varient, which is the most efficient
      */
     public void setUdpMessageFactory(UdpMessageFactory udpMessageFactory) {
         this.udpMessageFactory = udpMessageFactory;
     }
 
     /**
      * Publish all single item appends to series s
      */
     public void publishAppends(IdentifiableTimeSeries s) {
         s.addTimeSeriesListener(appendPublishingListener);
     }
 
     /**
      * Stop publishing single items appends for series s
      */
     public void stopPublishing(IdentifiableTimeSeries s) {
         s.removeTimeSeriesListener(appendPublishingListener);
     }
 
     /**
      * Publish the description for this timeseries
      * This is performed automatically at start of series data publication, but subsequent changes to the description
      * will not be published automatically, and this method provides a means to send an update
      */
     public void publishDescription(IdentifiableTimeSeries identifiable) {
         SeriesDescriptionMessage d = udpMessageFactory.createTimeSeriesDescriptionMessage(
             identifiable.getPath(), identifiable.getDescription()
         );
         seriesWithDescriptionsPublished.put(identifiable, null);
         safelyAddToQueue(d);
     }
 
     private class AppendPublishingListener extends TimeSeriesListenerAdapter {
         public void itemsAddedOrInserted(TimeSeriesEvent e) {
             if ( e.isAppend() ) {
                 IdentifiableTimeSeries i = (IdentifiableTimeSeries)e.getSource();
 
                 //only publish the description at the first point we actually have valid data to publish
                 //this avoids creating unnecessary empty series in downstream components
                 if ( ! isDescriptionPublished(i)) {
                     publishDescription(i);
                 }
 
                 if (e.getItems().size() == 1) {
                     TimeSeriesValueMessage m = udpMessageFactory.createTimeSeriesValueMessage(
                         i.getPath(),
                         e.getItems().get(0)
                     );
                     safelyAddToQueue(m);
 
                     if (! started.getAndSet(true)) {
                         startPublisherQueue();
                     }
                 }
             }
         }
     }
 
     private boolean isDescriptionPublished(IdentifiableTimeSeries i) {
         return seriesWithDescriptionsPublished.containsKey(i);
     }
 
     private void safelyAddToQueue(UdpMessage m) {
         try {
             messageQueue.add(m);
         } catch (IllegalStateException ise) {
             logMethods.warn("Failed to publish UDP message, outbound queue is full," +
                 " max datagrams per second is " + maxDatagramsPerSecond + " is this enough?", ise);
         }
     }
 
     private void startPublisherQueue() {
        rateControllingExecutor.scheduleWithFixedDelay(new Runnable() {
             public void run() {
                 try {
                     udpClient.sendMessages(messageQueue);
                 } catch (Throwable t) {
                     logMethods.error("Failed to send UDP message(s)", t);
                 }
             }
         }, delayTimeMicroseconds, delayTimeMicroseconds, TimeUnit.MICROSECONDS);
     }
 
     @Override
     public String toString() {
         return "UdpPublisher{" +
                 "maxQueueSize=" + maxQueueSize +
                 ", maxDatagramsPerSecond=" + maxDatagramsPerSecond +
                 ", started=" + started +
                 ", udpMessageFactory=" + udpMessageFactory +
                 '}';
     }
 }
