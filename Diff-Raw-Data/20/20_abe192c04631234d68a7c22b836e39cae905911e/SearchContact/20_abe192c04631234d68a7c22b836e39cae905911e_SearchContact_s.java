 package servlet;
 
 import generator.website.Template_addContact;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.ejb.EJB;
 import javax.naming.Context;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 
 /*import org.springframework.context.ApplicationContext;
 import org.springframework.web.context.support.WebApplicationContextUtils;*/
 
 import util.ServerUtils;
 
 import daoInterface.IDAOContact;
 import daoInterface.IDAOEntreprise;
 import util.*;
 
 /**
  * Servlet implementation class SContactSearchContact
  */
 public class SearchContact extends BaseServlet {
 	private static final long serialVersionUID = 1L;
 	@EJB(name = "DAOContactBean")
 	private IDAOContact daoContact;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public SearchContact() {
         super();
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		String reqUrl =  request.getRequestURL().toString();
 		System.out.println("SearchContact::doPost reqUrl='" + reqUrl + "'");
 		System.out.println("request.getContextPath()" + request.getContextPath());
 		
 
 		String firstName = request.getParameter("firstname");
 		String lastName = request.getParameter("lastname");
 		String email    = request.getParameter("email");
 		String street = request.getParameter("street");	
 		String city = request.getParameter("city");
 		String zip = request.getParameter("zip");
 		String country = request.getParameter("country");
 		String phoneKind = request.getParameter("phonekind");
 		String phoneNumber = request.getParameter("phonenumber");
 		String numSiret = request.getParameter("numsiret");
 		
 		ServerUtils.showParameters(firstName, lastName, email, street, city, zip, country, phoneKind, phoneNumber, numSiret);
 
 		IDAOEntreprise daoEntreprise = null;
 
 		
 		
 		String dbOutput = null;
 		dbOutput = daoContact.searchContact(0, firstName, lastName, email, street, city, zip, country, phoneKind, phoneNumber, numSiret);
 		//dbOutput = daoEntreprise.searchEntreprise(0, firstName, lastName, email, street, city, zip, country, phoneKind, phoneNumber, numSiret);
 		 
 		

 
 	      
 	      PrintWriter out = response.getWriter() ;
 
 	      
 	      out.println (getHeader(null));
 	      out.println (getBody("Search Contact/Entreprise"));
 	      out.println (dbOutput);
 	         
 	      out.println (getFooter());
 	      
 	     
 
 
 //		
 //		String responseUrl = "/" + "menuMainPage" + ServerUtils.getNewParameter("dbOutputRaw", dbOutput);
 //		System.out.println("::doPost responseUrl=" + responseUrl);
 //		
 ////
 //		response.sendRedirect(request.getContextPath() + responseUrl); 
 		
 		
 
 		
 		
 		
 
 
 		
 	}
 
 }
