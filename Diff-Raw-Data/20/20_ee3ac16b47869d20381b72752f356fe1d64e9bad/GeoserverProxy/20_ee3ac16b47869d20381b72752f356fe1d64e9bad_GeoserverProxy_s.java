 package gov.cida.sedmap.web;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import gov.usgs.cida.proxy.ProxyServlet;
 
 
 public class GeoserverProxy extends ProxyServlet {
 
 	private static final long serialVersionUID = 1L;
 
	private static final String HTML_MIME_TYPE = "text/html";
	private static final String UTF_8          = "UTF-8";

	// TODO need this in dev env -- cida-wiwsc-sedmapdev.er.usgs.gov
	private static final String PROXIED_URL    = "http://localhost:8080/geoserver/upload/wms?service=WMS&version=1.1.0&request=GetMap&layers=upload:SM_SITE_REF&styles=&bbox=-172.953934831794,18.032464,-58.1941695891842,70.4953759999999&width=1000&height=550&srs=EPSG:4269&format=application/openlayers";


 	@Override
 	public URL buildRequestURL(HttpServletRequest request, URL baseURL)
 			throws MalformedURLException {

		return new URL(PROXIED_URL);
 	}
 

 	@Override
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 
 		HttpURLConnection  targetConn = null; //Do not disconnect when done
 		OutputStreamWriter targetConnWriter = null; // Close when done
 		InputStream 	   targetIs = null; // Close when done
 		OutputStream       responseOutputStream = null;
 
 		URL targetURL     = buildRequestURL(request, null);
		String requestURI = request.getRequestURI();
 
 
 		try {
 			int connTimeout = strategy.getConnectTimeout(requestURI);
 			int requTimeout = strategy.getReadTimeout(requestURI);
 
 
 			targetConn = getConnection(targetURL, "GET", connTimeout, requTimeout);
 			// Copy headers from the incoming request to the forwarded request
 			copyHeaders(request, targetConn);
 
 			// Applications can override the next method to addStrategy app specific properties
 			addCustomRequestHeaders(request, targetConn);
 
 			// Establish connection to forwarded server
 			targetConn.setDoOutput(true);
 			targetConn.connect();
 
 			// No custom params are defined, so we can just copy the body of
 			// the request from the incoming request to the forwarded request
 			copyStreams(request.getInputStream(), targetConn.getOutputStream());
 
 			// Copy header back from the forwarded response to the servlet response
 			copyHeaders(targetConn, response);
 
 			response.setStatus(targetConn.getResponseCode());
 			responseOutputStream = response.getOutputStream();
 			targetIs = targetConn.getInputStream();
 			copyStreams(targetIs, responseOutputStream);
 
 			//		} catch (IOException e) {
 			//			log.warn("An exception was thrown in the Proxy Servlet", e);
 			//			if (log.isDebugEnabled()) {
 			//				dumpRequest(request);
 			//				//attempting to access these seems to cause another error
 			//				//dumpURLConnProperties(targetConn);
 			//			}
 		} catch (Exception e) {
 			log.warn("An exception was thrown in the Proxy Servlet", e);
 			if (log.isDebugEnabled()) {
 				dumpRequest(request);
 				//attempting to access these seems to cause another error
 				//dumpURLConnProperties(targetConn);
 			}
 
 			//This err is caught before writing to the output stream, so send err content
 			response.setStatus(targetConn.getResponseCode());
 			handleErrorStream(targetConn, responseOutputStream);
 		} finally {
 			if (targetConnWriter != null) {
 				//This is likely not an err at all - could already be closed.
 				try { targetConnWriter.close(); } catch (Exception e) {}
 			}
 
 			if (targetIs != null) {
 				//This is likely not an err at all - could already be closed.
 				try { targetIs.close(); } catch (Exception e) {}
 			}
 		}
 	}
 
 }
