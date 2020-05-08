 package com.uvs.coffeejob;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class FriendsFragment extends SherlockFragment {
     private ListView mFriendsList;
     private TextView mFriendsCount;
     
     private List<User> mFriends = UserManager.getInstance().getUserFriends();
     private FriendsAdapter mAdapter;
     
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) 
     {
         View view = inflater.inflate(R.layout.friends_fragment, container, false);
         setHasOptionsMenu(true);
         
         mFriendsList = (ListView) view.findViewById(R.id.friendsList);
         mFriendsList.setOnItemClickListener(mOnItemClickListener);
         mFriendsCount = (TextView)view.findViewById(R.id.friendsCountText);
         
         return view;
     }
     
     @Override
     public void onStart() {
         super.onStart();
         showFriends();
     }
     
     @Override
     public void onPause() {
         super.onPause();
         mAdapter.mGetPhotoTask.cancel(false);
     }
     
     private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
             User friend = mAdapter.getItem(position);
             Intent intent = new Intent();
             intent.setAction(Intent.ACTION_VIEW);
             try {
                 // will throw exception if there is no Facebook app installed
                 getActivity().getPackageManager().getPackageInfo("com.facebook.katana", 0);
                 // Facebook app installed, launch it
                 intent.setData(Uri.parse("fb://profile/" + friend.getId()));
             }
             catch (Exception e) {
                 // Facebook app is not installed, launch browser
                 String url = FacebookManager.userIdToURL(friend.getId());
                 intent.setData(Uri.parse(url));
             }
             startActivity(intent);
         }
     };
     
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         menu.add(0, R.id.menu_show_user, 0, R.string.User)
             .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
         menu.add(0, R.id.menu_about_myself, 0, R.string.About)
             .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == android.R.id.home || item.getItemId() == 0) {
             return false;
         }
         Fragment fragment = null;
         FragmentTransaction transaction;
         FragmentManager frManager = getActivity().getSupportFragmentManager();
         switch (item.getItemId()) {
         case R.id.menu_show_user:
             // clear back stack
             frManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
             
             fragment = new ShowUserFragment();
             transaction = getActivity().getSupportFragmentManager().beginTransaction();
             transaction.replace(R.id.main_frame, fragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
             break;
         case R.id.menu_about_myself:
             // remove this screen from stack
             frManager.popBackStack();
             
             fragment = new AboutMyselfFragment();
             transaction = getActivity().getSupportFragmentManager().beginTransaction();
             transaction.replace(R.id.main_frame, fragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .addToBackStack(null)
                        .commit();
             break;
         }
         
         return true;
     }
     
     private void showFriends() {
         mFriendsCount.setText(String.valueOf(mFriends.size()));
         mAdapter = new FriendsAdapter(getActivity(), mFriends);
         mFriendsList.setAdapter(mAdapter);
     }
     
     private class FriendsAdapter extends ArrayAdapter<User> {
        private List<User> mFriends;
         private LayoutInflater mInflater;
         
         private FacebookManager mFBManager = new FacebookManager();
         public GetPhotoTask mGetPhotoTask = new GetPhotoTask();
 
         public FriendsAdapter(Context context, List<User> friends) {
             super(context, R.layout.friends_list_item, friends);
             Collections.sort(mFriends, new User.PriorityComparator());
             mInflater = (LayoutInflater) context.getSystemService(
                         Context.LAYOUT_INFLATER_SERVICE);
             
             mFriends = friends;
             mGetPhotoTask.execute();
         }
 
         @Override
         public int getCount() {
             return mFriends.size();
         }
 
         @Override
         public User getItem(int position) {
             return mFriends.get(position);
         }
 
         @Override
         public long getItemId(int position) {
             return 0;
         }
 
         @Override
         public View getView(int position, View v, ViewGroup parent) {
             ViewHolder holder;
             
             Log.i("getView()", "Entering function");
 
             if (v == null) {
                 v = mInflater.inflate(R.layout.friends_list_item, parent, false);
                 holder = new ViewHolder();
                 holder.photo    = (ImageView) v.findViewById(R.id.friendPhotoImage);
                 holder.name     = (TextView)  v.findViewById(R.id.friendNameText);
                 holder.priority = (Spinner)   v.findViewById(R.id.priorityList);
                 
                 v.setTag(holder);
             }
             else {
                 holder = (ViewHolder) v.getTag();
             }
 
             User user = getItem(position);
             if (user.getPhoto() != null) {
                 holder.photo.setImageBitmap(user.getPhoto());
             }
             String name = user.getName();
             if (user.getSurname() != null) {
                 name = name + " " + user.getSurname();
             }
             holder.name .setText(name);
             
             Spinner priority = holder.priority;
             priority.setId(position);
             priority.setSelection(priority.getCount() - user.getPriority());
             priority.setOnItemSelectedListener(onPrioritySelected);        
 
             return v;
         }
 
         private class ViewHolder {
             TextView  name;
             ImageView photo;
             Spinner   priority;
         }
         
         OnItemSelectedListener onPrioritySelected = new OnItemSelectedListener() {
             boolean initialized = false;
             
             @Override
             public void onItemSelected(AdapterView<?> parent, View view,
                                        int position, long id) 
             {           
                 int priority = parent.getCount() - position;
                 User friend = mAdapter.getItem(parent.getId());
                 Log.i("onPrioritySelected()", 
                         "user position: " + parent.getId() + "\n" +
                         "old priority: " + friend.getPriority() + "\n" +
                         "new priority: " + priority);
                 if (friend.getPriority() != priority) {
                     friend.setPriority(priority);
                     Collections.sort(mFriends, new User.PriorityComparator());
                     mAdapter.notifyDataSetChanged();
                 }
             }
 
             @Override
             public void onNothingSelected(AdapterView<?> arg0) {}
         };
         
         private class GetPhotoTask extends AsyncTask<Void, Void, User> {
             @Override
             protected User doInBackground(Void... params) {
                 for (User friend: mFriends) {
                     if (isCancelled()) {
                         break;
                     }
                     if (friend.getPhoto() != null) {
                         continue;
                     }
                     // download photo
                     friend.setPhoto(mFBManager.getUserPhoto(friend));
                     // update ListView
                     if (getActivity() != null) {
                         getActivity().runOnUiThread(new Runnable() {
                             @Override
                             public void run() {
                                 FriendsAdapter.this.notifyDataSetChanged();
                             }
                         });
                     }
                 }
                 return null;
             }
         }
     }
 }
