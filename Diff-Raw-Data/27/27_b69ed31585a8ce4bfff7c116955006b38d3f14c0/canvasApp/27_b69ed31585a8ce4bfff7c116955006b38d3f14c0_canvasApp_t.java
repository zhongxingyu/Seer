 package com.group2.finger_occ_demo;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.FrameLayout;
 import android.widget.Spinner;
 
 import com.group2.finger_occ_demo.activities.MovieActivity;
 import com.group2.finger_occ_demo.activities.UserActivity;
 import com.group2.finger_occ_demo.data.DataObjects;
 import com.group2.finger_occ_demo.data.Movie;
 import com.group2.finger_occ_demo.io.DataObjectCreator;
 import com.group2.finger_occ_demo.io.SaveData;
 
 /**
  * Initializes Canvas and starts application.
  */
 public class canvasApp extends Activity implements OnItemSelectedListener, OnClickListener {
 	
 	public static DataObjects data;
 	public static List<String> directors = new ArrayList<String>();
 	public static Users users;
 	
 	//Application Constants
 	private final String MOVIE_FILE_NAME = "movies.json";
 	private final String USERS_FILE_NAME = "users.json";
 	
 	private MyCanvas canvasView;
 	private Button addMovie;
 	
 	/**
 	 * Handles any returns from screens generated from this screen.
 	 */
 	protected void onActivityResult(int requestCode, int resultCode, Intent data){
 		//set login button to correct value
 		Button login = (Button) findViewById(R.id.login);
 		if (users.currentUser() == null)
 			login.setText("Login");
 		else
 			login.setText("Profile");
 		
     	if (requestCode == MyCanvas.MOVIE_SELECT_PROCESS){
     		MyCanvas.movieFound = MyCanvas.moviesFound.get(resultCode);
     		startActivityForResult(new Intent(this, MovieActivity.class), MyCanvas.MOVIE_VIEW_PROCESS);
     	}
     	// need to reload the points because one could have changed
     	else
     		canvasView.onActivityResult(requestCode, resultCode, data);
     }
     
 	
     /** Called when the activity is first created.. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	DataObjectCreator creator = new DataObjectCreator(this.getAssets(), getBaseContext());
     	
         super.onCreate(savedInstanceState);
         
         // Give default view until main screen is loaded
         setContentView(R.layout.main);
         
         // Get the data from the JSON file
         data = creator.createMovieObjects(MOVIE_FILE_NAME);
         users = creator.createUserObjects(USERS_FILE_NAME);
         if (users == null)
         	users = new Users();
 
         //create directors list
         for(Movie m:data.getMovie()){
         	
         	if(!(directors.contains(m.getDirector()))){
         		directors.add(m.getDirector());
         	}
         }
         
         addMovie = (Button) findViewById(R.id.AddButton);
         addMovie.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				addMovie();
             }
 		});
         
         directors.add(0, "All");
         Collections.sort(directors);
         ArrayAdapter<CharSequence> adapter;
         
         Spinner dirs = (Spinner) findViewById(R.id.directors);
         adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, directors);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         dirs.setAdapter(adapter);
 
         Spinner genres = (Spinner) findViewById(R.id.genres);
         adapter = ArrayAdapter.createFromResource(this, R.array.genre_array, android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         genres.setAdapter(adapter);
         
         Spinner ratings = (Spinner) findViewById(R.id.ratings);
         adapter = ArrayAdapter.createFromResource(this, R.array.rating_array, android.R.layout.simple_spinner_item);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         ratings.setAdapter(adapter);
         
         Button search = (Button) findViewById(R.id.button1);
         
         // Create a new canvas set it as the view and give it focus. Also give it
         // the data created from the json file.
 		canvasView = new MyCanvas(this);
 		canvasView.setBackgroundColor(Color.WHITE);
 		
 		search.setOnClickListener(this);
 		dirs.setOnItemSelectedListener(this);
 		genres.setOnItemSelectedListener(this);
 		ratings.setOnItemSelectedListener(this);
 		
 		FrameLayout frame = (FrameLayout) findViewById(R.id.frame);
 		
 		frame.addView(canvasView);
     }
     
     /**
      * Adds a movie by going to the usual editing screen with nothing in it. Then by
      * pressing save.
      */
     private void addMovie(){
     	MyCanvas.movieFound = null;
 		startActivityForResult(new Intent(this, MovieActivity.class), MyCanvas.MOVIE_VIEW_PROCESS);
     }
 
     /**
      * Save all data so persistence is maintained.
      */
     public void onBackPressed(){
     	SaveData save = new SaveData(getBaseContext());
     	save.save(MOVIE_FILE_NAME, data, USERS_FILE_NAME, users);
     	this.finish();
     }
     
     @Override
    @SuppressWarnings(value = { "unused" })
 	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
 		 		 
 		Spinner genres = (Spinner)findViewById(R.id.genres);
 		Spinner dirs = (Spinner)findViewById(R.id.directors);
 		Spinner ratings = (Spinner)findViewById(R.id.ratings);
 		
 		String genre = genres.getSelectedItem().toString();
 		String rating = ratings.getSelectedItem().toString();
 		
 		if(parent.getId() == R.id.genres){
 			 
 			genre = genres.getSelectedItem().toString();
 		}
 		
 		else if(parent.getId() == R.id.ratings){
 			 
 			rating = ratings.getSelectedItem().toString();
 		}
 	
 	 	canvasView.scatterView.points.filter_points(genre, rating);
 
 	 	canvasView.invalidate();
 	}
     
 	public void onNothingSelected(AdapterView<?> parent) {}
 	
 	
 	
 	public void login(View view){
 		Intent intent = new Intent(this, UserActivity.class);
 		
 		startActivityForResult(intent, 0);
 	}
 
 	/**
 	 * Handles generic onclick events.
 	 */
 	public void onClick(View v) {
 		Spinner genres = (Spinner)findViewById(R.id.genres);
 		Spinner ratings = (Spinner)findViewById(R.id.ratings);
 		EditText searchText = (EditText)findViewById(R.id.searchText);
 		
 		String text = searchText.getText().toString();
 		String genre = genres.getSelectedItem().toString(); 
 		String rating = ratings.getSelectedItem().toString();
 		
 		//Search should respect filters that are already selected. We only ever search visible Movies.
 		canvasView.scatterView.points.filter_points(text, genre, rating);
 		canvasView.invalidate();
 	}
     
 }
