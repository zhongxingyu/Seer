 /**
  * Copyright (C) 2011-2012 enStratus Networks Inc
  *
  * ====================================================================
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * ====================================================================
  */
 
 package org.dasein.cloud.opsource;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import javax.annotation.Nonnull;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpEntityEnclosingRequest;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.HttpVersion;
 import org.apache.http.ParseException;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpHead;
 import org.apache.http.client.methods.HttpOptions;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpTrace;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.client.params.AuthPolicy;
 import org.apache.http.conn.params.ConnRoutePNames;
 import org.apache.http.entity.AbstractHttpEntity;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.AbstractHttpMessage;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.params.HttpProtocolParams;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.apache.log4j.Logger;
 import org.dasein.cloud.CloudException;
 import org.dasein.cloud.InternalException;
 
 import org.dasein.cloud.ProviderContext;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import org.xml.sax.SAXException;
 
 public class OpSourceMethod {
     static private final Logger logger = OpSource.getLogger(OpSourceMethod.class);
     static private final Logger wire   = OpSource.getWireLogger(OpSourceMethod.class);
 
 	private Map<String,String> parameters  = null;
 	private OpSource           provider    = null;
 	private String             endpoint    = null;
 
 	
 	static public class ParsedError {
         public int code;
         public String message;
     }
 	
 	public OpSourceMethod(OpSource provider, String url, Map<String,String> parameters) throws InternalException {
         this.endpoint = url;
 		this.parameters = parameters;
         this.provider = provider;
 	}	
 
     protected AbstractHttpMessage getMethod(String httpMethod,String urlStr) {
     	AbstractHttpMessage method = null;
         if(httpMethod.equals("GET")){
         	method = new HttpGet(urlStr);
         }else if(httpMethod.equals("POST")){
         	 method = new HttpPost(urlStr);
         }else if(httpMethod.equals("PUT")){
             method = new HttpPut(urlStr);	        	
         }else if(httpMethod.equals("DELETE")){
         	method = new HttpDelete(urlStr);
         }else if(httpMethod.equals("HEAD")){
         	 method = new HttpHead(urlStr);
         }else if(httpMethod.equals("OPTIONS")){
         	 method = new HttpOptions(urlStr);
         }else if(httpMethod.equals("HEAD")){
         	method = new HttpTrace(urlStr);
         }else{
         	return null;
         }
     	return method;
     }
 
     private @Nonnull HttpClient getClient(@Nonnull ProviderContext ctx, boolean ssl) {
         HttpParams params = new BasicHttpParams();
 
         HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
         //noinspection deprecation
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
     
 	public Document invoke() throws CloudException, InternalException {
         if( logger.isTraceEnabled() ) {
             logger.trace("enter - " + OpSource.class.getName() + ".invoke()");
         }
         try {
         	URL url = null;
 			try {
 				url = new URL(endpoint);
 			} catch (MalformedURLException e1) {
 				throw new CloudException(e1);				
 			}
 	        final String host = url.getHost();
 	        final int urlPort = url.getPort()==-1?url.getDefaultPort():url.getPort();
 	        final String urlStr = url.toString();
 	      	
 	        DefaultHttpClient httpclient = new DefaultHttpClient();
 
 	        /**  HTTP Authentication */
 	        String uid = new String(provider.getContext().getAccessPublic());
 	        String pwd = new String(provider.getContext().getAccessPrivate());
 	        
 	        /** Type of authentication */
 	        List<String> authPrefs = new ArrayList<String>(2);	       
 	        authPrefs.add(AuthPolicy.BASIC);
  
 	        httpclient.getParams().setParameter("http.auth.scheme-pref", authPrefs);
 	        httpclient.getCredentialsProvider().setCredentials(
                     new AuthScope(host, urlPort, null),
                     new UsernamePasswordCredentials(uid, pwd));
 	        
 	        if( wire.isDebugEnabled() ) {
 	            wire.debug("--------------------------------------------------------------> " + urlStr);
 	            wire.debug("");
 	        }
  
 	        AbstractHttpMessage method = this.getMethod(parameters.get(OpSource.HTTP_Method_Key), urlStr) ;
 	        method.setParams(new BasicHttpParams().setParameter(urlStr, url));
 	        /**  Set headers */
 	        method.addHeader(OpSource.Content_Type_Key, parameters.get(OpSource.Content_Type_Key));
       
 	        /** POST/PUT method specific logic */
 	        if (method instanceof HttpEntityEnclosingRequest) {
 	        	HttpEntityEnclosingRequest entityEnclosingMethod = (HttpEntityEnclosingRequest) method;
 	        	String requestBody = parameters.get(OpSource.HTTP_Post_Body_Key);
 	        	
 	            if (requestBody != null) {
                     if(wire.isDebugEnabled()){
                         wire.debug(requestBody);
                     }
 
 	            	AbstractHttpEntity entity = new ByteArrayEntity(requestBody.getBytes());
 					entity.setContentType(parameters.get(OpSource.Content_Type_Key));
 					entityEnclosingMethod.setEntity(entity);
 	            }else{
 	            	throw new CloudException("The request body is null for a post request");
 	            }                
             }           
 	        
 	        /** Now parse the xml */
 	        try {
         		
     			HttpResponse httpResponse ;
         		int status;
                 if( wire.isDebugEnabled() ) {                   
                     for( org.apache.http.Header header : method.getAllHeaders()) {
                         wire.debug(header.getName() + ": " + header.getValue());
                     }
                 }
                 /**  Now execute the request */
                 httpResponse = httpclient.execute((HttpUriRequest) method);
                 status = httpResponse.getStatusLine().getStatusCode();
                 if( wire.isDebugEnabled() ) {
                     wire.debug("invoke(): HTTP Status " + httpResponse.getStatusLine().getStatusCode() + " " +  httpResponse.getStatusLine().getReasonPhrase());
                 }                
                 org.apache.http.Header[] headers = httpResponse.getAllHeaders();
                 
                 HttpEntity entity = httpResponse.getEntity();
                 if( wire.isDebugEnabled() ) {
                     wire.debug("HTTP xml status code ---------" + status);
                     for( org.apache.http.Header h : headers ) {
                         if( h.getValue() != null ) {
                             wire.debug(h.getName() + ": " + h.getValue().trim());
                         }
                         else {
                             wire.debug(h.getName() + ":");
                         }
                     }
                     /** Can not enable this line, otherwise the entity would be empty*/
                    // wire.debug("OpSource Response Body for request " + urlStr + " = " + EntityUtils.toString(entity));
                     wire.debug("-----------------");
                 }
                 if( entity == null ) {
                     parseError(status, "Empty entity");
                 }
 
                 String responseBody = EntityUtils.toString(entity);
         		if( status == HttpStatus.SC_OK ) {
                     InputStream input = null;
                     try {
                     	input = new ByteArrayInputStream(responseBody.getBytes("UTF-8"));
                     	if(input != null){
                             Document doc = null;
                             try{
                                 doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
                                 if(wire.isDebugEnabled()){
                                     try{
                                         TransformerFactory transfac = TransformerFactory.newInstance();
                                         Transformer trans = transfac.newTransformer();
                                         trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                                         trans.setOutputProperty(OutputKeys.INDENT, "yes");
 
                                         StringWriter sw = new StringWriter();
                                         StreamResult result = new StreamResult(sw);
                                         DOMSource source = new DOMSource(doc);
                                         trans.transform(source, result);
                                         String xmlString = sw.toString();
                                         wire.debug(xmlString);
                                     }
                                     catch(Exception ex){
                                         ex.printStackTrace();
                                     }
                                 }
                             }
                             catch(Exception ex){
                                 ex.printStackTrace();
                                 logger.debug(ex.toString(), ex);
                             }
                     		return doc;
                         }
                     }
                     catch( IOException e ) {
                         logger.error("invoke(): Failed to read xml error due to a cloud I/O error: " + e.getMessage());
                         throw new CloudException(e);                    
                     }
                     /*
                     catch( SAXException e ) {
                         throw new CloudException(e);
                     }                    
                     catch( ParserConfigurationException e ) {
                         throw new InternalException(e);
                     }
                     */
         		}
                 else if(status == HttpStatus.SC_NOT_FOUND){
                     throw new CloudException("An internal error occured: The endpoint was not found");
                 }
         		else{        			
                     /*String resultXML = null;
                     try {                    	
                     	resultXML = EntityUtils.toString(entity);
                     }
                     catch( IOException e ) {
                         logger.error("invoke(): Failed to read xml error due to a cloud I/O error: " + e.getMessage());
                         if( logger.isTraceEnabled() ) {
                             e.printStackTrace();
                         }
                         throw new CloudException(e);                    
                     }
                     if(resultXML != null){
                     	parseError(status, resultXML);
                         return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(resultXML);
                     } */
                     if(responseBody != null){
                         parseError(status, responseBody);
                         Document parsedError = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(responseBody.getBytes("UTF-8")));
                         if(wire.isDebugEnabled()){
                             try{
                                 TransformerFactory transfac = TransformerFactory.newInstance();
                                 Transformer trans = transfac.newTransformer();
                                 trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                                 trans.setOutputProperty(OutputKeys.INDENT, "yes");
 
                                 StringWriter sw = new StringWriter();
                                 StreamResult result = new StreamResult(sw);
                                 DOMSource source = new DOMSource(parsedError);
                                 trans.transform(source, result);
                                 String xmlString = sw.toString();
                                 wire.debug(xmlString);
                             }
                             catch(Exception ex){
                                 ex.printStackTrace();
                             }
                         }
                         return parsedError;
                     }
         		}
     		} catch (ParseException e) {
     			throw new CloudException(e);
 			} catch (SAXException e) {
 				throw new CloudException(e);
 			} catch (IOException e) {
                 e.printStackTrace();
 				throw new CloudException(e);
 			} catch (ParserConfigurationException e) {
 				throw new CloudException(e);
 			}
     		finally {
     			httpclient.getConnectionManager().shutdown();
     		}
         }
         finally {
             if( logger.isTraceEnabled() ) {
                 logger.trace("exit - " + OpSource.class.getName() + ".invoke()");
             }
             if( wire.isDebugEnabled() ) {
                 wire.debug("");
                 wire.debug("--------------------------------------------------------------> " + endpoint);
             } 
         }
 		return null; 
 	}
 	
 	public String requestResult(String action, Document doc,String resultTag, String resultDetailTag) throws CloudException, InternalException{
 		 if(doc== null){
 	        throw new CloudException("Action -> " + action + " failed because request reponse is null");	
 		 }
                 
         if( wire.isDebugEnabled() ) {
         	wire.debug(provider.convertDomToString(doc));
         }
 
         String sNS = "";
         try{
             sNS = doc.getDocumentElement().getTagName().substring(0, doc.getDocumentElement().getTagName().indexOf(":") + 1);
         }
         catch(IndexOutOfBoundsException ex){}
     	NodeList blocks = doc.getElementsByTagName(sNS + resultTag);
     	if(blocks != null){
     		for(int i=0;i< blocks.getLength();i++){
     			Node attr = blocks.item(i);
     			if(attr.getFirstChild().getNodeValue().equals(OpSource.RESPONSE_RESULT_SUCCESS_VALUE)){
     				blocks = doc.getElementsByTagName(sNS + resultDetailTag);
     	    		if(blocks == null){
     	    			throw new CloudException(action + "  fails " + "without explaination !!!");
     	    		}else{
     	    			return blocks.item(0).getFirstChild().getNodeValue();
     	    		}
     			}else if(attr.getFirstChild().getNodeValue().equals(OpSource.RESPONSE_RESULT_ERROR_VALUE)){
     				blocks = doc.getElementsByTagName(sNS + resultDetailTag);
     	    		if(blocks == null){
     	    			throw new CloudException(action+ " fails " + "without explaination !!!");
     	    		}else{    	    			
     	    			throw new CloudException(blocks.item(0).getFirstChild().getNodeValue());
     	    		}
     			}  			
     		}
     	}
 		return null;		
 	}
 	
 	public String requestResultCode(String action, Document doc,String resultCode) throws CloudException, InternalException{
         if( wire.isDebugEnabled() ) {
         	wire.debug(provider.convertDomToString(doc));
         }
 
         String sNS = "";
         try{
             sNS = doc.getDocumentElement().getTagName().substring(0, doc.getDocumentElement().getTagName().indexOf(":") + 1);
         }
         catch(IndexOutOfBoundsException ex){}
     	NodeList blocks = doc.getElementsByTagName(sNS + resultCode);
     	
     	if(blocks != null){
     		return blocks.item(0).getFirstChild().getNodeValue();
     	}
 		return null;		
 	}
 	
 	public String getRequestResultId(String action, Document doc,String resultTag, String resultDetailTag) throws CloudException, InternalException{
         if( wire.isDebugEnabled() ) {
         	wire.debug(provider.convertDomToString(doc));
         }
 
         String sNS = "";
         try{
             sNS = doc.getDocumentElement().getTagName().substring(0, doc.getDocumentElement().getTagName().indexOf(":") + 1);
         }
         catch(IndexOutOfBoundsException ex){}
     	NodeList blocks = doc.getElementsByTagName(sNS + resultTag);
 
         try{
             StringWriter stw = new StringWriter();
             Transformer serializer = TransformerFactory.newInstance().newTransformer();
             serializer.transform(new DOMSource(doc), new StreamResult(stw));
         }
         catch(Exception ex){
             ex.printStackTrace();
         }
 
     	if(blocks != null){
     		for(int i=0;i< blocks.getLength();i++){
     			Node attr = blocks.item(i);
     			if(attr.getFirstChild().getNodeValue().equals(OpSource.RESPONSE_RESULT_SUCCESS_VALUE)){
     				blocks = doc.getElementsByTagName(sNS + resultDetailTag);
     	    		if(blocks == null){
     	    			throw new CloudException(action + "  fails " + "without explaination !!!");
     	    		}else{
     	    			String result =  blocks.item(0).getFirstChild().getNodeValue().toLowerCase();
     	    			return result.split("id:")[1].substring(0, result.split("id:")[1].indexOf(")")).trim();
     	    		}
     			}  		
     			if(attr.getFirstChild().getNodeValue().equals(OpSource.RESPONSE_RESULT_ERROR_VALUE)){
     				blocks = doc.getElementsByTagName(sNS + OpSource.RESPONSE_RESULT_DETAIL_TAG);
     	    		if(blocks == null){
     	    			logger.error(action + "  fails " + "without explaination !!!");
     	    			throw new CloudException(action + "  fails " + "without explaination !!!");
     	    		}else{
     	    			logger.error(blocks.item(0).getFirstChild().getNodeValue());
     	    			throw new CloudException(blocks.item(0).getFirstChild().getNodeValue());
     	    		}
     			}  			
     		}
     	}
 		return null;		
 	}
 	
 	public boolean requestResult(String action, Document doc) throws CloudException, InternalException{
 		
         if( wire.isDebugEnabled() ) {
         	wire.debug(provider.convertDomToString(doc));
         }
 
         String sNS = "";
         try{
             sNS = doc.getDocumentElement().getTagName().substring(0, doc.getDocumentElement().getTagName().indexOf(":") + 1);
         }
         catch(IndexOutOfBoundsException ex){}
 
     	NodeList blocks = doc.getElementsByTagName(sNS + OpSource.RESPONSE_RESULT_TAG);
     	if(blocks != null){
     		for(int i=0;i< blocks.getLength();i++){
     			Node attr = blocks.item(i);
     			if(attr.getFirstChild().getNodeValue().equals(OpSource.RESPONSE_RESULT_SUCCESS_VALUE)){
     				return true;
     			}  		
     			if(attr.getFirstChild().getNodeValue().equals(OpSource.RESPONSE_RESULT_ERROR_VALUE)){
     				blocks = doc.getElementsByTagName(sNS + OpSource.RESPONSE_RESULT_DETAIL_TAG);
     	    		if(blocks == null){
     	    			throw new CloudException(action + " fails " + "without explaination !!!");
     	    		}else{
     	    			throw new CloudException(blocks.item(0).getFirstChild().getNodeValue());
     	    		}
     			}  			
     		}
     	}
 		return false;		
 	}
 	
 	public boolean parseRequestResult(String action, Document doc, String resultTag, String resultDetailTag) throws CloudException, InternalException{
         if( wire.isDebugEnabled() ) {
         	wire.debug(provider.convertDomToString(doc));
         }
 
         String sNS = "";
         try{
             sNS = doc.getDocumentElement().getTagName().substring(0, doc.getDocumentElement().getTagName().indexOf(":") + 1);
         }
         catch(IndexOutOfBoundsException ex){}
     	NodeList blocks = doc.getElementsByTagName(sNS + resultTag);
     	if(blocks != null){
     		for(int i=0;i< blocks.getLength();i++){
     			Node attr = blocks.item(i);
     			if(attr.getFirstChild().getNodeValue().equals(OpSource.RESPONSE_RESULT_SUCCESS_VALUE)){
     				return true;
     			}  		
     			if(attr.getFirstChild().getNodeValue().equals(OpSource.RESPONSE_RESULT_ERROR_VALUE)){
     				blocks = doc.getElementsByTagName(sNS + resultDetailTag);
     	    		if(blocks == null){
     	    			logger.error(action + " fails " + "without explaination !!!");
     	    			throw new CloudException(action + " fails " + "without explaination !!!");
     	    			
     	    		}else{
     	    			logger.error(blocks.item(0).getFirstChild().getNodeValue());
     	    			throw new CloudException(blocks.item(0).getFirstChild().getNodeValue());
     	    		}
     			}  			
     		}
     	}
 		return false;		
 	}
 	
 	private ParsedError parseError(int httpStatus, String assumedXml) throws InternalException {
 		if( logger.isTraceEnabled() ) {
 		  logger.trace("enter - " + OpSourceMethod.class.getName() + ".parseError(" + httpStatus + "," + assumedXml + ")");
 		}	
 		try {
             ParsedError error = new ParsedError();            
             error.code = httpStatus;
             error.message = null;
             try {
                 Document doc = parseResponse(httpStatus, assumedXml);
                 String sNS = "";
                 try{
                     sNS = doc.getDocumentElement().getTagName().substring(0, doc.getDocumentElement().getTagName().indexOf(":") + 1);
                 }
                 catch(IndexOutOfBoundsException ex){}
 
                 NodeList codes = doc.getElementsByTagName(sNS + "resultCode");
                 String reasoncode = codes.item(0).getFirstChild().getNodeValue();
                 reasoncode = reasoncode.substring(reasoncode.indexOf("_")+1);
                 error.code = Integer.parseInt(reasoncode);
                 /*for( int i=0; i<codes.getLength(); i++ ) {
                     Node n = codes.item(i);
                     
                     if( n != null && n.hasChildNodes() ) {
                         error.code = Integer.parseInt(n.getFirstChild().getNodeValue().trim());
                     }
                 }*/
 
                 NodeList text = doc.getElementsByTagName(sNS + "resultDetail");
                 error.message = text.item(0).getFirstChild().getNodeValue();
                 /*for( int i=0; i<text.getLength(); i++ ) {
                     Node n = text.item(i);
                     
                     if( n != null && n.hasChildNodes() ) {
                         error.message = n.getFirstChild().getNodeValue();
                     }
                 }*/
                 logger.error(error.code + ": " + error.message);
             }
             catch( Throwable ignore ) {
                 String errorMessage = "";
                 try{
                     errorMessage = URLDecoder.decode(ignore.getMessage(), "UTF-8");
                 }
                 catch(Exception ex){ex.printStackTrace();}
                 logger.warn("parseError(): Error was unparsable: " + errorMessage);
                 if( error.message == null ) {
                     error.message = assumedXml;
                 }
             }
             if( error.message == null ) {
                 if( httpStatus == 401 ) {
                     error.message = "Unauthorized user";
                 }
                 else if( httpStatus == 430 ) {
                     error.message = "Malformed parameters";
                 }
                 else if( httpStatus == 547 || httpStatus == 530 ) {
                     error.message = "Server error in cloud (" + httpStatus + ")";
                 }
                 else if( httpStatus == 531 ) {
                     error.message = "Unable to find account";
                 }
                 else {
                     error.message = "Received error code from server: " + httpStatus;
                 }
             }
             
             logger.trace("errors - " + error.message);
             
             return error;
         }
         finally {
             if( logger.isTraceEnabled() ) {
                 logger.trace("exit - " + OpSourceMethod.class.getName() + ".parseError()");
             }
         }
     }
 	
 	private Document parseResponse(int code, String xml) throws CloudException, InternalException {
 	    if( logger.isTraceEnabled() ) {
 	    	logger.trace("enter - " + OpSourceMethod.class.getName() + ".parseResponse(" + xml + ")");
 	    }
 	    try {
 	    	try {
 	    		if( wire.isDebugEnabled() ) {
 	    			wire.debug(xml);
 	    		}
 	            //ByteArrayInputStream input = new ByteArrayInputStream(xml.getBytes("UTF-8"));
 
 	            //return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
 
                 Document parseForError = null;
                parseForError = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
                return parseForError;
 	    	}
	        catch( IOException e ) {
 	        	throw new CloudException(e);
 	        }
            catch( ParserConfigurationException e ) {
                 throw new CloudException(e);
             }
            catch( SAXException e ) {
                 throw new CloudException("Received error code from server [" + code + "]: " + xml);
             }
         }
         finally {
         	if( logger.isTraceEnabled() ) {
         		logger.trace("exit - " + OpSourceMethod.class.getName() + ".parseResponse()");
         	}
         }
 	}
 }
