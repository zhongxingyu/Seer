 package com.traindirector;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
import org.eclipse.jetty.deploy.App;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.application.ActionBarAdvisor;
 import org.eclipse.ui.application.IActionBarConfigurer;
 import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
 import org.eclipse.ui.application.WorkbenchWindowAdvisor;
 
 import com.traindirector.editors.WelcomePage;
 import com.traindirector.files.IniFile;
import com.traindirector.web.server.WebServer;
 
 public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {
 
 	public ApplicationActionBarAdvisor _actionBarAdvisor;
 	
     public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
         super(configurer);
     }
 
     public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
         _actionBarAdvisor = new ApplicationActionBarAdvisor(configurer);
         return _actionBarAdvisor;
     }
     
     @Override
     public void preWindowOpen() {
         IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
         configurer.setInitialSize(new Point(1200, 700));
         configurer.setShowCoolBar(true);
         configurer.setShowStatusLine(false);
     }
     
     @Override
     public void postWindowOpen() {
     	super.postWindowOpen();
     	IniFile initFile = new IniFile(Application._simulator);
     	initFile.load();
     	WelcomePage.openEditor(_actionBarAdvisor._window, "Home");
     	
     	Application._simulator.initWebServer();
     }
     
     @Override
 	public IStatus saveState(IMemento memento) {
 		super.saveState(memento);
 		return Status.OK_STATUS;
 	}
 	
     @Override
 	public IStatus restoreState(IMemento memento) {
     	super.restoreState(memento);
 		return Status.OK_STATUS;
 	}
 }
