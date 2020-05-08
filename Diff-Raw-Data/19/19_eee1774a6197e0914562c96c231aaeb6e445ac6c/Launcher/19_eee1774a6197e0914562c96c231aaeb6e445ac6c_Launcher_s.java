 /*******************************************************************************
  * Poor Man's CMS (pmcms) - A very basic CMS generating static html pages.
  * http://poormans.sourceforge.net
  * Copyright (C) 2004-2013 by Thilo Schwarz
  * 
  * == BEGIN LICENSE ==
  * 
  * Licensed under the terms of any of the following licenses at your
  * choice:
  * 
  *  - GNU Lesser General Public License Version 2.1 or later (the "LGPL")
  *    http://www.gnu.org/licenses/lgpl.html
  * 
  *  - Mozilla Public License Version 1.1 or later (the "MPL")
  *    http://www.mozilla.org/MPL/MPL-1.1.html
  * 
  * == END LICENSE ==
  ******************************************************************************/
 package de.thischwa.pmcms.tool.launcher;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.Properties;
 
 import de.thischwa.pmcms.Constants;
 import de.thischwa.pmcms.conf.BasicConfigurator;
 import de.thischwa.pmcms.tool.CliParser;
 import de.thischwa.pmcms.tool.InternalAntTool;
 import de.thischwa.pmcms.tool.PropertiesTool;
 
 
 /**
  * Launcher object to starts pmcms. This is the start object in <code>start.jar</code>!
  * 
  * @author Thilo Schwarz
  */
 public class Launcher {
 
 	public static void main(String[] args) throws FileNotFoundException {
 		CliParser cliParser = null;
 		try {
 			cliParser = new CliParser(args);
 		} catch (Exception e) {
 			System.out.print(e.getMessage());
 			System.out.println();
 			System.exit(1);
 		}
 		if(cliParser.hasOption("help")) {
 			cliParser.printHelp();
 			System.exit(0);
 		}
 		
 		File dataDir;
		if(cliParser.hasOption("portable")) 
 			dataDir = Constants.APPLICATION_DIR;
 		else {
 			dataDir = (cliParser.hasOption("datadir")) 
 					? new File(cliParser.getOptionValue("datadir")) 
 					: new File(Constants.HOME_DIR, Constants.NAME);
 		}
 		if(!dataDir.exists()) {
 			System.out.println(String.format("'datadir' [%s] not found!", dataDir.getPath()));
 			System.exit(1);
 		}
 		
 		//users props
 		File propertiesFile = new File(dataDir, BasicConfigurator.PROPERTIES_NAME);
 		if(!propertiesFile.exists()) {
			System.out.println(String.format("Properties file [%] not found!", propertiesFile.getPath()));
 			System.exit(1);
 		}
 		InputStream userIn = new BufferedInputStream(new FileInputStream(propertiesFile));
 		// the common props
 		InputStream commonIn = new BufferedInputStream(BasicConfigurator.class.getResourceAsStream("common.properties"));
 		Properties props = PropertiesTool.loadProperties(commonIn, userIn);
		InternalAntTool.start(dataDir, props, "de.thischwa.pmcms.Starter", cliParser.hasOption("debug"), args);
 	}
 }
