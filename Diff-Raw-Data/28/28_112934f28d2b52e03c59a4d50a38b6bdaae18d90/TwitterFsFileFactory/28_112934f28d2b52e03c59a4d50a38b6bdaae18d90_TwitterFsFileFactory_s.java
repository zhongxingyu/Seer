 package com.cafeform.iumfs.twitterfs;
 
 import com.cafeform.iumfs.FileFactory;
 import com.cafeform.iumfs.InvalidUserException;
 import com.cafeform.iumfs.IumfsFile;
 import com.cafeform.iumfs.Request;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author kaizawa
  */
 public class TwitterFsFileFactory implements FileFactory
 {
     static final Logger logger = Logger.getLogger(TwitterFsFileFactory.class.getName());
 
     @Override
     public IumfsFile getFile(Request request)
     {
         return getFile(request.getUserName(), request.getPathname());
     }
 
     /*
      * Get a IumfsFile object of given pathname for a user.
      * This method is called from TwitterXXXXRequest methods.
      * So this is an entry point for file operation.
      */
     public IumfsFile getFile(String username, String pathname)
     {
         if (username.isEmpty())
         {
             throw new InvalidUserException("Unknown user \"" + username + "\" specified");
         }
 
         logger.log(Level.FINER, "pathname=" + pathname + ", usernaem=" + username);
                
         Account account = Account.getAccountMap().get(username);
 
         if (account == null)
         {
             account = new Account(username);
             account.setRootDirectory(getInitialRootDirectory(account));
             Account.getAccountMap().put(username, account);
             logger.log(Level.FINE, "New Account for " + username + " created.");
         }
 
         /* 
          * If file root directory has 1 entiry but has access toke, it seems 
          * to have finished setting up twitter account. 
          * Create default root directory swap it to existing root directory.
          */
         logger.log(Level.FINE, "Map size="
                 + account.getRootDirector().listFiles().length);
         if (2 == account.getRootDirector().listFiles().length)
         {
             Prefs.sync();
             if (Prefs.get(username + "/accessToken").length() > 0)
             {
                 account.setRootDirectory(getDefaultRootDirectory(account));
             }
         }
         return lookup(account.getRootDirector(), pathname);
     }
 
     private IumfsFile getDefaultRootDirectory(Account account)
     {
        TwitterFsDirectory rootDir = new TwitterFsDirectory(account, "");
        rootDir.addFile(new PostFile(account, "post"));
        rootDir.addFile(new TimelineFile(account, "home", true, 0L));
        rootDir.addFile(new TimelineFile(account, "mentions", false, 120000));
        rootDir.addFile(new TimelineFile(account, "user", false, 300000));
        rootDir.addFile(new TimelineFile(account, "retweets_of_me", false, 600000));
        TwitterFsDirectory friendsDir = new FriendsDirectory(account, "friends");
        TwitterFsDirectory followersDir = new TwitterFsDirectory(account, "followers");        
         rootDir.addFile(friendsDir);
         rootDir.addFile(followersDir);
         // Includes itself
         rootDir.addFile(rootDir);
         return rootDir;
     }
 
     private IumfsFile getInitialRootDirectory(Account account)
     {
        TwitterFsDirectory rootDir = new TwitterFsDirectory(account, "");
        rootDir.addFile(new SetupFile(account, "setup"));
         // Includes itself        
         rootDir.addFile(rootDir);        
         return rootDir;
     }
 
     private IumfsFile lookup(IumfsFile directory, String pathname)
     {
         if (pathname.startsWith("/"))
         {
             pathname = pathname.substring(1);
         }
 
         String[] paths = pathname.split("/", 2);
 
         for (IumfsFile file : directory.listFiles())
         {
             if (file.getName().equals(paths[0]))
             {
                 if (1 == paths.length)
                 {
                     logger.log(Level.FINER, "Found " + paths[0] + 
                             " in " + directory.getName());
                     return file;
                 }
                 else
                 {
                     if (file.isDirectory())
                     {
                         // path is correct, but not a target entry
                         // dig more.
                         return lookup(file, paths[1]);
                     }
                     else 
                     {
                         logger.log(Level.WARNING, paths[0] + " is expected" +
                                 " to be directory, but regular file.");
                         // the entry must be directory but file!
                         return null;
                     }
                 }
             }
         }
         logger.log(Level.FINER, "Cannot find " + paths[0] + " in " + 
                 ("".equals(directory.getName()) ? "/":(directory.getName())));
                      
         return null;
     }
 }
