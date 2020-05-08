 package com.where.atlas.feed.citysearch;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.lucene.analysis.WhitespaceAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.Field.Index;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriter.MaxFieldLength;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.NIOFSDirectory;
 
 import com.where.commons.feed.citysearch.CSListing;
 import com.where.commons.feed.citysearch.CSListingDocumentFactory;
 import com.where.commons.feed.citysearch.Category;
 
 public class FixCatsInLis
 {
     public static void main(String [] args) throws IOException
     {
         Directory directory = new NIOFSDirectory(new File("/idx/lis"));
         IndexReader reader = IndexReader.open(directory);
         Directory newDirectory = new NIOFSDirectory(new File("/idx/fixed_cat_lis"));
         IndexWriter writer = new IndexWriter(newDirectory, new WhitespaceAnalyzer(),true, MaxFieldLength.UNLIMITED);
         
         long md = reader.maxDoc();
         StringBuilder catNames = new StringBuilder();
         StringBuilder catIds =  new StringBuilder();
 
         for(int i = 0; i < md; i++)
         {
             Document doc = reader.document(i);
             CSListing listing = CSListingDocumentFactory.createCSListing(doc, i);
             
             List<Category> cats = listing.getCategories();
             int cnt = 0;
             for(Category cat : cats)
             {
                 if(cnt > 0) {catNames.append(",");}
                 catNames.append(cat.getName());
                 if(cnt > 0) {catIds.append(",");}
                 catIds.append(cat.getId());
		cnt++;
             }
             
             doc.add(new Field("catnames", catNames.toString(), Field.Store.YES, Index.ANALYZED_NO_NORMS));
             doc.add(new Field("catids", catNames.toString(), Field.Store.YES, Index.ANALYZED_NO_NORMS));
             catNames.delete(0, catNames.length());
             catIds.delete(0, catNames.length());
             writer.addDocument(doc);
         }
         reader.close();
         writer.optimize();
         writer.close();
     }
 }
