 package com.android.helpme.demo.manager;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 import org.w3c.dom.Element;
 
 import android.content.res.Resources.Theme;
 import android.graphics.Path.Direction;
 import android.location.Location;
 import android.util.Log;
 
 import com.android.helpme.demo.exceptions.UnkownMessageType;
 import com.android.helpme.demo.exceptions.WrongObjectType;
 import com.android.helpme.demo.gui.DrawManager;
 import com.android.helpme.demo.gui.SeekerActivity;
 import com.android.helpme.demo.gui.DrawManager.DRAWMANAGER_TYPE;
 import com.android.helpme.demo.manager.interfaces.HistoryManagerInterface;
 import com.android.helpme.demo.manager.interfaces.MessageHandlerInterface;
 import com.android.helpme.demo.manager.interfaces.MessageOrchestratorInterface;
 import com.android.helpme.demo.manager.interfaces.PositionManagerInterface;
 import com.android.helpme.demo.manager.interfaces.RabbitMQManagerInterface;
 import com.android.helpme.demo.manager.interfaces.RabbitMQManagerInterface.ExchangeType;
 import com.android.helpme.demo.manager.interfaces.UserManagerInterface;
 import com.android.helpme.demo.messagesystem.AbstractMessageSystem;
 import com.android.helpme.demo.messagesystem.InAppMessage;
 import com.android.helpme.demo.utils.ThreadPool;
 import com.android.helpme.demo.utils.User;
 import com.android.helpme.demo.utils.position.Position;
 import com.android.helpme.demo.utils.position.PositionInterface;
 
 /**
  * 
  * @author Andreas Wieland
  * 
  */
 public abstract class MessageHandler extends AbstractMessageSystem implements MessageHandlerInterface{
 
 	abstract protected boolean reloadDatabase();
 
 	protected static RabbitMQManagerInterface rabbitMQManagerInterface = RabbitMQManager.getInstance();
 	protected static UserManagerInterface userManagerInterface = UserManager.getInstance();
 	protected static PositionManagerInterface positionManagerInterface = PositionManager.getInstance();
 	protected static HistoryManagerInterface historyManagerInterface = HistoryManager.getInstance();
 
 	/**
 	 * Handels the Messages form the {@link PositionManager}
 	 * @param message
 	 */
 	protected void handlePositionMessage(InAppMessage message) {
 		switch (message.getType()) {
 		case LOCATION:
 			if (!(message.getObject() instanceof Position)) {
 				fireError(new WrongObjectType(message.getObject(), Position.class));
 				return;
 			}
 			Position position = (Position) message.getObject();
 			userManagerInterface.thisUser().updatePosition(position);
 
 			if (getDrawManager(DRAWMANAGER_TYPE.MAP) != null) {
 				getDrawManager(DRAWMANAGER_TYPE.MAP).drawThis(userManagerInterface.getThisUser());
 			}
 			historyManagerInterface.getTask().sendPosition(position);
 			break;
 
 		default:
 			fireError(new UnkownMessageType());
 			break;
 		}
 	}
 
 	/**
 	 * Handels the Messages form the {@link RabbitMQManager} 
 	 * @param message
 	 */
 	protected void handleRabbitMQMessages(InAppMessage message) {
 		switch (message.getType()) {
 		case UNBOUND_FROM_SERVICE:
 			//TODO
 			break;
 		case BOUND_TO_SERVICE:
 			run(rabbitMQManagerInterface.connect());
 			break;
 
 		case CONNECTED:
 			run(rabbitMQManagerInterface.subscribeToMainChannel());
 			break;
 
 		case RECEIVED_DATA:
 			if (!(message.getObject() instanceof User)) {
 				fireError(new WrongObjectType(message.getObject(), User.class));
 				return;
 			}
 			User user = (User) message.getObject();
 
 			if (!UserManager.getInstance().isUserSet()) {
 				return;
 			}
 
 			if (!user.getId().equalsIgnoreCase(userManagerInterface.getThisUser().getId())) {
 
 				if (userManagerInterface.getThisUser().getHelfer()) {
 					handleIncomingUserAsHelper(user);
 				}else {
 					handleIncomingUserAsHelperSeeker(user);
 				}
 			}
 			break;
 
 		default:
 			fireError(new UnkownMessageType());
 			break;
 		}
 	}
 
 	/**
 	 * if this user is a Helper this Method will be called and starts the List {@link DrawManager}
 	 * @param incomingUser
 	 */
 	private void handleIncomingUserAsHelper(User incomingUser){
 		if (userManagerInterface.addUser(incomingUser)) {
 			run(rabbitMQManagerInterface.showNotification(incomingUser));
 		}
 
		historyManagerInterface.getTask().updatePosition(incomingUser);
 		if (getDrawManager(DRAWMANAGER_TYPE.MAP) != null) {
 			
 			if (historyManagerInterface.getTask().isUserInShortDistance()) {
 				getDrawManager(DRAWMANAGER_TYPE.MAP).drawThis(historyManagerInterface.getTask());
 				
 			}else{
 				getDrawManager(DRAWMANAGER_TYPE.MAP).drawThis(incomingUser);
 			}
 		} else {
 			getDrawManager(DRAWMANAGER_TYPE.LIST).drawThis(incomingUser);
 		}
 	}
 
 	/**
 	 * if this user is a Help Seeker this Method will be called and starts the Map {@link DrawManager}
 	 * @param incomingUser
 	 */
 	private void handleIncomingUserAsHelperSeeker(User incomingUser){
 		userManagerInterface.addUser(incomingUser);
 		historyManagerInterface.getTask().updatePosition(incomingUser);
 
 		if (getDrawManager(DRAWMANAGER_TYPE.HELPERCOMMING) != null) {
 			getDrawManager(DRAWMANAGER_TYPE.HELPERCOMMING).drawThis(incomingUser);
 		} else {
 
 			getDrawManager(DRAWMANAGER_TYPE.SEEKER).drawThis(incomingUser);
 		}
 	}
 	
 	protected void handleHistoryMessages(InAppMessage message) {
 		switch (message.getType()) {
 		case TIMEOUT:
 			historyManagerInterface.stopTask();
 			getDrawManager(DRAWMANAGER_TYPE.SEEKER).drawThis(message.getObject());
 			break;
 
 		default:
 			fireError(new UnkownMessageType());
 			break;
 		}
 	}
 
 	/**
 	 * Handles Messages from the {@link UserManager}
 	 * @param message
 	 */
 	protected void handleUserMessages(InAppMessage message) {
 		switch (message.getType()) {
 		case LOADED:
 			getDrawManager(DRAWMANAGER_TYPE.SWITCHER).drawThis(message.getObject());
 
 			break;
 		case RECEIVED_DATA:
 			if (!(message.getObject() instanceof ArrayList<?>)) {
 				fireError(new WrongObjectType(message.getObject(), ArrayList.class));
 				return;
 			}
 
 			if (getDrawManager(DRAWMANAGER_TYPE.LOGIN) != null) {
 				getDrawManager(DRAWMANAGER_TYPE.LOGIN).drawThis(message.getObject());
 			}
 			break;
 
 		default:
 			fireError(new UnkownMessageType());
 			break;
 		}
 	}
 }
