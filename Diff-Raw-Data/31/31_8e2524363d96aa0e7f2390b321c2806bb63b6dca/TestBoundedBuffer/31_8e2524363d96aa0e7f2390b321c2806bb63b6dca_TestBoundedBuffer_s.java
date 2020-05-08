 
 /**
  * <p>Title: Monitor package and examples</p>
  * <p>Description: </p>
  * <p>Copyright: Copyright (c) 2003</p>
  * <p>Company: Memorial University of Newfoundland</p>
  * @author Theodore S. Norvell
  * @version 1.0
  */
 
 /** Test a bounded buffer with multiple producers and consumers.
  */
 
 package examples.BoundedBuffer;
 
 import java.lang.management.*;
 
 import examples.util.DoneCounter;
 
 public class TestBoundedBuffer {
 
     public static void main(String[] args) {
         int CONSUMERS = 10;
         int PRODUCERS = 10;
         int totalNumActions = 10; 
         ObjectBoundedBuffer rw_controller = null;
 
         try {
             switch(args[0].charAt(0)) {
             case 'n':
                 rw_controller = new NaiveImplicitBoundedBuffer(4);
                 break;
             case 'l':
                 rw_controller = new HashSetBoundedBuffer(4);
                 break;
             case 'h':
                 rw_controller = new HashBoundedBuffer(4);
                 break;
             case 'p':
                 rw_controller = new PDSLLockBoundedBuffer(4);
                 break;
             case 'c':
                 rw_controller = new NConditionLockBoundedBuffer(4);
                 break;
             default:
                 rw_controller = new NaiveExplicitBoundedBuffer(4);
 
             }
             CONSUMERS = PRODUCERS = Integer.parseInt(args[1]);
             totalNumActions = Integer.parseInt(args[2]);
         } catch (Exception e) { /* use defaults */ 
             e.printStackTrace();
             if(rw_controller == null) {
                 rw_controller = new NaiveExplicitBoundedBuffer(4);
             }
         }
 
         DoneCounter doneCounter = new DoneCounter() ;
 
 
 
 
         doneCounter.set( CONSUMERS + PRODUCERS ) ;
         TestThread[] threads = new TestThread[CONSUMERS + PRODUCERS];
         long startTime = System.currentTimeMillis();
         //System.out.println("Please wait. This takes a while");
         for( int k=0 ; k < CONSUMERS ; ++k ) {
             TestThread w = new ObjectConsumer( rw_controller, doneCounter, totalNumActions/CONSUMERS ) ;
             threads[k] = w;
             w.start(); }
         for( int k=0 ; k < PRODUCERS ; ++k ) {
             TestThread r = new ObjectProducer( rw_controller, doneCounter, totalNumActions/PRODUCERS) ;
             threads[k + CONSUMERS] = r;
             r.start(); }
         doneCounter.waitForDone() ;
         long execTime = System.currentTimeMillis() - startTime;
         float totalCpuTime = 0.0f;
 
         for(int i = 0; i < threads.length; ++i) {
             totalCpuTime += threads[i].getCpuTime();
             //System.out.println("cpu time: " + threads[i].getCpuTime()/10e6);
         }
 
 
         System.out.println( execTime );
        System.out.println( totalCpuTime/10e6)  ;
        System.out.println( rw_controller.getSyncTime() / 10e6);
     }
 }
 
 class ObjectProducer extends TestThread {
 
     private ObjectBoundedBuffer boundedBuffer ;
     private DoneCounter doneCounter ;
     private int numActions = 10;
     ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
 
     ObjectProducer( ObjectBoundedBuffer bb, DoneCounter d, int n) {
         boundedBuffer = bb ; doneCounter = d ; numActions = n;}
 
     public void run() {
         long startTime = threadMXBean.getCurrentThreadCpuTime();
         for(int i=0 ; i < numActions ; ++i ) {
             delay(5);
 
             try {
                 boundedBuffer.put( new Object() ) ; }
             catch(InterruptedException e ) { }
         }
         //System.out.println("ObjectProducer " +Thread.currentThread() +" Done ") ; 
         long endTime = threadMXBean.getCurrentThreadCpuTime();
         cpuTime = endTime - startTime;
         doneCounter.increment() ;
     }
 }
 
 class ObjectConsumer extends TestThread {
 
     private ObjectBoundedBuffer boundedBuffer ;
     private DoneCounter doneCounter ;
     private int numActions = 10;
     ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
 
     ObjectConsumer( ObjectBoundedBuffer bb, DoneCounter d, int n) {
         boundedBuffer = bb ; doneCounter = d ; numActions = n; }
 
     public void run() {
         
         long startTime = threadMXBean.getCurrentThreadCpuTime();
 
         for(int i=0 ; i < numActions ; ++i ) {
             delay(5);
 
             try {
                 boundedBuffer.take() ; }
             catch(InterruptedException e ) { }
 
         }
         //System.out.println("ObjectConsumer " +Thread.currentThread() + " Done ") ; 
         long endTime = threadMXBean.getCurrentThreadCpuTime();
         cpuTime = endTime - startTime;
         doneCounter.increment() ;
     }
 }
