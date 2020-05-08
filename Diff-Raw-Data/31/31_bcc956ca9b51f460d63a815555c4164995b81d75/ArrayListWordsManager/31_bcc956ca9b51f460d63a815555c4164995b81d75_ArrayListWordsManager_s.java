 package fr.iutvalence.java.mp.thelasttyper.client.data.wordsmanager;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import fr.iutvalence.java.mp.thelasttyper.client.data.Constants;
 import fr.iutvalence.java.mp.thelasttyper.client.data.Word;
 import fr.iutvalence.java.mp.thelasttyper.client.data.WordsManager;
 
 /**
  * This class generate a word's list from an array List.
  * 
  * @author culasb
  * 
  */
 public class ArrayListWordsManager implements WordsManager
 {
     /**
      * This is list of words.
      */
     private List<Word> words;
 
     /**
      * Initialize the list at null.
      * @param amountPerDifficulty the amount of Words for each difficulty
      */
     public ArrayListWordsManager(int amountPerDifficulty)
     {
         int count =0;
         int difficulty = 1;
         this.words = new ArrayList<Word>();
         while (difficulty <= 5)
         {
             while (count < amountPerDifficulty)
                 {
                 this.words.add(createRandomWord(difficulty));
                 count ++;
                 }
             difficulty ++;
         }
     }
 
     /**
      * Initialized a word's list
      * 
      * @param list
      *            the words
      */
     public ArrayListWordsManager(ArrayList<Word> list)
     {
         this.words = list;
     }
 
     @Override
     public List<Word> getWordsList(int amount, int difficulty)
     {
        // TODO use a random to prevent users to alway have the same list.
        List<Word> newList = new ArrayList<Word>();
        int i = 0;
        while (i < amount)
         {
            if (this.words.get(i).getContent().length() == difficulty)
                newList.add(this.words.get(i));
            i++;
 
         }
        return null;
     }
     
     /**
      * @param difficulty the difficulty for a word
      * @return a word generate randomly with a length based on the difficulty
      */
     private Word createRandomWord(int difficulty)
     {
         int i = 0;
         String string = "";
         int count = 0;
         Random random = new Random();
         switch(difficulty)
         {
         case(1) : // Words with exactly 3 characters 
         while(count < Word.MAX_LENGTH_1)
         {
             char c = (char)(random.nextInt(26) + 'a');
             count ++;
             string = string + c;
         }
         break;
         case(2) :
             while(count < Word.MAX_LENGTH_2)
             {
                 char c = (char)(random.nextInt(26) + 'a');
                 count ++;
                 string = string + c;
             }
             break;
         case(3) :
             while(count < Word.MAX_LENGTH_3)
             {
                 char c = (char)(random.nextInt(26) + 'a');
                 count ++;
                 string = string + c;
             }
             break;
         case(4) :
             i = random.nextInt(Word.MAX_LENGTH_4 - Word.MAX_LENGTH_3);
             while(count < Word.MAX_LENGTH_3 + i)
             {
                 char c = (char)(random.nextInt(26) + 'a');
                 count ++;
                 string = string + c;
             }
             break;
         case(5) :
             i = random.nextInt(Constants.MAX_WORD_LENGTH - Word.MAX_LENGTH_4);
             while(count < Word.MAX_LENGTH_4 + i)
             {
                 char c = (char)(random.nextInt(26) + 'a');
                 count ++;
                 string = string + c;
             }
             break;
         default:
             //TODO throw exception
             
             break;
         }
         return new Word(Word.getValue(string),Word.getDifficulty(string),string);
         
     }
 
 }
