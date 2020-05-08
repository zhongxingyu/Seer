 package org.redbus.ui;
 
 import org.redbus.R;
 import org.redbus.settings.SettingsHelper;
 
 import android.app.ListActivity;
 import android.database.Cursor;
 import android.widget.SimpleCursorAdapter;
 
 public class Utils 
 {
 	private static final String[] columnNames = new String[] { SettingsHelper.ID, SettingsHelper.BOOKMARKS_COL_STOPNAME };
 	private static final int[] listViewIds = new int[] { R.id.stopbookmarks_stopcode, R.id.stopbookmarks_name };
 
 	public static void updateBookmarksListAdaptor(ListActivity la)
 	{
    	SimpleCursorAdapter cursorAdapter = (SimpleCursorAdapter) la.getListAdapter();
    	if (cursorAdapter == null) {
            SettingsHelper db = new SettingsHelper(la);
 	        Cursor listContentsCursor = db.getBookmarks();
 	        la.startManagingCursor(listContentsCursor);
 	        la.setListAdapter(new SimpleCursorAdapter(la, R.layout.stopbookmarks_item, listContentsCursor, columnNames, listViewIds));
    	} else {
    		Cursor oldCursor = cursorAdapter.getCursor();
    		oldCursor.requery();
    	}
 	}
 }
