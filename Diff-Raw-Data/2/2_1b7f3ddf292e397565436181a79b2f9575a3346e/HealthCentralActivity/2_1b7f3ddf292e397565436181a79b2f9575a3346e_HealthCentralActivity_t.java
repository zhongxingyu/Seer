 package com.healthcentral.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.kroz.activerecord.ActiveRecordException;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.healthcentral.common.CustomVerticalAdapter;
 import com.heathcentral.model.Vertical;
 import com.heathcentral.service.DatabaseController;
 import com.heathcentral.service.GetVerticalsTask;
 
 public class HealthCentralActivity extends Activity implements
 		OnItemClickListener {
 
 	private DatabaseController databaseController;
 	private ListView verticalsListView;
 	private List<Vertical> verticals = new ArrayList<Vertical>();
 
 	public void onCreate(Bundle paramBundle) {
 		super.onCreate(paramBundle);
 		requestWindowFeature(1);
 		setContentView(R.layout.main);
 		verticalsListView = (ListView) findViewById(R.id.list_verticals);
 		this.verticalsListView.setOnItemClickListener(this);
 		this.databaseController = new DatabaseController(
 				getApplicationContext());
 		try {
 			DatabaseController.initDatabase();
 			new GetVerticalsTask(this, this.databaseController).execute(new String[0]);
 			return;
 		} catch (ActiveRecordException localActiveRecordException) {
 			while (true)
 				localActiveRecordException.printStackTrace();
 		}
 	}
 
 	protected void onDestroy() {
 		super.onDestroy();
 		try {
 			if (this.databaseController.getIsOpenDatabase())
 				this.databaseController.closeConnection();
 			return;
 		} catch (ActiveRecordException localActiveRecordException) {
 			while (true)
 				localActiveRecordException.printStackTrace();
 		}
 	}
 
 	public void onItemClick(AdapterView<?> paramAdapterView, View paramView,
 			int paramInt, long paramLong) {
 		Intent localIntent = new Intent(this, SiteResourcesActivity.class);
 		localIntent.putExtra("vertical", ((Vertical) this.verticals.get(paramInt)).verticalId);
 		localIntent.putExtra("hasSlideshows", ((Vertical) this.verticals.get(paramInt)).getHasSlideshows());
 		localIntent.putExtra("hasQuizzes", ((Vertical) this.verticals.get(paramInt)).getHasQuizzes());
 		startActivity(localIntent);
 	}
 
 	public void updateList() {
 		this.verticals = this.databaseController.getVerticals();
 		CustomVerticalAdapter localCustomAdapter = new CustomVerticalAdapter(this, this.verticals);
 		this.verticalsListView.setAdapter(localCustomAdapter);
		//((TextView) findViewById(R.id.action_activity)).setVisibility(View.VISIBLE);
 	}
 }
