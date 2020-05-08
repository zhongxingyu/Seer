 package com.kii.demo.sync.ui.fragments;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.app.Fragment;
 import android.text.TextUtils;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ExpandableListView;
 import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
 import android.widget.ExpandableListView.OnChildClickListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.kii.cloud.sync.BackupService;
 import com.kii.cloud.sync.DownloadManager;
 import com.kii.cloud.sync.KiiSyncClient;
 import com.kii.cloud.sync.SyncNewEventListener;
 import com.kii.demo.sync.R;
 import com.kii.demo.sync.ui.ProgressListActivity;
 import com.kii.demo.sync.ui.SettingsActivity;
 import com.kii.demo.sync.ui.view.KiiFileExpandableListAdapter;
 import com.kii.demo.sync.utils.MimeInfo;
 import com.kii.demo.sync.utils.MimeUtil;
 import com.kii.demo.sync.utils.UiUtils;
 import com.kii.demo.sync.utils.Utils;
 import com.kii.sync.KiiFile;
 import com.kii.sync.SyncMsg;
 
 public class KiiFileFragment extends Fragment {
     private static View mView;
     private NewEventListener mNewEventListener = null;
     private ExpandableListView mList = null;
     KiiFileExpandableListAdapter mAdapter;
 
    final static int MENU_RESTORE_TRASH = 201;
    final static int MENU_MOVE_TRASH = 202;
    final static int MENU_DELETE = 203;
    final static int MENU_DELETE_LOCAL = 204;
    final static int MENU_DOWNLOAD = 205;
    final static int MENU_CANCEL = 206;
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
        if(!(item.getMenuInfo() instanceof ExpandableListContextMenuInfo)) {
            return false;
        }
         ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item
                 .getMenuInfo();
 
         String title = "Menu";
 
         int type = ExpandableListView
                 .getPackedPositionType(info.packedPosition);
         if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
 
             int groupPos = ExpandableListView
                     .getPackedPositionGroup(info.packedPosition);
             int childPos = ExpandableListView
                     .getPackedPositionChild(info.packedPosition);
 
             final KiiFile kFile = (KiiFile) mAdapter.getChild(groupPos,
                     childPos);
             if ((kFile != null) && kFile.isFile()) {
                 final KiiSyncClient client = KiiSyncClient
                         .getInstance(getActivity());
                 if (client == null) {
                     return true;
                 }
                 switch (item.getGroupId()) {
                     case MENU_RESTORE_TRASH:
                         Runnable r = new Runnable() {
                             @Override
                             public void run() {
                                 client.restoreFromTrash(kFile);
                             }
                         };
                         new Thread(r).start();
                         break;
                     case MENU_MOVE_TRASH:
                         int ret = client.moveKiiFileToTrash(kFile);
                         if (ret == SyncMsg.ERROR_RECORD_NOT_FOUND) {
                             UiUtils.showToast(
                                     getActivity(),
                                     getString(R.string.error_cannot_trash_server_file));
                             return true;
                         }
                         break;
                     case MENU_DELETE:
                         client.delete(kFile, false);
                         break;
                     case MENU_DELETE_LOCAL:
                         client.delete(kFile, true);
                         break;
                     case MENU_CANCEL:
                         client.cancel(kFile);
                         break;
                     case MENU_DOWNLOAD:
                         Toast.makeText(
                                 getActivity(),
                                 "Download at folder:"
                                         + KiiSyncClient.getInstance(
                                                 getActivity())
                                                 .getDownloadFolder(),
                                 Toast.LENGTH_SHORT).show();
                         Runnable r1 = new Runnable() {
                             @Override
                             public void run() {
                                 client.download(kFile, null);
                             }
                         };
                         new Thread(r1).start();
                         break;
                 }
 
                 Utils.startSync(getActivity(), BackupService.ACTION_REFRESH);
             }
 
             return true;
         } else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
             int groupPos = ExpandableListView
                     .getPackedPositionGroup(info.packedPosition);
             Toast.makeText(getActivity(),
                     title + ": Group " + groupPos + " clicked",
                     Toast.LENGTH_SHORT).show();
 
             return true;
         }
 
         return false;
 
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,
             ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
         ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
         int type = ExpandableListView
                 .getPackedPositionType(info.packedPosition);
         int group = ExpandableListView
                 .getPackedPositionGroup(info.packedPosition);
         int child = ExpandableListView
                 .getPackedPositionChild(info.packedPosition);
         // Only create a context menu for child items
         if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
             KiiFile kFile = (KiiFile) mAdapter.getChild(group, child);
             if ((kFile != null) && kFile.isFile()) {
                 menu.setHeaderTitle(kFile.getTitle());
 
                 KiiSyncClient kiiClient = KiiSyncClient
                         .getInstance(getActivity());
 
                 if (kiiClient == null) {
                     UiUtils.showToast(getActivity(), "Not ready.");
                     return;
                 }
 
                 int status = kiiClient.getStatus(kFile);
 
                 switch (status) {
                     case KiiFile.STATUS_DELETE_REQUEST:
                     case KiiFile.STATUS_DELETE_REQUEST_INCLUDEBODY:
                     case KiiFile.STATUS_SERVER_DELETE_REQUEST:
                         UiUtils.showToast(getActivity(),
                                 "No option for deleted file.");
                         break;
                     case KiiFile.STATUS_PREPARE_TO_SYNC:
                     case KiiFile.STATUS_UPLOADING_BODY:
                         menu.add(MENU_CANCEL, 0, 0, "Cancel Upload");
                         break;
                     // TODO: can implement the option to update the backup copy
                     // or restore the previous version
                     case KiiFile.STATUS_BODY_OUTDATED:
                     case KiiFile.STATUS_NO_BODY:
                     case KiiFile.STATUS_SYNCED:
                     case KiiFile.STATUS_REQUEST_BODY:
                     case KiiFile.STATUS_DOWNLOADING_BODY:
                     case KiiFile.STATUS_UNKNOWN:
                     default:
                         String category = kFile.getCategory();
                         if (TextUtils.isEmpty(category)
                                 || !KiiSyncClient.CATEGORY_TRASH
                                         .equalsIgnoreCase(category)) {
                             menu.add(MENU_MOVE_TRASH, 0, 0, "Move To Trash");
                             if (status == KiiFile.STATUS_NO_BODY) {
                                 menu.add(MENU_DOWNLOAD, 0, 0, "Download");
                             } else {
                                 menu.add(MENU_DELETE_LOCAL, 0, 0,
                                         "Delete Local & Backup");
                             }
                         } else {
                             menu.add(MENU_RESTORE_TRASH, 0, 0,
                                     "Restore From Trash");
                         }
                         menu.add(MENU_DELETE, 0, 0, "Delete Backup Copy");
                         break;
                 }
             } else {
                 UiUtils.showToast(getActivity(), "No menu for directory.");
             }
         }
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         inflater.inflate(R.menu.menu, menu);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         mView = inflater.inflate(R.layout.expandable_list_with_header,
                 container, false);
         Button b = (Button) mView.findViewById(R.id.button_left);
         b.setText(getString(R.string.button_refresh));
         b.setOnClickListener(mClickListener);
         b = (Button) mView.findViewById(R.id.button_right);
         b.setText(getString(R.string.header_upload));
         b.setOnClickListener(mClickListener);
         refreshUI(getActivity());
         mList = (ExpandableListView) mView.findViewById(android.R.id.list);
         setHasOptionsMenu(true);
         return mView;
     }
 
     private ExpandableListView getExpandableListView() {
         return mList;
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         mNewEventListener = new NewEventListener(getActivity());
         registerForContextMenu(getExpandableListView());
         mAdapter = new KiiFileExpandableListAdapter(getActivity(),
                 KiiSyncClient.getInstance(getActivity()),
                 KiiFileExpandableListAdapter.TYPE_DATA, mClickListener);
         mList.setAdapter(mAdapter);
         mList.setOnChildClickListener(mChildListener);
         mNewEventListener.register();
         registerReceiver();
     }
 
     private Receiver receiver;
 
     private void registerReceiver() {
         receiver = new Receiver();
         getActivity().registerReceiver(receiver,
                 new IntentFilter(DownloadManager.ACTION_DOWNLOAD_END));
         getActivity().registerReceiver(receiver,
                 new IntentFilter(DownloadManager.ACTION_DOWNLOAD_START));
     }
 
     class Receiver extends BroadcastReceiver {
 
         @Override
         public void onReceive(Context context, Intent intent) {
             if (intent.getAction()
                     .equals(DownloadManager.ACTION_DOWNLOAD_START)) {
                 handler.sendEmptyMessageDelayed(PROGRESS_START, 500);
             } else if (intent.getAction().equals(
                     DownloadManager.ACTION_DOWNLOAD_END)) {
                 handler.sendEmptyMessage(PROGRESS_END);
             }
         }
     }
 
     @Override
     public void onDestroy() {
         if (mNewEventListener != null) {
             mNewEventListener.unregister();
         }
         getActivity().unregisterReceiver(receiver);
         super.onDestroy();
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.resume_upload:
                 fullFefresh();
                 break;
             case R.id.refresh:
                 syncRefresh();
                 break;
             case R.id.suspend:
                 syncStop();
                 break;
             case R.id.settings:
                 Intent intent = new Intent(getActivity(),
                         SettingsActivity.class);
                 startActivity(intent);
                 break;
             default:
                 return super.onOptionsItemSelected(item);
         }
         return true;
     }
 
     public class NewEventListener extends SyncNewEventListener {
 
         public NewEventListener(Context context) {
             super(context);
         }
 
         @Override
         public void onNewSyncDeleteEvent(Uri[] arg0) {
             handler.sendEmptyMessageDelayed(PROGRESS_UPDATE, 500);
         }
 
         @Override
         public void onNewSyncInsertEvent(Uri[] arg0) {
             handler.sendEmptyMessageDelayed(PROGRESS_UPDATE, 500);
         }
 
         @Override
         public void onNewSyncUpdateEvent(Uri[] arg0) {
             handler.sendEmptyMessageDelayed(PROGRESS_UPDATE, 500);
         }
 
         @Override
         public void onSyncComplete(SyncMsg msg) {
             handler.sendEmptyMessageDelayed(PROGRESS_END, 500);
             if (msg != null) {
                 switch (msg.sync_result) {
                     case SyncMsg.OK:
                         refreshUI(getActivity());
                         break;
                 }
             }
         }
 
         @Override
         public void onSyncStart(String syncMode) {
             handler.sendEmptyMessageDelayed(PROGRESS_START, 500);
         }
 
         @Override
         public void onQuotaExceeded(Uri arg0) {
             handler.sendEmptyMessageDelayed(PROGRESS_UPDATE, 500);
 
         }
 
         @Override
         public void onLocalChangeSyncedEvent(Uri[] uris) {
             handler.sendEmptyMessageDelayed(PROGRESS_UPDATE, 500);
         }
 
         @Override
         public void onDownloadComplete(Uri[] arg0) {
             handler.sendEmptyMessageDelayed(PROGRESS_UPDATE, 500);
         }
 
     }
 
     public final static int PROGRESS_START = 1;
     public final static int PROGRESS_END = 2;
     public final static int PROGRESS_UPDATE = 3;
 
     private static void refreshUI(Context context) {
         //refresh the header text;
         TextView tv = (TextView) mView.findViewById(R.id.header_text);
         tv.setText(UiUtils.getLastSyncTime(context));
     }
 
     View.OnClickListener mClickListener = new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             switch (v.getId()) {
                 case R.id.button_left:
                     syncRefresh();
                     break;
                 case R.id.button_right:
                     Intent i = new Intent(getActivity(),
                             ProgressListActivity.class);
                     getActivity().startActivity(i);
                     break;
                 case R.id.list_complex_more_button:
                     View row = (View) v.getTag();
                     getExpandableListView().showContextMenuForChild(row);
                     break;
             }
         }
     };
 
     private void syncStop() {
         KiiSyncClient kiiClient = KiiSyncClient.getInstance(getActivity());
         if (kiiClient != null) {
             kiiClient.suspend();
         }
     }
 
     /**
      * resume upload
      */
     private void fullFefresh() {
         Utils.startSync(getActivity(), BackupService.ACTION_REFRESH);
     }
 
     /**
      * get new records from server if there are any
      */
     private void syncRefresh() {
         Utils.startSync(getActivity(), BackupService.ACTION_REFRESH_QUICK);
     }
 
     public Handler handler = new Handler() {
         @Override
         public void handleMessage(Message msg) {
             switch (msg.what) {
                 case PROGRESS_START:
                     handler.removeMessages(PROGRESS_END);
                 case PROGRESS_UPDATE:
                     mAdapter.notifyDataSetChanged();
                     break;
                 case PROGRESS_END:
                 default:
                     handler.removeMessages(PROGRESS_END);
                     if (mAdapter != null) {
                         mAdapter.notifyDataSetChanged();
                     }
                     break;
             }
 
         }
     };
 
     private OnChildClickListener mChildListener = new OnChildClickListener() {
 
         @Override
         public boolean onChildClick(ExpandableListView parent, View v,
                 int groupPosition, int childPosition, long id) {
             KiiFile kFile = (KiiFile) mAdapter.getChild(groupPosition,
                     childPosition);
             if (kFile.isFile()) {
 
                 String category = kFile.getCategory();
 
                 // check if the file is trash category, if yes prompt for
                 // restore
                 if (!TextUtils.isEmpty(category)
                         && KiiSyncClient.CATEGORY_TRASH
                                 .equalsIgnoreCase(category)) {
                     UiUtils.showToast(getActivity(),
                             "Please restore before view!");
                     return true;
                 }
 
                 Intent intent = null;
                 MimeInfo mime = MimeUtil.getInfoByKiiFile(kFile);
 
                 if (KiiSyncClient.getInstance(getActivity()).getStatus(kFile) != KiiFile.STATUS_NO_BODY) {
                     intent = UiUtils.getLaunchFileIntent(kFile.getLocalPath(),
                             mime);
                 }
                 if ((intent == null) && (kFile.getAvailableURL() != null)) {
                     if (mime != null) {
                         intent = UiUtils.getLaunchURLIntent(
                                 kFile.getAvailableURL(), mime.getMimeType());
                     }
                 }
 
                 if (intent == null) {
                     UiUtils.showToast(getActivity(),
                             "Failed to launch the file - " + kFile.getTitle());
                 } else {
                     try {
                         startActivity(intent);
                     } catch (Exception ex) {
                         UiUtils.showToast(
                                 getActivity(),
                                 "Encounter error when launch file ("
                                         + kFile.getTitle() + "). Error("
                                         + ex.getMessage() + ")");
                     }
                 }
             } else {
                 UiUtils.showToast(getActivity(),
                         "Failed to launch the viewer for " + kFile.getTitle());
             }
             return true;
         }
 
     };
 
 }
