 /*
  *
  *  JMoney - A Personal Finance Manager
  *  Copyright (c) 2002 Johann Gyger <johann.gyger@switzerland.org>
  *  Copyright (c) 2004 Nigel Westbury <westbury@users.sourceforge.net>
  *
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
  *
  */
 
 package net.sf.jmoney.stocks.model;
 
 import java.text.NumberFormat;
 import java.text.ParseException;
 
 import net.sf.jmoney.fields.IAmountFormatter;
 import net.sf.jmoney.model2.CapitalAccount;
 import net.sf.jmoney.model2.Commodity;
 import net.sf.jmoney.model2.Currency;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.IListManager;
 import net.sf.jmoney.model2.IObjectKey;
 import net.sf.jmoney.model2.IValues;
 import net.sf.jmoney.model2.IncomeExpenseAccount;
 import net.sf.jmoney.model2.ListKey;
 
 /**
  * The data model for an account.
  */
 public class StockAccount extends CapitalAccount {
 
 	/**
 	 * Guaranteed non-null because the session default currency is
 	 * set by default.
 	 */
 	IObjectKey currencyKey;
 
 	protected String brokerageFirm = null;
 
 	protected String accountNumber = null;
 
 	/**
 	 * A table that allows the commission to be calculated based
 	 * on the amount of any purchase.  Null indicates no rate table
 	 * is available in which case the user must enter the
 	 * amount.
 	 */
 	protected RatesTable buyCommissionRates;
 
 	/**
 	 * A table that allows the commission to be calculated based
 	 * on the amount of any sale.  Null indicates no rate table
 	 * is available in which case the user must enter the
 	 * amount.
 	 */
 	protected RatesTable sellCommissionRates;
 
 	/**
 	 * The income account into which all dividends from stock in this
 	 * account are entered.
 	 */
 	IObjectKey dividendAccountKey;
 
 	/**
 	 * The income account into which any tax withholding on dividends from stock in this
 	 * account are entered.
 	 */
 	IObjectKey withholdingTaxAccountKey;
 	
 	/**
 	 * The expense account into which all buy and sell commissions
 	 * are entered.
 	 */
 	IObjectKey commissionAccountKey;
 
 	/**
 	 * The name of this tax.  For example, in the UK a transfer
 	 * stamp is charged so you might want to call this tax
 	 * "Transfer Stamp".
 	 */
 	protected String tax1Name = "Tax 1";
 	
 	/**
 	 * A table that allows a tax to be calculated from the
 	 * amount of any purchase.  Null indicates no rate table
 	 * is available in which case the user must enter the
 	 * amount.
 	 * 
 	 * For example, in the UK a transfer stamp is charged
 	 * on every purchase/sale.
 	 */
 	protected RatesTable tax1Rates;
 
 	/**
 	 * The expense account into which all amounts of
 	 * tax 1 are entered.
 	 */
 	IObjectKey tax1AccountKey;
 	
 	/**
 	 * The name of this tax.  For example, in the UK a PTM Levy
 	 * is charged so you might want to call this tax
 	 * "PTM Levy".
 	 */
 	protected String tax2Name = "Tax 2";
 	
 	/**
 	 * A table that allows a tax to be calculated from the
 	 * amount of any purchase.  Null indicates no rate table
 	 * is available in which case the user must enter the
 	 * amount.
 	 * 
 	 * For example, in the UK a PTM levy is charged
 	 * on every purchase/sale.
 	 */
 	protected RatesTable tax2Rates;
 	
 	/**
 	 * The expense account into which all amounts of
 	 * tax 1 are entered.
 	 */
 	IObjectKey tax2AccountKey;
 	
 	/**
 	 * The full constructor for a StockAccount object.  This constructor is called
 	 * only be the datastore when loading data from the datastore.  The properties
 	 * passed to this constructor must be valid because datastores should only pass back
 	 * values that were previously saved from a StockAccount object.  So, for example,
 	 * we can be sure that a non-null name and currency are passed to this constructor.
 	 * 
 	 * @param name the name of the account
 	 */
 	public StockAccount(
 			IObjectKey objectKey,
 			ListKey parentKey,
 			String name,
 			IListManager<CapitalAccount> subAccounts,
 			String abbreviation,
 			String comment,
 			IObjectKey currencyKey,
 			String brokerageFirm,
 			String accountNumber,
 			IObjectKey dividendAccountKey,
 			IObjectKey withholdingTaxAccountKey,
 			String tax1Name,
 			String tax2Name,
 			IObjectKey commissionAccountKey,
 			IObjectKey tax1AccountKey,
 			IObjectKey tax2AccountKey,
 			RatesTable buyCommissionRates,
 			RatesTable sellCommissionRates,
 			RatesTable tax1Rates,
 			RatesTable tax2Rates,
 			
 			IValues extensionValues) {
 			
 		super(objectKey, parentKey, name, subAccounts, abbreviation, comment, extensionValues);
 		
 		/*
 		 * The currency for this account is not allowed to be null, because
 		 * users of this class may assume it to be non-null and would not know
 		 * how to handle this account if it were null.
 		 * 
 		 * If null is passed, set to the default currency for the session.
 		 * This is guaranteed to be never null.
 		 */
 		if (currencyKey != null) {
 			this.currencyKey = currencyKey;
 		} else {
 			this.currencyKey = objectKey.getSession().getDefaultCurrency().getObjectKey();
 		}
 		
         this.brokerageFirm = brokerageFirm;
         this.accountNumber = accountNumber;
         this.dividendAccountKey = dividendAccountKey;
         this.withholdingTaxAccountKey = withholdingTaxAccountKey;
         this.tax1Name = tax1Name;
         this.tax2Name = tax2Name;
         this.commissionAccountKey = commissionAccountKey;
         this.tax1AccountKey = tax1AccountKey;
         this.tax2AccountKey = tax2AccountKey;
 		this.buyCommissionRates = buyCommissionRates;
 		this.sellCommissionRates = sellCommissionRates;
 		this.tax1Rates = tax1Rates;
 		this.tax2Rates = tax2Rates;
 	}
 
 	public StockAccount(
 			IObjectKey objectKey, 
 			ListKey parent) { 
 		super(objectKey, parent);
 		
 		// Overwrite the default name with our own default name.
 		name = "New Stock Account";
 		
 		this.currencyKey = objectKey.getSession().getDefaultCurrency().getObjectKey();
 		this.brokerageFirm = null;
 		this.accountNumber = null;
 		
 		this.buyCommissionRates = new RatesTable();
 		this.sellCommissionRates = new RatesTable();
 		this.tax1Rates = new RatesTable();
 		this.tax2Rates = new RatesTable();
 	}
 
 	@Override
 	protected String getExtendablePropertySetId() {
 		return "net.sf.jmoney.stocks.stockAccount";
 	}
 	
 	public Currency getCurrency() {
         return (Currency)currencyKey.getObject();
 	}
 
 	/**
 	 * Returns the commodity represented by the amount in this
 	 * entry.  If this entry represents an addition of stock to
 	 * the account or a removal of stock from the account then
 	 * the amount represents the amount of that stock.  Otherwise
 	 * the amount represents an amount in the currency for the account.
 	 */
 	@Override
 	public Commodity getCommodity(Entry entry) {
 		/*
 		 * If a stock is set as the commodity for an entry in a stock account
 		 * then the entry represents a gain or loss of stock in the account.
 		 * Note that the stock field may be set in income and expense accounts
 		 * (such as a dividend account) and in those cases the entry does not
 		 * represent a change in the amount of stock, only that it is associated
 		 * with that stock.
 		 */
 		// TODO: remove this test at some time because getCommodity should never
 		// return null.
 		if (entry.getCommodity() != null) {
 			return entry.getCommodity();
 		} else {
 			return getCurrency();
 		}
 	}
 	
 	/**
 	 * @return the bank name of this account.
 	 */
 	public String getBrokerageFirm() {
 		return brokerageFirm;
 	}
 
 	/**
 	 * @return the account number of this account.
 	 */
 	public String getAccountNumber() {
 		return accountNumber;
 	}
 
 	public String getTax1Name() {
 		return tax1Name;
 	}
 
 	public String getTax2Name() {
 		return tax2Name;
 	}
 
 	/**
 	 * @return the account that contains the dividend income for
 	 * 		stock in this account
 	 */
 	public IncomeExpenseAccount getDividendAccount() {
         return dividendAccountKey == null
         ? null
         		: (IncomeExpenseAccount)dividendAccountKey.getObject();
 	}
 
 	public IncomeExpenseAccount getWithholdingTaxAccount() {
         return withholdingTaxAccountKey == null
         ? null
         		: (IncomeExpenseAccount)withholdingTaxAccountKey.getObject();
 	}
 
 	public IncomeExpenseAccount getCommissionAccount() {
         return commissionAccountKey == null
         ? null
         		: (IncomeExpenseAccount)commissionAccountKey.getObject();
 	}
 
 	public IncomeExpenseAccount getTax1Account() {
         return tax1AccountKey == null
         ? null
         		: (IncomeExpenseAccount)tax1AccountKey.getObject();
 	}
 
 	public IncomeExpenseAccount getTax2Account() {
         return tax2AccountKey == null
         ? null
         		: (IncomeExpenseAccount)tax2AccountKey.getObject();
 	}
 
 	public RatesTable getBuyCommissionRates() {
 		return buyCommissionRates;
 	}
 
 	public RatesTable getSellCommissionRates() {
 		return sellCommissionRates;
 	}
 
 	public RatesTable getTax1Rates() {
 		return tax1Rates;
 	}
 
 	public RatesTable getTax2Rates() {
 		return tax2Rates;
 	}
 
 	public void setCurrency(Currency aCurrency) {
 	    if (aCurrency == null) throw new IllegalArgumentException();
         Currency oldCurrency = getCurrency();
 		currencyKey = aCurrency.getObjectKey();
 
 		// Notify the change manager.
 		processPropertyChange(StockAccountInfo.getCurrencyAccessor(), oldCurrency, aCurrency);
 	}
 
 	/**
 	 * @param aBank the name of this account.
 	 */
 
 	public void setBrokerageFirm(String brokerageFirm) {
         String oldBrokerageFirm = this.brokerageFirm;
 		this.brokerageFirm = brokerageFirm;
 
 		// Notify the change manager.
 		processPropertyChange(StockAccountInfo.getBrokerageFirmAccessor(), oldBrokerageFirm, brokerageFirm);
 	}
 
 	public void setAccountNumber(String accountNumber) {
         String oldAccountNumber = this.accountNumber;
         this.accountNumber = accountNumber;
 
 		// Notify the change manager.
 		processPropertyChange(StockAccountInfo.getAccountNumberAccessor(), oldAccountNumber, accountNumber);
 	}
 
 	public void setTax1Name(String newTaxName) {
         String oldTaxName = this.tax1Name;
         this.tax1Name = newTaxName;
 
 		// Notify the change manager.
 		processPropertyChange(StockAccountInfo.getTax1NameAccessor(), oldTaxName, newTaxName);
 	}
 
 	public void setTax2Name(String newTaxName) {
         String oldTaxName = this.tax2Name;
         this.tax2Name = newTaxName;
 
 		// Notify the change manager.
 		processPropertyChange(StockAccountInfo.getTax2NameAccessor(), oldTaxName, newTaxName);
 	}
 
 	public void setDividendAccount(IncomeExpenseAccount newAccount) {
 		IncomeExpenseAccount oldAccount = getDividendAccount();
 		dividendAccountKey = newAccount == null ? null : newAccount.getObjectKey();
 
 		// Notify the change manager.
 		processPropertyChange(StockAccountInfo.getDividendAccountAccessor(), oldAccount, newAccount);
 	}
 
 	public void setWithholdingTaxAccount(IncomeExpenseAccount newAccount) {
 		IncomeExpenseAccount oldAccount = getWithholdingTaxAccount();
 		withholdingTaxAccountKey = newAccount == null ? null : newAccount.getObjectKey();
 
 		// Notify the change manager.
 		processPropertyChange(StockAccountInfo.getWithholdingTaxAccountAccessor(), oldAccount, newAccount);
 	}
 
 	public void setCommissionAccount(IncomeExpenseAccount newAccount) {
 		IncomeExpenseAccount oldAccount = getCommissionAccount();
 		commissionAccountKey = newAccount == null ? null : newAccount.getObjectKey();
 
 		// Notify the change manager.
 		processPropertyChange(StockAccountInfo.getCommissionAccountAccessor(), oldAccount, newAccount);
 	}
 
 	public void setTax1Account(IncomeExpenseAccount newAccount) {
 		IncomeExpenseAccount oldAccount = getTax1Account();
 		tax1AccountKey = newAccount == null ? null : newAccount.getObjectKey();
 
 		// Notify the change manager.
 		processPropertyChange(StockAccountInfo.getTax1AccountAccessor(), oldAccount, newAccount);
 	}
 
 	public void setTax2Account(IncomeExpenseAccount newAccount) {
 		IncomeExpenseAccount oldAccount = getTax2Account();
 		tax2AccountKey = newAccount == null ? null : newAccount.getObjectKey();
 
 		// Notify the change manager.
 		processPropertyChange(StockAccountInfo.getTax2AccountAccessor(), oldAccount, newAccount);
 	}
 
 	public void setBuyCommissionRates(RatesTable newCommissionRates) {
         RatesTable oldCommissionRates = this.buyCommissionRates;
 		this.buyCommissionRates = newCommissionRates;
 
 		// Notify the change manager.
 		processPropertyChange(StockAccountInfo.getBuyCommissionRatesAccessor(), oldCommissionRates, newCommissionRates);
 	}
 
 	public void setSellCommissionRates(RatesTable newCommissionRates) {
         RatesTable oldCommissionRates = this.sellCommissionRates;
 		this.sellCommissionRates = newCommissionRates;
 
 		// Notify the change manager.
 		processPropertyChange(StockAccountInfo.getSellCommissionRatesAccessor(), oldCommissionRates, newCommissionRates);
 	}
 
 	public void setTax1Rates(RatesTable newTaxRates) {
         RatesTable oldTaxRates = this.tax1Rates;
 		this.tax1Rates = newTaxRates;
 
 		// Notify the change manager.
 		processPropertyChange(StockAccountInfo.getTax1RatesAccessor(), oldTaxRates, newTaxRates);
 	}
 
 	public void setTax2Rates(RatesTable newTaxRates) {
         RatesTable oldTaxRates = this.tax2Rates;
 		this.tax2Rates = newTaxRates;
 
 		// Notify the change manager.
 		processPropertyChange(StockAccountInfo.getTax2RatesAccessor(), oldTaxRates, newTaxRates);
 	}
 
 	/**
 	 * Returns an object that knows how to both format and parse stock prices.
 	 */
 	public IAmountFormatter getPriceFormatter() {
 		// There is currently only one implementation.  We may need to extend this
 		// if there is a requirement to have different formatters for stock depending
 		// on the currency/exchange of the stock.
 		
 		// This implementation formats all prices with four decimal places.
 		final int SCALE_FACTOR = 10000;
 		final NumberFormat numberFormat = NumberFormat.getNumberInstance();
 		numberFormat.setMaximumFractionDigits(4);
 		numberFormat.setMinimumFractionDigits(0);
 		
 		return new IAmountFormatter() {
 			public String format(long amount) {
 				double a = ((double) amount) / SCALE_FACTOR;
 				return numberFormat.format(a);
 			}
 
 			public long parse(String amountString) {
 				Number amount = new Double(0);
 				try {
 					amount = numberFormat.parse(amountString);
 				} catch (ParseException pex) {
 					// If bad user entry, leave as zero
 				}
 				return Math.round(
 						amount.doubleValue() * SCALE_FACTOR);
 			}
 		};
 	}
 
 	/**
 	 * Returns an object that knows how to both format and parse stock quantities.
 	 * 
 	 * If the stock is known then the stock will be used as the formatter.  However,
 	 * it is possible the user will enter the stock quantity before entering the
 	 * stock, in which case the account decides how the stock quantity is formatted.
 	 */
 	public IAmountFormatter getQuantityFormatter() {
 		// There is currently only one implementation.  We may need to extend this
 		// if there is a requirement to have different formatters for stock depending
 		// on the currency/exchange of the stock.
 		
 		// This implementation formats all quantities as numbers with three decimal places.
 		final int SCALE_FACTOR = 1000;
 		final NumberFormat numberFormat = NumberFormat.getNumberInstance();
 		numberFormat.setMaximumFractionDigits(3);
 		numberFormat.setMinimumFractionDigits(0);
 		
 		return new IAmountFormatter() {
 			public String format(long amount) {
 				double a = ((double) amount) / SCALE_FACTOR;
 				return numberFormat.format(a);
 			}
 
 			public long parse(String amountString) {
 				Number amount;
 				try {
 					amount = numberFormat.parse(amountString);
 				} catch (ParseException ex) {
 					// If bad user entry, return zero
 					amount = new Double(0);
 				}
 				return Math.round(amount.doubleValue() * SCALE_FACTOR);
 			}
 		};
 	}
 }
