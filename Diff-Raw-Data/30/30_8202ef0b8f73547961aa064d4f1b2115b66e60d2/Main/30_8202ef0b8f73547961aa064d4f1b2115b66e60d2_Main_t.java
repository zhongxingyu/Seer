 package principal;
 
 import java.io.IOException;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 
 public class Main {
 
 	/**
 	 * Punto de entrada al programa
 	 * @param args
 	 * @throws IOException 
 	 * @throws SAXException 
 	 * @throws ParserConfigurationException 
 	 */
 	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
 		
		Sistema s = Sistema.getInstancia();
 		
		try {
			
			s.inicializar("CAJA1", "ficheroVenta.xml", "pruebaEntrada.xml");
			s.run();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 			
 	}
 
 }
