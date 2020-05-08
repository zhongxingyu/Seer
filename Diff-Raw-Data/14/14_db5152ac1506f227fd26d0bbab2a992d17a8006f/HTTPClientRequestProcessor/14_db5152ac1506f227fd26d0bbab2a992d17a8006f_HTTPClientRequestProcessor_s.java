 package com.gentics.cr.http;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Vector;
 
 import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.HttpVersion;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.apache.log4j.Logger;
 
 import com.gentics.cr.CRConfig;
 import com.gentics.cr.CRError;
 import com.gentics.cr.CRRequest;
 import com.gentics.cr.CRResolvableBean;
 import com.gentics.cr.RequestProcessor;
 import com.gentics.cr.exceptions.CRException;
 /**
  * 
  * Last changed: $Date: 2010-04-01 15:25:54 +0200 (Do, 01 Apr 2010) $
  * @version $Revision: 545 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public class HTTPClientRequestProcessor extends RequestProcessor {
 
 	private static Logger log = Logger.getLogger(HTTPClientRequestProcessor.class);
 	protected String name=null;
 	
 	private static final String URL_KEY = "URL";
 	
 	private String path="";
 	
 	protected HttpClient client;
 	
 	/**
 	 * Create new instance of HTTPClientRequestProcessor
 	 * @param config
 	 * @throws CRException
 	 */
 	public HTTPClientRequestProcessor(CRConfig config) throws CRException {
 		super(config);
 		this.name=config.getName();
 		//LOAD ADDITIONAL CONFIG
 		client = new HttpClient();
 		this.path = (String)config.get(URL_KEY);
 		if(this.path==null)log.error("COULD NOT GET URL FROM CONFIG (add RP.<rpnumber>.url=<url> to config). OVERTHINK YOUR CONFIG!");
 	}
 	
 		
 	private String encode(String str)
 	{
 		if(str!=null)
 		{
 			try {
 				return URLEncoder.encode(str, "UTF-8");
 			} catch (UnsupportedEncodingException e2) {
 				log.error(e2.getMessage());
 				e2.printStackTrace();
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Requests Objects from a remote ContentConnector Servlet using type JavaXML
 	 * @param request
 	 * @param doNavigation
 	 * @return Collection of CRResolvableBean
 	 * @throws CRException
 	 */
 	public Collection<CRResolvableBean> getObjects(CRRequest request, boolean doNavigation) throws CRException {
 			ArrayList<CRResolvableBean> resultlist = new ArrayList<CRResolvableBean>();
 		 	String additional = null;
 			String filter = "filter="+encode(request.getRequestFilter());
 			
 			String attributesstring = "";
 			for(String att:request.getAttributeArray())
 			{
 				attributesstring+="&attributes="+encode(att);
 			}
 			
 			String countstring="";
 			if(request.getCountString()!=null && !request.getCountString().equals(""))
 			{
 				countstring="&count="+encode(request.getCountString());
 			}
 			
 			String startstring="";
 			if(request.getStartString()!=null && !request.getStartString().equals(""))
 			{
 				startstring="&start="+encode(request.getStartString());
 			}
 			
 			String sortstring="";
 			if(request.getSortArray()!=null && request.getSortArray().length>0)
 			{
 				for(String sort:request.getSortArray()){
 					sortstring+="&sorting="+encode(sort);
 				}
 			}
 			
 			if(request.get(RequestProcessor.META_RESOLVABLE_KEY)!=null && (Boolean)request.get(RequestProcessor.META_RESOLVABLE_KEY))
 			{
 				if(additional==null)
 					additional="";
 				additional+="&"+RequestProcessor.META_RESOLVABLE_KEY+"=true";
 			}
 			
 			String highlightquery = (String)request.get(RequestProcessor.HIGHLIGHT_QUERY_KEY);
 			if(highlightquery!=null)
 			{
 				if(additional==null)
 					additional="";
 				additional+="&"+RequestProcessor.HIGHLIGHT_QUERY_KEY+"="+encode(highlightquery);
 			}
 			//TODO REQUEST ERWEITERN
 			String reqUrl = filter+attributesstring+startstring+countstring+sortstring+"&type=JavaBIN";
 			if(additional!=null) reqUrl +=additional;
 			
 			GetMethod method = new GetMethod(this.path+"?"+reqUrl);
 		    
 		    // Provide custom retry handler is necessary
 		 	method.getParams().setVersion(HttpVersion.HTTP_1_0);
 		 	
 		 	//Set request charset
 		 	method.setRequestHeader("Content-type","text/xml; charset=UTF-8");
 		 	
 		    method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
 		    		new DefaultHttpMethodRetryHandler(3, false));
 
 		    try {
 			      // Execute the method.
 			    int statusCode = client.executeMethod(method);
 	
 			    if (statusCode != HttpStatus.SC_OK) {
 			    	HTTPClientRequestProcessor.log.error("Request failed: " + method.getStatusLine());
 			    }
 	
 			    Collection<CRResolvableBean> result = new Vector<CRResolvableBean>();
 			    
 			    ObjectInputStream objstream = new ObjectInputStream(method.getResponseBodyAsStream());
 			    Object responseObject;
 				try {
 					responseObject = objstream.readObject();
 					
 				    objstream.close();
 	
 				     
 				    if(responseObject instanceof Collection<?>)
 				    {
 				    	result = this.toCRResolvableBeanCollection(responseObject);
 				    }
 				    else if(responseObject instanceof CRError)
 				    {
 				    	CRError ex = (CRError)responseObject;
 				    	throw new CRException(ex);
 				    }
 				    else
 				    {
 				    	HTTPClientRequestProcessor.log.error("COULD NOT CAST RESULT. Perhaps remote agent does not work properly");
 				    }
 			    
 				} catch (ClassNotFoundException e) {
					HTTPClientRequestProcessor.log.error("Coult not load object from http response: "+e.getMessage());
					e.printStackTrace();
 				}
 		      
 				if(result!=null)
 				{
 					for(CRResolvableBean crBean:result)
 					{
 						resultlist.add(crBean);
 					}
 				}
 
 		    } catch (HttpException e) {
		      System.err.println("Fatal protocol violation: " + e.getMessage());
		      e.printStackTrace();
 		    } catch (IOException e) {
		      System.err.println("Fatal transport error: " + e.getMessage());
		      e.printStackTrace();
 		    } finally {
 		      // Release the connection.
 		      method.releaseConnection();
 		    }  
 
 		return resultlist;
 	}
 
 
 	@Override
 	public void finalize() {
 		
 	}
 
 
 
 }
