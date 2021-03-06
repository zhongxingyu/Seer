 package services.notificacion;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.apache.cxf.endpoint.Client;
 import org.apache.cxf.frontend.ClientProxy;
 import org.apache.cxf.transport.http.HTTPConduit;
 import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 
 import com.google.inject.Inject;
 
 import controllers.fap.AgenteController;
 
 import enumerado.fap.gen.EstadoNotificacionEnum;
 import es.gobcan.eadmon.aed.ws.Aed;
 import es.gobcan.eadmon.aed.ws.AedExcepcion;
 import es.gobcan.platino.servicios.enotificacion.notificacion.NotificacionException;
 import es.gobcan.platino.servicios.enotificacion.notificacion.NotificacionPortType;
 import es.gobcan.platino.servicios.enotificacion.notificacion.ResultadoBusquedaNotificacionType;
 import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.ArrayOfArrayResultadoType;
 import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.ArrayOfInteresadoType;
 import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.ArrayOfNotificacionEnvioType;
 import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.ArrayOfNotificacionType;
 import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.DocumentoCreateType;
 import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.DocumentoNotificacionEnumType;
 import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.DocumentoType;
 import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.EstadoNotificacionEnumType;
 import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.EstadoNotificacionType;
 import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.InteresadoType;
 import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.MimeTypeEnumType;
 import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.NotificacionCreateType;
 import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.NotificacionCriteriaType;
 import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.NotificacionEnvioType;
 import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.NotificacionType;
 import es.gobcan.platino.servicios.enotificacion.dominio.notificacion.ResultadoType;
 
 import play.modules.guice.InjectSupport;
 import properties.FapProperties;
 import properties.PropertyPlaceholder;
 
 import models.Agente;
 import models.Documento;
 import models.DocumentoNotificacion;
 import models.Interesado;
 import models.Notificacion;
 import services.GestorDocumentalService;
 import services.NotificacionService;
 import utils.BinaryResponse;
 import utils.NotificacionUtils;
 import utils.WSUtils;
 import utils.XMLGregorianCalendarConverter;
 
 @InjectSupport
 public class NotificacionServiceImpl implements NotificacionService {
 	
 	private final PropertyPlaceholder propertyPlaceholder;
 	
 	protected static es.gobcan.platino.servicios.enotificacion.notificacion.NotificacionService notificacionService;
 	private final NotificacionPortType notificacionPort;
 	
 	private final static String URL_AED = FapProperties.get("fap.aed.url");
 	
 	@Inject
 	GestorDocumentalService gestorDocumental;
 	
 	protected static Logger log = Logger.getLogger(NotificacionServiceImpl.class);
 	
 	private final static String COD_ERROR_NOTIFICACION = "INSERCION_CORRECTA";
 	private final static String URL_WSDL = FapProperties.get("fap.notificaciones.url");
 	private final static String TIPO_DOC_PUESTAADISPOSICION = FapProperties.get("fap.aed.notificacion.tipodocumento.puestaadisposicion");
 	private final static String TIPO_DOC_ANULACION = FapProperties.get("fap.aed.notificacion.tipodocumento.anulacion");
 	private final static String TIPO_DOC_MARCARARESPONDIDA = FapProperties.get("fap.aed.notificacion.tipodocumento.marcararespondida");
 	private final static String TIPO_DOC_ACUSERECIBO = FapProperties.get("fap.aed.notificacion.tipodocumento.acuserecibo");
 	
 	private final static String EXCEPTION_CON_WS = "El gestor %s no pudo conectarse al servicio de notificaciones para realizar %s de la notificación %s. ";
 	private final static String EXCEPTION_DESCONOCIDO = "El gestor %s no pudo realizar la operación de %s para la notificación %s al obtenerse un error desconocido. ";
 	
 	private final static String MSG_CON_WS = "No se pudo conectar con el servicio de notificaciones. ";
 	private final static String MSG_DESCONOCIDO = "Error desconocido. ";
 	
 	private final static String KEY_CONNECTION_TIMEOUT = "fap.notificacion.proxy.connectiontimeout";
     private final static String KEY_RECEIVE_TIMEOUT = "fap.notificacion.proxy.receivetimeout";
 	
 	@Inject
 	public NotificacionServiceImpl (PropertyPlaceholder propertyPlaceholder) {
 		this.propertyPlaceholder = propertyPlaceholder;
 		URL wsdlLocation = null;
         try {
               wsdlLocation = new URL(URL_WSDL);
         } catch (MalformedURLException e) {
               log.error("No se puede inicializar la wsdl por defecto " + URL_WSDL);
         }
         
         try {
 	        notificacionService = new es.gobcan.platino.servicios.enotificacion.notificacion.NotificacionService(wsdlLocation);
         } catch (Exception e) {
         	log.error("No se ha podido injectar el servicio de notificaciones: " + e.getMessage());
         	notificacionPort = null;
         	return;
         }
 	        
         notificacionPort = notificacionService.getNotificacionService();
 			
 		Client client = ClientProxy.getClient(notificacionPort);
 	    HTTPConduit httpConduit = (HTTPConduit) client.getConduit();
 	        
 	    HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
 	    httpClientPolicy.setConnectionTimeout(FapProperties.getLong(KEY_CONNECTION_TIMEOUT));
 	    httpClientPolicy.setReceiveTimeout(FapProperties.getLong(KEY_RECEIVE_TIMEOUT));
 	        
 	    httpConduit.setClient(httpClientPolicy);
  
 	}
 	
 	public boolean isConfigured() {
 		return notificacionPort != null;
 	}
 	
     private String getEndPoint() {
         return propertyPlaceholder.get("fap.notificaciones.url");
     }
 
 	@Override
 	public void crearDocumentoPuestaADisposicion(Notificacion dbNotificacion, List<String> urisDocumentos, List<Interesado> interesados, String descripcion) {
 		String idGestor = AgenteController.getAgente().username; // Se refiere al dni
 		String uriNotificacion = dbNotificacion.uri;
 		
 		try {
 	
 			// El estado de la notificación en el servicio de notificaciones debe ser igual al de la base de datos de fap
 			EstadoNotificacionType estadoNotificacionWS = notificacionPort.obtenerEstadoNotificacion(uriNotificacion);
 			if (estadoNotificacionWS.getEstado() == EstadoNotificacionEnumType.PUESTA_A_DISPOSICION) {
 				log.warn(String.format("Actualizando la notificicación %s al estado de PUESTA A DISPOSICIÓN, debido a un error previo", uriNotificacion));
 				
 				// Se obtiene la uri del documento de notificación
 				String uriDoc = notificacionPort.obtenerURIDocumentoNotificacion("", uriNotificacion, DocumentoNotificacionEnumType.PUESTA_A_DISPOSICION);
 				log.info(String.format("Documento de puesta a disposición (%s) para la notificación (%s)", uriDoc, uriNotificacion));
 				
 				// Se actualizan las propiedades del documento			
 				dbNotificacion.documentoPuestaADisposicion.uri = uriDoc;
 				dbNotificacion.documentoPuestaADisposicion.clasificado = true;
 				dbNotificacion.documentoPuestaADisposicion.save();
 				
 				// Se cambia el estado de la notificación
 				dbNotificacion.estado = EstadoNotificacionEnum.puestaadisposicion.name();
 				log.info(String.format("La notificación (%s) pasa al estado de puesta a disposición", uriNotificacion));
 			
 				dbNotificacion.save();
 			}
 			else {
 				// Se crea el documento de puesta a disposición
 				log.info(String.format("Se crea el documento de puesta a disposición para la notificación (%s) por el gestor (%s)", uriNotificacion, idGestor));
 
 				ArrayOfInteresadoType interesadosType = new ArrayOfInteresadoType();
 				for (Interesado interesado: interesados){
 					interesadosType.getInteresados().add(NotificacionUtils.convertInteresadoToInteresadoType(interesado));
 				}
 				
 				DocumentoType docPuestaADisposicion = notificacionPort.crearDocumentoPuestaADisposicion(urisDocumentos, interesadosType, descripcion, dbNotificacion.idExpedienteAed, this.getUriBackOffice(), this.getUriProcedimiento());
 				
 				// Se inserta el documento en el Aed
 				Documento dbOld = dbNotificacion.documentoPuestaADisposicion;
 				dbNotificacion.documentoPuestaADisposicion = null;
 				dbNotificacion.save();
 				dbOld.delete();
 				dbOld = null;
 
 				Documento dbDocPuestaADisposicion = new Documento();
 				dbDocPuestaADisposicion.tipo = this.getTipoDocPuestaADisposicion();
 				dbDocPuestaADisposicion.descripcion = "Documento \"Puesta a Disposición\" creado por el servicio de notificaciones";
 				dbDocPuestaADisposicion.estadoDocumento = EstadoNotificacionEnum.puestaadisposicion.name();
 				dbDocPuestaADisposicion.uri = gestorDocumental.saveDocumentoTemporal(dbDocPuestaADisposicion, docPuestaADisposicion.getDatos().getInputStream(), UUID.randomUUID().toString() + ".pdf");
 				log.info(String.format("Se guarda el documento de anulación (%s) para la notificación (%s) en la carpeta temporal", dbDocPuestaADisposicion.uri, uriNotificacion));
 
 				dbNotificacion.documentoAnulacion = dbDocPuestaADisposicion;
 				dbNotificacion.documentosAuditoria.add(dbDocPuestaADisposicion);  // Añadir el documento a la collección de documentos de auditoría
 				dbNotificacion.preparadaAnulacion = true;
 				
 				dbDocPuestaADisposicion.save();
 				dbNotificacion.save();
 			}
 		}
 		catch (Exception e) {
 			log.error("No se ha podido crear el documento de puesta a disposición de la notificación: "+ e.getMessage());
 		} 
 	}
 
 	@Override
 	public void enviarNotificaciones(Notificacion notificacion, Agente gestor) throws NotificacionException {
 		String uriNotificacion = notificacion.uri;
 		String idGestor = gestor.username; // Se refiere al dni
 		
 		try {
 			
 			// Conversor de solicitud a objetos del servicio web
 			NotificacionCreateType notificacionCreateType = new NotificacionCreateType();
 			notificacionCreateType.setAsunto(notificacion.asunto);
 			notificacionCreateType.setCuerpo(notificacion.descripcion);
 			notificacionCreateType.setNumeroExpediente(notificacion.idExpedienteAed);
 			notificacionCreateType.setPlazoAcceso(notificacion.plazoAcceso);
 			notificacionCreateType.setPlazoRespuesta(notificacion.plazoRespuesta);
 			notificacionCreateType.setRecordatorioAcceso(notificacion.frecuenciaRecordatorioAcceso);
 			notificacionCreateType.setRecordatorioRespuesta(notificacion.frecuenciaRecordatorioRespuesta);
 			notificacionCreateType.setUriBackoffice(this.getUriBackOffice()); // Especifica la aplicación
 			notificacionCreateType.setUriProcedimiento(this.getUriProcedimiento());
 			notificacionCreateType.setVersionProcedimiento("");
 			
 			// Se añaden los documentos a notificar
 			for (DocumentoNotificacion doc : notificacion.documentosANotificar) {
 				notificacionCreateType.getUrisDocumentosANotificar().add(doc.uri);
 			}
 			
 			// Se añaden los documentos adjuntos
 			for (DocumentoNotificacion doc : notificacion.documentosAnexos) {
 				notificacionCreateType.getUrisDocumentosAdjuntos().add(doc.uri);
 			}
 			
 			NotificacionEnvioType notificacionEnvioType = new NotificacionEnvioType();
 			
 			// Se añaden los datos de la notificación
 			notificacionEnvioType.setDatosNotificacion(notificacionCreateType);
 			
 			// Se añade el documento de envío de notificación
 			notificacionEnvioType.setDocumento(null);
 			
 			ArrayOfInteresadoType interesados = new ArrayOfInteresadoType();
 			for (Interesado in : notificacion.interesados) {
 				if (in.notificar) { // Debe notificarse?
 					InteresadoType interesadoType = NotificacionUtils.convertInteresadoToInteresadoType(in);
 					if (interesadoType != null)
 						interesados.getInteresados().add(interesadoType);
 					else
 						play.Logger.error("Hubo un problema al convertir el interesado: "+in.getId()+" a tipo InteresadoType");
 				}
 			}
 			
 			// Se añade los interesados
 			notificacionEnvioType.setInteresados(interesados);
 			
 			notificacionEnvioType.setUriGestorDocumental(URL_AED);
 			
 			ArrayOfNotificacionEnvioType notificaciones = new ArrayOfNotificacionEnvioType();
 			notificaciones.getNotificacion().add(notificacionEnvioType);
 
 			String mailGestor = gestor.email;
 			
 			// Se envía la notificación
 			play.Logger.info("Se envía la notificación por el gestor: "+idGestor+ " cuyo email es: "+mailGestor);
 			ArrayOfArrayResultadoType enviarNotificacionesResponse = notificacionPort.enviarNotificaciones(notificaciones, idGestor, mailGestor);
 			
 			uriNotificacion = enviarNotificacionesResponse.getArrayResultado().get(0).getResultado().get(0).getUriNotificacion();
 			ResultadoType result0 = enviarNotificacionesResponse.getArrayResultado().get(0).getResultado().get(0);
 			
 			if (!result0.getCodigoError().equalsIgnoreCase(COD_ERROR_NOTIFICACION)) {
 				StringBuilder sb = new StringBuilder();
 				sb.append("No se pudo realizar la notificación debido a los siguientes errores: ");
 				for (ResultadoType result : enviarNotificacionesResponse.getArrayResultado().get(0).getResultado()) {
 					sb.append(String.format("Codigo (%s). ", result.getCodigoError()) + result.getDescripcionError());
 				}
 				log.info(String.format("No se pudo realizar la notificación del gestor (%s) debido a los siguientes errores: ", idGestor) + sb.toString());
 				throw new NotificacionException("No se pudo realizar la notificación debido a los siguientes errores: " + sb.toString());
 			}
 
 			log.info(String.format("Notificación (%s) realizada por el gestor (%s).", uriNotificacion, idGestor));
 			
 			// Se obtiene la uri del documento de notificación
 			String uriDoc = null;
 			try {
 				uriDoc = notificacionPort.obtenerURIDocumentoNotificacion("", uriNotificacion, DocumentoNotificacionEnumType.PUESTA_A_DISPOSICION);
 				log.info(String.format("Documento de puesta a disposición (%s) para la notificación (%s)", uriDoc, uriNotificacion));
 			} catch (Exception e){
 				log.error("Fallo al intentar recuperar la URI del Documento Notificacion. Error: "+e.getMessage());
 				log.error("Ojo, la URI del documento puesta a disposicion de la notificacion "+uriNotificacion+" se seteará a NULL en la BBDD local de la aplicación");
 			}
 			
 			// Cumplimentar los campos del documento
 			Documento docPuestaADisposicion = new Documento();
 			docPuestaADisposicion.clasificado = true;
 			docPuestaADisposicion.descripcion = "Documento \"Puesta a disposición\" creado por el servicio de notificaciones";
 			docPuestaADisposicion.fechaRegistro = DateTime.now();
 			docPuestaADisposicion.fechaSubida = DateTime.now();
 			docPuestaADisposicion.tipo = this.getTipoDocPuestaADisposicion();
 			docPuestaADisposicion.uri = uriDoc;
 			docPuestaADisposicion.estadoDocumento = EstadoNotificacionEnum.puestaadisposicion.name();
 		
 			docPuestaADisposicion.save();
 			
 			Documento dbDoc = notificacion.documentoPuestaADisposicion;
 			notificacion.documentoPuestaADisposicion = null;
 			notificacion.save();
 			dbDoc.delete();
 			dbDoc = null;
 
 			notificacion.uri = uriNotificacion;
 			notificacion.documentoPuestaADisposicion = docPuestaADisposicion;
 			notificacion.documentosAuditoria.add(docPuestaADisposicion); // Añadir el documento a la collección de documentos de auditoría
 			notificacion.estado = EstadoNotificacionEnum.puestaadisposicion.name(); // Se cambia el estado de la notificación
 			
 			log.info(String.format("La notificación (%s) pasa al estado de puesta a disposición", uriNotificacion));
 			
 			// Asignamos el gestor a la notificación
 			notificacion.agente = gestor;
 			
 			notificacion.save();
 		}
 		catch (es.gobcan.platino.servicios.enotificacion.notificacion.NotificacionException ex1) {
 			log.error(String.format(EXCEPTION_CON_WS, idGestor, "puesta a disposición", uriNotificacion), ex1);
 			throw new NotificacionException(MSG_CON_WS + ex1.getMessage(), ex1);
 		} catch (Exception ex4) {
 			log.error(String.format(EXCEPTION_DESCONOCIDO + ex4.getMessage(), idGestor, "puesta a dispoción", uriNotificacion));
 			throw new NotificacionException(MSG_DESCONOCIDO + ex4.getMessage(), ex4);
 		}
 		
 	}
 
 	@Override
 	public void crearDocumentoAcuseRecibo(Notificacion dbNotificacion, String dniInteresado) {
 		String idGestor = AgenteController.getAgente().username; // Se refiere al dni
 		String uriNotificacion = dbNotificacion.uri;
 		
 		try {
 			
 			// El estado de la notificación en el servicio de notificaciones debe ser igual al de la base de datos de fap
 			EstadoNotificacionType estadoNotificacionWS = notificacionPort.obtenerEstadoNotificacion(uriNotificacion);
 			if (estadoNotificacionWS.getEstado() == EstadoNotificacionEnumType.LEIDA) {
 				log.warn(String.format("Actualizando la notificicación %s al estado de LEIDA, debido a un error previo", uriNotificacion));
 				
 				// Se obtiene la uri del documento de notificación
 				String uriDoc = notificacionPort.obtenerURIDocumentoNotificacion("", uriNotificacion, DocumentoNotificacionEnumType.ACUSE_RECIBO);
 				log.info(String.format("Documento de acuse de recibo (%s) para la notificación (%s)", uriDoc, uriNotificacion));
 				
 				// Se actualizan las propiedades del documento
 				dbNotificacion.documentoAcuseRecibo.uri = uriDoc;
 				dbNotificacion.documentoAcuseRecibo.save();
 				
 				// Se cambia el estado de la notificación
 				dbNotificacion.estado = EstadoNotificacionEnum.leida.name();
 				log.info(String.format("La notificación (%s) pasa al estado leida", uriNotificacion));
 				
 				dbNotificacion.save();
 			}
 			else {
 				
 				// Se crea el documento de Acuse de Recibo
 				log.info(String.format("Se crea el documento de acuse de recibo para la notificación (%s) por el gestor (%s)", uriNotificacion, idGestor));
 				DocumentoType docAcuseDeRecibo = notificacionPort.crearDocumentoAcuseDeRecibo(uriNotificacion, dniInteresado);
 				
 				// Se inserta el documento en el Aed
 				Documento dbOld = dbNotificacion.documentoAcuseRecibo;
 				dbNotificacion.documentoAcuseRecibo = null;
 				dbNotificacion.save();
 				dbOld.delete();
 				dbOld = null;
 				
 				Documento dbDocAcuseDeRecibo = new Documento();
 				dbDocAcuseDeRecibo.tipo = this.getTipoDocAcuseRecibo();
 				dbDocAcuseDeRecibo.descripcion = "Documento \"Acuse de Recibo\" creado por el servicio de notificaciones";
 				dbDocAcuseDeRecibo.estadoDocumento = EstadoNotificacionEnum.leida.name();
 				dbDocAcuseDeRecibo.uri = gestorDocumental.saveDocumentoTemporal(dbDocAcuseDeRecibo, docAcuseDeRecibo.getDatos().getInputStream(), UUID.randomUUID().toString() + ".pdf");
				log.info(String.format("Se guarda el acuse de recibo (%s) para la notificación (%s) en la carpeta temporal", dbDocAcuseDeRecibo.uri, uriNotificacion));
 				
 				dbNotificacion.documentoRespondida = dbDocAcuseDeRecibo;
 				dbNotificacion.documentosAuditoria.add(dbDocAcuseDeRecibo); // Añadir el documento a la collección de documentos de auditoría
 				dbNotificacion.preparadaRespondida = true;
 
 				dbDocAcuseDeRecibo.save();
 				dbNotificacion.save();
 			}
 		}
 		catch (Exception e) {
 			log.error("No se ha podido crear el documento de acuse de recibo de la notificación: "+e.getMessage());
 		} 
 	}
 
 	@Override
 	public void enviarAcuseRecibo(Notificacion dbNotificacion, String dniInteresado, String firma) {
 		String uriNotificacion = dbNotificacion.uri;
 		String uriDocAcuseRecibo = dbNotificacion.documentoAcuseRecibo.uri;
 		
 		try {
 			
 			// Se descarga el fichero del aed
 			BinaryResponse docHandler = gestorDocumental.getDocumentoByUri(uriDocAcuseRecibo);
 			
 			DocumentoCreateType docAcuseDeRecibo = new DocumentoCreateType();
 			docAcuseDeRecibo.setContadorRegistro(null); // NOUSED:
 			docAcuseDeRecibo.setDatos(docHandler.contenido);
 			docAcuseDeRecibo.setFechaRegistro(null); // NOUSED:
 			docAcuseDeRecibo.setFirmaXmlSignature(firma);
 			docAcuseDeRecibo.setMimeType(MimeTypeEnumType.APPLICATION_PDF);
 			docAcuseDeRecibo.setNombre(null); // NOUSED:
 			docAcuseDeRecibo.setNumeroGeneral(null); // NOUSED:
 			docAcuseDeRecibo.setNumeroRegistro(null); // NOUSED:
 			docAcuseDeRecibo.setUri(null); // NOUSED:
 			
 			// Se envía la notificación
 			log.info(String.format("Acuse de Recibo de la notificación (%s)", uriNotificacion));
 			try{
 				notificacionPort.enviarAcuseRecibo(uriNotificacion, docAcuseDeRecibo, dniInteresado);
 			} catch (Exception e){
 				log.error("No se ha podido enviar el acuse de recibo de la notificacion debido a un fallo en la llamada al servicio web: "+e.getMessage());
 				return;
 			}
 			
 			// Se procede a la actualización de la notificación de acuse de recibo
 			// Se obtiene la uri del documento de notificación
 			String uriDoc = notificacionPort.obtenerURIDocumentoNotificacion("", uriNotificacion, DocumentoNotificacionEnumType.ACUSE_RECIBO);
 			log.info(String.format("Documento de acuse de recibo (%s) para la notificación (%s)", uriDoc, uriNotificacion));
 			
 			// Se actualizan las propiedades del documento			
 			dbNotificacion.documentoAcuseRecibo.uri = uriDoc;
 			dbNotificacion.documentoAcuseRecibo.clasificado = true;
 			dbNotificacion.documentoAcuseRecibo.save();
 
 			// TODO: ¿Cambiar el estado de la notificacion?
 
 			dbNotificacion.save();
 		}
 		catch (Exception e) {
 			log.error("Ha ocurrido un error al intentar anular la notificación: "+e.getMessage());
 		} 
 				
 	}
 
 	/* Funcion que consulta el WS de Notificaciones para conocer las notificaciones a raiz de un patrón de búsqueda
 	 * 
 	 * uriProcedimiento: La uri del procedimiento que queremos saber sus notificaciones
 	 */
 	@Override
 	public List<Notificacion> getNotificaciones(String uriProcedimiento) {
 		List<Notificacion> notificacionesWS = new ArrayList<Notificacion>();
 		if ((uriProcedimiento == null) || (uriProcedimiento.trim().isEmpty())){
 			play.Logger.info("La uri del procedimiento no puede ser vacía");
 			return notificacionesWS;
 		}
 		try {
 			NotificacionCriteriaType criterioBusqueda = new NotificacionCriteriaType();
 			criterioBusqueda.setUriProcedimiento(uriProcedimiento);
 			ResultadoBusquedaNotificacionType resultadoBusqueda = notificacionPort.buscarNotificaciones(criterioBusqueda);
 			List<NotificacionType> notificacionesType = resultadoBusqueda.getNotificaciones().getNotificacion();
 			for (NotificacionType notificacionType: notificacionesType)
 				notificacionesWS.add(NotificacionUtils.convertNotificacionTypeToNotificacion(notificacionType));
 			return notificacionesWS;
 		} catch (NotificacionException e) {
 			play.Logger.error("Error en la llamada al método del Servicio Web de búsqueda de notificaciones: "+e.getMessage());
 			return notificacionesWS;
 		} catch (Exception e){
 			play.Logger.error("Error al intentar obtener las notificaciones del servicio web "+e.getMessage());
 			return null;
 		}
 	}
 	
 	@Override
 	public List<Notificacion> getNotificaciones() {
 		List<Notificacion> notificacionesWS = new ArrayList<Notificacion>();
 		try {
 			NotificacionCriteriaType criterioBusqueda = new NotificacionCriteriaType();
 			ResultadoBusquedaNotificacionType resultadoBusqueda = notificacionPort.buscarNotificaciones(criterioBusqueda);
 			List<NotificacionType> notificacionesType = resultadoBusqueda.getNotificaciones().getNotificacion();
 			for (NotificacionType notificacionType: notificacionesType)
 				notificacionesWS.add(NotificacionUtils.convertNotificacionTypeToNotificacion(notificacionType));
 			return notificacionesWS;
 		} catch (NotificacionException e) {
 			play.Logger.info("Error en la llamada al método del Servicio Web de búsqueda de notificaciones: "+e.getMessage());
 			return notificacionesWS;
 		}
 	}
 
 	@Override
 	public String estadoNotificacion(String uriNotificacion) {
 		EstadoNotificacionType estadoNotificacionType;
 		try {
 			estadoNotificacionType = notificacionPort.obtenerEstadoNotificacion(uriNotificacion);
 			return NotificacionUtils.convertEstadoNotificacionEnumTypeToEstadoNotificacion(estadoNotificacionType.getEstado());
 		} catch (Exception e) {
 			log.error("No se ha podido recuperar del servicio web el estado de la notificacion "+uriNotificacion+" : "+e.getMessage());
 		}
 		return "";
 	}
 
 	@Override
 	public Notificacion obtenerNotificacion(String uriNotificacion) {
 		Notificacion notificacion = null;
 		try {
 			NotificacionType notificacionType = notificacionPort.obtenerNotificacion(uriNotificacion);
 			notificacion = NotificacionUtils.convertNotificacionTypeToNotificacion(notificacionType);
 		} catch (Exception e) {
 			log.error("Hubo un error al intentar recuperar la notificacion del servicio web con uri "+uriNotificacion+" : "+e.getMessage());
 		}
 		return notificacion;
 	}
 
 	@Override
 	public Documento obtenerDocumentoNotificacion(String idUsuario, String uriNotificacion, DocumentoNotificacionEnumType tipoDocumento) {
 		Documento documento = new Documento();
 		try {
 			DocumentoType documentoType = notificacionPort.obtenerDocumentoNotificacion(idUsuario, uriNotificacion, tipoDocumento);
 			documento.descripcion = documentoType.getNombre();
 			documento.uri = documentoType.getUri();
 			documento.fechaRegistro = new DateTime(documentoType.getFechaRegistro().toGregorianCalendar().getTime());
 		} catch (NotificacionException e) {
 			log.error("Hubo un error al intentar obtener el documento de notificación del servicio web con uri "+uriNotificacion+" : "+e.getMessage());
 		}
 		return documento;
 	}
 
 	@Override
 	public void crearDocumentacionAnulacion(Notificacion dbNotificacion) {
 
 		String idGestor = AgenteController.getAgente().username; // Se refiere al dni
 		String uriNotificacion = dbNotificacion.uri;
 		
 		try {
 			
 			String motivoAnulacion = null;
 	
 			// El estado de la notificación en el servicio de notificaciones debe ser igual al de la base de datos de fap
 			EstadoNotificacionType estadoNotificacionWS = notificacionPort.obtenerEstadoNotificacion(uriNotificacion);
 			if (estadoNotificacionWS.getEstado() == EstadoNotificacionEnumType.ANULADA) {
 				log.warn(String.format("Actualizando la notificicación %s al estado de ANULADA, debido a un error previo", uriNotificacion));
 				
 				// Se obtiene la uri del documento de notificación
 				String uriDoc = notificacionPort.obtenerURIDocumentoNotificacion("", uriNotificacion, DocumentoNotificacionEnumType.ANULACION);
 				log.info(String.format("Documento de anulación (%s) para la notificación (%s)", uriDoc, uriNotificacion));
 				
 				// Se actualizan las propiedades del documento			
 				dbNotificacion.documentoAnulacion.uri = uriDoc;
 				dbNotificacion.documentoAnulacion.clasificado = true;
 				dbNotificacion.documentoAnulacion.save();
 				
 				// Se cambia el estado de la notificación
 				dbNotificacion.estado = EstadoNotificacionEnum.anulada.name();
 				log.info(String.format("La notificación (%s) pasa al estado de anulada", uriNotificacion));
 			
 				dbNotificacion.save();
 			}
 			else {
 				// Se crea el documento de anulación
 				log.info(String.format("Se crea el documento de anulación para la notificación (%s) por el gestor (%s)", uriNotificacion, idGestor));
 				DocumentoType docAnulacion = notificacionPort.crearDocumentoAnulacion(uriNotificacion, motivoAnulacion, idGestor);
 				
 				// Se inserta el documento en el Aed
 				Documento dbOld = dbNotificacion.documentoAnulacion;
 				dbNotificacion.documentoAnulacion = null;
 				dbNotificacion.save();
 				dbOld.delete();
 				dbOld = null;
 
 				Documento dbDocAnulacion = new Documento();
 				dbDocAnulacion.tipo = this.getTipoDocAnulacion();
 				dbDocAnulacion.descripcion = "Documento \"Anulación\" creado por el servicio de notificaciones";
 				dbDocAnulacion.estadoDocumento = EstadoNotificacionEnum.anulada.name();
 			 	dbDocAnulacion.uri = gestorDocumental.saveDocumentoTemporal(dbDocAnulacion, docAnulacion.getDatos().getInputStream(), UUID.randomUUID().toString() + ".pdf");
 				log.info(String.format("Se guarda el documento de anulación (%s) para la notificación (%s) en la carpeta temporal", dbDocAnulacion.uri, uriNotificacion));
 
 				dbNotificacion.documentoAnulacion = dbDocAnulacion;
 				dbNotificacion.documentosAuditoria.add(dbDocAnulacion);  // Añadir el documento a la collección de documentos de auditoría
 				dbNotificacion.preparadaAnulacion = true;
 				
 				dbDocAnulacion.save();
 				dbNotificacion.save();
 			}
 		}
 		catch (Exception e) {
 			log.error("No se ha podido crear el documento de anulación de la notificación: "+ e.getMessage());
 		} 
 	}
 
 	@Override
 	public void anularNotificacion(Notificacion dbNotificacion, String firma) {
 		String uriNotificacion = dbNotificacion.uri;
 		String uriDocAnulacion = dbNotificacion.documentoAnulacion.uri;
 		String idGestor = AgenteController.getAgente().username; // Se refiere al dni
 		
 		try {
 			
 			// Se descarga el fichero del aed
 			BinaryResponse docHandler = gestorDocumental.getDocumentoByUri(uriDocAnulacion);
 			
 			DocumentoCreateType docAnulacion = new DocumentoCreateType();
 			docAnulacion.setContadorRegistro(null); // NOUSED:
 			docAnulacion.setDatos(docHandler.contenido);
 			docAnulacion.setFechaRegistro(null); // NOUSED:
 			docAnulacion.setFirmaXmlSignature(firma);
 			docAnulacion.setMimeType(MimeTypeEnumType.APPLICATION_PDF);
 			docAnulacion.setNombre(null); // NOUSED:
 			docAnulacion.setNumeroGeneral(null); // NOUSED:
 			docAnulacion.setNumeroRegistro(null); // NOUSED:
 			docAnulacion.setUri(null); // NOUSED:
 			
 			// Se envía la notificación
 			log.info(String.format("Anulación de la notificación (%s) por el gestor (%s)", uriNotificacion, idGestor));
 			try{
 				notificacionPort.anularNotificacion(uriNotificacion, docAnulacion, idGestor);
 			} catch (Exception e){
 				log.error("No se ha podido anular la notificacion debido a un fallo en la llamada al servicio web: "+e.getMessage());
 				return;
 			}
 			
 			// Se procede a la actualización de la notificación de anulación
 			// Se obtiene la uri del documento de notificación
 			String uriDoc = notificacionPort.obtenerURIDocumentoNotificacion("", uriNotificacion, DocumentoNotificacionEnumType.ANULACION);
 			log.info(String.format("Documento de anulación (%s) para la notificación (%s)", uriDoc, uriNotificacion));
 			
 			// Se actualizan las propiedades del documento			
 			dbNotificacion.documentoAnulacion.uri = uriDoc;
 			dbNotificacion.documentoAnulacion.clasificado = true;
 			dbNotificacion.documentoAnulacion.save();
 			
 			// Se cambia el estado de la notificación
 			dbNotificacion.estado = EstadoNotificacionEnum.anulada.name();
 			log.info(String.format("La notificación (%s) pasa al estado de anulada", uriNotificacion));
 
 			dbNotificacion.save();
 		}
 		catch (Exception e) {
 			log.error("Ha ocurrido un error al intentar anular la notificación: "+e.getMessage());
 		} 
 		
 	}
 
 	@Override
 	public void crearDocumentoMarcarComoRespondida(Notificacion dbNotificacion) {
 
 		String idGestor = AgenteController.getAgente().username; // Se refiere al dni
 		String uriNotificacion = dbNotificacion.uri;
 		
 		try {
 			
 			// El estado de la notificación en el servicio de notificaciones debe ser igual al de la base de datos de fap
 			EstadoNotificacionType estadoNotificacionWS = notificacionPort.obtenerEstadoNotificacion(uriNotificacion);
 			if (estadoNotificacionWS.getEstado() == EstadoNotificacionEnumType.RESPONDIDA) {
 				log.warn(String.format("Actualizando la notificicación %s al estado de RESPONDIDA, debido a un error previo", uriNotificacion));
 				
 				// Se obtiene la uri del documento de notificación
 				String uriDoc = notificacionPort.obtenerURIDocumentoNotificacion("", uriNotificacion, DocumentoNotificacionEnumType.MARCADA_RESPONDIDA);
 				log.info(String.format("Documento de respondida (%s) para la notificación (%s)", uriDoc, uriNotificacion));
 				
 				// Se actualizan las propiedades del documento
 				dbNotificacion.documentoRespondida.uri = uriDoc;
 				dbNotificacion.documentoRespondida.save();
 				
 				// Se cambia el estado de la notificación
 				dbNotificacion.estado = EstadoNotificacionEnum.respondida.name();
 				log.info(String.format("La notificación (%s) pasa al estado de respondida", uriNotificacion));
 				
 				dbNotificacion.save();
 			}
 			else {
 				XMLGregorianCalendar fechaHoraRespuesta = XMLGregorianCalendarConverter.asXMLGregorianCalendar(DateTime.now());
 
 				List<String> urisDocumentosRespuesta = new ArrayList<String>();
 				for (DocumentoNotificacion doc : dbNotificacion.documentosRespuesta) {
 					urisDocumentosRespuesta.add(doc.uri);
 				}
 				
 				// Se crea el documento de Marcar como Respondida
 				log.info(String.format("Se crea el documento de respondida para la notificación (%s) por el gestor (%s)", uriNotificacion, idGestor));
 				DocumentoType docRespondida = notificacionPort.crearDocumentoMarcarComoRespondida(uriNotificacion, fechaHoraRespuesta, urisDocumentosRespuesta, idGestor);
 				
 				// Se inserta el documento en el Aed
 				Documento dbOld = dbNotificacion.documentoRespondida;
 				dbNotificacion.documentoRespondida = null;
 				dbNotificacion.save();
 				dbOld.delete();
 				dbOld = null;
 				
 				Documento dbDocRespondida = new Documento();
 				dbDocRespondida.tipo = this.getTipoDocMarcarARespondida();
 				dbDocRespondida.descripcion = "Documento \"Marcar a respondida\" creado por el servicio de notificaciones";
 				dbDocRespondida.estadoDocumento = EstadoNotificacionEnum.respondida.name();
 				dbDocRespondida.uri = gestorDocumental.saveDocumentoTemporal(dbDocRespondida, docRespondida.getDatos().getInputStream(), UUID.randomUUID().toString() + ".pdf");
 				log.info(String.format("Se guarda el documento de respondida (%s) para la notificación (%s) en la carpeta temporal", dbDocRespondida.uri, uriNotificacion));
 				
 				dbNotificacion.documentoRespondida = dbDocRespondida;
 				dbNotificacion.documentosAuditoria.add(dbDocRespondida); // Añadir el documento a la collección de documentos de auditoría
 				dbNotificacion.preparadaRespondida = true;
 
 				dbDocRespondida.save();
 				dbNotificacion.save();
 			}
 		}
 		catch (Exception e) {
 			log.error("No se ha podido crear el documento de marcar como respondida de la notificación: "+e.getMessage());
 		} 
 	}
 
 	@Override
 	public void marcarNotificacionComoRespondida(Notificacion dbNotificacion, String firma) {
 
 		String uriNotificacion = dbNotificacion.uri;
 		String uriDocRespondida = dbNotificacion.documentoRespondida.uri;
 		String idGestor = AgenteController.getAgente().username; // Se refiere al dni
 		
 		try {
 			// Se descarga el fichero del aed
 			BinaryResponse docHandler = gestorDocumental.getDocumentoByUri(uriDocRespondida);
 			
 			XMLGregorianCalendar fechaHoraRespuesta = XMLGregorianCalendarConverter.asXMLGregorianCalendar(DateTime.now());
 			
 			List<String> urisDocumentosRespuesta = new ArrayList<String>();
 			for (DocumentoNotificacion docs : dbNotificacion.documentosRespuesta) {
 				urisDocumentosRespuesta.add(docs.uri);
 			}
 			
 			DocumentoCreateType docMarcadaComoRespondida = new DocumentoCreateType();
 			docMarcadaComoRespondida.setContadorRegistro(null); // NOUSED:
 			docMarcadaComoRespondida.setDatos(docHandler.contenido);
 			docMarcadaComoRespondida.setFechaRegistro(null); // NOUSED:
 			docMarcadaComoRespondida.setFirmaXmlSignature(firma);
 			docMarcadaComoRespondida.setMimeType(MimeTypeEnumType.APPLICATION_PDF);
 			docMarcadaComoRespondida.setNombre(null); // NOUSED:
 			docMarcadaComoRespondida.setNumeroGeneral(null); // NOUSED:
 			docMarcadaComoRespondida.setNumeroRegistro(null); // NOUSED:
 			docMarcadaComoRespondida.setUri(null); // NOUSED:
 			
 			// Se envía la notificación
 			log.info(String.format("Se marca la notificación (%s) por el gestor (%s) como respondida", uriNotificacion, idGestor));
 			try{
 				notificacionPort.marcarNotificacionComoRespondida(uriNotificacion, fechaHoraRespuesta, urisDocumentosRespuesta, docMarcadaComoRespondida, idGestor);
 			} catch (Exception e){
 				log.error("Error al intentar marcar la notificación como respondida a raiz de la llamada al servicio web: "+e.getMessage());
 				return;
 			}
 			
 			// Se procede a la actualización de la notificación de asignar a respondida
 			// Se obtiene la uri del documento de notificación
 			String uriDoc = notificacionPort.obtenerURIDocumentoNotificacion("", uriNotificacion, DocumentoNotificacionEnumType.MARCADA_RESPONDIDA);
 			log.info(String.format("Documento de respondida (%s) para la notificación (%s)", uriDoc, uriNotificacion));
 			
 			// Se actualizan las propiedades del documento
 			dbNotificacion.documentoRespondida.uri = uriDoc;
 			dbNotificacion.documentoRespondida.save();
 			
 			// Se cambia el estado de la notificación
 			dbNotificacion.estado = EstadoNotificacionEnum.respondida.name();
 			log.info(String.format("La notificación (%s) pasa al estado de respondida", uriNotificacion));
 			
 			dbNotificacion.save();
 		} 
 		catch (Exception e) {
 			log.error("Hubo un error al marcar como respondida la notificacion: "+e.getMessage());
 		} 
 	}
 	
 	@Override
 	public final String getUriProcedimiento() {
 		return FapProperties.get("fap.notificacion.procedimiento");
 	}
 	
 	@Override
 	public final String getUriBackOffice() {
 		return FapProperties.get("fap.notificacion.backoffice");
 	}
 	
 	protected String getTipoDocPuestaADisposicion() {
 		return TIPO_DOC_PUESTAADISPOSICION;
 	}
 	
 	protected String getTipoDocAnulacion() {
 		return TIPO_DOC_ANULACION;
 	}
 	
 	protected String getTipoDocMarcarARespondida() {
 		return TIPO_DOC_MARCARARESPONDIDA;
 	}
 	
 	protected String getTipoDocAcuseRecibo() {
 		return TIPO_DOC_ACUSERECIBO;
 	}
 
 }
