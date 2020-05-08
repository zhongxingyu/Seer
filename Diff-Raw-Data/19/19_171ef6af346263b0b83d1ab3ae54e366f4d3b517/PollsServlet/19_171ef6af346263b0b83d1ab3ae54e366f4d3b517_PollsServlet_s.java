 package fr.intechinfo.servlets;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.mycompany.dao.exceptions.DAOException;
 import org.mycompany.dao.factories.DAOFactory;
 import org.mycompany.dao.interfaces.IPollDAO;
 import org.mycompany.dao.interfaces.IVoteDAO;
 import org.mycompany.dao.models.Poll;
 import org.mycompany.dao.models.User;
 import org.mycompany.dao.models.Vote;
 
@WebServlet("/Polls")
 public class PollsServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
     
     public PollsServlet() {
         super();
     }
     
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		HttpSession session = request.getSession();
 		
 		DAOFactory daoFactory = DAOFactory.getInstance("polls");
 		
 		IPollDAO pollDAO = daoFactory.getPollDAO();
 		IVoteDAO voteDAO = daoFactory.getVoteDAO();
 		
		try {
			List<Poll> polls = pollDAO.findAll();
			request.setAttribute("polls", polls);
			List<Vote> allVotes = voteDAO.findAll();
			request.setAttribute("allVotes", allVotes);
			
 			User user = (User)session.getAttribute("user");
 			if(user!=null) {
 				List<Poll> unvotedPolls = pollDAO.getUnvotedPollsForUser(user);
 				request.setAttribute("pollsUnvoted", unvotedPolls);
 				List<Vote> votes = voteDAO.findAllByUser(user);
 				request.setAttribute("votes", votes);
 			}
 			
 		} catch (DAOException e) {
 			e.printStackTrace();
 		}
 		
 		request.getRequestDispatcher("/WEB-INF/jsp/page/home.jsp").forward(request, response);
 	}
 	
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {}
 
 }
