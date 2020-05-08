 package edu.ibs.core.controller;
 
 import edu.ibs.common.dto.TransactionType;
 import edu.ibs.common.enums.Fraction;
 import edu.ibs.core.controller.exception.FreezedException;
 import edu.ibs.core.controller.exception.NotEnoughMoneyException;
 import edu.ibs.core.entity.*;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import static org.junit.Assert.*;
 import org.junit.*;
 
 /**
  *
  * @author vadim
  */
 public class SpecifiedJpaControllerTest {
 
 	private static final SpecifiedJpaController controller = SpecifiedJpaController.instance();
 	private User user;
 	private Currency usd, eur;
 	private CardBook from, to;
 	private BankBook fromBB, toBB;
 	private CreditPlan plan;
 	private Credit credit;
 
 	public SpecifiedJpaControllerTest() {
 	}
 
 	@BeforeClass
 	public static void setUpClass() throws Exception {
 	}
 
 	@AfterClass
 	public static void tearDownClass() throws Exception {
 	}
 
 	@Before
 	public void setUp() {
 		user = new User("test", "user", "MP1452752");
 		controller.insert(user);
 		usd = new Currency("usd-test", 1.5f, Fraction.TWO);
 		controller.insert(usd);
 		eur = new Currency("eur-test", 3f, Fraction.TWO);
 		controller.insert(eur);
 		Money m1 = new Money(10000, usd);
 		fromBB = new BankBook(m1, user, false);
 		controller.insert(fromBB);
 		from = new CardBook(fromBB);
 		controller.insert(from);
 		Money m2 = new Money(10000, eur);
 		toBB = new BankBook(m2, user, false);
 		controller.insert(toBB);
 		to = new CardBook(toBB);
 		controller.insert(to);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		try {
 			controller.delete(CardBook.class, from.getId());
 		} catch (Throwable ignore) {
 		}
 		try {
 			controller.delete(CardBook.class, to.getId());
 		} catch (Throwable ignore) {
 		}
 		try {
 			controller.delete(BankBook.class, fromBB.getId());
 		} catch (Throwable ignore) {
 		}
 		try {
 			controller.delete(BankBook.class, toBB.getId());
 		} catch (Throwable ignore) {
 		}
 		try {
 			controller.delete(Currency.class, usd.getId());
 		} catch (Throwable ignore) {
 		}
 		try {
 			controller.delete(Currency.class, eur.getId());
 		} catch (Throwable ignore) {
 		}
 		try {
 			controller.delete(User.class, user.getId());
 		} catch (Throwable ignore) {
 		}
 	}
 
 	@Test(expected = NotEnoughMoneyException.class)
 	public void payTest() throws Exception {
 		Money money = new Money(5000, to.getBankBook().getCurrency());
 		Transaction tr = controller.pay(from, to, money, TransactionType.PAYMENT);
 		assertNotNull(tr);
 		long tid = tr.getId();
 		assertNotNull(from.getBankBook());
 		assertNotNull(to.getBankBook());
 		assertEquals(controller.select(BankBook.class, from.getBankBook().getId()).getMoney().integer(), 0);
 		assertEquals(controller.select(BankBook.class, to.getBankBook().getId()).getMoney().integer(), 150);
 		controller.delete(Transaction.class, tid);
 		tr = controller.pay(from, to, money, TransactionType.PAYMENT);
 //		assertNull(tr);
 	}
 
 	@Test
 	public void rollbackTest() throws Exception {
 		Money money = new Money(5000, to.getBankBook().getCurrency());
 		Transaction tr = controller.pay(from, to, money, TransactionType.PAYMENT);
 		Transaction rollback = controller.rollback(tr);
 		assertNotNull(rollback);
 		assertEquals(rollback.getMoney().integer(), 50);
 		assertEquals(rollback.getMoney().currency(), to.getBankBook().getCurrency());
 		from = controller.select(CardBook.class, from.getId());
 		assertEquals(from.getBankBook().getMoney().integer(), 100);
 		to = controller.select(CardBook.class, to.getId());
 		assertEquals(to.getBankBook().getMoney().integer(), 100);
 		controller.delete(Transaction.class, rollback.getId());
 		controller.delete(Transaction.class, tr.getId());
 	}
 	//todo add test cases for credit
 
 	@Test
 	public void getContragentListTest() {
 		List<CardBook> contragents = controller.getContragentList();
 		assertNotNull(contragents);
 		assertEquals(SpecifiedJpaController.CONTRAGENT_CARD_BOOK_IDS.length, contragents.size());
 	}
 
 	@Test
 	public void historyTest() {
 		Transaction tr = null;
 		try {
 			Date d1 = new Date();
 			tr = controller.pay(to, to, new Money(0, to.getBankBook().getCurrency()), TransactionType.PAYMENT);
 			Date d2 = new Date();
 			List<Transaction> transactions = controller.getAllHistory(user);
 			assertNotNull(transactions);
 			assertEquals(1, transactions.size());
 		} catch (IllegalArgumentException ex) {
 			Logger.getLogger(SpecifiedJpaControllerTest.class.getName()).log(Level.SEVERE, null, ex);
 		} catch (FreezedException ex) {
 			Logger.getLogger(SpecifiedJpaControllerTest.class.getName()).log(Level.SEVERE, null, ex);
 		} catch (NotEnoughMoneyException ex) {
 			Logger.getLogger(SpecifiedJpaControllerTest.class.getName()).log(Level.SEVERE, null, ex);
 		} finally {
 			if (tr != null) {
 				controller.delete(tr.getClass(), tr.getId());
 			}
 		}
 	}
 }
