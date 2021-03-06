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
  * limitations under the License.
  */
 
 package com.android.email.activity;
 
 import com.android.email.Email;
 import com.android.email.RefreshManager;
 import com.android.email.Utility;
 
 import android.app.Activity;
 import android.app.ListFragment;
 import android.app.LoaderManager.LoaderCallbacks;
 import android.content.Loader;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 
 import java.security.InvalidParameterException;
 
 /**
  * This fragment presents a list of mailboxes for a given account.  The "API" includes the
  * following elements which must be provided by the host Activity.
  *
  *  - call bindActivityInfo() to provide the account ID and set callbacks
  *  - provide callbacks for onOpen and onRefresh
  *  - pass-through implementations of onCreateContextMenu() and onContextItemSelected() (temporary)
  *
  * TODO Restoring ListView state -- don't do this when changing accounts
  */
 public class MailboxListFragment extends ListFragment implements OnItemClickListener {
     private static final String BUNDLE_KEY_ACCOUNT_ID
             = "MailboxListFragment.state.selected_mailbox_id";
     private static final String BUNDLE_LIST_STATE = "MailboxListFragment.state.listState";
     private static final int LOADER_ID_MAILBOX_LIST = 1;
 
     private long mLastLoadedAccountId = -1;
     private long mAccountId = -1;
     private long mSelectedMailboxId = -1;
 
     // UI Support
     private Activity mActivity;
     private MailboxesAdapter mListAdapter;
     private Callback mCallback = EmptyCallback.INSTANCE;
     private final MyLoaderCallbacks mMyLoaderCallbacks = new MyLoaderCallbacks();
 
     private ListView mListView;
 
     private boolean mOpenRequested;
     private boolean mResumed;
 
     private Utility.ListStateSaver mSavedListState;
 
     /**
      * Callback interface that owning activities must implement
      */
     public interface Callback {
         public void onMailboxSelected(long accountId, long mailboxId);
     }
 
     private static class EmptyCallback implements Callback {
         public static final Callback INSTANCE = new EmptyCallback();
         @Override
         public void onMailboxSelected(long accountId, long mailboxId) {
         }
     }
 
     /**
      * Called to do initial creation of a fragment.  This is called after
      * {@link #onAttach(Activity)} and before {@link #onActivityCreated(Bundle)}.
      */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         if (Email.DEBUG_LIFECYCLE && Email.DEBUG) {
             Log.d(Email.LOG_TAG, "MailboxListFragment onCreate");
         }
         super.onCreate(savedInstanceState);
 
         mActivity = getActivity();
         mListAdapter = new MailboxesAdapter(mActivity);
         if (savedInstanceState != null) {
             restoreInstanceState(savedInstanceState);
         }
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         if (Email.DEBUG_LIFECYCLE && Email.DEBUG) {
             Log.d(Email.LOG_TAG, "MailboxListFragment onActivityCreated");
         }
         super.onActivityCreated(savedInstanceState);
 
         mListView = getListView();
         mListView.setOnItemClickListener(this);
         mListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
         registerForContextMenu(mListView);
     }
 
     public void setCallback(Callback callback) {
         mCallback = (callback == null) ? EmptyCallback.INSTANCE : callback;
     }
 
     /**
      * @param accountId the account we're looking at
      */
     public void openMailboxes(long accountId) {
         if (Email.DEBUG_LIFECYCLE && Email.DEBUG) {
             Log.d(Email.LOG_TAG, "MailboxListFragment openMailboxes");
         }
         if (accountId == -1) {
             throw new InvalidParameterException();
         }
         if (mAccountId == accountId) {
             return;
         }
         mOpenRequested = true;
         mAccountId = accountId;
         if (mResumed) {
             startLoading();
         }
     }
 
     public void setSelectedMailbox(long mailboxId) {
         mSelectedMailboxId = mailboxId;
         if (mResumed) {
             highlightSelectedMailbox();
         }
     }
 
     /**
      * Called when the Fragment is visible to the user.
      */
     @Override
     public void onStart() {
         if (Email.DEBUG_LIFECYCLE && Email.DEBUG) {
             Log.d(Email.LOG_TAG, "MailboxListFragment onStart");
         }
         super.onStart();
     }
 
     /**
      * Called when the fragment is visible to the user and actively running.
      */
     @Override
     public void onResume() {
         if (Email.DEBUG_LIFECYCLE && Email.DEBUG) {
             Log.d(Email.LOG_TAG, "MailboxListFragment onResume");
         }
         super.onResume();
         mResumed = true;
 
         // If we're recovering from the stopped state, we don't have to reload.
         // (when mOpenRequested = false)
         if (mAccountId != -1 && mOpenRequested) {
             startLoading();
         }
     }
 
     @Override
     public void onPause() {
         if (Email.DEBUG_LIFECYCLE && Email.DEBUG) {
             Log.d(Email.LOG_TAG, "MailboxListFragment onPause");
         }
         mResumed = false;
         super.onPause();
         mSavedListState = new Utility.ListStateSaver(getListView());
     }
 
     /**
      * Called when the Fragment is no longer started.
      */
     @Override
     public void onStop() {
         if (Email.DEBUG_LIFECYCLE && Email.DEBUG) {
             Log.d(Email.LOG_TAG, "MailboxListFragment onStop");
         }
         super.onStop();
     }
 
     /**
      * Called when the fragment is no longer in use.
      */
     @Override
     public void onDestroy() {
         if (Email.DEBUG_LIFECYCLE && Email.DEBUG) {
             Log.d(Email.LOG_TAG, "MailboxListFragment onDestroy");
         }
         super.onDestroy();
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         if (Email.DEBUG_LIFECYCLE && Email.DEBUG) {
             Log.d(Email.LOG_TAG, "MailboxListFragment onSaveInstanceState");
         }
         super.onSaveInstanceState(outState);
         outState.putLong(BUNDLE_KEY_ACCOUNT_ID, mSelectedMailboxId);
         outState.putParcelable(BUNDLE_LIST_STATE, new Utility.ListStateSaver(getListView()));
     }
 
     private void restoreInstanceState(Bundle savedInstanceState) {
         mSelectedMailboxId = savedInstanceState.getLong(BUNDLE_KEY_ACCOUNT_ID);
         mSavedListState = savedInstanceState.getParcelable(BUNDLE_LIST_STATE);
     }
 
     private void startLoading() {
         if (Email.DEBUG_LIFECYCLE && Email.DEBUG) {
             Log.d(Email.LOG_TAG, "MailboxListFragment startLoading");
         }
         mOpenRequested = false;
         // Clear the list.  (ListFragment will show the "Loading" animation)
         setListShown(false);
 
         // If we've already loaded for a different account, discard the previous result and
         // start loading again.
         // We don't want to use restartLoader(), because if the Loader is retained, we *do* want to
         // reuse the previous result.
         if ((mLastLoadedAccountId != -1) && (mLastLoadedAccountId != mAccountId)) {
             getLoaderManager().stopLoader(LOADER_ID_MAILBOX_LIST);
         }
         getLoaderManager().initLoader(LOADER_ID_MAILBOX_LIST, null, mMyLoaderCallbacks);
     }
 
     private class MyLoaderCallbacks implements LoaderCallbacks<Cursor> {
         @Override
         public Loader<Cursor> onCreateLoader(int id, Bundle args) {
             if (Email.DEBUG_LIFECYCLE && Email.DEBUG) {
                 Log.d(Email.LOG_TAG, "MailboxListFragment onCreateLoader");
             }
             return MailboxesAdapter.createLoader(getActivity(), mAccountId,
                     MailboxesAdapter.MODE_NORMAL);
         }
 
         @Override
         public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
             if (Email.DEBUG_LIFECYCLE && Email.DEBUG) {
                 Log.d(Email.LOG_TAG, "MailboxListFragment onLoadFinished");
             }
             mLastLoadedAccountId = mAccountId;
 
             // Save list view state (primarily scroll position)
             final ListView lv = getListView();
             final Utility.ListStateSaver lss;
             if (mSavedListState != null) {
                 lss = mSavedListState;
                 mSavedListState = null;
             } else {
                 lss = new Utility.ListStateSaver(lv);
             }
 
            // Set the adapter.
            mListAdapter.changeCursor(cursor);
            setListAdapter(mListAdapter);
            setListShown(true);
            highlightSelectedMailbox();
 
             // Restore the state
             lss.restore(lv);
         }
     }
 
     public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         mCallback.onMailboxSelected(mAccountId, id);
     }
 
     public void onRefresh() {
         if (mAccountId != -1) {
             RefreshManager.getInstance(getActivity()).refreshMailboxList(mAccountId);
         }
     }
 
     /**
      * Highlight the selected mailbox.
      */
     private void highlightSelectedMailbox() {
         if (mSelectedMailboxId == -1) {
             // No mailbox selected
             mListView.clearChoices();
             return;
         }
         final int count = mListView.getCount();
         for (int i = 0; i < count; i++) {
             if (mListView.getItemIdAtPosition(i) == mSelectedMailboxId) {
                 mListView.setItemChecked(i, true);
                 break;
             }
         }
     }
 }
