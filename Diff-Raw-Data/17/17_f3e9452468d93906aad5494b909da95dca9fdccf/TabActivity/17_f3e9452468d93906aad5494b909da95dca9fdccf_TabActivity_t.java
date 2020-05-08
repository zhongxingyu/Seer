 package ru.telepuzinator.tabs;
 
import java.util.ArrayList;

 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.widget.LinearLayout;
 
 public abstract class TabActivity extends FragmentActivity {
 	private LinearLayout mTabLayout;
 	private State mState;
 	
	private ArrayList<OnTabChangeListener> mTabListener;
 	
 	private Fragment mCurrent;
 	
 	@Override
 	protected void onCreate(Bundle state) {
 		super.onCreate(state);
 		setContentView(R.layout.tabs_activity);
		mTabListener = new ArrayList<OnTabChangeListener>();
 		mTabLayout = (LinearLayout) findViewById(R.id.tab_activity_tabs);
 		
 		if(state == null) {
 			mState = new State();
 		} else {
 			mState = new State(state, getLayoutInflater(), mTabLayout);
 			displayFragment(mState.getCurrentTab(), mState.getCurrent());
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
		if(!mTabListener.contains(tabListener)) {
			mTabListener.add(tabListener);
		}
 	}
 	
 	public void switchTab(int tab) {
 		if(mState.getCurrentTab() == tab) return;
 		displayFragment(tab, mState.getLast(tab));
 		mState.setCurrentTab(tab);
 		
		for(OnTabChangeListener listener: mTabListener) {
			listener.onTabChange(tab);
 		}
 	}
 	
 	public void addTab(Fragment frag, int tabLayout) {
 		int tab = mState.createTab(tabLayout, getLayoutInflater(), mTabLayout);
 		addFragment(frag, tab, mState.getCurrent() < 0);
 	}
 	
 	public void addFragment(Fragment frag) {
 		addFragment(frag, mState.getCurrentTab(), true);
 	}
 	
 	public void addFragment(Fragment frag, int tab, boolean display) {
 		int i = mState.addFragment(frag, tab);
 		if(display) {
 			displayFragment(tab, i);
 		}
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
