 package main.web;  
 
 import java.io.*;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import main.util.DBConnection;
 import main.util.Filter;
 
 public class Search extends HttpServlet {
     private static final long serialVersionUID = 1L;
 
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		request.getRequestDispatcher("/Search.jsp").forward(request, response);
 	}
     
     public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         
     	 String keywords = request.getParameter("keywords");
          String fromDate = request.getParameter("fromDate");
          String toDate = request.getParameter("toDate");
          String sort = request.getParameter("SortBy");
 
          String query = "SELECT * FROM images";
       
          if(!keywords.isEmpty()){
        	 query += " WHERE (CONTAINS(subject, ?, 1) > 0 OR CONTAINS(place, ?, 2) > 0 OR CONTAINS(description, ?, 3) > 0 )";
          }
          
          if(!fromDate.isEmpty() && keywords.isEmpty()){
         	 query += " WHERE timing >=  TO_DATE( ? , 'DD/MM/YYYY HH24:MI:SS')";
          }else if(!fromDate.isEmpty() && !keywords.isEmpty()){
         	 query += " AND timing >= TO_DATE( ? , 'DD/MM/YYYY HH24:MI:SS')";
          }
          if(!toDate.isEmpty() && keywords.isEmpty() && fromDate.isEmpty()){
         	 query += " WHERE timing <= TO_DATE( ? , 'DD/MM/YYYY HH24:MI:SS')";
          }else if(!toDate.isEmpty()){
         	 query += " AND timing <=  TO_DATE( ? , 'DD/MM/YYYY HH24:MI:SS')";
          }
 
          if(sort.equals("Rank")){
                          query += " ORDER BY ((6*SCORE(1))+(3*SCORE(2))+SCORE(3)) DESC";
          }else if(sort.equals("New")){
                          query += " ORDER BY CASE WHEN timing IS NULL THEN 1 ELSE 0 END, timing DESC";
          }else if(sort.equals("Old")){
                          query += " ORDER BY CASE WHEN timing IS NULL THEN 1 ELSE 0 END, timing ASC";
          }
 
          Connection myConn = null;
          try{
         	 	myConn = DBConnection.createConnection();
         	 	ArrayList<String> matchingIds = new ArrayList<String>();
         	 	PreparedStatement myQuery = myConn.prepareStatement(query);
         	 	int n = 1;
         	 	if(!keywords.isEmpty()){
         	 		myQuery.setString(n, keywords);
         	 		n++;
     				myQuery.setString(n, keywords);
     				n++;
     				myQuery.setString(n, keywords);
     				n++;
         	 	}
         	 	if(!fromDate.isEmpty()){
         	 		myQuery.setString(n, fromDate);
     				n++;
         	 	}
         	 	if(!toDate.isEmpty()){
         	 		myQuery.setString(n, toDate);
     				n++;
         	 	}
         	 	ResultSet results = myQuery.executeQuery();
         		HttpSession session = request.getSession();
                 String currentUser = (String) session.getAttribute("username");
         	 	while (results.next()) {
         	 		String foundId = Integer.toString(results.getInt("photo_id"));
         	 		if(Filter.isViewable(currentUser, foundId)){
         	 			matchingIds.add(foundId);
         	 		}
         	 	}
         	 	request.setAttribute("matchingIds", matchingIds);
                 myQuery.close();
          } catch(Exception ex) {
                  System.err.println("Exception: " + ex.getMessage());
          } finally {
              try {
                  myConn.close();
              } catch (Exception ex) {
             	 System.err.println("Exception: " + ex.getMessage());
              }
          }
          request.getRequestDispatcher("/SearchResults.jsp").forward(request, response);
     }
 }
