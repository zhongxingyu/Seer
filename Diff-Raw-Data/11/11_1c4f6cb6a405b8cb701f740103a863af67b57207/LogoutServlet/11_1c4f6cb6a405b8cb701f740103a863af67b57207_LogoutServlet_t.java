 package eionet.meta;
 
 import java.io.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import com.tee.xmlserver.*;
 
 import eionet.util.SecurityUtil;
 import eionet.meta.filters.EionetCASFilter;
 
 /**
  * Simple servlet to handle logout form submit.
  *
  * @author  Jaanus Heinlaid
  */
 
 public class LogoutServlet extends HttpServlet {
     
     /** */
 	public static final String LOGOUT_PAGE = "logout-page";
 
     /*
      *  (non-Javadoc)
      * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
      */
     public void service(HttpServletRequest req, HttpServletResponse res)
     										throws ServletException, IOException {
         
 		req.setCharacterEncoding("UTF-8");
 		
         AppUserIF user = SecurityUtil.getUser(req);
         if (user != null)
             SecurityUtil.freeSession(req);
         
        String dontUseCasLogin = getServletContext().getInitParameter(LoginServlet.INITPARAM_DONT_USE_CAS_LOGIN);
        if (dontUseCasLogin!=null)
        	res.sendRedirect("index.jsp");
        else{
			EionetCASFilter.attachEionetLoginCookie(res,false);
			res.sendRedirect(EionetCASFilter.getCASLogoutURL(req));
        }
     }
 }
