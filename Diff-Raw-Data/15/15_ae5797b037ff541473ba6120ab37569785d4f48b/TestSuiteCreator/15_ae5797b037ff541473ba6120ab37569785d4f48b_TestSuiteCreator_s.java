 package org.mxunit.eclipseplugin.actions;
 
 import java.util.Arrays;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Preferences;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.mxunit.eclipseplugin.MXUnitPlugin;
 import org.mxunit.eclipseplugin.MXUnitPluginLog;
 import org.mxunit.eclipseplugin.model.TestCase;
 import org.mxunit.eclipseplugin.model.TestSuite;
 import org.mxunit.eclipseplugin.preferences.MXUnitPreferenceConstants;
 import org.mxunit.eclipseplugin.properties.MXUnitPropertyManager;
 
 public class TestSuiteCreator {
 	private Preferences prefs;
 	private MXUnitPropertyManager props;   
 	private IPath webrootAsPath;
 	private String testFilter;
 	private TestSuite suite;
 	/* slashes and periods as the component root property indicate "don't prefix anything underneath this container with a root, but don't try to derive the component path from the webroot, either */
 	private String[] emptyPathIndicators = {"/","\\","."};
 	
 	public TestSuiteCreator(){
 		prefs = MXUnitPlugin.getDefault().getPluginPreferences();
 		props  = new MXUnitPropertyManager();
 		webrootAsPath = new Path(prefs.getString(MXUnitPreferenceConstants.P_WEBROOTPATH));
 		testFilter = "(?i)Test.*.cfc|(?i).*Test.cfc";
 		suite = new TestSuite();
 		Arrays.sort(emptyPathIndicators);
 	}
 	
 	
 	/**
 	 * Looks at the resource and its parents for a component root, and at the webroot configured for this plugin. If neither are configured, booooooo.
 	 * @param resource the resource whose tests will be collected
 	 * @return true if shit's OK; false if suckas using this are suckas
 	 */
 	public boolean isResourceConfigured(IResource resource){
 		if(webrootAsPath.toString().length()==0 && !doesResourceHaveConfiguredAncestor(resource)){
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Throws up alert box for an unconfigured resource
 	 * @param resource the dastardly resource actin' a fool
 	 */
 	public void alertIfResourceNotConfigured(IResource resource){		
 		MXUnitPluginLog.logInfo("No webroot or component root defined for resource " + resource.toString());
 		MessageDialog.openInformation( null, "No webroot or component root defined", "Either a webroot or component root must be defined.\n\nIf your test files are located under the webroot, please specify the webroot in Window -- Preferences -- MXUnit.\n\nIf the test files in this project are located outside of your webroot, please specify a component root in Project -- Properties -- MXUnit (or any of its subdirectories)");			
 	}
 	
 	/**
 	 * This is the heart 'n soul of gathering up all the tests for the selected project/folder/test. It crawls
 	 * through directories and finds files that look like tests. It then derives the CFC notation (dot notation) 
 	 * for each file.
 	 * @param resources the selected resources (tests, folders, projects)
 	 * @return a brand spankin' new TestSuite
 	 */
 	public TestSuite createSuite(IResource[] resources){
 		IResource startResource = resources[0];
 		for (int i = 0; i < resources.length; i++) {
 			MXUnitPluginLog.logInfo("Adding to suite with resource named " + resources[i] + "; webroot is " + webrootAsPath.toOSString() + "; rawLocation is " + resources[i].getRawLocation().toString());
 			collectFiles(resources[i]);
 		}
 		
 		if(startResource.getType() == IResource.FOLDER){
 			suite.setName( startResource.getFullPath().toString() );
 		}else{
 			suite.setName( suite.getTestsAsArray()[0].getName() );
 		}
 		
 		return suite;
 	}
 	
 	
 	/**
 	 * climbs a resource's tree to see if it or any ancestors have a component root configured
 	 * @param resource the resource whose tests will be collected
 	 * @return true if any ancestors have a component root; false otherwise
 	 */
 	private boolean doesResourceHaveConfiguredAncestor(IResource resource){
 		boolean configured = false;
 		
 		IResource currentParent = resource;
 		IPath p = resource.getFullPath().removeFileExtension();
 		for(int i = p.segmentCount()-1; i > 0;i--){
 			currentParent = currentParent.getParent();
 			if(props.getComponentPropertyValue(currentParent).trim().length() > 0){
 				configured = true;
 				break;
 			}
 			if(currentParent.getType() == IResource.ROOT){
 				break;
 			}
 		}
 		return configured;
 	}
 	
 	/**
 	 * convenience method for adding a resource to the suite
 	 * @param suite The TestSuite to modify
 	 * @param fullPath full path to component
 	 * @param name component name using dot notation
 	 */
 	private void addTestToSuite(TestSuite suite, String fullPath, String name){
 		TestCase tc = new TestCase();
 		tc.setFilePath(fullPath);
 		tc.setName(name);
 		suite.addTest(tc);
 		MXUnitPluginLog.logInfo("MXUnit TestSuiteCreator: Adding Test To Suite: fullPath is " + fullPath + "; name is" + name);
 	}
 	
 	private void collectFiles(IResource resource){
 		IResourceVisitor visitor = new TestVisitor();
 		try {
 			resource.accept(visitor);
 		} catch (CoreException e) {
 			MXUnitPluginLog.logError("Error visiting test resources with Resource " + resource.toString(), e);
 		}
 	}
 	
 	/**
 	 * a dandy of a function that parses stuff and otherwise creates the DOT-notation CFC paths
 	 * @param resource the resource whose cfc notation will be derived
 	 * @return the cfc notation
 	 */
 	private String deriveCFCPath(IResource resource){
 		String path = "";
 		String propValue = "";
 		
 		//for each cfc found, loop over all parents up to the project;
 		//if any parents have a property configured for cfc path
 		//then attach that root to the component and return;
 		//otherwise, derive it from the webroot
 		
 		IResource currentParent = resource;
		IPath p = resource.getFullPath().removeFileExtension();
 		
 		for(int i = p.segmentCount()-1; i > 0;i--){
 			currentParent = currentParent.getParent();
 			if(currentParent.getType() == IResource.ROOT){
 				break;
 			}
 			
 			propValue = props.getComponentPropertyValue(currentParent).trim();
 			if(propValue.length()>0){
 				if( Arrays.binarySearch(emptyPathIndicators, propValue) >= 0 ){
 					path = p.removeFirstSegments(i).toString().replaceAll("/", ".");
 				}else{
 					path = propValue + "." + p.removeFirstSegments(i).toString().replaceAll("/", ".");
 				}
 				
 				MXUnitPluginLog.logInfo("Using resource " + currentParent + " as source for componentroot property [value="+propValue+"]; path is determined to be " + path);
 				break;
 			}
 		}
 		//we got this far but no parents had a component root defined; now we simply knock the webroot off of the component's path and replace slashes and whatnot.
 		if(path.length() == 0){
			p.removeFirstSegments(webrootAsPath.segmentCount());
			path = p.toString().replaceAll("/", ".").replaceFirst(".", "");//have to remove the first period since the path always starts with the /, and that got converted to a period which hangs at the front.
 		}
 		
 		return path;
 	}
 	
 	
 	class TestVisitor implements IResourceVisitor{
 		public boolean visit(IResource resource) throws CoreException {
 			if(resource.getType() == IResource.FILE && 
					resource.getFullPath().lastSegment().matches(testFilter)){
				
 				String path = deriveCFCPath(resource);
 				addTestToSuite(suite, resource.getRawLocation().toString(), path);
 			}
 			return true;
 		}
 	}
 }
