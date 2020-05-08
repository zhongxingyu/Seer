 import java.net.MalformedURLException;
 import java.rmi.Naming;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
 
 
public class BedSideRMIImpl extends UnicastRemoteObject implements BedSideRMI{
 
 	private String nurseRegistryID;
 	private String rmiRegistryName = "Bed-Side-RMI";
 	private BedInterface bedInterface;
 	
	public BedSideRMIImpl() throws RemoteException{ 
 		
 		bedInterface = new BedInterface(); 
 	}
 	
 	public synchronized void getPatientTrendInfo() throws RemoteException { 
 		
 		bedInterface.getPatientTrendDate();
 	}
 	
 	public synchronized void registerPatientToBedSide(String patientName, String patientID, String nurseRegistryID, String admitDate) throws RemoteException { 
 		
 		bedInterface.registerPatient(patientName, patientID, admitDate);
 	}
 	
 	public synchronized void dischargePatientFromBedSide(String patientId, String dischargeDate) throws RemoteException { 
 		
 		bedInterface.dischargePatient(patientId, dischargeDate);
 	}
 	
 	public synchronized void alarmAcknowledged() throws RemoteException { 
 		
 	}
 	
	public synchronized void startBedSideRMI() throws RemoteException, MalformedURLException { 
 		
 		System.out.println("Binding bed side to RMI registry...");
 		BedSideRMIImpl bedSideRMI = new BedSideRMIImpl(); 
		Naming.rebind("Bed-Side-RMI", bedSideRMI);
 	}
 	
 	public void stopBedSideRMI() throws RemoteException, MalformedURLException, NotBoundException { 
 		
 		System.out.println("Unbinding bed side from RMI registry...");
 		Naming.unbind(rmiRegistryName);
 	}
 }
