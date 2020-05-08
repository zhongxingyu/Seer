 package com.task;
 
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 import com.actionbarsherlock.app.SherlockListFragment;
 import com.facebook.Request;
 import com.facebook.Response;
 import com.facebook.Session;
 import com.facebook.model.GraphUser;
 import com.facebook.widget.ProfilePictureView;
 import com.task.json.Friend;
 import com.task.json.GsonHelper;
 
 import java.util.*;
 
 import static com.task.Utils.isFbAuthenticated;
 
 /**
  * @author Leus Artem
  * @since 02.06.13
  */
 public class FriendListFragment extends SherlockListFragment {
 
     private static final String TAG = "FriendListFragment";
     private final String prioritiesKey = "prioritiesKey";
     private FriendListAdapter adapter;
     private List<GraphUser> fbUsers;
     private Map<String, Friend> prioritizedFriends = new HashMap<String, Friend>();
 
 
 
     private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
             ViewHolder holder = (ViewHolder) view.getTag();
             startActivity(getOpenFacebookIntent(getActivity(), holder.fbId));
         }
     };
 
     private Runnable fillAdapterRunnable = new Runnable() {
         @Override
         public void run() {
             if (adapter != null && adapter.isEmpty()) {
                 for (GraphUser user : fbUsers) adapter.add(user);
             }
             setLoading(getView(), false);
         }
     };
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setRetainInstance(true);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         return inflater.inflate(R.layout.friends_fragment, container, false);
     }
 
     @Override
     public void onViewCreated(View view, Bundle savedInstanceState) {
         super.onViewCreated(view, savedInstanceState);
     }
 
     @Override
     public void onResume() {
         super.onResume();
         if (fbUsers == null) {
             setLoading(getView(), true);
             if (isFbAuthenticated()) fetchFriendListData(fillAdapterRunnable);
         } else {
             fillAdapterRunnable.run();
         }
         if(prioritizedFriends.isEmpty()){
             String friendsJson = PreferenceManager.getDefaultSharedPreferences(getActivity())
                     .getString(prioritiesKey, "");
             if(!friendsJson.isEmpty()){
                 Collection<Friend> friends = GsonHelper.deserialize(friendsJson);
                 for(Friend friend: friends){
                     prioritizedFriends.put(friend.getId(), friend);
                 }
             }
         }
         Log.i(TAG, "onResume " + TAG);
     }
 
     @Override
     public void onPause() {
         super.onPause();
         PreferenceManager.getDefaultSharedPreferences(getActivity())
                 .edit().putString(prioritiesKey, GsonHelper.serialize(prioritizedFriends.values())).commit();
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         adapter = new FriendListAdapter(getActivity());
         setListAdapter(adapter);
         getListView().setOnItemClickListener(onItemClickListener);
     }
 
     private void setLoading(View rootView, boolean loading) {
         ListView listView = getListView();
         View loadingView = rootView.findViewById(R.id.loadingView);
         listView.setVisibility(loading ? View.GONE : View.VISIBLE);
         loadingView.setVisibility(loading ? View.VISIBLE : View.GONE);
     }
 
     private void fetchFriendListData(final Runnable onPostExecuteRunnable) {
         Session session = Session.getActiveSession();
         // Asynchronously fetching prioritizedFriends list
         Request.executeMyFriendsRequestAsync(session, new Request.GraphUserListCallback() {
             @Override
             public void onCompleted(List<GraphUser> users, Response response) {
                     /* invoked in UI thread*/
                 FriendListFragment.this.fbUsers = users;
                 if(prioritizedFriends.isEmpty()){
                     for(GraphUser fbUser: fbUsers){
                         // filling map with default priorities fbUsers
                         prioritizedFriends.put(fbUser.getId(), new Friend(fbUser.getId(), 0));
                     }
                 }
                 if (onPostExecuteRunnable != null) {
                     onPostExecuteRunnable.run();
                 }
                 Log.i(TAG, "Friend list fetched");
                 for (GraphUser user : users) Log.i(TAG, user.toString());
             }
         });
     }
 
     /**
      * If user has facebook app installed than returns app launch intent
      * otherwise browser will be launched
      */
     private Intent getOpenFacebookIntent(Context context, String userId) {
         try {
             context.getPackageManager().getPackageInfo("com.facebook.katana", 0);
             return new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/" + userId));
         } catch (Exception e) {
             return new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/" + userId));
         }
     }
 
     public static class ViewHolder {
 
         ProfilePictureView profilePic;
         TextView nameView;
         Spinner spinner;
         String fbId;
     }
 
     public class FriendListAdapter extends ArrayAdapter<GraphUser> {
 
         public FriendListAdapter(Context context) {
             super(context, 0);
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             GraphUser user = getItem(position);
             ViewHolder holder;
             if (convertView == null) {
                 holder = new ViewHolder();
                 convertView = LayoutInflater.from(getContext()).inflate(R.layout.friend_list_row, null);
 
                 holder.profilePic = (ProfilePictureView) convertView.findViewById(R.id.userPic);
                 holder.nameView =  (TextView) convertView.findViewById(R.id.name);
 
               final Spinner prioritySpinner = (Spinner) convertView.findViewById(R.id.priority_spinner);
                 ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext()
                         , R.array.priorities_array, android.R.layout.simple_spinner_item);
                 spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                 prioritySpinner.setAdapter(spinnerAdapter);
                 prioritySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                     @Override
                     public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                         GraphUser clickedUser = (GraphUser) prioritySpinner.getTag();
                         Friend friend = prioritizedFriends.get(clickedUser.getId());
                         String prioritySelected = (String) parent.getItemAtPosition(position);
                         friend.setPriority(Utils.getPriorityByString(getContext(), prioritySelected));
                         sort(new Comparator<GraphUser>() {
                             @Override
                             public int compare(GraphUser first, GraphUser second) {
                                 int fpriority = prioritizedFriends.get(first.getId()).getPriority();
                                 int spriority = prioritizedFriends.get(second.getId()).getPriority();
                                 return fpriority < spriority ? -1: 1;
                             }
                         });
                         notifyDataSetChanged();
                     }
 
                     @Override
                     public void onNothingSelected(AdapterView<?> parent) {/* nothing */}
                 });
                 holder.spinner = prioritySpinner;
 
                 convertView.setTag(holder);
             } else {
                 holder=(ViewHolder)convertView.getTag();
             }
 
             holder.profilePic.setProfileId(user.getId());
             holder.nameView.setText(user.getName());
             holder.fbId = user.getId();
 
             Friend friend = prioritizedFriends.get(user.getId());
             if(friend != null) holder.spinner.setSelection(friend.getPriority());
 
             holder.spinner.setTag(user);
 
             return convertView;
         }
     }
 }
