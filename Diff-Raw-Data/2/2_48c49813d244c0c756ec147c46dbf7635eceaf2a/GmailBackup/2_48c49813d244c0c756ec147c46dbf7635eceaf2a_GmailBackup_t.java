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
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.mail.FetchProfile;
 import javax.mail.Folder;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.search.ComparisonTerm;
 import javax.mail.search.ReceivedDateTerm;
 import javax.mail.search.SearchTerm;
 
 import com.google.code.samples.oauth2.OAuth2Authenticator;
 import com.sun.mail.imap.IMAPStore;
 
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
     
     Date oldestDate = getDate(p.getProperty("oldestDate", "2012/01/01"), "yyyy-MM-dd");
     this.userTimestamps = loadTimestamp(this.timestampFile, oldestDate);
     
     this.dataDir = new File(p.getProperty("dataDir"));
   }
 
   private void backup() throws Exception {
     OAuth2Authenticator.initialize();
 
     for(String user: this.users) {
       System.out.println("Backing up ["+user+"]");
       String email = user + "@" + this.domain;
       IMAPStore store = getStore(email);
       
       UserMessagesIterator iterator = new UserMessagesIterator(store, this.userTimestamps.get(user), this.ignoreFrom);
       int count = 0;
       while(iterator.hasNext()) {
         Message message = iterator.next();
         File f = saveMessage(user, message);
         // update stats
         this.userTimestamps.put(user, message.getReceivedDate());
         System.out.println(iterator.getStats()+" "+f.getAbsolutePath());
         count++;
         if (count % 1000 == 0) {
           saveTimestamp(this.userTimestamps, this.timestampFile);
         }
       }
       saveTimestamp(this.userTimestamps, this.timestampFile);
     }
     System.out.println("Done");
   }
   
   @SuppressWarnings("resource")
   private File saveMessage(String user, Message message) throws Exception {
     File f = generateFileName(user, message);
     f.getParentFile().mkdirs();
     BufferedOutputStream os = null;
     try {
       os = new BufferedOutputStream(new FileOutputStream(f));
       message.writeTo(os);
       os.flush();
     }
     finally {
       close(os);
     }
     return f;
   }
 
   private File generateFileName(String user, Message message) throws MessagingException {
     Calendar c = Calendar.getInstance();
     c.setTime(message.getReceivedDate());
     String year = Integer.toString(c.get(Calendar.YEAR));
    String month = Integer.toString(c.get(Calendar.MONTH)) + 1;
     String day = Integer.toString(c.get(Calendar.DAY_OF_MONTH));
     if (month.length() < 2) month = "0"+month;
     if (day.length() < 2) day = "0"+day;
     
     File folder = new File(this.dataDir, this.domain);
     folder = new File(folder, year);
     folder = new File(folder, month);
     folder = new File(folder, day);
 
     String id = generateId(user, message);
     return new File(folder, id+".mail");
   }
   
   private String generateId(String user, Message message) throws MessagingException {
     StringBuilder sb = new StringBuilder();
     sb.append(user);
     sb.append("_");
     SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
     sb.append(df.format(message.getReceivedDate()));
 
     return sb.toString();
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
     private final List<Message> messages;
     private int index;
 
     public UserMessagesIterator(IMAPStore store, Date fetchFrom, List<String> ignoreFrom) throws MessagingException {
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
       Folder folder = store.getFolder("[Gmail]/All Mail");
       folder.open(Folder.READ_ONLY);
       System.out.println("imap folder open OK");
 
       int totalMessages = folder.getMessageCount();
       System.out.println("Total messages: " + totalMessages);
 
       // IMAP search command disregards time, only date is used
       SearchTerm st = new ReceivedDateTerm(ComparisonTerm.GE, fetchFrom);
       Message[] messages = folder.search(st);
       System.out.println("Search returned: " + messages.length);
       // Fetch profile
       FetchProfile fp = new FetchProfile();
       fp.add(FetchProfile.Item.ENVELOPE);
       //fp.add("X-mailer");
       folder.fetch(messages, fp);
 
       List<Message> result = new ArrayList<Message>();
       for(Message m: messages) {
         if (m.getReceivedDate() != null && m.getReceivedDate().after(fetchFrom)) {
           String from = m.getFrom()[0].toString();
           for(String ignore: ignoreFrom) {
             if (from.toLowerCase().contains(ignore)) {
               System.out.println("Ignoring email from "+from);
               continue;
             }
           }
           result.add(m);
         }
       }
       System.out.println("Result filtered to: " + result.size());
       return result;
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
