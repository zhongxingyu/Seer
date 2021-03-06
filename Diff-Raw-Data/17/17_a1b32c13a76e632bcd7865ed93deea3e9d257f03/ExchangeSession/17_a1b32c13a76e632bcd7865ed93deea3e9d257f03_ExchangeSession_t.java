 package davmail.exchange;
 
 import davmail.Settings;
 import davmail.http.DavGatewayHttpClientFacade;
 import org.apache.commons.httpclient.*;
 import org.apache.commons.httpclient.auth.AuthenticationException;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.PutMethod;
 import org.apache.commons.httpclient.util.URIUtil;
 import org.apache.log4j.Logger;
 import org.apache.webdav.lib.Property;
 import org.apache.webdav.lib.ResponseEntity;
 import org.apache.webdav.lib.WebdavResource;
 import org.apache.webdav.lib.methods.PropPatchMethod;
 import org.apache.webdav.lib.methods.SearchMethod;
 import org.htmlcleaner.CommentToken;
 import org.htmlcleaner.HtmlCleaner;
 import org.htmlcleaner.TagNode;
 
 import java.io.*;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * Exchange session through Outlook Web Access (DAV)
  */
 public class ExchangeSession {
     protected static final Logger LOGGER = Logger.getLogger("davmail.exchange.ExchangeSession");
 
     /**
      * exchange message properties needed to rebuild mime message
      */
     protected static final Vector<String> MESSAGE_REQUEST_PROPERTIES = new Vector<String>();
 
     static {
         MESSAGE_REQUEST_PROPERTIES.add("DAV:uid");
         // size
         MESSAGE_REQUEST_PROPERTIES.add("http://schemas.microsoft.com/mapi/proptag/x0e080003");
     }
 
     protected static final Vector<String> EVENT_REQUEST_PROPERTIES = new Vector<String>();
 
     static {
         EVENT_REQUEST_PROPERTIES.add("DAV:getetag");
     }
 
     protected static final Vector<String> WELL_KNOWN_FOLDERS = new Vector<String>();
 
     static {
         WELL_KNOWN_FOLDERS.add("urn:schemas:httpmail:inbox");
         WELL_KNOWN_FOLDERS.add("urn:schemas:httpmail:deleteditems");
         WELL_KNOWN_FOLDERS.add("urn:schemas:httpmail:sentitems");
         WELL_KNOWN_FOLDERS.add("urn:schemas:httpmail:sendmsg");
         WELL_KNOWN_FOLDERS.add("urn:schemas:httpmail:drafts");
         WELL_KNOWN_FOLDERS.add("urn:schemas:httpmail:calendar");
     }
 
     /**
      * Date parser from Exchange format
      */
     private final SimpleDateFormat dateFormatter;
 
     /**
      * Various standard mail boxes Urls
      */
     private String inboxUrl;
     private String deleteditemsUrl;
     private String sentitemsUrl;
     private String sendmsgUrl;
     private String draftsUrl;
     private String calendarUrl;
 
     /**
      * Base user mailboxes path (used to select folder)
      */
     private String mailPath;
     private String currentFolderUrl;
     private WebdavResource wdr = null;
 
     private final ExchangeSessionFactory.PoolKey poolKey;
 
     private boolean disableGalLookup = false;
 
     ExchangeSessionFactory.PoolKey getPoolKey() {
         return poolKey;
     }
 
     /**
      * Create an exchange session for the given URL.
      * The session is not actually established until a call to login()
      *
      * @param poolKey session pool key
      */
     ExchangeSession(ExchangeSessionFactory.PoolKey poolKey) {
         this.poolKey = poolKey;
         // SimpleDateFormat are not thread safe, need to create one instance for
         // each session
         dateFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
         LOGGER.debug("Session " + this + " created");
     }
 
     public boolean isExpired() {
         boolean isExpired = false;
         try {
            wdr.propfindMethod(0);
            int status = wdr.getStatusCode();
 
            if (status != HttpStatus.SC_MULTI_STATUS) {
                 isExpired = true;
             }
 
         } catch (IOException e) {
             isExpired = true;
         }
 
         return isExpired;
     }
 
     /**
      * Test authentication mode : form based or basic.
      *
      * @param url exchange base URL
      * @return true if basic authentication detected
      * @throws java.io.IOException unable to connect to exchange
      */
     protected boolean isBasicAuthentication(String url) throws IOException {
         return DavGatewayHttpClientFacade.getHttpStatus(url) == HttpStatus.SC_UNAUTHORIZED;
     }
 
     protected String getAbsolutePath(HttpMethod method, String path) {
         String absolutePath = path;
         // allow relative path
         if (!absolutePath.startsWith("/")) {
             String currentPath = method.getPath();
             int end = currentPath.lastIndexOf('/');
             if (end >= 0) {
                 absolutePath = currentPath.substring(0, end + 1) + absolutePath;
             }
         }
         return absolutePath;
     }
 
     /**
      * Try to find logon method path from logon form body.
      *
      * @param httpClient httpClient instance
      * @param initmethod form body http method
      * @return logon method
      * @throws java.io.IOException on error
      */
     protected PostMethod buildLogonMethod(HttpClient httpClient, HttpMethod initmethod) throws IOException {
 
         PostMethod logonMethod = null;
 
         // create an instance of HtmlCleaner
         HtmlCleaner cleaner = new HtmlCleaner();
 
         try {
             TagNode node = cleaner.clean(initmethod.getResponseBodyAsStream());
             List forms = node.getElementListByName("form", true);
             if (forms.size() == 1) {
                 TagNode form = (TagNode) forms.get(0);
                 String logonMethodPath = form.getAttributeByName("action");
 
                 logonMethod = new PostMethod(getAbsolutePath(initmethod, logonMethodPath));
 
                 List inputList = form.getElementListByName("input", true);
                 for (Object input : inputList) {
                     String type = ((TagNode) input).getAttributeByName("type");
                     String name = ((TagNode) input).getAttributeByName("name");
                     String value = ((TagNode) input).getAttributeByName("value");
                     if ("hidden".equalsIgnoreCase(type) && name != null && value != null) {
                         logonMethod.addParameter(name, value);
                     }
                 }
             } else {
                 List frameList = node.getElementListByName("frame", true);
                 if (frameList.size() == 1) {
                     String src = ((TagNode) frameList.get(0)).getAttributeByName("src");
                     if (src != null) {
                         LOGGER.debug("Frames detected in form page, try frame content");
                         initmethod.releaseConnection();
                         HttpMethod newInitMethod = DavGatewayHttpClientFacade.executeFollowRedirects(httpClient, src);
                         logonMethod = buildLogonMethod(httpClient, newInitMethod);
                     }
                 } else {
                     // another failover for script based logon forms (Exchange 2007)
                     List scriptList = node.getElementListByName("script", true);
                     for (Object script : scriptList) {
                         List contents = ((TagNode) script).getChildren();
                         for (Object content : contents) {
                             if (content instanceof CommentToken) {
                                 String scriptValue = ((CommentToken) content).getCommentedContent();
                                 int a_sUrlIndex = scriptValue.indexOf("var a_sUrl = \"");
                                 int a_sLgnIndex = scriptValue.indexOf("var a_sLgn = \"");
                                 if (a_sUrlIndex >= 0 && a_sLgnIndex >= 0) {
                                     a_sUrlIndex += "var a_sUrl = \"".length();
                                     a_sLgnIndex += "var a_sLgn = \"".length();
                                     int a_sUrlEndIndex = scriptValue.indexOf("\"", a_sUrlIndex);
                                     int a_sLgnEndIndex = scriptValue.indexOf("\"", a_sLgnIndex);
                                     if (a_sUrlEndIndex >= 0 && a_sLgnEndIndex >= 0) {
                                         String src = getAbsolutePath(initmethod,
                                                 scriptValue.substring(a_sLgnIndex, a_sLgnEndIndex) +
                                                         scriptValue.substring(a_sUrlIndex, a_sUrlEndIndex));
                                         LOGGER.debug("Detected script based logon, redirect to form at " + src);
                                         HttpMethod newInitMethod = DavGatewayHttpClientFacade.executeFollowRedirects(httpClient, src);
                                         logonMethod = buildLogonMethod(httpClient, newInitMethod);
                                     }
                                 } else {
                                     a_sLgnIndex = scriptValue.indexOf("var a_sLgnQS = \"");
                                     if (a_sUrlIndex >= 0 && a_sLgnIndex >= 0) {
                                         a_sUrlIndex += "var a_sUrl = \"".length();
                                         a_sLgnIndex += "var a_sLgnQS = \"".length();
                                         int a_sUrlEndIndex = scriptValue.indexOf("\"", a_sUrlIndex);
                                         int a_sLgnEndIndex = scriptValue.indexOf("\"", a_sLgnIndex);
                                         if (a_sUrlEndIndex >= 0 && a_sLgnEndIndex >= 0) {
                                             String src = initmethod.getPath() +
                                                     scriptValue.substring(a_sLgnIndex, a_sLgnEndIndex) +
                                                     scriptValue.substring(a_sUrlIndex, a_sUrlEndIndex);
                                             LOGGER.debug("Detected script based logon, redirect to form at " + src);
                                             HttpMethod newInitMethod = DavGatewayHttpClientFacade.executeFollowRedirects(httpClient, src);
                                             logonMethod = buildLogonMethod(httpClient, newInitMethod);
                                         }
                                     }
                                 }
                             }
                         }
 
 
                     }
                 }
             }
         } catch (IOException e) {
             LOGGER.error("Error parsing login form at " + initmethod.getURI());
         } finally {
             initmethod.releaseConnection();
         }
 
         if (logonMethod == null) {
             throw new IOException("Authentication form not found at " + initmethod.getURI());
         }
         return logonMethod;
     }
 
     protected HttpMethod formLogin(HttpClient httpClient, HttpMethod initmethod, String userName, String password) throws IOException {
         LOGGER.debug("Form based authentication detected");
 
         HttpMethod logonMethod = buildLogonMethod(httpClient, initmethod);
         ((PostMethod) logonMethod).addParameter("username", userName);
         ((PostMethod) logonMethod).addParameter("password", password);
         logonMethod = DavGatewayHttpClientFacade.executeFollowRedirects(httpClient, logonMethod);
         return logonMethod;
     }
 
     protected String getMailPath(HttpMethod method) {
         String result = null;
         // get user mail URL from html body (multi frame)
         BufferedReader mainPageReader = null;
         try {
             mainPageReader = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
             String line;
             // find base url
             final String BASE_HREF = "<base href=\"";
             //noinspection StatementWithEmptyBody
             while ((line = mainPageReader.readLine()) != null && line.toLowerCase().indexOf(BASE_HREF) == -1) {
             }
             if (line != null) {
                 int start = line.toLowerCase().indexOf(BASE_HREF) + BASE_HREF.length();
                 int end = line.indexOf("\"", start);
                 String mailBoxBaseHref = line.substring(start, end);
                 URL baseURL = new URL(mailBoxBaseHref);
                 result = baseURL.getPath();
             } else {
                 // failover for Exchange 2007 : try to get mailbox from options
                 result = getMailPathFromOptions(method.getPath());
             }
         } catch (IOException e) {
             LOGGER.error("Error parsing main page at " + method.getPath());
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
 
         return result;
     }
 
     protected String getMailPathFromOptions(String path) {
         String result = null;
         // get user mail URL from html body
         BufferedReader optionsPageReader = null;
         GetMethod optionsMethod = new GetMethod(path + "?ae=Options&t=About");
         try {
             wdr.retrieveSessionInstance().executeMethod(optionsMethod);
             optionsPageReader = new BufferedReader(new InputStreamReader(optionsMethod.getResponseBodyAsStream()));
             String line;
             // find mailbox full name
             final String MAILBOX_BASE = "cn=recipients/cn=";
             //noinspection StatementWithEmptyBody
             while ((line = optionsPageReader.readLine()) != null && line.toLowerCase().indexOf(MAILBOX_BASE) == -1) {
             }
             if (line != null) {
                 int start = line.toLowerCase().indexOf(MAILBOX_BASE) + MAILBOX_BASE.length();
                 int end = line.indexOf("<", start);
                 result = "/exchange/" + line.substring(start, end) + "/";
             }
         } catch (IOException e) {
             LOGGER.error("Error parsing options page at " + optionsMethod.getPath());
         } finally {
             if (optionsPageReader != null) {
                 try {
                     optionsPageReader.close();
                 } catch (IOException e) {
                     LOGGER.error("Error parsing options page at " + optionsMethod.getPath());
                 }
             }
             optionsMethod.releaseConnection();
         }
 
         return result;
     }
 
     void login() throws IOException {
         LOGGER.debug("Session " + this + " login");
         try {
             boolean isBasicAuthentication = isBasicAuthentication(poolKey.url);
 
             // get proxy configuration from setttings properties
             URL urlObject = new URL(poolKey.url);
             // webdavresource is unable to create the correct url type
             HttpURL httpURL;
             if (poolKey.url.startsWith("http://")) {
                 httpURL = new HttpURL(poolKey.userName, poolKey.password,
                         urlObject.getHost(), urlObject.getPort());
             } else if (poolKey.url.startsWith("https://")) {
                 httpURL = new HttpsURL(poolKey.userName, poolKey.password,
                         urlObject.getHost(), urlObject.getPort());
             } else {
                 throw new IllegalArgumentException("Invalid URL: " + poolKey.url);
             }
             wdr = new WebdavResource(httpURL, WebdavResource.NOACTION, 0);
 
             // get the internal HttpClient instance
             HttpClient httpClient = wdr.retrieveSessionInstance();
 
             DavGatewayHttpClientFacade.configureClient(httpClient);
 
             // get webmail root url
             // providing credentials
             // manually follow redirect
             HttpMethod method = DavGatewayHttpClientFacade.executeFollowRedirects(httpClient, poolKey.url);
 
             if (!isBasicAuthentication) {
                 method = formLogin(httpClient, method, poolKey.userName, poolKey.password);
             }
             int status = method.getStatusCode();
 
             if (status == HttpStatus.SC_UNAUTHORIZED) {
                 throw new AuthenticationException("Authentication failed: invalid user or password");
             } else if (status != HttpStatus.SC_OK) {
                 HttpException ex = new HttpException();
                 ex.setReasonCode(status);
                 ex.setReason(method.getStatusText());
                 throw ex;
             }
             // test form based authentication
             String queryString = method.getQueryString();
             if (queryString != null && queryString.contains("reason=2")) {
                 method.releaseConnection();
                 if (poolKey.userName != null && poolKey.userName.contains("\\")) {
                     throw new AuthenticationException("Authentication failed: invalid user or password");
                 } else {
                     throw new AuthenticationException("Authentication failed: invalid user or password, " +
                             "retry with domain\\user");
                 }
             }
 
             mailPath = getMailPath(method);
 
             if (mailPath == null) {
                 throw new HttpException(poolKey.url + " not found in body, authentication failed: password expired ?");
             }
 
             // got base http mailbox http url
             wdr.setPath(mailPath);
             getWellKnownFolders();
             // set current folder to Inbox
             selectFolder("INBOX");
 
             wdr.setPath(URIUtil.getPath(inboxUrl));
 
         } catch (AuthenticationException exc) {
             LOGGER.error(exc.toString());
             throw exc;
         } catch (IOException exc) {
             StringBuffer message = new StringBuffer();
             message.append("DavMail login exception: ");
             if (exc.getMessage() != null) {
                 message.append(exc.getMessage());
             } else if (exc instanceof HttpException) {
                 message.append(((HttpException) exc).getReasonCode());
                 String httpReason = ((HttpException) exc).getReason();
                 if (httpReason != null) {
                     message.append(" ");
                     message.append(httpReason);
                 }
             } else {
                 message.append(exc);
             }
 
             LOGGER.error(message.toString());
             throw new IOException(message.toString());
         }
     }
 
     protected void getWellKnownFolders() throws IOException {
         // Retrieve well known URLs
         Enumeration foldersEnum = wdr.propfindMethod(0, WELL_KNOWN_FOLDERS);
         if (!foldersEnum.hasMoreElements()) {
             throw new IOException("Unable to get mail folders");
         }
         ResponseEntity inboxResponse = (ResponseEntity) foldersEnum.
                 nextElement();
         Enumeration inboxPropsEnum = inboxResponse.getProperties();
         if (!inboxPropsEnum.hasMoreElements()) {
             throw new IOException("Unable to get mail folders");
         }
         while (inboxPropsEnum.hasMoreElements()) {
             Property inboxProp = (Property) inboxPropsEnum.nextElement();
             if ("inbox".equals(inboxProp.getLocalName())) {
                 inboxUrl = URIUtil.decode(inboxProp.getPropertyAsString());
             }
             if ("deleteditems".equals(inboxProp.getLocalName())) {
                 deleteditemsUrl = URIUtil.decode(inboxProp.getPropertyAsString());
             }
             if ("sentitems".equals(inboxProp.getLocalName())) {
                 sentitemsUrl = URIUtil.decode(inboxProp.getPropertyAsString());
             }
             if ("sendmsg".equals(inboxProp.getLocalName())) {
                 sendmsgUrl = URIUtil.decode(inboxProp.getPropertyAsString());
             }
             if ("drafts".equals(inboxProp.getLocalName())) {
                 draftsUrl = URIUtil.decode(inboxProp.getPropertyAsString());
             }
             if ("calendar".equals(inboxProp.getLocalName())) {
                 calendarUrl = URIUtil.decode(inboxProp.getPropertyAsString());
             }
         }
         LOGGER.debug("Inbox URL : " + inboxUrl +
                 " Trash URL : " + deleteditemsUrl +
                 " Sent URL : " + sentitemsUrl +
                 " Send URL : " + sendmsgUrl +
                 " Drafts URL : " + draftsUrl +
                 " Calendar URL : " + calendarUrl
         );
     }
 
     /**
      * Create message in current folder
      *
      * @param messageName    message name
      * @param bcc            blind carbon copy header
      * @param messageBody    mail body
      * @param allowOverwrite allow existing message overwrite
      * @throws java.io.IOException when unable to create message
      */
     public void createMessage(String messageName, String bcc, String messageBody, boolean allowOverwrite) throws IOException {
         createMessage(currentFolderUrl, messageName, bcc, messageBody, allowOverwrite);
     }
 
     /**
      * Create message in specified folder.
      * Will overwrite an existing message with same subject in the same folder
      *
      * @param folderUrl      Exchange folder URL
      * @param messageName    message name
      * @param bcc            blind carbon copy header
      * @param messageBody    mail body
      * @param allowOverwrite allow existing message overwrite
      * @throws java.io.IOException when unable to create message
      */
     public void createMessage(String folderUrl, String messageName, String bcc, String messageBody, boolean allowOverwrite) throws IOException {
         String messageUrl = URIUtil.encodePathQuery(folderUrl + "/" + messageName + ".EML");
 
         PutMethod putmethod = new PutMethod(messageUrl);
         putmethod.setRequestHeader("Translate", "f");
         putmethod.setRequestHeader("Content-Type", "message/rfc822");
         InputStream bodyStream = null;
         try {
             // use same encoding as client socket reader
             bodyStream = new ByteArrayInputStream(messageBody.getBytes());
             putmethod.setRequestBody(bodyStream);
             int code = wdr.retrieveSessionInstance().executeMethod(putmethod);
 
             if (code == HttpURLConnection.HTTP_OK) {
                 if (allowOverwrite) {
                     LOGGER.warn("Overwritten message " + messageUrl);
                 } else {
                     throw new IOException("Overwritten message " + messageUrl);
                 }
             } else if (code != HttpURLConnection.HTTP_CREATED) {
                 throw new IOException("Unable to create message " + code + " " + putmethod.getStatusLine());
             }
         } finally {
             if (bodyStream != null) {
                 try {
                     bodyStream.close();
                 } catch (IOException e) {
                     LOGGER.error(e);
                 }
             }
             putmethod.releaseConnection();
         }
         // update message with blind carbon copy
         if (bcc != null && bcc.length() > 0) {
             PropPatchMethod patchMethod = new PropPatchMethod(messageUrl);
             try {
                 patchMethod.addPropertyToSet("bcc", bcc, "b", "urn:schemas:mailheader:");
                 int statusCode = wdr.retrieveSessionInstance().executeMethod(patchMethod);
                 if (statusCode != HttpStatus.SC_MULTI_STATUS) {
                     throw new IOException("Unable to add bcc recipients: " + bcc);
                 }
                 Enumeration responseEntityEnum = patchMethod.getResponses();
 
                 if (responseEntityEnum.hasMoreElements()) {
                     ResponseEntity entity = (ResponseEntity) responseEntityEnum.nextElement();
                     if (entity.getStatusCode() != HttpStatus.SC_OK) {
                         throw new IOException("Unable to add bcc recipients: " + bcc);
                     }
                 }
 
             } finally {
                 patchMethod.releaseConnection();
             }
         }
     }
 
     protected Message buildMessage(ResponseEntity responseEntity) throws URIException {
         Message message = new Message();
         message.messageUrl = URIUtil.decode(responseEntity.getHref());
         Enumeration propertiesEnum = responseEntity.getProperties();
         while (propertiesEnum.hasMoreElements()) {
             Property prop = (Property) propertiesEnum.nextElement();
             String localName = prop.getLocalName();
 
             if ("x0e080003".equals(localName)) {
                 message.size = Integer.parseInt(prop.getPropertyAsString());
             } else if ("uid".equals(localName)) {
                 message.uid = prop.getPropertyAsString();
             }
         }
 
         return message;
     }
 
     public Message getMessage(String messageUrl) throws IOException {
 
         Enumeration messageEnum = wdr.propfindMethod(messageUrl, 0, MESSAGE_REQUEST_PROPERTIES);
 
         if ((wdr.getStatusCode() != HttpURLConnection.HTTP_OK)
                 || !messageEnum.hasMoreElements()) {
             throw new IOException("Unable to get message: " + wdr.getStatusCode()
                     + " " + wdr.getStatusMessage());
         }
         ResponseEntity entity = (ResponseEntity) messageEnum.nextElement();
 
         return buildMessage(entity);
 
     }
 
     public List<Message> getAllMessages() throws IOException {
         List<Message> messages = new ArrayList<Message>();
         String searchRequest = "Select \"DAV:uid\", \"http://schemas.microsoft.com/mapi/proptag/x0e080003\"" +
                 "                FROM Scope('SHALLOW TRAVERSAL OF \"" + currentFolderUrl + "\"')\n" +
                 "                WHERE \"DAV:ishidden\" = False AND \"DAV:isfolder\" = False\n" +
                 "                ORDER BY \"urn:schemas:httpmail:date\" ASC";
         Enumeration folderEnum = DavGatewayHttpClientFacade.executeSearchMethod(wdr.retrieveSessionInstance(), currentFolderUrl, searchRequest);
 
         while (folderEnum.hasMoreElements()) {
             ResponseEntity entity = (ResponseEntity) folderEnum.nextElement();
 
             Message message = buildMessage(entity);
             messages.add(message);
         }
         return messages;
     }
 
     /**
      * Delete oldest messages in trash.
      * keepDelay is the number of days to keep messages in trash before delete
      *
      * @throws IOException when unable to purge messages
      */
     public void purgeOldestTrashAndSentMessages() throws IOException {
         int keepDelay = Settings.getIntProperty("davmail.keepDelay");
         if (keepDelay != 0) {
             purgeOldestFolderMessages(deleteditemsUrl, keepDelay);
         }
         // this is a new feature, default is : do nothing
         int sentKeepDelay = Settings.getIntProperty("davmail.sentKeepDelay");
         if (sentKeepDelay != 0) {
             purgeOldestFolderMessages(sentitemsUrl, sentKeepDelay);
         }
     }
 
     public void purgeOldestFolderMessages(String folderUrl, int keepDelay) throws IOException {
         Calendar cal = Calendar.getInstance();
         cal.add(Calendar.DAY_OF_MONTH, -keepDelay);
         LOGGER.debug("Delete messages in " + folderUrl + " since " + cal.getTime());
 
         String searchRequest = "Select \"DAV:uid\"" +
                 "                FROM Scope('SHALLOW TRAVERSAL OF \"" + folderUrl + "\"')\n" +
                 "                WHERE \"DAV:isfolder\" = False\n" +
                 "                   AND \"DAV:getlastmodified\" &lt; '" + dateFormatter.format(cal.getTime()) + "'\n";
         Enumeration folderEnum = DavGatewayHttpClientFacade.executeSearchMethod(wdr.retrieveSessionInstance(), folderUrl, searchRequest);
 
         while (folderEnum.hasMoreElements()) {
             ResponseEntity entity = (ResponseEntity) folderEnum.nextElement();
             String messageUrl = URIUtil.decode(entity.getHref());
 
             LOGGER.debug("Delete " + messageUrl);
             wdr.deleteMethod(messageUrl);
         }
     }
 
     public void sendMessage(List<String> recipients, BufferedReader reader) throws IOException {
         String line = reader.readLine();
         StringBuilder mailBuffer = new StringBuilder();
         StringBuilder recipientBuffer = new StringBuilder();
         boolean inHeader = true;
         boolean inRecipientHeader = false;
         while (!".".equals(line)) {
             mailBuffer.append(line).append("\n");
             line = reader.readLine();
 
             if (inHeader && line.length() == 0) {
                 inHeader = false;
             }
 
             inRecipientHeader = inRecipientHeader && line.startsWith(" ");
 
             if ((inHeader && line.length() >= 3) || inRecipientHeader) {
                 String prefix = line.substring(0, 3).toLowerCase();
                 if ("to:".equals(prefix) || "cc:".equals(prefix) || inRecipientHeader) {
                     inRecipientHeader = true;
                     recipientBuffer.append(line);
                 }
             }
             // Exchange 2007 : skip From: header
             if ((inHeader && line.length() >= 5)) {
                 String prefix = line.substring(0, 5).toLowerCase();
                 if ("from:".equals(prefix)) {
                     line = reader.readLine();
                 }
             }
             // patch thunderbird html in reply for correct outlook display
             if (line.startsWith("<head>")) {
                 mailBuffer.append(line).append("\n");
                 line = "  <style> blockquote { display: block; margin: 1em 0px; padding-left: 1em; border-left: solid; border-color: blue; border-width: thin;}</style>";
             }
         }
         // remove visible recipients from list
         List<String> visibleRecipients = new ArrayList<String>();
         for (String recipient : recipients) {
             if (recipientBuffer.indexOf(recipient) >= 0) {
                 visibleRecipients.add(recipient);
             }
         }
         recipients.removeAll(visibleRecipients);
 
         StringBuffer bccBuffer = new StringBuffer();
         for (String recipient : recipients) {
             if (bccBuffer.length() > 0) {
                 bccBuffer.append(',');
             }
             bccBuffer.append("&lt;");
             bccBuffer.append(recipient);
             bccBuffer.append("&gt;");
         }
 
         String messageName = UUID.randomUUID().toString();
 
         createMessage(draftsUrl, messageName,
                 bccBuffer.toString()
                 , mailBuffer.toString(), false);
 
         // warning : slide library expects *unencoded* urls
         String tempUrl = draftsUrl + "/" + messageName + ".EML";
         boolean sent = wdr.moveMethod(tempUrl, sendmsgUrl);
         if (!sent) {
             throw new IOException("Unable to send message: " + wdr.getStatusCode()
                     + " " + wdr.getStatusMessage());
         }
 
     }
 
     /**
      * Select current folder.
      * Folder name can be logical names INBOX, DRAFTS or TRASH (translated to local names),
      * relative path to user base folder or absolute path.
      *
      * @param folderName folder name
      * @return Folder object
      * @throws IOException when unable to change folder
      */
     public Folder selectFolder(String folderName) throws IOException {
         Folder folder = new Folder();
         folder.folderUrl = null;
         if ("INBOX".equals(folderName)) {
             folder.folderUrl = inboxUrl;
         } else if ("TRASH".equals(folderName)) {
             folder.folderUrl = deleteditemsUrl;
         } else if ("DRAFTS".equals(folderName)) {
             folder.folderUrl = draftsUrl;
             // absolute folder path
         } else if (folderName != null && folderName.startsWith("/")) {
             folder.folderUrl = folderName;
         } else {
             folder.folderUrl = mailPath + folderName;
         }
 
         Vector<String> reqProps = new Vector<String>();
         reqProps.add("urn:schemas:httpmail:unreadcount");
         reqProps.add("DAV:childcount");
         Enumeration folderEnum = wdr.propfindMethod(folder.folderUrl, 0, reqProps);
 
         if (folderEnum.hasMoreElements()) {
             ResponseEntity entity = (ResponseEntity) folderEnum.nextElement();
             Enumeration propertiesEnum = entity.getProperties();
             while (propertiesEnum.hasMoreElements()) {
                 Property prop = (Property) propertiesEnum.nextElement();
                 if ("unreadcount".equals(prop.getLocalName())) {
                     folder.unreadCount = Integer.parseInt(prop.getPropertyAsString());
                 }
                 if ("childcount".equals(prop.getLocalName())) {
                     folder.childCount = Integer.parseInt(prop.getPropertyAsString());
                 }
             }
 
         } else {
             throw new IOException("Folder not found: " + folder.folderUrl);
         }
         currentFolderUrl = folder.folderUrl;
         return folder;
     }
 
     public static class Folder {
         public String folderUrl;
         public int childCount;
         public int unreadCount;
     }
 
     public class Message {
         public String messageUrl;
         public String uid;
         public int size;
 
         public void write(OutputStream os) throws IOException {
             HttpMethod method = null;
             BufferedReader reader = null;
             try {
                 method = new GetMethod(URIUtil.encodePath(messageUrl));
                 method.setRequestHeader("Content-Type", "text/xml; charset=utf-8");
                 method.setRequestHeader("Translate", "f");
                 wdr.retrieveSessionInstance().executeMethod(method);
 
                 boolean inHTML = false;
 
                 reader = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
                 OutputStreamWriter isoWriter = new OutputStreamWriter(os);
                 String line;
                 while ((line = reader.readLine()) != null) {
                     if (".".equals(line)) {
                         line = "..";
                         // patch text/calendar to include utf-8 encoding
                     } else if ("Content-Type: text/calendar;".equals(line)) {
                         StringBuffer headerBuffer = new StringBuffer();
                         headerBuffer.append(line);
                         while ((line = reader.readLine()) != null && line.startsWith("\t")) {
                             headerBuffer.append((char) 13);
                             headerBuffer.append((char) 10);
                             headerBuffer.append(line);
                         }
                         if (headerBuffer.indexOf("charset") < 0) {
                             headerBuffer.append(";charset=utf-8");
                         }
                         headerBuffer.append((char) 13);
                         headerBuffer.append((char) 10);
                         headerBuffer.append(line);
                         line = headerBuffer.toString();
                         // detect html body to patch Exchange html body
                     } else if (line.startsWith("<html")) {
                         inHTML = true;
                     } else if (inHTML && "</html>".equals(line)) {
                         inHTML = false;
                     }
                     if (inHTML) {
                         //    line = line.replaceAll("&#8217;", "'");
                         //    line = line.replaceAll("&#8230;", "...");
                     }
                     isoWriter.write(line);
                     isoWriter.write((char) 13);
                     isoWriter.write((char) 10);
                 }
                 isoWriter.flush();
             } finally {
                 if (reader != null) {
                     try {
                         reader.close();
                     } catch (IOException e) {
                         LOGGER.warn("Error closing message input stream", e);
                     }
                 }
                 if (method != null) {
                     method.releaseConnection();
                 }
             }
         }
 
         public void delete() throws IOException {
             String destination = deleteditemsUrl + messageUrl.substring(messageUrl.lastIndexOf("/"));
             LOGGER.debug("Deleting : " + messageUrl + " to " + destination);
 
             wdr.moveMethod(messageUrl, destination);
             if (wdr.getStatusCode() == HttpURLConnection.HTTP_PRECON_FAILED) {
                 int count = 2;
                 // name conflict, try another name
                 while (wdr.getStatusCode() == HttpURLConnection.HTTP_PRECON_FAILED) {
                     wdr.moveMethod(messageUrl, destination.substring(0, destination.lastIndexOf('.')) + "-" + count++ + ".eml");
                 }
             }
 
             LOGGER.debug("Deleted to :" + destination + " " + wdr.getStatusCode() + " " + wdr.getStatusMessage());
         }
 
     }
 
     public WebdavResource getWebDavResource() {
         return wdr;
     }
 
     public class Event {
         protected String href;
         protected String etag;
 
         public String getICS() throws IOException {
             LOGGER.debug("Get event: " + href);
             StringBuilder buffer = new StringBuilder();
             GetMethod method = new GetMethod(URIUtil.encodePath(href));
             method.setRequestHeader("Content-Type", "text/xml; charset=utf-8");
             method.setRequestHeader("Translate", "f");
             BufferedReader eventReader = null;
             try {
                 int status = wdr.retrieveSessionInstance().executeMethod(method);
                 if (status != HttpStatus.SC_OK) {
                     LOGGER.warn("Unable to get event at " + href + " status: " + status);
                 }
                 eventReader = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream(), "UTF-8"));
                 String line;
                 boolean inbody = false;
                 while ((line = eventReader.readLine()) != null) {
                     if ("BEGIN:VCALENDAR".equals(line)) {
                         inbody = true;
                     }
                     if (inbody) {
                         buffer.append(line);
                         buffer.append((char) 13);
                         buffer.append((char) 10);
                     }
                     if ("END:VCALENDAR".equals(line)) {
                         inbody = false;
                     }
                 }
 
             } finally {
                 if (eventReader != null) {
                     try {
                         eventReader.close();
                     } catch (IOException e) {
                         LOGGER.error("Error parsing event at " + method.getPath());
                     }
                 }
                 method.releaseConnection();
             }
             return fixICS(buffer.toString(), true);
         }
 
         public String getPath() {
             return href.substring(calendarUrl.length());
         }
 
         public String getEtag() {
             return etag;
         }
     }
 
     public List<Event> getAllEvents() throws IOException {
         int caldavPastDelay = Settings.getIntProperty("davmail.caldavPastDelay", Integer.MAX_VALUE);
         String dateCondition = "";
         if (caldavPastDelay != Integer.MAX_VALUE) {
             Calendar cal = Calendar.getInstance();
             cal.add(Calendar.DAY_OF_MONTH, -caldavPastDelay);
             dateCondition = "                AND \"urn:schemas:calendar:dtstart\" > '" + dateFormatter.format(cal.getTime()) + "'\n";
         }
 
         List<Event> events = new ArrayList<Event>();
         String searchRequest = "<?xml version=\"1.0\"?>\n" +
                 "<d:searchrequest xmlns:d=\"DAV:\">\n" +
                 "        <d:sql> Select \"DAV:getetag\", \"urn:schemas:calendar:instancetype\"" +
                 "                FROM Scope('SHALLOW TRAVERSAL OF \"" + calendarUrl + "\"')\n" +
                 "                WHERE NOT\"urn:schemas:calendar:instancetype\" = 2\n" +
                 "                AND NOT\"urn:schemas:calendar:instancetype\" = 3\n" +
                 "                AND \"DAV:contentclass\" = 'urn:content-classes:appointment'\n" +
                 dateCondition +
                 "                ORDER BY \"urn:schemas:calendar:dtstart\" DESC\n" +
                 "         </d:sql>\n" +
                 "</d:searchrequest>";
         SearchMethod searchMethod = new SearchMethod(URIUtil.encodePath(calendarUrl), searchRequest);
         searchMethod.setDebug(4);
         try {
             int status = wdr.retrieveSessionInstance().executeMethod(searchMethod);
             // Also accept OK sent by buggy servers.
             if (status != HttpStatus.SC_MULTI_STATUS
                     && status != HttpStatus.SC_OK) {
                 HttpException ex = new HttpException();
                 ex.setReasonCode(status);
                 throw ex;
             }
 
             Enumeration calendarEnum = searchMethod.getResponses();
             while (calendarEnum.hasMoreElements()) {
                 events.add(buildEvent((ResponseEntity) calendarEnum.nextElement()));
             }
         } finally {
             searchMethod.releaseConnection();
         }
         return events;
     }
 
     public Event getEvent(String path) throws IOException {
         Enumeration calendarEnum = wdr.propfindMethod(calendarUrl + "/" + URIUtil.decode(path), 0, EVENT_REQUEST_PROPERTIES);
         if (!calendarEnum.hasMoreElements()) {
             throw new IOException("Unable to get calendar event");
         }
         return buildEvent((ResponseEntity) calendarEnum.
                 nextElement());
     }
 
     protected Event buildEvent(ResponseEntity calendarResponse) throws URIException {
         Event event = new Event();
         String href = calendarResponse.getHref();
         event.href = URIUtil.decode(href);
         Enumeration propertiesEnumeration = calendarResponse.getProperties();
         while (propertiesEnumeration.hasMoreElements()) {
             Property property = (Property) propertiesEnumeration.nextElement();
             if ("getetag".equals(property.getLocalName())) {
                 event.etag = property.getPropertyAsString();
             }
         }
         return event;
     }
 
     protected String fixICS(String icsBody, boolean fromServer) throws IOException {
         // first pass : detect
         class AllDayState {
             boolean isAllDay = false;
             boolean hasCdoAllDay = false;
             boolean isCdoAllDay = false;
         }
         List<AllDayState> allDayStates = new ArrayList<AllDayState>();
         AllDayState currentAllDayState = new AllDayState();
         BufferedReader reader = null;
         try {
             reader = new BufferedReader(new StringReader(icsBody));
             String line;
             while ((line = reader.readLine()) != null) {
                 int index = line.indexOf(':');
                 if (index >= 0) {
                     String key = line.substring(0, index);
                     String value = line.substring(index + 1);
                     if ("DTSTART;VALUE=DATE".equals(key)) {
                         currentAllDayState.isAllDay = true;
                     } else if ("X-MICROSOFT-CDO-ALLDAYEVENT".equals(key)) {
                         currentAllDayState.hasCdoAllDay = true;
                         currentAllDayState.isCdoAllDay = "TRUE".equals(value);
                     } else if ("END:VEVENT".equals(line)) {
                         allDayStates.add(currentAllDayState);
                         currentAllDayState = new AllDayState();
                     }
                 }
             }
         } finally {
             if (reader != null) {
                 reader.close();
             }
         }
         // second pass : fix
         int count = 0;
         StringBuilder result = new StringBuilder();
         try {
             reader = new BufferedReader(new StringReader(icsBody));
             String line;
             while ((line = reader.readLine()) != null) {
                 if (currentAllDayState.isAllDay && "X-MICROSOFT-CDO-ALLDAYEVENT:FALSE".equals(line)) {
                     line = "X-MICROSOFT-CDO-ALLDAYEVENT:TRUE";
                 } else if ("END:VEVENT".equals(line) && currentAllDayState.isAllDay && !currentAllDayState.hasCdoAllDay) {
                     result.append("X-MICROSOFT-CDO-ALLDAYEVENT:TRUE").append((char) 13).append((char) 10);
                 } else if (!currentAllDayState.isAllDay && "X-MICROSOFT-CDO-ALLDAYEVENT:TRUE".equals(line)) {
                     line = "X-MICROSOFT-CDO-ALLDAYEVENT:FALSE";
                 } else if (fromServer && currentAllDayState.isCdoAllDay && line.startsWith("DTSTART;TZID")) {
                     line = getAllDayLine(line);
                 } else if (fromServer && currentAllDayState.isCdoAllDay && line.startsWith("DTEND;TZID")) {
                     line = getAllDayLine(line);
                 } else if ("BEGIN:VEVENT".equals(line)) {
                     currentAllDayState = allDayStates.get(count++);
                 }
                 result.append(line).append((char) 13).append((char) 10);
             }
         } finally {
             reader.close();
         }
 
         return result.toString();
     }
 
     protected String getAllDayLine(String line) throws IOException {
         int keyIndex = line.indexOf(';');
         int valueIndex = line.lastIndexOf(':');
         int valueEndIndex = line.lastIndexOf('T');
         if (keyIndex < 0 || valueIndex < 0 || valueEndIndex < 0) {
             throw new IOException("Invalid ICS line: " + line);
         }
         String dateValue = line.substring(valueIndex + 1);
         String key = line.substring(0, keyIndex);
         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
         dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
         Date date;
         try {
             date = dateFormat.parse(dateValue);
         } catch (ParseException e) {
             throw new IOException("Invalid ICS line: " + line);
         }
         if ("DTEND".equals(key)) {
             date.setTime(date.getTime() - 1);
         }
         return line.substring(0, keyIndex) + ";VALUE=DATE:" + line.substring(valueIndex + 1, valueEndIndex);
     }
 
     public int createOrUpdateEvent(String path, String icsBody, String etag) throws IOException {
         String messageUrl = URIUtil.encodePathQuery(calendarUrl + "/" + URIUtil.decode(path));
         String uid = path.substring(0, path.lastIndexOf("."));
         PutMethod putmethod = new PutMethod(messageUrl);
         putmethod.setRequestHeader("Translate", "f");
         putmethod.setRequestHeader("Overwrite", "f");
         if (etag != null) {
             putmethod.setRequestHeader("If-Match", etag);
         }
         putmethod.setRequestHeader("Content-Type", "message/rfc822");
         StringBuilder body = new StringBuilder();
         body.append("Content-Transfer-Encoding: 7bit\n" +
                 "Content-class: urn:content-classes:appointment\n" +
                 "MIME-Version: 1.0\n" +
                 "Content-Type: multipart/alternative;\n" +
                 "\tboundary=\"----=_NextPart_").append(uid).append("\"\n" +
                 "\n" +
                 "This is a multi-part message in MIME format.\n" +
                 "\n" +
                 "------=_NextPart_").append(uid).append("\n" +
                 "Content-class: urn:content-classes:appointment\n" +
                 "Content-Type: text/calendar;\n" +
                 "\tmethod=REQUEST;\n" +
                 "\tcharset=\"utf-8\"\n" +
                 "Content-Transfer-Encoding: 8bit\n\n");
         body.append(new String(fixICS(icsBody, false).getBytes("UTF-8"), "ISO-8859-1"));
         body.append("------=_NextPart_").append(uid).append("--\n");
         putmethod.setRequestBody(body.toString());
         int status;
         try {
             status = wdr.retrieveSessionInstance().executeMethod(putmethod);
 
             if (status == HttpURLConnection.HTTP_OK) {
                 LOGGER.warn("Overwritten event " + messageUrl);
             } else if (status != HttpURLConnection.HTTP_CREATED) {
                 LOGGER.warn("Unable to create or update message " + status + " " + putmethod.getStatusLine());
             }
         } finally {
             putmethod.releaseConnection();
         }
         return status;
     }
 
     public int deleteEvent(String path) throws IOException {
         wdr.deleteMethod(calendarUrl + "/" + URIUtil.decode(path));
         return wdr.getStatusCode();
     }
 
     public String getCalendarEtag() throws IOException {
         String etag = null;
         //wdr.setDebug(4);
         Enumeration calendarEnum = wdr.propfindMethod(calendarUrl, 0);
         //wdr.setDebug(0);
         if (!calendarEnum.hasMoreElements()) {
             throw new IOException("Unable to get calendar object");
         }
         while (calendarEnum.hasMoreElements()) {
             ResponseEntity calendarResponse = (ResponseEntity) calendarEnum.
                     nextElement();
             Enumeration propertiesEnumeration = calendarResponse.getProperties();
             while (propertiesEnumeration.hasMoreElements()) {
                 Property property = (Property) propertiesEnumeration.nextElement();
                 if ("http://schemas.microsoft.com/repl/".equals(property.getNamespaceURI())
                         && "contenttag".equals(property.getLocalName())) {
                     etag = property.getPropertyAsString();
                 }
             }
         }
         if (etag == null) {
             throw new IOException("Unable to get calendar etag");
         }
         return etag;
     }
 
     /**
      * Get current Exchange user name
      *
      * @return user name
      * @throws java.io.IOException on error
      */
     public String getUserName() throws IOException {
         int index = mailPath.lastIndexOf("/", mailPath.length() - 2);
         if (index >= 0 && mailPath.endsWith("/")) {
             return mailPath.substring(index + 1, mailPath.length() - 1);
         } else {
             throw new IOException("Invalid mail path: " + mailPath);
         }
     }
 
     /**
      * Get current user email
      *
      * @return user email
      * @throws java.io.IOException on error
      */
     public String getEmail() throws IOException {
         String email = null;
         GetMethod getMethod = new GetMethod("/public/?Cmd=galfind&AN=" + getUserName());
         try {
             int status = wdr.retrieveSessionInstance().executeMethod(getMethod);
             if (status != HttpStatus.SC_OK) {
                 throw new IOException("Unable to get user email from: " + getMethod.getPath());
             }
             email = XMLStreamUtil.getElementContentByLocalName(getMethod.getResponseBodyAsStream(), "EM");
         } finally {
             getMethod.releaseConnection();
         }
         if (email == null) {
             throw new IOException("Unable to get user email from: " + getMethod.getPath());
         }
 
         return email;
     }
 
     /**
      * Search users in global address book
      *
      * @param searchAttribute exchange search attribute
      * @param searchValue     search value
      * @return List of users
      * @throws java.io.IOException on error
      */
     public Map<String, Map<String, String>> galFind(String searchAttribute, String searchValue) throws IOException {
         Map<String, Map<String, String>> results;
         GetMethod getMethod = new GetMethod(URIUtil.encodePathQuery("/public/?Cmd=galfind&" + searchAttribute + "=" + searchValue));
         try {
             int status = wdr.retrieveSessionInstance().executeMethod(getMethod);
             if (status != HttpStatus.SC_OK) {
                 throw new IOException(status + "Unable to find users from: " + getMethod.getURI());
             }
             results = XMLStreamUtil.getElementContentsAsMap(getMethod.getResponseBodyAsStream(), "item", "AN");
         } finally {
             getMethod.releaseConnection();
         }
 
         return results;
     }
 
     public void galLookup(Map<String, String> person) {
         if (!disableGalLookup) {
             GetMethod getMethod = null;
             try {
                 getMethod = new GetMethod(URIUtil.encodePathQuery("/public/?Cmd=gallookup&ADDR=" + person.get("EM")));
                 int status = wdr.retrieveSessionInstance().executeMethod(getMethod);
                 if (status != HttpStatus.SC_OK) {
                     throw new IOException(status + "Unable to find users from: " + getMethod.getURI());
                 }
                 Map<String, Map<String, String>> results = XMLStreamUtil.getElementContentsAsMap(getMethod.getResponseBodyAsStream(), "person", "alias");
                 // add detailed information
                 if (results.size() > 0) {
                     Map<String, String> fullperson = results.get(person.get("AN"));
                     for (Map.Entry<String, String> entry : fullperson.entrySet()) {
                         person.put(entry.getKey(), entry.getValue());
                     }
                 }
             } catch (IOException e) {
                 LOGGER.warn("Unable to gallookup person: " + person + ", disable GalLookup");
                 disableGalLookup = true;
             } finally {
                 if (getMethod != null) {
                     getMethod.releaseConnection();
                 }
             }
         }
     }
 
     public String getFreebusy(Map<String, String> valueMap) throws IOException {
         String result = null;
 
         String startDateValue = valueMap.get("DTSTART");
         String endDateValue = valueMap.get("DTEND");
         String attendee = valueMap.get("ATTENDEE");
         if (attendee.startsWith("mailto:")) {
             attendee = attendee.substring("mailto:".length());
         }
         int interval = 30;
 
         SimpleDateFormat icalParser = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
         icalParser.setTimeZone(new SimpleTimeZone(0, "GMT"));
 
         SimpleDateFormat shortIcalParser = new SimpleDateFormat("yyyyMMdd");
         shortIcalParser.setTimeZone(new SimpleTimeZone(0, "GMT"));
 
         SimpleDateFormat owaFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
         owaFormatter.setTimeZone(new SimpleTimeZone(0, "GMT"));
 
         String url;
         Date startDate;
         Date endDate;
         try {
             if (startDateValue.length() == 8) {
                 startDate = shortIcalParser.parse(startDateValue);
             } else {
                 startDate = icalParser.parse(startDateValue);
             }
             if (endDateValue.length() == 8) {
                 endDate = shortIcalParser.parse(endDateValue);
             } else {
                 endDate = icalParser.parse(endDateValue);
             }
             url = "/public/?cmd=freebusy" +
                     "&start=" + owaFormatter.format(startDate) +
                     "&end=" + owaFormatter.format(endDate) +
                     "&interval=" + interval +
                     "&u=SMTP:" + attendee;
         } catch (ParseException e) {
             throw new IOException(e.getMessage());
         }
 
         GetMethod getMethod = new GetMethod(url);
         getMethod.setRequestHeader("Content-Type", "text/xml");
 
         try {
             int status = wdr.retrieveSessionInstance().executeMethod(getMethod);
             if (status != HttpStatus.SC_OK) {
                 throw new IOException("Unable to get free-busy from: " + getMethod.getPath());
             }
             String body = getMethod.getResponseBodyAsString();
             int startIndex = body.lastIndexOf("<a:fbdata>");
             int endIndex = body.lastIndexOf("</a:fbdata>");
             if (startIndex >= 0 && endIndex >= 0) {
                 String fbdata = body.substring(startIndex + "<a:fbdata>".length(), endIndex);
                 Calendar currentCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                 currentCal.setTime(startDate);
 
                 StringBuilder busyBuffer = new StringBuilder();
                 boolean isBusy = fbdata.charAt(0) != '0';
                 if (isBusy) {
                     busyBuffer.append(icalParser.format(currentCal.getTime()));
                 }
                 for (int i = 1; i < fbdata.length(); i++) {
                     currentCal.add(Calendar.MINUTE, interval);
                     if (isBusy && fbdata.charAt(i) == '0') {
                         // busy -> non busy
                         busyBuffer.append('/').append(icalParser.format(currentCal.getTime()));
                     } else if (!isBusy && fbdata.charAt(i) != '0') {
                         // non busy -> busy
                         if (busyBuffer.length() > 0) {
                             busyBuffer.append(',');
                         }
                         busyBuffer.append(icalParser.format(currentCal.getTime()));
                     }
                     isBusy = fbdata.charAt(i) != '0';
                 }
                 result = busyBuffer.toString();
             }
         } finally {
             getMethod.releaseConnection();
         }
         if (result == null) {
             throw new IOException("Unable to get user free-busy data from: " + getMethod.getPath());
         }
 
         return result;
     }
 
 }
