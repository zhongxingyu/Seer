 /*
  * Copyright (C) 2012 Gregory S. Meiste  <http://gregmeiste.com>
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
 package com.meiste.greg.ptw;
 
 import java.io.File;
 import java.io.FileOutputStream;
 
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.google.gson.Gson;
 import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
 import com.handmark.pulltorefresh.library.PullToRefreshBase;
 import com.handmark.pulltorefresh.library.PullToRefreshListView;
 import com.meiste.greg.ptw.GAE.GaeListener;
 import com.squareup.otto.Subscribe;
 
 public final class Standings extends TabFragment implements OnRefreshListener<ListView>, GaeListener, DialogInterface.OnClickListener {
     public static final String FILENAME = "standings";
 
     private boolean mSetupNeeded;
     private boolean mChanged = false;
     private boolean mFailedConnect = false;
     private boolean mConnecting = false;
     private boolean mCheckName = false;
     private PullToRefreshListView mPullToRefresh;
     private PlayerAdapter mAdapter;
     private TextView mAfterRace;
     private TextView mFooter;
    private long mAccountSetupTime = 0;
 
     private PrivacyDialog mDialog;
     private boolean mDialogResume = false;
 
     public static Standings newInstance(final Context context) {
         final Standings fragment = new Standings();
         fragment.setTitle(context.getString(R.string.tab_standings));
 
         return fragment;
     }
 
     @Override
     public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
         mSetupNeeded = GAE.isAccountSetupNeeded(getActivity());
         mChanged = false;
        mAccountSetupTime = Util.getAccountSetupTime(getActivity());
         setRetainInstance(true);
         BusProvider.getInstance().register(this);
 
         if (mSetupNeeded)
             return Util.getAccountSetupView(getActivity(), inflater, container);
         else if (mFailedConnect) {
             mFailedConnect = false;
             final View v = inflater.inflate(R.layout.no_connection, container, false);
 
             final Button retry = (Button) v.findViewById(R.id.retry);
             retry.setOnClickListener(new View.OnClickListener() {
                 public void onClick(final View v) {
                     mChanged = true;
                     notifyChanged();
                 }
             });
 
             return v;
         } else if (!isStandingsPresent() || mConnecting) {
             if (!mConnecting) {
                 mConnecting = true;
                 GAE.getInstance(getActivity()).getPage(this, "standings");
             }
             return inflater.inflate(R.layout.connecting, container, false);
         }
 
         final View v = inflater.inflate(R.layout.standings, container, false);
         mPullToRefresh = (PullToRefreshListView) v.findViewById(R.id.standings);
         mPullToRefresh.setOnRefreshListener(this);
 
         final ListView lv = mPullToRefresh.getRefreshableView();
         final View header = inflater.inflate(R.layout.standings_header, lv, false);
         mAdapter = new PlayerAdapter(getActivity());
         mAfterRace = (TextView) header.findViewById(R.id.after);
         mAfterRace.setText(getRaceAfterText(getActivity()));
         lv.addHeaderView(header, null, false);
         final View footer = inflater.inflate(R.layout.standings_footer, lv, false);
         mFooter = (TextView) footer.findViewById(R.id.standings_footer);
         mFooter.setText(getFooterText(getActivity()));
         lv.addFooterView(footer, null, false);
         lv.setAdapter(mAdapter);
 
         lv.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(final AdapterView<?> parent, final View v, final int pos, final long id) {
                 Util.log("Starting privacy dialog: id=" + id);
                 if (mDialog == null) {
                     mDialog = new PrivacyDialog(getActivity(), Standings.this);
                 }
                 mDialog.show(mAdapter.getPlayerName());
             }
         });
 
         if (mCheckName) {
             if ((mDialog.getNewName() != null) &&
                     !mDialog.getNewName().equals(mAdapter.getPlayerName())) {
                 Toast.makeText(getActivity(), R.string.name_taken, Toast.LENGTH_SHORT).show();
             }
             mCheckName = false;
         }
 
         return v;
     }
 
     @Override
     public void onResume() {
         super.onResume();
 
         if ((mSetupNeeded != GAE.isAccountSetupNeeded(getActivity())) ||
                (mAccountSetupTime != Util.getAccountSetupTime(getActivity()))) {
             Util.log("Standings: onResume: notifyChanged");
             notifyChanged();
         } else if ((mDialog != null) && mDialogResume) {
             mDialog.show();
             mDialogResume = false;
         }
     }
 
     @Override
     public void onPause() {
         super.onPause();
 
         if ((mDialog != null) && mDialog.isShowing()) {
             mDialog.dismiss();
             mDialogResume = true;
         }
     }
 
     @Override
     public void onDestroyView() {
         BusProvider.getInstance().unregister(this);
         super.onDestroyView();
     }
 
     @Override
     public boolean isChanged() {
         // Must check for account status change or account setup separately in
         // case another tab noticed the change and already called notifyChanged().
         return mChanged || (mSetupNeeded != GAE.isAccountSetupNeeded(getActivity())) ||
                (mAccountSetupTime != Util.getAccountSetupTime(getActivity()));
     }
 
     private boolean isStandingsPresent() {
         return new File(getActivity().getFilesDir(), FILENAME).exists();
     }
 
     private String getRaceAfterText(final Context context) {
         if (mAdapter.getRaceAfterNum() > 0)
             return context.getString(R.string.standings_after, mAdapter.getRaceAfterName());
 
         return context.getString(R.string.standings_preseason);
     }
 
     private String getFooterText(final Context context) {
         final int topX = Math.max(mAdapter.getCountWithoutPlayer(), 25);
         return context.getString(R.string.standings_footer, topX);
     }
 
     @Override
     public void onRefresh(final PullToRefreshBase<ListView> refreshView) {
         GAE.getInstance(getActivity()).getPage(this, "standings");
     }
 
     @Subscribe
     public void onStandingsUpdate(final StandingsUpdateEvent event) {
         Util.log("Standings: onStandingsUpdate");
         if (mAdapter != null) {
             mAdapter.notifyDataSetChanged();
             mAfterRace.setText(getRaceAfterText(getActivity()));
             mFooter.setText(getFooterText(getActivity()));
         }
     }
 
     @Override
     public void onFailedConnect(final Context context) {
         Util.log("Standings: onFailedConnect");
 
         // mConnecting not set for pull to refresh case
         if (!mConnecting) {
             mPullToRefresh.onRefreshComplete();
             Toast.makeText(context, R.string.failed_connect, Toast.LENGTH_SHORT).show();
         }
         // Verify application wasn't closed before callback returned
         else if (getActivity() != null) {
             mConnecting = false;
             mFailedConnect = mChanged = true;
             notifyChanged();
         }
     }
 
     @Override
     public void onGet(final Context context, final String json) {
         Util.log("Standings: onGet");
         update(context, json);
 
         // mConnecting not set for pull to refresh case
         if (!mConnecting) {
             mPullToRefresh.onRefreshComplete();
             BusProvider.getInstance().post(new StandingsUpdateEvent());
         }
         // Verify application wasn't closed before callback returned
         else if (getActivity() != null) {
             mConnecting = false;
             mChanged = true;
             notifyChanged();
         }
     }
 
     @Override
     public void onLaunchIntent(final Intent launch) {}
 
     @Override
     public void onConnectSuccess(final Context context, final String json) {
         Util.log("Standings: onConnectSuccess");
 
         mCheckName = true;
         onGet(context, json);
     }
 
     @Override
     public void onClick(final DialogInterface dialog, final int which) {
         if (which == DialogInterface.BUTTON_POSITIVE) {
             final String json = new Gson().toJson(mDialog.getNewName());
             mConnecting = mChanged = true;
             notifyChanged();
             GAE.getInstance(getActivity()).postPage(this, "standings", json);
         }
     }
 
     public static void update(final Context context, final String json) {
         try {
             final FileOutputStream fos = context.openFileOutput(FILENAME, Context.MODE_PRIVATE);
             fos.write(json.getBytes());
             fos.close();
         } catch (final Exception e) {
             Util.log("Failed to save new standings");
         }
     }
 }
