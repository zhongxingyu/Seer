 
 package jamm.webapp;
 
 import jamm.ldap.LdapFacade;
 import jamm.ldap.LdapPassword;
 import jamm.ldap.PasswordScheme;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import javax.servlet.ServletException;
 
 import javax.naming.NamingException;
 import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
 
 
 /**
  * Changes an LDAP password.
  *
  * @web:servlet name="change-password"
  * @web:servlet-mapping url-pattern="/password/change"
  */
 public class ChangePasswordServlet extends HttpServlet
 {
     protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
         throws ServletException
     {
         String user;
         String oldPassword;
         String newPassword1;
         String newPassword2;
         String hashedPassword;
         LdapFacade ldap;
         HttpSession session;
         Attributes modifiedAttributes;
 
         ldap = null;
         try
         {
             session = request.getSession();
             user = request.getParameter("user");
             oldPassword = request.getParameter("old_password");
             newPassword1 = request.getParameter("new_password_1");
             newPassword2 = request.getParameter("new_password_2");
 
             if (user.equals("") ||
                 oldPassword.equals("") ||
                 newPassword1.equals("") ||
                 newPassword2.equals(""))
             {
                 session.setAttribute("error",
                                      "Please fill in all fields");
                 response.sendRedirect("error.jsp");
                 return;
             }
 
             ldap = authenticate(user, oldPassword);
                 
             if (ldap == null)
             {
                 session.setAttribute("error",
                                      "Invalid user and password");
                 response.sendRedirect("error.jsp");
                 return;
             }
 
 
             if (!newPassword1.equals(newPassword2))
             {
                 session.setAttribute("error",
                                      "New passwords do not match");
                 response.sendRedirect("error.jsp");
                 return;
             }
 
             hashedPassword =
                 LdapPassword.hash(PasswordScheme.SSHA_SCHEME, newPassword1);
             ldap.modifyElementAttribute(ldap.getName(), "userPassword",
                                         hashedPassword);
 
             response.sendRedirect("success.jsp");
         }
         catch (IOException e)
         {
             throw new ServletException(e);
         }
         catch (NamingException e)
         {
             throw new ServletException(e);
         }
         finally
         {
             closeLdap(ldap);
         }
     }
 
     private LdapFacade authenticate(String user, String password)
     {
         LdapFacade ldap;
         String name;
 
         ldap = null;
         try
         {
             ldap = new LdapFacade(Globals.getLdapHost());
             ldap.anonymousBind();
             ldap.searchSubtree(Globals.getLdapSearchBase(),
                                Globals.getLdapQueryFilter(user));
             if (!ldap.nextResult())
             {
                 ldap.close();
                 ldap = null;
                 return ldap;
             }
 
             name = ldap.getResultName();
             ldap.close();
             ldap.simpleBind(name, password);
         }
         catch (NamingException e)
         {
             closeLdap(ldap);
             ldap = null;
         }
         
         return ldap;
     }
     
     private void closeLdap(LdapFacade ldap)
     {
         if (ldap != null)
         {
             ldap.close();
         }
     }
 }
