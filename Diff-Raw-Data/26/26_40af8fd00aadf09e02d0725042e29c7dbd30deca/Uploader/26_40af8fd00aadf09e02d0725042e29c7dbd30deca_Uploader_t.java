 /*
  * @(#)Uploader.java
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
 import java.util.*;
 import java.net.URL;
 import java.net.MalformedURLException;
 
 import org.apache.commons.httpclient.*;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.apache.commons.httpclient.cookie.*;
 import org.apache.commons.httpclient.methods.*;
 import org.apache.commons.httpclient.methods.multipart.*;
 import org.xml.sax.SAXException;
 import org.w3c.dom.Document;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 /**
  * A stateful class to handle uploads to Blip.
  *
  * @author Jared Klett
 * @version $Id: Uploader.java,v 1.10 2007/04/04 21:43:13 jklett Exp $
  */
 
 public class Uploader {
 
 // CVS info ///////////////////////////////////////////////////////////////////
 
    public static final String CVS_REV = "$Revision: 1.10 $";
 
 // Constants //////////////////////////////////////////////////////////////////
 
     /** The name of the cookie that contains the authcode. */
     public static final String AUTH_COOKIE_NAME = "otter_auth";
 
     /** TODO */
     public static final int ERROR_UNKNOWN = 10;
     public static final int ERROR_BAD_AUTH = 11;
     public static final int ERROR_SERVER = 12;
     /** Default timeout for an HTTP request: 30 seconds. */
     protected static final int TIMEOUT = 30000;
 
 // Instance variables /////////////////////////////////////////////////////////
 
     private Cookie authCookie;
     private String url;
     private String urlWithGuid;
     private String postURL;
     private String userAgent;
     private int timeout;
     private int errorCode;
 
 // Constructor ////////////////////////////////////////////////////////////////
 
     /**
      * Creates an uploader instance with the default timeout, a URL loaded from
      * the configuration file (bliplib.properties) and no auth cookie (be sure
      * to pass a username and password in your parameters object when
      * you call <code>uploadFile()</code>).
      */
     public Uploader() {
         this(TIMEOUT);
     }
 
     /**
      * Creates an uploader instance with the passed timeout, a URL loaded from
      * the configuration file (bliplib.properties) and no auth cookie (be sure
      * to pass a username and password in your parameters object when
      * you call <code>uploadFile()</code>).
      *
      * @param timeout A value, in milliseconds, after which the HTTP request should time out.
      */
     public Uploader(int timeout) {
         this(null, timeout, null);
     }
 
     /**
      * Creates an uploader instance with the the default timeout, a URL loaded from
      * the configuration file (bliplib.properties) and the passed <code>Cookie</code>
      * as the authentication cookie.
      *
      * @param authCookie The authentication cookie that will be set in the HTTP request.
      */
     public Uploader(Cookie authCookie) {
         this(null, TIMEOUT, authCookie);
     }
 
     /**
      * Creates an uploader instance with the the passed timeout, passed URL,
      * and the passed <code>Cookie</code> as the authentication cookie.
      *
      * @param url The URL that will be posted to.
      * @param timeout A value, in milliseconds, after which the HTTP request should time out.
      * @param authCookie The authentication cookie that will be set in the HTTP request.
      */
     public Uploader(String url, int timeout, Cookie authCookie) {
         String fullURL;
         if (url == null) {
             String baseURL = Parameters.config.getProperty(Parameters.BASE_URL, Parameters.BASE_URL_DEF);
             String uploadURI = Parameters.config.getProperty(Parameters.UPLOAD_URI, Parameters.UPLOAD_URI_DEF);
             fullURL = baseURL + uploadURI;
         } else {
             fullURL = url;
         }
         // check the URL and throw a runtime exception if we fail
         try {
             new URL(fullURL);
         } catch (MalformedURLException e) {
             throw new IllegalArgumentException("URL is invalid: " + fullURL);
         }
         // okay, on with the show...
         this.url = fullURL;
         this.timeout = timeout;
         this.authCookie = authCookie;
     }
 
 // Instance methods ///////////////////////////////////////////////////////////
 
     /**
      * Uploads the passed file with the passed parameters as the form data.
      *
      * @param videoFile The video file to be uploaded.
      * @param parameters A collection of key-value paired form data.
      * @return True on confirmation of a successful upload, false otherwise.
      * @throws FileNotFoundException If the passed file doesn't exist.
      * @throws HttpException If an error occurs while talking to the server.
      * @throws IOException If an error occurs while talking to the server.
      * @throws ParserConfigurationException If we can't create an XML parser.
      * @throws SAXException If an error occurs while parsing the XML response.
      */
     public boolean uploadFile(File videoFile, Properties parameters) throws FileNotFoundException, HttpException, IOException, ParserConfigurationException, SAXException {
         return uploadFile(videoFile, null, parameters);
     }
 
     /**
      * Uploads the passed file with the passed parameters as the form data.
      *
      * @param videoFile The video file to be uploaded.
      * @param thumbnailFile The image file to be used as a thumbnail.
      * @param parameters A collection of key-value paired form data.
      * @return True on confirmation of a successful upload, false otherwise.
      * @throws FileNotFoundException If the passed file doesn't exist.
      * @throws HttpException If an error occurs while talking to the server.
      * @throws IOException If an error occurs while talking to the server.
      * @throws ParserConfigurationException If we can't create an XML parser.
      * @throws SAXException If an error occurs while parsing the XML response.
      */
     public boolean uploadFile(File videoFile, File thumbnailFile, Properties parameters) throws FileNotFoundException, HttpException, IOException, ParserConfigurationException, SAXException {
         return uploadFile(videoFile, thumbnailFile, parameters, null);
     }
 
     /**
      * Uploads the passed file with the passed parameters as the form data.
      *
      * @param videoFile The video file to be uploaded.
      * @param thumbnailFile The image file to be used as a thumbnail.
      * @param parameters A collection of key-value paired form data.
      * @param crossposts A list of cross-post destinations.
      * @return True on confirmation of a successful upload, false otherwise.
      * @throws FileNotFoundException If the passed file doesn't exist.
      * @throws HttpException If an error occurs while talking to the server.
      * @throws IOException If an error occurs while talking to the server.
      * @throws ParserConfigurationException If we can't create an XML parser.
      * @throws SAXException If an error occurs while parsing the XML response.
      */
     public boolean uploadFile(File videoFile, File thumbnailFile, Properties parameters, List crossposts) throws FileNotFoundException, HttpException, IOException, ParserConfigurationException, SAXException {
        FilePartSource thumbnailFilePartSource = null;
        if (thumbnailFile != null)
            thumbnailFilePartSource = new FilePartSource(thumbnailFile);
        return uploadFile(new FilePartSource(videoFile), thumbnailFilePartSource, parameters, crossposts);
     }
 
    /**
     * Uploads the passed file with the passed parameters as the form data.
     *
     * @param videoFilePartSource The video file to be uploaded wrapped in a PartSource object.
     * @param thumbnailFilePartSource The image file to be used as a thumbnail wrapped in a PartSource object.
     * @param parameters A collection of key-value paired form data.
     * @param crossposts A list of cross-post destinations.
     * @return True on confirmation of a successful upload, false otherwise.
     * @throws FileNotFoundException If the passed file doesn't exist.
     * @throws HttpException If an error occurs while talking to the server.
     * @throws IOException If an error occurs while talking to the server.
     * @throws ParserConfigurationException If we can't create an XML parser.
     * @throws SAXException If an error occurs while parsing the XML response.
     */
     public boolean uploadFile(PartSource videoFilePartSource, PartSource thumbnailFilePartSource, Properties parameters, List crossposts) throws FileNotFoundException, HttpException, IOException, ParserConfigurationException, SAXException {
         if (urlWithGuid == null)
             throw new IllegalStateException("No GUID has been set");
         PostMethod post = new PostMethod(urlWithGuid);
         FilePart videoFilePart = new FilePart(Parameters.FILE_PARAM_KEY, videoFilePartSource);
         FilePart thumbnailFilePart = null;
         if (thumbnailFilePartSource != null)
             thumbnailFilePart = new FilePart(Parameters.THUMB_PARAM_KEY, thumbnailFilePartSource);
         Part[] parts = setRequestParts(videoFilePart, thumbnailFilePart, parameters, crossposts);
         post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));
         boolean succeeded = false;
         try {
             HttpClient client = new HttpClient();
             // Set a tolerant cookie policy
             client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
             if (userAgent != null)
                 client.getParams().setParameter(HttpMethodParams.USER_AGENT, userAgent);
             // Set our timeout
             client.getHttpConnectionManager().getParams().setConnectionTimeout(timeout);
             // If we had an auth cookie previously, set it in the client before
             // we send the request
             if (authCookie != null)
                 client.getState().addCookie(authCookie);
             // Send the post request
             int responseCode = client.executeMethod(post);
             // Check for an authorization cookie in the response
             if (authCookie == null) {
                 Cookie[] cookies = client.getState().getCookies();
                 for (int i = 0; i < cookies.length; i++) {
                     if (cookies[i].getName().equals(AUTH_COOKIE_NAME)) {
                         authCookie = cookies[i];
                         break;
                     }
                 }
             }
             // Check the HTTP response code
             succeeded = responseCode < 400;
             // Read the response
             InputStream responseStream = post.getResponseBodyAsStream();
             Document document = XmlUtils.loadDocumentFromInputStream(responseStream);
             if (responseCode == HttpStatus.SC_OK) {
                 if (document != null) {
                     // avoid NPE below
                     String responseText = "";
                     try {
                         // attempt to discern the status from the respose
                         responseText = document.getElementsByTagName("response").item(0).getFirstChild().getNodeValue();
                         String[] lines = responseText.trim().split("\n");
                         if (lines.length >= 2) {
                             URL testURL = new URL(lines[1]);
                             postURL = testURL.toString();
                         }
                     }
                     catch (Exception e) {
                         // we want to catch anything since we're making some assumptions above
                         // regarding indices and whatnot
                         System.out.println("Couldn't find valid URL in response text:\n" + responseText);
                         e.printStackTrace();
                     }
                     if (responseText.indexOf("couldn't find an account") != -1)
                         errorCode = ERROR_BAD_AUTH;
                     return responseText.indexOf("has been successfully posted") != -1;
 /*
 Other possible response strings:
                     "username and password combination";
                     "You must";
                     "critical error";
 */
                 }
             } else {
                 succeeded = false;
                 errorCode = ERROR_SERVER;
             }
         }
         finally {
             post.releaseConnection();
         }
         return succeeded;
     } // method uploadFile
 
     private Part[] setRequestParts(FilePart videoFilePart, FilePart thumbnailFilePart, Properties parameters, List crossposts) {
         Part[] typeArray = new Part[0];
         List list = new ArrayList();
         list.add(videoFilePart);
         if (thumbnailFilePart != null)
             list.add(thumbnailFilePart);
         list.add(Parameters.getStringPart(parameters, Parameters.TITLE_PARAM_KEY));
         list.add(Parameters.getStringPart(parameters, Parameters.POST_PARAM_KEY));
         list.add(Parameters.getStringPart(parameters, Parameters.CAT_PARAM_KEY));
         list.add(Parameters.getStringPart(parameters, Parameters.TAGS_PARAM_KEY));
         list.add(Parameters.getStringPart(parameters, Parameters.LICENSE_PARAM_KEY));
         list.add(Parameters.getStringPart(parameters, Parameters.SKIN_PARAM_KEY));
         list.add(Parameters.getStringPart(parameters, Parameters.DESC_PARAM_KEY));
         list.add(Parameters.getStringPart(parameters, Parameters.INGEST_PARAM_KEY));
         if (crossposts != null) {
             for (int i = 0; i < crossposts.size(); i++)
                 list.add(new StringPart(Parameters.CROSSPOST_PARAM_KEY, (String)crossposts.get(i)));
         }
         String ia = parameters.getProperty(Parameters.IA_PARAM_KEY);
         if (ia != null)
             list.add(new StringPart(Parameters.IA_PARAM_KEY, ia));
         // We want to omit the un/pw parts if we have an auth cookie
         if (authCookie == null) {
             // if the caller hasn't populated their parameters with a un/pw
             // an exception will be thrown from within the Parameters class
             list.add(Parameters.getStringPart(parameters, Parameters.USER_PARAM_KEY));
             list.add(Parameters.getStringPart(parameters, Parameters.PASS_PARAM_KEY));
         }
         return (Part[])list.toArray(typeArray);
     }
 
 // Accessors //////////////////////////////////////////////////////////////////
 
     /**
      * Upon upload failure, an error code will be recorded.
      * @return The error code.
      */
     public int getErrorCode() {
         return errorCode;
     }
 
     /**
      * Upon upload success, the XML response will be parsed for the URL to the
      * post on Blip.tv
      * @return The post URL.
      */
     public String getPostURL() {
         return postURL;
     }
 
     /**
      * Retrieves the currently set user-agent string that will be sent in the
      * HTTP request.
      * @return The user-agent string.
      */
     public String getUserAgent() {
         return userAgent;
     }
 
 // Mutators ///////////////////////////////////////////////////////////////////
 
     /**
      * Validates the passed GUID/UUID, and sets it to be used upon success.
      * @param guid The GUID/UUID to be used when uploading.
      */
     public void setGuid(String guid) {
         // let the UUID class validate it
         UUID uuid = UUID.fromString(guid);
         urlWithGuid = url + uuid.toString();
     }
 
     /**
      * Sets the authentication cookie to be used when uploading.
      * @param authCookie The cookie received from the <code>Authenticator</code> class.
      */
     public void setAuthCookie(Cookie authCookie) {
         this.authCookie = authCookie;
     }
 
     /**
      * Sets the user-agent string that will be sent in the HTTP request.
      * @param userAgent The new user-agent string to be used.
      */
     public void setUserAgent(String userAgent) {
         this.userAgent = userAgent;
     }
 
 // Main method ////////////////////////////////////////////////////////////////
 
     /**
      * Main method will run a bare-bones upload using the passed arguments.
      *
      * @param args Command-line arguments.
      */
     public static void main(String[] args) {
         if (args.length < 3) {
             System.out.println("Usage: java Uploader <file> <user> <pass>");
             System.out.println("Optional parameters: <title> <desc>");
             return;
         }
         try {
             File file = new File(args[0]);
             Properties props = new Properties();
             if (args.length > 4) {
                 props.put(Parameters.TITLE_PARAM_KEY, args[3]);
                 props.put(Parameters.DESC_PARAM_KEY, args[4]);
             }
 
             Cookie cookie = Authenticator.authenticate(args[1], args[2]);
             Uploader uploader = new Uploader(cookie);
             String guid = new RandomGUID().toString();
             uploader.setGuid(guid);
             uploader.uploadFile(file, props);
         }
         catch (Exception e) {
             e.printStackTrace();
         }
     }
 
 } // class Uploader
