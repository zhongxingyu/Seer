 package ua.cooperok.foxhunting.gui;
 
 import java.util.ArrayList;
 
 import ua.cooperok.foxhunting.db.FoxhuntingDatabase;
 import ua.cooperok.foxhunting.db.RecordsColumns;
 import ua.cooperok.foxhunting.R;
 import android.app.ListActivity;
 import android.os.Bundle;
 import android.database.Cursor;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class RecordsActivity extends ListActivity {
 
 	private FoxhuntingDatabase db_adapter;
 	
 	private Cursor records;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		
 		db_adapter = new FoxhuntingDatabase(getApplicationContext());

 		records = getRecords();
 		
 		initListAdapter();
		
         ListView lv = getListView();
         
         lv.setTextFilterEnabled(true);
 		
 		setContentView(R.layout.records_layout);
 	
 	}
 	
 	private void initListAdapter() {
 		
 		setListAdapter(new ArrayAdapter<String>(this, R.layout.records_list_item, getRecordsArrayList()));
 		
 	}
 	
 	private Cursor getRecords() {
 		
 		return db_adapter.getRecords();
 		
 	}
 	
 	private ArrayList <String> getRecordsArrayList() {
 		
 		ArrayList <String> records_list = new ArrayList <String>();
 		
 		if (records.moveToFirst()) {
 			
 			do {
 				
 				records_list.add(
 						records.getString(records.getColumnIndex(RecordsColumns.USERNAME))
 						+ " / " + records.getInt(records.getColumnIndex(RecordsColumns.STEPS))
 				);
 				
 			} while(records.moveToNext());
 			
 		}
 		
 		return records_list;
 		
 	}
 	
 }
