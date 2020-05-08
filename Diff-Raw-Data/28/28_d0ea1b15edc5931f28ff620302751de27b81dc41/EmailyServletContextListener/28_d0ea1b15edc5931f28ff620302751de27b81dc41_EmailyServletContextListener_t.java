 package com.google.wave.extensions.emaily.web;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.google.wave.extensions.emaily.robot.EmailyRobotServlet;
 
 public class EmailyServletContextListener extends GuiceServletContextListener {
 
 	@Override
 	protected Injector getInjector() {
		return Guice.createInjector(new ServletModule() {
			@Override
			protected void configureServlets() {
				serve("/_wave/robot/jsonrpc").with(EmailyRobotServlet.class);
			}
		});
 	}
 
 }
