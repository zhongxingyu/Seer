 /**
  * @(#)FullCurrencyField.
  * Copyright © 2012 tourapp.com. All rights reserved.
  */
 package com.tourapp.tour.base.db;
 
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
 import org.jbundle.base.model.*;
 import org.jbundle.base.util.*;
 import org.jbundle.model.*;
 import org.jbundle.model.db.*;
 import org.jbundle.model.screen.*;
 
 /**
  *  FullCurrencyField - This is the field to hold a foreign currency amount.
  */
 public class FullCurrencyField extends CurrencyField
 {
     /**
      * Default constructor.
      */
     public FullCurrencyField()
     {
         super();
     }
     /**
      * Constructor.
      * @param record The parent record.
      * @param strName The field name.
      * @param iDataLength The maximum string length (pass -1 for default).
      * @param strDesc The string description (usually pass null, to use the resource file desc).
      * @param strDefault The default value (if object, this value is the default value, if string, the string is the default).
      */
     public FullCurrencyField(Record record, String strName, int iDataLength, String strDesc, Object strDefault)
     {
         this();
         this.init(record, strName, iDataLength, strDesc, strDefault);
     }
     /**
      * Initialize class fields.
      */
     public void init(Record record, String strName, int iDataLength, String strDesc, Object strDefault)
     {
         super.init(record, strName, iDataLength, strDesc, strDefault);
     }
     /**
      * Set up the default screen control for this field.
      * @param itsLocation Location of this component on screen (ie., GridBagConstraint).
      * @param targetScreen Where to place this component (ie., Parent screen or GridBagLayout).
      * @param converter The converter to set the screenfield to.
      * @param iDisplayFieldDesc Display the label? (optional).
      * @param properties Extra properties
      * @return Return the component or ScreenField that is created for this field.
      */
     public ScreenComponent setupDefaultView(ScreenLoc itsLocation, ComponentParent targetScreen, Convert converter, int iDisplayFieldDesc, Map<String, Object> properties)
     {
         if (targetScreen != null)
         {
            Record recCurrencys = (Record)Utility.getRecordOwner(targetScreen).getRecord(Currencys.CURRENCYS_FILE);
             if (recCurrencys != null)
             {
                 BaseField fldCurrencyCode = recCurrencys.getField(Currencys.CURRENCY_CODE);
                 Converter conv = new FieldDescConverter(fldCurrencyCode, (Converter)converter);    // Use the description for this field
                 ScreenComponent sfCurrency = createScreenComponent(ScreenModel.EDIT_TEXT, itsLocation, targetScreen, conv, iDisplayFieldDesc, properties);
                 sfCurrency.setEnabled(false);
                 itsLocation = targetScreen.getNextLocation(ScreenConstants.RIGHT_OF_LAST, ScreenConstants.DONT_SET_ANCHOR);
                 iDisplayFieldDesc = ScreenConstants.DONT_DISPLAY_DESC;      // Display it only once
             }
         }
         return super.setupDefaultView(itsLocation, targetScreen, converter, iDisplayFieldDesc, properties);
     }
 
 }
