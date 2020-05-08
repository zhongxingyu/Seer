 /**
  * 
  */
 package simulation.actions;
 
 import empire.Empire;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import simulation.GameEvent;
 import simulation.Simulator;
 import simulation.StarCheck;
 import state.*;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Stores information when a star system can be colonized by an empire, allowing such an action.
  * @author Daniel Langdon
  */
 public class Colonization extends GameEvent
 {
 	Fleet fleet;
 	Unit unit;
 	
 	/**
 	 * Constructs a colonization event. 
 	 */
 	public Colonization(Star location, Fleet fleet, Unit unit)
 	{
 		super(location);
 		this.fleet = fleet;
 		this.unit = unit;
 	}
 	
 	/**
 	 * @return The icon to use if this action is going to be added to an interface.
 	 */
 	public Image icon()
 	{
 		// FIXME Ugly, I definitely need to tackle the whole resources thingy...
 		try
 		{
 			return new Image("resources/icon_colonize.png");
 		}
 		catch (SlickException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	/**
 	 * @return An index that determines where this icon is put on the interface. The idea is that actions appear on the same location, independently of what is currently available.
 	 */
 	public int slot()
 	{
 		return 1;
 	}
 
 	/* (non-Javadoc)
 	 * @see simulation.GameEvent#description()
 	 */
 	@Override
 	public String description()
 	{
 		return "Colonize this star system.";
 	}
 	
 	@Override
 	public void runAction()
 	{
 		// Create the colony.
 		new Colony(location(), fleet.owner());
 		fleet.addUnits(unit, -1);
 
 		// Add new options that become available for the colony
 		Simulator.instance().addEvent(new ShipyardBuildEvent(location())); // TODO remove.
 		
 		// Can't colonize twice. The event is done.
 		Simulator.instance().removeEvent(this);
 	}
 
 	public static StarCheck check = new StarCheck()
 	{
 		@Override
 		public GameEvent check(Star location)
 		{
 			// We don't need previous colonization events.
 			List<GameEvent> existing = Simulator.instance().eventsForLocation(location);
 			Iterator<GameEvent> i = existing.iterator();
 			while (i.hasNext())
 			{
 				GameEvent e = i.next();
 				if (e instanceof Colonization)
 					i.remove();
 			}
 
 			// If there is a colony already, nothing to do.
 			Colony colony = location.getPlaceable(Colony.class);
 			if (colony != null)
 				return null;
 
 			// Are there still fleets in orbit?
 			List<Fleet> fleets = location.getFleets();
 			if (fleets.isEmpty())
 				return null;
 
 			Empire e = fleets.get(0).owner();
 			Fleet canColonize = null;
 			Unit toSpend = null;
 			for (Fleet f : fleets)
 			{
 				// Check if there are fleets from more than one empire.
 				if (f.owner() != e)
 					return null;
 
 				// Check if this fleets contains a colony ship.
 				for (Map.Entry<Unit, UnitStack> stack : f.stacks().entrySet())
 				{
 					// TODO This is hardcoded for now. Eventually we need to be able to discover which unit has special abilities of any kind.
 					if (stack.getKey().name().compareToIgnoreCase("Colony Ship") == 0)
 					{
 						canColonize = f;
 						toSpend = stack.getKey();
 						break;
 					}
 				}
 			}
 
 			// Create the option to colonize.
 			if (canColonize != null)
 			{
				Colonization c = new Colonization(location, canColonize, toSpend);
				Simulator.instance().addEvent(c);
				return c;
 			}
 			return null;
 		}
 	};
 }
