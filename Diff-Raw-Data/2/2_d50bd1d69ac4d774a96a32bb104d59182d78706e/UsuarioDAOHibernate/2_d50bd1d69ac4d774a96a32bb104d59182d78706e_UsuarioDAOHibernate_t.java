 package financeiro.usuario;
 
 import org.hibernate.Query;
 
 import financeiro.util.GenericDAOHibernate;
 
 public class UsuarioDAOHibernate extends GenericDAOHibernate<Usuario> implements
 		UsuarioDAO {
 
 	public UsuarioDAOHibernate() {
 		super(Usuario.class);
 	}
 
 	@Override
 	public Usuario buscarPorLogin(String login) {
 		String hql = "select u from Usuario u where u.login = :login";
 		Query consulta = session.createQuery(hql);
		consulta.setString("login", login);
 		return (Usuario) consulta.uniqueResult();
 	}
 	
 	@Override
 	public void atualizar(Usuario usuario) {
 		if (usuario.getPermissao() == null || usuario.getPermissao().size() == 0) {
 			Usuario usuarioPermissao = this.carregar(usuario.getCodigo());
 			usuario.setPermissao(usuarioPermissao.getPermissao());
 			session.evict(usuarioPermissao);
 		}
 		super.atualizar(usuario);
 	}
 
 }
