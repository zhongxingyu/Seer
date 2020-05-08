 package de.haas.searchandfind.backend;
 
 import de.haas.searchandfind.backend.filesource.FileWrapper;
 import de.haas.searchandfind.backend.filesource.FileWatcher;
 import de.haas.searchandfind.backend.filesource.FileLister;
 import de.haas.searchandfind.backend.documentgenerator.DocumentFactory;
 import java.io.File;
 import java.io.IOException;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.util.Version;
 
 /**
  *
  * @author Michael Haas <haas@cl.uni-heidelberg.de>
  */
 public class Indexer extends Thread {
 
     // TODO: sensible value
     private static final int MAX_FIELD_LENGTH = 500;
     private BlockingQueue<FileWrapper> queue = new LinkedBlockingQueue<FileWrapper>();
     // TODO: is an IndexWriter threadsafe? Or do we need a singleton here?
     // "An IndexWriter creates and maintains an index. "
     private Directory directory;
     private File targetDir;
 
     public Indexer(File targetDirectory, Directory indexDirectory) {
         this.targetDir = targetDirectory;
         this.directory = indexDirectory;
 
     }
 
     @Override
     public void run() {
         try {
             this.kickOffIndexing();
         } catch (IOException ex) {
             Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
         } catch (InterruptedException ex) {
             Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private void kickOffIndexing() throws IOException, InterruptedException {
 
         Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);
         IndexWriter.MaxFieldLength mfl = new IndexWriter.MaxFieldLength(MAX_FIELD_LENGTH);
         IndexWriter writer = new IndexWriter(this.directory, analyzer, mfl);
 
         // TODO: this is somewhat race-y. Ideally, we'd start the FileWatcher once
         // all files are read by the FileLister. On the other hand, we might miss
         // newly created files in this case.
         FileLister lister = new FileLister(this.queue, this.targetDir);
         lister.start();
         FileWatcher watcher = new FileWatcher(this.queue, this.targetDir);
         watcher.start();
         // loop endlessly.
         // possibly add support for poison element in queue later on
         // OTOH, it's quite possible we want to keep indexing with the live
         // indexer forever and ever
         // TODO: filter out known documents
 
 
         while (true) {
         //for (int i = 0; i < 1; i++) {
 
             //System.out.println(i);
             File file = this.queue.take().getFile();
             Logger.getLogger(Indexer.class.getName()).log(Level.INFO, "Creating Document for file " + file.getCanonicalPath());
             Document document = DocumentFactory.getDocument(file);
             if (document == null) {
                Logger.getLogger(Indexer.class.getName()).log(Level.SEVERE, "Document as returned by Factory is null. Skipping");
                 continue;
             }
             writer.addDocument(document);
             writer.commit();
         }
         //writer.close();
     }
 }
