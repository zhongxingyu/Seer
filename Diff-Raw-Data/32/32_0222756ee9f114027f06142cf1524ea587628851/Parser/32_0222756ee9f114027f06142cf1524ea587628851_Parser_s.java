 import javax.xml.parsers.FactoryConfigurationError;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import org.xml.sax.SAXException;
 import org.xml.sax.Attributes;
 import org.xml.sax.helpers.DefaultHandler;
 import java.io.BufferedWriter;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 import java.util.ArrayList;
 
 public class Parser {
 
 	/*Suggested structure:
 	* - Parser class which performs initial streaming in of sgm files
 	* - Tokenizer class which performs stem removal/other text processing to give root words
 	* - Classes that then represent each text mining model
 	* - Classes that run classification algorithms on the document representations
 	* - (optional) implementation of the mapreduce framework via apache hadoop.
 	*/
 	
 	private static String dirpath;
 	private static int sgmCount;
 	
 	public static void main (String[] args) {
 		
 		//this main will eventually be abstracted out to a handler class
 		
 		//file path for reuters files
 		dirpath = "reuters21578/reut2-0";
 
 		//has this file been converted from SGML to our preferred XML yet? 
 		File f = new File(dirpath + args[0] + ".xml");
 		if(!f.exists()) { 
 			loadSGMFile(args[0]);
 		}
 		
 		//call file load
 		parseXMLFile(args[0]);
 	}
 	
 	public static void parseXMLFile(String filenum) {
 	
 		//Loads the requested XML file and splits out the desired tags that we are
 		//interested in. This will eventually go to a tokenizer and is stored as
 		//in a document object.
 		
		ReutersDocument document = new ReutersDocument();
		
 		List<ReutersDocument> documentCollection = new ArrayList<ReutersDocument>();
		
 		SAXParserFactory factory = SAXParserFactory.newInstance();
 		factory.setNamespaceAware(true);
 		
 		//SAX Handler callback functions.
 		DefaultHandler handler = new DefaultHandler() {
 		
 			boolean title = false;
 			boolean dateline = false;
 			boolean body = false;
 			boolean endofdoc = false;
 			String bodytext;
 		
 			public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
  
 				if (qName.equalsIgnoreCase("title")) {
 					title = true;
 				}
  
 				if (qName.equalsIgnoreCase("dateline")) {
 					dateline = true;
 				}
  
 				if (qName.equalsIgnoreCase("body")) {
 					body = true;
 				}
  
 			}	
 			
 			public void characters(char ch[], int start, int length) throws SAXException {
  
 				if (title) {
 					System.out.println("Document title : " + new String(ch, start, length));
 					title = false;
 				}
  
 				if (dateline) {
 					System.out.println("Document date : " + new String(ch, start, length));
 					dateline = false;
 				}
  
 				if (body) {
 					bodytext = bodytext + new String(ch, start, length);
 				}
  
 			}
 			
 			public void endElement(String uri, String localName, String qName) throws SAXException {
  
 				if (qName.equalsIgnoreCase("reuters")) {
 					endofdoc = true;
 				}
 				
 				if (qName.equalsIgnoreCase("body")) {
 					System.out.println("Document body: " + bodytext);
 					body = false;
 					bodytext="";
 				}
  
 			}
 		
 		};
 	
 		try {
 	
 			SAXParser parser = factory.newSAXParser();
 			parser.parse(dirpath+filenum+".xml", handler);
 			System.out.println("(PARSER): Parsing complete");
 		
 		} catch (Exception e) {
 		
 			System.out.println(e.toString());
 		
 		}
 	
 	
 	}
 	
 	public static void loadSGMFile(String sgmNumber) {
 	
 		//Loads the requested SGM file, removing noise and converting into
 		//a well formed XML file that exists in the reuters21578 subdirectory.
 		//Essentially a deprecated method now that we have clean xml files.
 		
 		SAXParserFactory factory = SAXParserFactory.newInstance();
 		factory.setNamespaceAware(true);
 		DefaultHandler handler = new DefaultHandler();
 	
 		try {
 			
 			//1) First add a root node as file isn't well formed.
 			String ln;
 		    SAXParser parser = factory.newSAXParser();
 			
 			//a)Temp file
 			File file = new File(dirpath+sgmNumber+".xml");
 			file.createNewFile();
 			
 			//b)Load sgm file
 			File sgmFile = new File(dirpath+sgmNumber+".sgm");
 			BufferedReader br = new BufferedReader(new FileReader(dirpath+sgmNumber+".sgm"));
 			
 			//c)Write to file
 			FileWriter fw = new FileWriter(file.getAbsoluteFile());
 			BufferedWriter bw = new BufferedWriter(fw);
 			bw.write("<ReutersRoot>");
 			
 			while ((ln = br.readLine()) != null) {
 				if(!ln.contains("DOCTYPE")) {
 					bw.write(ln.replaceAll("&#","")+"\n");
 				} 
 			}
 			
 			bw.write("</ReutersRoot>");
 		
 			bw.flush();
 			bw.close();
 			
 			parser.parse(dirpath+sgmNumber+".xml", handler);
 			System.out.println("(PARSER): Parsing complete");
 			
 		
 		} catch (Exception e) {
 			
 			System.out.println(e.toString());
 		
 		}
 	
 	}
 
 }
