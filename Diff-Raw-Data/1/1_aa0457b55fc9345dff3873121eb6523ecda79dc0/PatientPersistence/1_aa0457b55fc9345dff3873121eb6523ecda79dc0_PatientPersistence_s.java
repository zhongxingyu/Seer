 /**
  * 
  */
 package com.isesalud.controller.persistence;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.enterprise.context.Conversation;
 import javax.enterprise.context.ConversationScoped;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.event.ActionEvent;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import com.isesalud.ejb.persistence.PatientPersistenceEjb;
 import com.isesalud.model.Paciente;
 
 import java.io.Serializable;
 
 /**
  * @author Haysoos
  *
  */
 @Named
 @ViewScoped
 public class PatientPersistence implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 8376685298021320691L;
 	
 	private Paciente paciente;
 	
 	@PostConstruct
 	public void init(){
 		paciente = new Paciente();
 	}
 	
 	/*@Inject
 	private Conversation conversation;*/
 	
 	@EJB
 	private PatientPersistenceEjb manager;
 	
 	/*public void addNewPaciente(ActionEvent e){
 		paciente = new Paciente();
 		if(conversation.isTransient())
 			conversation.begin();
 	}*/
 	
 	public void cancelPaciente(ActionEvent e){
 		/*if(!conversation.isTransient())
 			conversation.end();*/
 	}
 	
 	public void savePaciente(ActionEvent e){
 		if(paciente != null){
 			manager.save(paciente);
 		}
 		/*if(!conversation.isTransient())
 			conversation.end();*/
 	}
 	
 	public String navigate(){
 		return "home";
 	}
 	
 	public Paciente getPaciente() {
 		return paciente;
 	}
 	
 	public void setPaciente(Paciente paciente) {
 		this.paciente = paciente;
 	}
 
 }
