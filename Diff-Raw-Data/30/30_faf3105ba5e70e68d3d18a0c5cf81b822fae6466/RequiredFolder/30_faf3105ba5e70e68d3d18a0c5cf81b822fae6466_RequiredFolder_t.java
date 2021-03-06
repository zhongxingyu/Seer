 package com.softartisans.timberwolf.exchange;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /** Helper class for required folders in exchange */
 public class RequiredFolder
 {
     private final String name;
     private String id;
     private List<RequiredFolder> folders;
     private List<RequiredEmail> emails;
 
     public RequiredFolder(final String folderName)
     {
         this.name = folderName;
         folders = new ArrayList<RequiredFolder>();
         emails = new ArrayList<RequiredEmail>();
     }
 
     public String getName()
     {
         return name;
     }
 
     public String getId()
     {
         return id;
     }
 
     public void setId(final String folderId)
     {
         id = folderId;
     }
 
     public RequiredFolder addFolder(final String childFolder)
     {
         RequiredFolder folder = new RequiredFolder(childFolder);
         folders.add(folder);
         return folder;
     }
 
    public RequiredEmail add(String to, String subject, String body)
     {
        RequiredEmail email = new RequiredEmail(to, subject, body);
         emails.add(email);
         return email;
     }
 
     public void initialize(ExchangePump pump, String user)
     {
         if (folders.size() > 0)
         {
             pump.createFolders(user, getId(), folders);
             for (RequiredFolder folder : folders)
             {
                 System.err.println("    Initialized folder: " + folder.getId());
                 folder.initialize(pump, user);
             }
             for (RequiredEmail email : emails)
             {
                 email.initialize(this, user);
             }
         }
     }
 
     public void sendEmail(ExchangePump pump, String user)
     {
         if (emails.size() > 0)
         {
             pump.sendMessages(emails);
         }
         if (folders.size() > 0)
         {
             for (RequiredFolder folder : folders)
             {
                 folder.sendEmail(pump, user);
             }
         }
 
     }
 }
