 /*-
  * Copyright © 2013 Diamond Light Source Ltd.
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
 
 package uk.ac.diamond.tomography.reconstruction.dialogs;
 
 import java.util.Collections;
 
 import org.dawnsci.plotting.api.IPlottingSystem;
 import org.dawnsci.plotting.api.PlotType;
 import org.dawnsci.plotting.api.PlottingFactory;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 
 public abstract class BaseRoiDialog extends Dialog {
 	
 	private static final String SHELL_TITLE = "Define ROI";
 
 	private static final String DEFINE_ROI_PLOT = SHELL_TITLE;
 
 	protected IPlottingSystem plottingSystem;
 
 	protected final AbstractDataset image;
 
 	private final int dialogHeight;
 
 	private final int dialogWidth;
 
 	protected BaseRoiDialog(Shell parentShell, AbstractDataset image, int dialogWidth, int dialogHeight) {
 		super(parentShell);
 		this.image = image;
 		this.dialogWidth = dialogWidth;
 		this.dialogHeight = dialogHeight;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	protected final Control createDialogArea(Composite parent) {
 		getShell().setText(SHELL_TITLE);
 		Composite plotComposite = new Composite(parent, SWT.None);
 		GridData layoutData = new GridData(GridData.FILL_BOTH);
 
 		layoutData.widthHint = dialogWidth;
 
 		layoutData.heightHint = dialogHeight;
 		plotComposite.setLayoutData(layoutData);
 		applyDialogFont(plotComposite);
 		plotComposite.setLayout(new FillLayout());
 
 		try {
 			plottingSystem = PlottingFactory.createPlottingSystem();
 		} catch (Exception e) {
 
 		}
 		plottingSystem.createPlotPart(plotComposite, DEFINE_ROI_PLOT, null, PlotType.IMAGE, null);
 
		plottingSystem.createPlot2D(image, null, new NullProgressMonitor());
 
 		doCreateControl(plotComposite);
 
 		return plotComposite;
 	}
 
 	public abstract void doCreateControl(Composite plotComposite);
 }
