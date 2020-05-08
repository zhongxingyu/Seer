 package jp.gr.java_conf.neko_daisuki.photonote;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
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
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.util.SparseArray;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View.OnClickListener;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseExpandableListAdapter;
 import android.widget.EditText;
 import android.widget.ExpandableListView;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 
     private interface ActivityResultProc {
 
         public void run(Intent data);
     }
 
     private class EditResultProc implements ActivityResultProc {
 
         public void run(Intent data) {
             mReturnedFromEdit = true;
         }
     }
 
     private class CaptureResultProc implements ActivityResultProc {
 
         public void run(Intent data) {
             Entry entry = new Entry(makeNewEntryName());
             new File(entry.getDirectory()).mkdir();
 
             String src = getTemporaryPath();
             String dest = entry.getOriginalPath();
             if (!new File(src).renameTo(new File(dest))) {
                 String fmt = "failed to move %s to %s.";
                 logError(String.format(fmt, src, dest));
                 return;
             }
             if (!makeThumbnail(entry)) {
                 return;
             }
             String path = entry.getAdditionalPath();
             try {
                 new File(path).createNewFile();
             }
             catch (IOException e) {
                 String fmt = "failed to create %s: %s";
                 logError(String.format(fmt, path, e.getMessage()));
                 return;
             }
 
             mResultEntry = entry;
 
             Log.i(LOG_TAG, String.format("added %s.", dest));
         }
     }
 
     private abstract class EntryButtonOnClickListener implements OnClickListener {
 
         private Entry mEntry;
 
         public EntryButtonOnClickListener(Entry entry) {
             mEntry = entry;
         }
 
         public abstract void onClick(View view);
 
         protected Entry getEntry() {
             return mEntry;
         }
     }
 
     private class EditButtonOnClickListener extends EntryButtonOnClickListener {
 
         public EditButtonOnClickListener(Entry entry) {
             super(entry);
         }
 
         public void onClick(View view) {
             openEditActivity(getEntry());
         }
     }
 
     private static class Size {
 
         public int width;
         public int height;
     }
 
     private static class DialogCancelListener implements DialogInterface.OnClickListener {
 
         public void onClick(DialogInterface dialog, int which) {
         }
     }
 
     private class DialogDeleteEntryListener implements DialogInterface.OnClickListener {
 
         public void onClick(DialogInterface dialog, int which) {
             deleteEntry(mDeletingEntry);
            saveGroups();
             updateList();
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
 
     private class DeleteEntryDialogCreatingProc extends DialogCreatingProc {
 
         public Dialog create() {
             return createDeleteEntryDialog();
         }
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
 
         public View getChildView(int groupPosition, int childPosition,
                                  boolean isLastChild, View convertView,
                                  ViewGroup parent) {
             return isLastChild
                 ? getGroupControlView(groupPosition, childPosition, parent)
                 : getEntryView(groupPosition, childPosition, parent);
         }
 
         public View getGroupView(int groupPosition, boolean isExpanded,
                                  View convertView, ViewGroup parent) {
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
             List<Entry> entries = mGroups.get(groupPosition).getEntries();
             return childPosition < entries.size()
                 ? entries.get(childPosition).hashCode()
                 : -1;
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
 
         private View getEntryView(int groupPosition, int childPosition,
                                   ViewGroup parent) {
             View view = mInflater.inflate(R.layout.child_row, parent, false);
             Entry entry = (Entry)getChild(groupPosition, childPosition);
 
             TextView text = (TextView)view.findViewById(R.id.name);
             text.setText(entry.getName());
             ImageView image = (ImageView)view.findViewById(R.id.thumbnail);
             image.setImageURI(Uri.fromFile(new File(entry.getThumbnailPath())));
             View editButton = view.findViewById(R.id.edit_button);
             editButton.setOnClickListener(new EditButtonOnClickListener(entry));
             View deleteButton = view.findViewById(R.id.delete_button);
             deleteButton.setOnClickListener(
                     new DeleteEntryButtonOnClickListener(entry));
 
             return view;
         }
 
         private View getGroupControlView(int groupPosition, int childPosition,
                                          ViewGroup parent) {
             int resId = R.layout.child_last_row;
             View view = mInflater.inflate(resId, parent, false);
             Group group = (Group)getGroup(groupPosition);
             View shotButton = view.findViewById(R.id.shot_button);
             shotButton.setOnClickListener(new ShotButtonOnClickListener(group));
             View deleteButton = view.findViewById(R.id.delete_button);
             deleteButton.setOnClickListener(new DeleteGroupOnClickListener(group));
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
 
     private class DeleteEntryButtonOnClickListener extends EntryButtonOnClickListener {
 
         public DeleteEntryButtonOnClickListener(Entry entry) {
             super(entry);
         }
 
         public void onClick(View view) {
             mDeletingEntry = getEntry();
             showDialog(DIALOG_DELETE_ENTRY);
         }
     }
 
     private class DeleteGroupOnClickListener extends GroupButtonOnClickListener {
 
         public DeleteGroupOnClickListener(Group group) {
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
             return getPath("original.png");
         }
 
         public String getAdditionalPath() {
             return getPath("additional.json");
         }
 
         public String getThumbnailPath() {
             return getPath("thumbnail.png");
         }
 
         public int hashCode() {
             return getName().hashCode();
         }
 
         private String getPath(String name) {
             return String.format("%s/%s", getDirectory(), name);
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
         SHOTTING_GROUP_NAME,
         TARGET_ENTRY_NAME
     }
 
     private static final int REQUEST_CAPTURE = 42;
     private static final int REQUEST_EDIT = 43;
 
     private static final int DIALOG_GROUP_NAME = 42;
     private static final int DIALOG_DELETE_GROUP = 43;
     private static final int DIALOG_DELETE_ENTRY = 44;
 
     private static final String LOG_TAG = "photonote";
 
     // document
     private List<Group> mGroups;
 
     // view
     private ListAdapter mAdapter;
 
     // stateful helpers
     private String mShottingGroupName;
     private String mTargetEntryName;
     private Entry mResultEntry;
     private Entry mDeletingEntry;
     private Group mDeletingGroup;
     private boolean mReturnedFromEdit;
 
     // stateless helpers
     private SimpleDateFormat mDateFormat;
     private LayoutInflater mInflater;
     private SparseArray<DialogCreatingProc> mDialogCreatingProcs;
     private SparseArray<ActivityResultProc> mActivityResultProcs;
     private View mGroupNameView;
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         mGroups = new ArrayList<Group>();
 
         ExpandableListView list = (ExpandableListView)findViewById(R.id.list);
         mAdapter = new ListAdapter();
         list.setAdapter(mAdapter);
 
         View addGroupButton = findViewById(R.id.add_a_new_group_button);
         addGroupButton.setOnClickListener(new AddGroupListener());
 
         setupFileTree();
 
         mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         String service = Context.LAYOUT_INFLATER_SERVICE;
         mInflater = (LayoutInflater)getSystemService(service);
 
         mDialogCreatingProcs = new SparseArray<DialogCreatingProc>();
         mDialogCreatingProcs.put(
                 DIALOG_GROUP_NAME,
                 new GroupNameDialogCreatingProc());
         mDialogCreatingProcs.put(
                 DIALOG_DELETE_GROUP,
                 new DeleteGroupDialogCreatingProc());
         mDialogCreatingProcs.put(
                 DIALOG_DELETE_ENTRY,
                 new DeleteEntryDialogCreatingProc());
 
         mActivityResultProcs = new SparseArray<ActivityResultProc>();
         mActivityResultProcs.put(REQUEST_CAPTURE, new CaptureResultProc());
         mActivityResultProcs.put(REQUEST_EDIT, new EditResultProc());
 
         mGroupNameView = mInflater.inflate(R.layout.dialog_group_name, null);
 
         mReturnedFromEdit = false;
     }
 
     protected Dialog onCreateDialog(int id) {
         return mDialogCreatingProcs.get(id).create();
     }
 
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putString(Key.SHOTTING_GROUP_NAME.name(), mShottingGroupName);
         outState.putString(Key.TARGET_ENTRY_NAME.name(), mTargetEntryName);
     }
 
     protected void onRestoreInstanceState(Bundle savedInstanceState) {
         super.onRestoreInstanceState(savedInstanceState);
         mShottingGroupName = savedInstanceState.getString(Key.SHOTTING_GROUP_NAME.name());
         mTargetEntryName = savedInstanceState.getString(Key.TARGET_ENTRY_NAME.name());
     }
 
     protected void onResume() {
         super.onResume();
 
         updateData();
         if (mResultEntry != null) {
             findGroupOfName(mShottingGroupName).getEntries().add(mResultEntry);
             updateList();
             mResultEntry = null;
         }
         if (mReturnedFromEdit) {
             String src = getTemporaryAdditionalPath();
             String dest = findEntryOfName(mTargetEntryName).getAdditionalPath();
             try {
                 copyFile(dest, src);
             }
             catch (IOException e) {
                 String fmt = "failed to copy data.json from %s to %s: %s";
                 logError(String.format(fmt, src, dest, e.getMessage()));
             }
             mReturnedFromEdit = false;
         }
     }
 
     protected void onPause() {
         super.onPause();
         saveGroups();
     }
 
     protected void onActivityResult(int requestCode, int resultCode,
                                     Intent data) {
         if (resultCode != RESULT_OK) {
             return;
         }
         mActivityResultProcs.get(requestCode).run(data);
     }
 
     private void setupFileTree() {
         makeDirectories();
         makeDefaultGroup();
         touchNoMedia();
     }
 
     private void touchNoMedia() {
         String path = String.format("%s/.nomedia", getDataDirectory());
         File file = new File(path);
         try {
             file.createNewFile();
         }
         catch (IOException e) {
             String fmt = "failed to create .nomedia: %s: %s";
             logError(String.format(fmt, path, e.getMessage()));
             return;
         }
         Log.i(LOG_TAG, String.format("touched: %s", path));
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
             getGroupsDirectory(),
             getTemporaryDirectory() };
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
 
     private String getDataDirectory() {
         String dir = Environment.getExternalStorageDirectory().getPath();
         return String.format("%s/.photonote", dir);
     }
 
     private String getTemporaryDirectory() {
         return String.format("%s/tmp", getDataDirectory());
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
         try {
             String entryName;
             while ((entryName = in.readLine()) != null) {
                 group.getEntries().add(entries.get(entryName));
             }
         }
         finally {
             in.close();
         }
 
         return group;
     }
 
     private void shot(Group group) {
         mShottingGroupName = group.getName();
 
         Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
         Uri uri = Uri.fromFile(new File(getTemporaryPath()));
         i.putExtra(MediaStore.EXTRA_OUTPUT, uri);
         startActivityForResult(i, REQUEST_CAPTURE);
     }
 
     private String makeNewEntryName() {
         return mDateFormat.format(new Date());
     }
 
     private Entry findEntryOfName(String name) {
         for (Group group: mGroups) {
             for (Entry entry: group.getEntries()) {
                 if (name.equals(entry.getName())) {
                     return entry;
                 }
             }
         }
         return null;
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
 
     private Dialog createDeleteDialog(
             int msgId, String name, DialogInterface.OnClickListener listener) {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
         Resources res = getResources();
         String fmt = res.getString(msgId);
         String positive = res.getString(R.string.positive);
         String negative = res.getString(R.string.negative);
 
         builder.setMessage(String.format(fmt, name, positive, negative));
         builder.setPositiveButton(positive, listener);
         builder.setNegativeButton(negative, new DialogCancelListener());
 
         return builder.create();
     }
 
     private Dialog createDeleteEntryDialog() {
         int msgId = R.string.delete_entry_dialog_message;
         String name = mDeletingEntry.getName();
         return createDeleteDialog(msgId, name, new DialogDeleteEntryListener());
     }
 
     private Dialog createDeleteGroupDialog() {
         int msgId = R.string.delete_group_dialog_message;
         String name = mDeletingGroup.getName();
         return createDeleteDialog(msgId, name, new DialogDeleteGroupListener());
     }
 
     private Dialog createGroupNameDialog() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
         Resources res = getResources();
         String positive = res.getString(R.string.positive);
         String negative = res.getString(R.string.negative);
 
         builder.setView(mGroupNameView);
         builder.setPositiveButton(positive, new DialogAddGroupListener());
         builder.setNegativeButton(negative, new DialogCancelListener());
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
 
     private void deleteDirectory(String directory) {
         deleteDirectory(new File(directory));
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
 
     private void deleteEntry(Entry entry) {
         String name = entry.getName();
         for (Group group: mGroups) {
             List<Entry> entries = group.getEntries();
             int size = entries.size();
             for (int i = 0; i < size; i++) {
                 if (name.equals(entries.get(i).getName())) {
                     entries.remove(i);
                     break;
                 }
             }
         }
 
         deleteDirectory(entry.getDirectory());
     }
 
     private void deleteGroup(Group group) {
         for (Entry entry: group.getEntries()) {
             deleteDirectory(entry.getDirectory());
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
 
     private String getTemporaryPath(String name) {
         return String.format("%s/%s", getTemporaryDirectory(), name);
     }
 
     private String getTemporaryPath() {
         return getTemporaryPath("original.png");
     }
 
     private Size computeThumbnailSize(Bitmap orig) {
         Size size = new Size();
 
         int width = orig.getWidth();
         int height = orig.getHeight();
         int maxThumbnailWidth = 256;
         int maxThumbnailHeight = maxThumbnailWidth;
         if ((width < maxThumbnailWidth) && (height < maxThumbnailHeight)) {
             size.width = maxThumbnailWidth;
             size.height = maxThumbnailHeight;
             return size;
         }
 
         if (width < height) {
             float ratio = (float)maxThumbnailHeight / (float)height;
             size.width = (int)(ratio * (float)width);
             size.height = maxThumbnailHeight;
             return size;
         }
 
         float ratio = (float)maxThumbnailWidth / (float)width;
         size.width = maxThumbnailWidth;
         size.height = (int)(ratio * (float)height);
         return size;
     }
 
     private boolean makeThumbnail(Entry entry) {
         String origPath = entry.getOriginalPath();
         Bitmap orig = BitmapFactory.decodeFile(origPath);
         if (orig == null) {
             logError(String.format("failed to decode image: %s", origPath));
             return false;
         }
 
         Size size = computeThumbnailSize(orig);
         Log.i(LOG_TAG, String.format("thumbnail: width=%d, height=%d", size.width, size.height));
         Bitmap thumb = Bitmap.createScaledBitmap(
                 orig,
                 size.width, size.height,
                 false);
         orig.recycle();
 
         String thumbPath = entry.getThumbnailPath();
         OutputStream out;
         try {
             out = new FileOutputStream(thumbPath);
         }
         catch (FileNotFoundException e) {
             String fmt = "failed to open %s: %s";
             logError(String.format(fmt, thumbPath, e.getMessage()));
             return false;
         }
         try {
             if (!thumb.compress(CompressFormat.PNG, 100, out)) {
                 String fmt = "failed to make thumbnail: %s";
                 logError(String.format(fmt, thumbPath));
                 return false;
             }
         }
         finally {
             try {
                 out.close();
             }
             catch (IOException e) {
                 String fmt = "failed to close thumbnail %s: %s";
                 logError(String.format(fmt, thumbPath, e.getMessage()));
                 return false;
             }
         }
 
         return true;
     }
 
     private void copyFile(String dest, String src) throws IOException {
         OutputStream out = new FileOutputStream(dest);
         try {
             InputStream in = new FileInputStream(src);
             try {
                 byte[] buf = new byte[8192];
                 while (0 < in.available()) {
                     int len = in.read(buf);
                     out.write(buf, 0, len);
                 }
             }
             finally {
                 in.close();
             }
         }
         finally {
             out.close();
         }
     }
 
     private void openEditActivity(Entry entry) {
         String additionalPath = entry.getAdditionalPath();
         String temporaryPath = getTemporaryAdditionalPath();
         try {
             copyFile(temporaryPath, additionalPath);
         }
         catch (IOException e) {
             String fmt = "failed to copy from %s to %s: %s";
             String msg = e.getMessage();
             logError(String.format(fmt, temporaryPath, additionalPath, msg));
             return;
         }
 
         mTargetEntryName = entry.getName();
 
         Intent i = new Intent(this, EditActivity.class);
         i.putExtra(
                 EditActivity.Extra.ORIGINAL_PATH.name(),
                 entry.getOriginalPath());
         i.putExtra(EditActivity.Extra.ADDITIONAL_PATH.name(), temporaryPath);
         startActivityForResult(i, REQUEST_EDIT);
     }
 
     private String getTemporaryAdditionalPath() {
         return getTemporaryPath("data.json");
     }
 }
 
 /**
  * vim: tabstop=4 shiftwidth=4 expandtab softtabstop=4
  */
