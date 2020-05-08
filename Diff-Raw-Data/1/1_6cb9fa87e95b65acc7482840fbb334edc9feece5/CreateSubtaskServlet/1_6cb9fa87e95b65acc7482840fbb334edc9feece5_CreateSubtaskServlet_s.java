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
 import org.cvut.wa2.projectcontrol.DAO.TeamDAO;
 import org.cvut.wa2.projectcontrol.entities.CompositeTask;
 import org.cvut.wa2.projectcontrol.entities.Team;
 import org.cvut.wa2.projectcontrol.entities.TeamMember;
 
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 
 public class CreateSubtaskServlet extends HttpServlet {
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		UserService userService = UserServiceFactory.getUserService();
 		RequestDispatcher disp = null;
 		if (userService.isUserLoggedIn()) {
 			String taskName = (String) req.getParameter("taskName");
 			CompositeTask ct = TaskDAO.getTask(taskName);
 			Team team = TeamDAO.getTeam(ct.getOwner());
 			List<String> listOfEmails = new ArrayList<String>();
 			for (TeamMember mem : team.getMembers()) {
 				listOfEmails.add(mem.getName());
 			}
 			req.setAttribute("emails", listOfEmails);
 			disp = req.getRequestDispatcher("CreateSubtask.jsp");
 			disp.forward(req, resp);
 		} else {
 			disp = req.getRequestDispatcher("/projectcontrol");
 			disp.forward(req, resp);
 		}
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		doGet(req, resp);
 	}
 
 }
