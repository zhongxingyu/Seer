 package com.ouchadam.fang.presentation.item;
 
 import android.content.ContentResolver;
 import android.content.Context;
 import android.database.Cursor;
 
 import com.ouchadam.bookkeeper.Downloader;
 import com.ouchadam.bookkeeper.domain.DownloadId;
 import com.ouchadam.bookkeeper.watcher.AsyncNotificationWatcher;
 import com.ouchadam.fang.ItemDownload;
 import com.ouchadam.fang.domain.ItemToPlaylist;
 import com.ouchadam.fang.domain.item.Item;
 import com.ouchadam.fang.persistance.AddToPlaylistPersister;
 import com.ouchadam.fang.persistance.FangProvider;
 import com.ouchadam.fang.persistance.database.Tables;
 import com.ouchadam.fang.persistance.database.Uris;
 
 public class ItemDownloader {
 
     private static final int NEXT_POSITION_OFFSET = 1;
     private final Downloader downloader;
     private final Context context;
 
     public ItemDownloader(Downloader downloader, Context context) {
         this.downloader = downloader;
         this.context = context;
     }
 
     public void downloadItem(Item item) {
         ItemDownload downloadable = ItemDownload.from(item);
         DownloadId downloadId = downloader.keep(downloadable);
         downloader.store(downloadId, item.getId());
 
         addToPlaylist(item, downloadId);
         AsyncNotificationWatcher asyncNotificationWatcher = new AsyncNotificationWatcher(context, downloadable, downloadId);
         asyncNotificationWatcher.startWatching();
     }
 
     private void addToPlaylist(final Item item, final DownloadId downloadId) {
         new Thread(new Runnable() {
             @Override
             public void run() {
                 int listPosition = getPlaylistTotal() + NEXT_POSITION_OFFSET;
                 new AddToPlaylistPersister(context.getContentResolver()).persist(ItemToPlaylist.from(item, downloadId.value(), listPosition));
             }
         }).start();
     }
 
     private int getPlaylistTotal() {
         return new PlaylistQuery(context.getContentResolver()).getCurrentCount();
     }
 
     private static class PlaylistQuery {
 
         private static final int ZERO_COUNT = 0;
         private final ContentResolver contentResolver;
 
         private PlaylistQuery(ContentResolver contentResolver) {
             this.contentResolver = contentResolver;
         }
 
         public int getCurrentCount() {
             Cursor cursor = getQuery();
             try {
                 return isValid(cursor) ? cursor.getCount() : ZERO_COUNT;
             } finally {
                 if (cursor != null) {
                     cursor.close();
                 }
             }
         }
 
         private Cursor getQuery() {
             return contentResolver.query(
                     FangProvider.getUri(Uris.PLAYLIST),
                     new String[]{Tables.Playlist.ITEM_ID.name()},
                     null,
                     null,
                     null
             );
         }
 
         private boolean isValid(Cursor cursor) {
             return cursor != null && cursor.moveToFirst();
         }
     }
 
 }
