 /**
  * This file is part of the LDAP Persistence API (LPA).
  *
  * Copyright Trenton D. Adams <lpa at trentonadams daught ca>
  *
  * LPA is free software: you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  *
  * LPA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
  * License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public 
  * License along with LPA.  If not, see <http://www.gnu.org/licenses/>.
  *
  * See the COPYING file for more information.
  */
 package ca.tnt.ldaputils;
 
 import ca.tnt.ldaputils.annotations.LdapEntity;
 import ca.tnt.ldaputils.annotations.processing.AnnotationProcessor;
 import ca.tnt.ldaputils.annotations.processing.LdapEntityBinder;
 import ca.tnt.ldaputils.annotations.processing.LdapEntityLoader;
 import ca.tnt.ldaputils.exception.LdapNamingException;
 import ca.tnt.ldaputils.impl.LdapEntry;
 import org.apache.log4j.Logger;
 
 import javax.naming.Context;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.directory.Attributes;
 import javax.naming.directory.DirContext;
 import javax.naming.directory.SearchControls;
 import javax.naming.directory.SearchResult;
 import javax.naming.ldap.InitialLdapContext;
 import javax.naming.ldap.LdapContext;
 import javax.naming.ldap.LdapName;
 import java.util.*;
 
 /**
  * Encapsulates LDAP access in to an easy to use factory object.  The purpose is
  * to provide a method of access LDAP that is similar to JPA, but specifically
  * customized for LDAP purposes.  JPA's annotations did not seem to fit the bill
  * enough to warrant creating a JPA provider for LDAP.
  * <p/>
  * The main reason for starting this project is that I have found Sun JNDI
  * interfaces too cumbersome to use for my simple purposes.
  * <p/>
  * Created :  Feb 28, 2006 7:43:12 PM MST
  *
  * @author Trenton D. Adams <trent.nospam@telus.net>
  */
 @SuppressWarnings({"PublicMethodNotExposedInInterface", "ReturnOfNull"})
 public class LdapManager
 {
     // logging
     private static Logger logger = Logger.getLogger(LdapManager.class);
     private static final int TIMEOUT = 300;
 
     // internal configuration
     private final String sLDAPURL;
     private String sLDAPuidAttribute;
     private final String sLDAPManagerDN;
     private final String sLDAPManagerPW;
 
     /**
      * Return search results in no particular order.  i.e. the are stored in a
      * non sorted Map.  Probably a Hashtable.
      */
     public static final int NO_ORDER = 0;
 
     /**
      * Return search results in the order they were found.
      */
     public static final int SEARCH_ORDER = 1;
 
     /**
      * Return search result in alphanumeric order, sorting by the keyAttribute.
      */
     public static final int SORTED_ORDER = 2;
 
 
     /**
      * Initializes internal data store parameters.  Namely loads the
      * ldap.properties file from the classpath.
      */
     @SuppressWarnings({"CatchGenericClass"})
     public LdapManager()
     {
         try
         {
             /*
              * Get all the properties we need from the CAS properties file in
              * /etc
              */
             final Properties properties;
             properties = Property.loadProperties("/ldap.properties");
 
             if (properties == null)
             {
                 throw new NullPointerException("unable to load " +
                     "ldap.properties.  Please ensure that it is " +
                     "located in the CLASSPATH.");
             }
 
             // use defaults for most properties, just in case
             final String sLDAPHost = properties.getProperty("LDAP.host");
             final String sLDAPPort = properties.getProperty("LDAP.port");
             sLDAPURL = "ldap://" + sLDAPHost + ':' + sLDAPPort;
             final String sLDAPBaseDN = properties.getProperty("LDAP.baseDN");
 
             // don't use defaults in code because it could be a security
             // vulnerability
             sLDAPManagerDN = properties.getProperty("LDAP.managerdn");
             sLDAPManagerPW = properties.getProperty("LDAP.managerpw");
 
             if (sLDAPHost == null ||
                 sLDAPPort == null ||
                 sLDAPBaseDN == null ||
                 sLDAPManagerDN == null || sLDAPManagerPW == null)
             {
                 throw new NullPointerException(
                     "please ensure all properties are set in ldap.properties");
             }
 
             logger.info("loaded new " + LdapManager.class);
         }
         catch (Throwable exception)
         {
             throw new LdapNamingException("error loading ldap settings",
                 exception);
         }
     }
 
     /**
      * Initialize LdapManager instance with the host, port, auth dn, and auth
      * password set.
      *
      * @param sLDAPHost      the ldap host
      * @param sLDAPPort      the ldap port
      * @param sLDAPManagerDN the fully qualified DN of the ldap manager account,
      *                       or one of sufficient privileges to carry out the
      *                       required operations
      * @param sLDAPManagerPW the password of the sLDAPManagerDN account.
      */
     public LdapManager(final String sLDAPHost, final String sLDAPPort,
         final String sLDAPManagerDN, final String sLDAPManagerPW)
     {
         this.sLDAPManagerDN = sLDAPManagerDN;
         this.sLDAPManagerPW = sLDAPManagerPW;
         sLDAPURL = "ldap://" + sLDAPHost + ':' + sLDAPPort;
     }
 
     /**
      * Do a search and return a Map of the entries.  They key is the value of
      * the keyAttribute that you passed in.  So, if you wanted "cn" to be the
      * key, you would pass "cn" in for the keyAttribute.  Then the value of
      * "cn", for a particular entry, would be the key you use to find that
      * entry.  It is assumed that the key can be typecast to a
      * java.lang.String.
      * <p/>
      * It is your responsibility to make sure that the keyAttribute exists in
      * the attributes parameter, and that any results returned will also have
      * that attribute set.  So do not set keyAttribute to an attribute that is
      * not required by LDAP, unless you know that it will always have values for
      * it due to your business practices.  For instance, the mail attribute is
      * not required on every person entry, but the uid attribute is.  So uid
      * would be a good keyAttribute, as we know that it MUST exist in LDAP.
      * <p/>
      * In addition, the caller is required to be very careful that they do not
      * search subtrees that could potentially return the same "keyAttribute"
      * value. If this happened, then only the last entry found would be in the
      * hashtable. for instance, you may have a uid=trenta,ou=People,dc=domain,dc=ca
      * entry as well as a uid=trenta,ou=Staff,ou=People,dc=domain,dc=ca entry.
      * If your keyAttribute is uid, then only the last item found will be in the
      * returned Map.  If you do not want to search subtree, just specify {@link
      * SearchControls#ONELEVEL_SCOPE} as the "searchScope" parameter.
      *
      * @param baseDN         the base DN to search on
      * @param searchFilter   the ldap search filter to use for ldap entry
      *                       retreival.
      * @param keyAttribute   the attribute that will be the key for the
      *                       getSortedAttributes() method that returns a
      *                       SortedMap
      * @param attributes     the array of attribute names to retrieve.  If you
      *                       want ALL attributes to be loaded for a particular
      *                       LDAPObject, then pass in a null value for this
      *                       parameter
      * @param ldapEntryClass the clas of the object that is {@link LdapEntity
      *                       annotated}
      * @param sorted         One of NO_ORDER, SEARCH_ORDER, SORTED_ORDER
      * @param searchScope    One of the scope values in {@link SearchControls}
      *
      * @return a map of LDAPObjects with the keys being the keyAttribute value
      *
      * @throws LdapNamingException if any naming problems occur
      * @see LdapManager#NO_ORDER
      * @see LdapManager#SEARCH_ORDER
      * @see LdapManager#SORTED_ORDER
      */
     @SuppressWarnings(
         {"unchecked", "ObjectAllocationInLoop", "ChainedMethodCall"})
     public Map find(final LdapName baseDN, final String searchFilter,
         final String keyAttribute, final String[] attributes,
         final Class ldapEntryClass, final int sorted, final int searchScope)
     {
         DirContext ldapContext = null;
         final SearchControls searchControls;
         NamingEnumeration<SearchResult> results = null;
         Attributes entryAttributes;
         final int scope =
             searchScope != -1 ? searchScope : SearchControls.SUBTREE_SCOPE;
 
 
         searchControls = new SearchControls();
         searchControls.setReturningAttributes(attributes);
         searchControls.setSearchScope(scope);
 
         final Map sortedLDAPObjects;
         if (sorted == SORTED_ORDER)
             sortedLDAPObjects = new TreeMap();
         else if (sorted == NO_ORDER)
             sortedLDAPObjects = new HashMap();
         else    // assume SEARCH_ORDER
             sortedLDAPObjects = new LinkedHashMap();
 
         try
         {
             ldapContext = getConnection();
 
             // perform a search to find the entries
             results = ldapContext.search(baseDN, searchFilter, searchControls);
 
             while (results.hasMore())
             {
                 final SearchResult entry;
 
                 entry = results.next();
                 entryAttributes = entry.getAttributes();
 
                 logger.debug("keyAttribute: " + keyAttribute);
                 logger.debug("keyAttribute - " +
                     entryAttributes.get(keyAttribute).get());
                 final String keyValue;
                 keyValue = (String) entryAttributes.get(keyAttribute).get();
 
                 sortedLDAPObjects.put(keyValue, find(ldapEntryClass,
                     new LdapName(entry.getNameInNamespace()), entryAttributes));
             }
         }
         catch (NamingException namingException)
         {
             throw new LdapNamingException("an error occurred doing an ldap " +
                 "search", namingException);
         }
         catch (Exception exception)
         {
             throw new LdapNamingException("an error occurred doing an ldap " +
                 "search", exception);
         }
 
         finally
         {
             if (results != null)
             {
                 try
                 {
                     results.close();
                 }
                 catch (NamingException e)
                 {
                     logger.error("error closing results: " +
                         getNamingExceptionMessage(e));
                 }
             }
 
             releaseConnection(ldapContext);
         }
         return sortedLDAPObjects;
     }
 
     /**
      * Retrieves the exact LdapEntity POJO you specify, using the dn passed in.
      * You must pass in a fully qualified DN.
      *
      * @param annotatedClass the class that has been annotated with ldaputil
      *                       annotations
      * @param dn             the LDAP Distinquished Name.
      *
      * @return the {@link LdapEntity} annotated POJO that you specified in
      *         annotatedClass, or null if it was not found
      *
      * @throws LdapNamingException      if any ldap naming errors occur.
      * @throws IllegalArgumentException if the annotatedClass is not correctly
      *                                  annotated in some way
      */
     public Object find(final Class annotatedClass, final LdapName dn)
     {
         return find(annotatedClass, dn, getAttributes(dn, null));
     }
 
     /**
      * Creates an LdapEntity with the LdapEntity POJO and attributes you
      * specify.
      *
      * @param annotatedClass the class that has been annotated with ldaputil
      *                       annotations
      * @param dn             the LDAP Distinquished Name.
      * @param attributes     the ldap attributes to populate the object with
      *
      * @return the {@link LdapEntity} annotated POJO that you specified in
      *         annotatedClass, or null if it was not found
      *
      * @throws LdapNamingException      if any ldap naming errors occur.
      * @throws IllegalArgumentException if the annotatedClass is not correctly
      *                                  annotated in some way
      */
     private Object find(final Class annotatedClass, final LdapName dn,
         final Attributes attributes)
     {
         Object newObject = null;
         try
         {
             newObject = annotatedClass.newInstance();
             final AnnotationProcessor annotationProcessor =
                 new AnnotationProcessor();
             final LdapEntityLoader entityLoader = new LdapEntityLoader(
                 newObject, attributes, dn);
             entityLoader.setManager(this);
             annotationProcessor.addHandler(entityLoader);
             if (!annotationProcessor.processAnnotations())
             {
                 return null;
             }
         }
         catch (InstantiationException e)
         {   // instantiation problems will mean it's not a valid object
             throw new IllegalArgumentException(e);
         }
         catch (IllegalAccessException e)
         {
             throw new IllegalArgumentException("If this is happening, there " +
                 "is something wrong with your policy, or it is a " +
                 "programming error", e);
         }
 
         return newObject;
     }
 
 
     /**
      * Generic method for retrieving entry attributes from the LDAP store. There
      * is no need to log any NamingExceptions that have been thrown from this
      * method. They will have already been logged.  This method returns an array
      * of non-null "Attributes" objects.  Each array index corresponds to a 'DN'
      * entry found in LDAP by the <strong>searchFilter</strong>.
      *
      * @param baseDN       the DN given.
      * @param searchFilter the ldap search filter to use for ldap entry
      *                     retrieval.
      * @param attributes   array with the attribute names to grab
      *
      * @return array of "Attributes"
      *
      * @throws LdapNamingException if an ldap error occurs
      */
     public Object[] getAttributes(final String baseDN,
         final String searchFilter, final String[] attributes)
     { // BEGIN getAttributes ()
         DirContext ldapContext = null;
         final SearchControls searchControls;
         final NamingEnumeration<SearchResult> results;
         Attributes ldapAttributes;
         final List<Attributes> returnedEntries;
 
         returnedEntries = new ArrayList<Attributes>();
         // setup the search
         searchControls = new SearchControls();
         searchControls.setReturningAttributes(attributes);
         searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
 
         try
         { // BEGIN LDAP try block
             ldapContext = getConnection();
 
             // perform a search to find the entries
             logger.debug("baseDN: " + baseDN);
             results = ldapContext.search(baseDN, searchFilter, searchControls);
 //            ldapContext.getAttributes(baseDN, attributes);
 
             if (results != null)
             {
                 while (results.hasMore())
                 {
                     final SearchResult entry;
 
                     entry = results.next();
                     logger.debug("dn: " + entry.getName());
                     ldapAttributes = entry.getAttributes();
                     returnedEntries.add(ldapAttributes);
                 }
             }
             else
             {
                 // nothing returned, returnedEntries.toArray() should return
                 // a zero length array
             }
         } // END LDAP try block
         catch (NamingException exception)
         {
             if (ldapContext == null)
             {
                 logger.error("LDAP getConnection FAILURE");
             }
 
             throw new LdapNamingException(exception);  // propogate
         }
         catch (Exception exception)
         {
             logger.error("getAttributes - ", exception);
             throw new LdapNamingException(exception);
         }
         finally
         {
             releaseConnection(ldapContext);
         }
 
         return returnedEntries.toArray();
     } // BEGIN getAttributes ()
 
     public Attributes getAttributes(final LdapName dn,
         final String[] attributes)
     {   // BEGIN getAttributes(dn)
         Attributes returnedAttributes = null;
         DirContext ldapContext = null;
 
         try
         { // BEGIN LDAP try block
             ldapContext = getConnection();
             returnedAttributes = ldapContext.getAttributes(dn, attributes);
         } // END LDAP try block
         catch (NamingException exception)
         {
             throw new LdapNamingException(exception);  // propogate
         }
         catch (Exception exception)
         {
             throw new LdapNamingException(
                 "unknown exception while retrieving attributes", exception);
         }
         finally
         {
             releaseConnection(ldapContext);
         }
 
         return returnedAttributes;
     }   // END getAttributes(dn)
 
     /**
      * Gets an LDAP directory context.
      * <p/>
      * CRITICAL implement something that let's developers choose how to
      * authenticate, or even allow passing of DirContext???  Perhaps we make
      * LdapManager an instance based object, where an instance is created for
      * every user?  That way authentication could be done through
      * ldap.properties, or by passing principal/credentials.
      *
      * @param isPooled       turn pooling on?
      * @param timeout        the connection timeout value
      * @param sLDAPURL       the ldap url
      * @param sLDAPManagerDN the manager dn
      * @param sLDAPManagerPW @return an LDAP directory context object
      *
      * @return the new DirContext
      *
      * @throws NamingException if a JNDI error occurs.
      */
     @SuppressWarnings(
         {"UseOfObsoleteCollectionType", "MagicNumber"})
     public static DirContext getConnection(final boolean isPooled,
         final int timeout, final String sLDAPURL, final String sLDAPManagerDN,
         final String sLDAPManagerPW) throws NamingException
     { // BEGIN getConnection ()
 
         final DirContext ldapContext;
         final Hashtable<String, String> env = new Hashtable<String, String>(5,
             0.75f);
         env.put(Context.INITIAL_CONTEXT_FACTORY,
             "com.sun.jndi.ldap.LdapCtxFactory");
         if (isPooled)
         {
             env.put("com.sun.jndi.ldap.connect.pool", "true");
         }
 
         if (timeout != 0)
         {
             env.put("com.sun.jndi.ldap.connect.timeout", "" + timeout);
         }
         env.put(Context.PROVIDER_URL, sLDAPURL);
         env.put(Context.SECURITY_AUTHENTICATION, "simple");
         env.put(Context.SECURITY_PRINCIPAL, sLDAPManagerDN);
         env.put(Context.SECURITY_CREDENTIALS, sLDAPManagerPW);
 
         ldapContext = new InitialLdapContext(env, null);
 
         return ldapContext;
         //      conn = new InitialLdapContext(env, new Control [0]);
 
         //      conn.setRequestControls(new Control [0]);
 
         //      context.log ("controls : " + conn.getAttributes(sLDAPURL, new
         // String[]{"supportedcontrol"}));
 
     } // END getConnection ()
 
     public DirContext getConnection() throws NamingException
     {
         return getConnection(true, TIMEOUT, sLDAPURL, sLDAPManagerDN,
             sLDAPManagerPW);
     }
 
     /**
      * Releases an LDAP directory context.
      *
      * @param conn LDAP directory context object
      */
     public static void releaseConnection(final DirContext conn)
     {
         if (conn == null)
             return;
         try
         {
             conn.close();
         }
         catch (Exception e)
         {
             logger.error("this error should not occur - " +
                 e.getMessage());
         }
     }
 
     public static void logNamingException(final NamingException namingException)
     {
         final String explanation;
         explanation = namingException.getExplanation();
         if (explanation != null &&
             (explanation.indexOf("user entry not found") != -1 ||
                 explanation.indexOf("Invalid Credentials") != -1))
         {
             /**
              * no big deal, we don't care about this much, as it's probably
              * nothing major, we'll print
              * a simple log though, and a debug log.
              */
             logger.warn("explanation : " + explanation);
             logger.debug("naming exception", namingException);
         }
         else
         {
             logger.warn("explanation : " + explanation);
             logger.error("naming exception", namingException);
         }
     }
 
     /**
      * Returns an appropriate message for the exception.
      *
      * @param namingException the exception
      *
      * @return the message to log
      */
     public static String getNamingExceptionMessage(
         final NamingException namingException)
     {
         final String explanation;
         explanation = namingException.getExplanation();
         return explanation;
     }
 
     /**
      * Hopefully provides an example of how to use all the basic features of the
      * LDAP objects framework.
      *
      * @param args the arguments to pass in.
      */
     public static void main(final String[] args)
     {   // BEGIN main()
         if (args.length < 5)
         {
             System.out.println(args.length + " arguments are not enough " +
                 "arguments");
             System.exit(1);
         }
 
         final LdapManager manager = new LdapManager();
 
         final Map sortedEntries;
         try
         {
             final String[] attributes;
             final String operation;
             final LdapName baseDN;
             final String filter;
             final String keyAttribute;
             ILdapEntry ldapEntry;
 
             operation = args[0];
 
             if ("search".equals(operation))
             {
                 baseDN = new LdapName(args[1]);
                 filter = args[2];
                 keyAttribute = args[3];
                 if ("all".equals(args[4]))
                 {
                     attributes = null;
                 }
                 else
                 {
                     attributes = args[4].split(",");
                 }
 
                 sortedEntries =
                     manager.search(baseDN, filter, keyAttribute, attributes);
                 for (final Object o : sortedEntries.values())
                 {
                     ldapEntry = (ILdapEntry) o;
                     System.out.print(ldapEntry);
                 }
             }
             else if ("modify".equals(operation))
             {
                 if (args.length < 6 || args.length > 7)
                 {
                     System.err.println(
                         "invalid number of arguments : " + args.length);
                     System.exit(1);
                 }
                 final String modAttr;
                 final String[] values;
                 final String op;
                 final int iop;
                 final LdapName dn;
 
                 dn = new LdapName(args[1]);
                 attributes = args[2].split(",");
                 op = args[3];
                 modAttr = args[4];
                 if (args.length > 5)
                 {
                     if ("all".equals(args[5]))
                     {
                         values = new String[0];
                     }
                     else
                     {
                         values = args[5].split(",");
                     }
                 }
                 else
                 {
                     values = new String[0];
                 }
                 System.out.println("modAttr : " + modAttr);
                 System.out.println("values.length : " + values.length);
 
                 if ("ADD_ATTRIBUTE".equals(op))
                 {
                     iop = ILdapEntry.ADD_ATTRIBUTE;
                 }
                 else if ("REPLACE_ATTRIBUTE".equals(op))
                 {
                     iop = ILdapEntry.REPLACE_ATTRIBUTE;
                 }
                 else if ("REMOVE_ATTRIBUTE".equals(op))
                 {
                     iop = ILdapEntry.REMOVE_ATTRIBUTE;
                 }
                 else
                 {
                     iop = 0;
                     System.err.println("invalid operation : " + op);
                     System.exit(2);
                 }
 
                 System.out.println("dn: " + dn);
                 System.out
                     .println("attributes: " + Arrays.toString(attributes));
 
                 ldapEntry =
                     (ILdapEntry) manager.find(LdapEntry.class, dn);
                 System.out.println("ldapObject : " + ldapEntry);
 
                 for (final String value : values)
                 {
                     ldapEntry.modifyBatchAttribute(iop, modAttr, value
                     );
                 }
 
                 if (values.length == 0)
                 {
                     ldapEntry.modifyBatchAttribute(iop, modAttr, null);
                 }
 
                 ldapEntry.modifyBatchAttributes();
 
                 final List attributeValues;
                 attributeValues =
                     ldapEntry.getAttributeValues(modAttr);
                 for (final Object attributeValue : attributeValues)
                 {   // print out all the new values
                     System.out.println(modAttr + " : " +
                         attributeValue);
                 }
             }
         }
         catch (NamingException namingException)
         {
             logNamingException(namingException);
         }
     }   // END main()
 
     /**
      * Searches using LDAP_OBJECT as the default object type, and SEARCH_ORDER
      * for the entry order.
      *
      * @param baseDN       the base DN to search on
      * @param searchFilter the ldap search filter to use for ldap entry
      *                     retreival.
      * @param keyAttribute the attribute that will be the key for the
      *                     getSortedAttributes() method that returns a
      *                     SortedMap
      * @param attributes   the array of attribute names to retrieve.  If you
      *                     want ALL attributes to be loaded for a particular
      *                     LDAPObject, then pass in a null value for this
      *                     parameter
      *
      * @return a map of LDAPObjects with the keys being the keyAttribute value
      *
      * @throws NamingException if any naming problems occur
      */
     private Map search(final LdapName baseDN, final String searchFilter,
         final String keyAttribute, final String[] attributes)
         throws NamingException
     {
         final Map sortedEntries;
         sortedEntries = find(baseDN, searchFilter, keyAttribute,
             attributes, LdapEntry.class, SEARCH_ORDER,
             SearchControls.SUBTREE_SCOPE);
 
         return sortedEntries;
     }
 
 
     /**
      * Binds the {@link LdapEntity} annotated object to ldap, with all of it's
      * attributes.
      * <p/>
     * CRITICAL binding annotation processor https://github.com/TrentonAdams/lpa/issues/4
     * <p/>
      * CRITICAL updating annotation processor https://github.com/TrentonAdams/lpa/issues/5
      *
      * @param ldapEntry {@link LdapEntity} annotated object
      *
      * @throws UnsupportedOperationException if there is some error in the code
      *                                       that uses the recursive binding
      *                                       functionality.  This shouldn't
      *                                       happen, if it does, it's a bug, and
      *                                       needs to be reported.
      */
     public void bind(final Object ldapEntry)
     {
         LdapContext ldapContext = null;
         try
         {
             // accessing dn method should be fine, but must be done through reflect
             final AnnotationProcessor annotationProcessor =
                 new AnnotationProcessor();
             final LdapEntityBinder entityBinder = new LdapEntityBinder(
                 ldapEntry);
             entityBinder.setManager(this);
             annotationProcessor.addHandler(entityBinder);
             if (!annotationProcessor.processAnnotations())
             {
                 throw new LdapNamingException("annotation processing failed");
             }
             ldapContext = (LdapContext) getConnection();
             final List<Attributes> attributesList =
                 entityBinder.getAttributesList();
             final List<LdapName> dnList = entityBinder.getDnList();
             if (attributesList.size() > 1)
             {
                 throw new UnsupportedOperationException(
                     "we do not yet support recursive binding");
             }
             for (int index = 0; index < attributesList.size(); index++)
             {
                 final LdapName dn = dnList.get(index);
                 final Attributes attributes = attributesList.get(index);
                 ldapContext.bind(dn, null, attributes);
             }
         }
         catch (NamingException e)
         {
             logger.error(e);
         }
         finally
         {
             releaseConnection(ldapContext);
         }
 
     }
 
     public void unbind(final LdapName dn)
     {
         LdapContext ldapContext = null;
         try
         {
             ldapContext = (LdapContext) getConnection();
             ldapContext.unbind(dn);
         }
         catch (NamingException e)
         {
             logger.error(e);
         }
         finally
         {
             releaseConnection(ldapContext);
         }
 
     }
 
     public void unbind(final ILdapEntry ldapEntry)
     {
         unbind(ldapEntry.getDn());
     }
 
 }
