 package ch.unibe.yala;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.google.android.maps.GeoPoint;
 
 public class TracksActivity extends Activity {
 
 	static List<String> values;
 	AlertDialog.Builder deleteBuilder;
 	AlertDialog.Builder chooseBuilder;
 	int pos;
 	ArrayAdapter<String> aAdapter;
 	EditText et;
 	TextView tv;
 	Dialog di;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_tracks);
 
 		ListView listView = (ListView) findViewById(R.id.list);
 		aAdapter = new ArrayAdapter<String>(this, R.layout.activity_tracks, R.id.info, values);
 		listView.setAdapter(aAdapter);
 		listView.setTextFilterEnabled(true);
 		listView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 
 				DataLayer datLay = new DataLayer(getBaseContext());
 				String points = datLay.getStats("point", position);
 				String times = datLay.getStats("time", position);
 				String alti = datLay.getStats("alti", position);
 				String pausePoints = datLay.getStats("pausePoint", position);
 				String pauseTimes = datLay.getStats("pauseTime", position);
 				FinishActivity.setPoints(convertGeoPointStringToArray(points));
 				FinishActivity.setTimes(convertLongStringToArray(times));
 				FinishActivity.setAltitude(convertDoubleStringToArray(alti));
 				FinishActivity.setPausePoints(convertGeoPointStringToArray(pausePoints));
 				FinishActivity.setPauseTimes(convertLongStringToArray(pauseTimes));
 				FinishActivity.save = false;
 				Intent intent = new Intent(getApplicationContext(), FinishActivity.class);
 				startActivity(intent);
 			}
 		});
 		listView.setOnItemLongClickListener(new OnItemLongClickListener() {
 			@Override
 			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
 				pos = position;
 				chooseBuilder.show();
 				return true;
 			}
 		});
 
 		DialogInterface.OnClickListener deleteDialog = new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				switch (which) {
 				case DialogInterface.BUTTON_POSITIVE:
 					DataLayer datLay = new DataLayer(getBaseContext());
 					datLay.deleteRun(pos);
 					aAdapter.remove(values.get(pos));
 					aAdapter.notifyDataSetChanged();
 					break;
 				case DialogInterface.BUTTON_NEGATIVE:
 					break;
 				}
 			}
 		};
 		deleteBuilder = new AlertDialog.Builder(this);
 		deleteBuilder.setMessage(getString(R.string.delete));
 		deleteBuilder.setPositiveButton("Yes", deleteDialog);
 		deleteBuilder.setNegativeButton("No", deleteDialog);
 
 		chooseBuilder = new AlertDialog.Builder(this);
 		chooseBuilder.setTitle(getString(R.string.choose));
 		chooseBuilder.setItems(new String[] { "Delete", "Rename" }, new OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				if (which == 0)
 					deleteBuilder.show();
 				else{
 					tv.setText(values.get(pos));
 					di.show();
 				}
 			}
 		});
 
 		di = new Dialog(this);
 		di.setContentView(R.layout.dialog_rename);
 		di.setTitle("Rename");
 		et = (EditText) di.findViewById(R.id.newName);
 		tv = (TextView) di.findViewById(R.id.renameDialog);
 		Button noRenBut = (Button) di.findViewById(R.id.noRename);
 		noRenBut.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				di.dismiss();
 			}
 		});
 		Button renBut = (Button) di.findViewById(R.id.doRename);
 		renBut.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				DataLayer datLay = new DataLayer(getBaseContext());
 				String s = et.getText().toString();
 				if (s.length() > 0) {
 					datLay.setName(s, pos);
 					values.set(pos, s);
 					aAdapter.notifyDataSetChanged();
 				}
 				di.dismiss();
 			}
 		});
 	}
 
 	public static void setValues(List<String> v) {
 		values = v;
 	}
 
 	public static Long[] convertLongStringToArray(String s) {
 		if (s.length() < 1)
 			return new Long[0];
 		String[] arr = s.split(",");
 		Long[] longs = new Long[arr.length];
 		for (int i = 0; i < arr.length; i++)
 			longs[i] = Long.parseLong(arr[i]);
 		return longs;
 	}
 
 	public static Double[] convertDoubleStringToArray(String s) {
 		if (s.length() < 1)
 			return new Double[0];
 		String[] arr = s.split(",");
 		Double[] doubles = new Double[arr.length];
 		for (int i = 0; i < arr.length; i++)
 			doubles[i] = Double.parseDouble(arr[i]);
 		return doubles;
 	}
 
 	public static GeoPoint[] convertGeoPointStringToArray(String s) {
 		if (s.length() < 1)
 			return new GeoPoint[0];
 		String[] arr = s.split(",");
 		GeoPoint[] geoPoints = new GeoPoint[arr.length];
 		String[] point = new String[2];
 		for (int i = 0; i < arr.length; i++) {
 			point = arr[i].split(";");
 			geoPoints[i] = new GeoPoint(Integer.parseInt(point[0]), Integer.parseInt(point[1]));
 		}
 		return geoPoints;
 	}
 
 }
