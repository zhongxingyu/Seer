 /*
  * Stats
  *
  * Keep track of per-flow and total bytes sent per
  * second. 
  *
  * You must follow the coding standards distributed
  * on the class web page.
  *
  * (C) 2007 Mike Dahlin
  *
  */
 
 import java.util.*;
 
 public class Stats{
   
   
   long startTimeMS;
   //HashMap<Integer,HashMap<Integer,Integer>> BytesPerSecondTable;
   LinkedList<HashMap<Integer,Integer>> BytesPerSecondTable;
   LinkedHashSet<Integer> flowIds;
   
   SimpleLock mutex;
     //
     // Add more per-object state.
     //
     //           REMEMBER
     // 
     // You *must* follow the coding standards
     // distributed on the class web page.
     // Solutions failing to conform to these
     // standards will receive little or
     // no credit.
     //
 
   //-------------------------------------------------
   // Constructor
   //-------------------------------------------------
   public Stats(){
     this.startTimeMS = System.currentTimeMillis();
     //this.BytesPerSecondTable = new HashMap<Integer,HashMap<Integer,Integer>>();
     this.flowIds = new LinkedHashSet<Integer>();
     this.BytesPerSecondTable = new LinkedList<HashMap<Integer,Integer>>();
     this.mutex = new SimpleLock();
 
   }
 
   //-------------------------------------------------
   // update -- update stats
   //-------------------------------------------------
   public void update(int flowId, int bytes)
   {
 	mutex.lock();
	System.out.println("Updating");
 	//float convert to int truncates correctly?
 	int currentSecond = (int)((System.currentTimeMillis()-this.startTimeMS)/1000);
 	HashMap<Integer,Integer> hm;
 	while(BytesPerSecondTable.size() <= currentSecond){
 		BytesPerSecondTable.add(new HashMap<Integer,Integer>());
 	}
 	hm = BytesPerSecondTable.getLast();
 
 	flowIds.add(flowId);
 	Integer current = hm.get(flowId);
 	if (current == null){
 		current =0;
 	}
 	current +=bytes;
 	hm.put(flowId, current);
 	
 	mutex.unlock();
     //
     // Fill in this code. 
     //
     // Keep track of how many bytes each flowId
     // sent for each second since the start
     // of the run. E.g., if the Stats is created
     // at time 832000 and update(23, 1024)
     // is called at time 833200 and update(942, 8192)
     // is called at time 841200, then
     // you would record that flow 23 sent 1024 bytes
     // during second 1 and that flow 942 sent
     // 8192 bytes during second 10.
     //
     // Do *not* assume that flowIDs are small
     // consecutive positive integers. E.g., a system
     // may have flowIDs between Integer.MIN_VALUE
     // to Integer.MAX_VALUE. This non-constraint
     // may affect your choice of data structure.
     //
     // Do synchronize access to your shared
     // state. 
     //
     //
     //           REMEMBER
     // 
     // You *must* follow the coding standards
     // distributed on the class web page.
     // Solutions failing to conform to these
     // standards will receive little or
     // no credit.
     //
   }
 
   //-------------------------------------------------
   // print -- print stats
   //-------------------------------------------------
   public void print() {
 	  mutex.lock();
      System.out.println("here");

 	  ListIterator<HashMap<Integer,Integer>> iter = BytesPerSecondTable.listIterator();
 	  int i = 0;
	  for(HashMap<Integer,Integer> hm = iter.next(); iter.hasNext(); i++, hm = iter.next()){
 		  StringBuilder sb = new StringBuilder();
 		  sb.append(i);
 		  int total = 0;
		  
 		  for(Integer fid : flowIds) {  //same order each time?
 			  Integer numBytes = hm.get(fid);
 			  if(numBytes == null){
 				  numBytes=0;
 			  }
 			  sb.append(" " + numBytes); 
 			  total += numBytes;
 		  }
 		  sb.append(" " + total);
 		  System.out.println(sb.toString());
 	  }
 	  
 	  mutex.unlock();
 	  
 	  
     //
     // Fill in this code. 
     //
     // Output 1 row per second of the run with
     // each row having the form:
     // [second] [sent_0] [sent_1] [sent_2] ... [tot]
     //
     // where [second] is the number of seconds
     // into the run (i.e., the first line
     // should begin with "0", the second with "1"
     // etc.)
     //
     // [sent_i] is the number of bytes whose
     // IO completed during the second under
     // consideration for the i'th flow.
     // Note that if there have been nFlows
     // distinct flows, then i is between 0
     // and nFlows-1 (e.g., i may not match
     // the flowID.) Note that if flowID x
     // appears as the i'th flow in a row
     // it must always appear in the same
     // position in subsequent rows.
     // (I.e., your mapping from flowID to
     // column must remain consistent.)
     //
     //           REMEMBER
     // 
     // You *must* follow the coding standards
     // distributed on the class web page.
     // Solutions failing to conform to these
     // standards will receive little or
     // no credit.
     //
     
   }
 
 
 
 }
