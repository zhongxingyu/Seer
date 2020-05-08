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
 
 public class LogApiScript implements Script {
 	private final org.slf4j.Logger slog = org.slf4j.LoggerFactory.getLogger(LogApiScript.class.getName());
 	private ScriptContext context;
 	private LoggerFactoryRegistry loggerFactoryRegistry;
 	private LoggerRegistry loggerRegistry;
 	private LogParserFactoryRegistry parserFactoryRegistry;
 	private LogNormalizerFactoryRegistry normalizerRegistry;
 	private LogTransformerFactoryRegistry transformerRegistry;
 
 	public LogApiScript(LoggerFactoryRegistry loggerFactoryRegistry, LoggerRegistry loggerRegistry,
 			LogParserFactoryRegistry parserFactoryRegistry, LogNormalizerFactoryRegistry normalizerRegistry,
 			LogTransformerFactoryRegistry transformerRegistry) {
 		this.loggerFactoryRegistry = loggerFactoryRegistry;
 		this.loggerRegistry = loggerRegistry;
 		this.parserFactoryRegistry = parserFactoryRegistry;
 		this.normalizerRegistry = normalizerRegistry;
 		this.transformerRegistry = transformerRegistry;
 	}
 
 	@Override
 	public void setScriptContext(ScriptContext context) {
 		this.context = context;
 	}
 
 	public void normalizers(String[] args) {
 		context.println("Log Normalizers");
 		context.println("---------------------");
 
 		for (String name : normalizerRegistry.getNames()) {
 			context.println(name);
 		}
 	}
 
 	public void normalize(String[] args) {
 		try {
 			context.print("Normalizer Name? ");
 			String normalizerName = context.readLine();
 			LogNormalizerFactory factory = normalizerRegistry.get(normalizerName);
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
 
 	public void loggerFactories(String[] args) {
 		context.println("Logger Factories");
 		context.println("---------------------");
 
 		for (LoggerFactory loggerFactory : loggerFactoryRegistry.getLoggerFactories()) {
 			context.println(loggerFactory.toString());
 		}
 	}
 
 	public void loggers(String[] args) {
 		context.println("Loggers");
 		context.println("----------------------");
 
 		ScriptOptionParser sop = new ScriptOptionParser(args);
 		ScriptOption verbOpt = sop.getOption("v", "verbose", false);
 		ScriptOption fullVerbOpt = sop.getOption("V", "full-verbose", false);
 		ScriptOption factFilter = sop.getOption("f", "factory", true);
 		
 		List<String> argl = sop.getArguments();
 
 		List<Logger> filtered = new ArrayList<Logger>(loggerRegistry.getLoggers().size());
 		for (Logger logger : loggerRegistry.getLoggers()) {
			if (argl.size() == 0 && factFilter == null)  
 				filtered.add(logger);
 			else {
 				boolean matches = true;
 				if (argl.size() > 0 && !containsTokens(logger.getFullName(), argl))
 					matches = false;
 				if (factFilter != null && !containsTokens(logger.getFactoryFullName(), factFilter.values))
 					matches = false;
 				if (matches)
 					filtered.add(logger);
 			}
 		}
 
 		if (filtered.isEmpty())
 			return;
 		
 		if (fullVerbOpt != null) {
 			for (Logger logger : filtered) {
 				context.println(logger.toString());
 			}
 		}
 		else if (verbOpt != null)
 			context.println(ASCIITable.getInstance().getTable(
 					new CollectionASCIITableAware<Logger>(filtered,
 							Arrays.asList("name", "factoryFullName", "Running", "interval", "logCount", "lastStartDate", "lastRunDate", "lastLogDate"),
 							Arrays.asList("name", "factory", "running", "intvl.(ms)", "log count", "last start", "last run", "last log"))));
 		else
 			context.println(ASCIITable.getInstance().getTable(
 					new CollectionASCIITableAware<Logger>(filtered,
 							Arrays.asList("name", "factoryName", "Running", "interval", "logCount", "lastLogDate"),
 							Arrays.asList("name", "factory", "running", "intvl.(ms)", "log count", "last log"))));
 			
 	}
 
 	private boolean containsTokens(String fullName, List<String> args) {
 		if (fullName == null)
 			return false;
 		for (String arg: args) {
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
 
 	public void parserFactories(String[] args) {
 		context.println("Log Parser Factories");
 		context.println("----------------------");
 
 		for (String name : parserFactoryRegistry.getNames()) {
 			context.println(name);
 		}
 	}
 
 	public void transformerFactories(String[] args) {
 		context.println("Log Transformer Factories");
 		context.println("---------------------------");
 
 		for (LogTransformerFactory f : transformerRegistry.getFactories()) {
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
 			@ScriptArgument(name = "logger fullname", type = "string", description = "the logger fullname to start"),
 			@ScriptArgument(name = "interval", type = "int", description = "sleep time of active logger thread in milliseconds. 60000ms by default. passive logger will ignore interval", optional = true) })
 	public void startLogger(String[] args) {
 		try {
 			String fullName = args[0];
 			int interval = 60000;
 			if (args.length > 1)
 				interval = Integer.parseInt(args[1]);
 
 			Logger logger = loggerRegistry.getLogger(fullName);
 			if (logger == null) {
 				context.println("logger not found");
 				return;
 			}
 
 			if (logger.isPassive())
 				logger.start();
 			else
 				logger.start(interval);
 			context.println("logger started");
 		} catch (NumberFormatException e) {
 			context.println("interval should be number in milliseconds");
 		} catch (IllegalStateException e) {
 			context.println(e.getMessage());
 		}
 	}
 
 	@ScriptUsage(description = "stop the logger", arguments = {
 			@ScriptArgument(name = "logger name", type = "string", description = "the logger name to stop"),
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
 
 	@ScriptUsage(description = "create new logger", arguments = {
 			@ScriptArgument(name = "logger factory name", type = "string", description = "logger factory name. try logapi.loggerFactories command."),
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
 			String transformer = context.readLine().trim();
 			if (!transformer.isEmpty()) {
 				LogTransformerFactory transformerFactory = transformerRegistry.getFactory(transformer);
 				if (transformerFactory != null) {
 					for (LoggerConfigOption type : transformerFactory.getConfigOptions()) {
 						setOption(config, type);
 					}
 					config.put("transform", transformer);
 				} else {
 					context.println("transformer not found");
 				}
 			}
 
 			Logger logger = loggerFactory.newLogger(loggerNamespace, loggerName, description, config);
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
 
 	@ScriptUsage(description = "remove logger", arguments = { @ScriptArgument(name = "logger fullname", type = "string", description = "the logger fullname") })
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
