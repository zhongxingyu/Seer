 package uy.com.amensg.logistica.bean;
 
 import java.text.DateFormat;
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
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Order;
 import javax.persistence.criteria.Path;
 import javax.persistence.criteria.Root;
 
 import uy.com.amensg.logistica.entities.ACMInterfaceMid;
 import uy.com.amensg.logistica.entities.ACMInterfaceProceso;
 import uy.com.amensg.logistica.entities.MetadataCondicion;
 import uy.com.amensg.logistica.entities.MetadataConsulta;
 import uy.com.amensg.logistica.entities.MetadataConsultaResultado;
 import uy.com.amensg.logistica.entities.MetadataOrdenacion;
 import uy.com.amensg.logistica.util.Configuration;
 import uy.com.amensg.logistica.util.Constants;
 import uy.com.amensg.logistica.util.QueryHelper;
 
 @Stateless
 public class ACMInterfaceMidBean implements IACMInterfaceMidBean {
 
 	@PersistenceContext(unitName = "uy.com.amensg.logistica.persistenceUnit")
 	private EntityManager entityManager;
 	
 	@EJB
 	private IACMInterfaceProcesoBean iACMInterfaceProcesoBean;
 	
 	private CriteriaQuery<ACMInterfaceMid> criteriaQuery;
 	
 	public MetadataConsultaResultado listEnProceso(MetadataConsulta metadataConsulta) {
 		MetadataConsultaResultado result = new MetadataConsultaResultado();
 		
 		try {
 			// Condicion de estado "En proceso"
 			Collection<String> valores = new LinkedList<String>();
 			valores.add(Configuration.getInstance().getProperty("acmInterfaceEstado.EnProceso"));
 			
 			MetadataCondicion metadataCondicion = new MetadataCondicion();
 			metadataCondicion.setCampo("estado");
 			metadataCondicion.setOperador(Constants.__METADATA_CONDICION_OPERADOR_IGUAL);
 			metadataCondicion.setValores(valores);
 			
 			metadataConsulta.getMetadataCondiciones().add(metadataCondicion);
 			
 			// Query para obtener los registros de muestra
 			TypedQuery<ACMInterfaceMid> queryMuestra = this.construirQuery(metadataConsulta);
 			queryMuestra.setMaxResults(metadataConsulta.getTamanoMuestra().intValue());
 			
 			Collection<Object> registrosMuestra = new LinkedList<Object>();
 			for (ACMInterfaceMid acmInterfaceMid : queryMuestra.getResultList()) {
 				registrosMuestra.add(acmInterfaceMid);
 			}
 			
 			result.setRegistrosMuestra(registrosMuestra);
 			
 			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
 			
 			// Query para obtener la cantidad de registros
 			CriteriaQuery<Long> criteriaQueryCount = criteriaBuilder.createQuery(Long.class);
 			
 			criteriaQueryCount.select(
 				criteriaBuilder.count(criteriaQueryCount.from(ACMInterfaceMid.class))
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
 	
 	public void reprocesarEnProceso(MetadataConsulta metadataConsulta) {
 		// Condicion de estado "En proceso"
 		Collection<String> valores = new LinkedList<String>();
 		valores.add(Configuration.getInstance().getProperty("acmInterfaceEstado.EnProceso"));
 		
 		MetadataCondicion metadataCondicion = new MetadataCondicion();
 		metadataCondicion.setCampo("estado");
 		metadataCondicion.setOperador(Constants.__METADATA_CONDICION_OPERADOR_IGUAL);
 		metadataCondicion.setValores(valores);
 		
 		metadataConsulta.getMetadataCondiciones().add(metadataCondicion);
 		
 		this.reprocesar(metadataConsulta);
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
 			
 			TypedQuery<ACMInterfaceMid> query = this.construirQuery(metadataConsulta);
 			
 			Collection<ACMInterfaceMid> resultList = new LinkedList<ACMInterfaceMid>();
 			if (metadataConsulta.getTamanoSubconjunto() != null) {
 				List<ACMInterfaceMid> toOrder = query.getResultList();
 				
 				Collections.sort(toOrder, new Comparator<ACMInterfaceMid>() {
 					public int compare(ACMInterfaceMid arg0, ACMInterfaceMid arg1) {
 						Random random = new Random();
 						
 						return random.nextBoolean() ? 1 : -1;
 					}
 				});
 				
 				int i = 0;
 				for (ACMInterfaceMid acmInterfaceMid : toOrder) {
 					resultList.add(acmInterfaceMid);
 					
 					i++;
 					if (i == metadataConsulta.getTamanoSubconjunto()) {
 						break;
 					}
 				}
 			} else {
 				resultList = query.getResultList();
 			}
 			
 			for (ACMInterfaceMid acmInterfaceMid : query.getResultList()) {
 				acmInterfaceMid.setEstado(
 					new Long(
 						Configuration.getInstance().getProperty("acmInterfaceEstado.ParaProcesarPrioritario")
 					)
 				);
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
 	
 	public void update(ACMInterfaceMid acmInterfaceMid) {
 		try {
 			entityManager.merge(acmInterfaceMid);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private TypedQuery<ACMInterfaceMid> construirQuery(MetadataConsulta metadataConsulta) {
 		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
 		
 		criteriaQuery = criteriaBuilder.createQuery(ACMInterfaceMid.class);
 		
 		Root<ACMInterfaceMid> root = criteriaQuery.from(ACMInterfaceMid.class);
 		
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
 		
 		TypedQuery<ACMInterfaceMid> query = entityManager.createQuery(criteriaQuery);
 		
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
			
			if (metadataCondicion.getValores().size() == 0) {
				i++;
			}
 		}
 		
 		return query;
 	}
 }
