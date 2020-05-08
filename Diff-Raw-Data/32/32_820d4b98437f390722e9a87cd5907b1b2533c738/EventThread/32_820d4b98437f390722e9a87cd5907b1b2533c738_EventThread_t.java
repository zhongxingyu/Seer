 package deeva;
 
 import com.sun.jdi.VMDisconnectedException;
 import com.sun.jdi.VirtualMachine;
 import com.sun.jdi.event.EventIterator;
 import com.sun.jdi.event.EventQueue;
 import com.sun.jdi.event.EventSet;
 
 public class EventThread extends Thread {
     private final VirtualMachine vm;     // Running VM
     private final String[] excludes;     // Packages to exclude
     private final EventHandler handler;  // The object to receive each event.
 
     private boolean connected = true;  // Connected to VM
 
     EventThread(VirtualMachine vm, String[] excludes, EventHandler handler) {
         super("event-handler");
         this.vm = vm;
         this.excludes = excludes;
         this.handler = handler;
     }
 
     @Override
     public void run() {
         EventQueue queue = vm.eventQueue();
         while (connected) {
             try {
                 EventSet eventSet = queue.remove();
                 EventIterator it = eventSet.eventIterator();
                 while (it.hasNext()) {
                     handler.handleEvent(it.nextEvent());
                 }
                 //eventSet.resume();
             } catch (InterruptedException exc) {
                 // Ignore
             } catch (VMDisconnectedException discExc) {
                 //handleDisconnectedException();
                 break;
             } catch (Exception e) {
                 // I'M SO SORRY
                System.err.println(e.getMessage());
                e.printStackTrace();
             }
         }
     }
 }
 
 
