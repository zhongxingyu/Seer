 /*
  * Copyright 2010 jccastrejon
  *  
  * This file is part of MexADL.
  *
  * MexADL is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * MexADL is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with MexADL.  If not, see <http://www.gnu.org/licenses/>.
  */
 package mx.itesm.mexadl.metrics.util;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.sax.SAXSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import org.apache.commons.io.FileUtils;
 import org.xml.sax.EntityResolver;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 /**
  * The Util class provides helper methods for the MexADL verification process.
  * 
  * @author jccastrejon
  * 
  */
 public class Util {
 
     /**
      * Verification properties.
      */
     private static ResourceBundle properties = ResourceBundle.getBundle("mx.itesm.mexadl.metrics.configuration");
 
     /**
      * JAXP transformer factory.
      */
     private static TransformerFactory transformerFactory = TransformerFactory.newInstance();
 
     /**
      * MEXADL_HOME environment variable.
      */
     private final static String MEXADL_HOME = System.getenv().get("MEXADL_HOME");
 
     /**
      * Date formatter.
      */
     private final static DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy:MM:dd 'at' HH.mm.ss");
 
     /**
      * Get the value associated to the specified property in the MexADL
      * configuration file.
      * 
      * @param clazz
      * @param name
      * @return
      */
     public static String getConfigurationProperty(final Class<?> clazz, final String name) {
         String returnValue;
 
         try {
             returnValue = Util.properties.getString(clazz.getName() + "." + name);
         } catch (Exception e) {
             returnValue = null;
         }
 
         return returnValue;
     }
 
     /**
      * Generate an HTML report for the specified mexadl log.
      * 
      * @param logName
      * @param basedir
      * @param componentTypes
      * @throws Exception
      */
     public static void generateHtmlReport(final String logName, final String basedir,
             final Map<String, String> componentTypes) throws Exception {
         File logFile;
         File reportDir;
         String xsltContent;
 
         if (Util.MEXADL_HOME != null) {
 
             reportDir = new File(basedir, logName);
             if (!reportDir.exists()) {
                 reportDir.mkdir();
             }
 
             logFile = new File("mexadl-" + logName + ".log");
             xsltContent = loadFileContent("mx/itesm/mexadl/metrics/" + logName + ".xslt");
             xsltContent = xsltContent.replaceAll("MEXADL_HOME", Util.MEXADL_HOME);
 
             Util.transformXMLReport2Html(logFile, new ByteArrayInputStream(xsltContent.getBytes("UTF-8")), new File(
                    reportDir, Util.DATE_FORMATTER.format(new Date()) + ".html"), componentTypes);
             logFile.delete();
         }
     }
 
     /**
      * Transform a logging report from XML to HTML format.
      * 
      * @param xmlFile
      * @param xsltFile
      * @param outputFile
      * @param componentTypes
      * @throws TransformerException
      * @throws SAXException
      * @throws IOException
      */
     public static void transformXMLReport2Html(final File xmlFile, final InputStream xsltFile, final File outputFile,
             final Map<String, String> componentTypes) throws TransformerException, SAXException, IOException {
         Source xmlSource;
         Source xsltSource;
         Result outputTarget;
         XMLReader xmlReader;
         String reportContents;
         Transformer transformer;
 
         // Blank reader
         xmlReader = XMLReaderFactory.createXMLReader();
         xmlReader.setEntityResolver(new EntityResolver() {
 
             @Override
             public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                 if (systemId.endsWith(".dtd")) {
                     StringReader stringInput = new StringReader(" ");
                     return new InputSource(stringInput);
                 } else {
                     return null; // use default behavior
                 }
             }
         });
 
         xmlSource = new SAXSource(xmlReader, new InputSource(new FileInputStream(xmlFile)));
         xsltSource = new StreamSource(xsltFile);
         outputTarget = new StreamResult(outputFile);
 
         // Generate output file
         transformer = transformerFactory.newTransformer(xsltSource);
         transformer.transform(xmlSource, outputTarget);
 
         // Add components type information
         reportContents = FileUtils.readFileToString(outputFile, "UTF-8");
         for (String componentType : componentTypes.keySet()) {
             reportContents = reportContents.replace("[ " + componentType + " ]", componentTypes.get(componentType));
         }
         FileUtils.writeStringToFile(outputFile, reportContents);
     }
 
     /**
      * Load the contents of a configuration file into a String.
      * 
      * @param path
      * @return
      * @throws Exception
      */
     public static String loadFileContent(final String path) throws Exception {
         String returnValue;
         InputStream inputStream;
 
         inputStream = Util.class.getClassLoader().getResourceAsStream(path);
 
         returnValue = null;
         if (inputStream != null) {
             Writer writer = new StringWriter();
 
             char[] buffer = new char[1024];
             try {
                 Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
                 int n;
                 while ((n = reader.read(buffer)) != -1) {
                     writer.write(buffer, 0, n);
                 }
             } finally {
                 inputStream.close();
             }
             returnValue = writer.toString();
         }
 
         return returnValue;
     }
 
     /**
      * Get the Java classes in the given directory, including those in
      * sub-directories.
      * 
      * @param directory
      * @return
      */
     public static List<String> getClassesInDirectory(final File directory) {
         File currentFile;
         File[] directoryFiles;
         List<String> innerFiles;
         List<String> returnValue;
 
         returnValue = null;
         directoryFiles = directory.listFiles(new FileFilter() {
             @Override
             public boolean accept(final File pathname) {
                 return (pathname.isDirectory() || pathname.toString().endsWith(".class"));
             }
         });
 
         if ((directoryFiles != null) && (directoryFiles.length > 0)) {
             returnValue = new ArrayList<String>();
             for (int i = 0; i < directoryFiles.length; i++) {
                 currentFile = directoryFiles[i];
 
                 if (currentFile.isDirectory()) {
                     innerFiles = Util.getClassesInDirectory(currentFile);
                     if (innerFiles != null) {
                         returnValue.addAll(innerFiles);
                     }
                 } else {
                     returnValue.add(currentFile.getAbsolutePath());
                 }
             }
         }
 
         return returnValue;
     }
 }
