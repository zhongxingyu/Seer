 package fr.cpcgifts;
 
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
import fr.cpcgifts.model.CpcUser;
 import fr.cpcgifts.model.Giveaway;
import fr.cpcgifts.persistance.CpcUserPersistance;
 import fr.cpcgifts.persistance.GAPersistance;
 
 @SuppressWarnings("serial")
 public class CloseGAServlet extends HttpServlet {
 
 	private static final Logger log = Logger.getLogger(CloseGAServlet.class.getName());
 	
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException {
 		
 		Calendar c = Calendar.getInstance();
 		
 		List<Giveaway> gas = GAPersistance.getOpenGAs(false);
 		
 		for(Giveaway ga : gas) {
 			if(ga.getEndDate().getTime() < c.getTimeInMillis()) {
 				ga.drawWinner();
 				
				log.info("Giveaway " + ga.getKey().getId() + " ended.");
				
				if(ga.getWinner() != null) {
					CpcUser winner = CpcUserPersistance.getCpcUserUndetached(ga.getWinner());
					winner.addWon(ga.getKey());
					CpcUserPersistance.closePm();
					
					log.info("Winner is " + winner.getCpcNickname() + " !");
				}
 				
 			}
 		}
 		
 		GAPersistance.closePm();
 		
 	}
 	
 	
 }
