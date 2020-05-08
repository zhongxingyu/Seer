 package pl.agh.enrollme.webflow.autocomplete;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import javax.faces.convert.FacesConverter;
 
 import org.springframework.util.StringUtils;
 
 import pl.agh.enrollme.model.Person;
 
 @FacesConverter(value="personConverter")
 public class PersonConverter implements Converter {
 
 	private static List<Person> cache = new ArrayList<Person>();
 	
 	static {
 		cache.add(new Person(0, "Jamie", "Carr"));
 		cache.add(new Person(1, "Jean", "Cobbs"));
 		cache.add(new Person(2, "John", "Howard"));
 		cache.add(new Person(3, "John", "Mudra"));
 		cache.add(new Person(4, "Julia", "Webber"));
 	}
 
 	public Object getAsObject(FacesContext context, UIComponent component, String value) {
 		if (StringUtils.hasText(value)) {
 			for (Person p : cache) {
 				if ((p.getFirstName()+" "+p.getLastName()).equals(value)) {
 					return p;
 				}
 			}
 		}
 		return null;
 	}
 
 	public String getAsString(FacesContext context, UIComponent component, Object value) {
         if (value != null) {
         	final Person person = (Person) value;
            return String.valueOf(person.getFirstName() + " " + person.getLastName());
         }
         return null;
 	}
 
 }
