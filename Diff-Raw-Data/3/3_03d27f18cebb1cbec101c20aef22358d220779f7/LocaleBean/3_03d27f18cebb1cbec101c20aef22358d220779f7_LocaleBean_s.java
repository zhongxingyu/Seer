 package org.inftel.ssa.web;
 
 import java.io.Serializable;
 import java.util.Locale;
import javax.enterprise.context.SessionScoped;
 import javax.faces.bean.ManagedBean;
 import javax.faces.context.FacesContext;
 
 @ManagedBean
 @SessionScoped
 public class LocaleBean implements Serializable {
 
 	private Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
 
 	public Locale getLocale() {
 		return locale;
 	}
 
 	public String getLanguage() {
 		return locale.getLanguage();
 	}
 
 	public void setLanguage(String language) {
 		locale = new Locale(language);
 		FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
 	}
 }
