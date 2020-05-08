 /*
  * Copyright 2003 - 2013 Herb Bowie
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.powersurgepub.twodue.data;
 
  import com.powersurgepub.pstextio.*;
   import com.powersurgepub.psdatalib.elements.*;
   import com.powersurgepub.psdatalib.pstags.*;
   import com.powersurgepub.psdatalib.psdata.*;
   import com.powersurgepub.psutils.*;
   import com.powersurgepub.twodue.*;
   import com.powersurgepub.xos2.*;
   import java.awt.*;
   import java.awt.event.*;
   import java.net.*;
   import java.text.*;
   import java.util.*;
   import javax.swing.*;
 
 /**
    An object representing one item on a to do list. 
  */
 
 public class ToDoItem 
     implements
       Comparable,
       Taggable    {
   
   public  final static String OBJECT_NAME         = "ToDoItem";
   
   public  final static String NA_STRING           = "N/A";
   
   public  final static int  NUMBER_OF_COLUMNS     = 27;
   
   public  final static int  DELETED               = 0;
   public  final static int  TAGS                  = 1;
   public  final static int  DUE_DATE              = 2;
   public  final static int  LATE                  = 3;
   public  final static int  PRIORITY              = 4;
   public  final static int  STATUS                = 5;
   public  final static int  DONE                  = 6;
   public  final static int  ASSIGNED_TO           = 7;
   public  final static int  SEQUENCE              = 8;
   public  final static int  TITLE                 = 9;
   public  final static int  DESCRIPTION           = 10;
   public  final static int  OUTCOME               = 11;
   public  final static int  WEB_PAGE              = 12;
   public  final static int  START_TIME            = 13;
   public  final static int  DURATION              = 14;
   public  final static int  ALERT_PRIOR           = 15;
   public  final static int  RECURS_EVERY          = 16;
   public  final static int  RECURS_UNIT           = 17;
   public  final static int  RECURS_DAY_OF_WEEK    = 18;
   public  final static int  RECURS_WITHIN_MONTH   = 19;
   public  final static int  DUE_DATE_YMD          = 20;
   public  final static int  ID                    = 21;
   public  final static int  TYPE                  = 22;
   public  final static int  FILE_LENGTH           = 23;
   public  final static int  LAST_MOD_DATE         = 24;
   public  final static int  LAST_MOD_DATE_YMD     = 25;
   public  final static int  LINKED_TAGS           = 26;
   
   public  final static String[] COLUMN_NAME = {
     "deleted",
     "tags",
     "duedate",
     "late",
     "priority",
     "status",
     "done",
     "assignedto",
     "sequence",
     "title",
     "description",
     "outcome",
     "webpage",
     "starttime",
     "duration",
     "alertprior",
     "recursevery",
     "recursunit",
     "recursdayofweek",
     "recurswithinmonth",
     "duedateymd",
     "id",
     ItemType.getColumnName(),
     "length",
     "lastmod",
     "lastmoddateymd",
     "linkedtags"
   };
   
   /**
     Array of strings that can be used for column headings, and also as a list
     of field names that can be used with the TextBlock class. 
    */
   public  final static String[] COLUMN_DISPLAY = {
     "Deleted",
     "Tags",
     "Due Date",
     "Late",
     "Priority",
     "Status",
     "Done",
     "Assigned To",
     "Seq",
     "Title",
     "Description",
     "Outcome",
     "Web Page",
     "Start Time",
     "Duration",
     "Alert Prior",
     "Recurs Every",
     "Recurs Unit",
     "Day of Week",
     "Within Month",
     "Due Date YMD",
     "ID",
     ItemType.getColumnDisplayName(),
     "File Length",
     "Last Modified",
     "Last Mod YMD",
     "Linked Tags",
     "Tag1",
     "Tag2",
     "Tag3",
     "Tag4",
     "Tag5",
     "Tag6",
     "Tag7",
     "Tag8",
     "Tag9",
     "Tag10",
   };
   
   public  final static String  TAG = "Tag";
   public  final static int     TAGS_MAX          = 5;
   
   public  final static String[] COLUMN_BRIEF = {
     "Deleted",
     "Tags",
     "Due Date",
     " ",
     "Priority",
     "Status",
     "Done",
     "Assigned To",
     "Seq",
     "Title",
     "Description",
     "Outcome",
     "Web Page",
     "Start Time",
     "Duration",
     "Alert Prior",
     "Every",
     "Unit",
     "Day of Wk",
     "Within Month",
     "YMD",
     "ID",
     ItemType.getColumnBriefName(),
     "File Size",
     "Mod Date",
     "Mod YMD",
     "Linked Tags"
   };
   
   public  final static int[] COLUMN_WIDTH = {
     40,   // Deleted
    	120,  // Tags
     90,   // Due Date
     19,   // Late Flag
     70,   // Priority
     80,   // Status
     40,   // Done
     100,  // Assigned To
     40,   // Seq
     250,  // Title
     300,  // Description
     200,  // Outcome
     200,  // Web Page
     80,   // Start Time
     80,   // Duration
     80,   // Alert Prior
     70,   // Recurs Every
     70,   // Recurs Unit
     70,   // Day of Week
     70,   // Within Month
     90,   // Due Date in YMD format
     90,   // ID
     ItemType.getColumnWidth(),
     80,   // File Size
     90,   // Last Modification Date
     90,   // Last Mod Date in YMD format
     200
   };
   
   public  final static int[] COLUMN_CLASS_TYPE = {
     1, // Deleted: Check Box
    	0,
     0, // Due Date
     2, // Icon
     3, // ItemPriority
     0,
     1, // Check Box
     0,
     0,
     0,
     0,
     0,
     0,
     0,
     0,
     0,
     0,
     0,
     0,
     0,
     0,
     4,  // ID Number
     ItemType.getColumnClassType(),
     0,
     0,
     0,
     0
   };
   
   public  final static int[] COLUMN_DISPLAY_PRIORITY = {
     // (lower numbers are higher priorities for display)
     900,
    	900,
      30,
      40,
      60,
      20,
      10,
     900,
      50,
      80,
     900,
     900,
     900,
      70,
     900,
     900,
     900,
     900,
     900,
     900,
     900,
     25,
     ItemType.getColumnDisplayPriority(),
     100,
     110,
     900,
     900
   };
   
   /** Prior Default date for a new to do item. */
   public final static GregorianCalendar OLD_DEFAULT_DATE 
       = new GregorianCalendar (2050, 11, 31);
   
   /** Default date for a new to do item. */
   public final static GregorianCalendar DEFAULT_DATE 
       = new GregorianCalendar (2050, 11, 1);
   
   /** Default modification date for a to do item. */
   public final static GregorianCalendar DEFAULT_MOD_DATE
       = new GregorianCalendar (1970, 01, 01);
   
   public final static String DEFAULT_START_TIME = "00:00 AM";
   
   public final static String NO_DATE_HIGH = "9999-99-99";
   public final static String NO_DATE_LOW  = " "; 
   
   public final static String   YMD_FORMAT_STRING = "yyyy-MM-dd";
   public final static String   MDY_FORMAT_STRING = "MM-dd-yyyy";
   public final static String   
       COMPLETE_FORMAT_STRING = "EEEE MMMM d, yyyy KK:mm:ss aa zzz";
   
   public final static DateFormat YMD_FORMAT 
       = new SimpleDateFormat (YMD_FORMAT_STRING);
   public final static DateFormat MDY_FORMAT
       = new SimpleDateFormat (MDY_FORMAT_STRING);
   public final static DateFormat COMPLETE_FORMAT
       = new SimpleDateFormat (COMPLETE_FORMAT_STRING);
   
   public final static TimeOfDay MIDNIGHT = new TimeOfDay();
   
   /** Has this item been deleted? */
   private     boolean         deleted         = false;
   
   /** Item Number of this item within a ToDoItems collection. */
   private     int             itemNumber      = -1;
 
 	/** General category into which this item falls */
 	private Tags     tags = new Tags();
   private StringBuilder workTags = new StringBuilder();
 	private TagsNode tagsNode = null;
   
   /** Date on which this item is to be completed. */
   private     Date            dueDate = DEFAULT_DATE.getTime();
   
 	/** The length of time this item is expected to take to complete */
 	private    	TimeDuration    duration = new TimeDuration();
   
   public static final int     DURATION_NA = -1;
   
   public static final int     MINUTES = Calendar.MINUTE;
   
 	/** Amount of warning desired, in days or minutes */
 	private    	TimeDuration    alertPrior = new TimeDuration();
   
   public static final int     ALERT_PRIOR_NA = -1;
 
 	/** The type of item (action, agenda, etc.) */
 	private    	ItemType        type = new ItemType();
 
 	/** True if the time of day for this item is significant */
 	private    	boolean         timeMatters = false;
 
 	/** The priority of the item, where 1 is highest and 5 is lowest */
 	private    	int          		priority = 3;
   
   public final static String[] PRIORITY_LABEL = {
     "0 N/A",
     "High",
     "Medium-High",
     "Medium",
     "Medium-Low",
     "Low"
   };
 
 	/** Status of item. */
 	private    	ActionStatus    status = new ActionStatus();
   
   public  final static String   PENDING_RECURS_STRING = "8";
   public  final static String   CLOSED_STRING   = "9";
 
 	/** Date on which this item was closed */
 	private    	Date            dateDone;
 
 	/** True if this is a private item */
 	private    	boolean         privateFlag = false;
 
 	/** The person or group or function responsible for this item */
 	private    	String          assignedTo = "";
   
   /** A sequence code for this item. */
   private     String          sequence = "";
 
 	/** The title of this item */
 	private    	String          title = "";
   
   /** The title of this item, converted to its lowest common denominator. */
   private     String          commonTitle = "";
 
 	/** The full description of this item */
 	private    	String          description = "";
   
   /** The way in which this item was completed */
   private     String          outcome = "";
   
   /** A Web page with more information about this item. */
   private     String          webPage = "";
 
 	/** 
 		True if the date and time for this item are the point at which 
 		the item is expected to be completed. 
 	 */
 	private    	boolean         endPoint = true;
 
 	/** Number of recurrence intervals between instances of this item */
 	private    	int             recursEvery = 0;
 
 	/** 
 		The interval (week, day, month, quarter, year, etc.) 
 		between instances of this item 
 		*/
 	private    	int          		recursUnit = 0;
   
   public final static int 		NA        = 0;
   public final static int 		DAYS 			= 1;
   public final static int     WEEKS     = 2;
   public final static int     MONTHS 		= 3;
   public final static int			YEARS 		= 4;
   
   /** Day of week on which item is to be scheduled (0 = Sunday) */
   private     int           recursDayOfWeek = -1;
   
   /** Occurrence in month (1 = first, 2 = second, etc., 5 = last) */
   private     int           recursWithinMonth = -1;
   public final static int    LAST         = 5;
   public final static String LAST_DISPLAY = "Last";
 
 	/** Total number of times this item should occur (-1 if no limit) */
 	private    	int          recursNumber = -1;
 
 	/** 
       Zero if recurrence is calculated based on scheduled date, 
       1 if based on actual completion date 
 	 */
 	private    	int          recursBase = 0;
   
  	/** Date that this item was opened */
 	private    	Date         openDate = new Date();
   
   /** Flag used for folder sync operations. */
   private     boolean      synced = false;
   
   /** ID fields. */
   /*
   private     IDListHeader   header;
   private     IDNumber       id;  
   */
   /** Size of file in bytes. */
   private     long         fileLength = 0;
   
   public final static DecimalFormat fileLengthFormatter 
       = new DecimalFormat("###,###,###,###");
   
   /** Last modification date for file. */
   private     Date         lastModDate = DEFAULT_MOD_DATE.getTime();
   
   /** Pointer to TwoDueCommon, to use for alerts. */
   private     TwoDueCommon td;
   
   /** Timer for alerts. */
   private     javax.swing.Timer        alert;
 
 	/**
 	   Constructor with minimal arguments.
 	 */
 	public ToDoItem () {
 		/*
     this.header = header;
     id = new IDNumber (header);
      */
 	}
   
 	/**
 	   Constructor with data record argument.
  
      @param fmt       A DateFormat instance to be used to parse the following string.
      @param toDoRec   DataRecord to be used to populate this item.
 	  */
   public ToDoItem (DateFormat fmt, DataRecord toDoRec) {
     /*
     this.header = header;
     id = new IDNumber (header);
      */
     setMultiple (fmt, toDoRec);
   }
   
   public void setCommon (TwoDueCommon td) {
     this.td = td;
     setAlert();
   }
   
   /**
     Returns a record definition for a To Do item, 
     in com.powersurgepub.psdata.RecordDefinition format.
    
     @return A record format definition for a To Do item.
    */
   public static RecordDefinition getRecDef() {
     RecordDefinition recDef = new RecordDefinition();
     recDef.addColumn (COLUMN_NAME [TAGS]);
     for (int i = 1; i <= TAGS_MAX; i++) {
       String digit = String.valueOf(i).trim();
       recDef.addColumn (TAG + digit);
     }
     recDef.addColumn (COLUMN_NAME [DUE_DATE]);
     recDef.addColumn (COLUMN_NAME [START_TIME]);
     recDef.addColumn (COLUMN_NAME [DURATION]);
     recDef.addColumn (COLUMN_NAME [ALERT_PRIOR]);
     recDef.addColumn (COLUMN_NAME [PRIORITY]);
     recDef.addColumn (COLUMN_NAME [STATUS]);
     recDef.addColumn (COLUMN_NAME [ASSIGNED_TO]);
     recDef.addColumn (COLUMN_NAME [SEQUENCE]);
     recDef.addColumn (COLUMN_NAME [TITLE]);
     recDef.addColumn (COLUMN_NAME [DESCRIPTION]);
     recDef.addColumn (COLUMN_NAME [OUTCOME]);
     recDef.addColumn (COLUMN_NAME [WEB_PAGE]);
     recDef.addColumn (COLUMN_NAME [RECURS_UNIT]);
     recDef.addColumn (COLUMN_NAME [RECURS_EVERY]);
     recDef.addColumn (COLUMN_NAME [RECURS_DAY_OF_WEEK]);
     recDef.addColumn (COLUMN_NAME [RECURS_WITHIN_MONTH]);
     recDef.addColumn (COLUMN_NAME [DUE_DATE_YMD]);
     // recDef.addColumn (COLUMN_NAME [ID]);
     recDef.addColumn (ItemType.getColumnName());
     recDef.addColumn (COLUMN_NAME [FILE_LENGTH]);
     recDef.addColumn (COLUMN_NAME [LAST_MOD_DATE]);
     recDef.addColumn (COLUMN_NAME [LAST_MOD_DATE_YMD]);
     recDef.addColumn (COLUMN_NAME [LINKED_TAGS]);
     return recDef;
   }
   
   /** 
    If this is a recurring item, and has just been closed by the user,
    then create the next recurrence of this item.
    
    @return Next recurrence of this item, or null if no recurrence.
    */
   public ToDoItem recurIfClosed() {
     ToDoItem recursItem = null;
     if (status.getValueAsInt() == (ActionStatus.PENDING_RECURS)) {
       if ((recursEvery > 0) && (recursUnit >= 0)) {
         recursItem = new ToDoItem();
         recursItem.setTags (tags);
         recursItem.setPriority (priority);
         recursItem.setStatus (ActionStatus.OPEN);
         recursItem.setAssignedTo (assignedTo);
         recursItem.setDescription (description);
         recursItem.setSequence (sequence);
         recursItem.setTitle (title);
         recursItem.setDescription (description);
         recursItem.setRecursEvery (recursEvery);
         recursItem.setRecursUnit (recursUnit);
         recursItem.setRecursDayOfWeek (recursDayOfWeek);
         recursItem.setRecursWithinMonth (recursWithinMonth);
         recursItem.setDueDate (dueDate);
         recursItem.recur();
         recursItem.setStartTime (getStartTime());
         recursItem.setDuration (duration.toString());
         recursItem.setAlertPrior (alertPrior);
         recursItem.setFileLength (fileLength);
         recursItem.setLastModDate (lastModDate);
       }
       status.setStatus(ActionStatus.CLOSED);
     } // end if pending recurs
     return recursItem;
   } // end method
   
   /** 
    Increment the due date for this item using its recursion data.
    */
   public void recur() {
     GregorianCalendar recursDate = new GregorianCalendar ();
     recursDate.setTime (dueDate);
     recur (recursDate);
     setDueDate (recursDate.getTime());
   }
   
   /** 
    Increment the passed date using this item's recursion data.
    
    @param recursDate The Gregorian Calendar holding the date to be incremented.
    */
   public void recur(GregorianCalendar recursDate) {
     int recursField = 0;
     int recursAmount = recursEvery;
     switch (recursUnit) {
       case DAYS:
         recursField = GregorianCalendar.DATE;
         break;
       case WEEKS:
         recursField = GregorianCalendar.DATE;
         recursAmount = recursEvery * 7;
         break;
       case MONTHS:
         recursField = GregorianCalendar.MONTH;
         break;
       case YEARS:
         recursField = GregorianCalendar.YEAR;
         break;
       default:
         recursField = GregorianCalendar.DATE;
         break;
     } // end switch
     recursDate.add (recursField, recursAmount);
     if (recursDayOfWeek >= 0
         && recursWithinMonth > 0) {
       int inc = 1;
       int rwm = recursWithinMonth;
       if (recursWithinMonth == LAST) {
         recursDate.set (GregorianCalendar.DAY_OF_MONTH,
             recursDate.getActualMaximum (GregorianCalendar.DAY_OF_MONTH));
         inc = -1;
         rwm = 1;
       } else {
         recursDate.set (GregorianCalendar.DAY_OF_MONTH, 1);
       }
       int count = 0;
       while (count < rwm) {
         if (recursDate.get (GregorianCalendar.DAY_OF_WEEK)
             == recursDayOfWeek) {
           count++;
         }
         if (count < rwm) {
           recursDate.add (GregorianCalendar.DATE, inc);
         }
       } // end while not yet desired week number
     } // end if shooting for a specific day of the week within the month
   } // end method
 
   /**
     Sets multiple fields based on contents of passed record.
  
     @param  fmt      A DateFormat instance to be used to parse the following string.  
     @param  toDoRec  A data record containing multiple fields.
    */
   public void setMultiple (DateFormat fmt, DataRecord toDoRec) {
   
     setType ("Action");
     toDoRec.startWithFirstField();
     DataField nextField;
     String name;
     while (toDoRec.hasMoreFields()) {
       nextField = toDoRec.nextField();
       name = nextField.getCommonFormOfName();
       if (name.equals (COLUMN_NAME [DELETED])) {
         setDeleted (nextField.getDataBoolean());
       }
       else
       if (name.equals (COLUMN_NAME [TAGS]) 
           || name.equalsIgnoreCase("categories")
           || name.equalsIgnoreCase("category")) {
         setTags (nextField.getData());
       }
       else
       if (name.startsWith (TAG)
           || name.startsWith ("category")) {
         int y = name.length() - 1;
         while (y > 0 && Character.isDigit(name.charAt(y))) {
           y--;
         }
         String digit = name.substring(y + 1).trim();
         try {
           int level = Integer.parseInt (digit); 
           setTagsLevel (nextField.getData(), level - 1);
         } catch (NumberFormatException e) {
           System.out.println ("Bad Tags Level of " + digit);
         }
       }
       else
       if (name.equals (COLUMN_NAME [PRIORITY]) || name.equals ("urlpriority")) {
         setPriority (nextField.getDataInteger());
       }
       else
       if (name.equals (COLUMN_NAME [DUE_DATE])) {
         setDueDate (fmt, nextField.getData());
       }
       else
       if (name.equals (COLUMN_NAME [START_TIME])) {
         setStartTime (nextField.getData());
       }
       else
       if (name.equals (COLUMN_NAME [DURATION])) {
         setDuration (nextField.getData());
       }
       else
       if (name.equals (COLUMN_NAME [ALERT_PRIOR])) {
         setAlertPrior (nextField.getData());
       }
       else
       if (name.equals ("done")
           || name.equals ("closed")) {
         setStatus (nextField.getDataBoolean());
       }
       else
       if (name.equals (COLUMN_NAME [STATUS])) {
         int stat = nextField.getDataInteger();
         if (stat == ActionStatus.PENDING_RECURS) {
           stat = ActionStatus.CLOSED;
         }
         setStatus (stat);
       }
       else
       if (name.equals ("who") 
           || name.equals ("lastnamefirst")
           || name.equals (COLUMN_NAME [ASSIGNED_TO])) {
         setAssignedTo (nextField.getData());
       }
       else
       if (name.equals ("versionimplemented")
           || startOfNameEquals (name, "seq")) {
         setSequence (nextField.getData());
       }
       else
       if (name.equals ("action") 
           || name.equals ("companyanddepartment")
           || name.equals (COLUMN_NAME [TITLE])) {
         setTitle (nextField.getData());
       }
       else
       if (name.equals ("description")
           || name.equals ("details")
           || startOfNameEquals (name, "problem")) {
         setDescription (nextField.getData());
         setDefaultTitle();
         if (title.equals ("")) {
           int l = description.length();
           if (l > 50) {
             setTitle (description.substring (0,49));
           } else {
             setTitle (description);
           }          
         } // end if title is blank
       }
       else
       if (name.equals (COLUMN_NAME [OUTCOME])
           || name.equals("resolution")) {
         setOutcome (nextField.getData());
       }
       else
       if (name.equals (COLUMN_NAME [WEB_PAGE])
           || name.equals("url")
           || name.equals("homepage")
           || name.equals("website")) {
         setWebPage (nextField.getData());
       }
       else
       if (name.equals ("interval") 
           || name.equals ("recursperiod")
           || name.equals (COLUMN_NAME [RECURS_UNIT])) {
         if (nextField.isAnInteger()) {
           setRecursUnit (nextField.getDataInteger());
         } else {
           setRecursUnit (nextField.getData());
         }
       }
       else
       if (name.equals ("every") || name.equals (COLUMN_NAME [RECURS_EVERY])) {
         setRecursEvery (nextField.getDataInteger());
       }
       else
       if (name.equals (COLUMN_NAME [RECURS_DAY_OF_WEEK])) {
         setRecursDayOfWeek (nextField.getDataInteger());
       }
       else
       if (name.equals (COLUMN_NAME [RECURS_WITHIN_MONTH])) {
         setRecursWithinMonth (nextField.getDataInteger());
       } 
       else
       if (name.equals (ItemType.getColumnName())) {
         setType (nextField.getData());
       }
       else
       if (name.equals (COLUMN_NAME [FILE_LENGTH])) {
         setFileLength (nextField.getDataLong());
       }
       else
       if (name.equals (COLUMN_NAME [LAST_MOD_DATE])) {
         setLastModDate (fmt, nextField.getData());
       }
       
       /*
       else
       if (name.equals (COLUMN_NAME [ID])) {
         setID (nextField.getData());
       } // end if
       */
     } // end while
   } // end method
   
   /**
     Sets multiple fields based on contents of passed text block.
    
     @param  text  A string formatted as a text block.
     @return       Number of fields found for the next record in the block.
    */
   public int setFromTextBlock (String text) {
     TextBlock block = new TextBlock (text);
     return setFromTextBlock (block);
   }
 
   /**
     Sets multiple fields based on contents of passed text block.
    
     @param  block A formatted text block.
     @return       Number of fields found for the next record in the block.
    */
   public int setFromTextBlock (TextBlock block) {
     DateFormat fmt = new SimpleDateFormat ("MM/dd/yyyy"); 
     int numberOfFields = 0;
     block.startBlockIn (OBJECT_NAME, COLUMN_DISPLAY);
     String name;
     String field;
     boolean anotherField = false;
     do {
       anotherField = block.findNextField();
       if (anotherField) {
         numberOfFields++;
         name = block.getNextLabel();
         field = block.getNextField();
         if (name.equals (COLUMN_DISPLAY [DELETED])) {
           // setDeleted (nextField.getDataBoolean());
         }
         else
         if (name.equals (COLUMN_DISPLAY [TAGS])
             || name.equalsIgnoreCase("categories")
             || name.equalsIgnoreCase("category")) {
           setTags (field);
         }
         else
         if (name.startsWith (TAG)
             || name.startsWith ("category")) {
           int y = name.length() - 1;
           while (y > 0 && Character.isDigit(name.charAt(y))) {
             y--;
           }
           String digit = name.substring(y + 1).trim();
           try {
             int level = Integer.parseInt (digit); 
             setTagsLevel (field, level - 1);
           } catch (NumberFormatException e) {
             System.out.println ("Bad Tags Level of " + digit);
           }
         }
         else
         if (name.equals (COLUMN_DISPLAY [PRIORITY])) {
           setPriority (field);
         }
         else
         if (name.equals (COLUMN_DISPLAY [DUE_DATE])) {
           setDueDate (fmt, field);
         }
         else
         if (name.equals (COLUMN_DISPLAY [START_TIME])) {
           setStartTime (field);
         }
         else
         if (name.equals (COLUMN_DISPLAY [DURATION])) {
           setDuration (field);
         }
         else
         if (name.equals (COLUMN_DISPLAY [ALERT_PRIOR])) {
           setAlertPrior (field);
         }
         else
         if (name.equals (COLUMN_DISPLAY [STATUS])) {
           setStatus (field);
         }
         else
         if (name.equals (COLUMN_DISPLAY [ASSIGNED_TO])) {
           setAssignedTo (field);
         }
         else
         if (name.equals (COLUMN_DISPLAY [SEQUENCE])
             || name.equals ("versionimplemented")
             || startOfNameEquals (name, "seq")) {
           setSequence (field);
         }
         else
         if (name.equals (COLUMN_DISPLAY [TITLE])) {
           setTitle (field);
         }
         else
         if (name.equals (COLUMN_DISPLAY [DESCRIPTION])) {
           setDescription (field);
         }
         else
         if (name.equals (COLUMN_DISPLAY [OUTCOME])) {
           setOutcome (field);
         }
         else
         if (name.equals (COLUMN_DISPLAY [WEB_PAGE])) {
           setWebPage (field);
         }
         else
         if (name.equals (COLUMN_DISPLAY [RECURS_UNIT])) {
           try {
             int period = Integer.parseInt (field);
             setRecursUnit (period);
           } catch (NumberFormatException e) {
             setRecursUnit (field);
           }
         }
         else
         if (name.equals (COLUMN_DISPLAY [RECURS_EVERY])) {
           setRecursEvery (field);
         }
         else
         if (name.equals (COLUMN_DISPLAY [RECURS_DAY_OF_WEEK])) {
           setRecursDayOfWeek (field);
         }
         else
         if (name.equals (COLUMN_DISPLAY [RECURS_WITHIN_MONTH])) {
           setRecursWithinMonth (field);
         }
         else
         if (name.equals (ItemType.getColumnDisplayName())) {
           setType (field);
         } // end if name equals...
       } // end if another field
     } while (! block.endOfBlock());
     return numberOfFields;
   } // end method
   
   private boolean startOfNameEquals (String name, String s) {
     if (name.length() < s.length()) {
       return false;
     } 
     else
     if (name.substring (0, s.length()).equals (s)) {
       return true;
     } else {
       return false;
     }
   }
   
   /**
     Return this object, formatted as a DataRecord.
    
     @param recDef Record Definition to be used in building the record. 
     */
   public DataRecord getDataRec (RecordDefinition recDef) {
     DataRecord nextRec = new DataRecord();
     nextRec.addField (recDef, getTags().toString());
     for (int i = 0; i < TAGS_MAX; i++) {
       nextRec.addField (recDef, getTagLevel (i));
     }
     return addFieldsBeyondTags (recDef, nextRec);
   }
   
   /**
     Return this object, formatted as a DataRecord, but with only the 
     tag at the specified level, instead of all tags.
    
     @param recDef Record Definition to be used in building the record. 
     */
   public DataRecord getDataRec (RecordDefinition recDef, int tagIndex) {
     DataRecord nextRec = new DataRecord();
     String tags = getTags().getTag(tagIndex);
     if (tags.length() > 0) {
       nextRec.addField (recDef, tags);
       for (int i = 0; i < TAGS_MAX; i++) {
         nextRec.addField (recDef, getTagLevel (i));
       }
       return addFieldsBeyondTags (recDef, nextRec);
     } else {
       return null;
     }
   }
   
   private DataRecord addFieldsBeyondTags 
       (RecordDefinition recDef, DataRecord nextRec) {
     DateFormat fmt = new SimpleDateFormat ("MM/dd/yyyy");
     nextRec.addField (recDef, getDueDate (fmt));
     nextRec.addField (recDef, getStartTimeAsString());
     nextRec.addField (recDef, getDurationAsString());
     nextRec.addField (recDef, getAlertPriorAsString());
     nextRec.addField (recDef, String.valueOf (getPriority ()));
     nextRec.addField (recDef, String.valueOf (getStatus ()));
     nextRec.addField (recDef, getAssignedTo ());
     nextRec.addField (recDef, getSequence ());
     nextRec.addField (recDef, getTitle ());
     nextRec.addField (recDef, getDescription ());
     nextRec.addField (recDef, getOutcome ());
     nextRec.addField (recDef, getWebPage ());
     nextRec.addField (recDef, getRecursUnitAsString ());
     nextRec.addField (recDef, String.valueOf (getRecursEvery ()));
     nextRec.addField (recDef, String.valueOf (getRecursDayOfWeek ()));
     nextRec.addField (recDef, String.valueOf (getRecursWithinMonth ()));
     nextRec.addField (recDef, getDueDate (YMD_FORMAT));
     nextRec.addField (recDef, getTypeName());
     nextRec.addField (recDef, getFileLengthAsString());
     nextRec.addField (recDef, getLastModDate(fmt));
     nextRec.addField (recDef, getLastModDate (YMD_FORMAT));
     nextRec.addField (recDef, getLinkedTags("tags/"));
     return nextRec;
   }
   
     /**
     Return this object, formatted as a block of self-evident text.
      
     @return String with each important, non-null field in it, with
             each field preceded by the field's label and a colon, and 
             followed by a line feed character.
     */
   public String getTextBlock () {
     TextBlock block = new TextBlock();
     block.startBlockOut (OBJECT_NAME);
     // Create a date formatter
     DateFormat fmt = new SimpleDateFormat ("MM/dd/yyyy"); 
     block.addField ("From", System.getProperty ("user.name"));
     String duedate = getDueDate(fmt).trim();
     if (duedate.length() > 0) {
       block.addField (COLUMN_DISPLAY [DUE_DATE], duedate);
     }
     block.addField (COLUMN_DISPLAY [PRIORITY], String.valueOf (getPriority ()));
     for (int i = 0; i < TAGS_MAX; i++) {
       block.addField (COLUMN_DISPLAY [TAGS] + String.valueOf(i+1), 
           getTagLevel (i));
     }
     block.addField (COLUMN_DISPLAY [TITLE], getTitle ());
     block.addField (COLUMN_DISPLAY [STATUS], getStatusLabel());
     block.addField (ItemType.getColumnDisplayName(), getTypeName());
     block.addField (COLUMN_DISPLAY [ASSIGNED_TO], getAssignedTo ());
     block.addField (COLUMN_DISPLAY [SEQUENCE], getSequence ());
     block.addField (COLUMN_DISPLAY [DESCRIPTION], getDescriptionSansHTML());
     block.addField (COLUMN_DISPLAY [OUTCOME], getOutcomeSansHTML ());
     block.addField (COLUMN_DISPLAY [WEB_PAGE], getWebPage ());
     String startTime = getStartTimeAsString();
     if (! startTime.equals (DEFAULT_START_TIME)) {
       block.addField (COLUMN_DISPLAY [START_TIME], getStartTimeAsString());
     }
     String duration = getDurationAsString();
     if (! duration.equals (NA_STRING)) {
       block.addField (COLUMN_DISPLAY [DURATION], duration);
     }
     String alertprior = getAlertPriorAsString();
     if (! alertprior.equals (NA_STRING)) {
       block.addField (COLUMN_DISPLAY [ALERT_PRIOR], alertprior);
     }
     if (recursEvery > 0) {
       block.addField (COLUMN_DISPLAY [RECURS_EVERY], 
           String.valueOf (getRecursEvery ()));
     }
     String recursunit = getRecursUnitAsString ();
     if (! recursunit.equals (NA_STRING)) {
       block.addField (COLUMN_DISPLAY [RECURS_UNIT], recursunit);
     }
     if (recursWithinMonth >= 0) {
       block.addField (COLUMN_DISPLAY [RECURS_WITHIN_MONTH], 
           getRecursWithinMonthName ());
     }
     if (recursDayOfWeek >= 0) {
       block.addField (COLUMN_DISPLAY [RECURS_DAY_OF_WEEK], 
           getRecursDayOfWeekName ());
     }
     block.endBlockOut();
     return block.toString();
   }
   
   /**
     Sets the deleted flag on for this item.
    */
   public void setDeleted () {
     setDeleted (true);
     
   }
   
   /**
     Sets the deleted flag for this item.
    
     @param deleted True if this record has been deleted by the user.
    */
   public void setDeleted (boolean deleted) {
     this.deleted = deleted;
     setAlert();
   }
   
   /**
     Gets the deleted flag for this item.
    
     @return True if this record has been deleted by the user.
    */
   public boolean isDeleted () {
     return deleted;
   }
   
   /**
     Sets the tags for this item.
    
     @param  tags  A string representing the tags 
                   that should be assigned to this item.
    */
   public void setTags (String tags) {
     this.tags.set (tags);
   }
   
   /**
     Sets the tags for this item.
    
     @param  tags The tags that should be assigned to this item.  
    */
   public void setTags (Tags tags) {
     this.tags = tags;
   }
   
 	/**
 	 Return the tags assigned to this taggable item. 
 
 	 @return The tags assigned. 
 	 */
 	public Tags getTags () {
 		return tags;
 	}
   
   public String getTagsAsString() {
     return tags.toString();
   }
   
   public String getLinkedTags (String parent) {
     return tags.getLinkedTags(parent);
   }
   
   public boolean equalsTags (String tags2) {
     return tags.toString().equals (tags2.trim());
   }
 	
 	/**
 	 Flatten all the tags for this item, separating each level/word into its own
 	 first-level tag.
 	 */
 	public void flattenTags () {
 		tags.flatten();
 	}
 	
 	/**
 	 Convert the tags to all lower-case letters.
 	 */
 	public void lowerCaseTags () {
 		tags.makeLowerCase();
 	}
 	
 	/**
 	 Compare one taggable item to another, for sequencing purposes.
 	
 	 @param  The second object to be compared to this one.
 	 @return A negative number if this taggable item is less than the passed
 	         taggable item, zero if they are equal, or a positive number if
 	         this item is greater.
 	 */
 	public int compareTo (Object obj2) {
 		int comparison = -1;
 		if (obj2.getClass().getSimpleName().equals ("ToDoItem")) {
 			ToDoItem item2 = (ToDoItem)obj2;
 		comparison = this.getTitle().compareToIgnoreCase(item2.getTitle());
 		}
 		return comparison;
 	}
 	
 	/**
 	 Determine if this taggable item has a key that is equal to the passed
 	 taggable item.
 	
 	 @param  The second object to be compared to this one.
 	 @return True if the keys are equal.
 	 */
 	public boolean equals (Object obj2) {
 		boolean eq = false;
 		if (obj2.getClass().getSimpleName().equals ("ToDoItem")) {
 			ToDoItem item2 = (ToDoItem)obj2;
 			eq = (this.getTitle().equalsIgnoreCase (item2.getTitle()));
 		}
 		return eq;
 	}
 	
 	/**
 	 Set the first TagsNode occurrence for this Taggable item. This is stored
 	 in a TagsModel occurrence.
 	
 	 @param tagsNode The tags node to be stored.
 	 */
 	public void setTagsNode (TagsNode tagsNode) {
 		this.tagsNode = tagsNode;
 	}
 	
 	/**
 	 Return the first TagsNode occurrence for this Taggable item. These nodes
 	 are stored in a TagsModel occurrence.
 	
 	 @return The tags node stored. 
 	 */
 	public TagsNode getTagsNode () {
 		return tagsNode;
 	}
   
   /**
     Set one category level. Note that this is only allowed if the
     level to be set is a higher number than any previously
     set levels. The bottom line is that this method may be used
     to set one level at a time, but only when the category is initially
     being populated, and only when the levels are set in ascending
     sequence by level number. 
    
     @param  inSubCat Category string at given level.
    
     @param  level    Level at which category is to be set, with
                      zero indicating the first level.
    */
   public void setTagsLevel (String inSubCat, int level) {
     if (workTags.length() > 0) {
       workTags.append('.');
     }
     workTags.append(inSubCat);
     tags.set(workTags.toString());
   }
   
   /**
     Returns a particular category level for this item.
    
     @return Category level for this item.
    
     @param  level Level to be obtained, with the first denoted by 0.
    */
   public String getTagLevel (int level) {
     return tags.getLevel (0, level);
   }
   
   /**
     Sets the due date to today's date. 
    */
   public void setDueDateToday () {
     
     setDueDate (new GregorianCalendar());
   }
   
   public void setDueDateDefault() {
     setDueDate (DEFAULT_DATE);
   }
   
   /**
      Sets the due date for this item.
  
      @param  fmt  A DateFormat instance to be used to parse the following string.
      @param  date String representation of a date.
    */
   public void setDueDate (DateFormat fmt, String date) {
     
     try {
       setDueDate (fmt.parse (date));
     } catch (ParseException e) {
     }
 
   } // end method
   
   /**
      Sets the due date for this item from a Gregorian Calendar date. 
   
      @param dueDateCal Desired due date in Gregorian Calendar format. 
    */
   public void setDueDate (GregorianCalendar dueDateCal) {
     setDueDate(dueDateCal.getTime());
   }
   
   /**
      Sets the due date for this item.
  
      @param  date Date representation of a date.
    */
   public void setDueDate (Date date) {
     
     if (date.equals (OLD_DEFAULT_DATE.getTime())) {
       dueDate = DEFAULT_DATE.getTime();
     } else {
       dueDate = date;
     }
     setAlert();
 
   } // end method
   
   public boolean hasDueDate() {
     return (! dueDate.equals (DEFAULT_DATE.getTime()));
   }
   
   /**
      Gets the due date for this item, formatted as a string.
  
      @return  String representation of a date.
      @param   fmt  A DateFormat instance to be used to format the date as a string.
 
    */
   public String getDueDate (DateFormat fmt) {
     
     if (dueDate.equals (DEFAULT_DATE.getTime())) {
       return " ";
     } else {
       return fmt.format (dueDate);
     }
 
   } // end method
   
   /**
      Gets the due date for this item, formatted as a string 
      in yyyy/mm/dd format.
  
      @return  String representation of a date in yyyy/mm/dd format.
    */
   public String getDueDateYMD () {
     
     if (dueDate.equals (DEFAULT_DATE.getTime())) {
       return NO_DATE_LOW;
     } else {
       return YMD_FORMAT.format (dueDate);
     }
 
   } // end method
   
   /**
      Gets the due date for this item, formatted as a string 
      in yyyy/mm/dd format, with null dates optionally set high or low.
  
      @return  String representation of a date in yyyy/mm/dd format.
      @param   undatedHigh If true, then return a high date rather than low.
    */
   public String getDueDateYMD (boolean undatedHigh) {
     
     if (dueDate.equals (DEFAULT_DATE.getTime())) {
       if (undatedHigh) {
         return NO_DATE_HIGH;
       } else {
         return NO_DATE_LOW;
       }
     } else {
       return YMD_FORMAT.format (dueDate);
     }
 
   } // end method
   
   /**
      Gets the due date for this item.
  
      @return  date Date representation of a date.
    */
   public Date getDueDate () {
     
     return dueDate;
 
   } // end method
   
   /**
     See if due date is unspecified (equal to default date).
    
     @return True if due date is still equal to the default future value.
    */
   public boolean isOngoing () {
     return (dueDate.equals (OLD_DEFAULT_DATE.getTime())
       || dueDate.equals (DEFAULT_DATE.getTime()));
   }
   
 
   /**
      Sets the time at which this item should start.
  
      @param  startTime The time at which this item should start.
    */
   public void setStartTime (String startTime) {
     TimeOfDay work = new TimeOfDay (startTime);
     setStartTime (work);
   }
   
   /**
      Sets the time at which this item should start.
  
      @param  startTime The time at which this item should start.
    */
   public void setStartTime (TimeOfDay startTime) {
     dueDate = startTime.get (dueDate);
     setAlert();
   }
   
   /**
      Returns the time at which this item should start.
  
      @return The time at which this item should start.
    */
   public TimeOfDay getStartTime () {
     return new TimeOfDay (dueDate);
   }
 
   /**
      Returns the time at which this item should start.
  
      @return The time at which this item should start.
    */
   public String getStartTimeAsString () {
     return new TimeOfDay(dueDate).toString();
   }
 
   /**
      Sets the length of time this item should last, in minutes.
  
      @param  duration The length of time this item should last, in minutes.
    */
   public void setDuration (int duration) {
     this.duration.set (duration);
   }
   
   /**
      Sets the length of time this item should last, in minutes.
  
      @param  duration The length of time this item should last, in minutes.
    */
   public void setDuration (String duration) {
     this.duration.set (duration);
   }
   
   /**
      Returns the length of time this item should last, in minutes.
  
      @return The length of time this item should last, in minutes.
    */
   public TimeDuration getDuration () {
     return duration;
   }
 
   /**
      Returns the length of time this item should last, in minutes.
  
      @return The length of time this item should last, in minutes.
    */
   public int getDurationAsInt () {
     return duration.get();
   }
   
   /**
      Returns the length of time this item should last, in minutes.
  
      @return The length of time this item should last, in minutes.
    */
   public String getDurationAsString () {
     return duration.toString();
   }
   
   /**
      Sets the number of minutes prior to the start time that you 
      wish to be reminded of this item.
  
      @param  alertPrior The number of minutes prior to the start time 
                         that you wish to be reminded of this item.
    */
   public void setAlertPrior (TimeDuration alertPrior) {
     this.alertPrior = alertPrior;
     setAlert();
   }
 
   /**
      Sets the number of minutes prior to the start time that you 
      wish to be reminded of this item.
  
      @param  alertPrior The number of minutes prior to the start time 
                         that you wish to be reminded of this item.
    */
   public void setAlertPrior (int alertPrior) {
     this.alertPrior.set (alertPrior);
     setAlert();
   }
   
   /**
      Sets the number of minutes prior to the start time that you 
      wish to be reminded of this item.
  
      @param  alertPrior The number of minutes prior to the start time 
                         that you wish to be reminded of this item.
    */
   public void setAlertPrior (String alertPrior) {
     this.alertPrior.set (alertPrior);
     setAlert();
   }
 
   /**
      Returns the number of minutes prior to the start time 
      that you wish to be reminded of this item.
  
      @return The number of minutes prior to the start time 
              that you wish to be reminded of this item.
    */
   public int getAlertPrior () {
     return alertPrior.get();
   }
   
   /**
      Returns the number of minutes prior to the start time 
      that you wish to be reminded of this item.
  
      @return The number of minutes prior to the start time 
              that you wish to be reminded of this item.
    */
   public String getAlertPriorAsString () {
     return alertPrior.toString();
   }
   
   /**
     Evaluate alert fields whenever one of them has changed, to see
     if an alert needs to be set.
    */
   public void setAlert() {
     
     // If alert is already set, stop it
     stopAlert();
     
     // See if we need to set the alert
     if (td != null
         && (! deleted)
         && isDueToday()
         && alertPrior.get() > ALERT_PRIOR_NA
         && (! getStartTime().equals (MIDNIGHT))) {
       GregorianCalendar now = new GregorianCalendar();
       GregorianCalendar start = new GregorianCalendar();
       start.setTime (dueDate);
       if (start.after (now)) {
         if (alert == null) {
           alert = new javax.swing.Timer(1000, new ActionListener() {
             public void actionPerformed (ActionEvent evt) {
               displayAlert();
             } // end alert action performed
           });
         } // end if we need to construct the alert
         long initialDelayLong = start.getTimeInMillis() - now.getTimeInMillis();
         int initialDelay = (int)initialDelayLong;
         int prior = getAlertPrior() * 1000 * 60;
         if (initialDelay > prior) {
           initialDelay = initialDelay - prior;
         } 
         alert.setInitialDelay (initialDelay);
         alert.setRepeats (false);
         alert.start();
       } // end if start time is today and hasn't already passed
     } // end if it looks like we have an alert situation
   } // end method setAlert
   
   public void stopAlert () {
     // If alert is already set, stop it
     if (alert != null
         && alert.isRunning()) {
       alert.stop();
     }
   }
   
   private void displayAlert() {
     
     Toolkit tk = Toolkit.getDefaultToolkit();
     tk.beep();
     Object[] options = {"Tell Me More",
                         "OK"};
     int userChoice = JOptionPane.showOptionDialog (
         td.tabs1,
         "Item "
         + title
         + GlobalConstants.LINE_FEED 
         + "is due at "
         + getStartTimeAsString(),
         "Item Alert",
         JOptionPane.YES_NO_OPTION,
         JOptionPane.INFORMATION_MESSAGE,
         null,
         options,
         options[1]);
     if (userChoice == JOptionPane.YES_OPTION) {
       td.selectItem (this);
     } // end if user wants more info
   }
   
   /**
      Indicates whether this item is due tomorrow.
    
      @return True if this item's due date is tomorrow, and if it is 
              still open.
    */
   public boolean isDueTomorrow() {
     return (getLateCode() == 1);
   }
   
   /**
      Indicates whether this item is due today.
    
      @return True if this item's due date is today, and if it is 
              still open.
    */
   public boolean isDueToday() {
     return (getLateCode() == 0);
   }
   
   /**
      Indicates whether this item is late.
    
      @return True if this item was due in the past.
    */
   public boolean isLate() {
     return (getLateCode() < 0);
   }
   
   /**
    Indicate whether item is overdue, due today, tomorrow, or in the future.
    
    @return -1 if due before today,
             0 if due today, or
            +1 if due tomorrow, or
            +2 if in the future or already done. 
    */
   public int getLateCode () {
     return (DateUtils.getLateCode (isDone(), dueDate));
   }
 
   public void setType (int type) {
     this.type.set (type);
   }
   
   public int setType (String typeName) {
     return (type.set (typeName));
   }
   
   public int getType () {
     return type.get();
   }
   
   public ItemType getTypeObject () {
     return type;
   }
   
   public String getTypeName () {
     return (type.getName());
   }
   
   public static String getTypeName (int i) {
     return (ItemType.getName (i));
   }
   
   public static boolean isValidType (int i) {
     return (ItemType.isValidIndex(i));
   }
   
   public static int typeSize() {
     return ItemType.size();
   }
 
   /**
      Sets true if the time of day for this item is significant.
  
      @param  timeMatters True if the time of day for this item is significant.
    */
   public void setTimeMatters (boolean timeMatters) {
     this.timeMatters = timeMatters;
   }
 
   /**
      Returns true if the time of day for this item is significant.
  
      @return True if the time of day for this item is significant.
    */
   public boolean getTimeMatters () {
     return timeMatters;
   }
   
   /**
      Sets the priority of the item, where 1 is highest and 5 is lowest.
  
      @param  priorityString The priority of the item, 
                             where 1 is highest and 5 is lowest.
    */
   public void setPriority (String priorityString) {
     try {
       int p = Integer.parseInt (priorityString);
       setPriority (p);
     } catch (NumberFormatException e) {
       System.out.println ("Trouble parsing priority " + priorityString);
     }
   }
 
   /**
      Sets the priority of the item, where 1 is highest and 5 is lowest.
  
      @param  priority The priority of the item, where 1 is highest and 5 is lowest.
    */
   public void setPriority (int priority) {
     if (priority >= 1 && priority <= 5) { 
       this.priority = priority;
     }
   }
 
   /**
      Returns the priority of the item, where 1 is highest and 5 is lowest.
  
      @return The priority of the item, where 1 is highest and 5 is lowest.
    */
   public int getPriority () {
     return priority;
   }
   
   /**
     Returns a String with a label for the status value.
    
     @return Status value.
    */
   public String getPriorityLabel () {
     return getPriorityLabel (priority);
   }
   
   /**
     Returns a String with a label for the priority value.
    
     @return Priority value.
    
     @param  status Priority integer to be converted to a String label.
    */
   public static String getPriorityLabel (int priority) {
     if (priority < 1 || priority >= PRIORITY_LABEL.length) {
       return String.valueOf (priority) + "-Out of Range";
     } else {
       return PRIORITY_LABEL [priority];
     }
   }
   
   /**
      Sets zero if false, 9 if true.
  
      @param  done True if item has been completed.
    */
   public void setStatus (String statusString) {
     status.setValue(statusString);
   }
   
   /**
      Sets zero if false, 9 if true.
  
      @param  done True if item has been completed.
    */
   public void setStatus (boolean done) {
     status.setValue(done);
   }
 
   /**
      Sets Status of item.
  
      @param  status Status of item.
    */
   public void setStatus (int status) {
     this.status.setValue(status);
     setAlert();
   }
 
 
   /**
      Returns Status of item.
  
      @return Status of item.
    */
   public int getStatus () {
     return status.getValueAsInt();
   }
   
   /**
     Returns a String with a label for the status value.
    
     @return Status value.
    */
   public String getStatusLabel () {
     return status.getLabel();
   }
   
   /**
     Returns a String with a label for the status value.
    
     @return Status value.
    
     @param  status Status integer to be converted to a String label.
    */
   public static String getStatusLabel (int status) {
     return ActionStatus.getLabel(status);
   }
   
   /**
      Indicates whether item is still pending.
  
      @return True if item is canceled or closed.
    */
   public boolean isDone () {
     return (status.isDone());
   }
   
   /**
      Indicates whether item is still pending.
  
      @return True if item is open or in-work.
    */
   public boolean isNotDone () {
     return (status.isNotDone());
   }
 
   /**
      Sets date on which this item was closed.
  
      @param  dateDone Date on which this item was closed.
    */
   public void setDateDone (Date dateDone) {
     this.dateDone = dateDone;
   }
 
   /**
      Returns date on which this item was closed.
  
      @return Date on which this item was closed.
    */
   public Date getDateDone () {
     return dateDone;
   }
 
   /**
      Sets true if this is a private item.
  
      @param  privateFlag True if this is a private item.
    */
   public void setPrivateFlag (boolean privateFlag) {
     this.privateFlag = privateFlag;
   }
 
   /**
      Returns true if this is a private item.
  
      @return True if this is a private item.
    */
   public boolean isPrivate () {
     return privateFlag;
   }
 
   /**
      Sets the person or group or function responsible for this item.
  
      @param  assignedTo The person or group or function responsible for this item.
    */
   public void setAssignedTo (String assignedTo) {
     this.assignedTo = StringUtils.purify(assignedTo).trim();
   }
 
   /**
      Returns the person or group or function responsible for this item.
  
      @return The person or group or function responsible for this item.
    */
   public String getAssignedTo () {
     return assignedTo;
   }
   
   /**
      Sets the sequence identifier for this item.
  
      @param  sequence The sequence identifier for this item.
    */
   public void setSequence (String sequence) {
     this.sequence = StringUtils.purify(sequence).trim();
   }
 
   /**
      Returns the sequence identifier for this item.
  
      @return The sequence identifier for this item.
    */
   public String getSequence () {
     return sequence;
   }
 
   /**
    Set title from description, if title is blank
    */
   public void setDefaultTitle () {
     if (title.equals ("")) {
       int l = description.length();
       if (l > 50) {
         setTitle (description.substring (0,49));
       } else {
         setTitle (description);
       }          
     } // end if title is blank
   }
   /**
      Sets the title of this item.
  
      @param  title The title of this item.
    */
   public void setTitle (String title) {
     this.title = StringUtils.purifyInvisibles(title).trim();
   }
 
   /**
      Returns the title of this item.
  
      @return The title of this item.
    */
   public String getTitle () {
     return title;
   }
   
   public void setCommonTitle() {
     commonTitle = StringUtils.commonName(title);
   }
   
   public String getCommonTitle() {
     return commonTitle;
   }
 
   /**
      Sets the full description of this item.
  
      @param  description The full description of this item.
    */
   public void setDescription (String description) {
     this.description = StringUtils.replaceString 
         (StringUtils.encodeHTML(description).trim(), 
           "<br />", 
           "<br>");
   }
 
   /**
      Returns the full description of this item.
  
      @return The full description of this item.
    */
   public String getDescription () {
     return description;
   }
   
   /**
      Returns the full description of this item, but with CR/LFs replacing HTML
      br tags.
  
      @return The full description of this item, with HTML replaced by CR/LFs.
    */
   public String getDescriptionSansHTML () {
     return StringUtils.decodeHTML (description);
   }
   
   /**
      Sets the outcome or result for this item.
  
      @param  outcome The outcome for this item.
    */
   public void setOutcome (String outcome) {
     this.outcome = StringUtils.replaceString 
         (StringUtils.encodeHTML(outcome).trim(), 
           "<br />", 
           "<br>");
   }
 
   /**
      Returns the outcome for this item.
  
      @return The outcome for this item.
    */
   public String getOutcome () {
     return outcome;
   }
   
   /**
      Returns the outcome for this item, with HTML decoded.
  
      @return The outcome for this item, with HTML decoded.
    */
   public String getOutcomeSansHTML () {
     return StringUtils.decodeHTML (outcome);
   }
   
   public void setWebPage (URL url) {
     setWebPage (url.toString());
   }
   
   /**
      Sets the Web page for this item.
  
      @param  webPage The web page for this item.
    */
   public void setWebPage (String webPage) {
     this.webPage = StringUtils.cleanURLString(webPage);
   }
 
   /**
      Returns the Web page for this item.
  
      @return The Web page for this item.
    */
   public String getWebPage () {
     return webPage;
   }
 
   /**
      Sets true if the date and time for this item is the 
      point at which the item is expected to be completed.
  
      @param  endPoint True if the date and time for this item is the 
      point at which the item is expected to be completed.
    */
   public void setEndPoint (boolean endPoint) {
     this.endPoint = endPoint;
   }
 
   /**
      Returns true if the date and time for this item is the 
      point at which the item is expected to be completed.
  
      @return True if the date and time for this item is the 
      point at which the item is expected to be completed.
    */
   public boolean getEndPoint () {
     return endPoint;
   }
   
   /**
     Is this a recurring item?
    
     @return True if recurs values are non-zero.
    */
   public boolean isRecurring() {
     return ((recursEvery > 0) && (recursUnit > NA));
   }
   
   /**
      Sets number of recurrence intervals between instances of this item.
  
      @param  recursEvery Number of recurrence intervals between instances of this item.
    */
   public void setRecursEvery (String recursEvery) {
     try {
       setRecursEvery (Integer.parseInt(recursEvery.trim()));
     } catch (NumberFormatException e) {
       System.out.println (recursEvery + " is not a valid recurs interval");
     }
   }
 
   /**
      Sets number of recurrence intervals between instances of this item.
  
      @param  recursEvery Number of recurrence intervals between instances of this item.
    */
   public void setRecursEvery (int recursEvery) {
     this.recursEvery = recursEvery;
   }
 
   /**
      Returns number of recurrence intervals between instances of this item.
  
      @return Number of recurrence intervals between instances of this item.
    */
   public int getRecursEvery () {
     return recursEvery;
   }
   
   /**
      Sets the interval (week, day, month, quarter, year, etc.) 
      between instances of this item.
  
      @param  recursUnit The interval (week, day, month, quarter, year, etc.) 
      between instances of this item.
    */
   public void setRecursUnit (String recursUnit) {
     char u = ' ';
     if (recursUnit.length() > 0) {
       u = Character.toLowerCase (recursUnit.charAt (0));
     }
     if (u == 'd') {
       this.recursUnit = DAYS;
     } 
     else
     if (u == 'w') {
       this.recursUnit = WEEKS;
     } 
     else
     if (u == 'm') {
       this.recursUnit = MONTHS;
     } 
     else
     if (u == 'y') {
       this.recursUnit = YEARS;
     } 
     else {
       this.recursUnit = NA;
     }
   }
 
   /**
      Sets the interval (week, day, month, quarter, year, etc.) between instances of this item.
  
      @param  recursUnit The interval (week, day, month, quarter, year, etc.) between instances of this item.
    */
   public void setRecursUnit (int recursUnit) {
     this.recursUnit = recursUnit;
   }
 
   /**
      Returns the interval (week, day, month, quarter, year, etc.) between instances of this item.
  
      @return The interval (week, day, month, quarter, year, etc.) between instances of this item.
    */
   public int getRecursUnit () {
     return recursUnit;
   }
   
   /**
      Returns the interval as a string.
  
      @return The interval (week, day, month, quarter, year, etc.) as a string.
    */
   public String getRecursUnitAsString () {
     String unit;
     switch (recursUnit) {
       case NA:
         unit = NA_STRING;
         break;
       case DAYS:
         unit = "Days";
         break;
       case WEEKS:
         unit = "Weeks";
         break;
       case MONTHS:
         unit = "Months";
         break;
       case YEARS:
         unit = "Years";
         break;
       default:
         unit = NA_STRING;
         break;
     }
     return unit;
   }
   
   /**
     Sets day of week on which item is to be scheduled, passing a
     String as input.
    
     @param dayOfWeekName String form of day of week.
    */
   public void setRecursDayOfWeek (String dayOfWeekName) {
     String name = dayOfWeekName.substring(0,2).toLowerCase();
     int dayOfWeek = -1;
     if (name.equals("su")) {
       dayOfWeek = Calendar.SUNDAY;
     }
     else
     if (name.equals("mo")) {
       dayOfWeek = Calendar.MONDAY;
     }
     else
     if (name.equals("tu")) {
       dayOfWeek = Calendar.TUESDAY;
     }
     else
     if (name.equals("we")) {
       dayOfWeek = Calendar.WEDNESDAY;
     }
     else
     if (name.equals("th")) {
       dayOfWeek = Calendar.THURSDAY;
     }
     else
     if (name.equals("fr")) {
       dayOfWeek = Calendar.FRIDAY;
     }
     else
     if (name.equals("sa")) {
       dayOfWeek = Calendar.SATURDAY;
     }
     setRecursDayOfWeek (dayOfWeek);
   }
 
   /**
      Sets day of week on which item is to be scheduled.
  
      @param  recursDayOfWeek Day of week on which item is to be scheduled
                              (0 = Sunday)
    */
   public void setRecursDayOfWeek (int recursDayOfWeek) {
     if (recursDayOfWeek >= -1
         && recursDayOfWeek <= Calendar.SATURDAY) {
       this.recursDayOfWeek = recursDayOfWeek;
     }
   }
 
   /**
      Returns day of week on which item should be scheduled.
  
      @return Day of week on which item should be scheduled.
    */
   public int getRecursDayOfWeek () {
     return recursDayOfWeek;
   }
   
   /**
      Returns day of week on which item should be scheduled.
  
      @return Day of week on which item should be scheduled.
    */
   public String getRecursDayOfWeekName () {
     String name;
     switch (recursDayOfWeek) {
       case (Calendar.SUNDAY):
         name = "Sunday";
         break;
       case (Calendar.MONDAY):
         name = "Monday";
         break;
       case (Calendar.TUESDAY):
         name = "Tuesay";
         break;
       case (Calendar.WEDNESDAY):
         name = "Wednesday";
         break;
       case (Calendar.THURSDAY):
         name = "Thursday";
         break;
       case (Calendar.FRIDAY):
         name = "Friday";
         break;
       case (Calendar.SATURDAY):
         name = "Saturday";
         break;
       default:
         name = NA_STRING;
         break;
     }
     return name;
   }
 
   /**
      Sets position of a day of week within the month, passing a String as input.
  
      @param  recursWithinMonth 1st = first Monday in month, 2nd = second, etc.
    */
   public void setRecursWithinMonth (String recursWithinMonth) {
     String digit = recursWithinMonth.substring(0,1);
     int i = -1;
     if (digit.equalsIgnoreCase ("L")) {
       i = 5;
     } 
     else
     if (digit.equalsIgnoreCase ("N")) {
       i = -1;
     } else {
       try {
         i = Integer.parseInt (digit);
       } catch (NumberFormatException e) {
         i = -1;
       }
     }
     setRecursWithinMonth (i);
   }
   
   /**
      Sets position of a day of week within the month .
  
      @param  recursWithinMonth 1 = first Monday in month, 2 = second, 
                                5 = last, etc.
    */
   public void setRecursWithinMonth (int recursWithinMonth) {
     if (recursWithinMonth >= -1
         && recursWithinMonth <= 5) {
       this.recursWithinMonth = recursWithinMonth;
     }
   }
   
   /**
      Returns position of a day of week within the month.
  
      @return 1 = first Monday in month, 2 = second, etc.
    */
   public int getRecursWithinMonth () {
     return recursWithinMonth;
   }
   
   /**
      Returns position of a day of week within the month, as a String.
  
      @return N/A, 1st, 2nd, etc. 
    */
   public String getRecursWithinMonthName () {
     switch (recursWithinMonth) {
       case 1:
         return "1st";
       case 2:
         return "2nd";
       case 3:
         return "3rd";
       case 4:
         return "4th";
       case 5:
         return "Last";
       default:
         return NA_STRING;
     }
   }
 
   /**
      Sets total number of times this item should occur (-1 if no limit).
  
      @param  recursNumber Total number of times this item should occur (-1 if no limit).
    */
   public void setRecursNumber (int recursNumber) {
     this.recursNumber = recursNumber;
   }
 
   /**
      Returns total number of times this item should occur (-1 if no limit).
  
      @return Total number of times this item should occur (-1 if no limit).
    */
   public int getRecursNumber () {
     return recursNumber;
   }
 
   /**
      Sets zero if recurrence is calculated based on scheduled date, 1 if based on actual completion date.
  
      @param  recursBase Zero if recurrence is calculated based on scheduled date, 1 if based on actual completion date.
    */
   public void setRecursBase (int recursBase) {
     this.recursBase = recursBase;
   }
 
   /**
      Returns zero if recurrence is calculated based on scheduled date, 1 if based on actual completion date.
  
      @return Zero if recurrence is calculated based on scheduled date, 1 if based on actual completion date.
    */
   public int getRecursBase () {
     return recursBase;
   }
   
   /**
      Sets date that this item was opened.
  
      @param  openDate Date that this item was opened.
    */
   public void setOpenDate (Date openDate) {
     this.openDate = openDate;
   }
 
   /**
      Returns date that this item was opened.
  
      @return Date that this item was opened.
    */
   public Date getOpenDate () {
     return openDate;
   }
   
   /**
      Sets date that this item was opened.
  
      @param  openDate Date that this item was opened.
    */
   public void setItemNumber (int itemNumber) {
     this.itemNumber = itemNumber;
   }
 
   /**
      Returns number of this item within a ToDoItems collection.
  
      @return Item Number of this to do item, or -1 if not yet identified. 
    */
   public int getItemNumber () {
     return itemNumber;
   }
   
   /**
      Sets the length of the associated file, in bytes.
  
      @param  fileLength The length of the associated file.
    */
   public void setFileLength (long fileLength) {
     this.fileLength = fileLength;
   }
   
   /**
      Sets the length of the associated file, in bytes.
  
      @param  fileLength The length of the associated file.
    */
   public void setFileLength (String fileLength) {
     this.fileLength = Long.parseLong(fileLength);
   }
 
   /**
      Returns the length of the associated file, in bytes.
  
      @return the length of the associated file, in bytes.
    */
   public long getFileLength () {
     return fileLength;
   }
   
   /**
      Returns the length of the associated file, in bytes.
  
      @return The length of the associated file, in bytes.
    */
   public String getFileLengthAsString () {
     return fileLengthFormatter.format(fileLength);
   }
   
   /**
      Sets the due date for this item.
  
      @param  fmt  A DateFormat instance to be used to parse the following string.
      @param  date String representation of a date.
    */
   public void setLastModDate (DateFormat fmt, String date) {
     
     try {
       setLastModDate (fmt.parse (date));
     } catch (ParseException e) {
     }
 
   } // end method
   
   /**
      Sets the due date for this item.
  
      @param  date Date representation of a date.
    */
   public void setLastModDate (Date date) {
     
     lastModDate = date;
 
   } // end method
   
   public boolean hasLastModDate() {
     return (! lastModDate.equals (DEFAULT_MOD_DATE.getTime()));
   }
   
   /**
      Gets the due date for this item, formatted as a string.
  
      @return  String representation of a date.
      @param   fmt  A DateFormat instance to be used to format the date as a string.
 
    */
   public String getLastModDate (DateFormat fmt) {
     
     if (lastModDate.equals (DEFAULT_MOD_DATE.getTime())) {
       return " ";
     } else {
       return fmt.format (lastModDate);
     }
 
   } // end method
   
   /**
      Gets the due date for this item, formatted as a string 
      in yyyy/mm/dd format.
  
      @return  String representation of a date in yyyy/mm/dd format.
    */
   public String getLastModDateYMD () {
     
     if (lastModDate.equals (DEFAULT_MOD_DATE.getTime())) {
       return " ";
     } else {
       return YMD_FORMAT.format (lastModDate);
     }
 
   } // end method
   
   /**
      Gets the due date for this item.
  
      @return  date Date representation of a date.
    */
   public Date getLastModDate () {
     
     return lastModDate;
 
   } // end method
   
   public void setSynced(boolean synced) {
     this.synced = synced;
   }
   
   public boolean wasSynced() {
     return synced;
   }
   
   /*
   public IDNumber getIDNumber () {
     return id;
   }
   
   public String getID () {
     return id.toString();
   }
   
   public void setID (String id) {
     this.id.setID (id);
   } // end method setID
   
   public int getIDComponentAsInt (int i) {
     return (id.getIDComponentAsInt (i));
   }
   
   public Integer getIDComponentAsInteger(int i) {
     return (id.getIDComponentAsInteger (i));
   }
   
   public int getIDComponentsSize() {
     return (id.getIDComponentsSize());
   }
   */
 	
 	/**
 	   Returns the object in string form.
 	  
 	   @return object formatted as a string
 	 */
 	public String toString() {
     return title;
 	}
   
 } // end of class
 
 
