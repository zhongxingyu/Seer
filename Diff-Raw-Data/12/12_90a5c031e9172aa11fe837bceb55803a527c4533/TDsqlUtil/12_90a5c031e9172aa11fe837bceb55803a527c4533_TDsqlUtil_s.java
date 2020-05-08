 /**
  * 
  */
 package com.grimesco.gcocentral.td.dao;
 
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.Arrays;
 
 import com.grimesco.gcocentral.SqlUtil;
 import com.grimesco.translateSW.model.SWsymtranslate;
 import com.grimesco.translateTD.model.TDcostbasis;
 import com.grimesco.translateTD.model.TDposition;
 import com.grimesco.translateTD.model.TDprice;
 import com.grimesco.translateTD.model.TDreconciliation;
 import com.grimesco.translateTD.model.TDsymtranslate;
 import com.grimesco.translateTD.model.TDtransaction;
 
 /**
  * @author jaeboston
  *
  */
 public class TDsqlUtil {
 
 
 	public static PreparedStatement prepareStatementTDTransactionReady(PreparedStatement ps, TDtransaction transaction) throws SQLException {
 		
 	       	ps.setNString(	1, 	SqlUtil.returnString(transaction.getADVISOR_REP_CODE())); 
 	        ps.setDate(		2, 	transaction.getSQL_FILE_DATE());
 	        ps.setNString(	3, 	SqlUtil.returnString(transaction.getACCOUNT_NUMBER())); 
 	        ps.setNString(	4, 	SqlUtil.returnString(transaction.getTRANSACTION_CODE())); 
 	        ps.setNString(  5,  SqlUtil.returnStringChar(transaction.getCANCEL_STATUS_FLAG()));
 	        ps.setNString( 	6, 	SqlUtil.returnString(transaction.getSYMBOL()));
 	        ps.setNString(  7,  SqlUtil.returnString(transaction.getSECURITY_CODE()));
 	        ps.setDate(		8, 	transaction.getSQL_TRADE_DATE());
 	        ps.setDouble(   9,	SqlUtil.returnDouble(transaction.getQUANTITY()));
 	        ps.setDouble(   10, SqlUtil.returnDouble(transaction.getNET_AMOUNT())); 
 	    	ps.setDouble(   11, SqlUtil.returnDouble(transaction.getGROSS_AMOUNT()));
 	    	ps.setDouble(   12, SqlUtil.returnDouble(transaction.getBROKER_FEE()));
 	    	ps.setDouble(   13, SqlUtil.returnDouble(transaction.getOTHER_FEES()));
 	        
 	        if (transaction.getSETTLE_DATE()!= null & !Arrays.equals(transaction.getSETTLE_DATE(), new char[transaction.getSETTLE_DATE().length]))
 	        	ps.setDate(	14, 	transaction.getSQL_SETTLE_DATE());
 	        else //-- use TRADE_DATE instead according to the TD developer's Guide
 	        	ps.setDate(	14, 	transaction.getSQL_TRADE_DATE());
 	        
 	        ps.setNString(  15,  SqlUtil.returnString(transaction.getFROMTO_ACCOUNT()));
 	        ps.setNString(  16,  SqlUtil.returnString(transaction.getACCOUNT_TYPE()));
 	        ps.setDouble(    17,  SqlUtil.returnDouble(transaction.getACCRUED_INTEREST()));
 	        ps.setNString(  18,  SqlUtil.returnString(transaction.getCOMMENT()));
 	        ps.setNString(  19,  SqlUtil.returnString(transaction.getCLOSING_ACCOUNT_METHOD()));
 
 	        ps.setDate(		20, 	transaction.getSQL_SOURCE_DATE());
 
 	        return ps;
 
 	}
 	
 
 	public static PreparedStatement prepareStatementTDPriceReady(PreparedStatement ps, TDprice price) throws SQLException {
 
 		ps.setNString(	1, 	String.valueOf(price.getSYMBOL())); 
         ps.setNString(	2, 	String.valueOf(price.getSECURITY_TYPE_CODE())); 
         ps.setDate(		3, 	price.getSQL_PRICE_DATE());
     	ps.setDouble(    4,  SqlUtil.returnDouble(price.getCLOSE_PRICE()));
     	
     	if (price.getFACTOR_PAR()!= null) {
     		if  (!Arrays.equals(price.getFACTOR_PAR(), new char[ price.getFACTOR_PAR().length]) )
     			ps.setNString(  5,  String.valueOf(price.getFACTOR_PAR()));
     		else
     			ps.setNString(  5,  null);
     	} else {
         	ps.setNString(  5,  null);
     	}
 		
     	ps.setDate(		6, 	price.getSQL_SOURCE_DATE());
     	
     	
     	return ps;
 	}
 	
 
 	public static PreparedStatement prepareStatementTDPositionReady(PreparedStatement ps, TDposition position) throws SQLException {
 
 	   
 		
 		ps.setNString(	1, 	String.valueOf(position.getACCOUNT_NUMBER())); 
 		
 		if (position.getACCOUNT_TYPE()!= null) {
     	   if  (!Arrays.equals(position.getACCOUNT_TYPE(), new char[ position.getACCOUNT_TYPE().length]) )
     			ps.setNString(	2, 	String.valueOf(position.getACCOUNT_TYPE())); 
     		else
     			ps.setNString(	2, 	null); 
 		} else {
 			ps.setNString(	2, 	null); 			
 		}
        
 		ps.setNString(	3, 	String.valueOf(position.getSECURITY_CODE())); 
 		
 		ps.setNString(	4, 	String.valueOf(position.getSYMBOL())); 
         
 		if (position.getQUANTITY()!= null) {
     		if  (!Arrays.equals(position.getQUANTITY(), new char[ position.getQUANTITY().length]) )
     			ps.setDouble(    5,  SqlUtil.returnDouble(position.getQUANTITY()));
     		else
     			ps.setDouble(    5,  (double)0.0);
 		} else {
 			ps.setDouble(    5,  (double)0.0); 			
 		}
 		
 		if (position.getAMOUNT()!= null) {
     		if  (!Arrays.equals(position.getAMOUNT(), new char[ position.getAMOUNT().length]) )
     			ps.setDouble(    6,  SqlUtil.returnDouble(position.getAMOUNT()));
     		else
     			ps.setDouble(    6,  (double)0.0);
 		} else {
 			ps.setDouble(    6,  (double)0.0); 			
 		}
 		
         ps.setDate(		7,  position.getSQL_POSITION_DATE());
 
         
         if (position.getADVISOR_REP_CODE()!= null) {
      	   if  (!Arrays.equals(position.getADVISOR_REP_CODE(), new char[ position.getADVISOR_REP_CODE().length]) )
      			ps.setNString(	8, 	String.valueOf(position.getADVISOR_REP_CODE())); 
      		else
      			ps.setNString(	8, 	null); 
  		} else {
  			ps.setNString(	8, 	null); 			
  		}
     	return ps;
 	}
 	
 	
 	
 	
 	public static PreparedStatement prepareStatementTDReconciliationReady(PreparedStatement ps, TDreconciliation reconciliation) throws SQLException {
 		
 		ps.setNString(	1, 	String.valueOf(reconciliation.getCUSTODIAL_ID())); 
 		ps.setDate(		2, 	reconciliation.getSQL_BUSINESS_DATE());
 		ps.setNString(	3, 	String.valueOf(reconciliation.getACCOUNT_NUMBER())); 
 		
 		if (reconciliation.getACCOUNT_TYPE()!= null) {
     		if  (!Arrays.equals(reconciliation.getACCOUNT_TYPE(), new char[ reconciliation.getACCOUNT_TYPE().length]) )
     			ps.setNString(	4, 	String.valueOf(reconciliation.getACCOUNT_TYPE())); 
     		else
     			ps.setNString(	4, 	null); 
 		} else {
 			ps.setNString(	4, 	null); 			
 		}
 
         ps.setNString(	5, 	String.valueOf(reconciliation.getSECURITY_CODE())); 
 		ps.setNString(	6, 	String.valueOf(reconciliation.getSYMBOL())); 
 		ps.setDouble(    7,  SqlUtil.returnDouble(reconciliation.getCURRENT_QUANTITY()));
     	ps.setDouble(    8,  SqlUtil.returnDouble(reconciliation.getCOST_BASIS()));
     	
 		if (reconciliation.getADJUSTED_COST_BASIS()!= null) {
     		if  (!Arrays.equals(reconciliation.getADJUSTED_COST_BASIS(), new char[ reconciliation.getADJUSTED_COST_BASIS().length]) )
     			ps.setDouble(    9,  SqlUtil.returnDouble(reconciliation.getADJUSTED_COST_BASIS()));
     		else
     			ps.setDouble(    9,  (double)0.0);
 		} else {
 			ps.setDouble(    9,  (double)0.0); 			
 		}
 		
 		if (reconciliation.getUNREALIZED_GAINLOSS()!= null) {
     		if  (!Arrays.equals(reconciliation.getUNREALIZED_GAINLOSS(), new char[ reconciliation.getUNREALIZED_GAINLOSS().length]) )
     			ps.setDouble(    10,  SqlUtil.returnDouble(reconciliation.getUNREALIZED_GAINLOSS()));
     		else
     			ps.setDouble(    10,  (double)0.0);
 		} else {
 			ps.setDouble(    10,  (double)0.0); 			
 		}
 		
 		ps.setNString(  11,  String.valueOf(reconciliation.getCOST_BASIS_FULLY_KNOWN_FLAG()));
 		ps.setNString(  12,  String.valueOf(reconciliation.getCERTIFIED_FLAG()));
         ps.setDate(		13, 	reconciliation.getSQL_ORIGINAL_PURCHASE_DATE());
 		
         if (reconciliation.getORIGINAL_PURCHASE_PRICE()!= null) {
     		if  (!Arrays.equals(reconciliation.getORIGINAL_PURCHASE_PRICE(), new char[ reconciliation.getORIGINAL_PURCHASE_PRICE().length]) )
     			ps.setDouble(    14,  SqlUtil.returnDouble(reconciliation.getORIGINAL_PURCHASE_PRICE()));
     		else
     			ps.setDouble(    14,  (double)0.0);
 		} else {
 			ps.setDouble(    14,  (double)0.0); 			
 		}
         
         if (reconciliation.getWASH_SALE_FLAG()!= '\u0000')
     		ps.setNString(	15, 	String.valueOf(reconciliation.getWASH_SALE_FLAG())); 
         else
         	ps.setNString(	15, 	"-"); 
 
         
         if (reconciliation.getDISALLOWED_AMOUNT()!= null) {
     		if  (!Arrays.equals(reconciliation.getDISALLOWED_AMOUNT(), new char[ reconciliation.getDISALLOWED_AMOUNT().length]) )
     			ps.setDouble(    16,  SqlUtil.returnDouble(reconciliation.getDISALLOWED_AMOUNT()));
     		else
     			ps.setDouble(    16,  (double)0.0);
 		} else {
 			ps.setDouble(    16,  (double)0.0); 			
 		}
         
         if (reconciliation.getAVERAGED_COST_FLAG()!= '\u0000')
     		ps.setNString(	17, 	String.valueOf(reconciliation.getAVERAGED_COST_FLAG())); 
         else
         	ps.setNString(	17, 	"-"); 
         
         if (reconciliation.getBOOK_COST()!= null) {
     		if  (!Arrays.equals(reconciliation.getBOOK_COST(), new char[ reconciliation.getBOOK_COST().length]) )
     			ps.setDouble(    18,  SqlUtil.returnDouble(reconciliation.getBOOK_COST()));
     		else
     			ps.setDouble(    18,  (double)0.0);
 		} else {
 			ps.setDouble(    18,  (double)0.0); 			
 		}
         
         if (reconciliation.getBOOK_PROCEEDS()!= null) {
     		if  (!Arrays.equals(reconciliation.getBOOK_PROCEEDS(), new char[ reconciliation.getBOOK_PROCEEDS().length]) )
     			ps.setDouble(    19,  SqlUtil.returnDouble(reconciliation.getBOOK_PROCEEDS()));
     		else
     			ps.setDouble(    19,  (double)0.0);
 		} else {
 			ps.setDouble(    19,  (double)0.0); 			
 		}
         
         if (reconciliation.getFI_COST_ADJ()!= null) {
     		if  (!Arrays.equals(reconciliation.getFI_COST_ADJ(), new char[ reconciliation.getFI_COST_ADJ().length]) )
     			ps.setDouble(    20,  SqlUtil.returnDouble(reconciliation.getFI_COST_ADJ()));
     		else
     			ps.setDouble(    20,  (double)0.0);
 		} else {
 			ps.setDouble(    20,  (double)0.0); 			
 		}
         
         if (reconciliation.getTX_ID()!= null) {
     		if  (!Arrays.equals(reconciliation.getTX_ID(), new char[ reconciliation.getTX_ID().length]) )
     			ps.setNString(	21, 	String.valueOf(reconciliation.getTX_ID())); 
     		else
     			ps.setNString(	21, 	null); 
 		} else {
 			ps.setNString(	21, 	null); 			
 		}
 
 
         if (reconciliation.getSEC_NAME()!= null) {
     		if  (!Arrays.equals(reconciliation.getSEC_NAME(), new char[ reconciliation.getSEC_NAME().length]) )
     			ps.setNString(	22, 	String.valueOf(reconciliation.getSEC_NAME())); 
     		else
    			ps.setNString(	22, 	null); 
 		} else {
			ps.setNString(	22, 	null); 			
 		}
         
         if (reconciliation.getCOVERED_FLAG()!= '\u0000')
     		ps.setNString(	23, 	String.valueOf(reconciliation.getCOVERED_FLAG())); 
         else
         	ps.setNString(	23, 	"-"); 
         
         if (reconciliation.getUNKNOWN_TOTAL_FLAG()!= '\u0000')
     		ps.setNString(	24, 	String.valueOf(reconciliation.getUNKNOWN_TOTAL_FLAG())); 
         else
         	ps.setNString(	24, 	"-"); 
         
        	ps.setNString(	25, 	SqlUtil.returnString(reconciliation.getADVISOR_REP_CODE())); 
 
         return ps; 
 	
 	}
 	
 	public static PreparedStatement prepareStatementTDcostbasisReady(PreparedStatement ps, TDcostbasis costbasis) throws SQLException {
 
 		ps.setInt(		1, 	costbasis.getTransactionID()); 
 		ps.setByte( 	2,  costbasis.getFlag());
 		ps.setDouble(	3, 	costbasis.getAmount());
 		ps.setDate(     4, 	costbasis.getSQL_AUTORESOLVEDDATE());
 		ps.setDate(     5, 	costbasis.getSQL_MANRESOLVEDDATE());
 		ps.setByte( 	6,  costbasis.getExportFlag());
 		ps.setByte(		7, 	costbasis.getRejectFlag());
 		
     	return ps;
 	}
 	
 	public static PreparedStatement prepareStatementTDCostbasisTransactionReady(PreparedStatement ps, int _costbasisID, TDtransaction transaction) throws SQLException {
 		
 		ps.setInt(	1, 	_costbasisID); 
         ps.setNString(	2, 	SqlUtil.returnString(transaction.getADVISOR_REP_CODE())); 
         ps.setDate(		3, 	transaction.getSQL_FILE_DATE());
         ps.setNString(	4, 	SqlUtil.returnString(transaction.getACCOUNT_NUMBER())); 
         ps.setNString(	5, 	SqlUtil.returnString(transaction.getTRANSACTION_CODE())); 
         ps.setNString(  6,  SqlUtil.returnStringChar(transaction.getCANCEL_STATUS_FLAG()));
         ps.setNString( 7, 	SqlUtil.returnString(transaction.getSYMBOL()));
         ps.setNString(  8,  SqlUtil.returnString(transaction.getSECURITY_CODE()));
         ps.setDate(		9, 	transaction.getSQL_TRADE_DATE());
         ps.setDouble(    10,	SqlUtil.returnDouble(transaction.getQUANTITY()));
         ps.setDouble(    11, SqlUtil.returnDouble(transaction.getNET_AMOUNT())); 
     	ps.setDouble(    12, SqlUtil.returnDouble(transaction.getGROSS_AMOUNT()));
     	ps.setDouble(    13, SqlUtil.returnDouble(transaction.getBROKER_FEE()));
     	ps.setDouble(    14, SqlUtil.returnDouble(transaction.getOTHER_FEES()));
         
         if (transaction.getSETTLE_DATE()!= null & !Arrays.equals(transaction.getSETTLE_DATE(), new char[transaction.getSETTLE_DATE().length]))
         	ps.setDate(		15, 	transaction.getSQL_SETTLE_DATE());
         else //-- use TRADE_DATE instead according to the TD developer's Guide
         	ps.setDate(		15, 	transaction.getSQL_TRADE_DATE());
         
         ps.setNString(  16,  SqlUtil.returnString(transaction.getFROMTO_ACCOUNT()));
         ps.setNString(  17,  SqlUtil.returnString(transaction.getACCOUNT_TYPE()));
         ps.setDouble(    18,  SqlUtil.returnDouble(transaction.getACCRUED_INTEREST()));
         ps.setNString(  19,  SqlUtil.returnString(transaction.getCOMMENT()));
         ps.setNString(  20,  SqlUtil.returnString(transaction.getCLOSING_ACCOUNT_METHOD()));
 
         ps.setDate(		21, 	transaction.getSQL_SOURCE_DATE());
 
         return ps;
 
 	}
 	
 	public static PreparedStatement prepareStatementTDsymtranslateReady(PreparedStatement ps, TDsymtranslate _systranslate) throws SQLException {
 		
 		ps.setNString(	1, SqlUtil.getStringAfterChecktheField(_systranslate.getTD_SYMBOL()));         
 		ps.setNString(	2, SqlUtil.getStringAfterChecktheField(_systranslate.getSYMBOL()));         
 		
 		return ps;    
 	}
 	
 
 	
 }
