 package mage.tracker.filter;
 
 import java.io.IOException;
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import mage.tracker.domain.Account;
 import mage.tracker.authentication.AuthenticationContext;
 import mage.tracker.service.CardService;
 import org.springframework.beans.factory.annotation.Autowired;
 
 /**
  *
  * @author North
  */
 public class AuthenticationFilter implements Filter {
 
     private static final String ACCOUNT_KEY = "AuthenticatedUser";
     @Autowired
     CardService cardService;
 
     @Override
     public void init(FilterConfig filterConfig) throws ServletException {
     }
 
     @Override
     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
         HttpServletRequest httpRequest = (HttpServletRequest) request;
         HttpSession session = httpRequest.getSession();
 
         String name = httpRequest.getParameter("name");
         String password = httpRequest.getParameter("password");
         Account account = (Account) session.getAttribute(ACCOUNT_KEY);
         if (account == null || (account != null && !account.getName().equals(name) || !account.getPassword().equals(password))) {
             account = cardService.authenticateAccount(name, password);
             session.setAttribute(ACCOUNT_KEY, account);
         }
 
         AuthenticationContext.setAccount(account);
        chain.doFilter(request, response);
     }
 
     @Override
     public void destroy() {
     }
 }
