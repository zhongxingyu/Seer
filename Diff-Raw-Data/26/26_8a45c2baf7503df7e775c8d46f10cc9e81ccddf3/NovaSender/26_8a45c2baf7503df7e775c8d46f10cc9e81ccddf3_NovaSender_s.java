 package novajoy.sender;
 import com.pdfjet.*;
 import com.pdfjet.Font;
 import novajoy.util.config.IniWorker;
 
 import javax.activation.DataHandler;
 import javax.activation.DataSource;
 import javax.mail.*;
 import javax.mail.internet.*;
 import javax.mail.util.ByteArrayDataSource;
 import java.awt.*;
 import java.io.*;
 import java.nio.charset.Charset;
 import java.sql.*;
 import java.util.Properties;
 import java.util.Set;
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
 
     private final String configPath = "/Users/romanfilippov/Dropbox/mydocs/Development/java/novaJoy/novajoy/config/config.ini";
 
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
 
     private String createPDF (String htmlDocument) throws Exception {
 
         FileOutputStream fos = new FileOutputStream("Example_06.pdf");
 
         PDF pdf = new PDF(fos);
 
         InputStream inp = getClass().getResourceAsStream("/Users/romanfilippov/Dropbox/mydocs/Development/java/novaJoy/novajoy/fonts/Core/Times-Roman.afm");
         BufferedInputStream bis1 = new BufferedInputStream(inp);
         Font f1 = new Font(pdf, bis1, CodePage.UNICODE, Embed.YES);
 
         BufferedInputStream bis2 = new BufferedInputStream(
                 getClass().getResourceAsStream(
                         "fonts/Core/Courier.afm"));
         Font f2 = new Font(pdf, bis1, CodePage.UNICODE, Embed.YES);
 
         BufferedInputStream bis3 = new BufferedInputStream(
                 getClass().getResourceAsStream(
                         "fonts/Core/Helvetica.afm"));
         Font f3 = new Font(pdf, bis1, CodePage.UNICODE, Embed.YES);
 
         Font f4 = new Font(pdf, CoreFont.ZAPF_DINGBATS);
 
         Page page = new Page(pdf, Letter.PORTRAIT);
 
         int x_pos = 50;
         int y_pos = 0;
 
         f1.setSize(20);
         f2.setSize(20);
         f3.setSize(20);
         f4.setSize(18);
 
         TextLine text = new TextLine(f1);
         text.setPosition(x_pos, y_pos);
         StringBuilder buf = new StringBuilder();
         for (int i = 32; i <= 256; i++) {
             if (i % 32 == 0) {
                 text.setText(buf.toString());
                 text.setPosition(x_pos, y_pos += 24);
                 text.drawOn(page);
                 buf = new StringBuilder();
             }
             buf.append((char) i);
         }
 
         text.setFont(f2);
         buf = new StringBuilder();
         for (int i = 32; i <= 256; i++) {
             if (i % 32 == 0) {
                 text.setText(buf.toString());
                 text.setPosition(x_pos, y_pos += 24);
                 text.drawOn(page);
                 buf = new StringBuilder();
             }
             buf.append((char) i);
         }
 
         text.setFont(f3);
         buf = new StringBuilder();
         for (int i = 32; i <= 256; i++) {
             if (i == 210 || i == 242) {
                 // Character 210 is not mapped in the 1253 code page
                 // Character 242 - "SIGMA FINAL" is not available in this font
                 continue;
             }
             if (i % 32 == 0) {
                 text.setText(buf.toString());
                 text.setPosition(x_pos, y_pos += 24);
                 text.drawOn(page);
                 buf = new StringBuilder();
             }
             buf.append((char) i);
         }
 
         text.setFont(f4);
         buf = new StringBuilder();
         for (int i = 32; i <= 256; i++) {
             if (i % 32 == 0) {
                 text.setText(buf.toString());
                 text.setPosition(x_pos, y_pos += 22);
                 text.setUnderline( true );
                 text.drawOn(page);
                 buf = new StringBuilder();
             }
             buf.append((char) i);
         }
 
         pdf.flush();
         fos.close();
         return null;
     }
 
     /*
     private String createPDF (String htmlDocument) throws Exception {
 
         if (htmlDocument == null)
             return null;
 
         DocumentCreator<DocumentStream, DocumentTemplateStream> creator = DocBag.newDocumentCreator();
         DocumentStream document = creator.createDocument("templates/template.html");
 
         FileOutputStream fs = new FileOutputStream("sup_doc.pdf");
         fs.write(document.toString().getBytes("UTF-8"));
         fs.close();
 
         return document.toString();
     }*/
 
     /*private String createPDF (String htmlDocument) throws Exception {
 
         PD4ML pd4ml = new PD4ML();
 
         try {
             pd4ml.setPageSize( PD4Constants.A4 );
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         int topValue = 10;
         int leftValue = 10;
         int rightValue = 10;
         int bottomValue = 10;
         String unitsValue = "mm";
         int userSpaceWidth = 780;
 
         if ( unitsValue.equals("mm") ) {
             pd4ml.setPageInsetsMM( new Insets(topValue, leftValue,
                     bottomValue, rightValue) );
         } else {
             pd4ml.setPageInsets( new Insets(topValue, leftValue,
                     bottomValue, rightValue) );
         }
 
         pd4ml.setHtmlWidth( userSpaceWidth );
 
         byte[] input = htmlDocument.getBytes("UTF-8");
         InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(input));
 
         pd4ml.render(reader, new FileOutputStream("my_doc_2.pdf"));
 
 
         //pd4ml.render(htmlDocument, new FileOutputStream("my_doc_2.pdf"));
         return null;
     }
 
     private String createPDF (String htmlDocument){
 
         if (htmlDocument == null)
             return null;
 
         PdfWriter pdfWriter = null;
 
         //create a new document
         Document document = new Document();
         FontFactory.defaultEmbedding = true;
 
         FontFactory.registerDirectory("/Users/romanfilippov/Dropbox/mydocs/Development/java/novaJoy/novajoy/fonts");
 
         Set<String> registeredFonts = FontFactory.getRegisteredFonts();
         log.info("Registered fonts : " + registeredFonts);
 
         try {
 
             ByteArrayOutputStream os = new ByteArrayOutputStream();
             //get Instance of the PDFWriter
             pdfWriter = PdfWriter.getInstance(document, new FileOutputStream("my_doc_aa.pdf"));
 
             //document header attributes
             FontFactory.registerDirectories();
             FontFactory.register("/Users/romanfilippov/Dropbox/mydocs/Development/java/novaJoy/novajoy/fonts");
             document.addAuthor("NovaJoy");
             document.addCreationDate();
             document.addProducer();
             document.addCreator("novajoy.org");
             document.addTitle("Your RSS feed from NovaJoy");
             document.setPageSize(PageSize.LETTER);
 
             //open document
             document.open();
 
             //To convert a HTML file from the filesystem
             //String File_To_Convert = "docs/SamplePDF.html";
             //FileInputStream fis = new FileInputStream(File_To_Convert);
 
             //URL for HTML page
             byte[] input = htmlDocument.getBytes("UTF-8");
             //InputStreamReader fis = new InputStreamReader();
             FileOutputStream fos = new FileOutputStream("res.html");
             fos.write(input);
 
             //get the XMLWorkerHelper Instance
             XMLWorkerHelper worker = XMLWorkerHelper.getInstance();
             //convert to PDF
             worker.parseXHtml(pdfWriter, document, new ByteArrayInputStream(input));
             //worker.parseXHtml(pdfWriter, document, new ByteArrayInputStream(input), XMLWorkerHelper.class.getResourceAsStream("/default.css"), Charset.defaultCharset(), new XMLWorkerFontProvider());
             //close the document
             document.close();
             //close the writer
             pdfWriter.close();
 
             String result = os.toString();
             os.close();
 
             return result;
 
         }
 
         catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } catch (DocumentException e) {
             e.printStackTrace();
         }
         return null;
     } */
 
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
 
     public Message prepareMessage(InternalMessage msg) throws MessagingException,FileNotFoundException,Exception {
 
         //String pdfDoc = createPDF(msg.attachment);
 
         /*FileOutputStream fs = new FileOutputStream(new File("feed.pdf"));
         fs.write(pdfDoc.getBytes("UTF-8"));*/
 
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
     public Message formMessage(String subject, String body, String content, String[] to) throws MessagingException {
 
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
 
         if (content != null) {
             // message attach
             MimeBodyPart attachmentPart = new MimeBodyPart();
 
             try {
                 DataSource ds = new ByteArrayDataSource(content.getBytes("UTF-8"), "application/octet-stream");
                 attachmentPart = new MimeBodyPart();
                 attachmentPart.setDataHandler(new DataHandler(ds));
             } catch (Exception e) {
                 log.warning(e.getMessage());
             }
 
             attachmentPart.setFileName("feed.html");
             multipart.addBodyPart(attachmentPart);
         }
 
         // Put parts in message
         message.setContent(multipart);
 
         return message;
     }
 }
