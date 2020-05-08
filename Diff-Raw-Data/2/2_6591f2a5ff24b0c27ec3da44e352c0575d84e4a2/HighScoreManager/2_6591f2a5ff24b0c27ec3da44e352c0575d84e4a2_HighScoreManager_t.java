 /*
  *  Wezzle
  *  Copyright (c) 2007-2008 Couchware Inc.  All rights reserved.
  */
 
 package ca.couchware.wezzle2d.manager;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * TODO Describe how class works.
  * 
  * @author Kevin, Cameron
  */
 public class HighScoreManager 
 {        
     
     /**
      * The number of high scores to keep track of.
      */
     public final static int NUMBER_OF_SCORES = 5;
     
     /**
      * The symbol indicating an empty name.
      */
     public final static String EMPTY_NAME = "-";             
     
     /**
      * The list of high scores.
      */
     private HighScore[] highScoreList;
     
     final private static List<Settings.Key> nameKeyList;
     final private static List<Settings.Key> scoreKeyList;
     final private static List<Settings.Key> levelKeyList;
     
     static
     {
         nameKeyList  = new ArrayList<Settings.Key>();
         scoreKeyList = new ArrayList<Settings.Key>();
         levelKeyList = new ArrayList<Settings.Key>();
         
         nameKeyList.add(Settings.Key.USER_HIGHSCORE_NAME_1);
         nameKeyList.add(Settings.Key.USER_HIGHSCORE_NAME_2);
         nameKeyList.add(Settings.Key.USER_HIGHSCORE_NAME_3);
         nameKeyList.add(Settings.Key.USER_HIGHSCORE_NAME_4);
         nameKeyList.add(Settings.Key.USER_HIGHSCORE_NAME_5);
         
         scoreKeyList.add(Settings.Key.USER_HIGHSCORE_SCORE_1);
         scoreKeyList.add(Settings.Key.USER_HIGHSCORE_SCORE_2);
         scoreKeyList.add(Settings.Key.USER_HIGHSCORE_SCORE_3);
         scoreKeyList.add(Settings.Key.USER_HIGHSCORE_SCORE_4);
         scoreKeyList.add(Settings.Key.USER_HIGHSCORE_SCORE_5);
         
         levelKeyList.add(Settings.Key.USER_HIGHSCORE_LEVEL_1);
         levelKeyList.add(Settings.Key.USER_HIGHSCORE_LEVEL_2);
         levelKeyList.add(Settings.Key.USER_HIGHSCORE_LEVEL_3);
         levelKeyList.add(Settings.Key.USER_HIGHSCORE_LEVEL_4);
         levelKeyList.add(Settings.Key.USER_HIGHSCORE_LEVEL_5);
     }
     
     /**
      * Create a high score manager and fill it with 0's.
      * 
      * @param properytMan
      */
     private HighScoreManager()
     {
         // Initialize 
         this.highScoreList = new HighScore[NUMBER_OF_SCORES];
                
         // If this is the first time running the game, we have no built list.
         // Every other time it will load the list from file.
         if (readSettings() == false)
             resetScoreList();
     }
     
     
     /**
      * Returns a new instance of the high score manager.
      * 
      * @param propertyMan
      * @return
      */
     public static HighScoreManager newInstance()
     {
         return new HighScoreManager();
     }
     
     /**
      * Get the highest score. Return position 0 in the list.
      * @return the high score.
      */
     public int getHighestScore()
     {
         return this.highScoreList[0].getScore();
     }
     
     /**
      * A method to check the lowest score in the list. Since the list is sorted
      * it returns the bottom score. This is used to see if a new score belongs
      * in the list.
      * 
      * @return the lowest score.
      */
     public int getLowestScore()
     {
         return this.highScoreList[highScoreList.length - 1].getScore();
     }       
     
     /**
      * Add a score to the list. If the list has 10 values, 
      * the bottom value will be removed to
      * accommodate this value. This is because if it belongs in the list, and
      * the list is sorted, the bottom value will be the lowest value.
      * 
      * The insert into the list sorts the list.
      * 
      * @param key The key associated with the score.
      * @param score The score associated with the key.
      */
     public void addScore(String name, int score, int level)
     {
         // See if the score belongs on the list.
         if (score < getLowestScore())
             return;
         
         HighScore newScore = HighScore.newInstance(name, score, level);       
         
         // Add the score.
         this.highScoreList[this.highScoreList.length - 1] = newScore;
         
         // Sort.
         this.bubbleUp();
         
         // Write to properties.
         writeProperties();
     }
     
     /**
      * A method to bubble the bottom value in the array to the proper spot.
      * Will only move the bottom value up to the correct spot.
      * 
      * Kevin is very proud of this method.
      */
     private void bubbleUp()
     {
         for (int i = this.highScoreList.length - 1; i > 0; i--)
         {
             // Swap.
             if (highScoreList[i].getScore() > highScoreList[i - 1].getScore())
             {
                 HighScore temp = highScoreList[i - 1];
                 highScoreList[i - 1] = highScoreList[i];
                 highScoreList[i] = temp;
             }
             else
             {
                 // If we have found the right spot. break.
                 break;
             }
         }
     }
         
     /**
      * Write the list to properties.
      */
     private void writeProperties()
     {
         SettingsManager settingsMan = SettingsManager.get();
         
         for (int i = 0; i < highScoreList.length; i++)
         {
             settingsMan.setString(nameKeyList.get(i), 
                     highScoreList[i].getName());
             
            settingsMan.setInt(scoreKeyList.get(i),    
                     highScoreList[i].getScore());
             
             settingsMan.setInt(levelKeyList.get(i),   
                     highScoreList[i].getLevel());                   
         }
     }
         
     /**
      * Read the list from properties.
      * 
      * @return Whether the list was read or not.
      */
     private boolean readSettings()
     {
         SettingsManager settingsMan = SettingsManager.get();
         
         for (int i = 0; i < highScoreList.length; i++)
         {
             String name = settingsMan.getString(nameKeyList.get(i));
             
             // If the properties aren't set, return false.
             if (name == null) return false;
             
             // Otherwise, add to the high score list.
             int score = settingsMan.getInt(scoreKeyList.get(i));
             int level = settingsMan.getInt(levelKeyList.get(i));
             
             highScoreList[i] = HighScore.newInstance(name, score, level);           
         }
         
         return true;
     }
     
     /**
      * Reset the list.
      */
     public void resetScoreList()
     {
         HighScore blankScore = HighScore.newInstance("-", -1, -1);
         
         // Load with dummy scores.
         for (int i = 0; i < highScoreList.length; i++)        
             highScoreList[i] = blankScore;        
         
         // Save the properties.
         writeProperties();
     }
     
     /**
      * Returns a copy of the high score list.
      */
     public HighScore[] getScoreList()
     {
         return highScoreList.clone();
     }         
     
 }
