 package Roma.Cards;
 
import Roma.*;
 
 /**
  * File Name:
  * Creator: Varun Nayyar
  * Date: 11/04/12
  * Desc:
  */
 public class Basilica extends Card {
 
     private final static String NAME = "Basilica";
     private final static String TYPE = "Building";
     private final static String DESCRIPTION = "If a Forum is activated (it must lie directly next to the basilica)," +
             " the player gets 2 more victory points. The Basilica itself is not activiated.";
     private final static int COST = 5;
     private final static int DEFENCE = 2;
 
 
     public Basilica(PlayArea playArea){
         super(NAME, TYPE , DESCRIPTION,COST, DEFENCE, playArea);
 
     }
 
     public void activate(int player){
 
     }
 
 }
