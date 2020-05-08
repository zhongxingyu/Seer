 package classifierTests;
 
 import static org.junit.Assert.*;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.apache.lucene.index.IndexReader;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import twitterClassifier.*;
 
 public class ClassifierObject {
 	private ArrayList<String> testDocs;
 	private String[] coffeeDocs = {
 			"resources/cocoa.txt",
 			"resources/cocoa1.txt",
 			"resources/cocoa2.txt",
 			"resources/coffee.txt",
 			"resources/coffee1.txt"
 	};
 	
 	public String readFileToString(String fileName){
 		try {
 			BufferedReader reader = new BufferedReader(new FileReader(fileName));
 			StringBuffer buf = new StringBuffer();
 			String curLine = "";
 			while((curLine = reader.readLine()) != null){
 				buf.append(curLine);
 			}
 			return buf.toString();
 		} catch (FileNotFoundException e) {
 			System.out.println("File "+ fileName + " Not found");
 			return null;
 		} catch (IOException e) {
 			System.out.println("Caught IO Exception in readFileToString");
 			return null;
 		}
 	}
 	@Before
 	public void loadTests(){
 		testDocs = new ArrayList<String>();
 		for(String s : coffeeDocs){
 			testDocs.add(readFileToString(s));
 		}
 	}
 	
 	@After
 	public void after(){
 		
 	}
 
 	@Test
 	public void testClassify() {
 		LuceneClassifier classifier = new LuceneClassifier("resources/coffee-test-set.txt", "positive", false, false );
 		try {
 			System.out.println(classifier.classify(testDocs.get(0)));
 			System.out.println(classifier.classify(testDocs.get(1)));
 			System.out.println(classifier.classify(testDocs.get(2)));
 			System.out.println(classifier.classify(testDocs.get(3)));
 			System.out.println(classifier.classify(testDocs.get(4)));
			IndexReader reader = IndexReader.open(classifier.getIndex());
			System.out.println(reader.numDocs());
 			
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		assertEquals(2,2);
 	}
 
 }
