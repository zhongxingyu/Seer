 /*
                         QueryJ
 
     Copyright (C) 2002-2005  Jose San Leandro Armendariz
                         chous@acm-sl.org
 
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU General Public
     License as published by the Free Software Foundation; either
     version 2 of the License, or any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     Lesser General Public License for more details.
 
     You should have received a copy of the GNU General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
     Thanks to ACM S.L. for distributing this library under the GPL license.
     Contact info: chous@acm-sl.org
     Postal Address: c/Playa de Lagoa, 1
                     Urb. Valdecabanas
                     Boadilla del monte
                     28660 Madrid
                     Spain
 
  ******************************************************************************
  *
  * Filename: $RCSfile$
  *
  * Author: Jose San Leandro Armendariz
  *
  * Description: Thread-aware DataSource wrapper.
  */
 package org.acmsl.queryj.dao;
 
 /*
  * Importing Spring classes.
  */
 import org.springframework.jdbc.datasource.SmartDataSource;
 import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
 
 /*
  * Importing some JDK1.3 classes.
  */
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.io.PrintWriter;
 
 /*
  * Importing some extension classes.
  */
 import javax.sql.DataSource;
 
 /*
  * Importing Jakarta Commons Logging classes.
  */
 import org.apache.commons.logging.LogFactory;
 
 /**
  * Thread-aware DataSource wrapper.
  * @author <a href="mailto:chous@acm-sl.org"
  *         >Jose San Leandro Armendariz</a>
  */
 public class ThreadAwareDataSourceWrapper
    extends  TransactionAwareDataSourceProxy
 //    implements  SmartDataSource
     implements  DataSource
 {
     /**
      * The connection used in the transaction.
      */
     private Connection m__Connection;
 
     /**
      * The thread-based hash code.
      */
     private int m__iThreadBasedHashCode;
 
     /**
      * Creates a <code>ThreadAwareDataSourceWrapper</code> using
      * given data source.
      * @param dataSource the data source to wrap.
      */
     public ThreadAwareDataSourceWrapper(final DataSource dataSource)
     {
         super(dataSource);
     }
 
     /**
      * Creates a <code>ThreadAwareDataSourceWrapper</code>.
      */
     public ThreadAwareDataSourceWrapper() {};
 
     /**
      * Specifies the JDBC connection.
      * @param connection the connection.
      */
     protected final void immutableSetConnection(final Connection connection)
     {
         m__Connection = connection;
     }
 
     /**
      * Specifies the JDBC connection.
      * @param connection the connection.
      */
     protected void setConnection(final Connection connection)
     {
         immutableSetConnection(connection);
     }
 
     /**
      * Retrieves the JDBC connection.
      * @return such connection.
      */
     protected Connection getInternalConnection()
     {
         return m__Connection;
     }
 
     /**
      * Attempts to establish a database connection.
      * @return the connection.
      */
     public Connection getConnection()
       throws  SQLException
     {
         return
             getConnection(
                 getInternalConnection(),
                 getTargetDataSource());
     }
 
     /**
      * Attempts to establish a database connection.
      * @param internalConnection the internal connection.
      * @param dataSource the data source.
      * @return the connection.
      */
     protected Connection getConnection(
         final Connection internalConnection,
         final DataSource dataSource)
       throws  SQLException
     {
         Connection result = internalConnection;
 
         if  (   (   (result == null)
                  || (isClosed(result)))
              && (dataSource != null))
         {
             result = super.getConnection();
 
             synchronized (this)
             {
                 setConnection(result);
                 setThreadBasedHashCode(
                     buildThreadBasedHashCode(
                         Thread.currentThread()));
             }
         }
 
         return result;
     }
 
     /**
      * Specifies the thread-based hash code.
      * @param hashCode such hash code.
      */
     private void immutableSetThreadBasedHashCode(final int hashCode)
     {
         m__iThreadBasedHashCode = hashCode;
     }
 
     /**
      * Specifies the thread-based hash code.
      * @param hashCode such hash code.
      */
     protected final void setThreadBasedHashCode(final int hashCode)
     {
         immutableSetThreadBasedHashCode(hashCode);
     }
 
     /**
      * Retrieves the thread-based hash code.
      * @return such hash code.
      */
     public int getThreadBasedHashCode()
     {
         return m__iThreadBasedHashCode;
     }
 
     /**
      * Builds a hash code depending on given thread.
      * @param thread the thread.
      * @return the custom hash code.
      * @precondition thread != null
      */
     protected int buildThreadBasedHashCode(final Thread thread)
     {
         int result =
             (  getClass().getName()
              + ".queryj."
              + thread).hashCode();
 
         return result;
     }
 
     /**
      * Checks if the data source is equal logically to given object.
      * @param object the object to compare to.
      * @return true if both data sources represents the same entity.
      */
     public boolean equals(final Object object)
     {
         boolean result =
             equals(
                 object,
                 getThreadBasedHashCode(),
                 buildThreadBasedHashCode(
                     Thread.currentThread()),
                 getTargetDataSource());
 
         return result;
     }
 
     /**
      * Checks if the data source is equal logically to given object.
      * @param object the object to compare to.
      * @param threadBasedHashCode the thread-based hash code.
      * @param currentThreadBasedHashCode the hash code based on current thread.
      * @param dataSource the data source.
      * @return <code>true</code> if both data sources represents the same entity.
      * @precondition dataSource != null
      */
     protected boolean equals(
         final Object object,
         final int threadBasedHashCode,
         final int currentThreadBasedHashCode,
         final DataSource dataSource)
     {
         boolean result = (object == this);
 
         if  (   (threadBasedHashCode == 0) // data source not used yet
                                            // -> let's try to take the thread's
              || (threadBasedHashCode == currentThreadBasedHashCode))
         {
             // same thread -> assuming only one instance per thread.
             result = true;
         }
 
         if  (!result)
         {
             result = equals(object, dataSource);
         }
 
         return result;
     }
 
     /**
      * Checks if the data source is equal logically to given object.
      * @param object the object to compare to.
      * @param dataSource the data source.
      * @return <code>true</code> if both data sources represents the same entity.
      * @precondition dataSource != null
      */
     protected boolean equals(
         final Object object, final DataSource dataSource)
     {
         boolean result = false;
 
         if  (   (object != null)
              && (object instanceof DataSource))
         {
             DataSource t_GivenDataSource = (DataSource) object;
 
             result =
                 (   (t_GivenDataSource == dataSource)
                  || (t_GivenDataSource.equals(dataSource)));
         }
 
         return result;
     }
 
     /**
      * Retrieves the hash code.
      * @return such value.
      */
     public int hashCode()
     {
         int result =
             hashCode(
                 getThreadBasedHashCode(),
                 buildThreadBasedHashCode(
                     Thread.currentThread()),
                 getTargetDataSource());
 
         return result;
     }
 
     /**
      * Retrieves the hash code.
      * @param threadBasedHashCode the cached thread-based hash code.
      * @param currentThreadBasedHashCode the hash code based on current thread.
      * @param dataSource the data source.
      * @return such value.
      * @precondition dataSource != null
      */
     protected int hashCode(
         final int threadBasedHashCode,
         final int currentThreadBasedHashCode,
         final DataSource dataSource)
     {
         int result = currentThreadBasedHashCode;
 
         if  (   (threadBasedHashCode != 0) // data source not used yet
                                            // -> we'll take the thread's
              && (result != currentThreadBasedHashCode))
         {
             // Different threads -> wrapper behaviour
             result = dataSource.hashCode();
         }
 
         return result;
     }
 
     /**
      * Retrieves the textual version of this instance.
      * @return such information.
      */
     public String toString()
     {
         return toString(getTargetDataSource());
     }
 
     /**
      * Retrieves the textual version of this instance.
      * @param dataSource the wrapped data source.
      * @return such information.
      */
     protected String toString(final DataSource dataSource)
     {
         String result = null;
 
         if  (dataSource == null)
         {
             result = super.toString();
         }
         else
         {
             result = dataSource.toString();
         }
 
         return result;
     }
 
     /**
      * Checks whether given connection is already closed.
      * @param connection the connection.
      * @return <code>true</code> in such case.
      * @precondition connection != null
      */
     protected boolean isClosed(final Connection connection)
     {
         boolean result = false;
 
         try
         {
             result = connection.isClosed();
         }
         catch  (final SQLException sqlException)
         {
             try
             {
                 LogFactory.getLog(getClass()).warn(
                     "Error checking if connection is closed.",
                     sqlException);
             }
             catch  (final Throwable throwable)
             {
                 // class-loading problem.
             }
         }
 
         return result;
     }
 
     /** 
      * Should we close this connection, obtained from this DataSource?
      * <p>Code that uses connections from a SmartDataSource should always
      * perform a check via this method before invoking <code>close()</code>.
      * <p>However, the JdbcTemplate class in the core package should take care of
      * closing JDBC connections, freeing application code of this responsibility.
      * @param con connection, which should have been obtained
      * from this data source, to check closure status of.
      * @return whether the given connection should be closed.
      */
     public boolean shouldClose(final Connection connection)
     {
         return false;
     }
 }
