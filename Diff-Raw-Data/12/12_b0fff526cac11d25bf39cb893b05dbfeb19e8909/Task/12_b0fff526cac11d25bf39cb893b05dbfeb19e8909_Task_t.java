 /**
  * 
  */
 package com.android.helpme.demo.utils;
 
 import java.util.Observable;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.json.simple.JSONObject;
 
 import com.android.helpme.demo.manager.PositionManager;
 import com.android.helpme.demo.manager.RabbitMQManager;
 import com.android.helpme.demo.manager.UserManager;
 import com.android.helpme.demo.manager.interfaces.PositionManagerInterface;
 import com.android.helpme.demo.manager.interfaces.RabbitMQManagerInterface;
 import com.android.helpme.demo.manager.interfaces.RabbitMQManagerInterface.ExchangeType;
 import com.android.helpme.demo.manager.interfaces.UserManagerInterface;
 import com.android.helpme.demo.utils.position.Position;
 
 /**
  * @author Andreas Wieland
  *
  */
 public class Task extends Observable{
 	public static final String ID = "id", START_TIME ="start_time", STOP_TIME="stop_time",SUCCESSFUL ="successful",FAILED ="failed", RUNNING = "running", LOOKING = "looking", STATE = "state";
 	public static final int LONGDISTANCE = 1000;
 	public static final int MIDDISTANCE = 100;
 	public static final int SHORTDISTANCE = 10;
	public static final long TIMERDELAY = 60000;
 	private UserInterface user;
 	private Timer timer;
 	private boolean answered;
 	private String exchangeName;
 	private Position startPosition;
 	private String state;
 	UserManagerInterface userManagerInterface;
 	RabbitMQManagerInterface rabbitMQManagerInterface;
 	PositionManagerInterface positionManagerInterface;
 
 	/**
 	 * 
 	 */
 	public Task() {
 		userManagerInterface = UserManager.getInstance();
 		rabbitMQManagerInterface = RabbitMQManager.getInstance();
 		positionManagerInterface = PositionManager.getInstance();
 		answered = false;
 		user = null;
 		state = null;
 	}
 
 	/**
 	 * gets called by Helper
 	 * @param user
 	 */
 	public void startTask(UserInterface user) {
 		run(positionManagerInterface.startLocationTracking());
 		exchangeName = user.getId();
 		startPosition = user.getPosition();
 		run(rabbitMQManagerInterface.subscribeToChannel(exchangeName, ExchangeType.fanout));
 		answered = true;
 		state = RUNNING;
 	}
 	
 	/**
 	 * gets called by Help Seeker
 	 */
 	public void startTask() {
 		run(positionManagerInterface.startLocationTracking());
 		exchangeName = userManagerInterface.getThisUser().getId();
 		run(rabbitMQManagerInterface.subscribeToChannel(exchangeName, ExchangeType.fanout));
 		state = RUNNING;
 		timer = new Timer();
 		timer.schedule(createTimerTask(), TIMERDELAY);
 	}
 	
 	public void sendPosition(Position position){
 		JSONObject object = new JSONObject();
 		object = UserManager.getInstance().getThisUser().getJsonObject();
 		userManagerInterface.thisUser().updatePosition(position);
 		object.put(User.POSITION, position.getJSON());
 		
 		if (answered) {
 			run(rabbitMQManagerInterface.sendStringOnChannel(object.toString(), exchangeName));
 		}else {
 			run(rabbitMQManagerInterface.sendStringOnMain(object.toString()));
 		}
 	}
 	
 	public Boolean isAnswered(){
 		return answered;
 	}
 	
 	private void run(Runnable runnable){
 		ThreadPool.runTask(runnable);
 	}
 	
 	private void setUser(UserInterface user) {
 		answered = true;
 		this.user = user;
 	}
 	public double getDistance(){
 		Position helperPosition = this.user.getPosition();
 		Position ourPosition = userManagerInterface.thisUser().getPosition();
 		return helperPosition.calculateSphereDistance(ourPosition);
 	}
 	
 	public void updatePosition(UserInterface userInterface) {
 		// if our Task is not answered yet, with this it is now
 		if (!answered) {
 			setUser(userInterface);
 		}else {
 			this.user.updatePosition(userInterface.getPosition());
 		}
 	}
 	
 	public boolean isUserInRange(int range) {
 		return userManagerInterface.getThisUser().getDistanceTo(user) <= range;
 	}
 	
 	public boolean isUserInShortDistance(){
 		return isUserInRange(SHORTDISTANCE);
 	}
 	
 	public boolean isUserInMidDistance() {
 		return isUserInRange(MIDDISTANCE);
 	}
 	
 	public boolean isUserInLongDistance() {
 		return isUserInRange(LONGDISTANCE);
 	}
 	
 	public void setSuccesfull() {
 		if (state.equalsIgnoreCase(RUNNING) && answered) {
 			state = SUCCESSFUL;
 		}
 		
 	}
 	
 	public String state(){
 		return state;
 	}
 	
 	public void setFailed() {
 		if (state.equalsIgnoreCase(RUNNING)) {
 			state = FAILED;
 		}
 	}
 	
 	public void stopUnfinishedTask(){
 		run(positionManagerInterface.stopLocationTracking());
 		run(rabbitMQManagerInterface.endSubscribtionToChannel(exchangeName));
 	}
 	
 	public JSONObject stopTask() {
 		run(positionManagerInterface.stopLocationTracking());
 		run(rabbitMQManagerInterface.endSubscribtionToChannel(exchangeName));
 		JSONObject jsonObject = new JSONObject();
 		jsonObject.put(ID, user.getId());
 		jsonObject.put(START_TIME, startPosition.getMeasureDateTime());
 		jsonObject.put(STOP_TIME, user.getPosition().getMeasureDateTime());
 		jsonObject.put(STATE, state);
 		return jsonObject;
 	}
 	
 	private TimerTask createTimerTask(){
 		return new TimerTask() {
 			
 			@Override
 			public void run() {
 				if (!answered) {
 					setChanged();
 					notifyObservers();
 				}
 			}
 		};
 	}
 }
