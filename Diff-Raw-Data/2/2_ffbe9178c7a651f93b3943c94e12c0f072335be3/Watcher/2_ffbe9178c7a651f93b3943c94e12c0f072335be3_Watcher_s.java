 package com.github.davidmoten.logan.watcher;
 
 import java.io.File;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.github.davidmoten.logan.Data;
 import com.github.davidmoten.logan.LogFile;
 import com.github.davidmoten.logan.LogParser;
 import com.github.davidmoten.logan.LogParserOptions;
 import com.github.davidmoten.logan.config.Configuration;
 import com.github.davidmoten.logan.config.Group;
 import com.github.davidmoten.logan.config.Log;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 
 /**
  * Watches (tails) groups of files configured by persister configuration and
  * reports lines to the <i>log-database</i>.
  * 
  * @author dave
  * 
  */
 public class Watcher {
 
 	private static final int DELAY_BETWEEN_CHECKS_FOR_NEW_CONTENT_MS = 500;
 
 	private static final int TERMINATION_TIMEOUT_MS = 30000;
 
 	private static final Logger log = Logger.getLogger(Watcher.class.getName());
 
 	private final ExecutorService executor;
 
 	private final List<LogFile> logs = Lists.newArrayList();
 
 	private final Configuration configuration;
 
 	private final Data data;
 
 	private final int numTailers;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param factory
 	 * @param configuration
 	 */
 	public Watcher(Data data, Configuration configuration) {
 		this.data = data;
 		this.configuration = configuration;
 		// executor = Executors.newFixedThreadPool(Runtime.getRuntime()
 		// .availableProcessors());
 		numTailers = countTailers(configuration);
 		log.info("numTailers=" + numTailers);
 		// each tailer needs an active thread at all times and we need some
 		// spares to load the files that are not being watched after load. The
 		// non-watched file threads will terminate so a pool smaller than the
 		// number of such files is appropriate.
 		int size = numTailers + 10;
 		executor = Executors.newFixedThreadPool(size);
 		log.info("create fixed thread pool of size=" + size);
 	}
 
 	private static int countTailers(Configuration configuration) {
 
 		int tailers = 0;
 		for (Group group : configuration.group) {
 			for (Log lg : group.log) {
 				List<File> files = Util
 						.getFilesFromPathWithRegexFilename(lg.path);
 				if (lg.watch)
 					tailers += files.size();
 			}
 		}
 		return tailers;
 	}
 
 	private static class LogFileInfo {
 		final LogFile logFile;
 		final Log log;
 
 		public LogFileInfo(LogFile logFile, Log log) {
 			super();
 			this.logFile = logFile;
 			this.log = log;
 		}
 	}
 
 	/**
 	 * Starts tailing threads for each configured matched file.
 	 */
 	public void start() {
 		log.info("starting watcher");
 		List<LogFileInfo> list = Lists.newArrayList();
 		for (Group group : configuration.group) {
 			log.info("starting group " + group);
 			for (Log lg : group.log) {
 				for (File file : Util
 						.getFilesFromPathWithRegexFilename(lg.path)) {
 					LogParserOptions options = LogParserOptions.load(
 							configuration.parser, group);
 					String source;
 					if (lg.source == null
 							&& configuration.parser.sourcePattern != null)
 						source = extractSource(
 								configuration.parser.sourcePattern,
 								file.getName());
 					else
 						source = lg.source;
 					if (source == null)
 						throw new RuntimeException(
 								"source not specified or could not be extracted using sourcePattern for log:"
 										+ lg);
 					LogFile logFile = new LogFile(file, source,
 							DELAY_BETWEEN_CHECKS_FOR_NEW_CONTENT_MS,
 							new LogParser(options), executor);
 					boolean follow = lg.watch;
 					list.add(new LogFileInfo(logFile, lg));
 				}
 			}
 		}
 
 		// tail oldest files first so that we are less likely to trim recent
 		// records using Data.maxSize
 		Collections.sort(list, new Comparator<LogFileInfo>() {
 
 			@Override
 			public int compare(LogFileInfo a, LogFileInfo b) {
 				long max = Long.MAX_VALUE;
 
 				final long timeA;
 				if (!a.logFile.getFile().exists())
 					timeA = max;
 				else
 					timeA = a.logFile.getFile().lastModified();
 
 				final long timeB;
 				if (!b.logFile.getFile().exists())
 					timeB = max;
 				else
 					timeB = b.logFile.getFile().lastModified();
 
 				return ((Long) timeA).compareTo(timeB);
 			}
 		});
 
 		Map<Log, LogFileInfo> watchLatest = Maps.newHashMap();
 		for (LogFileInfo info : list) {
 			if (info.log.watchLatest) {
 				LogFileInfo latest = watchLatest.get(info.log);
 				if (latest == null
 						|| info.logFile.getFile().lastModified() > latest.logFile
 								.getFile().lastModified())
 					watchLatest.put(info.log, info);
 			}
 		}
 
 		// start tails
 		for (LogFileInfo info : list) {
 			boolean follow = info.log.watch
 					|| info == watchLatest.get(info.log);
 			log.info("starting tail (follow=" + follow + ") on "
 					+ info.logFile.getFile());
 			logs.add(info.logFile);
 			info.logFile.tail(data, follow);
 		}
 		log.info("started watcher");
 	}
 
 	private String extractSource(String sourcePattern, String filename) {
 		Pattern p = Pattern.compile(sourcePattern);
 		Matcher m = p.matcher(filename);
 		if (!m.find())
 			throw new RuntimeException("could not find source in " + filename
 					+ " using pattern " + sourcePattern);
 		else
 			return m.group();
 	}
 
 	/**
 	 * Stops each thread watching a file and shuts down the executor that
 	 * started the threads.
 	 */
 	public void stop() {
 		log.info("stopping watcher");
 		for (LogFile lg : logs) {
 			lg.stop();
 		}
 		executor.shutdownNow();
 		try {
 			executor.awaitTermination(TERMINATION_TIMEOUT_MS,
 					TimeUnit.MILLISECONDS);
 			log.info("stopped watcher");
 		} catch (InterruptedException e) {
 			throw new RuntimeException("failed to stop running threads", e);
 		}
 	}
 
 	public int getNumTailers() {
 		return numTailers;
 	}
 
 }
