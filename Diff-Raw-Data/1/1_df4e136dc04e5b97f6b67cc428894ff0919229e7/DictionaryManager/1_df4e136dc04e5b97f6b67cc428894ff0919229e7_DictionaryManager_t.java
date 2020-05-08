 package com.mromer.ghost.service;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.mromer.ghost.model.Dictionary;
 
 
 public class DictionaryManager {
 	
 	// Added feature 001
	// Release solve some bugs
 	
 	private final static Logger logger = (Logger) LoggerFactory.getLogger(DictionaryManager.class);
 
 	private Dictionary dictionary;	
 
 	public DictionaryManager() {		
 		
 		dictionary = new Dictionary();
 
 		if (dictionary.size() == 0){
 			throw new RuntimeException("Unable to load dictionary.");
 		} else {
 			logger.debug("allWords.size() " + dictionary.size());
 		}
 	}
 
 	public Dictionary getDictionary() {
 		return dictionary;
 	}
 
 	public void setDictionary(Dictionary dictionary) {
 		this.dictionary = dictionary;
 	}
 	
 	
 	
 
 }
