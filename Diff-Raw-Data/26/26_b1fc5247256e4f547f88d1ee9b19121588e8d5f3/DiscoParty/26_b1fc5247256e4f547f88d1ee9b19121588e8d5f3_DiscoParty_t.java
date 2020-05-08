 package ca.gibstick.discosheep;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 import org.bukkit.Color;
 import org.bukkit.DyeColor;
 import org.bukkit.Location;
 import org.bukkit.Sound;
 import org.bukkit.World;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Firework;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Sheep;
 import org.bukkit.FireworkEffect;
 import org.bukkit.FireworkEffect.Builder;
 import org.bukkit.entity.Entity;
 import org.bukkit.event.HandlerList;
 import org.bukkit.inventory.meta.FireworkMeta;
 import org.bukkit.scheduler.BukkitRunnable;
 import org.bukkit.util.Vector;
 
 public class DiscoParty {
 
 	private DiscoSheep parent;
 	private Player player;
 	private ArrayList<Sheep> sheepList = new ArrayList<Sheep>();
 	private ArrayList<Entity> guestList = new ArrayList<Entity>();
 	static int defaultDuration = 300; // ticks for entire party
 	static int defaultPeriod = 10; // ticks per state change
 	static int defaultRadius = 5;
 	static int defaultSheep = 10;
 	static float defaultSheepJump = 0.35f;
 	static int maxDuration = 2400; // 120 seconds
 	static int maxSheep = 100;
 	static int maxRadius = 100;
 	static int minPeriod = 5;	// 0.25 seconds
 	static int maxPeriod = 40;	// 2.0 seconds
 	private HashMap<String, Integer> guestNumbers = new HashMap<String, Integer>();
 	private static HashMap<String, Integer> defaultGuestNumbers = new HashMap<String, Integer>();
 	private static HashMap<String, Integer> maxGuestNumbers = new HashMap<String, Integer>();
 	private boolean doFireworks = false;
 	private boolean doJump = true;
 	private boolean doLightning = false;
 	private int duration, period, radius, sheep;
 	private int state = 0; // basically our own tick system
 	private DiscoUpdater updater;
 	private static final DyeColor[] discoColours = {
 		DyeColor.RED,
 		DyeColor.ORANGE,
 		DyeColor.YELLOW,
 		DyeColor.GREEN,
 		DyeColor.BLUE,
 		DyeColor.LIGHT_BLUE,
 		DyeColor.PINK,
 		DyeColor.MAGENTA,
 		DyeColor.LIME,
 		DyeColor.CYAN,
 		DyeColor.PURPLE,
 		DyeColor.BLACK,
 		DyeColor.WHITE
 	};
	private static final float[] pentatonicNotes = {
		1.0f,
		1.125f,
		1.25f,
		1.5f,
		1.667f,
		2.0f
	};
 	private Random r=new Random();
 	private PartyEvents partyEvents;
 
 	public DiscoParty(DiscoSheep parent, Player player) {
 		this(parent);
 		this.player = player;
 	}
 
 	public DiscoParty(DiscoSheep parent) {
 		this.parent = parent;
 		this.duration = DiscoParty.defaultDuration;
 		this.period = DiscoParty.defaultPeriod;
 		this.radius = DiscoParty.defaultRadius;
 		this.sheep = DiscoParty.defaultSheep;
 		this.guestNumbers = new HashMap<String, Integer>(DiscoParty.defaultGuestNumbers);
 		r = new Random();
 	}
 
 	// copy but with new player
 	// used for /ds other and /ds all
 	public DiscoParty clone(Player player) {
 		DiscoParty newParty;
 		newParty = new DiscoParty(this.parent, player);
 		newParty.doFireworks = this.doFireworks;
 		newParty.duration = this.duration;
 		newParty.period = this.period;
 		newParty.radius = this.radius;
 		newParty.sheep = this.sheep;
 		newParty.doLightning = this.doLightning;
 		newParty.guestNumbers = this.getGuestNumbers();
 		return newParty;
 	}
 
 	ArrayList<Sheep> getSheepList() {
 		return sheepList;
 	}
 
 	ArrayList<Entity> getGuestList() {
 		return guestList;
 	}
 
 	public static HashMap<String, Integer> getDefaultGuestNumbers() {
 		return defaultGuestNumbers;
 	}
 
 	public HashMap<String, Integer> getGuestNumbers() {
 		return guestNumbers;
 	}
 
 	public static HashMap<String, Integer> getMaxGuestNumbers() {
 		return maxGuestNumbers;
 	}
 
 	public int getSheep() {
 		return this.sheep;
 	}
 
 	public DiscoParty setPlayer(Player player) {
 		if (player != null) {
 			this.player = player;
 			return this;
 		} else {
 			throw new NullPointerException();
 		}
 	}
 
 	public DiscoParty setDuration(int duration) throws IllegalArgumentException {
 		if (duration <= DiscoParty.maxDuration && duration > 0) {
 			this.duration = duration;
 			return this;
 		} else {
 			throw new IllegalArgumentException();
 		}
 	}
 
 	public DiscoParty setPeriod(int period) throws IllegalArgumentException {
 		if (period >= DiscoParty.minPeriod && period <= DiscoParty.maxPeriod) {
 			this.period = period;
 			return this;
 		} else {
 			throw new IllegalArgumentException();
 		}
 	}
 
 	public DiscoParty setRadius(int radius) throws IllegalArgumentException {
 		if (radius <= DiscoParty.maxRadius && radius > 0) {
 			this.radius = radius;
 			return this;
 		} else {
 			throw new IllegalArgumentException();
 		}
 	}
 
 	public DiscoParty setDenseRadius(int sheepNo) throws IllegalArgumentException {
 		Integer rand = (int) Math.floor(Math.sqrt(sheep / Math.PI));
 		if (rand > DiscoParty.maxRadius) {
 			rand = DiscoParty.maxRadius;
 		}
 		if (rand < 1) {
 			rand = 1;
 		}
 
 		this.setRadius(rand);
 		return this;
 	}
 
 	public DiscoParty setSheep(int sheep) throws IllegalArgumentException {
 		if (sheep <= DiscoParty.maxSheep && sheep > 0) {
 			this.sheep = sheep;
 			return this;
 		} else {
 			throw new IllegalArgumentException();
 		}
 	}
 
 	public DiscoParty setDoFireworks(boolean doFireworks) {
 		this.doFireworks = doFireworks;
 		return this;
 	}
 	
 	public DiscoParty setDoLightning(boolean doLightning) {
 		this.doLightning = doLightning;
 		return this;
 	}
 
 	public DiscoParty setGuestNumber(String key, int n) throws IllegalArgumentException {
 		if (getMaxGuestNumbers().containsKey(key.toUpperCase())) {
 			if (n <= getMaxGuestNumbers().get(key.toUpperCase()) && n >= 0) { // so that /ds defaults can take 0 as arg
 				getGuestNumbers().put(key, n);
 
 				return this;
 			}
 		}
 		throw new IllegalArgumentException();
 	}
 
 	// use current settings as new defaults
 	public DiscoParty setDefaultsFromCurrent() {
 		DiscoParty.defaultDuration = this.duration;
 		DiscoParty.defaultPeriod = this.period;
 		DiscoParty.defaultRadius = this.radius;
 		DiscoParty.defaultSheep = this.sheep;
 		DiscoParty.defaultGuestNumbers = new HashMap<String, Integer>(this.getGuestNumbers());
 		return this;
 	}
 
 	Location getRandomSpawnLocation(double x, double z, World world, int spawnRadius) {
 		Location loc;
 
 		double y;
 
 		/* random point on circle with polar coordinates
 		 * random number must be square rooted to obtain uniform distribution
 		 * otherwise the sheep are biased toward the centre */
 		double rand = Math.sqrt(r.nextDouble()) * spawnRadius;
 		double azimuth = r.nextDouble() * 2 * Math.PI; // radians
 		x += rand * Math.cos(azimuth);
 		z += rand * Math.sin(azimuth);
 		y = this.player.getLocation().getY();
 
 		loc = new Location(world, x, y, z);
 		loc.setPitch(r.nextFloat() * 360 - 180);
 		loc.setYaw(0);
 
 		return loc;
 	}
 
 	// Spawn some number of guests next to given player
 	void spawnAll(int sheep, int spawnRadius) {
 		Location loc;
 		World world = player.getWorld();
 
 
 		double x = player.getLocation().getX();
 		double z = player.getLocation().getZ();
 		for (int i = 0; i < sheep; i++) {
 			loc = getRandomSpawnLocation(x, z, world, spawnRadius);
 			spawnSheep(world, loc);
 		}
 
 		// loop through hashmap of other guests and spawn accordingly
 		for (Map.Entry entry : guestNumbers.entrySet()) {
			EntityType ent = EntityType.valueOf((String) entry.getKey());
 			int num = (Integer) entry.getValue();
 
 			for (int i = 0; i < num; i++) {
 				loc = getRandomSpawnLocation(x, z, world, spawnRadius);
 				spawnGuest(world, loc, ent);
 			}
 		}
 	}
 
 	void spawnSheep(World world, Location loc) {
 		Sheep newSheep = (Sheep) world.spawnEntity(loc, EntityType.SHEEP);
 		newSheep.setColor(discoColours[(r.nextInt(discoColours.length))]);
 		newSheep.setBreed(false);	// this prevents breeding - no event listener required
 		newSheep.teleport(loc);	// teleport is needed to set orientation
 		getSheepList().add(newSheep);
 		if (doLightning) {
 			world.strikeLightningEffect(loc);
 		}
 	}
 
 	void spawnGuest(World world, Location loc, EntityType type) {
 		Entity newGuest = loc.getWorld().spawnEntity(loc, type);
 		getGuestList().add(newGuest);
 		if (doLightning) {
 			world.strikeLightningEffect(loc);
 		}
 	}
 
 	// Mark all guests for removal, then clear the array
 	void removeAll() {
 		for (Sheep sheeple : getSheepList()) {
 			sheeple.remove();
 		}
 		for (Entity guest : getGuestList()) {
 			guest.remove();
 		}
 		getSheepList().clear();
 		getGuestList().clear();
 	}
 
 	// Set a random colour for all sheep in array
 	void randomizeSheepColour(Sheep sheep) {
 		sheep.setColor(discoColours[(r.nextInt(discoColours.length))]);
 	}
 
 	void jump(Entity entity) {
 		Vector orgVel = entity.getVelocity();
 		Vector newVel = (new Vector()).copy(orgVel);
 		newVel.add(new Vector(0, defaultSheepJump, 0));
 		entity.setVelocity(newVel);
 	}
 
 	// WHY ISN'T THERE A Color.getValue() ?!?!?!?!
 	private Color getColor(int i) {
 		Color c = null;
 		if (i == 1) {
 			c = Color.AQUA;
 		}
 		if (i == 2) {
 			c = Color.BLACK;
 		}
 		if (i == 3) {
 			c = Color.BLUE;
 		}
 		if (i == 4) {
 			c = Color.FUCHSIA;
 		}
 		if (i == 5) {
 			c = Color.GRAY;
 		}
 		if (i == 6) {
 			c = Color.GREEN;
 		}
 		if (i == 7) {
 			c = Color.LIME;
 		}
 		if (i == 8) {
 			c = Color.MAROON;
 		}
 		if (i == 9) {
 			c = Color.NAVY;
 		}
 		if (i == 10) {
 			c = Color.OLIVE;
 		}
 		if (i == 11) {
 			c = Color.ORANGE;
 		}
 		if (i == 12) {
 			c = Color.PURPLE;
 		}
 		if (i == 13) {
 			c = Color.RED;
 		}
 		if (i == 14) {
 			c = Color.SILVER;
 		}
 		if (i == 15) {
 			c = Color.TEAL;
 		}
 		if (i == 16) {
 			c = Color.WHITE;
 		}
 		if (i == 17) {
 			c = Color.YELLOW;
 		}
 
 		return c;
 	}
 
 	void updateAll() {
 		for (Sheep sheeple : getSheepList()) {
 			randomizeSheepColour(sheeple);
 
 			if (doFireworks && state % 8 == 0) {
 				if (r.nextDouble() < 0.50) {
 					spawnRandomFireworkAtSheep(sheeple);
 				}
 			}
 
 			if (doJump) {
 				if (state % 2 == 0 && r.nextDouble() < 0.5) {
 					jump(sheeple);
 				}
 			}
 		}
 
 		for (Entity guest : getGuestList()) {
 			if (doJump) {
 				if (state % 2 == 0 && r.nextDouble() < 0.5) {
 					jump(guest);
 				}
 			}
 		}
 	}
 
	private float getPentatonicNote() {
		return DiscoParty.pentatonicNotes[r.nextInt(pentatonicNotes.length)];
	}
	
 	void playSounds() {
		player.playSound(player.getLocation(), Sound.NOTE_BASS_DRUM, 0.75f, 1.0f);
 		if (this.state % 2 == 0) {
			player.playSound(player.getLocation(), Sound.NOTE_SNARE_DRUM, 0.8f, 1.0f);
 		}

 		if ((this.state + 1) % 8 == 0) {
 			player.playSound(player.getLocation(), Sound.NOTE_STICKS, 1.0f, 1.0f);
 		}
 	}
 
 	void randomizeFirework(Firework firework) {
 		Random r = new Random();
 		Builder effect = FireworkEffect.builder();
 		FireworkMeta meta = firework.getFireworkMeta();
 
 		// construct [1, 3] random colours
 		int numColours = r.nextInt(3) + 1;
 		Color[] colourArray = new Color[numColours];
 		for (int i = 0; i < numColours; i++) {
 			colourArray[i] = getColor(r.nextInt(17) + 1);
 		}
 
 		// randomize effects
 		effect.withColor(colourArray);
 		effect.flicker(r.nextDouble() < 0.5);
 		effect.trail(r.nextDouble() < 0.5);
 		effect.with(FireworkEffect.Type.values()[r.nextInt(FireworkEffect.Type.values().length)]);
 
 		// set random effect and randomize power
 		meta.addEffect(effect.build());
 		meta.setPower(r.nextInt(2) + 1);
 
 		// apply it to the given firework
 		firework.setFireworkMeta(meta);
 	}
 
 	void spawnRandomFireworkAtSheep(Sheep sheep) {
 		Firework firework = (Firework) sheep.getWorld().spawnEntity(sheep.getEyeLocation(), EntityType.FIREWORK);
 		randomizeFirework(firework);
 	}
 
 	void update() {
 		if (duration > 0) {
 			updateAll();
 			playSounds();
 			duration -= period;
 			this.scheduleUpdate();
 			this.state = (this.state + 1) % 10000;
 		} else {
 			this.stopDisco();
 		}
 	}
 
 	void scheduleUpdate() {
 		updater = new DiscoUpdater();
 		updater.runTaskLater(parent, this.period);
 	}
 
 	void startDisco() {
 		this.spawnAll(sheep, radius);
 		this.scheduleUpdate();
 		parent.getPartyMap().put(this.player.getName(), this);
 		// start listening
 		this.partyEvents = new PartyEvents(this.parent, this);
 		parent.getServer().getPluginManager().registerEvents(this.partyEvents, this.parent);
 	}
 
 	void stopDisco() {
 		removeAll();
 		this.duration = 0;
 		if (updater != null) {
 			updater.cancel();
 		}
 		updater = null;
 		parent.getPartyMap().remove(this.player.getName());
 		// stop listening
 		HandlerList.unregisterAll(this.partyEvents);
 	}
 
 	class DiscoUpdater extends BukkitRunnable {
 
 		@Override
 		public void run() {
 			update();
 		}
 	}
 }
