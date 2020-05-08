 /*
  * The PairConfidence class is the key value stored in our final result map, 
  * in which the value is the list of the bug locations.
  * 
  * There are 4 main parameters in this class:
  * function: the function. e.g A
  * pair: function pairs. e.g (A, B)
  * support
  * confidence
  * 
  */
 
 package core;
 
 public class PairConfidence implements Comparable<PairConfidence>{
 	
 	private String function;
 	private String pair;
 	private int support;
 	private double confidence;
 	
 	public PairConfidence(String function, String pair, int support, double confidence) {
 		this.function = function;
 		this.pair = pair;
 		this.support = support;
 		this.confidence = confidence;
 	}
 	
 	public String getFunction() {
 		return function;
 	}
 	
 	public String getPair() {
 		return pair;
 	}
 	
 	public int getSupport() {
 		return support;
 	}
 	
 	public double getConf() {
 		return confidence;
 	}
 	
 	public int compareTo(PairConfidence pc) {
		if (this.function.compareTo(pc.getFunction()) == 0) {
 			String p1 = pair;
 			String p2 = pc.getPair();
 			return p1.compareTo(p2);
 		}
 		return this.function.compareTo(pc.getFunction());
 	}
 	
 	public String toString() {
 		return " pair: " + pair + "support: " + support + " confidence: " + String.format("%.2f", confidence*100.0) + "%";
 	}
 }
