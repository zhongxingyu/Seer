 package me.Kruithne.MinecartsMod;
 
 import java.io.InputStream;
 
import no.runsafe.framework.IConfigurationDefaults;
import no.runsafe.framework.IConfigurationFile;
 import no.runsafe.framework.RunsafePlugin;
 
 public class MinecartsMod extends RunsafePlugin implements IConfigurationFile, IConfigurationDefaults 
 {
 	public MinecartsMod()
 	{
 		super();
 	}
 	
 	@Override
 	protected void PluginSetup()
 	{
 		this.addComponent(MinecartPowerGrid.class);
 		this.addComponent(PlayerListener.class);
 		this.addComponent(VehicleListener.class);
 	}
 	
 	@Override
 	public InputStream getDefaultConfiguration() 
 	{
 		return getResource(Constants.defaultConfigurationFile);
 	}
 
 	@Override
 	public String getConfigurationPath() 
 	{
 		return Constants.configurationFile;
 	}
 }
