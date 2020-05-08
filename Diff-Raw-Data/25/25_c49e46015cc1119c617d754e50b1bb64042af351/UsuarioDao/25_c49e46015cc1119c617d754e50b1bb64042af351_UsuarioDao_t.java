 package br.gov.al.itec.sapim.dao;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.Query;
 import org.hibernate.SQLQuery;
 import org.hibernate.Session;
 import org.hibernate.criterion.Restrictions;
 
 import br.com.caelum.vraptor.ioc.Component;
 import br.gov.al.itec.sapim.model.Unidade;
 import br.gov.al.itec.sapim.model.Usuario;
 
 @Component
 public class UsuarioDao extends Dao<Usuario> {
 
 	UsuarioDao(Session session) {
 		super(session, Usuario.class);
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Usuario> login(Usuario vo) {
 		if (!"".equals(vo.getCpf())) {
 			Query query = session
 					.createQuery(" select u from Usuario u where cpf=:cpf and senha=:senha ");
 			query.setParameter("cpf", vo.getCpf());
 			query.setParameter("senha", vo.getSenha());
 			return query.list();
 		}
 		return new ArrayList<Usuario>();
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Usuario> buscar(String criteria){
 	   	Criteria c = session.createCriteria(Usuario.class);    
 	   	c.add(Restrictions.ilike("nome", "%" + criteria + "%"));
 	   	
 	   	return c.list();
 	}
 	
 	@SuppressWarnings("unchecked")
	public List<Usuario> buscarUsuariosDesativados(Boolean ativado){
 		Criteria c = session.createCriteria(Usuario.class);    
 		c.add(Restrictions.eq("excluido", new Boolean(ativado  == null || ativado == true ? true : false)));
 		
 		return c.list();
 	}
	@SuppressWarnings("unchecked")
	public List<Usuario> buscarUsuariosDesativados(Usuario usuario){
		Criteria c = session.createCriteria(Usuario.class);    
		c.add(Restrictions.eq("excluido", new Boolean(usuario.getExcluido()  == null || usuario.getExcluido() == true ? true : false)));
		if(!(usuario.getNome() == null) || !(usuario.getNome().length() == 0)){
			c.add(Restrictions.ilike("nome","%" +usuario.getNome()+ "%"));
		}
		return c.list();
	}
 	
 	/** @TODO deixar a busca mais completa */
 	@SuppressWarnings("unchecked")
 	public List<Usuario> buscar(Usuario user, Unidade unidade, boolean desativado) {
         String sql = "select distinct(u.*)" +
         		" from Usuario u" +
         		" inner join usuario_departamento ud ON (u.id = ud.usuario_id)" +
         		" inner join departamento d ON (ud.departamento_id = d.id)" +
         		" where";
         
         if (desativado) sql+= " deleted = 'true'"; else sql += " deleted = 'false'"; 
         if (unidade != null) sql += " and d.unidade_id="+unidade.getId();
         if(user != null){
         	if (user.getNome()!= null && !user.getNome().equals(""))sql+=" and sem_acentos(u.nome) ilike sem_acentos('%"+user.getNome()+"%')";
         	if (user.getCpf()!=null && !user.getCpf().equals(""))sql+=" and u.cpf ilike '"+user.getCpf()+"'";
         	if (user.getEmail()!=null && !user.getEmail().equals(""))sql+=" and u.email ilike '"+user.getEmail()+"'";
         	if (user.getFuncao()!=null && !user.getFuncao().equals(""))sql+=" and sem_acentos(funcao) ilike sem_acentos('%"+user.getFuncao()+"%')";
         }
 
         SQLQuery squery = session.createSQLQuery(sql);
         squery.addEntity(Usuario.class);
 		return squery.list();
 	}
 }
