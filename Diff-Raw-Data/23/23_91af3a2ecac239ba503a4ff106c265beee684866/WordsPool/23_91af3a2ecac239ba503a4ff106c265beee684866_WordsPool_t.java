 package com.alperovichsimon.gamemodel;
 
 import java.util.*;
 
 /**
  * Simon Alperovich
  * Date: 27.09.12
  * Time: 2:55
  */
 public class WordsPool {
     private static WordsPool INSTANCE;
 
     private ArrayList<Word> pool = new ArrayList<Word>();
 
     private int curIndex = 0;
     private int notGuessedNumber = 0;
 
     private int hardNumber;
     private int mediumNumber;
     private int easyNumber;
    private Word currentWord;
 
 
     private WordsPool() {
     }
 
     public void addWord(Word word) {
         pool.add(word);
         switch (word.getLevel()) {
             case HARD:
                 ++hardNumber;
                 break;
             case MEDIUM:
                 ++mediumNumber;
                 break;
             case EASY:
                 ++easyNumber;
         }
         notGuessedNumber++;
     }
 
 
     public int getHardNumber() {
         return hardNumber;
     }
 
     public int getMediumNumber() {
         return mediumNumber;
     }
 
     public int getEasyNumber() {
         return easyNumber;
     }
 
     public void wordGuessed() {
         if (notGuessedNumber == 0) {
             return;
         }
 
         Collections.swap(pool, curIndex, notGuessedNumber - 1);
         notGuessedNumber--;
        currentWord = cacheNextWord();
     }
 
     //null if all words have been guessed
     public Word getNextWord() {
        if (currentWord == null) {
            currentWord = cacheNextWord();
        }
        return currentWord;
    }


    public Word cacheNextWord() {
         if (notGuessedNumber == 0) {
             return null;
         }
         curIndex = new Random().nextInt(notGuessedNumber);
         return pool.get(curIndex);
     }
 
     public int getWordsNumber() {
         return pool.size();
     }
 
     public void deleteAll() {
         pool.clear();
 
         hardNumber = 0;
         mediumNumber = 0;
         easyNumber = 0;
 
         curIndex = 0;
         notGuessedNumber = 0;
     }
 
     public static synchronized WordsPool getInstance() {
         if (INSTANCE == null) {
             INSTANCE = new WordsPool();
         }
         return INSTANCE;
     }
 
 }
