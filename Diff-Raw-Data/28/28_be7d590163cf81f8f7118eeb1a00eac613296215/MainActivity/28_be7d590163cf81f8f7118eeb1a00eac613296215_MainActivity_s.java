 package com.example.skidrow;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.text.Editable;
 import android.text.Layout;
 import android.text.TextWatcher;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.PopupMenu;
 import android.widget.RelativeLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.view.LayoutInflater;
 
 /**
  * Main activity class that controls the UI of the program.
  * 
  * @author clayton
  * @version 1.6
  */
 public class MainActivity extends Activity {
 
 	//Intsance of SkidRow game 
 	private Game game;
 	private Player player;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         this.setContentView(R.layout.home);
         //Creates a new SkidRow Game
         game = new Game();
     }
     
     /**
      * This method changes the layout visible to the user.
      * It is called when the user clicks something on the screen which takes them to a new screen.
      * 
      * @param view view clicked by user
      */
     public void changeLayout(View view){
     	int viewID = view.getId();
     	
     	switch(viewID){
     		case R.id.newGame:
     			this.setContentView(R.layout.new_game);
     			break;
     		case R.id.cntinue:
     			this.setContentView(R.layout.cntinue);
     			break;
     		case R.id.highScore:
     			this.setContentView(R.layout.high_score);
     			break;
     		default:
     			this.setContentView(R.layout.new_game);
     	}
     	readyEnvironment(viewID);
     }
     
     /** 
      * Behaves the same way as changeLayout(View view), however takes in a menuitem
      * 
      * @param item Menuitem clicked by user
      */
     public void changeLayout(MenuItem item){
     	int itemID = item.getItemId();
     	
     	switch(itemID){
     		case R.id.newGame:
     			this.setContentView(R.layout.new_game);
     			break;
     		case R.id.cntinue:
     			this.setContentView(R.layout.cntinue);
     			break;
     		case R.id.highScore:
     			this.setContentView(R.layout.high_score);
     			break;
     		default:
     			this.setContentView(R.layout.new_game);
     	} 	
     	readyEnvironment(itemID);
     }
     
     /**
      * This method displays the menu for switching between the three main start screens(Continue, New Game, High Score).
      * It is called when the user clicks the navigation button.
      * 
      * @param view Navigation button clicked by user
      */
     public void showLayoutMenu(View view){
     	int viewID = view.getId();
     	int menuID;
     	
     	switch(viewID){
 	    	case R.id.newGame:
 				menuID=R.menu.layout_menu_new_game;
 				break;
 			case R.id.cntinue:
 				menuID=R.menu.layout_menu_cntinue;
 				break;
 			case R.id.highScore:
 				menuID=R.menu.layout_menu_high_score;
 				break;
 			default:
 				menuID=R.menu.layout_menu_new_game;
     	}    	
     	readyEnvironment(viewID);
     	
     	PopupMenu popup = new PopupMenu(this, view);
     	popup.inflate(menuID);
     	popup.show(); 
     }
     
     /**
      * Method used to wipe all data from the player create screen.
      * The clearable inputs are as follows: nameInput(TextView), difficultyLevel(Spinner), communicationsSkillsInput(EditText), 
      * fighterSkillsInput(EditText), driverSkillsInput(EditText), and dealerSkillsInput(EditText).
      * 
      * @param view Button clicked by user to clear data. Or can be null if called from within the program
      */
     public void clearData(View view){
     	
     }
     
     /**
      * This is the method called when the user tries to create the player. The input that is needed to create the player is as follows: name, difficulty, 
      * communication skill, fighter skill, driver skill, and dealer skill. 
      * Those inputs correspond to the following android views respectively: nameInput(TextView), difficultyLevel(Spinner), communicationsSkillsInput(EditText), 
      * fighterSkillsInput(EditText), driverSkillsInput(EditText), and dealerSkillsInput(EditText).
      * The restrictions are such that the total combined skill points must be exactly equal to 16. Also the player name must be of legal characters. 
      * 
      * @param view Button clicked by the user
      */
     public void createPlayer(View view){
     	EditText playerName = (EditText) this.findViewById(R.id.nameInput);
     	EditText communicationSkills = (EditText)this.findViewById(R.id.communicationSkillsInput);
     	EditText fighterSkills = (EditText)this.findViewById(R.id.fighterSkillsInput);
     	EditText driverSkills = (EditText)this.findViewById(R.id.driverSkillsInput);
     	EditText dealerSkills = (EditText)this.findViewById(R.id.dealerSkillsInput);
     	Spinner difficulty = (Spinner)this.findViewById(R.id.difficultyLevel);
     	int comSkills = Integer.parseInt(communicationSkills.getText().toString());
     	int fightSkills = Integer.parseInt(fighterSkills.getText().toString());
     	int drivSkills = Integer.parseInt(driverSkills.getText().toString());
     	int dealSkills = Integer.parseInt(dealerSkills.getText().toString());
    	game.createPlayer(playerName.getText().toString(), comSkills, fightSkills, drivSkills, dealSkills);
     }
     
     /**
      * Generates a message to the user to inform them of an error. This may be due to the user or the the program. 
      * 
      * @param errorMessage The message to display
      */
     public void displayError(String errorMessage){
     	Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
     }
     
     /**
      * Stub for future implementation
      * 
      * @param viewId Id of layout being switched to
      */
     public void readyEnvironment(int viewId){
     }
 }
