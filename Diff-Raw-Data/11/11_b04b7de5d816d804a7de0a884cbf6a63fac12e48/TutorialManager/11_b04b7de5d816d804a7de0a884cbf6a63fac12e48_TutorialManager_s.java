 /*
  *  Wezzle
  *  Copyright (c) 2007-2008 Couchware Inc.  All rights reserved.
  */
 
 package ca.couchware.wezzle2d.tutorial;
 
 import ca.couchware.wezzle2d.*;
 import ca.couchware.wezzle2d.BoardManager.AnimationType;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 /**
  *
  * @author cdmckay
  */
 public class TutorialManager 
 {
 
     /**
      * The tutorial list.
      */
     private ArrayList<ITutorial> tutorialList;
     
     /**
      * The currently running tutorial.
      */    
     private ITutorial currentTutorial;
     
     public TutorialManager()
     {
         // Initialize animation list.
         tutorialList = new ArrayList<ITutorial>();                
     }
     
     /**
      * Add a tutorial to the manager's list.
      * 
      * @param t
      */
     public void add(AbstractTutorial t)
     {
         tutorialList.add(t);       
     }
     
     /**
      * Remove the tutorial to the manager's list.
      * 
      * @param t
      */
     public void remove(AbstractTutorial t)
     {
         if (contains(t) == true)
             tutorialList.remove(t);
     }
     
     /**
      * Check if the given tutorial is in the manager's list?
      * 
      * @param t
      * @return
      */
     public boolean contains(AbstractTutorial t)
     {
         return tutorialList.contains(t);
     }   
     
     /**
      * Is a tutorial currently running?
      * 
      * @return True if a tutorial is running, false otherwise.
      */
     public boolean isTutorialInProgress()
     {
         return currentTutorial != null;
     }
 
     /**
      * Get the currently running tutorial.
      * 
      * @return The currently running tutorial, or null, if no tutorial is running.
      */
     public ITutorial getTutorialInProgress()
     {
         return currentTutorial;
     }
     
     public void updateLogic(Game game)
     {
         // If the board is refactoring, do not logicify.
         if (game.isBusy() == true)
              return;
         
         // If no tutorial is running, look for a new one to run.
         if (currentTutorial == null && tutorialList.isEmpty() == false)
         {
             for (Iterator<ITutorial> it = tutorialList.iterator(); it.hasNext(); ) 
             {
                 ITutorial t = it.next();
                 
                 // Check to see if the tutorial is activated.                
                 if (t.evaluateRules(game) == true)
                 {       
                     if (game.boardMan.isVisible() == true)                    
                     {
                         // Hide the board nicely.
                         // Make sure the grid doesn't flicker on for a second.
                         game.startBoardHideAnimation(AnimationType.SLIDE_FADE);    
                         game.pieceMan.getPieceGrid().setVisible(false); 
                         break;
                     }
                     
                     currentTutorial = t;
                     currentTutorial.initialize(game);
                     it.remove();                    
                     break;
                 }                                                                    
             } // end for
         }    
         
         // See if there is a tutorial running.
         if (currentTutorial != null)
         {
             currentTutorial.updateLogic(game);
             if (currentTutorial.isDone() == true)
             {
                 game.startBoardShowAnimation(AnimationType.SLIDE_FADE);
                 currentTutorial = null;
             }
         }
 
         //Util.handleMessage(animationList.size() + "", "AnimationManager#animate");
     }
     
 }
