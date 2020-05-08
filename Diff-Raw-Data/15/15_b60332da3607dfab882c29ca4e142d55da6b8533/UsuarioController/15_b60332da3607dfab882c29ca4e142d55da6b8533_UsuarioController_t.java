 package controller;
 
 import java.util.List;
 
 import modelo.entidade.Usuario;
import modelo.entidade.UsuarioLogado;
import annotation.Permitido;
 import br.com.caelum.vraptor.Get;
 import br.com.caelum.vraptor.Path;
 import br.com.caelum.vraptor.Post;
 import br.com.caelum.vraptor.Resource;
 import br.com.caelum.vraptor.Result;
 import dao.UsuarioDao;
 
 @Resource
 public class UsuarioController {
 	
 	private final UsuarioDao dao;
 	private final Result result;
	private final UsuarioLogado usuarioLogado;
 
	public UsuarioController(UsuarioDao dao, Result result, UsuarioLogado usuarioLogado){
 		this.dao = dao;
 		this.result = result;
		this.usuarioLogado = usuarioLogado;
 	}
 	@Get @Path("/usuario/adicionar")
 	public void adicionar() {
 	}
 	@Post @Path("/usuario/adicionar")
 	public void adicionar(Usuario usuario) {
 		usuario.setSenha(usuario.getEncryptedSenha());
 		dao.salva(usuario);
 		result.redirectTo(UsuarioController.class).listar();
 	}
 	
 	
 	
 	
 	@Get @Path("/usuario/listar")
 	public List<Usuario> listar(){
 		return dao.listaTudo();
 	}
 	
 	
 	@Get @Path("/usuario/login/erro")
 	public void erroLogin() {
 		
 	}
 	
 	
 	@Get @Path("/login")
	@Permitido
 	public void login() {
 	}
 	
 	@Post @Path("/login")
	@Permitido
 	public void login(Usuario usuario) {
 		if( dao.existeLoginSenha(usuario) ) {
			usuarioLogado.login(usuario);
 			result.redirectTo(MenuController.class).inicio();
 		} else {
 			result.include("falha",true);
 			result.redirectTo(getClass()).login();
 		}
 	}
 	
 }
