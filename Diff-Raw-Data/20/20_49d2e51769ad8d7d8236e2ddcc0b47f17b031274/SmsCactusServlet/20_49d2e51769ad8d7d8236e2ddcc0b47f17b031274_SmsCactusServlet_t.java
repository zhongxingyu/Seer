 package org.ocactus.sms.server;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 import java.sql.DriverManager;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONTokener;
 import org.ocactus.sms.common.JSONUtils;
 import org.ocactus.sms.common.PendingSms;
 import org.ocactus.sms.common.Sms;
 
 public class SmsCactusServlet extends ServletBase {
 	
	private static final Charset ENCODING = Charset.forName("UTF-8");
 	private ISmsCactus server;
 	
 	public SmsCactusServlet() {
 		this(null);
 	}
 	
 	public SmsCactusServlet(ISmsCactus server) {
 		this.server = server;		
 	}
 	
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		if(server == null)
 		{
 			try {
 				 Class.forName(config.getInitParameter("dbDriver")).newInstance();
 				 this.server = new SmsCactus(DriverManager.getConnection(
 					config.getInitParameter("dbConnection"),
 					config.getInitParameter("dbUser"),
 					config.getInitParameter("dbPassword")));
 				 
 			} catch(Exception ex) {
 				throw new RuntimeException(ex);
 			}
 		}
 	}
	
	private static void setupResponse(HttpServletResponse resp) {
		resp.setCharacterEncoding(ENCODING.displayName());
		resp.setContentType("application/json");
	}
 
 	@Override
 	public void defaultAction(HttpServletRequest req, HttpServletResponse resp)
 		throws ServletException, IOException {
 		latest(req, resp);
 	}
 	
 	public void latest(HttpServletRequest req, HttpServletResponse resp)
 		throws ServletException, IOException {
 		try {
			setupResponse(resp);
 			
 			Sms[] latest = server.latest(25);
 			JSONArray data = JSONUtils.getJSONArray(latest);
 			data.write(resp.getWriter());
 			
 		} catch(JSONException ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 	
 	public void send(HttpServletRequest req, HttpServletResponse resp)
 		throws ServletException, IOException {
 		
 		String address = req.getParameter("address");
 		String text = req.getParameter("text");
 		
 		server.send(new PendingSms(address, text));
 		
 		resp.sendRedirect(req.getContextPath());
 	}
 	
 	public void sendlist(HttpServletRequest req, HttpServletResponse resp)
 		throws ServletException, IOException {
 		try {
			setupResponse(resp);
 			
 			PendingSms[] sendlist = server.sendlist();
 			JSONArray data = JSONUtils.getJSONArray(sendlist);
 			data.write(resp.getWriter());
 			
 		} catch(JSONException ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 	
 	public void archive(HttpServletRequest req, HttpServletResponse resp)
 		throws ServletException, IOException {
 		
 		try {
 			JSONArray data = new JSONArray(new JSONTokener(
				new InputStreamReader(req.getInputStream(), ENCODING)));
 			
 			server.archive(JSONUtils.getSms(data));
 			
 		} catch(JSONException ex) {
 			throw new RuntimeException(ex);
 		}
 	}
 }
