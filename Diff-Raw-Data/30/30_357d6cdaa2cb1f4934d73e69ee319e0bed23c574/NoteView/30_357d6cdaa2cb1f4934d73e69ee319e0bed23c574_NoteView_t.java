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
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.nio.channels.FileChannel;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 /**
  * Activity to display the contents of a single note
  * @author Emanuele Alimonda
  * @author Giovanni Serra
  */
 public class NoteView extends Activity {
    /** Menu IDs */
    private static final int kMenuItemEdit = 100;
    private static final int kMenuItemDelete = 101;
    private static final int kMenuItemSend = 102;
    /** ID of the current note */
    long _noteID;
    /** The current note */
    Note _note;
    /** Database helper / content provider */
    private NoteDB _database;
    /** Logging tag */
    private static final String kTag = "NoteView";
 
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
      this._database = null;
      this._note = null;
 
       // shows selected note's details
       this._noteID = extras.getLong(Note.kID);
       Log.v(kTag, "Loading note: " + this._noteID);
    }
 
    @Override
    public void onResume() {
       super.onResume();
 
      this.loadData();
 
       TextView titleField = (TextView)findViewById(R.id.ViewTitle);
       TextView contentsField = (TextView)findViewById(R.id.ViewContents);
       Button attachmentField = (Button)findViewById(R.id.ViewAttachment);
       TextView urlField = (TextView)findViewById(R.id.ViewUrl);
       TextView tagsField = (TextView)findViewById(R.id.ViewTags);
       LinearLayout attachmentBox = (LinearLayout)findViewById(R.id.ViewAttachmentLayout);
       LinearLayout urlBox = (LinearLayout)findViewById(R.id.ViewUrlLayout);
       LinearLayout tagsBox = (LinearLayout)findViewById(R.id.ViewTagsLayout);
 
       titleField.setText(this._note.getTitle());
       contentsField.setText(this._note.getText());
 
       if (this._note.getAttachment().getFiletype() != NoteAttachment.kFileTypeInvalid) {
          attachmentBox.setVisibility(View.VISIBLE);
          attachmentField.setText(this._note.getAttachment().getFilename());
       } else {
          attachmentBox.setVisibility(View.GONE);
       }
 
       if (this._note.getURL() == "") {
          urlBox.setVisibility(View.GONE);
       } else {
          urlField.setText(this._note.getURL());
          urlBox.setVisibility(View.VISIBLE);
       }
       
       if (this._note.getTagsAsString().trim().length() <= 0) {
          tagsBox.setVisibility(View.GONE);
       } else {
          tagsField.setText(this._note.getTagsAsString());
          tagsBox.setVisibility(View.VISIBLE);
       }
    }
 
    @Override
   public void onStart() {
      super.onStart();

      this.loadData();
   }
   
   /** Ensure activity state after rotation, etc */
   private void loadData() {
      if (this._database == null) {
         this._database = new NoteDB();
      }
      if (this._note == null) {
         this._note = this._database.getNoteById(this, this._noteID);
      }
   }

   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       menu.add(0, kMenuItemEdit, 1, R.string.editItem)
             .setIcon(getResources().getDrawable(R.drawable.ic_menu_edit));
       menu.add(0, kMenuItemDelete, 2, R.string.deleteItem)
             .setIcon(getResources().getDrawable(R.drawable.ic_menu_delete));
       menu.add(0, kMenuItemSend, 3, R.string.sendItem)
             .setIcon(getResources().getDrawable(R.drawable.ic_menu_send));
       return true;
    }
 
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
       switch (item.getItemId()) {
       case kMenuItemEdit:
       {  // Edit note
          Intent i = new Intent(this, NoteEdit.class);
          i.putExtra(Note.kID, this._noteID);
          startActivity(i);
       }
          break;
       case kMenuItemDelete:
       {  // Delete note
          if (this._database.deleteNote(this, this._noteID)) {
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
       }
          break;
       case kMenuItemSend:
       {  // Send note
          try {
             File tmpDir = Note.getSharedTmpDir();
             File attachmentFile = File.createTempFile("eNote.", ".eNote", tmpDir);
             FileWriter attachmentWriter = new FileWriter(attachmentFile);
             attachmentWriter.write(this._note.getJSON());
             attachmentWriter.close();
 
             Intent emailIntent = new Intent(Intent.ACTION_SEND);
             emailIntent.setType("plain/text");
             emailIntent.putExtra(Intent.EXTRA_SUBJECT, "[eNote] "+ this._note.getTitle());
             emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+ attachmentFile.getPath()));
             Log.v(kTag, "Sending: "+attachmentFile.toURI());
             startActivity(Intent.createChooser(emailIntent, getString(R.string.sendMail)));
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
          } catch (FileNotFoundException e) {
             e.printStackTrace();
             return false;
          } catch (IOException e) {
             e.printStackTrace();
             return false;
          }
       }
          break;
       default:
          return false;
       }
       return true;
    }
 
    /**
     * View the note's attachment through its associated system application.
     * The file is saved in the temporary directory before being displayed.
     * @param view Originating view
     */
    public void viewAttachment(View view) {
       NoteAttachment attachment = this._note.getAttachment();
       String fileExtension = "";
       String mimeType = "*/*";
       switch (attachment.getFiletype()) {
       case NoteAttachment.kFileTypePicture:
          fileExtension = NoteAttachment.kFileExtensionPicture;
          mimeType = NoteAttachment.kFileMimePicture;
          break;
       case NoteAttachment.kFileTypeVideo:
          fileExtension = NoteAttachment.kFileExtensionVideo;
          mimeType = NoteAttachment.kFileMimeVideo;
          break;
       case NoteAttachment.kFileTypeAudio:
          fileExtension = NoteAttachment.kFileExtensionAudio;
          mimeType = NoteAttachment.kFileMimeAudio;
          break;
       default:
          return;
       }
       try {
          File tmpDir = Note.getSharedTmpDir();
          File attachmentFile = File.createTempFile("Attachment.", fileExtension, tmpDir);
          FileChannel writeChannel = new FileOutputStream(attachmentFile, false).getChannel();
          writeChannel.write(attachment.getRawData());
          writeChannel.close();
 
          Intent openIntent = new Intent(Intent.ACTION_VIEW);
          openIntent.setDataAndType(Uri.fromFile(attachmentFile), mimeType);
          startActivity(openIntent);
       } catch (FileNotFoundException e) {
          e.printStackTrace();
          return;
       } catch (IOException e) {
          e.printStackTrace();
          return;
       }
    }
 }
 
 /* vim: set ts=3 sw=3 smarttab expandtab cc=101 : */
