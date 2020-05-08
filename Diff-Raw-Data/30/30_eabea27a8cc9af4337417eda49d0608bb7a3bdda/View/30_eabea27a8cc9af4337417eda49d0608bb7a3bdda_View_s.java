 /**
  *
  */
 package empire;
 
 import galaxy.structure.Place;
 import galaxy.structure.Placeable;
 import graphic.Render.Visibility;
 import simulation.GameEvent;
 import simulation.LaneCheck;
 import simulation.Simulator;
 import simulation.StarCheck;
 import state.Lane;
 import state.Star;
 
 import java.util.HashMap;
 
 /**
  * A view of the current state of the galaxy, as seen by a single empire.
  * This is a highly skewed view, as each empire will only have partial knowledge about what is going on, both due to fog of war and invisible units.
  * It is more efficient to update this view every time a turn begins, since we can distribute objects from the true state to each empire's view without having to go through it multiple times.
  * The scope of this class does not include understanding of tactical status (defended or undefended territory, frontier or core system, etc). The idea is to leave those concepts for the player or AI.
  * The scope is thus limited to rechableStars and accessibility (this given by empire relationships)
  *
  * For efficiency purposes, the visible state of all empires can be computed on a single run.
  * This is a good idea since even while there is a single player which view is being rendered, the AI will use this partial view to make decisions (we'll make it cheat in other ways hehe).
  * Also, for debugging/observing purposes we might want to switche the rendered view to what other empire is seeing.
  */
 public class View
 {
 	private HashMap<Lane, Visibility> reachableLanes;
 	private HashMap<Star, Visibility> rechableStars;
 
 	View()
 	{
 		rechableStars = new HashMap<>();
 		reachableLanes = new HashMap<>();
 	}
 
 	public HashMap<Star, Visibility> getRechableStars()
 	{
 		return rechableStars;
 	}
 
 	public HashMap<Lane, Visibility> getReachableLanes()
 	{
 		return reachableLanes;
 	}
 
 	public static Visibility getVisibility(Place p)
 	{
 		View v = Empire.getPlayerEmpire().view();
 		Visibility aux = v.rechableStars.get(p);
 		if(aux != null)
 			return aux;
 		return Visibility.HIDDEN;
 	}
 
 	/**
 	 * Checks if a given star is visible by all empires.
 	 * A star is only visible if the empire owns it or has units on it.
 	 */
 	public static StarCheck checkStarVisibility = new StarCheck()
 	{
 		@Override
 		public GameEvent check(Star s)
 		{
 			for (Empire e : Empire.all())
 			{
 				View view = e.view();
 				Visibility previous = view.getRechableStars().get(s);
				Visibility next = Visibility.REMEMBERED;
 
 				for(Placeable p: s.allPlaceables())
 					if(p.owner() == e)
 					{
 						next = Visibility.VISIBLE;
 						break;
 					}
 
				if(previous != next)
 				{
 					view.rechableStars.put(s, next);
 
 					for(Lane l: Lane.outgoingLanes(s))
 					{
 						// If a star is no longer visible, the lanes around it might also not be...
 						// They need to be checked after every star has updated visibility.
 						if(next == Visibility.REMEMBERED)
 							Simulator.instance().addLaneToCheck(l);
 
 						// If a new star is visible, lanes are visible and new stars can be uncovered.
 						else
 						{
 							view.reachableLanes.put(l, Visibility.VISIBLE);
 
 							// This might discover some stuff.
 							Star to = l.exitPoint(s);
 							if(!view.rechableStars.containsKey(to))
 								view.rechableStars.put(to, Visibility.REACHABLE);
 						}
 					}
 				}
 			}
 			// TODO Extend to a list of allies.
 			return null;
 		}
 	};
 
 	public static LaneCheck checkLaneVisibility = new LaneCheck() {
 		@Override
 		public GameEvent check(Lane l)
 		{
 			// Remember if it was visible.
 			for (Empire e : Empire.all())
 			{
 				Visibility v = Visibility.REMEMBERED;
 				if(e.view().rechableStars.get(l.from()) == Visibility.VISIBLE || e.view().rechableStars.get(l.to()) == Visibility.VISIBLE)
 					v = Visibility.VISIBLE;
 				else
 				{
 					for(Placeable p: l.allPlaceables())
 						if(p.owner() == e)
 							v = Visibility.VISIBLE;
 				}
 
 				if(e.view().reachableLanes.containsKey(l) || v == Visibility.VISIBLE)
 					e.view().reachableLanes.put(l, v);
 			}
 			return null;
 		}
 	};
 }
