 package adsim.core;
 
 import lombok.*;
 
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.Future;
 import java.util.concurrent.FutureTask;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import adsim.handler.FIFOCollector;
 import adsim.handler.FloodingReplayer;
 import adsim.handler.InfoSpreader;
 import adsim.handler.IntervalPublisher;
 import adsim.handler.RecentKeepCollector;
 import adsim.handler.RegularKeepCollector;
 import adsim.handler.RoundsMotion;
 
 public class Case implements ICase {
     public static final int DEFAULT_STEP_LIMIT = 100;
     @Getter
     private int id;
    
     @Getter
     private int tryId;
 
     @Getter
     @Setter
     private String name;
 
     @Getter
     @Setter
     private long stepLimit;
 
     @Getter
     private double fieldSize;
 
     @Getter
     private int spreadStep;
 
     @Getter
     private CollectMode collectMode;
 
     @Getter
     @Setter
     private int watchNodeCount;
 
     private ArrayList<Node> nodes;
     private final LinkedBlockingQueue<Object[]> result;
 
     public Case() {
         nodes = new ArrayList<Node>();
         result = new LinkedBlockingQueue<Object[]>();
         spreadStep = 0;
         collectMode = CollectMode.FIFO;
         this.fieldSize = 1000;
     }
 
     public Case(int id, int inTypeId, int nodesCount, double fieldSize,
             int spreadStep,
             CollectMode collectMode, int stepLimit, double publishPerStep) {
         this();
         this.id = id;
         this.tryId = inTypeId;
         this.stepLimit = stepLimit;
         this.fieldSize = fieldSize;
         this.spreadStep = spreadStep;
         this.collectMode = collectMode;
         // build handlers
         val handlers = new ArrayList<INodeHandler>();
         handlers.add(new RoundsMotion());
         if (spreadStep > 0) {
             handlers.add(new InfoSpreader(spreadStep));
         }
         handlers.add(collectMode.toHandler());
         handlers.add(new FloodingReplayer());
         handlers.add(new IntervalPublisher(publishPerStep));
         val handler = new CompositeNodeHandler(handlers).prune();
         this.nodes = new ArrayList<Node>(nodesCount);
         for (int i = 0; i < nodesCount; i++) {
             this.nodes.add(new Node(handler.clone()));
         }
     }
 
     @Override
     public List<Node> getNodes() {
         return nodes;
     }
 
     public void addNode(Node node) {
         nodes.add(node);
     }
 
     public void addNodes(Collection<Node> nodes2) {
         nodes.addAll(nodes2);
     }
 
     @Override
     public void tellResult(ResultReport report) {
         try {
             result.put(new Object[] {
                     id,
                     tryId,
                     nodes.size(),
                     fieldSize,
                     collectMode,
                     spreadStep,
                     report.getMessagesCreatedCount(),
                     report.getMessagesAcceptedCount(),
                     report.getPacketsSentCount(),
                     report.getPacketsDisposedCount()
             });
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public Future<Object[]> getResult() {
         val f = new FutureTask<Object[]>(new Callable<Object[]>() {
             @Override
             public Object[] call() throws Exception {
                 return result.take();
             }
         });
         f.run();
         return f;
     }
 
     public static enum CollectMode {
 
         FIFO, RecentKeep, RegularKeep;
 
         public INodeHandler toHandler() {
             switch (this) {
             case FIFO:
                 return new FIFOCollector();
             case RecentKeep:
                 return new RecentKeepCollector();
             case RegularKeep:
                 return new RegularKeepCollector();
             default:
                 throw new IllegalStateException();
             }
         }
     }
 }
