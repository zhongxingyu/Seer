 package org.contato.controller;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 
 import org.contato.entity.Usuario;
 import org.contato.service.UsuarioService;
 import org.sharkness.jsf.support.ModelController;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
 import org.springframework.stereotype.Component;
 
 @Component
 @ManagedBean
 @RequestScoped
 @SuppressWarnings("serial")
public class UsuarioController extends ModelController<Long, Usuario, UsuarioService> {
 
 	@Autowired
 	private MessageDigestPasswordEncoder passwordEncoder;
 	
 	@Override
 	protected Usuario modelProcessBeforeSave(Usuario model) {
 		try {
 			Usuario usuario = model;
 			if (usuario.getPassword() != null && usuario.getPassword().trim().length() > 0) {
 				usuario.setPassword(passwordEncoder.encodePassword(usuario.getPassword(), null));
 			} else {
 				usuario.setPassword(getModelService().getById(usuario.getId()).getPassword());
 			}
 			return usuario;
 		} catch (Exception e) {
 			return model;
 		}
 	}
 
 }
