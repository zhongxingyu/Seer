 package guice;
 
 import play.modules.guice.GuiceSupport;
 import services.DemotivatorCreator;
 import services.DemotivatorCreatorImpl;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.Singleton;
 
public class GuiceConfig extends AbstractModule {
 	@Override
    protected void configure() {
		bind(DemotivatorCreator.class).to(DemotivatorCreatorImpl.class).in(Singleton.class);
     }
 }
