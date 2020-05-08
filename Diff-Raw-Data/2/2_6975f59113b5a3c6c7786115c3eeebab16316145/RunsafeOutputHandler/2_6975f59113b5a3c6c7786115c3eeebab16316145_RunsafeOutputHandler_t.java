 package no.runsafe.framework.output;
 
 import no.runsafe.framework.server.RunsafeServer;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 public class RunsafeOutputHandler implements IOutput
 {
 	private final RunsafeServer serverOutput;
 	private final Logger consoleLog;
 	private Level debugLevel;
 
 	public RunsafeOutputHandler(RunsafeServer server, Logger logger)
 	{
 		this.serverOutput = server;
 		this.consoleLog = logger;
 	}
 
 	// Check if the output handler has a event available to broadcast to
 	private boolean hasServer()
 	{
 		return this.serverOutput != null;
 	}
 
 	// Check if the output handler has a console/log available to broadcast to
 	private boolean hasConsole()
 	{
 		return this.serverOutput != null;
 	}
 
 	@Override
 	public void writeColoured(String message)
 	{
 		writeColoured(message, Level.INFO);
 	}
 
 	@Override
 	public void writeColoured(String message, Object... params)
 	{
 		writeColoured(message, Level.INFO, params);
 	}
 
 	@Override
 	public void writeColoured(String message, Level level, Object... params)
 	{
		outputToConsole(ChatColour.ToConsole(String.format(message, params)), level);
 	}
 
 	// Sends the supplied string to the console/log the output handler has
 	@Override
 	public void outputToConsole(String message)
 	{
 		outputToConsole(message, Level.INFO);
 	}
 
 	@Override
 	public void outputColoredToConsole(String message, Level level)
 	{
 		outputToConsole(ConsoleColors.FromMinecraft(message), level);
 	}
 
 	// Sends the supplied string with the supplied logging level to the console/log the output handler has
 	@Override
 	public void outputToConsole(String message, Level level)
 	{
 		if (this.hasConsole())
 		{
 			this.consoleLog.log(level, message);
 		}
 	}
 
 	// Sends the supplied string to the console/log the output handler has if the debug level is high enough
 	@Override
 	public void outputDebugToConsole(String message, Level messageLevel)
 	{
 		if (debugLevel != null && messageLevel.intValue() >= debugLevel.intValue())
 			outputToConsole(
 				String.format(
 					"[%s%s%s] %s",
 					ConsoleColors.DARK_GREEN,
 					messageLevel.getName(),
 					ConsoleColors.RESET, message
 				),
 				Level.INFO
 			);
 	}
 
 	// Broadcasts the supplied string to all players on the event the output handler has
 	@Override
 	public void outputToServer(String message)
 	{
 		if (this.hasServer())
 		{
 			this.serverOutput.broadcastMessage(message);
 		}
 	}
 
 	@Override
 	public void broadcastColoured(String message)
 	{
 		outputToServer(ChatColour.ToMinecraft(message));
 	}
 
 	@Override
 	public void broadcastColoured(String format, Object... params)
 	{
 		broadcastColoured(String.format(format, params));
 	}
 
 	// Gets the current debug output level
 	@Override
 	public Level getDebugLevel()
 	{
 		return this.debugLevel;
 	}
 
 	// Sets the debug output level
 	@Override
 	public void setDebugLevel(Level level)
 	{
 		this.debugLevel = level;
 	}
 
 	@Override
 	public void write(String message)
 	{
 		outputToConsole(message);
 	}
 
 	@Override
 	public void severe(String message)
 	{
 		outputDebugToConsole(message, Level.SEVERE);
 	}
 
 	@Override
 	public void warning(String message)
 	{
 		outputDebugToConsole(message, Level.WARNING);
 	}
 
 	@Override
 	public void info(String message)
 	{
 		outputDebugToConsole(message, Level.INFO);
 	}
 
 	@Override
 	public void config(String message)
 	{
 		outputDebugToConsole(message, Level.CONFIG);
 	}
 
 	@Override
 	public void fine(String message)
 	{
 		outputDebugToConsole(message, Level.FINE);
 	}
 
 	@Override
 	public void finer(String message)
 	{
 		outputDebugToConsole(message, Level.FINER);
 	}
 
 	@Override
 	public void finest(String message)
 	{
 		outputDebugToConsole(message, Level.FINEST);
 	}
 
 }
