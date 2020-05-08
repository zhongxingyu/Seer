 package servlets;
 
 import db.DB;
 import db.RealDB;
 import model.User;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 //Created By Ilan Godik
 public class ResetAnswersServlet extends HttpServlet {
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         User user = (User) req.getSession().getAttribute("user");
         if (user != null) {
             DB db = new RealDB();
            db.update("delete from userAnswers where userID=" + user.id + ";");
             db.update("update users set lastQuestion=1 where id=" + user.id + ";");
         }
         resp.sendRedirect("/");
     }
 }
