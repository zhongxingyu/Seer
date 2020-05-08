 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lab2.test;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Date;
import lab2.indexing.MyAnalyser;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.util.Version;
 
 /**
  *
  * @author Cyril
  */
 /**
  * Index all text files under a directory. <p> This is a command-line
  * application demonstrating simple Lucene indexing. Run it with no command-line
  * arguments for usage information.
  */
 public class LuceneCustom {
     
     
     
     /**
      * Index all text files under a directory.
      */
     public static void main(String[] args) {
         String usage = "java org.apache.lucene.demo.IndexFiles"
                 + " [-index INDEX_PATH] [-docs DOCS_PATH] [-update]\n\n"
                 + "This indexes the documents in DOCS_PATH, creating a Lucene index"
                 + "in INDEX_PATH that can be searched with SearchFiles";
         String indexPath = "index";
         String docsPath = null;
         boolean create = true;
         for (int i = 0; i < args.length; i++) {
             switch (args[i]) {
                 case "-index":
                     indexPath = args[i + 1];
                     i++;
                     break;
                 case "-docs":
                     docsPath = args[i + 1];
                     i++;
                     break;
                 case "-update":
                     create = false;
                     break;
             }
         }
 
         if (docsPath == null) {
             System.err.println("Usage: " + usage);
             System.exit(1);
         }
 
         final File docDir = new File(docsPath);
         if (!docDir.exists() || !docDir.canRead()) {
             System.out.println("Document directory '" + docDir.getAbsolutePath() + "' does not exist or is not readable, please check the path");
             System.exit(1);
         }
 
         Date start = new Date();
         try {
             System.out.println("Indexing to directory '" + indexPath + "'...");
 
             Directory dir = FSDirectory.open(new File(indexPath));
             Analyzer analyzer = new MyAnalyser(Version.LUCENE_40);
             //Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
 
             IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40, analyzer);
 
             // iwc.setSimilarity(new TestSimilarity());
 
 
             if (create) {
                 // Create a new index in the directory, removing any
                 // previously indexed documents:
                 iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
             } else {
                 // Add new documents to an existing index:
                 iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
             }
             try (IndexWriter writer = new IndexWriter(dir, iwc)) {
                 // NOTE: if you want to maximize search performance,
                 // you can optionally call forceMerge here.  This can be
                 // a terribly costly operation, so generally it's only
                 // worth it when your index is relatively static (ie
                 // you're done adding documents to it):
                 //
                 // writer.forceMerge(1);
 
                 indexDocs(writer, docDir);
 
             }
 
             Date end = new Date();
             System.out.println(end.getTime() - start.getTime() + " total milliseconds");
 
         } catch (IOException e) {
             System.out.println(" caught a " + e.getClass()
                     + "\n with message: " + e.getMessage());
         }
     }
 
     /**
      * Indexes the given file using the given writer, or if a directory is
      * given, recurses over files and directories found under the given
      * directory.
      *
      * NOTE: This method indexes one document per input file. This is slow. For
      * good throughput, put multiple documents into your input file(s). An
      * example of this is in the benchmark module, which can create "line doc"
      * files, one document per line, using the <a
      * href="../../../../../contrib-benchmark/org/apache/lucene/benchmark/byTask/tasks/WriteLineDocTask.html"
      * >WriteLineDocTask</a>.
      *
      * @param writer Writer to the index where the given file/dir info will be
      * stored
      * @param file The file to index, or the directory to recurse into to find
      * files to index
      * @throws IOException If there is a low-level I/O error
      */
     static void indexDocs(IndexWriter writer, File file)
             throws IOException {
         // do not try to index files that cannot be read
         if (file.canRead()) {
             if (file.isDirectory()) {
                 String[] files = file.list();
                 // an IO error could occur
                 if (files != null) {
                     for (int i = 0; i < files.length; i++) {
                         indexDocs(writer, new File(file, files[i]));
                     }
                 }
             } else {
 
                 FileInputStream fis;
                 try {
                     fis = new FileInputStream(file);
                 } catch (FileNotFoundException fnfe) {
                     // at least on windows, some temporary files raise this exception with an "access denied" message
                     // checking if the file can be read doesn't help
                     return;
                 }
 
                 try {
 
                     Analyzer analyzer = writer.getAnalyzer();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
 
                     //analyzer = new StandardAnalyzer(Version.LUCENE_40);
                     TokenStream tokenStream = analyzer.tokenStream("contents", reader);
                     CharTermAttribute termAtt = tokenStream.addAttribute(CharTermAttribute.class);
 
                     tokenStream.reset();
                     while (tokenStream.incrementToken()) {
                         
                     }
                     
                     
                     System.out.println("another doc" );
                 } finally {
                     fis.close();
                 }
             }
         }
     }
 }
