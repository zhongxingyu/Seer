 package com.sabayrean.hangman;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class TextResult extends Text {
 
 	public TextResult(String text){
 		this.setText(text);
 	}
 	
 	public List<Integer> getIndexes(char c){
 		List<Integer> indexes = new ArrayList<Integer>();
 		for(int i = 0; i < this.getText().length(); i++){
			if(this.getText().charAt(i) == c) indexes.add(i);
 		}
 		return indexes;
 	}
 }
