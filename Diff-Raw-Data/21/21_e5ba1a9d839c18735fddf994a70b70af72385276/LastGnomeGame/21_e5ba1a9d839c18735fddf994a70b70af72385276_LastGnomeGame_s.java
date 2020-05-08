 package gt.lastgnome;
 
 import gt.general.Game;
 import gt.general.Hero;
 import gt.general.HeroManager;
 import gt.general.Team;
 import gt.general.aura.Effect;
 import gt.general.aura.GnomeSlowEffect;
 
 import java.util.Iterator;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 
 /**
  * Game Controller for a Last-Gnome-Scenario
  */
 public class LastGnomeGame implements Listener, Game {
 
 	private final Team team;
 	private final GnomeItem gnome;
 
 	/** so that e.g. Zombies know who the Gnome-Bearer is */
 	private Hero gnomeBearer;
 
 	/**
 	 * initiates a new Last Gnome Game
 	 *
 	 * @param team the Team playing the game
 	 */
 	public LastGnomeGame(final Team team) {
 		this.team = team;
 		gnome = new GnomeItem();
 	}
 
 	/**
 	 * initiates a new Last Gome Game with an initial GnomeBearer
 	 * @param team the Team playing the game
 	 * @param initialBearer the hero bearing the gnome from the start
 	 */
 	public LastGnomeGame(final Team team, final Hero initialBearer) {
 		this(team);
 		
 		initialBearer.setActiveItem(gnome);
 		setGnomeBearer(initialBearer);
 	}
 	
 	/**
 	 * handles passing of the gnome to another player, as triggered by minecraft
 	 *
 	 * @param event fired by minecraft
 	 */
 	@EventHandler
 	public void handleGnomPassing(final PlayerInteractEntityEvent event) {
 
 		System.out.println(gnomeBearer.getPlayer());
 		System.out.println(event.getPlayer());
 		
 		if (!gnomeBearer.getPlayer().equals(event.getPlayer())) {
 			return;
 		}
 
 		Entity target = event.getRightClicked();
 		
 		if (target instanceof Player) {
 			giveGnomeTo(HeroManager.getHero((Player) target));
 		}
 	}
 
 	/**
 	 * <b>WARNING</b> does not perform any checks
 	 *
 	 * @param newBearer the new gnomeBearer
 	 */
 	void giveGnomeTo(final Hero newBearer) {
 		
 		// If Player does not belong to the Team, stop here.
 		if (team.isMember(newBearer)
 				&& newBearer.canRecieveItem()) {
 			
 			Hero oldBearer = gnomeBearer;			
 
 			// pass the gnome
 			oldBearer.transferActiveItem(newBearer);
			
			
 			setGnomeBearer(newBearer);
 			
			// remove effects
			for (Iterator<Effect> it = oldBearer.getEffects().iterator(); it.hasNext(); ) {
		        if (it instanceof GnomeSlowEffect) {
 		            it.remove();
 		        }
			}			
			
			
 			oldBearer.removeEffect(gnome.getGnomeEffect());
 		}
 	}
 
 	/**
 	 * <b>WARNING</b> does not perform any checks
 	 *
 	 * @param newBearer
 	 *            the new gnome bearer
 	 */
 	void setGnomeBearer(final Hero newBearer) {
 		newBearer.addEffect(gnome.getGnomeEffect());
 		gnome.getGnomeAura().setOwner(newBearer);
 		
 		gnomeBearer = newBearer;
 	}
 
 	/**
 	 * @return the gnome
 	 */
 	public GnomeItem getGnome() {
 		return gnome;
 	}
 
 	/**
 	 * @return the current gnome bearer
 	 */
 	public Hero getGnomeBearer() {
 		return gnomeBearer;
 	}
 }
