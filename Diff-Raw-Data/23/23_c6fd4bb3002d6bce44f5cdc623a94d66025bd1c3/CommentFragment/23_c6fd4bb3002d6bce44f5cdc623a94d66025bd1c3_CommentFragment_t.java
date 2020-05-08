 package uk.co.jwlawson.nof1.fragments;
 
import uk.co.jwlawson.nof1.BuildConfig;
 import uk.co.jwlawson.nof1.R;
 import android.os.Bundle;
import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.EditText;
 
 import com.actionbarsherlock.app.SherlockFragment;
 
 public class CommentFragment extends SherlockFragment {
 
	private static final String TAG = "CommentFragment";
 	private static int COUNT;
 	private String mComment;
 	private EditText mText;
	private boolean init = false;
 	private int mId;
 
 	public CommentFragment() {
 		mId = COUNT;
 		COUNT++;
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		COUNT--;
 		// Bit of a hack, but assume all fragments are destroyed at once eg.
 		// screen rotation. This means they all get initialised correctly.
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		if (!init) mySetArguments(savedInstanceState);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 
		if (!init) mySetArguments(savedInstanceState);

		View view = inflater.inflate(R.layout.row_layout_data_comment, container, false);
 
 		mText = (EditText) view.findViewById(R.id.data_input_comment_edittext);
 		if (mComment != null) mText.setText(mComment);
 
		if (BuildConfig.DEBUG) Log.d(TAG, mId + " view created");
 		return view;
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 		outState.putString("CommentFrag" + mId + "Comment", mComment);
 	}
 
 	@Override
 	public void setArguments(Bundle args) {
 		super.setArguments(args);
		mySetArguments(args);
	}

	private void mySetArguments(Bundle args) {
 		if (args != null && args.containsKey("CommentFrag" + mId + "Comment")) {
 			mComment = args.getString("CommentFrag" + mId + "Comment");
 		}
		init = true;
 	}
 }
