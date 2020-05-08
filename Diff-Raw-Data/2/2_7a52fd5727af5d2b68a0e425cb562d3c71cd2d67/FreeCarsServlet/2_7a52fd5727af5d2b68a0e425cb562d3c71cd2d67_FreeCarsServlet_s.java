 package servlet;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 
 import session.SessionManager;
 import bean.Car;
 
 /**
  * Servlet implementation class FreeCarsServlet
  */
 // @WebServlet("/FreeCarsServlet")
 public class FreeCarsServlet extends HttpServlet {
   private static final long serialVersionUID = 1L;
 
   private static final Logger LOGGER = Logger.getLogger(FreeCarsServlet.class.getName());
 
   /**
    * @see HttpServlet#HttpServlet()
    */
   public FreeCarsServlet() {
     super();
     // TODO Auto-generated constructor stub
   }
 
   /**
    * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
    *      response)
    */
   @Override
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     HttpSession session;
     Session hbSession;
     SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
     String fromDateString = request.getParameter("fromDate");
     String toDateString = request.getParameter("toDate");
 
     session = request.getSession();
     hbSession = SessionManager.openSession();
 
     try {
       session.setAttribute("fromDate", fromDateString);
       session.setAttribute("toDate", toDateString);
 
       Date fromDate = sdf.parse(fromDateString);
       Date toDate = sdf.parse(toDateString);
 
       List<Car> cars = Car.getFreeCars(hbSession, fromDate, toDate);
 
       LOGGER.log(Level.INFO, "The List of the free cars is:{0}", new Object[] { Car.getFreeCars(hbSession, fromDate, toDate) });
 
      request.setAttribute("cars", cars); 
       request.getRequestDispatcher("showFreeCars.jsp").forward(request, response);
     } catch (HibernateException e) {
       SessionManager.rollbackTransaction();
       LOGGER.log(Level.SEVERE, "Loading of the free cars list {0}\n failed with the following error:\n{1}",
           new Object[] { Car.getFreeCars(hbSession, new Date(), new Date()), e });
       throw new RuntimeException(e);
     } catch (ParseException parseEx) {
       parseEx.printStackTrace();
     } finally {
       SessionManager.closeSession();
     }
   }
 
   /**
    * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
    *      response)
    */
   @Override
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
   }
 }
