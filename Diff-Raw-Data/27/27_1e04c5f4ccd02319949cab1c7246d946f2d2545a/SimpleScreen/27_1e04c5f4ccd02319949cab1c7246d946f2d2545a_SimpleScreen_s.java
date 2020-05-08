 package com.myrontuttle.screener;
 
 import java.util.List;
 import com.myrontuttle.adaptivetrader.ScreenerService;
 import com.myrontuttle.adaptivetrader.ScreenCriteria;
 
 public class SimpleScreen {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		
 		ScreenerService screener = new YahooFinanceStockScreener();
 		
		List<ScreenCriteria> options = screener.getAvailableCriteria();
 		
 		int[] selected = new int[options.size() / 4];
 		
 		for (int i=0; i<selected.length; i++) {
 			selected[i] = options.get(i).getLength() - 1;
 		}
 		
		if (screener.areValid(selected)) {
			List<String> symbols = screener.screen(selected);
			for (String symbol : symbols) {
				System.out.println(symbol);
 			}
		} else {
			System.out.println("Invalid selection");
 		}
 	}
 
 }
