package c301.AdventureBook;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import c301.AdventureBook.Models.Annotations;
 
 import com.example.adventurebook.R;
 
 public class AnnotationActivity extends Activity {
 	private EditText author;
 	private EditText comment;
 	private Annotations someAnnotations;
 	String authorAnnotation;
 	String commentAnnotation;
 	Intent i;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.annotations);
 		
 		author = (EditText) findViewById(R.id.editTextAnnotationAuthor);
 		
 		comment = (EditText)findViewById(R.id.editTextAnnotationComment);
 		
 		ImageButton attachImage = (ImageButton)findViewById(R.id.imageButtonAnnotationAttachImage);
 		
 		attachImage.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				goTakePhoto();
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
 			}
 		});}
 		private void createAnnotation(){
 			getUserInfo();
 			someAnnotations = new Annotations(authorAnnotation, commentAnnotation, 0); 
 			
 			goBackPage();
 			Bundle bundle = new Bundle();
 			bundle.putSerializable("someAnnotation", someAnnotations);
 			i.putExtras(bundle);
 			startActivityForResult(i,0);
 			
 		}
 		private void goTakePhoto(){
 			Intent b = new Intent(this, TakePhotoActivity.class);
 		}
 		private void goBackPage(){
 			i = new Intent(this, StoryFragment.class);
 		}
 		private void getUserInfo(){
 			authorAnnotation = author.getText().toString();
 			commentAnnotation= comment.getText().toString();
 			
 		}
 	}
