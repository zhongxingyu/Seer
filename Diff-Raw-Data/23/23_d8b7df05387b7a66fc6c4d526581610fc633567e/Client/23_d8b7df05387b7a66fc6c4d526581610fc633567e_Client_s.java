 package clt;
 
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.Random;
 
 import lab.Drone;
 import lab.Mutalisk;
 import lab.MutaliskHelper;
 import lab.Overlord;
 import lab.OverlordHelper;
 import lab.Xelnaga;
 import lab.XelnagaHelper;
 import lab._MutaliskStub;
 
 import org.omg.CORBA.ORB;
 import org.omg.CosNaming.NameComponent;
 import org.omg.CosNaming.NamingContextExt;
 import org.omg.CosNaming.NamingContextExtHelper;
 import org.omg.PortableServer.POA;
 import org.omg.PortableServer.POAHelper;
 
 public class Client {
	private static Xelnaga someRandomHiveOnSomePlanet;
	private final static String myNameIs = "Client"+(new Random()).nextInt();
		// TODO move to class (obj)

	public static void main(String[] args) {
 		try {
 			// things 'n stuff from oracle tutorial
 			ORB orb = ORB.init(args, null);
 			POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
 			rootpoa.the_POAManager().activate();
 			
 			OverlordImpl overlordImpl = new OverlordImpl();
 			org.omg.CORBA.Object ref = rootpoa.servant_to_reference(overlordImpl);
 			Overlord href = OverlordHelper.narrow(ref);
 			
 			org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
 			NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
 			
 			NameComponent path[] = ncRef.to_name(myNameIs);
 			ncRef.rebind(path, href);
 			
 			someRandomHiveOnSomePlanet = XelnagaHelper.narrow(ncRef.resolve_str("Xelnaga"));
 			
 			System.out.println("Drones:");
 			for(Drone i : someRandomHiveOnSomePlanet.searchSpaceAndDestroyProtossRace())
 				System.out.println("\tAvailable drone: #" + i.id() + " name:" + i.name());
 			
 			Drone ed = someRandomHiveOnSomePlanet.searchDroneByName("Ed");
//			System.out.println("Ed is a mutalisk? " + ((_MutaliskStub)ed)._is_a("Mutalisk"));
 			Mutalisk m_ed = MutaliskHelper.narrow(ed);
 			System.out.println("Ed?\n >>> " + m_ed);
 			
			someRandomHiveOnSomePlanet.controlDrone(ed.id(), OverlordHelper.narrow(ncRef.resolve_str(myNameIs)));
			someRandomHiveOnSomePlanet.controlDrone(ed.id(), OverlordHelper.narrow(ncRef.resolve_str(myNameIs)));
 			someRandomHiveOnSomePlanet.controlDrone(ed.id(), OverlordHelper.narrow(ncRef.resolve_str(myNameIs)));
 			
 			DateFormat jo = DateFormat.getTimeInstance(DateFormat.FULL);
 			
 			while(true) {
 				ed.command("Rotfl @ " + jo.format(new Date()));
 				m_ed.disableWPA();
 				Thread.sleep(5000);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
