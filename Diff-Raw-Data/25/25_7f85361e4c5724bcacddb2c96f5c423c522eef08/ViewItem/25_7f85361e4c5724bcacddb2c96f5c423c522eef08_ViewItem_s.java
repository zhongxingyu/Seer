 package com.iappsam.servlet.item;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.iappsam.entities.Account;
 import com.iappsam.entities.Contact;
 import com.iappsam.entities.Employee;
 import com.iappsam.entities.Item;
 import com.iappsam.entities.Person;
 import com.iappsam.managers.AccountManager;
 import com.iappsam.managers.ContactManager;
 import com.iappsam.managers.DivisionOfficeManager;
 import com.iappsam.managers.ItemManager;
 import com.iappsam.managers.PersonManager;
 import com.iappsam.managers.exceptions.TransactionException;
 import com.iappsam.managers.sessions.AccountManagerSession;
 import com.iappsam.managers.sessions.ContactManagerSession;
 import com.iappsam.managers.sessions.DivisionOfficeManagerSession;
 import com.iappsam.managers.sessions.ItemManagerSession;
 import com.iappsam.managers.sessions.PersonManagerSession;
 
 /**
  * Servlet implementation class ViewItem
  */
 @WebServlet("/items/ViewItem.do")
 public class ViewItem extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * @see HttpServlet#HttpServlet()
 	 */
 	public ViewItem() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 
 		ItemManager iManager = new ItemManagerSession();
		String description = (String)request.getParameter("description");
 
 		Item item = new Item();
 		try {
 			item = iManager.getItem(description);
 		} catch (TransactionException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
		request.setAttribute("description", item.getDescription());
		request.setAttribute("unit", item.getUnit());
		request.setAttribute("category", item.getCategory());
		request.setAttribute("price", "" + item.getPrice());
		// request.setAttribute("", ); //date
		request.setAttribute("stockNumber", item.getStockNumber());
		request.setAttribute("inventoryItemNumber", "" + item.getInventoryItemNumber());
		request.setAttribute("propertyNumber", item.getPropertyNumber());
		request.setAttribute("status", item.getStatus());
		request.setAttribute("condition", item.getCondition());
 		RequestDispatcher view = request.getRequestDispatcher("../stocks/items/ViewItem.jsp");
 		view.forward(request, response);
 
 	}
 
 }
