 package br.com.classeencanto.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.ModelAndView;
 
 import br.com.classeencanto.dao.UsuarioDAO;
 import br.com.classeencanto.model.impl.Administrador;
 import br.com.classeencanto.model.impl.Usuario;
 
 @Controller
 public class UsuarioController extends AbstractLoginController {
 
 	@Autowired
 	private AdminController adminController;
 
 	@Autowired
 	private UsuarioDAO usuarioDAO;
 
 	private Usuario usuarioQueSeraAlterado;
 
 	@RequestMapping("login")
 	public ModelAndView login(Usuario usuario) {
 
 		return super.login(usuario, usuarioDAO);
 	}
 
 	@Override
 	protected String getPaginaDeLogin() {
 		return "login";
 	}
 
 	@Override
 	protected String getPaginaDeRetorno() {
 		return "redirect:listaDeDesejos";
 	}
 
 	@RequestMapping("logout")
 	public ModelAndView logout() {
 
 		adminController.logout();
 
 		return super.logout();
 	}
 
 	@RequestMapping("cadastroDeUsuario")
 	public ModelAndView cadastroDeUsuario() {
 
		ModelAndView mav = new ModelAndView("cadastroDeUsuario");
 
 		mav.addObject("usuario", usuarioQueSeraAlterado);
 
 		List<String> feedbacks = new ArrayList<>();
 
 		feedbacks.addAll(this.feedbacks);
 
 		mav.addObject("feedbacks", feedbacks);
 		
 		this.feedbacks.clear();
 
 		finaliza(mav);
 
 		return mav;
 	}
 
 	@RequestMapping("salvarUsuario")
 	public ModelAndView salvarUsuario(Usuario usuario) {
 
 		ModelAndView mav = new ModelAndView("redirect:listaDeUsuarios");
 
 		boolean existeLoginOuSenha = existeLoginOuSenha(usuario, mav);
 
 		if (!existeLoginOuSenha) {
 
 			salvar(usuario);
 
 			if (!adminController.isLogado()) {
 
 				return logar(usuario);
 
 			} else {
 
 				usuario = null;
 
 				finaliza(mav);
 			}
 		}
 
 		return mav;
 	}
 
 	private void salvar(Usuario usuario) {
 
 		if (usuario.getId() == 0) {
 
 			usuarioDAO.save(usuario);
 
 		} else {
 
 			usuarioDAO.merge(usuario);
 		}
 	}
 
 	private boolean existeLoginOuSenha(Usuario usuario, ModelAndView mav) {
 
 		boolean existeLoginOuSenha = false;
 
 		List<Usuario> usuariosExistentes = usuarioDAO.findAll();
 
 		if (usuariosExistentes != null && !usuariosExistentes.isEmpty()) {
 
 			for (Usuario usuarioExistente : usuariosExistentes) {
 
 				if (usuarioExistente.getLogin().equals(usuario.getLogin())
 						|| usuarioExistente.getEmail().equals(
 								usuario.getEmail())) {
 
 					if (usuarioExistente.getId() != usuario.getId()) {
 
 						feedbacks.add("Login ou email j cadastrados.");
 
 						mav.setViewName("redirect:cadastroDeUsuario");
 
 						existeLoginOuSenha = true;
 
 						break;
 					}
 				}
 			}
 		}
 
 		return existeLoginOuSenha;
 	}
 
 	private ModelAndView logar(Usuario usuario) {
 
 		if (usuario.isAdmin()) {
 
 			return adminController.login((Administrador) usuario);
 
 		} else {
 
 			return super.login(usuario, usuarioDAO);
 		}
 	}
 
 	@RequestMapping("excluirUsuario")
 	public ModelAndView excluirUsuario(Usuario usuario) {
 
 		ModelAndView mav = new ModelAndView("loginAdmin");
 
 		if (adminController.isLogado()) {
 
 			usuarioDAO.delete(usuario);
 
 			mav.setViewName("redirect:listaDeUsuarios");
 
 		}
 
 		finaliza(mav);
 
 		return mav;
 	}
 
 	@RequestMapping("formAlterarDadosUsuario")
 	public ModelAndView formAlterarDadosUsuario(Long idUsuario) {
 
 		ModelAndView mav = new ModelAndView("loginAdmin");
 
 		if (adminController.isLogado()) {
 
 			usuarioQueSeraAlterado = usuarioDAO.findById(idUsuario);
 
 			mav.setViewName("redirect:cadastroDeUsuario");
 		}
 
 		finaliza(mav);
 
 		return mav;
 	}
 
 	@RequestMapping("listaDeUsuarios")
 	public ModelAndView listaDeUsuarios() {
 
 		ModelAndView mav = new ModelAndView();
 
 		if (adminController.isLogado()) {
 
 			super.usuario = adminController.usuario;
 
 			mav.setViewName("listaDeUsuarios");
 
 			List<Usuario> listaDeUsuarios = usuarioDAO.findAll();
 
 			mav.addObject("listaDeUsuarios", listaDeUsuarios);
 
 		} else {
 
 			mav.setViewName("loginAdmin");
 		}
 
 		finaliza(mav);
 
 		return mav;
 	}
 }
