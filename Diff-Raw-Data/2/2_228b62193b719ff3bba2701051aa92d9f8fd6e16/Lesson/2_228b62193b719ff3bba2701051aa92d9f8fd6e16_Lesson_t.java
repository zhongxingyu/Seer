 package models;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 
 import play.data.validation.Check;
 import play.data.validation.CheckWith;
 import play.data.validation.MaxSize;
 import play.data.validation.Required;
 import play.data.validation.Validation;
 import play.db.jpa.Model;
 
 @Entity(name = "lesson")
 public class Lesson extends Model {
 
 	@Required
 	public String hebrewName;
 	
 	public String originalName;
 
 	@CheckWith(UrlCheck.class)
 	public String url;
 	
 	public boolean linkless;
 	
 	@MaxSize(1000)
 	public String description;
 
 	@Required
 	public int serialNumber;
 	
 	@Required
 	@ManyToOne
 	@JoinColumn(name = "topic_id", nullable = false)
 	public Topic topic;
 
 	public static List<Lesson> sort(List<Lesson> lessons) {
 		Collections.sort(lessons, new Comparator<Lesson>() {
 
 			@Override
 			public int compare(Lesson o1, Lesson o2) {
 				return o1.serialNumber - o2.serialNumber;
 			}
 
 		});
 		return lessons;
 	}
 	
 	static class UrlCheck extends Check {
         
         public boolean isSatisfied(Object lesson, Object url) {
         	Lesson l = (Lesson) lesson;
         	if (!l.linkless && !Validation.required("lesson.url.required", url).ok) {
        			return false;
         	}
         	return Validation.url("lesson.url.notvalid", url).ok;
             
         }
     }
 
 }
