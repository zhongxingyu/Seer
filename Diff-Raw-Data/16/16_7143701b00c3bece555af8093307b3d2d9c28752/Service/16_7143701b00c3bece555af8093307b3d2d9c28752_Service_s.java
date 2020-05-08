 package org.cloudcrawler.domain.crawler;
 
 import com.google.inject.Inject;
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.cloudcrawler.domain.crawler.contentparser.XHTMLContentParser;
 import org.cloudcrawler.domain.crawler.robotstxt.RobotsTxtService;
 import org.cloudcrawler.system.charset.converter.ConversionResult;
 import org.cloudcrawler.system.charset.converter.ConversionService;
 import org.cloudcrawler.system.http.HttpService;
 import org.cloudcrawler.system.stream.SizeValidator;
 import org.cloudcrawler.system.uri.URIValidator;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.xpath.XPathExpressionException;
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URISyntaxException;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * The crawling service is responsible to create
  * crawled object for a given URI.
  * <p/>
  * It returns a collection of documents.
  * This collection contains the requested document
  * with all crawling information and Crawling documents for all
  * linked documents which are marked as uncrawled
  *
  * @author Timo Schmidt <timo-schmidt@gmx.net>
  */
 public class Service {
 
     protected HttpService httpService;
 
     protected XHTMLContentParser xHTMLParser;
 
     protected RobotsTxtService robotsTxtRobotsTxtService;
 
     protected ConversionService utf8ConversionService;
 
     protected SizeValidator sizeValidator;
 
     protected URIValidator uriValidator;
 
     @Inject
     public Service(HttpService httpService,
                    XHTMLContentParser xHTMLParser,
                    RobotsTxtService robotsTxtRobotsTxtService,
                    ConversionService utf8ConversionService,
                    SizeValidator sizeValidator,
                    URIValidator uriValidator) {
         this.httpService = httpService;
         this.xHTMLParser = xHTMLParser;
         this.robotsTxtRobotsTxtService = robotsTxtRobotsTxtService;
         this.utf8ConversionService = utf8ConversionService;
         this.sizeValidator = sizeValidator;
         this.uriValidator = uriValidator;
     }
 
     /**
      * This method is used to fetch the content of a crawling document
      * and creating new documents for linked documents.
      *
      * @param toCrawl
      * @return Vector<Document>
      * @throws Exception
      */
     public Vector<Document> crawlAndFollowLinks(Document toCrawl, boolean storeContent) throws Exception {
 
         try {
             Vector<Document> results = new Vector<Document>();
 
             if(!this.uriValidator.isValid(toCrawl.getUri())) {
                 System.out.println("Unallowed url "+toCrawl.getUri().toString());
                 return results;
             }
 
             if (getRequestIsUnAllowedByRobotsTxt(toCrawl) ||
                     getHeadRequestIndicatesUnAllowedContentType(toCrawl)) {
                 return results;
             }
 
 
             HttpResponse getResponse = httpService.get(toCrawl.getUri());
             Header header = getResponse.getLastHeader(new String("Content-Type"));
             String getMimeType = header.getValue();
 
             if (getGetRequestIndicatesUnAllowedContentType(getMimeType)) {
                 return results;
             }
 
             // All checks passed, do the download
             System.out.println("Crawling " + toCrawl.getUri().toString());
             toCrawl.incrementCrawCount();
             toCrawl.setCrawlingState(Document.CRAWLING_STATE_CRAWLED);
 
             InputStream is              = getResponse.getEntity().getContent();
             BufferedInputStream bis     = new BufferedInputStream(is);
 
             if (getContainsUnAllowedFileSize(bis)) {
                 return results;
             }
 
             String website = convertWebsiteInputStreamToUtf8String(bis);
             toCrawl.setContent(website);
             toCrawl.setMimeType(getMimeType);
             results.add(toCrawl);
             results = this.prepareLinkedDocuments(results, toCrawl);
 
             if(!storeContent) {
                 toCrawl.setContent("");
             }
             this.httpService.close(getResponse);
 
             return results;
         } catch (Exception e) {
             this.httpService.reset();
             throw e;
         }
     }
 
     /**
      *
      * @param bis
      * @return
      * @throws IOException
      */
     protected boolean getContainsUnAllowedFileSize(BufferedInputStream bis) throws IOException {
        boolean hasAllowedSize = sizeValidator.isValid(bis, 1024 * 1024);
 
        if(!hasAllowedSize) {
            System.out.println("Crawl blocked by unallowed filesize");
            this.httpService.reset();
            return true;
        }
 
        return false;
     }
 
     /**
      * This method is checking if the request is blocked by the robots txt.
      *
      * @param toCrawl
      * @return
      * @throws Exception
      */
     private boolean getRequestIsUnAllowedByRobotsTxt(Document toCrawl) throws Exception {
         boolean isBlocked = true;
         isBlocked = !this.robotsTxtRobotsTxtService.isAllowedUri(toCrawl.getUri());
         if (isBlocked) {
             System.out.println("Crawl blocked by robotstxt txt");
             this.httpService.reset();
         }
 
         return isBlocked;
     }
 
     /**
      * This method is used to check if the get request indicates an unallowed content type.
      *
      * @param getMimeType
      * @return boolean
      */
     private boolean getGetRequestIndicatesUnAllowedContentType(String getMimeType) {
         Boolean isGetIndicatingHtml = getMimeType.contains(new String("text/html"));
         if (!isGetIndicatingHtml) {
             System.out.println("No html response indicated in get");
             this.httpService.reset();
             return true;
         }
         return false;
     }
 
     /**
      * This method is used to check if the head request in advanced allready indicates that
      * the mimeType of the response is unallowed.
      *
      * @param toCrawl
      * @return boolean
      * @throws IOException
      */
     private boolean getHeadRequestIndicatesUnAllowedContentType(Document toCrawl) throws IOException {
         HttpResponse headResponse = httpService.head(toCrawl.getUri());
         Header header = headResponse.getLastHeader(new String("Content-Type"));
         Boolean isHeadIndicatingHtml = header.getValue().contains(new String("text/html"));
         this.httpService.close(headResponse);
         if (!isHeadIndicatingHtml) {
             System.out.println("No html response indicated by head");
             this.httpService.reset();
             return true;
         }
         return false;
     }
 
     /**
      * Converts the input stream of the website into utf8.
      *
      * @param bis
      * @return
      * @throws IOException
      */
     private String convertWebsiteInputStreamToUtf8String(BufferedInputStream bis) throws IOException {
         ConversionResult result = utf8ConversionService.convertToUTF8(bis);
         String website = result.getContent();
 
         if (result.getWasConverted()) {
             Pattern regex = Pattern.compile("(<meta.*?charset=[^\"']+(\"|')[^>]*>)", Pattern.CASE_INSENSITIVE);
             Matcher regexMatcher = regex.matcher(website);
             website = regexMatcher.replaceAll("");
         }
 
         return website;
     }
 
     /**
      * This method is used to extract the links from the input documents and
      * create new documents for every linked element.
      *
      * @param result
      * @param document
      * @return
      */
     protected Vector<Document> prepareLinkedDocuments(Vector<Document> result, Document document) {
         int analyzeCount = document.getLinkAnalyzeCount();
 
         if (analyzeCount != 0) {
             return result;
         }
 
         try {
             xHTMLParser.initialize(document.getUri(), document.getContent(), document.getMimeType());
             Vector<Link> links = xHTMLParser.getOutgoingLinks(false);
 
             for (Link link : links) {
                 if ((link == null) || !this.uriValidator.isValid(link.getTargetUri())) {
                     continue;
                 }
 
                 //create a new document from the followed link
                 Document linkDocument = new Document();
                 linkDocument.addIncomingLink(link);
                 linkDocument.setUri(link.getTargetUri());
                 linkDocument.setCrawlingState(Document.CRAWLING_STATE_WAITING);
 
                 result.add(linkDocument);
             }
 
             document.incrementLinkAnalyzeCount();
 
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (SAXException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (ParserConfigurationException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (XPathExpressionException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (URISyntaxException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         return result;
     }
 }
