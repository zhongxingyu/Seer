 package com.vibhinna.binoy;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.ContentResolver;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.CheckBox;
 
 import com.actionbarsherlock.app.SherlockDialogFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 
 public class FormatDialogFragment extends SherlockDialogFragment {
 	private static SherlockFragmentActivity mContext;
 	private static ContentResolver mResolver;
 
 	private static Cursor mCursor;
 	private static String mPath;
 	private static String mName;
 
 	protected boolean cacheCheckBool = false;
 	protected boolean dataCheckBool = false;
 	protected boolean systemCheckBool = false;
 
 	static FormatDialogFragment newInstance(VibhinnaFragment vibhinnaFragment,
 			long id) {
 		FormatDialogFragment fragment = new FormatDialogFragment();
 		mContext = vibhinnaFragment.getSherlockActivity();
 		mResolver = mContext.getContentResolver();
 		mCursor = mResolver.query(
 				Uri.parse("content://" + VibhinnaProvider.AUTHORITY + "/"
 						+ VibhinnaProvider.VFS_BASE_PATH + "/" + id), null,
 				null, null, null);
 		mCursor.moveToFirst();
 		mName = mCursor.getString(mCursor
 				.getColumnIndex(DatabaseHelper.VIRTUAL_SYSTEM_COLUMN_NAME));
 		mPath = mCursor.getString(mCursor
 				.getColumnIndex(DatabaseHelper.VIRTUAL_SYSTEM_COLUMN_PATH));
 		return fragment;
 	}
 
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 		super.onCreateDialog(savedInstanceState);
 		LayoutInflater factory = LayoutInflater.from(mContext);
 		final View formatView = factory.inflate(R.layout.format_dialog, null);
 		AlertDialog.Builder builder;
 		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB)
 			builder = new AlertDialog.Builder(mContext);
 		else
 			builder = new HoloAlertDialogBuilder(mContext);
 		final AlertDialog dialog = builder
				.setTitle(getString(R.string.format, mName))
 				.setView(formatView)
 				.setPositiveButton(getString(R.string.okay), onClickListener)
 				.setNeutralButton(getString(R.string.cancel),
 						new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(
 									DialogInterface dialogInterface, int i) {
 							}
 						}).show();
 		CheckBox chkCache = (CheckBox) formatView.findViewById(R.id.cache);
 		CheckBox chkData = (CheckBox) formatView.findViewById(R.id.data);
 		CheckBox chkSystem = (CheckBox) formatView.findViewById(R.id.system);
 		chkCache.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (((CheckBox) v).isChecked()) {
 					cacheCheckBool = true;
 				} else {
 					cacheCheckBool = false;
 				}
 			}
 		});
 		chkData.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (((CheckBox) v).isChecked()) {
 					dataCheckBool = true;
 				} else {
 					dataCheckBool = false;
 				}
 			}
 		});
 		chkSystem.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				if (((CheckBox) v).isChecked()) {
 					systemCheckBool = true;
 				} else {
 					systemCheckBool = false;
 				}
 			}
 		});
 		return dialog;
 	}
 
 	private DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
 
 		@Override
 		public void onClick(DialogInterface dialog, int whichButton) {
 			Intent service = new Intent(mContext, VibhinnaService.class);
 			service.putExtra(VibhinnaService.TASK_TYPE,
 					VibhinnaService.TASK_TYPE_FORMAT_VFS);
 			service.putExtra(VibhinnaService.FORMAT_CACHE, cacheCheckBool);
 			service.putExtra(VibhinnaService.FORMAT_DATA, dataCheckBool);
 			service.putExtra(VibhinnaService.FORMAT_SYSTEM, systemCheckBool);
 			service.putExtra(VibhinnaService.FOLDER_PATH, mPath);
 			mContext.startService(service);
 			cacheCheckBool = false;
 			dataCheckBool = false;
 			systemCheckBool = false;
 		}
 	};
 }
