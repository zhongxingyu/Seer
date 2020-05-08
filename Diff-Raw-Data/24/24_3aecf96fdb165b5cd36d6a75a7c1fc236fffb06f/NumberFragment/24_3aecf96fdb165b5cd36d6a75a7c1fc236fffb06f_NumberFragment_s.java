 package uk.co.jwlawson.nof1.fragments;
 
 import uk.co.jwlawson.nof1.R;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 public class NumberFragment extends SherlockFragment {
 
 	private static int COUNT = 0;
 	private String mQuestion;
 	private EditText mText;
 	private String mNumber;
 	private int mId;
 
 	public NumberFragment() {
 		mId = COUNT;
 		COUNT++;
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		COUNT--;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
		View view = inflater.inflate(R.layout.row_layout_data_number, container);
 
 		TextView text = (TextView) view.findViewById(R.id.data_input_number_text);
 		text.setText(mQuestion);
 
 		mText = (EditText) view.findViewById(R.id.data_input_number_edittext);
 		if (mNumber != null) mText.setText(mNumber);
 
 		return view;
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putString("NumFrag" + mId + "Question", mQuestion);
 		outState.putString("NumFrag" + mId + "Number", mNumber);
 	}
 
 	@Override
 	public void setArguments(Bundle args) {
 		super.setArguments(args);
 		if (args != null) {
 			mQuestion = args.getString("NumFrag" + mId + "Question");
 			mNumber = args.getString("NumFrag" + mId + "Number");
 		}
 	}
 }
