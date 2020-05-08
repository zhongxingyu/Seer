 package myaction;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.apache.struts2.interceptor.SessionAware;
 
 import Bowling.Dao.ServiceDao;
 import Bowling.entities.Game;
 import Bowling.entities.Player;
 import Bowling.entities.Score;
 
 import com.opensymphony.xwork2.ActionSupport;
 
 public class SubmitBall extends ActionSupport implements SessionAware {
 
 	private static final Logger logger = Logger.getLogger(ActionSupport.class);
 
 	private Map<String, Object> session;
 	private int currentball;
	private Game currentGame = ServiceDao.getCurrentGame();;
 	private String frameValue;
 	private Player currentplayer;
 	private int currentframe;
 	
 	public String execute() {
 		
 		currentplayer = (Player) session.get("currentPlayer");
 		currentframe = (Integer) session.get("currentFrameNo");
 		currentball = (Integer) session.get("currentBall");
 		
 		boolean islastframe = BowlingSupport.isLastFrame(currentplayer,
 				currentframe);
 
 		Score currentscore = ServiceDao.getScoreByGameAndPlayer(currentGame,
 				currentplayer);
 
 		if (currentball == 1 && !frameValue.equals("x")) {
 			currentscore.getFrames().get(currentframe).setBall1(frameValue);
 			session.put("currentBall", 2);
 		}else if (currentball == 2 && islastframe) {
 			currentscore.getFrames().get(currentframe).setBall2(frameValue);
 			session.clear();
 			logger.debug("Game finished");
 			return "FINISH";
 
 		} else {
 			currentscore.getFrames().get(currentframe).setBall2(frameValue);
 			session.put("currentPlayer",
 					BowlingSupport.getNextPlayerCurrentGame(currentplayer));
 			session.put("currentFrameNo", BowlingSupport
 					.getNextFrameNoCurrentGamePlayer(currentplayer,
 							currentframe));
 			session.put("currentBall", 1);
 		}
 
 		ServiceDao.persistScore(currentscore);
 		currentGame.getScores().put(currentplayer, currentscore);
 		ServiceDao.persistGame(currentGame);
 		logger.debug("Ball submited");
 		return SUCCESS;
 
 	}
 
 	public void validate() {
 
 		if (frameValue.trim().equals("")) {
 			addFieldError("frameValue", "type '-' if no pins are knocked down");
 		}
 	}
 
 	public Player getCurrentplayer() {
 		return currentplayer;
 	}
 
 	public void setCurrentplayer(Player currentplayer) {
 		this.currentplayer = currentplayer;
 	}
 
 	public int getCurrentframe() {
 		return currentframe;
 	}
 
 	public void setCurrentframe(int currentframe) {
 		this.currentframe = currentframe;
 	}
 
 	public int getCurrentball() {
 		return currentball;
 	}
 
 	public void setCurrentball(int currentball) {
 		this.currentball = currentball;
 	}
 	
 	public Map<String, Object> getSession() {
 		return session;
 	}
 
 	public void setSession(Map<String, Object> session) {
 		this.session = session;
 	}
 
 	public Game getCurrentGame() {
 		return currentGame;
 	}
 
 	public void setCurrentGame(Game currentGame) {
 		this.currentGame = currentGame;
 	}
 
 	public String getFrameValue() {
 		return frameValue;
 	}
 
 	public void setFrameValue(String frameValue) {
 		this.frameValue = frameValue;
 	}
 }
