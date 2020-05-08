 package nz.gen.wellington.guardian.android.api.caching;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Date;
 
 import nz.gen.wellington.guardian.android.activities.ArticleCallback;
import nz.gen.wellington.guardian.android.dates.DateTimeHelper;
 import nz.gen.wellington.guardian.android.model.Article;
 import nz.gen.wellington.guardian.android.model.ArticleBundle;
 import nz.gen.wellington.guardian.android.model.ArticleSet;
 import nz.gen.wellington.guardian.android.model.FavouriteStoriesArticleSet;
 import android.content.Context;
 
 public class FileBasedArticleCache {
 	
 	//private static final String TAG = "FileBasedArticleCache";
 
 	private Context context;
 	
 	public FileBasedArticleCache(Context context) {
 		this.context = context;
 	}
 	
 	
 	 public void putArticleSetArticles(ArticleSet articleSet, ArticleBundle bundle) {
 		 //Log.i(TAG, "Writing to disk: " + articleSet.getName());
 		 try {
 			 FileOutputStream fos = FileService.getFileOutputStream(context, getLocalFileKeyForArticleSet(articleSet));
 			 ObjectOutputStream out = new ObjectOutputStream(fos);
 			 out.writeObject(bundle);
 			 out.close();
 		 } catch (IOException ex) {
 			 //Log.e(TAG, "IO Exception while writing article set: " + articleSet.getName() + ex.getMessage());
 		 }
 	 }
 	 
 	 
	 public void touchArticleSet(ArticleSet articleSet, Date modTime) {
		 FileService.touchFile(context, articleSet.getApiUrl(), modTime);
 	 }
 
 
 	private String getLocalFileKeyForArticleSet(ArticleSet articleSet) {
 		String localFileKey = articleSet.getApiUrl();
 		 if (articleSet instanceof FavouriteStoriesArticleSet) {
 			 localFileKey = "favourites";
 		 }
 		return localFileKey;
 	}
 
 
 	public ArticleBundle getArticleSetArticles(ArticleSet articleSet, ArticleCallback articleCallback) {
 		String localFileKey = getLocalFileKeyForArticleSet(articleSet);
 		if (!FileService.isLocallyCached(context, localFileKey)) {
 			return null;
 		}
 		
 		try {
 			FileInputStream fis = FileService.getFileInputStream(context, localFileKey);
 
 			//Log.i(TAG, "Reading from disk: " + filepath);
 			ObjectInputStream in = new ObjectInputStream(fis);
 			ArticleBundle loaded = (ArticleBundle) in.readObject();
 			in.close();
 			//Log.i(TAG, "Finished reading from disk: " + filepath);
 			if (loaded != null) {
 								
 				if (articleCallback != null) {
 					for (Article article : loaded.getArticles()) {
 						articleCallback.articleReady(article);
 					}
 				}
 					
 				//Log.i(TAG, "Loaded " + loaded.getArticles().size() + " articles");
 				//Log.i(TAG, "Content checksum is: " + loaded.getChecksum());
 				return loaded;
 			}
 			return null;
 
 		} catch (IOException ex) {
 			//Log.e(TAG, "IO Exception while writing article set: " + articleSet.getName() + ex.getMessage());
 		} catch (ClassNotFoundException ex) {
 			//Log.e(TAG, "Exception while writing article set: " + articleSet.getName() + ex.getMessage());
 		}
 		return null;
 	}
 	
 	
 	public void clear() {
 		FileService.clearAll(context);
 	}
 	
 		
 	public void clear(ArticleSet articleSet) {
 		//Log.i(TAG, "Clearing article set: " + articleSet.getName());
 		if (FileService.isLocallyCached(context, articleSet.getApiUrl())) {
 			FileService.clear(context, articleSet.getApiUrl());
 		} else {
 			//Log.i(TAG, "No local copy to clear:" + articleSet.getApiUrl());
 		}
 	}
 
 
 	public Date getModificationTime(ArticleSet articleSet) {
 		return FileService.getModificationTime(context, articleSet.getApiUrl());
 	}
 		
 }
