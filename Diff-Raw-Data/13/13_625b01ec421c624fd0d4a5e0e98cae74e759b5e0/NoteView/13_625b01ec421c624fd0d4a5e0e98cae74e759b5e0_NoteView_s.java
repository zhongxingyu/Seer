 /* *************************************************************************************************
  *                                         eNotes                                                  *
  * *************************************************************************************************
  * File:        NoteView.java                                                                      *
  * Copyright:   (c) 2011-2012 Emanuele Alimonda, Giovanni Serra                                    *
  *              eNotes is free software: you can redistribute it and/or modify it under the terms  *
  *              of the GNU General Public License as published by the Free Software Foundation,    *
  *              either version 3 of the License, or (at your option) any later version.  eNotes is *
  *              distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without  *
  *              even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  *
  *              See the GNU General Public License for more details.  You should have received a   *
  *              copy of the GNU General Public License along with eNotes.                          *
  *              If not, see <http://www.gnu.org/licenses/>                                         *
  * *************************************************************************************************/
 
 package it.unica.enotes;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.format.Time;
import android.text.method.LinkMovementMethod;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.TextView;
 
 /**
  * Activity to display the contents of a single note
  * @author Emanuele Alimonda
  * @author Giovanni Serra
  */
 public class NoteView extends Activity {
    private static final int kMenuItemEdit = 100;
    private static final int kMenuItemDelete = 101;
    private static final int kMenuItemSend = 102;
    long _noteID;
 
    private static final String kTag = "NoteView";
    private NoteDB database;
 
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       Log.v(kTag, "created activity");
       this._noteID = -1;
       setContentView(R.layout.view);
       //Bundle extras = getIntentactivity().getExtras();
       Bundle extras = getIntent().getExtras();
       if (extras == null) {
          Log.v(kTag, "Invalid or missing bundle");
          return;
       }
 
       // shows selected note's details
       this._noteID = extras.getLong(Note.kID);
       Log.v(kTag, "Loading note: " + this._noteID);
       database = new NoteDB();
    }
 
    @Override
    public void onResume() {
       super.onResume();
 
       Note note = database.getNoteById(this, this._noteID);
 
       TextView titleField = (TextView) findViewById(R.id.ViewTitle);
       TextView contentsField = (TextView) findViewById(R.id.ViewContents);
       TextView attachmentField = (TextView) findViewById(R.id.ViewAttachments);
       TextView urlField = (TextView) findViewById(R.id.ViewUrl);   
       TextView tagsField = (TextView) findViewById(R.id.ViewTags);
 
       titleField.setText(note.getTitle());
       contentsField.setText(note.getText());
       attachmentField.setText(note.getAttachment().getFilename());
       urlField.setText(note.getURL());
       tagsField.setText(note.getTagsAsString());      
 
       // Do some temp files cleanup (Why here?  See note below.)
       String tmpDirPath = System.getProperty("java.io.tmpdir");
       File tmpDir = new File(tmpDirPath, "eNotesTmp");
       if (tmpDir.exists() && tmpDir.isDirectory()) {
          File[] tmpFileList = tmpDir.listFiles();
          Time thresholdTimestamp = new Time();
          thresholdTimestamp.setToNow();
          thresholdTimestamp.set(thresholdTimestamp.toMillis(true)-1000*3600*24); // 24 hours
          for (int i = 0; i < tmpFileList.length; i++) {
             if (!tmpFileList[i].exists() || !tmpFileList[i].isFile()) {
                continue;
             }
             if (!tmpFileList[i].toString().endsWith(".eNote")) {
                continue;
             }
             if (tmpFileList[i].lastModified() < thresholdTimestamp.toMillis(true)) {
                tmpFileList[i].delete();
                Log.v(kTag, "Deleted temp file " + tmpFileList.toString());
             }
             Log.v(kTag, "check done");
          }
       }
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       menu.add(0, kMenuItemEdit, 1, R.string.editItem).setIcon(getResources().getDrawable(R.drawable.ic_menu_edit));
       menu.add(0, kMenuItemDelete, 2, R.string.deleteItem).setIcon(getResources().getDrawable(R.drawable.ic_menu_delete));
       menu.add(0, kMenuItemSend, 3, R.string.sendItem).setIcon(getResources().getDrawable(R.drawable.ic_menu_send));
       return true;
    }
 
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
       if (item.getItemId() == kMenuItemEdit) {
          Intent i = new Intent(this, NoteEdit.class);
          i.putExtra(Note.kID, this._noteID);
          startActivityForResult(i, 0);
       } else if (item.getItemId() == kMenuItemDelete) {
          if (database.deleteNote(this, this._noteID)) {
             new AlertDialog.Builder(this).setTitle(R.string.deleteConf)                  
             	  .setMessage(R.string.deleteMsg)            
                   .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                   finish();
                }
             })
                .setNeutralButton(R.string.cancel, null) // don't need to do anything but dismiss here
                .create()
                .show();
          }
       } else if (item.getItemId() == kMenuItemSend) {
          try {
             Note note = database.getNoteById(this, this._noteID);
             String tmpDirPath = System.getProperty("java.io.tmpdir");
             File tmpDir = new File(tmpDirPath, "eNotesTmp");
             if (tmpDir.exists() && !tmpDir.isDirectory()) {
                tmpDir.delete();
             }
             if (!tmpDir.exists() && !tmpDir.mkdirs()) {
                throw new IOException();
             }
             File attachmentFile = File.createTempFile("eNote.", ".eNote", tmpDir);
             FileWriter attachmentWriter = new FileWriter(attachmentFile);
             attachmentWriter.write(note.getJSON());
             attachmentWriter.close();
 
             Intent emailIntent = new Intent(Intent.ACTION_SEND);
             emailIntent.setType("plain/text");
             emailIntent.putExtra(Intent.EXTRA_SUBJECT, "[eNote] "+ note.getTitle());
             emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+ attachmentFile.getPath()));
             Log.v(kTag, "Sending: "+attachmentFile.toURI());
             startActivity(Intent.createChooser(emailIntent, "Send email:"));
             /* Note about temporary file deletion:
              * - We can't delete the file right away, since we don't know whether the email has been
              *   sent yet (and since the intent returns right away, we're pretty much sure it
              *   *hasn't*.  Emails on a mobile device may be deferred to send when a 3g or wifi
              *   connection is available anyways.
              * - We can't have the JVM delete the file when it's no longer needed, since on Android,
              *   deleteOnExit() isn't reliable (cleanup done on VM termination, but VM termination
              *   isn't part of the app's lifecycle.
              * - We can say Android sucks when it comes to temporary files and all I can do is to,
              *   in onResume, delete all files matching my own wildcard filename structure that are
              *   older than 24 hours, hoping the user ran into a 3g or wifi network during the last
              *   day.  Oh well, if I delete something you care about or leave cruft behind, you can
              *   go complain to the Android engineers.  Or switch to an Apple or MS device.
              */
          } catch (IOException e) {
             Log.v(kTag, e.toString());
             return false;
          };
       }
       return true;
    }
 
 }
 
 /* vim: set ts=3 sw=3 smarttab expandtab cc=101 : */
