 package Servlet;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Robert
  * Date: 2/20/13
  * Time: 6:50 PM
  * To change this template use File | Settings | File Templates.
  */
 //housing stuff use this servlet to get housing information
 @WebServlet("/Housing")
 public class Housing extends HttpServlet
 {
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         String housingType = request.getParameter("housing");
         String housingRent = request.getParameter("housingA");
         String housingUtility = request.getParameter("housingB");
         String daysEatingLunch = request.getParameter("daysEatingLunch");
         String daysEatingDinner = request.getParameter("daysEatingDinner");
         String spentOnVideoGames = request.getParameter("spentonvideogames");
              /*
         try{
             //not sure if these will be need but here in case
         int rent = Integer.parseInt(housingRent);
         int utiltiy = Integer.parseInt(housingUtility);
         int spentOnLunch = Integer.parseInt(daysEatingLunch);
         int spentOnDinner = Integer.parseInt(daysEatingDinner);
         int videoGameSpending = Integer.parseInt(spentOnVideoGames);
         }
         catch(Exception e)
         {
             //will do better error checking soon
             e.printStackTrace();
         }
             */
         //will probably change this to use the integers but to be determined later
         HttpSession session = request.getSession();
         session.setAttribute("housingType", housingType);
         session.setAttribute("housingRent", housingRent);
         session.setAttribute("housingUtility", housingUtility);
         session.setAttribute("daysEatingLunch",daysEatingLunch);
         session.setAttribute("daysEatingDinner",daysEatingDinner);
         session.setAttribute("spentOnVideoGames",spentOnVideoGames);
 
         request.getRequestDispatcher("Post_Graduation.jsp");
     }
 
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
 
     }
 }
