 package edu.ibs.core.operation.logic;
 
 import edu.ibs.common.dto.TransactionType;
 import edu.ibs.common.enums.AccountRole;
 import edu.ibs.common.enums.CardBookType;
 import edu.ibs.common.enums.Period;
 import edu.ibs.core.controller.SpecifiedJpaController;
 import edu.ibs.core.controller.exception.FreezedException;
 import edu.ibs.core.controller.exception.NotEnoughMoneyException;
 import edu.ibs.core.entity.*;
 import edu.ibs.core.operation.AdminOperations;
 import edu.ibs.core.operation.UserOperations;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Pattern;
 import org.apache.log4j.Logger;
 
 /**
  * @date Oct 22, 2012
  *
  * @author Vadim Martos
  */
 public final class CommonService implements UserOperations, AdminOperations {
 
 	public static final String VALID_EMAIL_REGEXP = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
 	private static final Pattern EMAIL_PATTERN = Pattern.compile(VALID_EMAIL_REGEXP);
 	private final SpecifiedJpaController dataSource = SpecifiedJpaController.instance();
 	private final Logger log = Logger.getLogger(CommonService.class);
 
 	@Override
 	public void update(Account acc) {
 		if (dataSource.exist(Account.class, acc.getId())) {
 			dataSource.update(acc);
 		} else {
 			throw new IllegalArgumentException("Account does not exists");
 		}
 	}
 
 	@Override
 	public List<BankBook> getBankBooks(User user) {
 		return dataSource.bankBooks(user);
 	}
 
 	@Override
 	public List<CardBook> getCardBooks(User user) {
 		if (user == null) {
 			throw new NullPointerException();
 		}
 		return dataSource.getCardBooks(user);
 	}
 
 	@Override
 	public List<CardBook> getCardBooks(User user, BankBook book, CardBookType type) {
 		if (user == null || book == null || type == null) {
 			throw new NullPointerException();
 		}
 		return dataSource.getCardBooks(user, book, type);
 	}
 
 	@Override
 	public List<CardBook> getCardBooks(User user, BankBook book) {
 		if (user == null || book == null) {
 			throw new NullPointerException();
 		}
 		return dataSource.getCardBooks(user, book);
 	}
 
 	@Override
 	public List<CardBook> getCardBooks(User user, CardBookType type) {
 		if (user == null || type == null) {
 			throw new NullPointerException();
 		}
 		return dataSource.getCardBooks(user, type);
 	}
 
 	@Override
 	public List<CardBook> getCardBooks(BankBook bankBook) {
 		if (bankBook == null) {
 			throw new NullPointerException();
 		}
 		return dataSource.getCardBooks(bankBook);
 	}
 
 	@Override
 	public List<CardBook> getCardBooks(BankBook bankBook, CardBookType type) {
 		if (bankBook == null || type == null) {
 			throw new NullPointerException();
 		}
 		return dataSource.getCardBooks(bankBook, type);
 	}
 
 	@Override
 	public void update(CardBook cardBook) {
 		if (!dataSource.exist(cardBook.getClass(), cardBook.getId())) {
 			throw new IllegalArgumentException(String.format("Card book %s does not exist", cardBook));
 		}
 		dataSource.update(cardBook);
 	}
 
 	@Override
 	public void reassign(CardBook cardBook, String toUserWithEmail) {
 		if (!emailIsValid(toUserWithEmail)) {
 			throw new IllegalArgumentException(String.format("Invalid email %s", toUserWithEmail));
 		} else if (!dataSource.exist(cardBook.getClass(), cardBook.getId())) {
 			throw new IllegalArgumentException(String.format("Card book %s does not exist", cardBook));
 		} else if (!reassignmentIsAvailable(toUserWithEmail)) {
 			throw new IllegalArgumentException(String.format("No active user with account's email %s found", toUserWithEmail));
 		} else {
 			User u = dataSource.getUserByEmail(toUserWithEmail);
 			cardBook.setOwner(u);
 			dataSource.update(cardBook);
 		}
 	}
 
 	@Override
 	public boolean reassignmentIsAvailable(String toUserWithEmail) {
 		return dataSource.userExists(toUserWithEmail);
 	}
 
 	@Override
 	public Transaction pay(CardBook from, long toCardBookID, Money money, TransactionType type) throws IllegalArgumentException, FreezedException, NotEnoughMoneyException {
 		CardBook to = dataSource.select(CardBook.class, toCardBookID);
 		return dataSource.pay(from, to, money, type);
 	}
 
 	@Override
 	public Transaction pay(SavedPayment savedPayment, Money money) throws IllegalArgumentException, FreezedException, NotEnoughMoneyException {
 		CardBook from = savedPayment.getTransaction().getFrom();
 		CardBook to = savedPayment.getTransaction().getTo();
 		TransactionType type = savedPayment.getTransaction().getType();
 		return dataSource.pay(from, to, money, type);
 	}
 
 	@Override
 	public SavedPayment savePayment(Transaction transaction, User owner) {
 		SavedPayment sp = new SavedPayment(transaction, owner);
 		dataSource.insert(sp);
 		return sp;
 	}
 
 	@Override
 	public List<SavedPayment> getSavedPayments(User user) {
 		return dataSource.savedPayments(user);
 	}
 
 	@Override
 	public List<Transaction> getHistory(User user, TransactionType type) {
 		return dataSource.historyAll(user, type, null, null);
 	}
 
 	@Override
 	public List<Transaction> getHistory(User user, TransactionType type, Date from, Date to) {
 		return dataSource.historyAll(user, type, from, to);
 	}
 
 	@Override
 	public List<Transaction> getHistoryIncome(User user, TransactionType type) {
 		return dataSource.historyIncome(user, type, null, null);
 	}
 
 	@Override
 	public List<Transaction> getHistoryIncome(User user, TransactionType type, Date from, Date to) {
 		return dataSource.historyIncome(user, type, from, to);
 	}
 
 	@Override
 	public List<Transaction> getHistoryOutcome(User user, TransactionType type) {
 		return dataSource.historyOutcome(user, type, null, null);
 	}
 
 	@Override
 	public List<Transaction> getHistoryOutcome(User user, TransactionType type, Date from, Date to) {
 		return dataSource.historyOutcome(user, type, from, to);
 	}
 
 	@Override
 	public void delete(SavedPayment payment) {
 		dataSource.delete(payment.getClass(), payment.getId());
 	}
 
 	private boolean emailIsValid(String email) {
 		return EMAIL_PATTERN.matcher(email).matches();
 	}
 
 	@Override
 	public Account login(String email, String passwd) {
 		return dataSource.getUserAccount(email, passwd);
 	}
 
 	@Override
 	public Account register(String email, String passwd) {
 		return create(AccountRole.USER, email, passwd);
 	}
 
 	@Override
 	public boolean isFree(String email) {
 		if (emailIsValid(email)) {
			return dataSource.accountExists(email);
 		} else {
 			return false;
 		}
 	}
 
 	@Override
 	public Currency getCurrency(String name) {
 		return dataSource.currency(name);
 	}
 
 	@Override
 	public List<Currency> getCurrencies() {
 		return dataSource.selectAll(Currency.class);
 	}
 
 	@Override
 	public List<CreditPlan> getCreditPlans() {
 		return dataSource.selectAll(CreditPlan.class);
 	}
 
 	@Override
 	public Account create(AccountRole role, String email, String passwd) {
 		if (emailIsValid(email)) {
 			if (isFree(email)) {
 				Account acc = new Account(email, passwd, role);
 				dataSource.insert(acc);
 				return acc;
 			} else {
 				throw new IllegalArgumentException(String.format("Email %s is not free", email));
 			}
 		} else {
 			throw new IllegalArgumentException(String.format("Invalid email %s", email));
 		}
 	}
 
 	@Override
 	public BankBook create(User user, Money money) {
 		if (!dataSource.exist(user.getClass(), user.getId())) {
 			throw new IllegalArgumentException("User does not exist");
 		}
 		BankBook bankBook = new BankBook(user, money);
 		dataSource.insert(bankBook);
 		return bankBook;
 
 	}
 
 	@Override
 	public CardBook create(User user, BankBook bankBook) {
 		if (!dataSource.exist(user.getClass(), user.getId())) {
 			throw new IllegalArgumentException("User does not exist");
 		} else if (!dataSource.exist(bankBook.getClass(), bankBook.getId())) {
 			throw new IllegalArgumentException("Bank book does not exist");
 		} else {
 			CardBook cb = new CardBook(bankBook);
 			dataSource.insert(cb);
 			return cb;
 		}
 	}
 
 	@Override
 	public CardBook create(User user, BankBook bankBook, CreditPlan credit) {
 		if (!dataSource.exist(user.getClass(), user.getId())) {
 			throw new IllegalArgumentException("User does not exist");
 		} else if (!dataSource.exist(bankBook.getClass(), bankBook.getId())) {
 			throw new IllegalArgumentException("Bank book does not exist");
 		} else {
 			CardBook cb = new CardBook(bankBook, new Credit(credit));
 			dataSource.insert(cb);
 			return cb;
 		}
 	}
 
 	@Override
 	public boolean addMoney(BankBook bankBook, Money money) throws IllegalArgumentException, FreezedException {
 		dataSource.addMoney(bankBook, money);
 		return true;
 	}
 
 	@Override
 	public CreditPlan create(String name, Money limit, Period period, int periodMultiply, int percent) {
 		CreditPlan plan = new CreditPlan(name, limit, period, periodMultiply, percent);
 		dataSource.insert(plan);
 		return plan;
 	}
 
 	@Override
 	public void delete(User user) {
 		dataSource.delete(user.getClass(), user.getId());
 	}
 
 	@Override
 	public void update(List<Currency> currencies) {
 		dataSource.batchUpdate(currencies);
 	}
 
 	@Override
 	public void rollback(Transaction transaction) throws IllegalArgumentException, FreezedException, NotEnoughMoneyException {
 		dataSource.rollback(transaction);
 	}
 
 	@Override
 	public List<CardRequest> getAllRequests() {
 		return getRequests(null, null);
 	}
 
 	@Override
 	public List<CardRequest> getRequests(Date from, Date to) {
 		if ((from == null && to != null) || (from != null && to == null)) {
 			throw new NullPointerException();
 		}
 		return dataSource.requests(from, to);
 	}
 
 	@Override
 	public CreditPlan getCreditPlan(String name) {
 		return dataSource.getCreditPlan(name);
 	}
 
 	@Override
 	public boolean bankBookExists(long id) {
 		return dataSource.exist(BankBook.class, id);
 	}
 
 	@Override
 	public boolean cardBookExists(long id) {
 		return dataSource.exist(CardBook.class, id);
 	}
 }
