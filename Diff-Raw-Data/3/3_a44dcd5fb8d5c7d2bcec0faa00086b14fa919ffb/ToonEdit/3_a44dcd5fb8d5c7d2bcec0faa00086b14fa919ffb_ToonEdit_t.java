 package com.astrofrog.android.raidtimer;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 
 public class ToonEdit extends ListActivity {
     private static final int ACTIVITY_CREATE_RAID = 0;
     private static final int ACTIVITY_EDIT_RAID = 1;
 
     private static final int INSERT_RAID_ID = Menu.FIRST;
     private static final int DELETE_RAID_ID = Menu.FIRST + 1;
 
     private Long mRowId;
     private EditText mToonName;
     private ToonsDbAdapter mToonsDbHelper;
     private RaidsDbAdapter mRaidsDbHelper;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mToonsDbHelper = new ToonsDbAdapter(this);
         mToonsDbHelper.open();
 
         mRaidsDbHelper = new RaidsDbAdapter(this);
         mRaidsDbHelper.open();
 
         setContentView(R.layout.toon_edit);
         setTitle(R.string.edit_toon);
 
         mToonName = (EditText) findViewById(R.id.toon);
 
         Button confirmButton = (Button) findViewById(R.id.confirm_toon);
 
         mRowId = (savedInstanceState == null) ? null :
             (Long) savedInstanceState.getSerializable(RaidsDbAdapter.KEY_ROWID);
 		if (mRowId == null) {
 			Bundle extras = getIntent().getExtras();
 			mRowId = extras != null ? extras.getLong(RaidsDbAdapter.KEY_ROWID)
 									: null;
 		}
 
 		populateFields();
 		fillRaidData();
		registerForContextMenu(getListView());
 
         confirmButton.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 setResult(RESULT_OK);
                 finish();
             }
         });
     }
 
     private void populateFields() {
         if (mRowId != null) {
             Cursor toon = mToonsDbHelper.fetchToon(mRowId);
             startManagingCursor(toon);
             mToonName.setText(toon.getString(
             		toon.getColumnIndexOrThrow(ToonsDbAdapter.KEY_TOON_NAME)));
         }
     }
 
     private void fillRaidData() {
     	Cursor raidsCursor = null;
     	if (mRowId != null) {
             raidsCursor = mRaidsDbHelper.fetchToonsRaids(mRowId);
     	} else {
     		// Apparently other queries don't work for crap, so I just look for id 0 which doesn't exist, g'dur
     		// Would be nice to just have a de facto "empty cursor" since a null one blows up to holy hell
             raidsCursor = mRaidsDbHelper.fetchToonsRaids(0);
     	}
         startManagingCursor(raidsCursor);
 
         String[] from = new String[]{RaidsDbAdapter.KEY_NAME};
 
         int[] to = new int[]{R.id.text1};
 
         SimpleCursorAdapter raids = 
             new SimpleCursorAdapter(this, R.layout.raids_row, raidsCursor, from, to);
         setListAdapter(raids);
     }
     
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         saveState();
         outState.putSerializable(ToonsDbAdapter.KEY_TOON_ROWID, mRowId);
     }
 
     @Override
     protected void onPause() {
         super.onPause();
         saveState();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         populateFields();
     }
 
     private void saveState() {
         String toon = mToonName.getText().toString();
 
         if (mRowId == null) {
             long id = mToonsDbHelper.createToon(toon);
             if (id > 0) {
                 mRowId = id;
             }
         } else {
             mToonsDbHelper.updateToon(mRowId, toon);
         }
     }
     
     /*****************************
      *  Raid Editing Awesomeness *
      *****************************/
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         if (mRowId != null) {
         	menu.add(0, INSERT_RAID_ID, 0, R.string.menu_raid_insert);
         }
         return true;
     }
 
     @Override
     public boolean onMenuItemSelected(int featureId, MenuItem item) {
         switch(item.getItemId()) {
             case INSERT_RAID_ID:
                 createRaid();
                 return true;
         }
 
         return super.onMenuItemSelected(featureId, item);
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,
             ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         menu.add(0, DELETE_RAID_ID, 0, R.string.menu_raid_delete);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         switch(item.getItemId()) {
             case DELETE_RAID_ID:
                 AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                 mRaidsDbHelper.deleteRaid(info.id);
                 fillRaidData();
                 return true;
         }
         return super.onContextItemSelected(item);
     }
 
     private void createRaid() {
         Intent i = new Intent(this, RaidEdit.class);
         i.putExtra(RaidsDbAdapter.KEY_TOON_ID, mRowId);
         startActivityForResult(i, ACTIVITY_CREATE_RAID);
     }
 
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
         super.onListItemClick(l, v, position, id);
         Intent i = new Intent(this, RaidEdit.class);
         i.putExtra(RaidsDbAdapter.KEY_ROWID, id);
         i.putExtra(RaidsDbAdapter.KEY_TOON_ID, mRowId);
         startActivityForResult(i, ACTIVITY_EDIT_RAID);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
         super.onActivityResult(requestCode, resultCode, intent);
         fillRaidData();
     }    
 }
