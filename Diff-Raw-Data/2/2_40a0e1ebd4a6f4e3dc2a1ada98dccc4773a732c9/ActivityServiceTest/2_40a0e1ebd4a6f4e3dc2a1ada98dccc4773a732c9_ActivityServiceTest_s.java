 package no.fll.activity;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.List;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.orm.hibernate3.HibernateTemplate;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"/fll-ds-test.xml","/fll-dao.xml"})
 public class ActivityServiceTest {
 	@Autowired
 	private HibernateTemplate hibernateTemplate;
 	
 	@Test
 	public void testCrud() {
 		Activity activity = new Activity(1, "a", 15, "09:00");
 		hibernateTemplate.save(activity);
 		List<Activity> activities = hibernateTemplate.loadAll(Activity.class);
 		assertEquals(1, activities.size());
 		assertEquals(activity, activities.get(0));
 		hibernateTemplate.delete(activities.get(0));
 		activities = hibernateTemplate.loadAll(Activity.class);
 		assertEquals(0, activities.size());
 	}
 }
