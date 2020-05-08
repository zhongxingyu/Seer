 package org.apache.camel.processor.mashup.core;
 
 import org.apache.camel.Exchange;
 import org.apache.camel.Message;
 import org.apache.camel.Processor;
 import org.apache.camel.processor.mashup.api.IExtractor;
 import org.apache.camel.processor.mashup.model.*;
 import org.apache.commons.beanutils.*;
 import org.apache.http.Header;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.impl.client.BasicCookieStore;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.cookie.BasicClientCookie;
 import org.apache.http.util.EntityUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.FileInputStream;
 
 /**
  * Camel processor loading the navigation file and extract data
  */
 public class MashupProcessor implements Processor {
     
     private final static transient Logger LOGGER = LoggerFactory.getLogger(MashupProcessor.class);
     
     public final static String DEFAULT_STORE = "data/mashup";
     public final static String MASHUP_ID_HEADER = "MASHUP_ID";
     public final static String MASHUP_STORE_HEADER = "MASHUP_STORE";
     
     public void process(Exchange exchange) throws Exception {
         LOGGER.trace("Get the Camel in message");
         Message in = exchange.getIn();
         
         LOGGER.trace("Get the Camel out message");
         Message out = exchange.getOut();
         
         LOGGER.trace("Get the {} header", MASHUP_ID_HEADER);
         String mashupId = (String) in.getHeader(MASHUP_ID_HEADER);
         LOGGER.debug("Mashup ID: {}", mashupId);
         
         LOGGER.trace("Get the {} header", MASHUP_STORE_HEADER);
         String store = (String) in.getHeader(MASHUP_STORE_HEADER);
         LOGGER.debug("Mashup Store: {}", store);
         
         LOGGER.debug("Digesting the navigation file {}/{}.xml", store, mashupId);
         FileInputStream fileInputStream = new FileInputStream(store + "/" + mashupId + ".xml");
         Mashup mashup = new Mashup();
         mashup.digeste(fileInputStream);
         
         LOGGER.trace("Create the HTTP client");
         DefaultHttpClient httpClient = new DefaultHttpClient();
         CookieStore cookieStore = CookieStore.getInstance();
 
         LOGGER.trace("Iterate in the pages");
         for (Page page : mashup.getPages()) {
             LOGGER.trace("Replacing the headers in the URL");
             String url = page.getUrl();
             for (String header : in.getHeaders().keySet()) {
                url.replace("%" + header + "%", (String) in.getHeader(header));
             }
             
             LOGGER.trace("Constructing the HTTP request");
             HttpUriRequest request = null;
             if (page.getMethod() != null && page.getMethod().equalsIgnoreCase("POST")) {
                 request = new HttpPost(url);
             } else {
                 request = new HttpGet(url);
             }
             
             if (mashup.getCookie() != null) {
                 LOGGER.trace("Looking for an existing cookie");
                 String cookieKey = (String) in.getHeader(mashup.getCookie().getKey());
                 if (cookieKey == null) {
                     LOGGER.warn("Cookie key " + mashup.getCookie().getKey() + " is not found in the Camel \"in\" header");
                 } else {
                     BasicClientCookie basicClientCookie = cookieStore.getCookie(cookieKey);
                     if (basicClientCookie == null) {
                         LOGGER.debug("No cookie yet exist for " + cookieKey);
                     } else {
                         LOGGER.debug("A cookie exists for " + cookieKey + " use it for the request");
                         BasicCookieStore basicCookieStore = new BasicCookieStore();
                         basicCookieStore.addCookie(basicClientCookie);
                         httpClient.setCookieStore(basicCookieStore);
                     }
                 }
             } else {
                 LOGGER.warn("No cookie configuration defined");
             }
             
             HttpResponse response = httpClient.execute(request);
             HttpEntity entity = response.getEntity();
             
             if (mashup.getCookie() != null) {
                 String cookieKey = (String) in.getHeader(mashup.getCookie().getKey());
                 if (cookieKey == null) {
                     LOGGER.warn("Cookie key " + mashup.getCookie().getKey() + " is not found i nthe Camel \"in\" header");
                 } else {
                     LOGGER.trace("Populating the cookie store");
                     Header[] headers = response.getHeaders("Set-Cookie");
                     for (Header header : headers) {
                         if (header.getName().equals(mashup.getCookie().getName())) {
                             BasicClientCookie basicClientCookie = new BasicClientCookie(mashup.getCookie().getName(), header.getValue());
                             basicClientCookie.setDomain(mashup.getCookie().getDomain());
                             basicClientCookie.setPath(mashup.getCookie().getPath());
                             cookieStore.addCookie(cookieKey, basicClientCookie);
                             break;
                         }
                     }
                 }
             }
             
             if (page.getExtractors() != null && page.getExtractors().size() > 0) {
                 LOGGER.trace("Populate content to be used by extractors");
                 String content = EntityUtils.toString(entity);
                 try {
                     for (Extractor extractor : page.getExtractors()) {
                         IExtractor extractorBean = this.instantiateExtractor(extractor);
                         String extractedData = extractorBean.extract(content);
                         if (extractor.isMandatory() && (extractedData == null || extractedData.isEmpty())) {
                             throw new IllegalStateException("Extracted data is empty");
                         }
                         if (extractor.isAppend()) {
                             out.setBody(out.getBody() + "<extract id=\"" + extractor.getId() + "\"><![CDATA[" + extractedData + "]]></extract>");
                         }
                     }
                 } catch (Exception e) {
                     LOGGER.warn("An exception occurs during the extraction",e);
                     LOGGER.warn("Calling the error handler");
                     exchange.setException(e);
                     out.setFault(true);
                     out.setBody(null);
                     if (page.getErrorHandler() != null && page.getErrorHandler().getExtractors() != null
                             && page.getErrorHandler().getExtractors().size() > 0) {
                         LOGGER.trace("Processing the error handler extractor");
                         for (Extractor extractor : page.getErrorHandler().getExtractors()) {
                             IExtractor extractorBean = this.instantiateExtractor(extractor);
                             String extractedData = extractorBean.extract(content);
                             if (extractedData != null) {
                                 out.setBody(out.getBody() + extractedData);
                             }
                         }
                     }
                 }
 
             }
 
         }
     }
 
     /**
      * Create a new instance of a extractor
      * 
      * @param extractor the extractor model object.
      * @return the IExtractor object.
      */
     protected IExtractor instantiateExtractor(Extractor extractor) throws Exception {
         LOGGER.trace("Create new instance of " + extractor.getClazz() + "extractor");
         Class extractorClass = Class.forName(extractor.getClazz());
         IExtractor extractorBean = (IExtractor) extractorClass.newInstance();
         if (extractor.getProperties() != null) {
             for (Property property : extractor.getProperties()) {
                 LOGGER.trace("Setting property " + property.getName() + " with value " + property.getValue());
                 PropertyUtils.setProperty(extractorBean, property.getName(), property.getValue());   
             }
         }
         return extractorBean;
     }
 
 }
