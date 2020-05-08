 package com.iappsam.servlet.entities.employee;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
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
 import com.iappsam.managers.ContactManager;
 import com.iappsam.managers.DivisionOfficeManager;
 import com.iappsam.managers.PersonManager;
 import com.iappsam.managers.exceptions.DuplicateEntryException;
 import com.iappsam.managers.exceptions.TransactionException;
 import com.iappsam.managers.sessions.ContactManagerSession;
 import com.iappsam.managers.sessions.DivisionOfficeManagerSession;
 import com.iappsam.managers.sessions.PersonManagerSession;
 import com.iappsam.util.EntryFormatter;
 
 /**
  * Servlet implementation class C
  */
 @WebServlet("/entities/employees/update_employee.do")
 public class EmployeeUpdate extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	private String title;
 	private String name;
 	private String[] designation;
 	private String[] employeeNo;
 	private String[] divisionOfficeID;
 	private String mobileNumber;
 	private String landline;
 	private String emailad;
 	private String personID;
 	private String employeeID;
 
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 	}
 
 	private EntryFormatter entryFormatter = new EntryFormatter();
 
 	private void failedResponse(HttpServletRequest request, HttpServletResponse response) {
 		if (name.isEmpty() || entryFormatter.check(name))
 			request.setAttribute("nameOK", "false");
 		else
 			request.setAttribute("nameOK", "true");
 
 		String designation1OK = null;
 		if (designation[0].isEmpty() && !employeeNo[0].isEmpty()) {
 			designation1OK = "true";
 		}
 		String designation2OK = null;
 		if (designation.length > 1 && designation[1].isEmpty() && !employeeNo[1].isEmpty()) {
 			designation2OK = "true";
 		}
 		String designation3OK = null;
 		if (designation.length > 2 && designation[2].isEmpty() && !employeeNo[2].isEmpty()) {
 			designation3OK = "true";
 		}
 
 		RequestDispatcher view = request.getRequestDispatcher("view_employee.jsp");
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
 		request.setAttribute("employeeID", employeeID);
 		try {
 			view.forward(request, response);
 		} catch (ServletException e) {
 			failedResponse(request, response);
 		} catch (IOException e) {
 			failedResponse(request, response);
 		}
 
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 
 	@Override
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		title = entryFormatter.spaceTrimmer(request.getParameter("title"));
 		name = entryFormatter.spaceTrimmer(request.getParameter("name"));
 		designation = request.getParameterValues("designation");
 		employeeNo = request.getParameterValues("empNo3");
 		divisionOfficeID = request.getParameterValues("divisionOffice");
 		mobileNumber = entryFormatter.spaceTrimmer(request.getParameter("cellphoneNumber"));
 		landline = entryFormatter.spaceTrimmer(request.getParameter("landline"));
 		emailad = entryFormatter.spaceTrimmer(request.getParameter("e-mail_ad"));
 		employeeID = request.getParameter("employeeID");
 		personID = request.getParameter("personID");
 
 		for (int i = 0; i < designation.length; i++) {
 			designation[i] = entryFormatter.spaceTrimmer(designation[i]);
 			employeeNo[i] = entryFormatter.spaceTrimmer(employeeNo[i]);
 		}
 		if (!name.isEmpty() && designation != null && entryFormatter.check(name)) {
 			acceptResponse(request, response);
 		} else
 			failedResponse(request, response);
 	}
 
 	private void acceptResponse(HttpServletRequest request, HttpServletResponse response) {
 		PersonManager pManager = new PersonManagerSession();
 		DivisionOfficeManager dManager = new DivisionOfficeManagerSession();
 		Person person;
 		ContactManager cManager = new ContactManagerSession();
 		try {
 			if (name != null && designation != null) {
 				person = pManager.getPerson(Integer.parseInt(personID));
 
 				Contact[] contacts = new Contact[person.getContacts().size()];
 				person.getContacts().toArray(contacts);
 				ArrayList<Contact> mobileList = new ArrayList<Contact>();
 				ArrayList<Contact> landlineList = new ArrayList<Contact>();
 				ArrayList<Contact> emailList = new ArrayList<Contact>();
 				if (contacts != null) {
 
 					for (int i = 0; i < contacts.length; i++) {
 						if (contacts[i].getType().equals(ContactType.EMAIL))
 							emailList.add(contacts[i]);
 
 						if (contacts[i].getType().equals(ContactType.MOBILE))
 							mobileList.add(contacts[i]);
 
 						if (contacts[i].getType().equals(ContactType.LANDLINE))
 							landlineList.add(contacts[i]);
 					}
 				}
 				if (title != null && !title.equalsIgnoreCase("null") && !title.isEmpty()) {
 					person.setName(name);
 					person.setTitle(title);
 				} else {
 					person.setName(name);
 				}
 				if (emailad != null && emailList.isEmpty() && !emailad.isEmpty()) {
 					Contact email = new Contact();
 					email.setData(emailad);
 					email.setType(ContactType.EMAIL);
 					person.addContact(email);
 				}
 				for (int i = 0; i < emailList.size(); i++)
 					if (emailad != null && !emailad.equalsIgnoreCase("null") && !emailad.isEmpty()) {
 						Contact email = new Contact();
 						if (i < emailList.size()) {
 							email = emailList.get(i);
 							email.setData(emailad);
 							cManager.addContact(email);
 						}
 
 					}
 				if (landline != null && landlineList.isEmpty() && !landline.isEmpty()) {
 
 					Contact landline2 = new Contact();
 					landline2.setData(landline);
 					landline2.setType(ContactType.LANDLINE);
 					person.addContact(landline2);
 				}
 				for (int i = 0; i < landlineList.size(); i++)
 					if (landline != null && !landline.equalsIgnoreCase("null") && !landline.isEmpty()) {
 						Contact landline2 = new Contact();
 
 						if (i < landlineList.size()) {
 							landline2 = landlineList.get(i);
 							landline2.setData(landline);
 							landline2.setType(landlineList.get(i).getType());
 							landline2.setId(landlineList.get(i).getContactID());
 							cManager.updateContact(landline2);
 						}
 					}
 				if (mobileNumber != null && mobileList.isEmpty() && !mobileNumber.isEmpty()) {
 					Contact mobile = new Contact();
 					mobile.setData(mobileNumber);
 					mobile.setType(ContactType.MOBILE);
 					person.addContact(mobile);
 				}
 				for (int i = 0; i < mobileList.size(); i++)
 					if (mobileNumber != null && !mobileNumber.equalsIgnoreCase("null") && !mobileNumber.isEmpty()) {
 						Contact mobile = new Contact();
 						if (i < mobileList.size()) {
 							mobile = mobileList.get(i);
 							mobile.setData(mobileNumber);
 							mobile.setType(mobileList.get(i).getType());
 							mobile.setId(mobileList.get(i).getContactID());
 							cManager.updateContact(mobile);
 						}
 					}
 
 				List<Employee> empList = pManager.getEmployeeByPerson(person.getId());
 				for (int i = 0; i < designation.length; i++) {
 					if (designation[i].isEmpty())
 						continue;
 					Employee employee = new Employee();
 					if (i < empList.size())
 						employee = empList.get(i);
 					employee = empList.get(i);
 					employee.setDesignation(designation[i]);
 					employee.setEmployeeNumber(employeeNo[i]);
 					try {
 						employee.setDivisionOffice(dManager.getDivisionOffice(Integer.parseInt(divisionOfficeID[i])));
 						pManager.updateEmployee(employee);
 						empList.add(employee);
 					} catch (NumberFormatException e) {
 						failedResponse(request, response);
 					} catch (Exception e) {
 						failedResponse(request, response);
 					}
 					try {
 						pManager.updatePerson(person);
 					} catch (DuplicateEntryException e) {
 						// TODO Auto-generated catch block
 						failedResponse(request, response);
 					}
 				}
 				request.setAttribute("employeeID", empList.get(0).getId());
 			}
 
 			RequestDispatcher view = request.getRequestDispatcher("search_employee.do");
 			view.forward(request, response);
 		} catch (ServletException e) {
 			failedResponse(request, response);
 		} catch (IOException e) {
 			failedResponse(request, response);
 		} catch (TransactionException e) {
 			failedResponse(request, response);
 		}
 	}
 }
