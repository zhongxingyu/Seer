 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 package org.amanzi.awe;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 
 import net.refractions.udig.catalog.CatalogPlugin;
 import net.refractions.udig.catalog.ICatalog;
 import net.refractions.udig.catalog.IService;
 import net.refractions.udig.internal.ui.UDIGApplication;
 import net.refractions.udig.internal.ui.UDIGWorkbenchAdvisor;
 
 import org.amanzi.neo.core.NeoCorePlugin;
 import org.amanzi.neo.services.ui.NeoServiceProviderUi;
 import org.amanzi.splash.ui.SplashPlugin;
 import org.apache.log4j.Logger;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.equinox.app.IApplication;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.ui.application.IWorkbenchConfigurer;
 import org.eclipse.ui.application.WorkbenchAdvisor;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
 import org.eclipse.ui.internal.ide.model.WorkbenchAdapterBuilder;
 import org.osgi.framework.Bundle;
 /**
  * This is the default application for the Amanzi Wireless Explorer.
  * It is based directly on uDIG, and uses its advisor.
  * 
  * @since AWE 1.0.0
  * @author craig
  */
 public class Application extends UDIGApplication implements IApplication {
     private static final Logger LOGGER = Logger.getLogger(Application.class);
 	/**
 	 * Create the AWE workbench advisor by using the UDIGWorkbenchAdvisor with
 	 * only the perspective changed to match the AWE requirements.
 	 * @see net.refractions.udig.internal.ui.UDIGApplication#createWorkbenchAdvisor()
 	 */
 	@Override
 	protected WorkbenchAdvisor createWorkbenchAdvisor() {
 		return new AWEWorkbenchAdivsor() {
 			/**
 			 * @see org.eclipse.ui.application.WorkbenchAdvisor#initialize(org.eclipse.ui.application.IWorkbenchConfigurer)
 			 */
 			@Override
             public void initialize(IWorkbenchConfigurer configurer) {
 				super.initialize(configurer);
 				configurer.setSaveAndRestore(true);
 			}
 			/**
 			 * @see org.eclipse.ui.application.WorkbenchAdvisor#preStartup()
 			 */
 		    @Override
             public void preStartup() {
 		        // Navigator view needs this
 		        WorkbenchAdapterBuilder.registerAdapters();
 		    }
 		};
 	}
 	
 	private class AWEWorkbenchAdivsor extends UDIGWorkbenchAdvisor {
 		@Override
 		public String getInitialWindowPerspectiveId() {
 			return PerspectiveFactory.AWE_PERSPECTIVE;
 		}
 
         @Override
         public void postStartup() {
             super.postStartup();
             // initialize splash plugin
             SplashPlugin.getDefault();
         }
 		//Lagutko, 30.06.2009, add some icons
 		@Override
 	    public void initialize( IWorkbenchConfigurer configurer ) {
 			super.initialize(configurer);
 			NeoCorePlugin.getDefault().getInitializer().initializeDefaultPreferences();
             createService();
 			final String ICONS_PATH = "icons/full/";
 			final String PATH_OBJECT = ICONS_PATH + "obj16/";
 			Bundle ideBundle = Platform.getBundle(IDEWorkbenchPlugin.IDE_WORKBENCH);
 			
 			declareWorkbenchImage(configurer, ideBundle, IDE.SharedImages.IMG_OBJ_PROJECT,
 			    PATH_OBJECT + "prj_obj.gif", true);
 			declareWorkbenchImage(configurer, ideBundle, IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED,
 			    PATH_OBJECT + "cprj_obj.gif", true);
 		}
 		
         /**
          * create service on Catalog
          */
         public void createService() {
             try {
                 String databaseLocation = NeoServiceProviderUi.getProvider().getDefaultDatabaseLocation();
                 ICatalog catalog = CatalogPlugin.getDefault().getLocalCatalog();
                 List<IService> services = CatalogPlugin.getDefault().getServiceFactory().createService(
                         new URL("file://" + databaseLocation));
                 IService curService = null;
                 for (IService service : services) {
                     LOGGER.debug("Found catalog service: " + service);
                     curService = service;
                     if (catalog.getById(IService.class, service.getIdentifier(), new NullProgressMonitor()) != null) {
                         catalog.replace(service.getIdentifier(), service);
                     } else {
                         catalog.add(service);
                     }
                 }
             } catch (MalformedURLException e) {
                 e.printStackTrace();
                 // TODO Handle MalformedURLException
             } catch (UnsupportedOperationException e) {
                 e.printStackTrace();
                 // TODO Handle UnsupportedOperationException
             }
         }
 
         private void declareWorkbenchImage(IWorkbenchConfigurer configurer_p, Bundle ideBundle, String symbolicName,
 			String path, boolean shared) {
 			URL url = ideBundle.getEntry(path);
 			ImageDescriptor desc = ImageDescriptor.createFromURL(url);
 			configurer_p.declareImage(symbolicName, desc, shared);
 		}
 	}
 
 }
