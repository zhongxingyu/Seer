 package edu.selu.android.classygames;
 
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.facebook.Request;
 import com.facebook.Request.GraphUserListCallback;
 import com.facebook.Response;
 import com.facebook.Session;
 import com.facebook.model.GraphUser;
 
 import edu.selu.android.classygames.models.Person;
 import edu.selu.android.classygames.utilities.FacebookUtilities;
 import edu.selu.android.classygames.utilities.TypefaceUtilities;
 import edu.selu.android.classygames.utilities.Utilities;
 
 
 public class FriendsListFragment extends SherlockFragment implements OnItemClickListener
 {
 
 
 
 
 	/**
 	 * Boolean that marks if this is the first time that the onResume() method
 	 * was hit.
 	 */
 	private boolean isFirstOnResume = true;
 
 
 	/**
 	 * Variables that holds whether or not the asyncRefreshFriendsList
 	 * AsyncTask is currently running.
 	 */
 	private boolean isAsyncRefreshFriendsListRunning = false;
 
 
 	/**
 	 * Holds a handle to the currently running (if it's currently running)
 	 * AsyncRefreshFriendsList AsyncTask.
 	 */
 	private AsyncRefreshFriendsList asyncRefreshFriendsList;
 
 
 	/**
 	 * List Adapter for this Fragment's ListView layout item.
 	 */
 	private FriendsListAdapter friendsListAdapter;
 
 
 
 
 	/**
 	 * One of this class's callback methods. This is fired whenever the use
 	 * has selected a friend in their friends list.
 	 */
 	private FriendsListFragmentOnFriendSelectedListener friendsListFragmentOnFriendSelectedListener;
 
 	public interface FriendsListFragmentOnFriendSelectedListener
 	{
 		public void friendsListFragmentOnFriendSelected(final Person friend);
 	}
 
 
 	/**
 	 * One of this class's callback methods. This is fired whenever the user
 	 * has selected the Refresh button on the action bar.
 	 */
 	private FriendsListFragmentOnRefreshSelectedListener friendsListFragmentOnRefreshSelectedListener;
 
 	public interface FriendsListFragmentOnRefreshSelectedListener
 	{
 		public void friendsListFragmentOnRefreshSelected();
 	}
 
 
 
 
 	@Override
 	public void onCreate(final Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setHasOptionsMenu(true);
 	}
 
 
 	@Override
 	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState)
 	{
 		return inflater.inflate(R.layout.friends_list_fragment, container, false);
 	}
 
 
 	@Override
 	public void onAttach(final Activity activity)
 	// This makes sure that the Activity containing this Fragment has
 	// implemented the callback interface. If the callback interface has not
 	// been implemented, an exception is thrown.
 	{
 		super.onAttach(activity);
 
 		try
 		{
 			friendsListFragmentOnFriendSelectedListener = (FriendsListFragmentOnFriendSelectedListener) activity;
 			friendsListFragmentOnRefreshSelectedListener = (FriendsListFragmentOnRefreshSelectedListener) activity;
 		}
 		catch (final ClassCastException e)
 		{
 			throw new ClassCastException(activity.toString() + " must implement listeners!");
 		}
 	}
 
 
 	@Override
 	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater)
 	{
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && isAsyncRefreshFriendsListRunning)
 		{
 			inflater.inflate(R.menu.generic_cancel, menu);
 		}
 		else
 		{
 			inflater.inflate(R.menu.generic_refresh, menu);
 		}
 
 		super.onCreateOptionsMenu(menu, inflater);
 	}
 
 
 	@Override
 	public void onItemClick(final AdapterView<?> l, final View v, final int position, final long id)
 	{
 		final Person friend = friendsListAdapter.getItem(position);
 		friendsListFragmentOnFriendSelectedListener.friendsListFragmentOnFriendSelected(friend);
 	}
 
 
 	@Override
 	public boolean onOptionsItemSelected(final MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case R.id.generic_cancel_menu_cancel:
 				if (isAsyncRefreshFriendsListRunning)
 				{
 					asyncRefreshFriendsList.cancel(true);
 				}
 				break;
 
 			case R.id.generic_refresh_menu_refresh:
 				getSherlockActivity().getPreferences(Context.MODE_PRIVATE).edit().clear().commit();
 				friendsListFragmentOnRefreshSelectedListener.friendsListFragmentOnRefreshSelected();
 				break;
 
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 
 		return true;
 	}
 
 
 	@Override
 	public void onResume()
 	{
 		super.onResume();
 
 		if (isFirstOnResume)
 		{
 			isFirstOnResume = false;
 			refreshFriendsList();
 		}
 	}
 
 
 
 
 	/**
 	 * Cancels the AsyncRefreshFriendsList AsyncTask if it is currently
 	 * running.
 	 */
 	public void cancelAsyncRefreshFriendsList()
 	{
 		if (isAsyncRefreshFriendsListRunning)
 		{
 			asyncRefreshFriendsList.cancel(true);
 		}
 	}
 
 
 	/**
 	 * @return
 	 * Returns true if the asyncRefreshFriendsList AsyncTask is currently
 	 * running.
 	 */
 	public boolean isAsyncRefreshFriendsListRunning()
 	{
 		return isAsyncRefreshFriendsListRunning;
 	}
 
 
 	/**
 	 * Refreshes the friends list if a refresh is not already running.
 	 */
 	public void refreshFriendsList()
 	{
 		if (!isAsyncRefreshFriendsListRunning)
 		{
 			asyncRefreshFriendsList = new AsyncRefreshFriendsList(getSherlockActivity(), getLayoutInflater(getArguments()), Session.getActiveSession(), (ViewGroup) getView());
 			asyncRefreshFriendsList.execute();
 		}
 	}
 
 
 
 
 	private final class AsyncRefreshFriendsList extends AsyncTask<Void, Void, ArrayList<Person>>
 	{
 
 
 		private SherlockFragmentActivity fragmentActivity;
 		private LayoutInflater inflater;
 		private Session session;
 		private ViewGroup viewGroup;
 
 
 		AsyncRefreshFriendsList(final SherlockFragmentActivity fragmentActivity, final LayoutInflater inflater, final Session session, final ViewGroup viewGroup)
 		{
 			this.fragmentActivity = fragmentActivity;
 			this.inflater = inflater;
 			this.session = session;
 			this.viewGroup = viewGroup;
 		}
 
 
 		@Override
 		protected ArrayList<Person> doInBackground(final Void... params)
 		{
 			final ArrayList<Person> friends = new ArrayList<Person>();
 
 			if (!isCancelled())
 			{
 				final SharedPreferences sPreferences = fragmentActivity.getPreferences(Context.MODE_PRIVATE);
 
 				@SuppressWarnings("unchecked")
 				final Map<String, String> map = (Map<String, String>) sPreferences.getAll();
 
 				if (map == null || map.isEmpty())
 				{
 					Request.newMyFriendsRequest(session, new GraphUserListCallback()
 					{
 						@Override
 						public void onCompleted(final List<GraphUser> users, final Response response)
 						{
 							for (int i = 0; i < users.size() && !isCancelled(); ++i)
 							{
 								final GraphUser user = users.get(i);
 								final String id = user.getId();
 								final String name = user.getName();
 
 								final Person friend = addFriend(id, name);
 
 								if (friend != null)
 								{
 									friends.add(friend);
 								}
 							}
 
 							friends.trimToSize();
 
 							final SharedPreferences.Editor editor = sPreferences.edit();
 							editor.clear();
 
 							for (int i = 0; i < friends.size() && !isCancelled(); ++i)
 							{
 								final Person friend = friends.get(i);
 								editor.putString(friend.getIdAsString(), friend.getName());
 							}
 
 							editor.commit();
 						}
 					}).executeAndWait();
 				}
 				else
 				{
 					final Set<String> set = map.keySet();
 
 					for (final Iterator<String> i = set.iterator(); i.hasNext() && !isCancelled(); )
 					{
 						final String id = i.next();
 						final String name = map.get(id);
 
 						final Person friend = addFriend(id, name);
 
 						if (friend != null)
 						{
 							friends.add(friend);
 						}
 					}
 
 					friends.trimToSize();
 				}
 			}
 
 			Collections.sort(friends, new FriendsListSorter());
 
 			return friends;
 		}
 
 
 		/**
 		 * Creates a Person object out of the given data (if the given data is
 		 * valid).
 		 * 
 		 * @param id
 		 * The friend's Facebook ID.
 		 * 
 		 * @param name
 		 * The friend's Facebook name.
 		 * 
 		 * @return
 		 * Returns a Person object representing the given Facebook friend. Has
 		 * the possibility of returning null if the given data is invalid.
 		 */
 		private Person addFriend(final String id, final String name)
 		{
 			Person friend = null;
 
 			if (Person.isIdValid(id) && Person.isNameValid(name))
 			{
 				friend = new Person(id, name);
 			}
 
 			return friend;
 		}
 
 
 		private void cancelled()
 		{
 			viewGroup.removeAllViews();
 			inflater.inflate(R.layout.friends_list_fragment_cancelled, viewGroup);
 
 			setRunningState(false);
 		}
 
 
 		@Override
 		protected void onCancelled()
 		{
 			cancelled();
 		}
 
 
 		@Override
 		protected void onCancelled(final ArrayList<Person> friends)
 		{
 			cancelled();
 		}
 
 
 		@Override
 		protected void onPostExecute(final ArrayList<Person> friends)
 		{
 			viewGroup.removeAllViews();
 
			if (friends != null && friends.size() >= 1)
 			{
 				inflater.inflate(R.layout.friends_list_fragment, viewGroup);
 
 				friendsListAdapter = new FriendsListAdapter(fragmentActivity, R.layout.friends_list_fragment_listview_item, friends);
 				final ListView listView = (ListView) viewGroup.findViewById(R.id.friends_list_fragment_listview);
 				listView.setAdapter(friendsListAdapter);
 				listView.setOnItemClickListener(FriendsListFragment.this);
 			}
 			else
 			{
 				inflater.inflate(R.layout.friends_list_fragment_no_friends, viewGroup);
 			}
 
 			setRunningState(false);
 		}
 
 
 		@Override
 		protected void onPreExecute()
 		{
 			setRunningState(true);
 
 			viewGroup.removeAllViews();
 			inflater.inflate(R.layout.friends_list_fragment_loading, viewGroup);
 		}
 
 
 		/**
 		 * Use this method to reset the options menu. This should only be used when
 		 * an AsyncTask has either just begun or has just ended.
 		 * 
 		 * @param isRunning
 		 * True if the AsyncTask is just starting to run, false if it's just
 		 * finished.
 		 */
 		private void setRunningState(final boolean isRunning)
 		{
 			isAsyncRefreshFriendsListRunning = isRunning;
 			Utilities.compatInvalidateOptionsMenu(fragmentActivity, true);
 		}
 
 
 	}
 
 
 
 
 	private final class FriendsListAdapter extends ArrayAdapter<Person>
 	{
 
 
 		private ArrayList<Person> friends;
 		private Context context;
 		private Drawable emptyProfilePicture;
 
 
 		FriendsListAdapter(final Context context, final int textViewResourceId, final ArrayList<Person> friends)
 		{
 			super(context, textViewResourceId, friends);
 			this.friends = friends;
 			this.context = context;
 
 			emptyProfilePicture = (Drawable) context.getResources().getDrawable(R.drawable.empty_profile_picture_small);
 		}
 
 
 		@Override
 		public View getView(final int position, View convertView, final ViewGroup parent)
 		{
 			if (convertView == null)
 			{
 				final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				convertView = inflater.inflate(R.layout.friends_list_fragment_listview_item, null);
 
 				final ViewHolder viewHolder = new ViewHolder();
 				viewHolder.name = (TextView) convertView.findViewById(R.id.friends_list_fragment_listview_item_name);
 				viewHolder.picture = (ImageView) convertView.findViewById(R.id.friends_list_fragment_listview_item_picture);
 
 				convertView.setTag(viewHolder);
 			}
 
 			final ViewHolder viewHolder = (ViewHolder) convertView.getTag();
 			final Person friend = friends.get(position);
 			viewHolder.name.setText(friend.getName());
 			viewHolder.name.setTypeface(TypefaceUtilities.getTypeface(context.getAssets(), TypefaceUtilities.BLUE_HIGHWAY_D));
 			viewHolder.picture.setImageDrawable(emptyProfilePicture);
 			Utilities.getImageLoader(context).displayImage(FacebookUtilities.GRAPH_API_URL + friend.getId() + FacebookUtilities.GRAPH_API_URL_PICTURE_TYPE_SMALL_SSL, viewHolder.picture);
 
 			return convertView;
 		}
 
 
 	}
 
 
 
 
 	private final static class ViewHolder
 	{
 
 
 		ImageView picture;
 		TextView name;
 
 
 	}
 
 
 
 
 	private final class FriendsListSorter implements Comparator<Person>
 	{
 		@Override
 		public int compare(final Person geo, final Person jarrad)
 		{
 			return geo.getName().compareToIgnoreCase(jarrad.getName());
 		}
 	}
 
 
 
 
 }
