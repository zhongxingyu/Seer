 package com.node.create;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import com.node.display.DisplayNodesServlet;
 import com.node.utilities.UtilityFunctionsImpl;
 
 /**
  * Servlet implementation class MyServlet
  */
 @WebServlet("/MyServlet")
 public class MyServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private DisplayNodesServlet authLogin = new DisplayNodesServlet();
 	private UtilityFunctionsImpl utility = new UtilityFunctionsImpl();
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public MyServlet() {
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
 		authLogin.doPost(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	@Override
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 
 		HttpSession session = request.getSession();
 		String userId = session.getAttribute("userID").toString();
 		String title = request.getParameter("title");
 		String parentId = request.getParameter("parentId");
 		String description = request.getParameter("description");
 		List<String> tags = new ArrayList<String>();
 		if (!request.getParameter("tag1").isEmpty())
 			tags.add(request.getParameter("tag1"));
 		if (!request.getParameter("tag2").isEmpty())
 			tags.add(request.getParameter("tag2"));
 		if (!request.getParameter("tag3").isEmpty())
 			tags.add(request.getParameter("tag3"));
 		// System.out.println("tag null-"+ request.getParameter("tag3"));
 		// String lastname= request.getParameter("last_name");
 
 		try {
			int nodeId = insertvalues(title, parentId, description, userId);
 
 			inserttagvalues(tags, nodeId);
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		// request.
 		RequestDispatcher dispatcher = request
				.getRequestDispatcher("/CreateNewTree");
 		dispatcher.forward(request, response);
 	}
 
 	int insertvalues(String title, String parentId, String description,
			String userId) throws ClassNotFoundException, SQLException,
 			ParseException {
 		Class.forName("com.mysql.jdbc.Driver");
 		Connection con = DriverManager
 				.getConnection("jdbc:mysql://localhost:3306/test");
 		// Connection con =
 		// DriverManager.getConnection("jdbc:mysql://localhost:3306/orbits?"
 		// +"user=orbits&password=orbits");
 		// System.out.println("parentId--"+parentId);
 		// to get the level no and children number of the parent
 		java.sql.PreparedStatement stat = con
 				.prepareStatement("select Levelno,countChildren from Node where NodeID='"
 						+ parentId + "'");
 		ResultSet result = stat.executeQuery();
 		int levelNo = 0;
 		int countChildren = 0;
 		int authorId = Integer.parseInt(userId);
 		while (result.next()) {
 			levelNo = result.getInt(1);
 			// levelNo=Integer.parseInt(result.getString(1));
 			countChildren = Integer.parseInt(result.getString(2));
 		}
 
 		levelNo++;
 		countChildren++;
 		// to update the countchildren of parent node
 		java.sql.PreparedStatement stat1 = con
 				.prepareStatement("UPDATE Node SET countChildren="
 						+ countChildren + " Where NodeID='" + parentId + "'");
 		stat1.executeUpdate();
 		String dt = utility.getCuttentDateTime();
 		String[] escapetexts = utility.setEcsapeTitleDesc(title, description);
 		java.sql.PreparedStatement stat2 = con
				.prepareStatement("INSERT INTO Node(Title,Description,AuthorId,Parent,Levelno,countChildren,UpVote,DownVote,CreationTimeDate) VALUES ('"
 						+ escapetexts[0]
 						+ "','"
 						+ escapetexts[1]
 						+ "',"
 						+ authorId
 						+ ","
 						+ parentId
 						+ ","
 						+ levelNo
						+ ",0,0,0,'" + dt + "');");
 		stat2.executeUpdate();
 		// get the node id
 		java.sql.PreparedStatement stat3 = con
 				.prepareStatement("select NodeID from test.Node where title='"
 						+ escapetexts[0] + "' and Description='"
 						+ escapetexts[1] + "'");
 		ResultSet result1 = stat3.executeQuery();
 		result1.first();
 		int nodeId = result1.getInt(1);
 		return nodeId;
 	}
 
 	void inserttagvalues(List<String> tags, int nodeId)
 			throws ClassNotFoundException, SQLException {
 		Class.forName("com.mysql.jdbc.Driver");
 		Connection con = DriverManager
 				.getConnection("jdbc:mysql://localhost:3306/test");
 		// Connection con =
 		// DriverManager.getConnection("jdbc:mysql://localhost:3306/orbits?"
 		// +"user=orbits&password=orbits");
 		// get all the tags and id's
 		java.sql.PreparedStatement stat = con
 				.prepareStatement("select * from Tags");
 		ResultSet result = stat.executeQuery();
 		Iterator it = tags.listIterator();
 		while (it.hasNext()) {
 			String inputTag = it.next().toString();
 			int tagid = 0;
 			// result.first();
 			boolean tagexists = false;
 
 			while (result.next()) {
 				tagid = result.getInt("TagID");
 				// System.out.println("here the tag--it.next().toString()-"+inputTag);
 				if (result.getString("Tagname").toString()
 						.equalsIgnoreCase(inputTag)) {
 					tagexists = true;
 					// int tagid=result.getInt("TagID");
 					break;
 				}
 			}
 
 			if (tagexists) {
 				// insert tag id and node id in the connection table
 				java.sql.PreparedStatement stat2 = con
 						.prepareStatement("INSERT INTO NodeTagsCon VALUES ("
 								+ tagid + "," + nodeId + ")");
 				stat2.executeUpdate();
 			} else {
 				// insert into tag table
 				java.sql.PreparedStatement stat2 = con
 						.prepareStatement("INSERT INTO Tags(Tagname) VALUES ('"
 								+ inputTag.toLowerCase() + "')");
 				stat2.executeUpdate();
 				// get tag id
 				java.sql.PreparedStatement stat4 = con
 						.prepareStatement("select TagID from Tags where Tagname='"
 								+ inputTag.toLowerCase() + "'");
 				ResultSet result4 = stat4.executeQuery();
 				result4.first();
 				tagid = result4.getInt(1);
 				// insert into node connection table
 				java.sql.PreparedStatement stat3 = con
 						.prepareStatement("INSERT INTO NodeTagsCon VALUES ("
 								+ tagid + "," + nodeId + ")");
 				stat3.executeUpdate();
 
 			}
 			result.first();
 		}
 	}
 
 }
