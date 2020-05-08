 package com.whiterabbit.bondi.server.guice;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Singleton;
 import com.whiterabbit.bondi.server.dao.BusDAO;
 import com.whiterabbit.bondi.server.dao.impl.BusDAOImpl;
 
 public class DAOModule extends AbstractModule {
 
 	@Override
 	protected void configure() {
		bind(BusDAO.class).to(BusDAOImpl.class).in(Singleton.class);
 	}
 
 }
