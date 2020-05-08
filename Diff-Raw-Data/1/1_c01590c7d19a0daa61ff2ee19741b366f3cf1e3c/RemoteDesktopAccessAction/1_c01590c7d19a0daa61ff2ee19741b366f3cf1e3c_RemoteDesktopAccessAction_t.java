 /**
  * SAHARA Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2009, University of Technology, Sydney
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
  * @author Tania Machet (tmachet)
  * @date 16th January 2010
  *
  * Changelog:
  * - 16/01/2010 - tmachet- Initial file creation.
  */
 package au.edu.uts.eng.remotelabs.rigclient.action.access;
 
 import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
 
 /**
  * Windows Terminal Services access action. Performs the access action by
  * adding and removing users from the configurable Remote Desktop Users group 
  * which controls who may remote login to a Windows console using RDP. 
  * <p>
  * This action only works for Windows, on other platforms a 
  * {@link IllegalStateException} will be thrown on construction.
  * <p>
  * The configuration properties for RemoteDesktopAccessAction are:
  * <ul>
  *  <li><tt>Remote_Desktop_Windows_Domain</tt> - specifies the Windows/
  *  Samba domain the user is part of (i.e their name 
  *  is '\\&lt;Windows_Domain&gt;\&lt;name&gt;')
  *  <li><tt>Remote_Desktop_Groupname</tt> - the name of the user group
  *   to which the user must be added for Remote Desktop access permissions
  *  </ul> 
  *  Access is granted with the <code>assign</code> method that adds users 
  *  to the user group if they do not exist there yet.
  *  Access is revoked with the <code>revoke</code> method that removes
  *  the user from the group   
  */
 public class RemoteDesktopAccessAction extends ExecAccessAction
 {
     /** Default user group for remote desktop access. */
     public static final String DEFAULT_GROUPNAME = "Remote Desktop Users";
 
     /** Default command for changing user groups for windows */
     public static final String DEFAULT_COMMAND = "net";
 
     /** Default command for changing user groups for windows */
     public static final String DEFAULT_LOCALGROUP = "localgroup";
 
     /** Domain name. */
     private final String domainName;
     
     /** group name for user group that has remote desktop access. */
     protected String groupName;
 
     /** user name to be assigned access. */
     protected String userName;
     
    
     /**
      * Constructor.
      */
     public RemoteDesktopAccessAction() 
     {
         final String os = System.getProperty("os.name");
 
         // RDP access only valid for WIndows - chack that the OS is windows
         if (os.startsWith("Windows"))
         {
             // Get domain if it is confirgured
             this.domainName = ConfigFactory.getInstance().getProperty("Remote_Desktop_Windows_Domain");
             if (this.domainName == null)
             {
                 this.logger.info("Windows domain name not found, so not using a domain name.");
             }
             
             // Get Remote Desktop User group name
             this.groupName = ConfigFactory.getInstance().getProperty("Remote_Desktop_Groupname",RemoteDesktopAccessAction.DEFAULT_GROUPNAME);
             this.logger.debug("Remote Desktop User group is " + this.groupName);
 
             //Set up command command and arguments for remote access
             this.setupAccessAction();
             
         }
         else
         {
             throw new IllegalStateException("Remote Desktop Action is only valid for WINDOWS platforms not " + os);
         }
     }
     
     /* *
      * The action to assign users to a Remote Desktop session is done by adding them to 
      * a configurable user group that has permissions for the Remote Desktop.
      * 
      *  The user is first checked to see if it already in the group, if not, a command
      *  is set up and executed to add the user to the group, and the result verified.
      *  
      *  Additional arguments for assign are:
      *  <ul>
      *  <li> <tt> Domain </tt> - optional configurable windows domain of user
      *  <li> <tt> User Name </tt> - name of the user
      *   
      */
     @Override
     public boolean assign(String name)
     {
         final boolean failed;
         
         this.userName = name;
 
         // Check whether this user already belongs to the group, if so continue
         if(!this.checkUserInGroup())
         {
             /* Add the command argument user name (with the Domain name if it is configured) and /ADD */
             if (this.domainName != null)
             {
                 this.commandArguments.add(this.domainName + "\\" + this.userName);
             }
             else
             {
                  this.commandArguments.add(this.userName);
             }
             this.commandArguments.add("/ADD");
             this.logger.debug("Remote Desktop Access assign - arguments are"  + this.commandArguments.toString());
             
             // Execute the command ie net localgroup groupname (domain/)username /ADD
             if(!this.executeAccessAction())
             {
                 this.logger.error("Remote Desktop Access action failed, command unsuccessful");
                 failed = true;
             }
             else
             {
                 if(!this.verifyAccessAction())
                 {
                     this.logger.error("Remote Desktop Access revoke action failed, exit code is" + this.getExitCode());
                     failed = true;
                 }
                 else
                 {
                     failed = false;
                 }
             }
             
             if(failed == true)
             {
                 return false;
             }
             else
             {
                 /* Remove the command arguments user name (with the Domain name if it is configured) and /ADD */
                 if (this.domainName != null)
                 {
                     this.commandArguments.remove(this.domainName + "\\" + this.userName);
                 }
                 else
                 {
                     this.commandArguments.remove(this.userName);
                 }                
                 this.commandArguments.remove("/ADD");
                 return true;
             }
         }
         else
         {
             this.logger.info("User " + this.userName + " is already in the group " + this.groupName);
             return true;
         }
     }
 
     /**
      * Method to check that a user is not already assigned to a user group
      * before adding them.
      * 
      * 
      */
     private boolean checkUserInGroup()
     {
         /* Execute the command to determine the users in the group
         * ie net localgroup groupname */
         this.executeAccessAction();
         
         if(this.getAccessOutputString().contains(this.userName))
         {
             return true;
         }
         
         return false;
 
     }
 
     /* 
      * @see au.edu.uts.eng.remotelabs.rigclient.rig.IAccessAction#revoke(java.lang.String)
      */
     @Override
     public boolean revoke(String name)
     {
         final boolean failed;
         
         /* Add the command argument user name (with the Domain name if it is configured) and /DELETE */
         if (this.domainName != null)
         {
             this.commandArguments.add(this.domainName + "\\" + this.userName);
         }
         else
         {
              this.commandArguments.add(this.userName);
         }
         this.commandArguments.add("/DELETE");
         this.logger.debug("Remote Desktop Access revoke - arguments are"  + this.commandArguments.toString());
         
 
         // Execute the command ie net localgroup groupname (domain/)username /DELETE
         if(!this.executeAccessAction())
         {
             this.logger.error("Remote Desktop Access revoke action failed, command unsuccessful");
             failed = true;
         }
         else
         {
             if(this.checkUserInGroup())
             {
                 this.logger.error("Remote Desktop Access revoke action failed, user " + this.userName + " still in group.");
                 failed = true;
             }
             else
             {
                 failed = false;
             }
         }
 
         if(failed == true)
         {
             return false;
         }
         else
         {
             /* Remove the command arguments user name (with the Domain name if it is configured) and /DELETE */
             if (this.domainName != null)
             {
                 this.commandArguments.remove(this.domainName + "\\" + this.userName);
             }
             else
             {
                 this.commandArguments.remove(this.userName);
             }                
             this.commandArguments.remove("/DELETE");
             return true;
         }
         
     }
 
     /* 
      * @see au.edu.uts.eng.remotelabs.rigclient.rig.IAction#getFailureReason()
      */
     @Override
     public String getFailureReason()
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     /* 
      * @see au.edu.uts.eng.remotelabs.rigclient.rig.IAction#getActionType()
      */
     @Override
     public String getActionType()
     {
         return "Windows Remote Desktop Access.";
     }
     
     /**
      * Sets up access action common parts.  
      * 
      * This supplies the:
      * <ul>
      *     <li><strong>Command</strong> - The common command for assign and revoke "net"
      *     <li><strong>Command arguments</strong> - The command parameters ie localgroup",
      *     configurable remote desktop group name
      */
     @Override
     public void setupAccessAction()
     {
         
         this.command = RemoteDesktopAccessAction.DEFAULT_COMMAND;
         this.commandArguments.add(RemoteDesktopAccessAction.DEFAULT_LOCALGROUP);
         this.commandArguments.add(this.groupName);
         
     }
     
     /**
      * Verifies the result of the process.  
      * 
      * This is done using the Windows exit code recieved from the last command.
      */
     @Override
     public boolean  verifyAccessAction()
     {
         if(this.getExitCode() != 0)
         {
             this.logger.warn("Verifying Access Action, output is " + this.getAccessOutputString());
             this.logger.warn("Verifying Access Action, std error is " + this.getAccessErrorString());
 
             return false;
         }
         
         return true;
             
     }
     
 }
