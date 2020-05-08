 package com.sabayrean.hangman;
 
 public abstract class Text {
 	
	private String text = "";
 	
 	protected void setText(String text){
 		this.text = text;
 	}
 	
 	protected String getText(){
 		return this.text;
 	}
 
 	public boolean isExists(char c){
 		for(char ch : this.getText().toCharArray()){
 			if(ch == c) return true;
 		}
 		return false;
 	}
 
 }
