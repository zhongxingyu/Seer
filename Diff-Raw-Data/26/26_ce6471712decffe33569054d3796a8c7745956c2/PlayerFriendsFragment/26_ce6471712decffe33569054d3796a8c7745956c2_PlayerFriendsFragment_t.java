 package wei.mark.pingpongboss.fragment;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 
 import wei.mark.pingpongboss.Pingpongboss;
 import wei.mark.pingpongboss.R;
 import wei.mark.pingpongboss.activity.MainViewPagerActivity;
 import wei.mark.pingpongboss.misc.adapter.FriendModelAdapter;
 import wei.mark.pingpongboss.misc.adapter.MainFragmentAdapter;
 import wei.mark.pingpongboss.misc.model.FriendModel;
 import wei.mark.pingpongboss.misc.task.FriendsTask;
 import wei.mark.pingpongboss.misc.task.LinkTask;
 import wei.mark.pingpongboss.misc.task.FriendsTask.FriendsCallback;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 
 import com.facebook.android.AsyncFacebookRunner;
 import com.facebook.android.AsyncFacebookRunner.RequestListener;
 import com.facebook.android.FacebookError;
 
 public class PlayerFriendsFragment extends ListFragment implements
 		FriendsCallback {
 	public static final String TAG = "FragmentPlayerFriend";
 
 	Pingpongboss app;
 
 	ArrayList<FriendModel> mFriends;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		app = (Pingpongboss) getActivity().getApplication();
 
 		mFriends = new ArrayList<FriendModel>();
 
 		setListAdapter(new FriendModelAdapter(getActivity(),
 				R.layout.item_player_friends, mFriends));
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.fragment_player_friends, null,
 				false);
 
 		final ProgressBar progress = (ProgressBar) view
 				.findViewById(R.id.progress);
 
 		final View login = (View) view.findViewById(R.id.login);
 		login.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				ProgressBar progress = (ProgressBar) getView().findViewById(
 						R.id.progress);
 				progress.setVisibility(View.VISIBLE);
 				View login = (View) getView().findViewById(R.id.login);
 				login.setVisibility(View.GONE);
 				View search = getView().findViewById(R.id.search);
 				search.setVisibility(View.GONE);
 
 				app.login(getActivity(), false, new Runnable() {
 
 					@Override
 					public void run() {
 						retrieveFriends();
 					}
 				}, new Runnable() {
 
 					@Override
 					public void run() {
 						fail(null);
 					}
 				});
 			}
 		});
 
 		final View logout = (View) view.findViewById(R.id.logout);
 		logout.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				try {
 					progress.setVisibility(View.VISIBLE);
 					logout.setVisibility(View.GONE);
 					ImageView arrow = (ImageView) getView().findViewById(
 							R.id.arrow);
 					arrow.setVisibility(View.GONE);
 					View tutorial = getView().findViewById(
 							R.id.facebook_tutorial);
 					tutorial.setVisibility(View.GONE);
 
 					new AsyncFacebookRunner(app.facebook).logout(getActivity(),
 							new RequestListener() {
 
 								@Override
 								public void onMalformedURLException(
 										MalformedURLException e, Object state) {
 									fail(e);
 								}
 
 								@Override
 								public void onIOException(IOException e,
 										Object state) {
 									fail(e);
 								}
 
 								@Override
 								public void onFileNotFoundException(
 										FileNotFoundException e, Object state) {
 									fail(e);
 								}
 
 								@Override
 								public void onFacebookError(FacebookError e,
 										Object state) {
 									fail(e);
 								}
 
 								@Override
 								public void onComplete(String response,
 										Object state) {
 									SharedPreferences facebookPrefs = getActivity()
 											.getSharedPreferences("facebook",
 													Context.MODE_PRIVATE);
 									SharedPreferences.Editor editor = facebookPrefs
 											.edit();
 									editor.remove("access_token");
 									editor.remove("access_expires");
 									editor.remove("facebookId");
 									editor.commit();
 
 									PlayerFriendsFragment.this.getActivity()
 											.runOnUiThread(new Runnable() {
 
 												@Override
 												public void run() {
 													FriendModelAdapter listAdapter = ((FriendModelAdapter) getListAdapter());
 													listAdapter.getLoader()
 															.clearCache();
 													listAdapter.clear();
 													listAdapter
 															.notifyDataSetChanged();
 
 													progress.setVisibility(View.GONE);
 													login.setVisibility(View.VISIBLE);
 												}
 											});
 								}
 							});
 				} catch (Exception e) {
 					fail(e);
 					e.printStackTrace();
 				}
 			}
 		});
 
 		ViewGroup facebookLayout = (ViewGroup) view
 				.findViewById(R.id.facebook_layout);
 		facebookLayout.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (logout.getVisibility() == View.VISIBLE)
 					logout.setVisibility(View.GONE);
 				else if (login.getVisibility() == View.GONE
 						&& progress.getVisibility() == View.GONE)
 					logout.setVisibility(View.VISIBLE);
 			}
 		});
 
 		EditText search = (EditText) view.findViewById(R.id.search);
 		search.addTextChangedListener(new TextWatcher() {
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 			}
 
 			@Override
 			public void afterTextChanged(Editable s) {
 				((FriendModelAdapter) getListAdapter()).getFilter().filter(s);
 			}
 		});
 		search.setVisibility(View.GONE);
 
 		View tutorial = view.findViewById(R.id.facebook_tutorial);
 		tutorial.setVisibility(View.GONE);
 
 		View link = view.findViewById(R.id.link);
 		link.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (app.facebookId != null && !app.facebookId.equals("")) {
 					EditText usatt = (EditText) getView().findViewById(
 							R.id.usatt);
 					EditText rc = (EditText) getView().findViewById(R.id.rc);
 					String usattId = usatt.getText().toString();
 					String rcId = rc.getText().toString();
 
 					if (!usattId.equals(""))
 						new LinkTask().execute(app.getDeviceId(), usattId,
 								"usatt", app.facebookId, app.facebookId);
 
 					if (!rcId.equals(""))
 						new LinkTask().execute(app.getDeviceId(), rcId, "rc",
 								app.facebookId, app.facebookId);
 				}
 			}
 		});
 
 		return view;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
		getListView().setVisibility(View.VISIBLE);

 		try {
 			((MainViewPagerActivity) getActivity()).getViewPagerListener()
 					.addOnPageChangeListener(new OnPageChangeListener() {
 
 						@Override
 						public void onPageSelected(int position) {
 							if (TAG.equals(MainFragmentAdapter
 									.getFragmentPosition().get(position))) {
 								pageSelected();
 							}
 						}
 
 						@Override
 						public void onPageScrolled(int arg0, float arg1,
 								int arg2) {
 						}
 
 						@Override
 						public void onPageScrollStateChanged(int arg0) {
 						}
 					});
 			if (TAG.equals(MainFragmentAdapter.getFragmentPosition().get(
 					app.CurrentMainViewPagerPosition)))
 				pageSelected();
 		} catch (Exception e) {
 			fail(e);
 		}
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 
 		if (app.friendsTask != null) {
 			app.friendsTask.setFriendsCallback(null);
 		}
 	}
 
 	@Override
 	public void onLowMemory() {
 		((FriendModelAdapter) getListAdapter()).getLoader().clearCache();
 
 		super.onLowMemory();
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		FriendModel profile = ((FriendModelAdapter) getListAdapter())
 				.getFriendModel(position);
 		// TODO
 		Log.d(TAG, String.format("Profile selected: %s", profile.getName()));
 	}
 
 	private void pageSelected() {
 		View search = getView().findViewById(R.id.search);
 		search.setVisibility(View.GONE);
 
 		app.login(getActivity(), true, new Runnable() {
 
 			@Override
 			public void run() {
 				retrieveFriends();
 			}
 		}, new Runnable() {
 
 			@Override
 			public void run() {
 				fail(null);
 			}
 		});
 	}
 
 	private void retrieveFriends() {
 		if (app.facebookId == null) {
 			fail("retriveFriends facebookId is null");
 			return;
 		}
 
 		ProgressBar progress = (ProgressBar) getView().findViewById(
 				R.id.progress);
 		progress.setVisibility(View.VISIBLE);
 		View login = (View) getView().findViewById(R.id.login);
 		login.setVisibility(View.GONE);
 
 		mFriends.clear();
 		((ArrayAdapter<?>) getListAdapter()).notifyDataSetChanged();
 
 		if (app.friendsTask == null) {
 			app.friendsTask = new FriendsTask(this);
 			app.friendsTask.execute(app.getDeviceId(), app.facebookId,
 					app.facebook.getAccessToken(), "true");
 		} else if (!app.facebookId.equals(app.friendsTask.getFacebookId())) {
 			app.friendsTask.cancel(true);
 			app.friendsTask = new FriendsTask(this);
 			app.friendsTask.execute(app.getDeviceId(), app.facebookId,
 					app.facebook.getAccessToken(), "true");
 		}
 
 		app.friendsTask.setFriendsCallback(this);
 	}
 
 	@Override
 	public void friendsCompleted(ArrayList<FriendModel> friends) {
 		app.friendsTask = null;
 
 		final ProgressBar progress = (ProgressBar) getView().findViewById(
 				R.id.progress);
 		progress.setVisibility(View.GONE);
 		View login = (View) getView().findViewById(R.id.login);
 		login.setVisibility(View.GONE);
 		ImageView arrow = (ImageView) getView().findViewById(R.id.arrow);
 		arrow.setVisibility(View.VISIBLE);
 		View tutorial = getView().findViewById(R.id.facebook_tutorial);
 
 		mFriends.clear();
 		if (friends != null) {
 			if (friends.isEmpty()) {
 				// user did not link his profile
 				tutorial.setVisibility(View.VISIBLE);
 			} else {
 				View search = getView().findViewById(R.id.search);
 				search.setVisibility(View.VISIBLE);
 
 				mFriends.addAll(friends);
 			}
 			((ArrayAdapter<?>) getListAdapter()).notifyDataSetChanged();
 		} else
 			fail("friendsCompleted friends is null");
 	}
 
 	private void fail(Object error) {
 		ProgressBar progress = (ProgressBar) getView().findViewById(
 				R.id.progress);
 		progress.setVisibility(View.GONE);
 		View login = (View) getView().findViewById(R.id.login);
 		login.setVisibility(View.VISIBLE);
 	}
 }
