 package test;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Writer;
 import java.sql.Connection;
 import java.sql.Statement;
 import java.util.StringTokenizer;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.sql.DataSource;
 
 /**
  * Servlet implementation class dbCreate
  */
 public class dbCreate extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public dbCreate() {
 		super();
		// TODO Auto-generated constructor stub
 		System.out.println("dbCreate----");
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
 		Writer out = response.getWriter();
 		out.write("dbCreate-doGet");
 	}
 
 	@Override
 	public void init() throws ServletException {
		// TODO Auto-generated method stub
 	super.init();
		//InputStream inputStream = this.getClass().getResourceAsStream("../resource/database.txt");
 		InputStream inputStream = this.getClass().getResourceAsStream("../resource/CreateAllTables.txt");
		String str = FileManager.getStringFromFile(inputStream);
		StringTokenizer st = new StringTokenizer(str, ";");
 		Connection conn = null;
 		try {
 			Context initContext = new InitialContext();
 			Context envContext = (Context) initContext.lookup("java:/comp/env");
 			DataSource ds = (DataSource) envContext.lookup("jdbc/eiskit");
 			conn = ds.getConnection();
 			System.out.println("...");
 			Statement statement = conn.createStatement();
 
 			while (st.hasMoreElements()) {
 				String strSql = st.nextToken();
 				if (!strSql.trim().equals("")){
 					statement.executeUpdate(strSql);	
 				}
 				
 			}
		} catch (Exception e) {
 			e.printStackTrace();
		} finally {
 			if (conn != null) {
 				try {
 					conn.close();		
				} catch (Exception e2) {
 					e2.printStackTrace();
 				}
 			
 			}
 		}
 
 		System.out.println("init----");
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		Writer out = response.getWriter();
 		out.write("dbCreate-doPost");
 	}
 
 }
