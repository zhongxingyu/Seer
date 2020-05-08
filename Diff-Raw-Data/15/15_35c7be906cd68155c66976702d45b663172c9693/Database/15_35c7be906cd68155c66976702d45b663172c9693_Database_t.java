 package org.jcouchdb.db;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.jcouchdb.document.AbstractViewResult;
 import org.jcouchdb.document.DesignDocument;
 import org.jcouchdb.document.DocumentHelper;
 import org.jcouchdb.document.DocumentInfo;
 import org.jcouchdb.document.ViewAndDocumentsResult;
 import org.jcouchdb.document.ViewResult;
 import org.jcouchdb.exception.DataAccessException;
 import org.jcouchdb.exception.NotFoundException;
 import org.jcouchdb.exception.UpdateConflictException;
 import org.jcouchdb.util.Assert;
 import org.svenson.JSON;
 import org.svenson.JSONParser;
 
 /**
  * Contains the main interface of working with a couchdb database
  *
  * @author shelmberger
  *
  */
 public class Database
 {
     private static final String VIEW_DOCUMENT_PREFIX = "_view/";
 
     private JSON jsonGenerator = new JSON();
 
     static final String VIEW_QUERY_VALUE_TYPEHINT = ".rows[].value";
     private static final String VIEW_QUERY_DOCUMENT_TYPEHINT = ".rows[].doc";
 
     protected static Logger log = Logger.getLogger(Database.class);
 
     /**
      * Name of the all docs view.
      */
     private static final String ALL_DOCS = "_all_docs";
 
     private static final String ALL_DOCS_BY_SEQ = "_all_docs_by_seq";
 
 
     private String name;
 
     private Server server;
 
     private List<DatabaseEventHandler> eventHandlers = new ArrayList<DatabaseEventHandler>();
 
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
         Response resp = server.get("/" + name + "/");
         if (!resp.isOk())
         {
             throw new DataAccessException("error getting database status for database " + name +
                 ": ", resp);
         }
         return resp.getContentAsBean(DatabaseStatus.class);
     }
 
     /**
      * Triggers database compaction.
      *
      */
     public void compact()
     {
         Response resp = server.get("/" + name + "/_compact");
         if (!resp.isOk())
         {
             throw new DataAccessException("error getting database status for database " + name +
                 ": ", resp);
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
 
         String uri = "/" + name + "/" + docId;
         if (revision != null)
         {
             uri += "?rev="+revision;
         }
         Response resp = server.get(uri);
         if (resp.getCode() == 404)
         {
             throw new NotFoundException("document not found", resp);
         }
         else if (!resp.isOk())
         {
             throw new DataAccessException("error getting document " + docId + ": ", resp);
         }
         if (parser != null)
         {
             resp.setParser(parser);
         }
         return resp.getContentAsBean(cls);
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
      * Bulk creates the given list of documents.
      * @param documents
      * @return
      */
     public List<DocumentInfo> bulkCreateDocuments(List<?> documents)
     {
         Assert.notNull(documents, "documents cannot be null");
 
         Map<String,List<?>> wrap = new HashMap<String, List<?>>();
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
         Response resp = server.post("/" + name + "/_bulk_docs", json);
 
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
 
         JSONParser parser = new JSONParser();
         parser.addTypeHint(".new_revs[]", DocumentInfo.class);
         resp.setParser(parser);
         Map m = resp.getContentAsBean(HashMap.class);
 
         if (m.get("ok") != null)
         {
             return (List<DocumentInfo>) m.get("new_revs");
         }
         else
         {
             throw new DataAccessException("Error bulk creating documents", resp);
         }
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
 
         Response response = server.delete("/" + name + "/" + docId+"?rev=" + revision );
 
         for (DatabaseEventHandler eventHandler : eventHandlers)
         {
             eventHandler.deletedDocument(this, docId, revision, response);
         }
 
         if (!response.isOk())
         {
             throw new DataAccessException("Error deleting document", response);
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
         Response resp;
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
             resp = server.put("/" + name + "/" + id, json);
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
         return (ViewResult<V>)queryViewInternal(VIEW_DOCUMENT_PREFIX + viewName, cls, null, options, parser, null);
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
         return (ViewAndDocumentsResult<V,D>)queryViewInternal(VIEW_DOCUMENT_PREFIX + viewName, valueClass, documentClass, options, parser, null);
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
 
         Response resp = server.post(uri,fn);
         if (!resp.isOk())
         {
             throw new DataAccessException("error querying view", resp);
         }
 
         if (parser == null)
         {
             parser = new JSONParser();
         }
         parser.addTypeHint(VIEW_QUERY_VALUE_TYPEHINT, cls);
         resp.setParser(parser);
         return resp.getContentAsBean(ViewResult.class);
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
 
         Response resp;
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
 
         if (parser == null)
         {
             parser = new JSONParser();
         }
         parser.addTypeHint(VIEW_QUERY_VALUE_TYPEHINT, valueClass);
         resp.setParser(parser);
 
         if (isDocumentQuery)
         {
             parser.addTypeHint(VIEW_QUERY_DOCUMENT_TYPEHINT, documentClass);
             return resp.getContentAsBean(ViewAndDocumentsResult.class);
         }
         else
         {
             return resp.getContentAsBean(ViewResult.class);
         }
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
         return (ViewResult<V>)queryViewInternal(VIEW_DOCUMENT_PREFIX + viewName, cls, null, options, parser, m);
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
         return (ViewAndDocumentsResult<V,D>)queryViewInternal(VIEW_DOCUMENT_PREFIX + viewName, valueClass, documentClass, options, parser, m);
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
         Response resp = server.put(attachmentURI(docId, revision, attachmentId) , data, contentType);
 
         if (!resp.isOk())
         {
             throw new DataAccessException("Error creating attachment",resp);
         }
         Map<String,String> m = resp.getContentAsMap();
         return m.get("rev");
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
         Response resp = server.put(attachmentURI(docId, revision, attachmentId) , data, contentType);
 
         if (!resp.isOk())
         {
             throw new DataAccessException("Error updating attachment",resp);
         }
         Map<String,String> m = resp.getContentAsMap();
         return m.get("rev");
     }
 
     private String attachmentURI(String docId, String revision, String attachmentId)
     {
         String uri = "/" + name + "/" + docId + "/" + attachmentId;
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
         Response resp = server.delete(attachmentURI(docId, revision, attachmentId));
         if (!resp.isOk())
         {
             throw new DataAccessException("Error deleting attachment",resp);
         }
         Map<String,String> m = resp.getContentAsMap();
         return m.get("rev");
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
         Response resp = server.get("/" + name + "/" + docId + "/" + attachmentId);
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
 }
