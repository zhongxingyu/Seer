 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package nl.tudelft.stocktrader.derby;
 
 import java.math.BigDecimal;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 //import nl.tudelft.stocktrader.Account;
 import nl.tudelft.stocktrader.Account;
 import nl.tudelft.stocktrader.AccountProfile;
 import nl.tudelft.stocktrader.Holding;
 import nl.tudelft.stocktrader.Order;
 import nl.tudelft.stocktrader.Wallet;
 import nl.tudelft.stocktrader.dal.CustomerDAO;
 import nl.tudelft.stocktrader.dal.DAOException;
 import nl.tudelft.stocktrader.util.StockTraderUtility;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class DerbyCustomerDAO extends AbstractDerbyDAO implements CustomerDAO {
     private static final Log logger = LogFactory.getLog(DerbyCustomerDAO.class);
 
     private static final String SQL_DEBIT_ACCOUNT = "UPDATE account SET balance= balance - ? WHERE accountid = ?";
     private static final String SQL_SELECT_HOLDING_LOCK = "SELECT h.account_accountid, h.holdingid, h.quantity, h.purchaseprice, h.purchasedate, h.quote_symbol FROM holding as h INNER JOIN orders as o on h.holdingid = o.holding_holdingid WHERE (o.orderid = ?)";
     private static final String SQL_SELECT_HOLDING_NOLOCK = "SELECT account_accountid, quantity, purchaseprice, purchasedate, quote_symbol FROM holding WHERE holdingid = ? AND account_accountid = (SELECT accountid FROM account WHERE profile_userid = ?)";
     private static final String SQL_SELECT_CUSTOMER_PROFILE_BY_USERID = "SELECT userid, password, fullname, address, email, creditcard FROM accountprofile WHERE userid = ?";
     private static final String SQL_UPDATE_CUSTOMER_LOGIN = "UPDATE account SET logincount = logincount + 1, lastlogin = current_timestamp where profile_userid = ?";
     private static final String SQL_SELECT_CUSTOMER_LOGIN = "SELECT accountid, creationdate, openbalance, logoutcount, balance, lastlogin, logincount FROM account WHERE profile_userid = ?";
     private static final String SQL_UPDATE_LOGOUT = "UPDATE account SET logoutcount = logoutcount + 1 WHERE profile_userid = ?";
     private static final String SQL_SELECT_GET_CUSTOMER_BY_USERID = "SELECT account.ACCOUNTID, account.PROFILE_USERID, account.CREATIONDATE, account.OPENBALANCE, account.LOGOUTCOUNT, account.BALANCE, account.LASTLOGIN, account.LOGINCOUNT, account.CURRENCY FROM account WHERE account.PROFILE_USERID = ?";
     private static final String SQL_SELECT_ORDERS_BY_ID = " o.orderid, o.ordertype, o.orderstatus, o.opendate, o.completiondate, o.quantity, o.price, o.orderfee, o.quote_symbol FROM orders o WHERE o.account_accountid = (SELECT a.accountid FROM account a WHERE a.profile_userid = ?) ORDER BY o.orderid DESC";
     private static final String SQL_SELECT_CLOSED_ORDERS = "SELECT orderid, ordertype, orderstatus, completiondate, opendate, quantity, price, orderfee, quote_symbol FROM orders WHERE account_accountid = (SELECT accountid FROM account WHERE profile_userid = ?) AND orderstatus = 'closed'";
     private static final String SQL_UPDATE_CLOSED_ORDERS = "UPDATE orders SET orderstatus = 'completed' WHERE orderstatus = 'closed' AND account_accountid = (SELECT accountid FROM account WHERE profile_userid = ?)";
     private static final String SQL_INSERT_ACCOUNT_PROFILE = "INSERT INTO accountprofile VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_INSERT_ACCOUNT = "INSERT INTO account (creationdate, openbalance, logoutcount, balance, lastlogin, logincount, profile_userid) VALUES (current_timestamp, ?, ?, ?, ?, ?, ?); SELECT LAST_INSERT_ID();";
     private static final String SQL_UPDATE_ACCOUNT_PROFILE = "UPDATE accountprofile SET address = ?, password = ?, email = ?, creditcard = ?, fullname = ? WHERE userid = ?";
     private static final String SQL_SELECT_HOLDINGS = "SELECT holdingid, quantity, purchaseprice, purchasedate, quote_symbol, account_accountid FROM holding WHERE account_accountid = (SELECT accountid FROM account WHERE profile_userid = ?) ORDER BY holdingid DESC";
     //for register
 //    private static final String SQL_SELECT_USER_LIST = "SELECT userid FROM accountprofile";
     private static final String SQL_INSERT_WALLET = "INSERT INTO wallet (userid, usd, eur, gbp, cny, inr) VALUES (?, ?, ?, ?, ?, ?)";
     private static final String SQL_SELECT_WALLET = "SELECT userid, usd, eur, gbp, cny, inr FROM wallet where userid = ?";
     private static final String SQL_UPDATE_WALLET = "UPDATE wallet SET usd = ?, eur = ?, gbp = ?, cny = ?, inr = ? WHERE userid = ?";
 
  
     
     public DerbyCustomerDAO(Connection sqlConnection) throws DAOException {
         super(sqlConnection);
     }
 
     public Holding getHoldingForUpdate(int orderId) throws DAOException {
         if (logger.isDebugEnabled()) {
             logger.debug("MySQLCustomerDAO.getHoldingForUpdate(int)\nOrder ID :" + orderId);
         }
 
         Holding holding = null;
         PreparedStatement selectHoldingLockStat = null;
         try {
             selectHoldingLockStat = sqlConnection.prepareStatement(SQL_SELECT_HOLDING_LOCK);
             selectHoldingLockStat.setInt(1, orderId);
             ResultSet rs = selectHoldingLockStat.executeQuery();
             if (rs.next()) {
                 try {
                     holding = new Holding(
                             rs.getInt(1),
                             rs.getInt(2),
                             rs.getDouble(3),
                             rs.getBigDecimal(4),
                             StockTraderUtility.convertToCalendar(rs.getDate(5)),
                             rs.getString(6));
                     return holding;
                 } finally {
                     try {
                         rs.close();
                     } catch (SQLException e) {
                         logger.debug("", e);
                     }
                 }
             }
         } catch (SQLException e) {
             throw new DAOException("Exception is thrown when selecting the holding entry for order ID :" + orderId, e);
         } finally {
             if (selectHoldingLockStat != null) {
                 try {
                     selectHoldingLockStat.close();
                 } catch (SQLException e) {
                     logger.debug("", e);
                 }
             }
         }
         return holding;
     }
 
     public Holding getHolding(String userId, int holdingID) throws DAOException {
         if (logger.isDebugEnabled()) {
             logger.debug("MSSQLCustomerDAO.getHolding(String,int)\nUserID :" + userId + "\nOrder ID :" + holdingID);
         }
         Holding holding = null;
         PreparedStatement selectHoldingNoLockStat = null;
         try {
             selectHoldingNoLockStat = sqlConnection.prepareStatement(SQL_SELECT_HOLDING_NOLOCK);
             selectHoldingNoLockStat.setInt(1, holdingID);
             selectHoldingNoLockStat.setString(2, userId);
 
             ResultSet rs = selectHoldingNoLockStat.executeQuery();
             if (rs.next()) {
                 try {
                     holding = new Holding(
                             rs.getInt(1),
                             holdingID,
                             rs.getDouble(2),
                             rs.getBigDecimal(3),
                             StockTraderUtility.convertToCalendar(rs.getDate(4)),
                             rs.getString(5));
                     return holding;
                 } finally {
                     try {
                         rs.close();
                     } catch (SQLException e) {
                         logger.debug("", e);
                     }
                 }
             }
         } catch (SQLException e) {
             logger.debug("", e);
             throw new DAOException("Exception is thrown when selecting the holding entry for userID :" + userId + " and orderID :" + holdingID, e);
 
         } finally {
             if (selectHoldingNoLockStat != null) {
                 try {
                     selectHoldingNoLockStat.close();
                 } catch (SQLException e) {
                     logger.debug("", e);
                 }
             }
         }
         return holding;
     }
 
     public void updateAccountBalance(int accountId, BigDecimal total) throws DAOException {
         if (logger.isDebugEnabled()) {
             logger.debug("MySQLCustomerDAO.updateAccoutBalance(int,BigDecimal)\n Account ID :" + accountId + "\nTotal :" + total);
         }
         PreparedStatement debitAccountStat = null;
         try {
             debitAccountStat = sqlConnection.prepareStatement(SQL_DEBIT_ACCOUNT);
             debitAccountStat.setBigDecimal(1, total);
             debitAccountStat.setInt(2, accountId);
             debitAccountStat.executeUpdate();
 
         } catch (SQLException e) {
             throw new DAOException("Excpetion is thrown when updating the account balance for accountID :" + accountId + " total :" + total, e);
         } finally {
             if (debitAccountStat != null) {
                 try {
                     debitAccountStat.close();
                 } catch (SQLException e) {
                     logger.debug("", e);
                 }
             }
         }
     }
 
     public Account login(String userId, String password) throws DAOException {
         PreparedStatement selectCustomerProfileByUserId = null;
         PreparedStatement updateCustomerLogin = null;
         PreparedStatement selectCustomerLogin = null;
         try {
             selectCustomerProfileByUserId = sqlConnection.prepareStatement(SQL_SELECT_CUSTOMER_PROFILE_BY_USERID);
             selectCustomerProfileByUserId.setString(1, userId);
             ResultSet customerProfileRS = selectCustomerProfileByUserId.executeQuery();
             if (customerProfileRS.next()) {
                 try {
                     String userPassword = customerProfileRS.getString(2);
                     if (userPassword.equals(password)) {
                         try {
                             updateCustomerLogin = sqlConnection.prepareStatement(SQL_UPDATE_CUSTOMER_LOGIN);
                             updateCustomerLogin.setString(1, userId);
                             updateCustomerLogin.executeUpdate();
                             selectCustomerLogin = sqlConnection.prepareStatement(SQL_SELECT_CUSTOMER_LOGIN);
                             selectCustomerLogin.setString(1, userId);
                             ResultSet rs = selectCustomerLogin.executeQuery();
                             if (rs.next()) {
                                 try {
                                     Account accountData = new Account(
                                             rs.getInt(1),
                                             userId,
                                             StockTraderUtility.convertToCalendar(rs.getDate(2)),
                                             rs.getBigDecimal(3),
                                             rs.getInt(4),
                                             rs.getBigDecimal(5),
                                             StockTraderUtility.convertToCalendar(rs.getDate(6)),
                                             rs.getInt(7) + 1);
                                     return accountData;
                                 } finally {
                                     try {
                                         rs.close();
                                     } catch (SQLException e) {
                                         logger.debug("", e);
                                     }
                                 }
                             }
                         } catch (SQLException e) {
                             throw new DAOException("", e);
                         } finally {
                             if (updateCustomerLogin != null) {
                                 try {
                                     updateCustomerLogin.close();
                                 } catch (SQLException e) {
                                     logger.debug("", e);
                                 }
                             }
                             if (selectCustomerLogin != null) {
                                 try {
                                     selectCustomerLogin.close();
                                 } catch (SQLException e) {
                                     logger.debug("", e);
                                 }
                             }
                         }
                     }
                 } finally {
                     try {
                         customerProfileRS.close();
                     } catch (SQLException e) {
                         logger.debug("", e);
                     }
                 }
             }
 
         } catch (SQLException e) {
             throw new DAOException("", e);
         } finally {
             if (selectCustomerProfileByUserId != null) {
                 try {
                     selectCustomerProfileByUserId.close();
                 } catch (SQLException e) {
                     logger.debug("", e);
                 }
             }
         }
         return null;
     }
 
     public void logoutUser(String userId) throws DAOException {
         PreparedStatement updateLogout = null;
         try {
             updateLogout = sqlConnection.prepareStatement(SQL_UPDATE_LOGOUT);
             updateLogout.setString(1, userId);
             updateLogout.executeUpdate();
         } catch (SQLException e) {
             throw new DAOException("", e);
         } finally {
             if (updateLogout != null) {
                 try {
                     updateLogout.close();
                 } catch (SQLException e) {
                     logger.debug("", e);
                 }
             }
         }
     }
 
     public Account getCustomerByUserId(String userId) throws DAOException {
         PreparedStatement getCustomerByUserId = null;
 
         try {
             getCustomerByUserId = sqlConnection.prepareStatement(SQL_SELECT_GET_CUSTOMER_BY_USERID);
             getCustomerByUserId.setString(1, userId);
             ResultSet rs = getCustomerByUserId.executeQuery();
             if (rs.next()) {
                 try {
                     Account bean = new Account(
                             rs.getInt(1),
                             rs.getString(2),
                             StockTraderUtility.convertToCalendar(rs.getDate(3)),
                             rs.getBigDecimal(4),
                             rs.getInt(5),
                             rs.getBigDecimal(6),
                             StockTraderUtility.convertToCalendar(rs.getDate(7)),
                             rs.getInt(8),
                             rs.getString(9));
                     return bean;
                 } finally {
                     try {
                         rs.close();
                     } catch (SQLException e) {
                         logger.debug("", e);
                     }
                 }
             }
         } catch (SQLException e) {
             throw new DAOException("", e);
         } finally {
             if (getCustomerByUserId != null) {
                 try {
                     getCustomerByUserId.close();
                 } catch (SQLException e) {
                     logger.debug("", e);
                 }
             }
         }
         return null;
     }
 
     public AccountProfile getAccountProfileData(String userId) throws DAOException {
 
         PreparedStatement customerProfileByUserId = null;
         try {
             customerProfileByUserId = sqlConnection.prepareStatement(SQL_SELECT_CUSTOMER_PROFILE_BY_USERID);
             customerProfileByUserId.setString(1, userId);
             ResultSet rs = customerProfileByUserId.executeQuery();
             if (rs.next()) {
                 try {
                     AccountProfile accountProfileDataBean = new AccountProfile(
                             rs.getString(1),
                             rs.getString(2),
                             rs.getString(3),
                             rs.getString(4),
                             rs.getString(5),
                             rs.getString(6));
                     return accountProfileDataBean;
                 } finally {
                     try {
                         rs.close();
                     } catch (SQLException e) {
                         logger.debug("", e);
                     }
                 }
             }
         } catch (SQLException e) {
             throw new DAOException("", e);
         } finally {
             if (customerProfileByUserId != null) {
                 try {
                     customerProfileByUserId.close();
                 } catch (SQLException e) {
                     logger.debug("", e);
                 }
             }
         }
         return null;
     }
 
     public List<Order> getOrders(String userId, boolean top, int maxTop, int maxDefault) throws DAOException {
         PreparedStatement selectOrdersById = null;
         try {
             String sqlQuery;
             if (top) {
                 sqlQuery = "SELECT " + SQL_SELECT_ORDERS_BY_ID + " FETCH FIRST " + maxTop + " ROWS ONLY";
             } else {
                 sqlQuery = "SELECT " + SQL_SELECT_ORDERS_BY_ID + " FETCH FIRST " + maxDefault + " ROWS ONLY";
             }
             selectOrdersById = sqlConnection.prepareStatement(sqlQuery);
             selectOrdersById.setString(1, userId);
             ResultSet rs = selectOrdersById.executeQuery();
             List<Order> orders = new ArrayList<Order>();
 
             try {
                 while (rs.next()) {
                     int orderId = rs.getInt(1);
                     Calendar openDate = StockTraderUtility.convertToCalendar(rs.getDate(4));
                     Calendar completionDate = null;
                     try {
                         if (rs.getDate(5) != null) {
                             completionDate = StockTraderUtility.convertToCalendar(rs.getDate(5));
                         } else {
                             completionDate = Calendar.getInstance();
                             completionDate.setTimeInMillis(0);
                         }
                     } catch (SQLException e) {
                         logger.debug("", e);
                         completionDate = Calendar.getInstance();
                         completionDate.setTimeInMillis(0);
                     }
 
                     Order orderBean = new Order(
                             orderId,
                             rs.getString(2),
                             rs.getString(3),
                             openDate,
                             completionDate,
                             rs.getDouble(6),
                             rs.getBigDecimal(7),
                             rs.getBigDecimal(8),
                             rs.getString(9));
                     orders.add(orderBean);
                 }
 
             } finally {
                 try {
                     rs.close();
                 } catch (SQLException e) {
                     logger.debug("", e);
                 }
             }
             return orders;
 
         } catch (SQLException e) {
             throw new DAOException("", e);
         } finally {
             if (selectOrdersById != null) {
                 try {
                     selectOrdersById.close();
                 } catch (SQLException e) {
                     logger.debug("", e);
                 }
             }
         }
     }
 
     public List<Order> getClosedOrders(String userId) throws DAOException {
         PreparedStatement selectClosedOrders = null;
         PreparedStatement updateClosedOrders = null;
         try {
             selectClosedOrders = sqlConnection.prepareStatement(SQL_SELECT_CLOSED_ORDERS);
             selectClosedOrders.setString(1, userId);
             ResultSet rs = selectClosedOrders.executeQuery();
             List<Order> closedOrders = new ArrayList<Order>();
 
             try {
                 while (rs.next()) {
                     int orderId = rs.getInt(1);
                     Calendar openDate = StockTraderUtility.convertToCalendar(rs.getDate(4));
                     Calendar completionDate = null;
                     try {
                         completionDate = StockTraderUtility.convertToCalendar(rs.getDate(5));
                     } catch (SQLException e) {
                         logger.debug("", e);
                         completionDate = Calendar.getInstance();
                         completionDate.setTimeInMillis(0);
                     }
                     Order closedOrderBean = new Order(
                             orderId,
                             rs.getString(2),
                             rs.getString(3),
                             openDate,
                             completionDate,
                             rs.getDouble(6),
                             rs.getBigDecimal(7),
                             rs.getBigDecimal(8),
                             rs.getString(9));
                     closedOrderBean.setOrderStatus(StockTraderUtility.ORDER_STATUS_CLOSED);
                     closedOrders.add(closedOrderBean);
                 }
             } finally {
                 try {
                     rs.close();
                 } catch (SQLException e) {
                     logger.debug("", e);
                 }
             }
 
             if (!closedOrders.isEmpty()) {
                 updateClosedOrders = sqlConnection.prepareStatement(SQL_UPDATE_CLOSED_ORDERS);
                 updateClosedOrders.setString(1, userId);
                 updateClosedOrders.executeUpdate();
             }
 
             return closedOrders;
         } catch (SQLException e) {
             throw new DAOException("", e);
         } finally {
             if (selectClosedOrders != null) {
                 try {
                     selectClosedOrders.close();
                 } catch (SQLException e) {
                     logger.debug("", e);
                 }
             }
             if (updateClosedOrders != null) {
                 try {
                     selectClosedOrders.close();
                 } catch (SQLException e) {
                     logger.debug("", e);
                 }
             }
 
         }
     }
 
     public void insertAccountProfile(AccountProfile accountProfileBean) throws DAOException {
         PreparedStatement insertAccountProfile = null;
         try {
             insertAccountProfile = sqlConnection.prepareStatement(SQL_INSERT_ACCOUNT_PROFILE);
             insertAccountProfile.setString(1, accountProfileBean.getAddress());
             insertAccountProfile.setString(2, accountProfileBean.getPassword());
             insertAccountProfile.setString(3, accountProfileBean.getUserID());
             insertAccountProfile.setString(4, accountProfileBean.getEmail());
             insertAccountProfile.setString(5, accountProfileBean.getCreditCard());
             insertAccountProfile.setString(6, accountProfileBean.getFullName());
             insertAccountProfile.executeUpdate();
         } catch (SQLException e) {
             throw new DAOException("", e);
         } finally {
             if (insertAccountProfile != null) {
                 try {
                     insertAccountProfile.close();
                 } catch (SQLException e) {
                     logger.debug("", e);
                 }
             }
         }
     }
 
     public void insertAccount(Account accountBean) throws DAOException {
         PreparedStatement insertAccount = null;
         try {
             insertAccount = sqlConnection.prepareStatement(SQL_INSERT_ACCOUNT);
             insertAccount.setBigDecimal(1, accountBean.getOpenBalance());
            insertAccount.setInt(2, accountBean.getLogoutCount());
            insertAccount.setBigDecimal(3, accountBean.getBalance());
             //first insert: the user didn't exist before, no last login
 //            insertAccount.setDate(4, StockTraderUtility.convertToSqlDate(accountBean.getLastLogin()));
            insertAccount.setInt(5, accountBean.getLoginCount());
            insertAccount.setString(6, accountBean.getUserID());
             insertAccount.executeUpdate();
 
         } catch (SQLException e) {
             throw new DAOException("", e);
 
         } finally {
             if (insertAccount != null) {
                 try {
                     insertAccount.close();
                 } catch (SQLException e) {
                     logger.debug("", e);
                 }
             }
         }
     }
 
     public AccountProfile update(AccountProfile customerAccountProfile) throws DAOException {
         PreparedStatement updateAccountProfile = null;
         try {
             updateAccountProfile = sqlConnection.prepareStatement(SQL_UPDATE_ACCOUNT_PROFILE);
             updateAccountProfile.setString(1, customerAccountProfile.getAddress());
             updateAccountProfile.setString(2, customerAccountProfile.getPassword());
             updateAccountProfile.setString(3, customerAccountProfile.getEmail());
             updateAccountProfile.setString(4, customerAccountProfile.getCreditCard());
             updateAccountProfile.setString(5, customerAccountProfile.getFullName());
             updateAccountProfile.setString(6, customerAccountProfile.getUserID());
             updateAccountProfile.executeUpdate();
             return customerAccountProfile;
         } catch (SQLException e) {
             throw new DAOException("", e);
         } finally {
             if (updateAccountProfile != null) {
                 try {
                     updateAccountProfile.close();
                 } catch (SQLException e) {
                     logger.debug("", e);
                 }
             }
         }
     }
 
     public List<Holding> getHoldings(String userID) throws DAOException {
         PreparedStatement selectHoldings = null;
         try {
             selectHoldings = sqlConnection.prepareStatement(SQL_SELECT_HOLDINGS);
             selectHoldings.setString(1, userID);
             ResultSet rs = selectHoldings.executeQuery();
             List<Holding> holdings = new ArrayList<Holding>();
             try {
                 while (rs.next()) {
                     Holding holding = new Holding(
                             rs.getInt(1),
                             rs.getDouble(2),
                             rs.getBigDecimal(3),
                             StockTraderUtility.convertToCalendar(rs.getDate(4)),
                             rs.getString(5),
                             rs.getInt(6));
                     holdings.add(holding);
                 }
             } finally {
                 try {
                     rs.close();
                 } catch (SQLException e) {
                     logger.debug("", e);
                 }
             }
             return holdings;
         } catch (SQLException e) {
             throw new DAOException("", e);
         } finally {
             if (selectHoldings != null) {
                 try {
                     selectHoldings.close();
                 } catch (SQLException e) {
                     logger.debug("", e);
                 }
             }
         }
     }
 
 //userid, usd, eur, gbp, cny, inr
     
 	public boolean insertWallet(Wallet wallet) throws DAOException {
 		PreparedStatement insertWallet = null;
 		boolean insertSuccess = true;
         try {
             insertWallet = sqlConnection.prepareStatement(SQL_INSERT_WALLET);
             insertWallet.setString(1, wallet.getUserID());
             insertWallet.setBigDecimal(2, wallet.getUsd());
             insertWallet.setBigDecimal(3, wallet.getEur());
             insertWallet.setBigDecimal(4, wallet.getGbp());
             insertWallet.setBigDecimal(5, wallet.getCny());
             insertWallet.setBigDecimal(6, wallet.getInr());
             insertWallet.executeUpdate();
         } catch (SQLException e) {
         	insertSuccess = false;
             throw new DAOException("", e);
         } finally {
             if (insertWallet != null) {
                 try {
                     insertWallet.close();
                 } catch (SQLException e) {
                     logger.debug("", e);
                 }
             }
         }
         return insertSuccess;
 	}
 	//userid, usd, eur, gbp, cny, inr
 	public Wallet getWallet(String userID) throws DAOException {
 		PreparedStatement selectWallet = null;
         try {
             selectWallet = sqlConnection.prepareStatement(SQL_SELECT_WALLET);
             selectWallet.setString(1, userID);
             ResultSet rs = selectWallet.executeQuery();
             try {
                 while (rs.next()) {
             		Wallet wallet = new Wallet(
             				rs.getString(1),
             				rs.getBigDecimal(2),
             				rs.getBigDecimal(3),
             				rs.getBigDecimal(4),
             				rs.getBigDecimal(5),
             				rs.getBigDecimal(6)
             			); 
                     return wallet;
                 }
             } finally {
                 try {
                     rs.close();
                 } catch (SQLException e) {
                     logger.debug("", e);
                 }
             }
         } catch (SQLException e) {
             throw new DAOException("", e);
         } finally {
             if (selectWallet != null) {
                 try {
                     selectWallet.close();
                 } catch (SQLException e) {
                     logger.debug("", e);
                 }
             }
         }
         return null;
 	}
 
 	//userid, usd, eur, gbp, cny, inr
 	public Wallet updateWallet(Wallet wallet) throws DAOException {
 		PreparedStatement updateWallet = null;
         try {
             updateWallet = sqlConnection.prepareStatement(SQL_UPDATE_WALLET);
             updateWallet.setBigDecimal(1, wallet.getUsd());
             updateWallet.setBigDecimal(2, wallet.getEur());
             updateWallet.setBigDecimal(3, wallet.getGbp());
             updateWallet.setBigDecimal(4, wallet.getCny());
             updateWallet.setBigDecimal(5, wallet.getInr());
             updateWallet.setString(6, wallet.getUserID());
             updateWallet.executeUpdate();
             return wallet;
         } catch (SQLException e) {
             throw new DAOException("", e);
         } finally {
             if (updateWallet != null) {
                 try {
                     updateWallet.close();
                 } catch (SQLException e) {
                     logger.debug("", e);
                 }
             }
         }
 	}
 
 	
 	
 
 //	public List<String> getUserList() throws DAOException {	
 //		PreparedStatement selectUserList = null;
 //    	try {
 //			selectUserList = sqlConnection.prepareStatement(SQL_SELECT_USER_LIST);
 //			ResultSet rs = selectUserList.executeQuery();
 //			List<String> userList = new ArrayList<String>();
 //			
 //			while(rs.next()){
 //				userList.add(rs.getString(1));
 //			}			
 //			return userList;
 //    	} catch (SQLException e) {
 //            throw new DAOException("", e);
 //        } finally {
 //            if (selectUserList != null) {
 //                try {
 //                    selectUserList.close();
 //                } catch (SQLException e) {
 //                    logger.debug("", e);
 //                }
 //            }
 //        }
 //	}
 }
