 package fr.utc.assos.uvweb;
 
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ListView;
 import com.actionbarsherlock.app.SherlockFragment;
 
 import static fr.utc.assos.uvweb.util.LogUtils.makeLogTag;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Thomas
  * Date: 08/04/13
  * Time: 17:59
  * To change this template use File | Settings | File Templates.
  */
 public class WrapperFragment extends SherlockFragment {
 	private static final String TAG = makeLogTag(WrapperFragment.class);
 	private UVListFragment mUvListFragment;
 
 	public WrapperFragment() {
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		//setHasOptionsMenu(true); // Workaround for https://code.google.com/p/android/issues/detail?id=45722
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.fragment_wrapper,
 				container, false);
 	}
 
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) {
 		super.onViewCreated(view, savedInstanceState);
 
 		// Restore the previously serialized activated item position and search query.
 		if (savedInstanceState != null) {
 			if (savedInstanceState.containsKey(UVListFragment.STATE_ACTIVATED_POSITION)) {
 				mUvListFragment = UVListFragment.newInstance(
 						savedInstanceState.getInt(UVListFragment.STATE_ACTIVATED_POSITION),
 						savedInstanceState.getString(UVListFragment.STATE_SEARCH_QUERY));
 			} else {
 				mUvListFragment = UVListFragment.newInstance(
 						ListView.INVALID_POSITION,
 						savedInstanceState.getString(UVListFragment.STATE_SEARCH_QUERY));
 			}
 		}
 		else {
 			mUvListFragment = UVListFragment.newInstance(ListView.INVALID_POSITION, null);
 		}
 
 		mUvListFragment.setIsTwoPane(true);
 
 		getChildFragmentManager()
 				.beginTransaction()
 				.replace(R.id.uv_list, mUvListFragment)
 				.commit();
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		super.onSaveInstanceState(outState);
 
 		final int activatedPosition = mUvListFragment.getActivatedPosition();
 		if (activatedPosition != ListView.INVALID_POSITION) {
 			// Serialize and persist the activated item position.
 			outState.putInt(UVListFragment.STATE_ACTIVATED_POSITION, activatedPosition);
 		}
		final String searchQuery = mUvListFragment.getSearchQuery();
 		if (searchQuery != null) {
 			// Serialize and persist the search query.
			outState.putString(UVListFragment.STATE_SEARCH_QUERY, searchQuery);
 		}
 	}
 }
