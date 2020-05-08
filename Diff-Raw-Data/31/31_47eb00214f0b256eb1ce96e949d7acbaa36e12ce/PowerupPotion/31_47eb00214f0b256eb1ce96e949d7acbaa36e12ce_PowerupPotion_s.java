 package uk.thecodingbadgers.minekart.powerup;
 
 import java.io.File;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.potion.PotionEffect;
 import org.bukkit.potion.PotionEffectType;
 
 import uk.thecodingbadgers.minekart.MineKart;
 import uk.thecodingbadgers.minekart.jockey.Jockey;
 
 public class PowerupPotion extends Powerup {
 
 	/** The type of potion to apply **/
 	private PotionEffectType type;
 
 	/** The length to apply the effect **/
 	private Long length;
 
 	/** The level of potion to use **/
 	private int level;
 
 	/** The powerup mode **/
 	protected PowerupApplyMode applyMode;
 
 	/**
 	 * Class constructor
 	 */
 	public PowerupPotion() {
 		this.useMode = PowerupUseMode.Potion;
 	}
 
 	/**
 	 * Copy constructor
 	 * 
 	 * @param powerup Powerup to copy from
 	 */
 	public PowerupPotion(PowerupPotion powerup) {
 		super(powerup);
 		this.type = powerup.type;
 		this.length = powerup.length;
 		this.level = powerup.level;
 		this.applyMode = powerup.applyMode;
 	}
 
 	/**
 	 * Gets the apply mode of the potion
 	 * 
 	 * @return The apply mode
 	 */
 	public PowerupApplyMode getApplyMode() {
 		return applyMode;
 	}
 
 	/**
 	 * Load the powerup
 	 * 
 	 * @param configfile The file containing the powerup data
 	 */
 	public void load(File configfile) {
 		super.load(configfile);
 
 		FileConfiguration file = YamlConfiguration.loadConfiguration(configfile);
 
 		this.length = file.getLong("powerup.potion.length");
 		this.level = file.getInt("powerup.potion.level");
 		this.type = PotionEffectType.getByName(file.getString("powerup.potion.type"));
 		this.applyMode = PowerupApplyMode.valueOf(file.getString("powerup.potion.apply"));
 
 	}
 
 	/**
 	 * Called when the powerup is used
 	 * 
 	 * @param jockey The player who used it
 	 */
 	@Override
 	public void onUse(Jockey jockey) {
 
 		this.amount--;
 
 		PotionEffect effect = new PotionEffect(this.type, (int) (this.length * 20), this.level, false);
 
 		if (this.applyMode == PowerupApplyMode.Self) {
 			jockey.getPlayer().addPotionEffect(effect, true);
 			jockey.getMount().getBukkitEntity().addPotionEffect(effect, true);
 		} else if (this.applyMode == PowerupApplyMode.Others) {
 			for (Jockey other : jockey.getRace().getJockeys()) {
 				other.getPlayer().addPotionEffect(effect, true);
 				other.getMount().getBukkitEntity().addPotionEffect(effect, true);
 			}
 		}
 
 		handleInvisible(jockey);
 
 	}
 
 	@SuppressWarnings("deprecation")
 	private void handleInvisible(final Jockey jockey) {
 
 		if (this.type.getId() != 14) { // Doing it by type doesn't work? but id does
 			return;
 		}
 
 		PlayerInventory invent = jockey.getPlayer().getInventory();
 		invent.setHelmet(null);
 		invent.setChestplate(null);
 		invent.setLeggings(null);
 		invent.setBoots(null);
 		jockey.getPlayer().updateInventory();
 
 		Bukkit.getScheduler().runTaskLater(MineKart.getInstance(), new Runnable() {
 
 			@Override
 			public void run() {
 				jockey.equipGear();
 			}
 
 		}, this.length * 20L);
 	}
 
 }
