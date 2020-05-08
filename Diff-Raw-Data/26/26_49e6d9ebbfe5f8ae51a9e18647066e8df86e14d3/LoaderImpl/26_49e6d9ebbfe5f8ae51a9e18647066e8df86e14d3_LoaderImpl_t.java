 package org.xbrlapi.loader;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.EmptyStackException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 import java.util.Vector;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.apache.log4j.Logger;
 import org.xbrlapi.Fragment;
 import org.xbrlapi.cache.CacheImpl;
 import org.xbrlapi.data.Store;
 import org.xbrlapi.sax.ContentHandlerImpl;
 import org.xbrlapi.sax.EntityResolverImpl;
 import org.xbrlapi.utilities.Constants;
 import org.xbrlapi.utilities.XBRLException;
 import org.xbrlapi.xlink.ElementState;
 import org.xbrlapi.xlink.XLinkProcessor;
 import org.xbrlapi.xpointer.resolver.PointerResolver;
 import org.xbrlapi.xpointer.resolver.PointerResolverImpl;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.EntityResolver;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXNotRecognizedException;
 import org.xml.sax.SAXNotSupportedException;
 import org.xml.sax.XMLReader;
 
 /**
  * Implementation of the XBRL API Loader interface. The loader is responsible
  * for managing the DTS discovery process. It manages:
  * 1. the queue of documents to be explored
  * 2. the document discovery process
  * 3. the stack of fragments being built and stored
  * 4. The fragment naming scheme
  * 
  * Features of the loader include:
  * 1. The ability to interupt the document discovery process
  * between documents without losing track of the documents
  * remaining to be discovered.  Interrupts can be requested and 
  * cancelled.
  * 
  * @author Geoffrey Shuetrim (geoff@galexy.net)
  */
 public class LoaderImpl implements Loader {
 
     // Create the logger
     static Logger logger = Logger.getLogger(Loader.class);
 
     /**
      * The data store to be used to hold the DTS.
      */
     private Store store;
 
     /**
      * The stack of fragments that are being built
      */
     private Stack<Fragment> fragments = new Stack<Fragment>();
 
     /**
      * A stack of element states, one per root element
      * of a fragment currently undergoing construction.
      */
     Stack<ElementState> states = new Stack<ElementState>();
     
     /**
      * @return the stack of element states for the root
      * elements of the fragments currently being built.
      */
     private Stack<ElementState> getStates() {
         return this.states;
     }    
     
     /**
      * The cache to use when discovering XML materials specified as a String
      * rather than just via a URL that resolves to the required XML.
      */
     private CacheImpl cache = null;
 
     /**
      * @see org.xbrlapi.loader.Loader#setCache(CacheImpl)
      */
     public void setCache(CacheImpl cache) {
         this.cache = cache;
     }
 
     /**
      * @see org.xbrlapi.loader.Loader#getCache()
      */
     public CacheImpl getCache() throws XBRLException {
         if (this.cache == null)
             throw new XBRLException(
                     "The loader cache is null and so cannot be used.");
         return this.cache;
     }
 
     /**
      * The absolute URL of the document currently being parsed. Used to record
      * this metadata in each fragment.
      */
     private String documentURL = null;
 
     /**
      * The document Id (including the document hash and its counter)
      */
     private String documentId = null;
 
     /**
      * Stack of vectors used to track children.
      */
     private Stack<Vector<Long>> childrenStack = new Stack<Vector<Long>>();
 
     /**
      * The Xlink processor
      */
     private XLinkProcessor xlinkProcessor;
 
     /**
      * The entity resolver to use for resolution of entities (URLs etc) during
      * the loading/discovery process.  Defaults to one without a caching system.
      */
     private EntityResolver entityResolver = new EntityResolverImpl();
 
     /**
      * The XPointer resolver
      */
     private PointerResolver pointerResolver;
 
     /**
      * The map of documents awaiting loading into the DTS. This queue is
      * implemented as a hash map to ensure that the keys (URLS in string form)
      * eliminate multiple discoveries of the same document. The value for each
      * URL is set to false initially and is changed to true when the document
      * has been loaded.
      */
     private HashMap<String, Integer> documentQueue = new HashMap<String, Integer>();
 
     /**
      * The unique fragment ID, that will be one for the first fragment. This is
      * incremented just before as it is retrieved for use with a new fragment
      * created during the loading process.
      */
     private int fragmentId = 0;
 
     /**
      * discovering equals false if the loader is not presently doing document
      * discovery and true otherwise.
      */
     private boolean discovering = false;
 
     private void setDiscovering(boolean value) {
         if (value) logger.debug(Thread.currentThread().getName() + " starting discovery.");
         else logger.debug(Thread.currentThread().getName() + " stopping discovery.");
         discovering = value;
     }
     
     /**
      * @see Loader#isDiscovering()
      */
     public boolean isDiscovering() {
         return discovering;
     }
 
     private boolean interrupt = false;
 
     /**
      * Interrupts the loading process once the current 
      * document discovery has been completed.
      * This can be useful when the loader is shared among
      * several threads.
      */
     public void requestInterrupt() {
         logger.info("An interrupt has been requested.");
         interrupt = true;
     }
     
     /**
      * @return true if an interrupt to the loading process 
      * has been requested and false otherwise.
      */
     private boolean interruptRequested() {
         return interrupt;
     }
 
     /**
      * @see org.xbrlapi.loader.Loader#cancelInterrupt()
      */
     public void cancelInterrupt() {
         interrupt = false;
         logger.info("Cancelled the discovery interrupt.");
     }
     
     /**
      * @param store The data store to hold the DTS
      * @param xlinkProcessor The XLink processor to use for link resolution
      * @throws XBRLException if the loader cannot be instantiated.
      */
     public LoaderImpl(Store store, XLinkProcessor xlinkProcessor)
             throws XBRLException {
         super();
         setStore(store);
         setXlinkProcessor(xlinkProcessor);
         this.setPointerResolver(new PointerResolverImpl(getStore()));
     }
 
     /**
      * @param store The data store to hold the DTS
      * @param xlinkProcessor The XLink processor to use for link resolution
      * @param urls The array of URLs for loading.
      * @throws XBRLException if the loader cannot be instantiated.
      */
     public LoaderImpl(Store store, XLinkProcessor xlinkProcessor, List<URL> urls)
             throws XBRLException {
         this(store, xlinkProcessor);
         setStartingURLs(urls);
     }
 
 
 
     /**
      * Set the data store to be used by the loader.
      * @throws XBRLException if the given store is null.
      */
     private void setStore(Store store) throws XBRLException {
         if (store == null) {
             throw new XBRLException("The data store must not be null.");
         }
         this.store = store;
     }
 
     /**
      * Get the data store used by a loader.
      */
     public Store getStore() {
         return store;
     }
 
     /**
      * Set the URL of the document now being parsed.
      * 
      * @param url
      *            The URL of the document now being parsed.
      * @throws XBRLException.
      */
     private void setDocumentURL(String url) throws XBRLException {
         this.documentURL = url;
         this.documentId = getStore().getDocumentId(url.toString());
     }
     
     /**
      * @return the document ID for the document being analysed or
      * null if no document is being analysed.
      */
     private String getDocumentId() {
         return documentId;
     }
 
     /**
      * Get the URL for the document being parsed.
      * 
      * @return The original (non-cache) URL of the document being parsed.
      */
     public String getDocumentURL() {
         return this.documentURL;
     }
 
     /**
      * Set the XLink processor used by the loader.
      * @throws XBRLException if the given XLink processor is null.
      */
     private void setXlinkProcessor(XLinkProcessor xlinkProcessor) throws XBRLException {
         if (xlinkProcessor == null) {
             throw new XBRLException("The XLink processor must not be null.");
         }
         this.xlinkProcessor = xlinkProcessor;
     }
 
     /**
      * Get the Xlink processor used by the loader. TODO Should the getter for
      * the loader Xlink Processor be public?
      */
     public XLinkProcessor getXlinkProcessor() {
         return xlinkProcessor;
     }
 
     /**
      * Set the XPointer resolver.
      * 
      * @param pointerResolver
      *            The XPointer resolver implementation.
      */
     private void setPointerResolver(PointerResolver pointerResolver) {
         this.pointerResolver = pointerResolver;
     }
 
     /**
      * Get the XPointer resolver.
      * 
      * @return The XPointer resolver used by the loader.
      */
     private PointerResolver getPointerResolver() {
         return pointerResolver;
     }
 
     /**
      * Add a new child tracking vector to the childrenStack to use for the new
      * fragment that is being built by the loader. Initialise it with a single
      * node containing the value of zero to signify that the root element of the
      * new fragment has not had any child elements found for it yet.
      * 
      * @throws XBRLException
      */
     public void prepareToTrackChildrenForNewFragment() throws XBRLException {
         Vector<Long> v = new Vector<Long>();
         v.add(new Long(0));
         childrenStack.add(v);
     }
 
     /**
      * The children vector contains an item for each element that has been started
      * and that has not yet been ended by the SAX content handler.
      * 
      * @return The vector of children or null if none exist (implying no parent fragments).
      */
     public Vector<Long> getChildrenVector() {
         if (childrenStack.isEmpty())
             return null;
         Vector<Long> children = childrenStack.peek();
         return children;
     }
 
     /**
      * @see Loader#incrementChildren()
      */
     public void incrementChildren() {
 
         // Do nothing if the children stack is empty - we are at the root of a
         // document
         if (this.childrenStack.isEmpty()) {
             return;
         }
 
         // record a new child for the parent element
         long c = (getChildrenVector().lastElement()).longValue() + 1;
         getChildrenVector().setElementAt(new Long(c),
                 getChildrenVector().size() - 1);
     }
 
     /**
      * Add a new node to the vector of children being tracked for the current
      * fragment. Initialise its value to zero to capture the fact that no
      * children have been found for the newly processed element - as yet.
      * 
      * @throws XBRLException
      */
     public void extendChildren() throws XBRLException {
         getChildrenVector().add(new Long(0));
     }
 
     /**
      * Remove the last element in the children vector. 
      * This is done when we are finished with processing 
      * an element or fragment.
      * 
      * @throws XBRLException
      */
     private void reduceChildren() throws XBRLException {
         if (getChildrenVector().size() > 0)
             getChildrenVector().removeElementAt(getChildrenVector().size() - 1);
         else
             throw new XBRLException("The element being completed has a corrupted child count.");
     }
 
     /**
      * @see org.xbrlapi.loader.Loader#updateState(long)
      */
     public void updateState(ElementState state) throws XBRLException {
 
         if (getStates().peek() == state) {
             this.removeFragment();
         } else {
             this.reduceChildren();
         }
     }
 
     /**
      * @see org.xbrlapi.loader.Loader#getFragment()
      */
     public Fragment getFragment() throws XBRLException {
         if (fragments.isEmpty()) throw new XBRLException("No fragments are available to be retrieved.");
         return fragments.peek();
     }
     /**
      * @see org.xbrlapi.loader.Loader#isBuildingAFragment()
      */
     public boolean isBuildingAFragment() {
         return (!fragments.isEmpty());
     }
 
     /**
      * @see org.xbrlapi.loader.Loader#addFragment(Fragment, ElementState)
      */
     public void addFragment(Fragment fragment, ElementState state)
             throws XBRLException {
 
         // Get the XPointer expressions that identify the root of this fragment
         // TODO Should the following xpointer code be contingent on children != null?
         Vector<String> pointers = state.getElementSchemePointers();
         for (String pointer : pointers) {
             fragment.appendElementSchemeXPointer(pointer);
         }
 
         // Set the document reconstruction metadata for the fragment
         Vector<Long> children = getChildrenVector();
         if (children != null) { 
             Fragment parent = getFragment();
             if (parent == null) throw new XBRLException("The parent fragment is missing.");
             String parentIndex = parent.getFragmentIndex();
             if (parentIndex == null) throw new XBRLException("The parent index is null.");
             fragment.setParentIndex(parentIndex);
             fragment.setSequenceToParentElement(children);
             fragment.setPrecedingSiblings(children);
 
         } else { // We have a document root fragment.
             fragment.setParentIndex("none");
         }
         fragment.setURL(getDocumentURL());
 
         // Push the fragment onto the stack of fragments
         fragments.add(fragment);
 
         // Push the element state onto the stack of fragment root element states
         getStates().add(state);
 
         // Push a new child count vector onto the stack of child count vectors
         prepareToTrackChildrenForNewFragment();
 
     }
 
     /**
      * Remove a fragment from the stack of fragments that 
      * are being built by the loader.
      * @throws XBRLException if their are no fragments being built.
      */
     private Fragment removeFragment() throws XBRLException {
         try {
             
             getStates().pop();
             childrenStack.pop();
             Fragment f = fragments.pop();
             getStore().storeFragment(f);
             return f;
         } catch (EmptyStackException e) {
             throw new XBRLException(
                     "There are no fragments being built.  The stack of fragments is empty.",
                     e);
         }
     }
 
     /**
      * @see org.xbrlapi.loader.Loader#discover(List)
      */
     public void discover(List<URL> startingURLs) throws XBRLException {
 
         for (int i = 0; i < startingURLs.size(); i++) {
             Object object = startingURLs.get(i);
             // TODO Eliminate this check of URL object type now that generics
             // are being used.
             if (object instanceof java.net.URL)
                 stashURL((URL) object);
             else
                 throw new XBRLException(
                         "Loader discovery must be passed a list of java.net.URL objects.");
         }
 
         discover();
     }
 
     /**
      * @see org.xbrlapi.loader.Loader#discover(URL)
      */
     public void discover(URL url) throws XBRLException {
         stashURL(url);
         discover();
     }
 
     /**
      * @see org.xbrlapi.loader.Loader#discover(String)
      */
     public void discover(String url) throws XBRLException {
         try {
             discover(new URL(url));
         } catch (MalformedURLException e) {
             throw new XBRLException("The URL to discover, " + url + " is malformed.", e);
         }
     }
 
     /**
      * @see org.xbrlapi.loader.Loader#getDocumentsStillToAnalyse()
      */
     public List<String> getDocumentsStillToAnalyse() {
         List<String> documents = new LinkedList<String>();
 
         for (String document : documentQueue.keySet()) {
             if ((documentQueue.get(document)).equals(new Integer(0))) {
                 documents.add(document);
             }
         }
 
         return documents;
     }
 
     /**
      * @see org.xbrlapi.loader.Loader#discover()
      */
     public void discover() throws XBRLException {
 
        if (isDiscovering()) {
            logger.debug("The loader is already doing discovery.");
            return;
        }
         setDiscovering(true);
 
         for (URL url: getStore().getDocumentsToDiscover()) {
             this.stashURL(url);
         }
        
        Object[] urls = this.documentQueue.keySet().toArray();
        logger.debug(urls.length + " documents queued for discovery.");
        for (int i=0; i<urls.length;i++){
            logger.debug(urls[i]);
        }
        
         URL url = getNextDocumentToExplore();
        logger.debug("Next is " + url);
         while (url != null) {
             if (!getStore().hasDocument(url.toString())) {
                 setDocumentURL(url.toString());
                 this.setNextFragmentId("1");
                 double startTime = System.currentTimeMillis();
                 int startIndex = this.fragmentId;
                 parse(url);
                 String time = (new Double(
                         (System.currentTimeMillis() - startTime)
                                 / (fragmentId - startIndex))).toString();
                 if (time.length() > 4) time = time.substring(0, 4);
                 logger.info("Average time taken per fragment = " + time + " milliseconds");
                 logger.info(this.fragmentId + " fragments in " + url);
             } else {
                 logger.info(url + " is already in the data store.");
             }
 
             if (interruptRequested()) {
                 cancelInterrupt();
                 break;
             }
 
             url = getNextDocumentToExplore();
         }
 
         setDiscovering(false);
        
        this.documentQueue = new HashMap<String,Integer>();
 
     }
 
     /**
      * @see org.xbrlapi.loader.Loader#discoverNext()
      */
     public void discoverNext() throws XBRLException {
 
         Store store = this.getStore();
         
         if (isDiscovering()) return;
         setDiscovering(true);
 
         URL url = getNextDocumentToExplore();
         while (store.hasDocument(url.toString()) && (url != null)) {
             url = getNextDocumentToExplore();
         }
 
         if (url != null) {
             logger.info("Up to fragment " + this.fragmentId + ". Now parsing " + url);
             setDocumentURL(url.toString());
             this.setNextFragmentId("1");
             parse(url);
         }
 
         setDiscovering(false);
 
     }
 
     /**
      * Perform a discovery starting with an XML document that is represented as
      * a string.
      * 
      * @param url
      *            The URL to be used for the document that is supplied as a
      *            string. This URL MUST be an absolute URL.
      * @param xml
      *            The string representation of the XML document to be parsed.
      * @throws XBRLException
      *             if the discovery process fails or if the supplied URL is not
      *             absolute or is not a valid URI syntax or the loader does not
      *             have a cache.
      */
     public void discover(URL url, String xml) throws XBRLException {
 
         logger.debug("Discovering a resource supplied as a string and with URL: " + url);
 
         try {
             if (!url.toURI().isAbsolute())
                 throw new XBRLException("The URL " + url + " must be absolute.");
         } catch (URISyntaxException e) {
             throw new XBRLException("The URL " + url
                     + " must be a valid URI syntax.", e);
         }
 
         // Copy the XML to the local cache even if it is there already (possibly over-writing existing documents)
         this.getCache().copyToCache(url, xml);
 
         try {
             this.stashURL(new URL(
                     "http://www.xbrlapi.org/xbrl/xbrl-2.1-roles.xsd"));
         } catch (MalformedURLException e) {
             throw new XBRLException(
                     "The standard roles URL could not be formed for discovery.",
                     e);
         }
         
         discover(url);
     }
 
     /**
      * Perform a discovery starting with an XML document that is represented as
      * an input source.
      * 
      * @param url
      *            The URL to be used for the document that is supplied as a
      *            string.
      * @param inputSource
      *            The InputSource representation of the XML document to be
      *            parsed.
      * @throws XBRLException
      *             if the discovery process fails.
      */
     /*
      * public void discover(URL url, InputSource inputSource) throws
      * XBRLException {
      * 
      * this.setNextFragmentId(getStore().getNextFragmentId());
      * 
      * while (url != null) { if (! getStore().hasDocument(url.toString())) {
      * setDocumentURL(url.toString()); parse(url, inputSource); } url =
      * getNextDocumentToExplore(); }
      * 
      * getStore().storeLoaderState(this.getCurrentFragmentId(),this.getDocumentsStillToAnalyse());
      *  }
      */
 
     /**
      * Retrieve URI of the next document to parse from the list of starting
      * point URLs provided or URLs found during the discovery process.
      * 
      * @throws XBRLException
      * @return the URI of the next document to explore or null if there are
      *         none.
      */
     private URL getNextDocumentToExplore() throws XBRLException {
         try {
             for (String key : documentQueue.keySet()) {
                 if ((documentQueue.get(key)).equals(new Integer(0))) {
                     documentQueue.put(key, new Integer(1));
                     URL url = new URL(key);
                     return url;
                 }
             }
             return null;
         } catch (MalformedURLException e) {
             throw new XBRLException(
                     "The URL syntax for the next DTS document is malformed.", e);
         }
     }
 
     /**
      * Parse an XML Document supplied as a URL the next part of the DTS.
      * @param url The URL of the document to parse.
      * @throws XBRLException
      */
     protected void parse(URL url) throws XBRLException {
         try {
             InputSource inputSource = this.getEntityResolver().resolveEntity("", url.toString());
             ContentHandler contentHandler = new ContentHandlerImpl(this, url);
             parse(url, inputSource, contentHandler);
         } catch (SAXException e) {
             throw new XBRLException("SAX exception thrown when parsing " + url,e);
         } catch (IOException e) {
             throw new XBRLException("IO exception thrown when parsing " + url,e);
         }
     }
 
     /**
      * Parse an XML Document supplied as a string the next part of the DTS.
      * @param url The URL to associate with the supplied XML.
      * @param xml The XML document as a string.
      * @throws XBRLException
      */
     protected void parse(URL url, String xml) throws XBRLException {
         InputSource inputSource = new InputSource(new StringReader(xml));
         ContentHandler contentHandler = new ContentHandlerImpl(this, url, xml);
         parse(url, inputSource, contentHandler);
     }
 
     /**
      * Parse the supplied input source.
      * @param url The URL to be associated with the supplied input source.
      * @param inputSource The input source to parse.
      * @param contentHandler The content handler to use for SAX parsing.
      * @throws XBRLException
      */
     protected void parse(URL url, InputSource inputSource, ContentHandler contentHandler) throws XBRLException {
 
         // Create and configure the SAX parser factory
         SAXParserFactory factory = SAXParserFactory.newInstance();
         factory.setValidating(false); // Standard XML validation - schema
                                         // validation used instead.
         factory.setNamespaceAware(true);
 
         /*
          * // Turn on XML Schema validation
          * factory.setFeature("http://xml.org/sax/features/validation",true);
          * factory.setFeature("http://apache.org/xml/features/validation/schema",true);
          * factory.setFeature("http://apache.org/xml/features/validation/schema-full-checking",false);
          */
 
         // Create the SAX parser to use
         SAXParser parser = null;
         try {
             parser = factory.newSAXParser();
         } catch (ParserConfigurationException e) {
             throw new XBRLException("SAX Parser could not be created.", e);
         } catch (SAXException e) {
             throw new XBRLException(
                     "Exception thrown setting up the SAX Parser.", e);
         }
 
         XMLReader reader = null;
         try {
             reader = parser.getXMLReader();
         } catch (SAXException e) {
             throw new XBRLException(
                     "Exception thrown setting up the SAX reader.", e);
         }
 
         reader.setEntityResolver(this.entityResolver);
         reader.setContentHandler(contentHandler);
         reader.setErrorHandler((ErrorHandler) contentHandler);
 
         try {
             reader.setFeature("http://xml.org/sax/features/namespace-prefixes",
                     true);
         } catch (SAXNotRecognizedException e) {
             throw new XBRLException(
                     "The sax parser does not recognise a lexical handler or a declaration handler.",
                     e);
         } catch (SAXNotSupportedException e) {
             throw new XBRLException(
                     "The sax parser does not support a lexical handler or a declaration handler.",
                     e);
         }
 
         try {
             reader.setProperty(Constants.JAXP_SCHEMA_LANGUAGE,
                     Constants.W3C_XML_SCHEMA);
         } catch (SAXNotSupportedException e) {
             throw new XBRLException(
                     "The SAX parser does not support XML Schema validation.", e);
         } catch (SAXNotRecognizedException e) {
             throw new XBRLException(
                     "The SAX parser does not recognise the XML Schema validation property.",
                     e);
         }
 
         try {
             logger.debug("Parsing " + url);
             reader.parse(inputSource);
         } catch (SAXException e) {
             e.printStackTrace();
             throw new XBRLException("SAX exception thrown when parsing " + url,e);
         } catch (IOException e) {
             throw new XBRLException("IO exception thrown when parsing " + url,e);
         }
 
         // Remove any document stub from the data store once parsing is complete.
         getStore().removeStub(documentId);
         
     }
 
     /**
      * Set the starting points for DTSImpl discovery using a linked list
      * 
      * @param urls
      *            A list of starting point document URLs for DTSImpl discovery
      * @throws XBRLException
      */
     protected void setStartingURLs(List<URL> urls) throws XBRLException {
         if (urls == null)
             throw new XBRLException("Null list of urls is not permitted.");
 
         for (int i = 0; i < urls.size(); i++) {
             stashURL(urls.get(i));
         }
     }
 
     /**
      * Stash a URL to await loading into DTS.
      * 
      * @param url
      *            The absolute URL to be stashed (any relative URL gets resolved
      *            against the Base URL before stashing. TODO put this
      *            functionality at the SAX parse call for the document. TODO
      *            make sure that the fragment after the # is handled for stashed
      *            URLs in the loader.
      * @throws XBRLException
      *             if the URL cannot be stored for later exploration or if the
      *             URL is not absolute
      */
     public synchronized void stashURL(URL url) throws XBRLException {
 
         // Make sure that the URL is a valid URI and is absolute
         try {
             if (!new URI(url.toString()).isAbsolute()) {
                 logger.warn("Failed to stash " + url);
                 throw new XBRLException("The URL: " + url + " needs to be resolved against a base URL prior to stashing.");                
             }
                 
         } catch (URISyntaxException e) {
             throw new XBRLException("The URL: " + url + " is not a valid URI.",e);
         }
 
         URL dereferencedURL = null;
         try {
             dereferencedURL = new URL(url.getProtocol(), url.getHost(), url
                     .getPort(), url.getPath());
         } catch (MalformedURLException e) {
             throw new XBRLException(
                     "Malformed URL found in DTS discovery process: " + url, e);
         }
 
         // Stash the URL if it has not already been stashed
         if (!documentQueue.containsKey(dereferencedURL.toString())) {
 
             // Only stash if the document does not already have a match.
             URL matchURL = getStore().getMatcher().getMatch(dereferencedURL);
             if (matchURL.equals(dereferencedURL)) {
                 documentQueue.put(dereferencedURL.toString(), new Integer(0));
             } else {
                 logger.debug("No need to stash " + dereferencedURL + " because it has match " + matchURL);
             }
             
         }
 
     }
 
     /**
      * Set the resolver for the resolution of entities found during the loading
      * and XLink processing
      * 
      * @param resolver
      *            An entity resolver implementation
      */
     public void setEntityResolver(EntityResolver resolver) {
         this.entityResolver = resolver;
     }
 
     public String getNextFragmentId() throws XBRLException {
         String id = getCurrentFragmentId();
         incrementFragmentId();
         return id;
     }
 
     public String getCurrentFragmentId() {
         return getDocumentId() + "_" + (new Integer(fragmentId)).toString();
     }
 
     public void incrementFragmentId() {
         fragmentId = fragmentId + 1;
     }
 
     /**
      * Used to set the next fragment id using the information in the data store.
      * This is useful when coming back to an existing data store to add
      * additional documents.
      * 
      * @param id
      * @throws XBRLException
      */
     private void setNextFragmentId(String id) {
         fragmentId = (new Integer(id)).intValue();
     }
 
     /**
      * Return the entity resolver being used by the loader.
      * 
      * @return the entity resolver being used by the loader.
      */
     public EntityResolver getEntityResolver() {
         return this.entityResolver;
     }
 
     private boolean useSchemaLocationAttributes = false;
 
     /**
      * @see org.xbrlapi.loader.Loader#useSchemaLocationAttributes()
      */
     public boolean useSchemaLocationAttributes() {
         return this.useSchemaLocationAttributes;
     }
 
     /**
      * @see org.xbrlapi.loader.Loader#setSchemaLocationAttributeUsage(boolean)
      */
     public void setSchemaLocationAttributeUsage(boolean useThem) {
         this.useSchemaLocationAttributes = useThem;
     }
 
     /**
      * @see org.xbrlapi.loader.Loader#storeDocumentsToAnalyse()
      */
     public void storeDocumentsToAnalyse() throws XBRLException {
         getStore().storeLoaderState(getDocumentsStillToAnalyse());
     }
 }
