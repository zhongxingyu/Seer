 package Servlet;
 
 import Helper.Loan;
 import Entity.StudentInformation;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Sean
  * Date: 3/1/13
  * Time: 1:05 PM
  * To change this template use File | Settings | File Templates.
  */
 @WebServlet("/SchoolLoan")
 public class SchoolLoan extends HttpServlet{
     //Class will be used to calculate the financial information based on the input
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
 
         String loanRatio = request.getParameter("slider");
         int max = 100;
         int oopAmount = max - (Integer.valueOf(request.getParameter("slider")));
         String oopRatio = Integer.toString(oopAmount);
 
         String[] loanAmounts = request.getParameterValues("loanAmount");
         String[] loanInterests = request.getParameterValues("loanInterest");
         String[] monthlyPayments = request.getParameterValues("monthlyPayment");
 
         int ooPAmount = Integer.valueOf(request.getParameter("oOPAmount"));
 
         HttpSession session = request.getSession(false);
         StudentInformation curUser = (StudentInformation) session.getAttribute("currentUser");
 
         if(ooPAmount == 100){
             int loanAmount = 0;
             int loanInterest = 0;
             int monthlyPayment = 0;
             curUser.setOutOfPocket(ooPAmount);
         }else {
             int loanNumbers = loanAmounts.length; //Amount of loans
 
             for(int i = 0; i < loanNumbers; i++){
                 int loanAmount = Integer.valueOf(loanAmounts[i]);
                 int loanInterest = Integer.valueOf(loanInterests[i]);
                 int monthlyPayment = Integer.valueOf(monthlyPayments[i]);
                Loan loan = new Loan(loanAmount, loanInterest, monthlyPayment);
                 curUser.getStudentLoans().add(loan);
             }
         }
 
         session.setAttribute("currentUser", curUser);
 
         response.sendRedirect(request.getContextPath() + "/Housing");
     }
 
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         request.getRequestDispatcher("/School/School_Questions_2.jsp").forward(request,response);
     }
 }
