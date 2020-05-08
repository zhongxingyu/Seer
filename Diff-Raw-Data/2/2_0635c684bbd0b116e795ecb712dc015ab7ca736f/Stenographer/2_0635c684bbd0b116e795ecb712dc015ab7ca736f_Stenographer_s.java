 import java.util.*;
 import java.io.*;
 
 public class Stenographer {
 
     Map<Integer, PacketEntry> data;
 
     public Stenographer(Map<String, Double> parameters) {
 	data = new HashMap<Integer, PacketEntry>();
     }
 
     /**
        Map:
        Key:int Packet ID (time var)
        Value: PacketEntry
     **/
 
     public void record(int packetId, String key, int value) {
 	if (data.containsKey(packetId)) {
 	    data.get(packetId).setQueueLen(value);
 	} else {
 	    PacketEntry pe = new PacketEntry(packetId);
 	    pe.setQueueLen(value);
 	    data.put(packetId, pe);
 	}
     }
 
     public void record(int packetId, String key, double value) {
 	if (data.containsKey(packetId)) {
 	    data.get(packetId).setAverageDelay(value);
 	} else {
 	    PacketEntry pe = new PacketEntry(packetId);
 	    pe.setAverageDelay(value);
 	    data.put(packetId, pe);
 	}
     }
 
     public void record(int packetId, String key, long value) {
 	if (data.containsKey(packetId)) {
 	    if (key.equals("Delay")) {
 		data.get(packetId).setDelay(value);
 	    } else {
 		data.get(packetId).setRoundTripTime(value);
 	    }
 	} else {
 	    PacketEntry pe = new PacketEntry(packetId);
 	    if (key.equals("delay")) {
 		pe.setDelay(value);
 	    } else {
 		pe.setRoundTripTime(value);
 	    }
 	    data.put(packetId, pe);
 	}
     }
 
     public void systemOutput() {
 	for (int i = 0; i < data.size(); i++) {
 	    PacketEntry pe = data.get(i);
 	    System.out.println("Packet ID: "+i);
 	    System.out.println("Delay: "+pe.getDelay());
 	    System.out.println("QueueLen: "+pe.getQueueLen());
 	    System.out.println("RTT: "+pe.getRoundTripTime());
 	    System.out.println("AverageDelay: "+pe.getAverageDelay());
 	}
     }
 
     public void timeVsAvgDelay(String filename) throws IOException {
 	BufferedWriter out = writeFile(filename);
 	out.write("packetId, avgDelay\n");
	int average = 100000;
 	for (int i = 0; i < data.size(); i++) {
 	    PacketEntry pe = data.get(i);
 	    out.write(i +", ");
 	    if (pe == null) {
 		//do nothing
 	    } else if (pe.getAverageDelay() != 0) {
 		average = (int) pe.getAverageDelay();
 	    }
 	    out.write(""+average+"\n");
 	}
 	out.close();
     }
 
     public void timeVsQueueLen(String filename) throws IOException {
 	BufferedWriter out = writeFile(filename);
 	long time = 0;
 	out.write("time, queueLength\n");
 	for (int i = 0; i < data.size(); i++) {
 	    PacketEntry pe = data.get(i);
 	    time += pe.getDelay();
 	    out.write(time +", ");
 	    out.write(""+pe.getQueueLen()+"\n");
 	}
 	out.close();    
     }
 
     BufferedWriter writeFile(String filename) throws IOException {
 	BufferedWriter out = new BufferedWriter(new FileWriter(filename));
 	return out;
     }
 
 }
 
 class PacketEntry {
     private int packetId;
     private long delay;
     private int queueLen;
     private long roundTripTime;
     private double averageDelay;
   
     public PacketEntry(int packetId) {
 	this.packetId = packetId;
     }
 
     public void setDelay(long delay) {
 	this.delay = delay;
     }
 
     public void setQueueLen(int len) {
 	this.queueLen = len;
     }
 
     public void setRoundTripTime(long rtt) {
 	this.roundTripTime = rtt;
     }
 
     public void setAverageDelay(double avg) {
 	this.averageDelay = avg;
     }
 
     public long getDelay() {
 	return delay;
     }
 
     public int getQueueLen() {
 	return queueLen;
     }
 
     public long getRoundTripTime() {
 	return roundTripTime;
     }
 
     public double getAverageDelay() {
 	return averageDelay;
     }
 }
