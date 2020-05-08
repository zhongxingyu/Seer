 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.responses.wfs;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Logger;
 
 import javax.xml.transform.TransformerException;
 
 import org.geotools.feature.FeatureType;
 import org.geotools.gml.producer.FeatureTypeTransformer;
 import org.vfny.geoserver.WfsException;
 import org.vfny.geoserver.global.FeatureTypeInfo;
 import org.vfny.geoserver.global.GeoServer;
 import org.vfny.geoserver.global.NameSpaceInfo;
 import org.vfny.geoserver.requests.Request;
 import org.vfny.geoserver.requests.wfs.DescribeRequest;
 import org.vfny.geoserver.responses.Response;
 
 
 /**
  * Handles a DescribeFeatureType request and creates a DescribeFeatureType
  * response GML string.
  *
  * @author Rob Hranac, TOPP
  * @author Chris Holmes, TOPP
 * @version $Id: DescribeResponse.java,v 1.11 2004/01/18 01:29:03 dmzwiers Exp $
  *
  * @task TODO: implement the response streaming in writeTo instead of the
  *       current String generation
  */
 public class DescribeResponse implements Response {
     /** Standard logging instance for class */
     private static final Logger LOGGER = Logger.getLogger(
             "org.vfny.geoserver.responses");
 
     /** Bean that holds global featureType information */
 
     // Initialize some generic GML information
     // ABSTRACT OUTSIDE CLASS, IF POSSIBLE
     private static final String SCHEMA_URI = "\"http://www.w3.org/2001/XMLSchema\"";
     private static final String XS_NAMESPACE = "\n  xmlns:xs=" + SCHEMA_URI;
     private static final String GML_URL = "\"http://www.opengis.net/gml\"";
     private static final String GML_NAMESPACE = "\n  xmlns:gml=" + GML_URL;
     		
     private static final String ELEMENT_FORM_DEFAULT = "\n  elementFormDefault=\"qualified\"";
     private static final String ATTR_FORM_DEFAULT = "\n  attributeFormDefault=\"unqualified\" version=\"1.0\">";
     private static final String TARGETNS_PREFIX = "\n  targetNamespace=\"";
     private static final String TARGETNS_SUFFIX = "\" ";
 
     /** Fixed return footer information */
     private static final String FOOTER = "\n</xs:schema>";
 
     /** Main XML class for interpretation and response. */
     private String xmlResponse = new String();
 
     /**
      * Constructor with request.
      *
      * @param request The DescribeFeatureType request object.
      *
      * @throws WfsException For any problems making the xml response.
      */
     public void execute(Request request) throws WfsException {
         if (!(request instanceof DescribeRequest)) {
             throw new WfsException(
                 "illegal request type, expected DescribeRequest, got "
                 + request);
         }
 
         DescribeRequest wfsRequest = (DescribeRequest) request;
         LOGGER.finer("processing describe request" + wfsRequest);
 
         String outputFormat = wfsRequest.getOutputFormat();
 
         if (!outputFormat.equalsIgnoreCase("XMLSCHEMA")) {
             throw new WfsException("output format: " + outputFormat + " not "
                 + "supported by geoserver");
         }
 
         // generates response, using general function
         xmlResponse = generateTypes(wfsRequest);
         if (!request.getGeoServer().isVerbose()) {
             //strip out the formatting.  This is pretty much the only way we
             //can do this, as the user files are going to have newline
             //characters and whatnot, unless we can get rid of formatting
             //when we read the file, which could be worth looking into if
             //this slows things down.
             xmlResponse = xmlResponse.replaceAll(">\n[ \\t\\n]*", ">");
             xmlResponse = xmlResponse.replaceAll("\n[ \\t\\n]*", " ");
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return DOCUMENT ME!
      */
     public String getContentType(GeoServer gs) {
         return gs.getMimeType();
     }
 
     /**
      * Writes the describe response to the output stream.
      *
      * @param out Where to write to.
      *
      * @throws WfsException For any io exceptions.  Needs to be a buffer
      * or file strategy, if on SPEED it's already too late at this point and
      * the client is going to get some odd errors.
      */
     public void writeTo(OutputStream out) throws WfsException {
         try {
             byte[] content = xmlResponse.getBytes();
             out.write(content);
         } catch (IOException ex) {
             throw new WfsException(ex, "", getClass().getName());
         }
     }
 
     /**
      * Internal method to generate the XML response object, using feature
      * types.
      *
      * @param wfsRequest The request object.
      *
      * @return The XMLSchema describing the features requested.
      *
      * @throws WfsException For any problems.
      */
     private final String generateTypes(DescribeRequest wfsRequest)
         throws WfsException {
         List requestedTypes = wfsRequest.getFeatureTypes();
 
         // Initialize return information and intermediate return objects
         StringBuffer tempResponse = new StringBuffer();
 
         //ComplexType table = new ComplexType();
         if (requestedTypes.size() == 0) {
             //if there are no specific requested types then get all.
             requestedTypes = new ArrayList(wfsRequest.getGeoServer().getData().getFeatureTypeInfos().keySet());
         }
         
         tempResponse.append("<?xml version=\"1.0\" encoding=\"" + wfsRequest.getGeoServer().getCharSet().displayName()+ "\"?>" + "\n<xs:schema ");
 
         //allSameType will throw WfsException if there are types that are not found.
         if (allSameType(requestedTypes, wfsRequest)) {
             //all the requested have the same namespace prefix, so return their
             //schemas.
             FeatureTypeInfo nsInfoType = wfsRequest.getGeoServer().getData().getFeatureTypeInfo((String) requestedTypes
                     .get(0));
 
             //all types have same prefix, so just use the first.
             NameSpaceInfo namespace = nsInfoType.getDataStoreInfo().getNameSpace();
             String targetNs = namespace.getUri();
 
             //String targetNs = nsInfoType.getXmlns();
             tempResponse.append(TARGETNS_PREFIX + targetNs + TARGETNS_SUFFIX);
             //namespace
             tempResponse.append("\n  " + "xmlns:" + namespace.getPrefix() + "=\"" + targetNs + "\""); 
             //xmlns:" + nsPrefix + "=\"" + targetNs
             //+ "\"");
             tempResponse.append(GML_NAMESPACE);
             tempResponse.append(XS_NAMESPACE);
             tempResponse.append(ELEMENT_FORM_DEFAULT + ATTR_FORM_DEFAULT);
 
             //this is not always necessary, but it doesn't seem to hurt...
             tempResponse.append("\n\n<xs:import namespace="
 			+ GML_URL + " schemaLocation=\"gml/2.1.2/feature.xsd\"/>\n\n");
             tempResponse.append(generateSpecifiedTypes(requestedTypes,wfsRequest.getGeoServer()));
         } else {
             //the featureTypes do not have all the same prefixes.
             tempResponse.append(XS_NAMESPACE);
             tempResponse.append(ELEMENT_FORM_DEFAULT + ATTR_FORM_DEFAULT);
 
             Set prefixes = new HashSet();
             Iterator nameIter = requestedTypes.iterator();
 
             //iterate through the types, and make a set of their prefixes.
             while (nameIter.hasNext()) {
                 String typeName = nameIter.next().toString();
                 String typePrefix = wfsRequest.getGeoServer().getData().getFeatureTypeInfo(typeName).getPrefix();
                 prefixes.add(typePrefix);
             }
 
             Iterator prefixIter = prefixes.iterator();
 
             while (prefixIter.hasNext()) {
                 //iterate through prefixes, and add the types that have that prefix.
                 String prefix = prefixIter.next().toString();
                 tempResponse.append(getNSImport(prefix, requestedTypes,wfsRequest));
             }
         }
 
         tempResponse.append(FOOTER);
 
         return tempResponse.toString();
     }
 
     /**
      * Creates a import namespace element, for cases when requests contain
      * multiple namespaces, as you can not have more than one target
      * namespace.  See wfs spec. 8.3.1.  All the typeNames that have the
      * correct prefix are added to the import statement.
      *
      * @param prefix the namespace prefix, which must be mapped in the main
      *        ConfigInfo, for this import statement.
      * @param typeNames a list of all requested typeNames, only those that
      *        match the prefix will be a part of this import statement.
      *
      * @return The namespace element.
      */
     private StringBuffer getNSImport(String prefix, List typeNames, Request r) {
         LOGGER.finer("prefix is " + prefix);
 
         StringBuffer retBuffer = new StringBuffer("\n  <xs:import namespace=\"");
         String namespace = r.getGeoServer().getData().getNameSpace(prefix).getUri();
         retBuffer.append(namespace + "\"");
         retBuffer.append("\n        schemaLocation=\""
             + r.getBaseUrl() + "wfs/DescribeFeatureType?typeName=");
 
         Iterator nameIter = typeNames.iterator();
 
         //boolean first = true;
         while (nameIter.hasNext()) {
             String typeName = nameIter.next().toString();
 
             if (typeName.startsWith(prefix)
                     || ((typeName.indexOf(':') == -1)
                     && prefix.equals(r.getGeoServer().getData().getDefaultNameSpace()
                                                .getPrefix()))) {
                 retBuffer.append(typeName + ",");
             }
         }
 
         retBuffer.deleteCharAt(retBuffer.length() - 1);
         retBuffer.append("\"/>");
 
         return retBuffer;
     }
 
     /**
      * Internal method to print just the requested types.  They should all be
      * in the same namespace, that handling should be done before.  This will
      * not do any namespace handling, just prints up either what's in the 
      * schema file, or if it's not there then generates the types from their
      * FeatureTypes.  Also appends the global element so that the types can
      * substitute as features.
      *
      * @param requestedTypes The requested table names.
      *
      * @return A string of the types printed.
      *
      * @throws WfsException DOCUMENT ME!
      *
      * @task REVISIT: We need a way to make sure the extension bases are
      *       correct. should likely add a field to the info.xml in the
      *       featureTypes folder, that optionally references an extension base
      *       (should it be same namespace? we could also probably just do an
      *       import on the extension base).  This function then would see if
      *       the typeInfo has an extension base, and would add or import the
      *       file appropriately, and put the correct substitution group in
      *       this function.
      */
     private String generateSpecifiedTypes(List requestedTypes, GeoServer gs)
         throws WfsException {
         //TypeRepository repository = TypeRepository.getInstance();
         String tempResponse = new String();
         String currentFile = new String();
         String curTypeName = new String();
         String generatedType = new String();
         Set validTypes = new HashSet();
 
         // Loop through requested tables to add element types
         
         for (int i = 0; i < requestedTypes.size(); i++) {
             // set the current file
             // print type data for the table object
             curTypeName = requestedTypes.get(i).toString();
 
             //TypeInfo meta = repository.getFeatureType(curTypeName);
             FeatureTypeInfo meta = gs.getData().getFeatureTypeInfo(curTypeName);
 
            //curTypeName = meta.getName();
 
             if (meta == null) {
                 throw new WfsException("Feature Type " + curTypeName + " does "
                     + "not exist on this server");
             }
 
             if (!validTypes.contains(meta)) {
                 //FeatureType ft = meta.getSchema();
 
                 //File inputFile = new File(currentFile);
                 //FeatureType ft2 = meta.getSchema();
                 //generatedType = generateFromSchema(ft2);
             	try{
             		generatedType = meta.getXMLSchema();
             	}catch(IOException e){
             		generatedType = "";
             	}
 
                 if (!generatedType.equals("")) {
                     tempResponse = tempResponse + generatedType;
                     validTypes.add(meta);
                 }
             }
         }
 
         // Loop through requested tables again to add elements
         // NOT VERY EFFICIENT - PERHAPS THE MYSQL ABSTRACTION CAN FIX THIS;
         //  STORE IN HASH?
         for (Iterator i = validTypes.iterator(); i.hasNext();) {
             // Print element representation of table
             tempResponse = tempResponse
                 + printElement((FeatureTypeInfo) i.next());
         }
 
         tempResponse = tempResponse + "\n\n";
 
         return tempResponse;
     }
 
     /**
      * Transforms a FeatureTypeInfo into gml, with no headers.
      *
      * @param schema the schema to transform.
      *
      * @return DOCUMENT ME!
      *
      * @throws WfsException DOCUMENT ME!
      *
      * @task REVISIT: when this class changes to writing directly to out this
      *       can just take a writer and write directly to it.
      */
     private String generateFromSchema(FeatureType schema)
         throws WfsException {
         try {
             StringWriter writer = new StringWriter();
             FeatureTypeTransformer t = new FeatureTypeTransformer();
             t.setIndentation(4);
             t.setOmitXMLDeclaration(true);
             t.transform(schema, writer);
 
             return writer.getBuffer().toString();
         } catch (TransformerException te) {
             LOGGER.warning(te.toString());
             throw new WfsException("problem transforming type", te);
         }
     }
 
     /**
      * Internal method to print XML element information for table.
      *
      * @param type The table name.
      *
      * @return The element part of the response.
      */
     private static String printElement(FeatureTypeInfo type) {
         //int prefixDelimPos = typeName.indexOf(":");
         //String tablename = typeName;
         //if (prefixDelimPos > 0) {
         //String tableName = typeName.substring(prefixDelimPos + 1);
         //  }
         return "\n  <xs:element name='" + type.getShortName() + "' type='"
        + type.getShortName() + "_Type' substitutionGroup='gml:_Feature'/>";
     }
 
     /**
      * Adds a feature type object to the final output buffer
      *
      * @param inputFileName The name of the feature type.
      *
      * @return The string representation of the file containing the schema.
      *
      * @throws WfsException For io problems reading the file.
      */
     public String writeFile(String inputFileName) throws WfsException {
         LOGGER.finest("writing file " + inputFileName);
 
         String finalOutput = new String();
 
         try {
             File inputFile = new File(inputFileName);
             FileInputStream inputStream = new FileInputStream(inputFile);
             byte[] fileBuffer = new byte[inputStream.available()];
             int bytesRead;
 
             while ((bytesRead = inputStream.read(fileBuffer)) != -1) {
                 String tempOutput = new String(fileBuffer);
                 finalOutput = finalOutput + tempOutput;
             }
         } catch (IOException e) {
             //REVISIT: should things fail if there are featureTypes that
             //don't have schemas in the right place?  Because as it is now
             //a describe all will choke if there is one ft with no schema.xml
             throw new WfsException("problem writing featureType information "
                 + " from " + inputFileName);
         }
 
         return finalOutput;
     }
 
     /**
      * Checks that the collection of featureTypeNames all have the same prefix.
      * Used to determine if their schemas are all in the same namespace or if
      * imports need to be done.
      *
      * @param featureTypeNames list of featureTypes, generally from a
      *        DescribeFeatureType request.
      *
      * @return true if all the typenames in the collection have the same
      *         prefix.
      *
      * @throws WfsException if any of the names do not exist in this
      *         repository.
      */
     public boolean allSameType(Collection featureTypeNames, Request request)
         throws WfsException {
         Iterator nameIter = featureTypeNames.iterator();
         boolean sameType = true;
 
         if (!nameIter.hasNext()) {
             return false;
         }
 
         String firstPrefix = getPrefix(nameIter.next().toString(),request.getGeoServer());
 
         while (nameIter.hasNext()) {
             if (!firstPrefix.equals(getPrefix(nameIter.next().toString(),request.getGeoServer()))) {
                 return false;
             }
         }
 
         return sameType;
     }
 
     /**
      * gets the prefix for the featureType
      *
      * @param featureTypeName The name of the featureType to retrieve the
      *        prefix for.
      *
      * @return the xml prefix used internally for the passed in featureType.
      *
      * @throws WfsException if the featureTypeName is not in the repository.
      */
     private String getPrefix(String featureTypeName, GeoServer gs) throws WfsException {
         FeatureTypeInfo ftConf = gs.getData().getFeatureTypeInfo(featureTypeName);
 
         if (ftConf == null) {
             throw new WfsException("Feature Type " + featureTypeName + " does "
                 + "not exist or is not enabled on this server");
         }
 
         return ftConf.getPrefix();
     }
 
     /* (non-Javadoc)
      * @see org.vfny.geoserver.responses.Response#abort()
      */
     public void abort(GeoServer gs) {
         // nothing to undo
     }
 }
