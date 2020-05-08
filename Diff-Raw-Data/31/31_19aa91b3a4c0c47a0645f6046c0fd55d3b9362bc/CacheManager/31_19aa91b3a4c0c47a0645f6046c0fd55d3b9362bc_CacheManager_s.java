 package com.tools.tvguide.managers;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.StreamCorruptedException;
 import java.util.HashMap;
 import java.util.List;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.support.v4.util.LruCache;
 
 public class CacheManager 
 {
     private Context mContext;
     private HashMap<String, List<HashMap<String, String>>> mCategories;
     private HashMap<String, List<HashMap<String, String>>> mChannels;
     private HashMap<String, String> mAllTvmaoIds;
     private HashMap<String, List<String>> mSearchWords;
     private final String FILE_CACHED_CATEGORIES = "categories.txt";
     private final String FILE_CACHED_CHANNELS   = "channels.txt";
     private final String FILE_ALL_TVMAO_IDS = "web_ids.txt";
     private final String FILE_SEARCH_WORDS = "search_words.txt";
    private HashMap<String, String> mMemoryCache;
     private LruCache<String, Bitmap> mBitmapCache;
         
     public CacheManager(Context context)
     {
         mContext = context;
        mMemoryCache = new HashMap<String, String>();
         mBitmapCache = new LruCache<String, Bitmap>(100);
     }
     
     /*
      * return: true on success; false on fail
      */
     @SuppressWarnings("unchecked")
     public boolean loadCategoriesByType(final String type, final List<HashMap<String, String>> result)
     {
         if (mCategories == null)
         {
             mCategories = (HashMap<String, List<HashMap<String, String>>>) loadObjectFromFile(FILE_CACHED_CATEGORIES);
             if (mCategories == null)
             {
                 mCategories = new HashMap<String, List<HashMap<String,String>>>();
                 return false;
             }
         }
         
         List<HashMap<String, String>> categories = mCategories.get(type);
         if (categories == null || categories.isEmpty())
         {
             return false;
         }
         for (int i=0; i<categories.size(); ++i)
         {
             result.add(categories.get(i));
         }
         return true;
     }
     
     /*
      * return: true on success; false on fail
      */
     public boolean saveCatgegoriesByType(String type, List<HashMap<String, String>> categories)
     {
         if (type == null || categories == null)
             return false;
         
         mCategories.put(type, categories);
         return saveObjectToFile(mCategories, FILE_CACHED_CATEGORIES);
     }
     
     @SuppressWarnings("unchecked")
     public boolean loadChannelsByCategory(final String categoryId, final List<HashMap<String, String>> result)
     {
         if (mChannels == null)
         {
             mChannels = (HashMap<String, List<HashMap<String, String>>>) loadObjectFromFile(FILE_CACHED_CHANNELS);
             if (mChannels == null)
             {
                 mChannels = new HashMap<String, List<HashMap<String,String>>>();
                 return false;
             }
         }
         
         List<HashMap<String, String>> channels = mChannels.get(categoryId);
         if (channels == null || channels.isEmpty())
         {
             return false;
         }
         for (int i=0; i<channels.size(); ++i)
         {
             result.add(channels.get(i));
         }
         return true;
     }
     
     public boolean saveChannelsByCategory(final String categoryId, final List<HashMap<String, String>> channels)
     {
         mChannels.put(categoryId, channels);
         return saveObjectToFile(mChannels, FILE_CACHED_CHANNELS);
     }
     
     @SuppressWarnings("unchecked")
     public boolean loadAllTvmaoIds(HashMap<String, String> result)
     {
         assert (result != null);
         if (mAllTvmaoIds == null)
         {
             mAllTvmaoIds = (HashMap<String, String>) loadObjectFromFile(FILE_ALL_TVMAO_IDS);
             if (mAllTvmaoIds == null)
             {
                 mAllTvmaoIds = new HashMap<String, String>();
                 return false;
             }
         }
         
         if (mAllTvmaoIds.isEmpty())
             return false;
         
         result.putAll(mAllTvmaoIds);
         return true;
     }
     
     public boolean saveAllTvmaoIds(HashMap<String, String> tvmaoIdMap)
     {
         assert (tvmaoIdMap != null);
         mAllTvmaoIds.putAll(tvmaoIdMap);
         return saveObjectToFile(mAllTvmaoIds, FILE_ALL_TVMAO_IDS);
     }
     
     @SuppressWarnings("unchecked")
     public boolean loadSearchWords(HashMap<String, List<String>> result)
     {
         assert (result != null);
         if (mSearchWords == null)
         {
             mSearchWords = (HashMap<String, List<String>>) loadObjectFromFile(FILE_SEARCH_WORDS);
             if (mSearchWords == null)
             {
                 mSearchWords = new HashMap<String, List<String>>();
                 return false;
             }
         }
         
         if (mSearchWords.isEmpty())
             return false;
         
         result.putAll(mSearchWords);
         return true;
     }
     
     public boolean saveSearchWords(HashMap<String, List<String>> searchWords)
     {
         assert (searchWords != null);
         mSearchWords.putAll(searchWords);
         return saveObjectToFile(mSearchWords, FILE_SEARCH_WORDS);
     }
     
     public void clear()
     {
         if (mCategories != null)
             mCategories.clear();
         if (mChannels != null)
             mChannels.clear();
         File file1 = new File(mContext.getFilesDir() + File.separator + FILE_CACHED_CATEGORIES);
         File file2 = new File(mContext.getFilesDir() + File.separator + FILE_CACHED_CHANNELS);
         File file3 = new File(mContext.getFilesDir() + File.separator + FILE_ALL_TVMAO_IDS);
         File file4 = new File(mContext.getFilesDir() + File.separator + FILE_SEARCH_WORDS);
         deleteFile(file1);
         deleteFile(file2);
         deleteFile(file3);
         deleteFile(file4);
        mMemoryCache.clear();
     }
     
    public String get(String key)
     {
        return mMemoryCache.get(key);
     }
     
    public void set(String key, String value)
     {
        mMemoryCache.put(key, value);
     }
     
     public Bitmap getBitmap(String key)
     {
         return mBitmapCache.get(key);
     }
     
     public void setBitmap(String key, Bitmap bitmap)
     {
         if (key != null && bitmap != null)
             mBitmapCache.put(key, bitmap);
     }
     
     private void deleteFile(File file)
     {
         if (file.exists())
         {
             if (file.isFile())
             {
                 file.delete();
             }
             else if (file.isDirectory())
             {
                 File files[] = file.listFiles();
                 for (int i=0; i<files.length; i++)
                 {
                     this.deleteFile(file);
                 }
             }
             file.delete();
         }
     }
     
     private Object loadObjectFromFile(String fileName)
     {
         try
         {
             FileInputStream fis = mContext.openFileInput(fileName);
             ObjectInputStream ois = new ObjectInputStream(fis);
             Object obj = ois.readObject();
             ois.close();
             fis.close();
             return obj;
         }
         catch (StreamCorruptedException e)
         {
             e.printStackTrace();
         }
         catch (FileNotFoundException e)
         {
             e.printStackTrace();
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
         catch (ClassNotFoundException e)
         {
             e.printStackTrace();
         }
         
         return null;
     }
     
     private boolean saveObjectToFile(Object obj, String fileName)
     {
         try
         {
             FileOutputStream fos = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
             ObjectOutputStream oos = new ObjectOutputStream(fos);
             oos.writeObject(obj);
             oos.flush();
             oos.close();
             fos.close();
             return true;
         }
         catch (FileNotFoundException e)
         {
             e.printStackTrace();
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
         
         return false;
     }
 }
