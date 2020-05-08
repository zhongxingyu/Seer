 package memeograph.generator.jdb;
 
 import com.sun.jdi.ThreadReference;
 import com.sun.jdi.VirtualMachine;
 import com.sun.jdi.event.Event;
 import com.sun.jdi.event.StepEvent;
 import com.sun.jdi.request.EventRequest;
 import com.sun.jdi.request.StepRequest;
 import memeograph.Config;
 import memeograph.graph.Graph;
 
 public class InteractiveJDBGraphGenerator extends JDBGraphGenerator {
 
   public InteractiveJDBGraphGenerator(Config c){
       super(c);
   }
 
   @Override
   public void startupEventRequests(){ }
 
   @Override
   public void VMStarted(){
       VirtualMachine vm = getVirtualMachine();
       ThreadReference main = null;
 
       for (ThreadReference thread :  vm.allThreads()){
           if (thread.name().equals("main") ){
              main = thread;
              break;
           }
       }
 
       if (main == null) {
           throw new RuntimeException("Couldn't find the main thread!");
       }
 
       StepRequest sr = vm.eventRequestManager().createStepRequest(main, StepRequest.STEP_LINE, StepRequest.STEP_OVER);
 
       addVMEventListener(sr, new EventAction() {
           public Graph doAction(Event e) {
               StepEvent se = (StepEvent) e;
               return generateGraph();
           }
       });
 
       sr.addClassFilter("BSTExample");
       sr.setSuspendPolicy(EventRequest.SUSPEND_ALL);
       sr.enable();
   }
 
 }
