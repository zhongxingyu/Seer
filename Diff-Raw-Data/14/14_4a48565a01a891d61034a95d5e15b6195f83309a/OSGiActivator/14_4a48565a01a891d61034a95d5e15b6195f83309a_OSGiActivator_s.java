 package org.gatherdata.archiver.core.osgi;
 
 import static com.google.inject.Guice.createInjector;
 import static org.ops4j.peaberry.Peaberry.osgiModule;
 
 import java.util.logging.Logger;
 
 import org.gatherdata.archiver.core.spi.ArchiverDao;
 import org.gatherdata.archiver.core.spi.ArchiverService;
 import org.ops4j.peaberry.Export;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 
 import com.google.inject.Inject;
 
 /**
  * The OSGi activator for the Gather Archiver Storage bundle.
  * 
  **/
 public class OSGiActivator implements BundleActivator {
 	
 	Logger log = Logger.getLogger(OSGiActivator.class.getName());
 
 	@Inject
 	ArchiverDao archiverDaos;
 	
 	@Inject
 	Export<ArchiverService> archiver;
 	
 	/**
 	 * Implements BundleActivator.start().
 	 * 
 	 * Adds itself as a ServiceListener to the OSGi context and registers a
 	 * Service.
 	 * 
 	 * @param bc the OSGi framework context for the bundle.
 	 **/
 	public void start(BundleContext bc) {
 		
 		createInjector(osgiModule(bc), new GuiceBindingModule()).injectMembers(this);
		
		log.info("Gather Archiver started");
	}
 
 	/**
 	 * Implements BundleActivator.stop(). Prints a message and removes itself
 	 * from the bundle context as a service listener.
 	 * 
 	 * @param context
 	 *            the framework context for the bundle.
 	 **/
 	public void stop(BundleContext context) {
		log.info("Gather Archiver started");
 	}
 	
 }
