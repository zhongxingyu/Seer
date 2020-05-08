 package net.sourcewalker.smugview.data;
 
 import java.io.IOException;
 import java.util.List;
 
 import net.sourcewalker.smugview.R;
 import net.sourcewalker.smugview.auth.Authenticator;
 import net.sourcewalker.smugview.gui.AlbumListActivity;
 import net.sourcewalker.smugview.parcel.AlbumInfo;
 import net.sourcewalker.smugview.parcel.ImageInfo;
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.accounts.AuthenticatorException;
 import android.accounts.OperationCanceledException;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.AbstractThreadedSyncAdapter;
 import android.content.ContentProviderClient;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SyncResult;
 import android.os.Bundle;
 import android.os.RemoteException;
 import android.util.Log;
 import android.widget.RemoteViews;
 
 /**
  * This sync adapter will synchronize all albums and the metadata of every image
  * they contain with a local database. The images itself (including thumbnails)
  * will not be downloaded with the sync adapter.
  * 
  * @author Xperimental
  */
 public class SmugViewSyncAdapter extends AbstractThreadedSyncAdapter {
 
     private static final String TAG = "SmugViewSyncAdapter";
     private static final int NOTIFY_SYNC = 1;
 
     private Context context;
     private AccountManager accountManager;
     private NotificationManager notificationManager;
     private boolean canceled;
 
     public SmugViewSyncAdapter(Context context) {
         super(context, true);
 
         this.context = context;
         this.accountManager = AccountManager.get(context);
         this.notificationManager = (NotificationManager) context
                 .getSystemService(Context.NOTIFICATION_SERVICE);
     }
 
     /*
      * (non-Javadoc)
      * @see
      * android.content.AbstractThreadedSyncAdapter#onPerformSync(android.accounts
      * .Account, android.os.Bundle, java.lang.String,
      * android.content.ContentProviderClient, android.content.SyncResult)
      */
     @Override
     public void onPerformSync(Account account, Bundle extras, String authority,
             ContentProviderClient provider, SyncResult syncResult) {
         canceled = false;
         try {
             updateNotification(R.string.sync_authenticate, null, 0, 1);
             String sessionId = accountManager.blockingGetAuthToken(account,
                     Authenticator.TOKEN_TYPE, true);
             if (sessionId != null) {
                 // Remove current content
                 syncResult.stats.numDeletes += provider.delete(
                         SmugView.Image.CONTENT_URI, null, null);
                 syncResult.stats.numDeletes += provider.delete(
                         SmugView.Album.CONTENT_URI, null, null);
                 // Get list of albums
                 updateNotification(R.string.sync_updatealbums, null, 0, 1);
                 List<AlbumInfo> albumList = ServerOperations
                         .getAlbums(sessionId);
                 int position = 0;
                 int count = albumList.size();
                 for (AlbumInfo album : albumList) {
                     if (canceled) {
                         throw new OperationCanceledException();
                     }
                    updateNotification(R.string.sync_updateimages, album
                            .getTitle(), position, count);
                     Log.d(TAG, "Getting images for: " + album.getTitle());
                    provider.insert(SmugView.Album.CONTENT_URI, album
                            .toValues());
                     syncResult.stats.numInserts++;
                     List<ImageInfo> images = ServerOperations.getImages(
                             sessionId, album.getId(), album.getKey());
                     for (ImageInfo image : images) {
                        provider.insert(SmugView.Image.CONTENT_URI, image
                                .toValues());
                         syncResult.stats.numInserts++;
                     }
                     position++;
                 }
             }
         } catch (OperationCanceledException e) {
             Log.d(TAG, "Sync canceled.");
         } catch (AuthenticatorException e) {
             syncResult.stats.numAuthExceptions++;
         } catch (IOException e) {
             syncResult.stats.numIoExceptions++;
         } catch (RemoteException e) {
             syncResult.stats.numIoExceptions++;
         }
         clearNotification();
     }
 
     /**
      * Create or update the status bar notification to show the sync status.
      * 
      * @param stringId
      *            Resource id of status bar format string.
      * @param field
      *            Field to use with status bar format string.
      * @param position
      *            Value for progress bar.
      * @param count
      *            Maximum for progress bar.
      */
     private void updateNotification(int stringId, String field, int position,
             int count) {
         String format = context.getString(stringId);
        String text = String.format(format, field);
 
         final Notification notification = new Notification(
                 R.drawable.statusbar, text, System.currentTimeMillis());
         notification.flags = notification.flags
                 | Notification.FLAG_ONGOING_EVENT;
         notification.contentView = new RemoteViews(context
                 .getApplicationContext().getPackageName(),
                 R.layout.statusbar_sync);
         notification.contentView.setTextViewText(R.id.sync_text, text);
         notification.contentView.setProgressBar(R.id.sync_progress, count,
                 position, false);
         Intent intent = new Intent(context, AlbumListActivity.class);
         notification.contentIntent = PendingIntent.getActivity(context, 0,
                 intent, 0);
 
         notificationManager.notify(NOTIFY_SYNC, notification);
     }
 
     /**
      * Clears the status bar notification.
      */
     private void clearNotification() {
         notificationManager.cancel(NOTIFY_SYNC);
     }
 
     /*
      * (non-Javadoc)
      * @see android.content.AbstractThreadedSyncAdapter#onSyncCanceled()
      */
     @Override
     public void onSyncCanceled() {
         canceled = true;
     }
 
 }
