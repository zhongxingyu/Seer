 package fr.quoteBrowser.service;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 
 import org.apache.commons.collections.map.LRUMap;
 
 import android.content.Context;
 import fr.quoteBrowser.Quote;
 
 public class QuotePager {
 
 	private static QuotePager instance = null;
 
 	private static final String TAG = "quoteBrowser";
 
 	private static final int PAGE_CACHE_SIZE = 10;
 
 	// Number of pages to prefetch before and after current page while loading
 	// it
 	private static final int PAGE_PREFETCH_NUMBER = 4;
 
 	private static final int MAX_CONCURRENT_PAGE_FETCH = 3;
 
 	private int currentPage = -1;
 
 	private QuoteProviderService service;
 
 	private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
 			MAX_CONCURRENT_PAGE_FETCH);
 
 	private Map<Integer, Future<List<Quote>>> pageCache = Collections
 			.synchronizedMap(new LRUMap(PAGE_CACHE_SIZE));
 
 	private QuotePager(Context context) {
 		super();
 		service = QuoteProviderService.getInstance(context);
 	}
 
 	public static QuotePager getInstance(Context context) {
 		if (instance == null) {
 			instance = new QuotePager(context);
 		}
 		return instance;
 	}
 
 	public List<Quote> getNextQuotePage() throws IOException {
 		List<Quote> quotes = getQuotePageFromCache(currentPage + 1);
 		currentPage++;
 		return quotes;
 	}
 
 	public List<Quote> getPreviousQuotePage() throws IOException {
 		int previousPage = currentPage > 0 ? currentPage - 1 : 0;
 		List<Quote> quotes = getQuotePageFromCache(previousPage);
 		currentPage = previousPage;
 		return quotes;
 	}
 
 	public Collection<? extends Quote> reloadQuotePage() throws IOException {
 		pageCache.remove(currentPage);
 		return getQuotePageFromCache(currentPage);
 	}
 
 	public int getCurrentPage() {
 		return currentPage;
 	}
 
 	private List<Quote> getQuotePageFromCache(int pageNumber)
 			throws IOException {
 		cachePage(pageNumber);
 		for (int i = 1; i < PAGE_PREFETCH_NUMBER + 1; i++) {
 			cachePage(pageNumber + i);
 			cachePage(pageNumber - i);
 		}
 
 		try {
 			return pageCache.get(pageNumber).get();
 		} catch (InterruptedException e) {
			pageCache.put(pageNumber, null);
 			throw new IOException(e);
 		} catch (ExecutionException e) {
			pageCache.put(pageNumber, null);
 			throw new IOException(e);
 		}
 	}
	
	public void invalidateCache(){
		pageCache.clear();
	}
 
 	private void cachePage(final int pageNumber) {
 
 		if (pageNumber >= 0 && !pageCache.containsKey(pageNumber)) {
 
 			Callable<List<Quote>> quotePageRequest = new Callable<List<Quote>>() {
 
 				@Override
 				public List<Quote> call() throws Exception {
 					return service.getQuotesFromPage(pageNumber);
 				}
 			};
 			pageCache.put(pageNumber, executor.submit(quotePageRequest));
 
 		}
 	}
 }
