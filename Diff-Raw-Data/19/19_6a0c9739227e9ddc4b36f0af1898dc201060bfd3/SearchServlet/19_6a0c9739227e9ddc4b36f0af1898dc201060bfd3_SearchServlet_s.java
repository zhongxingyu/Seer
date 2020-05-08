 package servlet;
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Properties;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import db.DBInterface;
 
 import models.Entertainment;
 
 /**
  * Servlet implementation class SearchServlet
  */
 @WebServlet(name = "search", urlPatterns = { "/search" })
 public class SearchServlet extends HttpServlet {
     private static final long serialVersionUID = 1L;
     private DBInterface db = null;
     /**
      * Default constructor.
      */
     public SearchServlet() {
         db = new DBInterface();
     }
 
     /**
      * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
      */
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         // TODO Auto-generated method stub
     }
 
     /**
      * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
      */
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         // TODO Auto-generated method stub
         response.setContentType("text/html; charset=utf-8");
         request.setCharacterEncoding("UTF-8");
 
         HttpSession session = request.getSession();
         String query = request.getParameter("searchWord");
         query = query.replaceAll(".*([';]+).*", " ");
 
         System.out.println(query);
 
         ArrayList<Entertainment> result = db.getEntertainmentByName(query);
 
         if (result.size() == 0) {
             result = db.getEntertainmentByKeyword(query);
         }
         session.setAttribute("query", query);
        session.setAttribute("type", "关键词");
         session.setAttribute("list", result);
         response.sendRedirect("list.jsp");
     }
 
 }
