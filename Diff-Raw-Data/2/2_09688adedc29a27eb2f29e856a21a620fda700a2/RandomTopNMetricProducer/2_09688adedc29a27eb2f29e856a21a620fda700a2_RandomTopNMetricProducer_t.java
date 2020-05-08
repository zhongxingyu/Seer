 package dashboard.server.producer;
 
 import java.util.Random;
 
 import dashboard.server.metric.Metric;
 import dashboard.server.metric.TopNMetric;
 import dashboard.server.metric.TrendMetric;
 
 public class RandomTopNMetricProducer extends AbstractMetricProducer {
 
     private class ServiceThread extends Thread {
 
         private static final int MAX = 20;
         private static final int RATE = 500;
 
         private boolean stopSignal = false;
 
         private Random rand = new Random();
 
         public Metric generateMetric() {
             TopNMetric topTenMetric = new TopNMetric();
             topTenMetric.setLabel("Top N Metric");
             topTenMetric.setTime(System.currentTimeMillis());
             TrendMetric trendMetric = null;
             for (int i = 0; i < TopNMetric.SIZE; i++) {
                 trendMetric = new TrendMetric();
                 trendMetric.setLabel("Trend Metric");
                trendMetric.setValue(rand.nextInt(MAX));
                 trendMetric.setTime(System.currentTimeMillis());
                 if (rand.nextBoolean()) {
                     trendMetric.setTrend(rand.nextDouble());
                 } else {
                     trendMetric.setTrend(rand.nextDouble() * -1);
                 }
                 trendMetric.setInfo("Information regarding trend");
                 topTenMetric.getMetrics()[i] = trendMetric;
             }
 
             return topTenMetric;
         }
 
         @Override
         public void run() {
             stopSignal = false;
             while (!stopSignal) {
                 metricBroker.publish(generateMetric(), TOPIC);
                 try {
                     Thread.sleep(RATE);
                 } catch (InterruptedException e) {
                     // do nothing
                 }
             }
         }
 
         public void sendStopSignal() {
             stopSignal = true;
         }
     }
 
     private static final String TOPIC = "dashboard.metric.RandomTopTenMetric";
 
     private ServiceThread serviceThread;
 
     @Override
     public void start() {
         if (serviceThread == null) {
             serviceThread = new ServiceThread();
             serviceThread.start();
         }
     }
 
     @Override
     public void stop() {
         if (serviceThread != null) {
             serviceThread.sendStopSignal();
             try {
                 serviceThread.join();
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
     }
 
 }
