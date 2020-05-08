 package Server;
 
 import java.io.IOException;
 
 import Messages.ErrorMessage;
 import Messages.Message;
 import Communications.TCP;
 
 public class ServerThread implements Runnable{
 	TCP tcp=null;
 	public void setTCP(TCP in){
 		tcp=in;
 	}
 	public void run() {
 		boolean done=false;
 		boolean error=false;
 		ServerCurrentState state=new ServerCurrentState();
 		long timeEnteredState=0;
 		ServerState lastState=null;
 		Message tcpMessage=null;
 		
 		while(!done && !error){
			if(state.getState().getClass()!=lastState.getClass()){
 				timeEnteredState=System.currentTimeMillis();
 			}
 			tcpMessage = tcp.read();
 			if (tcpMessage instanceof ErrorMessage && tcpMessage.getCorrect()) {
 				System.out
 						.println("\rAn error occured while communicating.\nDisconnecting");
 				try {
 					tcp.close();
 				} catch (IOException e) {
 				}
 				state.state = new ServerDisconnected();
 				done = true;
 				continue;
 			}
 			state.process(tcp, tcpMessage, timeEnteredState);
 			lastState = state.getState();
 		}
 	}
 }
