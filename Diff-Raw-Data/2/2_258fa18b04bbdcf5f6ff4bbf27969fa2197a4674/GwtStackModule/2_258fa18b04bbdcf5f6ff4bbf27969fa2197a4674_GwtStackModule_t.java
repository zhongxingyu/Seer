 package topshelf.utils.guice;
 
 import topshelf.utils.persist.CatchNoResult;
 import topshelf.utils.persist.CatchNoResultHandler;
 import topshelf.utils.persist.EntityPersistModule;
 import topshelf.utils.persist.EntityPersistModule.EntityModuleConfig;
 import topshelf.utils.validation.ValidatorFactoryModule;
 import topshelf.utils.web.NoCacheStaticServlet;
 import topshelf.utils.web.bindery.RequestFactoryModule;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.matcher.Matchers;
 import com.google.inject.persist.PersistFilter;
 import com.google.inject.servlet.ServletModule;
 
 public class GwtStackModule extends AbstractModule {
 
 	private EntityModuleConfig config;
 	
 	public GwtStackModule(EntityModuleConfig config) {
 		this.config = config;
 	}
 
 	@Override
 	protected void configure() {
 
 		install(new EntityPersistModule(config));
		install(new RequestFactoryModule(true));
 
 		// hib creates its own default validatorfactory unless we tell it not to
 		// http://java.dzone.com/articles/tapestry-ioc-aware-jsr-303
 		// we don't want it to because it doesn't use our guice-enabled
 		// validatorfactory
 		//
 		install(new ValidatorFactoryModule());
 		
 		install(new ServletModule() {
 			@Override
 			public void configureServlets() {
 				serve("*.nocache.js").with(NoCacheStaticServlet.class);		
 				filter("/*").through(PersistFilter.class);
 			}
 		});
 		
 		bindInterceptor(Matchers.any(), Matchers.annotatedWith(CatchNoResult.class), new CatchNoResultHandler());
 	}
 }
