 package dat255.grupp06.bibbla.backend.tasks;
 
 import java.util.List;
 
 import junit.framework.TestCase;
 import dat255.grupp06.bibbla.model.Book;
 import dat255.grupp06.bibbla.utils.Message;
 import dat255.grupp06.bibbla.utils.Session;
 
 public class UnreserveJobTest extends TestCase {
 	
 	private final Session SESSION = new Session();
	private final ReserveJob reserveJob;

	protected void setUp() throws Exception {
		super.setUp();
		reserveJob = new ReserveJob(ReserveJobTest.TEST_BOOK, ReserveJobTest.
				LIBRARY_CODE, ReserveJobTest.CREDENTIALS, SESSION);
	}
 
 	public void testConnect() {
 		fail("Not yet implemented");
 	}
 
 	public void testRun() {
 		// Prepare: Get number of reservations and reserve the test book.
 		MyReservationsJob booksJob = new MyReservationsJob(
 				ReserveJobTest.CREDENTIALS, SESSION);
 		@SuppressWarnings("unchecked")
 		int beforeReserveSize = ((List<Book>) booksJob.run().obj).size();
 		Message reserveMessage = reserveJob.run();
 		assertNull(reserveMessage.error);
 		
 		// Method call
 		UnreserveJob job = new UnreserveJob(ReserveJobTest.TEST_BOOK,
 				ReserveJobTest.CREDENTIALS, SESSION);
 		Message message = job.run();
 		
 		// Assertion
 		assertNull(message.error);
 		@SuppressWarnings("unchecked")
 		List<Book> myBooksAfterUnreserve = (List<Book>) message.obj;
 		assertFalse(myBooksAfterUnreserve.contains(ReserveJobTest.TEST_BOOK));
 
 		@SuppressWarnings("unchecked")
 		int afterUnreserveSize = ((List<Book>) booksJob.run().obj).size();
 		assertEquals(beforeReserveSize, afterUnreserveSize);
 	}
 }
