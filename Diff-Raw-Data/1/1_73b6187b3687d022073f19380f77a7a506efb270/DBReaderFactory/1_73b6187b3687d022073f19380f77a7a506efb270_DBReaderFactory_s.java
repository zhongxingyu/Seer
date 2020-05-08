 package database;
 
 import java.io.*;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 
 
 public class DBReaderFactory {
 	public static enum SOURCE_FORMAT {
 		 TXT,
 		 XML
 	}
 	private DBReaderFactory() {}
 	
 	public static final DBReaderFactory instance = new DBReaderFactory();
 	
 		private Configuration config = Configuration.instance();
 			
 		public Reader getReader() throws IOException, SAXException, ParserConfigurationException{
 		
			if(config.getSourceFormat() == SOURCE_FORMAT.TXT){
 			SOURCE_FORMAT format = config.getSourceFormat();
 			if(format == SOURCE_FORMAT.TXT){
 				return new PlainTextReader();
 				}	else {
 				return new XmlReader();
 				}
 			}
 		
 		
 }
