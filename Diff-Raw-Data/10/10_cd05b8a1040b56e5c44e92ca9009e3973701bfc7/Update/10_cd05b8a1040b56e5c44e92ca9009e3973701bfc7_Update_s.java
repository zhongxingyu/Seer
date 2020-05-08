 package com.kain.tom.dioe;
 
 import java.io.IOException;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Servlet implementation class Update
  */
 @WebServlet("/Update")
 public class Update extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		String currentTaskId = request.getParameter("task_id");
 		Task taskToUpdate;
 		try {
 			taskToUpdate = TaskHelper.findTaskByID(Integer
 					.valueOf(currentTaskId));
 			taskToUpdate.setTaskName(request.getParameter("task_name"));
 			String dueDateParameter = request.getParameter("due_date");
 			if (dueDateParameter != null && dueDateParameter.length()>0) {
 				taskToUpdate.setDueDateString(request.getParameter("due_date"));
 			} else {
 				taskToUpdate.setDueDate(null);
 			}
			taskToUpdate.setPriority(Priority.valueOf(request.getParameter("priority")));
 			String isDone = request.getParameter("is_done");
 			if (isDone == null) {
 				taskToUpdate.setIsDone(false);
 			} else { 
 				taskToUpdate.setIsDone(true);
 			}
 			TaskHelper.updateTask(taskToUpdate);
 		} catch (NumberFormatException e) {
 			e.printStackTrace();
 		}
 		response.sendRedirect("Home");
 	}
 
 }
