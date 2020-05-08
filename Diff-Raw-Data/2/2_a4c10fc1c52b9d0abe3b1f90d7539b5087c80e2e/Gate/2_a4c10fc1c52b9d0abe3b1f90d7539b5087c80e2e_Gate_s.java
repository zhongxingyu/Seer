 import java.util.ArrayList;
 import info.gridworld.actor.Actor;
 import info.gridworld.grid.Grid;
 import info.gridworld.grid.Location;
 
 public class Gate extends Actor
 {
  private int count;
 
 	public Gate()
 	{
 		count = 0;
 		this.setColor(null);
 	}
 
 	public void act()
 	{
 		if(count == 2)
 		{
 			turn();
 			count = 0;
 		}
 		count++;
 	}
 
 	public void turn()
 	{
 		setDirection(getDirection() + Location.HALF_RIGHT);
 	}
 
 	//If this wipes everything behind it, there has to be a
 	//recursive method involved.
 	public ArrayList<Actor> getDetonateTargets(int direction)
 	{
 		Grid<Actor> gr = getGrid();
 		ArrayList<Actor> targets = new ArrayList<Actor>();
 		Location loc = this.getLocation();
 
 		for(int r = loc.getRow()-3; r <= loc.getRow()+3; r++)
 		{
 			for(int c = loc.getCol()-3; c <= loc.getCol()+3; c++)
 			{
 				Location check = new Location(r,c);
 				if(gr.isValid(check) && ((gr.get(check) instanceof Drone) ||
 						(gr.get(check) instanceof CentipedeTail)))/* && !(gr.get(check) instanceof Gate)
 					&& !(gr.get(check) instanceof SpaceDebris) && !(gr.get(check) instanceof PowerUp))*/
 					targets.add(gr.get(check));
 			}
 		}
 		return targets;
 	}
 
 	public void detonate(int direction)
 	{
 		ArrayList<Actor> targets = getDetonateTargets(direction + 180);
 		for(Actor a : targets)
 			((Drone)(a)).die();
 		removeSelfFromGrid();
 	}
 }
