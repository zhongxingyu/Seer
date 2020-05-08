 package se.chalmers.project14.main;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import se.chalmers.project14.database.Coordinates;
 import se.chalmers.project14.database.DatabaseAdapter;
 import se.chalmers.project14.database.DatabaseHandler;
 import android.os.Bundle;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.support.v4.app.NavUtils;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class ChooseLocationActivity extends ListActivity implements
 OnItemClickListener {
 
 	public final static String CTHBUILDING_COORDINATES = "se.chalmers.project14.main.CTHBUILDING_COORDINATES";
 	public final static String CTHBUILDING = "se.chalmers.project14.main.CTHBUILDING";
 	public final static String CTHDOOR_COORDINATES = "se.chalmers.project14.main.CTHDOOR_CTHBUILDING";
 	private DatabaseAdapter dba;
 	private ListView listview;
 	private List<Coordinates> coordinateList;
 	private DatabaseHandler db;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_choose_location);
 		listview = getListView();
 		db = new DatabaseHandler(this);
 		coordinateList = new ArrayList<Coordinates>();
 		coordinateList = db.getAllCoordinates();
 		sortCoordinateList(coordinateList);
 		dba = new DatabaseAdapter(this, R.layout.row, coordinateList);
 		setListAdapter(dba);
 		listview.setOnItemClickListener(this);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_choose_location, menu);
 		return true;
 	}
 
 	public void sortCoordinateList(List<Coordinates> sortCoordinateList) {
 		Collections.sort(coordinateList, new Comparator<Coordinates>() {
 
 			public int compare(Coordinates c1, Coordinates c2) {
 				return c1.getCTHplace().compareTo(c2.getCTHplace());
 
 			}
 		});
 
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	/**
 	 * Invoked when browse button is pressed, send the coordinates to Chalmers
 	 * school to next view
 	 * 
 	 * @param view
 	 */
	public void searchLocationButton(View view) {
 		Intent intent = new Intent(this, Map.class);
 		startActivity(intent);
 	}
 
 	/**
 	 * Invoked when List view is pressed, send the coordinate to selected
 	 * building and coordinates to the building doors and which building
 	 * 
 	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView,
 	 *      android.view.View, int, long)
 	 */
 	public void onItemClick(AdapterView<?> arg0, View view, int listPosition,
 			long i) {
 		Coordinates coordinate = coordinateList.get(listPosition);
 		String cthBuilding = coordinate.getCTHplace();
 		coordinate = db.getCoordinates(cthBuilding);
 		String doorAndbuildingCoordinates = coordinate.getCoordinates();
 		String[] dbc = doorAndbuildingCoordinates.split("-");
 		Intent intent = new Intent(this, Map.class);
 		intent.putExtra(CTHBUILDING_COORDINATES, dbc[0]);
 		intent.putExtra(CTHBUILDING, cthBuilding);
 		intent.putExtra(CTHDOOR_COORDINATES, dbc[1]);
 		startActivity(intent);
 
 	}
 }
