 package gov.usgs;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.net.URLConnection;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import static gov.usgs.HTTPParameters.ExtParam.*;
 
 
 public class IdentifyDataProxy extends HttpServlet {
 	
 	private static Logger logger = LoggerFactory.getLogger(IdentifyDataProxy.class);
 
 	private static final long serialVersionUID = 1L;
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		resp.setContentType("text/xml");
 
 		RequestType serviceRequest = RequestType.valueOf(req.getParameter("request"));
 		String url = serviceRequest.makeCacheRESTUrl(AGENCY_CODE.parse(req), SITE_NO.parse(req));
 
 		logger.info("gw_data_portal fetching {} data from get url: {}", serviceRequest, url);
 
 		PrintWriter writer = null;
 		try {
 			URL urlObject = new URL(url);
 			URLConnection conn = urlObject.openConnection();
 
 			InputStream is = conn.getInputStream();
 			try {
 				OutputStream os = resp.getOutputStream();
 				try {
 					long ct = copy(is, os);
 					logger.info("copied {} bytes", ct);
 				} finally {
 					os.close();
 				}
 			} finally {
 				is.close();
 			}
 
 		} catch (Exception e) {
 			logger.error("failed " + url, e);
 		} finally {
 			if (writer != null) writer.close();
 		}
 
 		logger.info("Done get for {}",url);
 	}
 
 	private long copy(InputStream is, OutputStream os) throws IOException {
		// Don't need to buffer; both streams are already buffered.
 		long ct = 0;
 		
 		while (true) {
 			int c = is.read();
 			if (c < 0) {
 				break;
 			}
 			ct++;
 			os.write(c);
 		}
 		return ct;
 	}
 	
 	
 }
