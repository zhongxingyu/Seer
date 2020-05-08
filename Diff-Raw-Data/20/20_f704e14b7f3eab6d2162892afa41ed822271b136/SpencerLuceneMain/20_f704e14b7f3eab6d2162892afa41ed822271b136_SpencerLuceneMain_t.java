 package edu.uwsp.lucene;
 
import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FilenameFilter;
import java.io.InputStreamReader;
 import java.util.Vector;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.apache.lucene.document.Document;
 import org.xml.sax.InputSource;
 import org.xml.sax.XMLReader;
 
 public class SpencerLuceneMain {
 
 	/**
 	 * @param args
 	 * @throws Exception 
 	 */
 	public static void main(String[] args) throws Exception{
 		
 		// build fileArray
 		File dir = new File("data/");
 		Vector<File> fileArray = (new FileTraversal()).traverse(dir, ".xml");
     	
		BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
 		
 		SAXParserFactory pfactory = SAXParserFactory.newInstance();
 		pfactory.setValidating(false);
 		pfactory.setNamespaceAware(true);
 		SAXParser parser = pfactory.newSAXParser();
 		XMLReader reader = parser.getXMLReader();
 		LuceneSaxParser splitter = new LuceneSaxParser();
 		reader.setContentHandler(splitter);
 		Vector<Document> docVector = new Vector<Document>();
 		
 		// fileArray will be null if there is no .xml files found
 		if(fileArray != null)
 		{
 			for (File file : fileArray) {
 				reader.parse(new InputSource(new FileReader(file)));
 				Document doc = splitter.getDoc();
 				splitter.clearDoc();
 				docVector.add(doc);
 				//System.out.println(doc.getFields().size());
 			}
 		}
 		// Test docVector
 		for (Document document : docVector) {
 			System.out.println(document.getFields().size());
 		}
		
		System.out.print("Tag to find: ");	
		String test = keyboard.readLine();
		System.out.print("Value to find: ");	
		String value = keyboard.readLine();
		System.out.println(test + ": " + value);	
 	}
 
 }
