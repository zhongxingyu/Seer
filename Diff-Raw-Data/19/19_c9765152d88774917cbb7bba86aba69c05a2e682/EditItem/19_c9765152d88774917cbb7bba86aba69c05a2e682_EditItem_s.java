 package com.ulyanova.todo.web;
 
 import com.ulyanova.todo.service.Controller;
 import com.ulyanova.todo.service.ControllerImpl;
 import com.ulyanova.todo.service.StringToDate;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.text.ParseException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ainex
  * Date: 15.09.13
  * Time: 19:08
  * To change this template use File | Settings | File Templates.
  */
 public class EditItem implements Action {
 
 
     @Override
     public void perform(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
         Controller controller = new ControllerImpl();
         String folderName = request.getParameter("folderName");
         String  task = request.getParameter("task-description");
         String dateAndTimeStr = request.getParameter("exp-date") + " "
                 +request.getParameter("exp-time");
         java.util.Date date = null;
         try {
             date = StringToDate.getDate(dateAndTimeStr);
             folderName = "EDUCATION";
            controller.addToDoItem(date, task, folderName);
         } catch (ParseException e) {
             e.printStackTrace();
         }
     }
 }
