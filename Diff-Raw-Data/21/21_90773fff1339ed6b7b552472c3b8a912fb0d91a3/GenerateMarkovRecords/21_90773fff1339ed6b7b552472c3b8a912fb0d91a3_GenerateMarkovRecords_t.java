 package com.zw.test;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 import java.util.Set;
 
 import com.zw.markov.MarkovInfo;
 import com.zw.markov.MarkovRecord;
 import com.zw.markov.MarkovState;
 
 public class GenerateMarkovRecords {
 	static Queue<MarkovState> queue = new LinkedList<MarkovState>();
 	static Set<MarkovState> stateSet = new HashSet<MarkovState>();
 	static List<MarkovRecord> records = null;
 	static int count;
 	static FileWriter writer;
 	
 	private final static String LOG_FILE_NAME = "E:\\markov_log.txt";
 	
 	public static void main(String[] args) throws IOException {
 		writer = new FileWriter(LOG_FILE_NAME);
 		MarkovState state = new MarkovState();
 		//state.printFlow();
 		state.setCurrGlobalState(MarkovInfo.S_UNKNOWN);
 		state.getActivity(0).setX(-1);
 		state.init();
 		stateSet.add(state);
 		System.out.println(" StateBefore \t Action \t StateAfter \t Posibility \t Time \t Cost");
 		count = 0;
 		queue.offer(state);
 		while (!queue.isEmpty()) {
 			state = queue.poll();
			//System.out.println("\nSTATE:" + state);
 			records = MarkovInfo.terminate(state);
 			printRecord();
 			records = MarkovInfo.noAction(state);
 			printRecord();
 			records = MarkovInfo.redo(state);
 			printRecord();
 			records = MarkovInfo.replace(state);  //Without replace, count is 39
 			printRecord();
			//System.out.println("-------------------------");
 		} 
 		writer.close();
 	}
 
 	public static void printRecord() throws IOException {
 		if (records != null) {
 			for (MarkovRecord rd : records) {
 				System.out.println(String.format("%4s", (++count)) + " " + rd.toString());
 				writer.append(String.format("%4s", (count)) + " " + rd.toString() + "\n");
 				if (!stateSet.contains(rd.getStateAfter())) {
 					queue.offer(rd.getStateAfter());
 					stateSet.add(rd.getStateAfter());
 				}
 			}
 		}
 	}
 }
