 package com.mollom.client;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientHandlerException;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.core.util.MultivaluedMapImpl;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 /**
  * Primary interaction point with all of the Mollom services.
  */
 public class MollomClient {
   private final static Logger logger = Logger.getLogger("com.mollom.client.MollomClient");
   private final Client client;
   private final int retries;
 
   private final Unmarshaller unmarshaller;
   private final DocumentBuilder documentBuilder;
 
   private final WebResource contentResource;
   private final WebResource captchaResource;
   private final WebResource feedbackResource;
   private final WebResource blacklistResource;
   private final WebResource whitelistResource;
 
   /**
    * Constructs a new MollomClient instance.
    *
    * MollomClient instances are expensive resources. It is recommended to share
    * a single MollomClient instance between multiple threads. Requests and
    * responses are guaranteed to be thread-safe.
    */
   MollomClient(Client client, WebResource contentResource, WebResource captchaResource,
       WebResource feedbackResource, WebResource blacklistResource,
       WebResource whitelistResource, int retries) {
     this.client = client;
     this.contentResource = contentResource;
     this.captchaResource = captchaResource;
     this.feedbackResource = feedbackResource;
     this.blacklistResource = blacklistResource;
     this.whitelistResource = whitelistResource;
 
     this.retries = retries;
 
     try {
       JAXBContext jaxbContext = JAXBContext.newInstance(Content.class, Captcha.class, BlacklistEntry.class, WhitelistEntry.class);
       this.unmarshaller = jaxbContext.createUnmarshaller();
 
       DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
       this.documentBuilder = documentBuilderFactory.newDocumentBuilder();
     } catch (JAXBException | ParserConfigurationException e) {
       throw new MollomConfigurationException("Failed to initialize MollomClient.", e);
     }
   }
 
   /**
    * Checks content.
    *
    * Injects the Mollom classification scores into the given Content object.
    */
   public void checkContent(Content content)
       throws MollomRequestException, MollomUnexpectedResponseException, MollomNoResponseException {
     MultivaluedMap<String, String> postParams = new MultivaluedMapImpl();
     if (content.getAuthorIp() != null) {
       postParams.putSingle("authorIp", content.getAuthorIp());
     }
     if (content.getAuthorId() != null) {
       postParams.putSingle("authorId", content.getAuthorId());
     }
     if (content.getAuthorOpenIds() != null) {
       // Exception: authorOpenID is the only API parameter that accepts multiple
       // values as a space-separated list.
       String openIds = "";
       for (String authorOpenId : content.getAuthorOpenIds()) {
         openIds += authorOpenId += " ";
       }
       postParams.putSingle("authorOpenid", openIds);
     }
     if (content.getAuthorName() != null) {
       postParams.putSingle("authorName", content.getAuthorName());
     }
     if (content.getAuthorMail() != null) {
       postParams.putSingle("authorMail", content.getAuthorMail());
     }
     if (content.getAuthorUrl() != null) {
       postParams.putSingle("authorUrl", content.getAuthorUrl());
     }
     if (content.getHoneypot() != null) {
       postParams.putSingle("honeypot", content.getHoneypot());
     }
     if (content.getPostTitle() != null) {
       postParams.putSingle("postTitle", content.getPostTitle());
     }
     if (content.getPostBody() != null) {
       postParams.putSingle("postBody", content.getPostBody());
     }
     if (content.getContextUrl() != null) {
       postParams.putSingle("contextUrl", content.getContextUrl());
     }
     if (content.getContextTitle() != null) {
       postParams.putSingle("contextTitle", content.getContextTitle());
     }
 
     if (content.getChecks() != null) {
       List<String> checks = new ArrayList<>();
       for (Check check : content.getChecks()) {
         checks.add(check.toString());
       }
       postParams.put("checks", checks);
     }
     List<Check> requestedChecks = Arrays.asList(content.getChecks());
     if (requestedChecks.contains(Check.SPAM)) {
       if (!content.isAllowUnsure()) {
         postParams.putSingle("unsure", "0");
       }
       if (content.getStrictness() != Strictness.NORMAL) {
         postParams.putSingle("strictness", content.getStrictness().toString());
       }
     }
 
     // Only send the stored parameter after the content was stored.
     // @see Content.setStored()
     if (content.getStored() != -1) {
       postParams.putSingle("stored", Integer.toString(content.getStored()));
     }
     if (content.getUrl() != null) {
       postParams.putSingle("url", content.getUrl());
     }
 
     // If the Content has an ID already (subsequent post after e.g. previewing
     // the content or asking the user to solve a CAPTCHA), re-check the content.
     ClientResponse response;
     if (content.getId() == null) { // Check new content
       response = request("POST", contentResource, postParams);
     } else { // Recheck existing content
       response = request("POST", contentResource.path(content.getId()), postParams);
     }
 
     // Parse the response into a new Content object.
     Content returnedContent = parseBody(response.getEntity(String.class), "content", Content.class);
 
     // Merge classification results into the original Content object.
     content.setId(returnedContent.getId());
     content.setReason(returnedContent.getReason());
 
     if (requestedChecks.contains(Check.SPAM)) {
       content.setSpamClassification(returnedContent.getSpamClassification());
       content.setSpamScore(returnedContent.getSpamScore());
     }
     if (requestedChecks.contains(Check.QUALITY)) {
       content.setQualityScore(returnedContent.getQualityScore());
     }
     if (requestedChecks.contains(Check.PROFANITY)) {
       content.setProfanityScore(returnedContent.getProfanityScore());
     }
     if (requestedChecks.contains(Check.LANGUAGE)) {
       content.setLanguages(returnedContent.getLanguages());
     }
   }
 
   /**
    * Creates a new CAPTCHA resource.
    *
    * Use this to create a standalone CAPTCHA that is not associated with a
    * content.
    *
    * @return The created Captcha object, or null on failure.
    */
   public Captcha createCaptcha(CaptchaType captchaType, boolean ssl)
       throws MollomRequestException, MollomUnexpectedResponseException, MollomNoResponseException {
     return createCaptcha(captchaType, ssl, null);
   }
 
   /**
    * Creates a new CAPTCHA resource linked to an unsure content.
    *
    * The passed in Content object must have been classified as unsure. The newly
    * created CAPTCHA is associated with that content.
    *
    * @return The created Captcha object, or null on failure.
    */
   public Captcha createCaptcha(CaptchaType captchaType, boolean ssl, Content content)
       throws MollomRequestException, MollomUnexpectedResponseException, MollomNoResponseException {
     MultivaluedMap<String, String> postParams = new MultivaluedMapImpl();
     postParams.putSingle("type", captchaType.toString());
     postParams.putSingle("ssl", ssl ? "1" : "0");
    if (content != null && content.getId() != null) {
       postParams.putSingle("contentId", content.getId());
     }
 
     ClientResponse response = request("POST", captchaResource, postParams);
     return parseBody(response.getEntity(String.class), "captcha", Captcha.class);
   }
 
   /**
    * Checks a CAPTCHA solution.
    *
    * Injects the Mollom check results into the given Captcha object.
    */
   public void checkCaptcha(Captcha captcha)
       throws MollomRequestException, MollomUnexpectedResponseException, MollomNoResponseException {
     MultivaluedMap<String, String> postParams = new MultivaluedMapImpl();
     if (captcha.getSolution() == null) {
       throw new MollomIllegalUsageException("Cannot check a CAPTCHA without a solution.");
     }
     postParams.putSingle("solution", captcha.getSolution());
 
     if (captcha.getAuthorIp() != null) {
       postParams.putSingle("authorIp", captcha.getAuthorIp());
     }
     if (captcha.getAuthorId() != null) {
       postParams.putSingle("authorId", captcha.getAuthorId());
     }
     if (captcha.getAuthorOpenIds() != null) {
       // Exception: authorOpenID is the only API parameter that accepts multiple
       // values as a space-separated list.
       String openIds = "";
       for (String authorOpenId : captcha.getAuthorOpenIds()) {
         openIds += authorOpenId += " ";
       }
       postParams.putSingle("authorOpenid", openIds);
     }
     if (captcha.getAuthorName() != null) {
       postParams.putSingle("authorName", captcha.getAuthorName());
     }
     if (captcha.getAuthorMail() != null) {
       postParams.putSingle("authorMail", captcha.getAuthorMail());
     }
     if (captcha.getAuthorUrl() != null) {
       postParams.putSingle("authorUrl", captcha.getAuthorUrl());
     }
 
     if (captcha.getRateLimit() > -1) {
       postParams.putSingle("rateLimit", Integer.toString(captcha.getRateLimit()));
     }
 
     ClientResponse response = request("POST", captchaResource.path(captcha.getId()), postParams);
 
     Captcha returnedCaptcha = parseBody(response.getEntity(String.class), "captcha", Captcha.class);
 
     captcha.setSolved(returnedCaptcha.isSolved() ? 1 : 0);
     captcha.setReason(returnedCaptcha.getReason());
   }
 
   /**
    * Sends feedback for a previously checked content.
    */
   public void sendFeedback(Content content, FeedbackReason reason)
       throws MollomIllegalUsageException, MollomRequestException, MollomUnexpectedResponseException, MollomNoResponseException {
     sendFeedback(content, null, reason);
   }
 
   /**
    * Sends feedback for a previously checked CAPTCHA.
    *
    * Only used for standalone CAPTCHAs. For CAPTCHAs pertaining to content that
    * was classified as unsure, it is sufficient to send feedback for the content
    * only.
    */
   public void sendFeedback(Captcha captcha, FeedbackReason reason)
       throws MollomIllegalUsageException, MollomRequestException, MollomUnexpectedResponseException, MollomNoResponseException {
     sendFeedback(null, captcha, reason);
   }
 
   private void sendFeedback(Content content, Captcha captcha, FeedbackReason reason)
       throws MollomIllegalUsageException, MollomRequestException, MollomUnexpectedResponseException, MollomNoResponseException {
     if (content.getId() == null && captcha.getId() == null) {
       throw new MollomIllegalUsageException("Cannot send feedback without a Content or Captcha ID.");
     }
     if (reason == null) {
       throw new MollomIllegalUsageException("Cannot send feedback without a reason.");
     }
     MultivaluedMap<String, String> postParams = new MultivaluedMapImpl();
     if (content != null) {
       postParams.putSingle("contentId", content.getId());
     }
     if (captcha != null) {
       postParams.putSingle("captchaId", captcha.getId());
     }
     postParams.putSingle("reason", reason.toString());
     request("POST", feedbackResource, postParams);
   }
 
   /**
    * Notify Mollom that the content has been stored on the client-side.
    *
    * @see MollomClient#markAsStored(Content, String, String, String)
    */
   public void markAsStored(Content content)
       throws MollomRequestException, MollomUnexpectedResponseException, MollomNoResponseException {
     markAsStored(content, null, null, null);
   }
 
   /**
    * Notify Mollom that the content has been stored on the client-side.
    */
   public void markAsStored(Content content, String url, String contextUrl, String contextTitle)
       throws MollomRequestException, MollomUnexpectedResponseException, MollomNoResponseException {
     content.setChecks(); // Don't re-check anything
     content.setStored(true);
     // The final/resulting URL of a new content is typically known after
     // accepting and storing a content only; supply it to Mollom.
     if (url != null) {
       content.setUrl(url);
     }
 
     // The context of a content is known before accepting and storing a content
     // already and should thus be supplied with the regular checkContent() calls
     // already.
     if (contextUrl != null) {
       content.setContextUrl(contextUrl);
     }
     if (contextTitle != null) {
       content.setContextTitle(contextTitle);
     }
 
     checkContent(content);
   }
 
   /**
    * Notify Mollom that the content has been deleted on the client-side.
    */
   public void markAsDeleted(Content content)
       throws MollomRequestException, MollomUnexpectedResponseException, MollomNoResponseException {
     content.setChecks(); // Don't re-check anything
     content.setStored(false);
     // TODO: Ideally send the stored parameter only.
     checkContent(content);
   }
 
   /**
    * Saves a blacklist entry.
    *
    * If the entry already exists, update it with the new properties.
    */
   public void saveBlacklistEntry(BlacklistEntry blacklistEntry)
       throws MollomRequestException, MollomUnexpectedResponseException, MollomNoResponseException {
     MultivaluedMap<String, String> postParams = new MultivaluedMapImpl();
     if (blacklistEntry.getValue() == null) {
       throw new MollomIllegalUsageException("Blacklist entries must have a value in order to be saved.");
     }
     postParams.putSingle("value", blacklistEntry.getValue());
     postParams.putSingle("reason", blacklistEntry.getReason().toString());
     postParams.putSingle("context", blacklistEntry.getContext().toString());
     postParams.putSingle("match", blacklistEntry.getMatch().toString());
     postParams.putSingle("status", blacklistEntry.isEnabled() ? "1" : "0");
     if (blacklistEntry.getNote() != null) {
       postParams.putSingle("note", blacklistEntry.getNote());
     }
 
     ClientResponse response;
     if (blacklistEntry.getId() != null) { // Update existing entry
       response = request("POST", blacklistResource.path(blacklistEntry.getId()), postParams);
     } else { // Create new entry
       response = request("POST", blacklistResource, postParams);
     }
 
     BlacklistEntry returnedBlacklistEntry = parseBody(response.getEntity(String.class), "entry", BlacklistEntry.class);
     blacklistEntry.setId(returnedBlacklistEntry.getId());
     blacklistEntry.setCreated(returnedBlacklistEntry.getCreated());
     blacklistEntry.setStatus(returnedBlacklistEntry.isEnabled() ? 1 : 0);
     // The stored value is not necessarily the given value; at minimum,
     // converted to lowercase.
     blacklistEntry.setValue(returnedBlacklistEntry.getValue());
   }
 
   /**
    * Deletes a blacklist entry.
    */
   public void deleteBlacklistEntry(BlacklistEntry blacklistEntry)
       throws MollomRequestException, MollomUnexpectedResponseException, MollomNoResponseException {
     request("POST", blacklistResource.path(blacklistEntry.getId()).path("delete"), new MultivaluedMapImpl());
   }
 
   /**
    * Lists all blacklist entries for this public key.
    *
    * @return A list of all blacklist entries.
    */
   public List<BlacklistEntry> listBlacklistEntries()
       throws MollomRequestException, MollomUnexpectedResponseException, MollomNoResponseException {
     ClientResponse response = request("GET", blacklistResource);
     return parseList(response.getEntity(String.class), "entry", BlacklistEntry.class);
   }
 
   /**
    * Retrieves a blacklist entry with a given ID.
    *
    * @return The blacklist entry or null if not found.
    */
   public BlacklistEntry getBlacklistEntry(String blacklistEntryId)
       throws MollomRequestException, MollomUnexpectedResponseException, MollomNoResponseException {
     ClientResponse response = request("GET", blacklistResource.path(blacklistEntryId));
     return parseBody(response.getEntity(String.class), "entry", BlacklistEntry.class);
   }
 
   /**
    * Saves a whitelist entry.
    *
    * If the entry already exists, update it with the new properties.
    */
   public void saveWhitelistEntry(WhitelistEntry whitelistEntry)
       throws MollomRequestException, MollomUnexpectedResponseException, MollomNoResponseException {
     if (whitelistEntry.getContext() == Context.ALLFIELDS
         || whitelistEntry.getContext() == Context.LINKS
         || whitelistEntry.getContext() == Context.POSTTITLE) {
       throw new MollomConfigurationException("Given context not supported for WhitelistEntry.");
     }
 
     MultivaluedMap<String, String> postParams = new MultivaluedMapImpl();
     if (whitelistEntry.getValue() == null) {
       throw new MollomIllegalUsageException("Whitelist entries must have a value to be saved.");
     }
     postParams.putSingle("value", whitelistEntry.getValue());
     postParams.putSingle("context", whitelistEntry.getContext().toString());
     postParams.putSingle("status", whitelistEntry.isEnabled() ? "1" : "0");
     if (whitelistEntry.getNote() != null) {
       postParams.putSingle("note", whitelistEntry.getNote());
     }
 
     ClientResponse response;
     if (whitelistEntry.getId() != null) { // Update existing entry
       response = request("POST", whitelistResource.path(whitelistEntry.getId()), postParams);
     } else { // Create new entry
       response = request("POST", whitelistResource, postParams);
     }
 
     WhitelistEntry returnedWhitelistEntry = parseBody(response.getEntity(String.class), "entry", WhitelistEntry.class);
     whitelistEntry.setId(returnedWhitelistEntry.getId());
     whitelistEntry.setCreated(returnedWhitelistEntry.getCreated());
     whitelistEntry.setStatus(returnedWhitelistEntry.isEnabled() ? 1 : 0);
     // The stored value is not necessarily the given value; at minimum,
     // converted to lowercase.
     whitelistEntry.setValue(returnedWhitelistEntry.getValue());
   }
 
   /**
    * Deletes a whitelist entry.
    */
   public void deleteWhitelistEntry(WhitelistEntry whitelistEntry)
       throws MollomRequestException, MollomUnexpectedResponseException, MollomNoResponseException {
     request("POST", whitelistResource.path(whitelistEntry.getId()).path("delete"), new MultivaluedMapImpl());
   }
 
   /**
    * Lists all whitelist entries for this public key.
    *
    * @return A list of all whitelist entries.
    */
   public List<WhitelistEntry> listWhitelistEntries()
       throws MollomRequestException, MollomUnexpectedResponseException, MollomNoResponseException {
     ClientResponse response = request("GET", whitelistResource);
     return parseList(response.getEntity(String.class), "entry", WhitelistEntry.class);
   }
 
   /**
    * Retrieves a whitelist entry with a given ID.
    *
    * @return The whitelist entry or null if not found.
    */
   public WhitelistEntry getWhitelistEntry(String whitelistEntryId)
       throws MollomRequestException, MollomUnexpectedResponseException, MollomNoResponseException {
     ClientResponse response = request("GET", whitelistResource.path(whitelistEntryId));
     return parseBody(response.getEntity(String.class), "entry", WhitelistEntry.class);
   }
 
   /**
    * Destroys the MollomClient object.
    *
    * Not doing this can cause connection leaks.
    *
    * The MollomClient instance must not be reused after this method is called;
    * otherwise, undefined behavior will occur.
    */
   public void destroy() {
     client.destroy();
   }
 
   private ClientResponse request(String method, WebResource resource)
       throws MollomRequestException, MollomUnexpectedResponseException, MollomNoResponseException {
     return request(method, resource, null);
   }
 
   private ClientResponse request(String method, WebResource resource, MultivaluedMap<String, String> params)
       throws MollomRequestException, MollomUnexpectedResponseException, MollomNoResponseException {
     for (int retryAttemptNumber = 0; retryAttemptNumber <= retries; retryAttemptNumber++) {
       try {
         ClientResponse response;
         if (params != null) {
           response = resource
             .accept(MediaType.APPLICATION_XML)
             .type(MediaType.APPLICATION_FORM_URLENCODED)
             .method(method, ClientResponse.class, params);
         } else {
           response = resource
             .accept(MediaType.APPLICATION_XML)
             .type(MediaType.APPLICATION_FORM_URLENCODED)
             .method(method, ClientResponse.class);
         }
         if (response.getStatus() >= 400 && response.getStatus() < 500) {
           throw new MollomRequestException(response.getEntity(String.class));
         } else if (response.getStatus() < 200 || response.getStatus() >= 300) {
           throw new MollomUnexpectedResponseException(response.getEntity(String.class));
         }
         return response;
       } catch (ClientHandlerException e) {
         logger.log(Level.WARNING, "Failed to contact Mollom service.", e);
       }
     }
     throw new MollomNoResponseException("Failed to contact Mollom service after retries.");
   }
 
   /**
    * Parses an object out of a response body.
    *
    * Expects XML in the format of:
    * <response>
    *  <code>200</code>
    *  <bodyTag>...</bodyTag>
    * </response>
    *
    * @return JAXB unmarshalled expectedType object from the response.
    *
    * @throws MollomUnexpectedResponseException
    *   Unable to parse the response from the Mollom server. Usually this means
    *   there is a version mismatch between the client library and the Mollom API.
    */
   private <T> T parseBody(String xml, String bodyTag, Class<T> expectedType) throws MollomUnexpectedResponseException {
     try {
       // We have to parse the XML into a Document before passing it to JAXB to
       // get the body, because the Mollom service response returns the object
       // wrapped in a <Response> object.
       Document document = documentBuilder.parse(new InputSource(new StringReader(xml)));
       Node bodyNode = document.getElementsByTagName(bodyTag).item(0);
       return unmarshaller.unmarshal(bodyNode, expectedType).getValue();
     } catch (SAXException | IOException | JAXBException e) {
       throw new MollomUnexpectedResponseException("Issue parsing response from Mollom server.", e);
     }
   }
 
   /**
    * Parses a list of objects out of a response body.
    *
    * Expects XML in the format of:
    * <response>
    *   <code>200</code>
    *   <list>
    *     <bodyTag>...</bodyTag>
    *     ...
    *   </list>
    * </response>
    *
    * @return List of JAXB unmarshalled expectedType objects from the response.
    *
    * @throws MollomUnexpectedResponseException
    *   Unable to parse the response from the Mollom server. Usually this means
    *   there is a version mismatch between the client library and the Mollom API.
    *
    * @todo Support the listCount, listOffset, listTotal response parameters
    *   (required for implementing client-side pagination of e.g. blacklist entries);
    *   cf. http://mollom.com/api#response-list
    */
   private <T> List<T> parseList(String xml, String bodyTag, Class<T> expectedType) throws MollomUnexpectedResponseException {
     try {
       // We have to parse the XML into a Document before passing it to JAXB to
       // get the body, because the Mollom service response returns the object
       // wrapped in a <Response> object.
       Document document = documentBuilder.parse(new InputSource(new StringReader(xml)));
 
       List<T> list = new ArrayList<>();
       NodeList bodyNodes = document.getElementsByTagName(bodyTag);
       for (int i = 0; i < bodyNodes.getLength(); i++) {
         Node bodyNode = bodyNodes.item(i);
         list.add(unmarshaller.unmarshal(bodyNode, expectedType).getValue());
       }
       return list;
     } catch (SAXException | IOException | JAXBException e) {
       throw new MollomUnexpectedResponseException("Issue parsing response from Mollom server.", e);
     }
   }
 }
