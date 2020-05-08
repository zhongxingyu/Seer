 package com.thunderunited.meterstand;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.ContentValues;
 import android.content.Context;
import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class Handmatig extends Activity {
 	
 	private static final String TAG = Handmatig.class.getName();
 	
	private static final int DB_VERSION = 1;
 	private Button _button;
 	private static final String DATA_PATH = Environment
 			.getExternalStorageDirectory()
 			+ "/"
 			+ ScanActivity.class.getPackage().getName() + "/";
 	private String DB_NAME = DATA_PATH + "/foto.db";
 	private String TABLE_NAME = "handmatig";
 	private Spinner spinner1, spinner2;
 	private SimpleDateFormat dateFormatISO8601 = new SimpleDateFormat(
 			"yyyy-MM-dd");
 	private String crntDate = dateFormatISO8601.format(new Date());
 	private Bitmap ocrBitmap;
 
 	public static final String METER_LEVEL_KEY = "meter_level";
 	public static final String IMAGE_PATH_KEY = "image_path";
 	
 	
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		Bundle bundle = getIntent().getExtras();
 		String meterValue = null;
 		String ocrImagePath = null;
 		if (bundle != null) {
 			meterValue = bundle.getString(METER_LEVEL_KEY);
 			ocrImagePath = bundle.getString(IMAGE_PATH_KEY);
 		}
 
 		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
 		setContentView(R.layout.activity_handmatig);
 		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
 				R.layout.window_title);
 
 		if (meterValue != null) {
 			EditText meterValueEditText = (EditText) findViewById(R.id.tbValue);
 			meterValueEditText.setText(meterValue);
 		}
 
 		if (ocrImagePath != null) {
 			BitmapFactory.Options options = new BitmapFactory.Options();
 			options.inSampleSize = 4;
 
 			ocrBitmap = BitmapFactory.decodeFile(ocrImagePath, options);
 			ImageView ocrImageView = (ImageView) findViewById(R.id.ocrImageView);
 			ocrImageView.setImageBitmap(ocrBitmap);
 		}
 
 		_button = (Button) findViewById(R.id.btnSubmit);
 		_button.setOnClickListener(new ButtonClickHandler());
 
 		// Type Meter lijst
 		List<String> typeofMeterList = new ArrayList<String>();
 		typeofMeterList.add("Soort Meter");
 		typeofMeterList.add("Particulier");
 		typeofMeterList.add("Business");
 
 		// Vullen van Dropdown type Meter
 		fillSpinner((Spinner) findViewById(R.id.sprTypeofMeter),
 				typeofMeterList);
 
 		// Type Meterstand
 		List<String> typeofValueList = new ArrayList<String>();
 		typeofValueList.add("Soort Stand");
 		typeofValueList.add("MHz");
 		typeofValueList.add("KHz");
 
 		// Vullen van Dropdown type Meterstand
 		fillSpinner((Spinner) findViewById(R.id.sprTypeofValue),
 				typeofValueList);
 	}
 
 	public class ButtonClickHandler implements View.OnClickListener {
 		public void onClick(View view) {
 			createDatabase();
 			updateDatabase();
 		}
 	}
 
 	void createDatabase() {
 		SQLiteDatabase myDb = openOrCreateDatabase(DB_NAME,
 				Context.MODE_PRIVATE, null);
 		String MySQL = "create table if not exists "
 				+ TABLE_NAME
 				+ " (_id INTEGER primary key autoincrement, soortMeter TEXT not null, stand INTEGER not null, soortStand TEXT not null, datum DATETIME, image BLOB);";
 		myDb.execSQL(MySQL);
 		
		try {
			String statement = "ALTER TABLE " + TABLE_NAME + " ADD COLUMN image BLOB ";
			myDb.execSQL(statement);
		} catch(SQLException e) {
			Log.i(TAG, "Column image already exists.");
		} finally {
			if (myDb != null) {
				myDb.close();
			}
 		}
 	}
 
 	void updateDatabase() {
 		spinner1 = (Spinner) findViewById(R.id.sprTypeofMeter);
 		spinner2 = (Spinner) findViewById(R.id.sprTypeofValue);
 		EditText stand = (EditText) findViewById(R.id.tbValue);
 
 		if (spinner1.getSelectedItem() == "Soort Meter") {
 			Toast.makeText(this, "U heeft geen soort meter ingevuld!",
 					Toast.LENGTH_SHORT).show();
 		} else if (isEmpty(stand) != false) {
 			Toast.makeText(this, "U heeft geen stand ingevuld!",
 					Toast.LENGTH_SHORT).show();
 		} else if (spinner2.getSelectedItem() == "Soort Stand") {
 			Toast.makeText(this, "U heeft geen soort stand ingevuld!",
 					Toast.LENGTH_SHORT).show();
 		} else {
 			
 			byte[] imageBytes = null;
 			
 			if (ocrBitmap != null) {
 				imageBytes = ImageUtil.bitmapToByteArray(ocrBitmap);
 			}
 			
 			ContentValues values = new ContentValues();
 			values.put("soortMeter", spinner1.getSelectedItem().toString());
 			values.put("stand", spinner2.getSelectedItem().toString());
 			values.put("soortStand", stand.getText().toString());
 			values.put("datum", crntDate);
 			values.put("image", imageBytes);
 			
 			SQLiteDatabase myDb = openOrCreateDatabase(DB_NAME,
 					Context.MODE_PRIVATE, null);
 			myDb.insert(TABLE_NAME, null, values);
 			myDb.close();
 			super.finish();
 		}
 	}
 
 	private boolean isEmpty(EditText etText) {
 		if (etText.getText().toString().trim().length() > 0) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	public void fillSpinner(Spinner id, List<String> spin) {
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_spinner_item, spin);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		Spinner Items = id;
 		Items.setAdapter(adapter);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.handmatig, menu);
 		return true;
 	}
 
 }
