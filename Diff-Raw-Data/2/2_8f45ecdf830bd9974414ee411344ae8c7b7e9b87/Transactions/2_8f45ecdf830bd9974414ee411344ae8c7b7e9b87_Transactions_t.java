 package no.kantega.android.afp.controllers;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.util.Log;
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.dao.GenericRawResults;
 import com.j256.ormlite.stmt.QueryBuilder;
 import no.kantega.android.afp.models.*;
 import no.kantega.android.afp.utils.DatabaseHelper;
 import no.kantega.android.afp.utils.FmtUtil;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 public class Transactions {
 
     private static final String TAG = Transactions.class.getSimpleName();
     private static final String SQLITE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
     private final DatabaseHelper helper;
     private final Dao<Transaction, Integer> transactionDao;
     private final Dao<TransactionTag, Integer> transactionTagDao;
     private final Dao<TransactionType, Integer> transactionTypeDao;
 
     public Transactions(Context context) {
         this.helper = new DatabaseHelper(context);
         this.transactionDao = helper.getTransactionDao();
         this.transactionTagDao = helper.getTransactionTagDao();
         this.transactionTypeDao = helper.getTransactionTypeDao();
     }
 
     public Transactions(Context context, DatabaseHelper helper) {
         this.helper = helper;
         this.transactionDao = helper.getTransactionDao();
         this.transactionTagDao = helper.getTransactionTagDao();
         this.transactionTypeDao = helper.getTransactionTypeDao();
     }
 
     /**
      * Add a new transaction tag
      *
      * @param tag Transaction tag to save
      * @return The newly added tag or the existing one
      */
     private TransactionTag insertIgnore(TransactionTag tag) {
         if (tag == null) {
             return null;
         }
         try {
             QueryBuilder<TransactionTag, Integer> queryBuilder = transactionTagDao.queryBuilder();
             queryBuilder.where().eq("name", tag.getName());
             List<TransactionTag> tags = transactionTagDao.query(queryBuilder.prepare());
             if (tags.size() > 0) {
                 return tags.get(0);
             } else {
                 transactionTagDao.create(tag);
                 tags = transactionTagDao.query(queryBuilder.prepare());
                 return tags.get(0);
             }
         } catch (SQLException e) {
             Log.e(TAG, "Failed to add transaction tag", e);
             return null;
         }
     }
 
     /**
      * Add a new transaction type
      *
      * @param type Transaction type to save
      * @return The newly added tag or the existing one
      */
     private TransactionType insertIgnore(TransactionType type) {
         if (type == null) {
             return null;
         }
         try {
             QueryBuilder<TransactionType, Integer> queryBuilder = transactionTypeDao.queryBuilder();
             queryBuilder.where().eq("name", type.getName());
             List<TransactionType> types = transactionTypeDao.query(queryBuilder.prepare());
             if (types.size() > 0) {
                 return types.get(0);
             } else {
                 transactionTypeDao.create(type);
                 types = transactionTypeDao.query(queryBuilder.prepare());
                 return types.get(0);
             }
         } catch (SQLException e) {
             Log.e(TAG, "Failed to add transaction type", e);
             return null;
         }
     }
 
     /**
      * Add a new transaction
      *
      * @param t Transaction to save
      */
     public void add(Transaction t) {
         try {
             t.setTag(insertIgnore(t.getTag()));
             t.setType(insertIgnore(t.getType()));
             transactionDao.create(t);
         } catch (SQLException e) {
             Log.e(TAG, "Failed to add transaction", e);
         }
     }
 
     /**
      * Add a new tag
      *
      * @param t Transaction tag to save
      */
     public void add(TransactionTag t) {
         insertIgnore(t);
     }
 
     /**
      * Update a transaction
      *
      * @param t Transaction tag to save
      */
     public void update(Transaction t) {
         try {
             t.setTag(insertIgnore(t.getTag()));
             t.setType(insertIgnore(t.getType()));
             transactionDao.update(t);
         } catch (SQLException e) {
             Log.e(TAG, "Failed to update transaction", e);
         }
     }
 
     /**
      * Retrieve a list of transactions ordered descending by date
      *
      * @param limit Max number of transactions to retrieve
      * @return List of transactions
      */
     public List<Transaction> get(final int limit) {
         QueryBuilder<Transaction, Integer> queryBuilder = transactionDao.
                 queryBuilder();
         queryBuilder.limit(limit);
         return get(queryBuilder);
     }
 
     /**
      * Get transaction by id
      *
      * @param id ID of transaction
      * @return The transaction or null if not found
      */
     public Transaction getById(final int id) {
         List<Transaction> transactions = Collections.emptyList();
         QueryBuilder<Transaction, Integer> queryBuilder = transactionDao.
                 queryBuilder();
         queryBuilder.limit(1);
         try {
             queryBuilder.setWhere(queryBuilder.where().eq("_id", id));
             transactions = transactionDao.query(queryBuilder.prepare());
         } catch (SQLException e) {
             Log.e(TAG, "Failed to find transaction", e);
         }
         return transactions.isEmpty() ? null : transactions.get(0);
     }
 
     /**
      * Retrieve a list of changed transactions
      *
      * @return List of transactions
      */
     public List<Transaction> getChanged() {
         QueryBuilder<Transaction, Integer> queryBuilder = transactionDao.queryBuilder();
         try {
             queryBuilder.setWhere(queryBuilder.where().eq("changed", true));
         } catch (SQLException e) {
             Log.e(TAG, "Failed to set where condition", e);
         }
         return get(queryBuilder);
     }
 
     /**
      * Retrieve a list of dirty transactions (which should be synchronized)
      *
      * @return List of transactions
      */
     public List<Transaction> getDirty() {
         QueryBuilder<Transaction, Integer> queryBuilder = transactionDao.
                 queryBuilder();
         try {
             queryBuilder.setWhere(queryBuilder.where().eq("dirty", true));
         } catch (SQLException e) {
             Log.e(TAG, "Failed to set where condition", e);
         }
         return get(queryBuilder);
     }
 
     /**
      * Get the latest external transaction
      *
      * @return Transaction
      */
     public Transaction getLatestExternal() {
         QueryBuilder<Transaction, Integer> queryBuilder = transactionDao.
                 queryBuilder();
         try {
             queryBuilder.setWhere(queryBuilder.where().eq("internal", false));
         } catch (SQLException e) {
             Log.e(TAG, "Failed to set where condition", e);
         }
         List<Transaction> transactions = get(queryBuilder);
         return transactions.isEmpty() ? null : transactions.get(0);
     }
 
     /**
      * Retrieve a list of transactions using the given query builder
      *
      * @param queryBuilder Query builder
      * @return List of transactions
      */
     private List<Transaction> get(QueryBuilder<Transaction, Integer> queryBuilder) {
         List<Transaction> transactions = Collections.emptyList();
         try {
             queryBuilder.orderBy("accountingDate", false).
                     orderBy("timestamp", false);
             transactions = transactionDao.query(queryBuilder.prepare());
         } catch (SQLException e) {
             Log.e(TAG, "Failed to retrieve transactions", e);
         }
         return transactions;
     }
 
     /**
      * Get a cursor for transactions
      *
      * @param onlyExternal Only fetch external transactions
      * @return Cursor
      */
     public Cursor getCursor(boolean onlyExternal) {
         String selection = null;
         String[] selectionArgs = null;
         if (onlyExternal) {
             selection = "internal = ?";
             selectionArgs = new String[]{"false"};
         }
         final Cursor cursor = helper.getReadableDatabase().query(
                 "transactions " +
                         "LEFT JOIN transactiontypes " +
                         "ON transactiontypes.id = transactions.type_id " +
                         "LEFT JOIN transactiontags " +
                         "ON transactiontags.id = transactions.tag_id"
                 , new String[]{"*", "transactiontypes.name AS type",
                         "transactiontags.name AS tag",
                         "transactiontags.imageId as imageId"}, selection,
                 selectionArgs, null, null,
                 "accountingdate DESC, timestamp DESC", null);
         return cursor;
     }
 
     public Cursor getCursorAfterTimestamp(long timestamp) {
         String selection = "internal = ? AND timestamp > ?";
        String[] selectionArgs = new String[]{"0",
                 String.valueOf(timestamp)};
         final Cursor cursor = helper.getReadableDatabase().query(
                 "transactions " +
                         "LEFT JOIN transactiontypes " +
                         "ON transactiontypes.id = transactions.type_id " +
                         "LEFT JOIN transactiontags " +
                         "ON transactiontags.id = transactions.tag_id"
                 , new String[]{"*", "transactiontypes.name AS type",
                         "transactiontags.name AS tag",
                         "transactiontags.imageId as imageId"}, selection,
                 selectionArgs, null, null,
                 "accountingdate DESC, timestamp DESC", null);
         return cursor;
     }
 
     /**
      * Retrieve total transaction count
      *
      * @return Transaction count
      */
     public int getCount() {
         return getCount("transactions");
     }
 
     /**
      * Retrieve total transaction tag count
      *
      * @return Transaction tag count
      */
     public int getTagCount() {
         return getCount("transactiontags");
     }
 
     /**
      * Retrieve number of dirty (unsynced) transactions
      *
      * @return Number of unsynced transactions
      */
     public int getDirtyCount() {
         try {
             GenericRawResults<String[]> rawResults = transactionDao.
                     queryRaw("SELECT COUNT(*) FROM transactions WHERE dirty = 1 LIMIT 1");
             return Integer.parseInt(rawResults.getResults().get(0)[0]);
         } catch (SQLException e) {
             Log.e(TAG, "Failed to retrieve transaction count", e);
         }
         return 0;
     }
 
     /**
      * Retrieve number of untagged transactions
      *
      * @return Untagged count
      */
     public int getUntaggedCount() {
         try {
             GenericRawResults<String[]> rawResults = transactionDao.
                     queryRaw("SELECT COUNT(*) FROM transactions " +
                             "WHERE tag_id IS NULL LIMIT 1");
             return Integer.parseInt(rawResults.getResults().get(0)[0]);
         } catch (SQLException e) {
             Log.e(TAG, "Failed to retrieve transaction count", e);
         }
         return 0;
     }
 
     private int getCount(String table) {
         try {
             GenericRawResults<String[]> rawResults = transactionDao.
                     queryRaw(String.format("SELECT COUNT(*) FROM %s", table));
             return Integer.parseInt(rawResults.getResults().get(0)[0]);
         } catch (SQLException e) {
             Log.e(TAG, "Failed to retrieve transaction count", e);
         }
         return 0;
     }
 
     /**
      * Retrieve a list of aggregated tags sorted by sum of all transactions in that tag
      *
      * @param limit Max number of aggregated tags to retrieve
      * @return List of aggregated tags
      */
     public List<AggregatedTag> getAggregatedTags(final int limit) {
         List<AggregatedTag> aggregatedTags = new ArrayList<AggregatedTag>();
         try {
             GenericRawResults<String[]> rawResults = transactionDao.queryRaw(
                     "SELECT transactiontags.name, SUM(amountOut) AS sum " +
                             "FROM transactions " +
                             "INNER JOIN transactiontags ON transactiontags.id = transactions.tag_id " +
                             "GROUP BY transactiontags.name " +
                             "ORDER BY sum DESC LIMIT ?", String.valueOf(limit));
             for (String[] row : rawResults) {
                 AggregatedTag aggregatedTag = new AggregatedTag();
                 aggregatedTag.setName(row[0]);
                 aggregatedTag.setAmount(Double.parseDouble(row[1]));
                 aggregatedTags.add(aggregatedTag);
             }
             rawResults.close();
         } catch (SQLException e) {
             Log.e(TAG, "Failed to retrieve aggregated tags", e);
         }
         return aggregatedTags;
     }
 
     /**
      * Get all transaction tags ordered descending by usage count
      *
      * @return List of transaction tags
      */
     public List<TransactionTag> getTags() {
         List<TransactionTag> transactionTags = new ArrayList<TransactionTag>();
         try {
             GenericRawResults<String[]> rawResults = transactionDao.queryRaw(
                     "SELECT name, COUNT(*) AS count FROM transactiontags GROUP BY name ORDER BY count DESC");
             for (String[] row : rawResults) {
                 TransactionTag tag = new TransactionTag();
                 tag.setName(row[0]);
                 transactionTags.add(tag);
             }
         } catch (SQLException e) {
             Log.e(TAG, "Failed to retrieve all tags", e);
         }
         return transactionTags;
     }
 
     /**
      * Calculate the average per day using the oldest and newest transaction date
      *
      * @return Average consumption per day
      */
     private double getAvgDay() {
         try {
             GenericRawResults<String[]> rawResults = transactionDao.
                     queryRaw("SELECT accountingDate FROM transactions ORDER BY accountingDate ASC LIMIT 1");
             List<String[]> results = rawResults.getResults();
             if (results.size() > 0) {
                 final Date start = FmtUtil.stringToDate(SQLITE_DATE_FORMAT, results.get(0)[0]);
                 rawResults.close();
                 rawResults = transactionDao.
                         queryRaw("SELECT accountingDate FROM transactions ORDER BY accountingDate DESC LIMIT 1");
                 final Date stop = FmtUtil.stringToDate(SQLITE_DATE_FORMAT, rawResults.getResults().get(0)[0]);
                 rawResults.close();
                 final int days =
                         (int) ((stop.getTime() - start.getTime()) / 1000) / 86400;
                 if (days > 0) {
                     rawResults = transactionDao.
                             queryRaw("SELECT SUM(amountOut) FROM transactions LIMIT 1");
                     final double avg = Double.parseDouble(
                             rawResults.getResults().get(0)[0]) / days;
                     rawResults.close();
                 }
                 return 0;
             }
         } catch (SQLException e) {
             Log.e(TAG, "Failed to retrieve average consumption");
         }
         return 0D;
     }
 
     /**
      * Calculate average consumption per day, week and month
      *
      * @return Average consumption
      */
     public AverageConsumption getAvg() {
         final double avgPerDay = getAvgDay();
         final AverageConsumption avg = new AverageConsumption();
         avg.setDay(avgPerDay);
         avg.setWeek(avgPerDay * 7);
         avg.setMonth(avgPerDay * 30.4368499);
         return avg;
     }
 
     /**
      * Empty all tables
      */
     public void emptyTables() {
         try {
             transactionDao.queryRaw("DELETE FROM transactions");
             transactionDao.queryRaw("DELETE FROM transactiontags");
             transactionDao.queryRaw("DELETE FROM transactiontypes");
         } catch (SQLException e) {
             Log.e(TAG, "Could not empty tables", e);
         }
     }
 
     /**
      * Close open database connections
      */
     public void close() {
         if (helper != null) {
             Log.d(TAG, "Closed database connection");
             helper.close();
         }
     }
 }
