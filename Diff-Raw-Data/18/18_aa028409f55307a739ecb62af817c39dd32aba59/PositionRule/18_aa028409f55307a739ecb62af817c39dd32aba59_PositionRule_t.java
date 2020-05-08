 /**
  * 
  */
 package com.grimesco.gcodataport.td;
 
 import java.util.ArrayList;
 
 import com.grimesco.gcocentral.SqlUtil;
 import com.grimesco.gcocore.act.Account;
 import com.grimesco.gcocore.axys.model.AxysBlotter;
 import com.grimesco.gcocore.axys.model.AxysSymbol;
 import com.grimesco.translateTD.model.TDposition;
 import com.grimesco.translateTD.model.TDprice;
 import com.grimesco.translateTD.model.TDtransaction;
 
 /**
  * @author jaeboston
  *
  */
 public class PositionRule extends A_TDBlotterPositionRule {
 
 	//-- constructor
 	public PositionRule(Account _account, TDposition _tdpos, AxysSymbol _asymbol, TDprice _tdprice) {
 	
 		super(_account, _tdpos, _asymbol, _tdprice);
 		
 		//===================================================================================
 		//-- Blotter Col #2 set Transaction Code
 		txBlotter.setTRANSACTION_CODE("li".toCharArray());
 		//===================================================================================
 		
 		//===================================================================================
 		//-- Blotter Col #4 set Security Type
 		//===================================================================================
 		txBlotter.setSECURITY_TYPE(asymbol.getTYPE());
 		//===================================================================================
 	
 		//===================================================================================
 		//-- Blotter Col #5 set Security Symbol
 		//===================================================================================
 		//-- ??? consider using type MF
 		
 		if (String.valueOf(tdpos.getSYMBOL()).trim().toUpperCase().equals("CASH") || String.valueOf(asymbol.getTYPE()).toUpperCase().equals("MMUS") || String.valueOf(asymbol.getTYPE()).toUpperCase().equals("MEUS") ) {
 			txBlotter.setSECURITY_SYMBOL(tdprice.getSYMBOL());
 		} else {
 			txBlotter.setSECURITY_SYMBOL(asymbol.getSYMBOL());
 		}
 		//===================================================================================
 	
 		//===================================================================================
 		//-- Blotter Col #6 set Trade Date -- should be the working date
 		//-- see manResolvetButton.addListener(new Button.ClickListener()  in CostbasisWindow.java
 		//===================================================================================
 		txBlotter.setSQL_TRADE_DATE(tdpos.getSQL_POSITION_DATE());	
 		//===================================================================================
 	
 		//===================================================================================
 		//-- Blotter Col #9 set Quantity
 		//===================================================================================		
 		if (String.valueOf(asymbol.getTYPE()).toUpperCase().equals("MMUS") || String.valueOf(asymbol.getTYPE()).toUpperCase().equals("MEUS")  ) {
 			txBlotter.setQUANTITY(tdpos.getAMOUNT());
 		} else if (String.valueOf(tdpos.getSYMBOL()).trim().toUpperCase().equals("CASH") ) {
 			//-- do nothing
 		} else {
 			txBlotter.setQUANTITY(tdpos.getQUANTITY());
 		}
 		//===================================================================================
 	
 		//===================================================================================
 		//-- Blotter Col #18 set Trade Amount
 		//===================================================================================
 		if (String.valueOf(tdpos.getSYMBOL()).trim().toUpperCase().equals("CASH") ) {
 			
 			txBlotter.setTRADE_AMOUNT(tdpos.getAMOUNT());
 			
 		} else {
 
 			String mkprice = String.valueOf(tdprice.getCLOSE_PRICE()).trim();
			//: BREADY-133 to fix @1.0E-4 notation
			mkprice = SqlUtil.cleanupDouble(Double.parseDouble(mkprice));
			
 			int decimalIndex = mkprice.indexOf('.');
 			
			//: only first 2 decimal places; cut off (not round up) the rest
 			if (mkprice.length() > decimalIndex+4) {
 				mkprice = mkprice.substring(0, decimalIndex+4);
			}
			
			//: for Refrence //char currentMarketPrice[] = new char[_swprice.getCLOSING_PRICE().length + 1];
 			char currentMarketPrice[] = new char[mkprice.length() + 1];
 			currentMarketPrice[0] = '@';
 			for (int idx = 1 ; idx<currentMarketPrice.length; idx++ ) {
				//: for Refrence //currentMarketPrice[idx] = _swprice.getCLOSING_PRICE()[idx-1];
 				currentMarketPrice[idx] = mkprice.charAt(idx-1);
 			}
			
 			txBlotter.setTRADE_AMOUNT(currentMarketPrice);
 		}
 		
 		//===================================================================================
 		
 		//===================================================================================
 		//-- Blotter Col #29 set Pledge
 		//===================================================================================
 		txBlotter.setPLEDGE('n');
 		//===================================================================================
 		
 		//===================================================================================
 		//-- Blotter Col #30 set Lot Location
 		//===================================================================================
 		txBlotter.setLOT_LOCATION("254".toCharArray());
 		//===================================================================================
 		
 		//===================================================================================
 		//-- Blotter Col #42 set Source
 		//===================================================================================
 		txBlotter.setSOURCE("24".toCharArray());
 		
 		//===================================================================================
 		//-- Blotter Col #45 set Recon
 		//===================================================================================
 		txBlotter.setRECON('n');
 		//===================================================================================
 		
 		//===================================================================================
 		//-- Blotter Col #46 set Post
 		//===================================================================================
 		txBlotter.setPOST('y');
 		//===================================================================================
 
 		//===================================================================================
 		//-- Blotter Col #59 set Perf.Contribution or Withdrawal
 		//===================================================================================
 		txBlotter.setPERF_CONTRIBUTION("y".toCharArray());
 		//===================================================================================
 
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.grimesco.gcodataport.TD.AxysBlotterTransactionRule#appendAxysBlotter(java.util.ArrayList)
 	 */
 	@Override
 	public ArrayList<AxysBlotter> appendAxysBlotter(ArrayList<AxysBlotter> blotterList) {
 	
 		blotterList.add(this.txBlotter);
 		
 		return blotterList;
 	
 	}
 
 }
