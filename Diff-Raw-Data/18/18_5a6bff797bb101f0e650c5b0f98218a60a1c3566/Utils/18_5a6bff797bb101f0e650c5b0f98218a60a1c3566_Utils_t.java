 //: "The contents of this file are subject to the Mozilla Public License
 //: Version 1.1 (the "License"); you may not use this file except in
 //: compliance with the License. You may obtain a copy of the License at
 //: http://www.mozilla.org/MPL/
 //:
 //: Software distributed under the License is distributed on an "AS IS"
 //: basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 //: License for the specific language governing rights and limitations
 //: under the License.
 //:
 //: The Original Code is Guanxi (http://www.guanxi.uhi.ac.uk).
 //:
 //: The Initial Developer of the Original Code is Alistair Young alistair@codebrane.com
 //: All Rights Reserved.
 //:
 
 package org.guanxi.common;
 
 import java.io.*;
 import java.net.URL;
 import java.rmi.server.UID;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.TimeZone;
 import java.util.zip.*;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.apache.xml.security.exceptions.Base64DecodingException;
 import org.apache.xml.security.utils.Base64;
 import org.apache.xmlbeans.XmlObject;
 import org.apache.xmlbeans.XmlOptions;
 import org.guanxi.common.definitions.Shibboleth;
 import org.guanxi.xal.saml_2_0.metadata.EntitiesDescriptorDocument;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * <font size=5><b></b></font>
  *
  * @author Alistair Young alistair@smo.uhi.ac.uk
  * @author matthew
  */
 public class Utils {
   /** OS dependent line engine, e.g. \n */
   public static final String LINE_ENDING = System.getProperty("line.separator");
   /** Doesn't provide compatibility with the compression format used by both GZIP and PKZIP */
   public static final boolean RFC1951_WRAP = false;
   /** Provides compatibility with the compression format used by both GZIP and PKZIP */
   public static final boolean RFC1951_NO_WRAP = true;
   /** Shortcut for using default compression level */
   public static final int RFC1951_DEFAULT_COMPRESSION_LEVEL = Deflater.DEFAULT_COMPRESSION;
   
   /**
    * Returns the parameters and their values from an HTTP request
    *
    * @param request HTTP request object
    * @return Hashtable of parameters and their values
    */
   public static Hashtable<String, String> getRequestParameters(HttpServletRequest request) {
     Hashtable<String, String> params = new Hashtable<String, String>();
     Enumeration<?> e = request.getParameterNames();
     String name,value;
 
     while (e.hasMoreElements()) {
       name = (String)e.nextElement();
       value = request.getParameter(name);
       params.put(name, value);
     }
 
     return params;
   }
 
   public static String getUniqueID() {
     UID uid = new UID();
     return uid.toString();
   }
 
   public static String base64(Document inDocToEncode) {
     try {
       DOMSource domSource = new DOMSource(inDocToEncode);
       StringWriter writer = new StringWriter();
       StreamResult result = new StreamResult(writer);
       TransformerFactory tf = TransformerFactory.newInstance();
       Transformer transformer = tf.newTransformer();
       transformer.transform(domSource, result);
       return Base64.encode(writer.toString().getBytes());
     }
     catch(TransformerException te) {
       return null;
     }
   }
 
   public static String base64(byte[] data) {
     return Base64.encode(data);
   }
 
   public static String decodeBase64(String b64Data) {
     try {
       return new String(Base64.decode(b64Data));
     }
     catch(Base64DecodingException bde) {
       return null;
     }
   }
 
  public static byte[] decodeBase64b(String b64Data) {
    try {
      return Base64.decode(b64Data);
    }
    catch(Base64DecodingException bde) {
      return null;
    }
  }

   /**
    * Gets the text value of a particular node in the config file
    *
    * @param configNode The parent node of the node we're interested in
    * @param option The node whose text we want
    * @return Text value of the node
    */
   public static String getConfigOption(Node configNode, String option) {
     NodeList childNodes = null;
     int childCount = 0;
 
     NodeList nodes = configNode.getChildNodes();
 
     if (nodes == null) return "-1";
 
     for (int count=0; count < nodes.getLength(); count++) {
       if (nodes.item(count).getLocalName() == null) continue;
 
       // Have we found the node we're looking for?
       if (nodes.item(count).getLocalName().equalsIgnoreCase(option)) {
         // Get the child nodes. We're looking for the text node containing the value
         childNodes = nodes.item(count).getChildNodes();
 
         for (childCount=0; childCount < childNodes.getLength(); childCount++) {
           // Got a text node?
           if (childNodes.item(childCount).getNodeType() == Node.TEXT_NODE) {
             // Anything in it?
             if (childNodes.item(childCount).getNodeValue() != null)
               // Sometimes you can get blank text nodes with Xerces
               if (!childNodes.item(childCount).getNodeValue().equalsIgnoreCase("")) {
                 return childNodes.item(childCount).getNodeValue();
             }
           }
         }
       }
     }
 
     return "-1";
   }
 
   public static void zipDirectory(String dir, ZipOutputStream zipStream) {
     try {
       File dirToZip = new File(dir);
 
       // Have a gander in the directory to see what's there
       String[] dirList = dirToZip.list();
 
       byte[] readBuffer = new byte[2156];
       int bytesIn = 0;
 
       for (int count=0; count < dirList.length; count++) {
         File dirFile = new File(dirToZip, dirList[count]);
 
         // If the next entity is a directory, call recursively
         if (dirFile.isDirectory()) {
           zipDirectory(dirFile.getPath(), zipStream);
           continue;
         }
 
         // Get a handle to the file...
         FileInputStream fis = new FileInputStream(dirFile);
         // ...get an entry ready for it ...
         ZipEntry zipEntry = new ZipEntry(dirFile.getPath());
         // ...bung it in the ZIP stream...
         zipStream.putNextEntry(zipEntry);
         // ...and read the data into the ZIP stream
         while ((bytesIn = fis.read(readBuffer)) != -1) {
           zipStream.write(readBuffer, 0, bytesIn);
         }
         
         fis.close();
       }
     }
     catch(Exception e) {
       System.out.println(e);
     }
   }
 
   /**
    * Creates an ID that is compatible with XML Schema
    *
    * @return ID that is compatible with XML Schema
    */
   public static String createNCNameID() {
     // xsd:NCName is derived from xsd:Name, which can't start with a number
     String id = new UID().toString();
     id = id.replaceAll(":", "-");
     id = "GUANXI-" + id;
     return id;
   }
 
   /**
    * Parses a SAML2 Metadata document
    *
    * @param metadataURL The url of the metadata
    * @return EntitiesDescriptorDocument for the metadata
    * @throws GuanxiException if an error occurs
    */
   public static EntitiesDescriptorDocument parseSAML2Metadata(String metadataURL) throws GuanxiException {
     try {
       return EntitiesDescriptorDocument.Factory.parse(new URL(metadataURL).openStream());
     }
     catch(Exception e) {
       throw new GuanxiException(e);
     }
   }
 
   /**
    * Writes a SAML2 metadata document to disk
    *
    * @param saml2MetadataDoc The SAML2 metadata document
    * @param filenameAndPath The full path and name of the file to write
    * @throws GuanxiException if an error occurs
    */
   public static void writeSAML2MetadataToDisk(EntitiesDescriptorDocument saml2MetadataDoc,
                                               String filenameAndPath) throws GuanxiException {
     HashMap<String, String> namespaces = new HashMap<String, String>();
     ByteArrayOutputStream buffer;
     OutputStream out;
 
     namespaces.put(Shibboleth.NS_SAML_10_PROTOCOL, Shibboleth.NS_PREFIX_SAML_10_PROTOCOL);
     namespaces.put(Shibboleth.NS_SAML_10_ASSERTION, Shibboleth.NS_PREFIX_SAML_10_ASSERTION);
     
     XmlOptions xmlOptions = new XmlOptions();
     xmlOptions.setSavePrettyPrint();
     xmlOptions.setSavePrettyPrintIndent(2);
     xmlOptions.setUseDefaultNamespace();
     xmlOptions.setSaveAggressiveNamespaces();
     xmlOptions.setSaveSuggestedPrefixes(namespaces);
     xmlOptions.setSaveNamespacesFirst();
 
     buffer = new ByteArrayOutputStream();
     try {
       saml2MetadataDoc.save(buffer, xmlOptions); // throws IOException, but shouldnt as ByteArrayOutputStream will not
       out = new FileOutputStream(filenameAndPath);
       try {
         out.write(buffer.toByteArray());
       }
       finally {
         out.close();
       }
     }
     catch (IOException ioe) {
       throw new GuanxiException(ioe);
     }
   }
 
   /**
    * Converts a local time to UTC Zulu format:
    * 2005-06-21T11:13:29Z
    *
    * @param obj The XmlObject that contains the local time in an attribute
    * @param interval the interval in minutes to add to any other time attrbutes
    */
   public static void zuluXmlObject(XmlObject obj, int interval) {
     SimpleDateFormat zulu = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
     zulu.setTimeZone(TimeZone.getTimeZone("GMT"));
     Calendar calNow = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
     Calendar calAfter = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
     calAfter.add(Calendar.MINUTE, interval);
 
     NamedNodeMap attrs = obj.getDomNode().getAttributes();
     for (int cc=0; cc < attrs.getLength(); cc++) {
       Node attr = attrs.item(cc);
       if (attr.getNodeName().equals("NotBefore")) {
         attr.setNodeValue(zulu.format(calNow.getTime()));
       }
       if (attr.getNodeName().equals("NotOnOrAfter")) {
         attr.setNodeValue(zulu.format(calAfter.getTime()));
       }
       if (attr.getNodeName().equals("IssueInstant")) {
         attr.setNodeValue(zulu.format(calNow.getTime()));
       }
       if (attr.getNodeName().equals("AuthenticationInstant")) {
         attr.setNodeValue(zulu.format(calNow.getTime()));
       }
       if (attr.getNodeName().equals("AuthnInstant")) {
         attr.setNodeValue(zulu.format(calNow.getTime()));
       }
     }
   }
 
   /**
    * Generates local time in UTC Zulu format:
    * 2005-06-21T11:13:29Z
    *
    * @return The time now in Zulu format
    */
   public static String zuluNow() {
     SimpleDateFormat zulu = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
     zulu.setTimeZone(TimeZone.getTimeZone("GMT"));
     Calendar calNow = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
     return zulu.format(calNow.getTime());
   }
   
   /**
    * This reads the input stream completely, returning the result. The input stream is
    * always in a closed state after calling this, even if an error has occurred.
    * 
    * @param in stream to read from
    * @return array of bytes read from the stream
    * @throws GuanxiException if an error occurs
    */
   public static byte[] read(InputStream in) throws GuanxiException {
     ByteArrayOutputStream out = new ByteArrayOutputStream();
     byte[] buffer = new byte[1024];
     int read;
 
     try {
       while ((read = in.read(buffer)) != -1) {
         out.write(buffer, 0, read);
       }
       return out.toByteArray();
     }
     catch(IOException ioe) {
       // Reading from the stream failed
       return null;
     }
     finally {
       try {
         in.close();
       }
       catch(IOException ioe) {
         // Closing the stream failed. Shouldn't stop us returning the data though
       }
     }
   }
 
   /**
    * Reconstitutes data deflated according to RFC1951
    *
    * @param deflatedData the deflated data to reconstitute
    * @param useWrap if true then the ZLIB header and checksum fields will not be used.
    *        This provides compatibility with the compression format used by both GZIP and PKZIP.
    * @return String representing the reconstituted data
    * @throws GuanxiException if an error occurs
    */
  public static String inflate(byte[] deflatedData, boolean useWrap) throws GuanxiException {
     try {
      byte[] inflatedData = new byte[(10 * deflatedData.length)];
       Inflater decompresser = new Inflater(useWrap);
      decompresser.setInput(deflatedData, 0, deflatedData.length);
       int inflatedBytesLength = decompresser.inflate(inflatedData);
       decompresser.end();
       return new String(inflatedData, 0, inflatedBytesLength);
     }
     catch(DataFormatException dfe) {
       throw new GuanxiException(dfe);
     }
   }
 
   /**
    * Deflates data according to RFC1951
    *
    * @param data the data to deflate
    * @param compressionLevel the compression level to use
    * @param useWrap if true then the ZLIB header and checksum fields will not be used.
    *        This provides compatibility with the compression format used by both GZIP and PKZIP.
    * @return String representing the deflated data
    */
   public static String deflate(String data, int compressionLevel, boolean useWrap) {
     byte[] deflatedData = new byte[data.length()];
     Deflater deflater = new Deflater(compressionLevel, useWrap);
     deflater.setInput(data.getBytes());
     deflater.finish();
     int deflatedBytesLength = deflater.deflate(deflatedData);
     return new String(deflatedData, 0, deflatedBytesLength);
   }
 }
