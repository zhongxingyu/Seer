 package com.allplayers.android;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.view.View;
 import android.widget.AbsListView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 
 import com.allplayers.objects.GroupData;
 import com.allplayers.rest.RestApiV1;
 
 public class GroupsFragment extends ListFragment {
     private ArrayList<GroupData> mGroupList = new ArrayList<GroupData>();
     private boolean hasGroups = false, loadMore = true;
     private int mPageNumber = 0;
     private int mCurrentAmountShown = 0;
     private ArrayAdapter<GroupData> mAdapter;
     private ProgressBar mProgressBar;
     private Activity mParentActivity;
     private String mSortType = "radioactive";
     private int mAmountToLoad = 15;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // Get a handle on our containing activity.
         mParentActivity = this.getActivity();
 
         // Create our adapter.
         mAdapter = new ArrayAdapter<GroupData>(mParentActivity, android.R.layout.simple_list_item_1, mGroupList);
 
         // Load our sort type from the arguments.
         if (getArguments() != null && getArguments().containsKey("sort")) {
             mSortType = getArguments().getString("sort");
         }
 
         // Load the first set of groups.
         new GetUserGroupsTask().execute();
     }
 
     public void onViewCreated(View view, Bundle savedInstanceState) {
         // Set up our loading indicator at the foot of the ListView.
         mProgressBar = new ProgressBar(mParentActivity);
        getListView().addFooterView(mProgressBar, null, false);
         // Set our adapter.
         setListAdapter(mAdapter);
 
         // Set a scroll listener to load more if we are at the end of our list
         // and less than two items are offscreen.
        getListView().setOnScrollListener(new OnScrollListener() {
             private int visibleThreshold = 2;
             private int previousTotal = 1;
             private boolean loading = true;
             public void onScroll(AbsListView view, int firstVisibleItem,
             int visibleItemCount, int totalItemCount) {
                 if (loading) {
                     if (totalItemCount > previousTotal) {
                         loading = false;
                         previousTotal = totalItemCount;
                     }
                 }
                 if (loadMore && !loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                     new GetUserGroupsTask().execute();
                     loading = true;
                 }
             }
             @Override
             public void onScrollStateChanged(AbsListView arg0, int arg1) {
             }
 
         });
     }
 
     @Override
     public void onListItemClick(ListView l, View v, int position, long id) {
         super.onListItemClick(l, v, position, id);
         if (hasGroups && position < mGroupList.size()) {
             //Display the group page for the selected group
             Intent intent = (new Router(mParentActivity)).getGroupPageActivityIntent(mGroupList.get(position));
             startActivity(intent);
         }
     }
 
     /** Populates the list of groups to display to the UI thread. */
     protected void updateGroupData() {
         if (!mGroupList.isEmpty()) {
             mAdapter.notifyDataSetChanged();
             // If we did not load 15 groups, we are at the end of the list, so signal
             // not to try to load more groups.
             if (mGroupList.size() - mCurrentAmountShown < mAmountToLoad) {
                 loadMore = false;
                if (getListView() != null) {
                    getListView().removeFooterView(mProgressBar);
                 }
             }
 
             hasGroups = true;
         } else {
             // If we do not have any groups, create a blank list item and remove our loading footer.
             ArrayAdapter<String> blankAdapter = new ArrayAdapter<String>(mParentActivity, android.R.layout.simple_list_item_1);
             blankAdapter.add("No groups to display");
            if (getListView() != null) {
                getListView().setAdapter(blankAdapter);
                getListView().setEnabled(false);
                getListView().removeFooterView(mProgressBar);
             }
         }
     }
 
     /**
      * Fetches the groups a user belongs to and stores the data locally.
      */
     public class GetUserGroupsTask extends AsyncTask<Void, Void, String> {
         protected String doInBackground(Void... args) {
             return RestApiV1.getUserGroups(mPageNumber++ * mAmountToLoad, mAmountToLoad, mSortType);
         }
 
         protected void onPostExecute(String jsonResult) {
             if (!jsonResult.equals("error")) {
                 // Create the new group data objects and add them to our list.
                 GroupsMap groups = new GroupsMap(jsonResult);
                 mCurrentAmountShown = mGroupList.size();
                 mGroupList.addAll(groups.getGroupData());
             }
             // Update the ListView.
             updateGroupData();
         }
     }
 }
