 package gt.general.character;
 
 import gt.general.Game;
 import gt.general.PortableItem;
 import gt.general.gui.HeroGui;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 /**
  * Our backend and wrapper for a Minecraft-Player
  */
 public class Hero extends Character{
 
 	/** unbuffed default value **/
 	public static final double DEFAULT_HERO_SPEED = 1.0;
 	/**Team the hero belongs to*/
 	private Team team;
 
 	/**associated Minecraft-Player*/
 	private Player player;
 	/**Item in hand*/
 	private PortableItem activeItem;
 	
 	/**Passiv-Slot for tools*/
 	//private PortableItem passiveItem;
 
 	private HeroGui gui;
 	
 	public enum Notification {
 		INVENTORY,
 		ATTRIBUTES,
 		UNSPECIFIED
 	}
 	
 	private final Set<HeroObserver> observers;
 	
 	/**
 	 * @param player the player to be wrapped
 	 */
 	public Hero(final Player player) {
 		super();
 
 		setAttribute(CharacterAttributes.SPEED, DEFAULT_HERO_SPEED);
 
 		team = Team.NOTEAM;
 		this.player = player;
 		
 		observers = new HashSet<HeroObserver>();
 	}
 
 	/**
 	 * @return the wrapped Player
 	 */
 	public Player getPlayer() {
 		return player;
 	}
 
 	/**
 	 * @return the Spout version of the wrapped Player
 	 */
 	public SpoutPlayer getSpoutPlayer() {
 		return SpoutManager.getPlayer(getPlayer());
 	}
 
 	@Override
 	public Location getLocation() {
 		return player.getLocation();
 	}
 
 	/**
 	 * @return the team
 	 */
 	public Team getTeam() {
 		return team;
 	}
 
 	/**
 	 * @param team the team to set
 	 */
 	public void setTeam(final Team team) {
 		this.team = team;
 	}
 
 	/**
 	 * @return true if hero has a team
 	 */
 	public boolean inTeam() {
 		return team != Team.NOTEAM;
 	}
 	
 	/**
 	 * @return the current Item in the active slot
 	 */
 	public PortableItem getActiveItem() {
 		return activeItem;
 	}
 
 	/**
 	 * @return the current Item in the passive slot
 	 */
 //	public PortableItem getPassivItem() {
 //		return passiveItem;
 //	}
 
 	/**
 	 * @return if true, the Hero can pick up another item
 	 */
 	public boolean canRecieveItem() {
		if (activeItem == null) {
 			return true;
 		}
 
 //		if (activeItem.isTool() && passiveItem == null) {
 //			return true;
 //		}
 
 		if (activeItem.isDropable()) {
 			return true;
 		}
 
 		return false;
 	}
 	
 	/**
 	 * @return true if the item can be transfered to another player
 	 */
 	public boolean canTransferItem() {
 		return activeItem.isTransferable();
 	}
 
 	/**
 	 * picks up a new item
 	 *
 	 * @param item the new active item
 	 */
 	public void setActiveItem(final PortableItem item) {
 		if (activeItem == null) {
 			innerSetActiveItem(item);
 			return;
 		}
 
 //		if (activeItem.isTool() && passiveItem == null) {
 //			passiveItem = activeItem;
 //			innerSetActiveItem(item);
 //			return;
 //		}
 
 		if (activeItem.isDropable()) {
 			dropActiveItem();
 			innerSetActiveItem(item);
 			return;
 		}
 		
 //		System.out.println("active: " +activeItem+" passive: "+passiveItem);
 		throw new RuntimeException();
 	}
 
 	/**
 	 * @param item the new active item of the Hero
 	 */
 	private void innerSetActiveItem(final PortableItem item) {
 		activeItem = item;
 		
 		activeItem.onAttachHero(this);
 		notifyChanged(Notification.INVENTORY);
 	}
 	
 	
 	/**
 	 * @return the old active item of the Hero
 	 */
 	public PortableItem removeActiveItem() {
 		PortableItem toRemove = activeItem;
 	
 		activeItem = null;
 		
 		toRemove.onDetachHero(this);
 		notifyChanged(Notification.INVENTORY);
 		
 //		equipPassiveItem();
 		
 		return toRemove;
 	}
 
 	/**
 	 * equip the passive item in the active slot
 	 */
 //	private void equipPassiveItem() {
 //		if(activeItem==null
 //				&& passiveItem!=null) {
 //			activeItem = passiveItem;
 //			passiveItem = null;
 //			
 //			notifyChanged(Notification.INVENTORY);
 //		}
 //			
 //	}
 
 	/**
 	 * transfers the current active item into another inventory
 	 *
 	 * @param target where to put the inventory
 	 */
 	public void transferActiveItem(final Hero target) {
 		if (activeItem.isTransferable() && target.canRecieveItem()) {
 			target.setActiveItem(removeActiveItem());
 		}
 	}
 
 	/**
 	 * Method to drop the active Item
 	 * TODO real dropping
 	 */
 	public void dropActiveItem() {
 		if (activeItemDropable()) {
 			
 			activeItem.onDetachHero(this);
 			
 			//creates a new Item, with ItemStack where player stands
 			World world = getPlayer().getWorld();
 			ItemStack item = activeItem.getItemStack();
 			world.dropItem(getLocation(), item);
 			//remove Item from MC-Player's inventory
 			getPlayer().setItemInHand(null);
 			activeItem = null;
 			
 			// TODO event!
 			
 		}
 		// TODO debug in else branch
 	}
 
 	/**
 	 * @return true if the currently active item is droppable
 	 */
 	public boolean activeItemDropable() {
 		return activeItem.isDropable();
 	}
 
 	
 	@Override
 	protected void calculateAttributes() {
 		super.calculateAttributes();
 		notifyChanged(Notification.ATTRIBUTES);
 	}
 	
 	@Override
 	public void applyAttributes() {
 
 		SpoutPlayer sPlayer = getSpoutPlayer();
 
 		//sPlayer.sendMessage("speed" + getCurrentSpeed());
 
 		sPlayer.setWalkingMultiplier(getCurrentSpeed());
 		sPlayer.setJumpingMultiplier(getAttribute(CharacterAttributes.JUMPMULTIPLIER));
 	}
 
 
 	/**
 	 * @param gui the gui associated with this Hero
 	 */
 	public void setGui(final HeroGui gui) {
 		this.gui = gui;
 	}
 	
 	/**
 	 * @return the gui associated with this Hero
 	 */
 	public HeroGui getGui() {
 		return gui;
 	}
 	
 	/**
 	 * @param heroObserver the observer to be added
 	 */
 	public void addObserver(final HeroObserver heroObserver) {
 		observers.add(heroObserver);
 	}
 	
 	/**
 	 * @param heroObserver the observer to be removed
 	 */
 	public void removeObserver(final HeroObserver heroObserver) {
 		observers.remove(heroObserver);
 	}
 	
 	/**
 	 * @param notification the type of change which happened
 	 */
 	public void notifyChanged(final Notification notification) {
 		for(HeroObserver observer : observers) {
 			observer.update(this, notification);
 		}
 	}
 
 	/**
 	 * @param player bukkit player
 	 */
 	public void setPlayer(final Player player) {
 		this.player = player;
 	}
 	
 	/**
 	 * @return hero of the game, if he is ingame
 	 */
 	public Game getGame() {
 		
 		if(! inGame()) {
 			return null;
 		} else {
 			return getTeam().getGame();
 		}
 	}
 	
 	/**
 	 * @return true if hero is part of a game
 	 */
 	public boolean inGame() {
 		
 		return inTeam() && getTeam().inGame();
 
 	}
 
 }
