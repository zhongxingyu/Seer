 /*
 * $HeadURL$
 * $Id$
 *
 * Copyright (c) 2006-2011 by Public Library of Science
 * http://plos.org
 * http://ambraproject.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. |
 */
 
 package org.ambraproject.article.service;
 
 import net.sf.saxon.Controller;
 import net.sf.saxon.TransformerFactoryImpl;
 import org.ambraproject.article.ArchiveProcessException;
 import org.ambraproject.models.Article;
 import org.ambraproject.models.ArticleAsset;
 import org.ambraproject.models.ArticleAuthor;
 import org.ambraproject.models.ArticleEditor;
 import org.ambraproject.models.ArticleRelationship;
 import org.ambraproject.models.Category;
 import org.ambraproject.models.CitedArticle;
 import org.ambraproject.models.CitedArticleAuthor;
 import org.ambraproject.models.CitedArticleEditor;
 import org.ambraproject.util.XPathUtil;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.configuration.Configuration;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Required;
 import org.topazproject.xml.transform.EntityResolvingSource;
 import org.topazproject.xml.transform.cache.CachedSource;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.EntityResolver;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.transform.ErrorListener;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.URIResolver;
 import javax.xml.transform.dom.DOMResult;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.xpath.XPathExpressionException;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 /**
  * {@link IngestArchiveProcessor} that uses an xsl stylesheet to format the article xml into an easy to parse xml
  *
  * @author Alex Kudlick Date: 6/20/11
  *         <p/>
  *         org.ambraproject.article.service
  */
 public class XslIngestArchiveProcessor implements IngestArchiveProcessor {
   private static final Logger log = LoggerFactory.getLogger(XslIngestArchiveProcessor.class);
 
   private DocumentBuilder documentBuilder;
   private TransformerFactory transformerFactory;
   private String xslStyleSheet;
   private Configuration configuration;
   private XPathUtil xPathUtil;
 
 
   public XslIngestArchiveProcessor() {
     transformerFactory = new TransformerFactoryImpl();
     transformerFactory.setURIResolver(new URLResolver());
     transformerFactory.setAttribute("http://saxon.sf.net/feature/version-warning", Boolean.FALSE);
     transformerFactory.setAttribute("http://saxon.sf.net/feature/strip-whitespace", "none");
     transformerFactory.setErrorListener(new ErrorListener() {
       public void warning(TransformerException te) {
         log.warn("Warning received while processing a stylesheet", te);
       }
 
       public void error(TransformerException te) {
         log.warn("Error received while processing a stylesheet", te);
       }
 
       public void fatalError(TransformerException te) {
         log.warn("Fatal error received while processing a stylesheet", te);
       }
     });
     xPathUtil = new XPathUtil();
   }
 
   private static class URLResolver implements URIResolver {
     public Source resolve(String href, String base) throws TransformerException {
       if (href.length() == 0)
         return null;  // URL doesn't handle this case properly, so let default resolver handle it
 
       try {
         URL url = new URL(new URL(base), href);
         return new StreamSource(url.toString());
       } catch (MalformedURLException mue) {
         log.warn("Failed to resolve '" + href + "' relative to '" + base + "' - falling back to " +
             "default URIResolver", mue);
         return null;
       }
     }
   }
 
   /**
    * This allows the stylesheets to access XML docs (such as pmc.xml) in the zip archive.
    */
   private static class ZipURIResolver extends URLResolver {
     private final ZipFile zip;
 
     public ZipURIResolver(ZipFile zip) {
       this.zip = zip;
     }
 
     @Override
     public Source resolve(String href, String base) throws TransformerException {
       if (log.isDebugEnabled())
         log.debug("resolving: base='" + base + "', href='" + href + "'");
 
       if (!base.startsWith("zip:"))
         return super.resolve(href, base);
 
       try {
         InputSource src = resolveToIS(base, href);
         if (src == null)
           return null;
 
         return new EntityResolvingSource(src, new EntityResolver() {
           public InputSource resolveEntity(String publicId, String systemId)
               throws SAXException, IOException {
             if (systemId != null && systemId.startsWith("zip:"))
               return resolveToIS("zip:/", systemId);
             return CachedSource.getResolver().resolveEntity(publicId, systemId);
           }
         });
       } catch (IOException ioe) {
         throw new TransformerException(ioe);
       } catch (SAXException se) {
         throw new TransformerException(se);
       }
     }
 
     private InputSource resolveToIS(String base, String rel) throws IOException {
       URI uri = URI.create(base).resolve(rel);
       InputStream is = zip.getInputStream(zip.getEntry(uri.getPath().substring(1)));
       if (is == null)         // hack to deal with broken AP zip's that contain absolute paths
         is = zip.getInputStream(zip.getEntry(uri.getPath()));
 
       if (log.isDebugEnabled())
         log.debug("resolved: uri='" + uri + "', found=" + (is != null));
 
       if (is == null)
         return null;
 
       InputSource src = new InputSource(is);
       src.setSystemId(uri.toString());
 
       return src;
     }
   }
 
   /**
    * Set the xsl style sheet to use
    *
    * @param xslStyleSheet - The classpath-relative location of an xsl stylesheet to use to process the article xml
    */
   @Required
   public void setXslStyleSheet(String xslStyleSheet) {
     this.xslStyleSheet = xslStyleSheet;
   }
 
   /**
    * Set the document builder to use for constructing documents from the zip file entries
    *
    * @param documentBuilder - the document builder to use
    */
   @Required
   public void setDocumentBuilder(DocumentBuilder documentBuilder) {
     this.documentBuilder = documentBuilder;
   }
 
   @Required
   public void setConfiguration(Configuration configuration) {
     this.configuration = configuration;
   }
 
   @Override
   public Article processArticle(ZipFile archive, Document articleXml) throws ArchiveProcessException {
     InputStream xsl = null;
     try {
       String zipInfo = describeZip(archive);
       xsl = getClass().getClassLoader().getResourceAsStream(xslStyleSheet);
       if (xsl == null) {
         throw new ArchiveProcessException("Couldn't open stylesheet: " + xslStyleSheet);
       }
       Document transformedXml = transformZip(archive, zipInfo, xsl,
           configuration.getString("ambra.platform.doiUrlPrefix", null));
 
       Article article = parseTransformedXml(transformedXml);
       String archiveName = archive.getName().contains(File.separator)
           ? archive.getName().substring(archive.getName().lastIndexOf(File.separator) + 1)
           : archive.getName();
       article.setArchiveName(archiveName);
       return article;
     } catch (IOException e) {
       throw new ArchiveProcessException("Error reading from Zip archive", e);
     } catch (TransformerException e) {
       throw new ArchiveProcessException("Error transforming Article xml", e);
     } catch (ParseException e) {
       throw new ArchiveProcessException("Error parsing a date in the xml", e);
     } catch (XPathExpressionException e) {
       throw new ArchiveProcessException("Error parsing transformed xml", e);
     } finally {
       if (xsl != null) {
         try {
           xsl.close();
         } catch (IOException e) {
           log.warn("Error closing input stream for xsl stylesheet");
         }
       }
     }
   }
 
   /**
    * TODO: use an xml unmarshaller to do this - see <a href="http://static.springsource.org/spring-ws/site/reference/html/oxm.html">this
    * TODO: page</a> for some spring-wrapped versions (mmmmm... spring).  For this it would behoove us to reformat the
    * transformed xml TODO: to match the object model
    *
    * @param transformedXml the result of the xsl transform on the article xml
    * @return a fully-populated, unsaved article object
    * @throws javax.xml.xpath.XPathExpressionException
    *          if there's an error parsing the xml
    */
   private Article parseTransformedXml(Document transformedXml) throws XPathExpressionException, ParseException {
     Article article = new Article();
 
     //basic article properties
     article.setDoi(xPathUtil.evaluate(transformedXml, "//Article/@id"));
     article.setState(Article.STATE_UNPUBLISHED);
     article.seteIssn(xPathUtil.evaluate(transformedXml, "//Article/eIssn"));
 
     //properties that used to be in dublin core
     if (xPathUtil.selectSingleNode(transformedXml, "//Article/dublinCore/title") != null) {
       article.setTitle(getAllText(xPathUtil.selectSingleNode(transformedXml, "//Article/dublinCore/title")));
     }
     if (!xPathUtil.evaluate(transformedXml, "//Article/dublinCore/format/text()").isEmpty()) {
       article.setFormat(xPathUtil.evaluate(transformedXml, "//Article/dublinCore/format/text()"));
     }
     if (!xPathUtil.evaluate(transformedXml, "//Article/dublinCore/language/text()").isEmpty()) {
       article.setLanguage(xPathUtil.evaluate(transformedXml, "//Article/dublinCore/language/text()"));
     }
     if (xPathUtil.selectSingleNode(transformedXml, "//Article/dublinCore/description") != null) {
       article.setDescription(getAllText(xPathUtil.selectSingleNode(transformedXml, "//Article/dublinCore/description")));
     }
     if (!xPathUtil.evaluate(transformedXml, "//Article/dublinCore/rights/text()").isEmpty()) {
       article.setRights(xPathUtil.evaluate(transformedXml, "//Article/dublinCore/rights/text()"));
     }
     DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     if (!xPathUtil.evaluate(transformedXml, "//Article/dublinCore/date/text()").isEmpty()) {
       article.setDate(dateFormatter.parse(xPathUtil.evaluate(transformedXml, "//Article/dublinCore/date/text()")
         .replaceAll(" UTC", "")));
     }
 
     //properties that used to be in bib citation
     if (xPathUtil.selectSingleNode(transformedXml, "//Article/dublinCore/bibliographicCitation") != null) {
       String volume = xPathUtil.evaluate(transformedXml, "//Article/dublinCore/bibliographicCitation/volume/text()");
       if (!volume.isEmpty()) {
         article.setVolume(volume);
       }
       String publisherLocation = xPathUtil.evaluate(transformedXml, "//Article/dublinCore/bibliographicCitation/publisherLocation/text()");
       if (!publisherLocation.isEmpty()) {
         article.setPublisherLocation(publisherLocation);
       }
       String publisherName = xPathUtil.evaluate(transformedXml, "//Article/dublinCore/bibliographicCitation/publisherName/text()");
       if(!publisherName.isEmpty()) {
         article.setPublisherName(publisherName);
       }
       String journal = xPathUtil.evaluate(transformedXml, "//Article/dublinCore/bibliographicCitation/journal/text()");
       if (!journal.isEmpty()) {
         article.setJournal(journal);
       }
       String issue = xPathUtil.evaluate(transformedXml, "//Article/dublinCore/bibliographicCitation/issue/text()");
       if (!issue.isEmpty()) {
         article.setIssue(issue);
       }
       String pages = xPathUtil.evaluate(transformedXml, "//Article/dublinCore/bibliographicCitation/pages/text()");
       if(!pages.isEmpty()) {
         article.setPages(pages);
       }
       String eLocationId = xPathUtil.evaluate(transformedXml, "//Article/dublinCore/bibliographicCitation/eLocationId/text()");
       if(!eLocationId.isEmpty()) {
         article.seteLocationId(eLocationId);
       }
       String url = xPathUtil.evaluate(transformedXml, "//Article/dublinCore/bibliographicCitation/url/text()");
       if (!url.isEmpty()) {
         article.setUrl(url);
       }
     }
 
     //types
     Set<String> articleTypes = parseArticleTypes(transformedXml);
     article.setTypes(articleTypes);
 
     //categories
     Set<Category> categories = parseArticleCategories(transformedXml);
     article.setCategories(categories);
 
     //authors
     List<ArticleAuthor> authors = parseArticleAuthors(transformedXml);
     article.setAuthors(authors);
 
     //editors
     List<ArticleEditor> editors = parseArticleEditors(transformedXml);
     article.setEditors(editors);
 
     //cited articles
     List<CitedArticle> references = parseCitedArticles(transformedXml);
     article.setCitedArticles(references);
 
     //assets
    List<ArticleAsset> assets = parseArticleAssets(transformedXml, article);
     article.setAssets(assets);
 
     //related articles
     List<ArticleRelationship> relatedArticles = parseRelatedArticles(transformedXml, article);
     article.setRelatedArticles(relatedArticles);
 
     List<String> collabAuthors = parseCollabAuthors(transformedXml, "//Article/dublinCore/bibliographicCitation");
     article.setCollaborativeAuthors(collabAuthors);
 
     return article;
   }
 
   private List<String> parseCollabAuthors(Document transformedXml, String nodeXpath) throws XPathExpressionException {
     int collabAuthorCount = Integer.valueOf(
         xPathUtil.evaluate(transformedXml, "count(" + nodeXpath + "/collaborativeAuthors)"));
     List<String> collabAuthors = new ArrayList<String>(collabAuthorCount);
     for (int i = 1; i <= collabAuthorCount; i++) {
       collabAuthors.add(xPathUtil.evaluate(transformedXml, nodeXpath + "/collaborativeAuthors[" + i + "]/text()"));
     }
     return collabAuthors;
   }
 
   private List<ArticleRelationship> parseRelatedArticles(Document transformedXml, Article article) throws XPathExpressionException {
     int relatedArticleCount = Integer.valueOf(xPathUtil.evaluate(transformedXml, "count(//Article/relatedArticles)"));
     List<ArticleRelationship> relatedArticles = new ArrayList<ArticleRelationship>(relatedArticleCount);
     for (int i = 1; i <= relatedArticleCount; i++) {
       ArticleRelationship relatedArticle = new ArticleRelationship();
       relatedArticle.setParentArticle(article);
       relatedArticle.setOtherArticleDoi(
           xPathUtil.evaluate(transformedXml, "//Article/relatedArticles[" + i + "]/article/text()"));
       relatedArticle.setType(
           xPathUtil.evaluate(transformedXml, "//Article/relatedArticles[" + i + "]/relationType/text()"));
       relatedArticles.add(relatedArticle);
     }
     return relatedArticles;
   }
 
   private List<CitedArticle> parseCitedArticles(Document transformedXml) throws XPathExpressionException {
     int referenceCount = Integer.valueOf(xPathUtil.evaluate(transformedXml, "count(" + "//Article/dublinCore/references)"));
     List<CitedArticle> references = new ArrayList<CitedArticle>(referenceCount);
     for (int i = 1; i <= referenceCount; i++) {
       String nodeXpath = "//Article/dublinCore/references[" + i + "]";
       CitedArticle citedArticle = new CitedArticle();
       String type = xPathUtil.evaluate(transformedXml, nodeXpath + "/citationType/text()");
       if (!type.isEmpty()) {
         citedArticle.setCitationType(type);
       }
       String year = xPathUtil.evaluate(transformedXml, nodeXpath + "/year/text()");
       if (!year.isEmpty()) {
         citedArticle.setYear(Integer.valueOf(year));
       }
       String displayYear = xPathUtil.evaluate(transformedXml, nodeXpath + "/displayYear/text()");
       if (!displayYear.isEmpty()) {
         citedArticle.setDisplayYear(displayYear);
       }
       String month = xPathUtil.evaluate(transformedXml, nodeXpath + "/month/text()");
       if (!month.isEmpty()) {
         citedArticle.setMonth(month);
       }
       String day = xPathUtil.evaluate(transformedXml, nodeXpath + "/day/text()");
       if (!day.isEmpty()) {
         citedArticle.setDay(day);
       }
       String volume = xPathUtil.evaluate(transformedXml, nodeXpath + "/volume/text()");
       if (!volume.isEmpty()) {
         citedArticle.setVolume(volume);
       }
       String volumeNumber = xPathUtil.evaluate(transformedXml, nodeXpath + "/volumeNumber/text()");
       if (!volumeNumber.isEmpty()) {
         citedArticle.setVolumeNumber(Integer.valueOf(volumeNumber));
       }
       String publisherLocation = xPathUtil.evaluate(transformedXml, nodeXpath + "/publisherLocation/text()");
       if (!publisherLocation.isEmpty()) {
         citedArticle.setPublisherLocation(publisherLocation);
       }
       String publisherName = xPathUtil.evaluate(transformedXml, nodeXpath + "/publisherName/text()");
       if (!publisherName.isEmpty()) {
         citedArticle.setPublisherName(publisherName);
       }
       String pages = xPathUtil.evaluate(transformedXml, nodeXpath + "/pages/text()");
       if (!pages.isEmpty()) {
         citedArticle.setPages(pages);
       }
       String eLocationId = xPathUtil.evaluate(transformedXml, nodeXpath + "/eLocationId/text()");
       if (!eLocationId.isEmpty()) {
         citedArticle.seteLocationID(eLocationId);
       }
       String journal = xPathUtil.evaluate(transformedXml, nodeXpath + "/journal/text()");
       if (!journal.isEmpty()) {
         citedArticle.setJournal(journal);
       }
       String issue = xPathUtil.evaluate(transformedXml, nodeXpath + "/issue/text()");
       if (!issue.isEmpty()) {
         citedArticle.setIssue(issue);
       }
       String key = xPathUtil.evaluate(transformedXml, nodeXpath + "/key/text()");
       if (!key.isEmpty()) {
         citedArticle.setKey(key);
       }
       String url = xPathUtil.evaluate(transformedXml, nodeXpath + "/url/text()");
       if (!url.isEmpty()) {
         citedArticle.setUrl(url);
       }
       String doi = xPathUtil.evaluate(transformedXml, nodeXpath + "/doi/text()");
       if (!doi.isEmpty()) {
         citedArticle.setDoi(doi);
       }
 
       Node noteNode = xPathUtil.selectSingleNode(transformedXml, nodeXpath + "/note");
       if (noteNode != null) {
         citedArticle.setNote(getAllText(noteNode));
       }
       Node titleNode = xPathUtil.selectSingleNode(transformedXml, nodeXpath + "/title");
       if (titleNode != null) {
         citedArticle.setTitle(getAllText(titleNode));
       }
       Node summaryNode = xPathUtil.selectSingleNode(transformedXml, nodeXpath + "/summary");
       if (summaryNode != null) {
         citedArticle.setSummary(getAllText(summaryNode));
       }
 
       //Set the people referenced by the article in this citation
       int authorCount = Integer.valueOf(xPathUtil.evaluate(transformedXml, "count(" + nodeXpath + "/authors)"));
       int editorCount = Integer.valueOf(xPathUtil.evaluate(transformedXml, "count(" + nodeXpath + "/editors)"));
       List<CitedArticleAuthor> authors = new ArrayList<CitedArticleAuthor>(authorCount);
       List<CitedArticleEditor> editors = new ArrayList<CitedArticleEditor>(editorCount);
       for (int j = 1; j <= authorCount; j++) {
         CitedArticleAuthor author = new CitedArticleAuthor();
         author.setFullName(xPathUtil.evaluate(transformedXml, nodeXpath + "/authors[" + j + "]/realName/text()"));
         author.setGivenNames(xPathUtil.evaluate(transformedXml, nodeXpath + "/authors[" + j + "]/givenNames/text()"));
         author.setSurnames(xPathUtil.evaluate(transformedXml, nodeXpath + "/authors[" + j + "]/surnames/text()"));
         authors.add(author);
       }
       for (int j = 1; j <= editorCount; j++) {
         CitedArticleEditor editor = new CitedArticleEditor();
         editor.setFullName(xPathUtil.evaluate(transformedXml, nodeXpath + "/editors[" + j + "]/realName/text()"));
         editor.setGivenNames(xPathUtil.evaluate(transformedXml, nodeXpath + "/editors[" + j + "]/givenNames/text()"));
         editor.setSurnames(xPathUtil.evaluate(transformedXml, nodeXpath + "/editors[" + j + "]/surnames/text()"));
         editors.add(editor);
       }
       citedArticle.setAuthors(authors);
       citedArticle.setEditors(editors);
 
       List<String> collabAuthors = parseCollabAuthors(transformedXml, nodeXpath);
       citedArticle.setCollaborativeAuthors(collabAuthors);
 
       references.add(citedArticle);
     }
     return references;
   }
 
   private Set<Category> parseArticleCategories(Document transformedXml) throws XPathExpressionException {
     int categoryCount = Integer.valueOf(xPathUtil.evaluate(transformedXml, "count(//Article/categories)"));
     Set<Category> categories = new HashSet<Category>(categoryCount);
     for (int i = 1; i <= categoryCount; i++) {
       String mainCategory = xPathUtil.evaluate(transformedXml, "//Article/categories[" + i + "]/mainCategory/text()");
       String subCategory = xPathUtil.evaluate(transformedXml, "//Article/categories[" + i + "]/subCategory/text()");
       Category category = new Category();
       if (!mainCategory.isEmpty()) {
         category.setMainCategory(mainCategory);
       }
       if (!subCategory.isEmpty()) {
         category.setSubCategory(subCategory);
       }
       categories.add(category);
     }
     return categories;
   }
 
   private Set<String> parseArticleTypes(Document transformedXml) throws XPathExpressionException {
     NodeList articleTypeNodes = xPathUtil.selectNodes(transformedXml, "//Article/articleType/text()");
     Set<String> articleTypes = new HashSet<String>(articleTypeNodes.getLength());
     for (int i = 0; i < articleTypeNodes.getLength(); i++) {
       articleTypes.add(articleTypeNodes.item(i).getNodeValue());
     }
     return articleTypes;
   }
 
   private List<ArticleEditor> parseArticleEditors(Document transformedXml) throws XPathExpressionException {
     int editorCount = Integer.valueOf(xPathUtil.evaluate(transformedXml,
         "count(//Article/dublinCore/bibliographicCitation/editors)"));
     List<ArticleEditor> editors = new ArrayList<ArticleEditor>(editorCount);
     for (int i = 1; i <= editorCount; i++) {
       ArticleEditor editor = new ArticleEditor();
       editor.setFullName(xPathUtil.evaluate(transformedXml,
           "//Article/dublinCore/bibliographicCitation/editors[" + i + "]/realName/text()"));
       editor.setGivenNames(xPathUtil.evaluate(transformedXml,
           "//Article/dublinCore/bibliographicCitation/editors[" + i + "]/givenNames/text()"));
       editor.setSurnames(xPathUtil.evaluate(transformedXml,
           "//Article/dublinCore/bibliographicCitation/editors[" + i + "]/surnames/text()"));
       editor.setSuffix(xPathUtil.evaluate(transformedXml,
           "//Article/dublinCore/bibliographicCitation/editors[" + i + "]/suffix/text()"));
       editors.add(editor);
     }
     return editors;
   }
 
   private List<ArticleAuthor> parseArticleAuthors(Document transformedXml) throws XPathExpressionException {
     int authorCount = Integer.valueOf(xPathUtil.evaluate(transformedXml,
         "count(//Article/dublinCore/bibliographicCitation/authors)"));
     List<ArticleAuthor> authors = new ArrayList<ArticleAuthor>(authorCount);
     for (int i = 1; i <= authorCount; i++) {
       ArticleAuthor author = new ArticleAuthor();
       author.setFullName(xPathUtil.evaluate(transformedXml,
           "//Article/dublinCore/bibliographicCitation/authors[" + i + "]/realName/text()"));
       author.setGivenNames(xPathUtil.evaluate(transformedXml,
           "//Article/dublinCore/bibliographicCitation/authors[" + i + "]/givenNames/text()"));
       author.setSurnames(xPathUtil.evaluate(transformedXml,
           "//Article/dublinCore/bibliographicCitation/authors[" + i + "]/surnames/text()"));
       author.setSuffix(xPathUtil.evaluate(transformedXml,
           "//Article/dublinCore/bibliographicCitation/authors[" + i + "]/suffix/text()"));
       authors.add(author);
     }
     return authors;
   }
 
  private List<ArticleAsset> parseArticleAssets(Document transformedXml, Article article) throws XPathExpressionException {
     //article assets
     int secondaryObjectCount = Integer.valueOf(xPathUtil.evaluate(transformedXml, "count(//Article/parts)"));
     //each secondary object has 3 files, plus article has xml and pdf
     List<ArticleAsset> assets = new ArrayList<ArticleAsset>(4 * secondaryObjectCount + 2);
 
     //get the 'representations' of the article (xml and pdf)
     Integer articleRepCount = Integer.valueOf(xPathUtil.evaluate(transformedXml, "count(//Article/representations)"));
 
     for (int i = 1; i <= articleRepCount; i++) {
       ArticleAsset asset = new ArticleAsset();
      asset.setDoi(article.getDoi());
       asset.setExtension(xPathUtil.evaluate(transformedXml,
           "//Article/representations[" + i + "]/name/text()"));
       asset.setContentType(xPathUtil.evaluate(transformedXml,
           "//Article/representations[" + i + "]/contentType/text()"));
       asset.setSize(Long.valueOf(
           xPathUtil.evaluate(transformedXml, "//Article/representations[" + i + "]/size/text()")));
      asset.setTitle(article.getTitle());
      asset.setDescription(article.getDescription());
       assets.add(asset);
     }
 
 
     for (int i = 1; i <= secondaryObjectCount; i++) {
       String partXpath = "//Article/parts[" + i + "]";
       Integer repCount = Integer.valueOf(xPathUtil.evaluate(transformedXml,
           "count(" + partXpath + "/representations)"));
      String title = xPathUtil.evaluate(transformedXml, partXpath + "/dublinCore/title/text()");
      String description = xPathUtil.evaluate(transformedXml, partXpath + "/dublinCore/description/text()");
       for (int j = 1; j <= repCount; j++) {
         String repXpath = partXpath + "/representations[" + j + "]";
         ArticleAsset asset = new ArticleAsset();
         asset.setDoi(xPathUtil.evaluate(transformedXml, partXpath + "/@id"));
         asset.setContextElement(xPathUtil.evaluate(transformedXml, partXpath + "/contextElement/text()"));
         asset.setContentType(xPathUtil.evaluate(transformedXml, repXpath + "/contentType/text()"));
         asset.setExtension(xPathUtil.evaluate(transformedXml, repXpath + "/name/text()"));
         asset.setSize(Long.valueOf(xPathUtil.evaluate(transformedXml, repXpath + "/size/text()")));
        asset.setTitle(title);
        asset.setDescription(description);
         assets.add(asset);
       }
     }
     return assets;
   }
 
   /**
    * Helper method to get all the text of child nodes of a given node
    *
    * @param node - the node to use as base
    * @return - all nested text in the node
    */
   private String getAllText(Node node) {
 
     String text = "";
     for (int i = 0; i < node.getChildNodes().getLength(); i++) {
       Node childNode = node.getChildNodes().item(i);
       if (Node.TEXT_NODE == childNode.getNodeType()) {
         text += childNode.getNodeValue();
       } else if (Node.ELEMENT_NODE == childNode.getNodeType()) {
         text += "<" + childNode.getNodeName() + ">";
         text += getAllText(childNode);
         text += "</" + childNode.getNodeName() + ">";
       }
     }
     return text.replaceAll("[\n\t]", "").trim();
   }
 
   /**
    * Run the zip file through the xsl stylesheet
    *
    * @param zip          the zip archive containing the items to ingest
    * @param zipInfo      the document describing the zip archive (adheres to zip.dtd)
    * @param handler      the stylesheet to run on <var>zipInfo</var>; this is the main script
    * @param doiUrlPrefix DOI URL prefix
    * @return a document describing the fedora objects to create (must adhere to fedora.dtd)
    * @throws javax.xml.transform.TransformerException
    *          if an error occurs during the processing
    */
   private Document transformZip(ZipFile zip, String zipInfo, InputStream handler, String doiUrlPrefix)
       throws TransformerException {
     Transformer t = transformerFactory.newTransformer(new StreamSource(handler));
     t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
     t.setURIResolver(new ZipURIResolver(zip));
 
     // override the doi url prefix if one is specified in the config
     if (doiUrlPrefix != null)
       t.setParameter("doi-url-prefix", doiUrlPrefix);
 
     /*
      * Note: it would be preferable (and correct according to latest JAXP specs) to use
      * t.setErrorListener(), but Saxon does not forward <xls:message>'s to the error listener.
      * Hence we need to use Saxon's API's in order to get at those messages.
      */
     final StringWriter msgs = new StringWriter();
     ((Controller) t).makeMessageEmitter();
     ((Controller) t).getMessageEmitter().setWriter(msgs);
     t.setErrorListener(new ErrorListener() {
       public void warning(TransformerException te) {
         log.warn("Warning received while processing zip", te);
       }
 
       public void error(TransformerException te) {
         log.warn("Error received while processing zip", te);
         msgs.write(te.getMessageAndLocation() + '\n');
       }
 
       public void fatalError(TransformerException te) {
         log.warn("Fatal error received while processing zip", te);
         msgs.write(te.getMessageAndLocation() + '\n');
       }
     });
 
     Source inp = new StreamSource(new StringReader(zipInfo), "zip:/");
     DOMResult res = new DOMResult();
 
     try {
       t.transform(inp, res);
     } catch (TransformerException te) {
       if (msgs.getBuffer().length() > 0)
         throw new TransformerException(msgs.toString(), te);
       else
         throw te;
     }
     if (msgs.getBuffer().length() > 0)
       throw new TransformerException(msgs.toString());
 
     return (Document) res.getNode();
   }
 
 
   @Override
   public Document extractArticleXml(ZipFile archive) throws ArchiveProcessException {
     try {
       Document manifest = extractXml(archive, "MANIFEST.xml");
       String xmlFileName = xPathUtil.evaluate(manifest, "//article/@main-entry");
       return extractXml(archive, xmlFileName);
     } catch (Exception e) {
       throw new ArchiveProcessException("Error extracting article xml from archive: " + archive.getName(), e);
     }
   }
 
   /**
    * Helper method to extract XML from the zip file
    *
    * @param zipFile  - the zip file containing the file to extract
    * @param fileName - the file to extract
    * @return - the parsed xml file
    * @throws java.io.IOException      - if there's a problem reading from the zip file
    * @throws org.xml.sax.SAXException - if there's a problem parsing the xml
    */
   private Document extractXml(ZipFile zipFile, String fileName) throws IOException, SAXException {
     InputStream inputStream = null;
     try {
       inputStream = zipFile.getInputStream(zipFile.getEntry(fileName));
       return documentBuilder.parse(inputStream);
     } finally {
       if (inputStream != null) {
         try {
           inputStream.close();
         } catch (IOException e) {
           log.warn("Error closing zip input stream during ingest processing", e);
         }
       }
     }
   }
 
   /**
    * Generate a description of the given zip archive.
    *
    * @param zip the zip archive to describe
    * @return the xml doc describing the archive (adheres to zip.dtd)
    * @throws IOException if an exception occurred reading the zip archive
    */
   public static String describeZip(ZipFile zip) throws IOException {
     StringBuilder res = new StringBuilder(500);
     res.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
     res.append("<ZipInfo");
     if (zip.getName() != null)
       res.append(" name=\"").append(attrEscape(zip.getName())).append("\"");
     res.append(">\n");
 
     Enumeration<? extends ZipEntry> entries = zip.entries();
     while (entries.hasMoreElements())
       entry2xml(entries.nextElement(), res);
 
     res.append("</ZipInfo>\n");
     return res.toString();
   }
 
   /**
    * Generate a description for a single zip-entry.
    *
    * @param ze  the zip entry to describe.
    * @param buf the buffer to place the description into
    */
   private static void entry2xml(ZipEntry ze, StringBuilder buf) {
     buf.append("<ZipEntry name=\"").append(attrEscape(ze.getName())).append("\"");
 
     if (ze.isDirectory())
       buf.append(" isDirectory=\"true\"");
     if (ze.getCrc() >= 0)
       buf.append(" crc=\"").append(ze.getCrc()).append("\"");
     if (ze.getSize() >= 0)
       buf.append(" size=\"").append(ze.getSize()).append("\"");
     if (ze.getCompressedSize() >= 0)
       buf.append(" compressedSize=\"").append(ze.getCompressedSize()).append("\"");
     if (ze.getTime() >= 0)
       buf.append(" time=\"").append(ze.getTime()).append("\"");
 
     if (ze.getComment() != null || ze.getExtra() != null) {
       buf.append(">\n");
 
       if (ze.getComment() != null)
         buf.append("<Comment>").append(xmlEscape(ze.getComment())).append("</Comment>\n");
       if (ze.getExtra() != null)
         buf.append("<Extra>").append(base64Encode(ze.getExtra())).append("</Extra>\n");
 
       buf.append("</ZipEntry>\n");
     } else {
       buf.append("/>\n");
     }
   }
 
   private static String xmlEscape(String str) {
     return str.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
   }
 
   private static String attrEscape(String str) {
     return str.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll("\"", "&quot;");
   }
 
   private static String base64Encode(byte[] data) {
     try {
       return new String(Base64.encodeBase64(data), "ISO-8859-1");
     } catch (IOException ioe) {
       throw new RuntimeException(ioe);  // can't happen
     }
   }
 
   /**
    * This method is useful for debugging, so you can put dom2String(xml) in the watch list of the debugger and see the
    * xml
    *
    * @param dom the xml to turn in to a string
    * @return the xml as a string
    */
   private String dom2String(Node dom) {
     try {
       StringWriter sw = new StringWriter(500);
       Transformer t = transformerFactory.newTransformer();
       t.transform(new DOMSource(dom), new StreamResult(sw));
       return sw.toString();
     } catch (TransformerException te) {
       log.error("Error converting dom to string", te);
       return "";
     }
   }
 }
