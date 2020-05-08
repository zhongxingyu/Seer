 package com.tools.tvguide.activities;
 
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.tools.tvguide.R;
 import com.tools.tvguide.components.DefaultNetDataGetter;
 import com.tools.tvguide.components.MyProgressDialog;
 import com.tools.tvguide.managers.AppEngine;
 import com.tools.tvguide.managers.UrlManager;
 import com.tools.tvguide.utils.NetDataGetter;
 import com.tools.tvguide.utils.NetworkManager;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Color;
 import android.text.Editable;
 import android.text.SpannableString;
 import android.text.Spanned;
 import android.text.TextWatcher;
 import android.text.style.ForegroundColorSpan;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.BaseAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class SearchActivity extends Activity 
 {
     private EditText mSearchEditText;
     private boolean mIsSelectAll = false;
     private ListView mListView;
     private BaseAdapter mListViewAdapter;
     private String mKeyword;
     private ArrayList<IListItem> mItemList;
     private ArrayList<IListItem> mItemDataList;
     private LayoutInflater mInflater;
     private Handler mUpdateHandler;
     private MyProgressDialog mProgressDialog;
     
     class PartAdapter extends BaseAdapter 
     {
         @Override
         public int getCount() 
         {
             return mItemList.size();
         }
 
         @Override
         public Object getItem(int position) 
         {
             return mItemList.get(position);
         }
 
         @Override
         public long getItemId(int position) 
         {
             return position;
         }
         
         @Override
         public boolean isEnabled(int position) 
         {
 //            return mItemList.get(position).isClickable();
             return false;
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) 
         {
             return mItemList.get(position).getView(SearchActivity.this, convertView, mInflater);
         }
     }
     
     @Override
     protected void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_search);
         
         mSearchEditText = (EditText)findViewById(R.id.search_edit_text);
         mListView = (ListView)findViewById(R.id.search_list_view);
         mItemList = new ArrayList<IListItem>();
         mItemDataList = new ArrayList<IListItem>();
         mListViewAdapter = new PartAdapter();
         mProgressDialog = new MyProgressDialog(this);
         mListView.setAdapter(mListViewAdapter);
         mInflater = LayoutInflater.from(this);
         createUpdateThreadAndHandler();
         
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
             public void beforeTextChanged(CharSequence s, int start, int count,
                     int after) {
                 // TODO Auto-generated method stub
             }
             
             @Override
             public void afterTextChanged(Editable arg0) {
                 // TODO Auto-generated method stub
             }
         });
         
         // For test
 //        LabelItem label = new LabelItem("CCTV-1");
 //        mItemList.add(label);
 //        Item item = new Item();
 //        item.id = "cctv1";
 //        item.name = "CCTV-1";
 //        item.time = "00:26";
 //        item.title = "新闻联播";
 //        ContentItem contentItem = new ContentItem(item);
 //        mItemList.add(contentItem);
 //        mListViewAdapter.notifyDataSetChanged();
     }
 
     private void createUpdateThreadAndHandler()
     {
         mUpdateHandler = new Handler(NetworkManager.getInstance().getNetworkThreadLooper());
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
         updateResult();
     }
     
     private void updateResult()
     {
         mUpdateHandler.post(new Runnable()
         {
             public void run()
             {
                 String url = AppEngine.getInstance().getUrlManager().tryToGetDnsedUrl(UrlManager.URL_SEARCH) + "?keyword=" + mKeyword;
                 NetDataGetter getter;
                 try 
                 {
                     getter = new DefaultNetDataGetter(url);
                     JSONObject jsonRoot = getter.getJSONsObject();
                     mItemDataList.clear();
                     if (jsonRoot != null)
                     {
                         JSONArray resultArray = jsonRoot.getJSONArray("result");
                         if (resultArray != null)
                         {
                             
                             for (int i=0; i<resultArray.length(); ++i)
                             {
                                 String id = resultArray.getJSONObject(i).getString("id");
                                 String name = resultArray.getJSONObject(i).getString("name");
                                 JSONArray programsArray = resultArray.getJSONObject(i).getJSONArray("programs");
                                 
                                 mItemDataList.add(new LabelItem(name));
                                 if (programsArray != null)
                                 {
                                     for (int j=0; j<programsArray.length(); ++j)
                                     {
                                         String time = programsArray.getJSONObject(j).getString("time");
                                         String title = programsArray.getJSONObject(j).getString("title");                               
                                         Item item = new Item();
                                         item.id = id;
                                         item.name = name;
                                         item.time = time;
                                         item.title = title;
                                         item.key = mKeyword;
                                         mItemDataList.add(new ContentItem(item));
                                     }
                                 }
                             }
                         }
                     }
                     uiHandler.sendEmptyMessage(0);
                 }
                 catch (MalformedURLException e) 
                 {
                     e.printStackTrace();
                 }
                 catch (JSONException e) 
                 {
                     e.printStackTrace();
                 }
             }
         });
         mProgressDialog.show();
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
             mProgressDialog.dismiss();
             super.handleMessage(msg);
             mItemList.clear();
             for (int i=0; i<mItemDataList.size(); ++i)
             {
                 mItemList.add(mItemDataList.get(i));
             }
             mListViewAdapter.notifyDataSetChanged();
             if (mItemDataList.isEmpty())
                 Toast.makeText(SearchActivity.this, getResources().getString(R.string.no_found_tips), Toast.LENGTH_SHORT).show();
         }
     };
     
     interface IListItem
     {
         public int getLayout();
         public boolean isClickable();
         public View getView(Context context, View convertView, LayoutInflater inflater);
     }
 
     class LabelItem implements IListItem 
     {
         private String mLabel;
         public LabelItem(String label)
         {
             mLabel = label;
         }
         
         @Override
         public int getLayout() 
         {
             return R.layout.search_list_label_item;
         }
 
         @Override
         public boolean isClickable() 
         {
             return false;
         }
 
         @Override
         public View getView(Context context, View convertView, LayoutInflater inflater) 
         {
             convertView = inflater.inflate(getLayout(), null);
             TextView title = (TextView) convertView.findViewById(R.id.search_item_label_text_view);
             title.setText(mLabel);
             return convertView;
         }
     }
 
     class Item
     {
         String id;
         String name;
         String time;
         String title;
         String key;
     }
 
     class ContentItem implements IListItem 
     {
         private String SEPERATOR = ": ";
         private Item mItem;
         public ContentItem(Item item)
         {
             mItem = item;
         }
         
         @Override
         public int getLayout() 
         {
             return R.layout.search_list_content_item;
         }
 
         @Override
         public boolean isClickable() 
         {
             return true;
         }
 
         @Override
         public View getView(Context context, View convertView, LayoutInflater inflater) 
         {
             convertView = inflater.inflate(getLayout(), null);
             TextView tv = (TextView) convertView.findViewById(R.id.search_item_content_text_view);
             SpannableString ss = new SpannableString(mItem.time + SEPERATOR + mItem.title);
             int start = ss.toString().indexOf(mItem.key);
             if (start != -1)
             {
                 ss.setSpan(new ForegroundColorSpan(Color.RED), start, start + mItem.key.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
             }
             tv.setText(ss);
             return convertView;
         }
     }
 }
