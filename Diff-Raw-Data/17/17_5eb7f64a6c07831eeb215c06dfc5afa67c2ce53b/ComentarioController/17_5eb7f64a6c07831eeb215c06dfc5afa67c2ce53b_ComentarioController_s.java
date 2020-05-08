 package br.com.fractal.controller;
 
 import javax.persistence.EntityManager;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.validation.Valid;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import br.com.fractal.dao.ComentariosDAO;
 import br.com.fractal.model.Comentario;
 import br.com.fractal.model.Tarefas;
 
 @Controller
 public class ComentarioController {
 	
 //	private EntityManager em;
 
 	@RequestMapping("adicionaComentario")
	public String adiciona(@Valid Comentario comentario, HttpServletRequest request){
 
 		EntityManager em = (EntityManager) request.getAttribute("em");
		new ComentariosDAO(em).adiciona(comentario);
 		return "redirect:Menu";
 	}
 	
 //	@RequestMapping("mostraComentariosPorTarefa")
 //	public String mostra(Long id, Model model, HttpServletResponse response, HttpServletRequest request, Tarefas tarefa){
 //		
 //		EntityManager em = (EntityManager) request.getAttribute("em");
 //		ComentariosDAO dao = new ComentariosDAO(em);
 //		model.addAttribute("comentario", dao.buscaComentarioPorTarefa(id, tarefa));
 //		
 //		response.setStatus(200);
 //		return "comentarios-do-dialog";
 //	}
 	
 	@RequestMapping("listaComentarios")
 	public String lista(Model model, HttpServletRequest request){
 
 		EntityManager em = (EntityManager) request.getAttribute("em");
 
 		ComentariosDAO dao = new ComentariosDAO(em);
 		
 		model.addAttribute("comentario", dao.lista());
 		
 		return "comentarios-do-dialog";
 	}
 
 	
 }
