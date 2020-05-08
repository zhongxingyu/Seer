 package com.vibhinna.binoy;
 
 import java.io.File;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ListFragment;
 import android.app.LoaderManager;
 import android.app.ProgressDialog;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.CursorLoader;
 import android.content.DialogInterface;
 import android.content.Loader;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.provider.BaseColumns;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.CursorAdapter;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.Spinner;
 
 public class VibhinnaFragment extends ListFragment implements
 		LoaderManager.LoaderCallbacks<Cursor> {
 	private static final int TUTORIAL_LIST_LOADER = 0x01;
 	private static final String TAG = null;
 	private VibhinnaAdapter adapter;
 
 	protected boolean cacheCheckBool = false;
 	protected boolean dataCheckBool = false;
 	protected boolean systemCheckBool = false;
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		DetailsDialog detailsDialog = new DetailsDialog(this);
 		detailsDialog.getDialog(id);
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		// use crsorloader in fragment
 		super.onCreate(savedInstanceState);
 		String[] from = { "name", "desc", "status", "path", "folder",
 				BaseColumns._ID };
 		int[] to = { R.id.name, R.id.desc, R.id.status, R.id.path };
 
 		getLoaderManager().initLoader(TUTORIAL_LIST_LOADER, null, this);
 
 		adapter = new VibhinnaAdapter(getActivity().getApplicationContext(),
 				R.layout.main_row, null, from, to,
 				CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
 		setListAdapter(adapter);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		registerForContextMenu(getListView());
 		if (!Constants.BINARY_FOLDER.exists()
				|| Constants.BINARY_FOLDER.list().length < 3) {
 			Constants.BINARY_FOLDER.mkdirs();
 			AssetsManager assetsManager = new AssetsManager(getActivity());
 			assetsManager .copyAssets();
 		}
 	}
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 		CursorLoader cursorLoader = new CursorLoader(getActivity(),
 				VibhinnaProvider.LIST_DISPLAY_URI, Constants.allColumns, null,
 				null, null);
 		return cursorLoader;
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
 		String[] a = cursor.getColumnNames();
 		cursor.moveToFirst();
 		do {
 			for (int i = 0; i < a.length; i++) {
 				Log.d(TAG,
 						"Column " + i + " : " + a[i] + " = "
 								+ cursor.getString(i));
 			}
 		} while (cursor.moveToNext());
 		adapter.swapCursor(cursor);
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> loader) {
 		adapter.swapCursor(null);
 	}
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenu.ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		AdapterView.AdapterContextMenuInfo info;
 		PropManager propmanager = new PropManager(this.getActivity()
 				.getApplicationContext());
 		try {
 			// Casts the incoming data object into the type for AdapterView
 			// objects.
 			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
 		} catch (ClassCastException e) {
 			// If the menu object can't be cast, logs an error.
 			Log.w("Exception", "exception in getting menuinfo");
 			return;
 		}
 		Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
 		String s1 = Constants.SD_PATH + propmanager.mbActivePath();
 		String s2 = cursor.getString(7);
 		if (cursor.equals(null) || s1.equals(s2))
 			return;
 		menu.setHeaderTitle(cursor.getString(1));
 		MenuInflater inflater = this.getActivity().getMenuInflater();
 		inflater.inflate(R.menu.context_menu, menu);
 	}
 
 	int iconid = 1;
 	private ProcessManager processManager = new ProcessManager();
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		final Context context = getActivity();
 		final ContentResolver cr = getActivity().getContentResolver();
 		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
 				.getMenuInfo();
 		// final DataSource datasource = new DataSource(this);
 		final Cursor item_cursor = (Cursor) getListAdapter().getItem(
 				menuInfo.position);
 		if (item_cursor == null) {
 			// For some reason the requested item isn't available, do nothing
 			return false;
 		}
 		final String folderpath = item_cursor.getString(7);
 		final String foldername = item_cursor.getString(1);
 		final String folderdesc = item_cursor.getString(2);
 		iconid = Integer.parseInt(item_cursor.getString(4));
 		// int j;
 		// for (j = 0; j < 8; j++) {
 		// Log.d(Tag.getTag(this),j + " = " + item_cursor.getString(j));
 		// }
 		final File mFolder = new File(folderpath);
 		final int itemid = Integer.parseInt(item_cursor.getString(0));
 		switch (item.getItemId()) {
 		case R.id.edit:
 			LayoutInflater factory = LayoutInflater.from(context);
 			final View editVSView = factory.inflate(R.layout.edit_vs_layout,
 					null);
 			final EditText evsname = (EditText) editVSView
 					.findViewById(R.id.vsname);
 			final EditText evsdesc = (EditText) editVSView
 					.findViewById(R.id.vsdesc);
 			final Spinner spinner = (Spinner) editVSView
 					.findViewById(R.id.spinner);
 			final ImageView i = (ImageView) editVSView
 					.findViewById(R.id.seticonimage);
 			ArrayAdapter<CharSequence> adapter = ArrayAdapter
 					.createFromResource(context, R.array.icon_array,
 							android.R.layout.simple_spinner_item);
 			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			spinner.setAdapter(adapter);
 			spinner.setSelection(iconid);
 			spinner.setAdapter(adapter);
 			spinner.setSelection(iconid);
 			spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
 				@Override
 				public void onItemSelected(AdapterView<?> arg0, View arg1,
 						int arg2, long arg3) {
 					iconid = arg2;
 					i.setImageResource(MiscMethods.getIcon(arg2));
 				}
 
 				@Override
 				public void onNothingSelected(AdapterView<?> arg0) {
 
 				}
 			});
 			evsdesc.setText(folderdesc);
 			evsname.setText(foldername);
 			new AlertDialog.Builder(context)
 					.setTitle(
 							getString(R.string.edits)
 									+ item_cursor.getString(1))
 					.setView(editVSView)
 					.setPositiveButton(getString(R.string.okay),
 							new DialogInterface.OnClickListener() {
 
 								@Override
 								public void onClick(DialogInterface dialog,
 										int whichButton) {
 									String vsname = mFolder.getName();
 									try {
 										vsname = evsname.getText().toString();
 									} catch (Exception e) {
 										e.printStackTrace();
 									}
 									String vsdesc = folderdesc;
 									try {
 										vsdesc = evsdesc.getText().toString();
 									} catch (Exception e) {
 										e.printStackTrace();
 									}
 									File newlocation = new File(
 											Constants.MBM_ROOT + vsname);
 									if (!mFolder.equals(newlocation)) {
 
 										newlocation = new File(MiscMethods
 												.newName(vsname));
 										mFolder.renameTo(newlocation);
 									}
 									ContentValues values = new ContentValues();
 									values.put(
 											DataBaseHelper.VIRTUAL_SYSTEM_COLUMN_NAME,
 											newlocation.getName());
 									values.put(
 											DataBaseHelper.VIRTUAL_SYSTEM_COLUMN_PATH,
 											"/mnt/sdcard/multiboot/"
 													+ newlocation.getName());
 									values.put(
 											DataBaseHelper.VIRTUAL_SYSTEM_COLUMN_DESCRIPTION,
 											vsdesc);
 									values.put(
 											DataBaseHelper.VIRTUAL_SYSTEM_COLUMN_TYPE,
 											iconid);
 									cr.update(
 											Uri.parse("content://"
 													+ VibhinnaProvider.AUTHORITY
 													+ "/"
 													+ VibhinnaProvider.TUTORIALS_BASE_PATH
 													+ "/" + itemid), values,
 											null, null);
 									iconid = 1;
 									// rlv.refreshListView();
 								}
 							})
 					.setNegativeButton(getString(R.string.cancel),
 							new DialogInterface.OnClickListener() {
 								@Override
 								public void onClick(DialogInterface dialog,
 										int whichButton) {
 									// Canceled.
 								}
 							}).show();
 			return true;
 		case R.id.delete:
 			new AlertDialog.Builder(context)
 					.setTitle(
 							getString(R.string.delete)
 									+ item_cursor.getString(1))
 					.setMessage(getString(R.string.rusure))
 					.setPositiveButton(getString(R.string.okay),
 							new DialogInterface.OnClickListener() {
 								@Override
 								public void onClick(
 										DialogInterface dialogInterface, int i) {
 									try {
 										MiscMethods.removeDirectory(mFolder);
 										// rlv.refreshListView();
 									} catch (Exception e) {
 										e.printStackTrace();
 										return;
 									}
 								}
 							})
 					.setNeutralButton(getString(R.string.cancel),
 							new DialogInterface.OnClickListener() {
 								@Override
 								public void onClick(
 										DialogInterface dialogInterface, int i) {
 								}
 							}).create().show();
 			return true;
 		case R.id.format:
 			factory = LayoutInflater.from(context);
 			final View formatView = factory.inflate(R.layout.format_dialog,
 					null);
 			CheckBox chkCache = (CheckBox) formatView.findViewById(R.id.cache);
 			CheckBox chkData = (CheckBox) formatView.findViewById(R.id.data);
 			CheckBox chkSystem = (CheckBox) formatView
 					.findViewById(R.id.system);
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
 			new AlertDialog.Builder(context)
 					.setTitle(
 							getString(R.string.format)
 									+ item_cursor.getString(1) + "?")
 					.setView(formatView)
 					.setPositiveButton(getString(R.string.okay),
 							new DialogInterface.OnClickListener() {
 								@Override
 								public void onClick(
 										DialogInterface dialogInterface, int i) {
 									final ProgressDialog processdialog = ProgressDialog
 											.show(context, Constants.EMPTY,
 													Constants.EMPTY, true);
 									final Handler handler = new Handler() {
 										@Override
 										public void handleMessage(Message msg) {
 											switch (msg.arg1) {
 											case 0:
 												processdialog
 														.setMessage(getString(R.string.formating)
 																+ folderpath
 																+ getString(R.string.cachext3));
 												break;
 											case 1:
 												processdialog
 														.setMessage(getString(R.string.formating)
 																+ folderpath
 																+ getString(R.string.dataext3));
 												break;
 											case 2:
 												processdialog
 														.setMessage(getString(R.string.formating)
 																+ folderpath
 																+ getString(R.string.systemext3));
 												break;
 											case 3:
 												processdialog.dismiss();
 												break;
 
 											}
 										}
 									};
 									Thread formatVFS = new Thread() {
 										@Override
 										public void run() {
 											String[] shellinput = {
 													Constants.EMPTY,
 													Constants.EMPTY,
 													Constants.EMPTY,
 													Constants.EMPTY,
 													Constants.EMPTY };
 											shellinput[0] = Constants.CMD_MKE2FS_EXT3;
 											shellinput[1] = folderpath;
 											final Message m0 = new Message();
 											final Message m1 = new Message();
 											final Message m2 = new Message();
 											final Message endmessage = new Message();
 											m0.arg1 = 0;
 											m1.arg1 = 1;
 											m2.arg1 = 2;
 											endmessage.arg1 = 3;
 											if (cacheCheckBool) {
 												handler.sendMessage(m0);
 												shellinput[2] = Constants.CACHE_IMG;
 												processManager
 														.inputStreamReader(
 																shellinput, 20);
 												cacheCheckBool = false;
 											}
 											if (dataCheckBool) {
 												handler.sendMessage(m1);
 												shellinput[2] = Constants.DATA_IMG;
 												processManager
 														.inputStreamReader(
 																shellinput, 20);
 												dataCheckBool = false;
 											}
 											if (systemCheckBool) {
 												handler.sendMessage(m2);
 												shellinput[2] = Constants.SYSTEM_IMG;
 												Log.d(TAG, "exec :"
 														+ shellinput[0]
 														+ shellinput[1]
 														+ shellinput[2]
 														+ shellinput[3]);
 												processManager
 														.inputStreamReader(
 																shellinput, 20);
 												systemCheckBool = false;
 
 											}
 											handler.sendMessage(endmessage);
 										}
 									};
 									formatVFS.start();
 								}
 							})
 					.setNeutralButton(getString(R.string.cancel),
 							new DialogInterface.OnClickListener() {
 								@Override
 								public void onClick(
 										DialogInterface dialogInterface, int i) {
 								}
 							}).create().show();
 			return true;
 		default:
 			return super.onContextItemSelected(item);
 		}
 
 	}
 }
