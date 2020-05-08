 package com.mitrejcevski.widget.activity;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 import com.mitrejcevski.widget.R;
 import com.mitrejcevski.widget.adapter.SectionsPagerAdapter;
 import com.mitrejcevski.widget.adapter.TasksListAdapter;
 import com.mitrejcevski.widget.database.DatabaseManipulator;
 import com.mitrejcevski.widget.fragment.TaskListFragment;
 import com.mitrejcevski.widget.model.Group;
 import com.mitrejcevski.widget.model.MyTask;
 import com.mitrejcevski.widget.provider.ListWidget;
 
 import java.util.ArrayList;
 
 /**
  * The main activity of the application.
  * 
  * @author jovche.mitrejchevski
  */
 public class MainActivity extends FragmentActivity {
 
 	public static final int REQUEST_CODE_NEW_STUFF = 5;
 	private SectionsPagerAdapter mSectionsPagerAdapter;
 	private ViewPager mViewPager;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		// Create the adapter that will return a fragment for each of the
 		// groups.
 		mSectionsPagerAdapter = new SectionsPagerAdapter(
 				getSupportFragmentManager());
 		// Set up the ViewPager with the sections adapter.
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(mSectionsPagerAdapter);
 	}
 
 	@Override
 	protected void onResume() {
 		update();
 		super.onResume();
 	}
 
 	/**
 	 * Initialize the groups and send them to the pager adapter. Also refreshes
 	 * the view pager.
 	 */
 	private void update() {
		mSectionsPagerAdapter.notifyDataSetChanged();
 		ArrayList<Group> allGroups = DatabaseManipulator.INSTANCE
 				.getAllGroups(this);
 		if (allGroups.size() == 0) {
 			Group group = new Group();
 			group.setGroupTitle(getString(R.string.default_tab_name));
 			DatabaseManipulator.INSTANCE.saveGroup(this, group);
 			allGroups.add(group);
 		}
 		mSectionsPagerAdapter.setGroups(allGroups);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.menu_settings:
 			Toast.makeText(this, R.string.menu_settings, Toast.LENGTH_SHORT)
 					.show();
 			return true;
 		case R.id.menu_delete_done_items:
 			askForDeleting();
 			return true;
 		case R.id.menu_add_group:
 			openNewGroupActivity();
 			return true;
 		case R.id.menu_add_task:
 			openNewTaskActivity();
 			return true;
 		case R.id.menu_group_editor:
 			openGroupsEditor();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	/**
 	 * Opens a dialog for accepting the deletion.
 	 */
 	private void askForDeleting() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle(R.string.delete_dialog_title);
 		builder.setMessage(R.string.delete_dialog_messaeg);
 		builder.setPositiveButton(R.string.accept_button_label,
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						deleteDoneTasks();
 						dialog.dismiss();
 					}
 				});
 		builder.setNegativeButton(R.string.reject_button_label,
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.dismiss();
 					}
 				});
 		builder.create().show();
 	}
 
 	/**
 	 * Deletes all the marked tasks inside the current group.
 	 */
 	private void deleteDoneTasks() {
 		Group group = mSectionsPagerAdapter.getGroupItem(mViewPager
 				.getCurrentItem());
 		DatabaseManipulator.INSTANCE.deleteDoneTasks(this,
 				group.getGroupTitle());
 		notifyAdaptersForDataChanged();
 		notifyWidget();
 	}
 
 	/**
 	 * Opens the group management activity.
 	 */
 	private void openGroupsEditor() {
 		Intent intent = new Intent(this, GroupsListActivity.class);
 		startActivityForResult(intent, REQUEST_CODE_NEW_STUFF);
 	}
 
 	/**
 	 * Opens the activity for adding/editing tasks.
 	 */
 	private void openNewTaskActivity() {
 		Intent intent = new Intent(this, NewItemActivity.class);
 		Group group = mSectionsPagerAdapter.getGroupItem(mViewPager
 				.getCurrentItem());
 		intent.putExtra(NewItemActivity.GROUP_EXTRA, group.getGroupTitle());
 		startActivityForResult(intent, REQUEST_CODE_NEW_STUFF);
 	}
 
 	/**
 	 * Opens activity for quick group adding.
 	 */
 	private void openNewGroupActivity() {
 		Intent intent = new Intent(this, NewGroupActivity.class);
 		startActivity(intent);
 	}
 
 	/**
 	 * Looking for the task at a specific position in the current group, and
 	 * sends it for editing.
 	 * 
 	 * @param position
 	 */
 	public void editTask(int position) {
 		Fragment current = mSectionsPagerAdapter.getFragmentItem(mViewPager
 				.getCurrentItem());
 		TasksListAdapter taskAdapter = ((TaskListFragment) current)
 				.getListAdapter();
 		MyTask currentTask = taskAdapter.getItem(position);
 		Group group = mSectionsPagerAdapter.getGroupItem(mViewPager
 				.getCurrentItem());
 		openTaskEditor(currentTask, group);
 	}
 
 	/**
 	 * Opens an activity for adding/editing tasks.
 	 * 
 	 * @param model
 	 *            Task that has to be edited.
 	 * @param group
 	 *            Group where this task belongs.
 	 */
 	private void openTaskEditor(MyTask model, Group group) {
 		Intent intent = new Intent(this, NewItemActivity.class);
 		intent.putExtra(NewItemActivity.MY_TASK_EXTRA, model.getId());
 		intent.putExtra(NewItemActivity.GROUP_EXTRA, group.getGroupTitle());
 		startActivityForResult(intent, REQUEST_CODE_NEW_STUFF);
 	}
 
 	/**
 	 * Notifies the view pager and the adapter of the current opened page to
 	 * reload the data.
 	 */
 	private void notifyAdaptersForDataChanged() {
 		mSectionsPagerAdapter.notifyDataSetChanged();
 		TaskListFragment current = (TaskListFragment) mSectionsPagerAdapter
 				.getFragmentItem(mViewPager.getCurrentItem());
 		current.loadData();
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == REQUEST_CODE_NEW_STUFF) {
 			if (resultCode == RESULT_OK) {
 				notifyAdaptersForDataChanged();
 			}
 		}
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	/**
 	 * Notifies the widget to reload the data.
 	 */
 	public void notifyWidget() {
 		final Intent fillInIntent = new Intent(this, ListWidget.class);
 		fillInIntent.setAction(ListWidget.UPDATE_ACTION);
 		sendBroadcast(fillInIntent);
 	}
 }
