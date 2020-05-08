 package com.tools.tvguide.activities;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import com.tools.tvguide.R;
 import com.tools.tvguide.adapters.HotProgramListAdapter;
 import com.tools.tvguide.adapters.ResultPageAdapter;
 import com.tools.tvguide.data.Program;
 import com.tools.tvguide.managers.AppEngine;
 import com.tools.tvguide.managers.UrlManager;
 import com.tools.tvguide.managers.ProgramHtmlManager.HotProgramsCallback;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Handler.Callback;
 import android.os.Message;
 import android.app.Activity;
 import android.content.Intent;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class HotActivity extends Activity implements Callback 
 {
     private LayoutInflater mInflater;
     private ViewPager mViewPager;
     private RadioGroup mTabsGroup;
     private ResultPageAdapter mPageAdapter;
     private HashMap<TabIndex, List<HashMap<String, String>>> mProgramInfoListMap;
     private HashMap<TabIndex, LinearLayout> mClassifyLayoutMap;
     private Handler mUiHandler;
     
     enum TabIndex {Drama, Tvcolumn, Movie}
     
     @Override
     protected void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_hot);
         
         mInflater = LayoutInflater.from(this);
         mViewPager = (ViewPager) findViewById(R.id.hot_view_pager);
         mTabsGroup = (RadioGroup) findViewById(R.id.hot_tabs_group);
         mProgramInfoListMap = new HashMap<TabIndex, List<HashMap<String,String>>>();
         mClassifyLayoutMap = new HashMap<TabIndex, LinearLayout>();
         mUiHandler = new Handler(this);
         
         mPageAdapter = new ResultPageAdapter();
         for (int i=0; i<mTabsGroup.getChildCount(); ++i)
         {
             if (mTabsGroup.getChildAt(i) instanceof RadioButton)
             {
                 LinearLayout loadingLayout = (LinearLayout)mInflater.inflate(R.layout.center_text_tips_layout, null);
                 ((TextView) loadingLayout.findViewById(R.id.center_tips_text_view)).setText(getResources().getString(R.string.loading_string));
                 mPageAdapter.addView(loadingLayout);
                 
                 mClassifyLayoutMap.put(TabIndex.values()[mPageAdapter.getCount() - 1], (LinearLayout) mInflater.inflate(R.layout.hot_program_layout, null));
             }
         }
         mViewPager.setAdapter(mPageAdapter);
         
         mViewPager.setOnPageChangeListener(new OnPageChangeListener() 
         {
             @Override
             public void onPageSelected(int position) 
             {
                 if (position == TabIndex.Drama.ordinal())
                     mTabsGroup.check(R.id.hot_tab_drama);
                 else if (position == TabIndex.Tvcolumn.ordinal())
                     mTabsGroup.check(R.id.hot_tab_tvcolumn);
                 else if (position == TabIndex.Movie.ordinal())
                     mTabsGroup.check(R.id.hot_tab_movie);
                 update();
             }
             
             @Override
             public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) 
             {
             }
             
             @Override
             public void onPageScrollStateChanged(int state) 
             {
             }
         });
         
         update();
     }
 
     public void onClickTabs(View view)
     {
         switch (view.getId())
         {
             case R.id.hot_tab_drama:
                 mViewPager.setCurrentItem(TabIndex.Drama.ordinal());
                 update();
                 break;
             case R.id.hot_tab_tvcolumn:
                 mViewPager.setCurrentItem(TabIndex.Tvcolumn.ordinal());
                 update();
                 break;
             case R.id.hot_tab_movie:
                 mViewPager.setCurrentItem(TabIndex.Movie.ordinal());
                 update();
                 break;
         }
     }
     
     private void update()
     {
         final TabIndex curIndex = TabIndex.values()[mViewPager.getCurrentItem()];
         String hotUrl = "";
         switch (curIndex)
         {
             case Drama:
                 hotUrl = UrlManager.URL_HOT_DRAMA;
                 break;
             case Tvcolumn:
                 hotUrl = UrlManager.URL_HOT_TVCOLUMN;
                 break;
             case Movie:
                 hotUrl = UrlManager.URL_HOT_MOVIE;
                 break;
         }
         
         if (mProgramInfoListMap.get(curIndex) == null)       // Not loading yet
         {
             AppEngine.getInstance().getProgramHtmlManager().getHotProgramsAsync(0, hotUrl, new HotProgramsCallback() 
             {
                 @Override
                 public void onProgramsLoaded(int requestId, List<HashMap<String, String>> programInfoList) 
                 {
                     if (!programInfoList.isEmpty())
                     {
                         List<HashMap<String, String>> tmpProgramInfoList = new ArrayList<HashMap<String,String>>();
                         tmpProgramInfoList.addAll(programInfoList);
                         mProgramInfoListMap.put(curIndex, tmpProgramInfoList);
                         mUiHandler.sendEmptyMessage(0);
                     }
                 }
             });
         }
        else 
        {
            mUiHandler.sendEmptyMessage(0);
        }
     }
 
     @Override
     public boolean handleMessage(Message msg) 
     {
         switch (msg.what) 
         {
             case 0:
                 TabIndex curIndex = TabIndex.values()[mViewPager.getCurrentItem()];
                if (mProgramInfoListMap.get(curIndex) == null)
                    break;
                
                 LinearLayout layout = mClassifyLayoutMap.get(curIndex);
                 ListView hotProgramListView = (ListView) layout.findViewById(R.id.hot_program_listview);
                if (hotProgramListView.getAdapter() != null)    // 已经设置并展示了页面
                    break;
                
                HotProgramListAdapter adapter = new HotProgramListAdapter(HotActivity.this, mProgramInfoListMap.get(curIndex));
                 hotProgramListView.setAdapter(adapter);
                 
                 hotProgramListView.setOnItemClickListener(new OnItemClickListener() 
                 {
                     @Override
                     public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
                     {
                         HashMap<String, String> programInfo = (HashMap<String, String>) parent.getItemAtPosition(position);
                         if (programInfo == null)
                             return;
                         
                         String name = programInfo.get("name");
                         String link = programInfo.get("link");
                         
                         TabIndex curIndex = TabIndex.values()[mViewPager.getCurrentItem()];
                         if (curIndex == TabIndex.Drama || curIndex == TabIndex.Movie)
                             link = link.replace("m.tvmao.com", "www.tvmao.com");
                         else if (curIndex == TabIndex.Tvcolumn)
                             link = link.replace("www.tvmao.com", "m.tvmao.com");
                         
                         Intent intent = new Intent(HotActivity.this, ProgramActivity.class);
                         Program program = new Program();
                         program.title = name;
                         program.link = link;
                         intent.putExtra("program", (Serializable) program);
                         startActivity(intent);
                     }
                 });
                 
                 ((LinearLayout) mPageAdapter.getView(mViewPager.getCurrentItem())).removeAllViews();
                 ((LinearLayout) mPageAdapter.getView(mViewPager.getCurrentItem())).addView(layout);
                 break;
         }
         return true;
     }
 }
