 package com.tools.tvguide.activities;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import com.tools.tvguide.R;
 import com.tools.tvguide.adapters.ChannellistAdapter2;
 import com.tools.tvguide.adapters.HotProgramListAdapter;
 import com.tools.tvguide.adapters.ResultPageAdapter;
 import com.tools.tvguide.adapters.ResultProgramAdapter;
 import com.tools.tvguide.adapters.ResultProgramAdapter.Item;
 import com.tools.tvguide.adapters.ResultProgramAdapter.IListItem;
 import com.tools.tvguide.adapters.ResultProgramAdapter.LabelItem;
 import com.tools.tvguide.adapters.ResultProgramAdapter.ContentItem;
 import com.tools.tvguide.components.MyProgressDialog;
 import com.tools.tvguide.data.Channel;
 import com.tools.tvguide.data.Program;
 import com.tools.tvguide.data.SearchResultCategory;
 import com.tools.tvguide.data.SearchResultDataEntry;
 import com.tools.tvguide.data.SearchResultCategory.Type;
 import com.tools.tvguide.managers.AppEngine;
 import com.tools.tvguide.managers.SearchHtmlManager.SearchResultCallback;
 import com.tools.tvguide.managers.SearchWordsManager;
 import com.tools.tvguide.utils.HtmlUtils;
 import com.tools.tvguide.views.MyViewPagerIndicator;
 import com.tools.tvguide.views.SearchHotwordsView;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Handler.Callback;
 import android.os.Message;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class SearchActivity extends Activity implements Callback 
 {
     private static int sRequest = 0;
     private EditText mSearchEditText;
     private boolean mIsSelectAll = false;
     private String mKeyword;
     private LayoutInflater mInflater;
     private LinearLayout mContentLayout;
     private LinearLayout mOriginContentLayout;
     private LinearLayout mNoSearchResultLayout;
     private LinearLayout mClassifyResultLayout;
     private LinearLayout.LayoutParams mCenterLayoutParams;
     private RelativeLayout mCancelImage;
     private MyProgressDialog mProgressDialog;
     private ViewPager mViewPager;
     private List<IListItem> mItemProgramDataList    = new ArrayList<IListItem>();; 
     private ResultPageAdapter mResultPagerAdapter   = new ResultPageAdapter();;
     private List<String> mPopSearchList             = new ArrayList<String>();;
     private List<Channel> mChannelList              = new ArrayList<Channel>();
     private List<SearchResultCategory> mCategoryList = new ArrayList<SearchResultCategory>();;
     private List<HashMap<String, String>> mTvcolumnList = new ArrayList<HashMap<String,String>>();
     private List<HashMap<String, String>> mMovieList = new ArrayList<HashMap<String,String>>();
     private List<HashMap<String, String>> mDramaList = new ArrayList<HashMap<String,String>>();
     private ResultProgramAdapter mScheduleAdapter;
     private enum SelfMessage {MSG_SHOW_POP_SEARCH, MSG_SHOW_CATEGORY, MSG_SHOW_CHANNEL,
             MSG_SHOW_TVCOLUMN, MSG_SHOW_MOVIE, MSG_SHOW_DRAMA, MSG_SHOW_SCHEDULE}
     private Handler mUiHandler;
     
     @Override
     protected void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_search);
         
         mInflater = LayoutInflater.from(this);
         mSearchEditText = (EditText)findViewById(R.id.search_edit_text);
         mProgressDialog = new MyProgressDialog(this);
         mCancelImage = (RelativeLayout)findViewById(R.id.search_cancel_layout);
         mContentLayout = (LinearLayout)findViewById(R.id.search_content_layout);
         mOriginContentLayout = (LinearLayout)mInflater.inflate(R.layout.search_init_layout, null);
         mNoSearchResultLayout = (LinearLayout)mInflater.inflate(R.layout.center_text_tips_layout, null); 
         mClassifyResultLayout = (LinearLayout)mInflater.inflate(R.layout.search_result_tabs, null);
         mCenterLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
         ((TextView) mNoSearchResultLayout.findViewById(R.id.center_tips_text_view)).setText(getResources().getString(R.string.no_found_tips));
         mViewPager = (ViewPager) mClassifyResultLayout.findViewById(R.id.search_view_pager);
         mUiHandler = new Handler(this);
         mScheduleAdapter = new ResultProgramAdapter(this);
         
         initContentLayout();
         ((SearchHotwordsView) mOriginContentLayout.findViewById(R.id.search_hotwords_view)).setOnItemClickListener(new SearchHotwordsView.OnItemClickListener() 
         {
             @Override
             public void onItemClick(String string) 
             {
                 mKeyword = string;
                 mSearchEditText.setText(mKeyword);
                 mSearchEditText.setSelection(mSearchEditText.getText().length());
                 updateSearchResult();
             }
         });
         ((SearchHotwordsView) mOriginContentLayout.findViewById(R.id.history_search_view)).setOnItemClickListener(new SearchHotwordsView.OnItemClickListener() 
         {
             @Override
             public void onItemClick(String string) 
             {
                 mKeyword = string;
                 mSearchEditText.setText(mKeyword);
                 mSearchEditText.setSelection(mSearchEditText.getText().length());
                 updateSearchResult();
             }
         });
         ((Button) mOriginContentLayout.findViewById(R.id.history_clear_btn)).setOnClickListener(new View.OnClickListener() 
         {
 			@Override
 			public void onClick(View v) 
 			{
 				AppEngine.getInstance().getSearchWordsManager().clearHistorySearch();
 				updateHistorySearch();
 			}
 		});
         
         mSearchEditText.setOnTouchListener(new View.OnTouchListener() 
         {
             @Override
             public boolean onTouch(View v, MotionEvent event) 
             {
                 if (v.getId() == mSearchEditText.getId())
                 {
                     if (event.getAction() != MotionEvent.ACTION_DOWN)
                     {
                         return true;
                     }
                     
                     if (mIsSelectAll == true)
                     {
                         mSearchEditText.setSelection(mSearchEditText.getText().length());
                         mIsSelectAll = false;
                     }
                     else if (mSearchEditText.getText().length() > 0)
                     {
                         mSearchEditText.selectAll();
                         mIsSelectAll = true;
                     }
                     showInputKeyboard();
                     return true;
                 }
                 return false;
             }
         });
         
         mSearchEditText.setOnKeyListener(new View.OnKeyListener() 
         {
             @Override
             public boolean onKey(View v, int keyCode, KeyEvent event) 
             {
                 if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)
                 {
                     search(v);
                     return true;
                 }
                 return false;
             }
         });
         
         mSearchEditText.addTextChangedListener(new TextWatcher() 
         {
             @Override
             public void onTextChanged(CharSequence s, int start, int before, int count) 
             {
                 mIsSelectAll = false;
             }
             
             @Override
             public void beforeTextChanged(CharSequence s, int start, int count, int after) 
             {
             }
             
             @Override
             public void afterTextChanged(Editable editable) 
             {
                 if (editable.toString().trim().length() > 0)
                 {
                     if (mCancelImage.getVisibility() != View.VISIBLE)
                         mCancelImage.setVisibility(View.VISIBLE);
                 }
                 else
                 {
                     if (mCancelImage.getVisibility() == View.VISIBLE)
                         mCancelImage.setVisibility(View.GONE);
                     initContentLayout();
                 }
             }
         });
         
         MyViewPagerIndicator indicator = (MyViewPagerIndicator) mClassifyResultLayout.findViewById(R.id.indicator);
         indicator.setOnTabClickListener(new MyViewPagerIndicator.OnTabClickListener() 
         {
             @Override
             public void onTabClick(int index, Object tag) 
             {
                 mViewPager.setCurrentItem(index);
             }
         });
         
         mViewPager.setOnPageChangeListener(new OnPageChangeListener() 
         {
             @Override
             public void onPageSelected(int position) 
             {
                 MyViewPagerIndicator indicator = (MyViewPagerIndicator) mClassifyResultLayout.findViewById(R.id.indicator);
                 indicator.setCurrentTab(position);
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
         
         updateHistorySearch();
         updatePopSearch();
     }
     
     public void search(View view)
     {
         hideInputKeyboard();
         if (mSearchEditText.getText().toString().trim().equals(""))
         {
             Toast.makeText(this, "请输入搜索关键字！", Toast.LENGTH_SHORT).show();
             return;
         }
         mKeyword = mSearchEditText.getText().toString().trim().split(" ")[0];
         updateSearchResult();
     }
     
     public void cancel(View view)
     {
         mSearchEditText.setText("");
         initContentLayout();
     }
     
     private void initContentLayout()
     {
         mContentLayout.removeAllViews();
         mContentLayout.addView(mOriginContentLayout, mCenterLayoutParams);
     }
     
     private void updateSearchResult()
     {
         AppEngine.getInstance().getSearchWordsManager().addSearchRecord(mKeyword);
         mChannelList.clear();
         mDramaList.clear();
         mTvcolumnList.clear();
         mMovieList.clear();
         mItemProgramDataList.clear();
         sRequest++;
         
         AppEngine.getInstance().getSearchHtmlManager().search(sRequest, mKeyword, new SearchResultCallback() 
         {
             @Override
             public void onCategoriesLoaded(int requestId, List<SearchResultCategory> categoryList) 
             {
                 if (requestId != sRequest)
                     return;
                 
                 if (categoryList != null)
                 {
                     mCategoryList.clear();
                     mCategoryList.addAll(categoryList);
                     mUiHandler.obtainMessage(SelfMessage.MSG_SHOW_CATEGORY.ordinal()).sendToTarget();
                 }
             }
             
             @Override
             public void onEntriesLoaded(int requestId, Type categoryType, List<SearchResultDataEntry> entryList) 
             {
                 if (requestId != sRequest)
                     return;
                 
                 if (categoryType == Type.Channel)
                 {
                     for (int i=0; i<entryList.size(); ++i)
                     {
                         Channel channel = new Channel();
                         channel.name = entryList.get(i).name;
                         channel.tvmaoId = HtmlUtils.filterTvmaoId(entryList.get(i).detailLink);
                         channel.tvmaoLink = entryList.get(i).detailLink;
                         channel.logoLink = entryList.get(i).imageLink;
                         mChannelList.add(channel);
                     }
                     mUiHandler.obtainMessage(SelfMessage.MSG_SHOW_CHANNEL.ordinal()).sendToTarget();
                 } 
                 else if (categoryType == Type.Tvcolumn)
                 {
                     for (int i=0; i<entryList.size(); ++i)
                     {
                         HashMap<String, String> tvcolumn = new HashMap<String, String>();
                         tvcolumn.put("name", entryList.get(i).name);
                         tvcolumn.put("profile", entryList.get(i).profile);
                         tvcolumn.put("link", entryList.get(i).detailLink);
                         tvcolumn.put("picture_link", entryList.get(i).imageLink);
                         mTvcolumnList.add(tvcolumn);
                     }
                     mUiHandler.obtainMessage(SelfMessage.MSG_SHOW_TVCOLUMN.ordinal()).sendToTarget();
                 }
                 else if (categoryType == Type.Movie)
                 {
                     for (int i=0; i<entryList.size(); ++i)
                     {
                         HashMap<String, String> movie = new HashMap<String, String>();
                         movie.put("name", entryList.get(i).name);
                         movie.put("profile", entryList.get(i).profile);
                         movie.put("link", entryList.get(i).detailLink);
                         movie.put("picture_link", entryList.get(i).imageLink);
                         mMovieList.add(movie);
                     }
                     mUiHandler.obtainMessage(SelfMessage.MSG_SHOW_MOVIE.ordinal()).sendToTarget();
                 }
                 else if (categoryType == Type.Drama)
                 {
                     for (int i=0; i<entryList.size(); ++i)
                     {
                         HashMap<String, String> drama = new HashMap<String, String>();
                         drama.put("name", entryList.get(i).name);
                         drama.put("profile", entryList.get(i).profile);
                         drama.put("link", entryList.get(i).detailLink);
                         drama.put("picture_link", entryList.get(i).imageLink);
                         mDramaList.add(drama);
                     }
                     mUiHandler.obtainMessage(SelfMessage.MSG_SHOW_DRAMA.ordinal()).sendToTarget();
                 }
             }
             
             @Override
             public void onProgramScheduleLoadeded(int requestId, int pageIndex, List<HashMap<String, Object>> scheduleList) 
             {
                 if (requestId != sRequest)
                     return;
                 
                 for (int i=0; i<scheduleList.size(); ++i)
                 {
                     Channel channel = (Channel) scheduleList.get(i).get("channel");
                     List<Program> programList = (List<Program>) scheduleList.get(i).get("programs");
                     if (channel == null || programList == null)
                         continue;
                     
                     mItemProgramDataList.add(new LabelItem(channel.name, R.layout.hot_channel_tvsou_item, R.id.hot_channel_name_tv));
                     for (int j=0; j<programList.size(); ++j)
                     {
                         Item item = new Item();
                         item.id = channel.id;
                         item.name = channel.name;
                         item.time = programList.get(j).date + " " + programList.get(j).time;
                         item.title = programList.get(j).title;
                         item.key = mKeyword;
                         item.hasLink = false;
                         mItemProgramDataList.add(new ContentItem(item, R.layout.hot_program_tvsou_item, R.id.hot_program_name_tv));
                     }
                 }
                 mUiHandler.obtainMessage(SelfMessage.MSG_SHOW_SCHEDULE.ordinal()).sendToTarget();
             }
         });
         mProgressDialog.show();
     }
     
     private void updateHistorySearch()
     {
         SearchWordsManager manager = AppEngine.getInstance().getSearchWordsManager();
         SearchHotwordsView historySearchView = (SearchHotwordsView) mOriginContentLayout.findViewById(R.id.history_search_view);
         RelativeLayout historySearchTipsLayout = (RelativeLayout) mOriginContentLayout.findViewById(R.id.history_search_tips_layout);
         if (manager.getHistorySearch().isEmpty())
         {
         	historySearchTipsLayout.setVisibility(View.GONE);
             historySearchView.setVisibility(View.GONE);
         }
         else
         {
         	historySearchTipsLayout.setVisibility(View.VISIBLE);
             historySearchView.setVisibility(View.VISIBLE);
             historySearchView.setWords(manager.getHistorySearch().toArray(new String[0]));
         }
     }
     
     private void updatePopSearch()
     {
         mPopSearchList.clear();
         mPopSearchList = AppEngine.getInstance().getSearchWordsManager().getPopSearch();
         if (mPopSearchList.size() > 0)
         {
             mUiHandler.sendEmptyMessage(SelfMessage.MSG_SHOW_POP_SEARCH.ordinal());
         }
         else
         {
             AppEngine.getInstance().getSearchWordsManager().updatePopSearch(new SearchWordsManager.UpdateListener() 
             {
                 @Override
                 public void onUpdateFinish(List<String> result) 
                 {
                     mPopSearchList.addAll(result);
                     mUiHandler.sendEmptyMessage(SelfMessage.MSG_SHOW_POP_SEARCH.ordinal());
                 }
             });
         }
     }
     
     private void showInputKeyboard()
     {
         InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
         imm.showSoftInput(mSearchEditText, 0);
     }
     
     private void hideInputKeyboard()
     {
         InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
         imm.hideSoftInputFromWindow(mSearchEditText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
     }
     
     private void setPageAdapterView(int index, View view)
     {
         ((LinearLayout) mResultPagerAdapter.getView(index)).removeAllViews();
         ((LinearLayout) mResultPagerAdapter.getView(index)).addView(view);
     }
 
     @Override
     public boolean handleMessage(Message msg) 
     {
         SelfMessage selfMsg = SelfMessage.values()[msg.what];
         switch (selfMsg) 
         {
             case MSG_SHOW_POP_SEARCH:
                 ((SearchHotwordsView) mOriginContentLayout.findViewById(R.id.search_hotwords_view)).setWords(mPopSearchList.toArray(new String[0]));
                 break;
             case MSG_SHOW_CATEGORY:
                 mProgressDialog.dismiss();
                 MyViewPagerIndicator indicator = (MyViewPagerIndicator) mClassifyResultLayout.findViewById(R.id.indicator);
                 indicator.reset();
                 mViewPager.removeAllViews();
                 mResultPagerAdapter = new ResultPageAdapter();
                 
                 if (mCategoryList.isEmpty())
                 {
                     mContentLayout.removeAllViews();
                     mContentLayout.addView(mNoSearchResultLayout, mCenterLayoutParams);
                     break;
                 }
                 
                 for (int i=0; i<mCategoryList.size(); ++i)
                 {
                     indicator.addTab(mCategoryList.get(i).name, null);
                     LinearLayout loadingLayout = (LinearLayout)mInflater.inflate(R.layout.center_text_tips_layout, null);
                     ((TextView) loadingLayout.findViewById(R.id.center_tips_text_view)).setText(getResources().getString(R.string.loading_string));
                     mResultPagerAdapter.addView(loadingLayout);
                 }
                 indicator.setCurrentTab(0);
                 mViewPager.setAdapter(mResultPagerAdapter);
                 mContentLayout.removeAllViews();
                 mContentLayout.addView(mClassifyResultLayout, mCenterLayoutParams);
                 updateHistorySearch();
                 break;
             case MSG_SHOW_CHANNEL:
                 int tabIndex = getCategoryTypeIndex(SearchResultCategory.Type.Channel);
                 if (tabIndex != -1)
                 {
                     View channelLayout = mInflater.inflate(R.layout.channel_listview, null);
                     ListView channelListView = (ListView) channelLayout.findViewById(R.id.channel_lv);
                     channelListView.setOnItemClickListener(new OnItemClickListener() 
                     {
                         @Override
                         public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
                         {
                            Intent intent = new Intent(SearchActivity.this, ChannelDetailActivity.class);
                             intent.putExtra("tvmao_id", mChannelList.get(position).tvmaoId);
                             intent.putExtra("name", mChannelList.get(position).name);
                             intent.putExtra("channel_list", (Serializable) mChannelList);
                             startActivity(intent);
                         }
                     });
                     channelListView.setAdapter(new ChannellistAdapter2(SearchActivity.this, mChannelList));
                     setPageAdapterView(tabIndex, channelLayout);
                 }
                 break;
             case MSG_SHOW_TVCOLUMN:
                 int tvcolumnTabIndex = getCategoryTypeIndex(SearchResultCategory.Type.Tvcolumn);
                 if (tvcolumnTabIndex != -1)
                 {
                     View tvcolumnLayout = mInflater.inflate(R.layout.hot_program_layout, null);
                     ListView tvcolumnListView = (ListView) tvcolumnLayout.findViewById(R.id.hot_program_listview);
                     tvcolumnListView.setOnItemClickListener(new OnItemClickListener() {
                         @Override
                         public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
                         {
                             String link = mTvcolumnList.get(position).get("link");
                             if (link == null || link.trim().length() == 0)
                                 return;
                             
                             link = link.replace("www.tvmao.com", "m.tvmao.com");    // for tvcolumn
                             Intent intent = new Intent(SearchActivity.this, ProgramActivity.class);
                             Program program = new Program();
                             program.title = mTvcolumnList.get(position).get("name");
                             program.link = link;
                             intent.putExtra("program", (Serializable) program);
                             startActivity(intent);
                         }
                     });
                     tvcolumnListView.setAdapter(new HotProgramListAdapter(SearchActivity.this, mTvcolumnList));
                     setPageAdapterView(tvcolumnTabIndex, tvcolumnLayout);
                 }
                 break;
             case MSG_SHOW_MOVIE:
                 int movieTabIndex = getCategoryTypeIndex(SearchResultCategory.Type.Movie);
                 if (movieTabIndex != -1)
                 {
                     View movieLayout = mInflater.inflate(R.layout.hot_program_layout, null);
                     ListView movieListView = (ListView) movieLayout.findViewById(R.id.hot_program_listview);
                     movieListView.setAdapter(new HotProgramListAdapter(SearchActivity.this, mMovieList));
                     movieListView.setOnItemClickListener(new OnItemClickListener() 
                     {
                         @Override
                         public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
                         {
                             String link = mMovieList.get(position).get("link");
                             if (link == null || link.trim().length() == 0)
                                 return;
                             
                             link = link.replace("m.tvmao.com", "www.tvmao.com");    // for movie
                             Intent intent = new Intent(SearchActivity.this, ProgramActivity.class);
                             Program program = new Program();
                             program.title = mMovieList.get(position).get("name");
                             program.link = link;
                             intent.putExtra("program", (Serializable) program);
                             startActivity(intent);
                         }
                     });
                     setPageAdapterView(movieTabIndex, movieLayout);
                 }
                 break;
             case MSG_SHOW_DRAMA:
                 int dramaTabIndex = getCategoryTypeIndex(SearchResultCategory.Type.Drama);
                 if (dramaTabIndex != -1)
                 {
                     View dramaLayout = mInflater.inflate(R.layout.hot_program_layout, null);
                     ListView dramaListView = (ListView) dramaLayout.findViewById(R.id.hot_program_listview);
                     dramaListView.setAdapter(new HotProgramListAdapter(SearchActivity.this, mDramaList));
                     dramaListView.setOnItemClickListener(new OnItemClickListener() 
                     {
                         @Override
                         public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
                         {
                             String link = mDramaList.get(position).get("link");
                             if (link == null || link.trim().length() == 0)
                                 return;
                             
                             link = link.replace("m.tvmao.com", "www.tvmao.com");    // for drama
                             Intent intent = new Intent(SearchActivity.this, ProgramActivity.class);
                             Program program = new Program();
                             program.title = mDramaList.get(position).get("name");
                             program.link = link;
                             intent.putExtra("program", (Serializable) program);
                             startActivity(intent);
                         }
                     });
                     setPageAdapterView(dramaTabIndex, dramaLayout);
                 }
                 break;
             case MSG_SHOW_SCHEDULE:
                 int scheduleTabIndex = getCategoryTypeIndex(SearchResultCategory.Type.ProgramSchedule);
                 if (scheduleTabIndex != -1)
                 {
                     View scheduleLayout = mInflater.inflate(R.layout.search_programs_layout, null);
                     ListView scheduleListView = (ListView) scheduleLayout.findViewById(R.id.program_list_view);
                     if (scheduleListView.getAdapter() == null)
                     {
                         scheduleListView.setAdapter(mScheduleAdapter);
                         setPageAdapterView(scheduleTabIndex, scheduleLayout);
                     }
                     mScheduleAdapter.updateItemList(mItemProgramDataList);
                 }
                 break;
             default:
                 break;
         }
         return true;
     }
     
     private int getCategoryTypeIndex(SearchResultCategory.Type type)
     {
         int result = -1;
         for (int i=0; i<mCategoryList.size(); ++i)
         {
             if (mCategoryList.get(i).type == type)
             {
                 result = i;
                 break;
             }
         }
         return result;
     }
 }
