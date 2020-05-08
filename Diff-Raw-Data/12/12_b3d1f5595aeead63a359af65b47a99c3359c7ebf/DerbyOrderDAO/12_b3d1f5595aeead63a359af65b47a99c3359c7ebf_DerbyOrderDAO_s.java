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
 
 import nl.tudelft.stocktrader.Holding;
 import nl.tudelft.stocktrader.Order;
 import nl.tudelft.stocktrader.Quote;
 import nl.tudelft.stocktrader.dal.CustomerDAO;
 import nl.tudelft.stocktrader.dal.DAOException;
 import nl.tudelft.stocktrader.dal.DAOFactory;
 import nl.tudelft.stocktrader.dal.MarketSummaryDAO;
 import nl.tudelft.stocktrader.dal.OrderDAO;
 import nl.tudelft.stocktrader.util.StockTraderUtility;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.math.BigDecimal;
 import java.sql.*;
 import java.util.Calendar;
 
 public class DerbyOrderDAO extends AbstractDerbyDAO implements OrderDAO {
 	private static Log logger = LogFactory.getLog(DerbyOrderDAO.class);
  
 	private static final String SQL_GET_ACCOUNTID_ORDER = "SELECT account_accountid FROM orders WHERE orderid = ?";
 	private static final String SQL_INSERT_HOLDING = "INSERT INTO holding (purchaseprice, quantity, purchasedate, account_accountid, quote_symbol, holdingid) VALUES (?, ?, ?, ?, ?, null)";
 	private static final String SQL_UPDATE_HOLDING = "UPDATE holding SET quantity =quantity - ? WHERE holdingid = ?";
 	private static final String SQL_DELETE_HOLDING = "DELETE FROM holding WHERE holdingid = ?";
 	private static final String SQL_SELECT_HOLDING = "SELECT holdingid, quantity, purchaseprice, purchasedate, quote_symbol, account_accountid FROM holding WHERE holdingid = ?";
 	private static final String SQL_UPDATE_ORDER = "UPDATE orders SET quantity = ? WHERE orderid = ?";
 	private static final String SQL_CLOSE_ORDER = "UPDATE orders SET orderstatus = ?, completiondate = current_timestamp, holding_holdingid = ?, price = ? WHERE orderid = ?";
 	private static final String SQL_GET_ACCOUNTID = "SELECT accountid FROM account  WHERE profile_userid = ?";
    private static final String SQL_GET_LAST_INSERT_ID = "SELECT LAST_INSERT_ID()";
 
 	// CHECKME
 	private static final String SQL_INSERT_ORDER = "INSERT INTO orders (opendate, orderfee, price, quote_symbol, quantity, ordertype, orderstatus, account_accountid, holding_holdingid) VALUES (current_timestamp, ?, ?, ?, ?, ?, 'open', ?, ?)";
	private static final String SQL_SELECT_ORDER_ID = "SELECT LAST_INSERT_ID() FROM orders WHERE orderfee = ? AND price = ? AND quote_symbol = ? AND quantity = ? AND ordertype = ? AND orderstatus = ? AND account_accountid = ? AND holding_holdingid = ?";
 
 	public DerbyOrderDAO(Connection sqlConnection) throws DAOException {
 		super(sqlConnection);
 	}
 
 	public Quote getQuoteForUpdate(String symbol) throws DAOException {
 		if (logger.isDebugEnabled()) {
 			logger.debug("OrderDAO.getQuoteForUpdate()\nSymbol :" + symbol);
 		}
 
 		DAOFactory fac = DerbyDAOFactory.getInstance();
 		MarketSummaryDAO marketSummaryDAO = fac.getMarketSummaryDAO();
 		return marketSummaryDAO.getQuoteForUpdate(symbol);
 	}
 
 	public int createHolding(Order order) throws DAOException {
 		if (logger.isDebugEnabled()) {
 			logger.debug("OrderDAO.createHolding(OrderDataModel)\nOrderID :"
 					+ order.getOrderID() + "\nOrderType :"
 					+ order.getOrderType() + "\nSymbol :" + order.getSymbol()
 					+ "\nQuantity :" + order.getQuantity() + "\nOrder Status :"
 					+ order.getOrderStatus() + "\nOrder Open Date :"
 					+ order.getOpenDate() + "\nCompletionDate :"
 					+ order.getCompletionDate());
 		}
 
 		PreparedStatement getAccountIdStat = null;
 		int accountId = -1;
 
 		try {
 			getAccountIdStat = sqlConnection.prepareStatement(SQL_GET_ACCOUNTID_ORDER);
 			getAccountIdStat.setInt(1, order.getOrderID());
 
 			ResultSet rs = getAccountIdStat.executeQuery();
 			if (rs.next()) {
 				accountId = Integer.parseInt(rs.getString(1));
 				order.setAccountId(accountId);
 			}
 
 			try {
 				rs.close();
 			} catch (Exception e) {
 				logger.debug("", e);
 			}
 		} catch (SQLException e) {
 			throw new DAOException("Exception is thrown when selecting the accountID from order entries where order ID :" + order.getOrderID(), e);
 
 		} finally {
 			if (getAccountIdStat != null) {
 				try {
 					getAccountIdStat.close();
 				} catch (Exception e) {
 					logger.debug("", e);
 				}
 			}
 		}
 
 		if (accountId != -1) {
 			int holdingId = -1;
 			PreparedStatement insertHoldingStat = null;
 
 			try {
 				insertHoldingStat = sqlConnection.prepareStatement(SQL_INSERT_HOLDING);
 				insertHoldingStat.setBigDecimal(1, order.getPrice());
 				insertHoldingStat.setDouble(2, order.getQuantity());
 				Calendar openDate = (order.getOpenDate() != null) ? order.getOpenDate() : Calendar.getInstance();
 				insertHoldingStat.setDate(3, StockTraderUtility.convertToSqlDate(openDate));
 				insertHoldingStat.setInt(4, order.getAccountId());
 				insertHoldingStat.setString(5, order.getSymbol());
                 insertHoldingStat.executeUpdate();
 
 				ResultSet rs = sqlConnection.prepareCall(SQL_GET_LAST_INSERT_ID).executeQuery();
 				if (rs.next()) {
 					holdingId = rs.getInt(1);
 				}
 
 				try {
 					rs.close();
 				} catch (Exception e) {
 					logger.debug("", e);
 				}
 				return holdingId;
 
 			} catch (SQLException e) {
 				throw new DAOException("An exception is thrown during an insertion of a holding entry",e);
 
 			} finally {
 				if (insertHoldingStat != null) {
 					try {
 						insertHoldingStat.close();
 					} catch (Exception e) {
 						logger.debug("", e);
 					}
 				}
 			}
 		}
 		return -1;
 	}
 
 	public void updateHolding(int holdingId, double quantity) throws DAOException {
 		if (logger.isDebugEnabled()) {
 			logger.debug("OrderDAO.updateHolding()\nHolding ID :" + holdingId + "\nQuantity :" + quantity);
 		}
 
 		PreparedStatement updateHoldingStat = null;
 		try {
 			updateHoldingStat = sqlConnection.prepareStatement(SQL_UPDATE_HOLDING);
 			updateHoldingStat.setDouble(1, quantity);
 			updateHoldingStat.setInt(2, holdingId);
 			updateHoldingStat.executeUpdate();
 
 		} catch (SQLException e) {
 			throw new DAOException("An exception is thrown during an updation of holding entry", e);
 		} finally {
 			if (updateHoldingStat != null) {
 				try {
 					updateHoldingStat.close();
 				} catch (Exception e) {
 					logger.debug("", e);
 				}
 			}
 		}
 	}
 
 	public void deleteHolding(int holdingId) throws DAOException {
 		if (logger.isDebugEnabled()) {
 			logger.debug("OrderDAO.deleteHolding()\nHolding ID :" + holdingId);
 		}
 
 		PreparedStatement deleteHoldingStat = null;
 		try {
 			deleteHoldingStat = sqlConnection.prepareStatement(SQL_DELETE_HOLDING);
 			deleteHoldingStat.setInt(1, holdingId);
 			deleteHoldingStat.execute();
 
 		} catch (SQLException e) {
 			throw new DAOException("An exception is thrown during deletion of a holding entry",e);
 		} finally {
 			if (deleteHoldingStat != null) {
 				try {
 					deleteHoldingStat.close();
 				} catch (SQLException e) {
 					logger.debug("", e);
 				}
 			}
 		}
 	}
 
 	public Holding getHoldingForUpdate(int orderId)
 			throws DAOException {
 		if (logger.isDebugEnabled()) {
 			logger.debug("HoldingDataModel.getHoldingForUpdate()\nOrder ID :"+ orderId);
 		}
 		DAOFactory fac = DerbyDAOFactory.getInstance();
 		CustomerDAO customerDAO = fac.getCustomerDAO();
 		return customerDAO.getHoldingForUpdate(orderId);
 	}
 
 	public Holding getHolding(int holdingId) throws DAOException {
 		Holding holding = null;
 		PreparedStatement selectHoldingStat = null;
 		try {
 			selectHoldingStat = sqlConnection.prepareStatement(SQL_SELECT_HOLDING);
 			selectHoldingStat.setInt(1, holdingId);
 			ResultSet rs = selectHoldingStat.executeQuery();
 			if (rs.next()) {
 				try {
 					holding = new Holding(
 							rs.getInt(1),
 							rs.getDouble(2),
 							rs.getBigDecimal(3),
 							StockTraderUtility.convertToCalendar(rs.getDate(4)),
 							rs.getString(5),
 							rs.getInt(6));
 					return holding;
 
 				} finally {
 					try {
 						rs.close();
 					} catch (Exception e) {
 						logger.debug("", e);
 					}
 				}
 			}
 		} catch (SQLException e) {
 			throw new DAOException("An Exception is thrown during selecting a holding entry",e);
 		} finally {
 			if (selectHoldingStat != null) {
 				try {
 					selectHoldingStat.close();
 				} catch (SQLException e) {
 					logger.debug("", e);
 				}
 			}
 		}
 		return holding;
 	}
 
 	public void updateAccountBalance(int accountId, BigDecimal total) throws DAOException {
 		if (logger.isDebugEnabled()) {
 			logger.debug("OrderDAO.updateAccountBalance(int,BigDecimal)\nAccount ID :" + accountId + "\nTotal :" + total);
 		}
 		DAOFactory fac = DerbyDAOFactory.getInstance();
 		CustomerDAO customerDAO = fac.getCustomerDAO();
 		customerDAO.updateAccountBalance(accountId, total);
 	}
 
 	public void updateStockPriceVolume(double quantity, Quote quote) throws DAOException {
 		if (logger.isDebugEnabled()) {
 			logger.debug("OrderDAO.updateStockPriceVolume(double,QuatedataModle)\nQuantity :" + quantity + "\nQuote\nSymbol" + quote.getSymbol());
 		}
 		DAOFactory fac = DerbyDAOFactory.getInstance();
 		MarketSummaryDAO marketSummaryDAO = fac.getMarketSummaryDAO();
 		marketSummaryDAO.updateStockPriceVolume(quantity, quote);
 	}
 
 	public void updateOrder(Order order) throws DAOException {
 		PreparedStatement updateHoldingStat = null;
 		try {
 			updateHoldingStat = sqlConnection.prepareStatement(SQL_UPDATE_ORDER);
 			updateHoldingStat.setDouble(1, order.getQuantity());
 			updateHoldingStat.setInt(2, order.getOrderID());
 			updateHoldingStat.executeUpdate();
 
 		} catch (SQLException e) {
 			throw new DAOException("An Exception is thrown during updating a holding entry", e);
 		} finally {
 			if (updateHoldingStat != null) {
 				try {
 					updateHoldingStat.close();
 				} catch (Exception e) {
 					logger.debug("", e);
 				}
 			}
 		}
 	}
 
 	public void closeOrder(Order order) throws DAOException {
 		if (logger.isDebugEnabled()) {
 			logger.debug("OrderDAO.closeOrder(OrderDataModel)\nOrderID :"
 					+ order.getOrderID() + "\nOrderType :"
 					+ order.getOrderType() + "\nSymbol :" + order.getSymbol()
 					+ "\nQuantity :" + order.getQuantity() + "\nOrder Status :"
 					+ order.getOrderStatus() + "\nOrder Open Date :"
 					+ order.getOpenDate() + "\nCompletionDate :"
 					+ order.getCompletionDate());
 		}
 
 		PreparedStatement closeOrderStat = null;
 		try {
 			closeOrderStat = sqlConnection.prepareStatement(SQL_CLOSE_ORDER);
 			closeOrderStat.setString(1, StockTraderUtility.ORDER_STATUS_CLOSED);
 			if (StockTraderUtility.ORDER_TYPE_SELL.equals(order.getOrderType())) {
 				closeOrderStat.setNull(2, Types.INTEGER);
 			} else {
 				closeOrderStat.setInt(2, order.getHoldingId());
 			}
 			closeOrderStat.setBigDecimal(3, order.getPrice());
 			closeOrderStat.setInt(4, order.getOrderID());
 			closeOrderStat.executeUpdate();
 
 		} catch (SQLException e) {
 			throw new DAOException("", e);
 
 		} finally {
 			if (closeOrderStat != null) {
 				try {
 					closeOrderStat.close();
 				} catch (Exception e) {
 					logger.debug("", e);
 				}
 			}
 		}
 	}
 
 	public Order createOrder(String userID, String symbol, String orderType, double quantity, int holdingID) throws DAOException {
 		int orderID = 0;
 		Calendar minCalender = Calendar.getInstance();
 		minCalender.setTimeInMillis(0);
 		Order order = new Order(
                 orderID,
                 orderType,
 				StockTraderUtility.ORDER_STATUS_OPEN,
                 Calendar.getInstance(),
 				minCalender,
                 quantity,
                 BigDecimal.valueOf(1),
 				StockTraderUtility.getOrderFee(orderType),
                 symbol);
 		order.setHoldingId(holdingID);
 
 		PreparedStatement getAccountId = null;
 		try {
 			getAccountId = sqlConnection.prepareStatement(SQL_GET_ACCOUNTID);
 			getAccountId.setString(1, userID);
 			ResultSet rs = getAccountId.executeQuery();
 			if (rs.next()) {
 				order.setAccountId(rs.getInt(1));
 			}
 		} catch (SQLException e) {
 
 		} finally {
 			if (getAccountId != null) {
 				try {
 					getAccountId.close();
 				} catch (SQLException e) {
 					logger.debug("", e);
 				}
 			}
 		}
 
 		PreparedStatement insertOrder = null;
 		PreparedStatement selectOrderID = null;
 		try {
 			insertOrder = sqlConnection.prepareStatement(SQL_INSERT_ORDER);
 			insertOrder.setBigDecimal(1, order.getOrderFee());
 			insertOrder.setBigDecimal(2, order.getPrice());
 			insertOrder.setString(3, order.getSymbol());
             // FIXED: metro used Double rather than double
 //			insertOrder.setFloat(4, (float) order.getQuantity());
 			insertOrder.setFloat(4, order.getQuantity().floatValue());
 			insertOrder.setString(5, order.getOrderType());
 			insertOrder.setInt(6, order.getAccountId());
 			insertOrder.setInt(7, order.getHoldingId());
 			insertOrder.executeUpdate();
 
 
 			selectOrderID = sqlConnection.prepareStatement(SQL_SELECT_ORDER_ID);
 			// ORDERFEE = ? AND PRICE = ? AND QUOTE_SYMBOL = ? AND QUANTITY = ?
 			// ORDERTYPE = ? ORDERSTATUS = ? AND ACCOUNT_ACCOUNTID = ?
 			// HOLDING_HOLDINGID = ?"
 			selectOrderID.setBigDecimal(1, order.getOrderFee());
 			selectOrderID.setBigDecimal(2, order.getPrice());
 			selectOrderID.setString(3, order.getSymbol());
 			selectOrderID.setDouble(4, order.getQuantity());
 			selectOrderID.setString(5, order.getOrderType());
 			selectOrderID.setString(6, "open");
 			selectOrderID.setInt(7, order.getAccountId());
 			selectOrderID.setInt(8, order.getHoldingId());
 			ResultSet rs = selectOrderID.executeQuery();
 			if (rs.next()) {
 				try {
 					order.setOrderID(rs.getInt(1));
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
 			if (insertOrder != null) {
 				try {
 					insertOrder.close();
 				} catch (SQLException e) {
 					logger.debug("", e);
 				}
 			}
 			if (selectOrderID != null) {
 				try {
 					selectOrderID.close();
 				} catch (SQLException e) {
 					logger.debug("", e);
 				}
 			}
 		}
 		return order;
 	}
 }
 
