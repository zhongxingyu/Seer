 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.gda.client;
 
import gda.rcp.GDAClientActivator;

 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.WorkbenchException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import uk.ac.gda.preferences.PreferenceConstants;

 public class OpenXYScanPlotHandler extends AbstractHandler {
 	static final Logger logger = LoggerFactory.getLogger(OpenXYScanPlotHandler.class);
 
 	@Override
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		
		IPreferenceStore preferenceStore = GDAClientActivator.getDefault().getPreferenceStore();
		String viewid = preferenceStore.getString(PreferenceConstants.GDA_OPEN_XYPLOT_ON_SCAN_START_ID);
 		try {
			IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(viewid);
 			view.setFocus();
 		} catch (WorkbenchException e) {
			logger.error("Cannot open plot '"+viewid + "'", e);
 			return Boolean.FALSE;
 		}
 		return Boolean.TRUE;
 	}
 
 }
