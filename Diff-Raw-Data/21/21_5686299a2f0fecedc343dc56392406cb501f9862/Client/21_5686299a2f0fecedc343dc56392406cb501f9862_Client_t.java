 package client;
 
 
 import ggTCalculator.*;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.Properties;
 
 
 import org.omg.CORBA.ORB;
 import org.omg.CosNaming.NamingContextExt;
 import org.omg.CosNaming.NamingContextExtHelper;
 import org.omg.PortableServer.POA;
 import org.omg.PortableServer.POAHelper;
 
 
 public class Client 
 {
 	private static Log log;
 	
 	public static void main(String[] args)
 	{
 			BufferedReader input = new BufferedReader(new InputStreamReader(
 					System.in));
 
 		try 
 		{
 			    /* Create Corba Connection */
 				Properties props = new Properties();
 				String coordname;
 				String help;
 				System.out.println("Coordinatorname eingeben");
 				coordname = input.readLine();
 				
 				System.out.println("Um Hilfe bei der Eingabe zu bekomme tippen sie Hilfe ein, sonst drcken sie Enter");
 				help = input.readLine();
 				if(help.equalsIgnoreCase("Hilfe")){
 					help();
 				}else{
 					
 				}
 				
 				
 				ORB orb = ORB.init(args, props);
 				POA rootPoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
 			    rootPoa.the_POAManager().activate();
 				NamingContextExt nc = NamingContextExtHelper.narrow(
 				orb.resolve_initial_references("NameService"));
 				/* Default Coordinator Name */
 				org.omg.CORBA.Object obj = nc.resolve_str(coordname);
 				Coordinator coord = CoordinatorHelper.narrow (obj);
 				LogImpl logI = new LogImpl();
				log = LogHelper.narrow(rootPoa.servant_to_reference(logI));
 				
 				if(args.length > 2){
 					int minggT = Integer.parseInt(args[2]);
 					int maxggT = Integer.parseInt(args[3]);
 					int minDe = Integer.parseInt(args[4]);
 					int maxDe = Integer.parseInt(args[5]);
 					int to = Integer.parseInt(args[6]);
 					int ggt = Integer.parseInt(args[7]);
 				
 					if(args[1].equalsIgnoreCase("calc")){
 						coord.calculate(to, minDe, maxDe, minggT, maxggT, ggt, log);
 					}else if(args[1].equalsIgnoreCase("quit")){
 						log.log(coordname, "beendet den Prozess");
 						coord.quit();
 						
 					}else if(args[1].equalsIgnoreCase("show")){
 						String[] s = coord.getStarterList();
 						log.log(coordname, "besorgt die Starterliste");
 						for(int i = 0; i < s.length; i++)
 							System.out.println(s[i]);
 					}else{
 						System.out.println("unbekannter Fehler");
 						log.log(coordname, "unbekannter Fehler");
 					}
 					
 				
 				
 				}
 				
 				
 		} 
 		catch (Exception ex) 
 		{
 				System.err.println (ex);
 				System.exit(1);
 		}
 		
 	}
 			
 	public static void help(){
 		
 	}
 
 }
