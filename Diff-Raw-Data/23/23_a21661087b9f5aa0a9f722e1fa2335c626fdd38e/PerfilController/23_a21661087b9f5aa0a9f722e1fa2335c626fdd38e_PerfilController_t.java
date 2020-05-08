 package org.contato.controller;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 
 import org.contato.entity.Perfil;
 import org.contato.service.PerfilService;
 import org.sharkness.jsf.support.ModelController;
 import org.springframework.stereotype.Component;
 
 @Component
 @ManagedBean
 @RequestScoped
 @SuppressWarnings("serial")
public class PerfilController extends ModelController<Long, Perfil, PerfilService> {
 
 }
