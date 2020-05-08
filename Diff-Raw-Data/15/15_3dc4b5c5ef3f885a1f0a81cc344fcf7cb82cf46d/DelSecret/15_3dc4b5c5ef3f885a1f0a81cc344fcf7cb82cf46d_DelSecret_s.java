 package com.miracle.secretra;
 
 import info.xmark.base.DBPool;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 
 /**
  * Servlet implementation class DelSecret
  */
 public class DelSecret extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	private static Logger log = Logger.getLogger(DelSecret.class);
 
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 		String id = request.getParameter("id");
 		PrintWriter out = response.getWriter();
 		if (id != null && !id.equals("")) {
 
 			Connection conn = DBPool.getInstance().getConnection();
 			String sql = "update secret set own=0 where id=?";
 			PreparedStatement pstmt = null;
 			ResultSet rs = null;
 
 			try {
 				pstmt = conn.prepareStatement(sql);
 				pstmt.setInt(1, Integer.parseInt(id));
 				out.print(pstmt.executeUpdate());
 			} catch (Exception e) {
 				DBPool.getInstance().close(pstmt, rs, conn);
 			}
 		}
 		log.info(id);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		this.doGet(request, response);
 	}
 
 }
