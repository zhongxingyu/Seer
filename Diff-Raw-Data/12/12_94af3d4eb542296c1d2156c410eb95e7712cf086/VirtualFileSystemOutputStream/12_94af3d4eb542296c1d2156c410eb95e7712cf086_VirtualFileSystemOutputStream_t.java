 package org.wyona.yarep.impl.repo.vfs;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import org.apache.log4j.Category;
 import org.wyona.yarep.core.Node;
 import org.wyona.yarep.core.RepositoryException;
 import org.wyona.yarep.impl.AbstractNode;
 
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.Field.Index;
 import org.apache.lucene.document.Field.Store;
 import org.apache.lucene.index.IndexWriter;
 
 /**
  * OutputStream which sets some properties (lastModified, size) to the node 
  * when the stream is closed.
  * 
  * NOTE: Currently not used, because the Node implemenation uses the lastModified and size
  * of the content file.
  */
 public class VirtualFileSystemOutputStream extends OutputStream {
 
     private static Category log = Category.getInstance(VirtualFileSystemOutputStream.class);
 
     protected OutputStream out;
     protected Node node;
     protected File file;
 
     /**
      * 
      */
     public VirtualFileSystemOutputStream(Node node, File file) throws FileNotFoundException {
         this.node  = node;
         this.out = new FileOutputStream(file);
         this.file = file;
     }
     
     /**
      * 
      */
     public void write(int b) throws IOException {
         out.write(b);
     }
 
     /**
      * 
      */
     public void close() throws IOException {
         out.close();
         try {
             //node.setProperty(AbstractNode.PROPERTY_SIZE, file.length());
             //node.setProperty(AbstractNode.PROPERTY_LAST_MODIFIED, file.lastModified());
 
             String mimeType = node.getMimeType();
             if (mimeType != null) {
                 if (log.isDebugEnabled()) log.debug("Mime type: " + mimeType);
                 VirtualFileSystemRepository vfsRepo = ((VirtualFileSystemNode) node).getRepository();
 
                 File searchIndexFile = vfsRepo.getSearchIndexFile();
                 IndexWriter indexWriter = null;
                 if (searchIndexFile != null) {
                     if (searchIndexFile.isDirectory()) {
                         indexWriter = new IndexWriter(searchIndexFile.getAbsolutePath(), vfsRepo.getAnalyzer(), false);
                     } else {
                         indexWriter = new IndexWriter(searchIndexFile.getAbsolutePath(), vfsRepo.getAnalyzer(), true);
                     }
                 }
                 // http://wiki.apache.org/lucene-java/LuceneFAQ#head-917dd4fc904aa20a34ebd23eb321125bdca1dea2
                 // http://mail-archives.apache.org/mod_mbox/lucene-java-dev/200607.mbox/%3C092330F8-18AA-45B2-BC7F-42245812855E@ix.netcom.com%3E
                 //indexWriter.deleteDocuments(new org.apache.lucene.index.Term("_PATH", node.getPath()));
                //log.debug("Number of deleted documents (" + node.getPath() + "): " + numberOfDeletedDocuments);
 
                 if (indexWriter != null) {
                     Document document = new Document();
                     // TODO: Use Tika to extract text depending on mime type
                     if (mimeType.equals("application/xhtml+xml") || mimeType.equals("application/xml") || mimeType.equals("text/plain") || mimeType.equals("text/html")) {
                          document.add(new Field("_FULLTEXT", new java.io.FileReader(file)));
                          document.add(new Field("_PATH", node.getPath(),Field.Store.YES,Field.Index.UN_TOKENIZED));
                          indexWriter.updateDocument(new org.apache.lucene.index.Term("_PATH", node.getPath()), document);
                          //indexWriter.addDocument(document);
                         if (log.isDebugEnabled()) log.debug("Node will be indexed: " + node.getPath());
                     } else {
                         log.warn("Indexing of mime type '" + mimeType + "' is not supported yet (path: " + node.getPath() + ")!");
                     }
                     indexWriter.close();
                 } else {
                     log.warn("IndexWriter is null and hence node will not be indexed: " + node.getPath());
                 }
             }
         } catch (Exception e) {
             log.error(e, e);
             throw new IOException(e.getMessage());
         }
     }
 }
