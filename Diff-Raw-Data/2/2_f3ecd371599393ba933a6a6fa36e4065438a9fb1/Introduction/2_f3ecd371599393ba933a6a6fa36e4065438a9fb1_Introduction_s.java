 package fi.mikuz.boarder.gui;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import fi.mikuz.boarder.R;
 
 /**
  * 
  * @author Jan Mikael Lindlf
  */
 public class Introduction extends Activity {
 	public static final String TAG = "Guide";
 	
 	LinearLayout mBody;
 	int mPage = 0;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
     	LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
         View layout = inflater.inflate(R.layout.introduction, (ViewGroup) findViewById(R.id.root));
         
         mBody = (LinearLayout) layout.findViewById(R.id.introduction_body);
         mBody.setOrientation(LinearLayout.VERTICAL);
         mBody.setPadding(20, 20, 20, 20);
         
         Button endButton = (Button) layout.findViewById(R.id.guide_end);
         Button lastButton = (Button) layout.findViewById(R.id.guide_last);
         Button nextButton = (Button) layout.findViewById(R.id.guide_next);
 
         endButton.setOnClickListener(new OnClickListener() {
         	public void onClick(View v) {
         		exit();
         	}
         });
 
         lastButton.setOnClickListener(new OnClickListener() {
         	public void onClick(View v) {
         		if (mPage > 0) {
         			mPage--;
         			changePage();
         		} else {
         			Toast.makeText(Introduction.this, "This is the first page", Toast.LENGTH_SHORT).show();
         		}
         	}
         });
         
         nextButton.setOnClickListener(new OnClickListener() {
         	public void onClick(View v) {
         		mPage++;
         		changePage();
         	}
         });
         
         setContentView(layout);
         
         changePage();
     }
     
     private void changePage() {
     	mBody.removeAllViews();
     	TextView text = new TextView(this);
     	switch(mPage) {
 	        case 0:
 	        	setTitle("Welcome");
 	        	text.setText("Welcome to Boarder!\n\n" +
 	        			"This introduction helps you to get started with Boarder.\n\n" +
 	        			"You can return here any time through 'Help Center'.\n\n" +
 	        			"This is a short introduction. For more help please visit 'Help Center'.");
 	        	mBody.addView(text);
 	        	break;
 	        case 1:
 	        	setTitle("Navigation");
 	        	text.setText("Downloading boards\n\n" +
 	        			"Default screen of Boarder is 'Soundboard Menu'. There you will be able to navigate to different places in Boarder.\n\n" +
 	        			"Opening menu will allow you to add new boards and stuff.\n\n" +
 	        			"You will will also find my email and forums so just ask me if you are lost.");
 	        	mBody.addView(text);
 	        	break;
 	        case 2:
 	        	setTitle("Downloading boards");
 	        	text.setText("Downloading boards\n\n" +
 	        			"In 'Soundboard Menu' open menu and select 'Internet'.\n\n" +
 	        			"Select 'Download from Internets'. You can search and sort the list. Navigate the list by swiping left and right.\n\n" +
 	        			"Enter a board you want to download. It's recommended to start with highly rated board to avoid troubles.\n\n" +
 	        			"Click any button in 'Download' section. Download and execute a board '.zip' file. Select Boarder as app that executes the zip file.");
 	        	mBody.addView(text);
 	        	break;
 	        case 3:
 	        	setTitle("Creating boards");
 	        	text.setText("Creating boards\n\n" +
 	        			"In 'Soundboard Menu' select 'Add' from menu.\n\n" +
 	        			"Add sounds from menu. Edit sound by short-clicking it.\n\n" +
 	        			"Swap to 'Listen board' mode in menu to use the board. Edit the board by swapping to 'Edit board' mode.");
 	        	mBody.addView(text);
 	        	break;
 	        case 4:
 	        	setTitle("Thanks");
 	        	ImageView promo = new ImageView(this);
 	        	promo.setImageResource(R.drawable.promo);
 	        	promo.setAdjustViewBounds(true);
 	        	promo.setScaleType(ScaleType.CENTER_INSIDE);
 	        	mBody.addView(promo);
 	        	text.setText("\n\nThanks for reading Boarder introduction.\n\n" +
	        			"I wish you pleasant boading.");
 	        	text.setGravity(Gravity.CENTER_HORIZONTAL);
 	        	mBody.addView(text);
 	        	break;
 	        default:
 	        	exit();
     	}
     }
     
     private void exit() {
     	try {
 			Introduction.this.finish();
 		} catch (Throwable e) {
 			Log.e(TAG, "Unable to finish", e);
 		}
     }
     
 }
