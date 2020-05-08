 package uk.thecodingbadgers.minekart.util;
 
 import java.util.ArrayList;
 
 import org.bukkit.Color;
 import org.bukkit.FireworkEffect;
 import org.bukkit.FireworkEffect.Builder;
 import org.bukkit.Location;
 import org.bukkit.craftbukkit.v1_6_R3.CraftWorld;
 import org.bukkit.craftbukkit.v1_6_R3.entity.CraftFirework;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Firework;
 import org.bukkit.inventory.meta.FireworkMeta;
 
 public class FireworkFactory {
 
	/**
 	 * Launch a firework at a given location with specified properties
	 * 
	 * @param spawnLocation the location for the firework
	 * @param type the firework type
	 * @param power the power of the firework
	 * @param colors the colors of the fireworks
	 * @param fadecolors the colors for the firework to fade to
	 * @param trail if the firework should leave a trail
	 * @param flicker if the firework should flicker
 	 */
 	static public Firework LaunchFirework(Location spawnLocation, FireworkEffect.Type type, int power, ArrayList<Color> colors, ArrayList<Color> fadecolors, boolean flicker, boolean trail) {
 
 		Firework firework = (Firework) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.FIREWORK);
 		FireworkMeta metadata = firework.getFireworkMeta();
 
 		Builder builder = FireworkEffect.builder();
 		builder.with(type);
 		builder.flicker(flicker);
 		builder.trail(trail);
 		builder.withColor(colors);
 		builder.withFade(fadecolors);
 
 		FireworkEffect effect = builder.build();
 		metadata.addEffect(effect);
 		metadata.setPower(power);
 
 		firework.setFireworkMeta(metadata);
 		
 		return firework;
 	}
 
	/**
 	 * Launch a firework at a given location with specified properties Will
 	 * use same fade color as color and have trail and flicker enabled
	 * 
	 * @param spawnLocation the location for the firework
	 * @param type the firework type
	 * @param power the power of the firework
	 * @param colors the colors of the fireworks
 	 */
 	static public Firework LaunchFirework(Location spawnLocation, FireworkEffect.Type type, int power, Color color) {
 		ArrayList<Color> colors = new ArrayList<Color>();
 		colors.add(color);
 
 		return LaunchFirework(spawnLocation, type, power, colors, colors, true, true);
 	}
 	
	/**
	 * Display a firework explosion at a location
	 * 
	 * @param spawnLocation the location for the explosion
	 * @param type the firework effect type
	 * @param color the color of the fireworks
	 */
 	static public void SpawnFireworkExplosion(Location spawnLocation, FireworkEffect.Type type, Color color) {
 		try {
 			Firework firework = LaunchFirework(spawnLocation, type, 1, color);
 			CraftWorld world = (CraftWorld) spawnLocation.getWorld();
 			world.getHandle().broadcastEntityEffect(((CraftFirework)firework).getHandle(), (byte) 17);
 			firework.remove();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 }
