 package fr.cpcgifts;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import fr.cpcgifts.model.Giveaway;
 import fr.cpcgifts.persistance.GAPersistance;
 
 @SuppressWarnings("serial")
 public class CloseGAServlet extends HttpServlet {
 
 	@SuppressWarnings("unused")
 	private static final Logger log = Logger.getLogger(CloseGAServlet.class.getName());
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		
 		List<Giveaway> gas = GAPersistance.getOpenGAsToClose(false);
 		
 		for(Giveaway ga : gas) {
 			ga.drawWinner();
 		}
 		
 		GAPersistance.closePm();
 		
 	}
 	
 	
 }
