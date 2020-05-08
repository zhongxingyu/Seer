 /*
  * Copyright 2012 ToureNPlaner
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 
 package de.uni.stuttgart.informatik.ToureNPlaner.UI;
 
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 
 public class TabListener<T extends Fragment> implements ActionBar.TabListener {
 	private Fragment mFragment;
 	private final SherlockFragmentActivity mActivity;
 	private final String mTag;
 	private final Class<T> mClass;
 
 	/**
 	 * Constructor used each time a new tab is created.
 	 *
 	 * @param activity The host Activity, used to instantiate the fragment
 	 * @param tag      The identifier tag for the fragment
 	 * @param clz      The fragment's Class, used to instantiate the fragment
 	 */
 	public TabListener(SherlockFragmentActivity activity, String tag, Class<T> clz) {
 		mActivity = activity;
 		mTag = tag;
 		mClass = clz;
 	}
 
 	/* The following are each of the ActionBar.TabListener callbacks */
 
 	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
 		boolean shouldCommit = false;
 		if (ft == null) {
 			ft = mActivity.getSupportFragmentManager().beginTransaction();
 			shouldCommit = true;
 		}
 
		mFragment = mActivity.getSupportFragmentManager().findFragmentByTag(mTag);
 		// Check if the fragment is already initialized
 		if (mFragment == null) {
 			// If not, instantiate and add it to the activity
 			mFragment = Fragment.instantiate(mActivity, mClass.getName());
 			ft.add(android.R.id.content, mFragment, mTag);
 		} else {
 			// If it exists, simply attach it in order to show it
 			ft.attach(mFragment);
 		}
 		if (shouldCommit)
 			ft.commit();
 	}
 
 	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
 		if (mFragment != null) {
 			// Detach the fragment, because another one is being attached
 			ft.detach(mFragment);
 		}
 	}
 
 	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
 		// User selected the already selected tab. Usually do nothing.
 	}
 }
