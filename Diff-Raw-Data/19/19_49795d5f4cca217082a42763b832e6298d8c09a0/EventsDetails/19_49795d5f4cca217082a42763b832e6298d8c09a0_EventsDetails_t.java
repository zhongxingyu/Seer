 ﻿package de.tum.in.tumcampus;
 
 import android.app.Activity;
 import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
			Bitmap b = BitmapFactory.decodeFile(image);
			iv.setImageBitmap(Bitmap.createScaledBitmap(b, 360,
					(b.getHeight() * 360) / b.getWidth(), true));
 		}
 		em.close();
 	}
 }
