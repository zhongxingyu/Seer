 /**
  * $RCSfile$
  * $Revision$
  * $Date$
  *
  * Copyright 2003-2004 Jive Software.
  *
  * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.jivesoftware.smackx.packet;
 
 import org.jivesoftware.smack.PacketCollector;
 import org.jivesoftware.smack.SmackConfiguration;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.filter.PacketIDFilter;
 import org.jivesoftware.smack.packet.IQ;
 import org.jivesoftware.smack.packet.XMPPError;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  * A VCard class for use with the
 * <a href="http://www.jivesoftware.com/xmpp/smack/" target="_blank">SMACK jabber library</a>.<p>
  *
  * You should refer to the
  * <a href="http://www.jabber.org/jeps/jep-0054.html" target="_blank">JEP-54 documentation</a>.<p>
  *
  * Please note that this class is incomplete but it does provide the most commonly found
  * information in vCards. Also remember that VCard transfer is not a standard, and the protocol
  * may change or be replaced.<p>
  *
  * <b>Usage:</b>
  * <pre>
  *
  * // To save VCard:
  *
  * VCard vCard = new VCard();
  * vCard.setFirstName("kir");
  * vCard.setLastName("max");
  * vCard.setEmailHome("foo@fee.bar");
  * vCard.setJabberId("jabber@id.org");
  * vCard.setOrganization("Jetbrains, s.r.o");
  * vCard.setNickName("KIR");
  *
  * vCard.setField("TITLE", "Mr");
  * vCard.setAddressFieldHome("STREET", "Some street");
  * vCard.setAddressFieldWork("CTRY", "US");
  * vCard.setPhoneWork("FAX", "3443233");
  *
  * vCard.save(connection);
  *
  * // To load VCard:
  *
  * VCard vCard = new VCard();
  * vCard.load(conn); // load own VCard
  * vCard.load(conn, "joe@foo.bar"); // load someone's VCard
  * </pre>
  *
  * @author Kirill Maximov (kir@maxkir.com)
  */
 public class VCard extends IQ {
 
     /**
      * Phone types:
      * VOICE?, FAX?, PAGER?, MSG?, CELL?, VIDEO?, BBS?, MODEM?, ISDN?, PCS?, PREF?
      */
     private Map homePhones = new HashMap();
     private Map workPhones = new HashMap();
 
 
     /**
      * Address types:
      * POSTAL?, PARCEL?, (DOM | INTL)?, PREF?, POBOX?, EXTADR?, STREET?, LOCALITY?,
      * REGION?, PCODE?, CTRY?
      */
     private Map homeAddr = new HashMap();
     private Map workAddr = new HashMap();
 
     private String firstName;
     private String lastName;
     private String middleName;
 
     private String emailHome;
     private String emailWork;
 
     private String organization;
     private String organizationUnit;
 
     /**
      * Such as DESC ROLE GEO etc.. see JEP-0054
      */
     private Map otherSimpleFields = new HashMap();
 
     public VCard() {
     }
 
     /**
      * Set generic VCard field.
      *
     * @param field value of field
     * @param field NICKNAME, PHOTO, BDAY, JABBERID, MAILER, TZ, GEO, TITLE, ROLE, LOGO,
     *              NOTE, PRODID, REV,
     *              SORT-STRING, SOUND, UID, URL, DESC
      */
     public String getField(String field) {
         return (String) otherSimpleFields.get(field);
     }
 
     /**
      * Set generic VCard field.
      *
      * @param value value of field
      * @param field field to set. See {@link #getField(String)}
      * @see #getField(String)
      */
     public void setField(String field, String value) {
         otherSimpleFields.put(field, value);
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     public String getMiddleName() {
         return middleName;
     }
 
     public void setMiddleName(String middleName) {
         this.middleName = middleName;
     }
 
     public String getNickName() {
         return (String) otherSimpleFields.get("NICKNAME");
     }
 
     public void setNickName(String nickName) {
         otherSimpleFields.put("NICKNAME", nickName);
     }
 
     public String getEmailHome() {
         return emailHome;
     }
 
     public void setEmailHome(String email) {
         this.emailHome = email;
     }
 
     public String getEmailWork() {
         return emailWork;
     }
 
     public void setEmailWork(String emailWork) {
         this.emailWork = emailWork;
     }
 
     public String getJabberId() {
         return (String) otherSimpleFields.get("JABBERID");
     }
 
     public void setJabberId(String jabberId) {
         otherSimpleFields.put("JABBERID", jabberId);
     }
 
     public String getOrganization() {
         return organization;
     }
 
     public void setOrganization(String organization) {
         this.organization = organization;
     }
 
     public String getOrganizationUnit() {
         return organizationUnit;
     }
 
     public void setOrganizationUnit(String organizationUnit) {
         this.organizationUnit = organizationUnit;
     }
 
     /**
      * Get home address field
      *
      * @param addrField one of POSTAL, PARCEL, (DOM | INTL), PREF, POBOX, EXTADR, STREET,
      *                  LOCALITY, REGION, PCODE, CTRY
      */
     public String getAddressFieldHome(String addrField) {
         return (String) homeAddr.get(addrField);
     }
 
     /**
      * Set home address field
      *
      * @param addrField one of POSTAL, PARCEL, (DOM | INTL), PREF, POBOX, EXTADR, STREET,
      *                  LOCALITY, REGION, PCODE, CTRY
      */
     public void setAddressFieldHome(String addrField, String value) {
         homeAddr.put(addrField, value);
     }
 
     /**
      * Get work address field
      *
      * @param addrField one of POSTAL, PARCEL, (DOM | INTL), PREF, POBOX, EXTADR, STREET,
      *                  LOCALITY, REGION, PCODE, CTRY
      */
     public String getAddressFieldWork(String addrField) {
         return (String) workAddr.get(addrField);
     }
 
     /**
      * Set work address field
      *
      * @param addrField one of POSTAL, PARCEL, (DOM | INTL), PREF, POBOX, EXTADR, STREET,
      *                  LOCALITY, REGION, PCODE, CTRY
      */
     public void setAddressFieldWork(String addrField, String value) {
         workAddr.put(addrField, value);
     }
 
 
     /**
      * Set home phone number
      *
      * @param phoneType one of VOICE, FAX, PAGER, MSG, CELL, VIDEO, BBS, MODEM, ISDN, PCS, PREF
      * @param phoneNum  phone number
      */
     public void setPhoneHome(String phoneType, String phoneNum) {
         homePhones.put(phoneType, phoneNum);
     }
 
     /**
      * Get home phone number
      *
      * @param phoneType one of VOICE, FAX, PAGER, MSG, CELL, VIDEO, BBS, MODEM, ISDN, PCS, PREF
      */
     public String getPhoneHome(String phoneType) {
         return (String) homePhones.get(phoneType);
     }
 
     /**
      * Set work phone number
      *
      * @param phoneType one of VOICE, FAX, PAGER, MSG, CELL, VIDEO, BBS, MODEM, ISDN, PCS, PREF
      * @param phoneNum  phone number
      */
     public void setPhoneWork(String phoneType, String phoneNum) {
         workPhones.put(phoneType, phoneNum);
     }
 
     /**
      * Get work phone number
      *
      * @param phoneType one of VOICE, FAX, PAGER, MSG, CELL, VIDEO, BBS, MODEM, ISDN, PCS, PREF
      */
     public String getPhoneWork(String phoneType) {
         return (String) workPhones.get(phoneType);
     }
 
     /**
      * Save this vCard for the user connected by 'connection'. Connection should be authenticated
      * and not anonymous.<p>
      * <p/>
      * NOTE: the method is asynchronous and does not wait for the returned value.
      */
     public void save(XMPPConnection connection) {
         checkAuthenticated(connection);
 
         setType(IQ.Type.SET);
         setFrom(connection.getUser());
         connection.sendPacket(this);
     }
 
     /**
      * Load VCard information for a connected user. Connection should be authenticated
      * and not anonymous.
      */
     public void load(XMPPConnection connection) throws XMPPException {
         checkAuthenticated(connection);
 
         setFrom(connection.getUser());
         doLoad(connection, connection.getUser());
     }
 
     /**
      * Load VCard information for a given user. Connection should be authenticated and not anonymous.
      */
     public void load(XMPPConnection connection, String user) throws XMPPException {
         checkAuthenticated(connection);
 
         setTo(user);
         doLoad(connection, user);
     }
 
     private void doLoad(XMPPConnection connection, String user) throws XMPPException {
         setType(Type.GET);
         PacketCollector collector = connection.createPacketCollector(
                 new PacketIDFilter(getPacketID()));
         connection.sendPacket(this);
 
         VCard result = null;
         try {
             result = (VCard) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
 
             if (result == null) {
                 throw new XMPPException(new XMPPError(408, "Timeout getting VCard information"));
             }
             if (result.getError() != null) {
                 throw new XMPPException(result.getError());
             }
         } catch (ClassCastException e) {
             System.out.println("No VCard for " + user);
         }
 
         copyFieldsFrom(result);
     }
 
     public String getChildElementXML() {
         StringBuffer sb = new StringBuffer();
         new VCardWriter(sb).write();
         return sb.toString();
     }
 
     private void copyFieldsFrom(VCard result) {
         if (result == null) result = new VCard();
 
         Field[] fields = VCard.class.getDeclaredFields();
         for (int i = 0; i < fields.length; i++) {
             Field field = fields[i];
             if (field.getDeclaringClass() == VCard.class &&
                     !Modifier.isFinal(field.getModifiers())) {
                 try {
                     field.setAccessible(true);
                     field.set(this, field.get(result));
                 } catch (IllegalAccessException e) {
                     throw new RuntimeException("This cannot happen:" + field, e);
                 }
             }
         }
     }
 
     private void checkAuthenticated(XMPPConnection connection) {
         if (connection == null) {
             new IllegalArgumentException("No connection was provided");
         }
         if (!connection.isAuthenticated()) {
             new IllegalArgumentException("Connection is not authenticated");
         }
         if (connection.isAnonymous()) {
             new IllegalArgumentException("Connection cannot be anonymous");
         }
     }
 
     private boolean hasContent() {
         //noinspection OverlyComplexBooleanExpression
         return hasNameField()
                 || hasOrganizationFields()
                 || emailHome != null
                 || emailWork != null
                 || otherSimpleFields.size() > 0
                 || homeAddr.size() > 0
                 || homePhones.size() > 0
                 || workAddr.size() > 0
                 || workPhones.size() > 0
                 ;
     }
 
     private boolean hasNameField() {
         return firstName != null || lastName != null || middleName != null;
     }
 
     private boolean hasOrganizationFields() {
         return organization != null || organizationUnit != null;
     }
 
     // Used in tests:
 
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         final VCard vCard = (VCard) o;
 
         if (emailHome != null ? !emailHome.equals(vCard.emailHome) : vCard.emailHome != null) {
             return false;
         }
         if (emailWork != null ? !emailWork.equals(vCard.emailWork) : vCard.emailWork != null) {
             return false;
         }
         if (firstName != null ? !firstName.equals(vCard.firstName) : vCard.firstName != null) {
             return false;
         }
         if (!homeAddr.equals(vCard.homeAddr)) {
             return false;
         }
         if (!homePhones.equals(vCard.homePhones)) {
             return false;
         }
         if (lastName != null ? !lastName.equals(vCard.lastName) : vCard.lastName != null) {
             return false;
         }
         if (middleName != null ? !middleName.equals(vCard.middleName) : vCard.middleName != null) {
             return false;
         }
         if (organization != null ?
                 !organization.equals(vCard.organization) : vCard.organization != null) {
             return false;
         }
         if (organizationUnit != null ?
                 !organizationUnit.equals(vCard.organizationUnit) : vCard.organizationUnit != null) {
             return false;
         }
         if (!otherSimpleFields.equals(vCard.otherSimpleFields)) {
             return false;
         }
         if (!workAddr.equals(vCard.workAddr)) {
             return false;
         }
         if (!workPhones.equals(vCard.workPhones)) {
             return false;
         }
 
         return true;
     }
 
     public int hashCode() {
         int result;
         result = homePhones.hashCode();
         result = 29 * result + workPhones.hashCode();
         result = 29 * result + homeAddr.hashCode();
         result = 29 * result + workAddr.hashCode();
         result = 29 * result + (firstName != null ? firstName.hashCode() : 0);
         result = 29 * result + (lastName != null ? lastName.hashCode() : 0);
         result = 29 * result + (middleName != null ? middleName.hashCode() : 0);
         result = 29 * result + (emailHome != null ? emailHome.hashCode() : 0);
         result = 29 * result + (emailWork != null ? emailWork.hashCode() : 0);
         result = 29 * result + (organization != null ? organization.hashCode() : 0);
         result = 29 * result + (organizationUnit != null ? organizationUnit.hashCode() : 0);
         result = 29 * result + otherSimpleFields.hashCode();
         return result;
     }
 
     public String toString() {
         return getChildElementXML();
     }
 
     //==============================================================
 
     private class VCardWriter {
         private final StringBuffer sb;
 
         VCardWriter(StringBuffer sb) {
             this.sb = sb;
         }
 
         public void write() {
             appendTag("vCard", "xmlns", "vcard-temp", hasContent(), new ContentBuilder() {
                 public void addTagContent() {
                     buildActualContent();
                 }
             });
         }
 
         private void buildActualContent() {
             if (hasNameField()) {
                 appendFN();
                 appendN();
             }
 
             appendOrganization();
             appendGenericFields();
 
             appendEmail(emailWork, "WORK");
             appendEmail(emailHome, "HOME");
 
             appendPhones(workPhones, "WORK");
             appendPhones(homePhones, "HOME");
 
             appendAddress(workAddr, "WORK");
             appendAddress(homeAddr, "HOME");
         }
 
         private void appendEmail(final String email, final String type) {
             if (email != null) {
                 appendTag("EMAIL", true, new ContentBuilder() {
                     public void addTagContent() {
                         appendEmptyTag(type);
                         appendEmptyTag("INTERNET");
                         appendEmptyTag("PREF");
                         appendTag("USERID", email);
                     }
                 });
             }
         }
 
         private void appendPhones(Map phones, final String code) {
             Iterator it = phones.entrySet().iterator();
             while (it.hasNext()) {
                 final Map.Entry entry = (Map.Entry) it.next();
                 appendTag("TEL", true, new ContentBuilder() {
                     public void addTagContent() {
                         appendEmptyTag(entry.getKey());
                         appendEmptyTag(code);
                         appendTag("NUMBER", (String) entry.getValue());
                     }
                 });
             }
         }
 
         private void appendAddress(final Map addr, final String code) {
             if (addr.size() > 0) {
                 appendTag("ADR", true, new ContentBuilder() {
                     public void addTagContent() {
                         appendEmptyTag(code);
 
                         Iterator it = addr.entrySet().iterator();
                         while (it.hasNext()) {
                             final Map.Entry entry = (Map.Entry) it.next();
                             appendTag((String) entry.getKey(), (String) entry.getValue());
                         }
                     }
                 });
             }
         }
 
         private void appendEmptyTag(Object tag) {
             sb.append('<').append(tag).append("/>");
         }
 
         private void appendGenericFields() {
             Iterator it = otherSimpleFields.entrySet().iterator();
             while (it.hasNext()) {
                 Map.Entry entry = (Map.Entry) it.next();
                 appendTag(entry.getKey().toString(), (String) entry.getValue());
             }
         }
 
         private void appendOrganization() {
             if (hasOrganizationFields()) {
                 appendTag("ORG", true, new ContentBuilder() {
                     public void addTagContent() {
                         appendTag("ORGNAME", organization);
                         appendTag("ORGUNIT", organizationUnit);
                     }
                 });
             }
         }
 
         private void appendField(String tag) {
             String value = (String) otherSimpleFields.get(tag);
             appendTag(tag, value);
         }
 
         private void appendFN() {
             final ContentBuilder contentBuilder = new ContentBuilder() {
                 public void addTagContent() {
                     if (firstName != null) {
                         sb.append(firstName + ' ');
                     }
                     if (middleName != null) {
                         sb.append(middleName + ' ');
                     }
                     if (lastName != null) {
                         sb.append(lastName);
                     }
                 }
             };
             appendTag("FN", true, contentBuilder);
         }
 
         private void appendN() {
             appendTag("N", true, new ContentBuilder() {
                 public void addTagContent() {
                     appendTag("FAMILY", lastName);
                     appendTag("GIVEN", firstName);
                     appendTag("MIDDLE", middleName);
                 }
             });
         }
 
         private void appendTag(String tag, String attr, String attrValue, boolean hasContent,
                                ContentBuilder builder) {
             sb.append('<').append(tag);
             if (attr != null) {
                 sb.append(' ').append(attr).append('=').append('\'').append(attrValue).append('\'');
             }
 
             if (hasContent) {
                 sb.append('>');
                 builder.addTagContent();
                 sb.append("</").append(tag).append(">\n");
             } else {
                 sb.append("/>\n");
             }
         }
 
         private void appendTag(String tag, boolean hasContent, ContentBuilder builder) {
             appendTag(tag, null, null, hasContent, builder);
         }
 
         private void appendTag(String tag, final String tagText) {
             if (tagText == null) return;
             final ContentBuilder contentBuilder = new ContentBuilder() {
                 public void addTagContent() {
                     sb.append(tagText.trim());
                 }
             };
             appendTag(tag, true, contentBuilder);
         }
 
     }
 
     //==============================================================
 
     private interface ContentBuilder {
         void addTagContent();
     }
 
     //==============================================================
 }
 
