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
 
 package transxchange2GoogleTransit.handler;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.Map;
 import java.util.HashMap;
 import java.io.*;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXParseException;
 
 import transxchange2GoogleTransit.Stop;
 
 /*
  * This class handles the TransXChange xml input file under the aspect of
  * 	stops
  */
 public class TransxchangeStops extends TransxchangeDataAspect{
 
 	// xml keys and output field fillers
 	static final String[] key_stops__stop_id = new String[] {"StopPoints", "AtcoCode", "OpenRequired"}; // GTFS required
 	static final String[] key_stops__stop_id2 = new String[] {"StopPoints", "StopPointRef", "OpenRequired"}; // GTFS required
 	static final String[] key_stops__stop_name = new String[] {"StopPoints", "CommonName", "OpenRequired"}; // GTFS required
 	static final String[] key_stops__stop_desc = new String[] {"__transxchange2GTFS_drawDefault", "", ""};
 	static final String[] key_stops__stop_east = new String[] {"StopPoints", "Easting", "OpenRequired"};
 	static final String[] key_stops__stop_north = new String[] {"StopPoints", "Northing", "OpenRequired"};
 	static final String[] key_stops__stop_lat = new String[] {"StopPoints", "Latitude", "OpenRequired"}; // GTFS required
 	static final String[] key_stops__stop_lon = new String[] {"StopPoints", "Longitude", "OpenRequired"}; // GTFS required
 	static final String[] key_stops__stop_street = new String[] {"__transxchange2GTFS_drawDefault", "", ""};
 	static final String[] key_stops__stop_city = new String[] {"__transxchange2GTFS_drawDefault", "", ""};
 	static final String[] key_stops__stop_region = new String[] {"__transxchange2GTFS_drawDefault", "", ""};
 	static final String[] key_stops__stop_postcode = new String[] {"__transxchange2GTFS_drawDefault", "", ""};
 	static final String[] key_stops__stop_country = new String[] {"__transxchange2GTFS_drawDefault", "", ""};
 
 	static final String [] _key_stops__stop_locality = new String[] {"StopPoints", "LocalityName"};
 	List<ValueList> _listStops__stop_locality;
 	ValueList _newStops__stop_locality;
 	static final String [] _key_stops__stop_indicator = new String[] {"StopPoints", "Indicator"};
 	List<ValueList> _listStops__stop_indicator;
 	ValueList _newStops__stop_indicator;
 
 	static final String[] _key_route_section = {"RouteSection"};
 	static final String[] _key_route_link_from = new String [] {"RouteLink", "From", "StopPointRef"};
 	static final String[] _key_route_link_to = new String [] {"RouteLink", "To", "StopPointRef"};
 	static final String[] _key_route_link_location_x = new String [] {"RouteLink", "Easting-removed!"}; // v1.7.5. Do not pick up Easting/Northing from route links any longer
 	static final String[] _key_route_link_location_y = new String [] {"RouteLink", "Northing-removed!"}; // v1.7.5
 	boolean inRouteSection = false;
 	String keyNestedLocation = "";
 	String stopPointFrom = "";
 	String stopPointTo = "";
 	String stopPointToLat = ""; // Store current lat of stop-to to maintain lat of last stop in route link
 	String stopPointToLon = ""; // same for lon
 
 	static Map<String, String> lat = null;
 	static Map<String, String> lon = null;
 	static Map<String, Integer> stopIx = null;
 
 	static Map<String, String> stops = null;
 
 	static Map<String, Integer> stopColumnIxs = null;
 	static List[] columnValues = {null, null, null, null, null, null, null, null, null, null,
 		null, null, null, null, null, null, null, null, null, null,
 		null, null, null, null, null, null, null, null, null, null,
 		null, null, null, null, null, null, null, null, null, null,
 		null, null, null, null, null, null, null, null, null, null};
 
 	static final String[] _key_stops_alternative_descriptor = new String[] {"StopPoints", "AlternativeDescriptors", "CommonName"};
 
 	String keyRef = "";
 
 	// Parsed data
 	List<ValueList> listStops__stop_id;
 	List<ValueList> listStops__stop_name;
 	List<ValueList> listStops__stop_desc;
 	List<ValueList> listStops__stop_lat;
 	List<ValueList> listStops__stop_lon;
 	List<ValueList> listStops__stop_street;
 	List<ValueList> listStops__stop_city;
 	List<ValueList> listStops__stop_postcode;
 	List<ValueList> listStops__stop_region;
 	List<ValueList> listStops__stop_country;
 
 	public List<ValueList> getListStops__stop_id() {
 		return listStops__stop_id;
 	}
 	public List<ValueList> getListStops__stop_name() {
 		return listStops__stop_name;
 	}
 	public List<ValueList> getListStops__stop_desc() {
 		return listStops__stop_desc;
 	}
 	public List<ValueList> getListStops__stop_lat() {
 		return listStops__stop_lat;
 	}
 	public List<ValueList> getListStops__stop_lon() {
 		return listStops__stop_lon;
 	}
 	public List<ValueList> getListStops__stop_street() {
 		return listStops__stop_street;
 	}
 	public List<ValueList> getListStops__stop_city() {
 		return listStops__stop_city;
 	}
 	public List<ValueList> getListStops__stop_postcode() {
 		return listStops__stop_postcode;
 	}
 	public List<ValueList> getListStops__stop_region() {
 		return listStops__stop_region;
 	}
 	public List<ValueList> getListStops__stop_country() {
 		return listStops__stop_country;
 	}
 
 	public void addStop(String stopId) {
 		if (stopId == null || stopId.length() == 0)
 			return;
 		if (stops == null)
 			stops = new HashMap<String, String>();
 		stops.put(stopId, "0");
 	}
 	public boolean hasStop(String testId) {
 		if (stops == null || testId == null || testId.length() == 0)
 			return false;
 		if (!stops.containsKey(testId))
 			return false;
 		if (!(stops.get(testId)).equals("1"))
 			return false;
 		return true;
 	}
 	public void flagStop(String stopId) {
 		if (stops == null || stopId == null || stopId.length() == 0)
 			return;
 		stops.put(stopId, "1");
 	}
 	public void flagAllStops(String flag) {
 		if (stops == null || flag == null)
 			return;
 		for (String key : stops.keySet()) {
 			stops.put(key, flag);
 		}
 	}
 
    	@Override
 	public void startElement(String uri, String name, String qName, Attributes atts)
 		throws SAXParseException {
 
 		super.startElement(uri, name, qName, atts);
 		if (key.equals(key_stops__stop_id[0]))
 			if (qName.equals(key_stops__stop_id[1])) {
 				keyNested = key_stops__stop_id[1];
 			}
 		if (key.equals(key_stops__stop_id2[0]))
 			if (qName.equals(key_stops__stop_id2[1])) {
 				keyNested = key_stops__stop_id2[1];
 			}
 		if (key.equals(key_stops__stop_name[0]) && keyNested.length() == 0)
 			if (qName.equals(key_stops__stop_name[1])) {
 				keyNested = key_stops__stop_name[1];
 			}
 		if (key.equals(key_stops__stop_east[0]))
 			if (qName.equals(key_stops__stop_east[1])) {
 				keyNested = key_stops__stop_east[1];
 			}
 		if (key.equals(key_stops__stop_north[0]))
 			if (qName.equals(key_stops__stop_north[1])) {
 				keyNested = key_stops__stop_north[1];
 			}
 
 	    // Embedded coordinates
 		if (key.equals(key_stops__stop_lat[0]))
 			if (qName.equals(key_stops__stop_lat[1])) {
 				keyNested = key_stops__stop_lat[1];
 			}
 		if (key.equals(key_stops__stop_lon[0]))
 			if (qName.equals(key_stops__stop_lon[1])) {
 				keyNested = key_stops__stop_lon[1];
 			}
 
 		if (qName.equals(key_stops__stop_id[0]))
 			key = key_stops__stop_id[0];
 		if (key.equals(_key_stops__stop_locality[0]) && qName.equals(_key_stops__stop_locality[1])) {
 			keyNested = _key_stops__stop_locality[1];
 		}
 		if (key.equals(_key_stops__stop_indicator[0]) && qName.equals(_key_stops__stop_indicator[1])) {
 			keyNested = _key_stops__stop_indicator[1];
 		}
 
 		// Route sections (to helper structures)
 		// From and to stop points
 		if (qName.equals(_key_route_section[0]))
 			inRouteSection = !inRouteSection;
 		if (key.equals(_key_route_link_from[0]) && (keyNested.equals(_key_route_link_from[1]) || keyNested.equals(_key_route_link_to[1])) && qName.equals(_key_route_link_from[2])) {
 			keyNestedLocation = _key_route_link_from[2];
 		}
 		if (key.equals(_key_route_link_to[0]) && qName.equals(_key_route_link_to[1])) {
 			keyNested = _key_route_link_to[1];
 		}
 		if (key.equals(_key_route_link_from[0]) && qName.equals(_key_route_link_from[1])) {
 			keyNested = _key_route_link_from[1];
 		}
 		if (key.equals(_key_route_link_location_x[0]) && qName.equals(_key_route_link_location_x[1])) {
 			keyNested = _key_route_link_location_x[1];
 		}
 		if (key.equals(_key_route_link_location_y[0]) && qName.equals(_key_route_link_location_y[1])) {
 			keyNested = _key_route_link_location_y[1];
 		}
 		if (qName.equals(_key_route_link_from[0])) 	// this also covers route_link_location_x and _y
 			key = _key_route_link_from[0];
 
 		//  Alternative description
 		if (key.equals(_key_stops_alternative_descriptor[0]) && qName.equals(_key_stops_alternative_descriptor[1]))
 			keyNested = _key_stops_alternative_descriptor[1];
 	}
 
    	@Override
 	public void endElement (String uri, String name, String qName) {
 		int i;
 	    boolean hot;
 
 	    if (niceString == null || niceString.length() == 0)
 	    	return;
 
 	    if (key.equals(key_stops__stop_id[0]) && keyNested.equals(key_stops__stop_id[1])) {
 	      ValueList newStops__stop_id = new ValueList(key_stops__stop_id[0]);
 	    	listStops__stop_id.add(newStops__stop_id);
 	    	newStops__stop_id.addValue(niceString);
 	    	keyRef = niceString;
 	    }
 	    if (key.equals(key_stops__stop_id2[0]) && keyNested.equals(key_stops__stop_id2[1])) {
 	      ValueList newStops__stop_id = new ValueList(key_stops__stop_id2[0]);
 	    	listStops__stop_id.add(newStops__stop_id);
 	    	newStops__stop_id.addValue(niceString);
 	    	keyRef = niceString;
 	    }
 	    if (key.equals(key_stops__stop_name[0]) && keyNested.equals(key_stops__stop_name[1])) {
 	      ValueList newStops__stop_name = new ValueList(keyRef);
 	    	listStops__stop_name.add(newStops__stop_name);
 	    	newStops__stop_name.addValue(niceString);
 	    	ValueList newStops__stop_desc = new ValueList(keyRef); // Default for _desc
 	    	listStops__stop_desc.add(newStops__stop_desc);
 	    	newStops__stop_desc.addValue(key_stops__stop_desc[2]);
 	    	ValueList newStops__stop_street = new ValueList(keyRef); // Default for _street
 	    	listStops__stop_street.add(newStops__stop_street);
 	    	newStops__stop_street.addValue(key_stops__stop_street[2]);
 	    	ValueList newStops__stop_city = new ValueList(keyRef); // Default for _city
 	    	listStops__stop_city.add(newStops__stop_city);
 	    	newStops__stop_city.addValue(key_stops__stop_city[2]);
 	    	ValueList newStops__stop_postcode = new ValueList(keyRef); // Default for _postcode
 	    	listStops__stop_postcode.add(newStops__stop_postcode);
 	    	newStops__stop_postcode.addValue(key_stops__stop_postcode[2]);
 	    	ValueList newStops__stop_region = new ValueList(keyRef); // Default for _region
 	    	listStops__stop_region.add(newStops__stop_region);
 	    	newStops__stop_region.addValue(key_stops__stop_region[2]);
 	    	ValueList newStops__stop_country = new ValueList(keyRef); // Default for _country
 	    	listStops__stop_country.add(newStops__stop_country);
 	    	newStops__stop_country.addValue(key_stops__stop_country[2]);
 	    }
 	    if (key.equals(_key_stops__stop_locality[0]) && keyNested.equals(_key_stops__stop_locality[1])) {
 	    	_newStops__stop_locality = new ValueList(keyRef);
 	    	_listStops__stop_locality.add(_newStops__stop_locality);
 	    	_newStops__stop_locality.addValue(niceString);
 	    }
 	    if (key.equals(_key_stops__stop_indicator[0]) && keyNested.equals(_key_stops__stop_indicator[1])) {
 	    	_newStops__stop_indicator = new ValueList(keyRef);
 	    	_listStops__stop_indicator.add(_newStops__stop_indicator);
 	    	_newStops__stop_indicator.addValue(niceString);
 	    }
 	    if (key.equals(key_stops__stop_east[0]) && keyNested.equals(key_stops__stop_east[1])) {
 	    	ValueList newStops__stop_lat = new ValueList(keyRef);
 	    	listStops__stop_lat.add(newStops__stop_lat);
 	    	newStops__stop_lat.addValue(niceString);
        	}
 	    if (key.equals(key_stops__stop_north[0]) && keyNested.equals(key_stops__stop_north[1])) {
 	      ValueList newStops__stop_lon = new ValueList(keyRef);
 	       	listStops__stop_lon.add(newStops__stop_lon);
 	       	newStops__stop_lon.addValue(niceString);
        	}
 
 	    // Embedded coordinates
 	    if (key.equals(key_stops__stop_lat[0]) && keyNested.equals(key_stops__stop_lat[1])) {
 	      ValueList newStops__stop_lat = new ValueList(keyRef);
 	    	listStops__stop_lat.add(newStops__stop_lat);
 	    	newStops__stop_lat.addValue(niceString);
        	}
 	    if (key.equals(key_stops__stop_lon[0]) && keyNested.equals(key_stops__stop_lon[1])) {
 	      ValueList newStops__stop_lon = new ValueList(keyRef);
 	       	listStops__stop_lon.add(newStops__stop_lon);
 	       	newStops__stop_lon.addValue(niceString);
        	}
 
 	    // if location of stop unknown from stop points, add location from route section
 	    if (key.equals(_key_route_link_location_x[0]) && keyNested.equals(_key_route_link_location_x[1]) && stopPointFrom.length() > 0) {
 	       	i = 0;
 	       	hot = true;
 	       	while (hot && i < listStops__stop_lat.size()) {
 	       		if (stopPointFrom.equals((listStops__stop_lat.get(i)).getKeyName()))
 	       			hot = false;
 	       		else
 	       			i++;
 	       	}
 	    	if (hot) {
 	    	  ValueList newStops__stop_lat = new ValueList(stopPointFrom);
        			listStops__stop_lat.add(newStops__stop_lat);
        			newStops__stop_lat.addValue(niceString);
        		}
 	   }
 	   if (key.equals(_key_route_link_location_y[0]) && keyNested.equals(_key_route_link_location_y[1]) && stopPointFrom.length() > 0) {
 	    	i = 0;
 	    	hot = true;
 	    	while (hot && i < listStops__stop_lon.size()) {
 	    		if (stopPointFrom.equals((listStops__stop_lon.get(i)).getKeyName()))
 	    			hot = false;
 	    		else
 	    			i++;
 	    	}
 	    	if (hot) {
 	    	  ValueList newStops__stop_lon = new ValueList(stopPointFrom);
 	    		listStops__stop_lon.add(newStops__stop_lon);
 	    		newStops__stop_lon.addValue(niceString);
 	    		stopPointFrom = "";
 	    	}
 	   }
 
 	   // Route sections (to stop point lat and lon), based on from- and to-stop points
 	   if (key.equals(_key_route_link_from[0]) && keyNested.equals(_key_route_link_from[1])&& keyNestedLocation.equals(_key_route_link_from[2])) {
 	    	stopPointFrom = niceString;
 	    	keyNestedLocation = "";
 	   }
 	   if (key.equals(_key_route_link_to[0]) && keyNested.equals(_key_route_link_to[1])&& keyNestedLocation.equals(_key_route_link_to[2])) {
 	      	stopPointTo = niceString;
 	    	keyNestedLocation = "";
 	    	keyNested = "";
 	    }
 	    if (key.equals(_key_route_link_location_x[0]) && keyNested.equals(_key_route_link_location_x[1])) {
 	    	stopPointToLat = niceString;
 	    }
 	    if (key.equals(_key_route_link_location_y[0]) && keyNested.equals(_key_route_link_location_y[1])) {
 	    	stopPointToLon = niceString;
 	    }
 	}
 
    	@Override
    	public void clearKeys (String qName) {
     	if (inRouteSection) {
     		if (keyNested.equals(_key_route_link_location_x[1]))
     			keyNestedLocation = "";
     		if (keyNestedLocation.equals(_key_route_link_from[2]))
     			keyNestedLocation = "";
     		if (keyNested.equals(_key_route_link_from[1]))
     			keyNested = "";
     		if (qName.equals(_key_route_link_location_y[1]))
     			keyNested = "";
     	}
     	if (qName.equals(_key_route_section[0])) {
     		if (inRouteSection) {
     			if (stopPointToLat.length() > 0) {
     			  ValueList newStops__stop_lat = new ValueList(stopPointTo); // last stop in route section
     				listStops__stop_lat.add(newStops__stop_lat);
     				newStops__stop_lat.addValue(stopPointToLat);
     			}
     			if (stopPointToLon.length() > 0) {
     			  ValueList newStops__stop_lon = new ValueList(stopPointTo); // last stop in route section
     				listStops__stop_lon.add(newStops__stop_lon);
     				newStops__stop_lon.addValue(stopPointToLon);
     			}
     		}
     		inRouteSection = !inRouteSection;
     		key = "";
     	}
     	if (key.equals(key_stops__stop_id[0]))
     		keyNested = "";
     	if (qName.equals(key_stops__stop_id[0]))
     		key = "";
     	if (key.equals(key_stops__stop_id[0]))
     		keyNested = "";
     	if (key.equals(key_stops__stop_east[0]))
     		keyNested = "";
     	if (key.equals(key_stops__stop_north[0]))
     		keyNested = "";
 
 	    // Embedded coordinates
     	if (key.equals(key_stops__stop_lat[0]))
     		keyNested = "";
     	if (key.equals(key_stops__stop_lon[0]))
     		keyNested = "";
    	}
 
    	@Override
 	public void endDocument() {
 	    int i, j;
 	    ValueList iterator, jterator;
 	    String stopId;
 	    boolean hot;
 	    String indicator, locality, stopname, naptanPick;
 	    Integer index;
 
 	    // Backfill missing stop coordinates with default lat/lon
 	    for (i = 0; i < listStops__stop_id.size(); i++) {
 	    	iterator = listStops__stop_id.get(i);
 	    	stopId = iterator.getValue(0);
 	    	j = 0;
 	    	hot = true;
 	    	while (hot && j < listStops__stop_lat.size()) {
 	    		jterator = listStops__stop_lat.get(j);
 	    		if (jterator.getKeyName().equals(stopId))
 	    			hot = false;
 	    		else
 	    			j++;
 	    	}
 	    	if (hot) {
 	    	  ValueList newStops__stop_lat = new ValueList(stopId);
 	    	  listStops__stop_lat.add(i, newStops__stop_lat);
 	    	  newStops__stop_lat.addValue(getLat(stopId));
 	    	  ValueList newStops__stop_lon = new ValueList(stopId);
 	    	  listStops__stop_lon.add(i, newStops__stop_lon);
 	    	  newStops__stop_lon.addValue(getLon(stopId));
 	    	}
 	    }
 
 	    // Roll stop locality and indicator into stopname
     	List<String> stopColumns = handler.getStopColumns();
     	if (stopColumns == null)
     		if (handler.getNaptanHelperStopColumn() == -1)
 			    for (i = 0; i < listStops__stop_name.size(); i++) {
 			    	indicator = "";
 			    	locality = "";
 			    	iterator = listStops__stop_name.get(i);
 			    	stopId = iterator.getKeyName();
 			    	j = 0; // Find locality
 			    	hot = true;
 			    	jterator = null;
 			    	while (hot && j < _listStops__stop_locality.size()) {
 			    		jterator = _listStops__stop_locality.get(j);
 			    		if (jterator.getKeyName().equals(stopId))
 			    			hot = false;
 			    		else
 			    			j++;
 			    	}
 			    	if (!hot)
 			    		locality = jterator.getValue(0);
 			    	j = 0; // Find indicator
 			    	hot = true;
 			    	jterator = null;
 			    	while (hot && j < _listStops__stop_indicator.size()) {
 			    		jterator = _listStops__stop_indicator.get(j);
 			    		if (jterator.getKeyName().equals(stopId))
 			    			hot = false;
 			    		else
 			    			j++;
 			    	}
 			    	if (!hot)
 			    		indicator = jterator.getValue(0);
 
 			    	if (locality.length() > 0 && iterator != null) // Prefix locality
 			    		iterator.setValue(0, locality + ", " + iterator.getValue(0));
 			    	if (indicator.length() > 0 && iterator != null) // Postfix indicator
 			        	iterator.setValue(0, iterator.getValue(0) + ", "+ indicator);
 			    }
     		else
 			    for (i = 0; i < listStops__stop_name.size(); i++) {
 			    	iterator = listStops__stop_name.get(i);
 			    	stopId = iterator.getKeyName();
 			    	iterator.setValue(0, handler.getNaPTANStopname(stopId));
 			    }
     	else
 		    for (i = 0; i < listStops__stop_name.size(); i++) {
 //		    	iterator = listStops__stop_id.get(i);
 //		    	stopId = iterator.getValue(0);
 		    	iterator = listStops__stop_name.get(i);
 		    	stopId = iterator.getKeyName();
 		    	stopname = "";
 		    	for (j = 0; j < 30; j++) {
 		    		if (columnValues[j] != null) {
 		    			if (stopname.length() > 0)
 		    				stopname += handler.getStopfilecolumnseparator(); // ",";
 		    			index = (Integer)stopIx.get(stopId);
 		    			if (index == null) {
 		    				if (stopname.length() == 0)
 		    					stopname += "OpenRequired";//FIXME BUG, don't do that
 		    			} else {
 		    				naptanPick = (String) columnValues[j].get((Integer)stopIx.get(stopId));
 		    				naptanPick = naptanPick.replaceAll("\"", "");
 		    				stopname += naptanPick;
 			    		}
 		    		}
 		    	}
 		    	iterator.setValue(0, stopname);
 		    }
 	}
 
    	@Override
 	public void completeData() {
   	    // Add quotes if needed
   	    csvProofList(listStops__stop_id);
   	    csvProofList(listStops__stop_name);
   	    csvProofList(listStops__stop_desc);
   	    csvProofList(listStops__stop_lat);
   	    csvProofList(listStops__stop_lon);
   	    csvProofList(listStops__stop_street);
   	    csvProofList(listStops__stop_city);
   	    csvProofList(listStops__stop_postcode);
   	    csvProofList(listStops__stop_region);
  	    csvProofList(listStops__stop_country);
 	}
 
    	@Override
 	public void dumpValues() {
 		int i;
 		ValueList iterator;
 
 	    System.out.println("*** Stops");
 	    for (i = 0; i < listStops__stop_id.size(); i++) {
 	    	iterator = listStops__stop_id.get(i);
 	    	iterator.dumpValues();
 	    }
 	    for (i = 0; i < listStops__stop_name.size(); i++) {
 	    	iterator = listStops__stop_name.get(i);
 	    	iterator.dumpValues();
 	    }
 	    for (i = 0; i < listStops__stop_lat.size(); i++) {
 	    	iterator = listStops__stop_lat.get(i);
 	    	iterator.dumpValues();
 	    }
 	    for (i = 0; i < listStops__stop_lon.size(); i++) {
 	    	iterator = listStops__stop_lon.get(i);
 	    	iterator.dumpValues();
 	    }
 
 	}
 
 	private String getLat(String stop) {
 		if (lat == null || !lat.containsKey(stop)){
 		  Stop nStop = handler.getStop(stop);
 		  if (nStop != null){
 		    return nStop.getPosition().latitude;
 		  }
 			return null;
 		} else {
 		  return lat.get(stop);
 		}
 	}
 
 	private String getLon(String stop) {
 	  if (lon == null || !lon.containsKey(stop)){
       Stop nStop = handler.getStop(stop);
       if (nStop != null){
         return nStop.getPosition().longitude;
       }
       return null;
     } else {
       return lon.get(stop);
     }
 	}
 
 /*	private static int findColumn(String headline, String code) {
 		if (headline == null || code == null)
 			return -1;
 
 		StringTokenizer st = new StringTokenizer(headline, ",");
 		String token;
 		int counter = 0;
 		boolean found = false;
 		while (!found && st.hasMoreTokens()) {
 			token = st.nextToken();
 			if (token.equals(code))
 				found = true;
 			else
 				counter++;
 		}
 		if (!found)
 			return -1;
 		return counter;
 	}
 */
 	public TransxchangeStops(TransxchangeHandlerEngine owner) {
 		super(owner);
 		listStops__stop_id = new ArrayList<ValueList>();
 		listStops__stop_name = new ArrayList<ValueList>();
 		listStops__stop_desc = new ArrayList<ValueList>();
 		listStops__stop_lat = new ArrayList<ValueList>();
 		listStops__stop_lon = new ArrayList<ValueList>();
 		listStops__stop_street = new ArrayList<ValueList>();
 		listStops__stop_city = new ArrayList<ValueList>();
 		listStops__stop_postcode = new ArrayList<ValueList>();
 		listStops__stop_region = new ArrayList<ValueList>();
 		listStops__stop_country = new ArrayList<ValueList>();
 
 		_listStops__stop_locality = new ArrayList<ValueList>();
 		_listStops__stop_indicator = new ArrayList<ValueList>();
 	}
 
 
 	public static void readStopfile(String stopsFileName, List<String> stopColumns)
 		throws UnsupportedEncodingException, IOException {
 
 		// Read Naptan format stop file
 		if (!(stopsFileName != null && stopsFileName.length() > 0))
 			return;
 
 		BufferedReader bufFileIn = new BufferedReader(new FileReader(stopsFileName));
 
 		// Read first line to find column positions of stopcode, lat and lon
 		String line;
 		int stopcodeIx;
 		int latIx;
 		int lonIx;
 		if ((line = bufFileIn.readLine()) != null) {
 			if ((stopcodeIx = NaPTANHelper.findColumn(line, "\"AtcoCode\"")) == -1)
 				throw new IOException("stopfile column AtcoCode not found");
 			if ((latIx = NaPTANHelper.findColumn(line, "\"Latitude\"")) == -1)
 				throw new IOException("stopfile column Latitude not found");
 			if ((lonIx = NaPTANHelper.findColumn(line, "\"Longitude\"")) == -1)
 				throw new IOException("stopfile column Longitude not found");
 		} else
 			throw new IOException("stopfile is empty");
 
 		if (lat != null)
 			lat.clear();
 		if (lon != null)
 			lon.clear();
 		lat = new HashMap<String, String>();
 		lon = new HashMap<String, String>();
 		stopIx = new HashMap<String, Integer>();
 		String stopcode;
 		String tokens[] = {"", "", "", "", "", "", "", "", "", "",
 				"", "", "", "", "", "", "", "", "", "",
 				"", "", "", "", "", "", "", "", "", "",
 				"", "", "", "", "", "", "", "", "", "",
 				"", "", "", "", "", "", "", "", "", ""};
 		int i, j;
 		int lineCounter = 0;
 		while((line = bufFileIn.readLine()) != null) {
 			StringTokenizer st = new StringTokenizer(line, ",");
 			i = 0;
 			while (st.hasMoreTokens() && i < 30) {
 				tokens[i] = st.nextToken();
 				i++;
 			}
 			stopcode = tokens[stopcodeIx].substring(1, tokens[stopcodeIx].length() - 1); // Remove quotation marks
 			lat.put(stopcode, tokens[latIx]);
 			lon.put(stopcode, tokens[lonIx]);
 			stopIx.put(stopcode, (Integer)lineCounter);
 			lineCounter++;
 		}
 		bufFileIn.close();
 
 		// Read columns
 		if (stopColumns != null && stopColumns.size() > 0) {
 			bufFileIn = new BufferedReader(new FileReader(stopsFileName));
 			if ((line = bufFileIn.readLine()) != null) {
 				Iterator<String> iterator = stopColumns.iterator();
 				String column;
 				stopColumnIxs = new HashMap<String, Integer>();
 				while (iterator.hasNext()) {
 					column = iterator.next();
 					stopColumnIxs.put(column, (Integer)NaPTANHelper.findColumn(line, column));
 				}
 			} else
 				throw new IOException("stopfile is empty");
 			while((line = bufFileIn.readLine()) != null) {
 				Iterator<String> iterator = stopColumns.iterator();
 				i = 0;
 				String column;
 				while(iterator.hasNext() && i < 30) {
 					column = iterator.next();
 					StringTokenizer st = new StringTokenizer(line, ",");
 					String token;
 					j = 0;
 					while (st.hasMoreTokens()) {
 						token = st.nextToken();
 						if ((Integer)stopColumnIxs.get(column) == j) {
 							if (columnValues[i] == null)
 								columnValues[i] = new ArrayList<String>();
 							columnValues[i].add(token);
 						}
 						j++;
 					}
 					i++;
 				}
 			}
 			bufFileIn.close();
 		}
 	}
 }
