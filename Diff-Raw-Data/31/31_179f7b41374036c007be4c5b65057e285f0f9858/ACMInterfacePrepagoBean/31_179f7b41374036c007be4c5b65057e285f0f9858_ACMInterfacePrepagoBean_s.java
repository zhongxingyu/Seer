 package uy.com.amensg.logistica.bean;
 
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.Parameter;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Order;
 import javax.persistence.criteria.Path;
 import javax.persistence.criteria.Root;
 
 import uy.com.amensg.logistica.entities.ACMInterfaceListaNegra;
 import uy.com.amensg.logistica.entities.ACMInterfaceMid;
 import uy.com.amensg.logistica.entities.ACMInterfacePrepago;
 import uy.com.amensg.logistica.entities.ACMInterfaceProceso;
 import uy.com.amensg.logistica.entities.MetadataCondicion;
 import uy.com.amensg.logistica.entities.MetadataConsulta;
 import uy.com.amensg.logistica.entities.MetadataConsultaResultado;
 import uy.com.amensg.logistica.entities.MetadataOrdenacion;
 import uy.com.amensg.logistica.util.Configuration;
 import uy.com.amensg.logistica.util.QueryHelper;
 
 @Stateless
 public class ACMInterfacePrepagoBean implements IACMInterfacePrepagoBean {
 
 	@PersistenceContext(unitName = "uy.com.amensg.logistica.persistenceUnit")
 	private EntityManager entityManager;
 	
 	@EJB
 	private IACMInterfaceProcesoBean iACMInterfaceProcesoBean;
 	
 	private CriteriaQuery<ACMInterfacePrepago> criteriaQuery;
 	
 	public Collection<ACMInterfacePrepago> list() {
 		Collection<ACMInterfacePrepago> result = new LinkedList<ACMInterfacePrepago>();
 		
 		try {
 			Query query = entityManager.createQuery("SELECT p FROM ACMInterfacePrepago p");
 			
 			for (Object object : query.getResultList()) {
 				result.add((ACMInterfacePrepago) object);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return result;
 	}
 	
 	public MetadataConsultaResultado list(MetadataConsulta metadataConsulta) {
 		MetadataConsultaResultado result = new MetadataConsultaResultado();
 		
 		try {
 			// Query para obtener los registros de muestra
 			TypedQuery<ACMInterfacePrepago> queryMuestra = this.construirQuery(metadataConsulta);
 			queryMuestra.setMaxResults(metadataConsulta.getTamanoMuestra().intValue());
 			
 			Collection<Object> registrosMuestra = new LinkedList<Object>();
 			for (ACMInterfacePrepago acmInterfacePrepago : queryMuestra.getResultList()) {
 				registrosMuestra.add(acmInterfacePrepago);
 			}
 			
 			result.setRegistrosMuestra(registrosMuestra);
 			
 			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
 			
 			// Query para obtener la cantidad de registros
 			CriteriaQuery<Long> criteriaQueryCount = criteriaBuilder.createQuery(Long.class);
 			
 			criteriaQueryCount.select(
 				criteriaBuilder.count(criteriaQueryCount.from(ACMInterfacePrepago.class))
 			);
 			
 			criteriaQueryCount.where(criteriaQuery.getRestriction());
 			
 			TypedQuery<Long> queryCount = entityManager.createQuery(criteriaQueryCount);
 			
 			for (Parameter<?> parameter : queryMuestra.getParameters()) {
 				queryCount.setParameter(parameter.getName(), queryMuestra.getParameterValue(parameter));
 			}
 			
 			result.setCantidadRegistros(queryCount.getSingleResult());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return result;
 	}
 
 	public String exportarAExcel(MetadataConsulta metadataConsulta) {
 		String result = null;
 		
 		try {
 			TypedQuery<ACMInterfacePrepago> query = this.construirQuery(metadataConsulta);
 			
 			Collection<ACMInterfacePrepago> resultList = new LinkedList<ACMInterfacePrepago>();
 			if (metadataConsulta.getTamanoSubconjunto() != null) {
 				List<ACMInterfacePrepago> toOrder = query.getResultList();
 				
 				Collections.sort(toOrder, new Comparator<ACMInterfacePrepago>() {
 					public int compare(ACMInterfacePrepago arg0, ACMInterfacePrepago arg1) {
 						Random random = new Random();
 						
 						return random.nextBoolean() ? 1 : -1;
 					}
 				});
 				
 				int i = 0;
 				for (ACMInterfacePrepago acmInterfacePrepago : toOrder) {
 					resultList.add(acmInterfacePrepago);
 					
 					i++;
 					if (i == metadataConsulta.getTamanoSubconjunto()) {
 						break;
 					}
 				}
 			} else {
 				resultList = query.getResultList();
 			}
 			
 			GregorianCalendar gregorianCalendar = new GregorianCalendar();
 			Date currentDate = gregorianCalendar.getTime();
 			
 			String fileName =
 				Configuration.getInstance().getProperty("exportacion.carpeta")
 					+ gregorianCalendar.get(GregorianCalendar.YEAR)
 					+ (gregorianCalendar.get(GregorianCalendar.MONTH) + 1)
 					+ gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH)
 					+ gregorianCalendar.get(GregorianCalendar.HOUR_OF_DAY)
 					+ gregorianCalendar.get(GregorianCalendar.MINUTE)
 					+ gregorianCalendar.get(GregorianCalendar.SECOND)
 					+ ".csv";
 					
 			PrintWriter printWriter = new PrintWriter(new FileWriter(fileName));
 			
 			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
 			SimpleDateFormat formatMesAno = new SimpleDateFormat("MM/yyyy");
 			DecimalFormat formatMonto = new DecimalFormat("0.00"); 
 			
 			for (ACMInterfacePrepago acmInterfacePrepago : resultList) {
 				acmInterfacePrepago.setFechaExportacionAnterior(
 					acmInterfacePrepago.getFechaExportacion()
 				);
 				acmInterfacePrepago.setFechaExportacion(currentDate);
 				
 				acmInterfacePrepago = entityManager.merge(acmInterfacePrepago);
 				
 				printWriter.println(
 					acmInterfacePrepago.getMid()
 					+ ";" + (acmInterfacePrepago.getMesAno() != null ?
 						formatMesAno.format(acmInterfacePrepago.getMesAno())
 						: "")
 					+ ";" + (acmInterfacePrepago.getMontoMesActual() != null ?
 						formatMonto.format(acmInterfacePrepago.getMontoMesActual())
 						: "")
 					+ ";" + (acmInterfacePrepago.getMontoMesAnterior1() != null ?
 						formatMonto.format(acmInterfacePrepago.getMontoMesAnterior1())
 						: "")
 					+ ";" + (acmInterfacePrepago.getMontoMesAnterior2() != null ? 
 						formatMonto.format(acmInterfacePrepago.getMontoMesAnterior2())
 						: "")
 					+ ";" + (acmInterfacePrepago.getMontoPromedio() != null ?
 						formatMonto.format(acmInterfacePrepago.getMontoPromedio())
 						: "")
 					+ ";" + (acmInterfacePrepago.getFechaExportacion() != null ?
 						format.format(acmInterfacePrepago.getFechaExportacion())
 						: "")
 					+ ";" + (acmInterfacePrepago.getFechaActivacionKit() != null ?
 						format.format(acmInterfacePrepago.getFechaActivacionKit())
 						: "")
 				);
 			}
 			
 			printWriter.close();
 			
 			result = fileName;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return result;
 	}
 	
 	public void deshacerAsignacion(MetadataConsulta metadataConsulta) {
 		try {
 			TypedQuery<ACMInterfacePrepago> query = entityManager.createQuery(
 				"SELECT p FROM ACMInterfacePrepago p ORDER BY p.fechaExportacion DESC",
 				ACMInterfacePrepago.class
 			);
 			
 			Date fechaExportacion = null;
 			for (ACMInterfacePrepago acmInterfacePrepago : query.getResultList()) {
 				if (fechaExportacion == null 
 					|| acmInterfacePrepago.getFechaExportacion()
 						.equals(fechaExportacion)) {
 					fechaExportacion = acmInterfacePrepago.getFechaExportacion();
 					
 					acmInterfacePrepago.setFechaExportacion(
 						acmInterfacePrepago.getFechaExportacionAnterior()
 					);
 					acmInterfacePrepago.setFechaExportacionAnterior(
 						fechaExportacion
 					);
 					
 					entityManager.merge(acmInterfacePrepago);
 				} else {
 					break;
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void reprocesar(MetadataConsulta metadataConsulta) {
 		try {
 			Date hoy = GregorianCalendar.getInstance().getTime();
 			
 			ACMInterfaceProceso acmInterfaceProceso = new ACMInterfaceProceso();
 			
 			acmInterfaceProceso.setFechaInicio(hoy);
 			
 			acmInterfaceProceso.setUact(new Long(1));
 			acmInterfaceProceso.setFact(hoy);
 			acmInterfaceProceso.setTerm(new Long(1));
 			
 			acmInterfaceProceso = iACMInterfaceProcesoBean.save(acmInterfaceProceso);
 			
 			TypedQuery<ACMInterfacePrepago> query = this.construirQuery(metadataConsulta);
 			
 			Collection<ACMInterfacePrepago> resultList = new LinkedList<ACMInterfacePrepago>();
 			if (metadataConsulta.getTamanoSubconjunto() != null) {
 				List<ACMInterfacePrepago> toOrder = query.getResultList();
 				
 				Collections.sort(toOrder, new Comparator<ACMInterfacePrepago>() {
 					public int compare(ACMInterfacePrepago arg0, ACMInterfacePrepago arg1) {
 						Random random = new Random();
 						
 						return random.nextBoolean() ? 1 : -1;
 					}
 				});
 				
 				int i = 0;
 				for (ACMInterfacePrepago acmInterfacePrepago : toOrder) {
 					resultList.add(acmInterfacePrepago);
 					
 					i++;
 					if (i == metadataConsulta.getTamanoSubconjunto()) {
 						break;
 					}
 				}
 			} else {
 				resultList = query.getResultList();
 			}
 			
 			for (ACMInterfacePrepago acmInterfacePrepago : resultList) {
 				ACMInterfaceMid acmInterfaceMid = new ACMInterfaceMid();
 				acmInterfaceMid.setEstado(
 					new Long(
 						Configuration.getInstance().getProperty("acmInterfaceEstado.ParaProcesarPrioritario")
 					)
 				);
 				acmInterfaceMid.setMid(acmInterfacePrepago.getMid());
 				acmInterfaceMid.setProcesoId(acmInterfaceProceso.getId());
 				
 				acmInterfaceMid.setUact(new Long(1));
 				acmInterfaceMid.setFact(hoy);
 				acmInterfaceMid.setTerm(new Long(1));
 				
 				entityManager.merge(acmInterfaceMid);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void agregarAListaNegra(MetadataConsulta metadataConsulta) {
 		try {
 			TypedQuery<ACMInterfacePrepago> query = this.construirQuery(metadataConsulta);
 			
 			Date hoy = GregorianCalendar.getInstance().getTime();
 			
 			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
 			SimpleDateFormat formatMesAno = new SimpleDateFormat("MM/yyyy");
 			DecimalFormat formatMonto = new DecimalFormat("0.00");
 			
 			for (ACMInterfacePrepago acmInterfacePrepago : query.getResultList()) {
 				ACMInterfaceMid acmInterfaceMid = new ACMInterfaceMid();
 				acmInterfaceMid.setEstado(
 					new Long(Configuration.getInstance().getProperty("acmInterfaceEstado.ListaNegra"))
 				);
 				acmInterfaceMid.setMid(acmInterfacePrepago.getMid());
 				
 				acmInterfaceMid.setUact(new Long(1));
 				acmInterfaceMid.setFact(hoy);
 				acmInterfaceMid.setTerm(new Long(1));
 				
 				entityManager.merge(acmInterfaceMid); 
 				
 				ACMInterfaceListaNegra acmInterfaceListaNegra = new ACMInterfaceListaNegra();
 				
 				acmInterfaceListaNegra.setMid(acmInterfacePrepago.getMid());
 				acmInterfaceListaNegra.setObservaciones(
 					(acmInterfacePrepago.getMesAno() != null ?
 						formatMesAno.format(acmInterfacePrepago.getMesAno())
 						: "")
 					+ (acmInterfacePrepago.getMontoMesActual() != null ?
 						formatMonto.format(acmInterfacePrepago.getMontoMesActual())
 						: "")
 					+ ";" + (acmInterfacePrepago.getMontoMesAnterior1() != null ?
 						formatMonto.format(acmInterfacePrepago.getMontoMesAnterior1())
 						: "")
 					+ ";" + (acmInterfacePrepago.getMontoMesAnterior2() != null ? 
 						formatMonto.format(acmInterfacePrepago.getMontoMesAnterior2())
 						: "")
 					+ ";" + (acmInterfacePrepago.getMontoPromedio() != null ?
 						formatMonto.format(acmInterfacePrepago.getMontoPromedio())
 						: "")
 					+ ";" + (acmInterfacePrepago.getFechaActivacionKit() != null ?
 						format.format(acmInterfacePrepago.getFechaActivacionKit())
 						: "")
 				);
 				
 				acmInterfaceListaNegra.setTerm(new Long(1));
 				acmInterfaceListaNegra.setFact(hoy);
 				acmInterfaceListaNegra.setUact(new Long(1));
 				
 				entityManager.persist(acmInterfaceListaNegra);
 				
 				entityManager.remove(acmInterfacePrepago);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private TypedQuery<ACMInterfacePrepago> construirQuery(MetadataConsulta metadataConsulta) {
 		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
 		
 		criteriaQuery = criteriaBuilder.createQuery(ACMInterfacePrepago.class);
 		
 		Root<ACMInterfacePrepago> root = criteriaQuery.from(ACMInterfacePrepago.class);
 		
 		List<Order> orders = new LinkedList<Order>();
 		
 		for (MetadataOrdenacion metadataOrdenacion : metadataConsulta.getMetadataOrdenaciones()) {
 			if (metadataOrdenacion.getAscendente()) {
 				orders.add(criteriaBuilder.asc(root.get(metadataOrdenacion.getCampo())));
 			} else {
 				orders.add(criteriaBuilder.desc(root.get(metadataOrdenacion.getCampo())));
 			}
 		}
 		
 		criteriaQuery
 			.select(root)
 			.orderBy(orders)
 			.where(new QueryHelper().construirWhere(metadataConsulta, criteriaBuilder, root));
 		
 		TypedQuery<ACMInterfacePrepago> query = entityManager.createQuery(criteriaQuery);
 		
 		int i = 0;
 		for (MetadataCondicion metadataCondicion : metadataConsulta.getMetadataCondiciones()) {
 			for (String valor : metadataCondicion.getValores()) {
 				Path<?> campo = root.get(metadataCondicion.getCampo());
 				
 				try {
 					if (campo.getJavaType().equals(Date.class)) {
 						query.setParameter(
 							"p" + i,
 							DateFormat.getInstance().parse(valor)
 						);
 					} else if (campo.getJavaType().equals(Long.class)) {
 						query.setParameter(
 							"p" + i,
 							new Long(valor)
 						);
 					} else if (campo.getJavaType().equals(String.class)) {
 						query.setParameter(
 							"p" + i,
 							valor
 						);
 					} else if (campo.getJavaType().equals(Double.class)) {
 						query.setParameter(
 							"p" + i,
 							new Double(valor)
 						);
 					}
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				
 				i++;
 			}
 		}
 		
 		return query;
 	}
 }
