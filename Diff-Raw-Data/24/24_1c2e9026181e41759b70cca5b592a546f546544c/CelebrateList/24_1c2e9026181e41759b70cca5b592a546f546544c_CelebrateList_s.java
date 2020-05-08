 package net.anzix.android.celebrate;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.anzix.android.celebrate.api.Celebrate;
 import net.anzix.android.celebrate.api.CelebrateFinder;
 import net.anzix.android.celebrate.api.Event;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 
 public class CelebrateList extends ListActivity {
 	private static final int EVENT_LIST = 0;
 	private static final int ACTIVITY_EDIT = 0;
 	private static final int TEST_NOTIFY = 1;
 	List<Event> list = new ArrayList<Event>();
 	private List<Celebrate> feasts = new ArrayList<Celebrate>();
 	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 	private EventAdapter db;
 
 	public CelebrateList() {
 		super();
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {		
 		super.onCreate(savedInstanceState);
		//SchedulingUtil.schendule(this);
 		setContentView(R.layout.celebrate_list);
 		db = new EventAdapter(this);
 		db.open();
 
 		fillData();
 	}
 
 	private void fillData() {
 		Cursor c = db.fetchAllEvent();
 		list.clear();
 		if (c.moveToFirst()) {
 			while (!c.isAfterLast()) {
 				String name = c.getString(c.getColumnIndex(EventHelper.KEY_NAME));
 				Date date = null;
 				String d = c.getString(c.getColumnIndex(EventHelper.KEY_DATE));
 				long id = c.getLong(c.getColumnIndex(EventHelper.KEY_ROWID));
 				try {
 					date = sdf.parse(d);
 				} catch (ParseException e) {
 					Log.e("", "Error on parsing date" + d);
 					e.printStackTrace();
 				}
 				list.add(new Event(id, name, date));
 				c.moveToNext();
 			}
 			c.deactivate();
 		}
 
 		feasts.clear();
 		feasts = CelebrateFinder.findFeasts(list);
 		List<Map<String, String>> data = new ArrayList<Map<String, String>>();
 		for (Celebrate cx : feasts) {
 			Map<String, String> values = new HashMap<String, String>();
 			values.put("reason", cx.getReason() + " : " + cx.getEvent().getName());
 			values.put("date", sdf.format(cx.getDate()));
 			data.add(values);
 		}
 		setListAdapter(new SimpleAdapter(this, data, R.layout.celebrate_item, new String[] { "reason", "date" },
 				new int[] { R.id.reason, R.id.cdate }));
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		menu.add(0, EVENT_LIST, 0, "Events");
 		menu.add(0, TEST_NOTIFY, 1, "Notify");
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		switch (item.getItemId()) {
 		case EVENT_LIST:
 			startActivityForResult(new Intent(this, EventList.class), 0);
 			return true;
 		case TEST_NOTIFY:
 			Intent active = new Intent(this, NotifyService.class);
 			startService(active);
 			return true;
 
 		}
 
 		return super.onMenuItemSelected(featureId, item);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
 		super.onActivityResult(requestCode, resultCode, intent);
 		fillData();
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		Intent i = new Intent(this, EventView.class);
 		Log.i("Celebrate", "Getting " + feasts.get(position).getEvent().getId());
 		i.putExtra(EventHelper.KEY_ROWID, feasts.get(position).getEvent().getId());
 		startActivityForResult(i, ACTIVITY_EDIT);
 	}
 
 }
