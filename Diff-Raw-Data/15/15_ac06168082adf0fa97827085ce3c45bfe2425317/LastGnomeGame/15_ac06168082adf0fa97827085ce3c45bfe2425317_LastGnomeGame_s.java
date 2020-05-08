 package gt.lastgnome.game;
 
 import static com.google.common.collect.Lists.*;
 import static org.bukkit.ChatColor.*;
 import gt.general.GameManager;
 import gt.general.RespawnManager;
 import gt.general.Vote;
 import gt.general.character.Hero;
 import gt.general.character.HeroManager;
 import gt.general.character.Team;
 import gt.general.character.ZombieManager;
 import gt.general.gui.GameScoreOverlay;
 import gt.general.gui.GuiElementType;
 import gt.lastgnome.GnomeItem;
 import gt.lastgnome.gui.SpeedBar;
 import gt.lastgnome.scoring.NullHighscoreEntry;
 import gt.lastgnome.scoring.ScoreManager;
 import gt.plugin.helloworld.HelloWorld;
 import gt.plugin.meta.Hello;
 
 import org.bukkit.World;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 
 /**
  * Game Controller for a Last-Gnome-Scenario
  */
 public class LastGnomeGame extends AbstractLastGnomeGame implements Listener{
 	
 	private final GnomeItem gnome;
 
 	
 	/** so that e.g. Zombies know who the Gnome-Bearer is */
 	private Hero gnomeBearer;
 	
 	private ScoreManager scoreManager;
 	
 	private RespawnManager respawnManager;
 
 	private ZombieManager zombieManager;
 	
 	/**
 	 * initiates a new Last Gnome Game
 	 *
 	 * @param team the Team playing the game
 	 */
 	public LastGnomeGame(final Team team) {
 		super(team);
 		
 		gnome = new GnomeItem(this);
 	}
 
 	
 	public ScoreManager getScoreManager() {
 		return scoreManager;
 	}
 
 	public void setRespawnManager(RespawnManager respawnManager) {
 		this.respawnManager = respawnManager;
 	}
 
 
 	public void setScoreManager(ScoreManager scoreManager) {
 		this.scoreManager = scoreManager;
 	}
 
 	/**
 	 * TODO downgrade? is here the right place for this stuff?
 	 * 		better in game manager?
 	 * 
 	 * @param hero whose gui to be upgraded
 	 */
 	public void upgradeGui(final Hero hero) {
 		SpeedBar speedBar = new SpeedBar();
 		hero.getGui().addGuiElement(GuiElementType.SPEEDBAR, speedBar);
 	}
 	
 	/**
 	 * handles passing of items to another player
 	 *
 	 * @param event player interact (right click on entity)
 	 */
 	@EventHandler
 	public void handleItemTransfering(final PlayerInteractEntityEvent event) {
 		Entity target = event.getRightClicked();
 		if (target instanceof Player) {	
 			Hero sender = HeroManager.getHero(event.getPlayer());
 			Hero receiver = HeroManager.getHero((Player) target);
 			
 			transferItem(sender, receiver);
 		}
 	}
 	
 	/**
 	 * initiates an item transfer from sender to receiver if possible
 	 * 
 	 * @param sender
 	 *            gives the item
 	 * @param receiver
 	 *            gets the item
 	 */
 	void transferItem(final Hero sender, final Hero receiver) {
 		// If Player does not belong to the Team, stop here.
 		if (getTeam().isMember(receiver) && receiver.canRecieveItem()
 				&& sender.canTransferItem()) {
 
 			System.out.println(sender.getPlayer().getName() + " gave an item to " + receiver.getPlayer().getName());
 			
 			sender.transferActiveItem(receiver);
 			
 			if (sender.equals(gnomeBearer)) {
 				setGnomeBearer(receiver);
 			}
 		}
 	}
 
 	/**
 	 * @param newBearer
 	 *            the new gnome bearer
 	 */
 	void setGnomeBearer(final Hero newBearer) {
 		gnomeBearer = newBearer;
 		getZombieManager().setTarget(newBearer);
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
 
 	@Override
 	public void dispose() {
 		getZombieManager().cleanup();
 
 		super.dispose();
 		
 		if (gnomeBearer != null) {
 			gnomeBearer.removeActiveItem();
 		}
 		
 		for(Hero hero : getTeam().getPlayers()) {
 			hero.getGui().removeGuiElement(GuiElementType.SPEEDBAR);
 		}
 		
 		respawnManager.dispose();
 		
 		//MultiListener.unregisterListener(scoreManager);
 	}
 	
 	@Override
 	public void finalize() {
 		System.out.println("finalizing a gnome game");
 	}
 	
 	@Override
 	public void onEnd() {
 		dispose();
 		
 		Vote restartVote = new Vote(getTeam().getPlayers().size(), 1) {
 
 			@Override
 			public void onAccept() {
 				System.out.println("restarting game");
 				
 				for(Hero h : getTeam().getPlayers()) {
 					h.getGui().closePopup();
 				}
 				
 				
 				Hero randomHero = newArrayList(getTeam().getPlayers()).get(0);
 				// XXX quite ugly
 				randomHero.getPlayer().performCommand("gg force");
 				
 				
 			}
 
 			@Override
 			public void onReject() {
 				System.out.println("back to the lobby");
				
				
 				//TODO: maybe put that in a RespawnManager.dispose() ?
 				World startWorld = Hello.getPlugin().getServer().getWorld("world");
 				for (Hero hero : getTeam().getPlayers()) {
 					hero.getPlayer().setBedSpawnLocation(startWorld.getSpawnLocation());
 					hero.getPlayer().teleport(startWorld.getSpawnLocation());
 				}
 				
 				getTeam().dispose();
 			}
 			
 		};
 		
 		for(Hero hero : getTeam().getPlayers()) {
 			hero.getGui().popup(new GameScoreOverlay(new NullHighscoreEntry(), hero, restartVote));
 		}
 		
 		
 	}
 	
 	/**
 	 * Detect Player Death
 	 * @param event player dies
 	 */
 	@EventHandler
 	public void onPlayerDeath(final PlayerDeathEvent event) {
 		Player player = event.getEntity();
 		if(gnomeBearer != null && player.equals(gnomeBearer.getPlayer())) {
 			scoreManager.stopMonitoring();			
 			lose();
 		}
 	}
 	
 
 	private void lose() {
 		for (Hero hero: getTeam().getPlayers()) {
 			hero.getPlayer().sendMessage(RED + "The Gnome has been eaten! You lost!");			
 		}		
 		onEnd();
 	}
 	
 	private void win() {
 		for (Hero hero: getTeam().getPlayers()) {
 			hero.getPlayer().sendMessage(GREEN + "The last Gnome on earth has been saved!");			
 		}
 		onEnd();	   	
 	}
 	
 	/**
 	 * handles gnome transfer from Start Socket to Player
 	 * @param player who clicked the socket
 	 */
 	@Override
 	public void onStartSocketInteract(final Player player) {
 		Hero hero = HeroManager.getHero(player);
 		
 		if (gnomeBearer == null && hero.canRecieveItem()) {
 
 			hero.setActiveItem(gnome);
 			setGnomeBearer(hero);
 			
 			scoreManager.startMonitoring();
 			
 			player.sendMessage(GREEN + "You obtained the last Gnome on earth");
 			
 		} else {
 			player.sendMessage(YELLOW + "nothing happens..");
 		}
 	}
 
 
 	/**
 	 * handles gnome transfer from Player to End Socket
 	 * @param player who klicked the socket
 	 */
 	@Override
 	public void onEndSocketInteract(final Player player) {
 		Hero hero = HeroManager.getHero(player);
 		
 		if(hero.equals(gnomeBearer)) {	
 			
 			hero.removeActiveItem();
 			setGnomeBearer(null);
 
 			scoreManager.stopMonitoring();
 		
 			win();
 		} else {
 			player.sendMessage(YELLOW + "The mighty Gnome Socket demands the Gnome!");
 		}
 	}
 
 
 	/**
 	 * @return the zombieManager
 	 */
 	public ZombieManager getZombieManager() {
 		return zombieManager;
 	}
 
 
 	/**
 	 * @param zombieManager the zombieManager to set
 	 */
 	public void setZombieManager(final ZombieManager zombieManager) {
 		this.zombieManager = zombieManager;
 	}
 
 
 	@Override
 	public void onPause() {
 		getZombieManager().freezeAllZombies();
 		getTeam().freezeAllHeros();
 	}
 
 
 	@Override
 	public void onResume() {
 		getZombieManager().unFreezeAllZombies();
 		getTeam().unfreezeAllHeros();
 	}
 
 }
