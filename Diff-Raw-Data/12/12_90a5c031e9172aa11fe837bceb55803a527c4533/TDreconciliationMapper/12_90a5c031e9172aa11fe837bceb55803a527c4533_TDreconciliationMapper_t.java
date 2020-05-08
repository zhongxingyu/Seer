 /**
  * 
  */
 package com.grimesco.gcocentral.td.dao;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Arrays;
 
 import org.springframework.jdbc.core.RowMapper;
 
 import com.grimesco.translateTD.model.TDreconciliation;
 
 /**
  * @author jaeboston
  *
  */
 public class TDreconciliationMapper implements RowMapper<TDreconciliation> {
 
 	/* (non-Javadoc)
 	 * @see org.springframework.jdbc.core.RowMapper#mapRow(java.sql.ResultSet, int)
 	 */
 	@Override
 	 public TDreconciliation mapRow(ResultSet rs, int rowNum) throws SQLException { 
 		
 		TDreconciliation reconciliation = new TDreconciliation();
 		
 		reconciliation.setID( 							rs.getInt("ID")); 
 		reconciliation.setCUSTODIAL_ID(					rs.getString("CUSTODIAL_ID").toCharArray());
 		
 		
 		reconciliation.setBUSINESS_DATE(				rs.getDate("BUSINESS_DATE"));
 		
 		//System.out.println("BUSINESS_DATE = " + String.valueOf(rs.getDate("BUSINESS_DATE")));
 		
 		reconciliation.setACCOUNT_NUMBER(				rs.getString("ACCOUNT_NUMBER").toCharArray());
 		
 		if (rs.getString("ACCOUNT_TYPE")!= null) {
 			if (!Arrays.equals(rs.getString("ACCOUNT_TYPE").toCharArray(), new char[ reconciliation.getACCOUNT_TYPE().length  ]) )    
 				reconciliation.setACCOUNT_TYPE( 			rs.getString("ACCOUNT_TYPE").toCharArray()); 
 			else
 				reconciliation.setACCOUNT_TYPE(null);	
 		} else {
 			reconciliation.setACCOUNT_TYPE(null);				
 		}
 		
 		reconciliation.setSECURITY_CODE(				rs.getString("SECURITY_CODE").toCharArray());
 		
 		reconciliation.setSYMBOL( 						rs.getString("SYMBOL").toCharArray()); 
 		
 		
 		reconciliation.setCURRENT_QUANTITY(				rs.getString("CURRENT_QUANTITY").toCharArray());
 		
 		
 		reconciliation.setCOST_BASIS(					rs.getString("COST_BASIS").toCharArray());
 		
 		if (rs.getString("ADJUSTED_COST_BASIS")!= null) {
 			if (!Arrays.equals(rs.getString("ADJUSTED_COST_BASIS").toCharArray(), new char[ reconciliation.getADJUSTED_COST_BASIS().length  ]) )    
 				reconciliation.setADJUSTED_COST_BASIS( 			rs.getString("ADJUSTED_COST_BASIS").toCharArray()); 
 			else
 				reconciliation.setADJUSTED_COST_BASIS(null);	
 		} else {
 			reconciliation.setADJUSTED_COST_BASIS(null);				
 		}
 		
 		//System.out.println("ADJUSTED_COST_BASIS = " + String.valueOf(reconciliation.getADJUSTED_COST_BASIS()));
 		
 		
 		if (rs.getString("UNREALIZED_GAINLOSS")!= null) {
 			if (!Arrays.equals(rs.getString("UNREALIZED_GAINLOSS").toCharArray(), new char[ reconciliation.getUNREALIZED_GAINLOSS().length  ]) )    
 				reconciliation.setUNREALIZED_GAINLOSS( 			rs.getString("UNREALIZED_GAINLOSS").toCharArray()); 
 			else
 				reconciliation.setUNREALIZED_GAINLOSS(null);	
 		} else {
 			reconciliation.setUNREALIZED_GAINLOSS(null);				
 		}
 		
 		//System.out.println("UNREALIZED_GAINLOSS = " + String.valueOf(reconciliation.getUNREALIZED_GAINLOSS()));
 		
 		
 		reconciliation.setCOST_BASIS_FULLY_KNOWN_FLAG(			rs.getString("COST_BASIS_FULLY_KNOWN_FLAG").toCharArray()[0]);
 		reconciliation.setCERTIFIED_FLAG(						rs.getString("CERTIFIED_FLAG").toCharArray()[0]);
 		reconciliation.setORIGINAL_PURCHASE_DATE(				rs.getDate("ORIGINAL_PURCHASE_DATE"));
 		
 		
 //		if (rs.getString("ORIGINAL_PURCHASE_PRICE")!= null) {
 //			if (!Arrays.equals(reconciliation.getORIGINAL_PURCHASE_PRICE(), new char[ reconciliation.getORIGINAL_PURCHASE_PRICE().length  ]) )    
 //				reconciliation.setORIGINAL_PURCHASE_PRICE( 			rs.getString("ORIGINAL_PURCHASE_PRICE").toCharArray()); 
 //			else
 //				reconciliation.setORIGINAL_PURCHASE_PRICE(null);	
 //		} else {
 //			reconciliation.setORIGINAL_PURCHASE_PRICE(null);				
 //		}
 		reconciliation.setORIGINAL_PURCHASE_PRICE( 			String.valueOf(rs.getFloat("ORIGINAL_PURCHASE_PRICE")).toCharArray());
 		
 		if (rs.getString("WASH_SALE_FLAG")!= null) {	
 			reconciliation.setWASH_SALE_FLAG(			rs.getString("WASH_SALE_FLAG").charAt(0));
 		} else {
 			reconciliation.setWASH_SALE_FLAG('N');				
 		}
 	
 		reconciliation.setDISALLOWED_AMOUNT( 			String.valueOf(rs.getFloat("DISALLOWED_AMOUNT")).toCharArray());
 		reconciliation.setAVERAGED_COST_FLAG( 			rs.getString("AVERAGED_COST_FLAG").toCharArray()[0]);
 		reconciliation.setBOOK_COST(					String.valueOf(rs.getFloat("BOOK_COST")).toCharArray());
 		reconciliation.setBOOK_PROCEEDS(				String.valueOf(rs.getFloat("BOOK_PROCEEDS")).toCharArray());
 		reconciliation.setFI_COST_ADJ(					String.valueOf(rs.getFloat("FI_COST_ADJ")).toCharArray());
 		reconciliation.setTX_ID( 						rs.getString("TX_ID").toCharArray()); 
		reconciliation.setSEC_NAME(						rs.getString("SEC_NAME").toCharArray()); 
 		reconciliation.setCOVERED_FLAG( 				rs.getString("COVERED_FLAG").toCharArray()[0]);
 		reconciliation.setUNKNOWN_TOTAL_FLAG(			rs.getString("UNKNOWN_TOTAL_FLAG").toCharArray()[0]);
 			
 		
 		reconciliation.setADVISOR_REP_CODE( 			rs.getString("ADVISOR_REP_CODE").toCharArray()); 
 		
 		return reconciliation;
 	}
 
 }
