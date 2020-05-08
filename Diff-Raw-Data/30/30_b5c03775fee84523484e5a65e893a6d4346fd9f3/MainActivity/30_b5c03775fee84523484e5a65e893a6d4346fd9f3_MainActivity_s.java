 package com.megginson.sloop.app;
 
 import java.io.IOException;
 
 import com.megginson.sloop.R;
 import com.megginson.sloop.model.DataCollection;
 import com.megginson.sloop.model.DummyDataCollectionFactory;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.Menu;
 
 public class MainActivity extends FragmentActivity {
	
 	// FIXME temporary kludge - need a collection manager
	public static DataCollection dataCollection;
 
 	/**
 	 * The {@link PagerAdapter} for the current data collection.
 	 */
 	DataCollectionPagerAdapter dataCollectionPagerAdapter;
 
 	/**
 	 * The {@link ViewPager} that will host the data collection.
 	 */
 	ViewPager mViewPager;
 
 	@Override
	protected void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		// Create a DataCollection and wrap it in a pager adapter.
		try {
			dataCollection = DummyDataCollectionFactory.readDataCollection(getAssets(), "pwgsc_pre-qualified_supplier_data.csv");
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return;
 		}
 		dataCollectionPagerAdapter = new DataCollectionPagerAdapter(
 				getSupportFragmentManager(), dataCollection);
 
 		// Set up the ViewPager with the data collection adapter.
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(dataCollectionPagerAdapter);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 }
