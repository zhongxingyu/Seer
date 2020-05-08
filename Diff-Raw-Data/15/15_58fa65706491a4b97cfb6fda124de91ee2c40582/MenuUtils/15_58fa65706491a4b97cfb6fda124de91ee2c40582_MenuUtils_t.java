 /*
  * Copyright (C) 2009 Android Shuffle Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.dodgybits.shuffle.android.core.view;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.SubMenu;
 import org.dodgybits.android.shuffle.R;
 import org.dodgybits.shuffle.android.core.activity.HelpActivity;
 import org.dodgybits.shuffle.android.list.activity.ContextsActivity;
 import org.dodgybits.shuffle.android.list.activity.ProjectsActivity;
 import org.dodgybits.shuffle.android.list.activity.State;
 import org.dodgybits.shuffle.android.list.activity.expandable.ExpandableContextsActivity;
 import org.dodgybits.shuffle.android.list.activity.expandable.ExpandableProjectsActivity;
 import org.dodgybits.shuffle.android.list.activity.task.*;
 import org.dodgybits.shuffle.android.preference.activity.PreferencesActivity;
 import org.dodgybits.shuffle.android.preference.model.Preferences;
 import org.dodgybits.shuffle.android.synchronisation.tracks.activity.SynchronizeActivity;
 
 public class MenuUtils {
 	private static final String cTag = "MenuUtils";
 
 
     private MenuUtils() {
 		// deny
 	}
 	
     // Identifiers for our menu items.
     public static final int SAVE_ID = Menu.FIRST;
     public static final int SAVE_AND_ADD_ID = Menu.FIRST + 1;
     public static final int REVERT_ID = Menu.FIRST + 2;
     public static final int DISCARD_ID = Menu.FIRST + 3;
     public static final int DELETE_ID = Menu.FIRST + 4;
     public static final int INSERT_ID = Menu.FIRST + 5;
     public static final int INSERT_CHILD_ID = Menu.FIRST + 6;
     public static final int INSERT_GROUP_ID = Menu.FIRST + 7;
     
     public static final int INBOX_ID = Menu.FIRST + 10;
     public static final int CALENDAR_ID = Menu.FIRST + 11;
     public static final int TOP_TASKS_ID = Menu.FIRST + 12;
     public static final int PROJECT_ID = Menu.FIRST + 13;
     public static final int CONTEXT_ID = Menu.FIRST + 14;
     public static final int TICKLER_ID = Menu.FIRST + 15;
 
     public static final int PREFERENCE_ID = Menu.FIRST + 20;
     public static final int HELP_ID = Menu.FIRST + 21;
     public static final int SYNC_ID = Menu.FIRST + 22;
     public static final int SEARCH_ID = Menu.FIRST + 23;
     public static final int CLEAN_INBOX_ID = Menu.FIRST + 50;
     public static final int PERMANENTLY_DELETE_ID = Menu.FIRST + 51;
 
     // Menu item for activity specific items
     public static final int PUT_BACK_ID = Menu.FIRST + 100;
     public static final int COMPLETE_ID = Menu.FIRST + 101;
     public static final int MOVE_UP_ID = Menu.FIRST + 102;
     public static final int MOVE_DOWN_ID = Menu.FIRST + 103;
     
     // Editor menus
     private static final int SAVE_ORDER = 1;
     private static final int SAVE_AND_ADD_ORDER = 2;
     private static final int REVERT_ORDER = 3;
     private static final int DISCARD_ORDER = 3;
     
     // Context menus
     private static final int EDIT_ORDER = 1;
     
     private static final int PUT_BACK_ORDER = 3;
     private static final int COMPLETE_ORDER = 4;
     private static final int MOVE_UP_ORDER = 5;
     private static final int MOVE_DOWN_ORDER = 6;
     private static final int DELETE_ORDER = 10;
     
     // List menus
     private static final int INSERT_ORDER = 1;
     private static final int INSERT_CHILD_ORDER = 1;
     private static final int INSERT_GROUP_ORDER = 2;
     private static final int CLEAN_INBOX_ORDER = 101;
     private static final int PERMANENTLY_DELETE_ORDER = 102;
     
     
     // General menus
     private static final int PERSPECTIVE_ORDER = 201;
     private static final int PREFERENCE_ORDER = 202;
     private static final int SYNCH_ORDER = 203;
     private static final int SEARCH_ORDER = 204;
     private static final int HELP_ORDER = 205;
     
 	public static void addInsertMenuItems(Menu menu, String itemName, boolean isTaskList, Context context) {
 		String menuName = context.getResources().getString(R.string.menu_insert, itemName);
 		menu.add(Menu.NONE, INSERT_ID, INSERT_ORDER, menuName)
 			.setIcon(android.R.drawable.ic_menu_add)
 			.setAlphabeticShortcut(isTaskList ? 'c' : 'a');
 	}
 
 	public static void addExpandableInsertMenuItems(Menu menu, String groupName, String childName, Context context) {
 		String menuName;
 		menuName = context.getResources().getString(R.string.menu_insert, childName);
         menu.add(Menu.NONE, INSERT_CHILD_ID, INSERT_CHILD_ORDER, menuName)
         	.setIcon(android.R.drawable.ic_menu_add).setAlphabeticShortcut('c');
 		menuName = context.getResources().getString(R.string.menu_insert, groupName);
         menu.add(Menu.NONE, INSERT_GROUP_ID, INSERT_GROUP_ORDER, menuName)
         	.setIcon(android.R.drawable.ic_menu_add).setAlphabeticShortcut('a');
 	}
 
 	public static void addViewMenuItems(Menu menu, int currentViewMenuId) {
         SubMenu viewMenu  = menu.addSubMenu(Menu.NONE, Menu.NONE, PERSPECTIVE_ORDER, R.string.menu_view)
         	.setIcon(R.drawable.preferences_system_windows);
         viewMenu.add(Menu.NONE, INBOX_ID, 0, R.string.title_inbox)
         	.setChecked(INBOX_ID == currentViewMenuId);
         viewMenu.add(Menu.NONE, CALENDAR_ID, 1, R.string.title_due_tasks)
         	.setChecked(CALENDAR_ID == currentViewMenuId);
         viewMenu.add(Menu.NONE, TOP_TASKS_ID, 2, R.string.title_next_tasks)
         	.setChecked(TOP_TASKS_ID == currentViewMenuId);
         viewMenu.add(Menu.NONE, PROJECT_ID, 3, R.string.title_project)
         	.setChecked(PROJECT_ID == currentViewMenuId);
         viewMenu.add(Menu.NONE, CONTEXT_ID, 4, R.string.title_context)
         	.setChecked(CONTEXT_ID == currentViewMenuId);
        viewMenu.add(Menu.NONE, TICKLER_ID, 5, R.string.title_tickler)
             .setChecked(TICKLER_ID == currentViewMenuId);
 	}
 	
 	public static void addEditorMenuItems(Menu menu, int state) {
         menu.add(Menu.NONE, SAVE_ID, SAVE_ORDER, R.string.menu_save)
     		.setIcon(android.R.drawable.ic_menu_save).setAlphabeticShortcut('s');
         menu.add(Menu.NONE, SAVE_AND_ADD_ID, SAVE_AND_ADD_ORDER, R.string.menu_save_and_add)
         	.setIcon(android.R.drawable.ic_menu_save);
         // Build the menus that are shown when editing.
         if (state == State.STATE_EDIT) {
             menu.add(Menu.NONE, REVERT_ID, REVERT_ORDER, R.string.menu_revert)
             	.setIcon(android.R.drawable.ic_menu_revert).setAlphabeticShortcut('r');
             menu.add(Menu.NONE, DELETE_ID, DELETE_ORDER, R.string.menu_delete)
             	.setIcon(android.R.drawable.ic_menu_delete).setAlphabeticShortcut('d');
 
         // Build the menus that are shown when inserting.
         } else {
             menu.add(Menu.NONE, DISCARD_ID, DISCARD_ORDER, R.string.menu_discard)
             	.setIcon(android.R.drawable.ic_menu_close_clear_cancel).setAlphabeticShortcut('d');
         }
 	}
 	
 	public static void addPrefsHelpMenuItems(Context context, Menu menu) {
         menu.add(Menu.NONE, PREFERENCE_ID, PREFERENCE_ORDER, R.string.menu_preferences)
         	.setIcon(android.R.drawable.ic_menu_preferences).setAlphabeticShortcut('p');
         menu.add(Menu.NONE, HELP_ID, HELP_ORDER, R.string.menu_help)
         	.setIcon(android.R.drawable.ic_menu_help).setAlphabeticShortcut('h');
 	}
 	
     public static void addSyncMenuItem(Context context, Menu menu) {
         menu.add(Menu.NONE, SYNC_ID, SYNCH_ORDER, R.string.menu_sync)
         .setIcon(android.R.drawable.ic_menu_rotate).setVisible(Preferences.validateTracksSettings(context));
     }
 
     public static void addSearchMenuItem(Context context, Menu menu) {
         menu.add(Menu.NONE, SEARCH_ID, SEARCH_ORDER, R.string.menu_search)
         .setIcon(android.R.drawable.ic_menu_search).setAlphabeticShortcut('s');
     }
     
 	public static void addSelectedAlternativeMenuItems(Menu menu, Uri uri, boolean includeView) {
         // Build menu...  always starts with the EDIT action...
 		
         int viewIndex = 0;
         int editIndex = (includeView ? 1 : 0);
         Intent[] specifics = new Intent[editIndex + 1];
         MenuItem[] items = new MenuItem[editIndex + 1];
         if (includeView) {
 	        specifics[viewIndex] = new Intent(Intent.ACTION_VIEW, uri);
         }
         specifics[editIndex] = new Intent(Intent.ACTION_EDIT, uri);
 
         // ... is followed by whatever other actions are available...
         Intent intent = new Intent(null, uri);
         intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
         menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, Menu.NONE, EDIT_ORDER, null, specifics,
                               intent, 0, items);
 
         // Give a shortcut to the edit action.
         if (items[editIndex] != null) {
             items[editIndex].setAlphabeticShortcut('e');
             items[editIndex].setIcon(android.R.drawable.ic_menu_edit);
         }		
         if (includeView && items[viewIndex] != null) {
             items[viewIndex].setAlphabeticShortcut('v');
             items[viewIndex].setIcon(android.R.drawable.ic_menu_view);
         }		
 	}
 	
 	public static void addPutBackMenuItem(Menu menu) {
 	    menu.add(Menu.CATEGORY_ALTERNATIVE, PUT_BACK_ID, PUT_BACK_ORDER, R.string.menu_put_back);
 	}
 	
 	public static void addCompleteMenuItem(Menu menu, boolean isComplete) {
         int labelId = isComplete ? R.string.menu_incomplete : R.string.menu_complete;
         menu.add(Menu.CATEGORY_ALTERNATIVE, COMPLETE_ID, COMPLETE_ORDER, labelId)
         	.setIcon(R.drawable.btn_check_on).setAlphabeticShortcut('x');
     }
 
 	public static void addDeleteMenuItem(Menu menu, boolean isDeleted) {
         int labelId = isDeleted ? R.string.menu_undelete : R.string.menu_delete;
         menu.add(Menu.CATEGORY_ALTERNATIVE, DELETE_ID, DELETE_ORDER, labelId)
         	.setIcon(android.R.drawable.ic_menu_delete).setAlphabeticShortcut('d');
     }
 
 	public static void addCleanInboxMenuItem(Menu menu) {
         menu.add(Menu.NONE, CLEAN_INBOX_ID, CLEAN_INBOX_ORDER, R.string.clean_inbox_button_title)
         	.setIcon(R.drawable.edit_clear).setAlphabeticShortcut('i');
     }
 	
 	public static void addPermanentlyDeleteMenuItem(Menu menu) {
 	    menu.add(Menu.NONE, PERMANENTLY_DELETE_ID, PERMANENTLY_DELETE_ORDER, R.string.permanently_delete_button_title)
 	    .setIcon(R.drawable.icon_delete);
 	}
 
 	public static void addMoveMenuItems(Menu menu, boolean enableUp, boolean enableDown) {
 		if (enableUp) {
 	        menu.add(Menu.CATEGORY_ALTERNATIVE, MOVE_UP_ID, MOVE_UP_ORDER, R.string.menu_move_up)
 	        	.setIcon(R.drawable.go_up).setAlphabeticShortcut('k');
 		}
 		if (enableDown) {
 	        menu.add(Menu.CATEGORY_ALTERNATIVE, MOVE_DOWN_ID, MOVE_DOWN_ORDER, R.string.menu_move_down)
 	        	.setIcon(R.drawable.go_down).setAlphabeticShortcut('j');
 		}
     }
 
 	public static boolean checkCommonItemsSelected(MenuItem item, Activity activity, int currentViewMenuId) {
 		return checkCommonItemsSelected(item.getItemId(), activity, currentViewMenuId, true);
 	}
 	
 	public static boolean checkCommonItemsSelected(int menuItemId, Activity activity, int currentViewMenuId) {
 		return checkCommonItemsSelected(menuItemId, activity, currentViewMenuId, true);
 	}
 	
 	public static boolean checkCommonItemsSelected(int menuItemId, Activity activity, int currentViewMenuId, boolean finishCurrentActivity) {
 		switch (menuItemId) {
         case MenuUtils.INBOX_ID:
         	if (currentViewMenuId != INBOX_ID) {
             	Log.d(cTag, "Switching to inbox");
             	activity.startActivity(new Intent(activity, InboxActivity.class));
         		if (finishCurrentActivity) activity.finish();
         	}
         	return true;
         case MenuUtils.CALENDAR_ID:
         	if (currentViewMenuId != CALENDAR_ID) {
             	Log.d(cTag, "Switching to calendar");
             	activity.startActivity(new Intent(activity, TabbedDueActionsActivity.class));
         		if (finishCurrentActivity) activity.finish();
         	}
         	return true;
         case MenuUtils.TOP_TASKS_ID:
         	if (currentViewMenuId != TOP_TASKS_ID) {
         		Log.d(cTag, "Switching to top tasks");
         		activity.startActivity(new Intent(activity, TopTasksActivity.class));
         		if (finishCurrentActivity) activity.finish();
         	}
         	return true;
         case MenuUtils.PROJECT_ID:
         	if (currentViewMenuId != PROJECT_ID) {
             	Log.d(cTag, "Switching to project list");
                 Class<? extends Activity> activityClass = null;
             	if (Preferences.isProjectViewExpandable(activity)) {
                 	activityClass = ExpandableProjectsActivity.class;
             	} else {
                 	activityClass = ProjectsActivity.class;
             	}
             	activity.startActivity(new Intent(activity, activityClass));
         		if (finishCurrentActivity) activity.finish();
         	}
         	return true;
         case CONTEXT_ID:
         	if (currentViewMenuId != CONTEXT_ID) {
             	Log.d(cTag, "Switching to context list");
                 Class<? extends Activity> activityClass = null;
             	if (Preferences.isContextViewExpandable(activity)) {
                 	activityClass = ExpandableContextsActivity.class;
             	} else {
                 	activityClass = ContextsActivity.class;
             	}
             	activity.startActivity(new Intent(activity, activityClass));
         		if (finishCurrentActivity) activity.finish();
         	}
         	return true;
         case TICKLER_ID:
             Log.d(cTag, "Switching to tickler list");
             activity.startActivity(new Intent(activity, TicklerActivity.class));
             return true;
 
         case PREFERENCE_ID:
         	Log.d(cTag, "Bringing up preferences");
         	activity.startActivity(new Intent(activity, PreferencesActivity.class));
         	return true;
 	    case HELP_ID:
 	    	Log.d(cTag, "Bringing up help");
 	    	Intent intent = new Intent(activity, HelpActivity.class);
 	    	intent.putExtra(HelpActivity.cHelpPage, getHelpScreen(currentViewMenuId));
 	    	activity.startActivity(intent);
 	    	return true;
         case SYNC_ID:
             Log.d(cTag, "starting sync");
 	    	activity.startActivity(new Intent(activity, SynchronizeActivity.class));
             return true;
         case SEARCH_ID:
             Log.d(cTag, "starting search");
             activity.onSearchRequested();
             return true;
 
         }
         return false;
 	}
 	
 	private static int getHelpScreen(int currentViewMenuId) {
 		int result = 0;
 		switch (currentViewMenuId) {
 		case INBOX_ID:
 			result = 1;
 			break;
 		case PROJECT_ID:
 			result = 2;
 			break;
 		case CONTEXT_ID:
 			result = 3;
 			break;
 		case TOP_TASKS_ID:
 			result = 4;
 			break;
 		case CALENDAR_ID:
 			result = 5;
 			break;
 		}
 		return result;
 	}
 	
 }
