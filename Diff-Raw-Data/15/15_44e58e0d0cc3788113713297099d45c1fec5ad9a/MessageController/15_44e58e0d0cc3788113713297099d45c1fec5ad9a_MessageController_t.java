 package Email;
 
 import Meeting.MeetingSummary;
 import Persist.MessageTransfer;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Observable;
 import java.util.UUID;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import util.Util;
 
 /**
  * Message controller class
  */
 public class MessageController extends Observable {
 
     private static MessageController instance = null;
     Mailbox store;
     Rules rules;
     // What should I be doing here? this is dumb
     HashMap<String, Message> messageLookup = Util.newHashMap();
     HashMap<String, Folder> folderLookup = Util.newHashMap();
     HashMap<String, FilterRule> ruleLookup = Util.newHashMap();
 
     /**
      * MessageController Constructor
      *
      * @param messagestore
      */
     private MessageController(Mailbox messagestore) {
         store = messagestore;
 
     }
 
     /**
      * Get the instance ofMessageController
      *
      * @param messagestore
      * @return instance
      */
     static public MessageController getInstance(Mailbox messagestore) {
         if (instance == null) {
             instance = new MessageController(messagestore);
         }
         return instance;
 
     }
 
     /**
      * Get the instance ofMessageController
      *
      * @return instance
      */
     static public MessageController getInstance() {
         return instance;
     }
 
     private Message getMessageFromId(String messageId) {
         return messageLookup.get(messageId);
     }
 
     private Folder getFolderFromId(String folderId) {
         return folderLookup.get(folderId);
     }
 
     private String getIdfromMessage(Message message) {
         String id = message.getId();
         messageLookup.put(id, message);
         return id;
     }
 
     private String getIdfromFolder(Folder folder) {
         String id = folder.getId();
         folderLookup.put(id, folder);
         return id;
     }
 
     /**
      * Get sub folders from a folder
      *
      * @param folderId
      * @return Array of string id's of all the sub folders inside our folder
      */
     public String[] getFolderList(String folderId) {
         Folder fldr = getFolderFromId(folderId);
         ArrayList<Folder> set = fldr.getSubfolders();
         String[] ids = new String[set.size()];
 
         int index = 0;
         for (Folder subfolder : set) {
             ids[index++] = getIdfromFolder(subfolder);
         }
 
         return ids;
     }
 
     /**
      * Get email list from a folder
      *
      * @param folderId Id of folder
      * @return Array of string id's of all the messages inside our folder
      */
     public String[] getEmailList(String folderId) {
         String[] ids;
 
         try {
             Folder fldr = getFolderFromId(folderId);
             ArrayList<Message> set;
             set = fldr.getMessages();
 
             ids = new String[set.size()];
 
             int index = 0;
             for (Message message : set) {
                 ids[index++] = getIdfromMessage(message);
             }
         } catch (Exception ex) {
             ids = new String[0];
         }
         return ids;
     }
 
     /**
      * Get email content
      *
      * @param id
      * @return content
      */
     public String getEmailContent(String id) {
         try {
             Message message = getMessageFromId(id);
             return message.getContent();
         } catch (NullPointerException e) {
             return new String();
         }
     }
 
     /**
      * Set email content
      *
      * @param id
      * @param content
      */
     public void setEmailContent(String id, String content) {
         Message message = getMessageFromId(id);
         message.setContent(content);
 
         // update anyone waiting on updates
         this.setChanged();
         this.notifyObservers();
 
     }
 
     /**
      * Get email summary
      *
      * @param messageId Id of Message
      * @return Summary of the Message
      */
     public Summary getEmailSummary(String messageId) {
         Message message = getMessageFromId(messageId);
         Summary summary = new Summary(message);
 
         return summary;
     }
 
     /**
      * Set email header
      *
      * @param messageId Message Id
      * @param key Key
      * @param value Value
      */
     public void setEmailHeader(String messageId, String key, String value) {
         Message msg = getMessageFromId(messageId);
         msg.setHeader(key, value);
 
         // update anyone waiting on updates
         this.setChanged();
         this.notifyObservers();
 
     }
 
     /**
      * Get email header
      *
      * @param messageId Message Id
      * @param key Key for which we want a value
      * @return Value of particular key within message header
      */
     public String getEmailHeader(String messageId, String key) {
         Message msg = getMessageFromId(messageId);
         return msg.getHeaderValue(key);
     }
 
     /**
      * Mark message as read
      *
      * @param messageId Message Id
      */
     public void markRead(String messageId) {
         if (getEmailHeader(messageId, "X-Read").length() > 0) {
             return;
         }
         setEmailHeader(messageId, "X-Read", "FIXME: Set to NOW()");
 
         // update anyone waiting on updates
         this.setChanged();
         this.notifyObservers();
     }
 
     /**
      * Mark message as unread
      *
      * @param messageId Message Id
      */
     public void markUnread(String messageId) {
         String read = getEmailHeader(messageId, "X-Read");
         if (read == null || read.length() == 0) {
             return;
         }
 
         setEmailHeader(messageId, "X-Read", null);
 
         // update anyone waiting on updates
         this.setChanged();
         this.notifyObservers();
     }
 
     /**
      * Delete message for given messageId
      *
      * @param messageId Message Id
      */
     public void delete(String messageId) {
        
        Message msg = this.getMessageFromId(messageId);
 
        // FIXME: we assume this is only getting called from the trash
        store.getTrash().deleteMessage(msg);
        
         // update anyone waiting on updates
         this.setChanged();
         this.notifyObservers();
 
     }
 
     /**
      * Move message to specified destination folder
      *
      * @param messageId Message Id
      * @param destinationFolderId Destination Folder Id
      */
     public void moveMessageToFolder(String messageId, String destinationFolderId) {
         Message msg = getMessageFromId(messageId);
         Folder destination = getFolderFromId(destinationFolderId);
         destination.addMessage(msg);
 
         this.setChanged();
         this.notifyObservers();
     }
 
     public void copyMessageToFolder(String messageId, String folderId) {
         Message msg = getMessageFromId(messageId);
         Folder destination = getFolderFromId(folderId);
         destination.addMessageCopy(msg);
 
         this.setChanged();
         this.notifyObservers();
     }
 
     //FIXME Not sure what this does
     /**
      * Compose a new message
      *
      * @return message
      */
     public String compose() {
         Message newMsg = new PlainTextMessage();
         UUID messageId = UUID.randomUUID();
         newMsg.setId(messageId.toString());
         Folder drafts = store.getDrafts();
         //drafts.addMessage(newMsg);
         return getIdfromMessage(newMsg);
     }
 
     public String composeFrom(String template) {
         String id = compose();
 
         Message temp = this.getMessageFromId(template);
         Message msg = this.getMessageFromId(id);
 
         msg.setContent(temp.getContent());
         msg.setHeader("Subject", temp.getHeaderValue("Subject"));
         msg.setHeader("To", temp.getHeaderValue("To"));
 
         return id;
     }
 
     /**
      * Update the date
      *
      * @param messageid
      */
     public void updateDate(String messageid) {
         Message msg = this.getMessageFromId(messageid);
         DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
         Date date = new Date();
         msg.setHeader("Date", dateFormat.format(date));
 
         this.setChanged();
         this.notifyObservers();
     }
 
     /**
      * Create Reply content from the original message
      *
      * @param originalMessage
      * @return reply id
      */
     public String reply(String originalMessage) {
         // create a new message
         String replyid = compose();
         updateDate(replyid);
 
         Message replymsg = getMessageFromId(replyid);
 
         // get the original message and use it to create the reply content
         Message original = getMessageFromId(originalMessage);
 
         String replyContent = original.getContent();
         replyContent = "\r\n\r\n" + replyContent;
         replyContent = replyContent.replaceAll("\n", "\n> ");
 
         // get the headers based on the original message
         String to = original.getHeaderValue("From");
         String subject = original.getHeaderValue("Subject");
 
         // add RE to the subject if it is not already there
         if (subject.length() < 3 || !"RE:".equals(subject.substring(0, 3).toUpperCase())) {
             subject = "RE: " + subject;
         }
 
         // set the headers and content of the reply
         replymsg.setContent(replyContent);
         replymsg.setHeader("To", to);
         replymsg.setHeader("Subject", subject);
 
         // update anyone waiting on updates
         this.setChanged();
         this.notifyObservers();
 
         return replyid;
     }
 
     /**
      *
      * @param currentMessage
      * @return
      */
     public String forward(String currentMessage) {
         // create a new message
         String forwardid = compose();
         updateDate(forwardid);
 
         Message forwardmsg = getMessageFromId(forwardid);
 
         // get the original message and use it to create the reply content
         Message original = getMessageFromId(currentMessage);
 
         String forwardContent = original.getContent();
         forwardContent = "\r\n\r\n" + forwardContent;
         forwardContent = forwardContent.replaceAll("\n", "\n> ");
 
         // get the headers based on the original message
         String subject = original.getHeaderValue("Subject");
 
         // add RE to the subject if it is not already there
         if (subject.length() < 4 || !"FWD:".equals(subject.substring(0, 4).toUpperCase())) {
             subject = "Fwd: " + subject;
         }
 
         // set the headers and content of the reply
         forwardmsg.setContent(forwardContent);
         forwardmsg.setHeader("Subject", subject);
 
         // update anyone waiting on updates
         this.setChanged();
         this.notifyObservers();
 
         return forwardid;
     }
 
     // get particular folder ids
     /**
      * Get root folder id
      *
      * @return id
      */
     public String getRootFolderId() {
         return getIdfromFolder(store);
     }
 
     /**
      * Get drafts folder id
      *
      * @return id
      */
     public String getDraftsFolderId() {
         return getIdfromFolder(store.getDrafts());
     }
 
     /**
      * Get inbox folder id
      *
      * @return id
      */
     public String getInboxFolderId() {
         return getIdfromFolder(store.getInbox());
     }
 
     /**
      * Get outbox folder id
      *
      * @return id
      */
     public String getOutboxFolderId() {
         return getIdfromFolder(store.getOutbox());
     }
 
     /**
      * Get sent messages folder id
      *
      * @return id
      */
     public String getSentMessagesFolderId() {
         return getIdfromFolder(store.getSentMessages());
     }
 
     /**
      * Get trash folder id
      *
      * @return id
      */
     public String getTrashFolderId() {
         return getIdfromFolder(store.getTrash());
     }
 
     /**
      * Get template folder id
      *
      * @return id
      */
     public String getTemplateFolderId() {
         return getIdfromFolder(store.getTemplates());
     }
 
     /**
      * Get template folder id
      *
      * @return id
      */
     public String getMeetingFolderId() {
         return getIdfromFolder(store.getMeetings());
     }
 
     /**
      * Get the name of a folder
      *
      * @param folder
      * @return folderName
      */
     public String getFolderName(String folder) {
         return getFolderFromId(folder).getName();
     }
 
     /**
      * Create a new folder
      *
      * @param in
      * @param name
      */
     public void newFolder(String in, String name) {
         Folder folder = getFolderFromId(in);
         folder.createFolder(name);
 
         // update anyone waiting on updates
         this.setChanged();
         this.notifyObservers();
     }
 
     /**
      * Delete a folder selected
      *
      * @param selected
      */
     public void deletefolder(String selected) {
         Folder folder;
         folder = getFolderFromId(selected);
         store.deleteFolder(folder);
 
         this.setChanged();
         this.notifyObservers();
     }
 
     /**
      * Move folder form source to destination
      *
      * @param sourcePath
      * @param destinationPath
      */
     public void moveFolder(String sourcePath, String destinationPath) {
         if (sourcePath.equals(destinationPath)) {
             return;
         }
 
         Folder source = this.getFolderFromId(sourcePath);
         Folder dest = this.getFolderFromId(destinationPath);
         source.moveFolder(dest);
 
         this.setChanged();
         this.notifyObservers();
     }
 
     /**
      *
      */
     public void doSendRecieve() {
         Folder outbox = store.getOutbox();
         Folder sent = store.getSentMessages();
         Folder inbox = store.getInbox();
 
         String userId = store.getUserId();
 
         MessageTransfer transfer = MessageTransfer.getInstance();
 
         ArrayList<Message> outbound = store.getOutbox().getMessages();
         for (Message out : outbound) {
             out.setHeader("From", store.getUserId());
 
             PlainTextMessage msg = (PlainTextMessage) out; // FIXME (this is pretty derpy)
             String content = msg.serialize();
 
             for (String to : out.getHeaderValue("To").split(",")) {
                 to = to.trim();
 
                 Pattern mailpat = Pattern.compile("<(.*)>");
                 Matcher mailmat = mailpat.matcher(to);
                 if (mailmat.find()) {
                     to = mailmat.group(1).trim();
                 }
 
                 Pattern userpat = Pattern.compile("([^@]+)@.*");
                 Matcher usermat = userpat.matcher(to);
 
                 if (usermat.find()) {
                     to = usermat.group(1).trim();
                 }
 
                 transfer.sendMessageTo(to, content);
             }
             sent.addMessage(out);
             this.getIdfromMessage(out);
             this.setChanged();
         }
 
         while (transfer.MessageExistFor(userId)) {
             String message = transfer.getMessageFor(userId);
             Message newMsg = PlainTextMessage.parse(message);
             UUID messageId = UUID.randomUUID();
             newMsg.setId(messageId.toString());
             inbox.addMessage(newMsg);
             this.setChanged();
         }
 
         store.sync();
 
         // update anyone waiting on updates
         this.notifyObservers();
     }
 
     public String getMeetingsFolderId() {
         return getIdfromFolder(store.getMeetings());
     }
 
     public String getTemplatesFolderId() {
         return getIdfromFolder(store.getTemplates());
     }
 
     public String createMeeting() {
         Message newMeeting = new PlainTextMessage();
         UUID messageId = UUID.randomUUID();
         newMeeting.setId(messageId.toString());
         return getIdfromMessage(newMeeting);
     }
 
     public FilterRule getRuleFromId(String ruleId) {
         return ruleLookup.get(ruleId);
     }
 
     public String getIdfromRule(FilterRule rule) {
         String id = rule.getRuleId();
         ruleLookup.put(id, rule);
         return id;
     }
 
     public void loadRules() {
         rules = Rules.getInstance();
     }
 
     public void addRule(FilterRule rule) {
         rules.addRule(rule);
     }
 
     public void deleteRule(int ruleId) {
         rules.deleteRule(ruleId);
     }
 
     public String[] getRuleList() {
         String[] ids;
 
         try {
 
             ArrayList<FilterRule> set;
             set = rules.getListOfRules();
 
             ids = new String[set.size()];
 
             int index = 0;
             for (FilterRule rule : set) {
                 ids[index++] = getIdfromRule(rule);
             }
         } catch (Exception ex) {
             ids = new String[0];
         }
         return ids;
     }
 
     public int getRulesCount() {
         return rules.getListOfRules().size();
     }
 
     public void sendMeeting(String messageId) {
         copyMessageToFolder(messageId, getOutboxFolderId());
         moveMessageToFolder(messageId, getMeetingsFolderId());
     }
 
     public MeetingSummary getMeetingSummary(String messageId) {
         Message message = getMessageFromId(messageId);
         MeetingSummary summary = new MeetingSummary(message);
 
         return summary;
     }
 
     public void saveRules() {
         rules.saveRules();
     }
 }
