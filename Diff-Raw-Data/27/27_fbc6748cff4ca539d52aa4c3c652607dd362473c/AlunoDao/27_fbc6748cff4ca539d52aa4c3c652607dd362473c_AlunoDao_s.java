 package dao;
 
 import modelo.entidade.Aluno;
 
 import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
 
 import br.com.caelum.vraptor.ioc.Component;
 
 @Component
 public class AlunoDao extends Dao<Aluno> {
 	
 	public AlunoDao(Session session) {
 		super(session);
 	}
 	
 	/*public AlunoDao buscarPorNome(String nome) {
 		addCriterion(getParametroBusca(nome, "nome_aluno"));
 		return this;
 	}*/
 }
