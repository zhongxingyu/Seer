 package com.tools.tvguide.managers;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import com.tools.tvguide.managers.ContentManager.LoadListener;
 import com.tools.tvguide.views.SearchHotwordsView;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 
 public class SearchWordsManager
 {
     private Context mContext;
     private SharedPreferences   mPreference;
     private static final String SHARE_PREFERENCES_NAME                      = "searchwords_settings";
     private static final String KEY_POP_SEARCH_LAST_UPDATE_FLAG             = "key_pop_search_last_update_flag";
     
     private List<String> mPopSearchList;
     private List<String> mHistorySearchList;
     private HashMap<String, List<String>> mSearchWordsMap;
     
     public abstract interface UpdateListener
     {
         public void onUpdateFinish(List<String> result);
     }
     
     public SearchWordsManager(Context context)
     {
         assert (context != null);
         mContext = context;
         mPopSearchList = new ArrayList<String>();
         mHistorySearchList = new ArrayList<String>();
         mSearchWordsMap = new HashMap<String, List<String>>();
         mPreference = mContext.getSharedPreferences(SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE);
         load();
     }
     
     public boolean needUpdate()
     {
         long lastUpdateTime = mPreference.getLong(KEY_POP_SEARCH_LAST_UPDATE_FLAG, 0);
         long currentTime = System.currentTimeMillis();
         
         if ((currentTime - lastUpdateTime) > 3600 * 12 * 1000)   // 超过12个小时，则需要重新更新
             return true;
         
         return false;
     }
     
     public List<String> getPopSearch()
     {
         List<String> popSearchList = new ArrayList<String>();
         popSearchList.addAll(mPopSearchList);
         return popSearchList;
     }
     
     public List<String> getHistorySearch()
     {
         List<String> historySearchList = new ArrayList<String>();
         historySearchList.addAll(mHistorySearchList);
         return historySearchList;
     }
     
     public void addSearchRecord(String word)
     {
         if (mHistorySearchList.contains(word))
             mHistorySearchList.remove(word);
         
         mHistorySearchList.add(0, word);    // 放在最前
         if (mHistorySearchList.size() > SearchHotwordsView.MAX_WORDS)
         {
             // 因subList得到的List无法序列化存储，故采用此方法
             List<String> newList = new ArrayList<String>();
             newList.addAll(mHistorySearchList.subList(0, SearchHotwordsView.MAX_WORDS));
             mHistorySearchList.clear();
             mHistorySearchList.addAll(newList);
         }
         store();
     }
     
     public void clearHistorySearch()
     {
     	mHistorySearchList.clear();
     	store();
     }
     
     public void updatePopSearch(final UpdateListener listener)
     {
        final List<String> popSearchList = new ArrayList<String>();
        AppEngine.getInstance().getContentManager().loadPopSearch(SearchHotwordsView.MAX_WORDS, popSearchList, new ContentManager.LoadListener() 
         {
             @Override
             public void onLoadFinish(int status) 
             {
                 if (status == LoadListener.SUCCESS)
                 {
                     if (listener != null)
                        listener.onUpdateFinish(popSearchList);
                    if (!popSearchList.isEmpty())
                    {
                        mPopSearchList.clear();
                        mPopSearchList.addAll(popSearchList);
                    }
                     store();
                     mPreference.edit().putLong(KEY_POP_SEARCH_LAST_UPDATE_FLAG, System.currentTimeMillis()).commit();
                 }
             }
         });
     }
     
     private void load()
     {
         HashMap<String, List<String>> saveData = new HashMap<String, List<String>>();
         boolean success = AppEngine.getInstance().getCacheManager().loadSearchWords(saveData);
         if (success)
         {
             if (saveData.get("pop_search") != null)
             {
                 mPopSearchList = saveData.get("pop_search");
             }
             if (saveData.get("history_search") != null)
             {
                 mHistorySearchList = saveData.get("history_search");
             }
         }
     }
     
     private void store()
     {
         synchronized (this) 
         {
             mSearchWordsMap.put("pop_search", mPopSearchList);
             mSearchWordsMap.put("history_search", mHistorySearchList);
             AppEngine.getInstance().getCacheManager().saveSearchWords(mSearchWordsMap);
         }
     }
 }
