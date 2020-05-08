 package lt.asinica.lm.objects;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 
 import lt.asinica.lm.LMApp;

 import android.util.Log;
 
 public class Cache {
 	private static Cache instance;
 	public static Cache getInstance() {
 		if(instance==null)
 			instance = new Cache();
 		return instance;
 	}
 	private Cache() {
 		mCacheDir = LMApp.getDefaultContext().getCacheDir();
 		mCacheMap = new HashMap<String, Integer>();
 		mCacheExpiryList = new HashMap<Integer, Long>();
		clean();
 	}
 	public void clean() {
 		File[] cached = mCacheDir.listFiles();
 		int deleted = 0;
 		if(cached!=null) {
 			for(int i = 0; i<cached.length; i++) {
 				if(cached[i].delete())
 					deleted++;
 			}
 		}
 		mCacheMap.clear();
 		mCacheExpiryList.clear();
 		Log.i("DEBUG", "Cache cleaned. Deleted "+deleted);
 	}
 	
 	private int mCounter = 0;
 	private File mCacheDir;
 	private HashMap<String, Integer> mCacheMap;
 	private HashMap<Integer, Long> mCacheExpiryList;
 	
 	public String fetch(String identifier) {
 		if(mCacheMap.containsKey(identifier)) {
 			int cacheLoc = mCacheMap.get(identifier);
 			File cached = getFileHandle(cacheLoc);
 			if(mCacheExpiryList.containsKey(cacheLoc)) {
 				long expiry = mCacheExpiryList.get(cacheLoc);
 				if(expiry < System.currentTimeMillis()) {
 					Log.i("DEBUG", "Cache "+Integer.toString(cacheLoc)+" expired at "+String.valueOf(expiry) );
					cached.delete();
 					mCacheMap.remove(identifier);
 					mCacheExpiryList.remove(cacheLoc);
 					return null;
 				}
 			}
 	        try { // catches IOException below
 	            StringBuffer fileData = new StringBuffer(1000);
 	            BufferedReader reader = new BufferedReader(
 	                    new FileReader(cached));
 	            char[] buf = new char[1024];
 	            int numRead=0;
 	            while((numRead=reader.read(buf)) != -1){
 	                String readData = String.valueOf(buf, 0, numRead);
 	                fileData.append(readData);
 	                buf = new char[1024];
 	            }
 	            reader.close();
	            Log.v("DEBUG", "Succesfully loaded from cache "+Integer.toString(cacheLoc));
 	            return fileData.toString();	        	
 	        } catch(IOException e) {
 	        	Log.e("DEBUG", "Reading of cached file failed. "+e.getMessage());
 	        	e.printStackTrace();
 	        }
 		}
 		return null;
 	}
 	public boolean put(String identifier, String str, int expiresAfterMinutes) {
 		File cached = getFileHandle(mCounter);
         try { // catches IOException below
         	cached.createNewFile();
             FileWriter fw = new FileWriter(cached, false);
             fw.write(str);
             fw.flush();
             fw.close();
             mCacheMap.put(identifier, mCounter);
            Log.v("DEBUG", "Succesfully wrote to cache "+Integer.toString(mCounter));
         	long expiresAt = System.currentTimeMillis() + 1000 * 60 * expiresAfterMinutes;
         	mCacheExpiryList.put(mCounter, expiresAt);
         	Log.v("DEBUG", "Cache expires at "+String.valueOf(expiresAt));
         	
             mCounter++;
     		return true;
         } catch(IOException e) {
         	Log.e("DEBUG", "Caching of file failed. "+e.getMessage());
         	e.printStackTrace();
         }
 		return false;
 	}
 	
 	private File getFileHandle(int cacheLoc) {
 		 return new File(mCacheDir, Integer.toString(cacheLoc) );
 	}
 }
