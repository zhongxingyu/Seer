 package com.danielpecos.gtdtm.activities;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ExpandableListActivity;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnDismissListener;
 import android.content.Intent;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ExpandableListView;
 import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
 import android.widget.Toast;
 
 import com.danielpecos.gtdtm.R;
 import com.danielpecos.gtdtm.model.TaskManager;
 import com.danielpecos.gtdtm.model.beans.Context;
 import com.danielpecos.gtdtm.model.beans.Project;
 import com.danielpecos.gtdtm.model.beans.Task;
 import com.danielpecos.gtdtm.model.beans.Task.Status;
 import com.danielpecos.gtdtm.utils.ActivityUtils;
 import com.danielpecos.gtdtm.utils.ExpandableNestedMixedListAdapter;
import com.danielpecos.gtdtm.utils.GoogleTasksClientAsyncTask;
 import com.danielpecos.gtdtm.utils.ExpandableNestedMixedListAdapter.RowDisplayListener;
 import com.danielpecos.gtdtm.views.ContextViewHolder;
 import com.danielpecos.gtdtm.views.ProjectViewHolder;
 import com.danielpecos.gtdtm.views.TaskViewHolder;
 import com.danielpecos.gtdtm.views.ViewHolder;
 import com.google.ads.AdRequest;
 import com.google.ads.AdView;
 import com.google.android.maps.GeoPoint;
 
 public class ContextActivity extends ExpandableListActivity implements ExpandableListView.OnChildClickListener {
 	private TaskManager taskManager;
 
 	private ViewHolder triggerViewHolder;
 
 	private HashMap<Long, ContextViewHolder> contextViewHolders;
 	private HashMap<Long, ProjectViewHolder> projectViewHolders;
 	private HashMap<Long, TaskViewHolder> taskViewHolders;
 
 	private HashMap<Long, Boolean> listViewStatus = new HashMap<Long, Boolean>();
 
 	private ExpandableNestedMixedListAdapter mAdapter;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_layout_context);
 
 		// Admob banner
 		AdView adView = (AdView)this.findViewById(R.id.adView);
 		if (!TaskManager.isFullVersion(this)) {
 			AdRequest adRequest = new AdRequest();
 			adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
 			adView.loadAd(adRequest);
 		} else {
 			adView.setVisibility(View.GONE);
 		}
 
 		//		Log.d(TaskManager.TAG, "Settings: Fijo los valores por defecto");
 		PreferenceManager.setDefaultValues(this.getApplicationContext(), R.xml.preferences, false);
 
 		this.taskManager = TaskManager.getInstance(this);
 
 		if (this.taskManager.getContexts().size() == 0) {
 			new AlertDialog.Builder(this)
 			.setIcon(android.R.drawable.ic_dialog_info)
 			.setTitle(R.string.context_loadDemo_title)
 			.setMessage(R.string.context_loadDemo_message)
 			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					loadDemoData();
 					initializeUI();
 				}
 			})
 			.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					initializeUI();
 				}
 			})
 			.show();
 
 		} else {
 			//			this.loadDemoData();
 			this.initializeUI();
 		}
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.options_menu_context_activity, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.context_optionsMenu_addContext:
 			ActivityUtils.showTextBoxDialog(
 					this, 
 					this.getResources().getString(R.string.textbox_addContext_title), 
 					this.getResources().getString(R.string.textbox_addContext_label), 
 					null,
 					new OnDismissListener() {
 						@Override
 						public void onDismiss(DialogInterface dialog) {
 							String contextName = ((EditText)((Dialog)dialog).findViewById(R.id.textbox_text)).getText().toString();
 							if (taskManager.createContext(ContextActivity.this, contextName) != null) {
 								initializeUI();
 							} else {
 								Toast.makeText(ContextActivity.this, R.string.error_creatingContext, Toast.LENGTH_SHORT).show();
 							}
 						}
 					});
 			break;
 		case R.id.context_optionsMenu_reloadData:
 			taskManager = TaskManager.reset(ContextActivity.this);
 			initializeUI();
 			break;
 		case R.id.context_optionsMenu_preferences:
 			Intent i = new Intent(this, PreferencesActivity.class);  
 			startActivityForResult(i, ActivityUtils.PREFERENCES_ACTIVITY);
 			break;
 		case R.id.context_optionsMenu_about:
 			i = new Intent(this, AboutActivity.class);  
 			startActivity(i);
 			break;
 		}
 		return true;
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 
 		MenuInflater inflater = getMenuInflater();
 
 		int itemId = ((ExpandableListContextMenuInfo) menuInfo).targetView.getId();
 
 		if (itemId == R.id.context_item) {
 			inflater.inflate(R.menu.context_menu_context_item, menu);
 			menu.setHeaderTitle(R.string.context_contextMenu_contextTitle);
 			menu.setHeaderIcon(android.R.drawable.ic_menu_agenda);
 		} if (itemId == R.id.project_item) {
 			inflater.inflate(R.menu.context_menu_project_item, menu);
 			menu.setHeaderTitle(R.string.context_contextMenu_projectTitle);
 			menu.setHeaderIcon(R.drawable.ic_menu_archive);
 		} else if (itemId == R.id.task_item) {
 			inflater.inflate(R.menu.context_menu_task_item, menu);
 			menu.setHeaderTitle(R.string.context_contextMenu_taskTitle);
 			menu.setHeaderIcon(R.drawable.ic_menu_mark);
 		}
 
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 
 		ExpandableListContextMenuInfo  menuInfo = (ExpandableListContextMenuInfo) item.getMenuInfo();
 		int type = ExpandableListView.getPackedPositionType(menuInfo.packedPosition);
 
 		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
 			int groupPos = ExpandableListView.getPackedPositionGroup(menuInfo.packedPosition); 
 			int childPos = ExpandableListView.getPackedPositionChild(menuInfo.packedPosition); 
 
 			final Context context = taskManager.elementAt(groupPos);
 			final Object child = context.elementAt(childPos);
 
 			switch (item.getItemId()) {
 			case R.id.context_contextMenu_deleteProject: {
 				final Project project = (Project)child;
 				ActivityUtils.createConfirmDialog(this, R.string.confirm_delete_project).setPositiveButton(R.string.yes, new Dialog.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						if (context.deleteProject(ContextActivity.this, project)) {
 							initializeUI();
 							Toast.makeText(ContextActivity.this, "Project \"" + project.getName() + "\" successfully deleted", Toast.LENGTH_SHORT).show();
 						} else {
 							Toast.makeText(ContextActivity.this, R.string.error_deletingProject, Toast.LENGTH_SHORT).show();
 						}
 					}
 				}).show();
 
 				return true;
 			}
 			case R.id.context_contextMenu_deleteTask: {
 				final Task task = (Task)child;
 				ActivityUtils.createConfirmDialog(this, R.string.confirm_delete_task).setPositiveButton(R.string.yes, new Dialog.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						if (context.deleteTask(ContextActivity.this, task)) {
 							initializeUI();
 							Toast.makeText(ContextActivity.this, "Task \"" + task.getName() + "\" successfully deleted", Toast.LENGTH_SHORT).show();
 						} else {
 							Toast.makeText(ContextActivity.this, R.string.error_deletingTask, Toast.LENGTH_SHORT).show();
 						}
 					}
 				}).show();
 
 				return true;
 			}
 
 			case R.id.context_contextMenu_addTask: {
 				final Project project = (Project)child;
 
 				ActivityUtils.showTextBoxDialog(
 						this, 
 						this.getResources().getString(R.string.textbox_addTask_title), 
 						this.getResources().getString(R.string.textbox_addTask_label), 
 						null,
 						new OnDismissListener() {
 							@Override
 							public void onDismiss(DialogInterface dialog) {
 								String taskName = ((EditText)((Dialog)dialog).findViewById(R.id.textbox_text)).getText().toString();
 								if (project.createTask(ContextActivity.this, taskName, null, Task.Priority.Normal) != null) {
 									initializeUI();
 								} else {
 									Toast.makeText(ContextActivity.this, R.string.error_creatingTask, Toast.LENGTH_SHORT).show();
 								}
 							}
 						});
 				return true;
 			}
 			case R.id.context_contextMenu_editTask: {
 				final Task task = (Task)child;
 
 				this.triggerViewHolder = this.taskViewHolders.get(task.getId());
 				ActivityUtils.showTaskActivity(this, context, null, task);
 
 				return true;
 			}
 			}
 
 			return false;
 
 		} else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
 
 			final int groupPos = ExpandableListView.getPackedPositionGroup(menuInfo.packedPosition); 
 
 			final Context context = taskManager.elementAt(groupPos);
 
 			switch(item.getItemId()) {
 			case R.id.context_contextMenu_renameContext: {
 				ActivityUtils.showTextBoxDialog(
 						this, 
 						this.getResources().getString(R.string.textbox_renameContext_title), 
 						this.getResources().getString(R.string.textbox_renameContext_label), 
 						context.getName(),
 						new OnDismissListener() {
 							@Override
 							public void onDismiss(DialogInterface dialog) {
 								String contextName = ((EditText)((Dialog)dialog).findViewById(R.id.textbox_text)).getText().toString();
 								context.setName(ContextActivity.this, contextName);
 								initializeUI();
 							}
 						});
 				return true;
 			}
 			case R.id.context_contextMenu_deleteContext: {
 				ActivityUtils.createConfirmDialog(this, R.string.confirm_delete_context).setPositiveButton(R.string.yes, new Dialog.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						if (taskManager.deleteContext(ContextActivity.this, context)) {
 							if (listViewStatus.containsKey(context.getId())) {
 								listViewStatus.remove(context.getId());
 							}
 							initializeUI();
 							Toast.makeText(ContextActivity.this, "Context \"" + context.getName() + "\" successfully deleted", Toast.LENGTH_SHORT).show();
 						} else {
 							Toast.makeText(ContextActivity.this, R.string.error_deletingContext, Toast.LENGTH_SHORT).show();
 						}
 					}
 				}).show();
 				return true;
 			}
 			case R.id.context_contextMenu_addProject: {
 				ActivityUtils.showTextBoxDialog(
 						this, 
 						this.getResources().getString(R.string.textbox_addProject_title), 
 						this.getResources().getString(R.string.textbox_addProject_label), 
 						null,
 						new OnDismissListener() {
 							@Override
 							public void onDismiss(DialogInterface dialog) {
 								String projectName = ((EditText)((Dialog)dialog).findViewById(R.id.textbox_text)).getText().toString();
 								if (context.createProject(ContextActivity.this, projectName, null) != null) {
 									initializeUI();
 								} else {
 									Toast.makeText(ContextActivity.this, R.string.error_creatingProject, Toast.LENGTH_SHORT).show();
 								}
 							}
 						});
 				return true;
 			}
 			case R.id.context_contextMenu_addTask: {
 				ActivityUtils.showTextBoxDialog(
 						this, 
 						this.getResources().getString(R.string.textbox_addTask_title), 
 						this.getResources().getString(R.string.textbox_addTask_label), 
 						null,
 						new OnDismissListener() {
 							@Override
 							public void onDismiss(DialogInterface dialog) {
 								String taskName = ((EditText)((Dialog)dialog).findViewById(R.id.textbox_text)).getText().toString();
 								if (context.createTask(ContextActivity.this, taskName, null, Task.Priority.Normal) != null) {
 									initializeUI();
 								} else {
 									Toast.makeText(ContextActivity.this, R.string.error_creatingTask, Toast.LENGTH_SHORT).show();
 								}
 							}
 						});
 				return true;
 			}
 			case R.id.context_contextMenu_synchronizeGTasks: {
 				synchronizeGoogleTasks(context);
 
 				return true;
 			}			
 			}
 			return false;
 		}
 
 		return false;
 	}
 
 	@Override
 	public void onGroupCollapse(int groupPosition) {
 		super.onGroupCollapse(groupPosition);
 		listViewStatus.put(taskManager.elementAt(groupPosition).getId(), false);
 	}
 
 	@Override
 	public void onGroupExpand(int groupPosition) {
 		super.onGroupExpand(groupPosition);
 		listViewStatus.put(taskManager.elementAt(groupPosition).getId(), true);
 	}
 
 	private void initializeUI() {
 
 		ExpandableListView listView = this.getExpandableListView();
 
 		this.contextViewHolders = new HashMap<Long, ContextViewHolder>();
 		this.projectViewHolders = new HashMap<Long, ProjectViewHolder>();
 		this.taskViewHolders = new HashMap<Long, TaskViewHolder>();
 
 		if (taskManager != null) {
 			ArrayList<HashMap<String, Object>> groupData = new ArrayList<HashMap<String, Object>>();
 			ArrayList<ArrayList<HashMap<String, Object>>> childrenData_projects =  new ArrayList<ArrayList<HashMap<String, Object>>>();
 			ArrayList<ArrayList<HashMap<String, Object>>> childrenData_tasks =  new ArrayList<ArrayList<HashMap<String, Object>>>();
 
 			Collection<Context> contexts = taskManager.getContexts();
 
 			for (Context ctx : contexts) {
 				ContextViewHolder cvh = new ContextViewHolder(null, ctx);
 				this.contextViewHolders.put(ctx.getId(), cvh);
 				groupData.add(cvh.getListFields());
 
 				// PROJECTS LIST
 				ArrayList<HashMap<String, Object>> contextChildData = new ArrayList<HashMap<String,Object>>();
 				for (Project project : ctx.getProjects()) {
 					ProjectViewHolder pvh = new ProjectViewHolder(null, project);
 					this.projectViewHolders.put(project.getId(), pvh);
 					HashMap<String, Object> projectData = pvh.getListFields();
 					contextChildData.add(projectData);
 				}
 				childrenData_projects.add(contextChildData);
 
 				// TASKS LIST
 				contextChildData = new ArrayList<HashMap<String,Object>>();
 				for (final Task task : ctx) {
 
 					TaskViewHolder tvh = new TaskViewHolder(null, task);
 					taskViewHolders.put(task.getId(), tvh);
 
 					HashMap<String, Object> taskData = tvh.getListFields();
 
 					contextChildData.add(taskData);
 				}
 				childrenData_tasks.add(contextChildData);
 			}
 
 			this.mAdapter = new ExpandableNestedMixedListAdapter(
 					this, 
 					groupData, 
 					R.layout.item_context, 
 					new String[] {"name"}, 
 					new int[] {R.id.context_name}, 
 					new RowDisplayListener() {
 						@Override
 						public void onViewSetUp(View view, HashMap<String, Object> data) {
 							Context context = (Context)data.get("_BASE_");
 							ViewHolder cvh = contextViewHolders.get(context.getId());
 							cvh.setView(view);
 							cvh.updateView(ContextActivity.this);
 						}
 					},
 
 					childrenData_projects, 
 					R.layout.item_project, 
 					new String[] {"name", "description", "status_text", "status_icon"},
 					new int[] {R.id.project_name, R.id.project_description, R.id.project_status_text, R.id.project_status_icon},
 					new RowDisplayListener() {
 						@Override
 						public void onViewSetUp(View view, HashMap<String, Object> data) {
 							Project project = (Project)data.get("_BASE_");
 							ViewHolder tvh = projectViewHolders.get(project.getId());
 							tvh.setView(view);
 							tvh.updateView(ContextActivity.this);
 						}
 					},
 
 					childrenData_tasks, 
 					R.layout.item_task, 
 					new String[] {"name", "description", "status_check"},
 					new int[] {R.id.task_name, R.id.task_description, R.id.task_status_check}, 
 					new RowDisplayListener() {
 						@Override
 						public void onViewSetUp(View view, HashMap<String, Object> data) {
 							Task task = (Task)data.get("_BASE_");
 							view.findViewById(R.id.task_status_check).setClickable(false);
 							ViewHolder tvh = taskViewHolders.get(task.getId());
 							tvh.setView(view);
 							tvh.updateView(ContextActivity.this);
 						}
 					}
 			);
 			this.setListAdapter(mAdapter);
 
 			if (listViewStatus != null && listViewStatus.size() > 0) {
 				int i = 0;
 				for (Context c: contexts) {
 					if (listViewStatus.containsKey(c.getId())) {
 						if (listViewStatus.get(c.getId())) {
 							Log.d(TaskManager.TAG, "Expanding group " + i);
 							listView.expandGroup(i);
 						}
 					}
 					i++;
 				}
 			}
 		}
 
 		this.registerForContextMenu(listView);
 
 		listView.setOnChildClickListener(this);
 	}
 
 
 	private void loadDemoData() {
 		// Test data 
 
 		Context ctx = this.taskManager.createContext(this, "Context 1");
 		Project prj = ctx.createProject(this, "Project 1.1", "Project 1.1 description");
 		prj.createTask(this, "Task 1", "Task number 1.1.1", Task.Priority.Critical);
 		prj.createTask(this, "Task 2", "Task number 1.1.2", Task.Priority.Important);
 		prj.createTask(this, "Task 3", "Task number 1.1.3", Task.Priority.Low);
 		prj.createTask(this, "Task 4", "Task number 1.1.4", Task.Priority.Important);
 		prj.createTask(this, "Task 5", "Task number 1.1.5", Task.Priority.Critical);
 
 		ctx.createTask(this, "Task 1", "Task number 1.0.1", Task.Priority.Critical);
 		ctx.createTask(this, "Task 2", "Task number 1.0.2", Task.Priority.Important);
 		ctx.createTask(this, "Task 3", "Task number 1.0.3", Task.Priority.Normal).setLocation(new GeoPoint(40000000, 0));
 		ctx.createTask(this, "Task 4", "Task number 1.0.4", Task.Priority.Low);
 
 		prj = ctx.createProject(this, "Project 1.2", "Project 1.2 description");
 		prj.createTask(this, "Task 1", "Task number 1.2.1", Task.Priority.Critical);
 		prj.createTask(this, "Task 2", "Task number 1.2.2", Task.Priority.Important);
 		prj.createTask(this, "Task 3", "Task number 1.2.3", Task.Priority.Low);
 		prj.createTask(this, "Task 4", "Task number 1.2.4", Task.Priority.Important);
 		prj.createTask(this, "Task 5", "Task number 1.2.5", Task.Priority.Critical);
 
 		this.taskManager.createContext(this, "Context 2");
 		this.taskManager.createContext(this, "Context 3");
 
 	}
 
 
 	@Override
 	public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
 		boolean result = super.onChildClick(parent, view, groupPosition, childPosition, id);
 
 		Context ctx = taskManager.elementAt(groupPosition);
 		Object child = ctx.elementAt(childPosition);
 
 		if (child instanceof Project) {
 			Project project = (Project) child;
 			this.triggerViewHolder = this.projectViewHolders.get(project.getId());
 			ActivityUtils.showProjectActivity(this, ctx, project);
 		} else {
 			Task task = (Task) child;
 			if (task.getStatus() == Status.Active || task.getStatus() == Status.Completed) {
 				((CheckBox)this.taskViewHolders.get(task.getId()).getView(R.id.task_status_check)).toggle();
 
 				if (task.store(view.getContext()) < 0) {
 					Toast.makeText(view.getContext(), "Problems updating task", Toast.LENGTH_SHORT).show();
 				}
 			}
 		}
 
 		return result;
 	}
 
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == ActivityUtils.PROJECT_ACTIVITY) {
 			if (this.triggerViewHolder != null) {
 				ProjectViewHolder projectViewHolder = (ProjectViewHolder) this.triggerViewHolder;
 				projectViewHolder.updateView(this);
 			}
 		} else if (requestCode == ActivityUtils.TASK_ACTIVITY) {
 			if (resultCode == RESULT_OK) {
 				// task saved
 				if (data.getBooleanExtra(TaskActivity.FULL_RELOAD, false)) {
 					this.initializeUI();
 				} else {
 					TaskViewHolder taskViewHolder = (TaskViewHolder) this.triggerViewHolder;
 					taskViewHolder.updateView(this);
 				}
 			} else if (this.triggerViewHolder != null) {
 				// always refresh view, its status may change 
 				TaskViewHolder taskViewHolder = (TaskViewHolder) this.triggerViewHolder;
 				taskViewHolder.updateView(this);
 			}
 		} else if (requestCode == ActivityUtils.GOOGLE_ACCOUNT_ACTIVITY) {
 			if (resultCode == RESULT_OK) {
 				Long contextId = data.getLongExtra("context_id", -1);
 				Context context = taskManager.getContext(contextId);
 
 				synchronizeGoogleTasks(context);
 			}
 		} else if (requestCode == ActivityUtils.PREFERENCES_ACTIVITY) {
 			if (resultCode == RESULT_OK) {
 				this.initializeUI();
 			}
 		}
 
 		this.triggerViewHolder = null;
 	}
 
 	private void synchronizeGoogleTasks(final Context context) {
 		GoogleTasksClientAsyncTask googleTasksClientAsyncTask = new GoogleTasksClientAsyncTask(this, context, this.contextViewHolders.get(context.getId()));
 		googleTasksClientAsyncTask.execute();
 	}
 }
