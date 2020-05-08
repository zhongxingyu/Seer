 import java.awt.Image;
 import java.io.File;
 import java.io.IOException;
 
 import javax.swing.ImageIcon;
 import javax.xml.XMLConstants;
 import javax.xml.transform.Source;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 import javax.xml.validation.Validator;
 
 import org.xml.sax.SAXException;
 
 /**
  * Valideaza un fisier dat ca input.
  * 
  * @author Unknown-Revengers
 * 
  */
 public class FileValidator {
 
 	/**
	 * Verifica daca e un fisier valid (imagine JPG sau fisier XML formatat
	 * conform cu layout specifications).
 	 * 
 	 * @param fileName Calea fisierului pentru input (JPG sau XML).
 	 * 
 	 * @return boolean
 	 */
 	public static boolean isValid(String fileName) {
 
 		// Verifica extensie fisier
 		String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1);
 
 		// Daca e JPG atunci se verifica imaginea
 		if (fileExt.equalsIgnoreCase("jpg") || fileExt.equalsIgnoreCase("jpeg")) {
 			return validImage(fileName);
 		}
 
 		// Daca e XML atunci se verifica sa fie respectat XSD-ul de layout
 		if (fileExt.equalsIgnoreCase("xml")) {
 			return validXML(fileName);
 		}
 
 		// Daca e altceva return false
 		return false;
 	}
 
 	/**
	 * Verifica daca XML-ul dat este OK din pct de vedere al layoutului.
 	 * 
	 * @param XMLPath Calea catre fisierul XML analizat.
 	 * 
 	 * @return boolean
 	 */
 	private static boolean validXML(String XMLPath) {
		/* Verifica XML in baza unui XSD */
		String xsdPath = Config.output_schemas + "\\layout_specs.xsd";
 		Source xsdFile = new StreamSource(new File(xsdPath));
 		Source xmlFile = new StreamSource(new File(XMLPath));
		SchemaFactory schemaFactory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
 		try {
 			Schema schema = schemaFactory.newSchema(xsdFile);
 			Validator validator = schema.newValidator();
 			validator.validate(xmlFile);
 			return true;
 		} catch (SAXException | IOException e) {
 			return false;
 		}
 	}
 
 	/**
	 * Verifica daca imaginea data este valida (am vazut ca exista diverse
	 * moduri de a face asta).
 	 * 
 	 * @param imagePath Calea catre imaginea JPG analizata.
 	 * 
 	 * @return boolean
 	 */
 	private static boolean validImage(String imagePath) {
 		Image image = new ImageIcon(imagePath).getImage();
 		if (image.getWidth(null) == -1) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 }
