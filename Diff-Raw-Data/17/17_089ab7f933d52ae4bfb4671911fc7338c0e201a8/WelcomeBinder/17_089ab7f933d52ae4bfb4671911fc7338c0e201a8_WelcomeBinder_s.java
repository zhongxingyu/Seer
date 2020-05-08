 package de.devsurf.echo.frameworks.rs.examples.welcome;
 
 import javax.inject.Inject;
 
import com.saperion.frameworks.rs.api.InstallableModule;
import com.saperion.frameworks.rs.system.api.Framework;
import com.saperion.frameworks.rs.system.api.ResourceBinder;
 
 public class WelcomeBinder implements InstallableModule {
 	@Inject
 	private ResourceBinder binder;
 	
 	@Override
 	public void install(Framework framework) {
 		binder.publish(WelcomeEndpoint.class).to("welcome").install(framework);
 	}
 }
