 /* *************************************************************************************************
  *                                         eNotes                                                  *
  * *************************************************************************************************
  * File:        NoteList.java                                                                      *
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
 import java.io.FileReader;
 import java.io.IOException;
 import java.nio.CharBuffer;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Activity to list all existing notes
  * @author Emanuele Alimonda
  * @author Giovanni Serra
  */
 public class NoteList extends ListActivity {
    /** Menu IDs */
    private static final int kMenuItemAdd = 100;
    private static final int kMenuItemSearch = 101;
    /** Logging tag */
    private static final String kTag = "NoteList";
    /** Database helper / content provider */
    private NoteDB _database;
    /** Fields to query */
    private static final String fields[] = { Note.kTitle, Note.kTimestamp, Note.kTags, Note.kID };
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       Log.v(kTag, "created activity");
       setContentView(R.layout.main);
 
       this._database = null;
 
       ListView view = getListView();
       view.setHeaderDividersEnabled(true);
 
       refreshList();
 
       if (savedInstanceState == null) {
          // Run this only if we're not resuming
          Intent intent = getIntent();
          newIntent(intent);
       }
    }
 
    @Override
    public void onResume() {
       super.onResume();
       refreshList();
    }
    
    @Override
    public void onStart() {
       super.onStart();
       refreshList();
 
       // Do some temporary files cleanup (Why here?  See note in NoteView.)
       try {
          File tmpDir = Note.getSharedTmpDir();
          File[] tmpFileList = tmpDir.listFiles();
 
          Time thresholdTimestamp = new Time();
          thresholdTimestamp.setToNow();
          thresholdTimestamp.set(thresholdTimestamp.toMillis(true)-1000*3600*24); // 24 hours
 
          for (int i = 0; i < tmpFileList.length; i++) {
             if (!tmpFileList[i].exists() || !tmpFileList[i].isFile()) {
                continue;
             }
             if (tmpFileList[i].lastModified() < thresholdTimestamp.toMillis(true)) {
                tmpFileList[i].delete();
                Log.v(kTag, "Deleted temp file " + tmpFileList.toString());
             }
          }
          Log.v(kTag, "temp file check done");
       } catch (FileNotFoundException e) {
          e.printStackTrace();
       }
    }
 
    @Override
    public void onNewIntent(Intent intent) {
       super.onNewIntent(intent);
       newIntent(intent);
    }
 
    /**
     * Import a new note from a file loaded through intent
     * @param intent  The loading intent
     */
    protected void newIntent(Intent intent) {
       setIntent(intent);
       Log.v(kTag, "Found intent: "+ intent.toString());
       Uri importUri = intent.getData();
       if (importUri != null) {
          File importFile = new File(importUri.getPath());
          if (
                // File doesn't exist
                !importFile.isFile()
                // File is zero bytes
                || importFile.length() <= 0
                // File is larger than max attachment size
                || importFile.length() > NoteAttachment.kMaxAttachmentSize*15/10
          ) {
            Toast.makeText(this.getApplicationContext(), R.string.invalidImportFile,
                  Toast.LENGTH_LONG).show();
             return;
          }
          try {
             FileReader importReader = new FileReader(importFile);
             CharBuffer importBuffer = CharBuffer.allocate((int)importFile.length());
             importReader.read(importBuffer);
             importReader.close();
             Log.v(kTag, "Importing: " + String.valueOf(importBuffer.array()));
             long newID = this._database.addNote(this, null, getString(R.string.importedNote),
                   String.valueOf(importBuffer.array()));
             refreshList();
             if (newID >= 0) {
                Intent i = new Intent(this, NoteEdit.class);
                i.putExtra(Note.kID, newID);
                startActivityForResult(i, 0);
             }
          } catch (FileNotFoundException e) {
             e.printStackTrace();
             return;
          } catch (IOException e) {
             e.printStackTrace();
             return;
          }
       }
    }
 
    /** Refresh the list, re-querying the database as needed */
    protected void refreshList() {
       if (this._database == null) {
          this._database = new NoteDB();
       }
       Cursor data = this._database.getAllNotesHeaders(this);
 
       SimpleCursorAdapter dataSource = new SimpleCursorAdapter(this, R.layout.row, data, fields,
             new int[] { R.id.RowTitle, R.id.RowTimestamp, R.id.RowTags, -1 });
 
       dataSource.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
 
          @Override
          public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex) {
             if (aView.getId() == R.id.RowTimestamp) {
                TextView textView = (TextView) aView;
                Time timestamp = new Time();
                timestamp.set(aCursor.getLong(aColumnIndex));
                textView.setText(timestamp.format("%c"));
                return true;
             }
             return false;
          }
       });
 
       setListAdapter(dataSource);
    }
 
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
       Intent i = new Intent(this, NoteView.class);
       i.putExtra(Note.kID, id);
       startActivity(i);
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       menu.add(0, kMenuItemAdd, 1, R.string.addItem).setIcon(getResources().getDrawable(R.drawable.ic_new_note));
       menu.add(0, kMenuItemSearch, 2, R.string.searchItem).setIcon(getResources().getDrawable(R.drawable.ic_search));
       return true;
    }
 
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
       switch (item.getItemId()) {
       case kMenuItemAdd:
       {
          long newID = this._database.addNote(this, null, null, null);
          refreshList();
          if (newID < 0) {
             return false;
          }
          Intent i = new Intent(this, NoteEdit.class);
          i.putExtra(Note.kID, newID);
          startActivity(i);
       }
          break;
       case kMenuItemSearch:
       {
          Intent i = new Intent(this, NoteSearch.class);
          startActivity(i);
       }
          break;
       default:
          return false;
       }
       return true;
    }
 
    @Override
    protected Dialog onCreateDialog(int id) {
       return null;
    }
 
 }
 /* vim: set ts=3 sw=3 smarttab expandtab cc=101 : */
