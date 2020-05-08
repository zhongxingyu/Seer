 package net.cyklotron.cms.modules.actions.fixes;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
import javax.naming.NameClassPair;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.directory.Attribute;
 import javax.naming.directory.Attributes;
 import javax.naming.directory.BasicAttribute;
 import javax.naming.directory.BasicAttributes;
 import javax.naming.directory.DirContext;
 import javax.naming.directory.ModificationItem;
 import javax.naming.directory.SearchControls;
 import javax.naming.directory.SearchResult;
 
import net.cyklotron.cms.CmsDataFactory;
import net.cyklotron.cms.modules.actions.BaseCMSAction;
import net.cyklotron.cms.structure.StructureService;

 import org.jcontainer.dna.Logger;
 import org.objectledge.context.Context;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.naming.ContextFactory;
 import org.objectledge.naming.ContextHelper;
 import org.objectledge.parameters.Parameters;
 import org.objectledge.pipeline.ProcessingException;
 import org.objectledge.templating.TemplatingContext;
 import org.objectledge.web.HttpContext;
 import org.objectledge.web.mvc.MVCContext;
 
 public class BAZYNGO318LdapCleanUp
     extends BaseCMSAction
 {
     private final ContextHelper directory;
 
     private final ContextFactory contextFactory;
 
     private boolean part2Done;
 
     private final static String PREFIX = "123abcddsda";
 
     private static int LIMIT = 30;
 
     public BAZYNGO318LdapCleanUp(Logger logger, StructureService structureService,
         CmsDataFactory cmsDataFactory, ContextFactory factory)
         throws NamingException
     {
         super(logger, structureService, cmsDataFactory);
         directory = new ContextHelper(factory, "people", logger);
         contextFactory = factory;
         part2Done = false;
     }
 
     @Override
     public void execute(Context context, Parameters parameters, MVCContext mvcContext,
         TemplatingContext templatingContext, HttpContext httpContext, CoralSession coralSession)
         throws ProcessingException
     {
         final int limitOfEntries = parameters.getInt("limit", LIMIT);
         logger.info("LDAP CLEANUP RAN");
         try
         {
             DirContext dirContext = directory.getBaseDirContext();
 
             // Create the default search controls
             SearchControls ctls = new SearchControls();
             String filter = "(objectClass=cyklotronPerson)";
             ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
             // Search for objects using the filter
             NamingEnumeration<SearchResult> answer = dirContext.search("", filter, ctls);
             int proccessedCount = -1;
             while(answer.hasMore() && proccessedCount < limitOfEntries)
             {
                 proccessedCount++;
                 SearchResult result = answer.nextElement();
                 String rdn = result.getName(); // rdn: uid=xxx
                 String login = rdn.substring(4); // login: xxx
                 String newRdn = "uid=" + PREFIX + login; // uid=PREFIXxxx
 
                 javax.naming.Context userContext = (javax.naming.Context)dirContext.lookup(rdn);
                 DirContext userDirContext = (DirContext)userContext;
                 Attributes userAttributes = userDirContext.getAttributes("");
                 Attribute snAttr = userAttributes.get("sn");
                 Attribute cnAttr = userAttributes.get("cn");
                 if(snAttr != null && cnAttr != null)
                 {
                     Attributes attrs = new BasicAttributes(true); // case-ignore
                     Attribute objclass = new BasicAttribute("objectclass");
                     objclass.add("shadowAccount");
                     objclass.add("inetOrgPerson");
                     objclass.add("logonTracking");
                     attrs.put(objclass);
                     attrs.put(snAttr);
                     attrs.put(cnAttr);
 
                     javax.naming.Context newContext = dirContext.createSubcontext(newRdn, attrs);
                     processUser(userContext, newContext); // contexts are closed after processing
                     dirContext.destroySubcontext(rdn);
                     dirContext.rename(newRdn, rdn); // renames from newRdn to rdn so that PREFIX is
                                                     // gone.
                 }
 
             }
 
             // process root
             String rootRDN = "uid=root";
             String rootNewRDN = "uid=" + PREFIX + "root";
             processSystemUser(dirContext, rootRDN, rootNewRDN);
 
             // process anonymous
             String anonymousRDN = "uid=anonymous";
             String anonymousNewRDN = "uid=" + PREFIX + "anonymous";
             processSystemUser(dirContext, anonymousRDN, anonymousNewRDN);
 
             removeCyklotronSystemUsersButWithout(
                 new TreeSet<String>(Arrays.asList(rootRDN, anonymousRDN)), dirContext);
 
             if(!part2Done || parameters.isDefined("removeAliases")) // this flag is set in processSystemUser
             {
                 ContextHelper contextHelper = new ContextHelper(contextFactory, "ngo", logger);
                 DirContext ngoContext = contextHelper.getBaseDirContext();
                 // remove aliases subtree
                 DirContext aliasesContext = (DirContext)ngoContext.lookup("ou=aliases");
                 removeAllChildrenOfAliases(aliasesContext);
                 aliasesContext.close();
                 ngoContext.destroySubcontext("ou=aliases");
                 // remove entry
                 ngoContext.destroySubcontext("cn=postmaster");
             }
 
             dirContext.close();
 
         }
         catch(NamingException e)
         {
             throw new RuntimeException("cleanup failed", e);
         }
 
     }
 
     private void removeAllChildrenOfAliases(DirContext dirContext) throws NamingException
     {
         SearchControls ctls = new SearchControls();
         String filter = "(objectClass=cyklotronMailAlias)";
         ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
         NamingEnumeration<SearchResult> answer = dirContext.search("", filter, ctls);
         while(answer.hasMore())
         {
             SearchResult result = answer.nextElement();
             String rdn = result.getName();
             dirContext.destroySubcontext(rdn);
         }
     }
 
     private void removeCyklotronSystemUsersButWithout(Set<String> excludedRDNs,
         DirContext dirContext)
         throws NamingException
     {
         SearchControls searchControls = new SearchControls();
         String filterQuery = "(objectClass=cyklotronSystemUser)";
         searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
         NamingEnumeration<SearchResult> answer = dirContext.search("", filterQuery, searchControls);
         while(answer.hasMore())
         {
             SearchResult result = answer.nextElement();
             String rdn = result.getName();
             if(!excludedRDNs.contains(rdn))
             {
                 dirContext.destroySubcontext(rdn);
             }
         }
     }
 
     private void processSystemUser(DirContext dirContext, String oldRDN, String newRDN)
         throws NamingException
     {
         javax.naming.Context systemUserContext = (javax.naming.Context)dirContext.lookup(oldRDN);
         DirContext systemUserDirContext = (DirContext)systemUserContext;
         Attributes systemUserAttributes = systemUserDirContext.getAttributes(""); // get current
                                                                                   // system user
                                                                                   // attributes
         if(systemUserAttributes.get("description") == null) // process system user only once
         {
             Attributes newAttributes = new BasicAttributes(true);
             // copy uid
             newAttributes.put(systemUserAttributes.get("uid"));
             // copy password
             newAttributes.put(systemUserAttributes.get("userPassword"));
 
             // create new object classes
             Attribute objclass = new BasicAttribute("objectclass");
             objclass.add("shadowAccount");
             objclass.add("logonTracking");
             objclass.add("organizationalRole");
             newAttributes.put(objclass);
 
             // change cn attribute to current uid
             Attribute cn = new BasicAttribute("cn");
             cn.add(systemUserAttributes.get("uid").get());
             newAttributes.put(cn);
 
             // create new description attribute with value of old cn
             Attribute description = new BasicAttribute("description");
             description.add(systemUserAttributes.get("cn").get());
             newAttributes.put(description);
 
             // create new system user with prefix
             dirContext.createSubcontext(newRDN, newAttributes);
             // delete old system user
             dirContext.destroySubcontext(oldRDN);
             // remove prefix
             dirContext.rename(newRDN, oldRDN);
         }
         else
         {
             part2Done = true; // mark it so that removing aliases subtree will not create new
                               // context each time
         }
         systemUserDirContext.close();
     }
 
     private void processUser(javax.naming.Context userContext, javax.naming.Context newContext)
         throws NamingException
     {
         DirContext dirContext = (DirContext)userContext;
         DirContext newDirContext = (DirContext)newContext;
 
         List<ModificationItem> copyItems = new ArrayList<>();
 
         Attributes attributes = dirContext.getAttributes("");
         Attribute userPassword = attributes.get("userPassword");
         copyItems.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, userPassword));
 
         Collection<String> mails = getMergedMails(attributes);
         for(String mail : mails)
         {
             copyItems.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("mail",
                 mail)));
         }
         String city = getCleanedCity(attributes);
         if(city != null)
         {
             copyItems.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute("l",
                 city)));
         }
 
         Address address = getCleanedAddress(attributes);
         if(address.hasPostalCode())
         {
             copyItems.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute(
                 "postalCode", address.getPostalCode())));
         }
         if(address.hasPostalAddress())
         {
             copyItems.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, new BasicAttribute(
                 "postalAddress", address.getPostalAddress())));
         }
 
         newDirContext.modifyAttributes("",
             (ModificationItem[])copyItems.toArray(new ModificationItem[copyItems.size()]));
         newDirContext.close();
         dirContext.close();
 
     }
 
     private String getCleanedCity(Attributes attributes)
         throws NamingException
     {
         final String attrName = "l";
         Attribute attribute = attributes.get(attrName);
         if(attribute != null)
         {
             String city = (String)attribute.get();
             if(city.trim().toLowerCase().equals("miasto"))
             {
                 return null;
             }
             else
             {
                 return city;
             }
         }
         return null;
     }
 
     private Address getCleanedAddress(Attributes attributes)
         throws NamingException
     {
         final String attrName = "postalCode";
         Attribute attribute = attributes.get(attrName);
         if(attribute != null)
         {
             String postalCode = (String)attribute.get();
             if(!postalCode.trim().toLowerCase().equals("00-000"))
             {
                 Attribute postalAddressAttr = attributes.get("postalAddress");
                 if(postalAddressAttr != null)
                 {
                     return new Address(postalCode, (String)postalAddressAttr.get());
                 }
                 else
                 {
                     return new Address(postalCode);
                 }
             }
         }
         return new Address();
     }
 
     private Collection<String> getMergedMails(Attributes attributes)
         throws NamingException
     {
         final String attrName = "altMail";
         Set<String> mails = new HashSet<>();
         Attribute mailAttribute = attributes.get("mail");
         if(mailAttribute != null)
         {
             for(NamingEnumeration<?> e = mailAttribute.getAll(); e.hasMore();)
             {
                 mails.add((String)e.next());
             }
             Attribute attribute = attributes.get(attrName);
             if(attribute != null)
             {
                 for(NamingEnumeration<?> e = attribute.getAll(); e.hasMore();)
                 {
                     String altMail = (String)e.next();
                     mails.add(altMail);
                 }
             }
         }
         return mails;
     }
 
     private static class Address
     {
         private final boolean hasPostalCode;
 
         private final String postalCode;
 
         private final boolean hasPostalAddress;
 
         private final String postalAddress;
 
         public Address(String postalCode, String postalAddress)
         {
             this.postalCode = postalCode;
             this.postalAddress = postalAddress;
             this.hasPostalAddress = true;
             this.hasPostalCode = true;
         }
 
         public Address(String postalCode)
         {
             this.postalCode = postalCode;
             this.postalAddress = "";
             this.hasPostalAddress = false;
             this.hasPostalCode = true;
         }
 
         public Address()
         {
             this.postalCode = "";
             this.postalAddress = "";
             this.hasPostalAddress = false;
             this.hasPostalCode = false;
         }
 
         public String getPostalCode()
         {
             if(!hasPostalCode)
             {
                 throw new IllegalStateException("has no postalCode");
             }
             return postalCode;
         }
 
         public String getPostalAddress()
         {
             if(!hasPostalAddress)
             {
                 throw new IllegalStateException("has no postal addres");
             }
             return postalAddress;
         }
 
         public boolean hasPostalCode()
         {
             return hasPostalCode;
         }
 
         public boolean hasPostalAddress()
         {
             return hasPostalAddress;
         }
 
     }
 
 }
