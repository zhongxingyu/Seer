 package Roma.Cards;
 
import Roma.PlayArea;
 
 /**
  * File Name:
  * Creator: Varun Nayyar
  * Date: 11/04/12
  * Desc:
  */
 public class Mercatus extends Card {
 
     private final static String NAME = "Mercatus";
     private final static String TYPE = "Building";
     private final static String DESCRIPTION = "The player gets " +
             "1 victory point for every face-up Forum that the opponent has.";
     private final static int COST = 6;
     private final static int DEFENCE = 3;
 
 
     public Mercatus(PlayArea playArea){
         super(NAME, TYPE , DESCRIPTION,COST, DEFENCE, playArea);
 
     }
 
     public void activate(int player){
 
     }
 
 }
