 package principal;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 
 import utilidades.*;
 
 public class Sistema {
 
 	private static Sistema sistema=null;
 	
 	private ArrayList<Venta> ventas;
 	private ArrayList<Comando> comandos;
 	private ComandoFactory factoria;
 	private PuenteSalida salida;
 	private CajaRegistradora caja;
 	
 	
 	public static Sistema getInstancia()
 	{
 		if(sistema==null)
 		{
 			sistema = new Sistema();
 		}
 		
 		return sistema;
 	}
 
 	private Sistema(){}
 	
 	/**
 	 * Este mtodo es el encargado de cargar las propiedades y la lista de catlogos
 	 * @throws IOException Posibles excepciones
 	 * @throws SAXException 
 	 * @throws ParserConfigurationException 
 	 */
 	public void inicializar(String idCaja,String ent,String sal) throws IOException, ParserConfigurationException, SAXException
 	{
 		
 		ventas = new ArrayList<Venta>();
 		caja = new CajaRegistradora(idCaja);
 		factoria =  new ComandoFactory(ent);
 		salida = new PuenteSalida(sal);
 		
 	}
 	
 	/**
 	 * Crea una nueva venta (asociada con su caja), la aade a su lista de ventas y 
 	 * devuelve la venta para poder usarla en "anyadirLinVenta". 
 	 * 
 	 * @param c
 	 * @return
 	 */
 	public Venta crearNuevaVenta()
 	{
 
 		Venta v = new Venta(caja);
 		ventas.add(v);
 		
 		return v;
 	}
 	
 	public void cancelarVenta()
 	{
 		ventas.set(ventas.size()-1, null);
 	}
 	
 	private float subtotal(Venta v)
 	{
 		return ventas.get(ventas.size()-1).subtotal();
 	}
 	
 	/**
 	 * He pensado que alguien que quiera aadir una linea de venta querr poner en una venta
 	 * cierta cantidad de un producto que ha pasado por el lector de cdigos o ha puesto manualmente.
 	 * En cualquier caso, devuelve la descripcin de dicho producto y el subtotal que lleva la venta 
 	 * hasta el momento o un mensaje de error si no reconoce el producto.
 	 * 
 	 * @param cod
 	 * @param cantidad
 	 * @param v
 	 * @return
 	 * @throws IOException
 	 */
 	public void anyadirLinVenta(String cod, int cantidad)
 	{
 		try {
 			if (Catalogo.getInstancia().existeProducto(cod))
 			{
 				Producto p = Catalogo.getInstancia().getProducto(cod);
 				LinVenta lv = new LinVenta(p,cantidad);
 				ventas.get(ventas.size()-1).anyadirLinVenta(lv);
 				
 				System.out.print(p.getDescripcion()+" \n "+subtotal(ventas.get(ventas.size()-1)));
 				//return p.getDescripcion()+" \n "+subtotal(ventas.get(ventas.size()-1));
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		System.out.print("ERROR: PRODUCTO NO CATALOGADO");
 		//return "ERROR: PRODUCTO NO CATALOGADO";
 	}
 	
 	public void deshacerLinVenta()
 	{
 		ventas.get(ventas.size()-1).deshacerLinVenta();
 	}
 	
 	//ESTA FUNCION NO ESTA TERMINADA
 	/**
 	 * No se si es correcto o no, pero supongo que la venta siempre ser una, es decir, que se inicializa antes
 	 * de llegar aqu y aqu tan slo debemos ocuparnos de aadir nuevas lneas a la venta actual, que ser
 	 * la ltima venta, no?
 	 */
 	/*public boolean anyadirLinVenta() throws IOException
 	{
 		ventas.get(ventas.size() - 1);
 		String linea = entrada.getLinVenta();
 		boolean res=false;
 		
 		
 		if(linea == "cancelarVenta"){
 			//Deshacer la ltima venta
 			ventas.remove(ventas.size() - 1);
 		} else if(linea == "deshacerLinVenta"){
 			//Se deshace la ltima lnea
 			if(ventas.get(ventas.size() - 1).getLinventas().size()>0){
 				ventas.get(ventas.size() - 1).getLinventas().remove(ventas.get(ventas.size() - 1).getLinventas().size() - 1);
 			}
 		} else if(linea.length()>0){
 			//Una lnea normal
 			String[] campos = linea.split("&&");
 			if(catalogo.existeProducto(campos[0])){
 				Producto p = catalogo.getProducto(campos[0]);
 				ventas.get(ventas.size() - 1).anyadirLinVenta(new LinVenta(p, Integer.parseInt(campos[1])));
 			}
 		}
 		/*if(lv!=null)
 			{
 				Venta v = ventas.get(ventas.size()-1);
 			
 				if(catalogo.existeProducto(lv.getProducto().getCodigo()))
 					{
 					v.anyadirLinVenta(lv);
 					ventas.set(ventas.size()-1,v);
 					res=true;
 					}
 			}*/
 		
 		 //return res;
 		
 	//}
 	
 	public void cerrarVenta(Venta v)
 	{
 		// TODO calcular descuentos e impuestos
		salida.generarTicket(ventas.get(ventas.size()-1));
 	}
 	
 	/*public void crearTicket(Venta v, String fichero) throws IOException{
 		salida = new SalidaXML(fichero);
 		
 		salida.setVenta(v);
 	}*/
 	
 	public void run()
 	{
 		int ultimo = comandos.size(); //Por si hubiesen comandos de antes.
 		comandos.addAll(factoria.getComando());
 		for (int i=ultimo; i<comandos.size(); i++)
 		{
 			comandos.get(i).ejecutar();
 		}
 	}
 	
 }
