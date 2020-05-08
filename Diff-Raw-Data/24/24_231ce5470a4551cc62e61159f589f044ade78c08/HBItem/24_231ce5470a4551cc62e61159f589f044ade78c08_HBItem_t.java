 import java.util.*;
 
 /*
  * The item stored in HBQ(HoldBackQueue) for multi-casting messages
  */
 public class HBItem {
 	
 	/* identification */
 	String src;
 	int mc_id;
 	
 	TimeStamp ts;
 	TimeStampedMessage message = null;
 	ArrayList<Boolean> ack_list;
 	
 	/* for timeout and retransmit */
 	long timestamp;
 	long wait_interval = 30000 + ((new Random()).nextLong() % 2000);
 	
 	/* global singleton */
 	MessagePasser mp;
 	
 	public HBItem(TimeStampedMessage msg) {
 		
 		this.mp = MessagePasser.getInstance();		
 		this.ack_list = new ArrayList<Boolean>();
 		for(int i = 0; i < this.mp.num_nodes; i++) {
 			this.ack_list.add(false);
 		}		
 		this.timestamp = System.currentTimeMillis();
 		
 //		
 //		if(msg.type.equals("multicast"))
 //			System.out.println("OK");
 //		if(msg.kind.equals("ack"))
 //			System.out.println("OK");
 
 		/* get an original multicast message */
 		if(msg.type.equals("multicast") && !msg.kind.equals("ack")) {
 			
 			System.out.println("In HBItem(): I am about to create Multicast HBItem");
 			
 			this.message = msg;//.clone();
 			this.src = this.message.src;
 			//this.src = msg.src;
 			this.mc_id = this.message.mc_id;//msg.mc_id;
 			this.ts = this.message.ts;//msg.ts;
 			if(this.message == null)
 			{
 				System.out.println("We have a null message, in HBItem Constructor");
 			}
 			this.tryAcceptAck(msg);
 			System.out.println("Message created in HBItem --> "+this.message.toString());
 		}
 		
 		/* get an ACK */
 		else if(msg.type.equals("multicast") && msg.kind.equals("ack")) {
 			
 			System.out.println("In HBItem(): I am about to create ACK HBItem for message "+msg.toString());
 			
 			//System.out.println("In the get ack of HBItem");
 			if(msg.payload == null) {
 				System.out.println("In HBItem(): ACK message has no payload! Craete HBItem failed.");
 				return;
 			}
 			String[] payload = ((String)msg.payload).split("\t");
 			this.src = payload[0];
 			this.mc_id = Integer.parseInt(payload[1]);
 			System.out.println("Src: "+this.src+"\nMC_ID: "+this.mc_id);
 			System.out.println("VTS raw is "+payload[2]);
 			this.ts = ClockService.getInstance("vector", this.mp.num_nodes).parseTS(payload[2]);
 			System.out.println("VTS: "+this.ts);
 		
 			this.tryAcceptAck(msg);
 			System.out.println("After TACK in ACK block");
 			//System.out.println("Message created in HBItem --> "+this.message.toString());
 		}
 		
 		/* get a retransmitted multicast message by someone else */
 		else if(msg.type.equals("unicast") && msg.kind.equals("retransmit")) {
 			
 			System.out.println("In HBItem(): I am about to create Retransmit HBItem");
 			System.out.println("Message payload: "+ msg.payload);
 			this.message = (TimeStampedMessage)msg.payload;
 			this.src = this.message.src;
 			this.mc_id = this.message.mc_id;
 			this.ts = this.message.ts;
 			
 			this.tryAcceptAck(msg);
 			System.out.println("Message created in HBItem --> "+this.message.toString());
 		}
 		
 		/* else: the default case */
 		else
 			return;
 		
 	}
 	
 	public void setMessage(TimeStampedMessage msg) {
 		this.message = msg;//.clone();
 	}
 	
 	
 	/*
 	 * Determine whether this message is ready based on seq# and acked nodes.
 	 * Because this is a polling function, it will continuously print anything
 	 * in here when things are not in order.
 	 */
 	public boolean isReady() {
 		
 		boolean is_ready = true;
 		
 		/* message should not be empty */
 		if(this.message == null)
 		{
 			is_ready = false;
 			//System.out.println("We have a null message, in isReady()");
 		}
 		
 		/* check if it is acked by everyone */ 
 		for(int i = 0; i < ack_list.size(); i++)
 		{
 			//System.out.println(ack_list.get(i)+" has a value of "+ack_list.get(i).booleanValue());
 			if(ack_list.get(i).booleanValue() == false) {
 				is_ready = false;
 				break;
 			}
 		}
 		//System.out.println("About to test ID "+this.mc_id+" with expected val "+Integer.parseInt(this.mp.mc_ids.get(this.mp.names_index.get(this.src)).toString()) + 1);
 //		/* check if it is the next MC message in the sequence */
 		if(this.mc_id != Integer.parseInt(this.mp.mc_ids.get(this.mp.names_index.get(this.src)).toString()) + 1)
 		{
 			//System.out.println(this.mc_id+" is not the next message in the sequence (expected "+this.mp.mc_ids.get(this.mp.names_index.get(this.src)) + 1+")");
			is_ready = false;
 		}
 		//System.out.println("IS ready? "+is_ready);
 		return is_ready;
 		
 	}
 	
 	/*
 	 * Ack something
 	 */
 	public void tryAcceptAck(TimeStampedMessage msg) {
 		
 		/* get an original multicast message */
 		if(msg.type.equals("multicast") && !msg.kind.equals("ack")) {
 			if(this.src.equals(msg.src) && this.mc_id == msg.mc_id) {
 				int index = this.mp.names_index.get(msg.src); //the original sender has received the message
 				this.ack_list.set(index, true);
 				index = this.mp.names_index.get(this.mp.local_name); //since our mc msg goes only to one dest at a time, we can use that to set our own bit
 				this.ack_list.set(index, true);
 			}			
 		}
 		
 		/* get an multicast ack */
 		else if(msg.type.equals("multicast") && msg.kind.equals("ack")) {
 			String[] payload = ((String)msg.payload).split("\t");
 			if(this.src.equals(payload[0]) && this.mc_id == Integer.parseInt(payload[1])) {
 				int index = this.mp.names_index.get(msg.src); //acknowledge that the mcAck sender has received the message
 				System.out.println(msg.src+" has an index of "+index);
 				this.ack_list.set(index, true);
 //				System.out.println("Ack List set: "+this.ack_list.toString());
 			}			
 		}
 		
 		/* get a retransmitted multicast message by someone else */
 		else if(msg.type.equals("unicast") && msg.kind.equals("retransmit")) {			
 			message = (TimeStampedMessage)msg.payload;
 			if(this.message.src.equals(message.src) && this.message.mc_id == message.mc_id) //is it possible for this to fail???
 			{
 				int index = this.mp.names_index.get(this.src); //make sure the original sender has been marked as acknowledged
 				this.ack_list.set(index, true);
 				index = this.mp.names_index.get(msg.src); //make sure the retransmitter has been marked as acknowledged
 				this.ack_list.set(index, true);
 			}			
 		}
 		
 		/* else: the default case */
 		else
 			return;
 	}
 	
 	
 	/*
 	 * Determine whether time-out for resend
 	 * and will update timestamp if timeout
 	 */
 	public boolean isTimeOut() {
 		
 		boolean is_timeout = false;
 		
 		long now = System.currentTimeMillis();
 		if(this.timestamp + this.wait_interval <= now) {
 			is_timeout = true;
 			timestamp = now;
 		}
		if(this.message == null)
			is_timeout = false;
 		return is_timeout;
 		
 	}
 	  
 	/*
 	 * Return the nodes that need retransmission of the original MC message
 	 */
 	public ArrayList<String> nodesNeedResend() {
 		  
 		ArrayList<String> nodes = new ArrayList<String>();
 		  
 		for(int i = 0 ; i < this.ack_list.size(); i++) {
 			System.out.println("Node "+this.mp.getName(i)+" has a value of "+this.ack_list.get(i));
 			if(this.ack_list.get(i) == false) {
 				nodes.add(this.mp.getName(i));
 				System.out.println("Added node "+ i+" "+this.mp.getName(i)+" to list of nodes need resend.");
 			}
 		}
 		  
 		return nodes;	  
 	}
 	
 	
 	  /*
 	   * Compare the relationship between two messages
 	   * Return:
 	   * 	-1 : this happens before msg
 	   * 	 0 : they are concurrent
 	   *     1 : msg happens before this
 	   */
 	  public int compareOrder(HBItem hbi)
 	  {
 	    int order = 0;
 	    if(this.ts.isLess(hbi.ts))
 	    	order = -1;
 	    else if(hbi.ts.isLess(this.ts))
 	    	order = 1;
 	    else
 	    	order = 0;
 	    return order;
 	  }
 
 	  
 	  public String toString() {
 		  
 		  String print = "";
 		  
 		  print += "src=" + this.src;
 		  print += ", mc_id=" + this.mc_id;
 		  print += ", timestamp=" + this.ts.toString();
 		  print += ", type=" + this.message.type;
 		  print += ", kind=" + this.message.kind;
 		  print += "\n\t" + "Acked list:";
 		  for(int i = 0; i < this.ack_list.size(); i++) {
 			  String name = this.mp.getName(i);
 			  if(this.ack_list.get(i).booleanValue() == true)
 				  print += name + "=true ";
 			  else
 				  print += name + "=false ";
 		  }
 		  return print;
 	  }
 }
