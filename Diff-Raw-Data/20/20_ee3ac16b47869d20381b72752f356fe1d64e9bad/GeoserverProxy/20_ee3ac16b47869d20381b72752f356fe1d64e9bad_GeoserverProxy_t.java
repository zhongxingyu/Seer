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
 
 	@Override
 	public URL buildRequestURL(HttpServletRequest request, URL baseURL)
 			throws MalformedURLException {
		String uri = request.getRequestURI();
		uri = uri.replace("sediment", "geoserver");
		uri = uri.replace("map", "sedmap");
		String params = request.getQueryString();
		return new URL("http://localhost:8080" + uri +"?" +params);
 	}
 
 	@Override
 	public void doGet(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 
 		HttpURLConnection  targetConn = null; //Do not disconnect when done
 		OutputStreamWriter targetConnWriter = null; // Close when done
 		InputStream 	   targetIs = null; // Close when done
 		OutputStream       responseOutputStream = null;
 
 		URL targetURL     = buildRequestURL(request, null);
		String requestURI = request.getRequestURI() + "";
 
 
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
