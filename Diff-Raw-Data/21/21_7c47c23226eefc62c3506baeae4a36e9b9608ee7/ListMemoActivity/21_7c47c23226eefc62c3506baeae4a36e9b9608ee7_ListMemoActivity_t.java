 package com.azazeleleven.android.memo;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 
 import com.azazeleleven.android.memo.DaoMaster.DevOpenHelper;
 
 public class ListMemoActivity extends ListActivity {
 
 	private static final int DELETE_ID = Menu.FIRST;
 
 	private DaoMaster daoMaster;
 	private DaoSession daoSession;
	private NoteDao memoDao;
 
 	private Cursor cursor;
 
 	private SQLiteDatabase db;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_list_memo);
 
 		DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "memo-db",
 				null);
 		db = helper.getReadableDatabase();
 		daoMaster = new DaoMaster(db);
 		daoSession = daoMaster.newSession();
		memoDao = daoSession.getNoteDao();
 
		String textColumn = NoteDao.Properties.Text.columnName;
 		cursor = db.query(memoDao.getTablename(), memoDao.getAllColumns(),
 				null, null, null, null, null);
 		String[] from = { textColumn };
 		int[] to = { android.R.id.text1 };
 
 		@SuppressWarnings("deprecation")
 		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
 				android.R.layout.simple_list_item_2, cursor, from, to);
 		setListAdapter(adapter);
 
 		registerForContextMenu(getListView());
 	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	protected void onResume() {
 		cursor.requery();
 		super.onResume();
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		menu.add(0, DELETE_ID, 0, R.string.menu_delete);
 	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case DELETE_ID:
 			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
 					.getMenuInfo();
 			memoDao.deleteByKey(info.id);
 			cursor.requery();
 		}
 		return super.onContextItemSelected(item);
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		cursor.moveToPosition(position);
 		Intent intent = new Intent(this, EditMemoActivity.class);
 		intent.putExtra("memoText", cursor.getString(cursor
 				.getColumnIndexOrThrow("TEXT")));
 		intent.putExtra("mRowId", id);
 		startActivity(intent);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		return super.onOptionsItemSelected(item);
 	}
 
 	public void addMemo(MenuItem item) {
 		Intent intent = new Intent(this, EditMemoActivity.class);
 		startActivity(intent);
 	}
 
 }
