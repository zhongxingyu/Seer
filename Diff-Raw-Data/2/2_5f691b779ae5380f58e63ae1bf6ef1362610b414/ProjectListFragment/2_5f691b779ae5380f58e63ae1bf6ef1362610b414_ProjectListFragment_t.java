 
 package se.slide.timy;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ListView;
 
 import se.slide.timy.db.DatabaseManager;
 import se.slide.timy.model.Project;
 
 import java.util.List;
 
 public class ProjectListFragment extends Fragment implements AdapterView.OnItemClickListener {
 
     private static final String TAG = "ProjectListFragment";
 
     public static final String EXTRA_ID = "id";
 
     public static final int MENU_EDIT = 0;
     public static final int MENU_REMOVE = 1;
 
     private int mId;
     private ResponseReceiver mReceiver;
     private ProjectArrayAdapter mAdapter;
     private ProjectListInterface mActivity;
     private ListView mListView;
 
     public interface ProjectListInterface {
         public boolean hasProjectsChanged();
 
     }
 
     public static final ProjectListFragment getInstance(int id) {
         ProjectListFragment fragment = new ProjectListFragment();
         Bundle bdl = new Bundle(2);
         bdl.putInt(EXTRA_ID, id);
         fragment.setArguments(bdl);
 
         return fragment;
     }
 
     public ProjectListFragment() {
 
     }
 
     /*
      * (non-Javadoc)
      * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
      */
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
 
         mActivity = (ProjectListInterface) activity;
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
 
         mReceiver = new ResponseReceiver();
     }
 
     @Override
     public void onResume() {
         super.onResume();
 
         if (mActivity.hasProjectsChanged())
             resetAdapter();
 
         registerReceiver();
 
     }
 
     /*
      * (non-Javadoc)
      * @see android.support.v4.app.Fragment#onDetach()
      */
     @Override
     public void onDetach() {
         super.onDetach();
 
     }
 
     /*
      * (non-Javadoc)
      * @see android.support.v4.app.Fragment#onStop()
      */
     @Override
     public void onStop() {
         super.onStop();
 
     }
 
     @Override
     public void onPause() {
         super.onPause();
         unregisterReceiver();
     }
 
     /*
      * (non-Javadoc)
      * @see android.support.v4.app.Fragment#onStart()
      */
     @Override
     public void onStart() {
         super.onStart();
 
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
 
         View view = (View) inflater.inflate(R.layout.project_listview, null);
 
         mListView = (ListView) view.findViewById(android.R.id.list);
         mListView.setOnItemClickListener(this);
         mListView.setEmptyView(view.findViewById(R.id.empty_list_view));
         
         mId = getArguments().getInt(EXTRA_ID);
 
        registerForContextMenu(mListView);
 
         attachAdapter();
 
         return view;
     }
 
     /*
      * (non-Javadoc)
      * @see
      * android.support.v4.app.Fragment#onContextItemSelected(android.view.MenuItem
      * )
      */
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         // Are we on the correct listview, check the groupId which should match
         // the mId we set in menu.add
         if (item.getGroupId() == mId) {
             AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
                     .getMenuInfo();
             final Project project = mAdapter.getItem(info.position);
 
             int menuItemIndex = item.getItemId();
 
             if (menuItemIndex == MENU_EDIT) {
                 //
                 Intent i = new Intent(getActivity(), ProjectActivity.class)
                         .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 i.putExtra(ProjectActivity.EXTRA_PROJECT_ID, project.getId());
                 startActivityForResult(i, ProjectActivity.ACTIVITY_CODE);
             }
             else if (menuItemIndex == MENU_REMOVE) {
                 // Ask to hide or delete
                 AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                 builder.setTitle(R.string.delete_or_hide_title)
                         .setPositiveButton(R.string.ok, new OnClickListener() {
 
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 int selectedPosition = ((AlertDialog) dialog).getListView()
                                         .getCheckedItemPosition();
                                 if (selectedPosition == 0) {
                                     // Hide
                                     project.setActive(false);
                                     DatabaseManager.getInstance().updateProject(project);
                                 }
                                 else {
                                     // Delete
                                     DatabaseManager.getInstance().deleteProjectAndItsReports(
                                             project);
                                 }
 
                                 mAdapter.clear();
                                 mAdapter.addAll(DatabaseManager.getInstance().getAllProjects(mId));
                                 mAdapter.notifyDataSetChanged();
                             }
                         })
                         .setNegativeButton(R.string.cancel, new OnClickListener() {
 
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 dialog.dismiss();
                             }
                         })
                         .setSingleChoiceItems(
                                 getResources().getStringArray(R.array.delete_or_hide), 0,
                                 new DialogInterface.OnClickListener() {
                                     public void onClick(DialogInterface dialogInterface, int item) {
 
                                     }
                                 });
 
                 builder.create().show();
 
             }
         }
 
         return super.onContextItemSelected(item);
     }
 
     /*
      * (non-Javadoc)
      * @see
      * android.support.v4.app.Fragment#onCreateContextMenu(android.view.ContextMenu
      * , android.view.View, android.view.ContextMenu.ContextMenuInfo)
      */
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
 
         AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
         Project project = mAdapter.getItem(info.position);
 
         menu.setHeaderTitle(getString(R.string.listmenu_title) + " " + project.getName());
         menu.add(mId, MENU_EDIT, 0, getString(R.string.listmenu_edit));
         menu.add(mId, MENU_REMOVE, 1, getString(R.string.listmenu_remove));
     }
 
     /*
      * (non-Javadoc)
      * @see android.support.v4.app.Fragment#onActivityResult(int, int,
      * android.content.Intent)
      */
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
 
         if (requestCode == ProjectActivity.ACTIVITY_CODE) {
             if (resultCode == getActivity().RESULT_OK) {
                 String name = data.getStringExtra(ProjectActivity.EXTRA_PROJECT_NAME);
                 String colorId = data.getStringExtra(ProjectActivity.EXTRA_PROJECT_COLOR_ID);
                 int id = data.getIntExtra(ProjectActivity.EXTRA_PROJECT_ID, -1);
 
                 List<Project> projects = DatabaseManager.getInstance().getProject(id);
 
                 if (projects.size() > 0) {
                     Project project = projects.get(0);
                     project.setName(name);
                     project.setColorId(colorId);
 
                     DatabaseManager.getInstance().updateProject(project);
 
                     resetAdapter();
                 }
                 else {
                     // We should always have an existing project
                 }
 
             }
         }
     }
 
     /*
     public void onListItemClick(ListView l, View v, int position, long id) {
         onListItemClick(l, v, position, id);
 
         Project project = mAdapter.getItem(position);
 
         Intent i = new Intent(getActivity(), HoursActivity.class)
                 .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         i.putExtra(HoursActivity.EXTRA_PROJECT_ID, project.getId());
         startActivity(i);
 
     }
     */
 
     private void attachAdapter() {
         List<Project> projectList = DatabaseManager.getInstance().getAllProjects(mId);
 
         mAdapter = new ProjectArrayAdapter(getActivity(),
                 android.R.layout.simple_list_item_1, projectList);
 
         mListView.setAdapter(mAdapter);
     }
 
     private void resetAdapter() {
         mAdapter.clear();
         mAdapter.addAll(DatabaseManager.getInstance().getAllProjects(mId));
         mAdapter.notifyDataSetChanged();
     }
 
     private void registerReceiver() {
         IntentFilter filter = new IntentFilter();
         filter.addAction(ProjectListFragment.ResponseReceiver.INTENT_ACTION_ADD_PROJECT);
         filter.addAction(ProjectListFragment.ResponseReceiver.INTENT_ACTION_DELETE_PROJECT);
         filter.addAction(ProjectListFragment.ResponseReceiver.INTENT_ACTION_CLEAR_ALL);
         filter.addCategory(Intent.CATEGORY_DEFAULT);
         getActivity().registerReceiver(mReceiver, filter);
     }
 
     private void unregisterReceiver() {
         getActivity().unregisterReceiver(mReceiver);
     }
 
     public class ResponseReceiver extends BroadcastReceiver {
         /** Actions */
         public static final String INTENT_ACTION_ADD_PROJECT = "se.slide.timy.intent.action.ADD_PROJECT";
         public static final String INTENT_ACTION_DELETE_PROJECT = "se.slide.timy.intent.action.DELETE_PROJECT";
         public static final String INTENT_ACTION_CLEAR_ALL = "se.slide.timy.intent.action.CLEAR_ALL";
 
         /** Extras */
         public static final String CURRENT_PAGE = "current_page";
 
         @Override
         public void onReceive(Context context, Intent intent) {
             int currentPage = intent.getIntExtra(CURRENT_PAGE, 0);
 
             String action = intent.getAction();
             if (action.equals(INTENT_ACTION_ADD_PROJECT) && currentPage == mId) {
                 mAdapter.clear();
                 mAdapter.addAll(DatabaseManager.getInstance().getAllProjects(mId));
                 mAdapter.notifyDataSetChanged();
             }
             if (action.equals(INTENT_ACTION_CLEAR_ALL) && currentPage == mId) {
                 mAdapter.clear();
             }
 
         }
     }
 
     @Override
     public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
         Project project = mAdapter.getItem(position);
 
         /*
          * FragmentManager fm = getActivity().getSupportFragmentManager();
          * HoursDialog dialog = HoursDialog.newInstance(project.getName(),
          * project.getId()); dialog.show(fm, "dialog_select_hours");
          */
 
         Intent i = new Intent(getActivity(), HoursActivity.class)
                 .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         i.putExtra(HoursActivity.EXTRA_PROJECT_ID, project.getId());
         startActivity(i);
     }
 }
