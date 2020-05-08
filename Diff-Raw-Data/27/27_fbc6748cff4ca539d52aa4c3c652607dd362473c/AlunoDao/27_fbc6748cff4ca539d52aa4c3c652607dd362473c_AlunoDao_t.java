 package dao;
 
import java.util.List;

 import modelo.entidade.Aluno;
import modelo.entidade.Responsavel;
 
 import org.hibernate.Session;
 
 import br.com.caelum.vraptor.ioc.Component;
 
@SuppressWarnings("unchecked")
 @Component
 public class AlunoDao extends Dao<Aluno> {
 	
 	public AlunoDao(Session session) {
 		super(session);
 	}
 	
 	/*public AlunoDao buscarPorNome(String nome) {
 		addCriterion(getParametroBusca(nome, "nome_aluno"));
 		return this;
 	}*/
	
	
	public List<Aluno> listaPeloResponsavel(Responsavel responsavel){
		StringBuffer qry = new StringBuffer();
		qry.append("from Aluno a join fetch a.listaResponsavel r ");
		qry.append("where r.id = ?");
		
		return getSession().createQuery(qry.toString())
				.setLong(0, responsavel.getId())
				.list();
	}
 }
