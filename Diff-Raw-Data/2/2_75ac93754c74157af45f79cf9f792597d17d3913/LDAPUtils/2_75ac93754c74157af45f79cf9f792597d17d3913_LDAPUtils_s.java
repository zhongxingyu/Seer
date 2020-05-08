 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package se.umu.cs.umume.util;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Hashtable;
 import java.util.List;
 
 import javax.naming.Context;
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.directory.SearchResult;
 import javax.naming.ldap.InitialLdapContext;
 import javax.naming.ldap.LdapContext;
 import javax.naming.directory.Attributes;
 import javax.naming.directory.BasicAttributes;
 import javax.naming.directory.Attribute;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import se.umu.cs.umume.PersonBean;
 
 import java.util.ArrayList;
 
 /**
  *
  * @author anton
  */
 public class LDAPUtils {
     private static final String URL = "ldap://ldap.umu.se";
     private static final Logger logger = LoggerFactory.getLogger(LDAPUtils.class);
 
     private static LdapContext createLdapContext() throws NamingException {
         // Setup connection
         Hashtable<String, String> env = new Hashtable<String, String>();
         env.put(Context.INITIAL_CONTEXT_FACTORY,
         "com.sun.jndi.ldap.LdapCtxFactory");
         env.put(Context.PROVIDER_URL, URL);
 
         return new InitialLdapContext(env, null);
     }
 
     private static NamingEnumeration<SearchResult> doLDAPSearch(String searchBase, Attributes searchAttrs,
             String[] matchingAttributes) throws NamingException {
         LdapContext context = createLdapContext();
         // Perform search
         return  context.search(searchBase, searchAttrs, matchingAttributes);
     }
 
     public static NamingEnumeration<SearchResult> searchForUid(final String uid) throws NamingException {
         String searchBase = "cn=person,dc=umu,dc=se";
         Attributes searchAttrs = new BasicAttributes("uid", uid);
         // Change to specified number/string
         String[] matchingAttributes = {"*"};//{searchAttrString};
 
         return doLDAPSearch(searchBase, searchAttrs, matchingAttributes);
     }
 
     public static List<PersonBean> toPersonBeans(final NamingEnumeration<SearchResult> resultEnum) throws NamingException {
         List<PersonBean> resultList = new ArrayList<PersonBean>();
         // Print result
         if (!resultEnum.hasMore()) {
             return resultList;
         }
 
         while (resultEnum.hasMore()) {
             Attributes attrs = resultEnum.next().getAttributes();
             PersonBean person = new PersonBean();
 
             // Get name
             String givenName = (String) attrs.get("givenName").get();
             // TODO: verify "sn"
             String familyName = (String) attrs.get("sn").get();
             person.setGivenName(givenName);
             person.setFamilyName(familyName);
             
             String uid = (String) attrs.get("uid").get();
             person.setUid(uid);
 
             //private String employeeType;
             Attribute employeeTypeAttr = attrs.get("employeeType");
             if (employeeTypeAttr != null) {
                person.setEmployeeType(((String) employeeTypeAttr.get());
             }
             
             //private String floor;
             Attribute floorAttr = attrs.get("floor");
             if (floorAttr != null) {
                 person.setFloor((String) floorAttr.get());
             }
             
             //physicalDeliveryOfficeName
             Attribute physicalDeliveryOfficeAttr = attrs.get("physicalDeliveryOfficeName");
             if (physicalDeliveryOfficeAttr != null) {
                 person.setPhysicalDeliveryOffice((String) physicalDeliveryOfficeAttr.get());
             }
             
             //private String street;
             Attribute streetAttr = attrs.get("street");
             if (streetAttr != null) {
                 person.setStreet((String) streetAttr.get());
             }
             //private String postalCode;
             Attribute postalCodeAttr = attrs.get("postalCode");
             if (postalCodeAttr != null) {
                 person.setPostalCode((String) postalCodeAttr.get());
             }
             
             //private String postalAddress;
             Attribute postalAddressAttr = attrs.get("postalAddress");
             if (postalAddressAttr != null) {
                 person.setPostalAddress((String) postalAddressAttr.get());
             }
             
             //private String institution;
             Attribute institutionAttr = attrs.get("institution");
             if (institutionAttr != null) {
                 person.setInstitution((String) institutionAttr.get());
             }
 
             //private String buildingName;
             Attribute buildingNameAttr = attrs.get("buildingName");
             if (buildingNameAttr != null) {
                 person.setBuildingName((String) buildingNameAttr.get());
             }
 
             //private String roomNumber;
             Attribute roomNumberAttr = attrs.get("roomNumber");
             if (roomNumberAttr != null) {
                 person.setRoomNumber((String) roomNumberAttr.get());
             }
             //private String phoneNumber;
             Attribute phoneNumberAttr = attrs.get("telephoneNumber");
             if (phoneNumberAttr != null) {
                 person.setPhoneNumber((String) phoneNumberAttr.get());
             }
             //private String photoURI;
             Attribute photoURIAttr = attrs.get("labeledURI");
             if (photoURIAttr != null) {
                 NamingEnumeration<?> uriEnum = photoURIAttr.getAll();
                 while (uriEnum.hasMore()) {
                     String content = (String) uriEnum.next();
                     content = content.trim();
                     int i = content.indexOf(" ");
                     if (i > 0) {
                         content = content.substring(0, i);
                         try {
                             person.setPhotoURI(new URI(content));
                         } catch (URISyntaxException e) {
                             logger.warn("photoURI not URI: {}", content);
                         }
                         break;
                     }
                 }
             }
 
             // Get all mails
             Attribute mailAttr = attrs.get("mail");
             if (mailAttr != null) {
                 NamingEnumeration<?> mailEnum = mailAttr.getAll();
                 if (mailEnum.hasMore()) {
                     List<String> emails = new ArrayList<String>();
                     while (mailEnum.hasMore()) {
                         //sb.append("\t\t" + ea.next() + "\n");
                         //System.out.println("\t\tmail: " + mailEnum.next());
                         emails.add((String) mailEnum.next());
                     }
                     person.setEmails(emails);
                 }
             }
             resultList.add(person);
         }
         return resultList;
     }
 
     public static NamingEnumeration<SearchResult> searchPerson(String searchString) throws NamingException {
         String searchBase = "cn=person,dc=umu,dc=se";
         String escapedSearchString = escapeLDAPSearchFilter(searchString);
         return createLdapContext().search(searchBase, "(cn=*" + escapedSearchString + "*)", null);
     }
 
     public static String toString(final NamingEnumeration<SearchResult> resultEnum) {
         try {
             StringBuffer sb = new StringBuffer();
             // Print result
             if (!resultEnum.hasMore()) {
                 return "";
             }
 
             while (resultEnum.hasMore()) {
                 Attributes resultAttributes = resultEnum.next().getAttributes();
                 sb.append("Has result: " + resultAttributes + "\n");
 
                 // get all attributes from result
                 for (NamingEnumeration<? extends Attribute> e = resultAttributes.getAll(); e.hasMore();) {
                     Attribute attr = e.next();
                     sb.append("\t" + attr.getID() + "\n");
 
                     // Get all duplicate attributes from current attribute
                     for (NamingEnumeration<?> ea = attr.getAll(); ea.hasMore();) {
                         sb.append("\t\t" + ea.next() + "\n");
                     }
                 }
             }
             return sb.toString();
         } catch (NamingException e) {
             // TODO - fix error message
             return e.getMessage();
         }
     }
 
     /**
      * Escape special characters in search filter.
      * From http://www.owasp.org/index.php/Preventing_LDAP_Injection_in_Java
      * 
      * @param The string to escape
      * @return The escaped string
      */
     private static final String escapeLDAPSearchFilter(String filter) {
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < filter.length(); i++) {
             char curChar = filter.charAt(i);
             switch (curChar) {
             case '\\':
                 sb.append("\\5c");
                 break;
             case '*':
                 sb.append("\\2a");
                 break;
             case '(':
                 sb.append("\\28");
                 break;
             case ')':
                 sb.append("\\29");
                 break;
             case '\u0000': 
                 sb.append("\\00"); 
                 break;
             default:
                 sb.append(curChar);
             }
         }
         return sb.toString();
     }
 
 }
