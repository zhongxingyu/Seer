 /*
  *  JMoney - A Personal Finance Manager
  *  Copyright (c) 2002 Johann Gyger <johann.gyger@switzerland.org>
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 
 package net.sf.jmoney.qif;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Reader;
 import java.text.DateFormat;
 import java.text.NumberFormat;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.fields.BankAccountInfo;
 import net.sf.jmoney.fields.IncomeExpenseAccountInfo;
 import net.sf.jmoney.fields.TransactionInfo;
 import net.sf.jmoney.model2.Account;
 import net.sf.jmoney.model2.CapitalAccount;
 import net.sf.jmoney.model2.Currency;
 import net.sf.jmoney.model2.CurrencyAccount;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.IncomeExpenseAccount;
 import net.sf.jmoney.model2.Session;
 import net.sf.jmoney.model2.Transaction;
 import net.sf.jmoney.qif.format.QIFCashTransaction;
 import net.sf.jmoney.qif.format.QIFHeader;
 import net.sf.jmoney.qif.format.QIFSplitTransaction;
 
 /**
  * This class is responsible for loading and exporting Quicken interchange
  * format files.
  * 
  * This class is designed to work best when importing complete accounts.
  * 
  * When dealing with transfers between two bank accounts this QIFFileFormat
  * first creates a single-entry transaction in the originating account. If a
  * corresponding single-entry transaction can be found in the destination
  * account then the two transactions will be deleted and replaced by one
  * double-entry transaction. This way it is possible to properly import a
  * collection of QIF files exported from another accounting package e.g. MS
  * Money.
  * 
  * All transfers between bank accounts and Income and Expense Accounts (i.e.
  * payments assigned to categories) are added as double-entry transactions.
  * 
  * This class can not handle investment transactions.
  * 
  * This class does not include any UI code. Associated UI components should be
  * included elsewhere.
  * 
  * Exporting of QIF Files is untested.
  */
 public class QIFFileFormat implements FileFormat {
 	static NumberFormat number = NumberFormat.getInstance(Locale.US);
 
 	private static SimpleDateFormat df = (SimpleDateFormat) DateFormat
 			.getDateInstance();
 
 	private Calendar calendar = Calendar.getInstance();
 
 	/**
 	 * Indicates whether the QIF file uses the mm/dd/yyyy or dd/mm/yyyy format.
 	 * Set by the <code>isUSDateFormat</code> method
 	 */
 	private boolean usesUSDates;
 
 	/**
 	 * A local copy of all bank accounts in the current session, stored by name.
 	 * Before use accountMap must be initialised by calling the buildAccountMap
 	 * method
 	 */
 	private Map<String, CapitalAccount> accountMap;
 
 	/**
 	 * A local copy of all categories in the current session, stored by name.
 	 * Before use this must be initialised by calling the buildCategoryMap
 	 * method
 	 */
 	private Map<String, IncomeExpenseAccount> categoryMap;
 
 	/**
 	 * Creates a new QIFFileFormat.
 	 */
 	public QIFFileFormat() {
 		number.setMinimumFractionDigits(2);
 		number.setMaximumFractionDigits(2);
 	}
 
 	/**
 	 * Imports a QIF-file.
 	 */
 	public void importFile(Session session, File qifFile) {
 
 		try {
 			BufferedReader buffer = new BufferedReader(new FileReader(qifFile));
 			String line = buffer.readLine();
 
 			buildCategoryMap(session);
 			buildAccountMap(session);
 
 			usesUSDates = isUSDateFormat(qifFile);
 
 			// import transactions of a non-investment account
 			if (line.startsWith(QIFHeader.ACCT_BANK)
 					|| line.startsWith(QIFHeader.ACCT_CASH)
 					|| line.startsWith(QIFHeader.ACCT_MS97)
 					|| line.startsWith(QIFHeader.ACCT_CARD)
 					|| line.startsWith(QIFHeader.ACCT_ASSET)
 					|| line.startsWith(QIFHeader.ACCT_LIAB)) {
 
 				// Assume we are adding a new Bank Account and that the
 				// first transaction contains the opening balance and
 				// the name of the account under the category.
 				String accountName = qifFile.getName();
 				long startBalance = 0;
 				Currency currency = session.getDefaultCurrency();
 				
 				do {
 					line = buffer.readLine();
 					switch (line.charAt(0)) {
 					case QIFCashTransaction.TOTAL:
 						startBalance = parseAmount(line, currency);
 						break;
 					case QIFCashTransaction.CATEGORY:
 						// Assume line is formated as "L[Account Name]"
 						accountName = line.substring(2, line.length() - 1);
 						break;
 					default:
 						break;
 					}
 				} while (line.charAt(0) != QIFCashTransaction.END);
 
 				CapitalAccount account = getAccount(accountName, session);
 				if (!(account instanceof CurrencyAccount)) {
 					// TODO: process error properly
 					if (QIFPlugin.DEBUG) System.out.println("account is not a currency account");
 					throw new RuntimeException("account is not a currency account");
 				}
 				
 				CurrencyAccount currencyAccount = (CurrencyAccount)account;
 				currencyAccount.setStartBalance(startBalance);
 				
 				importAccount(session, currencyAccount, buffer);
 			}
 
 			// import transactions of a investment account
 			else {
 				JMoneyPlugin.log(new RuntimeException("Cannot import " + line.substring(0)));
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Scans a QIF file to determine whether the date format is US or
 	 * EU ie: mm/dd/yyyy or dd/mm/yyyy
 	 * <P>
 	 * If a number is found in the day or month field that is
 	 * greater than 12 then we know immediately the date format.
 	 * <P>
 	 * If we get to the end of the file with no such number greater
 	 * than 12 then we assume the dates are in order and return the
 	 * format that would put the dates in order.
 	 * <P>
 	 * If both formats put the dates in order then we give up
 	 * and assume US format.  This scenario would be extremely
 	 * unlikely.
 	 * 
 	 * @param qifFile
 	 *            the file to scan
 	 * @return
 	 * @throws IOException
 	 */
 	private boolean isUSDateFormat(File file) throws IOException {
 		String line;
 		Reader reader = new FileReader(file);
 		BufferedReader r = new BufferedReader(reader);
 		
 		int lastDateIfUS = 0;
 		int lastDateIfEU = 0;
 		boolean usDatesInOrder = true;
 		boolean euDatesInOrder = true;
 		
 		try {
 			line = r.readLine();
 			while (line != null) {
 				if (line.charAt(0) == 'D') {
 					StringTokenizer st = new StringTokenizer(line, "D/\'");
 					int number1 = Integer.parseInt(st.nextToken().trim());
 					if (number1 > 12) {
 						return false;
 					}
 					int number2 = Integer.parseInt(st.nextToken().trim());
 					if (number2 > 12) {
 						return true;
 					}
 					
 					int year = Integer.parseInt(st.nextToken().trim());
 					
 					int dateIfUS = number1*100 + number2 + year*10000;
 					if (dateIfUS < lastDateIfUS) {
 						usDatesInOrder = false;
 					}
 					lastDateIfUS = dateIfUS;
 					
 					int dateIfEU = number1*100 + number2 + year*10000;
 					if (dateIfEU < lastDateIfEU) {
 						euDatesInOrder = false;
 					}
 					lastDateIfEU = dateIfEU;
 				}
				line = r.readLine();
 			}
 		} finally {
 			r.close();
 			reader.close();
 		}
 
 		// No month or day above 12, but if the dates are in
 		// order only when we assume EU format then indicate
 		// EU format, otherwise assume US format.
 		return (usDatesInOrder || !euDatesInOrder);
 	}
 
 	/**
 	 * Creates a temporary map of all the accounts in the given session using
 	 * the account's name as the key.
 	 */
 	private void buildAccountMap(Session session) {
 		if (accountMap == null)
 			accountMap = new HashMap<String, CapitalAccount>();
 		for (Account account: session.getAccountCollection()) {
 			if (account instanceof CapitalAccount) {
 				CapitalAccount capitalAccount = (CapitalAccount)account;
 				accountMap.put(account.getName(), capitalAccount);
 			}
 		}
 	}
 
 	/**
 	 * Creates a temporary map of all the categories in the given session using
 	 * the categories' names as keys.
 	 */
 	private void buildCategoryMap(Session session) {
 		if (categoryMap == null)
 			categoryMap = new HashMap<String, IncomeExpenseAccount>();
 		for (Account account: session.getAccountCollection()) {
 			if (account instanceof IncomeExpenseAccount) {
 				IncomeExpenseAccount category = (IncomeExpenseAccount) account;
 				categoryMap.put(category.getName(), category);
 			}
 		}
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
 	private void importAccount(Session session, CurrencyAccount account,
 			BufferedReader buffer) throws IOException {
 		String line;
 		Currency currency = session.getDefaultCurrency();
 
 		// At this point we should have read the account information from
 		// the first transaction/header. The current line should == '^'.
 		// the next line is usually the date 'D...' although any of the
 		// other fields is also valid
 
 		line = buffer.readLine();   // get first line of first transaction
 		while (line != null) {
 			
 			// Create a new transaction
 			Transaction transaction = session.createTransaction();
 
 			// Add the first entry for this transaction and set the account
 			QIFEntry firstEntry = (QIFEntry)transaction.createEntry().getExtension(QIFEntryInfo.getPropertySet(), true);
 			firstEntry.setAccount(account);
 
 			// Add the second entry for this transaction
 			Entry secondEntry = transaction.createEntry();
 
 			// Set the entry into which split entry data is put.
 			// Initially, this is the second entry.
 			Entry splitEntry = secondEntry;
 
 			boolean categoryInSplitAlreadySpecified = false;
 			boolean memoInSplitAlreadySpecified = false;
 			boolean amountInSplitAlreadySpecified = false;
 
 			String address = null;
 			
 			// Loop through the file reading each line until we get to the
 			// end of the transaction '^' or the end of the file (line==null)
 
 			while (true) {
 				if (line == null)
 					break;  // Should not happen because always a ^ after last transaction
 				char firstChar = line.charAt(0);
 				if (firstChar == QIFCashTransaction.END)
 					break;
 				switch (firstChar) {
 				case QIFCashTransaction.DATE:
 					transaction.setDate(parseDate(line));
 					break;
 				case QIFCashTransaction.TOTAL:
 					long amount = parseAmount(line, currency);
 					firstEntry.setAmount(amount);
 					secondEntry.setAmount(-amount);
 					break;
 				case QIFCashTransaction.CLEARED:
 					firstEntry.setReconcilingState(parseStatus(line));
 					break;
 				case QIFCashTransaction.NUMBER:
 					firstEntry.setCheck(line.substring(1));
 					break;
 				case QIFCashTransaction.PAYEE:
 					String payee = line.substring(1);
 					firstEntry.setDescription(payee);
 					break;
 				case QIFCashTransaction.CATEGORY:
 				{
 					Account category = parseCategory(session, line);
 					secondEntry.setAccount(category);
 					if (category instanceof CapitalAccount) {
 						// isTransfer = true;
 					} else {
 						IncomeExpenseAccount incomeExpenseCategory = (IncomeExpenseAccount)category;
 						if (incomeExpenseCategory.isMultiCurrency()) {
 							secondEntry.setIncomeExpenseCurrency(currency);
 						} else {
 							// Quicken categories are (I think) always multi-currency.
 							// This means that under the quicken model, all expenses are
 							// in the same currency as the account from which the expense came.
 							// For example, I am visiting a customer in Europe and I incur a
 							// business expense in Euro, but I charge to my US dollar billed
 							// credit card.  Under the JMoney model, the expense category for the
 							// client can be set to 'Euro only' and the actual cost in Euro may 
 							// be entered, resulting an expense report for the European client that
 							// has all amounts in Euro exactly matching the reciepts.
 							// The Quicken model, however, is problematic.  The expense shows
 							// up in US dollars.  The report may translate at some exchange rate,
 							// but the amounts on the expense report will then not match the
 							// reciepts.
 							// This gives us a problem in this import.  If the currency of the
 							// bank account does not match the currency of the category then we do
 							// not have sufficient information.  Quicken only gives us the amount
 							// in the currency of the bank account.
 							if (!incomeExpenseCategory.getCurrency().equals(currency)) {
 								// TODO: resolve this.  For time being, the amount is set even though
 								// the currency is different, thus assuming an exchange rate of
 								// one to one.
 							}
 						}
 					}
 					break;
 				}	
 				case QIFCashTransaction.MEMO:
 					firstEntry.setMemo(line.substring(1));
 					break;
 					
 				case QIFCashTransaction.ADDRESS:
 					if (address == null) {
 						address = line.substring(1);
 					} else {
 						address = address + '\n' + line.substring(1); 
 					}
 					break;
 					
 				case QIFSplitTransaction.CATEGORY:
 					if (categoryInSplitAlreadySpecified) {
 						// A split category has already been specified, so assume
 						// we are staring a new split.
 						splitEntry = transaction.createEntry();
 						
 						categoryInSplitAlreadySpecified = false;
 						memoInSplitAlreadySpecified = false;
 						amountInSplitAlreadySpecified = false;
 					}
 					
 					splitEntry.setAccount(parseCategory(session, line));
 				
 					categoryInSplitAlreadySpecified = true;
 					break;
 				
 				case QIFSplitTransaction.MEMO:
 					if (memoInSplitAlreadySpecified) {
 						// A split memo has already been specified, so assume
 						// we are staring a new split.
 						splitEntry = transaction.createEntry();
 
 						categoryInSplitAlreadySpecified = false;
 						memoInSplitAlreadySpecified = false;
 						amountInSplitAlreadySpecified = false;
 					}
 					
 					if (splitEntry.getAccount() instanceof CapitalAccount) {
 						splitEntry.setMemo(line.substring(1));
 					} else {
 						splitEntry.setDescription(line.substring(1));
 					}
 				
 					memoInSplitAlreadySpecified = true;
 					break;
 				
 				case QIFSplitTransaction.AMOUNT:
 					if (amountInSplitAlreadySpecified) {
 						// A split memo has already been specified, so assume
 						// we are staring a new split.
 						splitEntry = transaction.createEntry();
 						
 						categoryInSplitAlreadySpecified = false;
 						memoInSplitAlreadySpecified = false;
 						amountInSplitAlreadySpecified = false;
 					}
 					
 					splitEntry.setAmount(-parseAmount(line, currency));
 				
 					amountInSplitAlreadySpecified = true;
 					break;
 				
 				default:
 					break;
 				}
 
 				line = buffer.readLine();
 			}
 
 			firstEntry.setAddress(address);
 			
 			// If we have a transfer then we need to search through the other
 			// account to see if a matching entry exists and then keep only one
 			// (if one is a split transaction, we should keep that one, otherwise
 			// it does not matter which we keep so keep the old one).
 			
 			for (Iterator iter = transaction.getEntryCollection().iterator(); iter.hasNext(); ) {
 				Entry entry = (Entry)iter.next();
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
 
 			line = buffer.readLine();   // get first line of next transaction
 		}
 	}
 
 	/**
 	 * Find an entry in this account that has the given date and amount.
 	 * <P>
 	 * 
 	 * @param date
 	 * @param amount
 	 * @param ourTransaction
 	 *            When we look for a match, ignore this transaction. This is the
 	 *            transaction we have added so of course it will match. We are
 	 *            looking for another transaction that matches.
 	 */
 	private Entry findMatch(CapitalAccount capAccount, Date date, long amount, Transaction ourTransaction) {
 		Collection otherEntries = capAccount.getEntries();
 		for (Iterator iter = otherEntries.iterator(); iter.hasNext();) {
 			Entry otherEntry = (Entry) iter.next();
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
 	 * @param line
 	 * @param currency
 	 * @return
 	 */
 	private long parseAmount(String line, Currency currency) {
 		short factor = currency.getScaleFactor();
 		Number n = number.parse(line, new ParsePosition(1));
 		return n == null ? 0 : Math.round(n.doubleValue() * factor);
 	}
 
 	/**
 	 * @param line
 	 * @return 'X', '*', or ' '
 	 */
 	private char parseStatus(String line) {
 		char c = line.charAt(1);
 		if (c == 'x' || c == 'X') {
 			return 'X';
 		} else if (c == '*') {
 			return '*';
 		} else {
 			return ' ';
 		}
 	}
 
 	/**
 	 * Returns the account in "session" associated with the category specified
 	 * by "line".
 	 */
 	private Account parseCategory(Session session, String line) {
 		if (line.charAt(1) == '[') {
 			// transfer
 			String accountName = line.substring(2, line.length() - 1);
 			return getAccount(accountName, session);
 		} else {
 			// assumption: a category consists at least of one char
 			// either "LCategory" or "LCategory:Subcategory"
 			int colon;
 			for (colon = 1; colon < line.length(); colon++)
 				if (line.charAt(colon) == ':')
 					break;
 			if (colon == line.length()) {
 				// "LCategory"
 				String categoryName = line.substring(1);
 				return getCategory(categoryName, session);
 			} else {
 				// "LCategory:Subcategory
 				String categoryName = line.substring(1, colon);
 				String subcategoryName = line.substring(colon + 1);
 				IncomeExpenseAccount category = getCategory(categoryName,
 						session);
 				return getSubcategory(subcategoryName, category);
 			}
 		}
 	}
 
 	public void exportAccount(Session session, CapitalAccount capitalAccount,
 			File file) {
 		if (!(capitalAccount instanceof CurrencyAccount)) {
 			// TODO: process other account types
 			return;
 		}
 		CurrencyAccount account = (CurrencyAccount) capitalAccount;
 
 		try {
 			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
 
 			// Get the entries in date order.
 			// The entries must be in date order because the date of the
 			// first entry is used as the date of the opening balance record.
 			Collection entries = account.getSortedEntries(TransactionInfo
 					.getDateAccessor(), false);
 
 			// write header
 			writeln(writer, "!Type:Bank");
 
 			// write first entry (containing the start balance)
 			if (!entries.isEmpty()) {
 				Entry entry = (Entry) entries.iterator().next();
 				String dateString = formatDate(entry.getTransaction().getDate());
 				if (dateString != null)
 					writeln(writer, dateString);
 			}
 			writeln(writer, "T"
 					+ formatAmount(account.getStartBalance(), account));
 			writeln(writer, "CX");
 			writeln(writer, "POpening Balance");
 			writeln(writer, "L[" + account.getName() + "]");
 			writeln(writer, "^");
 
 			// write entries
 			for (Iterator entryIter = entries.iterator(); entryIter.hasNext();) {
 				Entry entry = (Entry) entryIter.next();
 				// date
 				String dateString = formatDate(entry.getTransaction().getDate());
 				if (dateString != null)
 					writeln(writer, dateString);
 				// memo
 				if (entry.getMemo() != null)
 					writeln(writer, "M" + entry.getMemo());
 
 				// status
 				QIFEntry ourEntry = (QIFEntry) entry
 						.getExtension(QIFEntryInfo.getPropertySet());
 				if (ourEntry.getReconcilingState() == '*')
 					writeln(writer, "C*");
 				else if (ourEntry.getReconcilingState() == 'X')
 					writeln(writer, "CX");
 
 				// amount
 				writeln(writer, "T" + formatAmount(entry.getAmount(), account));
 				// check
 				if (entry.getCheck() != null)
 					writeln(writer, "N" + entry.getCheck());
 				// description
 				if (entry.getDescription() != null)
 					writeln(writer, "P" + entry.getDescription());
 				// category
 				Account category = entry.getAccount();
 				if (category != null) {
 					if (category instanceof CapitalAccount)
 						writeln(writer, "L[" + category.getName() + "]");
 					else {
 						writeln(writer, "L" + category.getFullAccountName());
 					}
 					// TODO: Split Entries
 				}
 				// end of entry
 				writeln(writer, "^");
 			}
 			writer.close();
 		} catch (IOException e) {
 		}
 	}
 
 	private String formatAmount(long amount, CurrencyAccount account) {
 		return number.format(((double) amount)
 				/ account.getCurrency().getScaleFactor());
 	}
 
 	/**
 	 * Parses the date string and returns a date object: <br>
 	 * 11/2/98 ->> 11/2/1998 <br>
 	 * 3/15'00 ->> 3/15/2000
 	 */
 	private Date parseDate(String line) {
 		try {
 			StringTokenizer st = new StringTokenizer(line, "D/\'");
 			int day, month, year;
 			if (usesUSDates) {
 				month = Integer.parseInt(st.nextToken().trim());
 				day = Integer.parseInt(st.nextToken().trim());
 			} else {
 				day = Integer.parseInt(st.nextToken().trim());
 				month = Integer.parseInt(st.nextToken().trim());
 			}
 			year = Integer.parseInt(st.nextToken().trim());
 			if (year < 100) {
 				if (line.indexOf("'") < 0)
 					year = year + 1900;
 				else
 					year = year + 2000;
 			}
 			calendar.clear();
 			calendar.setLenient(false);
 			calendar.set(year, month - 1, day, 0, 0, 0);
 			return calendar.getTime();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private String formatDate(Date date) {
 		if (date == null)
 			return null;
 		calendar.setTime(date);
 		df.applyPattern("dd/mm/yyyy");
 		return df.format(date);
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
 	private CapitalAccount getAccount(String name, Session session) {
 		// Test to see if we have an account with the same name in our map
 		CapitalAccount account = accountMap.get(name);
 		// If not then create a new account, set the name and add it to the map
 		if (account == null) {
 			account = session.createAccount(BankAccountInfo.getPropertySet());
 			account.setName(name);
 			accountMap.put(name, account);
 		}
 		return account;
 	}
 
 	/**
 	 * Returns the category with the specified name. If it doesn't exist a new
 	 * category will be created.
 	 */
 	private IncomeExpenseAccount getCategory(String name, Session session) {
 		IncomeExpenseAccount category;
 		category = categoryMap.get(name);
 		if (category == null) {
 			category = (IncomeExpenseAccount) session.createAccount(IncomeExpenseAccountInfo.getPropertySet());
 			category.setName(name);
 			categoryMap.put(name, category);
 		}
 		return category;
 	}
 
 	/**
 	 * Returns the subcategory with the specified name. If it doesn't exist a
 	 * new subcategory will be created. We don't use a map for sub categories
 	 * instead we just iterate through them trying to find a match.
 	 */
 	private IncomeExpenseAccount getSubcategory(
 			String name,
 			IncomeExpenseAccount category) {
 		IncomeExpenseAccount subcategory = null;
 
 		for (Iterator iter = category.getSubAccountCollection().iterator(); iter.hasNext();) {
 			subcategory = (IncomeExpenseAccount) iter.next();
 			if (subcategory.getName().equals(name))
 				return subcategory;
 		}
 		subcategory = category.createSubAccount();
 		subcategory.setName(name);
 		return subcategory;
 	}
 
 	/**
 	 * Writes a line and jumps to a new one.
 	 */
 	private void writeln(BufferedWriter writer, String line) throws IOException {
 		writer.write(line);
 		writer.newLine();
 	}
 
 }
