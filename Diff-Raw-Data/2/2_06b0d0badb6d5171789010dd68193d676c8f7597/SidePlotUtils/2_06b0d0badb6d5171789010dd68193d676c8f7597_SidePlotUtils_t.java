 /*
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.rcp.plotting;
 
 
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.rcp.views.SidePlotView;
 
 public class SidePlotUtils {
 	
 	private static final Logger logger = LoggerFactory.getLogger(SidePlotUtils.class);
 	/**
 	 * Attempts to find the side plot view and logs errors if not
 	 * rather than throwing exceptions.
 	 * @param page
 	 * @param plotViewID
 	 * @return SidePlotView
 	 */
 	public static SidePlotView getSidePlotView(final IWorkbenchPage page,
 			                                   final String         plotViewID) {
 		
 		
 		//if (PlatformUI.getWorkbench().isStarting()) throw new IllegalStateException("Workbench is starting!");
 		
 		SidePlotView sidePlotView = null;
 		// necessary for multiple SPVs
 		try {
 			// Cannot use the findView(...) because of some side plot initiation madness.
 			//sidePlotView = (SidePlotView) page.findView("uk.ac.diamond.scisoft.analysis.rcp.views.SidePlotView");
 
 			//if (sidePlotView==null) {
 				
 				sidePlotView = (SidePlotView) page.showView(SidePlotView.ID,
 						                                    plotViewID, IWorkbenchPage.VIEW_CREATE);
 			//}
 				
 			
 		} catch (PartInitException e) {
 			logger.warn("Could not find side plot ",e);
 		}
 		if (sidePlotView == null) {
//			logger.error("Cannot find side plot");
 			throw new IllegalStateException("Cannot find side plot");
 		}
 
 		return sidePlotView;
 	}
 	
 	
 	public static void bringToTop(final IWorkbenchPage activePage, final IWorkbenchPart part) {
 		
 		if (part.getSite().getShell().isDisposed()) return;
 		
 		// activating the view stops the rogue toolbars appearing
 		// these could also be avoided by moving the toolbars to
 		// eclipse configured things.
 		try {
 			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 				@Override
 				public void run() {
 					activePage.bringToTop(part);
 				}
 			});
 
 		} catch (Exception ne) {
 			logger.error("Cannot acivate plot "+part.getTitle(), ne);
 		} 
 		
 	}
 
 }
