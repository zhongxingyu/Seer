 package com.google.wave.extensions.emaily.web;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.servlet.GuiceServletContextListener;
 
 public class EmailyServletContextListener extends GuiceServletContextListener {
 
 	@Override
 	protected Injector getInjector() {
		return Guice.createInjector(new EmailyServletModule());
 	}
 
 }
