 /**
  * Tysan Clan Website
  * Copyright (C) 2008-2013 Jeroen Steenbeeke and Ties van de Ven
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.tysanclan.site.projectewok;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.tysanclan.site.projectewok.entities.Activation;
 import com.tysanclan.site.projectewok.entities.User;
 
 /**
  * @author Jeroen Steenbeeke
  */
 public class SessionBuilder {
 	private static Logger log = LoggerFactory.getLogger(SessionFactory.class);
 	private SessionFactory factory;
 
 	public SessionBuilder() {
 		log.debug("Initializing Hibernate");
 		Configuration ac = new Configuration();
 		log.debug("Configuration created");
 		ac.configure();
 		log.debug("Basic configuration done");
 
 		ac = ac.addAnnotatedClass(User.class);
 		ac = ac.addAnnotatedClass(Activation.class);
 
		final ServiceRegistry serviceRegistry = new ServiceRegistryBuilder()
				.applySettings(ac.getProperties()).buildServiceRegistry();
 
 		factory = ac.buildSessionFactory(serviceRegistry);
 	}
 
 	public Session createSession() {
 		return factory.openSession();
 	}
 }
