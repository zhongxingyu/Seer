 package novajoy.sender;
 import novajoy.util.config.IniWorker;
 import novajoy.util.logger.Loggers;
 import javax.activation.DataHandler;
 import javax.activation.DataSource;
 import javax.activation.FileDataSource;
 import javax.mail.*;
 import javax.mail.internet.*;
 import java.io.*;
 import java.sql.*;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: romanfilippov
  * Date: 23.03.13
  * Time: 21:00
  */
 public class NovaSender {
 
     // database config
 
     private String hostName = "";
     private final String className = "com.mysql.jdbc.Driver";
     private String dbName = "";
     private String userName = "";
     private String userPassword = "";
     private static Logger log =  new Loggers().getSenderLogger();
 
     // E-mail  configuration
 
     private Properties props = null;
     private Session session = null;
     private String host = "";
     private String from = "";
     private String pass = "";
     private String smtpPort = "";
 
     private static String SMTP_USERNAME = "";
     private static String SMTP_PASSWORD = "";
 
     InternalMessage[] collection = null;
 
     private final String configPath = "/home/ubuntu/NovaJoyConfig/config.ini";
 
     Connection con = null;
 
     //private static String DEFAULT_SUBJECT = "your rss feed from novaJoy";
 
     public void InitConfiguration(IniWorker worker) {
 
         hostName = worker.getDBaddress();
         dbName = worker.getDBbasename();
         userName = worker.getDBuser();
         userPassword = worker.getDBpassword();
         host = worker.getSenderSmtpHost();
         smtpPort = worker.getSenderSmtpPort();
         from = worker.getSenderFromMail();
         pass = worker.getSenderFromPass();
         SMTP_USERNAME = worker.getSenderSmtpUser();
         SMTP_PASSWORD = worker.getSenderSmtpPass();
     }
 
     public NovaSender() {
 
         try {
 
             IniWorker config = new IniWorker(configPath);
             InitConfiguration(config);
 
             log.info("Establishing a connection...");
             String url = "jdbc:mysql://" + hostName + "/" + dbName;
             Class.forName(className);
             con = DriverManager.getConnection(url, userName, userPassword);
             log.info("Connection established");
 
             log.info("Setting mail properties");
 
             props = System.getProperties();
             props.put("mail.smtp.starttls.enable", "true");
             props.put("mail.smtp.starttls.required", "true");
             props.put("mail.smtp.host", host);
             props.put("mail.smtp.user", from);
             props.put("mail.smtp.password", pass);
             props.put("mail.smtp.port", smtpPort);
             props.put("mail.smtp.auth", "true");
             props.put("mail.transport.protocol", "smtp");
 
             session = Session.getDefaultInstance(props, null);
 
             log.info("Init completed.");
 
         } catch (ClassNotFoundException e) {
             log.warning("Class not found" + e.getMessage());
         } catch (SQLException ex) {
             log.warning(ex.getMessage());
         } catch (Exception exc) {
             log.warning(exc.getMessage());
         }
     }
 
     public void performSend(Message message) throws MessagingException {
 
         Transport transport = session.getTransport("smtp");
         transport.connect(host, SMTP_USERNAME, SMTP_PASSWORD);
         transport.sendMessage(message, message.getAllRecipients());
         transport.close();
         log.info("Message sent");
     }
 
     InternalMessage[] getMessages() throws SQLException {
 
         Statement st = con.createStatement();
         ResultSet rs = st.executeQuery("Select id,target,title,body,attachment from Server_postletters");
 
         int rowcount = 0;
         if (rs.last()) {
             rowcount = rs.getRow();
             rs.beforeFirst(); // not rs.first() because the rs.next() below will move on, missing the first element
         } else {
             return null;
         }
 
         InternalMessage[] messages = new InternalMessage[rowcount];
 
         int i = 0;
         while (rs.next()) {
             messages[i] = new InternalMessage(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5));
             i++;
         }
 
         return messages;
     }
 
     public Message prepareMessage(InternalMessage msg) throws Exception {
 
         //"<html><head><style type='text/css'>body{font-family:PT Sans}</style><title>FAAAAA</title></head><body><h1>косяпорович</h1></body></html>"
         //String pdfDoc = createPDF(document);
 
         return formMessage(msg.title, msg.body, msg.attachment, msg.target.split(","));
     }
 
     public void cleanDataBase(InternalMessage[] messages) throws SQLException {
 
         log.info("Starting clean");
 
         String query = "delete from Server_postletters where id in ";
 
         query += "(";
         query += new String(new char[messages.length-1]).replace("\0", "?,");
         query += "?);";
 
         PreparedStatement ps = con.prepareStatement(query);
 
         for (int i = 0; i < messages.length; i++) {
 
             ps.setInt(i+1,messages[i].id);
             if (messages[i].attachment == null)
                 continue;
 
             File file = new File(messages[i].attachment);
             if (file.exists())
                 file.delete();
 
            File pdfFile = new File(messages[i].attachment.replace(".html",".pdf"));
             if (pdfFile.exists())
                 pdfFile.delete();
 
             File epubFile = new File(messages[i].attachment.replace(".html",".epub"));
             if (epubFile.exists())
                epubFile.delete();
         }
 
         int rs = ps.executeUpdate();
         if (rs > 0) {
             log.info("Clean finished");
         } else {
             log.warning("Something went wrong while deleting");
         }
     }
 
     public void performRoutineTasks() {
 
         log.info("Starting routines");
 
         try {
 
             // get messages from database queue
             collection = getMessages();
 
             for (int i = 0; i < collection.length; i++) {
 
                 Message message =  prepareMessage(collection[i]);
                 performSend(message);
             }
 
             cleanDataBase(collection);
 
         } catch (SQLException e) {
             log.warning(e.getMessage());
         } catch (MessagingException e) {
             log.warning(e.getMessage());
         } catch (NullPointerException e) {
             log.info("No mails in queue");
         } catch (Exception e) {
             log.warning(e.getMessage());
         }
 
         log.info("Routines finished");
     }
 
     /**
      * Returns mail message with attachment
      *
      * @return  {@code Message} instance
      *
      */
     public Message formMessage(String subject, String body, String pathToContent, String[] to) throws MessagingException {
 
        String format = pathToContent.substring(pathToContent.length()-3);
 
         MimeMessage message = new MimeMessage(session);
         message.setFrom(new InternetAddress(from));
 
         InternetAddress[] toAddress = new InternetAddress[to.length];
 
         // To get the array of addresses
         for( int i=0; i < to.length; i++ ) {
             toAddress[i] = new InternetAddress(to[i]);
         }
 
         //add recipients
         for( int i=0; i < toAddress.length; i++) {
             message.addRecipient(Message.RecipientType.TO, toAddress[i]);
         }
         message.setSubject(subject);
 
         Multipart multipart = new MimeMultipart();
 
         // message body
         MimeBodyPart bodyPart = new MimeBodyPart();
         bodyPart.setText(body);
 
         multipart.addBodyPart(bodyPart);
 
         if (pathToContent != null) {
 
             File attach = new File(pathToContent);
 
             MimeBodyPart attachmentPart = null;
 
             if (attach.exists()) {
                 // message attach
                 attachmentPart = new MimeBodyPart();
 
                 try {
                     DataSource ds = new FileDataSource(pathToContent);
                     attachmentPart = new MimeBodyPart();
                     attachmentPart.setDataHandler(new DataHandler(ds));
                 } catch (Exception e) {
                     log.warning(e.getMessage());
                 }
 
                 attachmentPart.setFileName("feed." + format);
             }
 
             multipart.addBodyPart(attachmentPart);
         }
 
         // Put parts in message
         message.setContent(multipart);
 
         return message;
     }
 }
