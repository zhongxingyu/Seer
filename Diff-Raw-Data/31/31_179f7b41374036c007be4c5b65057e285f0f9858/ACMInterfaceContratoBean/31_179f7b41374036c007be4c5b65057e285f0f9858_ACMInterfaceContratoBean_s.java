 package uy.com.amensg.logistica.bean;
 
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.text.DateFormat;
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
 
 import uy.com.amensg.logistica.entities.ACMInterfaceContrato;
 import uy.com.amensg.logistica.entities.ACMInterfaceListaNegra;
 import uy.com.amensg.logistica.entities.ACMInterfaceMid;
 import uy.com.amensg.logistica.entities.ACMInterfaceProceso;
 import uy.com.amensg.logistica.entities.MetadataCondicion;
 import uy.com.amensg.logistica.entities.MetadataConsulta;
 import uy.com.amensg.logistica.entities.MetadataConsultaResultado;
 import uy.com.amensg.logistica.entities.MetadataOrdenacion;
 import uy.com.amensg.logistica.entities.TipoContrato;
 import uy.com.amensg.logistica.util.Configuration;
 import uy.com.amensg.logistica.util.QueryHelper;
 
 @Stateless
 public class ACMInterfaceContratoBean implements IACMInterfaceContratoBean {
 
 	@PersistenceContext(unitName = "uy.com.amensg.logistica.persistenceUnit")
 	private EntityManager entityManager;
 	
 	@EJB
 	private IACMInterfaceProcesoBean iACMInterfaceProcesoBean;
 	
 	@EJB
 	private IACMInterfaceBean iACMInterfaceBean;
 	
 	private CriteriaQuery<ACMInterfaceContrato> criteriaQuery;
 	
 	public Collection<ACMInterfaceContrato> list() {
 		Collection<ACMInterfaceContrato> result = new LinkedList<ACMInterfaceContrato>();
 		
 		try {
 			Query query = entityManager.createQuery("SELECT c FROM ACMInterfaceContrato c");
 			
 			for (Object object : query.getResultList()) {
 				result.add((ACMInterfaceContrato) object);
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
 			TypedQuery<ACMInterfaceContrato> queryMuestra = this.construirQuery(metadataConsulta);
 			queryMuestra.setMaxResults(metadataConsulta.getTamanoMuestra().intValue());
 			
 			Collection<Object> registrosMuestra = new LinkedList<Object>();
 			for (ACMInterfaceContrato acmInterfaceContrato : queryMuestra.getResultList()) {
 				registrosMuestra.add(acmInterfaceContrato);
 			}
 			
 			result.setRegistrosMuestra(registrosMuestra);
 			
 			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
 			
 			// Query para obtener la cantidad de registros
 			CriteriaQuery<Long> criteriaQueryCount = criteriaBuilder.createQuery(Long.class);
 			
 			criteriaQueryCount.select(
 				criteriaBuilder.count(criteriaQueryCount.from(ACMInterfaceContrato.class))
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
 			TypedQuery<ACMInterfaceContrato> query = this.construirQuery(metadataConsulta);
 			
 			Collection<ACMInterfaceContrato> resultList = new LinkedList<ACMInterfaceContrato>();
 			if (metadataConsulta.getTamanoSubconjunto() != null) {
 				List<ACMInterfaceContrato> toOrder = query.getResultList();
 				
 				Collections.sort(toOrder, new Comparator<ACMInterfaceContrato>() {
 					public int compare(ACMInterfaceContrato arg0, ACMInterfaceContrato arg1) {
 						Random random = new Random();
 						
 						return random.nextBoolean() ? 1 : -1;
 					}
 				});
 				
 				int i = 0;
 				for (ACMInterfaceContrato acmInterfaceContrato : toOrder) {
 					resultList.add(acmInterfaceContrato);
 					
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
 			
 			for (ACMInterfaceContrato acmInterfaceContrato : resultList) {
 				acmInterfaceContrato.setFechaExportacionAnterior(
 					acmInterfaceContrato.getFechaExportacion()
 				);
 				acmInterfaceContrato.setFechaExportacion(currentDate);
 				
 				acmInterfaceContrato = entityManager.merge(acmInterfaceContrato);
 				
 				printWriter.println(
 					acmInterfaceContrato.getMid()
 					+ ";" + (acmInterfaceContrato.getFechaFinContrato() != null ? 
 						format.format(acmInterfaceContrato.getFechaFinContrato())
 						: "")
 					+ ";" + acmInterfaceContrato.getTipoContratoCodigo()
 					+ ";" + acmInterfaceContrato.getTipoContratoDescripcion()
 					+ ";" + acmInterfaceContrato.getDocumentoTipo()
 					+ ";'" + acmInterfaceContrato.getDocumento()
 					+ ";" + acmInterfaceContrato.getNombre()
 					+ ";" + acmInterfaceContrato.getDireccion()
 					+ ";" + acmInterfaceContrato.getCodigoPostal()
 					+ ";" + acmInterfaceContrato.getLocalidad()
 					+ ";" + acmInterfaceContrato.getEquipo()
 					+ ";" + acmInterfaceContrato.getAgente()
 					+ ";" + (acmInterfaceContrato.getFechaExportacion() != null ?
 						format.format(acmInterfaceContrato.getFechaExportacion())
 						: "")
 					+ ";" + format.format(acmInterfaceContrato.getFact())
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
 			TypedQuery<ACMInterfaceContrato> query = entityManager.createQuery(
 				"SELECT c FROM ACMInterfaceContrato c ORDER BY c.fechaExportacion DESC",
 				ACMInterfaceContrato.class
 			);
 			
 			Date fechaExportacion = null;
 			for (ACMInterfaceContrato acmInterfaceContrato : query.getResultList()) {
 				if (fechaExportacion == null 
 					|| acmInterfaceContrato.getFechaExportacion()
 						.equals(fechaExportacion)) {
 					fechaExportacion = acmInterfaceContrato.getFechaExportacion();
 					
 					acmInterfaceContrato.setFechaExportacion(
 						acmInterfaceContrato.getFechaExportacionAnterior()
 					);
 					acmInterfaceContrato.setFechaExportacionAnterior(
 						fechaExportacion
 					);
 					
 					entityManager.merge(acmInterfaceContrato);
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
 			
 			TypedQuery<ACMInterfaceContrato> query = this.construirQuery(metadataConsulta);
 			
 			Collection<ACMInterfaceContrato> resultList = new LinkedList<ACMInterfaceContrato>();
 			if (metadataConsulta.getTamanoSubconjunto() != null) {
 				List<ACMInterfaceContrato> toOrder = query.getResultList();
 				
 				Collections.sort(toOrder, new Comparator<ACMInterfaceContrato>() {
 					public int compare(ACMInterfaceContrato arg0, ACMInterfaceContrato arg1) {
 						Random random = new Random();
 						
 						return random.nextBoolean() ? 1 : -1;
 					}
 				});
 				
 				int i = 0;
 				for (ACMInterfaceContrato acmInterfaceContrato : toOrder) {
 					resultList.add(acmInterfaceContrato);
 					
 					i++;
 					if (i == metadataConsulta.getTamanoSubconjunto()) {
 						break;
 					}
 				}
 			} else {
 				resultList = query.getResultList();
 			}
 			
 			for (ACMInterfaceContrato acmInterfaceContrato : resultList) {
 				ACMInterfaceMid acmInterfaceMid = new ACMInterfaceMid();
 				acmInterfaceMid.setEstado(
 					new Long(
 						Configuration.getInstance().getProperty("acmInterfaceEstado.ParaProcesarPrioritario")
 					)
 				);
 				acmInterfaceMid.setMid(acmInterfaceContrato.getMid());
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
 			TypedQuery<ACMInterfaceContrato> query = this.construirQuery(metadataConsulta);
 			
 			Date hoy = GregorianCalendar.getInstance().getTime();
 			
 			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
 			
 			for (ACMInterfaceContrato acmInterfaceContrato : query.getResultList()) {
 				ACMInterfaceMid acmInterfaceMid = new ACMInterfaceMid();
 				acmInterfaceMid.setEstado(
 					new Long(Configuration.getInstance().getProperty("acmInterfaceEstado.ListaNegra"))
 				);
 				acmInterfaceMid.setMid(acmInterfaceContrato.getMid());
 				
 				acmInterfaceMid.setUact(new Long(1));
 				acmInterfaceMid.setFact(hoy);
 				acmInterfaceMid.setTerm(new Long(1));
 				
 				entityManager.merge(acmInterfaceMid);
 				
 				ACMInterfaceListaNegra acmInterfaceListaNegra = new ACMInterfaceListaNegra();
 				
 				acmInterfaceListaNegra.setMid(acmInterfaceContrato.getMid());
 				acmInterfaceListaNegra.setObservaciones(
 					(acmInterfaceContrato.getFechaFinContrato() != null ? 
 						format.format(acmInterfaceContrato.getFechaFinContrato())
 						: "")
 					+ ";" + acmInterfaceContrato.getTipoContratoCodigo()
 					+ ";" + acmInterfaceContrato.getTipoContratoDescripcion()
 					+ ";" + acmInterfaceContrato.getDocumentoTipo()
 					+ ";'" + acmInterfaceContrato.getDocumento()
 					+ ";" + acmInterfaceContrato.getNombre()
 					+ ";" + acmInterfaceContrato.getDireccion()
 					+ ";" + acmInterfaceContrato.getCodigoPostal()
 					+ ";" + acmInterfaceContrato.getLocalidad()
 					+ ";" + acmInterfaceContrato.getEquipo()
 					+ ";" + acmInterfaceContrato.getAgente()
 				);
 				
 				acmInterfaceListaNegra.setTerm(new Long(1));
 				acmInterfaceListaNegra.setFact(hoy);
 				acmInterfaceListaNegra.setUact(new Long(1));
 				
 				entityManager.persist(acmInterfaceListaNegra);
 				
 				entityManager.remove(acmInterfaceContrato);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public Collection<TipoContrato> listTipoContratos() {
 		Collection<TipoContrato> result = new LinkedList<TipoContrato>();
 		
 		try {
 			Query query = entityManager.createQuery(
 				"SELECT c.tipoContratoDescripcion, COUNT(c)"
 				+ " FROM ACMInterfaceContrato c"
 				+ " WHERE c.tipoContratoDescripcion IS NOT NULL"
 				+ " GROUP BY c.tipoContratoDescripcion"
 				+ " ORDER BY COUNT(c) DESC"
 			);
 			
 			for (Object object : query.getResultList()) {
 				Object[] fields = (Object[]) object;
 				
 				TipoContrato tipoContrato = new TipoContrato();
 				
 				// tipoContrato.setTipoContratoCodigo((String) fields[0]);
 				tipoContrato.setTipoContratoDescripcion((String) fields[0]);
 				
 				result.add(tipoContrato);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return result;
 	}
 	
 	private TypedQuery<ACMInterfaceContrato> construirQuery(MetadataConsulta metadataConsulta) {
 		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
 		
 		criteriaQuery = criteriaBuilder.createQuery(ACMInterfaceContrato.class);
 		
 		Root<ACMInterfaceContrato> root = criteriaQuery.from(ACMInterfaceContrato.class);
 		
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
 		
 		TypedQuery<ACMInterfaceContrato> query = entityManager.createQuery(criteriaQuery);
 		
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
