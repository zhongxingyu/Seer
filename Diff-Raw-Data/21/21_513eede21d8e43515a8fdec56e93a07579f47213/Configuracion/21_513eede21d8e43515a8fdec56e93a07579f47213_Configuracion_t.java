 package despacho.backend.utils;
 
 import java.util.*;
 
 public class Configuracion {
 	
	public static Map<String, String> get() {
 		// TODO: manipular toda la informacion de configuracion desde una pagina web
		Map<String, String> configuracion = new HashMap<String, String>();
 		
 		// Configuracion de la queue de Deposito "UNO"
 		configuracion.put("Deposito-UNO-Queue-Url", "remote://localhost:4447");
 		configuracion.put("Deposito-UNO-Queue-Nombre", "jms/queue/solicitarArticuloOrdenDespacho");
 		configuracion.put("Deposito-UNO-Queue-Usuario", "test");
 		configuracion.put("Deposito-UNO-Queue-Password", "test123");
				
 		return configuracion;
 	}
 }
