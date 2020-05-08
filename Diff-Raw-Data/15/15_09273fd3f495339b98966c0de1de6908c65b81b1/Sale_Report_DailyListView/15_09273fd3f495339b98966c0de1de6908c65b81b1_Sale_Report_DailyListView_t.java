 package com.rtt_ku.pos;
 
 import java.util.ArrayList;
 
 import com.database.pos.Database;
 import com.rtt_store.pos.StoreController;
 import com.salerecord.pos.DatabaseSaleRecord;
 
 import Inventory.Product;
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.ListView;
 
 public class Sale_Report_DailyListView extends Activity {
 	public static ListView listView;
	ArrayList<Product> list = new ArrayList<Product>();
	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.sale_report_daily_listview);
 		
		listView = (ListView)findViewById(R.id.sale_report_daily_listView);
 		Database myDb = new Database(this);
         myDb.getReadableDatabase();
         DatabaseSaleRecord DbSr = new DatabaseSaleRecord(this);
         DbSr.getReadableDatabase();
 		StoreController sCT = new StoreController(myDb,DbSr);
 
 		listView.setAdapter(new Sale_Report_DailyListViewAdapter(list, this));
 	}
 
 	public View getView() {
 		View view = LayoutInflater.from(getApplicationContext()).inflate(
 				R.layout.sale_report_daily_lineitem, null);
 		return view;
 
 	}
 }
