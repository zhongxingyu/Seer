 /**
     This file is part of Bibbla.
 
     Bibbla is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Bibbla is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Bibbla.  If not, see <http://www.gnu.org/licenses/>.    
  **/
 
 package dat255.grupp06.bibbla.frontend;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Window;
 
 import dat255.grupp06.bibbla.R;
 import dat255.grupp06.bibbla.backend.Backend;
 import dat255.grupp06.bibbla.fragments.BookListFragment;
 import dat255.grupp06.bibbla.fragments.ProfileFragment;
 import dat255.grupp06.bibbla.fragments.SearchFragment;
 import dat255.grupp06.bibbla.fragments.SearchListFragment;
 
 import dat255.grupp06.bibbla.model.Book;
 import dat255.grupp06.bibbla.model.CredentialsMissingException;
 import dat255.grupp06.bibbla.utils.Callback;
 import dat255.grupp06.bibbla.utils.Message;
 
 
 /**
  * An activity that will display more details about a selected book.
  * @author Jonathan Orr
  *
  */
 public class BookOverlayActivity extends SherlockActivity {
 
 	Book book;
 	boolean isReserved;
 	boolean isLoaned;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		//Sets up some graphical stuff
 		setTheme(com.actionbarsherlock.R.style.Theme_Sherlock); //Used for theme switching in samples
 		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_book_overlay);
 		getSupportActionBar().hide();
 
 		//Get all of the data the intent sends
 		Intent intent = getIntent();
 
 		book =(Book)intent.getSerializableExtra("dat255.grupp06.bibbla.BOOK");
 		Log.d("Jonis", "unreserveid : "+book.getUnreserveId());
 		((TextView)findViewById(R.id.overlay_book_title)).setText(book.getName());
 		((TextView)findViewById(R.id.overlay_book_author)).setText(book.getAuthor());
 		isReserved = intent.getBooleanExtra(BookListFragment.RESERVED, false);
 		isLoaned = intent.getBooleanExtra(BookListFragment.LOANED, false);
 
 		if(isReserved) {
 			((Button)findViewById(R.id.button_reserve_book)).setHint("Avreservera");
 			((Spinner)findViewById(R.id.library_spinner)).setVisibility(Spinner.INVISIBLE);
 		} else if(isLoaned) {
 			((Button)findViewById(R.id.button_reserve_book)).setHint("Frlng ln");
 			((Spinner)findViewById(R.id.library_spinner)).setVisibility(Spinner.INVISIBLE);
 		} else if(!Backend.getBackend().isLoggedIn()) {
 			((Button)findViewById(R.id.button_reserve_book)).setVisibility(Button.INVISIBLE);
 			((Spinner)findViewById(R.id.library_spinner)).setVisibility(Spinner.INVISIBLE);
 		}
 		
 		//Then get the rest from Gotlib's servers
 	    if(!isReserved && !isLoaned) {
 			Callback c = new Callback() {
 				public void handleMessage(Message msg) {
 					BookOverlayActivity.this.setDetails(msg);
 				}
 			};
 	    
 			Backend.getBackend().fetchDetailedView(book, c);
 	    }
 		
 		
 	}
 
 	/**
 	 * This method will be called by a callback object when a reservation is done.
 	 * @param msg - A message object that will contain any eventual error messages
 	 */
 	public void reserveDone(Message msg) {
 		((TextView)findViewById(R.id.text_reserve_book)).setText("Reservation klar!");
 		setSupportProgressBarIndeterminateVisibility(false);
 	}
 	
 	public void unReserveDone(Message msg) {	
 		if(msg.error == null) {
 			((TextView)findViewById(R.id.text_reserve_book)).setText("Avreservation klar!");
 			setSupportProgressBarIndeterminateVisibility(false);
 		} else {
 			Log.d("Jonis", msg.error.toString());
 		}
 	}
 	
 	public void renewLoanDone(Message msg) {
 		if(msg.error == null) {
 		((TextView)findViewById(R.id.text_reserve_book)).setText("Lnet r frlngt!");
 		setSupportProgressBarIndeterminateVisibility(false);
 		} else {
 			Log.d("Jonis", msg.error.toString());
 		}
 	}
 
 	/**
 	 * This method is called when the reserve book button is pressed on the
 	 * book overlay.
 	 * @param view
 	 */
 	public void reserveBook(View view) {
 		setSupportProgressBarIndeterminateVisibility(true);
 		
 		if(isReserved) {
 			Callback c = new Callback() {
 				public void handleMessage(Message msg) {
 					BookOverlayActivity.this.unReserveDone(msg);
 				}
 			};
 			
 			try {
				// TODO: Unreserves everything temporarily. Change asap!
				Backend.getBackend().unreserve(c);
 			} catch (CredentialsMissingException e) {
 				Toast toast = Toast.makeText(getApplicationContext(), "Du r inte inloggad", Toast.LENGTH_SHORT);
 				toast.show();
 			}
 		} else if(isLoaned) {			
 			Callback c = new Callback() {
 				public void handleMessage(Message msg) {
 					BookOverlayActivity.this.renewLoanDone(msg);
 				}
 			};
 			
 			try {
 				Backend.getBackend().renew(book, c);
 			} catch (CredentialsMissingException e) {
 				Toast toast = Toast.makeText(getApplicationContext(), "Du r inte inloggad", Toast.LENGTH_SHORT);
 				toast.show();
 			}
 		} else {
 			Callback c = new Callback() {
 				public void handleMessage(Message msg) {
 					BookOverlayActivity.this.reserveDone(msg);
 				}
 			};
 
 			Spinner spinner = (Spinner)findViewById(R.id.library_spinner);
 			String lib = libraryToCode(String.valueOf(spinner.getSelectedItem()));
 
 			try {
 				Backend.getBackend().reserve(book, lib, c);
 				((TextView)findViewById(R.id.text_reserve_book)).setText("Reserverar bok...");
 			} catch (CredentialsMissingException e) {
 				setSupportProgressBarIndeterminateVisibility(false);
 			}		
 		}
 	}
 
 
 	/**
 	 * Method that is called from a callback object when the backend is done fetching
 	 * more details about a book.
 	 * @param msg - A message object that contains the data collected by the backend and
 	 * 				any eventual error messages.
 	 */
 	public void setDetails(Message msg) {
 		// Hide progress bar.
 		setSupportProgressBarIndeterminateVisibility(false);
 		// Did the job fail?
 		if (msg.error != null) {
 			// Log,
 			Log.e("Jonis", "Details fetcher failed: "+msg.error);
 			// And toast user.
 			Toast.makeText(getApplicationContext(), "Details fetcher failed: "+msg.error, Toast.LENGTH_LONG).show();
 			return;
 		}
 
 		book = (Book)msg.obj;
 
 		((TextView)findViewById(R.id.overlay_book_title)).setText(book.getName());
 		((TextView)findViewById(R.id.overlay_book_author)).setText(book.getAuthor());
 		((TextView)findViewById(R.id.overlay_book_isbn)).setText(book.getIsbn());
 		((TextView)findViewById(R.id.overlay_book_physical)).setText(book.getPhysicalDescription());
 	}
 
 	/**
 	 * 
 	 * @param library
 	 * @return
 	 */
 	public String libraryToCode(String library) {
 		if(library.equals("Askim"))
 			return "as";
 		else if(library.equals("Backa"))
 			return "ba";
 		else if(library.equals("Bergsjn"))
 			return "bs";
 		else if(library.equals("Biskopsgrden"))
 			return "bi";
 		else if(library.equals("Bokbuss Tur A"))
 			return "bussa";
 		else if(library.equals("Bokbuss Tur B"))
 			return "bussb";
 		else if(library.equals("Dons"))
 			return "do";
 		else if(library.equals("Dynamo p Stadsmuseet"))
 			return "dy";
 		else if(library.equals("Gamlestaden"))
 			return "ga";
 		else if(library.equals("Global p Stadsmuseet"))
 			return "in";
 		else if(library.equals("Guldheden"))
 			return "gu";
 		else if(library.equals("Hammarkullen"))
 			return "ha";
 		else if(library.equals("Hisingen"))
 			return "hi";
 		else if(library.equals("Hjllbo"))
 			return "hj";
 		else if(library.equals("Hrlanda/rgryte"))
 			return "hl";
 		else if(library.equals("Hgsbo"))
 			return "ho";
 		else if(library.equals("Kortedala"))
 			return "ko";
 		else if(library.equals("Kyrkbyn"))
 			return "ky";
 		else if(library.equals("Krra"))
 			return "ka";
 		else if(library.equals("Linnstaden"))
 			return "li";
 		else if(library.equals("Majorna"))
 			return "ma";
 		else if(library.equals("Miini p Rhsska"))
 			return "mi";
 		else if(library.equals("Styrs"))
 			return "st";
 		else if(library.equals("Torslanda"))
 			return "to";
 		else if(library.equals("Trulsegrden"))
 			return "tr";
 		else if(library.equals("Tuve"))
 			return "tu";
 		else if(library.equals("Vstra Frlunda"))
 			return "vf";
 		else if(library.equals("lvstranden"))
 			return "al";
 		else if(library.equals("300m2 p Sdra Hamng"))
 			return "ci";
 		else
 			return "error";
 	}
 
 
 }
