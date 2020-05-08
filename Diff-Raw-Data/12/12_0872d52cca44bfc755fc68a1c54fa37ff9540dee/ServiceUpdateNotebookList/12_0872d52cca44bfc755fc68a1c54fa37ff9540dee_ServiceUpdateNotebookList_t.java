 package com.suyuxin.suvermemo;
 
 import android.app.IntentService;
 import android.app.NotificationManager;
 import android.content.Context;
 import android.content.Intent;
 import android.support.v4.app.NotificationCompat;
 import android.util.Log;
 import com.evernote.client.android.EvernoteSession;
 import com.evernote.edam.notestore.NoteFilter;
 import com.evernote.edam.notestore.NoteStore;
 import com.evernote.edam.type.Notebook;
 
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 
 /**
  * Created by suyuxin on 13-5-25.
  */
 public class ServiceUpdateNotebookList extends ServiceDownload {
 
     private static final String LogTag = "Update Notebook List";
 
    public ServiceUpdateNotebookList() {
        super("name");
     }
 
     @Override
     protected void onHandleIntent(Intent intent) {
         try {
             List<Notebook> cloud_notebooks = client.listNotebooks(auth_token);
             //get count of notes in each notebook
             Map<String, Integer> cloud_counts = client.findNoteCounts(auth_token, new NoteFilter(), false)
                     .getNotebookCounts();
             //open database
             database.open();
             notebook_info = database.getNotebookList();
             updateProgress(LogTag, "Get Notebook list from Cloud", 0);
             ListIterator<Notebook> iterator = cloud_notebooks.listIterator();
             //for update progress
             int index = 0;
             while(iterator.hasNext())
             {
                 Notebook notebook = iterator.next();
                 String guid = notebook.getGuid();
                 if(notebook_info.containsKey(guid))
                 {
                     if(notebook_info.get(guid).note_number != cloud_counts.get(guid))
                     {//need to update
                         database.updateNotebook(notebook.getName(), guid, cloud_counts.get(guid));
                     }
                     updateProgress(LogTag, "Update Notebook: " + notebook.getName(), 0);
                 }
                 else
                 {//store new notebook into database
                     database.insertNotebook(notebook.getName(), guid, cloud_counts.get(guid));
                     updateProgress(LogTag, "Download Notebook: " + notebook.getName(), 0);
                 }
                 //update progress
                 index++;
             }
 
         } catch (Exception e) {
             // TODO Auto-generated catch block
             Log.e(LogTag, e.toString(), e);
         }finally{
             stopSelf();
         }
 
     }
 }
