 package com.zotca.vbc.dbhistory;
 
 import com.zotca.vbc.dbhistory.core.DatabaseFileManager;
 
import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
 
 public class DatabaseActivity extends FragmentActivity {
 
 	private CardListPagerAdapter mPagerAdapter;
 	private DatabaseFileManager mFileManager;
 	private ViewPager mViewPager;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_database);
 		
 		mFileManager = DatabaseFileManager.getManager(this,
 				new DatabaseFileManager.PostProcessHandler() {
 			
 			@Override
 			public void onPostProcess(DatabaseFileManager manager) {
 				mFileManager = manager;
 				mPagerAdapter = new CardListPagerAdapter(getSupportFragmentManager(),
 						mFileManager);
 				mViewPager = (ViewPager) findViewById(R.id.pager);
 				mViewPager.setAdapter(mPagerAdapter);
 			}
 		});
 	}
 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.database, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id)
		{
		case R.id.about:
			this.startActivity(new Intent(this, AboutActivity.class));
			break;
		default:
			return false;
		}
		return true;
	}
 }
