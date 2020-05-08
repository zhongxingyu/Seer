 package dat255.grupp06.bibbla.fragments;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 import dat255.grupp06.bibbla.R;
 import dat255.grupp06.bibbla.backend.Backend;
 import dat255.grupp06.bibbla.model.Book;
 import dat255.grupp06.bibbla.utils.Callback;
 import dat255.grupp06.bibbla.utils.Message;
 
 /**
  * Main fragment for the profile or "me" tab. You must give a reference to the
  * Backend object before using the fragment.
  * @author arla
  */
 public class ProfileFragment extends SherlockFragment {
 
 	Backend backend;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.profile_fragment, container, false);
 	}
 	
 	/**
 	 * Give a reference to the Backend
 	 * @param backend The Backend object used by the application
 	 */
 	public void setBackend(Backend backend) {
 		this.backend = backend;
 	}
 	
 	@Override
 	public void onStart() {
 		super.onStart();
 		updateFromBackend();
 	}
 	
 	/**
 	 * Update the contents of GUI elements with information from Backend, such
 	 * as name, current debt, books on loan and pending reservations.
 	 * @throws IllegalStateException if the backend is not set.
 	 * @see setBackend(Backend)
 	 */
 	private void updateFromBackend() throws IllegalStateException {
 		if (backend == null)
 			throw new IllegalStateException();
 		
 		Activity activity = getSherlockActivity();
 		// Name header
 		String name = backend.getUserName();
 		TextView nameHeading = (TextView) activity.findViewById(R.id.name_heading);
 		nameHeading.setText(name);
 		// Current debt
 		int debt = backend.getUserDebt();
 		TextView debtView = (TextView) activity.findViewById(R.id.debt_view);
 		debtView.setText(String.format(getString(R.string.debt_view_text), debt));
 		
 		// The lists take some time so let's use Callback.
 		// TODO Loading spinner
 		Callback loansCallback = new Callback() {
 			@Override public void handleMessage(Message msg) {
 				ProfileFragment.this.loansUpdateDone(msg);
 			}
 		};
 		backend.fetchLoans(loansCallback);
 		
 		Callback reservationsCallback = new Callback() {
 			@Override public void handleMessage(Message msg) {
 				ProfileFragment.this.reservationsUpdateDone(msg);
 			}
 		};
 		backend.fetchReservations(reservationsCallback);
 	}
 	
 	private void loansUpdateDone(Message msg) {
 		Activity activity = getSherlockActivity();
 		try {
 			@SuppressWarnings("unchecked")
 			List<Book> loans = (List<Book>) msg.obj;
 			ListView loansList = (ListView) activity.findViewById(R.id.loans_list);
			loansList.setAdapter(new BookListAdapter(activity, loans, false));
 		} catch (ClassCastException e) {
 			Toast.makeText(activity, R.string.loans_list_error, Toast.LENGTH_SHORT).show();
 		}
 	}
 	
 	private void reservationsUpdateDone(Message msg) {
 		Activity activity = getSherlockActivity();
 		try {
 			@SuppressWarnings("unchecked")
 			List<Book> reservations = (List<Book>) msg.obj;
 			ListView reservationsList = (ListView) activity.findViewById(R.id.reservations_list);
			reservationsList.setAdapter(new BookListAdapter(activity, reservations, false));
 		} catch (ClassCastException e) {
 			Toast.makeText(activity, R.string.reservations_list_error, Toast.LENGTH_SHORT).show();
 		}
 		
 	}
 	
 }
