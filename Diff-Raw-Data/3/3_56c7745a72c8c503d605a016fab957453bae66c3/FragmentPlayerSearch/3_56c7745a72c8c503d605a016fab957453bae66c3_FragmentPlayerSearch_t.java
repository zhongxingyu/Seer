 package wei.mark.tabletennis;
 
 import java.util.ArrayList;
 
 import wei.mark.tabletennis.TableTennisRatings.Navigation;
 import wei.mark.tabletennis.util.AppEngineParser;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.app.ListFragment;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class FragmentPlayerSearch extends ListFragment {
 	TableTennisRatings app;
 
 	int mListIndex, mListTop;
 
 	ArrayList<String> mHistory;
 	String mQuery, mPreviousInput;
 	boolean mUserChangedScroll;
 
 	EditText searchInput;
 	ImageButton searchButton;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		app = (TableTennisRatings) getActivity().getApplication();
 
 		mHistory = retrieveHistory();
 
 		SharedPreferences prefs = getActivity().getSharedPreferences("search",
 				0);
 		mQuery = prefs.getString("query", null);
 		mPreviousInput = prefs.getString("input", null);
 		mListIndex = prefs.getInt("listIndex", 0);
 		mListTop = prefs.getInt("listTop", 0);
 
 		setListAdapter(new ArrayAdapter<String>(getActivity(),
 				android.R.layout.simple_list_item_1, mHistory));
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.fragment_player_search, null,
 				false);
 
 		((TextView) view.findViewById(R.id.title))
 				.setText("Table Tennis Ratings");
 		view.findViewById(R.id.provider_logo).setVisibility(View.GONE);
 
 		searchInput = (EditText) view.findViewById(R.id.searchEditText);
 		if (mPreviousInput != null) {
 			searchInput.setText(mPreviousInput);
 		}
 
 		searchInput.setOnKeyListener(new OnKeyListener() {
 
 			@Override
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				if (event.getAction() == KeyEvent.ACTION_UP
 						&& keyCode == KeyEvent.KEYCODE_ENTER) {
 					search(((EditText) v).getText().toString(), true);
 				}
 				return false;
 			}
 		});
 
 		searchButton = (ImageButton) view.findViewById(R.id.searchButton);
 		searchButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				search(searchInput.getText().toString(), true);
 			}
 		});
 
 		OnTouchListener l = new OnTouchListener() {
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				updateCurrentNavigation();
 				return false;
 			}
 		};
 
 		view.setOnTouchListener(l);
 		searchInput.setOnTouchListener(l);
 		searchButton.setOnTouchListener(l);
 
 		return view;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		getListView().setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> l, View v, int position,
 					long id) {
 				String query = mHistory.get(position);
 				searchInput.setText(query);
 				search(query, true);
 			}
 		});
 
 		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
 
 			@Override
 			public boolean onItemLongClick(AdapterView<?> l, View v,
 					int position, long id) {
 				removeFromHistory(mHistory.get(position));
 				return true;
 			}
 		});
 
 		getListView().setOnTouchListener(new OnTouchListener() {
 
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				mUserChangedScroll = true;
 				updateCurrentNavigation();
 				return false;
 			}
 		});
 
 		getListView().setSelectionFromTop(mListIndex, mListTop);
 
 		View contentFrame = getActivity().findViewById(R.id.content);
 		app.DualPane = contentFrame != null
 				&& contentFrame.getVisibility() == View.VISIBLE;
 
 		if (app.DualPane) {
 			getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 
 			searchInput.setBackgroundResource(R.drawable.white_selector);
 			searchButton.setVisibility(View.GONE);
 
 			getView().findViewById(R.id.logo).setBackgroundResource(
 					R.drawable.logo_small_selector);
 		}
 
 		if (app.CurrentNavigation == Navigation.LIST || app.DualPane) {
 			boolean screenOrientationChange = app.DualPane
 					&& app.CurrentNavigation != Navigation.LIST;
 			search(mQuery, !screenOrientationChange);
 		}
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 
 		SharedPreferences prefs = getActivity().getSharedPreferences("search",
 				0);
 		Editor editor = prefs.edit();
 
 		// save ListView scroll position
 		if (mUserChangedScroll) {
 			editor.putInt("listIndex", getListView().getFirstVisiblePosition());
 			View rcv = getListView().getChildAt(0);
 			editor.putInt("listTop", rcv == null ? 0 : rcv.getTop());
 		}
 
 		editor.putString("query", mQuery);
 		editor.putString("input", searchInput.getText().toString());
 
 		editor.commit();
 
 		// save search history
 		saveHistory();
 	}
 
 	@Override
 	public void onLowMemory() {
 		super.onLowMemory();
 		mQuery = null;
 		AppEngineParser.getParser().onLowMemory();
 	}
 
 	private ArrayList<String> retrieveHistory() {
 		ArrayList<String> history = new ArrayList<String>();
 		SharedPreferences prefs = getActivity().getSharedPreferences("history",
 				0);
 
 		int i = 0;
 		String q;
 
 		while ((q = prefs.getString(String.valueOf(i++), null)) != null) {
 			history.add(q);
 		}
 
 		if (history.isEmpty()) {
 			history.add("Wei, Mark");
 			history.add("Boll, Timo");
 		}
 
 		return history;
 	}
 
 	private void saveHistory() {
 		SharedPreferences prefs = getActivity().getSharedPreferences("history",
 				0);
 		Editor editor = prefs.edit();
 
 		editor.clear();
 
 		int i = 0;
 		for (String q : mHistory) {
 			editor.putString(String.valueOf(i++), q);
 		}
 
 		editor.commit();
 	}
 
 	private void removeFromHistory(final String item) {
 		new AlertDialog.Builder(getActivity())
 				.setTitle(String.format("Remove %s from History?", item))
 				.setPositiveButton("Remove",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								mHistory.remove(item);
 								((ArrayAdapter<?>) getListAdapter())
 										.notifyDataSetChanged();
 								saveHistory();
 							}
 						})
 				.setNeutralButton("Clear All",
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								mHistory.clear();
 								((ArrayAdapter<?>) getListAdapter())
 										.notifyDataSetChanged();
 								saveHistory();
 							}
 						}).setNegativeButton("Cancel", null).show();
 	}
 
 	protected void search(String query, boolean user) {
 		// InputMethodManager imm = (InputMethodManager) getActivity()
 		// .getSystemService(Context.INPUT_METHOD_SERVICE);
 		// imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
 
 		mQuery = query;
 
 		if (mQuery == null) {
 			return;
 		} else {
 			mHistory.remove(mQuery);
 			mHistory.add(0, mQuery);
 			((ArrayAdapter<?>) getListAdapter()).notifyDataSetChanged();
 
 			saveHistory();
 
 			int position = mHistory.indexOf(mQuery);
 			getListView().setItemChecked(position, true);
 		}
 
 		if (app.DualPane) {
 			FragmentPlayerList usattFragment = FragmentPlayerList.getInstance(
 					"usatt", query, user);
 			FragmentPlayerList rcFragment = FragmentPlayerList.getInstance(
 					"rc", query, user);
 
 			getFragmentManager().beginTransaction()
 					.replace(R.id.content_usatt, usattFragment)
 					.replace(R.id.content_rc, rcFragment)
 					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
 					.commit();
 		} else {
 			Intent intent = new Intent();
 			intent.setClass(getActivity(), ActivityTabbedPlayerList.class);
 			intent.putExtra("query", query);
 			intent.putExtra("user", user);
 			startActivity(intent);
 		}
 
		if (user)
			app.CurrentNavigation = Navigation.LIST;
 	}
 
 	public void clearQuery() {
 		mQuery = null;
 	}
 
 	private void updateCurrentNavigation() {
 		app.CurrentNavigation = Navigation.IDLE;
 	}
 }
