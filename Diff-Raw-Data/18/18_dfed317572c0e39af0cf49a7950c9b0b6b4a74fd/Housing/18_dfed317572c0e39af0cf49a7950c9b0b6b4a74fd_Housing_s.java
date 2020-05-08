 package Servlet;
 
 import Entity.StudentInformation;
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
         String spentEatingOut = request.getParameter("spentEatingOut");
         String entertainment = request.getParameter("spentOnEntertainment");
         String savings = request.getParameter("saving");
         String transportation = request.getParameter("transportation");
         String spentOnEating = request.getParameter("foodPerWeek");
 
        try{
             //not sure if these will be need but here in case
        double rent = Double.parseDouble(housingRent);
        double utility = Double.parseDouble(housingUtility);
        int eatingOut = Integer.parseInt(spentEatingOut);
        double spentOnEntertainment = Double.parseDouble(entertainment);
        double savingsAmount = Double.parseDouble(savings);
        double spentOnTransportation = Double.parseDouble(transportation);
        double spentOnFood= Double.parseDouble(spentOnEating);
 
             //curUser.getStudentLoans().add(loan);
             //will probably change this to use the integers but to be determined later
             HttpSession session = request.getSession();
             StudentInformation curUser = (StudentInformation) session.getAttribute("currentUser");
 
             curUser.setMonthlyUtilities(utility);
             curUser.setMonthlyRent(rent);
             session.setAttribute("currentUser", curUser);
             session.setAttribute("housingType",housingType);
             session.setAttribute("eatingOut",eatingOut);
             session.setAttribute("spentOnTransportation",spentOnTransportation);
             session.setAttribute("spentOnEntertainment",spentOnEntertainment);
             session.setAttribute("saving",savingsAmount);
             session.setAttribute("spentOnFood", spentOnFood);
         }
         catch(Exception e)
         {
             //will do better error checking soon
             e.printStackTrace();
         }
 
         response.sendRedirect(request.getContextPath() + "/PostGraduation");
     }
 
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
     {
         request.getRequestDispatcher("/housing/housing.jsp").forward(request,response);
     }
 }
