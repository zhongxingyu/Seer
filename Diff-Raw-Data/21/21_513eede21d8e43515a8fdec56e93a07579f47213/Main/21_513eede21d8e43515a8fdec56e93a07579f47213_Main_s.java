 import despacho.backend.servicios.*;
 
 
 public class Main {
 
 	public static void main(String[] args) {
 		try {
 			// DCH02.Recepcin y Procesamiento de Orden de Despacho (desde LOGISTICA)
 			ServicioOrdenesDespachoBean port = new ServicioOrdenesDespachoBeanServiceLocator().getServicioOrdenesDespachoBeanPort();
 			
 			Deposito deposito1 = new Deposito();
 			deposito1.setNombre("UNO");
 			
 			ArticuloOrdenDespacho articulo1 = new ArticuloOrdenDespacho();
 			articulo1.setCodigo("articulo-01");
 			articulo1.setDeposito(deposito1);
 			
 			ArticuloOrdenDespacho articulo2 = new ArticuloOrdenDespacho();
			articulo1.setCodigo("articulo-02");
			articulo1.setDeposito(deposito1);
 			
 			OrdenDespacho orden = new OrdenDespacho();
 			orden.setCodigo("orden-despacho-01");
 			orden.setArticulos(new ArticuloOrdenDespacho[] { articulo1, articulo2 });
 			
 			port.ingresarOrdenDespacho(orden);
 			
 			// ASSERTS:
 			//	- Cada articulo de la orden se envio a la queue del DEPOSITO correspondiente
 			//	- La orden se guardo en la DB con el estado "pendientes de entrega"
 			//	- [TODO] Se debe registrar la solicitud por Deposito
 			
 			System.out.println("LISTO!");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
