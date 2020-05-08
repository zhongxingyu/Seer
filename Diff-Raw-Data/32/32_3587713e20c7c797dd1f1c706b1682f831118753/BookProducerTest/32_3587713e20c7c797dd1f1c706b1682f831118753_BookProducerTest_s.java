 package com.hidden.data.producer;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Before;
import org.junit.Ignore;
 import org.junit.Test;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.MockitoAnnotations;
 
 import com.common.reflection.Reflection;
 import com.hidden.data.db.dao.BookDao;
 import com.hidden.data.db.dao.PatternDao;
 import com.hidden.data.db.model.Book;
 import com.hidden.data.db.model.Pattern;
 import com.hidden.data.db.model.PatternRow;
 import com.hidden.data.queue.connection.QueueConnection;
 import com.hidden.data.queue.connection.QueueConnectionFactory;
 import com.hidden.data.queue.model.FilterItem;
 
@Ignore
 public class BookProducerTest {
 
 	private static final String PATTERN_NAME = "patternName";
 	private static final Integer BOOK_ID = Integer.valueOf(1);
 	private static final Integer PATTERN_ID = Integer.valueOf(23);
 
 	@Mock
 	private BookDao bookDao;
 	@Mock
 	private PatternDao patternDao;
 	@Mock
 	private QueueConnection queueConnection;
 	@Mock
 	private QueueConnectionFactory connectionFactory;
 	@Mock
 	private PatternRow patternRow;
 
 	private ProducerConnectionStub producerConnection;
 
 	@InjectMocks
 	private BookProducer victim = new BookProducer();
 
 	@Before
 	public void setUp() {
 		MockitoAnnotations.initMocks(this);
 		mockQueueConnection();
 	}
 
 	private void mockQueueConnection() {
 		producerConnection = new ProducerConnectionStub(queueConnection);
 		Mockito.when(connectionFactory.createFilterProducerConnection())
 				.thenReturn(producerConnection);
 	}
 
 	@Test
 	public void run_loadsAllBooks() {
 		whenRun();
 		thenVerifyAllBooksLoaded();
 	}
 
 	private void whenRun() {
 		victim.run();
 	}
 
 	private void thenVerifyAllBooksLoaded() {
 		Mockito.verify(bookDao).loadAll();
 	}
 
 	@Test
 	public void run_LoadsAllPatterns() {
 		whenRun();
 		thenVerifyAllPatternsLoaded();
 	}
 
 	private void thenVerifyAllPatternsLoaded() {
 		Mockito.verify(patternDao).loadAll();
 	}
 
 	@Test
 	public void run_OpensAndClosesFilterProducerConnection() {
 		whenRun();
 		thenVerifiesOpensAndClosesFilterProducerConnection();
 	}
 
 	private void thenVerifiesOpensAndClosesFilterProducerConnection() {
 		Mockito.verify(connectionFactory).createFilterProducerConnection();
 		Assert.assertTrue(producerConnection.isConnectionClosed());
 	}
 
 	@Test
 	public void run_0B0P_SendsNothing() {
 		givenBooksWithLines();
 		givenPatternsWithLines();
 		whenRun();
 		thenShouldSendToQueue(0);
 	}
 
 	private void givenBooksWithLines(int... numberOfLinesList) {
 		List<Book> books = new ArrayList<Book>();
 		for (int numberOfLines : numberOfLinesList) {
 			books.add(createBook(numberOfLines));
 		}
 		Mockito.when(bookDao.loadAll()).thenReturn(books);
 	}
 
 	private Book createBook(int numberOfLines) {
		return null;
		// Book book = Book.createEmptyBook();
		// Reflection.getInstance().setField(book, "id", BOOK_ID);
		// StringBuilder sb = new StringBuilder();
		// for (int i = 0; i < numberOfLines; i++) {
		// sb.append(BOOK_LINE_CONTENT).append(i)
		// .append(BookProducer.LINE_BREAK);
		// }
		// book.setContent(sb.toString());
		// return book;
 	}
 
 	private void givenPatternsWithLines(int... numberOfLinesList) {
 		List<Pattern> patterns = new ArrayList<Pattern>();
 		for (int numberOfLines : numberOfLinesList) {
 			patterns.add(createPatternWithLines(numberOfLines));
 		}
 		Mockito.when(patternDao.loadAll()).thenReturn(patterns);
 	}
 
 	private Pattern createPatternWithLines(int numberOfLines) {
 		Pattern pattern = Pattern.createEmptyPattern();
 		Reflection.getInstance().setField(pattern, "id", PATTERN_ID);
 		Reflection.getInstance().setField(pattern, "name", PATTERN_NAME);
 		Reflection.getInstance().setField(pattern, "rows",
 				Collections.nCopies(numberOfLines, patternRow));
 		return pattern;
 	}
 
 	private void thenShouldSendToQueue(int numberOfItems) {
 		Assert.assertEquals(numberOfItems,
 				producerConnection.getNumberOfMessagesSent());
 	}
 
 	@Test
 	public void run_3B0P_SendsNothing() {
 		givenBooksWithLines();
 		givenPatternsWithLines();
 		whenRun();
 		thenShouldSendToQueue(0);
 	}
 
 	@Test
 	public void run_0B3P_SendsNothing() {
 		givenBooksWithLines();
 		givenPatternsWithLines(3);
 		whenRun();
 		thenShouldSendToQueue(0);
 	}
 
 	@Test
 	public void run_BP_SameLengh_SendsOneMessage() {
 		givenBooksWithLines(10);
 		givenPatternsWithLines(10);
 		whenRun();
 		thenShouldSendToQueue(1);
 	}
 
 	@Test
 	public void run_BP_BiggerPattern_SendsNothing() {
 		givenBooksWithLines(10);
 		givenPatternsWithLines(11);
 		whenRun();
 		thenShouldSendToQueue(0);
 	}
 
 	@Test
 	public void run_B10LinesP5Lines_Sends6Messages() {
 		givenBooksWithLines(10);
 		givenPatternsWithLines(5);
 		whenRun();
 		thenShouldSendToQueue(6);
 	}
 
 	@Test
 	public void run_B10LinesP1Lines_Sends10Messages() {
 		givenBooksWithLines(10);
 		givenPatternsWithLines(1);
 		whenRun();
 		thenShouldSendToQueue(10);
 	}
 
 	@Test
 	public void run_B10LinesP1LineP5Lines_Sends16Messages() {
 		givenBooksWithLines(10);
 		givenPatternsWithLines(1, 5);
 		whenRun();
 		thenShouldSendToQueue(16);
 	}
 
 	@Test
 	public void run_B10LinesB5LinesP1LineP5Lines_Sends22Messages() {
 		givenBooksWithLines(10, 5);
 		givenPatternsWithLines(1, 5);
 		whenRun();
 		thenShouldSendToQueue(22);
 	}
 
 	@Test
 	public void run_B1LineP1Line_SentItemShouldContainBookPatternIds() {
 		givenBooksWithLines(1);
 		givenPatternsWithLines(1);
 		whenRun();
 		thenItemSentShouldContainBookPatternIds();
 	}
 
 	private void thenItemSentShouldContainBookPatternIds() {
 		List<FilterItem> sentItems = producerConnection.getSentItems();
 		for (FilterItem item : sentItems) {
 			Assert.assertEquals(BOOK_ID, item.getBookId());
 			Assert.assertEquals(PATTERN_ID, item.getPatternId());
 		}
 	}
 
 }
