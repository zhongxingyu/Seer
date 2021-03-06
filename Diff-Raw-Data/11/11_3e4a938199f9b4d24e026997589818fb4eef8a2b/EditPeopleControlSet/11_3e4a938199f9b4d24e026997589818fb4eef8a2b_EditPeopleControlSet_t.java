 /**
  * Copyright (c) 2012 Todoroo Inc
  *
  * See the file "LICENSE" for the full license governing this code.
  */
 package com.todoroo.astrid.actfm;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.net.Uri;
 import android.provider.ContactsContract;
 import android.support.v4.app.Fragment;
 import android.text.TextUtils;
 import android.util.DisplayMetrics;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CheckedTextView;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.commonsware.cwac.merge.MergeAdapter;
 import com.timsu.astrid.R;
 import com.todoroo.andlib.data.TodorooCursor;
 import com.todoroo.andlib.service.Autowired;
 import com.todoroo.andlib.service.DependencyInjectionService;
 import com.todoroo.andlib.service.ExceptionService;
 import com.todoroo.andlib.sql.Criterion;
 import com.todoroo.andlib.sql.Functions;
 import com.todoroo.andlib.sql.Order;
 import com.todoroo.andlib.sql.Query;
 import com.todoroo.andlib.utility.DialogUtilities;
 import com.todoroo.andlib.utility.Preferences;
 import com.todoroo.astrid.actfm.sync.ActFmPreferenceService;
 import com.todoroo.astrid.actfm.sync.ActFmSyncService;
 import com.todoroo.astrid.activity.TaskEditFragment;
 import com.todoroo.astrid.dao.UserDao;
 import com.todoroo.astrid.data.Metadata;
 import com.todoroo.astrid.data.TagData;
 import com.todoroo.astrid.data.Task;
 import com.todoroo.astrid.data.User;
 import com.todoroo.astrid.helper.AsyncImageView;
 import com.todoroo.astrid.service.AstridDependencyInjector;
 import com.todoroo.astrid.service.MetadataService;
 import com.todoroo.astrid.service.StatisticsConstants;
 import com.todoroo.astrid.service.StatisticsService;
 import com.todoroo.astrid.service.TagDataService;
 import com.todoroo.astrid.service.TaskService;
 import com.todoroo.astrid.service.ThemeService;
 import com.todoroo.astrid.service.abtesting.ABChooser;
 import com.todoroo.astrid.tags.TagService;
 import com.todoroo.astrid.ui.PeopleContainer;
 import com.todoroo.astrid.ui.PeopleContainer.OnAddNewPersonListener;
 import com.todoroo.astrid.ui.PeopleContainer.ParseSharedException;
 import com.todoroo.astrid.ui.PopupControlSet;
 
 public class EditPeopleControlSet extends PopupControlSet {
 
     public static final String EXTRA_TASK_ID = "task"; //$NON-NLS-1$
 
     private static final String CONTACT_CHOOSER_USER = "the_contact_user"; //$NON-NLS-1$
 
     private Task task;
 
     private final ArrayList<Metadata> nonSharedTags = new ArrayList<Metadata>();
 
     @Autowired ActFmPreferenceService actFmPreferenceService;
 
     @Autowired ActFmSyncService actFmSyncService;
 
     @Autowired TaskService taskService;
 
     @Autowired UserDao userDao;
 
     @Autowired MetadataService metadataService;
 
     @Autowired ExceptionService exceptionService;
 
     @Autowired TagDataService tagDataService;
 
     @Autowired ABChooser abChooser;
 
     private final Fragment fragment;
 
     private final PeopleContainer sharedWithContainer;
 
     private final CheckBox cbFacebook;
 
     private final CheckBox cbTwitter;
 
     private final View sharedWithRow;
 
     private final View sharedWithView;
 
     private final Dialog sharedWithDialog;
 
     private final ListView assignedList;
 
     private final TextView assignedDisplay;
 
     private final EditText assignedCustom;
 
     private final View assignedClear;
 
     private final int loginRequestCode;
 
     private boolean assignedToMe = false;
 
     private AssignedToUser taskRabbitUser = null;
 
     private AssignedToUser contactPickerUser = null;
 
     private boolean loadedUI = false;
 
     private boolean dontClearAssignedCustom = false;
 
     private final List<AssignedChangedListener> listeners = new LinkedList<AssignedChangedListener>();
 
     public interface AssignedChangedListener {
         public boolean showTaskRabbitForUser(String name, JSONObject json);
         public boolean shouldShowTaskRabbit();
         public boolean didPostToTaskRabbit();
     }
 
     private int selected = 0; //need to remember last selected state for task rabbit
 
     static {
         AstridDependencyInjector.initialize();
     }
 
     // --- UI initialization
 
     public EditPeopleControlSet(Activity activity, Fragment fragment, int viewLayout, int displayViewLayout, int title, int loginRequestCode) {
         super(activity, viewLayout, displayViewLayout, title);
         DependencyInjectionService.getInstance().inject(this);
         this.loginRequestCode = loginRequestCode;
         this.fragment = fragment;
         displayText.setText(activity.getString(R.string.TEA_control_who));
 
         sharedWithRow = LayoutInflater.from(activity).inflate(R.layout.control_set_default_display, null);
         sharedWithView = LayoutInflater.from(activity).inflate(R.layout.control_set_collaborators, null);
         TextView sharedRowDisplayText = (TextView)sharedWithRow.findViewById(R.id.display_row_title);
         sharedRowDisplayText.setText(activity.getString(R.string.actfm_EPA_collaborators_header));
 
         sharedWithDialog = new Dialog(activity, ThemeService.getEditDialogTheme());
         sharedWithDialog.setTitle(R.string.actfm_EPA_collaborators_header);
         View v = getSharedWithView();
         DisplayMetrics metrics = new DisplayMetrics();
         activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
         sharedWithDialog.setContentView(v, new LayoutParams(metrics.widthPixels - (int)(30 * metrics.density), LayoutParams.WRAP_CONTENT));
         Button dismiss = (Button) v.findViewById(R.id.edit_dlg_ok);
         if (dismiss != null) {
             dismiss.setOnClickListener(new OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     DialogUtilities.dismissDialog(EditPeopleControlSet.this.activity, sharedWithDialog);
                 }
             });
         }
         sharedWithDialog.setOwnerActivity(this.activity);
 
         assignedCustom = (EditText) getView().findViewById(R.id.assigned_custom);
         assignedList = (ListView) getView().findViewById(R.id.assigned_list);
         assignedList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
         assignedList.setItemsCanFocus(false);
         assignedClear = getView().findViewById(R.id.assigned_clear);
 
         assignedDisplay = (TextView) getDisplayView().findViewById(R.id.display_row_edit);
         sharedWithContainer = (PeopleContainer) getSharedWithView().findViewById(R.id.share_container);
         cbFacebook = (CheckBox) getSharedWithView().findViewById(R.id.checkbox_facebook);
         cbTwitter = (CheckBox) getSharedWithView().findViewById(R.id.checkbox_twitter);
 
         sharedWithContainer.addPerson();
         setUpListeners();
     }
 
     @Override
     public void readFromTask(Task sourceTask) {
         setTask(sourceTask);
         if (!dontClearAssignedCustom)
             assignedCustom.setText(""); //$NON-NLS-1$
         dontClearAssignedCustom = false;
         setUpData(task, null);
     }
 
     public void setTask(Task task) {
         this.task = task;
     }
 
     public View getSharedWithRow() {
         return sharedWithRow;
     }
 
     private View getSharedWithView() {
         return sharedWithView;
     }
 
     @SuppressWarnings("nls")
     public void setUpData(final Task task, final TagData includeTag) {
         try {
             JSONObject sharedWith;
             if(task.getValue(Task.SHARED_WITH).length() > 0)
                 sharedWith = new JSONObject(task.getValue(Task.SHARED_WITH));
             else
                 sharedWith = new JSONObject();
 
             cbFacebook.setChecked(sharedWith.optBoolean("fb", false));
             cbTwitter.setChecked(sharedWith.optBoolean("tw", false));
 
             final ArrayList<JSONObject> sharedPeople = new ArrayList<JSONObject>();
             JSONArray people = sharedWith.optJSONArray("p");
             if(people != null) {
                 for(int i = 0; i < people.length(); i++) {
                     String person = people.getString(i);
                     TextView textView = sharedWithContainer.addPerson(person, "");
                     textView.setEnabled(false);
                     sharedPeople.add(PeopleContainer.createUserJson(textView));
                 }
             }
 
             new Thread(new Runnable() {
                 @Override
                 public void run() {
                     ArrayList<JSONObject> collaborators = new ArrayList<JSONObject>();
                     TodorooCursor<Metadata> tags = TagService.getInstance().getTags(task.getId(), true);
                     try {
                         Metadata metadata = new Metadata();
                         for(tags.moveToFirst(); !tags.isAfterLast(); tags.moveToNext()) {
                             metadata.readFromCursor(tags);
                             final String tag = metadata.getValue(TagService.TAG);
                             TagData tagData = tagDataService.getTag(tag, TagData.MEMBER_COUNT, TagData.MEMBERS, TagData.USER);
                             if(tagData != null && tagData.getValue(TagData.MEMBER_COUNT) > 0) {
                                 try {
                                     addMembersFromTagData(tagData, tag, sharedPeople, collaborators);
                                 } catch (JSONException e) {
                                     exceptionService.reportError("json-reading-data", e);
                                 }
                             } else {
                                 nonSharedTags.add((Metadata) metadata.clone());
                             }
                         }
 
                         if (includeTag != null && tags.getCount() == 0) {
                             try {
                                 addMembersFromTagData(includeTag, null, sharedPeople, collaborators);
                             } catch (JSONException e) {
                                 exceptionService.reportError("json-reading-data", e);
                             }
                         }
 
                         if(collaborators.size() > 0)
                             buildCollaborators(collaborators);
                         buildAssignedToSpinner(task, sharedPeople);
                     } finally {
                         tags.close();
                         loadedUI = true;
                     }
                 }
             }).start();
 
         } catch (JSONException e) {
             exceptionService.reportError("json-reading-data", e);
         }
     }
 
     @SuppressWarnings("nls")
     private static void addMembersFromTagData(TagData tagData, String tag, ArrayList<JSONObject> sharedPeople, ArrayList<JSONObject> collaborators) throws JSONException {
         JSONArray members = new JSONArray(tagData.getValue(TagData.MEMBERS));
         if (tag == null)
             tag = tagData.getValue(TagData.NAME);
         for(int i = 0; i < members.length(); i++) {
             JSONObject user = members.getJSONObject(i);
             user.put("tag", tag);
             sharedPeople.add(user);
             collaborators.add(user);
         }
         if(!TextUtils.isEmpty(tagData.getValue(TagData.USER))) {
             JSONObject user = new JSONObject(tagData.getValue(TagData.USER));
             user.put("tag", tag);
             sharedPeople.add(user);
             collaborators.add(user);
         }
     }
 
     @SuppressWarnings("nls")
     private void buildCollaborators(final ArrayList<JSONObject> sharedPeople) {
 
         activity.runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 HashSet<Long> userIds = new HashSet<Long>();
                 LinearLayout collaborators = (LinearLayout) getSharedWithView().findViewById(R.id.collaborators);
 
                 for(JSONObject person : sharedPeople) {
                     if(person == null)
                         continue;
                     long id = person.optLong("id", -1);
                     if(id == 0 || id == ActFmPreferenceService.userId() || (id > -1 && userIds.contains(id)))
                         continue;
                     userIds.add(id);
 
                     View contact = activity.getLayoutInflater().inflate(R.layout.contact_adapter_row, collaborators, false);
                     AsyncImageView icon = (AsyncImageView) contact.findViewById(R.id.icon);
                     TextView name = (TextView) contact.findViewById(android.R.id.text1);
                     TextView tag = (TextView) contact.findViewById(android.R.id.text2);
 
                     icon.setUrl(person.optString("picture"));
                     name.setText(person.optString("name"));
                     name.setTextAppearance(activity, R.style.TextAppearance_Medium);
                     tag.setText(activity.getString(R.string.actfm_EPA_list, person.optString("tag")));
 
                     collaborators.addView(contact);
                 }
             }
         });
     }
 
     public static class AssignedToUser {
         public String label;
         public JSONObject user;
 
         public AssignedToUser(String label, JSONObject user) {
             super();
             this.label = label;
             this.user = user;
         }
 
         @Override
         public String toString() {
             return label;
         }
     }
 
     @SuppressWarnings("nls")
     private void buildAssignedToSpinner(Task t, ArrayList<JSONObject> sharedPeople) {
         HashSet<Long> userIds = new HashSet<Long>();
         HashSet<String> emails = new HashSet<String>();
         HashMap<String, AssignedToUser> names = new HashMap<String, AssignedToUser>();
 
         ArrayList<AssignedToUser> coreUsers = new ArrayList<AssignedToUser>();
         ArrayList<AssignedToUser> listUsers = new ArrayList<AssignedToUser>();
         ArrayList<AssignedToUser> astridUsers = new ArrayList<AssignedToUser>();
 
         int assignedIndex = 0;
         try {
             ArrayList<JSONObject> coreUsersJson = new ArrayList<JSONObject>();
             JSONObject myself = new JSONObject();
             myself.put("id", Task.USER_ID_SELF);
             myself.put("picture", ActFmPreferenceService.thisUser().optString("picture"));
             coreUsersJson.add(myself);
 
             boolean hasTags = t.getTransitory("tags") != null &&
                     ((HashSet<String>)t.getTransitory("tags")).size() > 0;
             boolean addUnassigned = actFmPreferenceService.isLoggedIn() && hasTags;
             if (addUnassigned) {
                 JSONObject unassigned = new JSONObject();
                 unassigned.put("id", Task.USER_ID_UNASSIGNED);
                 unassigned.put("default_picture", R.drawable.icn_anyone);
                 coreUsersJson.add(unassigned);
             }
 
             if(t.getValue(Task.USER_ID) > 0) {
                 JSONObject user = new JSONObject(t.getValue(Task.USER));
                 coreUsersJson.add(0, user);
             }
 
             ArrayList<JSONObject> astridFriends = getAstridFriends();
 
             // de-duplicate by user id and/or email
             coreUsers = convertJsonUsersToAssignedUsers(coreUsersJson, userIds, emails, names);
             listUsers = convertJsonUsersToAssignedUsers(sharedPeople, userIds, emails, names);
             astridUsers = convertJsonUsersToAssignedUsers(astridFriends, userIds, emails, names);
 
             contactPickerUser = new AssignedToUser(activity.getString(R.string.actfm_EPA_choose_contact),
                     new JSONObject().put("default_picture", R.drawable.icn_friends)
                     .put(CONTACT_CHOOSER_USER, true));
             int contactsIndex = addUnassigned ? 2 : 1;
             boolean addContactPicker = Preferences.getBoolean(R.string.p_use_contact_picker, true) && contactPickerAvailable();
             if (addContactPicker)
                 coreUsers.add(contactsIndex, contactPickerUser);
 
             for (AssignedChangedListener l : listeners) {
                 if (l.shouldShowTaskRabbit()) {
                     taskRabbitUser = new AssignedToUser(activity.getString(R.string.actfm_EPA_task_rabbit), new JSONObject().put("default_picture", R.drawable.task_rabbit_image));
                     int taskRabbitIndex = addUnassigned ? 3 : 2;
                     if (!addContactPicker)
                         taskRabbitIndex--;
                     coreUsers.add(taskRabbitIndex, taskRabbitUser);
                     if(l.didPostToTaskRabbit()){
                         assignedIndex = taskRabbitIndex;
                     }
                 }
             }
 
             if (assignedIndex == 0) {
                 assignedIndex = findAssignedIndex(t, coreUsers, listUsers, astridUsers);
             }
 
         } catch (JSONException e) {
             exceptionService.reportError("json-reading-data", e);
         }
 
         selected = assignedIndex;
 
         final MergeAdapter mergeAdapter = new MergeAdapter();
         AssignedUserAdapter coreUserAdapter = new AssignedUserAdapter(activity, coreUsers, 0);
         AssignedUserAdapter listUserAdapter = new AssignedUserAdapter(activity, listUsers, coreUserAdapter.getCount() + 1);
         int offsetForAstridUsers = listUserAdapter.getCount() > 0 ? 2 : 1;
         AssignedUserAdapter astridUserAdapter = new AssignedUserAdapter(activity, astridUsers, coreUserAdapter.getCount() + listUserAdapter.getCount() + offsetForAstridUsers);
 
         LayoutInflater inflater = activity.getLayoutInflater();
         TextView header1 = (TextView) inflater.inflate(R.layout.list_header, null);
         header1.setText(R.string.actfm_EPA_assign_header_members);
         TextView header2 = (TextView) inflater.inflate(R.layout.list_header, null);
         header2.setText(R.string.actfm_EPA_assign_header_friends);
 
         mergeAdapter.addAdapter(coreUserAdapter);
         if (listUserAdapter.getCount() > 0) {
             mergeAdapter.addView(header1);
             mergeAdapter.addAdapter(listUserAdapter);
         }
         if (astridUserAdapter.getCount() > 0) {
             mergeAdapter.addView(header2);
             mergeAdapter.addAdapter(astridUserAdapter);
         }
 
         activity.runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 assignedList.setAdapter(mergeAdapter);
                 assignedList.setItemChecked(selected, true);
                 refreshDisplayView();
             }
         });
     }
 
     @SuppressWarnings("nls")
     private ArrayList<AssignedToUser> convertJsonUsersToAssignedUsers(ArrayList<JSONObject> jsonUsers,
             HashSet<Long> userIds, HashSet<String> emails, HashMap<String, AssignedToUser> names) {
         ArrayList<AssignedToUser> users = new ArrayList<AssignedToUser>();
         for(int i = 0; i < jsonUsers.size(); i++) {
             JSONObject person = jsonUsers.get(i);
             if(person == null)
                 continue;
             long id = person.optLong("id", -2);
             if(id == ActFmPreferenceService.userId() || (id >= -1 && userIds.contains(id)))
                 continue;
             userIds.add(id);
 
             String email = person.optString("email");
             if(!TextUtils.isEmpty(email) && emails.contains(email))
                 continue;
             emails.add(email);
 
             String name = person.optString("name");
             if(id == 0)
                 name = activity.getString(R.string.actfm_EPA_assign_me);
             if (id == -1)
                 name = activity.getString(R.string.actfm_EPA_unassigned);
 
             AssignedToUser atu = new AssignedToUser(name, person);
             users.add(atu);
             if(names.containsKey(name)) {
                 AssignedToUser user = names.get(name);
                 if(user != null && user.user.has("email")) {
                     user.label += " (" + user.user.optString("email") + ")";
                     names.put(name, null);
                 }
                 if(!TextUtils.isEmpty(email))
                     atu.label += " (" + email + ")";
             } else if(TextUtils.isEmpty(name) || "null".equals(name)) {
                 if(!TextUtils.isEmpty(email))
                     atu.label = email;
                 else
                     users.remove(atu);
             } else
                 names.put(name, atu);
         }
         return users;
     }
 
     @SuppressWarnings("nls")
     private int findAssignedIndex(Task t, ArrayList<AssignedToUser>... userLists) throws JSONException {
         String assignedStr = t.getValue(Task.USER);
         if (!TextUtils.isEmpty(assignedStr)) {
             JSONObject assigned = new JSONObject(assignedStr);
             long assignedId = assigned.optLong("id", Task.USER_ID_EMAIL);
             String assignedEmail = assigned.optString("email");
 
             int index = 0;
             for (ArrayList<AssignedToUser> userList : userLists) {
                 for (int i = 0; i < userList.size(); i++) {
                     JSONObject user = userList.get(i).user;
                     if (user != null) {
                         if (user.optLong("id") == assignedId ||
                                 (user.optString("email").equals(assignedEmail) &&
                                         !(TextUtils.isEmpty(assignedEmail))))
                             return index;
                     }
                     index++;
                 }
                 index++; // Add one for headers separating user lists
             }
         }
         return 0;
     }
 
     private ArrayList<JSONObject> getAstridFriends() {
         ArrayList<JSONObject> astridFriends = new ArrayList<JSONObject>();
         TodorooCursor<User> users = userDao.query(Query.select(User.PROPERTIES)
                 .orderBy(Order.asc(Functions.caseStatement(Criterion.or(User.NAME.isNull(), User.NAME.eq("")), //$NON-NLS-1$
                         Functions.upper(User.EMAIL),
                         Functions.upper(User.NAME)))));
         try {
             User user = new User();
             for (users.moveToFirst(); !users.isAfterLast(); users.moveToNext()) {
                 user.readFromCursor(users);
                 JSONObject userJson = new JSONObject();
                 try {
                     ActFmSyncService.JsonHelper.jsonFromUser(userJson, user);
                     astridFriends.add(userJson);
                 } catch (JSONException e) {
                     // Ignored
                 }
             }
         } finally {
             users.close();
         }
         return astridFriends;
     }
 
 
 
     private class AssignedUserAdapter extends ArrayAdapter<AssignedToUser> {
 
         private final int positionOffset;
 
         public AssignedUserAdapter(Context context, ArrayList<AssignedToUser> people, int positionOffset)  {
             super(context, R.layout.assigned_adapter_row, people);
             this.positionOffset = positionOffset;
         }
 
         @SuppressWarnings("nls")
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             if(convertView == null)
                 convertView = activity.getLayoutInflater().inflate(R.layout.assigned_adapter_row, parent, false);
             CheckedTextView ctv = (CheckedTextView) convertView.findViewById(android.R.id.text1);
             super.getView(position, ctv, parent);
             if (assignedList.getCheckedItemPosition() == position + positionOffset) {
                 ctv.setChecked(true);
             } else {
                 ctv.setChecked(false);
             }
             AsyncImageView image = (AsyncImageView) convertView.findViewById(R.id.person_image);
             image.setDefaultImageResource(R.drawable.icn_default_person_image);
             image.setUrl(getItem(position).user.optString("picture"));
             if (getItem(position).user.optInt("default_picture", 0) > 0) {
                 image.setDefaultImageResource(getItem(position).user.optInt("default_picture"));
             }
             return convertView;
         }
     }
 
     public void assignToMe() {
         if (assignedList != null && assignedList.getChildAt(0) != null) {
             assignedList.performItemClick(assignedList.getChildAt(0), 0, 0);
             refreshDisplayView();
         }
     }
 
     private void setUpListeners() {
 
         assignedList.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                     long id) {
                 AssignedToUser user = (AssignedToUser) assignedList.getAdapter().getItem(position);
 
                 for (AssignedChangedListener l : listeners) {
                     if(l.showTaskRabbitForUser(user.label, user.user)) {
                         assignedDisplay.setText(user.toString());
                         assignedCustom.setText(""); //$NON-NLS-1$
                         DialogUtilities.dismissDialog(activity, dialog);
                         return;
                     }
 
                 }
 
                 if (user.user.has(CONTACT_CHOOSER_USER)) {
                     Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                     fragment.startActivityForResult(intent, TaskEditFragment.REQUEST_CODE_CONTACT);
                     return;
                 }
 
                 assignedDisplay.setText(user.toString());
                 assignedCustom.setText(""); //$NON-NLS-1$
                 selected = position;
                 refreshDisplayView();
                 DialogUtilities.dismissDialog(activity, dialog);
             }
 
         });
 
         assignedClear.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 assignedCustom.setText(""); //$NON-NLS-1$
                 selected = 0;
                 assignedList.setItemChecked(selected, true);
             }
         });
 
         sharedWithContainer.setOnAddNewPerson(new OnAddNewPersonListener() {
             @Override
             public void textChanged(String text) {
                 getSharedWithView().findViewById(R.id.share_additional).setVisibility(View.VISIBLE);
                 if(text.indexOf('@') > -1) {
 //                    getSharedWithView().findViewById(R.id.tag_label).setVisibility(View.VISIBLE);
 //                    getSharedWithView().findViewById(R.id.tag_name).setVisibility(View.VISIBLE);
                 }
             }
         });
 
         sharedWithRow.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 sharedWithDialog.show();
             }
         });
     }
 
     private boolean contactPickerAvailable() {
         Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
         return activity.getPackageManager().queryIntentActivities(intent, 0).size() > 0;
     }
 
     // --- events
 
     @Override
     protected void readFromTaskOnInitialize() {
         // Nothing, we don't lazy load this control set yet
     }
 
     @Override
     public String writeToModel(Task t) {
         // do nothing, we use a separate method
         return null;
     }
 
     @Override
     protected String writeToModelAfterInitialized(Task t) {
         // Nothing, we don't lazy load this control set yet
         return null;
     }
 
     @Override
     protected void afterInflate() {
         // Nothing, we don't lazy load this control set yet
     }
 
     public boolean hasLoadedUI() {
         return loadedUI;
     }
 
     /**
      * Save sharing settings
      * @param toast toast to show after saving is finished
      * @return false if login is required & save should be halted
      */
     @SuppressWarnings("nls")
     public boolean saveSharingSettings(String toast) {
         if(task == null)
             return false;
 
         boolean dirty = false;
         try {
             JSONObject userJson = null;
             TextView assignedView = null;
             if(!TextUtils.isEmpty(assignedCustom.getText())) {
                 userJson = PeopleContainer.createUserJson(assignedCustom);
                 assignedView = assignedCustom;
             } else {
                 if (!loadedUI || assignedList.getCheckedItemPosition() == ListView.INVALID_POSITION)
                     return true;
                 AssignedToUser item = (AssignedToUser) assignedList.getAdapter().getItem(assignedList.getCheckedItemPosition());
                 if (item != null) {
                     if (item.equals(taskRabbitUser) || item.equals(contactPickerUser)) { //don't want to ever set the user as the task rabbit user
                         return true;
                     }
                     userJson = item.user;
                 }
             }
 
             if (userJson != null) {
                 String email = userJson.optString("email");
                 if (!TextUtils.isEmpty(email) && email.indexOf('@') == -1)
                     throw new ParseSharedException(assignedView,
                             activity.getString(R.string.actfm_EPA_invalid_email, userJson.optString("email")));
             }
 
             if(userJson == null || userJson.optLong("id", Task.USER_ID_EMAIL) == Task.USER_ID_SELF) {
                 dirty = task.getValue(Task.USER_ID) == Task.USER_ID_SELF ? dirty : true;
                 task.setValue(Task.USER_ID, Task.USER_ID_SELF);
                 if(!TextUtils.isEmpty(task.getValue(Task.USER)))
                     task.setValue(Task.USER, "");
 
                 assignedToMe = true;
             } else if(userJson.optLong("id") == Task.USER_ID_UNASSIGNED) {
                 dirty = task.getValue(Task.USER_ID) == Task.USER_ID_UNASSIGNED ? dirty : true;
                 task.setValue(Task.USER_ID, Task.USER_ID_UNASSIGNED);
             } else {
                 String user = userJson.toString();
 
                 long taskUserId = -2;
                 String taskUserEmail = "";
                 try {
                     JSONObject taskUser = new JSONObject(task.getValue(Task.USER));
                     taskUserId = taskUser.optLong("id", Task.USER_ID_EMAIL);
                     taskUserEmail = taskUser.optString("email");
                 } catch (JSONException e) {
                     // sad times
                 }
                 long userId = userJson.optLong("id", Task.USER_ID_EMAIL);
                 String userEmail = userJson.optString("email");
 
 
                 boolean match = (userId == taskUserId && userId != Task.USER_ID_EMAIL);
                 match = match || (userEmail.equals(taskUserEmail) && !TextUtils.isEmpty(userEmail));
 
                 dirty = match ? dirty : true;
                 task.setValue(Task.USER_ID, userJson.optLong("id", Task.USER_ID_EMAIL));
                 task.setValue(Task.USER, user);
 
                 String assignedName = userJson.optString("name", userEmail);
             }
 
             JSONObject sharedWith = sharedWithContainer.parseSharedWithAndTags(activity, false);
             EditText message = (EditText) getSharedWithView().findViewById(R.id.message);
             if (!TextUtils.isEmpty(message.getText()))
                 sharedWith.put("message", message.getText().toString());
 
             if(cbFacebook.isChecked())
                 sharedWith.put("fb", true);
             if(cbTwitter.isChecked())
                 sharedWith.put("tw", true);
 
             dirty = dirty || sharedWith.has("p");
             if(dirty && !actFmPreferenceService.isLoggedIn()) {
                 DialogInterface.OnClickListener okListener = new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface d, int which) {
                         fragment.startActivityForResult(new Intent(activity, ActFmLoginActivity.class),
                                 loginRequestCode);
                     }
                 };
 
                 DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface d, int which) {
                         makePrivateTask();
                         AssignedToUser me = (AssignedToUser) assignedList.getAdapter().getItem(0);
                         task.setValue(Task.USER_ID, me.user.optLong("id", Task.USER_ID_EMAIL));
                         task.setValue(Task.USER, me.user.toString());
                     }
                 };
                 DialogUtilities.okCancelCustomDialog(activity, activity.getString(R.string.actfm_EPA_login_button),
                         activity.getString(R.string.actfm_EPA_login_to_share), R.string.actfm_EPA_login_button,
                         R.string.actfm_EPA_dont_share_button, android.R.drawable.ic_dialog_alert,
                         okListener, cancelListener);
                 showSaveToast(toast);
 
                 return false;
             }
 
             if(!TextUtils.isEmpty(task.getValue(Task.SHARED_WITH)) || sharedWith.length() != 0)
                 task.setValue(Task.SHARED_WITH, sharedWith.toString());
 
             task.putTransitory(TaskService.TRANS_ASSIGNED, true);
 
             if (assignedView == assignedCustom)
                 StatisticsService.reportEvent(StatisticsConstants.TASK_ASSIGNED_EMAIL);
             else if (task.getValue(Task.USER_ID) != Task.USER_ID_SELF)
                 StatisticsService.reportEvent(StatisticsConstants.TASK_ASSIGNED_PICKER);
 
             showSaveToast(toast);
 
             return true;
         } catch (JSONException e) {
             exceptionService.displayAndReportError(activity, "save-people", e);
         } catch (ParseSharedException e) {
             if(e.view != null) {
                 e.view.setTextColor(Color.RED);
                 e.view.requestFocus();
             }
             DialogUtilities.okDialog(activity, e.message, null);
         }
         return false;
     }
 
     private void makePrivateTask() {
         sharedWithContainer.removeAllViews();
         sharedWithContainer.addPerson();
         assignToMe();
     }
 
     private void showSaveToast(String saveToast) {
         if(saveToast == null) return;
         int length = saveToast.contains("\n") ? //$NON-NLS-1$
                 Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
         Toast.makeText(activity, saveToast, length).show();
     }
 
     @SuppressWarnings("nls")
     protected Object[] buildSharingArgs(JSONArray emails) throws JSONException {
         ArrayList<Object> values = new ArrayList<Object>();
 
         if(task.containsNonNullValue(Task.REMOTE_ID)) {
             long currentTaskID = task.getValue(Task.REMOTE_ID);
             values.add("id");
             values.add(currentTaskID);
         }
 
         if(emails != null) {
             for(int i = 0; i < emails.length(); i++) {
                 String email = emails.optString(i);
                 if(email == null || email.indexOf('@') == -1)
                     continue;
                 values.add("emails[]");
                 values.add(email);
             }
         }
 
         if(task.getValue(Task.USER_ID) == 0) {
             values.add("assign_id");
             values.add(ActFmPreferenceService.userId());
         } else {
             long id = task.getValue(Task.USER_ID);
             if(id > 0 || id == -1) {
                 values.add("assign_id");
                 values.add(task.getValue(Task.USER_ID));
             } else {
                 values.add("assign_email");
                 JSONObject user = new JSONObject(task.getValue(Task.USER));
                 String userEmail = user.getString("email");
                 if (userEmail.indexOf('@') == -1)
                     values.add("");
                 else
                     values.add(user.getString("email"));
             }
         }
 
         String message = ((TextView) getSharedWithView().findViewById(R.id.message)).getText().toString();
         if(!TextUtils.isEmpty(message) && getSharedWithView().findViewById(R.id.share_additional).getVisibility() == View.VISIBLE) {
             values.add("message");
             values.add(message);
         }
 
 //        String tag = ((TextView) getSharedWithView().findViewById(R.id.tag_name)).getText().toString();
 //        if(!TextUtils.isEmpty(tag)) {
 //            values.add("tag");
 //            values.add(tag);
 //        }
 
         return values.toArray(new Object[values.size()]);
     }
 
     /**
      * Warning - only valid after a call to saveSharingSettings
      * @return
      */
     public boolean isAssignedToMe() {
         return assignedToMe;
     }
 
     /**
      * Check if task will be assigned to current user when save setting is called
      */
     public boolean willBeAssignedToMe() {
         JSONObject userJson = null;
         if(!TextUtils.isEmpty(assignedCustom.getText())) {
             userJson = PeopleContainer.createUserJson(assignedCustom);
         } else {
             if (!hasLoadedUI() || assignedList.getCheckedItemPosition() == ListView.INVALID_POSITION) {
                 if (task != null)
                     return task.getValue(Task.USER_ID) == Task.USER_ID_SELF;
                 else
                     return true;
             }
             AssignedToUser item = (AssignedToUser) assignedList.getAdapter().getItem(assignedList.getCheckedItemPosition());
             if (item != null)
                 userJson = item.user;
         }
 
         if(userJson == null || userJson.optLong("id", Task.USER_ID_EMAIL) == Task.USER_ID_SELF) { //$NON-NLS-1$
             return true;
         }
 
         return false;
     }
 
     public String getAssignedToString() {
         return assignedDisplay.getText().toString();
     }
 
     /** Resume save
      * @param data */
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         if(requestCode == loginRequestCode && resultCode == Activity.RESULT_OK) {
             // clear user values & reset them to trigger a save
             task.clearValue(Task.USER_ID);
             task.clearValue(Task.USER);
         } else if (requestCode == loginRequestCode) {
             makePrivateTask();
         } else if (requestCode == TaskEditFragment.REQUEST_CODE_CONTACT && resultCode == Activity.RESULT_OK) {
             Uri contactData = data.getData();
             String contactId = contactData.getLastPathSegment();
             String[] args = { contactId };
             String[] projection = { ContactsContract.CommonDataKinds.Email.DATA };
             String selection = ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?"; //$NON-NLS-1$
             Cursor emailCursor = activity.managedQuery(ContactsContract.CommonDataKinds.Email.CONTENT_URI, projection, selection, args, null);
             if (emailCursor.getCount() > 0) {
                 emailCursor.moveToFirst();
                 int emailIndex = emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
                 String email = emailCursor.getString(emailIndex);
                 if (!TextUtils.isEmpty(email)) {
                     String[] nameProjection = { ContactsContract.Contacts.DISPLAY_NAME };
                     Cursor nameCursor = activity.managedQuery(contactData, nameProjection, null, null, null);
                     if (nameCursor.getCount() > 0) {
                         nameCursor.moveToFirst();
                         int nameIndex = nameCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                         String name = nameCursor.getString(nameIndex);
                         if (!TextUtils.isEmpty(name)) {
                             StringBuilder fullName = new StringBuilder();
                             fullName.append(name).append(" <").append(email).append('>'); //$NON-NLS-1$
                             email = fullName.toString();
                         }
                     }
                     assignedCustom.setText(email);
                     dontClearAssignedCustom = true;
                     refreshDisplayView();
                     if (dialog != null)
                         dialog.dismiss();
                 } else {
                     DialogUtilities.okDialog(activity, activity.getString(R.string.TEA_contact_error), null);
                 }
             } else {
                 DialogUtilities.okDialog(activity, activity.getString(R.string.TEA_contact_error), null);
             }
         }
     }
 
     @Override
     protected void refreshDisplayView() {
         if (!TextUtils.isEmpty(assignedCustom.getText())) {
             assignedDisplay.setText(assignedCustom.getText());
         } else {
             AssignedToUser user = (AssignedToUser) assignedList.getAdapter().getItem(assignedList.getCheckedItemPosition());
             if (user == null)
                 user = (AssignedToUser) assignedList.getAdapter().getItem(0);
             assignedDisplay.setText(user.toString());
         }
     }
 
     @Override
     protected boolean onOkClick() {
         if (!TextUtils.isEmpty(assignedCustom.getText())) {
             JSONObject assigned = PeopleContainer.createUserJson(assignedCustom);
             String email = assigned.optString("email"); //$NON-NLS-1$
             if (!TextUtils.isEmpty(email) && email.indexOf('@') == -1) {
                 assignedCustom.requestFocus();
                 DialogUtilities.okDialog(activity, activity.getString(R.string.actfm_EPA_invalid_email,
                         assigned.optString("email")), null); //$NON-NLS-1$
                 return false;
             }
         }
         return super.onOkClick();
     }
 
     @Override
     protected void additionalDialogSetup() {
         super.additionalDialogSetup();
         dialog.getWindow()
             .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                     | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
     }
 
     public void addListener(AssignedChangedListener listener) {
         listeners.add(listener);
     }
 
     public void removeListener(AssignedChangedListener listener) {
         if (listeners.contains(listener))
             listeners.remove(listener);
     }
 }
