 /*
  * CentralModule.java
  * Copyright (C) 2011 Meyer Kizner
  * All rights reserved.
  */
 
 package com.subitarius.central;
 
 import java.util.concurrent.Executor;
 
 import javax.servlet.http.HttpSession;
 
 import com.google.inject.Inject;
 import com.google.inject.Provides;
 import com.google.inject.Singleton;
 import com.google.inject.matcher.Matchers;
 import com.google.inject.persist.PersistFilter;
 import com.google.inject.servlet.RequestScoped;
 import com.google.inject.servlet.ServletModule;
 import com.subitarius.domain.Team;
 import com.subitarius.util.logging.Slf4jTypeListener;
 
 public final class CentralModule extends ServletModule {
 	static final String TEAM_ATTR = "team";
 
 	public CentralModule() {
 	}
 
 	@Override
 	protected void configureServlets() {
 		filter("/*").through(PersistFilter.class);
 
 		bind(SearcherServlet.class).in(Singleton.class);
 		serve("/searcher").with(SearcherServlet.class);
 
 		bind(AuthenticationServlet.class).in(Singleton.class);
 		serve("/auth").with(AuthenticationServlet.class);
 
 		bind(DistributedEntityServlet.class).in(Singleton.class);
		filter("/DistributedEntity").through(AuthenticationFilter.class);
		serve("/DistributedEntity").with(DistributedEntityServlet.class);
 
 		bindListener(Matchers.any(), new Slf4jTypeListener());
 	}
 
 	@Provides
 	Executor getExecutor() {
 		return new Executor() {
 			@Override
 			public void execute(Runnable runnable) {
 				new Thread(runnable, "Searcher").start();
 			}
 		};
 	}
 
 	@Provides
 	@RequestScoped
 	@Inject
 	Team getUser(HttpSession session) {
 		return (Team) session.getAttribute(TEAM_ATTR);
 	}
 }
