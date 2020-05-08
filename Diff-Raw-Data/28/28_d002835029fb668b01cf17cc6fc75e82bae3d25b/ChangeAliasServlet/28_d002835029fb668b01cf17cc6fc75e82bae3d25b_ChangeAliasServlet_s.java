 
 package jamm.webapp;
 
 import jamm.ldap.LdapFacade;
 
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
  * @web:servlet name="change-alias"
  * @web:servlet-mapping url-pattern="/alias/change"
  */
 public class ChangeAliasServlet extends HttpServlet
 {
     protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
         throws ServletException
     {
         String user;
         String password;
         String newAlias;
         LdapFacade ldap;
         HttpSession session;
         Attributes modifiedAttributes;
 
         ldap = null;
         try
         {
             session = request.getSession();
             user = request.getParameter("user");
             password = request.getParameter("password");
             newAlias = request.getParameter("new_alias");
 
             if (user.equals("") ||
                 password.equals("") ||
                 newAlias.equals(""))
             {
                 session.setAttribute("error",
                                      "Please fill in all fields");
                 response.sendRedirect("error.jsp");
                 return;
             }
 
             ldap = authenticate(user, password);
                 
             if (ldap == null)
             {
                 session.setAttribute("error",
                                      "Invalid user or password");
                 response.sendRedirect("error.jsp");
                 return;
             }
 
             ldap.modifyElementAttribute(ldap.getName(), "maildrop", newAlias);
 
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
