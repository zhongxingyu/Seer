 package org.logparser.example;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.type.TypeReference;
 import org.logparser.Config;
 import org.logparser.ILogEntryFilter;
 import org.logparser.LogEntry;
 import org.logparser.LogEntryFilter;
 import org.logparser.LogSnapshot;
 import org.logparser.Config.SamplerConfig;
 import org.logparser.io.ChartView;
 import org.logparser.io.CommandLineArguments;
 import org.logparser.io.CsvView;
 import org.logparser.io.LineByLineLogFilter;
 import org.logparser.io.LogFiles;
 import org.logparser.sampling.SamplingByFrequency;
 import org.logparser.sampling.SamplingByTime;
 
 import com.beust.jcommander.JCommander;
 
 /**
  * Responsible for running the log parser via the command line.
  * 
 * <code>
 *  java -Xmx128m -jar log-parser-1.0.jar -help
 * </code>
  * 
  * @author jorge.decastro
  */
 public class CommandLineApplicationRunner {
 	private static final Logger LOGGER = Logger.getLogger(CommandLineApplicationRunner.class.getName());
 
 	public static void main(String[] args) {
 		CommandLineArguments cla = new CommandLineArguments();
 		JCommander jc = new JCommander(cla, args);
 		if (cla.help) {
 			jc.usage();
 			return;
 		}
 
 		ObjectMapper mapper = new ObjectMapper();
 		Config config = null;
 		try {
 			Map<String, Config> configs = mapper.readValue(new File(cla.configFile), new TypeReference<Map<String, Config>>() { });
 			config = configs.get(cla.logName);
 
 			config.validate();
 			LOGGER.info(String.format("Loaded '%s' configuration", config.getFriendlyName()));
 			// TODO fix exception handling
 		} catch (JsonParseException jpe) {
 			jpe.printStackTrace();
 		} catch (JsonMappingException jme) {
 			jme.printStackTrace();
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 
 		if (config != null) {
 			LogFiles logfiles = config.getLogFiles();
 			File[] files = logfiles.list();
 
 			LogEntryFilter filter = new LogEntryFilter(config);
 			// for large log files sampling is preferred/required
 			ILogEntryFilter<LogEntry> sampler = null;
 			if (config.getSampler() != null) {
 				SamplerConfig samplerConfig = config.getSampler();
 				switch (samplerConfig.sampleBy) {
 				case TIME:
 					sampler = new SamplingByTime<LogEntry>(filter, samplerConfig.value);
 					break;
 				case FREQUENCY:
 					sampler = new SamplingByFrequency<LogEntry>(filter, samplerConfig.value);
 					break;
 				default:
 					sampler = null;
 				}
 			}
 
 			@SuppressWarnings("unchecked")
 			LineByLineLogFilter<LogEntry> lineByLineParser = new LineByLineLogFilter<LogEntry>(config, sampler != null ? sampler : filter);
 			CsvView csvView = new CsvView();
 
 			ChartView<LogEntry> chartView;
 			String filepath;
 			String path;
 			String filename;
 			for (File f : files) {
 				filepath = f.getAbsolutePath();
 				filename = f.getName();
 				path = f.getParent();
 
 				long start = System.nanoTime();
 				LogSnapshot<LogEntry> logSnapshot = lineByLineParser.filter(filepath);
 				long end = (System.nanoTime() - start) / 1000000;
 				DecimalFormat df = new DecimalFormat("####.##");
 				int totalEntries = logSnapshot.getTotalEntries();
 				int filteredEntries = logSnapshot.getFilteredEntries().size();
 				LOGGER.info(String.format("\n%s - Ellapsed = %sms, rate = %sstrings/ms, total = %s, filtered = %s\n", filename, end, df.format(totalEntries / (double) end), totalEntries, filteredEntries));
 
 				chartView = new ChartView<LogEntry>(logSnapshot);
 				chartView.write(path, filename);
 				csvView.write(path, filename, logSnapshot);
 
 				LOGGER.info("\n" + logSnapshot.toString());
 			}
 		}
 	}
 }
