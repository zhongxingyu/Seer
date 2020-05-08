 /**
  * 
  */
 package xmlHandling;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.Iterator;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 
 import analytics.analyzer.handlers.Repository;
 import analytics.analyzer.handlers.XMLHandler;
 
 /**
  * @author vogias
  * 
  */
 public class FS2XMLInput extends XmlHandlerInput {
 
 	@Override
	public void getInputData(Repository repo) throws SAXException,
			ParserConfigurationException {
 		// TODO Auto-generated method stub
 		Iterator<File> iterator = (Iterator<File>) repo.getXmls().iterator();
 		int j = 0;
 		try {
 			while (iterator.hasNext()) {
				File xml = iterator.next();
 				XMLHandler xmlHandler = new XMLHandler(repo);
 
 				InputStream inS = new FileInputStream(xml);
 
 				xmlHandler.parseDocument(inS);
 				j++;
 			}
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		}
 	}
 }
