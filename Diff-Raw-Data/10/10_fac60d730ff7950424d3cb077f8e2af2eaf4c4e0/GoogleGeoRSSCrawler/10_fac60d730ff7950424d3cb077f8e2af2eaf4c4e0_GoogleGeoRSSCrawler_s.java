 package com.zarcode.data.webcrawler;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.CharacterData;
 import org.w3c.dom.Element;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import com.google.appengine.api.labs.taskqueue.Queue;
 import com.google.appengine.api.labs.taskqueue.QueueFactory;
 import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.*;
 
 import com.google.apphosting.api.DeadlineExceededException;
 import com.zarcode.app.AppCommon;
 import com.zarcode.common.EmailHelper;
 import com.zarcode.common.EscapeChars;
 import com.zarcode.common.Util;
 import com.zarcode.data.dao.ReportDao;
 import com.zarcode.data.dao.WaterResourceDao;
 import com.zarcode.data.exception.WebCrawlException;
 import com.zarcode.platform.loader.JDOLoaderServlet;
 import com.zarcode.data.model.ReportDO;
 import com.zarcode.data.model.WaterResourceDO;
 
 import ch.hsr.geohash.WGS84Point;
 
 /**
  * This is a web GeoRSS Webcrawler for accessing Google Maps feeds.
  * 
  * @author Administrator
  */
 public class GoogleGeoRSSCrawler extends WebCrawler {
 
 	private Logger logger = Logger.getLogger(GoogleGeoRSSCrawler.class.getName());
 	
 	private final String[] URL_LIST =  {
 			// Southwest Region
 			"http://maps.google.com/maps/ms?ie=UTF8&hl=en&vps=2&jsv=242c&msa=0&output=georss&msid=115945530916463422167.0004881ff70543eab31e6"
 	};
 	
  
 	@Override
     public void doCrawl(HttpServletRequest req) throws WebCrawlException {
     	int i = 0;
     	int j = 0;
     	int k = 0;
     	String msg = null;
     	String urlStr = null;
     	String rssLink = null;
     	String rssTitle = null;
     	String rssDesc = null;
     	String temp = null;
 		int itemCount = 0;
  		int memberCount = 0;
  		Node itemNode = null;
  		WaterResourceDO res = null;
  		HashMap props = null;
  		List<WGS84Point> polygon = null;
 		OutputStream os = null;
  		Document doc = null;
  		int MAX_TIME_THREHOLD = 20;
  		String[] targetList = null;
  	
  		// starting timestamp
  		Date startTimestamp = new Date();
  		
  		String urlParam = req.getParameter("url");
  		String startIndexParam = req.getParameter("start");
  		int startingIndex = Integer.parseInt(startIndexParam);
  		logger.info("Starting index --> " + startingIndex + " Parameter Map: " + req.getParameterMap().toString());
  		
  		if (urlParam != null) {
  			targetList = new String[1];
  			logger.info("Using user-provided URL: " + urlParam);
  			targetList[0] = urlParam;
  		}
  		else {
  			targetList = URL_LIST;
  			logger.info("Using default URL(s)." + urlParam);
  		}
     	
     	try {
     		
     		for (k=0; k<targetList.length; k++) {
     			
     			// get URLs
     			urlStr = targetList[k];
     			
     			logger.info("Google map: " + urlStr);
     			
 	            URL url = new URL(urlStr);
 	            InputStream is = url.openStream();
 
 	            
 	 			//
 	            // load XML RSS feed into local DOM
 	            //
 	 			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		        factory.setNamespaceAware(true);
 		        factory.setIgnoringComments(true);
 		        factory.setIgnoringElementContentWhitespace(true);
 		        DocumentBuilder builder = factory.newDocumentBuilder();
 	 			doc = builder.parse(is);
 	 			
 	 			Node n = null;
 	 			WaterResourceDao dao = new WaterResourceDao();
 
 	 			///////////////////////////////////////////////////////////////////////////////
 	 			//
 	 			// Process header of Geo RSS feed
 	 			//
 	 			///////////////////////////////////////////////////////////////////////////////
 	 		
 	 			String nodeName = null;
 	 			Date now = null;
 	 			NodeList channelList = doc.getElementsByTagName("channel");
 	 			
 	 			if (channelList != null && channelList.getLength() > 0) {
 	 				
 	 				logger.info("Found channel node --> " + channelList.getLength());
 	 				Node header = channelList.item(0);
 	 				NodeList headerChildren = header.getChildNodes();
 	 				logger.info("# of children in <channel> node: " + headerChildren.getLength());
 	 				
     	        	for (j=0; j<headerChildren.getLength(); j++) {
     	        		
     	        		
     	        		n = headerChildren.item(j);
     	        		logger.info("Found <channel> children node: " + n.getNodeName());
     	        		nodeName = n.getNodeName();
     	        		if (nodeName != null) {
     	        			nodeName = nodeName.trim();
     	        		}
     	        		if ("link".equalsIgnoreCase(nodeName)) {
     	        			rssLink = n.getFirstChild().getNodeValue();
     	        			logger.info("Saving header value for link: " + rssLink);
     	        		}
     	        		else if ("title".equalsIgnoreCase(nodeName)) {
     	        			rssTitle = n.getFirstChild().getNodeValue();
     	        			logger.info("Saving header value for title: " + rssTitle);
     	        		}
     	        		else if ("description".equalsIgnoreCase(nodeName)) {
         					rssDesc = getCharacterDataFromElement((Element)n);
         					logger.info("Saving header value for description: " + rssDesc);
     	        			break;
     	        		}
     	        	}
 	 			}
 	 			else {
 	 				logger.warning("Unable to channel node");
 	 				throw new WebCrawlException("Unable to find <channel> node in RSS feed.", urlStr);
 	 			}
 	 			logger.info("HEADER: [ link: " + rssLink + " title: " + rssTitle + " desc: " + rssDesc + " ]");
 	 			
 	 			///////////////////////////////////////////////////////////////////////////////
 	 			//
 	 			// Process actual data in the Geo RSS feed
 	 			//
 	 			///////////////////////////////////////////////////////////////////////////////
 
 	 			NodeList lakeItemList = doc.getElementsByTagName("item");
 	 			if (lakeItemList != null && lakeItemList.getLength() > 0) {
 	 				itemCount = lakeItemList.getLength();
 	 				for (i=startingIndex; i<itemCount; i++) {
 	 				
 	 					/**
 	 					 * check if we are about to the Google Timeout Threshold
 	 					 */
 	 					now = new Date();
     	        		long durationInSecs = (now.getTime() - startTimestamp.getTime())/1000;
     	        		if (durationInSecs > MAX_TIME_THREHOLD) {
     	        			logger.warning("Hitting ending of processing time -- Queuing task to handle late!");
     	            		Queue queue = QueueFactory.getDefaultQueue();
     	            		String nextIndex = "" + i;
     	            		queue.add(url("/georssload").param("url", urlParam).param("start", nextIndex));
     	            		return;
     	        		}
     	        		else {
     	        			logger.info(i + ") Time is still good ---> " + durationInSecs);
     	        		}
     	        		
 	 					itemNode = lakeItemList.item(i);
 	 					
 	 					///////////////////////////////////////////////////////////////////////
 	 					//
 	 					// Process each lake item data item
 	 					//
 	 					///////////////////////////////////////////////////////////////////////
 	 					
 	 					if (itemNode != null) {
 	 						
 	 						// create water resource
 	 						res =  new WaterResourceDO();
 	 						res.setLastUpdate(new Date());
 	 						res.setRegion(rssTitle);
 	 						
 	 						NodeList itemMembers = itemNode.getChildNodes();
 	 						memberCount = itemMembers.getLength();
 	 						for (j=0; j<memberCount; j++) {
 	 							n = itemMembers.item(j);
 	 							if (n != null) {
 	 								if ("guid".equalsIgnoreCase(n.getNodeName())) {
 	 	    	        				temp = n.getFirstChild().getNodeValue();
 	 	    	        				logger.info("Found guid=" + temp);
 	 	    	        				res.setGuid(temp);
 	 	    	        			}
 	 	    	        			else if ("title".equalsIgnoreCase(n.getNodeName())) {
 	 	    	        				temp = n.getFirstChild().getNodeValue();
 	 	    	        				logger.info("Found title=" + temp);
 	 	    	        				res.setName(temp);
 	 	    	        				res.setContent(temp);
 	 	    	        			}
 	 	    	        			else if ("author".equalsIgnoreCase(n.getNodeName())) {
 	 	    	        				temp = n.getFirstChild().getNodeValue();
 	 	    	        				logger.info("Found author=" + temp);
 	 	    	        			}
 	 	    	        			else if ("description".equalsIgnoreCase(n.getNodeName())) {
 	 	           						String descData = getCharacterDataFromElement((Element)n);
 	 	    	        				props = convertKVString2HashMap(descData);
 	 	    	        				if (props != null && props.containsKey("reportKey")) {
 	 	    	        					res.setReportKey((String)props.get("reportKey"));
 	 	    	        				}
 	 	    	        				else {
 	 	    	        					logger.warning("reportKey not found for resource=" + res.getName());
 	 	    	        				}
 	 	    	        			}
 	 	    	        			else if ("gml:Polygon".equalsIgnoreCase(n.getNodeName())) {
 	 	    	        				List<String> textList = new ArrayList<String>();
 	 	    	        				findMatchingNodes(n, Node.TEXT_NODE, textList);
 	    	        					if (textList != null && textList.size() > 0) {
 	    	        						String polygonStr = textList.get(0);
 	    	        						polygonStr = polygonStr.trim();
 	    	        						polygon = convertGMLPosList2Polygon(polygonStr);
 	    	        						logger.info("Converted incoming polygonStr into " + polygon.size() + " object(s).");
 	    	        						res.setPolygon(polygon);
 	    	        					}
 	 	    	        			}
 	 							}
 	 						}
 	 						//
 	 						// add water resource to model
 	 						//
 	 						logger.info("Inserting resource into model!");
 	 						dao.insertResource(res);
 	 					}
 	 				}
 	 				logger.info("Processing is done on index=" + i);
 	 				EmailHelper.sendAppAlert(AppCommon.APPNAME + ": GeoRSSFeed Status", "Completed GeoRSS processing\nURL: " + url + "\nLAST INDEX: " + i, AppCommon.APPNAME);
 	 			}
 	 			else {
 	 				throw new WebCrawlException("GeoRSS Feed is empty!", urlStr);
 	 			}
 	 			
     		} // for loop for Map URLs
         } 
     	catch (DeadlineExceededException e1) {
     		logger.warning("Google HTTP Request Timeout has fired ... ");
     		Queue queue = QueueFactory.getDefaultQueue();
     		queue.add(url("/georssload").param("url", urlParam).param("start", "0"));
     	}
     	catch (ParserConfigurationException e) {
     		logger.severe("Unable to create XML parser to start DOM loading. [EXCEPTION]\n" + Util.getStackTrace(e));
     		throw new WebCrawlException(e.getMessage(), urlStr);
         }
     	catch (SAXException e) {
     		logger.severe("Unable to parse incoming XML GeoRSS document. [EXCEPTION]\n" + Util.getStackTrace(e));
     		throw new WebCrawlException(e.getMessage(), urlStr);
         }
     	catch (MalformedURLException e) {
     		logger.severe(Util.getStackTrace(e));
     		throw new WebCrawlException(e.getMessage(), urlStr);
         }
     	catch (IOException e) {
     		logger.severe(Util.getStackTrace(e));
     		throw new WebCrawlException(e.getMessage(), urlStr);
         }
     }
 	
 	public String getCharacterDataFromElement(Element elem) {
 		Node child = elem.getFirstChild();
 	    if (child instanceof CharacterData) {
 	    	CharacterData cd = (CharacterData) child;
 	    	String str = cd.getData();
 	    	// take of html
 	    	str = str.substring(15);
 	    	str = str.substring(0, str.length()-6);
 	    	str = str.trim();
 	    	logger.info("Data --> " + str);
 	    	return str;
 	    }
 	   	logger.warning("First child is not instanceof 'CharacterData'");
 	    return "";
 	}
 	
 	private List<WGS84Point> convertGMLPosList2Polygon(String dataStr) {
 		int i = 0;
 		List<WGS84Point> res = null;
 		String latLngStr = null;
 		double lat = 0;
 		double lng = 0;
 		WGS84Point pt = null;
 		
 		if (dataStr != null) {
 			res = new ArrayList<WGS84Point>();
 			dataStr = dataStr.trim();
 			String[] pointList = dataStr.split("\n"); 
 			if (pointList != null && pointList.length > 0) {
				for (i=0; i<pointList.length; i++) {
 					latLngStr = pointList[i];
 					latLngStr = latLngStr.trim();
 					String[] latLngList = latLngStr.split(" ");
 					// should have individual points here
 					if (latLngList != null && latLngList.length == 2) {
 						lat = Double.parseDouble(latLngList[0]);
 						lng = Double.parseDouble(latLngList[1]);
 						pt = new WGS84Point(lat, lng);
 						res.add(pt);
 					}
 					else {
 						logger.warning("LatLng format is not as expected --> " + latLngStr);
 					}
 				}
 			}
 			else {
 				logger.warning("LatLng List format is not as expected --> " + dataStr);
 				
 			}
 		}
 		
 		return res;
 	}
 	
 	private HashMap<String, String> convertKVString2HashMap(String dataStr) {
 		int i = 0;
 		String nvPair = null;
 		HashMap<String, String> map = null;
 
 		if (dataStr != null) {
 			map = new HashMap<String, String>();
 			String[] nameValueList = dataStr.split("\n"); 
 			if (nameValueList != null && nameValueList.length > 0) {
 				for (i=0; i<nameValueList.length; i++) {
 					nvPair = nameValueList[i];
 					String[] keyValue = nvPair.split("=");
 					if (keyValue != null && keyValue.length == 2) {
 						logger.info("Adding key=" + keyValue[0] + " val=" + keyValue[1]);
 						map.put(keyValue[0], keyValue[1]);
 					}
 				}
 			}
 		}
 		return map;
 	}
 }
