 package rafaelcds.service;
 
 import java.util.List;
 
 import javax.ejb.Stateless;
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import rafaelcds.model.Movimentacao;
 
 @Stateless
 public class MovimentacaoService {
 	
 	@PersistenceContext
 	private EntityManager entityManager;
 	
 	@TransactionAttribute(TransactionAttributeType.REQUIRED)
 	public void salvar(Movimentacao movimentacao) {
 		entityManager.merge(movimentacao);
 	}
 	
 	@TransactionAttribute(TransactionAttributeType.REQUIRED)
 	public void excluir(Long id) {
 		String sql = "DELETE FROM Movimentacao m " +
 					 "WHERE m.id = :id";
 		entityManager.createQuery(sql)
 						.setParameter("id", id)
 						.executeUpdate();
 	}
 	
 	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
 	public List<Movimentacao> obterTodos() {
		String sql = "FROM Movimentacao ORDER BY data, tipo, valor";
 		return entityManager.createQuery(sql, Movimentacao.class)
 								.getResultList();
 	}
 
 	public Movimentacao obterPorId(Long id) {
 		return entityManager.find(Movimentacao.class, id);
 	}	
 }
