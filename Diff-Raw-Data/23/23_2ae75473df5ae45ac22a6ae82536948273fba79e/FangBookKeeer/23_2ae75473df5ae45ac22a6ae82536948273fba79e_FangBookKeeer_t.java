 package com.ouchadam.fang.presentation;
 
 import android.content.ContentResolver;
 import android.content.Context;
 
 import com.ouchadam.bookkeeper.Downloader;
 import com.ouchadam.bookkeeper.RestoreableBookKeeper;
 import com.ouchadam.bookkeeper.delegate.IdManager;
 import com.ouchadam.bookkeeper.domain.DownloadId;
 import com.ouchadam.bookkeeper.domain.Downloadable;
 import com.ouchadam.bookkeeper.watcher.DownloadWatcher;
 import com.ouchadam.bookkeeper.watcher.LazyWatcher;
 import com.ouchadam.fang.persistance.DownloadedItemPersister;
 
 public class FangBookKeeer implements Downloader {
 
     private final RestoreableBookKeeper bookKeeper;
     private final ContentResolver contentResolver;
 
     public static FangBookKeeer getInstance(Context context) {
        return new FangBookKeeer(RestoreableBookKeeper.newInstance(context), context.getContentResolver());
     }
 
     public FangBookKeeer(RestoreableBookKeeper bookKeeper, ContentResolver contentResolver) {
         this.bookKeeper = bookKeeper;
         this.contentResolver = contentResolver;
         initBookKeeper();
     }
 
     private void initBookKeeper() {
         restore(new LazyDatabaseWatcher(createDownloadItemPersister()));
     }
 
     private static class LazyDatabaseWatcher implements LazyWatcher {
 
         private final DownloadedItemPersister persister;
 
         private LazyDatabaseWatcher(DownloadedItemPersister persister) {
             this.persister = persister;
         }
 
         @Override
         public DownloadWatcher create(DownloadId downloadId, long itemId) {
             return new DownloadToDatabaseWatcher(downloadId, persister);
         }
     }
 
     @Override
     public DownloadId keep(Downloadable from) {
         return bookKeeper.keep(from);
     }
 
     @Override
     public void restore(final LazyWatcher lazyWatcher) {
         bookKeeper.restore(new IdManager.BookKeeperRestorer() {
             @Override
             public void onRestore(DownloadId downloadId, long itemId) {
                 DownloadWatcher downloadWatcher = lazyWatcher.create(downloadId, itemId);
                 bookKeeper.watch(downloadId, downloadWatcher);
             }
         });
     }
 
     @Override
     public void watch(DownloadId downloadId, DownloadWatcher... downloadWatchers) {
         DownloadWatcher[] watchers = attachDatabaseWatcher(downloadId, downloadWatchers);
         bookKeeper.watch(downloadId, watchers);
     }
 
     public DownloadWatcher[] attachDatabaseWatcher(DownloadId downloadId, DownloadWatcher[] downloadWatchers) {
         DownloadToDatabaseWatcher downloadToDatabaseWatcher = new DownloadToDatabaseWatcher(downloadId, createDownloadItemPersister());
         DownloadWatcher[] newArr = new DownloadWatcher[downloadWatchers.length + 1];
         System.arraycopy(downloadWatchers, 0, newArr, 0, downloadWatchers.length);
         newArr[downloadWatchers.length] = downloadToDatabaseWatcher;
         return newArr;
     }
 
     private DownloadedItemPersister createDownloadItemPersister() {
         return new DownloadedItemPersister(contentResolver);
     }
 
     @Override
     public void store(DownloadId downloadId, long itemId) {
         bookKeeper.store(downloadId, itemId);
     }
 
 }
