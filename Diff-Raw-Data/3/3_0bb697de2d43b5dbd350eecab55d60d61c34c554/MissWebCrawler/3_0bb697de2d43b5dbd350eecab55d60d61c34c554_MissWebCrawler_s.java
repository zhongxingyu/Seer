 package com.zarcode.data.webcrawler;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.StringReader;
 import java.io.StringWriter;
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
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.tidy.Tidy;
 
 import com.zarcode.common.EscapeChars;
 import com.zarcode.common.Util;
 import com.zarcode.data.dao.ReportDao;
 import com.zarcode.data.exception.WebCrawlException;
 import com.zarcode.platform.loader.JDOLoaderServlet;
 import com.zarcode.data.model.ReportDO;
 
 public class MissWebCrawler extends WebCrawler {
 
 	private Logger logger = Logger.getLogger(MissWebCrawler.class.getName());
 	
 	private static final String PROVIDER = "mdwfp.com";
 	
 	private final String[] URL_LIST =  {
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=1",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=2",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=3",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=4",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=5",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=6",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=7",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=8",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=9",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=10",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=11",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=12",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=14",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=15",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=19",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=20",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=21",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=22",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=23",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=24",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=25",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=26",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=27",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=28",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=31",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=33",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=36",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=37",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=38",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=39",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=40",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=41",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=43",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=44",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=45",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=47",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=49",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=50",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=52",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=53",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=108",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=109",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=110",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=111",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=112",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=115",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=118",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=120",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=127",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=129",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=135",
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=137"
 	};
 	
 	/*
 	private final String[] URL_LIST =  {
 		"http://home.mdwfp.com/Fisheries/FishingReportsInfo.aspx?id=109",
 	};
 	*/
 	
 	public static final Map<Integer, Integer> CRAWL_MAP = new HashMap<Integer, Integer>()  {
         {
              put(Calendar.SUNDAY, 1);
              put(Calendar.MONDAY, 1);
              put(Calendar.TUESDAY, 1);
              put(Calendar.WEDNESDAY, 1);
         }
     };
 
 	private final String STATE = "MS";
 	
 	@Override
 	public boolean readyToCrawl() {
 		boolean flag = false;
 		Calendar now = Calendar.getInstance();
 	
 		if (isFeedUpdated(STATE)) {
 			logger.info("Feed is updated --> " + STATE);
 			return flag;
 		}
 		else {
 			Integer dayOfWeek = now.get(Calendar.DAY_OF_WEEK);
 			if (CRAWL_MAP.containsKey(dayOfWeek)) {
 				flag = true;
 			}
 		}
 		return flag;
 	}
 	
 	@Override
 	public  String convertStreamToString(InputStream is) throws Exception {
 	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
 	    StringBuilder sb = new StringBuilder();
 	    String line = null;
 	    while ((line = reader.readLine()) != null) {
 	    	line = line.replace("<p>", "");
 	    	line = line.replace("</p>", "");
 	    	sb.append(line + "\n");
 	    }
 	    is.close();
 	    return sb.toString();
 	}
 
 
 	@Override
     public void doCrawl(HttpServletRequest req) throws WebCrawlException {
     	int i = 0;
     	int j = 0;
     	int k = 0;
     	String msg = null;
     	String urlStr = null;
     	
     	try {
     		for (k=0; k<URL_LIST.length; k++) {
     			
     			urlStr = URL_LIST[k];
     			
     			logger.info("Processing URL: " + urlStr);
     			
 	            URL url = new URL(urlStr);
 	            
 	            InputStream is = url.openStream();
 	            String res = null;
 	            try {
 	            	res = convertStreamToString(is);
 	            }
 	            catch (Exception e) {
 	            }
 	            
 	            Tidy tidy = new Tidy();
 	            tidy.setMakeClean(true);
 	            tidy.setQuiet(false);
 	            tidy.setBreakBeforeBR(true);
 	            tidy.setOutputEncoding("latin1");
 	 			tidy.setXmlOut(false);
 	 			tidy.setNumEntities(true);
 	 			tidy.setDropFontTags(true);
 	 			tidy.setSpaces(2);
 	 			tidy.setIndentAttributes(false);
 	 	 	   	tidy.setHideComments(true);
 	 	 	   	tidy.setShowWarnings(false);
 	 	 	   	
 	 			OutputStream os = null;
 	 			StringReader r = new StringReader(res);
 	 			StringWriter w = new StringWriter();
 	 			Document doc = tidy.parseDOM(r, w);
 	 			Date reportDate = null;
 	 			
 	 			// 05/10/2010
 	 			DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
 					
 	
 	 			//
 	 			// try parsing out the reports
 	 			//
 	 			NodeList spanList = doc.getElementsByTagName("span");
 	 			if (spanList != null && spanList.getLength() > 0) {
 	 				logger.info("Found expected span tag(s) on the page");
 	 			}
 	 			else {
 	 				throw new WebCrawlException("Unexpected number of <table> tags", urlStr);
 	 			}
 	 		
 	 			ReportDO report = null;
 	 			Node tdTag = null;
 	 			ReportDao reportDao = new ReportDao();
	 			report.setReportedBy(PROVIDER);
 	 			String keyword = null;
 	 			String dateStr = null;
 	 			String reportStr = null;
 	 			List<String> textList = null;
 	 			NamedNodeMap attribMap = null;
 	 			//
 	 			// traverse span tags
 	 			//
 	 			for (i=0; i<spanList.getLength(); i++) {
 	 				Node node = spanList.item(i);
 	    	        if (node != null) {
 	    	        	attribMap = node.getAttributes();
 	    		    	Node id = attribMap.getNamedItem("id");
 	    		    	if (id != null) {
 	    		    		if (id.getNodeValue().equalsIgnoreCase("ctl00_ContentPlaceHolder1_FormView1_NameLabel")) {
 	    		    			keyword = getNodeContents(node);
 	    		    			logger.info("Found keyword: " + keyword);
 	    		    		}
 	    		    		else if (id.getNodeValue().equalsIgnoreCase("ctl00_ContentPlaceHolder1_FormView1_Label1")) {
 	    		    			dateStr = getNodeContents(node);
 	    		    			logger.info("Found dateStr: " + dateStr);
 	    	        			try {
 	    	        				reportDate = formatter.parse(dateStr);
 	    	        			}
 	    	        			catch (Exception e) {
 	    	        				throw new WebCrawlException(e.getMessage(), urlStr);
 	    	        			}
 	    		    		}
 	    		    		else if (id.getNodeValue().equalsIgnoreCase("ctl00_ContentPlaceHolder1_FormView1_FishingReportLabel")) {
 	    		    			reportStr = getNodeContents(node);
 	    	        			logger.info("Using report text: " + reportStr);
 	    		    			//
 		        				// create report object
 		        				//
 		        				report = new ReportDO();
 		        				report.setKeyword(keyword);
 		        				report.setReportDate(reportDate);
 		        				report.setReportBody(reportStr);
 		        				report.setState(STATE);
 		        				StringBuilder sb = new StringBuilder();
 								sb.append(STATE);
 								sb.append(":");
 								String uniqueKey = report.getKeyword();
 								uniqueKey= uniqueKey.toUpperCase();
 								uniqueKey = EscapeChars.forXML(uniqueKey);
 								sb.append(uniqueKey);
 								report.setReportKey(sb.toString());
 								reportDao.addOrUpdateReport(report);
 								break;
 	    		    		}
 	    		    		else {
 	    		    			throw new WebCrawlException("Unknown <span id='" + id.getNodeValue() + "'> tag; page might have changed.", urlStr);
 	    		    		}
 	    		    	}
 	    	        }
 	 			}
     		}
         } 
     	catch (MalformedURLException e) {
     		logger.severe(Util.getStackTrace(e));
     		throw new WebCrawlException(e.getMessage(), urlStr);
         }
     	catch (IOException e) {
     		logger.severe(Util.getStackTrace(e));
     		throw new WebCrawlException(e.getMessage(), urlStr);
         }
     } // doCrawl
 }
