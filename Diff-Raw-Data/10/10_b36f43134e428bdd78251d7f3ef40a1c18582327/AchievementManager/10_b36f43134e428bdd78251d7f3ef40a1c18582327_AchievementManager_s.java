 /*
  *  Wezzle
  *  Copyright (c) 2007-2008 Couchware Inc.  All rights reserved.
  */
 
 package ca.couchware.wezzle2d.manager;
 
import ca.couchware.wezzle2d.event.MoveEvent;
import ca.couchware.wezzle2d.manager.ListenerManager.GameType;
 import ca.couchware.wezzle2d.util.CouchLogger;
 import ca.couchware.wezzle2d.Game;
 import ca.couchware.wezzle2d.ManagerHub;
 import ca.couchware.wezzle2d.event.CollisionEvent;
 import ca.couchware.wezzle2d.event.ICollisionListener;
import ca.couchware.wezzle2d.event.IMoveListener;
 import ca.couchware.wezzle2d.manager.Settings.Key;
import ca.couchware.wezzle2d.tile.Tile;
 import ca.couchware.wezzle2d.util.SuperCalendar;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * A class to manage achievements.
  * The manager holds an arraylist of achievements not yet achieved
  * and a list of achievements which have already been achieved
  * 
  * every iteration of the game, the manager passes the gamestate into all of
  * the achievements which check to see if they have been achieved. The 
  * achievments have an evaluate(gamestate) function which returns a boolean.
  * if the achievement has been achieved, the achievement is moved from the
  * arraylist into the achieved array list and displayed to the screen.
  *
  * @author Kevin
  */
 public class AchievementManager implements ICollisionListener
 {
 
     /** The settings manager. */
     final private SettingsManager settingsMan;
     
     /** A flag that is set to true if an achievement has been completed. */
     private boolean achievementCompleted = false;
     
     /** The unachieved achievements. */
     private List<Achievement> incompletedList;
     
     /** The achieved achievements. */
     private List<Achievement> completedList;
     
     /** 
      * The newly achieved achievements.  This list holds achievements until
      * they are asked for by <pre>getNewlyCompletedAchievements()</pre>.
      */
     private List<Achievement> newlyCompletedList;
     
     /** The master list */
     //private List<Achievement> masterList;
         
     /**
      * The constructor.
      */
     private AchievementManager(SettingsManager settingsMan)
     {
         this.settingsMan = settingsMan;
         
         this.incompletedList    = new ArrayList<Achievement>();
         this.completedList      = new ArrayList<Achievement>();
         this.newlyCompletedList = new ArrayList<Achievement>();        
         
         this.importAchievements();
     }
     
     /**
      * Return a new AchiementManager instance.
      * 
      * @return
      */
     public static AchievementManager newInstance(SettingsManager settingsMan)
     {
         return new AchievementManager(settingsMan);
     }
     
     /**
      * Import the achievements from the settings amanager.
      */
     final private void importAchievements()
     {        
         // Get the master list from the settings manager. 
         List masterList = this.settingsMan.getList(Key.USER_ACHIEVEMENT);
         
         // Get the completed list.
         List storedCompletedList = null;
         if (this.settingsMan.containsKey(Key.USER_ACHIEVEMENT_COMPLETED))
         {
             storedCompletedList = this.settingsMan.getList(Key.USER_ACHIEVEMENT_COMPLETED);
         }
         else
         {
             storedCompletedList = new ArrayList();
         }        
         
         for (Object object : masterList)
         {
             Achievement ach = (Achievement) object;
             
             if (!storedCompletedList.contains(ach))
             {
                 this.incompletedList.add(ach);
             }                            
         }        
         
         for (Object object : storedCompletedList)
         {
             Achievement ach = (Achievement) object;
             this.completedList.add(ach);
         }
     }          
     
     /**
      * Export the achievements to the settings manager.
      */
     private void exportAchievements()
     {
         CouchLogger.get().recordMessage(this.getClass(), "Exported achievements to settings manager.");
         this.settingsMan.setObject(Key.USER_ACHIEVEMENT_COMPLETED, this.completedList);
     }
 
     /**
      * Returns true if one or more achievement has recently been completed.
      * 
      * @return
      */
     public boolean isNewAchievementCompleted()
     {
         return achievementCompleted;
     }
 
     /**
      * Clears the achievement completed flag.
      * 
      * @param achievementCompleted
      */
     public void clearNewAchievementCompleted()
     {
         this.achievementCompleted = false;
     }        
     
     public List<Achievement> getNewlyCompletedAchievementList()
     {
         List<Achievement> list = new ArrayList<Achievement>(this.newlyCompletedList);
         this.newlyCompletedList.clear();
         return list;
     }
     
     /**
      * Report the completed descriptions to the console.
      */
     public void reportNewlyCompleted()
     {
         // Clear the achievement completed flag.
         this.achievementCompleted = false;
         
         for (int i = 0; i < newlyCompletedList.size(); i++)
             CouchLogger.get().recordMessage(this.getClass(), newlyCompletedList.get(i).toString());
     }     
     
     private void completeAchievement(Achievement achievement)
     {
         // Set the date.
         Achievement completedAchievement = 
             Achievement.newInstance(achievement, SuperCalendar.newInstance());
         this.newlyCompletedList.add(completedAchievement);
         this.completedList.add(completedAchievement);
 
         // Set the flag.
         this.achievementCompleted = true;
 
  
     }
     
     /**
      * Evaluate each achievement. 
      * If the achievement is completed transfer from the incomplete to 
      * the completed lists.
      * 
      * @param game The state of the game.
      * @return True if an achievement was completed, false otherwise.
      */
     public boolean evaluate(Game game, ManagerHub hub)
     {
         boolean achieved = false;
         
         for (Iterator<Achievement> it = incompletedList.iterator(); it.hasNext(); )
         {
             Achievement achievement = it.next();
             
             if (achievement.evaluate(game, hub))
             {
                 completeAchievement(achievement);
                 it.remove();
                 achieved = true;
             }
         }
 
         // If we have achieved something, check meta achievements.
         if (achieved == true)
         {
             for (Iterator<Achievement> itr = incompletedList.iterator(); itr.hasNext(); )
             {
                 Achievement ach = itr.next();
 
                 if (ach.evaluateMeta(this))
                 {
                     completeAchievement(ach);
                     itr.remove();
                 }
             } // end for
         }
         
         // Export.
         if (achieved)
         {
             exportAchievements();
         }
 
         return achieved;
     }
     
     /**
      * Listens for collision events.
      * 
      * @param e
      */
     public void collisionOccured(CollisionEvent e)
     {               
         CouchLogger.get().recordMessage(this.getClass(), e.getChain().getTree().toString());
         
         // Set to true if an achievement was achieved.
         boolean achieved = false;
         
         for (Iterator<Achievement> it = incompletedList.iterator(); it.hasNext(); )
         {
             Achievement achievement = it.next();
 
             if (achievement.evaluateCollision(e.getChain().getTree()))
             {
                 completeAchievement(achievement);
                 it.remove();
                 achieved = true;
             }
         } // end for
 
          // If we have achieved something, check meta achievements.
         if(achieved == true)
         {
             for (Iterator<Achievement> itr = incompletedList.iterator(); itr.hasNext(); )
             {
                 Achievement ach = itr.next();
 
                 if (ach.evaluateMeta(this))
                 {
                     completeAchievement(ach);
                     itr.remove();
                 }
             } // end for
 
         }
         
         // Export if achieved.
         if (achieved) exportAchievements();
     }               
     
     /**
      * Get the list of all achievements.
      * 
      * @return The master list.
      */
     public List<Achievement> getAchievementList()
     {
         List<Achievement> list = new ArrayList<Achievement>();        
         list.addAll(completedList);
         list.addAll(incompletedList);                
         return list;
     }    
     
     public int getNumberOfAchievements()
     {
         return this.completedList.size() + this.incompletedList.size();
     }
     
     public int getNumberOfCompletedAchievements()
     {
         return this.completedList.size();
     }
 
     public List<Achievement> getIncompletedAchievementList()
     {
         return this.incompletedList;
     }
 
     public List<Achievement> getCompletedAchievementList()
     {
         return this.completedList;
     }
 
   
 }
