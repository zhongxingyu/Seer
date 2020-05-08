 package monopoly.web.servlet;
 
 import java.io.IOException;
 import java.util.Arrays;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import monopoly.Player;
 import monopoly.ReadPlayer;
 import monopoly.web.util.MQ;
 
 /**
  * Servlet implementation class MonopolySubmit
  */
 @WebServlet("/MonopolySubmit")
 public class MonopolySubmit extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// Example:
 		// http://localhost:8080/Java2013git/MonopolySubmit?player=Alfred&submit=YES"
		final String name = request.getParameter("name");
 		final String submit = request.getParameter("submit");
 		if (name == null || submit == null
 				|| !Arrays.asList(GameRunner.PLAYER_NAMES).contains(name)) {
 			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
 		} else {
 			MQ<GameRequest> q = (MQ<GameRequest>) this.getServletContext()
					.getAttribute("monopoly.respQ");
 			q.submitRequest(new GameRequest() {
 				
 				@Override
 				public boolean isYes() {
 					return submit.matches("[Yy][Ee][Ss]");
 				}
 				
 				@Override
 				public ReadPlayer getPlayer() {
 					return new Player(name);
 				}
 				
 				@Override
 				public int getChoice() {
 					// TODO Auto-generated method stub
 					return 0;
 				}
 			});
 		}
 	}
 
 }
