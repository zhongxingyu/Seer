 package dat255.grupp06.bibbla.frontend;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.TextView.OnEditorActionListener;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.view.Window;
 
 import dat255.grupp06.bibbla.R;
 import dat255.grupp06.bibbla.backend.Backend;
 import dat255.grupp06.bibbla.model.Book;
 import dat255.grupp06.bibbla.utils.Callback;
 import dat255.grupp06.bibbla.utils.Message;
 
 /**
  * A fragment that holds the search bar, button 
  * and a ListFragment that displays search results
  * 
  * @author Jonathan Orr
  */
 
 public class SearchFragment extends SherlockFragment {
 
 	Backend backend;
 	SearchListFragment listFragment;
 	EditText searchEdit;
 	Button searchButton;
 	
 	@Override
 	/**
 	 * Inflates the view and sets up the ListFragment.
 	 */
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
 		FragmentManager fragmentManager = getFragmentManager();
         FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
         
         listFragment = new SearchListFragment();
         fragmentTransaction.add(R.id.list_container, listFragment);
         fragmentTransaction.commit();
 		
 		return inflater.inflate(R.layout.fragment_search, container, false);
 	}
 	
 	@Override
 	/**
 	 * Makes keyboard "enter" start searching procedure.
 	 */
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		
 		// Get our GUI elements.
 		searchButton = (Button)getSherlockActivity().findViewById(R.id.button_search);
 		searchEdit = (EditText)getSherlockActivity().findViewById(R.id.edit_search);
 
 		// Create a listener for keyboard "enter" button.
 		OnEditorActionListener listener = new OnEditorActionListener() {
 		    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 		        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
 		        	// Simulate click on the search button.
 		            searchButton.performClick();
 		            return true;
 		        }
 		        return false;
 		    }
 		};
 		// Add our newly created listener to the text field.
 		searchEdit.setOnEditorActionListener(listener);
 	}
 	
 	/**
 	 * Starts searching procedure in backend.
 	 */
 	public void searchClicked() {
		
		// Convert search string to UTF-8.
 		String searchString = searchEdit.getText().toString();
 		try {
			searchString = URLEncoder.encode(searchString, "UTF-8");
 		} catch (UnsupportedEncodingException e) {}

 		// Display progress bar.
 		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
 		
 		// Hide keyboard.
         InputMethodManager imm = (InputMethodManager) getSherlockActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(searchEdit.getWindowToken(), 0);
 		
 		// No input? No need to search.
 		if("".equals(searchString)) {
 			listFragment.updateList(new ArrayList<Book>());
 			return;
 		}
 		
 		// Create a new callback object, which refers to our searchDone(). 
 		Callback c = new Callback() {
 			public void handleMessage(Message msg) {
 				SearchFragment.this.searchDone(msg);
 			}
 		};
 		
 		// Call backend search.
 		backend.search(searchString, 0, c);
 	}
 	
 	/** Is called when backend searching is done.**/
 	public void searchDone(Message msg) {
 		// Hide progress bar.
 		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
 		
 		// Did the job fail?
 		if (msg.error != null) {
 			// Log,
 			Log.e("searching", "Searching failed: "+msg.error);
 			// And toast user.
 			Toast.makeText(getSherlockActivity().getApplicationContext(), "Searching failed: "+msg.error, Toast.LENGTH_LONG).show();
 			return;
 		}
 		// Convert results to List<Book>.  
 		ArrayList<Book> books = (ArrayList<Book>) msg.obj;
 				
 		// Did we get no results? 
 		if (books.size() == 0) {
 			Toast.makeText(getSherlockActivity().getApplicationContext(), "No results found.", Toast.LENGTH_LONG).show();
 		}
 		
 		// Update list with titles (empty or not).
 		listFragment.updateList(books);
 	}
 
 	public void setBackend(Backend b) {
 		backend = b;
 	}
 }
