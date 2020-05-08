 package org.contato.controller;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 
 import org.contato.entity.Contato;
 import org.contato.service.ContatoService;
 import org.sharkness.jsf.support.ModelController;
 import org.springframework.stereotype.Component;
 
 @Component
 @ManagedBean
 @RequestScoped
 @SuppressWarnings("serial")
public class ContatoController extends ModelController<Long, Contato, ContatoService> {
 
 }
