 package no.runsafe.runsafejail;
 
 import no.runsafe.framework.RunsafeConfigurablePlugin;
 import no.runsafe.framework.RunsafePlugin;
 import no.runsafe.runsafejail.commands.JailCommand;
import no.runsafe.runsafejail.database.JailedPlayersDatabase;
 import no.runsafe.runsafejail.database.JailsDatabase;
 import no.runsafe.runsafejail.handlers.JailHandler;
 
 public class Plugin extends RunsafeConfigurablePlugin
 {
 	@Override
 	protected void PluginSetup()
 	{
 		// TODO: Jail command
 		// TODO: Unjail command
 		// TODO: Location restriction
 
 		// TODO: in-game jail creator
 		// TODO: Handle players who have no previous location?
 
 		// Commands
 		this.addComponent(JailCommand.class);
 
 		// Handlers
 		this.addComponent(JailHandler.class);
 
 		// Database
 		this.addComponent(JailsDatabase.class);
		this.addComponent(JailedPlayersDatabase.class);
 	}
 }
