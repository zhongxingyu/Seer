 package org.openmrs.module.chartsearch;
 
 public class SearchPhrase {
 	String phrase;
 	
 	public SearchPhrase() {
 		phrase="";
 	}
 	
 	public SearchPhrase(String phrase) {
 		this.phrase=phrase;
 	}
 
 	public String getPhrase() {
        if(phrase==null) {
            return "";
        }
 		return phrase;
 	}
 
 	public void setPhrase(String phrase) {
 		this.phrase = phrase;
 	}
 	
 	
 	
 	
 }
