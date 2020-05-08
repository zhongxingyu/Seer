 package client;
 
 import net.rim.device.api.command.Command;
 import net.rim.device.api.command.CommandHandler;
 import net.rim.device.api.command.ReadOnlyCommandMetadata;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.toolbar.ToolbarButtonField;
 import net.rim.device.api.ui.toolbar.ToolbarManager;
 import net.rim.device.api.util.StringProvider;
 import data.GameData;
 import data.User;
 
 public class Common {
 
 	private static ToolbarManager toolbar;
 	
 	/**
 	 * creates the toolbar for common actions
 	 */
 	private static void createToolbar(){
 		toolbar = new ToolbarManager();
 		try {
 			// refresh button
 			ToolbarButtonField btnRefresh = new ToolbarButtonField(null,
 					new StringProvider("Refresh"));
 			
 			btnRefresh.setCommandContext(new Object() {
 				public String toString() {
 					return "toolbutton1";
 				}
 			});
 			
 			// if pressed, go back to the splash screen
 			btnRefresh.setCommand(new Command(new CommandHandler() {
 				public void execute(ReadOnlyCommandMetadata metadata,
 						Object context) {
 					UiApplication.getUiApplication().pushScreen(
 							new SplashScreen());
 				}
 			}));
 			
 			
 			// Exit button 
 			ToolbarButtonField btnExit = new ToolbarButtonField(null,
 					new StringProvider("Exit"));
 			btnExit.setCommandContext(new Object() {
 				public String toString() {
 					return "toolbutton2";
 				}
 			});
 			
 			// if pressed, exit the system 
 			btnExit.setCommand(new Command(new CommandHandler() {
 				public void execute(ReadOnlyCommandMetadata metadata,
 						Object context) {
 					exitApp();
 				}
 			}));
 
 			/* add buttons to the tool bar */
 			toolbar.add(btnRefresh);
 			toolbar.add(btnExit);
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 	
 	/**
 	 * The toolbar with refresh and exit
 	 * @return
 	 */
 	public static ToolbarManager getToolbar(){
 		createToolbar();
 		return toolbar;
 	}
 	
 	/**
 	 * Close the app with the option to saveData
 	 * @param saveData
 	 */
 	public static void exitApp(){
 		int response = Dialog.ask(Dialog.D_SAVE,"Do you want to save?");
 		if(response == Dialog.SAVE){
 			//TODO: is first week, week 0?
			System.out.println("****cur week"+GameData.getCurrentGame().getCurrentWeek());
			System.out.println("****name"+GameData.getCurrentGame().getCurrentUser().getFirstName());
 			if(GameData.getCurrentGame().getCurrentWeek()==1){
 				User u = GameData.getCurrentGame().getCurrentUser();
				
 				if(u.getUltimatePick()==null){
 					Dialog.alert("Cannot save unless you choose an ultimate pick.");
 					return;
 				}
			}else{
				GameData.getCurrentGame().writeData();
 			}
 		}else if(response == Dialog.CANCEL)
 			return;
 		System.exit(0);
 	}
 
 	/**
 	 * Display error to user.
 	 * @param msg
 	 */
 	public static void displayErrorMsg(String msg){
 		Dialog.alert(msg);
 	}
 }
