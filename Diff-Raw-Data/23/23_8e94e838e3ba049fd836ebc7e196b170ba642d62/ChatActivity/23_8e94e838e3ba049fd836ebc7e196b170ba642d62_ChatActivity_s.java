 package net.meneame.fisgodroid;
 
 import java.util.List;
 
 import android.annotation.TargetApi;
 import android.app.ActionBar;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.RadioGroup;
 
 public class ChatActivity extends FragmentActivity implements ActionBar.OnNavigationListener
 {
     private ChatFragment mFragment = null;
 
 
     /**
      * The serialization (saved instance state) Bundle key representing the
      * current dropdown position.
      */
     private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_chat);
         
         // Set up the action bar to show a dropdown list.
         final ActionBar actionBar = getActionBar();
         actionBar.setDisplayShowTitleEnabled(false);
         actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 
         // Set up the dropdown list navigation in the action bar.
         actionBar.setListNavigationCallbacks(
         // Specify a SpinnerAdapter to populate the dropdown list.
                 new ArrayAdapter<String>(getActionBarThemedContextCompat(), android.R.layout.simple_list_item_1, android.R.id.text1, new String[] {
                         getString(R.string.title_general), getString(R.string.title_friends), }), this);
     }
 
     @Override
     protected void onStop()
     {
         super.onStop();
     }
 
     /**
      * Backward-compatible version of {@link ActionBar#getThemedContext()} that
      * simply returns the {@link android.app.Activity} if
      * <code>getThemedContext</code> is unavailable.
      */
     @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
     private Context getActionBarThemedContextCompat()
     {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
         {
             return getActionBar().getThemedContext();
         } else
         {
             return this;
         }
     }
 
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState)
     {
         // Restore the previously serialized current dropdown position.
         if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM))
         {
             getActionBar().setSelectedNavigationItem(savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
         }
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState)
     {
         // Serialize the current dropdown position.
         outState.putInt(STATE_SELECTED_NAVIGATION_ITEM, getActionBar().getSelectedNavigationIndex());
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.chat, menu);
         return true;
     }
 
     @Override
     public boolean onNavigationItemSelected(int position, long id)
     {
         // When the given dropdown item is selected, show its contents in the
         // container view.
         if (mFragment == null)
         {
             mFragment = new ChatFragment();
             getSupportFragmentManager().beginTransaction().replace(R.id.container, mFragment).commit();
         }
         mFragment.setType(position == 0 ? ChatType.PUBLIC : ChatType.FRIENDS);
         return true;
     }
 
     /**
      * A dummy fragment representing a section of the app, but that simply
      * displays dummy text.
      */
     public static class ChatFragment extends Fragment
     {
         // Create a handler to update the view from the UI thread
         // when the message list changes.
         private Handler mHandler = new Handler ()
         {
             public void handleMessage ( Message msg )
             {
                 updateMessages(mFisgoBinder.getMessages());
             }
         };
 
         // Reference to the service binder
         private FisgoService.FisgoBinder mFisgoBinder = null;
         private ServiceConnection mServiceConn = new ServiceConnection()
         {
             @Override
             public void onServiceConnected(ComponentName arg0, IBinder binder)
             {
                 mFisgoBinder = (FisgoService.FisgoBinder) binder;
                 mFisgoBinder.addHandler(mHandler);
             }
 
             @Override
             public void onServiceDisconnected(ComponentName arg0)
             {
                mFisgoBinder.removeHandler(mHandler);
             }
         };
         
         private RadioGroup mRadioGroup;
         private ListView mMessages;
         private ChatType mType;
         private ChatMessageAdapter mAdapter;
         private AvatarStorage mAvatarStorage;
 
         /**
          * The fragment argument representing the section number for this
          * fragment.
          */
         public static final String ARG_SECTION_NUMBER = "section_number";
 
         public ChatFragment()
         {
         }
 
         @Override
         public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
         {
             View rootView = inflater.inflate(R.layout.chat_layout, container, false);
             
             // Get views
             mRadioGroup = (RadioGroup) rootView.findViewById(R.id.radio_group_chattype);
             mMessages = (ListView) rootView.findViewById(R.id.chat_messages);
 
             // Setup
             mAvatarStorage = new AvatarStorage(getActivity());
             mAdapter = new ChatMessageAdapter(getActivity(), mAvatarStorage);
             mMessages.setAdapter(mAdapter);
             setType(mType);
 
             // Connect with the chat service
            getActivity().bindService(new Intent(getActivity(), FisgoService.class), mServiceConn, BIND_AUTO_CREATE);
             
             return rootView;
         }
         
         @Override
         public void onDestroyView ()
         {
             getActivity().unbindService(mServiceConn);
             super.onDestroyView();
         }
 
         public void setType(ChatType type)
         {
             mType = type;
             if (mRadioGroup != null)
             {
                 mRadioGroup.setVisibility(type == ChatType.PUBLIC ? View.VISIBLE : View.GONE);
             }
         }
 
         public void updateMessages(List<ChatMessage> messages)
         {
             mAdapter.setMessages(messages);
         }
     }
 
 }
