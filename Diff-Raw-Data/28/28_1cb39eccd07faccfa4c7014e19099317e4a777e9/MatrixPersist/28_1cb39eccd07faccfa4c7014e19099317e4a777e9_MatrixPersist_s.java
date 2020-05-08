 /* (c) Copyright by Man YUAN */
 package net.epsilony.mf.util.persistence;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import net.epsilony.mf.util.MFConstants;
 import no.uib.cipr.matrix.MatrixEntry;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author <a href="mailto:epsilonyuan@gmail.com">Man YUAN</a>
  */
 public class MatrixPersist {
 
     public static final Logger logger = LoggerFactory.getLogger(MatrixPersist.class);
     private final static String SQL_CREATE_MATRIES_TABLE =
             "CREATE TABLE "
             + "IF NOT EXISTS "
             + "%s "
             + "(" + MFConstants.SQL_DATABASE_ID_SPC + ", "
             + "num_rows INTEGER NOT NULL CHECK(num_rows>0), "
             + "num_cols INTEGER NOT NULL CHECK(num_cols>0), "
             + "entries_start_id INTEGER NOT NULL CHECK(entries_start_id>0), "
             + "entries_size INTEGER NOT NULL CHECK(entries_size>0))";
     private final static String SQL_CREATE_ENTRIES_TABLE =
             "CREATE TABLE "
             + "IF NOT EXISTS "
             + "%s "
             + "(" + MFConstants.SQL_DATABASE_ID_SPC + ", "
             + "row INTEGER NOT NULL CHECK(row>=0), "
             + "col INTEGER NOT NULL CHECK(col>=0), "
             + "value REAL NOT NULL)";
     Connection connection;
     private final static String SQL_INSERT_A_MATRIX =
             "INSERT INTO %s VALUES(NULL, %d, %d, %d, %d)";
     private final static String SQL_INSERT_MATRIX_ENTRIES =
             "INSERT INTO %s VALUES(NULL, ?, ?, ?)";
     private final static String DEFAULT_MATRIES_TABLE_NAME = "matries";
     private final static String DEFAULT_MATRIES_ENTRIES_NAME = "matries_entries";
     private final static String SQL_FIND_MATRIX_INFO = "SELECT * FROM %s WHERE " + MFConstants.SQL_DATABASE_ID_NAME + " = %d";
     private final static String SQL_SELECT_MATRIX_ENTITIES =
             "SELECT * FROM %s WHERE "
             + MFConstants.SQL_DATABASE_ID_NAME + ">=%d and "
             + MFConstants.SQL_DATABASE_ID_NAME + " < %d";
     //
     protected Statement statement;
     protected int lastMaxId;
     private String matriesTableName = DEFAULT_MATRIES_TABLE_NAME;
     private String entriesTableName = DEFAULT_MATRIES_ENTRIES_NAME;
 
     public void setConnection(Connection connection) throws SQLException {
         this.connection = connection;
         statement = connection.createStatement();
     }
 
     public void createTables() throws SQLException {
 
         statement.executeUpdate(String.format(SQL_CREATE_MATRIES_TABLE, matriesTableName));
         statement.executeUpdate(String.format(SQL_CREATE_ENTRIES_TABLE, entriesTableName));
     }
 
     public int store(MFMatrix mat) throws SQLException {
         logger.debug("start saving matrix: {}x{}", mat.numRows(), mat.numCols());
         int entryStartId = 1 + Persists.getMaxDbId(statement, entriesTableName);
 
         PreparedStatement pst = connection.prepareStatement(String.format(SQL_INSERT_MATRIX_ENTRIES, entriesTableName));
         int batchSize = 0;
         final int batchLim = MFConstants.SQL_BATCH_SIZE_LIMIT;
         boolean oldAutoCommit = connection.getAutoCommit();
         int entriesSize = 0;
        connection.setAutoCommit(false);
         for (MatrixEntry me : mat) {
             double value = me.get();
             if (value == 0) {
                 continue;
             }
 
             pst.setInt(1, me.row());
             pst.setInt(2, me.column());
             pst.setDouble(3, value);
             pst.addBatch();
             if (batchSize >= batchLim) {
                 pst.executeBatch();
                 pst.clearBatch();
                 batchSize = 0;
                 connection.commit();
             }
             batchSize++;
             entriesSize++;
         }
 
         if (batchSize != 0) {
             pst.executeBatch();
         }
         statement.executeUpdate(String.format(
                 SQL_INSERT_A_MATRIX,
                 matriesTableName, mat.numRows(), mat.numCols(), entryStartId, entriesSize));
         connection.commit();
        connection.setAutoCommit(oldAutoCommit);
         lastMaxId = Persists.getMaxDbId(statement, matriesTableName);
         logger.debug("matrix saved as id:{}", lastMaxId);
         return lastMaxId;
     }
 
     MatrixInfo retrieveInfo(int id) throws SQLException {
         if (id <= 0) {
             throw new IllegalArgumentException("id show be positive, not " + id);
         }
 
         ResultSet resultSet = statement.executeQuery(String.format(SQL_FIND_MATRIX_INFO, matriesTableName, id));
         if (!resultSet.isBeforeFirst()) {
             return null;
         }
         MatrixInfo matrixInfo = new MatrixInfo();
         matrixInfo.setNumRows(resultSet.getInt(2));
         matrixInfo.setNumCols(resultSet.getInt(3));
         matrixInfo.setEntitesSize(resultSet.getInt(5));
         return matrixInfo;
     }
 
     MFMatrix retrieve(MFMatrix mat, int id) throws SQLException {
         logger.debug("start retrieving matrix which id = {}", id);
         ResultSet resultSet = statement.executeQuery(String.format(SQL_FIND_MATRIX_INFO, matriesTableName, id));
         if (!resultSet.isBeforeFirst()) {
             throw new IllegalArgumentException("can't find matrix by id " + id);
         }
         int numRows = resultSet.getInt(2);
         int numCols = resultSet.getInt(3);
         int entriesStartId = resultSet.getInt(4);
         int entriesSize = resultSet.getInt(5);
 
         if (mat.numRows() != numRows || mat.numCols() != numCols) {
             throw new IllegalArgumentException(
                     String.format("wrong input mat size, exps: %d*%d not %d*%d",
                     numRows, numCols, mat.numRows(), mat.numCols()));
         }
         resultSet = statement.executeQuery(String.format(SQL_SELECT_MATRIX_ENTITIES,
                 entriesTableName,
                 entriesStartId,
                 entriesStartId + entriesSize));
         while (resultSet.next()) {
             mat.set(resultSet.getInt(2), resultSet.getInt(3), resultSet.getDouble(4));
         }
         logger.debug("matrix retrieved: {}x{}", mat.numRows(), mat.numCols());
         return mat;
     }
 
     public void setMatriesTableName(String matriesTableName) {
         this.matriesTableName = matriesTableName;
     }
 
     public void setEntriesTableName(String entriesTableName) {
         this.entriesTableName = entriesTableName;
     }
 
     public String getMatriesTableName() {
         return matriesTableName;
     }
 
     public String getEntriesTableName() {
         return entriesTableName;
     }
 
     public int getLastMaxId() {
         return lastMaxId;
     }
 }
