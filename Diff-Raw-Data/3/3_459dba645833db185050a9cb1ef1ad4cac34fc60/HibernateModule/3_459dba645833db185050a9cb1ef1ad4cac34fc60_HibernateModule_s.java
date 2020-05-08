 package nz.ac.victoria.ecs.kpsmart.integration;
 
 import java.io.IOException;
 import java.util.Properties;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.service.ServiceRegistry;
 import org.hibernate.service.ServiceRegistryBuilder;
 
 import com.google.inject.Provider;
 
 import nz.ac.victoria.ecs.kpsmart.LifecycleModule;
 import nz.ac.victoria.ecs.kpsmart.entities.logging.CarrierDeleteEvent;
 import nz.ac.victoria.ecs.kpsmart.entities.logging.CarrierUpdateEvent;
 import nz.ac.victoria.ecs.kpsmart.entities.logging.CustomerPriceDeleteEvent;
 import nz.ac.victoria.ecs.kpsmart.entities.logging.CustomerPriceUpdateEvent;
 import nz.ac.victoria.ecs.kpsmart.entities.logging.DomesticCustomerPriceDeleteEvent;
 import nz.ac.victoria.ecs.kpsmart.entities.logging.DomesticCustomerPriceUpdateEvent;
 import nz.ac.victoria.ecs.kpsmart.entities.logging.EventID;
 import nz.ac.victoria.ecs.kpsmart.entities.logging.LocationDeleteEvent;
 import nz.ac.victoria.ecs.kpsmart.entities.logging.LocationUpdateEvent;
 import nz.ac.victoria.ecs.kpsmart.entities.logging.MailDeliveryDeleteEvent;
 import nz.ac.victoria.ecs.kpsmart.entities.logging.MailDeliveryUpdateEvent;
 import nz.ac.victoria.ecs.kpsmart.entities.logging.RouteDeleteEvent;
 import nz.ac.victoria.ecs.kpsmart.entities.logging.RouteUpdateEvent;
 import nz.ac.victoria.ecs.kpsmart.entities.state.Carrier;
 import nz.ac.victoria.ecs.kpsmart.entities.state.CustomerPrice;
 import nz.ac.victoria.ecs.kpsmart.entities.state.CustomerPriceID;
 import nz.ac.victoria.ecs.kpsmart.entities.state.DomesticCustomerPrice;
 import nz.ac.victoria.ecs.kpsmart.entities.state.EntityID;
 import nz.ac.victoria.ecs.kpsmart.entities.state.Location;
 import nz.ac.victoria.ecs.kpsmart.entities.state.MailDelivery;
 import nz.ac.victoria.ecs.kpsmart.entities.state.Route;
 
 public class HibernateModule extends LifecycleModule {
 	private final String propertiesFileName;
 	
 	public HibernateModule() {
 		this("hibernate.properties");
 	}
 	
 	public HibernateModule(String fileName) {
 		this.propertiesFileName = fileName;
 	}
 	
 	private SessionFactory factory = null;
 	private Session session = null;
 	
 	@Override
 	public void unload() {
 		this.session.cancelQuery();
 		this.session.flush();
 		this.session.close();
 		this.factory.close();
 		this.factory = null;
 		this.session = null;
 	}
 
 	@Override
 	public void load() {
 		this.session = getSession(this.propertiesFileName);
 		this.session.beginTransaction();
 	}
 
 	@Override
 	protected void configure() {
 		bind(Session.class).toProvider(new Provider<Session>() {
 			@Override
 			public Session get() {
 				return session;
 			}
 		});
 	}
 	
 	protected Session getSession(String configurationFileName) {
 		// Configure hibernate
 		final Class<?>[] annotatedClasses = {
 				CustomerPriceUpdateEvent.class,
 				CustomerPriceDeleteEvent.class,
 				
 				MailDeliveryUpdateEvent.class,
 				MailDeliveryDeleteEvent.class,
 				
 				CarrierUpdateEvent.class,
 				CarrierDeleteEvent.class,
 				
 				DomesticCustomerPriceUpdateEvent.class,
 				DomesticCustomerPriceDeleteEvent.class,
 				
 				RouteUpdateEvent.class,
 				RouteDeleteEvent.class,
 				
 				LocationUpdateEvent.class,
 				LocationDeleteEvent.class,
 				
 				EventID.class,
 				
 				
 				EntityID.class,
 				Carrier.class,
 				Location.class,
 				MailDelivery.class,
 				Route.class,
 				EntityID.class,
 				CustomerPrice.class,
 				CustomerPriceID.class,
 				DomesticCustomerPrice.class
 		};
 		
 		try {
 			Properties p = new Properties();
 			p.load(this.getClass().getClassLoader().getResourceAsStream(configurationFileName));			
 			final Configuration configuration = new Configuration();
 			configuration.addProperties(p);
 			
 			for (final Class<?> c : annotatedClasses)
 				configuration.addAnnotatedClass(c);
 			
 			ServiceRegistry serviceRegistry = new ServiceRegistryBuilder().applySettings(configuration.getProperties()).buildServiceRegistry();
 			this.factory = configuration.buildSessionFactory(serviceRegistry);
 			return factory.getCurrentSession();
 		} catch (IOException e) {
 			throw new RuntimeException("Could not load the hibernate properties file", e);
 		}
 	}
 }
