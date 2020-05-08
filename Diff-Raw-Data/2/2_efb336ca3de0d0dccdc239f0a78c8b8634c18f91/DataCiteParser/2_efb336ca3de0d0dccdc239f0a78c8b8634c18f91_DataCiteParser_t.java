 package dk.rootuser.daa.parsers;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.xml.sax.SAXException;
 
 import dk.rootuser.daa.handlers.DataCiteHandler;
 import dk.rootuser.daa.pojos.datacite.Resource;
 
 public class DataCiteParser {
 
 	private static DataCiteParser instance = null;
 	
 	private SAXParser parser;
 	private DataCiteHandler handler;
 	
 	public DataCiteParser() throws ParserConfigurationException, SAXException {
 		SAXParserFactory factory = SAXParserFactory.newInstance();
 		this.parser = factory.newSAXParser();
 		this.handler = new DataCiteHandler();
 	}
 	
 	public static DataCiteParser getInstance() throws ParserConfigurationException, SAXException {
 		if(instance == null)
 			instance = new DataCiteParser();
 		
 		return instance;
 	}
 	
 	public Resource parse(String filePath) throws SAXException, IOException {
 		File xml = new File(filePath);
 		
 		if(!xml.exists())
			throw new FileNotFoundException("Couldn't find " + xml.getAbsoluteFile());
 		
 		parser.parse(xml, handler);
 		
 		return this.handler.getParsedResource();
 	}
 	
 }
