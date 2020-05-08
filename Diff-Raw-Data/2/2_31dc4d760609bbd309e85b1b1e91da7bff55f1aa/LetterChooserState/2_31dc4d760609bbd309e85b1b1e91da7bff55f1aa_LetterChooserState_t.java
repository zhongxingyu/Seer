 package com.fun.midworx;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created: 7/6/13 12:46 PM
  */
 public class LetterChooserState {
 
 	private List<String> lettersPool;
 	private List<Integer> chosenLetterIndexes;
 
 
 	public void setLettersPool(List<String> lettersPool){
 		this.lettersPool = new ArrayList<String>(lettersPool);
 		this.chosenLetterIndexes = new ArrayList<Integer>();
 		this.resetChosen();
 	}
 
 	public String getChosenWord(){
 		String res = "";
 		for (Integer i : chosenLetterIndexes) { res += lettersPool.get(i); }
 		this.resetChosen();
 		return res;
 
 	}
 
 	public void chooseLetter(int index){
 		chosenLetterIndexes.add(index);
 		poolChanged();
 	}
 
 	private void poolChanged(){
 		for (IPoolChangeCallback poolChangeListener : this.poolChangeListeners) {
 			poolChangeListener.poolChanged(chosenLetterIndexes);
 		}
 	}
 
 	public String getLetterForIndex(int index){
 		return lettersPool.get(index);
 	}
 
 	private void resetChosen(){
 		for (IPoolChangeCallback poolChangeListener : this.poolChangeListeners) {
 			poolChangeListener.poolChanged(chosenLetterIndexes);
 		}
		chosenLetterIndexes = new ArrayList<Integer>();
		poolChanged();
 	}
 
 	private List<IPoolChangeCallback> poolChangeListeners = new ArrayList<IPoolChangeCallback>();
 
 	public void onPoolChange(IPoolChangeCallback callback){
 		this.poolChangeListeners.add(callback);
 	}
 
 }
