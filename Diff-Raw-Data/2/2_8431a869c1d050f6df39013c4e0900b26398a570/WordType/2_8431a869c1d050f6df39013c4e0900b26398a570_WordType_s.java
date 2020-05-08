 /**
 * 
  */
 package edu.rit.se.agile.data;
 
 /**
  * @author Ian
  *
  */
 public enum WordType {
 
 	ADJECTIVE("#ADJ"), 
 	ADVERB("#ADV"), 
 	NOUN("#NOUN"), 
 	VERB("#VERB");
 	
 	private String placeholder;
 	
 	WordType(String placeholder) {
 		this.placeholder = placeholder;
 	}
 	
 	String val() {
 		return placeholder;
 	}
 }
