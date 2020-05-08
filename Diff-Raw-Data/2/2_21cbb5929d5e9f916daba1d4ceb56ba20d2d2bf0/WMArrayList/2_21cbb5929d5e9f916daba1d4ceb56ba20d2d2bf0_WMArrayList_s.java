 package fr.iutvalence.java.mp.thelasttyper.client.data.wordsmanager;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import fr.iutvalence.java.mp.thelasttyper.client.data.Word;
 import fr.iutvalence.java.mp.thelasttyper.client.data.WordsManager;
 
 /**
  * This class generate a word's list from an array List. 
  * @author culasb
  *
  */
 public class WMArrayList implements WordsManager
 {
     /**
      * This is list of words.
      */
    List<Word> list;
     
     
     /**
      * Initialize the list at null.
      */
     public WMArrayList(){
         this.list = null;
     }
     
     /**
      * Initialized a word's list
      * @param list the words
      */
     public WMArrayList(ArrayList<Word> list){
         this.list=list;
     }
 
     @Override
     public List<Word> getWordsList(int amount, int difficulty)
     {
         //TODO use a random to prevent users to alway have the same list.
         List<Word> newList = new ArrayList<Word>();
         int i =0;
         while (i < amount){
             if (this.list.get(i).getContent().length() == difficulty)
                 newList.add(this.list.get(i));
             i++;
             
         }
         return null;
     }
 
 
 }
