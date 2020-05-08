 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 package org.amanzi.neo.loader;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import net.refractions.udig.catalog.IGeoResource;
 import net.refractions.udig.project.ILayer;
 import net.refractions.udig.project.IMap;
 
 import org.amanzi.awe.views.network.view.NetworkTreeView;
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.database.services.UpdateDatabaseEvent;
 import org.amanzi.neo.core.database.services.UpdateDatabaseEventType;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.GisTypes;
 import org.amanzi.neo.core.enums.NetworkElementTypes;
 import org.amanzi.neo.core.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.core.enums.SplashRelationshipTypes;
 import org.amanzi.neo.core.utils.ActionUtil;
 import org.amanzi.neo.core.utils.ActionUtil.RunnableWithResult;
 import org.amanzi.neo.loader.internal.NeoLoaderPlugin;
 import org.amanzi.neo.preferences.DataLoadPreferences;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.EmbeddedNeo;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.ReturnableEvaluator;
 import org.neo4j.api.core.StopEvaluator;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.api.core.TraversalPosition;
 import org.neo4j.api.core.Traverser.Order;
 
 /**
  * This class was written to handle CSV (tab delimited) network data from ice.net in Sweden.
  * It has been written in a partially generic way so as to be possible to change to various
  * other data sources, but some things are hard coded, like the names of key columns and the 
  * assumption of RT90 projection for non-angular coordinates.
  * 
  * It also assumes the data is structured as BSC->Site->Sector in a tree layout.
  * 
  * @author craig
  */
 public class NetworkLoader extends AbstractLoader {
     /** String LOAD_NETWORK_TITLE field */
     private static final String LOAD_NETWORK_TITLE = "Load Network";
     private static final String LOAD_NETWORK_MSG = "This network is already loaded into the database.\nDo you wish to overwrite the data?";
 
     /**
 	 * This class handles the CRS specification.
 	 * Currently it is hard coded to return WGS84 (EPSG:4326) for data that looks like lat/long
      * and RT90 2.5 gon V (EPSG:3021) for data that looks like it is in meters and no hints are given.
      * If the user passes a hint, the following are considered:
      * 
 	 * @author craig
 	 */
 	public static class CRS {
         private String type = null;
         private String epsg = null;
         private CRS() {}
         public String getType() {return type;}
         public String toString() {return epsg;}
         public static CRS fromLocation(float lat, float lon, String hint) {
             CRS crs = new CRS();
             crs.type = "geographic";
             crs.epsg = "EPSG:4326";
             if ((lat > 90 || lat < -90) && (lon > 180 || lon < -180)) {
                 crs.type = "projected";
                 if (hint != null && hint.toLowerCase().startsWith("germany")) {
                     crs.epsg = "EPSG:31467";
                 } else {
                     crs.epsg = "EPSG:3021";
                 }
             }
             return crs;
         }
     }
 
     private static Pattern channelPattern = Pattern.compile("(^BCCH$)|(^TRX\\d+$)|(^TCH\\d+$)");
     //private Map<Integer, Integer> channalMap;
 	private String siteName = null;
     private String bscName = null;
     private String cityName = null;
 	private Node site = null;
     private HashMap<String,Node> bsc_s = new HashMap<String,Node>();
     private HashMap<String,Node> city_s = new HashMap<String,Node>();
     private Node bsc = null;
     private Node city = null;
     private Node network = null;
     private ArrayList<String> mainHeaders = new ArrayList<String>();
     public ArrayList<String> shortLines = new ArrayList<String>();
     public ArrayList<String> emptyFields = new ArrayList<String>();
     public ArrayList<String> badFields = new ArrayList<String>();
     public ArrayList<String> lineErrors = new ArrayList<String>();
     private long siteNumber = 0;
     private long sectorNumber = 0;
     private boolean trimSectorName = true;
     private NetworkHeader networkHeader = null;
 
     /**
      * Constructor for loading data in AWE, with specified display and dataset, but no NeoService
      * 
      * @param filename of file to load
      * @param display for opening message dialogs
      * @param dataset to add data to
      */
     public NetworkLoader(String filename, Display display) {
         initialize("Network", null, filename, display);
         initializeKnownHeaders();
     }
 
     /**
      * Constructor for loading data in test mode, with no display and NeoService passed
      * 
      * @param neo database to load data into
      * @param filename of file to load
      * @param display
      */
     public NetworkLoader(NeoService neo, String filename) {
         initialize("Network", neo, filename, null);
         initializeKnownHeaders();
     }
 
     /**
      * Build a map of internal header names to format specific names for types that need to be known
      * in the algorithms later.
      */
     private void initializeKnownHeaders() {
 //        addHeaderFilters(new String[] {"time.*", "events", ".*latitude.*", ".*longitude.*", ".*server_report.*",
 //                ".*state_machine.*", ".*layer_3_message.*", ".*handover_analyzer.*"});
 
         //Known headers that are not sector data properties
         addMainHeader("city", new String[] {"City", "Town", "Ort"});
         addMainHeader("msc", new String[] {"MSC", "MSC_NAME", "MSC Name"});
         addMainHeader("bsc", new String[] {"BSC", "BSC_NAME", "RNC", "BSC Name"});
        addMainHeader("site", new String[] {"Site", "Name", "Site Name"});
         addMainHeader("sector", new String[] {"Sector", "Cell", "BTS_Name", "CELL_NAME", "GSM Sector ID"});
         addMainHeader("latitude", new String[] {"lat.*", "y_wert.*", "northing"});
         addMainHeader("longitude", new String[] {"long.*", "x_wert.*", "easting"});
         
         //Stop statistics collection for properties we will not save to the sector
         addNonDataHeaders(mainHeaders);
 
         //force String types on some risky headers (sometimes these look like integers)
         useMapper("site", new StringMapper());
         useMapper("sector", new StringMapper());
 
         //Known headers that are sector data properties
         addKnownHeader("beamwidth", new String[] {".*beamwidth.*", "beam", "hbw"});
         addKnownHeader("azimuth", new String[] {".*azimuth.*"});
     }
     
     /**
      * Add a known header entry as well as mark it as a main header. All other fields will be
      * assumed to be sector properties.
      * 
      * @param key
      * @param regexes
      */
     private void addMainHeader(String key, String[] regexes) {
         addKnownHeader(key, regexes);
         mainHeaders.add(key);
     }
     
     public boolean setup() {
         try {
             trimSectorName = NeoLoaderPlugin.getDefault().getPreferenceStore().getBoolean(DataLoadPreferences.REMOVE_SITE_NAME);
         } catch (Exception e) {
         }
         if (findOrCreateNetworkNode() != null) {
             return null != findOrCreateGISNode(network, GisTypes.NETWORK.getHeader());
         } else {
             return false;
         }
     }
 	
     /**
      * After all lines have been parsed, this method is called, allowing the implementing class the
      * opportunity to save any cached information, or write any final statistics. It is not abstract
      * because it is possible, or even probable, to write an importer that does not need it.
      */
     protected void finishUp() {
         printWarnings(emptyFields, "empty fields", 0, lineNumber);
         printWarnings(badFields, "field parsing warnings", 10, lineNumber);
         printWarnings(shortLines, "missing fields", 10, lineNumber);
         printWarnings(lineErrors, "uncaught errors", 10, lineNumber);
         Transaction transaction = neo.beginTx();
         try {
             networkHeader.saveStatistic(network);
             network.setProperty("site_count", siteNumber);
             network.setProperty("sector_count", sectorNumber);
             network.setProperty("bsc_count", bsc_s.size());
             network.setProperty("city_count", city_s.size());
             transaction.success();
         } finally {
             transaction.finish();
         }
         try {
             // add network to project and gis node to catalog
             super.finishUpGis(network);
             if (!isTest()) {
                 showNetworkTree();
             }
         } catch (MalformedURLException e) {
             throw (RuntimeException)new RuntimeException().initCause(e);
         }
     }
 
     private void printWarnings(ArrayList<String> warnings, String warning_type, int limit, long lineNumber) {
         if(warnings.size()>0){
             info("Had " + warnings.size() + " "+warning_type+" warnings in " + lineNumber + " lines parsed");
             if (limit > 0) {
                 int i = 0;
                 for (String warning : warnings) {
                     info("\t" + warning);
                     if (i++ > limit) {
                         info("\t... and " + (warnings.size() - 10) + " more ...");
                         break;
                     }
                 }
             }
         }
     }
 
     private void showNetworkTree() {
         //TODO: See if we need this event
         NeoCorePlugin.getDefault().getUpdateDatabaseManager()
                 .fireUpdateDatabase(new UpdateDatabaseEvent(UpdateDatabaseEventType.GIS));
 
         // Lagutko, 21.07.2009, show NeworkTree
         ActionUtil.getInstance().runTask(new Runnable() {
             @Override
             public void run() {
                 try {
                     PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
                             NetworkTreeView.NETWORK_TREE_VIEW_ID);
                 }
                 catch (PartInitException e) {
                     NeoCorePlugin.error(null, e);
                 }
             }
         }, false);
     }
 
     /**
      *Returns layer, that contains necessary gis node
      * 
      * @param map map
      * @param gisNode gis node
      * @return layer or null
      */
     public static ILayer findLayerByNode(IMap map, Node gisNode) {
         try {
             for (ILayer layer : map.getMapLayers()) {
                 IGeoResource resource = layer.findGeoResource(Node.class);
                 if (resource != null && resource.resolve(Node.class, null).equals(gisNode)) {
                     return layer;
                 }
             }
             return null;
         } catch (IOException e) {
             // TODO Handle IOException
             e.printStackTrace();
             return null;
         }
     }
 
     private class NetworkHeader {
         private Map<String,String> mainKeys = new HashMap<String,String>();
         private String crsHint = null;
         private ArrayList<String> sectorData = new ArrayList<String>();
         private Map<String, Integer> channelMap = new HashMap<String, Integer>();
         Map<String,Object> lineData = null;
 
         private NetworkHeader(String[] fields) {
             lineData = makeDataMap(fields);
             for (String header : lineData.keySet()) {
                 String name = headerName(header);
                 if(mainHeaders.contains(header)) {
                     mainKeys.put(header, name);
                     if(name.toLowerCase().startsWith("wert",2) && (header.startsWith("lat") || header.startsWith("lon"))){
                         crsHint = "germany";
                     }
                 } else if (channelPattern.matcher(name).matches()) {
                     channelMap.put(header, 0);
                     sectorData.add(header);
                 } else {
                     sectorData.add(header);
                 }
             }
         }
 
         private void setData(String[] fields){
             lineData = makeDataMap(fields);
         }
 
         private void saveStatistic(Node vault) {
             Set<String> channelProperties = new HashSet<String>();
             for (String header : channelMap.keySet()) {
                 if (channelMap.get(header) > 0) {
                     channelProperties.add(header);
                 }
             }
             vault.setProperty(INeoConstants.PROPERTY_ALL_CHANNELS_NAME, channelProperties.toArray(new String[0]));
         }
 
         private String getString(String key) {
             Object value = lineData.get(key);
             if(value == null || value instanceof String) {
                 return (String)value;
             } else {
                 return value.toString();
             }
         }
 
         private Float getFloat(String key) {
             Object value = lineData.get(key);
             if(value instanceof Integer) {
                 return ((Integer)value).floatValue();
             } else {
                 return (Float)value;
             }
         }
 
         private Float getLat() {
             return getFloat("latitude");
         }
 
         private Float getLon() {
             return getFloat("longitude");
         }
 
         private Map<String,Object> getSectorData() {
             Map<String,Object> data = new LinkedHashMap<String,Object>();
             for(String key: sectorData) {
                 if(lineData.containsKey(key)) {
                     if(channelMap.containsKey(key)){
                         channelMap.put(key, channelMap.get(key) + 1);
                     }
                     data.put(key,lineData.get(key));
                 }
             }
             return data;
         }
 
         private String getCrsHint() {
             return crsHint;
         }
     }
 
 	protected void parseLine(String line) {
         debug(line);
         String fields[] = splitLine(line);
         if (fields.length < 3)
             return;
         if (this.isOverLimit())
             return;
         Transaction transaction = neo.beginTx();
         try {
             if (networkHeader == null) {
                 networkHeader = new NetworkHeader(fields);
             } else {
                 networkHeader.setData(fields);
             }
             String bscField = networkHeader.getString("bsc");
             String cityField = networkHeader.getString("city");
             String siteField = networkHeader.getString("site");
             String sectorField = networkHeader.getString("sector");
             if(sectorField==null) {
                 lineErrors.add("Missing sector name on line " + lineNumber);
                 return;
             }
            if(siteField==null) {
                //lineErrors.add("Missing site name on line " + lineNumber);
                //return;
                siteField = sectorField.substring(0, sectorField.length() - 1);
            }
             if (trimSectorName) {
                 sectorField = sectorField.replaceAll(siteField + "[\\:\\-]?", "");
             }
             if (cityField != null && !cityField.equals(cityName)) {
                 cityName = cityField;
                 city = city_s.get(cityField);
                 if (city == null) {
                     debug("New City: " + cityName);
                     city = addChild(network, NetworkElementTypes.CITY.toString(), cityName);
                     city_s.put(cityField, city);
                 }
             }
             if (bscField != null && !bscField.equals(bscName)) {
                 bscName = bscField;
                 bsc = bsc_s.get(bscField);
                 if (bsc == null) {
                     debug("New BSC: " + bscName);
                     bsc = addChild(city == null ? network : city, NetworkElementTypes.BSC.toString(), bscName);
                     bsc_s.put(bscField, bsc);
                 }
             }
             if (!siteField.equals(siteName)) {
                 siteName = siteField;
                 debug("New site: " + siteName);
                 Node siteRoot = bsc == null ? (city == null ? network : city) : bsc;
                 Node newSite = addChild(siteRoot, NetworkElementTypes.SITE.toString(), siteName);
                 (site == null ? network : site).createRelationshipTo(newSite, GeoNeoRelationshipTypes.NEXT);
                 site = newSite;
                 siteNumber++;
                 Float latitude = networkHeader.getLat();
                 Float longitude = networkHeader.getLon();
                 updateBBox(latitude, longitude);
                 checkCRS(latitude, longitude, networkHeader.getCrsHint());
                 site.setProperty(INeoConstants.PROPERTY_LAT_NAME, latitude);
                 site.setProperty(INeoConstants.PROPERTY_LON_NAME, longitude);
             }
             debug("New Sector: " + sectorField);
             Node sector = addChild(site, NetworkElementTypes.SECTOR.toString(), sectorField);
             // TODO: deprecated sectorNumber in favour of saved data
             sectorNumber++;
             // header.parseLine(sector, fields);
             Map<String, Object> sectorData = networkHeader.getSectorData();
             for (Map.Entry<String, Object> entry : sectorData.entrySet()) {
                 sector.setProperty(entry.getKey(), entry.getValue());
             }
             incSaved();
             transaction.success();
             // return true;
         } catch (Exception e) {
             lineErrors.add("Error parsing line " + lineNumber + ": " + e);
             error(lineErrors.get(lineErrors.size() - 1));
             if (lineErrors.size() == 1) {
                 e.printStackTrace(System.err);
             } else if (lineErrors.size() > 10) {
                 e.printStackTrace(System.err);
                 // return false;
             }
         } finally {
             transaction.finish();
         }
     }
 
     /**
      * This code finds the specified network node in the database, creating its own transaction for
      * that.
      * 
      * @param gis gis node
      */
 	private Node findOrCreateNetworkNode() {
         if (network == null) {
             Transaction tx = neo.beginTx();
             try {
                 for (Node node : neo.getReferenceNode().traverse(Order.BREADTH_FIRST, new StopEvaluator() {
 
                     @Override
                     public boolean isStopNode(TraversalPosition currentPos) {
                         return currentPos.depth() > 3;
                     }
                 }, new ReturnableEvaluator() {
 
                     @Override
                     public boolean isReturnableNode(TraversalPosition currentPos) {
                         return currentPos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME, "").equals(
                                 NetworkElementTypes.NETWORK.toString());
                     }
                 }, SplashRelationshipTypes.AWE_PROJECT, Direction.OUTGOING, NetworkRelationshipTypes.CHILD, Direction.OUTGOING, GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING)) {
                     debug("Testing possible Network node " + node + ": " + node.getProperty("name", "").toString());
                     if (node.getProperty(INeoConstants.PROPERTY_NAME_NAME, "").equals(basename)) {
                         debug("Found matching Network node " + node + ": " + node.getProperty("name", "").toString());
                         try {
                             // remove all incoming relationships
                             for (Relationship relationshipIn : node.getRelationships(Direction.INCOMING)) {
                                 relationshipIn.delete();
                             }
                             if(isTest()) throw new Exception("Test mode");
                             PlatformUI.getWorkbench();
                             if (!askIfOverwrite())
                                 return null;
                             NeoCorePlugin.getDefault().getProjectService().deleteNode(node);
                         } catch (Exception e) {
                             // we are in test mode, automatically agree to overwrite network
                             deleteTree(node);
                             deleteNode(node);
                         }
                         break;
                     }
                 }
                 network = neo.createNode();
                 network.setProperty(INeoConstants.PROPERTY_TYPE_NAME, NetworkElementTypes.NETWORK.toString());
                 network.setProperty(INeoConstants.PROPERTY_NAME_NAME, basename);
                 network.setProperty(INeoConstants.PROPERTY_FILENAME_NAME, filename);
                 tx.success();
             } catch (Exception e) {
                 e.printStackTrace();
             } finally {
                 tx.finish();
             }
         }
         return network;
     }
 
     private static boolean askIfOverwrite() {
         int resultMsg = (Integer)ActionUtil.getInstance().runTaskWithResult(new RunnableWithResult() {
             int result;
             @Override
             public void run() {
                 MessageBox msg = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.YES
                         | SWT.NO);
                 msg.setText(LOAD_NETWORK_TITLE);
                 msg.setMessage(LOAD_NETWORK_MSG);
                 result = msg.open();
             }
 
             @Override
             public Object getValue() {
                 return new Integer(result);
             }
         });
         return resultMsg == SWT.YES;
     }
 
 	/**
 	 * This code expects you to create a transaction around it, so don't forget to do that.
 	 * @param parent
 	 * @param type
 	 * @param name
 	 * @return
 	 */
 	private Node addChild(Node parent, String type, String name) {
 		Node child = null;
 		child = neo.createNode();
 		child.setProperty(INeoConstants.PROPERTY_TYPE_NAME, type);
 		child.setProperty(INeoConstants.PROPERTY_NAME_NAME, name);
 		if (parent != null) {
 			parent.createRelationshipTo(child, NetworkRelationshipTypes.CHILD);
 			debug("Added '" + name + "' as child of '" + parent.getProperty(INeoConstants.PROPERTY_NAME_NAME));
 		}
 		return child;
 	}
 	
 	public void printStats(boolean verbose) {
         if (network != null) {
             if (verbose) {
                 Transaction tx = neo.beginTx();
                 try {
                     printChildren(network, 0);
                     tx.success();
                 } finally {
                     tx.finish();
                 }
             }
             info("Finished loading "+siteNumber+" sites and "+sectorNumber+" sectors from "+lineNumber+" lines");
         } else {
             error("No network node found");
         }
     }
 
 	private void printChildren(Node node, int depth) {
 		if(node==null || depth > 4 || !node.hasProperty(INeoConstants.PROPERTY_NAME_NAME)) return;
 		StringBuffer tab = new StringBuffer();
 		for(int i=0;i<depth;i++) tab.append("    ");
 		StringBuffer properties = new StringBuffer();
 		for(String property:node.getPropertyKeys()) {
 			if(!property.equals(INeoConstants.PROPERTY_NAME_NAME)) properties.append(" - ").append(property).append(" => ").append(node.getProperty(property));
 		}
 		info(tab.toString()+node.getProperty(INeoConstants.PROPERTY_NAME_NAME)+properties);
 		for(Relationship relationship:node.getRelationships(NetworkRelationshipTypes.CHILD,Direction.OUTGOING)){
 			//debug(tab.toString()+"("+relationship.toString()+") - "+relationship.getStartNode().getProperty("name")+" -("+relationship.getType()+")-> "+relationship.getEndNode().getProperty("name"));
 			printChildren(relationship.getEndNode(),depth+1);
 		}
 	}
 
 	/**
 	 * A main method for useful quick-turn-around testing and debugging of data parsing on various sample files.
 	 * @param args
 	 */
 	public static void main(String[] args) {
 	    //NeoLoaderPlugin.debug = true;
         if (args.length < 1)
             args = new String[] {"amanzi/network.txt"};
 		EmbeddedNeo neo = new EmbeddedNeo("../../testing/neo");
 		try{
             for (String filename : args) {
                 long startTime = System.currentTimeMillis();
                 NetworkLoader networkLoader = new NetworkLoader(neo, filename);
                 networkLoader.setup();
                 networkLoader.setLimit(1000);
                 networkLoader.setCommitSize(1000);
                 networkLoader.run(null);
                 networkLoader.printStats(true);
                 networkLoader.info("Ran test in " + (System.currentTimeMillis() - startTime) / 1000.0 + "s");
             }
 		} catch (IOException e) {
 			System.err.println("Failed to load network: "+e);
 			e.printStackTrace(System.err);
 		}finally{
 			neo.shutdown();
 		}
 	}
 }
