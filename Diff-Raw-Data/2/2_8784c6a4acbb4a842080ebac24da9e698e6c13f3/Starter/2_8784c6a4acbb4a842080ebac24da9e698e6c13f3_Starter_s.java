 package starter;
 
 import ggTCalculator.Coordinator;
 import ggTCalculator.CoordinatorHelper;
 import ggTCalculator.StarterHelper;
 
 import java.util.Properties;
 import java.util.Random;
 
 import org.omg.CORBA.ORB;
 import org.omg.CosNaming.NamingContextExt;
 import org.omg.CosNaming.NamingContextExtHelper;
 import org.omg.PortableServer.POA;
 import org.omg.PortableServer.POAHelper;
 
 public class Starter {
     static ORB orb;
     static ggTCalculator.Starter ref;
     static Coordinator coordinator;
     
     
     public static void main(String[] args) {
         
         String host = "localhost";
         String port = "1080";
         String coordinator_name = "Coordinator";      
         final String name;
         Random rnd = new Random();
         final NamingContextExt nc;      
         
         // digest commandline args
         if(args[0] != null){
             host = args[0];
         }
         
         if(args[1] != null){
             port = args[1];
         }
         
         if(args[2] != null){
             coordinator_name = args[2];
         }
         
         if(args[3] != null){
             name = args[3];
         }else{
             name = "STARTER"+rnd.nextInt();
         }
         
         try{
         // set properties
         Properties props = new Properties();
         props.put("org.omg.CORBA.ORBInitialPort", port);
         props.put("org.omg.CORBA.ORBInitialHost", host);
         orb = ORB.init(args, props);
 
         // get rootPOA holen und and activate POA Manager
         POA rootPoa = POAHelper.narrow(orb
                 .resolve_initial_references("RootPOA"));
         rootPoa.the_POAManager().activate();
         
         // Resolve Nameservice and get coordinator reference
         nc = NamingContextExtHelper.narrow(orb
                 .resolve_initial_references("NameService"));
         coordinator = CoordinatorHelper.narrow(nc.resolve_str(coordinator_name));
         
         // shutdown thread
         Thread sdh = new Thread(new Runnable() {
             @Override
             public void run() {
                 ref.quit();
                 try {
                     nc.unbind(nc.to_name(name));
                 } catch (Exception e) {
                     e.printStackTrace();
                 } finally {
                     orb.shutdown(true);
                     orb.destroy();
                 }
             }
         });
         
         // create new instance of Starter and get reference
         StarterImpl starter = new StarterImpl(rootPoa, orb, sdh, coordinator, name);
         ref = StarterHelper.narrow(rootPoa.servant_to_reference(starter));
         
         // bind to nameservice        
         nc.rebind(nc.to_name(name), ref);
         
         // add starter to coordinator
         coordinator.addStarter(name, ref);    
         
         // add shutdown hook
         Runtime.getRuntime().addShutdownHook(sdh);
                 
         
         System.out.println("Starter "+name+" gestartet auf "+host+":"+port);
         
         orb.run();
 
        System.out.println("...Server beendet");
         }catch(Exception e){
             e.printStackTrace();
         }
 
     }
 
 }
