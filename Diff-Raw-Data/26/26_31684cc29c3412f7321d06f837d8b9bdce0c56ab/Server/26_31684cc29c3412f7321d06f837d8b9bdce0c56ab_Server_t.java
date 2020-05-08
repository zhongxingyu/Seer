package net.novasharper.AlephNaught.Server;
 import org.zeromq.ZMQ;
 import TerminalIO.KeyboardReader;
 
 public class Server {
 	public static void main(String[] args){
 		//Prepare objects and context
 		KeyboardReader reader = new TerminalIO.KeyboardReader();
 		Player players = new Player();
 		ZMQ.Context context = ZMQ.context(1);
 		ZMQ.Socket send = context.socket(ZMQ.PUSH); //Push port to all clients
 		ZMQ.Socket receive = context.socket(ZMQ.PULL); //Pull port for all clients
 		
 		send.bind("tcp://*:42425");
 		receive.bind("tcp://*:42424");
 		
 		while(true){ //Infinite loop for now
 			//Recieve message from client
 			String msg = new String(receive.recv(0)).trim();
 			
 			
 			//Heartbeat
 			try {
 				Thread.sleep(10);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			
 		}
 	}
 }
