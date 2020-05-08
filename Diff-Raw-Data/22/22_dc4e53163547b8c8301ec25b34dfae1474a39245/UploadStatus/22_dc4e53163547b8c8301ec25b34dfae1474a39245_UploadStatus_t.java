 /*
  * @(#)UploadStatus.java
  *
  * Copyright (c) 2006-2007 by Blip Networks, Inc.
  * 239 Centre St, 3rd Floor
  * New York, NY 10013
  * All rights reserved.
  *
  * This software is the confidential and
  * proprietary information of Blip Networks, Inc.
  */
 
 package com.blipnetworks.util;
 
 import java.io.*;
 import java.net.URL;
 import java.net.MalformedURLException;
 
 import org.w3c.dom.*;
 import org.xml.sax.SAXException;
 import org.apache.commons.httpclient.Cookie;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 /**
  * This class knows how to ask Otter about the status of an upload, and nothing more.
  * It's immutable and should stay that way.
  *
  * @author Jared Klett
 * @version $Id: UploadStatus.java,v 1.8 2007/05/18 19:34:16 jklett Exp $
  */
 
 public class UploadStatus {
 
 // CVS info ///////////////////////////////////////////////////////////////////
 
    public static final String CVS_REV = "$Revision: 1.8 $";
 
 // Constants //////////////////////////////////////////////////////////////////
 
     private static final String GUID_TAG = "guid";
     private static final String FILENAME_TAG = "filename";
     private static final String START_TAG = "start";
     private static final String UPDATE_TAG = "update";
     private static final String READ_TAG = "read";
     private static final String TOTAL_TAG = "total";
 
 // Instance variables /////////////////////////////////////////////////////////
 
     private Document document;
     private String guid;
     private String filename;
     private long start;
     private long update;
     private int read;
     private int total;
 
 // Constructor ////////////////////////////////////////////////////////////////
 
     private UploadStatus() {
         // will never be called outside
         // set default objects
         // TODO: break out
         setGuid("undefined");
         setFilename("unknown");
     }
 
 // Class methods //////////////////////////////////////////////////////////////
 
     /**
      * Hits the URL and attempts to read XML back from the response.
      *
      * @param guid The GUID for the upload.
      * @param authCookie The authentication cookie to be used in the request.
      * @return A new object containing the status data.
      * @throws IOException If an error occurs while talking to the server.
      * @throws ParserConfigurationException If we can't create an XML parser.
      * @throws SAXException If an error occurs while parsing the XML response.
      */
     public static UploadStatus getStatus(String guid, Cookie authCookie) throws IOException, ParserConfigurationException, SAXException {
         String baseURL = Parameters.config.getProperty(Parameters.BASE_URL, Parameters.BASE_URL_DEF);
         String statusURI = Parameters.config.getProperty(Parameters.STATUS_URI, Parameters.STATUS_URI_DEF);
         String url = baseURL + statusURI + guid;
         // check the URL and throw a runtime exception if we fail
         try {
             new URL(url);
         } catch (MalformedURLException e) {
             throw new IllegalArgumentException("URL must be valid: " + e.getMessage());
         }
         // check the cookie
         if (authCookie == null)
             throw new IllegalArgumentException("Cookie cannot be null");
         // okay, on with the show...
         UploadStatus status = new UploadStatus();
        Document document = XmlUtils.loadDocumentFromURL(url, authCookie);
         if (document != null) {
             status.setDocument(document);
             status.setGuid(findTag(document, GUID_TAG));
             status.setFilename(findTag(document, FILENAME_TAG));
             status.setStart(Integer.parseInt(findTag(document, START_TAG)));
             status.setUpdate(Integer.parseInt(findTag(document, UPDATE_TAG)));
             status.setRead(Integer.parseInt(findTag(document, READ_TAG)));
             status.setTotal(Integer.parseInt(findTag(document, TOTAL_TAG)));
         }
         return status;
     }
 
     private static String findTag(Document document, String tag) {
         NodeList nodelist = document.getElementsByTagName(tag);
         Node node = nodelist.item(0);
         if (node == null)
             throw new RuntimeException("Could not locate parent node for tag: " + tag);
         Node child = node.getFirstChild();
         if (child == null)
             throw new RuntimeException("Found node, but could not locate child node for tag: " + tag);
         return child.getNodeValue();
     }
 
 // Instance methods ///////////////////////////////////////////////////////////
 
     /**
      * Retrieves the GUID from the XML response.
      * @return The GUID parsed out of the XML.
      */
     public String getGuid() {
         return guid;
     }
 
     /**
      * Sets the GUID value in this object.
      * @param guid The new GUID value.
      */
     private void setGuid(String guid) {
         this.guid = guid;
     }
 
     /**
      * Retrieves the file name from the XML response.
      * @return The file name parsed out of the XML.
      */
     public String getFilename() {
         return filename;
     }
 
     /**
      * Sets the GUID value in this object.
      * @param filename The new GUID value.
      */
     private void setFilename(String filename) {
         this.filename = filename;
     }
 
     /**
      * Retrieves the start time of the upload from the XML response.
      * @return The start time of the upload parsed out of the XML.
      */
     public long getStart() {
         return start;
     }
 
     /**
      * Sets the start time of the upload in this object.
      * @param start The new start time of the upload.
      */
     private void setStart(long start) {
         this.start = start;
     }
 
     /**
      * Retrieves the last update time from the XML response.
      * @return The last update time parsed out of the XML.
      */
     public long getUpdate() {
         return update;
     }
 
     /**
      * Sets the last update time in this object.
      * @param update The new last update time.
      */
     private void setUpdate(long update) {
         this.update = update;
     }
 
     /**
      * Retrieves the number of bytes read so far from the XML response.
      * @return The number of bytes read so far parsed out of the XML.
      */
     public int getRead() {
         return read;
     }
 
     /**
      * Sets the number of bytes read so far in this object.
      * @param read The number of bytes read so far parsed out of the XML.
      */
     private void setRead(int read) {
         this.read = read;
     }
 
     /**
      * Retrieves the total number of bytes from the XML response.
      * @return The total number of bytes parsed out of the XML.
      */
     public int getTotal() {
         return total;
     }
 
     /**
      * Sets the total number of bytes in this object.
      * @param total The total number of bytes.
      */
     private void setTotal(int total) {
         this.total = total;
     }
 
     /**
      * Retrieves the full XML document that was last read.
      * @return The XML document.
      */
     public Document getDocument() {
         return document;
     }
 
     /**
      * Sets the full XML document that was last read in this object.
      * @param document The XML document.
      */
     public void setDocument(Document document) {
         this.document = document;
     }
 
 } // class UploadStatus
