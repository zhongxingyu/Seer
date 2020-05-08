 /* Copyright (c) 2001 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root 
  * application directory.
  */
 package org.vfny.geoserver.responses;
 
 import java.io.*;
 import java.util.*;
 import java.sql.*;
 import java.util.logging.Logger;
 import org.vfny.geoserver.config.ConfigInfo;
 import org.vfny.geoserver.requests.*;
 
 /**
  * Handles a DescribeFeatureType request and creates a DescribeFeatureType 
  * response GML string.
  *
  *@author Rob Hranac, TOPP
  *@version $VERSION$
  */
 public class DescribeResponse {
 
     /** Standard logging instance for class */
     private static final Logger LOGGER = 
         Logger.getLogger("org.vfny.geoserver.responses");
     
     /** Main XML class for interpretation and response. */
     private String xmlResponse = new String();
     
     /** Bean that holds global server configuration information. */
     private static ConfigInfo config = ConfigInfo.getInstance();
     
     // Initialize some generic GML information
     // ABSTRACT OUTSIDE CLASS, IF POSSIBLE
     
     /** Fixed return header information */
     private static final String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<xs:schema targetNamespace=\"" + config.getUrl() + "\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:vfny=\"" + config.getUrl() + "\" xmlns:gml=\"http://www.opengis.net/gml\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\" version=\"1.0\">\n  <xs:import namespace=\"http://www.opengis.net/gml\" schemaLocation=\"http://www.opengis.net/namespaces/gml/core/feature.xsd\"/>\n\n";
     
     /** Fixed return footer information */
     private static final String FOOTER = "</xs:schema>";
     
     
     /**
      * Constructor with request.
      * @param wfsRequest The DescribeFeatureType reqeuset object.
      */      
     public DescribeResponse(DescribeRequest wfsRequest) {
         
         // generates response, using general function 
         xmlResponse  = generateTypes( wfsRequest );
     }
     
     
     /**
      * Passes the Post method to the Get method, with no modifications.
      */      
     public String getXmlResponse() {
         
         //_log.info("output: " + xmlResponse);
         return xmlResponse;
     }
     
 
     /**
      * Internal method to generate the XML response object, using feature 
      * types.
      * @param wfsRequest The request object.
      */ 
     private String generateTypes(DescribeRequest wfsRequest) {
         
         List requestedTables = wfsRequest.getFeatureTypes();
         //_log.info("these are the reqeusted feature types: " + requestedTables.toString() ); 
         //_log.info("is this null: " + (requestedTables.isEmpty()) ); 
 
         // Initialize database connection information
         String getDescribeFeatureResponse = new String();
         
         // Initialize return information and intermediate return objects
         String tempResponse = new String();
         ComplexType table = new ComplexType();
 
         // Initialize generic header information
         tempResponse = HEADER;
         
         // call appropriate subfunction
         if(requestedTables.isEmpty()) {
             tempResponse = tempResponse + 
                 generateAllTypes(config.getTypeDir());
         } else {
             tempResponse = tempResponse + 
                 generateSpecifiedTypes(requestedTables);
         }
         // Initialize generic footer information
         tempResponse = tempResponse + FOOTER;
         
         //_log.info("output: " + tempResponse);
         return tempResponse;
     }
 
     
     /**
      * Internal method to print just the requested types.
      *
      * @param requestedTables The requested table names.
      */ 
     private String generateSpecifiedTypes(List requestedTables) {
         
         String tempResponse = new String();             
         String currentFile = new String();
	String curTypeName = new String();
	String generatedType = new String();
	ArrayList validTypes = new ArrayList();
         
         // Loop through requested tables to add element types
         for (int i = 0; i < requestedTables.size(); i++ ) {
             
             // set the current file
             // print type data for the table object
            curTypeName = requestedTables.get(i).toString();
	    currentFile = config.getTypeDir() + curTypeName + 
		"/" + config.SCHEMA_FILE;
	    generatedType = writeFile(currentFile);
	    if (!generatedType.equals("")) {
		tempResponse = tempResponse + writeFile( currentFile );
		//_log.info("current file: " + currentFile);
		validTypes.add(curTypeName);
	    } 
       }
         
         // Loop through requested tables again to add elements
         // NOT VERY EFFICIENT - PERHAPS THE MYSQL ABSTRACTION CAN FIX THIS; 
         //  STORE IN HASH?
        for (int i = 0; i < validTypes.size(); i++ ) {
 
             // Print element representation of table
             tempResponse = tempResponse + 
                printElement(validTypes.get(i).toString());
         }
         
         tempResponse = tempResponse + "\n\n";
         return tempResponse;    
     }
     
 
     /**
      * Add feature type info. 
      * @param targetDirectoryName The directory in which to search for files.
      */
     private String generateAllTypes(String targetDirectoryName) {
         
         // holds final response variable
         String tempResponse = new String();
         
         // iterated convenience variables
         File currentDirectory = new File( targetDirectoryName );
         String currentFeatureType = new String();
         String currentFileName = new String();
         
         // keeps master list of files within the directory
         String[] files = currentDirectory.list();
         File[] file = currentDirectory.listFiles();
         
         // Loop through all files in the directory
         for (int i = 0; i < files.length; i++) {
             
             // assign temp variables; convenience/confusion lesseners only
             currentFeatureType = file[i].getName();
             currentFileName = targetDirectoryName + currentFeatureType + "/" +
                 config.SCHEMA_FILE;
             
             // actual work of writing out file is delegated to private function
             tempResponse = tempResponse + writeFile(currentFileName);
             tempResponse = tempResponse + "\n";
         }
         
         
         // Loop through requested files again to add elements
         // NOT VERY EFFICIENT - PERHAPS THE MYSQL ABSTRACTION CAN FIX THIS; 
         //  STORE IN HASH?
         for (int i = 0, n = files.length; i < n; i++) {            
             // assign convenience variable
             currentFeatureType = file[i].getName();
             // Print element representation of table
             tempResponse = tempResponse + printElement( currentFeatureType );
         }
         tempResponse = tempResponse + "\n\n";
         return tempResponse;
     }
     
     
     /**
      * Internal method to print XML element information for table.
      * @param table The table name.
      */ 
     private static String printElement(String table) {
         return "\n  <xs:element name='" + table + "' type='vfny:" + table + "_Type' substitutionGroup='gml:_Feature'/>";
     }
     
     
      /**
       * Adds a feature type object to the final output buffer
       *
       * @param featureTypeName The name of the feature type.
       */
     public String writeFile(String inputFileName) {        
 	LOGGER.finest("writing file " + inputFileName);
         String finalOutput = new String();        
         try {
             File inputFile = new File(inputFileName);
             FileInputStream inputStream = new FileInputStream(inputFile);
             byte[] fileBuffer = new byte[ inputStream.available() ];
             int bytesRead;
             
             while( (bytesRead = inputStream.read(fileBuffer)) != -1 ) {
                 String tempOutput = new String(fileBuffer);
                 finalOutput = finalOutput + tempOutput;
             }
         }
         catch (Exception e) {}
         return finalOutput;       
     }
     
     
 }
