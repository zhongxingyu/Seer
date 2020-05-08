 package us.yuxin.demo.hadoop.zero.userlogin;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.hadoop.io.Writable;
 
 public class ULDistribution implements Writable {
 	private HashMap<Integer, Integer> counter = new HashMap<Integer, Integer>();
 	
 	void add(int month) {
 		Integer c = counter.get(month);
 		if (c == null) {
 			counter.put(month, 1);
 		} else {
 			counter.put(month, c + 1);
 		}
 	}
 	
 	void add(ULDistribution uld) {
 		for (Map.Entry<Integer, Integer> entry : uld.counter.entrySet()) {
 			Integer c = counter.get(entry.getKey());
 			if (c == null) {
 				counter.put(entry.getKey(), entry.getValue());
 			} else {
 				counter.put(entry.getKey(), entry.getValue() + c);
 			}
 		}
 	}
 	
 	int firstLogin() {
 		int firstMonth = 999999;
 	
 		for (Integer m: counter.keySet()) {
 			if (m < firstMonth) {
 				firstMonth = m;
 			}
 		}
 		return firstMonth;
 	}
 
 	@Override
 	public void readFields(DataInput ins) throws IOException {
 		int size = ins.readInt();
 		for (int i = 0; i < size; i++) {
 			int k = ins.readInt();
 			int v = ins.readInt();
 			counter.put(k, v);
 		}
 	}
 
 	@Override
 	public void write(DataOutput outs) throws IOException {
 		outs.writeInt(counter.size());
 		for (Map.Entry<Integer, Integer> entry: counter.entrySet()) {
 			outs.writeInt(entry.getKey());
 			outs.writeInt(entry.getValue());
 		}
 	}
 
 	@Override
 	public String toString() {
 		List<Integer> keys = new ArrayList<Integer>(counter.keySet());
 		Collections.sort(keys);
 		
 		StringBuilder sb = new StringBuilder();
 		boolean first = true;
 		
 		for (Integer key : keys) {
 			if (first) {
 				first = false;
 			} else {
 				sb.append(",");
 			}
 			sb.append(key).append(":").append(counter.get(key));
 		}
 		return sb.toString();
 	}
 }
