 /*
  * Jamm
  * Copyright (C) 2002 Dave Dribin and Keith Garner
  *  
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package jamm.backend;
 
 import java.util.Map;
 import java.util.Set;
 import java.util.List;
 import java.util.HashMap;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Collection;
 import java.util.Iterator;
 import javax.naming.NamingException;
 import javax.naming.AuthenticationException;
 import javax.naming.AuthenticationNotSupportedException;
 import javax.naming.NameAlreadyBoundException;
 import javax.naming.CommunicationException;
 import javax.naming.NoPermissionException;
 
 import jamm.ldap.LdapFacade;
 import jamm.ldap.LdapPassword;
 import jamm.ldap.PasswordScheme;
 
 /**
  * Manages the backend mail LDAP subtree.  All mail data is stored in
  * a subtree of an LDAP directory.  This manipulates that data,
  * creating, deleting, and modifying LDAP entries, but not exposing
  * any of the LDAP complexities to the user of this object.  A
  * connection is not kept around, but created as needed.
  */
 public class MailManager
 {
     /**
      * Create a mail manager using an anonymous bind. See {@link
      * #MailManager(String, int, String, String, String) the full
      * constructor} for details.
      *
      * @param host Host name of LDAP server
      * @param port The port number of LDAP server
      * @param base The base DN of the mail subtree
      */
     public MailManager(String host, int port, String base)
     {
         this(host, port, base, "", "");
     }
 
     /**
      * Create a mail manager.  The host and port number of the LDAP
      * server is supplied.  If the bind DN is an empty string (""),
      * then an anonymous bind is done, otherwise a a simple bind is
      * done using the given element DN and password.
      *
      * @param host Host name of LDAP server
      * @param port Port number of LDAP server
      * @param base The base DN of the mail subtree
      * @param bindDn The DN of the element to bind as, or the empty
      * string for an anonymous bind.
      * @param bindPassword The password of the bind element
      */
     public MailManager(String host, int port, String base, String bindDn,
                        String bindPassword)
     {
         mHost = host;
         mPort = port;
         mBase = base;
         mBindDn = bindDn;
         mBindPassword = bindPassword;
     }
 
     /**
      * Create a mail manager using the default LDAP port using an
      * anonymous bind. See {@link #MailManager(String, int, String,
      * String, String) the full constructor} for details.
      *
      * @param host Host name of LDAP server
      * @param base The base DN of the mail subtree
      */
     public MailManager(String host, String base)
     {
         this(host, 389, base, "", "");
     }
 
     /**
      * Create a mail manager using the default LDAP port. See {@link
      * #MailManager(String, int, String, String, String) the full
      * constructor} for details.
      *
      * @param host Host name of LDAP server
      * @param base The base DN of the mail subtree
      * @param bindDn The DN of the element to bind as
      * @param bindPassword The password of the bind element
      */
     public MailManager(String host, String base, String bindDn,
                        String bindPassword)
     {
         this(host, 389, base, bindDn, bindPassword);
     }
 
     /**
      * Adds a catch all alias for the specified domain.
      *
      * @param domain Domain name
      * @param destination Destination alias
      * @exception PermissionException if a permission error occurs
      * @exception MailManagerException If the catch all could not be added
      */
     public void addCatchall(String domain, String destination)
         throws PermissionException, MailManagerException
     {
         LdapFacade ldap = null;
         String catchAll = "@" + domain;
 
         try
         {
             ldap = getLdap();
             Map attributes = new HashMap();
             attributes.put("objectClass",
                            new String[] { "top", ALIAS_OBJECT_CLASS });
             attributes.put("mail", catchAll);
             attributes.put("maildrop", destination);
             attributes.put("lastChange", getUnixTimeString());
             attributes.put("accountActive", booleanToString(true));
             ldap.addElement(mailDn(catchAll), attributes);
         }
         catch (NoPermissionException e)
         {
             throw new PermissionException(
                 "No permission to modify domain " + domain, e);
         }
         catch (NamingException e)
         {
             throw new MailManagerException("Could not create catchall @" +
                                            domain, e);
         }
         finally
         {
             closeLdap(ldap);
         }
     }
     
     /**
      * Adds user given by mail postmaster powers over domain.
      *
      * @param domain The domain to grant powers in.
      * @param mail The person to give power to.
      * @exception PermissionException if a permission error occurs
      * @exception MailManagerException When a postmaster can't be added.
      */
     public void addPostmaster(String domain, String mail)
         throws PermissionException, MailManagerException
     {
         LdapFacade ldap = null;
         String pmMail = "postmaster@" + domain;
         try
         {
             ldap = getLdap();
             ldap.searchOneLevel(domainDn(domain), "mail=" + pmMail);
 
             if (ldap.nextResult())
             {
                 Set roleOccupants =
                     ldap.getAllResultAttributeValues("roleOccupant");
                 roleOccupants.add(mailDn(mail));
                 ldap.modifyElementAttribute(ldap.getResultName(),
                                             "roleOccupant", roleOccupants);
             }
         }
         catch (NoPermissionException e)
         {
             throw new PermissionException(
                 "No permission to add postmaster to " + domain, e);
         }
         catch (NamingException e)
         {
             throw new MailManagerException("Adding " + mail + " as postmaster",
                                            e);
         }
     }
 
     /**
      * Checks to see if we can bind as the specified element.
      *
      * @return True if bind as the element.
      * @throws MailManagerException If an error occured
      */
     public boolean authenticate()
         throws MailManagerException
     {
         LdapFacade ldap = null;
         boolean authenticated = false;
         
         try
         {
             ldap = getLdap();
 
             Set objectClasses =
                 ldap.getAllAttributeValues("objectClass");
 
             if (objectClasses.contains("JammMailAccount") ||
                 objectClasses.contains("JammMailAlias"))
             {
                 String active = ldap.getAttribute("accountActive");
                 if (active == null)
                 {
                     authenticated = false;
                 }
                 else
                 {
                     authenticated = Boolean.valueOf(active).booleanValue();
                 }
             }
             else
             {
                 authenticated = true;
             }
         }
         catch (AuthenticationException e)
         {
             authenticated = false;
         }
         catch (AuthenticationNotSupportedException e)
         {
             // JDK 1.4.0 throws this exception if the entry has no
             // userPassword attribute.  This still means the user
             // cannot authenticate, so it's expected in this case.
             authenticated = false;
         }
         catch (NamingException e)
         {
             throw new MailManagerException("Could not bind", e);
         }
         finally
         {
             closeLdap(ldap);
         }
 
         return authenticated;
     }
 
     /**
      * Returns a string in uppercase of TRUE or FALSE
      *
      * @param bool a <code>boolean</code> value
      * @return a <code>String</code> value
      */
     private String booleanToString(boolean bool)
     {
         return String.valueOf(bool).toUpperCase();
     }
 
     /**
      * Change the password for an email account or alias.
      *
      * @param mail Email address to change password for
      * @param newPassword new password for this account or alias
      * @exception MailManagerException if an error occurs
      */
     public void changePassword(String mail, String newPassword)
         throws MailManagerException
     {
         if (MailManagerOptions.isUsePasswordExOp())
         {
             changePasswordExOp(mail, newPassword);
         }
         else
         {
             changePasswordHash(mail, newPassword);
         }
     }
     
     /**
      * Changes the password for an email account or alias.  This does
      * not hash or encode the password, but instead uses the
      * ModifyPassword ldap extended operation to change the password.
      * To remove the password (so the user may not log in with this
      * alias or account), pass <code>null</code> for the new password.
      *
      * @param mail Email address to change password for
      * @param newPassword New password for this account or alias
      * @exception PermissionException if an permission error occurs
      * @exception MailManagerException If an error occured
      */
     private void changePasswordExOp(String mail, String newPassword)
         throws PermissionException, MailManagerException
     {
         LdapFacade ldap = null;
 
         try
         {
             ldap = getLdap();
             searchForMail(ldap, mail);
 
             String foundDn = ldap.getResultName();
 
             if (newPassword != null)
             {
                 ldap.changePassword(foundDn, newPassword);
             }
             else
             {
                 ldap.modifyElementAttribute(foundDn, "userPassword",
                                             newPassword);
             }
 
             if (foundDn.equals(mBindDn))
             {
                 mBindPassword = newPassword;
             }
         }
         catch (NoPermissionException e)
         {
             throw new PermissionException("Can't change password", e);
         }
         catch (NamingException e)
         {
             throw new MailManagerException(e);
         }
         finally
         {
             closeLdap(ldap);
         }
     }
 
     /**
      * Changes the password for an email account or alias.  This
      * hashes hashes and encodes the password.  To remove the password
      * (so the user may not log in with this alias or account), pass
      * <code>null</code> for the new password.
      *
      * @param mail Email address to change password for
      * @param newPassword New password for this account or alias
      * @exception PermissionException if an permission error occurs
      * @exception MailManagerException If an error occured
      */
     private void changePasswordHash(String mail, String newPassword)
         throws PermissionException, MailManagerException
     {
         LdapFacade ldap = null;
 
         try
         {
             ldap = getLdap();
             searchForMail(ldap, mail);
 
             String foundDn = ldap.getResultName();
             String hashedPassword;
             if (newPassword != null)
             {
                 hashedPassword = LdapPassword.hash(PasswordScheme.SSHA_SCHEME,
                                                    newPassword);
             }
             else
             {
                 hashedPassword = null;
             }
 
             ldap.modifyElementAttribute(foundDn, "userPassword",
                                         hashedPassword);
 
             if (foundDn.equals(mBindDn))
             {
                 mBindPassword = newPassword;
             }
         }
         catch (NoPermissionException e)
         {
             throw new PermissionException("Can't change password", e);
         }
         catch (NamingException e)
         {
             throw new MailManagerException(e);
         }
         finally
         {
             closeLdap(ldap);
         }
     }
     
     /**
      * Closes an LDAP facade if it is not null.  This is handy in
      * finally blocks.
      *
      * @param ldap LDAP facade to close.
      */
     private void closeLdap(LdapFacade ldap)
     {
         if (ldap != null)
         {
             ldap.close();
         }
     }
 
     /**
      * Create a new account on an existing domain.
      *
      * @param domain Domain name
      * @param account New account name
      * @param commonName common name of new account.
      * @param password Password of new account
      * @exception PermissionException if a permission error occursb
      * @exception AccountExistsException if the account exists already
      * @exception MailManagerException If the account could not be created
      */
     public void createAccount(String domain, String account, String commonName,
                               String password)
         throws PermissionException, AccountExistsException,
                MailManagerException
     {
         LdapFacade ldap = null;
         String mail = MailAddress.addressFromParts(account, domain);
         String maildn = mailDn(mail);
 
         try
         {
             ldap = getLdap();
             Map attributes = new HashMap();
             attributes.put("objectClass",
                            new String[] { "top", ACCOUNT_OBJECT_CLASS });
             attributes.put("homeDirectory",
                            MailManagerOptions.getVmailHomedir());
             attributes.put("mail", mail);
             if (commonName != null && !commonName.equals(""))
             {
                 attributes.put("cn", commonName);
             }
             attributes.put("mailbox", domain + "/" + account + "/");
             attributes.put("accountActive", booleanToString(true));
             attributes.put("lastChange", getUnixTimeString());
             attributes.put("delete", booleanToString(false));
 
             if (!MailManagerOptions.isUsePasswordExOp())
             {
                 String hashedPassword =
                     LdapPassword.hash(PasswordScheme.SSHA_SCHEME, password);
                 attributes.put("userPassword", hashedPassword);
             }
 
             ldap.addElement(maildn, attributes);
 
             if (MailManagerOptions.isUsePasswordExOp())
             {
                 ldap.changePassword(maildn, password);
             }
         }
         catch (NameAlreadyBoundException e)
         {
             throw new AccountExistsException(
                 "Cound not create account: " + mail, e);
         }
         catch (NoPermissionException e)
         {
             throw new PermissionException(
                 "No permission to create account " + mail, e);
         }
         catch (NamingException e)
         {
             throw new MailManagerException("Could not create account: " + mail,
                                            e);
         }
         finally
         {
             closeLdap(ldap);
         }
     }
 
     /**
      * Creates a new <code>AccountInfo</code> object from the current
      * result in the LDAP facade.  This assumes a search has been
      * previously done and the facade has been advanced to point to an
      * alias element.
      *
      * @param ldap LDAP facade where result exists
      * @return A new account information bean
      * @throws NamingException If the object could not be created
      * @throws MailManagerException If there is a problem looking up postmaster
      */
     private AccountInfo createAccountInfo(LdapFacade ldap)
         throws NamingException, MailManagerException
     {
         String name = ldap.getResultAttribute("mail");
         String commonName = ldap.getResultAttribute("cn");
         boolean isActive =
             stringToBoolean(ldap.getResultAttribute("accountActive"));
         boolean isAdmin = isPostmaster(name);
         String homeDirectory = ldap.getResultAttribute("homeDirectory");
         String mailbox = ldap.getResultAttribute("mailbox");
         int lastChange =
             Integer.parseInt(ldap.getResultAttribute("lastChange"));
         boolean delete = stringToBoolean(ldap.getResultAttribute("delete"));
         return new AccountInfo(name, commonName, isActive, isAdmin,
                                homeDirectory, mailbox, delete, lastChange);
     }
 
     /**
      * Create a new alias on an existing domain.  See {@link
     * #createAlias(String, String, String, String[])} for details.
      *
      * @param domain Domain name
      * @param alias Alias name
      * @param commonName common name of new alias
      * @param destinations A collection of String objects
      * @exception AccountExistsException if the alias already exists
      * @exception MailManagerException If an error occured
      */
     public void createAlias(String domain, String alias,
                             String commonName, Collection destinations)
         throws AccountExistsException, MailManagerException
     {
         createAlias(domain, alias, commonName,
                     (String []) destinations.toArray(new String[0]));
     }
 
     /**
      * Create a new alias on an existing domain.  All previous
      * destinations are replaced with this list of destinations.
      *
      * @param domain Doman mame
      * @param alias Alias name
      * @param commonName common name of the alias
      * @param destinations An array of destinations
      * @exception PermissionException if a permission error occurs
      * @exception AccountExistsException if an alias already exists
      * @exception MailManagerException If an error occured
      */
     public void createAlias(String domain, String alias, String commonName,
                             String[] destinations)
         throws PermissionException, AccountExistsException,
         MailManagerException
     {
         LdapFacade ldap = null;
         String mail = MailAddress.addressFromParts(alias, domain);
         
         try
         {
             ldap = getLdap();
 
             Map attributes = new HashMap();
             attributes.put("objectClass",
                            new String[] {"top", ALIAS_OBJECT_CLASS});
             if (commonName != null && !commonName.equals(""))
             {                           
                 attributes.put("cn", commonName);
             }
             attributes.put("mail", mail);
             attributes.put("maildrop", destinations);
             attributes.put("accountActive", booleanToString(true));
             attributes.put("lastChange", getUnixTimeString());
             ldap.addElement(mailDn(mail), attributes);
         }
         catch (NameAlreadyBoundException e)
         {
             throw new AccountExistsException(
                 "Count not create alias: " + mail, e);
         }
         catch (NoPermissionException e)
         {
             throw new PermissionException(
                 "No permission to create alias " + mail, e);
         }
         catch (NamingException e)
         {
             throw new MailManagerException("Could not create alias: " + mail,
                                            e);
         }
         finally
         {
             closeLdap(ldap);
         }
     }
 
     /**
      * Creates a new <code>AliasInfo</code> object from the current
      * result in the LDAP facade.  This assumes a search has been
      * previously done and the facade has been advanced to point to an
      * alias element.
      *
      * @param ldap LDAP facade where result exists
      * @return A new alias information bean
      * @throws NamingException If the object could not be created
      * @throws MailManagerException If their is a problem looking up postmaster
      */
     private AliasInfo createAliasInfo(LdapFacade ldap)
         throws NamingException, MailManagerException
     {
         String name = ldap.getResultAttribute("mail");
         String commonName = ldap.getResultAttribute("cn");
 
         List destinations =
             new ArrayList(ldap.getAllResultAttributeValues("maildrop"));
         Collections.sort(destinations, String.CASE_INSENSITIVE_ORDER);
         boolean isActive = stringToBoolean(
             ldap.getResultAttribute("accountActive"));
         boolean isAdmin = isPostmaster(name);
         return new AliasInfo(name, commonName, destinations, isActive, isAdmin);
     }
 
     /**
      * Creates a new domain with the specified name.  It creates a new
      * subtree for the domain as well as the postmater and abuse
      * aliases, both with no password.
      *
      * @param domain Name of the new domain
      * @exception DomainExistsException if the domain already exists
      * @exception PermissionException if a permission error occurs
      * @exception MailManagerException If an error occured
      */
     public void createDomain(String domain)
         throws DomainExistsException, PermissionException, MailManagerException
     {
         LdapFacade ldap = null;
         try
         {
             ldap = getLdap();
 
             // Create the domain
             Map attributes = new HashMap();
             attributes.put("objectClass",
                            new String[] {"top", "JammVirtualDomain"});
             attributes.put("jvd", domain);
             attributes.put("postfixTransport", "virtual:");
             attributes.put("editAccounts", "TRUE");
             attributes.put("editPostmasters", "TRUE");
             attributes.put("accountActive", "TRUE");
             attributes.put("delete", "FALSE");
             attributes.put("lastChange", getUnixTimeString());
             String domainDn = domainDn(domain);
             ldap.addElement(domainDn, attributes);
 
             // Create the postmaster
             attributes.clear();
             attributes.put("objectClass",
                            new String[] {"top", "JammPostmaster",
                                          ALIAS_OBJECT_CLASS});
             attributes.put("cn", "postmaster");
             attributes.put("mail",
                            MailAddress.addressFromParts("postmaster", domain));
             attributes.put("maildrop", "postmaster");
             attributes.put("accountActive", "TRUE");
             attributes.put("lastChange", getUnixTimeString());
             String dn = "cn=postmaster," + domainDn;
             attributes.put("roleOccupant", dn);
             ldap.addElement(dn, attributes);
 
             // Create the abuse account
             attributes.clear();
             attributes.put("objectClass",
                            new String[] {"top", ALIAS_OBJECT_CLASS});
             String mail = MailAddress.addressFromParts("abuse", domain);
             attributes.put("mail", mail);
             attributes.put("maildrop", "postmaster");
             attributes.put("lastChange", getUnixTimeString());
             attributes.put("accountActive", booleanToString(true));
             ldap.addElement(mailDn(mail), attributes);
         }
         catch (NameAlreadyBoundException e)
         {
             throw new DomainExistsException(
                 "Could not create domain: " + domain, e);
         }
         catch (NoPermissionException e)
         {
             throw new PermissionException(
                 "No permission to create domain " + domain, e);
         }
         catch (NamingException e)
         {
             throw new MailManagerException("Could not create domain: " +
                                            domain, e);
         }
         finally
         {
             closeLdap(ldap);
         }
     }
 
     /**
      * Delete the specified account.  Note: <b>this only removes the
      * account from LDAP!</b> The code is currently no different from
      * deleteAlias in implementation, so this just calls deleteAlias.
      *
      * @param mail the e-mail account to delete
      * @exception MailManagerException if an error occurs
      */
     public void deleteAccount(String mail)
         throws MailManagerException
     {
         deleteAlias(mail);
     }
 
     /**
      * Delete the specified alias.
      *
      * @param mail Email address of alias to delete
      * @exception PermissionException if a permission error occurs
      * @exception MailManagerException If the alias could not be deleted
      */
     public void deleteAlias(String mail)
         throws PermissionException, MailManagerException
     {
         LdapFacade ldap = null;
         try
         {
             if (isPostmaster(mail))
             {
                 removePostmaster(mail);
             }
             ldap = getLdap();
             searchForMail(ldap, mail);
             ldap.deleteElement(ldap.getResultName());
         }
         catch (NoPermissionException e)
         {
             throw new PermissionException(
                 "No permission to delete alias " + mail, e);
         }
         catch (NamingException e)
         {
             throw new MailManagerException(mail, e);
         }
         finally
         {
             closeLdap(ldap);
         }
     }
 
     /**
      * Removes the domain and any of its contents.
      *
      * @param domain the domain to remove
      * @exception PermissionException if a permission error occurs
      * @exception MailManagerException if an error occurs
      */
     public void deleteDomain(String domain)
         throws PermissionException, MailManagerException
     {
         LdapFacade ldap = null;
         String domainDn = domainDn(domain);
         String pmdn = "cn=postmaster," + domainDn;
         List children = new ArrayList();
 
         try
         {
             ldap = getLdap();
             ldap.searchOneLevel(domainDn, "objectClass=*");
             while (ldap.nextResult())
             {
                 children.add(ldap.getResultName());
             }
 
             Iterator i = children.iterator();
             while (i.hasNext())
             {
                 ldap.deleteElement((String) i.next());
             }
             ldap.deleteElement(domainDn);
         }
         catch (NoPermissionException e)
         {
             throw new PermissionException(
                 "No permission to delete domain " + domain, e);
         }
         catch (NamingException e)
         {
             throw new MailManagerException("Could not remove domain: " +
                                            domain, e);
         }
         finally
         {
             closeLdap(ldap);
         }
     }
             
     /**
      * Constructs the DN of a domain.
      *
      * @param domain Domain name
      * @return DN for this domain
      */
     private final String domainDn(String domain)
     {
         StringBuffer domainDn = new StringBuffer();
         domainDn.append("jvd=").append(domain).append(",").append(mBase);
         return domainDn.toString();
     }
         
     /**
      * Returns a single AccountInfo for a given e-mail address.
      *
      * @param mail an e-mail address
      * @return an AccountInfo object or null
      * @exception MailManagerException if an error occurs
      */
     public AccountInfo getAccount(String mail)
         throws MailManagerException
     {
         LdapFacade ldap = null;
         AccountInfo account = null;
         try
         {
             ldap = getLdap();
             searchForMail(ldap, mail);
             account = createAccountInfo(ldap);
         }
         catch (MailNotFoundException e)
         {
             account = null;
         }
         catch (NamingException e)
         {
             throw new MailManagerException(e);
         }
         finally
         {
             closeLdap(ldap);
         }
         return account;
     }
     
     /**
      * Returns all accounts for the specified domain as a list of
      * {@link AccountInfo} objects.
      *
      * @param domain Domain name
      * @return List of {@link AccountInfo} objects
      * @throws MailManagerException If an error occured
      */
     public List getAccounts(String domain)
         throws MailManagerException
     {
         LdapFacade ldap = null;
         List accounts = new ArrayList();
 
         try
         {
             ldap = getLdap();
             String domainDn = domainDn(domain);
             ldap.searchOneLevel(domainDn, "objectClass=" +
                                 ACCOUNT_OBJECT_CLASS);
 
             while (ldap.nextResult())
             {
                 AccountInfo account = createAccountInfo(ldap);
                 accounts.add(account);
             }
 
         }
         catch (NamingException e)
         {
             throw new MailManagerException(e);
         }
         finally
         {
             closeLdap(ldap);
         }
 
         Collections.sort(accounts, new AccountNameComparator());
         return accounts;
     }
 
     /**
      * Returns all data for the specified alias.
      *
      * @param mail Email address of an alias
      * @return The alias information bean
      * @throws MailManagerException If an error occured
      */
     public AliasInfo getAlias(String mail)
         throws MailManagerException
     {
         LdapFacade ldap = null;
         AliasInfo alias = null;
         try
         {
             ldap = getLdap();
             searchForMail(ldap, mail);
             alias = createAliasInfo(ldap);
         }
         catch (MailNotFoundException e)
         {
             alias = null;
         }
         catch (NamingException e)
         {
             throw new MailManagerException(e);
         }
         finally
         {
             closeLdap(ldap);
         }
         return alias;
     }
 
     /**
      * Returns all aliases for the specified domain as list of {@link
      * AliasInfo} objects.
      *
      * @param domain Domain name
      * @return List of {@link AliasInfo} objects
      * @throws MailManagerException If an error occured
      */
     public List getAliases(String domain)
         throws MailManagerException
     {
         LdapFacade ldap = null;
         List aliases = new ArrayList();
 
         try
         {
             ldap = getLdap();
             String domainDn = domainDn(domain);
             ldap.searchOneLevel(domainDn, "objectClass=" + ALIAS_OBJECT_CLASS);
 
             while (ldap.nextResult())
             {
                 String name = ldap.getResultAttribute("mail");
                 // Skip "special" accounts
                 if (name.startsWith("postmaster@") ||
                     name.startsWith("abuse@") ||
                     name.startsWith("@"))
                 {
                     continue;
                 }
 
                 AliasInfo alias = createAliasInfo(ldap);
                 aliases.add(alias);
             }
         }
         catch (NamingException e)
         {
             throw new MailManagerException(e);
         }
         finally
         {
             closeLdap(ldap);
         }
 
         Collections.sort(aliases, new AliasNameComparator());
         return aliases;
     }
 
     /**
      * Returns all accounts for the specified domain as a list of
      * {@link AccountInfo} objects.
      *
      * @param domain domain name
      * @return {@link List} list of {@link AccountInfo}
      * @exception MailManagerException if an error occurs
      */
     public List getDeleteMarkedAccounts(String domain)
         throws MailManagerException
     {
         String filter = "(&(objectClass=" + ACCOUNT_OBJECT_CLASS +
             ")(delete=TRUE))";
 
         return getFilteredAccounts(filter, domain);
     }
 
     /**
      * Returns a List of domains that are marked for deletion.
      *
      * @return a List object containing DomainInfo objects
      * @exception MailManagerException if an error occurs
      */
     public List getDeleteMarkedDomains()
         throws MailManagerException
     {
         String filter = "(&(objectClass=" + DOMAIN_OBJECT_CLASS + ")" +
             "(delete=TRUE))";
 
         return getFilteredDomains(filter);
     }
 
     /**
      * Gets the LDAP DN of the specified email address.  Returns
      * <code>null</code> if the address was not found.
      *
      * @param mail Email address to lookup
      * @return The LDAP DN of the address, or <code>null</code>, if
      * not found.
      * @throws MailManagerException If an error occured.
      */
     public String getDnFromMail(String mail)
         throws MailManagerException
     {
         LdapFacade ldap = null;
         String foundDn = null;
         try
         {
             ldap = getLdap();
             searchForMail(ldap, mail);
             foundDn = ldap.getResultName();
         }
         catch (NamingException e)
         {
             throw new MailManagerException(e);
         }
         catch (MailNotFoundException e)
         {
             // This is not an exceptional case for this method
             foundDn = null;
         }
         finally
         {
             closeLdap(ldap);
         }
         return foundDn;
     }
 
     /**
      *
      * @param domainName a <code>String</code> value
      * @return a <code>DomainInfo</code> value
      * @exception MailManagerException if an error occurs
      */
     public DomainInfo getDomain(String domainName)
         throws MailManagerException
     {
         List foo = getFilteredDomains("jvd=" + domainName);
         if (foo.size() > 1)
         {
             // todo Bitch about more than one domain here.
         }
 
         return (DomainInfo) foo.get(0);
     }
 
     /**
      * Returns a list of all domains that are present in this
      * directory.
      *
      * @return A list of domains, as strings
      * @throws MailManagerException If an error occured
      */
     public List getDomains()
         throws MailManagerException
     {
         return getFilteredDomains("jvd=*");
     }
 
     /**
      * Searches one level below a domain given a filter defining some
      * constraints.
      *
      * @param filter the filter to use
      * @param domain the domain to check
      * @return {@link List} of {@link AccountInfo} objects.
      * @exception MailManagerException if an error occurs
      */
     private List getFilteredAccounts(String filter, String domain)
         throws MailManagerException
     {
         LdapFacade ldap = null;
         List accounts = new ArrayList();
 
         try
         {
             ldap = getLdap();
             String domainDn = domainDn(domain);
             ldap.searchOneLevel(domainDn, filter);
 
             while (ldap.nextResult())
             {
                 AccountInfo account = createAccountInfo(ldap);
                 accounts.add(account);
             }
 
         }
         catch (NamingException e)
         {
             throw new MailManagerException(e);
         }
         finally
         {
             closeLdap(ldap);
         }
 
         Collections.sort(accounts, new AccountNameComparator());
         return accounts;
     }
 
     /**
      * Helper functions that takes a filter and returns a List of
      * DomainInfo objects based on the filter.
      *
      * @param filter A string defining an ldap filter
      * @return a List of DomainInfo objects.
      * @exception MailManagerException if an error occurs
      */
     private List getFilteredDomains(String filter)
         throws MailManagerException
     {
         LdapFacade derivedLdap = null; 
         LdapFacade domainLdap = null;
         List domains = new ArrayList();
         try
         {
             domainLdap = getLdap();
             derivedLdap = getLdap();
 
             domainLdap.searchOneLevel(mBase, filter);
             
             while (domainLdap.nextResult())
             {
                 String dn = domainLdap.getResultName();
                 String name = domainLdap.getResultAttribute("jvd");
                 boolean canEditAccounts = stringToBoolean(
                     domainLdap.getResultAttribute("editAccounts"));
                 boolean canEditPostmasters = stringToBoolean(
                     domainLdap.getResultAttribute("editPostmasters"));
                 boolean active = stringToBoolean(
                     domainLdap.getResultAttribute("accountActive"));
                 int lastChange = Integer.parseInt(
                     domainLdap.getResultAttribute("lastChange"));
                 boolean delete = stringToBoolean(
                     domainLdap.getResultAttribute("delete"));
 
                 // Get the count the best way we can.  Maybe we can
                 // make a helper that does this?
                 derivedLdap.setReturningAttributes(new String[] { "mail" });
                 derivedLdap.searchOneLevel(
                     domainDn(name), "objectClass=" + ACCOUNT_OBJECT_CLASS);
                 int accountCount = 0;
                 while (derivedLdap.nextResult())
                 {
                     accountCount++;
                 }
                 derivedLdap.searchOneLevel(
                     domainDn(name),
                     "(&(!(|(cn=postmaster)(cn=abuse)))(objectClass=" +
                         ALIAS_OBJECT_CLASS + "))");
                         
                 int aliasCount = 0;
                 while (derivedLdap.nextResult())
                 {
                     String mail = derivedLdap.getResultAttribute("mail");
                     if (mail != null && !mail.startsWith("@"))
                     {
                         aliasCount++;
                     }
                 }
 
                 domains.add(
                     new DomainInfo(name, canEditAccounts, canEditPostmasters,
                     active, delete, aliasCount, accountCount, lastChange));
             }
         }
         catch (NamingException e)
         {
             throw new MailManagerException(e);
         }
         finally
         {
             closeLdap(domainLdap);
             closeLdap(derivedLdap);
         }
 
         Collections.sort(domains, new DomainNameComparator());
         return domains;
     }
 
     /**
      * Returns all accounts for the specified domain as a list of
      * {@link AccountInfo} objects.
      *
      * @param domain Domain name
      * @return {@link List} of {@link AccountInfo} objects
      * @throws MailManagerException If an error occured
      */
     public List getInactiveAccounts(String domain)
         throws MailManagerException
     {
         String filter = "(&(objectClass=" + ACCOUNT_OBJECT_CLASS +
             ")(accountActive=FALSE))";
 
         return getFilteredAccounts(filter, domain);
     }
 
     /**
      * Returns a list of all domains that are inactive present in this
      * directory.
      *
      * @return A list of domains, as strings
      * @throws MailManagerException If an error occured
      */
     public List getInactiveDomains()
         throws MailManagerException
     {
         String filter = "(&(objectClass=" + DOMAIN_OBJECT_CLASS + ")" +
             "(accountActive=FALSE))";
 
         return getFilteredDomains(filter);
     }
 
     /**
      * Create a new LDAP facade using the specified LDAP host and
      * port.  It creates a facade with an anonymous bind if the bind
      * DN is the empty string.
      *
      * @return A new LDAP facade
      * @exception MailManagerCommunicationException If an error occured
      * @exception NamingException If an error occured
      */
     private LdapFacade getLdap()
         throws MailManagerCommunicationException, NamingException
     {
         LdapFacade ldap = null;
         boolean anon = mBindDn.equals("") && mBindPassword.equals("");
         try
         {
             ldap = new LdapFacade(mHost, mPort);
             if (anon)
             {
                 ldap.anonymousBind();
             }
             else
             {
                 ldap.simpleBind(mBindDn, mBindPassword);
             }
         }
         catch (CommunicationException e)
         {
             String message;
             if (anon)
             {
                 message = "Could not bind anonymously to ldap server";
             }
             else
             {
                 message = "Could not bind to ldap server as " + mBindDn;
             }
             throw new MailManagerCommunicationException(message, e);
         }
         return ldap;
     }
 
     /**
      * Returns the system time in seconds since the unix epoch.
      *
      * @return a long with the current time.
      */
     private long getUnixTime()
     {
         return (System.currentTimeMillis() / 1000);
     }
 
     /**
      * Returns a string representation of the system time in seconds
      * since the unix epoch.
      *
      * @return a string with the current time.
      */
     private String getUnixTimeString()
     {
         return String.valueOf(getUnixTime());
     }
 
     /**
      * Updates the accountActive flag on the hidden accounts.  (These
      * are postmaster and abuse.
      *
      * @param ldap the ldap block we're using
      * @param domainName the domain name to update
      * @param active activate or deactive accounts
      * @exception NamingException if an ldap error occurs
      * @exception MailManagerException if a mail manager error occurs
      */
     private void hiddenAccountUpdate(LdapFacade ldap, String domainName,
                                      boolean active)
         throws NamingException, MailManagerException
     {
                 AliasInfo ai = getAlias("abuse@" + domainName);
                 if (ai != null)
                 {
                     ai.setActive(active);
                     modifyAlias(ai);
                 }
 
                 String pmMail = "postmaster@" + domainName;
                 ldap.searchOneLevel(domainDn(domainName), "mail=" + pmMail);
 
                 if (ldap.nextResult())
                 {
                     ldap.modifyElementAttribute(ldap.getResultName(),
                                                 "accountActive",
                                                 booleanToString(active));
                 }
     }
 
     /**
      * Checks if this email address is an alias or an account.
      *
      * @param mail Email address
      * @return True if it is an alias, false if it is an account
      * @throws MailManagerException If an error occured
      */
     public boolean isAlias(String mail)
         throws MailManagerException
     {
         LdapFacade ldap = null;
         boolean isAlias = false;
         
         try
         {
             ldap = getLdap();
             searchForMail(ldap, mail);
             Set objectClasses =
                 ldap.getAllResultAttributeValues("objectClass");
             isAlias = objectClasses.contains(ALIAS_OBJECT_CLASS);
         }
         catch (NamingException e)
         {
             throw new MailManagerException(mail, e);
         }
         finally
         {
             closeLdap(ldap);
         }
 
         return isAlias;
     }
 
     /**
      * Checks if the specified email address is a postmaster for the
      * domain he is in.
      *
      * @param mail Email address to check
      * @return True if this is a postmaster
      * @throws MailManagerException If an error occured
      */
     public boolean isPostmaster(String mail)
         throws MailManagerException
     {
         String domain = MailAddress.hostFromAddress(mail);
         return isPostmaster(domain, mail);
     }
 
     /**
      * Returns true if mail is a postmaster for the given domain.
      * 
      *
      * @param domain The domain to check in.
      * @param mail The mail address to check for
      * @return true if a postmaster, false if not.
      * @exception MailManagerException if an error occurs
      */
     public boolean isPostmaster(String domain, String mail)
         throws MailManagerException
     {
         LdapFacade ldap = null;
         String pmFilter = "mail=postmaster@" + domain;
         boolean result = false;
 
         try
         {
             ldap = getLdap();
             ldap.searchOneLevel(domainDn(domain), pmFilter);
 
             Set roleOccupants = null;
             if (ldap.nextResult())
             {
                 roleOccupants =
                     ldap.getAllResultAttributeValues("roleOccupant");
             }
 
             if (roleOccupants != null)
             {
                 searchForMail(ldap, mail);
                 String maildn = ldap.getResultName();
                 result = roleOccupants.contains(maildn);
             }
         }
         catch (NamingException e)
         {
             throw new MailManagerException("lookup up postmaster " + mail, e);
         }
 
         return result;
     }
 
     /**
      * Constructs the DN of an email address.
      *
      * @param mail Email address
      * @return DN for this address
      */
     private final String mailDn(String mail)
     {
         String domain = MailAddress.hostFromAddress(mail);
         StringBuffer mailDn = new StringBuffer();
         mailDn.append("mail=").append(mail).append(",");
         mailDn.append(domainDn(domain));
         return mailDn.toString();
     }
 
     /**
      * Modifies an account entry based on the AccountInfo passed on.
      *
      * @param account AccountInfo with current settings.
      * @exception PermissionException if a permission error occurs
      * @exception MailManagerException if an error occurs
      */
     public void modifyAccount(AccountInfo account)
         throws PermissionException, MailManagerException
     {
         LdapFacade ldap = null;
 
         String mail = account.getName();
         String dn = mailDn(account.getName());
         String domain = MailAddress.hostFromAddress(mail);
         
         try
         {
             ldap = getLdap();
             String commonName = account.getCommonName();
             if (commonName != null && commonName.equals(""))
             {
                 account.setCommonName(null);
             }
             ldap.modifyElementAttribute(dn, "cn", account.getCommonName());
             ldap.modifyElementAttribute(dn, "accountActive",
                                         booleanToString(account.isActive()));
             if (isPostmaster(domain, mail))
             {
                 if (!account.isAdministrator())
                 {
                     removePostmaster(domain, mail);
                 }
             }
             else
             {
                 if (account.isAdministrator())
                 {
                     addPostmaster(domain, mail);
                 }
             }
             ldap.modifyElementAttribute(dn, "lastChange",
                                         getUnixTimeString());
             ldap.modifyElementAttribute(dn, "delete",
                                         booleanToString(account.getDelete()));
         }
         catch (NoPermissionException e)
         {
             throw new PermissionException(
                 "No permission to modify account " + account.getName(), e);
         }
         catch (NamingException e)
         {
             throw new MailManagerException(mail, e);
         }
         finally
         {
             closeLdap(ldap);
         }
     }
 
     /**
      * Modify an existing alias replacing existing data.
      *
      * @param alias New alias data
      * @exception PermissionException if a permission error occurs
      * @exception MailManagerException If an error occured
      */
     public void modifyAlias(AliasInfo alias)
         throws PermissionException, MailManagerException
     {
         LdapFacade ldap = null;
         String mail = alias.getName();
         String dn = mailDn(mail);
         String domain = MailAddress.hostFromAddress(mail);
 
         try
         {
             ldap = getLdap();
             String commonName = alias.getCommonName();
             if (commonName != null && commonName.equals(""))
             {
                 alias.setCommonName(null);
             }
             ldap.modifyElementAttribute(dn, "cn", alias.getCommonName());
             ldap.modifyElementAttribute(dn, "maildrop",
                                         alias.getMailDestinations());
             ldap.modifyElementAttribute(dn, "accountActive",
                                         booleanToString(alias.isActive()));
             if (isPostmaster(domain, mail))
             {
                 if (!alias.isAdministrator())
                 {
                     removePostmaster(domain, mail);
                 }
             }
             else
             {
                 if (alias.isAdministrator())
                 {
                     addPostmaster(domain, mail);
                 }
             }
 
             ldap.modifyElementAttribute(dn, "lastChange",
                                         getUnixTimeString());
         }
         catch (NoPermissionException e)
         {
             throw new PermissionException(
                 "No permission to modify alias " + alias.getName(), e);
         }
         catch (NamingException e)
         {
             throw new MailManagerException(alias.getName(), e);
         }
         finally
         {
             closeLdap(ldap);
         }
     }
 
     /**
      * Modifies the domain's capabilities out of the DomainInfo object.
      *
      * @param domain a <code>DomainInfo</code> value
      * @exception PermissionException if a permission error occurs
      * @exception MailManagerException if an error occurs
      */
     public void modifyDomain(DomainInfo domain)
         throws PermissionException, MailManagerException
     {
         LdapFacade ldap = null;
         String domainName = domain.getName();
         String domaindn = domainDn(domainName);
         try
         {
             ldap = getLdap();
             ldap.modifyElementAttribute(domaindn, "editAccounts",
                                         booleanToString(
                                             domain.getCanEditAccounts()));
             ldap.modifyElementAttribute(domaindn, "editPostmasters",
                                         booleanToString(
                                             domain.getCanEditPostmasters()));
             ldap.modifyElementAttribute(domaindn, "accountActive",
                                         booleanToString(domain.getActive()));
             ldap.modifyElementAttribute(domaindn, "lastChange",
                                         getUnixTimeString());
             ldap.modifyElementAttribute(domaindn, "delete",
                                         booleanToString(domain.getDelete()));
 
             // We should recursively set stuff inactive if inactive.  If
             // active, set abuse and postmaster to be active.
             if (domain.getActive())
             {
                 hiddenAccountUpdate(ldap, domainName, true);
             }
             else
             {
                 hiddenAccountUpdate(ldap, domainName, false);
                 
                 Iterator i = getAccounts(domainName).iterator();
                 while (i.hasNext())
                 {
                     AccountInfo ai = (AccountInfo) i.next();
                     ai.setActive(false);
                     modifyAccount(ai);
                 }
 
                 i = getAliases(domainName).iterator();
                 while (i.hasNext())
                 {
                     AliasInfo ali = (AliasInfo) i.next();
                     ali.setActive(false);
                     modifyAlias(ali);
                 }
             }
         }
         catch (NoPermissionException e)
         {
             throw new PermissionException(
                 "Can't modify domain " + domainName, e);
         }
         catch (NamingException e)
         {
             throw new MailManagerException(domainName, e);
         }
         finally
         {
             closeLdap(ldap);
         }
     }
 
     /**
      * Removes postmaster power from mail in domain that mail is in.
      *
      * @param mail the address to remove power from
      * @exception MailManagerException if an error occurs
      */
     public void removePostmaster(String mail)
         throws MailManagerException
     {
         removePostmaster(MailAddress.hostFromAddress(mail), mail);
     }
 
     /**
      * Removes postmaster power from mail in domain.
      *
      * @param domain The domain to look in.
      * @param mail The mail address to remove power from.
      * @exception PermissionException if a permission error occurs
      * @exception MailManagerException if an error occurs
      */
     public void removePostmaster(String domain, String mail)
         throws PermissionException, MailManagerException
     {
         LdapFacade ldap = null;
         String pmMail = "postmaster@" + domain;
         try
         {
             ldap = getLdap();
             ldap.searchOneLevel(domainDn(domain), "mail=" + pmMail);
 
             if (ldap.nextResult())
             {
                 Set roleOccupants =
                     ldap.getAllResultAttributeValues("roleOccupant");
                 roleOccupants.remove(mailDn(mail));
                 ldap.modifyElementAttribute(ldap.getResultName(),
                                             "roleOccupant", roleOccupants);
             }
         }
         catch (NoPermissionException e)
         {
             throw new PermissionException(
                 "No permission to remove postmaster from " + domain, e);
         }
         catch (NamingException e)
         {
             throw new MailManagerException("Removing " + mail +
                                            " as postmaster", e);
         }
     }
 
     /**
      * Searchs the subtree for an entry matching the specified email
      * address.  If the address is found, the caller can get the
      * results from the LDAP facade without calling
      * <code>nextResult()</code>.  If the address is not found, an
      * exception is thrown.
      *
      * @param ldap The LDAP facade to use for the search
      * @param mail Email address to search for
      * @throws MailNotFoundException If the address is not found
      * @throws NamingException If a JNDI error occured
      * @see jamm.ldap.LdapFacade#nextResult
      */
     private void searchForMail(LdapFacade ldap, String mail)
         throws NamingException, MailNotFoundException
     {
         ldap.searchSubtree(mBase, "mail=" + mail);
 
         if (!ldap.nextResult())
         {
             throw new MailNotFoundException(mail);
         }
     }
 
     /**
      * Change the entry to bind as.  To perform an anonymous bind, set
      * the bind DN to be the empty string ("").
      *
      * @param bindDn The DN of the element to bind as, or the empty
      * string for an anonymous bind.
      * @param bindPassword The password of the bind element.
      */
     public void setBindEntry(String bindDn, String bindPassword)
     {
         mBindDn = bindDn;
         mBindPassword = bindPassword;
     }
 
     /**
      * Returns true if string passed in is "true", ignoring case.
      * False otherwise.  If the string is null, it returns true.
      *
      * @param string a <code>String</code> value
      * @return a <code>boolean</code> value
      */
     private boolean stringToBoolean(String string)
     {
         return stringToBoolean(string, true);
     }
 
     /**
      * Returns true if string passed in is "true", ignoring case.
      * Returns the default if the string is null.  False in all other
      * cases.
      *
      * @param string a string representing a boolean
      * @param defaultValue the default boolean value to return
      * @return a boolean value
      */
     private boolean stringToBoolean(String string, boolean defaultValue)
     {
         if (string == null)
         {
             return defaultValue;
         }
 
         return Boolean.valueOf(string).booleanValue();
     }
 
     /** The base of the LDAP subtree for mail data */
     private String mBase;
     /** The DN of the element to bind as */
     private String mBindDn;
     /** The password of the bind element */
     private String mBindPassword;
     /** Host name of LDAP server. */
     private String mHost;
     /** Port number of LDAP server */
     private int mPort;
     /** Name of the object class used for accounts. */
     private static final String ACCOUNT_OBJECT_CLASS = "JammMailAccount";
     /** Name of the object class used for aliases. */
     private static final String ALIAS_OBJECT_CLASS = "JammMailAlias";
     /** Name of the object class used for domains. */
     private static final String DOMAIN_OBJECT_CLASS = "JammVirtualDomain";
 }
