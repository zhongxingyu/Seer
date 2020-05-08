 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.shindig.domain;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 
 import javax.persistence.Entity;
 import javax.persistence.Table;
 import javax.persistence.Column;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathFactory;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.apache.commons.lang.Validate;
 import org.w3c.dom.Document;
 
 /** A social application, mainly represented by the gadget xml url.
  *
  * This id of this class is used as the open social appId.
  */
 @Entity
 @Table(name = "applications")
 public class Application {
 
   /** The id of the application, 0 for a newly created one.
    */
   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   private long id;
 
   /** The application (gadget) title.
    *
    * This is obtained from the gadget xml. Never null.
    */
   @Column(nullable = false)
   private String title;
 
   /** The optional gadget icon.
    *
    * This is obtained from the gadget xml.
    *
    * It is null if not known.
    */
   @Column(nullable = true)
   private String icon;
 
   /** The optional gadget description.
    *
    * This is obtained from the gadget xml.
    *
    * It is null if not known.
    */
   @Column(length = 4096, nullable = true)
   private String description;
 
   /** The optional gadget author.
    *
    * This is obtained from the gadget xml.
    *
    * It is null if not known.
    */
   @Column(nullable = true)
   private String author;
 
   /** The optional gadget thumbnail.
    *
    * This is obtained from the gadget xml.
    *
    * It is null if not known.
    */
   @Column(nullable = true)
   private String thumbnail;
 
   /** The url of the gadget xml spec.
    *
    * It is never null.
    */
   @Column(nullable = false)
   private String url;
 
   /** Hibernate constructor.
    */
   Application() {
   }
 
   /** Creates a new application.
    *
    * This constructor obtains all the gadget information from the xml obtained
    * from gadgetUrl. This constructor can throw an exception if the url is not
    * accessible.
    *
    * If the title is not found in the gadget xml spec, it sets it to the gadget
    * xml url.
    *
    * @param gadgetUrl with the url of the gadget xml. It cannot be empty.
    */
   public Application(final String gadgetUrl) {
     Validate.notEmpty(gadgetUrl, "gadget url cannot be empty");
     url = gadgetUrl;
 
     InputStream gadgetSpecStream = null;
     try {
       // This should probably go to some utility.
       gadgetSpecStream = new URL(url).openStream();
 
       DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
       domFactory.setNamespaceAware(true);
       DocumentBuilder builder = domFactory.newDocumentBuilder();
       Document document = builder.parse(gadgetSpecStream);
 
       title = getXpathValue(document, "/Module/ModulePrefs/@title");
       if (title == null) {
         title = gadgetUrl;
       }
       icon = getXpathValue(document, "/Module/ModulePrefs/icon/text()");
       description = getXpathValue(document,
           "/Module/ModulePrefs/@description");
       author = getXpathValue(document, "/Module/ModulePrefs/@author");
       thumbnail = getXpathValue(document, "/Module/ModulePrefs/@thumbnail");
 
     } catch (Exception e) {
       throw new RuntimeException("Error obtaining gadget title", e);
     } finally {
       if (gadgetSpecStream != null) {
         try {
           gadgetSpecStream.close();
         } catch (IOException e) {
           // Ignored, doesn't matter.
         }
       }
     }
   }
 
   /** Retuns the result of evaluating an xpath expression.
    */
   private String getXpathValue(final Document document,
       final String expression) throws XPathExpressionException {
 
     XPathFactory factory = XPathFactory.newInstance();
     XPath xpath = factory.newXPath();
     XPathExpression expr = xpath.compile(expression);
 
     Object result = expr.evaluate(document, XPathConstants.STRING);
     if (result != null) {
       return result.toString();
     } else {
       return null;
     }
   }
 
   /** @return long the id of the gadget instance.
    */
   public long getId() {
     return id;
   }
 
   /** The title of the gadget.
    *
    * @return The title of gadget, usually obtained from the gadget xml
    * specification. It never returns null.
    */
   public String getTitle() {
     return title;
   }
 
   /** The url of the icon of the gadget.
    *
    * @return The icon of the gadget, usually obtained from the gadget xml
    * specification. It returns null if the icon cannot be determined.
    */
   public String getIcon() {
     return icon;
   }
 
   /** The description of the gadget.
    *
    * @return The description of the gadget, usually obtained from the gadget
    * xml specification. It returns null if the description cannot be
   * determined.
    */
   public String getDescription() {
     return description;
   }
 
   /** The author of the gadget.
    *
    * @return The author of the gadget, usually obtained from the gadget xml
    * specification. It returns null if the author cannot be determined.
    */
   public String getAuthor() {
     return author;
   }
 
   /** The url of the thumbnail of the gadget.
    *
    * @return The thumbnail of the gadget, usually obtained from the gadget xml
    * specification. It returns null if the thumbnail cannot be determined.
    */
   public String getThumbnail() {
     return thumbnail;
   }
 
   /** @return {@link String} location of the gadget xml spec.
    */
   public String getUrl() {
     return url;
   }
 }
 
