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
 
 package org.dodgybits.android.shuffle.provider;
 
 import android.net.Uri;
 import android.provider.BaseColumns;
 
 public class Shuffle {
 
 	public static final String PACKAGE = "org.dodgybits.android.shuffle.provider.Shuffle";
 	/**
      * Tasks table
      */
     public static final class Tasks implements BaseColumns {
         /**
          * The content:// style URL for this table
          */
         public static final Uri CONTENT_URI = Uri.parse("content://" + PACKAGE + "/tasks");
         public static final Uri cTopTasksContentURI = Uri.parse("content://" + PACKAGE + "/topTasks");
         public static final Uri cInboxTasksContentURI = Uri.parse("content://" + PACKAGE + "/inboxTasks");
         public static final Uri cDueTasksContentURI = Uri.parse("content://" + PACKAGE + "/dueTasks");
         public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.dodgybits.task";
         public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.dodgybits.task";
         
         // used by due tasks to determine query type
         public static final int DAY_MODE = 0;
     	public static final int WEEK_MODE = 1;
     	public static final int MONTH_MODE = 2;
         
         /**
          * The default sort order for this table
          */
        public static final String DEFAULT_SORT_ORDER = "due ASC, created ASC";
         
         public static final String DESCRIPTION = "description";
         public static final String DETAILS = "details";
         public static final String CONTEXT_ID = "contextId";
         public static final String PROJECT_ID = "projectId";
         public static final String CREATED_DATE = "created";
         public static final String MODIFIED_DATE = "modified";
         public static final String START_DATE = "start";
         public static final String DUE_DATE = "due";
         public static final String TIMEZONE = "timezone";
         public static final String CAL_EVENT_ID = "calEventId";
         public static final String DISPLAY_ORDER = "displayOrder";
         public static final String COMPLETE = "complete";
         public static final String ALL_DAY = "allDay";
         public static final String HAS_ALARM = "hasAlarm"; 
         
         public static final String PROJECT_NAME = "project_name";
         public static final String PROJECT_DEFAULT_CONTEXT_ID = "project_default_context_id";
         public static final String PROJECT_ARCHIVED = "project_archived";
         
         public static final String CONTEXT_NAME = "context_name";
         public static final String CONTEXT_COLOUR = "context_colour";
         public static final String CONTEXT_ICON = "context_icon";
         
         /**
          * Projection for all the columns of a task.
          */
         public static final String[] cFullProjection = new String[] {
                 _ID,
                 DESCRIPTION,
                 DETAILS, 
                 PROJECT_ID,
                 CONTEXT_ID,
                 CREATED_DATE, 
                 MODIFIED_DATE,
                 START_DATE,
                 DUE_DATE,
                 TIMEZONE,
                 CAL_EVENT_ID,
                 DISPLAY_ORDER,
                 COMPLETE,
                 ALL_DAY,
                 HAS_ALARM,
         };
                 
 
         /**
          * All columns task plus columns from project and context from outer joins
          */
         public static final String[] cExpandedProjection = new String[] {
             _ID,
             DESCRIPTION,
             DETAILS, 
             PROJECT_ID,
             CONTEXT_ID,
             CREATED_DATE, 
             MODIFIED_DATE, 
             START_DATE,
             DUE_DATE,
             TIMEZONE,
             CAL_EVENT_ID,
             DISPLAY_ORDER,
             COMPLETE,
             ALL_DAY,
             HAS_ALARM,
             
             PROJECT_NAME,
             PROJECT_DEFAULT_CONTEXT_ID,
             PROJECT_ARCHIVED,
             
             CONTEXT_NAME,
             CONTEXT_COLOUR,
             CONTEXT_ICON,
         };
         
     }
     
 	/**
      * Projects table
      */
     public static final class Projects implements BaseColumns {
         /**
          * The content:// style URL for this table
          */
         public static final Uri CONTENT_URI = Uri.parse("content://" + PACKAGE + "/projects");
         public static final Uri cProjectTasksContentURI = Uri.parse("content://" + PACKAGE + "/projectTasks");
         public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.dodgybits.project";
         public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.dodgybits.project";
         
         
         /**
          * The default sort order for this table
          */
         public static final String DEFAULT_SORT_ORDER = "name DESC";
         
         public static final String NAME = "name";
         public static final String DEFAULT_CONTEXT_ID = "defaultContextId";
         public static final String ARCHIVED = "archived";
 
         /**
          * Projection for all the columns of a project.
          */
         public static final String[] cFullProjection = new String[] {
                 _ID,
                 NAME,
                 DEFAULT_CONTEXT_ID, 
                 ARCHIVED, 
         };
 
         public static final String TASK_COUNT = "count";
         
         /**
          * Projection for fetching the task count for each project.
          */
         public static final String[] cFullTaskProjection = new String[] {
             _ID,
             TASK_COUNT,
         };
     }
 
 	/**
      * Contexts table
      */
     public static final class Contexts implements BaseColumns {
         /**
          * The content:// style URL for this table
          */
         public static final Uri CONTENT_URI = Uri.parse("content://" + PACKAGE + "/contexts");
         public static final Uri cContextTasksContentURI = Uri.parse("content://" + PACKAGE + "/contextTasks");
         public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.dodgybits.context";
         public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.dodgybits.context";
 
         /**
          * The default sort order for this table
          */
         public static final String DEFAULT_SORT_ORDER = "name DESC";
         
         public static final String NAME = "name";
         public static final String COLOUR = "colour";
         public static final String ICON = "iconName";
         
         /**
          * Projection for all the columns of a context.
          */
         public static final String[] cFullProjection = new String[] {
                 _ID,
                 NAME,
                 COLOUR, 
                 ICON, 
         };
         
         public static final String TASK_COUNT = "count";
 
         /**
          * Projection for fetching the task count for each context.
          */
         public static final String[] cFullTaskProjection = new String[] {
             _ID,
             TASK_COUNT,
         };
 
 
     }
     
 	/**
      * Reminders table
      */
     public static final class Reminders implements BaseColumns {
         /**
          * The content:// style URL for this table
          */
         public static final Uri CONTENT_URI = Uri.parse("content://" + PACKAGE + "/reminders");
         public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.dodgybits.reminder";
         public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.dodgybits.reminder";
 
         /**
          * The default sort order for this table
          */
         public static final String DEFAULT_SORT_ORDER = "minutes DESC";
         
         /**
          * The task the reminder belongs to
          * <P>Type: INTEGER (foreign key to the task table)</P>
          */
         public static final String TASK_ID = "taskId";
         
         /**
          * The minutes prior to the event that the alarm should ring.  -1
          * specifies that we should use the default value for the system.
          * <P>Type: INTEGER</P>
          */
         public static final String MINUTES = "minutes";
         
         public static final int MINUTES_DEFAULT = -1;
         
         /**
          * The alarm method.
          */
         public static final String METHOD = "method";
 
         public static final int METHOD_DEFAULT = 0;
         public static final int METHOD_ALERT = 1;
         
         
         /**
          * Projection for all the columns of a context.
          */
         public static final String[] cFullProjection = new String[] {
                 _ID,
                 MINUTES, 
                 METHOD, 
         };
         
         public static final int MINUTES_INDEX = 1;
         public static final int METHOD_INDEX = 2;
     }
     
     
 }
 
