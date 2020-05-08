 package wei.mark.pingpongboss;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 
 import org.json.JSONObject;
 
 import wei.mark.pingpongboss.model.FriendModel;
 import wei.mark.pingpongboss.util.FriendModelAdapter;
 import wei.mark.pingpongboss.util.FriendsTask;
 import wei.mark.pingpongboss.util.FriendsTask.FriendsCallback;
 import wei.mark.pingpongboss.util.MainFragmentAdapter;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 
 import com.facebook.android.AsyncFacebookRunner;
 import com.facebook.android.AsyncFacebookRunner.RequestListener;
 import com.facebook.android.DialogError;
 import com.facebook.android.Facebook.DialogListener;
 import com.facebook.android.FacebookError;
 
 public class FragmentPlayerFriends extends ListFragment implements
 		FriendsCallback {
 	public static final String TAG = "FragmentPlayerFriend";
 
 	PingPongBoss app;
 	SharedPreferences facebookPrefs;
 
 	ArrayList<FriendModel> mFriends;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		app = (PingPongBoss) getActivity().getApplication();
 
 		mFriends = new ArrayList<FriendModel>();
 
 		setListAdapter(new FriendModelAdapter(getActivity(),
 				R.layout.item_player_friends, mFriends));
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.fragment_player_friends, null,
 				false);
 
		final View login = (View) view.findViewById(R.id.login);
 		login.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (facebookAuthorizeCached())
 					retrieveFriends();
 				else
 					facebookAuthorizeOnline();
 			}
 		});
 
		final View logout = (View) view.findViewById(R.id.logout);
 		logout.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				try {
 					app.facebook.logout(getActivity());
 
 					SharedPreferences.Editor editor = facebookPrefs.edit();
 					editor.remove("access_token");
 					editor.remove("access_expires");
 					editor.remove("facebookId");
 					editor.commit();
 
 					FriendModelAdapter listAdapter = ((FriendModelAdapter) getListAdapter());
 					listAdapter.getLoader().clearCache();
 					listAdapter.clear();
 					listAdapter.notifyDataSetChanged();
 
 					ProgressBar progress = (ProgressBar) getView()
 							.findViewById(R.id.progress);
 					progress.setVisibility(View.GONE);
 					login.setVisibility(View.VISIBLE);
 					logout.setVisibility(View.GONE);
 					ImageView arrow = (ImageView) getView().findViewById(
 							R.id.arrow);
 					arrow.setVisibility(View.GONE);
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
 				else if (login.getVisibility() == View.GONE)
 					logout.setVisibility(View.VISIBLE);
 			}
 		});
 
 		return view;
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		try {
 			((ActivityMainViewPager) getActivity()).getViewPagerListener()
 					.addOnPageChangeListener(new OnPageChangeListener() {
 
 						@Override
 						public void onPageSelected(int position) {
 							if (TAG.equals(MainFragmentAdapter
 									.getFragmentPosition().get(position))) {
 								if (facebookAuthorizeCached()) {
 									if (mFriends.isEmpty())
 										retrieveFriends();
 								} else
 									fail("onPageSelected failed to authorize from cache");
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
 		} catch (Exception e) {
 			fail(e);
 		}
 	}
 
 	@Override
 	public void onLowMemory() {
 		((FriendModelAdapter) getListAdapter()).getLoader().clearCache();
 
 		super.onLowMemory();
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
 		ImageView arrow = (ImageView) getView().findViewById(R.id.arrow);
 		arrow.setVisibility(View.VISIBLE);
 
 		mFriends.clear();
 		((ArrayAdapter<?>) getListAdapter()).notifyDataSetChanged();
 
 		if (app.friendsTask == null) {
 			app.friendsTask = new FriendsTask(this);
 			app.friendsTask.execute(app.facebookId,
 					app.facebook.getAccessToken());
 		} else if (!app.facebookId.equals(app.friendsTask.getFacebookId())) {
 			app.friendsTask.cancel(true);
 			app.friendsTask = new FriendsTask(this);
 			app.friendsTask.execute(app.facebookId,
 					app.facebook.getAccessToken());
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
 
 		mFriends.clear();
 		if (friends != null) {
 			mFriends.addAll(friends);
 			((ArrayAdapter<?>) getListAdapter()).notifyDataSetChanged();
 		} else
 			fail("friendsCompleted friends is null");
 	}
 
 	private boolean facebookAuthorizeCached() {
 		facebookPrefs = getActivity().getSharedPreferences("facebook",
 				Context.MODE_PRIVATE);
 		String access_token = facebookPrefs.getString("access_token", null);
 		long expires = facebookPrefs.getLong("access_expires", 0);
 		app.facebookId = facebookPrefs.getString("facebookId", null);
 		if (access_token != null) {
 			app.facebook.setAccessToken(access_token);
 		}
 		if (expires != 0) {
 			app.facebook.setAccessExpires(expires);
 		}
 
 		/*
 		 * Only call authorize if the access_token has expired.
 		 */
 		return app.facebook.isSessionValid() && app.facebookId != null;
 	}
 
 	private void facebookAuthorizeOnline() {
		ProgressBar progress = (ProgressBar) getView().findViewById(
				R.id.progress);
		progress.setVisibility(View.VISIBLE);
		View login = (View) getView().findViewById(R.id.login);
		login.setVisibility(View.GONE);
		ImageView arrow = (ImageView) getView().findViewById(R.id.arrow);
		arrow.setVisibility(View.VISIBLE);

 		app.facebook.authorize(getActivity(), new String[] {},
 				new DialogListener() {
 					@Override
 					public void onComplete(Bundle values) {
 						new AsyncFacebookRunner(app.facebook).request("me",
 								new RequestListener() {
 
 									@Override
 									public void onMalformedURLException(
 											MalformedURLException e,
 											Object state) {
 										fail(e);
 									}
 
 									@Override
 									public void onIOException(IOException e,
 											Object state) {
 										fail(e);
 									}
 
 									@Override
 									public void onFileNotFoundException(
 											FileNotFoundException e,
 											Object state) {
 										fail(e);
 									}
 
 									@Override
 									public void onFacebookError(
 											FacebookError e, Object state) {
 										fail(e);
 									}
 
 									@Override
 									public void onComplete(
 											final String response, Object state) {
 										FragmentPlayerFriends.this
 												.getActivity().runOnUiThread(
 														new Runnable() {
 
 															@Override
 															public void run() {
 																JSONObject me;
 																try {
 																	me = new JSONObject(
 																			response);
 																	app.facebookId = me
 																			.getString("id");
 
 																	SharedPreferences.Editor editor = facebookPrefs
 																			.edit();
 																	editor.putString(
 																			"access_token",
 																			app.facebook
 																					.getAccessToken());
 																	editor.putLong(
 																			"access_expires",
 																			app.facebook
 																					.getAccessExpires());
 																	editor.putString(
 																			"facebookId",
 																			app.facebookId);
 																	editor.commit();
 
 																	retrieveFriends();
 																} catch (Exception e) {
 																	fail(e);
 																	e.printStackTrace();
 																}
 															}
 														});
 									}
 								});
 					}
 
 					@Override
 					public void onFacebookError(FacebookError error) {
 						fail(error);
 					}
 
 					@Override
 					public void onError(DialogError e) {
 						fail(e);
 					}
 
 					@Override
 					public void onCancel() {
 						fail("authorizeOnline cancel");
 					}
 				});
 	}
 
 	private void fail(Object error) {
 		ProgressBar progress = (ProgressBar) getView().findViewById(
 				R.id.progress);
 		progress.setVisibility(View.GONE);
		View login = (View) getView().findViewById(R.id.login);
 		login.setVisibility(View.VISIBLE);
 	}
 }
