 package com.in6k.servlet;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.List;
 
 import com.in6k.dao.UserDao;
 import com.in6k.entity.User;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class UserServlet extends HttpServlet {
     private final Log logger = LogFactory.getLog(getClass());
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         List usersList = null;
 
         try {
             usersList = UserDao.getAll();
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         request.setAttribute("usersList", usersList);
         request.getRequestDispatcher("index.jsp").include(request,response);
     }
 
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        logger.info(request.getParameter("user_name"));
        logger.info(request.getParameterValues("user_type")[0]);

         List errorsList = null;
 
         List usersList = null;
         User user = new User();
 
         user.setName(request.getParameter("user_name"));
         user.setId(1);
 
         errorsList = user.validate();
         boolean isUserValidate = errorsList.size() != 0;
 
         if(!isUserValidate) {
             UserDao.save(user);
         }
 
         try {
             usersList = UserDao.getAll();
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         request.setAttribute("usersList", usersList);
         request.setAttribute("errorsList", errorsList);
 
         request.getRequestDispatcher("index.jsp").include(request,response);
     }
 }
