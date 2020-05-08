 import java.io.IOException;
 
 import Communication.PCBluetooth;
 
 public class CommanderI extends Robotnik._CommanderDisp {
 	
 	private PCBluetooth pcb;
 	
 	public CommanderI(){
 		try {
 			pcb = new PCBluetooth();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
			System.exit(1)
 		}
 	}
 		
 	public void sendMessage(int message, Ice.Current current){
 		try {
 			pcb.sendMessage(message);
 		} catch (IOException e) {
			System.err.println("Send Message Failed.")
 			e.printStackTrace();
 		}
 	}
 }
 
