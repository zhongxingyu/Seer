 /*   
  * Copyright 2008-2010 Oleg Sukhoroslov
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *     http://www.apache.org/licenses/LICENSE-2.0
  *     
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package jlite.cli;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import jlite.GridSession;
 import jlite.GridSessionConfig;
 import jlite.GridSessionFactory;
 import jlite.GridSessionImpl;
 import jlite.util.PasswordPrompt;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.MissingArgumentException;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.glite.voms.contact.VOMSProxyBuilder;
 import org.globus.gsi.GlobusCredential;
 
 public class ProxyInit {
 	
 	private static final String COMMAND = "proxy-init [options] <voms>[:<command>] ...";
 	
 	public static void main(String[] args) {
 	    CommandLineParser parser = new GnuParser();
 	    Options options = setupOptions();
 	    HelpFormatter helpFormatter = new HelpFormatter();
 	    helpFormatter.setSyntaxPrefix("Usage: ");
 	    CommandLine line = null;
 		try {
 			line = parser.parse(options, args);			
             if (line.hasOption("help")) {
         		System.out.println(); // extra line
                 helpFormatter.printHelp(100, COMMAND, "\noptions:", options, "\n"+CLI.FOOTER+"\n", false);
                 System.out.println(); // extra line
                 System.exit(0);
             } else {
             	if (line.hasOption("xml")) {
 					System.out.println("<output>");
 				}
             	String[] remArgs = line.getArgs();
             	if (remArgs.length > 0) {
             		run(remArgs, line);
             	} else {
             		throw new MissingArgumentException("Missing required argument: <voms>[:<command>]");
             	}
            	if (line.hasOption("xml")) {
					System.out.println("</output>");
				}
             }
 		} catch (ParseException e) {
             System.err.println("\n" + e.getMessage() + "\n");
             helpFormatter.printHelp(100, COMMAND, "\noptions:", options, "\n"+CLI.FOOTER+"\n", false);
             System.out.println(); // extra line
             System.exit(-1);
 		} catch (Exception e) {
 			if ((line != null) && (line.hasOption("xml"))) {
 				System.out.println("<error>" + e.getMessage() + "</error>");
 			} else {
 				System.err.println(e.getMessage());
 			}
 		}
 		System.out.println(); // extra line
 	}
 
 	private static Options setupOptions() {
         Options options = new Options();
                 
         options.addOption(OptionBuilder
                 .withDescription("displays usage")
                 .create("help"));
 
         options.addOption(OptionBuilder
                 .withDescription("enables extra debug output")
                 .create("debug"));
 
         options.addOption(OptionBuilder
         		.withArgName("h:m")
                 .withDescription("proxy and AC are valid for h hours and m minutes (defaults to 12:00)")
                 .hasArg()
                 .create("valid"));
                 
         options.addOption(OptionBuilder
                 .withDescription("creates a limited proxy")
                 .create("limited"));
 
         options.addOption(OptionBuilder
         		.withArgName("version")
                 .withDescription("version of proxy certificate {2,3,4} (default is 2)")
                 .hasArg()
                 .create("proxyver"));
 
         options.addOption(OptionBuilder
                 .withDescription("creates RFC 3820 compliant proxy (synonomous with -proxyver 4)")
                 .create("rfc"));
 
         options.addOption(OptionBuilder
         		.withArgName("proxyfile")
                 .withDescription("non-standard location of new proxy cert")
                 .hasArg()
                 .create("out"));
 
         options.addOption(OptionBuilder
         		.withArgName("path")
                 .withDescription("non-standard location of VOMS configuration files")
                 .hasArg()
                 .create("vomses"));
 
         options.addOption(OptionBuilder
         		.withArgName("key")
                 .withDescription("set your private key passphrase without any prompt")
                 .hasArg()
                 .create("password"));
 
         options.addOption(OptionBuilder
         		.withArgName("xml")
                 .withDescription("output as xml")
                 .create("xml"));
         
         return options;
 	}
 
 	private static void run(String[] vomsArgs, CommandLine line) throws Exception {
 		if (line.hasOption("debug")) {
 			Logger logger = Logger.getLogger(GridSessionImpl.class);
 			logger.setLevel(Level.DEBUG);
 		}
 		
 		GridSessionConfig conf = new GridSessionConfig();
 		if (line.hasOption("out")) {
 			conf.setProxyPath(line.getOptionValue("out"));
 		}
 		if (line.hasOption("vomses")) {
 			conf.setVOMSDir(line.getOptionValue("vomses"));
 		}
 		
 		if (line.hasOption("password")) {
 			conf.setUserKeyPass(line.getOptionValue("password"));
 		} else {		
 			conf.setUserKeyPass(PasswordPrompt.prompt("Enter your private key passphrase: "));		
 		}
 
 		GridSession grid = GridSessionFactory.create(conf);
 		
 		int lifetime = 43200; // 12 hours by default
 		if (line.hasOption("valid")) {
 			String valid = line.getOptionValue("valid");
 			String[] validParts = valid.split(":");
 			int hours = Integer.parseInt(validParts[0]);
 			int minutes = Integer.parseInt(validParts[1]);
 			lifetime = hours*3600 + minutes*60;
 		}
 		int proxyType = VOMSProxyBuilder.DEFAULT_PROXY_TYPE;
 		if (line.hasOption("proxyver")) {
 			proxyType = Integer.parseInt(line.getOptionValue("proxyver"));
 		}
 		if (line.hasOption("rfc")) {
 			proxyType = VOMSProxyBuilder.GT4_PROXY;
 		}
 		boolean limited = line.hasOption("limited");
 
 		GlobusCredential proxy = grid.createProxy(vomsArgs, lifetime, proxyType, limited);
 
 		if (line.hasOption("xml")) {
			System.out.println("<voms-proxy>" + proxy.getSubject() + "</voms-proxy>");
 		} else {
 			System.out.println("Created VOMS proxy: " + proxy.getSubject());
 		}
 
 		long expiryTime = System.currentTimeMillis() + proxy.getTimeLeft()*1000;
 		SimpleDateFormat df = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy z", Locale.ENGLISH);
 		if (line.hasOption("xml")) {
			System.out.println("<valid-until>" + df.format(new Date(expiryTime)) + "</valid-until>");
 		} else {				
 			System.out.println("Proxy is valid until: " + df.format(new Date(expiryTime)));
 		}
 
 		if (line.hasOption("xml")) {
			System.out.println("<proxy-location>" + new File(conf.getProxyPath()).getAbsolutePath() + "</proxy-location>");
 		} else {
 			System.out.println("Proxy location: " + new File(conf.getProxyPath()).getAbsolutePath());
 		}
 	}
 	
 }
