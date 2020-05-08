 package br.com.mendes.model.dao.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.hibernate.Criteria;
 import org.hibernate.Query;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.MatchMode;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 import org.primefaces.model.SortOrder;
 import org.springframework.stereotype.Repository;
 
 import br.com.mendes.model.Cliente;
 import br.com.mendes.model.dao.ClienteDAO;
 import br.com.mendes.utils.CONSTANTS;
 
 @Repository
 public class ClienteDAOImpl extends DAOImpl<Cliente, Long> implements ClienteDAO {
 
 	private static final long serialVersionUID = -7190050683815189287L;
 
 	@Override
 	public Long obterQtdeClientesNoAnoMes(Integer ano, Integer mes) {
 
 		StringBuilder hql = new StringBuilder();
 
 		hql.append(" select count(c.codCliente) " +
 				" from  Cliente c 					 " +
 				" where year(c.dataCadastro)=:ano " +
 				" and month(c.dataCadastro)=:mes ");
 
 		Query query = getSession().createQuery(hql.toString());
 
 		query.setParameter("ano", ano);
 
 		query.setParameter("mes", mes);
 
 		return (Long) query.uniqueResult();
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<Cliente> getAllSorted() {
 		Criteria criteria = getSession().createCriteria(Cliente.class);
 
 		criteria.addOrder(Order.asc("nome"));
 
 		return criteria.list();
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<Cliente> obterTodosClientesPaginados(Integer first, Integer pageSize, SortOrder sortOrder,
 			String sortField, Map<String, String> filters) {
 		Criteria criteria = getSession().createCriteria(Cliente.class, "cliente");
 		criteria.createAlias("cliente.endereco", "endereco");
 
 		if (filters != null && !filters.isEmpty()) {
 
 			List<Criterion> predicates = new ArrayList<Criterion>();
 
 			for (Entry<String, String> entry : filters.entrySet()) {
 				if (entry.getKey().equals(CONSTANTS.NOME.getDescricao())) {
 					predicates.add(Restrictions.ilike(entry.getKey(), entry.getValue() + "%", MatchMode.ANYWHERE));
 					predicates.add(Restrictions.ilike(entry.getKey().replaceAll(CONSTANTS.NOME.getDescricao(), CONSTANTS.SOBRENOME.getDescricao()),
 							entry.getValue() + "%", MatchMode.ANYWHERE));;
				} else {
					criteria.add(Restrictions.ilike(entry.getKey(), entry.getValue() + "%", MatchMode.ANYWHERE));
				}
 
				if (predicates.isEmpty()) {
					criteria.add(Restrictions.isNull(entry.getKey()));
 				} else {
					criteria.add(Restrictions.or(predicates.toArray(new Criterion[0])));
 				}
 
 			}
 		}
 
 		if (first != null) {
 			criteria.setFirstResult(first);
 		}
 
 		if (pageSize != null) {
 			criteria.setMaxResults(pageSize);
 		}
 
 		if (sortField == null) {
 			criteria.addOrder(Order.asc("nome"));
 		} else {
 			criteria.addOrder(sortOrder.equals(SortOrder.ASCENDING) ? Order.asc(sortField) : Order.desc(sortField));
 		}
 
 		return criteria.list();
 	}
 }
