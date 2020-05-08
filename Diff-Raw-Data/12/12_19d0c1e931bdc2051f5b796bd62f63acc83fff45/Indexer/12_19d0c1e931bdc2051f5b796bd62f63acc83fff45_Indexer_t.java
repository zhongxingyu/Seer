 import java.io.*;
 import java.util.*;
 import javax.xml.stream.*;
 import javax.xml.stream.events.*;
 
 import org.apache.lucene.analysis.*;
 import org.apache.lucene.analysis.standard.*;
 import org.apache.lucene.document.*;
 import org.apache.lucene.index.*;
 import org.apache.lucene.index.IndexWriterConfig.*;
 import org.apache.lucene.store.*;
 import org.apache.lucene.util.*;
 
 /**
    Generate a Lucene index for the collection of ACM XML files.
  */
 class Indexer {
   /**
      Return the first article parsed from the current position of the specified
      reader.
    */
  static Document parseArticle(XMLStreamReader r, NumericField pid) throws
      XMLStreamException {
     Document result = new Document();
     result.add(pid);
     while (r.hasNext()) {
       r.next();
       if (r.getEventType() == XMLEvent.START_ELEMENT) {
         if (r.getName().toString().equals("title")) {
           result.add(new Field("title", r.getElementText(), Field.Store.YES,
                                Field.Index.ANALYZED));
         }
         else if (r.getName().toString().equals("subtitle")) {
           result.add(new Field("subtitle", r.getElementText(), Field.Store.YES,
                                Field.Index.ANALYZED));
         }
         else if (r.getName().toString().equals("ft_body")) {
           result.add(new Field("fulltext", r.getElementText(), Field.Store.NO,
                                Field.Index.ANALYZED));
         }
         else if (r.getName().toString().equals("article_id")) {
           result.add(new NumericField("article_id", Field.Store.YES, false).
                      setLongValue(Long.parseLong(r.getElementText())));
         }
       }
      else if (r.getEventType() == XMLEvent.END_ELEMENT &&
               r.getName().toString().equals("article_rec")) {
         return result;
       }
     }
     return result;
   }
   
   /**
      Return a list of Documents parsed from the specified file using the
      specified factory.
    */
  static List<Document> parse(File f, XMLInputFactory factory) throws
      FileNotFoundException, IOException, XMLStreamException {
     List<Document> result = new LinkedList<Document>();
     if (!f.exists() || !f.canRead() || !f.isFile()) {
       return result;
     }
     XMLStreamReader r = factory.createXMLStreamReader(new FileInputStream(f));
     NumericField pid = new NumericField("proc_id", Field.Store.YES, false);
     while (r.hasNext()) {
       r.next();
       if (r.getEventType() == XMLEvent.START_ELEMENT) {
         if (r.getName().toString().equals("proc_id")) {
           pid.setLongValue(Long.parseLong(r.getElementText()));
         }
         else if (r.getName().toString().equals("article_rec")) {
           result.add(parseArticle(r, pid));
         }
       }
       else if (r.getEventType() == XMLEvent.END_ELEMENT) {
         if (r.getName().toString().equals("content")) {
           return result;
         }
       }
     }
     return result;
   }
 
   /**
      Walk the specified directory, indexing files using the specified indexer
      and parsing them using the specified factory.
    */
   static void visit(File d, IndexWriter w, XMLInputFactory factory) {
     if (!d.exists() || !d.canRead() || !d.isDirectory()) {
       throw new IllegalArgumentException();
     }
     for (File f : d.listFiles()) {
       if (f.isDirectory()) {
         visit(f, w, factory);
       }
       else {
         try {
           for (Document doc : parse(f, factory)) {
             w.addDocument(doc);
           }
         }
         catch (Exception e) {
           System.err.format("Error parsing %s: %s\n", f, e.getMessage());
         }
       }
     }
   }
   
   public static void main(String[] args) {
     if (args.length < 1) {
       System.err.println("Usage: java Indexer DIR");
       System.exit(1);
     }
     File in = new File(args[0]);
     if (!in.exists() || !in.isDirectory()) {
       System.err.println("Cannot index non-directory");
       System.exit(1);
     }
     Directory out = null;
     try {
       out = FSDirectory.open(new File("index"));
       Analyzer a = new StandardAnalyzer(Version.LUCENE_36);
       IndexWriterConfig c = new IndexWriterConfig(Version.LUCENE_36, a);
       // Assume re-indexing for simplicity
       c.setOpenMode(OpenMode.CREATE_OR_APPEND);
       IndexWriter w = new IndexWriter(out, c);
       XMLInputFactory factory = XMLInputFactory.newInstance();
       // Simplify getting text of tags by including CDATA
       // factory.setProperty("isCoalescing", true);
       visit(in, w, factory);
       w.close();
     }
     catch (Exception e) {
       e.printStackTrace();
       System.exit(1);
     }
   }
 }
