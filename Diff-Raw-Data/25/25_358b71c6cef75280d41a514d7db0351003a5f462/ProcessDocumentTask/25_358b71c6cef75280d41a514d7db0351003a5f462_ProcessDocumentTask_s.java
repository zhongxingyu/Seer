 package ru.uspu.library.elib.spider.io;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.List;
 import java.util.Queue;
 import java.util.concurrent.Callable;
 
import ru.uspu.library.elib.spider.ErrorHandler;
 import ru.uspu.library.elib.spider.domain.Book;
 import ru.uspu.library.elib.spider.parser.BookInfoParser;
 import ru.uspu.library.elib.spider.parser.IndexPageUrlParser;
 
 public class ProcessDocumentTask implements Callable<List<Book>> {
 
 	private final BookInfoParser bookInfoParser;
	private final ErrorHandler errorHandler;
 	private final IndexPageUrlParser indexUrlParser;
 	private final String url;
 	private final Queue<String> urlQueue;
 	private final WebDocumentLoader webDocumentLoader;
 
 	public ProcessDocumentTask(String url, Queue<String> urlQueue,
 			WebDocumentLoader webDocumentLoader, BookInfoParser bookInfoParser,
			IndexPageUrlParser indexUrlParser, ErrorHandler errorHandler) {
 		this.url = url;
 		this.urlQueue = urlQueue;
 		this.webDocumentLoader = webDocumentLoader;
 		this.bookInfoParser = bookInfoParser;
 		this.indexUrlParser = indexUrlParser;
		this.errorHandler = errorHandler;
 	}
 
 	@Override
 	public List<Book> call() throws Exception {
 		String document = null;
 		try {
 			document = getWebDocumentLoader().load(getUrl());
 			List<Book> books = getBookInfoParser().parse(document, getUrl());
 			List<String> indexPageUrls = getIndexUrlParser().parse(document);
 			document = null;
 			for (String url : indexPageUrls) {
 				getUrlQueue().add(url);
 			}
 			return books;
 		}
 		catch (MalformedURLException e) {
			getErrorHandler().onError(e);
 			throw e;
 		}
 		catch (IOException e) {
			getErrorHandler().onError(e);
 			throw e;
 		}
 	}
 
 	private BookInfoParser getBookInfoParser() {
 		return bookInfoParser;
 	}
 
	private ErrorHandler getErrorHandler() {
		return errorHandler;
	}

 	private IndexPageUrlParser getIndexUrlParser() {
 		return indexUrlParser;
 	}
 
 	private String getUrl() {
 		return url;
 	}
 
 	private Queue<String> getUrlQueue() {
 		return urlQueue;
 	}
 
 	private WebDocumentLoader getWebDocumentLoader() {
 		return webDocumentLoader;
 	}
 
 }
