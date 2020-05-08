 package servlet;
 
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import util.Config;
 
 import models.Entertainment;
 import models.Keyword;
 import models.Weibo;
 
 import db.DBInterface;
 
 /**
  * Servlet implementation class DetailServlet
  */
 @WebServlet(name = "detail", urlPatterns = { "/detail" })
 public class DetailServlet extends HttpServlet {
     private static final long serialVersionUID = 1L;
 
     private DBInterface db;
     private SimpleDateFormat sdf;
 
     /**
      * @see HttpServlet#HttpServlet()
      */
     public DetailServlet() {
         super();
         db = new DBInterface();
         sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     }
 
     /**
      * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
      */
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         HttpSession session = request.getSession();
         String id = request.getParameter("id");
         Entertainment en = db.getEntertainmentById(id);
 
         ArrayList<Weibo> posWeibo = new ArrayList<Weibo>();
         ArrayList<Weibo> negWeibo = new ArrayList<Weibo>();
 
         Date currentTime = null, deadline = null;
         try {
             currentTime = sdf.parse(Config.getProperties().getProperty("SYSTEM_TIME"));
            deadline = new Date(currentTime.getTime() - 15 * 24 * 3600 * 1000);
         } catch (ParseException e) {
             e.printStackTrace();
         }
 
         String query = "SELECT create_at, text, sentiment FROM weibo where target = '" + en.name + "' AND create_at BETWEEN '" + sdf.format(deadline) + "' AND '" + sdf.format(currentTime) + "' ORDER BY create_at DESC;";
         System.out.println(query);
 
         try {
             Statement st = db.getConnection().createStatement();
             ResultSet rs = st.executeQuery(query);
             while(rs.next()) {
                 Weibo weibo = new Weibo();
                 weibo.time = sdf.format(new Date(rs.getTimestamp(1).getTime()));
                 weibo.text = rs.getString(2);
                 weibo.emotion = rs.getInt(3);
                 if (weibo.emotion > 0) {
                     posWeibo.add(weibo);
                 } else if (weibo.emotion < 0) {
                     negWeibo.add(weibo);
                 }
             }
             st.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         query = "SELECT keyword from keyword WHERE target = '" + en.name + "';";
         ArrayList<Keyword> keywordList = new ArrayList<Keyword>();
         try {
             Statement st = db.getConnection().createStatement();
             ResultSet rs = st.executeQuery(query);
             String keywords = null;
             while(rs.next()) {
                 keywords = rs.getString(1);
             }
             String[] words = keywords.split("::;");
             for (String w : words) {
                 Keyword k = new Keyword();
                 String[] sp = w.split("::=");
                 k.word = sp[0];
                 k.weight = (int)(Integer.valueOf(sp[1]) / 10) + 15;
                 keywordList.add(k);
             }
             st.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         session.setAttribute("pos_percent", new Integer((int)(1.0 * posWeibo.size() / (posWeibo.size() + negWeibo.size()) * 100)));
         session.setAttribute("entertainment", en);
         session.setAttribute("keywords", keywordList);
         session.setAttribute("pos", posWeibo);
         session.setAttribute("neg", negWeibo);
 
         response.sendRedirect("detail.jsp");
     }
 
     /**
      * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
      */
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         // TODO Auto-generated method stub
     }
 
 }
