 package matching;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Scanner;
 import java.util.Set;
 
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.store.LockObtainFailedException;
 import org.apache.lucene.store.SimpleFSDirectory;
 import org.apache.lucene.util.Version;
 
 import wrappers.Options;
 
 public class Lucene {
 	
 	public static void buildIndex(Options opt) throws IOException{
 		Scanner in = new Scanner(new File(opt.FREEBASE));
 		
 		int size = 0;
 		while(in.hasNextLine()){
 			size++;
 			in.nextLine();
 		}
 		
 		in = new Scanner(new File(opt.FREEBASE));
 		Set<String> dict = new HashSet<String>();
 		int cnt = 0;
 		
 		while(in.hasNextLine()){
 			String ent = in.nextLine().split("\t")[2];
 			if(!dict.contains(ent)){
 				dict.add(ent);
 			}
 				
 			String[] parts = Utils.cleanString(ent).split("( |_|-|,)");
 			for(String s : parts){
 				if(!dict.contains(s) && s.length() > 3){
 					dict.add(s);
 				}
 			}
 			cnt++;
 			if(cnt > size * 0.25)
 				break;
 		}
 		writeIndex(dict);
 	}
 	
	private static void writeIndex(Set<String> dict) throws CorruptIndexException, LockObtainFailedException, IOException {
 		IndexWriter indexWriter = new IndexWriter(new SimpleFSDirectory(new File("index")), new IndexWriterConfig(Version.LUCENE_31, new StandardAnalyzer(Version.LUCENE_31)));
 		
 		for(String word : dict){
 			Document document = new Document();
 			
 			document.add(new Field("entity", word, Field.Store.YES, Field.Index.ANALYZED));
 			
 			indexWriter.addDocument(document);
 		}
 		
 		indexWriter.optimize();
 		indexWriter.close();
 	}
 }
