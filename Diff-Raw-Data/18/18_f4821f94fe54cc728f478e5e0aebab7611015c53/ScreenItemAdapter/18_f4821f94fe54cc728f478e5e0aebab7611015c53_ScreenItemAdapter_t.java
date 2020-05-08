 package com.dympy.endless.home.screen;
 
 import java.util.ArrayList;
 
 import com.dympy.endless.R;
 import com.dympy.endless.home.LauncherModel;
 import com.dympy.endless.home.apps.AppData;
 import com.dympy.endless.home.apps.AppDataAdapter;
 import com.dympy.endless.home.screen.ScreenItem.Type;
 import com.dympy.endless.home.ui.CustomGrid;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.GridView;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 
 public class ScreenItemAdapter extends ArrayAdapter<ScreenItem> {
 	private Context context;
 	private ArrayList<ScreenItem> data = null;
 	private LauncherModel app;
 
 	private boolean longPressed = false;
 
 	public ScreenItemAdapter(Context context, int layoutResourceId,
 			ArrayList<ScreenItem> data) {
 		super(context, layoutResourceId, data);
 		this.context = context;
 		app = (LauncherModel) context.getApplicationContext();
 		this.data = data;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		ScreenItem instance = data.get(position);
 		LayoutInflater inflater = ((Activity) context).getLayoutInflater();
 		View row = inflater
 				.inflate(R.layout.list_item_workspace, parent, false);
 		ScreenItemHolder tempHolder = new ScreenItemHolder();
 
 		tempHolder.instance = instance;
 		tempHolder.title = (TextView) row
 				.findViewById(R.id.item_workspace_txt_title);
 		tempHolder.appGrid = (CustomGrid) row
 				.findViewById(R.id.item_workspace_grid_apps);
 
 		final ScreenItemHolder itemHolder = tempHolder;
 
 		ImageButton itemViewSettings = (ImageButton) row
 				.findViewById(R.id.item_workspace_btn_settings);
 		LinearLayout widgetViewContent = (LinearLayout) row
 				.findViewById(R.id.item_workspace_layout_widget);
 
 		itemHolder.title.setText(itemHolder.instance.getName());
 
 		itemViewSettings.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				showSettingsDialog(itemHolder);
 			}
 		});
 
 		if (itemHolder.instance.getType() == Type.APPS) {
 			AppDataAdapter appsAdapter = new AppDataAdapter(context,
 					R.layout.list_item_app, itemHolder.instance.getAppDatas());
 			itemHolder.appGrid.setExpanded(true);
 			itemHolder.appGrid.setAdapter(appsAdapter);
 			itemHolder.appGrid
 					.setOnItemClickListener(new OnItemClickListener() {
 
 						@Override
 						public void onItemClick(AdapterView<?> parent, View v,
 								int position, long id) {
 							if (longPressed) {
 								longPressed = false;
 							} else {
 								context.startActivity(itemHolder.instance
 										.getAppDatas().get(position)
 										.getAppIntent());
 							}
 						}
 					});
 			itemHolder.appGrid
 					.setOnItemLongClickListener(new OnItemLongClickListener() {
 
 						@Override
 						public boolean onItemLongClick(AdapterView<?> parent,
 								View v, int position, long id) {
 							longPressed = true;
 							removeAppDialog(itemHolder, position);
 							return false;
 						}
 					});
 		} else if (itemHolder.instance.getType() == Type.WIDGET) {
 			widgetViewContent.addView(itemHolder.instance.getWidgetView());
 		}
 		return row;
 	}
 
 	static class ScreenItemHolder {
 		ScreenItem instance;
 		TextView title;
 		CustomGrid appGrid;
 	}
 
 	private void showSettingsDialog(final ScreenItemHolder itemHolder) {
 		// TODO: Move strings to strings file
 		final AlertDialog actualDialog;
 		AlertDialog.Builder itemSettings = new AlertDialog.Builder(context);
 		itemSettings.setTitle("Item Settings");
 
 		String[] settings = { "Add app", "Remove item", "Rename item" };
 		ListView settingsContent = new ListView(context);
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
 				android.R.layout.simple_list_item_1, settings);
 		settingsContent.setAdapter(adapter);
 
 		itemSettings.setView(settingsContent);
 		itemSettings.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						// Canceled.
 					}
 				});
 
 		actualDialog = itemSettings.create();
 		actualDialog.show();
 		settingsContent.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View v,
 					int position, long id) {
 				actualDialog.dismiss();
 				switch (position) {
 				case 0: // Add app
 					addAppDialog(itemHolder);
 					break;
 				case 1: // Remove item
 					removeItemDialog(itemHolder);
 					break;
 				case 2: // Rename item
 					renameItemDialog(itemHolder);
 					break;
 				}
 			}
 		});
 	}
 
 	private void removeAppDialog(final ScreenItemHolder itemHolder,
 			final int appPos) {
 		// TODO: Move strings to strings file
 		AlertDialog.Builder removeApp = new AlertDialog.Builder(context);
 		removeApp.setTitle("Remove app");
 		removeApp
 				.setMessage("Are you sure you want to remove this app from the workspace?");
 		removeApp.setNegativeButton("No",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int whichButton) {
 						// Canceled.
 					}
 				});
 		removeApp.setPositiveButton("Yes",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int whichButton) {
 						String appName = itemHolder.instance.getAppDatas()
 								.get(appPos).getAppName();
 						// TODO: Remove ScreenItemApp
 						// app.removeAppFromItem(itemHolder.instance.getApps()
 						// .get(appPos));
 						resetContentView(itemHolder);
 						Toast.makeText(context, "Removed '" + appName + "'",
 								Toast.LENGTH_SHORT).show();
 					}
 				});
 		removeApp.show();
 	}
 
 	private void removeItemDialog(final ScreenItemHolder itemHolder) {
 		// TODO: Move strings to strings file
 		AlertDialog.Builder removeApp = new AlertDialog.Builder(context);
 		removeApp.setTitle("Remove item");
 		removeApp
 				.setMessage("Are you sure you want to remove this item from the workspace screen?");
 		removeApp.setNegativeButton("No",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int whichButton) {
 						// Canceled.
 					}
 				});
 		removeApp.setPositiveButton("Yes",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int whichButton) {
 						app.removeScreenItem(itemHolder.instance);
 					}
 				});
 		removeApp.show();
 	}
 
 	// TODO: Fix this
 	private void addAppDialog(final ScreenItemHolder itemHolder) {
 		// TODO: Move strings to strings file
 		final AlertDialog listDialog;
 		AlertDialog.Builder listBuilder = new AlertDialog.Builder(context);
 		listBuilder.setTitle("Select an app");
 
 		GridView appGrid = new GridView(context);
 		appGrid.setNumColumns(3);
 		AppDataAdapter gridAdapter = new AppDataAdapter(context,
 				R.layout.list_item_app, app.getApps());
 		appGrid.setAdapter(gridAdapter);
 		listBuilder.setView(appGrid);
 		listDialog = listBuilder.create();
 
 		appGrid.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View v,
 					int position, long id) {
 				listDialog.dismiss();
 				boolean shouldAdd = true;
 				for (AppData tempApp : itemHolder.instance.getAppDatas()) {
 					if (app.getApps().get(position).getPackageName() == tempApp
 							.getPackageName()
 							&& app.getApps().get(position).getActivityName() == tempApp
 									.getActivityName()) {
 						shouldAdd = false;
 					}
 				}
 				if (shouldAdd) {
 					// TODO: Add ScreenItemApp
 					// app.addAppToItem(itemHolder.instance.getApps()
 					// .get(position));
 					resetContentView(itemHolder);
 				} else {
 					Toast.makeText(context,
 							"You already have this app in this Item.",
 							Toast.LENGTH_SHORT).show();
 				}
 			}
 		});
 
 		listDialog.show();
 	}
 
 	private void renameItemDialog(final ScreenItemHolder itemHolder) {
 		// TODO: Move strings to strings file
 		AlertDialog.Builder listDialog = new AlertDialog.Builder(context);
 		listDialog.setTitle("Rename item");
 		listDialog.setMessage("Change the item title");
 
 		final EditText renameText = new EditText(context);
 		renameText.setText(itemHolder.instance.getName());
 		listDialog.setView(renameText);
 		listDialog.setPositiveButton("Ok",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						itemHolder.instance.setName(renameText.getText()
 								.toString());
 						itemHolder.title.setText(itemHolder.instance.getName());
						app.updateScreenItem(itemHolder.instance);
 					}
 				});
 
 		listDialog.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
						// Canceled.
 					}
 				});
 
 		listDialog.show();
 	}
 
 	private void resetContentView(ScreenItemHolder itemHolder) {
 		if (itemHolder.instance.getType() == Type.APPS) {
 			itemHolder.appGrid.setAdapter(new AppDataAdapter(context,
 					R.layout.list_item_app, itemHolder.instance.getAppDatas()));
 		}
 	}
 }
