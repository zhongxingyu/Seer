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
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import transxchange2GoogleTransitHandler.*;
 
 /*
  * Transxchange2GTFS 
  * 	$ transxchange2GoogleTransit <transxchange input filename> <url> <timezone> <default route type> <output-directory> <stopfile>
  * 
  * <default route type>: 0 - Tram, 1 - Subway, 2 - Rail, 3 - Bus, 4 - Ferry, 5 - Cable car, 6 - Gondola, 7 - Funicular
  */
 public class Transxchange2GoogleTransit {
 	
 	static boolean 										useAgencyShortname = false;
 	static boolean 										skipEmptyService = false;
 	static boolean 										skipOrphanStops = false;
 	static boolean 										geocodeMissingStops = false;
 	static Map<String, String>										modeList = null;
 	static List<String>									stopColumns = null;
 	static String										stopfilecolumnseparator;
 	static int											naptanHelperStopColumn = -1;
 	static Map<String, String>										naptanStopnames = null;
 	static Map<String, String>										agencyMap = null;
 
 	public static void main(String[] args) {
 
 		TransxchangeHandler handler = null;
 
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
 	        	System.out.println("Usage: $ transxchange2GoogleTransit <transxchange input filename> -");
 	        	System.out.println("         <url> <timezone> <default route type> <output-directory> [<stopfile>]");
 	        	System.out.println();
 	        	System.out.println("         Please refer to ");
 	        	System.out.println("             http://code.google.com/transit/spec/transit_feed_specification.html");
 	        	System.out.println("         for instructions about the values of the arguments <url>, <timezone> and <default route type>.");
 	        	System.exit(1);
 	        }
     
         // Parse transxchange input file and create initial GTFS output files
         try {
         	
         	handler = new TransxchangeHandler();
 
         	// v1.6.4: Read configuration file
         	if (args.length == 3)
         		args = readConfigFile(args[0], args[2]);
         	if (args.length == 4 && foundConfigFile == 2) {
         		String outdir = args[1];
         		args = readConfigFile(args[0], args[3]);
         		args[4] = outdir; // Copy work directory over
         	}
         	if (args.length == 5 && foundConfigFile == 3) {
         		handler.setAgencyOverride(args[2]);
         		String outdir = args[1];
         		args = readConfigFile(args[0], args[4]);
         		args[4] = outdir; // Copy work directory over
         	}
         	switch (args.length) {
         	case 8:
         		handler.parse(args[0], args[1], args[2], args[3], "", args[4], args[5], args[6], args[7], useAgencyShortname, skipEmptyService, skipOrphanStops, geocodeMissingStops, modeList, stopColumns, stopfilecolumnseparator, naptanHelperStopColumn, naptanStopnames, agencyMap);
         		break;
         	case 6:
        			handler.parse(args[0], args[1], args[2], args[3], "", args[4], args[5], "", "", useAgencyShortname, skipEmptyService, skipOrphanStops, geocodeMissingStops, modeList, stopColumns, stopfilecolumnseparator, naptanHelperStopColumn, naptanStopnames, agencyMap);
        			break;
        		default:
        			handler.parse(args[0], args[1], args[2], args[3], "", args[4], "", "", "", useAgencyShortname, skipEmptyService, skipOrphanStops, geocodeMissingStops, modeList, stopColumns, stopfilecolumnseparator, naptanHelperStopColumn, naptanStopnames, agencyMap);
        			break;
         	}
 		} catch (ParserConfigurationException e) {
        	System.out.println("transxchange2GTFS ParserConfiguration parse error:");
        	System.out.println(e.getMessage());
         	System.exit(1);
 		}
 		catch (SAXException e) {
			System.out.println("transxchange2GTFS SAX parse error:");
			System.out.println(e.getMessage());
			System.out.println(e.getException());
 			System.exit(1);						
 		}
 		catch (UnsupportedEncodingException e) {
			System.out.println("transxchange2GTFS NaPTAN stop file:");
			System.out.println(e.getMessage());
 			System.exit(1);						
 		}
  		catch (IOException e) {
			System.out.println("transxchange2GTFS IO parse error:");
			System.out.println(e.getMessage());
 			System.exit(1);						
 		}
 
        // Create final GTFS output files
         try {
         	handler.writeOutput("", args[4]);
         } catch (IOException e) {
         	System.out.println("transxchange2GTFS write error:");
         	System.out.println(e.getMessage());
         	System.exit(1);
         }
        
     	System.exit(0);
     }
 	
 	private static String[] readConfigFile(String inputFileName, String configFilename) 
 		throws IOException
 	
 	{
 		String[] result = {inputFileName, "", "", "", "", "", "", ""};
 		useAgencyShortname = false;
 		
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
 						result[1] = new String(configValues[1]);
 					if (configValues[0].equals("timezone"))
 						result[2] = new String(configValues[1]);
 					if (configValues[0].equals("default-route-type"))
 						result[3] = new String(configValues[1]);
 					if (configValues[0].equals("lang"))
 						result[6] = new String(configValues[1]);
 					if (configValues[0].equals("phone"))
 						result[7] = new String(configValues[1]);
 					if (configValues[0].equals("output-directory"))
 						result[4] = new String(configValues[1]);
 					if (configValues[0].equals("stopfile")) {
 						result[5] = new String(configValues[1]);
 						if (naptanStopnames == null)
 							naptanStopnames = NaPTANHelper.readStopfile(configValues[1]);
 					}
 					if (configValues[0].equals("naptanstopcolumn")) {
 						if (stopColumns == null)
 							stopColumns = new ArrayList<String>();
 						stopColumns.add(configValues[1]);
 					}
 					if (configValues[0].equals("naptanstophelper"))
 						if (stopColumns == null)
 							naptanHelperStopColumn = 0;
 						else
 							naptanHelperStopColumn = stopColumns.size();
 					
 					if (configValues[0].equals("stopfilecolumnseparator"))
 						stopfilecolumnseparator = new String(configValues[1]);
 						
 					if (configValues[0].equals("useagencyshortname") && configValues[1] != null && configValues[1].trim().toLowerCase().equals("true"))
 						useAgencyShortname = true;
 					if (configValues[0].equals("skipemptyservice") && configValues[1] != null && configValues[1].trim().toLowerCase().equals("true"))
 						skipEmptyService = true;
 					if (configValues[0].equals("skiporphanstops") && configValues[1] != null && configValues[1].trim().toLowerCase().equals("true"))
 						skipOrphanStops = true;
 					if (configValues[0].equals("geocodemissingstops") && configValues[1] != null && configValues[1].trim().toLowerCase().equals("true"))
 						geocodeMissingStops = true;
 					if (txcMode != null)
 						if (txcMode.length() > 0 && configValues[1].length() > 0) {
 							if (modeList == null)
 								modeList = new HashMap<String, String>();
 							modeList.put(txcMode, configValues[1]);
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
 						if (agencyMap == null)
 							agencyMap = new HashMap<String, String>();
 						agencyMap.put(configValues[1], configValues[2]);
 					}
 				}
 				if (configValues[0].length() >= 5 && configValues[0].substring(0, 5).equals("mode:"))
 					txcMode = configValues[0].substring(5, configValues[0].length());
 				tokenCount++;
 			}
 		}
 		in.close();
 			
 		return result;
 	}
 }
