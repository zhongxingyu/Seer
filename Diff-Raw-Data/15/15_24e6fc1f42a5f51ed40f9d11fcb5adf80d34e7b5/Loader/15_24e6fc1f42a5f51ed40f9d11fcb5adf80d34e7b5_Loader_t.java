 package de.cosmocode.palava;
 /*
 palava - a java-php-bridge
 Copyright (C) 2007  CosmoCode GmbH
 
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.xml.DOMConfigurator;
 
 
 /**
  * Commandline parser, initiates the server.
  * @author Tobias Sarnowski
  */
 public class Loader
 {
 
 	private static final Logger logger = Logger.getLogger(Loader.class);
 
 
 	/**
 	 * first line of palava server execution
 	 */
 	public static void main(String[] args)
 	{
 		if (args.length < 1) {
 			logger.fatal("No configuration file given, startup stopped!");
 			System.err.println("Usage:  palava <config file>");
 			System.exit(1);
 		}
 
 		// read in the config file
 		String configfile = args[0];
 
 		boolean interactive = args.length > 1 && args[1].equals("-");
 
 		if (interactive) {
 			System.out.println("running interactive: " + interactive);
 		}
 		Properties config = new Properties();
 		try {
 			config.load(new FileInputStream(configfile));
 		} catch (IOException ioe) {
 			logger.fatal("Cannot load configuration file, startup stopped!");
 			ioe.printStackTrace();
 			System.exit(1);
 		}
 		String palava_dir = config.getProperty("PALAVA_DIR");
 		if (!palava_dir.endsWith(File.separator)) {
 			palava_dir = palava_dir + File.separator;
 		}
 
 		// start logging
 		String log4j_conf = config.getProperty("LOG4J_XML");
 		if (!log4j_conf.startsWith(File.separator)) {
 			log4j_conf = palava_dir + log4j_conf;
 		}
 		DOMConfigurator.configure(log4j_conf);
 
 		// check if an alias file exists
 		Properties alias = new Properties();
 		String alias_conf = config.getProperty("ALIAS_CONF");
 		if (alias_conf != null) {
 			if (!alias_conf.startsWith(File.separator)) {
 				alias_conf = palava_dir + alias_conf;
 			}
 			try {
 				alias.load(new FileInputStream(new File(alias_conf)));
 			} catch (IOException ioe) {
				logger.error("Alias config file defined but cannot be loaded!", ioe);
 			}
 		}
 
 		// start the server
 		Server server = new Server(config, alias,interactive);
 		server.start();
 
 		// register shutdown event
 		Thread hook = new Thread(new ShutdownHook(server));
 		Runtime.getRuntime().addShutdownHook(hook);
 
 		// wait until it ends
 		try {
 			server.join();
 		} catch(Exception e) {
 			logger.fatal("Server main-thread ends unexpected!", e);
 			e.printStackTrace();
 			System.exit(1);
 		}
 
 		logger.info("Server stopped.");
 		System.exit(0);
 	}
 
 }
