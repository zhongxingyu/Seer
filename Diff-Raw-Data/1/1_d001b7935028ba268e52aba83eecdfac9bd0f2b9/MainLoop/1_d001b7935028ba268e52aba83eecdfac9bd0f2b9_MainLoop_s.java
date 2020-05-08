 import java.io.IOException;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 
 import Communications.TCP;
 import Communications.UDPReceiver;
 import Communications.UDPSender;
 import Messages.ErrorMessage;
 import Messages.Message;
 import States.Disconnected;
 import States.State;
 import Utilities.CurrentState;
 import Utilities.InputReader;
 import Utilities.User;
 
 
 public class MainLoop {
 	static boolean error =false;
 	static boolean done = false;
 	static InputReader ir = new InputReader();
 	static String input;
 	static TCP tcp=new TCP();
 	static UDPReceiver ur;
 	static UDPSender us;
 	static CurrentState state=new CurrentState();
 	static State lastState=new Disconnected();
 	
 	
 	public static void main(String[] args) {
 		state.setup();
 		try {
 			ur=new UDPReceiver();
 			us=new UDPSender();
 		} catch (SocketException | UnknownHostException e) {
 			System.out.println(e.getMessage());
 			System.out.println("\rCould not set up UDP socket");
 			System.exit(-1);
 		}
 		Thread tcpListen=new Thread(tcp);
 		tcpListen.start();
 		Thread irThread=new Thread(ir);
 		irThread.start();
 		long timeEnteredState=System.currentTimeMillis();
 		Message udpMessage=null;
 		Message tcpMessage=null;
 		while(!error && !done){
 			if(lastState==null || !state.state.getClass().equals(lastState.getClass())){
 				timeEnteredState=System.currentTimeMillis();
 			}
 			//System.out.print("\r"+ir.getInput());
 			input=ir.getSubmitted();
 			if(input.startsWith(":dc")){
 				System.out.println("Disconnecting");
 				state.state=new Disconnected();
 				continue;
 			}
 			else if(input.startsWith(":quit")){
 				done=true;
 				try {
 					tcp.close();
 				} catch (Exception e) {}
 				System.out.println("Quitting");
 				continue;
 			}
 			else if(input.startsWith(":user")){
 				if(input.length()<7){
 					System.out.println("\rAn argument is required");
 					continue;
 				}
 				User.setUserName(input.substring(6));
 				continue;
 			}
 			else if (input.equals("")) {
 				udpMessage = ur.read();
 				if(udpMessage==null){
 					tcpMessage = tcp.read();
 					if(tcpMessage instanceof ErrorMessage && tcpMessage.getCorrect()){
 						System.out.println("\rAn error occured while communicating.\nDisconnecting");
 						try {
 							tcp.close();
 						} catch (IOException e) {}
 						state.state=new Disconnected();
 						continue;
 					}
 				}
 				else{
 					System.out.println("I have a udp message");
 				}
 			}
 			lastState = state.getState();
 			state.process(input,tcp,us,udpMessage,tcpMessage,timeEnteredState);
 
 		}
 		ir.stop();
 		try {
 			ur.stop();
 		} catch (InterruptedException e) {}
 	}
 
 }
