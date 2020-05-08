 /* This Source Code Form is subject to the terms of the Mozilla Public
  * License, v. 2.0. If a copy of the MPL was not distributed with this
  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
  * 
  * Contributor(s):
  *    Marcus Staender <staender@tk.informatik.tu-darmstadt.de>
  *    Aristotelis Hadjakos <telis@tk.informatik.tu-darmstadt.de>
  *    Niklas Lochschmidt <niklas.lochschmidt@stud.tu-darmstadt.de>
  *    Christian Klos <christian.klos@stud.tu-darmstadt.de>
  *    Bastian Renner <bastian.renner@stud.tu-darmstadt.de>
  *
  */
 
 package de.tud.kitchen.main;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.util.tracker.ServiceTracker;
 import org.osgi.util.tracker.ServiceTrackerCustomizer;
 
 import de.tud.kitchen.api.module.KitchenModule;
 import de.tud.kitchen.main.impl.KitchenModuleManager;
 import de.tud.kitchen.main.impl.KitchenFactory;
 
 public class Activator implements BundleActivator {
 	
 	public ServiceTracker kitchenModuleTracker;
 	public KitchenModuleManager kitchenModuleManager;
 	
 	public void start(BundleContext context) throws Exception {
 		kitchenModuleManager = new KitchenModuleManager(new KitchenFactory());
 		kitchenModuleTracker = new ServiceTracker(context, KitchenModule.class.getName(), new KitchenModuleTrackerCustomizer(context));
 		kitchenModuleTracker.open();
 	}
 
 	public void stop(BundleContext context) throws Exception {
 		kitchenModuleTracker.close();
 	}
 	
 	public class KitchenModuleTrackerCustomizer implements ServiceTrackerCustomizer {
 		
 		private BundleContext context;
 		
 		public KitchenModuleTrackerCustomizer(BundleContext context) {
 			this.context = context;
 		}
 		
 		@Override
 		public Object addingService(ServiceReference reference) {
			KitchenModule module = (KitchenModule) context.getService(reference);
			kitchenModuleManager.add(module);
			return module;
 		}
 		
 		@Override
 		public void modifiedService(ServiceReference reference, Object service) {
 			
 		}
 		
 		@Override
 		public void removedService(ServiceReference reference, Object service) {
 			if (service instanceof KitchenModule) {
 				kitchenModuleManager.remove((KitchenModule) service);
 			}
 			kitchenModuleManager.stop();
 		}
 	}
 }
