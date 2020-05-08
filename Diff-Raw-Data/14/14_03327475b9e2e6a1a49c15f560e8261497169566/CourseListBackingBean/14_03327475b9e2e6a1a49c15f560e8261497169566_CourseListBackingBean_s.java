 package wfm.bean;
 
 import java.io.Serializable;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.List;
 import javax.enterprise.context.RequestScoped;
 import javax.faces.bean.ManagedBean;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import wfm.bean.ItemEntry;
 import wfm.db.ACT_ID_USER;
 import wfm.db.Course;
 import wfm.db.USER_COURSE;
 
 
 @Named
 @RequestScoped
 @ManagedBean(name="courses")
 public class CourseListBackingBean implements Serializable{
 
 	private static final long serialVersionUID = 1962753564688979487L;
 	
 	private static final Logger log = LoggerFactory.getLogger(CourseListBackingBean.class);
 
 	private List<ItemEntry> items;
 	private List<ItemEntry> personalItems; //for showing up in the delete courses screen
 	private String subscribedText;
 
 	@PersistenceContext
 	private EntityManager entityManager;
 
 	@Inject
 	private User user;
 	
 	@Inject
 	private ApproveCourseBean approveBean;
 
 	public List<ItemEntry> getItems(){
 		Timestamp currentDate = new Timestamp(System.currentTimeMillis());		
 		log.info("Current Date: "+currentDate);
 
 		Query query = entityManager.createQuery("SELECT c FROM Course c WHERE c.date > '"+currentDate+"'");
 		@SuppressWarnings("unchecked")
 		List<Course> courses =  query.getResultList();
 		
 		items = new ArrayList<ItemEntry>();
 		ItemEntry e;
 		Course c;
 		for(int i= 0; i<courses.size(); i++){
 			e = new ItemEntry();
 			c = courses.get(i);
 			e.setCourse(c);
 			items.add(e);
 		}
 
 		return (ArrayList<ItemEntry>) items;
 	}
 	public void setItems(List<ItemEntry> items)
 	{
 		this.items = items;
 	}
 
 	public List<ItemEntry> getPersonalItems(){
 
		log.info("Username for showing personal courses: "+user.getUsername());
 		Timestamp currentDate = new Timestamp(System.currentTimeMillis());		
 		log.info("Current Date: "+currentDate);
 		Query query = entityManager.createQuery("SELECT c FROM Course c WHERE c.trainer='"+user.getUsername()+"' and c.date > '"+currentDate+"'");
 		@SuppressWarnings("unchecked")
 		List<Course> courses =  query.getResultList();
 
 		personalItems = new ArrayList<ItemEntry>();
 		ItemEntry e;
 		Course c;
 		for(int i= 0; i<courses.size(); i++){
 			e = new ItemEntry();
 			c = courses.get(i);
 			e.setCourse(c);
 			personalItems.add(e);
 		}
 
 		return (ArrayList<ItemEntry>) personalItems;
 	}
 	public void setPersonalItems(List<ItemEntry> personalItems)
 	{
 		this.personalItems = personalItems;
 	}
 
 	public boolean checkSubscription(int courseNr)//check if user is already subscribed to the course
 	{
 		boolean subscribed = false;
 	
 		Course c = entityManager.find(Course.class, courseNr);
 		
 		
 		for(USER_COURSE u : c.getUserCourse()){
 			if(u.getPk().getUser().getId_().equals(user.getUsername())){
 				subscribed = true;
 				setSubscribedText("subscribed");
 			}
 					
 		}
 		return subscribed;
 	}
 	public String getSubscribedText() {
 		return subscribedText;
 	}
 	public void setSubscribedText(String subscribedText) {
 		this.subscribedText = subscribedText;
 	}
 	
 
 	
 	
 
 }
