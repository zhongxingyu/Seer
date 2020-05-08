 package com.media_moderator.article_data;
 
 public class Keyword {
 	protected String word;
 	protected String relevance;
 	protected String sentiment;
 	
     protected Keyword(String word, String relevence, String sentiment) {
         this.word = word;
         this.relevance = relevence;
         this.sentiment = sentiment;
     }
     
    public String getWord(){
    	return word;
    }
    public String getRelevance(){
    	return word;
    }
    public String getSentiment(){
    	return word;
    }
    
 }
