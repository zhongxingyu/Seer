 package no.runsafe.UniqueInventories;
 
 import no.runsafe.framework.RunsafePlugin;
 import no.runsafe.framework.command.ICommand;
 import no.runsafe.framework.command.RunsafeCommand;
 import no.runsafe.framework.configuration.IConfigurationDefaults;
 import no.runsafe.framework.configuration.IConfigurationFile;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 
 public class UniqueInventories extends RunsafePlugin implements IConfigurationFile, IConfigurationDefaults
 {
 	public PlayerListener playerListener = null;
 
 	@Override
 	protected void PluginSetup()
 	{
 		addComponent(InventoryHandler.class);
 		addComponent(PlayerListener.class);
 		addComponent(InventoryRepository.class);
 		addComponent(InventoryUniverses.class);
 
 		ArrayList<ICommand> subCommands = new ArrayList<ICommand>()
 		{{
 			add(getInstance(ListCommand.class));
 			add(getInstance(PushCommand.class));
 			add(getInstance(PopCommand.class));
			add(new DebugCommand());
 		}};
 
 		RunsafeCommand command = new RunsafeCommand("uniqueinv", subCommands);
 		addComponent(command);
 	}
 
 	@Override
 	public String getConfigurationPath()
 	{
 		return "plugins/" + this.getName() + "/config.yml";
 	}
 
 	@Override
 	public InputStream getDefaultConfiguration()
 	{
 		return getResource("defaults.yml");
 	}
 }
