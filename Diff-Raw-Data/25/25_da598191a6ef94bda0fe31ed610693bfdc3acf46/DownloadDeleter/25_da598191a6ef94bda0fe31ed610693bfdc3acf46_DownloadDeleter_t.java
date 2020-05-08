 package com.ouchadam.fang.presentation.item;
 
 import android.app.DownloadManager;
 import android.content.Context;
 import android.util.Log;
 
 import com.ouchadam.fang.domain.FullItem;
 import com.ouchadam.fang.persistance.ContentProviderOperationExecutable;
 import com.ouchadam.fang.persistance.DatabaseCleaner;
 
 import java.util.List;
 
 class DownloadDeleter {
 
     private final DownloadManager downloadManager;
     private final DatabaseCleaner databaseCleaner;
 
     static DownloadDeleter from(Context context) {
         DatabaseCleaner databaseCleaner = new DatabaseCleaner(new ContentProviderOperationExecutable(context.getContentResolver()));
         return new DownloadDeleter((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE), databaseCleaner);
     }
 
     DownloadDeleter(DownloadManager downloadManager, DatabaseCleaner databaseCleaner) {
         this.downloadManager = downloadManager;
         this.databaseCleaner = databaseCleaner;
     }
 
     public void deleteItems(List<FullItem> items) {
         long[] itemDownloadIds = getDownloadIds(items);
         long[] itemIds = getItemIds(items);
         deleteSelected(itemDownloadIds, itemIds);
     }
 
     private long[] getItemIds(List<FullItem> items) {
         long[] itemIds = new long[items.size()];
         for (int index = 0; index < items.size(); index ++) {
             long itemId = items.get(index).getItemId();
             itemIds[index] = itemId;
         }
         return itemIds;
     }
 
     private void deleteSelected(long[] itemDownloadIds, long[] itemIds) {
         downloadManager.remove(itemDownloadIds);
         databaseCleaner.deleteIdsFromPlaylist(itemIds);
     }
 
     public void deleteAll(List<FullItem> items) {
         long[] itemDownloadIds = getDownloadIds(items);
         deleteAll(itemDownloadIds);
     }
 
     private long[] getDownloadIds(List<FullItem> items) {
         long[] itemDownloadIds = new long[items.size()];
         for (int index = 0; index < items.size(); index ++) {
             long downloadId = items.get(index).getDownloadId();
             itemDownloadIds[index] = downloadId;
         }
         return itemDownloadIds;
     }
 
     public void deleteAll(long[] itemDownloadIds) {
         downloadManager.remove(itemDownloadIds);
         databaseCleaner.deletePlaylist();
     }
 }
