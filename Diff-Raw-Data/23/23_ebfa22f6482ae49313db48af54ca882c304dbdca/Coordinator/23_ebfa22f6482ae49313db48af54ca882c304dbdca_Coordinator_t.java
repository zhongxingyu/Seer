 package coordinator;
 
 import ggTCalculator.CoordinatorHelper;
 
 import java.util.Properties;
 
 import org.omg.CORBA.ORB;
 import org.omg.CosNaming.NamingContextExt;
 import org.omg.CosNaming.NamingContextExtHelper;
 import org.omg.PortableServer.POA;
 import org.omg.PortableServer.POAHelper;
 
 public class Coordinator {
     static ORB orb;
     static ggTCalculator.Coordinator ref;
 
     public static void main(String[] args) {
 
         String host = "localhost";
         String port = "1080";
         String name = "Coordinator";
         final NamingContextExt nc;
 
         // digest commandline args
         if (args.length >= 1) {
             host = args[0];
         }
 
         if (args.length >= 2) {
             port = args[1];
         }
 
         if (args.length >= 3) {
             name = args[2];
         }
 
         try {
             // set properties
             Properties props = new Properties();
             props.put("org.omg.CORBA.ORBInitialPort", port);
             props.put("org.omg.CORBA.ORBInitialHost", host);
             orb = ORB.init(args, props);
 
             // get rootPOA holen und and activate POA Manager
             POA rootPoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
             rootPoa.the_POAManager().activate();
 
             // Resolve Nameservice and get coordinator reference
             nc = NamingContextExtHelper.narrow(orb.resolve_initial_references("NameService"));
 
             // create new instance of Coordinator
            CoordinatorImpl coordinator = new CoordinatorImpl(name,orb);
             ref = CoordinatorHelper.narrow(rootPoa.servant_to_reference(coordinator));
 
             // bind to NameService
             nc.rebind(nc.to_name(name), ref);
 
             System.out.println("Coordinator " + name + " gestartet auf " + host + ":" + port);
 
             orb.run();
 
             System.out.println("...Coordinator beendet");
         } catch (Exception e) {
             e.printStackTrace();
         }
 
     }
 
 }
