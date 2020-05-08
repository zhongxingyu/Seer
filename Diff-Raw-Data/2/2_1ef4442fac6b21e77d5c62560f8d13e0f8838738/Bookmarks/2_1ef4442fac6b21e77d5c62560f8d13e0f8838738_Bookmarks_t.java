 package sk.ksp.riso.svpismo;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.os.Bundle;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.*;
 
 import android.util.Log;
 
 import java.lang.Float;
 import java.net.URLDecoder;
 
 import sk.ksp.riso.svpismo.Db;
 
 public class Bookmarks extends Activity
 {
     static final int BOOKMARKS = 1;
 
     static final int DIALOG_LABEL = 1;
     static final int DIALOG_EDIT = 2;
 
     SimpleCursorAdapter A;
     Db dbHelper;
     SQLiteDatabase db;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.bookmarks);
       dbHelper = new Db(this);
       db = dbHelper.getWritableDatabase();
       final Bookmarks current_activity = this;
       ((Button)findViewById(R.id.new_bookmark)).setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {
           current_activity.showDialog(DIALOG_LABEL);
 	}
       });
     }
 
     String getSuggestedLabel() {
       return URLDecoder.decode(getIntent().getStringExtra("location")
        .replaceFirst("^.*?(c|in)=", "")
         .replaceFirst("&.*$", ""));
     }
 
     protected Dialog onCreateDialog(int id) {
       final Dialog dialog;
       final EditText e;
       final Bookmarks current_activity = this;
       LayoutParams params;
 
       switch(id) {
       case DIALOG_LABEL:
         dialog = new Dialog(this);
         dialog.setContentView(R.layout.add_bookmark_dialog);
         dialog.setTitle(R.string.new_bookmark);
 
         params = dialog.getWindow().getAttributes(); 
         params.width = LayoutParams.FILL_PARENT; 
         dialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params); 
 
         e = (EditText) dialog.findViewById(R.id.bookmark_label);
         e.setText(getSuggestedLabel());
 
         ((Button)dialog.findViewById(R.id.cancel_bookmark)).setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
             dialog.dismiss();
           }
         });
 
         ((Button)dialog.findViewById(R.id.add_bookmark)).setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
             current_activity.addBookmark(e.getText().toString());
           }
         });
         break;
       case DIALOG_EDIT:
         dialog = new Dialog(this);
         dialog.setContentView(R.layout.edit_bookmark_dialog);
         dialog.setTitle(R.string.edit_bookmark);
 
         params = dialog.getWindow().getAttributes(); 
         params.width = LayoutParams.FILL_PARENT; 
         dialog.getWindow().setAttributes((android.view.WindowManager.LayoutParams) params); 
 
         e = (EditText) dialog.findViewById(R.id.bookmark_label);
         e.setText(label_);
 
         ((Button)dialog.findViewById(R.id.cancel_bookmark)).setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
             dialog.dismiss();
           }
         });
 
         ((Button)dialog.findViewById(R.id.delete_bookmark)).setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
             dialog.dismiss();
             current_activity.bookmarkDelete();
           }
         });
 
         ((Button)dialog.findViewById(R.id.modify_bookmark)).setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
             dialog.dismiss();
             current_activity.label_ = e.getText().toString();
             current_activity.bookmarkModify();
           }
         });
 
         break;
       default:
         dialog = null;
       }
       return dialog;
     }
 
     public void addBookmark(String label) {
       Intent i = getIntent();
       ContentValues r = new ContentValues();
       r.put("location", i.getStringExtra("location"));
       r.put("position", i.getFloatExtra("position", 0));
       r.put("stamp", System.currentTimeMillis());
       r.put("label", label);
       db.insert(Db.BOOKMARKS_TABLE, null, r);
       setResult(RESULT_CANCELED, null);
       finish();
     }
 
     public void bookmarkClicked(String location, float position) {
       setResult(RESULT_OK, new Intent()
           .putExtra("location", location)
           .putExtra("position", position)
       );
       finish();
     }
 
     public String rowid_, label_, location_;
     float position_;
 
     public void bookmarkEdit(String rowid, String label, String location, float position) {
       rowid_ = rowid;
       label_ = label;
       location_ = location;
       position_ = position;
       showDialog(DIALOG_EDIT);
     }
 
     public void bookmarkDelete() {
       db.delete(Db.BOOKMARKS_TABLE, "rowid="+rowid_, null);
       A = null;
       Refresh();
     }
 
     public void bookmarkModify() {
       ContentValues c = new ContentValues();
       c.put("label", label_);
       c.put("stamp", System.currentTimeMillis());
       db.update(Db.BOOKMARKS_TABLE, c, "rowid="+rowid_, null);
       A = null;
       Refresh();
     }
 
     void Refresh() {
       Cursor c = db.rawQuery("select label as _id, location, position, rowid " +
                              "  from " + Db.BOOKMARKS_TABLE + " order by stamp desc", null);
 
       if (A==null) {
         A = new SimpleCursorAdapter(this, R.layout.bookmarks_list, c, 
             new String[] { "_id" }, new int[] { R.id.listLabel } );
         A.setViewBinder ( new SimpleCursorAdapter.ViewBinder() {
           public boolean setViewValue (View view, Cursor cursor, int columnIndex) {
             if (columnIndex == 0) {
               ((TextView)view).setText(cursor.getString(columnIndex));
             }
             return true;
           }
         });
         ((ListView)findViewById(R.id.listBookmarks)).setAdapter(A);
 
         final Bookmarks current_activity = this;
         final SimpleCursorAdapter adapter = A;
         ((ListView)findViewById(R.id.listBookmarks)).setOnItemClickListener(
           new AdapterView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               Cursor c = adapter.getCursor();
               c.moveToPosition(position);
               current_activity.bookmarkClicked(c.getString(1), Float.parseFloat(c.getString(2)));
             }
           }
         );
 
         ((ListView)findViewById(R.id.listBookmarks)).setOnItemLongClickListener(
           new AdapterView.OnItemLongClickListener() {
             public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
               Cursor c = adapter.getCursor();
               c.moveToPosition(position);
               current_activity.bookmarkEdit(c.getString(3), c.getString(0), c.getString(1),
                                             Float.parseFloat(c.getString(2)));
               return true;
             }
           }
         );
       } else {
         A.changeCursor(c);
       }
     }
 
     @Override
     public void onResume() {
       Refresh(); 
       super.onResume();
     }
 
     @Override
     public void onDestroy() {
       db.close();
       super.onDestroy();
     }
 }
