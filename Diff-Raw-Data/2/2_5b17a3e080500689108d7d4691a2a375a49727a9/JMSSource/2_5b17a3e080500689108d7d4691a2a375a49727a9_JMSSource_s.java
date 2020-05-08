 package com.redshape.jobs.jms.sources;
 
 import com.redshape.jobs.IJob;
 import com.redshape.jobs.JobException;
 import com.redshape.jobs.JobStatus;
 import com.redshape.jobs.result.IJobResult;
 import com.redshape.jobs.sources.IJobSource;
 import com.redshape.persistence.entities.DtoUtils;
 import com.redshape.persistence.entities.IDTO;
 import com.redshape.utils.Commons;
 import com.redshape.utils.events.AbstractEventDispatcher;
 import org.apache.log4j.Logger;
 
 import javax.jms.*;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Cyril A. Karpenko <self@nikelin.ru>
  * @package com.redshape.jobs.sources
  * @date 2/8/12 {6:30 PM}
  */
 public class JMSSource extends AbstractEventDispatcher implements IJobSource<IJob> {
     private static final Logger log = Logger.getLogger(JMSSource.class);
     
     private int workChunkSize;
     private int maxFailuresCount;
     private int receiveTimeout;
     private int updateInterval;
     private int maxReceivingTime;
     
     private QueueConnection connection;
     private QueueSession session;
     private MessageProducer producer;
     private MessageConsumer consumer;
     
     private Queue producingQueueAddr;
     private Queue consumingQueueAddr;
     
     private String name;
     private String consumingQueue;
     private String producingQueue;
 
     public JMSSource( String name,
                       QueueConnection connection,
                       String consumingQueue,
                       String producingQueue,
                       int updateInterval,
                       int receiveTimeout,
                       int workChunkSize,
                       int maxFailuresCount )
         throws JobException {
         super();
 
         Commons.checkArgument( workChunkSize > 0 );
         Commons.checkNotNull(connection);
         
         this.name = name;
         this.updateInterval = updateInterval;
         this.receiveTimeout = receiveTimeout;
         this.workChunkSize = workChunkSize;
         this.maxFailuresCount = maxFailuresCount;
         this.consumingQueue = consumingQueue;
         this.producingQueue = producingQueue;
         
         this.connection = connection;
 
         this.init();
     }
 
     @Override
     public String getName() {
         return this.name;
     }
 
     public int getMaxReceivingTime() {
         return maxReceivingTime;
     }
 
     public void setMaxReceivingTime(int maxReceivingTime) {
         this.maxReceivingTime = maxReceivingTime;
     }
 
     protected MessageConsumer getConsumer() {
         return this.consumer;
     }
 
     protected MessageProducer getProducer() {
         return this.producer;
     }
     
     protected int getReceiveTimeout() {
         return this.receiveTimeout;
     }
     
     protected int getMaxFailuresCount() {
         return this.maxFailuresCount;
     }
     
     protected int getWorkChunkSize() {
         return this.workChunkSize;
     }
 
     protected QueueSession getSession() {
         return this.session;
     }
 
     protected QueueConnection getConnection() {
         return this.connection;
     }
     
     protected String getConsumingQueue() {
         return this.consumingQueue;
     }
     
     protected String getProducingQueue() {
         return this.producingQueue;
     }
 
     protected void init() throws JobException {
         try {
             this.session = this.connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
             this.session.run();
         } catch ( JMSException e ) {
             throw new JobException("Unable to establish JMS session through provided connection");
         }
 
         try {
             this.producingQueueAddr = this.session.createQueue( this.getProducingQueue() );
             this.producer = this.session.createSender( this.producingQueueAddr );
         } catch ( JMSException e ) {
             throw new JobException( "Unable to start messages producing thread", e );
         }
 
         try {
             this.consumingQueueAddr = this.session.createQueue(this.getConsumingQueue());
             this.consumer = this.session.createConsumer( this.consumingQueueAddr );
         } catch ( JMSException e ) {
             throw new JobException( "Unable to start messages consuming thread", e );
         }
     }
 
     @Override
     public int getUpdateInterval() {
         return this.updateInterval;
     }
 
     @Override
     public void complete(IJob job, IJobResult result) throws JobException {
         if ( job.getState().equals( JobStatus.COMPLETED ) ) {
             return;
         }
 
     }
 
     @Override
     public void save(IJob entity) throws JobException {
         try {
             ObjectMessage message = this.getSession().createObjectMessage();
             message.setJMSDestination( this.producingQueueAddr );
             message.setObject(entity);
             
             this.getProducer().send(message);
         } catch ( JMSException e ) {
             throw new JobException( e.getMessage(), e );
         }
     }
 
     @Override
     public List<IJob> fetch() throws JobException {
         int failuresCount = 0;
         
         List<IJob> result = new ArrayList<IJob>();
         long startReceivingTime = System.currentTimeMillis();
         while ( result.size() <= this.getWorkChunkSize()
                 && failuresCount < this.getMaxFailuresCount() ) {
             try {
                 Message message = this.getConsumer().receive(this.getReceiveTimeout());
                 if ( message == null ) {
                     continue;
                 }
 
                 log.info("New job received....");
 
                 if ( message instanceof ObjectMessage ) {
                     Object object = ((ObjectMessage) message).getObject();
                     if ( IJob.class.isAssignableFrom( object.getClass() ) ) {
                         log.info("Adding " + object.getClass().getCanonicalName() + " job object to processing chunk...");
                         result.add( (IJob) ( object instanceof IDTO ? DtoUtils.fromDTO( (IDTO) object) : object ) );
                     }
                 } else {
                     log.info("Unsupported job type...");
                 }
 
                 log.info("Sending acknowledge");
                 
                 message.acknowledge();
 
                 if ( result.size() >= this.getWorkChunkSize() || ( this.getMaxReceivingTime() != 0 &&
                                                                     ( startReceivingTime - System.nanoTime() ) >= this.getMaxReceivingTime() ) ) {
                     log.debug("Breaking a receiving cycle with a chunk of " + result.size() + "...");
                     break;
                 }
            } catch ( JMSException e ) {
                 log.error( e.getMessage(), e );
                 failuresCount++;
             }
         }
 
         return result;
     }
 }
