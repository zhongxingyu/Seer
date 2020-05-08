 /**
  * @(#)VendorScreen.
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
  *  VendorScreen - Vendors.
  */
 public class VendorScreen extends Screen
 {
     protected Integer m_intRegistryID = null;
     /**
      * Default constructor.
      */
     public VendorScreen()
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
     public VendorScreen(Record record, ScreenLocation itsLocation, BasePanel parentScreen, Converter fieldConverter, int iDisplayFieldDesc, Map<String,Object> properties)
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
      * Override this to open the other files in the query.
      */
     public void openOtherRecords()
     {
         super.openOtherRecords();
         new ApControl(this);
     }
     /**
      * Add all the screen listeners.
      */
     public void addListeners()
     {
         super.addListeners();
         
         ((Vendor)this.getMainRecord()).addPropertyListeners();
         
         this.addMainKeyBehavior();
         this.getMainRecord().getField(Vendor.COUNTRY_ID).addListener(new InitFieldHandler(this.getRecord(ApControl.AP_CONTROL_FILE).getField(ApControl.COUNTRY_ID)));
         this.getMainRecord().getField(Vendor.CURRENCYS_ID).addListener(new InitFieldHandler(this.getRecord(ApControl.AP_CONTROL_FILE).getField(ApControl.CURRENCYS_ID)));
         this.getMainRecord().getField(Vendor.MESSAGE_TRANSPORT_ID).addListener(new InitFieldHandler(this.getRecord(ApControl.AP_CONTROL_FILE).getField(ApControl.MESSAGE_TRANSPORT_ID)));
         this.getMainRecord().getField(Vendor.VENDOR_STATUS_ID).addListener(new InitFieldHandler(this.getRecord(ApControl.AP_CONTROL_FILE).getField(ApControl.VENDOR_STATUS_ID)));
         this.getMainRecord().getField(Vendor.PAYMENT_CYCLE_ID).addListener(new InitFieldHandler(this.getRecord(ApControl.AP_CONTROL_FILE).getField(ApControl.PAYMENT_CYCLE_ID)));
         this.getMainRecord().getField(Vendor.PAYMENT_CODE_ID).addListener(new InitFieldHandler(this.getRecord(ApControl.AP_CONTROL_FILE).getField(ApControl.PAYMENT_CODE_ID)));
         this.getMainRecord().getField(Vendor.PREPAY_TYPE_ID).addListener(new InitFieldHandler(this.getRecord(ApControl.AP_CONTROL_FILE).getField(ApControl.PREPAY_TYPE_ID)));
         this.getMainRecord().getField(Vendor.DEFAULT_ACCOUNT_ID).addListener(new InitFieldHandler(this.getRecord(ApControl.AP_CONTROL_FILE).getField(ApControl.COST_ACCOUNT_ID)));
         
         Record recCountry = ((ReferenceField)this.getMainRecord().getField(Vendor.COUNTRY_ID)).getReferenceRecord();
         this.getMainRecord().getField(Vendor.COUNTRY_ID).addListener(new MoveOnChangeHandler(this.getMainRecord().getField(Vendor.COUNTRY), recCountry.getField(Country.NAME), false,  true));
     }
     /**
      * Add button(s) to the toolbar.
      */
     public void addToolbarButtons(ToolScreen toolScreen)
     {
         new SCannedBox(toolScreen.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.SET_ANCHOR), toolScreen, null, ScreenConstants.DEFAULT_DISPLAY, null, MenuConstants.FORMDETAIL, MenuConstants.FORMDETAIL, MenuConstants.FORMDETAIL, null);
         new SCannedBox(toolScreen.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.SET_ANCHOR), toolScreen, null, ScreenConstants.DEFAULT_DISPLAY, null, Vendor.MESSAGE_DETAIL_SCREEN, MenuConstants.FORMDETAIL, Vendor.MESSAGE_DETAIL_SCREEN, null);
         new SCannedBox(toolScreen.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.SET_ANCHOR), toolScreen, null, ScreenConstants.DEFAULT_DISPLAY, null, Vendor.MESSAGE_LOG_SCREEN, MenuConstants.FORMDETAIL, Vendor.MESSAGE_LOG_SCREEN, null);
     }
     /**
      * Set up all the screen fields.
      */
     public void setupSFields()
     {
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.CODE).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.VENDOR_NAME).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.ADDRESS_LINE_1).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.ADDRESS_LINE_2).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.CITY_OR_TOWN).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.STATE_OR_REGION).setupDefaultView(this.getNextLocation(ScreenConstants.RIGHT_WITH_DESC, ScreenConstants.DONT_SET_ANCHOR), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.POSTAL_CODE).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.COUNTRY_ID).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.COUNTRY).setupDefaultView(this.getNextLocation(ScreenConstants.RIGHT_OF_LAST, ScreenConstants.DONT_SET_ANCHOR), this, ScreenConstants.DONT_DISPLAY_FIELD_DESC);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.TEL).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.FAX).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.EMAIL).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.WEB).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         FieldLengthConverter converter = new FieldLengthConverter(this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.MESSAGE_SERVER), 60);
         converter.setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         converter = new FieldLengthConverter(this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.WSDL_PATH), 60);
         converter.setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         BaseApplication application = (BaseApplication)this.getTask().getApplication();        
         String strRefresh = application.getResources(ResourceConstants.DEFAULT_RESOURCE, true).getString(MenuConstants.REFRESH);
         new SCannedBox(this.getNextLocation(ScreenConstants.RIGHT_OF_LAST, ScreenConstants.DONT_SET_ANCHOR), this, null, ScreenConstants.DONT_DISPLAY_DESC, null, null, MenuConstants.REFRESH, MenuConstants.REFRESH, strRefresh);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.MESSAGE_TRANSPORT_ID).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.CONTACT).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.CURRENCYS_ID).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         ScreenLocation secondColumn = this.getNextLocation(ScreenConstants.TOP_NEXT, ScreenConstants.DONT_SET_ANCHOR);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.VENDOR_STATUS_ID).setupDefaultView(this.getNextLocation(ScreenConstants.TOP_NEXT, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.PAYMENT_CYCLE_ID).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.PAYMENT_CODE_ID).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.PREPAY_TYPE_ID).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.DEPOSIT_ID).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.DEFAULT_ACCOUNT_ID).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.SEND_1099).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.TAX_ID).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.VENDORS_CUST_NO).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.OPERATION_TYPE_CODE).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.USER_ID).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         this.getRecord(Vendor.VENDOR_FILE).getField(Vendor.COMMENTS).setupDefaultView(this.getNextLocation(ScreenConstants.NEXT_LOGICAL, ScreenConstants.ANCHOR_DEFAULT), this, ScreenConstants.DEFAULT_DISPLAY);
         //?this.setAnchor(secondColumn);
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
         if (MenuConstants.REFRESH.equalsIgnoreCase(strCommand))
         {
             if ((this.getMainRecord().getEditMode() == DBConstants.EDIT_CURRENT) || (this.getMainRecord().getEditMode() == DBConstants.EDIT_IN_PROGRESS))
             {
                 if ((this.getMainRecord().getField(Vendor.WEB).isModified())
                     || (this.getMainRecord().getField(Vendor.PROPERTIES).isModified()))
                 {
                     try {
                         this.getMainRecord().writeAndRefresh();
                     } catch (DBException e) {
                         e.printStackTrace();
                     }
                 }
                 Map<String,Object> map = new Hashtable<String,Object>();
                 map.put(MenuConstants.RECORD, this.getMainRecord().getClass().getName());
                 try {
                     map.put(DBConstants.OBJECT_ID, this.getMainRecord().getHandle(DBConstants.OBJECT_ID_HANDLE).toString());
                 } catch (DBException e) {
                     e.printStackTrace();
                 }
                 
                map.put(DBParams.PROCESS, GetWSDL.class.getName());   // Default
                 Application app = (Application)this.getTask().getApplication();
                 String strQueueName = MessageConstants.TRX_SEND_QUEUE;
                 String strQueueType = MessageConstants.INTRANET_QUEUE;
             
                 if (m_intRegistryID == null)
                 {
                     MessageManager messageManager = app.getMessageManager();
                     if (messageManager != null)
                     {
                         Object source = this;
                         BaseMessageFilter filter = new BaseMessageFilter(MessageConstants.TRX_RETURN_QUEUE, MessageConstants.INTERNET_QUEUE, source, null);
                         filter.addMessageListener(this);
                         messageManager.addMessageFilter(filter);
                         m_intRegistryID = filter.getRegistryID();
                     }
                 }
                 if (m_intRegistryID != null)
                     map.put(TrxMessageHeader.REGISTRY_ID, m_intRegistryID);    // The return Queue ID
                 BaseMessage message = new MapMessage(new TrxMessageHeader(strQueueName, strQueueType, map), map);
                 app.getMessageManager().sendMessage(message);
                 
                 String strMessage = app.getResources(ResourceConstants.DEFAULT_RESOURCE, true).getString("Remote action queued");
                 this.getTask().setStatusText(strMessage, Cursor.WAIT_CURSOR);
             }
         }
         return super.doCommand(strCommand, sourceSField, iCommandOptions);
     }
     /**
      * A record with this datasource handle changed, notify any behaviors that are checking.
      * NOTE: Be very careful as this code is running in an independent thread
      * (synchronize to the task before calling record calls).
      * NOTE: For now, you are only notified of the main record changes.
      * @param message The message to handle.
      * @return The error code.
      */
     public int handleMessage(BaseMessage message)
     {
         if (message != null)
             if (message.getMessageHeader().getRegistryIDMatch() != null)    // My private message
                 if (message.getMessageHeader().getRegistryIDMatch().equals(m_intRegistryID))    // My private message
         {
             Application app = (Application)this.getTask().getApplication();
             String strMessage = app.getResources(ResourceConstants.DEFAULT_RESOURCE, true).getString("Remote action completed");
             this.getTask().setStatusText(strMessage, Constants.INFORMATION);
             message.consume();
         }
         return super.handleMessage(message);
     }
 
 }
