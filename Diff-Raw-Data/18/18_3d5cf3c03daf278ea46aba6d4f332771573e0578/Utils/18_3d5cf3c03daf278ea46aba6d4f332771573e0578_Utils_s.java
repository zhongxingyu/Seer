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
        SettingsHelper db = new SettingsHelper(la);
        try {
        	SimpleCursorAdapter oldAdapter = ((SimpleCursorAdapter) la.getListAdapter());
        	if (oldAdapter != null) {
        		la.stopManagingCursor(oldAdapter.getCursor());
        		oldAdapter.getCursor().close();
        	}
 	        Cursor listContentsCursor = db.getBookmarks();
 	        la.startManagingCursor(listContentsCursor);
 	        la.setListAdapter(new SimpleCursorAdapter(la, R.layout.stopbookmarks_item, listContentsCursor, columnNames, listViewIds));
        } finally {
        	db.close();
        }
 	}
 }
