 /**
  * Rig Client Commons.
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
  * @date 19th July 2010
  */
 
 package au.edu.labshare.rigclient.action.access;
 
 import java.util.Arrays;
 
 import netscape.ldap.LDAPAttribute;
 import netscape.ldap.LDAPConnection;
 import netscape.ldap.LDAPEntry;
 import netscape.ldap.LDAPException;
 import netscape.ldap.LDAPModification;
 import netscape.ldap.LDAPModificationSet;
 import netscape.ldap.LDAPSearchResults;
 import netscape.ldap.LDAPv2;
 import au.edu.labshare.rigclient.internal.SMBHash;
 import au.edu.labshare.rigclient.primitive.LoginCredentialsController;
 import au.edu.uts.eng.remotelabs.rigclient.rig.IAccessAction;
 import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.IConfig;
 import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
 import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;
 
 /**
  * Access action which replaces the stored users domain password with a 
  * generated value. The generated value is then set in the 
  * {@link LoginCredentialsController} for determination with primitive
  * control.
  * <br />
  * The required configuration of this is class is:
  * <ul>
  * <li>Ldap_Host - The address of the LDAP server.</li>
  *  <li>Ldap_Port - The connection port of the LDAP server. Optional, the 
  *  default is 389.</li>
  *  <li>Ldap_BindDN - The DN of the user to bind to the LDAP server with.
  *  This needs to be a user with permission to modify the group record.</li>
  *  <li>Ldap_Password - Password of the bind user.</li>
  *  <li>Ldap_BaseDN - The base DN of the LDAP tree to search for the 
  *  user.</li> 
  *  <li>Ldap_User_Filter - The filter used to find the user record. 
  *  '%s' is used to determine the location to put the user name.</li>
  * </ul>
  */
 public class SambaPasswordAccessAction implements IAccessAction
 {
     /** Default LDAP server port. */
     public static final int DEFAULT_LDAP_PORT = 389;
     
     /** The default user search filter. */
     public static final String DEFAULT_USER_FILTER = "(&(uid=%s)(objectclass=posixAccount))";
     
     /** User name place holder. */
     public static final String USER_NAME_PLACEHOLDER = "%s";
     
     /** Failure reason of last invocation. */
     private String failureReason;
     
     /** The connection address of the LDAP server. */
     private final String host;
     
     /** The port of the LDAP server. */
     private int port;
     
     /** The DN of the user to bind as - should have administrative privileges. */
     private final String binddn;
     
     /** The password of the bind user. */
     private final String password;
     
     /** The Base DN to searchs users from. */
     private final String basedn;
     
     /** The user record search filter. */
     private final String searchFilter;
 
     /** Logger. */
     private ILogger logger;
     
     public SambaPasswordAccessAction()
     {
         this.logger = LoggerFactory.getLoggerInstance();
 
         IConfig config = ConfigFactory.getInstance();
 
         if ((this.host = config.getProperty("Ldap_Host")) == null)
         {
             this.logger.error("LDAP server host is not configured for the " + this.getActionType() + ". The property " +
                     "'Ldap_Host' needs to be configured with the address of a LDAP server.");
             throw new IllegalStateException("LDAP host not configured");
         }
         this.logger.info("Configured LDAP server host is '" + this.host + "'.");
 
         try
         {
             this.port = Integer.parseInt(config.getProperty("Ldap_Port", "389"));
             this.logger.info("Configured LDAP server port is "  + this.port + '.');
         }
         catch (NumberFormatException ex)
         {
             this.port = LdapGroupAccessAction.DEFAULT_LDAP_PORT;
             this.logger.info("Using default LDAP server port 389.");
         }
 
         if ((this.binddn = config.getProperty("Ldap_BindDN")) == null)
         {
             this.logger.error("LDAP server bind user DN not configured for the " + this.getActionType() + 
                 ". The property 'Ldap_BindDN' needs to be configured with the DN of the bind user.");
             throw new IllegalStateException("LDAP bind user not configured.");
         }
         this.logger.info("Configured LDAP bind user is '" + this.binddn + "'.");
 
         if ((this.password = config.getProperty("Ldap_Password")) == null)
         {
             this.logger.error("LDAP password is not configured for the " + this.getActionType() + ". The property " +
                     "'Ldap_Password' needs to be configured with the bind user password.");
         }
         char starPass[] = new char[this.password.length()];
         Arrays.fill(starPass, '*');
         this.logger.info("Configured LDAP password is '" + String.valueOf(starPass) + "'.");
 
         if ((this.basedn = config.getProperty("Ldap_BaseDN")) == null)
         {
             this.logger.error("LDAP server base DN is not configured for the " + this.getActionType() + ". The property " +
                     "'Ldap_BaseDN' needs to be configured with the base of search tree.");
             throw new IllegalStateException("Base DN not configured");
         }
         this.logger.info("Configured LDAP server base DN is '" + this.basedn + "'.");
 
         this.searchFilter = config.getProperty("Ldap_User_Filter", LdapGroupAccessAction.DEFAULT_USER_FILTER);
         this.logger.info("Using LDAP user search filter '" + this.searchFilter + "'.");
         
         SMBHash.setup();
     }
 
     @Override
     public synchronized boolean assign(String name)
     {        
         LDAPConnection conn = new LDAPConnection();
         try
         {
             conn.connect(this.host, this.port, this.binddn, this.password);
             
             /* ------------------------------------------------------------------------------------
              * -- Search the user record. ---------------------------------------------------------
              * ------------------------------------------------------------------------------------*/
             String attrs[] = {"sambasid", "sambalmpassword", "sambantpassword"};
             LDAPSearchResults search = conn.search(this.basedn, LDAPv2.SCOPE_SUB, this.getUserSearchFilter(name), 
                    attrs, false);
             if (!search.hasMoreElements())
             {
                 this.logger.warn("User with name '" + name + "' not found on the LDAP server.");
                 this.failureReason = "User with name '" + name + "' not found on the LDAP server.";
                 return false;
             }
             
             LDAPEntry user = search.next();
             this.logger.debug("Found user '" + name + "' LDAP record with DN '" + user.getDN() + "'.");
             
             /* ------------------------------------------------------------------------------------
              * -- Determine the domain the user is a member of (based on sambasid). ---------------
              * ------------------------------------------------------------------------------------*/
             LDAPAttribute attr = user.getAttribute("sambasid");
             if (attr == null ||  attr.getStringValueArray().length == 0)
             {
                 this.failureReason = "Unable to determine user's Samba domain SID.";
                 this.logger.error("Unable to determine the Samba SID of the user (sambasid attribute), this means " +
                 		"they are probably not a member of a domain.");
                 return false;
             }
             
             String val[] = attr.getStringValueArray();
             String dattrs[] = {"sambadomainname"}; 
             search = conn.search(this.basedn, LDAPv2.SCOPE_SUB, "(&(objectclass=sambadomain)(sambasid=" 
                     + val[0].substring(0, val[0].lastIndexOf('-')) + "))", dattrs, false);
             
             if (!search.hasMoreElements())
             {
                 this.failureReason = "Unable to find Samba domain name.";
                 this.logger.warn("Unable to find Samba domain name (ldap filter was '(&(objectclass=sambadomain)(sambasid=" 
                     + val[0].substring(0, val[0].lastIndexOf('-')) + "))'.");
                 return false;
             }
 
             LDAPAttribute domain = search.next().getAttribute("sambadomainname");
             if (domain == null || domain.getStringValueArray().length == 0)
             {
                 this.failureReason = "Unable to find Samba domain name.";
                 this.logger.warn("Unable to find Samba domain name (ldap filter was '(&(objectclass=sambadomain)(sambasid=" 
                     + val[0].substring(0, val[0].lastIndexOf('-')) + "))'.");
                 return false;
             }
             LoginCredentialsController.setUsername(domain.getStringValueArray()[0] + '\\' + name);
             
             /* ------------------------------------------------------------------------------------
              * -- Put the Samba passwords in the 'l' (locality) field (arbitarily chosen field ----
              * -- which is multivalue). -----------------------------------------------------------
              * ------------------------------------------------------------------------------------*/
             String oldLmPass = "LM-", oldNtPass = "NT-";
             attr = user.getAttribute("sambalmpassword");
             if (attr != null)
             {
                 val = attr.getStringValueArray();
                 if (val.length > 0) oldLmPass += val[0];
             }
             
             attr = user.getAttribute("sambantpassword");
             if (attr != null)
             {
                 val = attr.getStringValueArray();
                 if (val.length > 0) oldNtPass += val[0];
             }
             
             LDAPAttribute lpass = new LDAPAttribute("l");
             lpass.addValue(oldLmPass);
             lpass.addValue(oldNtPass);
             
             LDAPModificationSet mods = new LDAPModificationSet();
             mods.add(LDAPModification.REPLACE, lpass);
             
             /* ------------------------------------------------------------------------------------
              * -- Generate and store new passwords. -----------------------------------------------
              * ------------------------------------------------------------------------------------*/
             String password = LoginCredentialsController.generatePassword();
             mods.add(LDAPModification.REPLACE, new LDAPAttribute("sambalmpassword", SMBHash.lmHash(password)));
             mods.add(LDAPModification.REPLACE, new LDAPAttribute("sambantpassword", SMBHash.ntlmHash(password)));
             
 
             conn.modify(user.getDN(), mods);
             return true;
         }
         catch (LDAPException ex)
         {
             this.failureReason = "Failed LDAP operation. Code: " + ex.getLDAPResultCode() + ", message: " 
                     + ex.getLDAPErrorMessage() + ".";
             this.logger.error(this.failureReason);            
             return false;
         }
         catch (Exception e)
         {
             this.failureReason = "Failed generating the SMB hashes of the clear text generated password (probably a bug).";
             this.logger.error(this.failureReason);
             return false;
         }
         finally
         {
             try
             {
                 if (conn.isConnected()) conn.disconnect();
             }
             catch (LDAPException ex)
             {
                 this.logger.error("Failed LDAP disconnect. Code: " + ex.getLDAPResultCode() + ", message: " 
                         + ex.getLDAPErrorMessage() + ".");
             }
         }
     }
 
     @Override
     public synchronized boolean revoke(String name)
     {
         LoginCredentialsController.clearCredentials();
         
         LDAPConnection conn = new LDAPConnection();
         try
         {
             conn.connect(this.host, this.port, this.binddn, this.password);
             
             String attrs[] = {"l"};
             LDAPSearchResults search = conn.search(this.basedn, LDAPv2.SCOPE_SUB, this.getUserSearchFilter(name), 
                    attrs, false);
             if (!search.hasMoreElements())
             {
                 this.logger.warn("User with name '" + name + "' not found on the LDAP server.");
                 this.failureReason = "User with name '" + name + "' not found on the LDAP server.";
                 return false;
             }
             
             LDAPEntry user = search.next();
             this.logger.debug("Found user '" + name + "' LDAP record with DN '" + user.getDN() + "'.");
             
             LDAPAttribute attr = user.getAttribute("l");
             if (attr == null)
             {
                 this.logger.warn("Unable to restore the " + name + "'s old password as it has not been stored (" +
                 		"expected it in 'l'). This is not treated as a failure.");
                 return true;
             }
             
             String lmpass = " ", ntpass = " ";
             for (String v : attr.getStringValueArray())
             {
                 if (v.startsWith("LM-")) lmpass = v.substring("LM-".length());
                 else if (v.startsWith("NT-")) ntpass = v.substring("NT-".length());
             }
             
             LDAPModificationSet mod = new LDAPModificationSet();
             mod.add(LDAPModification.REPLACE, new LDAPAttribute("sambalmpassword", lmpass));
             mod.add(LDAPModification.REPLACE, new LDAPAttribute("sambantpassword", ntpass));
             mod.add(LDAPModification.DELETE, new LDAPAttribute("l"));
             
             conn.modify(user.getDN(), mod);
             return true;
         }
         catch (LDAPException ex)
         {
             this.failureReason = "Failed LDAP operation. Code: " + ex.getLDAPResultCode() + ", message: " 
                     + ex.getLDAPErrorMessage() + ".";
             this.logger.error(this.failureReason);
             try
             {
                 if (conn.isConnected()) conn.disconnect();
             }
             catch (LDAPException e)
             {
                this.logger.error("Failed LDAP disconnect. Code: " + ex.getLDAPResultCode() + ", message: " 
                        + ex.getLDAPErrorMessage() + ".");
             }
            return false;
         }
     }
     
     /**
      * Substitutes the users name for the filter name placeholder.
      * 
      * @param user name of user
      * @return filter with name substituted
      */
     private String getUserSearchFilter(String user)
     {
         StringBuilder filter = new StringBuilder(this.searchFilter.length() + 8);
         
         int pos = this.searchFilter.indexOf(LdapGroupAccessAction.USER_NAME_PLACEHOLDER);
         filter.append(this.searchFilter.substring(0, pos));
         filter.append(user);
         filter.append(this.searchFilter.substring(pos + 2));
         
         return filter.toString();
     }
     
     @Override
     public String getFailureReason()
     {
         return this.failureReason;
     }
     
     @Override
     public String getActionType()
     {
        return "Samba password controller.";
     }
 }
