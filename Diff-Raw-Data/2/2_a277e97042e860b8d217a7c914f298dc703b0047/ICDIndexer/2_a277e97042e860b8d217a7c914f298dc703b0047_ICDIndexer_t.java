 package no.ntnu.tdt4215.group7.indexer;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import no.ntnu.tdt4215.group7.entity.ICD;
 
 import org.apache.lucene.analysis.no.NorwegianAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.FieldType;
 import org.apache.lucene.document.StringField;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.util.Version;
 
 /**
  * Index icd codes in Lucene
  * */
 public class ICDIndexer implements Indexer {
 
     /**
      * path where to store the index
      * */
     private String filePath;
     /**
      * list of icd codes to be indexed
      * */
     private List<ICD> icds;
 
     public ICDIndexer(String filePath, List<ICD> icds) {
         this.filePath = filePath;
         this.icds = icds;
     }
 
     /*
      * Index ICD objects on Lucene
      */
     public Directory createIndex() throws IOException {
         NorwegianAnalyzer analyzer = new NorwegianAnalyzer(Version.LUCENE_40);
         Directory index = FSDirectory.open(new File(filePath)); // disk index
         IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_40, analyzer);
         IndexWriter w = new IndexWriter(index, config);
         // add ICD codes
         for (ICD icd : this.icds) {
             this.addICDDoc(w, icd);
         }
         w.close();
         return index;
     }
 
     /**
      * In the Lucene doc relative to the icd file we index and stem the label
      * together with the extra information like synonyms The extra information
      * is used for query expansion
      **/
     private void addICDDoc(IndexWriter w, ICD icd) throws IOException {
         String codecompacted = icd.getCode_compacted();
         String label = icd.getLabel();
         // at the momemnt the extra information is given by the underterm and by
         // the synonyms
         String extraInformation = icd.getUnderterm();
         for (String syn : icd.getSynonyms()) {
             extraInformation += " " + syn;
         }
 
         Document doc = new Document();
         FieldType type = new FieldType();
         type.setIndexed(true);
         type.setStored(true);
         type.setStoreTermVectors(true);
         type.setTokenized(true);
         Field fieldLabel = new Field("label", label, type);
         Field fieldExtra = new Field("extra", label + " " + extraInformation, type);
 
         doc.add(fieldLabel);
 
         // use a string field for because we don't want it tokenized
         doc.add(new StringField("code_compacted", codecompacted, Field.Store.YES));
 
        if (extraInformation != null && !extraInformation.equals("")) {
             doc.add(fieldExtra);
         }
         w.addDocument(doc);
     }
 
     @Override
     public Directory call() throws Exception {
         return createIndex();
     }
 }
