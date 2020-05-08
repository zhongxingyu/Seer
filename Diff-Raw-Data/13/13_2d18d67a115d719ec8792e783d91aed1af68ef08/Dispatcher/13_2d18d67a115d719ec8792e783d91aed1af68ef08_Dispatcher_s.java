 package org.geoserver.request;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.AbstractController;
 import org.vfny.geoserver.ServiceException;
 import org.vfny.geoserver.global.GeoServer;
 import org.vfny.geoserver.servlets.AbstractService;
 import org.vfny.geoserver.util.requests.EncodingInfo;
 import org.vfny.geoserver.util.requests.XmlCharsetDetector;
 import org.vfny.geoserver.util.requests.readers.DispatcherKvpReader;
 import org.vfny.geoserver.util.requests.readers.DispatcherXmlReader;
 import org.vfny.geoserver.util.requests.readers.KvpRequestReader;
 
 
 
 /**
  * Root dispatcher which handles all incoming requests.
  * <p>
  * 
  * </p>
  * 
  * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
  *
  */
 public class Dispatcher extends AbstractController {
 
 	GeoServer geoServer;
 	
 	public Dispatcher ( GeoServer geoServer ) {
 		this.geoServer = geoServer;
 	}
 	
 	protected ModelAndView handleRequestInternal(
 		HttpServletRequest httpRequest, HttpServletResponse httpResponse
 	) throws Exception {
 		
 		try {
 			try {
 				dispatch( httpRequest, httpResponse );	
 			}
 			catch( IOException e ) {
 				throw new ServiceException( e );
 			}
 			catch( ServletException e ) {
 				throw new ServiceException( e );
 			}
 		}
 		catch(ServiceException se) {
 			String tempResponse = 
 				se.getXmlResponse(geoServer.isVerboseExceptions(), httpRequest, geoServer);
 
             httpResponse.setContentType(geoServer.getCharSet().toString());
             httpResponse.getWriter().write(tempResponse);
 		}
 		
 		return null;
 	}
 	
 	AbstractService find(String service, String request) throws ServiceException {
 		//map request parameters to a Request bean to handle it 
 		Map requests = 
 			//getApplicationContext().getBeansOfType(AbstractService.class);
 			getApplicationContext().getParent().getBeansOfType(AbstractService.class);
 		
 		List matches = new ArrayList();
 		for (Iterator itr = requests.entrySet().iterator(); itr.hasNext();) {
 			Map.Entry entry = (Entry) itr.next();
 			
 			AbstractService bean = (AbstractService) entry.getValue();
 			
 			//we allow for a null service
 			if (service == null) {
 				if (bean.getRequest().equalsIgnoreCase(request)) {
 					//we have a winner
 					matches.add(bean);
 				}
 			}
 			else {
 				if (bean.getService().equalsIgnoreCase(service) && 
 					bean.getRequest().equalsIgnoreCase(request)) {
 					
 					//we have a winner
 					matches.add(bean);
 				}	
 			}
 		}
 		
 		if (matches.isEmpty())
 			return null;
 		
 		if (matches.size() > 1) {
 			String msg = "Multiple requests found capable of handling:" + 
 			" (" + service + "," + request + ")"; 
 			throw new ServiceException( msg );
 		}
 		
 		return (AbstractService) matches.get(0);
 	}
 	
 	void dispatch(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
 		throws ServiceException, IOException, ServletException {
 		
 		String service = null;
 		String request = null;
 		
 		if (httpRequest.getQueryString() != null) { 
 			//process query string params
 			Map kvp = KvpRequestReader.parseKvpSet(httpRequest.getQueryString());
 			
 			//figure out service and request being processed
 			service = (String) kvp.get("SERVICE");
 			request = (String) kvp.get("REQUEST");
 		}
 		
 		if (service == null || request == null) {
 			//lookup in context path, (http://.../geoserver/service/request?)
 			
 			String path = httpRequest.getContextPath();
			String uri = httpRequest.getRequestURI();
			if (uri.length() > path.length()) {
				uri = uri.substring(path.length()+1);	
 			}
 			
 			int index = uri.indexOf('/'); 
 			if ( index != -1 ) {
 				//take everything before the slash to be the service and 
 				// everything after to be the request
 				if (service == null)
 					service = uri.substring(0,index);
 				
 				if (request == null)
 					request = uri.substring(index+1);
 			}
 			else {
 				//just take the whole string to be the service
 				if (service == null) {
 					service = uri;
 				}
 			}
 		}	
 		
 		/**
 		 * ALFA: this is a HACK to let GeoServer do a getCapabilities request by default.
 		 */
 		/*request = (request == null ? "GetCapabilities" : request);*/
 		
 		
 		if (service == null || request == null) {
 			//check for a POST request specifying the request as the 
 			if ("POST".equalsIgnoreCase(httpRequest.getMethod())) {
 				post(httpRequest,httpResponse);
 				return;
 			}
 		}
 		
 		AbstractService target = find(service,request);
 		
 		if (target != null) {
 			//we have a servlet, do it
 			if ("GET".equalsIgnoreCase(httpRequest.getMethod())) {
 				target.doGet(httpRequest,httpResponse);
 			}
 			else if ("POST".equalsIgnoreCase(httpRequest.getMethod())) {
 				
 				target.doPost(httpRequest,httpResponse);
 			}
 			else {
 				String msg = "Unkown request method: " + httpRequest.getMethod();
 				throw new ServiceException( msg );
 			}
 		}
 		else {
 			String msg = "Could not locate service mapping to: (" 
 				+ service + "," + request + ")";
 			throw new ServiceException( msg );
 		}
 		
 	}
 	
 	//static int sequence = 0;
 	void post(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
 		throws ServiceException, IOException, ServletException {
 		File temp;
     
         InputStream is = 
         		new BufferedInputStream(httpRequest.getInputStream());
 
         // REVISIT: Should do more than sequence here
         // (In case we are running two GeoServers at once)
         // - Could we use response.getHandle() in the filename?
         // - ProcessID is traditional, I don't know how to find that in Java
         long sequence = new Date().getTime();
         // test to see if we have permission to write, if not, throw an appropirate error
         try {
             	temp = File.createTempFile("dispatch" + sequence, "tmp");
             	if (!temp.canRead() || !temp.canWrite())
             	{
             		String errorMsg = "Temporary-file permission problem for location: " + temp.getPath();
                 	throw new IOException(errorMsg);
             	}
         } 
         catch (IOException e) {
             	String errorMsg = "Possible file permission problem. Root cause: \n" + e.toString();
             	IOException newE = new IOException(errorMsg);
             	throw newE;
         }
         
         FileOutputStream fos = new FileOutputStream(temp);
         BufferedOutputStream out = new BufferedOutputStream(fos);
 
         int c;
 
         while (-1 != (c = is.read())) {
             out.write(c);
         }
 
         is.close();
         out.flush();
         out.close();
 
         //JD: GEOS-323, Adding char encoding support
         EncodingInfo encInfo = new EncodingInfo();
 
         BufferedReader disReader;
         BufferedReader requestReader;
 
         try {
             disReader = new BufferedReader(
                     XmlCharsetDetector.getCharsetAwareReader(
                             new FileInputStream(temp), encInfo));
 
             requestReader = new BufferedReader(
                     XmlCharsetDetector.createReader(
                             new FileInputStream(temp), encInfo));
         } catch (Exception e) {
             /*
              * Any exception other than WfsException will "hang up" the
              * process - no client output, no log entries, only "Internal
              * server error". So this is a little trick to make detector's
              * exceptions "visible".
              */
             throw new ServiceException(e);
         }
 
         AbstractService target = null;
         //JD: GEOS-323, Adding char encoding support
         boolean kvpRequestContent = false;
         if (disReader != null) {
         	try {
         		DispatcherXmlReader requestTypeAnalyzer = new DispatcherXmlReader();
         		requestTypeAnalyzer.read(disReader, httpRequest);
 
         		target = find(requestTypeAnalyzer.getService(),requestTypeAnalyzer.getRequest());
         	} catch(ServiceException e) {
         		DispatcherKvpReader requestTypeAnalyzer = new DispatcherKvpReader();
         		requestTypeAnalyzer.read(requestReader, httpRequest);
         		
         		target = find(requestTypeAnalyzer.getService(),requestTypeAnalyzer.getRequest());
         		target.setKvpString(requestTypeAnalyzer.getQueryString());
         		kvpRequestContent = true;
         	}
 		} 
        
         if (target == null) {
         		String msg = "Could not locate service for request";
         		throw new ServiceException( msg );
         }
         
         if (!kvpRequestContent)
         	if (requestReader != null) {
         		target.doPost(httpRequest,httpResponse,requestReader);
         	}
         	else {
         		target.doPost(httpRequest,httpResponse);
         	}
         else
         	target.doGet(httpRequest, httpResponse);
 
         disReader.close();
         requestReader.close();
         temp.delete();
     }
 }
