 /**
  * Bristlecone Test Tools for Databases
  * Copyright (C) 2006-2007 Continuent Inc.
  * Contact: bristlecone@lists.forge.continuent.org
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of version 2 of the GNU General Public License as
  * published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
  *
  * Initial developer(s): Robert Hodges and Ralph Hannus.
  * Contributor(s):
  */
 
 package com.continuent.bristlecone.evaluator;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Stack;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.DefaultHandler;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 /**
  * This class holds the configuration data for the evaluator. The data is loaded
  * from an XML file.
  * 
  * @author <a href="mailto:ralph.hannus@continuent.com">Ralph Hannus</a>
  */
 public class Configuration extends DefaultHandler
 {
   private String     url;
   private String     driver;
   private List<TableGroup> tables = new ArrayList<TableGroup>();
   private XMLReader  parser;
   private String     configName;
   private int        testDuration;
   private String     password;
   private String     user;
   private TableGroup currentTableGroup;
   private boolean    autoCommit;
   private String     xmlFile;
   private int        statusInterval;
   private ArrayList<Exception> errors = new ArrayList<Exception>();
   private Stack<String> currentTag = new Stack<String>();
   private String     htmlFile;
   private String     csvFile;
   private String     timestampType;
 
   /**
    * Retrieves the path for the output XML file
    * 
    * @return the path for the XML file
    */
   public String getXmlFile()
   {
     return xmlFile;
   }
 
   /**
    * Sets the path for the output XML file.
    * 
    * @param xmlFile the path to the XML file
    */
   public void setXmlFile(String xmlFile)
   {
     this.xmlFile = xmlFile;
   }
 
   /**
    * Indicates whether or not autocommit should be enabled for connections based
    * on this configuration.
    * 
    * @return true autocommit should be enabled
    */
   public boolean isAutoCommit()
   {
     return autoCommit;
   }
 
   /**
    * Sets the autocommit value.
    * 
    * @param autoCommit the new value
    */
   public void setAutoCommit(boolean autoCommit)
   {
     this.autoCommit = autoCommit;
   }
 
   /**
    * Creates a new Configuration. The values for the Confiuration are read from
    * the specified XML file.
    * 
    * @param xmlFile the path to an XML file containing configuration data
    * @throws EvaluatorException when the specified XML file does not exist or is
    *           invalid.
    */
   public Configuration(String xmlFile) throws EvaluatorException
   {
     this(new File(xmlFile));
   }
 
   /**
    * Creates a new Configuration. The values for the Confiuration are read from
    * the specified XML file.
    * 
    * @param xmlFile the path to an XML file containing configuration data
    * @throws EvaluatorException when the specified XML file does not exist or is
    *           invalid.
    */
   public Configuration(File xmlFile) throws EvaluatorException
   {
     Reader reader = null;
     try
     {
       reader = new FileReader(xmlFile);
       readXML(reader);
     }
     catch (FileNotFoundException e)
     {
       throw new EvaluatorException("Could not read the configuration file", e);
     }
     finally
     {
       if (reader != null)
       {
         try
         {
           reader.close();
         }
         catch (IOException e)
         {
         }
       }
     }
   }
 
   /**
    * Creates a new Configuration. The values for the Confiuration are read from
    * the specified Reader instance.
    * 
    * @param xmlReader An open reader that supplies the XML document
    * @throws EvaluatorException when the specified XML is invalid or cannot be
    *           read
    */
   public Configuration(Reader xmlReader) throws EvaluatorException
   {
     readXML(xmlReader);
   }
 
   /**
    * Reads Configuration values from an XML document.
    * 
    * @param reader An open reader that supplies the XML document
    */
   public void readXML(Reader reader) throws EvaluatorException
   {
     // System.setProperty("org.xml.sax.driver",
     // "org.apache.crimson.parser.XMLReaderImpl");
     try
     {
       try
       {
         parser = XMLReaderFactory.createXMLReader();
       }
       catch (SAXException e)
       {
         try
         {
           parser = XMLReaderFactory
               .createXMLReader("org.apache.crimson.parser.XMLReaderImpl");
         }
         catch (SAXException e1)
         {
           parser = XMLReaderFactory
               .createXMLReader("org.apache.xerces.parsers.SAXParser");
         }
       }
 
       // Activate validation
       parser.setFeature("http://xml.org/sax/features/validation", true);
 
       // Install error handler
       parser.setErrorHandler(this);
 
       // Install document handler
       parser.setContentHandler(this);
 
       // Install local entity resolver
       parser.setEntityResolver(this);
 
       // Parse the XML configuration document.
       InputSource input = new InputSource(reader);
       parser.parse(input);
 
       if (errors.size() > 0)
       {
         // for (Iterator i = errors.iterator(); i.hasNext();)
         // {
         // System.out.println("Error: " + i.next());
         // }
         throw new EvaluatorException("Invalid Configuration");
       }
     }
     catch (SAXException e)
     {
       throw new EvaluatorException("Invalid configuration file", e);
     }
     catch (IOException e)
     {
       throw new EvaluatorException("Could not read the configuration file", e);
     }
   }
 
   /**
    * Receive notification of a recoverable parser error. The error is logged and
    * recorded so that an Exception ban be raised when parsing is complete.
    * 
    * @param e The warning information encoded as an exception.
    * @exception org.xml.sax.SAXException Any SAX exception, possibly wrapping
    *              another exception.
    * @see org.xml.sax.ErrorHandler#warning
    * @see org.xml.sax.SAXParseException
    */
   public void error(SAXParseException e) throws SAXException
   {
     if (currentTag.size() > 0)
     {
       System.out.println("Error: " + currentTag.peek() + ": " + e.getMessage());
     }
     else
     {
       System.out.println(e.getMessage());
     }
     errors.add(e);
   }
 
   /**
    * Report a fatal XML parsing error.
    * <p>
    * The default implementation throws a SAXParseException. Application writers
    * may override this method in a subclass if they need to take specific
    * actions for each fatal error (such as collecting all of the errors into a
    * single report): in any case, the application must stop all regular
    * processing when this method is invoked, since the document is no longer
    * reliable, and the parser may no longer report parsing events.
    * </p>
    * 
    * @param e The error information encoded as an exception.
    * @exception org.xml.sax.SAXException Any SAX exception, possibly wrapping
    *              another exception.
    * @see org.xml.sax.ErrorHandler#fatalError
    * @see org.xml.sax.SAXParseException
    */
   public void fatalError(SAXParseException e) throws SAXException
   {
     if (currentTag.size() > 0)
     {
       System.out.println("Error: " + currentTag.peek() + ": " + e.getMessage());
     }
     else
     {
       System.out.println(e.getMessage());
     }
     errors.add(e);
     throw e;
   }
 
   public void warning(SAXParseException e) throws SAXException
   {
     // if (currentTag.isEmpty())
     // {
     // System.out.println("Warning: " + e.getMessage());
     // }
     // else
     // {
     // System.out.println("Warning: " + currentTag.peek() + ": " +
     // e.getMessage());
     // }
     super.warning(e);
   }
 
   /**
    * Receive notification of the end of an XML element. Completes the processing
    * of the current element.
    * 
    * @param name The element type name.
   * @param attributes The specified or defaulted attributes.
    * @exception org.xml.sax.SAXException Any SAX exception, possibly wrapping
    *              another exception.
    * @see org.xml.sax.ContentHandler#endElement
    */
   public void endElement(String uri, String localName, String name)
       throws SAXException
   {
     if (name.equals("TableGroup"))
     {
       currentTableGroup = null;
     }
     currentTag.pop();
   }
 
   /**
    * Retrieves a numeric attribute and converts it to an integer.
    * 
    * @param attributes is the collection of attributes for the current tag.
    * @param id is the name of the desired attribute.
    * @return the value of the attribute
    * @throws SAXException when the value was not a valid number
    */
   private int getNumber(Attributes attributes, String id) throws SAXException
   {
     try
     {
       return Integer.valueOf(attributes.getValue(id)).intValue();
     }
     catch (NumberFormatException e)
     {
       String msg = "Invalid numeric value for" + currentTag.peek() + "." + id;
       System.out.println(msg);
       throw new SAXException(msg);
     }
   }
 
   /**
    * Receive notification of the start of an XML element. Sets the configuration
    * data based on the attributes of the element.
    * 
    * @param name The element type name.
    * @param attributes The specified or defaulted attributes.
    * @exception org.xml.sax.SAXException Any SAX exception, possibly wrapping
    *              another exception.
    * @see org.xml.sax.ContentHandler#startElement
    */
   public void startElement(String uri, String localName, String name,
       Attributes attributes) throws SAXException
   {
     currentTag.push(name);
     if (name.equals("EvaluatorConfiguration"))
     {
       this.configName = attributes.getValue("name");
       this.testDuration = getNumber(attributes, "testDuration");
       this.statusInterval = getNumber(attributes, "statusInterval");
       this.autoCommit = Boolean.valueOf(attributes.getValue("autoCommit"))
           .booleanValue();
       this.xmlFile = attributes.getValue("xmlFile");
       this.htmlFile = attributes.getValue("htmlFile");
       this.csvFile = attributes.getValue("csvFile");
     }
     else if (name.equals("Database"))
     {
       this.driver = attributes.getValue("driver");
       this.url = attributes.getValue("url");
       this.user = attributes.getValue("user");
       this.password = attributes.getValue("password");
       this.timestampType = attributes.getValue("timestampType");
     }
     else if (name.equals("TableGroup"))
     {
       String tableName = attributes.getValue("name");
       int size = getNumber(attributes, "size");
       currentTableGroup = new TableGroup(tableName, size);
       currentTableGroup.setInitializeDDL(Boolean.valueOf(
           attributes.getValue("initializeDDL")).booleanValue());
       currentTableGroup.setTruncateTable(attributes.getValue("truncateTable"));
       tables.add(currentTableGroup);
     }
     else if (name.equals("ThreadGroup"))
     {
       ThreadConfiguration tc = new ThreadConfiguration(currentTableGroup);
       tc.setCount(getNumber(attributes, "threadCount"));
       tc.setName(attributes.getValue("name"));
       tc.setUpdatePercentage(getNumber(attributes, "updates"));
       tc.setDeletePercentage(getNumber(attributes, "deletes"));
       tc.setInsertPercentage(getNumber(attributes, "inserts"));
       tc.setReadSize(getNumber(attributes, "readSize"));
       tc.setThinkTime(getNumber(attributes, "thinkTime"));
       tc.setRampUpIncrement(getNumber(attributes, "rampUpIncrement"));
       tc.setRampUpInterval(getNumber(attributes, "rampUpInterval"));
       tc.setReconnectInterval(getNumber(attributes, "reconnectInterval"));
       tc.setQueryFormat(attributes.getValue("queryFormat"));
       currentTableGroup.addThreadGroup(tc);
     }
     else
     {
       System.out.println("Unexpected name: " + name);
     }
   }
 
   /**
    * Retrieves the class name of the JDBC driver.
    * 
    * @return the JDBC driver class name
    */
   public String getDriver()
   {
     return driver;
   }
 
   /**
    * Sets the JDBC driver class name.
    * 
    * @param driver the name of the JDBC driver
    */
   public void setDriver(String driver)
   {
     this.driver = driver;
   }
 
   public String getUrl()
   {
     return url;
   }
 
   public void setUrl(String url)
   {
     this.url = url;
   }
 
   public void setName(String name)
   {
     this.configName = name;
   }
 
   public String getName()
   {
     return configName;
   }
 
   public int getTestDuration()
   {
     return testDuration;
   }
 
   public String getPassword()
   {
     return password;
   }
 
   public String getUser()
   {
     return user;
   }
 
   public Iterator getTableGroups()
   {
     return tables.iterator();
   }
 
   /**
    * Resolve an external entity. Finds the specified entity on the file or
    * classpath.
    * 
    * @param publicId The public identifer, or null if none is available.
    * @param systemId The system identifier provided in the XML document.
    * @return The new input source, or null to require the default behaviour.
    * @exception java.io.IOException If there is an error setting up the new
    *              input source.
    * @exception org.xml.sax.SAXException Any SAX exception, possibly wrapping
    *              another exception.
    * @see org.xml.sax.EntityResolver#resolveEntity
    */
   public InputSource resolveEntity(String publicId, String systemId)
       throws SAXException
   {
     // File dtd = new File("evaluator.dtd");
     // if (dtd.exists())
     // {
     // try
     // {
     // FileReader reader = new FileReader(dtd);
     // return new InputSource(reader);
     // }
     // catch (Exception e)
     // { // impossible
     // }
     // }
     InputStream stream = Configuration.class.getResourceAsStream("/"
         + "evaluator.dtd");
     if (stream == null)
       throw new SAXException("Cannot find Evaluator DTD file '"
           + "evaluator.dtd" + "' in classpath");
 
     return new InputSource(stream);
   }
 
   public int getStatusInterval()
   {
     return statusInterval;
   }
 
   public void setStatusInterval(int statusInterval)
   {
     this.statusInterval = statusInterval;
   }
 
   public String getHtmlFile()
   {
     return htmlFile;
   }
 
   public void setHtmlFile(String path)
   {
     htmlFile = path;
   }
   
   public String getCsvFile()
   {
     return csvFile;
   }
   
   public void setCsvFile(String path)
   {
     csvFile = path;
   }
 
   public String getTimestampType()
   {
     return timestampType;
   }
 
   public void setTimestampType(String timestampType)
   {
     this.timestampType = timestampType;
   }
 
   public void setTestDuration(int testDuration)
   {
     this.testDuration = testDuration;
   }
 
 }
