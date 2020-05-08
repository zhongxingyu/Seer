 package br.com.puc.sispol.controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 @Controller
 public class ManterUsuarioController {
	
 
 	public ManterUsuarioController() {
	
 	}
 
 	@RequestMapping("novoUsuario")
 	public String form() {
 		return "manter_usuario/incluir_usuario";
 	}
 }
