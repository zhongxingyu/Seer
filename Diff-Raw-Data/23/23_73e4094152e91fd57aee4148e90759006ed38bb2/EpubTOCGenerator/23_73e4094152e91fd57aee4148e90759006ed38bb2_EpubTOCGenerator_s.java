 package name.vysoky.epub;
 
 import nl.siegmann.epublib.domain.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Table of content generator.
  * @author Jiri Vysoky
  */
 public class EpubTOCGenerator {
 
     final Logger logger = LoggerFactory.getLogger(EpubTOCGenerator.class);
 
     private TableOfContents toc = new TableOfContents();
     private HeadingElementBuffer buffer = new HeadingElementBuffer();
 
     private EpubTool epubTool;
 
     public EpubTOCGenerator(EpubTool epubTool, int minLevel, int maxLevel) {
         this.epubTool = epubTool;
         this.buffer.setMinLevel(minLevel);
         this.buffer.setMaxLevel(maxLevel);
     }
 
     public void generate() {
         Book book = epubTool.getBook();
         Spine spine = book.getSpine();
         for (SpineReference spineReference : spine.getSpineReferences()) {
             Resource resource = spineReference.getResource();
             Document document = processPart(resource);
            epubTool.writeDocument(resource, document);
         }
         for (HeadingElement headingElement : buffer.getProcessed()) {
             TOCReference reference = generateToc(headingElement);
             toc.addTOCReference(reference);
         }
         book.setTableOfContents(toc);
         epubTool.writeTableOfContents(book);
     }
 
     Document processPart(Resource resource) {
         try {
             Document document = epubTool.readDocument(resource);
             findHeadings(resource, epubTool.getBodyElement(document));
             return document;
         } catch (Exception e) {
             logger.error("Unable to process part!", e);
             return null;
         }
     }
 
     private void findHeadings(Resource resource, Element element) {
         if (HeadingElement.isHeadingElement(element)) buffer.addElement(resource, element);
         // recursion
         NodeList children = element.getChildNodes();
         for (int i = 0; i < children.getLength(); i++) {
             Node node = children.item(i);
             if (node.getNodeType() == Node.ELEMENT_NODE) {
                 Element child = (Element) node;
                 findHeadings(resource, child);
             }
         }
     }
 
     private TOCReference generateToc(HeadingElement headingElement) {
         Element element = headingElement.getElement();
         Resource resource = headingElement.getResource();
         String name = element.getTextContent();
         String fragmentId = element.getAttribute("id");
         List<TOCReference> children = new ArrayList<TOCReference>(headingElement.getChildren().size());
         for (HeadingElement child : headingElement.getChildren()) children.add(generateToc(child));
         return new TOCReference(name, resource, fragmentId, children);
     }
 }
