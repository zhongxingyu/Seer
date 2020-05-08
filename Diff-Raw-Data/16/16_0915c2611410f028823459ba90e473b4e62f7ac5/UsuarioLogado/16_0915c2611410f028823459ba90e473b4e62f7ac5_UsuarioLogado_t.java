 package com.prisila.modelo.entidade;
 
 import java.io.Serializable;
 
 import br.com.caelum.vraptor.ioc.Component;
 import br.com.caelum.vraptor.ioc.SessionScoped;
 
 @Component
 @SessionScoped
 public class UsuarioLogado implements Serializable {
 	
 	private static final long serialVersionUID = 261932533970111881L;
 	private Usuario usuario;
 	
 	public String getLogin() {
 		return usuario.getLogin();
 	}
 	
 	public String getNome() {
 		return usuario.getNome();
 	}
 	
 	public void login(Usuario usuario) {
 		if (!isLogado()) {
 			this.usuario = usuario;
 		} else {
 			logout();
 			login(usuario);
 		}
 	}
 	
 	public boolean isLogado() {
 		return usuario != null;
 	}
 	
 	public void logout() {
 		this.usuario = null;
 	}
	
 	public Usuario getUsuario() {
 		return usuario;
 	}
 }
