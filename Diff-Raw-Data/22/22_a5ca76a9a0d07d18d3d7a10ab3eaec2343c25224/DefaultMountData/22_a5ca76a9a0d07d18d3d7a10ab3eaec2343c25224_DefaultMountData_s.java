 package uk.thecodingbadgers.minekart.mount;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.LivingEntity;
 
 public class DefaultMountData extends MountTypeData {
 
	private double health;
 
 	protected DefaultMountData(EntityType type) {
 		super(type);
 	}
 
 	@Override
 	public void loadData(ConfigurationSection section) {
 		health = section.getDouble("health", -1);
 		System.out.println("health loaded as " + health);
 	}
 
 	@Override
 	public ConfigurationSection getSaveData(ConfigurationSection section) {
 		section.set("health", health);
 		return section;
 	}
 
 	public double getCustomHealth() {
 		return health;
 	}
 
 	@Override
 	public void applyMountData(Entity entity) {
 
 		if (!(entity instanceof LivingEntity)) {
 			return;
 		}
 
 		LivingEntity lentity = (LivingEntity) entity;
 
 		if (health > 0) {
 			System.out.println("Health property applied (" + health + ")");
 			lentity.setMaxHealth(health);
 			lentity.setHealth(health);
 		}
 	}
 
 }
