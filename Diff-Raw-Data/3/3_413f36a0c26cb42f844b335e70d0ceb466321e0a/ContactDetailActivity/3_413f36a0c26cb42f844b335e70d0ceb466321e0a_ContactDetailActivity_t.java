 /*
  * Copyright (C) 2010 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License
  */
 
 package com.android.contacts.activities;
 
 import com.android.contacts.ContactLoader;
 import com.android.contacts.ContactSaveService;
 import com.android.contacts.ContactsActivity;
 import com.android.contacts.R;
 import com.android.contacts.detail.ContactDetailDisplayUtils;
 import com.android.contacts.detail.ContactDetailFragment;
 import com.android.contacts.detail.ContactDetailFragmentCarousel;
 import com.android.contacts.detail.ContactDetailTabCarousel;
 import com.android.contacts.detail.ContactDetailUpdatesFragment;
 import com.android.contacts.detail.ContactDetailViewPagerAdapter;
 import com.android.contacts.detail.ContactLoaderFragment;
 import com.android.contacts.detail.ContactLoaderFragment.ContactLoaderFragmentListener;
 import com.android.contacts.detail.TabCarouselScrollManager;
 import com.android.contacts.interactions.ContactDeletionInteraction;
 import com.android.contacts.model.AccountWithDataSet;
 import com.android.contacts.util.PhoneCapabilityTester;
 
 import android.app.ActionBar;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.content.ActivityNotFoundException;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v13.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.CheckBox;
 import android.widget.Toast;
 
 import java.util.ArrayList;
 
 // TODO: Use {@link ContactDetailLayoutController} so there isn't duplicated code
 public class ContactDetailActivity extends ContactsActivity {
     private static final String TAG = "ContactDetailActivity";
 
     /**
      * Intent key for a boolean that specifies whether the "up" afforance in this activity should
      * behave as default (return user back to {@link PeopleActivity}) or whether the activity should
      * instead be finished.
      */
     public static final String INTENT_KEY_IGNORE_DEFAULT_UP_BEHAVIOR = "ignoreDefaultUpBehavior";
 
     private static final String KEY_CONTACT_HAS_UPDATES = "contactHasUpdates";
     private static final String KEY_CURRENT_PAGE_INDEX = "currentPageIndex";
 
     public static final int FRAGMENT_COUNT = 2;
 
     private ContactLoader.Result mContactData;
     private Uri mLookupUri;
     private boolean mIgnoreDefaultUpBehavior;
 
     private ContactLoaderFragment mLoaderFragment;
     private ContactDetailFragment mDetailFragment;
     private ContactDetailUpdatesFragment mUpdatesFragment;
 
     private ContactDetailTabCarousel mTabCarousel;
     private ViewPager mViewPager;
 
     private ContactDetailFragmentCarousel mFragmentCarousel;
 
     private boolean mFragmentsAddedToFragmentManager;
 
     private ViewGroup mRootView;
     private ViewGroup mContentView;
     private LayoutInflater mInflater;
 
     private Handler mHandler = new Handler();
 
     /**
      * Whether or not the contact has updates, which dictates whether the
      * {@link ContactDetailUpdatesFragment} will be shown.
      */
     private boolean mContactHasUpdates;
 
     @Override
     public void onCreate(Bundle savedState) {
         super.onCreate(savedState);
         if (PhoneCapabilityTester.isUsingTwoPanes(this)) {
             // This activity must not be shown. We have to select the contact in the
             // PeopleActivity instead ==> Create a forward intent and finish
             final Intent originalIntent = getIntent();
             Intent intent = new Intent();
             intent.setAction(originalIntent.getAction());
             intent.setDataAndType(originalIntent.getData(), originalIntent.getType());
             intent.setFlags(
                     Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_FORWARD_RESULT
                             | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
 
             intent.setClass(this, PeopleActivity.class);
             startActivity(intent);
             finish();
             return;
         }
 
         mIgnoreDefaultUpBehavior = getIntent().getBooleanExtra(
                 INTENT_KEY_IGNORE_DEFAULT_UP_BEHAVIOR, false);
 
         setContentView(R.layout.contact_detail_activity);
         mRootView = (ViewGroup) findViewById(R.id.contact_detail_view);
         mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
         FragmentManager fragmentManager = getFragmentManager();
         mDetailFragment = (ContactDetailFragment)
                 fragmentManager.findFragmentByTag(
                 ContactDetailViewPagerAdapter.ABOUT_FRAGMENT_TAG);
         mUpdatesFragment = (ContactDetailUpdatesFragment)
                 fragmentManager.findFragmentByTag(
                 ContactDetailViewPagerAdapter.UPDTES_FRAGMENT_TAG);
 
         // If the fragment were found in the {@link FragmentManager} then we don't need to add
         // it again.
         if (mDetailFragment != null) {
             mFragmentsAddedToFragmentManager = true;
         } else {
             // Otherwise, create the fragments dynamically and remember to add them to the
             // {@link FragmentManager}.
             mDetailFragment = new ContactDetailFragment();
             mUpdatesFragment = new ContactDetailUpdatesFragment();
             mFragmentsAddedToFragmentManager = false;
         }
 
         mDetailFragment.setListener(mFragmentListener);
         mDetailFragment.setData(mLookupUri, mContactData);
         mUpdatesFragment.setData(mLookupUri, mContactData);
 
         if (savedState != null) {
             mContactHasUpdates = savedState.getBoolean(KEY_CONTACT_HAS_UPDATES);
             if (mContactHasUpdates) {
                 setupContactWithUpdates(savedState.getInt(KEY_CURRENT_PAGE_INDEX, 0));
             } else {
                 setupContactWithoutUpdates();
             }
         }
 
         // We want the UP affordance but no app icon.
         // Setting HOME_AS_UP, SHOW_TITLE and clearing SHOW_HOME does the trick.
         ActionBar actionBar = getActionBar();
         if (actionBar != null) {
             actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE,
                     ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_TITLE
                     | ActionBar.DISPLAY_SHOW_HOME);
             actionBar.setTitle("");
         }
 
         Log.i(TAG, getIntent().getData().toString());
     }
 
     @Override
     public void onAttachFragment(Fragment fragment) {
          if (fragment instanceof ContactLoaderFragment) {
             mLoaderFragment = (ContactLoaderFragment) fragment;
             mLoaderFragment.setRetainInstance(true);
             mLoaderFragment.setListener(mLoaderFragmentListener);
             mLoaderFragment.loadUri(getIntent().getData());
         }
     }
 
     @Override
     public boolean onSearchRequested() {
         return true; // Don't respond to the search key.
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.star, menu);
         return true;
     }
 
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
         MenuItem starredMenuItem = menu.findItem(R.id.menu_star);
         ViewGroup starredContainer = (ViewGroup) getLayoutInflater().inflate(
                 R.layout.favorites_star, null, false);
         final CheckBox starredView = (CheckBox) starredContainer.findViewById(R.id.star);
         starredView.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 // Toggle "starred" state
                 // Make sure there is a contact
                 if (mLookupUri != null) {
                     Intent intent = ContactSaveService.createSetStarredIntent(
                             ContactDetailActivity.this, mLookupUri, starredView.isChecked());
                     ContactDetailActivity.this.startService(intent);
                 }
             }
         });
         // If there is contact data, update the starred state
         if (mContactData != null) {
             ContactDetailDisplayUtils.setStarred(mContactData, starredView);
         }
         starredMenuItem.setActionView(starredContainer);
         return true;
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         // First check if the {@link ContactLoaderFragment} can handle the key
         if (mLoaderFragment != null && mLoaderFragment.handleKeyDown(keyCode)) return true;
 
         // Otherwise find the correct fragment to handle the event
         FragmentKeyListener mCurrentFragment;
         switch (getCurrentPage()) {
             case 0:
                 mCurrentFragment = mDetailFragment;
                 break;
             case 1:
                 mCurrentFragment = mUpdatesFragment;
                 break;
             default:
                 throw new IllegalStateException("Invalid current item for ViewPager");
         }
         if (mCurrentFragment != null && mCurrentFragment.handleKeyDown(keyCode)) return true;
 
         // In the last case, give the key event to the superclass.
         return super.onKeyDown(keyCode, event);
     }
 
     private int getCurrentPage() {
         // If the contact doesn't have any social updates, there is only 1 page (detail fragment).
         if (!mContactHasUpdates) {
             return 0;
         }
         // Otherwise find the current page based on the {@link ViewPager} or fragment carousel.
         if (mViewPager != null) {
             return mViewPager.getCurrentItem();
         } else if (mFragmentCarousel != null) {
             return mFragmentCarousel.getCurrentPage();
         }
         throw new IllegalStateException("Can't figure out the currently selected page. If the " +
                 "contact has social updates, there must be a ViewPager or fragment carousel");
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putBoolean(KEY_CONTACT_HAS_UPDATES, mContactHasUpdates);
         outState.putInt(KEY_CURRENT_PAGE_INDEX, getCurrentPage());
     }
 
     private final ContactLoaderFragmentListener mLoaderFragmentListener =
             new ContactLoaderFragmentListener() {
         @Override
         public void onContactNotFound() {
             finish();
         }
 
         @Override
         public void onDetailsLoaded(final ContactLoader.Result result) {
             if (result == null) {
                 return;
             }
             // Since {@link FragmentTransaction}s cannot be done in the onLoadFinished() of the
             // {@link LoaderCallbacks}, then post this {@link Runnable} to the {@link Handler}
             // on the main thread to execute later.
             mHandler.post(new Runnable() {
                 @Override
                 public void run() {
                     // If the activity is destroyed (or will be destroyed soon), don't update the UI
                     if (isFinishing()) {
                         return;
                     }
                     mContactData = result;
                     mLookupUri = result.getLookupUri();
                     mContactHasUpdates = !result.getStreamItems().isEmpty();
                     invalidateOptionsMenu();
                     setupTitle();
                     if (mContactHasUpdates) {
                         setupContactWithUpdates(null /* Don't change the current page */);
                     } else {
                         setupContactWithoutUpdates();
                     }
                 }
             });
         }
 
         @Override
         public void onEditRequested(Uri contactLookupUri) {
             startActivity(new Intent(Intent.ACTION_EDIT, contactLookupUri));
             finish();
         }
 
         @Override
         public void onDeleteRequested(Uri contactUri) {
             ContactDeletionInteraction.start(ContactDetailActivity.this, contactUri, true);
         }
     };
 
     /**
      * Setup the activity title and subtitle with contact name and company.
      */
     private void setupTitle() {
         CharSequence displayName = ContactDetailDisplayUtils.getDisplayName(this, mContactData);
         String company =  ContactDetailDisplayUtils.getCompany(this, mContactData);
 
         ActionBar actionBar = getActionBar();
         actionBar.setTitle(displayName);
         actionBar.setSubtitle(company);
     }
 
     /**
      * Setup the layout for the contact with updates. Pass in the index of the current page to
      * select or null if the current selection should be left as is.
      */
     private void setupContactWithUpdates(Integer currentPageIndex) {
         if (mContentView == null) {
             mContentView = (ViewGroup) mInflater.inflate(
                     R.layout.contact_detail_container_with_updates, mRootView, false);
             mRootView.addView(mContentView);
 
             // Make sure all needed views are retrieved. Note that narrow width screens have a
             // {@link ViewPager} and {@link ContactDetailTabCarousel}, while wide width screens have
             // a {@link ContactDetailFragmentCarousel}.
             mTabCarousel = (ContactDetailTabCarousel) findViewById(R.id.tab_carousel);
             if (mTabCarousel != null) {
                 mTabCarousel.setListener(mTabCarouselListener);
                TabCarouselScrollManager.bind(mTabCarousel, mDetailFragment, mUpdatesFragment);
             }
 
             mViewPager = (ViewPager) findViewById(R.id.pager);
             if (mViewPager != null) {
                 // Inflate 2 view containers to pass in as children to the {@link ViewPager},
                 // which will in turn be the parents to the mDetailFragment and mUpdatesFragment
                 // since the fragments must have the same parent view IDs in both landscape and
                 // portrait layouts.
                 ViewGroup detailContainer = (ViewGroup) mInflater.inflate(
                         R.layout.contact_detail_about_fragment_container, mViewPager, false);
                 ViewGroup updatesContainer = (ViewGroup) mInflater.inflate(
                         R.layout.contact_detail_updates_fragment_container, mViewPager, false);
 
                 ContactDetailViewPagerAdapter adapter = new ContactDetailViewPagerAdapter();
                 adapter.setAboutFragmentView(detailContainer);
                 adapter.setUpdatesFragmentView(updatesContainer);
 
                 mViewPager.addView(detailContainer);
                 mViewPager.addView(updatesContainer);
                 mViewPager.setAdapter(adapter);
                 mViewPager.setOnPageChangeListener(mOnPageChangeListener);
 
                 if (!mFragmentsAddedToFragmentManager) {
                     FragmentManager fragmentManager = getFragmentManager();
                     FragmentTransaction transaction = fragmentManager.beginTransaction();
                     transaction.add(R.id.about_fragment_container, mDetailFragment,
                             ContactDetailViewPagerAdapter.ABOUT_FRAGMENT_TAG);
                     transaction.add(R.id.updates_fragment_container, mUpdatesFragment,
                             ContactDetailViewPagerAdapter.UPDTES_FRAGMENT_TAG);
                     transaction.commit();
                     fragmentManager.executePendingTransactions();
                 }
 
                 // Select page if applicable
                 if (currentPageIndex != null) {
                     mViewPager.setCurrentItem(currentPageIndex);
                 }
             }
 
             mFragmentCarousel = (ContactDetailFragmentCarousel)
                     findViewById(R.id.fragment_carousel);
             // Add the fragments to the fragment containers in the carousel using a
             // {@link FragmentTransaction} if they haven't already been added to the
             // {@link FragmentManager}.
             if (mFragmentCarousel != null) {
                 if (!mFragmentsAddedToFragmentManager) {
                     FragmentManager fragmentManager = getFragmentManager();
                     FragmentTransaction transaction = fragmentManager.beginTransaction();
                     transaction.add(R.id.about_fragment_container, mDetailFragment,
                             ContactDetailViewPagerAdapter.ABOUT_FRAGMENT_TAG);
                     transaction.add(R.id.updates_fragment_container, mUpdatesFragment,
                             ContactDetailViewPagerAdapter.UPDTES_FRAGMENT_TAG);
                     transaction.commit();
                     fragmentManager.executePendingTransactions();
                 }
 
                 // Select page if applicable
                 if (currentPageIndex != null) {
                     mFragmentCarousel.setCurrentPage(currentPageIndex);
                 }
             }
         }
 
         // Then reset the contact data to the appropriate views
         if (mTabCarousel != null) {
             mTabCarousel.loadData(mContactData);
         }
         if (mFragmentCarousel != null && mDetailFragment != null && mUpdatesFragment != null) {
             mFragmentCarousel.setFragments(mDetailFragment, mUpdatesFragment);
         }
         if (mDetailFragment != null) {
             mDetailFragment.setData(mLookupUri, mContactData);
         }
         if (mUpdatesFragment != null) {
             mUpdatesFragment.setData(mLookupUri, mContactData);
         }
     }
 
     private void setupContactWithoutUpdates() {
         if (mContentView == null) {
             mContentView = (ViewGroup) mInflater.inflate(
                     R.layout.contact_detail_container_without_updates, mRootView, false);
             mRootView.addView(mContentView);
             mDetailFragment = (ContactDetailFragment) getFragmentManager().findFragmentById(
                     R.id.contact_detail_fragment);
             mDetailFragment.setListener(mFragmentListener);
         }
         // Reset contact data
         if (mDetailFragment != null) {
             mDetailFragment.setData(mLookupUri, mContactData);
         }
     }
 
     private final ContactDetailFragment.Listener mFragmentListener =
             new ContactDetailFragment.Listener() {
         @Override
         public void onItemClicked(Intent intent) {
             try {
                 startActivity(intent);
             } catch (ActivityNotFoundException e) {
                 Log.e(TAG, "No activity found for intent: " + intent);
             }
         }
 
         @Override
         public void onCreateRawContactRequested(
                 ArrayList<ContentValues> values, AccountWithDataSet account) {
             Toast.makeText(ContactDetailActivity.this, R.string.toast_making_personal_copy,
                     Toast.LENGTH_LONG).show();
             Intent serviceIntent = ContactSaveService.createNewRawContactIntent(
                     ContactDetailActivity.this, values, account,
                     ContactDetailActivity.class, Intent.ACTION_VIEW);
             startService(serviceIntent);
 
         }
     };
 
     public class ViewPagerAdapter extends FragmentPagerAdapter{
 
         public ViewPagerAdapter(FragmentManager fm) {
             super(fm);
         }
 
         @Override
         public Fragment getItem(int position) {
             switch (position) {
                 case 0:
                     return new ContactDetailFragment();
                 case 1:
                     return new ContactDetailUpdatesFragment();
             }
             throw new IllegalStateException("No fragment at position " + position);
         }
 
         @Override
         public int getCount() {
             return FRAGMENT_COUNT;
         }
     }
 
     private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {
 
         @Override
         public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
             // The user is horizontally dragging the {@link ViewPager}, so send
             // these scroll changes to the tab carousel. Ignore these events though if the carousel
             // is actually controlling the {@link ViewPager} scrolls because it will already be
             // in the correct position.
             if (mViewPager.isFakeDragging()) {
                 return;
             }
             int x = (int) ((position + positionOffset) *
                     mTabCarousel.getAllowedHorizontalScrollLength());
             mTabCarousel.scrollTo(x, 0);
         }
 
         @Override
         public void onPageSelected(int position) {
             // Since a new page has been selected by the {@link ViewPager},
             // update the tab selection in the carousel.
             mTabCarousel.setCurrentTab(position);
         }
 
         @Override
         public void onPageScrollStateChanged(int state) {}
 
     };
 
     private ContactDetailTabCarousel.Listener mTabCarouselListener =
             new ContactDetailTabCarousel.Listener() {
 
         @Override
         public void onTouchDown() {
             // The user just started scrolling the carousel, so begin "fake dragging" the
             // {@link ViewPager} if it's not already doing so.
             if (mViewPager.isFakeDragging()) {
                 return;
             }
             mViewPager.beginFakeDrag();
         }
 
         @Override
         public void onTouchUp() {
             // The user just stopped scrolling the carousel, so stop "fake dragging" the
             // {@link ViewPager} if was doing so before.
             if (mViewPager.isFakeDragging()) {
                 mViewPager.endFakeDrag();
             }
         }
 
         @Override
         public void onScrollChanged(int l, int t, int oldl, int oldt) {
             // The user is scrolling the carousel, so send the scroll deltas to the
             // {@link ViewPager} so it can move in sync.
             if (mViewPager.isFakeDragging()) {
                 mViewPager.fakeDragBy(oldl-l);
             }
         }
 
         @Override
         public void onTabSelected(int position) {
             // The user selected a tab, so update the {@link ViewPager}
             mViewPager.setCurrentItem(position);
         }
     };
 
     /**
      * This interface should be implemented by {@link Fragment}s within this
      * activity so that the activity can determine whether the currently
      * displayed view is handling the key event or not.
      */
     public interface FragmentKeyListener {
         /**
          * Returns true if the key down event will be handled by the implementing class, or false
          * otherwise.
          */
         public boolean handleKeyDown(int keyCode);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 
         switch (item.getItemId()) {
             case android.R.id.home:
                 if (mIgnoreDefaultUpBehavior) {
                     finish();
                     return true;
                 }
                 Intent intent = new Intent(this, PeopleActivity.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
                 finish();
                 return true;
             default:
                 break;
         }
         return super.onOptionsItemSelected(item);
     }
 }
