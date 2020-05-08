 package net.toddsarratt.GaussTrader;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.sql.DataSource;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 /**
  * Created by tsarratt on 10/7/2014.
  */
 public abstract class WatchList {
    private static DataSource dataSource = GaussTrader.getDataSource();
    private static Connection dbConnection;
    private static final Logger LOGGER = LoggerFactory.getLogger(DBHistoricalPrices.class);
 
    static {
       try {
          dbConnection = dataSource.getConnection();
       } catch (SQLException sqle) {
          LOGGER.warn("SQLException : dataSource.getConnection()", sqle);
       }
    }
 
    protected static void updateDbLastTick(Stock stockToUpdate) {
       LOGGER.debug("Entering WatchList.updateDb(Stock {})", stockToUpdate.getTicker());
       String ticker = stockToUpdate.getTicker();
       double currentPrice = stockToUpdate.getPrice();
       long lastTickEpoch = stockToUpdate.getLastPriceUpdateEpoch();
       PreparedStatement sqlStatement;
       int insertedRowCount;
       try {
          LOGGER.debug("Getting connection to {}", GaussTrader.DB_NAME);
          LOGGER.debug("Inserting current stock price for ticker {} into database.", ticker);
          dbConnection = dataSource.getConnection();
          sqlStatement = dbConnection.prepareStatement("UPDATE watchlist SET last_tick = ?, last_tick_epoch = ? WHERE ticker = ?");
          sqlStatement.setDouble(1, currentPrice);
          sqlStatement.setDouble(2, lastTickEpoch);
          sqlStatement.setString(3, ticker);
          LOGGER.debug("Executing UPDATE watchlist SET last_tick = {}, last_tick_epoch = {} WHERE ticker = {}", currentPrice, lastTickEpoch, ticker);
          if ((insertedRowCount = sqlStatement.executeUpdate()) != 1) {
             LOGGER.warn("Inserted {} rows. Should have inserted 1 row.", insertedRowCount);
          }
       } catch (SQLException sqle) {
          LOGGER.info("Unable to get connection to {}", GaussTrader.DB_NAME);
          LOGGER.debug("Caught (SQLException sqle)", sqle);
       }
    }
    protected static void resetWatchList() {
       LOGGER.debug("Entering WatchList.resetWatchList()");
       PreparedStatement sqlStatement;
       try {
          LOGGER.debug("Getting connection to {}", GaussTrader.DB_NAME);
          dbConnection = dataSource.getConnection();
          sqlStatement = dbConnection.prepareStatement("UPDATE watchlist SET active = FALSE");
          LOGGER.debug("Executing UPDATE watchlist SET active = FALSE");
          sqlStatement.executeUpdate();
       } catch (SQLException sqle) {
          LOGGER.info("Unable to get connection to {}", GaussTrader.DB_NAME);
          LOGGER.debug("Caught (SQLException sqle)", sqle);
       }
    }
    protected static void updateDbStockMetrics(Stock stockToUpdate) {
       LOGGER.debug("Entering WatchList.updateDb(Stock {})", stockToUpdate.getTicker());
       PreparedStatement sqlUniquenessStatement;
       PreparedStatement sqlUpdateStatement;
       try {
          String ticker = stockToUpdate.getTicker();
          sqlUniquenessStatement = dbConnection.prepareStatement("SELECT DISTINCT ticker FROM watchlist WHERE ticker = ?");
          sqlUniquenessStatement.setString(1, ticker);
          LOGGER.debug("Executing SELECT DISTINCT ticker FROM watchlist WHERE ticker = {}", ticker);
          ResultSet tickerInDbResultSet = sqlUniquenessStatement.executeQuery();
          if (tickerInDbResultSet.next()) {
             sqlUpdateStatement = dbConnection.prepareStatement("UPDATE watchlist SET twenty_dma = ?, low_boll = ?, high_boll = ?, " +
                     "last_tick = ?, last_tick_epoch = ?, active = TRUE WHERE ticker = ?");
          } else {
             sqlUpdateStatement = dbConnection.prepareStatement("INSERT INTO watchlist (twenty_dma, low_boll, high_boll, " +
                     "last_tick, last_tick_epoch, active, ticker) VALUES (?, ?, ?, ?, ?, TRUE, ?)");
          }
          sqlUpdateStatement.setDouble(1, stockToUpdate.getTwentyDma());
          sqlUpdateStatement.setDouble(2, stockToUpdate.getBollingerBand(3));
          sqlUpdateStatement.setDouble(3, stockToUpdate.getBollingerBand(1));
          sqlUpdateStatement.setDouble(4, stockToUpdate.getPrice());
          sqlUpdateStatement.setLong(5, stockToUpdate.getLastPriceUpdateEpoch());
          sqlUpdateStatement.setString(6, stockToUpdate.getTicker());
         LOGGER.debug("Executing SQL insert into watchlist table");
         sqlUpdateStatement.executeUpdate();
       } catch (SQLException sqle) {
          LOGGER.info("SQLException attempting to update DB table watchlist for {}", stockToUpdate.getTicker());
          LOGGER.debug("Exception", sqle);
       }
    }
 }
