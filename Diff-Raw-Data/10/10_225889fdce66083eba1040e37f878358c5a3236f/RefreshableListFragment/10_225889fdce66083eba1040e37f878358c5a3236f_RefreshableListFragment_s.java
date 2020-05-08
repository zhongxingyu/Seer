 /*   
  * Copyright 2013 Karsten Patzwaldt
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.muckebox.android.ui.widgets;
 
 import org.muckebox.android.R;
 import org.muckebox.android.net.RefreshTask;
 
 import android.app.ListFragment;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 
 abstract public class RefreshableListFragment
 	extends ListFragment
 	implements RefreshTask.Callbacks{
 	
 	private MenuItem mRefreshItem = null;
 	private boolean mIsRefreshing = false;
 	private boolean mWasRunning = false;
 
 	// Clients just implement this to handle the action menu click
 	protected abstract void onRefreshRequested();
 
 	@Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
     	stopRefreshAnimation();
     	
     	super.onCreateOptionsMenu(menu, inflater);
     	
     	mRefreshItem = menu.findItem(R.id.action_refresh);
     	
     	if (mRefreshItem == null) {
     	    inflater.inflate(R.menu.refreshable_list, menu);
     	    mRefreshItem = menu.findItem(R.id.action_refresh);
     	}
     }
     
     @Override
     public void onDestroyOptionsMenu()
     {
     	stopRefreshAnimation();
     	
     	super.onDestroyOptionsMenu();
     }
     
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	if (item == mRefreshItem)
     	{
     		onRefreshRequested();
     	}
     	
     	return super.onOptionsItemSelected(item);
     }
 
     @Override
     public void onPause()
     {
     	stopRefreshAnimation();
     	
     	super.onPause();
     }
 
     protected void startRefreshAnimation()
     {
 		if (mRefreshItem != null && mRefreshItem.getActionView() == null)
 			mRefreshItem.setActionView(
 					ImageViewRotater.getRotatingImageView(
 							getActivity(), R.layout.action_view_refresh));
     }
     
     protected void stopRefreshAnimation()
     {
     	if (mRefreshItem != null)
     	{
 			if (mRefreshItem.getActionView() != null)
 				mRefreshItem.getActionView().clearAnimation();
 			
 			mRefreshItem.setActionView(null);
     	}
     }
     
 	@Override
 	public void onPrepareOptionsMenu(Menu menu) {
 		super.onPrepareOptionsMenu(menu);
 		
 		if (mIsRefreshing)
 		{
 			startRefreshAnimation();
 		} else
 		{
 			stopRefreshAnimation();
 		}
 	}
 	
 	protected void setRefreshing(boolean refreshing) {
 		mIsRefreshing = refreshing;
 		
 		if (refreshing)
 		{
 			startRefreshAnimation();
 		} else
 		{
 			stopRefreshAnimation();
 		}
 	}
 
 	@Override
 	public void onRefreshStarted() {
 		setRefreshing(true);
 		
 		mWasRunning = true;
 	}
 	
 	@Override
 	public void onRefreshFinished(boolean success) {
 		setRefreshing(false);
 	}
 	
 	public boolean wasRefreshedOnce() {
 	    return mWasRunning;
 	}
 }
