 import java.util.Arrays;
 import java.util.Collections;
 
 public class HopfieldNetwork {
 	
 	private int n;
 	
 	private int w[][];
 	
 	public HopfieldNetwork(int neuronsCount) {
 		n = neuronsCount;
 		w = new int[n][n];
 	}
 	
 	void train(Image image) {
 		image = image.toBipolar();
 		for(int i=0;i<n;i++)
 			for(int j=0;j<n;j++)
 				if(i != j)
 					w[i][j] += image.get(i)*image.get(j);
 				else
 					w[i][j] = 0;
 	}
 	
 	Image recognise(Image image) {		
 		Image ans = new Image(n);
 		int[] order = new int[n];
 		for(int i=0;i<n;i++) {
 			ans.set(i, image.get(i));
 			order[i] = i;
 		}
 		ans = ans.toBipolar();
 		
 		int rounds = 1000, cur = 0;
 		while(cur < rounds)	{
 			Collections.shuffle(Arrays.asList(order));
 			for(int j=0;j<n;j++) {
 				int v = order[j];
 				ans.set(v, 0);
 				for(int i=0;i<n;i++)
					ans.set(v, w[i][v]*ans.get(i)+ans.get(v));
 				
 				if(ans.get(v) > 0) 
 					ans.setUpperState(v);
 				else 
 					ans.setLowerState(v);
 			}
 			cur++;
 		}
 		return ans.toUnipolar();
 	}
 }
