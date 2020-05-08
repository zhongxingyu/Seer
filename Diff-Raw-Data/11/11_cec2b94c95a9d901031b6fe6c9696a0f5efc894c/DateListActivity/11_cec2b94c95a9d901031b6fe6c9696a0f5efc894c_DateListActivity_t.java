 package ca.charland.tanita.manage;
 
 import java.util.List;
 
 import roboguice.activity.RoboListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import ca.charland.tanita.R;
 import ca.charland.tanita.db.Data;
 import ca.charland.tanita.db.DateListDataSource;
 import ca.charland.tanita.db.TanitaDataTable;
 
 /**
  * For a specific person shows a list of previous entries sorted by date and allows you to choose one to view.
  * 
  * @author mcharland
  * 
  */
 public class DateListActivity extends RoboListActivity {
 
 	static final String ID = "DATE";
 
 	private DateListDataSource datasource;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		datasource = new DateListDataSource(this);
 		datasource.openDatabaseConnection();
 
		final List<Data> data = datasource.queryWithOrdering(getSelection(), getOrderBy());
		//final List<Data> data = datasource.query(getSelection());
 		
 		setListAdapter(data);
 		setupListView(data);
 		
 		datasource.closeDatabaseConnection();
 	}
 
 	private String getOrderBy() {
 		return  TanitaDataTable.Column.DATE.toString();
 	}
 
 	private void setListAdapter(final List<Data> data) {
 		ArrayAdapter<Data> adapter = new ArrayAdapter<Data>(this, R.layout.date_list, data);
 
 		setListAdapter(adapter);
 	}
 
 	private String getSelection() {
 		Bundle extras = getIntent().getExtras();
 		int id = extras.getInt(PeopleListActivity.PERSON_ID);
 		String selection = TanitaDataTable.Column.PERSON.toString() + " = " + id;
 		return selection;
 	}
 
 	private void setupListView(final List<Data> data) {
 		ListView lv = getListView();
 		lv.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> par, View view, int pos, long id) {
 				Intent intent = new Intent(getBaseContext(), PersonDateActivity.class);
 
 				Data selectedItem = data.get(pos);
 				intent.putExtra(ID, selectedItem.getId());
 
 				startActivity(intent);
 			}
 		});
 	}
 
 	@Override
 	protected void onResume() {
 		datasource.openDatabaseConnection();
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 		datasource.closeDatabaseConnection();
 		super.onPause();
 	}
 }
