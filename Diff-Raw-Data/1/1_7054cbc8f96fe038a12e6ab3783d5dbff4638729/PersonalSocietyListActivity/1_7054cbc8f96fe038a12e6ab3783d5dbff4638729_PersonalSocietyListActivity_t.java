 package uk.ac.dur.duchess.ui.activity;
 
 import uk.ac.dur.duchess.R;
 import uk.ac.dur.duchess.ui.view.SocietyListView;
 import android.os.Bundle;
 
 public class PersonalSocietyListActivity extends BaseActivity
 {
 	private SocietyListView listView;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.society_list_layout);
 
 		listView = (SocietyListView) findViewById(R.id.societyListView);
 		listView.loadSocieties(this);
		listView.setEmptyView(findViewById(R.id.mySocietyListEmpty));
 	}
 }
