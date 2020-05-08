 package com.WordFinder;
 
 import java.io.IOException;
 import android.content.Context;
 import java.util.ArrayList;
 
 /**
  * // -------------------------------------------------------------------------
  * /** Solves LetterGrids, checks to see if a found word is valid.
  *
  * @author John Mooring (jmooring)
  * @version 2011.11.8
  */
 public class WordSolver
 {
     private static WordSolver instance;
     private ArrayList<String> foundWords;
     private Trie              dictionary;
     private final int         MAX_DEPTH = 10;
 
 
     /**
      * Singleton constructor
      */
     private WordSolver()
     {
         dictionary = new Trie();
         foundWords = new ArrayList<String>();
     }
 
 
     /**
      * Clears all found words from the list
      */
     private void clearWords()
     {
         foundWords.clear();
     }
 
 
     /**
      * Loads the dictionary into a prefix tree
      */
     public void loadDictionary()
     {
         try
         {
             byte[] b = null;
             WordFinderActivity.getInstance().getAssets().open("en.dict").read(b);
             for(String word : new String(b).split("\n")) {
                 addWord(word);
             }
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
 
     }
 
 
     /**
      * Adds a single word to the dictionary.
      *
      * @param word
      *            the word to add to the dictionary
      */
     public void addWord(String word)
     {
         dictionary.add(word);
     }
 
 
     /**
      * Solves the passed grid
      */
     public ArrayList<String> solve(LetterGrid grid)
     {
         clearWords();
         for (int i = 0; i < grid.size(); i++)
         {
             for (int j = 0; j < grid.size(); j++)
             {
                 solve(
                     grid.getTile(i, j),
                     "",
                     new boolean[grid.size()][grid.size()],
                     MAX_DEPTH);
             }
         }
         return foundWords;
     }
 
 
     /**
      * Uses recursion to solve the letter grid
      */
     private void solve(Tile tile, String current, boolean[][] visited, int depth)
     {
         String word = current + tile.getLetter();
         if (dictionary.hasNext(tile.getLetter()))
         {
             dictionary.gotoNext(tile.getLetter());
         }
         else
         {
             return;
         }
         visited[tile.getX()][tile.getY()] = true;
         if (dictionary.isWord())
         {
             foundWords.add(word);
         }
         if (depth <= 0)
         {
             return;
         }
         for (Tile t : tile.getAdjascent())
         {
             if (!visited[t.getX()][t.getY()])
             {
                 solve(t, word, visited, depth - 1);
             }
         }
         visited[tile.getX()][tile.getY()] = false;
         dictionary.pop();
     }
 
 
     /**
      * Checks to see if the dictionary contains the passed word
      *
      * @param word
      *            the word to check
      * @return whether or not the word is a word
      */
     public boolean isWord(String word)
     {
         return foundWords.contains(word);
     }
 
 
     /**
     * Returns an arraylist of all the possible words on the grid
      */
     public ArrayList<String> getWords()
     {
         return foundWords;
     }
 
 
     /**
      * Returns an instance of this class
      */
     public static WordSolver getInstance()
     {
         if (instance == null)
         {
             instance = new WordSolver();
         }
         return instance;
     }
 }
