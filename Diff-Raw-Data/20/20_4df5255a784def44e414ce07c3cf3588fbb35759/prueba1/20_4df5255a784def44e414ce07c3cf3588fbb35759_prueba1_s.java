 package pruebas;
 
 import java.io.IOException;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 
 import principal.Catalogo;
import principal.EntradaXML;
 import principal.Sistema;
 
 public class prueba1 {
 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 * @throws SAXException 
 	 * @throws ParserConfigurationException 
 	 */
 	public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
 		
 		
 		Sistema sistema = Sistema.getInstancia();
 		sistema.inicializar("CAJA1","ficheroVenta.xml","ficheroTicket.xml");
 		
 		//DirectorEntrada de = new DirectorEntrada(new EntradaXML("ficheroVenta.xml"));
 		
 		Catalogo c = Catalogo.getInstancia();
 		/*for (int i=0;i<c.getProductos().size();i++)
 		{
 			System.out.print(c.getProductos().get(i).getCodigo()+"\n");
 		}*/
 		if (c.existeProducto("003057")) //Producto existente pero no en memoria.
 		{
 			System.out.print("TRUE\n");
 		}
 		else
 		{
 			System.out.print("FALSE\n");
 		}
 		
 		System.out.print(c.getProductos().get(0).getCodigo()+"\n"); //Debera aparecer el nuevo aqu sustituyendo a 001633
 		System.out.print(c.getProductos().get(1).getCodigo()+"\n");
 		System.out.print(c.getProductos().get(2).getCodigo()+"\n");
 		
 		if (c.existeProducto("001633")) //Producto recien eliminado de memoria.
 		{
 			System.out.print("TRUE\n");
 		}
 		else
 		{
 			System.out.print("FALSE\n");
 		}
 		
 		System.out.print(c.getProductos().get(0).getCodigo()+"\n");
 		System.out.print(c.getProductos().get(1).getCodigo()+"\n"); //Debera aparecer aqu.
 		System.out.print(c.getProductos().get(2).getCodigo()+"\n");
 		
 		System.out.print(c.getProducto("001668").getDescripcion()+" PVP: "+c.getProducto("001668").getPvp()+"\n");
 		
 		if (!c.existeProducto("RASS-TPV")) //Producto no existente
 		{
 			System.out.print("TRUE\n");
 		}
 		else
 		{
 			System.out.print("FALSE\n");
 		}
 		
 		System.out.print(c.getProductos().get(0).getCodigo()+"\n");
 		System.out.print(c.getProductos().get(1).getCodigo()+"\n");
 		System.out.print(c.getProductos().get(2).getCodigo()+"\n"); //No debera irse el 001668.
 
 	}
 
 }
