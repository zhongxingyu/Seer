 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package humanplayer;
 
import info.gridworld.actor.Critter;
import info.gridworld.grid.Location;
import java.util.ArrayList;

 /**
  *
  * @author mark
  */
 public class ChildPlayer extends HumanPlayer
 {

    public ChildPlayer ()
    {
        super();
    }
    public ChildPlayer(int i)
    {
        //super(i);
    }

    @Override
    public void makeMove (Location loc)
    {
        ArrayList<Location> locations = getGrid ().getOccupiedLocations ();
        for (int j = 0; j < locations.size () - 1; j++)
        {
            if ((getGrid().get (locations.get (j)) instanceof HumanPlayer) && !(getGrid().get (locations.get (j)) instanceof ChildPlayer))
            {
                getGrid ().put (new Location ((int) (Math.random () * getGrid ().getNumRows ()), (int) (Math.random () * getGrid ().getNumRows ())), new HumanPlayer());
            }
        }
        super.makeMove (loc);
    }
 }
