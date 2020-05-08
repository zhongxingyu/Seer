 package org.levi.web;
 
 import org.hibernate.Hibernate;
 import org.levi.engine.ProcessEngine;
 import org.levi.engine.db.DBManager;
 import org.levi.engine.impl.ProcessEngineImpl;
 import org.levi.engine.impl.db.DBManagerImpl;
 import org.levi.engine.persistence.hibernate.HibernateDao;
 import org.levi.engine.persistence.hibernate.user.hobj.UserBean;
 
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 public class LoginServlet extends javax.servlet.http.HttpServlet {
     protected void doPost(javax.servlet.http.HttpServletRequest request,
                           javax.servlet.http.HttpServletResponse response)
             throws javax.servlet.ServletException, IOException {
         response.setContentType("text/html");
         PrintWriter out = response.getWriter();
 
         String username = request.getParameter("username");
         String password = request.getParameter("password");
 
         if (!allowUser(username, password)) {
             response.sendRedirect("login.jsp?isLoginSuccess=false");
         } else {
             // Valid login. Make a note in the session object.
             HttpSession session = request.getSession();
             session.setAttribute("username", username);
             session.setAttribute("logged", "true");
             // Try redirecting the client to the page he first tried to access
             try {
                 String target = (String) session.getAttribute("login.target");
                 if (target != null) {
                     response.sendRedirect(target);
                     return;
                 }
             } catch (Exception ignored) {
             }
             // Start Engine
             ProcessEngine engine = ProcessEngineImpl.getProcessEngine();
             request.getSession().setAttribute("processEngine", engine);
             DBManager dbManager = new DBManagerImpl();
             request.getSession().setAttribute("dbManager", dbManager);
             //TODO: Need to change!!!
             HibernateDao dao = new HibernateDao();
             UserBean user = (UserBean) dao.getObject(UserBean.class, username);
             if (user == null) {
                 //no user exists in the DB
                 user = new UserBean();
                 user.setUserId(username);
                 user.setPassword(password);
                 dao.save(user);
             }
             user = (UserBean) dao.getObject(UserBean.class, "admin");
             if (user == null) {
                 //no user exists in the DB
                 user = new UserBean();
                 user.setUserId("admin");
                 user.setPassword("admin");
                 dao.save(user);
             }
            user = (UserBean) dao.getObject(UserBean.class, "john");
            if (user == null) {
                //no user exists in the DB
                user = new UserBean();
                user.setUserId("john");
                user.setPassword("john");
                dao.save(user);
            }
             dao.close();
             // Couldn't redirect to the target. Redirect to the site's home page.
             response.sendRedirect("index.jsp");
         }
 
     }
 
     protected boolean allowUser(String username, String password) {
         //TODO: Handle UserBean Authentication here
 
         if (username.trim().equals("admin") && password.trim().equals("admin")) {
 
             return true;
         } else if (username.trim().equals("john") && password.trim().equals("john")) {
             return true;
         } else {
             return false;
         }
     }
 
     protected void doGet(javax.servlet.http.HttpServletRequest request,
                          javax.servlet.http.HttpServletResponse response)
             throws javax.servlet.ServletException, IOException {
 
     }
 }
