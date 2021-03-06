 /*
  * DavMail POP/IMAP/SMTP/CalDav/LDAP Exchange Gateway
  * Copyright (C) 2010  Mickael Guessant
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 package davmail.exchange.dav;
 
 import davmail.BundleMessage;
 import davmail.Settings;
 import davmail.exception.DavMailAuthenticationException;
 import davmail.exception.DavMailException;
 import davmail.exception.HttpNotFoundException;
 import davmail.exchange.ExchangeSession;
 import davmail.exchange.VObject;
 import davmail.exchange.XMLStreamUtil;
 import davmail.http.DavGatewayHttpClientFacade;
 import davmail.ui.tray.DavGatewayTray;
 import davmail.util.IOUtil;
 import davmail.util.StringUtil;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.httpclient.*;
 import org.apache.commons.httpclient.methods.*;
 import org.apache.commons.httpclient.util.URIUtil;
 import org.apache.jackrabbit.webdav.DavConstants;
 import org.apache.jackrabbit.webdav.DavException;
 import org.apache.jackrabbit.webdav.MultiStatus;
 import org.apache.jackrabbit.webdav.MultiStatusResponse;
 import org.apache.jackrabbit.webdav.client.methods.CopyMethod;
 import org.apache.jackrabbit.webdav.client.methods.MoveMethod;
 import org.apache.jackrabbit.webdav.client.methods.PropFindMethod;
 import org.apache.jackrabbit.webdav.client.methods.PropPatchMethod;
 import org.apache.jackrabbit.webdav.property.DavProperty;
 import org.apache.jackrabbit.webdav.property.DavPropertyNameSet;
 import org.apache.jackrabbit.webdav.property.DavPropertySet;
 import org.w3c.dom.Node;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 import javax.mail.internet.MimePart;
 import java.io.*;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.zip.GZIPInputStream;
 
 /**
  * Webdav Exchange adapter.
  * Compatible with Exchange 2003 and 2007 with webdav available.
  */
 public class DavExchangeSession extends ExchangeSession {
     protected static enum FolderQueryTraversal {
         Shallow, Deep
     }
 
     protected static final DavPropertyNameSet WELL_KNOWN_FOLDERS = new DavPropertyNameSet();
 
     static {
         WELL_KNOWN_FOLDERS.add(Field.getPropertyName("inbox"));
         WELL_KNOWN_FOLDERS.add(Field.getPropertyName("deleteditems"));
         WELL_KNOWN_FOLDERS.add(Field.getPropertyName("sentitems"));
         WELL_KNOWN_FOLDERS.add(Field.getPropertyName("sendmsg"));
         WELL_KNOWN_FOLDERS.add(Field.getPropertyName("drafts"));
         WELL_KNOWN_FOLDERS.add(Field.getPropertyName("calendar"));
         WELL_KNOWN_FOLDERS.add(Field.getPropertyName("contacts"));
         WELL_KNOWN_FOLDERS.add(Field.getPropertyName("outbox"));
     }
 
     /**
      * Various standard mail boxes Urls
      */
     protected String inboxUrl;
     protected String deleteditemsUrl;
     protected String sentitemsUrl;
     protected String sendmsgUrl;
     protected String draftsUrl;
     protected String calendarUrl;
     protected String contactsUrl;
     protected String outboxUrl;
 
     protected String inboxName;
     protected String deleteditemsName;
     protected String sentitemsName;
     protected String draftsName;
     protected String calendarName;
     protected String contactsName;
     protected String outboxName;
 
     protected static final String USERS = "/users/";
 
     /**
      * Convert logical or relative folder path to exchange folder path.
      *
      * @param folderPath folder name
      * @return folder path
      */
     public String getFolderPath(String folderPath) {
         String exchangeFolderPath;
         // IMAP path
         if (folderPath.startsWith(INBOX)) {
             exchangeFolderPath = mailPath + inboxName + folderPath.substring(INBOX.length());
         } else if (folderPath.startsWith(TRASH)) {
             exchangeFolderPath = mailPath + deleteditemsName + folderPath.substring(TRASH.length());
         } else if (folderPath.startsWith(DRAFTS)) {
             exchangeFolderPath = mailPath + draftsName + folderPath.substring(DRAFTS.length());
         } else if (folderPath.startsWith(SENT)) {
             exchangeFolderPath = mailPath + sentitemsName + folderPath.substring(SENT.length());
         } else if (folderPath.startsWith(CONTACTS)) {
             exchangeFolderPath = mailPath + contactsName + folderPath.substring(CONTACTS.length());
         } else if (folderPath.startsWith(CALENDAR)) {
             exchangeFolderPath = mailPath + calendarName + folderPath.substring(CALENDAR.length());
         } else if (folderPath.startsWith("public")) {
             exchangeFolderPath = publicFolderUrl + folderPath.substring("public".length());
 
             // caldav path
         } else if (folderPath.startsWith(USERS)) {
             // get requested principal
             String principal;
             String localPath;
             int principalIndex = folderPath.indexOf('/', USERS.length());
             if (principalIndex >= 0) {
                 principal = folderPath.substring(USERS.length(), principalIndex);
                 localPath = folderPath.substring(USERS.length() + principal.length() + 1);
                 if (localPath.startsWith(LOWER_CASE_INBOX)) {
                     localPath = inboxName + localPath.substring(LOWER_CASE_INBOX.length());
                 } else if (localPath.startsWith(CALENDAR)) {
                     localPath = calendarName + localPath.substring(CALENDAR.length());
                 } else if (localPath.startsWith(CONTACTS)) {
                     localPath = contactsName + localPath.substring(CONTACTS.length());
                 } else if (localPath.startsWith(ADDRESSBOOK)) {
                     localPath = contactsName + localPath.substring(ADDRESSBOOK.length());
                 }
             } else {
                 principal = folderPath.substring(USERS.length());
                 localPath = "";
             }
             if (principal.length() == 0) {
                 exchangeFolderPath = rootPath;
             } else if (alias.equalsIgnoreCase(principal) || email.equalsIgnoreCase(principal)) {
                 exchangeFolderPath = mailPath + localPath;
             } else {
                 LOGGER.debug("Detected shared path for principal " + principal + ", user principal is " + email);
                 exchangeFolderPath = rootPath + principal + '/' + localPath;
             }
 
             // absolute folder path
         } else if (folderPath.startsWith("/")) {
             exchangeFolderPath = folderPath;
         } else {
             exchangeFolderPath = mailPath + folderPath;
         }
         return exchangeFolderPath;
     }
 
     /**
      * Test if folderPath is inside user mailbox.
      *
      * @param folderPath absolute folder path
      * @return true if folderPath is a public or shared folder
      */
     @Override
     public boolean isSharedFolder(String folderPath) {
         return !getFolderPath(folderPath).toLowerCase().startsWith(mailPath.toLowerCase());
     }
 
     /**
      * LDAP to Exchange Criteria Map
      */
     static final HashMap<String, String> GALFIND_CRITERIA_MAP = new HashMap<String, String>();
 
     static {
         GALFIND_CRITERIA_MAP.put("uid", "AN");
         // assume mail starts with firstname
         GALFIND_CRITERIA_MAP.put("smtpemail1", "FN");
         GALFIND_CRITERIA_MAP.put("displayname", "DN");
         GALFIND_CRITERIA_MAP.put("cn", "DN");
         GALFIND_CRITERIA_MAP.put("givenname", "FN");
         GALFIND_CRITERIA_MAP.put("sn", "LN");
         GALFIND_CRITERIA_MAP.put("title", "TL");
         GALFIND_CRITERIA_MAP.put("company", "CP");
         GALFIND_CRITERIA_MAP.put("o", "CP");
         GALFIND_CRITERIA_MAP.put("l", "OF");
         GALFIND_CRITERIA_MAP.put("department", "DP");
         GALFIND_CRITERIA_MAP.put("apple-group-realname", "DP");
     }
 
     static final HashSet<String> GALLOOKUP_ATTRIBUTES = new HashSet<String>();
 
     static {
         GALLOOKUP_ATTRIBUTES.add("givenname");
         GALLOOKUP_ATTRIBUTES.add("initials");
         GALLOOKUP_ATTRIBUTES.add("sn");
         GALLOOKUP_ATTRIBUTES.add("street");
         GALLOOKUP_ATTRIBUTES.add("st");
         GALLOOKUP_ATTRIBUTES.add("postalcode");
         GALLOOKUP_ATTRIBUTES.add("c");
         GALLOOKUP_ATTRIBUTES.add("departement");
         GALLOOKUP_ATTRIBUTES.add("mobile");
     }
 
     /**
      * Exchange to LDAP attribute map
      */
     static final HashMap<String, String> GALFIND_ATTRIBUTE_MAP = new HashMap<String, String>();
 
     static {
         GALFIND_ATTRIBUTE_MAP.put("uid", "AN");
         GALFIND_ATTRIBUTE_MAP.put("smtpemail1", "EM");
         GALFIND_ATTRIBUTE_MAP.put("cn", "DN");
         GALFIND_ATTRIBUTE_MAP.put("displayName", "DN");
         GALFIND_ATTRIBUTE_MAP.put("telephoneNumber", "PH");
         GALFIND_ATTRIBUTE_MAP.put("l", "OFFICE");
         GALFIND_ATTRIBUTE_MAP.put("o", "CP");
         GALFIND_ATTRIBUTE_MAP.put("title", "TL");
 
         GALFIND_ATTRIBUTE_MAP.put("givenName", "first");
         GALFIND_ATTRIBUTE_MAP.put("initials", "initials");
         GALFIND_ATTRIBUTE_MAP.put("sn", "last");
         GALFIND_ATTRIBUTE_MAP.put("street", "street");
         GALFIND_ATTRIBUTE_MAP.put("st", "state");
         GALFIND_ATTRIBUTE_MAP.put("postalcode", "zip");
         GALFIND_ATTRIBUTE_MAP.put("co", "country");
         GALFIND_ATTRIBUTE_MAP.put("department", "department");
         GALFIND_ATTRIBUTE_MAP.put("mobile", "mobile");
         GALFIND_ATTRIBUTE_MAP.put("roomnumber", "office");
     }
 
     @Override
     public Map<String, ExchangeSession.Contact> galFind(Condition condition, Set<String> returningAttributes, int sizeLimit) throws IOException {
         Map<String, ExchangeSession.Contact> contacts = new HashMap<String, ExchangeSession.Contact>();
         if (condition instanceof MultiCondition) {
             // TODO
         } else if (condition instanceof AttributeCondition) {
             String searchAttribute = GALFIND_CRITERIA_MAP.get(((ExchangeSession.AttributeCondition) condition).getAttributeName());
             if (searchAttribute != null) {
                 String searchValue = ((ExchangeSession.AttributeCondition) condition).getValue();
                 Map<String, Map<String, String>> results;
                 GetMethod getMethod = new GetMethod(URIUtil.encodePathQuery(getCmdBasePath() + "?Cmd=galfind&" + searchAttribute + '=' + searchValue));
                 try {
                     DavGatewayHttpClientFacade.executeGetMethod(httpClient, getMethod, true);
                     results = XMLStreamUtil.getElementContentsAsMap(getMethod.getResponseBodyAsStream(), "item", "AN");
                 } finally {
                     getMethod.releaseConnection();
                 }
                 LOGGER.debug("galfind " + searchAttribute + '=' + searchValue + ": " + results.size() + " result(s)");
                 for (Map<String, String> result : results.values()) {
                     Contact contact = new Contact();
                     contact.setName(result.get("AN"));
                     contact.put("uid", result.get("AN"));
                     buildGalfindContact(contact, result);
                     if (condition.isMatch(contact)) {
                         if (needGalLookup(returningAttributes)) {
                             galLookup(contact);
                         }
                         contacts.put(contact.getName().toLowerCase(), contact);
                     }
                 }
             }
 
         }
         return contacts;
     }
 
     protected boolean needGalLookup(Set<String> returningAttributes) {
         // iCal search, do not call gallookup
        if (returningAttributes.contains("apple-serviceslocator")) {
             return false;
             // return all attributes => call gallookup
         } else if (returningAttributes.isEmpty()) {
             return true;
         }
 
         for (String attributeName : GALLOOKUP_ATTRIBUTES) {
             if (returningAttributes.contains(attributeName)) {
                 return true;
             }
         }
         return false;
     }
 
     private boolean disableGalLookup;
 
     /**
      * Get extended address book information for person with gallookup.
      * Does not work with Exchange 2007
      *
      * @param contact galfind contact
      */
     public void galLookup(Contact contact) {
         if (!disableGalLookup) {
             GetMethod getMethod = null;
             try {
                 getMethod = new GetMethod(URIUtil.encodePathQuery(getCmdBasePath() + "?Cmd=gallookup&ADDR=" + contact.get("smtpemail1")));
                 DavGatewayHttpClientFacade.executeGetMethod(httpClient, getMethod, true);
                 Map<String, Map<String, String>> results = XMLStreamUtil.getElementContentsAsMap(getMethod.getResponseBodyAsStream(), "person", "alias");
                 // add detailed information
                 if (!results.isEmpty()) {
                     Map<String, String> personGalLookupDetails = results.get(contact.get("uid").toLowerCase());
                     if (personGalLookupDetails != null) {
                         buildGalfindContact(contact, personGalLookupDetails);
                     }
                 }
             } catch (IOException e) {
                 LOGGER.warn("Unable to gallookup person: " + contact + ", disable GalLookup");
                 disableGalLookup = true;
             } finally {
                 if (getMethod != null) {
                     getMethod.releaseConnection();
                 }
             }
         }
     }
 
     protected void buildGalfindContact(Contact contact, Map<String, String> response) {
         for (Map.Entry<String, String> entry : GALFIND_ATTRIBUTE_MAP.entrySet()) {
             String attributeValue = response.get(entry.getValue());
             if (attributeValue != null) {
                 contact.put(entry.getKey(), attributeValue);
             }
         }
     }
 
     @Override
     protected String getFreeBusyData(String attendee, String start, String end, int interval) throws IOException {
         String freebusyUrl = publicFolderUrl + "/?cmd=freebusy" +
                 "&start=" + start +
                 "&end=" + end +
                 "&interval=" + interval +
                 "&u=SMTP:" + attendee;
         GetMethod getMethod = new GetMethod(freebusyUrl);
         getMethod.setRequestHeader("Content-Type", "text/xml");
         String fbdata = null;
         try {
             DavGatewayHttpClientFacade.executeGetMethod(httpClient, getMethod, true);
             fbdata = StringUtil.getLastToken(getMethod.getResponseBodyAsString(), "<a:fbdata>", "</a:fbdata>");
         } finally {
             getMethod.releaseConnection();
         }
         return fbdata;
     }
 
     /**
      * @inheritDoc
      */
     public DavExchangeSession(String url, String userName, String password) throws IOException {
         super(url, userName, password);
     }
 
     @Override
     protected void buildSessionInfo(HttpMethod method) throws DavMailException {
         buildMailPath(method);
 
         // get base http mailbox http urls
         getWellKnownFolders();
     }
 
     static final String BASE_HREF = "<base href=\"";
 
     protected void buildMailPath(HttpMethod method) throws DavMailAuthenticationException {
         // find base url
         String line;
 
         // get user mail URL from html body (multi frame)
         BufferedReader mainPageReader = null;
         try {
             mainPageReader = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
             //noinspection StatementWithEmptyBody
             while ((line = mainPageReader.readLine()) != null && line.toLowerCase().indexOf(BASE_HREF) == -1) {
             }
             if (line != null) {
                 int start = line.toLowerCase().indexOf(BASE_HREF) + BASE_HREF.length();
                 int end = line.indexOf('\"', start);
                 String mailBoxBaseHref = line.substring(start, end);
                 URL baseURL = new URL(mailBoxBaseHref);
                 mailPath = baseURL.getPath();
                 LOGGER.debug("Base href found in body, mailPath is " + mailPath);
                 buildEmail(method.getURI().getHost());
                 LOGGER.debug("Current user email is " + email);
             } else {
                 // failover for Exchange 2007 : build standard mailbox link with email
                 buildEmail(method.getURI().getHost());
                 mailPath = "/exchange/" + email + '/';
                 LOGGER.debug("Current user email is " + email + ", mailPath is " + mailPath);
             }
             rootPath = mailPath.substring(0, mailPath.lastIndexOf('/', mailPath.length() - 2) + 1);
         } catch (IOException e) {
             LOGGER.error("Error parsing main page at " + method.getPath(), e);
         } finally {
             if (mainPageReader != null) {
                 try {
                     mainPageReader.close();
                 } catch (IOException e) {
                     LOGGER.error("Error parsing main page at " + method.getPath());
                 }
             }
             method.releaseConnection();
         }
 
 
         if (mailPath == null || email == null) {
             throw new DavMailAuthenticationException("EXCEPTION_AUTHENTICATION_FAILED_PASSWORD_EXPIRED");
         }
     }
 
     protected String getURIPropertyIfExists(DavPropertySet properties, String alias) throws URIException {
         DavProperty property = properties.get(Field.getPropertyName(alias));
         if (property == null) {
             return null;
         } else {
             return URIUtil.decode((String) property.getValue());
         }
     }
 
     // return last folder name from url
 
     protected String getFolderName(String url) {
         if (url != null) {
             if (url.endsWith("/")) {
                 return url.substring(url.lastIndexOf('/', url.length() - 2) + 1);
             } else if (url.indexOf('/') > 0) {
                 return url.substring(url.lastIndexOf('/') + 1);
             } else {
                 return null;
             }
         } else {
             return null;
         }
     }
 
     protected void getWellKnownFolders() throws DavMailException {
         // Retrieve well known URLs
         MultiStatusResponse[] responses;
         try {
             responses = DavGatewayHttpClientFacade.executePropFindMethod(
                     httpClient, URIUtil.encodePath(mailPath), 0, WELL_KNOWN_FOLDERS);
             if (responses.length == 0) {
                 throw new DavMailException("EXCEPTION_UNABLE_TO_GET_MAIL_FOLDER", mailPath);
             }
             DavPropertySet properties = responses[0].getProperties(HttpStatus.SC_OK);
             inboxUrl = getURIPropertyIfExists(properties, "inbox");
             inboxName = getFolderName(inboxUrl);
             deleteditemsUrl = getURIPropertyIfExists(properties, "deleteditems");
             deleteditemsName = getFolderName(deleteditemsUrl);
             sentitemsUrl = getURIPropertyIfExists(properties, "sentitems");
             sentitemsName = getFolderName(sentitemsUrl);
             sendmsgUrl = getURIPropertyIfExists(properties, "sendmsg");
             draftsUrl = getURIPropertyIfExists(properties, "drafts");
             draftsName = getFolderName(draftsUrl);
             calendarUrl = getURIPropertyIfExists(properties, "calendar");
             calendarName = getFolderName(calendarUrl);
             contactsUrl = getURIPropertyIfExists(properties, "contacts");
             contactsName = getFolderName(contactsUrl);
             outboxUrl = getURIPropertyIfExists(properties, "outbox");
             outboxName = getFolderName(outboxUrl);
             // junk folder not available over webdav
 
             // default public folder path
             publicFolderUrl = PUBLIC_ROOT;
 
             // check public folder access
             try {
                 if (inboxUrl != null) {
                     // try to build full public URI from inboxUrl
                     URI publicUri = new URI(inboxUrl, false);
                     publicUri.setPath(PUBLIC_ROOT);
                     publicFolderUrl = publicUri.getURI();
                 }
                 DavPropertyNameSet davPropertyNameSet = new DavPropertyNameSet();
                 davPropertyNameSet.add(Field.getPropertyName("displayname"));
                 PropFindMethod propFindMethod = new PropFindMethod(publicFolderUrl, davPropertyNameSet, 0);
                 try {
                     DavGatewayHttpClientFacade.executeMethod(httpClient, propFindMethod);
                 } catch (IOException e) {
                     // workaround for NTLM authentication only on /public
                     if (!DavGatewayHttpClientFacade.hasNTLM(httpClient)) {
                         DavGatewayHttpClientFacade.addNTLM(httpClient);
                         DavGatewayHttpClientFacade.executeMethod(httpClient, propFindMethod);
                     }
                 }
                 // update public folder URI
                 publicFolderUrl = propFindMethod.getURI().getURI();
             } catch (IOException e) {
                 LOGGER.warn("Public folders not available: " + (e.getMessage() == null ? e : e.getMessage()));
                 publicFolderUrl = "/public";
             }
 
             LOGGER.debug("Inbox URL: " + inboxUrl +
                     " Trash URL: " + deleteditemsUrl +
                     " Sent URL: " + sentitemsUrl +
                     " Send URL: " + sendmsgUrl +
                     " Drafts URL: " + draftsUrl +
                     " Calendar URL: " + calendarUrl +
                     " Contacts URL: " + contactsUrl +
                     " Outbox URL: " + outboxUrl +
                     " Public folder URL: " + publicFolderUrl
             );
         } catch (IOException e) {
             LOGGER.error(e.getMessage());
             throw new DavMailAuthenticationException("EXCEPTION_UNABLE_TO_GET_MAIL_FOLDER", mailPath);
         }
     }
 
     protected static class MultiCondition extends ExchangeSession.MultiCondition {
         protected MultiCondition(Operator operator, Condition... condition) {
             super(operator, condition);
         }
 
         public void appendTo(StringBuilder buffer) {
             boolean first = true;
 
             for (Condition condition : conditions) {
                 if (condition != null && !condition.isEmpty()) {
                     if (first) {
                         buffer.append('(');
                         first = false;
                     } else {
                         buffer.append(' ').append(operator).append(' ');
                     }
                     condition.appendTo(buffer);
                 }
             }
             // at least one non empty condition
             if (!first) {
                 buffer.append(')');
             }
         }
     }
 
     protected static class NotCondition extends ExchangeSession.NotCondition {
         protected NotCondition(Condition condition) {
             super(condition);
         }
 
         public void appendTo(StringBuilder buffer) {
             buffer.append("(Not ");
             condition.appendTo(buffer);
             buffer.append(')');
         }
     }
 
     static final Map<Operator, String> operatorMap = new HashMap<Operator, String>();
 
     static {
         operatorMap.put(Operator.IsEqualTo, " = ");
         operatorMap.put(Operator.IsGreaterThanOrEqualTo, " >= ");
         operatorMap.put(Operator.IsGreaterThan, " > ");
         operatorMap.put(Operator.IsLessThanOrEqualTo, " <= ");
         operatorMap.put(Operator.IsLessThan, " < ");
         operatorMap.put(Operator.Like, " like ");
         operatorMap.put(Operator.IsNull, " is null");
         operatorMap.put(Operator.IsFalse, " = false");
         operatorMap.put(Operator.IsTrue, " = true");
         operatorMap.put(Operator.StartsWith, " = ");
         operatorMap.put(Operator.Contains, " = ");
     }
 
     protected static class AttributeCondition extends ExchangeSession.AttributeCondition {
         protected boolean isIntValue;
 
         protected AttributeCondition(String attributeName, Operator operator, String value) {
             super(attributeName, operator, value);
         }
 
         protected AttributeCondition(String attributeName, Operator operator, int value) {
             super(attributeName, operator, String.valueOf(value));
             isIntValue = true;
         }
 
         public void appendTo(StringBuilder buffer) {
             Field field = Field.get(attributeName);
             buffer.append('"').append(field.getUri()).append('"');
             buffer.append(operatorMap.get(operator));
             //noinspection VariableNotUsedInsideIf
             if (field.cast != null) {
                 buffer.append("CAST (\"");
             } else if (!isIntValue && !field.isIntValue()) {
                 buffer.append('\'');
             }
             if (Operator.Like == operator) {
                 buffer.append('%');
             }
             if ("urlcompname".equals(field.alias)) {
                 buffer.append(StringUtil.encodeUrlcompname(value));
             } else if (field.isIntValue()) {
                 // check value
                 try {
                     Integer.parseInt(value);
                     buffer.append(value);
                 } catch (NumberFormatException e) {
                     // invalid value, replace with 0
                     buffer.append('0');
                 }
             } else {
                 buffer.append(value);
             }
             if (Operator.Like == operator || Operator.StartsWith == operator) {
                 buffer.append('%');
             }
             if (field.cast != null) {
                 buffer.append("\" as '").append(field.cast).append("')");
             } else if (!isIntValue && !field.isIntValue()) {
                 buffer.append('\'');
             }
         }
 
         public boolean isMatch(ExchangeSession.Contact contact) {
             String lowerCaseValue = value.toLowerCase();
             String actualValue = contact.get(attributeName);
             if (actualValue == null) {
                 return false;
             }
             actualValue = actualValue.toLowerCase();
             if (operator == Operator.IsEqualTo) {
                 return actualValue.equals(lowerCaseValue);
             } else if (operator == Operator.Like) {
                 return actualValue.contains(lowerCaseValue);
             } else if (operator == Operator.StartsWith) {
                 return actualValue.startsWith(lowerCaseValue);
             } else {
                 return false;
             }
         }
     }
 
     protected static class HeaderCondition extends AttributeCondition {
 
         protected HeaderCondition(String attributeName, Operator operator, String value) {
             super(attributeName, operator, value);
         }
 
         @Override
         public void appendTo(StringBuilder buffer) {
             buffer.append('"').append(Field.getHeader(attributeName).getUri()).append('"');
             buffer.append(operatorMap.get(operator));
             buffer.append('\'');
             if (Operator.Like == operator) {
                 buffer.append('%');
             }
             buffer.append(value);
             if (Operator.Like == operator) {
                 buffer.append('%');
             }
             buffer.append('\'');
         }
     }
 
     protected static class MonoCondition extends ExchangeSession.MonoCondition {
         protected MonoCondition(String attributeName, Operator operator) {
             super(attributeName, operator);
         }
 
         public void appendTo(StringBuilder buffer) {
             buffer.append('"').append(Field.get(attributeName).getUri()).append('"');
             buffer.append(operatorMap.get(operator));
         }
     }
 
     @Override
     public ExchangeSession.MultiCondition and(Condition... condition) {
         return new MultiCondition(Operator.And, condition);
     }
 
     @Override
     public ExchangeSession.MultiCondition or(Condition... condition) {
         return new MultiCondition(Operator.Or, condition);
     }
 
     @Override
     public Condition not(Condition condition) {
         if (condition == null) {
             return null;
         } else {
             return new NotCondition(condition);
         }
     }
 
     @Override
     public Condition isEqualTo(String attributeName, String value) {
         return new AttributeCondition(attributeName, Operator.IsEqualTo, value);
     }
 
     @Override
     public Condition isEqualTo(String attributeName, int value) {
         return new AttributeCondition(attributeName, Operator.IsEqualTo, value);
     }
 
     @Override
     public Condition headerIsEqualTo(String headerName, String value) {
         return new HeaderCondition(headerName, Operator.IsEqualTo, value);
     }
 
     @Override
     public Condition gte(String attributeName, String value) {
         return new AttributeCondition(attributeName, Operator.IsGreaterThanOrEqualTo, value);
     }
 
     @Override
     public Condition lte(String attributeName, String value) {
         return new AttributeCondition(attributeName, Operator.IsLessThanOrEqualTo, value);
     }
 
     @Override
     public Condition lt(String attributeName, String value) {
         return new AttributeCondition(attributeName, Operator.IsLessThan, value);
     }
 
     @Override
     public Condition gt(String attributeName, String value) {
         return new AttributeCondition(attributeName, Operator.IsGreaterThan, value);
     }
 
     @Override
     public Condition contains(String attributeName, String value) {
         return new AttributeCondition(attributeName, Operator.Like, value);
     }
 
     @Override
     public Condition startsWith(String attributeName, String value) {
         return new AttributeCondition(attributeName, Operator.StartsWith, value);
     }
 
     @Override
     public Condition isNull(String attributeName) {
         return new MonoCondition(attributeName, Operator.IsNull);
     }
 
     @Override
     public Condition isTrue(String attributeName) {
         return new MonoCondition(attributeName, Operator.IsTrue);
     }
 
     @Override
     public Condition isFalse(String attributeName) {
         return new MonoCondition(attributeName, Operator.IsFalse);
     }
 
     /**
      * @inheritDoc
      */
     public class Contact extends ExchangeSession.Contact {
         /**
          * Build Contact instance from multistatusResponse info
          *
          * @param multiStatusResponse response
          * @throws URIException     on error
          * @throws DavMailException on error
          */
         public Contact(MultiStatusResponse multiStatusResponse) throws URIException, DavMailException {
             setHref(URIUtil.decode(multiStatusResponse.getHref()));
             DavPropertySet properties = multiStatusResponse.getProperties(HttpStatus.SC_OK);
             permanentUrl = getPropertyIfExists(properties, "permanenturl");
             etag = getPropertyIfExists(properties, "etag");
             displayName = getPropertyIfExists(properties, "displayname");
             for (String attributeName : CONTACT_ATTRIBUTES) {
                 String value = getPropertyIfExists(properties, attributeName);
                 if (value != null) {
                     if ("bday".equals(attributeName) || "anniversary".equals(attributeName)
                             || "lastmodified".equals(attributeName) || "datereceived".equals(attributeName)) {
                         value = convertDateFromExchange(value);
                     } else if ("haspicture".equals(attributeName) || "private".equals(attributeName)) {
                         value = "1".equals(value) ? "true" : "false";
                     }
                     put(attributeName, value);
                 }
             }
         }
 
         /**
          * @inheritDoc
          */
         public Contact(String folderPath, String itemName, Map<String, String> properties, String etag, String noneMatch) {
             super(folderPath, itemName, properties, etag, noneMatch);
         }
 
         public Contact() {
         }
 
         protected Set<PropertyValue> buildProperties() {
             Set<PropertyValue> propertyValues = new HashSet<PropertyValue>();
             for (Map.Entry<String, String> entry : entrySet()) {
                 String key = entry.getKey();
                 if (!"photo".equals(key)) {
                     propertyValues.add(Field.createPropertyValue(key, entry.getValue()));
                 }
             }
 
             return propertyValues;
         }
 
         protected ExchangePropPatchMethod internalCreateOrUpdate(String encodedHref) throws IOException {
             ExchangePropPatchMethod propPatchMethod = new ExchangePropPatchMethod(encodedHref, buildProperties());
             propPatchMethod.setRequestHeader("Translate", "f");
             if (etag != null) {
                 propPatchMethod.setRequestHeader("If-Match", etag);
             }
             if (noneMatch != null) {
                 propPatchMethod.setRequestHeader("If-None-Match", noneMatch);
             }
             try {
                 httpClient.executeMethod(propPatchMethod);
             } finally {
                 propPatchMethod.releaseConnection();
             }
             return propPatchMethod;
         }
 
         /**
          * Create or update contact
          *
          * @return action result
          * @throws IOException on error
          */
         public ItemResult createOrUpdate() throws IOException {
             String encodedHref = URIUtil.encodePath(getHref());
             ExchangePropPatchMethod propPatchMethod = internalCreateOrUpdate(encodedHref);
             int status = propPatchMethod.getStatusCode();
             if (status == HttpStatus.SC_MULTI_STATUS) {
                 status = propPatchMethod.getResponseStatusCode();
                 //noinspection VariableNotUsedInsideIf
                 if (status == HttpStatus.SC_CREATED) {
                     LOGGER.debug("Created contact " + encodedHref);
                 } else {
                     LOGGER.debug("Updated contact " + encodedHref);
                 }
             } else if (status == HttpStatus.SC_NOT_FOUND) {
                 LOGGER.debug("Contact not found at " + encodedHref + ", searching permanenturl by urlcompname");
                 // failover, search item by urlcompname
                 MultiStatusResponse[] responses = searchItems(folderPath, EVENT_REQUEST_PROPERTIES, DavExchangeSession.this.isEqualTo("urlcompname", convertItemNameToEML(itemName)), FolderQueryTraversal.Shallow, 1);
                 if (responses.length == 1) {
                     encodedHref = getPropertyIfExists(responses[0].getProperties(HttpStatus.SC_OK), "permanenturl");
                     LOGGER.warn("Contact found, permanenturl is " + encodedHref);
                     propPatchMethod = internalCreateOrUpdate(encodedHref);
                     status = propPatchMethod.getStatusCode();
                     if (status == HttpStatus.SC_MULTI_STATUS) {
                         status = propPatchMethod.getResponseStatusCode();
                         LOGGER.debug("Updated contact " + encodedHref);
                     } else {
                         LOGGER.warn("Unable to create or update contact " + status + ' ' + propPatchMethod.getStatusLine());
                     }
                 }
 
             } else {
                 LOGGER.warn("Unable to create or update contact " + status + ' ' + propPatchMethod.getStatusLine());
             }
             ItemResult itemResult = new ItemResult();
             // 440 means forbidden on Exchange
             if (status == 440) {
                 status = HttpStatus.SC_FORBIDDEN;
             }
             itemResult.status = status;
 
             if (status == HttpStatus.SC_OK || status == HttpStatus.SC_CREATED) {
                 String contactPictureUrl = URIUtil.encodePath(getHref() + "/ContactPicture.jpg");
                 String photo = get("photo");
                 if (photo != null) {
                     // need to update photo
                     byte[] resizedImageBytes = IOUtil.resizeImage(Base64.decodeBase64(photo.getBytes()), 90);
 
                     final PutMethod putmethod = new PutMethod(contactPictureUrl);
                     putmethod.setRequestHeader("Overwrite", "t");
                     putmethod.setRequestHeader("Content-Type", "image/jpeg");
                     putmethod.setRequestEntity(new ByteArrayRequestEntity(resizedImageBytes, "image/jpeg"));
                     try {
                         status = httpClient.executeMethod(putmethod);
                         if (status != HttpStatus.SC_OK && status != HttpStatus.SC_CREATED) {
                             throw new IOException("Unable to update contact picture: " + status + ' ' + putmethod.getStatusLine());
                         }
                     } catch (IOException e) {
                         LOGGER.error("Error in contact photo create or update", e);
                         throw e;
                     } finally {
                         putmethod.releaseConnection();
                     }
 
                     Set<PropertyValue> picturePropertyValues = new HashSet<PropertyValue>();
                     picturePropertyValues.add(Field.createPropertyValue("attachmentContactPhoto", "true"));
                     // picturePropertyValues.add(Field.createPropertyValue("renderingPosition", "-1"));
                     picturePropertyValues.add(Field.createPropertyValue("attachExtension", ".jpg"));
 
                     final ExchangePropPatchMethod attachmentPropPatchMethod = new ExchangePropPatchMethod(contactPictureUrl, picturePropertyValues);
                     try {
                         status = httpClient.executeMethod(attachmentPropPatchMethod);
                         if (status != HttpStatus.SC_MULTI_STATUS) {
                             LOGGER.error("Error in contact photo create or update: " + attachmentPropPatchMethod.getStatusCode());
                             throw new IOException("Unable to update contact picture");
                         }
                     } finally {
                         attachmentPropPatchMethod.releaseConnection();
                     }
 
                 } else {
                     // try to delete picture
                     DeleteMethod deleteMethod = new DeleteMethod(contactPictureUrl);
                     try {
                         status = httpClient.executeMethod(deleteMethod);
                         if (status != HttpStatus.SC_OK && status != HttpStatus.SC_NOT_FOUND) {
                             LOGGER.error("Error in contact photo delete: " + status);
                             throw new IOException("Unable to delete contact picture");
                         }
                     } finally {
                         deleteMethod.releaseConnection();
                     }
                 }
                 // need to retrieve new etag
                 HeadMethod headMethod = new HeadMethod(URIUtil.encodePath(getHref()));
                 try {
                     httpClient.executeMethod(headMethod);
                     if (headMethod.getResponseHeader("ETag") != null) {
                         itemResult.etag = headMethod.getResponseHeader("ETag").getValue();
                     }
                 } finally {
                     headMethod.releaseConnection();
                 }
             }
             return itemResult;
 
         }
 
     }
 
     /**
      * @inheritDoc
      */
     public class Event extends ExchangeSession.Event {
         /**
          * Build Event instance from response info.
          *
          * @param multiStatusResponse response
          * @throws URIException on error
          */
         public Event(MultiStatusResponse multiStatusResponse) throws URIException {
             setHref(URIUtil.decode(multiStatusResponse.getHref()));
             DavPropertySet properties = multiStatusResponse.getProperties(HttpStatus.SC_OK);
             permanentUrl = getPropertyIfExists(properties, "permanenturl");
             etag = getPropertyIfExists(properties, "etag");
             displayName = getPropertyIfExists(properties, "displayname");
         }
 
 
         /**
          * @inheritDoc
          */
         public Event(String folderPath, String itemName, String contentClass, String itemBody, String etag, String noneMatch) throws IOException {
             super(folderPath, itemName, contentClass, itemBody, etag, noneMatch);
         }
 
         protected static final String TEXT_CALENDAR = "text/calendar";
         protected static final String APPLICATION_ICS = "application/ics";
 
         protected boolean isCalendarContentType(String contentType) {
             return TEXT_CALENDAR.regionMatches(true, 0, contentType, 0, TEXT_CALENDAR.length()) ||
                     APPLICATION_ICS.regionMatches(true, 0, contentType, 0, APPLICATION_ICS.length());
         }
 
         protected MimePart getCalendarMimePart(MimeMultipart multiPart) throws IOException, MessagingException {
             MimePart bodyPart = null;
             for (int i = 0; i < multiPart.getCount(); i++) {
                 String contentType = multiPart.getBodyPart(i).getContentType();
                 if (isCalendarContentType(contentType)) {
                     bodyPart = (MimePart) multiPart.getBodyPart(i);
                     break;
                 } else if (contentType.startsWith("multipart")) {
                     Object content = multiPart.getBodyPart(i).getContent();
                     if (content instanceof MimeMultipart) {
                         bodyPart = getCalendarMimePart((MimeMultipart) content);
                     }
                 }
             }
 
             return bodyPart;
         }
 
         /**
          * Load ICS content from MIME message input stream
          *
          * @param mimeInputStream mime message input stream
          * @return mime message ics attachment body
          * @throws IOException        on error
          * @throws MessagingException on error
          */
         protected byte[] getICS(InputStream mimeInputStream) throws IOException, MessagingException {
             byte[] result;
             MimeMessage mimeMessage = new MimeMessage(null, mimeInputStream);
             Object mimeBody = mimeMessage.getContent();
             MimePart bodyPart = null;
             if (mimeBody instanceof MimeMultipart) {
                 bodyPart = getCalendarMimePart((MimeMultipart) mimeBody);
             } else if (isCalendarContentType(mimeMessage.getContentType())) {
                 // no multipart, single body
                 bodyPart = mimeMessage;
             }
 
             if (bodyPart != null) {
                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 bodyPart.getDataHandler().writeTo(baos);
                 baos.close();
                 result = baos.toByteArray();
             } else {
                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 mimeMessage.writeTo(baos);
                 baos.close();
                 throw new DavMailException("EXCEPTION_INVALID_MESSAGE_CONTENT", new String(baos.toByteArray(), "UTF-8"));
             }
             return result;
         }
 
         protected byte[] getICSFromInternetContentProperty() throws IOException, DavException, MessagingException {
             byte[] result = null;
             // PropFind PR_INTERNET_CONTENT
             DavPropertyNameSet davPropertyNameSet = new DavPropertyNameSet();
             davPropertyNameSet.add(Field.getPropertyName("internetContent"));
             PropFindMethod propFindMethod = new PropFindMethod(URIUtil.encodePath(permanentUrl), davPropertyNameSet, 0);
             try {
                 DavGatewayHttpClientFacade.executeHttpMethod(httpClient, propFindMethod);
                 MultiStatus responses = propFindMethod.getResponseBodyAsMultiStatus();
                 if (responses.getResponses().length > 0) {
                     DavPropertySet properties = responses.getResponses()[0].getProperties(HttpStatus.SC_OK);
                     String propertyValue = getPropertyIfExists(properties, "internetContent");
                     if (propertyValue != null) {
                         byte[] byteArray = Base64.decodeBase64(propertyValue.getBytes());
                         result = getICS(new ByteArrayInputStream(byteArray));
                     }
                 }
             } finally {
                 propFindMethod.releaseConnection();
             }
             return result;
         }
 
         /**
          * Load ICS content from Exchange server.
          * User Translate: f header to get MIME event content and get ICS attachment from it
          *
          * @return ICS (iCalendar) event
          * @throws HttpException on error
          */
         @Override
         public byte[] getEventContent() throws IOException {
             byte[] result;
             LOGGER.debug("Get event: " + permanentUrl);
             // try to get PR_INTERNET_CONTENT
             try {
                 result = getICSFromInternetContentProperty();
                 if (result == null) {
                     GetMethod method = new GetMethod(permanentUrl);
                     method.setRequestHeader("Content-Type", "text/xml; charset=utf-8");
                     method.setRequestHeader("Translate", "f");
                     try {
                         DavGatewayHttpClientFacade.executeGetMethod(httpClient, method, true);
                         result = getICS(method.getResponseBodyAsStream());
                     } finally {
                         method.releaseConnection();
                     }
                 }
             } catch (DavException e) {
                 throw buildHttpException(e);
             } catch (IOException e) {
                 throw buildHttpException(e);
             } catch (MessagingException e) {
                 throw buildHttpException(e);
             }
             return result;
         }
 
         protected PutMethod internalCreateOrUpdate(String encodedHref, byte[] mimeContent) throws IOException {
             PutMethod putmethod = new PutMethod(encodedHref);
             putmethod.setRequestHeader("Translate", "f");
             putmethod.setRequestHeader("Overwrite", "f");
             if (etag != null) {
                 putmethod.setRequestHeader("If-Match", etag);
             }
             if (noneMatch != null) {
                 putmethod.setRequestHeader("If-None-Match", noneMatch);
             }
             putmethod.setRequestHeader("Content-Type", "message/rfc822");
             putmethod.setRequestEntity(new ByteArrayRequestEntity(mimeContent, "message/rfc822"));
             try {
                 httpClient.executeMethod(putmethod);
             } finally {
                 putmethod.releaseConnection();
             }
             return putmethod;
         }
 
         /**
          * @inheritDoc
          */
         @Override
         public ItemResult createOrUpdate() throws IOException {
             byte[] mimeContent = createMimeContent();
             String encodedHref = URIUtil.encodePath(getHref());
             PutMethod putMethod = internalCreateOrUpdate(encodedHref, mimeContent);
             int status = putMethod.getStatusCode();
 
             if (status == HttpStatus.SC_OK) {
                 LOGGER.debug("Updated event " + encodedHref);
             } else if (status == HttpStatus.SC_CREATED) {
                 LOGGER.debug("Created event " + encodedHref);
             } else if (status == HttpStatus.SC_NOT_FOUND) {
                 LOGGER.debug("Event not found at " + encodedHref + ", searching permanenturl by urlcompname");
                 // failover, search item by urlcompname
                 MultiStatusResponse[] responses = searchItems(folderPath, EVENT_REQUEST_PROPERTIES, DavExchangeSession.this.isEqualTo("urlcompname", convertItemNameToEML(itemName)), FolderQueryTraversal.Shallow, 1);
                 if (responses.length == 1) {
                     encodedHref = getPropertyIfExists(responses[0].getProperties(HttpStatus.SC_OK), "permanenturl");
                     LOGGER.warn("Event found, permanenturl is " + encodedHref);
                     putMethod = internalCreateOrUpdate(encodedHref, mimeContent);
                     status = putMethod.getStatusCode();
                     if (status == HttpStatus.SC_OK) {
                         LOGGER.debug("Updated event " + encodedHref);
                     } else {
                         LOGGER.warn("Unable to create or update event " + status + ' ' + putMethod.getStatusLine());
                     }
                 }
             } else {
                 LOGGER.warn("Unable to create or update event " + status + ' ' + putMethod.getStatusLine());
             }
 
             ItemResult itemResult = new ItemResult();
             // 440 means forbidden on Exchange
             if (status == 440) {
                 status = HttpStatus.SC_FORBIDDEN;
             }
             itemResult.status = status;
             if (putMethod.getResponseHeader("GetETag") != null) {
                 itemResult.etag = putMethod.getResponseHeader("GetETag").getValue();
             }
 
             // trigger activeSync push event, only if davmail.forceActiveSyncUpdate setting is true
             if ((status == HttpStatus.SC_OK || status == HttpStatus.SC_CREATED) &&
                     (Settings.getBooleanProperty("davmail.forceActiveSyncUpdate"))) {
                 ArrayList<DavConstants> propertyList = new ArrayList<DavConstants>();
                 // Set contentclass to make ActiveSync happy
                 propertyList.add(Field.createDavProperty("contentclass", contentClass));
                 // ... but also set PR_INTERNET_CONTENT to preserve custom properties
                 propertyList.add(Field.createDavProperty("internetContent", new String(Base64.encodeBase64(mimeContent))));
                 PropPatchMethod propPatchMethod = new PropPatchMethod(encodedHref, propertyList);
                 int patchStatus = DavGatewayHttpClientFacade.executeHttpMethod(httpClient, propPatchMethod);
                 if (patchStatus != HttpStatus.SC_MULTI_STATUS) {
                     LOGGER.warn("Unable to patch event to trigger activeSync push");
                 } else {
                     // need to retrieve new etag
                     Item newItem = getItem(folderPath, itemName);
                     itemResult.etag = newItem.etag;
                 }
             }
             return itemResult;
         }
 
 
     }
 
     protected Folder buildFolder(MultiStatusResponse entity) throws IOException {
         String href = URIUtil.decode(entity.getHref());
         Folder folder = new Folder();
         DavPropertySet properties = entity.getProperties(HttpStatus.SC_OK);
         folder.displayName = getPropertyIfExists(properties, "displayname");
         folder.folderClass = getPropertyIfExists(properties, "folderclass");
         folder.hasChildren = "1".equals(getPropertyIfExists(properties, "hassubs"));
         folder.noInferiors = "1".equals(getPropertyIfExists(properties, "nosubs"));
         folder.unreadCount = getIntPropertyIfExists(properties, "unreadcount");
         folder.ctag = getPropertyIfExists(properties, "contenttag");
         folder.etag = getPropertyIfExists(properties, "lastmodified");
 
         folder.uidNext = getIntPropertyIfExists(properties, "uidNext");
 
         // replace well known folder names
        if (href.startsWith(inboxUrl)) {
             folder.folderPath = href.replaceFirst(inboxUrl, INBOX);
        } else if (href.startsWith(sentitemsUrl)) {
             folder.folderPath = href.replaceFirst(sentitemsUrl, SENT);
        } else if (href.startsWith(draftsUrl)) {
             folder.folderPath = href.replaceFirst(draftsUrl, DRAFTS);
        } else if (href.startsWith(deleteditemsUrl)) {
             folder.folderPath = href.replaceFirst(deleteditemsUrl, TRASH);
        } else if (href.startsWith(calendarUrl)) {
             folder.folderPath = href.replaceFirst(calendarUrl, CALENDAR);
        } else if (href.startsWith(contactsUrl)) {
             folder.folderPath = href.replaceFirst(contactsUrl, CONTACTS);
         } else {
             int index = href.indexOf(mailPath.substring(0, mailPath.length() - 1));
             if (index >= 0) {
                 if (index + mailPath.length() > href.length()) {
                     folder.folderPath = "";
                 } else {
                     folder.folderPath = href.substring(index + mailPath.length());
                 }
             } else {
                 try {
                     URI folderURI = new URI(href, false);
                     folder.folderPath = folderURI.getPath();
                 } catch (URIException e) {
                     throw new DavMailException("EXCEPTION_INVALID_FOLDER_URL", href);
                 }
             }
         }
         if (folder.folderPath.endsWith("/")) {
             folder.folderPath = folder.folderPath.substring(0, folder.folderPath.length() - 1);
         }
         return folder;
     }
 
     protected static final Set<String> FOLDER_PROPERTIES = new HashSet<String>();
 
     static {
         FOLDER_PROPERTIES.add("displayname");
         FOLDER_PROPERTIES.add("folderclass");
         FOLDER_PROPERTIES.add("hassubs");
         FOLDER_PROPERTIES.add("nosubs");
         FOLDER_PROPERTIES.add("unreadcount");
         FOLDER_PROPERTIES.add("contenttag");
         FOLDER_PROPERTIES.add("lastmodified");
         FOLDER_PROPERTIES.add("uidNext");
     }
 
     protected static final DavPropertyNameSet FOLDER_PROPERTIES_NAME_SET = new DavPropertyNameSet();
 
     static {
         for (String attribute : FOLDER_PROPERTIES) {
             FOLDER_PROPERTIES_NAME_SET.add(Field.getPropertyName(attribute));
         }
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public Folder getFolder(String folderPath) throws IOException {
         MultiStatusResponse[] responses = DavGatewayHttpClientFacade.executePropFindMethod(
                 httpClient, URIUtil.encodePath(getFolderPath(folderPath)), 0, FOLDER_PROPERTIES_NAME_SET);
         Folder folder = null;
         if (responses.length > 0) {
             folder = buildFolder(responses[0]);
             folder.folderPath = folderPath;
         }
         return folder;
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public List<Folder> getSubFolders(String folderPath, Condition condition, boolean recursive) throws IOException {
         boolean isPublic = folderPath.startsWith("/public");
         FolderQueryTraversal mode = (!isPublic && recursive) ? FolderQueryTraversal.Deep : FolderQueryTraversal.Shallow;
         List<Folder> folders = new ArrayList<Folder>();
 
         MultiStatusResponse[] responses = searchItems(folderPath, FOLDER_PROPERTIES, and(isTrue("isfolder"), isFalse("ishidden"), condition), mode, 0);
 
         for (MultiStatusResponse response : responses) {
             Folder folder = buildFolder(response);
             folders.add(buildFolder(response));
             if (isPublic && recursive) {
                 getSubFolders(folder.folderPath, condition, recursive);
             }
         }
         return folders;
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public int createFolder(String folderPath, String folderClass, Map<String, String> properties) throws IOException {
         Set<PropertyValue> propertyValues = new HashSet<PropertyValue>();
         if (properties != null) {
             for (Map.Entry<String, String> entry : properties.entrySet()) {
                 propertyValues.add(Field.createPropertyValue(entry.getKey(), entry.getValue()));
             }
         }
         propertyValues.add(Field.createPropertyValue("folderclass", folderClass));
 
         // standard MkColMethod does not take properties, override PropPatchMethod instead
         ExchangePropPatchMethod method = new ExchangePropPatchMethod(URIUtil.encodePath(getFolderPath(folderPath)), propertyValues) {
             @Override
             public String getName() {
                 return "MKCOL";
             }
         };
         int status = DavGatewayHttpClientFacade.executeHttpMethod(httpClient, method);
         if (status == HttpStatus.SC_MULTI_STATUS) {
             status = method.getResponseStatusCode();
         }
         return status;
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void deleteFolder(String folderPath) throws IOException {
         DavGatewayHttpClientFacade.executeDeleteMethod(httpClient, URIUtil.encodePath(getFolderPath(folderPath)));
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void moveFolder(String folderPath, String targetPath) throws IOException {
         MoveMethod method = new MoveMethod(URIUtil.encodePath(getFolderPath(folderPath)),
                 URIUtil.encodePath(getFolderPath(targetPath)), false);
         try {
             int statusCode = httpClient.executeMethod(method);
             if (statusCode == HttpStatus.SC_PRECONDITION_FAILED) {
                 throw new DavMailException("EXCEPTION_UNABLE_TO_MOVE_FOLDER");
             } else if (statusCode != HttpStatus.SC_CREATED) {
                 throw DavGatewayHttpClientFacade.buildHttpException(method);
             }
         } finally {
             method.releaseConnection();
         }
     }
 
     protected String getPropertyIfExists(DavPropertySet properties, String alias) {
         DavProperty property = properties.get(Field.getResponsePropertyName(alias));
         if (property == null) {
             return null;
         } else {
             Object value = property.getValue();
             if (value instanceof Node) {
                 return ((Node) value).getTextContent();
             } else if (value instanceof List) {
                 StringBuilder buffer = new StringBuilder();
                 for (Object node : (List) value) {
                     if (buffer.length() > 0) {
                         buffer.append(',');
                     }
                     buffer.append(((Node) node).getTextContent());
                 }
                 return buffer.toString();
             } else {
                 return (String) value;
             }
         }
     }
 
     protected int getIntPropertyIfExists(DavPropertySet properties, String alias) {
         DavProperty property = properties.get(Field.getPropertyName(alias));
         if (property == null) {
             return 0;
         } else {
             return Integer.parseInt((String) property.getValue());
         }
     }
 
     protected long getLongPropertyIfExists(DavPropertySet properties, String alias) {
         DavProperty property = properties.get(Field.getPropertyName(alias));
         if (property == null) {
             return 0;
         } else {
             return Long.parseLong((String) property.getValue());
         }
     }
 
     protected byte[] getBinaryPropertyIfExists(DavPropertySet properties, String alias) {
         byte[] property = null;
         String base64Property = getPropertyIfExists(properties, alias);
         if (base64Property != null) {
             try {
                 property = Base64.decodeBase64(base64Property.getBytes("ASCII"));
             } catch (UnsupportedEncodingException e) {
                 LOGGER.warn(e);
             }
         }
         return property;
     }
 
 
     protected Message buildMessage(MultiStatusResponse responseEntity) throws URIException, DavMailException {
         Message message = new Message();
         message.messageUrl = URIUtil.decode(responseEntity.getHref());
         DavPropertySet properties = responseEntity.getProperties(HttpStatus.SC_OK);
 
         message.permanentUrl = getPropertyIfExists(properties, "permanenturl");
         message.size = getIntPropertyIfExists(properties, "messageSize");
         message.uid = getPropertyIfExists(properties, "uid");
         message.imapUid = getLongPropertyIfExists(properties, "imapUid");
         message.read = "1".equals(getPropertyIfExists(properties, "read"));
         message.junk = "1".equals(getPropertyIfExists(properties, "junk"));
         message.flagged = "2".equals(getPropertyIfExists(properties, "flagStatus"));
         message.draft = (getIntPropertyIfExists(properties, "messageFlags") & 8) != 0;
         String lastVerbExecuted = getPropertyIfExists(properties, "lastVerbExecuted");
         message.answered = "102".equals(lastVerbExecuted) || "103".equals(lastVerbExecuted);
         message.forwarded = "104".equals(lastVerbExecuted);
         message.date = convertDateFromExchange(getPropertyIfExists(properties, "date"));
         message.deleted = "1".equals(getPropertyIfExists(properties, "deleted"));
 
         if (LOGGER.isDebugEnabled()) {
             StringBuilder buffer = new StringBuilder();
             buffer.append("Message");
             if (message.imapUid != 0) {
                 buffer.append(" IMAP uid: ").append(message.imapUid);
             }
             if (message.uid != null) {
                 buffer.append(" uid: ").append(message.uid);
             }
             buffer.append(" href: ").append(responseEntity.getHref()).append(" permanenturl:").append(message.permanentUrl);
             LOGGER.debug(buffer.toString());
         }
         return message;
     }
 
     @Override
     public MessageList searchMessages(String folderPath, Set<String> attributes, Condition condition) throws IOException {
         MessageList messages = new MessageList();
         MultiStatusResponse[] responses = searchItems(folderPath, attributes, and(isFalse("isfolder"), isFalse("ishidden"), condition), FolderQueryTraversal.Shallow, 0);
 
         for (MultiStatusResponse response : responses) {
             Message message = buildMessage(response);
             message.messageList = messages;
             messages.add(message);
         }
         Collections.sort(messages);
         return messages;
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public List<ExchangeSession.Contact> searchContacts(String folderPath, Set<String> attributes, Condition condition, int maxCount) throws IOException {
         List<ExchangeSession.Contact> contacts = new ArrayList<ExchangeSession.Contact>();
         MultiStatusResponse[] responses = searchItems(folderPath, attributes,
                 and(isEqualTo("outlookmessageclass", "IPM.Contact"), isFalse("isfolder"), isFalse("ishidden"), condition),
                 FolderQueryTraversal.Shallow, maxCount);
         for (MultiStatusResponse response : responses) {
             contacts.add(new Contact(response));
         }
         return contacts;
     }
 
     @Override
     public List<ExchangeSession.Event> searchEvents(String folderPath, Set<String> attributes, Condition condition) throws IOException {
         List<ExchangeSession.Event> events = new ArrayList<ExchangeSession.Event>();
         MultiStatusResponse[] responses = searchItems(folderPath, attributes, and(isFalse("isfolder"), isFalse("ishidden"), condition), FolderQueryTraversal.Shallow, 0);
         for (MultiStatusResponse response : responses) {
             String instancetype = getPropertyIfExists(response.getProperties(HttpStatus.SC_OK), "instancetype");
             Event event = new Event(response);
             //noinspection VariableNotUsedInsideIf
             if (instancetype == null) {
                 // check ics content
                 try {
                     event.getBody();
                     // getBody success => add event or task
                     events.add(event);
                 } catch (IOException e) {
                     // invalid event: exclude from list
                     LOGGER.warn("Invalid event " + event.displayName + " found at " + response.getHref(), e);
                 }
             } else {
                 events.add(event);
             }
         }
         return events;
     }
 
     protected MultiStatusResponse[] searchItems(String folderPath, Set<String> attributes, Condition condition,
                                                 FolderQueryTraversal folderQueryTraversal, int maxCount) throws IOException {
         String folderUrl = getFolderPath(folderPath);
         StringBuilder searchRequest = new StringBuilder();
         searchRequest.append("SELECT ")
                 .append(Field.getRequestPropertyString("permanenturl"));
         if (attributes != null) {
             for (String attribute : attributes) {
                 searchRequest.append(',').append(Field.getRequestPropertyString(attribute));
             }
         }
         searchRequest.append(" FROM SCOPE('").append(folderQueryTraversal).append(" TRAVERSAL OF \"").append(folderUrl).append("\"')");
         if (condition != null) {
             searchRequest.append(" WHERE ");
             condition.appendTo(searchRequest);
         }
         DavGatewayTray.debug(new BundleMessage("LOG_SEARCH_QUERY", searchRequest));
         return DavGatewayHttpClientFacade.executeSearchMethod(
                 httpClient, URIUtil.encodePath(folderUrl), searchRequest.toString(), maxCount);
     }
 
     protected static final Set<String> EVENT_REQUEST_PROPERTIES = new HashSet<String>();
 
     static {
         EVENT_REQUEST_PROPERTIES.add("permanenturl");
         EVENT_REQUEST_PROPERTIES.add("urlcompname");
         EVENT_REQUEST_PROPERTIES.add("etag");
         EVENT_REQUEST_PROPERTIES.add("contentclass");
         EVENT_REQUEST_PROPERTIES.add("displayname");
     }
 
     protected static final DavPropertyNameSet EVENT_REQUEST_PROPERTIES_NAME_SET = new DavPropertyNameSet();
 
     static {
         for (String attribute : EVENT_REQUEST_PROPERTIES) {
             EVENT_REQUEST_PROPERTIES_NAME_SET.add(Field.getPropertyName(attribute));
         }
 
     }
 
     @Override
     public Item getItem(String folderPath, String itemName) throws IOException {
         String emlItemName = convertItemNameToEML(itemName);
         String itemPath = getFolderPath(folderPath) + '/' + emlItemName;
         MultiStatusResponse[] responses;
         try {
             responses = DavGatewayHttpClientFacade.executePropFindMethod(httpClient, URIUtil.encodePath(itemPath), 0, EVENT_REQUEST_PROPERTIES_NAME_SET);
             if (responses.length == 0) {
                 throw new HttpNotFoundException(itemPath + " not found");
             }
         } catch (HttpNotFoundException e) {
             LOGGER.debug(itemPath + " not found, searching by urlcompname");
             // failover: try to get event by displayname
             responses = searchItems(folderPath, EVENT_REQUEST_PROPERTIES, isEqualTo("urlcompname", emlItemName), FolderQueryTraversal.Shallow, 1);
             if (responses.length == 0) {
                 throw new HttpNotFoundException(itemPath + " not found");
             }
         }
         // build item
         String contentClass = getPropertyIfExists(responses[0].getProperties(HttpStatus.SC_OK), "contentclass");
         String urlcompname = getPropertyIfExists(responses[0].getProperties(HttpStatus.SC_OK), "urlcompname");
         if ("urn:content-classes:person".equals(contentClass)) {
             // retrieve Contact properties
             List<ExchangeSession.Contact> contacts = searchContacts(folderPath, CONTACT_ATTRIBUTES, isEqualTo("urlcompname", urlcompname), 1);
             if (contacts.isEmpty()) {
                 LOGGER.warn("Item found, but unable to build contact");
                 throw new HttpNotFoundException(itemPath + " not found");
             }
             return contacts.get(0);
         } else if ("urn:content-classes:appointment".equals(contentClass)
                 || "urn:content-classes:calendarmessage".equals(contentClass)) {
             return new Event(responses[0]);
         } else {
             LOGGER.warn("wrong contentclass on item " + itemPath + ": " + contentClass);
             // return item anyway
             return new Event(responses[0]);
         }
 
     }
 
     @Override
     public ExchangeSession.ContactPhoto getContactPhoto(ExchangeSession.Contact contact) throws IOException {
         ContactPhoto contactPhoto = null;
         if ("true".equals(contact.get("haspicture"))) {
             final GetMethod method = new GetMethod(URIUtil.encodePath(contact.getHref()) + "/ContactPicture.jpg");
             method.setRequestHeader("Translate", "f");
             method.setRequestHeader("Accept-Encoding", "gzip");
 
             InputStream inputStream = null;
             try {
                 DavGatewayHttpClientFacade.executeGetMethod(httpClient, method, true);
                 if (isGzipEncoded(method)) {
                     inputStream = (new GZIPInputStream(method.getResponseBodyAsStream()));
                 } else {
                     inputStream = method.getResponseBodyAsStream();
                 }
 
                 contactPhoto = new ContactPhoto();
                 contactPhoto.contentType = "image/jpeg";
 
                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 InputStream partInputStream = inputStream;
                 byte[] bytes = new byte[8192];
                 int length;
                 while ((length = partInputStream.read(bytes)) > 0) {
                     baos.write(bytes, 0, length);
                 }
                 contactPhoto.content = new String(Base64.encodeBase64(baos.toByteArray()));
             } finally {
                 if (inputStream != null) {
                     try {
                         inputStream.close();
                     } catch (IOException e) {
                         LOGGER.debug(e);
                     }
                 }
                 method.releaseConnection();
             }
         }
         return contactPhoto;
     }
 
     @Override
     public int sendEvent(String icsBody) throws IOException {
         String itemName = UUID.randomUUID().toString() + ".EML";
         byte[] mimeContent = (new Event(getFolderPath(DRAFTS), itemName, "urn:content-classes:calendarmessage", icsBody, null, null)).createMimeContent();
         if (mimeContent == null) {
             // no recipients, cancel
             return HttpStatus.SC_NO_CONTENT;
         } else {
             sendMessage(mimeContent);
             return HttpStatus.SC_OK;
         }
     }
 
     @Override
     public void deleteItem(String folderPath, String itemName) throws IOException {
         String eventPath = URIUtil.encodePath(getFolderPath(folderPath) + '/' + convertItemNameToEML(itemName));
         DavGatewayHttpClientFacade.executeDeleteMethod(httpClient, eventPath);
     }
 
     @Override
     public void processItem(String folderPath, String itemName) throws IOException {
         String eventPath = URIUtil.encodePath(getFolderPath(folderPath) + '/' + convertItemNameToEML(itemName));
         // do not delete calendar messages, mark read and processed
         ArrayList<DavConstants> list = new ArrayList<DavConstants>();
         list.add(Field.createDavProperty("processed", "true"));
         list.add(Field.createDavProperty("read", "1"));
         PropPatchMethod patchMethod = new PropPatchMethod(eventPath, list);
         DavGatewayHttpClientFacade.executeMethod(httpClient, patchMethod);
     }
 
     @Override
     public ItemResult internalCreateOrUpdateEvent(String folderPath, String itemName, String contentClass, String icsBody, String etag, String noneMatch) throws IOException {
         return new Event(getFolderPath(folderPath), itemName, contentClass, icsBody, etag, noneMatch).createOrUpdate();
     }
 
     /**
      * create a fake event to get VTIMEZONE body
      */
     @Override
     protected void loadVtimezone() {
         try {
             // create temporary folder
             String folderPath = getFolderPath("davmailtemp");
             createCalendarFolder(folderPath, null);
 
             PostMethod postMethod = new PostMethod(URIUtil.encodePath(folderPath));
             postMethod.addParameter("Cmd", "saveappt");
             postMethod.addParameter("FORMTYPE", "appointment");
             String fakeEventUrl = null;
             try {
                 // create fake event
                 int statusCode = httpClient.executeMethod(postMethod);
                 if (statusCode == HttpStatus.SC_OK) {
                     fakeEventUrl = StringUtil.getToken(postMethod.getResponseBodyAsString(), "<span id=\"itemHREF\">", "</span>");
                 }
             } finally {
                 postMethod.releaseConnection();
             }
             // failover for Exchange 2007, use PROPPATCH with forced timezone
             if (fakeEventUrl == null) {
                 ArrayList<DavConstants> propertyList = new ArrayList<DavConstants>();
                 propertyList.add(Field.createDavProperty("contentclass", "urn:content-classes:appointment"));
                 propertyList.add(Field.createDavProperty("outlookmessageclass", "IPM.Appointment"));
                 propertyList.add(Field.createDavProperty("instancetype", "0"));
 
                 // get forced timezone id from settings
                 String timezoneId = Settings.getProperty("davmail.timezoneId");
                 if (timezoneId == null) {
                     // get timezoneid from OWA settings
                     timezoneId = getTimezoneIdFromExchange();
                 }
                 // without a timezoneId, use Exchange timezone
                 if (timezoneId != null) {
                     propertyList.add(Field.createDavProperty("timezoneid", timezoneId));
                 }
                 String patchMethodUrl = URIUtil.encodePath(folderPath) + '/' + UUID.randomUUID().toString() + ".EML";
                 PropPatchMethod patchMethod = new PropPatchMethod(URIUtil.encodePath(patchMethodUrl), propertyList);
                 try {
                     int statusCode = httpClient.executeMethod(patchMethod);
                     if (statusCode == HttpStatus.SC_MULTI_STATUS) {
                         fakeEventUrl = patchMethodUrl;
                     }
                 } finally {
                     patchMethod.releaseConnection();
                 }
             }
             if (fakeEventUrl != null) {
                 // get fake event body
                 GetMethod getMethod = new GetMethod(URIUtil.encodePath(fakeEventUrl));
                 getMethod.setRequestHeader("Translate", "f");
                 try {
                     httpClient.executeMethod(getMethod);
                     this.vTimezone = new VObject("BEGIN:VTIMEZONE" +
                             StringUtil.getToken(getMethod.getResponseBodyAsString(), "BEGIN:VTIMEZONE", "END:VTIMEZONE") +
                             "END:VTIMEZONE\r\n");
                 } finally {
                     getMethod.releaseConnection();
                 }
             }
 
             // delete temporary folder
             deleteFolder("davmailtemp");
         } catch (IOException e) {
             LOGGER.warn("Unable to get VTIMEZONE info: " + e, e);
         }
     }
 
     protected String getTimezoneIdFromExchange() {
         String timezoneId = null;
 
         try {
             Set<String> attributes = new HashSet<String>();
             attributes.add("roamingdictionary");
 
             MultiStatusResponse[] responses = searchItems("/users/" + getEmail() + "/NON_IPM_SUBTREE", attributes, isEqualTo("messageclass", "IPM.Configuration.OWA.UserOptions"), DavExchangeSession.FolderQueryTraversal.Deep, 1);
             if (responses.length == 1) {
                 byte[] roamingdictionary = getBinaryPropertyIfExists(responses[0].getProperties(HttpStatus.SC_OK), "roamingdictionary");
                 if (roamingdictionary != null) {
                     String roamingdictionaryString = new String(roamingdictionary, "UTF-8");
                     int startIndex = roamingdictionaryString.lastIndexOf("18-");
                     if (startIndex >= 0) {
                         int endIndex = roamingdictionaryString.indexOf('"', startIndex);
                         if (endIndex >= 0) {
                             String timezoneName = roamingdictionaryString.substring(startIndex + 3, endIndex);
                             try {
                                 timezoneId = ResourceBundle.getBundle("timezoneids").getString(timezoneName);
                             } catch (MissingResourceException mre) {
                                 LOGGER.warn("Invalid timezone name: " + timezoneName);
                             }
                         }
                     }
                 }
             }
         } catch (UnsupportedEncodingException e) {
             LOGGER.warn("Unable to retrieve Exchange timezone id: " + e.getMessage(), e);
         } catch (IOException e) {
             LOGGER.warn("Unable to retrieve Exchange timezone id: " + e.getMessage(), e);
         }
         return timezoneId;
     }
 
     @Override
     protected ItemResult internalCreateOrUpdateContact(String folderPath, String itemName, Map<String, String> properties, String etag, String noneMatch) throws IOException {
         return new Contact(getFolderPath(folderPath), itemName, properties, etag, noneMatch).createOrUpdate();
     }
 
     protected List<DavConstants> buildProperties(Map<String, String> properties) {
         ArrayList<DavConstants> list = new ArrayList<DavConstants>();
         if (properties != null) {
             for (Map.Entry<String, String> entry : properties.entrySet()) {
                 if ("read".equals(entry.getKey())) {
                     list.add(Field.createDavProperty("read", entry.getValue()));
                 } else if ("junk".equals(entry.getKey())) {
                     list.add(Field.createDavProperty("junk", entry.getValue()));
                 } else if ("flagged".equals(entry.getKey())) {
                     list.add(Field.createDavProperty("flagStatus", entry.getValue()));
                 } else if ("answered".equals(entry.getKey())) {
                     list.add(Field.createDavProperty("lastVerbExecuted", entry.getValue()));
                     if ("102".equals(entry.getValue())) {
                         list.add(Field.createDavProperty("iconIndex", "261"));
                     }
                 } else if ("forwarded".equals(entry.getKey())) {
                     list.add(Field.createDavProperty("lastVerbExecuted", entry.getValue()));
                     if ("104".equals(entry.getValue())) {
                         list.add(Field.createDavProperty("iconIndex", "262"));
                     }
                 } else if ("bcc".equals(entry.getKey())) {
                     list.add(Field.createDavProperty("bcc", entry.getValue()));
                 } else if ("deleted".equals(entry.getKey())) {
                     list.add(Field.createDavProperty("deleted", entry.getValue()));
                 } else if ("datereceived".equals(entry.getKey())) {
                     list.add(Field.createDavProperty("datereceived", entry.getValue()));
                 }
             }
         }
         return list;
     }
 
     /**
      * Create message in specified folder.
      * Will overwrite an existing message with same messageName in the same folder
      *
      * @param folderPath  Exchange folder path
      * @param messageName message name
      * @param properties  message properties (flags)
      * @param messageBody mail body
      * @throws IOException when unable to create message
      */
     @Override
     public void createMessage(String folderPath, String messageName, HashMap<String, String> properties, byte[] messageBody) throws IOException {
         String messageUrl = URIUtil.encodePathQuery(getFolderPath(folderPath) + '/' + messageName);
         PropPatchMethod patchMethod;
         List<DavConstants> davProperties = buildProperties(properties);
 
         if (properties != null && properties.containsKey("draft")) {
             // note: draft is readonly after create, create the message first with requested messageFlags
             davProperties.add(Field.createDavProperty("messageFlags", properties.get("draft")));
         }
         if (!davProperties.isEmpty()) {
             patchMethod = new PropPatchMethod(messageUrl, davProperties);
             try {
                 // update message with blind carbon copy and other flags
                 int statusCode = httpClient.executeMethod(patchMethod);
                 if (statusCode != HttpStatus.SC_MULTI_STATUS) {
                     throw new DavMailException("EXCEPTION_UNABLE_TO_CREATE_MESSAGE", messageUrl, statusCode, ' ', patchMethod.getStatusLine());
                 }
 
             } finally {
                 patchMethod.releaseConnection();
             }
         }
 
         // update message body
         PutMethod putmethod = new PutMethod(messageUrl);
         putmethod.setRequestHeader("Translate", "f");
         putmethod.setRequestHeader("Content-Type", "message/rfc822");
 
         try {
             // use same encoding as client socket reader
             putmethod.setRequestEntity(new ByteArrayRequestEntity(messageBody));
             int code = httpClient.executeMethod(putmethod);
 
             if (code != HttpStatus.SC_OK && code != HttpStatus.SC_CREATED) {
                 throw new DavMailException("EXCEPTION_UNABLE_TO_CREATE_MESSAGE", messageUrl, code, ' ', putmethod.getStatusLine());
             }
         } finally {
             putmethod.releaseConnection();
         }
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void updateMessage(Message message, Map<String, String> properties) throws IOException {
         PropPatchMethod patchMethod = new PropPatchMethod(message.permanentUrl, buildProperties(properties)) {
             @Override
             protected void processResponseBody(HttpState httpState, HttpConnection httpConnection) {
                 // ignore response body, sometimes invalid with exchange mapi properties
             }
         };
         try {
             int statusCode = httpClient.executeMethod(patchMethod);
             if (statusCode != HttpStatus.SC_MULTI_STATUS) {
                 throw new DavMailException("EXCEPTION_UNABLE_TO_UPDATE_MESSAGE");
             }
 
         } finally {
             patchMethod.releaseConnection();
         }
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void deleteMessage(Message message) throws IOException {
         LOGGER.debug("Delete " + message.permanentUrl + " (" + message.messageUrl + ')');
         DavGatewayHttpClientFacade.executeDeleteMethod(httpClient, message.permanentUrl);
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void sendMessage(byte[] messageBody) throws IOException {
         String messageName = UUID.randomUUID().toString() + ".EML";
 
         createMessage(sendmsgUrl, messageName, null, messageBody);
     }
 
     protected boolean isGzipEncoded(HttpMethod method) {
         Header[] contentEncodingHeaders = method.getResponseHeaders("Content-Encoding");
         if (contentEncodingHeaders != null) {
             for (Header header : contentEncodingHeaders) {
                 if ("gzip".equals(header.getValue())) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     /**
      * @inheritDoc
      */
     @Override
     protected byte[] getContent(Message message) throws IOException {
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         InputStream contentInputStream;
         try {
             try {
                 contentInputStream = getContentInputStream(message.messageUrl);
             } catch (HttpNotFoundException e) {
                 LOGGER.debug("Message not found at: " + message.messageUrl + ", retrying with permanenturl");
                 contentInputStream = getContentInputStream(message.permanentUrl);
             }
         } catch (HttpException e) {
             // other exception
             if (Settings.getBooleanProperty("davmail.deleteBroken")) {
                 LOGGER.warn("Deleting broken message at: " + message.messageUrl + " permanentUrl: " + message.permanentUrl);
                 try {
                     message.delete();
                 } catch (IOException ioe) {
                     LOGGER.warn("Unable to delete broken message at: " + message.permanentUrl);
                 }
             }
             throw e;
         }
 
         try {
             IOUtil.write(contentInputStream, baos);
         } finally {
             contentInputStream.close();
         }
         return baos.toByteArray();
     }
 
     protected InputStream getContentInputStream(String url) throws IOException {
         final GetMethod method = new GetMethod(URIUtil.encodePath(url));
         method.setRequestHeader("Content-Type", "text/xml; charset=utf-8");
         method.setRequestHeader("Translate", "f");
         method.setRequestHeader("Accept-Encoding", "gzip");
 
         InputStream inputStream;
         try {
             DavGatewayHttpClientFacade.executeGetMethod(httpClient, method, true);
             if (isGzipEncoded(method)) {
                 inputStream = new GZIPInputStream(method.getResponseBodyAsStream());
             } else {
                 inputStream = method.getResponseBodyAsStream();
             }
             inputStream = new FilterInputStream(inputStream) {
 
                 @Override
                 public void close() throws IOException {
                     try {
                         super.close();
                     } finally {
                         method.releaseConnection();
                     }
                 }
             };
 
         } catch (HttpException e) {
             method.releaseConnection();
             LOGGER.warn("Unable to retrieve message at: " + url);
             throw e;
         }
         return inputStream;
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void copyMessage(Message message, String targetFolder) throws IOException {
         String targetPath = URIUtil.encodePath(getFolderPath(targetFolder)) + '/' + UUID.randomUUID().toString();
         CopyMethod method = new CopyMethod(message.permanentUrl, targetPath, false);
         // allow rename if a message with the same name exists
         method.addRequestHeader("Allow-Rename", "t");
         try {
             int statusCode = httpClient.executeMethod(method);
             if (statusCode == HttpStatus.SC_PRECONDITION_FAILED) {
                 throw new DavMailException("EXCEPTION_UNABLE_TO_COPY_MESSAGE");
             } else if (statusCode != HttpStatus.SC_CREATED) {
                 throw DavGatewayHttpClientFacade.buildHttpException(method);
             }
         } finally {
             method.releaseConnection();
         }
     }
 
     @Override
     protected void moveToTrash(Message message) throws IOException {
         String destination = URIUtil.encodePath(deleteditemsUrl) + '/' + UUID.randomUUID().toString();
         LOGGER.debug("Deleting : " + message.permanentUrl + " to " + destination);
         MoveMethod method = new MoveMethod(message.permanentUrl, destination, false);
         method.addRequestHeader("Allow-rename", "t");
 
         int status = DavGatewayHttpClientFacade.executeHttpMethod(httpClient, method);
         // do not throw error if already deleted
         if (status != HttpStatus.SC_CREATED && status != HttpStatus.SC_NOT_FOUND) {
             throw DavGatewayHttpClientFacade.buildHttpException(method);
         }
         if (method.getResponseHeader("Location") != null) {
             destination = method.getResponseHeader("Location").getValue();
         }
 
         LOGGER.debug("Deleted to :" + destination);
     }
 
     protected String convertDateFromExchange(String exchangeDateValue) throws DavMailException {
         String zuluDateValue = null;
         if (exchangeDateValue != null) {
             try {
                 zuluDateValue = getZuluDateFormat().format(getExchangeZuluDateFormatMillisecond().parse(exchangeDateValue));
             } catch (ParseException e) {
                 throw new DavMailException("EXCEPTION_INVALID_DATE", exchangeDateValue);
             }
         }
         return zuluDateValue;
     }
 
     /**
      * Format date to exchange search format.
      *
      * @param date date object
      * @return formatted search date
      */
     @Override
     public String formatSearchDate(Date date) {
         SimpleDateFormat dateFormatter = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS, Locale.ENGLISH);
         dateFormatter.setTimeZone(GMT_TIMEZONE);
         return dateFormatter.format(date);
     }
 
 }
