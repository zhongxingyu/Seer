 package userAction;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import system.Person;
 import system.User;
 import system.UserAction;
 
 /**
  * Servlet implementation class SearchPersonServlet
  */
 @WebServlet("/SearchPerson")
 public class SearchPersonServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
    
     public SearchPersonServlet() {
         super();
         // TODO Auto-generated constructor stub
     }
     
     public static final String ERROR_MWSSAGE = "errorMessage";
     public static final String USER_EMAIL_PARAM = "email";
//  public static final String PERSON_PARAM = "person";
     public static final String PERSON_MSG_PARAM = "personParamMsg";
    
     public static final String PERSON_SEARCH_RESULT_PARAM = "Person_result";
     public static final String SEARCH_FIRST_NAME_PARAM = "personName";
     public static final String SEARCH_LAST_NAME_PARAM = "personLastName";
     
     
   
    
     public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException , IOException{
     	HttpSession session = request.getSession(false);
     	
       	
     	String personName1 = (String)request.getParameter(SEARCH_FIRST_NAME_PARAM);
     	String personLastName1 = (String)request.getParameter(SEARCH_LAST_NAME_PARAM);
 
     	
     	if (session.getAttribute("user") == null){
  
         		request.setAttribute(ERROR_MWSSAGE, "Invalid email/password combination, try again");
         
        		this.getServletConfig().getServletContext().getRequestDispatcher("/LogIng.jsp").forward(request, response);
         	return;	
         	}
 
    	    User ur = new User((String)(session.getAttribute(USER_EMAIL_PARAM)));
     	UserAction uac = new UserAction(ur);
     	ur = uac.getUser(ur.getEmail());
     	User user = new User(ur.getId(), ur.getFirstName(), ur.getLastName(), ur.getPassword(), ur.getEmail());
     
    	List<Person> Persons = new ArrayList<Person>();
    	
    	Persons  = uac.searchPerson((String)("%"+personName1+"%"),((String)("%"+personLastName1+"%")),user.getId());
System.out.println(("%"+personName1+"%"));
System.out.println((String)("%"+personLastName1+"%"));
  	
 		if(Persons == null){
 	    	this.getServletConfig().getServletContext().getRequestDispatcher("/SearchPerson.jsp").forward(request, response);
 	    	return;
 		}
 
     	
     	session.setAttribute(PERSON_MSG_PARAM ,"Your search result for Persons List");
    	//session.setAttribute(PERSON_PARAM,Persons);
     	session.setAttribute(PERSON_SEARCH_RESULT_PARAM,Persons);
     	this.getServletConfig().getServletContext().getRequestDispatcher("/SearchPersonRsult.jsp").forward(request, response);
     	return;
     	
 
     }
     
 
 }
