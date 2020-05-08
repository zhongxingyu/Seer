 package com.teamdev.projects.test.web.component.detailspanel.task;
 
 import org.openqa.selenium.By;
 
 /**
 * @author Sergii Moroz
  */
 public interface Locators {
 
     //Details Panel for Task Locators
     By DETAILS_PANEL = By.className("selected-task-details");
     By INFO_TAB_BODY = By.className("overview") ;
     By COMMENTS_TAB_BODY =  By.className("viewport");
 
     //Buttons
     By INFO = By.className("info");
 
     //General locators
     By BLOCK_TITLE = By.className("upper_case_block_title");
    By AUTO_COMPLETE_LIST = By.className("ac-renderer");
 
     //Details panel title (task name)
    By TITLE_CONTAINER = By.className("title_container");
     By INPUT_BOX = By.className("input-box-input");
    By INPUT_BOX_PLACEHOLDER = By.className("input-box-placeholder");
 
     //Link to task
     By LINK_TO_TASK = By.className("link-to-task");
 
     //Tabs
     By COMMENTS_EMPTY_TAB = By.className("comments-empty");
     By COMMENTS_TAB = By.className("comments");
     By LOG_TAB = By.className("activity");
 
     //Info tab locators
     //Notes
     By NOTES_CONTAINER = By.className("notes_container");
 
 
     //Attachments
     By ATTACH_CONTAINER = By.className("attachments");
     By ATTACH_LINK = By.className("add_attachment");
     By ATTACH_LIST = By.className("attachment_list_wrapper");
 
     //Tags
     By TAGS_CONTAINER = By.className("tags_block");
     By TAGS = By.className("content");
 
     //Owner and Assignee
     By OWNER_ASSIGNEE_CONTAINER = By.className("owner_assignee");
     By OWNER_TITLE = By.className("owner_title");
     By ASIGNEE_TITLE = By.className("assignee_title");
     By OWNER = By.className("owner");
 
     //Due By
     By DUE_BY_TITLE = By.className("due_to_title");
    By DUE_BY_FIELD_CONTAINER = By.cssSelector(".due_date_container");
 
     //Estimate and Logged
     By ESTIMATE_AND_LOGGED_BLOCK = By.className("estimate_logged");
     By ESTIMATE_TITLE = By.className("estimate_title");
     By LOGGED_TITLE = By.className("logged_title");
    By LOGGED = By.className("logged");
 
     //Wathers
     By WATCHERS_BLOCK = By.className("watchers");
     By WATCH_UNWATCH = By.className("add_watcher");
     By WATCHERS_LIST_CONTAINER = By.className("watcher_list_container");
 
     //Log tab
     By LOG_TAB_BODY = By.className("activity-tab");
 //  By LOG_LIST_CONTAINER = By.className("content");
 //  By EMPTY_LIST_LABEL = By.className("empty_list_label none");
 
     //Log item parts
     By LOG_ITEM = By.className("task_activity_item");
     By ITEM_ICON = By.className("icon");
 //    By ITEM_ICON_CREATED = By.className("icon block inline created");
 //    By ITEM_ICON_PAUSE = By.className("icon block inline checked-out");
 //    By ITEM_ICON_STARTED = By.className("icon block inline checked-in");
     By ACTION_TEXT = By.className("action");
     By PROJECT_NAME_FOCUS = By.className("blueLink");
     By ACTION_TIME = By.className("when");
 
     //Comments tab
     By COMMENTS_BLOCK = By.className("overview");
     By EMPTY_COMMENTS_NOTE = By.className("empty_list_label");
     By COMMENTS_LIST = By.className("comment_list");
 
     //Textarea block
     By INPUT_CONTAINER = By.className("textarea_container");
 
     //Post Comment block
     By POST_BUTTON_CONTAINER = By.className("post-comment-button-container");
     By POST_COMMENT = By.className("btn");
     By BUTTON_LABEL = By.tagName("button");
 
     //Applicable only when at least one comment exists
     By COMMENT_CONTAINER = By.className("comment-item");
     By AVATAR = By.className("avatar_holder");
     By COMMENT = By.className("comment_text");
 
     //Comment Info section
     By AUTHOR = By.className("actor");
     By DELETE_COMMENT = By.className("delete_comment");
 }
