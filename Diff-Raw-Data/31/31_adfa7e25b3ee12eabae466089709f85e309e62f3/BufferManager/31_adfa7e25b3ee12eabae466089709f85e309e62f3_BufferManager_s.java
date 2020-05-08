 /**
 * $Id: BufferManager.java,v 1.11 2002/05/02 22:04:56 vpapad Exp $
  *
  * A read-only implementation of the DOM Level 2 interface,
  * using an array of SAX events as the underlying data store.
  *
  */
 
 package niagara.ndom.saxdom;
 
 /**
  *
  * BufferManager preallocates and maintains an array of pages of SAX events,
  * and manages their translation into DOM events.
  *
  */
 
 import java.util.Stack;
 import java.util.Vector;
 import java.util.ArrayList;
 
 import org.w3c.dom.*;
 
 import niagara.utils.PEException;
 
 import java.lang.ref.Reference;
 import java.lang.ref.WeakReference;
 import java.lang.ref.ReferenceQueue;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 public class BufferManager {
     private static Page[] pages;
 
     private static Stack free_pages;
 
     private static int page_size;
 
     private static Map pageRegistry;
 
     /** The garbage collector will enqueue garbage collected
      * documents here */
     private static ReferenceQueue usedDocuments;
 
     /** We will wait for <code>timeout</code> milliseconds
      * for free pages to appear before throwing an insufficient
      * memory exception */
     private static final int timeout = 250;
 
     /** The page reclaimer thread will wait for <code>reclaimerTimeout</code>
      *  milliseconds for newly freed documents to appear before it performs
      *  a batch of unpinnings */
     private static final int reclaimerTimeout = 25;
 
     /** If the usedDocuments queue is not empty, the page reclaimer 
      *  thread will unpin a batch of <code>unpinBatchSize</code> pages */
     private static final int unpinBatchSize = 100;
 
     // No public constructor, BufferManager is a singleton
     private BufferManager(int size, int page_size) {
         pages = new Page[size]; 
         free_pages = new Stack();
         pageRegistry = new HashMap(size);
         usedDocuments = new ReferenceQueue();
         
         for (int i = 0; i < size; i++) {
             pages[i] = new Page(page_size, i);
             free_pages.push(pages[i]);
         }
 
         this.page_size = page_size;
 
        (new PageReclaimer()).run();
     }
 
     public static void createBufferManager(int size, int page_size) {
         System.out.println("SAXDOM buffer manager initialized: " 
                            + size + " pages of " + page_size + " events.");
         
         new BufferManager(size, page_size);
     }
 
     public static void addFreePage(Page page) {
         free_pages.push(page);
     }
 
     private static final class PageRange {
         Page first, last;
         PageRange(Page first, Page last) {
             this.first = first;
             this.last = last;
         }
     }
 
     public static Reference registerFirstPage(DocumentImpl d, Page page) {
         Reference ref = new WeakReference(d, usedDocuments);
         synchronized(pages) {
             pageRegistry.put(ref, new PageRange(page, page));
         }
         return ref;
     }
 
     public static void registerLastPage(Reference ref, Page page) {
         synchronized(pages) {
         ((PageRange) pageRegistry.get(ref)).last = page;
         }
     }
 
     class PageReclaimer extends Thread {
         private int[] page_unpins = new int [pages.length];
 
         public void run() {
             while (true) {
                 int unpins = 0;
                 
                 while (unpins < unpinBatchSize) {
                     Reference r;
                     try {
                         r = usedDocuments.remove(reclaimerTimeout);
                     } catch (InterruptedException e) {
                         continue;
                     }
 
                     if (r == null) 
                         if (unpins > 0) break;
                         else continue;
 
                     PageRange pr;
                     synchronized(pages) {
                         pr = (PageRange) pageRegistry.remove(r);
                     }
                     if (pr == null) continue;
 
                     Page page = pr.first;
                     do {
                         unpins++;
                         page_unpins[page.getNumber()]++;
 
                         if (page == pr.last) break;
 
                         page = page.getNext();
                     } while (page != null);
                 }
 
                 // Perform a batch of unpins
                 boolean wasEmpty = free_pages.empty();
                 
                 for (int i = 0; unpins > 0 && i < page_unpins.length; i++) {
                     int pins = page_unpins[i];
                     if (pins == 0) continue;
                     pages[i].unpin(pins);
                     unpins -= pins;
                     page_unpins[i] = 0;
                 }
 
                 synchronized (free_pages) {
                     if (wasEmpty && !free_pages.empty())
                         free_pages.notify();
                 }                    
             }
         }
     }
 
     public static Page getFreePage() {
         if (free_pages.empty()) {
             System.gc();
             synchronized(free_pages) {
                 try {
                     free_pages.wait(timeout);
                 }
                 catch (InterruptedException e) {}
             }
         }
 
         if (free_pages.empty()) 
             // XXX vpapad: Run away! Run away!
             throw new InsufficientMemoryException();
 
         return (Page) free_pages.pop();
     }
     
 
     // Accessors and utility methods 
 
     public static Page getPage(int index) {
         return pages[index / page_size];
     }
 
     public static int getOffset(int index) {
         return index % page_size;
     }
 
     public static int getIndex(Page page, int offset) {
         return page.getNumber()*page_size + offset;
     }
 
     public static byte getEventType(int index) {
         return getPage(index).getEventType(getOffset(index));
     }
 
     public static String getEventString(int index) {
         return getPage(index).getEventString(getOffset(index));
     }
 
     // DOM methods 
 
     public static Element getFirstElementChild(DocumentImpl doc, int index) {
         Page page = getPage(index);
         int offset = getOffset(index);
 
         int pageSize = page.getSize();
 
         while (true) {
             if (offset == pageSize - 1) {
                 page = page.getNext();
                 offset = 0;
             }
             else
                 offset++;
         
             switch (page.getEventType(offset)) {
             case SAXEvent.ATTR_NAME:
             case SAXEvent.ATTR_VALUE:
             case SAXEvent.TEXT:
                 continue;
             case SAXEvent.END_ELEMENT:
             case SAXEvent.END_DOCUMENT:
                 return null;
             case SAXEvent.START_ELEMENT:
                 return (Element) makeNode(doc, getIndex(page, offset));
             default:
                 throw new PEException("Unexpected event type");
             }
         }
     }
 
     public static String getTagName(int index) {
         return getEventString(index);
     }
 
     public static String getName(int index) {
         return getEventString(index);
     }
 
     public static String getValue(int index) {
 	throw new PEException("Not Implemented Yet!");
     }
 
     public static Element getOwnerElement(int index) {
         throw new PEException("Not Implemented Yet!");
     }
 
     public static Node getParentNode(int index) {
         throw new PEException("Not Implemented Yet!");
     }
 
     public static String getAttributeValue(int index) {
 	Page page = getPage(index);
 	int offset = getOffset(index);
 	if (offset == page.getSize() -1) {
 	    page = page.getNext();
 	    offset = 0;
 	}
 	else 
 	    offset++;
 
 	if (page.getEventType(offset) != SAXEvent.ATTR_VALUE)
 	    throw new PEException("Could not find attribute value");
 	
 	return page.getEventString(offset);
     }
 
     public static String getAttribute(int elementIndex, String attrName) {
         Page page = getPage(elementIndex);
         int offset = getOffset(elementIndex);
 
         int pageSize = page.getSize();
 
         boolean found = false;
 
         loop: while (true) {
             if (offset == pageSize - 1) {
                 page = page.getNext();
                 offset = 0;
             }
             else
                 offset++;
         
             if (found) return page.getEventString(offset);
 
             switch (page.getEventType(offset)) {
             case SAXEvent.ATTR_NAME:
                 if (page.getEventString(offset).equals(attrName))
                     found = true;
                 continue loop;
             case SAXEvent.ATTR_VALUE:
                 continue loop;
             default:
                 break loop;
             }
         }
 
         return "";
     }
 
     public static boolean hasAttribute(int index, String name) {
         throw new PEException("Not Implemented Yet!");
     }
 
     public static Attr getAttributeNS(int index, String namespaceURI, 
                                       String localName) {
         throw new PEException("Not Implemented Yet!");
     }
 
     public static boolean hasAttributeNS(int index, String namespaceURI, 
                                       String localName) {
         throw new PEException("Not Implemented Yet!");
     }
 
     public static Attr getAttributeNodeNS(int index, String namespaceURI, 
                                       String localName) {
         throw new PEException("Not Implemented Yet!");
     }
 
     /**
      * Find the attribute of the element with index <code>elementIndex</code>
      * with name <code>name</code>
      * @return the corresponding Attr node, or null, if there is none
      */
     public static Attr getAttributeNode(DocumentImpl doc, 
                                         int elementIndex, String attrName) {
         Page page = getPage(elementIndex);
         int offset = getOffset(elementIndex);
 
         int pageSize = page.getSize();
 
         loop: while (true) {
             if (offset == pageSize - 1) {
                 page = page.getNext();
                 offset = 0;
             }
             else
                 offset++;
         
             switch (page.getEventType(offset)) {
             case SAXEvent.ATTR_NAME:
 		if (page.getEventString(offset).equals(attrName))
 		    return (Attr) makeNode(doc, getIndex(page, offset));
 		continue loop;
             case SAXEvent.ATTR_VALUE:
                 continue loop;
 	    default:
 		break loop;
             }
         }
 
 	return null;
     }
 
     /**
      * Find the <code>attrOffset</code>-th attribute of the element
      * with index <code>elementIndex</code>
      * @return the corresponding Attr node, or null, if there is none
      */
     static Node getAttributeByIndex(DocumentImpl doc, 
 				    int elementIndex, int attrOffset) {
 	int count = 0;
 
 	if (attrOffset < 0)
 	    return null;
 
         Page page = getPage(elementIndex);
         int offset = getOffset(elementIndex);
 
         int pageSize = page.getSize();
 
         loop: while (true) {
             if (offset == pageSize - 1) {
                 page = page.getNext();
                 offset = 0;
             }
             else
                 offset++;
         
             switch (page.getEventType(offset)) {
             case SAXEvent.ATTR_NAME:
 		if (count < attrOffset) {
 		    count++;
 		    continue loop;
 		}
 		else
 		    break loop;
             case SAXEvent.ATTR_VALUE:
                 continue loop;
 	    default:
 		break loop;
             }
         }
 
 	if (count == attrOffset)
 	    return makeNode(doc, getIndex(page, offset));
 	else
 	    return null;
     }
 
     /**
      * Find the number of attributes of the element with index 
      * <code>elementIndex</code>
      * @return the number of attributes
      */
     static int getNumberOfAttributes(DocumentImpl doc, int elementIndex) {
 	int count = 0;
 
         Page page = getPage(elementIndex);
         int offset = getOffset(elementIndex);
 
         int pageSize = page.getSize();
 
         loop: while (true) {
             if (offset == pageSize - 1) {
                 page = page.getNext();
                 offset = 0;
             }
             else
                 offset++;
         
             switch (page.getEventType(offset)) {
             case SAXEvent.ATTR_NAME:
 		count++;
 		continue loop;
             case SAXEvent.ATTR_VALUE:
                 continue loop;
 	    default:
 		break loop;
             }
         }
 
 	return count;
     }
 
     public static NodeList getChildNodes(DocumentImpl doc, int index) {
         ArrayList al = new ArrayList();
         int child = getFirstChildIndex(index);
         
         while (child >= 0) {
             al.add(makeNode(doc, child));
             child = getNextSiblingIndex(doc, child);
         }
 
         return new NodeListImpl(al);
     }
 
     public static Node makeNode(DocumentImpl doc, int index) {
         byte et = getEventType(index);
         switch (et) {
         case SAXEvent.START_ELEMENT:
             return new ElementImpl(doc, index);
         case SAXEvent.ATTR_NAME:
             return new AttrImpl(doc, index);
         case SAXEvent.TEXT:
             return new TextImpl(doc, index);
         default:
             throw new PEException("makeNode() can't handle this event type: "
                 + et);
         }
         
     }
 
     public static boolean hasChildNodes(int index) {
         throw new PEException("Not Implemented Yet!");
     }
 
     public static Node getFirstChild(DocumentImpl doc, int index) {
         int firstChildIndex = getFirstChildIndex(index);
         if (firstChildIndex < 0)
             return null;
         return makeNode(doc, firstChildIndex);
     }
 
     public static int getFirstChildIndex(int index) {
         Page page = getPage(index);
         int offset = getOffset(index);
 
         int pageSize = page.getSize();
 
         while (true) {
             if (offset == pageSize - 1) {
                 page = page.getNext();
                 offset = 0;
             }
             else
                 offset++;
         
             switch (page.getEventType(offset)) {
             case SAXEvent.ATTR_NAME:
             case SAXEvent.ATTR_VALUE:
                 continue;
             case SAXEvent.END_ELEMENT:
             case SAXEvent.END_DOCUMENT:
                 return -1;
             case SAXEvent.START_ELEMENT:
             case SAXEvent.TEXT:
                 return getIndex(page, offset);
             default:
                 throw new PEException("Unexpected event type");
             }
         }
     }
 
     public static Node getLastChild(DocumentImpl doc, int index) {
         int lastChildIndex = getLastChildIndex(index);
         if (lastChildIndex < 0)
             return null;
         return makeNode(doc, lastChildIndex);
     }
 
     public static int getLastChildIndex(int index) {
         Page page = getPage(index);
         int offset = getOffset(index);
 
         int pageSize = page.getSize();
 
         while (true) {
             if (offset == pageSize - 1) {
                 page = page.getNext();
                 offset = 0;
             }
             else
                 offset++;
         
             switch (page.getEventType(offset)) {
             case SAXEvent.ATTR_NAME:
             case SAXEvent.ATTR_VALUE:
                 continue;
             case SAXEvent.END_ELEMENT:
                 return -1;
             case SAXEvent.START_ELEMENT:
             case SAXEvent.TEXT:
                 int nextSiblingIndex = page.getNextSibling(offset);
                 while (nextSiblingIndex >= 0) {
                     page = getPage(nextSiblingIndex);
                     offset = getOffset(nextSiblingIndex);
                     nextSiblingIndex = page.getNextSibling(offset);
                 } 
                 return getIndex(page, offset);
             default:
                 throw new PEException("Unexpected event type");
             }
         }
     }
 
     public static Node getNextSibling(DocumentImpl doc, int index) {
         int sibling = getPage(index).getNextSibling(getOffset(index));
         if (sibling == -1) return null;
         else return makeNode(doc, sibling);
     }
 
     public static int getNextSiblingIndex(DocumentImpl doc, int index) {
         return getPage(index).getNextSibling(getOffset(index));
     }
 
     public static NamedNodeMap getAttributes(DocumentImpl doc, int index) {
         return new NamedNodeMapImpl(doc, index);
     }
 
     public static boolean hasAttributes(int index) {
        throw new PEException("Not Implemented Yet!");
     }
 
     public static Node getPreviousSibling(DocumentImpl doc, int index) {
         int previousSiblingIndex = getPreviousSiblingIndex(index);
         if (previousSiblingIndex < 0)
             return null;
         
         return makeNode(doc, previousSiblingIndex);
     }
     
     public static int getPreviousSiblingIndex(int index) {
         Page page = getPage(index);
         int offset = getOffset(index);
         
         if (offset == 0) {
             page = page.getPrevious();
             offset = page.getSize() - 1;
         }
         else
             offset--;
         
         switch (page.getEventType(offset)) {
         case SAXEvent.START_DOCUMENT:
         case SAXEvent.START_ELEMENT:
         case SAXEvent.ATTR_NAME:
         case SAXEvent.ATTR_VALUE:
             return -1;
         case SAXEvent.END_ELEMENT:
             return page.getNextSibling(offset);
         case SAXEvent.TEXT:
             return getIndex(page, offset);
         default:
             throw 
                 new PEException("Unexpected event type in getPreviousSibling");
         }
     }
 
     public static String getData(int index) {
         return getEventString(index);
     }
 
     public static NodeList getElementsByTagName(int index, String tagname) {
         throw new PEException("Not Implemented Yet!");
     }
 
     public static NodeList getElementsByTagNameNS(int index, 
                                                   String namespaceURI, 
                                                   String localName) {
         throw new PEException("Not Implemented Yet!");
     }
 
     public static String getNamespaceURI(int index) {
         throw new PEException("Not Implemented Yet!");
     }
 
     public static String getPrefix(int index) {
         throw new PEException("Not Implemented Yet!");
     }
 
     public static String getLocalName(int index) {
         throw new PEException("Not Implemented Yet!");
     }
 
     public static String getNotationName(int index) {
        throw new PEException("Not Implemented Yet!");
     }
 
     public static String getPublicId(int index) {
         throw new PEException("Not Implemented Yet!");
     }
 
     public static String getSystemId(int index) {
         throw new PEException("Not Implemented Yet!");
     }
 
     public static String getTarget(int index) {
         throw new PEException("Not Implemented Yet!");
     }
 }
 
 
