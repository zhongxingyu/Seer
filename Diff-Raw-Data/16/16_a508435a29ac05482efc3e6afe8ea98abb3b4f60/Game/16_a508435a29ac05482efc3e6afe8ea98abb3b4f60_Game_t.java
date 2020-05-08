 package gt.general;
 
 import static com.google.common.collect.Sets.*;
 import gt.general.character.Hero;
 import gt.general.character.Team;
 import gt.general.world.WorldInstance;
 import gt.plugin.meta.Hello;
 import gt.plugin.meta.MultiListener;
 
 import java.util.Set;
 import java.util.Vector;
 
 import org.bukkit.ChatColor;
 import org.bukkit.event.Listener;
 
 public abstract class Game implements Listener {
 	
 	/** The team playing this game */
 	private final Team team;
 	
 	/** The instance where the game is played */
 	private WorldInstance world;
 	
 	private Vector<Listener> listeners;
 	private Vector<Integer> tasks;
 	
 	private final Set<Hero> disconnectedHeros = newHashSet();
 	private boolean running;
 
 	private boolean cmdPause;
 	
 	/**
 	 * @param team The team playing this game
 	 */
 	public Game(final Team team) {
 		super();
 		this.team = team;
 		listeners = new Vector<Listener>();
 		tasks = new Vector<Integer>();
 		team.setGame(this);
 		
 		running = true;
 	}
 	
 	/**
 	 * saves hero for reconnect, removes him otherwise
 	 * @param hero the hero which disconnects
 	 */
 	public void disconnectHero(final Hero hero) {
 		disconnectedHeros.add(hero);
 
 		// first disconnect
 		if(disconnectedHeros.size() == 1) {
 			setRunning(false);
 		}
 	}	
 	/**
 	 * restores a previous disconnected Hero
 	 * @param hero Hero to be restored
 	 */
 	public void reconnectHero(final Hero hero) {		
 		disconnectedHeros.remove(hero);
 		
 		// last reconnect
 		if(disconnectedHeros.size() == 0) {
 			setRunning(true);
 		}	
 	}
 
 	
 	public void toggleForcePause() {
 		cmdPause = !cmdPause;
 		setRunning(running);
 	}
 	
 	/**
 	 * sets the running state and calls the appropriate handlers
 	 * 
 	 * @param running the running to set
 	 */
 	public void setRunning(final boolean running) {
 		this.running = running;
 		
 		if(running && !cmdPause) {
 			team.sendMessage(ChatColor.GREEN + "game resumed");
 			onResume();
 		} else {
 			team.sendMessage(ChatColor.RED + "game paused");
 			onPause();
 		}
 	}
 
 	public abstract void onPause();
 	
 	public abstract void onResume();
 	
 	/**
 	 * handles the victory or defeat of a game
 	 */
 	public abstract void onEnd();
 
 	/**
 	 * ensures there are no internal dependencies to prevent the game from garbage collection
 	 * e.g. removes related tasks from the scheduler
 	 */
 	public void dispose() {
 		
 		for (Hero hero : team.getPlayers()) {
			if(hero.hasActiveItem()) {
				hero.removeActiveItem();
			}
 		}
 		
 		for (Listener listener : listeners) {
 			MultiListener.unregisterListener(listener);			
 		}
 		
 		for (Integer taskId : tasks) {
 			Hello.cancelScheduledTask(taskId);
 		}
 		
 		getWorldInstance().dispose();
 		
 	}
 
 	/**
 	 * @return the team
 	 */
 	public Team getTeam() {
 		return team;
 	}
 	
 	/**
 	 * @return the world
 	 */
 	public WorldInstance getWorldInstance() {
 		return world;
 	}
 	
 	/**
 	 * @param world the WorldInstance to be set
 	 */
 	public void setWorldInstance(final WorldInstance world) {
 		this.world = world;
 	}
 	
 	public void registerListener(Listener listener) {
 		MultiListener.registerListener(listener);
 		listeners.add(listener);
 	}
 	
 	public void registerAsyncTask(Runnable task, int initial, int ticks) {
 		tasks.add(Hello.scheduleAsyncTask(task, initial, ticks));
 	}
 	
 	public void registerSyncTask(Runnable task, int initial, int ticks) {
 		tasks.add(Hello.scheduleSyncTask(task, initial, ticks));
 	}
 	
 	public void registerListeners(final Listener... listeners) {
 		for (Listener listener : listeners) {
 			registerListener(listener);
 		}
 	}
 
 	/**
 	 * @return the running
 	 */
 	public boolean isRunning() {
 		return running;
 	}
 	
 	public boolean isPlayedBy(final Hero hero) {
 		return getTeam().containsHero(hero);
 	}
 
 }
 
