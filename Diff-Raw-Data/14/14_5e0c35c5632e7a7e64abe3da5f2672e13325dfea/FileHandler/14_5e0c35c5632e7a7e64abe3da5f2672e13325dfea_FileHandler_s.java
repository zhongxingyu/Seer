 package org.openshift.lucene;
 
 import java.io.File;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ApplicationScoped;
 import javax.inject.Named;
 
 import org.apache.lucene.index.DirectoryReader;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.SimpleFSDirectory;
 
 @Named
 @ApplicationScoped
 public class FileHandler {
 	
	private String indexLocation = "/home/spousty/git/lucenespatial/src/main/webapp/WEB-INF/indexDir";
 	
 	IndexSearcher indexSearcher = null;
 
 	public FileHandler() {
 		super();
 		System.out.println("Constructor");
 		
 	}
 	
 	@PostConstruct
 	public void afterCreate(){
 		
 		try {
 			Directory directory = new SimpleFSDirectory(new File(indexLocation));
 			IndexReader indexReader = DirectoryReader.open(directory);
 			this.indexSearcher = new IndexSearcher(indexReader);	
 			System.out.println("something about indexsearcher:: " + indexSearcher.toString());
 		} catch (Exception e){
 			System.out.println("Exception Opening directory:" + e.getClass() + " :: " + e.getMessage());			
 		}
 		
 	}
 
 	public IndexSearcher getIndexSearcher() {
 		return this.indexSearcher;
 	}
 	
 	public String getMessage(){
 		return "whole lotta good";
 	}
 	
 	
 	
 
 }
