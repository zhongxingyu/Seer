 /**
  * @(#)RecordReportDetail.
  * Copyright © 2012 tourapp.com. All rights reserved.
  */
 package com.tourapp.tour.booking.report.itinerary;
 
 import java.awt.*;
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
 import org.jbundle.base.screen.model.report.*;
 
 /**
  *  RecordReportDetail - Move this up!.
  */
 public class RecordReportDetail extends HeadingScreen
 {
     /**
      * Default constructor.
      */
     public RecordReportDetail()
     {
         super();
     }
     /**
      * Constructor.
      * @param record The main record for this screen.
      * @param itsLocation The location of this component within the parent.
      * @param parentScreen The parent screen.
      * @param fieldConverter The field this screen field is linked to.
      * @param iDisplayFieldDesc Do I display the field desc?
      * @param properties Addition properties to pass to the screen.
      */
     public RecordReportDetail(Record record, ScreenLocation itsLocation, BasePanel parentScreen, Converter fieldConverter, int iDisplayFieldDesc, Map<String,Object> properties)
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
         return "Move this up!";
     }
     /**
      * AddDetailXMLColumn Method.
      */
     public Object addDetailXMLColumn(Record record, String iFieldSeq)
     {
         return this.addColumn(new MultipleTableFieldConverter(record, iFieldSeq));
     }
     /**
      * AddDetailXMLColumn Method.
      */
     public Object addDetailXMLColumn(Record record, String iFieldSeq, String iSecondFieldSeq)
     {
         return this.addColumn(new MultipleTableFieldConverter(record, iFieldSeq, iSecondFieldSeq));
     }
     /**
      * AddColumn Method.
      */
     public Object addColumn(Converter converter)
     {
         if (converter.getField() instanceof ReferenceField)
             return new SEditText(this.getNextLocation(ScreenConstants.NEXT_INPUT_LOCATION, ScreenConstants.SET_ANCHOR), this, converter, ScreenConstants.DEFAULT_DISPLAY);
         else
             return converter.setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.SET_ANCHOR), this, ScreenConstants.DEFAULT_DISPLAY);
     }
     /**
      * Display this sub-control in html input format?
      * @param iPrintOptions The view specific print options.
      * @return True if this sub-control is printable.
      */
     public boolean isPrintableControl(ScreenField sField, int iPrintOptions)
     {
         return true;    // Yep
     }
 
 }
