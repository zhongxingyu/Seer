 package transxchange2GoogleTransit;
 /*
  * Copyright 2007, 2008, 2009, 2010, 2011, 2012 GoogleTransitDataFeed
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.StringTokenizer;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 
 import transxchange2GoogleTransit.handler.NaPTANHelper;
 import transxchange2GoogleTransit.handler.TransxchangeHandler;
 
 /*
  * Transxchange2GTFS
  * 	$ transxchange2GoogleTransit <transxchange input filename> <url> <timezone> <default route type> <output-directory> <stopfile>
  *
  * <default route type>: 0 - Tram, 1 - Subway, 2 - Rail, 3 - Bus, 4 - Ferry, 5 - Cable car, 6 - Gondola, 7 - Funicular
  */
 public class Transxchange2GoogleTransit {
 
 	public static void main(String[] args) {
 
 		TransxchangeHandler handler = null;
 		Configuration config = new Configuration();
 
         System.out.println("transxchange2GTFS 1.7.5");
         System.out.println("Please refer to LICENSE file for licensing information");
         int foundConfigFile = -1;
         int i = 0;
         while (i < args.length && foundConfigFile == -1)
         	if (args[i].toLowerCase().equals("-c"))
         		foundConfigFile = i;
         	else
         		i++;
          if (foundConfigFile == -1 && (args.length < 5 || args.length > 6) ||
         	foundConfigFile >= 0 && (args.length < 3 || args.length > 5)) {
 	        	System.out.println();
 	        	System.out.println("Usage: $ transxchange2GoogleTransit <transxchange input filename> -c <configuration file name>");
 	        	System.out.println("Usage: $ transxchange2GoogleTransit <transxchange input filename> <output-directory> -c <configuration file name>");
 	        	System.out.println("Usage: $ transxchange2GoogleTransit <transxchange input filename> <output-directory> <agency name> -c <configuration file name>");
	        	System.out.println("Usage: $ transxchange2GoogleTransit <transxchange input filename> ");
 	        	System.out.println("         <url> <timezone> <default route type> <output-directory> [<stopfile> [<lang> <phone>]]");
 	        	System.out.println();
 	        	System.out.println("         Please refer to ");
 	        	System.out.println("             http://code.google.com/transit/spec/transit_feed_specification.html");
 	        	System.out.println("         for instructions about the values of the arguments <url>, <timezone>, <default route type>, <lang> and <phone>.");
 	        	System.exit(1);
 	        }
 
         // Parse transxchange input file and create initial GTFS output files
         try {
 
         	handler = new TransxchangeHandler();
 
         	// v1.6.4: Read configuration file
         	if (args.length == 3){
         	  config = readConfigFile(args[0], args[2]);
         	} else if (args.length == 4 && foundConfigFile == 2) {
         		String outdir = args[1];
         		config = readConfigFile(args[0], args[3]);
         		config.outputDirectory = outdir;// Copy work directory over
         	} else if (args.length == 5 && foundConfigFile == 3) {
         		handler.setAgencyOverride(args[2]);
         		String outdir = args[1];
         		config = readConfigFile(args[0], args[4]);
         		config.outputDirectory = outdir; // Copy work directory over
         	} else if (args.length == 8 || args.length == 6 || args.length == 5){
         	  config.inputFileName = args[0];
         	  config.url = args[1];
         	  config.timezone = args[2];
         	  config.defaultRouteType = args[3];
         	  config.outputDirectory = args[4];
         	  if (args.length >= 6){
         	    config.stopFile = args[5];
         	    if (args.length == 8){
         	      config.lang = args[6];
         	      config.phone = args[7];
         	    }
         	  }
         	}
         	
         	handler.parse(config);
 		} catch (ParserConfigurationException e) {
         	System.err.println("transxchange2GTFS ParserConfiguration parse error:");
         	e.printStackTrace();
         	System.exit(1);
 		}
 		catch (SAXException e) {
 			System.err.println("transxchange2GTFS SAX parse error:");
 			e.printStackTrace();
 			System.exit(1);
 		}
 		catch (UnsupportedEncodingException e) {
 			System.err.println("transxchange2GTFS NaPTAN stop file:");
 			e.printStackTrace();
 			System.exit(1);
 		}
  		catch (IOException e) {
 			System.err.println("transxchange2GTFS IO parse error:");
 			e.printStackTrace();
 			System.exit(1);
 		}
 
        // Create final GTFS output files
         try {
         	handler.writeOutput(config);
         } catch (IOException e) {
         	System.err.println("transxchange2GTFS write error:");
         	System.err.println(e.getMessage());
         	System.exit(1);
         }
 
     	System.exit(0);
     }
 
 	private static Configuration readConfigFile(String inputFileName, String configFilename)
 		throws IOException
 
 	{
 	  Configuration config = new Configuration();
 	  config.inputFileName = inputFileName;
 
 		BufferedReader in = new BufferedReader(new FileReader(configFilename));
 		String line;
 		int tokenCount;
 		String configValues[] = {"", "", ""};
 //		String tagToken = "", configurationValue;
 		String txcMode = null;
 		while ((line = in.readLine()) != null) {
 			tokenCount = 0;
 			StringTokenizer st = new StringTokenizer(line, "=");
 			while (st.hasMoreTokens() && tokenCount <= 2) {
 				configValues[tokenCount] = st.nextToken();
 				if (tokenCount == 1) {
 					configValues[0] = configValues[0].trim().toLowerCase();
 //					configurationValue = st.nextToken().trim();
 					if (configValues[0].equals("url"))
 						config.url = configValues[1];
 					if (configValues[0].equals("timezone"))
 						config.timezone = configValues[1];
 					if (configValues[0].equals("default-route-type"))
 						config.defaultRouteType = configValues[1];
 					if (configValues[0].equals("lang"))
 						config.lang = configValues[1];
 					if (configValues[0].equals("phone"))
 						config.phone = configValues[1];
 					if (configValues[0].equals("output-directory"))
 						config.outputDirectory = configValues[1];
 					if (configValues[0].equals("stopfile")) {
 						config.stopFile = configValues[1];
 						if (config.naptanStops == null)
 							config.naptanStops = NaPTANHelper.readStopfile(configValues[1]);
 					}
 					if (configValues[0].equals("naptanstopcolumn")) {
 						if (config.stopColumns == null)
 						  config.stopColumns = new ArrayList<String>();
 						config.stopColumns.add(configValues[1]);
 					}
 					if (configValues[0].equals("naptanstophelper"))
 						if (config.stopColumns == null)
 						  config.naptanHelperStopColumn = 0;
 						else
 						  config.naptanHelperStopColumn = config.stopColumns.size();
 
 					if (configValues[0].equals("stopfileColumnSeparator"))
 					  config.stopfileColumnSeparator = new String(configValues[1]);
 
 					if (configValues[0].equals("useagencyshortname") && configValues[1] != null && configValues[1].trim().toLowerCase().equals("true"))
 					  config.useAgencyShortName = true;
 					if (configValues[0].equals("skipemptyservice") && configValues[1] != null && configValues[1].trim().toLowerCase().equals("true"))
 					  config.skipEmptyService = true;
 					if (configValues[0].equals("skiporphanstops") && configValues[1] != null && configValues[1].trim().toLowerCase().equals("true"))
 					  config.skipOrphanStops = true;
 					if (configValues[0].equals("geocodemissingstops") && configValues[1] != null && configValues[1].trim().toLowerCase().equals("true"))
 					  config.geocodeMissingStops = true;
 					if (txcMode != null)
 						if (txcMode.length() > 0 && configValues[1].length() > 0) {
 							if (config.modeList == null)
 							  config.modeList = new HashMap<String, String>();
 							config.modeList.put(txcMode, configValues[1]);
 						}
 						txcMode = null;
 					}
 				if (configValues[0].length() >= 7 && configValues[0].substring(0, 7).equals("agency:")) {
 					configValues[1] = configValues[0].substring(7, configValues[0].length());
 					configValues[0] = "agency";
 					tokenCount++;
 				}
 				if (tokenCount == 2) {
 					if (configValues[0].equals("agency")) {
 						if (config.agencyMap == null)
 						  config.agencyMap = new HashMap<String, String>();
 						config.agencyMap.put(configValues[1], configValues[2]);
 					}
 				}
 				if (configValues[0].length() >= 5 && configValues[0].substring(0, 5).equals("mode:"))
 					txcMode = configValues[0].substring(5, configValues[0].length());
 				tokenCount++;
 			}
 		}
 		in.close();
 
 		return config;
 	}
 }
