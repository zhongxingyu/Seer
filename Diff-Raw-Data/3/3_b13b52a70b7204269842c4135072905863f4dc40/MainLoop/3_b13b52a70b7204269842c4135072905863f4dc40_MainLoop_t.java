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
 	static boolean firstCall=true;
 	
 	
 	public static void main(String[] args) {
 		state.setup();
 		try {
 			ur=new UDPReceiver();
 			us=new UDPSender();
 		} catch (SocketException | UnknownHostException e) {
 			System.out.println(e.getMessage());
 			System.out.println("Could not set up UDP socket");
 			System.exit(-1);
 		}
 		Thread tcpListen=new Thread(tcp);
 		tcpListen.start();
 		Thread irThread=new Thread(ir);
 		irThread.start();
 		long timeEnteredState=System.currentTimeMillis();
 		Message udpMessage=null;
 		Message tcpMessage=null;
 		System.out.println("Welcome to our chat client");
 		System.out.println("To quit, type :quit");
 		System.out.println("To disconnect at any time, type :dc");
 		System.out.println("To change your username, type :user <new username>");
 		while(!error && !done){
 			if(lastState==null || !state.state.getClass().equals(lastState.getClass())){
 				firstCall=true;
 				timeEnteredState=System.currentTimeMillis();
 			}
 			else{
 				firstCall=false;
 			}
 
 			input=ir.getSubmitted();
 			if(input.startsWith(":dc")){
 				System.out.println("Disconnecting");
 				try{
 					tcp.close();
 				}
 				catch(Exception e){}
 				state.state=new Disconnected();
 				continue;
 			}
 			else if(input.startsWith(":quit")){
 				done=true;
 				System.out.println("Quitting");
 				continue;
 			}
 			else if(input.startsWith(":user")){
 				if(input.length()<7){
 					System.out.println("An argument is required");
 					continue;
 				}
 				User.setUserName(input.substring(6).trim());
 				System.out.println("Your username has been set to \""+User.getUserName()+"\"");
 				continue;
 			}
 			else if (input.equals("")) {
 				udpMessage = ur.read();
 				if(udpMessage==null){
 					tcpMessage = tcp.read();
 					if(tcpMessage instanceof ErrorMessage && tcpMessage.getCorrect()){
 						System.out.println("An error occured while communicating.\nDisconnecting");
 						try {
 							tcp.close();
 						} catch (Exception e) {}
 						state.state=new Disconnected();
 						continue;
 					}
 				}
 				else{
 					System.out.println("I have a udp message");
 					System.out.println(udpMessage.getCorrect());
 				}
 			}
 			lastState = state.getState();
 			state.process(input,tcp,us,udpMessage,tcpMessage,timeEnteredState,firstCall);
 
 		}
		System.out.println("Hit enter to finish exiting");
 		try {
 			tcp.close();
 		} catch (Exception e1) {}
 		try{
 			tcp.stop();
 		}catch(Exception e){}
 		try{
 			ir.stop();
 		}
 		catch(Exception e){}
 		try {
 			ur.stop();
 		} catch (Exception e) {}
 	}
 
 }
