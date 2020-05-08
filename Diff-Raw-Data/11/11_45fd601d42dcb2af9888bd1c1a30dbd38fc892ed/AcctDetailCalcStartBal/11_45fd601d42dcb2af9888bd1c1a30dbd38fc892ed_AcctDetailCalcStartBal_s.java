 /**
  *  @(#)AcctDetailCalcStartBal.
  *  Copyright © 2010 tourapp.com. All rights reserved.
  */
 package com.tourapp.tour.genled.screen.detail;
 
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
 import org.jbundle.base.util.*;
 import org.jbundle.model.*;
 import org.jbundle.main.screen.*;
 import com.tourapp.tour.genled.db.*;
 
 /**
  *  AcctDetailCalcStartBal - Recalc the Start balance.
  */
 public class AcctDetailCalcStartBal extends FileListener
 {
     protected Record m_recAccount = null;
     protected Record m_recAcctDetail = null;
     protected Record m_recAcctDetailScreenRecord = null;
     /**
      * Default constructor.
      */
     public AcctDetailCalcStartBal()
     {
         super();
     }
     /**
      * Constructor.
      */
     public AcctDetailCalcStartBal(Record recAccount, Record recAcctDetailScreenRecord)
     {
         this();
         this.init(recAccount, recAcctDetailScreenRecord);
     }
     /**
      * Initialize class fields.
      */
     public void init(Record recAccount, Record recAcctDetailScreenRecord)
     {
         m_recAccount = null;
         m_recAcctDetail = null;
         m_recAcctDetailScreenRecord = null;
         m_recAccount = recAccount;
         m_recAcctDetailScreenRecord = recAcctDetailScreenRecord;
         super.init(null);
     }
     /**
      * Free Method.
      */
     public void free()
     {
        if (m_recAcctDetail != null)
            m_recAcctDetail.free();
         m_recAcctDetail = null;
         super.free();
     }
     /**
      * Called when a change is the record status is about to happen/has happened.
      * @param field If this file change is due to a field, this is the field.
      * @param iChangeType The type of change that occurred.
      * @param bDisplayOption If true, display any changes.
      * @return an error code.
      * ADD_TYPE - Before a write.
      * UPDATE_TYPE - Before an update.
      * DELETE_TYPE - Before a delete.
      * AFTER_UPDATE_TYPE - After a write or update.
      * LOCK_TYPE - Before a lock.
      * SELECT_TYPE - After a select.
      * DESELECT_TYPE - After a deselect.
      * MOVE_NEXT_TYPE - After a move.
      * AFTER_REQUERY_TYPE - Record opened.
      * SELECT_EOF_TYPE - EOF Hit.
      */
     public int doRecordChange(FieldInfo field, int iChangeType, boolean bDisplayOption)
     {
         int errorCode = super.doRecordChange(field, iChangeType, bDisplayOption); // Initialize the record
         if (errorCode != DBConstants.NORMAL_RETURN)
             return errorCode;
        if (m_recAcctDetailScreenRecord.getField(AcctDetailScreenRecord.kCalcStart).getState())
         {
            if (iChangeType == DBConstants.AFTER_REQUERY_TYPE)
             {
                 if (m_recAcctDetail == null)
                 {
                     m_recAcctDetail = new AcctDetail(Utility.getRecordOwner(this.getOwner()));
                     if (m_recAcctDetail.getRecordOwner() != null)
                         m_recAcctDetail.getRecordOwner().removeRecord(m_recAcctDetail);
                     m_recAcctDetail.addListener(new SubFileFilter(m_recAccount));
         
                     m_recAcctDetail.addListener(new CompareFileFilter(AcctDetail.kTrxDate, m_recAcctDetailScreenRecord.getField(AcctDetailScreenRecord.kStartDate), "<", null, false));
                     m_recAcctDetail.addListener(new SubCountHandler(m_recAcctDetailScreenRecord.getField(AcctDetailScreenRecord.kStartBalance), AcctDetail.kAmountLocal, false, true));   // Init this field override for other value
                 }
                 m_recAcctDetailScreenRecord.getField(AcctDetailScreenRecord.kStartBalance).initField(DBConstants.DISPLAY);
                 m_recAcctDetail.close();
                 try   {
                     while (m_recAcctDetail.hasNext())
                     {   // Go through and count
                         m_recAcctDetail.next();
                     }
                 } catch (DBException ex)    {
                     ex.printStackTrace();
                 }
             }
         }
         return errorCode;
     }
 
 }
