 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package at.outdated.mdbc.connector;
 
 import com.mongodb.DB;
 import java.io.PrintWriter;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Logger;
 import javax.resource.ResourceException;
 import javax.resource.spi.*;
 import javax.security.auth.Subject;
 import javax.transaction.xa.XAResource;
 
 /**
  *
  * @author ebirn
  */
 public class Connection implements ManagedConnection {
 
     private ConnectionFactory mcf;
     @SuppressWarnings("NonConstantLogger")
     private Logger log;
     private PrintWriter out;
     private JCAMongoConnection mongoConnection;
     private ConnectionRequestInfo connectionRequestInfo;
     private List<ConnectionEventListener> listeners;
     
     private DB mongoDB;
      
     Connection(Logger log, ConnectionFactory mcf, ConnectionRequestInfo connectionRequestInfo, DB mongoDB) {
         this.log = log;
         this.mcf = mcf;
         this.connectionRequestInfo = connectionRequestInfo;
         this.listeners = new LinkedList<>();
         this.mongoDB = mongoDB;
     }
     
      
     @Override
     public Object getConnection(Subject subject, ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
         mongoConnection = new JCAMongoConnection(log, this, mcf, connectionRequestInfo);
         return mongoConnection;
     }
 
     @Override
     public void destroy() {
         log.fine("connection shutting down.");
     }
 
     @Override
     public void cleanup() {
        log.fine("#Connection.cleanup");
     }
 
     @Override
     public void associateConnection(Object connection) {
        log.fine("#Connection.associateConnection " + connection);
         this.mongoConnection = (JCAMongoConnection) connection;
     }
 
     @Override
     public void addConnectionEventListener(ConnectionEventListener listener) {
        log.fine("#Connection.addConnectionEventListener");
         this.listeners.add(listener);
     }
 
     @Override
     public void removeConnectionEventListener(ConnectionEventListener listener) {
        log.fine("#Connection.removeConnectionEventListener");
         this.listeners.remove(listener);
     }
 
     @Override
     public XAResource getXAResource() throws ResourceException {
        log.fine("#Connection.getXAResource");
         return null;
     }
 
   
 
     @Override
     public ManagedConnectionMetaData getMetaData() throws ResourceException {
         
        log.fine("#Connection.getMetaData");
         return new ManagedConnectionMetaData() {
 
             public String getEISProductName() throws ResourceException {
                 log.finer("#Connection.getEISProductName");
                 return "Mongo DB Connector JCA";
             }
 
             @Override
             public String getEISProductVersion() throws ResourceException {
                 log.finer("#Connection.getEISProductVersion");
                 return "1.0";
             }
 
             @Override
             public int getMaxConnections() throws ResourceException {
                 log.finer("#Connection.getMaxConnections");
                 return mcf.getMaxConnections();
             }
 
             public String getUserName() throws ResourceException {
                 return null;
             }
         };
     }
 
     @Override
     public void setLogWriter(PrintWriter out)
         throws ResourceException {
         System.out.println("#Connection.setLogWriter");
         this.out = out;
     }
 
     @Override
     public PrintWriter getLogWriter() throws ResourceException {
         System.out.println("#Connection.getLogWriter");
         return out;
     }
     
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Connection other = (Connection) obj;
         if (this.connectionRequestInfo != other.connectionRequestInfo && (this.connectionRequestInfo == null || !this.connectionRequestInfo.equals(other.connectionRequestInfo))) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 83 * hash + (this.connectionRequestInfo != null ? this.connectionRequestInfo.hashCode() : 0);
         return hash;
     }
 
     public ConnectionRequestInfo getConnectionRequestInfo() {
         return connectionRequestInfo;
     }
 
     public void fireConnectionEvent(int event) {
         ConnectionEvent connnectionEvent = new ConnectionEvent(this, event);
         connnectionEvent.setConnectionHandle(this.mongoConnection);
         
         for (ConnectionEventListener listener : this.listeners) {
             switch (event) {
                 case ConnectionEvent.LOCAL_TRANSACTION_STARTED:
                     listener.localTransactionStarted(connnectionEvent);
                     break;
                 case ConnectionEvent.LOCAL_TRANSACTION_COMMITTED:
                     listener.localTransactionCommitted(connnectionEvent);
                     break;
                 case ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK:
                     listener.localTransactionRolledback(connnectionEvent);
                     break;
                 case ConnectionEvent.CONNECTION_CLOSED:
                     listener.connectionClosed(connnectionEvent);
                     break;
                 default:
                     throw new IllegalArgumentException("Unknown event: " + event);
             }
         }
     }
 
     public void close() {
         this.fireConnectionEvent(ConnectionEvent.CONNECTION_CLOSED);
     }
 
     @Override
     public LocalTransaction getLocalTransaction() throws ResourceException {
         throw new UnsupportedOperationException("Not supported yet.");
     }
     
     public DB getDB() {
         return this.mongoDB;
     }
 }
