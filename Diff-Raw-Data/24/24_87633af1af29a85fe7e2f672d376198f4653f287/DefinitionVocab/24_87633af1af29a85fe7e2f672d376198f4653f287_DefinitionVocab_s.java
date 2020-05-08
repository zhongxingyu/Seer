   
   /*
    * Preppa, Inc.
    * 
    * Copyright 2009. All rights
   reserved.
    * 
    * $Id$
    */
 
 package com.preppa.web.pages.contribution.vocab;
 
 import com.preppa.web.data.DictionaryWordDAO;
 import com.preppa.web.data.VocabDAO;
 import com.preppa.web.entities.DictionaryWord;
 import java.util.List;
 import org.apache.tapestry5.PersistenceConstants;
 import org.apache.tapestry5.annotations.InjectPage;
 import org.apache.tapestry5.annotations.Persist;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.annotations.Property;
 
 /**
  *
  * @author Jan Jan
  */
 public class DefinitionVocab {
 
     @Inject
     private DictionaryWordDAO dictionarywordDAO;
 
     @Inject
     private VocabDAO vocabDAO;
 
     @Property
     private DictionaryWord definition;
 
     @Persist(PersistenceConstants.FLASH)
     private String input;
 
 
 	public void set(String word) {
         input = word;
 	}
     
     public String getInput() {
         return input;
     }
 
     public boolean getIsIn() {
         if (vocabDAO.findByName(input).isEmpty() == true) {
             return false;
         }
         else {
             return true;
         }
     }
 
     public boolean getIsEmpty() {
         return dictionarywordDAO.findByName(input).isEmpty();
     }
 
     public List<DictionaryWord> getAllWords() {
         List<DictionaryWord> returnVal = dictionarywordDAO.findByName(input);
 
         for (int i = 0; i < returnVal.size(); i++)
         {
            if (returnVal.get(i).getSubmitted() == true)
             {
                 returnVal.remove(i);
            }
         }
 
         return returnVal;
     }
 
 }
