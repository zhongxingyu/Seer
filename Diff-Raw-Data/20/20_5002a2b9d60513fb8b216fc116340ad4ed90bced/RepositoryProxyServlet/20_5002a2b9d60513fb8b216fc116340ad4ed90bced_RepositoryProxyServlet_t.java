 package kornell.server;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import kornell.api.client.KornellClient;
 import kornell.core.util.StringUtils;
 
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.CloseableHttpClient;
 import org.apache.http.impl.client.HttpClients;
 import org.apache.http.util.EntityUtils;
 
 public class RepositoryProxyServlet extends HttpServlet{
 	@Override
 	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
 			throws ServletException, IOException {
		String distributionURL = "http://eduvem.com";			
		String uri = req.getRequestURI(); 
		StringBuilder log = new StringBuilder();
 		CloseableHttpClient httpclient = HttpClients.createDefault();
 		try {
 			HttpGet httpget = new HttpGet(StringUtils.composeURL(distributionURL,uri));
 
			log.append("Requesting " + uri);
 
 			// Create a custom response handler
 			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
 
 				public String handleResponse(
 						final HttpResponse response)
 						throws ClientProtocolException, IOException {
 					int status = response.getStatusLine().getStatusCode();
 					if (status >= 200 && status < 300) {
 						HttpEntity entity = response.getEntity();
 						Header[] headers = response.getHeaders("Content-Type");
 						if(headers.length > 0) {
 						    String ctype = headers[0].toString();
 						    //TODO: Set Correct Content Type
 							resp.setHeader("Content-Type", ctype);
 						} 
 						return entity != null ? EntityUtils.toString(entity)
 								: null;
 					} else {
 						throw new ClientProtocolException(
 								"Unexpected response status: " + status);
 					}					
 				}
 
 			};
 			String responseBody = httpclient.execute(httpget, responseHandler);
			if(uri.endsWith("html")){
				resp.setContentType("text/html");
			}else if (uri.endsWith("css")){
				resp.setContentType("text/css");
			}else if (uri.endsWith("js")){
				resp.setContentType("application/javascript");
			}
			resp.setCharacterEncoding("UTF-8");
					
			System.out.println(log);
 			resp.getWriter().print(responseBody);
 		} finally {
 			httpclient.close();
 		}
 	}
 }
