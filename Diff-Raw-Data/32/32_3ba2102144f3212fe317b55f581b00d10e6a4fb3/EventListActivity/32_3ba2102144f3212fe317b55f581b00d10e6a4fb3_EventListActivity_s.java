 package no.uka.findmyapp.ukaprogram.activities;
 
 import no.uka.findmyapp.android.rest.contracts.UkaEvents.UkaEventContract;
 import no.uka.findmyapp.android.rest.datamodels.models.UkaEvent;
 import no.uka.findmyapp.ukaprogram.R;
 import no.uka.findmyapp.ukaprogram.adapters.EventListCursorAdapter;
 import no.uka.findmyapp.ukaprogram.utils.DateUtils;
 import no.uka.findmyapp.ukaprogram.wrapper.EventDatabase;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
 
 public class EventListActivity extends ListActivity implements OnClickListener{	
 	private final static String debug = "EventListActivity";
 	private final static String KONSERT = "'Konsert'";
 	private final static String REVY = "'Revy og Teater'";
 	private final static String KURS = "'Kurs & Events'";
 	private final static String FEST = "'Fest & Moro'";
 	private Cursor eventCursor;
 
 	private final static String ORDER_BY = UkaEventContract.SHOWING_TIME + " asc";
 	public final static String ITEM_CLICKED = "clicked";
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.event_list);
 		Bundle bundle = getIntent().getExtras();
 		Button categoryButton = (Button) findViewById(R.id.event_list_category_button);
 		Button calendarButton = (Button) findViewById(R.id.event_list_calendar_button);
 		LinearLayout line = (LinearLayout) findViewById(R.id.event_list_line);
 		Drawable drawable = getResources().getDrawable(R.drawable.katbuttonalle);
 		String whereStatement = getResources().getString(R.string.alle);
 
 		categoryButton.setOnClickListener(this);
 		calendarButton.setOnClickListener(this);
 
 		refresh(drawable, R.color.alle, null, whereStatement);
 
 		if (bundle != null) {
 			moveToDate(this.eventCursor, bundle.getInt("SelectedDate"));
 		}
 
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 
 		Cursor c = (Cursor) l.getItemAtPosition(position);
 
 		UkaEvent event = EventDatabase.getInstance().getEventFromCursor(c);
 		Log.v(debug, event.toString());
 
 		Intent intent = new Intent(this, EventDetailsActivity.class); 
 		intent.putExtra(ITEM_CLICKED, event);
 
 		startActivity(intent);
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.event_list_category_button:
 			registerForContextMenu(v);
 			v.setLongClickable(false);
 			openContextMenu(v);
 			break;
 
 		case R.id.event_list_calendar_button:
 			Intent intent = new Intent().setClass(this, CalendarActivity.class);
 			startActivity(intent);
 			break;
 		default:
 			break;
 		}
 
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.category_menu, menu);
 
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item){
 		return setUpdateSettings(item);
 	}
 
 	private boolean setUpdateSettings(MenuItem item){
 		Drawable drawable;
 		int color;
 		String whereStatement;
 		String categoryName;
 		switch (item.getItemId()){
 		case R.id.category_menu_alle:
 			drawable = getResources().getDrawable(R.drawable.katbuttonalle);
 			color = R.color.alle;
 			categoryName = getResources().getString(R.string.alle);
 			whereStatement = null;
 			refresh(drawable, color, whereStatement, categoryName);
 			return true;
 
 		case R.id.category_menu_fest:
 			whereStatement = UkaEventContract.EVENT_TYPE + " = " + FEST;
 			drawable = getResources().getDrawable(R.drawable.katbuttonfest);
 			color = R.color.fest;
 			categoryName = getResources().getString(R.string.fest);
 			refresh(drawable, color, whereStatement, categoryName);
 			return true;
 
 		case R.id.category_menu_konsert:
 			whereStatement = UkaEventContract.EVENT_TYPE + " = " + KONSERT;
 			drawable = getResources().getDrawable(R.drawable.katbuttonkonsert);
 			color = R.color.konsert;
 			categoryName = getResources().getString(R.string.konsert);
 			refresh(drawable, color, whereStatement, categoryName);
 			return true;
 
 		case R.id.category_menu_kurs:
 			whereStatement = UkaEventContract.EVENT_TYPE + " = " + KURS;
 			drawable = getResources().getDrawable(R.drawable.katbuttonkurs);
 			color = R.color.kurs;
 			categoryName = getResources().getString(R.string.kurs);
 			refresh(drawable, color, whereStatement, categoryName);
 			return true;
 
 		case R.id.category_menu_revy:
 			whereStatement =  UkaEventContract.EVENT_TYPE  + " = " + REVY ;
 			drawable = getResources().getDrawable(R.drawable.katbuttonrevy);
 			color = R.color.revy;
 			categoryName = getResources().getString(R.string.revy);
 			refresh(drawable, color, whereStatement, categoryName);
 			return true;
 
 		default:
 			return false;
 		}
 	}
 
 	private void refresh(Drawable drawable, int color, String whereStatement, String categoryName){
 		Log.v(debug, "Refreshing page");
 		Button categoryButton = (Button) findViewById(R.id.event_list_category_button);
 		LinearLayout line = (LinearLayout) findViewById(R.id.event_list_line);
 		line.setBackgroundColor(getResources().getColor(color));
 		categoryButton.setBackgroundDrawable(drawable);
 		categoryButton.setText(categoryName);
 		eventCursor = this.managedQuery(UkaEventContract.EVENT_CONTENT_URI, null, whereStatement, null, ORDER_BY);
 		this.setListAdapter(new EventListCursorAdapter(this, eventCursor));
 	}
 
 
 	private void moveToDate(Cursor c, int date)  {
 		DateUtils du = new DateUtils();
 		c.moveToFirst();
 
 		boolean dateNotFound = true; 
 		while (dateNotFound && c.moveToNext()) {
 			int i = du.getDayIntFromTimestamp(c.getLong(c.getColumnIndex(UkaEventContract.SHOWING_TIME))); 
 			if(i == date) {
 				dateNotFound = false; 
 			}
 			c.moveToNext();
 		}
 		if (dateNotFound){
 
 		}
 		else{
 			this.setSelection(c.getPosition());
 			Intent intent = new Intent();
 			intent.setClass(getApplicationContext(), EventListActivity.class);
 			startActivity(intent);
 		}
 	}
 
 }
