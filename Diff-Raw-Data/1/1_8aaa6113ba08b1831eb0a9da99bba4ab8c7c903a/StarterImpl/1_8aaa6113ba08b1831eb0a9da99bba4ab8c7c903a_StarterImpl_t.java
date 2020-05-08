 package starter;
 
 import ggTCalculator.Coordinator;
 import ggTCalculator.ProcessHelper;
 import ggTCalculator.StarterPOA;
 
 import java.util.ArrayList;
 
 import org.omg.CORBA.ORB;
 import org.omg.PortableServer.POA;
 
 public class StarterImpl extends StarterPOA {
     Coordinator coordinator;
     POA rootPOA;
     Thread sdh;
     ORB orb;
     String name;
     int nextID = 0;
     ArrayList<ggTCalculator.Process> processes = new ArrayList<ggTCalculator.Process>();
 
     public StarterImpl(POA rootPOA, ORB orb, Thread sdh, Coordinator coordinator, String name) {
         this.coordinator = coordinator;
         this.rootPOA = rootPOA;
         this.sdh = sdh;
         this.orb = orb;
         this.name = name;
     }
 
     @Override
     public void createProcess(int count) {
         for (int i = 0; i < count; i++) {
             // create new processes
             ProcessImpl newproc = new ProcessImpl(name, nextID, coordinator);
             ggTCalculator.Process ref;
             try {
                 ref = ProcessHelper.narrow(rootPOA.servant_to_reference(newproc));
                 processes.add(ref);
                 // connect processes to coordinator
                 coordinator.addProcess(name, nextID, ref);
                 nextID++;
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
         }
 
     }
 
     @Override
     public String getName() {
         return name;
     }
 
     @Override
     public void quit() {
         // remove hook
         Runtime.getRuntime().removeShutdownHook(sdh);
 
         // quit ggT processes
         quitProcess();
 
         // init shutdomn thread
         new Thread(new Runnable() {
             @Override
             public void run() {
                 orb.shutdown(true);
             }
         }).start();
 
     }
 
     @Override
     public void quitProcess() {
         for (ggTCalculator.Process process : processes) {
             process.stop();
         }
         processes.clear();
        nextID = 0;
     }
 
 }
