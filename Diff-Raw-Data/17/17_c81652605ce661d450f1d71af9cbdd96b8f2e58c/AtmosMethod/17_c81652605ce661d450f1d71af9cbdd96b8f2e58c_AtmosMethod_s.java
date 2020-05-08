 /**
  * ========= CONFIDENTIAL =========
  *
  * Copyright (C) 2012 enStratus Networks Inc - ALL RIGHTS RESERVED
  *
  * ====================================================================
  *  NOTICE: All information contained herein is, and remains the
  *  property of enStratus Networks Inc. The intellectual and technical
  *  concepts contained herein are proprietary to enStratus Networks Inc
  *  and may be covered by U.S. and Foreign Patents, patents in process,
  *  and are protected by trade secret or copyright law. Dissemination
  *  of this information or reproduction of this material is strictly
  *  forbidden unless prior written permission is obtained from
  *  enStratus Networks Inc.
  * ====================================================================
  */
 package org.dasein.cloud.atmos;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpVersion;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.conn.params.ConnRoutePNames;
 import org.apache.http.entity.ContentType;
 import org.apache.http.entity.InputStreamEntity;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.apache.log4j.Logger;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 import org.dasein.cloud.ProviderContext;
 import org.dasein.cloud.storage.Blob;
 import org.dasein.util.uom.storage.Gigabyte;
 import org.dasein.util.uom.storage.Storage;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.crypto.Mac;
 import javax.crypto.spec.SecretKeySpec;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Properties;
 import java.util.TreeSet;
 
 /**
  * Handles all RESTful interaction with the Atmos endpoint in order to perform specific REST operations.
  * <p>Created by George Reese: 10/5/12 9:14 AM</p>
  * @author George Reese
  * @version 2012.09 initial version
  * @since 2012.09
  */
 public class AtmosMethod {
     public enum EndpointType {
         OBJECT, NAMESPACE;
 
         public String toEndpoint() {
             return (equals(EndpointType.NAMESPACE) ? "rest/namespace" : "rest/objects");
         }
     }
 
     static private final Logger logger = Atmos.getLogger(AtmosMethod.class);
     static private final Logger wire = Atmos.getWireLogger(AtmosMethod.class);
 
     private Atmos provider;
 
     public AtmosMethod(Atmos provider) { this.provider = provider; }
 
     protected void authorize(@Nonnull ProviderContext ctx, @Nonnull HttpRequestBase method, @Nonnull String contentType, @Nullable String range) throws CloudException, InternalException {
         ArrayList<Header> emcHeaders = new ArrayList<Header>();
         String date = provider.formatTime(System.currentTimeMillis());
 
         try {
             method.addHeader("x-emc-uid", new String(ctx.getAccessPublic(), "utf-8") + "/" + ctx.getAccountNumber());
         }
         catch( UnsupportedEncodingException e ) {
             logger.error("UTF-8 error: " + e.getMessage());
             throw new InternalException(e);
         }
         method.addHeader("Date", date);
 
         for( Header h : method.getAllHeaders() ) {
             if( h.getName().toLowerCase().startsWith("x-emc") ) {
                 emcHeaders.add(h);
             }
         }
         String signatureString = toSignatureString(method, contentType, range == null ? "" : range, date, method.getURI(), emcHeaders);
         logger.debug(signatureString);
         String signature = sign(ctx, signatureString);
         logger.debug(signature);
         method.addHeader("x-emc-signature", signature);
     }
 
     public @Nonnull Blob create(@Nonnull String bucket, @Nonnull String name) throws CloudException, InternalException {
         if( logger.isTraceEnabled() ) {
             logger.trace("ENTER - " + AtmosMethod.class.getName() + ".create(" + bucket + "," + name + ")");
         }
         if( wire.isDebugEnabled() ) {
             wire.debug("");
             wire.debug(">>> [POST/create directory] -> " + bucket + " / " + name + "--------------------------------------------------------------------------------------");
         }
         try {
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 throw new CloudException("No context was set for this request");
             }
             if( !bucket.endsWith("/") ) {
                 bucket = bucket + "/";
             }
             if( !bucket.startsWith("/") ) {
                 bucket = "/" + bucket;
             }
             if( !name.endsWith("/") ) {
                 name = name + "/";
             }
             String endpoint = getEndpoint(ctx, EndpointType.NAMESPACE, bucket + name);
             HttpPost post = new HttpPost(endpoint);
             HttpClient client = getClient(endpoint);
 
             post.addHeader("Accept", "*/*");
             post.addHeader("Content-Type", "application/octet-stream");
             authorize(ctx, post, "application/octet-stream", null);
             if( wire.isDebugEnabled() ) {
                 wire.debug(post.getRequestLine().toString());
                 for( Header header : post.getAllHeaders() ) {
                     wire.debug(header.getName() + ": " + header.getValue());
                 }
                 wire.debug("");
             }
             HttpResponse response;
 
             try {
                 response = client.execute(post);
                 if( wire.isDebugEnabled() ) {
                     wire.debug(response.getStatusLine().toString());
                 }
             }
             catch( IOException e ) {
                 logger.error("I/O error from server communications: " + e.getMessage());
                 e.printStackTrace();
                 throw new InternalException(e);
             }
             int status = response.getStatusLine().getStatusCode();
 
             if( status == HttpServletResponse.SC_CREATED ) {
                 if( !bucket.equals("/") ) {
                     name = bucket + name;
                 }
                 return toBlob(ctx, response, name, null, null);
             }
             else {
                 throw new AtmosException(response);
             }
         }
         finally {
             if( logger.isTraceEnabled() ) {
                 logger.trace("EXIT - " + AtmosMethod.class.getName() + ".create()");
             }
             if( wire.isDebugEnabled() ) {
                 wire.debug("<<< [POST/create directory] -> " + bucket + " / " + name + "--------------------------------------------------------------------------------------");
                 wire.debug("");
             }
         }
     }
 
     public void delete(@Nonnull String bucketName, @Nullable String objectName) throws CloudException, InternalException {
         if( logger.isTraceEnabled() ) {
             logger.trace("ENTER - " + AtmosMethod.class.getName() + ".delete(" + bucketName + ")");
         }
         if( wire.isDebugEnabled() ) {
             wire.debug("");
             wire.debug(">>> [DELETE] -> " + bucketName + "--------------------------------------------------------------------------------------");
         }
         try {
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 throw new CloudException("No context was set for this request");
             }
             if( !bucketName.endsWith("/") ) {
                 bucketName = bucketName + "/";
             }
             if( !bucketName.startsWith("/") ) {
                 bucketName = "/" + bucketName;
             }
             if( objectName != null ) {
                 while( objectName.startsWith("/") && !objectName.equals("/") ) {
                     objectName = objectName.substring(1);
                 }
                 if( !objectName.equals("/") ) {
                     bucketName = bucketName + objectName;
                 }
             }
             String endpoint = getEndpoint(ctx, EndpointType.NAMESPACE, bucketName);
             HttpDelete delete = new HttpDelete(endpoint);
             HttpClient client = getClient(endpoint);
 
             delete.addHeader("Accept", "*/*");
             delete.addHeader("Content-Type", "application/octet-stream");
             authorize(ctx, delete, "application/octet-stream", null);
             if( wire.isDebugEnabled() ) {
                 wire.debug(delete.getRequestLine().toString());
                 for( Header header : delete.getAllHeaders() ) {
                     wire.debug(header.getName() + ": " + header.getValue());
                 }
                 wire.debug("");
             }
             HttpResponse response;
 
             try {
                 response = client.execute(delete);
                 if( wire.isDebugEnabled() ) {
                     wire.debug(response.getStatusLine().toString());
                 }
             }
             catch( IOException e ) {
                 logger.error("I/O error from server communications: " + e.getMessage());
                 e.printStackTrace();
                 throw new InternalException(e);
             }
             int status = response.getStatusLine().getStatusCode();
 
             if( status != HttpServletResponse.SC_NO_CONTENT ) {
                 throw new AtmosException(response);
             }
         }
         finally {
             if( logger.isTraceEnabled() ) {
                 logger.trace("EXIT - " + AtmosMethod.class.getName() + ".delete()");
             }
             if( wire.isDebugEnabled() ) {
                 wire.debug("<<< [DELETE] -> " + bucketName + "--------------------------------------------------------------------------------------");
                 wire.debug("");
             }
         }
     }
 
     public @Nonnull InputStream download(@Nonnull String bucket, @Nonnull String name) throws CloudException, InternalException {
         if( logger.isTraceEnabled() ) {
             logger.trace("ENTER - " + AtmosMethod.class.getName() + ".download(" + bucket + "," + name + ")");
         }
         if( wire.isDebugEnabled() ) {
             wire.debug("");
             wire.debug(">>> [GET/download] -> " + bucket + " / " + name + "--------------------------------------------------------------------------------------");
         }
         try {
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 throw new CloudException("No context was set for this request");
             }
             if( !bucket.endsWith("/") ) {
                 bucket = bucket + "/";
             }
             if( !bucket.startsWith("/") ) {
                 bucket = "/" + bucket;
             }
             if( !name.endsWith("/") ) {
                 name = name + "/";
             }
             String endpoint = getEndpoint(ctx, EndpointType.NAMESPACE, bucket + name);
             HttpGet get = new HttpGet(endpoint);
             HttpClient client = getClient(endpoint);
 
             get.addHeader("Accept", "*/*");
             authorize(ctx, get, "", null);
             if( wire.isDebugEnabled() ) {
                 wire.debug(get.getRequestLine().toString());
                 for( Header header : get.getAllHeaders() ) {
                     wire.debug(header.getName() + ": " + header.getValue());
                 }
                 wire.debug("");
             }
             HttpResponse response;
 
             try {
                 response = client.execute(get);
                 if( wire.isDebugEnabled() ) {
                     wire.debug(response.getStatusLine().toString());
                 }
             }
             catch( IOException e ) {
                 logger.error("I/O error from server communications: " + e.getMessage());
                 e.printStackTrace();
                 throw new InternalException(e);
             }
             int status = response.getStatusLine().getStatusCode();
 
             if( status == HttpServletResponse.SC_OK ) {
                 HttpEntity entity = response.getEntity();
 
                 if( entity == null ) {
                     throw new CloudException("No content was returned");
                 }
                 if( wire.isDebugEnabled() ) {
                     wire.debug("[CONTENT:" + entity.getContentType() + " - " + entity.getContentLength() + "]");
                 }
                 try {
                     return entity.getContent();
                 }
                 catch( IOException e ) {
                     logger.error("I/O error from server communications: " + e.getMessage());
                     e.printStackTrace();
                     throw new InternalException(e);
                 }
             }
             else {
                 throw new AtmosException(response);
             }
         }
         finally {
             if( logger.isTraceEnabled() ) {
                 logger.trace("EXIT - " + AtmosMethod.class.getName() + ".download()");
             }
             if( wire.isDebugEnabled() ) {
                 wire.debug("<<< [GET/download] -> " + bucket + " / " + name + "--------------------------------------------------------------------------------------");
                 wire.debug("");
             }
         }
     }
 
     protected @Nonnull HttpClient getClient(String endpoint) throws CloudException, InternalException {
         ProviderContext ctx = provider.getContext();
 
         if( ctx == null ) {
             throw new CloudException("No context was specified for this request");
         }
         boolean ssl = endpoint.startsWith("https");
         HttpParams params = new BasicHttpParams();
 
         HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
         HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
         HttpProtocolParams.setUserAgent(params, "Dasein Cloud");
 
         Properties p = ctx.getCustomProperties();
 
         if( p != null ) {
             String proxyHost = p.getProperty("proxyHost");
             String proxyPort = p.getProperty("proxyPort");
 
             if( proxyHost != null ) {
                 int port = 0;
 
                 if( proxyPort != null && proxyPort.length() > 0 ) {
                     port = Integer.parseInt(proxyPort);
                 }
                 params.setParameter(ConnRoutePNames.DEFAULT_PROXY, new HttpHost(proxyHost, port, ssl ? "https" : "http"));
             }
         }
         return new DefaultHttpClient(params);
     }
 
     protected @Nonnull String getEndpoint(@Nonnull ProviderContext ctx, @Nonnull EndpointType type, @Nullable String target) throws CloudException, InternalException {
         StringBuilder url = new StringBuilder();
         String endpoint = ctx.getEndpoint();
 
         if( endpoint == null ) {
             throw new CloudException("No endpoint was set for this request");
         }
         url.append(endpoint);
         url.append(type.toEndpoint());
 
         if( target != null && !target.equals("/") ) {
             if( !target.startsWith("/") ) {
                 target = "/" + target;
             }
             url.append(target);
         }
         return url.toString();
     }
 
     public @Nullable Blob info(@Nonnull String bucket, @Nonnull String name) throws CloudException, InternalException {
         if( logger.isTraceEnabled() ) {
             logger.trace("ENTER - " + AtmosMethod.class.getName() + ".info(" + bucket + "," + name + ")");
         }
         if( wire.isDebugEnabled() ) {
             wire.debug("");
             wire.debug(">>> [GET/info] -> " + bucket + " / " + name + "--------------------------------------------------------------------------------------");
         }
         try {
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 throw new CloudException("No context was set for this request");
             }
             String regionId = ctx.getRegionId();
 
             if( regionId == null ) {
                 throw new CloudException("No region was set for this request");
             }
             String target;
 
             if( !bucket.endsWith("/") ) {
                 bucket = bucket + "/";
             }
             if( !bucket.startsWith("/") ) {
                 bucket = "/" + bucket;
             }
             target = bucket;
             while( name.startsWith("/") && !name.equals("/") ) {
                 name = name.substring(1);
             }
             if( !name.equals("/") ) {
                 target = bucket + name;
             }
             String endpoint = getEndpoint(ctx, EndpointType.NAMESPACE, target);
             HttpGet get = new HttpGet(endpoint + "?metadata/system");
             HttpClient client = getClient(endpoint);
 
             get.addHeader("Accept", "*/*");
             authorize(ctx, get, "", null);
             if( wire.isDebugEnabled() ) {
                 wire.debug(get.getRequestLine().toString());
                 for( Header header : get.getAllHeaders() ) {
                     wire.debug(header.getName() + ": " + header.getValue());
                 }
                 wire.debug("");
             }
             HttpResponse response;
 
             try {
                 response = client.execute(get);
                 if( wire.isDebugEnabled() ) {
                     wire.debug(response.getStatusLine().toString());
                 }
             }
             catch( IOException e ) {
                 logger.error("I/O error from server communications: " + e.getMessage());
                 e.printStackTrace();
                 throw new InternalException(e);
             }
             int status = response.getStatusLine().getStatusCode();
 
             if( status == HttpServletResponse.SC_NOT_FOUND ) {
                 return null;
             }
             if( status == HttpServletResponse.SC_OK ) {
                 HttpEntity entity = response.getEntity();
 
                 if( entity == null ) {
                     return null;
                 }
                 Header header = response.getFirstHeader("x-emc-meta");
                 Properties p = toProperties(header);
 
                 String objectName = p.getProperty("objname");
                 String objectId = p.getProperty("objectid");
                 String ctime = p.getProperty("ctime");
                 String size = p.getProperty("size");
 
                 if( objectName == null || objectId == null ) {
                     return null;
                 }
                 if( size == null ) {
                     size = "0";
                 }
                 if( ctime == null ) {
                     ctime = "0";
                 }
                 Storage<org.dasein.util.uom.storage.Byte> s = new Storage<org.dasein.util.uom.storage.Byte>(Long.parseLong(size), Storage.BYTE);
 
                 return Blob.getInstance(regionId, "/rest/objects/" + objectId, bucket, objectName, provider.parseTime(ctime), s);
             }
             else {
                 throw new AtmosException(response);
             }
         }
         finally {
             if( logger.isTraceEnabled() ) {
                 logger.trace("EXIT - " + AtmosMethod.class.getName() + ".download()");
             }
             if( wire.isDebugEnabled() ) {
                 wire.debug("<<< [GET/info] -> " + bucket + " / " + name + "--------------------------------------------------------------------------------------");
                 wire.debug("");
             }
         }
     }
 
     public Iterable<Blob> list(@Nonnull String directory) throws CloudException, InternalException {
         if( logger.isTraceEnabled() ) {
             logger.trace("ENTER - " + AtmosMethod.class.getName() + ".list(" + directory + ")");
         }
         if( wire.isDebugEnabled() ) {
             wire.debug("");
             wire.debug(">>> [GET] -> " + directory + "--------------------------------------------------------------------------------------");
         }
         try {
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 throw new CloudException("No context was set for this request");
             }
             if( !directory.startsWith("/") ) {
                 directory = "/" + directory;
             }
             String endpoint = getEndpoint(ctx, EndpointType.NAMESPACE, directory);
             HttpGet get = new HttpGet(endpoint);
             HttpClient client = getClient(endpoint);
 
             get.addHeader("x-emc-include-meta", "true");
             get.addHeader("Accept", "text/xml");
             authorize(ctx, get, "", null);
             if( wire.isDebugEnabled() ) {
                 wire.debug(get.getRequestLine().toString());
                 for( Header header : get.getAllHeaders() ) {
                     wire.debug(header.getName() + ": " + header.getValue());
                 }
                 wire.debug("");
             }
             HttpResponse response;
 
             try {
                 response = client.execute(get);
                 if( wire.isDebugEnabled() ) {
                     wire.debug(response.getStatusLine().toString());
                 }
             }
             catch( IOException e ) {
                 logger.error("I/O error from server communications: " + e.getMessage());
                 e.printStackTrace();
                 throw new InternalException(e);
             }
             int status = response.getStatusLine().getStatusCode();
 
             if( status == HttpServletResponse.SC_OK ) {
                 HttpEntity entity = response.getEntity();
 
                 if( entity == null ) {
                     return Collections.emptyList();
                 }
                 try {
                     ArrayList<Blob> entries = new ArrayList<Blob>();
                     String xml = EntityUtils.toString(entity);
 
                     if( wire.isDebugEnabled() ) {
                          wire.debug(xml);
                     }
                     ByteArrayInputStream bas = new ByteArrayInputStream(xml.getBytes());
 
                     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                     DocumentBuilder parser = factory.newDocumentBuilder();
                     Document doc = parser.parse(bas);
 
                     NodeList blocks = doc.getElementsByTagName("DirectoryEntry");
 
                     for( int i=0; i<blocks.getLength(); i++ ) {
                         Node entry = blocks.item(i);
 
                         Blob blob = toBlob(ctx, entry, directory);
 
                         if( blob != null ) {
                             entries.add(blob);
                         }
                     }
                     return entries;
                 }
                 catch( IOException e ) {
                     logger.error("I/O error reading from the cloud: " + e.getMessage());
                     e.printStackTrace();
                     throw new CloudException("Error reading response from cloud");
                 }
                 catch( ParserConfigurationException e ) {
                     logger.error("Error with internal XML parser: " + e.getMessage());
                     e.printStackTrace();
                     throw new InternalException("Error with internal XML parser");
                 }
                 catch( SAXException e ) {
                     logger.error("Invalid XML from the cloud: " + e.getMessage());
                     e.printStackTrace();
                     throw new CloudException("Invalid XML from cloud");
                 }
             }
             else {
                 throw new AtmosException(response);
             }
         }
         finally {
             if( logger.isTraceEnabled() ) {
                 logger.trace("EXIT - " + AtmosMethod.class.getName() + ".list()");
             }
             if( wire.isDebugEnabled() ) {
                 wire.debug("<<< [GET] " + directory + "--------------------------------------------------------------------------------------");
                 wire.debug("");
             }
         }
 
     }
 
     public void rename(@Nonnull String root, @Nonnull String oldName, @Nonnull String newName) throws CloudException, InternalException {
         if( logger.isTraceEnabled() ) {
             logger.trace("ENTER - " + AtmosMethod.class.getName() + ".rename(" + root + "," + oldName + "," + newName + ")");
         }
         if( wire.isDebugEnabled() ) {
             wire.debug("");
             wire.debug(">>> [POST/rename] -> " + root + " / " + oldName + " / " + newName + "--------------------------------------------------------------------------------------");
         }
         try {
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 throw new CloudException("No context was set for this request");
             }
             if( !root.endsWith("/") ) {
                 root = root + "/";
             }
             if( !root.startsWith("/") ) {
                 root = "/" + root;
             }
             if( !oldName.endsWith("/") ) {
                 oldName = oldName + "/";
             }
             if( !newName.endsWith("/") ) {
                 newName = newName + "/";
             }
             String endpoint = getEndpoint(ctx, EndpointType.NAMESPACE, root + oldName);
             HttpPost post = new HttpPost(endpoint + "?rename");
             HttpClient client = getClient(endpoint);
 
             post.addHeader("Accept", "*/*");
             post.addHeader("Content-Type", "application/octet-stream");
             post.addHeader("x-emc-path", root + "/" + newName);
             authorize(ctx, post, "application/octet-stream", null);
             if( wire.isDebugEnabled() ) {
                 wire.debug(post.getRequestLine().toString());
                 for( Header header : post.getAllHeaders() ) {
                     wire.debug(header.getName() + ": " + header.getValue());
                 }
                 wire.debug("");
             }
             HttpResponse response;
 
             try {
                 response = client.execute(post);
                 if( wire.isDebugEnabled() ) {
                     wire.debug(response.getStatusLine().toString());
                 }
             }
             catch( IOException e ) {
                 logger.error("I/O error from server communications: " + e.getMessage());
                 e.printStackTrace();
                 throw new InternalException(e);
             }
             int status = response.getStatusLine().getStatusCode();
 
             if( status != HttpServletResponse.SC_NO_CONTENT ) {
                 throw new AtmosException(response);
             }
         }
         finally {
             if( logger.isTraceEnabled() ) {
                 logger.trace("EXIT - " + AtmosMethod.class.getName() + ".rename()");
             }
             if( wire.isDebugEnabled() ) {
                 wire.debug("<<< [POST/rename] -> " +  root + " / " + oldName + " / " + newName  + "--------------------------------------------------------------------------------------");
                 wire.debug("");
             }
         }
     }
 
     private @Nonnull String sign(@Nonnull ProviderContext ctx, @Nonnull String stringToSign) throws InternalException {
         try {
             Mac mac = Mac.getInstance("HmacSHA1");
 
             mac.init(new SecretKeySpec(Base64.decodeBase64(new String(ctx.getAccessPrivate(), "utf-8")), "HmacSHA1"));
             return new String(Base64.encodeBase64(mac.doFinal(stringToSign.getBytes("UTF-8"))), "utf-8");
         }
         catch( NoSuchAlgorithmException e ) {
             logger.error(e);
             e.printStackTrace();
             throw new InternalException(e);
         }
         catch( InvalidKeyException e ) {
             logger.error(e);
             e.printStackTrace();
             throw new InternalException(e);
         }
         catch( IllegalStateException e ) {
             logger.error(e);
             e.printStackTrace();
             throw new InternalException(e);
         }
         catch( UnsupportedEncodingException e ) {
             logger.error(e);
             e.printStackTrace();
             throw new InternalException(e);
         }
     }
 
     private @Nonnull Blob toBlob(@Nonnull ProviderContext ctx, @Nonnull Node node, @Nonnull String directory) throws CloudException, InternalException {
         String regionId = ctx.getRegionId();
 
         if( regionId == null ) {
             throw new CloudException("No region was specified for this request");
         }
         String objectId = null, objectName = null;
         NodeList attrs = node.getChildNodes();
         boolean bucket = false;
         Storage<?> size = null;
         long created = 0L;
 
         for( int i=0; i<attrs.getLength(); i++ ) {
             Node attr = attrs.item(i);
 
             if( attr.getNodeName().equalsIgnoreCase("objectid") && attr.hasChildNodes() ) {
                 objectId = attr.getFirstChild().getNodeValue().trim();
             }
             else if( attr.getNodeName().equalsIgnoreCase("filename") && attr.hasChildNodes() ) {
                 objectName = attr.getFirstChild().getNodeValue().trim();
             }
             else if( attr.getNodeName().equalsIgnoreCase("filetype") && attr.hasChildNodes() ) {
                 bucket = attr.getFirstChild().getNodeValue().trim().equalsIgnoreCase("directory");
             }
             else if( (attr.getNodeName().equalsIgnoreCase("systemmetadatalist") || attr.getNodeName().equalsIgnoreCase("usermetadatalist")) && attr.hasChildNodes() ) {
                 NodeList metas = attr.getChildNodes();
 
                 for( int j=0; j<metas.getLength(); j++ ) {
                     Node meta = metas.item(j);
 
                     if( meta.getNodeName().equalsIgnoreCase("metadata") && meta.hasChildNodes() ) {
                         NodeList childNodes = meta.getChildNodes();
                         String name = null, value = null;
 
                         for( int k=0; k<childNodes.getLength(); k++ ) {
                             Node child = childNodes.item(k);
 
                             if( child.getNodeName().equalsIgnoreCase("name") && child.hasChildNodes() ) {
                                 name = child.getFirstChild().getNodeValue().trim();
                             }
                             else if( child.getNodeName().equalsIgnoreCase("value") && child.hasChildNodes() ) {
                                 value = child.getFirstChild().getNodeValue().trim();
                             }
                         }
                         if( name != null && value != null ) {
                             if( name.equalsIgnoreCase("itime") ) {
                                 created = provider.parseTime(value);
                             }
                             else if( name.equalsIgnoreCase("size") ) {
                                 size = new Storage<org.dasein.util.uom.storage.Byte>(Long.parseLong(value), Storage.BYTE);
                             }
                         }
                     }
                 }
             }
         }
         if( objectId == null ) {
             return null;
         }
         while( !directory.equals("/") && directory.endsWith("/") ) {
             directory = directory.substring(0, directory.length()-1);
         }
         if( directory.equals("/") ) {
             directory = null;
         }
         if( bucket ) {
             return Blob.getInstance(regionId, "/rest/objects/" + objectId, (directory == null ? objectName : (directory + "/" + objectName)), created);
         }
         else {
             if( size == null ) {
                 size = new Storage<Gigabyte>(0, Storage.GIGABYTE);
             }
             return Blob.getInstance(regionId, "/rest/objects/" + objectId, directory, objectName, created, size);
         }
     }
 
     private @Nonnull Blob toBlob(@Nonnull ProviderContext ctx, @Nonnull HttpResponse response, @Nonnull String bucketName, @Nullable String objectName, @Nullable Storage<?> size) throws CloudException, InternalException {
         String regionId = ctx.getRegionId();
 
         if( regionId == null ) {
             throw new CloudException("No region was set for this request");
         }
         Header location = response.getFirstHeader("location");
         String value = (location == null ? null : location.getValue());
 
         if( value == null ) {
             throw new CloudException(provider.getCloudName() + " indicated the object was created, but did not provide a location");
         }
         String objectId = toId(value);
         Header date = response.getFirstHeader("Date");
         long created = System.currentTimeMillis();
 
         if( date != null ) {
             value = date.getValue();
             if( value != null ) {
                 created = provider.parseTime(value);
             }
         }
         String directory = bucketName;
         while( !directory.equals("/") && directory.endsWith("/") ) {
             directory = directory.substring(0, directory.length()-1);
         }
         if( directory.equals("/") ) {
             directory = null;
         }
         if( objectName == null ) {
             return Blob.getInstance(regionId, "/rest/objects/" + objectId, directory == null ? "/" : directory, created);
         }
         else {
             if( size == null ) {
                 size = new Storage<Gigabyte>(0, Storage.GIGABYTE);
             }
             return Blob.getInstance(regionId, "/rest/objects/" + objectId, directory, objectName, created, size);
         }
     }
 
     private @Nonnull String toId(@Nonnull String location) {
         return location.substring("/rest/objects/".length());
     }
 
     private @Nonnull Properties toProperties(@Nonnull Header header) {
         Properties p = new Properties();
 
         if( header.getValue() == null ) {
             return p;
         }
         String[] parts = header.getValue().split(",");
 
         if( parts == null || parts.length < 1 ) {
             parts = new String[] { header.getValue() };
         }
         for( String part : parts ) {
             String[] s = part.split("=");
 
             if( s.length == 2 ) {
                 p.setProperty(s[0].trim(), s[1].trim());
             }
         }
         return p;
     }
 
     private @Nonnull String toSignatureString(@Nonnull HttpRequestBase method, @Nonnull String contentType, @Nonnull String range, @Nonnull String date, @Nonnull URI resource, @Nonnull List<Header> emcHeaders) {
         StringBuilder emcHeaderString = new StringBuilder();
 
         TreeSet<String> sorted = new TreeSet<String>();
 
         for( Header header : emcHeaders ) {
             sorted.add(header.getName().toLowerCase());
         }
         boolean first = true;
         for( String headerName : sorted ) {
             for( Header header : emcHeaders ) {
                 if( header.getName().toLowerCase().equals(headerName) ) {
                     if( !first ) {
                         emcHeaderString.append("\n");
                     }
                     else {
                         first = false;
                     }
                     String val = header.getValue();
 
                     if( val == null ) {
                         val = "";
                     }
                     emcHeaderString.append(headerName);
                     emcHeaderString.append(":");
                     StringBuilder tmp = new StringBuilder();
                     for( char c : val.toCharArray() ) {
                         if( Character.isWhitespace(c) ) {
                             tmp.append(" ");
                         }
                         else {
                             tmp.append(c);
                         }
                     }
                     val = tmp.toString();
                     while( val.contains("  ") ) {
                         val = val.replaceAll("  ", " ");
                     }
                     emcHeaderString.append(val);
                 }
             }
         }
         String path = resource.getRawPath().toLowerCase();
 
         if( resource.getRawQuery() != null) {
             path = path + "?" + resource.getRawQuery().toLowerCase();
         }
 
         return (method.getMethod()  + "\n" + contentType + "\n" + range + "\n" + date + "\n" + path + "\n" + emcHeaderString.toString());
     }
 
     public @Nonnull Blob upload(@Nonnull String bucket, @Nonnull String name, @Nonnull String contentType, @Nonnull String content) throws CloudException, InternalException {
         if( logger.isTraceEnabled() ) {
             logger.trace("ENTER - " + AtmosMethod.class.getName() + ".upload(" + bucket + "," + name + "," + contentType + ",[CONTENT])");
         }
         if( wire.isDebugEnabled() ) {
             wire.debug("");
             wire.debug(">>> [POST/upload text] -> " + bucket + " / " + name + "--------------------------------------------------------------------------------------");
         }
         try {
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 throw new CloudException("No context was set for this request");
             }
             if( !bucket.endsWith("/") ) {
                 bucket = bucket + "/";
             }
             if( !bucket.startsWith("/") ) {
                 bucket = "/" + bucket;
             }
             String endpoint = getEndpoint(ctx, EndpointType.NAMESPACE, bucket + name);
             HttpPost post = new HttpPost(endpoint);
             HttpClient client = getClient(endpoint);
 
             authorize(ctx, post, contentType, null);
             try {
                 post.setEntity(new StringEntity(content, contentType, "utf-8"));
             }
             catch( UnsupportedEncodingException e ) {
                 logger.error("Unsupported UTF-8 encoding: " + e.getMessage());
                 e.printStackTrace();
                 throw new InternalException("Unsupported UTF-8 encoding");
             }
             if( wire.isDebugEnabled() ) {
                 wire.debug(post.getRequestLine().toString());
                 for( Header header : post.getAllHeaders() ) {
                     wire.debug(header.getName() + ": " + header.getValue());
                 }
                 wire.debug("");
             }
             HttpResponse response;
 
             try {
                 response = client.execute(post);
                 if( wire.isDebugEnabled() ) {
                     wire.debug(response.getStatusLine().toString());
                 }
             }
             catch( IOException e ) {
                 logger.error("I/O error from server communications: " + e.getMessage());
                 e.printStackTrace();
                 throw new InternalException(e);
             }
             int status = response.getStatusLine().getStatusCode();
 
             if( status == HttpServletResponse.SC_CREATED ) {
                 return toBlob(ctx, response, bucket + name, null, null);
             }
             else {
                 throw new AtmosException(response);
             }
         }
         finally {
             if( logger.isTraceEnabled() ) {
                 logger.trace("EXIT - " + AtmosMethod.class.getName() + ".upload()");
             }
             if( wire.isDebugEnabled() ) {
                 wire.debug("<<< [POST/upload text] -> " + bucket + " / " + name + "--------------------------------------------------------------------------------------");
                 wire.debug("");
             }
         }
     }
 
     public @Nonnull Blob upload(@Nonnull String bucket, @Nonnull String name, @Nonnull InputStream input, Storage<?> size) throws CloudException, InternalException {
         if( logger.isTraceEnabled() ) {
             logger.trace("ENTER - " + AtmosMethod.class.getName() + ".upload(" + bucket + "," + name + ",[CONTENT]," + size + ")");
         }
         if( wire.isDebugEnabled() ) {
             wire.debug("");
             wire.debug(">>> [POST/upload binary] -> " + bucket + " / " + name + "--------------------------------------------------------------------------------------");
         }
         try {
             ProviderContext ctx = provider.getContext();
 
             if( ctx == null ) {
                 throw new CloudException("No context was set for this request");
             }
             if( !bucket.endsWith("/") ) {
                 bucket = bucket + "/";
             }
             if( !bucket.startsWith("/") ) {
                 bucket = "/" + bucket;
             }
             long length = size.convertTo(Storage.BYTE).getQuantity().longValue();
             String endpoint = getEndpoint(ctx, EndpointType.NAMESPACE, bucket + name);
             HttpPost post = new HttpPost(endpoint);
             HttpClient client = getClient(endpoint);
 
             authorize(ctx, post, "application/octet-stream", null);
             post.setEntity(new InputStreamEntity(input, length, ContentType.APPLICATION_OCTET_STREAM));
             if( wire.isDebugEnabled() ) {
                 wire.debug(post.getRequestLine().toString());
                 for( Header header : post.getAllHeaders() ) {
                     wire.debug(header.getName() + ": " + header.getValue());
                 }
                 wire.debug("");
             }
             HttpResponse response;
 
             try {
                 response = client.execute(post);
                 if( wire.isDebugEnabled() ) {
                     wire.debug(response.getStatusLine().toString());
                 }
             }
             catch( IOException e ) {
                 logger.error("I/O error from server communications: " + e.getMessage());
                 e.printStackTrace();
                 throw new InternalException(e);
             }
             int status = response.getStatusLine().getStatusCode();
 
             if( status == HttpServletResponse.SC_CREATED ) {
                 return toBlob(ctx, response, bucket + name, null, null);
             }
             else {
                 throw new AtmosException(response);
             }
         }
         finally {
             if( logger.isTraceEnabled() ) {
                 logger.trace("EXIT - " + AtmosMethod.class.getName() + ".upload()");
             }
             if( wire.isDebugEnabled() ) {
                 wire.debug("<<< [POST/upload binary] -> " + bucket + " / " + name + "--------------------------------------------------------------------------------------");
                 wire.debug("");
             }
         }
     }
 }
