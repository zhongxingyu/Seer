 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jboss.messaging.core.plugin;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.sql.DataSource;
 import javax.transaction.TransactionManager;
 import javax.transaction.xa.Xid;
 
 import org.jboss.jms.destination.JBossDestination;
 import org.jboss.jms.destination.JBossQueue;
 import org.jboss.jms.destination.JBossTopic;
 import org.jboss.jms.message.JBossMessage;
 import org.jboss.logging.Logger;
 import org.jboss.messaging.core.Message;
 import org.jboss.messaging.core.MessageReference;
 import org.jboss.messaging.core.message.CoreMessage;
 import org.jboss.messaging.core.message.MessageFactory;
 import org.jboss.messaging.core.message.MessageSupport;
 import org.jboss.messaging.core.plugin.contract.PersistenceManager;
 import org.jboss.messaging.core.tx.PreparedTxInfo;
 import org.jboss.messaging.core.tx.Transaction;
 import org.jboss.messaging.core.tx.TxCallback;
 import org.jboss.messaging.core.tx.MessagingXid;
 import org.jboss.messaging.util.JDBCUtil;
 import org.jboss.messaging.util.StreamUtils;
 import org.jboss.messaging.util.Util;
 
 /**
  *  
  * JDBC implementation of PersistenceManager 
  *  
  * @author <a href="mailto:ovidiu@jboss.org">Ovidiu Feodorov</a>
  * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
  * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
  * @author <a href="mailto:juha@jboss.org">Juha Lindfors</a>
  *
  * @version <tt>1.1</tt>
  *
  * JDBCPersistenceManager.java,v 1.1 2006/02/22 17:33:41 timfox Exp
  */
 public class JDBCPersistenceManager extends JDBCSupport implements PersistenceManager
 {
    // Constants -----------------------------------------------------
    
    private static final Logger log = Logger.getLogger(JDBCPersistenceManager.class); 
 
    // Static --------------------------------------------------------
    
    // Attributes ----------------------------------------------------
    
    private boolean trace = log.isTraceEnabled();
       
    private boolean usingBatchUpdates = false;
    
    private boolean usingBinaryStream = true;
    
    private int maxParams;
    
    private short orderCount;
    
    
    // Constructors --------------------------------------------------
     
    public JDBCPersistenceManager(DataSource ds, TransactionManager tm, Properties sqlProperties,
                                  boolean createTablesOnStartup, boolean usingBatchUpdates,
                                  boolean usingBinaryStream, int maxParams)
    {
       super(ds, tm, sqlProperties, createTablesOnStartup);
       
       this.usingBatchUpdates = usingBatchUpdates;
       
       this.usingBinaryStream = usingBinaryStream;
       
       this.maxParams = maxParams;      
    }
    
    
    // MessagingComponent overrides ---------------------------------
    
    public void start() throws Exception
    {
       super.start();
 
       Connection conn = null;
 
       TransactionWrapper wrap = new TransactionWrapper();
 
       try
       {
          conn = ds.getConnection();      
          //JBossMessaging requires transaction isolation of READ_COMMITTED
          //Any looser isolation level and we cannot maintain consistency for paging (HSQL)
          if (conn.getTransactionIsolation() != Connection.TRANSACTION_READ_COMMITTED)
          {
             int level = conn.getTransactionIsolation();
 
             String warn =
                "\n\n" +
                "JBoss Messaging Warning: DataSource connection transaction isolation should be READ_COMMITTED, but it is currently " + Util.transactionIsolationToString(level) + ".\n" +
                "                         Using an isolation level less strict than READ_COMMITTED may lead to data consistency problems.\n" +
                "                         Using an isolation level more strict than READ_COMMITTED may lead to deadlock.\n";
             log.warn(warn);
          }
       }
       catch (Exception e)
       {
          wrap.exceptionOccurred();
          throw e;
       }
       finally
       {
          if (conn != null)
          {
             conn.close();
          }
          wrap.end();
       }
         
       //We can't remnove unreliable data since it might introduce holes into the paging order
       //removeUnreliableMessageData();
          
       log.debug(this + " started");
    }
    
    public void stop() throws Exception
    {
       super.stop();
    }
    
    // PersistenceManager implementation -------------------------
    
    // Related to XA Recovery
    // ======================
    
    public List getMessageChannelPairRefsForTx(long transactionId) throws Exception
    {
       String sql = this.getSQLStatement("SELECT_MESSAGEID_FOR_REF");
       return getMessageChannelPair(sql, transactionId);
    }
    
    public List getMessageChannelPairAcksForTx(long transactionId) throws Exception
    {
       String sql = this.getSQLStatement("SELECT_MESSAGEID_FOR_ACK");
       return getMessageChannelPair(sql, transactionId);
    }
    
    public List retrievePreparedTransactions() throws Exception
    {
       /* Note the API change for 1.0.2 XA Recovery -- List now contains instances of PreparedTxInfo<TxId, Xid>
        * instead of direct Xids [JPL] */
       
       Connection conn = null;
       Statement st = null;
       ResultSet rs = null;
       PreparedTxInfo txInfo = null;
       TransactionWrapper wrap = new TransactionWrapper();
       
       try
       {
          List transactions = new ArrayList();
          
          conn = ds.getConnection();
          
          st = conn.createStatement();
          
          String sql = this.getSQLStatement("SELECT_PREPARED_TRANSACTIONS");
          
          rs = st.executeQuery(sql);
          
          while (rs.next())
          {
             //get the existing tx id --MK START
             long txId = rs.getLong(1);
             
             byte[] branchQual = rs.getBytes(2);
             int formatId = rs.getInt(3);
             byte[] globalTxId = rs.getBytes(4);
             Xid xid = new MessagingXid(branchQual, formatId, globalTxId);
             
             // create a tx info object with the result set detailsdetails
             txInfo = new PreparedTxInfo(txId, xid);
             transactions.add(txInfo);
          }
          
          return transactions;
          
       }
       catch (Exception e)
       {
          wrap.exceptionOccurred();
          throw e;
       }
       finally
       {
          if (rs != null)
          {
             try
             {
                rs.close();
             }
             catch (Throwable e)
             {
             }
          }
          if (st != null)
          {
             try
             {
                st.close();
             }
             catch (Throwable e)
             {
             }
          }
          if (conn != null)
          {
             try
             {
                conn.close();
             }
             catch (Throwable e)
             {
             }
          }
          wrap.end();
       }
    }
    
                
    // Related to counters
    // ===================
    
    public long reserveIDBlock(String counterName, int size) throws Exception
    {
       if (trace) { log.trace("Getting ID block for counter " + counterName + ", size " + size); }
       
       if (size <= 0)
       {
          throw new IllegalArgumentException("block size must be > 0");
       }
       
       Connection conn = null;
       PreparedStatement ps = null;
       ResultSet rs = null;
       TransactionWrapper wrap = new TransactionWrapper();
       
       try
       {
          conn = ds.getConnection();
 
          //For the clustered case - this MUST use SELECT .. FOR UPDATE or a similar
          //construct the locks the row
          String selectCounterSQL = getSQLStatement("SELECT_COUNTER");
          
          ps = conn.prepareStatement(selectCounterSQL);
          
          ps.setString(1, counterName);
          
          rs = ps.executeQuery();
          
          if (trace) { log.trace(JDBCUtil.statementToString(selectCounterSQL, counterName)); }         
          
          if (!rs.next())
          {
             rs.close();
             rs = null;
             
             ps.close();
             
             //There is a very small possibility that two threads will attempt to insert the same counter
             //at the same time, if so, then the second one will fail eventually after a few retries by throwing
             //a primary key violation.
             
             String insertCounterSQL = getSQLStatement("INSERT_COUNTER");
             
             ps = conn.prepareStatement(insertCounterSQL);
             
             ps.setString(1, counterName);
             ps.setLong(2, size);
             
             int rows = updateWithRetry(ps);
             if (trace) { log.trace(JDBCUtil.statementToString(insertCounterSQL, counterName, new Integer(size)) + " inserted " + rows + " rows"); }
             
             ps.close();            
             ps = null;
             return 0;
          }
          
          long nextId = rs.getLong(1);
          
          rs.close();
          rs = null;
          
          ps.close();
 
          String updateCounterSQL = getSQLStatement("UPDATE_COUNTER");
 
          ps = conn.prepareStatement(updateCounterSQL);
          
          ps.setLong(1, nextId + size);
          ps.setString(2, counterName);
          
          int rows = updateWithRetry(ps);
          if (trace) { log.trace(JDBCUtil.statementToString(updateCounterSQL, new Long(nextId + size), counterName) + " updated " + rows + " rows"); }
          
          return nextId;
       }
       catch (Exception e)
       {
          wrap.exceptionOccurred();
          throw e;
       }
       finally
       {
          if (ps != null)
          {
             try
             {
                ps.close();
             }
             catch (Throwable e)
             {
             }
          }
          if (conn != null)
          {
             try
             {
                conn.close();
             }
             catch (Throwable e)
             {
             }
          }
          wrap.end();
       }     
    }
          
    /*
     * Retrieve a List of messages corresponding to the specified List of message ids.
     * The implementation here for HSQLDB does this by using a PreparedStatment with an IN clause
     * with a maximum of 100 elements.
     * If there are more than maxParams message to retrieve this is repeated a number of times.
     * For "Enterprise" databases (Oracle, DB2, Sybase etc) a more sophisticated technique should be used
     * e.g. Oracle ARRAY types in Oracle which can be submitted as a param to an Oracle prepared statement
     * Although this would all be DB specific.
     */
    public List getMessages(List messageIds) throws Exception
    {
       if (trace) { log.trace("Getting batch of messages for " + messageIds); }
       
       Connection conn = null;
       PreparedStatement ps = null;
       ResultSet rs = null;
       TransactionWrapper wrap = new TransactionWrapper();
       
       try
       {
          conn = ds.getConnection();
          
          Iterator iter = messageIds.iterator();
          
          int size = messageIds.size();
          
          int count = 0;
          
          List msgs = new ArrayList();
          
          while (iter.hasNext())
          {
             if (ps == null)
             {
                //PreparedStatements are cached in the JCA layer so we will never actually have more than
                //100 distinct ones            
                int numParams;
                if (count < (size / maxParams) * maxParams)
                {
                   numParams = maxParams;
                }
                else
                {
                   numParams = size % maxParams;
                }
                StringBuffer buff = new StringBuffer(getSQLStatement("LOAD_MESSAGES"));
                buff.append(" WHERE ").append(getSQLStatement("MESSAGEID_COLUMN")).append(" IN (");
                for (int i = 0; i < numParams; i++)
                {
                   buff.append("?");
                   if (i < numParams - 1)
                   {
                      buff.append(",");
                   }
                }
                buff.append(")");
                ps = conn.prepareStatement(buff.toString());
                
                if (trace)
                {
                   log.trace(buff.toString());
                }
             }
             
             long msgId = ((Long)iter.next()).longValue();
             
             ps.setLong((count % maxParams) + 1, msgId);
             
             count++;
             
             if (!iter.hasNext() || count % maxParams == 0)
             {
                rs = ps.executeQuery();
                
                while (rs.next())
                {
                   
 //                  "SELECT MESSAGEID, RELIABLE, EXPIRATION, TIMESTAMP, " +
 //                  "PRIORITY, COREHEADERS, PAYLOAD, CHANNELCOUNT, TYPE, JMSTYPE, CORRELATIONID, " +
 //                  "CORRELATIONID_BYTES, DESTINATION, REPLYTO, JMSPROPERTIES " +
 //                  "FROM JMS_MESSAGE"
                   
                   
                   long messageId = rs.getLong(1);
                   boolean reliable = rs.getString(2).equals("Y");
                   long expiration = rs.getLong(3);
                   long timestamp = rs.getLong(4);
                   byte priority = rs.getByte(5);                  
                   byte[] bytes = getBytes(rs, 6);
                   HashMap coreHeaders = bytesToMap(bytes);
                   byte[] payload = getBytes(rs, 7);
                   
 
                   //TODO - We are mixing concerns here
                   //The basic JDBCPersistencManager should *only* know about core messages - not 
                   //JBossMessages - we should subclass JBDCPersistenceManager and the JBossMessage
                   //specific code in a subclass
                   
                   byte type = rs.getByte(8);
                   
                   Message m;
                   
                   if (type != CoreMessage.TYPE)
                   {
                      //JBossMessage
                      String jmsType = rs.getString(9);
                      String correlationID = rs.getString(10);
                      byte[] correlationIDBytes = rs.getBytes(11);
                      String destination = rs.getString(12);
                      String replyTo = rs.getString(13);
                      boolean replyToExists = !rs.wasNull();
                      bytes = getBytes(rs, 14);
                      HashMap jmsProperties = bytesToMap(bytes);
                      
                      JBossDestination dest;
                      if (destination.charAt(0) == 'Q')
                      {
                         dest = new JBossQueue(destination.substring(1, destination.length()));
                      }
                      else
                      {
                         dest = new JBossTopic(destination.substring(1, destination.length()));
                      }
                      
                      JBossDestination replyToDest = null;
                      
                      if (replyToExists)
                      {
                         if (replyTo.charAt(0) == 'Q')
                         {
                            replyToDest = new JBossQueue(replyTo.substring(1, replyTo.length()));
                         }
                         else
                         {
                            replyToDest = new JBossTopic(replyTo.substring(1, replyTo.length()));
                         }
                      }
                          
                      m = MessageFactory.createMessage(messageId, reliable, expiration, timestamp, priority,
                                                       coreHeaders, payload,
                                                       type, jmsType, correlationID, correlationIDBytes,
                                                       dest, replyToDest, 0,
                                                       jmsProperties);
                   }
                   else
                   {
                      m = MessageFactory.createMessage(messageId, reliable, expiration, timestamp, priority,
                                                           coreHeaders, payload, type,
                                                           null, null, null, null, null, 0, null);
                   }
                   
                   
                   msgs.add(m);
                }
                
                rs.close();
                rs = null;
                
                ps.close();
                ps = null;
             }
          }
          
          if (trace) { log.trace("Loaded " + msgs.size() + " messages in total"); }
 
          return msgs;
       }
       catch (Exception e)
       {
          wrap.exceptionOccurred();
          throw e;
       }
       finally
       {
          if (rs != null)
          {
             try
             {
                rs.close();
             }
             catch (Throwable e)
             {
             }
          }
          if (ps != null)
          {
             try
             {
                ps.close();
             }
             catch (Throwable e)
             {
             }
          }
          if (conn != null)
          {
             try
             {
                conn.close();
             }
             catch (Throwable e)
             {
             }
          }
          wrap.end();
       }
    }  
    
        
    // Related to paging functionality
    // ===============================                 
    
    public void pageReferences(long channelID, List references, boolean paged) throws Exception
    {  
       Connection conn = null;
       PreparedStatement psInsertReference = null;  
       PreparedStatement psInsertMessage = null;
       PreparedStatement psUpdateMessage = null;
       PreparedStatement psMessageExists = null;
       ResultSet rsMessageExists = null;
       TransactionWrapper wrap = new TransactionWrapper();
             
       //First we order the references in message order
       orderReferences(references);
                          
       try
       {
          //Now we get a lock on all the messages. Since we have ordered the refs we should avoid deadlock
          getLocks(references);
          
          conn = ds.getConnection();
          
          Iterator iter = references.iterator();
          
          boolean messageInsertsInBatch = false;
          boolean messageUpdatesInBatch = false;
          
          if (usingBatchUpdates)
          {
             psInsertReference = conn.prepareStatement(getSQLStatement("INSERT_MESSAGE_REF"));
             psInsertMessage = conn.prepareStatement(getSQLStatement("INSERT_MESSAGE"));
             psUpdateMessage = conn.prepareStatement(getSQLStatement("INC_CHANNELCOUNT"));
          }
          
          while (iter.hasNext())
          {
             //We may need to persist the message itself 
             MessageReference ref = (MessageReference) iter.next();
                                             
             //For non reliable refs we insert the ref (and maybe the message) itself
                            
             if (!usingBatchUpdates)
             {
                psInsertReference = conn.prepareStatement(getSQLStatement("INSERT_MESSAGE_REF"));
             }
             
             //Now store the reference
             addReference(channelID, ref, psInsertReference, paged);
                         
             if (usingBatchUpdates)
             {
                psInsertReference.addBatch();
             }
             else
             {
                int rows = updateWithRetry(psInsertReference);
                
                if (trace)
                {
                   log.trace("Inserted " + rows + " rows");
                }
                
                psInsertReference.close();
                psInsertReference = null;
             }
             
             if (!usingBatchUpdates)
             {
                psInsertMessage = conn.prepareStatement(getSQLStatement("INSERT_MESSAGE"));
                psUpdateMessage = conn.prepareStatement(getSQLStatement("INC_CHANNELCOUNT"));
             }
                                                                                      
             //Maybe we need to persist the message itself
             Message m = ref.getMessage();
             
             //In a paging situation, we cannot use the persisted flag on the message to determine whether
             //to insert the message or not.
             //This is because a channel (possibly on another node) may be paging too and referencing
             //the same message, and might have removed the message independently, the other
             //channel will not know about this.
             //Therefore we have to check if the message is already in the database and insert it if it isn't
             
             //TODO This is a bit of a hassle -
             //A cleaner and better solution here is to completely separate out the paging functionality from the
             //standard persistence functionality since it complicates things considerably.
             //We should define a paging store which is separate from the persistence store, and
             //typically not using the database for the paging store - probably use a file based store
             //e.g HOWL or some other logger
             
             //Note when running this with two or more competing channels in the same process, then
             //we do not need a FOR UPDATE on the select since we lock the messages in memory
             //However for competing nodes, we do, therefore we require a database that supports
             //this, this is another reason why we cannot use HSQL in a clustered environment
             //since it does not have a for update equivalent
             
             boolean added;
             
             psMessageExists = conn.prepareStatement(getSQLStatement("MESSAGE_EXISTS"));
             
             psMessageExists.setLong(1, m.getMessageID());
             
             rsMessageExists = psMessageExists.executeQuery();
              
             if (rsMessageExists.next())
             {
                //Message exists
                
                // Update the message with the new channel count
                incrementChannelCount(m, psUpdateMessage);
                   
                added = false;              
             }
             else
             {
                //Hasn't been persisted before so need to persist the message
                storeMessage(m, psInsertMessage);
                
                added = true;
             }    
             
             if (usingBatchUpdates)
             {
                if (added)
                {
                   psInsertMessage.addBatch();
                   messageInsertsInBatch = true;
                }
                else
                {
                   psUpdateMessage.addBatch();
                   messageUpdatesInBatch = true;
                }
             }
             else
             {
                if (added)
                {
                   int rows = updateWithRetry(psInsertMessage);
                                       
                   if (trace)
                   {
                      log.trace("Inserted " + rows + " rows");
                   }
                }
                else
                {               
                   int rows = updateWithRetry(psUpdateMessage);
                   
                   if (trace)
                   {
                      log.trace("Updated " + rows + " rows");
                   }
                }
                psInsertMessage.close();
                psInsertMessage = null;
                psUpdateMessage.close();
                psUpdateMessage = null;
             }      
          }         
          
          if (usingBatchUpdates)
          {
             int[] rowsReference = updateWithRetryBatch(psInsertReference);
             
             if (trace) { logBatchUpdate(getSQLStatement("INSERT_MESSAGE_REF"), rowsReference, "inserted"); }
             
             if (messageInsertsInBatch)
             {
                int[] rowsMessage = updateWithRetryBatch(psInsertMessage);
                
                if (trace) { logBatchUpdate(getSQLStatement("INSERT_MESSAGE"), rowsMessage, "inserted"); }
             }
             if (messageUpdatesInBatch)
             {
                int[] rowsMessage = updateWithRetryBatch(psUpdateMessage);
                
                if (trace) { logBatchUpdate(getSQLStatement("INC_CHANNELCOUNT"), rowsMessage, "updated"); }
             }
             
             psInsertReference.close();
             psInsertReference = null;
             psInsertMessage.close();
             psInsertMessage = null;
             psUpdateMessage.close();
             psUpdateMessage = null;
          }        
       }
       catch (Exception e)
       {
          wrap.exceptionOccurred();
          throw e;
       }
       finally
       {
          if (psInsertReference != null)
          {
             try
             {
                psInsertReference.close();
             }
             catch (Throwable t)
             {
             }
          }
          if (psInsertMessage != null)
          {
             try
             {
                psInsertMessage.close();
             }
             catch (Throwable t)
             {
             }
          }
          if (psUpdateMessage != null)
          {
             try
             {
                psUpdateMessage.close();
             }
             catch (Throwable t)
             {
             }
          }
          if (conn != null)
          {
             try
             {
                conn.close();
             }
             catch (Throwable t)
             {
             }
          }
          try
          {
             wrap.end();                       
          }
          finally
          {            
             //And then release locks
             this.releaseLocks(references);
          }         
       }      
    }
          
    public void removeDepagedReferences(long channelID, List references) throws Exception
    {
       if (trace) { log.trace(this + " Removing " + references.size() + " refs from channel " + channelID); }
           
       Connection conn = null;
       PreparedStatement psDeleteReference = null;  
       PreparedStatement psDeleteMessage = null;
       PreparedStatement psUpdateMessage = null;
       TransactionWrapper wrap = new TransactionWrapper();
         
       //We order the references
       orderReferences(references);
              
       try
       {
          //We get locks on all the messages - since they are ordered we avoid deadlock
          getLocks(references);
          
          conn = ds.getConnection();
          
          Iterator iter = references.iterator();
          
          if (usingBatchUpdates)
          {
             psDeleteReference = conn.prepareStatement(getSQLStatement("DELETE_MESSAGE_REF"));
             psDeleteMessage = conn.prepareStatement(getSQLStatement("DELETE_MESSAGE"));
             psUpdateMessage = conn.prepareStatement(getSQLStatement("DEC_CHANNELCOUNT"));
          }
          
          while (iter.hasNext())
          {
             MessageReference ref = (MessageReference) iter.next();
                                                              
             if (!usingBatchUpdates)
             {
                psDeleteReference = conn.prepareStatement(getSQLStatement("DELETE_MESSAGE_REF"));
             }
             
             removeReference(channelID, ref, psDeleteReference);
             
             if (usingBatchUpdates)
             {
                psDeleteReference.addBatch();
             }
             else
             {
                int rows = updateWithRetry(psDeleteReference);
                
                if (trace) { log.trace("Deleted " + rows + " rows"); }
                
                psDeleteReference.close();
                psDeleteReference = null;
             }
             
             if (!usingBatchUpdates)
             {
                psDeleteMessage = conn.prepareStatement(getSQLStatement("DELETE_MESSAGE"));
                psUpdateMessage = conn.prepareStatement(getSQLStatement("DEC_CHANNELCOUNT"));
             }
                
             Message m = ref.getMessage();
                                     
             //Maybe we need to delete the message itself
               
             //Update the message with the new channel count
             decrementChannelCount(m, psUpdateMessage);
             
 
             //Run the remove message update
             removeMessage(m, psDeleteMessage);
                         
             if (usingBatchUpdates)
             {
                psUpdateMessage.addBatch();
                
                psDeleteMessage.addBatch();
             }
             else
             {  
                int rows = updateWithRetry(psUpdateMessage);
                                                  
                if (trace) { log.trace("Updated " + rows + " rows"); }
                
                rows = updateWithRetry(psDeleteMessage);
         
                if (trace) { log.trace("Deleted " + rows + " rows"); }
             
                psDeleteMessage.close();
                psDeleteMessage = null;
                psUpdateMessage.close();
                psUpdateMessage = null;
             }  
             
          }         
          
          if (usingBatchUpdates)
          {
             int[] rowsReference = updateWithRetryBatch(psDeleteReference);
             
             if (trace) { logBatchUpdate(getSQLStatement("DELETE_MESSAGE_REF"), rowsReference, "deleted"); }
             
             rowsReference = updateWithRetryBatch(psUpdateMessage);
             
             if (trace) { logBatchUpdate(getSQLStatement("DEC_CHANNELCOUNT"), rowsReference, "updated"); }
             
             rowsReference = updateWithRetryBatch(psDeleteMessage);
             
             if (trace) { logBatchUpdate(getSQLStatement("DELETE_MESSAGE"), rowsReference, "deleted"); }
                                     
             psDeleteReference.close();
             psDeleteReference = null;
             psDeleteMessage.close();
             psDeleteMessage = null;
             psUpdateMessage.close();
             psUpdateMessage = null;
          }              
       }
       catch (Exception e)
       {
          wrap.exceptionOccurred();
          throw e;
       }
       finally
       {
          if (psDeleteReference != null)
          {
             try
             {
                psDeleteReference.close();
             }
             catch (Throwable t)
             {
             }
          }
          if (psDeleteMessage != null)
          {
             try
             {
                psDeleteMessage.close();
             }
             catch (Throwable t)
             {
             }
          }
          if (psUpdateMessage != null)
          {
             try
             {
                psUpdateMessage.close();
             }
             catch (Throwable t)
             {
             }
          }
          if (conn != null)
          {
             try
             {
                conn.close();
             }
             catch (Throwable t)
             {
             }
          }
          try
          {
             wrap.end();
          }
          finally
          {     
             //And then release locks
             this.releaseLocks(references);
          }         
       }      
    }
    
    public void updateReliableReferencesNotPagedInRange(long channelID, long orderStart, long orderEnd, long num) throws Exception
    {
       if (trace) { log.trace("Updating reliable references for channel " + channelID + " between " + orderStart + " and " + orderEnd); }
       
       Connection conn = null;
       PreparedStatement ps = null;
       TransactionWrapper wrap = new TransactionWrapper();
 
       final int MAX_TRIES = 25;      
       
       try
       {
          conn = ds.getConnection();
          
          ps = conn.prepareStatement(getSQLStatement("UPDATE_RELIABLE_REFS_NOT_PAGED"));
                  
          ps.setLong(1, orderStart);
          
          ps.setLong(2, orderEnd);
          
          ps.setLong(3, channelID);
          
          int tries = 0;
          
          while (true)
          {
             try
             {
                int rows = updateWithRetry(ps);
                  
                if (trace) { log.trace(JDBCUtil.statementToString(getSQLStatement("UPDATE_RELIABLE_REFS_NOT_PAGED"), new Long(channelID),
                                       new Long(orderStart), new Long(orderEnd)) + " updated " + rows + " rows"); }
                if (tries > 0)
                {
                   log.warn("Update worked after retry");
                }
                
                //Sanity check
                if (rows != num)
                {
                   throw new IllegalStateException("Did not update correct number of rows");
                }
                
                break;
             }
             catch (SQLException e)
             {
                log.warn("SQLException caught - assuming deadlock detected, try:" + (tries + 1), e);
                
                tries++;
                
                if (tries == MAX_TRIES)
                {
                   log.error("Retried " + tries + " times, now giving up");
                   
                   throw new IllegalStateException("Failed to update references");
                }
                
                log.warn("Trying again after a pause");
                
                //Now we wait for a random amount of time to minimise risk of deadlock occurring again
                Thread.sleep((long)(Math.random() * 500));
             }  
          }
       }
       catch (Exception e)
       {
          wrap.exceptionOccurred();
          throw e;
       }
       finally
       {
          if (ps != null)
          {
             try
             {
                ps.close();
             }
             catch (Throwable e)
             {
             }
          }
          if (conn != null)
          {
             try
             {
                conn.close();
             }
             catch (Throwable e)
             {
             }
          }
          wrap.end();
       }
    }
    
    public void updatePageOrder(long channelID, List references) throws Exception
    {
       Connection conn = null;
       PreparedStatement psUpdateReference = null;  
       TransactionWrapper wrap = new TransactionWrapper();
       
       if (trace) { log.trace("Updating page order for channel:" + channelID); }
         
       try
       {
          conn = ds.getConnection();
          
          Iterator iter = references.iterator();
          
          if (usingBatchUpdates)
          {
             psUpdateReference = conn.prepareStatement(getSQLStatement("UPDATE_PAGE_ORDER"));
          }
          
          while (iter.hasNext())
          {
             MessageReference ref = (MessageReference) iter.next();
                  
             if (!usingBatchUpdates)
             {
                psUpdateReference = conn.prepareStatement(getSQLStatement("UPDATE_PAGE_ORDER"));
             }
             
             psUpdateReference.setLong(1, ref.getPagingOrder());
 
             psUpdateReference.setLong(2, ref.getMessageID());
             
             psUpdateReference.setLong(3, channelID);
             
             if (usingBatchUpdates)
             {
                psUpdateReference.addBatch();
             }
             else
             {
                int rows = updateWithRetry(psUpdateReference);
                
                if (trace) { log.trace("Updated " + rows + " rows"); }
                
                psUpdateReference.close();
                psUpdateReference = null;
             }
          }
                      
          if (usingBatchUpdates)
          {
             int[] rowsReference = updateWithRetryBatch(psUpdateReference);
             
             if (trace) { logBatchUpdate(getSQLStatement("UPDATE_PAGE_ORDER"), rowsReference, "updated"); }
                         
             psUpdateReference.close();
             psUpdateReference = null;
          }
       }
       catch (Exception e)
       {
          wrap.exceptionOccurred();
          throw e;
       }
       finally
       {
          if (psUpdateReference != null)
          {
             try
             {
                psUpdateReference.close();
             }
             catch (Throwable t)
             {
             }
          }
          if (conn != null)
          {
             try
             {
                conn.close();
             }
             catch (Throwable t)
             {
             }
          }
          wrap.end();
       }    
    }
       
    public List getPagedReferenceInfos(long channelID, long orderStart, long number) throws Exception
    {
       if (trace) { log.trace("loading message reference info for channel " + channelID + " from " + orderStart + " number " + number);      }
                  
       List refs = new ArrayList();
       
       Connection conn = null;
       PreparedStatement ps = null;
       ResultSet rs = null;
       TransactionWrapper wrap = new TransactionWrapper();
       
       try
       {
          conn = ds.getConnection();
          
          ps = conn.prepareStatement(getSQLStatement("LOAD_PAGED_REFS"));
          
          ps.setLong(1, channelID);
          
          ps.setLong(2, orderStart);
          
          ps.setLong(3, orderStart + number - 1);
          
          rs = ps.executeQuery();
          
          long ord = orderStart;
          
          while (rs.next())
          {
             long msgId = rs.getLong(1);     
             int deliveryCount = rs.getInt(2);
             int pageOrd = rs.getInt(3);
             boolean reliable = rs.getString(4).equals("Y");
             long sched = rs.getLong(5);
             
             //Sanity check
             if (pageOrd != ord)
             {
                throw new IllegalStateException("Unexpected pageOrd: " + pageOrd + " expected: " + ord);
             }
             
             ReferenceInfo ri = new ReferenceInfo(msgId, deliveryCount, reliable, sched);
             
             refs.add(ri);
             ord++;
          }
          
          //Sanity check
          if (ord != orderStart + number)
          {
             throw new IllegalStateException("Didn't load expected number of references, loaded: " + (ord - orderStart) +
                                             " expected: " + number);
          }
          
          return refs;
       }
       catch (Exception e)
       {
          wrap.exceptionOccurred();
          throw e;
       }
       finally
       {
          if (rs != null)
          {
             try
             {
                rs.close();
             }
             catch (Throwable e)
             {
             }
          }
          if (ps != null)
          {
             try
             {
                ps.close();
             }
             catch (Throwable e)
             {
             }
          }
          if (conn != null)
          {
             try
             {
                conn.close();
             }
             catch (Throwable e)
             {
             }
          }
          wrap.end();
       }      
    }   
    
    /*
     * Load the initial, non paged refs
     */
    public InitialLoadInfo getInitialReferenceInfos(long channelID, int fullSize) throws Exception
    {
       if (trace) { log.trace("loading initial reference infos for channel " + channelID);  }
                     
       List refs = new ArrayList();
  
       Connection conn = null;
       PreparedStatement ps = null;
       ResultSet rs = null;
       TransactionWrapper wrap = new TransactionWrapper();
                                 
       try
       {
          conn = ds.getConnection();         
          
          //First we get the values for min() and max() page order
          ps = conn.prepareStatement(getSQLStatement("SELECT_MIN_MAX_PAGE_ORD"));
          
          ps.setLong(1, channelID);
          
          rs = ps.executeQuery();
                   
          rs.next();
          
          Long minOrdering = new Long(rs.getLong(1));
          
          if (rs.wasNull())
          {
             minOrdering = null;
          }
          
          Long maxOrdering = new Long(rs.getLong(2));
          
          if (rs.wasNull())
          {
             maxOrdering = null;
          }
          
          //For unpaged refs we must make sure we only load refs with state='C' - i.e.
          //they're not part of an XA transactions.
          //Otherwise we could end up loading message that hadn't be committed
          //or end up loading refs which are due to be acked by a transaction that's yet
          //to be recovered.
          
          ps = conn.prepareStatement(getSQLStatement("LOAD_UNPAGED_REFS"));
          
          ps.setLong(1, channelID);
                  
          rs = ps.executeQuery();
          
          int count = 0;
          while (rs.next())
          {
             long msgId = rs.getLong(1);            
             int deliveryCount = rs.getInt(2);
             boolean reliable = rs.getString(3).equals("Y");
             long sched = rs.getLong(4);
             
             ReferenceInfo ri = new ReferenceInfo(msgId, deliveryCount, reliable, sched);
             
             if (count < fullSize)
             {
                refs.add(ri);
             }            
             
             count++;
          }
                   
          //No refs paged
             
          if (count > fullSize)
          {
             throw new IllegalStateException("Cannot load channel " + channelID + " since the fullSize parameter is too small to load " +
                      " all the required references, fullSize needs to be at least " + count + " it is currently " + fullSize);
          }
                          
          return new InitialLoadInfo(minOrdering, maxOrdering, refs);
       }
       catch (Exception e)
       {
          wrap.exceptionOccurred();
          throw e;
       }
       finally
       {
          if (rs != null)
          {
             try
             {
                rs.close();
             }
             catch (Throwable e)
             {
             }
          }
          if (ps != null)
          {
             try
             {
                ps.close();
             }
             catch (Throwable e)
             {
             }
          }
          if (conn != null)
          {
             try
             {
                conn.close();
             }
             catch (Throwable e)
             {
             }
          }
          wrap.end();
       }      
    }   
    
    
    
    // End of paging functionality
    // ===========================
    
    public void addReference(long channelID, MessageReference ref, Transaction tx) throws Exception
    {      
       if (tx != null)
       {
          //In a tx so we just add the ref in the tx in memory for now
 
          TransactionCallback callback = getCallback(tx);
 
          callback.addReferenceToAdd(channelID, ref);
       }
       else
       {         
          //No tx so add the ref directly in the db
          
          TransactionWrapper wrap = new TransactionWrapper();
          
          PreparedStatement psReference = null;
          PreparedStatement psMessage = null;
          
          Connection conn = ds.getConnection();
          
          Message m = ref.getMessage();     
            
          try
          {            
             // Get lock on message
             LockMap.instance.obtainLock(m);
                                     
             psReference = conn.prepareStatement(getSQLStatement("INSERT_MESSAGE_REF"));
             
             // Add the reference
             addReference(channelID, ref, psReference, false);
             
             int rows = updateWithRetry(psReference);      
             
             if (trace) { log.trace("Inserted " + rows + " rows"); }
               
             if (!m.isPersisted())
             {
                // First time so persist the message
                psMessage = conn.prepareStatement(getSQLStatement("INSERT_MESSAGE"));
                
                storeMessage(m, psMessage);
                
                m.setPersisted(true);
             }
             else
             {
                //Update the message's channel count
                psMessage = conn.prepareStatement(getSQLStatement("INC_CHANNELCOUNT"));
                
                incrementChannelCount(m, psMessage);
             }
                            
             rows = updateWithRetry(psMessage);
             
             if (trace) { log.trace("Inserted/updated " + rows + " rows"); }     
             
             log.trace("message Inserted/updated " + rows + " rows");
          }
          catch (Exception e)
          {
             wrap.exceptionOccurred();
             throw e;
          }
          finally
          {
             if (psReference != null)
             {
                try
                {
                   psReference.close();
                }
                catch (Throwable t)
                {
                }
             }
             if (psMessage != null)
             {
                try
                {
                   psMessage.close();
                }
                catch (Throwable t)
                {
                }
             }
             if (conn != null)
             {
                try
                {
                   conn.close();
                }
                catch (Throwable t)
                {
                }
             }
             try
             {
                wrap.end();
             }
             finally
             {   
                //Release Lock
                LockMap.instance.releaseLock(m);
             }
          }      
       }
    }
    
    public void updateDeliveryCount(long channelID, MessageReference ref) throws Exception
    {
       TransactionWrapper wrap = new TransactionWrapper();
       
       PreparedStatement psReference = null;
       
       Connection conn = ds.getConnection();
        
       try
       {                                    
          psReference = conn.prepareStatement(getSQLStatement("UPDATE_DELIVERYCOUNT"));
          
          psReference.setInt(1, ref.getDeliveryCount());
          
          psReference.setLong(2, channelID);
          
          psReference.setLong(3, ref.getMessageID());
          
          int rows = updateWithRetry(psReference);
 
          if (trace) { log.trace("Updated " + rows + " rows"); }
       }
       catch (Exception e)
       {
          wrap.exceptionOccurred();
          throw e;
       }
       finally
       {
          if (psReference != null)
          {
             try
             {
                psReference.close();
             }
             catch (Throwable t)
             {
             }
          }         
          if (conn != null)
          {
             try
             {
                conn.close();
             }
             catch (Throwable t)
             {
             }
          }
          wrap.end();                        
       }  
    }
    
    public void removeReference(long channelID, MessageReference ref, Transaction tx) throws Exception
    {      
       if (tx != null)
       {
          //In a tx so we just add the ref in the tx in memory for now
 
          TransactionCallback callback = getCallback(tx);
 
          callback.addReferenceToRemove(channelID, ref);
       }
       else
       {         
          //No tx so we remove the reference directly from the db
          
          TransactionWrapper wrap = new TransactionWrapper();
          
          PreparedStatement psReference = null;
          PreparedStatement psUpdate = null;
          PreparedStatement psMessage = null;
          
          Connection conn = ds.getConnection();
          
          Message m = ref.getMessage();         
          
          try
          {
             //get lock on message
             LockMap.instance.obtainLock(m);
                               
             psReference = conn.prepareStatement(getSQLStatement("DELETE_MESSAGE_REF"));
             
             //Remove the message reference
             removeReference(channelID, ref, psReference);
             
             int rows = updateWithRetry(psReference);
             
             if (rows != 1)
             {
               // http://jira.jboss.com/jira/browse/JBMESSAGING-808
               log.warn("Failed to remove row for: " + ref);
               return;
               //throw new IllegalStateException("Failed to remove row for: " + ref);
             }
             
             if (trace) { log.trace("Deleted " + rows + " rows"); }
             
             //Update the messages channel count
             
             psUpdate = conn.prepareStatement(getSQLStatement("DEC_CHANNELCOUNT"));
             
             decrementChannelCount(m, psUpdate);
             
             rows = updateWithRetry(psUpdate);
             
             if (trace) { log.trace("Updated " + rows + " rows"); } 
             
             //Delete the message (if necessary)
             
             psMessage = conn.prepareStatement(getSQLStatement("DELETE_MESSAGE"));
             
             removeMessage(m, psMessage);
                        
             rows = updateWithRetry(psMessage);
             
             if (trace) { log.trace("Delete " + rows + " rows"); }                           
          }
          catch (Exception e)
          {
             wrap.exceptionOccurred();
             throw e;
          }
          finally
          {
             if (psReference != null)
             {
                try
                {
                   psReference.close();
                }
                catch (Throwable t)
                {
                }
             }
             if (psUpdate != null)
             {
                try
                {
                   psUpdate.close();
                }
                catch (Throwable t)
                {
                }
             }
             if (psMessage != null)
             {
                try
                {
                   psMessage.close();
                }
                catch (Throwable t)
                {
                }
             }
             if (conn != null)
             {
                try
                {
                   conn.close();
                }
                catch (Throwable t)
                {
                }
             }
             try
             {
                wrap.end();               
             }
             finally
             {      
                //release the lock
                LockMap.instance.releaseLock(m);
             }
          }      
       }
    }
    
            
    public boolean referenceExists(long channelID, long messageID) throws Exception
    {
       Connection conn = null;
       PreparedStatement st = null;
       ResultSet rs = null;
       TransactionWrapper wrap = new TransactionWrapper();
       
       try
       {
          conn = ds.getConnection();
          
          st = conn.prepareStatement(getSQLStatement("SELECT_EXISTS_REF"));
          st.setLong(1, channelID);
          st.setLong(2, messageID);
          
          rs = st.executeQuery();
          
          if (rs.next())
          {
             return true;
          }
          else
          {
             return false;
          }
       }
       catch (Exception e)
       {
          wrap.exceptionOccurred();
          throw e;
       }
       finally
       {
          if (rs != null)
          {
             try
             {
                rs.close();
             }
             catch (Throwable e)
             {
             }
          }
          if (st != null)
          {
             try
             {
                st.close();
             }
             catch (Throwable e)
             {
             }
          }
          if (conn != null)
          {
             try
             {
                conn.close();
             }
             catch (Throwable e)
             {
             }
          }
          wrap.end();
       }
    }
    
    // Public --------------------------------------------------------
    
    public String toString()
    {
       return "JDBCPersistenceManager[" + Integer.toHexString(hashCode()) + "]";
    }   
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
       
    protected TransactionCallback getCallback(Transaction tx)
    {
       TransactionCallback callback = (TransactionCallback) tx.getCallback(this);
 
       if (callback == null)
       {
          callback = new TransactionCallback(tx);
 
          tx.addCallback(callback, this);
       }
 
       return callback;
    }
    
    /**
     * We order the list of references in ascending message order thus preventing deadlock when 2 or
     * more channels are updating the same messages in different transactions.
     */
    protected void orderReferences(List references)
    {      
       Collections.sort(references, MessageOrderComparator.instance);
    }
    
    protected void handleBeforeCommit1PC(List refsToAdd, List refsToRemove, Transaction tx)
       throws Exception
    {
       //TODO - A slight optimisation - it's possible we have refs referring to the same message
       //       so we will end up acquiring the lock more than once which is unnecessary. If find
       //       unique set of messages can avoid this.
 
       List allRefs = new ArrayList(refsToAdd.size() + refsToRemove.size());
 
       for(Iterator i = refsToAdd.iterator(); i.hasNext(); )
       {
          ChannelRefPair pair = (ChannelRefPair)i.next();
          allRefs.add(pair.ref);
       }
 
       for(Iterator i = refsToRemove.iterator(); i.hasNext(); )
       {
          ChannelRefPair pair = (ChannelRefPair)i.next();
          allRefs.add(pair.ref);
       }
             
       orderReferences(allRefs);
       
       // For one phase we simply add rows corresponding to the refs and remove rows corresponding to
       // the deliveries in one jdbc tx. We also need to store or remove messages as necessary,
       // depending on whether they've already been stored or still referenced by other channels.
          
       Connection conn = null;
       PreparedStatement psReference = null;
       PreparedStatement psInsertMessage = null;
       PreparedStatement psIncMessage = null;
       PreparedStatement psDecMessage = null;
       PreparedStatement psDeleteMessage = null;
       TransactionWrapper wrap = new TransactionWrapper();
       
       try
       {
          conn = ds.getConnection();
          
          // Obtain locks on all messages
          getLocks(allRefs);
          
          // First the adds
 
          boolean messageInsertsInBatch = false;
          boolean messageUpdatesInBatch = false;
          boolean batch = usingBatchUpdates && refsToAdd.size() > 0;
 
          if (batch)
          {
             psReference = conn.prepareStatement(getSQLStatement("INSERT_MESSAGE_REF"));
             psInsertMessage = conn.prepareStatement(getSQLStatement("INSERT_MESSAGE"));
             psIncMessage = conn.prepareStatement(getSQLStatement("INC_CHANNELCOUNT"));
          }
 
          for(Iterator i = refsToAdd.iterator(); i.hasNext(); )
          {
             ChannelRefPair pair = (ChannelRefPair)i.next();
             MessageReference ref = pair.ref;
                                                 
             if (!batch)
             {
                psReference = conn.prepareStatement(getSQLStatement("INSERT_MESSAGE_REF"));
             }
             
             // Now store the reference
             addReference(pair.channelID, ref, psReference, false);
               
             if (batch)
             {
                psReference.addBatch();
             }
             else
             {
                int rows = updateWithRetry(psReference);
                
                if (trace) { log.trace("Inserted " + rows + " rows"); }                              
                
                psReference.close();
                psReference = null;
             }
             
             Message m = ref.getMessage();        
             
             if (!batch)
             {
                psInsertMessage = conn.prepareStatement(getSQLStatement("INSERT_MESSAGE"));
                psIncMessage = conn.prepareStatement(getSQLStatement("INC_CHANNELCOUNT"));
             }
                          
             boolean added;
             if (!m.isPersisted())
             {               
                // First time so add message
                storeMessage(m, psInsertMessage);
                added = true;
                m.setPersisted(true);
             }
             else
             {               
                // Update message channel count
                incrementChannelCount(m, psIncMessage);
                added = false;
             }
             
             if (batch)
             {
                if (added)
                {
                   psInsertMessage.addBatch();
                   messageInsertsInBatch = true;
                }
                else
                { 
                   psIncMessage.addBatch();
                   messageUpdatesInBatch = true;
                }
             }
             else
             {
                if (added)
                {
                   int rows = updateWithRetry(psInsertMessage);
                   if (trace) { log.trace("Inserted " + rows + " rows"); }
                }
                else
                {
                   int rows = updateWithRetry(psIncMessage);
                   if (trace) { log.trace("Updated " + rows + " rows"); }
                }
                psInsertMessage.close();
                psInsertMessage = null;
                psIncMessage.close();
                psIncMessage = null;
             }
          }         
          
          if (batch)
          {
             // Process the add batch
 
             int[] rowsReference = updateWithRetryBatch(psReference);
             
             if (trace) { logBatchUpdate(getSQLStatement("INSERT_MESSAGE_REF"), rowsReference, "inserted"); }
             
             if (messageInsertsInBatch)
             {
                int[] rowsMessage = updateWithRetryBatch(psInsertMessage);
                if (trace) { logBatchUpdate(getSQLStatement("INSERT_MESSAGE"), rowsMessage, "inserted"); }
             }
 
             if (messageUpdatesInBatch)
             {
                int[] rowsMessage = updateWithRetryBatch(psIncMessage);
                if (trace) { logBatchUpdate(getSQLStatement("INC_CHANNELCOUNT"), rowsMessage, "updated"); }
             }
             
             psReference.close();
             psReference = null;
             psInsertMessage.close();
             psInsertMessage = null;
             psIncMessage.close();
             psIncMessage = null;
          }
          
          // Now the removes
 
          psReference = null;
          psDeleteMessage = null;
          batch = usingBatchUpdates && refsToRemove.size() > 0;
 
          if (batch)
          {
             psReference = conn.prepareStatement(getSQLStatement("DELETE_MESSAGE_REF"));
             psDeleteMessage = conn.prepareStatement(getSQLStatement("DELETE_MESSAGE"));
             psDecMessage = conn.prepareStatement(getSQLStatement("DEC_CHANNELCOUNT"));
          }
 
          
          for(Iterator i = refsToRemove.iterator(); i.hasNext(); )
          {
             ChannelRefPair pair = (ChannelRefPair)i.next();
             
             if (!batch)
             {
                psReference = conn.prepareStatement(getSQLStatement("DELETE_MESSAGE_REF"));
             }
             
             removeReference(pair.channelID, pair.ref, psReference);
             
             if (batch)
             {
                psReference.addBatch();
             }
             else
             {
                int rows = updateWithRetry(psReference);
                if (trace) { log.trace("Deleted " + rows + " rows"); }
                psReference.close();
                psReference = null;
             }
             
             if (!batch)
             {
                psDeleteMessage = conn.prepareStatement(getSQLStatement("DELETE_MESSAGE"));
                psDecMessage = conn.prepareStatement(getSQLStatement("DEC_CHANNELCOUNT"));
             }
             
             Message m = pair.ref.getMessage();
                                 
             // Update the channel count
             
             decrementChannelCount(m, psDecMessage);
             
             // Delete the message (if necessary)
             
             removeMessage(m, psDeleteMessage);
                        
             if (batch)
             {
                psDecMessage.addBatch();
                psDeleteMessage.addBatch();
             }
             else
             {
                int rows = updateWithRetry(psDecMessage);
                if (trace) { log.trace("Updated " + rows + " rows"); }
                
                rows = updateWithRetry(psDeleteMessage);
                if (trace) { log.trace("Deleted " + rows + " rows"); }
 
                psDeleteMessage.close();
                psDeleteMessage = null;
                psDecMessage.close();
                psDecMessage = null;
             }            
          }
          
          if (batch)
          {
             // Process the remove batch
 
             int[] rows = updateWithRetryBatch(psReference);
             
             if (trace) { logBatchUpdate(getSQLStatement("DELETE_MESSAGE_REF"), rows, "deleted"); }
             
             rows = updateWithRetryBatch(psDecMessage);
             
             if (trace) { logBatchUpdate(getSQLStatement("DEC_CHANNELCOUNT"), rows, "updated"); }
 
             rows = updateWithRetryBatch(psDeleteMessage);
             
             if (trace) { logBatchUpdate(getSQLStatement("DELETE_MESSAGE"), rows, "deleted"); }
 
             psReference.close();
             psReference = null;
             psDeleteMessage.close();
             psDeleteMessage = null;
             psDecMessage.close();
             psDecMessage = null;
          }         
       }
       catch (Exception e)
       {
          wrap.exceptionOccurred();                  
          throw e;
       }
       finally
       {
          if (psReference != null)
          {
             try
             {
                psReference.close();
             }
             catch (Throwable t)
             {
             }
          }
          if (psInsertMessage != null)
          {
             try
             {
                psInsertMessage.close();
             }
             catch (Throwable t)
             {
             }
          }
          if (psIncMessage != null)
          {
             try
             {
                psIncMessage.close();
             }
             catch (Throwable t)
             {
             }
          }
          if (psDecMessage != null)
          {
             try
             {
                psDecMessage.close();
             }
             catch (Throwable t)
             {
             }
          }
          if (psDeleteMessage != null)
          {
             try
             {
                psDeleteMessage.close();
             }
             catch (Throwable t)
             {
             }
          }
          if (conn != null)
          {
             try
             {
                conn.close();
             }
             catch (Throwable e)
             {
             }
          }
          try
          {
             wrap.end();                        
          }
          finally
          {  
             //Release the locks
             this.releaseLocks(allRefs);
          }
       }
    }
    
    protected void handleBeforeCommit2PC(List refsToRemove, Transaction tx)
       throws Exception
    {          
       Connection conn = null;
       PreparedStatement psUpdateMessage = null;
       PreparedStatement psDeleteMessage = null;
       TransactionWrapper wrap = new TransactionWrapper();
       
       List refs = new ArrayList(refsToRemove.size());
       Iterator iter = refsToRemove.iterator();
       while (iter.hasNext())
       {
          ChannelRefPair pair = (ChannelRefPair)iter.next();
          refs.add(pair.ref);
       }
             
       orderReferences(refs);      
       
       try
       {
          //get locks on all the refs
          this.getLocks(refs);
          
          conn = ds.getConnection();
                   
          //2PC commit
          
          //First we commit any refs in state "+" to "C" and delete any
          //refs in state "-", then we
          //remove any messages due to refs we just removed
          //if they're not referenced elsewhere
          
          commitPreparedTransaction(tx, conn);
          
          boolean batch = usingBatchUpdates && refsToRemove.size() > 0;
 
          if (batch)
          {
             psDeleteMessage = conn.prepareStatement(getSQLStatement("DELETE_MESSAGE"));
             psUpdateMessage = conn.prepareStatement(getSQLStatement("DEC_CHANNELCOUNT"));
          }
                   
          iter = refsToRemove.iterator();
          while (iter.hasNext())
          {
             ChannelRefPair pair = (ChannelRefPair) iter.next();
             
             MessageReference ref = pair.ref;
             
             if (!batch)
             {
                psDeleteMessage = conn.prepareStatement(getSQLStatement("DELETE_MESSAGE"));
                psUpdateMessage = conn.prepareStatement(getSQLStatement("DEC_CHANNELCOUNT"));
             }
             
             Message m = ref.getMessage();
                                    
             //We may need to remove the message itself
             
             //Update the channel count
             
             decrementChannelCount(m, psUpdateMessage);
             
             //Remove the message (if necessary)
             
             removeMessage(m, psDeleteMessage);          
                            
             if (batch)
             {
                psUpdateMessage.addBatch();
                 
                psDeleteMessage.addBatch(); 
             }
             else
             {
                int rows = updateWithRetry(psUpdateMessage);
                
                if (trace) { log.trace("Updated " + rows + " rows"); }
                
                rows = updateWithRetry(psDeleteMessage);
                
                if (trace) { log.trace("Deleted " + rows + " rows"); }
                
                psDeleteMessage.close();
                psDeleteMessage = null;
                psUpdateMessage.close();
                psUpdateMessage = null;
             }
          }         
          
          if (batch)
          {
             int[] rows = updateWithRetryBatch(psUpdateMessage);
             
             if (trace) { logBatchUpdate(getSQLStatement("DEC_CHANNELCOUNT"), rows, "updated"); }
             
             psUpdateMessage.close();
             psUpdateMessage = null;
             
             rows = updateWithRetryBatch(psDeleteMessage);
             
             if (trace) { logBatchUpdate(getSQLStatement("DELETE_MESSAGE"), rows, "deleted"); }
             
             psDeleteMessage.close();
             psDeleteMessage = null;                           
          }         
       }
       catch (Exception e)
       {
          wrap.exceptionOccurred();
          throw e;
       }
       finally
       {
          if (psDeleteMessage != null)
          {
             try
             {
                psDeleteMessage.close();
             }
             catch (Throwable t)
             {
             }
          }
          if (psUpdateMessage != null)
          {
             try
             {
                psUpdateMessage.close();
             }
             catch (Throwable t)
             {
             }
          }
          if (conn != null)
          {
             try
             {
                conn.close();
             }
             catch (Throwable e)
             {
             }
          }
          try
          {
             wrap.end();
          }
          finally
          {
             //release the locks
             this.releaseLocks(refs);
          }
       }
    }
    
    protected void handleBeforePrepare(List refsToAdd, List refsToRemove, Transaction tx) throws Exception
    {
       //We only need to lock on the adds
       List refs = new ArrayList(refsToAdd.size());
       
       Iterator iter = refsToAdd.iterator();
       while (iter.hasNext())
       {
          ChannelRefPair pair = (ChannelRefPair)iter.next();
          
          refs.add(pair.ref);
       }
       
       orderReferences(refs);
       
       //We insert a tx record and
       //a row for each ref with +
       //and update the row for each delivery with "-"
       
       PreparedStatement psReference = null;
       PreparedStatement psInsertMessage = null;
       PreparedStatement psUpdateMessage = null;
       Connection conn = null;
       TransactionWrapper wrap = new TransactionWrapper();
       
       try
       {
          //get the locks
          getLocks(refs);
          
          conn = ds.getConnection();
          
          //Insert the tx record
          if (!refsToAdd.isEmpty() || !refsToRemove.isEmpty())
          {
             addTXRecord(conn, tx);
          }
          
          iter = refsToAdd.iterator();
          
          boolean batch = usingBatchUpdates && refsToAdd.size() > 1;
          boolean messageInsertsInBatch = false;
          boolean messageUpdatesInBatch = false;
          if (batch)
          {
             psReference = conn.prepareStatement(getSQLStatement("INSERT_MESSAGE_REF"));
             psInsertMessage = conn.prepareStatement(getSQLStatement("INSERT_MESSAGE"));
             psUpdateMessage = conn.prepareStatement(getSQLStatement("INC_CHANNELCOUNT"));
          }
          
          while (iter.hasNext())
          {
             ChannelRefPair pair = (ChannelRefPair) iter.next();
             
             if (!batch)
             {
                psReference = conn.prepareStatement(getSQLStatement("INSERT_MESSAGE_REF"));
             }
             
             prepareToAddReference(pair.channelID, pair.ref, tx, psReference);
             
             if (batch)
             {
                psReference.addBatch();
             }
             else
             {
                int rows = updateWithRetry(psReference);
                
                if (trace) { log.trace("Inserted " + rows + " rows"); }
                
                psReference.close();
                psReference = null;
             }
             
             if (!batch)
             {
                psInsertMessage = conn.prepareStatement(getSQLStatement("INSERT_MESSAGE"));
                psUpdateMessage = conn.prepareStatement(getSQLStatement("INC_CHANNELCOUNT"));
             }
             
             Message m = pair.ref.getMessage();
                    
             boolean added;         
             
             if (!m.isPersisted())
             {
                //First time so persist the message
                storeMessage(m, psInsertMessage);
                
                m.setPersisted(true);
                
                added = true;
             }
             else
             {
                //Update message channel count
                incrementChannelCount(m, psUpdateMessage);
                
                added = false;
             }
             
             if (batch)
             {
                if (added)
                {
                   psInsertMessage.addBatch();
                   messageInsertsInBatch = true;
                }
                else
                {
                   psUpdateMessage.addBatch();
                   messageUpdatesInBatch = true;
                }
             }
             else
             {
                if (added)
                {
                   int rows = updateWithRetry(psInsertMessage);
                   
                   if (trace) { log.trace("Inserted " + rows + " rows"); }
                }
                else
                {
                   int rows = updateWithRetry(psUpdateMessage);
                   
                   if (trace) { log.trace("Updated " + rows + " rows"); }
                }
                psInsertMessage.close();
                psInsertMessage = null;
                psUpdateMessage.close();
                psUpdateMessage = null;
             }
          }         
          
          if (batch)
          {
             int[] rowsReference = updateWithRetryBatch(psReference);
             
             if (trace) { logBatchUpdate(getSQLStatement("INSERT_MESSAGE_REF"), rowsReference, "inserted"); }
             
             if (messageInsertsInBatch)
             {
                int[] rowsMessage = updateWithRetryBatch(psInsertMessage);
                
                if (trace) { logBatchUpdate(getSQLStatement("INSERT_MESSAGE"), rowsMessage, "inserted"); }
             }
             if (messageUpdatesInBatch)
             {
                int[] rowsMessage = updateWithRetryBatch(psUpdateMessage);
                
                if (trace) { logBatchUpdate(getSQLStatement("INC_CHANNELCOUNT"), rowsMessage, "updated"); }
             }
             
             psReference.close();
             psReference = null;
             psInsertMessage.close();
             psInsertMessage = null;
             psUpdateMessage.close();
             psUpdateMessage = null;
          }
          
          //Now the removes
          
          iter = refsToRemove.iterator();
          
          batch = usingBatchUpdates && refsToRemove.size() > 1;
          if (batch)
          {
             psReference = conn.prepareStatement(getSQLStatement("UPDATE_MESSAGE_REF"));
          }
 
          while (iter.hasNext())
          {
             ChannelRefPair pair = (ChannelRefPair) iter.next();
             
             if (!batch)
             {
                psReference = conn.prepareStatement(getSQLStatement("UPDATE_MESSAGE_REF"));
             }
             
             prepareToRemoveReference(pair.channelID, pair.ref, tx, psReference);
             
             if (batch)
             {
                psReference.addBatch();
             }
             else
             {
                int rows = updateWithRetry(psReference);
                
                if (trace) { log.trace("updated " + rows + " rows"); }
                
                psReference.close();
                psReference = null;
             }
          }
          
          if (batch)
          {
             int[] rows = updateWithRetryBatch(psReference);
             
             if (trace) { logBatchUpdate(getSQLStatement("UPDATE_MESSAGE_REF"), rows, "updated"); }
             
             psReference.close();
             psReference = null;
          }
       }
       catch (Exception e)
       {
          wrap.exceptionOccurred();
          throw e;
       }
       finally
       {
          if (psReference != null)
          {
             try
             {
                psReference.close();
             }
             catch (Throwable t)
             {
             }
          }
          if (psInsertMessage != null)
          {
             try
             {
                psInsertMessage.close();
             }
             catch (Throwable t)
             {
             }
          }
          if (psUpdateMessage != null)
          {
             try
             {
                psUpdateMessage.close();
             }
             catch (Throwable t)
             {
             }
          }
          if (conn != null)
          {
             try
             {
                conn.close();
             }
             catch (Throwable e)
             {
             }
          }
          try
          {
             wrap.end();            
          }
          finally
          {
             //release the locks
             
             this.releaseLocks(refs);
          }
       }
    }
    
    protected void handleBeforeRollback(List refsToAdd, Transaction tx) throws Exception
    {
       //remove refs marked with +
       //and update rows marked with - to C
             
       PreparedStatement psDeleteMessage = null;
       PreparedStatement psUpdateMessage = null;
       Connection conn = null;
       TransactionWrapper wrap = new TransactionWrapper();
       
       List refs = new ArrayList(refsToAdd.size());
       
       Iterator iter = refsToAdd.iterator();
       
       while (iter.hasNext())
       {
          ChannelRefPair pair = (ChannelRefPair)iter.next();
          refs.add(pair.ref);
       }
       
       orderReferences(refs);
       
       try
       {
          this.getLocks(refs);
          
          conn = ds.getConnection();
          
          rollbackPreparedTransaction(tx, conn);
          
          iter = refsToAdd.iterator();
          
          boolean batch = usingBatchUpdates && refsToAdd.size() > 1;
 
          if (batch)
          {
             psDeleteMessage = conn.prepareStatement(getSQLStatement("DELETE_MESSAGE"));
             psUpdateMessage = conn.prepareStatement(getSQLStatement("DEC_CHANNELCOUNT"));
          }
                                  
          while (iter.hasNext())
          {
             ChannelRefPair pair = (ChannelRefPair) iter.next();
             
             if (!batch)
             {
                psDeleteMessage = conn.prepareStatement(getSQLStatement("DELETE_MESSAGE"));
                psUpdateMessage = conn.prepareStatement(getSQLStatement("DEC_CHANNELCOUNT"));
             }
             
             Message m = pair.ref.getMessage();
                                          
             //We may need to remove the message for messages added during the prepare stage
                         
             //update the channel count
             
             decrementChannelCount(m, psUpdateMessage);
             
             //remove the message (if necessary)
             
             removeMessage(m, psDeleteMessage);
                                         
             if (batch)
             {
                psUpdateMessage.addBatch();
                
                psDeleteMessage.addBatch();
             }
             else
             {
                int rows = updateWithRetry(psUpdateMessage);
                
                if (trace) { log.trace("updated " + rows + " rows"); }
                
                rows = updateWithRetry(psDeleteMessage);
                
                if (trace) { log.trace("deleted " + rows + " rows"); }
                
                psDeleteMessage.close();
                psDeleteMessage = null;
                psUpdateMessage.close();
                psUpdateMessage = null;
             }            
          }
          
          if (batch)
          {
             int[] rows = updateWithRetryBatch(psUpdateMessage);
             
             if (trace) { logBatchUpdate(getSQLStatement("DEC_CHANNELCOUNT"), rows, "updated"); }
             
             rows = updateWithRetryBatch(psDeleteMessage);
             
             if (trace) { logBatchUpdate(getSQLStatement("DELETE_MESSAGE"), rows, "deleted"); }
             
             psDeleteMessage.close();
             psDeleteMessage = null;
             
             psUpdateMessage.close();
             psUpdateMessage = null;            
          }
       }
       catch (Exception e)
       {
          wrap.exceptionOccurred();
          throw e;
       }
       finally
       {
          if (psDeleteMessage != null)
          {
             try
             {
                psDeleteMessage.close();
             }
             catch (Throwable t)
             {
             }
          }
          if (psUpdateMessage != null)
          {
             try
             {
                psUpdateMessage.close();
             }
             catch (Throwable t)
             {
             }
          }
          if (conn != null)
          {
             try
             {
                conn.close();
             }
             catch (Throwable e)
             {
             }
          }
          try
          {
             wrap.end();
          }
          finally
          {
             //release locks
             this.releaseLocks(refs);
          }
       }      
    }
    
    
    protected void addTXRecord(Connection conn, Transaction tx) throws Exception
    {
       if (trace)
       {
          log.trace("Inserting tx record for " + tx);
       }
       
       PreparedStatement ps = null;
       String statement = "UNDEFINED";
       int rows = -1;
       int formatID = -1;
       try
       {
          statement = getSQLStatement("INSERT_TRANSACTION");
          
          ps = conn.prepareStatement(statement);
          
          ps.setLong(1, tx.getId());
          
          Xid xid = tx.getXid();
          formatID = xid.getFormatId();
          ps.setBytes(2, xid.getBranchQualifier());
          ps.setInt(3, formatID);
          ps.setBytes(4, xid.getGlobalTransactionId());
          
          rows = updateWithRetry(ps);
          
       }
       finally
       {
          if (trace)
          {
             String s = JDBCUtil.statementToString(statement, new Long(tx.getId()), "<byte-array>",
                   new Integer(formatID), "<byte-array>");
             log.trace(s + (rows == -1 ? " failed!" : " inserted " + rows + " row(s)"));
          }
          try
          {
             if (ps != null)
             {
                ps.close();
             }
          }
          catch (Throwable e)
          {
             //Ignore
          }
       }
    }
    
    protected void removeTXRecord(Connection conn, Transaction tx) throws Exception
    {
       PreparedStatement ps = null;
       try
       {
          ps = conn.prepareStatement(getSQLStatement("DELETE_TRANSACTION"));
          
          ps.setLong(1, tx.getId());
          
          int rows = updateWithRetry(ps);
          
          if (trace)
          {
             log.trace(JDBCUtil.statementToString(getSQLStatement("DELETE_TRANSACTION"), new Long(tx.getId())) + " removed " + rows + " row(s)");
          }
       }
       finally
       {
          try
          {
             if (ps != null)
             {
                ps.close();
             }
          }
          catch (Throwable e)
          {
             //Ignore
          }
       }
    }  
    
    protected void addReference(long channelID, MessageReference ref,
                                PreparedStatement ps, boolean paged) throws Exception
    {
       if (trace) { log.trace("adding " + ref + " to channel " + channelID); }
       
       ps.setLong(1, channelID);
       ps.setLong(2, ref.getMessageID());
       ps.setNull(3, Types.BIGINT);
       ps.setString(4, "C");
       ps.setLong(5, getOrdering());
       if (paged)
       {
          ps.setLong(6, ref.getPagingOrder());
       }
       else
       {
          ps.setNull(6, Types.BIGINT);
       }
       ps.setInt(7, ref.getDeliveryCount());
       ps.setString(8, ref.isReliable() ? "Y" : "N");
       ps.setLong(9, ref.getScheduledDeliveryTime());
    }
    
    protected void removeReference(long channelID, MessageReference ref, PreparedStatement ps)
       throws Exception
    {
       if (trace) { log.trace("removing " + ref + " from channel " + channelID); }
       
       ps.setLong(1, ref.getMessageID());
       ps.setLong(2, channelID);      
    }
    
    protected void prepareToAddReference(long channelID, MessageReference ref, Transaction tx, PreparedStatement ps)
      throws Exception
    {
       if (trace) { log.trace("adding " + ref + " to channel " + channelID + (tx == null ? " non-transactionally" : " on transaction: " + tx)); }
       
       ps.setLong(1, channelID);
       ps.setLong(2, ref.getMessageID());
       ps.setLong(3, tx.getId());
       ps.setString(4, "+");
       ps.setLong(5, getOrdering());
       ps.setNull(6, Types.BIGINT);      
       ps.setInt(7, ref.getDeliveryCount());
       ps.setString(8, ref.isReliable() ? "Y" : "N");
       ps.setLong(9, ref.getScheduledDeliveryTime());
    }
    
    protected void prepareToRemoveReference(long channelID, MessageReference ref, Transaction tx, PreparedStatement ps)
       throws Exception
    {
       if (trace)
       {
          log.trace("removing " + ref + " from channel " + channelID
                + (tx == null ? " non-transactionally" : " on transaction: " + tx));
       }
       
       ps.setLong(1, tx.getId()); 
       ps.setLong(2, ref.getMessageID());
       ps.setLong(3, channelID);           
    }
    
    protected void commitPreparedTransaction(Transaction tx, Connection conn) throws Exception
    {
       PreparedStatement ps = null;
       
       if (trace) { log.trace(this + " commitPreparedTransaction, tx= " + tx); }
         
       try
       {
          ps = conn.prepareStatement(getSQLStatement("COMMIT_MESSAGE_REF1"));
          
          ps.setLong(1, tx.getId());        
          
          int rows = updateWithRetry(ps);
          
          if (trace)
          {
             log.trace(JDBCUtil.statementToString(getSQLStatement("COMMIT_MESSAGE_REF1"), new Long(tx.getId())) + " removed " + rows + " row(s)");
          }
          
          ps.close();
          ps = conn.prepareStatement(getSQLStatement("COMMIT_MESSAGE_REF2"));
          ps.setLong(1, tx.getId());         
          
          rows = updateWithRetry(ps);
          
          if (trace)
          {
             log.trace(JDBCUtil.statementToString(getSQLStatement("COMMIT_MESSAGE_REF2"), new Long(tx.getId())) + " updated " + rows
                   + " row(s)");
          }
          
          removeTXRecord(conn, tx);
       }
       finally
       {
          if (ps != null)
          {
             try
             {
                ps.close();
             }
             catch (Throwable e)
             {
             }
          }
       }
    }
    
    protected void rollbackPreparedTransaction(Transaction tx, Connection conn) throws Exception
    {
       PreparedStatement ps = null;
       
       try
       {
          ps = conn.prepareStatement(getSQLStatement("ROLLBACK_MESSAGE_REF1"));
          
          ps.setLong(1, tx.getId());         
          
          int rows = updateWithRetry(ps);
          
          if (trace)
          {
             log.trace(JDBCUtil.statementToString(getSQLStatement("ROLLBACK_MESSAGE_REF1"), new Long(tx.getId())) + " removed " + rows + " row(s)");
          }
          
          ps.close();
          
          ps = conn.prepareStatement(getSQLStatement("ROLLBACK_MESSAGE_REF2"));
          ps.setLong(1, tx.getId());
          
          rows = updateWithRetry(ps);
          
          if (trace)
          {
             log.trace(JDBCUtil.statementToString(getSQLStatement("ROLLBACK_MESSAGE_REF2"), new Long(tx.getId())) + " updated " + rows
                   + " row(s)");
          }
          
          removeTXRecord(conn, tx);
       }
       finally
       {
          if (ps != null)
          {
             try
             {
                ps.close();
             }
             catch (Throwable e)
             {
             }
          }
       }
    }
    
    protected byte[] mapToBytes(Map map) throws Exception
    {
       if (map == null || map.isEmpty())
       {
          return null;
       }
       
       final int BUFFER_SIZE = 1024;
        
       ByteArrayOutputStream bos = new ByteArrayOutputStream(BUFFER_SIZE);
       
       DataOutputStream oos = new DataOutputStream(bos);
       
       StreamUtils.writeMap(oos, map, true);
       
       oos.close();
       
       return bos.toByteArray();
    }
    
    protected HashMap bytesToMap(byte[] bytes) throws Exception
    {
       if (bytes == null)
       {
          return new HashMap();
       }
        
       ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
       
       DataInputStream dais = new DataInputStream(bis);
       
       Map m = StreamUtils.readMap(dais, true);
       
       dais.close();
       
       HashMap map;
       if (!(m instanceof HashMap))
       {
          map = new HashMap(m);
       }
       else
       {
          map = (HashMap) m;
       }
       
       return map;
    }
    
 
    //TODO - combine these
    protected void incrementChannelCount(Message m, PreparedStatement ps) throws Exception
    {
       ps.setLong(1, m.getMessageID());
    }
          
    protected void decrementChannelCount(Message m, PreparedStatement ps) throws Exception
    {
       ps.setLong(1, m.getMessageID());
    }
    
    /**
     * Stores the message in the MESSAGE table.
     */
    protected void storeMessage(Message m, PreparedStatement ps) throws Exception
    {      
       // physically insert the row in the database
       // first set the fields from org.jboss.messaging.core.Routable
       ps.setLong(1, m.getMessageID());
       ps.setString(2, m.isReliable() ? "Y" : "N");
       ps.setLong(3, m.getExpiration());
       ps.setLong(4, m.getTimestamp());
       ps.setByte(5, m.getPriority());
       
       //Core headers
       byte[] bytes = mapToBytes(((MessageSupport) m).getHeaders());
       if (bytes != null)
       {
          setBytes(ps, 6, bytes);
       }
       else
       {
          ps.setNull(6, Types.LONGVARBINARY);
       }
       
       // now set the fields from org.jboss.messaging.core.Message
       
       byte[] payload = m.getPayloadAsByteArray();
       if (payload != null)
       {
          setBytes(ps, 7, payload);
       }
       else
       {
          ps.setNull(7, Types.LONGVARBINARY);
       }
       
       //The number of channels that hold a reference to the message - initially always 1
       ps.setInt(8, 1);
       
       //Now set the fields from org.joss.jms.message.JBossMessage if appropriate
       
       //TODO - We are mixing concerns here
       //The basic JDBCPersistencManager should *only* know about core messages - not 
       //JBossMessages - we should subclass JBDCPersistenceManager and the JBossMessage
       //specific code in a subclass
       if (m instanceof JBossMessage)
       {
          JBossMessage jbm = (JBossMessage) m;
          
          ps.setByte(9, jbm.getType());
          if (jbm.getJMSType() != null)
          {
             ps.setString(10, jbm.getJMSType());
          }
          else
          {
             ps.setNull(10, Types.VARCHAR);
          }
          if (jbm.getJMSCorrelationID() != null)
          {
             ps.setString(11, jbm.getJMSCorrelationID());
          }
          else
          {
             ps.setNull(11, Types.VARCHAR);
          }
          if (jbm.getJMSCorrelationIDAsBytes() != null)
          {
             ps.setBytes(12, jbm.getJMSCorrelationIDAsBytes());
          }
          else
          {
             ps.setNull(12, Types.BINARY);
          }
          
          JBossDestination jbd = (JBossDestination) jbm.getJMSDestination();
         
          ps.setString(13, (jbd.isQueue() ? "Q" : "T") + jbd);
          
          JBossDestination replyTo = (JBossDestination) jbm.getJMSReplyTo();
          if (replyTo == null)
          {
             ps.setNull(14, Types.BIGINT);
          }
          else
          {            
             ps.setString(14, (replyTo.isQueue() ? "Q" : "T") + replyTo);
          }
          
          //jms properties
          bytes = mapToBytes(jbm.getJMSProperties());
          if (bytes != null)
          {
             setBytes(ps, 15, bytes);
          }
          else
          {
             ps.setNull(15, Types.LONGVARBINARY);
          }
       }
       else
       {
          ps.setByte(9, m.getType());
          ps.setNull(10, Types.VARCHAR);
          ps.setNull(11, Types.VARCHAR);
          ps.setNull(12, Types.BINARY);
          ps.setNull(13, Types.BIGINT);
          ps.setNull(14, Types.BIGINT);
          ps.setNull(15, Types.LONGVARBINARY);
       }
    }
    
    /**
     * Removes the message from the MESSAGE table.
     */
    protected void removeMessage(Message message, PreparedStatement ps) throws Exception
    {
       // physically delete the row in the database
       ps.setLong(1, message.getMessageID());      
    }
      
    protected void setBytes(PreparedStatement ps, int columnIndex, byte[] bytes) throws Exception
    {
       if (usingBinaryStream)
       {
          //Set the bytes using a binary stream - likely to be better for large byte[]
          
          InputStream is = null;
          
          try
          {
             is = new ByteArrayInputStream(bytes);
             
             ps.setBinaryStream(columnIndex, is, bytes.length);
          }
          finally
          {
             if (is != null)
             {
                is.close();
             }
          }
       }
       else
       {
          //Set the bytes using setBytes() - likely to be better for smaller byte[]
          ps.setBytes(columnIndex, bytes);
       }
    }
    
    protected byte[] getBytes(ResultSet rs, int columnIndex) throws Exception
    {
       if (usingBinaryStream)
       {
          //Get the bytes using a binary stream - likely to be better for large byte[]
          
          InputStream is = null;
          ByteArrayOutputStream os = null;
          
          final int BUFFER_SIZE = 4096;
          
          try
          {
             InputStream i = rs.getBinaryStream(columnIndex);
             
             if (i == null)
             {
                return null;
             }
             
             is = new BufferedInputStream(rs.getBinaryStream(columnIndex), BUFFER_SIZE);
             
             os = new ByteArrayOutputStream(BUFFER_SIZE);
             
             int b;
             while ((b = is.read()) != -1)
             {
                os.write(b);
             }
             
             return os.toByteArray();
          }
          finally
          {
             if (is != null)
             {
                is.close();
             }
             if (os != null)
             {
                os.close();
             }
          }
       }
       else
       {
          //Get the bytes using getBytes() - better for smaller byte[]
          return rs.getBytes(columnIndex);
       }
    }
    
    protected void getLocks(List refs)
    {
       Iterator iter = refs.iterator();
       while (iter.hasNext())
       {
          MessageReference ref = (MessageReference)iter.next();
          Message m = ref.getMessage();
          LockMap.instance.obtainLock(m);        
       }
    }
    
    protected void releaseLocks(List refs)
    {
       Iterator iter = refs.iterator();
       while (iter.hasNext())
       {
          MessageReference ref = (MessageReference)iter.next();
          Message m = ref.getMessage();
          LockMap.instance.releaseLock(m);         
       }
    }
    
    protected void logBatchUpdate(String name, int[] rows, String action)
    {
       int count = 0;
       for (int i = 0; i < rows.length; i++)
       {
          count += rows[i];
       }
       log.trace("Batch update " + name + ", " + action + " total of " + count + " rows");
    }
    
    protected int updateWithRetry(PreparedStatement ps) throws Exception
    {
       return updateWithRetry(ps, false)[0];
    }
    
    protected int[] updateWithRetryBatch(PreparedStatement ps) throws Exception
    {
       return updateWithRetry(ps, true);
    }
    
    //PersistentServiceSupport overrides ----------------------------
    
    protected Map getDefaultDDLStatements()
    {
       Map map = new LinkedHashMap();
       //Message reference
       map.put("CREATE_MESSAGE_REFERENCE",
               "CREATE TABLE JMS_MESSAGE_REFERENCE (CHANNELID BIGINT, " +
               "MESSAGEID BIGINT, TRANSACTIONID BIGINT, STATE CHAR(1), ORD BIGINT, PAGE_ORD BIGINT, " +
               "DELIVERYCOUNT INTEGER, RELIABLE CHAR(1), LOADED CHAR(1), SCHED_DELIVERY BIGINT, PRIMARY KEY(CHANNELID, MESSAGEID))"); //CHANGED
       map.put("CREATE_IDX_MESSAGE_REF_TX", "CREATE INDEX JMS_MESSAGE_REF_TX ON JMS_MESSAGE_REFERENCE (TRANSACTIONID)");
       map.put("CREATE_IDX_MESSAGE_REF_ORD", "CREATE INDEX JMS_MESSAGE_REF_ORD ON JMS_MESSAGE_REFERENCE (ORD)");
       map.put("CREATE_IDX_MESSAGE_REF_PAGE_ORD", "CREATE INDEX JMS_MESSAGE_REF__PAGE_ORD ON JMS_MESSAGE_REFERENCE (PAGE_ORD)");
       map.put("CREATE_IDX_MESSAGE_REF_MESSAGEID", "CREATE INDEX JMS_MESSAGE_REF_MESSAGEID ON JMS_MESSAGE_REFERENCE (MESSAGEID)");
       map.put("CREATE_IDX_MESSAGE_REF_RELIABLE", "CREATE INDEX JMS_MESSAGE_REF_RELIABLE ON JMS_MESSAGE_REFERENCE (RELIABLE)");
       map.put("CREATE_IDX_MESSAGE_REF_SCHED_DELIVERY", "CREATE INDEX JMS_MESSAGE_REF_SCHED_DELIVERY ON JMS_MESSAGE_REFERENCE (SCHED_DELIVERY)");
       //Message
       map.put("CREATE_MESSAGE",
               "CREATE TABLE JMS_MESSAGE (MESSAGEID BIGINT, RELIABLE CHAR(1), " +
               "EXPIRATION BIGINT, TIMESTAMP BIGINT, PRIORITY TINYINT, COREHEADERS LONGVARBINARY, " +
               "PAYLOAD LONGVARBINARY, CHANNELCOUNT INTEGER, TYPE TINYINT, JMSTYPE VARCHAR(255), CORRELATIONID VARCHAR(255), " +
               "CORRELATIONID_BYTES VARBINARY(254), DESTINATION VARCHAR(255), REPLYTO VARCHAR(255), " +
               "JMSPROPERTIES LONGVARBINARY, " +
               "PRIMARY KEY (MESSAGEID))"); 
       //Transaction
       map.put("CREATE_TRANSACTION",
               "CREATE TABLE JMS_TRANSACTION (" +
               "TRANSACTIONID BIGINT, BRANCH_QUAL VARBINARY(254), " +
               "FORMAT_ID INTEGER, GLOBAL_TXID VARBINARY(254), PRIMARY KEY (TRANSACTIONID))");
       //Counter
       map.put("CREATE_COUNTER",
               "CREATE TABLE JMS_COUNTER (NAME VARCHAR(255), NEXT_ID BIGINT, PRIMARY KEY(NAME))");
       return map;
    }
       
    protected Map getDefaultDMLStatements()
    {                
       Map map = new LinkedHashMap();
       //Message reference
       map.put("INSERT_MESSAGE_REF",
               "INSERT INTO JMS_MESSAGE_REFERENCE (CHANNELID, MESSAGEID, TRANSACTIONID, STATE, ORD, PAGE_ORD, DELIVERYCOUNT, RELIABLE, SCHED_DELIVERY) " +
               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"); //CHANGED
       map.put("DELETE_MESSAGE_REF", "DELETE FROM JMS_MESSAGE_REFERENCE WHERE MESSAGEID=? AND CHANNELID=? AND STATE='C'");
       map.put("UPDATE_MESSAGE_REF",
               "UPDATE JMS_MESSAGE_REFERENCE SET TRANSACTIONID=?, STATE='-' " +
               "WHERE MESSAGEID=? AND CHANNELID=? AND STATE='C'");
       map.put("UPDATE_PAGE_ORDER", "UPDATE JMS_MESSAGE_REFERENCE SET PAGE_ORD = ? WHERE MESSAGEID=? AND CHANNELID=?");
       map.put("COMMIT_MESSAGE_REF1", "UPDATE JMS_MESSAGE_REFERENCE SET STATE='C', TRANSACTIONID = NULL WHERE TRANSACTIONID=? AND STATE='+'");
       map.put("COMMIT_MESSAGE_REF2", "DELETE FROM JMS_MESSAGE_REFERENCE WHERE TRANSACTIONID=? AND STATE='-'");
       map.put("ROLLBACK_MESSAGE_REF1", "DELETE FROM JMS_MESSAGE_REFERENCE WHERE TRANSACTIONID=? AND STATE='+'");
       map.put("ROLLBACK_MESSAGE_REF2", "UPDATE JMS_MESSAGE_REFERENCE SET STATE='C', TRANSACTIONID = NULL WHERE TRANSACTIONID=? AND STATE='-'");
       map.put("LOAD_PAGED_REFS",
               "SELECT MESSAGEID, DELIVERYCOUNT, PAGE_ORD, RELIABLE, SCHED_DELIVERY FROM JMS_MESSAGE_REFERENCE " +
               "WHERE CHANNELID = ? AND PAGE_ORD BETWEEN ? AND ? ORDER BY PAGE_ORD"); //CHANGED
       map.put("LOAD_UNPAGED_REFS",
               "SELECT MESSAGEID, DELIVERYCOUNT, RELIABLE, SCHED_DELIVERY FROM JMS_MESSAGE_REFERENCE WHERE STATE = 'C' " +
               "AND CHANNELID = ? AND PAGE_ORD IS NULL ORDER BY ORD"); //CHANGED
       map.put("UPDATE_RELIABLE_REFS_NOT_PAGED", "UPDATE JMS_MESSAGE_REFERENCE SET PAGE_ORD = NULL WHERE PAGE_ORD BETWEEN ? AND ? AND CHANNELID=?");       
       map.put("SELECT_MIN_MAX_PAGE_ORD", "SELECT MIN(PAGE_ORD), MAX(PAGE_ORD) FROM JMS_MESSAGE_REFERENCE WHERE CHANNELID = ?");
       map.put("SELECT_EXISTS_REF", "SELECT MESSAGEID FROM JMS_MESSAGE_REFERENCE WHERE CHANNELID = ? AND MESSAGEID = ?");
       map.put("UPDATE_DELIVERYCOUNT", "UPDATE JMS_MESSAGE_REFERENCE SET DELIVERYCOUNT = ? WHERE CHANNELID = ? AND MESSAGEID = ?");
       //Message
       map.put("LOAD_MESSAGES",
               "SELECT MESSAGEID, RELIABLE, EXPIRATION, TIMESTAMP, " +
               "PRIORITY, COREHEADERS, PAYLOAD, TYPE, JMSTYPE, CORRELATIONID, " +
               "CORRELATIONID_BYTES, DESTINATION, REPLYTO, JMSPROPERTIES " +
               "FROM JMS_MESSAGE");
       map.put("INSERT_MESSAGE",
               "INSERT INTO JMS_MESSAGE (MESSAGEID, RELIABLE, EXPIRATION, " +
               "TIMESTAMP, PRIORITY, COREHEADERS, PAYLOAD, CHANNELCOUNT, TYPE, JMSTYPE, CORRELATIONID, " +
               "CORRELATIONID_BYTES, DESTINATION, REPLYTO, JMSPROPERTIES) " +
               "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" );
       map.put("INC_CHANNELCOUNT", "UPDATE JMS_MESSAGE SET CHANNELCOUNT = CHANNELCOUNT + 1 WHERE MESSAGEID=?");
       map.put("DEC_CHANNELCOUNT", "UPDATE JMS_MESSAGE SET CHANNELCOUNT = CHANNELCOUNT - 1 WHERE MESSAGEID=?");
       map.put("DELETE_MESSAGE", "DELETE FROM JMS_MESSAGE WHERE MESSAGEID=? AND CHANNELCOUNT = 0");
       map.put("MESSAGEID_COLUMN", "MESSAGEID");
       map.put("MESSAGE_EXISTS", "SELECT MESSAGEID FROM JMS_MESSAGE WHERE MESSAGEID = ?");
       //Transaction
       map.put("INSERT_TRANSACTION",
               "INSERT INTO JMS_TRANSACTION (TRANSACTIONID, BRANCH_QUAL, FORMAT_ID, GLOBAL_TXID) " +
               "VALUES(?, ?, ?, ?)");
       map.put("DELETE_TRANSACTION", "DELETE FROM JMS_TRANSACTION WHERE TRANSACTIONID = ?");
       map.put("SELECT_PREPARED_TRANSACTIONS", "SELECT TRANSACTIONID, BRANCH_QUAL, FORMAT_ID, GLOBAL_TXID FROM JMS_TRANSACTION");
       map.put("SELECT_MESSAGEID_FOR_REF", "SELECT MESSAGEID, CHANNELID FROM JMS_MESSAGE_REFERENCE WHERE TRANSACTIONID = ? AND STATE = '+' ORDER BY ORD");
       map.put("SELECT_MESSAGEID_FOR_ACK", "SELECT MESSAGEID, CHANNELID FROM JMS_MESSAGE_REFERENCE WHERE TRANSACTIONID = ? AND STATE = '-' ORDER BY ORD");
       
       //Counter
       map.put("UPDATE_COUNTER", "UPDATE JMS_COUNTER SET NEXT_ID = ? WHERE NAME=?");
       map.put("SELECT_COUNTER", "SELECT NEXT_ID FROM JMS_COUNTER WHERE NAME=?");
       map.put("INSERT_COUNTER", "INSERT INTO JMS_COUNTER (NAME, NEXT_ID) VALUES (?, ?)");
       //Other
       map.put("SELECT_ALL_CHANNELS", "SELECT DISTINCT(CHANNELID) FROM JMS_MESSAGE_REFERENCE");      
       return map;
    }
    
    // Private -------------------------------------------------------
    
    private int[] updateWithRetry(PreparedStatement ps, boolean batch) throws Exception
    {
       final int MAX_TRIES = 25;      
       
       int rows = 0;
       
       int[] rowsArr = null;
       
       int tries = 0;
       
       while (true)
       {
          try
          {
             if (batch)
             {
                rowsArr = ps.executeBatch();
             }
             else
             {
                rows = ps.executeUpdate();
             }
             
             if (tries > 0)
             {
                log.warn("Update worked after retry");
             }
             break;
          }
          catch (SQLException e)
          {
             log.warn("SQLException caught - assuming deadlock detected, try:" + (tries + 1), e);
             tries++;
             if (tries == MAX_TRIES)
             {
                log.error("Retried " + tries + " times, now giving up");
                throw new IllegalStateException("Failed to update references");
             }
             log.warn("Trying again after a pause");
             //Now we wait for a random amount of time to minimise risk of deadlock
             Thread.sleep((long)(Math.random() * 500));
          }  
       }
       
       if (batch)
       {
          return rowsArr;
       }
       else
       {
          return new int[] { rows };
       }
    }
    
    private List getMessageChannelPair(String sqlQuery, long transactionId) throws Exception
    {
       if (trace) log.trace("loading message and channel ids for tx [" + transactionId + "]");
       
       Connection conn = null;
       PreparedStatement ps = null;
       ResultSet rs = null;
       TransactionWrapper wrap = new TransactionWrapper();
       
       try
       {
          conn = ds.getConnection();
          
          ps = conn.prepareStatement(sqlQuery);
          
          ps.setLong(1, transactionId);
          
          rs = ps.executeQuery();
          
          //Don't use a Map. A message could be in multiple channels in a tx, so if you use a map
          //when you put the same message again it's going to overwrite the previous put!!
          
          List holders = new ArrayList();
          
          //Unique set of messages
          Set msgIds = new HashSet();
          
          //TODO it would probably have been simpler just to have done all this in a SQL JOIN rather
          //than do the join in memory.....
          
          class Holder
          {
             long messageId;
             long channelId;
             Holder(long messageId, long channelId)
             {
                this.messageId = messageId;
                this.channelId = channelId;
             }
          }
                   
          while(rs.next())
          {            
             long messageId = rs.getLong(1);
             long channelId = rs.getLong(2);
             
             Holder holder = new Holder(messageId, channelId);
             
             holders.add(holder);
                         
             msgIds.add(new Long(messageId));
             
             if (trace) log.trace("Loaded MsgID: " + messageId + " and ChannelID: " + channelId);
          }
          
          Map messageMap = new HashMap();
          
          List messages = getMessages(new ArrayList(msgIds));
          
          for (Iterator iter = messages.iterator(); iter.hasNext(); )
          {
             Message msg = (Message)iter.next();
             
             messageMap.put(new Long(msg.getMessageID()), msg);            
          }
          
          List returnList = new ArrayList();
          
          for (Iterator iter = holders.iterator(); iter.hasNext(); )
          {
             Holder holder = (Holder)iter.next();
             
             Message msg = (Message)messageMap.get(new Long(holder.messageId));
             
             if (msg == null)
             {
                throw new IllegalStateException("Cannot find message " + holder.messageId);
             }
             
             MessageChannelPair pair = new MessageChannelPair(msg, holder.channelId);
             
             returnList.add(pair);
          }
          
          return returnList;
       }
       catch (Exception e)
       {
          wrap.exceptionOccurred();
          throw e;
       }
       finally
       {
          if (rs != null)
          {
             try
             {
                rs.close();
             }
             catch (Throwable e)
             {
             }
          }
          if (ps != null)
          {
             try
             {
                ps.close();
             }
             catch (Throwable e)
             {
             }
          }
          if (conn != null)
          {
             try
             {
                conn.close();
             }
             catch (Throwable e)
             {
             }
          }
          wrap.end();
       }
    }
    
    private synchronized long getOrdering()
    {
       //We generate the ordering for the message reference by taking the lowest 48 bits of the current time and
       //concetaning with a 15 bit rotating counter to form a string of 63 bits which we then place
       //in the right most bits of a long, giving a positive signed 63 bit integer.
       
       //We only have to guarantee ordering per session, so having slight differences of time on different nodes is
       //not a problem
       
       //This is good for about 8919 years - if you're still running JBoss Messaging then, I suggest you need an
       //upgrade!
       
       long order = System.currentTimeMillis();
       
       order = order << 15;
       
       order = order | orderCount;
       
       if (orderCount == Short.MAX_VALUE)
       {
          orderCount = 0;
       }
       else
       {
          orderCount++;
       }
       
       return order;
    }
 
    // Inner classes -------------------------------------------------
         
    private static class ChannelRefPair
    {
       private long channelID;
       private MessageReference ref;
       
       private ChannelRefPair(long channelID, MessageReference ref)
       {
          this.channelID = channelID;
          this.ref = ref;
       }
    }
    
    private class TransactionCallback implements TxCallback
    {
       private Transaction tx;
       
       private List refsToAdd;
       
       private List refsToRemove;
       
       private TransactionCallback(Transaction tx)
       {
          this.tx = tx;
          
          refsToAdd = new ArrayList();
          
          refsToRemove = new ArrayList();
       }
       
       private void addReferenceToAdd(long channelId, MessageReference ref)
       {
          refsToAdd.add(new ChannelRefPair(channelId, ref));
       }
       
       private void addReferenceToRemove(long channelId, MessageReference ref)
       {
          refsToRemove.add(new ChannelRefPair(channelId, ref));
       }
       
       public void afterCommit(boolean onePhase)
       {
          //NOOP
       }
       
       public void afterPrepare()
       {
          //NOOP
       }
       
       public void afterRollback(boolean onePhase)
       {
          //NOOP
       }
       
       public void beforeCommit(boolean onePhase) throws Exception
       {
          if (onePhase)
          {
             handleBeforeCommit1PC(refsToAdd, refsToRemove, tx);
          }
          else
          {
             handleBeforeCommit2PC(refsToRemove, tx);
          }
       }
       
       public void beforePrepare() throws Exception
       {
          handleBeforePrepare(refsToAdd, refsToRemove, tx);
       }
       
       public void beforeRollback(boolean onePhase) throws Exception
       {
          if (onePhase)
          {
             //NOOP - nothing in db
          }
          else
          {
             handleBeforeRollback(refsToAdd, tx);
          }
       }
    }
    
    static class MessageOrderComparator implements Comparator
    {
       static MessageOrderComparator instance = new MessageOrderComparator();
       
       public int compare(Object o1, Object o2)
       {        
          MessageReference ref1 = (MessageReference)o1;
          MessageReference ref2 = (MessageReference)o2;
 
          long id1 = ref1.getMessageID();         
          long id2 = ref2.getMessageID(); 
          
          return (id1 < id2 ? -1 : (id1 == id2 ? 0 : 1));
       }      
    }
    
 }
