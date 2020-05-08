 /*
  * Copyright (c) 2010-2012 Matthias Klass, Johannes Leimer,
  *               Rico Lieback, Sebastian Gabriel, Lothar Gesslein,
  *               Alexander Rampp, Kai Weidner
  *
  * This file is part of the Physalix Enrollment System
  *
  * Foobar is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Foobar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package hsa.awp.common.naming;
 
 import hsa.awp.common.exception.ConfigurationException;
 import hsa.awp.common.exception.ProgrammingErrorException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.naming.Context;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.ServiceUnavailableException;
 import javax.naming.directory.*;
 import java.util.*;
 
 /**
  * This adapter implements lookup to an ldap directory. This implementation normally generates its dirContext automatically but you
  * can also use the setter to set a custom dirContext.
  *
  * @author alex
  */
 public class LdapDirectoryAdapter implements IDirectoryAdapter {
 
   private static final Logger logger = LoggerFactory.getLogger(LdapDirectoryAdapter.class);
 
   /**
    * LDAP directory context.
    */
   private DirContext dirContext;
 
   private String[] userDnPatterns;
 
   /**
    * Default attribute ids we are interested in.
    */
   private String[] defaultAttrIds;
 
   /**
    * URL of the ldap server.
    */
   private String providerURL;
 
   /**
    * Login for the ldap server.
    */
   private String securityPrincipal;
 
   /**
    * Password for the ldap server.
    */
   private String securityCredentials;
 
   /**
    * Protocol (e. g. ssl) for the ldap server.
    */
   private String securityProtocol;
 
   /**
    * Auth option for the LDAP server (e. g. 'simple')
    */
   private String securityAuthentication;
 
   /**
    * If configuration changed, the dirty flag will be set. Then you must call initContext() to set the new configuration.
    */
   private boolean dirty;
 
   /**
    * If a dir context is set via the setter, this flag turns of the dircontext configuration. This is usefull for testing with
    * mock objects.
    */
   private boolean customDirContext = false;
 
   @Override
   public void finalize() {
 
     try {
       dirContext.close();
     } catch (NamingException e) {
       e.printStackTrace();
       throw new RuntimeException(e);
     }
   }
 
   @Override
   public Attributes getAttributes(String name) throws NamingException {
 
     return getAttributes(name, defaultAttrIds);
   }
 
   @Override
   public Attributes getAttributes(long uuid) throws NamingException {
 
     cleanContext();
 
     Attributes matchingAttributes = new BasicAttributes();
 
     // search for the attribute name for the uuid
     String uuidFieldName = null;
     for (String ldapField : defaultAttrIds) {
       String field = Directory.getInstance().getAbstractFieldName(ldapField);
       if (field.equals(Directory.UUID)) {
         uuidFieldName = ldapField;
         break;
       }
     }
     if (uuidFieldName == null) {
       throw new ProgrammingErrorException("uuidFieldName cannot be resolved!");
     }
 
     Attribute uidNumber = new BasicAttribute(uuidFieldName, uuid);
     matchingAttributes.put(uidNumber);
 
     for (String pattern : userDnPatterns) {
       int start = pattern.indexOf("{0},") + 4;
       String searchPattern = pattern.substring(start);
 
       NamingEnumeration<SearchResult> searchResult = dirContext.search(searchPattern, matchingAttributes,
           defaultAttrIds);
 
       if (searchResult.hasMore()) {
         return searchResult.next().getAttributes();
       }
     }
     throw new NamingException("No user with uidNumber" + uuid + " found.");
   }
 
   private void cleanContext() {
     if (dirty) {
       initContext();
     }
   }
 
   @Override
   public Attributes getAttributes(String name, String[] attrIds) throws NamingException {
 
     cleanContext();
 
     NamingException exception = null;
     for (String pattern : userDnPatterns) {
       try {
         String searchPattern = pattern.replaceFirst("\\{0\\}", name);
         return dirContext.getAttributes(searchPattern, attrIds);
       } catch (ServiceUnavailableException e) {
         logger.error("retry because of unavailable service", e);
         dirty = true;
         return getAttributes(name, attrIds);
       } catch (NamingException e) {
         exception = e;
       }
     }
 
     throw exception;
   }
 
   @Override
   public void setDirContext(DirContext context) {
 
     dirContext = context;
     customDirContext = true;
   }
 
   /**
    * Initializes the dircontext.
    */
   private void initContext() {
 
     if (!customDirContext) {
       try {
         Properties env = new Properties();
         env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
         env.put(Context.PROVIDER_URL, providerURL);
         env.put(Context.SECURITY_PRINCIPAL, securityPrincipal);
         env.put(Context.SECURITY_CREDENTIALS, securityCredentials);
         env.put(Context.SECURITY_PROTOCOL, securityProtocol);
         env.put(Context.SECURITY_AUTHENTICATION, securityAuthentication);
         dirContext = new InitialDirContext(env);
         dirty = false;
       } catch (NamingException e) {
         throw new ConfigurationException(e);
       }
     }
   }
 
   /**
    * A comma separated list of ldap attribute ids to request.
    *
    * @param ids The attribute ids to set as comma separated list.
    */
   public void setDefaultAttrIds(String ids) {
 
     dirty = true;
     if (ids != null) {
       defaultAttrIds = ids.split(",");
     } else {
       defaultAttrIds = null;
     }
   }
 
   /**
    * @param providerURL the providerURL to set
    */
   public void setProviderURL(String providerURL) {
 
     dirty = true;
     this.providerURL = providerURL;
   }
 
   /**
    * @param securityAuthentication the securityAuthentication to set
    */
   public void setSecurityAuthentication(String securityAuthentication) {
 
     dirty = true;
     this.securityAuthentication = securityAuthentication;
   }
 
   /**
    * @param securityCredentials the securityCredentials to set
    */
   public void setSecurityCredentials(String securityCredentials) {
 
     dirty = true;
     this.securityCredentials = securityCredentials;
   }
 
   /**
    * @param securityPrincipal the securityPrincipal to set
    */
   public void setSecurityPrincipal(String securityPrincipal) {
 
     dirty = true;
     this.securityPrincipal = securityPrincipal;
   }
 
   /**
    * @param securityProtocol the securityProtocol to set
    */
   public void setSecurityProtocol(String securityProtocol) {
 
     dirty = true;
     this.securityProtocol = securityProtocol;
   }
 
   public void setUserDnPatterns(String[] userDnPatterns) {
 
     this.userDnPatterns = userDnPatterns;
   }
 
   @Override
   public List<Attributes> searchDirectory(String searchString) {
 
     String searchPattern = "(|(uid=#)(sn=#))";
     searchPattern = searchPattern.replaceAll("#", searchString);
     List<NamingEnumeration<SearchResult>> searches = queryDirectories(searchPattern);
 
     return convertToListOfAttributes(searches);
   }
 
   private List<Attributes> convertToListOfAttributes(List<NamingEnumeration<SearchResult>> searches) {
     List<Attributes> list = new ArrayList<Attributes>();
 
     for (NamingEnumeration<SearchResult> result : searches) {
       list.addAll(convertToListOfAttributes(result));
     }
 
     return list;
   }
 
 
   private List<Attributes> convertToListOfAttributes(NamingEnumeration<SearchResult> search) {
 
     List<Attributes> attributes = new ArrayList<Attributes>();
 
     if (search == null) {
       return attributes;
     }
 
     try {
       while (search.hasMore()) {
         attributes.add(search.next().getAttributes());
       }
     } catch (NamingException e) {
       throw new RuntimeException(e);
     }
 
     return attributes;
   }
 
 
   @Override
   public Set<String> getAllStudyCourses() {
     Set<String> studyCourses = new HashSet<String>();
     for (String userDnPattern : userDnPatterns) {
       Set<String> courses = getAllStudyCourses(userDnPattern);
       studyCourses.addAll(studyCourses);
     }
 
     return studyCourses;
   }
 
   private Set<String> getAllStudyCourses(String searchDn) {
     String concreteStudyCourseFieldName = Directory.getInstance().getLowLevelFieldName(IAbstractDirectory.STUDYCOURSE);
    NamingEnumeration<SearchResult> results = queryDirectory("(" + concreteStudyCourseFieldName + "=*)", searchDn);
     Set<String> studyCourses = new HashSet<String>();
     try {
       while (results.hasMore()) {
 
         SearchResult result = results.next();
         String studyCourse = new String((String) result.getAttributes().get(concreteStudyCourseFieldName).get());
         studyCourses.add(studyCourse);
       }
     } catch (NamingException e) {
       throw new RuntimeException(e);
     }
     return studyCourses;
   }
 
   private List<NamingEnumeration<SearchResult>> queryDirectories(String searchPattern) {
     List<NamingEnumeration<SearchResult>> results = new ArrayList<NamingEnumeration<SearchResult>>();
 
     for (String pattern : userDnPatterns) {
       pattern = pattern.substring(pattern.indexOf(",") + 1);
       results.add(queryDirectory(searchPattern, pattern));
     }
 
     return results;
   }
 
   private NamingEnumeration<SearchResult> queryDirectory(String searchPattern, String directory) {
     cleanContext();
     NamingEnumeration<SearchResult> search = null;
     try {
       search = dirContext.search(directory, searchPattern, defaultAttrIds, null);
     } catch (NamingException e) {
       throw new RuntimeException(e);
     }
     return search;
   }
 
 }
