 package fr.iutvalence.java.mp.thelasttyper.client.data;
 
 /**
  * This class contains all the informations about a word. The word have to be
  * destroyed in the game.
  * 
  * @author culasb
  */
 public class Word
 {
     /**
      * Word score value. When the this word is killed by a player, this value is
      * added to the player's score this value cannot be changed
      * 
      */
     private final int value;
 
     /**
      * Word's level
      * 
      * this value cannot be changed
      */
     private final int difficulty;
 
     /**
      * Word's content (string) this value cannot be changed
      */
     private final String content;
 
     /**
      * Instantiation of new word with a given value, a given level, and a given
      * content.
      * 
      * @param value
      *            Default score Value for this word
      * @param difficulty
      *            Word level
      * @param content
      *            the string
      */
     public Word(int value, int difficulty, String content)
     {
         this.value = value;
         this.difficulty = difficulty;
         this.content = content;
     }
     
     
     /**
      * static method to get the difficulty of a given string
      * @param s the string which need to be tested
      * @return the difficulty
      */
     public static int getDifficulty(String s)
     {
         int i = s.length();
         
        if (i ==0) return 0;
         
         if (i < 4)
             return 1;
         else if (i< 5)
             return 2;
         else if (i< 6)
             return 3;
         else if (i <8)
             return 4;
         //else
         return 5;
         
     }
     
     /**
      * this static method return the value of a string.
      * @param s the given which need to be tested
      * @return its value.
      */
     public static int getValue (String s){
         return (Word.getDifficulty(s) * 100);
     }
 
     /**
      * get the score value of the Word
      * 
      * @return score value
      */
     public int getValue()
     {
         return this.value;
     }
 
     /**
      * get the level of the Word
      * 
      * @return word's level
      */
     public int getDifficulty()
     {
         return this.difficulty;
     }
 
     /**
      * get the content of the Word
      * 
      * @return word's content.
      */
     public String getContent()
     {
         return this.content;
     }
 
 }
