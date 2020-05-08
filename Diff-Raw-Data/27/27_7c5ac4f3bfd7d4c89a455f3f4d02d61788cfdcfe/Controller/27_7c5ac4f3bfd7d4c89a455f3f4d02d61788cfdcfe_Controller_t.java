 package ru.nsu.vakhrushev.pusher.controller;
 
 import ru.nsu.vakhrushev.pusher.model.Model;
 import ru.nsu.vakhrushev.pusher.viewer.Viewer;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.util.Properties;
 import java.util.Set;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Vakhrushev Maxim
  * Date: 10.04.13
  * Time: 18:12
  */
 public class Controller
 {
     private static final int columnsInScores = 4;
    private static final int timeColumn = 1;
    private static final int stepsColumn = 2;
 
     private boolean gameIsPlaying = false;
     private boolean gameStarted = false;
     private String defaultLevelInfo = "NotComplete;NotComplete;NotComplete";
     private String propertyPath;
     private Object[][] scoresData = null;
     private Properties prop = new Properties();
     private Timer timer;
     private String currLevelName;
     private Score score;
 
     private Viewer viewer;
 
     public Controller () {}
 
     public void startGame()
     {
         viewer = new Viewer(this);
         gameStarted = true;
     }
 
     public boolean isPlaying()
     {
         return gameIsPlaying;
     }
 
     public void startLevel (String levelName) throws IOException
     {
         Model model = new Model(levelName);
         currLevelName = levelName;
         gameIsPlaying = true;
         viewer.setModel(model);
         model.addObserver(viewer);
         score = new Score();
         score.startCount();
 
         timer = new Timer(1000, new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 score.setTimeInSec(System.currentTimeMillis());
                 SwingUtilities.invokeLater(new Runnable() {
                     @Override
                     public void run()
                     {
                         viewer.updateInfoPanel(score);
                     }
                 });
             }
         });
 
         timer.start();
     }
 
     public void stopTimer()
     {
         timer.stop();
     }
 
    public boolean isNewRecord (String levelName, long newTime, long newSteps)
     {
         String str = prop.getProperty(levelName);
         if (str.equals(defaultLevelInfo))
         {
             return true;
         }
         else
         {
             String [] levelInfo = prop.getProperty(levelName).split(";");
             {
                if (Integer.decode(levelInfo[timeColumn]) > newTime || (Integer.decode(levelInfo[timeColumn]) == newTime && Integer.decode(levelInfo[stepsColumn]) > newSteps))
                 {
                     return true;
                 }
             }
         }
         return false;
     }
 
     public void exitLevel ()
     {
        stopTimer();
        if (isNewRecord(currLevelName, score.getTime(), score.getSteps()))
         {
             String playerName = viewer.getPlayerName();
             if (playerName != null)
             {
                 prop.setProperty(currLevelName, playerName + ";" + score.getTime() + ";" + score.getSteps());
                 saveScores();
             }
         }
         else
         {
             viewer.showEndLevelMessage();
         }
         gameIsPlaying = false;
     }
 
     public void saveScores ()
     {
         try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream("src/ru/nsu/vakhrushev/pusher/controller/scoresProperty.txt")), true))
         {
             Set<String> set = prop.stringPropertyNames();
             for (String str : set)
             {
                 String outString = str + "=" + prop.getProperty(str);
                 writer.println(outString);
 //                System.out.println(outString);
             }
             writer.close();
         }
         catch (IOException e)
         {
             System.err.println(e.getLocalizedMessage());
             e.printStackTrace();
         }
     }
     public void readScores (String fileName)
     {
         try
         {
             propertyPath = fileName;
             int count = 0;
             if (!gameStarted)
             {
                 prop.load(this.getClass().getResourceAsStream(propertyPath));
             }
             Set<String> set = prop.stringPropertyNames();
             scoresData = new Object[set.size()][columnsInScores];
 
             for (String str : set)
             {
                 String [] levelInfo = prop.getProperty(str).split(";");
                 scoresData[count][0] = str;
                 System.arraycopy(levelInfo, 0, scoresData[count], 1, levelInfo.length);
                 count++;
             }
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
     }
     public void clearScores ()
     {
         Set<String> set = prop.stringPropertyNames();
         for (String str : set)
         {
             prop.setProperty(str, defaultLevelInfo);
         }
         saveScores();
     }
 
     public Score getScore()
     {
         return score;
     }
 
     public Object[][] getScoresData()
     {
         return scoresData;
     }
 }
