 package org.esgf.datacart;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.Iterator;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.esgf.metadata.JSONArray;
 import org.esgf.metadata.JSONException;
 import org.esgf.metadata.JSONObject;
 import org.esgf.metadata.XML;
 import org.jdom.Element;
 import org.jdom.output.XMLOutputter;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 public class FileElement implements DataCartElement{
 
     /** Description */
     private String fileId;
     /** Description */
     private String title;
     /** Description */
     private String size;
     
 
     /** Description */
     private String tracking_id;
     /** Description */
     private String checksum;
     /** Description */
     private String checksum_type;
     
 
     /** Description */
     private String hasGrid;
     /** Description */
     private String hasHttp;
     /** Description */
     private String hasOpenDap;
     /** Description */
     private String hasSRM;
     
 
     /** Description */
     private ServicesElement servicesElement;
     /** Description */
     private MIMESElement mimesElement;
     /** Description */
     private URLSElement urlsElement;
     /** Description */
     private String technoteStr;
     
     private final static String testInitializationFile = "C:\\Users\\8xo\\esgf-web-fe\\fileelement.xml";
     
     
     
     /** Description of FileElement2() construction
      * 
      * DOCUMENT ME!
      * 
      */
     public FileElement() {
         
     }
 
     
     /** Description of toElement()
      * 
      * @return serialized XML element equivalent of the class
      */
     public Element toElement() {
         Element fileEl = new Element("file");
         
         if(this.fileId != null) {
             Element fileIdEl = new Element("fileId");
             fileIdEl.addContent(this.fileId);
             fileEl.addContent(fileIdEl);
         }
         
         if(this.size != null) {
             Element sizeEl = new Element("size");
             sizeEl.addContent(this.size);
             fileEl.addContent(sizeEl);
         }
         
         if(this.title != null) {
             Element titleEl = new Element("title");
             titleEl.addContent(this.title);
             fileEl.addContent(titleEl);
         }
         
         if(this.tracking_id != null) {
             Element tracking_idEl = new Element("tracking_id");
             tracking_idEl.addContent(this.tracking_id);
             fileEl.addContent(tracking_idEl);
         }
 
         if(this.checksum != null) {
             Element checksumEl = new Element("checksum");
             checksumEl.addContent(this.checksum);
             fileEl.addContent(checksumEl);
         }
 
         if(this.checksum_type != null) {
             Element checksum_typeEl = new Element("checksum_type");
             checksum_typeEl.addContent(this.checksum_type);
             fileEl.addContent(checksum_typeEl);
         }
         
         if(this.hasGrid != null) {
             Element hasGridEl = new Element("hasGrid");
             hasGridEl.addContent(this.hasGrid);
             fileEl.addContent(hasGridEl);
         }
         
         if(this.hasHttp != null) {
             Element hasHttpEl = new Element("hasHttp");
             hasHttpEl.addContent(this.hasHttp);
             fileEl.addContent(hasHttpEl);
         }
 
         if(this.hasOpenDap != null) {
             Element hasOpenDapEl = new Element("hasOpenDap");
             hasOpenDapEl.addContent(this.hasOpenDap);
             fileEl.addContent(hasOpenDapEl);
         }
 
         if(this.hasSRM != null) {
             Element hasSRMEl = new Element("hasSRM");
             hasSRMEl.addContent(this.hasSRM);
             fileEl.addContent(hasSRMEl);
         }
         
         if(this.technoteStr != null) {
             Element technoteEl = new Element("technote");
             technoteEl.addContent(this.technoteStr);
             fileEl.addContent(technoteEl);
         }
 
         if(this.servicesElement != null) {
             fileEl.addContent(this.servicesElement.toElement());
         }
         if(this.urlsElement != null) {
             fileEl.addContent(this.urlsElement.toElement());
         }
         if(this.mimesElement != null) {
             fileEl.addContent(this.mimesElement.toElement());
         }
         
         return fileEl;
     }
     
 
     /** Description of toXML()
      * 
      * @return
      */
     public String toXML() {
         String xml = "";
         
         Element fileEl = this.toElement();
 
         XMLOutputter outputter = new XMLOutputter();
         xml = outputter.outputString(fileEl);
         
         return xml;
     }
 
     /** Description of toJSONObject()
      * 
      * @return
      */
     public JSONObject toJSONObject() {
         
         JSONObject json = null;
         
         try {
             json = XML.toJSONObject(this.toXML());
         } catch (JSONException e) {
             System.out.println("Problem in toJSONObject");
             e.printStackTrace();
         }
         
         return json;
     }
     
     /** Description of toJSON()
      * 
      * @return
      */
     public String toJSON() {
      
         String json = null;
         
         try {
             json = this.toJSONObject().toString(3);
         } catch (JSONException e) {
             System.out.println("Problem in toJSON");
             e.printStackTrace();
         }
         
         return json;
     }
     
     
     
     /** Description of toFile()
      * 
      * @param file Filename of the output
      */
     public void toFile(String file) {
         
         try {
             BufferedWriter out = new BufferedWriter(new FileWriter(file));
             out.write(new XmlFormatter().format(this.toXML()));
             out.close();
         } 
         catch (IOException e) { 
             e.printStackTrace();
             System.out.println("Exception ");
 
         }
         
     }
     
     
     public void fromSolr(JSONObject solrResponse) {
         Iterator iter = solrResponse.sortedKeys();
         
 
         this.tracking_id = "unknown";
         this.checksum = "unknown";
         this.checksum_type = "unknown";
         this.technoteStr = "NA";
         
         while(iter.hasNext()) {
             String key = (String)iter.next();
 
             String value = null;
             try {
                 value = solrResponse.getString(key);
             } catch (JSONException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             //System.out.println("key: " + key + " value: " + value);
             //set the fileid
             if(key.equals("id")) {
                 
                 this.fileId = value;
                 /*if(this.fileId.equals("cmip5.output1.NCC.NorESM1-M.historicalExt.6hr.atmos.6hrLev.r2i1p1.v20111102.hus_6hrLev_NorESM1-M_historicalExt_r2i1p1_2006010100-2006063018.nc|bmbf-ipcc-ar5.dkrz.de")){
                     //System.out.println("file");
                 }
                 */
             } 
             //set the file size element
             else if(key.equals("size")) {
                 
                 this.size = value;
                 
             }
             //set the file title element
             else if(key.equals("title")) {
                 
                 this.title = value;
                 
             }
             //set the file title element
             else if(key.equals("checksum")) {
                 
                 JSONArray checksumsJSON;
                 try {
                     checksumsJSON = (JSONArray)solrResponse.getJSONArray(key);
                     this.checksum = (String)checksumsJSON.get(0);
                 } catch (JSONException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
                 //System.out.println("                   checksum: " + this.checksum);
                 
             }
             //set the file title element
             else if(key.equals("tracking_id")) {
                 
                 JSONArray trackingIdJSON;
                 try {
                     trackingIdJSON = (JSONArray)solrResponse.getJSONArray(key);
                     this.tracking_id = (String)trackingIdJSON.get(0);
                 } catch (JSONException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
                 //System.out.println("                   TrackingId: " + this.tracking_id);
                 
             }
             //set the file title element
             else if(key.equals("checksum_type")) {
                 
                 
                 JSONArray checksum_typeJSON;
                 try {
                     checksum_typeJSON = (JSONArray)solrResponse.getJSONArray(key);
                     this.checksum_type = (String)checksum_typeJSON.get(0);
                 } catch (JSONException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
                 
                 //System.out.println("Checksum Type: " + this.checksum_type);
                 
                 
             }
             else if(key.equals("url")) {
                 
                 
                 //set the urls, mimes, and services elements
                 URLSElement urlsElement = new URLSElement();
                 MIMESElement mimesElement = new MIMESElement();
                 ServicesElement servicesElement = new ServicesElement();
                 JSONArray urlsJSON = null;
                 try {
                     urlsJSON = (JSONArray)solrResponse.getJSONArray(key);
                 } catch (JSONException e1) {
                     // TODO Auto-generated catch block
                     e1.printStackTrace();
                 }
                 
                 for(int i=0;i<urlsJSON.length();i++) {
                     String urlStr = null;
                     try {
                         urlStr = urlsJSON.get(i).toString();
                     } catch (JSONException e) {
                         // TODO Auto-generated catch block
                         e.printStackTrace();
                     }
                     
                     String [] urlStrTokens = urlStr.split("\\|");
                     
                     String url = urlStrTokens[0];
                     urlsElement.addUrl(url);
                     
                     String mime = urlStrTokens[1];
                     mimesElement.addMime(mime);
                     
                     String service = urlStrTokens[2];
                     servicesElement.addService(service);
                     if(service.equals("OPENDAP")) {
                         this.hasOpenDap = "true";
                         //System.out.print("\tService is an OPendap");
                     } else if(service.equals("HTTPServer")) {
                         //System.out.print("\tService is an HTTP");
                         this.hasHttp = "true";
                     } else if(service.equals("GridFTP")) {
                         this.hasGrid = "true";
                         //System.out.print("\tService is an GRIDFTP");
                     } else if(service.equals("SRM")) {
                         this.hasSRM = "true";
                         //System.out.print("\tService is an GRIDFTP");
                     }
                 }
 
                 if(this.hasOpenDap == null) {
                     this.hasOpenDap = "false";
                 } 
                 if(this.hasGrid == null) {
                     this.hasGrid = "false";
                 } 
                 if(this.hasHttp == null) {
                     this.hasHttp = "false";
                 } 
                 if(this.hasSRM == null) {
                     this.hasSRM = "false";
                 }
                 
                 this.urlsElement = urlsElement;
                 this.mimesElement = mimesElement;
                 this.servicesElement = servicesElement;
 
                 
                 
             }
             else if(key.equals("xlink")) {
                 
                 try {
                     JSONArray xlinkJSON = (JSONArray)solrResponse.getJSONArray(key);
                     //System.out.println("\n****XLINK LENGTH****\n\n" + xlinkJSON.length());
                     
                     //just get the pdf url
                     String [] technoteTokens = ((String)xlinkJSON.get(0)).split("\\|");
                     
                     String technoteStr = technoteTokens[0];
                     
                     this.technoteStr = technoteStr;
                     
                     //System.out.println("\ttechnote: " + this.technoteStr);
                 } catch(Exception e) {
                     e.printStackTrace();
                 }
                
             }
             
             
         }
     }
     
     /** Description 
      * 
      * @param xmlStr
      */
     public void fromXML(String xmlStr) {
         try {
             DocumentBuilderFactory dbf =
                 DocumentBuilderFactory.newInstance();
             DocumentBuilder db = dbf.newDocumentBuilder();
             InputSource is = new InputSource();
             is.setCharacterStream(new StringReader(xmlStr));
 
             Document doc = db.parse(is);
             
             doc.getDocumentElement().normalize();
             
             org.w3c.dom.Element fileElement = doc.getDocumentElement();
             
             this.readHelper(fileElement);
             
         } catch(Exception e) {
             e.printStackTrace();
         }
     }
     
     
     /**Description of fromFile()
      * 
      * @param file
      */
     public void fromFile(String file) {
         
         this.fileId = null;
         this.size = null;
         this.title = null;
         
         File fXmlFile = new File(file);
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder dBuilder = null;
         
         try {
             dBuilder = dbFactory.newDocumentBuilder();
             Document doc = dBuilder.parse(fXmlFile);
 
             doc.getDocumentElement().normalize();
             
             org.w3c.dom.Element docElement = doc.getDocumentElement();
             
             this.readHelper(docElement);
             
             
             
         }catch (ParserConfigurationException e) {
             e.printStackTrace();
         } catch (SAXException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         
     }
     
     /** Description of readHelper
      * 
      * @param docElement
      */
     public void readHelper(org.w3c.dom.Element docElement) {
         
         
         if(docElement.getNodeName().equals("file")) {
             
             NodeList fileNodeList = docElement.getChildNodes();//doc.getDocumentElement().getChildNodes();
             
             //for (int temp = 0; temp < nList.getLength(); temp++) {
             for(int i=0;i<fileNodeList.getLength();i++) {
                 Node topLevelFileNode = fileNodeList.item(i);
                 if (topLevelFileNode.getNodeType() == Node.ELEMENT_NODE) {
                     org.w3c.dom.Element topLevelElement = (org.w3c.dom.Element) topLevelFileNode;
                     if(topLevelElement.getTagName().equals("fileId")) {
                         this.fileId = topLevelElement.getTextContent();
                     } else if(topLevelElement.getTagName().equals("size")) {
                         this.size = topLevelElement.getTextContent();
                     } else if(topLevelElement.getTagName().equals("title")) {
                         this.title = topLevelElement.getTextContent();
                     } else if(topLevelElement.getTagName().equals("tracking_id")) {
                         this.tracking_id = topLevelElement.getTextContent();
                     } else if(topLevelElement.getTagName().equals("checksum")) {
                         this.checksum = topLevelElement.getTextContent();
                     } else if(topLevelElement.getTagName().equals("checksum_type")) {
                         this.checksum_type = topLevelElement.getTextContent();
                     } else if(topLevelElement.getTagName().equals("hasGrid")) {
                         this.hasGrid = topLevelElement.getTextContent();
                     } else if(topLevelElement.getTagName().equals("hasHttp")) {
                         this.hasHttp = topLevelElement.getTextContent();
                     } else if(topLevelElement.getTagName().equals("hasOpenDap")) {
                         this.hasOpenDap = topLevelElement.getTextContent();
                     } else if(topLevelElement.getTagName().equals("hasSRM")) {
                         this.hasSRM = topLevelElement.getTextContent();
                     } else if(topLevelElement.getTagName().equals("technote")) {
                         this.technoteStr = topLevelElement.getTextContent();
                     } else if(topLevelElement.getTagName().equals("services")) {
                         this.servicesElement = new ServicesElement();
                         this.servicesElement.readHelper(topLevelElement);
                     } else if(topLevelElement.getTagName().equals("mimes")) {
                         this.mimesElement = new MIMESElement();
                         this.mimesElement.readHelper(topLevelElement);
                     } else if(topLevelElement.getTagName().equals("urls")) {
                         this.urlsElement = new URLSElement();
                         this.urlsElement.readHelper(topLevelElement);
                     }
 
                 }
             }
         }
 
         System.out.println(this.fileId);
         
     }
     
     
     
 
     /** Description of addMime()
      * 
      * @param mime
      */
     public void addMime(String mime) {
         if(mime != null) {
             mimesElement.addMime(mime);
         }
     }
     /** Description of removeMime()
      * 
      * @param mime
      */
     public void removeMime(String mime) {
         if(mime != null) {
             mimesElement.removeMime(mime);
         }
     }
     /** Description of addUrl()
      * 
      * @param url
      */
     public void addUrl(String url) {
         if(url != null) {
             urlsElement.addUrl(url);
         }
     }
     /** Description of removeUrl()
      * 
      * @param url
      */
     public void removeUrl(String url) {
         if(url != null) {
             urlsElement.removeUrl(url);
         }
     }
     
     
     
     /* Getters and setters for FileElement */
     
     public String getFileId() {
         return fileId;
     }
     public void setFileId(String fileId) {
         this.fileId = fileId;
     }
     public String getTitle() {
         return title;
     }
     public void setTitle(String title) {
         this.title = title;
     }
     public String getSize() {
         return size;
     }
     public void setSize(String size) {
         this.size = size;
     }
     public String getTracking_id() {
         return tracking_id;
     }
     public void setTracking_id(String tracking_id) {
         this.tracking_id = tracking_id;
     }
     public String getChecksum() {
         return checksum;
     }
     public void setChecksum(String checksum) {
         this.checksum = checksum;
     }
     public String getChecksum_type() {
         return checksum_type;
     }
     public void setChecksum_type(String checksum_type) {
         this.checksum_type = checksum_type;
     }
     public String getHasGrid() {
         return hasGrid;
     }
     public void setHasGrid(String hasGrid) {
         this.hasGrid = hasGrid;
     }
     public String getHasHttp() {
         return hasHttp;
     }
     public void setHasHttp(String hasHttp) {
         this.hasHttp = hasHttp;
     }
     public String getHasOpenDap() {
         return hasOpenDap;
     }
     public void setHasOpenDap(String hasOpenDap) {
         this.hasOpenDap = hasOpenDap;
     }
     public String getHasSRM() {
         return hasSRM;
     }
     public void setHasSRM(String hasSRM) {
         this.hasSRM = hasSRM;
     }
 
 
     public ServicesElement getServicesElement() {
         return servicesElement;
     }
     public void setServicesElement(ServicesElement servicesElement) {
         this.servicesElement = servicesElement;
     }
     public URLSElement getURLSElement() {
         return urlsElement;
     }
     public void setURLSElement(URLSElement urlsElement) {
         this.urlsElement = urlsElement;
     }
     public MIMESElement getMIMESElement() {
         return mimesElement;
     }
 
     public void setMIMESElement(MIMESElement mimesElement) {
         this.mimesElement = mimesElement;
     }
 
     /** Description of addService()
      * 
      * @param service
      */
     public void addService(String service) {
         if(service != null) {
             servicesElement.addService(service);
         }
     }
     /** Description of removeService()
      * 
      * @param service
      */
     public void removeService(String service) {
         if(service != null) {
             servicesElement.removeService(service);
         }
     }
 
     
     public static void main(String [] args) {
         
         
         FileElement u = new FileElement();
         
         u.fromFile(testInitializationFile);
         
         System.out.println(new XmlFormatter().format(u.toXML()));
         
         String xml = new XmlFormatter().format(u.toXML());
         
         FileElement uu = new FileElement();
         uu.fromXML(xml);
         
         System.out.println(new XmlFormatter().format(uu.toXML()));
         
         
         
         //u.toFile(testInitializationFile);
         
         //u = new URLSElement2();
         
         //u.fromFile(testInitializationFile);
 
         //System.out.println(u.toXML());
         /* */
         
     }
 
 
     public void setTechnoteStr(String technoteStr) {
         this.technoteStr = technoteStr;
     }
 
 
     public String getTechnoteStr() {
         return technoteStr;
     }
     
     
 }
