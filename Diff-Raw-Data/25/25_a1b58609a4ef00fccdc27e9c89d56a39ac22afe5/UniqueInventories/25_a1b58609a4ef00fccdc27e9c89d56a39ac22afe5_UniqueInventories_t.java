 package no.runsafe.UniqueInventories;
 
 import no.runsafe.UniqueInventories.Command.*;
 import no.runsafe.UniqueInventories.Command.Template.LoadCommand;
 import no.runsafe.UniqueInventories.Command.Template.SaveCommand;
 import no.runsafe.framework.RunsafePlugin;
 import no.runsafe.framework.command.RunsafeCommand;
 import no.runsafe.framework.configuration.IConfigurationFile;
 
 import java.io.InputStream;
 
 public class UniqueInventories extends RunsafePlugin implements IConfigurationFile
 {
 	public PlayerListener playerListener = null;
 
 	@Override
 	protected void PluginSetup()
 	{
 		addComponent(InventoryHandler.class);
 		addComponent(PlayerListener.class);
 		addComponent(InventoryRepository.class);
 		addComponent(InventoryUniverses.class);
 
 		RunsafeCommand template = new RunsafeCommand("template", null);
 		template.addSubCommand(getInstance(LoadCommand.class));
 		template.addSubCommand(getInstance(SaveCommand.class));
 
 		RunsafeCommand command = new RunsafeCommand("uniqueinv", null);
 		command.addSubCommand(getInstance(ListCommand.class));
 		command.addSubCommand(getInstance(PushCommand.class));
 		command.addSubCommand(getInstance(PopCommand.class));
 		command.addSubCommand(getInstance(WorldwipeCommand.class));
 		command.addSubCommand(template);
 
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
