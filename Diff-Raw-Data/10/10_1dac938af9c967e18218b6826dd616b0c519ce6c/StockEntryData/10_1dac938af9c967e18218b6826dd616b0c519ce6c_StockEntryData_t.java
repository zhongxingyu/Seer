 package net.sf.jmoney.stocks.pages;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.Iterator;
 
 import net.sf.jmoney.entrytable.EntryData;
 import net.sf.jmoney.entrytable.InvalidUserEntryException;
 import net.sf.jmoney.model2.Currency;
 import net.sf.jmoney.model2.CurrencyAccount;
 import net.sf.jmoney.model2.DataManager;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.Transaction.EntryCollection;
 import net.sf.jmoney.stocks.model.Stock;
 import net.sf.jmoney.stocks.model.StockAccount;
import net.sf.jmoney.stocks.model.StockEntry;
import net.sf.jmoney.stocks.model.StockEntryInfo;
 import net.sf.jmoney.stocks.pages.StockEntryRowControl.TransactionType;
 
 import org.eclipse.core.runtime.Assert;
 
 public class StockEntryData extends EntryData {
 
 	private StockAccount account;
 	
 	private TransactionType transactionType;
 
 	private Entry mainEntry;
 	private Entry dividendEntry;
 	private Entry withholdingTaxEntry;
 	
 	/**
 	 * the entry for the commission, or null if this is not a purchase or sale
 	 * transaction or if no commission account is configured for this stock
 	 * account because commissions are never charged on any purchases or sales
 	 * in this account, and possibly null if there can be a commission but none
 	 * has been entered for this entry
 	 */
 	private Entry commissionEntry;
 	
 	private Entry tax1Entry;
 	private Entry tax2Entry;
 	private Entry purchaseOrSaleEntry;
 	private Entry transferEntry;
 
 	private boolean unknownTransactionType;
 
 	public StockEntryData(Entry entry, DataManager dataManager) {
 		super(entry, dataManager);
 
 
 		// Note that there are two versions of this object for every row.
 		// One contains the committed entry and the other contains the entry
 		// being edited inside a transaction.  If this is the new entry row
 		// and is the committed version then entry will be null, so we can't
 		// analyze it and we can't determine the account.
 		
 		// TODO We should consider merging the two instances into one.
 		
 		// TODO Call this on-demand.
 		if (entry != null) {
 			account = (StockAccount)entry.getAccount();
 			analyzeTransaction();
 		}
 	}
 
 	private void analyzeTransaction() {
 		/*
 		 * Analyze the transaction to see which type of transaction this is.
 		 */
 
 		/*
 		 * If just one entry then this is not a valid transaction, so must be
 		 * a new transaction.  We set the transaction type to null which means
 		 * no selection will be set in the transaction type combo.
 		 */
 		if (getEntry().getTransaction().getEntryCollection().size() == 1) {
			mainEntry = getEntry().getTransaction().getEntryCollection().iterator().next();
 			transactionType = null;
 		} else {
 
 			for (Entry entry: getEntry().getTransaction().getEntryCollection()) {
 				if (entry.getAccount() == account.getDividendAccount()) {
 					if (dividendEntry != null) {
 						unknownTransactionType = true;
 					}
 					dividendEntry = entry;
 				} else if (entry.getAccount() == account.getWithholdingTaxAccount()) {
 					if (withholdingTaxEntry != null) {
 						unknownTransactionType = true;
 					}
 					withholdingTaxEntry = entry;
 				} else if (entry.getAccount() == account.getCommissionAccount()) {
 					if (commissionEntry != null) {
 						unknownTransactionType = true;
 					}
 					commissionEntry = entry;
 				} else if (entry.getAccount() == account.getTax1Account()) {
 					if (tax1Entry != null) {
 						unknownTransactionType = true;
 					}
 					tax1Entry = entry;
 				} else if (entry.getAccount() == account.getTax2Account()) {
 					if (tax2Entry != null) {
 						unknownTransactionType = true;
 					}
 					tax2Entry = entry;
 				} else if (entry.getAccount() == account) {
 					if (entry.getCommodity() instanceof Stock) {
 						if (purchaseOrSaleEntry != null) {
 							unknownTransactionType = true;
 						}
 						purchaseOrSaleEntry = entry;
 					} else if (entry.getCommodity() instanceof Currency) {  //TODO: check for actual currency of account.
 						if (mainEntry != null) {
 							unknownTransactionType = true;
 						}
 						mainEntry = entry;
 					}
 				} else if (entry.getAccount() instanceof CurrencyAccount) {
 					if (transferEntry != null) {
 						unknownTransactionType = true;
 					}
 					transferEntry = entry;
 				} else {
 					unknownTransactionType = true;
 				}
 			}
 
 			if (unknownTransactionType) {
 				transactionType = TransactionType.Other;
 			} else if (dividendEntry != null
 					&& commissionEntry == null
 					&& tax1Entry == null
 					&& tax2Entry == null
 					&& purchaseOrSaleEntry == null
 					&& transferEntry == null) {
 				transactionType = TransactionType.Dividend;
 			} else if (dividendEntry == null
 					&& withholdingTaxEntry == null
 					&& purchaseOrSaleEntry != null
 					&& transferEntry == null) {
 				if (purchaseOrSaleEntry.getAmount() >= 0) {
 					transactionType = TransactionType.Buy;
 				} else {
 					transactionType = TransactionType.Sell;
 				}
 			} else if (dividendEntry == null
 					&& withholdingTaxEntry == null
 					&& commissionEntry == null
 					&& tax1Entry == null
 					&& tax2Entry == null
 					&& purchaseOrSaleEntry == null
 					&& transferEntry != null) {
 				transactionType = TransactionType.Transfer;
 			} else {
 				transactionType = TransactionType.Other;
 			}
 		}
 	}
 
 	public void forceTransactionToDividend() {
 		transactionType = TransactionType.Dividend;
 
 		EntryCollection entries = getEntry().getTransaction().getEntryCollection();
 		for (Iterator<Entry> iter = entries.iterator(); iter.hasNext(); ) {
 			Entry entry = iter.next();
 			if (entry != mainEntry
 					&& entry != dividendEntry
 				&& entry != withholdingTaxEntry) {
 				iter.remove();
 			}
 		}
 		
 		commissionEntry = null;
 		tax1Entry = null;
 		tax2Entry = null;
 		purchaseOrSaleEntry = null;
 		transferEntry = null;
 		
 		if (dividendEntry == null) {
 			dividendEntry = entries.createEntry();
 			dividendEntry.setAccount(account.getDividendAccount());
 		}
 
 		// withholdingTaxEntry is optional and will be created when needed.
 
 		dividendEntry.setAmount(-mainEntry.getAmount());
 	}
 
 	public void forceTransactionToBuy() {
 		forceTransactionToBuyOrSell(TransactionType.Buy);
 	}
 	
 	public void forceTransactionToSell() {
 		forceTransactionToBuyOrSell(TransactionType.Sell);
 	}
 	
 	private void forceTransactionToBuyOrSell(TransactionType transactionType) {
 		this.transactionType = transactionType;
 			
 		EntryCollection entries = getEntry().getTransaction().getEntryCollection();
 		for (Iterator<Entry> iter = entries.iterator(); iter.hasNext(); ) {
 			Entry entry = iter.next();
 			if (entry != mainEntry
 					&& entry != commissionEntry
 					&& entry != tax1Entry
 					&& entry != tax2Entry
 					&& entry != purchaseOrSaleEntry) {
 				iter.remove();
 			}
 		}
 		
 		dividendEntry = null;
 		withholdingTaxEntry = null;
 		transferEntry = null;
 		
 		if (purchaseOrSaleEntry == null) {
 			purchaseOrSaleEntry = entries.createEntry();
 			purchaseOrSaleEntry.setAccount(account);
			StockEntry stockEntry = purchaseOrSaleEntry.getExtension(StockEntryInfo.getPropertySet(), true);
			stockEntry.setStockChange(true);
 		}
 
 		// Commission, tax 1, and tax 2 entries may be null in this transaction type.
 		// They are created when needed to connect the controls.
 
 		// TODO: What is our strategy on changing values to keep
 		// the transaction balanced.  Quicken has a dialog box that
 		// asks the user what to adjust (with a 'recommended' choice
 		// that in my experience is never the correct choice!).
 		
 //		dividendEntry.setAmount(-mainEntry.getAmount());
 	}
 
 	public void forceTransactionToTransfer() {
 		transactionType = TransactionType.Transfer;
 
 		EntryCollection entries = getEntry().getTransaction().getEntryCollection();
 		for (Iterator<Entry> iter = entries.iterator(); iter.hasNext(); ) {
 			Entry entry = iter.next();
 			if (entry != mainEntry
 					&& entry != transferEntry) {
 				iter.remove();
 			}
 		}
 		
 		dividendEntry = null;
 		withholdingTaxEntry = null;
 		commissionEntry = null;
 		tax1Entry = null;
 		tax2Entry = null;
 		purchaseOrSaleEntry = null;
 		
 		if (transferEntry == null) {
 			transferEntry = entries.createEntry();
 		}
 		transferEntry.setAmount(-mainEntry.getAmount());
 	}
 
 	public void forceTransactionToCustom() {
 		transactionType = TransactionType.Other;
 
 		/*
 		 * This method is not so much a 'force' as a 'set'.  The other 'force' methods
 		 * have to modify the transaction, including the lose of information, in order
 		 * to transform the transaction to the required type.  This method does not need
 		 * to change the transaction data at all.  It does adjust the UI to give the user
 		 * full flexibility.
 		 * 
 		 * Note that the user may edit the transaction so that it matches one of the
 		 * types (buy, sell, dividend etc).  In that case, the transaction will appear
 		 * as that type, not as a custom type, if it is saved and re-loaded.
 		 */
 		
 		// Must be at least one entry
 		EntryCollection entries = getEntry().getTransaction().getEntryCollection();
 		if (entries.size() == 1) {
 			entries.createEntry();
 		}
 
 		/*
 		 * Forget the special entries. It may be that these would be useful to
 		 * keep in case the user decides to go back to one of the set
 		 * transaction types. However, the user may edit these entries, or
 		 * delete them, and it is too complicated to worry about the
 		 * consequences.
 		 */
 		dividendEntry = null;
 		withholdingTaxEntry = null;
 		commissionEntry = null;
 		tax1Entry = null;
 		tax2Entry = null;
 		purchaseOrSaleEntry = null;
 		transferEntry = null;
 	}
 
 	public TransactionType getTransactionType() {
 		return transactionType;
 	}
 
 	public boolean isPurchaseOrSale() {
 		return transactionType == TransactionType.Buy
 		|| transactionType == TransactionType.Sell;
 	}
 
 	public boolean isDividend() {
 		return transactionType == TransactionType.Dividend;
 	}
 
 	public Entry getDividendEntry() {
 		Assert.isTrue(isDividend());
 		return dividendEntry;
 	}
 
 	/**
 	 * 
 	 * @return the entry that represents the withholding tax, or null if the
 	 *         withholding tax is not applicable to this transaction
 	 *         (transaction is not a dividend transaction or no withholding tax
 	 *         account has been set for the account) but never null if a
 	 *         withholding tax amount could be entered for this entry even if
 	 *         none has been
 	 */
 	public Entry getWithholdingTaxEntry() {
 		assert(transactionType == TransactionType.Dividend);
 		
 		if (withholdingTaxEntry == null && account.getWithholdingTaxAccount() != null) {
 			withholdingTaxEntry = getEntry().getTransaction().createEntry();
 			withholdingTaxEntry.setAccount(account.getWithholdingTaxAccount());
 		}
 		return withholdingTaxEntry;
 	}
 
 	/**
 	 * @return the entry in the transaction that represents the gain or loss in
 	 *         the number of shares, or null if this is not a purchase or sale
 	 *         transaction
 	 */
 	public Entry getPurchaseOrSaleEntry() {
 		return purchaseOrSaleEntry;
 	}
 
 	/**
 	 * @return the entry in the transaction that contains the commission, or
 	 *         null if this is not a purchase or sale transaction or if no
 	 *         commission account is configured for this stock account because
 	 *         commissions are never charged on any purchases or sales in this
 	 *         account, but never null if a commission can be entered for this
 	 *         entry even if none has been
 	 */
 	public Entry getCommissionEntry() {
 		assert(isPurchaseOrSale());
 		
 		if (commissionEntry == null && account.getCommissionAccount() != null) {
 			commissionEntry = getEntry().getTransaction().createEntry();
 			commissionEntry.setAccount(account.getCommissionAccount());
 		}
 
 		return commissionEntry;
 	}
 
 	/**
 	 * @return the entry in the transaction that represents the
 	 * 		tax 1 amount, or null if this is not a purchase or sale
 	 * 		transaction or if no tax 1 is configured for the account,
 	 * 		but never null if a tax 1 amount could be entered for this
 	 * 		entry even if none has been
 	 */
 	public Entry getTax1Entry() {
 		assert(isPurchaseOrSale());
 		
 		if (tax1Entry == null && account.getTax1Account() != null) {
 			tax1Entry = getEntry().getTransaction().createEntry();
 			tax1Entry.setAccount(account.getTax1Account());
 		}
 
 		return tax1Entry;
 	}
 
 	/**
 	 * @return the entry in the transaction that represents the
 	 * 		tax 2 amount, or null if this is not a purchase or sale
 	 * 		transaction or if no tax 2 is configured for the account,
 	 * 		but never null if a tax 2 amount could be entered for this
 	 * 		entry even if none has been
 	 */
 	public Entry getTax2Entry() {
 		assert(isPurchaseOrSale());
 		
 		if (tax2Entry == null && account.getTax2Account() != null) {
 			tax2Entry = getEntry().getTransaction().createEntry();
 			tax2Entry.setAccount(account.getTax2Account());
 		}
 
 		return tax2Entry;
 	}
 
 	/**
 	 * @return the entry in the transaction that is the other entry
 	 * 		in a transfer transaction, or null if this is not a transfer
 	 * 		transaction
 	 */
 	public Entry getTransferEntry() {
 		return transferEntry;
 	}
 
 	/*
 	 * The price is calculated, not stored in the model. This method
 	 * calculates the share price from the data in the model.  It does
 	 * this by adding up all the cash entries to get the gross proceeds
 	 * or cost and then dividing by the number of shares.
 	 * 
 	 * @return the calculated price to four decimal places, or null
 	 * 		if the price cannot be calculated (e.g. if the share quantity
 	 * 		is zero)
 	 */
 	public BigDecimal calculatePrice() {
 		assert(isPurchaseOrSale());
 
 		BigDecimal totalShares = BigDecimal.valueOf(purchaseOrSaleEntry.getAmount())
 				.movePointLeft(3);
 
 		long totalCash = 0;
 		for (Entry eachEntry: getEntry().getTransaction().getEntryCollection()) {
 			if (eachEntry.getCommodity() instanceof Currency) {
 				totalCash += eachEntry.getAmount();
 			}
 		}
 		
 		BigDecimal price = null;
		if (totalCash != 0 && totalShares.compareTo(BigDecimal.ZERO) != 0) {
 			/*
 			 * Either we gain cash and lose stock, or we lose cash and gain
 			 * stock. Hence we need to negate to get a positive value.
 			 */
 			price = BigDecimal.valueOf(-totalCash).movePointLeft(2).divide(totalShares, 4, RoundingMode.HALF_UP);
 		}
 		
 		return price;
 	}
 
 	public void specificValidation() throws InvalidUserEntryException {
 		if (transactionType == null) {
 			throw new InvalidUserEntryException("No transaction type selected.", null);
 		}
 		
 		/*
 		 * Check for zero amounts. Some fields may be zeroes (for example, commissions and
 		 * withheld taxes), others may not (for example, quantity of stock sold).
 		 * 
 		 * We do leave entries with zero amounts.  This makes the code simpler
 		 * because the transaction is already set up for the transaction type,
 		 * and it is easier to determine the transaction type.  
 		 * 
 		 * It is possible that the total proceeds of a sale are zero.  Anyone who
 		 * has disposed of shares in a sub-prime mortgage company in order to
 		 * claim the capital loss will know that the commission may equal the sale
 		 * price.  It is probably good that the transaction still shows up in
 		 * the cash entries list for the account.
 		 */
 		switch (transactionType) {
 		case Buy:
 		case Sell:
 			if (purchaseOrSaleEntry.getAmount() == 0) {
 				throw new InvalidUserEntryException("The quantity of stock in a purchase or sale cannot be zero.", null);
 			}
 			if (commissionEntry != null 
 					&& commissionEntry.getAmount() == 0) {
 				mainEntry.getTransaction().deleteEntry(commissionEntry);
 				commissionEntry = null;
 			}
 			if (tax1Entry != null 
 					&& tax1Entry.getAmount() == 0) {
 				mainEntry.getTransaction().deleteEntry(tax1Entry);
 				tax1Entry = null;
 			}
 			if (tax2Entry != null 
 					&& tax2Entry.getAmount() == 0) {
 				mainEntry.getTransaction().deleteEntry(tax2Entry);
 				tax2Entry = null;
 			}
 			break;
 		case Dividend:
 			if (dividendEntry.getAmount() == 0) {
 				throw new InvalidUserEntryException("The amount of a dividend cannot be zero.", null);
 			}
 			if (withholdingTaxEntry != null 
 					&& withholdingTaxEntry.getAmount() == 0
 					&& withholdingTaxEntry.getMemo() == null) {
 				mainEntry.getTransaction().deleteEntry(withholdingTaxEntry);
 				withholdingTaxEntry = null;
 			}
 			break;
 		case Transfer:
 			if (transferEntry.getAmount() == 0) {
 				throw new InvalidUserEntryException("The amount of a transfer cannot be zero.", null);
 			}
 			break;
 		case Other:
 			// We don't allow any amounts to be zero except the listed entry
 			// (the listed entry is used to ensure a transaction appears in this
 			// list even if the transaction does not result in a change in the cash
 			// balance).
 			Entry mainEntry = getEntry();
 			if (mainEntry.getTransaction().getEntryCollection().size() == 1) {
 				// TODO: create another entry when 'other' selected and don't allow it to be
 				// deleted, thus this check is not necessary.
 				// TODO: should not be 'other' when no transaction has been selected
 				// (should be null)
 				throw new InvalidUserEntryException("Must have another entry.", null);
 			}
 			for (Entry entry : mainEntry.getTransaction().getEntryCollection()) {
 				if (entry != mainEntry) {
 					if (entry.getAmount() == 0) {
 						throw new InvalidUserEntryException("The amount of an entry in this transaction cannot be zero.", null);
 					}
 				}
 			}
 			break;
 		}
 	}
 }
