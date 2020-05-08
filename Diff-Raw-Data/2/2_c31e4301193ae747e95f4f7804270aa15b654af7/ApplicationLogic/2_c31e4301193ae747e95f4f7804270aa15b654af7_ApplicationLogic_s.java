 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.pmarlen.client;
 
 //import com.pmarlen.client.business.dao.BasicInfoDAO;
 import com.pmarlen.businesslogic.exception.AuthenticationException;
 import com.pmarlen.businesslogic.exception.PedidoVentaException;
 import com.pmarlen.client.controller.PreferencesController;
 import com.pmarlen.client.model.ApplicationSession;
 import com.pmarlen.client.model.ProductoFastDisplayModel;
 import com.pmarlen.client.ticketprinter.TicketBlueToothPrinter;
 import com.pmarlen.client.ticketprinter.TicketPOSTermalPrinter;
 import com.pmarlen.client.ticketprinter.TicketPrinteService;
 import com.pmarlen.model.Constants;
 import com.pmarlen.model.beans.*;
 import com.pmarlen.model.controller.BasicInfoDAO;
 import com.pmarlen.model.controller.PersistEntityWithTransactionDAO;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.Timestamp;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import javax.imageio.ImageIO;
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.NoResultException;
 import javax.persistence.NonUniqueResultException;
 import javax.persistence.Query;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 
 /**
  *
  * @author alfred
  */
 @Controller("applicationLogic")
 public class ApplicationLogic {
 
 	public static final String CONFIG_PREFERENCES_DIR = "./config/";
 	public static final String CONFIG_PREFERENCESPROPERTIES = "./config/Preferences.properties";
 	private ApplicationSession applicationSession;
 	private SendDataSynchronizer sendDataSynchronizer;
 	private Logger logger;
 	private EntityManagerFactory emf;
 	private TicketPrinteService ticketPrinterService;
 	public static int IMPRESION_TIPO_TERMICAPOS = 1;
 	public static int IMPRESION_TIPO_BLUETOOTH = 2;
 	//private int impresionTipo;
 	
 	private Properties preferences;
 
 	@Autowired
 	public void setEntityManagerFactory(EntityManagerFactory emf) {
 		this.emf = emf;
 	}
 	private BasicInfoDAO basicInfoDAO;
 
 	@Autowired
 	public void setBasicInfoDAO(BasicInfoDAO basicInfoDAO) {
 		this.basicInfoDAO = basicInfoDAO;
 	}
 	PersistEntityWithTransactionDAO persistEntityWithTransactionDAO;
 
 	@Autowired
 	public void setPersistEntityWithTransactionDAO(PersistEntityWithTransactionDAO persistEntityWithTransactionDAO) {
 		this.persistEntityWithTransactionDAO = persistEntityWithTransactionDAO;
 	}
 	private double factorIVA;
 
 	public ApplicationLogic() {
 		factorIVA = 0.16;
 		logger = LoggerFactory.getLogger(ApplicationLogic.class);
 		logger.debug("->ApplicationLogic, created");
 
 		//readPreferences();
 		//impresionTipo = IMPRESION_TIPO_TERMICAPOS;
 
 		cambiarTipoImpresionTermicaPOS();
 	}
 
 	public void cambiarTipoImpresionTermicaPOS() {
 		//impresionTipo = IMPRESION_TIPO_TERMICAPOS;
 		ticketPrinterService = new TicketPOSTermalPrinter();
 		ticketPrinterService.setApplicationLogic(this);
 	}
 
 	public void cambiarTipoImpresionBlueTooth() {
 		//impresionTipo = IMPRESION_TIPO_BLUETOOTH;
 		ticketPrinterService = new TicketBlueToothPrinter();
 		ticketPrinterService.setApplicationLogic(this);
 	}
 
 	public void deleteProductoFromCurrentPedidoVenta(int indexProdToDelete) {
 		try {
 			Collection<PedidoVentaDetalle> afeterDelete = new ArrayList<PedidoVentaDetalle>();
 
 			final Iterator<PedidoVentaDetalle> iterator = applicationSession.getPedidoVenta().getPedidoVentaDetalleCollection().iterator();
 			PedidoVentaDetalle pvdToDelete = null;
 			for (int nd = 0; iterator.hasNext(); nd++) {
 				final PedidoVentaDetalle next = iterator.next();
 				if (nd != indexProdToDelete) {
 					afeterDelete.add(next);
 				}
 			}
 
 			applicationSession.getPedidoVenta().getPedidoVentaDetalleCollection().clear();
 			applicationSession.getPedidoVenta().getPedidoVentaDetalleCollection().addAll(afeterDelete);
 
 		} catch (IndexOutOfBoundsException ioe) {
 			throw new IllegalArgumentException("Producto [index] Not exist in PedidoVentaDetalle:" + ioe.getMessage());
 		}
 	}
 
 	public void setMarcaPorLinea(int idMarca) {
 		logger.debug("->setMarcaDeProductos: ");
 		EntityManager em = null;
 		try {
 			em = emf.createEntityManager();
 			logger.debug("->EntityManager em, created");
 			Marca marca = em.find(Marca.class, idMarca);
 			applicationSession.setMarcaPorLinea(marca);
 		} catch (Exception e) {
 			logger.error("Exception caught:", e);
 		} finally {
 			if (em != null) {
 				em.close();
 				logger.debug("->Ok, Entity Manager Closed");
 			}
 		}
 	}
 
 	public void setMarcaPorIndustria(int idMarca) {
 		logger.debug("->setMarcaDeProductos: ");
 		EntityManager em = null;
 		try {
 			em = emf.createEntityManager();
 			logger.debug("->EntityManager em, created");
 			Marca marca = em.find(Marca.class, idMarca);
 			applicationSession.setMarcaPorIndustria(marca);
 		} catch (Exception e) {
 			logger.error("Exception caught:", e);
 		} finally {
 			if (em != null) {
 				em.close();
 				logger.debug("->Ok, Entity Manager Closed");
 			}
 		}
 	}
 
 	public List<Producto> getProductosForPrinting() {
 		logger.debug("->setMarcaDeProductos: ");
 		List<Producto> result = null;
 		EntityManager em = null;
 		try {
 			em = emf.createEntityManager();
 			final Query q = em.createQuery("select x from Producto x");
 			result = q.getResultList();
 			for (Producto p : result) {
 				final Collection<AlmacenProducto> almacenProductoCollection = p.getAlmacenProductoCollection();
 				for (AlmacenProducto ap : almacenProductoCollection) {
 					ap.getAlmacen().getId();
 				}
 			}
 		} catch (Exception e) {
 			logger.error("Exception caught:", e);
 		} finally {
 			if (em != null) {
 				em.close();
 				logger.debug("->Ok, Entity Manager Closed");
 			}
 		}
 		return result;
 	}
 
 	public void startNewPedidoVentaSession() {
 		applicationSession.setProductoBuscadoActual(null);
 		applicationSession.setPedidoVenta(new PedidoVenta());
 		applicationSession.getPedidoVenta().
 				setPedidoVentaDetalleCollection(new ArrayList<PedidoVentaDetalle>());
 	}
 
 	public PedidoVentaDetalle searchProducto(Producto prod) {
 		PedidoVentaDetalle dvpFound = null;
 		logger.debug("===>>>searchProducto(" + prod.getId() + ")");
 		for (PedidoVentaDetalle dvp : applicationSession.getPedidoVenta().getPedidoVentaDetalleCollection()) {
 			logger.debug("\t===>>>comparing " + prod.getId() + " != " + dvp.getProducto().getId() + " ? ");
 			if (dvp.getProducto().getId().equals(prod.getId())) {
 				dvpFound = dvp;
 				logger.debug("\t\t===>>>FOUND");
 				break;
 			}
 		}
 		return dvpFound;
 	}
 //
 //    public void addProductoToCurrentPedidoVenta(Producto prod) {
 //        PedidoVentaDetalle dvpFound = searchProducto(prod);
 //        PedidoVentaDetalle dvp = null;
 //        if (dvpFound == null) {
 //            dvp = new PedidoVentaDetalle();
 //            dvp.setProducto(prod);
 //            dvp.setCantidad(0);
 //            dvp.setPrecioVenta(prod.getPrecioBase());            
 //        } else {
 //            dvp = dvpFound;
 //        }
 //
 //        dvp.setCantidad(dvp.getCantidad() + 1);
 //
 //        if (dvpFound == null) {
 //            applicationSession.getPedidoVenta().getPedidoVentaDetalleCollection().add(dvp);
 //        }
 //    }
 //
 //    public void addProductoCajaToCurrentPedidoVenta(Producto prod) {
 //        PedidoVentaDetalle dvpFound = searchProducto(prod);
 //        PedidoVentaDetalle dvp = null;
 //        if (dvpFound == null) {
 //            dvp = new PedidoVentaDetalle();
 //            dvp.setProducto(prod);
 //            dvp.setCantidad(0);
 //            dvp.setPrecioVenta(prod.getPrecioBase());            
 //        } else {
 //            dvp = dvpFound;
 //        }
 //
 //        dvp.setCantidad(dvp.getCantidad() + prod.getUnidadesPorCaja());
 //
 //        if (dvpFound == null) {
 //            applicationSession.getPedidoVenta().getPedidoVentaDetalleCollection().add(dvp);
 //        }
 //    }
 
 	public void addProductoNToCurrentPedidoVenta(Producto prod, int n) {
 		PedidoVentaDetalle dvpFound = searchProducto(prod);
 		PedidoVentaDetalle dvp = null;
 		if (dvpFound == null) {
 			dvp = new PedidoVentaDetalle();
 			dvp.setProducto(prod);
 			dvp.setCantidad(n);
 			dvp.setPrecioVenta(-1.0);
 			//dvp.setPrecioVenta(prod.getPrecioBase());            
 		} else {
 			dvp = dvpFound;
 			dvp.setCantidad(dvp.getCantidad() + n);
 		}
 
 		//dvp.setCantidad(dvp.getCantidad() + n);
 		boolean ea = checarExistanciaEnAlmacenActual(dvp, dvp.getCantidad());
 		logger.debug("->addProductoNToCurrentPedidoVenta: ea ?" + ea);
 		if (!ea) {
 			throw new IllegalStateException("¡ NO HAY EXISTENCIA EN ESTE ALMACÉN !");
 		}
 
 		if (dvpFound == null) {
 			applicationSession.getPedidoVenta().getPedidoVentaDetalleCollection().add(dvp);
 		}
 	}
 
 	public Producto consultarProductoAlmacenActual(String codigoDeBarras) {
 		logger.debug("->consultarProductoAlmacenActual: codigoDeBarras=" + codigoDeBarras);
 		EntityManager em = null;
 		try {
 			em = emf.createEntityManager();
 			logger.debug("->EntityManager em, created");
 			final Query q = em.createQuery("select x from Producto x where x.codigoBarras = :codigoDeBarras");
 			Producto p = (Producto) q.setParameter("codigoDeBarras", codigoDeBarras).getSingleResult();
 			final Collection<AlmacenProducto> almacenProductoCollection = p.getAlmacenProductoCollection();
 			if (almacenProductoCollection.size() > 0) {
 				for (AlmacenProducto ap : almacenProductoCollection) {
 				}
 			}
 			return p;
 		} catch (Exception e) {
 			logger.error("Exception caught:", e);
 		} finally {
 			if (em != null) {
 				em.close();
 				logger.debug("->Ok, Entity Manager Closed");
 			}
 			return null;
 		}
 
 	}
 
 	public boolean checarExistanciaEnAlmacenActual(PedidoVentaDetalle pvd, int cantidad) {
 		logger.debug("->checarExistanciaEnAlmacenActual: pvd=" + pvd + ", cantidad=" + cantidad);
 		EntityManager em = null;
 		try {
 			em = emf.createEntityManager();
 			logger.debug("->EntityManager em, created");
 			Producto p = em.find(Producto.class, pvd.getProducto().getId());
 			final Collection<AlmacenProducto> almacenProductoCollection = p.getAlmacenProductoCollection();
 			final int almacenId = applicationSession.getAlmacen().getId().intValue();
 			int cantidaActual = 0;
 			AlmacenProducto apObjetivo = null;
 			if (almacenProductoCollection.size() > 0) {
 				for (AlmacenProducto ap : almacenProductoCollection) {
 					if (ap.getAlmacen().getId().intValue() == almacenId) {
 						cantidaActual = ap.getCantidadActual();
 						apObjetivo = ap;
 						break;
 					}
 				}
 			}
 
 			if (apObjetivo == null //|| cantidaActual < cantidad
 					) {
 				return false;
 			}
 			pvd.setPrecioVenta(apObjetivo.getPrecioVenta());
 			logger.debug("\t->cantidaActual=" + cantidaActual + ", precioVenta=" + apObjetivo.getPrecioVenta());
 
 		} catch (Exception e) {
 			logger.error("Exception caught:", e);
 		} finally {
 			if (em != null) {
 				em.close();
 				logger.debug("->Ok, Entity Manager Closed");
 			}
 		}
 		return true;
 
 	}
 
 //    public void editProductoCajaToCurrentPedidoVenta(Producto prod, int cantidadPedida) {
 //        PedidoVentaDetalle dvpFound = searchProducto(prod);
 //        PedidoVentaDetalle dvp = null;
 //        if (dvpFound == null) {
 //            throw new IllegalArgumentException("Producto not exist in PedidoVentaDetalle");
 //        }
 //
 //        if (cantidadPedida == 0) {
 //            applicationSession.getPedidoVenta().getPedidoVentaDetalleCollection().remove(dvp);
 //        } else {
 //            dvp.setCantidad(cantidadPedida);
 //        }
 //    }
 	public void setClienteToCurrentPedidoVenta(Cliente cliente) {
 		applicationSession.getPedidoVenta().setCliente(cliente);
 	}
 
 	public void setFormaDePagoToCurrentPedidoVenta(FormaDePago formaDePago) {
 		applicationSession.getPedidoVenta().setFormaDePago(formaDePago);
 	}
 
 	public void persistCurrentPedidoVenta() throws BusinessException {
 		logger.debug("->persistCurrentPedidoVenta: ");
 
 		EntityManager em = null;
 		try {
 			em = emf.createEntityManager();
 			logger.debug("->EntityManager em, created");
 			em.getTransaction().begin();
 			logger.debug("->begin transaction");
 			PedidoVenta nuevoPedidoVenta = new PedidoVenta();
 			logger.debug("->prepare PedidoVenta");
 			Usuario usuario = applicationSession.getUsuario();
 			logger.debug("->\tUsuario:" + usuario);
 			logger.debug("->\tSucursal:" + usuario.getSucursal());
 			logger.debug("->\tSucursal in Session:" + applicationSession.getSucursal());
 			logger.debug("->\tAlmacen in Session:" + applicationSession.getAlmacen());
 
 			nuevoPedidoVenta.setUsuario(em.getReference(Usuario.class, usuario.getUsuarioId()));
 			nuevoPedidoVenta.setCliente(em.getReference(Cliente.class, applicationSession.getPedidoVenta().getCliente().getId()));
 			nuevoPedidoVenta.setFormaDePago(em.getReference(FormaDePago.class, applicationSession.getPedidoVenta().getFormaDePago().getId()));
 			nuevoPedidoVenta.setComentarios(applicationSession.getPedidoVenta().getComentarios());
 			nuevoPedidoVenta.setFactoriva(getFactorIVA());
 			nuevoPedidoVenta.setAlmacen(applicationSession.getAlmacen());
 
 			em.persist(nuevoPedidoVenta);
 			logger.debug("->ok, Pedido inserted");
 
 			PedidoVentaEstado pedidoVentaEstado = null;
 			pedidoVentaEstado = new PedidoVentaEstado();
 
 			pedidoVentaEstado.setPedidoVenta(nuevoPedidoVenta);
 			pedidoVentaEstado.setEstado(new Estado(Constants.ESTADO_CAPTURADO));
 			pedidoVentaEstado.setFecha(new Date());
 			pedidoVentaEstado.setUsuario(usuario);
 			pedidoVentaEstado.setComentarios("TEST CASE INSERT");
 
 			List<PedidoVentaEstado> listPedidoVentaEstados = new ArrayList<PedidoVentaEstado>();
 			listPedidoVentaEstados.add(pedidoVentaEstado);
 
 			Collection<PedidoVentaDetalle> pedidoVentaDetalleCollection = applicationSession.getPedidoVenta().getPedidoVentaDetalleCollection();
 
 			List<PedidoVentaDetalle> pedidoVentaDetalleCollectionInsert = new ArrayList<PedidoVentaDetalle>();
 
 			em.persist(pedidoVentaEstado);
 			logger.debug("->ok, PedidoVentaEstado inserted");
 
 			for (PedidoVentaDetalle pvd : pedidoVentaDetalleCollection) {
 				PedidoVentaDetalle pvdInsert = new PedidoVentaDetalle();
 
 				pvdInsert.setCantidad(pvd.getCantidad());
 				pvdInsert.setPedidoVenta(nuevoPedidoVenta);
 				pvdInsert.setPrecioVenta(pvd.getPrecioVenta());
 				pvdInsert.setProducto(em.getReference(Producto.class, pvd.getProducto().getId()));
 
 				pedidoVentaDetalleCollectionInsert.add(pvdInsert);
 				pvd.setPedidoVenta(nuevoPedidoVenta);
 				pvd.setProducto(pvd.getProducto());
 				em.persist(pvd);
 
 				logger.debug("->\tok, PedidoVentaDetalle inserted");
 			}
 
 			em.getTransaction().commit();
 			logger.debug("->committed.");
 			//em.refresh(nuevoPedidoVenta);
 			//applicationSession.setPedidoVenta(nuevoPedidoVenta);
 //			logger.debug("->refreshed: nuevoPedidoVenta.id=" + nuevoPedidoVenta.getId());
 //			Collection<PedidoVentaDetalle> pedidoVentaDetalleCollectionForIteration = nuevoPedidoVenta.getPedidoVentaDetalleCollection();
 //			for (PedidoVentaDetalle dvpI : pedidoVentaDetalleCollectionForIteration) {
 //				logger.debug("->\trefreshed: nuevoPedidoVenta.getPedidoVentaDetalleCollection.producto=" + dvpI.getProducto());
 //			}
 //			//
 
 		} catch (Exception e) {
 			logger.error("Exception caught:", e);
 			if (em.getTransaction().isActive()) {
 				em.getTransaction().rollback();
 			}
 			throw new BusinessException(getClass().getSimpleName(), "APP_LOGIC_PEDIDO_NOT_PERSISTED");
 		} finally {
 			if (em != null) {
 				em.close();
 				logger.debug("->Ok, Entity Manager Closed");
 			}
 		}
 
 	}
 
 	public Sucursal getSucursalMatriz() throws BusinessException {
 		logger.debug("->getSucursalMatriz: ");
 		Sucursal sucursalMatriz = null;
 		EntityManager em = null;
 		try {
 			em = emf.createEntityManager();
 			logger.debug("->EntityManager em, created");
 			Query q = em.createQuery("select s from Sucursal s where s.sucursal is null");
 			sucursalMatriz = (Sucursal) q.getSingleResult();
 			Collection<Almacen> almacenCollection = sucursalMatriz.getAlmacenCollection();
 			for (Almacen a : almacenCollection) {
 				a.getTipoAlmacen();
 				Collection<AlmacenProducto> almacenProductoCollection = a.getAlmacenProductoCollection();
 				for (AlmacenProducto ap : almacenProductoCollection) {
 					ap.getProducto();
 				}
 			}
 			return sucursalMatriz;
 
 		} catch (NoResultException nre) {
 			throw new BusinessException("Login", "No hay sucursl Default");
 		} catch (Exception e) {
 			throw new BusinessException("Login", "No hay sucursl Default:" + e);
 		} finally {
 			if (em != null) {
 				em.close();
 				logger.debug("->Ok, Entity Manager Closed");
 			}
 
 		}
 
 	}
 
 	public Sucursal getSucursal(int sucursalId) throws BusinessException {
 		logger.debug("->getSucursalMatriz: ");
 		Sucursal sucursalMatriz = null;
 		EntityManager em = null;
 		try {
 			em = emf.createEntityManager();
 			logger.debug("->EntityManager em, created");
 			Query q = em.createQuery("select s from Sucursal s where s.id =:sucursalId");
 			q.setParameter("sucursalId", sucursalId);
 
 			sucursalMatriz = (Sucursal) q.getSingleResult();
 			Collection<Almacen> almacenCollection = sucursalMatriz.getAlmacenCollection();
 			for (Almacen a : almacenCollection) {
 				a.getTipoAlmacen();
 				Collection<AlmacenProducto> almacenProductoCollection = a.getAlmacenProductoCollection();
 				for (AlmacenProducto ap : almacenProductoCollection) {
 					ap.getProducto();
 				}
 			}
 		} catch (Exception e) {
 
 			logger.error("Exception caught:", e);
 		} finally {
 			if (em != null) {
 				em.close();
 				logger.debug("->Ok, Entity Manager Closed");
 			}
 			return sucursalMatriz;
 		}
 
 	}
 
 	public void sendPedidosAndDelete(ProgressProcessListener pl) throws AuthenticationException, PedidoVentaException {
 		sendDataSynchronizer.sendAndDeletePedidos();
 	}
 
 	public void persistCliente(Cliente c) throws BusinessException {
 		Cliente parecidoRfc = null;
 		if (c.getId() == null) {
 			try {
 				parecidoRfc = basicInfoDAO.getClienteByRFC(c);
 			} catch (NoResultException ex1) {
 				logger.debug("->Ok, not Found by RFC");
 			} catch (NonUniqueResultException ex) {
 				throw new BusinessException(
 						ApplicationInfo.getLocalizedMessage("DLG_EDIT_CLEINTE_TITLE"),
 						"APP_LOGIC_CLIENTE_EXIST_RFC");
 			}
 		}
 
 		try {
 
 			if (c.getId() == null) {
 				//persistEntityWithTransactionDAO.persistCliente(c);
 				persistEntityWithTransactionDAO.create(c);
 			} else {
 				persistEntityWithTransactionDAO.updateCliente(c);
 
 			}
 		} catch (NoResultException ex) {
 			throw new BusinessException(
 					ApplicationInfo.getLocalizedMessage("DLG_EDIT_CLEINTE_TITLE"),
 					"APP_LOGIC_CLIENTE_NOT_FOUND");
 		} catch (Exception ex) {
 			logger.error("Exception caught:", ex);
 			throw new BusinessException(
 					ApplicationInfo.getLocalizedMessage("DLG_EDIT_CLEINTE_TITLE"),
 					"APP_LOGIC_CLIENTE_NOT_SAVED");
 		}
 	}
 
 	public void removeCliente(Cliente c) throws BusinessException {
 		try {
 			persistEntityWithTransactionDAO.deleteCliente(c);
 		} catch (NoResultException ex) {
 			throw new BusinessException(
 					ApplicationInfo.getLocalizedMessage("DLG_EDIT_CLEINTE_TITLE"),
 					"APP_LOGIC_CLIENTE_NOT_FOUND");
 		} catch (Exception ex) {
 			logger.error("Exception caught:", ex);
 			throw new BusinessException(
 					ApplicationInfo.getLocalizedMessage("DLG_EDIT_CLEINTE_TITLE"),
 					"APP_LOGIC_CLIENTE_NOT_DELETED");
 		}
 	}
 
 	public void printTicketPedido(PedidoVenta pedidoVenta, boolean sendToTprinter, String recibimos, String cambio) throws BusinessException {
 		Object printedObject = null;
 
 		try {
 			String propPrinterMode = preferences.getProperty(PreferencesController.PRINTER_MODE,TicketBlueToothPrinter.TERMAL_PRINTER_MODE);
 		
 			logger.debug("printTicketPedido: read properties -> propPrinterMode= "+propPrinterMode);
 			if(propPrinterMode.equals(TicketBlueToothPrinter.BT_PRINTER_MODE) ){
 				cambiarTipoImpresionBlueTooth();
 			} else if(propPrinterMode.equals(TicketBlueToothPrinter.TERMAL_PRINTER_MODE)){							
 				cambiarTipoImpresionTermicaPOS();
 			}
 			
 			
 			HashMap<String, String> extraInformation = new HashMap<String, String>();
 			extraInformation.put("recibimos", recibimos);
 			extraInformation.put("cambio", cambio);
 
 			printedObject = ticketPrinterService.generateTicket(pedidoVenta, extraInformation);
 			logger.debug("-> printTicketPedido: ticketFileName=" + printedObject);
 		} catch (Exception ex) {
 			logger.error("-> printTicketPedido:TicketPrinter.generateTicket ", ex);
 			throw new BusinessException(getClass().getSimpleName(), "APP_LOGIC_TICKET_NOT_GENERATED");
 		}
 
 		if (sendToTprinter) {
 			try {
 				logger.debug("-> printTicketPedido:now send to default Printer");
 				//TicketBlueToothPrinter.print(ticketFileName);
 				ticketPrinterService.sendToPrinter(printedObject);
 
 			} catch (Exception ex) {
 				logger.error("-> printTicketPedido:TicketPrinter.generateTicket ", ex);
 				throw new BusinessException(getClass().getSimpleName(), "APP_LOGIC_TICKET_NOT_PRINTED");
 			}
 		}
 	}
 
 	public void testPrinter() throws BusinessException {
 		try {
 			String propPrinterMode = preferences.getProperty(PreferencesController.PRINTER_MODE,TicketBlueToothPrinter.TERMAL_PRINTER_MODE);
 		
 			logger.debug("testPrinter: read properties -> propPrinterMode= "+propPrinterMode);
 			if(propPrinterMode.equals(TicketBlueToothPrinter.BT_PRINTER_MODE) ){
 				cambiarTipoImpresionBlueTooth();
 			} else if(propPrinterMode.equals(TicketBlueToothPrinter.TERMAL_PRINTER_MODE)){			
 				cambiarTipoImpresionTermicaPOS();
 			}
 			
 			
 			
 			logger.debug("-> testPrinter:TicketPrinter.testDefaultPrinter ?");
 			ticketPrinterService.testDefaultPrinter();
 			logger.debug("-> testPrinter: Ok !");
 		} catch (Exception ex) {
 			logger.error("-> testPrinter:TicketPrinter.testDefaultPrinter", ex);
 			throw new BusinessException(getClass().getSimpleName(), "APP_LOGIC_TICKET_NOT_PRINTED");
 		}
 	}
 
 	public void exit() throws BusinessException {
 		if (applicationSession.getPedidoVenta().getPedidoVentaDetalleCollection().size() > 0) {
 			throw new BusinessException(getClass().getSimpleName(), "APP_LOGIC_PEDIDO_NOT_SAVED");
 		}
 	}
 
 	@Autowired
 	public void setApplicationSession(ApplicationSession as) {
 		applicationSession = as;
 		readPreferences();
 	}
 
 	public ApplicationSession getSession() {
 		return applicationSession;
 	}
 	ProductoFastDisplayModel[] arrProds;
 	//LinkedHashMap<Integer,Producto> productosFastTable;
 
 	/**
 	 * @return the producto4Display
 	 */
 	public ProductoFastDisplayModel[] getProducto4Display() {
 		logger.debug("->getProducto4Display()");
 		if (arrProds == null) {
 			try {
 				//productosFastTable = new LinkedHashMap<Integer,Producto> ();
 				List<Producto> producto4Display = null;
 				producto4Display = basicInfoDAO.getProductos4DisplayList();
 				arrProds = new ProductoFastDisplayModel[producto4Display.size()];
 				int ap = 0;
 				for (Producto p : producto4Display) {
 					arrProds[ap++] = new ProductoFastDisplayModel(
 							p.getId(),
 							p.getId() != null ? (p.getNombre() + "(" + p.getPresentacion() + "):" + p.getContenido() + "" + p.getUnidadMedida()) : "",
 							p);
 					//productosFastTable.put(p.getId(), p);
 				}
 			} catch (Exception ex) {
 				logger.error("", ex);
 			}
 		}
 
 		return arrProds;
 	}
 
 	public Producto findProductoByCodigoDeBarras(String codigoDeBarras) throws Exception {
 		logger.debug("->findProductoByCodigoDeBarras():codigoDeBarras=" + codigoDeBarras);
 
 		return basicInfoDAO.findProductoByCodigoDeBarras(codigoDeBarras);
 	}
 
 	public ProductoFastDisplayModel[] getProducto4Display(String searchString) {
 		ArrayList<ProductoFastDisplayModel> found = new ArrayList<ProductoFastDisplayModel>();
 		ProductoFastDisplayModel[] foundArray = null;
 		ProductoFastDisplayModel[] search = getProducto4Display();
 		if (search != null) {
 			if (searchString.trim().length() > 0) {
 				for (ProductoFastDisplayModel pfd : search) {
 					if (pfd.getForDisplay().toLowerCase().indexOf(searchString.toLowerCase()) >= 0) {
 						found.add(pfd);
 					}
 				}
 			}
 		}
 
 		foundArray = new ProductoFastDisplayModel[found.size()];
 		found.toArray(foundArray);
 		return foundArray;
 	}
 
 	/**
 	 * @return the factorIVA
 	 */
 	public double getFactorIVA() {
 		return factorIVA;
 	}
 
 	/**
 	 * @param factorIVA the factorIVA to set
 	 */
 	public void setFactorIVA(double factorIVA) {
 		this.factorIVA = factorIVA;
 	}
 //    public Producto getProducto(Integer id){
 //        return productosFastTable.get(id);
 //    }
 
 	void setComentariosToCurrentPedidoVenta(String comentarios) {
 		this.getSession().getPedidoVenta().setComentarios(comentarios);
 	}
 
 	/**
 	 * @return the applicationSession
 	 */
 	@Autowired
 	public ApplicationSession getApplicationSession() {
 		return applicationSession;
 	}
 
 	/**
 	 * @param sendDataSynchronizer the sendDataSynchronizer to set
 	 */
 	@Autowired
 	public void setSendDataSynchronizer(SendDataSynchronizer sendDataSynchronizer) {
 		this.sendDataSynchronizer = sendDataSynchronizer;
 	}
 	private BufferedImage defaultImageForProducto;
 
 	public BufferedImage getDefaultImageForProducto() {
 		if (this.defaultImageForProducto == null) {
 			try {
 				this.defaultImageForProducto = ImageIO.read(ApplicationLogic.class.getResource("/imgs/proximamente_2.jpg"));
 			} catch (IOException ex) {
 				logger.error("DefaultImageForProducto not found:", ex);
 			}
 		}
 		return this.defaultImageForProducto;
 	}
 
 	public static String getMD5Encrypted(String e) {
 
 		MessageDigest mdEnc = null; // Encryption algorithm
 		try {
 			mdEnc = MessageDigest.getInstance("MD5");
 		} catch (NoSuchAlgorithmException ex) {
 			return null;
 		}
 		mdEnc.update(e.getBytes(), 0, e.length());
 		return (new BigInteger(1, mdEnc.digest())).toString(16);
 	}
 
 	public Object[][] concentradoVentasUsuario(Date fechaCorteInicial, Date fechaCorteFinal) {
 		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
 		SimpleDateFormat sdfParam = new SimpleDateFormat("yyyy-MM-dd");
 		logger.debug("---------->concentradoVentasUsuario: fechaCorteInicial=" + sdfParam.format(fechaCorteInicial) + ", =fechaCorteFinal=" + sdfParam.format(fechaCorteFinal));
 		EntityManager em = null;
 		Object[][] result = null;
 
 		try {
 			em = emf.createEntityManager();
 			logger.debug("->EntityManager em, created");
 			final String query = "SELECT   U.NOMBRE_COMPLETO,PV.USUARIO_ID,A.TIPO_ALMACEN,CAST(PVE.FECHA AS DATE) AS FECHA_DIARIA, (SUM(CANTIDAD*PRECIO_VENTA)) AS IMPORTE_PEDIDO\n"
 					+ "FROM     PEDIDO_VENTA_DETALLE PVD, PEDIDO_VENTA PV, PEDIDO_VENTA_ESTADO PVE,USUARIO U,ALMACEN A\n"
 					+ "WHERE    1=1\n"
 					+ "AND      PVD.PEDIDO_VENTA_ID = PV.ID\n"
 					+ "AND      PVE.PEDIDO_VENTA_ID = PV.ID\n"
 					+ "AND      PVE.PEDIDO_VENTA_ID = PV.ID\n"
 					+ "AND      PV.ALMACEN_ID       = A.ID\n"
 					+ "AND      U.USUARIO_ID        = PV.USUARIO_ID\n"
 					+ "AND      PVE.ESTADO_ID       = 1\n"
 					+ "AND      CAST(PVE.FECHA AS DATE)  >= :fechaCorteInicial \n"
 					+ "AND      CAST(PVE.FECHA AS DATE)  <= :fechaCorteFinal \n"
 					+ "GROUP BY U.NOMBRE_COMPLETO,PV.USUARIO_ID,A.TIPO_ALMACEN,PVE.FECHA\n"
 					+ "ORDER BY U.NOMBRE_COMPLETO,PV.USUARIO_ID,A.TIPO_ALMACEN,PVE.FECHA";
 
 			logger.debug("============>concentradoVentasUsuario: query=" + query);
 
 			Query q = em.createNativeQuery(query);
 
 			q.setParameter("fechaCorteInicial", sdfParam.format(fechaCorteInicial));
 			q.setParameter("fechaCorteFinal", sdfParam.format(fechaCorteFinal));
 
 			ArrayList resultList = (ArrayList) q.getResultList();
 			int index = 0;
 			LinkedHashMap<String, Object[]> concentradoUsuario = new LinkedHashMap<String, Object[]>();
 			logger.debug("============>concentradoVentasUsuario: resultList.size=" + resultList.size());
 			final Iterator iterator = resultList.iterator();
 
 			while (iterator.hasNext()) {
 				final Object next = iterator.next();
 				Object[] row = (Object[]) next;
 
 				String usr = (String) row[0];
 				String date = sdf.format((Date) row[3]);
 				Double importe = (Double) row[4];
 				//logger.debug("============>concentradoVentasUsuario["+index+"]: usr="+usr);
 				Object[] rowEncontrado = (Object[]) concentradoUsuario.get(usr + date);
 				Double importeEncontrado = null;
 				if (rowEncontrado != null) {
 					importeEncontrado = (Double) rowEncontrado[4];
 					rowEncontrado[4] = importeEncontrado + importe;
 					concentradoUsuario.put(usr + date, rowEncontrado);
 				} else {
 					concentradoUsuario.put(usr + date, row);
 				}
 				index++;
 			}
 
 			final Set<String> keySetUsrs = concentradoUsuario.keySet();
 			result = new Object[keySetUsrs.size()][];
 			int i = 0;
 
 			for (String usrDateKey : keySetUsrs) {
 				final Object[] row = concentradoUsuario.get(usrDateKey);
 
 				if (((Integer) row[2]).intValue() == Constants.ALMACEN_PRINCIPAL) {
 					row[2] = "NORMAL";
 				} else {
 					row[2] = "OPORTUNIDAD";
 				}
 				row[3] = sdf.format((Date) row[3]);
 				//row[4] =  //df.format((Double)row[4]);
 
 				result[i] = row;
 				i++;
 			}
 		} catch (Exception e) {
 			logger.error("-->>>concentradoVentasUsuario:", e);
 		} finally {
 			if (em != null) {
 				em.close();
 				logger.debug("->Ok, Entity Manager Closed");
 			}
 
 		}
 		return result;
 	}
 
 	public Object[][] concentradoVentasProducto(Date fechaCorteInicial, Date fechaCorteFinal) {
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
 		SimpleDateFormat sdfParam = new SimpleDateFormat("yyyy-MM-dd");
 		logger.debug("---------->concentradoVentasProducto: fechaCorteInicial=" + sdf.format(fechaCorteInicial) + ", =fechaCorteFinal=" + sdf.format(fechaCorteFinal));
 		EntityManager em = null;
 		Object[][] result = null;
 
 		try {
 			em = emf.createEntityManager();
 			logger.debug("->EntityManager em, created");
 			Query q = em.createNativeQuery(
					"SELECT     PVD.CANTIDAD,P.CODIGO_BARRAS,P.NOMBRE,P.PRESENTACION,A.TIPO_ALMACEN, (SUM(PVD.CANTIDAD*PVD.PRECIO_VENTA)) AS IMPORTE_PEDIDO "
 					+ "FROM     PEDIDO_VENTA_DETALLE PVD, PEDIDO_VENTA PV, PEDIDO_VENTA_ESTADO PVE,PRODUCTO P,ALMACEN A "
 					+ "WHERE    1=1 "
 					+ "AND      PVD.PEDIDO_VENTA_ID = PV.ID "
 					+ "AND      PVE.PEDIDO_VENTA_ID = PV.ID "
 					+ "AND      PVE.PEDIDO_VENTA_ID = PV.ID "
 					+ "AND      PVD.PRODUCTO_ID     = P.ID "
 					+ "AND      PV.ALMACEN_ID       = A.ID "
 					+ "AND      PVE.ESTADO_ID = 1 "
 					+ "AND      CAST(PVE.FECHA AS DATE)  >= :fechaCorteInicial "
 					+ "AND      CAST(PVE.FECHA AS DATE)  <= :fechaCorteFinal "
 					+ "GROUP BY P.CODIGO_BARRAS,P.NOMBRE,P.PRESENTACION,A.TIPO_ALMACEN,PVD.CANTIDAD "
 					+ "ORDER BY P.NOMBRE,P.PRESENTACION,A.TIPO_ALMACEN ");
 
 			q.setParameter("fechaCorteInicial", sdfParam.format(fechaCorteInicial));
 			q.setParameter("fechaCorteFinal", sdfParam.format(fechaCorteFinal));
 
 			ArrayList resultList = (ArrayList) q.getResultList();
 
 			LinkedHashMap<String, Object[]> concentradoUsuario = new LinkedHashMap<String, Object[]>();
 			//logger.debug("============>concentradoVentasProducto: resultList.size="+resultList.size());
 
 
 			result = new Object[resultList.size()][];
 			int i = 0;
 			//DecimalFormat df = new DecimalFormat("$###,###,##0.00");
 
 			final Iterator iterator = resultList.iterator();
 			while (iterator.hasNext()) {
 				final Object next = iterator.next();
 				Object[] row = (Object[]) next;
 
 				//logger.debug("============>concentradoVentasProducto["+i+"]: prod="+row[1]);
 				if (((Integer) row[4]).intValue() == Constants.ALMACEN_PRINCIPAL) {
 					row[4] = "NORMAL";
 				} else {
 					row[4] = "OPORTUNIDAD";
 				}
 				//row[5] =  df.format((Double)row[5]);
 
 				result[i] = row;
 				i++;
 			}
 		} catch (Exception e) {
 			logger.error("-->>>concentradoVentasProducto:", e);
 		} finally {
 			if (em != null) {
 				em.close();
 				logger.debug("->Ok, Entity Manager Closed");
 			}
 
 		}
 		return result;
 	}
 
 	public Object[][] concentradoVentasTicket(Date fechaCorteInicial, Date fechaCorteFinal) {
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
 		SimpleDateFormat sdfParam = new SimpleDateFormat("yyyy-MM-dd");
 		logger.debug("---------->concentradoVentasTicket: fechaCorteInicial=" + sdf.format(fechaCorteInicial) + ", =fechaCorteFinal=" + sdf.format(fechaCorteFinal));
 		EntityManager em = null;
 		Object[][] result = null;
 
 		try {
 			em = emf.createEntityManager();
 			logger.debug("->EntityManager em, created");
 			Query q = em.createNativeQuery(
 					"SELECT   A.TIPO_ALMACEN,U.NOMBRE_COMPLETO,C.RAZON_SOCIAL,FP.DESCRIPCION,PV.ID,PVE.FECHA,(SUM(PVD.CANTIDAD*PVD.PRECIO_VENTA)) AS IMPORTE_PEDIDO "
 					+ "FROM     PEDIDO_VENTA_DETALLE PVD, PEDIDO_VENTA PV, PEDIDO_VENTA_ESTADO PVE,USUARIO U, CLIENTE C,ALMACEN A, FORMA_DE_PAGO FP "
 					+ "WHERE    1=1 "
 					+ "AND      PVD.PEDIDO_VENTA_ID = PV.ID "
 					+ "AND      PVE.PEDIDO_VENTA_ID = PV.ID "
 					+ "AND      PVE.PEDIDO_VENTA_ID = PV.ID "
 					+ "AND      PV.CLIENTE_ID       = C.ID "
 					+ "AND      PV.USUARIO_ID       = U.USUARIO_ID "
 					+ "AND      PV.ALMACEN_ID       = A.ID "
 					+ "AND      PV.FORMA_DE_PAGO_ID = FP.ID "
 					+ "AND      PVE.ESTADO_ID       = 1"
 					+ "AND      CAST(PVE.FECHA AS DATE)  >= :fechaCorteInicial "
 					+ "AND      CAST(PVE.FECHA AS DATE)  <= :fechaCorteFinal "
 					+ "GROUP BY A.TIPO_ALMACEN,U.NOMBRE_COMPLETO,C.RAZON_SOCIAL,FP.DESCRIPCION,PV.ID,PVE.FECHA "
 					+ "ORDER BY A.TIPO_ALMACEN,U.NOMBRE_COMPLETO,C.RAZON_SOCIAL,FP.DESCRIPCION,PV.ID,PVE.FECHA ");
 
 			q.setParameter("fechaCorteInicial", sdfParam.format(fechaCorteInicial));
 			q.setParameter("fechaCorteFinal", sdfParam.format(fechaCorteFinal));
 
 			ArrayList resultList = (ArrayList) q.getResultList();
 
 			LinkedHashMap<String, Object[]> concentradoUsuario = new LinkedHashMap<String, Object[]>();
 			logger.debug("============>concentradoVentasTicket: resultList.size=" + resultList.size());
 
 
 			result = new Object[resultList.size()][];
 			int i = 0;
 			//DecimalFormat df = new DecimalFormat("$###,###,##0.00");
 
 			final Iterator iterator = resultList.iterator();
 			while (iterator.hasNext()) {
 				final Object next = iterator.next();
 				Object[] row = (Object[]) next;
 
 				//logger.debug("============>concentradoVentasTicket["+i+"]: prod="+row[1]);
 				if (((Integer) row[0]).intValue() == Constants.ALMACEN_PRINCIPAL) {
 					row[0] = "NORMAL";
 				} else {
 					row[0] = "OPORTUNIDAD";
 				}
 				row[5] = sdf.format((Timestamp) row[5]);
 				result[i] = row;
 				i++;
 			}
 		} catch (Exception e) {
 			logger.error("-->>>concentradoVentasTicket:", e);
 		} finally {
 			if (em != null) {
 				em.close();
 				logger.debug("->Ok, Entity Manager Closed");
 			}
 
 		}
 		return result;
 	}
 	
 	public PedidoVenta findPedidoVenta(Integer id) {
         EntityManager em = emf.createEntityManager();
         try {
             PedidoVenta pedidoVenta = em.find(PedidoVenta.class, id);
 			
 			pedidoVenta.getUsuario();
 			pedidoVenta.getCliente();
 			pedidoVenta.getCliente().getPoblacion();
 			pedidoVenta.getAlmacen();
 			pedidoVenta.getFormaDePago();
 			
             Collection<PedidoVentaDetalle> detalleVentaPedidoCollection = pedidoVenta.getPedidoVentaDetalleCollection();
             for(PedidoVentaDetalle detalleVentaPedido: detalleVentaPedidoCollection) {
             }
 
             Collection<PedidoVentaEstado> pedidoVentaEstadoCollection = pedidoVenta.getPedidoVentaEstadoCollection();
             for(PedidoVentaEstado pedidoVentaEstado:pedidoVentaEstadoCollection) {
             }
             
             return pedidoVenta;
         } finally {
             em.close();
         }
     }
 
 	public List<Date> getRangoFechasDeVentas() {
 		List<Date> result = new ArrayList<Date>();
 		EntityManager em = null;
 
 		try {
 			em = emf.createEntityManager();
 			logger.debug("->EntityManager em, created");
 			Query q = em.createNativeQuery(
 					"SELECT DISTINCT(CAST(PVE.FECHA AS DATE)) AS FECHA_DIARIA FROM     PEDIDO_VENTA_ESTADO PVE");
 
 			ArrayList resultList = (ArrayList) q.getResultList();
 
 			final Iterator iterator = resultList.iterator();
 			while (iterator.hasNext()) {
 				final Object next = iterator.next();
 
 				result.add((Date) next);
 			}
 
 			if (result.size() == 0) {
 				result.add(new Date());
 			}
 		} catch (Exception e) {
 			logger.error("-->>>getRangoFechasDeVentas:", e);
 		} finally {
 			if (em != null) {
 				em.close();
 				logger.debug("->Ok, Entity Manager Closed");
 			}
 
 		}
 
 		return result;
 	}
 
 	private void readPreferences() {
 		InputStream is = null;
 		try {
 			is = new FileInputStream(CONFIG_PREFERENCESPROPERTIES);
 			//
 		} catch (FileNotFoundException ex) {
 			is = ApplicationLogic.class.getResourceAsStream("/config/");
 		}
 
 		Properties preferencesNew = new Properties();
 		try {
 			preferencesNew.load(is);
 		} catch (IOException ex) {
 			logger.error("readPreferences:", ex);
 		}
 
 		if (is != null) {
 			try {
 				is.close();
 			} catch (IOException ex) {
 			}
 		}
 
 		setPreferences(preferencesNew);
 	}
 
 	public void updatePreferences() {
 		FileOutputStream fos = null;
 		try {
 			File dirPrefs = new File(CONFIG_PREFERENCES_DIR);
 			if (!dirPrefs.exists()) {
 				dirPrefs.mkdirs();
 			}
 			fos = new FileOutputStream(CONFIG_PREFERENCESPROPERTIES);
 			getPreferences().store(fos, "Updated :" + new Date());
 		} catch (IOException ex) {
 			logger.error("readPreferences:", ex);
 			throw new IllegalStateException("No s epuede guardad:"+ex.getMessage());
 		}
 	}
 
 	/**
 	 * @return the preferences
 	 */
 	public Properties getPreferences() {
 		if(preferences == null){
 			readPreferences();
 		}
 		return preferences;
 	}
 
 	/**
 	 * @param preferences the preferences to set
 	 */
 	private void setPreferences(Properties preferences) {
 		this.preferences = preferences;
 	}
 
 	public void actualizaPrecio(Producto productoEscaneado, Almacen almacen,Double precioNuevo) {
 		EntityManager em = null;
 		int updated=0;
 		try {
 			em = emf.createEntityManager();
 			em.getTransaction().begin();
 			logger.debug("->EntityManager em, created");
 			Query q = em.createNativeQuery(
 					"UPDATE ALMACEN_PRODUCTO SET PRECIO_VENTA=:precioVenta WHERE PRODUCTO_ID=:productoId AND ALMACEN_ID=:almacenId");
 
 			q.setParameter("precioVenta", precioNuevo);
 			q.setParameter("productoId", productoEscaneado.getId());
 			q.setParameter("almacenId", almacen.getId());
 			
 			updated = q.executeUpdate();
 			
 			if(updated<1){
 				throw new IllegalArgumentException("No se pudo actualizar, pues n o se encotro producto");
 			}
 			em.getTransaction().commit();
 		} catch (Exception e) {
 			logger.error("-->>>getRangoFechasDeVentas:", e);
 		} finally {
 			if (em != null) {
 				em.close();
 				logger.debug("->Ok, Entity Manager Closed");
 			}
 
 		}
 		
 	}
 }
