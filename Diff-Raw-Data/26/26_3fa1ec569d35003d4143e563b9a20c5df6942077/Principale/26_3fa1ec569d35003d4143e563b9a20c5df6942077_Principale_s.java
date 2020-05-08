 package com.liebekinder.mobiledegreasor;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.Editable;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ExpandableListView;
 import android.widget.ExpandableListView.OnGroupCollapseListener;
 import android.widget.ExpandableListView.OnGroupExpandListener;
 import android.widget.LinearLayout;
 import android.widget.ListAdapter;
 import android.widget.Toast;
 
 import com.liebekinder.mobiledegreasor.core.Category;
 import com.liebekinder.mobiledegreasor.core.CategoryManager;
 import com.liebekinder.mobiledegreasor.core.Task;
 
 public class Principale extends Activity {
 
 	/**
 	 * The global category manager. Restore it.
 	 */
 	private CategoryManager categoryManager;
 	private ListAdapter listadapter;
 	private SharedPreferences shared = null;
 	private Principale context;
 	private MyExpandableListAdapter adapter;
 	private ExpandableListView list;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		shared = PreferenceManager.getDefaultSharedPreferences(this);
 
 		this.context = this;
 
 		restoreState();
 
 //		if(categoryManager == null){
 //			Log.i("info","cat manager is null");
 //		}
 		
 		list = new ExpandableListView(this);
 		list.setChildIndicator(null);
 		adapter = new MyExpandableListAdapter(this,
 				(ArrayList<Category>) categoryManager.getCategoriesList());
 		list.setAdapter(adapter);
 
 
 		list.setOnGroupCollapseListener(new OnGroupCollapseListener() {
 
 			@Override
 			public void onGroupCollapse(int groupPosition) {
 				categoryManager.getCategoriesList().get(groupPosition)
 						.setUnwrapped(false);
 				saveState();
 			}
 		});
 
 		list.setOnGroupExpandListener(new OnGroupExpandListener() {
 
 			@Override
 			public void onGroupExpand(int groupPosition) {
 				categoryManager.getCategoriesList().get(groupPosition)
 						.setUnwrapped(true);
 				saveState();
 			}			
 		});
 
 		for (int i = 0; i < categoryManager.getCategoriesList().size(); i++) {
 			if (categoryManager.getCategoriesList().get(i).isUnwrapped())
 				list.expandGroup(i);
 		}
 
 		registerForContextMenu(list);
 
 		setContentView(list);
 		
 		if(categoryManager.getCategoriesList().size() == 0){
 			//we should open a context menu that tells that the user should add a category
 			AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 			alert.setTitle("Create a new category");
			alert.setMessage("It seems that you haven't created any category yet! To do so, push the menu button and click on 'New category': ");
 
 			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int whichButton) {
 					//nothing
 				}
 			});
 			alert.show();
 		}
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 
 		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
 
 		menu.setHeaderTitle("Default");
 		int type = ExpandableListView
 				.getPackedPositionType(info.packedPosition);
 
 		int group = ExpandableListView
 				.getPackedPositionGroup(info.packedPosition);
 
 		int child = ExpandableListView
 				.getPackedPositionChild(info.packedPosition);
 
 		// Only create a context menu for child items
 		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
 			// Array created earlier when we built the expandable list
 			String page = categoryManager.getCategoriesList().get(group)
 					.getTasksList().get(child).getName();
 
 			menu.setHeaderTitle(page);
 		}
 		else if(type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
 			// Array created earlier when we built the expandable list
 			String page = categoryManager.getCategoriesList().get(group)
 					.getName();
 
 			menu.setHeaderTitle(page);
 			menu.add(Menu.NONE, 0, 0, "Add a task...");
 		}
 		
 		menu.add(Menu.NONE, 1, 1, "Delete");
 		menu.add(Menu.NONE, 2, 2, "Cancel");
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		super.onContextItemSelected(item);
 		
 		ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
 
 		int type = ExpandableListView
 				.getPackedPositionType(info.packedPosition);
 
 		final int group = ExpandableListView
 				.getPackedPositionGroup(info.packedPosition);
 
 		int child = ExpandableListView
 				.getPackedPositionChild(info.packedPosition);
 
 		// Only create a context menu for child items
 		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
 			// Array created earlier when we built the expandable list
 			Category cat = categoryManager.getCategoriesList().get(group);
 			Task task = cat.getTasksList().get(child);
 			String page = task.getName();
 			Log.i("page",page);
 			if(item.getItemId()==1) {
 				Log.i("Suppr","delete");
 				cat.deleteTask(task.getUuid());
 				saveState();
 				adapter.notifyDataSetChanged();
 			}
 
 		}
 		else if(type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
 			// Array created earlier when we built the expandable list
 			final Category cat = categoryManager.getCategoriesList().get(group);
 			String page = cat.getName();
 			Log.i("page",page);
 			if(item.getItemId()==0) {
 				Log.i("Add","add");
 				AlertDialog.Builder alert = new AlertDialog.Builder(this);
 
 				alert.setTitle("Create a new task");
 				alert.setMessage("Enter your task name : ");
 
 				// Set an EditText view to get user input
 				final MyTextEdit input = new MyTextEdit(this);
 				alert.setView(input);
 
 				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						Editable value = input.getText();
 						String name = value.toString().replace("\n", "").trim();
 						String message;
 						if (name.equals("")) {
 							message = "Task name cannot be null !";
 						} else {
 							message = "Task \"" + name
 									+ "\" successfully created !";
 							cat.addTask(new Task(name,cat));
 							saveState();
 							list.expandGroup(group);
 							adapter.notifyDataSetChanged();
 							
 						}
 						Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
 					}
 				});
 
 				alert.setNegativeButton("Cancel",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int whichButton) {
 								// Canceled.
 							}
 						});
 
 				alert.show();
 			} 
 			else if(item.getItemId()==1) {
 				Log.i("Suppr","Delete");
 				categoryManager.deleteCategory(page);
 				adapter.notifyDataSetChanged();
 				saveState();
 			}
 		}
 
 		return true;
 	}
 
 	public void saveState() {
 		SharedPreferences.Editor editor = null;
 		editor = shared.edit();
 		try {
 			editor.putString("main", categoryManager.serialize());
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 		adapter.notifyDataSetChanged();
 		editor.commit();
 	}
 
 	public void restoreState() {
 		String s = null;
 		try {
 			s = shared.getString("main", null);
			if (s != null){
				categoryManager.deserialize(s);
			}
			else categoryManager = new CategoryManager(this);
 		} catch (Throwable e) {
 			categoryManager = new CategoryManager(this);
 		}
 	}
 
 	public LinearLayout affiche() {
 		LinearLayout global = new LinearLayout(this);
 
 		ExpandableListView lv = new ExpandableListView(this);
 		lv.setAdapter(listadapter);
 		global.addView(lv);
 
 		return global;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.principale, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		int lal = item.getNumericShortcut();
 		Log.i("her", "clic method action: " + lal);
 		switch(lal){		
 			case 49:
 				AlertDialog.Builder alert = new AlertDialog.Builder(this);
 	
 				alert.setTitle("Create a new category");
 				alert.setMessage("Enter your category name : ");
 	
 				// Set an EditText view to get user input
 				final MyTextEdit input = new MyTextEdit(this);
 				alert.setView(input);
 	
 				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						Editable value = input.getText();
 						String name = value.toString().replace("\n", "").trim();
 						String message;
 						if (name.equals("")) {
 							message = "Category name cannot be null !";
 						} else {
 							message = "Category \"" + name
 									+ "\" successfully created !";
 							if (!categoryManager.addCategory(new Category(name,
 									categoryManager)))
 								message = "Category \"" + name + "\" already exists !";
 							else {
 								saveState();
 								adapter.notifyDataSetChanged();
 							}
 						}
 						Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
 					}
 				});
 	
 				alert.setNegativeButton("Cancel",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int whichButton) {
 								// Canceled.
 							}
 						});
 	
 				alert.show();
 				break;
 			case 50:
 				for (int i = 0; i < categoryManager.getCategoriesList().size(); i++) {
 					if (categoryManager.getCategoriesList().get(i).isUnwrapped()){
 						categoryManager.getCategoriesList().get(i).setUnwrapped(false);
 						list.collapseGroup(i);						
 					}
 				}
 				saveState();
 				break;
 			default:
 				Log.i("err", "le numrique shortcut ne sert  rien...");
 				break;
 		}
 		return false;
 	}
 
 }
