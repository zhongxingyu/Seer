 package whipkey.stemesteem.components;
 
 //Copyright 2011, Zackary Whipkey
 //This program/source is distributed under the terms of the GNU General Public License.
 //Please read the License.txt file for more information.
 
 import whipkey.stemesteem.R;
 import whipkey.stemesteem.main.StemEsteemApplication;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.database.Cursor;
 import android.os.Bundle;
import android.support.v4.app.Fragment;
 import android.support.v4.app.ListFragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import android.view.View.OnClickListener;
 
 /*
  * EndingListFragment holds the listview that shows the endings for the selected stem.
  * It also issues dialog callbacks.
  */
 public class EndingListFragment extends ListFragment {
 	DialogItemClickListener dialogClickListener;
 
 	private Intent i; // intent given from StemEsteem.
 	private Cursor c;
 	private Button addEndingButton;
 	private Bundle b; // bundle for required attributes in ending functions. Is
 						// provided by a intent or manually via setBundle in
 						// StemEsteem.
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		try {
 			dialogClickListener = (DialogItemClickListener) activity;
 			if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) < Configuration.SCREENLAYOUT_SIZE_LARGE) {
 				i = activity.getIntent();
 				b = i.getExtras();
 			} // the above is skipped if we are on a large/xlarge device,
 				// because we have no intent (one activity, not two).
 
 		} catch (ClassCastException e) {
 			throw new ClassCastException(activity.toString()
 					+ "needs to implement DialogClickListener");
 		}
 	}
 
 	// Ask for dialog, parent activity must implement this interface.
 	public interface DialogItemClickListener {
 		public void dialogItemClick(Bundle b, int dialogId);
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		c.moveToPosition(position);
 		Bundle b = new Bundle();
 		b.putInt("_id", c.getInt(c.getColumnIndex("_id")));
 		b.putString("ending", c.getString(c.getColumnIndex("ending")));
		b.putString("stem", this.b.getString("stem"));
 		dialogClickListener.dialogItemClick(b, 0); // An ending was clicked, ask
 													// for a EditEndingDialog.
 
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.endinglistfragment, container);
 
 		return v;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
		if ((getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) < Configuration.SCREENLAYOUT_SIZE_LARGE) { // checks for large/xlarge screen.
 			fillData();
 		} // if we are on a large/xlarge screen device, skip the above and wait
 			// for StemEsteem to call for fillData().
 		addEndingButton = (Button) getView().findViewById(R.id.addEndingButton);
 		addEndingButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
				if(b != null){ // make sure a bundle existant.
 				dialogClickListener.dialogItemClick(b, 1); // our add ending
 															// button was
				}											// clicked, ask for
 															// a
 															// AddEndingDialog.
 			}
 		});
 
 	}
 
 	public void fillData() { // refresh our listview.
 		TextView stemText = (TextView) getView()
 				.findViewById(R.id.stemTextView);
 		stemText.setText(b.getString("stem") + ":");
 
 		c = StemEsteemApplication.getDbHelper().getAllEndings(
 				b.getInt("weekNo"), b.getInt("stemNo"));
 
 		getActivity().startManagingCursor(c);
 		String[] from = new String[] { "ending" };
 		int[] to = new int[] { R.id.endingListText };
 
 		setListAdapter(new SimpleCursorAdapter(getActivity(),
 				R.layout.endinglist, c, from, to));
 	}
 
 	public Bundle getB() {
 		return b;
 	}
 
 	public void setB(Bundle b) {
 		this.b = b;
 	}
 
 	// b.putInt("weekNo", i.getExtras().getInt("weekNo"));
 	// b.putInt("stemNo", i.getExtras().getInt("stemNo"));
 
 }
