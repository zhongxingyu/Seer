 package news.index;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.ansj.lucene4.AnsjAnalysis;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.StringField;
 import org.apache.lucene.document.TextField;
 import org.apache.lucene.document.Field.Store;
 import org.apache.lucene.index.DirectoryReader;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.queryparser.classic.ParseException;
 import org.apache.lucene.queryparser.classic.QueryParser;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.util.Version;
 
 import util.ChineseSplit;
 
 
 
 import db.data.Article;
 import db.data.Word;
 
 public class ArticleTitleIndex extends Index{
 	
 	private String IndexPath ;
 	
 	public ArticleTitleIndex(String path){
 		this.IndexPath = path;
 	}
 	
 	public Article checkSameInIndex(Article at){
 		
 		Article result = null;
 		double score = -1;
 		List<Word> words = ChineseSplit.SplitStrWithPos(at.getTitle());
 		StringBuilder tmpQuery = new StringBuilder();
 		for(Word wd : words){
 			tmpQuery.append(wd.getName() +" ");				
 		}		
 		//no useful information of title
 		if(tmpQuery.length() == 0){
 			return null;
 		}
 		ArticleTitleIndex ati = new ArticleTitleIndex(IndexPath);
 		List<Article> sims = ati.search(tmpQuery.toString(), false);	
 		for(Article tmpat : sims){
 			if(tmpat.getId() == at.getId())
 				continue;
 			///article index only contains id and title.
 			/// use id to get article from db
 			Article tmpatdb = util.Util.getArticleById(tmpat.getId());
 			if(tmpatdb != null){
				if(tmpatdb.getTitle().equals(at.getTitle())){
					result = tmpatdb;
					break;
				}		
 				score = util.Similarity.ContentOverlap(tmpatdb.getContent(),at.getContent());
				if(score > 0.85){
					result = tmpatdb;
					break;
				}
 			}
 		}
 		return result;
 	}
 	
 	
 	public void update(List<Article> scrs){
 		File file = new File(IndexPath);
 		if(!file.exists()){
 			file.mkdir();
 		}
 		IndexWriter iwriter = new Index().CreateWriter(IndexPath);
 		for(Article at : scrs){
 			try {
 	        	Document doc = new Document();
 	   		 	doc.add(new StringField("id", at.getId().toString(), Store.YES));  
 	            doc.add(new TextField("title", at.getTitle(), Store.YES));  
 				iwriter.addDocument(doc);					
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		try {
 			iwriter.commit();
 			iwriter.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		scrs = null;
 	}
 	
 	public void update(Article at){
 		///for init of the index
 		File file = new File(IndexPath);
 		if(!file.exists()){
 			file.mkdir();
 		}
 		IndexWriter iwriter = new Index().CreateWriter(IndexPath);
 		try {
         	Document doc = new Document();
    		 	doc.add(new StringField("id", at.getId().toString(), Store.YES));  
             doc.add(new TextField("title", at.getTitle(), Store.YES));  
 			iwriter.addDocument(doc);					
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		try {
 			iwriter.commit();
 			iwriter.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	
 	 /** 
      * 查询 
      * @throws Exception 
      */       
 	public List<Article> search(String text,boolean single){  
 
     	Directory dir;
         List<Article> results = new ArrayList<Article>();
         ///check if there is the index
 		File file = new File(IndexPath);
 		if(!file.exists()){
 			return results;
 		}
 		try {
 			dir = FSDirectory.open(new File(IndexPath));
 			IndexReader reader=DirectoryReader.open(dir);  
 	        IndexSearcher searcher=new IndexSearcher(reader);  
 	        TopDocs topdocs = null;
 	        if(single){
 		        Term term=new Term("title", text);  
 		        TermQuery query=new TermQuery(term);
 		        topdocs=searcher.search(query, 0);  
 	        }else{
 	        	AnsjAnalysis aas = new AnsjAnalysis();
 	  			QueryParser parser = new QueryParser(Version.LUCENE_40, "title", aas);
 	  			Query query = parser.parse(text);         
	            topdocs=searcher.search(query, 500);
 	        }    
 	        ScoreDoc[] scoreDocs=topdocs.scoreDocs;       
 	        for(int i=0; i < scoreDocs.length; i++) {  
 	            int doc = scoreDocs[i].doc;  
 	            Document document = searcher.doc(doc);
 	            String id = document.get("id");
 	            String title = document.get("title");	
 				Article at = new Article();
 				at.setId(Integer.valueOf(id));
 				at.setTitle(title);
 				results.add(at);
 	        }  
 	        reader.close();  
 		} catch (IOException e) {
 			return results;
 		} catch (ParseException e) {
 			return results;
 		} 
 		return results;
     }
 	
 
 }
