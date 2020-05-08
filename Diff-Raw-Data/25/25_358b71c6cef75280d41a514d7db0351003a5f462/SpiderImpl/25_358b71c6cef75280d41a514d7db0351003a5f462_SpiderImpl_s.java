 package ru.uspu.library.elib.spider;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Queue;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.RejectedExecutionException;
 import java.util.concurrent.TimeUnit;
 
 import ru.uspu.library.elib.spider.domain.Book;
 import ru.uspu.library.elib.spider.domain.BookComparator;
 import ru.uspu.library.elib.spider.io.ProcessDocumentTask;
 import ru.uspu.library.elib.spider.io.WebDocumentLoader;
 import ru.uspu.library.elib.spider.parser.BookInfoParser;
 import ru.uspu.library.elib.spider.parser.IndexPageUrlParser;
 import ru.uspu.library.elib.spider.settings.WebSpiderSettings;
 
 public class SpiderImpl implements Spider {
 
 	private final BookInfoParser bookInfoParser;
 
 	private final ErrorHandler errorHandler;
 
 	private final IndexPageUrlParser indexPageUrlParser;
 
 	private final WebSpiderSettings settings;
 
 	private final WebDocumentLoader webDocumentLoader;
 
 	public SpiderImpl(WebSpiderSettings settings,
 			WebDocumentLoader webDocumentLoader, BookInfoParser bookInfoParser,
 			IndexPageUrlParser indexPageUrlParser, ErrorHandler errorHandler) {
 		this.settings = settings;
 		this.webDocumentLoader = webDocumentLoader;
 		this.bookInfoParser = bookInfoParser;
 		this.indexPageUrlParser = indexPageUrlParser;
 		this.errorHandler = errorHandler;
 	}
 
 	private BookInfoParser getBookInfoParser() {
 		return bookInfoParser;
 	}
 
 	private ErrorHandler getErrorHandler() {
 		return errorHandler;
 	}
 
 	private IndexPageUrlParser getIndexPageUrlParser() {
 		return indexPageUrlParser;
 	}
 
 	private Comparator<Book> getIndexUrlComparator() {
 		Comparator<Book> comparator = new Comparator<Book>() {
 
 			@Override
 			public int compare(Book book1, Book book2) {
 				return book1.getIndexUrl().compareTo(book2.getIndexUrl());
 			}
 
 		};
 		return comparator;
 	}
 
 	private List<Book> getResults(Map<String, Future<List<Book>>> results)
 			throws InterruptedException {
 		List<Book> allBooks = getResultsOfAllTasks(results);
 		// sort of books is needed to exclude random order of result urls
 		Collections.sort(allBooks, getIndexUrlComparator());
 		Set<Book> uniqueBooks = new TreeSet<Book>(new BookComparator());
 		uniqueBooks.addAll(allBooks);
 		return new ArrayList<Book>(uniqueBooks);
 	}
 
 	private List<Book> getResultsOfAllTasks(
 			Map<String, Future<List<Book>>> results)
 			throws InterruptedException {
 		List<Book> books = new ArrayList<Book>();
 		Set<String> urls = results.keySet();
 		for (String url : urls) {
 			List<Book> taskResults = null;
 			try {
 				taskResults = results.get(url).get();
 				books.addAll(taskResults);
 			}
 			catch (InterruptedException e) {
 				throw e;
 			}
 			catch (ExecutionException e) {
 				getErrorHandler().onError(e);
 			}
 		}
 		return books;
 	}
 
 	private WebSpiderSettings getSettings() {
 		return settings;
 	}
 
 	private Queue<String> getUrlQueue() {
 		Queue<String> queue = new ConcurrentLinkedQueue<String>();
 		queue.add(getSettings().getStartUrl());
 		return queue;
 	}
 
 	private WebDocumentLoader getWebDocumentLoader() {
 		return webDocumentLoader;
 	}
 
 	private boolean isDone(Queue<Future<List<Book>>> taskInProgress) {
 		while (!taskInProgress.isEmpty()) {
 			Future<List<Book>> queueItem = taskInProgress.peek();
 			if ((queueItem.isDone()) || (queueItem.isCancelled())) {
 				taskInProgress.poll();
 			}
 			else {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public List<Book> retrieve() throws InterruptedException {
 		Queue<String> urlQueue = getUrlQueue();
 		ExecutorService executorService = Executors.newFixedThreadPool(Runtime
 				.getRuntime().availableProcessors());
 		Map<String, Future<List<Book>>> results = new HashMap<String, Future<List<Book>>>();
 		Queue<Future<List<Book>>> taskInProgress = new LinkedList<Future<List<Book>>>();
 
 		while (!Thread.currentThread().isInterrupted()) {
 			try {
 				String urlToProcess = urlQueue.remove();
 				if (results.get(urlToProcess) == null) {
 					ProcessDocumentTask task = new ProcessDocumentTask(
 							urlToProcess, urlQueue, getWebDocumentLoader(),
							getBookInfoParser(), getIndexPageUrlParser(),
							getErrorHandler());
 					try {
 						Future<List<Book>> futureResult = executorService
 								.submit(task);
 						results.put(urlToProcess, futureResult);
 						taskInProgress.add(futureResult);
 					}
 					catch (RejectedExecutionException e) {
 						getErrorHandler().onError(e);
 						results.remove(urlToProcess);
 					}
 				}
 			}
 			catch (NoSuchElementException e) {
 				// queue empty
 				if (isDone(taskInProgress)) {
 					break;
 				}
 			}
 		}
 		try {
 			executorService.shutdown();
 			executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
 		}
 		catch (InterruptedException e) {
 			throw new InterruptedException();
 		}
 		if (Thread.currentThread().isInterrupted()) {
 			throw new InterruptedException();
 		}
 		return getResults(results);
 	}
 }
