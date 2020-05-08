 /*
     StatCvs - CVS statistics generation 
     Copyright (C) 2002  Lukasz Pekacki <lukasz@pekacki.de>
     http://statcvs.sf.net/
     
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU Lesser General Public
     License as published by the Free Software Foundation; either
     version 2.1 of the License, or (at your option) any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     Lesser General Public License for more details.
 
     You should have received a copy of the GNU Lesser General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 package de.berlios.statcvs.xml;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.logging.Level;
 
 /**
  * Takes a command line, like given to the {@link net.sf.statcvs.Main#main} method,
  * and turns it into a {@link ConfigurationOptions} object.
  * 
  * @author Richard Cyganiak <rcyg@gmx.de>
 * @version $Id: CommandLineParser.java,v 1.15 2004-11-10 16:18:52 squig Exp $
  */
 public class CommandLineParser {
 
 	private String[] argsArray;
 	private List args = new ArrayList();
 	private int argCount = 0;
 
 	/**
 	 * Constructor for CommandLineParser
 	 * 
 	 * @param args the command line parameters
 	 */
 	public CommandLineParser(String[] args) {
 		argsArray = args;
 	}
 	
 	/**
 	 * Parses the command line and sets the options (as static
 	 * fields in {@link ConfigurationOptions}).
 	 * 
 	 * @throws InvalidCommandLineException if errors are present on the command line
 	 */
 	public void parse(Hashtable settings) throws InvalidCommandLineException {
 		for (int i = 0; i < argsArray.length; i++) {
 			args.add(argsArray[i]);
 		}
 		while (!args.isEmpty()) {
 			String currentArg = popNextArg();
 			if (currentArg.startsWith("-")) {
 				parseSwitch(settings, currentArg.substring(1));
 			} else {
 				parseArgument(settings, currentArg);
 			}
 		}
 		//checkForRequiredArgs();
 	}
 
 	private String popNextArg() {
 		return (String) args.remove(0);
 	}
 
 	private void parseSwitch(Hashtable settings, String switchName) throws InvalidCommandLineException {
 		String s = switchName.toLowerCase();
 		if (s.equals("output-dir")) {
 			if (args.isEmpty()) {
 				throw new InvalidCommandLineException("Missing argument for -output-dir");
 			}
 			settings.put("outputDir", popNextArg());
 		} else if (s.equals("renderer")) {
 			settings.put("renderer", popNextArg());
 		} else if (s.equals("suite")) {
 			settings.put("suite", popNextArg());
 		} else if (s.equals("verbose")) {
 			settings.put("_logLevel", Level.INFO);
 		} else if (s.equals("debug")) {
 			settings.put("_logLevel", Level.FINEST); 
 		} else if (s.equals("weburl")) {
 			if (args.isEmpty()) {
 				throw new InvalidCommandLineException("Missing argument for -weburl");
 			}
 			settings.put("webRepository", popNextArg());
 		}
 		else if (s.equals("viewcvs")) {
 			if (args.isEmpty()) {
 				throw new InvalidCommandLineException("Missing argument for -viewcvs");
 			}
 			settings.put("viewcvs", popNextArg());
 		} else if (s.equals("cvsweb")) {
 			if (args.isEmpty()) {
 				throw new InvalidCommandLineException("Missing argument for -cvsweb");
 			}
 			settings.put("cvsweb", popNextArg());
 		} else if (s.equals("chora")) {
 			if (args.isEmpty()) {
 				throw new InvalidCommandLineException("Missing argument for -chora");
 			}
 			settings.put("chora", popNextArg());
 		} else if (s.equals("include")) {
 			if (args.isEmpty()) {
 				throw new InvalidCommandLineException("Missing argument for -include");
 			}
 			settings.put("include", popNextArg());
 		} else if (s.equals("exclude")) {
 			if (args.isEmpty()) {
 				throw new InvalidCommandLineException("Missing argument for -exclude");
 			}
 			settings.put("exclude", popNextArg());
 		} else if (s.equals("title")) {
 			if (args.isEmpty()) {
 				throw new InvalidCommandLineException("Missing argument for -title");
 			}
 			settings.put("projectName", popNextArg());
 		} else if (s.equals("maven")) {
 			settings.put("maven", "project.xml");
 		} else if (s.equals("no-images")) {
		    settings.put("no-images", "true");
 		} else {
 			throw new InvalidCommandLineException("Unrecognized option -" + s);
 		}
 	}
 	
 	private void parseArgument(Hashtable settings, String arg) throws InvalidCommandLineException {
 		argCount++;
 		switch (argCount) {
 			case 1:
 				settings.put("logFile", arg);
 				break;
 			case 2:
 				settings.put("localRepository", arg);
 				break;
 			default:
 				throw new InvalidCommandLineException("Too many arguments");
 		}
 	}
 
 	private void checkForRequiredArgs() throws InvalidCommandLineException {
 		switch (argCount) {
 			case 0:
 				throw new InvalidCommandLineException("Not enough arguments - <logfile> is missing");
 			case 1:
 				throw new InvalidCommandLineException("Not enough arguments - <directory> is missing");
 		}
 	}
 }
