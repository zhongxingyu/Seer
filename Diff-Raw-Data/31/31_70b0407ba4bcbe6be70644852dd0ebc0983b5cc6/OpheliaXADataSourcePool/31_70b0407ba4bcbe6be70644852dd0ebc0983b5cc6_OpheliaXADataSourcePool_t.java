 /*
  * Created On:  16-Mar-07 8:42:10 PM
  */
 package com.thinkparity.ophelia.model.workspace.impl.xapool;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 
import javax.transaction.TransactionManager;

 import org.enhydra.jdbc.pool.StandardXAPoolDataSource;
 
 /**
  * <b>Title:</b>thinkParity OpheliaModel Transaction Data Source Pool<br>
  * <b>Description:</b><br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public class OpheliaXADataSourcePool extends StandardXAPoolDataSource {
 
     /** The underlying <code>OpheliaXADataSource</code>. */
     private final OpheliaXADataSource xaDataSource;
 
     /**
      * Create OpheliaXADataSourcePool.
      * 
      * @param xaDataSource
      *            An <code>OpheliaXADataSource</code>.
      */
     public OpheliaXADataSourcePool(final OpheliaXADataSource xaDataSource) {
         super(xaDataSource, 5);
         this.xaDataSource = xaDataSource;
         setUser(xaDataSource.getUser());
         setPassword(xaDataSource.getPassword());
     }
 
     /**
      * Obtain a connection used to shutdown the database.
      * 
      * @return A <code>Connection</code>.
      * @throws SQLException
      */
     public synchronized Connection getShutdownConnection() throws SQLException {
         return xaDataSource.getShutdownConnection();
     }

    /**
     * @see org.enhydra.jdbc.pool.StandardXAPoolDataSource#setTransactionManager(javax.transaction.TransactionManager)
     *
     */
    @Override
    public void setTransactionManager(final TransactionManager tm) {
        xaDataSource.setTransactionManager(tm);
        super.setTransactionManager(tm);
    }
 }
