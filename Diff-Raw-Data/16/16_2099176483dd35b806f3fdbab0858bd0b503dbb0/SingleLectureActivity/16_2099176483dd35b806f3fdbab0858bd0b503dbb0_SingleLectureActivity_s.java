 package com.gdg.andconlab.ui;
 
 import java.util.ArrayList;
 
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.support.v4.app.NavUtils;
 import android.text.Html;
 import android.text.Spannable;
 import android.text.Spanned;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.MenuItem;
 import com.gdg.andconlab.DBUtils;
 import com.gdg.andconlab.DatabaseHelper;
 import com.gdg.andconlab.R;
 import com.gdg.andconlab.models.Lecture;
 import com.gdg.andconlab.models.Speaker;
 import com.pagesuite.flowtext.FlowTextView;
 
 /**
  * Activity that displays a single lecture with all its details.
  * This activity expects to get the lecture ID in the calling intent
  * @author Ran Nachmany
  *
  */
 public class SingleLectureActivity extends SherlockActivity{
 
 	public static final String EXTRA_LECTURE_ID = "lecture_id";
 	
 	private static final int ILLEGAL_ID = -1;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 		getSupportActionBar().setHomeButtonEnabled(true);
 		
 		setContentView(R.layout.single_lecture);
 		
 		long id = getIntent().getLongExtra(EXTRA_LECTURE_ID, ILLEGAL_ID);
 		if (ILLEGAL_ID == id) {
 			throw new IllegalStateException("You should set lecture ID in the calling intent");
 		}
 		else {
 			//fetch the lecture from db
 			SQLiteDatabase db = new DatabaseHelper(getApplicationContext(), DatabaseHelper.DB_NAME, null, DatabaseHelper.DB_VERSION).getReadableDatabase();
 			Lecture lecture = DBUtils.getLectureById(db, id);
 			
 			//set lecture name and description
 			((TextView)findViewById(R.id.lecture_name)).setText(lecture.getName());
 			((TextView)findViewById(R.id.lecture_description)).setText(lecture.getDescription());
 			
 			//get all speakers
 			ArrayList<Speaker> speakers = DBUtils.getSpeakersByLectureId(db, id);
 			
 			//loop and add speakers.
 			LinearLayout ll = (LinearLayout) findViewById(R.id.lecture_container);
 			FlowTextView ftv;
 			ImageView img;
 			for (Speaker speaker : speakers) {
 				ftv = new FlowTextView(this);
 				img = new ImageView(this);
 				img.setImageResource(R.drawable.andconlablauncher);
 				Spanned span = Html.fromHtml("<b>"+speaker.getFirstName() + " " + speaker.getLastName() +"</B><BR>" + speaker.getBio());
 				ftv.addView(img);
 				ftv.setText(span);
 				ftv.setTextSize(getResources().getDimensionPixelSize(R.dimen.sub_title_size));
 				ftv.invalidate();
 				ll.addView(ftv);
 			}
 		}
 	}
 	
 	@Override
 	  public boolean onOptionsItemSelected(MenuItem item) {
 	     switch (item.getItemId()) {
 	        case android.R.id.home:
 	           finish();
 	           return true;
 	     }
 	     return super.onOptionsItemSelected(item);
 	  }
 }
