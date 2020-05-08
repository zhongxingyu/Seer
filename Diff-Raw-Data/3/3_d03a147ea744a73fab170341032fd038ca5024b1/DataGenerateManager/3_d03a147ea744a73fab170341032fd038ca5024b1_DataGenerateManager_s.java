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
 
 package org.amanzi.neo.data_generator;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.sax.SAXTransformerFactory;
 import javax.xml.transform.sax.TransformerHandler;
 import javax.xml.transform.stream.StreamResult;
 
 import org.amanzi.neo.data_generator.data.calls.GeneratedCallsData;
 import org.amanzi.neo.data_generator.generate.IDataGenerator;
 import org.amanzi.neo.data_generator.generate.calls.CsvDataGenerator;
 import org.amanzi.neo.data_generator.generate.calls.log_data.GroupCallsGenerator;
 import org.amanzi.neo.data_generator.generate.calls.log_data.ITSIAttachDataGenerator;
 import org.amanzi.neo.data_generator.generate.calls.log_data.IndividualCallsGenerator;
 import org.amanzi.neo.data_generator.generate.calls.log_data.SDSDataGenerator;
 import org.amanzi.neo.data_generator.generate.calls.log_data.TSMDataGenerator;
 import org.amanzi.neo.data_generator.generate.calls.xml_data.EmergencyXMLDataGenerator;
 import org.amanzi.neo.data_generator.generate.calls.xml_data.GroupCallXmlDataGenerator;
 import org.amanzi.neo.data_generator.generate.calls.xml_data.HoCcXmlDataGenerator;
 import org.amanzi.neo.data_generator.generate.calls.xml_data.IndividualCallXmlDataGenerator;
 import org.amanzi.neo.data_generator.generate.calls.xml_data.ItsiAttachXmlDataGenerator;
 import org.amanzi.neo.data_generator.generate.calls.xml_data.SDSXmlDataGenerator;
 import org.amanzi.neo.data_generator.generate.calls.xml_data.TSMXmlDataGenerator;
 import org.amanzi.neo.data_generator.generate.nemo.NemoDataGenerator;
 import org.amanzi.neo.data_generator.generate.nokia.NokiaTopologyGenerator;
 import org.amanzi.neo.data_generator.utils.NeoDataUtils;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.RelationshipType;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.kernel.EmbeddedGraphDatabase;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.AttributesImpl;
 
 /**
  * <p>
  * Manager for getting generators for different data.
  * </p>
  * 
  * @author Shcharbatsevich_A
  * @since 1.0.0
  */
 public class DataGenerateManager {
 
     /**
      * Returns AMS data generator for individual calls.
      * 
      * @param aDirectory String (path to save data)
      * @param aHours Integer (count of hours)
      * @param aHourDrift Integer (drift of start time)
      * @param aCallsPerHour Integer (call count in hour)
      * @param aCallPerHourVariance Integer (call variance in hour)
      * @param aProbes Integer (probes count)
      * @return AmsDataGenerator.
      */
     public static IDataGenerator getIndividualAmsGenerator(String aDirectory, Integer aHours, Integer aHourDrift,
             Integer aCallsPerHour, Integer aCallPerHourVariance, Integer aProbes) {
         return new IndividualCallsGenerator(aDirectory, aHours, aHourDrift, aCallsPerHour, aCallPerHourVariance, aProbes);
     }
     
     /**
      * Returns AMS data generator for individual calls (xml).
      * 
      * @param aDirectory String (path to save data)
      * @param aHours Integer (count of hours)
      * @param aHourDrift Integer (drift of start time)
      * @param aCallsPerHour Integer (call count in hour)
      * @param aCallPerHourVariance Integer (call variance in hour)
      * @param aProbes Integer (probes count)
      * @return AmsDataGenerator.
      */
     public static IDataGenerator getXmlIndividualAmsGenerator(String aDirectory, Integer aHours, Integer aHourDrift,
             Integer aCallsPerHour, Integer aCallPerHourVariance, Integer aProbes) {
         return new IndividualCallXmlDataGenerator(aDirectory, aHours, aHourDrift, aCallsPerHour, aCallPerHourVariance, aProbes);
     }
     
     /**
      * Returns AMS data generator for Handover/Cell change calls (xml).
      * 
      * @param aDirectory String (path to save data)
      * @param aHours Integer (count of hours)
      * @param aHourDrift Integer (drift of start time)
      * @param aCallsPerHour Integer (call count in hour)
      * @param aCallPerHourVariance Integer (call variance in hour)
      * @param aProbes Integer (probes count)
      * @return AmsDataGenerator.
      */
     public static IDataGenerator getXmlHoCcAmsGenerator(String aDirectory, Integer aHours, Integer aHourDrift,
             Integer aCallsPerHour, Integer aCallPerHourVariance, Integer aProbes) {
         return new HoCcXmlDataGenerator(aDirectory, aHours, aHourDrift, aCallsPerHour, aCallPerHourVariance, aProbes);
     }
 
     /**
      * Returns AMS data generator for group calls.
      * 
      * @param aDirectory String (path to save data)
      * @param aHours Integer (count of hours)
      * @param aHourDrift Integer (drift of start time)
      * @param aCallsPerHour Integer (call count in hour)
      * @param aCallPerHourVariance Integer (call variance in hour)
      * @param aProbes Integer (probes count)
      * @param aMaxGroupSize the a max group size
      * @return AmsDataGenerator.
      */
     public static IDataGenerator getGroupAmsGenerator(String aDirectory, Integer aHours, Integer aHourDrift, Integer aCallsPerHour,
             Integer aCallPerHourVariance, Integer aProbes, Integer aMaxGroupSize) {
         return new GroupCallsGenerator(aDirectory, aHours, aHourDrift, aCallsPerHour, aCallPerHourVariance, aProbes, aMaxGroupSize);
     }
     
     /**
      * Returns AMS data generator for group calls (xml).
      * 
      * @param aDirectory String (path to save data)
      * @param aHours Integer (count of hours)
      * @param aHourDrift Integer (drift of start time)
      * @param aCallsPerHour Integer (call count in hour)
      * @param aCallPerHourVariance Integer (call variance in hour)
      * @param aProbes Integer (probes count)
      * @param aMaxGroupSize the a max group size
      * @return AmsDataGenerator.
      */
     public static IDataGenerator getXmlGroupAmsGenerator(String aDirectory, Integer aHours, Integer aHourDrift, Integer aCallsPerHour,
             Integer aCallPerHourVariance, Integer aProbes, Integer aMaxGroupSize) {
         return new GroupCallXmlDataGenerator(aDirectory, aHours, aHourDrift, aCallsPerHour, aCallPerHourVariance, aProbes, aMaxGroupSize);
     }
     
     /**
      * Returns AMS data generator for EC1 calls (xml).
      * 
      * @param aDirectory String (path to save data)
      * @param aHours Integer (count of hours)
      * @param aHourDrift Integer (drift of start time)
      * @param aCallsPerHour Integer (call count in hour)
      * @param aCallPerHourVariance Integer (call variance in hour)
      * @param aProbes Integer (probes count)
      * @param aMaxGroupSize the a max group size
      * @return AmsDataGenerator.
      */
     public static IDataGenerator getXmlEmergencyAmsGenerator(String aDirectory, Integer aHours, Integer aHourDrift, Integer aCallsPerHour,
             Integer aCallPerHourVariance, Integer aProbes, Integer aMaxGroupSize) {
         return new EmergencyXMLDataGenerator(aDirectory, aHours, aHourDrift, aCallsPerHour, aCallPerHourVariance, aProbes, aMaxGroupSize);
     }
     
     /**
      * Returns AMS data generator for SDS messages.
      * 
      * @param aDirectory String (path to save data)
      * @param aHours Integer (count of hours)
      * @param aHourDrift Integer (drift of start time)
      * @param aCallsPerHour Integer (call count in hour)
      * @param aCallPerHourVariance Integer (call variance in hour)
      * @param aProbes Integer (probes count)
      * @return AmsDataGenerator.
      */
     public static IDataGenerator getSDSMessagesGenerator(String aDirectory, Integer aHours, Integer aHourDrift, Integer aCallsPerHour,
             Integer aCallPerHourVariance, Integer aProbes) {
         return new SDSDataGenerator(aDirectory, aHours, aHourDrift, aCallsPerHour, aCallPerHourVariance, aProbes);
     }
     
     /**
      * Returns AMS data generator for SDS messages (xml).
      * 
      * @param aDirectory String (path to save data)
      * @param aHours Integer (count of hours)
      * @param aHourDrift Integer (drift of start time)
      * @param aCallsPerHour Integer (call count in hour)
      * @param aCallPerHourVariance Integer (call variance in hour)
      * @param aProbes Integer (probes count)
      * @return AmsDataGenerator.
      */
     public static IDataGenerator getXmlSDSMessagesGenerator(String aDirectory, Integer aHours, Integer aHourDrift, Integer aCallsPerHour,
             Integer aCallPerHourVariance, Integer aProbes) {
         return new SDSXmlDataGenerator(aDirectory, aHours, aHourDrift, aCallsPerHour, aCallPerHourVariance, aProbes);
     }
     
     /**
      * Returns AMS data generator for TSM messages.
      * 
      * @param aDirectory String (path to save data)
      * @param aHours Integer (count of hours)
      * @param aHourDrift Integer (drift of start time)
      * @param aCallsPerHour Integer (call count in hour)
      * @param aCallPerHourVariance Integer (call variance in hour)
      * @param aProbes Integer (probes count)
      * @return AmsDataGenerator.
      */
     public static IDataGenerator getTSMMessagesGenerator(String aDirectory, Integer aHours, Integer aHourDrift, Integer aCallsPerHour,
             Integer aCallPerHourVariance, Integer aProbes) {
         return new TSMDataGenerator(aDirectory, aHours, aHourDrift, aCallsPerHour, aCallPerHourVariance, aProbes);
     }
     
     /**
      * Returns AMS data generator for TSM messages (xml).
      * 
      * @param aDirectory String (path to save data)
      * @param aHours Integer (count of hours)
      * @param aHourDrift Integer (drift of start time)
      * @param aCallsPerHour Integer (call count in hour)
      * @param aCallPerHourVariance Integer (call variance in hour)
      * @param aProbes Integer (probes count)
      * @return AmsDataGenerator.
      */
     public static IDataGenerator getXmlTSMMessagesGenerator(String aDirectory, Integer aHours, Integer aHourDrift, Integer aCallsPerHour,
             Integer aCallPerHourVariance, Integer aProbes) {
         return new TSMXmlDataGenerator(aDirectory, aHours, aHourDrift, aCallsPerHour, aCallPerHourVariance, aProbes);
     }
     
     /**
      * Returns AMS data generator for ITSI attach data.
      * 
      * @param aDirectory String (path to save data)
      * @param aHours Integer (count of hours)
      * @param aHourDrift Integer (drift of start time)
      * @param aCallsPerHour Integer (call count in hour)
      * @param aCallPerHourVariance Integer (call variance in hour)
      * @param aProbes Integer (probes count)
      * @return AmsDataGenerator.
      */
     public static IDataGenerator getItsiAttachGenerator(String aDirectory, Integer aHours, Integer aHourDrift, Integer aCallsPerHour,
             Integer aCallPerHourVariance, Integer aProbes) {
         return new ITSIAttachDataGenerator(aDirectory, aHours, aHourDrift, aCallsPerHour, aCallPerHourVariance, aProbes);
     }
     
     /**
      * Returns AMS data generator for ITSI attach data (xml).
      * 
      * @param aDirectory String (path to save data)
      * @param aHours Integer (count of hours)
      * @param aHourDrift Integer (drift of start time)
      * @param aCallsPerHour Integer (call count in hour)
      * @param aCallPerHourVariance Integer (call variance in hour)
      * @param aProbes Integer (probes count)
      * @return AmsDataGenerator.
      */
     public static IDataGenerator getXmlItsiAttachGenerator(String aDirectory, Integer aHours, Integer aHourDrift, Integer aCallsPerHour,
             Integer aCallPerHourVariance, Integer aProbes) {
         return new ItsiAttachXmlDataGenerator(aDirectory, aHours, aHourDrift, aCallsPerHour, aCallPerHourVariance, aProbes);
     }
     
     /**
      * Returns AMS data generator for Csv statistics data.
      * 
      * @param aDirectory String (path to save data)
      * @param aHours Integer (count of hours)
      * @param aHourDrift Integer (drift of start time)
      * @param aCallsPerHour Integer (call count in hour)
      * @param aCallPerHourVariance Integer (call variance in hour)
      * @param aProbes Integer (probes count)
      * @return AmsDataGenerator.
      */
     public static IDataGenerator getCsvStatisticsGenerator(String aDirectory, Integer aHours, Integer aHourDrift, Integer aCallsPerHour,
             Integer aCallPerHourVariance, Integer aProbes, boolean needDuplicates) {
         return new CsvDataGenerator(aDirectory, aHours, aHourDrift, aCallsPerHour, aCallPerHourVariance, aProbes,needDuplicates);
     }
 
     /**
      * Returns Nokia Topology data generator.
      * 
      * @param aPath String (path to save file)
      * @param aFileName String (file name)
      * @param bscs Integer (BSCs count)
      * @param sites Integer (maximum sites count for one BSC)
      * @param sectors Integer (maximum sectors count for one site)
      * @param extUmtsCount Integer (external UMTS sectors count)
      * @param latBorders Float[] (must be like {min_latitude,max_latitude})
      * @param lonBorders Float[] (must be like {min_longitude,max_longitude})
      * @return NokiaTopologyGenerator
      */
     public static IDataGenerator getNokiaTopologyGenerator(String aPath, String aFileName, Integer bscs, Integer sites,
             Integer sectors, Integer extUmtsCount, Float[] latBorders, Float[] lonBorders) {
         return new NokiaTopologyGenerator(aPath, aFileName, bscs, sites, sectors, extUmtsCount, latBorders, lonBorders);
     }
 
     /**
      * Returns generator for ams data.
      * 
      * @param data IDataGenerator generated ams data
      * @param aDirectory the a directory
      * @param aFileName the a file name
      * @return the nemo data generator
      */
     public static IDataGenerator getNemoDataGenerator(GeneratedCallsData data, String aDirectory, String aFileName) {
         return new NemoDataGenerator(data.getData(), aDirectory, aFileName);
     }
 
     /**
      * Creates the etalon network.
      * 
      * @param networkName the network name
      * @param neo the neo
      * @return the node
      */
     public static Node createEtalonNetwork(String networkName, String fileName, EmbeddedGraphDatabase neo) {
         Transaction tx = neo.beginTx();
         try {
             Map<String, Object> propertyMap = new HashMap<String, Object>();
             propertyMap.put("name", networkName);
             propertyMap.put("type", "gis");
             propertyMap.put("bbox", new double[] {3290118.0, 3290118.0, 5621767.0, 5637016.0});
             propertyMap.put("crs", "EPSG:31467");
             propertyMap.put("crs_type", "geographic");
             propertyMap.put("gis_type", "network");
             propertyMap.put("network_type", "radio");
             Node result = NeoDataUtils.createNode(propertyMap, neo);
             NeoDataUtils.createRelationship(neo.getReferenceNode(), result, "CHILD");
             propertyMap.clear();
             propertyMap.put("name", networkName);
             propertyMap.put("type", "network");
             propertyMap.put("filename", fileName);
             Node network = NeoDataUtils.createNode(propertyMap, neo);
             NeoDataUtils.createRelationship(result, network, "NEXT");
             propertyMap.clear();
             propertyMap.put("name", "ERNBC1");
             propertyMap.put("type", "rnc");
             propertyMap.put("userLabel", "ERNBC1");
             Node rnc = NeoDataUtils.createNode(propertyMap, neo);
             NeoDataUtils.createRelationship(network, rnc, "CHILD");
 
             Node site = NeoDataUtils.createSite(rnc, "3043", 3290118.0, 5621767.0, neo);
             propertyMap.clear();
             propertyMap.put("name", "30431");
             propertyMap.put("type", "sector");
             propertyMap.put("azimuth", 335);
             propertyMap.put("userLabel", "ERNBC1");
             propertyMap.put("sector_type", "utran");
             Node sector = NeoDataUtils.createNode(propertyMap, neo);
             NeoDataUtils.createRelationship(site, sector, "CHILD");
             propertyMap.clear();
             tx.success();
             return result;
         } finally {
             tx.finish();
         }
     }
 
     public static enum NeoRelationTypes implements RelationshipType {
         CHILD, NEXT;
     }
 
     /**
      * @param fileName
      * @param etalonGis
      * @throws IOException
      * @throws TransformerConfigurationException
      * @throws SAXException
      */
     public static void generateEriccsonTopology(String fileName, Node etalonGis, EmbeddedGraphDatabase neo) throws IOException,
             TransformerConfigurationException, SAXException {
         Transaction tx = neo.beginTx();
         try {
             File file = new File(fileName);
             PrintWriter out = new PrintWriter(file);
             StreamResult streamResult = new StreamResult(out);
             SAXTransformerFactory tf = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
             TransformerHandler hd = tf.newTransformerHandler();
             Transformer serializer = hd.getTransformer();
             serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
             serializer.setOutputProperty(OutputKeys.INDENT, "yes");
             hd.setResult(streamResult);
             hd.startDocument();
             AttributesImpl atts = new AttributesImpl();
             atts.addAttribute("", "", "xmlns:un", "CDATA", "utranNrm.xsd");
             atts.addAttribute("", "", "xmlns:xn", "CDATA", "genericNrm.xsd");
             atts.addAttribute("", "", "xmlns:gn", "CDATA", "geranNrm.xsd");
             atts.addAttribute("", "", "xmlns:es", "CDATA", "EricssonSpecificAttributes.6.9.xsd");
             atts.addAttribute("", "", "xmlns", "CDATA", "configData.xsd");
             hd.startElement("", "", "bulkCmConfigDataFile", atts);
             atts.clear();
             hd.startElement("", "", "fileHeader", atts);
             hd.endElement("", "", "fileHeader");
             atts.addAttribute("", "", "dnPrefix", "CDATA", "Undefined");
             hd.startElement("", "", "configData", atts);
             atts.clear();
             atts.addAttribute("", "", "id", "CDATA", "IL_R");
             hd.startElement("", "SubNetwork", "xn:SubNetwork", atts);
             atts.clear();
             atts.addAttribute("", "", "id", "CDATA", "3000");
             hd.startElement("", "MeContext", "xn:MeContext", atts);
             atts.clear();
             atts.addAttribute("", "", "id", "CDATA", "1");
             hd.startElement("", "ManagedElement", "xn:ManagedElement", atts);
             atts.clear();
 
             Relationship rel = etalonGis.getSingleRelationship(NeoRelationTypes.NEXT, Direction.OUTGOING);
             if (rel != null) {
                 Node network = rel.getOtherNode(etalonGis);
                 new EriccsonGenerator(hd, network).generate();
 
             }
             hd.endElement("", "ManagedElement", "xn:ManagedElement");
             hd.endElement("", "MeContext", "xn:MeContext");
             hd.endElement("", "SubNetwork", "xn:SubNetwork");
             hd.endElement("", "", "configData");
             hd.endElement("", "", "bulkCmConfigDataFile");
             hd.endDocument();
             out.close();
         } finally {
             tx.finish();
         }
     }
 
     private static class EriccsonGenerator {
 
         private final TransformerHandler hd;
         private final Node network;
         private String siteId;
 
         /**
          * @param hd
          * @param network
          */
         public EriccsonGenerator(TransformerHandler hd, Node network) {
             this.hd = hd;
             this.network = network;
         }
 
         /**
          * @throws SAXException
          */
         public void generate() throws SAXException {
             for (Relationship relation : network.getRelationships(NeoRelationTypes.CHILD, Direction.OUTGOING)) {
                 generarteSubChild(relation.getOtherNode(network));
             }
         }
 
         /**
          * @param otherNode
          * @throws SAXException
          */
         private void generarteSubChild(Node node) throws SAXException {
             AttributesImpl atts = new AttributesImpl();
             String type = (String)node.getProperty("type", "");
             String name = (String)node.getProperty("name", "");
             atts.clear();
             if (type.equals("rnc")) {
                 atts.addAttribute("", "", "id", "CDATA", "1");
                 hd.startElement("", "RncFunction", "un:RncFunction", atts);
                 atts.clear();
                 hd.startElement("", "attributes", "un:attributes", atts);
                 for (String key : node.getPropertyKeys()) {
                     char[] chArr = node.getProperty(key).toString().toCharArray();
                     if (key.equals("name")) {
                         key = "userLabel";
                     }
                     hd.startElement("", key, "un:" + key, atts);
                     hd.characters(chArr, 0, chArr.length);
                     hd.endElement("", key, "un:" + key);
                 }
                 hd.endElement("", "attributes", "un:attributes");
                 for (Relationship relation : node.getRelationships(NeoRelationTypes.CHILD, Direction.OUTGOING)) {
                     generarteSubChild(relation.getOtherNode(node));
                 }
                 hd.endElement("", "RncFunction", "un:RncFunction");
             } else if (type.equals("site")) {
                 siteId = "SubNetwork=IL_R,MeContext=ERNBC1,ManagedElement=1,RncFunction=1,IubLink=IUB_" + name;
                 for (Relationship relation : node.getRelationships(NeoRelationTypes.CHILD, Direction.OUTGOING)) {
                     generarteSubChild(relation.getOtherNode(node));
                 }
             } else if (type.equals("sector")) {
                 atts.clear();
                 atts.addAttribute("", "", "id", "CDATA", name);
                 hd.startElement("", "UtranCell", "un:UtranCell", atts);
                 atts.clear();
                 hd.startElement("", "attributes", "un:attributes", atts);
                 hd.startElement("", "utranCellIubLink", "un:utranCellIubLink", atts);
                 final char[] charArray = siteId.toCharArray();
                 hd.characters(charArray, 0, charArray.length);
                 hd.endElement("", "utranCellIubLink", "un:utranCellIubLink");
                 for (String key : node.getPropertyKeys()) {
                     char[] chArr = node.getProperty(key).toString().toCharArray();
                     if (key.equals("name")) {
                         key = "userLabel";
                     } else if (key.equals("PROPERTY_SECTOR_CI")) {
                         key = "cId";
                     } else if (key.equals("PROPERTY_SECTOR_LAC")) {
                         key = "lac";
                     }
                     hd.startElement("", key, "un:" + key, atts);
                     hd.characters(chArr, 0, chArr.length);
                     hd.endElement("", key, "un:" + key);
                 }
                 hd.endElement("", "attributes", "un:attributes");
                 hd.endElement("", "UtranCell", "un:UtranCell");
             } else {
                 System.err.println(String.format("error type %s of node %s", type, node.toString()));
             }
         }
 
     }
 }
