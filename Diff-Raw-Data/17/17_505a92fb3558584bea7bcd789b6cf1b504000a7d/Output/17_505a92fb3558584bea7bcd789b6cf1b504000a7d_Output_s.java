 package no.runsafe.framework.internal;
 
 import no.runsafe.framework.api.IDebug;
 import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;
 import no.runsafe.framework.text.ChatColour;
 import no.runsafe.framework.text.ConsoleColour;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public abstract class Output implements IDebug
 {
 	Output(Logger consoleLog)
 	{
 		this.consoleLog = consoleLog;
 		this.debugLevel = DefaultDebugLevel;
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
 
 	// Sends the supplied String to the console/log the output handler has
 	@Override
 	public void outputToConsole(String message)
 	{
 		outputToConsole(message, Level.INFO);
 	}
 
 	@Override
 	public void write(String message)
 	{
 		outputToConsole(message);
 	}
 
 	@Override
 	public void logException(Exception exception)
 	{
 		writeColoured(
 			"Exception caught: &c%s&r\n%s",
 			Level.SEVERE,
 			ExceptionUtils.getMessage(exception),
 			ExceptionUtils.getStackTrace(exception)
 		);
 	}
 
 	@Override
 	public void logWarning(String message, Object... params)
 	{
 		outputToConsole(ChatColour.ToConsole("&e" + String.format(message.replace("&r", "&e"), params) + "&r"), Level.WARNING);
 	}
 
 	@Override
 	public void logError(String message, Object... params)
 	{
 		outputToConsole(ChatColour.ToConsole("&4" + String.format(message.replace("&r", "&4"), params) + "&r"), Level.SEVERE);
 	}
 
 	/**
 	 * This will log a fatal error and make the server die in a great big fireball.
 	 *
 	 * @param message The message to print before exiting the process.
 	 * @param params  Values to be passed into the message using String.format
 	 */
 	@Override
 	public void logFatal(String message, Object... params)
 	{
 		String formatted = String.format(message, params);
 		String border = StringUtils.repeat("=", formatted.length());
 		writeColoured("\n\n&4&l%1$s\n%2$s\n%1$s&r", Level.SEVERE, border, formatted);
 		System.exit(1);
 	}
 
 	@Override
 	public void logInformation(String message, Object... params)
 	{
 		outputToConsole(ChatColour.ToConsole("&2" + String.format(message.replace("&r", "&2"), params) + "&r"), Level.INFO);
 	}
 
 	// Sends the supplied String with the supplied logging level to the console/log the output handler has
 	@Override
 	public void outputToConsole(String message, Level level)
 	{
 		this.consoleLog.log(level, message);
 	}
 
 	// Sends the supplied String to the console/log the output handler has if the debug level is high enough
 	@Override
 	public void outputDebugToConsole(String message, Level messageLevel, Object... params)
 	{
 		if (debugLevel != null && messageLevel.intValue() >= debugLevel.intValue())
 			outputToConsole(formatDebugMessage(message, messageLevel, params), Level.INFO);
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
 	public void severe(String message, Object... params)
 	{
 		outputDebugToConsole(message, Level.SEVERE, params);
 	}
 
 	@Override
 	public void warning(String message, Object... params)
 	{
 		outputDebugToConsole(message, Level.WARNING, params);
 	}
 
 	@Override
 	public void info(String message, Object... params)
 	{
 		outputDebugToConsole(message, Level.INFO, params);
 	}
 
 	@Override
 	public void config(String message, Object... params)
 	{
 		outputDebugToConsole(message, Level.CONFIG, params);
 	}
 
 	@Override
 	public void fine(String message, Object... params)
 	{
 		outputDebugToConsole(message, Level.FINE, params);
 	}
 
 	@Override
 	public void finer(String message, Object... params)
 	{
 		outputDebugToConsole(message, Level.FINER, params);
 	}
 
 	@Override
 	public void finest(String message, Object... params)
 	{
 		outputDebugToConsole(message, Level.FINEST, params);
 	}
 
 	@Override
 	public void dumpData(Object raw, Level messageLevel)
 	{
 		if (debugLevel != null && messageLevel.intValue() >= debugLevel.intValue())
 			if (raw instanceof RunsafeMeta)
 				dumpData(((RunsafeMeta) raw).getRaw());
 	}
 
 	private String formatDebugMessage(String message, Level messageLevel, Object... params)
 	{
 		String formatted = String.format(
 			"[%s%s%s] %s",
 			ConsoleColour.DARK_GREEN,
 			messageLevel.getName(),
 			ConsoleColour.RESET,
 			String.format(message, params)
 		);
 
 		if (messageLevel.intValue() <= Level.FINEST.intValue())
 			formatted = String.format("%s\nat %s", formatted, getStackTrace());
 
 		return formatted;
 	}
 
 	private String getStackTrace()
 	{
 		int skip = 5;
 		List<String> stack = new ArrayList<String>();
 		for (StackTraceElement element : Thread.currentThread().getStackTrace())
 		{
 			if (skip < 1)
 				stack.add(element.toString());
 			else
 				skip--;
 		}
 		return StringUtils.join(stack, "\n\t");
 	}
 
 	private void dumpData(ConfigurationSerializable raw)
 	{
 		outputToConsole(String.format("Dumping instance of %s", raw.getClass().getCanonicalName()));
 		Map<String, Object> values = raw.serialize();
 		for (String key : values.keySet())
 			outputToConsole(String.format(" - %s: %s", key, values.get(key)));
 	}
 
 	private final Logger consoleLog;
 	private Level debugLevel;
 
 	static private final Level DefaultDebugLevel;
 
 	static
 	{
 		File configFile = new File("runsafe/output.yml");
 		YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
 		if (!config.contains("debug"))
 			config.set("debug", "OFF");
 		DefaultDebugLevel = Level.parse(config.getString("debug").toUpperCase());
 	}
 }
