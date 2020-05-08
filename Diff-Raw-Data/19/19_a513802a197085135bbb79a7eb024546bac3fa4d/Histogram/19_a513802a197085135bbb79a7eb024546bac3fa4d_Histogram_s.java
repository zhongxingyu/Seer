 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 
 public class Histogram {
 	protected int[] features;
 	
 	public Histogram(int[] histo) {
 		List<Integer> features = new LinkedList<Integer>();
 		for(int f : histo) {
 			if(f>0) {
 				features.add(f);
 			}
 		}
 		
 		int[] h = new int[features.size()];
 		for(int i =0;i<h.length;i++) {
 			h[i] = features.get(i);
 		}
 		this.features = h;
 		Arrays.sort(this.features);
 	}
 	
 	public Histogram(List<Integer> features) {
 		int[] h = new int[features.size()];
 		for(int i =0;i<h.length;i++) {
 			h[i] = features.get(i);
 		}
 		this.features = h;
 		Arrays.sort(this.features);
 	}
 	
 	public boolean equals(Object o) {
 		if(o instanceof Histogram) {
			return (((Histogram)o).features.equals(this.features));
 		}
 		return false;
 	}
 	
 	public boolean contains(int f) {
 		for(int g : this.features) {
 			if(g==f) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean containsAll(Histogram h) {
 		for(int f : h.features) {
 			boolean hasFeature = false;
 			for(int g : this.features) {
 				if(g==f) {
 					hasFeature = true;
 					break;
 				}
 			}
 			if(!hasFeature) {
 				return false;
 			}
 		}
 		return true;
 	}
 }
