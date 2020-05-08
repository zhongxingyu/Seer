 package mtgscraper.http;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import org.jsoup.nodes.Document;
 
 import mtgscraper.Visitor;
 import mtgscraper.entities.Card;
 import mtgscraper.http.Http.Processor;
 
 public class CardDownloadManager {
 	private final ThreadPoolExecutor threadPool;
 	private final Http http;
 	
 	public CardDownloadManager(Http http) {
 		this.threadPool = new ThreadPoolExecutor(5, 10, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
 		this.http = http;
 	}
 	
 	/**
 	 * Requests all of the cards for the given set. The visitor is invoked for each
 	 * card within the set. The order the visitor is invoked is guaranteed to be in
 	 * the order in the set.
 	 */
 	public void requestCards(List<CardLink> cardLinks, Visitor<Card> cardVisitor, Visitor<List<Card>> finished) {
 		//copy the array so it can't change during the request
 		cardLinks = new ArrayList<CardLink>(cardLinks);
 		
 		ListDownloadContext<Card> handler = new ListDownloadContext<Card>(cardLinks.size());
 		handler.setOnDownloaded(cardVisitor);
 		handler.setOnListFinished(finished);
 		
 		for(int cardIndex = 0; cardIndex < cardLinks.size(); cardIndex++) {
 			CardLink cardLink = cardLinks.get(cardIndex);
 			threadPool.execute(new DownloadAction<Card>(handler, cardIndex, cardLink.getUrl(), new CardProcessor(cardLink)));
 		}
 	}
 	
 	public void shutdown() {
 		threadPool.shutdown();
 
 		try {
 			while(!threadPool.awaitTermination(3, TimeUnit.MINUTES)) {
 				//waiting for threads to finish
 			}
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private static class ListDownloadContext<T> {
 		private Object[] downloaded;
 		private int lastNotified = -1;
 		private Visitor<T> onDownloadedVisitor;
 		private Visitor<List<T>> onFinished;
 		
 		public ListDownloadContext(int numberOfItems) {
 			//initialize the array to hold the downloaded cards
 			this.downloaded = new Object[numberOfItems];
 		}
 		
		public void setOnDownloaded(Visitor<T> visitor) {
 			this.onDownloadedVisitor = visitor;
 		}
 		
		public void setOnListFinished(Visitor<List<T>> finished) {
 			this.onFinished = finished;
 		}
 		
 		/**
 		 * Each thread will invoke the finished method.
 		 */
 		public synchronized void finished(final int index, final T card) {
 			downloaded[index] = card;
 			notifyVisitors();
 		}
 		
 		public void onError(int index, String url) {
 			System.err.println("Error downloading card from URL: " + url);
 		}
 		
 		/**
 		 * Tells the visitors when a card has been downloaded. The order 
 		 * the visitors are invoked is guaranteed to be the order the
 		 * URL's were submitted.
 		 */
 		@SuppressWarnings("unchecked")
 		private synchronized void notifyVisitors() {
 			if(lastNotified + 1 == downloaded.length) {
 				//notify the finished visitor that we have finished
 				if(onFinished != null) {
 					ArrayList<T> list = new ArrayList<T>();
 					for(Object item : this.downloaded) {
 						list.add((T) item);
 					}
 					onFinished.visit(list);
 				}
 				return;
 			}
 			if(downloaded[lastNotified + 1] == null) {
 				//since the card is not download yet, there is no one to notify
 				return;
 			}
 			
 			//notify the card visitor that the next card is available
 			lastNotified += 1;
 			T card = (T)downloaded[lastNotified];
 			if(onDownloadedVisitor != null) {
 				onDownloadedVisitor.visit(card);
 			}
 			
 			//run notify again to check the next card is finished
 			notifyVisitors();
 		}
 	}
 	
 	/**
 	 * Requests the page at the given URL, processes the response and tells the handler when finished.
 	 * @author jared.pearson
 	 */
 	private class DownloadAction<T> implements Runnable {
 		private final int urlIndex;
 		private final String url;
 		private Processor<Document, T> responseProcessor;
 		private final ListDownloadContext<T> context;
 		
 		private DownloadAction(ListDownloadContext<T> context, int urlIndex, String url, Processor<Document, T> responseProcessor) {
 			this.urlIndex = urlIndex;
 			this.url = url;
 			this.responseProcessor = responseProcessor;
 			this.context = context;
 		}
 		
 		@Override
 		public void run() {
 			try {
 				T response = http.requestDocument(url, responseProcessor);
 				context.finished(urlIndex, response);
 			} catch (IOException e) {
 				context.onError(urlIndex, url);
 			}
 		}
 	}
 }
