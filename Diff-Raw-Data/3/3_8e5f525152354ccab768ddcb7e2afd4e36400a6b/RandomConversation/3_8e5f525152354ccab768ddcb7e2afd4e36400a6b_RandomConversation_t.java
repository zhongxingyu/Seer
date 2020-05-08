 package com.ncgeek.games.shattered.dialog;
 
 import java.util.Arrays;
 
 import com.ncgeek.games.shattered.utils.Rand;
 
 public class RandomConversation extends Conversation {
 	
 	private int [] weights;
 	
 	public RandomConversation() {}
 	
 	public void setWeights(int[] weights) { 
 //		ArrayList<Integer> lst = new ArrayList<Integer>(weights.length);
 //		lst.add(weights[0]);
 //		for(int i=1; i<weights.length; ++i) {
 //			int w = lst.get(i-1) + weights[i];
 //			if(w <= 100) {
 //				lst.add(w);
 //			} else {
 //				lst.add(100);
 //				break;
 //			}
 //		}
 //			lst.set(lst.size()-1, 100);
 //		this.weights = new int[lst.size()];
 //		for(int i=0; i<weights.length; ++i) 
 //			this.weights[i] = lst.get(i);
 		this.weights = Arrays.copyOf(weights, weights.length);
 	}
 	public int[] getWeights() { return weights; }
 	
 	@Override
 	public void begin() {
 		int n = Rand.next(100);
 		int idx = Arrays.binarySearch(weights, n);
		if(idx < 0) {
			idx = Math.max(0, Math.min(weights.length-1, -idx - 1));
		}
 		Dialog.getInstance().setText(getLine(idx));
 	}
 	
 	@Override
 	public void dialogFinished(int id) {}
 }
