 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package humanplayer;
 
 import info.gridworld.actor.ActorWorld;
 import info.gridworld.actor.Critter;
 import info.gridworld.grid.Location;
 import java.awt.Color;
 
 /**
  *
  * @author root
  */
 public class HumanPlayerRunner
 {
 
     public static void main (String[] args)
     {
         ActorWorld world = new ActorWorld ();
         HumanPlayer alice = new HumanPlayer (9);
        HumanPlayer frank = new HumanPlayer (6);
         frank.setColor (Color.darkGray);
         alice.setColor (Color.PINK);
         HumanPlayer bob = new HumanPlayer (3);
         for (int i = 0; i < 9; i++)
         {
             for (int j = 0; i < 9; i++)
             {
                 world.add(new Critter ());
             }
         }
         world.add (new Location (7, 8), alice);
         world.add (new Location (5, 5), bob);
         world.add (new Location (5, 8), frank);
         world.show ();
     }
 }
