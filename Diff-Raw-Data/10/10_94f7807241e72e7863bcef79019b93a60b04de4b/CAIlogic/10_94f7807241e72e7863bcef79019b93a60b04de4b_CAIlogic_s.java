 package com.google.appengine.aethanengine;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.ArrayList;
 import com.google.appengine.aethanengine.AEthan;
 
 /*
  * CAIlogic Handels Game Logic Routines
  */
 public class CAIlogic
 {
 
   //STRUCTS
 
   class Fielding {
     String pitcher = new String();
     String firstBaseman = new String();
     String secondBaseman= new String();
     String thirdBaseman= new String();
     String catcher= new String();
     String shortstop= new String();
     String rightFielder= new String();
     String leftFielder= new String();
     String centerFielder= new String();
    
  }
    
    
    Fielding thoughts_fielding;
     ArrayList thoughts_currentbattingorder = new ArrayList();
 
   // Implement Logging
    private static final Logger LOG = Logger.getLogger(MainServlet.class.getName());
    
     /*
     * AI To Determine Batting Order
     */
    public void aiDetermineBattingOrder(CTeam team)
    {
       LOG.info("AI.. DetermineBattingOrder");
      ArrayList thoughts = new ArrayList();
 
       //TODO Batting Order set First to Last.. no logic here now
       for (int x = 0; x < 10; x++)
       {
          thoughts_currentbattingorder.add(team.player[x].humanID.toString());
       }
 
       //Logging
      LOG.info("Batting Order: " + thoughts);
       
 
    }
    
    /*
     * AI To Determine Fielding
     */
    public void aiDetermineFielding(CTeam team)
    {
      LOG.info("AI.. DetermineFielding");

     AEthan.Lineup fielding = new AEthan.Lineup();
      
      //TODO put together some real logic code.. right now first in first out
      thoughts_fielding.pitcher = team.player[0].humanID.toString();
      thoughts_fielding.firstBaseman = team.player[1].humanID.toString();
      thoughts_fielding.secondBaseman = team.player[2].humanID.toString();
      thoughts_fielding.thirdBaseman = team.player[3].humanID.toString();
      thoughts_fielding.catcher = team.player[4].humanID.toString();
      thoughts_fielding.shortstop = team.player[5].humanID.toString();
      thoughts_fielding.rightFielder = team.player[6].humanID.toString();
      thoughts_fielding.leftFielder = team.player[7].humanID.toString();
      thoughts_fielding.centerFielder = team.player[8].humanID.toString();
      
      LOG.info("AI.. Fielding thinking done.");
 
    }
 
 }
