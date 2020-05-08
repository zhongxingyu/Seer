 package com.morgajel.spoe.service;
 
 import java.io.Serializable;
 import java.util.List;
 
 import com.morgajel.spoe.domain.Critique;
 import com.morgajel.spoe.domain.Snippet;
 
 //Copied and tweaked from ProductManager in tutorial	
 	public interface SnippetManager extends Serializable{
 		
 	    public void addCritique(Critique critique);
 	    
 	    public List<Snippet> getSnippets();
 
	
	

 }
