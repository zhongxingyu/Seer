 package pl.agh.enrollme.converter;
 
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import pl.agh.enrollme.model.Teacher;
 import pl.agh.enrollme.repository.ITeacherDAO;
 
 import javax.faces.bean.ViewScoped;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 
 //@FacesConverter(value="teacherConverter")
 @Service
 @ViewScoped
 public class TeacherConverter implements Converter {
    Logger LOGGER = LoggerFactory.getLogger(TeacherConverter.class);
 
     @Autowired
     ITeacherDAO teacherDAO;
 
 	public Object getAsObject(FacesContext context, UIComponent component, String value) {
        LOGGER.debug("Converting from " + value + " asObject...");
         Integer ID = Integer.parseInt(value);
         return teacherDAO.getByPK(ID);
 	}
 
 	public String getAsString(FacesContext context, UIComponent component, Object value) {
        LOGGER.debug("Got for converting: " + value.toString());
         return ((Teacher) value).getTeacherID().toString();
 	}
 
 }
