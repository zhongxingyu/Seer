 package org.jalview.jalscape.internal;
 
 import static org.cytoscape.work.ServiceProperties.COMMAND;
 import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
 import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
 import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;
 import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
 import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
 import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
 import static org.cytoscape.work.ServiceProperties.TITLE;
 
 import java.util.Properties;
 
 import org.cytoscape.application.swing.CySwingApplication;
 import org.cytoscape.service.util.AbstractCyActivator;
 import org.cytoscape.service.util.CyServiceRegistrar;
 import org.cytoscape.task.NetworkTaskFactory;
 import org.cytoscape.task.NetworkViewTaskFactory;
 import org.cytoscape.work.TaskFactory;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.jalview.jalscape.internal.model.JalScapeManager;
 import org.jalview.jalscape.internal.tasks.CreateAlignmentTaskFactory;
 
 
 // TODO: Allow opening and closing the molecular navigator dialog
 // TODO: Consider headless mode
 public class CyActivator extends AbstractCyActivator {
 	private static Logger logger = LoggerFactory
 			.getLogger(org.jalview.jalscape.internal.CyActivator.class);
 
 	public CyActivator() {
 		super();
 	}
 
 	public void start(BundleContext bc) {
 
 		// See if we have a graphics console or not
 		boolean haveGUI = true;
 		ServiceReference ref = bc.getServiceReference(CySwingApplication.class.getName());
 
 		if (ref == null) {
 			haveGUI = false;
 			// Issue error and return
 		}
 
		System.out.println("Creating manager");

 		// Create the context object
 		JalScapeManager jalscapeManager = new JalScapeManager(bc, haveGUI);
 
 		// Get a handle on the CyServiceRegistrar
 		CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);
 
		System.out.println("Creating task factory");

 		// Menu task factories
 		TaskFactory createAlignment = new CreateAlignmentTaskFactory(jalscapeManager);
 		Properties createAlignmentProps = new Properties();
 		createAlignmentProps.setProperty(PREFERRED_MENU, "Apps.JalScape");
 		createAlignmentProps.setProperty(TITLE, "Create alignments for nodes");
 		createAlignmentProps.setProperty(COMMAND, "create alignments");
 		createAlignmentProps.setProperty(COMMAND_NAMESPACE, "jalscape");
 		createAlignmentProps.setProperty(ENABLE_FOR, "networkAndView");
 		createAlignmentProps.setProperty(IN_MENU_BAR, "true");
 		createAlignmentProps.setProperty(MENU_GRAVITY, "1.0");

		System.out.println("Registering task factory");
		// registerService(bc, createAlignment, NetworkViewTaskFactory.class, createAlignmentProps);
		registerService(bc, createAlignment, TaskFactory.class, createAlignmentProps);
 	}
 
 }
