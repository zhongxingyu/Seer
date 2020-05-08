 /* CMPUT301F13T06-Adventure Club: A choose-your-own-adventure story platform
  * Copyright (C) 2013 Alexander Cheung, Jessica Surya, Vina Nguyen, Anthony Ou,
  * Nancy Pham-Nguyen
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package story.book;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 /**
  * Activity that serves as the main menu where the user can 
  * navigate to a list of their local stories or to a list of online stories.
  * Every view is able to navigate back to this Dashboard.
  * 
  * @author Nancy Pham-Nguyen
  * @author Vina Nguyen
  */
 
 public class Dashboard extends Activity {
 
 	private EditText enterName;
     private String defaultName;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.dashboard);
 
 		enterName = (EditText) findViewById(R.id.enter_name);
 		defaultName = this.getString(R.string.default_nickname);
 		displayNickname();
 		
 		final Intent localIntent = new Intent(this, LocalStoriesActivity.class);
 		final Intent onlineIntent = new Intent(this, OnlineStoriesActivity.class);
 
 		Button localButton = (Button) findViewById(R.id.local_stories);
 		localButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				//Navigate to the Local Stories
 				startActivity(localIntent);
 			}
 		});
 
 		Button onlineButton = (Button) findViewById(R.id.online_stories);
 		onlineButton.setOnClickListener(new OnClickListener() {
 		    public void onClick(View v) {
 		    	//Navigate to the Online Stories
 				startActivity(onlineIntent);
 			}
 		});
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		
 		setNickname();
 	}
 
     /**
      * Method where the user can set their nickname. If 
      * they do not enter anything, set the name to "Anonymous"
      */
     private void setNickname() {
 		String name = enterName.getText().toString();
		if (name.equals("")) {
 			name = defaultName;
 		}
 		
     	SharedPreferences settings = getPreferences(MODE_PRIVATE);
     	SharedPreferences.Editor editor = settings.edit();
     	editor.putString("Nickname", name);
     	editor.commit();
     	
     	StoryApplication.setNickname(name);
     }
     
     /**
      * Displays the nickname of the user whether it is the one
      * they entered or "Anonymous"
      */
     private void displayNickname() {
     	SharedPreferences settings = getPreferences(MODE_PRIVATE);
     	String name = settings.getString("Nickname", defaultName );
     	
        if (!name.equals(defaultName)) {
     		enterName.setText(name);
     	}
         
         StoryApplication.setNickname(name);
     }
 
 }
