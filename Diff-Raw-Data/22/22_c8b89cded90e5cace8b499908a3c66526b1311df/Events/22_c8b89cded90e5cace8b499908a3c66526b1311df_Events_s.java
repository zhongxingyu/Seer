 /**
  * @author wolfgang Teves
  * @version 0.6
  */
 package ac.at.fhkufstein.EventFunc;
 
 import ac.at.fhkufstein.bean.BmwEventController;
 import ac.at.fhkufstein.entity.BmwEvent;
 import ac.at.fhkufstein.session.BmwEventFacade;
 import ac.at.fhkufstein.session.BmwParticipantsFacade;
 import java.io.Serializable;
 import java.util.Date;
 import java.util.List;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 import javax.persistence.EntityManager;
 
 
 @ManagedBean(name = "Events")
 @ViewScoped
 
 public class Events implements Serializable{
 /*
  * Returns Events (Provides Diffrent Datasets like current events and past Events)
     */
 
 	//private BmwUserController bmwUserController; 
 	private BmwEventController bmwEventController;// Controllers
 	
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
 	public Events() {
 
 
 		try {
 			//Instanciate Controllers
 			bmwEventController = FacesContext.getCurrentInstance().getApplication().evaluateExpressionGet(FacesContext.getCurrentInstance(), "#{bmwEventController}", BmwEventController.class);
 			//Get eventID from URL
 		
 		
 		} catch (Exception ex) {
 			System.out.println(ex.getMessage());
 		}
 	}
 	
 	public List upcoming(){
 		Date d = new Date(System.currentTimeMillis());
 	EntityManager em = ((BmwEventFacade) bmwEventController.getFacade()).getEntityManager();
 			List p = em.createNamedQuery("BmwEvent.upcoming")
 					.setParameter("now", d)
 					.getResultList();
			System.out.println("upcoming"+p.size());
 			return p;
 	
 }
 
 	public List past(){
 		Date d = new Date(System.currentTimeMillis());
 	EntityManager em = ((BmwEventFacade) bmwEventController.getFacade()).getEntityManager();
 			List p = em.createNamedQuery("BmwEvent.past")
 					.setParameter("now", d)
 					.getResultList();
			System.out.println("past"+p.size());
 			return p;
 	
 }
 	
 }
