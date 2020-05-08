 package com.bixito.db;
 
 import java.io.IOException;
import javax.servlet.http.*;
 
 public class BixiDataServlet extends HttpServlet {
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		try {
 			resp.getWriter().println(DB.getStationsData());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
