 package br.com.fractal.controller;
 
 import javax.persistence.EntityManager;
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import br.com.fractal.dao.UsuariosDAO;
 import br.com.fractal.model.Usuarios;
 
 @Controller
 public class UsuariosController {
 	
 //	private EntityManager em;
 
 	@RequestMapping("telaUsuarios")
 	public String lista(Model model, HttpServletRequest request){
 		EntityManager em = (EntityManager) request.getAttribute("em");
 		UsuariosDAO dao = new UsuariosDAO(em);
 		model.addAttribute("usuarios", dao.lista());
 		return "usuarios/usuarios";
 	}
 	
 	@RequestMapping("adicionaUsuario")
	public String adiciona(Usuarios usuario, HttpServletRequest request, Model model){
 		EntityManager em = (EntityManager) request.getAttribute("em");
		
		
		try {
			new UsuariosDAO(em).adiciona(usuario);
			model.addAttribute("userAddSuccess", "User successfully registered");
		} catch (Exception e) {
			model.addAttribute("userAddError", "The user cannot be created");
		}
		return "formularioDeLogin";
 	}
 	
 	@RequestMapping("removeUsuario")
 	public String remove(Usuarios usuarios, HttpServletRequest request){
 		EntityManager em = (EntityManager) request.getAttribute("em");
 		UsuariosDAO dao = new UsuariosDAO(em);
 		dao.remove(usuarios);
 		return "redirect:telaUsuarios";
 	}
 
 	@RequestMapping("associaEmProjeto")
 	public void associaEmProjeto(Long usuarioId, Long projetoId, HttpServletRequest request) {
 		EntityManager em = (EntityManager) request.getAttribute("em");
 		UsuariosDAO dao = new UsuariosDAO(em);
 		dao.associaUsuarioEmProjeto(usuarioId, projetoId);
 	}
 	
 }
