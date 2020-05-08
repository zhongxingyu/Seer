 /**
  * Created with IntelliJ IDEA.
  * User: romanfilippov
  * Date: 09.03.13
  * Time: 23:02
  */
 package novajoy.packer;
 import java.io.*;
 import java.lang.String;
 import java.sql.*;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.ListIterator;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.CRC32;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import com.lowagie.text.pdf.BaseFont;
 import novajoy.util.config.IniWorker;
 import novajoy.util.logger.Loggers;
 import org.w3c.tidy.Tidy;
 import org.xhtmlrenderer.pdf.ITextRenderer;
 
 
 class Packer{
 
     private String hostName = "";
     private final String className = "com.mysql.jdbc.Driver";
     private final String encProperties = "?useUnicode=true&characterEncoding=utf-8";
     private String dbName = "";
     private String userName = "";
     private String userPassword = "";
 
     private final String configPath = "/home/ubuntu/NovaJoyConfig/config.ini";
     private static Logger log =  Logger.getLogger(Packer.class.getName());//new Loggers().getPackerLogger();
     private final String mailStoragePath = "mail_storage";
 
     private static Tidy tidy = null;
 
     private final String DEFAULT_SUBJECT = "Your rss feed from novaJoy";
     private final String DEFAULT_BODY = "Thank you for using our service!";
 
     private LinkedList<Thread> tasks = null;
     //Collection<Future<?>> tasks = null;
     //ExecutorService es = null;
 
     Connection con = null;
 
     private UserItem[] users = null;
 
     public void InitConfiguration(IniWorker worker) {
 
         hostName = worker.getDBaddress();
         dbName = worker.getDBbasename();
         userName = worker.getDBuser();
         userPassword = worker.getDBpassword();
         tasks = new LinkedList<Thread>();
         //es = Executors.newCachedThreadPool();
         //tasks = new LinkedList<Future<?>>();
     }
 
     public Packer() {
 
         try {
 
             IniWorker config = new IniWorker(configPath);
             InitConfiguration(config);
 
             log.info("Establishing a connection...");
             String url = "jdbc:mysql://" + hostName + "/" + dbName;
             Class.forName(className);
             con = DriverManager.getConnection(url + encProperties, userName, userPassword);
             log.info("Connection established");
 
         } catch (ClassNotFoundException e) {
             log.warning("Class not found" + e.getMessage());
         } catch (SQLException ex) {
             log.warning(ex.getMessage());
         } catch (Exception exc) {
             log.warning(exc.getMessage());
         }
     }
 
     private static Tidy getTidy() {
         if (null == tidy) {
             tidy = new Tidy();
             tidy.setQuiet(true);
             tidy.setShowErrors(0);
             tidy.setShowWarnings(false);
             tidy.setXHTML(true);
             tidy.setOutputEncoding("UTF-8");
             tidy.setInputEncoding("UTF-8");
             tidy.setAsciiChars(false);
             tidy.setMakeClean(true);
             tidy.setEscapeCdata(false);
             tidy.setFixComments(false);
             tidy.setFixUri(false);
             tidy.setLiteralAttribs(true);
             tidy.setXmlOut(true);
         }
         return tidy;
     }
 
     UserItem[] getUsersIds() throws SQLException {
 
         Statement st = con.createStatement();
         ResultSet rs = st.executeQuery("select id,email from auth_user where id in (select distinct user_id from Server_collection where UNIX_TIMESTAMP(last_update_time)+delta_sending_time<UNIX_TIMESTAMP());");
 
         int rowcount = 0;
         if (rs.last()) {
             rowcount = rs.getRow();
             rs.beforeFirst(); // not rs.first() because the rs.next() below will move on, missing the first element
         } else {
             return null;
         }
 
         UserItem[] usersIds = new UserItem[rowcount];
 
         int i = 0;
         while (rs.next()) {
             usersIds[i] = new UserItem(rs.getInt(1), rs.getString(2));
             i++;
         }
 
         rs.close();
         st.close();
 
         return usersIds;
     }
 
     ArrayList getDataForUserId(int uid) throws SQLException {
 
         Statement st = con.createStatement();
         //System.out.println("select * from Server_rssitem where rssfeed_id in (select rssfeed_id from Server_rssfeed_collection where collection_id in (select id from Server_collection where user_id = " + uid +"));");
         //ResultSet rs = st.executeQuery("select * from Server_rssitem where rssfeed_id in (select rssfeed_id from Server_rssfeed_collection where collection_id in (select id from Server_collection where user_id = " + uid +"));");
         ResultSet rs = st.executeQuery("SELECT IT.rssfeed_id , IT.title, IT.description, IT.link, IT.author, IT.pubDate, COL.id, COL.format, COL.subject  \n" +
                 "FROM Server_rssfeed RS \n" +
                 "JOIN Server_rssfeed_collection CONN \n" +
                 "ON RS.id=CONN.rssfeed_id \n" +
                 "JOIN Server_collection COL \n" +
                 "ON COL.id=CONN.collection_id \n" +
                 "JOIN Server_rssitem IT \n" +
                 "ON IT.rssfeed_id=RS.id\n" +
                 "WHERE COL.user_id = " + uid + " AND IT.pubDate >= COL.last_update_time AND (UNIX_TIMESTAMP(COL.last_update_time)+COL.delta_sending_time<UNIX_TIMESTAMP()) ORDER BY COL.id, IT.pubDate;");
 
         int rowcount = 0;
         if (rs.last()) {
             rowcount = rs.getRow();
             rs.beforeFirst(); // not rs.first() because the rs.next() below will move on, missing the first element
         } else {
             return null;
         }
 
         //RssItem[] items = new RssItem[rowcount];
         ArrayList items = new ArrayList();
 
         int last_group_id = 0;
         int i = 0;
 
         if (rs.next()) {
             last_group_id = rs.getInt(7);
 
             items.add(i, new ItemCollection(rs.getString(9),rs.getString(8)));
             ((ItemCollection)items.get(i)).insertItem(new RssItem(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getDate(6)));
         }
 
         while (rs.next()) {
 
             if (rs.getInt(7) == last_group_id) {
 
                 ItemCollection rss = (ItemCollection)items.get(i);
                 rss.insertItem(new RssItem(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getDate(6)));
             } else {
 
                 last_group_id = rs.getInt(7);
                 i++;
                 items.add(i, new ItemCollection(rs.getString(9),rs.getString(8)));
                 ((ItemCollection)items.get(i)).insertItem(new RssItem(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getDate(6)));
             }
         }
 
         rs.close();
         st.close();
 
         return items;
     }
 
 
     DocumentItem formDocument (String target, ItemCollection userFeeds) {
 
         StringBuilder builder =
                 new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><html><head>" +
                         "<style type='text/css'>body{font-family:PT Sans}</style>" +
                         "<title>Your RSS feed from novaJoy</title></head><body>");
 
         for (int i = 0; i < userFeeds.size(); i++) {
             builder.append(((RssItem)userFeeds.get(i)).toHtml().replaceAll("</hr>","<hr/>"));
         }
 
         builder.append("</body></html>");
 
         return new DocumentItem(target, builder.toString().trim(), userFeeds.subject, userFeeds.format);
     }
 
     void updateFeedTime(UserItem[] users) throws SQLException {
 
         log.info("Start updating feed updating times");
 
         String query = "update Server_collection set last_update_time=FROM_UNIXTIME(UNIX_TIMESTAMP()) where user_id in ";
 
         query += "(";
         query += new String(new char[users.length-1]).replace("\0", "?,");
         query += "?);";
 
         PreparedStatement ps = con.prepareStatement(query);
 
         for (int i = 0; i < users.length; i++) {
 
             ps.setInt(i+1,users[i].user_id);
         }
 
         int rs = ps.executeUpdate();
 
         users = null;
 
         if (rs > 0) {
             log.info("Updating tasks completed");
         } else {
             log.warning("Something went wrong while updating");
         }
     }
 
     LinkedList getPackagedData() throws SQLException {
 
         users = getUsersIds();
         LinkedList usersDocuments = new LinkedList();
 
         int j=0;
 
         for (; ; ) {
 
             if (j >= users.length)
                 break;
 
             ArrayList userFeed = getDataForUserId(users[j].user_id);
 
             if (userFeed == null) {
                 j++;
                 continue;
             }
 
             for (Object elem : userFeed) {
 
                 usersDocuments.add(formDocument(users[j].user_email, (ItemCollection)elem));
             }
 
             j++;
         }
 
         return usersDocuments;
     }
 
     public String saveAttachmentToPath(String attachment, String path) throws FileNotFoundException, IOException {
 
         File file = new File(path);
         file.mkdirs();
 
         int i=0;
 
         while (true) {
 
             if (!(new File(path + "/feed" + i + ".html")).exists())
                 break;
 
             i++;
         }
 
         FileOutputStream os = new FileOutputStream(file.toString() + "/feed" + i + ".html");
 
         try {
 
             os.write(attachment.getBytes("UTF-8"));
 
         } catch (Exception e) {
             log.warning(e.getMessage());
         } finally {
             os.close();
             return "/feed" + i + ".html";
         }
     }
 
     private long calculateCrc(byte[] data) {
         CRC32 crc = new CRC32();
         crc.update(data);
         return crc.getValue();
     }
 
     private void createMime(ZipOutputStream zos) throws IOException {
 
         ZipEntry mimetypeZipEntry = new ZipEntry("mimetype");
         mimetypeZipEntry.setMethod(ZipEntry.STORED);
 
         byte[] mimetypeBytes = "application/epub+zip".getBytes();
         mimetypeZipEntry.setSize(mimetypeBytes.length);
         mimetypeZipEntry.setCrc(calculateCrc(mimetypeBytes));
 
         zos.putNextEntry(mimetypeZipEntry);
         zos.write(mimetypeBytes);
     }
 
     private void createContainer(ZipOutputStream zos) throws IOException {
 
         zos.putNextEntry(new ZipEntry("META-INF/container.xml"));
         Writer out = new OutputStreamWriter(zos);
         out.write("<?xml version=\"1.0\"?>\n");
         out.write("<container version=\"1.0\" xmlns=\"urn:oasis:names:tc:opendocument:xmlns:container\">\n");
         out.write("\t<rootfiles>\n");
         out.write("\t\t<rootfile full-path=\"OEBPS/content.opf\" media-type=\"application/oebps-package+xml\"/>\n");
         out.write("\t</rootfiles>\n");
         out.write("</container>");
         out.flush();
     }
 
     private void createPackageDocument(ZipOutputStream zos) throws IOException {
 
         zos.putNextEntry(new ZipEntry("OEBPS/content.opf"));
         Writer out = new OutputStreamWriter(zos);
 
         out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                 "<package xmlns=\"http://www.idpf.org/2007/opf\" unique-identifier=\"BookID\" version=\"2.0\">\n" +
                 "    <metadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:opf=\"http://www.idpf.org/2007/opf\">\n" +
                 "        <dc:title>NovaJoy RSS Feed</dc:title>\n" +
                 "\t<dc:language>en</dc:language>\n" +
                 "        <dc:rights>novajoy.org</dc:rights>\n" +
                 "        <dc:creator opf:role=\"aut\">novajoy</dc:creator>\n" +
                 "        <dc:publisher>novajoy.org</dc:publisher>\n" +
                 "        <dc:identifier id=\"BookID\" opf:scheme=\"UUID\">015ffaec-9340-42f8-b163-a0c5ab7d0611</dc:identifier>\n" +
                 "        <meta name=\"Sigil version\" content=\"0.2.4\"/>\n" +
                 "    </metadata>\n" +
                 "    <manifest>\n" +
                 "        <item id=\"ncx\" href=\"toc.ncx\" media-type=\"application/x-dtbncx+xml\"/>\n" +
                 "        <item id=\"page-template.xpgt\" href=\"Styles/page-template.xpgt\" media-type=\"application/vnd.adobe-page-template+xml\"/>\n" +
                 "        <item id=\"stylesheet.css\" href=\"Styles/stylesheet.css\" media-type=\"text/css\"/>\n" +
                 "        <item id=\"news.xhtml\" href=\"Text/news.xhtml\" media-type=\"application/xhtml+xml\"/>\n" +
                 "        <item id=\"title_page.xhtml\" href=\"Text/title_page.xhtml\" media-type=\"application/xhtml+xml\"/>\n" +
                 "    </manifest>\n" +
                 "    <spine toc=\"ncx\">\n" +
                 "        <itemref idref=\"title_page.xhtml\"/>\n" +
                 "        <itemref idref=\"news.xhtml\"/>\n" +
                 "    </spine>\n" +
                 "</package>\n");
         out.flush();
     }
 
     private void createBody(String body, ZipOutputStream zos) throws IOException{
 
         zos.putNextEntry(new ZipEntry("OEBPS/Text/title_page.xhtml"));
         Writer out = new OutputStreamWriter(zos);
         out.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                 "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"\n" +
                 "  \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n" +
                 "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n" +
                 "<head>\n" +
                 "  <title>Title Page</title>\n" +
                 "  <link rel=\"stylesheet\" href=\"../Styles/stylesheet.css\" type=\"text/css\" />\n" +
                 "  <link rel=\"stylesheet\" type=\"application/vnd.adobe-page-template+xml\" href=\"../Styles/page-template.xpgt\" />\n" +
                 "</head>\n" +
                 "<body>\n" +
                 "  <div>\n" +
                 "    <p>&nbsp;</p>\n" +
                 "    <p>&nbsp;</p>\n" +
                 "    <h2 id=\"heading_id_2\">Rss news summary</h2>\n" +
                 "    <h2 id=\"heading_id_3\">NovaJoy.org</h2>\n" +
                 "  </div>\n" +
                 "</body>\n" +
                 "</html>");
 
         out.flush();
 
         zos.putNextEntry(new ZipEntry("OEBPS/Text/news.xhtml"));
         zos.write(body.getBytes("UTF-8"));
 
     }
 
     private void createStyles(ZipOutputStream zos) throws IOException{
 
         zos.putNextEntry(new ZipEntry("OEBPS/Styles/stylesheet.css"));
 
         Writer out = new OutputStreamWriter(zos);
         out.write("/* Style Sheet */\n" +
                 "/* This defines styles and classes used in the book */\n" +
                 "body { margin-left: 5%; margin-right: 5%; margin-top: 5%; margin-bottom: 5%; text-align: justify; }\n" +
                 "pre { font-size: x-small; }\n" +
                 "h1 { text-align: center; }\n" +
                 "h2 { text-align: center; }\n" +
                 "h3 { text-align: center; }\n" +
                 "h4 { text-align: center; }\n" +
                 "h5 { text-align: center; }\n" +
                 "h6 { text-align: center; }\n" +
                 ".CI {\n" +
                 "    text-align:center;\n" +
                 "    margin-top:0px;\n" +
                 "    margin-bottom:0px;\n" +
                 "    padding:0px;\n" +
                 "    }\n" +
                 ".center   {text-align: center;}\n" +
                 ".smcap    {font-variant: small-caps;}\n" +
                 ".u        {text-decoration: underline;}\n" +
                 ".bold     {font-weight: bold;}\n");
 
         zos.putNextEntry(new ZipEntry("OEBPS/Styles/page-template.xpgt"));
         out.write("<ade:template xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:ade=\"http://ns.adobe.com/2006/ade\"\n" +
                 "\t\t xmlns:fo=\"http://www.w3.org/1999/XSL/Format\">\n" +
                 "\n" +
                 "  <fo:layout-master-set>\n" +
                 "   <fo:simple-page-master master-name=\"single_column\">\n" +
                 "\t\t<fo:region-body margin-bottom=\"3pt\" margin-top=\"0.5em\" margin-left=\"3pt\" margin-right=\"3pt\"/>\n" +
                 "    </fo:simple-page-master>\n" +
                 "  \n" +
                 "    <fo:simple-page-master master-name=\"single_column_head\">\n" +
                 "\t\t<fo:region-before extent=\"8.3em\"/>\n" +
                 "\t\t<fo:region-body margin-bottom=\"3pt\" margin-top=\"6em\" margin-left=\"3pt\" margin-right=\"3pt\"/>\n" +
                 "    </fo:simple-page-master>\n" +
                 "\n" +
                 "    <fo:simple-page-master master-name=\"two_column\"\tmargin-bottom=\"0.5em\" margin-top=\"0.5em\" margin-left=\"0.5em\" margin-right=\"0.5em\">\n" +
                 "\t\t<fo:region-body column-count=\"2\" column-gap=\"10pt\"/>\n" +
                 "    </fo:simple-page-master>\n" +
                 "\n" +
                 "    <fo:simple-page-master master-name=\"two_column_head\" margin-bottom=\"0.5em\" margin-left=\"0.5em\" margin-right=\"0.5em\">\n" +
                 "\t\t<fo:region-before extent=\"8.3em\"/>\n" +
                 "\t\t<fo:region-body column-count=\"2\" margin-top=\"6em\" column-gap=\"10pt\"/>\n" +
                 "    </fo:simple-page-master>\n" +
                 "\n" +
                 "    <fo:simple-page-master master-name=\"three_column\" margin-bottom=\"0.5em\" margin-top=\"0.5em\" margin-left=\"0.5em\" margin-right=\"0.5em\">\n" +
                 "\t\t<fo:region-body column-count=\"3\" column-gap=\"10pt\"/>\n" +
                 "    </fo:simple-page-master>\n" +
                 "\n" +
                 "    <fo:simple-page-master master-name=\"three_column_head\" margin-bottom=\"0.5em\" margin-top=\"0.5em\" margin-left=\"0.5em\" margin-right=\"0.5em\">\n" +
                 "\t\t<fo:region-before extent=\"8.3em\"/>\n" +
                 "\t\t<fo:region-body column-count=\"3\" margin-top=\"6em\" column-gap=\"10pt\"/>\n" +
                 "    </fo:simple-page-master>\n" +
                 "\n" +
                 "    <fo:page-sequence-master>\n" +
                 "        <fo:repeatable-page-master-alternatives>\n" +
                 "            <fo:conditional-page-master-reference master-reference=\"three_column_head\" page-position=\"first\" ade:min-page-width=\"80em\"/>\n" +
                 "            <fo:conditional-page-master-reference master-reference=\"three_column\" ade:min-page-width=\"80em\"/>\n" +
                 "            <fo:conditional-page-master-reference master-reference=\"two_column_head\" page-position=\"first\" ade:min-page-width=\"50em\"/>\n" +
                 "            <fo:conditional-page-master-reference master-reference=\"two_column\" ade:min-page-width=\"50em\"/>\n" +
                 "            <fo:conditional-page-master-reference master-reference=\"single_column_head\" page-position=\"first\" />\n" +
                 "            <fo:conditional-page-master-reference master-reference=\"single_column\"/>\n" +
                 "        </fo:repeatable-page-master-alternatives>\n" +
                 "    </fo:page-sequence-master>\n" +
                 "\n" +
                 "  </fo:layout-master-set>\n" +
                 "\n" +
                 "  <ade:style>\n" +
                 "    <ade:styling-rule selector=\".title_box\" display=\"adobe-other-region\" adobe-region=\"xsl-region-before\"/>\n" +
                 "  </ade:style>\n" +
                 "\n" +
                 "</ade:template>");
 
         out.flush();
     }
 
     private void createEpub (String htmlDocument, String path) throws FileNotFoundException, IOException {
 
         ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(path));
 
         createMime(zos);
         createContainer(zos);
         createPackageDocument(zos);
         createStyles(zos);
         createBody(htmlDocument, zos);
 
         zos.close();
     }
 
     private String cleanHTML(String doc) throws IOException {
 
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         getTidy().parse(new ByteArrayInputStream(doc.getBytes("UTF-8")), os);
 
         String result = os.toString("UTF-8");
 
         String cleanXMLString = null;
         Pattern pattern = null;
         Matcher matcher = null;
         pattern = Pattern.compile("[\\000]*");
         matcher = pattern.matcher(result);
         if (matcher.find()) {
             cleanXMLString = matcher.replaceAll("");
         }
 
 
         os.close();
         return cleanXMLString;
     }
 
     public String prepareAttachmentAndSave(String email, String attachment, String format) throws IOException {
 
         String domain = email.substring(email.indexOf("@")+1);
         String name = email.substring(0, email.indexOf("@"));
         System.out.println("Processing: " + domain + "|" + name);
         String path = mailStoragePath + "/" + domain + "/" + name;
 
         String validXHTML = cleanHTML(attachment);
         String fileName = saveAttachmentToPath(validXHTML, path);
         String resultPath = path + fileName;
 
         /*File source=new File(resultPath);
         File destination=new File(epubTextStoragePath + "/chap01.xhtml");
 
         copyFile(source,destination);
 
         Archiver.createZip(mailStoragePath + "/epub", resultPath.replace(".html",".epub"));  */
 
         if (format.equalsIgnoreCase("EPUB")) {
             createEpub(validXHTML, resultPath.replace(".html", ".epub"));
             return resultPath.replace(".html", ".epub");
         } else if (format.equalsIgnoreCase("PDF")) {
             //tasks.add(es.submit(new PdfTask(validXHTML, resultPath.replace(".html", ".pdf"))));
             PdfTask pdftask = new PdfTask(validXHTML, resultPath.replace(".html", ".pdf"));
             tasks.add(pdftask);
             pdftask.start();
             return resultPath.replace(".html", ".pdf");
         } else
             return resultPath;
     }
 
     /*public static void copyFile(File sourceFile, File destFile) throws IOException {
 
         if(!destFile.exists()) {
             destFile.createNewFile();
         }
 
         FileChannel source = null;
         FileChannel destination = null;
         try {
             source = new RandomAccessFile(sourceFile,"rw").getChannel();
             destination = new RandomAccessFile(destFile,"rw").getChannel();
 
             long position = 0;
             long count    = source.size();
 
             source.transferTo(position, count, destination);
         }
         finally {
             if(source != null) {
                 source.close();
             }
             if(destination != null) {
                 destination.close();
             }
         }
     }    */
 
     public void performRoutineTasks() {
 
         log.info("Starting routine tasks");
         try {
 
             LinkedList docs = getPackagedData();
 
             if (docs.isEmpty())
                 throw new NullPointerException();
 
             //es.shutdown();
 
             while (!tasks.isEmpty()) {
 
                 tasks.getFirst().join();
                 tasks.removeFirst();
             }
 
             log.info("Doc generating tasks completed. Mails create process starting");
 
             /*for (Future<?> future : tasks) {     // wait all
 
                 future.get();
             }*/
 
             String query = "insert into Server_postletters (target,title,body,attachment) values ";
 
             ListIterator iterator = docs.listIterator();
 
             while (iterator.hasNext()) {
 
                 /*query += "('" + docs[i].target_email + "','" +
                         DEFAULT_SUBJECT + "','" + DEFAULT_BODY + "','" +
                         docs[i].user_document + "')" + (i == docs.length-1 ? ";" : ",");*/
                 query += "(?,?,?,?),";
                 iterator.next();
             }
 
             query = query.substring(0,query.length()-1) + ";";
 
             PreparedStatement ps = con.prepareStatement(query);
 
             int j = 1;
             iterator = docs.listIterator();
             while (iterator.hasNext()) {
 
                 DocumentItem item = (DocumentItem)iterator.next();
 
                 ps.setString(j++, item.target_email);
                 ps.setString(j++, item.subject);
                 ps.setString(j++, DEFAULT_BODY);
 
                 String filePath = prepareAttachmentAndSave(item.target_email, item.user_document, item.format);
 
                 ps.setString(j++, filePath);
             }
 
             int rs = ps.executeUpdate();
 
             if (users != null)
                 updateFeedTime(users);
 
            tasks = null;
 
             if (rs > 0) {
                 log.info("Routine tasks completed");
             } else {
                 log.warning("Something went wrong while inserting");
             }
 
             users = null;
             ps.close();
 
         } catch (NullPointerException e) {
             log.info("There are no documents for update");
             return;
         } catch (Exception e) {
             log.warning(e.getMessage());
         }
     }
 
     /**
      * Returns transformed String which consists result of XSLT transformation
      *
      * @return  {@code String} representing transformed document
      *          (which may be {@code null}).
      */
     public String performXSLT(String source, String stylesheet) {
 
         try {
             StringReader reader = new StringReader(source);
             StringWriter writer = new StringWriter();
             TransformerFactory tFactory = TransformerFactory.newInstance();
             Transformer transformer = tFactory.newTransformer(
                     new javax.xml.transform.stream.StreamSource(stylesheet));
 
             transformer.transform(
                     new javax.xml.transform.stream.StreamSource(reader),
                     new javax.xml.transform.stream.StreamResult(writer));
 
             String s = writer.toString();
             return s;
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
     }
 }
 
 
 
