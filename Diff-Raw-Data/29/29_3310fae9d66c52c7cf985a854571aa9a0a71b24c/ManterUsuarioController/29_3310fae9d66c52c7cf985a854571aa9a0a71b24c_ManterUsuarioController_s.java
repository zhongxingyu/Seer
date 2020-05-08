 package br.com.puc.sispol.controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
import br.com.puc.sispol.dao.TarefaDAO;

 @Controller
 public class ManterUsuarioController {
	private TarefaDAO dao;
 
 	public ManterUsuarioController() {
		this.dao = new TarefaDAO();
 	}
 
 	@RequestMapping("novoUsuario")
 	public String form() {
 		return "manter_usuario/incluir_usuario";
 	}
 }
