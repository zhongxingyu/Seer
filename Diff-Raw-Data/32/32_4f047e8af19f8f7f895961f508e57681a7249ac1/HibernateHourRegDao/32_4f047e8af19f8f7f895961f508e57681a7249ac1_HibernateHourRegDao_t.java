 package no.steria.swhrs;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.cfg.Environment;
 import org.hibernate.context.ThreadLocalSessionContext;
 import org.hibernate.criterion.Restrictions;
 import org.joda.time.LocalDate;
 
 public class HibernateHourRegDao implements HourRegDao{
 
 	private SessionFactory sessionFactory;
 	private Person loggedInUser = Person.createDummyPerson("Ola Nordmann");
 	
 	public HibernateHourRegDao(String jndi) {
 		Configuration configuration = new Configuration();
 		configuration.setProperty(Environment.DATASOURCE, jndi);
 		configuration.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, ThreadLocalSessionContext.class.getName());
 		//configuration.addAnnotatedClass(Person.class);
 		configuration.addAnnotatedClass(HourRegistrationEntity.class);
 		sessionFactory = configuration.buildSessionFactory();		
 	}
 	
 	@Override
 	public void beginTransaction() {
 		session().beginTransaction();
 	}
 
 	@Override
 	public void endTransaction(boolean b) {
 		session().getTransaction().commit();
 	}
 
 	@Override
 	public void saveHours(HourRegistration reg) {
 		HourRegistrationEntity newEntity = HourRegistrationEntity.createRegistration(reg);
 		session().save(newEntity);
 	}
 
 	@Override
 	public List<HourRegistration> getAllHoursForDate(int person_id, LocalDate date) {
 		//This actually just gets all registrations in the database, so the parameters are useless atm
 		Criteria criteria = session().createCriteria(HourRegistrationEntity.class);
 		criteria.add(Restrictions.and(Restrictions.eq("personId", person_id),Restrictions.eq("regDate", date)));
 		List<HourRegistrationEntity> entityList = criteria.list();
 		
 		//I have not figured out how to make a query using hibernate that searches for the specific date. 
 		//Giving up for now... under I will remove the elements with wrong date from the list
 		
 		//This is very ugly, but it's ok for now since all of this is only for local testing
 		List<HourRegistration> list = new LinkedList<HourRegistration>();
 		for (HourRegistrationEntity hr: entityList) {
 			//if(hr.getDate().equals(date)) {
 				list.add(hr.createHourRegistrationFromEntity());
 			//}		
 		}
 		return list;
 	}
 	
 	@Override
 	public boolean validateUser(String username, String password){
 		//Validate the user in the database
 		return true;
 	}
 	
 	private Session session(){
 		return sessionFactory.getCurrentSession();
 	}
 
 	@Override
 	public HourRegistration getHourRegistration(int person_id,
 			String project_id, LocalDate date) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public boolean updataHourRegistration(int person_id, String project_id,
 			LocalDate date) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean deleteHourRegistration(int person_id, String project_id,
 			LocalDate date) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public List<WeekRegistration> getWeekSummary(String week) {
 		
 		LocalDate date1 = LocalDate.parse("2012-07-11");
 		LocalDate date2 = LocalDate.parse("2012-07-12");
 		LocalDate date3 = LocalDate.parse("2012-07-13");
 		LocalDate date4 = LocalDate.parse("2012-07-14");
 		LocalDate date5 = LocalDate.parse("2012-07-15");
 		
 		List<WeekRegistration> weeklist = new LinkedList<WeekRegistration>();
 		weeklist = new ArrayList();
 		weeklist.add(new WeekRegistration(2, date1, 8));
		weeklist.add(new WeekRegistration(2, date2, 2));
		weeklist.add(new WeekRegistration(2, date3, 9));
 		weeklist.add(new WeekRegistration(2, date4, 3));
 		weeklist.add(new WeekRegistration(2, date5, 7));
 		System.out.println(weeklist.toString());
 		return weeklist;
 		
 	}
 }
