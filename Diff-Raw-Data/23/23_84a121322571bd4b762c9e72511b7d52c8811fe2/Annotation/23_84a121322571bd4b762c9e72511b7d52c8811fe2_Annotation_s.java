 package models;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.bson.types.ObjectId;
 
 import com.google.code.morphia.annotations.Entity;
 import com.google.code.morphia.annotations.Reference;
 
 import controllers.MorphiaObject;
 
 @Entity("Resources")
public class Annotation extends Resource{

 	@Reference
 	private Resource annotated ;
 	
 	private String annotatedContent ;
 	
 	private String pointerBegin ; //du coup pointer mériterait d'être une classe avec sa propre méthode equals et une hierarchie de sous types
 	
 	private String pointerEnd ;
 	
 	
 	public Resource getAnnotated() {
 		return annotated;
 	}
 	
 	public void setAnnotated(Resource annotated) {
 		this.annotated = annotated;
 	}
 	
 	public String getAnnotatedContent() {
 		return annotatedContent;
 	}
 	
 	public void setAnnotatedContent(String annotatedContent) {
 		this.annotatedContent = annotatedContent;
 	}
 	
 	public String getPointerBegin() {
 		return pointerBegin;
 	}
 	
 	public void setPointerBegin(String pointerBegin) {
 		this.pointerBegin = pointerBegin;
 	}
 	
 	public String getPointerEnd() {
 		return pointerEnd;
 	}
 	
 	public void setPointerEnd(String pointerEnd) {
 		this.pointerEnd = pointerEnd;
 	}
 	
 	public static List<Annotation> allAnnotation() {
 		List<Annotation> annotations = new ArrayList<Annotation>() ;
 		if (MorphiaObject.datastore != null) {
 			List<Resource> ressources = MorphiaObject.datastore.find(Resource.class).asList();
 			for(Resource r : ressources) 
 			{
 				if(r.getClass().equals(Annotation.class)) annotations.add((Annotation)r);
 			}
 			return annotations ;
 		} else {
 			return annotations ;
 		}
 	}
 	
 	public static void create(Annotation annotation) {
 		annotation.setCreationDate(new Date());
 		MorphiaObject.morphia.map(Resource.class);
 		MorphiaObject.datastore.save(annotation);
 	}
 
 	public static void delete(String idToDelete) {
 		Annotation toDelete = MorphiaObject.datastore.find(Annotation.class).field("_id").equal(new ObjectId(idToDelete)).get();
 		if (toDelete != null) {
 			//Logger.info("toDelete: " + toDelete);
 			MorphiaObject.datastore.delete(toDelete);
 		} else {
 			//Logger.debug("ID No Found: " + idToDelete);
 		}
 	}
 	
 	public static Annotation findById(String id)
 	{
 		return MorphiaObject.datastore.find(Annotation.class).field("_id").equal(new ObjectId(id)).get();
 	}
 	
 	public static List<Annotation> findByResourceId(String id)
 	{
 		Resource resource = Resource.findById(id);
 		return MorphiaObject.datastore.find(Annotation.class).field("annotated").equal(resource).asList();
 	}
 	
 	public static List<Annotation> findByResourceId(String id, int start, int end)
 	{
		return Annotation.findByResourceId(id).subList(start, end);
 	}
 	
 	public static List<Annotation> findByAuthor(UserAccount author)
 	{
 		List<Annotation> annotations = new ArrayList<Annotation>();
 		
 		if (MorphiaObject.datastore != null) {
 			List<Resource> ressources = MorphiaObject.datastore.find(Resource.class).asList();
 			for(Resource r : ressources) 
 			{
 				if(r.getClass().equals(Annotation.class) && r.getAuthor().isSameUser(author))
 				{
 					annotations.add((Annotation)r);
 				}
 			}
 			return annotations ;
 		} else {
 			return annotations ;
 		}
 	}
 	
 	public static List<Annotation> findByRange(int start, int end)
 	{
 		List<Annotation> annotations = Annotation.allAnnotation();
		return annotations.subList(start, end);
 	}
 }
