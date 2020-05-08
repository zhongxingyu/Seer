 package niagara.utils.nitree;
 
 /*
  * Class that (theoretically) extends DOM - I am concerned that it will be lots
  * of grunge work and possibly not worth the effort to support the full DOM
  * interface.  NIDocument and the associated other classes which will form
  * the NIDOM interface will support copy on write
  *
  * Initial implementation of NIDocument and associated classes
  * will be based on DOM trees
  * NIDocument should be an interface - prototype as a class
  * and then convert to an interface (named NIDocument - rename the 
  * class) once interface is established
  */
 
 import org.w3c.dom.*;
 import com.ibm.xml.parser.*;
 import java.lang.ref.*;
 import java.util.*;
 import java.io.*;
 
 import niagara.utils.*;
 
 public class NIDocument extends NINode {
     
     /* 
      * Each NIDocument has an associated MapTable which maintains
      * associations between DOM Elements and NIElements
      * Map table will be implemented with those reference counting
      * stuff so that NIElements for DOM trees that have gone away
      * are deleted properly (have to remove reference to NIElement
      * in MapTable so that reference count for NIElement can go to 
      * zero).
      * MapTable variable must be set in constructor or initialization
      */
 
     private MapTable mapTable; 
     private Document domDoc;
 
     /*
      * permission flags (for copy on write) are kept in the NIDocument so it is
      * easy to set/unset all of a tree's permissions at one time
      * Instead of an array of Booleans for writeable - use an array of 
      * WeakReferences so that the Boolean writeable bits go away when
      * no NINode references them anymore
      */
     private ArrayList writeableWR;
     private boolean initialized;
 
     /* 
      * for printing ...
      */
     private PrintWriter pw;
 
     /**
      * Creates an unitialized NIDocument.  Must be initialized before
      * it is used.
      */
     public NIDocument() {
 	mapTable = null;
 	domDoc = null;
 	initialized = false;
 	writeableWR = new ArrayList();
 	pw = new PrintWriter(System.out);
         return;
     }
 
     /**
      * Initialize a NIDocument with appropriate variables.
      *
      * @param _mapTable  The map table to be associated with the document.
      * @param _domDoc    The DOM doc to be associated with this NIDocument.
      */
     public void initialize(MapTable _mapTable, Document _domDoc) {
 	mapTable = _mapTable;
 	domDoc = _domDoc;
	mapTable.insert(domDoc, this);
 	initialized = true;
 	return;
     }
 
     /**
      * Tells whether the NIDocument has been initialized or not.
      */
     public boolean isInitialized() { return initialized; }
 
     /**
      * resets all the writable bits to false, removing all cleared
      * references along the way TODO - this cleaning needs to be done
      * somewhere else and in a better fashion - this could leak too
      * much memory
      */
     public void globalSetWriteableFalse() {
 	int size = writeableWR.size();
 	for(int i = 0; i<size; i++) {
 	    BooleanHolder tmp = 
 		(BooleanHolder)((WeakReference)writeableWR.get(i)).get();
 	    if(tmp == null) {
 		/* this reference has been cleared (object referred to
 		 * is no longer in use), so take it out of the list
 		 */
 		writeableWR.remove(i);
 		size--;
 		i--;
 	    } else {
 		tmp.setValue(false);
 	    }
 	}
 	return;
     }
 
     /* 
      * returns the Dom Document associated with this NIDocument
      *
      */
     public Document getDomDoc() { return domDoc; }
 
     /*
      * Returns the NIElement associated with a given
      * DOM element - if no associated DOM element exists one is created.
      *
      * @param _domElement - The element for which we will find the
      *                       associated NIElement
      *
      * @return The NIElement associated with the _domeElement passed as
      *          a parameter
      */
     public NIElement getAssocNIElement(Element domElt) {
 	if(domElt == null) {
 	    return null;
 	}
 
 	/*
 	 * Check to see if this element is in the map table already
 	 * and if not add it to the table
 	 */
 	Object o = mapTable.lookup(domElt);
         if(o != null) {
             return (NIElement)o;
 	}
 
 	/* else no association found, must create a new NIElement */
 	return createNIElement(domElt);
     }
 
     /*
      * Returns the NIAttribute associated with a given
      * DOM attribute - if no associated DOM attribute exists one is created.
      *
      * @param domAttr - The attribute for which we will find the
      *                       associated NIAttribute
      *
      * @return The NIAttribute associated with the domAttr passed as
      *          a parameter
      */
 
     public NIAttribute getAssocNIAttr(Attr domAttr, NIElement parent) {
 	
 	/*
 	 * Check to see if this element is in the map table already
 	 * and if not add it to the table
 	 */
 
 	Object o = mapTable.lookup(domAttr);
 	if(o != null) {
              return (NIAttribute)o;
 	}
 
 	/* else have to create a new NIAttribute */
 	NIAttribute niAttr = new NIAttribute();
 	niAttr.initialize(domAttr, parent);
 
 	mapTable.insert(domAttr, niAttr);
 	
 	return niAttr;
     }
 
     /**
      * Returns the NIDocument associated with a given DOM document -
      * if no such NIDocument exists, will create one and return it.
      *
      * @param _domDoc   The dom doc for which we will return an associated 
      *                     NIDoc.
      * @param _mapTable The map table to search for an existing association
      *                   - note has to be a parameter since this is a 
      *                   static function.
      *
      * @return The NIDocument associated with the param <code> _domDoc <\code>
      */
 
     public static NIDocument getAssocNIDocument(Document _domDoc, 
 						MapTable _mapTable) {
	/* handle orphan elements */
	Object o = null;
	if(_domDoc == null) {
	    _domDoc = new TXDocument();
	} else {
	    o = _mapTable.lookup(_domDoc);
	    if(o != null) {
		return (NIDocument)o;
	    }
 	}
 
 	/* else have to create a new NIDocument */
 	NIDocument niDoc = new NIDocument();
        
         niDoc.initialize(_mapTable, _domDoc);
 	
 	return niDoc;
     }
 
     /* DOM Interface Functions */
 
     /**
      * Function defined in DOM.
      */
     public NIElement getDocumentElement() {
 	return getAssocNIElement(domDoc.getDocumentElement());
     }
 
     /**
      * Creates a new NIElement from scratch - also creates the DOM element
      * associated with the NIElement
      *
      * @param tagName The tagName of the new element.
      *
      * @return Returns the newly-created NIElement
      */
     public NIElement createNIElement(String tagName) {
 	Element domElt = domDoc.createElement(tagName);
 
 	/* creates and initializes a new NIElt and adds an 
 	 * association in the MapTable
 	 */
 	return createNIElement(domElt);
     }
 
     /**
      * Creates a new NIElement to be associated with a particular
      * dom element 
      *
      * @param domElement The dom element to be associated with 
      *        the new NIElement 
      *
      * @return Returns the newly-created NIElement
      */
     public NIElement createNIElement(Element domElement) {
 
 	/* creates and initializes a new NIElt and adds an 
 	 * association in the MapTable
 	 */
 	NIElement niElt = new NIElement();
 
 	/* initialize the element - we can update any elements
 	 * we create
 	 */
 	BooleanHolder tmp;
 	synchronized (this) {
 	    tmp = new BooleanHolder(true);
 	    writeableWR.add(new WeakReference(tmp));
 	}
 
 	niElt.initialize((TXElement)domElement, tmp, this);
 
 	/* and insert into the map table to make association */
 	mapTable.insert(domElement, niElt);
 
 	return niElt;
     }
 
     /* NOTE - this is important - we assume that a document is
      * always writeable, that it has been cloned appropriately
      * before any operator that can update it is allowed to work
      * on it
      */
     public void replaceChild(NIElement newChild, NIElement oldChild) 
 	throws NITreeException {
 
 	/* now do the update */
 	domDoc.replaceChild(newChild.getDomElement(), 
 			    oldChild.getDomElement());
 	return;
     }
 
 
     /* NOTE - this is important - we assume that a document is
      * always writeable, that it has been cloned appropriately
      * before any operator that can update it is allowed to work
      * on it
      */
     public void appendChild(NIElement child) 
 	throws NITreeException {
 
 	/* now do the update */
 	domDoc.appendChild(child.getDomElement());
 	return;
     }
 
     public String getNodeName() {
 	return domDoc.getNodeName();
     }
 
     public String myGetNodeValue() {
 	throw new PEException("myGetNodeValue not supported for NIDocument");
     }
 
     public void mySetNodeValue(String nodeValue) 
 	throws NITreeException {
 	throw new PEException("mySetNodeValue not supported for NIDocument");
     }
 
     /** Function to clone a document - based on DOM's cloneNode.
      * Copies all attributes, members, etc of the NIDocument and
      * clones the associated DOM document. Clone of DOM doc is
      * deep or not based on parameter
      *
      * @param deep Indicates if clone should copy children or not
      */
     public NIDocument cloneDocRefDocElt(boolean deep) {
 	/* for now I set all the writeable bits to false so that any
 	 * node in the cloned tree must be itself cloned - haven't figured
 	 * out yet, how to pass on write permissions 
 	 */
 	globalSetWriteableFalse();
 	
 	NIDocument cloneDoc = new NIDocument();
 	Document cloneDomDoc = (Document)domDoc.cloneNode(deep);
 	
 	cloneDoc.initialize(mapTable, cloneDomDoc); 
 
 	/* Set the document element of the cloned doc
 	 * to the doc element of the clonee
 	 */
	if(this.domDoc.getDocumentElement() == null) {
	    System.out.println("here doc elt is null!!");
	}
 	cloneDoc.domDoc.appendChild(this.domDoc.getDocumentElement());
 	
 	/* don't copy the writeable array to the cloned NIDoc, we have
 	 * set all writeable bits to false, so if something needs to be
 	 * updated, it will get cloned and it will get a new 
 	 * writeable reference (make sure this is true)
 	 * Also would be nice if elements in the writeable array went
 	 * away when they were no longer referenced so that writeable
 	 * arrays don't grow indefinitely
 	 * fine unless someone
 	 * goes to the writeable array in the old tree and sets all
 	 * the bits to true.
 	 * Have to think about this cloning stuff really well - this will
 	 * work for accumulate, but need to think about this more when
 	 * we do something more complicated and want to pass on write
 	 * permissions.  Ideally, don't clear writeable array
 	 */
 
 	return cloneDoc;
     }
 
     /** from TXDocument
      */
     public void printWithFormat() {
 	try {
 	    ((TXDocument)domDoc).printWithFormat(pw, null, 3);
 	} catch (IOException ioe) {
 	    System.out.println("Error printing accum doc" + ioe.getMessage());
 	} catch (LibraryException le) {
 	    System.out.println("Error printing accum doc" + le.getMessage());
 	}
     }
 }
