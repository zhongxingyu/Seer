 package net.minekingdom.continuum.world;
 
 import java.util.List;
 
 import net.minekingdom.continuum.Continuum;
 import net.minekingdom.continuum.utils.GenUtils;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.bukkit.Bukkit;
 import org.bukkit.Difficulty;
 import org.bukkit.Server;
 import org.bukkit.World;
 import org.bukkit.World.Environment;
 import org.bukkit.WorldCreator;
 import org.bukkit.WorldType;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.metadata.MetadataValue;
 import org.bukkit.permissions.Permissible;
 import org.bukkit.permissions.Permission;
 
 public class Dimension {
 	
 	public final Permission ACCESS_PERMISSION;
 	
 	protected final Server server;
 	protected final Universe universe;
 	
 	protected final String name;
 	
 	protected World handle;
 	
 	protected long seed;
 	protected String generator;
 	protected Environment environment;
 	protected WorldType type;
 	
 	protected double scale;
 	
 	protected boolean loaded;
 	
 	protected boolean monsters;
 	protected boolean animals;
 	
 	protected int monsterSpawnLimit;
 	protected int waterMobSpawnLimit;
 	protected int animalSpawnLimit;
 	
 	protected boolean pvp;
 	protected Difficulty	difficulty;
 	
 	protected boolean keepSpawnInMemory;
 	
 	public Dimension(Server server, Universe universe, String name, long seed, String generator, Environment environment, WorldType type) {
 		this.server = server;
 		this.universe = universe;
 		
 		this.ACCESS_PERMISSION = new Permission(universe.ACCESS_PERMISSION.getName() + "." + name);
 		this.ACCESS_PERMISSION.addParent(universe.ACCESS_PERMISSION, true);
 		server.getPluginManager().addPermission(ACCESS_PERMISSION);
 		server.getPluginManager().recalculatePermissionDefaults(Universe.ALL_ACCESS_PERMISSION);
 		
 		this.name        = name;
 		this.seed        = seed;
 		this.generator   = generator;
 		this.environment = environment;
 		this.type        = type;
 		
 		this.scale = 1;
 		
 		this.monsters = true;
 		this.animals  = true;
 		
 		this.monsterSpawnLimit  = -1;
 		this.animalSpawnLimit   = -1;
 		this.waterMobSpawnLimit = -1;
 
 		this.difficulty = Difficulty.NORMAL;
 		this.pvp = true;
 		
 		this.keepSpawnInMemory = false;
 	}
 	
 	public Dimension(Server server, Universe parent, String name, long seed) {
 		this(server, parent, name, seed, "", Environment.NORMAL, WorldType.NORMAL);
 	}
 	
 	public Dimension(Server server, Universe parent, String name, long seed, String generator) {
 		this(server, parent, name, seed, generator, Environment.NORMAL, WorldType.NORMAL);
 	}
 	
 	public Dimension(Server server, Universe parent, String name, long seed, Environment environment) {
 		this(server, parent, name, seed, "", environment, WorldType.NORMAL);
 	}
 	
 	public Dimension(Server server, Universe parent, String name, long seed, WorldType type) {
 		this(server, parent, name, seed, "", Environment.NORMAL, type);
 	}
 	
 	public Dimension(Server server, Universe parent, String name, long seed, String generator, Environment environment) {
 		this(server, parent, name, seed, generator, environment, WorldType.NORMAL);
 	}
 	
 	public Dimension(Server server, Universe parent, String name, long seed, String generator, WorldType type) {
 		this(server, parent, name, seed, generator, Environment.NORMAL, type);
 	}
 	
 	public Dimension(Server server, Universe parent, String name, String generator) {
 		this(server, parent, name, GenUtils.generateSeed(), generator, Environment.NORMAL, WorldType.NORMAL);
 	}
 	
 	public Dimension(Server server, Universe parent, String name, String generator, Environment environment) {
 		this(server, parent, name, GenUtils.generateSeed(), generator, environment, WorldType.NORMAL);
 	}
 	
 	public Dimension(Server server, Universe parent, String name, String generator, WorldType type) {
 		this(server, parent, name, GenUtils.generateSeed(), generator, Environment.NORMAL, type);
 	}
 
 	public Dimension(Server server, Universe parent, String name, String generator, Environment environment, WorldType type) {
 		this(server, parent, name, GenUtils.generateSeed(), generator, environment, type);
 	}
 	
 	public Dimension(Server server, Universe parent, String name, Environment environment) {
 		this(server, parent, name, GenUtils.generateSeed(), "", environment, WorldType.NORMAL);
 	}
 
 	public Dimension(Server server, Universe parent, String name, Environment environment, WorldType type) {
 		this(server, parent, name, GenUtils.generateSeed(), "", environment, type);
 	}
 	
 	public Dimension(Server server, Universe parent, String name, WorldType type) {
 		this(server, parent, name, GenUtils.generateSeed(), "", Environment.NORMAL, type);
 	}
 	
 	public Dimension(Server server, Universe parent, String name) {
 		this(server, parent, name, GenUtils.generateSeed(), "", Environment.NORMAL, WorldType.NORMAL);
 	}
 	
 	/*-------------------------------------*
 	 *            Load functions           *
 	 *-------------------------------------*/
 	
 		public boolean isLoaded() {
 			return this.loaded;
 		}
 		
 		public boolean load() {
 			if (isLoaded()) {
 				throw new IllegalStateException("World is already loaded.");
 			}
 			
 			WorldCreator creator = getWorldCreator();
 			try {
 				this.handle = server.createWorld(creator);
 			} catch (Throwable t) {
 				t.printStackTrace();
 				if (this.handle == null) {
 					this.handle = Bukkit.getWorld(creator.name());
 					if (handle == null
 							|| handle.getSeed() != creator.seed()
 							|| !handle.getWorldType().equals(creator.type()) 
 							|| !handle.getEnvironment().equals(creator.environment())) {
 						return false;
 					}
 				}
 			}
 			updateHandle();
 			
 			this.handle.setMetadata("continuum.universe", new FixedMetadataValue(Continuum.getInstance(), this.universe));
 			this.handle.setMetadata("continuum.dimension", new FixedMetadataValue(Continuum.getInstance(), this));
 			
 			this.loaded = true;
 			return true;
 		}
 		
 		protected WorldCreator getWorldCreator() {
 			WorldCreator creator = new WorldCreator(universe.getName() + "_" + name);
 			if (seed != 0) {
 				creator.seed(seed);
 			}
 			if (environment != null) {
 				creator.environment(environment);
 			}
			if (generator != null) {
 				creator.generator(generator);
 			}
 			if (type != null) {
 				creator.type(type);
 			}
 			return creator;
 		}
 		
 		public void unload() {
 			unload(true);
 		}
 	
 		public void unload(boolean save) {
 			if (!isLoaded()) {
 				throw new IllegalStateException("World is already unloaded.");
 			}
 			this.loaded = false;
 			this.server.unloadWorld(this.handle, save);
 			this.handle = null;
 		}
 	
 	/*-------------------------------------*
 	 *           Update Functions          *
 	 *-------------------------------------*/
 	
 		private void updateHandle() {
 			updateSpawnFlags();
 			updateMonsterSpawnLimit();
 			updateAnimalSpawnLimit();
 			updateWaterMobSpawnLimit();
 			updateDifficulty();
 			updatePVP();
 			updateKeepSpawnInMemory();
 		}
 		
 		private void updateSpawnFlags() {
 			handle.setSpawnFlags(monsters, animals);
 		}
 		
 		private void updateMonsterSpawnLimit() {
 			this.handle.setMonsterSpawnLimit(this.monsterSpawnLimit);
 		}
 		
 		private void updateAnimalSpawnLimit() {
 			this.handle.setAnimalSpawnLimit(this.animalSpawnLimit);
 		}
 		
 		private void updateWaterMobSpawnLimit() {
 			this.handle.setWaterAnimalSpawnLimit(this.waterMobSpawnLimit);
 		}
 	
 		private void updateDifficulty() {
 			this.handle.setDifficulty(difficulty);
 		}
 	
 		private void updatePVP() {
 			this.handle.setPVP(pvp);
 		}
 		
 		private void updateKeepSpawnInMemory() {
 			try {
 				this.handle.setKeepSpawnInMemory(this.keepSpawnInMemory);
 			} catch (Throwable t) {
 				t.printStackTrace();
 			}
 		}
 	
 	/*-------------------------------------*
 	 *        Mutators / Accessors         *
 	 *-------------------------------------*/
 
 		public String getName() {
 			return name;
 		}
 	
 		public long getSeed() {
 			return seed;
 		}
 	
 		public Dimension setSeed(long seed) {
 			this.seed = seed;
 			return this;
 		}
 	
 		public String getGenerator() {
 			return generator;
 		}
 	
 		public Dimension setGenerator(String generator) {
 			this.generator = generator;
 			return this;
 		}
 	
 		public Environment getEnvironment() {
 			return environment;
 		}
 	
 		public Dimension setEnvironment(Environment environment) {
 			this.environment = environment;
 			return this;
 		}
 	
 		public WorldType getWorldType() {
 			return type;
 		}
 	
 		public Dimension setWorldType(WorldType worldType) {
 			this.type = worldType;
 			return this;
 		}
 	
 		public boolean hasMonsters() {
 			return monsters;
 		}
 	
 		public Dimension setMonsters(boolean monsters) {
 			this.monsters = monsters;
 			if (isLoaded()) {
 				updateSpawnFlags();
 			}
 			return this;
 		}
 	
 		public boolean hasAnimals() {
 			return animals;
 		}
 	
 		public Dimension setAnimals(boolean animals) {
 			this.animals = animals;
 			if (isLoaded()) {
 				updateSpawnFlags();
 			}
 			return this;
 		}
 	
 		public int getMonsterSpawnLimit() {
 			return this.monsterSpawnLimit;
 		}
 	
 		public Dimension setMonsterSpawnLimit(int monsterSpawnLimit) {
 			this.monsterSpawnLimit = monsterSpawnLimit;
 			if (isLoaded()) {
 				updateMonsterSpawnLimit();
 			}
 			return this;
 		}
 	
 		public int getAnimalSpawnLimit() {
 			return this.animalSpawnLimit;
 		}
 	
 		public Dimension setAnimalSpawnLimit(int animalSpawnLimit) {
 			this.animalSpawnLimit = animalSpawnLimit;
 			if (isLoaded()) {
 				updateAnimalSpawnLimit();
 			}
 			return this;
 		}
 		
 		public int getWaterMobSpawnLimit() {
 			return this.waterMobSpawnLimit;
 		}
 	
 		public Dimension setWaterMobSpawnLimit(int waterMobSpawnLimit) {
 			this.waterMobSpawnLimit = waterMobSpawnLimit;
 			if (isLoaded()) {
 				updateWaterMobSpawnLimit();
 			}
 			return this;
 		}
 		
 		public Difficulty getDifficulty() {
 			return difficulty;
 		}
 	
 		public Dimension setDifficulty(Difficulty difficulty) {
 			this.difficulty = difficulty;
 			if (isLoaded()) {
 				updateDifficulty();
 			}
 			return this;
 		}
 	
 		public boolean hasPVP() {
 			return pvp;
 		}
 	
 		public Dimension setPVP(boolean pvp) {
 			this.pvp = pvp;
 			if (isLoaded()) {
 				updatePVP();
 			}
 			return this;
 		}
 		
 		public boolean keepsSpawnInMemory() {
 			return keepSpawnInMemory;
 		}
 		
 		public Dimension setKeepSpawnInMemory(boolean keepSpawnInMemory) {
 			this.keepSpawnInMemory = keepSpawnInMemory;
 			if (isLoaded()) {
 				updateKeepSpawnInMemory();
 			}
 			return this;
 		}
 		
 		public double getScale() {
 			return this.scale;
 		}
 		
 		public Dimension setScale(double scale) {
 			this.scale = scale;
 			return this;
 		}
 
 		public World getHandle() {
 			return handle;
 		}
 		
 		public Universe getWorld() {
 			return this.universe;
 		}
 		
 	/*-------------------------------------*
 	 *                Misc                 *
 	 *-------------------------------------*/
 		
 		public boolean canAccess(Permissible permissible) {
 			return permissible.hasPermission(ACCESS_PERMISSION);
 		}
 		
 		@Override
 		public int hashCode() {
 			return new HashCodeBuilder()
 				.append(this.name)
 				.append(this.universe)
 				.toHashCode();
 		}
 		
 		@Override
 		public boolean equals(Object o) {
 			if (!(o instanceof Dimension)) {
 				return false;
 			}
 			if (o == this) {
 				return true;
 			}
 			Dimension other = (Dimension) o;
 			
 			return new EqualsBuilder()
 				.append(this.name,  other.name)
 				.append(this.universe, other.universe)
 				.isEquals();
 		}
 		
 	/*-------------------------------------*
 	 *               Static                *
 	 *-------------------------------------*/
 		
 		public static Dimension get(World world) {
 			if (world != null) {
 				List<MetadataValue> meta = world.getMetadata("continuum.dimension");
 				if (meta.size() > 0 && meta.get(0).value() instanceof Dimension) {
 					return (Dimension) meta.get(0).value();
 				}
 			}
 			return null;
 		}
 }
