 package com.anderspersson.xbmcwidget.recentvideo;
 
 import com.anderspersson.xbmcwidget.common.BitmapCache;
 import com.anderspersson.xbmcwidget.common.FileLog;
 import com.anderspersson.xbmcwidget.xbmc.DownloadBitmapCommand;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class CachedFanArtDownloader {
 	private Context _context;
 	private BitmapCache _cache;
 	private final FanArtSize fanArtSize;
 	
 	public CachedFanArtDownloader(Context context, FanArtSize fanArtSize) {
 		this._context = context;
 		this.fanArtSize = fanArtSize;
 		this._cache = new BitmapCache(context);
 	}
 
 	public String download(String path) {
 		if(path == null)
 			return null;
 		
 		String url = "/vfs/" + Uri.encode(path);
 		String key = makeCacheKey(path);
 		
 		if(_cache.has(key)) {
 			FileLog.appendLog("FanArt cache hit: " + key);
 			return _cache.get(key);
 		}
 		
 		try {
 			DownloadBitmapCommand downloadCommand = createBitmapDownloader(url);
 			Bitmap result = downloadCommand.execute();
 			
 			if(result == null) {
 				return null;
 			}
 			
 			_cache.put(key, result);
 			FileLog.appendLog("FanArt cached: " + key);
 			result.recycle();
 			return _cache.get(key);
 			
 		} catch(Exception ex) {
 			Log.v(CachedFanArtDownloader.class.toString(), 
 					String.format("Failed to download fanart '%s' - using default.", url), ex);
 			return null;
 		}
 	}
 	
 	public boolean isCached(String fanArtPath) {
 		return _cache.has(makeCacheKey(fanArtPath));
 	}
 
 	private String makeCacheKey(String path) {
 		return Uri.parse(path).getLastPathSegment();
 	}
 
 	private DownloadBitmapCommand createBitmapDownloader(String url) {
 		SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(_context);
 		
 		return new DownloadBitmapCommand(defaultSharedPreferences, url, fanArtSize.getHeight(), fanArtSize.getWidth());
 	}
 }
