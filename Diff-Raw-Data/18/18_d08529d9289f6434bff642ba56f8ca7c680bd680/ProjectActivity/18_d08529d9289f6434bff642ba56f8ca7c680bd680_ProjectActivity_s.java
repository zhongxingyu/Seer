 package com.danielpecos.gtdtm.activities;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnDismissListener;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 
 import com.danielpecos.gtdtm.R;
 import com.danielpecos.gtdtm.model.TaskManager;
 import com.danielpecos.gtdtm.model.beans.Context;
 import com.danielpecos.gtdtm.model.beans.Project;
 import com.danielpecos.gtdtm.model.beans.Task;
 import com.danielpecos.gtdtm.utils.ActivityUtils;
 import com.danielpecos.gtdtm.utils.ExpandableNestedMixedListAdapter.RowDisplayListener;
 import com.danielpecos.gtdtm.utils.SimpleListAdapter;
 import com.danielpecos.gtdtm.views.OnCheckedChangeListener;
 import com.danielpecos.gtdtm.views.ProjectViewHolder;
 import com.danielpecos.gtdtm.views.TaskViewHolder;
 import com.danielpecos.gtdtm.views.ViewHolder;
 
 public class ProjectActivity extends ListActivity {
 
 	private TaskManager taskManager;
 	private Context context;
 	private Project project;
 
 	private ViewHolder triggerViewHolder;
 
 	private ProjectViewHolder projectViewHolder;
 	private HashMap<Long, TaskViewHolder> taskViewHolders;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		this.taskManager = TaskManager.getInstance(this);
 
 		Long context_id = (Long) getIntent().getSerializableExtra("context_id");
 		Long project_id = (Long) getIntent().getSerializableExtra("project_id");
 
 		this.context = this.taskManager.getContext(context_id);
 		this.project = this.context.getProject(project_id);
 
 		this.initializeUI();
 
 		setResult(RESULT_OK, getIntent());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.options_menu_project_activity, menu);
 		return true;
 	}
 
 
 	private void initializeUI() {
 		setContentView(R.layout.activity_layout_project);
 
 		this.setTitle(project.getName());
 
 		// PROJECT 
 		LayoutInflater mInflater = (LayoutInflater) getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
 
 		View projectItemView = mInflater.inflate(R.layout.item_project, null);
 		projectItemView.setMinimumHeight(projectItemView.getMeasuredHeight() + 8);
 		projectItemView.setPadding(0, 6, 10, 0);
 
 		LinearLayout header = (LinearLayout)findViewById(R.id.project_header);
 		header.addView(projectItemView);
 
 		this.projectViewHolder = new ProjectViewHolder(projectItemView, project);
 
 		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)findViewById(R.id.project_icons).getLayoutParams();
 		lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
 		lp.addRule(RelativeLayout.RIGHT_OF, RelativeLayout.NO_ID);
 		lp.rightMargin = 0;
 		findViewById(R.id.project_icons).setLayoutParams(lp);
 		
 		findViewById(R.id.project_chevron).setVisibility(View.GONE);
 
 		projectItemView.requestLayout();
 		projectViewHolder.updateView(this);
 
 		//projectItemView.setTag(projectViewHolder);
 
 		// TASKS LIST
 		this.taskViewHolders = new HashMap<Long, TaskViewHolder>();
 
 		ArrayList<HashMap<String, Object>> itemsData = new ArrayList<HashMap<String, Object>>();
 		for (final Task task : project) {
 
 			TaskViewHolder tvh = new TaskViewHolder(null, task);
 
 			tvh.registerChainedFieldEvents(R.id.task_status_check, new Object[] {
 					new OnCheckedChangeListener() {
 						@Override
 						public void onCheckedChanged(View buttonView,	boolean isChecked) {
 							if (task.store(ProjectActivity.this) < 0) {
 								Toast.makeText(ProjectActivity.this, "Problems updating task", Toast.LENGTH_SHORT).show();
 							}
 							if (projectViewHolder != null) {
 								projectViewHolder.updateView(ProjectActivity.this);
 							}
 						}
 					}
 			});
 			taskViewHolders.put(task.getId(), tvh);
 
 			HashMap<String, Object> taskData = tvh.getListFields();
 
 			itemsData.add(taskData);
 		}
 
 		SimpleListAdapter adapter = new SimpleListAdapter(
 				this,
 				itemsData, 
 				R.layout.item_task, 
 				new String[] {"name", "description"},
 				new int[] {R.id.task_name, R.id.task_description},
 				new RowDisplayListener() {
 					@Override
 					public void onViewSetUp(View view, HashMap<String, Object> data) {
 						Task task = (Task)data.get("_BASE_");
 //						view.findViewById(R.id.task_status_check).setClickable(false);
 						ViewHolder tvh = taskViewHolders.get(task.getId());
 						tvh.setView(view);
 						tvh.updateView(ProjectActivity.this);
 					}
 				}
 		);
 
 		this.setListAdapter(adapter);

 		this.registerForContextMenu(this.getListView());
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.project_optionsMenu_renameProject: {
 			ActivityUtils.showTextBoxDialog(
 					this, 
 					this.getResources().getString(R.string.textbox_renameProject_title), 
 					this.getResources().getString(R.string.textbox_renameProject_label), 
 					project.getName(),
 					false,
 					new OnDismissListener() {
 						@Override
 						public void onDismiss(DialogInterface dialog) {
 							String projectName = ((EditText)((Dialog)dialog).findViewById(R.id.textbox_text)).getText().toString();
 							project.setName(projectName);
 							project.store(ProjectActivity.this);
 							projectViewHolder.updateView(ProjectActivity.this);
 						}
 					});
 			break;
 		}
 		case R.id.project_optionsMenu_changeDescription: {
 			ActivityUtils.showTextBoxDialog(
 					this, 
 					this.getResources().getString(R.string.textbox_changeProjectDescription_title), 
 					this.getResources().getString(R.string.textbox_changeProjectDescription_label), 
 					project.getDescription(),
 					true,
 					new OnDismissListener() {
 						@Override
 						public void onDismiss(DialogInterface dialog) {
 							String projectDescription = ((EditText)((Dialog)dialog).findViewById(R.id.textbox_text)).getText().toString();
 							if (projectDescription.equalsIgnoreCase("")) {
 								projectDescription = null;
 							}
 							project.setDescription(projectDescription);
 							project.store(ProjectActivity.this);
 							projectViewHolder.updateView(ProjectActivity.this);
 						}
 					});
 			break;
 		}
 		case R.id.project_optionsMenu_addTask: {
 
 			ActivityUtils.showTextBoxDialog(
 					this, 
 					this.getResources().getString(R.string.textbox_addTask_title), 
 					this.getResources().getString(R.string.textbox_addTask_label), 
 					null,
 					false,
 					new OnDismissListener() {
 						@Override
 						public void onDismiss(DialogInterface dialog) {
 							String taskName = ((EditText)((Dialog)dialog).findViewById(R.id.textbox_text)).getText().toString();
 							if (project.createTask(ProjectActivity.this, taskName, null, Task.Priority.Normal) != null) {
 								initializeUI();
 							} else {
 								Toast.makeText(ProjectActivity.this, R.string.error_creatingTask, Toast.LENGTH_SHORT).show();
 							}
 						}
 					});
 			break;
 		}
 		}
 		return true;
 	}
 
 	@Override
 	public void onListItemClick(ListView parent, View view, int position, long id) {
 		super.onListItemClick(parent, view, position, id);
 
 		Task task = project.elementAt(position);
 		this.triggerViewHolder = this.taskViewHolders.get(task.getId());
 		ActivityUtils.showTaskActivity(this, context, project, task);
 		
 		/*if (task.getStatus() == Status.Active || task.getStatus() == Status.Completed) {
 			((CheckBox)this.taskViewHolders.get(task.getId()).getView(R.id.task_status_check)).toggle();
 
 			if (task.store(view.getContext()) < 0) {
 				Toast.makeText(view.getContext(), "Problems updating task", Toast.LENGTH_SHORT).show();
 			}
 		}*/
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == ActivityUtils.TASK_ACTIVITY) {
 			Log.d(TaskManager.TAG, "ProjectActivity: Returning from the task activity");
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
 			projectViewHolder.updateView(this);
 		}
 
 		this.triggerViewHolder = null;
 	}
 
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.context_menu_task_item, menu);
 
 		menu.setHeaderTitle(R.string.context_contextMenu_taskTitle);
 		menu.setHeaderIcon(R.drawable.ic_menu_mark);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 
 		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
 		final Task task = project.elementAt(info.position);
 
 		switch (item.getItemId()) {
 		case R.id.context_contextMenu_deleteTask: {
 
 			ActivityUtils.createConfirmDialog(this, R.string.confirm_delete_task).setPositiveButton(R.string.yes, new Dialog.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					if (project.deleteTask(ProjectActivity.this, task)) {
 						initializeUI();
 						Toast.makeText(ProjectActivity.this, String.format(getString(R.string.result_delete_task), task.getName()), Toast.LENGTH_SHORT).show();
 					} else {
 						Toast.makeText(ProjectActivity.this, R.string.error_deletingTask, Toast.LENGTH_SHORT).show();
 					}
 				}
 			}).show();
 			return true;
 		}
 		}
 
 		return false;
 
 	}
 
 
 }
