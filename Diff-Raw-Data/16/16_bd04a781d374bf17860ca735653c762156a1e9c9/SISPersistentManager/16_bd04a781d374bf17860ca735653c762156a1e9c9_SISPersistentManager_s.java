 package org.iucn.sis.server.api.persistance;
 
 
 import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 import java.util.Properties;
 
 import javassist.util.proxy.MethodFilter;
 
 import org.dom4j.DocumentException;
 import org.gogoego.api.plugins.GoGoEgo;
 import org.hibernate.HibernateException;
 import org.hibernate.LockMode;
 import org.hibernate.MappingException;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.dialect.H2Dialect;
 import org.hibernate.dialect.PostgreSQLDialect;
 import org.hibernate.tool.hbm2ddl.SchemaExport;
 import org.hibernate.tuple.entity.EntityTuplizerFactory;
 import org.hibernate.tuple.entity.PojoEntityTuplizer;
 import org.iucn.sis.server.api.application.MultiClassLoader;
 import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
 import org.iucn.sis.server.api.persistance.listeners.SISHibernateListener;
 import org.iucn.sis.server.api.persistance.ormmapping.ClassLoaderTester;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.postgresql.Driver;
 import org.slf4j.impl.StaticLoggerBinder;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.solertium.util.BaseDocumentUtils;
 import com.solertium.util.ElementCollection;
 
 public class SISPersistentManager {
 
 	private static SISPersistentManager instance;
 	private SessionFactory sessionFactory;
 
 	public static void setCurrentThread() {
 		Thread.currentThread().setContextClassLoader(createClassLoader());
 	}
 	
 	private static ClassLoader createClassLoader() {
 		MultiClassLoader loader = new MultiClassLoader(SISPersistentManager.class.getClassLoader().getParent());
 		loader.addClassLoader(SISPersistentManager.class.getClassLoader());
 		loader.addClassLoader(MethodFilter.class.getClassLoader());
 		loader.addClassLoader(PojoEntityTuplizer.class.getClassLoader());
 		loader.addClassLoader(EntityTuplizerFactory.class.getClassLoader());
 		loader.addClassLoader(StaticLoggerBinder.class.getClassLoader());
 		loader.addClassLoader(DocumentException.class.getClassLoader());
 		loader.addClassLoader(DocumentException.class.getClassLoader().getParent());
 		loader.addClassLoader(SISHibernateListener.class.getClassLoader());
 		
 		//Postgresql
 		loader.addClassLoader(Driver.class.getClassLoader());
 		loader.addClassLoader(PostgreSQLDialect.class.getClassLoader());
 		
 		//H2
 		loader.addClassLoader(org.h2.Driver.class.getClassLoader());
 		loader.addClassLoader(H2Dialect.class.getClassLoader());
 		
 		//Access
 		//loader.addClassLoader(com.hxtt.sql.access.AccessDriver.class.getClassLoader());
 		//loader.addClassLoader(com.hxtt.support.hibernate.HxttAccessDialect.class.getClassLoader());
 		
 		return loader;
 	}
 
 	public static synchronized final SISPersistentManager instance() {
 		if (instance == null || instance.sessionFactory == null) {
 			setCurrentThread();
 			instance = new SISPersistentManager();
 			instance.sessionFactory = instance.buildSessionFactory("sis", GoGoEgo.getInitProperties());
 		}
 		return instance;
 	}
 	
 	public static synchronized final SISPersistentManager newInstance(String name, Properties properties, boolean fresh) {
 		setCurrentThread();
 		
 		SISPersistentManager instance = new SISPersistentManager();
 		
 		if (fresh) {
 			Configuration config = instance.buildConfiguration(name, properties);
 			
 			SchemaExport export = new SchemaExport(config);
 			export.create(false, true);
 			
 			instance.sessionFactory = config.buildSessionFactory();		
 		}
 		else
 			instance.sessionFactory = instance.buildSessionFactory(name, properties);
 		
 		return instance;
 	}
 
 	private SessionFactory buildSessionFactory(String session, Properties properties) {
 		return buildConfiguration(session, properties).buildSessionFactory();
 	}
 
 	public SessionFactory getSessionFactory() {
 		return sessionFactory;
 	}
 	
 	public Session openSession() {
 		setCurrentThread();
 		return sessionFactory.openSession();
 	}
 
 	public void shutdown() {
 		sessionFactory.close();
 	}
 	
 	private Configuration buildConfiguration(String session, Properties properties) {
		String generator = properties.getProperty("generator", null);
 		
 		Configuration configuration = new SISPersistenceConfiguration(generator);
 		setCurrentThread();
 		
 		final String configUri = properties.getProperty("org.iucn.sis.server.configuration.uri", "local:postgres");
 		if (configUri.startsWith("file")) {
 			Debug.println("Creating new persistence manager from file " + configUri);
 			try {
 				configuration.configure(new URL(configUri));
 			} catch (MalformedURLException e) {
 				throw new RuntimeException(configUri + " is not a valid URL", e);
 			} catch (HibernateException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		else if (configUri.startsWith("local:") && !configUri.equals("local:")) {
 			final String name = configUri.substring(configUri.indexOf(':')+1);
 			Debug.println("Creating new persistence manager from local resource " + name);
 			try {
 				configuration.configure(ClassLoaderTester.class.getResource(name + ".cfg.xml"));
 			} catch (HibernateException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		
 		String dialect = properties.getProperty("database_dialect");
 		
 		String driverClass = properties.getProperty("dbsession."+session+".driver");
 		String connectionURL = properties.getProperty("dbsession."+session+".uri");
 		String username = properties.getProperty("dbsession."+session+".user");
 		String password = properties.getProperty("dbsession."+session+".password");
 		
 		SISHibernateListener listener = new SISHibernateListener();
 		
 		configuration.setProperty("hibernate.dialect", dialect);
 		configuration.setProperty("hibernate.connection.driver_class", driverClass);
 		configuration.setProperty("hibernate.connection.url", connectionURL);
 		configuration.setProperty("hibernate.connection.username", username);
 		configuration.setProperty("hibernate.connection.password", password);
 		configuration.setListener("post-update", listener);
 		configuration.setListener("post-delete", listener);
 		configuration.setListener("post-insert", listener);
 
 		return configuration;
 	}
 	
 
 	public void saveObject(Session session, Object obj) throws PersistentException {		
 		session.saveOrUpdate(obj);
 	}
 
 	public void deleteObject(Session session, Object obj) throws PersistentException {
 		session.delete(obj);
 	}
 
 	public void lockObject(Session session, Object obj) throws PersistentException {
 		session.lock(obj, LockMode.NONE);
 		//FIXME: should we be using the code below?
 		//session.buildLockRequest(LockOptions.NONE).lock(obj);
 	}
 	
 	public void updateObject(Session session, Object obj) throws PersistentException {
 		session.update(obj);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public <X> X mergeObject(Session session, X obj) throws PersistentException {
 		try {
 			return (X) session.merge(obj);
 		} catch (ClassCastException e) {
 			throw new PersistentException(e);
 		} catch (HibernateException e) {
 			throw new PersistentException(e);
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public <X> X getObject(Session session, Class<X> clazz, Serializable id) throws PersistentException {
 		try {
 			return (X) session.get(clazz, id);
 		} catch (ClassCastException e) {
 			throw new PersistentException(e);
 		} catch (HibernateException e) {
 			throw new PersistentException(e);
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	public <X> X loadObject(Session session, Class<X> clazz, Serializable id) throws PersistentException {
 		try {
 			return (X) session.load(clazz, id);
 		} catch (ClassCastException e) {
 			throw new PersistentException(e);
 		} catch (HibernateException e) {
 			throw new PersistentException(e);
 		}
 	}
 	@SuppressWarnings("unchecked")
 	public <X> List<X> listObjects(Class<X> criteria, Session session) throws PersistentException {
 		try {
 			return session.createCriteria(criteria).list();
 		} catch (ClassCastException e) {
 			throw new PersistentException(e);
 		} catch (HibernateException e) {
 			throw new PersistentException(e);
 		}
 	}
 	
 	private static class SISPersistenceConfiguration extends Configuration {
 		
 		private static final long serialVersionUID = 1L;
 		
 		private final String generator;
 		
 		public SISPersistenceConfiguration(String generator) {
 			super();
 			this.generator = generator;
 		}
 		
 		@Override
 		public Configuration addResource(String arg0) throws MappingException {
 			return addResource(arg0, ClassLoaderTester.class.getClassLoader());
 		}
 		
 		@Override
 		public Configuration addResource(String arg0, ClassLoader arg1)
 				throws MappingException {
 			if (generator == null)
 				return addInputStream(arg1.getResourceAsStream(arg0));
 			
 			try {
 				final Document document = BaseDocumentUtils.impl.getInputStreamFile(arg1.getResourceAsStream(arg0));
 				final ElementCollection nodes = 
 					new ElementCollection(document.getDocumentElement().getElementsByTagName("generator"));
 				for (Element el : nodes) {
 					el.setAttribute("class", generator);
 					break;
 				}
 				return addDocument(document);
 			} catch (Exception e) {
 				return addInputStream(arg1.getResourceAsStream(arg0));	
 			}
 		}
 	}
 
 }
