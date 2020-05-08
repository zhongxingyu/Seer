 package sheridan.servlets;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import sheridan.BudgetUser;
 import sheridan.Transaction;
 
 /**
  * Servlet implementation class SubmitTransactionServlet
  */
 @WebServlet("/SubmitTransactionServlet")
 public class SubmitTransactionServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public SubmitTransactionServlet() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		/*
 		 * List to store all invoices created in session
 		 */
 		ArrayList<Transaction> transactionList;
 
 		/*
 		 * Setup the local invoice copy to store values passed in from the
 		 * request parameters
 		 */
 		Transaction t = null;
 		HttpSession session = request.getSession();
 
 		/*
 		 * Checks if a session already exists
 		 */
 		if (!session.isNew()) {
 			transactionList = (ArrayList<Transaction>) session
 					.getAttribute("transactionList");
 			/*
 			 * Set the local Invoice object to the selectedInvoice session
 			 * attribute This is used in case the request came from invoiceForm
 			 * after hitting submit and found errors in the form. The invoice
 			 * has errors in it is saved as a attribute called selectedInvoice
 			 * and is passed to invoiceForm to repopulate the input fields with
 			 * selectedInvoices variables
 			 */
 			t = (Transaction) session.getAttribute("selectedInvoice");
 			if (transactionList == null) {
 				transactionList = new ArrayList<Transaction>();
 			}
 
 			/*
 			 * If selectedInvoice is null, meaning either no invoice was
 			 * attempted to be submitted or no invoice was selected from
 			 * invoiceBinder This only case this if statement will be true is if
 			 * it is a first time invoice or if the user clicks on Add Invoice
 			 * from invoiceBinder
 			 */
 			if (t == null) {
 				t = new Transaction();
 			}
 		} else {
 			/*
 			 * A new session will create a new list and invoice
 			 */
 			transactionList = new ArrayList<Transaction>();
 			t = new Transaction();
 		}
 
 		/*
 		 * Store the request parameters
 		 */
 		String description = request.getParameter("description");
 		float amount = 0;
 
 		/*
 		 * A boolean that will check if the form values are valid Later on in
 		 * the function, if hasError is true, it will set the selectedInvoice to
 		 * this invoice then it will redirect back to invoiceForm.jsp
 		 */
 		boolean hasError = false;
 
 		try {
 			amount = Float.parseFloat(request.getParameter("amount"));
 			session.setAttribute("amountError", null);
 		} catch (NumberFormatException e) {
 			/*
 			 * Set the error message to illustrate the problem Set hasError to
 			 * true so the code will later on redirect back to the invoiceForm
 			 * displaying the error message The redirect is not done here
 			 * because there could possibly be more errors in other fields
 			 */
 			// not an int
 			session.setAttribute("amountError", "please enter the quantity");
 			hasError = true;
 		} catch (NullPointerException e) {
 			/*
 			 * Set the error message to illustrate the problem Set hasError to
 			 * true so the code will later on redirect back to the invoiceForm
 			 * displaying the error message The redirect is not done here
 			 * because there could possibly be more errors in other fields
 			 */
 			// string was null
 			session.setAttribute("amountError", "please enter the quantity");
 			hasError = true;
 		}
 
 		if (description != null && !"".equals(description)) {
 			t.setDescription(description);
 			session.setAttribute("descriptionError", null);
 		} else {
 			session.setAttribute("descriptionError",
 					"please enter the currency");
 			t.setDescription(description);
 			hasError = true;
 		}
 
 		if (amount > 0) {
 			t.setAmount(amount);
 			session.setAttribute("amountError", null);
 		} else {
 			session.setAttribute("amountError", "please enter the quantity");
 			t.setAmount(0);
 			hasError = true;
 		}
 
 		/*
 		 * If anything had an error in it, redirect back to the invoiceForm
 		 */
 		if (hasError) {
 			response.sendRedirect("SystemPage.jsp");
 			/*
 			 * Cut doPost off early here. There is no need to run the code below
 			 * if there was an error
 			 */
 			return;
 		}
 
 		BudgetUser user = (BudgetUser) session.getAttribute("user");
 
 		String bool = request.getParameter("dORc");
 		if ("credit".equals(bool)) {
			t.setIsCredit(true);
 			user.setTotalCredit(user.getTotalCredit() + amount);
 			user.setTotalCash(user.getTotalCash() - amount);
 		} else {
			t.setIsCredit(false);
 			user.setTotalDebit(user.getTotalDebit() + amount);
 			user.setTotalCash(user.getTotalCash() + amount);
 		}
 
 		/*
 		 * If addInvoice was not set to false above, that means the
 		 * invoiceNumber was not found in the list, meaning it is a new invoice.
 		 * Add it to the list
 		 */
 		transactionList.add(t);
 
 		/*
 		 * Set the invoiceList as a session attribute
 		 */
 		session.setAttribute("transactionList", transactionList);
 
 		/*
 		 * forward the request, response to invoiceBinder
 		 */
 		// RequestDispatcher rd = request
 		// .getRequestDispatcher("invoiceBinder.jsp");
 		// rd.forward(request, response);
 
 		response.sendRedirect("TChart.jsp");
 	}
 }
