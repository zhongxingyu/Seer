 package net.cyklotron.cms.documents.internal;
 
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.dom4j.Attribute;
 import org.dom4j.Document;
 import org.dom4j.Element;
 import org.dom4j.Node;
 import org.objectledge.coral.session.CoralSession;
 import org.objectledge.coral.store.Resource;
 import org.objectledge.encodings.HTMLEntityDecoder;
 import org.objectledge.html.HTMLContentFilter;
 import org.objectledge.html.HTMLException;
 import org.objectledge.html.HTMLService;
 import org.objectledge.pipeline.ProcessingException;
 
 import net.cyklotron.cms.documents.DocumentException;
 import net.cyklotron.cms.documents.DocumentNodeResource;
 import net.cyklotron.cms.documents.LinkRenderer;
 import net.cyklotron.cms.documents.DocumentMetadataHelper;
 import net.cyklotron.cms.site.SiteException;
 import net.cyklotron.cms.site.SiteResource;
 import net.cyklotron.cms.site.SiteService;
 import net.cyklotron.cms.structure.NavigationNodeResource;
 import net.cyklotron.cms.structure.StructureService;
 import net.cyklotron.cms.util.URI;
 import net.cyklotron.cms.util.URI.MalformedURIException;
 
 /**
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
  * @version $Id: DocumentRenderingHelper.java,v 1.18 2008-07-31 14:33:09 rafal Exp $
  */
 public class DocumentRenderingHelper
 {
     private HTMLService htmlService;
 
     private HTMLEntityDecoder entityDecoder = new HTMLEntityDecoder();
 
     private SiteService siteService;
     
     private StructureService structureService;
     
     /** owner document */
     private DocumentNodeResource doc;
     /** owner document's content's DOM */
     private Document contentDom;
     /** owner document's metadata's DOM */
     private Document metaDom;
     /** owner document's keywords  */
     private List keywords;
     /** serialized document pages */
     private String[] pages;
     
     public DocumentRenderingHelper(CoralSession coralSession, SiteService siteService,
         StructureService structureService, HTMLService htmlService, 
         DocumentNodeResource doc,
     	LinkRenderer linkRenderer, HTMLContentFilter filter)
         throws ProcessingException
     {
         this.siteService = siteService;
         this.structureService = structureService;
         this.doc = doc;
         this.htmlService = htmlService; 
         try
         {
         	// get HTML DOM and filter it
             contentDom = filter.filter(htmlService.textToDom4j(doc.getContent()));
             // WARN: replace URIs
 			// replace internal links
             replaceAnchorURIs(coralSession, contentDom, linkRenderer);
 			// replace image sources
             
 			replaceImageURIs(coralSession, contentDom, linkRenderer);
         }
         catch(HTMLException e)
         {
             contentDom = htmlService.emptyDom4j();
         }
     }
 
     // public interface ///////////////////////////////////////////////////////
 
     public DocumentNodeResource getDocument()
     {
         return doc;
     }
 
     // content /////////////////////////////////////////////////////////////////////////////////////
 
     public String getContent()
     throws DocumentException, HTMLException
     {
         // compose from pages
         int numPages = getNumPages();
         int length = 0;
         for(int i=1; i <= numPages; i++)
         {
             length += getPageContent(i).length();
         }
         StringBuilder buf = new StringBuilder(length);
         for(int i=1; i <= numPages; i++)
         {
             if(i > 1)
             {
                 buf.append("<hr class='page-break' />");
             }
             buf.append(getPageContent(i));
         }
         return buf.toString();
     }
 
     public int getNumPages()
     {
         if(pages == null)
         {
             Element srcBody = getContentDom().getRootElement().element("BODY");
 
             int numPages = 1;
             for(Iterator i=srcBody.nodeIterator(); i.hasNext();)
             {
                 Node n = (Node)(i.next());
                 // match page break
                 if(isPageBreak(n))
                 {
                     numPages++;
                 }
             }
             pages = new String[numPages];
         }
         return pages.length;
     }
 
     public String getPageContent(int i)
     throws DocumentException, HTMLException
     {
         // i is a page number => i belongsto [1..numPages]
         if(i > 0 && i <= getNumPages())
         {
             int index = i - 1;
             if(pages[index] == null)
             {
                 pages[index] = serialize(getPageDom(i));
             }
             return pages[index];
         }
         else
         {
             return "";
         }
     }
 
     // meta ////////////////////////////////////////////////////////////////////////////////////////
 
     public List getKeywords()
     {
         if(keywords == null)
         {
             ArrayList keywords = new ArrayList(10);
             String value = doc.getKeywords();
             if(value != null && value.length() > 0)
             {
                 StringTokenizer tokenizer = new StringTokenizer(value, ",");
 
                 while(tokenizer.hasMoreTokens())
                 {
                     keywords.add(tokenizer.nextToken());
                 }
             }
         }
         return keywords;
     }
 
     // document DOM access methods ////////////////////////////////////////////////////////////
 
     public Document getContentDom()
     {
         return contentDom;
     }
 
     public Document getPageDom(int pageNum)
     {
         Element srcBody = getContentDom().getRootElement().element("BODY");
 
         Document destDocument = htmlService.emptyDom4j();
         Element destBody = destDocument.getRootElement().element("BODY");
 
         int currentPage = 1;
         for(Iterator i=srcBody.nodeIterator(); i.hasNext();)
         {
             Node n = (Node)(i.next());
             // match page break
             if(isPageBreak(n))
             {
                 currentPage++;
                 if(currentPage > pageNum) // stop processing after required page
                 {
                     break;
                 }
             }
             else if(currentPage == pageNum)
             {
                 Node newN = (Node)(n.clone());
                 newN.detach();
                 destBody.add(newN);
             }
         }
         return destDocument;
     }
 
     public Document getMetaDom()
         throws HTMLException
     {
         if(metaDom == null)
         {
             String meta = doc.getMeta();
             if(meta != null && meta.length() > 0)
             {
                 meta = entityDecoder.decodeXML(meta);
                 metaDom = DocumentMetadataHelper.textToDom4j(meta);
             }
         }
         return metaDom;
     }
 
     // utility methods /////////////////////////////////////////////////////////////////////////////
 
     private boolean isPageBreak(Node n)
     {
         if(n instanceof Element)
         {
             Element e = (Element)n;
            if(!e.getName().equals("HR"))
             {
                 return false;
             }
             if(e.attribute("class") == null)
             {
                 return false;
             }
             String value = e.attribute("class").getValue();
             if(value != null && value.equals("page-break"))
             {
                 return true;
             }
             else
             {
                 return false;
             }
         }
         else
         {
             return false;
         }
     }
 
     private String serialize(Document dom)
         throws DocumentException, HTMLException
     {
         String html = "";
         if(dom != null)
         {
             StringWriter writer = new StringWriter();
             htmlService.dom4jToText(dom, writer, true);
             html = writer.toString();
         }
         return html;
     }
 
     // URI modification methods ////////////////////////////////////////////////////////////////////
 
     private void replaceAnchorURIs(CoralSession coralSession, Document dom4jDoc, LinkRenderer linkRenderer)
     {
         // replace uris
         List anchors = dom4jDoc.selectNodes("//A");
         for(Iterator i=anchors.iterator(); i.hasNext();)
         {
             Element element = (Element)(i.next());
             Attribute attribute = element.attribute("href");
 
             // go further if this anchor is not a link
             if(attribute == null)
             {
                 continue;
             }
 
             try
             {
                 // go further if this anchor is not a link
                 if(attribute.getValue() == null || attribute.getValue().startsWith("/"))
                 {
                     continue;
                 }
 
                 URI uri = new URI(attribute.getValue());
 
                 String linkClassName = null;
                 // in CMS link
                 if(uri.getScheme().equals("cms"))
                 {
                     linkClassName = "cms-lnk";
 
                     String wholePath = uri.getSchemeSpecificPart();
                     String fragment = uri.getFragment();
                     int breakIndex = wholePath.indexOf('/');
                     int breakIndex2 = (fragment != null)?
                                     wholePath.length() -fragment.length() -1 //-1 for # character
                                     :wholePath.length();
 
                     String siteName = wholePath.substring(0, breakIndex);
                     String pagePath = wholePath.substring(breakIndex+1, breakIndex2);
 
                     //1. get site
                     SiteResource site = siteService.getSite(coralSession, siteName);
                     if(site != null)
                     {
                         //2. get linked node
                         NavigationNodeResource homepage = structureService.getRootNode(coralSession, site);
                         Resource parent = homepage.getParent();
                         Resource[] temp = coralSession.getStore().getResourceByPath(
                                                                     parent.getPath()+'/'+pagePath);
                         if(temp.length == 1)
                         {
                             // set a virtual for this link
                             StringBuffer newUri = new StringBuffer(
                                 linkRenderer.getNodeURL(coralSession, (NavigationNodeResource)(temp[0])));
                             if(fragment != null)
                             {
                                 newUri.append('#');
                                 newUri.append(fragment);
                             }
                             attribute.setValue(newUri.toString());
                         }
                         else if(temp.length == 0)
                         {
                             throw new DocumentException(
                                 "Cannot find a page with this path - cannot link ");
                         }
                         else
                         {
                             throw new DocumentException(
                                 "Multiple pages with the same path - cannot link ");
                         }
                     }
                 }
                 // in document link
                 else if(uri.getScheme().equals("htmlarea"))
                 {
                     linkClassName = "doc-lnk";
 
                     String wholePath = uri.getSchemeSpecificPart();
                     attribute.setValue(wholePath);
                 }
                 else if(uri.getScheme().equals("mailto"))
                 {
                     linkClassName = "eml-lnk";
                 }
                 else // must be external link
                 {
                     linkClassName = "ext-lnk";
                 }
 
                 if(linkClassName != null)
                 {
                     Attribute classAttr = element.attribute("class");
                     if(classAttr == null)
                     {
                         element.addAttribute("class", linkClassName);
                     }
                     else
                     {
                         classAttr.setValue(classAttr.getValue()+" "+linkClassName);
                     }
                 }
             }
             catch(Exception e)
             {
                 // ignore errors
             }
         }
     }
 
     public void replaceImageURIs(CoralSession coralSession, Document dom4jDoc, LinkRenderer linkRenderer)
     {
         List images = dom4jDoc.selectNodes("//IMG");
         for(Iterator i=images.iterator(); i.hasNext();)
         {
             Element element = (Element)(i.next());
             Attribute attribute = element.attribute("src");
 
             boolean brokenImage = false;
 
             if(attribute == null || attribute.getValue()==null)
             {
                 brokenImage = true;
             }
             else
             {
                 try
                 {
                     if(!attribute.getValue().startsWith("/"))
                     {
                         URI uri = new URI(attribute.getValue());
                         String imageHost = uri.getHost();
                         if(siteService.isVirtualServer(coralSession, imageHost))
                         {
                             // we have an internal image
                             String restOfImageUri = uri.getPath(true, true);
                             attribute.setValue(restOfImageUri);
                         }
                     }
                 }
                 catch(MalformedURIException e)
                 {
                     brokenImage = true;
                 }
                 catch(SiteException e)
                 {
                     brokenImage = true;
                 }
             }
 
             if(brokenImage)
             {
                 String value = linkRenderer.getCommonResourceURL(coralSession, null, "images/no_image.png");
                 if(attribute == null)
                 {
                     element.addAttribute("src", value);
                 }
                 else
                 {
                     attribute.setValue(value);
                 }
             }
         }
     }
 }
