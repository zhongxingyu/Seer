 /*
  * Copyright (C) <2013>  <Lin Tong>
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
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.util.Base64;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 import c301.AdventureBook.Controllers.StoryManager;
 import c301.AdventureBook.Models.Annotation;
 import c301.AdventureBook.Models.Page;
 
 import com.example.adventurebook.R;
 
 /**
  * This is the annotation activity. It is run when a user wants to annotate a
  * story fragment.
  * 
  * @author Lin Tong
  * 
  */
 public class AnnotationActivity extends Activity {
 	private EditText author;
 	private EditText comment;
 	private Annotation someAnnotation;
 	private static final int PHOTO_ACTIVITY_REQUEST = 1001;
 	String authorAnnotation;
 	String commentAnnotation;
 	ImageView image;
 	String show_path;
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
 
 		image = (ImageView) findViewById(R.id.imageView1);
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
 				// TODO Auto-generated method stub
 				goBackPage();
 			}
 		});
 
 		Button submit = (Button) findViewById(R.id.submit);
 
 		submit.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				createAnnotation();
 
 				Toast.makeText(getBaseContext(), "Annotation Added!",
 						Toast.LENGTH_SHORT).show();
 			}
 		});
 
 		populateAnnotations();
 
 	}
 
 	public void populateAnnotations() {
 		Page currentPage = sManager.getPage();
 		currentAnnotations = currentPage.getAnnotations();
 
 		TextView author = (TextView) findViewById(R.id.annotationAuthor);
 		TextView comment = (TextView) findViewById(R.id.annotationComment);
 
 		if (!currentAnnotations.isEmpty()) {
 			author.setText(currentAnnotations.get(0).getAuthor());
 			comment.setText(currentAnnotations.get(0).getComment());
 			
			imageByte = currentAnnotations.get(0).getIllustration();
 			
			// check if annotation has a picture first
			if(imageByte != null){
				byte[] decodedString = Base64.decode(imageByte, Base64.DEFAULT);
				Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString,
						0, decodedString.length);
				image.setImageBitmap(decodedByte);
			}
 		}
 	}
 
 	/**
 	 * this will create annotation save as a bundle and return it back to view
 	 * page
 	 */
 	private void createAnnotation() {
 		getUserInfo();
 		someAnnotation = new Annotation(authorAnnotation, commentAnnotation);
 		someAnnotation.setIllustration(imageByte);
 		
 		Page currentPage = sManager.getPage();
 		currentPage.addAnnotation(someAnnotation);
 		sManager.saveStory(sManager.getStory(), true);
 		populateAnnotations(); // Re-Populate Annotaions list.
 	}
 
 	/**
 	 * this should go back to the view page
 	 */
 	private void goBackPage() {
 		Intent intent = new Intent(this, ViewPageActivity.class);
 		startActivity(intent);
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
 		// TODO Auto-generated method stub
 		super.onActivityResult(requestCode, resultCode, data);
 		if (requestCode == PHOTO_ACTIVITY_REQUEST && resultCode == RESULT_OK) {
 			imageByte = data.getStringExtra("imagebyte");
 
 
 
 		}
 	}
 
 }
