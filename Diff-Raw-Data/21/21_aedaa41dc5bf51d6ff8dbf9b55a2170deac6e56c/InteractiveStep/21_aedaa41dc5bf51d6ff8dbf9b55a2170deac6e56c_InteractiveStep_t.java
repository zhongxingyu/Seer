 package memeograph.generator.jdi;
 
 import com.sun.jdi.Location;
import com.sun.jdi.ThreadReference;
 import com.sun.jdi.event.Event;
 import com.sun.jdi.event.StepEvent;
 import com.sun.jdi.request.EventRequest;
 import com.sun.jdi.request.StepRequest;
import com.sun.jdi.request.ThreadStartRequest;
 import memeograph.Config;
 import memeograph.graph.Graph;
 
 
 /**
  * An Interactive graph generator that expects input as to what its next
  * step should be.
  *
  * nextGraph() will only return after a call to step() is made to decide the
  * next step.
  * @author Sandro Badame <a href="mailto:s.badame@gmail.com">s.badame&amp;gmail.com</a>
  */
 public class InteractiveStep extends JDI{
     
     public enum Depth {
         STEP_INTO(StepRequest.STEP_INTO),
         STEP_OVER(StepRequest.STEP_OVER),
         STEP_OUT(StepRequest.STEP_OUT);
 
         private int value;
         Depth(int val){value = val;}
         public int value(){ return value; }
     }
 
     public enum Size {
         STEP_MIN(StepRequest.STEP_MIN),
         STEP_LINE(StepRequest.STEP_LINE);
 
         private int value;
         Size(int val){value = val;}
         public int value(){ return value; }
     }
 
     private boolean hasNextStep = false;
     private final Object stepLock = new Object();
     private volatile Location currentlocation = null;
 
     private volatile boolean waitingOnGraph = false;//The only case of a volatile
                                                     //boolean that I've ever seen!
 
     public InteractiveStep(Config c){
         super(c);
     }
 
     /**
      * Listens for a step of the given size. This will queue the next call
      * to {@link #getNextGraph()} to return the object graph when this step
      * is detected. If there are multiple calls to step the subsequent calls
      * will be discarded until the graph at getNextGraph() is consumed.
      * @param s the size of the next step
      * @param d the depth of the next step
      */
     public void step(Size s, Depth d){
         if (waitingOnGraph || hasDied()) {
             return;
         }else{
             waitingOnGraph = true;
         }
         StepRequest sr = getVirtualMachine().eventRequestManager().createStepRequest(getMainThread(), s.value(), d.value() );
         sr.setSuspendPolicy(EventRequest.SUSPEND_ALL);
         sr.addClassFilter(Config.getConfig().getProperty(Config.TARGET_MAIN));
         sr.addCountFilter(1);
 
         addVMEventListener(sr, new EventAction() {
             public Graph doAction(Event e) {
                 e.request().disable();
                 currentlocation = ((StepEvent)e).location();
                 return generateGraph();
             }
         });
 
        if (getMainThread().status() == 0) { //Thread is dead??
            for ( ThreadReference t : getVirtualMachine().allThreads()){
                System.out.println("t.name() = " + t.name());
                System.out.println("t.status() = " + t.status());
            }
        }
         sr.enable();
         synchronized(stepLock){hasNextStep = true; stepLock.notify();}
     }
 
     @Override
     public Graph getNextGraph(){
        synchronized(stepLock){
            while(hasNextStep == false){
                try {
                    stepLock.wait();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            Graph g = super.getNextGraph();
            hasNextStep = false;
            waitingOnGraph = false;
            return g;
        }
     }
 
     @Override
     public void VMStarted(){
         step(Size.STEP_MIN, Depth.STEP_INTO);
     }
 
     public Location getCurrentLocation(){
         return currentlocation;
     }

    @Override
    public void startupEventRequests(){
        //This looks insane, but we need an event that can stall the VM before it
        //dies, listening to VMStarted doesn't seem to do the trick for some
        // odd reason...
        ThreadStartRequest es = getVirtualMachine().eventRequestManager().createThreadStartRequest();
        es.setSuspendPolicy(EventRequest.SUSPEND_ALL);
        es.enable();
    }
 }
