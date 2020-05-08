 package com.fun.midworx;
 
 import java.util.*;
 
 /**
  * Created: 7/6/13 12:46 PM
  */
 public class LetterChooserState {
 
 	private List<String> lettersPool;
 
 	private ChosenLetterToIndex chosenLetterToIndex = new ChosenLetterToIndex();
 
 	public void setLettersPool(List<String> lettersPool){
 		this.lettersPool = new ArrayList<String>(lettersPool);
 		this.resetState();
 	}
 
 	public String getChosenWord(){
		return this.chosenLetterToIndex.getWord();
 	}
 
 	public String getChosenWordAndReset(){
 		String word = getChosenWord();
 		resetState();
 		return word;
 	}
 
 	public void chooseLetter(int index){
 		this.chosenLetterToIndex.addChosen(this.lettersPool.get(index),index);
 		poolChanged();
 	}
 
 	public void unChooseLetter(int index) {
 		this.chosenLetterToIndex.removeChosen(index);
 		poolChanged();
 	}
 
 	private void poolChanged(){
 		for (IChooserStateChange poolChangeListener : this.poolChangeListeners) {
 			poolChangeListener.chooserStateChane(lettersPool, this.chosenLetterToIndex.getSortedSelectedIndexes() );
 		}
 	}
 
 	private void resetState(){
 		this.chosenLetterToIndex.resetPoolMapping();
 		this.poolChanged();
 	}
 
 	private List<IChooserStateChange> poolChangeListeners = new ArrayList<IChooserStateChange>();
 
 	public void onStateChange(IChooserStateChange callback){
 		this.poolChangeListeners.add(callback);
 	}
 
 	private class ChosenLetterToIndex {
 		private Set<String> lettersPoolMapping = new LinkedHashSet<String>();
 
 		public void addChosen(String letter, int poolIndex){
 			String key = letter + "," + poolIndex;
 			if (!lettersPoolMapping.contains(key)){
 				lettersPoolMapping.add(key);
 			}
 		}
 
 		public void removeChosen(int insertIndex){
 			if (insertIndex < this.lettersPoolMapping.size()){
 				this.lettersPoolMapping.remove((String)this.lettersPoolMapping.toArray()[insertIndex]);
 			}
 		}
 
 		public void resetPoolMapping(){
 			this.lettersPoolMapping = new LinkedHashSet<String>();
 		}
 
 		public List<Integer> getSortedSelectedIndexes(){
 			List<Integer> res = new ArrayList<Integer>();
 			for (String s : this.lettersPoolMapping) {
 				res.add(Integer.valueOf(s.split(",")[1]));
 			}
 			return res;
 		}
 
 		public String getWord() {
 			String out = "";
 			for (String s : this.lettersPoolMapping) {
 				out += s.split(",")[0];
 			}
 			return out;
 		}
 	}
 
 
 }
