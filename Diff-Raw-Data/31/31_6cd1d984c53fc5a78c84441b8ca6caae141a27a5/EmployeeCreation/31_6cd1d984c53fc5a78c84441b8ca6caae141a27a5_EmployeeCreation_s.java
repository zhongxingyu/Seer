 package com.iappsam.servlet.entities.employee;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.iappsam.Contact;
 import com.iappsam.ContactType;
 import com.iappsam.Employee;
 import com.iappsam.Person;
 import com.iappsam.managers.DivisionOfficeManager;
 import com.iappsam.managers.PersonManager;
 import com.iappsam.managers.exceptions.DuplicateEntryException;
 import com.iappsam.managers.exceptions.TransactionException;
 import com.iappsam.managers.sessions.DivisionOfficeManagerSession;
 import com.iappsam.managers.sessions.PersonManagerSession;
 import com.iappsam.util.EntryFormatter;
 
 /**
  * Servlet implementation class C
  */
 @WebServlet("/entities/employees/add_employee.do")
 public class EmployeeCreation extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	private String title;
 	private String name;
 	private String[] designation;
 	private String[] employeeNo;
 	private String[] divisionOfficeID;
 	private String mobileNumber;
 	private String landline;
 	private String emailad;
 
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 	}
 
 	private void failedResponse(HttpServletRequest request, HttpServletResponse response) {
 		if (name.isEmpty() || entryFormatter.check(name))
 			request.setAttribute("nameOK", "false");
 		else
 			request.setAttribute("nameOK", "true");
 
 		String designation1OK = null;
 		if (designation[0].isEmpty() && !employeeNo[0].isEmpty() || entryFormatter.check(designation[0])) {
 			designation1OK = "true";
 		}
 		String designation2OK = null;
 		if (designation[1].isEmpty() && !employeeNo[1].isEmpty() || entryFormatter.check(designation[1])) {
 			designation2OK = "true";
 		}
 		String designation3OK = null;
 		if (designation[2].isEmpty() && !employeeNo[2].isEmpty() || entryFormatter.check(designation[2])) {
 			designation3OK = "true";
 		}
 
 		RequestDispatcher view = request.getRequestDispatcher("add_employee.jsp");
 		request.setAttribute("title", title);
 		request.setAttribute("name", name);
 		request.setAttribute("designation", designation);
 		request.setAttribute("designation1OK", designation1OK);
 		request.setAttribute("designation2OK", designation2OK);
 		request.setAttribute("designation3OK", designation3OK);
 		request.setAttribute("employeeNo", employeeNo);
 		request.setAttribute("mobileNumber", mobileNumber);
 		request.setAttribute("landline", landline);
 		request.setAttribute("emailad", emailad);
 		try {
 			view.forward(request, response);
 		} catch (ServletException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	private EntryFormatter entryFormatter = new EntryFormatter();
 
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		title = entryFormatter.spaceTrimmer(request.getParameter("title"));
 		name = entryFormatter.spaceTrimmer(request.getParameter("name"));
 		designation = request.getParameterValues("designation");
 		employeeNo = request.getParameterValues("employeeNo");
 		divisionOfficeID = request.getParameterValues("divisionOfficeDropdown");
 		mobileNumber = entryFormatter.spaceTrimmer(request.getParameter("cellphoneNumber"));
 		landline = entryFormatter.spaceTrimmer(request.getParameter("landline"));
 		emailad = entryFormatter.spaceTrimmer(request.getParameter("e-mail_ad"));
 
 		boolean isFail = false;
 
 		for (int i = 0; i < designation.length; i++) {
 			designation[i] = entryFormatter.spaceTrimmer(designation[i]);
 			employeeNo[i] = entryFormatter.spaceTrimmer(employeeNo[i]);
 		}
 		for (int i = 0; i < 3; i++) {
 			if (designation[i] == null && employeeNo[i] != null) {
 				isFail = true;
 			}
 		}
 		if (!name.isEmpty() && designation != null && !isFail && entryFormatter.check(name)) {
			System.out.println("Designation Lenght:" + designation.length);
 			acceptResponse(request, response);
 		} else {
 			failedResponse(request, response);
 			isFail = false;
 		}
 	}
 
 	private void acceptResponse(HttpServletRequest request, HttpServletResponse response) {
 		PersonManager pManager = new PersonManagerSession();
 		DivisionOfficeManager dManager = new DivisionOfficeManagerSession();
 		Person person;
 		if (name != null && designation != null) {
 			if (title != null && !title.equalsIgnoreCase("null") && !title.isEmpty())
 				person = new Person(title, name);
 			else
 				person = new Person(name);
 
 			if (emailad != null && !emailad.equalsIgnoreCase("null") && !emailad.isEmpty()) {
 				Contact email = new Contact(emailad, ContactType.EMAIL);
 				person.addContact(email);
 			}
 
 			if (landline != null && !landline.equalsIgnoreCase("null") && !landline.isEmpty()) {
 				Contact landline2 = new Contact(landline, ContactType.LANDLINE);
 				person.addContact(landline2);
 			}
 
 			if (mobileNumber != null && !mobileNumber.equalsIgnoreCase("null") && !mobileNumber.isEmpty()) {
 				Contact mobile = new Contact(mobileNumber, ContactType.MOBILE);
 
 				person.addContact(mobile);
 			}
 			try {
 
 				pManager.addPerson(person);
 				request.setAttribute("person", person);
 			} catch (TransactionException e) {
 				failedResponse(request, response);
 			} catch (DuplicateEntryException e) {
 				failedResponse(request, response);
 			}
 			ArrayList<Employee> empList = new ArrayList<Employee>();
 			for (int i = 0; i < designation.length; i++) {
 				if (designation[i].isEmpty())
 					continue;
 				Employee employee = new Employee(designation[i], employeeNo[i], person);
 				try {
 					employee.setDivisionOffice(dManager.getDivisionOffice(Integer.parseInt(divisionOfficeID[i])));
 					pManager.addEmployee(employee);
 					empList.add(employee);
 				} catch (NumberFormatException e) {
 					failedResponse(request, response);
 				} catch (TransactionException e) {
 					failedResponse(request, response);
 				} catch (DuplicateEntryException e) {
 					failedResponse(request, response);
 				} catch (Exception e) {
 					e.printStackTrace();
 					failedResponse(request, response);
 				}
 
 			}
 			request.setAttribute("empList", empList);
 			request.setAttribute("employeeID", "" + empList.get(0).getId());
 			try {
 
 				RequestDispatcher view = request.getRequestDispatcher("search_employee.do");
 				view.forward(request, response);
 			} catch (ServletException e) {
 				failedResponse(request, response);
 			} catch (IOException e) {
 				failedResponse(request, response);
 			}
 		}
 	}
 }
