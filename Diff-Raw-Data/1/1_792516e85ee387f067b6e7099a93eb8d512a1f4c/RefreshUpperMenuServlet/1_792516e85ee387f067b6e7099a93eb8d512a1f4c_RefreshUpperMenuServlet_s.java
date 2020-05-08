 package servlet;
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import command.CommandExecutor;
 import domain.User;
 
 
 
 /**
  * Servlet implementation class CreateDepartmentServlet
  */
 @WebServlet(description = "servlet to log in users", urlPatterns = { "/RefreshUpperMenuServlet" })
 public class RefreshUpperMenuServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 	
 	public void init() throws ServletException {
 		super.init();
 		try {
 			CommandExecutor.getInstance();
 		} catch (Exception e) {
 			throw new ServletException(e);
 		}
 	}
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public RefreshUpperMenuServlet() {
         super();
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		HttpSession session = request.getSession();
 		User userE = (User)session.getAttribute("user");
 		if(userE != null){
 			try {
 				int countPAD = (Integer) CommandExecutor.getInstance().executeDatabaseCommand(new command.CountPendingAdmissionDischarges());
 				int countPCN = (Integer) CommandExecutor.getInstance().executeDatabaseCommand(new command.CountPendingCreditNotes());	
 				int countPCNR = (Integer) CommandExecutor.getInstance().executeDatabaseCommand(new command.CountPendingCreditNotesReviews());	
 				int countPB = (Integer) CommandExecutor.getInstance().executeDatabaseCommand(new command.CountPendingBills());	
 				int countPPD = (Integer) CommandExecutor.getInstance().executeDatabaseCommand(new command.CountPendingPharmacyDischarges());	
 				int countPED = (Integer) CommandExecutor.getInstance().executeDatabaseCommand(new command.CountPendingEstimationDiscounts());	
 				int countPPP = (Integer) CommandExecutor.getInstance().executeDatabaseCommand(new command.CountPendingPayments());	
 				
 				System.out.println("a" + countPPP);
 				
 				session.setAttribute("countPAD", countPAD);
 				session.setAttribute("countPCN", countPCN);
 				session.setAttribute("countPCNR", countPCNR);
 				session.setAttribute("countPB", countPB);
 				session.setAttribute("countPPD", countPPD);
 				session.setAttribute("countPED", countPED);
 				session.setAttribute("countPPP", countPPP);
 				
 				
 				RequestDispatcher rd = getServletContext().getRequestDispatcher("/upperMenuAux.jsp");
 				rd.forward(request, response);
 			}
 			catch (Exception e) {
 				throw new ServletException(e);
 			}
 		} else {
 			request.setAttribute("time_out", "Su sesin ha expirado. Ingrese nuevamente"); RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.jsp");
 			rd.forward(request, response);
 		}	
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		doGet(request, response);
 	}
 }
