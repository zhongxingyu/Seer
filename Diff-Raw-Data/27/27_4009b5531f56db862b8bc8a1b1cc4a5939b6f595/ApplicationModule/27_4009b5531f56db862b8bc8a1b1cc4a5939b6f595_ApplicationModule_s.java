 /**
  * 
  */
 package com.chinarewards.qqgbvpn.main;
 
 import javax.inject.Singleton;
 
 import com.chinarewards.qqgbvpn.common.SimpleDateTimeModule;
 import com.chinarewards.qqgbvpn.main.protocol.filter.LoginFilter;
 import com.google.inject.AbstractModule;
 
 /**
  * 
  * 
  * @author Cyril
  * @since 1.0.0
  */
 public class ApplicationModule extends AbstractModule {
 
 	@Override
 	protected void configure() {
 
 		install(new SimpleDateTimeModule());
 
 		// log
 		bind(LogConfig.class).to(LogConfigImpl.class).in(Singleton.class);
 
		bind(LoginFilter.class);
 	}
 
 }
