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
	public boolean run = true;
 	
 	public ServerListener(CalculatorQueue queue, ServerReplicated v){
 		this.queue = queue;
 		visitor = v;
 	}
 
 	@Override
 	public void run() {
 		ClientEventMessage msg;
 		
 		try{
			System.out.println("Before while...");
			while(run){
				System.out.println("Starting loop...");
 				if((msg = queue.get()) != null){
					System.out.println("Getting event...");
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
 						
						System.out.println("Received event of type " + ce.getClass().getName());
 						ce.accept(visitor);
 				}
				System.out.println("Before sleep");
 				Thread.sleep(timeout);
				System.out.println("Ending loop...");
 			}
 		}catch(InterruptedException e){
 			// Stop
 			System.out.println("Interrupted D:");
 		}
 	}
 }
