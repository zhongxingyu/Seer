 /*
  * Copyright 2013 Thales Ferreira
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.thalespf.library.wizardmaker.wizard.internal;
 
 import android.content.Context;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentStatePagerAdapter;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 
 import com.actionbarsherlock.ActionBarSherlock;
 import com.thalespf.library.wizardmaker.R;
 import com.thalespf.library.wizardmaker.wizard.BranchPage;
 import com.thalespf.library.wizardmaker.wizard.IWizard;
 import com.thalespf.library.wizardmaker.wizard.IWizardPage;
 import com.thalespf.library.wizardmaker.wizard.WizardMakerActivity;
 import com.thalespf.library.wizardmaker.wizard.internal.shifter.AbstractShifter;
 import com.thalespf.library.wizardmaker.wizard.internal.shifter.ShifterFactory;
 
 /**
  * @author Thales Ferreira / l.thales.x@gmail.com
  *
  */
 public class WizardContainer implements IWizardContainer, ViewPager.OnPageChangeListener {
 
 	private ViewPager viewPager;
 	private PagerAdapter pagerAdapter;
 	private WizardMakerActivity wizardActivity;
 	private IWizard wizard;
 
 	protected WizardContainerController controller = new WizardContainerController();
 	private AbstractShifter shifter;
 	public ReviewWizardPage reviewPage = null;
 	
 	/**
 	 * @param wizardMakerActivity
 	 * @param wizard
 	 */
 	public WizardContainer(WizardMakerActivity wizardMakerActivity,
 			IWizard wizard) {
 		this.wizardActivity = wizardMakerActivity;
 		this.wizard = wizard;
 		this.wizard.setContainer(this);
 	}
 
 	public void initWizard() {
 		ActionBarSherlock mSherlock = wizardActivity.getActionBarSherlock();
 		
 		LinearLayout ll = (LinearLayout) LayoutInflater.from(wizardActivity).inflate(R.layout.wizard_maker_wizard_container, null);
 		
 		shifter = ShifterFactory.createShifter(wizard.getShifterViewLocation(), controller); 
 		
 		View shifterView = shifter.getShifterView(wizardActivity);
 		
 		if(wizard.getShifterViewLocation() == IWizard.VALUE__SHIFTER_VIEW_LOCATION_ABOVE) {
 			mSherlock.setContentView(ll);
 			mSherlock.getActionBar().setCustomView(shifterView);
 			mSherlock.getActionBar().setDisplayShowCustomEnabled(true);
 		} else if(wizard.getShifterViewLocation() == IWizard.VALUE__SHIFTER_VIEW_LOCATION_BELOW) {
 			ll.addView(shifterView);
 			ll.requestLayout();
 			mSherlock.setContentView(ll);
 		}
 	    
 	    wizard.onCreatePages();
 	    reviewPage = null;
 	    
 	    pagerAdapter = new PagerAdapt(wizardActivity.getSupportFragmentManager());
 	    viewPager = (ViewPager) wizardActivity.findViewById(R.id.pager);
 	    viewPager.setAdapter(pagerAdapter);
 	    
 	    updateShifter();
 	    registerListeners();
 	}
 	
 	/**
 	 * 
 	 */
 	private void registerListeners() {
 		viewPager.setOnPageChangeListener(this);
 	}
 	
 	/**
 	 * A controller to make a common access to the wizard container
 	 * from the UI interaction
 	 * 
 	 * @author Thales Ferreira / l.thales.x@gmail.com
 	 *
 	 */
 	public class WizardContainerController {
 
 		/**
 		 * Do tasks when page is changed. The position is the
 		 * next page or the previous.
 		 * 
 		 * @param position the position of the page to change to
 		 * @param fromSlide TODO
 		 */
 		public void wizardPageChanged(int position, boolean fromSlide) {			
 			//change in case of new position
 			if(position != viewPager.getCurrentItem() || fromSlide) {
 				Log.i("WizardContainer", "change to " + position);
 				pagerAdapter.notifyDataSetChanged();
 				viewPager.setCurrentItem(position);
 				wizard.setCurrentPage(position);
 				updateShifter();
 			}
 		}
 
 		/**
 		 * 
 		 */
 		public void onFinishWizard() {
 			Log.i("WizardContainer", "Finishing wizard...");
 			//verify if the wizard canFinish
 			if(wizard.canFinish()) {
 				wizard.performFinish();
 			}
 		}
 
 		/**
 		 * 
 		 */
 		public void wizardPagePreviousChanged() {
 			wizardPageChanged(wizard.getPreviousPage(), false);
 		}
 
 		/**
 		 * 
 		 */
 		public void wizardPageNextChanged() {
 			//verify is the actual page is a branch
 			if(wizard.getPage() instanceof BranchPage) {
 				wizard.breakDownBranchPage();
 			}
 			
 			int nextPage = wizard.getNextPage();
 			
 			//verify if can shift the previous page (actual page)
 			if(nextPage != -1 && !wizard.getPages().get(nextPage - 1).canShiftNextPage()) {
 				//TODO: emit error messages
 				Log.i("WizardMaker:wizardPageNextChanged()", "Page cant shifted");
 				return;
 			}
 			
 			//verify if is last page
 			if(wizard.getPagePosition(wizard.getPage()) == wizard.getPagesSize() - 1) {
 				//verify if is last page then put the review page
 				if(wizard.isReviewPage()) {
 					if(reviewPage == null) {
 						Log.i("WizardMaker:wizardPageNextChanged()", "Putting the review page");
 						reviewPage = new ReviewWizardPage("Review Items");
 						nextPage++;
 						wizard.addPage(reviewPage);
 					}
 					if(wizard.getPage() instanceof ReviewWizardPage) {
 						onFinishWizard();
 					}
 				} else {
 					onFinishWizard();
 				}
 			}
 			
 			wizardPageChanged(nextPage, false);
 		}
 	}
 	
 	public class PagerAdapt extends FragmentStatePagerAdapter {
 
 		private Fragment mPrimaryItem;
 
 		/**
 		 * @param fm
 		 */
 		public PagerAdapt(FragmentManager fm) {
 			super(fm);
 		}
 
 		/** (non-Javadoc)
 		 * @see android.support.v4.app.FragmentStatePagerAdapter#getItem(int)
 		 */
 		@Override
 		public Fragment getItem(int position) {
 			if(position >= 0 && position < wizard.getPagesSize()) {
 				return wizard.getPages().get(position).createFragment();
 			}
 			return null;
 		}
 		
 		/** (non-Javadoc)
 		 * @see android.support.v4.view.PagerAdapter#getItemPosition(java.lang.Object)
 		 */
 		@Override
 		public int getItemPosition(Object object) {
             if (object == mPrimaryItem) {
                 return POSITION_UNCHANGED;
             }
             return POSITION_NONE;
 		}
 		
 		/** (non-Javadoc)
 		 * @see android.support.v4.app.FragmentStatePagerAdapter#setPrimaryItem(android.view.ViewGroup, int, java.lang.Object)
 		 */
 		@Override
 		public void setPrimaryItem(ViewGroup container, int position,
 				Object object) {
 			super.setPrimaryItem(container, position, object);
             mPrimaryItem = (Fragment) object;
 		}
 
 		/** (non-Javadoc)
 		 * @see android.support.v4.view.PagerAdapter#getCount()
 		 */
 		@Override
 		public int getCount() {
 			return wizard.getPagesSize();
 		}
 
 	}
 	
 	/** (non-Javadoc)
 	 * @see com.thalespf.library.wizardmaker.wizard.internal.IWizardContainer#onEditScreenAfterReview(com.thalespf.library.wizardmaker.wizard.IChoiceWizardPage)
 	 */
 	@Override
 	public void onEditScreenAfterReview(int position) {
 		controller.wizardPageChanged(position, false);
 	}
 
 	/**
 	 * Updates the shifter based on the current wizard state based on user
 	 * interaction from shifter and from ViewPager.
 	 * 
 	 * @param position 
 	 * 
 	 */
 	public void updateShifter() {
		if(wizard.getPages().size() > 0) {
			IWizardPage page = wizard.getPages().get(viewPager.getCurrentItem());
			shifter.updateShifter(page, viewPager.getCurrentItem(), wizard.getPagesSize() - 1);
		}
 	}
 	
 	/** (non-Javadoc)
 	 * @see com.thalespf.library.wizardmaker.wizard.internal.IWizardContainer#onValueChoosed()
 	 */
 	@Override
 	public void onValueChoosed() {
 		updateShifter();
 	}
 
 	/** (non-Javadoc)
 	 * @see com.thalespf.library.wizardmaker.wizard.internal.IWizardContainer#getFragmentActivity()
 	 */
 	@Override
 	public FragmentActivity getFragmentActivity() {
 		return wizardActivity;
 	}
 	
 	/** (non-Javadoc)
 	 * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageSelected(int)
 	 */
 	@Override
 	public void onPageSelected(int position) {
 		controller.wizardPageChanged(position, true);
 	}
 
 	/** (non-Javadoc)
 	 * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageScrollStateChanged(int)
 	 */
 	@Override
 	public void onPageScrollStateChanged(int state) {
 	}
 
 	/** (non-Javadoc)
 	 * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageScrolled(int, float, int)
 	 */
 	@Override
 	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
 	}
 	
 	/** (non-Javadoc)
 	 * @see com.thalespf.library.wizardmaker.wizard.internal.IWizardContainer#getContext()
 	 */
 	@Override
 	public Context getContext() {
 		return wizardActivity;
 	}
 
 }
