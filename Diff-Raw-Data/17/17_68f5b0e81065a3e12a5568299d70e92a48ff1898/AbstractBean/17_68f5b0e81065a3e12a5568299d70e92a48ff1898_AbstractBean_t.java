 package br.ufrn.cerescaico.bsi.sigest.web;
 
import java.util.ResourceBundle;

 import javax.faces.context.FacesContext;
 
 public abstract class AbstractBean {
 	
 	protected FacesContext context;
 	
 	public AbstractBean() {
 		// TODO Auto-generated constructor stub
 		context = FacesContext.getCurrentInstance();
 	}
 	
 	protected FacesContext getContext() {
 		return context;
 	}
	
	protected String msg(String key) {
		ResourceBundle bundle = context.getApplication().getResourceBundle(FacesContext.getCurrentInstance(), "msg");  
        String message = bundle.getString(key);
		return message;
	}

 
 }
