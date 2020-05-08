 /**
  * University of Illinois/NCSA
  * Open Source License
  *
  * Copyright (c) 2008, Board of Trustees-University of Illinois.
  * All rights reserved.
  *
  * Developed by:
  *
  * Automated Learning Group
  * National Center for Supercomputing Applications
  * http://www.seasr.org
  *
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to
  * deal with the Software without restriction, including without limitation the
  * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
  * sell copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  *  * Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimers.
  *
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimers in the
  *    documentation and/or other materials provided with the distribution.
  *
  *  * Neither the names of Automated Learning Group, The National Center for
  *    Supercomputing Applications, or University of Illinois, nor the names of
  *    its contributors may be used to endorse or promote products derived from
  *    this Software without specific prior written permission.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
  * CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
  * WITH THE SOFTWARE.
  */
 
 package org.seasr.meandre.components.tools.geo;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.meandre.annotations.Component;
 import org.meandre.annotations.ComponentInput;
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.annotations.Component.FiringPolicy;
 import org.meandre.annotations.Component.Licenses;
 import org.meandre.annotations.Component.Mode;
 import org.meandre.components.abstracts.AbstractExecutableComponent;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextProperties;
 import org.seasr.datatypes.BasicDataTypesTools;
 import org.seasr.datatypes.BasicDataTypes.Strings;
 import org.seasr.datatypes.BasicDataTypes.StringsArray;
 import org.seasr.meandre.components.tools.Names;
 
 
 import org.seasr.meandre.support.components.tuples.SimpleTuple;
 import org.seasr.meandre.support.components.tuples.SimpleTuplePeer;
 
 import org.seasr.meandre.support.components.geographic.GeoLocation;
 
 
 /**
  *
  * @author Mike Haberman;
  * ABQIAAAADV1H5JfZH41B6yxB1yGVFhQmgYtTImkVBc-VhblDgOLOdwhVaBSPwcEgsBl7atDdDJjnfl51p9fU5A
  * 
  * Yahoo problems:  location=New England
  * 
  */
 
 @Component(
 		name = "GeoLocation Cleaner",
 		creator = "Mike Haberman",
 		baseURL = "meandre://seasr.org/components/foundry/",
 		firingPolicy = FiringPolicy.all,
 		mode = Mode.compute,
 		rights = Licenses.UofINCSA,
 		tags = "tuple, group",
 		description = "This component uses the Yahoo GeoService to attempt to fully qualify " +
 		              "location entities within a single sentence." ,
 		dependency = {"trove-2.0.3.jar","protobuf-java-2.2.0.jar"}
 )
 public class GeoLocationCleaner extends AbstractExecutableComponent {
 
     //------------------------------ INPUTS ------------------------------------------------------
 
     @ComponentInput(
             name = Names.PORT_TUPLES,
             description = "set of labelled tuples to be grouped (e.g. startTokenPosition, token, concept)" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
     )
     protected static final String IN_TUPLES = Names.PORT_TUPLES;
 
     @ComponentInput(
             name = Names.PORT_META_TUPLE,
             description = "meta data for tuples to be labeled" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String IN_META_TUPLE = Names.PORT_META_TUPLE;
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             name = Names.PORT_TUPLES,
             description = "set of grouped tuples" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.StringsArray"
     )
     protected static final String OUT_TUPLES = Names.PORT_TUPLES;
 
     @ComponentOutput(
             name = Names.PORT_META_TUPLE,
             description = "meta data for the tuples (windowId, begin, end, concept, count, frequency)" +
                 "<br>TYPE: org.seasr.datatypes.BasicDataTypes.Strings"
     )
     protected static final String OUT_META_TUPLE = Names.PORT_META_TUPLE;
 
 	//----------------------------- PROPERTIES ---------------------------------------------------
 
     @ComponentProperty(
             description = "field name for the n.e. type",
             name = "key",
             defaultValue = "type"
     )
     protected static final String DATA_PROPERTY_KEY_FIELD = "key";
 
     @ComponentProperty(
             description = "field name for the value to use for windowing, must be numeric",
             name = "windowField",
             defaultValue = "sentenceId"
     )
     protected static final String DATA_PROPERTY_WINDOW_FIELD = "windowField";
 
     /* NOT USED
     @ComponentProperty(
             description = "window size, -1 means use dynamic value based on maxWindows",
             name = "windowSize",
             defaultValue = "-1"
     )
     protected static final String DATA_PROPERTY_WINDOW_SIZE = "windowSize";
 
     @ComponentProperty(
             description = "max. number of windows, -1 means use dyanamic value based on windowSize",
             name = "maxWindows",
             defaultValue = "-1"
     )
     protected static final String DATA_PROPERTY_MAX_WINDOWS = "maxWindows";
     */
 
 	//--------------------------------------------------------------------------------------------
 
     // fields for accessing the incoming tuples
     private String keyField     = "type";
     private String valueField   = "text";
     private String windowField  = "sentenceId";
     
     
     // fields added to the outgoing tuples
     private String locationField = "fqLocation";
     private String latField      = "lat";
     private String longField     = "long";
     private String[] newFields   = {locationField, latField, longField};
 
     long windowSize = 1;  
     // TODO,(perhaps) make this a property
     // number of sentences to consume before labeling them
     // a better algorithm, might be to use a sliding window cache of sentences
     // so sentence N gets resolved after processing sentence N-1 and N+1
     //
 
    //--------------------------------------------------------------------------------------------
 
     private Map<String, GeoLocation> globalCache = new HashMap<String,GeoLocation>();
     
 	@Override
     public void initializeCallBack(ComponentContextProperties ccp) throws Exception {
 
 		this.keyField    = ccp.getProperty(DATA_PROPERTY_KEY_FIELD);
 		this.windowField = ccp.getProperty(DATA_PROPERTY_WINDOW_FIELD);
 	}
 
 	@Override
     public void executeCallBack(ComponentContext cc) throws Exception {
 		Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
 		SimpleTuplePeer inPeer  = new SimpleTuplePeer(inputMeta);
 		SimpleTuplePeer outPeer = new SimpleTuplePeer(inPeer, newFields);
 
 		StringsArray input = (StringsArray) cc.getDataComponentFromInput(IN_TUPLES);
 		Strings[] in = BasicDataTypesTools.stringsArrayToJavaArray(input);
 
 
 		SimpleTuple tuple    = inPeer.createTuple();
 		SimpleTuple outTuple = outPeer.createTuple();
 
 		int KEY_IDX    = inPeer.getIndexForFieldName(keyField);
 		int VALUE_IDX  = inPeer.getIndexForFieldName(valueField);
 		int START_IDX  = inPeer.getIndexForFieldName(windowField);
 		
 		// cache the outgoing fields
 		int FQLOC_IDX  = outPeer.getIndexForFieldName(locationField);
 		int LAT_IDX    = outPeer.getIndexForFieldName(latField);
 		int LONG_IDX   = outPeer.getIndexForFieldName(longField);
 
 
 		if (KEY_IDX == -1){
 			console.info(inPeer.toString());
 			throw new RuntimeException("tuple has no key field " + keyField);
 		}
 		if (START_IDX == -1){
 			throw new RuntimeException("tuple has no window field " + windowField);
 		}
 
 		List<Strings> output = new ArrayList<Strings>();
 		long lastPosition = 0;
 		
 		
 		
 		List<String> locations = new ArrayList<String>();
 		
 		
 		int lastIdx  = 0;
 		for (int i = 0; i < in.length; i++) {
 
 			tuple.setValues(in[i]);
 			String key   = tuple.getValue(KEY_IDX);
 			String value = tuple.getValue(VALUE_IDX);
 			long currentPosition = Long.parseLong(tuple.getValue(START_IDX));
 			
 			if (! key.equals("location")) {
 				continue;
 			}
 			
 			if (value == null) {
 				console.info("warning, null location");
 				continue;
 			}
 			
 			
 			if (currentPosition - lastPosition >= windowSize || (i + 1 == in.length)) {
 				
 				int end = i;
 				// if this is it, add the last location to our set
 				if (i + 1 == in.length) {
 					end = i + 1;
 					locations.add(value);
 				}
 				
 				
 				
 				//
 				// resolve locations in the window
 				//
 				console.fine(lastPosition +"," + currentPosition + " locations to process: " + locations.size());
 				console.fine(lastIdx + " to " + end);
 				
 				
 				Map<String,GeoLocation> map = resolve(locations);
 				
 				// now relabel each location
 				for (int j = lastIdx; j < end; j++) {
 					// relabel
 
 					tuple.setValues(in[j]);
 					key = tuple.getValue(VALUE_IDX);
 					GeoLocation geo = map.get(key);
 					if (geo == null) {
 						// check the global cache
 						geo = globalCache.get(key);
 					}
 
 					outTuple.setValue(tuple); // copy original values
 					if (geo != null) {
 						// ("MAP " + key + " : " + geo.toString());
 						outTuple.setValue(FQLOC_IDX, geo.getQueryLocation());
 						outTuple.setValue(LAT_IDX,   geo.getLatitude());
 						outTuple.setValue(LONG_IDX,  geo.getLongitude());
 
 						globalCache.put(key, geo);
 					}
 					else {
                         // console.fine("NO MAP for key " + key);
 						outTuple.setValue(FQLOC_IDX, "N.A");
 						outTuple.setValue(LAT_IDX,   "-1");
 						outTuple.setValue(LONG_IDX,  "-1");
 					}
 
 
 					output.add(outTuple.convert());
 
 
 				}
 
 				
 				
 				lastPosition = currentPosition;
 				lastIdx = i;
 				locations.clear();
 				
 				
 			}
 			
             // add the location
 			locations.add(value);
 
 
 		}
 
 		//
 		// push the whole collection, protocol safe
 		//
 		Strings[] results = new Strings[output.size()];
 		output.toArray(results);
 		StringsArray outputSafe = BasicDataTypesTools.javaArrayToStringsArray(results);
 		cc.pushDataComponentToOutput(OUT_TUPLES, outputSafe);
 
 	    //
 		// metaData for this tuple producer
 		//
 	    cc.pushDataComponentToOutput(OUT_META_TUPLE, outPeer.convert());
 	}
 
     @Override
     public void disposeCallBack(ComponentContextProperties ccp) throws Exception {
 
     }
     
    
     //
     // This algorithm is based mostly on empirical data and heuristics
     // it is mostly used for resolving city and state information
     // if address or zip code resolution were needed, this will not work
     // it is also tuned to the Yahoo GeoCaching service
     // This algorithm MUST be re-written if another (more accurate) ? service
     // is used.
     // I am NOT proud of this code :)  M.E.H
     //
     public Map<String,GeoLocation> resolve(List<String> locations)
     {
    	console.fine("request to resolve " + locations.size());
     	
     	Map<String,GeoLocation> out = new HashMap<String,GeoLocation>();
     	
     	// reduce:  A,B ==> AB
     	// if A + B ==> resolve to ONE location, remove B
     	List<String> more = new ArrayList<String>();
     	int size = locations.size();
     	for (int i = 0; i < size;) {
     		
     		String a = locations.get(i++);
     		
     		if (i < size) {
     			String b = locations.get(i++);
     			
     			if (a.equals(b)) {
     				// if both are the same, just skip 
     				i--;
     				continue;
     			}
     			
     			//
     			// Hack for special case: states
     			//
     			// Pennsylvania, New York  (a street in New York, or 2 states)
     			// Richmond, Virginia  ==> both return unique
     			// Richmond     --> returns "zip"
     			// Virginia     --> returns "state"
     			// New York     --> returns "zip"   precision
     			// Pennsylvania --> returns "state" precision
     			
     			
     			List<GeoLocation> geosA = getLocations(GeoLocation.P_STATE, a);
     			List<GeoLocation> geosB = getLocations(GeoLocation.P_STATE, b);
     			// even though we requested state precision, the result may not be
     			
     			
    			console.fine("A " + a + " " + geosA.size());
    			console.fine("B " + b + " " + geosB.size());
     			
     			GeoLocation geoA = null;
 				GeoLocation geoB = null;
 				if (geosA.size() == 1) {
 					geoA = geosA.get(0);
 				}
 				if (geosB.size() == 1) {
 					geoB = geosB.get(0);
 				}
 				
     			if (geoA != null && geoB != null) {
     				
     				//
     				// Virginia, Kentucky ==> is a city in KY 
     				// assume if both are states, that wins
     				if (geoA.isState() && geoB.isState()) {
     					out.put(a, geoA);
     					out.put(b, geoB);
     					
     					continue;
     				}
     				
     				// check if one is within the other
     				// e.g. Richmond, VA   AND Virgina
     				if (geoA.isFoundWithin(geoB)) {
     					
     					// e.g  Richmond, Virgina
     					String c = a + "," + b;
     					console.fine("A is in B " +  c);
     					geoA.setQueryLocation(c);
     					
     					out.put(a, geoA);
     					out.put(b, geoA); 
     					
     					continue;
     					
     				}
     				else if (geoB.isFoundWithin(geoA)) {
     					
     					// Virgina, Richmond
     					String c = b + "," + a;
     					console.fine("B is in A " +  c);
     					geoB.setQueryLocation(c);
     					
     					
     					out.put(a, geoB); // or NULL ?
     					out.put(b, geoB);
     			
     					continue;
     				}
     				else if (geoA.isState()) {
     					//
     					// I moved from Virginia to Burks County
     					//
     					out.put(a, geoA);
     					i--; // re-process B
     					continue;
     				}
     				else {
     					
     					// we will tackle this case below
     					
     				}
     				
     			}
     			else if (geosA.size() > 1 && geosB.size() > 1) {
     				//
     				// try to find a commonn state
     				// while in Bloomington, I came across someone from Springfield
     				//
     			}
     			
     			//
     			// try the two combined into a single location
     			//
 
     			
     			//
     			// 
     			//
     			
     			String c = a + "," + b;
     			List<GeoLocation> geos = getLocations(c);
     			if (geos.size() == 1) {
     
     				GeoLocation geo = geos.get(0);
     				// Bloomington,Springfield ==> returns "address" precision
     				// Bolivia, Illinois ==> returns "zip" precision
     				if (geo.getPrecision() > GeoLocation.P_ZIP) {
     					// not what we want
    					more.add(a); i--; // reprocess the b
     					continue;
     				}
     				
     			    if (geoB != null) {
     			    	
     			    	//
     	    			// Springfield ==> lots
     	    			// Washington  ==> DC (zip)
     	    			// Springfield, Washington == > KY (zip)
     			    	//
     			    	
     			    	String stateB = geoB.getState();
     			    	String stateC = geo.getState();
     			    	
    			    	console.fine("b " + stateB + " " + geoB.toString());
    			    	console.fine("c " + stateC + " " + geo.toString());
     			    	
     			    	if (stateB.equals(stateC)) {
     			    		out.put(a, geo);
     	    				out.put(b, geo);
     			    	}
     			    	
     			    } else { // geoB == null (i.e. geoB.size() > 0)
     			    	
     			    	// we got a unique location  (geo != null)
     			    	
     			    	// but it may NOT be because of  a,b
     			    	// Wisconsin, New Salam  --> WI, (state)
     			    	// Bolivia, Springfield  --> Boliva (country)
     			    	// (google returns an address in springfield, yahoo, just the country)
     			    	if (geoA != null && geo.getState().equals(geoA.getState())) {
     			    		
     			    		console.fine("adding each as separate geos " + a + " " + b);
     			    		out.put(a, geoA);
     			    		out.put(b, geoB); // could be null
     			    	}
     			    	else {
     			    		console.fine("adding combo " + a + " " + b);
     			    		out.put(a, geo);
     	    				out.put(b, geo);
     			    		
     			    	}
     			    	
     			    }
     			    
     			   
     			}
     			else {  
     				// geos.size() != 1
     				more.add(a);
     				i--; // reprocess the b
     			}
     		}
     		else {
     			more.add(a);
     		}
 
     		
     	}  // end of loop over locations
     	
     	locations.clear();
     	locations.addAll(more);
     	
     	
     	
     	
     	//
     	// handle strange cases here
     	//
     	
     	// Illinois, Springfield ==> 
     	// Springfield --> many, one has IL as state
     	// Illinois    --> the state
     	
     	// ex, Frederick ---> a bunch, no virgina
     	//     Virginia --> state
     	//     Frederick, Virigina ==> YES
     	
     	//
     	// Illinois,Paris  ==> Paris France and IL (State)
     	// want a city
     	//
     	
     	List<GeoLocation> keep = new ArrayList<GeoLocation>();
     	List<String> toResolve = new ArrayList<String>();
     	
     	//
     	// remove those that resolve to a single location
     	//
     	
     	console.fine("leftovers " + locations.size());
     	for (String loc : locations) {
     		
     		List<GeoLocation> geos = getLocations(loc);
     		console.fine(loc + " " + geos.size());
     		if (geos.size() == 1) {
     			keep.addAll(geos);
     			out.put(loc, geos.get(0));
     		}
     		else if (geos.size() > 1) {
     			toResolve.add(loc);
     		}
     	}
     	if (toResolve.size() == 0) {
     		return out;
     	}
     	
     	
     	
     	for (String loc : toResolve) {
     		List<GeoLocation> geos = getLocations(loc);
     		
     		// see if any of the geos are found within any of the keepers
     		GeoLocation a = resolve(keep, geos);
     		if (a != null) {
     			console.fine("found keeper " + loc);
     			out.put(loc, a);
     		}
     		else { // a == null
     			
     			// try adding loc to each value in keep until you get a unique value
     			for (GeoLocation geo: keep) {
     				if (geo.isState()) {
     					String newLocation = loc + "," + geo.getState();
     					List<GeoLocation> vals = getLocations(newLocation);
     					console.fine("Dx " + newLocation + ":" + vals.size());
     					if (vals.size() == 1) {
     						a = vals.get(0);
     						console.fine("YES " + a);
     						// WOW we found it
     						out.put(loc, a);
     					}
     				}
     				else {
     					console.fine("not state " + geo);
     				}
 
     			}
 
     		}
     	}	
     	
     	return out;
     }
     
    
     public GeoLocation resolve(List<GeoLocation> keepList, List<GeoLocation> geos)
     {
     	for (GeoLocation tmp: geos) {       // e.g. Springfield
     		for (GeoLocation k: keepList) { // e.g. Illinois
     			if (tmp.isFoundWithin(k)) {
     				
     				String q = tmp.getQueryLocation() + "," + k.getQueryLocation();
     				System.out.println("check " + q);
     				List<GeoLocation> vals = getLocations(q);
     				if (vals.size() > 0) {
     					return vals.get(0);
     				}
     			}
     		}
     	}
     	return null;
     }
   
  
     
     Map<String, List<GeoLocation>> cache = new HashMap<String, List<GeoLocation>>();
     
     public List<GeoLocation> getLocations(String location) 
     {
     	return getLocations(GeoLocation.P_LOCATION, location);
     }
     
     public List<GeoLocation> getLocations(int type, String location) 
     {
     	//
     	// TODO: cache the results
     	// apply remaps here Ill. --> Illinois
     	//
     	String key = location + "." + type;
     	
     	List<GeoLocation> entry = cache.get(key);
     	
     	if (entry == null) {
     		
     		try {
             	entry = GeoLocation.getAllLocations(type, location);
             	cache.put(key, entry);
             	return entry;
         	}
         	catch (Exception e) {
         		console.fine("WARNING ERROR " + e.toString());
         		return new ArrayList<GeoLocation>();
         	}
     		
     	}
     	return entry;
     	
     	
     }
 }
 
 /*
 Strings inputMeta = (Strings) cc.getDataComponentFromInput(IN_META_TUPLE);
 String[] meta = DataTypeParser.parseAsString(inputMeta);
 String fields = meta[0];
 DynamicTuplePeer inPeer = new DynamicTuplePeer(fields);
 
 Strings input = (Strings) cc.getDataComponentFromInput(IN_TUPLES);
 String[] tuples = DataTypeParser.parseAsString(input);
 */
