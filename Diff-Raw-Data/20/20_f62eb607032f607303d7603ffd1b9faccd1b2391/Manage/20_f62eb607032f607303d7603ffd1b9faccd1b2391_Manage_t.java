 package dsq.listless.screens.manage;
 
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.ContentValues;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView;
 import dsq.listless.R;
 import dsq.listless.action.Action;
 import dsq.listless.action.CancelAction;
 import dsq.listless.action.NewScrollAction;
 import dsq.listless.db.app.AppDb;
 import dsq.listless.db.app.ListlessDb;
 import dsq.listless.db.general.DefaultSimpleDbAdapter;
 import dsq.listless.db.general.SimpleDbAdapter;
 import dsq.listless.db.item.ItemTable;
 import dsq.listless.db.scroll.DefaultScrollMapping;
 import dsq.listless.db.scroll.ScrollTable;
 import dsq.listless.screens.general.DefaultTextDialog;
 import dsq.listless.screens.general.DialogListener;
 import dsq.listless.screens.general.TextDialog;
 import dsq.listless.ui.DefaultPlainViewBinder;
 import dsq.listless.ui.PlainViewBinder;
 import dsq.listless.xml.SidebarIconLayout;
 import dsq.thedroid.db.DbLifecycle;
 import dsq.thedroid.db.DbUtils;
 import dsq.thedroid.db.DefaultDbLifecycle;
 import dsq.thedroid.ui.*;
 import dsq.listless.screens.general.Dialogs;
 
 public class Manage extends ListActivity {
 
     private final DbLifecycle lifecycle = new DefaultDbLifecycle();
     private SQLiteDatabase db;
     private SimpleDbAdapter sAdapter;
     private final Lists lists = new DefaultLists();
     private final ListMapping mapping = new DefaultScrollMapping();
     private final Menus menus = new DefaultMenus();
 
     private static final String CONTEXT_DIALOG = "CONTEXT_DIALOG";
     
     private TextDialog textDialog = new DefaultTextDialog(this);
     
     
     @Override
     protected void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         setContentView(R.layout.manage_list);
 
         db = lifecycle.open(this, AppDb.value);
         sAdapter = new DefaultSimpleDbAdapter(db, ListlessDb.SCROLL_TABLE);
         registerForContextMenu(getListView());
         
         addButtonAction(R.id.back, new CancelAction());
         addButtonAction(R.id.new_scroll, new NewScrollAction());
         refreshList();
     }
 
     // FIX 5/08/12 Dupe with Main
     private void addButtonAction(final int buttonId, final Action action) {
         final SidebarIconLayout layout = (SidebarIconLayout)findViewById(buttonId);
         layout.setAction(this, action);
     }
 
     private void refreshList() {
         final PlainViewBinder binder = new DefaultPlainViewBinder();
         lists.refreshAll(this, sAdapter, getListView(), new ComponentIndex(R.layout.manage_list_row), mapping, binder);
     }
     
     private void openScroll(long id) {
         final Intent resultIntent = new Intent();
         resultIntent.putExtra(ItemTable.SCROLL_ID, id);
         setResult(RESULT_OK, resultIntent);
         finish();
     }
 
     @Override
     protected Dialog onCreateDialog(final int id) {
         switch (id) {
             case Dialogs.ADD_SCROLL_TITLE: {
                 return textDialog.dialog("Title for Scroll: ", "", new DialogListener() {
                     @Override
                     public void onSuccess(final String value) {
                         final ContentValues values = new ContentValues();
                         values.put(ScrollTable.TITLE, value);
                         final long newId = sAdapter.create(values);
                         openScroll(newId);
                     }
                 });
             }
             case Dialogs.EDIT_SCROLL_TITLE: {
                 final ContentValues values = new ContentValues();
                 final Intent intent = getIntent();
                 final long scrollId = intent.getLongExtra(CONTEXT_DIALOG, -1);
                 intent.removeExtra(CONTEXT_DIALOG);
                 final Cursor cursor = sAdapter.fetchById(scrollId);
                if (cursor.moveToFirst()) {
                    final String title = DbUtils.getColumn(cursor, ScrollTable.TITLE);
                    return textDialog.dialog("Title for Scroll: ", title, new DialogListener() {
                        @Override
                        public void onSuccess(final String value) {
                            values.put(ScrollTable.TITLE, value);
                            sAdapter.updateById(scrollId, values);
                            refreshList();
                        }
                    });
                }
             }
         }
         return null;
     }
 
     @Override
     public void onCreateContextMenu(final ContextMenu menu, final View v, final ContextMenu.ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         menus.context(this, menu, R.menu.manage_context);
     }
 
     @Override
     public boolean onContextItemSelected(final MenuItem item) {
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
         switch (item.getItemId()) {
             case R.id.edit_scroll: {
                 removeDialog(Dialogs.EDIT_SCROLL_TITLE);
                 final Intent intent = getIntent();
                 intent.putExtra(CONTEXT_DIALOG, info.id);
                 showDialog(Dialogs.EDIT_SCROLL_TITLE);
                 return true;
             }
             case R.id.delete_scroll: {
                 sAdapter.deleteById(info.id);
                 refreshList();
                 return true;
             }
             case R.id.open_scroll: {
                 openScroll(info.id);
                 return true;
             }
         }
         return super.onContextItemSelected(item);
     }
 }
