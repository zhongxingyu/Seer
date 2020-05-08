 package client;
 
 /* Author: CS2212 Group 2
  * File Name: MainMenuScreen.java
  * Date: 25/01/2012
  * Project: UWOSurvivorPool
  * Course: CS2212b
  * Description:
  * */
 
 import data.bonus.Bonus;
 import net.rim.device.api.system.Bitmap;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.FieldChangeListener;
 import net.rim.device.api.ui.Graphics;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.ButtonField;
 import net.rim.device.api.ui.component.LabelField;
 import net.rim.device.api.ui.container.MainScreen;
 import net.rim.device.api.ui.container.VerticalFieldManager;
 
 public class MainMenuScreen extends MainScreen implements FieldChangeListener {
 	/* Variables */
 	private ButtonField btnUserStanding, btnVote, btnBonus; // The continue button.
 	private Bitmap backgroundBitmap; // background image
 
 	public MainMenuScreen() {
 		super(NO_VERTICAL_SCROLL);
 
 		backgroundBitmap = Bitmap.getBitmapResource("MainMenu.png");
 
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
 		/* buttons */
 		btnUserStanding = new ButtonField("View Standings", LabelField.FIELD_HCENTER);
 		btnUserStanding.setChangeListener(this);
 		btnUserStanding.setMargin(230, 0, 0, 0); // formatting the button placements
 		btnVote = new ButtonField("Make Your Vote", LabelField.FIELD_HCENTER);
 		btnVote.setChangeListener(this);
 		btnBonus = new ButtonField("Bonus Questions", LabelField.FIELD_HCENTER);
 		btnBonus.setChangeListener(this);
 
 		/* Build the components to MainScreen */
 		this.setStatus(Common.getToolbar());
 		vertFieldManager.add(btnUserStanding);
 		vertFieldManager.add(btnVote);
		if(Bonus.getAllQuestions()!=null||Bonus.getAllQuestions().size()!=0)
 			vertFieldManager.add(btnBonus);
 		this.add(vertFieldManager);
 	}
 	
 	public boolean onSavePrompt(){
 		return true;
 	}
 
 	public void fieldChanged(Field arg0, int arg1) {
 		if (arg0 == btnUserStanding) { // View Standings
 			UiApplication.getUiApplication().pushScreen(
 					new StandingScreen());
 		} else if (arg0 == btnVote) { // Voting Area
 			UiApplication.getUiApplication().pushScreen(
 					new PickMenuScreen());
 		} else if (arg0 == btnBonus) { // Bonus Questions
 			UiApplication.getUiApplication().pushScreen(
 					new BonusScreen());
 		}
 
 	}
 }
