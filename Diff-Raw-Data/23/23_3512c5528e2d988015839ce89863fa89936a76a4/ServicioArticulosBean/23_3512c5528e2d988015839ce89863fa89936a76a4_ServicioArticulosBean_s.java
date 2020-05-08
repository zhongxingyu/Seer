 package despacho.backend.servicios;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 
 import ar.edu.uade.integracion.VO.ArticuloVO;
 import ar.edu.uade.integracion.VO.AtributoVO;
 import ar.edu.uade.integracion.VO.ItemSolicitudArticuloVO;
 import ar.edu.uade.integracion.VO.SolicitudArticuloVO;
 import despacho.backend.administradores.AdministradorArticulos;
 import despacho.backend.administradores.AdministradorOrdenesDespacho;
 import despacho.backend.entities.Articulo;
 import despacho.backend.entities.ArticuloAtributo;
 import despacho.backend.entities.EstadoOrdenDespacho;
 import despacho.backend.entities.EstadoSolicitudArticulo;
 import despacho.backend.entities.OrdenDespacho;
 import despacho.backend.entities.SolicitudArticulo;
 import despacho.backend.utils.Logger;
 
 @Stateless
 public class ServicioArticulosBean implements ServicioArticulos {
 
 	@EJB
 	private AdministradorArticulos administradorArticulos;
 	
 	@EJB
 	private AdministradorOrdenesDespacho administradorOrdenesDespacho;
 	
 	@EJB
 	private ServicioOrdenesDespacho servicioOrdenesDespacho;
 	
 	@Override
 	// DCH01.Recibir nuevo Artculo
 	public void ingresarArticulo(ArticuloVO articulo) {
 		try {
 			if (articulo == null) {
 				return;
 			}
 			
 			Logger.info("DCH01", "Nuevo articulo ingresado: " + articulo.getIdarticulo() + " - Deposito: " + articulo.getIdDeposito());
 			
 			// Persistir el articulo
 			List<ArticuloAtributo> nuevoArticuloAtributos = new ArrayList<ArticuloAtributo>();
 			
 			for (AtributoVO atributo: articulo.getAtributos()) {
 				ArticuloAtributo nuevoArticuloAtributo = new ArticuloAtributo();
 				nuevoArticuloAtributo.setNombre(atributo.getNombre());
 				nuevoArticuloAtributo.setValor(atributo.getValor());
 				nuevoArticuloAtributos.add(nuevoArticuloAtributo);
 			}
 			
 			Articulo nuevoArticulo = new Articulo();
 			nuevoArticulo.setCategoria(articulo.getCategoria());
 			nuevoArticulo.setDescripcion(articulo.getDescripcion());
 			nuevoArticulo.setFoto(articulo.getFoto());
 			nuevoArticulo.setIdArticulo(articulo.getIdarticulo());
 			nuevoArticulo.setIdDeposito(articulo.getIdDeposito());
 			nuevoArticulo.setMarca(articulo.getMarca());
 			nuevoArticulo.setNombre(articulo.getNombre());
 			nuevoArticulo.setOrigen(articulo.getOrigen());
 			nuevoArticulo.setPrecio(articulo.getPrecio());
 			nuevoArticulo.setStock(articulo.getStock());
 			nuevoArticulo.setAtributos(nuevoArticuloAtributos);
 			
 			this.administradorArticulos.agregar(nuevoArticulo);
 			
 			Logger.info("DCH01", "Listo (DCH01 - Recibir nuevo Artculo)");
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			Logger.error("DCH01", e.getMessage());
 		}
 	}
 	
 	@Override
 	// DCH03.Recepcin y Procesamiento de Artculos a Despachar
 	public Boolean recepcionArticulosParaDespachar(SolicitudArticuloVO solicitudArticulo) {
 		try {
 			Logger.info("DCH03", "Recepcion solicitud articulo: " + solicitudArticulo.getIdSolicitudArticulo());
 			
 			for (ItemSolicitudArticuloVO itemSolicitud : solicitudArticulo.getArticulos()) {
 				
 				int cantidad = itemSolicitud.getCantSolicitada();
 				
 				// Obtengo las ordenes PENDIENTES DE ENTREGA (ordenadas por la mas antigua)
 				List<OrdenDespacho> ordenesPendientes = this.administradorOrdenesDespacho.listarPorEstado(EstadoOrdenDespacho.PENDIENTE_ENTREGA);
 				
 				for (OrdenDespacho ordenPendiente : ordenesPendientes) {
 					
 					// Proceso la orden que contenga el articulo en cuestion
 					for (SolicitudArticulo solicitudArticuloOrden : ordenPendiente.getArticulos()) {
 						
 						if (itemSolicitud.getIdArticulo().equalsIgnoreCase(solicitudArticuloOrden.getArticulo().getIdArticulo())) {
 							
 							if (solicitudArticuloOrden.getCantidad() <= cantidad) {
 								
 								solicitudArticuloOrden.setEstado(EstadoSolicitudArticulo.LISTA);
 								cantidad = cantidad - solicitudArticuloOrden.getCantidad();
								
								// Actualizo la orden
								this.administradorOrdenesDespacho.actualizar(ordenPendiente);
 							}
 						}
 					}
 					
 					// Si todos los articulos de la orden estan listos para entrega, se llama a DCH04
 					Boolean ordenListaParaEntrega = true;
 					
 					for (SolicitudArticulo solicitudArticuloOrden : ordenPendiente.getArticulos()) {
 						
 						if (!solicitudArticuloOrden.getEstado().equalsIgnoreCase(EstadoSolicitudArticulo.LISTA)) {
 							
 							ordenListaParaEntrega = false;
 							break;
 						}
 					}
 					
 					if (ordenListaParaEntrega) {
 						this.servicioOrdenesDespacho.completarOrdenDespacho(ordenPendiente.getCodOrden());
 					}
 				}
 			}
 			
 			Logger.info("DCH03", "Listo (DCH03 - Recepcin y Procesamiento de Artculos a Despachar)");
 			return true;
 		}
		catch (Exception e) {
 			e.printStackTrace();
 			Logger.error("DCH03", e.getMessage());
 			return false;
 		}
 	}
 }
