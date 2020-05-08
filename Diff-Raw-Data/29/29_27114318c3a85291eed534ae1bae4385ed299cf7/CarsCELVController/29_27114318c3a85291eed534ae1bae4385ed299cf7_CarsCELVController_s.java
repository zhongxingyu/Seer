 package backingbeans;
 
 import java.io.Serializable;
 import java.util.ResourceBundle;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.bean.RequestScoped;
 import javax.faces.bean.ViewScoped;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import javax.faces.convert.FacesConverter;
 import javax.faces.model.DataModel;
 import javax.faces.model.ListDataModel;
 import javax.faces.model.SelectItem;
 import javax.faces.bean.ManagedProperty;
 import java.util.logging.Logger;
 import java.util.List;
 import java.util.Arrays;
 import java.util.ArrayList;
 import javax.faces.event.ActionEvent;
 import javax.annotation.PostConstruct;
 import javax.naming.InitialContext;
 import java.util.Map;
 import java.util.HashMap;
 import javax.faces.event.ComponentSystemEvent;
 import java.util.Collection;
 import java.util.Date;
 import javax.servlet.http.HttpSession;
 
 import facades.ICarFacadeLocal;
 
 @ManagedBean
 @ViewScoped
 public class CarsCELVController implements Serializable {
 
     private static final String CLASSNAME=CarsCELVController.class.getName();
     private static final Logger l = Logger.getLogger(CLASSNAME);
 
     @EJB(beanName = "CarFacade")
    private ICarFacadeLocal
    
 
 }
 
