 package me.Kruithne.MinecartsMod;
 
 import java.io.InputStream;
 
 import no.runsafe.framework.RunsafePlugin;
import no.runsafe.framework.interfaces.IConfigurationDefaults;
import no.runsafe.framework.interfaces.IConfigurationFile;
 
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
