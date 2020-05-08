 package com.ouchadam.fang.presentation;
 
 import com.ouchadam.bookkeeper.domain.DownloadId;
 import com.ouchadam.bookkeeper.domain.ProgressValues;
 import com.ouchadam.bookkeeper.watcher.DownloadWatcher;
 import com.ouchadam.fang.persistance.DownloadedItemPersister;
 
 class DownloadToDatabaseWatcher implements DownloadWatcher {
 
     private final DownloadId downloadId;
     private final DownloadedItemPersister persister;
 
     DownloadToDatabaseWatcher(DownloadId downloadId, DownloadedItemPersister persister) {
         this.downloadId = downloadId;
         this.persister = persister;
     }
 
     @Override
     public boolean isWatching(DownloadId downloadId) {
         return this.downloadId.equals(downloadId);
     }
 
     @Override
     public void onStart() {

     }
 
     @Override
     public void onUpdate(ProgressValues progressValues) {

     }
 
     @Override
     public void onStop() {
         persister.persist(downloadId.value());
     }
 }
