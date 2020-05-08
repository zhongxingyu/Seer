 package org.contato.controller;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 
 import org.contato.entity.Tarefa;
 import org.contato.service.TarefaService;
 import org.sharkness.jsf.support.ModelController;
 import org.springframework.stereotype.Component;
 
 @Component
 @ManagedBean
 @RequestScoped
 @SuppressWarnings("serial")
public class TarefaController extends ModelController<Long, Tarefa, TarefaService> {
 
 }
