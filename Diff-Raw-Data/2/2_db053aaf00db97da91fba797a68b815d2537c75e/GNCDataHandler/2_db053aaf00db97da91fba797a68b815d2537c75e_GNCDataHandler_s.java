 /** GnuCash for Android.
  *
  * Copyright (C) 2010 Rednus Limited http://www.rednus.co.uk
  * Copyright (C) 2010,2011 John Gray
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package rednus.gncandroid;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Comparator;
 import java.util.Currency;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.UUID;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Debug;
 import android.os.SystemClock;
 import android.util.Log;
 
 class InvalidDataException extends Exception {
 	public InvalidDataException() {
 	}
 
 	public InvalidDataException(String msg) {
 		super(msg);
 	}
 }
 
 /**
  * This class implements methods to read data file and create a data collection
  * and also methods to update the data file.
  * 
  * @author shyam.avvari
  * 
  */
 public class GNCDataHandler {
 	private static final String TAG = "GNCDataHandler"; // TAG for this activity
 	private GNCAndroid app; // Application
 	private DataCollection gncData = new DataCollection(); // The parsed book.
 	private SQLiteDatabase sqliteHandle;
 	private SharedPreferences sp;
 	// A SQL snippet to be used in the WHERE clause to select only the chosen accounts.
 	private String accountFilter;
 	// Two maps from preference keys to database "enums".
 	private TreeMap<String, String> accountPrefMapping;
 	private TreeMap<String, String> accountTypeMapping;
 	private TreeMap<String,Double>  commodityPrices;
 	private Resources res;
 	// The GUID of the chosen currency. This is a property of the data.
 	private String currencyGUID;
 	// A poor man's condition variable, used to allow AccountsActivity to detect data changes.
 	private long changeCount;
 
 	/**
 	 * Create the handler and populate DataCollection with information from the
 	 * data file given in the preferences.
 	 * 
 	 * @param app
 	 *            GNCAndroid Application reference
 	 * @throws Exception
 	 *         if there was a problem reading the data file.
 	 */
 	public GNCDataHandler(GNCAndroid app) throws Exception {
 		Cursor cursor;
 		this.app = app;
 
 		res = app.getResources();
 		sp = app.getSharedPreferences(GNCAndroid.SPN, Context.MODE_PRIVATE);
 		changeCount = SystemClock.uptimeMillis();
 
 		buildAccountMapping();
 		genAccountFilter();
 
 		sqliteHandle = SQLiteDatabase.openDatabase(sp.getString(res.getString(R.string.pref_data_file_key), null), null,
 				SQLiteDatabase.OPEN_READWRITE
 						| SQLiteDatabase.NO_LOCALIZED_COLLATORS);
 		Currency currency = Currency.getInstance(Locale.getDefault());
 		String ccode = currency.getCurrencyCode();
 
 		try {
 			cursor = sqliteHandle.rawQuery("select guid, mnemonic from commodities where namespace = 'CURRENCY';", null);
 		}
 		catch (Exception e) {
 			sqliteHandle.close();
 			sqliteHandle = null;
 			throw e;
 		}
 
 		try {
 			// There had better be at least one currency...
 			if (cursor.moveToNext()) {
 				// If it's the only one, easy.
 				currencyGUID = cursor.getString(cursor.getColumnIndex("guid"));
 				if (cursor.getCount() > 1) {
 					// If there are more than one, prefer the one given in the default Locale.
 					do {
 						String mnemonic = cursor.getString(cursor.getColumnIndex("mnemonic"));
 						if (mnemonic == ccode)
 							currencyGUID = cursor.getString(cursor.getColumnIndex("guid"));
 					} while (cursor.moveToNext());
 				}
 			}
 			else {
 				sqliteHandle.close();
 				sqliteHandle = null;
 				throw new InvalidDataException("Could not determine default currency.");
 			}
 		}
 		catch (Exception e) {
 			sqliteHandle.close();
 			sqliteHandle = null;
 			throw e;
 		}
 		finally {
 			cursor.close();
 		}
 
 		cursor = sqliteHandle.rawQuery("select * from books", null);
 		try {
 			if (cursor.moveToNext()) {
 				// CREATE TABLE books (guid text(32) PRIMARY KEY NOT NULL,
 				// root_account_guid text(32) NOT NULL, root_template_guid
 				// text(32) NOT NULL);
 				gncData.book.GUID = cursor.getString(cursor
 						.getColumnIndex("guid"));
 				gncData.book.rootAccountGUID = cursor.getString(cursor
 						.getColumnIndex("root_account_guid"));
 			}
 			cursor.close();
 
 			cursor = sqliteHandle.rawQuery("select accounts.guid as aguid,commodities.guid as cguid,* from accounts left outer join commodities on accounts.commodity_guid = commodities.guid", null);
 			Map<String, Commodity> currencies = new TreeMap<String, Commodity>(); // Map currency codes to Commodity objects.
 			while (cursor.moveToNext()) {
 				Account account = new Account();
 				// CREATE TABLE accounts (guid text(32) PRIMARY KEY NOT
 				// NULL, name text(2048) NOT NULL, account_type text(2048)
 				// NOT NULL, commodity_guid text(32), commodity_scu integer
 				// NOT NULL, non_std_scu integer NOT NULL, parent_guid
 				// text(32), code text(2048), description text(2048), hidden
 				// integer, placeholder integer);
 				account.GUID = cursor.getString(cursor
 						.getColumnIndex("aguid"));
 				account.name = cursor.getString(cursor
 						.getColumnIndex("name"));
 				account.type = cursor.getString(cursor
 						.getColumnIndex("account_type"));
 				account.parentGUID = cursor.getString(cursor
 						.getColumnIndex("parent_guid"));
 				account.code = cursor.getString(cursor
 						.getColumnIndex("code"));
 				String mnemonic = cursor.getString(cursor
                                                         .getColumnIndex("mnemonic"));
 				if (mnemonic == null)
 					// This is probably the root account.
 					account.commodity = null;
 				else {
 					account.commodity = currencies.get(mnemonic);
 					if (account.commodity == null) {
 						// This commodity hasn't been encountered yet. Create it.
 						account.commodity = new Commodity();
 						account.commodity.guid = cursor.getString(cursor
 								.getColumnIndex("cguid"));
 						account.commodity.space = cursor.getString(cursor
 								.getColumnIndex("namespace"));
 						account.commodity.quoteSource = cursor.getString(cursor
 								.getColumnIndex("quote_source"));
 						account.commodity.mnemonic = cursor.getString(cursor
 								.getColumnIndex("mnemonic"));
 						account.commodity.currency = Currency.getInstance(
 								account.commodity.mnemonic);
 						currencies.put(account.commodity.mnemonic, account.commodity);
 					}
 				}
 				account.description = cursor.getString(cursor
 						.getColumnIndex("description"));
 				account.placeholder = cursor.getInt(cursor
 						.getColumnIndex("placeholder")) != 0;
 
 				gncData.accounts.put(account.GUID, account);
 			}
 		}
 		catch (Exception e) {
 			sqliteHandle.close();
 			sqliteHandle = null;
 			throw e;
 		}
 		finally {
 			cursor.close();
 		}
 
 		gncData.completeCollection();
 
 		// This appears to slow it down (I hoped it would speed it up)
 		//if ( sp.getBoolean(app.res.getString(R.string.pref_include_subaccount_in_balance), false) )
 		//	loadAccountBalances();
 	}
 
 	public void close() {
 		if (sqliteHandle != null)
 			sqliteHandle.close();
 	}
 
 	public long getChangeCount() {
 		return changeCount;
 	}
 
 	public TreeMap<String, String> getAccountTypeMapping() {
 		return accountTypeMapping;
 	}
 
 	public void buildAccountMapping() {
 		accountPrefMapping = new TreeMap<String, String>();
 		accountTypeMapping = new TreeMap<String, String>();
 
 		accountPrefMapping.put(res.getString(R.string.pref_account_type_asset), "ASSET");
 		accountTypeMapping.put(res.getString(R.string.account_type_asset), "ASSET");
 
 		accountPrefMapping.put(res.getString(R.string.pref_account_type_bank), "BANK");
 		accountTypeMapping.put(res.getString(R.string.account_type_bank), "BANK");
 
 		accountPrefMapping.put(res.getString(R.string.pref_account_type_cc), "CREDIT");
 		accountTypeMapping.put(res.getString(R.string.account_type_cc), "CREDIT");
 
 		accountPrefMapping.put(res.getString(R.string.pref_account_type_expense), "EXPENSE");
 		accountTypeMapping.put(res.getString(R.string.account_type_expense), "EXPENSE");
 
 		accountPrefMapping.put(res.getString(R.string.pref_account_type_equity), "EQUITY");
 		accountTypeMapping.put(res.getString(R.string.account_type_equity), "EQUITY");
 
 		accountPrefMapping.put(res.getString(R.string.pref_account_type_income), "INCOME");
 		accountTypeMapping.put(res.getString(R.string.account_type_income), "INCOME");
 
 		accountPrefMapping.put(res.getString(R.string.pref_account_type_liability), "LIABILITY");
 		accountTypeMapping.put(res.getString(R.string.account_type_liability), "LIABILITY");
 
 		accountPrefMapping.put(res.getString(R.string.pref_account_type_mutual_fund), "MUTUAL");
 		accountTypeMapping.put(res.getString(R.string.account_type_mutual_fund), "MUTUAL");
 
 		accountPrefMapping.put(res.getString(R.string.pref_account_type_stock),"STOCK");
 		accountTypeMapping.put(res.getString(R.string.account_type_stock),"STOCK");
 
 		accountPrefMapping.put(res.getString(R.string.pref_account_type_cash),"CASH");
 		accountTypeMapping.put(res.getString(R.string.account_type_cash),"CASH");
 
 		accountPrefMapping.put(res.getString(R.string.pref_account_type_currency),"CURRENCY");
 		accountTypeMapping.put(res.getString(R.string.account_type_currency),"CURRENCY");
 
 		accountPrefMapping.put(res.getString(R.string.pref_account_type_a_receivable),"RECEIVABLE");
 		accountTypeMapping.put(res.getString(R.string.account_type_a_receivable),"RECEIVABLE");
 
 		accountPrefMapping.put(res.getString(R.string.pref_account_type_a_payable),"PAYABLE");
 		accountTypeMapping.put(res.getString(R.string.account_type_a_payable),"PAYABLE");
 
 		accountPrefMapping.put(res.getString(R.string.pref_account_type_trading),"TRADING");
 		accountTypeMapping.put(res.getString(R.string.account_type_trading),"TRADING");
 	}
 
 	public void genAccountFilter() {
 		StringBuffer filter = new StringBuffer();
 		if (!sp.getBoolean(res.getString(R.string.pref_show_hidden_account),
 				false))
 			filter.append(" hidden=0 and");
 
 		filter.append(" account_type not in (");
 		for (String key : accountPrefMapping.keySet())
 			if (!sp.getBoolean(key, true))
 				filter.append("'" + accountPrefMapping.get(key) + "', ");
 		filter.append(")");
 
 		accountFilter = filter.toString();
 	}
 
 	public Account getAccount(String GUID, boolean getBalance) {
 		Account account = gncData.accounts.get(GUID);
 		if (account == null)
 			return null;
 
 		if (account.balance == null && getBalance)
 			account.balance = accountBalance(account);
 
 		return account;
 	}
 
 	private Account accountFromCursor(Cursor cursor, boolean getBalance) {
 		return getAccount(cursor.getString(cursor.getColumnIndex("guid")),
 				getBalance);
 	}
 
 	/** Find the balance of the given Account. If the account is a STOCK or
 	 * MUTUAL account, get the value of the Account on the date given by the
 	 * most recent price.
 	 *
 	 * @return The account balance.
 	 */
 	public Double accountBalance(Account account) {
 		String[] queryArgs = { account.GUID };
 		String query;
 		String commodityGUID = null;
 		boolean equity = false;
 		if ( account.type.equals("STOCK") || account.type.equals("MUTUAL") )
 			equity = true;
 		if ( equity )
 			query = "select accounts.*,sum(CAST(quantity_num AS REAL)/quantity_denom) as bal from accounts,transactions,splits where splits.tx_guid=transactions.guid and splits.account_guid=accounts.guid and accounts.guid=? group by accounts.name";
 		else
 			query = "select accounts.*,sum(CAST(value_num AS REAL)/value_denom) as bal from accounts,transactions,splits where splits.tx_guid=transactions.guid and splits.account_guid=accounts.guid and accounts.guid=? group by accounts.name";
 
 		Cursor cursor = sqliteHandle.rawQuery(query, queryArgs);
 		try {
 			Double retVal = 0.0;
 			if (cursor.moveToNext()) {
 				int balIndex = cursor.getColumnIndex("bal");
 				if (!cursor.isNull(balIndex))
 					retVal = cursor.getDouble(balIndex);
 			}
 			if ( equity ) {
 				return retVal*getCommodityPrice(account.commodity.guid);
 			}
 			else
 				return retVal;
 		}
 		finally {
 			cursor.close();
 		}
 	}
 
 	private Double getCommodityPrice(String GUID) {
 		if ( commodityPrices == null )
 		{
 			commodityPrices = new TreeMap<String,Double>();
 			Cursor cursor = sqliteHandle.rawQuery("select commodity_guid,CAST(value_num AS REAL)/value_denom as price from prices group by commodity_guid order by date desc", null);
 			while ( cursor.moveToNext() ) {
 				String commodity_guid = cursor.getString(cursor.getColumnIndex("commodity_guid"));
 				Double price = cursor.getDouble(cursor.getColumnIndex("price"));
 				commodityPrices.put(commodity_guid, price);
 			}
 			cursor.close();
 		}
 		Double price = 0.0;
 		Double cp = commodityPrices.get(GUID);
 		if ( cp != null )
 			price = cp;
 
 		return price;
 	}
 
 	public void loadAccountBalances() {
 		Cursor cursor = sqliteHandle.rawQuery("select accounts.guid,sum(CAST(value_num AS REAL)/value_denom) as bal,sum(CAST(quantity_num AS REAL)/quantity_denom) as eqbal from accounts,transactions,splits where splits.tx_guid=transactions.guid and splits.account_guid=accounts.guid and "+ accountFilter +" group by accounts.name",null);
 		while ( cursor.moveToNext() ) {
 			String guid = cursor.getString(cursor.getColumnIndex("guid"));
 			Double bal = cursor.getDouble(cursor.getColumnIndex("bal"));
 			Account account = getAccount(guid, false);
 
 			if ( account.type.equals("STOCK") || account.type.equals("MUTUAL") ) {
 				Double eqbal = cursor.getDouble(cursor.getColumnIndex("eqbal"));
 				if ( eqbal > 0.0 ) {
 					Double commodityPrice = getCommodityPrice(account.commodity.guid);
 					account.balance = eqbal*commodityPrice;
 				}
 				else
 					account.balance = 0.0;
 			} else {
 				account.balance = bal;
 			}
 		}
 	}
 
 	/** Like getAccountBalance, but recurse into the accounts which have this
 	 * one as their parent or grandparent etc.
 	 *
 	 * @return the total balance.
 	 */
 	public Double getAccountBalanceWithChildren(String GUID) {
 		Account account = getAccount(GUID,true);
 		if ( account == null )
 			return new Double(0.0);
 
 		if ( account.balanceWithChildren != null )  // Lets not recalc if we don't need to
 			return account.balanceWithChildren;
 
 		Double bal = new Double(account.balance);
 		LinkedHashMap<String, Account> subAccounts = getSubAccounts(GUID);
 		if ( subAccounts != null ) {
 			for (String subGUID: subAccounts.keySet()) {
 				if ( !subGUID.equals(GUID) ) {
 					Double subBalance = getAccountBalanceWithChildren(subGUID);
 					bal += subBalance;
 				}
 			}
 		}
 		account.balanceWithChildren = bal;
 		return bal;
 	}
 
 	/** Indicate that the balance of this account (and its parents, if the user
 	 * has chosen to include subaccounts in balances) should be recalculated.
 	 *
 	 * @param GUID The GUID of the Account which has changed.
 	 */
 	public void markAccountChanged(String GUID) {
 		Account account = getAccount(GUID, false);
 		if ( account != null ) {
 			account.balance = null;
 			account.balanceWithChildren = null;
 			changeCount++;
 			if ( account.parentGUID != null && sp.getBoolean(app.res.getString(R.string.pref_include_subaccount_in_balance), false))
 				markAccountChanged(account.parentGUID);
 		}
 	}
 
 	/** The list of accounts whose type are one of those given.
 	 *
 	 * @param String[] The list of requested account types.
 	 * @return A Map of the account names to GUIDs.
 	 */
 	public TreeMap<String, String> getAccountList(String[] accountTypes) {
 		StringBuffer tb = new StringBuffer();
 		tb.append("select guid, name from accounts where account_type in (");
 		boolean first = true;
 		for (String at: accountTypes) {
 			if ( !first )
 				tb.append(", ");
 			tb.append("'" + at + "'");
 			first = false;
 		}
 		tb.append(")");
 		if (!sp.getBoolean(res.getString(R.string.pref_show_hidden_account),
 				false))
 			tb.append(" hidden=0");
 		tb.append(" and non_std_scu=0 order by name");
 
 		Cursor cursor = sqliteHandle.rawQuery(tb.toString(), null);
 		try {
 			TreeMap<String, String> listData = new TreeMap<String, String>();
 			while (cursor.moveToNext()) {
 				Account account = accountFromCursor(cursor, false);
 				if ( account == null )
 					return null;
 
 				if (sp.getBoolean(res.getString(R.string.pref_long_account_names), false))
 					listData.put(account.fullName, account.GUID);
 				else {
 					String guid = listData.get(account.name);
 					if (guid == null)
 						listData.put(account.name, account.GUID);
 					else { // We have a name collision
 						listData.put(account.fullName, account.GUID);
 					}
 				}
 			}
 			return listData;
 		}
 		finally {
 			cursor.close();
 		}
 	}
 
 	public LinkedHashMap<String, Account> getSubAccounts(String rootGUID) {
 		String[] queryArgs = { rootGUID };
 		String filter = accountFilter==null||accountFilter==""?"":" and " + accountFilter;
 		Cursor cursor = sqliteHandle.rawQuery(
 				"select * from accounts where parent_guid=? "
 						+ filter + " order by name", queryArgs);
 		try {
 			LinkedHashMap<String, Account> listData = new LinkedHashMap<String, Account>();
 			Account rootAccount = getAccount(rootGUID, true);
 			if ( rootAccount == null ) // this should never happen
 				return null;
 
 			if (!rootAccount.name.contains("Root"))
 				listData.put(rootGUID, rootAccount);
 			while (cursor.moveToNext()) {
 				Account account = accountFromCursor(cursor, true);
 				if ( account == null )  // this shouldn't happen either
 					return null;
 
 				if (account.hasChildren
 						|| ((int) (account.balance * 100.0)) != 0
 						|| sp.getBoolean(res.getString(R.string.pref_show_zero_total_accounts), false))
 					listData.put(account.GUID, account);
 			}
 			return listData;
 		}
 		finally {
 			cursor.close();
 		}
 	}
 
 	/** Return a list of the descriptions of recent transactions. */
 	public String[] getTransactionDescriptions() {
 		Calendar cal = Calendar.getInstance();
 		int year = cal.get(Calendar.YEAR);
 		String lastyear = Integer.toString(year - 1);
 
 		Cursor cursor = sqliteHandle.rawQuery(
 				"select distinct description from transactions where post_date > "
 						+ lastyear + "0101000000", null);
 		try {
 			int count = cursor.getCount();
 			String[] values = new String[count];
 
 			int index = 0;
 			while (cursor.moveToNext()) {
 				values[index++] = cursor.getString(cursor
 						.getColumnIndex("description"));
 			}
 			return values;
 		}
 		finally {
 			cursor.close();
 		}
 	}
 
 	private String genGUID() {
 		UUID uuid = UUID.randomUUID();
 		String GUID = Long.toHexString(uuid.getMostSignificantBits())
 				+ Long.toHexString(uuid.getLeastSignificantBits());
 		return GUID;
 	}
 
 	/** Create a new financial transaction in the database. The SQL transaction
 	 * is protected by a transaction, which will be rolled back if an error
 	 * occurs.
 	 *
 	 * @param toGUID The GUID of the payee of the transaction.
 	 * @param fromGUID The GUID of the payer of the transaction.
 	 * @param description The transaction description.
 	 * @param amount The value of the transaction, as a String.
 	 * @param date The date of the transaction, as a String.
 	 * @return False if something went wrong.
 	 */
 	public boolean insertTransaction(String toGUID, String fromGUID,
 			String description, String amount, String date) {
 
 		String transInsert = "insert into transactions(guid,currency_guid,num,post_date,enter_date,description) values(?,?,?,?,?,?)";
 		String splitsInsert = "insert into splits(guid,tx_guid,account_guid,memo,action,reconcile_state,value_num,value_denom,quantity_num,quantity_denom)"
 				+ " values(?,?,?,?,?,?,?,?,?,?)";
 
 		try {
 			sqliteHandle.beginTransaction();
 
 			String tx_guid = genGUID();
 
 			Date now = new Date();
 			DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
 			String postDate = formatter.format(now);
 
 			DateFormat simpleFormat = new SimpleDateFormat("MM/dd/yyyy");
 			Date enter = simpleFormat.parse(date);
 			String enterDate = formatter.format(enter);
 
 			// TODO This is not ideal. We should have support for
 			// entering transactions between arbitrary accounts,
 			// and popping up a box asking for the exchange rate
 			// and offering the most recent one from prices. This
 			// will require computing the fractions separately for
 			// each split. --Bruce Duncan, 14/2/2010
 			String commoditySQL = "select a.commodity_guid = b.commodity_guid from accounts as a, accounts as b where a.guid=? and b.guid=?";
 			String[] commoditySQLArgs = { toGUID, fromGUID };
 			Cursor cursor = sqliteHandle.rawQuery(commoditySQL, commoditySQLArgs);
 			try {
 				if (!cursor.moveToNext() || cursor.getInt(0) != 1)
 					throw new Exception("Cannot transfer between accounts in different currencies.");
 			}
 			finally {
 				cursor.close();
 			}
 
 			// NOTE: the columns are ints, not doubles.
 			int fraction = 0;
 			cursor = sqliteHandle.rawQuery("select fraction from accounts, commodities where accounts.commodity_guid = commodities.guid;", null);
 			try {
 				if (cursor.moveToNext())
 					fraction = cursor.getInt(cursor.getColumnIndex("fraction"));
 			}
 			finally {
 				cursor.close();
 			}
 			int value = (int)(Double.parseDouble(amount) * fraction);
 
 			// We need to insert 3 records (a transaction and two splits)
 			// CREATE TABLE splits (guid text(32) PRIMARY KEY NOT NULL, tx_guid
 			// text(32) NOT NULL, account_guid text(32) NOT NULL, memo
 			// text(2048) NOT NULL, action text(2048) NOT NULL, reconcile_state
 			// text(1) NOT NULL, reconcile_date text(14), value_num bigint NOT
 			// NULL, value_denom bigint NOT NULL, quantity_num bigint NOT NULL,
 			// quantity_denom bigint NOT NULL, lot_guid text(32));
 			// CREATE TABLE transactions (guid text(32) PRIMARY KEY NOT NULL,
 			// currency_guid text(32) NOT NULL, num text(2048) NOT NULL,
 			// post_date text(14), enter_date text(14), description text(2048));
 
 			// First the transaction
 			Object[] transArgs = { tx_guid, currencyGUID,
 					"", postDate, enterDate, description };
 			sqliteHandle.execSQL(transInsert, transArgs);
 
 			// Second, the two splits.
 			Object[] toArgs = { genGUID(), tx_guid, toGUID, "", "", "n", value,
 					fraction, value, fraction };
 			sqliteHandle.execSQL(splitsInsert, toArgs);
 
 			Object[] fromArgs = { genGUID(), tx_guid, fromGUID, "", "", "n",
 					-value, fraction, -value, fraction };
 			sqliteHandle.execSQL(splitsInsert, fromArgs);
 
 			sqliteHandle.setTransactionSuccessful();
 
 			markAccountChanged(toGUID);
 			markAccountChanged(fromGUID);
 
 			return true;
 		} catch (Exception e) {
 			return false;
 		} finally {
 			sqliteHandle.endTransaction();
 		}
 
 	}
 
 	public String[] getAccountsFromTransactionDescription(String description) {
 		String transSQL = "select accounts.guid from accounts, splits where tx_guid = (select guid from transactions where description=? order by post_date desc limit 1) and account_guid = accounts.guid;";
 		String[] transSQLArgs = { description };
 		Cursor cursor = sqliteHandle.rawQuery(transSQL, transSQLArgs);
 		try {
 			int count = cursor.getCount();
 			int index = 0;
 			String[] accountGUIDs = new String[count];
 			while (cursor.moveToNext())
 				accountGUIDs[index++] = cursor.getString(cursor
 						.getColumnIndex("guid"));
 			return accountGUIDs;
 		}
 		finally {
 			cursor.close();
 		}
 	}
 
 	/**
 	 * Returns the data collection object.
 	 */
 	public DataCollection getGncData() {
 		return gncData;
 	}
 
 	//
 	/**
 	 * This class is a collection of all gnc data objects.
 	 * 
 	 * @author shyam.avvari
 	 * 
 	 */
 	public class DataCollection {
 		// book information
 		public Book book = new Book();
 		// all data types
 		public Map<String, Account> accounts = new HashMap<String, Account>();
 		public Map<String, Commodity> commodities = new HashMap<String, Commodity>();
 
 		/**
 		 * This method should be called once all the data has been filled. It
 		 * does the following: 1. Add children data to accounts 2. Calculate
 		 * account balances 3. #
 		 */
 		public void completeCollection() {
 			Log.i(TAG, "Calculating Data...");
 			// get full names
 			updateFullNames();
 			// create account tree
 			createAccountTree();
 			Log.i(TAG, "Calculating Data...Done");
 		}
 
 		/**
 		 * We need full account names like granparent:parent:accountname to sort
 		 * the accounts, this method will update accounts with fullName
 		 * attribute
 		 */
 		private void updateFullNames() {
 			for (Account account : accounts.values())
 				account.fullName = getFullName(account);
 		}
 
 		/**
 		 * This method takes an account, finds its parents and grand parents
 		 * until root, and returns fullName of account
 		 * 
 		 * @param account
 		 * @return fullName
 		 */
 		private String getFullName(Account account) {
 			String fullName;
 			// If we know the full name, then return it; otherwise, construct it
 			if (account.fullName != null)
 				return account.fullName;
 			// Follow chain of parents, pre-pending their names,
 			// so we get "Grandparent:Parent:Name"
 			String p = account.parentGUID;
 			fullName = account.name;
 			while (null != p) {
 				Account parent = accounts.get(p);
				if (parent == null || parent.name.contains("Root")) {
 					break;
 				}
 				fullName = parent.name + ":" + fullName;
 				p = parent.parentGUID;
 			}
 			return fullName;
 		}
 
 		/**
 		 * We need to populate each account with list of its immediate children
 		 * in subList. This method goes through each account and updates its
 		 * parent account's subList with its GUID.
 		 */
 		private void createAccountTree() {
 			for (String accountName : accounts.keySet()) {
 				Account child = accounts.get(accountName);
 				if (child.parentGUID != null) {
 					Account parent = accounts.get(child.parentGUID);
 					parent.subList.add(child.GUID);
 					parent.hasChildren = true;
 				}
 			}
 		}
 	}
 
 	public class Book {
 		// configuration data
 		public String GUID;
 		public String version;
 		public String compName;
 		public String compId;
 		public String compAddr;
 		public String compEmail;
 		public String compUrl;
 		public String compFax;
 		public String compContact;
 		public String compPhone;
 		public String defCustTaxTable;
 		public String defVendTaxTable;
 		public int cntAccount;
 		public int cntTransaction;
 		public int cntSchedxaction;
 		public int cntJob;
 		public int cntInvoice;
 		public int cntCustomer;
 		public int cntBillTerm;
 		public int cntTaxTable;
 		public int cntEmployee;
 		public int cntEntry;
 		public int cntVendor;
 		public String cmdtySpace;
 		public String cmdtyId;
 		public String rootAccountGUID;
 	}
 
 	public class Commodity {
 		public String guid;
 		public String space;
 		public String quoteSource;
 		public String mnemonic;
 		public Currency currency;
 	}
 
 	public class Account {
 		// fields for an account
 		public String type;
 		public String GUID;
 		public String parentGUID;
 		public String name;
 		public String fullName;
 		public String notes;
 		public String description;
 		public String code;
 		public boolean placeholder;
 		public Commodity commodity;
 		// calculated balance amount
 		public Double balance;
 		public Double balanceWithChildren;
 		// transactions that belong to account
 		public List<String> trans = new ArrayList<String>();
 		public boolean hasChildren = false;
 		// id's of child-accounts
 		public List<String> subList = new ArrayList<String>();
 	}
 
 	public class AccountComparator implements Comparator<Account> {
 		public int compare(Account o1, Account o2) {
 			return o1.fullName.compareTo(o2.fullName);
 		}
 	}
 }
