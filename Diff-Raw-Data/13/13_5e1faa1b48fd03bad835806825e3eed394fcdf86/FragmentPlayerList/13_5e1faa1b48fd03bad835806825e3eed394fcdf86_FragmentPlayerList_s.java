 package wei.mark.tabletennis;
 
 import java.util.ArrayList;
 
 import wei.mark.tabletennis.TableTennisRatings.Navigation;
 import wei.mark.tabletennis.model.PlayerModel;
 import wei.mark.tabletennis.util.Debuggable;
 import wei.mark.tabletennis.util.ProviderSearchTask;
 import wei.mark.tabletennis.util.SearchCallback;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class FragmentPlayerList extends ListFragment implements SearchCallback {
 	TableTennisRatings app;
 	ArrayList<PlayerModel> mPlayers;
 	String mProvider, mQuery;
 	int mListIndex, mListTop;
 	boolean mUserScroll, mUser;
 
 	public static FragmentPlayerList getInstance(String provider, String query,
 			boolean user) {
 		FragmentPlayerList fragment = new FragmentPlayerList();
 		Bundle b = new Bundle();
 		b.putString("provider", provider);
 		b.putString("query", query);
 		b.putBoolean("user", user);
 		fragment.setArguments(b);
 
 		return fragment;
 	}
 
 	public FragmentPlayerList() {
 		mPlayers = new ArrayList<PlayerModel>();
 		mProvider = null;
 		mQuery = null;
 		mListIndex = -1;
 		mListTop = 0;
 		mUserScroll = false;
 		mUser = false;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		app = (TableTennisRatings) getActivity().getApplication();
 
 		Bundle b = getArguments();
 
 		mProvider = b.getString("provider");
 		mQuery = b.getString("query");
 		mUser = b.getBoolean("user");
 
 		setListAdapter(new ArrayAdapter<PlayerModel>(getActivity(),
 				android.R.layout.simple_list_item_1, mPlayers));
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.fragment_player_list, container,
 				false);
 
 		TextView titleTextView = (TextView) v.findViewById(R.id.title);
 		String providerName = null;
 		if ("rc".equals(mProvider))
 			providerName = "Ratings Central";
 		else if ("usatt".equals(mProvider))
 			providerName = "USATT";
 		if (providerName != null && mQuery != null)
 			titleTextView.setText(String.format("%s search: %s", providerName,
 					mQuery));
 
 		v.findViewById(R.id.home).setVisibility(View.GONE);
 		Button logoButton = (Button) v.findViewById(R.id.logo);
 		if (!app.DualPane)
 			logoButton.setVisibility(View.GONE);
 		else if ("rc".equals(mProvider))
 			logoButton.setBackgroundResource(R.drawable.rc_selector);
 		else if ("usatt".equals(mProvider))
 			logoButton.setBackgroundResource(R.drawable.usatt_selector);
 
 		ListFragmentTouchListener l = new ListFragmentTouchListener();
 		v.setOnTouchListener(l);
 		v.findViewById(android.R.id.list).setOnTouchListener(l);
 
 		return v;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		if ("usatt".equals(mProvider)) {
 			if (app.usattSearchTask == null) {
 				app.usattSearchTask = new ProviderSearchTask(this);
 				app.usattSearchTask.execute(app.getDeviceId(), mProvider,
 						mQuery, String.valueOf(mUser));
 			}
 			app.usattSearchTask.setSearchCallback(this);
 		} else if ("rc".equals(mProvider)) {
 			if (app.rcSearchTask == null) {
 				app.rcSearchTask = new ProviderSearchTask(this);
 				app.rcSearchTask.execute(app.getDeviceId(), mProvider, mQuery,
 						String.valueOf(mUser));
 			}
 			app.rcSearchTask.setSearchCallback(this);
 		}
 
 		// SharedPreferences prefs = getActivity().getSharedPreferences(
 		// "listScroll", 0);
 		//
 		// if (prefs.getString("listQuery", "").equals(mQuery)) {
 		// int index = prefs.getInt("listIndex", 0);
 		// int top = prefs.getInt("listTop", 0);
 		// getListView().setSelectionFromTop(index, top);
 		// }
 		//
 		// getListView().setOnTouchListener(new OnTouchListener() {
 		//
 		// @Override
 		// public boolean onTouch(View v, MotionEvent event) {
 		// mUserScroll = true;
 		// return false;
 		// }
 		// });
 		//
 		// getListView().setOnScrollListener(new OnScrollListener() {
 		//
 		// @Override
 		// public void onScrollStateChanged(AbsListView view, int scrollState) {
 		// }
 		//
 		// @Override
 		// public void onScroll(AbsListView view, int firstVisibleItem,
 		// int visibleItemCount, int totalItemCount) {
 		// if (mUserScroll) {
 		// mListIndex = firstVisibleItem;
 		// View firstView = view.getChildAt(0);
 		// mListTop = firstView == null ? 0 : firstView.getTop();
 		// }
 		// }
 		// });
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 
 		// mUserScroll = false;
 		//
 		// if (mListIndex != -1) {
 		// Editor edit = getActivity().getSharedPreferences("listScroll", 0)
 		// .edit();
 		// edit.putString("listQuery", mQuery);
 		// edit.putInt("listIndex", mListIndex);
 		// edit.putInt("listTop", mListTop);
 		//
 		// edit.commit();
 		// }
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		// TODO
 		Toast.makeText(getActivity(),
 				String.format("Clicked %s", mPlayers.get(position)),
 				Toast.LENGTH_SHORT).show();
 	}
 
 	private void debug(String msg) {
 		((Debuggable) getActivity()).debug(msg);
 	}
 
 	private class ListFragmentTouchListener implements View.OnTouchListener {
 
 		@Override
 		public boolean onTouch(View v, MotionEvent event) {
 			app.CurrentNavigation = Navigation.LIST;
 			debug("Current navigation is now "
 					+ app.CurrentNavigation.toString());
 			return false;
 		}
 
 	}
 
 	@Override
 	public void searchCompleted(ArrayList<PlayerModel> players) {
 		TextView text = null;
 		try {
 			text = (TextView) getView().findViewById(R.id.emptyListText);
 			text.setVisibility(View.VISIBLE);
 			getView().findViewById(R.id.progress).setVisibility(View.GONE);
			getView().findViewById(R.id.retry).setVisibility(View.VISIBLE);
 		} catch (Exception ex) {
 		}
 
 		if (players != null)
 			mPlayers.addAll(players);
		else if (text != null)
 			text.setText("An error occurred");
 
 		((ArrayAdapter<?>) getListAdapter()).notifyDataSetChanged();
 
 		if ("usatt".equals(mProvider)) {
 			app.usattSearchTask = null;
 		} else if ("rc".equals(mProvider)) {
 			app.rcSearchTask = null;
 		}
 	}
 }
