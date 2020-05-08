 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package week6;
 
 import multicastqueue.MulticastMessage;
 import multicast.MulticastQueue;
 import multicastqueue.MulticastMessagePayload;
 import multicastqueue.MulticastQueueTotalOnly;
 import replicated_calculator.ClientEvent;
 import replicated_calculator.ClientEventConnect;
 import replicated_calculator.ClientEventDisconnect;
 import replicated_calculator.ClientEventVisitor;
 import week6.multicast.messages.ClientEventMessage;
 
 /**
  *
  * @author Randi K. Hillerøe <silwing@gmail.com>
  */
 public class ServerListener extends Thread {
 	MulticastQueueTotalOnly<ClientEvent> queue;
 	ServerReplicated visitor;
 	long timeout = 1;
 	public boolean run = true;
 	
 	public ServerListener(MulticastQueueTotalOnly<ClientEvent> queue, ServerReplicated v){
 		this.queue = queue;
 		visitor = v;
 	}
 
 	@Override
 	public void run() {
 		MulticastMessage msg;
 		
 		try{
 			while(run){
 				if((msg = queue.get()) != null){
 						if(msg instanceof MulticastMessagePayload){
 							ClientEvent ce = (ClientEvent)((MulticastMessagePayload)msg).getPayload();
							System.out.println("Got message: "+ce);
 							if(ce instanceof ClientEventConnect 
									& !msg.getSender().getAddress().equals(queue.getAddress()))
 							{
 								ClientEventConnect connect = (ClientEventConnect)ce;
 								ce = new ClientEventRemoteConnect(connect.clientName,connect.eventID,connect.clientAddress);
 							}
							else if(ce instanceof ClientEventDisconnect & !msg.getSender().getAddress().equals(queue.getAddress())){
 								ClientEventDisconnect dis = (ClientEventDisconnect)ce;
 								ce = new ClientEventRemoteDisconnect(dis.clientName,dis.eventID);
 							}

 							ce.accept(visitor);
 						}
 				}
 				Thread.sleep(timeout);
 			}
 		}catch(InterruptedException e){
 			// Stop
 			System.out.println("Interrupted D:");
 		}
 	}
 }
