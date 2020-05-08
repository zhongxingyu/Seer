 import java.util.ArrayList;
 
 class HBQThread implements Runnable {
 	
 	MessagePasser mp;
 	
 	public HBQThread() {
 		this.mp = MessagePasser.getInstance();
 	}
 	
 	public void run() {
 		
 		try{
 			while(true) {
 				this.mp.globalLock.lock();
 				/* push ready message to rcv_buf */
 				for(int i = 0; i < this.mp.hbq.size(); i++) {
 					if(!this.mp.hbq.isEmpty()) {
 						if(this.mp.hbq.get(0).isReady()) {
 							System.out.println("In HBQThread() $$ Before getReadyMessage, the HBQ size =  " + this.mp.hbq.size());
 							this.mp.printHBQ();
 							TimeStampedMessage msg = this.mp.getReadyMessage();
 							System.out.println("In HBQThread() $$ We got the ready message: " + msg.toString());
 							System.out.println("In HBQThread() $$ After getReadyMessage, the HBQ size =  " + this.mp.hbq.size());
 							System.out.println("In HBQThread() $$ Before offer rcv_buf, the rcv_buf size =  " + this.mp.rcv_buf.size());
 							this.mp.rcv_buf.nonblockingOffer(msg);
 							System.out.println("In HBQThread() $$ After offer rcv_buf, the rcv_buf size =  " + this.mp.rcv_buf.size());
 						}
 						else
 						{
 							break;
 						}
 					}
 				}
 						
 					
 				/* if timeout, resend to non-acked nodes of this message*/
 				for(int i = 0; i < this.mp.hbq.size(); i++) {
 					if(this.mp.hbq.get(i).isTimeOut() && !this.mp.hbq.get(i).isReady()) {
 						System.out.println(this.mp.hbq.get(i).src+" "+this.mp.hbq.get(i).mc_id+" is Out of Time!!!");
 						ArrayList<String> needed_nodes = this.mp.hbq.get(i).nodesNeedResend();
 						
 						for(int j = 0; j < needed_nodes.size(); j++) {
							//System.out.println("Resending to "+needed_nodes.get(i)+" instead of "+needed_nodes.get(j)); //needs to be j instead of i?
							this.mp.resend(this.mp.hbq.get(j).message, needed_nodes.get(j));
 						}
 					}
 				}
 				this.mp.globalLock.unlock();
 				Thread.sleep(50);
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 		finally{
 			this.mp.globalLock.unlock();
 		}
 				
 	}
 	
 }
