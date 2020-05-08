 /*
  * Copyright 2012, United States Geological Survey or
  * third-party contributors as indicated by the @author tags.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/  >.
  *
  */
 package asl.seedscan.config;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.List;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 
 import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 import javax.xml.validation.Validator;
 import javax.xml.XMLConstants;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.xml.sax.SAXException;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Document;
 
 import asl.logging.LogFileConfig;
 import asl.logging.LogDatabaseConfig;
 import asl.seedscan.scan.Scan;
 import asl.seedscan.scan.ScanOperation;
 import asl.seedscan.scan.ScanFrequency;
 
 /**
  * 
  */
 public class ConfigReader
 {
     private static final Logger logger = Logger.getLogger("asl.seedscan.config.ConfigReader");
 
     DocumentBuilderFactory  domFactory    = null;
     private SchemaFactory   schemaFactory = null;
     private DocumentBuilder builder       = null;
 
     private Schema    schema    = null;
     private Validator validator = null;
     private Document  doc       = null;
     private XPath     xpath     = null;
 
     private boolean validate = false;
     private boolean ready    = false;
 
     private Configuration config = null;
 
  // constructor(s)
     public ConfigReader()
     {
         _construct(null);
     }
 
     public ConfigReader(File schemaFile)
     {
         _construct(schemaFile);
     }
 
     private void _construct(File schemaFile)
     {
         xpath = XPathFactory.newInstance().newXPath();
         xpath.setNamespaceContext(new ConfigNamespaceContext());
         domFactory = DocumentBuilderFactory.newInstance();
         domFactory.setNamespaceAware(true);
 
         if (schemaFile != null) {
             schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
             try {
                 schema = schemaFactory.newSchema(schemaFile);
             } catch (SAXException e) {
                 logger.severe("Could not read validation file '" +schemaFile+ "'.\n  Details: " +e);
                 throw new RuntimeException("Could not read validation file.");
             }
             validate = true;
         }
 
         try {
             builder = domFactory.newDocumentBuilder();
         } catch (ParserConfigurationException e) {
             logger.severe("Invalid configuration for SAX parser.\n  Details: " +e);
             throw new RuntimeException("Invalid configuration for SAX parser.");
         }
     }
 
     public Configuration getConfiguration()
     {
         return config;
     }
 
  // read and validate the configuration
     public void loadConfiguration(File configFile)
     {
         config = new Configuration();
 
         if (validate) {
             Validator validator = schema.newValidator();
             try {
                 validator.validate(new StreamSource(configFile));
                 logger.info("Configuration file passed validation.");
             } catch (SAXException e) {
                 logger.severe("Configuration file did not pass validation.\n Details: " +e);
                 throw new RuntimeException("Configuration file failed validation.");
             } catch (IOException e) {
                 logger.severe("Failed to read configuration from file '" +configFile+ "'.\n Details: " +e);
                 throw new RuntimeException("Could not read configuration file.");
             }
         }
 
         try {
             doc = builder.parse(configFile);
             logger.info("Configuration file parsed.");
         } catch (SAXException e) {
             logger.severe("Could not assemble DOM from config file '" +configFile+ "'.\n Details: " +e);
             throw new RuntimeException("Could not assemble configuration from file.");
         } catch (IOException e) {
             logger.severe("Could not read config file '" +configFile+ "'.\n Details:" +e);
             throw new RuntimeException("Could not read configuration file.");
         }
 
         try {
             parseConfig();
         } catch (XPathExpressionException e) {
             logger.severe("XPath expression error!\n Details: " +e);
             e.printStackTrace();
             throw new RuntimeException("XPath expression error.");
         } catch (FileNotFoundException e) {
             logger.severe("Configuration error!\n Details: " +e);
             e.printStackTrace();
             throw new RuntimeException("Configuration error.");
         } catch (IOException e) {
             logger.severe("Configuration error!\n Details: " +e);
             e.printStackTrace();
             throw new RuntimeException("Configuration error.");
         }
         ready = true;
     }
 
  // parse the configuration
     private void parseConfig()
       throws FileNotFoundException,
              IOException,
              XPathExpressionException
     {
         parseConfig(null);
     }
 
     private void parseConfig(String configPassword)
       throws FileNotFoundException,
              IOException,
              XPathExpressionException
     {
         logger.info("Parsing the configuration file");
         logger.fine("Document: " + doc);
 
         // Lock File
         logger.fine("Parsing lockfile.");
         config.setLockFile(xpath.evaluate("//cfg:seedscan/cfg:lockfile/text()", doc));
 
         // Parse Log Config
        LogFileConfig logConfig = new LogFileConfig();
         //config.put("log-levels",      xpath.evaluate("//cfg:seedscan/cfg:log/cfg:level/text()", doc));
 
         String pathLog = "//cfg:seedscan/cfg:log";
         logConfig.setDirectory(xpath.evaluate(pathLog+"/cfg:directory/text()", doc));
         logConfig.setPrefix(   xpath.evaluate(pathLog+"/cfg:prefix/text()", doc));
         logConfig.setSuffix(   xpath.evaluate(pathLog+"/cfg:suffix/text()", doc));
 
         NodeList nodes = (NodeList)xpath.evaluate(pathLog+"/cfg:levels/cfg:level",
                                                   doc, XPathConstants.NODESET);
         for (int i = 0; i < nodes.getLength(); i++)
         {
             Node node = nodes.item(i);
             NamedNodeMap attribs = node.getAttributes();
             Node nameAttrib = attribs.getNamedItem("cfg:name");
             String name = nameAttrib.getNodeValue();
             String level = node.getTextContent();
             logConfig.setLevel(name, Level.parse(level));
             logger.fine("File logging context '" +name+ "' set to level " +level);
         }
 
      // Parse Database Config
         DatabaseConfig dbConfig = new DatabaseConfig();
         Password password;
         logger.fine("Parsing database.");
         String pathDB = "//cfg:seedscan/cfg:database";
         dbConfig.setURI(     xpath.evaluate(pathDB+"/cfg:uri/text()", doc));
         dbConfig.setUsername(xpath.evaluate(pathDB+"/cfg:username/text()", doc));
         String pathPass = pathDB + "/cfg:password";
         nodes = (NodeList)xpath.evaluate(pathPass+"/cfg:plain",
                                          doc, XPathConstants.NODESET);
         if (nodes.getLength() > 0) {
             String text = xpath.evaluate(pathPass+"/cfg:plain/text()", doc);
             password = (Password)(new TextPassword(text));
         } else {
             HexBinaryAdapter hexbin = new HexBinaryAdapter();
             String pathEnc = pathPass + "/cfg:encrypted";
             String salt        = xpath.evaluate(pathEnc+"/cfg:salt/text()", doc);
             String iv          = xpath.evaluate(pathEnc+"/cfg:iv/text()", doc);
             String cipherText  = xpath.evaluate(pathEnc+"/cfg:ciphertext/text()", doc);
             String hmac        = xpath.evaluate(pathEnc+"/cfg:hmac/text()", doc);
             EncryptedPassword cryptPass = new EncryptedPassword(hexbin.unmarshal(iv),
                                                                 hexbin.unmarshal(cipherText),
                                                                 hexbin.unmarshal(hmac));
             PassKey passKey = new PassKey(configPassword, 16, hexbin.unmarshal(salt));
             cryptPass.setKey(passKey.getKey());
             password = (Password)cryptPass;
         }
         dbConfig.setPassword(password);
 
         nodes = (NodeList)xpath.evaluate(pathDB+"/cfg:levels/cfg:level",
                                          doc, XPathConstants.NODESET);
         for (int i = 0; i < nodes.getLength(); i++)
         {
             Node node = nodes.item(i);
             NamedNodeMap attribs = node.getAttributes();
             Node nameAttrib = attribs.getNamedItem("cfg:name");
             String name = nameAttrib.getNodeValue();
             String level = node.getTextContent();
             dbConfig.setLevel(name, Level.parse(level));
             logger.fine("Database logging context '" +name+ "' set to level " +level);
         }
 
      // Parse Scans
         logger.fine("Parsing scans.");
         int id;
         String key;
         NodeList scans = (NodeList)xpath.evaluate("/cfg:seedscan/cfg:scans/cfg:scan",
                                                   doc, XPathConstants.NODESET);
         if ((scans == null) || (scans.getLength() < 1)) {
             logger.warning("No scans in configuration.");
         } 
         else {
             int scanCount = scans.getLength();
             for (int i=0; i < scanCount; i++) {
                 Node node = scans.item(i);
                 Scan scan = new Scan();
 
                 scan.setPathPattern(xpath.evaluate("./cfg:path/text()", node));
                 scan.setStartDepth(Integer.parseInt(xpath.evaluate("./cfg:start_depth/text()", node)));
                 scan.setScanDepth(Integer.parseInt(xpath.evaluate("./cfg:scan_depth/text()", node)));
 
                 ScanFrequency frequency = new ScanFrequency();
                 // TODO: parse frequency
                 scan.setScanFrequency(frequency);
 
                 NodeList ops = (NodeList)xpath.evaluate("./cfg:operations/cfg:operation",
                                                         node, XPathConstants.NODESET);
                 int opCount = ops.getLength();
                 if ((ops == null) || (opCount < 1)) {
                     logger.warning("No operations found in scan " +i+ ".");
                 } else {
                     for (int j=1; j <= opCount; j++) {
                         ScanOperation operation = new ScanOperation();
                         scan.addOperation(operation);
                     }
                 }
 
                 config.addScan(scan);
             }
         }
         logger.fine("Configuration: " + config);
     }
 
 }
