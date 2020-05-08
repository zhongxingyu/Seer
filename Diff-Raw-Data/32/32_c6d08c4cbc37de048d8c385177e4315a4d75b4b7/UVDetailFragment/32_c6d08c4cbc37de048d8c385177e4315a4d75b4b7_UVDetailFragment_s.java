 package fr.utc.assos.uvweb;
 
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.os.Handler;
 import android.text.Html;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewStub;
 import android.widget.ListView;
 import android.widget.TextView;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
 import de.keyboardsurfer.android.widget.crouton.Crouton;
 import fr.utc.assos.uvweb.adapters.UVCommentAdapter;
 import fr.utc.assos.uvweb.data.UVwebContent;
 import fr.utc.assos.uvweb.util.ConfigHelper;
 import fr.utc.assos.uvweb.util.ConnectionCheckerHelper;
 
 /**
  * A fragment representing a single UV detail screen. This fragment is either
  * contained in a {@link fr.utc.assos.uvweb.activities.UVListActivity} in two-pane mode (on tablets) or a
  * {@link fr.utc.assos.uvweb.activities.UVDetailActivity} on handsets.
  */
 public class UVDetailFragment extends SherlockFragment {
 	private static final String TAG = "UVDetailFragment";
 
 	/**
 	 * The fragment argument representing the UV ID that this fragment
 	 * represents.
 	 */
 	public static final String ARG_UV_ID = "item_id";
 
 	private final Handler mHandler = new Handler();
 	/**
 	 * The UV this fragment is presenting.
 	 */
 	private UVwebContent.UV mUV;
 	/**
 	 * The ListView containing all comment items.
 	 */
 	private ListView mListView;
 
 	/**
 	 * Mandatory empty constructor for the fragment manager to instantiate the
 	 * fragment (e.g. upon screen orientation changes).
 	 */
 	public UVDetailFragment() {
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		if (!ConfigHelper.hasSeveralFragmentConfigurations(getSherlockActivity(),
 				Configuration.ORIENTATION_LANDSCAPE)) {
 			// Workaround: we do not want to retain instance for a device like the Nexus 7,
 			// since in portrait mode, only the list is displayed.
 			// When switching from landscape to portrait on such a device, the framework will try to restore
 			// this fragment and fail if we setRetainInstance(true).
 			setRetainInstance(true);
 		}
 
 		final Bundle arguments = getArguments();
 		if (arguments.containsKey(ARG_UV_ID)) {
 			// Load the UV specified by the fragment
 			// arguments. In a real-world scenario, use a Loader
 			// to load content from a content provider.
 			mUV = UVwebContent.UV_MAP.get(arguments.getString(ARG_UV_ID));
 			getSherlockActivity().getSupportActionBar().setTitle(mUV.toString());
 
 			// Fragment configuration
 			setHasOptionsMenu(true);
 		}
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View rootView = inflater.inflate(R.layout.fragment_uv_detail,
 				container, false);
 
 		mListView = (ListView) rootView.findViewById(android.R.id.list);
 
 		final UVCommentAdapter adapter = new UVCommentAdapter(getSherlockActivity());
 
 		final SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(adapter);
 		swingBottomInAnimationAdapter.setListView(mListView);
 		adapter.updateComments(UVwebContent.COMMENTS);
 
 		// Show the UV as text in a TextView.
 		if (mUV != null) {
 			View headerView = rootView.findViewById(android.R.id.empty);
 			if (adapter.isEmpty()) {
 				ViewStub headerViewStub = (ViewStub) headerView;
 				headerViewStub.setOnInflateListener(new ViewStub.OnInflateListener() {
 					@Override
 					public void onInflate(ViewStub stub, View inflated) {
 						mListView.setEmptyView(inflated);
 						setHeaderData(inflated);
 					}
 				});
 				headerViewStub.inflate();
 			} else {
 				headerView = inflater.inflate(R.layout.uv_detail_header, null);
 				setHeaderData(headerView);
 				mListView.addHeaderView(headerView);
 			}
 		}
 
 		mListView.setAdapter(swingBottomInAnimationAdapter);
 
 		return rootView;
 	}
 
 	@Override
 	public void onDestroyView() {
 		// Resources cleanup
 		mListView = null;
 
 		super.onDestroyView();
 	}
 
 	private void setHeaderData(final View inflatedHeader) {
		((TextView) inflatedHeader.findViewById(R.id.uvcode)).setText(Html.fromHtml(String.format(
 				UVwebContent.UV_TITLE_FORMAT_LIGHT, mUV.getLetterCode(), mUV.getNumberCode())));
		((TextView) inflatedHeader.findViewById(R.id.desc)).setText(mUV.getDescription());
		((TextView) inflatedHeader.findViewById(R.id.rate)).setText(mUV.getFormattedRate());
 	}
 
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		inflater.inflate(R.menu.fragment_uv_detail, menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.menu_refresh:
 				final SherlockFragmentActivity context = getSherlockActivity();
 				if (!ConnectionCheckerHelper.isOnline(context)) {
 					Crouton.makeText(context, context.getString(R.string.network_error_message), ConnectionCheckerHelper.NETWORK_ERROR_STYLE).show();
 				} else {
 					final MenuItem refresh = item;
 					refresh.setActionView(R.layout.progressbar);
 					mHandler.postDelayed(new Runnable() {
 						@Override
 						public void run() {
 							refresh.setActionView(null);
 						}
 					}, 2000);
 				}
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 }
