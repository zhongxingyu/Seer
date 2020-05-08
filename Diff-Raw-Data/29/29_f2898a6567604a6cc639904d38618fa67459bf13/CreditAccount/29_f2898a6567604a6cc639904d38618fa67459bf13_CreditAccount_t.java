 package com.untzuntz.ustack.data.accting;
 
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.UUID;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 import org.bson.types.ObjectId;
 
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.BasicDBObjectBuilder;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 import com.untzuntz.ustack.data.UntzDBObject;
 import com.untzuntz.ustack.exceptions.AccountExistsException;
 import com.untzuntz.ustack.exceptions.InvalidSiteAccountName;
 import com.untzuntz.ustack.exceptions.InvalidUserAccountName;
 import com.untzuntz.ustack.exceptions.PasswordException;
 import com.untzuntz.ustack.exceptions.PasswordLengthException;
 import com.untzuntz.ustack.main.UOpts;
 import com.untzuntz.ustack.main.Msg;
 import com.untzuntz.ustack.main.UAppCfg;
 import com.untzuntz.ustack.main.UNotificationSvc;
 import com.untzuntz.ustack.main.UOpts;
 
 public class CreditAccount extends UntzDBObject {
 
 	private static final long serialVersionUID = 1L;
 	private static Logger logger = Logger.getLogger(CreditAccount.class);
 	
 	public static final String STATUS_DISABLED = "Disabled";
 	public static final String STATUS_ACTIVE = "Active";
 	public static final String TYPE_CREDIT = "Credit";
 	public static final String TYPE_INVOICE = "Invoice";
 	public static final String TYPE_CORPORATE = "Corporate";
 	public static final String TYPE_SPONSORED = "Sponsored";
 	public static final String TYPE_DEMO = "Demo";
 	
 	public String getCollectionName() { return "creditAccounts"; }
 	
 	private CreditAccount()
 	{
 		// setup basic values on account
 		put("created", new Date());
 		put("type", TYPE_CREDIT);
 		put("creditCount", 0);
 		put("priceTotal", 0);
 		setFundingRefreshLevel(5);
 		setFundingProductId(UOpts.getString(UAppCfg.CREDIT_REFRESH_DEF_PRODUCT_ID));
 		setStatus(STATUS_ACTIVE);
 	}
 
 	public String getAccountId() {
 		return get("_id") + "";
 	}
 
 	/** Gets the DB Collection for the CreditAccount object */
 	public static DBCollection getDBCollection() {
 		return new CreditAccount().getCollection();
 	}
 
 	/** Return the name of the database that houses the 'creditAccount' collection */
 	public static final String getDatabaseName() {
 		if (UOpts.getString(UAppCfg.DATABASE_CREDITACCT_COL) != null)
 			return UOpts.getString(UAppCfg.DATABASE_CREDITACCT_COL);
 		
 		return UOpts.getAppName();
 	}
 
 	/**
 	 * Generate a CreditAccount object from the MongoDB object
 	 * @param user
 	 */
 	public CreditAccount(DBObject acct) {
 		super(acct);
 	}
 
 	/** Rename the item - this runs a check to verify the new name isn't in use */
 	public void renameAccount(String name) throws AccountExistsException,InvalidSiteAccountName
 	{
 		if (name == null || name.length() == 0)
 			throw new InvalidSiteAccountName("CreditAccount");
 		
 		CreditAccount site = getAccount(name);
 		if (site != null) // already exists
 			throw new AccountExistsException("CreditAccount");
 
 		setAccountName(name);
 	}
 
 	/** Return BasicDBList of funding sources */
 	public BasicDBList getProductActionList()
 	{
 		BasicDBList ret = (BasicDBList)get("prodActionList");
 		if (ret == null)
 			ret = new BasicDBList();
 		
 		return ret;
 	}
 	
 	public void setProductActionList(BasicDBList list) {
 		put("prodActionList", list);
 	}
 	
 	public void removeProductIdForAction(String action)
 	{
 		BasicDBList list = getProductActionList();
 		for (int i = 0; i < list.size(); i++)
 		{
 			DBObject obj = (DBObject)list.get(i);
 			if (action.equalsIgnoreCase( (String)obj.get("action") ))
 			{
 				list.remove(i);
 				i--;
 			}
 		}
 		setProductActionList(list);
 	}
 	
 	public void setProductIdForAction(String action, String productId)
 	{
 		if (action == null || productId == null)
 			return;
 		
 		BasicDBList list = getProductActionList();
 		DBObject tgt = null;
 		for (int i = 0; tgt == null && i < list.size(); i++)
 		{
 			DBObject obj = (DBObject)list.get(i);
 			if (action.equalsIgnoreCase( (String)obj.get("action") ))
 				tgt = obj;
 		}
 		
 		if (tgt == null)
 		{
 			tgt = new BasicDBObject("action", action);
 			tgt.put("productId", productId);
 			list.add(tgt);
 		}
 		else
 			tgt.put("productId", productId);
 			
 		setProductActionList(list);
 	}		
 	
 	public String getProductIdByAction(String action)
 	{
 		if (action == null)
 			return null;
 		
 		BasicDBList list = getProductActionList();
 		for (int i = 0; i < list.size(); i++)
 		{
 			DBObject obj = (DBObject)list.get(i);
 			if (action.equalsIgnoreCase( (String)obj.get("action") ))
 				return (String)obj.get("productId");
 		}
 		
 		return null;
 	}
 
 	/** Sets the account name */
 	public void setAccountName(String name)
 	{
 		put("name", name);
 	}
 
 	/** Returns the username */
 	public String getAccountName()
 	{
 		return getString("name");
 	}
 	
 	/** Sets the uid */
 	private void setUid(String uid)
 	{
 		uid = uid.toLowerCase();
 		put("uid", uid);
 	}
 
 	/** Returns the uid */
 	public String getUid()
 	{
 		return getString("uid");
 	}
 	
 	public void setPIN(String pin)
 	{
 		put("pin", pin);
 		put("pinChangeDate", new Date());
 		put("pinErrorCount", 0);
 	}
 	
 	
 	/**
 	 * Increase failed password count and lock account if necessary
 	 */
 	public void increasePINErrorCount()
 	{
 		Integer errCnt = (Integer)get("pinErrorCount");
 		if (errCnt == null)
 			errCnt = new Integer(0);
 		
 		errCnt++;
 		put("pinErrorCount", errCnt);
 		
 		logger.info("PIN Error Cout: " + errCnt);
 		
 		if (errCnt >= UOpts.getInt(UAppCfg.PASSWORD_ERROR_LIMIT)) // we hit the max - lock it up!
 			lockAccount();
 		
 		save(UOpts.SUBSYS_AUTH);
 	}
 
 	/**
 	 * Locks the account for the configed amount of time
 	 */
 	public void lockAccount()
 	{
 		int lockSec = UOpts.getInt(UAppCfg.USER_ACCOUNT_LOCKTIME_SEC);
 		Calendar now = Calendar.getInstance();
 		now.add(Calendar.SECOND, lockSec);
 		put("locked", now.getTime());		
 	}
 
 	/**
 	 * Determine if the account is currently locked
 	 * 
 	 * @return
 	 */
 	public boolean isLocked()
 	{
 		if (get("locked") == null)
 			return false;
 		
 		// the lock date is set to when the account will be unlocked, if it is before 'now' we are not locked
 		Date lockDate = (Date)get("locked");
 		if (lockDate.before(new Date()))
 		{
 			unlock();
 			return false;
 		}
 		
 		return true;
 	}
 
 	/**
 	 * Clears fields to set account to a unlocked state
 	 */
 	public void unlock()
 	{
 		removeField("locked");
 		removeField("pinErrorCount");
 	}
 
 	/** Resets the password error count value */
 	public void resetPasswordErrorCount()
 	{
 		removeField("pinErrorCount");
 		save(UOpts.SUBSYS_AUTH);
 	}
 	
 	/** Returns the number of failed pin attempts */
 	public int getPINErrorCount()
 	{
 		if (get("pinErrorCount") == null)
 			return 0;
 		
 		return (Integer)get("pinErrorCount");
 	}
 
 	
 	public void setVolumePricingProducts(String val)
 	{
 		if (val == null)
 			removeField("volumePricingProducts");
 		else
 			put("volumePricingProducts", val);
 	}
 
 	public String getVolumePricingProducts()
 	{
 		return getString("volumePricingProducts");
 	}
 	
 	/** Return BasicDBList of volume pricing */
 	public BasicDBList getVolumePricingList()
 	{
 		BasicDBList ret = (BasicDBList)get("volumePricingList");
 		if (ret == null)
 			ret = new BasicDBList();
 		
 		return ret;
 	}
 	
 	public void addVolumeStep(int count, int price)
 	{
 		DBObject newStep = new BasicDBObject("count", count).append("price", price);
 		BasicDBList list = getVolumePricingList();
 		list.add(newStep);
 		setVolumePricingList(list);
 	}
 
 	public void setVolumePricingList(BasicDBList list)
 	{
 		put("volumePricingList", list);
 	}
 
 	/** Return BasicDBList of funding sources */
 	public BasicDBList getFundingSourceList()
 	{
 		BasicDBList ret = (BasicDBList)get("fundingSourceList");
 		if (ret == null)
 			ret = new BasicDBList();
 		
 		return ret;
 	}
 
 	public FundingConfig getFundingConfig(int idx)
 	{
 		BasicDBList list = getFundingSourceList();
 		if (idx > list.size())
 			return null;
 		
 		FundingConfig cfg = new FundingConfig((DBObject)list.get(idx));
 		list.set(idx, cfg);
 		setFundingSourceList(list);
 		
 		return cfg;
 	}
 	
 	/** Set the funding source list */
 	public void setFundingSourceList(BasicDBList list)
 	{
 		put("fundingSourceList", list);
 	}
 	
 	public void setFundingProductId(String productId)
 	{
 		put("fundingProductId", productId);
 	}
 	
 	public void removeFundingProductId()
 	{
 		removeField("fundingProductId");
 	}
 	
 	public String getFundingProductId()
 	{
 		return getString("fundingProductId");
 	}
 	
 	public void setAutoRecharge(boolean rc) {
 		put("autoRecharge", rc + "");
 	}
 	
 	public boolean isAutoRecharge()
 	{
 		return "true".equalsIgnoreCase(getString("autoRecharge"));
 	}
 	
 	public void setFundingRefreshLevel(int lvl)
 	{
 		put("fundingRefreshLevel", lvl);
 	}
 	
 	public int getFundingRefreshLevel()
 	{
 		return getInt("fundingRefreshLevel");
 	}
 	
 	public int getCreditCount()
 	{
 		return getInt("creditCount");
 	}
 	
 	public int getPriceTotal()
 	{
 		return getInt("priceTotal");
 	}
 	
 	public void addFundingSource(FundingConfig funding)
 	{
 		BasicDBList list = getFundingSourceList();
 		list.add(funding);
 		setFundingSourceList(list);
 	}
 	
 	/** 
 	 * Hit account configured funding sources for more credits
 	 * 
 	 * @param actor
 	 */
 	public void refreshCredits(String actor)
 	{
 		if (getFundingProductId() != null)
 		{
 			Product refresh = Product.getByProductId(getFundingProductId());
 			if (refresh != null)
 			{
 				try { 
 					processProductTransaction(actor, refresh);
 
 					NumberFormat currency = new DecimalFormat("$ #.00");
 					put("lastTransactionValue", currency.format(refresh.getDefaultPrice() / 100.0f));
 					
 					DBObject acctSearch = BasicDBObjectBuilder.start("creditAccountUid", getUid()).get();
 					
 					UNotificationSvc svc = new UNotificationSvc();
 					svc.setData("creditAccount", this);
 					svc.notify("ustack.creditRefreshed", acctSearch);
 
 				} catch (Exception err) {}
 			}
 			else
 				logger.error("Trying to refresh credits for account [" + getAccountName() + "/" + getAccountId() + "] -> Failed due to an invalid funding product id [" + getFundingProductId() + "]");
 		}
 		else
 			logger.error("Trying to refresh credits for account [" + getAccountName() + "/" + getAccountId() + "] -> Failed due to no funding product id");
 	}
 	
 	public void setExternalAPIName(String name)
 	{
 		put("externalAPIName", name);
 	}
 	
 	public String getExternalAPIName() 
 	{
 		String ret = getString("externalAPIName");
 		if (ret == null)
 			ret = "Stripe.com Test API";
 		
 		return ret;
 	}
 	
 	public void processProductTransaction(String actor, Product refresh) throws Exception
 	{
 		String transactionId = null;
 		FundingInt successFund = null;
 		boolean success = false;
 		
 		if (refresh == null)
 			throw new Exception("Invalid product for transaction");
 		
 		if (refresh.getDefaultPrice() == 0)
 			success = true;
 		else
 		{
 			Exception lastErr = null;
 			BasicDBList list = getFundingSourceList();
 			/*
 			 * Start at the top of the funding list and move down until we 'win'
 			 */
 			for (int i = 0; !success && i < list.size(); i++)
 			{
 				FundingConfig cfg = new FundingConfig( (DBObject)list.get(i) );
 				FundingInt fint = FundingConfig.getFundingInstance(cfg.getFundingActorClass());
 				if (fint != null)
 				{
 					fint.setAPIName( getExternalAPIName() );
 					try {
 						fint.setCreditAccount(this);
 						fint.setFundingConfig(cfg);
 						transactionId = fint.requestFunding(actor, null, refresh.getDescription(), refresh.getDefaultPrice(), "true".equalsIgnoreCase(System.getProperty("TestCase")));
 						successFund = fint;
 						success = true;
 					} catch (NoFundingAddedException err) {
 						
 						logger.info("Funding Request Skipped [" + getAccountName() + "/" + getAccountId() + "] -> Reason: " + err.getMessage(), err);
 						lastErr = err;
 						
 					} catch (Exception err) {
 						
						logger.warn("Funding Request Failed [" + getAccountName() + "/" + getAccountId() + "] -> Reason: " + err.getMessage(), err);
 						
 						// Save the failed transaction as part of the account record
 						AccountTransaction failedTran = new AccountTransaction(actor, refresh.getProductId());
 						failedTran.put("from", "n/a");
 						failedTran.put("to", "Transaction Failed");
 						failedTran.put("status", "error");
 						failedTran.setDescription(cfg.getName());
 						failedTran.put("errorText", err.getMessage());
 						failedTran.put("acctId", getAccountId());
 						failedTran.removeField("_id");
 						failedTran.save(actor);
 						
 						// Alert!
 						DBObject acctSearch = BasicDBObjectBuilder.start("creditAccountUid", getUid()).get();
 						UNotificationSvc svc = new UNotificationSvc();
 						svc.setData("creditAccount", this);
 						svc.setData("failedTransaction", failedTran);
 						svc.notify("ustack.fundingFailed", acctSearch);
 						lastErr = err;
 					}
 				}
 				else
 					logger.error("Failed to load funding actor class : " + cfg.getFundingActorClass());
 			}
 			
 			if (!success)
 			{
 				save(actor);
 				throw lastErr;
 			}
 		}
 		
 		// apply credits to the account based on the product that was approved above
 		AccountTransaction refreshTran = new AccountTransaction(actor, refresh.getProductId());
 		try {
 			if (successFund != null)
 				refreshTran.put("from", successFund.getDescription());
 			else
 				refreshTran.put("from", "n/a");
 				
 			refreshTran.put("to", "Account Balance");
 			if (transactionId != null)
 				refreshTran.put("transactionId", transactionId);
 			executeTransaction(actor, refreshTran);
 		} catch (InsufficientFundsException er) {
 			// ignore as we should only be adding funding
 		}
 	}
 	
 	/**
 	 * 
 	 *  Takes action on the account against the incoming transaction 
 	 *
 	 * 	Note: this does allow the caller to get to negative credits on the account
 	 * 
 	 */
 	public void executeTransaction(String actor, AccountTransaction tran) throws InsufficientFundsException
 	{
 		int credits = getInt("creditCount");
 		credits += tran.getCredits();
 		
 		int price = getInt("priceTotal");
 		price += tran.getPrice();
 		
 		// Update account values
 		put("creditCount", credits);
 		put("priceTotal", price);
 
 		// Check if we moved into the 'red'
 		if (tran.getCredits() < 0 && getInt("creditCount") < 0)
 		{
 			logger.warn("Disabling Credit Account [" + getAccountName() + "/" + getAccountId() + "] due to insufficient credits : " + getInt("creditCount"));
 			setStatus(STATUS_DISABLED);
 		}
 		else if (getInt("creditCount") > 0) // if we emerged
 			setStatus(STATUS_ACTIVE);
 
 		// set the account id on the transaction (for reporting) and remove the _id so we have unique transactions (ensures matching of credit counts)
 		tran.put("acctId", getAccountId());
 		tran.removeField("_id");
 		tran.save(actor);
 		save(actor); // save the accoun and transaction
 		
 		if (tran.getCredits() < 0)
 		{
 			DBObject acctSearch = BasicDBObjectBuilder.start("creditAccountUid", getUid()).get();
 
 			// We are below the funding level, send an alert
 			if (getInt("creditCount") < getFundingRefreshLevel())
 			{
 				UNotificationSvc svc = new UNotificationSvc();
 				svc.setData("creditAccount", this);
 				svc.notify("ustack.creditRefreshNeeded", acctSearch);
 				
 				// account needs refresh let's try to do that in this transaction
 				refreshCredits(actor);
 			}
 
 			// Accoun is in the red, alert on that too if necessary
 			if (getInt("creditCount") < 0)
 			{
 				UNotificationSvc svc = new UNotificationSvc();
 				svc.setData("creditAccount", this);
 				svc.notify("ustack.insufficientFunding", acctSearch);
 				throw new InsufficientFundsException();
 			}
 		}
 	}
 
 	/**
 	 * Determines if the credit account is disabled by the credit account status
 	 * @return
 	 */
 	public boolean isDisabled()
 	{
 		String status = getStatus();
 		
 		if (STATUS_DISABLED.equalsIgnoreCase(status))
 			return true;
 		
 		return false;
 	}
 
 	/** Sets the credit account's status */
 	public void setStatus(String status)
 	{
 		put("status", status);
 	}
 
 	/**
 	 * Returns the current credit account status
 	 * 
 	 * @return
 	 */
 	public String getStatus()
 	{
 		String status = (String)get("status");
 		if (status == null)
 			return STATUS_ACTIVE;
 		
 		return status;
 	}
 
 	/** Sets the credit account's type */
 	public void setType(String type)
 	{
 		put("type", type);
 	}
 
 	/**
 	 * Returns the current credit account type
 	 * 
 	 * @return
 	 */
 	public String getType()
 	{
 		String type = (String)get("type");
 		if (type == null)
 			return TYPE_CREDIT;
 		
 		return type;
 	}
 	
 	
 	
 	/**
 	 * Create a new credit account
 	 * 
 	 * @param name
 	 * @return
 	 * @throws AccountExistsException
 	 * @throws PasswordLengthException
 	 */
 	public static CreditAccount createAccount(String name) throws AccountExistsException,PasswordException
 	{
 		if (name == null || name.length() == 0)
 			throw new InvalidUserAccountName(Msg.getString("Invalid-Name"));
 		
 		// create the actual account
 		CreditAccount acct = new CreditAccount();
 		acct.setAccountName(name);
 
 		boolean valid = false;
 		while (!valid)
 		{
 			UUID nUid = UUID.randomUUID();
 			String uidStr = nUid.toString();
 			if (uidStr.length() > 20)
 				uidStr = uidStr.substring(0, 20);
 			
 			if (getAccountByUid(uidStr) == null)
 			{
 				acct.setUid(uidStr);
 				valid = true;
 			}
 		}		
 		logger.info("Creating credit account '" + name + "'");
 		
 		return acct;
 	}
 
 	/**
 	 * Get a credit account by name
 	 * 
 	 * @param userName
 	 * @return
 	 */
 	public static CreditAccount getAccount(String name)
 	{
 		if (name == null || name.length() == 0)
 			return null;
 		
 		DBObject acct = null;
 		try {
 			acct = new CreditAccount().getCollection().findOne(BasicDBObjectBuilder.start("name", Pattern.compile(".*" + name + ".*", Pattern.CASE_INSENSITIVE)).get());
 		} catch (Exception exp) { 
 			return null;
 		}
 		
 		if (acct == null)
 			return null;
 		
 		return new CreditAccount(acct);
 	}
 	
 	/**
 	 * Get a credit account by name
 	 * 
 	 * @param userName
 	 * @return
 	 */
 	public static CreditAccount getAccountByUid(String uid)
 	{
 		if (uid == null || uid.length() == 0)
 			return null;
 		
 		uid = uid.toLowerCase();
 		
 		DBObject acct = null;
 		try {
 			acct = new CreditAccount().getCollection().findOne(BasicDBObjectBuilder.start("uid", uid).get());
 		} catch (Exception exp) { 
 			return null;
 		}
 		
 		if (acct == null)
 			return null;
 		
 		return new CreditAccount(acct);
 	}
 	
 	/**
 	 * Get a credit account by uid
 	 * 
 	 * @param uid
 	 * @return
 	 */
 	public static CreditAccount getAccountById(String uid)
 	{
 		if (uid == null)
 			return null;
 		
 		DBObject acct = new CreditAccount().getCollection().findOne(BasicDBObjectBuilder.start("_id", new ObjectId(uid)).get());
 		
 		if (acct == null)
 			return null;
 		
 		return new CreditAccount(acct);
 	}
 	
 	
 }
