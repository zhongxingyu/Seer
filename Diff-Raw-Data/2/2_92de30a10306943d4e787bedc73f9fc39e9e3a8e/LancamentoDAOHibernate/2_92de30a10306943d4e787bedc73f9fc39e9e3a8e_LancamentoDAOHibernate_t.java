 package financeiro.lancamento;
 
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.SQLQuery;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 import financeiro.conta.Conta;
 import financeiro.util.GenericDAOHibernate;
 
 public class LancamentoDAOHibernate extends GenericDAOHibernate<Lancamento> implements LancamentoDAO {
 
 	public LancamentoDAOHibernate() {
 		super(Lancamento.class);
 	}
 	
 	@Override
 	public List<Lancamento> listar(Conta conta, Date dataInicio, Date dataFim) {
 		Criteria criteria = session.createCriteria(type);
 		
 		if (dataInicio != null && dataFim != null) {
 			criteria.add(Restrictions.between("data", dataInicio, dataFim));
 		} else if (dataInicio != null) {
 			criteria.add(Restrictions.ge("data", dataInicio));
 		} else if (dataFim != null) {
 			criteria.add(Restrictions.le("data", dataFim));
 		}
 		
 		criteria.add(Restrictions.eq("conta", conta));
 		criteria.addOrder(Order.asc("data"));
 		
 		@SuppressWarnings("unchecked")
 		List<Lancamento> lancamentos = criteria.list();
 		
 		return lancamentos;
 	}
 
 	@Override
 	public float saldo(Conta conta, Date data) {
 		StringBuilder sql = new StringBuilder();
 		sql.append("select sum(l.valor * c.fator)");
 		sql.append("  from LANCAMENTO l,");
		sql.append("       CATEGORIA c");
 		sql.append(" where l.categoria = c.codigo");
 		sql.append("   and l.conta = :conta");
 		sql.append("   and l.data <= :data");
 		
 		SQLQuery query = session.createSQLQuery(sql.toString());
 		query.setParameter("conta", conta.getConta());
 		query.setParameter("data", data);
 		BigDecimal saldo = (BigDecimal) query.uniqueResult();
 		if (saldo != null) {
 			return saldo.floatValue();
 		}
 		return 0f;
 	}
 
 }
