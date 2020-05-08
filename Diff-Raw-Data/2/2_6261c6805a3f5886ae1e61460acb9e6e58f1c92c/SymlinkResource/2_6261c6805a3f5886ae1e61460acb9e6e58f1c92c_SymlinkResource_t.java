 /*
  * "Copyright (c) 2010-11 The Regents of the University  of California. 
  * All rights reserved.
  *
  * Permission to use, copy, modify, and distribute this software and its
  * documentation for any purpose, without fee, and without written agreement is
  * hereby granted, provided that the above copyright notice, the following
  * two paragraphs and the author appear in all copies of this software.
  *
  * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
  * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
  * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
  * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
  * AND FITNESS FOR A PARTICULAR PURPOSE.  THE SOFTWARE PROVIDED HEREUNDER IS
  * ON AN "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO
  * PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS."
  *
  * Author:  Jorge Ortiz (jortiz@cs.berkeley.edu)
  * IS4 release version 1.1
  */
 package local.rest.resources;
 
 import local.db.*;
 import local.rest.*;
 import local.rest.resources.util.*;
 import local.rest.interfaces.*;
 import is4.*;
 
 import net.sf.json.*;
 
 import java.util.*;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 import java.lang.StringBuffer;
 import java.net.*;
 
 import com.sun.net.httpserver.*;
 import javax.naming.InvalidNameException;
 import java.io.*; 
 
 public class SymlinkResource extends Resource{
 	protected static transient Logger logger = Logger.getLogger(SymlinkResource.class.getPackage().getName());
 
 	private String uri_link = null;
 	private URL url_link = null;
 
 	public SymlinkResource(String path, String uri) throws Exception, InvalidNameException{
 		super(path);
 
 		uri_link = uri;
 
 		//set type
 		TYPE = ResourceUtils.SYMLINK_RSRC;
 		database.setRRType(URI, ResourceUtils.translateType(TYPE).toLowerCase());
 
 		if(!database.isSymlink(URI)){
 			//add to symlink table
 			database.insertNewSymlinkEntry(URI, uri_link);
 		}
 	}
 
 	public SymlinkResource(String path, URL is4Url) throws Exception, InvalidNameException{
 		super(path);
 
 		url_link = is4Url;
 
 		//set type
 		TYPE = ResourceUtils.SYMLINK_RSRC;
 		database.setRRType(URI, ResourceUtils.translateType(TYPE).toLowerCase());
 
 		if(!database.isSymlink(URI)){
 			//add to symlink table
 			database.insertNewSymlinkEntry(URI, is4Url.toString());
 		}
 	}
 
 	public String getLinkString(){
 		if(uri_link != null)
 			return uri_link;
 		else if(url_link != null)
 			return url_link.toString();
 		return null;
 	}
 
     public void get(HttpExchange exchange, boolean internalCall, JSONObject internalResp){
         String links_to = database.getSymlinkAlias(URI);
         links_to = cleanPath(links_to);
         logger.info("links_to::" + links_to);
         if(links_to.startsWith("/")){
             logger.info("EXCHANGE:" + exchangeJSON.toString());
             String requestPath = exchangeJSON.getString("requestUri");
             String tail = null;
             if(requestPath.contains("?")){
                 tail = requestPath.substring(requestPath.indexOf("?"), requestPath.length());
                 logger.info("TAIL::" + tail);
                 requestPath = requestPath.replace(tail, "");
             }
             requestPath = cleanPath(requestPath);
             String translation= requestPath.replace(URI, links_to);
             logger.info("REQUEST PATH::" + requestPath + ", REPLACE::" + URI + ", WITH::" + links_to);
             logger.info("TRANSLATION PATH::" + translation);
             Resource r = null;
             if(!database.isSymlink(links_to))
                 r = RESTServer.getResource(translation);
             else 
                 r = RESTServer.getResource(links_to);
             if(r!=null){
                 r.exchangeJSON.accumulateAll(this.exchangeJSON);
                 if(tail != null){
                     logger.fine("new request uri=" + translation + tail);
                     r.exchangeJSON.put("requestUri", translation + tail);
                 } else {
                     logger.fine("new request uri=" + translation);
                     r.exchangeJSON.put("requestUri", translation);
                 }
                 this.exchangeJSON.clear();
                r.get(exchange, internalCall, internalResp);
                 return;
             }
         }
 
         sendResponse(exchange, 404, null, internalCall, internalResp);
     }
 
 	public  void get2(HttpExchange exchange, boolean internalCall, JSONObject internalResp){
 		//String tailResources = null;
 		//this symlink points directly to a hardlink
 		if(uri_link != null && !database.isSymlink(uri_link)){
 			logger.info(uri_link + " is not a symlink");
 			handleUriSymlinkRequest(exchange, internalCall, internalResp, uri_link);		
 		} 
 
 		//this symlink points to another symlink
 		else if (uri_link != null && database.isSymlink(uri_link)){
 			String linksToStr = database.getSymlinkAlias(uri_link);
 			while(linksToStr !=null && database.isSymlink(linksToStr)){
 				linksToStr = database.getSymlinkAlias(linksToStr);
 			}
 
 			if(linksToStr !=null && linksToStr.startsWith("/"))
 				handleUriSymlinkRequest(exchange, internalCall, internalResp, linksToStr);
 			//forward to the sfs instances that this symlink points to
 			else
 				handleUrlSymlinkRequest(exchange, internalCall, internalResp, linksToStr);
 		}
 		
 		//forward to the sfs instances that this symlink points to
 		else if (url_link != null){
 			handleUrlSymlinkRequest(exchange, internalCall, internalResp, url_link.toString());
 		}
 	}
 
 	private void handleUriSymlinkRequest(HttpExchange exchange, boolean internalCall, JSONObject internalResp, String linksTo){
 		String tailResources = getTailResourceUri(exchange, true);
 		logger.info("tail_resources: " + tailResources);
 		String thisUri = null;
 		if( (linksTo.endsWith("/") && !tailResources.startsWith("/")) || 
 			(!linksTo.endsWith("/") && tailResources.startsWith("/")) )
 		{
 			thisUri = linksTo + tailResources;
 		} else if (linksTo.endsWith("/") && tailResources.startsWith("/")) {
 			tailResources = tailResources.substring(1, tailResources.length());
 			thisUri = linksTo + tailResources;
 		} else if (!linksTo.endsWith("/") && !tailResources.startsWith("/")) {
 			thisUri = linksTo + "/" +  tailResources;
 		}
 		//the request is translated
 		exchangeJSON.put("requestUri", thisUri);
 		logger.info("Setting internalExchange.requestUri=" + thisUri);
 
 		if(thisUri.contains("*")){
 			exchange.setAttribute("request_uri", thisUri);
 			handleRecursiveFSQuery(exchange, internalCall, internalResp);
 		} else {
 			Resource resource = RESTServer.getResource(thisUri);
 			if(resource !=null){
 				resource.exchangeJSON.putAll(this.exchangeJSON);
 				resource.get(exchange, internalCall, internalResp);
 			} else {
 				logger.warning("could not get resource: " + thisUri);
 				sendResponse(exchange, 404, null, internalCall, internalResp);
 			}
 		}
 	}
 
 	private void handleUrlSymlinkRequest(HttpExchange exchange, boolean internalCall, JSONObject internalResp, String linksToUrl){
 		try {
 			String tailResources = getTailResourceUri(exchange, true);
 			tailResources = getTailResourceUri(exchange, false);
 			String thisUrl = linksToUrl + tailResources;
 			logger.info("GET " + thisUrl);
 			StringBuffer serverRespBuffer = new StringBuffer();
 			HttpURLConnection is4Conn = is4ServerGet(thisUrl, serverRespBuffer);
 			if(is4Conn != null){
 				String requestUri = exchangeJSON.getString("requestUri");
 				if(requestUri.contains("?"))
 					requestUri = requestUri.substring(0, requestUri.indexOf("?"));
 
 				if(requestUri.contains("*") && !requestUri.endsWith("*")) {
 					JSONObject fixedServerResp = new JSONObject();
 					String localUri = URI + tailResources;
 					fixedServerResp.put(localUri, serverRespBuffer.toString());
 					sendResponse(exchange, is4Conn.getResponseCode(), fixedServerResp.toString(), 
 							internalCall, internalResp);
 				} else {
 					sendResponse(exchange, is4Conn.getResponseCode(), serverRespBuffer.toString(), 
 							internalCall, internalResp);
 				}
 				is4Conn.disconnect();
 			} else {
 				sendResponse(exchange, 504 /* Gateway timeout */, null, internalCall, internalResp);
 			}
 		}catch(Exception e){
 			logger.log(Level.WARNING, "", e);
 			sendResponse(exchange, 504 /* Gateway timeout */, null, internalCall, internalResp);
 		}
 	}
 
 	public void put(HttpExchange exchange, String data, boolean internalCall, JSONObject internalResp){
 		try {
 			String tailResources = null;
 			if(uri_link != null){
 				tailResources = getTailResourceUri(exchange, true);
 				String thisUri = uri_link + tailResources;
 				Resource resource = RESTServer.getResource(thisUri);
 				resource.exchangeJSON.putAll(this.exchangeJSON);
 				resource.put(exchange, data, internalCall, internalResp);
 			} else if (url_link != null){
 				tailResources = getTailResourceUri(exchange, false);
 				String thisUrl = url_link.toString() + tailResources;
 				StringBuffer serverRespBuffer = new StringBuffer();
 				HttpURLConnection is4Conn = is4ServerPut(thisUrl, data, serverRespBuffer);
 				if(is4Conn != null){
 					String requestUri = exchangeJSON.getString("requestUri");
 					if(requestUri.contains("?"))
 						requestUri = requestUri.substring(0, requestUri.indexOf("?"));
 
 					if(requestUri.contains("*") && !requestUri.endsWith("*")) {
 						JSONObject fixedServerResp = new JSONObject();
 						String localUri = URI + tailResources;
 						fixedServerResp.put(localUri, serverRespBuffer.toString());
 						sendResponse(exchange, is4Conn.getResponseCode(), fixedServerResp.toString(), 
 								internalCall, internalResp);
 					} else {
 						sendResponse(exchange, is4Conn.getResponseCode(), serverRespBuffer.toString(), 
 								internalCall, internalResp);
 					}
 					is4Conn.disconnect();
 				} else {
 					sendResponse(exchange, 504 /* Gateway timeout */, null, internalCall, internalResp);
 				}
 			}
 		} catch(Exception e){
 			logger.log(Level.WARNING, "", e);
 			sendResponse(exchange, 504 /* Gateway timeout */, null, internalCall, internalResp);
 		}
 
 	}
 
 	public void post(HttpExchange exchange, String data, boolean internalCall, JSONObject internalResp){
 		try {
 			String tailResources = null;
 			if(uri_link != null){
 				tailResources = getTailResourceUri(exchange, true);
 				String thisUri = uri_link + tailResources;
 				Resource resource = RESTServer.getResource(thisUri);
 				resource.exchangeJSON.putAll(this.exchangeJSON);
 				resource.post(exchange, data, internalCall, internalResp);
 			} else if (url_link != null){
 				tailResources = getTailResourceUri(exchange, false);
 				String thisUrl = url_link.toString() + tailResources;
 				StringBuffer serverRespBuffer = new StringBuffer();
 				HttpURLConnection is4Conn = is4ServerPost(thisUrl, data, serverRespBuffer);
 				if(is4Conn != null){
 					String requestUri = exchangeJSON.getString("requestUri");
 					if(requestUri.contains("?"))
 						requestUri = requestUri.substring(0, requestUri.indexOf("?"));
 
 					if(requestUri.contains("*") && !requestUri.endsWith("*")) {
 						JSONObject fixedServerResp = new JSONObject();
 						String localUri = URI + tailResources;
 						fixedServerResp.put(localUri, serverRespBuffer.toString());
 						sendResponse(exchange, is4Conn.getResponseCode(), fixedServerResp.toString(), 
 								internalCall, internalResp);
 					} else {
 						sendResponse(exchange, is4Conn.getResponseCode(), serverRespBuffer.toString(), 
 								internalCall, internalResp);
 					}
 					is4Conn.disconnect();
 				} else {
 					sendResponse(exchange, 504 /* Gateway timeout */, null, internalCall, internalResp);
 				}
 			}
 		} catch(Exception e){
 			logger.log(Level.WARNING, "", e);
 			sendResponse(exchange, 504 /* Gateway timeout */, null, internalCall, internalResp);
 		}
 
 	}
 
 	public void loopDelete(){
 		logger.info("LoopDelete; Removing symlink entry: " + URI);
 		database.removeSymlinkEntry(URI);
 		database.removeRestResource(URI);
 		RESTServer.removeResource(this);
 
 		//delete from internal presentation
 		this.metadataGraph.removeNode(this.URI);
 	}
 
 
 	public void delete(HttpExchange exchange, boolean internalCall, JSONObject internalResp){
 		try {
 			logger.info("Symlink delete:" + URI);
 			String tailResources = null;
 			if(uri_link != null){
 				tailResources = getTailResourceUri(exchange, true);
 				if(tailResources.equals("")){
 					database.removeSymlinkEntry(URI);
 					database.removeRestResource(URI);
 					RESTServer.removeResource(this);
 
 					//delete from internal presentation
 					this.metadataGraph.removeNode(this.URI);
 					
 					sendResponse(exchange, 202, null, internalCall, internalResp);
 					return;
 				}
 
 				//walk the link path to either a hardlink or an external url
 				String linksToStr = uri_link;
 				if(database.isSymlink(linksToStr)){
 					linksToStr = database.getSymlinkAlias(uri_link);
 					while(linksToStr !=null && database.isSymlink(linksToStr)){
 						linksToStr = database.getSymlinkAlias(linksToStr);
 					}
 				}
 
 				//handle the deletion of a hardlink
 				if(linksToStr.startsWith("/")){
 					String thisUri = uri_link + tailResources;
 					Resource resource = RESTServer.getResource(thisUri);
 					if(resource != null){
 						resource.exchangeJSON.putAll(this.exchangeJSON);
 						resource.delete(exchange, internalCall, internalResp);
 					}else{
 						sendResponse(exchange, 404, null, internalCall, internalResp);
 						return;
 					}
 				} 
 				//forward the delete to the external link
 				else {
 					String thisUrl = linksToStr + tailResources;
 					StringBuffer serverRespBuffer = new StringBuffer();
 					HttpURLConnection is4Conn = is4ServerDelete(thisUrl, serverRespBuffer);
 					if(is4Conn != null){
 						String requestUri = exchangeJSON.getString("requestUri");
 						if(requestUri.contains("?"))
 							requestUri = requestUri.substring(0, requestUri.indexOf("?"));
 
 						if(requestUri.contains("*") && !requestUri.endsWith("*")) {
 							JSONObject fixedServerResp = new JSONObject();
 							String localUri = URI + tailResources;
 							fixedServerResp.put(localUri, serverRespBuffer.toString());
 							sendResponse(exchange, is4Conn.getResponseCode(), fixedServerResp.toString(), 
 									internalCall, internalResp);
 						} else {
 							sendResponse(exchange, is4Conn.getResponseCode(), serverRespBuffer.toString(), 
 									internalCall, internalResp);
 						}
 						is4Conn.disconnect();
 					} else {
 						sendResponse(exchange, 504 /* Gateway timeout */, null, internalCall, internalResp);
 					}
 				}
 			} else if (url_link != null){
 				tailResources = getTailResourceUri(exchange, false);
 				if(tailResources.equals("")){
 					database.removeSymlinkEntry(URI);
 					database.removeRestResource(URI);
 					RESTServer.removeResource(this);
 
 					//delete from internal presentation
 					this.metadataGraph.removeNode(this.URI);
 
 					sendResponse(exchange, 202, null, internalCall, internalResp);
 					return;
 				}
 				String thisUrl = url_link.toString() + tailResources;
 				StringBuffer serverRespBuffer = new StringBuffer();
 				HttpURLConnection is4Conn = is4ServerDelete(thisUrl, serverRespBuffer);
 				if(is4Conn != null){
 					String requestUri = exchangeJSON.getString("requestUri");
 					if(requestUri.contains("?"))
 						requestUri = requestUri.substring(0, requestUri.indexOf("?"));
 
 					if(requestUri.contains("*") && !requestUri.endsWith("*")) {
 						JSONObject fixedServerResp = new JSONObject();
 						String localUri = URI + tailResources;
 						fixedServerResp.put(localUri, serverRespBuffer.toString());
 						sendResponse(exchange, is4Conn.getResponseCode(), fixedServerResp.toString(), 
 								internalCall, internalResp);
 					} else {
 						sendResponse(exchange, is4Conn.getResponseCode(), serverRespBuffer.toString(), 
 								internalCall, internalResp);
 					}
 					is4Conn.disconnect();
 				} else {
 					sendResponse(exchange, 504 /* Gateway timeout */, null, internalCall, internalResp);
 				}
 			}
 		} catch(Exception e){
 			logger.log(Level.WARNING, "", e);
 			sendResponse(exchange, 504 /* Gateway timeout */, null, internalCall, internalResp);
 		}
 
 	}
 
 	/**
 	 * Get the piece of the string that is ahead of this URI.  Remove parameters if symlink is to local resource, 
 	 * leave the params if this symlink points to another is4 server (url_link is not null).
 	 */
 	private String getTailResourceUri(HttpExchange exchange, boolean removeParams){
 		String requestUri=null;
 		if(exchangeJSON.containsKey("requestUri")){
 			requestUri=exchangeJSON.getString("requestUri");
 			logger.info("Got internalExchange.requestUri=" + requestUri);
 			exchangeJSON.discard("requestUri");
 		}else{
 			requestUri = exchangeJSON.getString("requestUri");
 		}
 		String myUri = URI;
 
 		//change the request uri if it's a recursive structural query
 		if(requestUri.contains("*") && requestUri.contains("?")){
 			String transReqUri = requestUri.substring(0, requestUri.indexOf("?"));
 			if(transReqUri.endsWith("*")){
 				requestUri = requestUri.replace(transReqUri, URI + "*");
 				logger.info("Found * query, replacing: " + transReqUri + " with " + URI +  "* = requestUri " + requestUri);
 			} else if(transReqUri.contains("*") && !transReqUri.endsWith("*")){
 				String res = processMidStarReq(transReqUri);
 				if(res != null)
 					requestUri = res;
 				logger.fine("Symlink_:: updated Request URI: " + requestUri);
 			} else if(transReqUri.contains("*") && transReqUri.endsWith("*") && 
 					transReqUri.indexOf("*")<transReqUri.lastIndexOf("*")){
 				String res = processMidStarReq(transReqUri);
 				if(res != null)
 					requestUri = res;
 				logger.fine("Symlink_:: updated Request URI: " + requestUri); 
 			}
 			else {
 				requestUri = requestUri.replace(transReqUri, URI);
 				logger.info("Found * query, replacing: " + transReqUri + " with " + URI + "* = requestUri " + requestUri);
 			}
 		} else if(requestUri.contains("*") && !requestUri.contains("?")){
 			String transReqUri = requestUri;
 			if(transReqUri.endsWith("*")){
 				requestUri = requestUri.replace(transReqUri, URI + "*");
 				logger.info("Found * query, replacing: " + transReqUri + " with " + URI + "* = requestUri " + requestUri);
 			} else if(transReqUri.contains("*") && !transReqUri.endsWith("*")){
 				String res = processMidStarReq(transReqUri);
 				if(res != null)
 					requestUri = res;
 				logger.fine("Symlink_:: updated Request URI: " + requestUri);
 			} else if(transReqUri.contains("*") && transReqUri.endsWith("*") && 
 					transReqUri.indexOf("*")<transReqUri.lastIndexOf("*")){
 				String res = processMidStarReq(transReqUri);
 				if(res != null)
 					requestUri = res;
 				logger.fine("Symlink_:: updated Request URI: " + requestUri); 
 			}
 			else {
 				requestUri = requestUri.replace(transReqUri, URI);
 				logger.info("Found * query, replacing: " + transReqUri + " with " + URI + " = requestUri " + requestUri);
 			}	
 		}
 
 		//remove parameters first
 		String paramsStr = null;
 		if(requestUri.contains("?")){
 			paramsStr = requestUri.substring(requestUri.indexOf("?"), requestUri.length());
 			requestUri = requestUri.replace(paramsStr, "");
 		}
 	
 		//remove trailing / for uris	
 		if(requestUri.endsWith("/"))
 			requestUri = requestUri.substring(0, requestUri.length()-1);
 		if(myUri.endsWith("/"))
 			myUri = myUri.substring(0, myUri.length()-1);
 		
 		//remove myUri from the request uri
 		String tailResources = requestUri.replace(myUri, "");
 		logger.info("getTailResourceUri(): Remove: " + myUri + " from " + requestUri + " result: " + tailResources);
 
 		//if only a / is left, then the request uri and this uri are equal
 		/*if(tailResources.equals("/"))
 			tailResources = "";*/
 		if(tailResources.startsWith("/"))
 			tailResources = tailResources.substring(1, tailResources.length());
 	
 		//if we want the parameters left on the uri, put them back on	
 		if(!removeParams && paramsStr!=null)
 			tailResources = tailResources.concat(paramsStr);
 		
 		return tailResources;
 	}
 
 	/**
 	 * IS4 get.  Returns the connection object and populates the is4Resp string.
 	 */
 	public static HttpURLConnection is4ServerGet(String is4Url, StringBuffer is4Resp){
 		try {
 			logger.info("Symlink GET: " + is4Url);
 			if(is4Url != null && is4Resp != null){
 				try{
 					if(is4Url.startsWith("http://")){
 						URL is4UrlObj = new URL(is4Url);
 
 						HttpURLConnection is4Conn = (HttpURLConnection) is4UrlObj.openConnection();
 						is4Conn.setConnectTimeout(5000);
 						is4Conn.setDoOutput(true);
 						is4Conn.connect();
 
 						//Populate reply
 						BufferedReader reader = new BufferedReader(new InputStreamReader(is4Conn.getInputStream()));
 						String line = null;
 						while((line = reader.readLine()) != null)
 							is4Resp.append(line);
 						logger.info("Symlink GET RESPONSE: " + is4Resp.toString());
 						return is4Conn;
 					}
 				} catch(Exception e){
 					logger.log(Level.WARNING, "", e);
 					return null;
 				}
 			}
 			logger.warning("is4Url and/or postData is null");
 			return null;
 		} catch (Exception e){
 			logger.log(Level.WARNING, "", e);
 			return null;
 		}
 	}
 
 	/**
 	 * IS4 post.  Returns the connection object and populates the is4Resp string.
 	 */
 	public static HttpURLConnection is4ServerPost(String is4Url, String postData, StringBuffer is4Resp){
 		try {
 			if(is4Url != null && postData != null && is4Resp != null){
 				try{
 					if(is4Url.startsWith("http://")){
 						URL is4UrlObj = new URL(is4Url);
 
 						HttpURLConnection is4Conn = (HttpURLConnection) is4UrlObj.openConnection();
 						is4Conn.setConnectTimeout(5000);
 						is4Conn.setDoOutput(true);
 						is4Conn.connect();
 
 						OutputStreamWriter wr = new OutputStreamWriter(is4Conn.getOutputStream());
 						wr.write(postData);
 						wr.flush();
 
 						//Populate reply
 						BufferedReader reader = new BufferedReader(new InputStreamReader(is4Conn.getInputStream()));
 						String line = null;
 						while((line = reader.readLine()) != null)
 							is4Resp.append(line);
 						return is4Conn;
 					}
 				} catch(Exception e){
 					logger.log(Level.WARNING, "", e);
 					return null;
 				}
 			}
 			logger.warning("is4Url and/or postData is null");
 			return null;
 		} catch(Exception e){
 			logger.log(Level.WARNING, "", e);
 			return null;
 		}
 	}
 
 	/**
 	 * IS4 put.  Returns the connection object and populates the is4Resp string.
 	 */
 	public static HttpURLConnection is4ServerPut(String is4Url, String putData, StringBuffer is4Resp){
 		try{
 			if(is4Url != null && putData != null && is4Resp != null){
 				try{
 					if(is4Url.startsWith("http://")){
 						URL is4UrlObj = new URL(is4Url);
 
 						HttpURLConnection is4Conn = (HttpURLConnection) is4UrlObj.openConnection();
 						is4Conn.setConnectTimeout(5000);
 						is4Conn.setDoOutput(true);
 						is4Conn.setRequestMethod("PUT");
 						is4Conn.connect();
 
 						OutputStreamWriter wr = new OutputStreamWriter(is4Conn.getOutputStream());
 						wr.write(putData);
 						wr.flush();
 
 						//Populate reply
 						BufferedReader reader = new BufferedReader(new InputStreamReader(is4Conn.getInputStream()));
 						String line =null;
 						while((line = reader.readLine()) != null)
 							is4Resp.append(line);
 						return is4Conn;
 					}
 				} catch(Exception e){
 					logger.log(Level.WARNING, "", e);
 					return null;
 				}
 			}
 			logger.warning("is4Url and/or postData is null");
 			return null;
 		} catch (Exception e){
 			logger.log(Level.WARNING, "", e);
 			return null;
 		}
 	}
 
 	/**
 	 * IS4 delete.  Returns the connection object and populates the is4Resp string.
 	 */
 	public static HttpURLConnection is4ServerDelete(String is4Url, StringBuffer is4Resp){
 		try {
 			if(is4Url != null && is4Resp != null){
 				try{
 					if(is4Url.startsWith("http://")){
 						URL is4UrlObj = new URL(is4Url);
 
 						HttpURLConnection is4Conn = (HttpURLConnection) is4UrlObj.openConnection();
 						is4Conn.setConnectTimeout(5000);
 						is4Conn.setRequestMethod("DELETE");
 						is4Conn.connect();
 
 						//Populate reply
 						BufferedReader reader = new BufferedReader(new InputStreamReader(is4Conn.getInputStream()));
 						String line = null;
 						while((line = reader.readLine()) != null)
 							is4Resp.append(line);
 						return is4Conn;
 					}
 				} catch(Exception e){
 					logger.log(Level.WARNING, "", e);
 					return null;
 				}
 			}
 			logger.warning("is4Url and/or postData is null");
 			return null;
 		} catch(Exception e){
 			logger.log(Level.WARNING, "", e);
 			return null;
 		}
 	}
 
 	private String processMidStarReq(String transReqUri){
 		String resultStr = null;
 		logger.fine("Symlink_:: Request w/out params: " + transReqUri);
 		logger.fine("Symlink_:: Resource URI: " + URI);
 		//replace the starred portion with the portion of this URI
 		// exmaple::  	
 		//		request: 	/is4/*/slink/all 
 		//		this uri: 	/is4/test/slink/
 		//		result:		/is4/test/slink/all
 		Vector<String> reqTokVec = createTokenVector(transReqUri, "*");
 		Vector<String> uriTokVec = createTokenVector(URI, "/");
 
 		if(reqTokVec.size()>0 && uriTokVec.size()>0){
 			String lastReqElt = (String) reqTokVec.get(reqTokVec.size()-1);
 			String lastUriElt = (String) uriTokVec.get(uriTokVec.size()-1);
 			logger.fine("Symlink_:: Request lastToken: " + lastReqElt);
 			logger.fine("Symlink_:: Resource URI lastToken " + lastUriElt);
 
 			if(transReqUri.contains(lastUriElt)){
 				String reqPrefix = transReqUri.substring(0, transReqUri.indexOf(lastUriElt));
 				String uriPrefix = URI.substring(0, URI.indexOf(lastUriElt));
 				logger.fine("Symlink_:: Request Prefix: " + reqPrefix);
 				logger.fine("Symlink_:: Resource URI Prefix: " + uriPrefix);
 
 				resultStr = transReqUri.replace(reqPrefix, uriPrefix);
 			} else {
 				logger.fine("Symlink_:: Resource URI: " + URI + " does NOT match " + transReqUri);
 			}
 
 		}
 		return resultStr;
 	}
 
 	private Vector<String> createTokenVector(String target, String delim){
 		StringTokenizer tokens = new StringTokenizer(target, delim);
 		Vector<String> allToks  = new Vector<String>();
 		while(tokens.hasMoreTokens())
 			allToks.add((String)tokens.nextToken());
 		return allToks;
 	}
 }
