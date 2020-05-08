 package org.sinouplen.tools.stream;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import org.apache.commons.io.Charsets;
 import org.apache.log4j.Logger;
 import org.sinouplen.tools.logger.LoggerLevel;
 
 /**
  * @author Sinouplen
  * 
  */
 public class ShowInputStream implements Runnable {
 
 	private static final Logger LOGGER = Logger
 			.getLogger(ShowInputStream.class);
 
 	private final InputStream inputStream;
 	private final LoggerLevel level;
 
 	/**
 	 * @param inputStream
 	 * @param level
 	 */
 	public ShowInputStream(InputStream inputStream, LoggerLevel level) {
 		this.inputStream = inputStream;
 		this.level = level;
 	}
 
 	/**
 	 * @param is
 	 * @return
 	 */
 	private BufferedReader getBufferedReader(InputStream is) {
 		return new BufferedReader(new InputStreamReader(is, Charsets.UTF_8));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Runnable#run()
 	 */
 	public void run() {
 		BufferedReader br = getBufferedReader(inputStream);
 		String ligne = "";
 		try {
 			while ((ligne = br.readLine()) != null) {
 				switch (this.level) {
 				case DEBUG:
 					LOGGER.info(ligne);
 				case ERROR:
 					LOGGER.error(ligne);
 				case FATAL:
 					LOGGER.fatal(ligne);
 				case INFO:
 					LOGGER.info(ligne);
 				case TRACE:
 					LOGGER.trace(ligne);
 				case WARN:
 					LOGGER.warn(ligne);
				default:
					LOGGER.error(this.level + " | " + ligne);
 				}
 			}
 		} catch (IOException e) {
 			LOGGER.error(e);
 		}
 	}
 }
