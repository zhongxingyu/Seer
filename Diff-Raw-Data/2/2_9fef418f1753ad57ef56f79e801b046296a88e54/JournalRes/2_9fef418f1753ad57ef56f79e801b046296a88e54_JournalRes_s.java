 package pfm.jaxrs;
 
import java.text.DateFormat;
import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.GenericEntity;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.core.UriInfo;
 
 import pfm.model.helper.JournalEntry;
 
 @Path("journal")
 @Stateless
 public class JournalRes {
     @Context
     private UriInfo context;
 
     @PersistenceContext
     EntityManager em;
     
     public JournalRes() {
     }
     
     @GET
 	@Path("/list")
 	@Produces("application/json")
 	public Response getJsonList(@QueryParam("userid") int id, 
 								@QueryParam("timeframe") int timeframe) {
 		
     	Date to;
     	Date from;
     	
     	Calendar cal = new GregorianCalendar();
     	cal.add(Calendar.DAY_OF_YEAR, 1);
     	cal.set(Calendar.SECOND, 0);
     	cal.set(Calendar.MINUTE, 0);
     	cal.set(Calendar.HOUR_OF_DAY, 0);
     	to = cal.getTime();
     	
     	switch (timeframe) {
 			case 1:
 				cal = new GregorianCalendar();
 		    	cal.set(Calendar.SECOND, 0);
 		    	cal.set(Calendar.MINUTE, 0);
 		    	cal.set(Calendar.HOUR_OF_DAY, 0);
 		    	from = cal.getTime();
 		    	break;
 				
 			case 2:
 				cal = new GregorianCalendar();
 				cal.add(Calendar.WEEK_OF_YEAR, -1);
 		    	cal.set(Calendar.SECOND, 0);
 		    	cal.set(Calendar.MINUTE, 0);
 		    	cal.set(Calendar.HOUR_OF_DAY, 0);
 		    	from = cal.getTime();
 				break;
 				
 			case 3:
 				cal = new GregorianCalendar();
 				cal.add(Calendar.MONTH, -1);
 		    	cal.set(Calendar.SECOND, 0);
 		    	cal.set(Calendar.MINUTE, 0);
 		    	cal.set(Calendar.HOUR_OF_DAY, 0);
 		    	from = cal.getTime();
 				break;
 				
 			case 4:
 				cal = new GregorianCalendar();
 				cal.add(Calendar.YEAR, -1);
 		    	cal.set(Calendar.SECOND, 0);
 		    	cal.set(Calendar.MINUTE, 0);
 		    	cal.set(Calendar.HOUR_OF_DAY, 0);
 		    	from = cal.getTime();
 				break;
 	
 			default:
 				cal = new GregorianCalendar();
 		    	cal.set(Calendar.SECOND, 0);
 		    	cal.set(Calendar.MINUTE, 0);
 		    	cal.set(Calendar.HOUR_OF_DAY, 0);
 		    	from = cal.getTime();
 				break;
 		}
     	
     	Query query = em.createNamedQuery("findJournalEntries");
     	query.setParameter(1, id);
     	query.setParameter(2, from);
     	query.setParameter(3, to);
     	List<JournalEntry> list = query.getResultList();
     	
     	if (!list.isEmpty()) {
     		GenericEntity<List<JournalEntry>> entity = new GenericEntity<List<JournalEntry>>(list) {};
     		return Response.ok(entity).build();
     	} else {
     		return Response.status(Status.NOT_FOUND).build();
     	}	
 	}
 }
