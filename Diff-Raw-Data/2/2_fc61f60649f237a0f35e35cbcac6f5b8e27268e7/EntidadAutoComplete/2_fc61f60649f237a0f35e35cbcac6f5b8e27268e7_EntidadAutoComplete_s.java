 /**
  * 
  */
 package com.isesalud.controller.autocomplete;
 
 import java.util.List;
 
 import javax.ejb.EJB;
import javax.faces.bean.RequestScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import com.isesalud.controller.security.SecurityComponent;
 import com.isesalud.ejb.query.EntidadEjb;
 import com.isesalud.model.Entidad;
 import com.isesalud.support.components.BaseAutoCompleteComponent;
 
 /**
  * @author ari
  *
  */
 @Named
 @RequestScoped
 public class EntidadAutoComplete extends BaseAutoCompleteComponent<Entidad, EntidadEjb>{
 	
 /**
 	 * 
 	 */
 	private static final long serialVersionUID = -6330098773718933290L;
 
 @EJB
 private EntidadEjb manager;
 
 @Inject
 private SecurityComponent securityComponent;
 
 @Override
 protected EntidadEjb getACManager() {
 	return manager;
 }
 
 @Override
 protected List<Entidad> getList(String description) {
 	Entidad params = new Entidad();
 	params.setName(description);
 	return manager.getEntidadForAutoComplete(params);
 }
 
 
 }
