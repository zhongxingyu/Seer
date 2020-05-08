 package org.alt60m.staffSite.bean.dbio;
 
 import java.sql.*;
 import java.util.*;
 
 import org.alt60m.util.DBConnectionFactory;
 import org.alt60m.security.dbio.manager.*;
 import org.alt60m.security.dbio.model.User;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class AccountBalanceUpdater {
 
 	private static Log log = LogFactory.getLog(AccountBalanceUpdater.class);
 	final String BALANCE_PREFERENCE_NAME = "balance";    
 
 	Connection m_connection = null;
 	UserPreferences _preferences;
 
 	public AccountBalanceUpdater() {}
     
 	public void performUpdate(boolean verbose)  {
 		try {
 			_preferences = new UserPreferences();
 
 			// Connect to the prod database to get the imported table... (wierd??)
 			m_connection = DBConnectionFactory.getOracleDatabaseConn();
 			 
 			if (m_connection == null) {
 				log.fatal("Failed to open Oracle database connection!");
 				return;
 			}
 
 			log.info("Connected to Oracle");
 			
 			java.util.Date querystart = new java.util.Date();
 			log.info("[" + querystart +"] Querying...");
 			
 			//get all StaffSiteProfiles
 			Connection sqlconn = DBConnectionFactory.getDatabaseConn();
 			Statement sqlstatement = sqlconn.createStatement();
			String query = "SELECT obj.firstName, obj.lastName, obj.accountNo, obj.userName, obj.StaffSiteProfileID FROM staffsite_staffsiteprofile obj WHERE LENGTH(obj.accountNo) > 0 ORDER BY obj.lastName";
 			ResultSet rs = sqlstatement.executeQuery(query);
 
 			java.util.Date querystop = new java.util.Date();
 			log.info("[" + querystop + "] Got Recordset.");
 
 			log.info("[" + new java.util.Date() + "] Getting current balances.");			
 			Hashtable allBalances = getAllBalances();
 			m_connection.close();
 
 			String accountNo;
 			String username;
 			int balance;
 
 			while (rs.next()) {
 				accountNo = (String) rs.getString("accountNo");
 				username = (new Integer(rs.getString("userName"))).toString();
 				SimpleSecurityManager ssm = new SimpleSecurityManager();
 				User user = ssm.getUserObjectByUsername(username);
 				String dateStamp = (new java.text.SimpleDateFormat ("yyyy.MM.dd")).format(new java.util.Date());
 				try {
 
 					if(accountNo.length() < 9) {
 						if(verbose) log.warn("**Account number is malformed for '" + rs.getString("LastName") + ", " + rs.getString("FirstName") + " (acct#" + accountNo + ")'.  ");
 					} else {
 						String commonAccountNo = accountNo.substring(0,9);
 
 						if(allBalances.containsKey(commonAccountNo)) {
 							balance = ((Integer) allBalances.get(commonAccountNo)).intValue();
 							_preferences.savePreference(String.valueOf(user.getUserID()), BALANCE_PREFERENCE_NAME, Integer.toString(balance));
 							if (verbose) 
 								log.info("  Updated balance for '" + rs.getString("LastName") + ", " + rs.getString("FirstName") + " (acct#" + accountNo + ")': " + balance);
 						} else { 
 							if (verbose) 
 								log.info("**No balance info available for '" + rs.getString("LastName") + ", " + rs.getString("FirstName") + " (acct#" + accountNo + ")'.  ");
 						}
 					}
 					
 				} catch(Exception e) {
 					log.error("**Failed to update balance for '" + rs.getString("LastName") + ", " + rs.getString("FirstName") + " (acct#" + accountNo + ")'.");
 					if(verbose) 
 						log.error("  Error message: " + e.getMessage(), e);
 					else
 						log.error("  Error message: " + e.toString());
 				}
 			}
 	
 			log.info("[" + new java.util.Date() +"] Success.");
 
 		} catch (Exception e) {
 			log.error("[" + new java.util.Date() +"] Failed.", e);
 		}
 	}
 	
 	public Hashtable getAllBalances() throws Exception {
 			
 	String query = "select lm.emplid as emplid, (lm.last_month_bal + nvl(tm.dasum,0)) as cur_bal from " + 
 					"(select emplid, last_month_bal from staff_last_bal_vw) lm left join (select emplid, sum(trans_amount) as dasum from SYSADM.PS_STAFF_TRNSACTNS a " +
 					"where a.stf_acct_type = 'PRIME' and a.trans_date > '01-may-2010' and a.posted_flag = 'N' group by emplid) tm " + 
 					"on lm.emplid = tm.emplid order by lm.emplid";
 
 		Statement statement = m_connection.createStatement();
 		log.info("[" + new java.util.Date() +"]before executing query .....");
 		ResultSet rs1 = statement.executeQuery(query);
 		log.info("[" + new java.util.Date() +"]after executing query .....");
 		
 		Hashtable<String, Integer> allBalances = new Hashtable<String, Integer>();
 		
 		while(rs1.next()) {
 			allBalances.put(rs1.getString("emplid"), new Integer(rs1.getInt("cur_bal")));
 		}
 
 		rs1.close();
 
 		return allBalances;
 	}
 
 	public static void main(String[] args) throws Exception {
 		if (args.length<3) {
 			log.info("Usage: AccountBalanceUpdater username password database [-verbose]");
 			return;
 		}
 
 		DBConnectionFactory.setDefaultProperties(args[0], args[1], args[2]);
 		DBConnectionFactory.setupPool();
 		AccountBalanceUpdater abu = new AccountBalanceUpdater();
 		
 		if (args.length>=1 && "-verbose".equalsIgnoreCase(args[4]))
 			abu.performUpdate(true);
 		else
 			abu.performUpdate(false);
 
 	}
 }
