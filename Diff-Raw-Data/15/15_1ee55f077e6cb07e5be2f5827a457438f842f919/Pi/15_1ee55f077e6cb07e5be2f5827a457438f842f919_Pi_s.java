 package com.mathieuancelin.actors.cdi;
 
 import akka.routing.RoundRobinRouter;
 import akka.routing.RouterConfig;
 import com.mathieuancelin.actors.cdi.api.ActorConfig;
 import com.mathieuancelin.actors.cdi.api.ActorEvent;
 import com.mathieuancelin.actors.cdi.api.RouterConfigurator;
 import com.mathieuancelin.actors.cdi.api.To;
 import java.util.concurrent.CountDownLatch;
 import javax.enterprise.context.ApplicationScoped;
 import javax.enterprise.event.Observes;
 import javax.inject.Inject;
 
 public class Pi {
     
     public static final CountDownLatch latch = new CountDownLatch(1);
     
     public static class Calculate {
         public final int nrOfWorkers;
         public final int nrOfElements;
         public final int nrOfMessages;
         public Calculate(int nrOfWorkers, int nrOfElements, int nrOfMessages) {
             this.nrOfWorkers = nrOfWorkers;
             this.nrOfElements = nrOfElements;
             this.nrOfMessages = nrOfMessages;
         }
         @Override
         public String toString() {
             return "Calculate{" 
                     + "nrOfWorkers=" + nrOfWorkers 
                     + ", nrOfElements=" + nrOfElements 
                     + ", nrOfMessages=" + nrOfMessages + '}';
         }
     }
 
     public static class Work {
         public final int start;
         public final int nrOfElements;
         public Work(int start, int nrOfElements) {
             this.start = start;
             this.nrOfElements = nrOfElements;
         }
     }
 
     public static class Result {
         public final double value;
         public Result(double value) {
             this.value = value;
         }
     }
     
     @ActorConfig(withRouter=RouterConf.class)
     public static class Worker extends CDIActor {
         
         @Inject @To("/user/master") ActorEvent<Result> master;
 
         private double calculatePiFor(int start, int nrOfElements) {
             double acc = 0.0;
             for (int i = start * nrOfElements; i <= ((start + 1) * nrOfElements - 1); i++) {
                 acc += 4.0 * (1 - (i % 2) * 2) / (2 * i + 1);
             }
             return acc;
         }
 
         public void onReceive(@Observes Work work) {
             double result = calculatePiFor(work.start, work.nrOfElements);
             master.fire(new Result(result), self());
         }
     }
     
     public static class RouterConf implements RouterConfigurator {
 
         public RouterConfig getConfig() {
             return new RoundRobinRouter(4);
         }
 
         public String routerName() {
             return "pi";
         }
 
         public Class<? extends CDIActor> actorOf() {
             return Worker.class;
         }
        
     }
     
     @ActorConfig("master")
     @ApplicationScoped
     public static class Master extends CDIActor {
         private int nrOfMessages;
         private int nrOfElements;
         private double pi;
         private int nrOfResults;
         private long start;
         
         @Inject @To("/user/pi") ActorEvent<Work> router;
 
         public void listenCalculate(@Observes Calculate message) {
             this.nrOfMessages = message.nrOfMessages;
             this.nrOfElements = message.nrOfElements;
             for (int start = 0; start < nrOfMessages; start++) {
                 router.fire(new Work(start, nrOfElements), self());
             }
         }
         
         public void listenResult(@Observes Result result) {
             pi += result.value;
             nrOfResults += 1;
             if (nrOfResults == nrOfMessages) context().stop(self());
         } 
 
         @Override
         public void preStart() {
             System.out.println("Starting Pi computation");
             start = System.currentTimeMillis();
         }
 
         @Override
         public void postStop() {
             System.out.println(String.format(
                 "\n\tPi estimate: \t\t%s\n\tCalculation time: \t%s millis",
                 pi, (System.currentTimeMillis() - start)));
             latch.countDown();
         }
     }
     
 //    public static class Worker extends UntypedActor {
 //
 //        private double calculatePiFor(int start, int nrOfElements) {
 //            double acc = 0.0;
 //            for (int i = start * nrOfElements; i <= ((start + 1) * nrOfElements - 1); i++) {
 //                acc += 4.0 * (1 - (i % 2) * 2) / (2 * i + 1);
 //            }
 //            return acc;
 //        }
 //
 //        public void onReceive(Object message) {
 //            if (message instanceof Work) {
 //                Work work = (Work) message;
 //                double result = calculatePiFor(work.start, work.nrOfElements);
 //                getSender().tell(new Result(result));
 //            } else {
 //                throw new IllegalArgumentException("Unknown message [" + message + "]");
 //            }
 //        }
 //    }
 }
