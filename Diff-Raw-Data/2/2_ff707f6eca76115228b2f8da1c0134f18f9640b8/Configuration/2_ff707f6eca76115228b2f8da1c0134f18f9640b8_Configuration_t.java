 package me.Kruithne.WolfHunt;
 
 public class Configuration {
 	
 	private WolfHunt wolfHuntPlugin = null;
 	
 	public int trackingItem;
 	public int trackingRadius;
 	
 	public boolean babyWolvesCanTrack;
 	
 	public boolean preventTrackingOps;
 	
 	public boolean allowOpOverride;
 	
 	Configuration(WolfHunt parentPlugin)
 	{
 		this.wolfHuntPlugin = parentPlugin;
 	}
 
 	public String getOrSetConfigValue(String configKey, String defaultValue)
 	{	
 		if (this.hasConfigValue(configKey))
 		{
 			return this.getConfigValue(configKey);
 		}
 		else
 		{
 			this.setConfigValue(String.format(Constants.pluginNodePath, configKey), defaultValue);
 			return defaultValue;
 		}
 	}
 	
 	public String getConfigValue(String configKey)
 	{
 		return this.wolfHuntPlugin.getConfig().getString(String.format(Constants.pluginNodePath, configKey));
 	}
 	
 	public boolean hasConfigValue(String configKey)
 	{
 		return this.wolfHuntPlugin.getConfig().contains(String.format(Constants.pluginNodePath, configKey));
 	}
 	
 	public void setConfigValue(String configKey, String configValue)
 	{
		this.wolfHuntPlugin.getConfig().set(String.format(Constants.pluginNodePath, configKey), configValue);
 		this.wolfHuntPlugin.saveConfig();
 	}
 	
 	public void loadConfiguration()
 	{
 		this.trackingItem = Integer.parseInt(this.getOrSetConfigValue("trackingItem", Constants.default_trackingItem));
 		this.trackingRadius = Integer.parseInt(this.getOrSetConfigValue("trackingRadius", Constants.default_trackingRadius));
 		this.allowOpOverride = Boolean.parseBoolean(this.getOrSetConfigValue("allowOpOverride", Constants.default_allowOpOverride));
 		this.preventTrackingOps = Boolean.parseBoolean(this.getOrSetConfigValue("preventTrackingOps", Constants.default_preventTrackingOps));
 		this.babyWolvesCanTrack = Boolean.parseBoolean(this.getOrSetConfigValue("babyWolvesCanTrack", Constants.default_babyWolvesCanTrack));
 	}
 	
 }
