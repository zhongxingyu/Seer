 package net.cyklotron.cms.documents;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 
 import org.dom4j.Document;
 import org.objectledge.html.HTMLException;
 import org.objectledge.pipeline.ProcessingException;
 
 import net.cyklotron.cms.documents.internal.DocumentRenderingHelper;
 
 /**
  * Tool for displaying documents contents in velocity templates.
  *
  * @author <a href="mailto:dgajda@caltha.pl">Damian Gajda</a>
  * @version $Id: DocumentTool.java,v 1.3 2006-05-08 12:29:08 pablo Exp $
  */
 public class DocumentTool
 {
     // state variables ////////////////////////////////////////////////////////
 
     /** document data for document which owns this tool. */
     private DocumentRenderingHelper docRenderer;
     /** owner document's current page */
     private int currentPage;
     /** currently used encoding. */
     private String encoding;
 
     // cache variables ////////////////////////////////////////////////////////
     
     /** cache for concatenated content prepared for current request */
     private String encodedContent;
 
     /** cache for xpath expressions executed on content DOM */
     private HashMap contentData = new HashMap();
     /** cache for xpath expressions executed on metadata DOM */
     private HashMap metaData = new HashMap();
     
     // initialisation ///////////////////////////////////////////////////////
     
     public DocumentTool(DocumentRenderingHelper docRenderer, 
         int currentPage, String encoding)
         throws ProcessingException
     {
         // state variables
         this.docRenderer = docRenderer;
         this.currentPage = currentPage;
 		this.encoding = encoding;
     }
     
     // public interface ///////////////////////////////////////////////////////
 
     public DocumentNodeResource getDocument()
     {
         return docRenderer.getDocument();
     }
     
     // content /////////////////////////////////////////////////////////////////////////////////////
 
     public String getTitle()
         throws HTMLException
     {
         return docRenderer.getDocument().getTitle();
     }
 
 	public String getAbstract()
 	    throws HTMLException
 	{
 		return docRenderer.getDocument().getAbstract();
 	}
 
     public String getContent()
         throws HTMLException, DocumentException
     {
         if(encodedContent == null)
         {
             encodedContent = docRenderer.getContent();
         }
         return encodedContent;
     }
 
     public int getNumPages()
     {
         return docRenderer.getNumPages();
     }
 
     public int getCurrentPage()
     {
         return currentPage;
     }
 
     public String getPageContent()
         throws HTMLException, DocumentException
     {
         return getPageContent(currentPage);
     }
 
     public String getPageContent(int page)
         throws HTMLException, DocumentException
     {
         return docRenderer.getPageContent(page);
     }
 
     /**
      * Returns the value of selected data from <code>content</code> attribute.
      *
      * @return selected value from the <code>content</code> attribute.
      */
     public List getContentNodes(String xPathExpression)
         throws DocumentException
     {
         List nodes = (List)contentData.get(xPathExpression);
         if(nodes == null)
         {
             Document dom = docRenderer.getContentDom();
             if(dom == null)
             {
                 nodes = new ArrayList();
             }
             else
             {
                 nodes = dom.selectNodes(xPathExpression);
             }
             contentData.put(xPathExpression, nodes);
         }
         return nodes;
     }
     
     
     // meta ////////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * Returns a list of keywords from document's <code>keywords</code> attribute.
      *
      * @return the list of keywords.
      */
     public List getKeywords()
     {
         return docRenderer.getKeywords();
     }
 
     /**
      * Returns the value of selected data from <code>meta</code> attribute.
      *
      * @return selected value from the <code>meta</code> attribute.
      */
     public List getMetaNodes(String xPathExpression)
         throws HTMLException
     {
         // rewrite for cyklotron 2.13
        if("/meta/organisation".equals(xPathExpression))
         {
             xPathExpression = "/meta/organizations/organization";
             List nodes = (List)metaData.get(xPathExpression);
             if(nodes == null)
             {
                 Document metaDom = docRenderer.getMetaDom();
                 if(metaDom == null)
                 {
                     nodes = new ArrayList();
                 }
                 else
                 {
                     nodes = metaDom.selectNodes(xPathExpression);
                 }
                 metaData.put(xPathExpression, nodes);
             }
             if(nodes == null)
             {
                 nodes = Arrays.asList(nodes.get(0));
             }
             return nodes;
         }
         else
         {
             List nodes = (List)metaData.get(xPathExpression);
             if(nodes == null)
             {
                 Document metaDom = docRenderer.getMetaDom();
                 if(metaDom == null)
                 {
                     nodes = new ArrayList();
                 }
                 else
                 {
                     nodes = metaDom.selectNodes(xPathExpression);
                 }
                 metaData.put(xPathExpression, nodes);
             }
             return nodes;
         }
     }
 }
