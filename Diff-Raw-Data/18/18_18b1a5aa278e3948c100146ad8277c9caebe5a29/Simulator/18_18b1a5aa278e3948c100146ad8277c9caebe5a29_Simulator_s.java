 package simulation;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import empire.Empire;
 import empire.View;
 import simulation.checks.FleetMerger;
 import galaxy.structure.MovementObserver;
 import galaxy.structure.Place;
 import galaxy.structure.Placeable;
 import simulation.checks.InvasionCheck;
 import simulation.checks.SpaceCombatCheck;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 
 import simulation.actions.Colonization;
 import state.*;
 
 /**
  * HIGHLY EXPERIMENTAL Event queue for special turn events. This is needed to
  * solve a special set of conflicts arising during the turn update that require
  * additional interaction, race conditions, information, etc. For instance: - A
  * fleet A arriving into a system might need to be merged to a fleet B already
  * in it. But we won't know if B will remain in the system until all fleets have
  * been updated. - A fleet A arriving into a system might engage a fleet B
  * already in it, a separate module needs to be called to solve the conflict and one of them might be destroyed. -
  * An important event should go into a situation report or something similar.
  * 
  * @author Daniel Langdon
  */
 public class Simulator
 {
 // Statics ==========================================================================================================	
 	private static Simulator instance_;
 
 	public static Simulator instance()
 	{
 		return instance_;
 	}
 
 // Internals ==========================================================================================================	
 
 	private int turn;
 	private Set<Star> startsToCheckForEvents;
 	private Set<Lane> lanesToCheckForEvents;
 	private HashMap<Star, List<GameEvent>> events;
 
 // Public methods ==========================================================================================================	
 
 	/**
 	 * Constructor.
 	 */
 	public Simulator()
 	{
 		instance_ = this;
 		startsToCheckForEvents = new HashSet<>();
 		lanesToCheckForEvents = new HashSet<>();
 
 		events = new HashMap<>();
 		turn = 0;
 
 		Place.addObserver(new MovementObserver()
 		{
 			@Override
 			public void arrivedAt(Placeable object, Place location)
 			{
 				if(location instanceof Star)
 					startsToCheckForEvents.add((Star)location);
 				else if(location instanceof Lane)
 					lanesToCheckForEvents.add((Lane)location);
 			}
 
 			@Override
 			public void departedAt(Placeable object, Place location)
 			{
 				arrivedAt(object, location);
 			}
 		});
 	}
 
 	public void addLaneToCheck(Lane l)
 	{
 		lanesToCheckForEvents.add(l);
 	}
 
 	/**
 	 * Adds an event to the turn's event queue.
 	 * 
 	 * @param event
 	 *           The event to add.
 	 */
 	public void addEvent(GameEvent event)
 	{
 		List<GameEvent> localEvents = events.get(event.location());
 		if (localEvents == null)
 		{
 			localEvents = new ArrayList<GameEvent>();
 			events.put(event.location(), localEvents);
 		}
 		localEvents.add(event);
 	}
 	
 	/**
 	 * @param location
 	 *           A location in the galaxy where events may occur.
 	 * @return A list of all events tied to the specified location, which could
 	 *         be empty. A copy of stored events is always returned, as actions on these events could modify this list by removing or adding items.
 	 */
 	public List<GameEvent> eventsForLocation(Star location)
 	{
 		List<GameEvent> localEvents = events.get(location);
		return localEvents == null ? new ArrayList<GameEvent>() : new ArrayList<GameEvent>(localEvents);
 	}
 	
 	public void removeEvent(GameEvent event)
 	{
 		List<GameEvent> localEvents = events.get(event.location());
 		localEvents.remove(event);
 	}
 
 	/**
 	 * Process a turn. This is done by running all turn sub-processes, which
 	 * depend on individual modules. The order at which these processes are run
 	 * is important, hence registration order matters.
 	 */
 	public void nextTurn()
 	{
 		// Jump the clock forward.
 		turn++;
 		System.out.println("New turn: " + turn);
 
 		// Resets
 		for (Empire e : Empire.all())
 			e.getEconomy().resetTurn();
 
 		// Produce new units
 		startsToCheckForEvents.clear();
 		for (HQ hq : HQ.all())
 			hq.turn();
 
 		// Update all fleets
 		for (Fleet f : Fleet.all())
 			f.turn();
 
 		// Update all empires.
 		for (Empire e : Empire.all())
 			e.getEconomy().applyGrowth(e.getColonies());
 
 		// Check for new events.
 		System.out.println("Checking events for turn " + turn);
 		for (Star s : startsToCheckForEvents)
 		{
 			new FleetMerger().check(s);
 			View.checkStarVisibility.check(s);
 			new SpaceCombatCheck().check(s);
 			new SpaceCombatCheck().check(s);
 			new InvasionCheck().check(s);
 			Colonization.check.check(s);
 		}
 
 		// Update all lanes (has to be after stars, since we might need to check some lanes added manually when a star is no longer visible.
 		for(Lane l : lanesToCheckForEvents)
 		{
 			View.checkLaneVisibility.check(l);
 		}
 
 
 		// TODO Try to process outstanding events.
 		// TODO Update power snapshots for graphs and AI
 	}
 
 	/**
 	 * @param gc
 	 * @param delta
 	 * @return True if all input was processed on this event (effectively
 	 *         creating a modal dialog for instance), else false.
 	 * @throws SlickException
 	 */
 	public boolean update(GameContainer gc, int delta) throws SlickException
 	{
 		// TODO
 //		Iterator<GameEvent> i = events.iterator();
 //		for(GameEvent e: events)
 //		{
 //			e.update(gc, delta);
 //
 //			if(e.status() != Status.DONE)
 //				return true;					// FIXME for now, all events are blocking (modal).
 //			events.removeFirst();
 //		}
 		return false;
 	}
 
 	public int getTurnCount()
 	{
 		return turn;
 	}
 }
