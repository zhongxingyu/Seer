 package controller;
 
 import manager.*;
 import model.Car;
 import model.Degree;
 import model.Housing;
 import model.Region;
 
 import javax.ejb.EJB;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebInitParam;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 
 @WebServlet(name = "ResultCalculator", urlPatterns = {"/result/calculate"},
         initParams =
                 {
                         @WebInitParam(name = "success", value = "/results.jsp"),
                         @WebInitParam(name = "failure", value = "/aspirations.jsp")})
 public class ResultCalculator extends HttpServlet
 {
     @EJB
     CarManager carManager;
     @EJB
     DebtTypeManager debtTypeManager;
     @EJB
     DegreeManager degreeManager;
     @EJB
     HousingManager housingManager;
     @EJB
     MiscManager miscManager;
     @EJB
     RegionManager regionManager;
 
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
     {
         HttpSession session = request.getSession();
 
         //School
         String startMonth = (String)session.getAttribute("startMonth");
         Integer startYear = Integer.parseInt((String)session.getAttribute("startYear"));
         Degree degree = (Degree)session.getAttribute("degree");
         Integer expectedFull = Integer.parseInt((String)session.getAttribute("expectedFull"));
         Integer expectedPart = Integer.parseInt((String)session.getAttribute("expectedPart"));
         Integer loanPercent = Integer.parseInt((String)session.getAttribute("loanPercent"));
         Double debt1 = Double.parseDouble((String)session.getAttribute("debt1"));
         Double debt2 = Double.parseDouble((String)session.getAttribute("debt2"));
         Double debt3 = Double.parseDouble((String)session.getAttribute("debt3"));
 
         //Housing & Lifestyle
         Double rent = Double.parseDouble((String)session.getAttribute("rent"));
         Double utilities = Double.parseDouble((String)session.getAttribute("utilities"));
         Double lunch = Double.parseDouble((String)session.getAttribute("go_out_to_lunch"));
         Double dinner = Double.parseDouble((String)session.getAttribute("go_out_to_dinner"));
         Double entertainment = Double.parseDouble((String)session.getAttribute("spend_on_entertainment"));
 
         //Post-Graduation
         Region region = (Region)session.getAttribute("region");
         Car car = (Car)session.getAttribute("car");
         Housing housing = (Housing)session.getAttribute("housing");
         Double payments = Double.parseDouble((String)session.getAttribute("payments"));
 
 
         //Salary
         Double salary = degree.getSalary() / 12;
 
         //Student loan, duration, payment, and interest
         //not calculated yet
 
         //Income tax
         Double incomeTax = degree.getSalary() * region.getTaxrate() / 12;
 
         // Misc Expenses
         Double lunchMonth = lunch * 4.34812;
         Double dinnerMonth = dinner * 4.34812;
         Double miscExpenses = lunchMonth + dinnerMonth + entertainment;
 
         //Car Expenses
         Double mpg = car.getMpg();
         Double gasMonth = 1600 / mpg * 3.6;
         Double carMonth = gasMonth + payments;
 
         //Mortgage/Rent
         Double mortgageRent = housing.getRent() + housing.getUtilities();
 
         //Misc. Loans
         //to be determined
 
         //Savings Act Contribution
         //to be determined
 



//        Salary.....................${salary}<br>
//            Student Loan...............${studentloan}<br>
//            Income Tax.................${income}<br>
//            Misc. Expenses.............${miscinsurance}<br>
//            Car Expenses...............${carpayment}<br>
//            Mortgage/Rent..............${mortgage}<br>
//            Misc. Loans................${totalloans}<br>
//            Savings Act Contribution...${inschoolsavings}<br>
//        <br>
//        <br>
//            Discretionary Income: ${discretionary}


         request.getRequestDispatcher(getServletConfig().getInitParameter("success")).forward(request, response);
 
     }
 
 }
