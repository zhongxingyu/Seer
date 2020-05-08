 package org.meth4j.futil;
 
 import static javax.faces.application.FacesMessage.SEVERITY_ERROR;
 import static javax.faces.application.FacesMessage.SEVERITY_INFO;
 import static javax.faces.application.FacesMessage.SEVERITY_WARN;
 
 import java.io.Serializable;
 
 import javax.faces.application.FacesMessage;
 
 public class Message implements Serializable {
 
 	private Message() {
 	}
 
 	private static final long serialVersionUID = 2L;
 
 	public static final FacesMessage infoMesg(IKey message) {
 		return new FacesMessage(SEVERITY_INFO, message.getLabel(), message.getLabel());
 	}
 
 	public static final FacesMessage warnMesg(IKey message) {
 		return new FacesMessage(SEVERITY_WARN, message.getLabel(), message.getLabel());
 	}
 
	public static final FacesMessage errMesg(IKey message) {
 		return new FacesMessage(SEVERITY_ERROR, message.getLabel(), message.getLabel());
 	}
 
 }
