 /**
  * @author wolfgang Teves
  * @version 0.6
  */
 package ac.at.fhkufstein.EventFunc;
 
 import ac.at.fhkufstein.bean.BmwEventController;
 import ac.at.fhkufstein.entity.BmwEvent;
 import java.io.Serializable;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 
 
 @ManagedBean(name = "Event")
 @ViewScoped
 
 public class Event implements Serializable{
 /*
     * This Class can Provide the Current Event pased on the parameter "eventID" in the URI-> It provides the save funktion as well
   */
 
 	//private BmwUserController bmwUserController; 
 	private BmwEventController bmwEventController;// Controllers
 	private Integer eventID; //Current Event ID
 	private BmwEvent current;//Current Event
 	
 
 
 //	public EntityManager em;
 	/**
 	 * Creates
 	 * a
 	 * new
 	 * instance
 	 * of
 	 * bmwEventFunc
 	 */
 	public Event() {
 
 
 		try {
 			//Instanciate Controllers
 			bmwEventController = FacesContext.getCurrentInstance().getApplication().evaluateExpressionGet(FacesContext.getCurrentInstance(), "#{bmwEventController}", BmwEventController.class);
 			//Get eventID from URL
 			FacesContext facesContext = FacesContext.getCurrentInstance();
 			this.eventID = (Integer) Integer.parseInt(facesContext.getExternalContext().getRequestParameterMap().get("eventID"));
 
 			//Get Current Event for eventID
 			current = bmwEventController.getFacade().find(eventID);
 		
 		} catch (Exception ex) {
 			System.out.println(ex.getMessage());
 		}
 	}
 
 	public Integer getEventID() {
 		return eventID;
 	}
 
 	
 	public void setEventID(Integer eventID) {
 		this.eventID = eventID;
 	}
 	public BmwEvent getCurrent() {
 		return current;
 	}
 
 	public void setCurrent(BmwEvent current) {
 		this.current = current;
 	
 	}
 
 	public void saveCurrent() {
 		//Saves the Event Itself //For Edit form

 		bmwEventController.setSelected(current);
 		bmwEventController.save(null);
 
 	}
 
 	
 }
