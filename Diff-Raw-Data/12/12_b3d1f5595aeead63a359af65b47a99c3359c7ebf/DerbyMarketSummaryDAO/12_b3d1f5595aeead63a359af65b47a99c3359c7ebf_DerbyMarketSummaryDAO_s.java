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
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.math.BigDecimal;
 import java.util.List;
 import java.util.ArrayList;
 
 import nl.tudelft.stocktrader.MarketSummary;
 import nl.tudelft.stocktrader.Quote;
 import nl.tudelft.stocktrader.dal.DAOException;
 import nl.tudelft.stocktrader.dal.MarketSummaryDAO;
 import nl.tudelft.stocktrader.util.StockTraderSQLUtil;
 import nl.tudelft.stocktrader.util.StockTraderUtility;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class DerbyMarketSummaryDAO extends AbstractDerbyDAO implements MarketSummaryDAO {
 	private static final Log logger = LogFactory.getLog(DerbyMarketSummaryDAO.class);
 
 	private static final String SQL_SELECT_ALL_QUOTES = "SELECT symbol, companyname, volume, price, open1, low, high, change1 FROM quote";
 	
 	private static final String SQL_SELECT_QUOTE = "SELECT symbol, companyname, volume, price, open1, low, high, change1 FROM quote WHERE symbol = ?";
 	private static final String SQL_SELECT_QUOTE_NOLOCK = "SELECT symbol, companyname, volume, price, open1, low, high, change1 FROM quote WHERE symbol = ?";
 	private static final String SQL_UPDATE_STOCKPRICEVOLUME = "UPDATE quote SET price = ?, low = ?, high = ?, change1 = ? - open1, volume = volume + ? WHERE symbol = ?";
 
 	private static final String SQL_SELECT_MARKETSUMMARY_GAINERS = "SELECT symbol, companyname, volume, price, open1, low, high, change1 FROM quote WHERE symbol LIKE 's:%' ORDER BY change1 DESC";
 	private static final String SQL_SELECT_MARKETSUMMARY_LOSERS = "SELECT symbol, companyname, volume, price, open1, low, high, change1 FROM quote WHERE symbol LIKE 's:%' ORDER BY change1";
 	private static final String SQL_SELECT_MARKETSUMMARY_TSIA = "SELECT SUM(price) / COUNT(*) as tsia FROM quote WHERE symbol LIKE 's:%'";
 	private static final String SQL_SELECT_MARKETSUMMARY_OPENTSIA = "SELECT SUM(open1) / COUNT(*) as opentsia FROM quote WHERE symbol LIKE 's:%'";
 	private static final String SQL_SELECT_MARKETSUMMARY_VOLUME = "SELECT SUM(volume) FROM quote WHERE symbol LIKE 's:%'";
 
 	public DerbyMarketSummaryDAO(Connection sqlConnection) throws DAOException {
 		super(sqlConnection);
 	}
 
 	public List<Quote> getAllQuotes() throws DAOException {
 		if (logger.isDebugEnabled()) {
 			logger.debug("MarketSummaryDAO.getAllQuotes()");
 		}
 		PreparedStatement selectQuote = null;
 		try {
 			selectQuote = sqlConnection.prepareStatement(SQL_SELECT_ALL_QUOTES);
 			ResultSet rs = selectQuote.executeQuery();
 
 			try {
 				ArrayList<Quote> quotes = new ArrayList<Quote>();
 				while (rs.next()) {
 					Quote quote = new Quote(
                             		rs.getString(1),
 		                            rs.getString(2),
 		                            rs.getDouble(3),
 									rs.getBigDecimal(4),
 		                            rs.getBigDecimal(5),
 		                            rs.getBigDecimal(6),
 		                            rs.getBigDecimal(7),
 		                            rs.getDouble(8));
 					
 					quotes.add(quote);
 				}
 				return quotes;
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
 			try {
 				if (selectQuote != null) {
 					selectQuote.close();
 				}
 			} catch (SQLException e) {
 				logger.debug("", e);
 			}
 		}
 		
 	}
 	
 	public List<Quote> getAllQuotesWithLimit(int start, int limit) throws DAOException {
 		if (logger.isDebugEnabled()) {
 			logger.debug("MarketSummaryDAO.getAllQuotesWithLimit(int, int)");
 		}
 		
 		throw new DAOException("NOT YET IMPLEMENTED");
 	}
 	
 	public Quote getQuote(String symbol) throws DAOException {
 		if (logger.isDebugEnabled()) {
 			logger.debug("MarketSummaryDAO.getQouteForUpdate(String)\nSymbol :"+ symbol);
 		}
 		PreparedStatement selectQuote = null;
 		try {
 			selectQuote = sqlConnection.prepareStatement(SQL_SELECT_QUOTE_NOLOCK);
 			selectQuote.setString(1, symbol);
 			ResultSet rs = selectQuote.executeQuery();
 
 			try {
 				Quote quote = null;
 				if (rs.next()) {
 					quote = new Quote(
                             rs.getString(1),
                             rs.getString(2),
                             rs.getDouble(3),
 							rs.getBigDecimal(4),
                             rs.getBigDecimal(5),
                             rs.getBigDecimal(6),
                             rs.getBigDecimal(7),
                             rs.getDouble(8));
 				}
 				return quote;
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
 			try {
 				if (selectQuote != null) {
 					selectQuote.close();
 				}
 			} catch (SQLException e) {
 				logger.debug("", e);
 			}
 		}
 	}
 
 	public Quote getQuoteForUpdate(String symbol) throws DAOException {
 		if (logger.isDebugEnabled()) {
 			logger.debug("MarketSummaryDAO.getQouteForUpdate(String)\nSymbol :"+ symbol);
 		}
 		PreparedStatement qouteForUpdateStat = null;
 		try {
 			qouteForUpdateStat = sqlConnection.prepareStatement(SQL_SELECT_QUOTE);
 			Quote quote = null;
 
 			qouteForUpdateStat.setString(1, symbol);
 			ResultSet rs = qouteForUpdateStat.executeQuery();
 
 			if (rs.next()) {
 				quote = new Quote(
                         rs.getString(1),
                         rs.getString(2),
 						rs.getDouble(3),
                         rs.getBigDecimal(4),
                         rs.getBigDecimal(5),
                         rs.getBigDecimal(6),
                         rs.getBigDecimal(7),
                         rs.getDouble(8));
 
 				try {
 					rs.close();
 				} catch (SQLException e) {
 					logger.debug("", e);
 				}
 				return quote;
 			} else {
 				throw new DAOException("No quote entry found");
 			}
 		} catch (SQLException e) {
 			throw new DAOException("", e);
 		} finally {
 			try {
 				if (qouteForUpdateStat != null) {
 					qouteForUpdateStat.close();
 				}
 			} catch (SQLException e) {
 				logger.debug("", e);
 			}
 		}
 	}
 
 	public void updateStockPriceVolume(double quantity, Quote quote) throws DAOException {
 		BigDecimal priceChangeFactor = StockTraderUtility.getRandomPriceChangeFactor(quote.getPrice());
 		BigDecimal newPrice = quote.getPrice().multiply(priceChangeFactor);
 
 		if (newPrice.compareTo(quote.getLow()) == -1) {
 			quote.setLow(newPrice);
 		}
 		if (newPrice.compareTo(quote.getHigh()) == 1) {
 			quote.setHigh(newPrice);
 		}
 
 		PreparedStatement updateStockPriceVolumeStat = null;
 		try {
 			updateStockPriceVolumeStat = sqlConnection.prepareStatement(SQL_UPDATE_STOCKPRICEVOLUME);
 			updateStockPriceVolumeStat.setBigDecimal(1, newPrice);
 			updateStockPriceVolumeStat.setBigDecimal(2, quote.getLow());
 			updateStockPriceVolumeStat.setBigDecimal(3, quote.getHigh());
 			updateStockPriceVolumeStat.setBigDecimal(4, newPrice);
 			updateStockPriceVolumeStat.setFloat(5, (float) quantity);
 			updateStockPriceVolumeStat.setString(6, quote.getSymbol());
 			updateStockPriceVolumeStat.executeUpdate();
 
 		} catch (SQLException e) {
 			throw new DAOException("", e);
 		} finally {
 			try {
 				if (updateStockPriceVolumeStat != null) {
 					updateStockPriceVolumeStat.close();
 				}
 			} catch (SQLException e) {
 				logger.debug("", e);
 			}
 		}
 	}
 
 	public MarketSummary getCustomMarketSummary() throws DAOException {
 		BigDecimal tSIA = (BigDecimal) StockTraderSQLUtil.executeScalarNoParm(SQL_SELECT_MARKETSUMMARY_TSIA, sqlConnection);
 		BigDecimal openTSIA = (BigDecimal) StockTraderSQLUtil.executeScalarNoParm(SQL_SELECT_MARKETSUMMARY_OPENTSIA, sqlConnection);
		double totalVolume = ((Double) StockTraderSQLUtil.executeScalarNoParm(SQL_SELECT_MARKETSUMMARY_VOLUME, sqlConnection)).doubleValue();
 
 		List<Quote> topGainers = new ArrayList<Quote>();
 		PreparedStatement gainers = null;
 		try {
 			gainers = sqlConnection.prepareStatement(SQL_SELECT_MARKETSUMMARY_GAINERS);
 			ResultSet rs = gainers.executeQuery();
 
 			try {
 				for (int i = 0; rs.next() && i < 5; i++) {
 					Quote quote = new Quote(
 							rs.getString(1),
                             rs.getString(2),
                             rs.getDouble(3),
 							rs.getBigDecimal(4),
                             rs.getBigDecimal(5),
                             rs.getBigDecimal(6),
                             rs.getBigDecimal(7),
                             rs.getDouble(8));
 					topGainers.add(quote);
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
 			if (gainers != null) {
 				try {
 					gainers.close();
 				} catch (SQLException e) {
 					logger.debug("", e);
 				}
 			}
 		}
 		List<Quote> topLosers = new ArrayList<Quote>();
 		PreparedStatement losers = null;
 		try {
 			losers = sqlConnection.prepareStatement(SQL_SELECT_MARKETSUMMARY_LOSERS);
 			ResultSet rs = losers.executeQuery();
 
 			try {
 				for (int i = 0; rs.next() && i < 5; i++) {
 					Quote quote = new Quote(
 							rs.getString(1),
                             rs.getString(2),
                             rs.getDouble(3),
 							rs.getBigDecimal(4),
                             rs.getBigDecimal(5),
                             rs.getBigDecimal(6),
                             rs.getBigDecimal(7),
                             rs.getDouble(8));
 					topLosers.add(quote);
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
 			if (losers != null) {
 				try {
 					losers.close();
 				} catch (SQLException e) {
 					logger.debug("", e);
 				}
 			}
 		}
 		MarketSummary marketSummary = new MarketSummary(
 				tSIA, openTSIA, totalVolume, topGainers, topLosers);
 		return marketSummary;
 	}
 }
