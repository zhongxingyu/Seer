 import java.io.*;

 
 public class RicartAgrawala {
 
 	public boolean bRequestingCS;
 	public int outstandingReplies;
 	public int highestSeqNum;
 	public int seqNum;
 	public int nodeNum;
 	public Driver driverModule;
 	
 	//Holds our writers to use
 	public PrintWriter[] w;
 	
 	//Hard coded to 3 right now, for 3 other nodes in network
 	public int channelCount = 3;
 	
 	public boolean[] replyDeferred;
 	
 	public RicartAgrawala(int nodeNum, int seqNum, Driver driverModule){
 		bRequestingCS = false;
 		
 		outstandingReplies = channelCount;
 		
 		highestSeqNum = 0;
 		this.seqNum = seqNum;
 		this.driverModule = driverModule;
 		
 		w = new PrintWriter[channelCount];
 		
 		// Node number is also used for priority (low node # == higher priority in RicartAgrawala scheme)
 		// Node numbers are [1,channelCount]; since we're starting at 1 check for errors trying to access node '0'.
 		this.nodeNum = nodeNum;
 		
 		replyDeferred = new boolean[channelCount];
 	}
 	
 	/** Invocation (begun in driver module with request CS) */
 	public boolean invocation(){
 		
 		bRequestingCS = true;
 		seqNum = highestSeqNum + 1;
 		
 		outstandingReplies = channelCount;
 		
 		for(int i = 1; i <= channelCount + 1; i++){
 			if(i != nodeNum){
 				requestTo(seqNum, nodeNum, i);
 			}
 		}
 		
 		
 		while(outstandingReplies > 0)
 		{
 			try{
 				Thread.sleep(5);
 				
 			}
 			catch(Exception e){
 				
 			}
 			/*wait until we have replies from all other processes */
 		}
 
 		//We return when ready to enter CS
 		return true;
 		
 		
 	}
 	
 	// The other half of invocation
 	public void releaseCS()
 	{
 		bRequestingCS = false;
 		
 		for(int i = 0; i < channelCount; i++){
 			if(replyDeferred[i]){
 				replyDeferred[i] = false;
 				if(i < (nodeNum - 1))
 					replyTo(i + 1);
 				else
 					replyTo(i + 2);
 			}
 		}
 	}
 	
 	/** Receiving Request 
 	 * 
 	 *	@param	j	The incoming message's sequence number
 	 *	@param	k	The incoming message's node number 
 	 * 
 	 */
 	public void receiveRequest(int j, int k){
 		System.out.println("Received request from node " + k);
 		boolean bDefer = false;
 		
 		highestSeqNum = Math.max(highestSeqNum, j);
 		bDefer = bRequestingCS && ((j > seqNum) || (j == seqNum && k > nodeNum));
 		if(bDefer){
 			System.out.println("Deferred sending message to " + k);
 			if(k > nodeNum)
 				replyDeferred[k - 2] = true;
 			else
 				replyDeferred[k - 1] = true;
 		}
 		else{ 
 			System.out.println("Sent reply message to " + k);
 			replyTo(k);
 		}
 		
 	}
 	
 	/** Receiving Replies */
 	public void receiveReply(){
 		outstandingReplies = Math.max((outstandingReplies - 1), 0);
 		//System.out.println("Outstanding replies: " + outstandingReplies);
 	}
 	
 	public void replyTo(int k)
 	{
 		System.out.println("Sending REPLY to node " + k);
 		if(k > nodeNum)
 		{
 			w[k-2].println("REPLY," + k);
 		}
 		else
 		{
 			w[k-1].println("REPLY," + k);
 		}
 	}
 	
 	public void requestTo(int seqNum, int nodeNum, int i)
 	{
 		System.out.println("Sending REQUEST to node " + (((i))));
 		if(i > nodeNum)
 		{
 			w[i-2].println("REQUEST," + seqNum + "," + nodeNum);
 		}
 		else
 		{
 			w[i-1].println("REQUEST," + seqNum + "," + nodeNum);
 		}
 	}
 
 }
