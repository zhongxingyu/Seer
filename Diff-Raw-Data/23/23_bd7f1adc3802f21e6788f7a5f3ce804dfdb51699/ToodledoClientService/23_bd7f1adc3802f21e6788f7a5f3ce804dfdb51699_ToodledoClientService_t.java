 package com.gmail.altakey.mint;
 
 import android.app.Service;
 import android.app.IntentService;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.ContentValues;
 import android.content.ContentResolver;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Binder;
 import android.os.IBinder;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonArray;
 import com.google.gson.reflect.TypeToken;
 
 import java.util.List;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Date;
 import java.io.IOException;
 
 public class ToodledoClientService extends IntentService {
     public static final String ACTION_SYNC = "com.gmail.altakey.mint.SYNC";
     public static final String ACTION_SYNC_DONE = "com.gmail.altakey.mint.SYNC_DONE";
 
     public static final String EXTRA_TASKS = "tasks";
     public static final String EXTRA_TASK_FIELDS = "task_fields";
 
     private DB mDB;
     private ToodledoClient mClient;
 
     public ToodledoClientService() {
         super("ToodledoClientService");
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
         mClient = new ToodledoClient(Authenticator.create(this), this);
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
     }
 
     public static String asListOfTasks(Task... tasks) {
         return getGson().toJson(tasks, Task[].class);
     }
 
     @Override
     protected void onHandleIntent(Intent intent) {
         final String action = intent.getAction();
 
         try {
             if (ACTION_SYNC.equals(action)) {
                 sync();
                 sync_done();
             }
         } catch (IOException e) {
             abort(e.getMessage());
         } catch (Authenticator.FailureException e) {
             fail();
         } catch (Authenticator.BogusException e) {
             require();
         } catch (Authenticator.Exception e) {
             abort(e.getMessage());
         }
     }
 
     private void abort(String message) {
         new Notifier(this).boo(String.format("sync failure: %s", message));
     }
 
     private void fail() {
         new Notifier(this).notify("Login failed", Notifier.NOTIFY_LOGIN_FAILED);
     }
 
     private void require() {
         new Notifier(this).notifyOnce("Setup synchronization", Notifier.NOTIFY_LOGIN_REQUIRED);
     }
 
     private void sync() throws IOException, Authenticator.Exception {
         final Notifier notifier = new Notifier(this);
         try {
             notifier.notify("Syncing...", "SYNC");
             final Synchronizer sync = new Synchronizer(this, mClient);
             sync.update();
             sync.commit();
         } finally {
             notifier.cancel();
         }
     }
 
     private void sync_done() {
         final Intent intent = new Intent(ACTION_SYNC_DONE);
         LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
     }
 
     private static Gson getGson() {
         final GsonBuilder builder = new GsonBuilder();
         builder.registerTypeAdapter(TaskStatus.class, new TaskStatus.JsonAdapter());
         builder.registerTypeAdapter(TaskFolder.class, new TaskFolder.JsonAdapter());
         builder.registerTypeAdapter(Task.class, new Task.JsonAdapter());
         builder.registerTypeAdapter(TaskContext.class, new TaskContext.JsonAdapter());
         return builder.create();
     }
 
     public static class Synchronizer {
         private Context mmContext;
         private ToodledoClient mmClient;
 
         public Synchronizer(final Context context, final ToodledoClient client) {
             mmContext = context;
             mmClient = client;
         }
 
         private Map<String, Long> updatedSince(TaskStatus s) {
             final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mmContext);
             final Map<String, Long> out = new HashMap<String, Long>();
             out.put("folder", pref.getLong("lastedit_folder", 0));
             out.put("context", pref.getLong("lastedit_context", 0));
             out.put("task", pref.getLong("lastedit_task", 0));
             out.put("task_delete", pref.getLong("lastedit_task_delete", 0));
             return out;
         }
 
         public void update() throws IOException, Authenticator.BogusException, Authenticator.FailureException, Authenticator.ErrorException {
             final TaskStatus st = mmClient.getStatus();
             final Map<String, Long> flags = updatedSince(st);
             final Map<String, List<?>> data = new HashMap<String, List<?>>();
 
             if (flags.containsKey("folder_delete")) {
                 data.put("folder_delete", mmClient.getFoldersDeletedAfter(flags.get("folder_delete")));
             }
 
             if (flags.containsKey("task_delete")) {
                 data.put("task_delete", mmClient.getTasksDeletedAfter(flags.get("task_delete")));
             }
 
             if (flags.containsKey("folder")) {
                 data.put("folder", mmClient.getFoldersAfter(flags.get("folder")));
             }
 
             if (flags.containsKey("context")) {
                 data.put("context", mmClient.getContextsAfter(flags.get("context")));
             }
 
             if (flags.containsKey("task")) {
                 data.put("task", mmClient.getTasksAfter(flags.get("task")));
             }
 
             final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mmContext);
             pref.edit()
                 .putLong("lastedit_folder", st.lastedit_folder)
                 .putLong("lastedit_context", st.lastedit_context)
                 .putLong("lastedit_goal", st.lastedit_goal)
                 .putLong("lastedit_location", st.lastedit_location)
                 .putLong("lastedit_task", st.lastedit_task)
                 .putLong("lastdelete_task", st.lastdelete_task)
                 .putLong("lastedit_notebook", st.lastedit_notebook)
                 .putLong("lastdelete_notebook", st.lastdelete_notebook)
                 .commit();
 
             final ContentResolver resolver = mmContext.getContentResolver();
             if (data.containsKey("folder_delete")) {
                 final List<String> args = new LinkedList<String>();
                 for (TaskFolder t : (List<TaskFolder>)data.get("folder_delete")) {
                     args.add(String.valueOf(t.id));
                 }
                 resolver.delete(TaskFolderProvider.CONTENT_URI, TaskFolderProvider.MULTIPLE_FOLDERS_FILTER, args.toArray(new String[] {}));
             }
 
             if (data.containsKey("task_delete")) {
                 final List<String> args = new LinkedList<String>();
                 for (Task t : (List<Task>)data.get("task_delete")) {
                     args.add(String.valueOf(t.id));
                 }
                 resolver.delete(TaskProvider.CONTENT_URI, TaskProvider.MULTIPLE_TASKS_FILTER, args.toArray(new String[] {}));
             }
 
             if (data.containsKey("folder")) {
                 final List<ContentValues> rows = new LinkedList<ContentValues>();
                 for (TaskFolder t : (List<TaskFolder>)data.get("folder")) {
                     final ContentValues row = new ContentValues();
                     row.put(TaskFolderProvider.COLUMN_FOLDER, t.id);
                     row.put(TaskFolderProvider.COLUMN_NAME, t.name);
                     row.put(TaskFolderProvider.COLUMN_PRIVATE, t.private_);
                     row.put(TaskFolderProvider.COLUMN_ARCHIVED, t.archived);
                     row.put(TaskFolderProvider.COLUMN_ORD, t.ord);
                     rows.add(row);
                 }
                 resolver.bulkInsert(TaskFolderProvider.CONTENT_URI, rows.toArray(new ContentValues[] {}));
             }
 
             if (data.containsKey("context")) {
                 final List<ContentValues> rows = new LinkedList<ContentValues>();
                 for (TaskContext t : (List<TaskContext>)data.get("context")) {
                     final ContentValues row = new ContentValues();
                     row.put(TaskContextProvider.COLUMN_CONTEXT, t.id);
                     row.put(TaskContextProvider.COLUMN_NAME, t.name);
                     rows.add(row);
                 }
                 resolver.bulkInsert(TaskContextProvider.CONTENT_URI, rows.toArray(new ContentValues[] {}));
             }
 
             if (data.containsKey("task")) {
                 final List<ContentValues> rows = new LinkedList<ContentValues>();
                 for (Task t : (List<Task>)data.get("task")) {
                     final ContentValues row = new ContentValues();
                     row.put(TaskProvider.COLUMN_TASK, t.id);
                     row.put(TaskProvider.COLUMN_TITLE, t.title);
                     row.put(TaskProvider.COLUMN_NOTE, t.note);
                     row.put(TaskProvider.COLUMN_MODIFIED, t.modified);
                     row.put(TaskProvider.COLUMN_COMPLETED, t.completed);
                     row.put(TaskProvider.COLUMN_FOLDER, t.folder);
                     row.put(TaskProvider.COLUMN_CONTEXT, t.context);
                     row.put(TaskProvider.COLUMN_PRIORITY, t.priority);
                     row.put(TaskProvider.COLUMN_STAR, t.star);
                     row.put(TaskProvider.COLUMN_DUEDATE, t.duedate);
                     row.put(TaskProvider.COLUMN_DUETIME, t.duetime);
                     row.put(TaskProvider.COLUMN_STATUS, t.status);
                     rows.add(row);
                 }
                 resolver.bulkInsert(TaskProvider.CONTENT_URI, rows.toArray(new ContentValues[] {}));
             }
         }
 
         public void commit() throws IOException, Authenticator.BogusException, Authenticator.FailureException, Authenticator.ErrorException {
             final DB db = new DB(mmContext);
             final SQLiteDatabase conn = db.open();
             try {
                 conn.beginTransaction();
 
                 final TaskStatus st = mmClient.getStatus();
                mmClient.commitTasks(db.getTasks(String.format("tasks.task is null or tasks.modified > %d", st.lastedit_task), null), new String[] { "note", "duedate", "duetime" });
             } finally {
                 conn.endTransaction();
             }
         }
     }
 }
