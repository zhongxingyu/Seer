 /**
  * @(#)BaseTrxPostScreen.
  * Copyright © 2012 tourapp.com. All rights reserved.
  */
 package com.tourapp.tour.assetdr.screen.batch;
 
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
 import com.tourapp.tour.genled.db.*;
 
 /**
  *  BaseTrxPostScreen - Base batch posting screen.
  */
 public class BaseTrxPostScreen extends Screen
 {
     /**
      * Default constructor.
      */
     public BaseTrxPostScreen()
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
     public BaseTrxPostScreen(Record record, ScreenLocation itsLocation, BasePanel parentScreen, Converter fieldConverter, int iDisplayFieldDesc, Map<String,Object> properties)
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
         return "Base batch posting screen";
     }
     /**
      * Add all the screen listeners.
      */
     public void addListeners()
     {
         super.addListeners();
         
        Record recTrxStatusRef = ((ReferenceField)this.getMainRecord().getField(Trx.TRX_STATUS_ID)).getReferenceRecord(); // Make sure this TrxStatus is different from the one I use for a key.
         this.removeRecord(recTrxStatusRef);
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
         if (strCommand.equalsIgnoreCase(MenuConstants.POST))
             return this.onPost();
         else
             return super.doCommand(strCommand, sourceSField, iCommandOptions);
     }
     /**
      * Post all the transactions in this batch.
      * @return true if successful.
      */
     public boolean onPost()
     {
         boolean bSuccess = true;
         Record recBaseTrxBatchDetail = this.getDetailRecord();
         try {
             if (!this.checkValidHeader())
                 return false;
             if (!this.updateBaseTrx())
                 return false;
             recBaseTrxBatchDetail.close();
             while (recBaseTrxBatchDetail.hasNext())
             {
                 recBaseTrxBatchDetail.next();
                 bSuccess = this.onPostTrx() & bSuccess;
                 if (!bSuccess)
                     break;
                 recBaseTrxBatchDetail.close();  // Since position may have changed
             }
             if (bSuccess)
                 bSuccess = this.removeTrxHeader();
         } catch (DBException ex)    {
             ex.printStackTrace();
         }
         return bSuccess;
     }
     /**
      * Get the batch detail record.
      */
     public Record getDetailRecord()
     {
         return null;    // Override
     }
     /**
      * Return the distribution detail record.
      * @return The dist record.
      */
     public Record getDistRecord()
     {
         return null;    // Override
     }
     /**
      * Get the base trx record.
      * @return The record.
      */
     public BaseTrx getBaseTrx()
     {
         return null;    // Override this
     }
     /**
      * Get the group ID for this detail transaction.
      * Typically you must override this method to supply the correct group ID.
      * @param recDetailTrx The current batch record.
      * @return The group trx id.
      */
     public int getTrxGroupID(Record recDetailTrx)
     {
         if (recDetailTrx instanceof Trx)
             return ((Trx)recDetailTrx).getTrxGroupID();
         return -1;
     }
     /**
      * Is the batch header record valid?
      * @return true if valid (if false, set the last error).
      */
     public boolean checkValidHeader()
     {
         try {
             BaseApplication application = (BaseApplication)this.getTask().getApplication();
             this.getMainRecord().setOpenMode(this.getMainRecord().getOpenMode() & ~DBConstants.OPEN_READ_ONLY); // Make sure not read only
             if (this.getMainRecord().edit() != DBConstants.NORMAL_RETURN)
             {
                 this.displayError(application.getResources(ResourceConstants.ASSETDR_RESOURCE, true).getString("Batch in use, can't post"));
                 return false;
             }
             if (this.getMainRecord().getEditMode() != Constants.EDIT_IN_PROGRESS)
             {   // Error - I need exclusive use of this record
                 this.displayError(application.getResources(ResourceConstants.ASSETDR_RESOURCE, true).getString("Not a valid batch, can't post"));
                 return false;
             }
         } catch (DBException ex)    {
             ex.printStackTrace();
             return false;
         }
         return true;    // Success
     }
     /**
      * Update the base trx to the new status.
      * @return true if successful.
      */
     public boolean updateBaseTrx()
     {
         return true;    // Default = success
     }
     /**
      * Setup and post this base transaction.
      * @param recBaseTrx The base transaction to post.
      * @param recTransactionType The transaction type for the TRX posting.
      * @return true If successful.
      */
     public boolean postBaseTrx(BaseTrx recBaseTrx, TransactionType recTransactionType)
     {
         return true;    // Override this!
     }
     /**
      * Post this detail transaction to the BaseTrx and to the G/L.
      * @return true If successful.
      */
     public boolean onPostTrx()
     {
         if (!this.checkValidDetail())
             return false;
         if (!this.updateDetailTrx())
             return false;
         if (!this.postDetailTrx())
             return false;
         return this.removeDetailTrx();  // Success
     }
     /**
      * Make sure this batch detail trx is valid.
      * @return True if batch is okay.
      */
     public boolean checkValidDetail()
     {
         BaseApplication application = (BaseApplication)this.getTask().getApplication();
         Record recBankTrxBatchDetail = this.getDetailRecord();
         try   {
             if (recBankTrxBatchDetail.getEditMode() == Constants.EDIT_CURRENT)
             {
                 if (recBankTrxBatchDetail.edit() != DBConstants.NORMAL_RETURN)
                 {
                     this.displayError(application.getResources(ResourceConstants.ASSETDR_RESOURCE, true).getString("Batch in use, can't post"));
                     return false;
                 }
             }
         } catch (DBException ex)    {
             ex.printStackTrace();
             return false;
         }
         return true;    // Success
     }
     /**
      * (Optionally) update this detail transaction.
      * @return true if success.
      */
     public boolean updateDetailTrx()
     {
         return true;    // Success (override to update the trx)
     }
     /**
      * Post this detail transaction to the BaseTrx and the G/L.
      * @return True if successful.
      */
     public boolean postDetailTrx()
     {
         BaseTrx recBaseTrx = this.getBaseTrx();
         recBaseTrx.startDistTrx();
         int iTrxGroupID = this.getTrxGroupID(this.getDetailRecord());
         TransactionType recTransactionType = recBaseTrx.getTransactionType();
         boolean bSuccess = this.postBaseTrx(recBaseTrx, recTransactionType.getTrxType(iTrxGroupID, PostingType.TRX_POST));
         if (!bSuccess)
             return bSuccess;
         bSuccess = this.postDistTrx(recBaseTrx, recTransactionType.getTrxType(iTrxGroupID, PostingType.DIST_POST));
         if (!bSuccess)
             return bSuccess;
         recBaseTrx.endDistTrx();
         return true;
     }
     /**
      * Post the distribution detail.
      * @param recBaseTrx The base transaction to post.
      * @param recTransactionType The transaction type for the DIST posting.
      * @return true If successful.
      */
     public boolean postDistTrx(BaseTrx recBaseTrx, TransactionType recTransactionType)
     {
         return true;
     }
     /**
      * Remove this batch detail transaction and the distribution.
      * @return true if successful.
      */
     public boolean removeDetailTrx()
     {
         // Step 3 - Delete the batch (if not recurring)
         Record recBankTrxBatchDetail = this.getDetailRecord();
         Record recBankTrxBatchDist = this.getDistRecord();
         recBankTrxBatchDist.close();
         try   {
             while (recBankTrxBatchDist.hasNext())
             {
                 recBankTrxBatchDist.next();
                 recBankTrxBatchDist.edit();
                 recBankTrxBatchDist.remove();
             }
             recBankTrxBatchDetail.remove();
             recBankTrxBatchDetail.addNew();
         } catch (DBException ex)    {
             ex.printStackTrace();
         }
         return true;
     }
     /**
      * Delete the batch header.
      */
     public boolean removeTrxHeader()
     {
         try {
             this.getMainRecord().remove();
         } catch (DBException ex) {
             ex.printStackTrace();
         }
         return true;
     }
 
 }
