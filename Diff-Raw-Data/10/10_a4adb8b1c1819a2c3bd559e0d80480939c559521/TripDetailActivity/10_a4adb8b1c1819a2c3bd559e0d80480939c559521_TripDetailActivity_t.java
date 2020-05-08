 package org.katta.activity;
 import org.katta.android.gd.db.DBConstants;
 import org.katta.android.gd.db.SQLRepository;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class TripDetailActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.trip_detail);
 		
 		Button btnAdd = (Button) findViewById(R.id.btn_trip_detail_add);
 		btnAdd.setOnClickListener(new AddTripButtonListener(this));
 	}
 }
 
 class AddTripButtonListener implements OnClickListener {
 
 	private final TripDetailActivity context;
 
 	public AddTripButtonListener(TripDetailActivity context) {
 		this.context = context;
 	}
 
 	public void onClick(View view) {
 		Toast.makeText(context, "adding trip", Toast.LENGTH_SHORT).show();
 		
 		SQLiteDatabase database = new SQLRepository(context).getWritableDatabase();
 		ContentValues values = new ContentValues();
 		EditText txtName = (EditText) context.findViewById(R.id.txt_trip_detail_name);
 		EditText txtDescription = (EditText) context.findViewById(R.id.txt_trip_detail_description);
 		DatePicker dtStartDate = (DatePicker) context.findViewById(R.id.date_trip_detail_startdate);
 		//TODO get date and persist
 		values.put(DBConstants.COL_TRIP_NAME, txtName.getText().toString());
 		values.put(DBConstants.COL_TRIP_DESCRIPTION, txtDescription.getText().toString());
 		
		long rowId = database.insert(DBConstants.TBL_TRIP, null, values);
		Toast.makeText(context, rowId==-1?"Failed to save data.":"Successfully added.", Toast.LENGTH_SHORT).show();
 	}
 }
