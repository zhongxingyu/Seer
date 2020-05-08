 package edu.uwm.cs552.playerchoice;
 
 import java.awt.Point;
 
import edu.uwm.cs552.PlayerChoice;

 public class BuildTrackChoice implements PlayerChoice {
 
   @Override
   public void doThings(Point p) {
     // TODO Auto-generated method stub
     System.out.println("You want to Build Track at " + p.toString());
   }
 
   public String toString() {
     return "Build Track";
   }
 }
