 package despacho.backend.utils;
 
 import java.util.*;
 
 public class Configuracion {
 	// TODO: manipular toda la informacion de configuracion desde una pagina web
 	private static Map<String, String> configuracion = new HashMap<String, String>();
 	private static Configuracion instance = null;
 	
 	private Configuracion() {
 		// Configuracion de DepositoA
 		configuracion.put("DepositoA-SolicitarArticuloQueue-Url", "remote://localhost:4447");
 		configuracion.put("DepositoA-SolicitarArticuloQueue-Nombre", "jms/queue/test");
 		configuracion.put("DepositoA-SolicitarArticuloQueue-Usuario", "test");
 		configuracion.put("DepositoA-SolicitarArticuloQueue-Password", "test123");
 		
 		// Configuracion de DepositoB
 		configuracion.put("DepositoB-SolicitarArticuloQueue-Url", "remote://localhost:4447");
 		configuracion.put("DepositoB-SolicitarArticuloQueue-Nombre", "jms/queue/test");
 		configuracion.put("DepositoB-SolicitarArticuloQueue-Usuario", "test");
 		configuracion.put("DepositoB-SolicitarArticuloQueue-Password", "test123");
 		
 		// Configuracion de Portales
 		configuracion.put("Portales", "PortalA;PortalB");
 		configuracion.put("PortalA-OrdenDespachoListaWS-Url", "https://192.168.X.XXX/ordenDespachoLista?wsdl");
 		configuracion.put("PortalB-OrdenDespachoListaWS-Url", "https://192.168.Y.YYY/ordenDespachoLista?wsdl");
 	}
 	
 	public static Configuracion getInstancia() {
 		if (instance == null) {
 			instance = new Configuracion();
 		}
 		
 		return instance;
 	}
 	
 	public String[] getPortales() {
 		return configuracion.get("Portales").split(";");
 	}
 	
 	public Map<String, String> get() {
 		return configuracion;
 	}
 }
