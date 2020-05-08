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
     
 	$RCSfile: CommandLineParser.java,v $
	Created on $Date: 2004-02-15 14:21:26 $ 
 */
 package de.berlios.statcvs.xml;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.statcvs.output.ChoraIntegration;
 import net.sf.statcvs.output.CvswebIntegration;
 import net.sf.statcvs.output.ViewCvsIntegration;
 import net.sf.statcvs.output.WebRepositoryIntegration;
 import de.berlios.statcvs.xml.output.XDocRenderer;
 import de.berlios.statcvs.xml.output.XMLRenderer;
 
 /**
  * Takes a command line, like given to the {@link net.sf.statcvs.Main#main} method,
  * and turns it into a {@link ConfigurationOptions} object.
  * 
  * @author Richard Cyganiak <rcyg@gmx.de>
 * @version $Id: CommandLineParser.java,v 1.2 2004-02-15 14:21:26 squig Exp $
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
 	 * @throws IOException if errors are present on the command line
 	 */
 	public void parse() throws IOException {
 		for (int i = 0; i < argsArray.length; i++) {
 			args.add(argsArray[i]);
 		}
 		while (!args.isEmpty()) {
 			String currentArg = popNextArg();
 			if (currentArg.startsWith("-")) {
 				parseSwitch(currentArg.substring(1));
 			} else {
 				parseArgument(currentArg);
 			}
 		}
 		checkForRequiredArgs();
 	}
 
 	private String popNextArg() {
 		return (String) args.remove(0);
 	}
 
 	private void parseSwitch(String switchName) throws IOException {
 		String s = switchName.toLowerCase();
 		if (s.equals("output-dir")) {
 			if (args.isEmpty()) {
 				throw new IOException("Missing argument for -output-dir");
 			}
 			Settings.setOutputDir(popNextArg());
 		} else if (s.equals("output-suite")) {
 			if (args.isEmpty()) {
 				Settings.setOutputSuite(XMLRenderer.class.getName());
 			}
 			else {
 				String arg = popNextArg();
 //				if (arg.equals("html")) {
 //					Settings.setOutputSuite
 //						(HTMLRenderer.class.getName());
 //				}
 //				else 
 				if (arg.equals("xdoc")) {
 					Settings.setOutputSuite(XDocRenderer.class.getName());
 				}
 				else {
 					Settings.setOutputSuite(arg);
 				}
 			}
 		} else if (s.equals("generate-history")) {
 			Settings.setGenerateHistory(true);
 		} else if (s.equals("use-history")) {
 			Settings.setUseHistory(true);
 		} else if (s.equals("verbose")) {
 			Settings.setVerboseLogging();
 		} else if (s.equals("debug")) {
 			Settings.setDebugLogging();
 		} else if (s.equals("nocredits")) {
 			Settings.setShowCreditInformation(false);
 		} else if (s.equals("notes")) {
 			if (args.isEmpty()) {
 				throw new IOException("Missing argument for -notes");
 			}
 			Settings.setNotesFile(popNextArg());
 		} else if (s.equals("weburl")) {
 			if (args.isEmpty()) {
 				throw new IOException("Missing argument for -weburl");
 			}
 			WebRepositoryIntegration wri = WebRepositoryFactory.getInstance(popNextArg());
			if (wri == null) {
				throw new IOException("Cannot recognize web repository type. Please select it explicitly");
			}
 			Settings.setWebRepository(wri);
 		}
 		else if (s.equals("viewcvs")) {
 			if (args.isEmpty()) {
 				throw new IOException("Missing argument for -viewcvs");
 			}
 			Settings.setWebRepository(new ViewCvsIntegration(popNextArg()));
 		} else if (s.equals("cvsweb")) {
 			if (args.isEmpty()) {
 				throw new IOException("Missing argument for -cvsweb");
 			}
 			Settings.setWebRepository(new CvswebIntegration(popNextArg()));
 		} else if (s.equals("chora")) {
 			if (args.isEmpty()) {
 				throw new IOException("Missing argument for -chora");
 			}
 			Settings.setWebRepository(new ChoraIntegration(popNextArg()));
 		} else if (s.equals("include")) {
 			if (args.isEmpty()) {
 				throw new IOException("Missing argument for -include");
 			}
 			Settings.setIncludePattern(popNextArg());
 		} else if (s.equals("exclude")) {
 			if (args.isEmpty()) {
 				throw new IOException("Missing argument for -exclude");
 			}
 			Settings.setExcludePattern(popNextArg());
 		} else if (s.equals("title")) {
 			if (args.isEmpty()) {
 				throw new IOException("Missing argument for -title");
 			}
 			Settings.setProjectTitle(popNextArg());
 		} else {
 			throw new IOException("Unrecognized option -" + s);
 		}
 	}
 	
 	private void parseArgument(String arg) throws IOException {
 		argCount++;
 		switch (argCount) {
 			case 1:
 				Settings.setLogFileName(arg);
 				break;
 			case 2:
 				Settings.setCheckedOutDirectory(arg);
 				break;
 			default:
 				throw new IOException("Too many arguments");
 		}
 	}
 
 	private void checkForRequiredArgs() throws IOException {
 		switch (argCount) {
 			case 0:
 				throw new IOException("Not enough arguments - <logfile> is missing");
 			case 1:
 				throw new IOException("Not enough arguments - <directory> is missing");
 		}
 	}
 }
