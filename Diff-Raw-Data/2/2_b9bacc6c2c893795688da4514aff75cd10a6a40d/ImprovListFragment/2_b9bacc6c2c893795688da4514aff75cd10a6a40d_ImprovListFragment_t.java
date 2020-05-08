 package org.bullecarree.improv.referee;
 
 import org.bullecarree.improv.db.ImprovDbTable;
 import org.bullecarree.improv.model.ImprovRenderer;
 import org.bullecarree.improv.model.ImprovType;
 import org.bullecarree.improv.referee.contentprovider.ImprovContentProvider;
 
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CursorAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class ImprovListFragment extends ListFragment implements
         LoaderCallbacks<Cursor> {
 
     // private Cursor cursor;
     private SimpleCursorAdapter adapter;
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
        setEmptyText(getString(R.string.improv_list_no_improv));
 
         // Fields from the database (projection)
         String[] from = new String[] { ImprovDbTable.COL_TITLE };
         // Fields on the UI to which we map
         int[] to = new int[] { R.id.improvListItem_title };
 
         Context context = this.getActivity();
 
         adapter = new SimpleCursorAdapter(context, R.layout.improv_list_item,
                 null, from, to, 0) {
 
             
             public View newView(Context context, Cursor cursor, ViewGroup parent) {
                 View v = super.newView(context, cursor, parent);
                 TextView title_text = (TextView) v.findViewById(R.id.improvListItem_details);
                 if (title_text != null) {
                     title_text.setText(getImprovDetails(cursor));
                 }
                 return v;
             };
 
             public void bindView(View v, Context context, Cursor cursor) {
                 super.bindView(v, context, cursor);
                 TextView title_text = (TextView) v.findViewById(R.id.improvListItem_details);
                 if (title_text != null) {
                     title_text.setText(getImprovDetails(cursor));
                 }
             };
             
         };
 
         this.getLoaderManager().initLoader(0, null, this);
 
         setListAdapter(adapter);
     }
 
     public String getImprovDetails(Cursor cursor) {
      // super.convertToString(cursor);
         StringBuffer res = new StringBuffer();
         /*
         String title = cursor.getString(cursor
                 .getColumnIndexOrThrow(ImprovDbTable.COL_TITLE));
         */
         
         String type = cursor.getString(cursor
                 .getColumnIndexOrThrow(ImprovDbTable.COL_TYPE));
         // cursor.getString(cursor.getColumnIndexOrThrow(ImprovDbTable.COL_TYPE));
         if (ImprovType.MIXT.toString().equalsIgnoreCase(type)) {
             res.append("(M) ");
         } else {
             res.append("(C) ");
         }
 
         // res.append(title);
 
         Integer duration = cursor.getInt(cursor
                 .getColumnIndexOrThrow(ImprovDbTable.COL_DURATION));
         res.append(ImprovRenderer.displayTime(duration));
 
         String category = cursor.getString(cursor
                 .getColumnIndex(ImprovDbTable.COL_CATEGORY));
         if (category != null && !"".equalsIgnoreCase(category)) {
             res.append(" (");
             res.append(category);
             res.append(")");
         }
         return res.toString();
     }
     
     // Creates a new loader after the initLoader () call
     @Override
     public Loader<Cursor> onCreateLoader(int id, Bundle args) {
         String[] projection = { ImprovDbTable.COL_ID, ImprovDbTable.COL_TITLE, ImprovDbTable.COL_TYPE, ImprovDbTable.COL_DURATION, ImprovDbTable.COL_CATEGORY };
         CursorLoader cursorLoader = new CursorLoader(this.getActivity(),
                 ImprovContentProvider.CONTENT_URI, projection, null, null, null);
         return cursorLoader;
     }
 
     @Override
     public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
         adapter.swapCursor(data);
     }
 
     @Override
     public void onLoaderReset(Loader<Cursor> loader) {
         // data is not available anymore, delete reference
         adapter.swapCursor(null);
     }
 
     @Override
     public void onListItemClick(ListView l, View v, int position, long id) {
         super.onListItemClick(l, v, position, id);
         Intent i = new Intent(getActivity(), ImprovDetailActivity.class);
         Uri todoUri = Uri.parse(ImprovContentProvider.CONTENT_URI + "/" + id);
         i.putExtra(ImprovContentProvider.CONTENT_ITEM_TYPE, todoUri);
 
         // Activity returns an result if called with startActivityForResult
         startActivityForResult(i, ImprovListActivity.ACTIVITY_EDIT);
     }
 
 }
