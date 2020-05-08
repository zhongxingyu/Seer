 package gov.usgs;
 
 
 import java.io.IOException;
 import java.net.URL;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 
 public class MVServlet extends HttpServlet {
 
 	public static final String MAPPING_SERVER = DebugSettings.MAPPING_SERVER;
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		resp.setContentType("image/png");   
 
 		String params = req.getQueryString();
 		URL baseUrl = new URL("http",req.getServerName(),req.getServerPort(),req.getContextPath() + "/templates");
 		try {
 			String mapreq = "";
 
 			//fetch base query
 			String query = URLUtil.getResponseAsStringFromURL(baseUrl.toExternalForm() + "/base_query.jsp;jsessionid=" + req.getSession().getId(), params + "&queryId=map");
 			//System.out.println(query.replaceAll("%2526lt;", "<").replaceAll("%252B", "+").replaceAll("FROM", "FROM\n"));	
 			//fetch xml request
 			mapreq =  URLUtil.getResponseAsStringFromURL(baseUrl.toExternalForm() + "/base_map_request.jsp;jsessionid=" + req.getSession().getId(), params + "&query=" + query + "&requestId=map");      
 			//System.out.println(mapreq.replaceAll(">", ">\n"));	
 			if (DebugSettings.isTraceSet) {
 				System.out.println("requesting maptile[" + mapreq + "] from " + MAPPING_SERVER  );
 			}
 
 			//String errorResponse = URLUtil.getResponseAsStringFromURL(MAPPING_SERVER,"");
 			//POST xml request to mapviewer server and return byte stream (image)
 			URLUtil.writeBytesToOutputStream(MAPPING_SERVER, "xml_request=" + mapreq, resp.getOutputStream());
 		} catch (Exception e) {
 			System.out.println("Error retrieving tile: " + e.getMessage()); //nothing
 		}
 	}
 }
