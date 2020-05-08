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
 
 package jamm.ldap;
 
 import java.util.Hashtable;
 import java.util.Set;
 import java.util.Map;
 import java.util.Iterator;
 import java.util.Collection;
 
 import javax.naming.Context;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.AuthenticationException;
 import javax.naming.directory.InitialDirContext;
 import javax.naming.directory.DirContext;
 import javax.naming.directory.SearchResult;
 import javax.naming.directory.SearchControls;
 import javax.naming.directory.Attribute;
 import javax.naming.directory.Attributes;
 import javax.naming.directory.BasicAttributes;
 import javax.naming.directory.BasicAttribute;
 
 import jamm.util.CaseInsensitiveStringSet;
 
 /**
  * Provides an easier to use interface for LDAP on top of JNDI.  The
  * JNDI interface is very generic and can be quite cumbesome to use
  * for LDAP.
  */
 public class LdapFacade
 {
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
      * Create a new facade to a host using the default port.  See
      * {@link #LdapFacade(String, int)} for more information.
      *
      * @param host Host name
      */
     public LdapFacade(String host)
     {
         this(host, 389);
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
      * attributes of the DN will be available.  This differs from the
      * JNDI bind in that an empty or null password results in an
      * <code>AuthenticationException</code>, where as JNDI will
      * continue with the bind, but falling back to an anonymous bind.
      *
      * @param dn Distinguished name to bind as
      * @param password Password for the DN
      * @throws AuthenticationException The DN and password do not
      * match (including empty and null passwords)
      * @throws NamingException If could not bind
      */
     public void simpleBind(String dn, String password)
         throws AuthenticationException, NamingException
     {
         // A password that is null or an empty string causes JNDI to
         // fall back to an anonymous bind.  This is supposed to be
         // helpful since LDAP requires a password.  But, it is a
         // little misleading and can cause problems, esp. when taking
         // passwords from an end user.  Force an authentication
         // exception in this case.
         if ((password == null) || (password.equals("")))
         {
             throw new AuthenticationException("Empty password");
         }
 
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
      *
      * @return The DN used to bind as.
      * @throws NamingException If an error occured
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
      * @throws NamingException If an error occured
      */
     public String getAttribute(String name)
         throws NamingException
     {
         return attributeToString(mAttributes.get(name));
     }
 
     /**
      * Converts a single-valued attribute to a string.  If attribute
      * is <code>null</code> then <code>null</code> is returned.  If
      * the attribute is a byte array, the string representation of the
      * byte array is returned.
      *
      * @param attribute Attribute to convert
      * @return The string represtentation of the attribute
     * @throws NamingExceptin If an error occured
      * @see String#String(byte[])
      */
     private String attributeToString(Attribute attribute)
         throws NamingException
     {
         if (attribute == null)
         {
             return null;
         }
 
         Object value = attribute.get();
         if (value instanceof byte[])
         {
             byte[] byteArray = (byte[]) value;
             return new String(byteArray);
         }
         else
         {
             return (String) value;
         }
     }
 
     /**
      * Returns all values of an attribute as a <code>Set</code>.  The
      * returned set is actually a
      * <code>CaseInsensitiveStringSet</code>.  Normally this will not
      * matter, but it will become apparent if comparing the whole set
      * to, say, a <code>HashSet</code> of normal <code>String</code>
      * objects.
      *
      * @param name An attribute name
      * @return The values of an attribute
      * @throws NamingException If an error occured
      * @see CaseInsensitiveStringSet
      */
     public Set getAllAttributeValues(String name)
         throws NamingException
     {
         Set values;
         NamingEnumeration valueEnumeration = null;
 
         values = new CaseInsensitiveStringSet();
         try
         {
             Attribute attribute = mAttributes.get(name);
             if (attribute != null)
             {
                 valueEnumeration = attribute.getAll();
                 while (valueEnumeration.hasMore())
                 {
                     String value;
 
                     value = (String) valueEnumeration.next();
                     values.add(value);
                 }
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
 
     /**
      * Sets the attributes to returned during the directory lookup.
      * By default, all attributes are returned.
      *
      * @param attributes Array of attributes to return.
      */
     public void setReturningAttributes(String[] attributes)
     {
         mControls.setReturningAttributes(attributes);
     }
 
     /**
      * Creates a new element.
      *
      * @param distinguishedName Full DN of the new element
      * @param attributes Attributes of the new element
      * @throws NamingException If element could not be added
      */
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
      * // Set a multi-value attribute with a String[]
      * attributes.add("objectClass", new String[] { "top", "organization" });
      * // Set a multi-value attribute with a Set
      * Set phones = new HashSet();
      * phones.add("555-1234");
      * phones.add("555-6789");
      * attributes.add("telephoneNumber", phones);
      * // Set single value attributes with a String
      * attributes.add("o", "myOrg");
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
 
     /**
      * Replace a multi-valued attribute with new values.
      *
      * @param dn DN of element to modify
      * @param attributeName Attribute to modify
      * @param newValues The new values
      * @throws NamingException If the attribute could not be modified
      */
     public void modifyElementAttribute(String dn, String attributeName,
                                        Collection newValues)
         throws NamingException
     {
         BasicAttributes attributes = new BasicAttributes();
         BasicAttribute attribute = new BasicAttribute(attributeName);
         Iterator i = newValues.iterator();
         while (i.hasNext())
         {
             attribute.add(i.next());
         }
         attributes.put(attribute);
         mContext.modifyAttributes(dn, DirContext.REPLACE_ATTRIBUTE,
                                   attributes);
     }
 
     /**
      * Replace a multi-valued attribute with new values.
      *
      * @param dn DN of element to modify
      * @param attributeName Attribute to modify
      * @param newValues The new values
      * @throws NamingException If the attribute could not be modified
      */
     public void modifyElementAttribute(String dn, String attributeName,
                                        String[] newValues)
         throws NamingException
     {
         BasicAttributes attributes = new BasicAttributes();
         BasicAttribute attribute = new BasicAttribute(attributeName);
         for (int i = 0; i < newValues.length; i++)
         {
             attribute.add(newValues[i]);
         }
         attributes.put(attribute);
         mContext.modifyAttributes(dn, DirContext.REPLACE_ATTRIBUTE,
                                   attributes);
     }
 
     /**
      * Deletes an element from the directory.
      *
      * @param distinguishedName DN to delete
      * @throws NamingException If the element could not be deleted
      */
     public void deleteElement(String distinguishedName)
         throws NamingException
     {
         mContext.destroySubcontext(distinguishedName);
     }
 
     /**
      * Prepares facade for another search.  This should be called in
      * between searches to reset all state information.
      */
     public void resetSearch()
     {
         mControls = new SearchControls();
         mSearchBase = "";
         mResults = null;
         mCurrentResultElement = null;
         mCurrentResultAttributes = null;
     }
 
     /**
      * Search one level of the directory.
      *
      * @param base Base DN to search from
      * @param filter The filter to search for
      * @throws NamingException If an error occured
      */
     public void searchOneLevel(String base, String filter)
         throws NamingException
     {
         mSearchBase = base;
         mControls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
         mResults = mContext.search(mSearchBase, filter, mControls);
     }
 
     /**
      * Recursively search a subtree of the directory.
      *
      * @param base Base DN to search from
      * @param filter The filter to search for
      * @throws NamingException If an error occured
      */
     public void searchSubtree(String base, String filter)
         throws NamingException
     {
         mSearchBase = base;
         mControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
         mResults = mContext.search(mSearchBase, filter, mControls);
     }
 
     /**
      * Advances to the next result of the search, if there is one.
      *
      * @return <code>true</code> if there are more elements, or
      * <code>false</code> if there are no more.
      * @throws NamingException If an error occured
      */
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
 
     /**
      * Gets the value of a single-valued attribute for the current
      * result element.
      *
      * @param name Attribute name
      * @return The value of this attribute
      * @throws NamingException If an error occured
      */
     public String getResultAttribute(String name)
         throws NamingException
     {
         return attributeToString(mCurrentResultAttributes.get(name));
     }
 
     /**
      * Gets the attributes for the current result element.
      *
      * @return All attributes for the current result element.
      * @throws NamingException If an error occured
      */
     public NamingEnumeration getAllResultAttributes()
         throws NamingException
     {
         return mCurrentResultAttributes.getAll();
     }
 
     /**
      * Gets the name (DN) of the current result attribute.
      *
      * @return DN of the current result attribute.
      * @throws NamingException If an error occured
      */
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
      * Gets all values of a multi-valued attribute for the current
      * result element.  The returned set is actually a
      * <code>CaseInsensitiveStringSet</code>.  Normally this will not
      * matter, but it will become apparent if comparing the whole set
      * to, say, a <code>HashSet</code> of normal <code>String</code>
      * objects.
      *
      * @param name Attribute name
      * @return The values of this attribute
      * @throws NamingException If an error occured
      * @see CaseInsensitiveStringSet
      */
     public Set getAllResultAttributeValues(String name)
         throws NamingException
     {
         Set values;
         NamingEnumeration valueEnumeration = null;
 
         values = new CaseInsensitiveStringSet();
         try
         {
             Attribute attribute = mCurrentResultAttributes.get(name);
             if (attribute != null)
             {
                 valueEnumeration = attribute.getAll();
                 while (valueEnumeration.hasMore())
                 {
                     String value;
 
                     value = (String) valueEnumeration.next();
                     values.add(value);
                 }
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
 
     /**
      * Releases any resources used by this facade.  This should be
      * called on every instance to avoid resource leakage in a finally
      * block.
      */
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
 
     /** Host name to connect to. */
     private String mHost;
     /** Port number to connect to. */
     private int mPort;
 
     /** The environment to use for this context. */
     private Hashtable mEnvironment;
     /** The context (connection) for this facade. */
     private DirContext mContext;
     /** The attributes of the bound element. */
     private Attributes mAttributes;
 
     /** Controls to use for searches. */
     private SearchControls mControls;
     /** The results of the last search. */
     private NamingEnumeration mResults;
     /** The element of the current search result. */
     private SearchResult mCurrentResultElement;
     /** The attributes of the current search result. */
     private Attributes mCurrentResultAttributes;
     /** The base of the previous search. */
     private String mSearchBase;
 }
