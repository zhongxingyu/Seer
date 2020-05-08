 package com.burda.scraper.inventory;
 
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import org.springframework.stereotype.Component;
 import org.springframework.stereotype.Service;
 
 import com.burda.scraper.model.SearchParams;
 import com.burda.scraper.model.SearchResult;
 
 public class InventoryServiceImpl implements InventoryService
 {
 	private NODCInventorySource nodcInventorySource;
 	private FrenchQuarterGuideInventorySource fqgInventorySource;
 	
 	@Override
 	public SearchResult getSearchResult(final SearchParams params) throws Exception
 	{
 		ExecutorService executor = Executors.newFixedThreadPool(2);
 		Future<SearchResult> task = executor.submit(new Callable<SearchResult>(){
 
 			@Override
 			public SearchResult call() throws Exception
 			{
 				return nodcInventorySource.getResults(params);
 			}});
 		Future<SearchResult> task1 = executor.submit(new Callable<SearchResult>(){
 
 			@Override
 			public SearchResult call() throws Exception
 			{
 				return fqgInventorySource.getResults(params);
 			}});		
 		
 		executor.shutdown();
 		SearchResult result = new SearchResult();
		result.getHotels().addAll(task.get().hotels);
 		result.getHotels().addAll(task1.get().hotels);
 		result.headers.addAll(task.get().headers);
 		result.headers.addAll(task1.get().headers);
 		result.endDate = task.get().endDate;
 		result.startDate = task.get().startDate;
 		return result;
 	}
 	
 	public void setNODCInventorySource(NODCInventorySource invSource)
 	{
 		this.nodcInventorySource = invSource;
 	}
 	
 	public void setFQGInventorySource(FrenchQuarterGuideInventorySource invSource)
 	{
 		this.fqgInventorySource = invSource;
 	}	
 }
