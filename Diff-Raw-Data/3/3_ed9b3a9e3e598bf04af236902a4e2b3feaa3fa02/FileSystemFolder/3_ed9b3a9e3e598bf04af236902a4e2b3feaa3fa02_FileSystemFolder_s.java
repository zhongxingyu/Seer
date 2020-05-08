 package Email;
 
 import Persist.PersistentStorage;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 import util.Util;
 
 /**
  * Implementation of Folder for the File System
  *
  * @author anasalkhatib, chanman
  */
 public class FileSystemFolder implements Folder {
 
     private FileSystemFolder parent;
     HashMap<String, Message> messages;
     ArrayList<Folder> folders;
     String name = new String();
     PersistentStorage persistStore = PersistentStorage.getInstance();
     static final Logger logger = Logger.getLogger(FileSystemFolder.class.getName());
 
     /**
      * Create an object to represent a folder on the filesystem
      *
      * @param name The name of this folder
      * @param parent The object the represents the folder containing this one
      */
     public FileSystemFolder(String name, FileSystemFolder parent) {
         this.name = name;
         this.parent = parent;
 
         this.folders = Util.newArrayList();
         this.messages = Util.newHashMap();
     }
 
     @Override
     public String getId() {
         return this.getPath();
     }
 
     @Override
     public void setId(String id) {
         // FIXME; should never be called;
         throw new NullPointerException();
     }
 
     @Override
     public String getName() {
         return this.name;
     }
 
     @Override
     public void setName(String name) {
         this.name = name;
     }
 
     @Override
     public ArrayList<Message> getMessages() {
 
         ArrayList<String> messageList = persistStore.loadMessageListFromFolder(this.getPath());
 
         if (messageList == null) {
             return Util.newArrayList();
         }
 
         for (int i = 0; i < messageList.size(); i++) {
         }
 
         HashMap<String, Message> newset = Util.newHashMap();
 
         for (String messagepath : messageList) {
 
             String pattern = Pattern.quote(System.getProperty("file.separator"));
             String[] sep = messagepath.split(pattern);
             String messagefn = sep[sep.length - 1];
 
             if (messages.containsKey(messagefn)) {
                 newset.put(messagefn, messages.get(messagefn));
             } else {
                 String filecontent = persistStore.loadMessage(messagepath);
                 PlainTextMessage msg = PlainTextMessage.parse(filecontent);
                 msg.setId(messagepath);
                 newset.put(messagefn, msg);
 
             }
         }
         messages = newset;
 
         return new ArrayList<Message>(messages.values());
     }
 
     @Override
     public ArrayList<Folder> getSubfolders() {
 
         ArrayList<String> subFolderList = persistStore.loadSubfolders(this.getPath());
 
         if (subFolderList == null) {
             return Util.newArrayList();
         }
 
         for (int i = 0; i < subFolderList.size(); i++) {
             String pattern = Pattern.quote(System.getProperty("file.separator"));
             String[] sep = subFolderList.get(i).split(pattern);
             subFolderList.set(i, sep[sep.length - 1]);
         }
 
         ArrayList<Folder> newset = Util.newArrayList();
 
         for (String subfolder : subFolderList) {
 
             // is this folder already here?
             boolean found = false;
             for (Folder f : folders) {
                 if (f.getName().equals(subfolder)) {
                     found = true;
                     newset.add(f);
                 }
             }
 
             if (!found) {
                 newset.add(new FileSystemFolderProxy(subfolder, this));
             }
 
         }
 
         folders = newset;
 
         return folders;
     }
 
     @Override
     public void addMessage(Message msg) {
         PlainTextMessage m = (PlainTextMessage) msg;
         String id = m.getId();
         String fn = id.substring(id.lastIndexOf(File.separator) + 1);
         String newid = this.getPath() + File.separator + fn;
         msg.setId(newid);
         persistStore.deleteMessage(id);
         persistStore.newMessage(m.getId());
         persistStore.saveMessage(m.getId(), m.serialize());
         sync();
     }
 
     @Override
     public void addMessageCopy(Message msg) {
         PlainTextMessage m = (PlainTextMessage) msg;
         String id = m.getId();
         String fn = id.substring(id.lastIndexOf(File.separator) + 1);
         String newid = this.getPath() + File.separator + fn;
         msg.setId(newid);
         persistStore.newMessage(m.getId());
         persistStore.saveMessage(m.getId(), m.serialize());
         sync();
     }
 
     @Override
     public void deleteMessage(Message msg) {
         persistStore.deleteMessage(msg.getId());
 
         String keytoremove = null;
         for (String s : messages.keySet()) {
             if (messages.get(s) == msg) {
                 keytoremove = s;
             }
         }
         if (keytoremove != null) {
             this.messages.remove(keytoremove);
         }
         sync();
     }
 
     @Override
     public void addFolder(Folder folder) {
         persistStore.newFolderInMailbox(folder.getId());
         this.folders.add(folder);
         sync();
     }
 
     @Override
     public void deleteFolder(Folder folder) {
         persistStore.deleteFolderAndAllContents(folder.getId());
         this.folders.remove(folder);
         sync();
     }
 
     @Override
     public void sync() {
         this.getMessages();
         this.getSubfolders();
     }
 
     /**
      * Set the parent folder
      *
      * This should be called in the event that this folder has been moved and
      * it's parent has therefore changed
      *
      * @param parent the new parent for this folder
      */
     public void setParent(FileSystemFolder parent) {
         this.parent = parent;
     }
 
     /**
      * Get the parent folder
      *
      * This is called to establish where this folder is and as a means back to
      * the root
      *
      * @return parent the parent object for this folder
      */
     public FileSystemFolder getParent() {
         return parent;
     }
 
     /**
      * Get the path for this folder on the File System relative the the mailbox
      *
      * @return path a string that can be passed to PersistentStorage
      */
     public String getPath() {
         return parent.getPath() + File.separator + this.getName();
     }
 
     @Override
     public void createFolder(String name) {
         persistStore.newFolder(this.getPath() + File.separator + name);
         sync();
     }
 
     @Override
     public void moveFolder(Folder destination) {
         FileSystemFolderProxy fsdest;
         fsdest = (FileSystemFolderProxy) destination;
         persistStore.moveFolder(this.getPath(), fsdest.getPath());
 
         parent.sync();
         sync();
         destination.sync();
     }
 }
