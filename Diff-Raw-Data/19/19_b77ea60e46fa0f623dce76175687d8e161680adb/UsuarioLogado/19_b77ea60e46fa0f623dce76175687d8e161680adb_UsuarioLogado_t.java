 package com.prisila.modelo.entidade;
 
import java.io.Serializable;

 import br.com.caelum.vraptor.ioc.Component;
 import br.com.caelum.vraptor.ioc.SessionScoped;
 
 @Component
 @SessionScoped
public class UsuarioLogado implements Serializable {

	private static final long serialVersionUID = 1L;
 	private Usuario logado;
 	
 	public void login(Usuario usuario) {
 		this.logado = usuario;
 	}
 	
 	public boolean isLogado() {
 		return logado != null;
 	}
 	
 	public String getNome() {
 		return logado.getNome();
 	}
 	
 	public void logout() {
 		this.logado = null;
 	}
 }
