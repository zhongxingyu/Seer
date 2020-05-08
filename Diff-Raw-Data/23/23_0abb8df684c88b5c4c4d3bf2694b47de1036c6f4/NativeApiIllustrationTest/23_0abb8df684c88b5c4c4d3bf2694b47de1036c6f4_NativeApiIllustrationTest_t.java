 package edu.vt.jgwes.test;
 
 import java.util.Date;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.Configuration;
 
 /**
  * Illustrates use of Hibernate native APIs.
  *
  * @author Steve Ebersole
  */
 public class NativeApiIllustrationTest extends TestCase {
 	private SessionFactory sessionFactory;
 
 	@Override
 	protected void setUp() throws Exception {
 		// A SessionFactory is set up once for an application
         sessionFactory = new Configuration()
                 .configure() // configures settings from hibernate.cfg.xml
                 .buildSessionFactory();
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		if ( sessionFactory != null ) {
 			sessionFactory.close();
 		}
 	}
 
 	public void testBasicUsage() {
 		// create a couple of events...
 		Session session = sessionFactory.openSession();
 		session.beginTransaction();
 		session.save( new Event( "Our very first event!", new Date() ) );
 		session.save( new Event( "A follow up event", new Date() ) );
 		session.getTransaction().commit();
 		session.close();
 
 		// now lets pull events from the database and list them
 		session = sessionFactory.openSession();
         session.beginTransaction();
         List result = session.createQuery( "from Event" ).list();
 		for ( Event event : (List<Event>) result ) {
 			System.out.println( "Event (" + event.getDate() + ") : " + event.getTitle() );
 		}
         session.getTransaction().commit();
         session.close();
 	}
 }
