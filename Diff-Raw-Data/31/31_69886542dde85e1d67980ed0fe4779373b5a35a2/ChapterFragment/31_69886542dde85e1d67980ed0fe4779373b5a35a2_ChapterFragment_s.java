 /**
  * 
  */
 package github.graded_reader;
 
 import android.app.Fragment;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 /**
  * @author smokey
  *
  */
 public class ChapterFragment extends Fragment {
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		Log.e("Test", "hello");
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.chapter, container, false);
 		return view;
 	}
 
 	public void setText(String item) {
 		TextView view = (TextView) getView().findViewById(R.id.chapterText);
 		view.setText(item);
 	}
 }
