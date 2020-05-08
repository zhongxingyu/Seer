 package fr.quoteBrowser.service;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.List;
 
 import android.content.Context;
 import android.util.Log;
 import fr.quoteBrowser.Quote;
 import fr.quoteBrowser.service.QuoteIndexer.FetchType;
 
 public class QuotePager {
 
 	private static QuotePager instance = null;
 
 	private List<Quote> quotes;
 
 	private static final String TAG = "quoteBrowser";
 
 	private int currentPage = FIRST_PAGE_INDEX;
 
 	public static final int FIRST_PAGE_INDEX = 1;
 
 	private Context context;
 
 	private QuotePager(Context context) {
 		super();
 		this.context = context;
 
 	}
 
 	public static QuotePager getInstance(Context context) {
 		if (instance == null) {
 			instance = new QuotePager(context);
 		}
 		return instance;
 	}
 
 	public List<Quote> getNextQuotePage() throws IOException {
 		loadQuotes();
 		int targetPage = currentPage + 1;
 		List<Quote> result = getQuotePage(targetPage);
 		if (!result.isEmpty()) {
 			currentPage = targetPage;
 		}
 		return result;
 	}
 
 	public List<Quote> getPreviousQuotePage() throws IOException {
 		loadQuotes();
 		int targetPage = currentPage > FIRST_PAGE_INDEX ? currentPage - 1
 				: FIRST_PAGE_INDEX;
 		List<Quote> result = getQuotePage(targetPage);
 		if (!result.isEmpty()) {
 			currentPage = targetPage;
 		}
 		return result;
 	}
 
 	protected List<Quote> getQuotePage(int targetPage) {
 		Log.d(TAG, "trying to display page " + targetPage);
 		int maxPage = computeMaxPage();
 		int nbQuotesPerPage = Preferences.getInstance(context)
 				.getNumberOfQuotesPerPage();
 		Log.d(TAG, quotes.size() + " quotes => " + maxPage + " pages");
 		int startIndex = (targetPage-1) * nbQuotesPerPage;
 		if (startIndex > quotes.size() - 1) {
 			startIndex = quotes.size() - 1;
 		}
		int endIndex = startIndex + nbQuotesPerPage;
 		if (endIndex > quotes.size() - 1) {
 			endIndex = quotes.size() - 1;
 		}
 		if (quotes.size() == 0) {
 			return quotes;
 		}
		Log.d(TAG,"sublist from "+startIndex+ " to "+ endIndex+ " list size="+quotes.size());
 		return quotes.subList(startIndex, endIndex);
 	}
 
 	public int computeMaxPage() {
 		int nbQuotesPerPage = Preferences.getInstance(context)
 				.getNumberOfQuotesPerPage();
 		return quotes != null && !quotes.isEmpty() ? (int) Math.ceil(1d
 				* quotes.size() / nbQuotesPerPage) : FIRST_PAGE_INDEX;
 	}
 
 	private void loadQuotes() {
 		DatabaseHelper db = DatabaseHelper.connect(context);
 		try {
 			String displayPreference = Preferences.getInstance(context)
 					.getDisplayPreference();
 			quotes = db.getQuotes(displayPreference);
 		} finally {
 			db.release();
 		}
 	}
 
 	public boolean isDatabaseEmpty() {
 		DatabaseHelper db = DatabaseHelper.connect(context);
 		boolean empty = true;
 		try {
 			empty = db.isDatabaseEmpty();
 		} finally {
 			db.release();
 		}
 		return empty;
 	}
 
 	public void reindexDatabase() {
 		new QuoteIndexer(context).index(FetchType.COMPLETE);
 	}
 
 	public Collection<? extends Quote> reloadQuotePage() throws IOException {
 		Log.d(TAG, "trying to reload " + currentPage);
 		loadQuotes();
 		if (currentPage > computeMaxPage()) {
 			currentPage = computeMaxPage();
 		}
 		return getQuotePage(currentPage);
 
 	}
 
 	public int getCurrentPage() {
 		return currentPage;
 	}
 
 }
