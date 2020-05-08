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
 
 public class CacheManager 
 {
     private Context mContext;
     private HashMap<String, List<HashMap<String, String>>> mCategories;
     private HashMap<String, List<HashMap<String, String>>> mChannels;
     private HashMap<String, String> mAllTvmaoIds;
     private final String FILE_CACHED_CATEGORIES = "categories.txt";
     private final String FILE_CACHED_CHANNELS   = "channels.txt";
     private final String FILE_ALL_TVMAO_IDS = "web_ids.txt";
     private HashMap<String, String> mMemoryCache;
         
     public CacheManager(Context context)
     {
         mContext = context;
         mMemoryCache = new HashMap<String, String>();
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
         mAllTvmaoIds.putAll(tvmaoIdMap);
         return saveObjectToFile(mAllTvmaoIds, FILE_ALL_TVMAO_IDS);
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
         deleteFile(file1);
         deleteFile(file2);
         deleteFile(file3);
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
