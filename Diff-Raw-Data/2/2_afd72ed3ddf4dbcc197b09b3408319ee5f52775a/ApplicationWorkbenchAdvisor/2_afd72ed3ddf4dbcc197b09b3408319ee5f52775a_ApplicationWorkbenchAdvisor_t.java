 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
 package ${package}.eclipse.application;
 
 import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
 import org.eclipse.ui.application.WorkbenchAdvisor;
 import org.eclipse.ui.application.WorkbenchWindowAdvisor;
 
 public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {
 
	private static final String PERSPECTIVE_ID = "${artifactId}.perspective"; //${symbol_dollar}NON-NLS-1${symbol_dollar}
 
     public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
         return new ApplicationWorkbenchWindowAdvisor(configurer);
     }
 
 	public String getInitialWindowPerspectiveId() {
 		return PERSPECTIVE_ID;
 	}
 }
