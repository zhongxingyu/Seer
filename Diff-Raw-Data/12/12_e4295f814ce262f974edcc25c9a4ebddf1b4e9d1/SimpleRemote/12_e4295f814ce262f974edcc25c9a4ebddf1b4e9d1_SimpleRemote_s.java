 package BluetoothPOC;
 
 import lejos.pc.comm.NXTComm;
 import lejos.pc.comm.NXTCommException;
 import lejos.pc.comm.NXTCommFactory;
 import lejos.pc.comm.NXTInfo;
 
 public class SimpleRemote {
 	public static void main(String[] args) {
 		System.out.println("Simple NXT Remote");
 		
 		NXTComm comm = null;
 		try {
 			comm = NXTCommFactory.createNXTComm(NXTCommFactory.BLUETOOTH);
			comm.open(new NXTInfo());
 		} catch (NXTCommException e) {
 			System.out.println("Error creating NXTComm");
 			e.printStackTrace();
 			System.exit(1);
 		}
		
		// Search for bluetooth device
		System.out.print("Enter name: ");
		
 	}
 }
