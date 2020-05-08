 package jp.gihyo.jenkinsbook.action;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import jp.gihyo.jenkinsbook.dto.SampleDTO;
 
 /**
  * DTO class for SampleServlet.
  */
 public class SampleAction {
     /**
      * First name of the user.
      */
     private String firstName;
     /**
      * Last name of the user.
      */
     private String lastName;
 
     /**
      * Constructor of SampleAction.
      */
     public SampleAction() {
         this(null, null);
     }
 
     /**
      * Constructor of SampleAction.
      * @param firstName first name of the user
      * @param lastName last name of the user
      */
     public SampleAction(final String firstName, final String lastName) {
         this.firstName = firstName;
         this.lastName = lastName;
     }
 
     /**
      * Check parameter of http servlet request.
      * @param request HttpServletRequest
      * @return check result
      */
     public final boolean checkParameter(final HttpServletRequest request) {
 
         firstName = request.getParameter("FirstName");
         if ((firstName == null) || ("".equals(firstName))) {
             return false;
         }
 
         lastName = request.getParameter("LastName");
         if ((lastName == null) || ("".equals(lastName))) {
            return true;
         }
 
         return true;
     }
 
     /**
      * Execute action.
      * @param request HttpServletRequest
      * @return result jsp file path
      */
     public final String execute(final HttpServletRequest request) {
         SampleDTO dto = new SampleDTO(firstName, lastName);
 
         HttpSession session = request.getSession(true);
         session.setAttribute("dto", dto);
 
         return "./WEB-INF/result.jsp";
     }
 }
