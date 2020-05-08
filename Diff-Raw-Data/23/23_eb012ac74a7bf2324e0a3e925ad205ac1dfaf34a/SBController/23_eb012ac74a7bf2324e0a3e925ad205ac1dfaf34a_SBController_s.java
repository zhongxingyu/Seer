 package epam.ph.sg.controllers;
 
 /**
  * @author Gutey Bogdan
  */
 import java.io.IOException;
 
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 import epam.ph.sg.models.sb.ActiveGames;
 import epam.ph.sg.models.sb.BSBoard;
 import epam.ph.sg.models.sb.BSSheeps;
 import epam.ph.sg.models.sb.FireResponse;
 import epam.ph.sg.models.sb.Game;
 import epam.ph.sg.models.sb.JsonParser;
 
 @Controller
 @SessionAttributes("sbJSLoader")
 public class SBController {
 	private static Logger log = Logger.getLogger(SBController.class);
 
 	
 
 	@RequestMapping(value = { "/init_sheeps.html" }, method = RequestMethod.POST)
 	public @ResponseBody
 	String sheeps_init(@RequestParam("sheeps") String sheeps,
 			Model model,	HttpSession session) {
 		
 		String connectionType = (String)session.getAttribute("ConnectionType");
 		int gameID = (int)((Game)session.getAttribute("Game")).getId();
 		JsonParser jp = new JsonParser();
 		BSBoard sc = jp.parseJsonSheepsCoordenates(sheeps);
 		if(connectionType.equalsIgnoreCase("server"))
 		{
 			ActiveGames.getGame(gameID).getServer().setGameBoard(sc);
 		}
 		if(connectionType.equalsIgnoreCase("client"))
 		{
 			ActiveGames.getGame(gameID).getClient().setGameBoard(sc);
 		}
 		
 		return "Server says: array recieved! ;-)"+sc ;
 	}
 
 	
 	@RequestMapping(value = {"/fire.html"}, method = RequestMethod.POST)
 	public @ResponseBody
 	FireResponse fireReciever(@RequestParam("firePoint") String firePoint,
 			Model model, HttpSession session) {
//		log.debug("session="+session.getId());
 //		if(!(session.getId().equalsIgnoreCase(LoginController.getSessionid())))
 //		{
 //			log.error("session==null");
 //		}
 		String connectionType = (String)session.getAttribute("ConnectionType");
 		Game g = (Game)session.getAttribute("Game");
 		int gameID = (int)g.getId();
 		log.debug(firePoint);
 		log.debug(connectionType);
 		log.debug(gameID);
 		FireResponse fr = g.fireCheck(gameID, connectionType, firePoint);
 		return  fr;
 	}
 	
 	@RequestMapping(value = {"/mess.html"}, method = RequestMethod.POST)
 	public @ResponseBody
 	String messReciever(@RequestParam("mess") String mess, HttpSession session) {
 		session.setAttribute("mess", mess);
 		return  "";
 	}
 
 	
 	@RequestMapping(value = {"/sheepsReady.html"}, method = RequestMethod.POST)
 	public @ResponseBody String SheepsReady(@RequestParam("sheepsReady") String sheepsReady,
 			@RequestParam("connType") String connType,
 			Model model, HttpSession session) {
 		
 		JsonParser jp = new JsonParser();
 		BSSheeps sheeps = jp.parseJsonSheepsReady(sheepsReady);
 		log.debug("ttteeesssttt  "+sheeps);
 		log.debug("connType= "+connType);
 		session.setAttribute("Sheeps", sheeps);
 		Game g = (Game)session.getAttribute("Game");
 		if(connType.equals("server"))
 		{
 			g.getServer().setStarted(true);
 			try {
 				g.getClient().getConn().sendMessage("ready");
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}else if(connType.equals("client"))
 		{
 			g.getClient().setStarted(true);
 			try {
 				g.getServer().getConn().sendMessage("ready");
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return "OK";
 	}
 	
 }
