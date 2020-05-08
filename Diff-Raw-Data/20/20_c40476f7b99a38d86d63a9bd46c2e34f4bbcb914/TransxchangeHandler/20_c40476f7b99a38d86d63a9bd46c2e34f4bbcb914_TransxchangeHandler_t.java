 /*
  * Copyright 2007 GoogleTransitDataFeed
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
 
 package transxchange2GoogleTransitHandler;
 
 import java.io.*;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.*;
 import org.xml.sax.helpers.*;
 
 import java.util.zip.*;
 import java.util.ArrayList;
 
 
 
 // This class extends DefaultHandler to parse an transxchange xml file
 // and generate corresponding Google Transit data feed structures
 public class TransxchangeHandler extends DefaultHandler {
 
 	TransxchangeAgency agencies;
 	TransxchangeStops stops;
 	TransxchangeRoutes routes;
 	TransxchangeTrips trips;
 	TransxchangeStopTimes stopTimes;
 	TransxchangeCalendar calendar;
 	TransxchangeCalendarDates calendarDates;
 
 	static String googleTransitUrl = "";
 	static String googleTransitTimezone = "";
 	static String googleTransitDefaultRouteType = "";
 	static String googleTransitOutfile = "";
 	
 	static String serviceStartDate;
 
 	static final String agencyFilename = "agency";
 	static final String stopsFilename = "stops";
 	static final String routesFilename = "routes";
 	static final String tripsFilename = "trips";
 	static final String stop_timesFilename = "stop_times";
 	static final String calendarFilename = "calendar";
 	static final String calendar_datesFilename = "calendar_dates";
 	static final String extension = ".txt";
 
 	static File outfile = null;
 	static PrintWriter out = null;
 	
 	public void parse(String filename, String url, String timezone, String defaultRouteType)
 	    throws SAXException, IOException, ParserConfigurationException
 	{
 		this.setUrl(url);
 		this.setTimezone(timezone);
 		this.setDefaultRouteType(defaultRouteType);
 		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
         SAXParser parser = parserFactory.newSAXParser();
     	parser.parse(new File(filename), this);	
 	}
 	
 	public void setUrl(String url) {
 		googleTransitUrl = url;
 	}
 
 	public void setTimezone(String timezone) {
 		googleTransitTimezone = timezone;
 	}
 
 	public void setDefaultRouteType(String defaultRouteType) {
 		googleTransitDefaultRouteType = defaultRouteType;
 	}
 	
 	public String getUrl() {
 		return googleTransitUrl;
 	}
 
 	public String getTimezone() {
 		return googleTransitTimezone;
 	}
 
 	public String getDefaultRouteType() {
 		return googleTransitDefaultRouteType;
 	}
 	
 	public TransxchangeAgency getAgencies() {
 		return agencies;
 	}
 	
 	public TransxchangeStops getStops() {
 		return stops;
 	}
 	
 	public TransxchangeRoutes getRoutes() {
 		return routes;
 	}
 	
 	public TransxchangeTrips getTrips() {
 		return trips;
 	}
 
 	public TransxchangeStopTimes getStopTimes() {
 		return stopTimes;
 	}
 	
 	public TransxchangeCalendar getCalendar() {
 		return calendar;
 	}
 	
 	public TransxchangeCalendarDates getCalendarDates() {
 		return calendarDates;
 	}
 	
 	/*
 	 * start element
 	 */   	
 	public void startElement(String uri, String name, String qName, Attributes atts) {
 	    agencies.startElement(uri, name, qName, atts);
 	    stops.startElement(uri, name, qName, atts);
 	    routes.startElement(uri, name, qName, atts);
 	    trips.startElement(uri, name, qName, atts);
 	    stopTimes.startElement(uri, name, qName, atts);
 	    calendar.startElement(uri, name, qName, atts);
 	    calendarDates.startElement(uri, name, qName, atts);
 	}
 	
 	/*
 	 * parse element
 	 */   	
 	public void characters (char ch[], int start, int length) {
 		agencies.characters(ch, start, length);
 		stops.characters(ch, start, length);
 		routes.characters(ch, start, length);
 		trips.characters(ch, start, length);
 		stopTimes.characters(ch, start, length);
 		calendar.characters(ch, start, length);
 		calendarDates.characters(ch, start, length);
 	}
     
 	/*
  	 * end element
  	 */   	
 	public void endElement (String uri, String name, String qName) {
 
 		// take care of element
 		agencies.endElement(uri, name, qName);
 		stops.endElement(uri, name, qName);
 		routes.endElement(uri, name, qName);
 		trips.endElement(uri, name, qName);
 		stopTimes.endElement(uri, name, qName);
 		calendar.endElement(uri, name, qName);
 		calendarDates.endElement(uri, name, qName);
 	
 		// clear keys
 		agencies.clearKeys(qName);
 		stops.clearKeys(qName);
 		routes.clearKeys(qName);
 		trips.clearKeys(qName);
 		stopTimes.clearKeys(qName);
 		calendar.clearKeys(qName);
 		calendarDates.clearKeys(qName);
 
 	}
 
 	/*
 	 * complete and dump Google Transit data feed data structure
 	 */   	
 	public void endDocument() {
     
 		// wrap up document parsing
 		agencies.endDocument();
 		stops.endDocument();
 		routes.endDocument();
 		trips.endDocument();
 		stopTimes.endDocument();
 		calendar.endDocument();
 		calendarDates.endDocument();
         
 		// Complete data structures (by filling in default values if necessary)
 		agencies.completeData();
 		stops.completeData();
 		routes.completeData();
 		trips.completeData();
 		stopTimes.completeData();
 		calendar.completeData();
 		calendarDates.completeData();
     
 		// Dump parsed data to System.out
 /*
 		agencies.dumpValues();
 		stops.dumpValues();
 		routes.dumpValues();
 		trips.dumpValues();
 		stopTimes.dumpValues();
 		calendar.dumpValues();
 		calendarDates.dumpValues();
 */
 	}
 
 	public String writeOutput(String rootDirectory, String workDirectory)
 	throws IOException
 	{		
 		ArrayList filenames = new ArrayList();
 		String outfileName;
 		
 		String outdir = rootDirectory + workDirectory;
 	
 		// Service start date - append to file names and use to create directory for outfiles
         serviceStartDate = (String)((ValueList)this.getCalendar().getListCalendar__start_date().get(0)).getValue(0);        
         new File(outdir + "/" + serviceStartDate).mkdirs();
       
     	// agency.txt
        outfileName = agencyFilename + /* "_" + serviceStartDate + */ extension;
         outfile = new File(outdir + "/" + serviceStartDate + "/" + outfileName);
         filenames.add(outfileName);
         out = new PrintWriter(new FileWriter(outfile));
         out.println("agency_name,agency_url,agency_timezone");
         for (int i = 0; i < this.getAgencies().getListAgency__agency_name().size(); i++) {
         	out.print(((ValueList)this.getAgencies().getListAgency__agency_name().get(i)).getValue(0));
         	out.print(",");
         	out.print(((ValueList)this.getAgencies().getListAgency__agency_url().get(i)).getValue(0));
         	out.print(",");
         	out.println(((ValueList)this.getAgencies().getListAgency__agency_timezone().get(i)).getValue(0));
 /* Java 1.5        		out.printf("%s,%s,%s", 
 /*        			((ValueList)handler.getAgencies().getListAgency__agency_name().get(i)).getValue(0),
 /*        			((ValueList)handler.getAgencies().getListAgency__agency_url().get(i)).getValue(0),
 /*        			((ValueList)handler.getAgencies().getListAgency__agency_timezone().get(i)).getValue(0));
 /*        		out.println();
 */
         }
         out.close();
        
         // stops.txt
        outfileName = stopsFilename + /* "_" + serviceStartDate + */ extension;
         outfile = new File(outdir + "/" + serviceStartDate + "/" + outfileName);
         filenames.add(outfileName);
         out = new PrintWriter(new FileWriter(outfile));
         out.println("stop_id,stop_name,stop_desc,stop_lat,stop_lon,stop_street,stop_city,stop_region,stop_postcode,stop_country");
         for (int i = 0; i < this.getStops().getListStops__stop_id().size(); i++) {
         	out.print(((ValueList)this.getStops().getListStops__stop_id().get(i)).getValue(0));
         	out.print(",");
         	out.print(((ValueList)this.getStops().getListStops__stop_name().get(i)).getValue(0));
         	out.print(",");
         	out.print(((ValueList)this.getStops().getListStops__stop_desc().get(i)).getValue(0));
         	out.print(",");
         	out.print(((ValueList)this.getStops().getListStops__stop_lat().get(i)).getValue(0));
         	out.print(",");
         	out.print(((ValueList)this.getStops().getListStops__stop_lon().get(i)).getValue(0));
         	out.print(",");
         	out.print(((ValueList)this.getStops().getListStops__stop_street().get(i)).getValue(0));
         	out.print(",");
         	out.print(((ValueList)this.getStops().getListStops__stop_city().get(i)).getValue(0));
         	out.print(",");
         	out.print(((ValueList)this.getStops().getListStops__stop_postcode().get(i)).getValue(0));
         	out.print(",");
         	out.print(((ValueList)this.getStops().getListStops__stop_region().get(i)).getValue(0));
         	out.print(",");
         	out.println(((ValueList)this.getStops().getListStops__stop_country().get(i)).getValue(0));
         	
 /* Java 1.5        		out.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", 
 /*        			((ValueList)this.getStops().getListStops__stop_id().get(i)).getValue(0),
 /*        			((ValueList)this.getStops().getListStops__stop_name().get(i)).getValue(0),
 /*        			((ValueList)this.getStops().getListStops__stop_desc().get(i)).getValue(0),
 /*        			((ValueList)this.getStops().getListStops__stop_lat().get(i)).getValue(0),
 /*        			((ValueList)this.getStops().getListStops__stop_lon().get(i)).getValue(0),
 /*        			((ValueList)this.getStops().getListStops__stop_street().get(i)).getValue(0),
 /*        			((ValueList)this.getStops().getListStops__stop_city().get(i)).getValue(0),
 /*        			((ValueList)this.getStops().getListStops__stop_postcode().get(i)).getValue(0),
 /*        			((ValueList)this.getStops().getListStops__stop_region().get(i)).getValue(0),
 /*        			((ValueList)this.getStops().getListStops__stop_country().get(i)).getValue(0));
 /*        		out.println();
  */
         }       
         out.close();
        
         // routes.txt
        outfileName = routesFilename + /* "_" + serviceStartDate + */ extension;
         outfile = new File(outdir + "/" + serviceStartDate + "/" + outfileName);
         filenames.add(outfileName);      
         out = new PrintWriter(new FileWriter(outfile));
         out.println("route_id,route_short_name,route_long_name,route_desc,route_type");
         for (int i = 0; i < this.getRoutes().getListRoutes__route_id().size(); i++) {
         	out.print(((ValueList)this.getRoutes().getListRoutes__route_id().get(i)).getValue(0));
         	out.print(",");
         	out.print(((ValueList)this.getRoutes().getListRoutes__route_short_name().get(i)).getValue(0));
         	out.print(",");
         	out.print(((ValueList)this.getRoutes().getListRoutes__route_long_name().get(i)).getValue(0));
         	out.print(",");
         	out.print(((ValueList)this.getRoutes().getListRoutes__route_desc().get(i)).getValue(0));
         	out.print(",");
         	out.println(((ValueList)this.getRoutes().getListRoutes__route_type().get(i)).getValue(0));     
 
 /* Java 1.5        		out.printf("%s,%s,%s,%s,%s", 
 /*        			((ValueList)this.getRoutes().getListRoutes__route_id().get(i)).getValue(0),
 /*        			((ValueList)this.getRoutes().getListRoutes__route_short_name().get(i)).getValue(0),
 /*        			((ValueList)this.getRoutes().getListRoutes__route_long_name().get(i)).getValue(0),
 /*        			((ValueList)this.getRoutes().getListRoutes__route_desc().get(i)).getValue(0),
 /*        			((ValueList)this.getRoutes().getListRoutes__route_type().get(i)).getValue(0));     
 /*      		out.println();
 */
         }       
         out.close();
 
         // trips.txt
        outfileName = tripsFilename + /* "_" + serviceStartDate + */ extension;
         outfile = new File(outdir + "/" + serviceStartDate + "/" + outfileName);
         out = new PrintWriter(new FileWriter(outfile));
         filenames.add(outfileName);      
         out.println("route_id,service_id,trip_id,trip_headsign,block_id");
         for (int i = 0; i < this.getTrips().getListTrips__route_id().size(); i++) {
         	out.print(((ValueList)this.getTrips().getListTrips__route_id().get(i)).getValue(0));
         	out.print(",");
         	out.print(((ValueList)this.getTrips().getListTrips__service_id().get(i)).getValue(0));
         	out.print(",");
         	out.print(((ValueList)this.getTrips().getListTrips__trip_id().get(i)).getKeyName());
         	out.print(",");
         	out.print(((ValueList)this.getTrips().getListTrips__trip_headsign().get(i)).getValue(0));
         	out.print(",");
         	out.println(((ValueList)this.getTrips().getListTrips__block_id().get(i)).getValue(0));
 
 /* Java 1.5       	 	out.printf("%s,%s,%s,%s,%s", 
 /*        			((ValueList)this.getTrips().getListTrips__route_id().get(i)).getValue(0),
 /*        			((ValueList)this.getTrips().getListTrips__service_id().get(i)).getValue(0),
 /*        			((ValueList)this.getTrips().getListTrips__trip_id().get(i)).getKeyName(),
 /*        			((ValueList)this.getTrips().getListTrips__trip_headsign().get(i)).getValue(0),
 /*        			((ValueList)this.getTrips().getListTrips__block_id().get(i)).getValue(0));
 /*        		out.println();
 */
         }       
         out.close();
 
         // stop_times.txt
        outfileName = stop_timesFilename + /* "_" + serviceStartDate + */ extension;
         outfile = new File(outdir + "/" + serviceStartDate + "/" + outfileName);
         filenames.add(outfileName);      
         out = new PrintWriter(new FileWriter(outfile));
         out.println("trip_id,arrival_time,departure_time,stop_id,stop_sequence,pickup_type,drop_off_type");
         for (int i = 0; i < this.getStopTimes().getListStoptimes__trip_id().size(); i++) {
         	out.print(((ValueList)this.getStopTimes().getListStoptimes__trip_id().get(i)).getKeyName());
         	out.print(",");
         	out.print(((ValueList)this.getStopTimes().getListStoptimes__arrival_time().get(i)).getValue(0));
         	out.print(",");
         	out.print(((ValueList)this.getStopTimes().getListStoptimes__departure_time().get(i)).getValue(0));
         	out.print(",");
         	out.print(((ValueList)this.getStopTimes().getListStoptimes__stop_id().get(i)).getValue(0));
         	out.print(",");
         	out.print(((ValueList)this.getStopTimes().getListStoptimes__stop_sequence().get(i)).getValue(0));
         	out.print(",");
         	out.print(((ValueList)this.getStopTimes().getListStoptimes__pickup_type().get(i)).getValue(0));
         	out.print(",");
         	out.println(((ValueList)this.getStopTimes().getListStoptimes__drop_off_type().get(i)).getValue(0));
 
 /* Java 1.5        		out.printf("%s,%s,%s,%s,%s,%s,%s", 
 /*           			((ValueList)this.getStopTimes().getListStoptimes__trip_id().get(i)).getKeyName(),
 /*           			((ValueList)this.getStopTimes().getListStoptimes__arrival_time().get(i)).getValue(0),
 /*           			((ValueList)this.getStopTimes().getListStoptimes__departure_time().get(i)).getValue(0),
 /*           			((ValueList)this.getStopTimes().getListStoptimes__stop_id().get(i)).getValue(0),
 /*           			((ValueList)this.getStopTimes().getListStoptimes__stop_sequence().get(i)).getValue(0),
 /*           			((ValueList)this.getStopTimes().getListStoptimes__pickup_type().get(i)).getValue(0),
 /*           			((ValueList)this.getStopTimes().getListStoptimes__drop_off_type().get(i)).getValue(0));
 /*        		out.println();
 */
         }       
         out.close();
         
         // calendar.txt
        outfileName = calendarFilename + /* "_" + serviceStartDate + */ extension;
         outfile = new File(outdir + "/" + serviceStartDate + "/" + outfileName);
         filenames.add(outfileName);
         out = new PrintWriter(new FileWriter(outfile));
         out.println("service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date");
         for (int i = 0; i < this.getCalendar().getListCalendar__service_id().size(); i++) {
         	out.print(((ValueList)this.getCalendar().getListCalendar__service_id().get(i)).getValue(0));
         	out.print(",");
         	out.print(((ValueList)this.getCalendar().getListCalendar__monday().get(i)).getValue(0));
         	out.print(",");
         	out.print(((ValueList)this.getCalendar().getListCalendar__tuesday().get(i)).getValue(0));
         	out.print(",");
         	out.print(((ValueList)this.getCalendar().getListCalendar__wednesday().get(i)).getValue(0));
         	out.print(",");
         	out.print(((ValueList)this.getCalendar().getListCalendar__thursday().get(i)).getValue(0));
         	out.print(",");
         	out.print(((ValueList)this.getCalendar().getListCalendar__friday().get(i)).getValue(0));
         	out.print(",");
         	out.print(((ValueList)this.getCalendar().getListCalendar__saturday().get(i)).getValue(0));
         	out.print(",");
         	out.print(((ValueList)this.getCalendar().getListCalendar__sunday().get(i)).getValue(0));
         	out.print(",");
         	out.print(((ValueList)this.getCalendar().getListCalendar__start_date().get(i)).getValue(0));
         	out.print(",");
         	out.println(((ValueList)this.getCalendar().getListCalendar__end_date().get(i)).getValue(0));           			
 
 /* Java 1.5        		out.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", 
 /*           			((ValueList)this.getCalendar().getListCalendar__service_id().get(i)).getValue(0),
 /*           			((ValueList)this.getCalendar().getListCalendar__monday().get(i)).getValue(0),
 /*           			((ValueList)this.getCalendar().getListCalendar__tuesday().get(i)).getValue(0),
 /*           			((ValueList)this.getCalendar().getListCalendar__wednesday().get(i)).getValue(0),
 /*           			((ValueList)this.getCalendar().getListCalendar__thursday().get(i)).getValue(0),
 /*           			((ValueList)this.getCalendar().getListCalendar__friday().get(i)).getValue(0),
 /*           			((ValueList)this.getCalendar().getListCalendar__saturday().get(i)).getValue(0),
 /*           			((ValueList)this.getCalendar().getListCalendar__sunday().get(i)).getValue(0),
 /*           			((ValueList)this.getCalendar().getListCalendar__start_date().get(i)).getValue(0),
 /*           			((ValueList)this.getCalendar().getListCalendar__end_date().get(i)).getValue(0));           			
 /*        		out.println();
 */
         }       
         out.close();
 
         // calendar_dates.txt
        outfileName = calendar_datesFilename + /* "_" + serviceStartDate + */ extension;
         outfile = new File(outdir + "/" + serviceStartDate + "/" + outfileName);
         out = new PrintWriter(new FileWriter(outfile));
         filenames.add(outfileName);
         out.println("service_id,date,exception_type");
         for (int i = 0; i < this.getCalendarDates().getListCalendarDates__service_id().size(); i++) {
         	out.print(((ValueList)this.getCalendarDates().getListCalendarDates__service_id().get(i)).getValue(0));
         	out.print(",");
         	out.print(((ValueList)this.getCalendarDates().getListCalendarDates__date().get(i)).getValue(0));
         	out.print(",");
         	out.println(((ValueList)this.getCalendarDates().getListCalendarDates__exception_type().get(i)).getValue(0));           			
 
 /* Java 1.5        		out.printf("%s,%s,%s", 
 /*           			((ValueList)this.getCalendarDates().getListCalendarDates__service_id().get(i)).getValue(0),
 /*           			((ValueList)this.getCalendarDates().getListCalendarDates__date().get(i)).getValue(0),
 /*           			((ValueList)this.getCalendarDates().getListCalendarDates__exception_type().get(i)).getValue(0));           			
 /*        		out.println();
 */
         }       
         out.close();
       
         // Compress the files
         ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outdir + "/" + serviceStartDate + "/" + "google_transit.zip"));
         byte[] buf = new byte[1024]; // Create a buffer for reading the files
         for (int i = 0; i < filenames.size(); i++) {
             FileInputStream in = new FileInputStream(outdir + "/" + serviceStartDate + "/" + (String)filenames.get(i));
     
             // Add ZIP entry to output stream.
             out.putNextEntry(new ZipEntry((String)filenames.get(i)));
     
             // Transfer bytes from the file to the ZIP file
             int len;
             while ((len = in.read(buf)) > 0) {
                 out.write(buf, 0, len);
             }
     
             // Complete the entry
             out.closeEntry();
             in.close();
         }
     
         // Complete the ZIP file
         out.close();
         
         return workDirectory+ "/" + serviceStartDate + "/" + "google_transit.zip";
 	}
 		
 	public TransxchangeHandler () {
 		agencies = new TransxchangeAgency(this);
 		stops = new TransxchangeStops(this);
 		routes = new TransxchangeRoutes(this);
 		trips = new TransxchangeTrips(this);
 		stopTimes = new TransxchangeStopTimes(this);
 		calendar = new TransxchangeCalendar(this);
 		calendarDates = new TransxchangeCalendarDates(this);
 	}
 }
