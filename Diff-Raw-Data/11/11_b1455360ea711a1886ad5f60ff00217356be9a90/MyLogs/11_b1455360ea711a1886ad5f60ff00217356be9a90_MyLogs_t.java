 package com.zeyomir.ocfun.gui;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.view.View;
 import android.widget.AdapterView;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.view.ContextMenu;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.zeyomir.ocfun.R;
 import com.zeyomir.ocfun.controller.ListMyLogs;
 import com.zeyomir.ocfun.dao.PreferencesDAO;
 import org.holoeverywhere.app.ListActivity;
 import org.holoeverywhere.widget.Toast;
 
 public class MyLogs extends ListActivity {
     private static final int DELETE_ID = 1;
     private ActionBar actionBar;
 	private SimpleCursorAdapter sca;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		actionBar = getSupportActionBar();
 		setActionBarOptions();
 		setContentView(R.layout.list);
         registerForContextMenu(getListView());
 	}
 
 	private void setActionBarOptions() {
 		actionBar.setDisplayShowTitleEnabled(true);
 		actionBar.setTitle(R.string.my_logs);
 		actionBar.setDisplayShowHomeEnabled(true);
 		actionBar.setDisplayHomeAsUpEnabled(true);
 		actionBar.setHomeButtonEnabled(true);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		fillData();
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		sca.getCursor().close();
 	}
 
 	public void fillData() {
 		sca = ListMyLogs.createAdapter(this);
 		setListAdapter(sca);
 	}
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, android.view.ContextMenu.ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         menu.add(0,DELETE_ID,0, R.string.menu_delete_one);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         switch(item.getItemId()) {
             case DELETE_ID:
                 AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                 ListMyLogs.delete(info.id);
                 fillData();
                 return true;
         }
         return super.onContextItemSelected(item);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.my_logs, menu);
         return true;
     }
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		Intent i;
 		switch (item.getItemId()) {
 			case android.R.id.home:
 				i = new Intent(this, OCFun.class);
 				i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				startActivity(i);
 				return true;
             case R.id.menu_sync:
                if (sca.isEmpty()){
                    Toast.makeText(this,"Brak wpisów do zsynchronizowania", Toast.LENGTH_LONG).show();
                    return true;
                }
                 if (new PreferencesDAO(this).isAuthenticated()){
                     new ListMyLogs(this).synchronize();
                     Toast.makeText(this,"Trwa synchronizacja", Toast.LENGTH_LONG).show();
                 } else
                     Toast.makeText(this,R.string.dialog_authenticate, Toast.LENGTH_LONG).show();
                 return true;
             case R.id.menu_delete:
                 ListMyLogs.clean();
                 fillData();
                 return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 }
