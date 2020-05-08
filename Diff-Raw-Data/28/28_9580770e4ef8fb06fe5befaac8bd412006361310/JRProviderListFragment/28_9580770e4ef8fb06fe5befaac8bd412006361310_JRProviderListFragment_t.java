 /*
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  *  Copyright (c) 2011, Janrain, Inc.
  *
  *  All rights reserved.
  *
  *  Redistribution and use in source and binary forms, with or without modification,
  *  are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, this
  *    list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation and/or
  *    other materials provided with the distribution.
  *  * Neither the name of the Janrain, Inc. nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  *
  *
  *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
  *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  */
 package com.janrain.android.engage.ui;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.janrain.android.engage.JREngage;
 import com.janrain.android.engage.R;
 import com.janrain.android.engage.session.JRProvider;
 import com.janrain.android.engage.session.JRSession;
 
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 /**
  * @internal
  *
  * @class JRProvidersActivity
  */
 public class JRProviderListFragment extends JRUiFragment {
     public static final int RESULT_FAIL = Activity.RESULT_FIRST_USER;
     public static final int RESULT_NATIVE_SIGNIN = Activity.RESULT_FIRST_USER + 1;
     
     private static final int TIMER_MAX_ITERATIONS = 40;
 
     private ArrayList<JRProvider> mProviderList;
     private ProviderAdapter mAdapter;
     private Timer mTimer;
     private int mTimerCount = 0;
 
     private ListView mListView;
     private TextView mEmptyTextLabel;
     private ProgressBar mLoadingProgress;
 
     /**
      * @internal
      *
      * @class ProviderAdapter
      * Array adapter used to render individual providers in list view.
      **/
     private class ProviderAdapter extends ArrayAdapter<JRProvider> {
        LayoutInflater mLayoutInflater =
                (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
         public ProviderAdapter() {
             // The super class only ends up using the last parameter passed into this super constructor,
             // the List.  The first two parameters are never used.
 
            super(JREngage.getActivity(), R.layout.jr_provider_listview_row, mProviderList);
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             if (convertView == null) {
                 // This line is pretty much copied from the super implementation, which doesn't attach
                 // the newly inflated View to it's parent. I don't know why.
                convertView = mLayoutInflater.inflate(R.layout.jr_provider_listview_row, parent, false);
             }
 
             ImageView icon = (ImageView) convertView.findViewById(R.id.jr_row_provider_icon);
             TextView label = (TextView) convertView.findViewById(R.id.jr_row_provider_label);
 
             JRProvider provider = getItem(position);
 
             Drawable providerIcon = provider.getProviderIcon(getContext());
             icon.setImageDrawable(providerIcon);
             label.setText(provider.getFriendlyName());
 
             return convertView;
         }
     }
 
     private Runnable mNoProvidersFoundRunner = new Runnable() {
         public void run() {
             mEmptyTextLabel.setVisibility(View.VISIBLE);
             mLoadingProgress.setVisibility(View.GONE);
 
             Toast.makeText(getActivity(), "No providers found.", Toast.LENGTH_LONG).show();
         }
     };
 
     private Runnable mProvidersLoadedRunner = new Runnable() {
         public void run() {
             mListView.setVisibility(View.VISIBLE);
             mLoadingProgress.setVisibility(View.GONE);
 
             for (JRProvider p : mProviderList) mAdapter.add(p);
             mAdapter.notifyDataSetChanged();
             maybeShowHideTaglines();
         }
     };
 
     public JRProviderListFragment() {}
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         JREngage.logd(TAG, "[onCreateView]");
         if (mSession == null) return null;
         View inflatedLayout = inflater.inflate(R.layout.jr_provider_listview, container, false);
 
         mListView = (ListView) inflatedLayout.findViewById(android.R.id.list);
         mEmptyTextLabel = (TextView) inflatedLayout.findViewById(R.id.jr_empty_label);
         mLoadingProgress = (ProgressBar) inflatedLayout.findViewById(android.R.id.empty);
 
         JRCustomUiConfiguration customUiConfiguration = getCustomUiConfiguration();
         if (customUiConfiguration != null) {
             if (customUiConfiguration.mProviderListHeader != null) {
                 doCustomViewCreate(customUiConfiguration.mProviderListHeader, inflater, savedInstanceState,
                         mListView);
                 mListView.addHeaderView(customUiConfiguration.mProviderListHeader.getView());
             }
 
             if (customUiConfiguration.mProviderListFooter != null) {
                 doCustomViewCreate(customUiConfiguration.mProviderListFooter, inflater, savedInstanceState,
                         mListView);
                 mListView.addFooterView(customUiConfiguration.mProviderListFooter.getView());
             }
 
             if (customUiConfiguration.mAuthenticationBackgroundColor != null) {
                 mListView.setBackgroundColor(customUiConfiguration.mAuthenticationBackgroundColor);
                 mListView.setCacheColorHint(customUiConfiguration.mAuthenticationBackgroundColor);
             }
 
             if (customUiConfiguration.mAuthenticationBackgroundDrawable != null) {
 //                FrameLayout outerContainer =
 //                        (FrameLayout) inflatedLayout.findViewById(R.id.jr_provider_list_container);
 //                doCustomViewCreate(customUiConfiguration.mAuthenticationBackgroundDrawable, inflater,
 //                        savedInstanceState, outerContainer);
 //                outerContainer.addView(customUiConfiguration.mAuthenticationBackgroundDrawable.getView(), 0);
                 mListView.setBackgroundDrawable(customUiConfiguration.mAuthenticationBackgroundDrawable);
                 mListView.setCacheColorHint(0);
             }
         }
 
         mProviderList = mSession.getAuthProviders();
         if (mProviderList == null) mProviderList = new ArrayList<JRProvider>();
 
         mAdapter = new ProviderAdapter();
         mListView.setAdapter(mAdapter);
         mListView.setOnItemClickListener(itemClickListener);
 
         if (mProviderList.size() == 0) {
             mListView.setVisibility(View.GONE);
             /* Show progress and poll for results */
             mLoadingProgress.setVisibility(View.VISIBLE);
 
             mTimer = new Timer();
             mTimer.schedule(new TimerTask() {
                 @Override
                 public void run() {
                     doSessionPoll();
                 }
             }, 0, 500);
         }
 
         return inflatedLayout;
     }
 
     @Override
     public void onDestroy() {
         if (mTimer != null) mTimer.cancel();
 
         super.onDestroy();
     }
 
     /**
      * This method will be called when an item in the list is selected.
      */
     private ListView.OnItemClickListener itemClickListener = new ListView.OnItemClickListener() {
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
             JRProvider provider = mAdapter.getItem(position);
             mSession.setCurrentlyAuthenticatingProvider(provider);
 
             if (provider.requiresInput() ||
                     (mSession.getAuthenticatedUserForProvider(provider) != null &&
                     !provider.getForceReauth()) && !mSession.getAlwaysForceReauth()) {
                 showUserLanding();
             } else {
                 showWebView();
             }
         }
     };
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         JREngage.logd(TAG, "requestCode: " + requestCode + " resultCode: " + resultCode);
         switch (requestCode) {
             case JRUiFragment.REQUEST_LANDING:
                 switch (resultCode) {
                     case JRLandingFragment.RESULT_RESTART:
                     case JRLandingFragment.RESULT_SWITCH_ACCOUNTS:
                         break;
                     case Activity.RESULT_OK:
                         finishFragmentWithResult(Activity.RESULT_OK);
                         return;
                     case JRLandingFragment.RESULT_FAIL:
                         getActivity().setResult(RESULT_FAIL);
                         getActivity().finish();
                         return;
 //                    I've seen RESULT_CANCELED before, but I can't figure out what could cause it
 //                    Maybe pressing the back button before the Activity is fully displayed?
 //                    case Activity.RESULT_CANCELED:
                     default:
                         Log.e(TAG, "Unrecognized request/result code " + requestCode + "/" + resultCode);
                 }
                 break;
             case JRUiFragment.REQUEST_WEBVIEW:
                 switch (resultCode) {
                     case Activity.RESULT_OK:
                         finishFragmentWithResult(Activity.RESULT_OK);
                         return;
                     case JRWebViewFragment.RESULT_FAIL:
                         finishFragmentWithResult(RESULT_FAIL);
                         return;
                     case JRWebViewFragment.RESULT_RESTART:
                         break;
                     case JRWebViewFragment.RESULT_BAD_OPENID_URL:
                         Log.e(TAG, "Unexpected RESULT_BAD_OPENID_URL from JRWebView");
                         break;
 //                    See RESULT_CANCELED case above.
 //                    case Activity.RESULT_CANCELED:
                     default:
                         Log.e(TAG, "Unrecognized request/result code " + requestCode + "/" + resultCode);
                 }
                 break;
             default:
                 Log.e(TAG, "Unrecognized request/result code " + requestCode + "/" + resultCode);
         }
 
 //        See the comment about specific provider flow in JRFragmentHostActivity#onCreate
         if (isSpecificProviderFlow()) {
             // reach this point when we haven't returned above after setting result and finishing
 //            if (requestCode == JRUiFragment.REQUEST_LANDING
 //                    && resultCode == JRLandingFragment.RESULT_SWITCH_ACCOUNTS) {
 //                showWebView();
 //            } else {
                 cancelProviderList();
 //            }
         }
     }
 
     private void doSessionPoll() {
         ++mTimerCount;
         JREngage.logd(TAG, "[doSessionPoll] timer count: " + mTimerCount);
 
         if (mTimerCount > TIMER_MAX_ITERATIONS) {
             mTimer.cancel();
             getActivity().runOnUiThread(mNoProvidersFoundRunner);
             Log.w(TAG, "[doSessionPoll] providers not found, max iterations hit, timer cancelled...");
         } else {
             ArrayList<JRProvider> providers = mSession.getAuthProviders();
             if (providers.size() > 0) {
                 mProviderList = providers;
                 getActivity().runOnUiThread(mProvidersLoadedRunner);
                 mTimer.cancel();
                 JREngage.logd(TAG, "[doSessionPoll] providers found, timer cancelled...");
             } else {
                 JREngage.logd(TAG, "[doSessionPoll] no providers yet, will retry soon...");
             }
         }
     }
 
     @Override
     /*package*/ void onBackPressed() {
         cancelProviderList();
     }
 
     private void cancelProviderList() {
         finishFragmentWithResult(Activity.RESULT_CANCELED);
         if (mSession != null) mSession.triggerAuthenticationDidCancel();
     }
 
     /*package*/ static boolean shouldOpenDirectToUserLandingPage(JRSession session) {
         JRProvider returningAuthProvider = session.getProviderByName(session.getReturningAuthProvider());
 
         return (!TextUtils.isEmpty(session.getReturningAuthProvider())
                 && !session.getSkipLandingPage()
                 && returningAuthProvider != null
                 && !session.getAlwaysForceReauth()
                 && !returningAuthProvider.getForceReauth()
                 && session.getAuthProviders().contains(returningAuthProvider)
                 && session.getAuthenticatedUserForProvider(returningAuthProvider) != null
                 && session.getEnabledAuthenticationProviders().contains(session.getReturningAuthProvider()));
 
 //        iOS library conditions which are not applicable to Android because this is only called when the
 //        provider list is already starting
 //                && !socialSharing
 //                && specificProvider != null  
 //                the provider list
     }
     
     /*package*/ void onFragmentHostActivityCreate(JRFragmentHostActivity jrfh, JRSession session) {
         /* check and see whether we should start the landing page */
         /* this is delegated to from JRFragmentHostActivity.onCreate(...) so that the provider list skips */
         /* rendering it's UI */
         String rapName = session.getReturningAuthProvider();
         JRProvider provider = session.getProviderByName(rapName);
 
         if (shouldOpenDirectToUserLandingPage(session)) {
             session.setCurrentlyAuthenticatingProvider(provider);
             Intent i = JRFragmentHostActivity.createUserLandingIntent(jrfh);
             i.putExtra(JR_FRAGMENT_FLOW_MODE, JR_FRAGMENT_FLOW_AUTH);
             jrfh.startActivityForResult(i, JRUiFragment.REQUEST_LANDING);
         }
     }
 
     public void finishJrSignin() {
         getActivity().setResult(JRProviderListFragment.RESULT_NATIVE_SIGNIN);
         getActivity().finish();
     }
     
     public String getCustomTitle() {
         String title = null;
         if (getCustomUiConfiguration() != null) title = getCustomUiConfiguration().mProviderListTitle;
         return title;
     }
 }
