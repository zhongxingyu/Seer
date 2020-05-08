 /**
   * @(#)TourGridScreen.
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
 import com.tourgeek.tour.product.tour.db.*;
 import com.tourgeek.tour.booking.db.*;
 import com.tourgeek.tour.booking.inventory.db.*;
 import com.tourgeek.tour.booking.db.event.*;
 
 /**
  *  TourGridScreen - Booking entry.
  */
 public class TourGridScreen extends GridScreen
 {
     /**
      * Default constructor.
      */
     public TourGridScreen()
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
     public TourGridScreen(Record record, ScreenLocation itsLocation, BasePanel parentScreen, Converter fieldConverter, int iDisplayFieldDesc, Map<String,Object> properties)
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
         return new TourLookupQuery(this);
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
             if (Tour.TOUR_FILE.equalsIgnoreCase(record.getTableNames(false)))
             {
                 record.free();
                 mainRecord = this.openMainRecord();
             }
             if (mainRecord == null)
                 mainRecord = this.getRecord(TourLookupQuery.TOUR_LOOKUP_QUERY_FILE);
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
         
         Tour recTour = (Tour)this.getRecord(Tour.TOUR_FILE);
         TourHeader recTourHeader = (TourHeader)this.getRecord(TourHeader.TOUR_HEADER_FILE);
         Record screenQuery = this.getScreenRecord();
         
         if (((NumberField)screenQuery.getField(LookupScreenRecord.QUERY_KEY)).getValue() > 2)
             ((NumberField)screenQuery.getField(LookupScreenRecord.QUERY_KEY)).setValue(0);
         FieldListener behCheckRange = new CheckRangeHandler(0, 2);
         screenQuery.getField(LookupScreenRecord.QUERY_KEY).addListener(behCheckRange);
         recTour.addListener(new FileRemoveBOnCloseHandler(behCheckRange));
         
         SortOrderHandler behQueryKeyHandler = new SortOrderHandler(this);
         behQueryKeyHandler.setGridTable(Tour.DESCRIPTION_KEY, recTour, -1);
         behQueryKeyHandler.setGridTable(Tour.DEPARTURE_DATE_KEY, recTour, -1);
         screenQuery.getField(LookupScreenRecord.QUERY_KEY).addListener(behQueryKeyHandler);
         
         this.getMainRecord().addListener(new CompareFileFilter(recTour.getField(Tour.DEPARTURE_DATE), screenQuery.getField(LookupScreenRecord.TOUR_HDR_START_DATE), ">=", null, true));
         screenQuery.getField(LookupScreenRecord.TOUR_HDR_START_DATE).addListener(new FieldReSelectHandler(this));
         this.getMainRecord().addListener(new CompareFileFilter(recTour.getField(Tour.DEPARTURE_DATE), screenQuery.getField(LookupScreenRecord.TOUR_HDR_END_DATE), "<=", null, true));
         screenQuery.getField(LookupScreenRecord.TOUR_HDR_END_DATE).addListener(new FieldReSelectHandler(this));
         this.getMainRecord().addListener(new BitFileFilter(recTourHeader.getField(TourHeader.TOUR_TYPE), screenQuery.getField(LookupScreenRecord.TOUR_HDR_TOUR_TYPE)));
         screenQuery.getField(LookupScreenRecord.TOUR_HDR_TOUR_TYPE).addListener(new FieldReSelectHandler(this));
         this.getMainRecord().addListener(new StartSearchFilter(screenQuery.getField(LookupScreenRecord.START_TARGET_FIELD)));
         screenQuery.getField(LookupScreenRecord.START_TARGET_FIELD).addListener(new FieldReSelectHandler(this));
         FieldListener behInitOnChange = new InitOnChangeHandler(screenQuery.getField(LookupScreenRecord.START_TARGET_FIELD));
         screenQuery.getField(LookupScreenRecord.QUERY_KEY).addListener(behInitOnChange);
         recTour.addListener(new FileRemoveBOnCloseHandler(behInitOnChange));
         this.getMainRecord().addListener(new CompareFileFilter(recTour.getField(Tour.TOUR_HEADER_ID), screenQuery.getField(LookupScreenRecord.TOUR_HEADER_ID), "=", null, true));
         screenQuery.getField(LookupScreenRecord.TOUR_HEADER_ID).addListener(new FieldReSelectHandler(this));
         this.getMainRecord().addListener(new CompareFileFilter(recTour.getField(Tour.TOUR_STATUS_ID), screenQuery.getField(LookupScreenRecord.TOUR_STATUS_ID), "=", null, true));
         screenQuery.getField(LookupScreenRecord.TOUR_STATUS_ID).addListener(new FieldReSelectHandler(this));
         
         this.getScreenRecord().getField(LookupScreenRecord.BK_DISPLAY_TYPE).setValue(DisplayTypeField.TOUR_DISPLAY, DBConstants.DISPLAY, DBConstants.INIT_MOVE);
         
         this.setEditing(false);
     }
     /**
      * Read the current file in the header record given the current detail record.
      */
     public void syncHeaderToMain()
     {
         super.syncHeaderToMain();
         
         this.restoreScreenParam(LookupScreenRecord.TOUR_HDR_START_DATE);
         this.restoreScreenParam(LookupScreenRecord.TOUR_HDR_END_DATE);
         this.restoreScreenParam(LookupScreenRecord.TOUR_HDR_TOUR_TYPE);
         this.restoreScreenParam(LookupScreenRecord.TOUR_HEADER_ID);
         this.restoreScreenParam(LookupScreenRecord.TOUR_STATUS_ID);
     }
     /**
      * Add the toolbars that belong with this screen.
      * @return The new toolbar.
      */
     public ToolScreen addToolbars()
     {
         ((LookupScreenRecord)this.getScreenRecord()).addStandardToolbar(this);  // I'm running stand-alone
         return ((LookupScreenRecord)this.getScreenRecord()).addTourToolbar(this);
     }
     /**
      * Add the navigation button(s) to the left of the grid row.
      */
     public void addNavButtons()
     {
         //x new SCannedBox(this.getNextLocation(ScreenConstants.FIRST_SCREEN_LOCATION, ScreenConstants.SET_ANCHOR), this, null, ScreenConstants.DEFAULT_DISPLAY, null, null, Booking.BUTTON_LOCATION + "Booking", "Booking entry", null);
         super.addNavButtons();  // Next buttons will be "First!"
     }
     /**
      * SetupSFields Method.
      */
     public void setupSFields()
     {
         Converter converter = this.getMainRecord().getField(Tour.TOUR_FILE, Tour.DESCRIPTION);
         converter = new FieldLengthConverter(converter, 30);
         this.addColumn(converter);
         this.getRecord(Tour.TOUR_FILE).getField(Tour.DEPARTURE_DATE).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         converter = this.getRecord(TourHeader.TOUR_HEADER_FILE).getField(TourHeader.TOUR_HEADER_FILE, TourHeader.DESCRIPTION);
         converter = new FieldLengthConverter(converter, 30);
         converter = new FieldDescConverter(converter, "Header Tour Desc");
         this.addColumn(converter);
         this.getRecord(Inventory.INVENTORY_FILE).getField(Inventory.BLOCKED).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Inventory.INVENTORY_FILE).getField(Inventory.AVAILABLE).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Tour.TOUR_FILE).getField(Tour.TOUR_STATUS_ID).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
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
             if (strCommand.equalsIgnoreCase("Booking Entry"))
         {
             iCommandOptions = ScreenConstants.USE_NEW_WINDOW | ScreenConstants.DONT_PUSH_TO_BROWSER;     // Must open a new window
             strCommand = Utility.addURLParam(null, DBParams.APPLET, DBParams.BASE_APPLET);
            strCommand = Utility.addURLParam(strCommand, DBParams.SCREEN, com.tourgeek.thin.app.booking.entry.TourGeekScreen.class.getName());
             if ((this.getMainRecord().getEditMode() == DBConstants.EDIT_IN_PROGRESS) ||
                 (this.getMainRecord().getEditMode() == DBConstants.EDIT_CURRENT))
             {   // Add booking ID
                 String strTourID = this.getMainRecord().getField(Tour.ID).toString();
                 strCommand = Utility.addURLParam(strCommand, "TourID", strTourID);
             }
         }
         return super.doCommand(strCommand, sourceSField, iCommandOptions);
     }
 
 }
