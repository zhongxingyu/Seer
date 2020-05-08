 package org.jboss.tools.windup.core.test;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.wst.validation.ValidationFramework;
 import org.jboss.tools.test.util.TestProjectProvider;
 import org.junit.Test;
 
 public class WindupValidatorTest extends TestCase {
 	private static final String WINDUP_DECORATION_MARKER_ID = "org.jboss.tools.windup.core.decorationMarker"; //$NON-NLS-1$
 	private static final String WINDUP_HINT_MARKER_ID = "org.jboss.tools.windup.core.hintMarker"; //$NON-NLS-1$
 	
 	@Test
 	public void testWASEAR() throws Exception {	
 		doWindupValidatorTest("WAS-EAR",
 			new ExpectedMarkerInfo[] {
 				new ExpectedMarkerInfo("IBM Deployment Descriptor", 1)
 			},
 			new ExpectedMarkerInfo[] {});
 	}
 	
 	@Test
 	public void testPortalWAR() throws Exception {
 		doWindupValidatorTest("Portal-WAR",
 			new ExpectedMarkerInfo[] {
 				new ExpectedMarkerInfo("Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-navigation", 3)
 				, new ExpectedMarkerInfo("Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-dynamicui", 4)
 				, new ExpectedMarkerInfo("Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-logic", 5)
 				, new ExpectedMarkerInfo("Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-core", 6)
 				, new ExpectedMarkerInfo("Blacklist Namespace: http://www.ibm.com/xmlns/prod/websphere/portal/v6.0/portal-fmt", 7)
 				, new ExpectedMarkerInfo("Blacklist Namespace: http://www.ibm.com/jsf/html_extended", 8)
 			},
 			new ExpectedMarkerInfo[] {
 				new ExpectedMarkerInfo("IBM specific.", 8)
 			});
 	}
 	
 	/**
 	 * <p>
 	 * Perform a single Windup validator test on a specific test project.
 	 * </p>
 	 * 
 	 * @param projectName
 	 *            name of the project to test
 	 * @param expectedDecorationMarkers
 	 *            expected Windup decoration markers
 	 * @param expectedHintMarkers
 	 *            expected Windup hint markers
 	 * 
 	 * @throws CoreException
 	 *             can happen when performing {@link IProject} operations
 	 * @throws InterruptedException
 	 *             can happen when waiting for validation framework
 	 * @throws OperationCanceledException
 	 *             can happen when waiting for validation framework
 	 */
 	private static void doWindupValidatorTest(String projectName,
 			ExpectedMarkerInfo[] expectedDecorationMarkers, ExpectedMarkerInfo[] expectedHintMarkers) throws CoreException, OperationCanceledException, InterruptedException {
 		
 		TestProjectProvider provider = new TestProjectProvider(
 				"org.jboss.tools.windup.core.test", null, projectName, false);
 		IProject project = provider.getProject();
 		
 		//be sure the project is built
 		project.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
 		
 		//wait for the validation framework to finish
 		ValidationFramework.getDefault().join(new NullProgressMonitor());
 		
 		compareMarkers(project, WINDUP_DECORATION_MARKER_ID, expectedDecorationMarkers);
 		compareMarkers(project, WINDUP_HINT_MARKER_ID, expectedHintMarkers);
 		
 		provider.dispose();
 	}
 	
 	/**
 	 * <p>
 	 * Compare a list of expected markers to the markers actually found on a
 	 * project.
 	 * </p>
 	 * 
 	 * @param project
 	 *            search for expected markers on this project
 	 * @param markerType
 	 *            search for markers of this type on the project
 	 * @param expectedMarkers
 	 *            list of markers that are expected to be found on the project
 	 * 
 	 * @throws CoreException
 	 *             can happen when performing {@link IProject} operations
 	 */
 	private static void compareMarkers(IProject project, String markerType, ExpectedMarkerInfo[] expectedMarkers) throws CoreException {
 		List<IMarker> actualMarkers = new ArrayList<IMarker>(
 				Arrays.asList(project.findMarkers(markerType, true, IResource.DEPTH_INFINITE)));
 		
 		if(expectedMarkers != null) {
 			
 			//for each expected marker
 			List<ExpectedMarkerInfo> expectedMarkersList = new ArrayList<ExpectedMarkerInfo>(
 					Arrays.asList(expectedMarkers));
 			Iterator<ExpectedMarkerInfo> expectedMarkersIter = expectedMarkersList.iterator();
 			while(expectedMarkersIter.hasNext()) {
 				ExpectedMarkerInfo expectedMarker = expectedMarkersIter.next();
 				
 				//for each actual marker
 				Iterator<IMarker> actualMarkersIter = actualMarkers.iterator();
 				while(actualMarkersIter.hasNext()) {
 					IMarker actualMarker = actualMarkersIter.next();
 					
 					//if an actual marker matches an expected marker
 					if(expectedMarker.message.equals(actualMarker.getAttribute(IMarker.MESSAGE))
							&& expectedMarker.lineNumber == (int)actualMarker.getAttribute(IMarker.LINE_NUMBER)) {
 						
 						// remove it from the list of expected markers and found markers
 						actualMarkersIter.remove();
 						expectedMarkersIter.remove();
 					}
 				}
 			}
 			
 			/* if the list of expected markers is not now empty
 			 * then did not find all of the expected markers */
 			if(!expectedMarkersList.isEmpty()) {
 				StringBuffer errorMessage = new StringBuffer();
 				errorMessage.append("Not all expected markers were found on the project: ");
 				for(ExpectedMarkerInfo expectedMarker : expectedMarkersList) {
 					errorMessage.append(expectedMarker);
 					errorMessage.append(", ");
 				}
 				
 				errorMessage.append("    " + actualMarkers.size());
 				
 				fail(errorMessage.toString());
 			}
 		}
 	}
 	
 	/**
 	 * <p>
 	 * Describes an expected marker.
 	 * </p>
 	 */
 	private static class ExpectedMarkerInfo {
 		/**
 		 * <p>
 		 * Message on the expected marker.
 		 * </p>
 		 */
 		private String message;
 		
 		/**
 		 * <p>
 		 * Line number of the expected marker.
 		 * </p>
 		 */
 		private int lineNumber;
 		
 		public ExpectedMarkerInfo(String message, int lineNumber) {
 			this.message = message;
 			this.lineNumber = lineNumber;
 		}
 		
 		@Override
 		public String toString() {
 			return "(" + this.message + " : " + this.lineNumber + ")";
 		}
 	}
 }
