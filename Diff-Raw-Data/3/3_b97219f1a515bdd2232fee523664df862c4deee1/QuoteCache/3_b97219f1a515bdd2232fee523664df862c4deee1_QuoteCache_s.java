 package fr.quoteBrowser.service;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import android.content.Context;
 import android.util.Log;
 import fr.quoteBrowser.Quote;
 
 public class QuoteCache {
 
 	private static String TAG = "quoteBrowser";
 
 	private static final int CACHE_FETCH_TIMEOUT = 20;
 
 	// Number of pages to prefetch before and after current page while loading
 	// it
 	private static final int PAGE_PREFETCH_NUMBER = 3;
 
 	private ExecutorService executor = Executors.newCachedThreadPool();
 
 	private Map<Integer, Future<List<Quote>>> pageCache = Collections
 			.synchronizedMap(new HashMap<Integer, Future<List<Quote>>>());
 
 	private QuoteProviderService service;
 
 	public QuoteCache(Context context) {
 		service = QuoteProviderServiceImpl.getInstance(context);
 	}
 
 	public List<Quote> getQuotePageFromCache(int pageNumber) throws IOException {
 		cachePage(pageNumber);
 		for (int i = 1; i <= PAGE_PREFETCH_NUMBER; i++) {
 			cachePage(pageNumber + i);
 			cachePage(pageNumber - i);
 		}
 
 		try {
 			return pageCache.get(pageNumber).get(CACHE_FETCH_TIMEOUT,
 					TimeUnit.SECONDS);
 		} catch (InterruptedException e) {
 			throw new IOException(e);
 		} catch (ExecutionException e) {
 			throw new IOException(e);
 		} catch (TimeoutException e) {
			throw new IOException(e);
 		}
 	}
 
 	public void invalidateCache() {
 		pageCache.clear();
 	}
 
 	private void cachePage(final int pageNumber) {
 		if (pageNumber >= 0) {
 
 			boolean pageNeedToBeCached = false;
 			if (!pageCache.containsKey(pageNumber)) {
 				pageNeedToBeCached = true;
 			} else if (pageCache.get(pageNumber).isDone()
 					|| pageCache.get(pageNumber).isCancelled()) {
 				try {
 					pageCache.get(pageNumber).get();
 				} catch (Exception e) {
 					pageNeedToBeCached = true;
 				}
 			}
 
 			if (pageNeedToBeCached) {
 				Log.d(TAG, "caching page " + pageNumber);
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
 
 	public void remove(int pageNumber) {
 		pageCache.remove(pageNumber);
 
 	}
 
 }
