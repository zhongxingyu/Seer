 /**
  * @(#)VendorGridScreen.
  * Copyright © 2011 tourapp.com. All rights reserved.
  */
 package com.tourapp.tour.acctpay.screen.vendor;
 
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
 import com.tourapp.tour.acctpay.screen.trx.*;
 import com.tourapp.tour.acctpay.db.*;
 import com.tourapp.tour.base.db.*;
import org.jbundle.main.msg.wsdl.*;
 import org.jbundle.thin.base.message.*;
 import org.jbundle.thin.base.screen.*;
 import org.jbundle.base.message.core.trx.*;
 import org.jbundle.model.message.*;
 
 /**
  *  VendorGridScreen - Vendors.
  */
 public class VendorGridScreen extends GridScreen
 {
     /**
      * Default constructor.
      */
     public VendorGridScreen()
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
     public VendorGridScreen(Record record, ScreenLocation itsLocation, BasePanel parentScreen, Converter fieldConverter, int iDisplayFieldDesc, Map<String,Object> properties)
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
         return "Vendors";
     }
     /**
      * Override this to open the main file.
      * <p />You should pass this record owner to the new main file (ie., new MyNewTable(thisRecordOwner)).
      * @return The new record.
      */
     public Record openMainRecord()
     {
         return new Vendor(this);
     }
     /**
      * Add the screen fields.
      * Override this to create (and return) the screen record for this recordowner.
      * @return The screen record.
      */
     public Record addScreenRecord()
     {
         return new VendorScreenRecord(this);
     }
     /**
      * Add all the screen listeners.
      */
     public void addListeners()
     {
         super.addListeners();
         Vendor recVendor = (Vendor)this.getMainRecord();
         Record recScreenRecord = this.getScreenRecord();
         ((NumberField)recScreenRecord.getField(VendorScreenRecord.VENDOR_KEY)).setValue(0, DBConstants.DISPLAY, DBConstants.INIT_MOVE);
         recScreenRecord.getField(VendorScreenRecord.VENDOR_KEY).addListener(new RegisterValueHandler(null));
         this.setEditing(false);
         SortOrderHandler keyBehavior = new SortOrderHandler(this);
         keyBehavior.setGridTable(Vendor.NAME_SORT_KEY, recVendor, 0);
         keyBehavior.setGridTable(Vendor.CODE_KEY, recVendor, 1);
         recScreenRecord.getField(VendorScreenRecord.VENDOR_KEY).addListener(keyBehavior);
         
         recVendor.addListener(new ExtractRangeFilter(Vendor.NAME_SORT, recScreenRecord.getField(VendorScreenRecord.VENDOR_NAME), ExtractRangeFilter.PAD_END_FIELD));
         recVendor.addListener(new ExtractRangeFilter(Vendor.COUNTRY_ID, recScreenRecord.getField(VendorScreenRecord.VENDOR_COUNTRY), ExtractRangeFilter.PAD_END_FIELD));
         
         recScreenRecord.getField(VendorScreenRecord.VENDOR_NAME).addListener(new FieldReSelectHandler(this));
         recScreenRecord.getField(VendorScreenRecord.VENDOR_COUNTRY).addListener(new FieldReSelectHandler(this));
     }
     /**
      * Add button(s) to the toolbar.
      */
     public void addToolbarButtons(ToolScreen toolScreen)
     {
         new SCannedBox(toolScreen.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.SET_ANCHOR), toolScreen, null, ScreenConstants.DEFAULT_DISPLAY, null, MenuConstants.FORMDETAIL, MenuConstants.FORMDETAIL, MenuConstants.FORMDETAIL, null);
         
         this.getScreenRecord().getField(VendorScreenRecord.VENDOR_NAME).setupDefaultView(toolScreen.getNextLocation(ScreenConstants.RIGHT_WITH_DESC, ScreenConstants.SET_ANCHOR), toolScreen, ScreenConstants.DEFAULT_DISPLAY);
         this.getScreenRecord().getField(VendorScreenRecord.VENDOR_COUNTRY).setupDefaultView(toolScreen.getNextLocation(ScreenConstants.RIGHT_WITH_DESC, ScreenConstants.SET_ANCHOR), toolScreen, ScreenConstants.DEFAULT_DISPLAY);
     }
     /**
      * Add the navigation button(s) to the left of the grid row.
      */
     public void addNavButtons()
     {
         new SCannedBox(this.getNextLocation(ScreenConstants.FIRST_SCREEN_LOCATION, ScreenConstants.SET_ANCHOR), this, null, ScreenConstants.DEFAULT_DISPLAY, null, null, MenuConstants.FORMDETAIL, MenuConstants.FORMDETAIL, null);
         super.addNavButtons();  // Next buttons will be "First!"
     }
     /**
      * SetupSFields Method.
      */
     public void setupSFields()
     {
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.VENDOR_NAME).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.CODE).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.COUNTRY_ID).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.CURRENCYS_ID).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
     }
 
 }
