 package br.ufrn.cerescaico.bsi.sigest.web;
 
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
 
 }
