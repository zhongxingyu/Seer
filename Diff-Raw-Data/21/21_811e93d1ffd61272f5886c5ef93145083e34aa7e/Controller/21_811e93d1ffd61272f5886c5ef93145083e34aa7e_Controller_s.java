 /*
  * Copyright (C) 2011 The original author or authors.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  * in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License
  * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  * or implied. See the License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.zapta.apps.maniana.controller;
 
 import static com.zapta.apps.maniana.util.Assertions.check;
 
 import java.util.ArrayList;
 
 import javax.annotation.Nullable;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.speech.RecognizerIntent;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.zapta.apps.maniana.R;
 import com.zapta.apps.maniana.editors.ItemEditor;
 import com.zapta.apps.maniana.help.PopupMessageActivity;
 import com.zapta.apps.maniana.help.PopupMessageActivity.MessageKind;
 import com.zapta.apps.maniana.main.AppContext;
 import com.zapta.apps.maniana.main.ResumeAction;
 import com.zapta.apps.maniana.model.ItemColor;
 import com.zapta.apps.maniana.model.ItemModel;
 import com.zapta.apps.maniana.model.ItemModelReadOnly;
 import com.zapta.apps.maniana.model.PushScope;
 import com.zapta.apps.maniana.model.OrganizePageSummary;
 import com.zapta.apps.maniana.model.PageKind;
 import com.zapta.apps.maniana.model.persistence.ModelPersistence;
 import com.zapta.apps.maniana.model.persistence.PersistenceMetadata;
 import com.zapta.apps.maniana.preferences.PreferenceKind;
 import com.zapta.apps.maniana.preferences.PreferencesActivity;
 import com.zapta.apps.maniana.quick_action.QuickActionItem;
 import com.zapta.apps.maniana.util.LogUtil;
 import com.zapta.apps.maniana.view.AppView;
 import com.zapta.apps.maniana.view.AppView.ItemAnimationType;
 import com.zapta.apps.maniana.widget.BaseWidgetProvider;
 
 /**
  * The controller class. Contains main app logic. Interacts with the model (data) and view
  * (display).
  * 
  * @author Tal Dayan
  */
 public class Controller {
 
     private static final int VOICE_RECOGNITION_REQUEST_CODE = 1001;
 
     /** The app context. Provide access to the model, view and services. */
     private final AppContext mApp;
 
     private final QuickActionsCache mQuickActionCache;
 
     /** Used to detect first app resume to trigger the startup animation. */
     private int mOnAppResumeCount = 0;
 
     /**
      * Used to determine if the resume is from own sub activity (e.g. voice or help) as opposed to
      * app re-entry.
      */
     private boolean mInSubActivity = false;
 
     public Controller(AppContext app) {
         mApp = app;
         mQuickActionCache = new QuickActionsCache(app);
     }
 
     /** Called by the view when user clicks on item's text area */
     public void onItemTextClick(PageKind pageKind, int itemIndex) {
         mApp.services().maybePlayStockSound(AudioManager.FX_KEY_CLICK, false);
         showItemMenu(pageKind, itemIndex);
     }
 
     /** Called the view when user clicks on item's color swatch area */
     public final void onItemColorClick(final PageKind pageKind, final int itemIndex) {
         mApp.services().maybePlayStockSound(AudioManager.FX_KEYPRESS_SPACEBAR, false);
         final ItemModel item = mApp.model().getItemForMutation(pageKind, itemIndex);
         item.setColor(item.getColor().nextCyclicColor());
         mApp.view().updateSingleItemView(pageKind, itemIndex);
     }
 
     /** Called by the view when user clicks on item's arrow/lock area */
     public final void onItemArrowClick(final PageKind pageKind, final int itemIndex) {
         // If item locked, show item menu, allowing to unlock it.
         if (mApp.model().getItemReadOnly(pageKind, itemIndex).isLocked()) {
             showItemMenu(pageKind, itemIndex);
             return;
         }
 
         // Here when item is not locked. Animate and move to other page and do the actual
         // move in the model at the end of the animation.
         mApp.services().maybePlayStockSound(AudioManager.FX_KEYPRESS_RETURN, false);
         mApp.view().startItemAnimation(pageKind, itemIndex,
                 AppView.ItemAnimationType.MOVING_ITEM_TO_OTHER_PAGE, 0, new Runnable() {
                     @Override
                     public void run() {
                         moveItemToOtherPage(pageKind, itemIndex);
                     }
                 });
     }
 
     /**
      * Move a model item to the other page.
      * 
      * @param pageKind
      *            the source page.
      * @param itemIndex
      *            item index in the source page.
      */
     private final void moveItemToOtherPage(PageKind pageKind, int itemIndex) {
         // Remove item from current page.
         final ItemModel item = mApp.model().removeItem(pageKind, itemIndex);
         // Insert at the beginning of the other page.
         final PageKind otherPageKind = pageKind.otherPageKind();
         mApp.model().insertItem(otherPageKind, 0, item);
 
         mApp.model().clearAllUndo();
         maybeAutoSortPage(otherPageKind, false, false);
         mApp.view().updateAllPages();
 
         // The item is inserted at the top of the other page. Scroll there so it is visible
         // if the user flips to the other page.
         // NOTE(tal): This must be done after updateAlLPages(), otherwise it is ignored.
         mApp.view().scrollToTop(otherPageKind);
     }
 
     /** Called by the view when the user drag an item within the page */
     public final void onItemMoveInPage(final PageKind pageKind, final int sourceItemIndex,
             final int destinationItemIndex) {
         // LogUtil.debug("OS version: %s", android.os.Build.VERSION.SDK_INT);
 
         final ItemModel itemModel = mApp.model().removeItem(pageKind, sourceItemIndex);
         // NOTE(tal): if source index < destination index, the item removal above affect the index
         // of the destination by 1. Despite that, we don't compensate for it as this acieve a more
         // intuitive behavior and allow to move an item to the end of the list.
         mApp.model().insertItem(pageKind, destinationItemIndex, itemModel);
         mApp.view().upadatePage(pageKind);
         mApp.view().getRootView().post(new Runnable() {
             @Override
             public void run() {
                 maybeAutosortPageWithItemOfInterest(pageKind, destinationItemIndex);
             }
         });
 
     }
 
     /** Called when the activity is paused */
     public final void onMainActivityPause() {
         // Close any leftover dialogs. This provides a more intuitive user experience.
         mApp.popupsTracker().closeAllLeftOvers();
         
         // If state is dirty save it, so we don't lose it if the app is note resumed.
         if (mApp.model().isDirty()) {
             PersistenceMetadata metadata = new PersistenceMetadata(mApp.services()
                     .getAppVersionCode(), mApp.services().getAppVersionName());
             // NOTE(tal): this clears the dirty bit.
             ModelPersistence.saveData(mApp, mApp.model(), metadata);
             check(!mApp.model().isDirty());
             onBackupDataChange();
         }
 
         // In any case, update the widgets. This will solve the issue of app data restored
         // (e.g. by Titanium Backup) and widgets not updated until the model is actually
         // mutated.
         updateWidgets();
     }
 
     /** Called when the main activity is resumed, including after app creation. */
     public final void onMainActivityResume(ResumeAction resumeAction) {
         maybeHandleDateChange();
         clearAllUndo();
 
         ++mOnAppResumeCount;
 
         // Typically we reset the view to default position (both pages are scrolled
         // to the top, Today page is shown) when the app is resume. We preserve it only
         // when it is resumed from a sub activity (e.g. settings or startup message)
         // with no resume action.
         final boolean preserveView = mInSubActivity && resumeAction.isNone();
 
         if (!preserveView) {
             // Force both pages to be scrolled to top. More intuitive this way.
             mApp.view().scrollToTop(PageKind.TOMOROW);
             mApp.view().scrollToTop(PageKind.TODAY);
 
             // Force showing today's page. This is done with animation or immedietly
             // depending on settings. In case of an actual resume action, we skip
             // the animation to save user's time.
             final boolean isAppStartup = (mOnAppResumeCount == 1);
             if (isAppStartup && mApp.pref().getStartupAnimationPreference()
                     && resumeAction.isNone()) {
                 // Show initial animation
                 mApp.view().setCurrentPage(PageKind.TOMOROW, -1);
                 mApp.view().getRootView().postDelayed(new Runnable() {
                     @Override
                     public void run() {
                         mApp.view().setCurrentPage(PageKind.TODAY, 800);
                     }
                 }, 500);
             } else {
                 // No animation. Jump directly to Today.
                 mApp.view().setCurrentPage(PageKind.TODAY, 0);
             }
         }
 
         // Reset the in sub activity tracking .
         mInSubActivity = false;
 
         maybeAutoSortPages(true, true);
 
         // Dispatch optional resume action by simulating user clicks. By the logic
         // above, if resumeAction is not NONE, here the TODAY page is already displayed and
         // is scrolled all the way to the top.
         switch (resumeAction) {
             case ADD_NEW_ITEM_BY_TEXT:
                 onAddItemByTextButton(PageKind.TODAY);
                 break;
             case ADD_NEW_ITEM_BY_VOICE:
                 onAddItemByVoiceButton(PageKind.TODAY);
                 break;
             default:
                 // Do nothing
         }
     }
 
     /** Update date and if needed push model items from Tomorow to Today. */
     private void maybeHandleDateChange() {
         // Sample and cache the current date.
         mApp.dateTracker().updateDate();
 
         // TODO: filter out redundant view date changes? (do not update unless date changed)
         mApp.view().onDateChange();
 
         // A quick check for the normal case where the last push was today.
         final String modelPushDateStamp = mApp.model().getLastPushDateStamp();
         final String trackerTodayDateStamp = mApp.dateTracker().getDateStampString();
         if (trackerTodayDateStamp.equals(modelPushDateStamp)) {
             return;
         }
 
         // Determine if to expire all locks
         final PushScope pushScope = mApp.dateTracker().computePushScope(modelPushDateStamp,
                 mApp.pref().getLockExpirationPeriodPrefernece());
 
         if (pushScope == PushScope.NONE) {
             // Not exepected because of the quick check above
             LogUtil.error("*** Unexpected condition, pushScope=NONE,"
                     + " modelTimestamp=%s, trackerDateStamp=%s", modelPushDateStamp,
                     trackerTodayDateStamp);
         } else {
             final boolean expireAllLocks = (pushScope == PushScope.ALL);
             final boolean deleteCompletedItems = mApp.pref().getAutoDailyCleanupPreference();
             LogUtil.info("Model push scope: %s, auto_cleanup=%s", pushScope, deleteCompletedItems);
             mApp.model().pushToToday(expireAllLocks, deleteCompletedItems);
             // Not bothering to test if anything changed. Always updating. This happens only once a day.
             mApp.view().updateAllPages();
         }
 
         // NOTE(tal): we update the model push to today date even if we did not push. This will
         // eliminate the need to parse the model date stamp for the rest of the day.
         //
         // TODO: pick a better name for model last push date member.
         mApp.model().setLastPushDateStamp(mApp.dateTracker().getDateStampString());
     }
 
     /** Show the popup menu for a given item. Item is assumed to be already visible. */
     private final void showItemMenu(PageKind pageKind, int itemIndex) {
         final ItemModelReadOnly item = mApp.model().getItemReadOnly(pageKind, itemIndex);
 
         // Done vs ToDo based on item isCompleted status.
         final QuickActionItem doneAction = item.isCompleted() ? mQuickActionCache.getToDoAction()
                 : mQuickActionCache.getDoneAction();
 
         // Edit.
         final QuickActionItem editAction = mQuickActionCache.getEditAction();
 
         // Lock vs Unlock based on item isLocked status.
         final QuickActionItem lockAction = item.isLocked() ? mQuickActionCache.getUnlockAction()
                 : mQuickActionCache.getLockAction();
 
         // Delete.
         final QuickActionItem deleteAction = mQuickActionCache.getDeleteAction();
 
         // TODO: could cache only this array since there are only 4 permutations.
         final QuickActionItem actions[] = { doneAction, editAction, lockAction, deleteAction };
 
         mApp.view().setItemViewHighlight(pageKind, itemIndex, true);
         mApp.view().showItemMenu(pageKind, itemIndex, actions,
                 QuickActionsCache.DISMISS_WITH_NO_SELECTION_ID);
     }
 
     /** Called when the user made a selection from an item popup menu. */
     public void onItemMenuSelection(final PageKind pageKind, final int itemIndex, int actionId) {
         // In case of dismissal with no selection we don't clear the undo buffer.
         if (actionId != QuickActionsCache.DISMISS_WITH_NO_SELECTION_ID) {
             clearPageUndo(pageKind);
         }
 
        // Clear the item highlited enabled when the menu was shown.
         mApp.view().setItemViewHighlight(pageKind, itemIndex, false);
 
         // Handle the action
         switch (actionId) {
             case QuickActionsCache.DISMISS_WITH_NO_SELECTION_ID: {
                 return;
             }
 
             case QuickActionsCache.DONE_ACTION_ID: {
                 mApp.services().maybePlayApplauseSoundClip(AudioManager.FX_KEY_CLICK, false);
                 final ItemModel item = mApp.model().getItemForMutation(pageKind, itemIndex);
                 item.setIsCompleted(true);
                 // NOTE(tal): we assume that the color flag is not needed once an item is completed.
                 // This is a usability heuristic. Not required otherwise.
                 item.setColor(ItemColor.NONE);
                 mApp.view().updateSingleItemView(pageKind, itemIndex);
                 maybeAutosortPageWithItemOfInterest(pageKind, itemIndex);
                 return;
             }
 
             case QuickActionsCache.TODO_ACTION_ID: {
                 mApp.services().maybePlayStockSound(AudioManager.FX_KEY_CLICK, false);
                 final ItemModel item = mApp.model().getItemForMutation(pageKind, itemIndex);
                 item.setIsCompleted(false);
                 mApp.view().updateSingleItemView(pageKind, itemIndex);
                 maybeAutosortPageWithItemOfInterest(pageKind, itemIndex);
                 return;
             }
 
             // Edit
             case QuickActionsCache.EDIT_ACTION_ID: {
                 mApp.services().maybePlayStockSound(AudioManager.FX_KEY_CLICK, false);
                 final ItemModel item = mApp.model().getItemForMutation(pageKind, itemIndex);
                 ItemEditor.startEditor(mApp, "Edit Task", item.getText(),
                         new ItemEditor.ItemEditorListener() {
                             @Override
                             public void onTextChange(String newText) {
                                 // This updates the model on each key, as the user types.
                                 // Closing the editor will still leave the new content
                                 // stored in the model.
                                 item.setText(newText);
                                 mApp.model().setDirty();
                             }
 
                             @Override
                             public void onDismiss(String finalString) {
                                 item.setText(finalString);
                                 mApp.model().setDirty();
                                 mApp.view().updateSingleItemView(pageKind, itemIndex);
                                 // Highlight the modified item for a short time, to provide
                                 // the user with
                                 // an indication of the modified item.
                                 briefItemHighlight(pageKind, itemIndex);
                             }
                         });
                 return;
             }
 
             case QuickActionsCache.LOCK_ACTION_ID:
             case QuickActionsCache.UNLOCK_ACTION_ID: {
                 mApp.services().maybePlayStockSound(AudioManager.FX_KEYPRESS_STANDARD, false);
 
                 final ItemModel item = mApp.model().getItemForMutation(pageKind, itemIndex);
                 item.setIsLocked(actionId == QuickActionsCache.LOCK_ACTION_ID);
                 mApp.view().updateSingleItemView(pageKind, itemIndex);
                 // If lock and in Today page, we also move it to the Tomorrow page, with an
                 // animation.
                 if (pageKind == PageKind.TODAY && actionId == QuickActionsCache.LOCK_ACTION_ID) {
                     // mApp.services().vibrateShort();
 
                     // We do a short delay before the animation to let the use see the icon change
                     // to lock before the item is moved to the other page.
                     // TODO: make the pre delay a resource or const
                     mApp.view().startItemAnimation(pageKind, itemIndex,
                             AppView.ItemAnimationType.MOVING_ITEM_TO_OTHER_PAGE, 200,
                             new Runnable() {
                                 @Override
                                 public void run() {
                                     moveItemToOtherPage(pageKind, itemIndex);
                                 }
                             });
                 } else {
                     maybeAutosortPageWithItemOfInterest(pageKind, itemIndex);
                 }
                 return;
             }
 
             case QuickActionsCache.DELETE_ACTION_ID: {
                 mApp.services().maybePlayStockSound(AudioManager.FX_KEYPRESS_DELETE, false);
                 mApp.view().startItemAnimation(pageKind, itemIndex,
                         AppView.ItemAnimationType.DELETING_ITEM, 0, new Runnable() {
                             @Override
                             public void run() {
                                 // This runs at the end of the animation.
                                 mApp.model().removeItemWithUndo(pageKind, itemIndex);
                                 mApp.view().upadatePage(pageKind);
                             }
                         });
                 return;
             }
         }
 
         throw new RuntimeException("Unknown menu action: " + actionId);
     }
 
     /** Highlight the given item for a brief time. The item is assumed to already be visible. */
     private final void briefItemHighlight(final PageKind pageKind, final int itemIndex) {
         mApp.view().setItemViewHighlight(pageKind, itemIndex, true);
         mApp.view().getRootView().postDelayed(new Runnable() {
             @Override
             public void run() {
                 mApp.view().setItemViewHighlight(pageKind, itemIndex, false);
             }
         }, 700);
     }
 
     /** Called by the app view when the user clicks on the Settings button. */
     public final void onIcsMenuOverflowButtonClick(PageKind pageKind) {
         mApp.mainActivity().openOptionsMenu();
     }
 
     /** Called by the app view when the user clicks on the Undo button. */
     public final void onUndoButton(PageKind pageKind) {
         mApp.services().maybePlayStockSound(AudioManager.FX_KEYPRESS_RETURN, false);
         final int itemRestored = mApp.model().applyUndo(pageKind);
         maybeAutoSortPage(pageKind, false, false);
         mApp.view().upadatePage(pageKind);
         mApp.services().toast("Restored %d deleted %s", itemRestored, taskOrTasks(itemRestored));
     }
 
     /** Called by the app view when the user clicks on the Add Item button. */
     public final void onAddItemByTextButton(final PageKind pageKind) {
         clearPageUndo(pageKind);
         mApp.services().maybePlayStockSound(AudioManager.FX_KEY_CLICK, false);
         ItemEditor.startEditor(mApp, "New Task", "", new ItemEditor.ItemEditorListener() {
             @Override
             public void onTextChange(String newText) {
             }
 
             @Override
             public void onDismiss(String finalString) {
                 maybeAddNewItem(finalString, pageKind, false);
             }
         });
     }
 
     public final void onAddItemByVoiceButton(final PageKind pageKind) {
         mApp.services().maybePlayStockSound(AudioManager.FX_KEY_CLICK, false);
 
         Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
         intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                 RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
         intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Dictate a new task");
         mInSubActivity = true;
         mApp.mainActivity().startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
     }
 
     private final void maybeAddNewItem(final String text, final PageKind pageKind,
             boolean upperCaseIt) {
         String cleanedValue = text.trim();
         if (cleanedValue.length() == 0) {
             return;
         }
 
         // TODO: this creates to many objects.
         if (upperCaseIt) {
             cleanedValue = cleanedValue.substring(0, 1).toUpperCase() + cleanedValue.substring(1);
         }
         
         // // TODO: share with add by voice below
         ItemModel item = new ItemModel(cleanedValue, false, false, ItemColor.NONE);
         // final
         mApp.model().insertItem(pageKind, 0, item);
         mApp.view().upadatePage(pageKind);
         mApp.view().scrollToTop(pageKind);
 
         // We perform the highlight only after the view has been
         // stabilized from the scroll (since item views are reused during the scroll).
         mApp.view().getRootView().post(new Runnable() {
             @Override
             public void run() {
                 briefItemHighlight(pageKind, 0);
             }
         });
     }
 
     public void onActivityResult(int requestCode, int resultCode, Intent intent) {
         switch (requestCode) {
             case VOICE_RECOGNITION_REQUEST_CODE: {
                 onVoiceActivityResult(requestCode, resultCode, intent);
                 break;
             }
             default:
                 LogUtil.warning("Unknown onActivityResult requestCode: %s", requestCode);
         }
     }
 
     private final void onVoiceActivityResult(int requestCode, int resultCode, Intent intent) {
         // Prevets the main activity to scroll to top of pages as we do when resuming from
         // an external activity.
         mInSubActivity = true;
 
         if (resultCode != Activity.RESULT_OK) {
             return;
         }
 
         ArrayList<String> matches = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
 
         final Dialog dialog = new Dialog(mApp.context());
         dialog.setContentView(R.layout.voice_list_dialog_layout);
         dialog.setTitle("Select best match");
 
         ListView listView = (ListView) dialog.findViewById(R.id.voice_selection_list);
         final ArrayAdapter<String> adapter = new ArrayAdapter<String>(mApp.context(),
                 android.R.layout.simple_list_item_1, matches);
 
         listView.setAdapter(adapter);
 
         listView.setOnItemClickListener(new OnItemClickListener() {
             @Override
             public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                 dialog.dismiss();
                 final TextView itemTextView = (TextView) arg1;
                 maybeAddNewItem(itemTextView.getText().toString(), mApp.view().getCurrentPage(),
                         true);
             }
         });
 
         dialog.show();
     }
 
     /** Called by the app view when the user click or long press the clean page button. */
     public final void onCleanPageButton(final PageKind pageKind, boolean isLongPress) {
         final boolean deleteCompletedItems = isLongPress;
         // TODO: could cache and reuse this object to reduce allocation.
         final OrganizePageSummary summary = new OrganizePageSummary();
 
         mApp.model().organizePageWithUndo(pageKind, deleteCompletedItems, -1, summary);
 
         mApp.services().maybePlayStockSound(
                 (summary.completedItemsDeleted > 0) ? AudioManager.FX_KEYPRESS_DELETE
                         : AudioManager.FX_KEY_CLICK, false);
 
         mApp.view().upadatePage(pageKind);
 
         // Display optional message to the user
         @Nullable
         final String message = constructPageCleanMessage(summary);
         if (message != null) {
             mApp.services().toast(message);
         }
     }
 
     /**
      * Compose the message to show to the user after a page cleanup operation.
      * 
      * @param summary
      *            the page cleanup summary.
      * @return the message or null if no message should me shown.
      */
     @Nullable
     private final String constructPageCleanMessage(OrganizePageSummary summary) {
         final boolean isMinimal = !mApp.pref().getVerboseMessagesEnabledPreference();
 
         if (summary.completedItemsDeleted > 0) {
             return isMinimal ? null : String.format("Deleted %d completed %s",
                     summary.completedItemsDeleted, taskOrTasks(summary.completedItemsDeleted));
         }
 
         // Here when not deleted
         if (summary.orderChanged) {
             // Here when reorderd
             if (isMinimal) {
                 return null;
             }
             return (summary.completedItemsFound == 0) ? "Tasks reordered" : String.format(
                     "Tasks reordered. Long press to delete %d completed %s",
                     summary.completedItemsFound, taskOrTasks(summary.completedItemsFound));
         }
 
         // Here when was already ordered.
         if (summary.completedItemsFound > 0) {
             // Here if found completed items.
             return isMinimal ? null : String.format(
                     "Page already organized. Long press to delete %d completed %s",
                     summary.completedItemsFound, taskOrTasks(summary.completedItemsFound));
         }
 
         return isMinimal ? null : "Page already organized";
     }
 
     /** Called by the framework when the user makes a main menu selection. */
     public final void onMainMenuSelection(MainMenuEntry entry) {
         switch (entry) {
             case HELP: {
                 startPopupMessageSubActivity(MessageKind.HELP);
                 break;
             }
             case SETTINGS: {
                 startSubActivity(PreferencesActivity.class);
                 break;
             }
             case ABOUT: {
                 startPopupMessageSubActivity(MessageKind.ABOUT);
                 // startSubActivity(AboutActivity.class);
                 break;
             }
             default:
                 throw new RuntimeException("Unknown main menu action id: " + entry);
         }
     }
 
     /** Handle back button event or return false if not used. */
     public final boolean onBackButton() {
         // If the current page is not today, we still the back key event and switch back to the
         // today page. Otherwise we use the default back behavior.
         final PageKind currentPage = mApp.view().getCurrentPage();
         if (currentPage != PageKind.TODAY) {
             mApp.view().setCurrentPage(PageKind.TODAY, -1);
             return true;
         }
         return false;
     }
 
     /**
      * Called by the app preferences client when app preferences changed.
      * 
      * @param id
      *            the id of the preference item that was changed.
      */
     public final void onPreferenceChange(PreferenceKind id) {
         onBackupDataChange();
 
         switch (id) {
             case ITEM_FONT_TYPE:
             case ITEM_FONT_SIZE:
             case ITEM_ACTIVE_TEXT_COLOR:
             case ITEM_COMPLETED_TEXT_COLOR:
                 mApp.resources().onItemFontVariationPreferenceChange();
                 mApp.view().onItemFontVariationPreferenceChange();
                 break;
 
             case PAGE_BACKGROUND_TYPE:
             case PAGE_BACKGROUND_SOLID_COLOR:
                 mApp.view().onPageBackgroundPreferenceChange();
                 break;
 
             case PAGE_ITEM_DIVIDER_COLOR:
                 mApp.view().onItemDividerColorPreferenceChange();
                 break;
 
             case AUTO_SORT:
                 maybeAutoSortPages(true, true);
                 break;
 
             case SOUND_ENABLED:
             case APPLAUSE_LEVEL:
             case LOCK_PERIOD:
             case VERBOSE_MESSAGES:
             case STARTUP_ANIMATION:
             case AUTO_DAILY_CLEANUP:
                 // Nothing to do here. We query these preferences on the fly.
                 break;
 
             case WIDGET_BACKGROUND_COLOR:
             case WIDGET_TEXT_COLOR:
             case WIDGET_SINGLE_LINE:
             case WIDGET_SHOW_TOOLBAR:
                 // NOTE: This covers the case where the user changes widget settings and presses the
                 // Home button immediately, going back to the widgets. The widget update at
                 // onAppPause() is not triggered in this case because the main activity is already
                 // paused.
                 updateWidgets();
                 break;
 
             default:
                 throw new RuntimeException("Unknown preference: " + id);
         }
     }
 
     /** Inform backup manager about change in persisted model data of app settings */
     private final void onBackupDataChange() {
         LogUtil.info("Backup data changed");
         mApp.services().backupManager().dataChanged();
     }
 
     /** Force a widget update with the current */
     private final void updateWidgets() {
         BaseWidgetProvider.updateAllWidgetsFromModel(mApp.context(), mApp.model());
 
     }
 
     /** Called by the main activity when it is created. */
     public final void onMainActivityCreated(StartupKind startupKind) {
         maybeHandleDateChange();
 
         switch (startupKind) {
             case NORMAL:
             case NEW_VERSION_SILENT:
                 // Model is assume to be clean here.
                 break;
             case NEW_USER:
                 // Mark as dirty to persist the sample data so we don't get this message again.
                 mApp.model().setDirty();
                 startPopupMessageSubActivity(MessageKind.NEW_USER);
                 break;
             case NEW_VERSION_ANNOUNCE:
                 // Mark the model for writing to avoid this what's up splash the next time.
                 mApp.model().setDirty();
                 startPopupMessageSubActivity(MessageKind.WHATS_NEW);
                 break;
             case SAMPLE_DATA_ERROR:
             case MODEL_DATA_ERROR:
                 mApp.model().clear();
                 mApp.services().toast("Error loading data (code %s)", startupKind);
             default:
                 LogUtil.error("Unknown startup message type: ", startupKind);
         }
     }
 
     private final void startPopupMessageSubActivity(MessageKind messageKind) {
         startSubActivity(PopupMessageActivity.intentFor(mApp.context(), messageKind));
     }
 
     private final void startSubActivity(Class<? extends Activity> cls) {
         // TODO: should we assert or print an error message is mInSubActivity is already true?
         final Intent intent = new Intent(mApp.context(), cls);
         startSubActivity(intent);
     }
 
     private final void startSubActivity(Intent intent) {
         // TODO: should we assert or print an error messge is mInSubActivity is already true?
         mInSubActivity = true;
         mApp.context().startActivity(intent);
     }
 
     /** Called by the main activity when it is destroyed. */
     public final void onMainActivityDestroy() {
     }
 
     /** Clear undo buffer of both model pages. */
     private final void clearAllUndo() {
         mApp.model().clearAllUndo();
         mApp.view().updateAllUndoButtons();
     }
 
     /** Clear undo buffer of given model page. */
     private final void clearPageUndo(PageKind pageKind) {
         mApp.model().clearPageUndo(pageKind);
         mApp.view().updateUndoButton(pageKind);
     }
 
     /** Return the correct 'n tasks' string. Result is user visible. */
     private static final String taskOrTasks(int n) {
         return (n == 1) ? "task" : "tasks";
     }
 
     private final boolean maybeAutoSortPage(PageKind pageKind, boolean updateViewIfSorted,
             boolean showMessageIfSorted) {
         if (mApp.pref().getAutoSortPreference()) {
             // TODO: use a temp member to avoid an extra object allocation;
             OrganizePageSummary summary = new OrganizePageSummary();
             mApp.model().organizePageWithUndo(pageKind, false, -1, summary);
             if (summary.orderChanged) {
                 if (updateViewIfSorted) {
                     mApp.view().upadatePage(pageKind);
                 }
                 if (showMessageIfSorted && mApp.pref().getVerboseMessagesEnabledPreference()) {
                     mApp.services().toast("Auto sorted");
                 }
                 return true;
             }
         }
         return false;
     }
 
     private final boolean maybeAutoSortPages(boolean updateViewIfSorted, boolean showMessageIfSorted) {
         // NOTE: avoiding '||' operator short circuit to make sure both pages are sorted.
         final boolean sorted1 = maybeAutoSortPage(PageKind.TODAY, updateViewIfSorted, false);
         final boolean sorted2 = maybeAutoSortPage(PageKind.TOMOROW, updateViewIfSorted, false);
         final boolean sorted = sorted1 || sorted2;
         // NOTE: suppressing message if showing a sub activity (e.g. SettingActivity).
         if (sorted && showMessageIfSorted && mApp.pref().getVerboseMessagesEnabledPreference()
                 && !mInSubActivity) {
             mApp.services().toast("Tasks sorted");
         }
         return sorted;
     }
 
     /**
      * @param pageKind
      *            the page
      * @param itemOfInteresttOriginalIndex
      *            if >= 0, the pre sort index of the item to highlight post sort.
      */
     private final void maybeAutosortPageWithItemOfInterest(final PageKind pageKind,
             final int itemOfInteresttOriginalIndex) {
         if (!mApp.pref().getAutoSortPreference() || mApp.model().isPageSorted(pageKind)) {
             return;
         }
 
         mApp.view().startItemAnimation(pageKind, itemOfInteresttOriginalIndex,
                 ItemAnimationType.SORTING_ITEM, 0, new Runnable() {
                     @Override
                     public void run() {
                         // This is done at the end of the animation
                         final OrganizePageSummary summary = new OrganizePageSummary();
                         mApp.model().organizePageWithUndo(pageKind, false,
                                 itemOfInteresttOriginalIndex, summary);
                         mApp.view().upadatePage(pageKind);
                         if (mApp.pref().getVerboseMessagesEnabledPreference()) {
                             mApp.services().toast("Auto sorted");
                         }
                         if (summary.itemOfInterestNewIndex >= 0) {
                             // After the animation, briefly highlight the item at the new
                             // location.
                             mApp.view().getRootView().post(new Runnable() {
                                 @Override
                                 public void run() {
                                     briefItemHighlight(pageKind, summary.itemOfInterestNewIndex);
                                 }
                             });
 
                         }
                     }
                 });
     }
 }
