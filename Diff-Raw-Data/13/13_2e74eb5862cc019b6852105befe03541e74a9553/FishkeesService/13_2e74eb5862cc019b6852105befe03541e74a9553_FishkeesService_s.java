 package com.fishkees.backend.service;
 
 import java.util.List;
 
 import com.fishkees.backend.configuration.FishkeesConfiguration;
 import com.fishkees.backend.healthcheck.HealthChecksModule;
 import com.fishkees.backend.healthcheck.PingHealthCheck;
 import com.fishkees.backend.modules.lists.ListsModule;
 import com.fishkees.backend.modules.lists.resources.FlashcardListResource;
 import com.google.common.collect.Lists;
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.yammer.dropwizard.Service;
 import com.yammer.dropwizard.config.Bootstrap;
 import com.yammer.dropwizard.config.Environment;
 
 public class FishkeesService extends Service<FishkeesConfiguration> {
 	private static final String APPLICATION_NAME = "fishkees";
 	private Injector injector;
 
 	public static void main(String[] args) throws Exception {
 		new FishkeesService().run(args);
 	}
 
 	@Override
 	public void initialize(Bootstrap<FishkeesConfiguration> bootstrap) {
 		bootstrap.setName(APPLICATION_NAME);
 	}
 
 	@Override
 	public void run(FishkeesConfiguration configuration, Environment environment)
 			throws Exception {
 		setInjector(configuration);
 		environment.addResource(injector
 				.getInstance(FlashcardListResource.class));
 
 		environment.addHealthCheck(injector.getInstance(PingHealthCheck.class));
 	}
 
 	private void setInjector(FishkeesConfiguration config) {
 		List<AbstractModule> modules = Lists.newLinkedList();
 		modules.add(new ListsModule(config.getFixturesConfiguration()));
 		modules.add(new HealthChecksModule());
 		injector = Guice.createInjector(modules);
 	}
 }
