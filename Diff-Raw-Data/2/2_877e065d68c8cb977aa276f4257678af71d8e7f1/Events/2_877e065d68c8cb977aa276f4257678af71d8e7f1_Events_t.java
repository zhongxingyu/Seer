 package de.tum.in.tumcampus;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.app.Activity;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.SlidingDrawer;
 import android.widget.TextView;
 
 public class Events extends Activity implements OnItemClickListener {
 
 	SQLiteDatabase db;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.events);
 
 		db = SQLiteDatabase.openDatabase(this.getDatabasePath("database.db")
 				.toString(), null, SQLiteDatabase.OPEN_READONLY);
 
 		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
 
 		// TODO move to manager
 
 		String[] weekDays = "So,Mo,Di,Mi,Do,Fr,Sa".split(",");
 
 		Cursor c = db
 				.rawQuery(
 						"SELECT image, name, strftime('%w', start_time), strftime('%d.%m.%Y %H:%M', start_time), "
 								+ "strftime('%H:%M', end_time), "
 								+ "location, description, id as _id "
								+ "FROM events WHERE end_time > datetime() "
 								+ "ORDER BY start_time ASC LIMIT 25", null);
 
 		while (c.moveToNext()) {
 			Map<String, Object> map = new HashMap<String, Object>();
 
 			map.put("image", c.getString(0));
 			map.put("name", c.getString(1));
 			map.put("infos", weekDays[c.getInt(2)] + ", " + c.getString(3)
 					+ " - " + c.getString(4) + "\n" + c.getString(5));
 			map.put("description", c.getString(6));
 			map.put("id", c.getString(7));
 			list.add(map);
 		}
 		c.close();
 
 		SimpleAdapter adapter;
 		adapter = new SimpleAdapter(this, list, R.layout.events_listview,
 				new String[] { "image", "name", "infos" }, new int[] {
 						R.id.icon, R.id.title, R.id.infos });
 
 		ListView lv = (ListView) findViewById(R.id.listView);
 		lv.setAdapter(adapter);
 		lv.setOnItemClickListener(this);
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> av, View v, int position, long id) {
 		ListView lv = (ListView) findViewById(R.id.listView);
 		@SuppressWarnings("unchecked")
 		Map<String, Object> map = (Map<String, Object>) lv.getAdapter()
 				.getItem(position);
 		String description = (String) map.get("description");
 		String image = (String) map.get("image");
 
 		SlidingDrawer sd = (SlidingDrawer) findViewById(R.id.slidingDrawer1);
 		if (!sd.isOpened()) {
 			sd.animateOpen();
 		}
 
 		TextView tv = (TextView) findViewById(R.id.description);
 		tv.setText(description);
 
 		ImageView iv = (ImageView) findViewById(R.id.image);
 		iv.setImageURI(Uri.parse(image));
 
 		// TODO optimize
 		double ratio = (double) iv.getDrawable().getIntrinsicWidth()
 				/ (double) iv.getDrawable().getIntrinsicHeight();
 		iv.getLayoutParams().width = 300;
 		iv.getLayoutParams().height = (int) Math.floor(300 / ratio);
 	}
 }
