 package com.github.pozo.volan;
 
 import com.github.pozo.volan.adapters.ListofFavoritesAdapter;
 
 import com.github.pozo.volan.db.DbConnector;
 import com.github.pozo.volan.shared.Dialogs;
 import com.github.pozo.volan.tasks.db.BusLineListOfFavoritesTask;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.graphics.drawable.GradientDrawable;
 import android.graphics.drawable.GradientDrawable.Orientation;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ImageButton;
 
 public class ListofFavoritesActivity extends ListActivity {
 	
 	private ListofFavoritesAdapter adapter;
	private boolean enable;
 	private ImageButton editFavorites;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		
 		setContentView(R.layout.favorites);
 
 		adapter = new ListofFavoritesAdapter(this);
 		adapter.setAdapterState(ListofFavoritesAdapter.FAVORITES_LIST_BASIC);
 		setListAdapter(adapter);
 		
 		int[] colors = {0, 0x50505000, 0};
 		getListView().setDivider(new GradientDrawable(Orientation.RIGHT_LEFT, colors));
 		getListView().setDividerHeight(1);
 		
 		editFavorites = (ImageButton) findViewById(R.id.edit_favorites_button);
 		editFavorites.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
				if(!enable && adapter.hasElement()) {
 					setFavoriteButtonToAccept();
 				} else {
 					setFavoriteButtonToEdit();
 				}
 				adapter.notifyDataSetChanged();
 			}
 
 		});
 		new BusLineListOfFavoritesTask(this).execute();
 	}
 	public void setFavoriteButtonToEdit() {
 		editFavorites.setImageResource(R.drawable.edit);
 		adapter.setAdapterState(ListofFavoritesAdapter.FAVORITES_LIST_BASIC);
		
		enable = false;
 	}
 
 	private void setFavoriteButtonToAccept() {
 		editFavorites.setImageResource(R.drawable.accept);
 		adapter.setAdapterState(ListofFavoritesAdapter.FAVORITES_LIST_EDIT);
		enable = true;
 	}
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.favorites_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.settings:
 			showDialog(Dialogs.MENU_SETTIGNS);
 			return true;
 		case R.id.about:
 			showDialog(Dialogs.MENU_ABOUT);
 			return true;
 		case R.id.lines:
 			Intent myIntent = new Intent();
 			myIntent.setClassName(this.getPackageName(), this.getPackageName()
 					+ ".ListofLinesActivity");
 			myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(myIntent);
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case Dialogs.MENU_SETTIGNS:
 			AlertDialog alert = Dialogs.getSettingsDialog(this);
 			return alert;
 		case Dialogs.MENU_ABOUT:
 			Dialog dialog = Dialogs.getAboutDialog((ViewGroup) findViewById(R.id.layout_root),this);
 			return dialog;
 		default:
 			return null;
 		}
 	}
 	@Override
 	protected void onResume() {
 		super.onResume();
		//editFavorites.setBackgroundResource(R.drawable.edit);	
		//editFavorites.setImageResource(R.drawable.edit);
 		adapter.setAdapterState(ListofFavoritesAdapter.FAVORITES_LIST_BASIC);
 		new BusLineListOfFavoritesTask(this).execute();
 	}
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		DbConnector.close();
 	}
 	public ListofFavoritesAdapter getAdapter() {
 		return this.adapter;
 	}
 }
