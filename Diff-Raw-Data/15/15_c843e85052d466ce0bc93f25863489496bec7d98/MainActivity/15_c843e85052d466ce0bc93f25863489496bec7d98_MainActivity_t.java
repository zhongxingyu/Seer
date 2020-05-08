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
 import android.content.Context;
 import android.content.Intent;
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
 import android.widget.ExpandableListView;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
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
             View shotButton = view.findViewById(R.id.shot_button);
             Group group = (Group)getGroup(groupPosition);
             shotButton.setOnClickListener(new ShotButtonOnClickListener(group));
             return view;
         }
     }
 
     private class ShotButtonOnClickListener implements OnClickListener {
 
         private Group mGroup;
 
         public ShotButtonOnClickListener(Group group) {
             mGroup = group;
         }
 
         public void onClick(View view) {
             shot(mGroup);
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
 
     // document
     private List<Group> mGroups;
 
     // view
     private ExpandableListView mList;
 
     // stateful helpers
     private String mShottingGroupName;
     private Entry mResultEntry;
 
     // stateless helpers
     private SimpleDateFormat mDateFormat;
     private LayoutInflater mInflater;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         mList = (ExpandableListView)findViewById(R.id.list);
 
         setupFileTree();
 
         mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         String service = Context.LAYOUT_INFLATER_SERVICE;
         mInflater = (LayoutInflater)getSystemService(service);
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
 
     private void makeDefaultGroup() {
         boolean done;
         String path = String.format("%s/default", getGroupsDirectory());
         try {
             done = new File(path).createNewFile();
         }
         catch (IOException e) {
             String fmt = "failed to open %s: %s";
             Log.e(LOG_TAG, String.format(fmt, path, e.getMessage()));
             return;
         }
         if (done) {
             Log.i(LOG_TAG, String.format("created: %s", path));
         }
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
             Log.e(LOG_TAG, String.format("failed to mkdir: %s", directory));
         }
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
 
         mGroups = readGroups(readEntries());
         if (mResultEntry != null) {
             findGroupOfName(mShottingGroupName).getEntries().add(mResultEntry);
             mResultEntry = null;
         }
         buildView();
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
             Log.e(LOG_TAG, String.format(fmt, path, e.getMessage()));
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
             Log.e(LOG_TAG, String.format(fmt, path, e.getMessage()));
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
                 String fmt = "failed to read a group of %s.";
                 Log.e(LOG_TAG, String.format(fmt, name));
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
 
     private void buildView() {
         ListAdapter adapter = new ListAdapter();
         mList.setAdapter(adapter);
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
                 Log.e(LOG_TAG, String.format(fmt, path, e.getMessage()));
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
 }
 
 /**
  * vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
  */
