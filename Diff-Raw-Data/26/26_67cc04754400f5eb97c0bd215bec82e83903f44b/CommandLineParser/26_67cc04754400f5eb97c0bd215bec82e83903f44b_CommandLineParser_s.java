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
 
 import net.sf.statcvs.output.ChoraIntegration;
 import net.sf.statcvs.output.CvswebIntegration;
 import net.sf.statcvs.output.ViewCvsIntegration;
 import de.berlios.statcvs.xml.output.XDocRenderer;
 
 /**
  * Takes a command line, like given to the {@link net.sf.statcvs.Main#main} method,
  * and turns it into a {@link ConfigurationOptions} object.
  * 
  * @author Richard Cyganiak <rcyg@gmx.de>
 * @version $Id: CommandLineParser.java,v 1.6 2004-02-26 16:12:53 squig Exp $
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
		checkForRequiredArgs();
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
 			String arg = popNextArg();
 			if (arg.equals("html")) {
 				//settings.put("renderer", HTMLRenderer.class.getName());
 			}
 			else if (arg.equals("xdoc")) {
 				settings.put("renderer", XDocRenderer.class.getName());
 			}
 			else {
 				settings.put("renderer", arg);
 			}
 		} else if (s.equals("document-suite")) {
 			settings.put("documentSuite", popNextArg());
 		} else if (s.equals("use-history")) {
 			settings.put("useHistory", "true");
 		} else if (s.equals("verbose")) {
 			settings.put("verbose", "true");
 		} else if (s.equals("debug")) {
 			settings.put("debug", "true"); 
 		} else if (s.equals("weburl")) {
 			if (args.isEmpty()) {
 				throw new InvalidCommandLineException("Missing argument for -weburl");
 			}
 			settings.put("_webRepository", WebRepositoryFactory.getInstance(popNextArg()));
 		}
 		else if (s.equals("viewcvs")) {
 			if (args.isEmpty()) {
 				throw new InvalidCommandLineException("Missing argument for -viewcvs");
 			}
 			settings.put("_webRepository", new ViewCvsIntegration(popNextArg()));
 		} else if (s.equals("cvsweb")) {
 			if (args.isEmpty()) {
 				throw new InvalidCommandLineException("Missing argument for -cvsweb");
 			}
 			settings.put("_webRepository", new CvswebIntegration(popNextArg()));
 		} else if (s.equals("chora")) {
 			if (args.isEmpty()) {
 				throw new InvalidCommandLineException("Missing argument for -chora");
 			}
 			settings.put("_webRepository", new ChoraIntegration(popNextArg()));
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
 			settings.put("title", popNextArg());
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
