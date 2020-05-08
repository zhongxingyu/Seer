 package uk.thecodingbadgers.minekart.powerup;
 
 import java.io.File;
 import java.util.Map;
 
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.metadata.FixedMetadataValue;
 
 import uk.thecodingbadgers.minekart.MineKart;
 import uk.thecodingbadgers.minekart.jockey.Jockey;
 import uk.thecodingbadgers.minekart.powerup.damageeffect.DamageEffect;
 
 public class PowerupProjectile extends Powerup {
 
 	/** Type of projectile to launch **/
 	private EntityType type;
 	
 	/** The speed of the projectile **/
 	private double speed;
 	
 	/** The effect to be applied on contact with another player **/
 	private String damageEffectType;
 	
 	/** The number of hearts damage to do on contact with another player **/
 	private double damage;
 
 	/**
 	 * Class constructor
 	 */
 	public PowerupProjectile() {
 		this.useMode = PowerupUseMode.Projectile;
 	}
 
 	/**
 	 * Copy constructor
 	 * 
 	 * @param powerup Powerup to copy from
 	 */
 	public PowerupProjectile(PowerupProjectile powerup) {
 		super(powerup);
 		this.speed = powerup.speed;
 		this.type = powerup.type;
 		this.damage = powerup.damage;
 		this.damageEffectType = powerup.damageEffectType;
 	}
 
 	/**
 	 * Load the powerup
 	 * 
 	 * @param configfile The file containing the powerup data
 	 */
 	public void load(File configfile) {
 		super.load(configfile);
 
 		FileConfiguration file = YamlConfiguration.loadConfiguration(configfile);
 
 		this.speed = file.getDouble("powerup.projectile.speed");
 		this.type = EntityType.valueOf(file.getString("powerup.projectile.type"));
 		this.damage = file.getDouble("powerup.projectile.damage", -1);		
 		this.damageEffectType = file.getString("powerup.projectile.damageeffect", "none").toLowerCase();
 
 	}
 
 	/**
 	 * Called when the powerup is used
 	 * 
 	 * @param jockey The player who used it
 	 */
 	@Override
 	public void onUse(Jockey jockey) {
 
 		this.amount--;
 
 		final Player player = jockey.getPlayer();
 		final World world = player.getWorld();
 		final Location location = player.getEyeLocation();		
 		
 		Projectile projectile = (Projectile)world.spawnEntity(location, type);
 		projectile.setVelocity(player.getLocation().getDirection().multiply(this.speed));
 		projectile.setShooter(player);
 		
 		projectile.setMetadata("powerup", new FixedMetadataValue(MineKart.getInstance(), this));
 		
 	}
 	
 	/**
 	 * Called when the powerup does damage to another entity
 	 * @param entityAttackEvent The entity damage by entity event 
 	 */
 	@Override
 	public void onDamageEntity(EntityDamageByEntityEvent entityAttackEvent) {
 	
 		LivingEntity entity = (LivingEntity)entityAttackEvent.getEntity();
 		
 		if (this.damage >= 0) {
 			// We store damage as heart damage. So multiply it by 2.
			Double newHealth = entity.getHealth() - (this.damage * 2.0);
			entity.setHealth(newHealth < 0 ? 0 : newHealth);
 			entityAttackEvent.setDamage(0.0);
 		}
 		
 		Map<String, DamageEffect> damageEffects = MineKart.getInstance().getDamageEffects();
 		if (damageEffects.containsKey(this.damageEffectType)) {
 			DamageEffect effect = damageEffects.get(damageEffectType);
 			
 			if (entity instanceof Player) {		
 				Jockey jockey = MineKart.getInstance().getJockey((Player)entity);
 				if (jockey != null) {
 					effect.use(jockey);
 				}
 			}
 		}
 		
 	}
 
 }
