 /*
  * Copyright (C) 2010 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.android.contacts;
 
 import android.accounts.Account;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.app.Service;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.IBinder;
 import android.provider.ContactsContract.RawContacts;
 import android.text.TextUtils;
 import android.util.Log;
 import android.widget.RemoteViews;
 import android.widget.Toast;
 
 import com.android.vcard.VCardConfig;
 import com.android.vcard.VCardEntry;
 import com.android.vcard.VCardEntryCommitter;
 import com.android.vcard.VCardEntryConstructor;
 import com.android.vcard.VCardEntryCounter;
 import com.android.vcard.VCardEntryHandler;
 import com.android.vcard.VCardInterpreter;
 import com.android.vcard.VCardInterpreterCollection;
 import com.android.vcard.VCardParser;
 import com.android.vcard.VCardParser_V21;
 import com.android.vcard.VCardParser_V30;
 import com.android.vcard.VCardSourceDetector;
 import com.android.vcard.exception.VCardException;
 import com.android.vcard.exception.VCardNestedException;
 import com.android.vcard.exception.VCardNotSupportedException;
 import com.android.vcard.exception.VCardVersionException;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 /**
  * The class responsible for importing vCard from one ore multiple Uris.
  */
 public class ImportVCardService extends Service {
     private final static String LOG_TAG = "ImportVCardService";
 
     private class ProgressNotifier implements VCardEntryHandler {
         private final int mId;
 
         public ProgressNotifier(int id) {
             mId = id;
         }
 
         public void onStart() {
         }
 
         public void onEntryCreated(VCardEntry contactStruct) {
             mCurrentCount++;  // 1 origin.
             if (contactStruct.isIgnorable()) {
                 return;
             }
 
             final Context context = ImportVCardService.this;
             // We don't use startEntry() since:
             // - We cannot know name there but here.
             // - There's high probability where name comes soon after the beginning of entry, so
             //   we don't need to hurry to show something.
             final String packageName = "com.android.contacts";
             final RemoteViews remoteViews = new RemoteViews(packageName,
                     R.layout.status_bar_ongoing_event_progress_bar);
             final String title = getString(R.string.reading_vcard_title);
             final String text = getString(R.string.progress_notifier_message,
                     String.valueOf(mCurrentCount),
                     String.valueOf(mTotalCount),
                     contactStruct.getDisplayName());
 
             // TODO: uploading image does not work correctly. (looks like a static image).
             remoteViews.setTextViewText(R.id.description, text);
             remoteViews.setProgressBar(R.id.progress_bar, mTotalCount, mCurrentCount,
                     mTotalCount == -1);
             final String percentage =
                     getString(R.string.percentage,
                             String.valueOf(mCurrentCount * 100/mTotalCount));
             remoteViews.setTextViewText(R.id.progress_text, percentage);
             remoteViews.setImageViewResource(R.id.appIcon, android.R.drawable.stat_sys_download);
 
             final Notification notification = new Notification();
             notification.icon = android.R.drawable.stat_sys_download;
             notification.flags |= Notification.FLAG_ONGOING_EVENT;
             notification.contentView = remoteViews;
 
             notification.contentIntent =
                     PendingIntent.getActivity(context, 0,
                             new Intent(context, ContactsListActivity.class), 0);
             mNotificationManager.notify(mId, notification);
         }
 
         public void onEnd() {
         }
     }
 
     private class VCardReadThread extends Thread {
         private final Context mContext;
         private final ContentResolver mResolver;
         private VCardParser mVCardParser;
         private boolean mCanceled;
         private final List<Uri> mErrorUris;
         private final List<Uri> mCreatedUris;
 
         public VCardReadThread() {
             mContext = ImportVCardService.this;
             mResolver = mContext.getContentResolver();
             mErrorUris = new ArrayList<Uri>();
             mCreatedUris = new ArrayList<Uri>();
         }
 
         @Override
         public void run() {
             while (!mCanceled) {
                 mErrorUris.clear();
                 mCreatedUris.clear();
 
                 final Account account;
                 final Uri uri;
                 final int estimatedType;
                 final String estimatedCharset;
                 final boolean useV30;
                 final int entryCount;
                 final int id;
                 final boolean needReview;
                 synchronized (mContext) {
                     if (mPendingInputs.size() == 0) {
                         mNowRunning = false;
                         break;
                     } else {
                         final PendingInput pendingInput = mPendingInputs.poll();
                         account = pendingInput.account;
                         uri = pendingInput.uri;
                         estimatedType = pendingInput.estimatedType;
                         estimatedCharset = pendingInput.estimatedCharset;
                         useV30 = pendingInput.useV30;
                         entryCount = pendingInput.entryCount;
                         id = pendingInput.id;
                     }
                 }
                 runInternal(account, uri, estimatedType, estimatedCharset,
                         useV30, entryCount, id);
                 doFinishNotification(id, uri);
             }
             Log.i(LOG_TAG, "Successfully imported. Total: " + mTotalCount);
             stopSelf();
         }
 
         private void runInternal(Account account,
                 Uri uri, int estimatedType, String estimatedCharset,
                 boolean useV30, int entryCount,
                 int id) {
             int totalCount = 0;
             final ArrayList<VCardSourceDetector> detectorList =
                 new ArrayList<VCardSourceDetector>();
             if (mCanceled) {
                 return;
             }
 
             // First scanning is over. Try to import each vCard, which causes side effects.
             mTotalCount += entryCount;
             mCurrentCount = 0;
 
             if (mCanceled) {
                 Log.w(LOG_TAG, "Canceled during importing (with storing data in database)");
                 // TODO: implement cancel correctly.
                 return;
             }
 
             final VCardEntryConstructor constructor =
                     new VCardEntryConstructor(estimatedType, account, estimatedCharset);
             final VCardEntryCommitter committer = new VCardEntryCommitter(mResolver);
             constructor.addEntryHandler(committer);
             constructor.addEntryHandler(new ProgressNotifier(id));
 
             if (!readOneVCard(uri, estimatedType, estimatedCharset, constructor)) {
                 Log.e(LOG_TAG, "Failed to read \"" + uri.toString() + "\" " +
                 "while first scan was successful.");
             }
                 final List<Uri> createdUris = committer.getCreatedUris();
                 if (createdUris != null && createdUris.size() > 0) {
                     mCreatedUris.addAll(createdUris);
                 } else {
                     Log.w(LOG_TAG, "Created Uris is null (src = " + uri.toString() + "\"");
                 }
         }
 
         private boolean readOneVCard(Uri uri, int vcardType, String charset,
                 VCardInterpreter interpreter) {
             InputStream is;
             try {
                 // TODO: use vcardType given from detector and stop trying to read the file twice.
                 is = mResolver.openInputStream(uri);
 
                 // We need synchronized since we need to handle mCanceled and mVCardParser
                 // at once. In the worst case, a user may call cancel() just before recreating
                 // mVCardParser.
                 synchronized (this) {
                     // TODO: ensure this change works fine.
                     // mVCardParser = new VCardParser_V21(vcardType, charset);
                     mVCardParser = new VCardParser_V21(vcardType);
                     if (mCanceled) {
                         mVCardParser.cancel();
                     }
                 }
 
                 try {
                     mVCardParser.parse(is, interpreter);
                 } catch (VCardVersionException e1) {
                     try {
                         is.close();
                     } catch (IOException e) {
                     }
                     if (interpreter instanceof VCardEntryConstructor) {
                         // Let the object clean up internal temporal objects,
                         ((VCardEntryConstructor) interpreter).clear();
                     }
                     is = mResolver.openInputStream(uri);
 
                     synchronized (this) {
                         // mVCardParser = new VCardParser_V30(vcardType, charset);
                         mVCardParser = new VCardParser_V30(vcardType);
                         if (mCanceled) {
                             mVCardParser.cancel();
                         }
                     }
 
                     try {
                         mVCardParser.parse(is, interpreter);
                     } catch (VCardVersionException e2) {
                         throw new VCardException("vCard with unspported version.");
                     }
                 } finally {
                     if (is != null) {
                         try {
                             is.close();
                         } catch (IOException e) {
                         }
                     }
                 }
             } catch (IOException e) {
                 Log.e(LOG_TAG, "IOException was emitted: " + e.getMessage());
                 return false;
             } catch (VCardNestedException e) {
                 // In the first scan, we may (correctly) encounter this exception.
                 // We assume that we were able to detect the type of vCard before
                 // the exception being thrown.
                 //
                 // In the second scan, we may (inappropriately) encounter it.
                 // We silently ignore it, since
                 // - It is really unusual situation.
                 // - We cannot handle it by definition.
                 // - Users cannot either.
                 // - We should not accept unnecessarily complicated vCard, possibly by wrong manner.
                 Log.w(LOG_TAG, "Nested Exception is found (it may be false-positive).");
             } catch (VCardNotSupportedException e) {
                 return false;
             } catch (VCardException e) {
                 return false;
             }
             return true;
         }
 
         private void doErrorNotification(int id) {
             final Notification notification = new Notification();
             notification.icon = android.R.drawable.stat_sys_download_done;
             final String title = mContext.getString(R.string.reading_vcard_failed_title);
             final PendingIntent intent =
                     PendingIntent.getActivity(mContext, 0, new Intent(), 0);
             notification.setLatestEventInfo(mContext, title, "", intent);
             mNotificationManager.notify(id, notification);
         }
 
         private void doFinishNotification(int id, Uri uri) {
             final Notification notification = new Notification();
             notification.icon = android.R.drawable.stat_sys_download_done;
             final String title = mContext.getString(R.string.reading_vcard_finished_title);
 
             final Intent intent;
             final long rawContactId = ContentUris.parseId(mCreatedUris.get(0));
             final Uri contactUri = RawContacts.getContactLookupUri(
                     getContentResolver(), ContentUris.withAppendedId(
                             RawContacts.CONTENT_URI, rawContactId));
             intent = new Intent(Intent.ACTION_VIEW, contactUri);
 
             final PendingIntent pendingIntent =
                     PendingIntent.getActivity(mContext, 0, intent, 0);
             notification.setLatestEventInfo(mContext, title, "", pendingIntent);
             mNotificationManager.notify(id, notification);
         }
 
         // We need synchronized since we need to handle mCanceled and mVCardParser at once.
         public synchronized void cancel() {
             mCanceled = true;
             if (mVCardParser != null) {
                 mVCardParser.cancel();
             }
         }
 
         public void onCancel(DialogInterface dialog) {
             cancel();
         }
     }
 
     private static class PendingInput {
         public final Account account;
         public final Uri uri;
         public final int estimatedType;
         public final String estimatedCharset;
         public final boolean useV30;
         public final int entryCount;
         public final int id;
 
         public PendingInput(Account account,
                 Uri uri, int estimatedType, String estimatedCharset,
                boolean useV30, int entryCount,
                 int id) {
             this.account = account;
             this.uri = uri;
             this.estimatedType = estimatedType;
             this.estimatedCharset = estimatedCharset;
             this.useV30 = useV30;
             this.entryCount = entryCount;
             this.id = id;
         }
     }
 
     // The two classes bellow must be called inside the synchronized block, using this
     // Activity as a Context.
     private boolean mNowRunning;
     private final Queue<PendingInput> mPendingInputs = new LinkedList<PendingInput>();
 
     private NotificationManager mNotificationManager;
     private Thread mThread;
     private int mTotalCount;
     private int mCurrentCount;
 
     private Uri[] tryGetUris(Intent intent) {
         final String[] uriStrings =
                 intent.getStringArrayExtra(ImportVCardActivity.VCARD_URI_ARRAY);
         if (uriStrings == null || uriStrings.length == 0) {
             Log.e(LOG_TAG, "Given uri array is empty");
             return null;
         }
 
         final int length = uriStrings.length;
         final Uri[] uris = new Uri[length];
         for (int i = 0; i < length; i++) {
             uris[i] = Uri.parse(uriStrings[i]);
         }
 
         return uris;
     }
 
     private Account tryGetAccount(Intent intent) {
         if (intent == null) {
             Log.w(LOG_TAG, "Intent is null");
             return null;
         }
 
         final String accountName = intent.getStringExtra("account_name");
         final String accountType = intent.getStringExtra("account_type");
         if (!TextUtils.isEmpty(accountName) && !TextUtils.isEmpty(accountType)) {
             return new Account(accountName, accountType);
         } else {
             Log.w(LOG_TAG, "Account is not set.");
             return null;
         }
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
         if (mNotificationManager == null) {
             mNotificationManager =
                     (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
         }
 
         // TODO: use this.
         final int[] estimatedTypeArray =
             intent.getIntArrayExtra(ImportVCardActivity.ESTIMATED_VCARD_TYPE_ARRAY);
         final String[] estimatedCharsetArray =
             intent.getStringArrayExtra(ImportVCardActivity.ESTIMATED_CHARSET_ARRAY);
         final boolean[] useV30Array =
             intent.getBooleanArrayExtra(ImportVCardActivity.USE_V30_ARRAY);
         final int[] entryCountArray =
             intent.getIntArrayExtra(ImportVCardActivity.ENTRY_COUNT_ARRAY);

         final Account account = tryGetAccount(intent);
         final Uri[] uris = tryGetUris(intent);
         if (uris == null) {
             Log.e(LOG_TAG, "Uris are null.");
             Toast.makeText(this, getString(R.string.reading_vcard_failed_title),
                     Toast.LENGTH_LONG).show();
             stopSelf();
             return START_NOT_STICKY;
         }
 
         int length = uris.length;
         if (estimatedTypeArray.length < length) {
             Log.w(LOG_TAG, String.format("estimatedTypeArray.length < length (%d, %d)",
                     estimatedTypeArray.length, length));
             length = estimatedTypeArray.length;
         }
         if (useV30Array.length < length) {
             Log.w(LOG_TAG, String.format("useV30Array.length < length (%d, %d)",
                     useV30Array.length, length));
             length = useV30Array.length;
         }
         if (entryCountArray.length < length) {
             Log.w(LOG_TAG, String.format("entryCountArray.length < length (%d, %d)",
                     entryCountArray.length, length));
             length = entryCountArray.length;
         }
 
         synchronized (this) {
             for (int i = 0; i < length; i++) {
                 mPendingInputs.add(new PendingInput(account,
                         uris[i], estimatedTypeArray[i], estimatedCharsetArray[i],
                         useV30Array[i], entryCountArray[i],
                         startId));
             }
             if (!mNowRunning) {
                 Toast.makeText(this, getString(R.string.vcard_importer_start_message),
                         Toast.LENGTH_LONG).show();
                 // Assume thread is alredy broken.
                 // Even when it still exists, it never scan the PendingInput newly added above.
                 mNowRunning = true;
                 mThread = new VCardReadThread();
                 mThread.start();
             } else {
                 Toast.makeText(this, getString(R.string.vcard_importer_will_start_message),
                         Toast.LENGTH_LONG).show();
             }
         }
 
         return START_NOT_STICKY;
     }
 
     @Override
     public IBinder onBind(Intent intent) {
         return null;
     }
 }
