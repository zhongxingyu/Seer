 package fr.quoteBrowser.service;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.Transformer;
 
 import android.content.Context;
 import android.util.Log;
 import fr.quoteBrowser.Quote;
 import fr.quoteBrowser.service.provider.QuoteProvider;
 
 public class QuoteIndexer {
 
 	private static final int NUMBER_OF_PAGES_TO_FETCH = 5;
 	private static String TAG = "quoteBrowser";
 
 	private ExecutorService executor = Executors.newCachedThreadPool();
 	private Context context;
 
 	public enum FetchType {
 		COMPLETE, INCREMENTAL
 	};
 
 	public QuoteIndexer(Context context) {
 		this.context = context;
 	}
 
 	public int index(final FetchType fetchType) {
 		Log.i(TAG, "indexing quotes fetch mode = " + fetchType);
 		DatabaseHelper db = DatabaseHelper.connect(context);
 		final List<Quote> loadedQuotes = new ArrayList<Quote>();
 		try {
 			loadedQuotes.addAll(db.getQuotes());
 		} finally {
 			db.release();
 		}
 		List<Future<List<Quote>>> fetchResults = new ArrayList<Future<List<Quote>>>();
 		for (final QuoteProvider p : QuoteUtils.PROVIDERS) {
 			fetchResults.add(executor.submit(new Callable<List<Quote>>() {
 				@Override
 				public List<Quote> call() throws Exception {
 					switch (fetchType) {
 					case COMPLETE:
 						return simultaneouslyFetchQuotesFromProvider(
 								loadedQuotes, p);
 					case INCREMENTAL:
 						return incrementallyFetchQuotesFromProvider(
 								loadedQuotes, p);
 					default:
 						return null;
 					}
 				}
 			}));
 		}

 		List<Quote> results = new ArrayList<Quote>();
 		for (Future<List<Quote>> fetchResult : fetchResults) {
 			try {
 				List<Quote> providerQuotes=fetchResult.get();
 				// les quotes les plus récentes doivent être ajoutées en dernier
 				// (et elles sont récupérées en tête de liste)
 				Collections.reverse(providerQuotes);
 				results.addAll(providerQuotes);
 			} catch (InterruptedException e) {
 				Log.e(TAG, e.getMessage(), e);
 			} catch (ExecutionException e) {
 				Log.e(TAG, e.getMessage(), e);
 			}
 		}
 		int nbQuotesAdded = 0;
 		db = DatabaseHelper.connect(context);
 		try {
 			db.putQuotes(results);
 			nbQuotesAdded = results.size();
 			Log.d(TAG, "Added " + nbQuotesAdded + " quotes");
 		} finally {
 			db.release();
 		}
 		Log.i(TAG, "done quotes fetch mode = " + fetchType);
 		return nbQuotesAdded;
 	}
 
 	private List<Quote> fetchQuotesFromPage(final int pageNumber,
 			final QuoteProvider provider) throws IOException {
 		Log.d(TAG,
 				"fetching page " + pageNumber + " for provider "
 						+ provider.getSource());
 
 		List<Quote> quotes = new ArrayList<Quote>();
 
 		List<Quote> newQuotes = provider.getQuotesFromPage(pageNumber);
 		quotes.addAll(newQuotes);
 
 		Log.d(TAG, "done fetching page " + pageNumber + " for provider "
 				+ provider.getSource());
 		return quotes;
 
 	}
 
 	/**
 	 * fetch quotes from provider {@link QuoteIndexer#NUMBER_OF_PAGES_TO_FETCH}
 	 * at a time
 	 * 
 	 * @param loadedQuotes
 	 * @param p
 	 * @return
 	 */
 	private List<Quote> simultaneouslyFetchQuotesFromProvider(
 			List<Quote> loadedQuotes, final QuoteProvider p) {
 		List<Quote> result = new ArrayList<Quote>();
 		List<Future<List<Quote>>> futures = new ArrayList<Future<List<Quote>>>();
 		for (int i = 0; i < NUMBER_OF_PAGES_TO_FETCH; i++) {
 			final int pageNumber = i;
 			futures.add(executor.submit(new Callable<List<Quote>>() {
 				@Override
 				public List<Quote> call() throws Exception {
 					return fetchQuotesFromPage(pageNumber, p);
 				}
 			}));
 		}
 		@SuppressWarnings("unchecked")
 		Collection<String> md5OfExistingQuotes = CollectionUtils.collect(
 				loadedQuotes, new Transformer() {
 
 					@Override
 					public Object transform(Object quote) {
 						return ((Quote) quote).getQuoteTextMD5();
 					}
 				});
 		for (Future<List<Quote>> pageresult : futures) {
 			try {
 				for (Quote q : pageresult.get()) {
 					if (!quoteAlreadyInList(q, md5OfExistingQuotes)) {
 						result.add(q);
 					}
 				}
 			} catch (InterruptedException e) {
 				Log.e(TAG, e.getMessage(), e);
 			} catch (ExecutionException e) {
 				Log.e(TAG, e.getMessage(), e);
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * Fetch quotes page after page and stop if a quote already exists in
 	 * database
 	 * 
 	 * @param loadedQuotes
 	 * @param p
 	 * @return
 	 */
 	private List<Quote> incrementallyFetchQuotesFromProvider(
 			List<Quote> loadedQuotes, final QuoteProvider p) {
 		List<Quote> result = new ArrayList<Quote>();
 		boolean databaseAlreadyContainsQuote = false;
 		@SuppressWarnings("unchecked")
 		Collection<String> md5OfExistingQuotes = CollectionUtils.collect(
 				loadedQuotes, new Transformer() {
 
 					@Override
 					public Object transform(Object quote) {
 						return ((Quote) quote).getQuoteTextMD5();
 					}
 				});
 		for (int i = 0; i < NUMBER_OF_PAGES_TO_FETCH
 				&& !databaseAlreadyContainsQuote; i++) {
 			try {
 				List<Quote> potentialQuotesToAdd = fetchQuotesFromPage(i, p);
 				for (Quote q : potentialQuotesToAdd) {
 					if (!quoteAlreadyInList(q, md5OfExistingQuotes)) {
 						result.add(q);
 					} else {
 						databaseAlreadyContainsQuote = true;
 					}
 				}
 
 			} catch (IOException e) {
 				Log.e(TAG, e.getMessage(), e);
 			}
 		}
 
 		return result;
 	}
 
 	private boolean quoteAlreadyInList(final Quote q,
 			Collection<String> md5OfLoadedQuotes) {
 		return (md5OfLoadedQuotes.contains(q.getQuoteTextMD5()));
 	}
 
 }
