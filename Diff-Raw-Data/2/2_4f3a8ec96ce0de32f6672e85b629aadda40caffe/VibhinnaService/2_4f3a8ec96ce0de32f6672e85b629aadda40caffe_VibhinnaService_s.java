 package com.binoy.vibhinna;
 
 import java.io.File;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.app.NotificationCompat;
 
 public class VibhinnaService extends CustomIntentService {
 
     public static final String TASK_TYPE = "type";
     public static final int TASK_TYPE_NEW_VFS = 0;
     public static final int TASK_TYPE_FORMAT_VFS = 1;
     protected static final int TASK_TYPE_RESIZE_VFS = 2;
     public static final String CACHE_SIZE = "cache_size";
     public static final String DATA_SIZE = "data_size";
     public static final String SYSTEM_SIZE = "system_size";
     public static final String FOLDER_PATH = "folder_path";
     public static final String VS_DESC = "vs_desc";
     public static final String ICON_ID = "icon_id";
     protected static final String TAG = "VibhinnaService";
     public static final String ACTION_TASK_QUEUE_UPDATED = "com.binoy.vibhinna.action.ACTION_TASK_QUEUE_UPDATED";
     protected static final String ACTION_VFS_LIST_UPDATED = "com.binoy.vibhinna.action.ACTION_VFS_LIST_UPDATED";
     protected static final String FORMAT_CACHE = "format_cache";
     protected static final String FORMAT_DATA = "format_data";
     protected static final String FORMAT_SYSTEM = "format_system";
     private static ContentResolver mResolver;
 
     private static Context mContext;
     static NotificationManager notificationManager;
 
     public VibhinnaService() {
         super("Service");
     }
 
     public int onStartCommand(Intent intent, int flags, int startId) {
         // startForeground(startId, null);
         return super.onStartCommand(intent, flags, startId);
     }
 
     @Override
     protected void onHandleIntent(Intent intent) {
         mResolver = this.getContentResolver();
         mContext = this;
         notificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
 
         new ExecuteVibinnaTask(intent);
     }
 
     private static class ExecuteVibinnaTask {
 
         public ExecuteVibinnaTask(Intent intent) {
             if (intent.getIntExtra(TASK_TYPE, 0) == TASK_TYPE_NEW_VFS) {
 
                 Object[] obj = new Object[7];
 
                 obj[0] = intent.getLongExtra(TasksProvider._ID, -1);
 
                 obj[1] = intent.getStringExtra(FOLDER_PATH);
 
                 obj[2] = intent.getIntExtra(CACHE_SIZE, 0);
                 obj[3] = intent.getIntExtra(DATA_SIZE, 0);
                 obj[4] = intent.getIntExtra(SYSTEM_SIZE, 0);
                 obj[5] = intent.getStringExtra(VS_DESC);
                 obj[6] = intent.getIntExtra(ICON_ID, 0);
                 new CreateVFSTask().execute(obj);
             } else if (intent.getIntExtra(TASK_TYPE, 0) == TASK_TYPE_FORMAT_VFS) {
                 Object[] obj = new Object[5];
                 obj[0] = intent.getLongExtra(TasksProvider._ID, -1);
 
                 obj[1] = intent.getStringExtra(FOLDER_PATH);
                 obj[2] = intent.getBooleanExtra(FORMAT_CACHE, false);
                 obj[3] = intent.getBooleanExtra(FORMAT_DATA, false);
                 obj[4] = intent.getBooleanExtra(FORMAT_SYSTEM, false);
                 new FormatVFSTask().execute(obj);
             } else if (intent.getIntExtra(TASK_TYPE, 0) == TASK_TYPE_RESIZE_VFS) {
                 Object[] obj = new Object[5];
                 obj[0] = intent.getLongExtra(TasksProvider._ID, -1);
 
                 obj[1] = intent.getStringExtra(FOLDER_PATH);
                 obj[2] = intent.getIntExtra(CACHE_SIZE, 0);
                 obj[3] = intent.getIntExtra(DATA_SIZE, 0);
                 obj[4] = intent.getIntExtra(SYSTEM_SIZE, 0);
                 new ResizeVFSTask().execute(obj);
             }
             return;
         }
 
         class CreateVFSTask extends AsyncTask<Object[], Void, Void> {
             protected Void doInBackground(Object[]... objs) {
 
                 String _id = (Long) objs[0][0] + Constants.EMPTY;
                 String folderPath = MiscMethods.avoidDuplicateFile(new File((String) objs[0][1]))
                         .getPath();
                 String cachesize = (Integer) objs[0][2] + Constants.EMPTY;
                 String datasize = (Integer) objs[0][3] + Constants.EMPTY;
                 String systemsize = (Integer) objs[0][4] + Constants.EMPTY;
                 String vsDesc = (String) objs[0][5];
                 int iconId = (Integer) objs[0][6];
 
                 String vsName = new File(folderPath).getName();
 
                 ContentValues values = new ContentValues();
                 values.put(DatabaseHelper.TASK_VS, vsName);
                 values.put(DatabaseHelper.TASK_TYPE, TASK_TYPE_NEW_VFS);
                 values.put(DatabaseHelper.TASK_STATUS, TasksAdapter.TASK_STATUS_RUNNING);
                 values.put(DatabaseHelper.TASK_MESSAGE,
                         mContext.getString(R.string.creating_cache, vsName));
                 values.put(DatabaseHelper.TASK_PROGRESS, 1 * 100 / 7);
                 mResolver.update(Uri.withAppendedPath(TasksProvider.CONTENT_URI, _id), values,
                         null, null);
 
                 final Message m1 = new Message();
                 m1.arg1 = 1;
                 m1.obj = new File(folderPath).getName();
                 handler.sendMessage(m1);
 
                 new File(folderPath).mkdir();
                 String[] shellinput = { Constants.EMPTY, Constants.EMPTY, Constants.EMPTY,
                         Constants.EMPTY, Constants.EMPTY };
                 shellinput[1] = folderPath;
                 shellinput[0] = Constants.CMD_DD;
                 shellinput[2] = Constants.CMD_DD_CACHE;
                 shellinput[3] = cachesize;
                 ProcessManager.errorStreamReader(shellinput);
                 values.put(DatabaseHelper.TASK_MESSAGE,
                         mContext.getString(R.string.formating_cache, vsName));
                 values.put(DatabaseHelper.TASK_PROGRESS, 2 * 100 / 7);
                 mResolver.update(Uri.withAppendedPath(TasksProvider.CONTENT_URI, _id), values,
                         null, null);
 
                 final Message m2 = new Message();
                 m2.arg1 = 2;
                 m2.obj = new File(folderPath).getName();
                 handler.sendMessage(m2);
 
                 shellinput[0] = Constants.CMD_MKE2FS_EXT3;
                 shellinput[2] = Constants.CACHE_IMG;
                 shellinput[3] = Constants.EMPTY;
                 ProcessManager.inputStreamReader(shellinput, 20);
 
                 values.put(DatabaseHelper.TASK_MESSAGE,
                         mContext.getString(R.string.creating_data, vsName));
                 values.put(DatabaseHelper.TASK_PROGRESS, 3 * 100 / 7);
                 mResolver.update(Uri.withAppendedPath(TasksProvider.CONTENT_URI, _id), values,
                         null, null);
 
                 final Message m3 = new Message();
                 m3.arg1 = 3;
                 m3.obj = new File(folderPath).getName();
                 handler.sendMessage(m3);
 
                 shellinput[0] = Constants.CMD_DD;
                 shellinput[2] = Constants.CMD_DD_DATA;
                 shellinput[3] = datasize;
                 ProcessManager.errorStreamReader(shellinput);
 
                 values.put(DatabaseHelper.TASK_MESSAGE,
                         mContext.getString(R.string.formating_data, vsName));
                 values.put(DatabaseHelper.TASK_PROGRESS, 4 * 100 / 7);
                 mResolver.update(Uri.withAppendedPath(TasksProvider.CONTENT_URI, _id), values,
                         null, null);
 
                 final Message m4 = new Message();
                 m4.arg1 = 4;
                 m4.obj = new File(folderPath).getName();
                 handler.sendMessage(m4);
 
                 shellinput[0] = Constants.CMD_MKE2FS_EXT3;
                 shellinput[2] = Constants.DATA_IMG;
                 shellinput[3] = Constants.EMPTY;
                 ProcessManager.inputStreamReader(shellinput, 20);
 
                 values.put(DatabaseHelper.TASK_MESSAGE,
                         mContext.getString(R.string.creating_system, vsName));
                 values.put(DatabaseHelper.TASK_PROGRESS, 5 * 100 / 7);
                 mResolver.update(Uri.withAppendedPath(TasksProvider.CONTENT_URI, _id), values,
                         null, null);
 
                 final Message m5 = new Message();
                 m5.arg1 = 5;
                 m5.obj = new File(folderPath).getName();
                 handler.sendMessage(m5);
 
                 shellinput[0] = Constants.CMD_DD;
                 shellinput[2] = Constants.CMD_DD_SYSTEM;
                 shellinput[3] = systemsize;
                 ProcessManager.errorStreamReader(shellinput);
 
                 values.put(DatabaseHelper.TASK_MESSAGE,
                         mContext.getString(R.string.formating_system, vsName));
                 values.put(DatabaseHelper.TASK_PROGRESS, 6 * 100 / 7);
                 mResolver.update(Uri.withAppendedPath(TasksProvider.CONTENT_URI, _id), values,
                         null, null);
 
                 final Message m6 = new Message();
                 m6.arg1 = 6;
                 m6.obj = new File(folderPath).getName();
                 handler.sendMessage(m6);
 
                 shellinput[0] = Constants.CMD_MKE2FS_EXT3;
                 shellinput[2] = Constants.SYSTEM_IMG;
                 shellinput[3] = Constants.EMPTY;
                 ProcessManager.inputStreamReader(shellinput, 20);
 
                 ContentValues vValues = new ContentValues();
                 vValues.put(DatabaseHelper.VIRTUAL_SYSTEM_COLUMN_NAME, vsName);
                 vValues.put(DatabaseHelper.VIRTUAL_SYSTEM_COLUMN_PATH, folderPath);
                 vValues.put(DatabaseHelper.VIRTUAL_SYSTEM_COLUMN_DESCRIPTION, vsDesc);
                 vValues.put(DatabaseHelper.VIRTUAL_SYSTEM_COLUMN_TYPE, iconId + Constants.EMPTY);
                 mResolver.insert(VibhinnaProvider.CONTENT_URI, vValues);
 
                 values.put(DatabaseHelper.TASK_STATUS, TasksAdapter.TASK_STATUS_FINISHED);
                 values.put(DatabaseHelper.TASK_MESSAGE,
                         mContext.getString(R.string.task_creation_complete, vsName));
                 values.put(DatabaseHelper.TASK_PROGRESS, 100);
                 mResolver.update(Uri.withAppendedPath(TasksProvider.CONTENT_URI, _id), values,
                         null, null);
 
                 final Message endMessage = new Message();
                 endMessage.arg1 = 7;
                 endMessage.obj = new File(folderPath).getName();
                 handler.sendMessage(endMessage);
                 return null;
             }
         }
 
         class FormatVFSTask extends AsyncTask<Object[], Void, Void> {
 
             @Override
             protected Void doInBackground(Object[]... objs) {
                 String _id = String.valueOf((Long) objs[0][0]);
                 String mPath = (String) objs[0][1];
                 boolean cacheCheckBool = (Boolean) objs[0][2];
                 boolean dataCheckBool = (Boolean) objs[0][3];
                 boolean systemCheckBool = (Boolean) objs[0][4];
 
                 // Calculate total partitions to format
                 int maxOp = 1;
                 if (cacheCheckBool)
                     maxOp++;
                 if (dataCheckBool)
                     maxOp++;
                 if (systemCheckBool)
                     maxOp++;
 
                 int progress = 0;
                 String vsName = new File(mPath).getName();
 
                 ContentValues values = new ContentValues();
                 values.put(DatabaseHelper.TASK_STATUS, TasksAdapter.TASK_STATUS_RUNNING);
                 mResolver.update(Uri.withAppendedPath(TasksProvider.CONTENT_URI, _id), values,
                         null, null);
 
                 String[] shellinput = { Constants.EMPTY, Constants.EMPTY, Constants.EMPTY,
                         Constants.EMPTY, Constants.EMPTY };
                 shellinput[0] = Constants.CMD_MKE2FS_EXT3;
                 shellinput[1] = mPath;
                 final Message m0 = new Message();
                 final Message m1 = new Message();
                 final Message m2 = new Message();
                 final Message m3 = new Message();
                 m0.arg1 = 8;
                 m1.arg1 = 9;
                 m2.arg1 = 10;
                 m3.arg1 = 11;
                 m0.obj = m1.obj = m2.obj = m3.obj = vsName;
                 if (cacheCheckBool) {
                     values.put(DatabaseHelper.TASK_MESSAGE,
                             mContext.getString(R.string.formating_cache, vsName));
                     progress = progress + 100;
                     values.put(DatabaseHelper.TASK_PROGRESS, progress / maxOp);
                     mResolver.update(Uri.withAppendedPath(TasksProvider.CONTENT_URI, _id), values,
                             null, null);
                     handler.sendMessage(m0);
                     shellinput[2] = Constants.CACHE_IMG;
                     ProcessManager.inputStreamReader(shellinput, 20);
                 }
                 if (dataCheckBool) {
                     values.put(DatabaseHelper.TASK_MESSAGE,
                             mContext.getString(R.string.formating_data, vsName));
                     progress = progress + 100;
                     values.put(DatabaseHelper.TASK_PROGRESS, progress / maxOp);
                     mResolver.update(Uri.withAppendedPath(TasksProvider.CONTENT_URI, _id), values,
                             null, null);
                     handler.sendMessage(m1);
                     shellinput[2] = Constants.DATA_IMG;
                     ProcessManager.inputStreamReader(shellinput, 20);
                     dataCheckBool = false;
                 }
                 if (systemCheckBool) {
                     values.put(DatabaseHelper.TASK_MESSAGE,
                             mContext.getString(R.string.formating_system, vsName));
                     progress = progress + 100;
                     values.put(DatabaseHelper.TASK_PROGRESS, progress / maxOp);
                     mResolver.update(Uri.withAppendedPath(TasksProvider.CONTENT_URI, _id), values,
                             null, null);
                     handler.sendMessage(m2);
                     shellinput[2] = Constants.SYSTEM_IMG;
                     ProcessManager.inputStreamReader(shellinput, 20);
                     systemCheckBool = false;
                 }
                 values.put(DatabaseHelper.TASK_MESSAGE,
                         mContext.getString(R.string.task_formating_complete, vsName));
                 progress = progress + 100;
                 values.put(DatabaseHelper.TASK_PROGRESS, 100);
                 values.put(DatabaseHelper.TASK_STATUS, TasksAdapter.TASK_STATUS_FINISHED);
                 mResolver.update(Uri.withAppendedPath(TasksProvider.CONTENT_URI, _id), values,
                         null, null);
                 handler.sendMessage(m3);
                 return null;
             }
         }
 
         class ResizeVFSTask extends AsyncTask<Object[], Void, Void> {
 
             @Override
             protected Void doInBackground(Object[]... objs) {
                 String _id = String.valueOf((Long) objs[0][0]);
                 String mPath = (String) objs[0][1];
                 int cacheSize = (Integer) objs[0][2];
                 int dataSize = (Integer) objs[0][3];
                 int systemSize = (Integer) objs[0][4];
 
                 final String vsName = new File(mPath).getName();
                 final Uri uri = Uri.withAppendedPath(TasksProvider.CONTENT_URI, _id);
 
                 final Message m0 = new Message();
                 final Message m1 = new Message();
                 final Message m2 = new Message();
                 final Message m3 = new Message();
 
                 m0.arg1 = 12;
                 m1.arg1 = 13;
                 m2.arg1 = 14;
                 m3.arg1 = 15;
 
                 m0.obj = vsName;
                 m1.obj = vsName;
                 m2.obj = vsName;
                 m3.obj = vsName;
 
                 ContentValues values = new ContentValues();
                 values.put(DatabaseHelper.TASK_STATUS, TasksAdapter.TASK_STATUS_RUNNING);
                 values.put(DatabaseHelper.TASK_MESSAGE,
                         mContext.getString(R.string.resizing_message, vsName + Constants.CACHE_IMG));
                 values.put(DatabaseHelper.TASK_PROGRESS, 25);
                 mResolver.update(uri, values, null, null);
                 handler.sendMessage(m0);
                 String[] shellinput = { Constants.CMD_E2FSCK, mPath, Constants.CACHE_IMG,
                         Constants.EMPTY, Constants.EMPTY };
                 ProcessManager.errorStreamReader(shellinput);
                 shellinput[0] = Constants.CMD_RESIZE2FS;
                 shellinput[3] = String.valueOf(cacheSize);
                 shellinput[4] = "M";
                 ProcessManager.inputStreamReader(shellinput, 5);
 
                 values.put(DatabaseHelper.TASK_PROGRESS, 50);
                 values.put(DatabaseHelper.TASK_MESSAGE,
                         mContext.getString(R.string.resizing_message, vsName + Constants.DATA_IMG));
                 mResolver.update(uri, values, null, null);
                 handler.sendMessage(m1);
                 shellinput[0] = Constants.CMD_E2FSCK;
                 shellinput[2] = Constants.DATA_IMG;
                 shellinput[3] = Constants.EMPTY;
                 shellinput[4] = Constants.EMPTY;
                 ProcessManager.errorStreamReader(shellinput);
                 shellinput[0] = Constants.CMD_RESIZE2FS;
                 shellinput[3] = String.valueOf(dataSize);
                 shellinput[4] = "M";
                 ProcessManager.inputStreamReader(shellinput, 5);
 
                 values.put(DatabaseHelper.TASK_PROGRESS, 75);
                 values.put(DatabaseHelper.TASK_MESSAGE, mContext.getString(
                         R.string.resizing_message, vsName + Constants.SYSTEM_IMG));
                 mResolver.update(uri, values, null, null);
                 handler.sendMessage(m2);
                 shellinput[0] = Constants.CMD_E2FSCK;
                 shellinput[2] = Constants.SYSTEM_IMG;
                 shellinput[3] = Constants.EMPTY;
                 shellinput[4] = Constants.EMPTY;
                 ProcessManager.errorStreamReader(shellinput);
                 shellinput[0] = Constants.CMD_RESIZE2FS;
                 shellinput[3] = String.valueOf(systemSize);
                 shellinput[4] = "M";
                 ProcessManager.inputStreamReader(shellinput, 5);
 
                 values.put(DatabaseHelper.TASK_PROGRESS, 100);
                 values.put(DatabaseHelper.TASK_STATUS, TasksAdapter.TASK_STATUS_FINISHED);
                 mResolver.update(uri, values, null, null);
                 handler.sendMessage(m3);
                 return null;
             }
         }
     }
 
     private static Handler handler = new Handler() {
 
         @Override
         public void handleMessage(Message msg) {
             String vsName = (String) msg.obj;
             mLocalBroadcastManager.sendBroadcast(tasksUpdatedIntent);
             switch (msg.arg1) {
                 case 1: {
 
                     displayNotificationMessage(mContext.getString(R.string.creating_cache, vsName),
                             false);
                     return;
                 }
                 case 2: {
                     displayNotificationMessage(
                             mContext.getString(R.string.formating_cache, vsName), false);
                     return;
                 }
                 case 3: {
                     displayNotificationMessage(mContext.getString(R.string.creating_data, vsName),
                             false);
                     return;
                 }
                 case 4: {
                     displayNotificationMessage(mContext.getString(R.string.formating_data, vsName),
                             false);
                     return;
                 }
                 case 5: {
                     displayNotificationMessage(
                             mContext.getString(R.string.creating_system, vsName), false);
                     return;
                 }
                 case 6: {
                     displayNotificationMessage(
                             mContext.getString(R.string.formating_system, vsName), false);
                     return;
                 }
                 case 7:
                     mLocalBroadcastManager.sendBroadcast(vfsListUpdatedIntent);
                     displayNotificationMessage(
                             mContext.getString(R.string.task_creation_complete, vsName), true);
                     return;
                 case 8:
                     displayNotificationMessage(
                             mContext.getString(R.string.formating_cache, vsName), false);
                     return;
                 case 9:
                     displayNotificationMessage(mContext.getString(R.string.formating_data, vsName),
                             false);
                     return;
                 case 10:
                     displayNotificationMessage(
                             mContext.getString(R.string.formating_system, vsName), false);
                     return;
                 case 11:
                     displayNotificationMessage(
                             mContext.getString(R.string.task_formating_complete, vsName), true);
                     return;
                 case 12:
                     displayNotificationMessage(
                             mContext.getString(R.string.resizing_message, vsName), false);
                     return;
                 case 13:
                     return;
                 case 14:
                     return;
                 case 15:
                     displayNotificationMessage(
                             mContext.getString(R.string.resizing_complete_message, vsName), true);
                     mLocalBroadcastManager.sendBroadcast(vfsListUpdatedIntent);
                     return;
             }
         }
     };
 
     private static void displayNotificationMessage(String message, boolean cancellable) {
         PendingIntent intent = PendingIntent.getActivity(mContext, 0, new Intent(mContext,
                 VibhinnaActivity.class), 0);
         Notification notification = new NotificationCompat.Builder(mContext)
                 .setContentTitle(mContext.getString(R.string.app_name))
                 .setContentText(message)
                 .setContentIntent(intent)
                 .setSmallIcon(R.drawable.ic_notification)
                 .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(),
                         R.drawable.ic_notification))
                 .setWhen(System.currentTimeMillis())
                 .setAutoCancel(cancellable ? true : false)
                 .setOngoing(cancellable ? false : true)
                .getNotification();
         notificationManager.notify(0, notification);
     }
 }
