 package org.cmpa.bean;
 
 import java.io.Serializable;
 import java.util.Locale;
 
 import javax.annotation.PostConstruct;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 
/**
 * Site locale backing bean.
 */
 @ManagedBean(name="localeBean")
 @SessionScoped
 public class LocaleBean implements Serializable {
 	private static final long serialVersionUID = 2113953011918789161L;
 	
 	private static final String ENGLISH = "en";
 	private static final String FRENCH = "fr";
 
 	private Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
 
 	@ManagedProperty(value=ENGLISH)
 	private String language;
 
 	@PostConstruct
 	public void initialize(){
 		locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
 		setLanguage(locale.getLanguage());
 	}
 	
 	public void setLanguage(String language){
 		this.language = language;
 	}
 	
 	public String getLanguage(){
 		return this.language;
 	}
 
 	public void setLocale(Locale locale){
 		this.locale = locale;
 		FacesContext.getCurrentInstance().getViewRoot().setLocale(this.locale);
 	}
 
 	public Locale getLocale(){
 		return this.locale;
 	}
 	
 	public void swapLocale(){
 		updateLanguageAndLocale((getLanguage() == ENGLISH) ? FRENCH : ENGLISH);
 	}
 	
 	private void updateLanguageAndLocale(String lang){
 		setLanguage(lang);
 		setLocale(new Locale(lang));
 	}
 }
