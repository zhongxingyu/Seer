 package com.maximdim.gmailbackup;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import javax.mail.Address;
 import javax.mail.FetchProfile;
 import javax.mail.Folder;
 import javax.mail.Message;
 import javax.mail.MessageRemovedException;
 import javax.mail.MessagingException;
 import javax.mail.search.AndTerm;
 import javax.mail.search.ComparisonTerm;
 import javax.mail.search.ReceivedDateTerm;
 import javax.mail.search.SearchTerm;
 
 import org.apache.commons.codec.digest.DigestUtils;
 
 import com.google.code.samples.oauth2.OAuth2Authenticator;
 import com.sun.mail.imap.IMAPFolder;
 import com.sun.mail.imap.IMAPStore;
 import com.sun.mail.util.FolderClosedIOException;
 import com.sun.mail.util.MessageRemovedIOException;
 
 public class GmailBackup {
   private static final String USER_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
   private final String serviceAccountId;
   private final File serviceAccountPkFile;
   private final String domain;
   private final File timestampFile;
   // <User, Date>
   private final Map<String, Date> userTimestamps;
 
   private final List<String> users;
   private final List<String> ignoreFrom;
   private int maxPerRun;
   private boolean zip;
   
   // storage format:
   // dataDir/domain/year/month/day/user_timestamp.mail
   private final File dataDir;
 
   public GmailBackup(Properties p) {
     this.serviceAccountId = p.getProperty("serviceAccountId");
     this.serviceAccountPkFile = new File(p.getProperty("serviceAccountPkFile"));
     this.domain = p.getProperty("domain");
     this.timestampFile = new File(p.getProperty("timestampFile"));
     this.users = Arrays.asList(p.getProperty("users").split(","));
     this.ignoreFrom = Arrays.asList(p.getProperty("ignoreFrom").split(","));
     this.maxPerRun = Integer.parseInt(p.getProperty("maxPerRun", "10000"));
     this.zip = Boolean.parseBoolean(p.getProperty("zip"));
     
     Date oldestDate = getDate(p.getProperty("oldestDate", "2012/01/01"), "yyyy-MM-dd");
     this.userTimestamps = loadTimestamp(this.timestampFile, oldestDate);
     
     this.dataDir = new File(p.getProperty("dataDir"));
   }
 
   private void backup() throws Exception {
     OAuth2Authenticator.initialize();
 
     for(String user: this.users) {
       try {
         System.out.println("Backing up ["+user+"]");
         String email = user + "@" + this.domain;
         IMAPStore store = getStore(email);
         
         UserMessagesIterator iterator = new UserMessagesIterator(store, this.userTimestamps.get(user), this.ignoreFrom, this.maxPerRun);
         int count = 0;
         while(iterator.hasNext() && count < this.maxPerRun) {
           try {
             Message message = iterator.next();
             File f = saveMessage(user, message);
             // update stats
             this.userTimestamps.put(user, message.getReceivedDate());
             System.out.println(iterator.getStats()+" "+f.getAbsolutePath());
             count++;
             if (count % 100 == 0) {
               saveTimestamp(this.userTimestamps, this.timestampFile);
             }
           }
           catch (MessageRemovedIOException e) {
             System.err.println(e.getMessage());
           }
           catch(FolderClosedIOException e) {
             System.err.println(e.getMessage());
             break;
           }
         }
         if (count > 0) {
           saveTimestamp(this.userTimestamps, this.timestampFile);
         }
       }
       catch (Exception e) {
         System.err.println("Error getting mail for user ["+user+"]: "+e.getClass().getSimpleName()+": "+e.getMessage());
        e.printStackTrace(System.err);
       }
     }
     System.out.println("Done");
   }
   
   @SuppressWarnings("resource")
   private File saveMessage(String user, Message message) throws Exception {
     File f = generateFileName(user, message);
     f.getParentFile().mkdirs();
     if (f.exists()) {
       System.out.println("File already exist: "+f.getAbsolutePath());
     }
     if (this.zip) {
       writeZip(f, message);
     }
     else {
       writeFile(f, message);
     }
     return f;
   }
 
   private void writeZip(File f, Message message) throws IOException, MessagingException {
     ZipOutputStream zos = null;
     try {
       zos = new ZipOutputStream(new FileOutputStream(f));
       ZipEntry zipEntry = new ZipEntry(f.getName());
       zos.putNextEntry(zipEntry);
       message.writeTo(zos);
       zos.closeEntry();
     }
     finally {
       close(zos);
     }
   }
   
   private void writeFile(File f, Message message) throws IOException, MessagingException {
     BufferedOutputStream os = null;
     try {
       os = new BufferedOutputStream(new FileOutputStream(f));
       message.writeTo(os);
       os.flush();
     }
     finally {
       close(os);
     }
   }
 
   // Format: user_yyyymmddThhmmss_hash.mail
   private File generateFileName(String user, Message message) throws MessagingException {
     // generate folder
     Calendar c = Calendar.getInstance();
     c.setTime(message.getReceivedDate());
     String year = Integer.toString(c.get(Calendar.YEAR));
     String month = Integer.toString(c.get(Calendar.MONTH)+1);
     String day = Integer.toString(c.get(Calendar.DAY_OF_MONTH));
     if (month.length() < 2) month = "0"+month;
     if (day.length() < 2) day = "0"+day;
     
     File folder = new File(this.dataDir, this.domain);
     folder = new File(folder, year);
     folder = new File(folder, month);
     folder = new File(folder, day);
 
     // generate name
     StringBuilder sb = new StringBuilder();
     sb.append(user);
     sb.append("_");
     SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
     sb.append(df.format(message.getReceivedDate()));
     sb.append("_");
     sb.append(getHash(message));
     sb.append(".mail");
     if (this.zip) {
       sb.append(".zip");
     }
     
     File file = new File(folder, sb.toString());
     return file;
   }
   
   private String getHash(Message m) throws MessagingException {
     String from = m.getFrom() != null && m.getFrom().length > 0? m.getFrom()[0].toString() : "";
     String subject = m.getSubject() != null ? m.getSubject() : "";
     String hash = DigestUtils.md5Hex(from+""+subject);
     // no need to be super long - the hash part is there just to avoid (infrequent) name collisions
     return hash.substring(0, 5);
   }
   
   private IMAPStore getStore(String email) throws Exception {
     String authToken = OAuth2Authenticator.getToken(this.serviceAccountPkFile, this.serviceAccountId, email);
     System.out.println("authToken OK");
 
     IMAPStore store = OAuth2Authenticator.connectToImap("imap.gmail.com", 993, email, authToken, false);
     System.out.println("imapStore OK");
     return store;
   }
   
   private final Date getDate(String d, String format) {
     SimpleDateFormat df = new SimpleDateFormat(format);
     try {
       return df.parse(d);
     } 
     catch (ParseException e) {
       throw new RuntimeException("Unable to parse Date ["+d+"]", e);
     }
   }
   
   /**
    * load saved timestamp file (if available)
    */
   private Map<String, Date> loadTimestamp(File f, Date defaultDate) {
     Map<String, Date> result = new HashMap<String, Date>();
     if (f.exists() && f.canRead()) {
       @SuppressWarnings("resource")
       BufferedReader br = null;
       try {
         br = new BufferedReader(new FileReader(f));
         String line = null;
         SimpleDateFormat df = new SimpleDateFormat(USER_TIMESTAMP_FORMAT);
         while((line = br.readLine()) != null) {
           String[] ss = line.split("=");
           if (ss.length != 2) {
             System.err.println("Don't understand line ["+line+"]");
             continue;
           }
           try {
             result.put(ss[0], df.parse(ss[1]));
           } 
           catch (ParseException e) {
             System.err.println("Unable to parse date ["+ss[1]+"]");
             continue;
           }
         }
       } 
       catch (IOException e) {
         System.err.println("Error loading user timestamps from "+f.getAbsolutePath()+": "+e.getMessage());
       }
       finally {
         close(br);
       }
     }
     // fill with defaults
     for(String user: this.users) {
       if (!result.containsKey(user)) {
         result.put(user, defaultDate);
       }
     }
     // log
     for(Map.Entry<String, Date> me: result.entrySet()) {
       System.out.println(me.getKey()+"="+me.getValue());
     }
     return result;
   }
   
   private static void close(Closeable c) {
     if (c != null) {
       try {
         c.close();
       }
       catch (IOException e) {
         // ignore
       }
     }
   }
   
   static class UserMessagesIterator implements Iterator<Message> {
     private final int max;
     private final List<Message> messages;
     private int index;
 
     public UserMessagesIterator(IMAPStore store, Date fetchFrom, List<String> ignoreFrom, int max) throws MessagingException {
       this.max = max;
       this.messages = getMessages(store, fetchFrom, ignoreFrom);
     }
 
     public String getStats() {
       return this.index+"/"+this.messages.size();
     }
     
     @Override
     public boolean hasNext() {
       return this.index < this.messages.size();
     }
 
     @Override
     public Message next() {
       return this.messages.get(this.index++);
     }
 
     @Override
     public void remove() {
       throw new UnsupportedOperationException();
     }
 
     private List<Message> getMessages(IMAPStore store, Date fetchFrom, List<String> ignoreFrom) throws MessagingException {
       IMAPFolder folder = (IMAPFolder)store.getFolder("[Gmail]/All Mail");
       folder.open(Folder.READ_ONLY);
       System.out.println("imap folder open OK");
       int totalMessages = folder.getMessageCount();
       System.out.println("Total messages: " + totalMessages);
 
       List<Message> result = new ArrayList<Message>();
       for(Message m: fetch(folder, fetchFrom)) {
         try {
           if (m.getReceivedDate() != null && m.getReceivedDate().after(fetchFrom)) {
             Address[] addresses = m.getFrom();
             if (addresses.length == 0) {
               System.out.println("Ignoring email with empty from");
               continue;
             }
             String from = addresses[0].toString();
             for(String ignore: ignoreFrom) {
               if (from.toLowerCase().contains(ignore)) {
                 System.out.println("Ignoring email from "+from);
                 continue;
               }
             }
             result.add(m);
           }
         }
         catch (MessageRemovedException e) {
           System.out.println("Message already removed: "+e.getMessage());
         }
       }
       System.out.println("Result filtered to: " + result.size());
       return result;
     }
 
     private Date getDateDaysFrom(Date from, int days) {
       Calendar c = Calendar.getInstance();
       c.setTime(from);
       c.add(Calendar.DAY_OF_YEAR, days);
       return c.getTime();
     }
     
     private Message[] fetch(IMAPFolder folder, Date fetchFrom) throws MessagingException {
       SearchTerm st = new ReceivedDateTerm(ComparisonTerm.GE, fetchFrom);
       Date fetchTo = getDateDaysFrom(fetchFrom, 365);
       if (fetchTo.before(new Date())) {
         SearchTerm stTo = new ReceivedDateTerm(ComparisonTerm.LT, fetchTo);
         st = new AndTerm(st, stTo);
         System.out.println("Setting fetchTo to "+fetchTo);
       }
       
       // IMAP search command disregards time, only date is used
       Message[] messages = folder.search(st);
       System.out.println("Search returned: " + messages.length);
       
       if (messages.length == 0 && fetchTo.before(new Date())) { // our search window could be too much in the past, retry
         System.out.println("Retrying with fetchFrom: "+fetchTo);
         return fetch(folder, fetchTo);
       }
       
       FetchProfile fp = new FetchProfile();
       fp.add(FetchProfile.Item.ENVELOPE);
       folder.fetch(messages, fp);
 
       // messages returned from search not in order. Since we might not process all of them at once we need to sort
       Arrays.sort(messages, new Comparator<Message>() {
         @Override
         public int compare(Message m1, Message m2) {
           try {
             return m1.getSentDate().compareTo(m2.getSentDate());
           } 
           catch (MessagingException e) {
             throw new RuntimeException("Comparator error: "+e.getMessage(), e);
           }
         }
       });
 
       return messages;
     }
     
   }
   
   private void saveTimestamp(Map<String, Date> data, File f) {
     @SuppressWarnings("resource")
     BufferedWriter bw = null;
     try {
       bw = new BufferedWriter(new FileWriter(f));
       SimpleDateFormat df = new SimpleDateFormat(USER_TIMESTAMP_FORMAT);
       for(Map.Entry<String, Date> me: data.entrySet()) {
         String line = me.getKey()+"="+df.format(me.getValue());
         bw.write(line + "\n");
         System.out.println(line);
       }
       bw.flush();
     }
     catch (IOException e) {
       System.err.println("Error saving user timestamps to "+f.getAbsolutePath()+": "+e.getMessage());
     }
     finally {
       close(bw);
     }
   }
   
   public static void main(String[] args) throws Exception {
     if (args.length != 1) {
       System.err.println("Usage: "+GmailBackup.class.getSimpleName()+" <properties file>");
       System.exit(1);
     }
     File propFile = new File(args[0]);
     if (!propFile.exists() || !propFile.canRead()) {
       System.err.println("Can't read from properties file "+propFile.getAbsolutePath());
       System.exit(2);
     }
     System.out.println("Reading properties from "+propFile.getAbsolutePath());
     Properties p = new Properties();
     
     @SuppressWarnings("resource")
     FileReader r = null;
     try {
       r = new FileReader(propFile);
       p.load(r);
     }
     finally {
       close(r);
     }
     System.out.println(p);
     new GmailBackup(p).backup();
   }
 
 }
