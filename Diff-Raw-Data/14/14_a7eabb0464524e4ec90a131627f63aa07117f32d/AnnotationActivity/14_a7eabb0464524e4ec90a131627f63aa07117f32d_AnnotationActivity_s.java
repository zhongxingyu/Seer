 /*
  * Copyright (C) <2013>  <Lin Tong, Terence Yin Kiu Leung>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package c301.AdventureBook;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Base64;
 import android.view.Gravity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 import c301.AdventureBook.Controllers.StoryManager;
 import c301.AdventureBook.ElasticSearch.ESClient;
 import c301.AdventureBook.Models.Annotation;
 import c301.AdventureBook.Models.Page;
 import c301.AdventureBook.Models.Story;
 
 import com.example.adventurebook.R;
 
 /**
  * This is the annotation activity. It is run when a user wants to annotate a
  * story fragment.
  * 
  * @author Lin Tong
  * @author Terence Yin Kiu Leung
  * 
  */
 public class AnnotationActivity extends Activity {
 	private EditText author;
 	private EditText comment;
 	private Annotation someAnnotation;
 	private static final int PHOTO_ACTIVITY_REQUEST = 1001;
	String authorAnnotation;
	String commentAnnotation;
	StoryManager sManager;
	String imageByte;
	ArrayList<Annotation> currentAnnotations;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.annotations);
 
 		sManager = StoryManager.getInstance();
 		sManager.initContext(this);
 
 		author = (EditText) findViewById(R.id.editTextAnnotationAuthor);
 		comment = (EditText) findViewById(R.id.editTextAnnotationComment);
 		ImageButton attachImage = (ImageButton) findViewById(R.id.imageButtonAnnotationAttachImage);
 
 		attachImage.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent intent = new Intent(getApplicationContext(),
 						TakePhotoActivity.class);
 				startActivityForResult(intent, PHOTO_ACTIVITY_REQUEST);
 			}
 		});
 
 		Button returnPage = (Button) findViewById(R.id.annotationButtonReturnToPage);
 		returnPage.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				goBackPage();
 			}
 		});
 
 		Button submit = (Button) findViewById(R.id.submit);
 
 		submit.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				createAnnotation();
 			}
 		});
 		populateAnnotations();
 	}
 
 	/**
 	 * Display all the page annotations on the page dynamically.
 	 */
 	public void populateAnnotations() {
 		Page currentPage = sManager.getPage();
 		currentAnnotations = currentPage.getAnnotations();
 
 		LinearLayout hLinearLayout = (LinearLayout) findViewById(R.id.horizontalLinearLayout);
 
 		if (!currentAnnotations.isEmpty()) {
 			// Loop over all the annotations, dynamically adding to the
 			// horizontal scroll view
 			for (int i = 0; i < currentAnnotations.size(); i++) {
 
 				// Create the vertical scrollview for individual annotations
 				ScrollView scrollView = initScrollView();
 
 				// Create the vertical linear layout for individual annotations
 				LinearLayout lLayout = initLinearLayout();
 
 				// Create the views to go in the linear layout
 				TextView authorTV = new TextView(this);
 				ImageView imageView = new ImageView(this);
 				TextView commentTV = new TextView(this);
 
 				// Set the author into the textview
 				authorTV.setText("Author: "
 						+ currentAnnotations.get(i).getAuthor());
 
 				// Set the image into the imageview
 				imageByte = currentAnnotations.get(i).getIllustration();
 				// check if annotation has a picture first
 				if (imageByte != null) {
 					byte[] decodedString = Base64.decode(imageByte,
 							Base64.DEFAULT);
 					Bitmap decodedByte = BitmapFactory.decodeByteArray(
 							decodedString, 0, decodedString.length);
 					imageView.setImageBitmap(decodedByte);
 				}
 
 				// Set the comment into the textview
 				commentTV.setText("Comment: " + currentAnnotations.get(i).getComment());
 
 				lLayout.addView(authorTV);
 				lLayout.addView(imageView);
 				lLayout.addView(commentTV);
 
 				scrollView.addView(lLayout);
 				hLinearLayout.addView(scrollView);
 			}
 		}
 	}
 
 	/**
 	 * Creates a new vertical scrollview for dynamic adding to the annotations.
 	 * 
 	 * @return scrollView the new scrollview
 	 */
 	private ScrollView initScrollView() {
 		ScrollView scrollView = new ScrollView(this);
 		scrollView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
 				LayoutParams.WRAP_CONTENT));
 		scrollView.setPadding(10, 10, 10, 10);
 		return scrollView;
 	}
 
 	/**
 	 * Creates a new linear layout for dynamic adding to the annotations. This
 	 * linear layout gets added to the vertical scrollview.
 	 * 
 	 * @return lLayout the new linear layout
 	 */
 	private LinearLayout initLinearLayout() {
 		LinearLayout lLayout = new LinearLayout(this);
 		lLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
 				LayoutParams.WRAP_CONTENT));
 		lLayout.setOrientation(LinearLayout.VERTICAL);
 		lLayout.setGravity(Gravity.CENTER_HORIZONTAL);
 		return lLayout;
 	}
 
 	/**
 	 * Create a new annotation and add it to the list of annotations for the
 	 * page.
 	 */
 	private void createAnnotation() {
 
 		boolean isOnline = ((AdventureBook) this.getApplication())
 				.getIsOnlineParameter();
 
 		getUserInfo();
 
 		if (authorAnnotation.length() == 0) {
 			Toast.makeText(this, "Author cannot be blank!", Toast.LENGTH_SHORT)
 					.show();
 		} else {
 			someAnnotation = new Annotation(authorAnnotation, commentAnnotation);
 			someAnnotation.setIllustration(imageByte);
 			
 			Page currentPage = sManager.getPage();
 			currentPage.addAnnotation(someAnnotation);
 
 			if (!isOnline) {
 				// If we are viewing this story locally then do this:
 				// Save the Updated Story Locally
 
 				sManager.saveStory(sManager.getCurrentStory(), true);
 				// Refresh the current activity to repopulate views
 
 			} else {
 				// If we are viewing this story from online then do this:
 				new publishStoryTask(sManager.getCurrentStory()).execute();
 
 			}
 			
 			Toast.makeText(getBaseContext(), "Annotation Added!",
 					Toast.LENGTH_SHORT).show();
 						
 			finish();
 			startActivity(getIntent());
 		}
 
 	}
 
 	/**
 	 * This class defines the publishStoryTask
 	 * 
 	 * @author Minhal
 	 * 
 	 */
 	private class publishStoryTask extends AsyncTask<String, String, String> {
 
 		Story story;
 
 		public publishStoryTask(Story storyClicked) {
 			this.story = storyClicked;
 		}
 
 		@Override
 		protected String doInBackground(String... arg0) {
 			ESClient client = new ESClient();
 			try {
 				client.insertStory(this.story);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return null;
 		}
 	}
 
 	/**
 	 * this should go back to the view page
 	 */
 	private void goBackPage() {
 		finish();
 	}
 
 	/**
 	 * get the user's info for annotation it will get the author it will get the
 	 * comment
 	 */
 	private void getUserInfo() {
 		authorAnnotation = author.getText().toString();
 		commentAnnotation = comment.getText().toString();
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (requestCode == PHOTO_ACTIVITY_REQUEST && resultCode == RESULT_OK) {
 			imageByte = data.getStringExtra("imagebyte");
 		}
 	}
 
 }
