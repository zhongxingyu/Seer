 /*
  * Copyright (c) 2005 Aetrion LLC.
  */
 package com.aetrion.flickr;
 
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Collections;
 import java.util.List;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import com.aetrion.flickr.util.IOUtilities;
 import com.aetrion.flickr.util.DebugInputStream;
 import com.aetrion.flickr.util.UrlUtilities;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 /**
  * Transport implementation using the REST interface.
  *
  * @author Anthony Eden
  */
 public class REST extends Transport {
 
     private Class responseClass = RESTResponse.class;
 
     private DocumentBuilder builder;
 
     /**
      * Construct a new REST transport instance.
      *
      * @throws ParserConfigurationException
      */
     public REST() throws ParserConfigurationException {
         DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
         builder = builderFactory.newDocumentBuilder();
     }
 
     /**
      * Construct a new REST transport instance using the specified host endpoint.
      *
      * @param host The host endpoint
      * @throws ParserConfigurationException
      */
     public REST(String host) throws ParserConfigurationException {
         this();
         setHost(host);
     }
 
     /**
      * Construct a new REST transport instance using the specified host and port endpoint.
      *
      * @param host The host endpoint
      * @param port The port
      * @throws ParserConfigurationException
      */
     public REST(String host, int port) throws ParserConfigurationException {
         this();
         setHost(host);
         setPort(port);
     }
 
     /**
      * Get the response Class. By default the RESTResponse class is used.
      *
      * @return The response Class
      */
     public Class getResponseClass() {
         return responseClass;
     }
 
     /**
      * Set the response Class.
      *
      * @param responseClass The response Class
      */
     public void setResponseClass(Class responseClass) {
         if (responseClass == null) {
             throw new IllegalArgumentException("The response Class cannot be null");
         }
         this.responseClass = responseClass;
     }
     
     /**
      * Invoke an HTTP GET request on a remote host.  You must close the InputStream after you are done with.
      *
      * @param path The request path
      * @param parameters The parameters (collection of Parameter objects)
      * @return The Response
      * @throws IOException
      * @throws SAXException
      */
     public Response get(String path, List parameters) throws IOException, SAXException {
         URL url = UrlUtilities.buildUrl(getHost(), getPort(), path, parameters);
         System.out.println("GET: " + url);
         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
         conn.setRequestMethod("GET");
         conn.connect();
 
         InputStream in = null;
         try {
             if (Flickr.debugStream) {
                 in = new DebugInputStream(conn.getInputStream(), System.out);
             } else {
                 in = conn.getInputStream();
             }
 
             Document document = builder.parse(in);
             Response response = (Response) responseClass.newInstance();
             response.parse(document);
             return response;
         } catch (IllegalAccessException e) {
             throw new RuntimeException(e); // TODO: Replace with a better exception
         } catch (InstantiationException e) {
             throw new RuntimeException(e); // TODO: Replace with a better exception
         } finally {
             IOUtilities.close(in);
         }
     }
 
     /**
      * Invoke an HTTP POST request on a remote host.
      *
      * @param path The request path
      * @param parameters The parameters (collection of Parameter objects)
      * @param multipart Use multipart
      * @return The Response object
      * @throws IOException
      * @throws SAXException
      */
     public Response post(String path, Collection parameters, boolean multipart) throws IOException, SAXException {
         URL url = UrlUtilities.buildUrl(getHost(), getPort(), path, Collections.EMPTY_LIST);
 
         HttpURLConnection conn = null;
         try {
             String boundary = "---------------------------7d273f7a0d3";
 
             conn = (HttpURLConnection) url.openConnection();
 
             conn.setDoOutput(true);
             conn.setRequestMethod("POST");
             if (multipart) {
                 conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
             }
             conn.connect();
 
             DataOutputStream out = null;
             try {
                 out = new DataOutputStream(conn.getOutputStream());
 
                 // construct the body
                 if (multipart) {
                     out.writeBytes("--" + boundary + "\r\n");
                     Iterator iter = parameters.iterator();
                     while (iter.hasNext()) {
                         Parameter p = (Parameter) iter.next();
                         writeParam(p.getName(), p.getValue(), out, boundary);
                     }
                 } else {
                     Iterator iter = parameters.iterator();
                     while (iter.hasNext()) {
                         Parameter p = (Parameter) iter.next();
                         out.writeBytes(p.getName());
                         out.writeBytes("=");
                         out.writeBytes(String.valueOf(p.getValue()));
                         if (iter.hasNext()) out.writeBytes("&");
                     }
                 }
                 out.flush();
             } finally {
                 IOUtilities.close(out);
             }
 
             InputStream in = null;
             try {
                 if (Flickr.debugStream) {
                     in = new DebugInputStream(conn.getInputStream(), System.out);
                 } else {
                     in = conn.getInputStream();
                 }
                 Document document = builder.parse(in);
                 Response response = (Response) responseClass.newInstance();
                 response.parse(document);
                 return response;
             } catch (IllegalAccessException e) {
                 throw new RuntimeException(e); // TODO: Replace with a better exception
             } catch (InstantiationException e) {
                 throw new RuntimeException(e); // TODO: Replace with a better exception
             } finally {
                 IOUtilities.close(in);
             }
         } finally {
             if (conn != null) {
                 conn.disconnect();
             }
         }
     }
 
     private void writeParam(String name, Object value, DataOutputStream out, String boundary)
             throws IOException {
         if (value instanceof byte[]) {
             out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"image.jpg\";\r\n");
             out.writeBytes("Content-Type: image/jpeg" + "\r\n\r\n");
             out.write((byte[]) value);
             out.writeBytes("\r\n" + "--" + boundary + "\r\n");
         } else {
             out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
             out.writeBytes(String.valueOf(value));
             out.writeBytes("\r\n" + "--" + boundary + "\r\n");
         }
     }
 
 }
