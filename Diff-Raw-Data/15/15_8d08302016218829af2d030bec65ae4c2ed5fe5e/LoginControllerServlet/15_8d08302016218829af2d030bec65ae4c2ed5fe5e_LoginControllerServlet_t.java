 package com.servlets.logining;
 
 import com.Params;
 import com.database.DataBaseConnector;
 import com.model.User;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Alex
  * Date: 07.06.13
  * Time: 10:53
  * To change this template use File | Settings | File Templates.
  */
 public class LoginControllerServlet extends HttpServlet {
 
     protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         String redirectAddress;
         HttpSession session = request.getSession();
 
         redirectAddress = Params.INDEX_JSP;
 
         String login = (String) session.getAttribute( Params.login );
         if ( login != null ){
             User user = DataBaseConnector.getUserDataBaseManager().getUser( login );
             if ( user!=null ){
                 SessionHelper.setParam(request, Params.login, user.getLogin(), Params.inactiveInterval);
                 redirectAddress = Params.USER_CABINET;
            }else{
                redirectAddress += Params.msgUnknown;
             }
         }
 
        response.sendRedirect( request.getContextPath() + redirectAddress );
     }
 }
