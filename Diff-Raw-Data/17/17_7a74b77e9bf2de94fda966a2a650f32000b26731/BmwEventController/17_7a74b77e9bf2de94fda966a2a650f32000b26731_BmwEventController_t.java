 package ac.at.fhkufstein.bean;
 
 import ac.at.fhkufstein.entity.BmwEvent;
 import ac.at.fhkufstein.session.BmwEventFacade;
 import java.io.Serializable;
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 
 @ManagedBean(name = "bmwEventController")
 @ViewScoped
 public class BmwEventController extends AbstractController<BmwEvent> implements Serializable {
 
     @EJB
     private BmwEventFacade ejbFacade;
 
     public BmwEventController() {
         super(BmwEvent.class);
     }
    
     @PostConstruct
     public void init() {
         super.setFacade(ejbFacade);
     }
 }
