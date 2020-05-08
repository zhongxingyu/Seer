 package com.tools.tvguide.activities;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import com.tools.tvguide.R;
 import com.tools.tvguide.adapters.ChannellistAdapter;
 import com.tools.tvguide.adapters.ResultPageAdapter;
 import com.tools.tvguide.adapters.ResultProgramAdapter;
 import com.tools.tvguide.adapters.ResultProgramAdapter.Item;
 import com.tools.tvguide.adapters.ResultProgramAdapter.IListItem;
 import com.tools.tvguide.adapters.ResultProgramAdapter.LabelItem;
 import com.tools.tvguide.adapters.ResultProgramAdapter.ContentItem;
 import com.tools.tvguide.components.MyProgressDialog;
 import com.tools.tvguide.data.Channel;
 import com.tools.tvguide.data.Program;
 import com.tools.tvguide.managers.AppEngine;
 import com.tools.tvguide.managers.ContentManager;
 import com.tools.tvguide.managers.SearchWordsManager;
 import com.tools.tvguide.utils.Utility;
 import com.tools.tvguide.utils.XmlParser;
 import com.tools.tvguide.views.SearchHotwordsView;
 
 import android.os.Bundle;
 import android.os.Handler;
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
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class SearchActivity extends Activity 
 {
     private EditText mSearchEditText;
     private boolean mIsSelectAll = false;
     private String mKeyword;
     private List<IListItem> mItemProgramDataList;
     private List<HashMap<String, Object>> mItemChannelDataList; 
     private List<HashMap<String, String>> mOnPlayingProgramList;            // Key: id, title
     private LayoutInflater mInflater;
     private LinearLayout mContentLayout;
     private LinearLayout mOriginContentLayout;
     private LinearLayout mNoSearchResultLayout;
     private LinearLayout mClassifyResultLayout;
     private LinearLayout.LayoutParams mCenterLayoutParams;
     private RelativeLayout mCancelImage;
     private int mResultProgramsNum;
     private MyProgressDialog mProgressDialog;
     private ViewPager mViewPager;
     private ResultPageAdapter mResultPagerAdapter;
     private String mOriginChannelsFormatString;
     private String mOriginProgramsFormatString;
     private enum SelfMessage {MSG_SHOW_RESULT, MSG_REFRESH_ON_PLAYING_PROGRAM_LIST, MSG_SHOW_POP_SEARCH}
     private final int TAB_INDEX_CHANNELS = 0;
     private final int TAB_INDEX_PROGRAMS = 1;
     private List<String> mPopSearchList;
     
     @Override
     protected void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_search);
         
         mInflater = LayoutInflater.from(this);
         mSearchEditText = (EditText)findViewById(R.id.search_edit_text);
         mItemProgramDataList = new ArrayList<IListItem>();
         mItemChannelDataList = new ArrayList<HashMap<String,Object>>();
         mOnPlayingProgramList = new ArrayList<HashMap<String,String>>();
         mProgressDialog = new MyProgressDialog(this);
         mCancelImage = (RelativeLayout)findViewById(R.id.search_cancel_layout);
         mContentLayout = (LinearLayout)findViewById(R.id.search_content_layout);
         mOriginContentLayout = (LinearLayout)mInflater.inflate(R.layout.search_init_layout, null);
         mNoSearchResultLayout = (LinearLayout)mInflater.inflate(R.layout.center_text_tips, null); 
         mClassifyResultLayout = (LinearLayout)mInflater.inflate(R.layout.search_result_tabs, null);
         mCenterLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
         ((TextView) mNoSearchResultLayout.findViewById(R.id.center_tips_text_view)).setText(getResources().getString(R.string.no_found_tips));
         mOriginChannelsFormatString = ((RadioButton) mClassifyResultLayout.findViewById(R.id.result_channels)).getText().toString();
         mOriginProgramsFormatString = ((RadioButton) mClassifyResultLayout.findViewById(R.id.result_programs)).getText().toString();
         mViewPager = (ViewPager) mClassifyResultLayout.findViewById(R.id.search_view_pager);
         mResultPagerAdapter = new ResultPageAdapter();
         mPopSearchList = new ArrayList<String>();
         
         // NOTE：Should follow the TAB INDEX order at the beginning of the class
         ListView channelListView = (ListView) mInflater.inflate(R.layout.activity_channellist, null).findViewById(R.id.channel_list);
         ListView programListView = (ListView) mInflater.inflate(R.layout.search_programs_layout, null).findViewById(R.id.program_list_view);
         mResultPagerAdapter.addView(channelListView);
         mResultPagerAdapter.addView(programListView);
         mViewPager.setAdapter(mResultPagerAdapter);
         
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
                 // TODO Auto-generated method stub
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
         
         mViewPager.setOnPageChangeListener(new OnPageChangeListener() 
         {
             @Override
             public void onPageSelected(int position) 
             {
                 RadioGroup radioTabs = (RadioGroup) mClassifyResultLayout.findViewById(R.id.result_tabs);
                 if (position == TAB_INDEX_CHANNELS)
                     radioTabs.check(R.id.result_channels);
                 else if (position == TAB_INDEX_PROGRAMS)
                     radioTabs.check(R.id.result_programs);
             }
             
             @Override
             public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) 
             {
                 // TODO Auto-generated method stub
             }
             
             @Override
             public void onPageScrollStateChanged(int state) 
             {
                 // TODO Auto-generated method stub
             }
         });
         
         channelListView.setOnItemClickListener(new OnItemClickListener() 
         {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
             {
                 String channelId = (String) mItemChannelDataList.get(position).get("id");
                 String channelName = (String) mItemChannelDataList.get(position).get("name");
                 Intent intent = new Intent(SearchActivity.this, ChannelDetailActivity.class);
                 intent.putExtra("id", channelId);
                 intent.putExtra("name", channelName);
                 startActivity(intent);
             }
         });
         
         updateHistorySearch();
         updatePopSearch();
     }
     
     public void onClick(View view)
     {
         switch(view.getId())
         {
             case R.id.result_channels:
                 mViewPager.setCurrentItem(TAB_INDEX_CHANNELS);
                 break;
             case R.id.result_programs:
                 mViewPager.setCurrentItem(TAB_INDEX_PROGRAMS);
                 break;
         }
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
         mItemProgramDataList.clear();
         mItemChannelDataList.clear();
         final List<Channel> channels = new ArrayList<Channel>();
         final List<HashMap<String, Object>> programs = new ArrayList<HashMap<String,Object>>();
         AppEngine.getInstance().getContentManager().loadSearchResult(mKeyword, channels, programs, new ContentManager.LoadListener() 
         {
             @Override
             public void onLoadFinish(int status) 
             {
                 HashMap<String, HashMap<String, Object>> xmlChannelInfo = XmlParser.parseChannelInfo(SearchActivity.this);
                 for (int i=0; i<channels.size(); ++i)
                 {
                     HashMap<String, Object> item = new HashMap<String, Object>();
                     item.put("id", channels.get(i).id);
                     item.put("name", channels.get(i).name);
                     if (xmlChannelInfo.get(channels.get(i).id) != null)
                     {
                         item.put("image", Utility.getImage(SearchActivity.this, (String) xmlChannelInfo.get(channels.get(i).id).get("logo")));
                     }
                     mItemChannelDataList.add(item);
                 }
                 
                 for (int i=0; i<programs.size(); ++i)
                 {
                     Channel channel = (Channel) programs.get(i).get("channel");
                     List<Program> programList = (List<Program>) programs.get(i).get("programs");
                     if (channel == null || programList == null)
                         continue;
                     
                     mItemProgramDataList.add(new LabelItem(channel.name, R.layout.hot_channel_item, R.id.hot_channel_name_tv));
                     for (int j=0; j<programList.size(); ++j)
                     {
                         Item item = new Item();
                         item.id = channel.id;
                         item.name = channel.name;
                         item.time = programList.get(j).time;
                         item.title = programList.get(j).title;
                         item.key = mKeyword;
                         item.hasLink = false;
                         mItemProgramDataList.add(new ContentItem(item, R.layout.hot_program_item, R.id.hot_program_name_tv));
                     }
                     mResultProgramsNum += programList.size();
                 }
                 uiHandler.sendEmptyMessage(SelfMessage.MSG_SHOW_RESULT.ordinal());
             }
         });
         mProgressDialog.show();
     }
     
     private void updateOnPlayingProgramList()
     {
         mOnPlayingProgramList.clear();
         List<String> idList = new ArrayList<String>();
         for (int i=0; i<mItemChannelDataList.size(); ++i)
         {
             idList.add((String) mItemChannelDataList.get(i).get("id"));
         }
         AppEngine.getInstance().getContentManager().loadOnPlayingPrograms(idList, mOnPlayingProgramList, new ContentManager.LoadListener() 
         {    
             @Override
             public void onLoadFinish(int status) 
             {
                 uiHandler.sendEmptyMessage(SelfMessage.MSG_REFRESH_ON_PLAYING_PROGRAM_LIST.ordinal());
             }
         });
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
             uiHandler.sendEmptyMessage(SelfMessage.MSG_SHOW_POP_SEARCH.ordinal());
         }
         else
         {
             AppEngine.getInstance().getSearchWordsManager().updatePopSearch(new SearchWordsManager.UpdateListener() 
             {
                 @Override
                 public void onUpdateFinish(List<String> result) 
                 {
                     mPopSearchList.addAll(result);
                     uiHandler.sendEmptyMessage(SelfMessage.MSG_SHOW_POP_SEARCH.ordinal());
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
     
     private Handler uiHandler = new Handler()
     {
         public void handleMessage(Message msg)
         {
             super.handleMessage(msg);
             SelfMessage selfMsg = SelfMessage.values()[msg.what];
             switch (selfMsg) 
             {
 			    case MSG_SHOW_RESULT:
 					mProgressDialog.dismiss();
 					// 数据拷贝，防止Crash: "Make sure the content of your adapter is not modified from a background thread, but only from the UI thread"
 					List<HashMap<String, Object>> itemChannelList = new ArrayList<HashMap<String,Object>>();
 					itemChannelList.addAll(mItemChannelDataList);
 					List<IListItem> itemProgramList = new ArrayList<ResultProgramAdapter.IListItem>();
 					itemProgramList.addAll(mItemProgramDataList);
 					
 					((ListView)mResultPagerAdapter.getView(TAB_INDEX_CHANNELS)).setAdapter(new ChannellistAdapter(SearchActivity.this, itemChannelList));
 					((ListView)mResultPagerAdapter.getView(TAB_INDEX_PROGRAMS)).setAdapter(new ResultProgramAdapter(SearchActivity.this, itemProgramList));
 		            mSearchEditText.requestFocus();
 		            if (itemChannelList.isEmpty() && itemProgramList.isEmpty())
 		            {
 		                mContentLayout.removeAllViews();
 		                mContentLayout.addView(mNoSearchResultLayout, mCenterLayoutParams);
 		            }
 		            else
 		            {
 		                RadioButton channelsBtn = (RadioButton) mClassifyResultLayout.findViewById(R.id.result_channels);
 	                    RadioButton programsBtn = (RadioButton) mClassifyResultLayout.findViewById(R.id.result_programs);
 	                    channelsBtn.setText(String.format(mOriginChannelsFormatString, mItemChannelDataList.size()));
 	                    programsBtn.setText(String.format(mOriginProgramsFormatString, mResultProgramsNum));
 		                
 		                if (!itemChannelList.isEmpty())
 		                {
 	                        mViewPager.setCurrentItem(0);
 	                        ((RadioGroup) mClassifyResultLayout.findViewById(R.id.result_tabs)).check(R.id.result_channels);
 		                }
 		                else 
 		                {
 		                    mViewPager.setCurrentItem(1);
 		                    ((RadioGroup) mClassifyResultLayout.findViewById(R.id.result_tabs)).check(R.id.result_programs);
                         }
 		                
 		                mContentLayout.removeAllViews();
 		                mContentLayout.addView(mClassifyResultLayout, mCenterLayoutParams);
 		                
 		                if (!itemChannelList.isEmpty())
 		                	updateOnPlayingProgramList();
 		            }
 		            updateHistorySearch();
 					break;
 				
 				case MSG_REFRESH_ON_PLAYING_PROGRAM_LIST:
 					if (mOnPlayingProgramList != null)
                     {
 					    List<HashMap<String, Object>> itemChannelList1 = new ArrayList<HashMap<String,Object>>();
 					    itemChannelList1.addAll(mItemChannelDataList);
                         for (int i=0; i<itemChannelList1.size(); ++i)
                         {
                             for (int j=0; j<mOnPlayingProgramList.size(); ++j)
                             {
                                 if (itemChannelList1.get(i).get("id").equals(mOnPlayingProgramList.get(j).get("id")))
                                 {
                                     itemChannelList1.get(i).put("program", "正在播出：" + mOnPlayingProgramList.get(j).get("title"));
                                 }
                             }
                         }
                         ((ListView)mResultPagerAdapter.getView(TAB_INDEX_CHANNELS)).setAdapter(new ChannellistAdapter(SearchActivity.this, itemChannelList1));
                     }
 					break;
 				case MSG_SHOW_POP_SEARCH:
 				    ((SearchHotwordsView) mOriginContentLayout.findViewById(R.id.search_hotwords_view)).setWords(mPopSearchList.toArray(new String[0]));
 				    break;
 				default:
 					break;
 			}
         }
     };
     
     
 }
