 package se.chalmers.watchme.ui;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.ResponseCache;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Calendar;
 
 import se.chalmers.watchme.R;
 import se.chalmers.watchme.activity.AddMovieActivity;
 import se.chalmers.watchme.activity.MovieDetailsActivity;
 import se.chalmers.watchme.database.DatabaseAdapter;
 import se.chalmers.watchme.database.MoviesTable;
 import se.chalmers.watchme.database.MyCursorLoader;
 import se.chalmers.watchme.database.WatchMeContentProvider;
 import se.chalmers.watchme.model.Movie;
 import se.chalmers.watchme.net.ImageDownloadTask;
 import se.chalmers.watchme.notifications.NotificationClient;
 import se.chalmers.watchme.utils.DateTimeUtils;
 import se.chalmers.watchme.utils.ImageCache;
 import android.annotation.TargetApi;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.ListFragment;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.Loader;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ImageView;
import android.widget.RatingBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 // TODO Important! API level required does not match with what is used
 @TargetApi(11)
 public class MovieListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
 	
 	private SimpleCursorAdapter adapter;
 	private DatabaseAdapter db;
 	
 	private AsyncTask<String, Void, Bitmap> imageTask;
 	private Long tagId;
 	private String orderBy;
 	
 	public MovieListFragment() {
 		super();
 		this.tagId = (long) -1;
 	}
 
 	public MovieListFragment(Long tagId) {
 		super();
 		this.tagId = tagId;
 		
 	}
 	
 	@Override
 	public void onActivityCreated(Bundle b) {
 		super.onActivityCreated(b);
 		
 		// TODO: Has to be done in onCreate instead?
 		setHasOptionsMenu(true);
 		Thread.currentThread().setContextClassLoader(getActivity().getClassLoader());
 		
 		// We want to participate in customizing the Action bar options menu
 		this.setHasOptionsMenu(true);
 		
 		final File cacheDir = getActivity().getBaseContext().getCacheDir();
 		ResponseCache.setDefault(new ImageCache(cacheDir));
 
 		String[] from = new String[] { 
 				MoviesTable.COLUMN_MOVIE_ID, 
 				MoviesTable.COLUMN_TITLE,  
 				MoviesTable.COLUMN_RATING ,
 				MoviesTable.COLUMN_DATE,
 				MoviesTable.COLUMN_POSTER_SMALL
 				};
 		
 		int[] to = new int[] { 0 , 
 				R.id.title, 
 				R.id.raiting, 
 				R.id.date,
 				R.id.poster};
 		
 		getActivity().getSupportLoaderManager().initLoader(2, null, this);
 		adapter = new SimpleCursorAdapter(getActivity(), R.layout.list_item_movie , null, from, to, 0);
 		
 		/**
 		 * Convert date text from millis to dd-mm-yyyy format
 		 */
 		//TODO: Refactor?
 		adapter.setViewBinder(new ViewBinder() {
 			
 			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
 				
 				if (columnIndex == cursor.getColumnIndexOrThrow(MoviesTable.COLUMN_DATE)) {
 					String date = cursor.getString(columnIndex);
 					TextView textView = (TextView) view;
 					Calendar cal = Calendar.getInstance();
 					cal.setTimeInMillis(Long.parseLong(date));
 					String formattedDate = DateTimeUtils.toSimpleDate(cal);
 					
 					textView.setText(formattedDate);
 					return true;
 				}
 				
 				/*
				 * Handle rating bar conversion
				 */
				else if (columnIndex == cursor.getColumnIndexOrThrow(MoviesTable.COLUMN_RATING)) {
					int rating = cursor.getInt(columnIndex);
					RatingBar bar = (RatingBar) view;
					bar.setRating(rating);
					
					return true;
				}
				
				/*
 				 * Handle poster images
 				 */
 				
 				else if(columnIndex == cursor.getColumnIndexOrThrow(MoviesTable.COLUMN_POSTER_SMALL)) {
 					String smallImageUrl = cursor.getString(columnIndex);
 					final ImageView imageView = (ImageView) view;
 					
 					if(smallImageUrl != null && !smallImageUrl.isEmpty()) {
 						
 						// Fetch the image in an async task
 						imageTask = new ImageDownloadTask(new ImageDownloadTask.TaskActions() {
 							
 							public void onFinished(Bitmap image) {
 								if(image != null) {
 									((ImageView) imageView).setImageBitmap(image);
 								}
 							}
 						});
 						
 						imageTask.execute(new String[]{smallImageUrl});
 					}
 					
 					return true;
 				}
 				
 				return false;
 			}
 		});
 		
 		setListAdapter(adapter);
 	    
 		// Set up listeners to delete and view a movie
         this.getListView().setOnItemClickListener(new OnDetailsListener());
 	    this.getListView().setOnItemLongClickListener(new OnDeleteListener());
 	}
 	
 	/*
 	 * Show the Share and Sort buttons while movie list view
 	 */
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		MenuItem sortItem = menu.findItem(R.id.menu_sort_button);
 		MenuItem shareItem = menu.findItem(R.id.menu_send_email_button);
 		
 		sortItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 		shareItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 		
 		// If there aren't any movies in the list, disable the "Share list" button
 		int count = new DatabaseAdapter(getActivity().getContentResolver()).getMovieCount();
     	
     	if(count == 0) {
     		shareItem.setEnabled(false);
     	}
 		
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		
 		return inflater.inflate(R.layout.movie_list_fragment_view, container, false);
 	}
 	
 	@Override
 	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
 		
 		return new MyCursorLoader(getActivity(), 
 				WatchMeContentProvider.CONTENT_URI_MOVIES,tagId,orderBy);
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
 		adapter.swapCursor(cursor);
 		adapter.notifyDataSetChanged();
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> arg0) {
 		// data is not available anymore, delete reference
 	    adapter.swapCursor(null);
 	    adapter.notifyDataSetChanged();
 		
 	}
 	
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	
     	switch(item.getItemId()) {
     	case R.id.menu_sort_button:
     		sortList();
     		break;
     	default:
     		break;
     	}
     	return super.onOptionsItemSelected(item);
     }
     
     /**
      * Show a Dialog Box with choices of attributes to order the Movies by.
      */
     private void sortList() {
     	final String[] alternatives = { "Date", "Rating", "Title" };
     	db = new DatabaseAdapter(getActivity().getContentResolver());
     	
     	AlertDialog.Builder alertbox = new AlertDialog.Builder(getActivity());
     	alertbox.setTitle("Order by");
     	alertbox.setSingleChoiceItems(alternatives, 0,
     			new DialogInterface.OnClickListener() {
     			public void onClick(DialogInterface dialog, int item) {
 
     				switch(item) {
     				case 0:
     					orderBy = MoviesTable.COLUMN_DATE;
     					break;
     				case 1:
     					orderBy = MoviesTable.COLUMN_RATING;
     					break;
     				case 2:
     					orderBy = MoviesTable.COLUMN_TITLE;
     					break;
     				default:
     					break;
     				}
     				// Change the cursor
     				Cursor cursor = db.getAllMoviesCursor(orderBy);
     				adapter.changeCursor(cursor);
     				
     				dialog.dismiss();
     			}
     			});
         
         alertbox.show();
     }
 	
 	/**
      * Listener for when the user clicks an item in the list
      * 
      * The movie object in the list is used to fill a new activity with data
      * 
      * @author Robin
      */
     private class OnDetailsListener implements OnItemClickListener {
 
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			
 			db = new DatabaseAdapter(getActivity().getContentResolver());
 			
 			if(imageTask != null && imageTask.getStatus() == AsyncTask.Status.RUNNING) {
 				imageTask.cancel(true);
 			}
 			
 			Cursor selectedMovie = (Cursor) getListView().getItemAtPosition(position);
 			Movie movie = db.getMovie(Long.parseLong(selectedMovie.getString(0)));
 			
 			Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
 			
 			// TODO Fetch all data from database in DetailsActivity instead?
 			intent.putExtra(MovieDetailsActivity.MOVIE_EXTRA, movie);
 			
 			startActivity(intent);
 			
 		}
     }
 	
 	 /**
      * The listener for when the user does a long-tap on an item in the list.
      * 
      * The Movie object in the list is removed if the user confirms that he wants to remove the Movie.
      * 
      * @author Johan
      * @author lisastenberg
      */
     private class OnDeleteListener implements OnItemLongClickListener {
     	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
 			db = new DatabaseAdapter(getActivity().getContentResolver());
 			
 			Cursor selectedMovie = (Cursor) getListView().getItemAtPosition(position);
     		final Movie movie = db.getMovie(Long.parseLong(selectedMovie.getString(0)));
     		
             AlertDialog.Builder alertbox = new AlertDialog.Builder(getActivity());
             alertbox.setMessage("Are you sure you want to delete the movie \"" + movie.getTitle() + "\"?");           
             alertbox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface arg0, int arg1) {
                 	
                 	db = new DatabaseAdapter(getActivity().getContentResolver());
                 	db.removeMovie(movie);
                 	
                 	NotificationClient.cancelNotification(getActivity(), movie);
                     Toast.makeText(getActivity().getApplicationContext(), "\"" + movie.getTitle() + "\" was deleted" , Toast.LENGTH_SHORT).show();
                 }
             });
             alertbox.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface arg0, int arg1) {
                     
                 }
             });
             
             alertbox.show();
 			return true;
 		}    	
 	}
 }
