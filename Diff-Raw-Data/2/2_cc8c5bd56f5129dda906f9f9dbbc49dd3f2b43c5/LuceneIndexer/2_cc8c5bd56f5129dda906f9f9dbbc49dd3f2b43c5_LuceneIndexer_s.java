 package org.wyona.yarep.impl.search.lucene;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.StringReader;
 import java.io.StringWriter;
 
 import org.apache.avalon.framework.configuration.Configuration;
 import org.apache.log4j.Logger;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.tika.parser.Parser;
 import org.apache.tika.sax.BodyContentHandler;
 import org.wyona.yarep.core.Node;
 import org.wyona.yarep.core.Property;
 import org.wyona.yarep.core.Repository;
 import org.wyona.yarep.core.search.Indexer;
 import org.wyona.yarep.core.search.Metadata;
 import org.wyona.yarep.core.search.SearchException;
 import org.wyona.yarep.impl.repo.vfs.VirtualFileSystemNode;
 import org.wyona.yarep.impl.repo.vfs.VirtualFileSystemRepository;
 import org.wyona.yarep.impl.search.lucene.LuceneConfig;
 
 /**
  * Lucene implementation of indexer
  */
 public class LuceneIndexer implements Indexer {
     
     static Logger log = Logger.getLogger(LuceneIndexer.class);
     private static final String LOCK = "lock";
     private static enum INDEX_TYPE {FULLTEXT, PROPERTIES};
     public static final String INDEX_PROPERTY_FULL = "_FULLTEXT";
     public static final String INDEX_PROPERTY_YAREPPATH = "_PATH";
     
     protected LuceneConfig config;
 
     /**
      * @see org.wyona.yarep.core.search.Indexer#configure(Configuration, File, Repository)
      */
     public void configure(Configuration searchIndexConfig, File configFile, Repository repo) throws SearchException {
         this.config = new LuceneConfig(searchIndexConfig, configFile.getParent(), repo);
     }
     
     /**
      * @see org.wyona.yarep.core.search.Indexer#index(Node)
      */
     public void index(Node node) throws SearchException {
         if (log.isDebugEnabled()) {
             try {
                 log.debug("Index fulltext of node: " + node.getPath());
             } catch(Exception e) {
                 log.warn(e, e);
             }
         }
         index(node, (Metadata)null);
     }
     
     /**
      * @see org.wyona.yarep.core.search.Indexer#index(Node, Metadata)
      */
     public void index(Node node, Metadata metaData) throws SearchException {
         try {
             log.debug("Trying to index fulltext of node: " + node.getPath());
             org.apache.tika.metadata.Metadata tikaMetaData = new org.apache.tika.metadata.Metadata();
             if (metaData != null) {
                 log.warn("This indexer implementation '" + getClass().getName() + "' is currently not making use of the meta data argument!");
             }
             String mimeType = node.getMimeType();
             if (mimeType != null) {
                 log.debug("Mime type: " + mimeType);
                 if (log.isDebugEnabled()) log.debug("Mime type: " + mimeType);
 
                 
                 // http://wiki.apache.org/lucene-java/LuceneFAQ#head-917dd4fc904aa20a34ebd23eb321125bdca1dea2
                 // http://mail-archives.apache.org/mod_mbox/lucene-java-dev/200607.mbox/%3C092330F8-18AA-45B2-BC7F-42245812855E@ix.netcom.com%3E
                 //indexWriter.deleteDocuments(new org.apache.lucene.index.Term(INDEX_PROPERTY_YAREPPATH, node.getPath()));
                 //log.debug("Number of deleted documents (" + node.getPath() + "): " + numberOfDeletedDocuments);
                     
                 String fullText = getFulltext(node, mimeType, tikaMetaData);
                 if (fullText != null && fullText.length() > 0) {
                     Document luceneDoc = getDocument(node.getPath());
 
                     luceneDoc.add(new Field(INDEX_PROPERTY_FULL, new StringReader(fullText))); // INFO: http://lucene.apache.org/java/2_0_0/api/org/apache/lucene/document/Field.html#Field(java.lang.String,%20java.io.Reader)
                     //luceneDoc.add(new Field(INDEX_PROPERTY_FULL, fullText, Field.Store.NO, Field.Index.TOKENIZED));
 
                     try {
                         updateDocument(INDEX_TYPE.FULLTEXT, node.getPath(), luceneDoc);
                     } catch(org.apache.lucene.store.LockObtainFailedException e) {
                         log.warn("Could not init fulltext IndexWriter (maybe because of existing lock), hence content of node '" + node.getPath() + "' will not be indexed!");
                     }
                 } else {
                     log.warn("No fulltext has been extracted to index node with mimeType " + mimeType + " (node: " + node.getPath() + ")");
                 }
             } else {
                 log.warn("Node '" + node.getPath() + "' has no mime-type set and hence will not be added to fulltext index.");
             }
         } catch (Exception e) {
             log.error(e, e);
             throw new SearchException(e.toString());
         }
     }
     
     /**
      * @see org.wyona.yarep.core.search.Indexer#removeFromIndex(org.wyona.yarep.core.Node)
      */
     public void removeFromIndex(Node node) {
         IndexWriter indexWriter = null;
         VirtualFileSystemRepository vfsRepo = ((VirtualFileSystemNode) node).getRepository();
         String nodePath = "Could not get Path of node.";
         synchronized (LOCK) {
             try {
                 nodePath = node.getPath();
                 indexWriter = createFulltextIndexWriter();
                 indexWriter.deleteDocuments(new org.apache.lucene.index.Term(INDEX_PROPERTY_YAREPPATH, node.getPath()));
                 indexWriter.close();
                 
             } catch(Throwable e) {
                 log.warn("Could not init IndexWriter, because of existing lock, hence content of node '" + nodePath + "' will not be deleted from the index!");
             } finally {
                 try {
                     indexWriter.close();
                 } catch (Throwable e2) {
                 }
             }
         }
     }
     
     /**
      * Get index writer
      */
    protected IndexWriter createFulltextIndexWriter() throws Exception {
         log.debug("Fulltext search index directory: " + config.getFulltextSearchIndexFile());
         return createIndexWriter(config.getFulltextSearchIndexFile(), config.getFulltextAnalyzer());
        // IMPORTANT: This doesn't work within a clustered environment!
        //return this.indexWriter;
    }
 
    /**
     * Get index writer
     */
    protected IndexWriter createPropertiesIndexWriter() throws Exception {
        return createIndexWriter(config.getPropertiesSearchIndexFile(), config.getPropertyAnalyzer());
        // IMPORTANT: This doesn't work within a clustered environment!
        //return this.propertiesIndexWriter;
    }
    
     /**
      * Init an IndexWriter
      * @param indexDir Directory where the index is located
      */
     private IndexWriter createIndexWriter(File indexDir, Analyzer analyzer) throws Exception {
        IndexWriter iw = null;
        if (indexDir != null) {
            //TODO: if (indexDir.isDirectory()) is probably not needed, try catch (FileNotFoundException e) should be enough
            if (indexDir.isDirectory()) {
                try {
                    iw = new IndexWriter(indexDir.getAbsolutePath(), analyzer, false);
                } catch (FileNotFoundException e) {
                    //probably it got an instance of the writer and didn't index anything so it's missing the segemnts files. 
                    //create a new index  
                    iw = new IndexWriter(indexDir.getAbsolutePath(), analyzer, true);
                }
            } else {
                iw = new IndexWriter(indexDir.getAbsolutePath(), analyzer, true);
            }
            // TODO: iw.setWriteLockTimeout(long ms)
            //log.debug("Max write.lock timeout: " + iw.getDefaultWriteLockTimeout() + " milliseconds");
            return iw;
        }
        return null;
     }
 
    /**
     * Get properties index reader
     */
    public IndexReader getPropertiesIndexReader() throws Exception {
        if (config.getPropertiesSearchIndexFile().exists() && IndexReader.indexExists(config.getPropertiesSearchIndexFile())) {
            return IndexReader.open(config.getPropertiesSearchIndexFile());
        } else {
            log.warn("No properties index exists yet: " + config.getPropertiesSearchIndexFile().getAbsolutePath());
            return null;
        }
    }
 
     /**
      * @see org.wyona.yarep.core.search.Indexer#index(Node, Property)
      */
     public void index(Node node, Property property) throws SearchException {
         try {
             String path = node.getPath();
             if (config.doIndexRevisions() && org.wyona.yarep.util.YarepUtil.isRevision(node)) {
                 String revisionName = ((org.wyona.yarep.core.Revision)node).getRevisionName();
                 log.debug("Index property '" + property.getName() + " of revision: " + path + " (" + revisionName + "), " + node.getClass().getName());
                 path = path + "#revision=" + revisionName; // TODO: Discuss the separator
             } else {
                 log.debug("Index property '" + property.getName() + " of node: " + path);
             }
 
             Document luceneDoc = getDocument(path);
 
             // TODO: Write typed property value to index. Is this actually possible?
             // INFO: As workaround Add the property as string value to the lucene document
             if (property.getValueAsString() != null) {
                 log.debug("Index property '" + property.getName() + "': " + property.getValueAsString());
                 //luceneDoc.add(new Field(property.getName(), new StringReader(property.getValueAsString())));
                 luceneDoc.add(new Field(property.getName(), property.getValueAsString(), Field.Store.YES, Field.Index.TOKENIZED));
             } else {
                 log.warn("Property '" + property.getName() + "' has null as string value and hence will not be indexed (path: " + path + ")!");
             }
 
             // INFO: Re-add all other properties, whereas either get TermDocs from IndexReader or just use Node.getProperties
             // INFO: Add all other properties of node to lucene doc, whereas this is just a workaround, because the termDocs does not work (please see below)
             Property[] properties = node.getProperties();
             for (int i = 0; i < properties.length; i++) {
                 if (!properties[i].getName().equals(property.getName())) {
                     if (properties[i].getValueAsString() != null) {
                         luceneDoc.add(new Field(properties[i].getName(), properties[i].getValueAsString(), Field.Store.YES, Field.Index.TOKENIZED));
                     }
                 }
             }
             // WARN: For some strange reason the code below throws a NullPointerException
             IndexReader propsIndexReader = getPropertiesIndexReader();
             if (propsIndexReader != null) {
                 log.debug("Number of documents of properties index: " + propsIndexReader.numDocs());
 
 /*
                 org.apache.lucene.index.TermDocs termDocs = indexReader.termDocs(new org.apache.lucene.index.Term(INDEX_PROPERTY_YAREPPATH, path));
                 if (termDocs != null) {
                     log.debug("Number of documents matching term: " + termDocs.doc());
                     termDocs.close();
                 } else {
                     log.warn("No term docs found for path: " + path);
                 }
 */
                 propsIndexReader.close();
             } else {
                 log.warn("Could not init properties index reader!");
             }
 
             // INFO: Now add lucene document containing all properties to index
             try {
                 updateDocument(INDEX_TYPE.PROPERTIES, path, luceneDoc);
             } catch(org.apache.lucene.store.LockObtainFailedException e) {
                 log.warn("Could not init properties IndexWriter (maybe because of existing lock), hence properties of node '" + path + "' will not be indexed!");
             }
 
             // INFO: Add lucene document also to fulltext index
             //log.warn("TODO: Also add lucene document to fulltext index...");
 /*
             try {
                 updateDocument(createFulltextIndexWriter(), path, luceneDoc);
             } catch(org.apache.lucene.store.LockObtainFailedException e) {
                 log.warn("Could not init fulltext IndexWriter (maybe because of existing lock), hence content of node '" + node.getPath() + "' will not be indexed!");
             }
 */
         } catch (Exception e) {
             log.error(e, e);
             throw new SearchException(e.getMessage());
         }
     }
   
     /**
      * @see org.wyona.yarep.core.search.Indexer#removeFromIndex(org.wyona.yarep.core.Node, Property)
      */
     public void removeFromIndex(Node node, Property property) throws SearchException {
         log.warn("TODO: Not implemented yet.");
     }
 
     /**
      * Update document of a particular path within index
      *
     * @param indexWriter Index writer
      * @param path Path of node with which the fields and values are related to
      * @param document Lucene document containing the fields and values
      */
     private void updateDocument(INDEX_TYPE type, String path, Document document) throws Exception {
         IndexWriter indexWriter = null;
         synchronized (LOCK) {
             try {
                 if (type == INDEX_TYPE.FULLTEXT) {
                     indexWriter = createFulltextIndexWriter();
                 } else if (type == INDEX_TYPE.PROPERTIES) {
                     indexWriter = createPropertiesIndexWriter();
                 } else {
                     throw new Exception("No such type: " + type);
                 }
                 if (indexWriter != null) {
                     if (log.isDebugEnabled()) log.debug("Node will be indexed: " + path);
                     indexWriter.updateDocument(new org.apache.lucene.index.Term(INDEX_PROPERTY_YAREPPATH, path), document);
                     indexWriter.close();
                     //indexWriter.flush();
                 } else {
                     log.fatal("IndexWriter is null!");
                     throw new Exception("IndexWriter is null and hence node will not be indexed: " + path);
                     //log.warn("IndexWriter is null and hence node will not be indexed: " + path);
                 }
             } catch (Throwable t) {
                 log.error(t,t);
                 
             } finally {
                 try {
                     indexWriter.close();
                 } catch (Throwable t) {
                 }
             }
         }
     }
 
     /**
      * Init lucene document
      * @param path Node path for which fields and values are associated with
      */
     private Document getDocument(String path) {
         Document luceneDoc = new Document();
         // INFO: Add path as field such that found properties can be related to a path
         luceneDoc.add(new Field(INDEX_PROPERTY_YAREPPATH, path, Field.Store.YES, Field.Index.UN_TOKENIZED));
         return luceneDoc;
     }
 
     /**
      * Get fulltext
      */
     private String getFulltext(Node node, String mimeType, org.apache.tika.metadata.Metadata tikaMetaData) throws Exception {
         String fullText = null;
                     // Extract/parse text content:
                     Parser parser = config.getTikaConfig().getParser(mimeType);
                     if (parser != null) {
                         StringWriter writer = new StringWriter();
                         try {
                             tikaMetaData.set("yarep-path", node.getPath());
                             // The WriteOutContentHandler writes all character content out to the writer. Please note that Tika also contains various other utility classes to extract content, such as for example the BodyContentHandler (see http://lucene.apache.org/tika/apidocs/org/apache/tika/sax/package-summary.html)
                             //parser.parse(node.getInputStream(), new WriteOutContentHandler(writer), tikaMetaData);
                             parser.parse(node.getInputStream(), new BodyContentHandler(writer), tikaMetaData);
                             fullText = writer.toString();
                             // See http://www.mail-archive.com/tika-dev@lucene.apache.org/msg00743.html
                             //log.warn("Fulltext generation with WriteOutContentHandler does seem to be buggy (because title and body are not separated with a space): " + fullText);
 
                             // TODO: Add more meta content to full text
                             String title = tikaMetaData.get(org.apache.tika.metadata.Metadata.TITLE);
                             if (title != null && title.trim().length() > 0) fullText = fullText + " " + title;
 
                             String keywords = tikaMetaData.get(org.apache.tika.metadata.Metadata.KEYWORDS);
                             if (keywords != null && keywords.trim().length() > 0) fullText = fullText + " " + keywords;
 
                             String description = tikaMetaData.get(org.apache.tika.metadata.Metadata.DESCRIPTION);
                             if (description != null && description.trim().length() > 0) fullText = fullText + " " + description;
 
                             //log.debug("debug: Fulltext including title and meta: " + fullText);
                         } catch (Exception e) {
                             log.error("Could not index node " + node.getPath() + ": error while extracting text: " + e, e);
                             // don't propagate exception
                         }
                     } else {
                         log.warn("No parser available to index node with mimeType " + mimeType + " (node: " + node.getPath() + ")");
                     }
         return fullText;
     }
 }
