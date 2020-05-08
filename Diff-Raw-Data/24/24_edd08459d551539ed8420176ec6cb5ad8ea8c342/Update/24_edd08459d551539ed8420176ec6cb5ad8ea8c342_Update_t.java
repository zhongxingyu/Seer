 package xelitez.frostcraft.plugins;
 
 import xelitez.frostcraft.FrostCraft;
 import xelitez.frostcraft.Version;
 import xelitez.frostcraft.registry.Settings;
 import xelitez.updateutility.IXEZUpdate;
 
 public class Update implements IXEZUpdate
 {
 
 	@Override
 	public String getCurrentVersion() 
 	{
 		return Version.getVersion() + " for " + Version.MC;
 	}
 
 	@Override
 	public String getNewVersion() 
 	{
 		return Version.newVersion;
 	}
 
 	@Override
 	public void checkForUpdates() 
 	{
 		FrostCraft.instance.version.checkForUpdates();
 	}
 
 	@Override
 	public boolean doesModCheckForUpdates() 
 	{
 		return Settings.checkForUpdates;
 	}
 
 	@Override
 	public boolean isUpdateAvailable() 
 	{
 		return Version.available;
 	}
 
 	@Override
 	public String getModIcon() 
 	{
 		return "/mods/FrostCraft/textures/xezmods.png";
 	}
 
 	@Override
 	public String getUpdateUrl() 
 	{
 		return "http://www.minecraftforum.net/topic/842232-/#KWC";
 	}
 
 	@Override
 	public String getDownloadUrl() 
 	{
		return "http://adf.ly/K3kPs";
	}

	@Override
	public String stringToDelete() 
	{
		return "FrostCraft";
 	}
 
 }
