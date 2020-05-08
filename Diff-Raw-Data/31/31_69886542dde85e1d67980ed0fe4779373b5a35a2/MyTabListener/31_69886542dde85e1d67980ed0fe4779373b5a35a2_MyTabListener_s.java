 package github.graded_reader;
 
 import android.app.ActionBar.Tab;
 import android.app.ActionBar.TabListener;
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 
 public class MyTabListener<T extends Fragment> implements TabListener {
 	private Fragment mFragment;
 	private final Activity mActivity;
 	private final String mTag;
 	private final Class<T> mClass;
 
 	public MyTabListener(Activity activity, String tag, Class<T> clz) {
 		mActivity = activity;
 		mTag = tag;
 		mClass = clz;
 	}
 
 	public void onTabSelected(Tab tab, FragmentTransaction ft) {
 		
 		if (mFragment == null) {
 			mFragment = Fragment.instantiate(mActivity, mClass.getName());
			ft.add(android.R.id.content, mFragment, mTag);
 		} else {
 			ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
 			ft.attach(mFragment);
 		}
 	}
 
 	public void onTabReselected(Tab tab, FragmentTransaction ft) {
 		if (mFragment == null) {
 			mFragment = Fragment.instantiate(mActivity, mClass.getName());
			ft.add(android.R.id.content, mFragment, mTag);
 		} else {
 			ft.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
 			ft.attach(mFragment);
 		}
 	}
 
 	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 		if (mFragment != null) {
 			ft.setCustomAnimations(android.R.animator.fade_in,android.R.animator.fade_out);
 			ft.detach(mFragment);
 		}
 	}
 }
