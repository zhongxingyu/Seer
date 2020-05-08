 /*
  * CentralContextListener.java
  * Copyright (C) 2011 Meyer Kizner
  * All rights reserved.
  */
 
 package com.subitarius.central;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.persist.jpa.JpaPersistModule;
 import com.google.inject.servlet.GuiceServletContextListener;
 import com.subitarius.central.search.SearchModule;
import com.subitarius.domain.DomainModule;
 import com.subitarius.util.http.HttpModule;
 
 public final class CentralContextListener extends GuiceServletContextListener {
 	public CentralContextListener() {
 	}
 
 	@Override
 	protected Injector getInjector() {
 		return Guice.createInjector(new JpaPersistModule("central"),
				new DomainModule(), new CentralModule(), new SearchModule(),
				new HttpModule());
 	}
 }
