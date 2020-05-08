 package org.cvut.wa2.projectcontrol;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.jdo.JDOObjectNotFoundException;
 import javax.jdo.PersistenceManager;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.cvut.wa2.projectcontrol.DAO.PMF;
 import org.cvut.wa2.projectcontrol.entities.Team;
 import org.cvut.wa2.projectcontrol.entities.TeamMember;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 
 public class CreateTeamServlet extends HttpServlet {
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		UserService service = UserServiceFactory.getUserService();
 		User user = service.getCurrentUser();
 		if (user != null) {
 			String teamName = req.getParameter("teamName");
 			RequestDispatcher disp = req.getRequestDispatcher("CreateTeam.jsp");
 			if (teamName.trim().equals("")) {
 				disp.forward(req, resp);
 			} else {
 				PersistenceManager manager = PMF.get().getPersistenceManager();
 				Team team = null;
 				try {
					team = manager.getObjectById(Team.class, teamName);
 					disp.forward(req, resp);
 				} catch (JDOObjectNotFoundException e) {
 					Team newTeam = new Team();
 					newTeam.setTeamKey(KeyFactory.createKey(
 							Team.class.getSimpleName(), teamName));
 					newTeam.setName(teamName);
 					newTeam.setMembers(new ArrayList<TeamMember>());
 					manager.makePersistent(newTeam);
 					req.setAttribute("team", newTeam);
 					disp = req.getRequestDispatcher("EditTeam.jsp");
 					disp.forward(req, resp);
 				}
 			}
 		} else {
 			resp.sendRedirect("/projectcontrol");
 		}
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		doGet(req, resp);
 	}
 
 }
