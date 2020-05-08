 package ds.lab.messagepasser;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import ds.lab.bean.NodeBean;
 import ds.lab.bean.VectorTimeStamp;
 import ds.lab.message.TimeStampMessage;
 
 public class VectorClock extends ClockService {
 	private int n;// number of nodes in the system
 	private VectorTimeStamp vts;
 
 	public VectorClock(int n, HashMap<String, NodeBean> nodelist) {
 		// TODO constructor, init d value, vector
 		this.n = n;
 		vts = new VectorTimeStamp();
 		Set<String> key = nodelist.keySet();
 		Iterator<String> i = key.iterator();
 		// initiating timestamps of every member in the vector clock;
 		while (i.hasNext()) {
 			String nodeName = i.next().toString();
 			vts.getVector().put(nodeName, new AtomicInteger(-1));// start each
 																	// timestamps
 																	// with -1
 			vts.getVector().get(nodeName).incrementAndGet();// set each
 															// timestamp to 0
 		}
//		HashMap<String, AtomicInteger> key2 = vts.getVector();
 //		System.out.println(key2);
 	}
 
 	@Override
 	VectorTimeStamp getCurrentTimeStamp(String localName) {
 		return vts;
 	}
 
 	@Override
 	VectorTimeStamp getNewTimeStamp(String localName) {
 		vts.getVector().get(localName).incrementAndGet();
//		HashMap<String, AtomicInteger> key2 = vts.getVector();
 //		System.out.println(key2);
 		return vts;
 	}
 
 	VectorTimeStamp updateTimeStampOnReceive(String localName, TimeStampMessage senderTsm) {
 		HashMap<String, AtomicInteger> myVector = vts.getVector();
 		for (Map.Entry<String, AtomicInteger> nodes : ((VectorTimeStamp) senderTsm.getTimeStamp()).getVector()
 				.entrySet()) {
 			String name = nodes.getKey();
 			int value = nodes.getValue().get();
 			if (name.equalsIgnoreCase(localName)) {
 				myVector.get(localName).incrementAndGet();// incr self-timestamp
 															// by 1
 			} else {
 				int n = Math.max(value, myVector.get(name).get());
 				// set timestamps of the resp nodes with latest time
 				// values/atomicint values
 				this.vts.getVector().get(name).set(n);// watch out atomic...
 			}
 		}
 		vts.setVector(myVector);
 		HashMap<String, AtomicInteger> key2 = vts.getVector();
 //		System.out.println(key2);
 		return vts;
 	}
 }
