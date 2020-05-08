 import javax.mail.*;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.mail.search.FlagTerm;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Properties;
 
 /**
  * Author: Chris Wald
  * Date: 5/26/13
  * Time: 2:02 PM
  */
 public class Communicator {
     private Store store;
     private Folder folder;
     private Message last_message;
     private String username, password;
     private String folder_name = "Inbox";
     private static final int MAX_MESSAGE_LENGTH = 150; // characters
 
     private static final String SERVER = "imap.gmail.com";
     private String attachment_file_name;
 
     public boolean Send(Object o)
     {
         if (last_message == null)
             return false;
         String s = o.toString();
         return this.SendMessage(s);
     }
 
     public ArrayList<String> Receive()
     {
         boolean step_success;
         step_success = getStore();
         if (!step_success)
             return null;
 
         step_success = getFolder();
         if (!step_success)
             return null;
 
         Message[] messages = this.getMessages();
         ArrayList<String> contents = new ArrayList<String>(messages.length);
         for (Message message : messages)
         {
             String str = handleMessageContents(message);
             contents.add(str);
         }
 
         /*try
         {
             this.folder.close(true);
             this.store.close();
         }
         catch (MessagingException e)
         {
             System.err.println(">>> IN RECEIVE:");
             System.err.println(e);
         }*/
 
 
         return contents;
     }
 
     public void SetUsername(String username)
     {
         this.username = username;
     }
 
     public void SetPassword(String password)
     {
         this.password = password;
     }
 
     public void SetFolder(String folder_name)
     {
         this.folder_name = folder_name;
     }
 
     private boolean SendMessage(String message)
     {
         // Build, format, and send a message containing some content to all
         // addresses listed in the REPLY-TO field of the message currently
         // being worked with.
 
         String host = "smtp.gmail.com";
         String protocol = "smtp";
         int    port = 587;
         //int    port = 465;  Maybe this port number will be useful again
         //                    sometime. It was the original that stopped
         //                    working when I got to school.
 
         try
         {
             System.out.println(">>> SENDING REPLY");
             InternetAddress[] to_addr = (InternetAddress[]) last_message.getReplyTo();
 
             Properties props = new Properties();
             props.put("mail.smtp.starttls.enable", "true");
             props.put("mail.transport.protocol", protocol);
             props.put("mail.smtps.host", host);
             Session mailSession = Session.getInstance(props);
             Transport transport = mailSession.getTransport(protocol);
             transport.connect(host, port, this.username, this.password);
 
             ArrayList<String> messages = this.splitMessage(message);
             int index = 0;
             for (String m : messages)
             {
                 if (messages.size() > 1)
                     m = "(" + (++index) + "/" + messages.size() + ") " + m;
                 MimeMessage mimeMessage = new MimeMessage(mailSession);
                 mimeMessage.setContent(m, "text/plain");
 
                 mimeMessage.addRecipients(Message.RecipientType.TO, to_addr);
 
                 transport.sendMessage(mimeMessage,
                         mimeMessage.getRecipients(Message.RecipientType.TO));
 
                 try {
                     Thread.sleep(1000);
                 } catch (InterruptedException e) {
                     System.err.println(">>> IN SENDMESSAGE (SEND):");
                     System.err.println(e);
                 }
             }
 
             transport.close();
         }
         catch (NoSuchProviderException e) {
             System.err.println(">>> IN SENDMESSAGE (REPLY):");
             System.err.println(e);
             return false;
         }
         catch (MessagingException e) {
             System.err.println(">>> IN SENDMESSAGE (REPLY): ");
             System.err.println(e);
             return false;
         }
         return true;
     }
 
     private ArrayList<String> splitMessage(String message)
     {
         ArrayList<String> parts = new ArrayList<String>();
         while (message.length() > MAX_MESSAGE_LENGTH)
         {
             String part = message.substring(0, MAX_MESSAGE_LENGTH);
             int indexNL = part.lastIndexOf("\n");
             int indexSP = part.lastIndexOf(" ");
             int index;
             if (indexNL != -1 && indexSP != -1)
                 index = (indexNL > indexSP ? indexNL : indexSP);
             else
                 index = MAX_MESSAGE_LENGTH;
             part = message.substring(0, index).trim();
             message = message.substring(part.length()).trim();
             parts.add(part);
         }
         // Add the remaining message
         parts.add(message.trim());
 
         return parts;
     }
 
     private boolean getStore()
     {
         try
         {
             // Get the store variable for the specified user's Gmail account.
             Properties props = System.getProperties();
             Session session = Session.getDefaultInstance(props, null);
             this.store = session.getStore("imaps");
 
             if (!this.store.isConnected())
                 this.store.connect(SERVER, this.username, this.password);
         }
         catch (NoSuchProviderException e) {
             System.err.println(">>> IN GETSTORE:");
             System.err.println(e);
             return false;
         }
         catch (MessagingException e) {
             System.err.println(">>> IN GETSTORE:");
             System.err.println(e);
             return false;
         }
 
         return true;
     }
 
     private boolean getFolder()
     {
         // Reopen the folder every iteration to check for new mail.
         // Open it with read-write permissions so the messages will
         // be marked as read and not re-retrieved next loop.
         try {
             // Make sure the store is still connected.
             while (!this.store.isConnected())
                 this.getStore();
             // If the folder exists make sure that it's closed.
             if (this.folder != null && this.folder.isOpen())
                 this.folder.close(false);
             this.folder = this.store.getFolder(this.folder_name);
             this.folder.open(Folder.READ_WRITE);
         } catch (MessagingException e) {
             // If the folder can't be opened with READ_WRITE try opening
             // it READ_ONLY before failing.
             try {
                 this.folder.open(Folder.READ_ONLY);
             } catch (MessagingException ex) {
                 System.err.println(">>> IN GETFOLDER:");
                 System.err.println(ex);
                 return false;
             }
         }
         return true;
     }
 
     private Message[] getMessages()
     {
         // Create a new flag that represents all unseen/unread messages.
        FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), true);
         try {
             // Return all messages in the folder that match that flag.
             if (this.folder.isOpen())
             {
                 Message[] messages = this.folder.search(ft);
                 this.last_message = messages[messages.length-1];
                 return messages;
             }
             else
                 return null;
         } catch (MessagingException e) {
             System.err.println(">>> IN GETMESSAGES:");
             System.err.println(e);
             return null;
         }
     }
 
     private String handleMessageContents(Message message)
     {
         String contents = null;
         try
         {
             Object msg_contents = message.getContent();
             if (msg_contents instanceof Multipart)
                 contents = this.handleMultipart((Multipart) msg_contents);
             else
                 contents = this.handlePart(message);
         }
         catch (IOException e)
         {
             System.err.println(">>> IN HANDLEMESSAGECONTENTS:");
             System.err.println(e);
         }
         catch (MessagingException e)
         {
             System.err.println(">>> IN HANDLEMESSAGECONTENTS:");
             System.err.println(e);
         }
         return contents;
     }
 
     private String handleMultipart(Multipart multipart) throws MessagingException, IOException
     {
         String contents = "";
         for (int i = 0; i < multipart.getCount(); i ++)
             contents += this.handlePart(multipart.getBodyPart(i));
         return contents;
     }
 
     private String handlePart(Part part) throws MessagingException, IOException
     {
         String contents = null;
         String disposition = part.getDisposition();
         String content_type = part.getContentType().toLowerCase();
 
         if (disposition == null)
         {
             if (content_type.contains("text/plain"))
             {
                 contents = (String) part.getContent();
             }
             else if (content_type.contains("image/"))
             {
                 this.saveFile(part.getFileName(), part.getInputStream());
             }
         }
         else if (disposition.equalsIgnoreCase(Part.ATTACHMENT))
         {
             this.saveFile(part.getFileName(), part.getInputStream());
         }
         else if (disposition.equalsIgnoreCase(Part.INLINE))
         {
             this.saveFile(part.getFileName(), part.getInputStream());
         }
         else
         {
             if (content_type.contains("text/plain"))
             {
                 contents = (String) part.getContent();
             }
         }
 
         return contents;
     }
 
     private void saveFile(String filename, InputStream input) throws IOException
     {
         this.deleteAttachment();
 
         // Creates a new temporary file if no name is given.
         if (filename == null)
         {
             filename = File.createTempFile("xx", ".out").getName();
         }
         // Do no overwrite existing file
         File file = new File(filename);
         for (int i=0; file.exists(); i++)
         {
             file = new File(filename+i);
         }
 
         this.attachment_file_name = file.getAbsolutePath();
 
         // Save the file.
         FileOutputStream fos = new FileOutputStream(file);
         BufferedOutputStream bos = new BufferedOutputStream(fos);
 
         BufferedInputStream bis = new BufferedInputStream(input);
         int aByte;
         while ((aByte = bis.read()) != -1)
         {
             bos.write(aByte);
         }
         bos.flush();
         bos.close();
         bis.close();
     }
 
     private void deleteAttachment()
     {
         if (this.attachment_file_name != null)
         {
             File f = new File(this.attachment_file_name);
             if (f.exists())
                 f.delete();
         }
     }
 }
