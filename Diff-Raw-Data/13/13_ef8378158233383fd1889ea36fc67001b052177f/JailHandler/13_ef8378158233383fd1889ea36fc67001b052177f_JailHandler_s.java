 package no.runsafe.runsafejail.handlers;
 
 import no.runsafe.framework.configuration.IConfiguration;
 import no.runsafe.framework.event.IConfigurationChanged;
 import no.runsafe.framework.output.IOutput;
 import no.runsafe.framework.server.RunsafeLocation;
 import no.runsafe.framework.server.player.RunsafePlayer;
 import no.runsafe.framework.timer.CallbackTimer;
 import no.runsafe.framework.timer.IScheduler;
 import no.runsafe.runsafejail.JailSentence;
 import no.runsafe.runsafejail.database.JailedPlayersDatabase;
 import no.runsafe.runsafejail.database.JailsDatabase;
 import org.joda.time.DateTime;
 
 import java.util.HashMap;
 import java.util.logging.Level;
 
 public class JailHandler implements IConfigurationChanged
 {
 	public JailHandler(JailsDatabase jailsDatabase, JailedPlayersDatabase jailedPlayersDatabase, IScheduler scheduler, IOutput console)
 	{
 		this.jailsDatabase = jailsDatabase;
 		this.jailedPlayersDatabase = jailedPlayersDatabase;
 		this.scheduler = scheduler;
 		this.console = console;
 	}
 
 	@Override
 	public void OnConfigurationChanged(IConfiguration configuration)
 	{
 		this.jails = this.jailsDatabase.getJails();
 		this.jailedPlayers = this.jailedPlayersDatabase.getJailedPlayers();
 
 		for (String key : this.jailedPlayers.keySet())
 		{
 			JailSentence jailSentence = this.jailedPlayers.get(key);
			jailSentence.setSentenceTimer(this.createJailTimer(key));
 			this.jailedPlayers.put(key, jailSentence);
 		}
 	}
 
 	public RunsafeLocation getJailLocation(String jailName)
 	{
 		return (this.jails.containsKey(jailName) ? this.jails.get(jailName) : null);
 	}
 
 	public JailSentence getPlayerSentence(String playerName)
 	{
 		return (this.jailedPlayers.containsKey(playerName) ? this.jailedPlayers.get(playerName) : null);
 	}
 
 	public boolean jailExists(String jailName)
 	{
 		return this.jails.containsKey(jailName);
 	}
 
	private CallbackTimer createJailTimer(final String playerName)
 	{
 		this.console.outputDebugToConsole("Creating callback timer for %s", Level.FINE, playerName);
 		return new CallbackTimer(this.scheduler, new Runnable() {
 			@Override
 			public void run()
 			{
 				unjailPlayer(playerName);
 			}
		}, 10L, 0L, true);
 	}
 
 	public final void unjailPlayer(String playerName)
 	{
 		this.console.outputDebugToConsole("Unjailing player %s", Level.FINE, playerName);
 		this.jailedPlayers.remove(playerName);
 		this.jailedPlayersDatabase.removeJailedPlayer(playerName);
 	}
 
 	public void jailPlayer(RunsafePlayer player, String jailName, DateTime end)
 	{
 		String playerName = player.getName();
 		this.console.outputDebugToConsole("Jailing player %s", Level.FINE, playerName);
 		player.teleport(this.getJailLocation(jailName));
 		this.jailedPlayers.remove(playerName);
 
 		JailSentence jailSentence = new JailSentence(playerName, jailName, end);
		jailSentence.setSentenceTimer(this.createJailTimer(playerName));
 		this.jailedPlayers.put(playerName, jailSentence);
 	}
 
 	private HashMap<String, RunsafeLocation> jails;
 	private HashMap<String, JailSentence> jailedPlayers;
 	private JailsDatabase jailsDatabase;
 	private JailedPlayersDatabase jailedPlayersDatabase;
 	private IScheduler scheduler;
 	private IOutput console;
 }
