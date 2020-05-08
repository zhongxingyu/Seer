 /**
  * JAFER Toolkit Poject.
  * Copyright (C) 2002, JAFER Toolkit Project, Oxford University.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 
 package org.jafer.conf;
 
 import org.jafer.exception.JaferException;
 import org.jafer.util.xml.DOMFactory;
 
import java.io.InputStream;
 import java.net.URL;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import org.apache.xpath.CachedXPathAPI;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * <p>Parses XML files used for configuration of client and server and provides static look up methods eg. getRecordSerializer (configured via recordDescriptor.xml), getBib1Message (bib1Messages.xml).
  * Also, has static utility methods eg. isSyntaxEqual, String convertSyntax(int[]), String getValue(Node), Node selectSingleNode(Node node, String xPath), URL getResource(String path).</p>
  * @author Antony Corfield; Matthew Dovey; Colin Tatham
  * @version 1.0
  */
   public class Config {
 
   public final static String RECORD_DESCRIPTOR_FILE = "org/jafer/conf/recordDescriptor.xml";
   public final static String BIB1_ATTRIBUTES_FILE   = "org/jafer/conf/bib1Attributes.xml";
   public final static String BIB1_DIAGNOSTICS_FILE  = "org/jafer/conf/bib1Diagnostics.xml";
   public final static String SEARCH_PROFILES_FILE   = "org/jafer/conf/searchProfiles.xml";
   public static final String CHARACTER_SETS_FILE    = "org/jafer/conf/characterSets/characterSets.xml";
   public static final String SERVER_CONFIG_FILE     = "org/jafer/conf/server.xml";
   public static final String SERVER_DECODE_FILE     = "org/jafer/xsl/server/server-decode.xsl";
   public static final String SERVER_ENCODE_FILE     = "org/jafer/xsl/server/server-encode.xsl";
 
   private static Document recordTransformDocument;
   private static Hashtable recordName;
   private static Hashtable recordSyntax;
   private static Hashtable recordSerializer;
   private static Hashtable recordSerializerTargetSchema;
   private static Hashtable recordTransformations;
     /** @todo SRW/XMLRecord hack: */
   private static Vector targetSchemaNames;
   private static Vector sourceSchemaNames;
   private static Vector stylesheetNames;
 
   private static Hashtable attributeSets;
   private static Hashtable attributeTypeName;
   private static Hashtable attributeTypeValue;
   private static Hashtable searchProfileSyntax;
   private static Hashtable searchProfileName;
   private static Hashtable bib1DiagMessage;
   private static Hashtable bib1DiagAddInfo;
 
   private static String attributeSetName;
   private static String attributeSetSyntax;
 
   private static CachedXPathAPI xPathAPI;
   private static Config config;
 
   public Config() {}
 
   static {
           xPathAPI = new CachedXPathAPI();
           config = new Config();//
           try {
             buildRecordConfig(RECORD_DESCRIPTOR_FILE);
             buildBib1AttributeConfig(BIB1_ATTRIBUTES_FILE);
             buildBib1DiagnosticConfig(BIB1_DIAGNOSTICS_FILE);
             buildSearchProfileConfig(SEARCH_PROFILES_FILE);
           } catch (Exception e) {
           /** @todo handle exception */
             System.out.println("org.jafer.conf.Config - Error attempting to build configuration!");
           e.printStackTrace();
           }
   }
 
  public static InputStream getResource(String path) {
 
    return new Config().getClass().getClassLoader().getResourceAsStream(path);
   }
 
   private static void buildRecordConfig(String recordDescriptorFile) throws JaferException {
 //Builds XML structure like the following e.g.:
 // <serializer priority="1" syntax="1.2.840.10003.5.10" targetSchema="http://www.openarchives.org/OAI/oai_marc">
 //   from OAI to MODS:
 //   <transform sourceSchema="http://www.openarchives.org/OAI/oai_marc" targetSchema="http://www.loc.gov/mods/">org/jafer/xsl/MARC21.xsl</transform>
 //   from OAI to MARC21:
 //   <transform sourceSchema="http://www.openarchives.org/OAI/oai_marc" targetSchema="http://www.loc.gov/MARC21/slim">org/jafer/xsl/OAIMARC2MARC21slim.xsl
 //       with nested transform from MARC21 to DC:
 //       <transform sourceSchema="http://www.loc.gov/MARC21/slim" targetSchema="http://purl.org/dc/elements/1.1/">org/jafer/xsl/MARC21slim2DC.xsl</transform>
 //  </transform>
 
     recordName = new Hashtable();
     recordSyntax = new Hashtable();
     recordSerializer = new Hashtable();
     recordSerializerTargetSchema = new Hashtable();
     recordTransformations = new Hashtable();
     /** @todo SRW/XMLRecord hack: *//////
     targetSchemaNames = new Vector();
     sourceSchemaNames = new Vector();
     stylesheetNames = new Vector();
 /////////////////////////////////////////
 
     Document recordDescriptorDocument = config.parseDocument(recordDescriptorFile);
     recordTransformDocument = DOMFactory.newDocument();
     Node transformRoot = recordTransformDocument.createElement("transformRoot");
     recordTransformDocument.appendChild(transformRoot);
 
     Hashtable transformNodes;
     String syntax, oidName, serializer, schema, priority;
     Node oidNode, serializerNode, transformNode;
     NodeList oidNodes = selectNodeList(recordDescriptorDocument, "recordDescriptor/oid");
     for (int i = 0; i < oidNodes.getLength(); i++) {
         oidNode = oidNodes.item(i);
         oidName = getValue(selectSingleNode(oidNode, "@name"));
         syntax = getValue(selectSingleNode(oidNode, "@syntax"));
         priority = getValue(selectSingleNode(oidNode, "@priority"));
         serializerNode = selectSingleNode(oidNode, "serializer");
         recordName.put(syntax, oidName);
         recordSyntax.put(oidName, syntax);
         if (serializerNode != null) {
             serializer = getValue(serializerNode);
             schema = getValue(selectSingleNode(serializerNode, "@targetSchema"));
             transformNode = recordTransformDocument.importNode(serializerNode, false);
             ((Element)transformNode).setAttribute("priority", priority);
             ((Element)transformNode).setAttribute("syntax", syntax);
             transformRoot.appendChild(transformNode);
             transformNodes = new Hashtable();
             transformNodes.put("fromSerializer", new Hashtable());
             transformNodes.put("toSerializer", new Hashtable());
             ((Hashtable)transformNodes.get("fromSerializer")).put(schema, transformNode);
             ((Hashtable)transformNodes.get("toSerializer")).put(schema, transformNode);
             buildTransformNodes(oidNode, transformNode, new Vector(), new Hashtable(), transformNodes, schema, true);
             buildTransformNodes(oidNode, transformNode, new Vector(), new Hashtable(), transformNodes, schema, false);
             recordTransformations.put(syntax, transformNodes);
             recordSerializerTargetSchema.put(syntax, schema);
             recordSerializer.put(syntax, serializer);
         }
         /** @todo SRW/XMLRecord hack: */
         buildSchemaTransforms(oidNode);
     }
   }
 
   private static void buildTransformNodes(Node oidNode, Node transformNode, Vector path, Hashtable schemaDepth, Hashtable schemaNodes, String fromSchema, boolean fromSerializer) throws JaferException {
 
     if (path.contains(fromSchema)) return;
     path = new Vector(path);
     path.add(fromSchema);
 
     NodeList nodes;
     Hashtable transformNodes;
     if (fromSerializer) {
       transformNodes = (Hashtable)schemaNodes.get("fromSerializer");
       nodes = selectNodeList(oidNode, "transform[@sourceSchema='" + fromSchema + "']");
     } else {
       transformNodes = (Hashtable)schemaNodes.get("toSerializer");
       nodes = selectNodeList(oidNode, "transform[@targetSchema='" + fromSchema + "']");
     }
 
     for (int i = 0; i < nodes.getLength(); i++) {
         String toSchema;
         if (fromSerializer)
           toSchema = getValue(selectSingleNode(nodes.item(i), "@targetSchema"));
         else
           toSchema = getValue(selectSingleNode(nodes.item(i), "@sourceSchema"));
 
         if (!path.contains(toSchema)) {
 
             if (transformNodes.containsKey(toSchema) &&
                   path.size() < ((Integer)schemaDepth.get(toSchema)).intValue()) {
                 Node child = (Node)transformNodes.get(toSchema);
                 Node parent = child.getParentNode();
                 parent.removeChild(child);
                 transformNodes.remove(toSchema);
                 schemaDepth.remove(toSchema);
             }
 
             if (!transformNodes.containsKey(toSchema)) {
                 Node child = recordTransformDocument.importNode(nodes.item(i), true);
                 transformNode.appendChild(child);
                 schemaDepth.put(toSchema, new Integer(path.size()));
                 transformNodes.put(toSchema, child);
                 buildTransformNodes(oidNode, child, path, schemaDepth, schemaNodes, toSchema, fromSerializer);
             }
         }
     }
   }
 
   private static void buildSchemaTransforms(Node oidNode) throws JaferException {
     /** @todo No priorities, just a list of possible transforms... */
     NodeList nodes = selectNodeList(oidNode, "./*");
     NodeList atts;
     String stylesheetName, targetSchemaName, sourceSchemaName;
     Element el;
     int index;
 
     for (int i=0; i <nodes.getLength(); i++) {
       el = (Element)nodes.item(i);
       if (el.getNodeName().equals("transform")) {
         stylesheetName = getValue(el);
         if (!stylesheetNames.contains(stylesheetName)) {
           sourceSchemaNames.add(el.getAttribute("sourceSchema"));
           targetSchemaNames.add(el.getAttribute("targetSchema"));
           stylesheetNames.add(stylesheetName);
         }
       }
     }
   }
 
   public static Document getRecordTransformDocument() {
 
     return recordTransformDocument;
   }
 
   public static String getRecordSerializer(String syntax) throws JaferException {
 
     if (recordSerializer.containsKey(syntax))
       return (String)recordSerializer.get(syntax);
     else
       throw new JaferException("recordSyntax " + syntax + " not found in recordDescriptor file " + RECORD_DESCRIPTOR_FILE);
   }
 
   public static String getRecordSerializerTargetSchema(String syntax) throws JaferException {
 
     if (recordSerializerTargetSchema.containsKey(syntax))
       return (String)recordSerializerTargetSchema.get(syntax);
     else
       throw new JaferException("recordSyntax " + syntax + " not found in recordDescriptor file " + RECORD_DESCRIPTOR_FILE);
   }
 
   public static String getRecordNameFromSyntax(String syntax) throws JaferException {
 
     if (recordName.containsKey(syntax))
       return (String)recordName.get(syntax);
     else
       throw new JaferException("recordSyntax " + syntax + " not found in recordDescriptor file " + RECORD_DESCRIPTOR_FILE);
   }
 
   public static String getRecordSyntaxFromName(String name) throws JaferException {
 
     if (recordSyntax.containsKey(name))
       return (String)recordSyntax.get(name);
     else
       throw new JaferException("syntax Name " + name + " not found in recordDescriptor file " + RECORD_DESCRIPTOR_FILE);
   }
 
   public static boolean isSyntaxEqual(String syntaxA, String syntaxB) {
 
     return isSyntaxEqual(convertSyntax(syntaxA), convertSyntax(syntaxB));
   }
 
   public static boolean isSyntaxEqual(int[] syntaxA, int[] syntaxB) {
 
     if (syntaxA.length != syntaxB.length)
       return false;
 
     for (int x = 0; x < syntaxA.length; x++) {
       if (syntaxA[x] != syntaxB[x])
             return false;
     }
 
     return true;
   }
 
   public static int[] convertSyntax(String syntaxString) {
 /** @todo rename to convertSyntaxString()? */
     if (syntaxString == null || syntaxString.equals("") || syntaxString.indexOf(".") < 1)
       return new int[0];
 
     String[] oidStringArray = syntaxString.split("\\.");
     int[] syntaxArray = new int[oidStringArray.length];
 
     for (int i = 0; i < syntaxArray.length; i++)
         syntaxArray[i] = Integer.parseInt(oidStringArray[i]);
 /** @todo catch NumberFormatException */
     return syntaxArray;
   }
 
   public static String convertSyntax(int[] syntaxArray) {
 
     String syntaxString = String.valueOf(syntaxArray[0]);
 
     for (int i = 1; i < syntaxArray.length; i++)
        syntaxString = syntaxString.concat("." + String.valueOf(syntaxArray[i]));
 
     return syntaxString;
   }
 
   public static String getRecordSyntax(String schema) throws JaferException {
 
     int currentPriority = 0, priority = Integer.MAX_VALUE;
     String currentSyntax = null, syntax = null;
     Hashtable transformsNodes;
     Node transformNode;
 
     Enumeration syntaxKeys = recordTransformations.keys();
     while (syntaxKeys.hasMoreElements()) {
         currentSyntax = (String)syntaxKeys.nextElement();
         transformsNodes = (Hashtable)((Hashtable)recordTransformations.get(currentSyntax)).get("fromSerializer");
         transformNode = (Node)transformsNodes.get(schema);
         if (transformNode != null) {
           currentPriority = getTransforms(true, transformNode, new Vector());
           if (currentPriority <= priority) {
               priority = currentPriority;
               syntax = currentSyntax;
           }
         }
     }
     if (syntax == null)
       throw new JaferException("NULL recordSyntax: schema " + schema + " not found in recordDescriptor file " + RECORD_DESCRIPTOR_FILE);
     return syntax;
   }
 //
 //  public static Vector getTransformsToSerializer(String syntax, String schema) throws JaferException {
 //
 //      return getTransforms(false, syntax, schema);
 //  }
 //
 //  public static Vector getTransformsFromSerializer(String syntax, String schema) throws JaferException {
 //
 //      return getTransforms(true, syntax, schema);
 //  }
 
   public static Vector getTransforms(boolean fromSerializer, String syntax, String schema) throws JaferException {
 
 
     Hashtable nodes;
     Hashtable transformNodes = (Hashtable)recordTransformations.get(syntax);
     if (fromSerializer && ((Hashtable)transformNodes.get("fromSerializer")).containsKey(schema)) {
       nodes = (Hashtable)transformNodes.get("fromSerializer");
     } else if ((!fromSerializer) && ((Hashtable)transformNodes.get("toSerializer")).containsKey(schema)) {
       nodes = (Hashtable)transformNodes.get("toSerializer");
     } else
       throw new JaferException("Schema " + schema + " for recordSyntax " + syntax + " not found in recordDescriptor file " + RECORD_DESCRIPTOR_FILE);
 
     Node transformNode = (Node)nodes.get(schema);
     Vector transforms = new Vector();
     getTransforms(fromSerializer, transformNode, transforms);
     return transforms;
   }
 
   public static Vector getTransforms(String recordSchema, String requestedRecordSchema) throws JaferException {
 
     /** order of transforms in recordDescriptor.xml dictates priority. */
     /** @todo optimize by caching Vectors of all possible paths. */
     Vector transforms;
     String currentTargetSchemaName, currentSourceSchemaName;
     String firstmatch = null, stylesheetName = null;
 
     currentTargetSchemaName = requestedRecordSchema;
 
       for (int i = 0; i < targetSchemaNames.size(); i++) {
         if (targetSchemaNames.get(i).equals(currentTargetSchemaName)) {
           currentSourceSchemaName = (String)sourceSchemaNames.get(i);
           if (currentSourceSchemaName.equals(recordSchema)) {
             transforms = new Vector();
             transforms.add(stylesheetNames.get(i));
             return transforms;
           }
           if (firstmatch == null) {
             firstmatch = currentSourceSchemaName;
             stylesheetName = (String)stylesheetNames.get(i);
           }
       }
     }
     if (firstmatch == null) /** @todo: message */
       throw new JaferException("Message about failing to find suitable schema combinations.");
 
     transforms = getTransforms(recordSchema, firstmatch);
     transforms.add(stylesheetName);
 
     return transforms;
   }
 
   private static int getTransforms(boolean fromSerializer, Node transformNode, Vector transforms) throws JaferException {
 
     int priority;
 
       if (transformNode.getNodeName().equals("serializer")) {
           try {
             return Integer.parseInt(getValue(selectSingleNode(transformNode, "@priority")));
           } catch (NumberFormatException ex) {
             return Integer.MAX_VALUE;
           }
       }
       priority = getTransforms(fromSerializer, transformNode.getParentNode(), transforms);
       if (fromSerializer)
         transforms.add(getValue(transformNode));
       else
         transforms.add(0, getValue(transformNode));
 
     return priority;
   }
 
 /////////////////////////
   private static Hashtable schemaMappings;
 
   private static void buildSRWSchemaMappings() {
 
     schemaMappings = new Hashtable();
     NodeList list = null;
     try {
       Document doc = config.parseDocument("org/jafer/conf/SRWSchemaMappings.xml");
       list = selectNodeList(doc, "schemaMappings/schema");
     }
     catch (JaferException ex) {
       /** @todo handle this when moving method call to constructor... */
     }
     Element el;
     for (int i=0; i <list.getLength(); i++) {
       el = (Element)list.item(i);
       schemaMappings.put(el.getAttribute("srwSchema"), el.getAttribute("schemaNamespace"));
     }
   }
   public static String translateSRWSchemaName(String SRWSchemaValue) {
 
     String schemaName = null;
     if (schemaMappings == null)
       buildSRWSchemaMappings();
 
     schemaName = (String)schemaMappings.get(SRWSchemaValue);
 
     return schemaName;
   }
 
   /////////////////////////
     private static Hashtable bib1ToCQLMappings;
 
     private static void buildBib1ToCQLMappings() {
 
       bib1ToCQLMappings = new Hashtable();
       NodeList list = null;
       try {
         Document doc = config.parseDocument("org/jafer/conf/cqlContextSets.xml");
         list = selectNodeList(doc, "contextSets/contextSet");
       }
       catch (JaferException ex) {
         ex.printStackTrace();
         /** @todo handle this when moving method call to constructor... */
       }
       Element el;
       for (int i=0; i <list.getLength(); i++) {
         el = (Element)list.item(i);
         String prefix = el.getAttribute("shortId");
         NodeList indexlist = null;
         try {
           indexlist = selectNodeList(el, "indexes/index");
         }
         catch (JaferException ex1) {
           /** @todo handle this when moving method call to constructor... */
         }
         Element el2;
        for (int j=0; j <indexlist.getLength(); j++) {
          el2 = (Element) indexlist.item(j);
           if (el2.getAttribute("attributeSet").equalsIgnoreCase("bib1")) {
             bib1ToCQLMappings.put(el2.getAttribute("useAttribute"),
                                   prefix + "." + el2.getAttribute("name"));
           }
         }
       }
     }
 
     public static String translateBib1ToCQLIndex(String use) {
 
       String cqlIndex = null;
       if (bib1ToCQLMappings == null)
         buildBib1ToCQLMappings();
 
       cqlIndex = (String)bib1ToCQLMappings.get(use);
 
       return cqlIndex;
     }
 
 ////////////////////////
 
   private static void buildBib1AttributeConfig(String bib1AttributesFile) throws JaferException {
 
     Document attributeDocument = config.parseDocument(bib1AttributesFile);
 
     attributeSets = new Hashtable();
     attributeTypeName = new Hashtable();
     attributeTypeValue = new Hashtable();
 
     String name, value;
     Node node;
     NodeList attributeSetNodes, attributeTypeNodes, attributeNodes;
     boolean defaultAttributeSet;
 
     attributeSetSyntax = getValue(selectSingleNode(attributeDocument, "attributeSets/@syntax"));
     attributeSetNodes = selectNodeList(attributeDocument, "attributeSets/attributeSet");
     for (int i = 0; i < attributeSetNodes.getLength(); i++) {
       node = attributeSetNodes.item(i);
       name = getValue(selectSingleNode(node, "@name"));
       Hashtable attributeSet = new Hashtable();
       attributeSets.put(name, attributeSet);
 
       if (Boolean.valueOf(getValue(selectSingleNode(node, "@default"))).booleanValue()) {
         defaultAttributeSet = true;
         attributeSetName = name;
       } else
         defaultAttributeSet = false;
 
       attributeTypeNodes = selectNodeList(node, "attributeType");
       for (int j = 0; j < attributeTypeNodes.getLength(); j++) {
         node = attributeTypeNodes.item(j);
         name = getValue(selectSingleNode(node, "@name"));
         value = getValue(selectSingleNode(node, "@value"));
         Hashtable attributes = new Hashtable();
         attributeSet.put(name, attributes);
 
         if (defaultAttributeSet) {
           attributeTypeName.put(value, name);
           attributeTypeValue.put(name, value);
         }
 
         attributeNodes = selectNodeList(node, "attribute");
         for (int k = 0; k < attributeNodes.getLength(); k++) {
           node = attributeNodes.item(k);
           name = getValue(selectSingleNode(node, "@name"));
           value = getValue(selectSingleNode(node, "@value"));
           attributes.put(name, value);
         }
       }
     }
   }
 
   public static String getAttributeSetName() {
 
     return attributeSetName;
   }
 
   public static String getAttributeSetSyntax() {
 
     return attributeSetSyntax;
   }
 
   public static int getAttributeValue(String attributeSetName, String attributeTypeName, String attributeName) throws JaferException {
 /** @todo throw exception lookup fails */
     int value;
     Hashtable attributeSet = (Hashtable)attributeSets.get(attributeSetName);
     Hashtable attributes = (Hashtable)attributeSet.get(attributeTypeName);
     try {
       value = Integer.parseInt((String)attributes.get(attributeName));
     }
     catch (NumberFormatException ex) {
       throw new JaferException("attribute type ("+ attributeName +") not found in "+ attributeSetName +" attribute lookup table.");
     }
     return value;
   }
   //////////////////////////////////////////////////////////
   /**
    * Following methods added temporarily for use in CQL query generation:
    */
   public static String getSemanticAttributeName(String attributeValue) throws JaferException {
     /** @todo rename method................ */
     /** @todo throw exception if lookup fails */
     Document attributeDocument = config.parseDocument(BIB1_ATTRIBUTES_FILE);
     String name = "";
     Node node = selectSingleNode(attributeDocument, "attributeSets/attributeSet[@name='bib1']/attributeType[@name='semantic']/attribute[@value='"+attributeValue+"']");
 
     if (node != null)
       name = getValue(selectSingleNode(node, "@name"));
     return name;
 
   }
 
   public static String getRelationSymbol(String attributeValue) throws JaferException {
     /** @todo rename method................ */
     /** @todo convert all methods to use XPath? */
     /** @todo throw exception if lookup fails */
     Document attributeDocument = config.parseDocument(BIB1_ATTRIBUTES_FILE);
     String symbol = "";
     Node node = selectSingleNode(attributeDocument, "attributeSets/attributeSet[@name='bib1']/attributeType[@name='relation']/attribute[@value='"+attributeValue+"']");
 
     if (node != null)
       symbol = getValue(selectSingleNode(node, "@symbol"));
     return symbol;
   }
 
 /////////////////////////////////////////////////////////
 
   public static int getAttributeType(String attributeTypeName) throws JaferException {
 
     try {
       return  Integer.parseInt((String)attributeTypeValue.get(attributeTypeName));
     }
     catch (NumberFormatException ex) {
       throw new JaferException("attributeTypeName not found");
     }
   }
 
   public static String getAttributeType(int attributeTypeValue) throws JaferException {
 
     String value = String.valueOf(attributeTypeValue);
     if (attributeTypeName.containsKey(value))
       return (String)attributeTypeName.get(value);
     else
       throw new JaferException("attributeTypeValue not found");
   }
 
   private static void buildBib1DiagnosticConfig(String bib1DiagnosticsFile) throws JaferException {
 
     Document diagnosticDocument = config.parseDocument(bib1DiagnosticsFile);
     bib1DiagMessage = new Hashtable();
     bib1DiagAddInfo = new Hashtable();
 
     String message, value, addInfo;
     Node node;
     NodeList nodes;
 
     nodes = selectNodeList(diagnosticDocument, "bib1Diagnostics/condition");
     for (int i = 0; i < nodes.getLength(); i++) {
       node = nodes.item(i);
       value = getValue(selectSingleNode(node, "@value"));
       message = getValue(node);
       bib1DiagMessage.put(value, message);
       if (getValue(selectSingleNode(node, "@addInfo")) != null) {
         addInfo = getValue(selectSingleNode(node, "@addInfo"));
         bib1DiagAddInfo.put(value, addInfo);
       }
     }
   }
 
   private static void buildSearchProfileConfig(String searchProfilesFile) throws JaferException {
 
     Document profileDocument = config.parseDocument(searchProfilesFile);
     searchProfileSyntax = new Hashtable();
     searchProfileName = new Hashtable();
 
     String name, syntax;
     Node node;
     NodeList searchProfileNodes;
 
     searchProfileNodes = selectNodeList(profileDocument, "searchProfiles/searchProfile");
     for (int i = 0; i < searchProfileNodes.getLength(); i++) {
         node = searchProfileNodes.item(i);
         name = getValue(selectSingleNode(node, "@name"));
         syntax = getValue(selectSingleNode(node, "@syntax"));
         searchProfileName.put(syntax, name);
         searchProfileSyntax.put(name, syntax);
     }
   }
 
   public static String getBib1Diagnostic(int condition) {
 
     return (String)bib1DiagMessage.get(String.valueOf(condition));
   }
 
   public static String getBib1DiagnosticAddInfo(int condition) {
 
     if (bib1DiagAddInfo.get(String.valueOf(condition)) != null)
       return (String)bib1DiagAddInfo.get(String.valueOf(condition));
     return "additional info";
   }
 
   public static String getSearchProfileSyntaxFromName(String searchProfileName) {
 
     return (String)searchProfileSyntax.get(searchProfileName);
   }
 
   public static String getSearchProfileNameFromSyntax(String searchProfileSyntax) {
 
     return (String)searchProfileName.get(searchProfileSyntax);
   }
 
   public static Document getCharacterSetsMap() throws JaferException {
 
     return config.parseDocument(CHARACTER_SETS_FILE);
   }
 
   public static Document getServerConfigDocument() throws JaferException {
 
     return config.parseDocument(SERVER_CONFIG_FILE);
   }
 
  public static InputStream getServerDecode() throws JaferException {
 
     return getResource(SERVER_DECODE_FILE);
   }
 
  public static InputStream getServerEncode() throws JaferException {
 
     return getResource(SERVER_ENCODE_FILE);
   }
 
   public static NodeList selectNodeList(Node node, String xPath) throws JaferException {
     return selectNodeList(node, xPath, false);
   }
 
   private static NodeList selectNodeList(Node node, String xPath, boolean retry) throws JaferException {
     NodeList nodeList = null;
     xPathAPI = new CachedXPathAPI();
     try {
       nodeList = xPathAPI.selectNodeList(node, xPath);
     } catch (javax.xml.transform.TransformerException e) {
       String message = "Error selecting Nodes: " + e.getMessage();
       throw new JaferException(message, e);
     } catch (ArrayIndexOutOfBoundsException e) {
       /**
        * There appears to be a problem with some versions of CachedXPathAPI
        * which produces array out of bounds when used for a long time
        * This is an attempt to workaround - i.e. refresh the Cache if this occurs
        */
       if (retry) {
         xPathAPI = new CachedXPathAPI();
         return selectNodeList(node, xPath, true);
       }
       else {
         String message = "Error selecting Nodes: " + e.getMessage();
         throw new JaferException(message, e);
       }
     }
     return nodeList;
   }
 
   public static Node selectSingleNode(Node node, String xPath) throws JaferException {
     return selectSingleNode(node, xPath, false);
   }
 
   private static Node selectSingleNode(Node node, String xPath, boolean retry) throws JaferException {
     Node selection = null;
     try {
       selection = xPathAPI.selectSingleNode(node, xPath);
     } catch (javax.xml.transform.TransformerException e) {
       String message = "Error selecting Node: " + e.getMessage();
       throw new JaferException(message, e);
     } catch (ArrayIndexOutOfBoundsException e) {
       /**
        * There appears to be a problem with some versions of CachedXPathAPI
        * which produces array out of bounds when used for a long time
        * This is an attempt to workaround - i.e. refresh the Cache if this occurs
        */
       if (retry) {
         xPathAPI = new CachedXPathAPI();
         return selectSingleNode(node, xPath, true);
       } else {
         String message = "Error selecting Node: " + e.getMessage();
         throw new JaferException(message, e);
       }
     }
 
     return selection;
   }
 
   public static String getValue(Node node) {
 /** @todo Should method throw exception instead of returning empty string?? */
     if (node == null)
         return null;
 
     if (node.getNodeType() == Node.ATTRIBUTE_NODE)
         return node.getNodeValue();
 
     if (node.hasChildNodes() &&
         node.getFirstChild().getNodeType() == Node.TEXT_NODE) {
       if (node.getFirstChild().getNodeValue() != null)
         return node.getFirstChild().getNodeValue();
       else
         return ""; //example: text node created with null content.
     }
 
     if (node.getNodeValue() != null)
         return node.getNodeValue();
     else
         return ""; //example: empty node.
   }
 
   private Document parseDocument(String documentPath) throws JaferException {
 
    InputStream stream = null;
     Document document = null;
     try {
      stream = this.getClass().getClassLoader().getResourceAsStream(documentPath);
      document = DOMFactory.parse(stream);
     } catch (JaferException e) {
       String message = "Error parsing document; could not find resource: " + documentPath;
       throw new JaferException(message, e);
     }
     return document;
   }
 }
