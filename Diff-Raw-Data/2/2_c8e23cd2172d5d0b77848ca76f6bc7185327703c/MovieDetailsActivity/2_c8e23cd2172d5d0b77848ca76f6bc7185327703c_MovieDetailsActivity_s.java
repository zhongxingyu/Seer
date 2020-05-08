 package se.chalmers.watchme.activity;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URL;
 import java.util.Date;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import se.chalmers.watchme.R;
 import se.chalmers.watchme.database.DatabaseAdapter;
 import se.chalmers.watchme.database.WatchMeContentProvider;
 import se.chalmers.watchme.model.Movie;
 import se.chalmers.watchme.model.Tag;
 import se.chalmers.watchme.net.IMDBHandler;
 import se.chalmers.watchme.net.ImageDownloadTask;
 import se.chalmers.watchme.net.MovieSource;
 import se.chalmers.watchme.ui.ImageDialog;
 import se.chalmers.watchme.utils.DateTimeUtils;
 import se.chalmers.watchme.utils.MovieHelper;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.BitmapDrawable;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.RatingBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.support.v4.app.NavUtils;
 
 // TODO IMPORTANT! Minimum allowed API is 11 by resources used,
 // although it is specified as 8. Research and fix it
 @TargetApi(11)
 public class MovieDetailsActivity extends Activity {
 	
 	public static final String MOVIE_EXTRA = "movie";
 	
 	private Movie movie;
 	private MovieSource imdb;
 	
 	private AsyncTask<String, Void, Bitmap> imageTask;
 	
 	// Timeout for fetching IMDb info (in milliseconds)
 	private final static int IMDB_FETCH_TIMEOUT = 10000;
 	
 	private ImageView poster;
 	
 	private ImageDialog dialog;
 	
 	private DatabaseAdapter db;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_movie_details);
         getActionBar().setDisplayHomeAsUpEnabled(true);
         
         this.movie = (Movie) getIntent().getSerializableExtra(MOVIE_EXTRA);
         this.imdb = new IMDBHandler();
         
         this.poster = (ImageView) findViewById(R.id.poster);
         this.poster.setOnClickListener(new OnPosterClickListener());
         
         this.dialog = new ImageDialog(this);
         
         /*
     	 * Create a new image download task for the poster image
     	 */
     	this.imageTask = new ImageDownloadTask(new ImageDownloadTask.TaskActions() {
 			
 			public void onFinished(Bitmap image) {
 				if(image != null) {
 					poster.setImageBitmap(image);
 				}
 			}
 		});
     	
         
         /*
          * If no movie id was received earlier then finish this activity before
          * anything else is done
          */
         if(this.movie == null) {
         	// TODO Why does this cause a crash?
         	finish();
         }
         
         // Kick off the fetch for IMDb info IF there's a set API id
         // set.
         if(this.movie.hasApiIDSet()){
         	final AsyncTask<Integer, Void, JSONObject> t = new IMDBTask()
         		.execute(new Integer[] {this.movie.getApiID()});
         	
         	// Cancel the task after a timeout
         	Handler handler = new Handler();
         	handler.postDelayed(new Runnable() {
 				
 				public void run() {
 					if(t.getStatus() == AsyncTask.Status.RUNNING) {
 						t.cancel(true);
 						System.err.println("Fetching IMDb info did timeout");
 					}
 				}
 			}, IMDB_FETCH_TIMEOUT);
         }
         
         // Populate various view fields from the Movie object
         populateFieldsFromMovie(this.movie);
         
     }
     
 	/**
 	 * Populate various view fields with data from a Movie.
 	 * 
 	 * @param m The movie to fill the fields with
 	 */
     public void populateFieldsFromMovie(Movie m) {
     	db = new DatabaseAdapter(this.getContentResolver());
     	
 		setTitle(m.getTitle());
 		
 		TextView noteField = (TextView) findViewById(R.id.note_field);
         RatingBar ratingBar = (RatingBar) findViewById(R.id.my_rating_bar);
         TextView tagField = (TextView) findViewById(R.id.tag_field);
         TextView releaseDate = (TextView) findViewById(R.id.releaseDate);
 		
     	noteField.setText(m.getNote());
         ratingBar.setRating(m.getRating());
         releaseDate.setText(DateTimeUtils.toSimpleDate(m.getDate()));
         
         Cursor cursor = db.getAttachedTags(m);
         String tags = "";
         if(cursor.moveToFirst()) {
         	tags = cursor.getString(1);
         	while(cursor.moveToNext()) {
         		tags = tags + "," + cursor.getString(1);
         	}
         }
        tagField.setText(tags.toString());
     }
     
     /*
      * TODO: These JSON-to-Android view parsing is too tight coupled to the
      * Activity I think .. I'd like to put this stuff somewhere else
      * where it's easier to test. 
      */
     
     public void populateFieldsFromJSON(JSONObject json) {
     	TextView rating = (TextView) findViewById(R.id.imdb_rating_number_label);
     	TextView plot = (TextView) findViewById(R.id.plot_content);
     	TextView cast = (TextView) findViewById(R.id.cast_list);
     	TextView duration = (TextView) findViewById(R.id.duration);
     	TextView genres = (TextView) findViewById(R.id.genres);
     	
     	double imdbRating = json.optDouble("rating");
     	if(!Double.isNaN(imdbRating)) {
     		rating.setText(String.valueOf(imdbRating));
     	}
     	
     	String imdbPlot = json.optString("overview");
     	if(!imdbPlot.isEmpty()) {
     		plot.setText(imdbPlot);
     	}
     	
     	int runtime = json.optInt("runtime");
     	if(runtime != 0) {
     		duration.setText(DateTimeUtils.minutesToHuman(runtime));
     	}
     	
     	
     	JSONArray imdbGenres = json.optJSONArray("genres");
     	
     	if(imdbGenres != null && imdbGenres.length() > 0) {
     		String genreString = "";
     		
     		for(int i = 0; i < imdbGenres.length(); i++) {
     			genreString += imdbGenres.optJSONObject(i).optString("name") + ", ";
     		}
     		
     		genres.setText(genreString);
     	}
     	
     	
     	JSONArray posters = json.optJSONArray("posters");
     	String imageURL = MovieHelper.getPosterFromCollection(posters, Movie.PosterSize.MID);
     	
     	// Fetch movie poster
     	this.imageTask.execute(new String[] {imageURL});
     	
     	
     	JSONArray imdbCast = json.optJSONArray("cast");
     	String actors = "";
     	
     	if(imdbCast != null) {
     		for(int i = 0; i < imdbCast.length(); i++) {
     			JSONObject o = imdbCast.optJSONObject(i);
     			if(o.optString("department").equalsIgnoreCase("actors")) {
     				actors += o.optString("name") + ", ";
     			}
     		}
     		
     		cast.setText(actors);
     	}
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_movie_details, menu);
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
 
 
     /**
      * The IMDb info fetch task.
      * 
      *  <p>This async task calls the IMDb API in order to fetch and
      *  show detailed JSON data from a single movie ID.</p>
      * 
      * @author Johan
      */
     private class IMDBTask extends AsyncTask<Integer, Void, JSONObject> {
 
     	private ProgressDialog dialog;
     	
     	public IMDBTask() {
     		this.dialog = new ProgressDialog(MovieDetailsActivity.this);
     	}
     	
     	@Override
     	protected void onPreExecute() {
     		this.dialog.setMessage("Loading from IMDb ...");
     		this.dialog.show();
     	}
     	
     	@Override
 		public void onCancelled() {
     		this.dialog.dismiss();
     		Toast.makeText(getBaseContext(), 
 					"An error occurred while fetching from IMDb", 
 					Toast.LENGTH_SHORT)
 			.show();
 		}
     	
 		@Override
 		protected JSONObject doInBackground(Integer... params) {
 			JSONObject response = imdb.getMovieById(params[0]);
 			
 			return response;
 		}
     	
 		@Override
 		protected void onPostExecute(JSONObject res) {
 			if(this.dialog.isShowing()) {
 				this.dialog.dismiss();
 			}
 			
 			// Update the UI with the JSON data
 			if(res != null) {
 				populateFieldsFromJSON(res);
 			}
 			else {
 				Toast.makeText(getBaseContext(), 
 						"An error occurred while fetching from IMDb", 
 						Toast.LENGTH_LONG)
 				.show();
 			}
 		}
     }
     
     /**
      * Listener class for when user clicks on the poster.
      * 
      *  <p>Gets the bitmap image from the poster and set it to the
      *  custom full screen overlay, then show it.</p>
      * 
      * @author Johan
      */
     private class OnPosterClickListener implements OnClickListener {
 
 		public void onClick(View v) {
 			ImageView view = (ImageView) v;
 			Bitmap bm = ((BitmapDrawable) view.getDrawable()).getBitmap();
 			
 			dialog.setImage(bm);
 			dialog.show();
 		}
     	
     }
 }
