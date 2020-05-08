 package ualberta.g12.adventurecreator;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
 
 public class CreateStoryActivity extends Activity {
 	
 	Button createButton;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_create_story);
         
         createButton = (Button) findViewById(R.id.button1);
         createButton.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				
 				// create a new story object! id should be unique too! 
 				
 				
 				// get the editTexts!
 				EditText title = (EditText) findViewById(R.id.editStoryTitle);
 				EditText author = (EditText) findViewById(R.id.editStoryAuthor);
 				
 				// create a new story (might want to use a controller here instead!)
 				Story myNewStory = new Story(title.getText().toString(), author.getText().toString());
 				// TODO: make sure id is unique!
				myNewStory.setId(1);
 				
 				// add the story with our story list controller!
 				StoryListController slc = AdventureCreatorApplication.getStoryListController(); 
 				slc.addStory(myNewStory);
 				// TODO discuss
 				// I'm changing it so that you can only create a story here
 				// it will return to our main screen where if they click on it it will let them edit
 				finish();
 				
 				
             	
 
 			}
 		});
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		return super.onOptionsItemSelected(item);
 	}
     
     
 }
