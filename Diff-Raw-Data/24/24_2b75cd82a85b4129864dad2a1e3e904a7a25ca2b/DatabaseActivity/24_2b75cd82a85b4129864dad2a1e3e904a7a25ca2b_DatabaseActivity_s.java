 package com.zotca.vbc.dbhistory;
 
 import com.zotca.vbc.dbhistory.core.DatabaseFileManager;
 
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 
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
 
 }
