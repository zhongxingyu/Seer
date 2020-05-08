 /*
  * Copyright (C) 2011  Southern Storm Software, Pty Ltd.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.southernstorm.tvguide;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.GridView;
 import android.widget.Toast;
 
 public class TVGuideActivity extends Activity implements TvNetworkListener, TvBookmarkChangedListener {
 
     private GridView channelListView;
     private TvChannelListAdapter channelListAdapter;
     private TvRegionListAdapter regionListAdapter;
     private ProgressDialog progressDialog;
     private ClearChannelHandler clearChannelHandler;
 
     private class ClearChannelHandler extends Handler {
         private String clearId;
         
         @Override
         public void handleMessage(Message msg) {
             String last = TvChannelCache.getInstance().getLastSelectedChannel();
             if (last != null && last.equals(clearId)) {
                 TvChannelCache.getInstance().setLastSelectedChannel(null);
                 channelListAdapter.forceUpdate();
             }
         }
 
         public void clearAfterDelay(long ms) {
             clearId = TvChannelCache.getInstance().getLastSelectedChannel();
             this.removeMessages(0);
             sendMessageDelayed(obtainMessage(0), ms);
         }
     };
     
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.channel_list);
 
         clearChannelHandler = new ClearChannelHandler();
         
         channelListView = (GridView)findViewById(R.id.channelList);
         channelListAdapter = new TvChannelListAdapter(this);
         
         regionListAdapter = new TvRegionListAdapter(this);
 
         channelListView.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                 if (parent.getAdapter() instanceof TvChannelListAdapter) {
                     TvChannel channel = channelListAdapter.getChannel(position);
                     selectChannel(channel);
                 } else if (parent.getAdapter() instanceof TvRegionListAdapter) {
                     String regionId = regionListAdapter.getRegionId(position);
                     selectRegion(regionId);
                 }
             }
         });
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         channelListAdapter.attach();
         TvChannelCache.getInstance().addNetworkListener(this);
         TvChannelCache.getInstance().addContext(this, true);
 
         TvBookmarkManager.getInstance().addContext(this);
         TvBookmarkManager.getInstance().addChangedListener(this);
 
         // If no region selected yet, then populate the initial channel list with regions.
         if (channelListAdapter.getCount() == 0)
             channelListView.setAdapter(regionListAdapter);
         else
             channelListView.setAdapter(channelListAdapter);
 
         if (TvChannelCache.getInstance().getLastSelectedChannel() != null)
             clearChannelHandler.clearAfterDelay(3000);
     }
 
     @Override
     protected void onStop() {
         TvChannelCache.getInstance().removeContext(this);
         TvChannelCache.getInstance().removeNetworkListener(this);
         channelListAdapter.detach();
         TvBookmarkManager.getInstance().removeContext(this);
         TvBookmarkManager.getInstance().removeChangedListener(this);
         if (progressDialog != null) {
             progressDialog.dismiss();
             progressDialog = null;
         }
         super.onStop();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
     }
 
     @Override
     protected void onPause() {
         if (progressDialog != null) {
             progressDialog.dismiss();
             progressDialog = null;
         }
         super.onPause();
     }
 
     @Override
     public void onBackPressed() {
         if (channelListView.getAdapter() == regionListAdapter) {
             // Currently showing the region list.  If we already have a region then
             // assume that the user is pressing Back from "Change Region" and just
             // return to the channel list.
             if (channelListAdapter.getCount() != 0) {
                 channelListView.setAdapter(channelListAdapter);
                 return;
             }
         }
         TvChannelCache.getInstance().setLastSelectedChannel(null);
         super.onBackPressed();
     }
 
     /**
      * Select a specific channel and display the programmes for today.
      * 
      * @param channel the channel that was selected
      */
     private void selectChannel(TvChannel channel) {
         Calendar date = new GregorianCalendar();
         Intent intent = new Intent(this, TvProgrammeListActivity.class);
         intent.putExtra("date_year", date.get(Calendar.YEAR));
         intent.putExtra("date_month", date.get(Calendar.MONTH));
         intent.putExtra("date_day", date.get(Calendar.DAY_OF_MONTH));
         intent.putExtra("channel", channel.toBundle());
         startActivity(intent);
     }
     
     /**
      * Select a specific region and switch to the channels for that region.
      * 
      * @param regionId the region identifier
      */
     private void selectRegion(String regionId) {
         TvChannelCache.getInstance().setRegion(regionId);
         channelListView.setAdapter(channelListAdapter);
 
         SharedPreferences.Editor editor = getPreferences(0).edit();
         editor.putString("region", regionId);
         editor.commit();
     }
     
     private static final int ITEM_CHANGE_REGION = 1;
     private static final int ITEM_ORGANIZE_BOOKMARKS = 2;
     private static final int ITEM_BULK_DOWNLOAD = 3;
     private static final int ITEM_CLEAR_CACHE = 4;
     private static final int ITEM_ADD_REMOVE_CHANNELS = 5;
 
     private static final int DIALOG_BULK_DOWNLOAD = 1;
     private static final int DIALOG_ADD_REMOVE_CHANNELS = 2;
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         if (channelListView.getAdapter() == channelListAdapter) {
             menu.add(ITEM_CHANGE_REGION, 0, 0, "Change Region");
             menu.add(ITEM_ADD_REMOVE_CHANNELS, 0, 0, "Add/Remove Channels");
         }
         menu.add(ITEM_ORGANIZE_BOOKMARKS, 0, 0, "Organize Bookmarks");
         if (TvChannelCache.getInstance().isNetworkingAvailable())
             menu.add(ITEM_BULK_DOWNLOAD, 0, 0, "Bulk Download");
         menu.add(ITEM_CLEAR_CACHE, 0, 0, "Clear Cache");
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getGroupId() == ITEM_CHANGE_REGION) {
             channelListView.setAdapter(regionListAdapter);
             return true;
         } else if (item.getGroupId() == ITEM_ORGANIZE_BOOKMARKS) {
             Intent intent = new Intent(this, TvBookmarkListActivity.class);
             startActivity(intent);
             return true;
         } else if (item.getGroupId() == ITEM_BULK_DOWNLOAD) {
             showDialog(DIALOG_BULK_DOWNLOAD);
         } else if (item.getGroupId() == ITEM_CLEAR_CACHE) {
             TvChannelCache.getInstance().clear();
         } else if (item.getGroupId() == ITEM_ADD_REMOVE_CHANNELS) {
             removeDialog(DIALOG_ADD_REMOVE_CHANNELS);
             showDialog(DIALOG_ADD_REMOVE_CHANNELS);
         }
         return super.onOptionsItemSelected(item);
     }
 
     private static final String[] bulkDownloadItems = {"1 day", "2 days", "3 days", "4 days", "5 days"};
     
     @Override
     public Dialog onCreateDialog(int id, Bundle bundle) {
         AlertDialog.Builder builder;
         switch (id) {
         case DIALOG_BULK_DOWNLOAD:
             final Context context = this;
             builder = new AlertDialog.Builder(this);
             builder.setTitle("Bulk Download");
             builder.setItems(bulkDownloadItems, new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface dialog, int item) {
                     // Extra day for 12:00am to 6:00am
                     if (!TvChannelCache.getInstance().bulkFetch(item + 2)) {
                         Toast toast = Toast.makeText(context, "Cache is up to date", Toast.LENGTH_SHORT);
                         toast.show();
                     }
                     dialog.dismiss();
                 }
             });
             return builder.create();
         case DIALOG_ADD_REMOVE_CHANNELS:
             builder = new AlertDialog.Builder(this);
             builder.setTitle("Add/Remove Channels");
             final List<TvChannel> allChannels = TvChannelCache.getInstance().getAllChannelsInRegion();
             String[] names = new String [allChannels.size()];
             boolean[] checked = new boolean [allChannels.size()];
             for (int index = 0; index < allChannels.size(); ++index) {
                 TvChannel channel = allChannels.get(index);
                 names[index] = channel.getName();
                 if (channel.getNumbers() != null)
                     names[index] += "\n" + channel.getNumbers();
                 checked[index] = channel.getHiddenState() != TvChannel.HIDDEN;
             }
             builder.setMultiChoiceItems(names, checked, new DialogInterface.OnMultiChoiceClickListener() {
                 public void onClick(DialogInterface dialog, int item, boolean isChecked) {
                     TvChannel channel = allChannels.get(item);
                     if (isChecked) {
                         if (channel.getRegion() != null)
                             channel.setHiddenState(TvChannel.HIDDEN_BY_REGION);
                         else
                             channel.setHiddenState(TvChannel.NOT_HIDDEN);
                     } else {
                         channel.setHiddenState(TvChannel.HIDDEN);
                     }
                 }
             });
             AlertDialog dialog = builder.create();
             dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                 public void onDismiss(DialogInterface dialog) {
                     TvChannelCache.getInstance().saveChannelHiddenStates();
                     TvChannelCache.getInstance().loadChannels();
                 }
             });
             return dialog;
         default: break;
         }
         return null;
     }
 
     public void setCurrentNetworkRequest(TvChannel channel, Calendar date, Calendar primaryDate) {
         String message = channel.getName(); // + " " + DateFormat.format("E, MMM dd", primaryDate);
         if (progressDialog == null) {
             progressDialog = ProgressDialog.show
                 (this, "Fetching guide data", message, true);
         } else {
             progressDialog.setMessage(message);
             progressDialog.show();
         }
     }
 
     public void setCurrentNetworkIconRequest(TvChannel channel) {
         String message = channel.getName();
         if (progressDialog == null) {
             progressDialog = ProgressDialog.show
                 (this, "Fetching channel icon", message, true);
         } else {
             progressDialog.setMessage(message);
             progressDialog.show();
         }
     }
 
     public void setCurrentNetworkListRequest() {
         if (progressDialog == null) {
             progressDialog = ProgressDialog.show
                 (this, "Fetching channel list", "", true);
         } else {
             progressDialog.setMessage("");
             progressDialog.show();
         }
     }
 
     public void endNetworkRequests() {
         if (progressDialog != null)
             progressDialog.hide();
     }
 
     public void dataAvailable(TvChannel channel, Calendar date, Calendar primaryDate) {
     }
 
     public void requestFailed(TvChannel channel, Calendar date, Calendar primaryDate) {
     }
     
     public void bookmarksChanged() {
         channelListAdapter.forceUpdate();
     }
 }
