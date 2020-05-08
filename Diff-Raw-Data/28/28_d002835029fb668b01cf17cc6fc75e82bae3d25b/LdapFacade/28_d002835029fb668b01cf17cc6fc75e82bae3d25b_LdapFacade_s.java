 package jamm.ldap;
 
 import java.util.Hashtable;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.Map;
import java.util.HashMap;
 import java.util.Iterator;
 
 import javax.naming.Context;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.directory.InitialDirContext;
 import javax.naming.directory.DirContext;
 import javax.naming.directory.SearchResult;
 import javax.naming.directory.SearchControls;
 import javax.naming.directory.Attributes;
 import javax.naming.directory.BasicAttributes;
 import javax.naming.directory.BasicAttribute;
 
 /**
  * This class provides an easier to use interface for LDAP on top of
  * JNDI.  The JNDI interface is very generic and can be quite
  * cumbesome to use for LDAP.
  */
 
 public class LdapFacade
 {
     /**
      * Create a new facade to a host using the default port.  See blah
      * for more information.
      *
      * @param host Host name
      */
     public LdapFacade(String host)
     {
         this(host, 389);
     }
 
     /**
      * Create a new facade to a host on a given port.  This does not
      * create a connection to the host, but all future interaction
      * will be with the host.
      *
      * @param host Host name
      * @param port LDAP port
      */
     public LdapFacade(String host, int port)
     {
         mHost = host;
         mPort = port;
         mContext = null;
         mAttributes = null;
 
         mEnvironment = new Hashtable();
         initEnvironment();
     }
 
     /**
      * Initialize the context environment to the minimal values.  The
      * bind must fill in the other values.
      */
     private void initEnvironment()
     {
         mEnvironment.clear();
         mEnvironment.put(Context.INITIAL_CONTEXT_FACTORY, 
                          "com.sun.jndi.ldap.LdapCtxFactory");
         mEnvironment.put(Context.PROVIDER_URL,
                          "ldap://" + mHost + ":" + mPort + "/");
     }
 
     /**
      * Bind anonymously.
      *
      * @throws NamingException If could not bind
      */
     public void anonymousBind()
         throws NamingException
     {
         mEnvironment.put(Context.SECURITY_AUTHENTICATION, "none");
         bind();
         mAttributes = null;
     }
 
     /**
      * Bind with simple authentication as a DN.  If successful,
      * attributes of the DN will be available.
      *
      * @param dn Distinguished name to bind as
      * @param password Password for the DN
      * @throws NamingException If could not bind
      */
     public void simpleBind(String dn, String password)
         throws NamingException
     {
         mEnvironment.put(Context.SECURITY_AUTHENTICATION, "simple");
         mEnvironment.put(Context.SECURITY_PRINCIPAL, dn);
         mEnvironment.put(Context.SECURITY_CREDENTIALS, password);
         bind();
         mAttributes = mContext.getAttributes(dn);
     }
 
     /**
      * Perform the actual bind.
      *
      * @throws NamingException If could not bind
      */
     private void bind()
         throws NamingException
     {
         mContext = new InitialDirContext(mEnvironment);
         resetSearch();
     }
 
     /**
      * Returns the distinguished name that was used to bind as.
      */
     public String getName()
         throws NamingException
     {
         return (String)
             mContext.getEnvironment().get(Context.SECURITY_PRINCIPAL);
     }
 
     /**
      * Returns the value for a given attribute name.
      *
      * @param name Attribute name
      * @return The value of the attribute
      * @throws NamingException If something goes wrong
      */
     public String getAttribute(String name)
         throws NamingException
     {
         return (String) mAttributes.get(name).get();
     }
 
     /**
      *
      */
     public Set getAllAttributeValues(String name)
         throws NamingException
     {
         HashSet values;
         NamingEnumeration valueEnumeration = null;
 
         values = new HashSet();
         try
         {
             valueEnumeration = mAttributes.get(name).getAll();
             while (valueEnumeration.hasMore())
             {
                 String value;
 
                 value = (String) valueEnumeration.next();
                 values.add(value);
             }
         }
         finally
         {
             if (valueEnumeration != null)
             {
                 valueEnumeration.close();
             }
         }
 
         return values;
     }
 
     public void setReturningAttributes(String[] attributes)
     {
         mControls.setReturningAttributes(attributes);
     }
 
     public void addElement(String distinguishedName, Attributes attributes)
         throws NamingException
     {
         DirContext subcontext;
 
         subcontext = mContext.createSubcontext(distinguishedName, attributes);
         subcontext.close();
     }
 
     /**
      * Adds a new element with the attributes specified in a Map.
      * Each value of the map can either be a String, String[], or Set.
      * If the value is either a String[] or Set, then all entries are
      * added for that value.  Here is an example:
      * <pre>
      * Set attributes = new HashSet();
      * attributes.add("objectClass", new String[] { "top", "organization" });
     * attributes.add("o", "myOrg");
      * Set phones = new HashSet();
      * phones.add("555-1234");
      * phones.add("555-6789");
      * attributes.add("telephoneNumber", phones);
      * attributes.add("description", "Sample Organization");
      * ldapFacade.addElement("o=myOrg,dc=example,dc=com", attributes);
      * </pre>
      *
      * @param dn DN to add
      * @param attributes Attributes for new element
      * @throws NamingException if element could not be added
      */
     public void addElement(String dn, Map attributes)
         throws NamingException
     {
         Attributes jndiAttributes = new BasicAttributes();
         Iterator attributeNames = attributes.keySet().iterator();
         while (attributeNames.hasNext())
         {
             String name = (String) attributeNames.next();
             Object value = attributes.get(name);
             if (value instanceof String)
             {
                 jndiAttributes.put(name, value);
             }
             else if (value instanceof Set)
             {
                 Set values = (Set) value;
                 BasicAttribute attribute = new BasicAttribute(name);
                 Iterator i = values.iterator();
                 while (i.hasNext())
                 {
                     attribute.add(i.next());
                 }
                 jndiAttributes.put(attribute);
             }
             else
             {
                 String[] values = (String[]) value;
                 BasicAttribute attribute = new BasicAttribute(name);
                 for (int i = 0; i < values.length; i++)
                 {
                     attribute.add(values[i]);
                 }
                 jndiAttributes.put(attribute);
             }
         }
         addElement(dn, jndiAttributes);
     }
 
     /**
      * Replace a value of an attribute with a new value.
      *
      * @param dn DN of element to modify
      * @param attributeName Attribute to modify
      * @param newValue The new value.
      * @throws NamingException if the attribute could not be modified
      */
     public void modifyElementAttribute(String dn, String attributeName,
                                        String newValue)
         throws NamingException
     {
         BasicAttributes attributes = new BasicAttributes();
         attributes.put(attributeName, newValue);
         mContext.modifyAttributes(dn, DirContext.REPLACE_ATTRIBUTE,
                                   attributes);
     }
 
     public void deleteElement(String distinguishedName)
         throws NamingException
     {
         mContext.destroySubcontext(distinguishedName);
     }
 
     public void resetSearch()
     {
         mControls = new SearchControls();
         mSearchBase = "";
         mResults = null;
         mCurrentResultElement = null;
         mCurrentResultAttributes = null;
     }
 
     public void searchOneLevel(String base, String filter)
         throws NamingException
     {
         mSearchBase = base;
         mControls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
         mResults = mContext.search(mSearchBase, filter, mControls);
     }
 
     public void searchSubtree(String base, String filter)
         throws NamingException
     {
         mSearchBase = base;
         mControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
         mResults = mContext.search(mSearchBase, filter, mControls);
     }
 
     public boolean nextResult()
         throws NamingException
     {
         boolean hasMore;
 
         hasMore = mResults.hasMore();
         if (hasMore)
         {
             mCurrentResultElement = (SearchResult) mResults.next();
             mCurrentResultAttributes = mCurrentResultElement.getAttributes();
         }
         
         return hasMore;
     }
 
     public String getResultAttribute(String name)
         throws NamingException
     {
         return (String) mCurrentResultAttributes.get(name).get();
     }
 
     public NamingEnumeration getAllResultAttributes()
         throws NamingException
     {
         return mCurrentResultAttributes.getAll();
     }
 
     public String getResultName()
         throws NamingException
     {
         String relativeDn;
 
         relativeDn = mCurrentResultElement.getName();
         if (relativeDn.equals(""))
         {
             return mSearchBase;
         }
         else
         {
             return  relativeDn + "," + mSearchBase;
         }
     }
 
     /**
      *
      */
     public Set getAllResultAttributeValues(String name)
         throws NamingException
     {
         HashSet values;
         NamingEnumeration valueEnumeration = null;
 
         values = new HashSet();
         try
         {
             valueEnumeration = mCurrentResultAttributes.get(name).getAll();
             while (valueEnumeration.hasMore())
             {
                 String value;
 
                 value = (String) valueEnumeration.next();
                 values.add(value);
             }
         }
         finally
         {
             if (valueEnumeration != null)
             {
                 valueEnumeration.close();
             }
         }
 
         return values;
     }
 
     public void close()
     {
         try
         {
             if (mContext != null)
             {
                 resetSearch();
                 mContext.close();
                 mContext = null;
                 initEnvironment();
             }
         }
         catch (NamingException e)
         {
             e.printStackTrace();
         }
     }
 
     private String mHost;
     private int mPort;
 
     private Hashtable mEnvironment;
     private DirContext mContext;
     private Attributes mAttributes;
 
     private SearchControls mControls;
     private NamingEnumeration mResults;
     private SearchResult mCurrentResultElement;
     private Attributes mCurrentResultAttributes;
     private String mSearchBase;
 }
