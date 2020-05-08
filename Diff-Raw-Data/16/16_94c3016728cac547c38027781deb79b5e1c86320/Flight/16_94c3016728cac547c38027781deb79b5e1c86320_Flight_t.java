 package models;
 
 import java.util.*;
 import play.db.ebean.*;
 import play.db.ebean.Model.Finder;
 import play.data.format.Formats.*;
 import play.data.validation.Constraints.*;
 import javax.persistence.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import play.i18n.*;
 import com.avaje.ebean.*;
 
 @Entity
 public class Flight extends Model {
 	
 	@Id
 	public Long id;
 
 	@ManyToOne
 	@Required
 	public Plane plane;
 
 	@DateTime(pattern="dd/MM/yyyy")
 	@Required
 	public Date date = new Date();
 
 	@Required
 	public Integer duration;
 
 	@Required
 	public Double flightReduction;
 
 	@Required
 	public Double weekReduction;
 
 	@Required
 	public Double specialPrice;
 
 	public Boolean initiationFee = false;
 
	public final static Integer DEFAULT_PAGE_SIZE = 10;
 
 	public static Finder<Long, Flight> find = new Finder(Long.class, Flight.class);
 
 	public static Page<Flight> page(int page, int pageSize) {
 		return
 			find.where()
 				.findPagingList(pageSize)
 				.getPage(page);
 	}
 
 	public String toString() {
 		return Messages.get("models.flight.toString", id, new SimpleDateFormat("dd/MM/yyyy").format(date), duration);
 	}
 }
