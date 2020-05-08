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
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.core.database.services.UpdateDatabaseEvent;
 import org.amanzi.neo.core.database.services.UpdateDatabaseEventType;
 import org.amanzi.neo.core.enums.GeoNeoRelationshipTypes;
 import org.amanzi.neo.core.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.core.service.NeoServiceProvider;
 import org.amanzi.neo.core.utils.CSVParser;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.amanzi.neo.core.utils.Pair;
 import org.amanzi.neo.loader.internal.NeoLoaderPlugin;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.PropertyContainer;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.ReturnableEvaluator;
 import org.neo4j.api.core.StopEvaluator;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.api.core.TraversalPosition;
 import org.neo4j.api.core.Traverser.Order;
 import org.neo4j.util.index.LuceneIndexService;
 
 /**
  * TODO extend from NeighbourLoader
  * <p>
  * Transmission data loader
  * </p>
  * 
  * @author Cinkel_A
  * @since 1.0.0
  */
 public class TransmissionLoader {
     /** String ID3 field */
     private static final String ID3 = "ITEM_Name";
     /** String ID2 field */
     private static final String ID1 = "Site ID";
     /** String ID1 field */
     private static final String ID2 = "Site No";
     /** String STRING field */
     // private static String directory = null;
     private static final int COMMIT_MAX = 1000;
     private Node network;
     private String fileName;
     private Header header;
     private Node neighbour;
     private String baseName;
     private final NeoService neo;
 
     /**
      * Constructor
      * 
      * @param networkNode network Node
      * @param fileName Neighbour file Name
      */
     public TransmissionLoader(Node networkNode, String fileName, NeoService neo) {
         network = networkNode;
         this.fileName = fileName;
         this.neo = neo;
         this.baseName = new File(fileName).getName();
     }
 
     /**
      * gets directory
      * 
      * @return
      */
     public static String getDirectory() {
         return NeoLoaderPlugin.getDefault().getPluginPreferences().getString(AbstractLoader.DEFAULT_DIRRECTORY_LOADER);
     }
 
     /**
      * Sets Default Directory path for file dialogs in TEMSLoad and NetworkLoad
      * 
      * @param newDirectory new default directory
      * @author Lagutko_N
      */
 
     public static void setDirectory(String newDirectory) {
         NeoLoaderPlugin.getDefault().getPluginPreferences().setValue(AbstractLoader.DEFAULT_DIRRECTORY_LOADER, newDirectory);
     }
 
     /**
      * Runs NeighbourLoader
      * 
      * @param monitor monitor
      * @throws IOException
      */
     public void run(IProgressMonitor monitor) throws IOException {
         CountingFileInputStream stream = null;
         BufferedReader reader = null;
         Transaction tx = neo.beginTx();
         try {
             if (monitor == null) {
                 monitor = new NullProgressMonitor();
             }
             monitor.beginTask("Importing " + baseName, 100);
             stream = new CountingFileInputStream(new File(fileName));
             String charSet = NeoLoaderPlugin.getDefault().getCharacterSet();
             reader = new BufferedReader(new InputStreamReader(stream, charSet));
             int perc = stream.percentage();
             int prevPerc = 0;
             String line = reader.readLine();
             if (line == null) {
                 monitor.setCanceled(true);
                 return;
             }
             header = new Header(line, neo);
             neighbour = getTransmission(network, baseName);
             header.createSectorCache(network);
             int commit = 0;
             while ((line = reader.readLine()) != null) {
                 header.parseLine(line, network, baseName);
                 if (monitor.isCanceled())
                     break;
                 perc = stream.percentage();
                 if (perc > prevPerc) {
                     monitor.worked(perc - prevPerc);
                     prevPerc = perc;
                 }
                 if (++commit > COMMIT_MAX) {
                     tx.success();
                     tx.finish();
                     tx = neo.beginTx();
                     commit = 0;
                 }
             }
             header.saveStatistic(neighbour);
             monitor.done();
             tx.success();
         } finally {
             if (reader != null) {
                 reader.close();
             }
             NeoCorePlugin.getDefault().getUpdateDatabaseManager().fireUpdateDatabase(
                     new UpdateDatabaseEvent(UpdateDatabaseEventType.TRANSMISSION));
             tx.finish();
             header.finish();
             NeoServiceProvider.getProvider().commit();
         }
 
     }
 
     /**
      * get transmission
      * 
      * @param network network node
      * @param fileName neighbour file name
      * @return neighbour node
      */
     private Node getTransmission(Node network, String fileName) {
         Node result = NeoUtils.findTransmission(network, fileName, neo);
         if (result != null) {
             return result;
         }
         Transaction tx = neo.beginTx();
         try {
             result = neo.createNode();
             result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, INeoConstants.TRANSMISSION_TYPE_NAME);
             result.setProperty(INeoConstants.PROPERTY_NAME_NAME, fileName);
             network.createRelationshipTo(result, NetworkRelationshipTypes.TRANSMISSION_DATA);
             tx.success();
             return result;
         } finally {
             tx.finish();
         }
     }
 
     /**
      * <p>
      * Header of Neighbour file
      * </p>
      * 
      * @author Cinkel_A
      * @since 1.0.0
      */
     public class Header {
 
         private static final String STRING = "STRING";
         /** String DOUBLE field */
         private static final String DOUBLE = "DOUBLE";
         /** String INTEGER field */
         private static final String INTEGER = "INTEGER";
         private static final int CACH_SIZE = 10000;
         private static final String KEY_ID = "name";
         private static final String KEY_ID2 = "name2";
         private Map<Integer, Pair<String, String>> indexMap = new LinkedHashMap<Integer, Pair<String, String>>();
         private NodeName serverNodeName;
         private NodeName neighbourNodeName;
         private String[] headers;
         // private Map<String, Pair<Node, Integer>> cach = new HashMap<String, Pair<Node,
         // Integer>>();
         private LuceneIndexService index;
         private final NeoService neo;
         private char fieldSepRegex;
         private char[] possibleFieldSepRegexes = new char[] {'\t', ',', ';'};
         private CSVParser parser;
         private Node lastNode = null;
 
         /**
          * Constructor
          * 
          * @param line - header line
          */
         public Header(String line, NeoService neo) {
             this.neo = neo;
 
             determineFieldSepRegex(line);
             headers = splitLine(line);
             serverNodeName = new NodeName(new String[] {ID1, "Near end Name"}, new String[] {ID2, "Near End Site No"},
                     new String[] {ID3});
             neighbourNodeName = new NodeName(new String[] {ID1, "Far end Name"}, new String[] {ID2, "Far End Site No"},
                     new String[] {ID3});
             for (int i = 0; i < headers.length; i++) {
                 String fieldHeader = headers[i];
                 if (serverNodeName.setFieldIndex(fieldHeader, i)) {
                     continue;
                 } else if (neighbourNodeName.setFieldIndex(fieldHeader, i)) {
                     continue;
                 } else {
                     indexMap.put(i, new Pair<String, String>(fieldHeader, null));
                 }
             }
         }
 
         protected String[] splitLine(String line) {
             return parser.parse(line).toArray(new String[0]);
         }
 
         private void determineFieldSepRegex(String line) {
             int maxMatch = 0;
             for (char regex : possibleFieldSepRegexes) {
                 String[] fields = line.split(String.valueOf(regex));
                 if (fields.length > maxMatch) {
                     maxMatch = fields.length;
                     fieldSepRegex = regex;
                 }
             }
             parser = new CSVParser(fieldSepRegex);
         }
 
         /**
          * save statistic information in node
          * 
          * @param neighbour
          */
         public void saveStatistic(Node neighbour) {
             saveNumericList(neighbour);
             saveAllFields(neighbour);
         }
 
         /**
          * finish work with header
          */
         public void finish() {
             if (index != null) {
                 index.shutdown();
             }
         }
 
         /**
          * create cache
          * 
          * @param network - network node
          */
         public void createSectorCache(Node network) {
 
             Transaction tx = neo.beginTx();
             try {
                 index = new LuceneIndexService(neo);
                 index.enableCache(KEY_ID, CACH_SIZE);
                 // it useful if neighbour file much bigger then network
                 indexesAllSectors(network, index);
                 tx.success();
             } finally {
                 // if tx.finish() - finds work slow but memory do not using
                 tx.finish();
             }
         }
 
         /**
          * Indexes all sectors
          * 
          * @param network network node
          * @param index LuceneIndexService
          */
         private void indexesAllSectors(Node network, LuceneIndexService index) {
             long t1 = System.currentTimeMillis();
             Iterator<Node> iterator = network.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     return currentPos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME, "").equals("site");
                 }
             }, GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING).iterator();
             while (iterator.hasNext()) {
                 Node node = (Node)iterator.next();
                 String id1 = NodeName.getId1(node);
                 if (id1 != null) {
                     index.index(node, KEY_ID, id1);
                 }
                 String id2 = NodeName.getId2(node);
                 if (id2 != null) {
                     index.index(node, KEY_ID2, id2);
                 }
             }
             System.out.println("INDEXES=\t" + (System.currentTimeMillis() - t1));
         }
 
         /**
          * Save list of Numeric properties in database
          * 
          * @param neighbour neighbour node
          */
         public void saveNumericList(Node neighbour) {
             Transaction tx = neo.beginTx();
             try {
                 Set<String> propertyes = new HashSet<String>();
                 for (Pair<String, String> pair : indexMap.values()) {
                     String clas = pair.getRight();
                     if (INTEGER.equals(clas) || DOUBLE.equals(clas)) {
                         propertyes.add(pair.getLeft());
 
                     }
                 }
                 neighbour.setProperty(INeoConstants.LIST_NUMERIC_PROPERTIES, propertyes.toArray(new String[0]));
                 tx.success();
             } finally {
                 tx.finish();
             }
         }
 
         /**
          * Save list of All properties in database
          * 
          * @param neighbour neighbour node
          */
         public void saveAllFields(Node neighbour) {
             Transaction tx = neo.beginTx();
 
             try {
                 Set<String> integer = new HashSet<String>();
                 Set<String> doubl = new HashSet<String>();
                 Set<String> propertyes = new HashSet<String>();
                 for (Pair<String, String> pair : indexMap.values()) {
                     String clas = pair.getRight();
                     if (INTEGER.equals(clas)) {
                         integer.add(pair.getLeft());
 
                     } else if (DOUBLE.equals(clas)) {
                         doubl.add(pair.getLeft());
                     }
                     propertyes.add(pair.getLeft());
 
                 }
                 neighbour.setProperty(INeoConstants.LIST_ALL_PROPERTIES, propertyes.toArray(new String[0]));
                 neighbour.setProperty(INeoConstants.LIST_DOUBLE_PROPERTIES, doubl.toArray(new String[0]));
                 neighbour.setProperty(INeoConstants.LIST_INTEGER_PROPERTIES, integer.toArray(new String[0]));
                 tx.success();
             } finally {
                 tx.finish();
             }
         }
 
         /**
          * Parse single line
          * 
          * @param line - string line
          * @param fileName - neighbour name
          * @param network - network node
          */
         public void parseLine(String line, Node network, String fileName) {
             String fields[] = splitLine(line);// line.split("\\t");
             Transaction tx = neo.beginTx();
             try {
                 String servCounName = NeoUtils.getTransmissionPropertyName(fileName);
                 serverNodeName.setFieldValues(fields);
                 neighbourNodeName.setFieldValues(fields);
                 String servId = serverNodeName.getId1();
                 Node serverNode = null;
                 if (servId != null) {
                     serverNode = index.getSingleNode(KEY_ID, servId);
                     if (serverNode == null) {
                         servId = serverNodeName.getId2();
                         serverNode = index.getSingleNode(KEY_ID2, servId);
                     }
                 }
                 Node neighbourNode = null;
                 String neighbourId = neighbourNodeName.getId1();
                 if (neighbourId != null) {
                     neighbourNode = index.getSingleNode(KEY_ID, neighbourId);
                     if (neighbourNode == null) {
                         neighbourId = neighbourNodeName.getId2();
                         neighbourNode = index.getSingleNode(KEY_ID2, neighbourId);
                     }
                 }
                 if (serverNode == null) {
                     serverNode = createTransmissionSite(serverNodeName, fields);
                     NeoLoaderPlugin.error("Not found site: " + servId);
                 }
                 if (neighbourNode == null) {
                    neighbourNode = createTransmissionSite(neighbourNodeName, fields);
                     NeoLoaderPlugin.error("Not found site: " + neighbourId);
                 }
                 if (serverNode == null || neighbourNode == null) {
 
                     NeoLoaderPlugin.error("Not found sites for line:\n" + line);
                     return;
                 }
 
                 Relationship relation = serverNode.createRelationshipTo(neighbourNode, NetworkRelationshipTypes.TRANSMISSION);
                 relation.setProperty(INeoConstants.NEIGHBOUR_NAME, fileName);
                 for (Integer index : indexMap.keySet()) {
                     String value = fields[index];
                     if (value.length() > 0) {
                         saveValue(relation, index, value);
                     }
                 }
                 updateCount(serverNode, servCounName);
                 tx.success();
             } catch (Exception e) {
                 NeoLoaderPlugin.error(line + "\n" + e.getLocalizedMessage());
             } finally {
                 tx.finish();
             }
 
         }
 
         /**
          * Creates Transmission Sites if
          * 
          * @param nodeName - Id of node
          * @param fields - line fields
          * @return Node
          */
         private Node createTransmissionSite(NodeName nodeName, String[] fields) {
             if (nodeName==null||(nodeName.getId1()==null&&nodeName.getId2()==null)){
                 return null;
             }
             //TODO create NetworkIndexes  if necessary.
             Transaction tx = neo.beginTx();
             try {
                 if (lastNode == null) {
                     lastNode = findNetworkLastSite();
                 }
                 Node result = neo.createNode();
                 result.setProperty(INeoConstants.PROPERTY_TYPE_NAME, "site");
 
                 String id1 = nodeName.getId1();
                 if (id1 != null) {
                     result.setProperty(INeoConstants.PROPERTY_NAME_NAME, id1);
                     index.index(result, KEY_ID, id1);
                 }
                 String id2 = nodeName.getId2();
                 if (id2 != null) {
                     result.setProperty("site_no", id2);
                     index.index(result, KEY_ID2, id2);
                 }
                 lastNode.createRelationshipTo(result, GeoNeoRelationshipTypes.NEXT);
                 tx.success();
                 lastNode = result;
                 return result;
             } finally {
                 tx.finish();
             }
         }
 
         /**
          * Finds last site in network
          * 
          * @return last node;
          */
         private Node findNetworkLastSite() {
             return network.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH, new ReturnableEvaluator() {
 
                 @Override
                 public boolean isReturnableNode(TraversalPosition currentPos) {
                     Node node = currentPos.currentNode();
                     return !node.hasRelationship(GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING);
                 }
             }, GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING).iterator().next();
         }
 
         /**
          * Updates count of properties
          * 
          * @param serverNode node
          * @param name name of properties
          */
         private void updateCount(Node serverNode, String name) {
             serverNode.setProperty(name, (Integer)serverNode.getProperty(name, 0) + 1);
         }
 
         /**
          * Save value in property container
          * 
          * @param container property container
          * @param index index of property
          * @param value value of property
          * @return true if save is successful
          */
         private boolean saveValue(PropertyContainer container, Integer index, String value) {
             Pair<String, String> pair = indexMap.get(index);
 
             if (pair == null || pair.left() == null) {
                 return false;
             }
             String key = pair.left();
             Object valueToSave;
             String clas = pair.right();
             try {
                 if (clas == null) {
                     try {
                         valueToSave = Integer.parseInt(value);
                         clas = INTEGER;
                     } catch (NumberFormatException e) {
                         valueToSave = Double.parseDouble(value);
                         clas = DOUBLE;
                     }
                 } else if (INTEGER.equals(clas)) {
                     try {
                         valueToSave = Integer.parseInt(value);
                     } catch (NumberFormatException e) {
                         valueToSave = Double.parseDouble(value);
                         clas = DOUBLE;
                     }
                 } else if (DOUBLE.equals(clas)) {
                     valueToSave = Double.parseDouble(value);
                 } else {
                     valueToSave = value;
                     clas = STRING;
                 }
             } catch (NumberFormatException e) {
                 clas = STRING;
                 valueToSave = value;
             }
             if (!valueToSave.toString().equals(value)) {
                 valueToSave = value;
                 clas = STRING;
             }
             pair.setRight(clas);
             // indexMap.put(index, pair.create(key, clas));
             container.setProperty(key, valueToSave);
             return true;
         }
 
         // /**
         // * finds necessary sector in network
         // *
         // * @param nodeName sector name
         // * @param network network node
         // * @param fields array of values
         // * @return necessary sector or null
         // */
         // private Node findSectors(final NodeName nodeName, Node network) {
         // Iterator<Node> iterator = network.traverse(Order.DEPTH_FIRST, StopEvaluator.END_OF_GRAPH,
         // new ReturnableEvaluator() {
         //
         // @Override
         // public boolean isReturnableNode(TraversalPosition currentPos) {
         // return nodeName.isNecessaryNode(currentPos.currentNode());
         // }
         // }, GeoNeoRelationshipTypes.NEXT, Direction.OUTGOING).iterator();
         // return iterator.hasNext() ? iterator.next() : null;
         // }
 
     }
 
     /**
      * <p>
      * Class that contains information about sectors name
      * </p>
      * 
      * @author Cinkel_A
      * @since 1.0.0
      */
     private static class NodeName {
         /** String BTS_NAME field "Site No", "Site ID", "Item Name" */
         private static final String SITE_NO = "site_no";
         /** String LAC field */
         private static final String SITE_ID = "site_id";
         /** String CI field */
         private static final String ITEM_NAME = "item_Name";
         Map<String, String> nameMap = new HashMap<String, String>();
         Map<String, Integer> indexMap = new HashMap<String, Integer>();
         Map<String, String> valuesMap = new HashMap<String, String>();
         private String[] id1;
         private String[] id2;
         private String[] id3;
 
         /**
          * Constructor
          * 
          * @param ci name of "SITE_NO" properties
          * @param lac name of "SITE_ID" properties
          * @param btsName name of "ITEM_NAME" properties
          */
         public NodeName(String[] siteId, String[] siteN, String[] ItemName) {
             id1 = siteId;
             id2 = siteN;
             id3 = ItemName;
             for (String id : siteId) {
                 nameMap.put(id, SITE_ID);
             }
             for (String id : siteN) {
                 nameMap.put(id, SITE_NO);
             }
             for (String id : ItemName) {
                 nameMap.put(id, ITEM_NAME);
             }
         }
 
         /**
          * get Id1 (SITE_ID)
          * 
          * @return id
          */
         public String getId1() {
             String ci = valuesMap.get(SITE_ID);
             if (ci == null || ci.isEmpty()) {
                 return null;
             }
             return ci;
         }
 
         /**
          * get Id1 (SITE_ID)
          * 
          * @return id
          */
         public String getId2() {
             String ci = valuesMap.get(SITE_NO);
             if (ci == null || ci.isEmpty()) {
                 return null;
             }
             return ci;
         }
 
         /**
          * get Id1 (CI+LAC) of node
          * 
          * @param node node
          * @return id
          */
         public static String getId1(Node node) {
             return (String)node.getProperty(INeoConstants.PROPERTY_NAME_NAME, null);
         }
 
         /**
          * get Id1 (CI+LAC) of node
          * 
          * @param node node
          * @return id
          */
         public static String getId2(Node node) {
             Object property = node.getProperty("site_no", null);
             return property == null ? null : property.toString();
         }
 
         /**
          * Checks node
          * 
          * @param checkNode - current node
          * @return true if current node contains necessary name
          */
         public boolean isNecessaryNode(Node checkNode) {
             if (!NeoUtils.getNodeType(checkNode, "").equals("site")) {
                 return false;
             }
             String siteId = valuesMap.get(SITE_ID);
             String siteId2 = valuesMap.get(SITE_NO);
             if (checkNode.hasProperty(SITE_NO) && siteId2 != null) {
                 return siteId2.equals(checkNode.getProperty(SITE_NO).toString());
             }
             return checkNode.hasProperty(INeoConstants.PROPERTY_NAME_NAME) && siteId != null
                     && siteId.equals(checkNode.getProperty(INeoConstants.PROPERTY_NAME_NAME));
         }
 
         /**
          * Sets the properties of sector
          * 
          * @param fields array of values
          */
         public void setFieldValues(String[] fields) {
             valuesMap.clear();
             for (String index : indexMap.keySet()) {
                 valuesMap.put(index, fields[indexMap.get(index)]);
             }
         }
 
         /**
          * Sets field index
          * 
          * @param fieldHeader field name
          * @param i - index
          * @return true if NodeName contains field
          */
         public boolean setFieldIndex(String fieldHeader, int i) {
             String key = nameMap.get(fieldHeader);
             // TODO identify column
             if (key == null && fieldHeader.length() > 1) {
                 String findSubstring = fieldHeader.toLowerCase();
                 for (String nameKey : nameMap.keySet()) {
                     if (nameKey.toLowerCase().equals(findSubstring)) {
                         key = nameMap.get(nameKey);
                         break;
                     }
                 }
             }
             if (key == null) {
                 return false;
             }
             // because site and child site header have equal name
             if (indexMap.get(key) != null) {
                 return false;
             }
             indexMap.put(key, i);
             return true;
         }
 
         /**
          * Check field
          * 
          * @param field - field name
          * @return true if NodeName contains field
          */
         public boolean containsField(String field) {
             return nameMap.keySet().contains(field);
         }
     }
 }
