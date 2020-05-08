 package client;
 
 /* Author: CS2212 Group 2
  * File Name: SplashScreen.java
  * Date: 25/01/2012
  * Project: UWOSurvivorPool
  * Course: CS2212b
  * Description: 
  * */
 
 import net.rim.device.api.system.Bitmap;
 import net.rim.device.api.ui.Color;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.Font;
 import net.rim.device.api.ui.FontFamily;
 import net.rim.device.api.ui.Graphics;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.ButtonField;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.component.EditField;
 import net.rim.device.api.ui.component.LabelField;
 import net.rim.device.api.ui.container.MainScreen;
 import net.rim.device.api.ui.container.VerticalFieldManager;
 import client.data.ErrorText;
 import client.data.GameData;
 import data.User;
 
 public class SplashScreen extends MainScreen implements FieldChangeListener {
 	/* Variables */
 	private ButtonField btnLogin; // The continue button.
 	private FontFamily ff1; // fonts.
 	private Font font1; // fonts.
 	private EditField edit; // editable text field
 	private Bitmap backgroundBitmap; // background image
 
 	public SplashScreen() {
 		super(NO_VERTICAL_SCROLL);
 
 		backgroundBitmap = Bitmap.getBitmapResource("Splash.png"); // background
 
 		VerticalFieldManager vertFieldManager = new VerticalFieldManager(
 				VerticalFieldManager.USE_ALL_WIDTH
 						| VerticalFieldManager.USE_ALL_HEIGHT) {
 			// Override the paint method to draw the background image.
 			public void paint(Graphics graphics) {
 				graphics.drawBitmap(0, 0, 640, 430, backgroundBitmap, 0, 0);
 				super.paint(graphics);
 			}
 		};
 
 		setToolbar(Common.getToolbar());
 
 
 		btnLogin = new ButtonField("Log In", LabelField.FIELD_HCENTER);
 		btnLogin.setChangeListener(this); // activate listener
 
 		/* build editable text field */
 		
 		edit = new EditField("\nUserID:  ", "", 10, EditField.NO_NEWLINE) {
 			public void paint(Graphics graphics) { // keep on same line
 				graphics.setColor(Color.WHITE); // white text
 				super.paint(graphics);
 			}
 		};
 		edit.setMargin(233, 120, 35, 70); // align components
 		edit.setNonSpellCheckable(true); // no spell check
 
 		try { // set up the font
 			ff1 = FontFamily.forName("Verdana");
 			font1 = ff1.getFont(Font.BOLD, 30);
 		} catch (final ClassNotFoundException cnfe) {
 			UiApplication.getUiApplication().invokeLater(new Runnable() {
 				public void run() {
 					Dialog.alert("FontFamily.forName() threw "
 							+ cnfe.toString());
 				}
 			});
 		}
 		edit.setFont(font1);
 
 		/* Build the components to MainScreen */
		this.setStatus(Common.getToolbar());
 		vertFieldManager.add(edit);
 		vertFieldManager.add(btnLogin);
 		this.add(vertFieldManager);
 
 	}
 
 	public void fieldChanged(Field arg0, int arg1) {
 		if (arg0 == btnLogin) { // if the log in button is clicked
 			if (checkLogIn(edit.getText().toLowerCase())) {
 				UiApplication.getUiApplication().pushScreen(
 						new MainMenuScreen());
 			} else
 				edit.setText(""); // bad username, clear the field
 		}
 
 	}
 
 	public boolean checkLogIn(String userID) {
 		try {
 			//TODO: find the proper place to check. Not during click.
 			if(GameData.getCurrentGame()==null){ 
 				if(!GameData.initGameData()){
 					ErrorText.displayErrorMsg("Exiting.");
 					System.exit(0);	
 				}
 			}
 			
 			User u =GameData.getCurrentGame().getUser(userID); 
 			if(u!=null){
 				GameData.getCurrentGame().setCurrentUser(u);
 				return true;
 			}else{
 				ErrorText.displayErrorMsg("Invalid user id.");
 			}
 			
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		
 		return false; // match is not found
 	}
 
 }
