 /**
  * @(#)TrxIDSField.
  * Copyright © 2012 tourapp.com. All rights reserved.
  */
 package com.tourapp.tour.genled.screen.trx;
 
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
 import org.jbundle.base.screen.model.*;
 import org.jbundle.base.screen.model.util.*;
 import com.tourapp.tour.genled.db.*;
 
 /**
  *  TrxIDSField - Special Screen field to open the correct record from AcctDetailDist.
  */
 public class TrxIDSField extends SCannedBox
 {
     /**
      * Default constructor.
      */
     public TrxIDSField()
     {
         super();
     }
     /**
      * Constructor.
      * @param itsLocation The location of this component within the parent.
      * @param parentScreen The parent screen.
      * @param fieldConverter The field this screen field is linked to.
      * @param iDisplayFieldDesc Do I display the field desc?.
      */
     public TrxIDSField(ScreenLocation itsLocation, BasePanel parentScreen, Converter fieldConverter, int iDisplayFieldDesc)
     {
         this();
         this.init(itsLocation, parentScreen, fieldConverter, iDisplayFieldDesc);
     }
     /**
      * Initialize class fields.
      */
     public void init(ScreenLocation itsLocation, BasePanel parentScreen, Converter fieldConverter, int iDisplayFieldDesc)
     {
         if (fieldConverter != null)
         { // Make sure this field comes back on the query
             ((BaseField)fieldConverter.getField()).getRecord().getField(AcctDetailDist.ACCT_DETAIL_ID).setSelected(true);
         }
         String strDesc = AcctDetailDist.DIST_SOURCE;
         if (parentScreen instanceof GridScreen)
             strDesc = null;
         super.init(itsLocation, parentScreen, fieldConverter, iDisplayFieldDesc, null, strDesc, AcctDetailDist.DIST_SOURCE, AcctDetailDist.DIST_SOURCE, null, null, null);
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
         if ((strCommand != null) && (strCommand.equals(AcctDetailDist.DIST_SOURCE)))
         { // Pressed the MenuConstants.FORM button, display the SourceTrx
             BaseField field = (BaseField)sourceSField.getConverter().getField();
             if (field.isNull())
                 return true; // Command handled (and ignored)
             // First, get the transaction type
             Record recAcctDetailDist = field.getRecord();
             Record recAcctDetail = ((ReferenceField)recAcctDetailDist.getField(AcctDetailDist.ACCT_DETAIL_ID)).getReference();
             Record recTransactionType = ((ReferenceField)recAcctDetail.getField(AcctDetail.TRX_TYPE_ID)).getReference();
             if ((recTransactionType == null) || (recTransactionType.getEditMode() != Constants.EDIT_CURRENT))
                 return false;
             // Next, get the Record, make it current, and display the MaintScreen.
             String strRecordTarget = recTransactionType.getField(TransactionType.SOURCE_FILE).toString();
             if (strRecordTarget.length() == 0)
                 strRecordTarget = recTransactionType.getField(TransactionType.DESC_CODE).toString();
             if (strRecordTarget.length() == 0)
                 return true;
             if (strRecordTarget.indexOf('.') == -1)
                 strRecordTarget = ".tour." + recTransactionType.getField(TransactionType.SYSTEM_CODE).toString() + ".db." + strRecordTarget;
             RecordOwner recordOwner = null;
             if (this.getConverter() != null)
                 if (this.getConverter().getField() != null)
                     recordOwner = ((BaseField)this.getConverter().getField()).getRecord().findRecordOwner();
             Record recTarget = Record.makeRecordFromClassName(strRecordTarget, recordOwner);
             if (recordOwner != null)
                 recordOwner.removeRecord(recTarget);    // Will be owned by the new screen
             if (recTarget == null)
                 return true;    // Command handled
             try   {
                 Record record = recTarget.setHandle(field.getData(), DBConstants.BOOKMARK_HANDLE);
                 if ((record == null) || (recTarget.getEditMode() != DBConstants.EDIT_CURRENT))
                 {
                     recTarget.free();
                     return true;
                 }
             } catch (DBException ex)    {
                 ex.printStackTrace();
                 recTarget.free();
                 return true;
             }   
             BaseScreen screen = (BaseScreen)recAcctDetailDist.getRecordOwner();
             BasePanel parentScreen = screen.getParentScreen();
             ScreenLocation itsLocation = null;
             if ((iCommandOptions & ScreenConstants.USE_NEW_WINDOW) == ScreenConstants.USE_SAME_WINDOW)  // Use same window
             {
                 itsLocation = screen.getScreenLocation();
                 screen.free();
             }
             else
                 parentScreen = Screen.makeWindow(null);
             recTarget.makeScreen(itsLocation, parentScreen, ScreenConstants.MAINT_MODE, null);
             return true;
         }
         else
             return super.doCommand(strCommand, sourceSField, iCommandOptions);
     }
 
 }
