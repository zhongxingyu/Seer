 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Email;
 import Persist.PersistentStorage;
 import java.util.ArrayList;
 import util.Util;
 
 /**
  *
  * @author anasalkhatib
  */
 public class FileSystemFolder implements Folder {
     //Id of the folder has to be path relative to mailboxPath
     //e.g id for <MailboxPath>/Inbox = Inbox
     //    id for <MailboxPath>/Inbox/SubFolder = Inbox/SubFolder
     String id = new String();
     ArrayList<Message> messages;
     ArrayList<Folder> folders;
     // name of <MailboxPath>/Inbox/SubFolder = SubFolder
     String name = new String();
     //Need the mailboxID to get persistenceStore, unless we get the persistStore
     //from the controller?
     PersistentStorage persistStore = PersistentStorage.getInstance();
 
     /**
      * Constructor for initialization
      * @param id
      */
     public FileSystemFolder(String id) {
         this.folders = Util.newArrayList();
         this.messages = Util.newArrayList();
         this.id = id;
         //FIXME extract last field
        this.name = id;
         this.sync();
     }
 
     @Override
     public String getId() {
         return this.id;
     }
 
     @Override
     public void setId(String id) {
         this.id = id;
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
         ArrayList<String> messageList = persistStore.loadMessageListFromFolder(id);
         ArrayList<Message> messages = Util.newArrayList();
         if (null != messageList) {
             for (String messagePath : messageList) {
             PlainTextMessage msg = (new PlainTextMessage());
             msg.setId(messagePath);
             msg.parse(persistStore.loadMessage(messagePath));
             messages.add(msg);
             }
         }
         this.messages = messages;
         return messages;
     }
 
     @Override
     public ArrayList<Folder> getSubfolders() {
         ArrayList<String> subFolderList = persistStore.loadSubfolders(id);
         ArrayList<Folder> subFolders = Util.newArrayList();
         if (null != subFolderList) {
         for (String subfolder : subFolderList) {
             FileSystemFolder folder = new FileSystemFolder(subfolder);
             //FIXME populate ArrayList in folder? Or In constructor?
             //Or does the folder do it as needed.
             subFolders.add(folder);
 
             }
         }
         this.folders = subFolders;
         return subFolders;
     }
 
     @Override
     public void addMessage(Message msg) {
         persistStore.newMessage(msg.getId());
         this.messages.add(msg);
     }
 
     @Override
     public void addMessageCopy(Message msg) {
         //FIXME: Currenlty Assuming this is save to existing message
         //TODO check that toString is implemented
         persistStore.saveMessage(msg.getId(), msg.toString());
     }
 
     @Override
     public void deleteMessage(Message msg) {
         persistStore.deleteMessage(msg.getId());
         this.messages.remove(msg);
     }
 
     @Override
     public void addFolder(Folder folder) {
         persistStore.newFolderInMailbox(folder.getId());
         this.folders.add(folder);
     }
 
     @Override
     public void deleteFolder(Folder folder) {
         persistStore.deleteFolderAndAllContents(folder.getId());
         this.folders.remove(folder);
     }
 
     @Override
     public void sync() {
         //TODO Maybe this is where we can re-read from FileSystem and repopulate
         //the arraylists for messages and folders?
         this.getMessages();
         this.getSubfolders();
     }
 }
