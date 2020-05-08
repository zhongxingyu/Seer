 package net.zhuoweizhang.piggrinder;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.command.*;
 import org.bukkit.event.Event;
 import org.bukkit.entity.Cow;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Pig;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Sheep;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.Recipe;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.player.AppearanceManager;
 import org.getspout.spoutapi.player.EntitySkinType;
 
 public class PigGrinderPlugin extends JavaPlugin {
 
 	public Recipe grinderRecipe; 
 
 	public Material grinderMaterial = Material.BUCKET;
 
 	public short grinderMetadata = 70;
 
 	public int grinderDelay = 5;
 
 	public int grinderAmount = 50;
 
 	public boolean grinderExplode = true;
 
 	public float grinderExplodePower = 1f;
 
 	public String grinderTextureURL, grinderCowTextureURL, grinderSheepTextureURL, grinderItemTextureURL;
 
 	public double grinderYVelocity;
 
 	public final PigGrinderPlayerListener playerListener = new PigGrinderPlayerListener(this);
 
 	public final List<PigGrinderTask> tasks = new ArrayList<PigGrinderTask>();
 
 	@Override
 	public void onEnable() {
 		Configuration config = getConfiguration();
 		Material mat = Material.matchMaterial(config.getString("material", "BUCKET"));
 		if (mat != null)
 			grinderMaterial = mat;
 		grinderMetadata = (short) config.getInt("metadata", 70);
 		grinderDelay = config.getInt("delay", 5);
 		grinderAmount = config.getInt("amount", 20);
 		grinderExplode = config.getBoolean("explode", true);
 		grinderExplodePower = (float) config.getDouble("explodepower", 1.0);
 		grinderTextureURL = config.getString("textureurl", "http://cloud.github.com/downloads/zhuowei/PigGrinder/pig_grinder_texture.png");
 		grinderCowTextureURL = config.getString("textureurl-cow", "http://cloud.github.com/downloads/zhuowei/PigGrinder/pig_grinder_cow_texture.png");
 		grinderSheepTextureURL = config.getString("textureurl-sheep", "http://cloud.github.com/downloads/zhuowei/PigGrinder/pig_grinder_sheep_texture.png");
 		grinderItemTextureURL = config.getString("itemtextureurl", "http://cloud.github.com/downloads/zhuowei/PigGrinder/pig_grinder_item_texture.png");
 		grinderYVelocity = config.getDouble("yvelocity", 0.25);
 		config.save();	
 		PluginManager pm = this.getServer().getPluginManager();
 		pm.registerEvent(Event.Type.PLAYER_INTERACT_ENTITY, playerListener, Event.Priority.Normal, this);
 		grinderRecipe = new ShapedRecipe(new ItemStack(grinderMaterial, 1, grinderMetadata)).shape("bib", "iri", "bib").setIngredient('b', Material.CLAY_BRICK).
 				setIngredient('i', Material.IRON_INGOT).setIngredient('r', Material.REDSTONE);
 		getServer().addRecipe(grinderRecipe);
 		try {
 			SpoutManager.getItemManager().setItemName(grinderMaterial, grinderMetadata, "Grinder");
			SpoutManager.getItemManager().setItemTexture(grinderMaterial, grinderMetadata, this, grinderItemTextureURL);
 		}
 		catch(NoClassDefFoundError err) {
 			System.err.println("[PigGrinder] Spout is not installed. ");
 		}
 		catch(Exception e) {
 			System.err.println("[PigGrinder] Could not initialize Spout support.");
 		}
 	}
 
 	public void onDisable() {
 		getServer().getScheduler().cancelTasks(this);
 	}
 
 	public PigGrinderTask grind(LivingEntity pig) {
 		if (!(pig instanceof Pig || pig instanceof Cow || pig instanceof Sheep)) {
 			throw new IllegalArgumentException("Only pigs, cows or sheep can be grinded");
 		}
 		//System.out.println("grind");
 		PigGrinderTask task = new PigGrinderTask(pig, grinderAmount);
 		int taskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, task, grinderDelay, grinderDelay);
 		task.taskId = taskId;
 		tasks.add(task);
 		return task;
 	}
 
 	public boolean checkCanUseGrinder(Player player, LivingEntity entity) {
 		String node;
 		if (entity instanceof Pig) {
 			node = "piggrinder.use.pig";
 		} else if (entity instanceof Sheep) {
 			node = "piggrinder.use.sheep";
 		} else if (entity instanceof Cow) {
 			node = "piggrinder.use.cow";
 		} else {
 			throw new IllegalArgumentException("Only pigs, cows or sheep can be grinded");
 		}
 		return player.hasPermission(node);
 	}
 
 	public class PigGrinderTask implements Runnable {
 
 		public LivingEntity pig;
 		public int amount;
 		public int taskId = 0;
 
 		public PigGrinderTask(LivingEntity pig, int amount) {
 			if (!(pig instanceof Pig || pig instanceof Cow || pig instanceof Sheep)) {
 				throw new IllegalArgumentException("Only pigs, cows or sheep can be grinded");
 			}
 			this.pig = pig;
 			this.amount = amount;
 			try {
 				AppearanceManager manager = SpoutManager.getAppearanceManager();
 				String texUrl = grinderTextureURL;
 				if (pig instanceof Pig) {
 					texUrl = grinderTextureURL;
 				} else if (pig instanceof Cow) {
 					texUrl = grinderCowTextureURL;
 				} else if (pig instanceof Sheep) {
 					texUrl = grinderSheepTextureURL;
 				}
 				manager.setGlobalEntitySkin(pig, texUrl, EntitySkinType.DEFAULT);
 			}
 			catch(NoClassDefFoundError err) {
 				//No Spout? Shame on you.
 			}
 			catch(Throwable e) {
 				e.printStackTrace();
 			}
 			if (pig instanceof Sheep) {
 				((Sheep) pig).setSheared(true);
 			}
 		}
 
 		public void run() {
 			//System.out.println("run");
 			if (pig.isDead()) {
 				Bukkit.getServer().getScheduler().cancelTask(taskId);
 				if (tasks.contains(this)) {
 					tasks.remove(this);
 				}
 				return;
 			}
 			Material dropMat; 
 			if (pig instanceof Pig) {
 				dropMat = (pig.getFireTicks() > 0 ? Material.GRILLED_PORK : Material.PORK);
 			} else if (pig instanceof Cow) {
 				dropMat = (pig.getFireTicks() > 0 ? Material.COOKED_BEEF : Material.RAW_BEEF);
 			} else if (pig instanceof Sheep) {
 				dropMat = Material.WOOL;
 			} else {
 				dropMat = Material.AIR; //How did we get here?
 			}
 			World world = pig.getWorld();
 			Location loc = pig.getLocation();
 			Item item = world.dropItemNaturally(loc, new ItemStack(dropMat, 1));
 			item.setVelocity(item.getVelocity().setY(item.getVelocity().getY() + grinderYVelocity));
 			//System.out.println("dropped pork");
 			amount--;
 
 			if (amount <= 0) {
 				pig.remove();
 				if (grinderExplode) {
 					world.createExplosion(loc, grinderExplodePower);
 				}
 				Bukkit.getServer().getScheduler().cancelTask(taskId);
 				if (tasks.contains(this)) {
 					tasks.remove(this);
 				}
 			}
 		}
 	}
 
 }
