 package org.amanzi.awe;
 
 import java.net.URL;
 
 import net.refractions.udig.internal.ui.UDIGApplication;
 import net.refractions.udig.internal.ui.UDIGWorkbenchAdvisor;
 
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.equinox.app.IApplication;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.ui.application.IWorkbenchConfigurer;
 import org.eclipse.ui.application.WorkbenchAdvisor;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
 import org.eclipse.ui.internal.ide.model.WorkbenchAdapterBuilder;
 import org.osgi.framework.Bundle;
import org.amanzi.neo.core.NeoCorePlugin;
 /**
  * This is the default application for the Amanzi Wireless Explorer.
  * It is based directly on uDIG, and uses its advisor.
  * 
  * @since AWE 1.0.0
  * @author craig
  */
 public class Application extends UDIGApplication implements IApplication {
 	
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
 			public void initialize(IWorkbenchConfigurer configurer) {
 				super.initialize(configurer);
 				configurer.setSaveAndRestore(true);
 			}
 			/**
 			 * @see org.eclipse.ui.application.WorkbenchAdvisor#preStartup()
 			 */
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
		
 		//Lagutko, 30.06.2009, add some icons
 		@Override
 	    public void initialize( IWorkbenchConfigurer configurer ) {
 			super.initialize(configurer);
 			NeoCorePlugin.getDefault().getInitializer().initializeDefaultPreferences();
 			final String ICONS_PATH = "icons/full/";
 			final String PATH_OBJECT = ICONS_PATH + "obj16/";
 			Bundle ideBundle = Platform.getBundle(IDEWorkbenchPlugin.IDE_WORKBENCH);
 			
 			declareWorkbenchImage(configurer, ideBundle, IDE.SharedImages.IMG_OBJ_PROJECT,
 			    PATH_OBJECT + "prj_obj.gif", true);
 			declareWorkbenchImage(configurer, ideBundle, IDE.SharedImages.IMG_OBJ_PROJECT_CLOSED,
 			    PATH_OBJECT + "cprj_obj.gif", true);
 		}
 		
 		private void declareWorkbenchImage(IWorkbenchConfigurer configurer_p, Bundle ideBundle, String symbolicName,
 			String path, boolean shared) {
 			URL url = ideBundle.getEntry(path);
 			ImageDescriptor desc = ImageDescriptor.createFromURL(url);
 			configurer_p.declareImage(symbolicName, desc, shared);
 		}
 	}
 
 }
