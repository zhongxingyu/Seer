 package de.fiz.ddb.aas.test;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 
 import javax.naming.NamingException;
 import javax.naming.directory.Attribute;
 import javax.naming.directory.Attributes;
 import javax.naming.directory.BasicAttribute;
 import javax.naming.directory.BasicAttributes;
 import javax.naming.directory.ModificationItem;
 
 import org.apache.http.entity.ContentType;
 import org.junit.runners.Parameterized.Parameters;
 import org.springframework.ldap.NameNotFoundException;
 import org.springframework.ldap.core.AttributesMapper;
 import org.springframework.ldap.core.DirContextAdapter;
 import org.springframework.ldap.core.DistinguishedName;
 import org.springframework.ldap.core.LdapTemplate;
 import org.springframework.ldap.core.support.LdapContextSource;
 
 /**
  * Base Class for tests.
  * Performs same tests with xml and with json.
  * 
  * @author mih
  * 
  */
 public class AasTestBase {
 
     @Parameters
     public static Collection<Object[]> getParameters() {
         Collection<Object[]> parameters = new ArrayList<Object[]>();
         parameters.add(new Object[] { ContentType.APPLICATION_XML });
         parameters.add(new Object[] { ContentType.APPLICATION_JSON });
         return parameters;
     }
 
     /**
      * delete all organizations and users in ldap.
      * create admin-user for tests
      * 
      * @throws Exception
      */
     public static void initLdapData() throws Exception {
         // do cleanup of LDAP
         if (!Constants.LDAP_URL.equals("ldap://localhost:389")) {
             throw new Exception("cleanup of ldap-data not allowed on remote ldap servers");
         }
         LdapTemplate ldapTemplate = getLdapTemplate();
         // remove users
         deleteObjectClass(ldapTemplate, "person", "uid");
         // remove Orgs
         deleteObjectClass(ldapTemplate, "ddbOrganization", "o");
         // remove members of global right ddb_admin
         createOrCleanCn(ldapTemplate, "ddb_admin");
         // remove members of global right ddb_org_admin
         createOrCleanCn(ldapTemplate, "ddb_org_admin");
         // remove members of global right ddb_user_admin
         createOrCleanCn(ldapTemplate, "ddb_user_admin");
         // add admin user
         addAdminUser(ldapTemplate);
     }
 
     /**
      * 
      * create admin-user for tests (if it doesnt exist yet)
      * 
      * @throws Exception
      */
     public static void initAdminUser() throws Exception {
         LdapTemplate ldapTemplate = getLdapTemplate();
         // add admin user
         addAdminUser(ldapTemplate);
     }
 
     /**
      * count the number of testMethods in the testClass.
      * 
      * @return number of testMethods.
      */
     public int getTestAnnotationsCount() {
         Method[] methods = this.getClass().getMethods();
         int count = 0;
         for (int i = 0; i < methods.length; i++) {
             if (methods[i].getAnnotations() != null) {
                 for (Annotation annotation : methods[i].getAnnotations()) {
                     if (annotation.annotationType().equals(org.junit.Test.class)) {
                         count++;
                     }
                 }
             }
         }
         return count;
     }
 
     /**
      * Get LDAP Connection and Template
      * @return LdapTemplate
      * @throws Exception
      */
     private static LdapTemplate getLdapTemplate() throws Exception {
         LdapContextSource ldapContext = new LdapContextSource();
         ldapContext.setUrl(Constants.LDAP_URL);
         ldapContext.setBase(Constants.LDAP_BASE);
         ldapContext.setUserDn(Constants.LDAP_USERNAME);
         ldapContext.setPassword(Constants.LDAP_PASSWORD);
         ldapContext.setPooled(true);
         ldapContext.afterPropertiesSet();
         return new LdapTemplate(ldapContext);
     }
 
     /**
      * 
      * Deletes objects belonging to particular objectClass. 
      * Search for Objects on top-level only.
      * 
      * @param ldapTemplate
      * @param objectClass
      * @param attribute
      */
     private static void deleteObjectClass(
         final LdapTemplate ldapTemplate, final String objectClass, final String attribute) {
        List<?> attributeValues =
            (ldapTemplate.search("", "(objectclass=" + objectClass + ")", 1, new AttributesMapper() {
                 public Object mapFromAttributes(Attributes attrs) throws NamingException {
                     return attrs.get(attribute).get();
                 }
            }));
        for (Object attributeValue : attributeValues) {
             DistinguishedName dn = new DistinguishedName();
            dn.add(attribute, attributeValue.toString());
             ldapTemplate.unbind(dn, true);
         }
     }
 
     /**
      * 
      * Search for given cn.
      * cn represents a right in AAS. Right contains userIds as memebers.
      * If it exists, delete all members, except required default-member.
      * If it doesnt exist, create cn.
      * 
      * @param ldapTemplate
      * @param cn
      */
     private static void createOrCleanCn(final LdapTemplate ldapTemplate, final String cn) {
         DistinguishedName dn = new DistinguishedName();
         dn.add("cn", cn);
         boolean objectExists = true;
         DirContextAdapter cnObject = null;
         try {
             cnObject = (DirContextAdapter) ldapTemplate.lookup(dn);
         }
         catch (NameNotFoundException e) {
             objectExists = false;
         }
         if (!objectExists) {
             Attributes attributes = new BasicAttributes();
             BasicAttribute basicAttribute = new BasicAttribute("objectclass");
             basicAttribute.add("top");
             basicAttribute.add("groupOfNames");
 
             attributes.put(basicAttribute);
             attributes.put("cn", cn);
             attributes.put("description", "DDBPrivilege: " + cn.replace("ddb_", "").toUpperCase());
 
             Attribute defaultMemberAttribute = new BasicAttribute("member", "cn=default," + Constants.LDAP_BASE);
             attributes.put(defaultMemberAttribute);
             ldapTemplate.bind(dn, null, attributes);
         }
         else {
             String[] members = cnObject.getStringAttributes("member");
             HashSet<String> membersSet = new HashSet<String>();
             if (members != null) {
                 for (int i = 0; i < members.length; i++) {
                     membersSet.add(members[i]);
                 }
             }
             boolean defaultContained = false;
             if (membersSet.contains("cn=default," + Constants.LDAP_BASE)) {
                 defaultContained = true;
                 membersSet.remove("cn=default," + Constants.LDAP_BASE);
                 members = new String[membersSet.size()];
                 int i = 0;
                 for (String member : membersSet) {
                     members[i] = member;
                     i++;
                 }
             }
 
             // maybe add default attribute
             if (!defaultContained) {
                 Attribute defaultMemberAttribute = new BasicAttribute("member", "cn=default," + Constants.LDAP_BASE);
                 ModificationItem defaultMemberItem =
                     new ModificationItem(DirContextAdapter.ADD_ATTRIBUTE, defaultMemberAttribute);
                 ldapTemplate.modifyAttributes(dn, new ModificationItem[] { defaultMemberItem });
             }
 
             // remove member attributes
             if (members.length > 0) {
                 ModificationItem[] modificationItems = new ModificationItem[members.length];
                 for (int i = 0; i < members.length; i++) {
                     modificationItems[i] =
                         new ModificationItem(DirContextAdapter.REMOVE_ATTRIBUTE, new BasicAttribute("member",
                             members[i]));
                 }
                 ldapTemplate.modifyAttributes(dn, modificationItems);
             }
         }
     }
 
     /**
      * 
      * create admin-user for tests (if it doesnt exist yet)
      * 
      * @param ldapTemplate
      * @throws IOException
      * @throws NamingException
      */
     private static void addAdminUser(final LdapTemplate ldapTemplate) throws IOException, NamingException {
         DistinguishedName dn = new DistinguishedName();
         dn.add("uid", "admin");
         try {
             ldapTemplate.lookup(dn);
             //if object already exists, return
             return;
         }
         catch (NameNotFoundException e) {
         }
         Attributes attributes = new BasicAttributes();
         BasicAttribute basicAttribute = new BasicAttribute("objectclass");
         basicAttribute.add("top");
         basicAttribute.add("ddbUser");
         basicAttribute.add("javaObject");
         basicAttribute.add("javaSerializedObject");
         basicAttribute.add("posixAccount");
 
         attributes.put(basicAttribute);
         attributes.put("cn", "admin");
         attributes.put("gidNumber", "9000");
         attributes.put("homeDirectory", ".");
         attributes.put("javaClassName", "java.util.properties");
 
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         byte[] serializedProperties;
         ObjectOutputStream oos = new ObjectOutputStream(bos);
         oos.writeObject(new Properties());
         serializedProperties = bos.toByteArray();
         attributes.put("javaSerializedData", serializedProperties);
         attributes.put("mail", "admin@admin.org");
         attributes.put("sn", "admin");
         attributes.put("uid", "admin");
         attributes.put("uidNumber", "1");
         attributes.put("ddbUsernamePrefix", "Herr");
         attributes.put("facsimileTelephoneNumber", "1");
         attributes.put("givenName", "admin");
         attributes.put("telephoneNumber", "1");
         attributes.put("title", "Dr.");
         attributes.put("userPassword", "admin");
         ldapTemplate.bind(dn, null, attributes);
 
         // find admin-right
         DistinguishedName dnAdmin = new DistinguishedName();
         dnAdmin.add("cn", "ddb_admin");
         Attribute memberAttribute = new BasicAttribute("member", "uid=admin," + Constants.LDAP_BASE);
         ModificationItem memberItem = new ModificationItem(DirContextAdapter.ADD_ATTRIBUTE, memberAttribute);
         ldapTemplate.modifyAttributes(dnAdmin, new ModificationItem[] { memberItem });
     }
 
 }
