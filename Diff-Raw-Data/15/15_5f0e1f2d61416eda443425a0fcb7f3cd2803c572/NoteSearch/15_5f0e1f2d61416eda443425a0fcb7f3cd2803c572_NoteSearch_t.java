 /* *************************************************************************************************
  *                                         eNotes                                                  *
  * *************************************************************************************************
  * File:        NoteSearch.java                                                                    *
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
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import android.app.ListActivity;
 
 /**
  * Activity to search a note by tag
  * @author Emanuele Alimonda
  * @author Giovanni Serra
  */
 public class NoteSearch extends ListActivity {
    /** Logging tag */
    private static final String kTag = "NoteSearch";
    /** Database helper / content provider */
    private NoteDB _database;
    /** Fields to query */
    private static final String fields[] = { Note.kTitle, Note.kTimestamp, Note.kTags, Note.kID };
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       Log.v(kTag, "created activity");
       setContentView(R.layout.search);
 
      this._database = null;
 
       ListView view = getListView();
       view.setHeaderDividersEnabled(true);
 
       EditText tagBox = (EditText) findViewById(R.id.SearchBox);
       tagBox.requestFocus();
       tagBox.addTextChangedListener(new TextWatcher() {
          public void afterTextChanged(Editable s) {
             refreshList();
          }
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {
          }
          public void onTextChanged(CharSequence s, int start, int count, int after) {
          }
       });
       refreshList();
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
   }

    /** Refresh the list, re-querying the database as needed */
    protected void refreshList() {
       EditText tagBox = (EditText) findViewById(R.id.SearchBox);
       String tag = tagBox.getText().toString();
      
      if (this._database == null) {
         this._database = new NoteDB();
      }
       Cursor data = this._database.getAllNotesHeadersByTag(this, tag);
 
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
 }
 /* vim: set ts=3 sw=3 smarttab expandtab cc=101 : */
