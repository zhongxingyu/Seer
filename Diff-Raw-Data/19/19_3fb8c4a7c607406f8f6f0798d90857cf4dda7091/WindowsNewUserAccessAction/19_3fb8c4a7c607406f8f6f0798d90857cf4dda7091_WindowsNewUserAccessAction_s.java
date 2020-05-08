 /**
  * SAHARA Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2010, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 25th April 2010
  */
 package au.edu.uts.eng.remotelabs.rigclient.action.access;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 
 import au.edu.uts.eng.remotelabs.rigclient.rig.IAccessAction;
 import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
 import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;
 
 /**
  * Action which creates a new user on Windows. This action does assign
  * access to be able login remotely using the {@link RemoteDesktopAccessAction}
  * class to add the newly created user to the "Remote Desktop Users"
  * group.
  * <br />
  * The name of the new user is the name of the assigned user if it does not 
  * already exist, otherwise it is the name of the user with a incremented 
  * number appended to it as needed to make the name unique. The password is 
  * randomly generated with alphanumeric characters and of length 8.
  * <br />
  * The {@link WindowsNewUserController} primitive controller class is set 
  * with the newly created user login credentials and these may accessed
  * with the <tt>getCredentialsAction</tt> action method.  
  */
 public class WindowsNewUserAccessAction implements IAccessAction
 {
     /** 'net' command string. */
     public static final String NET_COMMAND = "net";
     
     /** 'user' option for the 'net' command. */
     public static final String USER_OPTION = "user";
     
     /** ProcessBuilder for the Windows 'net user' command. */
     private final String netUser[] = {"net", "user"}; 
     
     /** Options to create the user account with. */
     private final String userOptions[] = {"/ACTIVE:YES",                  /* The account is immediately activated. */
                                           "/EXPIRES:NEVER",               /* The account never expires. */
                                           "/PASSWORDCHG:NO",              /* The user may not change their password. */
                                           "/PASSWORDREQ:YES"};            /* Password is required for user. */
     
     /** Assigns access to remote desktop sessions. */
     private final RemoteDesktopAccessAction rdpAccess;
     
     /** Random number generator. */
     private final Random random;
     
     /** The name of the user who was added on the previous <tt>assign</tt> call. */
     private static String userName;
     
     /** The password of user that was added on the previous <tt>assign</tt> call. */
     private static String userPassword;
     
     /** The reason the previous invocation of <tt>assign</tt> or <tt>revoke</tt>
      *  failed. */
     private String failureReason;
     
     /** Logger. */
     private final ILogger logger;
     
     public WindowsNewUserAccessAction()
     {
         this.logger = LoggerFactory.getLoggerInstance();
         
         /* Fail construction on non-Windows operating systems because this
          * action makes system calls that are only present on Windows. */
         if (!System.getProperty("os.name").startsWith("Windows"))
         {
             this.logger.fatal("Unable to construct WindowsNewUserAccessAction because it only works on Windows " +
                     "operating systems. Detected operating system is '" + System.getProperty("os.name") + "'.");
             throw new IllegalStateException("WindowsNewUserAccessAction not valid on " + System.getProperty("os.name"));
         }
         
         this.random = new Random();
         
         this.rdpAccess = new RemoteDesktopAccessAction();
     }
     
     @Override
     public synchronized boolean assign(String name)
     {
         this.logger.debug("Assigning access to '" + name + "' using the " + this.getActionType() + " action.");
         
         String winName = name, winPasswd;
         int suf = 1, exitVal;
         ProcessBuilder netUserCommand = new ProcessBuilder(this.netUser);
         List<String> userComm = netUserCommand.command();
         
         try
         {
             /* Find a unique name that contains the prefix of the user name and with
              * an optionally appended number to make the name system unique. */
             userComm.add(winName);
             Process p = netUserCommand.start();
             BufferedReader rdr = new BufferedReader(new InputStreamReader(p.getInputStream()));
             while (rdr.readLine() != null);
             while (p.waitFor() == 0)
             {
                 rdr.close();
                 this.logger.debug("Windows username " + winName + " not  unique, trying " + name + suf + ".");
                 userComm.remove(winName);
                 winName = name + suf++;
                 userComm.add(winName);
                 p = netUserCommand.start();
                 rdr = new BufferedReader(new InputStreamReader(p.getInputStream()));
                 while (rdr.readLine() != null);
             }
             rdr.close();
             
             this.logger.debug("Using " + winName + " as the new user username as it unique.");
             
             /* Generate the password for the user. */
             char pwd[] = new char[8];
             for (int i = 0; i < 8; i++)
             {
                 switch (this.random.nextInt(3))
                 {
                     case 0: pwd[i] = (char) (this.random.nextInt(10) + 48); break; /* Numeric characters. */
                     case 1: pwd[i] = (char) (this.random.nextInt(26) + 65); break; /* Upper case characters. */
                     case 2: pwd[i] = (char) (this.random.nextInt(26) + 97); break; /* Lower case characters. */
                 }
             }
             winPasswd = String.valueOf(pwd);
             
             /* Add the user. */
             userComm.add(winPasswd);
             userComm.add("/ADD");
             userComm.addAll(Arrays.asList(this.userOptions));
             this.logger.debug("Command to add a new user is '" + userComm.toString() + "'.");
             
             if ((exitVal = netUserCommand.start().waitFor()) == 0)
             {
                 this.logger.info("Created new user with the username '" + winName + "' and password '" + winPasswd +
                         "'.");
                 WindowsNewUserAccessAction.userName = winName;
                 WindowsNewUserAccessAction.userPassword = winPasswd;
                 
                 /* Assign access to Remote Desktop. */
                 if (this.rdpAccess.assign(winName))
                 {
                     this.failureReason = null;
                     return true;
                 }
                 else
                 {
                     this.failureReason = this.rdpAccess.getFailureReason();
                     return false;
                 }
             }
             
             this.logger.warn("Failed creating user '" + winName + "' with net user exit code " + exitVal + '.');
             this.failureReason = "Net user for " + winName + " failed with error " + exitVal;
         }
         catch (Exception e)
         {
             this.logger.warn("Failed creating user " + winName + " with exception " + e.getClass().getSimpleName() +
                     " and message '" + e.getMessage() + "'.");
             this.failureReason = "Net user invocation failed with exception " + e.getClass().getSimpleName() + 
                     ", message " + e.getMessage();
         }
         
         return false;
     }
 
     @Override
     public synchronized boolean revoke(String name)
     {
         this.logger.debug("Assigning access to '" + name + "' using the " + this.getActionType() + " action.");
         
         final String winName = WindowsNewUserAccessAction.userName;
         
         int exitVal;
         
         /* Clear the created crendentials. */
         WindowsNewUserAccessAction.userName = null;
         WindowsNewUserAccessAction.userPassword = null;
         
         try
         {
             /* Revoke RDP access. */
             this.rdpAccess.revoke(winName);
             
             ProcessBuilder netUserCommand = new ProcessBuilder(this.netUser);
             netUserCommand.command().add(winName);
             netUserCommand.command().add("/DELETE");
             this.logger.debug("Command to delete " + winName + " is " + netUserCommand.command().toString() + ".");
             
             /* Revoke access from user by deleting the users created user. */
             if ((exitVal = netUserCommand.start().waitFor()) == 0)
             {
                 this.logger.info("Deleting user " + winName + " to revoke access from " + name + '.');
                 return true;
             }
             
             this.logger.warn("Failed deleting user " + winName + " with net user exit code " + exitVal + ".");
             this.failureReason = "net user for deleting " + winName + " failed with code " + exitVal;
         }
         catch (Exception e)
         {
             this.logger.warn("Failed deleting user " + winName + " with exception " +e.getClass().getSimpleName() + 
                     " and message '" + e.getMessage() + "'.");
             this.failureReason = "Net user invocation failed with exception " + e.getClass().getSimpleName() + 
                     ", message " + e.getMessage(); 
         }
         
         return false;
     }
 
     @Override
     public String getActionType()
     {
         return "Windows New User Access Action";
     }
 
     @Override
     public String getFailureReason()
     {
         return this.failureReason;
     }
 
     /**
      * @return the userName
      */
     public static String getUserName()
     {
         return userName;
     }
 
     /**
      * @return the userPassword
      */
     public static String getUserPassword()
     {
         return userPassword;
     }
 
 }
