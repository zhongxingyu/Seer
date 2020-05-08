 package org.jcouchdb.db;
 
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.jcouchdb.document.AbstractViewResult;
 import org.jcouchdb.document.BaseDocument;
 import org.jcouchdb.document.DesignDocument;
 import org.jcouchdb.document.Document;
 import org.jcouchdb.document.DocumentHelper;
 import org.jcouchdb.document.DocumentInfo;
 import org.jcouchdb.document.ViewAndDocumentsResult;
 import org.jcouchdb.document.ViewResult;
 import org.jcouchdb.exception.DataAccessException;
 import org.jcouchdb.exception.DocumentValidationException;
 import org.jcouchdb.exception.NotFoundException;
 import org.jcouchdb.exception.UpdateConflictException;
 import org.jcouchdb.util.Assert;
 import org.jcouchdb.util.ExceptionWrapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.svenson.JSON;
 import org.svenson.JSONConfig;
 import org.svenson.JSONParser;
 
 /**
  * Contains the main interface of working with a couchdb database
  *
  * @author shelmberger
  *
  */
 public class Database
 {
     private static final String DESIGN_DOCUMENT_PREFIX = "_design/";
     
     private static final String VIEW_DOCUMENT_INFIX = "view";
 
     private static final String LIST_DOCUMENT_INFIX = "list";
 
     private static final String SHOW_DOCUMENT_INFIX = "show";
 
     private JSON jsonGenerator = new JSON();
 
     static final String VIEW_QUERY_VALUE_TYPEHINT = ".rows[].value";
     private static final String VIEW_QUERY_DOCUMENT_TYPEHINT = ".rows[].doc";
 
     protected static Logger log = LoggerFactory.getLogger(Database.class);
 
     /**
      * Name of the all docs view.
      */
     private static final String ALL_DOCS = "_all_docs";
 
     private static final String ALL_DOCS_BY_SEQ = "_all_docs_by_seq";
 
 
     private String name;
 
     private Server server;
 
     private List<DatabaseEventHandler> eventHandlers = new ArrayList<DatabaseEventHandler>();
 
     private JSONParser jsonParser;
 
     private volatile JSONParser bulkCreateParser;
 
     /**
      * Creates a database object for the given host, the default port and the given data base name.
      *
      * @param host
      * @param name
      */
     public Database(String host, String name)
     {
         this(new ServerImpl(host), name);
     }
 
     /**
      * Create a database object for the given host, port and database name.
      *
      * @param host
      * @param port
      * @param name
      */
     public Database(String host, int port, String name)
     {
         this(new ServerImpl(host, port), name);
     }
 
     /**
      * Creates a database object for the given Server object and the given database name.
      *
      * @param server
      * @param name
      */
     public Database(Server server, String name)
     {
         this.server = server;
         this.name = name;
     }
 
     /**
      * Returns the name of this database object
      * @return
      */
     public String getName()
     {
         return name;
     }
 
     /**
      * Returns the server this database is on.
      *
      * @return
      */
     public Server getServer()
     {
         return server;
     }
     
     public void setJsonGenerator(JSON jsonGenerator)
     {
         this.jsonGenerator = jsonGenerator;
     }
     
     public void setJsonParser(JSONParser jsonParser)
     {
         this.jsonParser = jsonParser;        
         this.bulkCreateParser = null;
         
     }
     
     public void setJsonConfig(JSONConfig config)
     {
         this.jsonGenerator = config.getJsonGenerator();
         this.jsonParser = config.getJsonParser();
     }
     
     public JSONConfig getJsonConfig()
     {
         return new JSONConfig(jsonGenerator, jsonParser);
     }
 
     public List<DatabaseEventHandler> getEventHandlers()
     {
         return eventHandlers;
     }
 
     public void setEventHandlers(List<DatabaseEventHandler> eventHandlers)
     {
         Assert.notNull(eventHandlers, "event handlers can't be null");
         this.eventHandlers = eventHandlers;
     }
 
     public void addEventHandler(DatabaseEventHandler handler)
     {
         eventHandlers.add(handler);
     }
 
     /**
      * Returns the {@link DatabaseStatus} of the current database
      *
      * @return
      */
     public DatabaseStatus getStatus()
     {
         Response resp = null;
         try
         {
             resp = server.get("/" + name + "/");
             if (!resp.isOk())
             {
                 throw new DataAccessException("error getting database status for database " + name +
                     ": ", resp);
             }
             return resp.getContentAsBean(DatabaseStatus.class);
         }
         finally
         {
             if (resp != null)
             {
                 resp.destroy();
             }
         }
     }
 
     /**
      * Triggers database compaction.
      *
      */
     public void compact()
     {
         Response resp = null;
         try
         {
             resp = server.get("/" + name + "/_compact");
             if (!resp.isOk())
             {
                 throw new DataAccessException("error getting database status for database " + name +
                     ": ", resp);
             }
         }
         finally
         {
             resp.destroy();
         }
         
     }
 
     /**
      * Returns the design document with the given id.
      *
      * @param id
      * @return
      */
     public DesignDocument getDesignDocument(String id)
     {
         return getDocument(DesignDocument.class, DesignDocument.extendId(id));
     }
 
     /**
      * Returns the document with the given id and converts it to the given class.
      *
      * @param <D>   type
      * @param cls   runtime class info
      * @param docId document id
      */
     public <D> D getDocument(Class<D> cls, String docId)
     {
         return getDocument(cls,docId,null,null);
     }
 
     /**
      * Returns the document with the given id and converts it to the given class with
      * the given configured JSONParser
      *
      * @param <D>       type
      * @param cls       runtime class info
      * @param docId     document id
      * @param revision  revision of the document to get
      * @param parser    configured parser
      * @return
      */
     public <D> D getDocument(Class<D> cls, String docId, String revision, JSONParser parser)
     {
         Assert.notNull(cls, "class cannot be null");
         Assert.notNull(docId, "document id cannot be null");
 
         if (!docId.startsWith("_design/"))
         {
             docId = encodeURL(docId);
         }
         
         String uri = "/" + name + "/" + (docId);
         if (revision != null)
         {
             uri += "?rev="+revision;
         }
         Response resp = null;
         try
         {
             resp = server.get(uri);
             if (resp.getCode() == 404)
             {
                 throw new NotFoundException("document not found", resp);
             }
             else if (!resp.isOk())
             {
                 throw new DataAccessException("error getting document " + docId + ": ", resp);
             }
             
             resp.setParser(getJSONParserCopy(parser));
             return resp.getContentAsBean(cls);
         }
         finally
         {
             if (resp != null)
             {
                 resp.destroy();
             }
         }
 
     }
 
     /**
      * Creates the given document and updates  the document's id and revision properties. If the
      * document has an id property, a named document will be created, else the id will be generated by the server.
      * assigned.
      *
      * @param doc   Document to create.
      * @throws IllegalStateException if the document already had a revision set
      * @throws UpdateConflictException  if there's an update conflict while updating the document
      */
     public void createDocument(Object doc)
     {
         Assert.notNull(doc, "document cannot be null");
 
         if (DocumentHelper.getRevision(doc) != null)
         {
             throw new IllegalStateException("Newly created docs can't have a revision ( is = " +
                 DocumentHelper.getRevision(doc) + " )");
         }
 
         createOrUpdateDocument(doc);
     }
 
     /**
      * @param documents
      * @return
      */
     public List<DocumentInfo> bulkCreateDocuments(List<? extends Document> documents)
     {
     	return this.bulkCreateDocuments(documents, false);
     }
     
     /**
      * Bulk creates the given list of documents.
      * @param documents
      * @return
      */
     public List<DocumentInfo> bulkCreateDocuments(List<?> documents, boolean allOrNothing)
     {
         Assert.notNull(documents, "documents cannot be null");
 
         Map<String,Object> wrap = new HashMap<String, Object>();
         if(allOrNothing)
         {
         	wrap.put("all_or_nothing", true);
         }
         wrap.put("docs", documents);
 
         for (Object doc : documents)
         {
             boolean isCreate = DocumentHelper.getId(doc) == null;
             for (DatabaseEventHandler eventHandler : eventHandlers)
             {
                 try
                 {
                     if (isCreate)
                     {
                         eventHandler.creatingDocument(this, doc);
                     }
                     else
                     {
                         eventHandler.updatingDocument(this, doc);
                     }
 
                 }
                 catch (Exception e)
                 {
                     throw new DatabaseEventException(e);
                 }
             }
         }
 
         final String json = jsonGenerator.forValue(wrap);
         Response resp = null;
         try
         {
             resp = server.post("/" + name + "/_bulk_docs", json);
 
             for (Object doc : documents)
             {
                 boolean isCreate = DocumentHelper.getId(doc) == null;
                 for (DatabaseEventHandler eventHandler : eventHandlers)
                 {
                     if (isCreate)
                     {
                         eventHandler.createdDocument(this, doc, resp);
                     }
                     else
                     {
                         eventHandler.updatedDocument(this, doc, resp);
                     }
                 }
             }
 
             resp.setParser(getBulkCreateParser());
             List<DocumentInfo> infos = resp.getContentAsBean(ArrayList.class);
 
             if (infos != null)
             {
                 return infos;
             }
             else
             {
                 throw new DataAccessException("Error bulk creating documents", resp);
             }
         }
         finally
         {
             if (resp != null)
             {
                 resp.destroy();
             }
         }
 
     }
     
     private JSONParser getBulkCreateParser()
     {
         if (bulkCreateParser == null)
         {
             synchronized(this)
             {
                 if (bulkCreateParser == null)
                 {
                     bulkCreateParser =  getJSONParserCopy(null);
                     this.bulkCreateParser.addTypeHint("[]", DocumentInfo.class);
                 }
             }
         }
         return bulkCreateParser;
     }
     
     /**
      * Deletes the document with the given id and revision.
      *
      * @param docId         document id to delete
      * @param revision      revision to delete
      */
     public void delete( String docId, String revision)
     {
         Assert.notNull(docId, "document id cannot be null");
         Assert.notNull(revision, "revision cannot be null");
 
         for (DatabaseEventHandler eventHandler : eventHandlers)
         {
             try
             {
                 eventHandler.deletingDocument(this, docId, revision);
             }
             catch (Exception e)
             {
                 throw new DatabaseEventException(e);
             }
         }
 
         Response resp = null;
         try
         {
             resp = server.delete("/" + name + "/" + encodeURL(docId)+"?rev=" + revision );
 
             for (DatabaseEventHandler eventHandler : eventHandlers)
             {
                 eventHandler.deletedDocument(this, docId, revision, resp);
             }
 
             if (!resp.isOk())
             {
                 throw new DataAccessException("Error deleting document", resp);
             }
         }
         finally
         {
             if (resp != null)
             {
                 resp.destroy();
             }
         }
     }
 
     /**
      * Deletes the given document.
      * @param document  document
      */
     public void delete(Object document)
     {
         String id = DocumentHelper.getId(document);
         String rev = DocumentHelper.getRevision(document);
         delete(id,rev);
     }
 
     /**
      * Creates or updates given document and updates the document's id and revision properties. If the
      * document has an id property, a named document will be created, else the id will be generated by the server.
      * assigned.
      *
      * @param doc   Document to create.
      * @throws UpdateConflictException  if there's an update conflict while updating the document
      */
     public void createOrUpdateDocument(Object doc)
     {
         Response resp = null;
         try
         {
             String id = DocumentHelper.getId(doc);
             boolean isCreate = id == null;
 
             for (DatabaseEventHandler eventHandler : eventHandlers)
             {
                 try
                 {
                     if (isCreate)
                     {
                         eventHandler.creatingDocument(this, doc);
                     }
                     else
                     {
                         eventHandler.updatingDocument(this, doc);
                     }
                 }
                 catch (Exception e)
                 {
                     throw new DatabaseEventException(e);
                 }
             }
 
             final String json = jsonGenerator.forValue(doc);
             if (isCreate)
             {
                 resp = server.post("/" + name + "/", json);
             }
             else
             {
                 resp = server.put("/" + name + "/" + encodeURL(id), json);
             }
 
             for (DatabaseEventHandler eventHandler : eventHandlers)
             {
                 try
                 {
                     if (isCreate)
                     {
                         eventHandler.createdDocument(this, doc, resp);
                     }
                     else
                     {
                         eventHandler.updatedDocument(this, doc, resp);
                     }
                 }
                 catch (Exception e)
                 {
                     throw new DatabaseEventException(e);
                 }
             }
 
             if (resp.getCode() == 409)
             {
                 throw new UpdateConflictException("error creating document "+json + "in database '" + name + "'", resp);
             }
             else if (resp.getCode() == 403)
             {
                 throw new DocumentValidationException(resp);
             }
             else if (!resp.isOk())
             {
                 throw new DataAccessException("error creating document " + json + "in database '" + name + "'", resp);
             }
             DocumentInfo info = resp.getContentAsBean(DocumentInfo.class);
 
             if (isCreate)
             {
                 DocumentHelper.setId(doc, info.getId());
             }
             DocumentHelper.setRevision(doc, info.getRevision());
         }
         finally
         {
             if (resp != null)
             {
                 resp.destroy();
             }
         }
     }
 
     /**
      * Updates given document and updates the document's revision property.
      *
      * @param doc   Document to create.
      * @throws IllegalStateException if the document had no revision property
      * @throws UpdateConflictException  if there's an update conflict while updating the document
      */
     public void updateDocument(Object doc)
     {
         if (DocumentHelper.getId(doc) == null)
         {
             throw new IllegalStateException("id must be set for updates");
         }
         if (DocumentHelper.getRevision(doc) == null)
         {
             throw new IllegalStateException("revision must be set for updates");
         }
 
         createOrUpdateDocument(doc);
     }
 
     /**
      * List all documents in the database.
      *
      * @param options
      * @param parser
      * @return
      */
     public ViewResult<Map> listDocuments(Options options, JSONParser parser)
     {
         return (ViewResult<Map>)queryViewInternal(ALL_DOCS, Map.class, null, options, parser, null);
     }
 
     /**
      * Lists all documents in the database in the order they were last updated.
      * @param options
      * @param parser
      * @return
      */
     public ViewResult<Map> listDocumentsByUpdateSequence(Options options, JSONParser parser)
     {
         return (ViewResult<Map>)queryViewInternal(ALL_DOCS_BY_SEQ, Map.class, null, options, parser, null);
     }
 
     /**
      * Queries the view with the given name and converts the received views
      * to the given type
      * @param <V>       type
      * @param viewName  view name
      * @param cls       runtime type information
      * @param options   query options
      * @param parser    configured JSON Parser
      * @return
      */
     public <V> ViewResult<V> queryView(String viewName, Class<V> cls, Options options, JSONParser parser)
     {
         return (ViewResult<V>)queryViewInternal(viewURIFromName(viewName), cls, null, options, parser, null);
     }
 
     private static String encodeURL(String s)
     {
         try
         {
             return URLEncoder.encode(s, "UTF-8");
         }
         catch (UnsupportedEncodingException e)
         {
             throw ExceptionWrapper.wrap(e);
         }
     }
     
     private String viewURIFromName(String viewName)
     {
         return getDesignURIFromNameAndInfix(viewName, VIEW_DOCUMENT_INFIX);
     }
     
     private String getDesignURIFromNameAndInfix(String viewName, String infix)
     {
         int slashPos = viewName.indexOf("/");
         if (slashPos < 0)
         {
             throw new IllegalArgumentException("viewName must contain a slash separating the design doc name from the " + infix + " name");
         }
         return DESIGN_DOCUMENT_PREFIX + (viewName.substring(0,slashPos)) + "/_" + infix + "/" + (viewName.substring(slashPos + 1));
     }
 
     /**
      * Queries the view and the documents with the given name and converts the received views and documents.
      *
      * to the given type
      * @param <V>           value type
      * @param <D>           document type
      * @param viewName      view name
      * @param valueClass    runtime value type information
      * @param documentClass runtime document type information
      * @param options       query options
      * @param parser        configured JSON Parser
      * @return
      */
     public <V,D> ViewAndDocumentsResult<V,D> queryViewAndDocuments(String viewName, Class<V> valueClass, Class<D> documentClass, Options options, JSONParser parser)
     {
         return (ViewAndDocumentsResult<V,D>)queryViewInternal(viewURIFromName(viewName), valueClass, documentClass, options, parser, null);
     }
 
     /**
      * Executes the given map / reduce functions and convert the response to a view result of the given class.
      * @param <V>       type
      * @param cls       runtime type info
      * @param fn        map / reduce function as valid JSON string (e.g. <code>{ "map" : "function(doc) { emit(null,doc);" }</code>
      * @param options   query options
      * @param parser    configured {@link JSONParser}
      * @return
      */
     public <V> ViewResult<V> queryAdHocView(Class<V> cls, String fn, Options options, JSONParser parser)
     {
         if (cls == null)
         {
             throw new IllegalArgumentException("class cannot be null");
         }
 
         String uri = "/"+name+"/_temp_view";
 
         if (options != null)
         {
             uri += options.toQuery();
         }
 
         if (log.isDebugEnabled())
         {
             log.debug("querying view " + uri);
         }
 
         Response resp = null;
         try
         {
             resp = server.post(uri,fn);
             if (!resp.isOk())
             {
                 throw new DataAccessException("error querying view", resp);
             }
 
             JSONParser parserCopy = getJSONParserCopy(parser);
             parserCopy.addTypeHint(VIEW_QUERY_VALUE_TYPEHINT, cls);
             resp.setParser(parserCopy);
             return resp.getContentAsBean(ViewResult.class);
         }
         finally
         {
             if (resp != null)
             {
                 resp.destroy();
             }
         }
     }
 
     /**
      * Queries a View by URI.
      *
      * @param <V>           value type
      * @param uri           uri to query (e.g. "_all_docs" or "_view/foo/bar")
      * @param valueClass    value runtime type
      * @param options       options or <code>null</code>
      * @param parser        JSON parser or <code>null</code>
      * @param keys          keys or <code>null</code>
      * @return
      */
     public <V> ViewResult<V> query(String uri, Class<V> valueClass, Options options, JSONParser parser, Object keys)
     {
         Assert.notNull(uri, "URI can't be null");
         Assert.notNull(valueClass, "value class can't be null");
         return (ViewResult<V>)queryViewInternal( uri, valueClass, null, options, parser, keys);
     }
 
     public <V,D> ViewAndDocumentsResult<V,D> query(String uri, Class<V> valueClass, Class<D> documentClass, Options options, JSONParser parser, Object keys)
     {
         Assert.notNull(uri, "URI can't be null");
         Assert.notNull(valueClass, "value class can't be null");
         Assert.notNull(documentClass, "document class can't be null");
         return (ViewAndDocumentsResult<V,D>)queryViewInternal( uri, valueClass, documentClass, options, parser, keys);
     }
 
     /**
      * Internal view query method.
      *
      * @param <V>               type to parse the response into
      * @param viewName          view name
      * @param valueClass        runtime value type
      * @param documentClass     runtime document type
      * @param options           query options
      * @param parser            parser to parse the response with
      * @param keys              keys to query, if this is not <code>null</code>, a POST request with the keys as JSON will be done.
      * @return
      */
     private <V> AbstractViewResult<V> queryViewInternal(String viewName, Class<V> valueClass, Class documentClass, Options options, JSONParser parser, Object keys)
     {
         if (viewName == null)
         {
             throw new IllegalArgumentException("view name cannot be null");
         }
         if (valueClass == null)
         {
             throw new IllegalArgumentException("class cannot be null");
         }
 
         String uri = "/" + this.name + "/" + viewName;
 
         boolean isDocumentQuery = documentClass != null;
 
         if (isDocumentQuery)
         {
             if (options == null)
             {
                 options = new Options();
             }
             options.includeDocs(true);
         }
 
         if (options != null)
         {
             uri += options.toQuery();
         }
 
         if (log.isDebugEnabled())
         {
             log.debug("querying view " + uri);
         }
 
         Response resp = null;
         try
         {
             if (keys == null)
             {
                 resp = server.get(uri);
             }
             else
             {
                 resp = server.post(uri, jsonGenerator.forValue(keys));
             }
 
             if (!resp.isOk())
             {
                 throw new DataAccessException("error querying view", resp);
             }
 
             JSONParser parserCopy = getJSONParserCopy(parser);
             parserCopy.addTypeHint(VIEW_QUERY_VALUE_TYPEHINT, valueClass);
             if (isDocumentQuery)
             {
                 parserCopy.addTypeHint(VIEW_QUERY_DOCUMENT_TYPEHINT, documentClass);
                 resp.setParser(parserCopy);
                 return resp.getContentAsBean(ViewAndDocumentsResult.class);
             }
             else
             {
                 resp.setParser(parserCopy);
                 return resp.getContentAsBean(ViewResult.class);
             }
         }
         finally
         {
             if (resp != null)
             {
                 resp.destroy();
             }
         }
 
     }
 
     private JSONParser getJSONParserCopy(JSONParser localParser)
     {
         return new JSONParser(localParser != null ? localParser : jsonParser);
     }
 
 
     /**
      * Queries the given keys from the view with the given name.
      *
      * @param <V>           Type parse the queried documents into
      * @param viewName      view name
      * @param cls           runtime type info
      * @param keys          list of keys to query
      * @param parser        configured JSON parser
      * @return view result
      */
     public <V> ViewResult<V> queryViewByKeys(String viewName, Class<V> cls, List<?> keys, Options options, JSONParser parser)
     {
         Map<String, Object> m = new HashMap<String, Object>();
         m.put("keys", keys);
         return (ViewResult<V>)queryViewInternal(viewURIFromName(viewName), cls, null, options, parser, m);
     }
 
     /**
      * Queries the given keys and documents from the view with the given name.
      *
      * @param <V>               Type parse the queried documents into
      * @param viewName          view name
      * @param valueClass        runtime value type info
      * @param documentClass     runtime document type info
      * @param keys              list of keys to query
      * @param parser            configured JSON parser
      * @return view result
      */
     public <V,D> ViewAndDocumentsResult<V,D> queryViewAndDocumentsByKeys(String viewName, Class<V> valueClass, Class<D> documentClass, List<?> keys, Options options, JSONParser parser)
     {
         Map<String, Object> m = new HashMap<String, Object>();
         m.put("keys", keys);
         return (ViewAndDocumentsResult<V,D>)queryViewInternal(viewURIFromName(viewName), valueClass, documentClass, options, parser, m);
     }
 
     /**
      * Queries the given keys from the all documents view.
      *
      * @param <V>           Type parse the queried documents into
      * @param cls           runtime type info
      * @param keys          list of keys to query
      * @param parser        configured JSON parser
      * @return view result
      */
     public <V> ViewResult<V> queryByKeys(Class<V> cls, List<?> keys, Options options, JSONParser parser)
     {
         Map<String, Object> m = new HashMap<String, Object>();
         m.put("keys", keys);
         return (ViewResult<V>)queryViewInternal( ALL_DOCS, cls, null, options, parser, m);
     }
 
     /**
      * Queries the given keys from the all documents view.
      *
      * @param <V>           Type parse the queried documents into
      * @param cls           runtime type info
      * @param keys          list of keys to query
      * @param parser        configured JSON parser
      * @return view result
      */
     public <V,D> ViewAndDocumentsResult<V,D> queryDocumentsByKeys(Class<V> cls, Class<D> documentClass, List<?> keys, Options options, JSONParser parser)
     {
         Map<String, Object> m = new HashMap<String, Object>();
         m.put("keys", keys);
         return (ViewAndDocumentsResult<V,D>)queryViewInternal( ALL_DOCS, cls, documentClass, options, parser, m);
     }
 
     /**
      * Creates the attachment with the given id on the document with the given document id.
      *
      * @param docId             document id
      * @param attachmentId      attachment id
      * @param revision          document revision or <code>null</code> if the document with the given id does not exist.
      * @param contentType       content type of the attachment
      * @param data              data of the attachment
      * @return new revision
      */
     public String createAttachment(String docId, String revision, String attachmentId, String contentType, byte[] data)
     {
         Response resp = null;
         try
         {
             resp = server.put(attachmentURI(docId, revision, attachmentId) , data, contentType);
 
             if (!resp.isOk())
             {
                 throw new DataAccessException("Error creating attachment",resp);
             }
             Map<String,String> m = resp.getContentAsMap();
             return m.get("rev");
         }
         finally
         {
             if (resp != null)
             {
                 resp.destroy();
             }
         }
     }
 
     /**
      * Updates the attachment with the given id on the document with the given document id and the given revision.
      *
      * @param docId             document id
      * @param revision          document revision
      * @param attachmentId      attachment id
      * @param contentType       content type of the attachment
      * @param data              data of the attachment
      * @return new revision
      */
     public String updateAttachment(String docId, String revision, String attachmentId, String contentType , byte[] data)
     {
         Assert.notNull(revision, "revision can't be null");
         return createAttachment(docId, revision, attachmentId, contentType, data);
     }
 
     /**
      * Creates the attachment with the given id on the document with the given document id.
      *
      * @param docId             document id
      * @param attachmentId      attachment id
      * @param revision          document revision or <code>null</code> if the document with the given id does not exist.
      * @param contentType       content type of the attachment
      * @param is                Input stream providing the binary data.
      * @return new revision
      */
     public String createAttachment(String docId, String revision, String attachmentId, String contentType, InputStream is, long length)
     {
         Response resp = null;
         try
         {
             resp = server.put(attachmentURI(docId, revision, attachmentId) , is, contentType, length);
 
             if (!resp.isOk())
             {
                 throw new DataAccessException("Error creating attachment",resp);
             }
             Map<String,String> m = resp.getContentAsMap();
             return m.get("rev");
         }
         finally
         {
             if (resp != null)
             {
                 resp.destroy();
             }
         }
     }
 
     /**
      * Updates the attachment with the given id on the document with the given document id and the given revision.
      *
      * @param docId             document id
      * @param revision          document revision
      * @param attachmentId      attachment id
      * @param contentType       content type of the attachment
      * @param is                data of the attachment
      * @return new revision
      */
     public String updateAttachment(String docId, String revision, String attachmentId, String contentType , InputStream is, long length)
     {
         Assert.notNull(revision, "revision can't be null");
         return createAttachment(docId, revision, attachmentId, contentType, is, length);
     }
     
     private String attachmentURI(String docId, String revision, String attachmentId)
     {
         String uri = "/" + name + "/" + encodeURL(docId) + "/" + attachmentId;
         if (revision != null)
         {
             uri +="?rev="+revision;
         }
 
         return uri;
     }
 
     /**
      * Deletes the attachment with the given id on the document with the given document id and the given revision.
      *
      * @param docId             document id
      * @param revision          document revision
      * @param attachmentId      attachment id
      * @return new revision
      */
     public String deleteAttachment(String docId, String revision, String attachmentId)
     {
         Response resp = null;
         try
         {
             resp = server.delete(attachmentURI(docId, revision, attachmentId));
             if (!resp.isOk())
             {
                 throw new DataAccessException("Error deleting attachment",resp);
             }
             Map<String,String> m = resp.getContentAsMap();
             return m.get("rev");
         }
         finally
         {
             if (resp != null)
             {
                 resp.destroy();
             }
         }
     }
 
     /**
      * Returns the content of the attachment with the given document id and the given attachment id.
      *
      * @param docId             document id
      * @param attachmentId      attachment id
      * @return
      */
     public byte[] getAttachment(String docId, String attachmentId)
     {
         Response resp = null;
         try
         {
             resp = server.get("/" + name + "/" + encodeURL(docId) + "/" + attachmentId);
             if (resp.getCode() == 404)
             {
                 throw new NotFoundException("attachment not found", resp);
             }
             else if (!resp.isOk())
             {
                 throw new DataAccessException("error getting attachment '" + attachmentId + "' of document '"+docId + "': ", resp);
             }
             return resp.getContent();
         }
         finally
         {
             if (resp != null)
             {
                 resp.destroy();
             }
         }
     }
 
     /**
      * Returns the Response object for the given document and attachment id. Use {@link Response#getInputStream()} to get the input stream
      * of the attachment and when you're done with it call {@link Response#destroy()} to clean up.
      *
      * @param docId             document id
      * @param attachmentId      attachment id
      * @return
      */
     public Response getAttachmentResponse(String docId, String attachmentId)
     {
         Response resp = server.get("/" + name + "/" + encodeURL(docId) + "/" + attachmentId);
         if (resp.getCode() == 404)
         {
             throw new NotFoundException("attachment not found", resp);
         }
         else if (!resp.isOk())
         {
             throw new DataAccessException("error getting attachment '" + attachmentId + "' of document '"+docId + "': ", resp);
         }
         return resp;
     }
 
 
     /**
      * @param documents
      * @return
      */
    public List<DocumentInfo> bulkDeleteDocuments(List<? extends Document> documents)
     {
         return this.bulkDeleteDocuments(documents, false);
     }
 
 
     /**
      * Delete a set of documents.
      * 
      * @param documents
      */
    public List<DocumentInfo> bulkDeleteDocuments(List<? extends Document> documents,
         boolean allOrNothing)
     {
         List<Document> docsToDelete = new ArrayList<Document>();
        for (Document doc : documents)
         {
             BaseDocument proxy  = new BaseDocument();
            proxy.setId(doc.getId());
            proxy.setRevision(doc.getRevision());
             proxy.setProperty("_deleted", true);
             
             for (DatabaseEventHandler eventHandler : eventHandlers)
             {
                 try
                 {
                     eventHandler.deletingDocument(this, proxy.getId(), proxy.getRevision());
                 }
                 catch (Exception e)
                 {
                     throw new DatabaseEventException(e);
                 }
             }
             
             docsToDelete.add(proxy);
         }
         
         
         List<DocumentInfo> documentInfos = this.bulkCreateDocuments(docsToDelete, allOrNothing);
         
         for (DocumentInfo info : documentInfos)
         {
             if (info.getError() != null)
             {
                 for (DatabaseEventHandler eventHandler : eventHandlers)
                 {
                     try
                     {               
                         eventHandler.deletedDocument(this, info.getId(), info.getRevision(), null);
                     }
                     catch (Exception e)
                     {
                         throw new DatabaseEventException(e);
                     }
                 }
             }
         }
         
         return documentInfos;
     }
     
     /**
      * Query the given show function with the given doc id 
      * 
      * @param showName  Name of list including design doc (e.g. "designDocId/showName")
      * @param docId     document id 
      * @param options
      * @return
      */
     public Response queryShow(String showName, String docId, Options options)
     {
         String uri = "/" + name + "/" + getDesignURIFromNameAndInfix(showName, SHOW_DOCUMENT_INFIX) + "/" + encodeURL(docId);
         
         if (options != null)
         {
             uri += options.toQuery();
         }
         
         return server.get(uri);
     }
 
     /**
      * Queries the specified list function with the specified view.
      * 
      * @param listName  Name of list including design doc (e.g. "designDocId/viewName")  
      * @param viewName  view name without design document
      * @param options   
      * @return
      */
     public Response queryList(String listName, String viewName, Options options)
     {
         String uri = "/" + name + "/" + getDesignURIFromNameAndInfix(listName, LIST_DOCUMENT_INFIX) + "/" + encodeURL(viewName);
         
         if (options != null)
         {
             uri += options.toQuery();
         }
         
         return server.get(uri);
     }
 }
