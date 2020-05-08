 /*
  * Copyright 2010 NCHOVY
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.log.api.impl;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.araqne.api.Script;
 import org.araqne.api.ScriptArgument;
 import org.araqne.api.ScriptContext;
 import org.araqne.api.ScriptOptionParser;
 import org.araqne.api.ScriptOptionParser.ScriptOption;
 import org.araqne.api.ScriptUsage;
 import org.araqne.log.api.*;
 
 import com.bethecoder.ascii_table.ASCIITable;
 import com.bethecoder.ascii_table.impl.CollectionASCIITableAware;
 import com.bethecoder.ascii_table.impl.PropertyColumn;
 
 public class LogApiScript implements Script {
 	private final org.slf4j.Logger slog = org.slf4j.LoggerFactory.getLogger(LogApiScript.class.getName());
 	private ScriptContext context;
 	private LoggerFactoryRegistry loggerFactoryRegistry;
 	private LoggerRegistry loggerRegistry;
 	private LogParserFactoryRegistry parserFactoryRegistry;
 	private LogNormalizerFactoryRegistry normalizerFactoryRegistry;
 	private LogTransformerFactoryRegistry transformerFactoryRegistry;
 	private LogParserRegistry parserRegistry;
 	private LogNormalizerRegistry normalizerRegistry;
 	private LogTransformerRegistry transformerRegistry;
 
 	public LogApiScript(LoggerFactoryRegistry loggerFactoryRegistry, LoggerRegistry loggerRegistry,
 			LogParserFactoryRegistry parserFactoryRegistry, LogNormalizerFactoryRegistry normalizerFactoryRegistry,
 			LogTransformerFactoryRegistry transformerFactoryRegistry, LogParserRegistry parserRegistry,
 			LogNormalizerRegistry normalizerRegistry, LogTransformerRegistry transformerRegistry) {
 		this.loggerFactoryRegistry = loggerFactoryRegistry;
 		this.loggerRegistry = loggerRegistry;
 		this.parserFactoryRegistry = parserFactoryRegistry;
 		this.normalizerFactoryRegistry = normalizerFactoryRegistry;
 		this.transformerFactoryRegistry = transformerFactoryRegistry;
 		this.parserRegistry = parserRegistry;
 		this.normalizerRegistry = normalizerRegistry;
 		this.transformerRegistry = transformerRegistry;
 	}
 
 	@Override
 	public void setScriptContext(ScriptContext context) {
 		this.context = context;
 	}
 
 	/**
 	 * @since 2.6.0
 	 */
 	@ScriptUsage(description = "print last log of loggers", arguments = {
 			@ScriptArgument(name = "name filter", type = "string", description = "name filter", optional = true) })
 	public void lastLogs(String[] args) {
 		String filter = null;
 		if (args.length > 0)
 			filter = args[0];
 
 		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");
 		for (Logger logger : loggerRegistry.getLoggers()) {
 			if (logger.getLastLog() == null)
 				continue;
 
 			if (filter != null && !logger.getFullName().contains(filter))
 				continue;
 
 			context.println("----");
 			context.println("Logger [" + logger.getFullName() + "] Last Timestamp [" + df.format(logger.getLastLogDate()) + "]");
 			context.println(logger.getLastLog().getParams());
 		}
 	}
 
 	public void parsers(String[] args) {
 		context.println("Log Parser Profiles");
 		context.println("---------------------");
 		for (LogParserProfile profile : parserRegistry.getProfiles()) {
 			context.println(profile);
 		}
 	}
 
 	@ScriptUsage(description = "create parser profile", arguments = {
 			@ScriptArgument(name = "profile name", type = "string", description = "parser profile name"),
 			@ScriptArgument(name = "factory name", type = "string", description = "parser factory name", autocompletion = LogParserFactoryAutoCompleter.class) })
 	public void createParser(String[] args) throws InterruptedException {
 		String name = args[0];
 		String factoryName = args[1];
 
 		LogParserFactory parserFactory = parserFactoryRegistry.get(factoryName);
 		if (parserFactory == null) {
 			context.println("parser factory not found");
 			return;
 		}
 
 		LogParserProfile profile = new LogParserProfile();
 		profile.setName(name);
 		profile.setFactoryName(factoryName);
 
 		Map<String, String> configs = profile.getConfigs();
 		for (LoggerConfigOption option : parserFactory.getConfigOptions()) {
 			setOption(configs, option);
 			profile.setConfigs(configs);
 		}
 
 		parserRegistry.createProfile(profile);
 		context.println("created");
 	}
 
 	@ScriptUsage(description = "remove parser profile", arguments = { @ScriptArgument(name = "profile name", type = "string", description = "profile name") })
 	public void removeParser(String[] args) {
 		parserRegistry.removeProfile(args[0]);
 		context.println("removed");
 	}
 
 	public void normalizers(String[] args) {
 		context.println("Log Normalizer Profiles");
 		context.println("-------------------------");
 		for (LogNormalizerProfile profile : normalizerRegistry.getProfiles()) {
 			context.println(profile);
 		}
 	}
 
 	@ScriptUsage(description = "print transformer profiles", arguments = {
 			@ScriptArgument(name = "name", type = "string", description = "filter by name", optional = true)
 	})
 	public void transformers(String[] args) {
 		String filter = null;
 		String filtered = "";
 		if (args.length > 0) {
 			filter = args[0];
 			filtered = " (filtered)";
 		}
 
 		context.println("Log Transformer Profiles" + filtered);
 		context.println("--------------------------");
 		List<LogTransformerProfile> profiles = transformerRegistry.getProfiles();
 		Collections.sort(profiles);
 		for (LogTransformerProfile profile : profiles) {
 			if (filter != null && !profile.getName().contains(filter))
 				continue;
 
 			context.println(profile);
 		}
 	}
 
 	@ScriptUsage(description = "create transformer profile", arguments = {
 			@ScriptArgument(name = "profile name", type = "string", description = "transformer profile name"),
 			@ScriptArgument(name = "factory name", type = "string", description = "transformer factory name", autocompletion = LogTransformerFactoryRegistry.class) })
 	public void createTransformer(String[] args) throws InterruptedException {
 		String name = args[0];
 		String factoryName = args[1];
 
 		LogTransformerProfile profile = new LogTransformerProfile();
 		profile.setName(name);
 		profile.setFactoryName(factoryName);
 
 		LogTransformerFactory factory = transformerFactoryRegistry.getFactory(factoryName);
 		if (factory == null) {
 			context.println("transformer factory not found");
 			return;
 		}
 
 		Map<String, String> configs = profile.getConfigs();
 		for (LoggerConfigOption option : factory.getConfigOptions()) {
 			setOption(configs, option);
 		}
 
 		transformerRegistry.createProfile(profile);
 		context.println("created");
 	}
 
 	@ScriptUsage(description = "remove transformer profile", arguments = { @ScriptArgument(name = "profile name", type = "string", description = "profile name") })
 	public void removeTransformer(String[] args) {
 		transformerRegistry.removeProfile(args[0]);
 		context.println("removed");
 	}
 
 	public void normalize(String[] args) {
 		try {
 			context.print("Normalizer Name? ");
 			String normalizerName = context.readLine();
 			LogNormalizerFactory factory = normalizerFactoryRegistry.get(normalizerName);
 			if (factory == null) {
 				context.println("normalizer not found");
 				return;
 			}
 
 			LogNormalizer normalizer = factory.createNormalizer(new HashMap<String, String>());
 
 			Map<String, Object> params = getParams();
 			Map<String, Object> m = normalizer.normalize(params);
 			context.println("---------------------");
 			for (String key : m.keySet()) {
 				context.println(key + ": " + m.get(key));
 			}
 		} catch (InterruptedException e) {
 			context.println("");
 			context.println("interrupted");
 		}
 	}
 
 	@ScriptUsage(description = "create normalizer profile", arguments = {
 			@ScriptArgument(name = "profile name", type = "string", description = "profile name"),
 			@ScriptArgument(name = "factory name", type = "string", description = "normalizer factory name") })
 	public void createNormalizer(String[] args) throws InterruptedException {
 		String name = args[0];
 		String factoryName = args[1];
 
 		LogNormalizerProfile profile = new LogNormalizerProfile();
 		profile.setName(name);
 		profile.setFactoryName(factoryName);
 
 		LogNormalizerFactory factory = normalizerFactoryRegistry.get(factoryName);
 		if (factory == null) {
 			context.println("normalizer factory not found");
 			return;
 		}
 
 		Map<String, String> configs = profile.getConfigs();
 		for (LoggerConfigOption option : factory.getConfigOptions()) {
 			setOption(configs, option);
 		}
 
 		normalizerRegistry.createProfile(profile);
 
 		context.println("created");
 	}
 
 	public void removeNormalizer(String[] args) {
 		normalizerRegistry.removeProfile(args[0]);
 		context.println("removed");
 	}
 
 	public static class LoggerFactoryListItem implements Comparable<LoggerFactoryListItem> {
 		private LoggerFactory loggerFactory;
 
 		public LoggerFactoryListItem(LoggerFactory loggerFactory) {
 			this.loggerFactory = loggerFactory;
 		}
 
 		public String getFullName() {
 			return loggerFactory.getFullName();
 		}
 
 		public String getName() {
 			return loggerFactory.getName();
 		}
 
 		public String getDisplayName() {
 			return loggerFactory.getDisplayName(Locale.ENGLISH);
 		}
 
 		public String getDescription() {
 			return loggerFactory.getDescription(Locale.ENGLISH);
 		}
 
 		@Override
 		public int compareTo(LoggerFactoryListItem o) {
 			return loggerFactory.getFullName().compareTo(o.getFullName());
 		}
 	}
 
 	public void loggerFactories(String[] args) {
 		ScriptOptionParser sop = new ScriptOptionParser(args);
 		ScriptOption verbOpt = sop.getOption("v", "verbose", false);
 		List<String> argl = sop.getArguments();
 
 		String filtered = "";
 		if (!argl.isEmpty())
 			filtered = " (filtered)";
 
 		context.println("Logger Factories" + filtered);
 		context.println("---------------------");
 
 		Collection<LoggerFactory> loggerFactories = loggerFactoryRegistry.getLoggerFactories();
 		List<LoggerFactoryListItem> filteredList = new ArrayList<LoggerFactoryListItem>(loggerFactories.size());
 		for (LoggerFactory loggerFactory : loggerFactories) {
 			if (argl.isEmpty())
 				filteredList.add(new LoggerFactoryListItem(loggerFactory));
 			else if (containsTokens(loggerFactory.getFullName(), argl))
 				filteredList.add(new LoggerFactoryListItem(loggerFactory));
 		}
 
 		Collections.sort(filteredList);
 
 		if (verbOpt != null) {
 			context.println(ASCIITable.getInstance().getTable(
 					new CollectionASCIITableAware<LoggerFactoryListItem>(filteredList, Arrays.asList("fullName", "displayName",
 							"description"), Arrays.asList("l!factory name", "l!display name", "l!description"))));
 		} else {
 			context.println(ASCIITable.getInstance().getTable(
 					new CollectionASCIITableAware<LoggerFactoryListItem>(filteredList, Arrays.asList("fullName", "displayName"), Arrays
 							.asList("l!factory name", "l!display name"))));
 		}
 	}
 
 	public static class LoggerListItem implements Comparable<LoggerListItem> {
 
 		private Logger logger;
 
 		public LoggerListItem(Logger logger) {
 			this.logger = logger;
 		}
 
 		@Override
 		public int compareTo(LoggerListItem o) {
 			return logger.getFullName().compareTo(o.logger.getFullName());
 		}
 
 		// "name", "factoryFullName", "Running", "interval", "logCount",
 		// "lastStartDate", "lastRunDate",
 		public String getName() {
 			return logger.getName();
 		}
 
 		public String getFullName() {
 			return logger.getFullName();
 		}
 
 		public String getFactoryName() {
 			return logger.getFactoryName();
 		}
 
 		public String getFactoryFullName() {
 			return logger.getFactoryFullName();
 		}
 
 		public String getStatus() {
 			return logger.isRunning() ? "running" : "stopped";
 		}
 
 		public int getInterval() {
 			return logger.getInterval();
 		}
 
 		public long getLogCount() {
 			return logger.getLogCount();
 		}
 
 		public long getDropCount() {
 			return logger.getDropCount();
 		}
 
 		public Date getLastStartDate() {
 			return logger.getLastStartDate();
 		}
 
 		public Date getLastRunDate() {
 			return logger.getLastRunDate();
 
 		}
 
 		public Date getLastLogDate() {
 			return logger.getLastLogDate();
 		}
 
 	}
 
 	public void loggers(String[] args) {
 		ScriptOptionParser sop = new ScriptOptionParser(args);
 		ScriptOption verbOpt = sop.getOption("v", "verbose", false);
 		ScriptOption fullVerbOpt = sop.getOption("V", "full-verbose", false);
 		ScriptOption factFilter = sop.getOption("f", "factory", true);
 
 		List<String> argl = sop.getArguments();
 		String filtered = "";
 		if (!argl.isEmpty())
 			filtered = " (filtered)";
 
 		context.println("Loggers" + filtered);
 		context.println("----------------------");
 
 		List<LoggerListItem> filteredList = new ArrayList<LoggerListItem>(loggerRegistry.getLoggers().size());
 		for (Logger logger : loggerRegistry.getLoggers()) {
 			if (argl.size() == 0 && factFilter == null)
 				filteredList.add(new LoggerListItem(logger));
 			else {
 				boolean matches = true;
 				if (argl.size() > 0 && !containsTokens(logger.getFullName(), argl))
 					matches = false;
 				if (factFilter != null && !containsTokens(logger.getFactoryFullName(), factFilter.values))
 					matches = false;
 				if (matches)
 					filteredList.add(new LoggerListItem(logger));
 			}
 		}
 
 		if (filteredList.isEmpty())
 			return;
 
 		Collections.sort(filteredList);
 
 		if (fullVerbOpt != null) {
 			for (LoggerListItem logger : filteredList) {
 				context.println(logger.toString());
 			}
 		} else if (verbOpt != null)
 			context.println(ASCIITable.getInstance().getTable(
 					new CollectionASCIITableAware<LoggerListItem>(filteredList, new PropertyColumn("fullName", "l!name"),
 							new PropertyColumn("factoryFullName", "l!factory"), new PropertyColumn("status", "l!status"),
 							new PropertyColumn("interval", "intvl.(ms)"), new PropertyColumn("logCount", "log count"),
 							new PropertyColumn("dropCount", "drop"), new PropertyColumn("lastStartDate", "l!last start"),
 							new PropertyColumn("lastRunDate", "l!last run"), new PropertyColumn("lastLogDate", "l!last log"))));
 		else
 			context.println(ASCIITable.getInstance().getTable(
 					new CollectionASCIITableAware<LoggerListItem>(filteredList, new PropertyColumn("fullName", "l!name"),
 							new PropertyColumn("factoryName", "l!factory"), new PropertyColumn("status", "l!status"),
 							new PropertyColumn("interval", "intvl.(ms)"), new PropertyColumn("logCount", "log count"),
 							new PropertyColumn("lastLogDate", "l!last log"))));
 
 	}
 
 	private boolean containsTokens(String fullName, List<String> args) {
 		if (fullName == null)
 			return false;
 		for (String arg : args) {
 			if (!fullName.contains(arg))
 				return false;
 		}
 		return true;
 	}
 
 	@ScriptUsage(description = "print logger configuration", arguments = { @ScriptArgument(name = "logger fullname", type = "string", description = "logger fullname") })
 	public void logger(String[] args) {
 		String fullName = args[0];
 		context.println("Logger [" + fullName + "]");
 		printLine(fullName.length() + 10);
 		Logger logger = loggerRegistry.getLogger(fullName);
 		if (logger == null) {
 			context.println("logger not found");
 			return;
 		}
 
 		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		String lastLogDate = logger.getLastLogDate() != null ? dateFormat.format(logger.getLastLogDate()) : "N/A";
 		String lastRunDate = logger.getLastRunDate() != null ? dateFormat.format(logger.getLastRunDate()) : "N/A";
 
 		context.println(" * Description: " + logger.getDescription());
 		context.println(" * Logger Factory: " + logger.getFactoryFullName());
 		context.println(" * Status: " + logger.getStatus());
 		context.println(" * Interval: " + logger.getInterval() + "ms");
 		context.println(" * Last Log: " + lastLogDate);
 		context.println(" * Last Run: " + lastRunDate);
 		context.println(" * Log Count: " + logger.getLogCount());
 		context.println(" * Drop Count: " + logger.getDropCount());
 		context.println("");
 
 		context.println("Configuration");
 		context.println("---------------");
 		Map<String, String> props = logger.getConfig();
 		if (props != null) {
 			for (Object key : props.keySet())
 				context.println(" * " + key + ": " + props.get(key));
 		}
 	}
 
 	private void printLine(int len) {
 		for (int i = 0; i < len; i++)
 			context.print('-');
 		context.println();
 	}
 
 	@ScriptUsage(description = "print log parser factories", arguments = {
 			@ScriptArgument(name = "name filter", type = "string", description = "filter by factory name", optional = true) })
 	public void parserFactories(String[] args) {
 		String filter = null;
 		String filtered = "";
 		if (args.length > 0) {
 			filter = args[0];
 			filtered = " (filtered)";
 		}
 
 		context.println("Log Parser Factories" + filtered);
 		context.println("----------------------");
 
 		for (String name : parserFactoryRegistry.getNames()) {
 			if (filter != null && !name.contains(filter))
 				continue;
 			context.println(name);
 		}
 	}
 
 	@ScriptUsage(description = "print log normalizer factories", arguments = {
 			@ScriptArgument(name = "name filter", type = "string", description = "filter by factory name", optional = true) })
 	public void normalizerFactories(String[] args) {
 		String filter = null;
 		String filtered = "";
 		if (args.length > 0) {
 			filter = args[0];
 			filtered = " (filtered)";
 		}
 
 		context.println("Log Normalizers" + filtered);
 		context.println("---------------------");
 
 		for (String name : normalizerFactoryRegistry.getNames()) {
 			if (filter != null && !name.contains(filter))
 				continue;
 			context.println(name);
 		}
 	}
 
 	@ScriptUsage(description = "print log transformer factories", arguments = {
 			@ScriptArgument(name = "name filter", type = "string", description = "filter by factory name", optional = true) })
 	public void transformerFactories(String[] args) {
 		String filter = null;
 		String filtered = "";
 		if (args.length > 0) {
 			filter = args[0];
 			filtered = " (filtered)";
 		}
 
 		context.println("Log Transformer Factories" + filtered);
 		context.println("---------------------------");
 
 		for (LogTransformerFactory f : transformerFactoryRegistry.getFactories()) {
 			if (filter != null && !f.getName().contains(filter))
 				continue;
 			context.println(f.getName() + ": " + f);
 		}
 	}
 
 	@ScriptUsage(description = "trace logger output", arguments = { @ScriptArgument(name = "logger name", type = "string", description = "logger fullname") })
 	public void trace(String[] args) {
 		Logger logger = loggerRegistry.getLogger(args[0]);
 		if (logger == null) {
 			context.println("logger not found");
 			return;
 		}
 
 		ConsoleLogPipe p = new ConsoleLogPipe();
 		logger.addLogPipe(p);
 
 		try {
 			context.println("tracing logger: " + logger);
 			while (true) {
 				context.readLine();
 			}
 		} catch (InterruptedException e) {
 			context.println("interrupted");
 		} finally {
 			logger.removeLogPipe(p);
 		}
 	}
 
 	private class ConsoleLogPipe implements LogPipe {
 		@Override
 		public void onLog(Logger logger, Log log) {
 			context.println(logger.getFullName() + ": " + log.toString());
 		}
 	}
 
 	@ScriptUsage(description = "start the logger", arguments = {
 			@ScriptArgument(name = "logger fullname", type = "string", description = "the logger fullname to start", autocompletion = LoggerAutoCompleter.class),
 			@ScriptArgument(name = "interval", type = "int", description = "sleep time of active logger thread in milliseconds. 60000ms by default. passive logger will ignore interval", optional = true) })
 	public void startLogger(String[] args) {
 		try {
 			String fullName = args[0];
 			int interval = 0;
 			if (args.length > 1)
 				interval = Integer.parseInt(args[1]);
 
 			Logger logger = loggerRegistry.getLogger(fullName);
 			if (logger == null) {
 				context.println("logger not found");
 				return;
 			}
 
 			if (logger.isPassive())
 				logger.start();
 			else if (interval > 0)
 				logger.start(interval);
 			else
 				throw new IllegalStateException("cannot start logger, interval is required");
 			context.println("logger started");
 		} catch (NumberFormatException e) {
 			context.println("interval should be number in milliseconds");
 		} catch (IllegalStateException e) {
 			if (e.getMessage().contains("pending"))
 				context.println("cannot start logger, " + e.getMessage());
 			else
 				context.println(e.getMessage());
 		}
 	}
 
 	public void startLoggers(String[] args) {
 		int interval = 60000;
 		int loggerCnt = args.length;
 		try {
 			interval = Integer.parseInt(args[args.length - 1]);
 			loggerCnt = args.length - 1;
 		} catch (NumberFormatException e) {
 			// ignore
 		}
 
 		for (int i = 0; i < loggerCnt; ++i) {
 			try {
 				Logger logger = loggerRegistry.getLogger(args[i]);
 				if (logger == null) {
					context.println("logger not found");
 					continue;
 				}
 
 				if (logger.isPassive())
 					logger.start();
 				else
 					logger.start(interval);
				context.println("logger started");
 
 			} catch (IllegalStateException e) {
 				context.println(e.getMessage());
 			}
 		}
 	}
 
 	@ScriptUsage(description = "stop the logger", arguments = {
			@ScriptArgument(name = "logger name", type = "string", description = "the logger name to stop", autocompletion = LoggerAutoCompleter.class),
			@ScriptArgument(name = "max wait time", type = "int", description = "max wait time in milliseconds", optional = true) })
 	public void stopLogger(String[] args) {
 		try {
 			int maxWaitTime = 5000;
 			String name = args[0];
 			if (args.length > 1)
 				maxWaitTime = Integer.parseInt(args[1]);
 
 			Logger logger = loggerRegistry.getLogger(name);
 			if (logger == null) {
 				context.println("logger not found");
 				return;
 			}
 
 			if (!logger.isPassive())
 				context.println("waiting...");
 
 			logger.stop(maxWaitTime);
 			context.println("logger stopped");
 		} catch (Exception e) {
 			context.println(e.getMessage());
 		}
 	}
 
 	public void stopLoggers(String[] args) {
 		int maxWaitTime = 5000;
 		int loggerCnt = args.length;
 		try {
 			maxWaitTime = Integer.parseInt(args[args.length - 1]);
 			loggerCnt = args.length - 1;
 		} catch (NumberFormatException e) {
 			// ignore
 		}
 
 		for (int i = 0; i < loggerCnt; ++i) {
 			Logger logger = loggerRegistry.getLogger(args[i]);
 			if (logger == null) {
				context.println("logger not found");
 				continue;
 			}
 
 			if (!logger.isPassive())
				context.println("waiting...");
 
 			logger.stop(maxWaitTime);
			context.println("logger stopped");
 		}
 	}
 
 	@ScriptUsage(description = "create new logger", arguments = {
 			@ScriptArgument(name = "logger factory name", type = "string", description = "logger factory name. try logapi.loggerFactories command.", autocompletion = LoggerFactoryAutoCompleter.class),
 			@ScriptArgument(name = "logger namespace", type = "string", description = "new logger namespace"),
 			@ScriptArgument(name = "logger name", type = "string", description = "new logger name"),
 			@ScriptArgument(name = "description", type = "string", description = "the description of new logger", optional = true) })
 	public void createLogger(String[] args) {
 		try {
 			String loggerFactoryName = args[0];
 			String loggerNamespace = args[1];
 			String loggerName = args[2];
 			String description = (args.length > 3) ? args[3] : null;
 
 			LoggerFactory loggerFactory = loggerFactoryRegistry.getLoggerFactory(loggerFactoryName);
 			if (loggerFactory == null) {
 				context.println("logger factory not found: " + loggerFactoryName);
 				return;
 			}
 
 			Map<String, String> config = new HashMap<String, String>();
 			for (LoggerConfigOption type : loggerFactory.getConfigOptions()) {
 				setOption(config, type);
 			}
 
 			// transform?
 			context.print("transformer (optional, enter to skip)? ");
 			String transformerName = context.readLine().trim();
 			if (!transformerName.isEmpty())
 				config.put("transformer", transformerName);
 
 			LoggerSpecification spec = new LoggerSpecification();
 			spec.setNamespace(loggerNamespace);
 			spec.setName(loggerName);
 			spec.setDescription(description);
 			spec.setConfig(config);
 
 			Logger logger = loggerFactory.newLogger(spec);
 			if (logger == null) {
 				context.println("failed to create logger");
 				return;
 			}
 
 			context.println("logger created: " + logger.toString());
 		} catch (InterruptedException e) {
 			context.println("interrupted");
 		} catch (Exception e) {
 			context.println(e.getMessage());
 			slog.error("araqne log api: cannot create logger", e);
 		}
 	}
 
 	@ScriptUsage(description = "remove logger", arguments = {
 			@ScriptArgument(name = "logger fullname", type = "string", description = "the logger fullname", autocompletion = LoggerAutoCompleter.class) })
 	public void removeLogger(String[] args) {
 		try {
 			String fullName = args[0];
 			Logger logger = loggerRegistry.getLogger(fullName);
 
 			if (logger == null) {
 				context.println("logger not found");
 				return;
 			}
 
 			// stop logger
 			logger.stop();
 
 			String[] tokens = fullName.split("\\\\");
 
 			LoggerFactory factory = loggerFactoryRegistry.getLoggerFactory(logger.getFactoryNamespace(), logger.getFactoryName());
 			// factory can already removed from registry
 			if (factory != null)
 				factory.deleteLogger(tokens[0], tokens[1]);
 
 			context.println("logger removed");
 		} catch (Exception e) {
 			context.println("error: " + e.getMessage());
 			slog.error("araqne log api: cannot remove logger", e);
 		}
 	}
 
 	private void setOption(Map<String, String> config, LoggerConfigOption type) throws InterruptedException {
 		String directive = type.isRequired() ? "(required)" : "(optional)";
 		context.print(type.getDisplayName(Locale.ENGLISH) + " " + directive + "? ");
 		String value = context.readLine();
 		if (!value.isEmpty())
 			config.put(type.getName(), value);
 
 		if (value.isEmpty() && type.isRequired()) {
 			setOption(config, type);
 		}
 	}
 
 	private Map<String, Object> getParams() throws InterruptedException {
 		Map<String, Object> params = new HashMap<String, Object>();
 
 		while (true) {
 			context.print("Key (press enter to end): ");
 			String key = context.readLine();
 			if (key == null || key.isEmpty())
 				break;
 
 			context.print("Value: ");
 			String value = context.readLine();
 
 			params.put(key, value);
 		}
 		return params;
 	}
 }
