 /*
  * $Id: $
  *
  * Copyright (c) 2009 Fujitsu Denmark
  * All rights reserved.
  */
 
 package dk.fujitsu.issuecheck.scm;
 
 import dk.fujitsu.issuecheck.Config;
 import dk.fujitsu.issuecheck.Shell;
 import org.apache.log4j.Logger;
 
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * @author Claus Brøndby Reimer (dencbr) / Fujitsu Denmark a|s
  * @version $Revision: $ $Date: $
  */
 public class GitService implements ScmService {
     private static final Logger LOGGER = Logger.getLogger(GitService.class);
 
     @Override
     public List<String> getMessages(String... arguments) {
         Shell shell;
         String[] entries;
         String messages;
 
         if (arguments.length != 3) {
             throw new RuntimeException("getMessages takes 3 arguments: object reference, old revision and new revision");
         }
 
         LOGGER.debug("fetching messages for " + Arrays.asList(arguments));
 
         shell = new Shell();
 
         try {
             shell.execute(new String[]{
                     Config.get("scm.git.bin"),
                     "log",
                     "--pretty=format:%s",
                    arguments[0] + ".." + arguments[1]});
         } catch (Throwable x) {             
             throw new RuntimeException("unable to execute git, " + x.getMessage(), x);
         }
 
         if (shell.getExitValue() != 0) {
             throw new RuntimeException("unable to retrieve git log for package " + Arrays.asList(arguments) + " - stdout: " + shell.getStandard() + " errout: " + shell.getError());
         }
 
         messages = shell.getStandard().toString();
 
         LOGGER.debug("messages retrieved " + messages);
         entries = messages.split(System.getProperty("line.separator"));
 
         return Arrays.asList(entries);
     }
 }
