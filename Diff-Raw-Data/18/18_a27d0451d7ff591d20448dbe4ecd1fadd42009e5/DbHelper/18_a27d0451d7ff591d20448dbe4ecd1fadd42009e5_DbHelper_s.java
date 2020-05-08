 package org.walkingarchive.backend;
 
 import org.hibernate.service.ServiceRegistryBuilder;
 import org.hibernate.service.ServiceRegistry;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.Session;
 
 public class DbHelper
 {
	public static Session getSession()
	{
		Configuration config = new Configuration();
		config.configure();
		ServiceRegistry registry = new ServiceRegistryBuilder()
			.applySettings(config.getProperties())
			.buildServiceRegistry();
		return config.buildSessionFactory(registry).openSession();
	}
 }
