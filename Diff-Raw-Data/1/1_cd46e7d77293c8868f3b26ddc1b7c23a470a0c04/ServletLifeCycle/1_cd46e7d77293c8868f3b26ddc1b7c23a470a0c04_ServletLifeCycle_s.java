 /*
  * @(#) $CVSHeader:  $
  *
  * Copyright (C) 2008 by Netcetera AG.
  * All rights reserved.
  *
  * The copyright to the computer program(s) herein is the property of
  * Netcetera AG, Switzerland.  The program(s) may be used and/or copied
  * only with the written permission of Netcetera AG or in accordance
  * with the terms and conditions stipulated in the agreement/contract
  * under which the program(s) have been supplied.
  *
  * @(#) $Id: codetemplates.xml,v 1.5 2004/06/29 12:49:49 hagger Exp $
  */
 package net.java.dev.cejug.classifieds.server.welcome;
 
 import java.util.Calendar;
 
 import javax.ejb.EJB;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import javax.xml.ws.WebServiceException;
 
 import net.java.dev.cejug.classifieds.server.ejb3.entity.ServiceLifeCycleEntity;
 import net.java.dev.cejug.classifieds.server.ejb3.entity.facade.ServiceLifeCycleFacadeLocal;
 
 public class ServletLifeCycle implements ServletContextListener {
 	@EJB
 	ServiceLifeCycleFacadeLocal lifeObserver;
 	private int id = -1;
 
 	@Override
 	public void contextDestroyed(ServletContextEvent arg0) {
 		try {
 			ServiceLifeCycleEntity lifeCycle = lifeObserver.read(
 					ServiceLifeCycleEntity.class, id);
 			lifeCycle.setFinish(Calendar.getInstance());
 			lifeObserver.update(lifeCycle);
 		} catch (Exception e) {
 			throw new WebServiceException(e.getMessage());
 		}
 	}
 
 	@Override
 	public void contextInitialized(ServletContextEvent arg0) {
 		try {
 			ServiceLifeCycleEntity lifeCycle = new ServiceLifeCycleEntity();
			lifeCycle = new ServiceLifeCycleEntity();
 			lifeCycle.setName("test");
 			lifeCycle.setStart(Calendar.getInstance());
 			lifeObserver.create(lifeCycle);
 			id = lifeCycle.getId();
 		} catch (Exception e) {
 			throw new WebServiceException(e.getMessage());
 		}
 	}
 }
