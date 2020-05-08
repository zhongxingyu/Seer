 package com.where.atlas.feed;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 
 import org.apache.lucene.analysis.SimpleAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.NumericField;
 import org.apache.lucene.document.Field.Index;
 import org.apache.lucene.document.Field.Store;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.index.IndexWriter.MaxFieldLength;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.NIOFSDirectory;
 
 import com.where.commons.util.StringUtil;
 import com.where.place.Place;
 
 public class LocalezeCollectAndIndex implements PlaceCollector
 {
     private LocalezeParserUtils parserutils;
     private BufferedWriter err;
     
     
     private String idRaw;
     private File index;
     private Directory directory;
     private IndexWriter writer;
     private IndexSearcher searcher;
     
     
     private long collectorCounter;
 
     
     
     LocalezeCollectAndIndex(LocalezeParserUtils Lparserutils)
     {
         collectorCounter=0;
         parserutils = Lparserutils;
         
         
         
         String logFile = parserutils.getCompanyFilesPath() + "/errs.txt";
         
         try{
         err = new BufferedWriter(new FileWriter(logFile));
         }
         catch(Exception e){
             System.err.println("Error setting up logFile");
         }
         
         try{
             idRaw = parserutils.getIndexPath();
             index = new File(idRaw);
             index.mkdir();
             directory = new NIOFSDirectory(index);
             writer = new IndexWriter(directory, new SimpleAnalyzer(), true, MaxFieldLength.UNLIMITED);
             writer.setMergeFactor(100000);
             writer.setMaxMergeDocs(Integer.MAX_VALUE);        
             
             searcher = new IndexSearcher(new NIOFSDirectory(new File(parserutils.getIndexPath() + "/cmpcat")));
             System.out.println("Indexing...");
         }
         catch(Exception e)
         {
             System.err.println("Error setting up main index");
         }
        
     }
     
     public void closeAll() throws Exception
     {
         writer.optimize();
         writer.close();
         searcher.close();
         directory.close();
         err.close();
     }
     
     public void collect(Place place)
     {
         Document doc = new Document();
         
         long pid = Long.parseLong(place.getNativeId());
         
         doc.add(new Field("pid", place.getNativeId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
         doc.add(new Field("rawname", place.getShortname(), Field.Store.YES, Field.Index.NOT_ANALYZED));
         doc.add(new Field("companyname", place.getName(), Field.Store.YES, Field.Index.ANALYZED_NO_NORMS));
         doc.add(new Field("whereid", place.getWhereId(), Field.Store.YES, Field.Index.NOT_ANALYZED));
         
         String streetAddress = place.getAddress().getAddress1()+" "+place.getAddress().getAddress2(); 
 
 
         doc.add(new Field("address", streetAddress, Field.Store.YES, Field.Index.ANALYZED));
         doc.add(new Field("city", place.getAddress().getCity(), Field.Store.YES, Field.Index.ANALYZED));
         doc.add(new Field("state", place.getAddress().getState(), Field.Store.YES, Field.Index.ANALYZED));
         doc.add(new Field("zip", place.getAddress().getZip(), Field.Store.YES, Field.Index.ANALYZED));
         
         try{
             TopDocs td = searcher.search(new TermQuery(new Term("pid", Long.toString(pid))), 1);
             ScoreDoc [] sd = td.scoreDocs;
             if(sd.length > 0)
             {
                 Document tmpdoc= searcher.getIndexReader().document(sd[0].doc);
                 String cats  = tmpdoc.get("data");
                 if(!StringUtil.isEmpty(cats))
                 {
                    doc.add(new Field("category", cats, Store.YES, Index.NOT_ANALYZED));                   
                 }
             }
         
 
         //Phone format in cs is no spaces, so we keep it the same for comparison purposes later
         String phone = place.getPhone();
         doc.add(new Field("phone", phone, Field.Store.YES, Field.Index.NOT_ANALYZED));        
                 
         double[] latlng = place.getLatlng();//two element array 0-lat  1-lng
         
         doc.add(new NumericField("latitude_range",  Store.YES,true).setDoubleValue(latlng[0]));   
         doc.add(new NumericField("longitude_range" , Store.YES,true).setDoubleValue(latlng[1]));
 
         String geohash = place.getGeohash();
         doc.add(new Field("geohash", geohash, Field.Store.YES, Field.Index.NOT_ANALYZED));
         
         writer.addDocument(doc);
         
         if(++collectorCounter % 500000 == 0) writer.optimize();
         
         
         displayProgress();
         }
         catch(Throwable e){
             System.out.println("Error indexing!");
             e.printStackTrace();
         }
     }
 
     //log bad input to err.txt
     public void collectBadInput(Object input, Exception reason) 
     {
         try{
             err.newLine();                
             err.write(input + " reason: " + reason.getMessage());
             err.newLine(); 
             }
             catch(Exception e){
                 System.out.println("Error writing logFile entry.");
             }
     }
     
     private void displayProgress()
     {
         if(collectorCounter % 5000 == 0) System.out.print("+");
         if(collectorCounter % 200000 == 0) System.out.println(" ~"+
                 new Double((collectorCounter/13800000.0)*100).toString()+"%");
     }
 }
