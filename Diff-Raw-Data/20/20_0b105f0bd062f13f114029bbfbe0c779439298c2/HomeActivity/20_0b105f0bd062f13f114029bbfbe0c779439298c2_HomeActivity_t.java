 package com.tools.tvguide.activities;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import com.tools.tvguide.R;
 import com.tools.tvguide.components.MyProgressDialog;
 import com.tools.tvguide.data.Category;
 import com.tools.tvguide.managers.AppEngine;
import com.tools.tvguide.managers.BootManager.OnSplashFinishedCallback;
 import com.tools.tvguide.managers.OnPlayingHtmlManager.CategoryEntriesCallback;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Handler.Callback;
 import android.os.Message;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class HomeActivity extends Activity implements Callback 
 {
     private static final String TAG = "HomeActivity";
     private ListView mCategoryListView;
     private SimpleAdapter mListViewAdapter;
     private List<HashMap<String, Object>> mItemList;
     private List<Category> mCategoryList;
     private MyProgressDialog mProgressDialog;
     private boolean mHasUpdated = false;
     private Handler mUiHandler;
     private enum SelfMessage { SHOW_CATEGORY };
     
     @Override
     protected void onCreate(Bundle savedInstanceState) 
     {
         Log.e(TAG, "onCreate this = " + this);
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_home);
         mCategoryListView = (ListView)findViewById(R.id.category_list);
         mItemList = new ArrayList<HashMap<String, Object>>();
         mListViewAdapter = new SimpleAdapter(HomeActivity.this, mItemList, R.layout.home_list_item,
                 new String[]{"name"}, new int[]{R.id.home_item_text});
         mCategoryList = new ArrayList<Category>();
         mProgressDialog = new MyProgressDialog(this);
         mUiHandler = new Handler(this);
         
         mCategoryListView.setAdapter(mListViewAdapter);
         mCategoryListView.setOnItemClickListener(new OnItemClickListener() 
         {
             @Override
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
             {
                 if (mCategoryList != null)
                 {
                     Intent intent = null;
                     if (mCategoryList.get(position).next == Category.Next.CategoryList)
                     {
                         intent = new Intent(HomeActivity.this, CategorylistActivity.class);
                     }
                     else
                     {
                         intent = new Intent(HomeActivity.this, ChannellistActivity.class);
                     }
                     
                     if (intent != null)
                     {
                         intent.putExtra("category", (Serializable) mCategoryList.get(position));
                         startActivity(intent);
                     }
                 }
             }
         });
         
         update();
     }
     
     @Override
     protected void onResume()
     {
         super.onResume();
         // 发现之前获取失败，则重新获取
         if (mHasUpdated && (mCategoryList == null || mCategoryList.size() == 0))
             update();
     }
 
     private void update()
     {
         AppEngine.getInstance().getOnPlayingHtmlManager().getCategoryEntries(0, new CategoryEntriesCallback() 
         {
             @Override
             public void onCategoryEntriesLoaded(int requestId, List<Category> categories) 
             {
                 if (categories != null)
                 {
                     mCategoryList.clear();
                     mCategoryList.addAll(categories);
                     mCategoryList = classifyCategory(mCategoryList);
                     mUiHandler.obtainMessage(SelfMessage.SHOW_CATEGORY.ordinal()).sendToTarget();
                 }
             }
         });
        
        if (AppEngine.getInstance().getBootManager().isSplashShowing())
        {
            AppEngine.getInstance().getBootManager().addOnSplashFinishedCallback(new OnSplashFinishedCallback() 
            {
                @Override
                public void OnSplashFinished() 
                {
                    if (mCategoryList.isEmpty())
                        mProgressDialog.show();
                }
            });
        }
        else if (mCategoryList.isEmpty())
        {
            mProgressDialog.show();
        }
     }
 
     @Override
     public boolean handleMessage(Message msg) 
     {
         SelfMessage selfMsg = SelfMessage.values()[msg.what];
         switch (selfMsg)
         {
             case SHOW_CATEGORY:
                 if (mProgressDialog.isShowing())
                     mProgressDialog.dismiss();
                 for (int i=0; i<mCategoryList.size(); ++i)
                 {
                     HashMap<String, Object> item = new HashMap<String, Object>();
                     item.put("name", mCategoryList.get(i).name);
                     mItemList.add(item);
                 }
                 mListViewAdapter.notifyDataSetChanged();
                 mHasUpdated = true;
                 break;
         }
         
         return true;
     }
     
     private List<Category> classifyCategory(List<Category> categoryList)
     {
         if (categoryList == null)
             return null;
         
         List<Category> newCategoryList      = new ArrayList<Category>();
         List<Category> hatCategoryList      = new ArrayList<Category>();       // 港澳台频道
         List<Category> localCategoryList    = new ArrayList<Category>();       // 各省频道
         
         for (int i=0; i<categoryList.size(); ++i)
         {
             String name = categoryList.get(i).name;
             if (TextUtils.equals(name, "央视") || TextUtils.equals(name, "卫视") || TextUtils.equals(name, "数字"))
             {
                 categoryList.get(i).name = categoryList.get(i).name + "频道";
                 newCategoryList.add(categoryList.get(i));
             }
             else if (TextUtils.equals(name, "香港") || TextUtils.equals(name, "澳门") || TextUtils.equals(name, "台湾"))
             {
                 hatCategoryList.add(categoryList.get(i));
             }
             else
             {
                 localCategoryList.add(categoryList.get(i));
             }
         }
         
         if (!hatCategoryList.isEmpty())
         {
             Category hatCategory = new Category();
             hatCategory.name = "港澳台";
             hatCategory.next = Category.Next.CategoryList;
             hatCategory.categoryList = hatCategoryList;
             newCategoryList.add(hatCategory);
         }
         if (!localCategoryList.isEmpty())
         {
             Category localCategory = new Category();
             localCategory.name = "各省频道";
             localCategory.next = Category.Next.CategoryList;
             localCategory.categoryList = localCategoryList;
             newCategoryList.add(localCategory);
         }
         
         return newCategoryList;
     }
 }
