 package se.chalmers.watchme.activity;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import se.chalmers.watchme.R;
 import se.chalmers.watchme.R.id;
 import se.chalmers.watchme.R.layout;
 import se.chalmers.watchme.R.menu;
 import se.chalmers.watchme.database.DatabaseHandler;
 import se.chalmers.watchme.model.Movie;
 import se.chalmers.watchme.ui.DatePickerFragment;
 import se.chalmers.watchme.ui.DatePickerFragment.DatePickerListener;
 import se.chalmers.watchme.utils.DateConverter;
 import android.os.Bundle;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.NavUtils;
 import android.view.View.OnClickListener;
 
 public class AddMovieActivity extends FragmentActivity 
 	implements DatePickerListener{
 	
 	private TextView titleField;
 	private TextView dateField;
 	private Button datePickerButton;
 	private TextView noteField;
 	private Button addButton;
 	
 	private final Context context = this;
 	private DatabaseHandler db;
 	
 	private Calendar releaseDate;
 
     @SuppressLint("NewApi")
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_add_movie);
         getActionBar().setDisplayHomeAsUpEnabled(true);
         
         this.releaseDate = Calendar.getInstance();
         
         this.titleField = (TextView) findViewById(R.id.title_field);
         
         //TODO Use the XML-value although it is overwritten here?
         this.dateField = (TextView) findViewById(R.id.release_date_label);
         dateField.setText(DateConverter.toSimpleDate(this.releaseDate));
         
         this.datePickerButton = (Button) findViewById(R.id.release_date_button);
         this.noteField = (TextView) findViewById(R.id.note_field);
         this.addButton = (Button) findViewById(R.id.add_movie_button);
         
         db = new DatabaseHandler(this);
         
         /**
          * Click callback. Shows the date picker for a movies release date
          */
         this.datePickerButton.setOnClickListener(new OnClickListener() {
         	
         	public void onClick(View v) {
         		DialogFragment datePickerFragment = new DatePickerFragment();
                 datePickerFragment.show(getSupportFragmentManager(),
                 		"datePicker");
         	}
         });
         
         
         /**
          * Click callback. Create a new Movie object and set it on
          * the Intent, and then finish this Activity.
          */
         this.addButton.setOnClickListener(new OnClickListener() {
 			
 			public void onClick(View v) {
 				String movieTitle = titleField.getText().toString();
 				String movieNote = noteField.getText().toString();
 				
 				Movie movie = new Movie(movieTitle);
 				movie.setNote(movieNote);
 				
				
 				db.addMovie(movie);
 				
 				Intent home = new Intent(context, MainActivity.class);
 				setResult(RESULT_OK, home);
 				home.putExtra("movie", movie);
 				
 				finish();
 			}
 		});
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_add_movie, menu);
         return true;
     }
 
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
     
     // @Override is not allowed in Java 1.5 for inherited interface methods
     public void setDate(Calendar pickedDate) {
 		
 		this.releaseDate = pickedDate;
 
 		dateField.setText(DateConverter.toSimpleDate(this.releaseDate));
 		
 	}
 
 }
