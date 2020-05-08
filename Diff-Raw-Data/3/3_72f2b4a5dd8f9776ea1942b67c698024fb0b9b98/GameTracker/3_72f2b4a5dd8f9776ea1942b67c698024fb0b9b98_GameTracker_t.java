 package ca.hilikus.jrobocom;
 
 import java.util.ArrayList;
 import java.util.EventListener;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ca.hilikus.jrobocom.World.WorldListener;
 import ca.hilikus.jrobocom.events.EventDispatcher;
 import ca.hilikus.jrobocom.events.GenericEventDispatcher;
 import ca.hilikus.jrobocom.events.LeaderChangedEvent;
 import ca.hilikus.jrobocom.events.PlayerEliminatedEvent;
 import ca.hilikus.jrobocom.events.ResultEvent;
 import ca.hilikus.jrobocom.events.RobotAddedEvent;
 import ca.hilikus.jrobocom.events.RobotMovedEvent;
 import ca.hilikus.jrobocom.events.RobotRemovedEvent;
 import ca.hilikus.jrobocom.security.GamePermission;
 import ca.hilikus.jrobocom.timing.MasterClock.ClockListener;
 
 /**
  * Keeps track of the statistics of the game
  * 
  * @author hilikus
  */
 public class GameTracker {
 
     private GenericEventDispatcher<GameStatusListener> eventDispatcher = new GenericEventDispatcher<>();
 
     private Map<Player, Integer> robotsCount = new HashMap<>();
 
     private static final Logger log = LoggerFactory.getLogger(GameTracker.class);
 
     private List<Player> currentLeaders = new ArrayList<>();
 
     private EventsHandler eventHandler = new EventsHandler();
 
     /**
      * Interface to receive events about the status of the game
      * 
      */
     public interface GameStatusListener extends EventListener {
 	/**
 	 * Called when a game ends
 	 * 
 	 * @param result info about the result
 	 */
 	public void update(ResultEvent result);
 
 	/**
 	 * Called when the leaders of the game changed
 	 * 
 	 * @param event info about the leaders
 	 */
 	public void update(LeaderChangedEvent event);
     }
 
     /**
      * Receives events the tracker is interested in
      * 
      * @author hilikus
      */
     public class EventsHandler implements WorldListener, ClockListener {
 
 	@Override
 	public void update(RobotAddedEvent add) {
 	    Player owner = add.getSource().getOwner();
 	    if (!robotsCount.containsKey(owner)) {
 		// first robot of the player
 		robotsCount.put(owner, 1);
 	    } else {
 		int old = robotsCount.get(owner);
 		robotsCount.put(owner, old + 1);
 	    }
	    findLeader();
 
 	}
 
 	@Override
 	public void update(RobotRemovedEvent rem) {
 	    Player owner = rem.getSource().getOwner();
 	    boolean ended = false;
 	    if (!robotsCount.containsKey(owner)) {
 		log.warn("[update] Removed unknown robot");
 	    } else {
 		int old = robotsCount.get(owner);
 		robotsCount.put(owner, old - 1);
 		if (old == 1) {
 		    ended = playerDied(owner);
 		}
 		
 	    }
 	    if (!ended) {
 		findLeader();
 	    }
 	}
 
 	@Override
 	public void update(RobotMovedEvent mov) {
 	    // do nothing
 	}
 
 	@Override
 	public void tick(long cycles) {
 	    if (cycles >= GameSettings.getInstance().MAX_WORLD_AGE) {
 		// game over
 		log.info("[tick] Maximum age of the world reached. Declaring a draw");
 		declareDraw();
 	    }
 
 	}
 
     }
 
     private boolean playerDied(Player owner) {
 	boolean ended = checkEnd();
 	if (!ended) {
 	    // generate event
 	    eventDispatcher.fireEvent(new PlayerEliminatedEvent(this, owner));
 	}
 
 	return ended;
     }
 
     private void findLeader() {
 	List<Player> leader = new ArrayList<>();
 	int max = 0;
 	for (Entry<Player, Integer> entry : robotsCount.entrySet()) {
 	    if (entry.getValue() > max) {
 		leader.clear();
 		leader.add(entry.getKey());
		max = entry.getValue();
 	    } else if (entry.getValue() == max) {
 		leader.add(entry.getKey());
 	    }
 	}
 	// found leader, compare to old
 	if (leader.size() != currentLeaders.size()) {
 	    // change detected
 	    eventDispatcher.fireEvent(new LeaderChangedEvent(this, leader));
 	    currentLeaders = leader;
 	} else {
 	    leader.removeAll(currentLeaders);
 	    if (leader.size() > 0) {
 		// change detected
 		eventDispatcher.fireEvent(new LeaderChangedEvent(this, leader));
 		currentLeaders = leader;
 	    }
 	}
 
     }
 
     private void declareWinner(Player winner) {
 	log.info("[declareWinner] Found winner! {}", winner);
 	eventDispatcher.fireEvent(new ResultEvent(this, winner));
     }
 
     private void declareDraw() {
 	log.info("[declareDraw] No robots left. The game is a draw :S");
 	eventDispatcher.fireEvent(new ResultEvent(this, true));
     }
 
     private boolean checkEnd() {
 	boolean end = false;
 	if (robotsCount.size() > 1) { // if there are more than 1, otherwise it is "practice"
 	    Player sawOne = null;
 	    for (Entry<Player, Integer> entry : robotsCount.entrySet()) {
 		if (entry.getValue() > 0) {
 		    if (sawOne != null) {
 			// not end, stop
 			return false;
 		    }
 		    sawOne = entry.getKey();
 		}
 	    }
 	    // this is the end
 	    if (sawOne != null) {
 		declareWinner(sawOne);
 	    } else {
 		// draw
 		declareDraw();
 	    }
 	    end = true;
 	} else {
 	    // practice
 	    int left = robotsCount.values().iterator().next();
 
 	    if (left <= 0) {
 		// last robot in practice died
 		declareEnd();
 		end = true;
 	    }
 	}
 	return end;
     }
 
     private void declareEnd() {
 	log.debug("[declareEnd] End of practice session");
 	eventDispatcher.fireEvent(new ResultEvent(this, false));
     }
 
     /**
      * @return the receiver of external events
      */
     public EventsHandler getEventsReceiver() {
 	return eventHandler;
     }
 
     /**
      * Cleans the tracker
      */
     public void clean() {
 	eventDispatcher.removeListeners();
     }
     
     /**
      * @return the object in charge of events
      */
     public EventDispatcher<GameStatusListener> getEventHandler() {
 	SecurityManager sm = System.getSecurityManager();
 	if (sm != null) {
 	    sm.checkPermission(new GamePermission("eventsListener"));
 	}
 	return eventDispatcher;
     }
 }
