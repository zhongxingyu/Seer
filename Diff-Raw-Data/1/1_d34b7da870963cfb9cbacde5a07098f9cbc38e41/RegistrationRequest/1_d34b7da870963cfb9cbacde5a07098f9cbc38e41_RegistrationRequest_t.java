 package formel0api.beans;
 
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.FacesContext;
 
 /**
  * An instance of this class exists as long as the server is running.
  * It holds all registered players. 
  * If a new Player should be added, call addPlayer(Player)
  *
  */
 @ManagedBean(name="registration")
 @RequestScoped
 public class RegistrationRequest {
 	
     @ManagedProperty(value="#{registrar}")  
     private Registrar registrar;
 	
     private Player tempPlayer;
 	
 	public RegistrationRequest() {
 		
 		setTempPlayer(new Player());
 	}
 	
 	
 	public String register(){
 		if(registrar.getPlayer(tempPlayer.getName())==null){
 
 			registrar.addPlayer(tempPlayer);
			FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
 			return "/index.xhtml";
 		}else{
 			FacesContext.getCurrentInstance().addMessage("form:submit",new FacesMessage(FacesMessage.SEVERITY_ERROR, "User already exists", null));
 			return "/register.xhtml";
 		}
 		
 	}
 
 	public Player getTempPlayer() {
 		return tempPlayer;
 	}
 
 	public void setTempPlayer(Player tempPlayer) {
 		this.tempPlayer = tempPlayer;
 	}
 	
     public void setRegistrar(Registrar registrar) {
     	this.registrar = registrar;
     }
 }
