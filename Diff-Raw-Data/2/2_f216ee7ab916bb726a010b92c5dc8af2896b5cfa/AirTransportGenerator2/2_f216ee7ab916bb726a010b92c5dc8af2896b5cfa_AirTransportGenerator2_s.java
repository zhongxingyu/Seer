 package org.eclipse.stem.utility.generators;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 /**
  * The utility takes as input a .csv file where each row has the following columns:
  * 
  * 1. Airport code
  * 2. STEM ID
  * 3. Total passangers (per year)
  * 4. Lat/Lon string
  * 5. Lat (number)
  * 6. Lon (number)
  * 
  * It generates multipe stem .properties files that the ant script converts to EMF models. 
  */
 @SuppressWarnings("nls")
 public class AirTransportGenerator2 {
 	
 	static String LS = System.getProperty("line.separator"); //$NON-NLS-1$
 	
 	private static HashMap<String, String>level0IsoKeys = new HashMap<String, String>();
 	
 	// When we don't have data, use this numbers. 15/day or 0.01% of population,
 	// whichever is smallest
 	
 	private static int DEFAULT_PASSENGERS_WHEN_MISSING = 5475;
 	private static int DEFAULT_DIVISIOR = 365; // Numbers are yearly, we want daily
 	// What percentage of passengers when departing from a level 2 (e.g. county) region
 	// are expected to arrive at at least a different county. Since cross-county flights
 	// are rare we set this to 100.
 	private static double DEFAULT_PERCENT_START_2_LEVEL_2_TO_LEVEL_1 = 100; 
 	// What percentage of passengers when departing from a level 1 (e.g. state) region
 	// are expected to arrive at at least another state. The average for US is 6.7 %
 	// of flights are within the same state so we use that globally.
 	private static double DEFAULT_PERCENT_START_2_LEVEL_1_TO_LEVEL_0 = 93.3;
 	// What percentage of passengers when departing from a level 0 (e.g. country) region
 	// are expected to arrive at at least another country. This is very tricky.
 	// It's international versus domestic flights needed for all countries. We're
 	// guessing 50%.
 	private static double DEFAULT_PERCENT_START_2_LEVEL_0_TO_LEVEL_m1 = 50;
 	
 	// The above percentages are when we have data on level 2 (counties) for airports.
 	// When we only have data on level 1 or 0, the percentages are different. The 
 	// motivation being less detailed data means it's more likely that flights are
 	// national/international.
 	
 	// A flight leaving the state is 100 % likely to at least go to another state
 	private static double DEFAULT_PERCENT_START_1_LEVEL_1_TO_LEVEL_0 = 93.3;
 	
 	// A flight departing the country is 50 % likely to arrive in another country
 	private static double DEFAULT_PERCENT_START_1_LEVEL_0_TO_LEVEL_m1 = 50;
 
 	// If only have airport for the country all flights are international
 	private static double DEFAULT_PERCENT_START_0_LEVEL_0_TO_LEVEL_m1 = 100;
 	
 	
 	static {
 		level0IsoKeys.put("TL", "TLS");
 		level0IsoKeys.put("TK", "TKL");
 		level0IsoKeys.put("TJ", "TJK");
 		level0IsoKeys.put("TH", "THA");
 		level0IsoKeys.put("TG", "TGO");
 		level0IsoKeys.put("TF", "ATF");
 		level0IsoKeys.put("GY", "GUY");
 		level0IsoKeys.put("TD", "TCD");
 		level0IsoKeys.put("TC", "TCA");
 		level0IsoKeys.put("GW", "GNB");
 		level0IsoKeys.put("GU", "GUM");
 		level0IsoKeys.put("GT", "GTM");
 		level0IsoKeys.put("GS", "SGS");
 		level0IsoKeys.put("GR", "GRC");
 		level0IsoKeys.put("GQ", "GNQ");
 		level0IsoKeys.put("GP", "GLP");
 		level0IsoKeys.put("SZ", "SWZ");
 		level0IsoKeys.put("SY", "SYR");
 		level0IsoKeys.put("GN", "GIN");
 		level0IsoKeys.put("GM", "GMB");
 		level0IsoKeys.put("GL", "GRL");
 		level0IsoKeys.put("SV", "SLV");
 		level0IsoKeys.put("ST", "STP");
 		level0IsoKeys.put("GI", "GIB");
 		level0IsoKeys.put("GH", "GHA");
 		level0IsoKeys.put("SR", "SUR");
 		level0IsoKeys.put("GG", "GGY");
 		level0IsoKeys.put("SQ", "SQR");
 		level0IsoKeys.put("GF", "GUF");
 		level0IsoKeys.put("GE", "GEO");
 		level0IsoKeys.put("SO", "SOM");
 		level0IsoKeys.put("GD", "GRD");
 		level0IsoKeys.put("SN", "SEN");
 		level0IsoKeys.put("SM", "SMR");
 		level0IsoKeys.put("GB", "GBR");
 		level0IsoKeys.put("SL", "SLE");
 		level0IsoKeys.put("GA", "GAB");
 		level0IsoKeys.put("SK", "SVK");
 		level0IsoKeys.put("SJ", "SJM");
 		level0IsoKeys.put("SI", "SVN");
 		level0IsoKeys.put("SH", "SHN");
 		level0IsoKeys.put("SG", "SGP");
 		level0IsoKeys.put("SE", "SWE");
 		level0IsoKeys.put("SD", "SDN");
 		level0IsoKeys.put("SC", "SYC");
 		level0IsoKeys.put("SB", "SLB");
 		level0IsoKeys.put("SA", "SAU");
 		level0IsoKeys.put("FR", "FRA");
 		level0IsoKeys.put("FO", "FRO");
 		level0IsoKeys.put("FM", "FSM");
 		level0IsoKeys.put("RW", "RWA");
 		level0IsoKeys.put("FK", "FLK");
 		level0IsoKeys.put("RU", "RUS");
 		level0IsoKeys.put("FJ", "FJI");
 		level0IsoKeys.put("FI", "FIN");
 		level0IsoKeys.put("RS", "SRB");
 		level0IsoKeys.put("RO", "ROU");
 		level0IsoKeys.put("RE", "REU");
 		level0IsoKeys.put("ET", "ETH");
 		level0IsoKeys.put("ES", "ESP");
 		level0IsoKeys.put("ER", "ERI");
 		level0IsoKeys.put("EH", "ESH");
 		level0IsoKeys.put("EG", "EGY");
 		level0IsoKeys.put("EE", "EST");
 		level0IsoKeys.put("EC", "ECU");
 		level0IsoKeys.put("DZ", "DZA");
 		level0IsoKeys.put("QA", "QAT");
 		level0IsoKeys.put("DO", "DOM");
 		level0IsoKeys.put("PY", "PRY");
 		level0IsoKeys.put("DM", "DMA");
 		level0IsoKeys.put("PW", "PLW");
 		level0IsoKeys.put("DK", "DNK");
 		level0IsoKeys.put("DJ", "DJI");
 		level0IsoKeys.put("PT", "PRT");
 		level0IsoKeys.put("PS", "PSE");
 		level0IsoKeys.put("PR", "PRI");
 		level0IsoKeys.put("DE", "DEU");
 		level0IsoKeys.put("PN", "PCN");
 		level0IsoKeys.put("PM", "SPM");
 		level0IsoKeys.put("PL", "POL");
 		level0IsoKeys.put("PK", "PAK");
 		level0IsoKeys.put("PH", "PHL");
 		level0IsoKeys.put("PG", "PNG");
 		level0IsoKeys.put("CZ", "CZE");
 		level0IsoKeys.put("PF", "PYF");
 		level0IsoKeys.put("CY", "CYP");
 		level0IsoKeys.put("PE", "PER");
 		level0IsoKeys.put("CX", "CXR");
 		level0IsoKeys.put("CV", "CPV");
 		level0IsoKeys.put("CU", "CUB");
 		level0IsoKeys.put("PA", "PAN");
 		level0IsoKeys.put("CR", "CRI");
 		level0IsoKeys.put("CO", "COL");
 		level0IsoKeys.put("CN", "CHN");
 		level0IsoKeys.put("CM", "CMR");
 		level0IsoKeys.put("CL", "CHL");
 		level0IsoKeys.put("CK", "COK");
 		level0IsoKeys.put("CI", "CIV");
 		level0IsoKeys.put("CH", "CHE");
 		level0IsoKeys.put("CG", "COG");
 		level0IsoKeys.put("CF", "CAF");
 		level0IsoKeys.put("CD", "COD");
 		level0IsoKeys.put("CC", "CCK");
 		level0IsoKeys.put("OM", "OMN");
 		level0IsoKeys.put("CA", "CAN");
 		level0IsoKeys.put("BZ", "BLZ");
 		level0IsoKeys.put("BY", "BLR");
 		level0IsoKeys.put("BW", "BWA");
 		level0IsoKeys.put("BV", "BVT");
 		level0IsoKeys.put("BT", "BTN");
 		level0IsoKeys.put("BS", "BHS");
 		level0IsoKeys.put("BR", "BRA");
 		level0IsoKeys.put("BO", "BOL");
 		level0IsoKeys.put("NZ", "NZL");
 		level0IsoKeys.put("BN", "BRN");
 		level0IsoKeys.put("BM", "BMU");
 		level0IsoKeys.put("BJ", "BEN");
 		level0IsoKeys.put("NU", "NIU");
 		level0IsoKeys.put("BI", "BDI");
 		level0IsoKeys.put("BH", "BHR");
 		level0IsoKeys.put("BG", "BGR");
 		level0IsoKeys.put("NR", "NRU");
 		level0IsoKeys.put("BF", "BFA");
 		level0IsoKeys.put("BE", "BEL");
 		level0IsoKeys.put("NP", "NPL");
 		level0IsoKeys.put("BD", "BGD");
 		level0IsoKeys.put("NO", "NOR");
 		level0IsoKeys.put("BB", "BRB");
 		level0IsoKeys.put("BA", "BIH");
 		level0IsoKeys.put("NL", "NLD");
 		level0IsoKeys.put("ZW", "ZWE");
 		level0IsoKeys.put("NI", "NIC");
 		level0IsoKeys.put("NG", "NGA");
 		level0IsoKeys.put("AZ", "AZE");
 		level0IsoKeys.put("NF", "NFK");
 		level0IsoKeys.put("NE", "NER");
 		level0IsoKeys.put("AX", "ALA");
 		level0IsoKeys.put("AW", "ABW");
 		level0IsoKeys.put("NC", "NCL");
 		level0IsoKeys.put("ZM", "ZMB");
 		level0IsoKeys.put("AU", "AUS");
 		level0IsoKeys.put("NA", "NAM");
 		level0IsoKeys.put("AT", "AUT");
 		level0IsoKeys.put("AS", "ASM");
 		level0IsoKeys.put("AR", "ARG");
 		level0IsoKeys.put("AQ", "ATA");
 		level0IsoKeys.put("AO", "AGO");
 		level0IsoKeys.put("MZ", "MOZ");
 		level0IsoKeys.put("AN", "ANT");
 		level0IsoKeys.put("MY", "MYS");
 		level0IsoKeys.put("AM", "ARM");
 		level0IsoKeys.put("MX", "MEX");
 		level0IsoKeys.put("AL", "ALB");
 		level0IsoKeys.put("MW", "MWI");
 		level0IsoKeys.put("MV", "MDV");
 		level0IsoKeys.put("MU", "MUS");
 		level0IsoKeys.put("ZA", "ZAF");
 		level0IsoKeys.put("AI", "AIA");
 		level0IsoKeys.put("MT", "MLT");
 		level0IsoKeys.put("MS", "MSR");
 		level0IsoKeys.put("AG", "ATG");
 		level0IsoKeys.put("MR", "MRT");
 		level0IsoKeys.put("AF", "AFG");
 		level0IsoKeys.put("MQ", "MTQ");
 		level0IsoKeys.put("AE", "ARE");
 		level0IsoKeys.put("MP", "MNP");
 		level0IsoKeys.put("AD", "AND");
 		level0IsoKeys.put("MO", "MAC");
 		level0IsoKeys.put("MN", "MNG");
 		level0IsoKeys.put("MM", "MMR");
 		level0IsoKeys.put("ML", "MLI");
 		level0IsoKeys.put("MK", "MKD");
 		level0IsoKeys.put("YT", "MYT");
 		level0IsoKeys.put("MH", "MHL");
 		level0IsoKeys.put("MG", "MDG");
 		level0IsoKeys.put("ME", "MNE");
 		level0IsoKeys.put("MD", "MDA");
 		level0IsoKeys.put("MC", "MCO");
 		level0IsoKeys.put("MA", "MAR");
 		level0IsoKeys.put("LY", "LBY");
 		level0IsoKeys.put("YE", "YEM");
 		level0IsoKeys.put("LV", "LVA");
 		level0IsoKeys.put("LU", "LUX");
 		level0IsoKeys.put("LT", "LTU");
 		level0IsoKeys.put("LS", "LSO");
 		level0IsoKeys.put("LR", "LBR");
 		level0IsoKeys.put("LK", "LKA");
 		level0IsoKeys.put("LI", "LIE");
 		level0IsoKeys.put("LC", "LCA");
 		level0IsoKeys.put("LB", "LBN");
 		level0IsoKeys.put("LA", "LAO");
 		level0IsoKeys.put("KZ", "KAZ");
 		level0IsoKeys.put("KY", "CYM");
 		level0IsoKeys.put("KW", "KWT");
 		level0IsoKeys.put("KR", "KOR");
 		level0IsoKeys.put("KP", "PRK");
 		level0IsoKeys.put("KN", "KNA");
 		level0IsoKeys.put("KM", "COM");
 		level0IsoKeys.put("KI", "KIR");
 		level0IsoKeys.put("KH", "KHM");
 		level0IsoKeys.put("WS", "WSM");
 		level0IsoKeys.put("KG", "KGZ");
 		level0IsoKeys.put("KE", "KEN");
 		level0IsoKeys.put("WF", "WLF");
 		level0IsoKeys.put("JP", "JPN");
 		level0IsoKeys.put("JO", "JOR");
 		level0IsoKeys.put("JM", "JAM");
 		level0IsoKeys.put("VU", "VUT");
 		level0IsoKeys.put("JE", "JEY");
 		level0IsoKeys.put("VN", "VNM");
 		level0IsoKeys.put("VI", "VIR");
 		level0IsoKeys.put("VG", "VGB");
 		level0IsoKeys.put("VE", "VEN");
 		level0IsoKeys.put("VC", "VCT");
 		level0IsoKeys.put("VA", "VAT");
 		level0IsoKeys.put("IT", "ITA");
 		level0IsoKeys.put("IS", "ISL");
 		level0IsoKeys.put("IR", "IRN");
 		level0IsoKeys.put("IQ", "IRQ");
 		level0IsoKeys.put("IO", "IOT");
 		level0IsoKeys.put("UZ", "UZB");
 		level0IsoKeys.put("IN", "IND");
 		level0IsoKeys.put("UY", "URY");
 		level0IsoKeys.put("IM", "IMN");
 		level0IsoKeys.put("IL", "ISR");
 		level0IsoKeys.put("US", "USA");
 		level0IsoKeys.put("IE", "IRL");
 		level0IsoKeys.put("ID", "IDN");
 		level0IsoKeys.put("UM", "UMI");
 		level0IsoKeys.put("UG", "UGA");
 		level0IsoKeys.put("HU", "HUN");
 		level0IsoKeys.put("UA", "UKR");
 		level0IsoKeys.put("HT", "HTI");
 		level0IsoKeys.put("HR", "HRV");
 		level0IsoKeys.put("TZ", "TZA");
 		level0IsoKeys.put("HN", "HND");
 		level0IsoKeys.put("HM", "HMD");
 		level0IsoKeys.put("TW", "TWN");
 		level0IsoKeys.put("TV", "TUV");
 		level0IsoKeys.put("HK", "HKG");
 		level0IsoKeys.put("TT", "TTO");
 		level0IsoKeys.put("TR", "TUR");
 		level0IsoKeys.put("TO", "TON");
 		level0IsoKeys.put("TN", "TUN");
 		level0IsoKeys.put("TM", "TKM");
 		level0IsoKeys.put("OH", "OMN");
 	}
 	/**
 	 * 
 	 * @param args
 	 */
 	@SuppressWarnings({ "nls", "synthetic-access" })
 	public static void main(String [] args) {
 		if(args.length < 1) {
 			System.err.println("Usage: AirTransportGenerator2 <input .properties file>");
 			System.exit(-1);
 		}
 		
 		try {
 		String inputFile = args[0];
 		
 		File propfile = new File(inputFile);
 		if(!propfile.exists()) {
 			System.err.println("Error, "+inputFile+" doesn't exist");
 			System.exit(-1);
 		}
 		
 		
 		BufferedReader ireader = new BufferedReader(new InputStreamReader(new FileInputStream(propfile)));
 		   			
 		HashMap<String, FileWriter> writers = new HashMap<String, FileWriter>();
 		HashMap<String, Integer> counters = new HashMap<String, Integer>();
 		HashMap<String, Integer> totalPassengers = new HashMap<String, Integer>();
 		HashMap<String, String> airportCodes = new HashMap<String, String>();
 		
 		String line;
 		while((line=ireader.readLine()) != null) {
 		  StringTokenizer st = new StringTokenizer(line, ",");
 		  String airportCode = st.nextToken().trim();
 		  String stemCode = st.nextToken().trim();
 		  // trim at first space if found
 		
 		  if(stemCode.indexOf(" ") != -1)
 			  stemCode = stemCode.substring(0, stemCode.indexOf(" "));
 		  
 		  int startLevel = GenUtils.getLevel(stemCode);
 		  int passengers = Integer.parseInt(st.nextToken().trim());
 		  int population = Integer.parseInt(st.nextToken().trim());
 		  
 		  
 		  // Skip rest
 		  String s = null;
 		  while(st.hasMoreTokens()) s=st.nextToken();
 		  boolean generated = (passengers == -1);
 		
 		  // 0.01 % per day or 15 / day, whichever is smallest
 		  double threshold = ((double)population*(double)DEFAULT_DIVISIOR)/10000.0;
 		  
 		  if(generated && threshold < DEFAULT_PASSENGERS_WHEN_MISSING)
 			  passengers = (int)threshold;
 		  else if(generated) 
 			  passengers = DEFAULT_PASSENGERS_WHEN_MISSING;
 		  else passengers = passengers / 2; // Total troughput, we want arrivals/departures so divide by 2
 		  	  
 		  System.out.println(stemCode+" Population: "+population+ " passengers: "+passengers+" generated: "+generated);
 		  while(stemCode != null) {
 			  int level = GenUtils.getLevel(stemCode);
 			  double factor = 1.0;
 			  if(level == 2 && startLevel == 2) factor = DEFAULT_PERCENT_START_2_LEVEL_2_TO_LEVEL_1/100.0;
 			  else if(level == 1 && startLevel == 2) factor = DEFAULT_PERCENT_START_2_LEVEL_1_TO_LEVEL_0/100.0;
 			  else if(level == 0 && startLevel == 2) factor = DEFAULT_PERCENT_START_2_LEVEL_0_TO_LEVEL_m1/100.0;
 			  else if(level == 1 && startLevel == 1) factor = DEFAULT_PERCENT_START_1_LEVEL_1_TO_LEVEL_0/100.0;
 			  else if(level == 0 && startLevel == 1) factor = DEFAULT_PERCENT_START_1_LEVEL_0_TO_LEVEL_m1/100.0;
 			  else if(level == 0 && startLevel == 0) factor = DEFAULT_PERCENT_START_0_LEVEL_0_TO_LEVEL_m1/100.0;
 				  	  
 			  if(totalPassengers.containsKey(stemCode))
 				  totalPassengers.put(stemCode, (int)(totalPassengers.get(stemCode)+passengers*factor));
 			  else
 				  totalPassengers.put(stemCode, (int)(passengers*factor));
 			  
 			  if(airportCodes.containsKey(stemCode))
 				  airportCodes.put(stemCode, airportCodes.get(stemCode)+", "+airportCode + ((generated)?"(Estimated)":"(Actual)"));
 			  else
 				  airportCodes.put(stemCode, airportCode + ((generated)?"(Estimated)":"(Actual)"));
 			  
 			  stemCode = GenUtils.upCode(stemCode);
 		  }
 		}
 		ireader.close();
 		
 		for(String stemCode:totalPassengers.keySet()) {
 		  int level = GenUtils.getLevel(stemCode);
 		  // Turn two letter country code into three letter country code
 		  String country = GenUtils.extractAlpha1(stemCode);
 		  double numPassengers = totalPassengers.get(stemCode);
 		  numPassengers /= (double)DEFAULT_DIVISIOR;
 		  
 		  if(country.length() == 2) 
 			  country = level0IsoKeys.get(country);
 		  if(country == null) {
 			  System.err.println("Error, country code in : "+stemCode+" not recognized");
 			  continue;
 		  }
 		  String filename=null;
 		  if(level == 0)
 			  filename = "ZZZ_-1_ZZZ_0.properties";
 		  else filename = country+"_"+(level-1)+"_"+country+"_"+level+".properties";
 		  FileWriter fw = null;
 		  if(writers.containsKey(filename))
 			  fw = writers.get(filename);
 		  else {
 			  fw = new FileWriter(filename);
 			  writers.put(filename, fw);
 			  writerHeader(fw, filename, country, level);
 		  }
 		  String parentKey=null, childKey=null;
 		  
 		  if(level == 2) {
			  parentKey = stemCode.substring(0, 5);
 			  childKey = stemCode;
 		  } else if (level == 1) { 
 			  parentKey = country;
 			  childKey = stemCode;
 		  } else if (level == 0) {
 			  parentKey = "ZZZ";
 			  childKey = country;
 		  }
 		  int linecount;
 		  if(counters.containsKey(filename)) {
 			  linecount = counters.get(filename);
 			  counters.put(filename, linecount+1);
 		  }
 		  else {
 			  linecount = 0;
 			  counters.put(filename, linecount+1);
 		  }
 		  
 		  fw.write("# "+airportCodes.get(stemCode)+LS);
 		  fw.write(linecount+" = ");
 		  fw.write(parentKey);
 		  fw.write(',');
 		  fw.write(childKey);
 		  fw.write(',');
 		  fw.write(numPassengers+"");
 		  fw.write(',');
 		  fw.write(numPassengers+"");
 		  fw.write(LS);
 		  System.out.println(stemCode+" passengers: "+numPassengers);
 		}  // For each line
 		
 		
 		for(FileWriter fw:writers.values()) {
 			fw.flush();
 			fw.close();
 		}		
 	} catch(Exception e) {
 		e.printStackTrace();
 	}
 		
 	} // main
 	
 	
 	@SuppressWarnings("nls")
 	public static void writerHeader(FileWriter fw, String filename, String country, int level) throws IOException {
 		String ctry = country;
 		if(level == 0) ctry = "ZZZ";
 		fw.write("# "+filename+LS);
 		fw.write("#/*******************************************************************************"+LS); 
 		fw.write("# * Copyright (c) 2008 IBM Corporation and others."+LS); 
 		fw.write("# * All rights reserved. This program and the accompanying materials"+LS); 
 		fw.write("# * are made available under the terms of the Eclipse Public License v1.0"+LS); 
 		fw.write("# * which accompanies this distribution, and is available at"+LS);
 		fw.write("# * http://www.eclipse.org/legal/epl-v10.html "+LS);
 		fw.write("# * "+LS);
 		fw.write("# * Contributors"+LS); 
 		fw.write("# *     IBM Corporation - initial API and implementation"+LS); 
 		fw.write("# *******************************************************************************/"+LS); 
 		fw.write(LS);		 
 		fw.write("# This is the name of the class that will interpret the rest of the file's contents"+LS); 
 		fw.write("RECORD_CLASSNAME =  org.eclipse.stem.internal.data.geography.infrastructure.transportation.specifications.AirTransportationGeographicRelationshipPropertyFileSpecification"+LS);
 		fw.write(LS); 
 		fw.write("# The title of the relationship"+LS); 
 		fw.write("TITLE = Air transport between "+ctry+"("+(level-1)+") and "+ctry+"("+level+")"); 
 		fw.write(LS); 
 		fw.write("# This is the date range for which the values in this file are valid"+LS); 
 		fw.write("# See http://purl.org/dc/terms/1.1/valid"+LS); 
 		fw.write("VALID = start=2000-01-01; end=2008-12-31;"+LS);
 		fw.write(LS); 
 		fw.write("# The data is derived from the following primary source"+LS); 
 		fw.write("#SOURCE = http://www.azworldairports.com"+LS); 
 		fw.write(LS); 
 		fw.write("# ISO3166-1 alpha3 keys and levels of relationship"+LS); 
 		fw.write("ISOKEY_0 = "+ctry+LS); 
 		fw.write("ISOKEY_0_LEVEL = "+(level -1)+LS); 
 		fw.write("ISOKEY_1 = "+ctry+LS); 
 		fw.write("ISOKEY_1_LEVEL = "+level+LS);
 		fw.write(LS); 
 		fw.write("# The mode of transportation "+LS);
 		fw.write("TRANSPORT_MODE = air"+LS); 
 		fw.write(LS);
 		fw.write("# The population being transported"+LS); 
 		fw.write("POPULATION_IDENTIFIER = human"+LS); 
 		fw.write(LS);
 		fw.write("# The time period for the rate in milliseconds "+LS); 
 		fw.write("RATE_TIME_PERIOD = 86400000"+LS); 
 		fw.write(LS); 
 		fw.write("#Average time in transportation network"+LS); 
 		fw.write("AVG_TIME_IN_SYSTEM = 1"+LS); 
 		fw.write(LS);
 		 
 		fw.write("# Record No. = Enclosing Node Key,  Node Key, ArrivalRate (individuals per RATE_TIME_PERIOD), DepartureRate(Individuals per RATE_TIME_PERIOD)"+LS); 
 		fw.flush();
 	}
 }
