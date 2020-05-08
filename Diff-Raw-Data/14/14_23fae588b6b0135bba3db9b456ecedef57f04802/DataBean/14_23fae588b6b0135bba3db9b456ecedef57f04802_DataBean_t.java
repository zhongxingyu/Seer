 package nl.amis.web.beans;
 
import java.io.Serializable;
 import java.util.List;
 import java.util.Set;
 
 import javax.ejb.EJB;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.validation.ConstraintViolation;
 import javax.validation.Validation;
 import javax.validation.Validator;
 import javax.validation.constraints.Null;
 import javax.validation.constraints.Size;
 
 import org.apache.myfaces.extensions.validator.baseval.annotation.Length;
 import org.apache.myfaces.extensions.validator.baseval.annotation.Pattern;
 
 import nl.amis.mode.hr.services.HrSessionLocal;
 import nl.amis.model.hr.entities.Department;
 

@ManagedBean( name = "dataBean")
 @ViewScoped
public class DataBean implements Serializable  {
 
 	private static final long serialVersionUID = -6996992412087723373L;
 
 	@Null
     @Size(min=2,max=10)
	private String departmentName = "fielda";
 
 	@Length(minimum=2 , maximum=50)
 	@Pattern(value="[A-Za-z ]*" , validationErrorMsgKey="locationValidation")
	private String departmentLocation = "fieldb";
 
 	
 
 	@EJB
     private HrSessionLocal sessionFacade;
 	
     public void setSessionFacade(HrSessionLocal sessionFacade) {
         this.sessionFacade = sessionFacade;
     }
 
     /**
      * @generated DT_ID=none
      */
     public HrSessionLocal getSessionFacade() {
         return sessionFacade;
     }
 
     private Validator validator;
 
     public void addDepartment(ActionEvent event) {
 
        if (validator == null) {
         	validator = Validation.buildDefaultValidatorFactory().getValidator();	
        }
        Department dept = new Department();
        dept.setDepartmentName("1234567890123456789012345678901234567890");
        
        Set<ConstraintViolation<Department>> violations = validator.validate(dept);
        for (ConstraintViolation<Department> violation : violations) {
            System.out.println("error size: "+violations.size());
 
     	   String propertyPath = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            FacesMessage message2 = 
         		 new FacesMessage(FacesMessage.SEVERITY_ERROR,
         				 		  "Invalid value for: '" + propertyPath + "': " + message,
         				 		  "Validation Error ");
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(event.getComponent().getClientId(), message2);
        }
        if ( violations.size() > 0 ) {
     	   return;
        }
        getSessionFacade().persistDepartment(dept);
     }
     
     public List<Department> getAllDepartments() {
     	List<Department> result = getSessionFacade().getAllDepartments();
     	System.out.println("size " + result.size() );
     	
     	
     	return result;
     }
 
 	public String getDepartmentName() {
 		return departmentName;
 	}
 
 	public void setDepartmentName(String departmentName) {
 		this.departmentName = departmentName;
 	}
 
 	public String getDepartmentLocation() {
 		return departmentLocation;
 	}
 
 	public void setDepartmentLocation(String departmentLocation) {
 		this.departmentLocation = departmentLocation;
 	}
 }
