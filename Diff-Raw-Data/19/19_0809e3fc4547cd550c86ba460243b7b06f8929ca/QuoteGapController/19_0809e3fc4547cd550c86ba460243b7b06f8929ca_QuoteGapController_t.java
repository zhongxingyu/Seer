 package com.cspinformatique.csptrading.controller;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Map;
import java.util.TreeMap;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.cspinformatique.csptrading.entity.QuoteGap;
 import com.cspinformatique.csptrading.entity.Stock;
 import com.cspinformatique.csptrading.service.QuoteGapService;
 import com.cspinformatique.csptrading.service.StockService;
 import com.cspinformatique.csptrading.util.MarketUtil;
 
 @Controller
 @RequestMapping("/quoteGap")
 public class QuoteGapController {
 	@Autowired private QuoteGapService quoteGapService;
 	@Autowired private StockService stockService;
 	
 	private DateFormat dateFormat;
 	
 	public QuoteGapController(){
 		this.dateFormat = new SimpleDateFormat("yyyy/MM/dd");
 	}
 	
 	@RequestMapping
 	public @ResponseBody Map<String, Map<String, QuoteGap>> getQuoteGaps(){
 		// Building the matrix.
		Map<String, Map<String, QuoteGap>> quoteGapsDates = new TreeMap<String, Map<String,QuoteGap>>();
 		for(Date openedDay : MarketUtil.getOpenedDatesSinceDays(10)){
			Map<String, QuoteGap> quoteGaps = new TreeMap<String, QuoteGap>();
 			for(Stock stock : this.stockService.getStocks()){
 				quoteGaps.put(stock.getSymbol(), this.quoteGapService.getQuoteGap(stock.getId(), openedDay));
 			}
 			quoteGapsDates.put(dateFormat.format(openedDay), quoteGaps);
 		}
 		
 		return quoteGapsDates;
 	}
 }
