 package org.onebusaway.wiki.xwiki.impl;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.Locale;
 
 import javax.xml.bind.DatatypeConverter;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.commons.digester.Digester;
 import org.apache.commons.digester.Rule;
 import org.apache.commons.httpclient.Credentials;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.onebusaway.wiki.api.WikiDocumentService;
 import org.onebusaway.wiki.api.WikiException;
 import org.onebusaway.wiki.api.WikiPage;
 
 /**
  * Implementation of {@link WikiDocumentService} that uses the XWiki REST
  * interface to communicate with an existing XWiki server instance. Configure
  * the datasource by calling {@link #setXwikiUrl(String)} to set the base xwiki
  * url for the existing wiki instance.
  * 
  * @author bdferris
  * @see WikiDocumentService
  */
 public class XWikiDocumentServiceImpl implements WikiDocumentService {
 
   private String _xwikiUrl;
 
   private String _xwikiUsername;
 
   private String _xwikiPassword;
 
   /**
    * Set the base XWiki url for the existing XWiki instance that will provide
    * content. If you XWiki home page url looks something like
    * http://your-wiki.com/bin/view/Main, then your base url would be
    * http://your-wiki.com
    * 
    * @param xwikiUrl the base XWiki url
    */
   public void setXwikiUrl(String xwikiUrl) {
     _xwikiUrl = xwikiUrl;
   }
 
   public void setXwikiUsername(String xwikiUsername) {
     _xwikiUsername = xwikiUsername;
   }
 
   public void setXwikiPassword(String xwikiPassword) {
     _xwikiPassword = xwikiPassword;
   }
 
   private XWikiPageImpl getWikiPage(String url, boolean forceRefresh) 
       throws WikiException {
     HttpClient httpClient = new HttpClient();
 
     // Configure basic authentication
     if (_xwikiUsername != null && _xwikiPassword != null) {
       httpClient.getParams().setAuthenticationPreemptive(true);
       Credentials defaultcreds = new UsernamePasswordCredentials(
           _xwikiUsername, _xwikiPassword);
 
       try {
         URL parsedUrl = new URL(url);
         int port = parsedUrl.getPort() == -1 ? 80 : parsedUrl.getPort();
         httpClient.getState().setCredentials(
             new AuthScope(parsedUrl.getHost(), port, AuthScope.ANY_REALM),
             defaultcreds);
       } catch (MalformedURLException ex) {
         throw new WikiException("bad url: " + url, ex);
       }
     }
 
     GetMethod getMethod = new GetMethod(url);
 
     getMethod.addRequestHeader("Accept", "application/xml");
 
     int code = evaluateHttpMethod(httpClient, getMethod);
 
     if (code != HttpStatus.SC_OK) {
       return null;
     }
 
     Digester digester = new Digester();
 
     List<XWikiPageImpl> pages = new ArrayList<XWikiPageImpl>();
     digester.push(pages);
 
     digester.addObjectCreate("page", XWikiPageImpl.class);
     digester.addBeanPropertySetter("page/space", "namespace");
     digester.addBeanPropertySetter("page/name");
     digester.addBeanPropertySetter("page/title");
     digester.addBeanPropertySetter("page/content");
     digester.addBeanPropertySetter("page/language");
     digester.addSetProperties("page/translations", "default", "defaultLanguage");
     digester.addObjectCreate("page/translations/translation", XWikiPageTranslation.class);
     digester.addSetProperties("page/translations/translation");
     digester.addSetNext("page/translations/translation", "addTranslation");
     digester.addObjectCreate("page/translations/translation/link", XWikiPageLink.class);
     digester.addSetProperties("page/translations/translation/link");
     digester.addSetNext("page/translations/translation/link", "addLink");
     digester.addSetNext("page", "add");
 
     digester.addRule("page/modified", new Iso8601DateRule("lastModified"));
 
     try {
       digester.parse(getMethod.getResponseBodyAsStream());
     } catch (Exception ex) {
       throw new WikiException("error parsing xwiki response", ex);
     }
 
     if (pages.isEmpty())
       return null;
     
     return pages.get(0);
   }
 
   @Override
   public WikiPage getWikiPage(String namespace, String name,
       Locale locale, boolean forceRefresh) throws WikiException {
 
     // Get wiki page in default language, with its translations
     String url = _xwikiUrl + "/rest/wikis/xwiki/spaces/" + namespace
         + "/pages/" + name;
     
     XWikiPageImpl page = getWikiPage(url, forceRefresh);
     
     // If a specific locale is requested and does not match the default one
    if (locale != null && !locale.equals(page.getLocale())) {
       // Check if the requested locale is available for this wiki page
       XWikiPageTranslation translation = page.findTranslation(locale.getLanguage());
       if (translation != null) {
         return getWikiPage(translation.getURL(), forceRefresh);
       }
     }
     
     // Return the default wiki page
     return page;
   }
 
   private int evaluateHttpMethod(HttpClient httpClient, GetMethod getMethod)
       throws WikiException {
     try {
       return httpClient.executeMethod(getMethod);
     } catch (Exception ex) {
       throw new WikiException("error evaluating xwiki http method", ex);
     }
 
   }
 
   private static class Iso8601DateRule extends Rule {
     
     private String _propertyName;
     
     private String _body;
     
     
     public Iso8601DateRule(String propertyName) {
       _propertyName = propertyName;
     }
 
     public void body(String namespace, String name, String text)
         throws Exception {
       _body = text;
     }
     
     public void end(String namespace, String name) throws Exception {
       Calendar c = DatatypeConverter.parseDateTime(_body);
       
       String property = _propertyName;
 
       if (property == null) {
           // If we don't have a specific property name,
           // use the element name.
           property = name;
       }
 
       // Get a reference to the top object
       Object top = digester.peek();
 
       // Set the property (with conversion as necessary)
       BeanUtils.setProperty(top, property, c.getTime());
     }
   }
 }
