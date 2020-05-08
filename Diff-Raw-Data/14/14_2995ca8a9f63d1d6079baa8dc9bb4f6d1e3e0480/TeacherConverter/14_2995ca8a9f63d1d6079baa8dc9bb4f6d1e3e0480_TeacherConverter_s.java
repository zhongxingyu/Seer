 package pl.agh.enrollme.converter;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import pl.agh.enrollme.model.Teacher;
 import pl.agh.enrollme.repository.ITeacherDAO;
 
 import javax.faces.bean.ViewScoped;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
 
 //@FacesConverter(value="teacherConverter")
 @Service
 @ViewScoped
 public class TeacherConverter implements Converter {
 
     @Autowired
     ITeacherDAO teacherDAO;
 
 	public Object getAsObject(FacesContext context, UIComponent component, String value) {
        System.out.println("[partyks DEBUG] Get for parsing:" + value);
         Integer ID = Integer.parseInt(value);
        System.out.println("[partyks DEBUG] after parsing:" + ID);
         return teacherDAO.getByPK(ID);
 	}
 
 	public String getAsString(FacesContext context, UIComponent component, Object value) {
        System.out.println("[partyks DEBUG] returned as: " + ((Teacher) value).getTeacherID().toString());
         return ((Teacher) value).getTeacherID().toString();
 	}
 
 }
