 package tool.email.sender;
 
 import java.io.UnsupportedEncodingException;
 import java.util.Properties;
 
 import javax.mail.Authenticator;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.URLName;
 import javax.mail.internet.MimeUtility;
 
 /**
  * @author dhf
  */
 public final class Connection {
     private Session session;
 
     private Transport transport;
 
     private ConnectionParams connectionParams;
 
     public Connection(ConnectionParams connectionParams) {
         if (null == connectionParams) {
             throw new IllegalArgumentException("connectionParams is null");
         }
         this.connectionParams = connectionParams;
     }
 
     public ConnectionParams getConnectionParams() {
         return connectionParams;
     }
 
     private Authenticator getAuthenticator() {
         if (connectionParams.isNeedAuth()) {
             return new SimpleAuthenticator(connectionParams.getEnvelopeFrom(),
                     connectionParams.getPassword());
         } else {
             return null;
         }
     }
 
     /**
      * @throws ConnectionException
      */
     public void connect() throws ConnectionException {
         close();
         Properties properties = new Properties();
        String protocol = connectionParams.getProtocol();
        properties.setProperty("mail." + protocol + ".host",
                connectionParams.getHost());
         if (null != connectionParams.getHeloName()) {
             try {
                properties.setProperty("mail." + protocol + ".localhost",
                         MimeUtility.encodeWord(connectionParams.getHeloName()));
             } catch (UnsupportedEncodingException e) {
                properties.setProperty("mail." + protocol + ".localhost",
                         connectionParams.getHeloName());
             }
         }
        properties.setProperty("mail." + protocol + ".auth", ""
                + connectionParams.isNeedAuth());
         properties.setProperty("mail.from", connectionParams.getEnvelopeFrom());
        properties.setProperty("mail." + protocol + ".connectiontimeout", ""
                 + connectionParams.getConnectTimeout());
        properties.setProperty("mail." + protocol + ".timeout", ""
                + connectionParams.getSocketTimeout());
 
         session = Session.getInstance(properties, getAuthenticator());
         session.setDebug(connectionParams.isDebug());
         if (connectionParams.isDebug()
                 && null != connectionParams.getDebugOut()) {
             session.setDebugOut(connectionParams.getDebugOut());
         }
         URLName urlName = new URLName(connectionParams.getProtocol(),
                 connectionParams.getHost(), connectionParams.getPort(), null,
                 connectionParams.getEnvelopeFrom(),
                 connectionParams.getPassword());
         try {
             transport = session.getTransport(urlName);
             transport.connect();
         } catch (MessagingException e) {
             close();
             throw new ConnectionException(e);
         }
     }
 
     public boolean isConnected() {
         return (null != transport && transport.isConnected());
     }
 
     /**
      * 关闭连接
      */
     public void close() {
         try {
             if (isConnected()) {
                 transport.close();
             }
         } catch (MessagingException e) {
 
         } finally {
             transport = null;
             session = null;
         }
 
     }
 
     public Session getSession() {
         return session;
     }
 
     public Transport getTransport() {
         return transport;
     }
 }
