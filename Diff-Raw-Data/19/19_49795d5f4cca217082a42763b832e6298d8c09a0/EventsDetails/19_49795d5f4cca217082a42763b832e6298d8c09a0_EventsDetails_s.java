 ﻿package de.tum.in.tumcampus;
 
 import android.app.Activity;
 import android.database.Cursor;
import android.net.Uri;
 import android.os.Bundle;
 import android.widget.ImageView;
 import android.widget.TextView;
 import de.tum.in.tumcampus.models.EventManager;
 
 /**
  * Activity to show event details (name, location, image, description, etc.)
  */
 public class EventsDetails extends Activity {
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.events_details);
 
 		// get event details from db
 		EventManager em = new EventManager(this, Const.db);
 		Cursor c = em.getDetailsFromDb(getIntent().getStringExtra("id"));
 
 		if (c.moveToNext()) {
 			String description = c.getString(c.getColumnIndex("description"));
 			String image = c.getString(c.getColumnIndex("image"));
 
 			String[] weekDays = "So,Mo,Di,Mi,Do,Fr,Sa".split(",");
 
 			setTitle(c.getString(c.getColumnIndex("name")));
 
 			/**
 			 * <pre>
 			 * show infos as:
 			 * Week-day, Start DateTime - End Time
 			 * Location
 			 * Link
 			 * </pre>
 			 */
 			String infos = weekDays[c.getInt(c.getColumnIndex("weekday"))];
 			infos += ", " + c.getString(c.getColumnIndex("start_de")) + " - "
 					+ c.getString(c.getColumnIndex("end_de")) + "\n";
 			infos += c.getString(c.getColumnIndex("location")) + "\n";
 			infos += c.getString(c.getColumnIndex("link"));
 
 			TextView tv = (TextView) findViewById(R.id.infos);
 			tv.setText(infos.trim());
 
 			tv = (TextView) findViewById(R.id.description);
 			tv.setText(description);
 
 			ImageView iv = (ImageView) findViewById(R.id.image);
			iv.setImageURI(Uri.parse(image));

			// resize image: 350 x height adapted in aspect ratio
			if (iv.getDrawable() != null) {
				double ratio = (double) iv.getDrawable().getIntrinsicWidth()
						/ (double) iv.getDrawable().getIntrinsicHeight();

				int screen = getWindowManager().getDefaultDisplay().getWidth();
				int width = Math.min((int)(screen*0.9), 375);
				iv.getLayoutParams().width = width;
				iv.getLayoutParams().height = (int) Math.floor(width / ratio);
			}
 		}
 		em.close();
 	}
 }
