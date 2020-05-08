 /**
  * 
  */
 package com.grimesco.gcodataport.fi;
 
 import java.util.ArrayList;
 
 import com.grimesco.gcocore.act.Account;
 import com.grimesco.gcocore.axys.model.AxysBlotter;
 import com.grimesco.gcocore.axys.model.AxysSymbol;
 import com.grimesco.translateFidelity.model.FIposition;
 import com.grimesco.translateFidelity.model.FIprice;
 
 /**
  * @author jaeboston
  *
  */
 public class PositionRule4FI extends A_FIBlotterPositionRule {
 
 	//-- constructor
 	public PositionRule4FI(Account _account, FIposition _fipos, AxysSymbol _asymbol, FIprice _fiprice) {
 	
 		super(_account, _fipos, _asymbol, _fiprice);
 		
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
 		if (String.valueOf(_fipos.getSYMBOL()).toUpperCase().equals("CASH") || String.valueOf(asymbol.getTYPE()).toUpperCase().equals("MMUS") ) {
 			txBlotter.setSECURITY_SYMBOL(_fiprice.getSYMBOL());
 		} else {
 			txBlotter.setSECURITY_SYMBOL(_fiprice.getSYMBOL());
 		}
 		//===================================================================================
 	
 		//===================================================================================
 		//-- Blotter Col #6 set Trade Date -- should be the working date
 		//-- see manResolvetButton.addListener(new Button.ClickListener()  in CostbasisWindow.java
 		//===================================================================================
 		txBlotter.setSQL_TRADE_DATE(_fipos.getSQL_POSITION_DATE());	
 		//===================================================================================
 	
 		//===================================================================================
 		//-- Blotter Col #9 set Quantity
 		//===================================================================================		
		if (String.valueOf(_fipos.getSYMBOL()).toUpperCase().equals("CASH") || String.valueOf(asymbol.getTYPE()).toUpperCase().equals("MMUS")  ) {
			txBlotter.setQUANTITY(_fipos.getORIGINAL_FACE_AMOUNT());
		} else {
 			txBlotter.setQUANTITY(_fipos.getTRANSACTION_QUANTITY());
 		}
 		//===================================================================================
 	
 		//===================================================================================
 		//-- Blotter Col #18 set Trade Amount
 		//===================================================================================
		if (String.valueOf(_fipos.getSYMBOL()).toUpperCase().equals("CASH")) {
 			
 			txBlotter.setTRADE_AMOUNT(_fipos.getTRANSACTION_QUANTITY());
 
 		} else {
 			char currentMarketPrice[] = new char[_fiprice.getCLOSE_PRICE().length + 1];
 			currentMarketPrice[0] = '@';
 			for (int idx = 1 ; idx<currentMarketPrice.length; idx++ ) {
 				currentMarketPrice[idx] = _fiprice.getCLOSE_PRICE()[idx-1];
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
 	
 	
 	@Override
 	public ArrayList<AxysBlotter> appendAxysBlotter(ArrayList<AxysBlotter> blotterList) {
 	
 		blotterList.add(this.txBlotter);
 		
 		return blotterList;
 	
 	}
 
 }
