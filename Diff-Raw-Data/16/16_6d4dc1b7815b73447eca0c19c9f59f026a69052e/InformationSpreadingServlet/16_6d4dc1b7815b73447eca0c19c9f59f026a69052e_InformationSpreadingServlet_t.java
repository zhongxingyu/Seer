 package de.hpi.InformationSpreading;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URLDecoder;

 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
import java.sql.Timestamp;
 import java.util.ArrayList;

import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.gson.Gson;
 
 /**
  * Servlet implementation class InformationSpreading
  */
 // @WebServlet("/InformationSpreading")
 public class InformationSpreadingServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	DBConnection conn = null;
 	private PreparedStatement toUrlStmt;
 	private PreparedStatement fromUrlStmt;
 	private PreparedStatement scoreStmt;
 
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 		
 		Properties p;
 		try {
 			InputStream input = getServletContext().getResourceAsStream("/WEB-INF/config.properties");
 			p = new Properties();
 			p.load(input);
 			conn = new DBConnection();
 			conn.connect(p.getProperty("DRIVERCLASS"), p.getProperty("DBURL"), p.getProperty("DBUSER"), p.getProperty("DBPASSWORD"));
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}		
 		
 		try {
 			toUrlStmt = conn.getCon()
 					.prepareStatement("SELECT TOP 20 ID, SCORE FROM (SELECT TOURL FROM SYSTEM.LINK WHERE FROMURL = ? ) AS A JOIN WEBPAGE ON A.TOURL = WEBPAGE.ID WHERE SCORE IS NOT NULL ORDER BY SCORE DESC");
 			fromUrlStmt = conn.getCon()
 					.prepareStatement("SELECT TOP 20 ID, SCORE FROM (SELECT FROMURL FROM SYSTEM.LINK WHERE TOURL = ? ) AS A JOIN WEBPAGE ON A.FROMURL = WEBPAGE.ID WHERE SCORE IS NOT NULL ORDER BY SCORE DESC");
 			scoreStmt = conn.getCon()
 					.prepareStatement("SELECT SCORE FROM SYSTEM.WEBPAGE WHERE ID = ?");
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 //	public void doGet(HttpServletRequest request, HttpServletResponse response)
 //			throws ServletException, IOException {
 //
 //		try {
 //			String param = request.getParameter("tohost");
 //			String tohost = "";
 //			if (param != null) {
 //				tohost = URLDecoder.decode(param);
 //			}
 //
 ////		byte[] csv = conn.getHeatMapAsTSV(keywords).getBytes();
 //			
 //			
 //			stmt.setString(1, tohost);
 //			ResultSet executeQuery = stmt.executeQuery();
 //			ArrayList<Link> list = new ArrayList<Link>();
 //			while(executeQuery.next()){
 //				String fromurl = executeQuery.getString(1);
 //				String tourl = executeQuery.getString(2);
 //				Link link = new Link(tourl, fromurl);
 //				list.add(link);			
 //			}
 //			
 //			Gson gson = new Gson();
 //			String json = gson.toJson(list);
 //			response.setContentType("text/json");
 //			response.addHeader("Access-Control-Allow-Origin", "*");
 //			response.getWriter().print(json);
 //		} catch (SQLException e) {
 //			e.printStackTrace(response.getWriter());
 //		}
 		
 		
 		public void doGet(HttpServletRequest request, HttpServletResponse response)
 				throws ServletException, IOException {
 
 			String param = request.getParameter("id");
 			String id = "";
 			if (param != null) {
 				id = URLDecoder.decode(param, "UTF-8");
 			}	
 			
 			String outputJson = "";
 			
 			//id = "de.zeit.www:http/digital/datenschutz/2012-11/google-transparency-report-2012";
 			InputJsonContainer inputContainer = null;
 			try {
 				inputContainer = fetchPostDataFromBlogIntelligenceServer(id);
 			} catch (Exception e) {
 				inputContainer = new InputJsonContainer();
 				inputContainer.setResult(false);
 				e.printStackTrace();
 			}
 			
 			if(inputContainer.isResult()) {
 				
 				OutputJsonContainer outputContainer = new OutputJsonContainer();
 				ArrayList<Post> posts = new ArrayList<Post>();
 				outputContainer.setPosts(posts);
 				
 				outputContainer.getPosts().add(populateContainerComplete(inputContainer, id).getPost());
 				
 				for (String link : inputContainer.getPost().getIncomingLinks()) {
 					outputContainer.getPosts().add(getPopulatedContainerWithScore(link).getPost());				
 				}
 				
 				for (String link : inputContainer.getPost().getOutgoingLinks()) {
 					outputContainer.getPosts().add(getPopulatedContainerWithScore(link).getPost());				
 				}				
 				
 				outputJson = new Gson().toJson(outputContainer);	
 				
 			}
 			else {
 				outputJson = new Gson().toJson(inputContainer); 
 			
 			}
 
 
 			response.setContentType("text/json");
 			response.addHeader("Access-Control-Allow-Origin", "*");
 			response.getWriter().print(outputJson);		
 		
 
 	
 	}
 		
 	private InputJsonContainer fetchPostDataFromBlogIntelligenceServer(String id) throws Exception {
 		
 		String link = "http://blog-intelligence.com:8080/bi_small/hc?type=post&id=" + id;
 		
 		String result = StaticHelpers.getContentFromUrl(link);			
 	
 		InputJsonContainer inputContainer = new Gson().fromJson(result, InputJsonContainer.class);
 	
 		return inputContainer;			
 	}
 	
 	private InputJsonContainer populateContainerComplete(InputJsonContainer inputContainer, String id) {
 				
 		Float score = null;
 		ArrayList<String> outgoingLinks = null;
 		ArrayList<String> incomingLinks = null;
 		try {
 			score = fetchScoreFromDB(id);
 			outgoingLinks = fetchOutgoingLinksFromDB(id);
 			incomingLinks = fetchIncomingLinksFromDB(id);
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}	
 		
 		inputContainer.getPost().setId(id);				
 		inputContainer.getPost().setScore(score);
		//inputContainer.getPost().setPubDate(parseDateStringFromDbToMilliseconds(inputContainer.getPost().getPubDate()));
 		inputContainer.getPost().setOutgoingLinks(outgoingLinks);
 		inputContainer.getPost().setIncomingLinks(incomingLinks);
 		
 		return inputContainer;
 		
 	}
 	
	private String parseDateStringFromDbToMilliseconds(String dateString){
//		Date date = new Date()
//		String.valueOf( Timestamp.valueOf( inputContainer.getPost().getPubDate() ).getTime() )
//		
		return "";
	}
	
 	private InputJsonContainer getPopulatedContainerWithScore(String id) {
 		
 		InputJsonContainer inputContainer = null;
 		try {
 			inputContainer = fetchPostDataFromBlogIntelligenceServer(id);
 			
 			if(inputContainer.isResult()) {
 			
 			Float score = null;
 			try {
 				score = fetchScoreFromDB(id);
 				
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}	
 			
 			inputContainer.getPost().setId(id);				
 			inputContainer.getPost().setScore(score);
 	
 		}
 		else {
 			
 			inputContainer.setPost(new Post());
 			inputContainer.getPost().setId(id);
 			
 		}
 		} catch (Exception e1) {
 			inputContainer = new InputJsonContainer();
 			inputContainer.setResult(false);
 			
 		}
 
 		return inputContainer;		
 	}
 	
 	private Float fetchScoreFromDB(String id) throws SQLException {
 		
 		String score = "";
 		
 		scoreStmt.setString(1, id);
 		ResultSet executeQuery = scoreStmt.executeQuery();
 		
 		while(executeQuery.next()){
 			score = executeQuery.getString(1);
 					
 		}
 		
 		if (score == null) return (float) 0;
 		else return Float.parseFloat(score);
 		
 	}
 	
 	private ArrayList<String> fetchOutgoingLinksFromDB(String id) throws SQLException {
 		
 		ArrayList<String> outgoingLinks = new ArrayList<String>();
 		
 		toUrlStmt.setString(1, id);
 		ResultSet executeQuery2 = toUrlStmt.executeQuery();
 		while(executeQuery2.next()){
 			String target = executeQuery2.getString(1);
 			outgoingLinks.add(target);			
 		}
 		
 		return outgoingLinks;
 	}
 	
 	private ArrayList<String> fetchIncomingLinksFromDB(String id) throws SQLException {
 		
 		ArrayList<String> incomingLinks = new ArrayList<String>();
 		
 		fromUrlStmt.setString(1, id);
 		ResultSet executeQuery3 = fromUrlStmt.executeQuery();
 		while(executeQuery3.next()){
 			String target = executeQuery3.getString(1);
 			incomingLinks.add(target);			
 		}
 		
 		
 		return incomingLinks;		
 		
 	}
 	
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void destroy() {
 		// conn.close();
 		super.destroy();
 	}
 
 	
 
 }
