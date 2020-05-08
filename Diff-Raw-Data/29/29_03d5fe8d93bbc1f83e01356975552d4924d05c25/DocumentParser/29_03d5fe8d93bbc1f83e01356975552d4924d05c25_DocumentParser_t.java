 /**
  * 
  */
 package ecologylab.bigsemantics.documentparsers;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 import java.util.HashSet;
 
 import ecologylab.bigsemantics.actions.SemanticActionsKeyWords;
 import ecologylab.bigsemantics.collecting.SemanticsGlobalScope;
 import ecologylab.bigsemantics.collecting.SemanticsSessionScope;
 import ecologylab.bigsemantics.downloaders.controllers.DownloadController;
 import ecologylab.bigsemantics.metadata.builtins.Document;
 import ecologylab.bigsemantics.metadata.builtins.DocumentClosure;
 import ecologylab.bigsemantics.metadata.output.DocumentLogRecord;
 import ecologylab.bigsemantics.metametadata.MetaMetadata;
 import ecologylab.bigsemantics.metametadata.MetaMetadataCompositeField;
 import ecologylab.bigsemantics.seeding.Seed;
 import ecologylab.collections.Scope;
 import ecologylab.concurrent.DownloadableLogRecord;
 import ecologylab.generic.Debug;
 import ecologylab.generic.ReflectionTools;
 import ecologylab.net.PURLConnection;
 import ecologylab.net.ParsedURL;
 
 /**
  * Super class for all document parser types. This class obtains the connection to a document. A parse
  * method may be present to process the document.
  * <p/>
  * Their role is to translate a Document into some kind of metadata and semantic actions. 
  * They start with a PURL, which the static connect() method
  * translates into an PURLConnection, and an appropriate instance of a subclass. The
  * {@link #parse() parse()} method is then called to translate the document into the semantic model,
  * using the PURLConnection.
  * <p/>
  * The translation from a PURL to an appropriate subclass instance is performed using a combination
  * of the PURLs extension, if it has a useful one, and then the mime-type returned by the
  * URLConnection response header. These keys are then used to perform a lookup in one of the
  * registries maintained in this class.
  * 
  * @author andruid
  * @author eunyee
  */
 abstract public class DocumentParser<D extends Document>
 		extends Debug
 {
 //	/**
 //	 * All information about the connection. Filled out by the connect() method.
 //	 */
 //	protected PURLConnection		purlConnection;
 
 	protected DocumentClosure		documentClosure;
 	
 	protected DownloadController downloadController;
 
 	public boolean 					cacheHit = false;
 
 	protected SemanticsGlobalScope					semanticsScope;
 	
 	private static boolean			inited;
 
 	protected static final Scope<Class<? extends DocumentParser>>	registryByMimeType	= new Scope<Class<? extends DocumentParser>>();
 
 	protected static final Scope<Class<? extends DocumentParser>>	registryBySuffix		= new Scope<Class<? extends DocumentParser>>();
 	
 	public static final Scope<Class<? extends DocumentParser>> bindingParserMap   		= new Scope<Class<? extends DocumentParser>>();
 
 	static
 	{
 		init();
 	}
 
 	public static void init()
 	{
 		if (!inited)
 		{
 			bindingParserMap.put(SemanticActionsKeyWords.DIRECT_BINDING_PARSER,DirectBindingParser.class);
 			bindingParserMap.put(SemanticActionsKeyWords.XPATH_PARSER,XPathParser.class);
 			bindingParserMap.put(SemanticActionsKeyWords.FEED_PARSER, FeedParser.class);
 			bindingParserMap.put(SemanticActionsKeyWords.HTML_IMAGE_DOM_TEXT_PARSER, HTMLDOMImageTextParser.class);
 			bindingParserMap.put(SemanticActionsKeyWords.PDF_PARSER, PdfParser.class);
 		}
 	}
 	
 	private static final HashSet<String>	NO_PARSER_SUFFIX_MAP	= new HashSet<String>();
 	
 	/**
 	 * Keys are DocumentParser class names without package names, returned by Class.getSimpleName().
 	 */
 	protected static final Scope<Class<? extends DocumentParser>>	registryByClassName	= new Scope<Class<? extends DocumentParser>>();
 
 	
 	/**
 	 * DocumentType constructor
 	 * 
 	 */
 	protected DocumentParser ()
 	{
 	}
 
 	/**
 	 * Set the InfoCollector while constructing.
 	 * 
 	 * @param infoCollector
 	 */
 	protected DocumentParser ( SemanticsGlobalScope infoCollector )
 	{
 		this.semanticsScope = infoCollector;
 	}
 	
 	public abstract void parse ( ) throws IOException;
 	
 	public DownloadController getDownloadController()
 	{
 	  return downloadController;
 	}
 
 	/**
 	 * Fill out the instance of this resulting from a successful connect().
 	 * 
 	 * @param purlConnection
 	 * @param documentClosure TODO
 	 * @param infoCollector
 	 */
 //	public void fillValues ( PURLConnection purlConnection, DocumentClosure documentClosure, SemanticsGlobalScope infoCollector )
 	public void fillValues ( DownloadController downloadController, DocumentClosure documentClosure, SemanticsGlobalScope infoCollector )
 	{
 		this.downloadController		= downloadController;
 		this.documentClosure	= documentClosure;
 		this.semanticsScope = infoCollector;
 	}
 
 	/**
 	 * True if our analysis indicates the present AbstractContainer is an article, and not a
 	 * collection of links. This affects calls to getWeight() in the model!
 	 * 
 	 * @return true for an article. false for a collection of links (like a homepage).
 	 */
 	public boolean isAnArticle ( )
 	{
 		return true;
 	}
 
 	/**
 	 * Free resources.
 	 * 
 	 */
 	public void recycle ( )
 	{
 //		if (purlConnection != null)
 //		{
 //			purlConnection.recycle();
 //			purlConnection	= null;
 //		}
 		semanticsScope 		= null;
 		documentClosure		= null;
 	}
 
 	/**
 	 * This method enables the default behavior of showing the user a message on the console,
 	 * "Downloading http://..." to be overriden in the case of particular DocumentTypes that display
 	 * their own custom messages.
 	 * 
 	 * @return true -- the default implementation.
 	 */
 	public boolean downloadingMessageOnConnect ( )
 	{
 		return true;
 	}
 
 	/*
 	 * This method is an odd out for File-based instances of DocumentType, that arise during parse()
 	 * of FileDirectoryType.
 	 * 
 	 * If they are many, we may want to process them without doing downloadAndParse().
 	 */
 	public void processWithoutParsing ( )
 	{
 
 	}
 
 	protected InputStream inputStream ( )
 	{
 		return (downloadController == null) ? null : downloadController.getInputStream();
 	}
 	
 	protected Reader reader()
 	{
 		return null;
 	}
 
 
 	public void handleIoError (Throwable e )
 	{
 		recycle();
 	}
 
 	public PURLConnection purlConnection ( )
 	{
 //		return purlConnection;
 	  return null;
 	}
 
 	/**
 	 * Get the ParsedURL value of the current document. If you want to get a URL value, you can use
 	 * url() method in ParsedURL.
 	 * 
 	 * @return
 	 */
 	public ParsedURL purl ( )
 	{
	  if (documentClosure == null)
	    return null;
	  
 		Document document	= documentClosure.getDocument();
		if (document == null)
		  return null;
		
		ParsedURL docPurl = document.getLocation();
 		if (downloadController != null)
 		{
 			ParsedURL connPurl = downloadController.getRedirectedLocation();
 			if (connPurl == null)
 			{
 			  connPurl = downloadController.getLocation();
 			}
 			if (semanticsScope.isService() && connPurl.isFile()	&& docPurl != null && !docPurl.isFile())
 			{
 				return docPurl;
 			}
 			return connPurl;
 		}
 		return docPurl;
 		//return purlConnection != null ? purlConnection.getPurl() : (document != null) ? document.getLocation() : null;
 	}
 
 	public String toString ( )
 	{
 		ParsedURL purl = purl();
 		String purlString = (purl != null) ? purl.toString() : null;
 		if (purlString == null)
 			purlString = "no purl";
 		return super.toString() + "[" + purlString + "]";
 	}
 
 	/**
 	 * Create a mapping from MimeType to the class object for a DocumentType.
 	 */
 /*	public static void registerMime ( String mimeType,
 			Class<? extends DocumentParser> documentTypeClass )
 	{
 		registryByMimeType.put(mimeType, documentTypeClass);
 
 		String simpleName = documentTypeClass.getSimpleName();
 		if (!registryByClassName.containsKey(simpleName))
 		{
 			println("register simple name: " + simpleName);
 			registryByClassName.put(simpleName, documentTypeClass);
 		}
 	}*/
 
 	/**
 	 * Create a mapping from filename suffix to the class object for a DocumentType. Actually create
 	 * two mappings, one lower case, and one upper case.
 	 */
 /*	public static void registerSuffix ( String suffix,
 			Class<? extends DocumentParser> documentTypeClass )
 	{
 		String lc = suffix.toLowerCase();
 		registryBySuffix.put(lc, documentTypeClass);
 		String uc = suffix.toUpperCase();
 		registryBySuffix.put(uc, documentTypeClass);
 	}
 */
 	/**
 	 * Find the DocumentType class that corresponds to the mimeType, and construct a fresh instance
 	 * of that type.
 	 * 
 	 * @param infoCollector
 	 *            TODO
 	 * 
 	 * @return an instance of the DocumentType subclass that corresponds to mimeType.
 	 */
 	/*public static DocumentParser getInstanceByMimeType ( String mimeType, InfoCollector infoCollector )
 	{
 		return getInstanceFromRegistry(registryByMimeType, mimeType, infoCollector);
 	}*/
 
 	/**
 	 * Find the DocumentType class that corresponds to the mimeType, and construct a fresh instance
 	 * of that type.
 	 * 
 	 * @param infoCollector
 	 *            TODO
 	 * 
 	 * @return an instance of the DocumentType subclass that corresponds to mimeType.
 	 */
 	/*public static DocumentParser getInstanceBySuffix ( String suffix, InfoCollector infoCollector )
 	{
 		return ((suffix == null) || (suffix.length() == 0)) ? null : getInstanceFromRegistry(
 				registryBySuffix, suffix, infoCollector);
 	}
 */
 
 	/**
 	 * Filter description cFMetadata for junk
 	 * 
 	 * @return true if the stuff is worth using
 	 */
 	public static boolean isNonJunkDescription ( String description )
 	{
 		// println("isNonJunkDescription("+description);
 		return (description != null) && !description.startsWith("Find breaking")
 				&& // nytimes
 				!description.startsWith("Read full")
 				&& // cnn
 				!description.startsWith("CNN.com") && !description.startsWith("Visit BBC")
 				&& !description.startsWith("Flickr is almost certainly");
 	}
 
 	/**
 	 * Find the DocumentType class that corresponds to the documentTypeSimpleName, and construct a
 	 * fresh instance of that type.
 	 * 
 	 * @param documentTypeSimpleName
 	 *            - Name of the DocumentType subclass, without the package.
 	 * @param infoCollector
 	 *            TODO
 	 * 
 	 * @return an instance of the DocumentType subclass that corresponds to documentTypeSimpleName.
 	 */
 	public static DocumentParser getInstanceBySimpleName ( String documentTypeSimpleName,
 			SemanticsSessionScope infoCollector )
 	{
 		return getInstanceFromRegistry(registryByClassName, documentTypeSimpleName, infoCollector);
 	}
 
 	public static boolean isMimeTypeParsable ( String mimeType )
 	{
 		return registryByMimeType.containsKey(mimeType);
 	}
 	
 	static final Class[]  DEFAULT_DOCUMENTPARSER_ARG        = {SemanticsGlobalScope.class};
 
 	/**
 	 * Given one of our registries, and a key, do a lookup in the registry to obtain the Class
 	 * object for the DocumentType subclass corresponding to the key -- in that registry.
 	 * <p/>
 	 * Then, construct an instance of that object
 	 * 
 	 * @param infoCollector
 	 *            TODO
 	 * 
 	 * @return an instance of the DocumentType subclass that corresponds to the key, in the
 	 *         specified registry.
 	 */
 	public static DocumentParser getInstanceFromRegistry (
 			Scope<Class<? extends DocumentParser>> thatRegistry, String key, SemanticsSessionScope infoCollector )
 	{
 		DocumentParser result = null;
 		Class<? extends DocumentParser> documentTypeClass = thatRegistry.get(key);
 
 		Object[] constructorArgs = new Object[1];
 		constructorArgs[0] = infoCollector;
 
 		result = ReflectionTools.getInstance(documentTypeClass, DEFAULT_DOCUMENTPARSER_ARG, constructorArgs);
 //		if (result == null)
 //			throw new RuntimeException("Registry lookup worked, but constructor matching failed :-( :-( :-(");
 		return result;
 	}
 
 	public static DocumentParser getInstanceFromRegistry (Scope<Class<? extends DocumentParser>> thatRegistry, 
 			String key, Class<?>[] parameterTypes, Object[] args )
 	{
 		DocumentParser result = null;
 		Class<? extends DocumentParser> documentTypeClass = thatRegistry.get(key);
 		if (documentTypeClass != null)
 		{
 			if ((parameterTypes == null) || (args == null))
 			{
 				result = getDocumentParserInstance(documentTypeClass);
 			}
 			else
 			{
 				result = ReflectionTools.getInstance(documentTypeClass, parameterTypes, args);
 			}
 		}
 		return result;
 	}
 
 	public static DocumentParser getParserInstanceFromBindingMap(String binding,
 			SemanticsGlobalScope infoCollector)
 	{
 		DocumentParser result = null;
 		Class<? extends DocumentParser> documentTypeClass = (Class<? extends DocumentParser>) bindingParserMap.get(binding);
 
 		Object[] constructorArgs = new Object[1];
 		constructorArgs[0] = infoCollector;
 
 		result = ReflectionTools.getInstance(documentTypeClass, DEFAULT_DOCUMENTPARSER_ARG, constructorArgs);
 		return result;
 	}
 
 	/**
 	 * Get a DocumentType from a generic parameterized Class object.
 	 * 
 	 * @param thatClass
 	 * @return
 	 */
 	public static DocumentParser getDocumentParserInstance ( Class<? extends DocumentParser> thatClass )
 	{
 		DocumentParser result = null;
 		if (thatClass != null)
 		{
 			try
 			{
 				result = thatClass.newInstance();
 			}
 			catch (InstantiationException e)
 			{
 				e.printStackTrace();
 			}
 			catch (IllegalAccessException e)
 			{
 				e.printStackTrace();
 			}
 		}
 		return result;
 	}
 
 	public boolean isIndexPage ( )
 	{
 		return false;
 	}
 
 	public boolean isContentPage ( )
 	{
 		return false;
 	}
 
 	/**
 	 * Differentiates referential documents, like HTML from composite documents, like PDF.
 	 * 
 	 * @return 	true if images referred to by this document are stored within the document itself.
 	 * 					The default implementation, here returns false.
 	 */
 	public boolean isCompositeDocument()
 	{
 		return false;
 	}
 
 
 	public SemanticsGlobalScope getSemanticsScope()
 	{
 		return semanticsScope;
 	}
 
 	public MetaMetadataCompositeField getMetaMetadata()
 	{
 		Document document	= documentClosure.getDocument();
 		return document == null ? null : document.getMetaMetadata();
 	}
 
 	public ParsedURL getTruePURL()
 	{
 		Document document	= documentClosure.getDocument();
 		return document == null ? null : document.getLocation();
 	}
 	
 	/**
 	 * Connects to a SeedDistributor, when appropriate.
 	 * 
 	 * @return 	null always for the default base class.
 	 */
 	public Seed getSeed()
 	{
 		return null;
 	}
 
 	/**
 	 * 
 	 * @return	true to avoid building a DOM at the beginning
 	 */
 	public boolean doesDirectBinding()
 	{
 		return false;
 	}
 	
 	public DocumentClosure getDocumentClosure()
 	{
 		return documentClosure;
 	}
 
 	/**
 	 * @return the document
 	 */
 	public D getDocument()
 	{
 		return (D) documentClosure.getDocument();
 	}
 	
 	public DocumentLogRecord getLogRecord()
 	{
 	  DocumentClosure documentClosure = getDocumentClosure();
 	  if (documentClosure != null)
 	  {
   	  DownloadableLogRecord downloadableLogRecord = documentClosure.getLogRecord();
   	  if (downloadableLogRecord instanceof DocumentLogRecord)
   	  {
   	    return (DocumentLogRecord) downloadableLogRecord;
   	  }
 	  }
 	  else
 	  {
 	    debug("weird: documentClosure is null!");
 	  }
 	  return DocumentLogRecord.DUMMY;
 	}
 	
   /**
    * Allow clients to map or remap parsers by name. This can be useful when a client needs to
    * provide an alternative implementation for an parser. For example, the semantics service may
    * need to use a different algorithm for HTML image text clipping derivation in another parser.
    * 
    * @param parserName
    * @param parserClass
    */
 	public static void map(String parserName, Class<? extends DocumentParser> parserClass)
 	{
 	  bindingParserMap.put(parserName, parserClass);
 	}
 
 	public static DocumentParser get(MetaMetadata mmd, SemanticsGlobalScope infoCollector)
 	{
 		String parserName = mmd.getParser();
 		if (parserName != null)
 		{
 			return DocumentParser.getParserInstanceFromBindingMap(parserName, infoCollector);
 		}
 		return null;
 	}
 
 	public static boolean isRegisteredNoParser(ParsedURL purl)
 	{
 		boolean result	= false;
 		if (purl != null)
 		{
   		String suffix		= purl.suffix();
   		if (suffix != null && suffix.length() > 0)
   		{
   			result				= NO_PARSER_SUFFIX_MAP.contains(suffix);
   			if (!result)
   				NO_PARSER_SUFFIX_MAP.add(suffix);
   		}
 		}
 		return result;
 	}
 	
 	/**
 	 * Use the DocumentClosure to close the current connection.
 	 * Re-open a connection to the same location.
 	 * Use the same Document object; don't process re-directs, or anything like that.
 	 * Re-connect simply.
 	 * Reset the purlConnection field of this to the new PURLConnection.
 	 * 
 	 * @return	InputStream for the new connection.
 	 * @throws IOException 
 	 */
 	public InputStream reConnect() throws IOException
 	{
 		DownloadController downloadController = documentClosure.reConnect();
 		this.downloadController	= downloadController;
 		return downloadController.getInputStream();
 	}
 }
