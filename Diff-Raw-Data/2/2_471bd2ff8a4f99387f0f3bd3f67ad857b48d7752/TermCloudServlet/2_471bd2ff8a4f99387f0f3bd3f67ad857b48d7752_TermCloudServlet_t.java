 package com.rtsearch.ui;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.rtsearch.dao.RedisDataStore;
 import com.rtsearch.dao.TwitterDAO;
 
 /**
  * Servlet implementation class HelloServlet
  */
 @WebServlet("/cloud")
 public class TermCloudServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	private static final int LIMIT = 15;
     private final TwitterDAO dao;   
     /**
      * @see HttpServlet#HttpServlet()
      */
     public TermCloudServlet() {
         super();
         this.dao = new TwitterDAO(new RedisDataStore());
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		response.setContentType("application/json");
         response.setStatus(HttpServletResponse.SC_OK);
         
         response.getWriter().print("[" + getTopKeywords() + "]");
         
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 	
 	private final String getTopKeywords()  {
 		StringBuilder result = new StringBuilder();
 		int count = 1;
 		for(String keyword: this.dao.getPupularSearchKeywords(3, 500, LIMIT)) {
			result.append(String.valueOf("\"" + keyword + "\""));
 			if(count < LIMIT) {
 				result.append(",");
 				count++;
 			}
 		}
 		return result.toString();
 	}
 }
