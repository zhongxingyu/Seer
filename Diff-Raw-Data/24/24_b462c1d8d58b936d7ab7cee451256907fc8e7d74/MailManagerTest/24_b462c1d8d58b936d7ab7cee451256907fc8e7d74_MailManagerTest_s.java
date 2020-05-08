 package jamm.backend;
 
 import java.util.Set;
 import java.util.List;
 import java.util.HashSet;
 import javax.naming.NamingException;
 
 import junit.framework.TestCase;
 
 import jamm.ldap.LdapFacade;
 import jamm.LdapConstants;
 
 public class MailManagerTest extends TestCase
 {
     public MailManagerTest(String name)
     {
         super(name);
     }
 
     protected void setUp()
     {
         mLdap = null;
     }
 
     protected void tearDown()
     {
         if (mLdap != null)
         {
             mLdap.close();
             mLdap = null;
         }
     }
 
     public void testCreateDomain()
         throws NamingException, MailManagerException
     {
         MailManager manager;
         Set expectedObjectClass;
         Set objectClass;
 
         String domain = "domain3.test";
         String domainDn = "jvd=" + domain + "," + BASE;
 
         manager = new MailManager("localhost", BASE, LdapConstants.MGR_DN,
                                   LdapConstants.MGR_PW);
         manager.createDomain(domain);
 
         mLdap = new LdapFacade("localhost");
         mLdap.anonymousBind();
         mLdap.searchOneLevel(BASE, "jvd=" + domain);
         assertTrue("jvd=" + domain + " hasn't been created",
                    mLdap.nextResult());
 
         mLdap.searchOneLevel(domainDn, "objectClass=*");
         int counter = 0;
         while (mLdap.nextResult())
         {
             counter++;
             if (mLdap.getResultName().equals("cn=postmaster," + domainDn))
             {
                 assertEquals("Checking postmaster cn",
                              "postmaster",
                              mLdap.getResultAttribute("cn"));
                 assertEquals("Checking postmaster mail",
                              "postmaster@" + domain,
                              mLdap.getResultAttribute("mail"));
                 assertEquals("Checking postmaster maildrop",
                              "postmaster",
                              mLdap.getResultAttribute("maildrop"));
 
                 expectedObjectClass = new HashSet();
                 expectedObjectClass.add("top");
                 expectedObjectClass.add("organizationalRole");
                 expectedObjectClass.add("JammMailAlias");
                 objectClass = mLdap.getAllResultAttributeValues("objectClass");
                 assertEquals("Checking postmaster objectClass",
                              expectedObjectClass, objectClass);
             }
             if (mLdap.getResultName().equals("mail=abuse@" + domain))
             {
                 assertEquals("Checking abuse mail",
                              "abuse@" + domain,
                              mLdap.getResultAttribute("mail"));
                 assertEquals("Checking abuse maildrop",
                              "postmaster",
                              mLdap.getResultAttribute("maildrop"));
                 expectedObjectClass = new HashSet();
                 expectedObjectClass.add("top");
                 expectedObjectClass.add("JammMailAlias");
                 objectClass = mLdap.getAllResultAttributeValues("objectClass");
                 assertEquals("Checking abuse objectClass",
                              expectedObjectClass, objectClass);
             }
         }
         assertEquals("Checking if we have the right amount of results",
                      2, counter);
         
         mLdap.close();
     }
 
     public void testCreateAlias()
         throws NamingException, MailManagerException
     {
         MailManager manager;
         String domain;
         String domainDn;
         String aliasName;
         String email;
         Set expectedObjectClass;
         Set objectClass;
 
         domain = "aliadomainDn.test";
         domainDn = "jvd=" + domain + "," + BASE;
         manager = new MailManager("localhost", BASE, LdapConstants.MGR_DN,
                                   LdapConstants.MGR_PW);
         manager.createDomain(domain);
 
         aliasName = "alias";
         manager.createAlias(domain, aliasName, new String[] {"postmaster"});
 
         email = aliasName + "@" + domain;
 
         mLdap = new LdapFacade("localhost");
         mLdap.anonymousBind();
         mLdap.searchOneLevel(domainDn, "mail=" + email);
 
         assertTrue("Checking for alias", mLdap.nextResult());
         assertEquals("Checking alias mail", email,
                      mLdap.getResultAttribute("mail"));
         assertEquals("Checking alias maildrop", "postmaster",
                      mLdap.getResultAttribute("maildrop"));
         assertTrue("Checking for no more aliases", !mLdap.nextResult());
 
         expectedObjectClass = new HashSet();
         expectedObjectClass.add("top");
         expectedObjectClass.add("JammMailAlias");
         objectClass = mLdap.getAllResultAttributeValues("objectClass");
         assertEquals("Checking alias object classes", expectedObjectClass,
                      objectClass);
 
         mLdap.close();
     }
 
     public void testModifyAlias()
         throws NamingException, MailManagerException
     {
         String domain = "modify-alias.test";
         MailManager manager =
             new MailManager("localhost", BASE, LdapConstants.MGR_DN,
                             LdapConstants.MGR_PW);
 
         String aliasName = "alias";
         String aliasMail = aliasName + "@" + domain;
         manager.createDomain(domain);
         manager.createAlias(domain, aliasName,
                             new String[] {"mail1@abc.test"});
         manager.modifyAlias(domain, aliasName,
                             new String[] {"mail2@xyz.test", "mail3@mmm.test"});
 
         mLdap = new LdapFacade("localhost");
         mLdap.anonymousBind();
         mLdap.searchSubtree(BASE, "mail=" + aliasMail);
         assertTrue("Checking for a result", mLdap.nextResult());
         Set expectedMaildrops = new HashSet();
         expectedMaildrops.add("mail2@xyz.test");
         expectedMaildrops.add("mail3@mmm.test");
         assertEquals("Checking alias mail", expectedMaildrops,
                      mLdap.getAllResultAttributeValues("maildrop"));
     }
 
     public void testGetAliasDestinations()
         throws MailManagerException
     {
         String domain = "get-alias-dest.test";
         MailManager manager =
             new MailManager("localhost", BASE, LdapConstants.MGR_DN,
                             LdapConstants.MGR_PW);
 
         String aliasName = "alias";
         String aliasMail = aliasName + "@" + domain;
         manager.createDomain(domain);
         manager.createAlias(domain, aliasName,
                             new String[] {"mail2@xyz.test", "mail1@abc.test"});
 
         String[] destinations = manager.getAliasDestinations(aliasMail);
         assertEquals("Checking number of destinations", 2,
                      destinations.length);
         assertEquals("Checking destination", "mail1@abc.test",
                      destinations[0]);
         assertEquals("Checking destination", "mail2@xyz.test",
                      destinations[1]);
     }
 
     public void testCreateAccount()
         throws NamingException, MailManagerException
     {
         MailManager manager;
         String domain = "accountdomain.test";
         String domainDn = "jvd=" + domain + "," + BASE;
         String accountName;
         String accountPassword;
         String email;
         Set expectedObjectClass;
         Set objectClass;
 
         domain = "accountdomain.test";
         domainDn = "jvd=" + domain + "," + BASE;
         manager = new MailManager("localhost", BASE, LdapConstants.MGR_DN,
                                   LdapConstants.MGR_PW);
         manager.createDomain(domain);
 
         accountName = "account";
         accountPassword = "account1pw";
         manager.createAccount(domain, accountName, accountPassword);
 
         email = accountName + "@" + domain;
 
         mLdap = new LdapFacade("localhost");
         mLdap.anonymousBind();
         mLdap.searchOneLevel(domainDn, "mail=" + email);
 
         assertTrue("Checking for account", mLdap.nextResult());
         assertEquals("Checking account mail", email,
                      mLdap.getResultAttribute("mail"));
         assertEquals("Checking account homeDirectory",
                      "/home/vmail/domains",
                      mLdap.getResultAttribute("homeDirectory"));
         assertEquals("Checking account mailbox",
                      domain + "/" + accountName + "/",
                      mLdap.getResultAttribute("mailbox"));
         assertTrue("Checking for no more account results",
                    !mLdap.nextResult());
 
         expectedObjectClass = new HashSet();
         expectedObjectClass.add("top");
         expectedObjectClass.add("JammMailAccount");
         objectClass = mLdap.getAllResultAttributeValues("objectClass");
         assertEquals("Checking alias objectClass", expectedObjectClass,
                      objectClass);
 
         mLdap.close();
 
         // Make sure we can bind as this new user
         mLdap.simpleBind("mail=" + email + "," + domainDn,
                          accountPassword);
         mLdap.close();
     }
 
     public void testAuthenticate()
         throws MailManagerException
     {
         String domain = "authenticatedomain.test";
         String domainDn = "jvd=" + domain + "," + BASE;
 
         MailManager manager =
             new MailManager("localhost", BASE, LdapConstants.MGR_DN,
                             LdapConstants.MGR_PW);
         manager.createDomain(domain);
 
         String accountName = "account";
         String accountPassword = "account1pw";
         manager.createAccount(domain, accountName, accountPassword);
 
         String mail = accountName + "@" + domain;
         String accountDn = "mail=" + mail + "," + domainDn;
 
         // Create a manager against the new account
         manager.setBindEntry(accountDn, accountPassword);
         assertTrue("Checking authentication of " + accountDn,
                    manager.authenticate());
 
         manager.setBindEntry(accountDn, "bad password");
         assertTrue("Checking non-authentication of " + accountDn,
                    !manager.authenticate());
 
         manager.setBindEntry(accountDn, "");
         assertTrue("Checking non-authentication of " + accountDn,
                    !manager.authenticate());
     }
 
     public void testFindByMail()
         throws MailManagerException
     {
         String domain = "find-by-mail-domain.test";
         String domainDn = "jvd=" + domain + "," + BASE;
 
         MailManager manager =
             new MailManager("localhost", BASE, LdapConstants.MGR_DN,
                             LdapConstants.MGR_PW);
         manager.createDomain(domain);
 
         String accountName = "account";
         String accountPassword = "account1pw";
         manager.createAccount(domain, accountName, accountPassword);
 
         String mail = accountName + "@" + domain;
         String accountDn = "mail=" + mail + "," + domainDn;
 
         String foundDn = manager.findByMail(mail);
         assertEquals("Checking found DN", accountDn, foundDn);
 
         String unknownMail = "no_account@" + domain;
         foundDn = manager.findByMail(unknownMail);
         assertNull("Checking DN not found for " + unknownMail, foundDn);
     }
 
     public void testAddCatchall()
         throws NamingException, MailManagerException
     {
         MailManager manager;
         String domain = "catchalldomain.test";
         String domainDn = "jvd=" + domain + "," + BASE;
         Set expectedObjectClass;
         Set objectClass;
 
         manager = new MailManager("localhost", BASE, LdapConstants.MGR_DN,
                                   LdapConstants.MGR_PW);
         manager.createDomain(domain);
 
         manager.addCatchall(domain, "postmaster");
 
         mLdap = new LdapFacade("localhost");
         mLdap.anonymousBind();
         mLdap.searchOneLevel(domainDn, "mail=@" + domain);
 
         assertTrue("Checking for catchall", mLdap.nextResult());
         assertEquals("Checking catchall mail", "@" + domain,
                      mLdap.getResultAttribute("mail"));
         assertEquals("Checking catchall maildrop", "postmaster",
                      mLdap.getResultAttribute("maildrop"));
 
         objectClass = mLdap.getAllResultAttributeValues("objectClass");
         expectedObjectClass = new HashSet();
         expectedObjectClass.add("top");
         expectedObjectClass.add("JammMailAlias");
         assertEquals("Checking alias objectClass", expectedObjectClass,
                      objectClass);
         
         assertTrue("Checking for lack of additional catchall",
                    !mLdap.nextResult());
 
         mLdap.close();
     }
 
     public void testChangePassword()
         throws MailManagerException
     {
         String domain = "chpasswd.test";
         String domainDn = "jvd=" + domain + "," + BASE;
 
         MailManager manager =
             new MailManager("localhost", BASE, LdapConstants.MGR_DN,
                             LdapConstants.MGR_PW);
         manager.createDomain(domain);
 
         String postmasterMail = "postmaster@" + domain;
        String postmasterDn = "cn=postmaster," +domainDn;
         String postmasterPassword = "pm1";
         manager.changePassword(postmasterMail, postmasterPassword);
         manager.setBindEntry(postmasterDn, postmasterPassword);
         assertTrue("Checking postmatser can authenticate",
                    manager.authenticate());
         manager.setBindEntry(LdapConstants.MGR_DN, LdapConstants.MGR_PW);
 
         String accountName = "account";
         String originalPassword = "account1pw";
         String newPassword1 = "changed pw";
         String newPassword2 = "another pw";
         String mail = accountName + "@" + domain;
         String accountDn = "mail=" + mail + "," + domainDn;
 
         manager.createAlias(domain, accountName,
                             new String[] {"mail1@abc.com", "mail2@xyz.com"});
         manager.changePassword(mail, newPassword1);
 
         manager.setBindEntry(accountDn, newPassword1);
         assertTrue("Checking authentication using new password 1",
                    manager.authenticate());
         manager.changePassword(mail, newPassword2);
         assertTrue("Checking authentication using new password 2",
                    manager.authenticate());
         manager.setBindEntry(accountDn, originalPassword);
         assertTrue("Checking non-authentication using original password",
                    !manager.authenticate());
         manager.setBindEntry(accountDn, newPassword1);
         assertTrue("Checking non-authentication using new password 1",
                    !manager.authenticate());
         manager.setBindEntry(accountDn, newPassword2);
         assertTrue("Double checking new password 2",
                    manager.authenticate());
     }
 
     public void testIsAlias()
         throws MailManagerException
     {
         String domain = "is-alias.test";
         String domainDn = "jvd=" + domain + "," + BASE;
 
         MailManager manager =
             new MailManager("localhost", BASE, LdapConstants.MGR_DN,
                             LdapConstants.MGR_PW);
         manager.createDomain(domain);
 
         String accountName = "account";
         String accountPassword = "accountpw";
         manager.createAccount(domain, accountName, accountPassword);
         assertTrue("Checking account is not an alias",
                    !manager.isAlias(accountName + "@" + domain));
 
         String aliasName = "alias";
         manager.createAlias(domain, aliasName, new String[] {"a@b.c"});
         assertTrue("Checking alias is an alias",
                    manager.isAlias(aliasName + "@" + domain));
     }
 
     public void testIsPostmaster()
         throws MailManagerException
     {
         String domain = "is-pm.test";
         String domainDn = "jvd=" + domain + "," + BASE;
 
         MailManager manager =
             new MailManager("localhost", BASE, LdapConstants.MGR_DN,
                             LdapConstants.MGR_PW);
         manager.createDomain(domain);
 
         String aliasName = "alias";
         manager.createAlias(domain, aliasName, new String[] {"a@b.c"});
 
         assertTrue("Checking postmaster has postmaster privileges",
                    manager.isPostmaster("postmaster@" + domain));
         assertTrue("Checking alias does NOT have postmaster privileges",
                    !manager.isPostmaster(aliasName + "@" + domain));
     }
 
     public void testGetInfo()
         throws MailManagerException
     {
         String domain = "info.test";
         String domainDn = "jvd=" + domain + "," + BASE;
 
         MailManager manager =
             new MailManager("localhost", BASE, LdapConstants.MGR_DN,
                             LdapConstants.MGR_PW);
         manager.createDomain(domain);
         manager.changePassword("postmaster@" + domain, "pm");
 
         // Create some accounts
         manager.createAccount(domain, "zzz", "zzz");
         manager.createAccount(domain, "aaa", "aaa");
         manager.createAccount(domain, "MMM", "MMM");
 
         // Create some aliases
         manager.createAlias(domain, "zzzz", new String[]
             { "z@z.test", "M@z.test", "a@z.test"});
         manager.changePassword("zzzz@" + domain, "zzzz");
         manager.createAlias(domain, "MMMM", new String[]
             { "z@M.test", "M@M.test", "a@cMtest"});
         manager.changePassword("MMMM@" + domain, "MMMM");
         manager.createAlias(domain, "aaaa", new String[]
             { "z@a.test", "M@a.test", "a@a.test"});
         manager.changePassword("aaaa@" + domain, "aaaa");
         manager.createAlias(domain, "xxxx", new String[]
             { "z@x.test", "M@x.test", "a@x.test"});
         manager.changePassword("xxxx@" + domain, "xxxx");
 
         List accounts = manager.getAccounts(domain);
         assertEquals("Checking number of accounts", 3, accounts.size());
         AccountInfo account = (AccountInfo) accounts.get(0);
         assertEquals("Checking name for account[0]",
                      "aaa@" + domain, account.getName());
         account = (AccountInfo) accounts.get(1);
         assertEquals("Checking name for account[1]",
                      "MMM@" + domain, account.getName());
         account = (AccountInfo) accounts.get(2);
         assertEquals("Checking name for account[2]",
                      "zzz@" + domain, account.getName());
 
         List aliases = manager.getAliases(domain);
         assertEquals("Checking number of aliases", 4, aliases.size());
     }
 
     private LdapFacade mLdap;
     private static final String BASE = "o=hosting,dc=jamm,dc=test";
 }
