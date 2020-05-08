 /**
  * @(#)AddFinStmtDetailTotals.
  * Copyright © 2012 tourapp.com. All rights reserved.
  */
 package com.tourapp.tour.genled.finstmt;
 
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
 import com.tourapp.tour.genled.db.*;
 import org.jbundle.base.screen.model.report.*;
 import com.tourapp.tour.genled.db.finstmt.*;
 import com.tourapp.tour.genled.finstmt.screen.*;
 import org.jbundle.main.db.*;
 
 /**
  *  AddFinStmtDetailTotals - Add to the report totals.
  */
 public class AddFinStmtDetailTotals extends FileListener
 {
     public static final String BASE_RATIO = "base-ratio";
     public static final String CHANGE = "change";
     public static final String END = "end";
     public static final String IS_START = "isstart";
     public static final String RATIO = "ratio";
     public static final String START = "start";
     public static final String TRANSFER = "transfer";
     protected Map<String,Object> m_htTotals = new HashMap<String,Object>();
     protected String m_strLastReportName;
     /**
      * Default constructor.
      */
     public AddFinStmtDetailTotals()
     {
         super();
     }
     /**
      * AddFinStmtDetailTotals Method.
      */
     public AddFinStmtDetailTotals(Record record)
     {
         this();
         this.init(record);
     }
     /**
      * Initialize class fields.
      */
     public void init(Record record)
     {
         m_strLastReportName = "";
         super.init(record);
     }
     /**
      * Calculate the amounts needed for Financial Statements.
      */
     public void doValidRecord(boolean bDisplayOption)
     {
         super.doValidRecord(bDisplayOption);
         
         if (this.getScreenRecord().getField(FinStmtReportScreenRecord.LAST_STATEMENT).getValue() != this.getOwner().getField(FinStmtDetail.FIN_STMT_ID).getValue())
         {
             if (this.getScreenRecord().getField(FinStmtReportScreenRecord.LAST_STATEMENT).getValue() != 0)
             {   // Save the total from the last statement for subsequent uses.
                 m_htTotals.put(m_strLastReportName, new Double(this.getScreenRecord().getField(FinStmtReportScreenRecord.TARGET_AMOUNT).getValue()));
             }
             this.resetTotals(9);    // New statement = reset all totals
             this.getScreenRecord().getField(FinStmtReportScreenRecord.RATIO_AMOUNT).setValue(0);
             this.getScreenRecord().getField(FinStmtReportScreenRecord.RATIO_PERCENT).setValue(0);
             this.getScreenRecord().getField(FinStmtReportScreenRecord.LAST_STATEMENT).moveFieldToThis(this.getOwner().getField(FinStmtDetail.FIN_STMT_ID));
             m_strLastReportName = this.getRecord(FinStmt.FIN_STMT_FILE).getField(FinStmt.STATEMENT_DESC).toString();
         }
         
         double dStartBalance = this.getScreenRecord().getField(FinStmtReportScreenRecord.START_BALANCE).getValue();
         double dBalanceChange = this.getScreenRecord().getField(FinStmtReportScreenRecord.BALANCE_CHANGE).getValue();
         double dEndBalance = dStartBalance + dBalanceChange;
         this.getScreenRecord().getField(FinStmtReportScreenRecord.END_BALANCE).setValue(dEndBalance);
         if (this.getOwner().getField(FinStmtDetail.ACCOUNT_ID).getValue() != 0)
         {
             Record recAccount = ((ReferenceField)this.getOwner().getField(FinStmtDetail.ACCOUNT_ID)).getReference();
             if (recAccount != null)
             {
                 if (recAccount.getField(Account.CLOSE_YEAR_END).getState() == true)
                     this.getScreenRecord().getField(FinStmtReportScreenRecord.IS_AMOUNT).setValue(this.getScreenRecord().getField(FinStmtReportScreenRecord.IS_AMOUNT).getValue() + dStartBalance);
             }
         }
         
         int iLevel = (int)this.getOwner().getField(FinStmtDetail.SUB_TOTAL_LEVEL).getValue();
         double dTrxAmount = 0.00;
         if (StatementNumberField.NET_CHANGE.equalsIgnoreCase(this.getRecord(FinStmt.FIN_STMT_FILE).getField(FinStmt.STATEMENT_NUMBER).toString()))
             dTrxAmount = dBalanceChange;
         else
             dTrxAmount = dEndBalance;
         if (this.checkCommand(null))
         {
             if (this.checkCommand(START))
                 dTrxAmount = this.getScreenRecord().getField(FinStmtReportScreenRecord.START_BALANCE).getValue();
             if (this.checkCommand(END))
                 dTrxAmount = this.getScreenRecord().getField(FinStmtReportScreenRecord.END_BALANCE).getValue();
             if (this.checkCommand(CHANGE))
                 dTrxAmount = this.getScreenRecord().getField(FinStmtReportScreenRecord.BALANCE_CHANGE).getValue();
             if (this.checkCommand(TRANSFER))
                 dTrxAmount = this.getTransferAmount();
             if (this.checkCommand(IS_START))
                 dTrxAmount = this.getScreenRecord().getField(FinStmtReportScreenRecord.IS_AMOUNT).getValue();
         }
         this.addTotals(dTrxAmount);
         this.getScreenRecord().getField(FinStmtReportScreenRecord.TARGET_AMOUNT).setValue(this.getTotal(iLevel));
         
         if (StatementFormatField.RATIO.equalsIgnoreCase(this.getRecord(FinStmt.FIN_STMT_FILE).getField(FinStmt.STATEMENT_FORMAT).toString()))
         {
             if (this.checkCommand(BASE_RATIO))
                 this.getScreenRecord().getField(FinStmtReportScreenRecord.RATIO_AMOUNT).setValue(dTrxAmount);
             double dRatio = 0;
             double dRatioAmount = this.getScreenRecord().getField(FinStmtReportScreenRecord.RATIO_AMOUNT).getValue();
             if (dRatioAmount != 0)
             {
                 double dTargetAmount = this.getScreenRecord().getField(FinStmtReportScreenRecord.TARGET_AMOUNT).getValue();
                 dRatio = Math.abs(dTargetAmount / dRatioAmount);
             }
             this.getScreenRecord().getField(FinStmtReportScreenRecord.RATIO_PERCENT).setValue(dRatio);
         }
         
         if ((this.getRecord(FinStmtDetail.FIN_STMT_DETAIL_FILE).getField(FinStmtDetail.INVISIBLE).getState() == false)   // Invisible
             && ((this.getRecord(FinStmtDetail.FIN_STMT_DETAIL_FILE).getField(FinStmtDetail.DATA_COLUMN).getValue() != 0) || (this.getRecord(FinStmtDetail.FIN_STMT_DETAIL_FILE).getField(FinStmtDetail.DATA_COLUMN).isNull())))  // Number does not display
                 this.resetTotals(iLevel);
     }
     /**
      * Does this line contain this command?
      * @return true if it does.
      * @param strCommand The command to look for (if null, return true if these is any commmand).
      */
     public boolean checkCommand(String strCommand)
     {
         String strThisCommand = this.getRecord(FinStmtDetail.FIN_STMT_DETAIL_FILE).getField(FinStmtDetail.SPECIAL_FUNCTION).toString();
         if ((strThisCommand == null) || (strThisCommand.indexOf('@') == -1))
             return false;
         if (strCommand == null)
             return true;    // Yes this is a command
         strCommand = "@" + strCommand.toLowerCase();
         strThisCommand = strThisCommand.toLowerCase();
         return (strThisCommand.indexOf(strCommand) != -1);
     }
     /**
      * Get the total amount from this statement.
      * @return The total.
      */
     public double getTransferAmount()
     {
         String strThisCommand = this.getRecord(FinStmtDetail.FIN_STMT_DETAIL_FILE).getField(FinStmtDetail.SPECIAL_FUNCTION).toString();
         String strFinStmt = strThisCommand.substring(strThisCommand.toLowerCase().indexOf(TRANSFER) + TRANSFER.length());
         if (strFinStmt.startsWith(":"))
             strFinStmt = strFinStmt.substring(1);
         if (strFinStmt.startsWith(" "))
             strFinStmt = strFinStmt.substring(1);
         Double doubleAmount = (Double)m_htTotals.get(strFinStmt);
         if (doubleAmount == null)
             return 0.00;
         return doubleAmount.doubleValue();
     }
     /**
      * Get the total at this level.
      */
     public double getTotal(int iLevel)
     {
        return this.getScreenRecord().getField(this.getScreenRecord().getFieldSeq(FinStmtReportScreenRecord.TOTAL_0) + iLevel).getValue();
     }
     /**
      * Add this amount to all the level totals.
      */
     public void addTotals(double dAmount)
     {
         Record record = this.getScreenRecord();
         for (int iFieldSeq = 0; iFieldSeq < record.getFieldCount(); iFieldSeq++)
         {
             if (record.getField(iFieldSeq).getFieldName().startsWith("total"))
                 record.getField(iFieldSeq).setValue(dAmount + this.getScreenRecord().getField(iFieldSeq).getValue());
         }
     }
     /**
      * Reset the totals at this level and below.
      */
     public void resetTotals(int iMaxLevel)
     {
         Record record = this.getScreenRecord();
         for (int iFieldSeq = 0; iFieldSeq < record.getFieldCount(); iFieldSeq++)
         {
             if (record.getField(iFieldSeq).getFieldName().startsWith("total"))
                 record.getField(iFieldSeq).setValue(0.00);
         }
     }
     /**
      * A convience method to get the owner's recordowner's record.
      */
     public Record getRecord(String strRecordName)
     {
         return (Record)this.getOwner().getRecordOwner().getRecord(strRecordName);
     }
     /**
      * A convience method to get the screenrecord.
      */
     public Record getScreenRecord()
     {
         return (Record)this.getOwner().getRecordOwner().getScreenRecord();
     }
 
 }
