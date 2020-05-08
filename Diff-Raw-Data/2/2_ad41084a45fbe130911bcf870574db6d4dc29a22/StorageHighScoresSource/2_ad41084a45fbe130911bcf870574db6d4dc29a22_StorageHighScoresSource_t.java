 package com.dfgames.lastplanet.highscores;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Preferences;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.utils.Array;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 /**
  * Author: Ivan Melnikov
  * Date: 07.11.12 20:48
  */
 public class StorageHighScoresSource implements HighScoresSource {
     private Array<HighScore> highScores;
     private Preferences prefs;
 
     public StorageHighScoresSource() {
         highScores = new Array<HighScore>();
         prefs = Gdx.app.getPreferences("last_planet");
 
         for (int i = 0; i < 9; i++) {
             String name = prefs.getString("name" + i);
             long score = prefs.getLong("score" + i);
             if (!name.equals("")) {
                 highScores.add(new HighScore(name, score));
             }
         }
 
         if (highScores.size == 0) {
             for (int i = 0; i < 9; i++) {
                 highScores.add(new HighScore("empty slot", 0));
             }
         }
     }
 
     @Override
     public Array<HighScore> getHighScores() {
         return highScores;
     }
 
     @Override
     public void add(HighScore highScore) {
         highScores.add(highScore);
         highScores.sort();
         highScores.removeValue(highScores.get(highScores.size - 1), true);
         prefs.clear();
         prefs.flush();
 
         for (int i = 0; i < 9; i++) {
             if (highScores.get(i) != null) {
                 prefs.putString("name" + i, highScores.get(i).getName());
                 prefs.putLong("score" + i, highScores.get(i).getScore());
             } else {
                 prefs.putString("name" + i, "empty slot");
                 prefs.putLong("score" + i, 0);
             }
         }
 
         prefs.flush();
     }
 
 }
