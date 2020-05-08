 package c301.AdventureBook;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.util.Base64;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
import android.widget.Toast;
 import c301.AdventureBook.Controllers.LibraryManager;
 import c301.AdventureBook.Controllers.StoryManager;
 import c301.AdventureBook.Models.Page;
 import c301.AdventureBook.Models.Story;
 
 import com.example.adventurebook.R;
 
 public class ViewStoryActivity extends Activity {
 	private static final int FIRST_PAGE_INDEX = 0;
 	LibraryManager lManager; // Controller for the Library
 	StoryManager sManager; // Controller for a story
 	Story currentStory;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.view_story);
 		
 		populateData();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.view_story, menu);
 		return true;
 	}
 	
 	public void populateData(){
 
 		sManager = StoryManager.getInstance();
 		sManager.initContext(this);
 		
 		currentStory = sManager.getStory();
 		
 		ImageView imageView = (ImageView)findViewById(R.id.storyThumnail);
 
 		if (currentStory.getImageByte() == null) {
 			imageView.setImageResource(R.drawable.default_image);
 		} else {
 			byte[] decodedString = Base64.decode(currentStory.getImageByte(), Base64.DEFAULT);
 			Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length); 
 			imageView.setImageBitmap(decodedByte);
 		}
 		TextView titleText = (TextView) findViewById(R.id.storyTitle);
 		titleText.setText(currentStory.getTitle());
 
 		TextView authorText = (TextView)findViewById(R.id.authorTV);
 		authorText.setText(currentStory.getAuthor());
 
 		//TextView dateText = (TextView)findViewById(R.id.dateCreatedTV);
 		//dateText.setText(currentStory.getDate());
 		
 		TextView storyDescription = (TextView) findViewById(R.id.storyDescription);
 		storyDescription.setText(currentStory.getDescription());
 		storyDescription.setKeyListener(null);
 
 	}
 	
 	/**
 	 * Start the view page activity when the "Begin Story" button is clicked.
 	 * The first page is displayed when the user begins a new story.
 	 * This method is called via android:onClick in the view_story XML.
 	 * 
 	 */
 	public void launchViewPageActivity(View v){
 		List<Page> pages = currentStory.getPages();
		// Do nothing if story doesn't contain any pages
		if (pages.size() > 0) {
			Page firstPage = pages.get(FIRST_PAGE_INDEX);
			sManager.setCurrentPage(firstPage);
			Intent i = new Intent(this, ViewPageActivity.class);
			startActivity(i);
		} else {
			Toast.makeText(this, "There are no pages to view!", Toast.LENGTH_LONG).show();
		}
 	}
 
 }
