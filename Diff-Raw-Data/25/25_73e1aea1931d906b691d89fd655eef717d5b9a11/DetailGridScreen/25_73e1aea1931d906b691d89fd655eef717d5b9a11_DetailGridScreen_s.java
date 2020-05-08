 /**
  * @(#)DetailGridScreen.
  * Copyright © 2012 jbundle.org. All rights reserved.
  * GPL3 Open Source Software License.
  */
 package org.jbundle.base.screen.model;
 
 import java.util.Map;
 
 import org.jbundle.base.db.Record;
 import org.jbundle.base.db.filter.SubFileFilter;
 import org.jbundle.base.model.DBConstants;
 import org.jbundle.base.model.ScreenConstants;
 import org.jbundle.base.screen.model.util.ScreenLocation;
 import org.jbundle.thin.base.db.Converter;
 
 /**
  *  DetailGridScreen - .
  */
 public class DetailGridScreen extends GridScreen
 {
     protected Record m_recHeader = null;
     /**
      * Default constructor.
      */
     public DetailGridScreen()
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
     public DetailGridScreen(Record record, ScreenLocation itsLocation, BasePanel parentScreen, Converter fieldConverter, int iDisplayFieldDesc, Map<String,Object> properties)
     {
         this();
         this.init(record, itsLocation, parentScreen, fieldConverter, iDisplayFieldDesc, properties);
     }
     /**
      * Initialize class fields.
      */
     public void init(Record record, ScreenLocation itsLocation, BasePanel parentScreen, Converter fieldConverter, int iDisplayFieldDesc, Map<String,Object> properties)
     {
         m_recHeader = null;
         if ((iDisplayFieldDesc & ScreenConstants.DETAIL_MODE) == ScreenConstants.DETAIL_MODE)
         {
             m_recHeader = record;
             record = null;
         }
         super.init(record, itsLocation, parentScreen, fieldConverter, iDisplayFieldDesc, properties);
     }
     /**
      * DetailGridScreen Method.
      */
     public DetailGridScreen(Record recHeader, Record record, ScreenLocation itsLocation, BasePanel parentScreen, Converter fieldConverter, int iDisplayFieldDesc, Map<String,Object>
      properties)
     {
         this();
         this.init(recHeader, record, itsLocation, parentScreen, fieldConverter, iDisplayFieldDesc, properties);
     }
     /**
      * Initialize class fields.
      */
     public void init(Record recHeader, Record record, ScreenLocation itsLocation, BasePanel parentScreen, Converter fieldConverter, int iDisplayFieldDesc, Map<String,Object>
      properties)
     {
         m_recHeader = null;
         m_recHeader = recHeader;
         super.init(record, itsLocation, parentScreen, fieldConverter, iDisplayFieldDesc, properties);
     }
     /**
      * OpenHeaderRecord Method.
      */
     public Record openHeaderRecord()
     {
         return null;    // Override this!
     }
     /**
      * If there is a header record, return it, otherwise, return the main record.
      * The header record is the (optional) main record on gridscreens and is sometimes used
      * to enter data in a sub-record when a header is required.
      * @return The header record.
      */
     public Record getHeaderRecord()
     {
         return m_recHeader;
     }
     /**
      * Override this to open the other files in the query.
      */
     public void openOtherRecords()
     {
         super.openOtherRecords();
         if (m_recHeader != null)
             this.addRecord(m_recHeader, false);
         else
         {
            Record mainRecord = this.getMainRecord();
             m_recHeader = this.openHeaderRecord();
             if (mainRecord == m_recHeader)  // Passed in the header record as main
                 this.openMainRecord();
         }
     }
     /**
      * Add all the screen listeners.
      */
     public void addListeners()
     {
         super.addListeners();
         this.addSubFileFilter();
     }
     /**
      * AddSubFileFilter Method.
      */
     public void addSubFileFilter()
     {
         // Override this if it is not correct.
         SubFileFilter listener = null;
         this.getMainRecord().addListener(listener = new SubFileFilter(this.getHeaderRecord()));
         if (!this.getMainRecord().getKeyArea().getField(DBConstants.MAIN_KEY_FIELD).isNullable())
             listener.setFilterIfNull(true); // If the header record's key can't be null, don't display any detail if new record
     }
 
 }
