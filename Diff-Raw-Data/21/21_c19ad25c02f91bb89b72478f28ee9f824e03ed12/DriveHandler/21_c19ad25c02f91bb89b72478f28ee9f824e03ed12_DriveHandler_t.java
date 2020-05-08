 package handler;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.tinkerforge.BrickServo;
 
 import core.Action;
 import core.Connection;
 import core.Handler;
 
 
 public class DriveHandler extends Handler{
 	private short speed;
 	private short servo0And1 = (short)((1 << 0) | (1 << 1) | (1 << 7));
 	private short servo0 = (short)0;
 	private short servo1 = (short)1;
 	
 	private final short SPEED_FACTOR = 900;
 	
 	private BrickServo servoBrick = Connection.getInstance().getServoBrick();
 	
 	public void serve(Action action, HttpServletRequest request, HttpServletResponse response) {
 		// /Drive/<direction>/<speed>
 		List<String> parameters = action.getParameters();
 		if(parameters.size() != 2){
 			sendResponse(HttpServletResponse.SC_BAD_REQUEST, "Wrong URL format, use /Drive/<direction>/<speed>", response);
 			return;
 		}
 		int inputSpeed = Integer.valueOf(parameters.get(1));
 		if(inputSpeed > 10){
 			sendResponse(HttpServletResponse.SC_BAD_REQUEST, "Speed is to high. Maximum of 10 allowed", response);
 			return;
 		}
 		this.speed = (short)(inputSpeed*SPEED_FACTOR);
 		String direction = parameters.get(0); 
 		Object resultString = null;
 		try {
 			Method method = this.getClass().getDeclaredMethod(direction);
 			resultString = method.invoke(this);
		} catch (NoSuchMethodException e) {
 			sendResponse(HttpServletResponse.SC_BAD_REQUEST, "Direction " + direction + " does not exist", response);
 			return;
		} catch (IllegalAccessException e) {
			sendResponse(HttpServletResponse.SC_BAD_REQUEST, "Could not call method "+direction, response);
			return;
		} catch (InvocationTargetException e) {
			sendResponse(HttpServletResponse.SC_BAD_REQUEST, "Could not call method "+direction, response);
			return;
 		}
 		sendResponse(HttpServletResponse.SC_OK, (String)resultString, response);
 	}
 	
 	
 	@SuppressWarnings("unused")
 	private String forward(){
 		servoBrick.setPosition(servo0And1, speed);
         servoBrick.enable(servo0And1);
 		return "Move forward with speed " + speed/SPEED_FACTOR;
 	}
 	
 	@SuppressWarnings("unused")
 	private String backward(){
 		servoBrick.setPosition(servo0And1, (short)(speed * (-1)));
         servoBrick.enable(servo0And1);
 		return "Move backward with speed " + speed/SPEED_FACTOR;
 	}
 	
 	@SuppressWarnings("unused")
 	private String turnLeft(){
 		servoBrick.setPosition(servo0, (short)(speed * (-1)));
 		servoBrick.setPosition(servo1, speed);
 		return "Turn left with speed " + speed/SPEED_FACTOR;
 	}
 	
 	@SuppressWarnings("unused")
 	private String turnRight(){
 		servoBrick.setPosition(servo1, (short)(speed * (-1)));
 		servoBrick.setPosition(servo0, speed);
 		return "Turn right with speed " + speed/SPEED_FACTOR;
 	}
 	
 	@SuppressWarnings("unused")
 	private String forwardLeft(){
 		servoBrick.setPosition(servo0, (short)(speed/1.6));
 		servoBrick.setPosition(servo1, speed);
 		return "Move forward left with speed " + speed/SPEED_FACTOR;
 	}
 	
 	@SuppressWarnings("unused")
 	private String forwardRight(){
 		servoBrick.setPosition(servo0, speed);
 		servoBrick.setPosition(servo1, (short)(speed/1.6));
 		return "Move forward right with speed " + speed/SPEED_FACTOR;
 	}
 	
 	@SuppressWarnings("unused")
 	private String backwardLeft(){
 		servoBrick.setPosition(servo0, (short)(speed * (-1)/1.6));
 		servoBrick.setPosition(servo1, (short)(speed * (-1)));
 		return "Move backward left with speed " + speed/SPEED_FACTOR;
 	}
 	
 	@SuppressWarnings("unused")
 	private String backwardRight(){
 		servoBrick.setPosition(servo0, (short)(speed * (-1)));
 		servoBrick.setPosition(servo1, (short)(speed * (-1)/1.6));
 		return "Move backward right with speed " + speed/SPEED_FACTOR;
 	}
 }
