 package despacho.backend.servicios;
 
 import java.util.*;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.*;
 import javax.jws.*;
 
 import despacho.backend.administradores.*;
 import despacho.backend.entities.*;
 import despacho.backend.utils.Configuracion;
 import despacho.backend.utils.Logger;
 import despacho.backend.utils.MensajeAsincronico;
 import despacho.backend.utils.MensajeSincronicoRest;
 import despacho.backend.utils.MensajeSincronicoWS;
 
 @Stateless
 @WebService
 public class ServicioOrdenesDespachoBean implements ServicioOrdenesDespacho {
 	
 	@EJB
 	private AdministradorOrdenesDespacho administradorOrdenesDespacho;
 	
 	@EJB
 	private AdministradorDepositos administradorDepositos;
 	
 	@EJB
 	private AdministradorArticulos administradorArticulos;
 
 	@Override
 	@WebMethod
 	// DCH02. Logistica ingresa nuevas ordenes de despacho
 	public void ingresarOrdenDespacho(OrdenDespacho ordenDespacho) {
 		Logger.info("Nueva Orden de despacho: " + ordenDespacho.getCodigo());
 		
 		// Se deben registrar como pendientes de entrega
 		ordenDespacho.setEstado(EstadoOrdenDespacho.PENDIENTE_ENTREGA);
 		
 		// Por cada articulo de la orden, se debe obtener el Deposito que lo administra y solicitarlo asincronicamente
 		List<ArticuloOrdenDespacho> articulosOrden = ordenDespacho.getArticulos();
 		
 		if (articulosOrden != null) {
 			for (ArticuloOrdenDespacho articuloOrden : articulosOrden) {
 				String codigoArticulo = articuloOrden.getCodigo();
 				Articulo articulo = this.administradorArticulos.get(codigoArticulo);
 				
 				if (articulo == null) {
 					Logger.error("El articulo con codigo " + codigoArticulo + " no existe.");
 					break;
 				}
 				
 				// Obtengo el deposito asociado al articulo
 				String nombreDeposito = articulo.getDeposito().getNombre();
 				
 				try {
 					// TODO: ver el error al enviar el mensaje a la queue
					Logger.info("Solicitando articulo " + articulo.getCodigo() + " al deposito " + nombreDeposito + "...");
 					
 					// Solicitar articulo al deposito
 					MensajeAsincronico.EnviarObjeto(
 							Configuracion.getInstancia().get().get(nombreDeposito + "-SolicitarArticuloQueue-Url"),
 							Configuracion.getInstancia().get().get(nombreDeposito + "-SolicitarArticuloQueue-Nombre"), 
 							Configuracion.getInstancia().get().get(nombreDeposito + "-SolicitarArticuloQueue-Usuario"),
 							Configuracion.getInstancia().get().get(nombreDeposito + "-SolicitarArticuloQueue-Password"), 
							articuloOrden);
 				} catch (Exception e) {
 					e.printStackTrace();
 					Logger.error(e.getMessage());
 				}
 				
 				// TODO: Se debe registrar la solicitud por Deposito
 			}
 		}
 		
 		this.administradorOrdenesDespacho.agregar(ordenDespacho);
 		
 		Logger.info("Listo (DCH02)!");
 	}
 	
 	@Override
 	// DCH04. Envo Cambio de Estado de Despacho (Entrega)
 	public void completarOrdenDespacho(String codigo) {
 		Logger.info("Completar Orden de Despacho: " + codigo);
 		
 		OrdenDespacho orden = this.administradorOrdenesDespacho.get(codigo);
 		if (orden == null) {
 			Logger.error("La orden de despacho con codigo " + codigo + " no existe.");
 			return;
 		}
 		
 		// Informar en comunicacin sincrnica (WS) a los Portales
 		for (String nombrePortal: Configuracion.getInstancia().getPortales()) {
 			Logger.info("Informando al portal " + nombrePortal + " que la orden de despacho fue completada...");
 			
 			MensajeSincronicoWS.Enviar(
 					Configuracion.getInstancia().get().get(nombrePortal + "-OrdenDespachoListaWS-Url"), 
 					null); // TODO: ver que objeto enviar
 		}
 		
 		// Informar en comunicacin sincrnica (REST) al mdulo Logstica
 		Logger.info("Informando a Logistica que la orden de despacho fue completada...");
 		
 		MensajeSincronicoRest.Enviar(
 				Configuracion.getInstancia().get().get("Logistica-OrdenDespachoListaRest-Url"), 
 				null); // TODO: ver que objeto enviar
 		
 		// El sistema debe registrar y cambiar de estado a la Orden de Despacho y marcarla como entregada
 		orden.setEstado(EstadoOrdenDespacho.ENTREGADA);
 		this.administradorOrdenesDespacho.actualizar(orden);
 		
 		Logger.info("Listo (DCH04)!");
 	}
 	
 	@PostConstruct
 	public void inicializacion () {
 		// Inicializar Depositos
		Deposito depositoA = new Deposito(Configuracion.getInstancia().get().get("DepositoA-Nombre"));
		Deposito depositoB = new Deposito(Configuracion.getInstancia().get().get("DepositoB-Nombre"));
 		this.administradorDepositos.agregar(depositoA);
 		this.administradorDepositos.agregar(depositoB);
 		
 		// Inicializar articulos (esto se va a hacer en DCH01)
 		Articulo articulo1 = new Articulo("articulo01", depositoA);
 		Articulo articulo2 = new Articulo("articulo02", depositoA);
 		Articulo articulo3 = new Articulo("articulo03", depositoB);
 		this.administradorArticulos.agregar(articulo1);
 		this.administradorArticulos.agregar(articulo2);
 		this.administradorArticulos.agregar(articulo3);
 	}
 }
