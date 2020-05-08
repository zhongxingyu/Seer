 package emcshop.scraper;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 
 import java.io.IOException;
 import java.net.ConnectException;
 import java.net.SocketTimeoutException;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.LogManager;
 
 import org.apache.http.client.HttpClient;
 import org.junit.Test;
 
 import emcshop.scraper.TransactionPuller.Config;
 import emcshop.util.DateGenerator;
 
 public class TransactionPullerTest {
 	private static final EmcSession session = new EmcSession("username", "token", new Date());
 
 	static {
 		//disable log messages
 		LogManager.getLogManager().reset();
 	}
 
 	@Test
 	public void filter() {
 		ShopTransaction t1 = new ShopTransaction();
 		PaymentTransaction t2 = new PaymentTransaction();
 		PaymentTransaction t3 = new PaymentTransaction();
 		List<RupeeTransaction> list = Arrays.asList(t1, t2, t3);
 
 		{
 			List<ShopTransaction> actual = TransactionPuller.filter(list, ShopTransaction.class);
 			List<ShopTransaction> expected = Arrays.asList(t1);
 			assertEquals(expected, actual);
 		}
 
 		{
 			List<PaymentTransaction> actual = TransactionPuller.filter(list, PaymentTransaction.class);
 			List<PaymentTransaction> expected = Arrays.asList(t2, t3);
 			assertEquals(expected, actual);
 		}
 
 		{
 			List<RawTransaction> actual = TransactionPuller.filter(list, RawTransaction.class);
 			List<RawTransaction> expected = Arrays.asList();
 			assertEquals(expected, actual);
 		}
 	}
 
 	@Test(expected = BadSessionException.class)
 	public void not_logged_in() throws Throwable {
 		final Map<Integer, TransactionPage> pages = new HashMap<Integer, TransactionPage>();
 		pages.put(1, new TransactionPage(false, null, Arrays.<RupeeTransaction> asList()));
 
 		thePages = pages;
 		new MockTransactionPuller();
 	}
 
 	@Test
 	public void no_rupee_balance() throws Throwable {
 		final Map<Integer, TransactionPage> pages = new HashMap<Integer, TransactionPage>();
 		pages.put(1, new TransactionPage(true, null, Arrays.<RupeeTransaction> asList()));
 
 		thePages = pages;
 		TransactionPuller puller = new MockTransactionPuller();
 
 		assertNull(puller.getRupeeBalance());
 	}
 
 	@Test
 	public void nextPage() throws Throwable {
 		final Map<Integer, TransactionPage> pages = new HashMap<Integer, TransactionPage>();
 		int page = 1;
 		DateGenerator dg = new DateGenerator(Calendar.MINUTE, -1);
 
 		RupeeTransaction t1 = shop(dg.next());
 		RupeeTransaction t2 = shop(dg.next());
 		RupeeTransaction t3 = shop(dg.next());
 		pages.put(page++, new TransactionPage(true, 20123, Arrays.asList(t1, t2, t3)));
 
 		RupeeTransaction t4 = shop(dg.next());
 		RupeeTransaction t5 = shop(dg.next());
 		RupeeTransaction t6 = shop(dg.next());
 		pages.put(page++, new TransactionPage(true, 40123, Arrays.asList(t4, t5, t6)));
 
 		RupeeTransaction t7 = shop(dg.next());
 		RupeeTransaction t8 = shop(dg.next());
 		RupeeTransaction t9 = shop(dg.next());
 		pages.put(page++, new TransactionPage(true, 40123, Arrays.asList(t7, t8, t9)));
 
 		thePages = pages;
 		TransactionPuller puller = new MockTransactionPuller();
 
 		assertEquals(Integer.valueOf(20123), puller.getRupeeBalance());
 		assertEquals(Arrays.asList(t1, t2, t3), puller.nextPage());
 		assertEquals(Arrays.asList(t4, t5, t6), puller.nextPage());
 		assertEquals(Arrays.asList(t7, t8, t9), puller.nextPage());
 		assertNull(puller.nextPage());
 	}
 
 	@Test
 	public void nextPage_new_transactions_added_during_download() throws Throwable {
 		final Map<Integer, TransactionPage> pages = new HashMap<Integer, TransactionPage>();
 		int page = 1;
 		DateGenerator dg = new DateGenerator(Calendar.MINUTE, -1);
 
 		final RupeeTransaction t0 = shop(dg.next());
 
 		final RupeeTransaction t1 = shop(dg.next());
 		final RupeeTransaction t2 = shop(dg.next());
 		final RupeeTransaction t3 = shop(dg.next());
 		pages.put(page++, new TransactionPage(true, 20123, Arrays.asList(t1, t2, t3)));
 
 		RupeeTransaction t4 = shop(dg.next());
 		RupeeTransaction t5 = shop(dg.next());
 		RupeeTransaction t6 = shop(dg.next());
 		pages.put(page++, new TransactionPage(true, 40123, Arrays.asList(t4, t5, t6)));
 
 		RupeeTransaction t7 = shop(dg.next());
 		RupeeTransaction t8 = shop(dg.next());
 		RupeeTransaction t9 = shop(dg.next());
 		pages.put(page++, new TransactionPage(true, 40123, Arrays.asList(t7, t8, t9)));
 
 		thePages = pages;
 		TransactionPuller puller = new MockTransactionPuller() {
 			private int page1Count = 0;
 
 			@Override
 			synchronized TransactionPage getPage(int page, HttpClient client) throws IOException {
 				//synchronize this method to ensure that the altered page 1 is returned
 				if (page == 1) {
 					page1Count++;
 					if (page1Count >= 3) {
 						//simulate a new transaction being added
 						return new TransactionPage(true, 40123, Arrays.asList(t0, t1, t2, t3));
 					}
 				}
 
 				return super.getPage(page, client);
 			}
 		};
 
 		//the new transaction should be ignored
 		assertEquals(Integer.valueOf(20123), puller.getRupeeBalance());
 		assertEquals(Arrays.asList(t1, t2, t3), puller.nextPage());
 		assertEquals(Arrays.asList(t4, t5, t6), puller.nextPage());
 		assertEquals(Arrays.asList(t7, t8, t9), puller.nextPage());
 		assertNull(puller.nextPage());
 	}
 
 	@Test
 	public void nextPage_maxPaymentTransactionAge() throws Throwable {
 		final Map<Integer, TransactionPage> pages = new HashMap<Integer, TransactionPage>();
 		DateGenerator dg = new DateGenerator(Calendar.HOUR_OF_DAY, -10);
 
 		RupeeTransaction t1 = payment(dg.next());
 		RupeeTransaction t2 = shop(dg.next());
 		RupeeTransaction t3 = payment(dg.next());
 		RupeeTransaction t4 = shop(dg.next());
 
 		pages.put(1, new TransactionPage(true, 20123, Arrays.asList(t1, t2, t3, t4)));
 
 		Config config = new Config.Builder().maxPaymentTransactionAge(1).build();
 		thePages = pages;
 		TransactionPuller puller = new MockTransactionPuller(config);
 
 		assertEquals(Integer.valueOf(20123), puller.getRupeeBalance());
 		assertEquals(Arrays.asList(t1, t2, t4), puller.nextPage());
 		assertNull(puller.nextPage());
 	}
 
 	@Test
 	public void nextPage_startAtPage() throws Throwable {
 		final Map<Integer, TransactionPage> pages = new HashMap<Integer, TransactionPage>();
 		int page = 1;
 		DateGenerator dg = new DateGenerator(Calendar.HOUR_OF_DAY, -1);
 
 		RupeeTransaction t1 = shop(dg.next());
 		RupeeTransaction t2 = shop(dg.next());
 		RupeeTransaction t3 = shop(dg.next());
 		pages.put(page++, new TransactionPage(true, 20123, Arrays.asList(t1, t2, t3)));
 
 		RupeeTransaction t4 = shop(dg.next());
 		RupeeTransaction t5 = shop(dg.next());
 		RupeeTransaction t6 = shop(dg.next());
 		pages.put(page++, new TransactionPage(true, 40123, Arrays.asList(t4, t5, t6)));
 
 		RupeeTransaction t7 = shop(dg.next());
 		RupeeTransaction t8 = shop(dg.next());
 		RupeeTransaction t9 = shop(dg.next());
 		pages.put(page++, new TransactionPage(true, 40123, Arrays.asList(t7, t8, t9)));
 
 		Config config = new Config.Builder().startAtPage(2).build();
 		thePages = pages;
 		TransactionPuller puller = new MockTransactionPuller(config);
 
 		assertEquals(Integer.valueOf(20123), puller.getRupeeBalance());
 		assertEquals(Arrays.asList(t4, t5, t6), puller.nextPage());
 		assertEquals(Arrays.asList(t7, t8, t9), puller.nextPage());
 		assertNull(puller.nextPage());
 	}
 
 	@Test
 	public void nextPage_stopAtDate() throws Throwable {
 		final Map<Integer, TransactionPage> pages = new HashMap<Integer, TransactionPage>();
 		int page = 1;
 		DateGenerator dg = new DateGenerator(Calendar.HOUR_OF_DAY, -1);
 
 		RupeeTransaction t1 = shop(dg.next());
 		RupeeTransaction t2 = shop(dg.next());
 		RupeeTransaction t3 = shop(dg.next());
 		pages.put(page++, new TransactionPage(true, 20123, Arrays.asList(t1, t2, t3)));
 
 		RupeeTransaction t4 = shop(dg.next());
 		RupeeTransaction t5 = shop(dg.next());
 		RupeeTransaction t6 = shop(dg.next());
 		pages.put(page++, new TransactionPage(true, 40123, Arrays.asList(t4, t5, t6)));
 
 		RupeeTransaction t7 = shop(dg.next());
 		RupeeTransaction t8 = shop(dg.next());
 		RupeeTransaction t9 = shop(dg.next());
 		pages.put(page++, new TransactionPage(true, 40123, Arrays.asList(t7, t8, t9)));
 
 		Config config = new Config.Builder().stopAtDate(dg.getGenerated(7)).build();
 		thePages = pages;
 		TransactionPuller puller = new MockTransactionPuller(config);
 
 		assertEquals(Integer.valueOf(20123), puller.getRupeeBalance());
 		assertEquals(Arrays.asList(t1, t2, t3), puller.nextPage());
 		assertEquals(Arrays.asList(t4, t5, t6), puller.nextPage());
 		assertEquals(Arrays.asList(t7), puller.nextPage());
 		assertNull(puller.nextPage());
 	}
 
 	@Test
 	public void nextPage_stopAtPage() throws Throwable {
 		final Map<Integer, TransactionPage> pages = new HashMap<Integer, TransactionPage>();
 		int page = 1;
 		DateGenerator dg = new DateGenerator(Calendar.HOUR_OF_DAY, -1);
 
 		RupeeTransaction t1 = shop(dg.next());
 		RupeeTransaction t2 = shop(dg.next());
 		RupeeTransaction t3 = shop(dg.next());
 		pages.put(page++, new TransactionPage(true, 20123, Arrays.asList(t1, t2, t3)));
 
 		RupeeTransaction t4 = shop(dg.next());
 		RupeeTransaction t5 = shop(dg.next());
 		RupeeTransaction t6 = shop(dg.next());
 		pages.put(page++, new TransactionPage(true, 40123, Arrays.asList(t4, t5, t6)));
 
 		RupeeTransaction t7 = shop(dg.next());
 		RupeeTransaction t8 = shop(dg.next());
 		RupeeTransaction t9 = shop(dg.next());
 		pages.put(page++, new TransactionPage(true, 40123, Arrays.asList(t7, t8, t9)));
 
 		Config config = new Config.Builder().stopAtPage(2).build();
 		thePages = pages;
 		TransactionPuller puller = new MockTransactionPuller(config);
 
 		assertEquals(Integer.valueOf(20123), puller.getRupeeBalance());
 		assertEquals(Arrays.asList(t1, t2, t3), puller.nextPage());
 		assertEquals(Arrays.asList(t4, t5, t6), puller.nextPage());
 		assertNull(puller.nextPage());
 	}
 
 	@Test(expected = DownloadException.class)
 	public void nextPage_bad_session_while_downloading() throws Throwable {
 		final Map<Integer, TransactionPage> pages = new HashMap<Integer, TransactionPage>();
 		int page = 1;
 		DateGenerator dg = new DateGenerator(Calendar.HOUR_OF_DAY, -1);
 
 		RupeeTransaction t1 = shop(dg.next());
 		RupeeTransaction t2 = shop(dg.next());
 		RupeeTransaction t3 = shop(dg.next());
 		pages.put(page++, new TransactionPage(true, 20123, Arrays.asList(t1, t2, t3)));
 
 		RupeeTransaction t4 = shop(dg.next());
 		RupeeTransaction t5 = shop(dg.next());
 		RupeeTransaction t6 = shop(dg.next());
 		pages.put(page++, new TransactionPage(true, 40123, Arrays.asList(t4, t5, t6)));
 
 		pages.put(page++, new TransactionPage(false, 40123, Arrays.<RupeeTransaction> asList()));
 
 		thePages = pages;
 		TransactionPuller puller = new MockTransactionPuller();
 
 		assertEquals(Integer.valueOf(20123), puller.getRupeeBalance());
 		assertEquals(Arrays.asList(t1, t2, t3), puller.nextPage());
 		assertEquals(Arrays.asList(t4, t5, t6), puller.nextPage());
 		puller.nextPage();
 	}
 
 	@Test(expected = DownloadException.class)
 	public void nextPage_IOException_while_downloading() throws Throwable {
 		final Map<Integer, TransactionPage> pages = new HashMap<Integer, TransactionPage>();
 		int page = 1;
 		DateGenerator dg = new DateGenerator(Calendar.HOUR_OF_DAY, -1);
 
 		RupeeTransaction t1 = shop(dg.next());
 		RupeeTransaction t2 = shop(dg.next());
 		RupeeTransaction t3 = shop(dg.next());
 		pages.put(page++, new TransactionPage(true, 20123, Arrays.asList(t1, t2, t3)));
 
 		RupeeTransaction t4 = shop(dg.next());
 		RupeeTransaction t5 = shop(dg.next());
 		RupeeTransaction t6 = shop(dg.next());
 		pages.put(page++, new TransactionPage(true, 40123, Arrays.asList(t4, t5, t6)));
 
 		pages.put(page++, new TransactionPage(false, 40123, Arrays.<RupeeTransaction> asList()));
 
 		thePages = pages;
 		TransactionPuller puller = new MockTransactionPuller() {
 			@Override
 			TransactionPage getPage(int page, HttpClient client) throws IOException {
 				if (page == 2) {
 					throw new IOException();
 				}
 				return super.getPage(page, client);
 			}
 		};
 
 		assertEquals(Integer.valueOf(20123), puller.getRupeeBalance());
 		assertEquals(Arrays.asList(t1, t2, t3), puller.nextPage());
 		puller.nextPage();
 	}
 
 	@Test
 	public void nextPage_retry_on_connection_error_while_downloading() throws Throwable {
 		final Map<Integer, TransactionPage> pages = new HashMap<Integer, TransactionPage>();
 		int page = 1;
 		DateGenerator dg = new DateGenerator(Calendar.HOUR_OF_DAY, -1);
 
 		RupeeTransaction t1 = shop(dg.next());
 		RupeeTransaction t2 = shop(dg.next());
 		RupeeTransaction t3 = shop(dg.next());
 		pages.put(page++, new TransactionPage(true, 20123, Arrays.asList(t1, t2, t3)));
 
 		RupeeTransaction t4 = shop(dg.next());
 		RupeeTransaction t5 = shop(dg.next());
 		RupeeTransaction t6 = shop(dg.next());
 		pages.put(page++, new TransactionPage(true, 40123, Arrays.asList(t4, t5, t6)));
 
 		RupeeTransaction t7 = shop(dg.next());
 		RupeeTransaction t8 = shop(dg.next());
 		RupeeTransaction t9 = shop(dg.next());
 		pages.put(page++, new TransactionPage(true, 40123, Arrays.asList(t7, t8, t9)));
 
 		thePages = pages;
 		TransactionPuller puller = new MockTransactionPuller() {
 			private boolean threwConnectException = false, threwSocketTimeoutException = false;
 
 			@Override
			synchronized TransactionPage getPage(int page, HttpClient client) throws IOException {
 				if (page == 2 && !threwConnectException) {
 					threwConnectException = true;
 					throw new ConnectException();
 				}
 
 				if (page == 3 && !threwSocketTimeoutException) {
 					threwSocketTimeoutException = true;
 					throw new SocketTimeoutException();
 				}
 
 				return super.getPage(page, client);
 			}
 		};
 
 		assertEquals(Integer.valueOf(20123), puller.getRupeeBalance());
 		assertEquals(Arrays.asList(t1, t2, t3), puller.nextPage());
 		assertEquals(Arrays.asList(t4, t5, t6), puller.nextPage());
 		assertEquals(Arrays.asList(t7, t8, t9), puller.nextPage());
 		assertNull(puller.nextPage());
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void configBuilder_maxPaymentTransactionAge() {
 		new Config.Builder().maxPaymentTransactionAge(0);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void configBuilder_startAtPage() {
 		new Config.Builder().startAtPage(0);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void configBuilder_stopAtPage() {
 		new Config.Builder().stopAtPage(0);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void configBuilder_stopAtPage_less_than_startAtPAge() {
 		new Config.Builder().startAtPage(5).stopAtPage(1).build();
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void configBuilder_threadCount() {
 		new Config.Builder().threadCount(0);
 	}
 
 	@Test
 	public void configBuilder_build() {
 		DateGenerator dg = new DateGenerator();
 		Config config = new Config.Builder().maxPaymentTransactionAge(1).startAtPage(2).stopAtDate(dg.next()).stopAtPage(3).threadCount(4).build();
 
 		assertEquals(Integer.valueOf(1), config.getMaxPaymentTransactionAge());
 		assertEquals(2, config.getStartAtPage());
 		assertEquals(dg.getGenerated(0), config.getStopAtDate());
 		assertEquals(Integer.valueOf(3), config.getStopAtPage());
 		assertEquals(4, config.getThreadCount());
 	}
 
 	private ShopTransaction shop(Date ts) {
 		ShopTransaction transaction = new ShopTransaction();
 		transaction.setTs(ts);
 		transaction.setItem("Apple");
 		transaction.setPlayer("Notch");
 		transaction.setQuantity(1);
 		transaction.setAmount(1);
 		transaction.setBalance(1);
 		return transaction;
 	}
 
 	private PaymentTransaction payment(Date ts) {
 		PaymentTransaction transaction = new PaymentTransaction();
 		transaction.setTs(ts);
 		transaction.setPlayer("Notch");
 		transaction.setAmount(1);
 		transaction.setBalance(1);
 		return transaction;
 	}
 
 	private static Map<Integer, TransactionPage> thePages;
 
 	private static class MockTransactionPuller extends TransactionPuller {
 		public MockTransactionPuller() throws BadSessionException, IOException {
 			this(new Config.Builder().build());
 		}
 
 		public MockTransactionPuller(Config config) throws BadSessionException, IOException {
 			super(session, config);
 		}
 
 		@Override
 		TransactionPage getPage(int page, HttpClient client) throws IOException {
 			return thePages.containsKey(page) ? thePages.get(page) : thePages.get(1);
 		}
 	};
 }
