 package pruebas;
 
 import java.io.IOException;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 
 import principal.Sistema;
 
 public class pruebaEntrada {
 	
	public static void Main(String args[])
 	{
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
