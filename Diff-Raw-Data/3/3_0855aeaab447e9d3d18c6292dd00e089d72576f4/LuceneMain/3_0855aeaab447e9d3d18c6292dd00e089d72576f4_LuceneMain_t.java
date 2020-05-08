 package edu.uwsp.lucene;
 
 import java.io.FileReader;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.WhitespaceAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.store.Directory;
 import org.xml.sax.InputSource;
 import org.xml.sax.XMLReader;
 
 public class LuceneMain {
 
 	/**
 	 * @param args
 	 * @throws Exception 
 	 */
 	public static void main(String[] args) throws Exception{
 		String indexDir = "data/";
 		String file = "department.xml";
 		SAXParserFactory pfactory = SAXParserFactory.newInstance();
 		pfactory.setValidating(false);
 		pfactory.setNamespaceAware(true);
 		SAXParser parser = pfactory.newSAXParser();
 		XMLReader reader = parser.getXMLReader();
 		LuceneSaxParser splitter = new LuceneSaxParser();
 		reader.setContentHandler(splitter);
 		reader.parse(new InputSource(new FileReader(file)));
 		Document doc = splitter.getDoc();
 		
 		//System.out.println(doc.getFields().size());
 		Analyzer anal = new WhitespaceAnalyzer();
 		Directory d = new Directory();
 		IndexWriter iw = new IndexWriter(d, anal,true, 200);
 		
 	}
 
 }
