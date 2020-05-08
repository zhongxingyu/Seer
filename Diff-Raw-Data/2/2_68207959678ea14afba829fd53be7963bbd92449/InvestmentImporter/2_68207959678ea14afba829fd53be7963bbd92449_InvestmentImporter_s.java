 package net.sf.jmoney.qifstock;
 
 import java.math.BigDecimal;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.sf.jmoney.model2.Account;
 import net.sf.jmoney.model2.CapitalAccount;
 import net.sf.jmoney.model2.Commodity;
 import net.sf.jmoney.model2.Currency;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.IncomeExpenseAccount;
 import net.sf.jmoney.model2.IncomeExpenseAccountInfo;
 import net.sf.jmoney.model2.Session;
 import net.sf.jmoney.model2.Transaction;
 import net.sf.jmoney.qif.IQifImporter;
 import net.sf.jmoney.qif.QIFEntry;
 import net.sf.jmoney.qif.QIFEntryInfo;
 import net.sf.jmoney.qif.QIFPlugin;
 import net.sf.jmoney.qif.parser.QifAccount;
 import net.sf.jmoney.qif.parser.QifCategoryLine;
 import net.sf.jmoney.qif.parser.QifDate;
 import net.sf.jmoney.qif.parser.QifFile;
 import net.sf.jmoney.qif.parser.QifInvstTransaction;
 import net.sf.jmoney.qif.parser.QifSplitTransaction;
 import net.sf.jmoney.stocks.model.Stock;
 import net.sf.jmoney.stocks.model.StockAccount;
 import net.sf.jmoney.stocks.model.StockAccountInfo;
 import net.sf.jmoney.stocks.model.StockEntry;
 import net.sf.jmoney.stocks.model.StockEntryInfo;
 import net.sf.jmoney.stocks.model.StockInfo;
 
 
 public class InvestmentImporter implements IQifImporter {
 
 	/**
 	 * A local copy of all bank accounts in the current session, stored by name.
 	 * Before use accountMap must be initialized by calling the buildAccountMap
 	 * method
 	 */
 	private Map<String, CapitalAccount> accountMap = new HashMap<String, CapitalAccount>();
 
 	/**
 	 * A local copy of all categories in the current session, stored by name.
 	 * Before use this must be initialized by calling the buildCategoryMap
 	 * method
 	 */
 	private Map<String, IncomeExpenseAccount> categoryMap = new HashMap<String, IncomeExpenseAccount>();
 
 	/**
 	 * Creates a temporary map of all the accounts in the given session using
 	 * the account's name as the key.
 	 */
 	private void buildAccountMap(Session session) {
 		for (Account account: session.getAccountCollection()) {
 			if (account instanceof CapitalAccount) {
 				CapitalAccount capitalAccount = (CapitalAccount)account;
 				addToCapitalMap(capitalAccount);
 			}
 		}
 	}
 
 	private void addToCapitalMap(CapitalAccount capitalAccount) {
 		accountMap.put(capitalAccount.getName(), capitalAccount);
 		for (CapitalAccount subAccount: capitalAccount.getSubAccountCollection()) {
 			addToCapitalMap(subAccount);
 		}
 	}
 
 	/**
 	 * Creates a temporary map of all the categories in the given session using
 	 * the categories' names as keys.
 	 */
 	private void buildCategoryMap(Session session) {
 		for (Account account: session.getAccountCollection()) {
 			if (account instanceof IncomeExpenseAccount) {
 				IncomeExpenseAccount category = (IncomeExpenseAccount) account;
 				addToCategoryMap(category);
 			}
 		}
 	}
 
 	private void addToCategoryMap(IncomeExpenseAccount category) {
 		categoryMap.put(category.getName(), category);
 		for (IncomeExpenseAccount subCategory: category.getSubAccountCollection()) {
 			addToCategoryMap(subCategory);
 		}
 	}
 
 	public String importData(QifFile qifFile, Session session) {
 
 		buildAccountMap(session);
 		buildCategoryMap(session);
 
 		for (QifAccount qifAccount : qifFile.accountList) {
 
 			if (qifAccount.getInvstTransactions().size() == 0) {
 				// No investment transactions, so don't process
 				continue;
 			}
 			
 			StockAccount account = getStockAccount(qifAccount.getName(), session);
 
 //			account.setStartBalance(qifAccount.startBalance);
 
 			importAccount(session, account, qifAccount.getInvstTransactions());
 		}
 
 		return "some investment transactions";
 	}
 
 	/**
 	 * Imports an account from a QIF-file.
 	 * <P>
 	 * As soon as a split category, memo, or amount is found when one has
 	 * already been specified for the split, a new split is created. If split
 	 * lines are specified then any category specified in the 'L' line is
 	 * ignored.  The first split is put into the entry that had been initially
 	 * created for the category.
 	 * <P>
 	 * Split entries that involve transfers are complicated.  One or more of the
 	 * splits in a split entry may be a transfer.  When a transfer is in a split,
 	 * the QIF export of the other account in the transfer will not show the split.
 	 * It will show only a simple transfer.  (At least, that is how MS-Money exports
 	 * QIF data).  When we see a transfer, and we find a match indicating that the
 	 * transfer is already is the datastore, we must see whether either end of the
 	 * transfer is a split transaction and we must be sure to keep the split entries.
 	 * Normally, when we find a transfer is already in the datastore, we simply leave
 	 * it there and delete our transaction.  However, if our transaction is a split
 	 * entry then we instead keep our transaction and delete the other transaction.
 	 * In the latter case, it is possible that additional data has been set in the
 	 * other transaction and we must copy that data across to our transaction.
 	 * <P>
 	 * If the transaction in this account is split, and there are one or more transfers
 	 * in the split entries, and if the other account in a transfer has already been imported,
 	 * then an entry will have been entered into this account for the transfer amount of that
 	 * split.  If there are multiple transfers in the split then multiple entries will exist
 	 * in this account.  All of those entries and their transactions must be deleted.
 	 */
 	private void importAccount(Session session, StockAccount account,
 			List<QifInvstTransaction> transactions) {
 		
 		// TODO: This should come from the account????
 		Currency currency = session.getDefaultCurrency();
 
 		Account miscIncAccount = getCategory("Ameritrade - Misc Income", session);
 		Account interestIncomeAccount = getCategory("Interest - Ameritrade", session);
 		Account stockSplitAccount = getStockAccount("Stock Split - Ameritrade", session);
 		
 		for (QifInvstTransaction qifTransaction : transactions) {
 			// Create a new transaction
 			Transaction transaction = session.createTransaction();
 
 			System.out.println("Processing " + qifTransaction.getAction());
 			if (qifTransaction.getAction().equals("MargInt")) {
 			System.out.println("Processing " + qifTransaction.getAction());
 			}
 			
 			// Add the first entry for this transaction and set the account
 			QIFEntry firstEntry = transaction.createEntry().getExtension(QIFEntryInfo.getPropertySet(), true);
 			firstEntry.setAccount(account);
 
 			transaction.setDate(convertDate(qifTransaction.getDate()));
 
 			// Get amount for all cases except script issues, which don't have amounts.
 			long amount = 0;
 			if (!qifTransaction.getAction().equals("ScrIssue")
 					&& !qifTransaction.getAction().equals("ShrsIn")
 					&& !qifTransaction.getAction().equals("ShrsOut")) {
 				// There will be no amount if the sale is really because the shares
 				// are deemed worthless.
 				if (qifTransaction.getAmount() != null) {
 					amount = adjustAmount(qifTransaction.getAmount(), currency);
 					System.out.println("amount: " + amount);
 				}
 			}
 			
 			firstEntry.setReconcilingState(qifTransaction.getStatus());
 			firstEntry.setMemo(qifTransaction.getMemo());
 
 			if (qifTransaction.getAction().equals("ShrsIn") || qifTransaction.getAction().equals("ShrsOut")) {
 				// For time being, just do as a purchase or sale for zero.
 				firstEntry.setAmount(0);
 				
 		        // Find the security
 		        String security = qifTransaction.getSecurity();
 		        Stock stock = findStock(session, security);		        
 				
 		        Long quantity = stock.parse(qifTransaction.getQuantity());
 		        
 	        	StockEntry saleEntry = transaction.createEntry().getExtension(StockEntryInfo.getPropertySet(), true);
 	        	saleEntry.setAccount(account);
 	        	
 	        	if (qifTransaction.getAction().equals("ShrsIn")) {
 	        		saleEntry.setAmount(quantity);
 	        	} else {
 	        		saleEntry.setAmount(-quantity);
 	        	}
 	        	
 	        	saleEntry.setStock(stock);
 	        	saleEntry.setStockChange(true);
 			} else if (qifTransaction.getAction().equals("Buy") || qifTransaction.getAction().equals("Sell")) {
 				if (qifTransaction.getAction().equals("Sell")) {
 					firstEntry.setAmount(amount);
 				} else {
 					firstEntry.setAmount(-amount);
 				}
 				
 		        // Find the security
 		        String security = qifTransaction.getSecurity();
 		        Stock stock = findStock(session, security);		        
 				
 		        Long quantity = stock.parse(qifTransaction.getQuantity());
 		        
 	        	StockEntry saleEntry = transaction.createEntry().getExtension(StockEntryInfo.getPropertySet(), true);
 	        	saleEntry.setAccount(account);
 	        	
 	        	if (qifTransaction.getAction().equals("Buy")) {
 	        		saleEntry.setAmount(quantity);
 	        	} else {
 	        		saleEntry.setAmount(-quantity);
 	        	}
 	        	
 	        	saleEntry.setStock(stock);
 	        	saleEntry.setStockChange(true);
 	        	
 	        	BigDecimal c = new BigDecimal(9.99);
 	        	
 				if (qifTransaction.getCommission() != null) {
 	        		StockEntry commissionEntry = transaction.createEntry().getExtension(StockEntryInfo.getPropertySet(), true);
 	        		commissionEntry.setAccount(account.getCommissionAccount());
 					commissionEntry.setAmount(adjustAmount(qifTransaction.getCommission().min(c), currency));
 	        		commissionEntry.setStock(stock);
 
 	        		if (qifTransaction.getCommission().compareTo(new BigDecimal(9.99)) > 0) {
 		        		StockEntry salesFeeEntry = transaction.createEntry().getExtension(StockEntryInfo.getPropertySet(), true);
 		        		salesFeeEntry.setAccount(account.getTax1Account());
 						salesFeeEntry.setAmount(adjustAmount(qifTransaction.getCommission().subtract(c), currency));
 		        		salesFeeEntry.setStock(stock);
 		        	}
 	        	}
 	        	
 			} else if (qifTransaction.getAction().equals("Div")) {
 				firstEntry.setAmount(amount);
 				Entry dividendEntry = transaction.createEntry();
 				dividendEntry.setAccount(account.getDividendAccount());
 				dividendEntry.setAmount(-amount);
 			} else if (qifTransaction.getAction().equals("IntInc")) {
 				firstEntry.setAmount(amount);
 				Entry interestEntry = transaction.createEntry();
 				interestEntry.setAccount(interestIncomeAccount);
 				interestEntry.setAmount(-amount);
 			} else if (qifTransaction.getAction().equals("MiscInc") || qifTransaction.getAction().equals("XIn")) {
 				firstEntry.setAmount(amount);
 				Entry transferEntry = transaction.createEntry();
 				if (qifTransaction.getTransferAccount() == null) {
 					transferEntry.setAccount(miscIncAccount);
 				} else {
 					transferEntry.setAccount(findCategory(session, qifTransaction.getTransferAccount()));
 				}
 				transferEntry.setAmount(-amount);
 			} else if (qifTransaction.getAction().equals("MiscExp") || qifTransaction.getAction().equals("MargInt") || qifTransaction.getAction().equals("XOut")) {
 				firstEntry.setAmount(-amount);
 				Entry transferEntry = transaction.createEntry();
 				if (qifTransaction.getTransferAccount() == null) {
 					transferEntry.setAccount(interestIncomeAccount);
 				} else {
 					transferEntry.setAccount(findCategory(session, qifTransaction.getTransferAccount()));
 				}
 				if (qifTransaction.getAction().equals("MargInt")) {
 					transferEntry.setMemo("margin interest");
 				} else if (qifTransaction.getAction().equals("MiscExp")) {
 					transferEntry.setMemo(qifTransaction.getMemo());
 				} else if (qifTransaction.getAction().equals("XOut")) {
 					transferEntry.setMemo(qifTransaction.getMemo());
 				}
				transferEntry.setAmount(-amount);
 			} else if (qifTransaction.getAction().equals("ScrIssue")) {
 				// For a stock split, the share arrive as though income paid as share
 				// for an income account.  The entry for the source of the shares is
 				// associated with the shares already in this account.
 		        // Find the security
 		        String security = qifTransaction.getSecurity();
 		        Stock stock = findStock(session, security);		        
 				
 		        Long quantity = stock.parse(qifTransaction.getQuantity());
 
 		        int ratio = 1;
 		        if (qifTransaction.getMemo().equals("2:1 stock split")) {
 		        	ratio = 2;
 		        }
 		        
 	        	StockEntry firstEntryAsStock = firstEntry.getExtension(StockEntryInfo.getPropertySet(), true);
 	        	firstEntryAsStock.setStock(stock);
 	        	firstEntryAsStock.setStockChange(true);
 	        	firstEntryAsStock.setAmount(quantity);
 
 	        	StockEntry otherEntry = transaction.createEntry().getExtension(StockEntryInfo.getPropertySet(), true);
 	        	otherEntry.setAccount(stockSplitAccount);
         		otherEntry.setAmount(-quantity);
 	        	otherEntry.setStock(stock);
 	        	otherEntry.setStockChange(true);
 			} else {
 				System.out.println("unknown type");
 			}
 			
 			// Process the category
 //			if (qifTransaction.getSplits().size() == 0) {
 //				// Add the second entry for this transaction
 //				Entry secondEntry = transaction.createEntry();
 //
 //				secondEntry.setAmount(-amount);
 //
 //				Account category = findCategory(session, qifTransaction.getCategory());
 //				secondEntry.setAccount(category);
 //				if (category instanceof CapitalAccount) {
 //					// isTransfer = true;
 //				} else {
 //					IncomeExpenseAccount incomeExpenseCategory = (IncomeExpenseAccount)category;
 //					if (incomeExpenseCategory.isMultiCurrency()) {
 //						secondEntry.setIncomeExpenseCurrency(currency);
 //					} else {
 //						// Quicken categories are (I think) always multi-currency.
 //						// This means that under the quicken model, all expenses are
 //						// in the same currency as the account from which the expense came.
 //						// For example, I am visiting a customer in Europe and I incur a
 //						// business expense in Euro, but I charge to my US dollar billed
 //						// credit card.  Under the JMoney model, the expense category for the
 //						// client can be set to 'Euro only' and the actual cost in Euro may 
 //						// be entered, resulting an expense report for the European client that
 //						// has all amounts in Euro exactly matching the receipts.
 //						// The Quicken model, however, is problematic.  The expense shows
 //						// up in US dollars.  The report may translate at some exchange rate,
 //						// but the amounts on the expense report will then not match the
 //						// receipts.
 //						// This gives us a problem in this import.  If the currency of the
 //						// bank account does not match the currency of the category then we do
 //						// not have sufficient information.  Quicken only gives us the amount
 //						// in the currency of the bank account.
 //						if (!incomeExpenseCategory.getCurrency().equals(currency)) {
 //							// TODO: resolve this.  For time being, the amount is set even though
 //							// the currency is different, thus assuming an exchange rate of
 //							// one to one.
 //						}
 //					}
 //				}
 //			}	
 
 
 			// Split transactions.
 			for (QifSplitTransaction qifSplit : qifTransaction.getSplits()) {					
 				Entry splitEntry = transaction.createEntry();
 				splitEntry.setAccount(findCategory(session, qifSplit.getCategory()));
 				splitEntry.setMemo(qifSplit.getMemo());
 				splitEntry.setAmount(-adjustAmount(qifSplit.getAmount(), currency));
 			}
 
 			// If we have a transfer then we need to search through the other
 			// account to see if a matching entry exists and then keep only one
 			// (if one is a split transaction, we should keep that one, otherwise
 			// it does not matter which we keep so keep the old one).
 
 			for (Entry entry : transaction.getEntryCollection()) {
 				if (!entry.equals(firstEntry)) {
 
 					// Force a category in each account.
 					// This is required by the JMoney data model.
 					if (entry.getAccount() == null) {
 						entry.setAccount(getCategory("Unknown Category", session));
 					}
 
 					if (entry.getAccount() instanceof IncomeExpenseAccount) {
 						// If this entry is for a multi-currency account,
 						// set the currency to be the same as the currency for this
 						// bank account.
 						if (((IncomeExpenseAccount)entry.getAccount()).isMultiCurrency()) {
 							entry.setIncomeExpenseCurrency(currency);
 						}
 					}
 
 					if (entry.getAccount() instanceof CapitalAccount) {
 						Entry oldEntry = findMatch(account, transaction.getDate(), -entry.getAmount(), transaction);
 						if (oldEntry != null) {
 							if (transaction.hasMoreThanTwoEntries()) {
 								// Our transaction is split.  The other should
 								// not be, so delete the other transaction,
 								// leaving only our transaction.
 								Transaction  oldTransaction = oldEntry.getTransaction(); 
 								if (oldTransaction.hasMoreThanTwoEntries()) {
 									// We have problems.  Both are split.
 									// For time being, leave both, but we should
 									// alert the user or something.  Actually, this
 									// should not happen (at least MS-Money does not seem
 									// to allow this to happen), so perhaps it does not
 									// really matter what we do.
 								} else {
 									// Copy some of the properties across from the old
 									// before we delete it.
 
 									Entry oldOtherEntry = oldTransaction.getOther(oldEntry);									
 									entry.setCheck(oldOtherEntry.getCheck());
 									entry.setValuta(oldOtherEntry.getValuta());
 
 									session.deleteTransaction(oldTransaction);
 								}
 							} else {
 								// Delete the transaction that we have created,
 								// leaving only the existing transaction.
 								session.deleteTransaction(transaction);
 
 								// We must stop processing because this transaction
 								// is now dead.
 								break;
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private Stock findStock(Session session, String security) {
 		Stock stock = null;
 		if (security.length() != 0) {
 			for (Commodity commodity : session.getCommodityCollection()) {
 				if (commodity instanceof Stock) {
 					Stock eachStock = (Stock)commodity;
 					if (security.equals(eachStock.getSymbol())) {
 						stock = eachStock;
 						break;
 					}
 				}
 			}
 			if (stock == null) {
 				// Create it.  The name is not available in the import file,
 				// so for time being we use the symbol as the name.
 				stock = session.createCommodity(StockInfo.getPropertySet());
 				stock.setName(security);
 				stock.setSymbol(security);
 			}
 		}
 		return stock;
 	}
 
 	private Date convertDate(QifDate date) {
 		Calendar calendar = Calendar.getInstance();
 		calendar.clear();
 		calendar.set(Calendar.YEAR, date.getYear());
 		calendar.set(Calendar.MONTH, date.getMonth()-1);
 		calendar.set(Calendar.DAY_OF_MONTH, date.getDay());
 		return calendar.getTime();
 	}
 
 	private long adjustAmount(BigDecimal amount, Currency currency) {
 		// TODO: revisit this method.
 		return amount.movePointRight(currency.getDecimals()).longValue();
 	}
 
 	/**
 	 * Find an entry in this account that has the given date and amount.
 	 * 
 	 * @param date
 	 * @param amount
 	 * @param ourTransaction
 	 *            When we look for a match, ignore this transaction. This is the
 	 *            transaction we have added so of course it will match. We are
 	 *            looking for another transaction that matches.
 	 */
 	private Entry findMatch(CapitalAccount capAccount, Date date, long amount, Transaction ourTransaction) {
 		for (Entry otherEntry : capAccount.getEntries()) {
 			Transaction otherTransaction = otherEntry.getTransaction();
 			if (!otherTransaction.equals(ourTransaction)) {
 				// Transaction dates must match
 				// Entry amounts must match
 				if (otherTransaction.getDate().equals(date)
 						&& otherEntry.getAmount() == amount) {
 					return otherEntry;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the account in "session" associated with the category specified
 	 * by "line".
 	 */
 	private Account findCategory(Session session, QifCategoryLine qifCategoryLine) {
 		switch (qifCategoryLine.getType()) {
 		case CategoryType:
 			return getCategory(qifCategoryLine.getName(), session);
 		case SubCategoryType:
 			IncomeExpenseAccount category = getCategory(qifCategoryLine.getName(), session);
 			return getSubcategory(qifCategoryLine.getSubCategoryName(), category);
 		case TransferType:
 			return getStockAccount(qifCategoryLine.getName(), session);
 		}
 		throw new RuntimeException("bad case");
 	}
 
 	/**
 	 * Returns the account with the specified name. If there is no account in
 	 * the session with that name then a new account is created
 	 * 
 	 * @param name
 	 *            the name of account to get
 	 * @param session
 	 *            the session to check for the account
 	 */
 	private StockAccount getStockAccount(String name, Session session) {
 		// Test to see if we have an account with the same name in our map
 		CapitalAccount account = accountMap.get(name);
 		// If not then create a new account, set the name and add it to the map
 		if (account == null) {
 			account = session.createAccount(StockAccountInfo.getPropertySet());
 			account.setName(name);
 			accountMap.put(name, account);
 		}
 
 		if (!(account instanceof StockAccount)) {
 			// TODO: process error properly
 			if (QIFPlugin.DEBUG) System.out.println("account is not a currency account");
 			throw new RuntimeException("account is not a currency account");
 		}
 		
 		return (StockAccount)account;
 	}
 
 	/**
 	 * Returns the category with the specified name. If it doesn't exist a new
 	 * category will be created.
 	 */
 	private IncomeExpenseAccount getCategory(String name, Session session) {
 		IncomeExpenseAccount category;
 		category = categoryMap.get(name);
 		if (category == null) {
 			category = session.createAccount(IncomeExpenseAccountInfo.getPropertySet());
 			category.setName(name);
 			categoryMap.put(name, category);
 		}
 		return category;
 	}
 
 	/**
 	 * Returns the sub-category with the specified name. If it doesn't exist a
 	 * new sub-category will be created. We don't use a map for sub categories
 	 * instead we just iterate through them trying to find a match.
 	 */
 	private IncomeExpenseAccount getSubcategory(
 			String name,
 			IncomeExpenseAccount category) {
 
 		for (IncomeExpenseAccount subcategory : category.getSubAccountCollection()) {
 			if (subcategory.getName().equals(name))
 				return subcategory;
 		}
 
 		IncomeExpenseAccount subcategory = category.createSubAccount();
 		subcategory.setName(name);
 		return subcategory;
 	}
 }
