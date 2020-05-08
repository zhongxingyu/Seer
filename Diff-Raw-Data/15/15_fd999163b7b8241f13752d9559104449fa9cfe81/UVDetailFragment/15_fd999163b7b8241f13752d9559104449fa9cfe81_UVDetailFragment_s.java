 package fr.utc.assos.uvweb;
 
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.TextView;
 import com.actionbarsherlock.app.SherlockFragment;
 import fr.utc.assos.uvweb.data.UVwebContent;
 
 /**
  * A fragment representing a single UV detail screen. This fragment is either
  * contained in a {@link UVListActivity} in two-pane mode (on tablets) or a
  * {@link UVDetailActivity} on handsets.
  */
 public class UVDetailFragment extends SherlockFragment {
 	/**
 	 * The fragment argument representing the UV ID that this fragment
 	 * represents.
 	 */
 	public static final String ARG_UV_ID = "item_id";
 
 	/**
 	 * The UV this fragment is presenting.
 	 */
 	private UVwebContent.UV mItem;
 
 	/**
 	 * Mandatory empty constructor for the fragment manager to instantiate the
 	 * fragment (e.g. upon screen orientation changes).
 	 */
 	public UVDetailFragment() {
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		if (getArguments().containsKey(ARG_UV_ID)) {
 			// Load the UV specified by the fragment
 			// arguments. In a real-world scenario, use a Loader
 			// to load content from a content provider.
 			mItem = UVwebContent.UV_MAP.get(getArguments().getString(
 					ARG_UV_ID));
 		}
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View rootView = inflater.inflate(R.layout.fragment_uv_detail,
 				container, false);
 
 		// Show the UV as text in a TextView.
 		if (mItem != null) {
			((TextView) rootView.findViewById(R.id.uv_detail))
 					.setText(mItem.getName());
 		}
 
 		return rootView;
 	}
 }
