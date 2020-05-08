 package com.example.educationalapp;
 
 import android.os.Bundle;
 import android.app.ActionBar;
 import android.app.ActionBar.Tab;
 import android.app.ActionBar.TabListener;
 import android.app.Activity;
 import android.app.FragmentTransaction;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.drawable.ColorDrawable;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.app.ListFragment;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.ArrayAdapter;
 import android.support.v4.view.PagerAdapter;
 
 public class TopicsTabs extends FragmentActivity implements TabListener {
 	ActionBar actionBar;
 	ViewPager viewPager;
 	int tabNum = 0;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	setContentView(R.layout.topics_tabs_layout);
     
     	
     	ActionBar bar = getActionBar();
 		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3CC7C7")));
     	
     	viewPager = (ViewPager) findViewById(R.id.pager);
     	viewPager.setAdapter(new MyTopicsAdapter(getSupportFragmentManager()));
     	//viewPager.setCurrentItem(i.getIntExtra("limits tab", 0));
     	viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
 			
 			@Override
 			public void onPageSelected(int arg0) {
 				//Log.d("MathPath", "onPageSelected at position " + arg0);
 				actionBar.setSelectedNavigationItem(arg0);
 				tabNum = arg0;
 			}
 			
 			@Override
 			public void onPageScrolled(int arg0, float arg1, int arg2) {
 				//Log.d("MathPath", "onPageScrolled at position " + arg0 + " from " + arg1 + " with number of pixels = " + arg2);
 				
 			}
 			
 			@Override
 			public void onPageScrollStateChanged(int arg0) {
 //				if (arg0 == ViewPager.SCROLL_STATE_IDLE)
 //					Log.d("MathPath", "onPageScrollStateChanged Idle");
 //				if (arg0 == ViewPager.SCROLL_STATE_DRAGGING)
 //					Log.d("MathPath", "onPageScrollStateChanged Dragging");
 //				if (arg0 == ViewPager.SCROLL_STATE_SETTLING)
 //					Log.d("MathPath", "onPageScrollStateChanged Settling");
 				
 			}
 		});
     	
     	actionBar = getActionBar();
     	
     	ActionBar.Tab limitsTab = actionBar.newTab();
     	limitsTab.setText("Limits");
     	limitsTab.setTabListener(this);
     	
     	ActionBar.Tab derivativesTab = actionBar.newTab();
     	derivativesTab.setText("Derivatives");
     	derivativesTab.setTabListener(this);
     	
     	ActionBar.Tab integralsTab = actionBar.newTab();
     	integralsTab.setText("Integrals");
     	integralsTab.setTabListener(this);
     	
     	ActionBar.Tab finalTab = actionBar.newTab();
     	finalTab.setText("Final");
     	finalTab.setTabListener(this);
    
     	
     	actionBar.addTab(limitsTab);
     	actionBar.addTab(derivativesTab);
     	actionBar.addTab(integralsTab);
     	actionBar.addTab(finalTab);
     	
     	actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu items for use in the action bar
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.topics_tabs_action_bar, menu);
         return super.onCreateOptionsMenu(menu);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle presses on the action bar items
     	Intent i = new Intent(this, Quiz.class);
     	
         switch (item.getItemId()) {
             case R.id.action_quiz:
             	if (tabNum == 0) {
             		i.putExtra("quiz", "limitsQuiz.txt");
            		
 					this.startActivity(i);
             	} else if (tabNum == 1) {
             		i.putExtra("quiz", "derivativesQuiz.txt");
 					
 					this.startActivity(i);
             	} else if (tabNum == 2) {
             		i.putExtra("quiz", "integralsQuiz.txt");
     				
     				this.startActivity(i);
             	}
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
 	@Override
 	public void onTabReselected(Tab tab, FragmentTransaction ft) {
 		//Log.d("MathPath", "onTabReselected at position " + tab.getPosition() + " name " + tab.getText());
 		
 	}
 
 	@Override
 	public void onTabSelected(Tab tab, FragmentTransaction ft) {
 		//Log.d("MathPath", "onTabSelected at position " + tab.getPosition() + " name " + tab.getText());
 		viewPager.setCurrentItem(tab.getPosition());
 		tabNum = tab.getPosition();
 	}
 
 	@Override
 	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 		//Log.d("MathPath", "onTabUnselected at position " + tab.getPosition() + " name " + tab.getText());
 		
 	}
 }
 
 class MyTopicsAdapter extends FragmentStatePagerAdapter {
 
 	public MyTopicsAdapter(FragmentManager fm) {
 		super(fm);
 	}
 
 	@Override
 	public Fragment getItem(int arg0) {
 		Fragment fragment = null;
 		
 		if (arg0 == 0)
 			fragment = new LimitsAllVideosTab();
 		if (arg0 == 1)
 			fragment = new DerivativesAllVideosTab();
 		if (arg0 == 2) 
 			fragment = new IntegralsAllVideosTab();
 		if (arg0 == 3)
 			fragment = new FinalTab();
 		return fragment;
 	}
 
 	@Override
 	public int getCount() {
 		return 4;
 	}
 	
 }
