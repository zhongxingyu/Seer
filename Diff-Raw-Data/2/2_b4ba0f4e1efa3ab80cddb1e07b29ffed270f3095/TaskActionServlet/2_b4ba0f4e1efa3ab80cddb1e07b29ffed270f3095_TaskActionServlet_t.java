 package org.levi.web;
 
 import org.levi.engine.db.DBManager;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 
 /**
  * Created by IntelliJ IDEA.
  * User: umashanthi
  * Date: 5/24/11
  * Time: 10:56 AM
  * To change this template use File | Settings | File Templates.
  */
 
 public class TaskActionServlet extends HttpServlet {
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         doGet(request, response);
     }
 
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         String action = request.getParameter("action");
         assert action != null;
         if (action.equals("claimTask")) {
             assert request.getParameter("username") != null;
             String username = request.getParameter("username");
             assert request.getParameter("taskId") != null;
            String taskId = request.getParameter("taskId");
             assert request.getParameter("processInstanceId") != null;
             String processInstanceId = request.getParameter("processInstanceId");
             assert request.getSession().getAttribute("dbManager") != null;
             DBManager dbManager = (DBManager) request.getSession().getAttribute("dbManager");
             dbManager.claimUserTask(taskId, processInstanceId, username);
             response.sendRedirect("tasks");
         } else {
             //other task actions should be handled here
         }
     }
 }
