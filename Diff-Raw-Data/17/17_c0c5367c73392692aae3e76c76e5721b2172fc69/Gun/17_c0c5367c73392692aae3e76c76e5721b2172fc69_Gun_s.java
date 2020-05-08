 package team.GunsPlus.Classes;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.Plugin;
 import org.getspout.spoutapi.material.item.GenericCustomItem;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
<<<<<<< HEAD
 import team.GunsPlus.EffectType;
=======
 import team.GunsPlus.GunsPlus;
 import team.GunsPlus.Util;
>>>>>>> 053498bcfa6b3a314da7c8e5baba2fecaa08ae0a
 
 public class Gun extends GenericCustomItem{
 
 	//PRIVATE PROPERTIES
 	private ArrayList<ItemStack> ammo = new ArrayList<ItemStack>(); //Holds all accepted ammo itemstacks
 	private Map<String,Float> values = new HashMap<String,Float>(); //Holds Damage, Recoil, Etc in single location
 	private Map<String, String> resources = new HashMap<String, String>(); //Holds file resources like texture and sounds
<<<<<<< HEAD
 	private ArrayList<EffectType> effects = new ArrayList<EffectType>(); //Holds the effects of the gun
=======
 	private GunsPlus plugin;
>>>>>>> 053498bcfa6b3a314da7c8e5baba2fecaa08ae0a
 	
 	public Gun(Plugin plugin, String name, String texture) {
 		super(plugin, name, texture);
 		this.plugin = (GunsPlus) plugin;
 	}
 	
 	
 	public void zoom(SpoutPlayer sp){
 		//When doing Scope texture placement, set the render priority to normal or low
 		if(!plugin.inZoom.contains(sp)){
 			Util.zoomIn(p, zTex, zoomfactor); //TODO: Finish Method
 			plugin.inZoom.add(sp);
			if(plugin.generalConfig.getBoolean("send-notifications"))
 			(sp).sendNotification(this.getName(), "Zoomed in!", Material.ENDER_PEARL);
 		}else{
 			Util.zoomOut(sp); //TODO: Finish Method
 			plugin.inZoom.remove(sp);
			if(plugin.warnings) (sp).sendNotification(this.getName(), "Zoomed out!", Material.GOLDEN_APPLE);
 		}
 	}
 	
 	public void fire(SpoutPlayer sp){
 		
 	}
 	
 	public void reload(SpoutPlayer sp){
 		
 	}
 	
 	//GETTERS AND SETTERS
 	public ArrayList<ItemStack> getAmmo() {
 		return ammo;
 	}
 
 	public void setAmmo(ArrayList<ItemStack> ammo) {
 		this.ammo = ammo;
 	}
 
 	public String getResource(String s) {
 		return resources.containsKey(s)?resources.get(s):null;
 	}
 
 	public void setResource(String name, String resource) {
 		this.resources.put(name, resource);
 	}
 	
 	public void setValue(String name, Float value) {
 		values.put(name, value);
 	}
 	
 	public float getValue(String name) {
 		return values.containsKey(name)?values.get(name):null;
 	}
 	
 	public void addEffect(EffectType et){
 		effects.add(et);
 	}
 	
 	public void removeEffect(EffectType et){
 		if(effects.contains(et))
 			effects.remove(et);
 	}
 
 }
