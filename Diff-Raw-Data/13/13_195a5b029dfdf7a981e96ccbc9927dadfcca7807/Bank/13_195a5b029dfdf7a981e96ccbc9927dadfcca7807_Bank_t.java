 package project.model;
 
 import com.thoughtworks.xstream.XStream;
import project.gui.util.DollarAmountFormatter;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.math.MathContext;
 import java.math.RoundingMode;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public final class Bank {
     public static final MathContext MATH_CONTEXT = new MathContext(4, RoundingMode.HALF_UP);
 
 	private static final File FILE = new File("bank.xml");
 	private static Bank instance;
 	static {
 		if (FILE.exists()) {
 			try {
 				XStream xstream = new XStream();
 				instance = (Bank) xstream.fromXML(new FileReader(FILE));
 			} catch (IOException iox) {
 				throw new ExceptionInInitializerError("could not read bank.xml");
 			}
 		} else {
 			instance = new Bank();
 		}
 	}
 	
 	public static Bank getInstance() {
 		return instance;
 	}
 	
 	public static void saveInstance() throws IOException {
 		if (!FILE.exists()) {
 			FILE.createNewFile();
 		}
 		XStream xstream = new XStream();
 		xstream.toXML(instance, new FileWriter(FILE));
 	}
 	
 	private final Map<String, User> users;
 
 	private final PaymentSchedule schedule;
 
     /**
      * The amount of new loans we are authorized to give out, adjusted whenever a loan is given out or returned. May be
      * non-negative or {@code null}, the latter representing an unlimited cap.
      */
 	private BigDecimal loanCap;
 
 	private int currentMonth;
 
 	private int lastAccountNumber;
 	
 	private Bank() {
 		users = new HashMap<String, User>();
 		schedule = new PaymentSchedule();
 		currentMonth = 0;
 	}
 
     public Collection<User> getUsers() {
         return Collections.unmodifiableCollection(users.values());
     }
 	
 	public User getUser(String username) throws InvalidInputException {
 		if (!users.containsKey(username)) {
             throw new InvalidInputException(username, "no user exists with that username");
 		}
 		return users.get(username);
 	}
 
 	public void addUser(String username, User user) throws InvalidInputException {
 		if (users.containsKey(username)) {
             throw new InvalidInputException(username, "a user already exists with that username");
 		}
 		users.put(username, user);
 	}
 
 	public PaymentSchedule getPaymentSchedule() {
 		return schedule;
 	}
 
 	public BigDecimal getLoanCap() {
 		return loanCap;
 	}
 
 	public void setLoanCap(BigDecimal loanCap) throws InvalidInputException {
 		if (loanCap != null && loanCap.compareTo(BigDecimal.ZERO) < 0) {
             throw new InvalidInputException(loanCap, "loan cap must be non-negative");
 		}
 		this.loanCap = loanCap;
 	}
 
     void authorizeLoan(BigDecimal loanAmount) throws LoanCapException {
         if (loanCap != null) {
             if (loanCap.compareTo(loanAmount) < 0) {
                 throw new LoanCapException(loanCap, loanAmount);
             }
             loanCap = loanCap.subtract(loanAmount);
         }
     }
 
     void returnLoan(BigDecimal returnedAmount) {
     	if (loanCap != null) {
     		loanCap = loanCap.add(returnedAmount);
     	}
     }
 
     public int getCurrentMonth() {
         return currentMonth;
     }
 
     /**
      * Invokes {@link Account#doPayments()} on all accounts, then advances the simulation month ({@link #getCurrentMonth()})
      * by one. If any exceptions are thrown when {@code doPayments()} is invoked on an account, the exception is logged
      * at level {@code FINER}, then counted; the total exception count is returned from the method.
      *
      * @return the total number of exceptions thrown in doPayments() invocations
      */
 	public int advanceCurrentMonth() {
         int failures = 0;
         // before we advance the month, go through all accounts and call doPayments()
         // do this now so that the Transaction objects reflect the current month
         for (User user : users.values()) {
             for (Account account : user.getAccounts()) {
                 try {
                     account.doPayments();
                 } catch (Exception x) {
                     Logger.getLogger(getClass().getCanonicalName())
                             .log(Level.FINER, "exception while handling doPayments() on " + account, x);
                     failures += 1;
                 }
             }
         }
         currentMonth += 1;
         return failures;
     }
 	
 	public int assignAccountNumber() {
 		return ++lastAccountNumber;
 	}
 
     // TODO really have to merge this...
 	private Map<String, String> getStandardDevs(){
 		BigDecimal averageBalance = BigDecimal.ZERO;
 		BigDecimal averageLiabilities = BigDecimal.ZERO;
 		BigDecimal sumAssets = BigDecimal.ZERO;
 		BigDecimal sumLiabilities = BigDecimal.ZERO;
 		BigDecimal standardDeviationAssets = BigDecimal.ZERO;
 		BigDecimal standardDeviationLiabilities = BigDecimal.ZERO;
 		int tempNumberAssets = 0;
 		int tempNumberLiabilities = 0;
 
 		for(User user:users.values()){
 			for(Account account:user.getAccounts()){
 				if (account.getType().isLoan()) {
 					sumAssets = sumAssets.add(account.getBalance());		
 					tempNumberAssets ++;
 				} else {
 					sumLiabilities = sumLiabilities.add(account.getBalance());
 					tempNumberLiabilities ++;
 				}
 			}
 		}
         if (tempNumberAssets > 0) {
 		    averageBalance = sumAssets.divide(new BigDecimal(tempNumberAssets));
         }
         if (tempNumberLiabilities > 0) {
 		    averageLiabilities = sumLiabilities.divide(new BigDecimal(tempNumberLiabilities));
         }
 		
 		for(User user:users.values()){
 			for(Account account:user.getAccounts()){
 				if (account.getType().isLoan()) {
 					standardDeviationAssets = standardDeviationAssets.add(account.getBalance().subtract(averageBalance)).multiply(standardDeviationAssets.add(account.getBalance().subtract(averageBalance)));
 				} else {
 					standardDeviationLiabilities = standardDeviationLiabilities.add(account.getBalance().subtract(averageLiabilities)).multiply(standardDeviationLiabilities.add(account.getBalance().subtract(averageLiabilities)));
 				}
 			}
 		}
         if (tempNumberAssets > 0) {
		    standardDeviationAssets = standardDeviationAssets.divide(new BigDecimal(tempNumberAssets), Bank.MATH_CONTEXT);
         }
         if (tempNumberLiabilities > 0) {
		    standardDeviationLiabilities = standardDeviationLiabilities.divide(new BigDecimal(tempNumberLiabilities), Bank.MATH_CONTEXT);
         }
 		standardDeviationAssets = BigDecimal.valueOf(Math.sqrt(standardDeviationAssets.doubleValue()));
 		standardDeviationLiabilities = BigDecimal.valueOf(Math.sqrt(standardDeviationLiabilities.doubleValue()));
 		
 		Map<String, String> bankStats = new HashMap<String, String>();
         if (tempNumberAssets > 0) {
		    bankStats.put("Standard deviation of assets", new DollarAmountFormatter().valueToString(standardDeviationAssets));
         } else {
             bankStats.put("Standard deviation of assets", "N/A");
         }
         if (tempNumberAssets > 0) {
            bankStats.put("Standard deviation of liabilities", new DollarAmountFormatter().valueToString(standardDeviationLiabilities));
         } else {
             bankStats.put("Standard deviation of liabilities", "N/A");
         }
 		return bankStats;
 	}
 
     public Map<String, String> getStatistics() {
         StatisticsTabulator tabulator = new StatisticsTabulator();
         for (User user : users.values()) {
             tabulator.tabulateUser(user);
             for (Account account : user.getAccounts()) {
                 tabulator.tabulateAccount(account);
             }
         }
         Map<String, String> stats = tabulator.produceStatistics();
         stats.putAll(getStandardDevs());
         return stats;
     }
 }
