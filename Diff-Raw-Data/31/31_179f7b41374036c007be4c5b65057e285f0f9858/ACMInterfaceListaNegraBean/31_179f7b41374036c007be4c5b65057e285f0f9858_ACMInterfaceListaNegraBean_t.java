 package uy.com.amensg.logistica.bean;
 
 import java.text.DateFormat;
 import java.util.Collection;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 
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
 
 import uy.com.amensg.logistica.entities.ACMInterfaceListaNegra;
 import uy.com.amensg.logistica.entities.MetadataCondicion;
 import uy.com.amensg.logistica.entities.MetadataConsulta;
 import uy.com.amensg.logistica.entities.MetadataConsultaResultado;
 import uy.com.amensg.logistica.entities.MetadataOrdenacion;
 import uy.com.amensg.logistica.util.QueryHelper;
 
 @Stateless
 public class ACMInterfaceListaNegraBean implements IACMInterfaceListaNegraBean {
 
 	@PersistenceContext(unitName = "uy.com.amensg.logistica.persistenceUnit")
 	private EntityManager entityManager;
 	
 	private CriteriaQuery<ACMInterfaceListaNegra> criteriaQuery;
 	
 	public MetadataConsultaResultado list(MetadataConsulta metadataConsulta) {
 		MetadataConsultaResultado result = new MetadataConsultaResultado();
 		
 		try {
 			// Query para obtener los registros de muestra
 			TypedQuery<ACMInterfaceListaNegra> queryMuestra = this.construirQuery(metadataConsulta);
 			queryMuestra.setMaxResults(metadataConsulta.getTamanoMuestra().intValue());
 			
 			Collection<Object> registrosMuestra = new LinkedList<Object>();
 			for (ACMInterfaceListaNegra acmInterfaceListaNegra : queryMuestra.getResultList()) {
 				registrosMuestra.add(acmInterfaceListaNegra);
 			}
 			
 			result.setRegistrosMuestra(registrosMuestra);
 			
 			CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
 			
 			// Query para obtener la cantidad de registros
 			CriteriaQuery<Long> criteriaQueryCount = criteriaBuilder.createQuery(Long.class);
 			
 			criteriaQueryCount.select(
 				criteriaBuilder.count(criteriaQueryCount.from(ACMInterfaceListaNegra.class))
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
 
 	private TypedQuery<ACMInterfaceListaNegra> construirQuery(MetadataConsulta metadataConsulta) {
 		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
 		
 		criteriaQuery = criteriaBuilder.createQuery(ACMInterfaceListaNegra.class);
 		
 		Root<ACMInterfaceListaNegra> root = criteriaQuery.from(ACMInterfaceListaNegra.class);
 		
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
 		
 		TypedQuery<ACMInterfaceListaNegra> query = entityManager.createQuery(criteriaQuery);
 		
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
