 package org.cvut.wa2.projectcontrol;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.cvut.wa2.projectcontrol.DAO.TaskDAO;
 import org.cvut.wa2.projectcontrol.entities.CompositeTask;
 import org.cvut.wa2.projectcontrol.entities.Task;
 
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 
 public class FilterByAccountServlet extends HttpServlet{
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		UserService userService = UserServiceFactory.getUserService();
 		RequestDispatcher disp = null;
 		if (userService.isUserLoggedIn()) {
			String account = (String)req.getAttribute("account");
 			List<CompositeTask> listOfCT = TaskDAO.getTasks();
 			List<CompositeTask> toRet = new ArrayList<CompositeTask>();
 			for (CompositeTask compositeTask : listOfCT) {
 				CompositeTask newCT = addCompoTaskIfSubtask(compositeTask, account);
 				if(newCT != null){
 					toRet.add(newCT);
 				}
 			}
 			req.setAttribute("listOfTasks", toRet);
 			disp = req.getRequestDispatcher("/Tasks.jsp");
 			disp.forward(req, resp);
 			
 			
 		}else{
 			resp.sendRedirect("/projectcontrol");
 		}
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		doGet(req, resp);
 	}
 	
 	private CompositeTask addCompoTaskIfSubtask(CompositeTask ct, String acc){
 		List<Task> listOfTasks = ct.getSubtasks();
 		CompositeTask compTask = new CompositeTask(ct.getOwner(),ct.getTaskName(),ct.getDateOfStartDelivery(),null);
 		ArrayList<Task> toAdd = new ArrayList<Task>();
 		boolean returnObject = false;
 		for (Task task : listOfTasks) {
 			if(task.getResponsible().equals(acc)){
 				toAdd.add(task);
 				returnObject = true;
 			}
 		}
 		if(returnObject){
 			compTask.setSubtasks(toAdd);
 			return compTask;
 		}
 		return null;
 	}
 
 }
