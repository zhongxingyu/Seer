 package com.socialcomputing.wps.server.planDictionnary.connectors.utils;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.zip.GZIPInputStream;
 
 import org.jdom.Element;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.socialcomputing.wps.server.planDictionnary.connectors.JMIException;
 
 import sun.misc.BASE64Encoder;
 
 public class UrlHelper extends ConnectorHelper {
 
     public static final String DTD_DEFINITION = "URL-connection";
     private static final Logger LOG = LoggerFactory.getLogger(UrlHelper.class);
 
     public enum Type {
         POST, GET;
     }
 
     protected Type type = Type.GET;
     protected String url = null;
     protected List<NameValuePair> defParams = new ArrayList<NameValuePair>();
     protected List<NameValuePair> curParams = new ArrayList<NameValuePair>();
     protected List<NameValuePair> headerParams = new ArrayList<NameValuePair>();
     protected boolean basicAuth = false;
     protected String user, password;
 
     protected InputStream stream = null;
     protected String contentType = null;
     protected String contentEncoding = null;
     protected int responseCode = 0;
     protected URLConnection connection = null;
 
     public UrlHelper() {
         super();
     }
 
     public UrlHelper(String url) {
         super();
         this.url = url;
     }
 
     public UrlHelper(Type type, String url) {
         super();
         this.type = type;
         this.url = url;
     }
 
     @Override
     public void readObject(Element connection) {
         url = connection.getChildText("url");
         if (connection.getAttributeValue("type") != null && connection.getAttributeValue("type").equalsIgnoreCase("POST")) {
             type = Type.POST;
         }
         for (Element elem : (List<Element>) connection.getChildren("url-header")) {
             NameValuePair nameValue = new NameValuePair(elem.getAttributeValue("name"), elem.getText(), elem.getAttributeValue("default"));
             LOG.debug("url header = {}", nameValue);
             headerParams.add(nameValue);
         }
         for (Element elem : (List<Element>) connection.getChildren("url-parameter")) {
             NameValuePair nameValue = new NameValuePair(elem.getAttributeValue("name"), elem.getText(), elem.getAttributeValue("default"));
             LOG.debug("url parameter = {}", nameValue);
             defParams.add(nameValue);
         }
         Element basic = connection.getChild("basic-authentication");
         if (basic != null) {
             basicAuth = true;
             user = basic.getAttributeValue("username");
             password = basic.getAttributeValue("password");
             LOG.debug("basic autentication enabled");
         }
     }
 
     public void openConnections() throws JMIException {
         openConnections(0, null);
     }
 
     @Override
     public void openConnections(int planType, Hashtable<String, Object> wpsparams) throws JMIException {
         LOG.debug("UrlHelper open connections");
         StringBuilder parameters = new StringBuilder();
         HttpURLConnection httpConnection = null;
         
         try {
             String realUrl = super.ReplaceParameter(url, wpsparams);
             boolean first = true;
             if (defParams != null) {
                 for (NameValuePair param : defParams) {
                     if (!first)
                         parameters.append('&');
                     parameters.append(param.getName()).append('=')
                             .append(URLEncoder.encode(super.ReplaceParameter(param.getValue(), wpsparams), "UTF-8"));
                     first = false;
                 }
             }
             if (curParams != null) {
                 for (NameValuePair param : curParams) {
                     if (!first)
                         parameters.append('&');
                     parameters.append(param.getName()).append('=')
                             .append(URLEncoder.encode(super.ReplaceParameter(param.getValue(), wpsparams), "UTF-8"));
                     first = false;
                 }
             }
 
             String parametersString = parameters.toString();
             LOG.debug("url = {}, params = {}", realUrl, parametersString);
             URL u = new URL(type == Type.POST || (type == Type.GET && parameters.length() == 0) ? realUrl : realUrl + "?" + parametersString);
             connection = u.openConnection();
             connection.setUseCaches(false);
             connection.setDoInput(true);
             if(basicAuth) {
                 BASE64Encoder enc = new BASE64Encoder();
                 String userpassword = super.ReplaceParameter(user, wpsparams) + ":" + super.ReplaceParameter(password, wpsparams);
                 String encodedAuthorization = enc.encode(userpassword.getBytes());
                 connection.setRequestProperty("Authorization", "Basic " + encodedAuthorization);
                 LOG.debug("Setting basic authentication header = {}", encodedAuthorization);
             }
             connection.setRequestProperty( "Accept-Encoding", "gzip");
             for (NameValuePair header : headerParams) {
                 String paramValue = super.ReplaceParameter(header.getValue(), wpsparams);
                 connection.setRequestProperty(header.getName(), paramValue);
                 LOG.debug("Setting parameter {} with value {}", header.getName(), paramValue);
             }
             if (connection instanceof HttpURLConnection) 
                 httpConnection = (HttpURLConnection) connection;
             if (type == Type.POST && httpConnection != null) {
                 httpConnection.setDoOutput(true);
                 httpConnection.setRequestMethod("POST");
                 httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                 httpConnection.setRequestProperty("Content-Length",
                                                   "" + Integer.toString(parameters.toString().getBytes().length));
                 DataOutputStream wr = new DataOutputStream(httpConnection.getOutputStream());
                 wr.writeBytes(parameters.toString());
                 wr.flush();
                 wr.close();
             }
             if( httpConnection != null)
                 responseCode = httpConnection.getResponseCode();
             contentType = connection.getContentType();
             contentEncoding = connection.getContentEncoding();
             stream = connection.getInputStream();
         }
         catch (IOException e) {
             if (httpConnection != null) {
                 stream = httpConnection.getErrorStream();
             }
             LOG.error(e.getMessage(), e);
            throw new JMIException("openConnections: ", e);
         }
     }
 
     @Override
     public void closeConnections() throws JMIException {
         if (stream != null) {
             try {
                 stream.close();
             }
             catch (IOException e) {}
             stream = null;
         }
     }
 
     public void setUrl(String url) {
         this.url = url;
     }
 
     public String getUrl() {
         return this.url;
     }
 
     public void setType(Type type) {
         this.type = type;
     }
     
     public Type getType() {
         return this.type;
     }
 
 
     public List<NameValuePair> getDefParams() {
         return defParams;
     }
 
     public InputStream getStream() throws JMIException {
         try {
             return contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip") ? new GZIPInputStream(stream) : stream;
         }
         catch (IOException e) {
             throw new JMIException("UrlHelper read failed", e);
         }
     }
 
     public String getContentType() {
         return contentType;
     }
 
     public String getContentEncoding() {
         return contentEncoding;
     }
     
     public URLConnection getConnection() {
         return connection ;
     }
     
     public int getResponseCode() {
         return responseCode;
     }
     
     public String getResult() {
         Writer writer = new StringWriter();
         try {
             Reader reader = new BufferedReader( new InputStreamReader( this.getStream()));
             char[] buffer = new char[5120];
             int n;
             while ((n = reader.read(buffer)) != -1) {
                 writer.write(buffer, 0, n);
             }
         }
         catch (Exception e) {
             e.printStackTrace();
         }
         return writer.toString();
     }
 
     public void addParameter(String name, String value) {
         curParams.add(new NameValuePair(name, value));
     }
     
     public void addParameter(NameValuePair parameter) {
         curParams.add(parameter);
     }
     
     public List<NameValuePair> getParameters() {
     	return this.curParams;
     }
 
     public void addHeader(String name, String value) {
         headerParams.add(new NameValuePair(name, value));
     }
     
     public void setBasicAuth( String user, String password) throws JMIException {
         basicAuth = true;
         this.user = user;
         this.password = password;
     }
 }
