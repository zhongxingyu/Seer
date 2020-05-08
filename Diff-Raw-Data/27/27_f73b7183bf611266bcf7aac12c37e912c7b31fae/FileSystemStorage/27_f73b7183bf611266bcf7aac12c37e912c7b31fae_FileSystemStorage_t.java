 package Persist;
 
 import Email.FilterRule;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 import java.util.UUID;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import util.Util;
 
 /**
  *
  * @author anasalkhatib
  */
 class FileSystemStorage extends PersistentStorage {
 
     static final Logger logger = Logger.getLogger(FileSystemStorage.class.getName());
 
     static {
         logger.setParent(Logger.getLogger(FileSystemStorage.class.getPackage().getName()));
     }
     String mailBoxPath;
     private String mailBoxID;
     //static private FileSystemStorage instance = null;
 
     FileSystemStorage(String mailBoxID) {
         //if (null == instance) {
         String path = getHomeFolderPathWithSeparator();
         mailBoxPath = path;
         logger.log(Level.INFO, "mailboxes are in {0} ", mailBoxPath);
         newFolder(mailBoxPath + File.separator + mailBoxID); // FIXME:
         this.mailBoxID = mailBoxID;
         initialiseMailboxFolder();
         newRuleFileInMailbox(File.separator + mailBoxID + File.separator + "rules.txt");
         //    this.instance = this;
         //  }
     }
 
     @Override
     public String getMailboxID() {
         return this.mailBoxID;
     }
 
     private String getHomeFolderPathWithSeparator() {
         File mailboxdir = new File(System.getProperty("user.home") + File.separator + "_mailbox");
         if (!mailboxdir.exists()) {
             mailboxdir.mkdir();
         }
         return System.getProperty("user.home") + File.separator + "_mailbox" + File.separator;
     }
 
     @Override
     public boolean newFolder(String fullPath) {
         File folder = new File(mailBoxPath + fullPath);
         //TODO Handle return, or leave it up to caller?
         return folder.mkdir();
     }
 
     @Override
     public boolean newFolderInMailbox(String newFolderPath) {
         File folder = new File(mailBoxPath + newFolderPath);
         //TODO Handle return?
         return folder.mkdir();
     }
 
     @Override
     public boolean newRuleFileInMailbox(String path) {
         File file = new File(mailBoxPath + path);
         try {
             return file.createNewFile();
         } catch (IOException ex) {
             logger.log(Level.SEVERE, null, ex);
         }
         return false;
     }
 
     @Override
     public ArrayList<String> loadMessageListFromFolder(String folder) {
         File parentFolder = new File(mailBoxPath + folder);
         ArrayList<String> mesageList = null;
         if (!parentFolder.isDirectory() || !parentFolder.exists()) {
             return mesageList;
         }
         mesageList = Util.newArrayList();
         File[] allFiles = parentFolder.listFiles();
         for (File file : allFiles) {
            if (file.isFile() && !(file.getName().equals("rules.txt"))) {
                 mesageList.add(folder + File.separator + file.getName());
                 logger.log(Level.INFO, mesageList.toString());
             }
         }
         return mesageList;
     }
 
     @Override
     public ArrayList<String> loadSubfolders(String folder) {
         File parentFolder = new File(mailBoxPath + folder);
         ArrayList<String> folderList = null;
         if (!parentFolder.exists() || !parentFolder.isDirectory()) {
             return folderList;
         }
         folderList = Util.newArrayList();
         File[] allFiles = parentFolder.listFiles();
         for (File file : allFiles) {
             if (file.isDirectory()) {
                 logger.log(Level.INFO, file.toString());
                 folderList.add(folder + File.separator + file.getName());
                 logger.log(Level.INFO, folderList.toString());
             }
         }
         return folderList;
     }
 
     @Override
     public boolean deleteFolderAndAllContents(String folder) {
         logger.log(Level.INFO, "Deleting file: {0}", folder);
         File folderToDelete = new File(mailBoxPath + folder);
         if (!folderToDelete.isDirectory() || !folderToDelete.exists()) {
             return false;
         }
         File[] allFiles = folderToDelete.listFiles();
         for (File file : allFiles) {
             if (file.isDirectory()) {
                 logger.log(Level.INFO, "Deleting folder and contents of: {0}", file.getName());
                 deleteFolderAndAllContents(folder + File.separator + file.getName());
             }
             file.delete();
             logger.log(Level.INFO, "Deleting file: {0}", file.getName());
         }
         return folderToDelete.delete();
     }
 
     @Override
     public boolean moveMessageToFolder(String messagePath, String folderPath) {
         File messageToMove = new File(mailBoxPath + File.separator + messagePath);
         File destinationFolder = new File(mailBoxPath + File.separator + folderPath);
         if (!destinationFolder.exists()) {
             return false;
         }
         File newFile = new File(destinationFolder + File.separator + messageToMove.getName());
         logger.log(Level.INFO, "Moving message {0}", messageToMove.toString());
         logger.log(Level.INFO, "To {0}", newFile.toString());
 
         return messageToMove.renameTo(newFile);
     }
 
     @Override
     public boolean newMessage(String messagePath) {
         File newMessage = new File(mailBoxPath + messagePath);
         boolean result = false;
         try {
             result = newMessage.createNewFile();
         } catch (IOException ex) {
             logger.log(Level.SEVERE, null, ex);
         }
         return result;
     }
 
     @Override
     public boolean saveMessage(String messagePath, String content) {
         File newMessage = new File(mailBoxPath + messagePath);
         FileOutputStream outputStream;
         if (!newMessage.exists()) {
             //TODO create it instaead?
             return false;
         } else {
             try {
                 outputStream = new FileOutputStream(newMessage);
                 outputStream.write(content.getBytes());
 
             } catch (FileNotFoundException ex) {
                 logger.log(Level.SEVERE, null, ex);
             } catch (IOException ex) {
                 logger.log(Level.SEVERE, null, ex);
             }
         }
         return true;
     }
 
     @Override
     public String loadMessage(String messagePath) {
         //http://www.javapractices.com/topic/TopicAction.do?Id=42
         StringBuilder text = new StringBuilder();
         String NL = System.getProperty("line.separator");
         Scanner scanner = null;
         try {
             scanner = new Scanner(new FileInputStream(mailBoxPath + messagePath));
             while (scanner.hasNextLine()) {
                 text.append(scanner.nextLine()).append(NL);
             }
 
         } catch (FileNotFoundException ex) {
             logger.log(Level.SEVERE, null, ex);
         } finally {
             if (scanner != null) {
                 scanner.close();
             }
         }
         return text.toString();
     }
 
     @Override
     public boolean deleteMessage(String message) {
         File pathToDelete = new File(mailBoxPath + message);
         return pathToDelete.delete();
     }
 
     @Override
     public boolean moveFolder(String folderToMove, String destinationFolder) {
         File source = new File(mailBoxPath + folderToMove);
         File destination = new File(
                 mailBoxPath
                 + destinationFolder
                 + File.separator
                 + source.getName());
         return source.renameTo(destination);
     }
 
     private void initialiseMailboxFolder() {
         List<String> initialFolders = Util.newArrayList();
         initialFolders.add("Inbox");
         initialFolders.add("Drafts");
         initialFolders.add("Outbox");
         initialFolders.add("Sent");
         initialFolders.add("Trash");
 
         initialFolders.add("Templates");
         initialFolders.add("Meetings");
 
         newFolderInMailbox(File.separator + mailBoxID);
         for (String folder : initialFolders) {
             logger.log(Level.INFO, "{0}{1}{2}{3}", new Object[]{File.separator, mailBoxID, File.separator, folder});
             newFolderInMailbox(File.separator + mailBoxID + File.separator + folder);
         }
     }
 
     @Override
     public ArrayList<FilterRule> loadRulesFromFileSystem() {
 
         ArrayList<FilterRule> listOfRules = new ArrayList<FilterRule>();
 
         String path = mailBoxPath + File.separator + mailBoxID + File.separator + "rules.txt";
 
         StringBuilder text = new StringBuilder();
         String NL = System.getProperty("line.separator");
         Scanner scanner = null;
 
         try {
             scanner = new Scanner(new FileInputStream(path));
             scanner.useDelimiter(NL);
             while (scanner.hasNext()) {
                 FilterRule rule = new FilterRule();
                 String line = scanner.next();
 
                 Scanner lineScanner = new Scanner(line);
                 lineScanner.useDelimiter("---");
                 rule.setRuleId(UUID.randomUUID().toString());
                 rule.setFromField(lineScanner.next());
                 rule.setsubjectField(lineScanner.next());
                 rule.setcontentField(lineScanner.next());
                 rule.setmoveToField(lineScanner.next());
                 listOfRules.add(rule);
             }
 
         } catch (FileNotFoundException ex) {
             System.out.println("File not found");
         } finally {
             if (scanner != null) {
                 scanner.close();
             }
         }
         return listOfRules;
     }
 
     @Override
     public void saveRulesToFileSystem(ArrayList<FilterRule> listOfRules) {
 
         String path = mailBoxPath + File.separator + mailBoxID + File.separator + "rules.txt";
         File file = new File(path);
         String delimiter = "---";
         try {
             FileOutputStream outputStream = new FileOutputStream(file);
             for (FilterRule rule : listOfRules) {
                 String line = rule.getFromField() + delimiter + rule.getsubjectField()
                         + delimiter + rule.getcontentField() + delimiter + rule.getmoveToField();
                 outputStream.write(line.getBytes());
                 outputStream.write("\n".getBytes());
             }
 
         } catch (FileNotFoundException ex) {
             Logger.getLogger(FileSystemStorage.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(FileSystemStorage.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     @Override
     public boolean isFolderExists(String folderPath) {
         String fullPath = mailBoxPath + File.separator + folderPath;
         File file = new File(fullPath);
         if ( !file.exists() )
             return false;
         return true;
     }
 }
