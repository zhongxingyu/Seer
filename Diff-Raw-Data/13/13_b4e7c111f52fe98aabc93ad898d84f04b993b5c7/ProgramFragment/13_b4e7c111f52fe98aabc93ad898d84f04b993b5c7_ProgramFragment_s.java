 package com.qsoft.pilotproject.ui.fragment;
 
 import android.content.ContentResolver;
 import android.content.ContentUris;
import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.util.Log;
 import android.widget.ImageButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import com.example.PilotProject.R;
 import com.googlecode.androidannotations.annotations.*;
 import com.qsoft.pilotproject.common.CommandExecutor;
import com.qsoft.pilotproject.common.commands.GenericStartActivityCommand;
 import com.qsoft.pilotproject.model.Feed;
 import com.qsoft.pilotproject.model.ProgramTab;
 import com.qsoft.pilotproject.provider.OnlineDioContract;
import com.qsoft.pilotproject.ui.activity.SlideBarActivity_;
 import com.qsoft.pilotproject.utils.Utilities;
 
 /**
  * User: binhtv
  * Date: 10/17/13
  * Time: 8:59 AM
  */
 @EFragment(R.layout.program)
 public class ProgramFragment extends Fragment
 {
     static final String TAG = "ProgramFragment";
     private static final int RC_SLIDE_BAR_ACTIVITY = 5;
     public static final String DETAIL_FRAGMENT = "detail";
     public static final String THUMBNAIL = "thumbnail";
     FragmentManager manager = getFragmentManager();
     @ViewById(R.id.ibProgramBack)
     ImageButton ibProgramBack;
 
     ProgramTab currentTab = ProgramTab.THUMB_NAIL;
     @ViewById(R.id.rgProgramTab)
     RadioGroup rgProgramTab;
     @ViewById(R.id.tvProgramTitle)
     TextView tvTitle;
     @ViewById(R.id.tvProgramDisplayNameFeed)
     TextView tvDisplayName;
     @ViewById(R.id.tvContentLike)
     TextView tvLikes;
     @ViewById(R.id.tvContentPlay)
     TextView tvPlayed;
     @ViewById(R.id.tvContentLook)
     TextView tvLooks;
     @ViewById(R.id.tvLastUpdate)
     TextView tvUpdated;
     Feed feed = null;
     @Bean
     CommandExecutor commandExecutor;
 
 
     @AfterViews
     void afterViews()
     {
         setRetainInstance(true);
         startContentPlayerFragment();
         final ContentResolver contentResolver = getActivity().getContentResolver();
         Bundle bundle = getArguments();
         Long id = bundle.getLong(HomeListFragment.FEED_ID);
         if (id == null)
         {
             Log.e(TAG, "ProgramFragment ERROR");
         }
         Uri singleUri = ContentUris.withAppendedId(OnlineDioContract.Feed.CONTENT_URI, id);
         // get all
         Cursor cursor = contentResolver.query(singleUri, null, null, null, null);
         assert cursor != null;
 
         while (cursor.moveToNext())
         {
             feed = Feed.fromCursor(cursor);
         }
 
         tvTitle.setText(feed.getTitle());
         tvDisplayName.setText(feed.getDisplayName());
         tvLikes.setText(Integer.toString(feed.getLikes()));
         tvLooks.setText(Integer.toString(feed.getViewed()));
         String played = feed.getPlayed();
         if (played == null)
         {
             played = 0 + "";
         }
         tvPlayed.setText(played);
         tvUpdated.setText(Utilities.calculatorUpdateTime(feed.getUpdatedAt()));
         cursor.close();
         rgProgramTab.setOnCheckedChangeListener(programTabOnCheckChangeListener);
         rgProgramTab.check(R.id.rbThumbnail);
 
     }
 
 
     @Click(R.id.ibProgramBack)
     void doClickBack()
     {
         setRetainInstance(false);
         ContentPlayerFragment playerFragment = (ContentPlayerFragment) getFragmentManager().findFragmentById(R.id.contentPlayerFragment);
         playerFragment.setRetainInstance(false);
        playerFragment.onDestroy();
         getFragmentManager().popBackStack();
     }
 
     RadioGroup.OnCheckedChangeListener programTabOnCheckChangeListener = new RadioGroup.OnCheckedChangeListener()
     {
         @Override
         public void onCheckedChanged(RadioGroup radioGroup, int i)
         {
             int checkedRbTab = rgProgramTab.getCheckedRadioButtonId();
             switch (checkedRbTab)
             {
                 case R.id.rbThumbnail:
                     currentTab = ProgramTab.THUMB_NAIL;
                     break;
                 case R.id.rbDetail:
                     currentTab = ProgramTab.DETAIL;
                     break;
                 case R.id.rbComment:
                     currentTab = ProgramTab.COMMENT;
                     break;
             }
             updateProgramFragment();
         }
     };
 
     void startContentPlayerFragment()
     {
         Fragment playerFragment = getFragmentManager().findFragmentById(R.id.contentPlayerFragment);
         if (playerFragment == null)
         {
             getFragmentManager().beginTransaction().replace(R.id.contentPlayerFragment, new ContentPlayerFragment_()).commit();
         }
        else
        {
            getFragmentManager().beginTransaction().remove(playerFragment).replace(R.id.contentPlayerFragment, new ContentPlayerFragment_()).commit();
        }
     }
 
     void updateProgramFragment()
     {
         Fragment fragmentContainer = getFragmentManager().findFragmentById(R.id.fragmentContainer);
         switch (currentTab)
         {
             case DETAIL:
                 fragmentContainer = new DetailFragment_();
                 Bundle detailBundle = new Bundle();
                 detailBundle.putString(DETAIL_FRAGMENT, feed.getDescription());
                 fragmentContainer.setArguments(detailBundle);
                 break;
             case COMMENT:
                 fragmentContainer = new CommentFragment_();
                 break;
             case THUMB_NAIL:
                 fragmentContainer = new ThumbnailFragment_();
                 Bundle thumbnailBundle = new Bundle();
                 thumbnailBundle.putString(THUMBNAIL, feed.getThumbnail());
                 fragmentContainer.setArguments(thumbnailBundle);
                 break;
         }
         getFragmentManager().beginTransaction().replace(R.id.fragmentContainer, fragmentContainer).commit();
     }
 
 
 }
