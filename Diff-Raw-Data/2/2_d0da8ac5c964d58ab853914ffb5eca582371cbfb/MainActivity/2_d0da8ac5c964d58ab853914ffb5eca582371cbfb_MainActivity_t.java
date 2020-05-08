 package com.moac.android.opensecretsanta.activity;
 
 import android.app.*;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.graphics.drawable.Drawable;
 import android.os.Build;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.support.v4.app.ActionBarDrawerToggle;
 import android.support.v4.widget.DrawerLayout;
 import android.util.Log;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import com.google.common.primitives.Longs;
 import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
 import com.moac.android.opensecretsanta.R;
 import com.moac.android.opensecretsanta.adapter.DrawerButtonItem;
 import com.moac.android.opensecretsanta.adapter.DrawerListAdapter;
 import com.moac.android.opensecretsanta.adapter.DrawerSectionHeaderItem;
 import com.moac.android.opensecretsanta.adapter.GroupDetailsRow;
 import com.moac.android.opensecretsanta.database.DatabaseManager;
 import com.moac.android.opensecretsanta.draw.MemberEditor;
 import com.moac.android.opensecretsanta.fragment.MemberListFragment;
 import com.moac.android.opensecretsanta.fragment.NotifyDialogFragment;
 import com.moac.android.opensecretsanta.fragment.NotifyExecutorFragment;
 import com.moac.android.opensecretsanta.model.Group;
 import com.moac.android.opensecretsanta.model.Member;
 import com.moac.android.opensecretsanta.model.PersistableObject;
 import com.moac.android.opensecretsanta.notify.NotifyAuthorization;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class MainActivity extends Activity implements MemberListFragment.FragmentContainer, NotifyDialogFragment.FragmentContainer, MemberEditor {
 
     private static final String TAG = MainActivity.class.getSimpleName();
 
     private static final String MEMBERS_LIST_FRAGMENT_TAG = "MemberListFragment";
     private static final String NOTIFY_DIALOG_FRAGMENT_TAG = "NotifyDialogFragment";
     private static final String NOTIFY_EXECUTOR_FRAGMENT_TAG = "NotifyExecutorFragment";
 
     protected DrawerLayout mDrawerLayout;
     protected ActionBarDrawerToggle mDrawerToggle;
     protected ListView mDrawerList;
     protected DatabaseManager mDb; // shorthand.
     protected MemberListFragment mMembersListFragment;
     private NotifyExecutorFragment mNotifyExecutorFragment;
     private DrawerListAdapter mDrawerListAdapter;
 
     @Override
     public void onCreate(Bundle _savedInstanceState) {
         super.onCreate(_savedInstanceState);
         mDb = OpenSecretSantaApplication.getInstance().getDatabase();
 
         // Find or create existing worker fragment
         FragmentManager fm = getFragmentManager();
 
         // Find or create existing worker fragment
         mNotifyExecutorFragment = (NotifyExecutorFragment) fm.findFragmentByTag(NOTIFY_EXECUTOR_FRAGMENT_TAG);
 
         if(mNotifyExecutorFragment == null) {
             mNotifyExecutorFragment = NotifyExecutorFragment.create();
             fm.beginTransaction().add(mNotifyExecutorFragment, NOTIFY_EXECUTOR_FRAGMENT_TAG).commit();
         }
         initialiseUI();
     }
 
     private void initialiseUI() {
         setContentView(R.layout.activity_main);
         mDrawerList = (ListView) findViewById(R.id.left_drawer_list);
 
         mDrawerListAdapter = new DrawerListAdapter(this);
         mDrawerList.setAdapter(mDrawerListAdapter);
         populateDrawerListView(mDrawerListAdapter);
         mDrawerList.setOnItemClickListener(new GroupItemClickListener());
 
         mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
         mDrawerToggle = new ActionBarDrawerToggle(
           this,                  /* host Activity */
           mDrawerLayout,         /* DrawerLayout object */
           R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
           R.string.drawer_open_accesshint,  /* "open drawer" description */
           R.string.drawer_close_accesshint) /* "close drawer" description */ {
 
             /** Called when a drawer has settled in a completely closed state. */
             public void onDrawerClosed(View view) {
                 getActionBar().setTitle(getString(R.string.app_name));
                 getActionBar().setIcon(R.drawable.icon);
                 invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
             }
 
             /** Called when a drawer has settled in a completely open state. */
             public void onDrawerOpened(View drawerView) {
                 invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
             }
         };
 
         // Set the drawer toggle as the DrawerListener
         mDrawerLayout.setDrawerListener(mDrawerToggle);
 
         getActionBar().setDisplayHomeAsUpEnabled(true);
         getActionBar().setHomeButtonEnabled(true);
 
         // Add the Members List for the most recent Group
         displayInitialGroup();
     }
 
     @Override
     protected void onPostCreate(Bundle savedInstanceState) {
         super.onPostCreate(savedInstanceState);
         // Sync the toggle state after onRestoreInstanceState has occurred.
         mDrawerToggle.syncState();
     }
 
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         mDrawerToggle.onConfigurationChanged(newConfig);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Pass the event to ActionBarDrawerToggle, if it returns
         // true, then it has handled the app icon touch event
         if(mDrawerToggle.onOptionsItemSelected(item)) {
             return true;
         }
         switch(item.getItemId()) {
             // TODO Move to overlay menu
             // TODO Rename group
             //  case R.id.menu_settings:
             //    Intent intent = new Intent(MainActivity.this, AllPreferencesActivity.class);
             //    slideInIntent(intent);
             //    return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public void onEditMember(long _memberId) {
         Intent intent = new Intent(MainActivity.this, EditActivity.class);
         intent.putExtra(Intents.MEMBER_ID_INTENT_EXTRA, _memberId);
         slideInIntent(intent);
     }
 
     @Override
     public void onRestrictMember(long _groupId, long _memberId) {
         Intent intent = new Intent(MainActivity.this, RestrictionsActivity.class);
         intent.putExtra(Intents.GROUP_ID_INTENT_EXTRA, _groupId);
         intent.putExtra(Intents.MEMBER_ID_INTENT_EXTRA, _memberId);
         slideInIntent(intent);
     }
 
     @Override
     public void requestNotifyDraw(Group _group, long[] _memberIds) {
         Log.i(TAG, "onNotifyDraw() - Requesting Notify member set size:" + _memberIds.length);
         // Check the requirement for the notify
         DialogFragment dialog = NotifyDialogFragment.create(_group.getId(), _memberIds);
         dialog.show(getFragmentManager(), NOTIFY_DIALOG_FRAGMENT_TAG);
     }
 
     @Override
     public void requestNotifyDraw(Group _group) {
         Log.i(TAG, "onNotifyDraw() - Requesting Notify entire Group");
         // TODO Background
         List<Member> members = mDb.queryAllMembersForGroup(_group.getId());
         List<Long> memberIds = new ArrayList<Long>(members.size());
         for(Member member : members) {
             memberIds.add(member.getId());
         }
         requestNotifyDraw(_group, Longs.toArray(memberIds));
     }
 
     @Override
     public void executeNotifyDraw(NotifyAuthorization auth, final Group group, final long[] members) {
         mNotifyExecutorFragment.notifyDraw(auth, group, members);
     }
 
     private void populateDrawerListView(DrawerListAdapter drawerListAdapter) {
 
         List<DrawerListAdapter.Item> drawerListItems = new ArrayList<DrawerListAdapter.Item>();
 
         // Add "My Groups" section header
         drawerListItems.add(new DrawerSectionHeaderItem(getString(R.string.drawer_groups_header)));
 
         // Add "Add Group" button item
         Drawable addIcon = getResources().getDrawable(R.drawable.ic_content_new);
         drawerListItems.add(new DrawerButtonItem(addIcon, "Add Group", new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 long id = createNewGroup();
                 showGroup(id);
                 // TODO Need to highlight the group in draw!
                 mDrawerLayout.closeDrawer(mDrawerList);
             }
         }));
 
         // Add each Group item
         List<Group> groups = OpenSecretSantaApplication.getInstance().getDatabase().queryAll(Group.class);
         Log.v(TAG, "initialiseUI() - group count: " + groups.size());
         for(Group g : groups) {
             List<Member> groupMembers = OpenSecretSantaApplication.getInstance().getDatabase().queryAllMembersForGroup(g.getId());
             drawerListItems.add(new GroupDetailsRow(g.getId(), g.getName(), g.getCreatedAt(), groupMembers));
         }
 
         // Add "Options" section header
         drawerListItems.add(new DrawerSectionHeaderItem(getString(R.string.drawer_options_header)));
 
         // Add Settings button item
         Drawable settingsIcon = getResources().getDrawable(R.drawable.ic_action_settings);
         drawerListItems.add(new DrawerButtonItem(settingsIcon, "Settings", null));
 
         drawerListAdapter.addAll(drawerListItems);
     }
 
     private long createNewGroup() {
         Log.i(TAG, "Creating new Group");
         Group group = new Group();
         // A sort of UUID
         long now = System.currentTimeMillis();
         group.setName(Long.toString(now));
         group.setCreatedAt(now);
         long id = mDb.create(group);
        group.setName(getString(R.string.first_group_name) + " - " + id);
         mDb.update(group);
         return id;
     }
 
     private void displayInitialGroup() {
         // Fetch the most recently used Group Id from preferences
         long groupId = PreferenceManager.getDefaultSharedPreferences(this).
           getLong(OpenSecretSantaApplication.MOST_RECENT_GROUP_KEY, PersistableObject.UNSET_ID);
         if(groupId <= PersistableObject.UNSET_ID)
             return;
 
         showGroup(groupId);
     }
 
     @Override
     public MemberEditor getMemberEditor() {
         // FIXME for now implement as this activity.
         return this;
     }
 
     private class GroupItemClickListener implements AdapterView.OnItemClickListener {
         @Override
         public void onItemClick(AdapterView<?> _parent, View _view, int _position, long _id) {
             Log.d(TAG, "onItemClick() - position: " + _position + " id: " + _id);
             if(_id <= PersistableObject.UNSET_ID)
                 return;
 
             // Highlight the selected item, update the title, and close the drawer
             showGroup(_id);
             mDrawerList.setItemChecked(_position, true);
             mDrawerLayout.closeDrawer(mDrawerList);
         }
     }
 
     private void slideInIntent(Intent intent) {
         // Activity options is since API 16.
         // Got this idea from Android Dev Bytes video - https://www.youtube.com/watch?v=Ho8vk61lVIU
         if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
             startActivity(intent);
             overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
         } else {
             Bundle translateBundle = ActivityOptions.makeCustomAnimation(this, R.anim.slide_in_left, R.anim.slide_out_left).toBundle();
             startActivity(intent, translateBundle);
         }
     }
 
     private void showGroup(long _groupId) {
         Log.i(TAG, "showGroup() - start. groupId: " + _groupId);
 
         FragmentManager fragmentManager = getFragmentManager();
 
         // See if the correct fragment already exists
         MemberListFragment existing = (MemberListFragment) fragmentManager.findFragmentByTag(MEMBERS_LIST_FRAGMENT_TAG);
         if(existing != null && existing.getGroupId() == _groupId) {
             Log.i(TAG, "showGroup() - found matching required fragment");
             mMembersListFragment = existing;
             return;
         }
 
         // Replace existing fragment
         // Note: Can't call replace, seems to replace ALL fragments in the layout.
         FragmentTransaction transaction = fragmentManager.beginTransaction();
         if(existing != null) {
             Log.i(TAG, "showGroup() - removing existing fragment");
             transaction.remove(mMembersListFragment);
         }
 
         Log.i(TAG, "showGroup() - creating new fragment");
         MemberListFragment newFragment = MemberListFragment.create(_groupId);
         transaction.add(R.id.content_frame, newFragment, MEMBERS_LIST_FRAGMENT_TAG)
           .commit();
         mMembersListFragment = newFragment;
 
         // Update preferences to save last viewed Group
         PreferenceManager.getDefaultSharedPreferences(this).
           edit().putLong(OpenSecretSantaApplication.MOST_RECENT_GROUP_KEY, _groupId).apply();
     }
 }
