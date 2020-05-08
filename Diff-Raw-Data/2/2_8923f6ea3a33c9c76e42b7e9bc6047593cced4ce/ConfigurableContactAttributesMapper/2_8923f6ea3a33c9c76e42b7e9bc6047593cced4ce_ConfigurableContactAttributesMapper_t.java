 /**
  * Licensed to Jasig under one or more contributor license
  * agreements. See the NOTICE file distributed with this work
  * for additional information regarding copyright ownership.
  * Jasig licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a
  * copy of the License at:
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.jasig.portlet.contacts.adapters.impl.ldap;
 
 import java.util.Map;
import java.util.List;
 import javax.naming.directory.Attribute;
 import javax.naming.directory.Attributes;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jasig.portlet.contacts.model.Address;
 import org.jasig.portlet.contacts.model.Contact;
 import org.jasig.portlet.contacts.model.EmailAddress;
 import org.jasig.portlet.contacts.model.ModelObjectFactory;
 import org.jasig.portlet.contacts.model.PhoneNumber;
 import org.springframework.ldap.core.AttributesMapper;
 import org.springframework.util.StringUtils;
 
 /**
  *
  * @author mfgsscw2
  */
 public class ConfigurableContactAttributesMapper {
 
     final static Log logger =  LogFactory.getLog(ConfigurableContactAttributesMapper.class);
     // Prefix to identify the LDAP attribute name value is instead a default value to apply.
     String defaultPrefix = "$default:";
 
     Map<String, Object> config;
     ModelObjectFactory factory;
 
     public ConfigurableContactAttributesMapper(Map<String, Object> config, ModelObjectFactory factory) {
         this.config = config;
         this.factory = factory;
     }
 
     public void setDefaultPrefix(String defaultPrefix) {
         this.defaultPrefix = defaultPrefix;
     }
 
     @SuppressWarnings("unchecked")
     // todo consider consolidating core of mapFromAttributes and populate into single method
     // todo consider changing collection property names to match method names and using reflection
     public Object mapFromAttributes(Attributes attrs) {
         Contact contact = factory.getObjectOfType(Contact.class);
 
         // For each property of the contact object, set the value from the corresponding
         // LDAP attribute (if any)
         for(String propertyName : config.keySet()) {
 
             // For dependent objects, construct an aggregate object if the appropriate
             // LDAP attributes are present.
             if (propertyName.equalsIgnoreCase("address")) {
                 List<Map<String,String>> addressMappings = (List<Map<String,String>>)config.get(propertyName);
                 for (Map<String,String> addressMap : addressMappings) {
                     Address addr = populate(Address.class, addressMap, attrs);
                     if (addr.isPopulated())
                         contact.getAddresses().add(addr);
                 }
             } else if (propertyName.equalsIgnoreCase("phone")) {
                 List<Map<String,String>> phoneMappings = (List<Map<String,String>>)config.get(propertyName);
                 for (Map<String,String> phoneMap : phoneMappings) {
                     PhoneNumber phone = populate(PhoneNumber.class, phoneMap, attrs);
                     if (phone.isPopulated())
                         contact.getPhoneNumbers().add(phone);
                 }
             } else if (propertyName.equalsIgnoreCase("email")) {
                 List<Map<String,String>> emailMappings = (List<Map<String,String>>)config.get(propertyName);
                 for (Map<String,String> emailMap : emailMappings) {
                     EmailAddress email = populate(EmailAddress.class, emailMap, attrs);
                     if (email.isPopulated())
                         contact.getEmailAddresses().add(email);
                 }
             } else {
                 setProperty(contact, propertyName, config, attrs);
             }
         }
 
         return contact;
     }
 
     /**
      * Create and populates an object of the indicated class by populating its properties
      * from the LDAP attribute values, using any configured default property values.
      * @param clazz Class to create
      * @param propertyToLDAPNameMap Map of the objects property names to the LDAP attribute names
      * @param attrs LDAP attributes
      * @return Populated object, or null if the LDAP attributes do not contain values that create a
      *         reasonably useful object of the requested type
      */
     private <T extends Object> T populate(Class<T> clazz, Map<String, String> propertyToLDAPNameMap, Attributes attrs) {
         T obj = factory.getObjectOfType(clazz);
 
         for (String propertyName : propertyToLDAPNameMap.keySet()) {
             setProperty(obj, propertyName, propertyToLDAPNameMap, attrs);
         }
         return obj;
     }
 
     /**
      * Sets the object's property from the LDAP attribute value or default property value.
      * @param obj object to set property on
      * @param propertyName property name to set
      * @param propertyNameToLDAPNameMap Map of object property names to ldap attribute names
      * @param attrs LDAP attributes
      * @return Populated object, or null if the LDAP attributes do not contain values that create a
      *         reasonably useful object of the requested type
      */
     private <T> void setProperty(T obj, String propertyName, Map<String,?> propertyNameToLDAPNameMap, Attributes attrs) {
         String method = "set" + StringUtils.capitalize(propertyName);
         String ldapAttributeName = (String) propertyNameToLDAPNameMap.get(propertyName);
         try {
             if (StringUtils.hasLength(ldapAttributeName)) {
                 if (ldapAttributeName.startsWith(defaultPrefix)) {
                     obj.getClass().getMethod(method, String.class).invoke(obj, ldapAttributeName.substring(defaultPrefix.length()));
                 } else {
                     Attribute attr = attrs.get(ldapAttributeName);
                     obj.getClass().getMethod(method, String.class).invoke(obj, getValue(attr));
                     if (attr != null && attr.size() > 1) {
                         logger.warn("Found multiple values for LDAP attribute " + ldapAttributeName
                                 + attrs.get("cn") != null ? ", cn=" + config.get("cn") : "");
                     }
                 }
             }
         } catch (Exception ex) {
             logger.error("Exception setting property for " + obj.getClass().getCanonicalName()
                     + "." + method + ", LDAP attribute " + ldapAttributeName, ex);
         }
     }
 
     private String getValue(Attribute attribute) throws javax.naming.NamingException {
         if(attribute != null) {
             String value = (String)attribute.get();
             if(value != null && !value.equalsIgnoreCase("empty")) {
                 return value;
             }
         }
         return "";
     }
 }
