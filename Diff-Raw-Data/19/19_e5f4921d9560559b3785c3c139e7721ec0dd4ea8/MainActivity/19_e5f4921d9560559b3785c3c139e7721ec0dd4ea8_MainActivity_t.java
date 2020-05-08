 package com.test.fragment;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 
 public class MainActivity extends FragmentActivity implements ListTwoFragment.Callbacks,
 	ListThreeFragment.Callbacks {
 	public static final String KEY_ARGS = "args";
 	
 	private String curUri = "";
 	private String curArgs = "";
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		selectContent(false);
 	}
 	
 	private void selectContent(boolean addToBackStack) {
 		Fragment fragment;
 		
 		if (curUri.isEmpty()) {
 			// Use default fragment
 			fragment = new ListTwoFragment();
 			curUri = ListTwoFragment.class.getName();
 		}
 		else {
 			try {
 				Class<Fragment> fragmentClass = (Class<Fragment>) Class.forName(curUri);
 				fragment = fragmentClass.newInstance();
 			}
 			catch (Exception e) { // ClassNotFound, IllegalAccess, etc.
 				return;
 			}
 		}
 		
 		// Configure fragment
 		Bundle args = new Bundle();
 		args.putString(KEY_ARGS, curArgs);
 		fragment.setArguments(args);
 		
 		attachFragment(fragment, addToBackStack, curUri + ";" + curArgs, R.id.fragment_container);
 	}
 	
	private void selectContentInline() {
		doBack();
		selectContent(true);
	}
	
	private void doBack() {
		FragmentManager man = getSupportFragmentManager();
		int count = man.getBackStackEntryCount();
		if (count > 0) {
			man.popBackStack(man.getBackStackEntryAt(count - 1).getName(),
				FragmentManager.POP_BACK_STACK_INCLUSIVE);
		}
	}
	
 	protected void attachFragment(Fragment fragment, boolean addToBackStack, String tag, int replaceId) {
 		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
 		transaction.replace(replaceId, fragment, tag);
 		if (addToBackStack) transaction.addToBackStack(tag);
 		transaction.commit();
 	}
 	
 	@Override
 	public void onTwoButtonClick(String title) {
 		curUri = ListTwoFragment.class.getName();
 		curArgs = title;
 		selectContent(false);
 	}
 	
 	@Override
 	public void onTwoListClick() {
 		curUri = ListThreeFragment.class.getName();
 		curArgs = "";
 		selectContent(true);
 	}
 	
 	@Override
 	public void onThreeButtonClick(String title) {
 		curUri = ListThreeFragment.class.getName();
 		curArgs = title;
		selectContentInline();
 	}
 }
