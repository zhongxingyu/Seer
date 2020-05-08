 package republicaEternityEventIII.republica.devteam;
 
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.CreatureSpawner;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.util.Vector;
 
 public class Ziminiar {
 	
 	private final int COOLDOWN_IN_SECONDS = 3;
 	public static boolean cooldownEnabled = false;
 	private int health;
 	private String username;
 	private EternityMain main;
 	private long lastSpellTime = 0;
 	private int damages = 0;
 	
 	private Player getPlayer() {
 		return main.getPlayer(username);
 	}
 	
 	public Ziminiar(Player ziminiar, EternityMain main){
 		this.main = main;
 		username = ziminiar.getName();
 		ziminiar.setDisplayName(ChatColor.BLACK + "Ziminiar" + ChatColor.RESET);
 		Bukkit.broadcastMessage(ChatColor.BLACK + "Ziminiar" + ChatColor.WHITE + " has risen!");
 		SoundEffectsManager.playSpawnSound(ziminiar.getLocation());
 		remakeZiminiar(); //Edits the player's attributes.
 	}
 	
 	private void remakeZiminiar(){
 		changeZiminiarNameplate();
 		DisguiseCraftIntermediary.dragonify(username);
 		getPlayer().setAllowFlight(true);
 		ziminiarHealth();
 		ziminiarWeapons();
 		ziminiarArmor();
 	}
 	
 	private void changeZiminiarNameplate(){
 		//Perolan's NBT code here
 	}
 	
 	private void ziminiarHealth(){
 		health = 100;
 	}
 	
 	private void rename(ItemStack stack, String name) {
 		ItemMeta meta = stack.getItemMeta();
 		meta.setDisplayName(name);
 		stack.setItemMeta(meta);
 	}
 	
 	private void ziminiarWeapons(){
 		//TODO: Give Ziminiar his actual weapons
 		ItemStack lightningStrike = new ItemStack(Material.BLAZE_ROD);
 		rename(lightningStrike, "Lightning Strike");
 		ItemStack arrowBarrage = new ItemStack(Material.ARROW);
 		rename(arrowBarrage, "Arrow Barrage");
 		ItemStack explosion = new ItemStack(Material.SULPHUR);
 		rename(explosion, "Explosion");
 		ItemStack spawnMobSpawners = new ItemStack(Material.MOB_SPAWNER);
 		rename(spawnMobSpawners, "Spawn Mob Spawners");
 		ItemStack spawnMinions = new ItemStack(Material.MONSTER_EGG);
 		rename(spawnMinions, "Spawn Minions");
 		ItemStack dropTNT = new ItemStack(Material.TNT);
 		rename(dropTNT, "Drop TNT");
 		ItemStack shootMoltenCharge = new ItemStack(Material.BLAZE_POWDER);
 		rename(shootMoltenCharge, "Shoot Molten Charge");
 		Inventory inv = getPlayer().getInventory();
 		inv.addItem(lightningStrike, arrowBarrage, explosion, spawnMobSpawners, spawnMinions, dropTNT, shootMoltenCharge);
 	}
 	
 	private void ziminiarArmor(){
 		//TODO: Give Ziminiar his armor and equip it
 	}
 	
 	private void damageZiminiarHealth(int i){
 		health -= i;
		if (health < 0) {
			health = 0;
		}
 		damages++;
 		
 		if(damages % 5 == 0){
 			Bukkit.broadcastMessage(ChatColor.BLACK + "Ziminiar" + ChatColor.RED + " has " + ChatColor.BLUE + health + ChatColor.RED + " remaining");
 			//Broadcasts when Ziminiar has been taking down 25 health
 		}
 	}
 	
 	public void ZiminiarHit(Player pl, int damage){
 		damageZiminiarHealth(damage);
 		//I might change this later...
 		
 		MagicalStorage.incrementPlayerScore(pl.getName(), 1);
 	}
 	
 	public void checkForSpells(PlayerInteractEvent e) {
 		Player player = e.getPlayer();
 		if (!player.getName().equals(username)) return;
 		// and now we know player is Ziminiar
 		if (!(e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_AIR))) return;
 		// and now we know Ziminiar clicked air (as opposed to a block)
 		if (cooldownEnabled) {
 			if (lastSpellTime + COOLDOWN_IN_SECONDS * 1000 > System.currentTimeMillis()) {
 				return;
 			}
 		}
 		Material heldItemType = player.getItemInHand().getType();
 		switch (heldItemType) {
 		case BLAZE_ROD:
 			strikeLightning();
 			lastSpellTime = System.currentTimeMillis();
 			break;
 		case ARROW:
 			fireArrowBarrage();
 			lastSpellTime = System.currentTimeMillis();
 			break;
 		case SULPHUR:
 			explode();
 			lastSpellTime = System.currentTimeMillis();
 			break;
 		case MOB_SPAWNER:
 			placeMobSpawner();
 			lastSpellTime = System.currentTimeMillis();
 			break;
 		case MONSTER_EGG:
 			spawnMinions();
 			lastSpellTime = System.currentTimeMillis();
 			break;
 		case TNT:
 			primeTNT();
 			lastSpellTime = System.currentTimeMillis();
 			break;
 		case BLAZE_POWDER:
 			shootMoltenCharge();
 			lastSpellTime = System.currentTimeMillis();
 			break;
 		default:
 			break;
 		}
 	}
 
 	private void shootMoltenCharge() {
 		// where is the player's head?
 		Location head = getPlayer().getEyeLocation();
 		// which direction is the player looking?
 		Vector direction = head.getDirection();
 		// where should the fireball start (more or less)?
 		Location fireballStart = head.clone();
 		fireballStart.add(direction.clone().multiply(3));
 		// spawn the fireball
 		World world = fireballStart.getWorld();
 		Entity fireball = world.spawnEntity(fireballStart, EntityType.FIREBALL);
 		fireball.setVelocity(direction);
 	}
 
 	private void primeTNT() {
 		Location place = getPlayer().getLocation();
 		World world = place.getWorld();
 		world.spawnEntity(place, EntityType.PRIMED_TNT);
 	}
 
 	private void spawnMinions() {
 		Random rand = new Random();
 		// spawn 2 to 4 zombies
 		int minionCount = rand.nextInt(3) + 2;
 		Location target = getPlayer().getTargetBlock(null, 100).getLocation().add(0, 1, 0);
 		World world = target.getWorld();
 		for (int i = 0; i < minionCount; i++) {
 			Location newSpot = target.clone().add(4 * rand.nextDouble() - 2, 0, 4 * rand.nextDouble() - 2);
 			world.spawnEntity(newSpot, EntityType.ZOMBIE);
 		}
 	}
 
 	private void placeMobSpawner() {
 		Location target = getPlayer().getTargetBlock(null, 100).getLocation().add(0, 1, 0);
 		World world = target.getWorld();
 		Block newSpawner = world.getBlockAt(target);
 		newSpawner.setType(Material.MOB_SPAWNER);
 		CreatureSpawner spawnerData = (CreatureSpawner) newSpawner.getState();
 		spawnerData.setSpawnedType(EntityType.PIG_ZOMBIE);
 	}
 
 	private void explode() {
 		Location target = getPlayer().getTargetBlock(null, 100).getLocation();
 		target.getWorld().createExplosion(target, 2f);
 	}
 
 	private void strikeLightning() {
 		Location target = getPlayer().getTargetBlock(null, 100).getLocation();
 		target.getWorld().strikeLightning(target);
 	}
 
 	private void fireArrowBarrage() {
 		// Shoot from 5 to 14 arrows.
 		Random rand = new Random();
 		int arrowCount = rand.nextInt(10) + 5;
 		for (int i = 0; i < arrowCount; i++) {
 			// where is the player's head?
 			Location head = getPlayer().getEyeLocation();
 			// which direction is the player looking?
 			Vector direction = head.getDirection();
 			// where should the arrow start (more or less)?
 			Location arrowStart = head.clone();
 			arrowStart.add(direction.clone().multiply(3));
 			// offset the arrow by a random amount
 			arrowStart.add(rand.nextDouble() - 0.5, rand.nextDouble() - 0.5, rand.nextDouble() - 0.5);
 			// spawn the arrow
 			World world = arrowStart.getWorld();
 			world.spawnArrow(arrowStart, direction, rand.nextFloat() * 0.1f + 1.5f, 12);
 		}
 	}
 
 	public int getHealth() {
 		return health;
 	}
 }
