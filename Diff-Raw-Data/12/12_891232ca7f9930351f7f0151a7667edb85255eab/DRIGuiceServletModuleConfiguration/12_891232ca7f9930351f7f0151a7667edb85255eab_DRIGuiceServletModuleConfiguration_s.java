 package vphshare.driservice.config;
 
 import static com.google.inject.matcher.Matchers.annotatedWith;
 import static com.google.inject.matcher.Matchers.any;
 import static com.google.inject.matcher.Matchers.subclassesOf;
 import static com.google.inject.name.Names.bindProperties;
 import static com.google.inject.name.Names.named;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.*;
 
 import javax.mail.Session;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 
 import com.google.common.util.concurrent.ThreadFactoryBuilder;
 import vphshare.driservice.aop.DRIServiceExceptionHandler;
 import vphshare.driservice.aop.MetadataRegistryExceptionHandler;
 import vphshare.driservice.auth.AuthConfigProvider;
 import vphshare.driservice.auth.AuthService;
 import vphshare.driservice.auth.AuthServiceImpl;
 import vphshare.driservice.notification.CombinedNotificationService;
 import vphshare.driservice.providers.MasterInterfaceConfigurationProvider;
 import vphshare.driservice.notification.NotificationService;
 import vphshare.driservice.providers.EmailSessionProvider;
 import vphshare.driservice.registry.lobcder.LobcderConfigurationProvider;
 import vphshare.driservice.providers.PropertiesProvider;
 import vphshare.driservice.registry.MetadataRegistry;
 import vphshare.driservice.registry.lobcder.LobcderMetadataRegistry;
 import vphshare.driservice.service.DRIServiceImpl;
 import vphshare.driservice.task.PeriodicScheduler;
 import vphshare.driservice.validation.DefaultValidationStrategy;
 import vphshare.driservice.validation.ValidationStrategy;
 
 import com.google.inject.AbstractModule;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.guice.JerseyServletModule;
 import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
 
 public class DRIGuiceServletModuleConfiguration extends JerseyServletModule {
 
 	@Override
 	protected void configureServlets() {
 
 		// REST resource bindings
 		Map<String, String> parameters = new HashMap<String, String>();
 		parameters.put("com.sun.jersey.config.property.packages", "vphshare.driservice.service");
 		serve("/dri/*").with(GuiceContainer.class, parameters);
 		
 		install(new AbstractModule() {
 			
 			@Override
 			protected void configure() {
 
                 // Properties bindings
                 bindProperties(binder(), PropertiesProvider.getProperties());
 
 				bind(DRIServiceImpl.class);
 				bind(ValidationStrategy.class).to(DefaultValidationStrategy.class);
 
                 // DRI executor service
                 bind(ThreadPoolExecutor.class)
                         .toInstance(new ThreadPoolExecutor(
                                 5, 5,
                                 0, TimeUnit.SECONDS,
                                 new ArrayBlockingQueue<Runnable>(20),
                                 new ThreadFactoryBuilder()
                                     .setThreadFactory(Executors.defaultThreadFactory())
                                     .setNameFormat("DRI-Executor-%d")
                                     .build(),
                                 new ThreadPoolExecutor.AbortPolicy()));
 
                 // Auth
                 bind(WebResource.class)
                         .annotatedWith(named("auth-config"))
                         .toProvider(AuthConfigProvider.class);
                 bind(AuthService.class)
                        .to(AuthServiceImpl.class)
                         .asEagerSingleton();
 
                 // Notifications
                 bind(NotificationService.class).to(CombinedNotificationService.class);
                 bind(Session.class)
                         .annotatedWith(named("email-notification"))
                         .toProvider(EmailSessionProvider.class);
                 bind(WebResource.class)
                         .annotatedWith(named("mi-notification"))
                         .toProvider(MasterInterfaceConfigurationProvider.class);
 				
 				// Lobcder metadata registry
 				bind(WebResource.class)
 					.annotatedWith(named("lobcder-config"))
 					.toProvider(LobcderConfigurationProvider.class);
                 bind(MetadataRegistry.class).to(LobcderMetadataRegistry.class);
 
                 // Periodic scheduling
                 bind(PeriodicScheduler.class).asEagerSingleton();
 
 				// AOP exception handling
                 MetadataRegistryExceptionHandler mreHandler = new MetadataRegistryExceptionHandler();
                 requestInjection(mreHandler);
                 bindInterceptor(
						subclassesOf(LobcderMetadataRegistry.class),
 						any(),
 						mreHandler);
 
                 DRIServiceExceptionHandler dseHandler = new DRIServiceExceptionHandler();
 				requestInjection(dseHandler);
 				bindInterceptor(
 						annotatedWith(Path.class),
 						annotatedWith(GET.class)
 						.or(annotatedWith(POST.class))
 						.or(annotatedWith(PUT.class))
 						.or(annotatedWith(DELETE.class)),
 						dseHandler);
 			}
 		});
 	}
 }
