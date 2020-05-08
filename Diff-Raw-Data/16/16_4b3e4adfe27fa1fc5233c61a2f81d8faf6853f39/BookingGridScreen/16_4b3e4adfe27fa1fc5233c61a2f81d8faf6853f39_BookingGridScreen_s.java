 /**
   * @(#)BookingGridScreen.
   * Copyright © 2013 tourgeek.com. All rights reserved.
   * GPL3 Open Source Software License.
   */
 package com.tourgeek.tour.booking.lookup;
 
 import java.util.*;
 
 import org.jbundle.base.db.*;
 import org.jbundle.thin.base.util.*;
 import org.jbundle.thin.base.db.*;
 import org.jbundle.base.db.event.*;
 import org.jbundle.base.db.filter.*;
 import org.jbundle.base.field.*;
 import org.jbundle.base.field.convert.*;
 import org.jbundle.base.field.event.*;
 import org.jbundle.base.screen.model.*;
 import org.jbundle.base.screen.model.util.*;
 import org.jbundle.base.model.*;
 import org.jbundle.base.util.*;
 import org.jbundle.model.*;
 import org.jbundle.model.db.*;
 import org.jbundle.model.screen.*;
 import com.tourgeek.tour.booking.db.*;
 import com.tourgeek.tour.booking.db.event.*;
 
 /**
  *  BookingGridScreen - Booking entry.
  */
 public class BookingGridScreen extends GridScreen
 {
     public final static String BOOKING_ENTRY = "Booking Entry";
     /**
      * Default constructor.
      */
     public BookingGridScreen()
     {
         super();
     }
     /**
      * Constructor.
      * @param record The main record for this screen.
      * @param itsLocation The location of this component within the parent.
      * @param parentScreen The parent screen.
      * @param fieldConverter The field this screen field is linked to.
      * @param iDisplayFieldDesc Do I display the field desc?.
      */
     public BookingGridScreen(Record record, ScreenLocation itsLocation, BasePanel parentScreen, Converter fieldConverter, int iDisplayFieldDesc, Map<String,Object> properties)
     {
         this();
         this.init(record, itsLocation, parentScreen, fieldConverter, iDisplayFieldDesc, properties);
     }
     /**
      * Initialize class fields.
      */
     public void init(Record record, ScreenLocation itsLocation, BasePanel parentScreen, Converter fieldConverter, int iDisplayFieldDesc, Map<String,Object> properties)
     {
         super.init(record, itsLocation, parentScreen, fieldConverter, iDisplayFieldDesc, properties);
     }
     /**
      * Get the screen display title.
      */
     public String getTitle()
     {
         return "Booking entry";
     }
     /**
      * Override this to open the main file.
      * <p />You should pass this record owner to the new main file (ie., new MyNewTable(thisRecordOwner)).
      * @return The new record.
      */
     public Record openMainRecord()
     {
         return new BookingLookupQuery(this);
     }
     /**
      * Override this to open the other files in the query.
      */
     public void openOtherRecords()
     {
         Record record = this.getMainRecord();
         if (record != null)
         {
             Record mainRecord = null;
             if (record instanceof ScreenRecord)
             {
                 mainRecord = this.openMainRecord();
                 this.setScreenRecord(record);
             }
             if (Booking.BOOKING_FILE.equalsIgnoreCase(record.getTableNames(false)))
             {
                 record.free();
                 mainRecord = this.openMainRecord();
             }
             if (mainRecord == null)
                 mainRecord = this.getRecord(BookingLookupQuery.BOOKING_LOOKUP_QUERY_FILE);
             this.addRecord(mainRecord, true);    // Make sure this is the main record
         }
         super.openOtherRecords();
     }
     /**
      * Add the screen fields.
      * Override this to create (and return) the screen record for this recordowner.
      * @return The screen record.
      */
     public Record addScreenRecord()
     {
         if (!(this.getScreenRecord() instanceof LookupScreenRecord))
             return new LookupScreenRecord(this);
         else
             return this.getScreenRecord();
     }
     /**
      * Add all the screen listeners.
      */
     public void addListeners()
     {
         super.addListeners();
         this.setEditing(false);
         Booking recBooking = (Booking)this.getRecord(Booking.BOOKING_FILE);
         BookingPax recBookingPax = (BookingPax)this.getRecord(BookingPax.BOOKING_PAX_FILE);
         Tour recTour = (Tour)this.getRecord(Tour.TOUR_FILE);
         Record screenRecord = this.getScreenRecord();
         
         int iCommandID = (int)screenRecord.getField(LookupScreenRecord.BOOKING_LIST_FORMAT).getValue();
         if (((NumberField)screenRecord.getField(LookupScreenRecord.QUERY_KEY)).getValue() > 5)
             ((NumberField)screenRecord.getField(LookupScreenRecord.QUERY_KEY)).setValue(0);
         FieldListener behCheckRange = new CheckRangeHandler(-5, 5);
         screenRecord.getField(LookupScreenRecord.QUERY_KEY).addListener(behCheckRange);
         recBooking.addListener(new FileRemoveBOnCloseHandler(behCheckRange));
         
         KeyArea tempKey = recBooking.makeIndex(DBConstants.NOT_UNIQUE, null); // Add temp key
         tempKey.addKeyField(Booking.GENERIC_NAME, DBConstants.ASCENDING);        // by Agency name
         SortOrderHandler behQueryKeyHandler = new SortOrderHandler(this);
         behQueryKeyHandler.setGridTable(Tour.DESCRIPTION_KEY, recTour, -1);
         behQueryKeyHandler.setGridTable(Tour.DEPARTURE_DATE_KEY, recTour, -1);
         behQueryKeyHandler.setGridTable(BookingPax.SUR_NAME_KEY, recBookingPax, -1);
         behQueryKeyHandler.setGridTable(Booking.BOOKING_DATE_KEY, recBooking, -1);
         behQueryKeyHandler.setGridTable(Booking.MOD_DATE_KEY, recBooking, -1);
         behQueryKeyHandler.setGridTable(tempKey.getKeyName(), recBooking, -1);
         screenRecord.getField(LookupScreenRecord.QUERY_KEY).addListener(behQueryKeyHandler);
         
         this.getMainRecord().addListener(new CompareFileFilter(recBooking.getField(Booking.EMPLOYEE_ID), screenRecord.getField(LookupScreenRecord.CURRENT_AGENT), "=", null, true));
         screenRecord.getField(LookupScreenRecord.CURRENT_AGENT).addListener(new FieldReSelectHandler(this));
         this.getMainRecord().addListener(new CompareFileFilter(recBooking.getField(Booking.BOOKING_DATE), screenRecord.getField(LookupScreenRecord.START_BK_DATE), ">=", null, true));
         screenRecord.getField(LookupScreenRecord.START_BK_DATE).addListener(new FieldReSelectHandler(this));
         this.getMainRecord().addListener(new CompareFileFilter(recTour.getField(Tour.DEPARTURE_DATE), screenRecord.getField(LookupScreenRecord.TOUR_HDR_START_DATE), ">=", null, true));
         screenRecord.getField(LookupScreenRecord.TOUR_HDR_START_DATE).addListener(new FieldReSelectHandler(this));
         this.getMainRecord().addListener(new CompareFileFilter(recTour.getField(Tour.DEPARTURE_DATE), screenRecord.getField(LookupScreenRecord.TOUR_HDR_END_DATE), "<=", null, true));
         screenRecord.getField(LookupScreenRecord.TOUR_HDR_END_DATE).addListener(new FieldReSelectHandler(this));
         this.getMainRecord().addListener(new StartSearchFilter(screenRecord.getField(LookupScreenRecord.START_TARGET_FIELD)));
         screenRecord.getField(LookupScreenRecord.START_TARGET_FIELD).addListener(new FieldReSelectHandler(this));
         FieldListener behInitOnChange = new InitOnChangeHandler(screenRecord.getField(LookupScreenRecord.START_TARGET_FIELD));
         screenRecord.getField(LookupScreenRecord.QUERY_KEY).addListener(behInitOnChange);
         recBooking.addListener(new FileRemoveBOnCloseHandler(behInitOnChange));
         
         this.getMainRecord().addListener(new CompareFileFilter(Booking.BOOKING_STATUS_ID, screenRecord.getField(LookupScreenRecord.BOOKING_STATUS_ID), "=", null, true));
         screenRecord.getField(LookupScreenRecord.BOOKING_STATUS_ID).addListener(new FieldReSelectHandler(this));
         this.getMainRecord().addListener(new CompareFileFilter(recTour.getField(Tour.TOUR_STATUS_ID), screenRecord.getField(LookupScreenRecord.TOUR_STATUS_ID), "=", null, true));
         screenRecord.getField(LookupScreenRecord.TOUR_STATUS_ID).addListener(new FieldReSelectHandler(this));
         
         this.getScreenRecord().getField(LookupScreenRecord.BK_DISPLAY_TYPE).setValue(DisplayTypeField.BOOKING_DISPLAY, DBConstants.DISPLAY, DBConstants.INIT_MOVE);
     }
     /**
      * Add the toolbars that belong with this screen.
      * @return The new toolbar.
      */
     public ToolScreen addToolbars()
     {
         ((LookupScreenRecord)this.getScreenRecord()).addStandardToolbar(this);  // I'm running stand-alone
         return ((LookupScreenRecord)this.getScreenRecord()).addBookingToolbar(this);
     }
     /**
      * Add the navigation button(s) to the left of the grid row.
      */
     public void addNavButtons()
     {
         BaseApplication application = (BaseApplication)this.getTask().getApplication();
         //x new SCannedBox(this.getNextLocation(ScreenConstants.FIRST_SCREEN_LOCATION, ScreenConstants.SET_ANCHOR), this, null, ScreenConstants.DEFAULT_DISPLAY, null, null, Booking.BUTTON_LOCATION + "Booking", BOOKING_ENTRY, application.getResources(ResourceConstants.BOOKING_RESOURCE, true).getString(BOOKING_ENTRY));
         super.addNavButtons();  // Next buttons will be "First!"
     }
     /**
      * Add button(s) to the toolbar.
      */
     public void addToolbarButtons(ToolScreen toolScreen)
     {
         BaseApplication application = (BaseApplication)this.getTask().getApplication();
         new SCannedBox(toolScreen.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.SET_ANCHOR), toolScreen, null, ScreenConstants.DEFAULT_DISPLAY, null, application.getResources(ResourceConstants.BOOKING_RESOURCE, true).getString(BOOKING_ENTRY), Booking.BUTTON_LOCATION + "Booking", BOOKING_ENTRY, null);
     }
     /**
      * SetupSFields Method.
      */
     public void setupSFields()
     {
         Record query = this.getMainRecord();
         
         Converter converter = query.getField(Tour.TOUR_FILE, Tour.DESCRIPTION);
         converter = new FieldLengthConverter(converter, 30);
         this.addColumn(converter);
         this.addColumn(query.getField(Tour.TOUR_FILE, Tour.DEPARTURE_DATE));
         converter = query.getField(Tour.TOUR_FILE, Tour.TOUR_STATUS_ID);
         this.addColumn(converter);
         BookingPax recBookingPax = (BookingPax)this.getRecord(BookingPax.BOOKING_PAX_FILE);
         converter = new FirstMLastConverter(recBookingPax, BookingPax.NAME_PREFIX, BookingPax.FIRST_NAME, BookingPax.MIDDLE_NAME, BookingPax.SUR_NAME);
         converter = new FieldDescConverter(converter, "Passenger Name");
         this.addColumn(converter);
         converter = new DateConverter(query.getField(Booking.BOOKING_FILE, Booking.BOOKING_DATE), DBConstants.DATE_FORMAT);
         this.addColumn(converter);
         converter = query.getField(Booking.BOOKING_FILE, Booking.BOOKING_STATUS_ID);
         this.addColumn(converter);
         converter = new DateConverter(query.getField(Booking.BOOKING_FILE, Booking.MOD_DATE), DBConstants.DATE_FORMAT);
         this.addColumn(converter);
         this.addColumn(query.getField(Booking.BOOKING_FILE, Booking.GENERIC_NAME));
     }
     /**
      * Process the command.
      * <br />Step 1 - Process the command if possible and return true if processed.
      * <br />Step 2 - If I can't process, pass to all children (with me as the source).
      * <br />Step 3 - If children didn't process, pass to parent (with me as the source).
      * <br />Note: Never pass to a parent or child that matches the source (to avoid an endless loop).
      * @param strCommand The command to process.
      * @param sourceSField The source screen field (to avoid echos).
      * @param iCommandOptions If this command creates a new screen, create in a new window?
      * @return true if success.
      */
     public boolean doCommand(String strCommand, ScreenField sourceSField, int iCommandOptions)
     {
         if (strCommand != null)
         {
             if (strCommand.equalsIgnoreCase(BOOKING_ENTRY))
             {
                 iCommandOptions = ScreenConstants.USE_NEW_WINDOW | ScreenConstants.DONT_PUSH_TO_BROWSER;     // Must open a new window
                 strCommand = Utility.addURLParam(null, DBParams.APPLET, DBParams.BASE_APPLET);
                strCommand = Utility.addURLParam(strCommand, DBParams.SCREEN, com.tourgeek.thin.app.booking.entry.tourgeekScreen.class.getName());
                 if ((this.getMainRecord().getEditMode() == DBConstants.EDIT_IN_PROGRESS) ||
                     (this.getMainRecord().getEditMode() == DBConstants.EDIT_CURRENT))
                 {   // Add booking ID
                     String strBookingID = this.getMainRecord().getField(Booking.ID).toString();
                     strCommand = Utility.addURLParam(strCommand, DBConstants.OBJECT_ID, strBookingID);
                 }
             }
             else if ((strCommand.equalsIgnoreCase(MenuConstants.FORMLINK))
                     || (strCommand.equalsIgnoreCase(MenuConstants.FORM)))
             {
                 try   {
                     int iSelection = this.getScreenFieldView().getSelectedRow();
                     if (iSelection != -1)
                     {
                         Record recAtTarget = (Record)((GridTable)this.getMainRecord().getTable()).get(iSelection);
                         if (recAtTarget != null)
                             if (recAtTarget.getHandle(DBConstants.BOOKMARK_HANDLE) == null)
                         {   // Special weird case - booking, but no pax, show booking (std logic will not read booking)
                             Object bookmark = recAtTarget.getBaseRecord().getHandle(DBConstants.BOOKMARK_HANDLE);
                             recAtTarget.getBaseRecord().setHandle(bookmark, DBConstants.BOOKMARK_HANDLE);
                             this.addRecord(recAtTarget.getBaseRecord(), false);
                             return (this.onForm(recAtTarget.getBaseRecord(), ScreenConstants.MAINT_MODE, true, iCommandOptions, null) != null);
                         }
                     }
                 } catch (DBException ex)    {
                     ex.printStackTrace();
                 }
             }
         }
         return super.doCommand(strCommand, sourceSField, iCommandOptions);
     }
 
 }
