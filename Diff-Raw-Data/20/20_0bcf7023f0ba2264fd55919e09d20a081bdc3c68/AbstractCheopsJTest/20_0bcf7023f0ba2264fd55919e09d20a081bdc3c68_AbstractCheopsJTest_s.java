 /*******************************************************************************
  * Copyright (c) 2011 Quinten David Soetens
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser Public License v2.1
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  * 
  * Contributors:
  *     Quinten David Soetens - initial API and implementation
  ******************************************************************************/
 package be.ac.ua.ansymo.cheopsj.logger;
 
import hibernate.session.SessionHandler;

 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.WorkbenchException;
 
 import be.ac.ua.ansymo.cheopsj.model.ModelManager;
 import be.ac.ua.ansymo.cheopsj.model.ModelManagerChange;
 
 
 
 public class AbstractCheopsJTest {
 	private static final String JAVA_PERSPECTIVE_ID = "org.eclipse.jdt.ui.JavaPerspective";
 	private static IWorkbenchPage javaPage = null;
 
 	/**
 	 * The object that is being tested.
 	 * 
 	 * @see com.qualityeclipse.favorites.views.FavoritesView
 	 */
 	// protected ChangeView testView;
 	// protected ZestChangeView zestView;
 	protected IJavaProject fJProject1;
 	protected IPackageFragmentRoot fRoot1;
 	protected ModelManager manager;
 	protected ModelManagerChange managerChange;
 
 	/**
 	 * Perform pre-test initialization.
 	 */
 	public void setUp() throws Exception {
 		manager = ModelManager.getInstance();
 		managerChange = ModelManagerChange.getInstance();
 		manager.clearModel();
 
 		// Initialize the test fixture for each test
 		// that is run.
 		waitForJobs();
 		// testView = (ChangeView) getJavaPage().showView(ChangeView.ID);
 		// zestView = (ZestChangeView)
 		// getJavaPage().showView(ZestChangeView.ID);
 
 		waitForJobs();
 
 		// Add additional setup code here.
 		// Create new project ...
 		// fWorkspace= ResourcesPlugin.getWorkspace();
 		fJProject1 = JavaProjectHelper.createJavaProject("TestProject1", "bin");//$NON-NLS-1$//$NON-NLS-2$
 		fJProject1.setRawClasspath(new IClasspathEntry[0], null);
 		fRoot1 = JavaProjectHelper.addSourceContainer(fJProject1, "src");//$NON-NLS-1$
 
 	}
 
 	/**
 	 * Perform post-test cleanup.
 	 */
 	public void tearDown() throws Exception {
 		// Dispose of test fixture.
 		waitForJobs();
 		// delay(300);
 
 		// Add additional teardown code here.
 		//manager.clearModel();
 		JavaProjectHelper.delete(fJProject1);
		SessionHandler.getHandler().cleanupHibernateSessions();
 		// getJavaPage().hideView(testView);
 		// getJavaPage().hideView(zestView);
 	}
 
 	/**
 	 * Open the java perspective if it is not already open
 	 * 
 	 * @return the page displaying the java perspective
 	 */
 	public static IWorkbenchPage getJavaPage() throws WorkbenchException {
 		if (javaPage == null)
 			javaPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().openPage(JAVA_PERSPECTIVE_ID, null);
 		return javaPage;
 	}
 
 	/**
 	 * Process UI input but do not return for the specified time interval.
 	 * 
 	 * @param waitTimeMillis
 	 *            the number of milliseconds
 	 */
 	protected void delay(long waitTimeMillis) {
 		Display display = Display.getCurrent();
 
 		// If this is the UI thread,
 		// then process input.
 
 		if (display != null) {
 			long endTimeMillis = System.currentTimeMillis() + waitTimeMillis;
 			while (System.currentTimeMillis() < endTimeMillis) {
 				if (!display.readAndDispatch())
 					display.sleep();
 			}
 			display.update();
 		}
 		// Otherwise, perform a simple sleep.
 
 		else {
 			try {
 				Thread.sleep(waitTimeMillis);
 			} catch (InterruptedException e) {
 				// Ignored.
 			}
 		}
 	}
 
 	/**
 	 * Wait until all background tasks are complete.
 	 */
 	public void waitForJobs() {
 		while (Job.getJobManager().currentJob() != null)
 			delay(1000);
 	}
 }
