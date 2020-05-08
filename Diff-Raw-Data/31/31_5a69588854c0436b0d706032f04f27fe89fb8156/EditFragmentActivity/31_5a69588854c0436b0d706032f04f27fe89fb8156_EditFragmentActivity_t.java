 
 package ualberta.g12.adventurecreator;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import java.util.LinkedList;
 
 // Right now we are making Fragments here and also choice
 // What i was thinking was maybe just make the general fragment here
 // and have another screen to add choices then we could have list a listview type thing so can have a lot of choices
 // and long clicking each one would let you edit/delete them 
 // TODO discuss with the team
 //
 public class EditFragmentActivity extends Activity implements FView<Fragment> {
 
     private Button addImage;
     private Button makeOrSave;
     private Fragment fragment;
     private FragmentController fragmentController;
     private int storyId;
     private TextView fragmentTitleTextView;
     private ListView fragmentPartListView;
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_fragment_editor);
 
         // obtain the intent
         Intent editActIntent = getIntent();
         Bundle bundledExtras = editActIntent.getExtras();
         String editType = bundledExtras.getString("EditType");
         
         if (editType.equals("Edit") == false){
         	// TODO: Load our fragment from the story model using the id given to us as an extra ( if we are editing an existing fragment)
         	storyId  = bundledExtras.getInt("Id");
         	// load title , id/page number , fragment description, choices
         	
         }
         // else then we are adding a new fragment dont need to load stuff
 
         
         // TODO: Set the fragmentController to our Fragment
         
         /* for testing, will delete later -Lindsay */
         //FragmentController fragcont = new FragmentController();
         fragment = new Fragment();
         System.out.println("going to addtextseg");
         FragmentController.addTextSegment(fragment, "entry1");
         FragmentController.addTextSegment(fragment,"entry2");
         FragmentController.addTextSegment(fragment,"entry number 3 which is rather long because we woud like to test text wrapping");
         FragmentController.addNewFragmentPart(fragment);
         if (editType.equals("Edit") == true){
             FragmentController.addNewFragmentPart(fragment);
         }
         // TODO: Load our fragment into view
         //get widget references
         fragmentPartListView = (ListView) findViewById(R.id.FragmentPartList);
         fragmentTitleTextView = (TextView) findViewById(R.id.fragmentTitle);
         
         //Loads title
         String title = fragment.getTitle();
         if (fragmentTitleTextView != null){
             if (title != null)
                 fragmentTitleTextView.setText(title);
             else
                 fragmentTitleTextView.setText("Title Here");  //should this go here? -Lindsay
         }
         
         //Loads fragment parts (text, images, videos, sounds, etc
         FragmentPartAdapter adapter = new FragmentPartAdapter(
                 this, R.layout.activity_fragment_editor, fragment);
         fragmentPartListView.setAdapter(adapter);
        
         registerForContextMenu(fragmentPartListView);
         
         // TODO: Set up on clickers
         setUpOnClickListeners();
 
     }
 
     private void setUpOnClickListeners() {
         addImage = (Button) findViewById(R.id.buttonAddIll);
         addImage.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 // From: http://stackoverflow.com/q/16391124/1684866
                 Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
                 galleryIntent.setType("image/*");
                 galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
 
                 Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
 
                 Intent chooser = new Intent(Intent.ACTION_CHOOSER);
                 chooser.putExtra(Intent.EXTRA_INTENT, galleryIntent);
                 chooser.putExtra(Intent.EXTRA_TITLE, "Select Illustration From");
 
                 Intent[] intentArray = {
                         cameraIntent
                 };
                 chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                 startActivityForResult(chooser, 0);
             }
         });
         
         makeOrSave = (Button) findViewById(R.id.ButtonAddOrSave);
         makeOrSave.setOnClickListener(new OnClickListener() {
         	
         	
             @Override
             public void onClick(View v) {
                 //AS OF RIGHT NOW ONLY LOADS ONE CHOICE TITLE AND TITLE PAGE NUMBER AND ALSO FRAGMENT DESCRIPTION
             	
         		EditText titleET = (EditText) findViewById(R.id.fragmentTitle);
 				EditText idPageNumET = (EditText) findViewById(R.id.idPageNum);
 				EditText choice1ET = (EditText) findViewById(R.id.choiceId1);
 				
 				String title = titleET.getText().toString();
 				String idPageNum = idPageNumET.getText().toString();
				int idPage = -9;
				try{
				 idPage = Integer.parseInt(idPageNum);
				}catch(NumberFormatException e){
					Log.d("Msg","There was a number format exception!");
				}
				
 				String choice1 = choice1ET.getText().toString();
 				//create a new fragment object as well as choice
 				Choice aNewChoice = new Choice();
 				aNewChoice.setChoiceText(choice1);
 				
 				//Fragment aNewFrag = new Fragment(title,Description);
 				Fragment aNewFrag = new Fragment();
 				aNewFrag.addChoice(aNewChoice);
				aNewFrag.setTitle(title);
				aNewFrag.setId(idPage);
 				StoryList sl = AdventureCreatorApplication.getStoryList();
 				
 				Story story = sl.getStoryById(storyId);
 				
 				story.addFragment(aNewFrag);
 				
 				// now we have to add it to our story object
 				// TODO WE HAVE TO HAVE STORY OBJECT THAT IS ASSOSCIATED WITH THIS FRAGMENT 
 				// StoryController.addFragment(ourStoryName , aNewFrag);
 				
 				Log.d("fragment", "a new fragment has been created");
 				finish();
             }	
         });
         
         System.out.println("start listener");
         fragmentPartListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
             @Override
             public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                 
                 if (fragment.getDisplayOrder().get(position).equals("n")){
                     /*
                      * open pop up menu with options:
                      * Add text
                      * Add Image
                      * Add Choice
                      * Delete
                      */
                     registerForContextMenu(fragmentPartListView);                    
                     
                 } else {
                     /*
                      * open pop up menu with options:
                      * Insert text
                      * Insert Image
                      * Insert Choice
                      * Delete
                      * 
                      * depending on position
                      */
                 }
                
             }
         });
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.fragment_editor, menu);
         return true;
     }
 
     @Override
     public void update(Fragment model) {
         // TODO reload all fields based on new info from model
 
     }
     
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,
                                     ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.new_fragment_part_menu, menu);
     }
 
 }
