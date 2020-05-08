 package jp.gr.java_conf.neko_daisuki.photonote;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View.OnClickListener;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ExpandableListView;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
     private static class DialogCancelListener implements DialogInterface.OnClickListener {
 
         public void onClick(DialogInterface dialog, int which) {
         }
     }
 
     private class DialogDeleteGroupListener implements DialogInterface.OnClickListener {
 
         public void onClick(DialogInterface dialog, int which) {
             deleteGroup(mDeletingGroup);
             updateData();
             updateList();
         }
     }
 
     private class DialogAddGroupListener implements DialogInterface.OnClickListener {
 
         public void onClick(DialogInterface dialog, int which) {
             EditText text = (EditText)mGroupNameView.findViewById(R.id.name);
             CharSequence name = text.getText();
             if (name.length() == 0) {
                 showInformation(
                         "You gave empty group name. No groups are added.");
                 return;
             }
             makeGroup(name);
             updateData();
             updateList();
         }
     }
 
     private abstract class DialogCreatingProc {
 
         public abstract Dialog create();
     }
 
     private class DeleteGroupDialogCreatingProc extends DialogCreatingProc {
 
         public Dialog create() {
             return createDeleteGroupDialog();
         }
     }
 
     private class GroupNameDialogCreatingProc extends DialogCreatingProc {
 
         public Dialog create() {
             return createGroupNameDialog();
         }
     }
 
     private class AddGroupListener implements OnClickListener {
 
         public void onClick(View view) {
             showDialog(DIALOG_GROUP_NAME);
         }
     }
 
     private class ListAdapter extends BaseExpandableListAdapter {
 
         public boolean isChildSelectable(int groupPosition, int childPosition) {
             return false;
         }
 
         public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
             return isLastChild ? getGroupControlView(groupPosition, childPosition, parent) : getEntryView(groupPosition, childPosition, parent);
         }
 
         public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
             View view = mInflater.inflate(R.layout.group_row, parent, false);
             TextView text = (TextView)view.findViewById(R.id.name);
             Group group = (Group)getGroup(groupPosition);
             text.setText(group.getName());
             return view;
         }
 
         public boolean hasStableIds() {
             return true;
         }
 
         public long getGroupId(int groupPosition) {
             return mGroups.get(groupPosition).getName().hashCode();
         }
 
         public long getChildId(int groupPosition, int childPosition) {
             Group group = mGroups.get(groupPosition);
             Entry entry = group.getEntries().get(childPosition);
             return entry.getName().hashCode();
         }
 
         public Object getChild(int groupPosition, int childPosition) {
             return mGroups.get(groupPosition).getEntries().get(childPosition);
         }
 
         public Object getGroup(int groupPosition) {
             return mGroups.get(groupPosition);
         }
 
         public int getChildrenCount(int groupPosition) {
             return mGroups.get(groupPosition).getEntries().size() + 1;
         }
 
         public int getGroupCount() {
             return mGroups.size();
         }
 
         private View getEntryView(int groupPosition, int childPosition, ViewGroup parent) {
             View view = mInflater.inflate(R.layout.child_row, parent, false);
             TextView text = (TextView)view.findViewById(R.id.name);
             Entry entry = (Entry)getChild(groupPosition, childPosition);
             text.setText(entry.getName());
             return view;
         }
 
         private View getGroupControlView(int groupPosition, int childPosition, ViewGroup parent) {
             View view = mInflater.inflate(R.layout.child_last_row, parent, false);
             Group group = (Group)getGroup(groupPosition);
             View shotButton = view.findViewById(R.id.shot_button);
             shotButton.setOnClickListener(new ShotButtonOnClickListener(group));
             View deleteButton = view.findViewById(R.id.delete_button);
             deleteButton.setOnClickListener(new DeleteButtonOnClickListener(group));
             return view;
         }
     }
 
     private abstract class GroupButtonOnClickListener implements OnClickListener {
 
         private Group mGroup;
 
         public GroupButtonOnClickListener(Group group) {
             mGroup = group;
         }
 
         public abstract void onClick(View view);
 
         protected Group getGroup() {
             return mGroup;
         }
     }
 
     private class DeleteButtonOnClickListener extends GroupButtonOnClickListener {
 
         public DeleteButtonOnClickListener(Group group) {
             super(group);
         }
 
         public void onClick(View view) {
             mDeletingGroup = getGroup();
             showDialog(DIALOG_DELETE_GROUP);
         }
     }
 
     private class ShotButtonOnClickListener extends GroupButtonOnClickListener {
 
         public ShotButtonOnClickListener(Group group) {
             super(group);
         }
 
         public void onClick(View view) {
             shot(getGroup());
         }
     }
 
     private class Entry {
 
         private String mName;
 
         public Entry(String name) {
             mName = name;
         }
 
         public String getName() {
             return mName;
         }
 
         public String getDirectory() {
             return String.format("%s/%s", getEntriesDirectory(), mName);
         }
 
         public String getOriginalPath() {
             return String.format("%s/original.png", getDirectory());
         }
     }
 
     private class Group {
 
         private String mName;
         private List<Entry> mEntries;
 
         public Group(String name) {
             mName = name;
             mEntries = new ArrayList<Entry>();
         }
 
         public List<Entry> getEntries() {
             return mEntries;
         }
 
         public String getName() {
             return mName;
         }
 
         public String getPath() {
             return String.format("%s/%s", getGroupsDirectory(), getName());
         }
     }
 
     private enum Key {
         SHOTTING_GROUP_NAME
     }
 
     private static final String LOG_TAG = "photonote";
     private static final int REQUEST_CAPTURE = 42;
     private static final int DIALOG_GROUP_NAME = 42;
     private static final int DIALOG_DELETE_GROUP = 43;
 
     // document
     private List<Group> mGroups;
 
     // view
     private ListAdapter mAdapter;
 
     // stateful helpers
     private String mShottingGroupName;
     private Entry mResultEntry;
     private Group mDeletingGroup;
 
     // stateless helpers
     private SimpleDateFormat mDateFormat;
     private LayoutInflater mInflater;
     private Map<Integer, DialogCreatingProc> mDialogCreatingProcs;
     private View mGroupNameView;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         mGroups = new ArrayList<Group>();
 
         ExpandableListView list = (ExpandableListView)findViewById(R.id.list);
         mAdapter = new ListAdapter();
         list.setAdapter(mAdapter);
 
         Button addGroupButton = (Button)findViewById(R.id.add_a_new_group_button);
         addGroupButton.setOnClickListener(new AddGroupListener());
 
         setupFileTree();
 
         mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         String service = Context.LAYOUT_INFLATER_SERVICE;
         mInflater = (LayoutInflater)getSystemService(service);
 
         mDialogCreatingProcs = new HashMap<Integer, DialogCreatingProc>();
         mDialogCreatingProcs.put(
                 new Integer(DIALOG_GROUP_NAME),
                 new GroupNameDialogCreatingProc());
         mDialogCreatingProcs.put(
                 new Integer(DIALOG_DELETE_GROUP),
                 new DeleteGroupDialogCreatingProc());
 
         mGroupNameView = mInflater.inflate(R.layout.dialog_group_name, null);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     private void setupFileTree() {
         makeDirectories();
         makeDefaultGroup();
     }
 
     private void makeGroup(CharSequence name) {
         boolean done;
         String path = String.format("%s/%s", getGroupsDirectory(), name);
         try {
             done = new File(path).createNewFile();
         }
         catch (IOException e) {
             String fmt = "failed to open %s: %s";
            logError(String.format(fmt, path, e.getMessage()));
             return;
         }
         if (done) {
             Log.i(LOG_TAG, String.format("created a new group: %s", path));
         }
     }
 
     private void makeDefaultGroup() {
         makeGroup("default");
     }
 
     private void makeDirectories() {
         String[] directories = new String[] {
             getDataDirectory(),
             getEntriesDirectory(),
             getGroupsDirectory() };
         for (String directory: directories) {
             File file = new File(directory);
             if (file.exists()) {
                 continue;
             }
             if (file.mkdir()) {
                 Log.i(LOG_TAG, String.format("make directory: %s", directory));
                 continue;
             }
            logError(String.format("failed to mkdir: %s", directory));
         }
     }
 
     protected Dialog onCreateDialog(int id) {
         return mDialogCreatingProcs.get(new Integer(id)).create();
     }
 
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putString(Key.SHOTTING_GROUP_NAME.name(), mShottingGroupName);
     }
 
     protected void onRestoreInstanceState(Bundle savedInstanceState) {
         super.onRestoreInstanceState(savedInstanceState);
         mShottingGroupName = savedInstanceState.getString(Key.SHOTTING_GROUP_NAME.name());
     }
 
     protected void onResume() {
         super.onResume();
 
         updateData();
         if (mResultEntry != null) {
             findGroupOfName(mShottingGroupName).getEntries().add(mResultEntry);
             updateList();
             mResultEntry = null;
         }
     }
 
     protected void onPause() {
         super.onPause();
         saveGroups();
     }
 
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if ((requestCode != REQUEST_CAPTURE) || (resultCode != RESULT_OK)) {
             return;
         }
 
         mResultEntry = new Entry(makeNewEntryName());
         new File(mResultEntry.getDirectory()).mkdir();
 
         Bitmap bitmap = (Bitmap)data.getExtras().get("data");
         String path = mResultEntry.getOriginalPath();
         OutputStream out;
         try {
             out = new FileOutputStream(path);
         }
         catch (FileNotFoundException e) {
             String fmt = "failed to open %s: %s";
            logError(String.format(fmt, path, e.getMessage()));
             return;
         }
         try {
             try {
                 bitmap.compress(CompressFormat.PNG, 100, out);
             }
             finally {
                 out.close();
             }
         }
         catch (IOException e) {
             String fmt = "failed to close %s: %s";
            logError(String.format(fmt, path, e.getMessage()));
             return;
         }
 
         Log.i(LOG_TAG, String.format("added %s.", path));
     }
 
     private String getDataDirectory() {
         return "/mnt/sdcard/.photonote";
     }
 
     private String getEntriesDirectory() {
         return String.format("%s/entries", getDataDirectory());
     }
 
     private String getGroupsDirectory() {
         return String.format("%s/groups", getDataDirectory());
     }
 
     private Map<String, Entry> readEntries() {
         Map<String, Entry> entries = new HashMap<String, Entry>();
 
         for (String name: new File(getEntriesDirectory()).list()) {
             entries.put(name, new Entry(name));
         }
 
         return entries;
     }
 
     private List<Group> readGroups(Map<String, Entry> entries) {
         List<Group> groups = new ArrayList<Group>();
 
         for (File file: new File(getGroupsDirectory()).listFiles()) {
             String name = file.getName();
             try {
                 groups.add(readGroup(name, entries));
             }
             catch (IOException e) {
                logError(String.format("failed to read a group of %s.", name));
             }
         }
 
         return groups;
     }
 
     private Group readGroup(String name, Map<String, Entry> entries) throws IOException {
         Group group = new Group(name);
 
         String path = String.format("%s/%s", getGroupsDirectory(), name);
         BufferedReader in = new BufferedReader(new FileReader(path));
         String entryName;
         while ((entryName = in.readLine()) != null) {
             group.getEntries().add(entries.get(entryName));
         }
 
         return group;
     }
 
     private void shot(Group group) {
         mShottingGroupName = group.getName();
 
         Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
         startActivityForResult(i, REQUEST_CAPTURE);
     }
 
     private String makeNewEntryName() {
         return mDateFormat.format(new Date());
     }
 
     private Group findGroupOfName(String name) {
         for (Group group: mGroups) {
             if (group.getName().equals(name)) {
                 return group;
             }
         }
         return null;
     }
 
     private void saveGroups() {
         for (Group group: mGroups) {
             String path = group.getPath();
             OutputStream out;
             try {
                 out = new FileOutputStream(path);
             }
             catch (FileNotFoundException e) {
                 String fmt = "failed to write %s: %s";
                logError(String.format(fmt, path, e.getMessage()));
                 continue;
             }
             PrintWriter writer = new PrintWriter(out);
             try {
                 for (Entry entry: group.getEntries()) {
                     writer.println(entry.getName());
                 }
             }
             finally {
                 writer.close();
             }
         }
     }
 
     private Dialog createDeleteGroupDialog() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
         Resources res = getResources();
         String fmt = res.getString(R.string.delete_dialog_message);
         String name = mDeletingGroup.getName();
         String positive = res.getString(R.string.positive);
         String negative = res.getString(R.string.negative);
 
         builder.setMessage(String.format(fmt, name, positive, negative));
         builder.setPositiveButton(positive, new DialogDeleteGroupListener());
         builder.setNegativeButton(negative, new DialogCancelListener());
         return builder.create();
     }
 
     private Dialog createGroupNameDialog() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setView(mGroupNameView);
         builder.setPositiveButton("Okey", new DialogAddGroupListener());
         builder.setNegativeButton("Cancel", new DialogCancelListener());
         return builder.create();
     }
 
     private void updateData() {
         mGroups = readGroups(readEntries());
     }
 
     private void updateList() {
         mAdapter.notifyDataSetChanged();
     }
 
     private void deleteFile(File file) {
         file.delete();
         Log.i(LOG_TAG, String.format("deleted: %s", file.getAbsolutePath()));
     }
 
     private void deleteDirectory(File directory) {
         for (File file: directory.listFiles()) {
             if (file.isDirectory()) {
                 deleteDirectory(file);
             }
             deleteFile(file);
         }
         deleteFile(directory);
     }
 
     private void deleteGroup(Group group) {
         for (Entry entry: group.getEntries()) {
             deleteDirectory(new File(entry.getDirectory()));
         }
         deleteFile(new File(group.getPath()));
     }
 
     private void showInformation(String message) {
         Toast.makeText(this, message, Toast.LENGTH_LONG).show();
     }

    private void logError(String message) {
        Log.e(LOG_TAG, message);
        showInformation(message);
    }
 }
 
 /**
  * vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
  */
