 package com.exam.slieer.ui.fragment;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 
 import com.exam.slieer.activities.DummyItemListActivity;
 import com.exam.slieer.data.DummyContent;
import com.example.helloworld.R;
 
 /**
  * A fragment representing a single Item detail screen. This fragment is either
  * contained in a {@link DummyItemListActivity} in two-pane mode (on tablets) or a
  * {@link ItemDetailActivity} on handsets.
  */
 public class DummyItemDetailFragment extends Fragment {
 	public static final String ARG_ITEM_ID = "item_id";
 
 	private DummyContent.DummyItem mItem;
 
 	public DummyItemDetailFragment() {
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		if (getArguments().containsKey(ARG_ITEM_ID)) {
 			// Load the dummy content specified by the fragment
 			// arguments. In a real-world scenario, use a Loader
 			// to load content from a content provider.
 			mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
 		}
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View rootView = inflater.inflate(R.layout.fragment_item_detail,container, false);
 
 		// Show the dummy content as text in a TextView.
 		if (mItem != null) {
 			((TextView) rootView.findViewById(R.id.item_detail))
 					.setText(mItem.content);
 		}
 
 		return rootView;
 	}
 }
