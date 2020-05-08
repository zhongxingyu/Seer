 package paybar.webservlets;
 
 import java.io.IOException;
 import java.net.HttpRetryException;
 import java.util.Date;
 
 import javax.inject.Inject;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.jboss.as.ee.component.interceptors.InterceptorOrder.ClientPostConstruct;
 import org.jboss.resteasy.client.ClientRequest;
 import org.jboss.resteasy.client.ClientResponse;
 
 import paybar.data.DetailAccountResource;
 import paybar.data.PartnerResource;
 import paybar.data.PaybarResourceException;
 import paybar.data.TransactionResource;
 import paybar.model.DetailAccount;
 import paybar.model.Transaction;
 import at.ac.uibk.paybar.messages.Configuration;
 import at.ac.uibk.paybar.messages.TransactionRequest;
 
 /**
  * ChargeServlet
  * Servlet responsible for charging the credit of
  * a user.
  * 
  */
 
 @WebServlet(name = "ChargeServlet", urlPatterns = { "/cpanel/ChargeServlet" })
 public class ChargeServlet extends HttpServlet {
 
 	@Inject
 	private PartnerResource pr;
 
 	@Inject
 	private TransactionResource tr;
 
 	@Inject
 	private DetailAccountResource dar;
 
 	private static final long serialVersionUID = 1L;
 
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		doPost(request, response);
 	}
 
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		String creditCardNumber = request.getParameter("creditcard");
 		if (creditCardNumber != null && request.getParameter("amount") != null) {
 			try {
 
 				/*
 				 * Create new transaction.
 				 */
 
 				Double d = Double.parseDouble(request.getParameter("amount")) * 100;
 				long amount = d.longValue();
 
 				DetailAccount da = (DetailAccount) request.getSession()
 						.getAttribute("user");
 				Date now = new Date();
 				
 				/*
 				tr.createChargeTransaction(amount,
 						"Charging Account from Credit Card: "
 								+ creditCardNumber,
 						Configuration.BankPosName.toString(), da.getUserName(),
 						now);
 				da.setCredit(dar.getUserByName(da.getUserName(), false)
 						.getCredit());
 						*/
 
 				ClientRequest clientRequest = new ClientRequest(
 						"http://localhost:8080/fastcheck/rest/transactions/charge/"
								+ da.getUserName() + "/" + creditCardNumber);
 				clientRequest.accept("application/json");
 				clientRequest
 						.body("application/json", new TransactionRequest(amount, Configuration.BankPosName.toString()));
 				
 				ClientResponse<String> fastcheckResponse = clientRequest.post(String.class);
 				
 				
 
 				// the code above has been moved to FastCheck and
 				// DelayedTransactionProcessor
 
 				// call fastcheck
 				// locate service and call it.
 
 				// only send message of success after everything has been
 				// updated.
 				if(fastcheckResponse.getStatus() == 200) {
 				request.setAttribute("message",
 						"The Ammount has ben charged to your Account");
 				} else {
 					request.setAttribute("message",
 							"The charge request has failed!");
 				}
 				RequestDispatcher dispatcher = request
 						.getRequestDispatcher("charge.jsp");
 				dispatcher.forward(request, response);
 
 			} catch (Exception e) {
 				e.printStackTrace();
 				if (e.getCause() != null
 						&& e.getCause().getClass()
 								.equals(PaybarResourceException.class)) {
 					request.setAttribute("error", e.getCause().getMessage());
 				} else {
 					if (e.getCause() != null
 							&& e.getCause().getClass()
 									.equals(PaybarResourceException.class)) {
 						request.setAttribute("error", e.getCause().getMessage());
 					} else {
 						request.setAttribute("error",
 								"Service temporary not available! Maybe db is not running!"
 										+ e.getMessage());
 					}
 				}
 				RequestDispatcher dispatcher = request
 						.getRequestDispatcher("charge.jsp");
 				dispatcher.forward(request, response);
 			}
 		}
 	}
 
 }
