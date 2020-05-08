 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package week6;
 
 import multicast.MulticastMessage;
 import multicast.MulticastQueue;
 import replicated_calculator.ClientEvent;
 import replicated_calculator.ClientEventConnect;
 import replicated_calculator.ClientEventDisconnect;
 import replicated_calculator.ClientEventVisitor;
 import week6.multicast.CalculatorQueue;
 import week6.multicast.messages.ClientEventMessage;
 
 /**
  *
  * @author Randi K. Hillerøe <silwing@gmail.com>
  */
 public class ServerListener extends Thread {
 	CalculatorQueue queue;
 	ServerReplicated visitor;
 	long timeout = 1;
 	
 	public ServerListener(CalculatorQueue queue, ServerReplicated v){
 		this.queue = queue;
 		visitor = v;
 	}
 
 	@Override
 	public void run() {
 		ClientEventMessage msg;
 		
 		try{
			while(true){
 				if((msg = queue.get()) != null){
 						ClientEvent ce = msg.getClientEvent();
 						if(msg.getClientEvent() instanceof ClientEventConnect 
 								& !msg.getSender().getAddress().equals(queue.getAddress()))
 						{
 							ClientEventConnect connect = (ClientEventConnect)msg.getClientEvent();
 							ce = new ClientEventRemoteConnect(connect.clientName,connect.eventID,connect.clientAddress);
 						}
 						else if(msg.getClientEvent() instanceof ClientEventDisconnect & !msg.getSender().getAddress().equals(queue.getAddress())){
 							ClientEventDisconnect dis = (ClientEventDisconnect)msg.getClientEvent();
 							ce = new ClientEventRemoteDisconnect(dis.clientName,dis.eventID);
 						}
 						
 						ce.accept(visitor);
 				}
				
 				Thread.sleep(timeout);
 			}
 		}catch(InterruptedException e){
 			// Stop
 			System.out.println("Interrupted D:");
 		}
 	}
 }
