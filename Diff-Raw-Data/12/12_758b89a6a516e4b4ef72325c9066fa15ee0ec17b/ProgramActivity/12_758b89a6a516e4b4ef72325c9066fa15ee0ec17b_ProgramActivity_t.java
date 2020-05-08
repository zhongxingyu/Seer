 package com.tools.tvguide;
 
 import com.tools.tvguide.activities.EpisodeActivity;
 import com.tools.tvguide.adapters.ResultPageAdapter;
 import com.tools.tvguide.data.Program;
 import com.tools.tvguide.managers.AppEngine;
 import com.tools.tvguide.managers.ProgramHtmlManager;
 import com.tools.tvguide.managers.AdManager.AdSize;
 import com.tools.tvguide.utils.Utility;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.support.v4.view.ViewPager;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 public class ProgramActivity extends Activity 
 {
     private static int sRequestId;
     private String mTitle;
     private String mProfile;
     private Bitmap mPicture;
     private String mSummary;
     private String mEpisodeEntryLink;
     
     private Program mProgram;
     private LayoutInflater mInflater;
     private TextView mProgramNameTextView;
     private TextView mProgramProfileTextView;
     private ImageView mProgramImageView;
     private ImageView mPlotsImageView;
     private ViewPager mViewPager;
     private ResultPageAdapter mPageAdapter;
     private LinearLayout mSummaryLayout;
     private LinearLayout.LayoutParams mCenterLayoutParams;
     
     enum SelfMessage {MSG_TITLE_LOADED, MSG_SUMMARY_LOADED, MSG_PROFILE_LOADED, MSG_PICTURE_LOADED, MSG_HAS_DETAIL_PLOTS};
     
     @Override
     protected void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_program);
         
         mInflater = LayoutInflater.from(this);
         mProgram = (Program) getIntent().getSerializableExtra("program");
         if (mProgram == null)
             return;
         
         mProgramNameTextView = (TextView) findViewById(R.id.program_name);
         mProgramProfileTextView = (TextView) findViewById(R.id.program_profile);
         mProgramImageView = (ImageView) findViewById(R.id.program_image);
         mPlotsImageView = (ImageView) findViewById(R.id.episode_iv);
         mSummaryLayout = (LinearLayout) mInflater.inflate(R.layout.program_tab_summary, null);
         mViewPager = (ViewPager) findViewById(R.id.program_view_pager);
         mCenterLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
         
         mProgramNameTextView.setText(mProgram.title);
         mPlotsImageView.setVisibility(View.INVISIBLE);
         
         mPageAdapter = new ResultPageAdapter();
         LinearLayout loadingLayout = (LinearLayout)mInflater.inflate(R.layout.center_text_tips, null);
         ((TextView) loadingLayout.findViewById(R.id.center_tips_text_view)).setText(getResources().getString(R.string.loading_string));
         mPageAdapter.addView(loadingLayout);
         mViewPager.setAdapter(mPageAdapter);
         
         update();
         
         new Handler().postDelayed(new Runnable() 
         {
             @Override
             public void run() 
             {
                 AppEngine.getInstance().getAdManager().addAdView(ProgramActivity.this, R.id.adLayout, AdSize.MINI_SIZE);
             }
         }, 500);
     }
     
     public void onClick(View view)
     {
         switch (view.getId()) 
         {
             case R.id.episode_iv:
                 startEpisodeActivity();
                 break;
             default:
                 break;
         }
     }
 
     private void update()
     {
         sRequestId++;
         AppEngine.getInstance().getProgramHtmlManager().getProgramDetailAsync(sRequestId, mProgram.link, new ProgramHtmlManager.ProgramDetailCallback() 
         {
             @Override
             public void onTitleLoaded(int requestId, String title) 
             {
                 mTitle = title;
                 uiHandler.sendEmptyMessage(SelfMessage.MSG_TITLE_LOADED.ordinal());
             }
             
             @Override
             public void onSummaryLoaded(int requestId, String summary) 
             {
                 mSummary = summary;
                 uiHandler.sendEmptyMessage(SelfMessage.MSG_SUMMARY_LOADED.ordinal());
             }
             
             @Override
             public void onProfileLoaded(int requestId, String profile) 
             {
                 mProfile = profile;
                 uiHandler.sendEmptyMessage(SelfMessage.MSG_PROFILE_LOADED.ordinal());
             }
             
             @Override
             public void onPictureLinkParsed(int requestId, final String link) 
             {
                 new Thread(new Runnable() 
                 {
                     @Override
                     public void run() 
                     {
                         mPicture = Utility.getNetworkImage(link);
                         if (mPicture != null)
                             uiHandler.sendEmptyMessage(SelfMessage.MSG_PICTURE_LOADED.ordinal());
                     }
                 }).start();
             }
             
             @Override
             public void onEpisodeLinkParsed(int requestId, String link) 
             {
                 mEpisodeEntryLink = link;
                 uiHandler.sendEmptyMessage(SelfMessage.MSG_HAS_DETAIL_PLOTS.ordinal());
             }
         });
     }
     
     private void startEpisodeActivity()
     {
         if (mEpisodeEntryLink != null && !mEpisodeEntryLink.equals(""))
         {
             Intent intent = new Intent(ProgramActivity.this, EpisodeActivity.class);
             
             intent.putExtra("source", "tvmao");
             intent.putExtra("episode_entry_link", mEpisodeEntryLink);
             intent.putExtra("program_name", mTitle);
             startActivity(intent);
         }
     }
     
     private Handler uiHandler = new Handler()
     {
         public void handleMessage(Message msg)
         {
             super.handleMessage(msg);
             SelfMessage selfMsg = SelfMessage.values()[msg.what];
             switch (selfMsg)
             {
                 case MSG_TITLE_LOADED:
                     mProgramNameTextView.setText(mTitle);
                     break;
                 case MSG_SUMMARY_LOADED:
                     ((TextView) mSummaryLayout.findViewById(R.id.program_tab_simpletext)).setText(mSummary);
                     ((LinearLayout) mPageAdapter.getView(0)).removeAllViews();
                     ((LinearLayout) mPageAdapter.getView(0)).addView(mSummaryLayout, mCenterLayoutParams);
                     mViewPager.setCurrentItem(0);
                     break;
                 case MSG_PROFILE_LOADED:
                     mProgramProfileTextView.setText(mProfile);
                     if (mProfile.length() > 0)
                     {
                         ((ScrollView) findViewById(R.id.program_profile_scroll_view)).setVisibility(View.VISIBLE);
                         ((LinearLayout) findViewById(R.id.program_profile_loading_ll)).setVisibility(View.GONE);
                     }
                     else 
                     {
                         ((TextView) findViewById(R.id.program_profile_loading_tv)).setText(getResources().getString(R.string.no_data));
                     }
                     break;
                 case MSG_PICTURE_LOADED:
                     mProgramImageView.setImageBitmap(mPicture);
                     break;
                 case MSG_HAS_DETAIL_PLOTS:
                     mPlotsImageView.setVisibility(View.VISIBLE);
                    ((Button) mSummaryLayout.findViewById(R.id.more_plot_btn)).setVisibility(View.VISIBLE);
                    ((Button) mSummaryLayout.findViewById(R.id.more_plot_btn)).setOnClickListener(new View.OnClickListener() 
                    {                        
                        @Override
                        public void onClick(View view) 
                        {
                            startEpisodeActivity();
                        }
                    });
                     break;
             }
         }
     };
 }
