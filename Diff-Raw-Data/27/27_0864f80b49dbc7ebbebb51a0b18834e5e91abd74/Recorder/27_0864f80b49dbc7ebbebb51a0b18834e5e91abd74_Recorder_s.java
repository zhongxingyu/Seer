 package org.imaginea.botbot;
 
 import java.util.ArrayList;
 
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Spinner;
 import android.widget.TextView;
 /**
  * Recorder is the main class. It provides the functions to record user-actions.
  * These user-actions then gets publised to the server.
  * 
  * @author Varun Menon
  *
  */
 public class Recorder {
 	static ArrayList<Command> commandList = new ArrayList<Command>();
 	static Command textCommand=null;
 	private static final String LOG_TAG = "debugger";
 	private static CommandTransmitter ct = new CommandTransmitter();
 	
 	/**
 	 *  A static record function that is mainly called for recording.
 	 *  Initializes the {@linkplain org.imaginea.botbot.Command}  object
 	 * @param userAction - the user-action that needs to be transmitted.
 	 * @param view - the class view of the element.
 	 * @param args - any extra arguments like text, position, etc.
 	 */
 	public static void record(String userAction, Object view, Object... args) {
 		if (view instanceof Spinner){
 			Object arg = args[0];
 			if(arg instanceof TextView){
 				String text = ((TextView) arg).getText().toString();
 				args[0]=text;
 			}
 		}else if (view.getClass().isAssignableFrom(AdapterView.class)){
 			Object arg = args[0];
 			if(arg instanceof TextView){
 				String text = ((TextView) arg).getText().toString();
 				args[0]=text;
 			}
 		}
 		Command command = new Command();
 		try {
 			command.add(userAction, ((View) view), args);
 
 		} catch (ClassCastException e) {
 				command.add(userAction, view, args);
 		}
 
 		publishCommand(command);
 		commandList.add(command);
 		Log.i(LOG_TAG, "command received: " + command.toString());
 	}
 	
 	/**
 	 * Publishes the new command. In case the user-action is "entertext", the command is stored temporarily.
 	 * When a new command is received the temporarily stored command is published along with the new command.
 	 * This is done because the text listener keeps on sending text as and when user enters it.
 	 * This will result in unnecessary command to be sent to the server.
 	 * 
 	 * @param command - The Command class object that needs to be published.
 	 */
 	private static void publishCommand(Command command){
 		if (textCommand!=null && (command.userAction).contentEquals("entertext") && (command.view.equals(textCommand.view))) {
 			textCommand=command;
 		}else if((command.userAction).contentEquals("entertext")){
 			if(textCommand!=null)
 				ct.publish(textCommand);
 			textCommand=command;
 		}else if(textCommand!=null){
 			ct.publish(textCommand);
 			textCommand=null;
 			ct.publish(command);
 		}else{
 			ct.publish(command);
 		}
 		
 	}
 	/**
 	 * Used to record only the user-action without the class information. For ex. clickback.
 	 * @param userAction
 	 */
 	public static void record(String userAction) {
 		Command command = new Command();
 		command.add(userAction);
 		publishCommand(command);
 		commandList.add(command);
 	}
 	/**
 	 * Used to record user-action along with the element view.
 	 * @param userAction
 	 * @param view
 	 */
 	public static void record(String userAction, View view) {
 		Command command = new Command();
 		command.add(userAction, view, "");
 		publishCommand(command);
 		commandList.add(command);
 		Log.i(LOG_TAG, "command received: " + command.toString());
 
 	}
 	/**
 	 * Used to record user-action for Menu action.
 	 * @param userAction
 	 * @param menu
 	 */
 	public static void record(String userAction, Menu menu) {
 		Command command = new Command();
 		command.add(userAction);
 		publishCommand(command);
 		ct.publish(command);
 		commandList.add(command);
 	}
 }
