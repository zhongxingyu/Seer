 package com.ces.cloudstorge.Dialog;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.ContentResolver;
 import android.content.DialogInterface;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 
 import com.ces.cloudstorge.Contract;
 import com.ces.cloudstorge.MainActivity;
 import com.ces.cloudstorge.R;
 import com.ces.cloudstorge.provider.CloudStorgeContract;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created by MichaelDai on 13-7-30.
  */
 public class FolderListDialog extends DialogFragment {
     public FolderListDialog() {
 
     }
 
     public interface FolderListDialogListener {
         void onFinishSelectFolder(int folderId, String arraylist);
     }
 
     FolderListDialogListener mListener;
     private ListView mDialogFolderList;
     private ImageView mDialogImageIcon;
     private TextView mDialogFolderName;
     private int currentFolderId;
     private int parentFolderId;
     private String arraylist;
     private SimpleAdapter mAdapter;
     private boolean isRoot;
     private String username;
     private List<Map<String, String>> listData;
     private ContentResolver mContentResolver;
 
     // sql查询条件(root)
     static final String SELECTION_SPECIAL = "( " + CloudStorgeContract.CloudStorge.COLUMN_NAME_PARENT_FOLDER_ID +
             "= (select " + CloudStorgeContract.CloudStorge.COLUMN_NAME_FOLDER_ID +
             " from " + CloudStorgeContract.CloudStorge.TABLE_NAME + " where " + CloudStorgeContract.CloudStorge.COLUMN_NAME_PARENT_FOLDER_ID +
             "=%d and " + CloudStorgeContract.CloudStorge.COLUMN_NAME_USERNAME + "='%s' limit 1)" +
             " and " + CloudStorgeContract.CloudStorge.COLUMN_NAME_USERNAME + " = '%s' " +
             " and " + CloudStorgeContract.CloudStorge.COLUMN_NAME_FOLDER_ID + "<> -1)";
 
     // sql查询条件(root)
     static final String SELECTION_CHILD = "( " + CloudStorgeContract.CloudStorge.COLUMN_NAME_PARENT_FOLDER_ID + "=%d " +
             "and " + CloudStorgeContract.CloudStorge.COLUMN_NAME_USERNAME + " = '%s' and " + CloudStorgeContract.CloudStorge.COLUMN_NAME_FOLDER_ID + "<> -1)";
 
     static final String selection_folder_format = "(" + CloudStorgeContract.CloudStorge.COLUMN_NAME_FOLDER_ID + "=%d and "
             + CloudStorgeContract.CloudStorge.COLUMN_NAME_USERNAME +
             " = '%s')";
 
     // 需要的字段
     public static final String[] fromColumns = {
             CloudStorgeContract.CloudStorge.COLUMN_NAME_NAME,
             CloudStorgeContract.CloudStorge.COLUMN_NAME_PARENT_FOLDER_ID,
             CloudStorgeContract.CloudStorge.COLUMN_NAME_FOLDER_ID
     };
 
     // view中的对象
     public static final int[] toViews = {R.id.list_dialog_text,
             R.id.list_dialog_parentfolderid,
             R.id.list_dialog_folderid
     };
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         try {
             mListener = (FolderListDialogListener) activity;
         } catch (ClassCastException e) {
             throw new ClassCastException(activity.toString()
                     + " must implement NoticeDialogListener");
         }
     }
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         LayoutInflater inflater = getActivity().getLayoutInflater();
         View view = inflater.inflate(R.layout.fragment_folder_list_dialog, null);
         mDialogFolderList = (ListView) view.findViewById(R.id.list_folder_dialog);
         mDialogImageIcon = (ImageView) view.findViewById(R.id.dialog_folder_icon);
         mDialogFolderName = (TextView) view.findViewById(R.id.dialog_folder_name);
         mDialogImageIcon.setVisibility(View.GONE);
         mDialogFolderName.setText(R.string.app_activity_title);
         builder.setView(view);
         arraylist = getArguments().getString("arraylist");
         currentFolderId = getArguments().getInt("currentFolderId");
         parentFolderId = getArguments().getInt("parentFolderId");
         username = getArguments().getString("currentUser");
         isRoot = true;
         mContentResolver = this.getActivity().getContentResolver();
         mDialogImageIcon.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 listData.clear();
                 String selection = String.format(SELECTION_CHILD, parentFolderId, username);
                 String selectionParent = String.format(selection_folder_format, parentFolderId, username);
                 Cursor parentCursor = mContentResolver.query(CloudStorgeContract.CloudStorge.CONTENT_URI, MainActivity.PROJECTION, selectionParent, null, null);
                 parentCursor.moveToFirst();
                 int parentFolderIdtmp = parentCursor.getInt(Contract.PROJECTION_PARENT_FOLDER_ID);
                 String folderName = parentCursor.getString(Contract.PROJECTION_NAME);
                 Cursor cursor = mContentResolver.query(CloudStorgeContract.CloudStorge.CONTENT_URI, MainActivity.PROJECTION, selection, null, null);
                 while (cursor.moveToNext()) {
                     Map<String, String> map = new HashMap<String, String>();
                     map.put(CloudStorgeContract.CloudStorge.COLUMN_NAME_NAME, cursor.getString(Contract.PROJECTION_NAME));
                     map.put(CloudStorgeContract.CloudStorge.COLUMN_NAME_PARENT_FOLDER_ID, cursor.getInt(Contract.PROJECTION_PARENT_FOLDER_ID) + "");
                     map.put(CloudStorgeContract.CloudStorge.COLUMN_NAME_FOLDER_ID, cursor.getInt(Contract.PROJECTION_FOLDER_ID) + "");
                     listData.add(map);
                 }
                 mAdapter.notifyDataSetChanged();
                 if (-1 == parentFolderIdtmp)
                     mDialogImageIcon.setVisibility(View.GONE);
                 currentFolderId = parentFolderId;
                 parentFolderId = parentFolderIdtmp;
                 mDialogFolderName.setText(folderName);
             }
         });
 
         listData = new ArrayList<Map<String, String>>();
         String selection = String.format(SELECTION_SPECIAL, -1, username, username);
         Cursor cursor = mContentResolver.query(CloudStorgeContract.CloudStorge.CONTENT_URI, MainActivity.PROJECTION, selection, null, null);
         while (cursor.moveToNext()) {
             Map<String, String> map = new HashMap<String, String>();
             map.put(CloudStorgeContract.CloudStorge.COLUMN_NAME_NAME, cursor.getString(Contract.PROJECTION_NAME));
             map.put(CloudStorgeContract.CloudStorge.COLUMN_NAME_PARENT_FOLDER_ID, cursor.getInt(Contract.PROJECTION_PARENT_FOLDER_ID) + "");
            map.put(CloudStorgeContract.CloudStorge.COLUMN_NAME_FOLDER_ID, cursor.getInt(Contract.PROJECTION_FOLDER_ID) + "");
             listData.add(map);
         }
         mDialogFolderList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                 mDialogImageIcon.setVisibility(View.VISIBLE);
                 Map<String, String> selectMap = listData.get(position);
                 int folderId = Integer.parseInt(selectMap.get(CloudStorgeContract.CloudStorge.COLUMN_NAME_FOLDER_ID));
                 int parentfolderId = Integer.parseInt(selectMap.get(CloudStorgeContract.CloudStorge.COLUMN_NAME_PARENT_FOLDER_ID));
                 String folderName = selectMap.get(CloudStorgeContract.CloudStorge.COLUMN_NAME_NAME);
                 currentFolderId = folderId;
                 parentFolderId = parentfolderId;
                 listData.clear();
                 String selection = String.format(SELECTION_CHILD, folderId, username);
                 Cursor cursor = mContentResolver.query(CloudStorgeContract.CloudStorge.CONTENT_URI, MainActivity.PROJECTION, selection, null, null);
                 while (cursor.moveToNext()) {
                     Map<String, String> map = new HashMap<String, String>();
                     map.put(CloudStorgeContract.CloudStorge.COLUMN_NAME_NAME, cursor.getString(Contract.PROJECTION_NAME));
                     map.put(CloudStorgeContract.CloudStorge.COLUMN_NAME_PARENT_FOLDER_ID, cursor.getInt(Contract.PROJECTION_PARENT_FOLDER_ID) + "");
                     map.put(CloudStorgeContract.CloudStorge.COLUMN_NAME_FOLDER_ID, cursor.getInt(Contract.PROJECTION_FOLDER_ID) + "");
                     listData.add(map);
                 }
                 mAdapter.notifyDataSetChanged();
                 mDialogFolderName.setText(folderName);
             }
         });
         mAdapter = new SimpleAdapter(getActivity().getApplicationContext(), listData, R.layout.list_item_dialog,
                 fromColumns, toViews);
         mDialogFolderList.setAdapter(mAdapter);
         builder.setPositiveButton(R.string.menu_action_move, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int id) {
                 mListener.onFinishSelectFolder(currentFolderId, arraylist);
             }
         }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int id) {
                 listData.clear();
                 dialog.cancel();
             }
         });
         return builder.create();
     }
 }
