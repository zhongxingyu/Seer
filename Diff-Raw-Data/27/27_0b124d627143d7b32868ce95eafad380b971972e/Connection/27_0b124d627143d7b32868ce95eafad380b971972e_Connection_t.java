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
 
     public void setConnectionParams(ConnectionParams connectionParams) {
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
        properties.setProperty("mail.smtp.host", connectionParams.getHost());
         try {
            properties.setProperty("mail.smtp.localhost",
                     MimeUtility.encodeWord(connectionParams.getHeloName()));
         } catch (UnsupportedEncodingException e) {
            properties.setProperty("mail.smtp.localhost",
                     connectionParams.getHeloName());
         }
        properties.setProperty("mail.smtp.auth",
                "" + connectionParams.isNeedAuth());
        properties.setProperty("mail.from", connectionParams.getEnvelopeFrom());
        properties.setProperty("mail.smtp.connectiontimeout", ""
                + connectionParams.getConnectTimeout());
        properties.setProperty("mail.smtp.timeout",
                "" + connectionParams.getSocketTimeout());
 
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
