 /**
   * @(#)TourHeaderGridScreen.
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
 import com.tourgeek.tour.booking.db.event.*;
 import com.tourgeek.tour.booking.db.*;
 
 /**
  *  TourHeaderGridScreen - Booking entry.
  */
 public class TourHeaderGridScreen extends GridScreen
 {
     /**
      * Default constructor.
      */
     public TourHeaderGridScreen()
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
     public TourHeaderGridScreen(Record record, ScreenLocation itsLocation, BasePanel parentScreen, Converter fieldConverter, int iDisplayFieldDesc, Map<String,Object> properties)
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
         return new TourHeader(this);
     }
     /**
      * Override this to open the other files in the query.
      */
     public void openOtherRecords()
     {
         Record record = this.getMainRecord();
         if (record instanceof ScreenRecord)
         {
             this.removeRecord(record);
             this.openMainRecord();
             this.addRecord(record, false);
             this.setScreenRecord(record);
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
         TourHeader recTourHeader = (TourHeader)this.getRecord(TourHeader.TOUR_HEADER_FILE);
         Record screenRecord = this.getScreenRecord();
         
         int iCommandID = (int)screenRecord.getField(LookupScreenRecord.BOOKING_LIST_FORMAT).getValue();
         if (((NumberField)screenRecord.getField(BookingScreenRecord.BK_SUB_SCREEN)).getValue() > 3)
             ((NumberField)screenRecord.getField(BookingScreenRecord.BK_SUB_SCREEN)).setValue(0);
         FieldListener behCheckRange = new CheckRangeHandler(0, 3);
         screenRecord.getField(BookingScreenRecord.BK_SUB_SCREEN).addListener(behCheckRange);
         recTourHeader.addListener(new FileRemoveBOnCloseHandler(behCheckRange));
         
         KeyArea tempKeyStart = recTourHeader.makeIndex(DBConstants.NOT_UNIQUE, null);  // Add temp key
         tempKeyStart.addKeyField(TourHeader.START_DATE, DBConstants.ASCENDING);
         KeyArea tempKeyEnd = recTourHeader.makeIndex(DBConstants.NOT_UNIQUE, null);
         tempKeyEnd.addKeyField(TourHeader.END_DATE, DBConstants.ASCENDING);
         SortOrderHandler behQueryKeyHandler = new SortOrderHandler(this);
         behQueryKeyHandler.setGridTable(TourHeader.DESCRIPTION, recTourHeader, 0);
         behQueryKeyHandler.setGridTable(tempKeyStart.getKeyName(), recTourHeader, 1);
         behQueryKeyHandler.setGridTable(tempKeyEnd.getKeyName(), recTourHeader, 2);
         behQueryKeyHandler.setGridTable(TourHeader.ID, recTourHeader, 3);
         screenRecord.getField(BookingScreenRecord.BK_SUB_SCREEN).addListener(behQueryKeyHandler);
         
         recTourHeader.addListener(new CompareFileFilter(TourHeader.BROCHURE_ID, screenRecord.getField(LookupScreenRecord.TOUR_HDR_BROCHURE_ID), "=", null, true));
         screenRecord.getField(LookupScreenRecord.TOUR_HDR_BROCHURE_ID).addListener(new FieldReSelectHandler(this));
         recTourHeader.addListener(new BitFileFilter(TourHeader.TOUR_TYPE, screenRecord.getField(LookupScreenRecord.TOUR_HDR_TOUR_TYPE)));
         screenRecord.getField(LookupScreenRecord.TOUR_HDR_TOUR_TYPE).addListener(new FieldReSelectHandler(this));
         recTourHeader.addListener(new CompareFileFilter(TourHeader.AIRLINE_ID, screenRecord.getField(LookupScreenRecord.TOUR_HDR_AIRLINE_CODE), "=", null, true));
         screenRecord.getField(LookupScreenRecord.TOUR_HDR_AIRLINE_CODE).addListener(new FieldReSelectHandler(this));
         recTourHeader.addListener(new CompareFileFilter(TourHeader.END_DATE, screenRecord.getField(LookupScreenRecord.TOUR_HDR_START_DATE), ">=", null, true));
         screenRecord.getField(LookupScreenRecord.TOUR_HDR_START_DATE).addListener(new FieldReSelectHandler(this));
         recTourHeader.addListener(new CompareFileFilter(TourHeader.START_DATE, screenRecord.getField(LookupScreenRecord.TOUR_HDR_END_DATE), "<=", null, true));
         screenRecord.getField(LookupScreenRecord.TOUR_HDR_END_DATE).addListener(new FieldReSelectHandler(this));
         this.getMainRecord().addListener(new StartSearchFilter(screenRecord.getField(LookupScreenRecord.START_TARGET_FIELD)));
         screenRecord.getField(LookupScreenRecord.START_TARGET_FIELD).addListener(new FieldReSelectHandler(this));
         FieldListener behInitOnChange = new InitOnChangeHandler(screenRecord.getField(LookupScreenRecord.START_TARGET_FIELD));
         screenRecord.getField(LookupScreenRecord.QUERY_KEY).addListener(behInitOnChange);
         recTourHeader.addListener(new FileRemoveBOnCloseHandler(behInitOnChange));
         
         this.getScreenRecord().getField(LookupScreenRecord.BK_DISPLAY_TYPE).setValue(DisplayTypeField.TOUR_HEADER_DISPLAY, DBConstants.DISPLAY, DBConstants.INIT_MOVE);
     }
     /**
      * Add the toolbars that belong with this screen.
      * @return The new toolbar.
      */
     public ToolScreen addToolbars()
     {
         ((LookupScreenRecord)this.getScreenRecord()).addStandardToolbar(this);  // I'm running stand-alone
         return ((LookupScreenRecord)this.getScreenRecord()).addTourHdrToolbar(this);
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
         Record query = this.getMainRecord();
         Converter converter = query.getField(TourHeader.TOUR_HEADER_FILE, TourHeader.DESCRIPTION);
         converter = new FieldLengthConverter(converter, 30);
         this.addColumn(converter);
         this.addColumn(query.getField(TourHeader.TOUR_HEADER_FILE, TourHeader.START_DATE));
         //? this.addColumn(query.getField(TourHeader.TOUR_HEADER_FILE, TourHeader.ID));
         this.addColumn(query.getField(TourHeader.TOUR_HEADER_FILE, TourHeader.END_DATE));
         this.addColumn(query.getField(TourHeader.TOUR_HEADER_FILE, TourHeader.DAYS));
         this.addColumn(query.getField(TourHeader.TOUR_HEADER_FILE, TourHeader.NIGHTS));
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
                 String strTourHeaderID = this.getMainRecord().getField(TourHeader.ID).toString();
                 strCommand = Utility.addURLParam(strCommand, "TourHeaderID", strTourHeaderID);
             }
         }
         return super.doCommand(strCommand, sourceSField, iCommandOptions);
     }
 
 }
