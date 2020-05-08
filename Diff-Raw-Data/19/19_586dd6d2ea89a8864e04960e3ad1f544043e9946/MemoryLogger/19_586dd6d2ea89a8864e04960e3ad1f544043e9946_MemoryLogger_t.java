 /*
  * Copyright (c) 2012 Mateusz Parzonka, Eric Bodden
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Mateusz Parzonka - initial API and implementation
  */
 package mop;
 
 import java.util.logging.FileHandler;
 import java.util.logging.Formatter;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.LogRecord;
 import java.util.logging.Logger;
 
 import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
 
 /**
  * Logs memory consumption to file. Activated by system property "prm4jeval.memoryLogging".
  */
 public class MemoryLogger {
 
    private final static boolean STATS_LOGGING = Boolean.parseBoolean(getSystemProperty("prm4jeval.statsLogging",
 	    "false"));
 
     private SummaryStatistics memStats;
     private long timestamp = 0L;
 
     private Logger logger;
 
     private String experimentName;
 
     // Trying to track down NaNs which appeared in mean and max.
     private int NaNcount = 0;
 
     MemoryLogger() {
	if (STATS_LOGGING) {
 	    memStats = new SummaryStatistics();
	    String outputPath = "logs/javamop-stats.log";
	    System.out.println("Logging activated. Output path: " + outputPath);
 	    experimentName = getMandatorySystemProperty("prm4jeval.invocation") + " "
 		    + getMandatorySystemProperty("prm4jeval.benchmark") + " "
 		    + getMandatorySystemProperty("prm4jeval.paramProperty");
 	    logger = getFileLogger(outputPath);
 	} else {
 	    System.out.println("Memory logging not activated.");
 	}
     }
 
     /**
      * Registers the memory consumption every 100 events. Flag MEMORY_LOGGING has to be activated.
      */
     public void logMemoryConsumption() {
	if (STATS_LOGGING && timestamp++ % 100 == 0) {
 	    double memoryConsumption = (((double) (Runtime.getRuntime().totalMemory() / 1024) / 1024) - ((double) (Runtime
 		    .getRuntime().freeMemory() / 1024) / 1024));
 	    // filter NaNs
 	    if (memoryConsumption != Double.NaN) {
 		memStats.addValue(memoryConsumption);
 	    } else {
 		NaNcount++;
 	    }
 	}
     }
 
     /**
      * Registers the memory consumption independent of any internal event counter or timestamp. Flag MEMORY_LOGGING has
      * to be activated.
      */
     public void reallyLogMemoryConsumption() {
	if (STATS_LOGGING) {
 	    double memoryConsumption = (((double) (Runtime.getRuntime().totalMemory() / 1024) / 1024) - ((double) (Runtime
 		    .getRuntime().freeMemory() / 1024) / 1024));
 	    // filter NaNs
 	    if (memoryConsumption != Double.NaN) {
 		memStats.addValue(memoryConsumption);
 	    } else {
 		NaNcount++;
 	    }
 	}
     }
 
     /**
      * Writes memory consumption (mean and max), number of counted events and number of violations to disk.
      */
     public void writeToFile(int matchCount) {
	if (STATS_LOGGING) {
 	    logger.log(Level.INFO, String.format("%s EVENTS (totalCount) %d", experimentName, timestamp));
 	    logger.log(Level.INFO, String.format("%s MATCHES (totalCount) %d", experimentName, matchCount));
 	    logger.log(Level.INFO,
 		    String.format("%s MEMORY (mean/max) %f %f", experimentName, memStats.getMean(), memStats.getMax()));
 	    if (NaNcount > 0) {
 		logger.log(Level.INFO, String.format("%s NaN (totalCount) %d", experimentName, NaNcount));
 	    }
 
 	    // reset
 	    memStats.clear();
 	    timestamp = 0L;
 	    NaNcount = 0;
 	}
     }
 
     /**
      * A simple file logger which outputs only the message.
      *
      * @param fileName
      *            path to the output file
      * @return the logger
      */
     private static Logger getFileLogger(String fileName) {
 	final Logger logger = Logger.getLogger(fileName);
 	try {
 	    logger.setUseParentHandlers(false);
 	    Handler handler = new FileHandler(fileName, true);
 	    handler.setFormatter(new Formatter() {
 		@Override
 		public String format(LogRecord record) {
 		    return record.getMessage() + "\n";
 		}
 	    });
 	    logger.addHandler(handler);
 	} catch (Exception e) {
 	    throw new RuntimeException(e);
 	}
 	return logger;
     }
 
     static String getSystemProperty(String key, String defaultValue) {
 	final String value = System.getProperty(key);
 	return value != null ? value : defaultValue;
     }
 
     static String getMandatorySystemProperty(String key) {
 	final String value = System.getProperty(key);
 	if (value == null) {
 	    throw new RuntimeException("System property [" + key + "] is mandatory but not defined!");
 	}
 	return value;
     }
 
 }
