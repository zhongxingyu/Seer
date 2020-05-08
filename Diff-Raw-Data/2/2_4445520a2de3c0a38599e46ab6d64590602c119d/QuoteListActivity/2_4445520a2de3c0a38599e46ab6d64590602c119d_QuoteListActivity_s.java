 package edu.rosehulman.moviequotesandroid;
 
 import java.io.IOException;
 
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.google.api.client.extensions.android.http.AndroidHttp;
 import com.google.api.client.json.gson.GsonFactory;
 
 import fisherds_movie_quotes.moviequotes.Moviequotes;
 import fisherds_movie_quotes.moviequotes.MoviequotesRequest;
 import fisherds_movie_quotes.moviequotes.MoviequotesRequestInitializer;
 import fisherds_movie_quotes.moviequotes.model.MovieQuote;
 import fisherds_movie_quotes.moviequotes.model.MovieQuoteCollection;
 
 public class QuoteListActivity extends ListActivity {
 
 	private Moviequotes mService;
 	private static final String MQ = "MQ";
 	private static final boolean USE_LOCAL_HOST = false;
 //	private static final String LOCAL_HOST_URL = "http://137.112.46.122:8080/_ah/api/";
	// private static final String LOCAL_HOST_URL = "http://10.0.1.11:8080/_ah/api/";
 	private static final String LOCAL_HOST_URL = "http://10.0.1.3:8080/_ah/api/";
 	
 
 	/** Dialog ID for adding and editing quotes (one dialog for both tasks). */
 	private static final int INSERT_QUOTE_DIALOG_ID = 1;
 	private static final int DELETE_QUOTE_DIALOG_ID = 2;
 
 	/**
 	 * Constant to indicate that no row is selected for editing.
 	 */
 	public static final int NO_POSITION_SELECTED = -1;
 
 	private int mSelectedPosition = NO_POSITION_SELECTED;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_quote_list);
 
 		// Create the service.
 		Moviequotes.Builder builder = new Moviequotes.Builder(
 				AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
 		builder.setApplicationName(getString(R.string.app_name));
 		if (USE_LOCAL_HOST) {
 			builder.setRootUrl(LOCAL_HOST_URL);
 		      // This is necessary for running against a local dev_appserver.py, due to this bug:
 		      // https://code.google.com/p/googleappengine/issues/detail?id=9140
 		      // TODO: remove this HACK once the bug is fixed.
 		      builder.setGoogleClientRequestInitializer(new MoviequotesRequestInitializer() {
 		        @Override
 		        protected void initializeMoviequotesRequest(MoviequotesRequest<?> request) {
 		          request.setDisableGZipContent(true);
 		        }
 		      });
 		}
 		mService = builder.build();
 
 
 		// Initial testing.
 		// ArrayList<MovieQuote> testQuotes = new ArrayList<MovieQuote>();
 		// MovieQuote quote1 = new MovieQuote();
 		// quote1.setMovieTitle("Title1");
 		// quote1.setQuote("Quote1");
 		// testQuotes.add(quote1);
 		// MovieQuote quote2 = new MovieQuote();
 		// quote2.setMovieTitle("Title2");
 		// quote2.setQuote("Quote2");
 		// testQuotes.add(quote2);
 		// MovieQuote quote3 = new MovieQuote();
 		// quote3.setMovieTitle("Title3");
 		// quote3.setQuote("Quote3");
 		// testQuotes.add(quote3);
 		// MovieQuoteArrayAdapter adapter = new MovieQuoteArrayAdapter(this,
 		// android.R.layout.simple_expandable_list_item_2, android.R.id.text1,
 		// testQuotes);
 		// setListAdapter(adapter);
 
 		updateQuotes();
 		registerForContextMenu(getListView());
 	}
 
 	// ======================================================================
 	// Click listeners
 	// ======================================================================
 
 	@Override
 	protected void onListItemClick(ListView listView, View selectedView,
 			int position, long id) {
 		super.onListItemClick(listView, selectedView, position, id);
 		// Show a Toast or other dialog (VERY optional).
 		MovieQuote thisRowsQuote = (MovieQuote) getListAdapter().getItem(position);
 		Toast.makeText(this, thisRowsQuote.getQuote(), Toast.LENGTH_SHORT).show();
 	}
 
 	// ======================================================================
 	// AsyncTasks for doing endpoints communication
 	// ======================================================================
 	
 	private void updateQuotes() {
 		new QueryForQuotesTask(this).execute(); // Get quotes from the backend.
 	}
 
 	private class QueryForQuotesTask extends
 			AsyncTask<Void, Void, MovieQuoteCollection> {
 		Context context;
 
 		public QueryForQuotesTask(Context context) {
 			this.context = context;
 		}
 
 		protected MovieQuoteCollection doInBackground(Void... unused) {
 			MovieQuoteCollection quotes = null;
 			try {
 //				quotes = mService.quotes().list().execute(); // unsorted
 				Moviequotes.Quotes.List quotesQuery = mService.quotes().list();
 				quotesQuery.setOrder("-last_touch_date_time");
 				quotes = quotesQuery.execute();
 			} catch (IOException e) {
 				Log.d(MQ, e.getMessage(), e);
 			}
 			return quotes;
 		}
 
 		protected void onPostExecute(MovieQuoteCollection quotes) {
 			if (quotes == null) {
 				Log.d(MQ, "No quotes received");
 				Toast.makeText(QuoteListActivity.this, "Query error", Toast.LENGTH_SHORT).show();
 				return;
 			}
 			Log.d(MQ, "Received " + quotes.getItems().size() + " movie quotes.");
 			Toast.makeText(QuoteListActivity.this, "Query for quotes completed successfully", Toast.LENGTH_SHORT).show();
 			MovieQuoteArrayAdapter adapter = new MovieQuoteArrayAdapter(
 					context, android.R.layout.simple_expandable_list_item_2,
 					android.R.id.text1, quotes.getItems());
 			setListAdapter(adapter);
 		}
 	}
 
 	/** Insert. */
 	private class InsertQuoteTask extends
 			AsyncTask<MovieQuote, Void, MovieQuote> {
 
 		protected MovieQuote doInBackground(MovieQuote... movieQuotes) {
 			MovieQuote quoteInserted = null;
 			try {
 				Log.d(MQ, "Inserting movie quote.  Title = " + movieQuotes[0].getMovieTitle() +"   Quote = " + movieQuotes[0].getQuote() + " id " + movieQuotes[0].getId());
 				quoteInserted = mService.quote().insert(movieQuotes[0]).execute();
 			} catch (IOException e) {
 				Log.d(MQ, "Error inserting quote. Error message = " + e.getMessage(), e);
 			}
 			return quoteInserted;
 		}
 
 		protected void onPostExecute(MovieQuote quote) {
 			if (quote == null) {
 				Log.d(MQ, "No quote was inserted.");
 				Toast.makeText(QuoteListActivity.this, "Insert error", Toast.LENGTH_SHORT).show();
 				return;
 			}
 			Log.d(MQ, "Insert complete. Received back quote = " + quote.getQuote());
 			Toast.makeText(QuoteListActivity.this, "Insert completed successfully", Toast.LENGTH_SHORT).show();
 			updateQuotes(); // Refresh the list of quotes.
 		}
 	}
 	
 	/** Delete. */
 	private class DeleteQuoteTask extends
 			AsyncTask<Long, Void, MovieQuote> {
 
 		protected MovieQuote doInBackground(Long... ids) {
 			MovieQuote quoteDeleted = null;
 			try {
 				quoteDeleted = mService.quote().delete(ids[0]).execute();
 			} catch (IOException e) {
 				Log.d(MQ, "Error deleting quote. Error message = " + e.getMessage(), e);
 			}
 			return quoteDeleted;
 		}
 
 		protected void onPostExecute(MovieQuote quote) {
 			if (quote == null) {
 				Log.d(MQ, "No quote was deleted.");
 				Toast.makeText(QuoteListActivity.this, "Delete error", Toast.LENGTH_SHORT).show();
 				return;
 			}
 			Log.d(MQ, "Delete complete. Received back quote = " + quote.getQuote());
 			Toast.makeText(QuoteListActivity.this, "Delete completed successfully", Toast.LENGTH_SHORT).show();
 			updateQuotes(); // Refresh the list of quotes.
 		}
 	}
 
 	// ======================================================================
 	// Menus
 	// ======================================================================
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.quote_list, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.add_quote:
 			mSelectedPosition = NO_POSITION_SELECTED;
 			showDialog(INSERT_QUOTE_DIALOG_ID);
 			return true;
 		case R.id.force_sync:
 			new QueryForQuotesTask(this).execute();
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	/** Create a context menu for the list view. */
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		MenuInflater inflator = getMenuInflater();
 		if (v == getListView()) {
 			inflator.inflate(R.menu.quotes_context_menu, menu);
 		}
 	}
 
 	/** Standard listener for the context menu item selections. */
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		mSelectedPosition = info.position;
 		switch (item.getItemId()) {
 		case R.id.menu_item_list_view_delete:
 			showDialog(DELETE_QUOTE_DIALOG_ID);
 			return true;
 		case R.id.menu_item_list_view_edit:
 			showDialog(INSERT_QUOTE_DIALOG_ID);
 			return true;
 		}
 		return super.onContextItemSelected(item);
 	}
 
 	// ======================================================================
 	// Dialogs for adding, updating, and deleting movie quotes
 	// ======================================================================
 
 	/** Create the dialog if it has never been launched. Uses a custom dialog layout. */
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		super.onCreateDialog(id);
 		final Dialog dialog = new Dialog(this);
 		switch (id) {
 		case INSERT_QUOTE_DIALOG_ID:
 			dialog.setContentView(R.layout.insert_quote_dialog);
 			dialog.setTitle("New Quote");
 			final Button confirmButton = (Button) dialog.findViewById(R.id.confirm_insert_quote_button);
 			final Button cancelButton = (Button) dialog.findViewById(R.id.cancel_quote_button);
 			final EditText movieTitleEditText = (EditText) dialog.findViewById(R.id.edittext_movie_title);
 			final EditText quoteEditText = (EditText) dialog.findViewById(R.id.edittext_quote);
 			confirmButton.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					if (mSelectedPosition != NO_POSITION_SELECTED) {
 						// Updating an existing quote.
 						Log.d(MQ, "Updating an existing quote.");
 						MovieQuote selectedQuote = (MovieQuote) getListAdapter().getItem(mSelectedPosition);
 						selectedQuote.setMovieTitle(movieTitleEditText.getText().toString());
 						selectedQuote.setQuote(quoteEditText.getText().toString());
 						new InsertQuoteTask().execute(selectedQuote);
 					} else {
 						// Creating a new quote.
 						Log.d(MQ, "Creating a new quote.");
 						MovieQuote newQuote = new MovieQuote();
 						newQuote.setMovieTitle(movieTitleEditText.getText().toString());
 						newQuote.setQuote(quoteEditText.getText().toString());
 						new InsertQuoteTask().execute(newQuote);
 					}
 					dialog.dismiss();
 				}
 			});
 			cancelButton.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					dialog.dismiss();
 				}
 			});
 			break;
 		case DELETE_QUOTE_DIALOG_ID:
 			dialog.setContentView(R.layout.delete_quote_dialog);
 			dialog.setTitle("Are you sure you wish to delete this quote?");
 			final Button confirmDeleteButton = (Button) dialog.findViewById(R.id.confirm_delete_button);
 			final Button cancelDeleteButton = (Button) dialog.findViewById(R.id.cancel_delete_button);
 			confirmDeleteButton.setOnClickListener(new View.OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					Log.d(MQ, "Deleting the quote");
 					MovieQuote selectedQuote = (MovieQuote) getListAdapter().getItem(mSelectedPosition);
 					new DeleteQuoteTask().execute(selectedQuote.getId());
 					dialog.dismiss();
 				}
 			});
 			cancelDeleteButton.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					dialog.dismiss();
 				}
 			});
 			break;
 		default:
 			break;
 		}
 		return dialog;
 	}
 
 	/** Update the dialog with appropriate text before presenting to the user. */
 	@Override
 	protected void onPrepareDialog(int id, Dialog dialog) {
 		super.onPrepareDialog(id, dialog);
 		switch (id) {
 		case INSERT_QUOTE_DIALOG_ID:
 			final EditText movieTitleEditText = (EditText) dialog.findViewById(R.id.edittext_movie_title);
 			final EditText quoteEditText = (EditText) dialog.findViewById(R.id.edittext_quote);
 			final Button confirmButton = (Button) dialog.findViewById(R.id.confirm_insert_quote_button);
 			if (mSelectedPosition == NO_POSITION_SELECTED) {
 				dialog.setTitle("New Quote");
 				confirmButton.setText("Create Quote");
 				movieTitleEditText.setText("");
 				quoteEditText.setText("");
 			} else {
 				dialog.setTitle("Edit quote");
 				confirmButton.setText("Update");
 				MovieQuote selectedQuote = (MovieQuote) getListAdapter().getItem(mSelectedPosition);
 				movieTitleEditText.setText(selectedQuote.getMovieTitle());
 				quoteEditText.setText(selectedQuote.getQuote());
 			}
 			break;
 		}
 	}
 
 }
