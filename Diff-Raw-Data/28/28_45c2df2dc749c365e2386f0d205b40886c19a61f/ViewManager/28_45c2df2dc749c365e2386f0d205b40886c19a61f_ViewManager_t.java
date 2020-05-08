 package com.view.threeLevel;
 
 import java.util.Stack;
 
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 
 import com.view.demo.BaseFragment;
 import com.view.demo.R;
 
 public class ViewManager{
 	long mLastTime;
 	private Stack<BaseFragment> mStack = new Stack<BaseFragment>();
 	private ViewGroup mCurrentView;
 	private ViewGroup mNextView;
 	private ViewGroup mPreView;
 	private ViewGroup mCoreView;
 	private FragmentManager mFragmentManager;
 	
 	private static ViewManager mInstance = new ViewManager();
 	
 	private ViewManager(){
 		
 	}
 	
 	public void setCoreView(ViewGroup coreView){
 		int count = coreView.getChildCount();
 		mCoreView = (ViewGroup) coreView;
		mPreView = (ViewGroup) mCoreView.findViewById(R.id.container_first);
		mCurrentView = (ViewGroup) mCoreView.findViewById(R.id.container_second);
		mNextView = (ViewGroup) mCoreView.findViewById(R.id.container_third);
 		
 	}
 	
 	public void setFragmentManager(FragmentManager fragmentManager){
 		mFragmentManager = fragmentManager;
 	}
 	
 	public static ViewManager getInstance(){
 		return mInstance;
 	}
 	
 	public BaseFragment getCurrentFragment(){
 		BaseFragment currentFragment = (BaseFragment) mFragmentManager.findFragmentById(mCurrentView.getId());
 		return currentFragment;
 	}
 	
 	public synchronized void showPage(BaseFragment fragment, boolean saveToStack){
 		showPage(fragment,saveToStack,true);
 	}
 	
 	public synchronized void showPage(BaseFragment fragment, boolean saveToStack, boolean scroll){
 		if(System.currentTimeMillis() - mLastTime < 500){
 			return ;
 		}
 		
 		mStack.push(fragment);
 		if(mNextView == null){
 			log("nextView is null");
 			return ;
 		}
 		mNextView.bringToFront();
 		showFragment(fragment,saveToStack,scroll);
 		destroyPreFragment();
 		
 		ViewGroup temp = mCurrentView;
 		
 		mCurrentView = mNextView;
 		mNextView = mPreView;
 		mPreView = temp;
 		
 		mPreView.setEnabled(false);
 		mLastTime = System.currentTimeMillis();
 	}
 	
 	private void destroyPreFragment(){
 		BaseFragment preFragment = (BaseFragment) mFragmentManager.findFragmentById(mPreView.getId());
 		if(preFragment == null){
 			return ;
 		}
 		FragmentTransaction ft = mFragmentManager.beginTransaction();
 		ft.detach(preFragment);
 		ft.commitAllowingStateLoss();
 	}
 	
 	private void showFragment(BaseFragment fragment, boolean saveToStack, boolean scroll){
 		FragmentTransaction ft = mFragmentManager.beginTransaction();
 		if(fragment.isDetached()){
 			ft.attach(fragment);
 		}
 		String name = null;
 		if(fragment instanceof BasePopFragment){
 			BasePopFragment basePopFragment = (BasePopFragment) fragment;
 			basePopFragment.setIsScroll(scroll);
 			name = basePopFragment.getFragmentTag();
 		}
 		
 //		ft.setCustomAnimations(enter, exit);
 		ft.add(mNextView.getId(), fragment, name);
 		ft.commitAllowingStateLoss();
 	}
 	
 	void showFragment(BaseFragment fragment, boolean saveToStack){
 		showFragment(fragment, saveToStack, true);
 	}
 	
 	public synchronized boolean dismissPage(){
 		if(mStack.isEmpty()){
 			return false;
 		}
 		//移走当前的fragment
 		BaseFragment currentFragment = mStack.pop();
 		if(mStack.isEmpty()){
 			mStack.push(currentFragment);
 			return false;
 		}
 		
 		destroyCurrentFragment();
 		mPreView.setEnabled(true);
 		mPreView.bringToFront();
 		//得到之前的fragment并显示
 		BaseFragment fragment = mStack.pop();//不明白为什么这么处理
 		if(!mStack.isEmpty()){
 			BaseFragment preloadFragment = mStack.peek();
 
 			showFragment(preloadFragment, false, false);
 		}
 		
 		mStack.push(fragment);
 		
 		ViewGroup temp = mPreView;
 		mPreView = mNextView;
 		mNextView = mCurrentView;
 		mCurrentView = temp;
 
 		
 		return true;
 	}
 	
 	private void destroyCurrentFragment(){
 		BaseFragment currentFragment = (BaseFragment) mFragmentManager.findFragmentById(mCurrentView.getId());
 	    if(currentFragment == null){
 	    	return ;
 	    }
 	    
 	    FragmentTransaction ft = mFragmentManager.beginTransaction();
 	    ft.detach(currentFragment);
 	    ft.remove(currentFragment);
 	    ft.commitAllowingStateLoss();
 	    
 	    mFragmentManager.popBackStack();
 	}
 	
 	public void destroyFragment(View view){
 		BaseFragment fragment = (BaseFragment) mFragmentManager.findFragmentById(view.getId());
 		if(fragment == null){
 			return ;
 		}
 		
 		FragmentTransaction ft = mFragmentManager.beginTransaction();
 		ft.detach(fragment);
 		ft.commitAllowingStateLoss();
 	}
 	
 	public void clearAll(){
 		mStack.clear();
 		destroyFragment(mCurrentView);
 		destroyFragment(mNextView);
 		destroyFragment(mPreView);
 		//为什么要再次初始化
		mPreView = (ViewGroup) mCoreView.findViewById(R.id.container_first);
		mCurrentView = (ViewGroup) mCoreView.findViewById(R.id.container_second);
		mNextView = (ViewGroup) mCoreView.findViewById(R.id.container_third);
 	}
 	
 	private void log(String s){
 		Log.d("zhou",s);
 	}
 }
