 package de.htwg.seapal.trip.app;
 
 import play.Application;
 import play.GlobalSettings;
 import play.Logger;
 import play.api.templates.Html;
 import scala.collection.mutable.StringBuilder;
 
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 import de.htwg.seapal.common.plugin.HookHandler;
 import de.htwg.seapal.common.plugin.HookRegistry;
 import de.htwg.seapal.common.plugin.Initializable;
 
 public class TripGlobal extends GlobalSettings implements Initializable {
 	private static Injector INJECTOR;
 
 	public static Injector createInjector() {
 		return Guice.createInjector(new TripDemoImplModule());
 	}
 
 	@Override
 	public <A> A getControllerInstance(Class<A> controllerClass)
 			throws Exception {
 		if (INJECTOR == null) {
 			INJECTOR = createInjector();
 		}
 
 		return INJECTOR.getInstance(controllerClass);
 	}
 
 
 	@Override
 	public void onStop(Application app) {
 		Logger.info("Trip app shutdown...");
 	}
 
 	@Override
 	public void initHooks(HookRegistry registry) {
 		registry.registerHook("menu.show", new HookHandler<Html, Object>(Html.class, Object.class){
 
 			@Override
 			public Html execute(Object nothing) {
 				StringBuilder builder = new StringBuilder();
				builder.append(String.format("<a href=\"%s\">Trip</a>",
						de.htwg.seapal.trip.controllers.routes.PlayTripController.trips().url()));
				
 				
 				return new Html(builder);
 			}
 		});
 	}
 }
