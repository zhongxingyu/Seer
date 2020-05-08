 package be.lreenaers.lafay.web.converters;
 
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import javax.faces.convert.FacesConverter;
 
 import org.bson.types.ObjectId;
 
 import be.lreenaers.lafay.DAOs.ExerciceDAO;
 import be.lreenaers.lafay.beans.Exercice;
 import be.lreenaers.lafay.factories.DAOFactory;
 
 @FacesConverter(forClass = Exercice.class, value = "exerciceConverter")
 public class ExerciceConverter implements Converter {
 
 	@Override
 	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
 		ExerciceDAO dao = DAOFactory.getExerciceDAO();
 		return dao.findById(new ObjectId(arg2));
 	}
 
 	@Override
 	public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
		String str= "null";
		if(arg2 != null){
			str = ((Exercice) arg2).getId().toString();
		}
		return str;
 	}
 
 }
