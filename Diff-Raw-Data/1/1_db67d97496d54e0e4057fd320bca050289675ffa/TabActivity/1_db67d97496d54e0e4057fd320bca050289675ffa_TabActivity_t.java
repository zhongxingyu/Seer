 package ru.telepuzinator.tabs;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.widget.LinearLayout;
 
 public abstract class TabActivity extends FragmentActivity {
 	private LinearLayout mTabLayout;
 	private State mState;
 	
 	private OnTabChangeListener mTabListener;
 	
 	private Fragment mCurrent;
 	private boolean mFirstLaunch = false;
 	
 	@Override
 	protected void onCreate(Bundle state) {
 		super.onCreate(state);
 		setContentView(R.layout.tabs_activity);
 		mTabLayout = (LinearLayout) findViewById(R.id.tab_activity_tabs);
 		
 		if(state == null) {
 			mState = new State();
 			mFirstLaunch = true;
 		} else {
 			mState = new State(state, getLayoutInflater(), mTabLayout);
 			displayFragment(mState.getCurrentTab(), mState.getCurrent());
 		}
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		if(mFirstLaunch) {
 			switchTab(0);
			mFirstLaunch = false;
 		}
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		mState.saveState(outState, getSupportFragmentManager());
 		super.onSaveInstanceState(outState);
 	}
 	
 	@Override
 	public void onBackPressed() {
 		replaceBack();
 	}
 	
 	public void setOnTabChangeListener(OnTabChangeListener tabListener) {
 		mTabListener = tabListener;
 	}
 	
 	public void switchTab(int tab) {
 		if(mState.getCurrentTab() == tab) return;
 		mState.setCurrentTab(tab);
 		displayFragment(tab, mState.getLast(tab));
 		
 		if(mTabListener != null) {
 			mTabListener.onTabChange(tab);
 		}
 	}
 	
 	public void addTab(Fragment frag, int tabLayout) {
 		int tab = mState.createTab(tabLayout, getLayoutInflater(), mTabLayout);
 		addFragment(frag, tab);
 	}
 	
 	public void addFragment(Fragment frag) {
 		addFragment(frag, mState.getCurrentTab());
 	}
 	
 	public void addFragment(Fragment frag, int tab) {
 		int i = mState.addFragment(frag, tab);
 		displayFragment(tab, i);
 	}
 	
 	public void replaceBack() {
 		int num = mState.getPrevious();
 		if(num >= 0) {
 			displayFragment(mState.getCurrentTab(), num);
 		} else {
 			finish();
 		}
 	}
 	
 	private Fragment getFragment(int number) {
 		return mState.getFragment(number);
 	}
 	
 	private void displayFragment(int tab, int number) {
 		FragmentManager fm = getSupportFragmentManager();
 		mState.onFragmentChange(fm, tab);
 		FragmentTransaction ft = fm.beginTransaction();
 		if(mCurrent != null) ft.remove(mCurrent);
 		mCurrent = getFragment(number);
 		ft.replace(R.id.tab_activity_tab_content, mCurrent);
 		ft.commit();
 	}
 }
