 package Server;
 
 import java.io.IOException;
 
 import Messages.Message;
 import Utilities.InputReader;
 
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
 		InputReader ir=new InputReader();
 		Message tcpMessage=null;
 		
 		while(!done && !error){
 			if(state.getState().getClass()!=lastState.getClass()){
 				timeEnteredState=System.currentTimeMillis();
 			}
 			String input=ir.getSubmitted();
 			if(input.startsWith(":quit")){
 				done=true;
 			}
 			else if(input.startsWith(":dc")){
 				try {
 					tcp.close();
 				} catch (IOException e) {}
 				System.out.println("Disconnecting");
 				state.state=new ServerDisconnected();
 			}
 			else if(input.equals("")){
 				tcpMessage=tcp.read();
 			}
 			state.process(tcp, tcpMessage, timeEnteredState);
 			lastState = state.getState();
 		}
 	}
 }
