 package com.node.display;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.ws.rs.core.MultivaluedMap;
 
 import org.apache.cxf.jaxrs.impl.MetadataMap;
 
 import com.node.utilities.UtilityFunctionsImpl;
 
 /**
  * Servlet implementation class AuthLogin
  */
 @WebServlet("/DisplayNodesServlet")
 public class DisplayNodesServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private UtilityFunctionsImpl utility = new UtilityFunctionsImpl();
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public DisplayNodesServlet() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	@Override
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		this.doPost(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	@Override
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws ServletException, IOException {
 		// TODO Auto-generated method stub
 
 		String projectId= request.getParameter("projectId");
 		HttpSession session = request.getSession(true);
 		int personID = 0;
 		if (null != session.getAttribute("userID")) {
 			// already authenticated
 			personID = Integer.parseInt(session.getAttribute("userID")
 					.toString());
 			try {
 				HashMap<String,List<String>> nodemap=displayResults(personID,projectId);
 				if(!nodemap.isEmpty()){
 				setAttributes(personID, nodemap, request,
						response,projectId);
 				}else{
 					// no projects 
 					RequestDispatcher dispatcher = request.getRequestDispatcher("main.jsp");
 					dispatcher.forward(request, response);
 				}
 			} catch (ClassNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} else {
 			// needs to be authenticated
 			try {
 				String username = request.getParameter("username");
 				String password = request.getParameter("password");
 				personID = authLogin(username, password);
 				if (personID == 0) {
 					request.setAttribute("results", "none");
 					RequestDispatcher dispatcher = request
 							.getRequestDispatcher("index.jsp");
 					dispatcher.forward(request, response);
 
 				} else {
 					HttpSession newsession = request.getSession(false);
 					newsession.setAttribute("userID", personID);
 					HashMap<String, List> hashMap = new HashMap<String, List>();
 					try {
 						setAttributes(personID, displayResults(personID,projectId),
								request, response,projectId);
 
 					} catch (ClassNotFoundException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (SQLException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			} catch (ClassNotFoundException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			} catch (SQLException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		}
 
 	}
 
 	int authLogin(String username, String password)
 			throws ClassNotFoundException, SQLException {
 
 		int parseInt = 0;
 		Class.forName("com.mysql.jdbc.Driver");
 		Connection con = DriverManager
 				.getConnection("jdbc:mysql://localhost:3306/test");
 		// Connection con =
 		// DriverManager.getConnection("jdbc:mysql://localhost:3306/orbits?"
 		// +"user=orbits&password=orbits");
 		java.sql.PreparedStatement stat = con
 				.prepareStatement("select PersonID from Person where Username='"
 						+ username + "' and MyPassword='" + password + "'");
 		ResultSet result = stat.executeQuery();
 		while (result.next()) {
 			parseInt = Integer.parseInt(result.getString(1));
 		}
 		return parseInt;
 		// return ((Number) result.getObject(1)).intValue();
 
 	}
 
 	HashMap<String, List<String>> displayResults(int personId,String projectId)
 			throws ClassNotFoundException, SQLException {
 		Class.forName("com.mysql.jdbc.Driver");
 		Connection con = DriverManager
 				.getConnection("jdbc:mysql://localhost:3306/test");
 		// Connection con =
 		// DriverManager.getConnection("jdbc:mysql://localhost:3306/orbits?"
 		// +"user=orbits&password=orbits");
 		// java.sql.PreparedStatement stat=
 		// con.prepareStatement("select * from Node where AuthorId='"+personId+"' order by Parent ASC");
 		java.sql.PreparedStatement stat = con
 				.prepareStatement("select * from Node where ProjectId='"+projectId+"' order by Parent ASC");
 		ResultSet result = stat.executeQuery();
 		HashMap<String, List<String>> hashMap = new HashMap<String, List<String>>();
 		if( null != result ){
 		while (result.next()) {
 			List<String> messages = new ArrayList<String>();
 			messages.add(result.getString(2));
 			messages.add(result.getString(3));
 			messages.add(result.getString(5));
 			messages.add(result.getString(6));
 			messages.add(result.getString(7));
 			// add the author id
 			String authorId = result.getString(4);
 			java.sql.PreparedStatement authorname = con
 					.prepareStatement("select Username from Person where PersonID='"
 							+ authorId + "'");
 			ResultSet personname = authorname.executeQuery();
 			personname.first();
 			// add author name
 			String displayAuthorname = personname.getString(1);
 			StringBuilder sb = new StringBuilder(displayAuthorname);
 			sb.replace(0, 1, sb.substring(0, 1).toUpperCase());
 			messages.add(sb.toString());
 			// also add the author Id
 			messages.add(authorId);
 			// add upvote and downvote
 			messages.add(result.getString(8));
 			messages.add(result.getString(9));
 			// Arrays.asList(result.getString(2),
 			// result.getString(3),result.getString(5),result.getString(6),result.getString(7));
 			String nodeId = result.getString(1);
 			// Get and set the time
 			String dt = result.getString(10);
 			messages.add(utility.setDisplayTime(dt));
 			// Get the related Tags
 			retrieveTags(nodeId, messages);
 			hashMap.put(nodeId, messages);
 		}
 		}
 		return hashMap;
 	}
 
 	void retrieveTags(String nodeId, List<String> messages)
 			throws ClassNotFoundException, SQLException {
 		Class.forName("com.mysql.jdbc.Driver");
 		Connection con = DriverManager
 				.getConnection("jdbc:mysql://localhost:3306/test");
 		// Connection con =
 		// DriverManager.getConnection("jdbc:mysql://localhost:3306/orbits?"
 		// +"user=orbits&password=orbits");
 		java.sql.PreparedStatement stat = con
 				.prepareStatement("select TagID from NodeTagsCon where NodeID='"
 						+ nodeId + "'");
 		ResultSet tableTagids = stat.executeQuery();
 		if (!tableTagids.wasNull()) {
 			while (tableTagids.next()) {
 				int Tagid = tableTagids.getInt(1);
 				java.sql.PreparedStatement stat1 = con
 						.prepareStatement("select Tagname from Tags where TagID='"
 								+ Tagid + "'");
 				ResultSet tags = stat1.executeQuery();
 				tags.first();
 				String tagnameMsg = tags.getString(1);
 				// messages.add("tagnameMsg");
 				messages.add(tagnameMsg);
 			}
 		}
 	}
 
 	void setAttributes(int personID, HashMap<String, List<String>> result,
			HttpServletRequest request, HttpServletResponse response,String projectId)
 			throws ServletException, IOException, ClassNotFoundException,
 			SQLException {
 		// find number of parents
 		
 		// create new set of hashmaps for nodes based on no of parents
 
 		HashMap<String, List> hashMap0 = new HashMap<String, List>();
 		HashMap<String, List> hashMap1 = new HashMap<String, List>();
 		HashMap<String, List> hashMap2 = new HashMap<String, List>();
 		HashMap<String, List> hashMap3 = new HashMap<String, List>();
 		HashMap<String, List> hashMap4 = new HashMap<String, List>();
 		HashMap<String, List> hashMap5 = new HashMap<String, List>();
 		HashMap<String, List> hashMap6 = new HashMap<String, List>();
 		HashMap<String, List> hashMap7 = new HashMap<String, List>();
 		HashMap<String, List> hashMap8 = new HashMap<String, List>();
 
 		Iterator it = result.entrySet().iterator();
 		while (it.hasNext()) {
 			Map.Entry entry = (Map.Entry) it.next();
 			// Integer key = (Integer)entry.getKey();
 			List val = (List) entry.getValue();
 			// sorting and dividing the list by level
 			String parent = val.get(3).toString();
 
 			switch (Integer.parseInt(parent)) {
 			case 0: {
 
 				hashMap0.put(entry.getKey().toString(), val);
 				request.setAttribute("node0", hashMap0);
 				break;
 			}
 			case 1: {
 
 				hashMap1.put(entry.getKey().toString(), val);
 				break;
 
 			}
 			case 2: {
 
 				hashMap2.put(entry.getKey().toString(), val);
 				break;
 
 			}
 			case 3: {
 
 				hashMap3.put(entry.getKey().toString(), val);
 				break;
 
 			}
 			case 4: {
 
 				hashMap4.put(entry.getKey().toString(), val);
 				break;
 
 			}
 			case 5: {
 
 				hashMap5.put(entry.getKey().toString(), val);
 				break;
 
 			}
 			case 6: {
 
 				hashMap6.put(entry.getKey().toString(), val);
 				break;
 
 			}
 			case 7: {
 
 				hashMap7.put(entry.getKey().toString(), val);
 				break;
 
 			}
 			case 8: {
 
 				hashMap8.put(entry.getKey().toString(), val);
 				break;
 
 			}
 			}
 
 		}
 
 		// if(!flag1)
 		request.setAttribute("node1", hashMap1);
 		// if(!flag2)
 		request.setAttribute("node2", hashMap2);
 		// if(!flag3)
 		request.setAttribute("node3", hashMap3);
 		// if(!flag4)
 		request.setAttribute("node4", hashMap4);
 		// if(!flag5)
 		request.setAttribute("node5", hashMap5);
 		// if(!flag6)
 		request.setAttribute("node6", hashMap6);
 		// if(!flag7)
 		request.setAttribute("node7", hashMap7);
 		// if(!flag8)
 		request.setAttribute("node8", hashMap8);
 
 		// setting the comments
 		request.setAttribute("comments", retrieveComments());
		RequestDispatcher dispatcher = request.getRequestDispatcher("main.jsp?projectId="+projectId);
 		dispatcher.forward(request, response);
 	}
 
 	MultivaluedMap<String, List<String>> retrieveComments()
 			throws ClassNotFoundException, SQLException {
 		MultivaluedMap<String, List<String>> commentMap = new MetadataMap<String, List<String>>();
 		Class.forName("com.mysql.jdbc.Driver");
 		Connection con = DriverManager
 				.getConnection("jdbc:mysql://localhost:3306/test");
 		// Connection con =
 		// DriverManager.getConnection("jdbc:mysql://localhost:3306/orbits?"
 		// +"user=orbits&password=orbits");
 		PreparedStatement stat = con
 				.prepareStatement("select NodeID,CommentText,CreationTimeDate,AuthorID from NodeConvo");
 		ResultSet commentsRes = stat.executeQuery();
 		
 		if (null != commentsRes) {
 			while (commentsRes.next()) {
 				List<String> commentDet = new ArrayList<String>();
 				String nodeId = ((Integer) commentsRes.getInt(1)).toString();
 				// add comment
 				commentDet.add(commentsRes.getString(2));
 				// add date
 				commentDet
 						.add(utility.setDisplayTime(commentsRes.getString(3)));
 				// add author name
 				Connection con1 = DriverManager
 						.getConnection("jdbc:mysql://localhost:3306/test");
 				// Connection con =
 				// DriverManager.getConnection("jdbc:mysql://localhost:3306/orbits?"
 				// +"user=orbits&password=orbits");
 				
 				PreparedStatement stat1 = con1
 						.prepareStatement("select Username from Person where PersonID='"
 								+ commentsRes.getString(4) + "'");
 				ResultSet authorName = stat1.executeQuery();
 				authorName.first();
 				commentDet.add(authorName.getString(1));
 
 				// Adding to the Map
 				commentMap.add(nodeId, commentDet);
 			}
 		}
 		System.out.println("comment map=" + commentMap.toString());
 		return commentMap;
 	}
 
 	ArrayList<Integer> noofparents(HashMap<String, List<String>> result) {
 		Iterator it = result.entrySet().iterator();
 		ArrayList<Integer> parents = new ArrayList<Integer>();
 		while (it.hasNext()) {
 			Map.Entry entry = (Map.Entry) it.next();
 			// Integer key = (Integer)entry.getKey();
 			List val = (List) entry.getValue();
 			String dbParent = val.get(2).toString();
 			Integer x = Integer.valueOf(dbParent);
 			for (int i = 0; i <= parents.size(); i++) {
 				if (!parents.contains(x)) {
 					parents.add(x);
 					break;
 				}
 			}
 
 		}
 		return parents;
 	}
 
 }
